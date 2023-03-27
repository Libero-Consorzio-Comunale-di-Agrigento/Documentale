package it.finmatica.modulistica.modulisticapack;
    
import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
import java.io.Serializable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;

import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;
 
/**
 * 
 */
public class ListaDomini implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected ArrayList<Dominio> domini;
//  private   boolean   isVerbose = Parametri.ISVERBOSE;
  private static Logger logger = Logger.getLogger(ListaDomini.class);
  private  boolean debuglog = logger.isDebugEnabled();

  /**
   * Costruttore della lista dei Domini.
   * @author Adelmo
   * @param pRequest è la request della eventuale servlet che richiama il dominio
   *                 nel caso in cui questo contenga dei parametri si suppone infatti che 
   *                 i valori con cui sostituirli siano delle variabili legate alla sessione 
   *                 HTML a cui è legata la creazione di questa lista di Domini.
   */
  public ListaDomini() throws Exception {
    domini = new ArrayList<Dominio>();
  }

  public Dominio getDominio(String pArea, String pDominio, String cm, HttpServletRequest pRequest) {
    return getDominio(pArea,pDominio,cm,pRequest,(IDbOperationSQL)null);
  }

  /**
   * Cerca un particolare domino all'interno della lista e se non lo trova lo carica
   * direttamente dal databse.
   * 
   * @author  Nicola Samoggia
   *          Adelmo Gentilini
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   *          pDominio  nome del dmonio da caricare
   * @return o domino esistente o dominio nuovo
   */
  /// settare il dbop 
  public Dominio getDominio(String pArea, String pDominio, String cm, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
      int     i = 0;
      boolean trovato = false;
      Dominio d = null;
      
      try {
        // Se il size è 0 non ci sono elementi quindi trovato resterà 'false'
        if (domini.size() > 0) {
//          d = (Dominio)domini.get(i);
          while ((!trovato) && (i < domini.size())) {
            d = (Dominio)domini.get(i);
            if ((pArea.equals(d.getArea())) && (pDominio.equals(d.getDominio())) && (cm.equals(d.getCodiceModello()))) {
              trovato = true;
            } else {
              i = i + 1;
//              d = (Dominio)domini.get(i);
            }
          }
        }
      } catch(Exception e) {
        loggerError("ListaDomini::getDominio() (1) - Area: "+pArea+" - Dominio: "+pDominio+" - Attenzione! Si è verificato un errore: "+e.toString(),e);
//        e.printStackTrace();
        trovato = false;
      }



      if (!trovato) {
        d = caricaDominio(pArea, pDominio, cm, pRequest,dbOpEsterna);
      } else {
        d.caricaValori(pRequest,dbOpEsterna);
      }


      return d;
  }

  public Dominio getDominio(String pArea, String pDominio, String cm, HttpServletRequest pRequest, AbstractParser ab) {
      Dominio d = null;

      d = caricaDominio(pArea, pDominio, cm, pRequest);
      d.caricaValori(ab);
      return d;
  }

  public void caricaDominiiDiArea(String pArea, HttpServletRequest pRequest, boolean primo_caricamento) {
    caricaDominiiDiArea(pArea, pRequest, primo_caricamento,null);
  }

  /**
   * Cerca i dominii di area per l'area indicata. Li restituisce come lista ordinata di dominii
   * in base al numero di sequenza memorizzato in tabella.
   * Utilizza il vecchio metodo di caricamento dei domini (che aveva bisogno del nome di dominio)
   * 
   * @author  Antonio Plastini
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   * @return  Array di dominii di area (se ne esistono) o null in caso contrario.
   */
  public void caricaDominiiDiArea(String pArea, HttpServletRequest pRequest, boolean primo_caricamento, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ListaDomini::caricaDominiiDiArea - Inizio",pArea,"",0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    String      persDom = null;
    String      nomeDominio = null;
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominioDiAreaCorrente = null;

    Personalizzazioni pers = null;
    pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    if (primo_caricamento) {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA in ('P','I') AND "+
                       "CODICE_MODELLO = '-' AND "+
                       "PERSONALIZZAZIONE = 'N' "+
              "ORDER BY SEQ_DOMINIO_AREA";
    } else {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA = 'P' AND "+
                       "CODICE_MODELLO = '-' AND "+
                       "PERSONALIZZAZIONE = 'N' "+
              "ORDER BY SEQ_DOMINIO_AREA";
    }
    
    try {
      if (dbOpEsterna == null)
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      else
        dbOp = dbOpEsterna;
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      while (rs.next()) {
        nomeDominio = rs.getString("DOMINIO");
        if (pers != null) {
          persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, pArea+"#"+nomeDominio+"#-");
          int j = persDom.indexOf("#");
          int k = persDom.lastIndexOf("#");
          nomeDominio = persDom.substring(j+1,k);
        }
        tempArray.add(i, nomeDominio);
        i++;
      }
    } catch (Exception e) {
      loggerError("ListaDomini::caricaDominiiDiArea() (2) - Attenzione! Si è verificato un errore: "+e.toString(),e);
      //Debug Tempo
      stampaTempo("ListaDomini::caricaDominiiDiArea - Fine",pArea,"",ptime);
      //Debug Tempo
      return;
    }
    finally {
      if (dbOpEsterna == null) {
        free(dbOp);
      }
    }

    // Se non ci sono domini di area posso uscire.
    int size = tempArray.size();
    if (size == 0) { 
      //Debug Tempo
      stampaTempo("ListaDomini::caricaDominiiDiArea - Fine",pArea,"",ptime);
      //Debug Tempo
      return;
    }
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    //result = new Dominio[size];
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);
      
      dominioDiAreaCorrente = getDominio(pArea, sDominio, "-", pRequest);

      try {
        dominioDiAreaCorrente.setDominioDiArea(true);
        domini.add(dominioDiAreaCorrente);
        pRequest.getSession().setAttribute("listaDomini",this);
      } catch (Exception egd) {
        loggerError("ListaDomini::getDominiiDiArea() (3) - Dominio: "+sDominio+" - Errore scrittura su RepositoryTemp: "+egd.toString(),egd);
      }
    }    
    //Debug Tempo
    stampaTempo("ListaDomini::caricaDominiiDiArea - Fine",pArea,"",ptime);
    //Debug Tempo
  }

  public void caricaDominiiFormulaModello(String pArea, String cm, HttpServletRequest pRequest) {
    caricaDominiiFormulaModello( pArea,  cm,  pRequest, null);
  }

  /**
   * Cerca i dominii di area per l'area indicata. Li restituisce come lista ordinata di dominii
   * in base al numero di sequenza memorizzato in tabella.
   * Utilizza il vecchio metodo di caricamento dei domini (che aveva bisogno del nome di dominio)
   * 
   * @author  Antonio Plastini
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   * @return  Array di dominii di area (se ne esistono) o null in caso contrario.
   */
  public void caricaDominiiFormulaModello(String pArea, String cm, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ListaDomini::caricaDominiiFormulaModello - Inizio",pArea,cm,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    String      persDom = null;
    String      nomeDominio = null;
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominioFormulaModello = null;

    Personalizzazioni pers = null;
    pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    query = "SELECT   DOMINIO "+
            "FROM     DOMINI "+
            "WHERE    AREA = :AREA AND "+
                     "SEQ_DOMINIO_AREA > 0 AND "+
                     "PRECARICA = 'M' AND "+
                     "CODICE_MODELLO = :CM AND "+
                       "PERSONALIZZAZIONE = 'N' "+
            "ORDER BY SEQ_DOMINIO_AREA";
            
    
    try {
      if (dbOpEsterna == null)
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      else
        dbOp = dbOpEsterna;
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      while (rs.next()) {
      	nomeDominio = rs.getString("DOMINIO");
        if (pers != null) {
          persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, pArea+"#"+nomeDominio+"#"+cm);
          int j = persDom.indexOf("#");
          int k = persDom.lastIndexOf("#");
          nomeDominio = persDom.substring(j+1,k);
        }
        tempArray.add(i, nomeDominio);
        i++;
      }
    } catch (Exception e) {
      loggerError("ListaDomini::caricaDominiiFormulaModello() (2) - Attenzione! Si è verificato un errore: "+e.toString(),e);
      //Debug Tempo
      stampaTempo("ListaDomini::caricaDominiiFormulaModello - Fine",pArea,cm,ptime);
      //Debug Tempo
      return;
    }
    finally {
      if (dbOpEsterna == null) free(dbOp);
    }

    
    // Se non ci sono domini di area posso uscire.
    int size = tempArray.size();
    if (size == 0) {
      //Debug Tempo
      stampaTempo("ListaDomini::caricaDominiiFormulaModello - Fine",pArea,cm,ptime);
      //Debug Tempo
      return;
    }
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    //result = new Dominio[size];
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);
      
      dominioFormulaModello = getDominio(pArea, sDominio, cm, pRequest);

      try {
        dominioFormulaModello.setDominioFormulaModello(true);
        domini.add(dominioFormulaModello);
        pRequest.getSession().setAttribute("listaDomini",this);
      } catch (Exception egd) {
        loggerError("ListaDomini::caricaDominiiFormulaModello() (3) - Dominio: "+sDominio+" - Errore scrittura su RepositoryTemp: "+egd.toString(),egd);
      }
    }    
    //Debug Tempo
    stampaTempo("ListaDomini::caricaDominiiFormulaModello - Fine",pArea,cm,ptime);
    //Debug Tempo
  }


  public void aggiornaDomini(HttpServletRequest pRequest) {
    aggiornaDomini(pRequest,null);
  }

  /**
   * Ricarica tutti i valori dei domini presenti nella lista.
   * Questa funzione dovrà effettuare il caricamento solo per quei domini 
   * che sono in qualche modo parametrici in quanto gli altri sono sostanzialmente
   * fissi.
   * 
   * @param   pRequest   l'oggetto che rappresenta la richiesta HTTP fatta al servlet
   * @author  Adelmo Gentilini
   */
  public void aggiornaDomini(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    Dominio d;
    boolean trovato = false;
    int     i = 0;

    // Se il size è 0 non ci sono elementi quindi trovato resterà 'false'
    if (domini.size() > 0){
      while ((!trovato) && (i < domini.size())) {
        d = (Dominio)domini.get(i);
//        if (d.isParametrico()==true){
          // Il dominio è parametrico quindi essendo già in lista va ricaricato
          d.caricaValori(pRequest, dbOpEsterna);
//        }
        i = i + 1;
      }
    }
    
  }

  public Dominio caricaDominio(String pArea, String pDominio, String cm, HttpServletRequest request) {
      return caricaDominio( pArea,  pDominio,  cm,  request,  null);
  }

  /**
   * Carica dal database il dominio.
   * 
   * @author  Nicola Samoggia
   *          Adelmo Gentilini
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   *          pDominio  nome del dominio da caricare
   *          request   l'oggetto che rappresenta la richiesta HTTP fatta al servlet
   * @return  nuova istanza di domino caricata dal database oppure null se il 
   *          domino non è riconosciuto
   */
  public Dominio caricaDominio(String pArea, String pDominio, String cm, HttpServletRequest request, IDbOperationSQL dbOpEsterna) {
    IDbOperationSQL dbOp = null;
    ResultSet   resultQuery = null;
    String      query, 
                tipo, 
                istruzione = null,
//                lettura = null,
                driver = null,
                ordinamento = null,
                connessione, 
                user, 
                passwd,
                sDsn = "";
    Dominio     d = null;

    query = "SELECT * FROM DOMINI " +
            "WHERE AREA = :AREA AND "+
            "      DOMINIO = :DOMINIO"+
            "  AND CODICE_MODELLO = :CM"+
            "  AND PRECARICA IN ('S','P','M','I')";
            
    try {
      if (dbOpEsterna == null)
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      else
        dbOp= dbOpEsterna;

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", cm);
      dbOp.setParameter(":DOMINIO", pDominio);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      
      if (resultQuery.next()) {
        tipo = resultQuery.getString("TIPO");
        ordinamento = resultQuery.getString("ORDINAMENTO");

        if (tipo.equals("O") || tipo.equals("F") ||  tipo.equals("J")) {
          // Ricavo l'istruzione (il campo è un CLOB).
          Clob clob = resultQuery.getClob("ISTRUZIONE");
          long clobLen = clob.length();
          if (clobLen < Dominio.MAXLEN_ISTRUZIONE) {
            int i_clobLen = (int)clobLen;
            istruzione = clob.getSubString(1, i_clobLen);
          } else {
            logger.error("ListaDomini::caricaDominio() - Attenzione! Si è verificato un errore. L'istruzione per il caricamento del dominio supera i "+Dominio.MAXLEN_ISTRUZIONE+" caratteri.");
          }
          
          if (!tipo.equals("J")) {
            // Determino il driver da utilizzare
            driver = resultQuery.getString("DRIVER");
            if ((driver == null) || (driver.trim().length() == 0)) {
              driver = "";  // Bridge STANDARD JDBC-ODBC
            }
          }
        }

        // Leggo i parametri di connessione
        connessione = resultQuery.getString("CONNESSIONE");
        if (connessione == null) {
          connessione = "";
        } 
        user = resultQuery.getString("UTENTE");
        passwd = resultQuery.getString("PASSWD");

        sDsn = resultQuery.getString("DSN");
        if (sDsn == null) {
          sDsn = "";
        }
        if (sDsn.length() != 0) {
          Connessione cn = new Connessione(dbOp,sDsn);
          driver      = cn.getDriver();
          connessione = cn.getConnessione();
          user        = cn.getUtente();
          passwd      = cn.getPassword();
        }

        if (tipo.equals("S"))
          d = new DominioStandard(pArea, cm, pDominio, tipo, ordinamento, request,dbOpEsterna);
        else if (tipo.equals("J")) 
          d = new DominioJava(pArea, pDominio, cm, tipo, ordinamento, istruzione, request,dbOpEsterna);
        else if (tipo.equals("N")) 
          d = new DominioNotes(pArea, pDominio, cm, tipo, ordinamento, connessione, request,dbOpEsterna);
        else if (tipo.equals("O"))
          d = new DominioCONN(pArea, pDominio, cm, tipo, ordinamento, driver, connessione, user, passwd, istruzione, request,dbOpEsterna);
        else if (tipo.equals("F"))
          d = new DominioFunction(pArea, pDominio, cm, tipo, ordinamento, driver, connessione, user, passwd, istruzione, request,dbOpEsterna);
        else
          d = null;  // TIPO DOMINIO NON RICONOSCIUTO
      } else {
        // Il dominio cercato NON è presente nel database
        d = null;
      }
      
    } catch(Exception e) {
      loggerError("ListaDomini::caricaDominio() - Attenzione: il dominio "+pDominio+" presenta delle anomalie. "+e.toString(),e);
//      e.printStackTrace();
      d = null;
    }
    finally {
      if (dbOpEsterna == null) {
        free(dbOp);
      }
    }

    return d;
  }

  public void caricaDominiiDelModello(String pArea, String cm, HttpServletRequest pRequest, boolean primo_caricamento) {
    caricaDominiiDelModello(pArea,cm,pRequest,primo_caricamento,null);
  }

  /**
   * 
   */
  public void caricaDominiiDelModello(String pArea, String cm, HttpServletRequest pRequest, boolean primo_caricamento, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ListaDomini::caricaDominiiDelModello - Inizio",pArea,cm,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    String      persDom = null;
    String      nomeDominio = null;
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominioDelModello = null;

    Personalizzazioni pers = null;
    pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    if (primo_caricamento) {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA in ('P','I') AND "+
                       "CODICE_MODELLO = :CM AND "+
                       "PERSONALIZZAZIONE = 'N' "+
              "ORDER BY SEQ_DOMINIO_AREA";
    } else {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA = 'P' AND "+
                       "CODICE_MODELLO = :CM AND "+
                       "PERSONALIZZAZIONE = 'N' "+
              "ORDER BY SEQ_DOMINIO_AREA";
    }        
    
    try {
      if (dbOpEsterna == null)
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      else
        dbOp = dbOpEsterna;
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      while (rs.next()) {
        nomeDominio = rs.getString("DOMINIO");
        if (pers != null) {
          persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, pArea+"#"+nomeDominio+"#"+cm);
          int j = persDom.indexOf("#");
          int k = persDom.lastIndexOf("#");
          nomeDominio = persDom.substring(j+1,k);
        }
        tempArray.add(i, nomeDominio);
        i++;
      }
    } catch (Exception e) {
      loggerError("ListaDomini::caricaDominiiDelModello() - Attenzione! Si è verificato un errore: "+e.toString(),e);
      if (dbOpEsterna == null) {
        free(dbOp);
      }
      //Debug Tempo
      stampaTempo("ListaDomini::caricaDominiiDelModello - Fine",pArea,cm,ptime);
      //Debug Tempo
      return;
    }
    finally {
      if (dbOpEsterna == null) {
        free(dbOp);
      }
    }
    
    // Se non ci sono domini posso uscire.
    int size = tempArray.size();
    if (size == 0) {
      //Debug Tempo
      stampaTempo("ListaDomini::caricaDominiiDelModello - Fine",pArea,cm,ptime);
      //Debug Tempo
      return;
    }
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    //result = new Dominio[size];
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);

      dominioDelModello = getDominio(pArea, sDominio, cm, pRequest);

      try {
        dominioDelModello.setDominioDelModello(true);
        domini.add(dominioDelModello);
        pRequest.getSession().setAttribute("listaDomini",this);
      } catch (Exception egd) {
        loggerError("ListaDomini::caricaDominiiDelModello() - "+egd.toString(),egd);
      }
    }    
    //Debug Tempo
    stampaTempo("ListaDomini::caricaDominiiDelModello - Fine",pArea,cm,ptime);
    //Debug Tempo
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
//      dbOp.getStmSql().clearParameters();
      dbOp.close();
    } catch (Exception e) { }
  }

  /**
   * 
   */
  public int getNumDomini() {
    return domini.size();
  }

  /**
   * 
   */
  public Dominio getDominio(int pos) {
    return (Dominio)domini.get(pos);
  }

  /**
   * 
   */
  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }
  private long stampaTempo(String sMsg, String area, String cm, long ptime) {
    if (!debuglog) {
      return 0;
    }
    long adesso = Calendar.getInstance().getTimeInMillis();
    long trascorso = 0;
    if (ptime > 0) {
      trascorso = adesso - ptime;
    }
    long min_time = Long.parseLong(Parametri.MIN_TIME_LOG);
    if (trascorso < min_time) {
      return adesso;
    }
    if (Parametri.DEBUG.equalsIgnoreCase("1") && ptime > 0) {
      logger.debug("\n"+sMsg+"\nArea:"+area+" -Codice Modello:\n"+cm+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -Codice Modello:\n"+cm+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }
}
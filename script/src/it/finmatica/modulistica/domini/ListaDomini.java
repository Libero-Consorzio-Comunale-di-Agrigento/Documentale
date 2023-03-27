package it.finmatica.modulistica.domini;

import java.util.*;
import java.sql.*;
import java.io.Serializable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;
//import oracle.jdbc.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.connessioni.Connessione;


public class ListaDomini implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -3436891549012243516L;
  public ArrayList<Dominio> domini;
//  private String sDebug = "2";
  private Connection      pConn;
  private static Logger logger = Logger.getLogger(ListaDomini.class);

  public ListaDomini(Connection pConn)  throws Exception  {
    this.pConn = pConn;
    domini = new ArrayList<Dominio>();
    if (Parametri.PARAM_CARICATI == 0) {
    	Parametri.leggiParametriConnection(this.pConn);
    }
  }

  /**
   * Cerca un particolare domino all'interno della lista e se non lo trova lo carica
   * direttamente dal databse.
   * 
   * @author  Marco Bonforte
   * @param   pArea     area di appartenenza del domino
   *          pDominio  nome del dmonio da caricare
   *          pAbPar    AbstractParser
   * @return o domino esistente o dominio nuovo
   */
  public Dominio getDominio(String pArea, String pDominio, String cm, AbstractParser pAbPar) {
      int     i = 0;
      boolean trovato = false;
      Dominio d = null;
      
      try {
        // Se il size è 0 non ci sono elementi quindi trovato resterà 'false'
        if (domini.size() > 0) {
          while ((!trovato) && (i < domini.size())) {
            d = (Dominio)domini.get(i);
            if ((pArea.equals(d.getArea())) && (pDominio.equals(d.getDominio()))) {
              trovato = true;
            } else {
              i = i + 1;
            }
          }
        }
      } catch(Exception e) {
        loggerError("ListaDominiDMS::getDominio() (1) - Area: "+pArea+" - Dominio: "+pDominio+" - Attenzione! Si è verificato un errore: "+e.toString(),e);
        trovato = false;
      }
      
      if (!trovato) {
        d = caricaDominio(pArea, pDominio, cm, pAbPar);
      } else {
        if (!d.tipo.equalsIgnoreCase("S")) {
          d.caricaValori(pAbPar);
        }
      }
      return d;
  }

  /**
   * Cerca i dominii di area per l'area indicata. Li restituisce come lista ordinata di dominii
   * in base al numero di sequenza memorizzato in tabella.
   * 
   * @author  Marco Bonforte
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   *          pAbPar    AbstractParser
   * @return  Array di dominii di area (se ne esistono) o null in caso contrario.
   */
  public void caricaDominiiDiArea(String pArea, AbstractParser pAbPar, boolean primo_caricamento) {
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominioDiAreaCorrente = null;

    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    if (primo_caricamento) {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA in ('P','I') AND "+
                       "CODICE_MODELLO = '-' " +
              "ORDER BY SEQ_DOMINIO_AREA";
    } else {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA = 'P' AND "+
                       "CODICE_MODELLO = '-' " +
              "ORDER BY SEQ_DOMINIO_AREA";
    }
            

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      while (rs.next()) {
        tempArray.add(i, rs.getString("DOMINIO"));
        i++;
      }
    } catch (Exception e) {
      loggerError("ListaDominiDMS::caricaDominiiDiArea() (2) - Attenzione! Si è verificato un errore: "+e.toString(),e);
      return;
    }
    finally {
      free(dbOp);
    }
    

    
    // Se non ci sono domini di area posso uscire.
    int size = tempArray.size();
    if (size == 0) 
      return;
   
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    //result = new Dominio[size];
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);
      
      dominioDiAreaCorrente = getDominio(pArea, sDominio, "-", pAbPar);

      try {
        dominioDiAreaCorrente.setDominioDiArea(true);
        domini.add(dominioDiAreaCorrente);
      } catch (Exception egd) {
        loggerError("ListaDominiDMS::getDominiiDiArea() (3) - Dominio: "+sDominio+" - Errore scrittura su RepositoryTemp: "+egd.toString(),egd);
      }
    }    
  }
  
  /**
   * Cerca i dominii Formula Modello per il modello indicato. Li restituisce come lista ordinata di dominii
   * in base al numero di sequenza memorizzato in tabella.
   * 
   * @author  Antonio Plastini
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   *          cm        codice del modello
   *          pAbPar    AbstractParser
   * @return  Array di dominii di area (se ne esistono) o null in caso contrario.
   */
  public void caricaDominiiFormulaModello(String pArea, String cm, AbstractParser pAbPar) {
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominioFormulaModello = null;

    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    query = "SELECT   DOMINIO "+
            "FROM     DOMINI "+
            "WHERE    AREA = :AREA AND "+
                     "SEQ_DOMINIO_AREA > 0 AND "+
                     "PRECARICA = 'M' AND "+
                     "CODICE_MODELLO = :CM "+
            "ORDER BY SEQ_DOMINIO_AREA";
            
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      while (rs.next()) {
        tempArray.add(i, rs.getString("DOMINIO"));
        i++;
      }
    } catch (Exception e) {
      loggerError("ListaDominiDMS::caricaDominiiFormulaModello() (2) - Attenzione! Si è verificato un errore: "+e.toString(),e);
      return;
    }
    finally {
      free(dbOp);
    }
    

    
    // Se non ci sono domini di area posso uscire.
    int size = tempArray.size();
    if (size == 0) 
      return;
   
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);
      
      dominioFormulaModello = getDominio(pArea, sDominio, cm, pAbPar);

      try {
        dominioFormulaModello.setDominioFormulaModello(true);
        domini.add(dominioFormulaModello);
      } catch (Exception egd) {
        loggerError("ListaDominiDMS::caricaDominiiFormulaModello() (3) - Dominio: "+sDominio+" - Errore scrittura su RepositoryTemp: "+egd.toString(),egd);
      }
    }    
  }
  
  /**
   * Ricarica tutti i valori dei domini presenti nella lista.
   * Questa funzione dovrà effettuare il caricamento solo per quei domini 
   * che sono in qualche modo parametrici in quanto gli altri sono sostanzialmente
   * fissi.
   * 
   * @param   pAbPar   AbstractParser
   * @author  Marco Bonforte
   */
  public void aggiornaDomini(AbstractParser pAbPar) {
    Dominio d;
    boolean trovato = false;
    int     i = 0;

    // Se il size è 0 non ci sono elementi quindi trovato resterà 'false'
    if (domini.size() > 0){
      while ((!trovato) && (i < domini.size())) {
        d = (Dominio)domini.get(i);
        if (d.isParametrico()==true){
          // Il dominio è parametrico quindi essendo già in lista va ricaricato
          d.caricaValori(pAbPar);
        }
        i = i + 1;
      }
    }
    
  }

  /**
   * Carica dal database il dominio.
   * 
   * @author  Marco Bonforte
   * @param   pConn     connessione al databse
   *          pArea     area di appartenenza del domino
   *          pDominio  nome del dominio da caricare
   *          pAbPar    AbstractParser
   * @return  nuova istanza di domino caricata dal database oppure null se il 
   *          domino non è riconosciuto
   */
  public Dominio caricaDominio(String pArea, String pDominio, String cm, AbstractParser pAbPar) {
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
            "  AND CODICE_MODELLO = :CM "+
            "  AND PRECARICA IN ('S','P','M','I')";
            
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
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
            logger.error("ListaDominiDMS::caricaDominio() - Attenzione! Si è verificato un errore. L'istruzione per il caricamento del dominio supera i "+Dominio.MAXLEN_ISTRUZIONE+" caratteri.");
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
        if (!sDsn.equalsIgnoreCase("")) {
          Connessione cn = new Connessione(dbOp,sDsn);
          driver      = cn.getDriver();
          connessione = cn.getConnessione();
          user        = cn.getUtente();
          passwd      = cn.getPassword();
        }

        if (tipo.equals("S"))
          d = new DominioStandard(pConn, pArea, pDominio, tipo, ordinamento, pAbPar);
        else if (tipo.equals("J")) 
          d = new DominioJava(pConn, pArea, pDominio, tipo, ordinamento, istruzione, pAbPar);
        else if (tipo.equals("N")) 
          d = new DominioNotes(pConn, pArea, pDominio, tipo, ordinamento, connessione, pAbPar);
        else if (tipo.equals("O"))
          d = new DominioCONN(pConn, pArea, pDominio, tipo, ordinamento, driver, connessione, user, passwd, istruzione, pAbPar);
        else if (tipo.equals("F"))
          d = new DominioFunction(pConn, pArea, pDominio, tipo, ordinamento, driver, connessione, user, passwd, istruzione, pAbPar);
        else
          d = null;  // TIPO DOMINIO NON RICONOSCIUTO
      } else {
        // Il dominio cercato NON è presente nel database
        d = null;
      }
      
    } catch(Exception e) {
      loggerError("ListaDominiDMS::caricaDominio() - Attenzione: il dominio "+pDominio+" presenta delle anomalie. "+e.toString(),e);
//      e.printStackTrace();
      d = null;
    }
    finally {
      free(dbOp);
    }


    return d;
  }

  /**
   * 
   */
  public void caricaDominiiDelModello(String pArea, String cm, AbstractParser pAbPar, boolean primo_caricamento) {
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominioDelModello = null;

    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    if (primo_caricamento) {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA in ('P','I') AND "+
                       "CODICE_MODELLO = :CM "+
              "ORDER BY SEQ_DOMINIO_AREA";
    } else {
      query = "SELECT   DOMINIO "+
              "FROM     DOMINI "+
              "WHERE    AREA = :AREA AND "+
                       "SEQ_DOMINIO_AREA > 0 AND "+
                       "PRECARICA = 'P' AND "+
                       "CODICE_MODELLO = :CM "+
              "ORDER BY SEQ_DOMINIO_AREA";
    }        
            
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      while (rs.next()) {
        tempArray.add(i, rs.getString("DOMINIO"));
        i++;
      }
    } catch (Exception e) {
      loggerError("ListaDominiDMS::caricaDominiiDelModello() - Attenzione! Si è verificato un errore: "+e.toString(),e);
      return;
    }
    finally {
      free(dbOp);
    }
    

    
    // Se non ci sono domini posso uscire.
    int size = tempArray.size();
    if (size == 0) 
      return;
   
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);
      dominioDelModello = getDominio(pArea, sDominio, cm, pAbPar);

      try {
        dominioDelModello.setDominioDelModello(true);
        domini.add(dominioDelModello);
      } catch (Exception egd) {
        loggerError("ListaDominiDMS::caricaDominiiDelModello() - Errore scrittura su RepositoryTemp: "+egd.toString(),egd);
      }
    }    
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
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
}
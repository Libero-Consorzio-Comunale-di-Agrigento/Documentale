package it.finmatica.modulistica.modulisticapack;

import it.finmatica.jfc.dbUtil.DbOperationSQL;
import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
//import java.io.Serializable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;

import org.apache.log4j.Logger;
 
public class ListaProtetti {
  protected ArrayList<Dominio> domini;
  private static Logger logger = Logger.getLogger(ListaProtetti.class);
  private  boolean debuglog = logger.isDebugEnabled();



  public ListaProtetti() {
    domini = new ArrayList<Dominio>();
  }

  public void caricaDominii(String pArea, String cm, HttpServletRequest pRequest) {
    caricaDominii( pArea,  cm,  pRequest,null);
  }

  /**
   * 
   */
  public void caricaDominii(String pArea, String cm, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ListaProtetti::caricaDominii - Inizio",pArea,cm,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      query;  
    int         i = 0;
    ArrayList<String>   tempArray = new ArrayList<String>();
    Dominio     dominio = null;

    // Seleziono tutti i nomi dei domini d'area nella giusta sequenza.
    query = "SELECT   DOMINIO "+
            "FROM     DOMINI "+
            "WHERE    AREA = :AREA AND "+
                     "SEQ_DOMINIO_AREA > 0 AND "+
                     "PRECARICA = 'D' AND "+
                     "CODICE_MODELLO = :CM AND "+
                     "PERSONALIZZAZIONE = 'N' "+
            "ORDER BY SEQ_DOMINIO_AREA";
            
    
    try {
      if (dbOpEsterna!=null) {
        dbOp = dbOpEsterna;
      }
      else {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      
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
      loggerError("ListaProtetti::caricaDominii() - Attenzione! Si è verificato un errore: "+e.toString(),e);
      //Debug Tempo
      stampaTempo("ListaProtetti::caricaDominii - Fine",pArea,cm,ptime);
      //Debug Tempo
      return;
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }
    
    // Se non ci sono domini posso uscire.
    int size = tempArray.size();
    if (size == 0) {
      //Debug Tempo
      stampaTempo("ListaProtetti::caricaDominii - Fine",pArea,cm,ptime);
      //Debug Tempo
      return;
    }
   
    // Ottenuti i nomi dei domini, cerco i domini fra quelli caricati ed eventualmente 
    // li carico in memoria in maniera "classica"
      
    //result = new Dominio[size];
    String sDominio = null;
   
    for (i=0; i<size; i++) {
      sDominio = (String)tempArray.get(i);
      dominio = getDominio(pArea, sDominio, cm, pRequest,dbOpEsterna);
      domini.add(dominio);
    }    
    //Debug Tempo
    stampaTempo("ListaProtetti::caricaDominii - Fine",pArea,cm,ptime);
    //Debug Tempo
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
  public Dominio getDominio(String pArea, String pDominio, String pCm, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
      int     i = 0;
      boolean trovato = false;
      Dominio d = null;
      
      try {
        // Se il size è 0 non ci sono elementi quindi trovato resterà 'false'
        if (domini.size() > 0) {
          while ((!trovato) && (i < domini.size())) {
            d = (Dominio)domini.get(i);
            if ((pArea.equals(d.getArea())) && (pDominio.equals(d.getDominio())) && (pCm.equals(d.getCodiceModello()))) {
              trovato = true;
            } else {
              i = i + 1;
            }
          }
        }
      } catch(Exception e) {
        loggerError("ListaProtetti::getDominio() (1) - Area: "+pArea+" - Dominio: "+pDominio+" - Attenzione! Si è verificato un errore: "+e.toString(),e);
        trovato = false;
      }
      
      if (!trovato) {
        d = caricaDominio(pArea, pDominio, pCm, pRequest, dbOpEsterna);
      } else {
        d.caricaValori(pRequest, dbOpEsterna);
      }
      return d;
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

  /**
   * 
   */
  public Dominio caricaDominio(String pArea, String pDominio, String pCm, HttpServletRequest request, IDbOperationSQL dbOpEsterna) {
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
                passwd;
    Dominio     d = null;

    query = "SELECT * FROM DOMINI " +
            "WHERE AREA = :AREA AND "+
            "      DOMINIO = :DOMINIO"+
            "  AND CODICE_MODELLO = :CM"+
            "  AND PRECARICA = 'D' AND "+
            "PERSONALIZZAZIONE = 'N' ";
            
    try {
      if (dbOpEsterna!=null)
        dbOp=dbOpEsterna;
      else
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", pCm);
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
            logger.error("ListaProtetti::caricaDominio() - Attenzione! Si è verificato un errore. L'istruzione per il caricamento del dominio supera i "+Dominio.MAXLEN_ISTRUZIONE+" caratteri.");
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
//        else {
//          connessione = completaConnessione(resultQuery.getString("CONNESSIONE"));
//        }
        user = resultQuery.getString("UTENTE");
        passwd = resultQuery.getString("PASSWD");
        

        String sDsn = resultQuery.getString("DSN");
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
          d = new DominioStandard(pArea, pCm, pDominio, tipo, ordinamento, request,dbOpEsterna);
        else if (tipo.equals("J")) 
          d = new DominioJava(pArea, pDominio, pCm, tipo, ordinamento, istruzione, request,dbOpEsterna);
        else if (tipo.equals("N")) 
          d = new DominioNotes(pArea, pDominio, pCm, tipo, ordinamento, connessione, request,dbOpEsterna);
        else if (tipo.equals("O"))
          d = new DominioCONN(pArea, pDominio, pCm, tipo, ordinamento, driver, connessione, user, passwd, istruzione, request,dbOpEsterna);
        else if (tipo.equals("F"))
          d = new DominioFunction(pArea, pDominio, pCm, tipo, ordinamento, driver, connessione, user, passwd, istruzione, request,dbOpEsterna);
        else
          d = null;  // TIPO DOMINIO NON RICONOSCIUTO
      } else {
        // Il dominio cercato NON è presente nel database
        d = null;
      }
      
    } catch(Exception e) {
      loggerError("ListaProtetti::caricaDominio() - Attenzione: il dominio "+pDominio+" presenta delle anomalie. "+e.toString(),e);
//      e.printStackTrace();
      d = null;
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }


    return d;
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
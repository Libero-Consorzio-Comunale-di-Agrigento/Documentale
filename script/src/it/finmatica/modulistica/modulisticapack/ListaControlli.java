/***********************************************************************
 * Module:  ListaControlli.java
 * Author:  Nicola
 * Created: Giovedi 11 Aprile
 ***********************************************************************/
package it.finmatica.modulistica.modulisticapack;

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
//import java.util.List;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.io.Serializable;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;

import org.apache.log4j.Logger;

public class ListaControlli implements Serializable {
 
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Invece che un array utilizzo una List che è più gestibile
   * 
   * @association <modulisticapack.ElencoControlli> modulisticapack.Controllo
   */
  protected ArrayList<Controllo> controlli;
  private static Logger logger = Logger.getLogger(ListaControlli.class);
  private  boolean debuglog = logger.isDebugEnabled();

  /**
   * 
   */
  public ListaControlli() throws Exception {
    controlli = new ArrayList<Controllo>();
  }

  public ListaControlli(HttpServletRequest pRequest, String pArea, String pDatoModello, String pTipo) throws Exception {
    this(pRequest,pArea,pDatoModello,pTipo,null);
  }

  /**
   * 
   */
  public ListaControlli(HttpServletRequest pRequest, String pArea, String pDatoModello, String pTipo, IDbOperationSQL dbOpEsterna) throws Exception {
    String query = "";

    controlli = new ArrayList<Controllo>();
    if (pTipo.equalsIgnoreCase("D")) {
      query = componiQueryCampo(pArea, pDatoModello);
    } else {
      query = componiQueryModello(pArea, pDatoModello);
    }
    caricaControlli(pRequest, pArea, query, pDatoModello, dbOpEsterna);
  }

  /**
   * 
   */
  public void caricaControlli(HttpServletRequest pRequest, String pArea, String pQuery, String pDatoModello, IDbOperationSQL dbOpEsterna) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("ListaControlli::caricaControlli - Inizio",pArea,pQuery,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
//    HttpSession httpSession;
    String      controllo, corpo, evento, query;
    int         sequenza;
    ResultSet   rs = null;
    Controllo   c;

    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

      
      dbOp.setStatement(pQuery);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":DATOMODELLO", pDatoModello);
      dbOp.execute();
      rs = dbOp.getRstSet();
      
      while (rs.next()) {
        controllo         = rs.getString("CONTROLLO");
        corpo             = rs.getString("CORPO");
        evento            = rs.getString("EVENTO");
        sequenza          = rs.getInt("SEQUENZA");
        c         = caricaControllo(pRequest, pArea, controllo, sequenza, corpo, evento);
        this.aggiungiControllo(c);
      }
      Personalizzazioni pers = null;
      pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
      if (pers != null) {
        String persContr, areaPers;
        for(int i = 0; i<controlli.size(); i++) {
          c = getControllo(i);
          if (pers.existPersonalizzazione(Personalizzazioni.LIBRERIA_CONTROLLI,c.getArea()+"#"+c.getControllo())) {
            controlli.remove(i);
            persContr = pers.getPersonalizzazione(Personalizzazioni.LIBRERIA_CONTROLLI,c.getArea()+"#"+c.getControllo());
            int j = persContr.indexOf("#");
            areaPers  = persContr.substring(0,j);
            controllo = persContr.substring(j+1);
            query = "select corpo from libreria_controlli where area = :AREA and controllo = :CONTROLLO";
            dbOp.setStatement(query);
            dbOp.setParameter(":AREA", areaPers);
            dbOp.setParameter(":CONTROLLO", controllo);
            dbOp.execute();
            rs = dbOp.getRstSet();
            if (rs.next()) {
              c = caricaControllo(pRequest, areaPers, controllo, c.getSequenza(), rs.getString(1), c.getEvento());
            }
            controlli.add(i, c);
          }
        }
      }
    } catch (Exception sqle) {
      loggerError("ListaControlli::caricaControlli - "+sqle.toString(),sqle);
    }
    finally {
      if (dbOpEsterna == null)  free(dbOp);
    }


    //Debug Tempo
    stampaTempo("ListaControlli::caricaControlli - Fine",pArea,pQuery,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  public void aggiungiControllo(Controllo c) throws Exception {
    controlli.add(c);
  }

  /**
   * 
   */
  public Controllo caricaControllo(HttpServletRequest pRequest, String pArea, String pControllo, int pSequenza, String pCorpo, String pEvento) throws Exception {
    HttpSession     httpSession;
    //ss SessioneDb      sessioneDb;
    //ss Connection      connessioneDb;    
    ListaControlli  controlliSessione;
    Controllo       c;

    if (pRequest!=null){
      httpSession = pRequest.getSession();
      controlliSessione = (ListaControlli)httpSession.getAttribute("listaControlli");
      if (controlliSessione == null) {
        c = null;
      } else {
        c = controlliSessione.getControllo(pArea, pControllo, pEvento);
      }
    } else {
      c = null;
      controlliSessione = null;
    }
    
    if (c == null) {
      // Creo effettivamente un oggetto Controllo che poi devo aggiungere alla lista
      // dei controlli di sessione per evitare di farne dei duplicati
//      c = new Controllo(connessioneDb, pArea, pControllo, pSequenza);
      c = new Controllo(pArea, pControllo, pSequenza, pCorpo, pEvento);
 /*     if (pRequest != null){
        controlliSessione.aggiungiControllo(c);
      }*/
    }
    
    return c;
  }

  /**
   * 
   */
  private String componiQueryCampo(String pArea, String pDato) {
    String query = "SELECT C.CONTROLLO, C.SEQUENZA, C.EVENTO, L.CORPO, L.PERSONALIZZAZIONE ";
    query = query + " FROM CONTROLLI_DATI C, LIBRERIA_CONTROLLI L";
    query = query + " WHERE C.AREA = :AREA";
    query = query + " AND C.DATO = :DATOMODELLO";
    query = query + " AND C.AREA = L.AREA";
    query = query + " AND C.CONTROLLO = L.CONTROLLO";
    query = query + " AND L.TIPO = 'S'";
    query = query + " ORDER BY C.SEQUENZA";
    return query;
  }

  /**
   * 
   */
  private String componiQueryModello(String pArea, String pModello) {
    String query = "SELECT C.CONTROLLO, C.SEQUENZA, C.EVENTO, L.CORPO, L.PERSONALIZZAZIONE ";
    query = query + " FROM CONTROLLI_MODELLI C, LIBRERIA_CONTROLLI L";
    query = query + " WHERE C.AREA = :AREA";
    query = query + " AND C.CODICE_MODELLO = :DATOMODELLO";
    query = query + " AND C.AREA = L.AREA";
    query = query + " AND C.CONTROLLO = L.CONTROLLO";
    query = query + " AND L.TIPO = 'S'";
    query = query + " UNION";
    query = query + " SELECT C.CONTROLLO, C.SEQUENZA, '' EVENTO, L.CORPO, L.PERSONALIZZAZIONE ";
    query = query + " FROM CONTROLLI_DATI C, LIBRERIA_CONTROLLI L, DATI_MODELLO D";
    query = query + " WHERE C.AREA = :AREA";
    query = query + " AND C.DATO = D.DATO";
    query = query + " AND C.AREA = D.AREA";
    query = query + " AND D.CODICE_MODELLO = :DATOMODELLO";
    query = query + " AND D.IN_USO = 'Y'";
    query = query + " AND C.AREA = L.AREA";
    query = query + " AND C.CONTROLLO = L.CONTROLLO";
    query = query + " AND L.TIPO = 'S'";
    query = query + " ORDER BY 2 ASC";
    return query;
  }


  /**
   * 
   */
  public int getNumeroControlli() {
    return controlli.size();
  }

  /**
   * 
   */
  public Controllo getControllo(int pos) {
    if ((pos >= 0) && (pos < controlli.size()))
      return (Controllo)controlli.get(pos);
    else
      return null;
  }

  /**
   * 
   */
  public Controllo getControllo(String pArea, String pControllo, String pEvento) {
      int i;
      boolean trovato;
      Controllo c = null;
      String sEvento = "";

      if (pEvento != null) {
        sEvento = pEvento;
      }
      trovato = false;
      i = 0;
      while ((trovato == false) && (i<controlli.size())) {
        c = (Controllo)controlli.get(i);
        if ((pArea.equals(c.getArea())) && (pControllo.equals(c.getControllo())) && (sEvento.equals(c.getEvento()))) {
          trovato = true;
        } else {
          i = i + 1;
        }
      }
      
      if (trovato)
        return c;
      else
        return null;
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
//      dbOp.getStmSql().clearParameters();
      dbOp.close();
    } catch (Exception e) {
      //e.printStackTrace();
    }
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

  private long stampaTempo(String sMsg, String area, String query, long ptime) {
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
      logger.debug("\n"+sMsg+"\nArea:"+area+"\nQUERY:\n"+query+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+"\nQUERY:\n"+query+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }
}

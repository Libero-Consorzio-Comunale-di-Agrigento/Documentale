package it.finmatica.modulistica.modulisticapack;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import java.io.Serializable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;


public class ListaEtichette implements Serializable {
  private static final long serialVersionUID = 1L;
  protected ArrayList<Etichetta> etichette;
  private static Logger logger = Logger.getLogger(ListaEtichette.class);
  private  boolean debuglog = logger.isDebugEnabled();

  public ListaEtichette(HttpServletRequest pRequest, String pArea, String pCodiceModello, IDbOperationSQL dbOpEsterna) throws Exception {
    etichette = new ArrayList<Etichetta>();
    caricaEtichette(pRequest, pArea, pCodiceModello, dbOpEsterna);
  }
  
  private void caricaEtichette(HttpServletRequest pRequest, String pArea, String pCm, IDbOperationSQL dbOpEsterna)  throws Exception {
    String query = "";
    //Debug Tempo
    long ptime = stampaTempo("ListaEtichette::caricaEtichette - Inizio",pArea,query,0);
    //Debug Tempo

    IDbOperationSQL   dbOp = null;
    String            area;
    String            cm;
    String            etichetta;
    String            valore;
    String            icona;
    String            nome;
    String            tooltip;
    String            separatore;
    String            controllo;
    String            controllo_js;
    long              data;
    ResultSet         rs = null;
    Etichetta         e;
    Date d;
    Time t;

    query = "SELECT 'A' ord, e.area, e.codice_modello, e.etichetta, e.valore, "+
                "e.icona, i.nome, i.tooltip, i.data_aggiornamento, e.separatore, "+
                "e.controllo, e.controllo_js "+
            " FROM etichette e, icone i "+
            "WHERE e.area = :AREA  "+
            "  AND e.codice_modello = :CM  "+
            "  AND e.tipo_uso != 'C'  "+
            "  AND e.personalizzazione = 'N'  "+
            "  and e.icona = i.icona(+) "+
            "UNION "+
            "SELECT 'B' ord, e.area, e.codice_modello, e.etichetta, e.valore, "+
                "e.icona, i.nome, i.tooltip, i.data_aggiornamento, e.separatore, "+
                "e.controllo, e.controllo_js "+
            " FROM etichette e, icone i "+
            "WHERE e.area = :AREA  "+
            "  AND e.codice_modello = '-'  "+
            "  AND e.tipo_uso != 'C'  "+
            "  AND e.personalizzazione = 'N'  "+
            "  and e.icona = i.icona(+) "+
            "UNION "+
            "SELECT 'C' ord, e.area, e.codice_modello, e.etichetta, e.valore, "+
                "e.icona, i.nome, i.tooltip, i.data_aggiornamento, e.separatore, "+
                "e.controllo, e.controllo_js "+
            " FROM etichette e, icone i "+
            "WHERE e.area = '-'  "+
            "  AND e.codice_modello = '-'  "+
            "  AND e.tipo_uso != 'C'  "+
            "  AND e.personalizzazione = 'N'  "+
            "  and e.icona = i.icona(+) "+
            "ORDER BY 4, ord";
    
    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", pCm);
      dbOp.execute();
      rs = dbOp.getRstSet();
      
      while (rs.next()) {
        area          = rs.getString("AREA");
        cm            = rs.getString("CODICE_MODELLO");
        etichetta     = rs.getString("ETICHETTA");
        valore        = rs.getString("VALORE");
        icona         = rs.getString("ICONA");
        nome          = rs.getString("NOME");
        tooltip       = rs.getString("TOOLTIP");
        separatore    = rs.getString("SEPARATORE");
        controllo     = rs.getString("CONTROLLO");
        controllo_js  = rs.getString("CONTROLLO_JS");
        d             = rs.getDate("DATA_AGGIORNAMENTO");
        t             = rs.getTime("DATA_AGGIORNAMENTO");
        if (d != null) {
          data = d.getTime() + t.getTime();
        } else {
          data = 0;
        }
        e = getEtichetta(etichetta);
        if (e == null) {
          e = new Etichetta(area,cm,etichetta,valore,icona,nome,tooltip,data,separatore,controllo,controllo_js);
          etichette.add(e);
        }
      }
      Personalizzazioni pers = null;
      pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
      if (pers != null) {
        String persEtich, etichetta_old;
        for(int i = 0; i<etichette.size(); i++) {
          e = getEtichetta(i);
          if (pers.existPersonalizzazione(Personalizzazioni.ETICHETTE,e.getArea()+"#"+e.getEtichetta()+"#"+e.getCm())) {
            etichetta_old = e.getEtichetta();
            persEtich = pers.getPersonalizzazione(Personalizzazioni.ETICHETTE,e.getArea()+"#"+e.getEtichetta()+"#"+e.getCm());
            int j = persEtich.indexOf("#");
            int k = persEtich.lastIndexOf("#");
            area  = persEtich.substring(0,j);
            etichetta = persEtich.substring(j+1,k);
            cm = persEtich.substring(k+1);
            query = "SELECT 'A' ord, e.area, e.codice_modello, e.valore, "+
                        "e.icona, i.nome, i.tooltip, i.data_aggiornamento, e.separatore, "+
                        "e.controllo, e.controllo_js "+
                    " FROM etichette e, icone i "+
                    "WHERE e.area = :AREA  "+
                    "  AND e.codice_modello = :CM  "+
                    "  AND e.etichetta = :ETICHETTA  "+
                    "  AND e.tipo_uso != 'C'  "+
                    "  and e.icona = i.icona(+) ";
            dbOp.setStatement(query);
            dbOp.setParameter(":AREA", pArea);
            dbOp.setParameter(":CM", pCm);
            dbOp.setParameter(":ETICHETTA", etichetta);
            dbOp.execute();
            rs = dbOp.getRstSet();
            if (rs.next()) {
              area          = rs.getString("AREA");
              cm            = rs.getString("CODICE_MODELLO");
              valore        = rs.getString("VALORE");
              icona         = rs.getString("ICONA");
              nome          = rs.getString("NOME");
              tooltip       = rs.getString("TOOLTIP");
              separatore    = rs.getString("SEPARATORE");
              controllo     = rs.getString("CONTROLLO");
              controllo_js  = rs.getString("CONTROLLO_JS");
              d             = rs.getDate("DATA_AGGIORNAMENTO");
              t             = rs.getTime("DATA_AGGIORNAMENTO");
              if (d != null) {
                data = d.getTime() + t.getTime();
              } else {
                data = 0;
              }
              e = new Etichetta(area,cm,etichetta_old,valore,icona,nome,tooltip,data,separatore,controllo,controllo_js);
              etichette.remove(i);
              etichette.add(i, e);
            }
          }
        }
      }
    } catch (Exception sqle) {
      loggerError("ListaEtichette::caricaEtichette - "+sqle.toString(),sqle);
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }


    //Debug Tempo
    stampaTempo("ListaEtichette::caricaEtichette - Fine",pArea,query,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  public int getNumeroEtichette() {
    return etichette.size();
  }

  /**
   * 
   */
  public Etichetta getEtichetta(int pos) {
    if ((pos >= 0) && (pos < etichette.size()))
      return (Etichetta)etichette.get(pos);
    else
      return null;
  }
  
  /**
   * 
   */
  public Etichetta getEtichetta(String pEtichetta) {
      int i;
      boolean trovato;
      Etichetta e = null;

      trovato = false;
      i = 0;
      while ((trovato == false) && (i<etichette.size())) {
        e = (Etichetta)etichette.get(i);
        if (pEtichetta.equalsIgnoreCase(e.getEtichetta())) {
          trovato = true;
        } else {
          i = i + 1;
        }
      }
      
      if (trovato)
        return e;
      else
        return null;
  }

  /**
  *
  */
 private void free(IDbOperationSQL dbOp) {
   try {
     dbOp.close();
   } catch (Exception e) {
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

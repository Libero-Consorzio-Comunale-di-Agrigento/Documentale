package it.finmatica.modulistica.personalizzazioni;

import it.finmatica.dmServer.Environment;
import java.util.*;
import java.sql.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Personalizzazioni {
  public static String   BLOCCHI             = "BLOCCHI";
  public static String   DOMINI              = "DOMINI";
  public static String   ETICHETTE           = "ETICHETTE";
  public static String   BARRA_ETICHETTE     = "BARRA_ETICHETTE";
  public static String   LIBRERIA_CONTROLLI  = "LIBRERIA_CONTROLLI";
  public static String   MODELLI             = "MODELLI";
  private Document dDoc = null;
  private static    Logger      logger = Logger.getLogger(Personalizzazioni.class);
  private  boolean debuglog = logger.isDebugEnabled();

  /**
   * Costruttore
   */
  public Personalizzazioni(String pEnte, String pUtente) throws Exception {
    init(pEnte, pUtente, null,null);
  }

  public Personalizzazioni(String pEnte, String pUtente, Connection cn) throws Exception {
    init(pEnte, pUtente, cn,null);
  }

  /**
   * Costruttore
   */
  public Personalizzazioni(String pEnte, String pUtente, Environment vu) throws Exception {
    init(pEnte, pUtente, null,vu);
  }
  
  private void init(String pEnte, String pUtente, Connection cn, Environment vu) {
    //Debug Tempo
    long ptime = stampaTempo("Personalizzazioni - Inizio",pEnte,pUtente,0);
    //Debug Tempo
    IDbOperationSQL   dbOp  = null;
    ResultSet         rst   = null;
    String            query = null;
    String            tipoOgg = "";
//    String            oldTipoOgg = "";
    String            orig = "";
    String            pers = "";
    boolean closeDbOp = true;

    try {
      Element elem = null;
      Element eleF = null;
      Element root = DocumentHelper.createElement("RADICE");
      dDoc = DocumentHelper.createDocument();
      dDoc.setRootElement(root);
      query = "select tipo_oggetto, originale, personalizzato "+
              "from personalizzazioni "+
              "where attivo = 'S' "+
              " and riferimento is null "+
              "union all "+
              "select tipo_oggetto, originale, personalizzato "+
              "from personalizzazioni "+
              "where attivo = 'S' "+
              " and riferimento = :RIF1 "+
              "union all "+
              "select tipo_oggetto, originale, personalizzato "+
              "from personalizzazioni "+
              "where attivo = 'S' "+
              " and riferimento = :RIF2 ";
      if (vu!=null && vu.getDbOp() != null) {
        dbOp=vu.getDbOp();
        closeDbOp=false;
      }
      else {
        if (cn == null) {
          dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
        } else {
          dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
        }
      }

      dbOp.setStatement(query);
      dbOp.setParameter(":RIF1", pEnte);
      dbOp.setParameter(":RIF2", pEnte+"/"+pUtente);
      dbOp.execute();
      rst = dbOp.getRstSet();
      while (rst.next()) {
        tipoOgg = rst.getString("tipo_oggetto");
        elem = leggiElementoXML(root, tipoOgg);
        
        if (elem == null) {
          elem = DocumentHelper.createElement(tipoOgg);
          root.add(elem);
        }
        orig = rst.getString("originale");
        pers = rst.getString("personalizzato");
        eleF = leggiElementoXML(elem, tipoOgg);
        if (eleF == null) {
          eleF = DocumentHelper.createElement(orig);
        } else {
          root.remove(eleF);
        }
        eleF.setText(pers);
        elem.add(eleF);          
      }
    } catch (Exception e) {
      loggerError("Personalizzazioni - Ente: "+pEnte+" Utente: "+pUtente+" Attenzione: si è verificato un errore: "+e.toString(),e);
    }
    finally {
      if(closeDbOp) free(dbOp);
    }

    //Debug Tempo
    stampaTempo("Personalizzazioni - Inizio",pEnte,pUtente,ptime);
    //Debug Tempo
    
  }

  public String getPersonalizzazione(String tipoOggetto, String originale) {
    String retval = "";
    Element to = leggiElementoXML(dDoc.getRootElement(),tipoOggetto);
    if (to != null) {
      retval = leggiValoreXML(to,originale);
      if (retval == null) {
        retval = originale;
      }
    } else {
      retval = originale;
    }
    return  retval;
  }
  
  public boolean existPersonalizzazione(String tipoOggetto, String originale) {
    boolean trovato = false;
    String valore = null;
    Element to = leggiElementoXML(dDoc.getRootElement(),tipoOggetto);
    if (to != null) {
      valore = leggiValoreXML(to,originale);
      if (valore != null) {
        trovato = true;;
      }
    }
    return  trovato;
  }
  
  public boolean isPersonalizzazione(String tipoOggetto, String personalizzazione) {
    boolean trovato = false;
    Element elemento = null;
    String valore = null;
    
    Element e = leggiElementoXML(dDoc.getRootElement(),tipoOggetto);
    if (e == null) {
      return trovato;
    }
    for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && !trovato;) {
      elemento = (Element)iterator.next();
      valore = elemento.getText();
      if (personalizzazione.equalsIgnoreCase(valore)) {
        trovato = true;
      }
    }
    return trovato;
  }
  
  private static Element leggiElementoXML(Element e, String tagName) {
      Element elemento = null, eFound = null;
      for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
      {
          elemento = (Element)iterator.next();
          if(elemento != null && elemento.getName().equals(tagName)) {
             eFound = elemento;
          } else {
              eFound = leggiElementoXML(elemento, tagName);
              if ( eFound != null) {
                return eFound;
              }
          }
      }

      return eFound;
  }

  private static String leggiValoreXML(Element e, String tagName)
  {
      String valore = null;
      for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
      {
          Element elemento = (Element)iterator.next();
          if(elemento != null && elemento.getName().equals(tagName))
              valore = elemento.getText();
          else
              valore = leggiValoreXML(elemento, tagName);
      }

      return valore;
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

  protected long stampaTempo(String sMsg, String ente, String utente, long ptime) {
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
      logger.debug("\n"+sMsg+"\n-Ente:"+ente+" -Utente:"+utente+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Ente:"+ente+" -Utente:"+utente+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }

  /**
  *
  */
   protected void free(IDbOperationSQL dbOp) {
  
     try {
       dbOp.close();
     } catch (Exception e) {
     }
   }
   
   public String getDoc() {
     return dDoc.asXML();
   }
}

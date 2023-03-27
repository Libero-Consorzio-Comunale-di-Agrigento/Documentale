package it.finmatica.modulistica.modulisticapack;

//import java.util.*;
import java.sql.*;
import java.util.Calendar;
import java.io.*;
import javax.servlet.http.*;
//import it.finmatica.jfc.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.net.URLDecoder;
import org.apache.log4j.Logger;
 
/**
 * 
 */
public class Grafico implements IElementoModello {
  String            pathfile;
  String            nomefile;
  String            ed;
  String            pdo;
  HttpSession       session;
  private static Logger logger = Logger.getLogger(Grafico.class);
  private  boolean debuglog = logger.isDebugEnabled();
  
  /**
   * 
   */
  public Grafico(HttpServletRequest request, String pArea, String pCodiceModello, String pKey, IDbOperationSQL dbOpEsterna) throws Exception {
    IDbOperationSQL    dbOp = null;  
    File              fdir, ffile;
    InputStream       srcBlob;
//   	Statement         stmt;
    ResultSet         rst;
    String            querySelect;
    String            serverScheme,serverName,serverPort;
    HttpSession       session = request.getSession();
    String            cr = request.getParameter("cr");
    String						rw = request.getParameter("rw");
    Date d;
    Time t;
    long data;
    ed = (String)session.getAttribute("ed");
//    String            us = (String)session.getAttribute("Utente");
//    LetturaScritturaFileFS  fOutput;

    session = request.getSession();
    pdo = (String)session.getAttribute("pdo");
    if (cr == null) {
      cr = (String)session.getAttribute("key");
    }
    if (rw == null) {
    	rw = "R";
    }
    //Debug Tempo
    long ptime = stampaTempo("Grafico - Inizio",pArea,pCodiceModello,cr,0);
    //Debug Tempo

    if (Parametri.PROTOCOLLO.length() == 0) {
      serverScheme = request.getScheme();
    } else {
      serverScheme = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      serverName = request.getServerName();
    } else {
      serverName = Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      serverPort = ""+request.getServerPort();
    } else {
      serverPort = Parametri.SERVERPORT;
    }

    String myRetUrl   = "";
    if (rw.equalsIgnoreCase("V")) {
    	myRetUrl = "../temp/";
    } else {
    	myRetUrl = serverScheme+"://"+serverName+":"+serverPort+request.getContextPath()+"/temp/";
    }
    String myPathTemp = request.getSession().getServletContext().getRealPath("")+File.separator+"temp"+File.separator;

    if (ed == null) {
      pathfile = myRetUrl + pArea + File.separator + pCodiceModello;
    } else {
//      pathfile = myRetUrl + pArea + File.separator + pCodiceModello +  File.separator + us +  File.separator + cr;
      pathfile = myRetUrl + pArea + File.separator + pCodiceModello +  File.separator + cr;
    }
    nomefile = URLDecoder.decode(pKey.substring(pKey.lastIndexOf("/") + 1),"windows-1252");

    if (rw.equalsIgnoreCase("V")) {
    	return;
    }    
    // Verifico se occorre creare la directory dove devo salvare l'immagine
    //fdir = new File(Parametri.PATHTEMP + pArea + pCodiceModello);
    if (ed == null) {
      fdir = new File(myPathTemp + pArea + File.separator + pCodiceModello);
    } else {
//      fdir = new File(myPathTemp + pArea + File.separator + pCodiceModello +  File.separator + us +  File.separator + cr);
      fdir = new File(myPathTemp + pArea + File.separator + pCodiceModello +  File.separator + cr);
    }
    if (!fdir.isDirectory()) {
        fdir.mkdirs();   // NNNNN
    }

    // Lettura BLOB
    try {


      querySelect = "SELECT GRAFICO, DATA_PUBBLICAZIONE "+
                    "FROM GRAFICI_MODELLO G,"+
                    " MODELLI M "+
                    "WHERE NOMEFILE = :NOMEFILE AND " +
                         "G.AREA = :AREA AND " +
                         "G.CODICE_MODELLO = :CM AND "+
                         "G.AREA = M.AREA AND "+
                         "G.CODICE_MODELLO = M.CODICE_MODELLO";

      dbOp = dbOpEsterna;
      dbOp.setStatement(querySelect);
      dbOp.setParameter(":NOMEFILE", pKey);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", pCodiceModello);
      dbOp.execute();

      rst = dbOp.getRstSet();

      if (!rst.next()) {
        logger.error("Grafico::Grafico() - Immagine ["+nomefile +"] non trovata sul DB");
        //Debug Tempo
        stampaTempo("Grafico - Fine",pArea,pCodiceModello,cr,ptime);
        //Debug Tempo
        throw new Exception("Immagine ["+nomefile +"] non trovata sul DB");
      }

      d             = rst.getDate("DATA_PUBBLICAZIONE");
      t             = rst.getTime("DATA_PUBBLICAZIONE");
      if (d != null) {
        data = d.getTime() + t.getTime();
      } else {
        data = 0;
      }
      // -----------------------------------------------------------------------
      // Creazione del file contenente l'immagine memorizzata sul DB.
      // -----------------------------------------------------------------------
      //ffile = new File(Parametri.PATHTEMP + pArea + pCodiceModello + File.separator + nomefile);
      if (ed == null) {
        ffile = new File(myPathTemp + pArea + File.separator + pCodiceModello + File.separator + nomefile);
      } else {
  //      ffile = new File(myPathTemp + pArea + File.separator + pCodiceModello + File.separator + us +  File.separator + cr + File.separator + nomefile);
        ffile = new File(myPathTemp + pArea + File.separator + pCodiceModello + File.separator +  cr + File.separator + nomefile);
      }
      if (!ffile.exists() || ffile.lastModified() != data) {
        srcBlob = dbOp.readBlob("GRAFICO");

        if (srcBlob == null) {
          logger.error("Grafico::Grafico() - Attenzione! InputStream vuoto.");
          //Debug Tempo
          stampaTempo("Grafico - Fine",pArea,pCodiceModello,cr,ptime);
          //Debug Tempo
          throw new Exception("InputStream vuoto.");
        }

        BufferedInputStream bis = new BufferedInputStream(srcBlob);
        FileOutputStream fos = new FileOutputStream(ffile);
        byte buf[] = new byte[1];

        while( bis.read(buf) != -1)
          fos.write(buf);
        fos.flush();
        fos.close();
        bis.close();
        ffile.setLastModified(data);
      }


      //Debug Tempo
      stampaTempo("Grafico - Fine",pArea,pCodiceModello,cr,ptime);
    }
    catch (Exception e) {

      throw e;
    }
    //Debug Tempo
  }

  public String getValue() {
    return getValue(null);
  }

  /**
   * 
   */
  public String getValue(IDbOperationSQL dbOpEsterna) {
    String retval = pathfile + File.separator + nomefile+"\" alt=\""+nomefile;
    retval = retval.replaceAll("\\\\", "/");
    return retval; 
  }

  /**
   * 
   */
  public String getZValue() {
    return null; 
  }


  public String getPRNValue() {
    return getPRNValue(null);
  }

  /**
   * 
   */
  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    return pathfile + File.separator + nomefile+"\" alt=\""+nomefile; 
  }  
  
  /**
   * 
   */
  public String getPRNComValue() {
    return pathfile + File.separator + nomefile+"\" alt=\""+nomefile; 
  }

  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return pathfile + File.separator + nomefile+"\" alt=\""+nomefile;
  }
  
  /**
   * Quando chiamata questa funzione non fa nulla.
   */
  public void release() {
}

  /**
   * free()
   */
  private void free(IDbOperationSQL dbOp) {
    try {
//      dbOp.getStmSql().clearParameters();
      dbOp.close();
    } catch (Exception e) { }
  }
  
  public void settaListFields(String l_fields){
  }

  private long stampaTempo(String sMsg, String area, String cm, String cr, long ptime) {
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
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Codice Richiesta:"+cr+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Codice Richiesta:"+cr+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }

  protected void finalize() throws Throwable {
    try {
      File f = new File(pathfile + File.separator + nomefile);
      f.delete();
    } catch (Exception e) {
      logger.error("Grafico::finalize - Errore: "+e.toString());
    } finally {
        super.finalize();
    }
  }

  public void settaProtetto(boolean b_protetto) {
    // TODO Auto-generated method stub
    
  }
}

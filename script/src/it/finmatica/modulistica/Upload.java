package it.finmatica.modulistica;

import javax.servlet.http.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import it.finmatica.modulistica.allegati.CompetenzeAllegati;
import it.finmatica.modulistica.allegati.FormatiFile;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.http.utils.multipartrequest.ServletMultipartRequest;
import it.finmatica.jfc.http.utils.multipartrequest.MultipartRequest;
import HTML.Template;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.GD4_Oggetti_File_Check;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.FileSystemUtility;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.JNDIParameter;

import org.apache.log4j.Logger;

public class Upload 
{
//  private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

  private String inifile = null;
  private String path = null, pathUploadBase=null, pathCrCm=null;
  private String completeContextURL = null;
  private String filesep = File.separator;
  private String serverScheme;
  private String serverName;
  private String serverPort;
  private Environment vu;
  private Vector lAllegati;
  private String p_user;
  private String p_ruolo;
  private String corpoHtml = "";
  private String urlExt = "";
  private String id_tipodoc;
  private String cm_tipodoc;
  private String scanser = "";
  private String noBody = Global.NOBODY;
  private String errmsg = "";  
  private int num_max_allegati;
  private long	compCancella = 1;
//  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Upload.class);

  public Upload(String sPath) {
    init(sPath);
  }

  public void init(String sPath) {
     try {
      String separa = File.separator;
      inifile = sPath + separa + "config" + separa + "gd4dm.properties";
      File f = new File(inifile);
      if (!f.exists()) {
        inifile = sPath + separa + ".." + separa + "jgdm" + separa + "config" + separa + "gd4dm.properties";
      }

      // Lettura parametri da file ini
      Parametri.leggiParametriStandard(inifile);

      // Creazione alias
      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      logger.error("Upload::init() - Attenzione! si è verificato un errore: "+e.toString());
    }
  }

  public void genera(HttpServletRequest request, String pdo) {
    String        reqContentType = request.getContentType();
//    HttpSession   session = request.getSession();
    String        cr, cm, area, fstyle;
    Template tmpl = null;
    String ptemp = null;
    logger.error("Inizio upload");
    if (pdo.equalsIgnoreCase("CC")) {
      scanser = "ServletScan.do";
      ptemp = request.getSession().getServletContext().getRealPath("")+filesep+".."+filesep+Parametri.APPLICATIVO+filesep+"template";
    } else {
      scanser = "ServletScan";
      ptemp = request.getSession().getServletContext().getRealPath("")+filesep+"template";
    }
    String [] paths = {ptemp,"."}; 
    String fileName = "ServletUpload.tmpl";
    
    area    = request.getParameter("area");
    cr      = request.getParameter("cr");
    cm      = request.getParameter("cm");
    logger.error("area = "+area);
    logger.error("cr = "+cr);
    logger.error("cm = "+cm);
    fstyle	= request.getParameter("fstyle");
    if (fstyle == null) {
    	fstyle = "";
    }

    if (Parametri.ALLEGATI_SINGLE_SIGN_ON.equalsIgnoreCase("N")) {
      p_user  = request.getParameter("us");
      p_ruolo = request.getParameter("ruolo");
    } else {
    	String nominativo = request.getRemoteUser();
      if (nominativo == null) {
        p_user = (String)request.getSession().getAttribute("UtenteGDM");
      } else {
        p_user = cercaUtente(nominativo.toUpperCase());
      }
  
      if (p_user == null) {
        p_user = "";
      }
      if (p_user.length() == 0) {
       return;
      }
      p_user = p_user.toUpperCase();

      p_ruolo = caricaRuolo(p_user);
    }

    String uAgg = (String)request.getSession().getAttribute("AUA-"+area+"-"+cm+"-"+cr);
    if (uAgg == null || uAgg.length() == 0) {
      uAgg = request.getParameter("ua");
    } else {
      String uAgg2 = request.getParameter("ua");
      double ua1, ua2;
      ua1 = Double.parseDouble(uAgg);
      ua2 = Double.parseDouble(uAgg2);
      if (ua2 > ua1) {
        uAgg = uAgg2;
      }
    }

    if (uAgg == null) {
      uAgg = "";
    }
    request.getSession().setAttribute("AUA-"+area+"-"+cm+"-"+cr,uAgg);

    if (pdo.equalsIgnoreCase("CC")) {
      urlExt = "common/ServletVisualizza.do";
    } else {
      urlExt = "ServletVisualizza";
    }

    try {
      initVu(area, cm, p_user, p_ruolo);
      vu.connect();
      vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
      //la classe rifaccia connect e/o disconnect.... a quello ci penso io
    } catch (Exception e) {
      logger.error("ServletUpload::genera() Errore in connessione enviroment - Errore:  ["+ e.toString()+"]",e);
      return;
    }

    try {
      id_tipodoc = ricavaIdtipodoc(area,cm);
      String idDoc = ricercaIdDocumento(request);

      try {
          CompetenzeAllegati compAll = new CompetenzeAllegati(vu, id_tipodoc, idDoc );
          compCancella = compAll.getCancellazione();
      } catch (Exception e1) {
          compCancella = 0;
      }
      // -------------------------------------------------
      // Inizializzo il completeContextURL che � del tipo:
      // http://hostName:port/Sportello/
      // -------------------------------------------------
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

      if (completeContextURL == null) {
        completeContextURL =
          serverScheme+"://"+
          serverName+":"+
          serverPort+
          request.getContextPath()+"/";
      }


      String pathFileFS="";
      try {
          pathFileFS=getPathAreaFS( area);
      } catch (Exception e) {
          logger.error("Upload::genera() - Attenzione! Si � verificato un errore: "+e.toString());
      }

      boolean bJdocAttach=false;
       //... se trovo la  jdocattach vado su jdocattach/DOCUMENTALE/upload (ovvero PATH_FILE della tabella aree /upload)
      if (!(pathFileFS.equals(""))) {
          File filedir = new File(pathFileFS);
          if (filedir.exists()) {
              File filedirUpload = new File(pathFileFS+filesep+"upload");
              filedirUpload.mkdir();

              if (filedirUpload.exists()) {
                  path = pathFileFS+filesep+"upload";
                  pathUploadBase=path;
                  bJdocAttach=true;
              }
          }
      }

      //se non la trovo....di default vado sulla jgdm/upload...
      if (!bJdocAttach) {
          path = request.getSession().getServletContext().getRealPath("")+filesep+"upload";
          pathUploadBase=path;
          File filedir = new File(path);
          if (!filedir.exists()) {
            filedir.mkdir();
          }
      }

      String pathCr, pathCm;
      pathCr = path+filesep+cr;
      pathCm = path+filesep+cr+filesep+cm;
      path = path+filesep+cr+filesep+cm;
      pathCrCm=path;
  /*    filedir = new File(path);
      if (!filedir.exists()) {
        filedir.mkdirs();
      }*/

      Hashtable tmpl_args = new Hashtable();
      tmpl_args.put("filename",fileName);
      tmpl_args.put("path", paths);

      try {
        tmpl = new Template(tmpl_args);
      } catch (Exception e) {
        logger.error("Upload::genera() - Attenzione! Si � verificato un errore: "+e.toString());
      }

      if (fstyle.equalsIgnoreCase("S")) {
          tmpl.setParam("pulsanti", "0");
          tmpl.setParam("stile", "Flex");
      } else {
          tmpl.setParam("pulsanti", "1");
          tmpl.setParam("stile", "AFC");
      }
      if (Parametri.SCANNER.equalsIgnoreCase("SI")) {
        tmpl.setParam("scanner", "1");
      } else {
        tmpl.setParam("scanner", "0");
      }
      if (compCancella > 0) {
          tmpl.setParam("eliminaAll", "1");
      } else {
          tmpl.setParam("eliminaAll", "0");
      }
      String minMax = getMaxDim(area, cm);
      if (minMax == "") {
          tmpl.setParam("dimMax", "-1");
          tmpl.setParam("blocco", "N");
      } else {
          int k = minMax.indexOf("@");
          tmpl.setParam("dimMax", minMax.substring(0, k));
          tmpl.setParam("blocco", minMax.substring(k+1));
      }

      if (pdo.equalsIgnoreCase("CC")) {
        tmpl.setParam("header", "0");
      } else {
        tmpl.setParam("header", "1");
        // ----------------------------------------------------------------------------------
        // Trattamento della request nel caso di submit multipart/form-data
        // ----------------------------------------------------------------------------------
          tmpl.setParam("conferma_all", "1");
        if ((reqContentType != null) && (reqContentType.indexOf("multipart/form-data") >= 0)) {
          this.nuovoSalvaAllegati(request, cr, cm, area, tmpl);
        } else {
          nuovoHtml_form(request, tmpl, cr, cm, area);
        }
      }

       //27/09/2016 MANNY: i file rimangono sulla upload del jgdm. Sar� il dmServer (o l'agente automatico)
       //					a cancellarli quando li tratta
       /*try {
        File f = new File(pathCm);
        f.delete();
        f = new File(pathCr);
        f.delete();
      } catch (Exception e) {
          logger.error("Upload::genera() - Attenzione! Si � verificato un errore: "+e.toString());
      }*/

      uAgg = (String)request.getSession().getAttribute("AUA-"+area+"-"+cm+"-"+cr);
      if (uAgg == null) {
        uAgg = "";
      }
      tmpl.setParam("sUagg", uAgg);
      corpoHtml = tmpl.output();
    }
    finally {
      //Chiusura della connessione
      try {
        vu.setDbOpRestaConnessa(false);
        vu.disconnectCommit();
      }
      catch (Exception e) {
      }
    }
  }

  public void nuovoSalvaAllegati(HttpServletRequest req,
                            String codiceRichiesta,
                            String codModello,
                            String area,
                            Template tmpl) {

    MultipartRequest multipartRequest = null;
    int i = 0;
    int fileLength  = 0;
    String nomeFile = null;
    String messaggio = null;

    try {
      File filedir = new File(path);
      if (!filedir.exists()) {
        filedir.mkdirs();
      }
      int max_read_bytes = (2 * (1024 * 1024 * 1024)) - 1;
      multipartRequest = new ServletMultipartRequest(req, path, max_read_bytes);
//      if (multipartRequest.getURLParameter("allegato") != null && multipartRequest.getURLParameter("conferma") == null && multipartRequest.getURLParameter("rifiuta") == null) {
//      	tmpl.setParam("conferma_all", "0");
      if (multipartRequest.getURLParameter("allegato") != null) {
        File file = new File(path);
        fileLength = file.listFiles().length;
        nomeFile = file.list()[0];
        GD4_Oggetti_File_Check ofc = new GD4_Oggetti_File_Check(area, codModello, codiceRichiesta, nomeFile, p_user, vu);
        messaggio = ofc.checkUserToObjFile();
        if (messaggio != null) {
        	tmpl.setParam("conferma_all", "0");
          tmpl.setParam("nomeAllegato", nomeFile);
          tmpl.setParam("messaggio", messaggio);
          nuovoHtml_form(req, tmpl, codiceRichiesta, codModello, area);
      		return;
        }
      } else {
      	tmpl.setParam("conferma_all", "1");
      	if (multipartRequest.getURLParameter("rifiuta") != null) {
          File file = new File(path);
          fileLength = file.listFiles().length;
          while (fileLength > 0 ) {
          	file.listFiles()[i].delete();
          	fileLength--;
          }
          nuovoHtml_form(req, tmpl, codiceRichiesta, codModello, area);
      		return;
      	}
      }

      if (multipartRequest.getURLParameter("delete") != null) {
        annullaAllegatoTemp(area,codModello,codiceRichiesta);
      } else {
        cancellaAllegati(multipartRequest, req);
      }
      
      if (multipartRequest.getURLParameter("allegato") != null || multipartRequest.getURLParameter("conferma") != null) {
        i = 0;
        fileLength  = 0;
        File file = new File(path);
        fileLength = file.listFiles().length;
        while (i < fileLength ) {
          nomeFile = file.list()[i];
          if (inserisciAllegati(req, nomeFile, area, codiceRichiesta,codModello) == -1 ) {
          	tmpl.setParam("messaggio2", errmsg);
          }
          i++;
        }
      }

      
      if (multipartRequest.getURLParameter("chiudi") != null) {
        aggiornaAllegatiTemp(area,codModello,codiceRichiesta);
        tmpl.setParam("chiudi",1);
      } else {
        if (multipartRequest.getURLParameter("delete") != null) {
          tmpl.setParam("chiudi",1);
        } else {
          tmpl.setParam("chiudi",0);
        }
      }
      
    } catch (Exception ex) {
      logger.error("ServletUpload::salvaAllegati() - Attenzione! Si � verificato un errore: "+ex.toString(),ex);
    }
    nuovoHtml_form(req, tmpl, codiceRichiesta, codModello, area);
  }

  private String getPathAreaFS(String area) throws Exception {
	  	  String ret="",query;
	  	  IDbOperationSQL dbOp = null;
	      ResultSet      rst = null;
	      
	  	  try {
	  		  query="Select decode(FORCE_FILE_ON_BLOB,1,'',decode(aree_path.ID_PATH_AREE_FILE,null,  nvl(aree.path_file,''),  nvl(aree_path.path_file,''))) from aree, aree_path where aree.area= :P_AREA and aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+) ";
	  		  dbOp = vu.getDbOp();
	          dbOp.setStatement(query);
	          dbOp.setParameter(":P_AREA", area);
	          dbOp.execute();
	          
	          rst = dbOp.getRstSet();
	          
	          if (rst.next()) ret=Global.nvl(rst.getString(1),"");
	  		  return ret;
	  	  } catch (Exception e) {
	  		  throw new Exception("Upload::getPathAreaFS() - Attenzione! Si � verificato un errore in getPathAreaFS("+area+"): "+e.toString());
	  	  }
  }
  
   /**
   * 
   */
  private void nuovoHtml_form(
                    HttpServletRequest request,
                    Template tmpl,
                    String cr,
                    String cm_r,
                    String area) {


    IDbOperationSQL dbOp = null;
    ResultSet      rst = null;
    String         servletPath = request.getServletPath();
    String         contextPath = request.getContextPath();
    String         queryString = request.getQueryString();
//    int            i = 0;
//    int            fileLength = 0;
    int            j=0;
    int            num_all = 0;
    String         iddoc;  
    String         query;
    String 				 stileRigo = "AFCDataTD";

    Vector v1 = new Vector(); /*Marika*/
    iddoc = ricercaIdDocumento(request);
    if (iddoc != null) {
      query = "SELECT NVL(FOFI.ICONA, 'generico.gif'), OGFI.ID_OGGETTO_FILE, OGFI.FILENAME, NVL(FOFI.VISIBILE,'S'), DA_CANCELLARE " + 
              "FROM OGGETTI_FILE OGFI, FORMATI_FILE FOFI " + 
              "WHERE OGFI.ID_FORMATO = FOFI.ID_FORMATO "+
              "AND OGFI.ID_DOCUMENTO = :ID_DOC "+
              "ORDER BY 3 ASC";
      try {
        dbOp = vu.getDbOp();
        dbOp.setStatement(query);
//        String iddoc = ricercaIdDocumento(request,null);
        dbOp.setParameter(":ID_DOC",iddoc);
        dbOp.execute();
        rst = dbOp.getRstSet();
        while (rst.next()) {
          String icona    = rst.getString(1);
          String id_ogg = rst.getString(2);
          String cod = rst.getString(3);
          String visibile = rst.getString(4);
          String da_cancellare = rst.getString(5);
          j=j+1;
          if (visibile.equalsIgnoreCase("S")) {
            num_all++;
            Hashtable h = new Hashtable(); 
            h.put("stileRigo", stileRigo);
            if (stileRigo.equalsIgnoreCase("AFCDataTD")) {
            	stileRigo = "AFCAltDataTD";
            } else {
            	stileRigo = "AFCDataTD";
            }
            h.put("checkAllegato","checkAllegato"+ "_" + Integer.toString(j));
            h.put("sporUrl", completeContextURL+urlExt);
            h.put("toImgUrl",icona);
            h.put("codModello", cm_r);
            h.put("codAllegato", cod);
            h.put("varea", area);
            h.put("idOgg", id_ogg);
            h.put("idDoc", iddoc);
            h.put("codRichiesta", cr);
            h.put("hAllegato","hAllegato"+"_"+ Integer.toString(j));
            h.put("cliccato","cliccato"+"_"+ Integer.toString(j));
            if (da_cancellare.equalsIgnoreCase("S")) {
              h.put("checkAllegato_value","1");
              h.put("trashImg","fulltrash.gif");
            } else {
              h.put("checkAllegato_value","");
              h.put("trashImg","trash.gif");
            }
            v1.addElement(h);     
          }
        }

      } catch (Exception e) {
        logger.error("Upload::html_form() - Attenzione! Si � verificato un errore: "+e.toString());
      }
    }

    query = "SELECT NOMEFILE FROM ALLEGATI_TEMP " +
      "WHERE AREA = :AREA AND "+
      "CODICE_RICHIESTA = :CR AND "+
      "CODICE_MODELLO = :CM AND "+
      "UTENTE_AGGIORNAMENTO = :UTENTE "+
      "ORDER BY NOMEFILE ASC";
    try {
      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CM",cm_tipodoc);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();
      rst = dbOp.getRstSet();
      while(rst.next()) {
        num_all++;
        String cod = rst.getString(1);
        j=j+1;
        Hashtable h = new Hashtable();    
        h.put("stileRigo", stileRigo);
        if (stileRigo.equalsIgnoreCase("AFCDataTD")) {
        	stileRigo = "AFCAltDataTD";
        } else {
        	stileRigo = "AFCDataTD";
        }
        h.put("checkAllegato","checkAllegato"+ "_" + Integer.toString(j));
        h.put("sporUrl", completeContextURL+urlExt);
        h.put("toImgUrl","nuovo.gif");
        h.put("codModello", cm_r);
        h.put("codAllegato", cod);
        h.put("varea", area);
        h.put("idOgg", cod);
        h.put("idDoc", "");
        h.put("codRichiesta", cr);
        h.put("hAllegato","hAllegato"+"_"+ Integer.toString(j));
        h.put("cliccato","cliccato"+"_"+ Integer.toString(j));
        h.put("checkAllegato_value","");
        h.put("trashImg","trash.gif");
        v1.addElement(h);     
      }

    } catch (Exception e) {
      logger.error("Upload::html_form() - Attenzione! Si � verificato un errore: "+e.toString());

    }

    if (num_all >= num_max_allegati && num_max_allegati > -1) {
      tmpl.setParam("max_all", 0);
    } else {
      tmpl.setParam("max_all", 1);
    }
    
    tmpl.setParam("serscan", scanser);
    tmpl.setParam("codMod", cm_r);
    tmpl.setParam("codRich", cr);
    tmpl.setParam("area", area);
    tmpl.setParam("lista_allegati", v1);
    tmpl.setParam("nomeServer", serverName);
    tmpl.setParam("portaServer", serverPort);
    tmpl.setParam("pathSpor", contextPath);
    tmpl.setParam("pathServlet", servletPath);
    tmpl.setParam("stringaQuery", queryString);
  }

  /**
   * 
   */
  private void cancellaAllegati(MultipartRequest mreq, HttpServletRequest req) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
//    ArrayList       modelli = null;

    String area    = req.getParameter("area");
    String cr      = req.getParameter("cr");
    String cm      = req.getParameter("cm");

    try {
      int j=0;
      String all, query = "";
      String iddoc = ricercaIdDocumento(req);
      dbOp = vu.getDbOp();
     if (iddoc != null) {
    	 //Controllo se � da cancellare la cartella pathCrCm che magari non contiene + nulla perch� l'ho creata cmq e magari non mi serviva
    	 try {FileSystemUtility.deleteAllPathifisEmpty(pathCrCm, pathUploadBase);}catch (Exception ex) {}
    	 
//        String uAgg = (String) req.getSession().getAttribute("AUA-"+area+"-"+cm+"-"+cr);
        AccediDocumento ad = new AccediDocumento(iddoc,vu);
        ad.accediDocumentoAllegati();
        lAllegati = ad.listaIdOggettiFile();
        if (lAllegati != null) {
//          AggiornaDocumento ad2 = new AggiornaDocumento(iddoc,vu);
//          if (!uAgg.length() == 0) {
//            ad2.setUltAggiornamento(uAgg);
//          }
          while(j < lAllegati.size()) {
            String cod = (String)lAllegati.get(j);
            j=j+1;
            all=mreq.getURLParameter("checkAllegato_" + Integer.toString(j));
            if (all == null) {
              all = "";
            }
            if (all.length() == 0) {
              query = "update oggetti_file set da_cancellare = 'N' where id_oggetto_file = :ID_OGFI";
              all = null;
            }
            if(all != null){
              query = "update oggetti_file set da_cancellare = 'S' where id_oggetto_file = :ID_OGFI";
//              ad2.cancellaAllegato(cod);
            }
            dbOp.setStatement(query);
            dbOp.setParameter(":ID_OGFI", cod);
            dbOp.execute();
          }
//          ad2.salvaDocumentoBozza();
//          uAgg = ad2.getUltAggiornamento();
//          req.getSession().setAttribute("AUA-"+area+"-"+cm+"-"+cr,uAgg);
        }
        dbOp.commit();
     		if (Parametri.ALLEGATI_AUTO_SAVE.equalsIgnoreCase("S")) {
	      	String uAgg = (String) req.getSession().getAttribute("AUA-"+area+"-"+cm+"-"+cr);
	        AggiornaDocumento ag = new AggiornaDocumento(iddoc, vu);
	        ag.settaCodiceRichiesta(cr);
	        ag.setUltAggiornamento(uAgg); 
	        ag.salvaAllegatiTemp(true);
	        ag.salvaDocumentoBozza();
	        uAgg = ag.getUltAggiornamento();
	        
	       
	        
	        req.getSession().setAttribute("AUA-"+area+"-"+cm+"-"+cr,uAgg);
	      }
      }


      query = "SELECT NOMEFILE FROM ALLEGATI_TEMP " +
        "WHERE AREA = :AREA AND "+
        "CODICE_RICHIESTA = :CR AND "+
        "CODICE_MODELLO = :CM AND "+
        "UTENTE_AGGIORNAMENTO = :UTENTE "+
        "ORDER BY NOMEFILE ASC";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CM",cm_tipodoc);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();
      rst = dbOp.getRstSet();
      List<String> listaNomiFile = new ArrayList<String>();
      while(rst.next()) {
        listaNomiFile.add(rst.getString(1));
      }

      for(int indexNomi=0;indexNomi<listaNomiFile.size();indexNomi++) {
        String cod = listaNomiFile.get(indexNomi);
        j=j+1;
        all=mreq.getURLParameter("checkAllegato_" + Integer.toString(j));
        if (all == null) {
          all = "";
        }
        if (all.length() == 0) {
          all = null;
        }
        if(all != null){
          cancellaAllegatoTemp(area, cm, cr, cod);
        }
      }

    } catch (Exception ex) {
      logger.error("Upload::cancellaAllegati() - Attenzione! Si è verificato un errore: "+ex.toString());
    }
  }
  
  private void cancellaAllegatoTempPercorsi(String area, String cm, String cr, String nf, String user, String stato, IDbOperationSQL dbOp, boolean bCommit) throws Exception {	  
	  try {
		Environment ev=  new Environment();
		ev.setDbOp(dbOp);
		DocUtil du = new DocUtil(ev);
		du.cancellaAllegatoTempPercorsi(area, cm, cr, nf, user,stato,false, bCommit,false);
		  
	  } catch (Exception ex) {		  
		  throw new Exception("Upload::cancellaAllegatoTempPercorsi - Errore: "+ex.getMessage());		  
      }
  }

  /**
   * 
   */
  private void cancellaAllegatoTemp(String area, String cm, String cr, String nf) {
    IDbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
    String          query;
    try {
    	
     
          	
      query = "DELETE ALLEGATI_TEMP "+
        "WHERE AREA = :AREA AND "+
        "CODICE_RICHIESTA = :CR AND "+
        "CODICE_MODELLO = :CM AND "+
        "UTENTE_AGGIORNAMENTO = :UTENTE AND "+
        "NOMEFILE = :NF";

      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CM",cm_tipodoc);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.setParameter(":NF",nf);
      dbOp.execute();
      
      
      
      
      dbOp.commit();
      
      
      //MANNY. Aggiunga la cancellazione della rsi e del file su file system
      //	   la cancellazione avviene in maniera del tutto slegata dall'allegati_temp perch�
      //	   deve essere atomica con il FS. Se per qualche ragione non va a buon fine non trappo nulla
      //	   resteranno dei file sporchi che poi canceller� la servlet di pulizia la notte
      try {
    	  cancellaAllegatoTempPercorsi( area, cm_tipodoc,  cr,  nf, null,null, dbOp,true);  
      } catch (Exception e) {
    	  logger.error("ServletUpload::cancellaAllegatoTempPercorsi() -  [WARN] Si � verificato un errore di cancellazione dei file temporanei. Verificare la pulizia della cartella: "+e.toString());
      }
      

    } catch (Exception ex) {
      try {dbOp.rollback();} catch (Exception ei) {}
      logger.error("ServletUpload::cancellaAllegatoTemp() - Attenzione! Si � verificato un errore: "+ex.toString());
    }
  }  

  /**
   * 
   */
  private void annullaAllegatoTemp(String area, String cm, String cr) {
    IDbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
    String          query;
    try {
      query = "DELETE ALLEGATI_TEMP "+
        "WHERE AREA = :AREA AND "+
        "CODICE_RICHIESTA = :CR AND "+
        "CODICE_MODELLO = :CM AND "+
        "UTENTE_AGGIORNAMENTO = :UTENTE AND "+
        "STATO = 'U'";

      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CM",cm_tipodoc);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();
      dbOp.commit();
      
      //MANNY. Aggiunga la cancellazione della allegati_temp_percorsi e del file su file system
      //	   la cancellazione avviene in maniera del tutto slegata dall'allegati_temp perch�
      //	   deve essere atomica con il FS. Se per qualche ragione non va a buon fine non trappo nulla
      //	   resteranno dei file sporchi che poi canceller� la servlet di pulizia la notte
      try {
    	  cancellaAllegatoTempPercorsi( area, cm_tipodoc,  cr,  null, p_user,"U", dbOp,true);  
      } catch (Exception e) {
    	  logger.error("ServletUpload::cancellaAllegatoTempPercorsi() -  [WARN] Si � verificato un errore di cancellazione dei file temporanei. Verificare la pulizia della cartella: "+e.toString());
      }      

    } catch (Exception ex) {
       try {dbOp.rollback();} catch (Exception ei) {}
       logger.error("ServletUpload::annullaAllegatoTemp() - Attenzione! Si � verificato un errore: "+ex.toString());
    }
  }  

  /**
   * 
   */
  private void aggiornaAllegatiTemp(String area, String cm, String cr) {
    IDbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
    String          query;
    try {
      query = "UPDATE ALLEGATI_TEMP SET STATO = 'I' "+
        "WHERE AREA = :AREA AND "+
        "CODICE_RICHIESTA = :CR AND "+
        "CODICE_MODELLO = :CM AND "+
        "UTENTE_AGGIORNAMENTO = :UTENTE AND "+
        "STATO = 'U'";

      dbOp = vu.getDbOp();
      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CM",cm_tipodoc);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();
      
      query = "UPDATE ALLEGATI_TEMP_PERCORSI SET STATO = 'I' "+
      "WHERE AREA = :AREA AND "+
      "CODICE_RICHIESTA = :CR AND "+
      "CODICE_MODELLO = :CM AND "+
      "UTENTE_AGGIORNAMENTO = :UTENTE AND "+
      "STATO = 'U'";
	      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CM",cm_tipodoc);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();      
      
      dbOp.commit();

    } catch (Exception ex) {
      try {dbOp.rollback();} catch (Exception ei) {}
      logger.error("ServletUpload::aggiornaAllegatiTemp() - Attenzione! Si � verificato un errore: "+ex.toString());
    }
  }
  /**
   * 
   */
  private String ricercaIdDocumento (HttpServletRequest  request) {
    Vector          ll;
    String          iddoc = null;
    String          //ar,
                    cr/*,
                    cm*/;

//    ar   = request.getParameter("area");
    cr   = request.getParameter("cr");
//    cm   = request.getParameter("cm");

    try {
      RicercaDocumento rd = new RicercaDocumento(id_tipodoc,vu);
      rd.settaFileName(noBody);
      rd.settaCodiceRichiesta(cr);
      ll = rd.ricercaBozza();
      if(ll.size() == 1 ) {
        iddoc = (String)ll.firstElement();
      } else {
        if (ll.size() > 1) {
          logger.error("Upload::ricercaIdDocumento() - Errore:  Nel numero documenti trovati!!");
        }
      }
    }
      catch (Exception e) {
      logger.error("Upload::ricercaIdDocumento() - Errore:  ["+ e.toString()+"]");
    }
    return iddoc;
  }

//  /**
//   * 
//   */
//  private void ricavaIdtipodoc (String ar, String cm) {
//    DbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
//    String          query;
//    String          idtipodoc = null;
//    String          codmod = null;
//
//    query = "SELECT CODICE_MODELLO_PADRE, ID_TIPODOC"+
//            " FROM MODELLI"+
//            " WHERE AREA = :AREA"+
//            "   AND CODICE_MODELLO = :CM";
//    try {
//      dbOp = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//
//      dbOp.setStatement(query);
//      dbOp.setParameter(":AREA",ar);
//      dbOp.setParameter(":CM",cm);
//      dbOp.execute();
//      rst = dbOp.getRstSet();
//
//      if (rst.next() ) {
//         idtipodoc = ""+rst.getString(2);
//         codmod = rst.getString(1);
//      }
//
//      if (idtipodoc == null) {
//        idtipodoc = "";
//      }
//
//      if (idtipodoc.length() == 0) {
//        dbOp.setParameter(":AREA",ar);
//        dbOp.setParameter(":CM",codmod);
//        dbOp.execute();
//        rst = dbOp.getRstSet();
//
//        if (rst.next() ) {
//           idtipodoc = rst.getString("ID_TIPODOC");
//        }
//      }
//      dbOp.close();
//    } catch (Exception e) {
//      free(dbOp);
//      Util.writeErr("Upload::ricavaIdtipodoc 2",e.toString());
//    }
//  }

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
  public String getValue() {
    return corpoHtml;
  }

  private void initVu(String p_area,String p_cm,String p_user, String p_ruolo) throws Exception {
    try {
      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo(p_ruolo);
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * 
   */
  private String ricavaIdtipodoc (String ar, String cm) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          idtipodoc = null;
    String          codmod = null;

    query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE, nvl(NUM_MAX_ALLEGATI,-1) NUM_MAX_ALLEGATI "+
            " FROM MODELLI"+
            " WHERE AREA = :AREA"+
            "   AND CODICE_MODELLO = :CM";
    try {
      cm_tipodoc = cm;
      dbOp = vu.getDbOp();

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         idtipodoc = rst.getString("ID_TIPODOC");
         codmod = rst.getString("CODICE_MODELLO_PADRE");
         num_max_allegati = rst.getInt("NUM_MAX_ALLEGATI");
      } else {
        return "";
      }

      if (idtipodoc == null) {
        idtipodoc = "";
      }

      if (idtipodoc.length() == 0) {
      	cm_tipodoc = codmod;
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CM",codmod);
        dbOp.execute();
        rst = dbOp.getRstSet();

        if (rst.next() ) {
           idtipodoc = rst.getString("ID_TIPODOC");
           num_max_allegati = rst.getInt("NUM_MAX_ALLEGATI");
        }
      }
      return idtipodoc;
    
    } catch (Exception e) {
      logger.error("ServletUpload::ricavaIdtipodoc - "+e.toString());
      return "";
    }
  }

  /**
   * 
   */
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      logger.error("ServletEditing::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString());
//      corpoHtml += "<H2>Attenzione! Errore in fase di rilascio connnessioni.</h2>";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpoHtml += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpoHtml += e.getStackTrace().toString();
//      }
//    }
  }

  /**
   * 
   */
  public int inserisciAllegati(HttpServletRequest request, String nomefile, String area, String cr, String cm) {
    IDbOperationSQL dbOp  = null;
    ResultSet      rst    = null;
    String         query  = null;
    String 		   queryPercorsi=null;
    String 				 iddoc 	= null;
    String				 forza 	= "N";

    try {
    	forza = request.getParameter("forza");
    	if (forza == null) {
    		forza = "N";
    	}
    	
    	
      File filedir = new File(path);
      
      File fparent = filedir.getParentFile();
      
      File fUp = new File(path+File.separator+nomefile);
      FileInputStream fIs = new FileInputStream(fUp);
      
      dbOp = vu.getDbOp();
      try {
      	
      	FormatiFile ff = new FormatiFile();
      	String formato = "";
      	formato = ff.formatoFileDaNome(nomefile);

  	    /*AutoDetectParser parser = new AutoDetectParser();
  	    parser.setParsers(new HashMap<MediaType, Parser>());

  	    Metadata metadata = new Metadata();
  	    metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, nomefile);

  	    InputStream stream = new FileInputStream(fUp);
  	    parser.parse(stream, new DefaultHandler(), metadata, new ParseContext());
  	    stream.close();

  	    String mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
      	
      	if (!ff.verificaFormato(mimeType, formato)) {*/
      	if (Parametri.VERIFICA_FILE.equalsIgnoreCase("S")) {
        	if (!ff.verificaFile(fUp)) {
            fIs.close();
            fUp.delete();
            filedir.delete();
            fparent.delete();
        		errmsg = "Attenzione! Tipo file non corrispondente alla sua estensione";
            return -1;
        	}
      	}
      	query = "SELECT COUNT (1), NVL (SUM (DECODE (upper(formato), :FORMATO, 1, 0)), 0) "+
      					"  FROM tipi_allegati_modello "+
      					" WHERE area = :AREA AND codice_modello = :CM";
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":CM",cm_tipodoc);
        dbOp.setParameter(":FORMATO",formato.toUpperCase());
        dbOp.execute();
        rst = dbOp.getRstSet();
        if (rst.next()) {
        	if (rst.getInt(1) > 0 && rst.getInt(2) == 0) {
            free(dbOp);
            fIs.close();
            fUp.delete();
            filedir.delete();
            fparent.delete();
            errmsg = "Attenzione! Formato non consentito";
            return -1;
        	}
        }
      	
        query = "SELECT NOMEFILE FROM ALLEGATI_TEMP "+
          "WHERE AREA = :AREA AND CODICE_MODELLO = :CM AND "+
          "CODICE_RICHIESTA = :CR AND NOMEFILE = :NF";
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":CM",cm_tipodoc);
        dbOp.setParameter(":CR",cr);
        dbOp.setParameter(":NF",nomefile);
        dbOp.execute();
        rst = dbOp.getRstSet();
        if (rst.next()) {
          query = "UPDATE ALLEGATI_TEMP SET "+
	          "DATA_AGGIORNAMENTO = SYSDATE, "+
	          "UTENTE_AGGIORNAMENTO = :UTENTE, "+
            "STATO = 'U',  FORZA = :FORZA "+
            "WHERE AREA = :AREA AND CODICE_MODELLO = :CM AND "+
            "CODICE_RICHIESTA = :CR AND NOMEFILE = :NF";         
        } else {        
          query = "INSERT INTO ALLEGATI_TEMP "+
            "(AREA, CODICE_MODELLO, CODICE_RICHIESTA, NOMEFILE, STATO, FORZA, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO) "+
            "VALUES (:AREA, :CM, :CR, :NF, 'U', :FORZA, SYSDATE, :UTENTE)";
        }
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":CM",cm_tipodoc);
        dbOp.setParameter(":CR",cr);
        dbOp.setParameter(":NF",nomefile);
        dbOp.setParameter(":UTENTE",p_user);
        dbOp.setParameter(":FORZA",forza);
        //dbOp.setParameter(":ALLEGATO", (InputStream)fIs, -1);
        dbOp.execute();
        
        
        //Insert into all_percorsi
        	query = "SELECT NOMEFILE FROM ALLEGATI_TEMP_PERCORSI "+
        	"WHERE AREA = :AREA AND CODICE_MODELLO = :CM AND "+
        	"CODICE_RICHIESTA = :CR AND NOMEFILE = :NF";
	      dbOp.setStatement(query);
	      dbOp.setParameter(":AREA",area);
	      dbOp.setParameter(":CM",cm_tipodoc);
	      dbOp.setParameter(":CR",cr);
	      dbOp.setParameter(":NF",nomefile);
	      dbOp.execute();
	      rst = dbOp.getRstSet();
	      if (rst.next()) {
	        query = "UPDATE ALLEGATI_TEMP_PERCORSI SET "+
		          "DATA_AGGIORNAMENTO = SYSDATE, "+
		          "UTENTE_AGGIORNAMENTO = :UTENTE, "+
	          "STATO = 'U',PERCORSO	= :PERCORSO, PERCORSO_ROOT = :PERCORSO_ROOT	 , PERCORSO_NOFILE	= :PERCORSO_NOFILE "+
	          "WHERE AREA = :AREA AND CODICE_MODELLO = :CM AND "+
	          "CODICE_RICHIESTA = :CR AND NOMEFILE = :NF";         
	      } else {        
	        query = "INSERT INTO ALLEGATI_TEMP_PERCORSI "+
	          "(AREA, CODICE_MODELLO, CODICE_RICHIESTA, NOMEFILE, STATO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO,PERCORSO,PERCORSO_ROOT,PERCORSO_NOFILE) "+
	          "VALUES (:AREA, :CM, :CR, :NF, 'U', SYSDATE, :UTENTE,:PERCORSO,:PERCORSO_ROOT,:PERCORSO_NOFILE)";
	      }
	      dbOp.setStatement(query);
	      dbOp.setParameter(":AREA",area);
	      dbOp.setParameter(":CM",cm_tipodoc);
	      dbOp.setParameter(":CR",cr);
	      dbOp.setParameter(":NF",nomefile);
	      dbOp.setParameter(":UTENTE",p_user);
	      dbOp.setParameter(":PERCORSO",path+File.separator+nomefile);
	      dbOp.setParameter(":PERCORSO_ROOT",pathUploadBase);
	      dbOp.setParameter(":PERCORSO_NOFILE",path);

	      dbOp.execute();
	        
        dbOp.commit();
      } catch (Exception e) {
    	try {dbOp.rollback();} catch (Exception ei) {}
        try {
        	fIs.close();
        	fUp.delete();
        	filedir.delete();
        	fparent.delete();
        } catch(Exception noex) {}
        logger.error("ServletUpload::inserisciAllegati() - Errore 1:  ["+ e.toString()+"]");
        errmsg = "Attenzione! Errore: "+ e.toString();
        return -1;
      }
      fIs.close();
      //fUp.delete();
      //filedir.delete();
      //fparent.delete();
      if (Parametri.ALLEGATI_AUTO_SAVE.equalsIgnoreCase("S")) {
	      iddoc = ricercaIdDocumento(request);
	      if (iddoc != null) {
	      	String uAgg = (String) request.getSession().getAttribute("AUA-"+area+"-"+cm+"-"+cr);
	        AggiornaDocumento ad = new AggiornaDocumento(iddoc, vu);
	        ad.settaCodiceRichiesta(cr);
	        ad.setUltAggiornamento(uAgg); 
	        ad.salvaAllegatiTemp(true);
	        ad.salvaDocumentoBozza();
	        uAgg = ad.getUltAggiornamento();
	        request.getSession().setAttribute("AUA-"+area+"-"+cm+"-"+cr,uAgg);
            dbOp.commit();
	      }
      }
    } catch (Exception e) {
      logger.error("ServletUpload::inserisciAllegati() - Errore 2:  ["+ e.toString()+"]");
      errmsg = "Attenzione! Errore: "+ e.toString();
      return -1;
    }
    return 0;
  }

//  /**
//   * cercaModello()
//   * Ricerca a livello di sessione la presenza del modello richiesto.
//   * Se � gi� presente evita di ricaricarlo da database.
//   **/
//  private Modello cercaModello(HttpSession pHttpSess, String pArea, String pCodiceModello, String pCodiceRichiesta) throws Exception {
//    Modello     md = null;
//    ArrayList   modelli;
////    String      idDoc= "";
//    String      sNomeServlet = "",sUltimoAgg = "";
//    boolean     trovato = false;
//    int         i = 0;
//
//    try {
//
//      modelli = (ArrayList) pHttpSess.getAttribute("modelli");
//      sNomeServlet = (String) pHttpSess.getAttribute("p_nomeservlet");
//      sUltimoAgg = (String) pHttpSess.getAttribute("UA-"+pArea+"-"+pCodiceModello+"-"+pCodiceRichiesta);
//      if (sUltimoAgg == null) {
//        sUltimoAgg = "";
//      }
//      if (modelli == null) {
//        logger.error("ServletModulistica::cercaModello() - Area: "+pArea+" - Modello: "+pCodiceModello+"- Richiesta: "+pCodiceRichiesta+" - Attenzione! Lista modelli non ancora inizializzata in memoria!");
//        corpoHtml += "Attenzione! Lista modelli non ancora inizializzata in memoria!";
//        return null;
//      }
//
//      while ( (!trovato) && (i < modelli.size()) ) {
//        md = (Modello)modelli.get(i);
//        if ((md.getArea().equals(pArea)) &&
//            (md.getCodiceModello().equals(pCodiceModello)) &&
//            (md.getCodiceRichiesta().equals(pCodiceRichiesta)) &&
//            (md.getNomeServlet().equals(sNomeServlet)) &&
//            (md.getUltimoAgg().equals(sUltimoAgg))) {
//          trovato = true;
//        } else {
//          i += 1;
//        }
//      }
//
//      if (!trovato) {
//        return null;
//      } else {
//        return md;
//      }
//
//    } catch(Exception e) {
//      logger.error("SerletModulistica::cercaModello() - Area: "+pArea+" - Modello: "+pCodiceModello+"- Richiesta: "+pCodiceRichiesta+" - Attenzione! Si � verificato un errore durante la ricerca del modello in memoria: ",e);
//      return null;
//    }
//  }

  /**
   * 
   */
  private String cercaUtente(String nominativo) {
    String          retval = null;
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    
    //RICAVO IL RUOLO DELL'UTENTE
    query = "select utente from ad4_utenti where nominativo = :NOMINATIVO";
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      dbOp.setStatement(query);
      dbOp.setParameter(":NOMINATIVO", nominativo);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         retval = rst.getString(1);
      }
      return retval;
    } catch (Exception e) {
      logger.error("ServletUpload::cercaUtente - "+e.toString(),e);
      return "";
    }
    finally {
      free(dbOp);
    }
  }

  /**
   * 
   */
  private String getMaxDim(String area, String cm) {
    String          retval = "";
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    
    //RICAVO IL RUOLO DELL'UTENTE
    query = "F_GET_MAXDIM_ATTACH('"+area+"','"+cm+"')";
    try {
      dbOp = vu.getDbOp();

      dbOp.setCallFunc(query);
      dbOp.execute();
      retval = dbOp.getCallSql().getString(1);
      if (retval == null) {
      	retval = "";
      }
      return retval;
    } catch (Exception e) {
      logger.error("ServletUpload::getMaxDim - "+e.toString(),e);
      return "";
    }
  }

  /**
   * 
   */
  private String caricaRuolo(String p_user) {
      return "GDM";
  }
}
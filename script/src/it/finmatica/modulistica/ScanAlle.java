package it.finmatica.modulistica;

import javax.servlet.http.*;
import java.io.*;
import java.sql.ResultSet;

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;

public class ScanAlle  {
  private String      inifile = null;
  private String      corpoHtml = "";
  private String      filesep = File.separator;
//  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(ScanAlle.class);

  public ScanAlle(String sPath) {
    init(sPath);
  }

  public void init(String sPath) {
     try {
      inifile = sPath + filesep + "config" + filesep + "gd4dm.properties";
      File f = new File(inifile);
      if (!f.exists()) {
        inifile = sPath + filesep + ".." + filesep + "jgdm" + filesep + "config" + filesep + "gd4dm.properties";
      }
      // Lettura parametri da file ini
      Parametri.leggiParametriStandard(inifile);

      // Creazione alias
//      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      logger.error("Upload::init() - Attenzione! si � verificato un errore: "+e.toString());
    }
  }

  /**
   * 
   */
  public void genera(HttpServletRequest request, String pdo) {
//    HttpSession     session = request.getSession();
//    String          nome      = request.getParameter("nome"),
//                    direc     = request.getParameter("direc");

    String          nome    = request.getParameter("nome"),
            		area    = request.getParameter("area"),
                    cr      = request.getParameter("cr"),
                    cm      = request.getParameter("cm"),
                    submit  = request.getParameter("submit");

    if (nome == null) {
      nome = "";
    }
    if (submit == null) {
      submit = "";
    }
    String path = "";
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
//   		    pathUploadBase=path;
   		    bJdocAttach=true;
   		}
   	}
   }
   
   //se non la trovo....di default vado sulla jgdm/upload...
   if (!bJdocAttach) {
	   path = request.getContextPath()+filesep+"upload"+filesep+cr+filesep+cm;
   }

//    String path = request.getContextPath()+filesep+"upload"+filesep+cr+filesep+cm;
    String realPath = request.getSession().getServletContext().getRealPath("")+filesep+"upload"+filesep+cr+filesep+cm;
    File filedir = new File(realPath);
    if (!filedir.exists()) {
      filedir.mkdirs();
    }
    
    String urlServer = "";
    if (Parametri.PROTOCOLLO.length() == 0) {
    	urlServer = request.getScheme();
    } else {
    	urlServer = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
    	urlServer += "://"+request.getServerName();
    } else {
    	urlServer += "://"+Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
    	urlServer += ":"+request.getServerPort();
    } else {
    	urlServer += ":"+Parametri.SERVERPORT;
    }

    corpoHtml = "<html>";
    corpoHtml += "<head><title>ServletScan</title>";
    corpoHtml += "<link rel='stylesheet' type='text/css' href='Themes/AFC/Style.css'>";
    corpoHtml += "</head><body>";
    if (submit.length() == 0) {
      if (pdo.equalsIgnoreCase("CC")) {
        corpoHtml += "<form action='"+urlServer+"/si4wscan/scanPage.jsp' method='post'>";
      } else {
        corpoHtml += "<form action='"+urlServer+"/si4wscan/scanPage.jsp' method='post'>";
      }
      corpoHtml += "<input type='hidden' name='cr' value = '"+cr+"'>"; 
      corpoHtml += "<input type='hidden' name='cm' value = '"+cm+"'>"; 
      corpoHtml += "<input type='hidden' name='uploadUrl' value = '"+urlServer+"/si4wscan/ScanServlet'>"; 
      corpoHtml += "<input type='hidden' name='currentDir' value = '"+path+"'>";
      corpoHtml += "<input type='hidden' name='callBack' value = 'scanOk'>";
      corpoHtml += "<input type='hidden' name='absolute' value = 'no'>";
      if (Parametri.SCANSIONE_PDF_TIFF.equalsIgnoreCase("PDF")) {
    	  corpoHtml += "<input type='hidden' name='pdf' value = 'yes'>";
      } else {
    	  corpoHtml += "<input type='hidden' name='pdf' value = 'no'>";
      }
      corpoHtml += "<table class='AFCFormTABLE' cellspacing='0' cellpadding='3' width='100%'>";
      corpoHtml += "<tr><td class='AFCFieldCaptionTD' >Nome allegato</td>";
      corpoHtml += "<td class='AFCDataTD' ><input  class='AFCInput' type='text' name='filename'> "; 
      corpoHtml += "<input class='AFCButton' type='submit' name='submit' value='Avvia'></td>"; 
      corpoHtml += "</tr><tr><td class='AFCFooterTD' align=right colspan='2' >";
      corpoHtml += "<input class='AFCButton' type='button' name='B1' value='Annulla' onclick='window.close();'></td>";
//      corpoHtml += "</tr><tr><td class='AFCDataTD' colspan='2'>Attenzione!\nSe � la prima volta che alleghi un documento mediante scanner,\n";
//      corpoHtml += "devi <a href='../si4wscan/install.html'>installare le librerie</a> necessarie</td>";
      corpoHtml += "</tr></table>";
      corpoHtml += "</form></body></html> ";
    } else {
      String serverScheme,serverName,serverPort;
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
      corpoHtml += "<applet code=\"it.finmatica.si4wscan.client.ScanApplet.class\" ";
      corpoHtml += "archive=\"./scan/finmatica-si4wscan.jar,./scan/JTwain.jar,./scan/jai_codec.jar,./scan/jai_core.jar\" ";
      corpoHtml += "width=\"600\" height=\"470\"> ";
      corpoHtml += "<param name=\"DOWNLOAD_URL\" value='"+serverScheme+"://"+serverName+":"+serverPort+"/si4wscan/install/AspriseJTwain.dll'> ";
      corpoHtml += "<param name=\"DLL_NAME\" value=\"AspriseJTwain.dll\">  ";
      corpoHtml += "<param name='CURRENT_DIR' value='"+path+"'> ";
      corpoHtml += "<param name=\"ABSOLUTE\" value=\"no\"> ";
      if (nome.length() != 0) {
        corpoHtml += "<PARAM NAME = 'FILE_NAME' VALUE = '"+nome+"'>";
      }
      if (Parametri.SCANSIONE_PDF_TIFF.equalsIgnoreCase("PDF")) {
      	corpoHtml += "<param name='PDF' value='yes'>";
      }
      corpoHtml += "<PARAM NAME = 'FUNCTION' VALUE = 'ScanOk'>";
      corpoHtml += "<param name=\"USE_DEFAULT\" value=\"no\"> "; 
      corpoHtml += "<param name=\"X_RESOLUTION\" value=\""+Parametri.X_RESOLUTION+"\"> "; 
      corpoHtml += "<param name=\"Y_RESOLUTION\" value=\""+Parametri.Y_RESOLUTION+"\"> "; 
      corpoHtml += "<param name=\"PIXEL_TYPE\" value=\""+Parametri.PIXEL_TYPE+"\"> "; 
      corpoHtml += "<param name=\"DUPLEX\" value=\""+Parametri.DUPLEX+"\"> "; 
      corpoHtml += "<param name=\"UI_ENABLED\" value=\""+Parametri.UI_ENABLED+"\"> "; 
      corpoHtml += "<param name=\"DLL_NAME\" value=\"AspriseJTwain.dll\"> ";
      corpoHtml += "<param name=\"java_version\" value=\"1.6+\">";
      corpoHtml += "<PARAM name=\"java_arguments\" value=\"-Xmx512m\">";
      corpoHtml += "<param name=\"UPLOAD_URL\" value='"+serverScheme+"://"+serverName+":"+serverPort+"/si4wscan/ScanServlet'> ";
      corpoHtml += "Oops, Your browser does not support Java applet! ";
      corpoHtml += "</applet> ";
      corpoHtml += "<INPUT TYPE='HIDDEN' NAME = 'NOME' VALUE = '"+nome+"'>";
      corpoHtml += "</body>\n<script language='Javascript'>function ScanOk() {";
      if (pdo.equalsIgnoreCase("CC")) {
        corpoHtml += "opener.addHiddenInput(opener.document.getElementById('mainForm'),NOME); window.close();} </script></html>";
      } else {
        corpoHtml += "opener.addHiddenInput(opener.document.getElementById('mainForm')); window.close();} </script></html>";
      }

    }
  }

  /**
   * 
   */
  public String getValue() {
    return corpoHtml;
  }

  private String getPathAreaFS(String area) throws Exception {
  	  String ret="",query;
  	  IDbOperationSQL dbOp = null;
      ResultSet      rst = null;
      
  	  try {
  		  query="Select decode(FORCE_FILE_ON_BLOB,1,'',nvl(PATH_FILE,'')) from aree where area= :P_AREA ";
  		  dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
          dbOp.setStatement(query);
          dbOp.setParameter(":P_AREA", area);
          dbOp.execute();
          
          rst = dbOp.getRstSet();
          
          if (rst.next()) ret=Global.nvl(rst.getString(1),"");
          
  		  free(dbOp);
  		  return ret;
  	  } catch (Exception e) {
  		  free(dbOp);
  		  throw new Exception("Upload::getPathAreaFS() - Attenzione! Si � verificato un errore in getPathAreaFS("+area+"): "+e.toString());
  	  }
  }


  private void free(IDbOperationSQL dbOp) {
	    try {
	      dbOp.close();
	    } catch (Exception e) { }
	  }
}
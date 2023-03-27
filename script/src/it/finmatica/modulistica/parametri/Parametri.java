package it.finmatica.modulistica.parametri;
import it.finmatica.dmServer.util.Global;
import java.sql.*;
import java.io.*; 
import java.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.authentication.Cryptable;

public class Parametri {
  public static String  USER            = ""; //"ads";
  public static String  PASSWD          = ""; //"ads";      
  public static String  PROGETTO        = ""; //"SPOR";
  public static String  ISTANZA         = ""; //"ADS";      
  public static String  SPORTELLO_DRIVER= ""; //"org.gjt.mm.mysql.Driver";
  public static String  SPORTELLO_DSN   = ""; //"jdbc:mysql://siriux/wall?user=ads&password=ads";    
  public static String  ALIAS           = ""; //oracle.
  public static String  JINDIDBNAME     = ""; //"@STANDARD";
  public static int     JINDIDBTYPE     = 0; //"@STANDARD";
  public static String  SERVERNAME      = ""; //"@STANDARD";
  public static String  SERVERPORT      = ""; //"@#@";
  public static String  PROTOCOLLO      = ""; 
  public static String  HTMLCONVERTER   = "";
  public static String  APPLICATIVO     = "";
  public static long    PARAM_CARICATI  = 0;
  public static String  CODIFICA_XSS  	= "N";
  public static String  VERIFICA_FILE  	= "N";

  public static String  AREASTANDARD    = ""; //"@STANDARD";
  public static String  SEPARAVALORI    = ""; //"@#@";
  public static String  DEBUG           = "0";  
  public static String  SCANNER         = "SI";  
  public static String  STAMPA_TEMPO    = "0";  
  public static String  MAX_MODELLI_MEM = "10";  
  public static String  SVILUPPO_FINMATICA = "N";  
  public static String  MIN_TIME_LOG    = "0";  
  public static String  STAMPA_SELECT_RICERCA    = "N";  

  public static String  DMNOTES_CODICEBEGIN     = ""; //"<CODICE>"; 
  public static String  DMNOTES_CODICEEND       = ""; //"</CODICE>"; 
  public static String  DMNOTES_VALOREBEGIN     = ""; //"<VALORE>"; 
  public static String  DMNOTES_VALOREEND       = ""; //"</VALORE>"; 
  public static String  PARAM_TAG_BEGIN         = ""; //"#@"; 
  public static String  PARAM_TAG_END           = ""; //"@#"; 
  public static String  URL_SERVER_DOMINO       = ""; //"URL_SERVER_DOMINO"; 

  public static String  JSIGN_LEVEL             = "";  

  public static String  FIRMA_URL			          = "";  
  public static String  FIRMA_PATH_CA           = "";  
  public static String  FIRMA_HTTP_CRL          = "S"; 
  public static String  FIRMA_MEMORIZZA_CRL     = "N"; 
  public static String  FIRMA_URL_VERIFICA_WS   = ""; 
  public static String  FIRMA_VERIFICA_CF   		= "N"; 
  public static String  FIRMA_TIPO				   		= "PKCS7"; 
  public static String  FIRMA_PDF_VISIBILE  		= "S"; 
  public static String  FIRMA_PDF_PAGINA	   		= "ULTIMA"; 
  public static String  FIRMA_PDF_POSIZIONE  		= "BASSO_DX"; 

  public static String  COPIA_CONF_TESTO        = "COPIA CONFORME ALL'ORIGINALE";  
  public static String  COPIA_CONF_DIAGONALE    = "S";  
  public static String  COPIA_CONF_BASSO_DX     = "N"; 
  public static String  COPIA_CONF_BASSO_SX     = "N"; 
  public static String  COPIA_CONF_ALTO_DX   		= "N"; 
  public static String  COPIA_CONF_ALTO_SX   		= "N"; 
  public static String  COPIA_CONF_PIE_PAG			= "N";

  public static String  MKPDF_PATH   						= ""; 
  public static String  MKPDF_LANDSCAPE   			= "N"; 
  
  public static String  ALLEGATI_SINGLE_SIGN_ON = "S"; 
  public static String  ALLEGATI_AUTO_SAVE 			= "N"; 
  public static String  ALLEGATI_AUTO_OPEN 			= "N"; 
  public static String  GENERA_IMPRONTA		 			= "N"; 
  public static String  SCANSIONE_PDF_TIFF 			= "TIFF"; 
  public static String  X_RESOLUTION	 					= "72"; 
  public static String  Y_RESOLUTION				 		= "72"; 
  public static String  PIXEL_TYPE 							= "1"; 
  public static String  DUPLEX 									= "NO"; 
  public static String  UI_ENABLED							= "YES"; 
  private static Connection  pConn;

  protected static String  contentType = "";    
  protected static String  tagTitleBegin = "";
  protected static String  tagTitleEnd = "";
  protected static String  tagHeadEnd = "";     
  protected static String  tagBodyBegin = "";   
  protected static String  tagBodyEnd = "";
  protected static String  tagBlockBegin = "";   
  protected static String  tagBlockEnd = "";    
  protected static String  tagFieldBegin = "";   
  protected static String  tagFieldEnd = "";     
  protected static String  tagTabBegin = "";   
  protected static String  tagTabEnd = "";    
  protected static String  tagPageBegin = "";   
  protected static String  tagPageEnd = "";    
  protected static String  tagPopBegin = "";   
  protected static String  tagPopEnd = "";    
  protected static String  tagVisualBegin = "";   
  protected static String  tagVisualEnd = "";    
  protected static String  tagVisualFineBegin = "";   
  protected static String  tagVisualFineEnd = "";    
  protected static String  nameBlockBegin = ""; 
  protected static String  nameBlockEnd = "";
  protected static String  namePopBegin = ""; 
  protected static String  namePopEnd = "";
  protected static String  nameTabBegin = ""; 
  protected static String  nameTabEnd = "";
  protected static String  nameFieldBegin = ""; 
  protected static String  nameFieldEnd = "";
  protected static String  nameVisualBegin = ""; 
  protected static String  nameVisualEnd = "";
  protected static String  nameVisualFineBegin = ""; 
  protected static String  nameVisualFineEnd = "";
  protected static String  offsetNameFieldBegin = "";
  protected static String  offsetNameFieldEnd = "";
  protected static String  offsetMetaInfoField = "";  
  protected static String  tagImgBegin = "";
  protected static String  tagImgEnd = "";
  protected static String  scanner = "";
  private   static boolean parametriModelloCaricati = false;

/*  public Parametri() {
    contentType     = "";
    tagHeadEnd      = ""; 
    tagPopBegin    = ""; 
    tagPopEnd      = ""; 
    tagPopBegin   = ""; 
    tagPopEnd     = ""; 
    nameBlockBegin   = ""; 
    nameBlockEnd     = ""; 
    tagBlockBegin   = ""; 
    tagBlockEnd     = ""; 
    nameBlockBegin   = ""; 
    nameBlockEnd     = ""; 
    tagFieldBegin   = ""; 
    tagFieldEnd     = ""; 
    nameFieldBegin  = ""; 
    nameFieldEnd    = ""; 
    tagPageBegin   = ""; 
    tagPageEnd     = ""; 
    tagVisualBegin   = ""; 
    tagVisualEnd     = ""; 
    nameVisualBegin   = ""; 
    nameVisualEnd     = ""; 
    offsetNameFieldBegin = "";
    offsetNameFieldEnd = "";
    offsetMetaInfoField = "";  // new
    tagTabBegin   = ""; 
    tagTabEnd     = ""; 
    nameTabBegin = "";
    nameTabEnd = "";
  }*/

  public static String getContentType() {
    if (contentType == null) {
      return "";
    }
    return contentType;  
  }
  
  public static String getTagTabBegin() {
    if (tagTabBegin == null) {
      return "";
    }
    return tagTabBegin;  
  }

  public static String getTagTabEnd() {
    if (tagTabEnd == null) {
      return "";
    }
    return tagTabEnd;  
  }
  
  public static String getTagTitleBegin() {
    if (tagTitleBegin == null) {
      return "";
    }
    return tagTitleBegin;  
  }

  public static String getTagTitleEnd() {
    if (tagTitleEnd == null) {
      return "";
    }
    return tagTitleEnd;  
  }
  
  public static String getTagHeadEnd() {
    if (tagHeadEnd == null) {
      return "";
    }
    return tagHeadEnd;  
  }

  public static String getTagBodyBegin() {
    if (tagBodyBegin == null) {
      return "";
    }
    return tagBodyBegin;  
  }

  public static String getTagBodyEnd() {
    if (tagBodyEnd == null) {
      return "";
    }
    return tagBodyEnd;  
  }

  public static String getTagVisualBegin() {
    if (tagVisualBegin == null) {
      return "";
    }
    return tagVisualBegin;  
  }

  public static String getTagVisualEnd() {
    if (tagVisualEnd == null) {
      return "";
    }
    return tagVisualEnd;  
  }

  public static String getTagVisualFineBegin() {
    if (tagVisualFineBegin == null) {
      return "";
    }
    return tagVisualFineBegin;  
  }

  public static String getTagVisualFineEnd() {
    if (tagVisualFineEnd == null) {
      return "";
    }
    return tagVisualFineEnd;  
  }

  public static String getTagPageBegin() {
    if (tagPageBegin == null) {
      return "";
    }
    return tagPageBegin;  
  }

  public static String getTagPageEnd() {
    if (tagPageEnd == null) {
      return "";
    }
    return tagPageEnd;  
  }

  public static String getTagPopBegin() {
    if (tagPopBegin == null) {
      return "";
    }
    return tagPopBegin;  
  }

  public static String getTagPopEnd() {
    if (tagPopEnd == null) {
      return "";
    }
    return tagPopEnd;  
  }

  public static String getTagFieldBegin() {
    if (tagFieldBegin == null) {
      return "";
    }
    return tagFieldBegin;  
  }

  public static String getTagFieldEnd() {
    if (tagFieldEnd == null) {
      return "";
    }
    return tagFieldEnd;  
  }

  public static String getNamePopBegin() {
    if (namePopBegin == null) {
      return "";
    }
    return namePopBegin;  
  }

  public static String getNamePopEnd() {
    if (namePopEnd == null) {
      return "";
    }
    return namePopEnd;  
  }

  public static String getNameFieldBegin() {
    if (nameFieldBegin == null) {
      return "";
    }
    return nameFieldBegin;  
  }

  public static String getNameFieldEnd() {
    if (nameFieldEnd == null) {
      return "";
    }
    return nameFieldEnd;  
  }

  public static String getNameTabBegin() {
    if (nameTabBegin == null) {
      return "";
    }
    return nameTabBegin;  
  }

  public static String getNameTabEnd() {
    if (nameTabEnd == null) {
      return "";
    }
    return nameTabEnd;  
  }

  public static String getTagBlockBegin() {
    if (tagBlockBegin == null) {
      return "";
    }
    return tagBlockBegin;  
  }

  public static String getTagBlockEnd() {
    if (tagBlockEnd == null) {
      return "";
    }
    return tagBlockEnd;  
  }

  public static String getNameBlockBegin() {
    if (nameBlockBegin == null) {
      return "";
    }
    return nameBlockBegin;  
  }

  public static String getNameBlockEnd() {
    if (nameBlockEnd == null) {
      return "";
    }
    return nameBlockEnd;  
  }

  public static String getNameVisualBegin() {
    if (nameVisualBegin == null) {
      return "";
    }
    return nameVisualBegin;  
  }

  public static String getNameVisualEnd() {
    if (nameVisualEnd == null) {
      return "";
    }
    return nameVisualEnd;  
  }

  public static String getNameVisualFineBegin() {
    if (nameVisualFineBegin == null) {
      return "";
    }
    return nameVisualFineBegin;  
  }

  public static String getNameVisualFineEnd() {
    if (nameVisualFineEnd == null) {
      return "";
    }
    return nameVisualFineEnd;  
  }

  public static int getOffsetNameFieldBegin() {
    return Integer.parseInt(offsetNameFieldBegin);  
  }

  public static int getOffsetNameFieldEnd() {
    return Integer.parseInt(offsetNameFieldEnd);  
  }

  public static int getOffsetMetaInfoField() {                // new
    return Integer.parseInt(offsetMetaInfoField);      // new
  }                                                    // new

  public static String getTagImgBegin() {
    return tagImgBegin;  
  }
  
  public static String getTagImgEnd() {
    return tagImgEnd;  
  }

  public static String getParametriDomini(String pCodice) {
    return caricaParametro(pCodice,"@DOMINI");  
  }

  public static String getParametriErrore(String pCodice) {
    return caricaParametro(pCodice,"@ERRORE");  
  }

  public static String getParametriLabel(String pArea, String pCodiceModello, String pLabel) {
    String retval = "";;
    
    retval = caricaEtichetta(pArea,pCodiceModello,pLabel,"@LABEL");
    if (retval == null) {
      retval = caricaEtichetta(pArea,"-",pLabel,"@LABEL");
      if (retval == null) {
        retval = caricaEtichetta("-","-",pLabel,"@LABEL");
        if (retval == null) {
          retval = "";
        }
      }
    }

    return retval;
  }

  public static void leggiParametriConnection(Connection sConn) throws Exception {
  	PARAM_CARICATI++;
  	pConn = sConn;
  	
    IDbOperationSQL dbOp1 = null;
    ResultSet   rs = null;
    String      query;
    String			codice = null;
    String      valore = null;
    
    ParametriStandard ps = new ParametriStandard();
    try {
      dbOp1 = SessioneDb.getInstance().createIDbOperationSQL(pConn,0);
      query = "SELECT CODICE, VALORE "+
              "FROM   PARAMETRI "+
              "WHERE  TIPO_MODELLO IN ('@STANDARD', '@FIRMA') " ;
      dbOp1.setStatement(query);
      dbOp1.execute();

      rs = dbOp1.getRstSet();
      while (rs.next())  {
      	codice = rs.getString("CODICE");
        valore = rs.getString("VALORE");
        ps.aggiungiParametro(codice, valore);
      }
    } catch (Exception ex) {
      try {
      	System.err.println("ParametriAdmin - Errore in fase di lettura parametri "+ex.toString());
        dbOp1.close();
      } catch (Exception e) {
      }
    }
    try {
      dbOp1.close();
    } catch (Exception e) {
    }

  	
  	
    CODIFICA_XSS     = ps.getValore("CODIFICA_XSS");
    if (CODIFICA_XSS == null || CODIFICA_XSS.length() == 0) {
    	CODIFICA_XSS = "N";
    }
  	
    VERIFICA_FILE     = ps.getValore("VERIFICA_FILE");
    if (VERIFICA_FILE == null || VERIFICA_FILE.length() == 0) {
    	VERIFICA_FILE = "N";
    }
  	
    SCANNER     = ps.getValore("SCANNER");
    if (SCANNER == null) {
      SCANNER = "SI";
    }
    AREASTANDARD    = ps.getValore("AREASTANDARD");
    SEPARAVALORI    = ps.getValore("SEPARAVALORI");
    DEBUG           = ps.getValore("DEBUG");
    if (DEBUG == null) {
      DEBUG = "1";
    }
    JSIGN_LEVEL          = ps.getValore("JSIGN_LEVEL");
    if (JSIGN_LEVEL == null) {
      JSIGN_LEVEL = "HIGH";
    }

    MKPDF_PATH   = ps.getValore("MKPDF_PATH");
    if (MKPDF_PATH == null) {
    	MKPDF_PATH = "";
    }

    MKPDF_LANDSCAPE   = ps.getValore("MKPDF_LANDSCAPE");
    if (MKPDF_LANDSCAPE == null) {
    	MKPDF_LANDSCAPE = "";
    }

    STAMPA_TEMPO         = ps.getValore("STAMPA_TEMPO");
    if (STAMPA_TEMPO == null) {
      STAMPA_TEMPO = "0";
    }

    GENERA_IMPRONTA      = caricaParametro("GENERA_IMPRONTA", "@STANDARD");
    if (GENERA_IMPRONTA == null) {
    	GENERA_IMPRONTA = "N";
    }

    MAX_MODELLI_MEM      = ps.getValore("MAX_MODELLI_MEM");
    if (MAX_MODELLI_MEM == null) {
      MAX_MODELLI_MEM = "10";
    }

    MIN_TIME_LOG      = ps.getValore("MIN_TIME_LOG");
    if (MIN_TIME_LOG == null) {
      MIN_TIME_LOG = "0";
    }

    STAMPA_SELECT_RICERCA      = ps.getValore("STAMPA_SELECT_RICERC");
    if (STAMPA_SELECT_RICERCA == null) {
    	STAMPA_SELECT_RICERCA = "N";
    }

    SVILUPPO_FINMATICA   = ps.getValore("SVILUPPO_FINMATICA");
    if (SVILUPPO_FINMATICA == null) {
      SVILUPPO_FINMATICA = "N";
    }

    FIRMA_URL   = ps.getValore("FIRMA_URL");
    if (FIRMA_URL == null) {
    	FIRMA_URL = "";
    }

    FIRMA_PATH_CA   = ps.getValore("FIRMA_PATH_CA");
    if (FIRMA_PATH_CA == null) {
      FIRMA_PATH_CA = "";
    }

    FIRMA_HTTP_CRL  = ps.getValore("FIRMA_HTTP_CRL");
    if (FIRMA_HTTP_CRL == null) {
      FIRMA_HTTP_CRL = "S";
    }

    FIRMA_MEMORIZZA_CRL  = ps.getValore("FIRMA_MEMORIZZA_CRL");
    if (FIRMA_MEMORIZZA_CRL == null) {
    	FIRMA_MEMORIZZA_CRL = "N";
    }

    FIRMA_URL_VERIFICA_WS  = ps.getValore("FIRMA_URL_VERIFICAWS");
    if (FIRMA_URL_VERIFICA_WS == null) {
    	FIRMA_URL_VERIFICA_WS = "";
    }

    FIRMA_VERIFICA_CF  = ps.getValore("FIRMA_VERIFICA_CF");
    if (FIRMA_VERIFICA_CF == null) {
    	FIRMA_VERIFICA_CF = "N";
    }

    FIRMA_TIPO  = ps.getValore("FIRMA_TIPO");
    if (FIRMA_TIPO == null) {
    	FIRMA_TIPO = "PKCS7";
    }

    FIRMA_PDF_VISIBILE  = ps.getValore("FIRMA_PDF_VISIBILE");
    if (FIRMA_PDF_VISIBILE == null) {
    	FIRMA_PDF_VISIBILE = "S";
    }

    FIRMA_PDF_PAGINA  = ps.getValore("FIRMA_PDF_PAGINA");
    if (FIRMA_PDF_PAGINA == null) {
    	FIRMA_PDF_PAGINA = "ULTIMA";
    }

    FIRMA_PDF_POSIZIONE  = ps.getValore("FIRMA_PDF_POSIZIONE");
    if (FIRMA_PDF_POSIZIONE == null) {
    	FIRMA_PDF_POSIZIONE = "BASSO_DX";
    }

    COPIA_CONF_TESTO  = ps.getValore("COPIA_CONF_TESTO");
    if (COPIA_CONF_TESTO == null) {
    	COPIA_CONF_TESTO = "COPIA CONFORME ALL'ORIGINALE";
    }

    COPIA_CONF_DIAGONALE  = ps.getValore("COPIA_CONF_DIAGONALE");
    if (COPIA_CONF_DIAGONALE == null) {
    	COPIA_CONF_DIAGONALE = "N";
    }

    COPIA_CONF_BASSO_DX  = ps.getValore("COPIA_CONF_BASSO_DX");
    if (COPIA_CONF_BASSO_DX == null) {
    	COPIA_CONF_BASSO_DX = "N";
    }

    COPIA_CONF_BASSO_SX  = ps.getValore("COPIA_CONF_BASSO_SX");
    if (COPIA_CONF_BASSO_SX == null) {
    	COPIA_CONF_BASSO_SX = "N";
    }

    COPIA_CONF_ALTO_DX  = ps.getValore("COPIA_CONF_ALTO_DX");
    if (COPIA_CONF_ALTO_DX == null) {
    	COPIA_CONF_ALTO_DX = "N";
    }

    COPIA_CONF_ALTO_SX  = ps.getValore("COPIA_CONF_ALTO_SX");
    if (COPIA_CONF_ALTO_SX == null) {
    	COPIA_CONF_ALTO_SX = "N";
    }

    COPIA_CONF_PIE_PAG  = ps.getValore("COPIA_CONF_PIE_PAG");
    if (COPIA_CONF_PIE_PAG == null) {
    	COPIA_CONF_PIE_PAG = "N";
    }

    ALLEGATI_SINGLE_SIGN_ON  = ps.getValore("ALLEG_SINGLE_SIGN_ON");
    if (ALLEGATI_SINGLE_SIGN_ON == null) {
    	ALLEGATI_SINGLE_SIGN_ON = "S";
    }

    ALLEGATI_AUTO_SAVE  = ps.getValore("ALLEGATI_AUTO_SAVE");
    if (ALLEGATI_AUTO_SAVE == null) {
    	ALLEGATI_AUTO_SAVE = "N";
    }

    ALLEGATI_AUTO_OPEN  = ps.getValore("ALLEGATI_AUTO_OPEN");
    if (ALLEGATI_AUTO_OPEN == null) {
    	ALLEGATI_AUTO_OPEN = "N";
    }

    SCANSIONE_PDF_TIFF  = ps.getValore("SCANSIONE_PDF_TIFF");
    if (SCANSIONE_PDF_TIFF == null) {
    	SCANSIONE_PDF_TIFF = "TIFF";
    }
    if (!SCANSIONE_PDF_TIFF.equalsIgnoreCase("PDF") && !SCANSIONE_PDF_TIFF.equalsIgnoreCase("TIFF")) {
    	SCANSIONE_PDF_TIFF = "TIFF";
    }

    X_RESOLUTION  = ps.getValore("X_RESOLUTION");
    if (X_RESOLUTION == null) {
    	X_RESOLUTION = "72";
    }

    Y_RESOLUTION  = ps.getValore("Y_RESOLUTION");
    if (Y_RESOLUTION == null) {
    	Y_RESOLUTION = "72";
    }

    PIXEL_TYPE  = ps.getValore("PIXEL_TYPE");
    if (PIXEL_TYPE == null) {
    	PIXEL_TYPE = "1";
    }

    DUPLEX  = ps.getValore("DUPLEX");
    if (DUPLEX == null) {
    	DUPLEX = "NO";
    } else {
    	if (DUPLEX.equalsIgnoreCase("S")) {
    		DUPLEX = "YES";
    	} else {
    		DUPLEX = "NO";
    	}
    }

    UI_ENABLED  = ps.getValore("UI_ENABLED");
    if (UI_ENABLED == null) {
    	UI_ENABLED = "YES";
    } else {
    	if (UI_ENABLED.equalsIgnoreCase("S")) {
    		UI_ENABLED = "YES";
    	} else {
    		UI_ENABLED = "NO";
    	}
    }

    DMNOTES_CODICEBEGIN  = ps.getValore("DMNOTES_CODICEBEGIN");
    DMNOTES_CODICEEND    = ps.getValore("DMNOTES_CODICEEND");
    DMNOTES_VALOREBEGIN  = ps.getValore("DMNOTES_VALOREBEGIN");
    DMNOTES_VALOREEND    = ps.getValore("DMNOTES_VALOREEND");
    PARAM_TAG_BEGIN      = ps.getValore("PARAM_TAG_BEGIN");
    PARAM_TAG_END        = ps.getValore("PARAM_TAG_END");
    URL_SERVER_DOMINO    = ps.getValore("URL_SERVER_DOMINO");
    ps = null;
  }
  
  protected static String caricaParametro(String pCodice, String pTipoModello) {
    IDbOperationSQL dbOp1 = null;
    ResultSet   rs = null;
    String      query = "";
    String      valore = null;
    
    try {
    	if (JINDIDBNAME.length() > 0) {
    		dbOp1 = SessioneDb.getInstance().createIDbOperationSQL(JINDIDBNAME,JINDIDBTYPE);
    	} else {
    		dbOp1 = SessioneDb.getInstance().createIDbOperationSQL(pConn,0);
    	}
/*  		query = "select 1 from dual";
  		try {
  			dbOp1 = SessioneDb.getInstance().createIDbOperationSQL(pConn,0);
    		dbOp1.setStatement(query);
    		dbOp1.execute();
    		rs = dbOp1.getRstSet();
    		if (rs.next()) {
    			rs.getInt(1);
    		}
  		} catch (Exception e) {
  			dbOp1 = SessioneDb.getInstance().createIDbOperationSQL(JINDIDBNAME,JINDIDBTYPE);
  		}*/
      
      query = "SELECT VALORE "+
              "FROM   PARAMETRI "+
              "WHERE  CODICE = :CODICE AND "+
              "TIPO_MODELLO = :TIPO" ;
      dbOp1.setStatement(query);
      dbOp1.setParameter(":CODICE", pCodice);
      dbOp1.setParameter(":TIPO", pTipoModello);
      dbOp1.execute();

      rs = dbOp1.getRstSet();
      if (rs.next()) 
        valore = rs.getString("VALORE");
    } catch (Exception ex) {
    	System.out.println(ex.getMessage());
      try {
        dbOp1.close();
      } catch (Exception e) {
      }
    }
    try {
      dbOp1.close();
    } catch (Exception e) {
    }
    return valore;
  }

	protected int testaCn(IDbOperationSQL dbOp) {
		int retval = 0;
		return retval;
	}
	
  public static void leggiParametri(String pSetupFile, Connection cn) throws Exception {
    leggiParametriProperties(pSetupFile);
    leggiParametriConnection(cn);
  }
  
  public static void leggiParametriStandard(String pSetupFile) throws Exception {
    leggiParametriProperties(pSetupFile);
    IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(JINDIDBNAME,JINDIDBTYPE);
    leggiParametriConnection(dbOp.getConn());
    try {
      dbOp.close();
    } catch(Exception e) {
    }
  }

  public static void leggiParametriProperties(String pSetupFile) throws Exception {
    IDbOperationSQL dbOp = null;
    ResultSet   rs = null;
    String      passwdCrypt = null;
    String      query = null;

    try {
      FileInputStream        fIni = new FileInputStream(pSetupFile);
      PropertyResourceBundle ini = new PropertyResourceBundle(fIni);

      PROGETTO          = ini.getString("PROGETTO");
      ISTANZA           = ini.getString("ISTANZA");
      try {
        JINDIDBNAME       = ini.getString("JINDIDBNAME");
      } catch(Exception e) {
        JINDIDBNAME = "jdbc/gdm";
      }

      if (Global.nvl(JINDIDBNAME,"").equals("")) {
        JINDIDBNAME="jdbc/gdm";
      }

      SERVERNAME        = ini.getString("SERVERNAME");
      SERVERPORT        = ini.getString("SERVERPORT");
      PROTOCOLLO        = ini.getString("PROTOCOLLO");
      String jType = "";
      try {
        jType = ini.getString("JINDIDBNAME");
      } catch(Exception e) {
        jType = null;
      }

      if (jType == null){
        JINDIDBTYPE = 0;
      } else {
        try {
          JINDIDBTYPE = Integer.parseInt(jType);
        } catch(Exception e) {
          JINDIDBTYPE = 0;
        }
      }

      if (JINDIDBNAME == null){
        JINDIDBNAME = "jdbc/gdm";
      }

      if (SERVERNAME == null){
        SERVERNAME = "";
      }
      if (SERVERPORT == null){
        SERVERPORT = "";
      }
      if (PROTOCOLLO == null){
        PROTOCOLLO = "";
      }
      try {
        APPLICATIVO = ini.getString("APPLICATIVO");
      } catch(Exception noex) {
        APPLICATIVO   = "jgdm";
      }
      if (APPLICATIVO == null){
        APPLICATIVO = "jgdm";
      }

      try {
        HTMLCONVERTER   = ini.getString("HTMLCONVERTER");
      } catch(Exception noex) {
        HTMLCONVERTER   = "it.finmatica.pdf.ITextConverterImpl";
      }

      USER = ini.getString("INIT_DB_USER");
      PASSWD = ini.getString("INIT_DB_PASSWD");
      SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");

      // ---------------------------------
      try {
        if (USER.length() == 0 || PASSWD.length() == 0) {
          dbOp = SessioneDb.getInstance().createIDbOperationSQL("jdbc/ad4",JINDIDBTYPE);
          query = "SELECT USER_ORACLE, PASSWORD_ORACLE "+
                  "FROM   ISTANZE "+
                  "WHERE  PROGETTO = :PROGETTO AND "+
                  "ISTANZA = :ISTANZA" ;
          dbOp.setStatement(query);
          dbOp.setParameter(":PROGETTO", PROGETTO);
          dbOp.setParameter(":ISTANZA", ISTANZA);
          dbOp.execute();

          rs = dbOp.getRstSet();
          if (rs.next())
            USER        = rs.getString("USER_ORACLE");
          passwdCrypt = rs.getString("PASSWORD_ORACLE");

          try
          {
            dbOp.close();
          }
          catch(Exception e)
          {
            System.err.println("ParametriAdmin - Mancata close()");
          }
        } else {
          passwdCrypt = "";
        }
        try {
          dbOp = SessioneDb.getInstance().createIDbOperationSQL("jdbc/gdm", JINDIDBTYPE);
          SPORTELLO_DSN = dbOp.getConn().getMetaData().getURL();
          if (dbOp.getTipoDb() == 0) {
            ALIAS = "oracle.";
            SPORTELLO_DRIVER = "oracle.jdbc.driver.OracleDriver";
          }
          if (dbOp.getTipoDb() == 1) {
            ALIAS = "postgres.";
            SPORTELLO_DRIVER = "org.postgresql.Driver";
          }
        } catch (Exception ex) {
          //Se nn riesco dalla jndi ci provo dall'INIT_DSN
          SPORTELLO_DSN        = ini.getString("INIT_DSN");
          ALIAS = "oracle.";
          SPORTELLO_DRIVER = "oracle.jdbc.driver.OracleDriver";
        }
      } catch (Exception ex) {
        System.err.println("ParametriAdmin - Errore in fase di lettura Utente/Password "+ex.toString());
      }

      try {
        dbOp.close();
      } catch (Exception e) {
        System.err.println("ParametriAdmin - Mancata close()");
      }

      if (passwdCrypt != null && passwdCrypt.length() > 0) {
        PASSWD = Cryptable.decryptPasswd(passwdCrypt);
      }

      // ------------------------------------------

    } catch(Exception e) {

      e.printStackTrace();
    }

  }

  public static synchronized IDbOperationSQL creaDbOp() throws Exception {
    IDbOperationSQL dbOperationSQL;

    dbOperationSQL = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

    if (dbOperationSQL==null) {
      dbOperationSQL = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
    }

    return dbOperationSQL;
  }


  public static void settaParametriModello(String pTipoModello) throws Exception {
    if (parametriModelloCaricati) {
     return;
    }
    contentType         = caricaParametro("CONTENT_TYPE", pTipoModello);
    tagTitleBegin       = caricaParametro("TAGTITLEBEGIN", pTipoModello);
    tagTitleEnd         = caricaParametro("TAGTITLEEND", pTipoModello);
    tagHeadEnd          = caricaParametro("TAGHEADEND", pTipoModello);
    tagBodyBegin        = caricaParametro("TAGBODYBEGIN", pTipoModello);
    tagBodyEnd          = caricaParametro("TAGBODYEND", pTipoModello);
    tagBlockBegin       = caricaParametro("TAGBLOCKBEGIN", pTipoModello);
    tagBlockEnd         = caricaParametro("TAGBLOCKEND", pTipoModello);
    tagFieldBegin       = caricaParametro("TAGFIELDBEGIN", pTipoModello);
    tagFieldEnd         = caricaParametro("TAGFIELDEND", pTipoModello);
    tagTabBegin         = caricaParametro("TAGTABBEGIN", pTipoModello);
    tagTabEnd           = caricaParametro("TAGTABEND", pTipoModello);
    tagPopBegin         = caricaParametro("TAGPOPBEGIN", pTipoModello);
    tagPopEnd           = caricaParametro("TAGPOPEND", pTipoModello);
    tagPageBegin        = caricaParametro("TAGPAGEBEGIN", pTipoModello);
    tagPageEnd          = caricaParametro("TAGPAGEEND", pTipoModello);
    tagVisualBegin      = caricaParametro("TAGVISUALBEGIN", pTipoModello);
    tagVisualEnd        = caricaParametro("TAGVISUALEND", pTipoModello);
    tagVisualFineBegin  = caricaParametro("TAGVISUALFINEBEGIN", pTipoModello);
    tagVisualFineEnd    = caricaParametro("TAGVISUALFINEEND", pTipoModello);
    nameBlockBegin      = caricaParametro("NAMEBLOCKBEGIN", pTipoModello);
    nameBlockEnd        = caricaParametro("NAMEBLOCKEND", pTipoModello);
    nameFieldBegin      = caricaParametro("NAMEFIELDBEGIN", pTipoModello);
    nameFieldEnd        = caricaParametro("NAMEFIELDEND", pTipoModello);
    nameTabBegin        = caricaParametro("NAMETABBEGIN", pTipoModello);
    nameTabEnd          = caricaParametro("NAMETABEND", pTipoModello);
    namePopBegin        = caricaParametro("NAMEPOPBEGIN", pTipoModello);
    namePopEnd          = caricaParametro("NAMEPOPEND", pTipoModello);
    nameVisualBegin     = caricaParametro("NAMEVISUALBEGIN", pTipoModello);
    nameVisualEnd       = caricaParametro("NAMEVISUALEND", pTipoModello);
    nameVisualFineBegin = caricaParametro("NAMEVISUALFINEBEGIN", pTipoModello);
    nameVisualFineEnd   = caricaParametro("NAMEVISUALFINEEND", pTipoModello);
    offsetNameFieldBegin  = caricaParametro("OFFSETNAMEFIELDBEGIN", pTipoModello);
    offsetNameFieldEnd  = caricaParametro("OFFSETNAMEFIELDEND", pTipoModello);
    offsetMetaInfoField = caricaParametro("OFFSETMETAINFOFIELD", pTipoModello);  // (new)
    tagImgBegin         = caricaParametro("TAGIMGBEGIN", pTipoModello);
    tagImgEnd           = caricaParametro("TAGIMGEND", pTipoModello);

    parametriModelloCaricati = true;
  }

  protected static String caricaEtichetta(String area, String cm, String etichetta, String pTipoModello) {
    IDbOperationSQL dbOp1 = null;
    ResultSet   rs = null;
    String      query;
    String      valore = null;
    
    try {
      dbOp1 = SessioneDb.getInstance().createIDbOperationSQL(JINDIDBNAME,JINDIDBTYPE);
      query = "SELECT VALORE "+
              "FROM   ETICHETTE "+
              "WHERE  AREA = :AREA AND "+
              "CODICE_MODELLO = :CM AND " +
              "ETICHETTA = :ETICHETTA";
      dbOp1.setStatement(query);
      dbOp1.setParameter(":AREA", area);
      dbOp1.setParameter(":CM", cm);
      dbOp1.setParameter(":ETICHETTA", etichetta);
      dbOp1.execute();

      rs = dbOp1.getRstSet();
      if (rs.next()) {
        valore = rs.getString("VALORE");
        if (valore == null) {
          valore = "";
        }
      }
    } catch (Exception ex) {
      try {
        dbOp1.close();
      } catch (Exception e) {
        System.err.println("ParametriAdmin::caricaEtichetta() - Mancata close()");
      }
      System.err.println("ParametriAdmin::caricaEtichetta() - Errore in fase di lettura ETICHETTA "+etichetta);
    }
    try {
      dbOp1.close();
    } catch (Exception e) {
      System.err.println("ParametriAdmin::caricaEtichetta() - Mancata close()");
    }
    return valore;
  }

}
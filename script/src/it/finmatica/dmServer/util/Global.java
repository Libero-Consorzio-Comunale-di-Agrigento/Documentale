package it.finmatica.dmServer.util;

/*
 * VARIABILI/COSTANTI E METODI GLOBALI
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.jfc.dbUtil.IDbOperationSQL;

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Properties;
import java.lang.reflect.InvocationTargetException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.sql.Clob;
import java.sql.Connection;
import java.io.*;

import oracle.sql.CLOB;

public class Global 
{ 
 
  // ***************** DEFINIZIONI DELLE COSTANTI GLOBALI ***************** //
  public final static int MSECALIVE=60000;  //1 minuto   
  public boolean bExitThreadRicerca=false;
  
  //Costante di debbugging
  public final static boolean DEBUG = false;
   
  public final static String FINMATICA_DM = "GD4";
  public final static String HUMMINGBIRD_DM = "Humm";
  
  //Utenti particolari
  public final static String GUEST   = "GUEST";
  public final static String UTEWKF  = "UTEJWF";
  
  //Applicativi con permessi speciali 
  public final static String APPLWKF  = "JWF";
  public final static String ENTEWKF  = "JWF";
  
  //Varie
  public final static String AREAGDM    = "AREAGDM";
  public final static String APPL_ABIL  = "MODULISTICA"; 
  public final static String RUOLO_GDM  = "GDM";
  public final static String NOBODY     = "@NOBODY@";
  public final static String AREA_SYS   = "GDMSYS";
  public final static String CART_STD   = "CartellaStandard";
  
  //
  public final static String ENTE_STANDARD="ENTE STANDARD";
  public final static String APPL_STANDARD="APPLICATIVO STANDARD";
  
  //Gestione tipi di competenza
  public final static String GEST_NESS  = "0";
  public final static String GEST_UTEN  = "1"; 
  public final static String GEST_GRUP  = "2";
  
  //Tipi di Competenza
  public final static String ABIL_LETT     = "L";  
  public final static String ABIL_MODI     = "U"; 
  public final static String ABIL_CREA     = "C";
  public final static String ABIL_CANC     = "D";
  public final static String ABIL_EXEC     = "X";
  public final static String ABIL_GEST     = "M";
  public final static String ABIL_LETTALL  = "LA";
  public final static String ABIL_MODIALL  = "UA";
  public final static String ABIL_CANCALL  = "DA";
  
  public final static String ABIL_LETT_ALLE     = "LA";  
  public final static String ABIL_MODI_ALLE     = "UA"; 
  public final static String ABIL_CANC_ALLE     = "DA"; 
  
  
  //Tipi di Competenza Alfresco
  public final static String ABIL_CONSUMER     = "CON";  
  public final static String ABIL_EDITOR       = "EDI"; 
  public final static String ABIL_CONTRIBUTOR  = "CNT";
  public final static String ABIL_COORDINATOR  = "COO";  
  
  //Oggetti soggetti a competenza
  public final static String ABIL_DOC        = "DOCUMENTI";  
  public final static String ABIL_TIPIDOC    = "TIPI_DOCUMENTO"; 
  public final static String ABIL_CARTELLA   = "VIEW_CARTELLA";   
  public final static String ABIL_QUERY      = "QUERY";   
  
  //Alias di connessione per DbOperationSQL
  public final static String ALIAS_POSTGRES  = "postres.";
  public final static String ALIAS_ORACLE    = "oracle.";
  public final static String ALIAS_MYSQL     = "mysql.";

  //Driver di connessione per DbOperationSQL
  public final static String DRIVER_POSTGRES = "postgresql.Driver";
  public final static String DRIVER_ORACLE   = "oracle.jdbc.driver.OracleDriver";
  public final static String DRIVER_MYSQL    = "org.gjt.mm.mysql.Driver";
  
  //Gestione degli oggetti file
  public final static String MANAGE_TYPE_BLOB  = "1";
  public final static String MANAGE_TYPE_BFILE = "2";
  public final static String MANAGE_TYPE_AUTO  = "3";
  
  //Gestione delle ACL per Humm e Finm
  public static String NO_ACCESS = "-1";
  public static String COMPLETE_ACCESS = "0";
  public static String NORMAL_ACCESS = "1";
  public static String READONLY_ACCESS = "2";
  //Questa utilizzata solo per cartella
  public static String INFOLDER_ACCESS = "3";
  
  //Azioni possibili
  public final static String TYPE_AZIONE_LETTURA   = "L";
  public final static String TYPE_AZIONE_MODIFICA  = "M";
  public final static String TYPE_AZIONE_CREA      = "C";
  public final static String TYPE_AZIONE_ELIMINA   = "E";
  public final static String TYPE_AZIONE_REVISIONE = "R";
 

  //Tipi di log
  public final static String TYPE_NO_LOG     = "N"; //Nessun Log
  public final static String TYPE_STD_LOG    = "S"; //Log Standard (Creaz.+Modify doc+campi con flag log a true)
  public final static String TYPE_STDVAL_LOG = "V"; //Log Standard con Valori (Creaz.+Modify doc+tutti i campi)
  public final static String TYPE_MAX_LOG    = "M"; //Log Massimo (Come Standard + Read doc (no read campi) )
  public final static String TYPE_MAXVAL_LOG = "X"; //Log Massimo con Valori (Come Standard con valori + Read doc (no read campi) )
  
  //Tipi di riferimento
  public final static String RIF_PRECEDENTE     = "PREC";
  public final static String RIF_VERSIONE       = "VER";
  public final static String RIF_SOGGETTO       = "SOGG";  
  public final static String RIF_REVISIONE      = "REV";    
  public final static String RIF_INVMAIL        = "MAILINV";      
  public final static String RIF_HUMM           = "1";
  
  //Possibili stati per i documenti
  public final static String STATO_BOZZA        = "BO";
  public final static String STATO_SOSPESO      = "SO";
  public final static String STATO_COMPLETO     = "CO";
  public final static String STATO_ANNULLATO    = "AN";
  public final static String STATO_PUBBLICO     = "PU";
  public final static String STATO_PROTOCOLLATO = "PR";
  public final static String STATO_ARCHIVIATO   = "A" ;
  public final static String STATO_REVISIONATO  = "RE";
  public final static String STATO_CHECKIN      = "I" ;
  public final static String STATO_CHECKOUT     = "O" ;
  public final static String STATO_ATTESAREV    = "AR";
  public final static String STATO_CANCELLATO   = "CA";
  public final static String STATO_PREBOZZA     = "PB";

  //Possibili stati per i documenti
  public final static String ROOT_USER_FOLDER   = "U";
  public final static String ROOT_SYSTEM_FOLDER = "S";  

  //Motore di ricerca
  public final static int    COND_AND       = 0;
  public final static int    COND_OR        = 1;
  public final static int    COND_NOT       = 2;
  public final static int    COND_SINGLE    = 3;
  public final static int    COND_TIMEOUT   = 4;
  public final static int    MAX_COND       = 5;
  public final static int    MAX_COND_CAMPI = 4;    
  
  
  //File di sistema
  public final static int    SYS_HASH    = 0;
  public final static int    SYS_PDF     = 1;
  
  //Percorso Assoluto File nel File System
  public final static String  PATH_FILE_ABSOLUTE = "DOC_FILES";
  
  // Filtro standard per la gestione query
  public final static String FILTRO_STANDARD = "<?xml version="+"''"+"1.0"+"''"+"encoding="+"''"+"UTF-8"+"''"+" ?>"+
  "<DOC_INFO xmlns:xsi="+"''"+"http://www.w3.org/2000/10/XMLSchema-instance"+"''"+" xsi:noNamespaceSchemaLocation="+"''"+"doc_info_v1.4.1.xsd"+"''"+">"+
  "</DOC_INFO>";
  
  //Gestione dell'accesso al profilo mediante
  //allegati o senza allegati (solo valori)
  public final static String ACCESS_NO_ATTACH = "0";        
  public final static String ACCESS_ATTACH = "1";   
  
  public final static String IS_USER = "user";      
  public final static String IS_GROUP = "group";   
  
  //Costanti per il LOG4JSUITE
  public final static String CATEGO_RICERCA_SEMPLICE_HORIZ 	      = "HRIC";   
  public final static String CATEGO_RICERCA_SEMPLICE_DAWEB_HORIZ  = "HRICWEB";  
  public final static String CATEGO_RICERCA_SEMPLICE_VERT 	      = "VRIC";   
  public final static String CATEGO_RICERCA_SEMPLICE_DAWEB_VERT   = "VRICWEB";  
  public final static String TAG_RICERCA_SEMPLICE_CREATESQL       = "SQLCREATE";
  public final static String TAG_RICERCA_SEMPLICE_EXECSQL         = "SQLEXCEC";
  public final static String TAG_RICERCA_SEMPLICE_ERRORSQL        = "SQLERROR";
  public final static String TAG_RICERCA_SEMPLICE_TIMEOUTSQL      = "SQLTIMEOUT";  
  public final static String TAG_RICERCA_SEMPLICE_NOROWSSQL       = "SQLNOROWS";
  public final static String TAG_RICERCA_SEMPLICE_GENERRORSQL     = "SQLGENERROR";
  
  //Costanti errore profilo
  public final static String CODERROR_NOT_DEFINED                          = "0";
  public final static String CODERROR_ACCESS_NOCOMPETENZE_READ             = "-1";
  public final static String CODERROR_ACCESS_TBLDOCUMENTI                  = "-2";
  public final static String CODERROR_ACCESS_VALORI                        = "-3";
  public final static String CODERROR_ACCESS_OGGETTIFILE                   = "-4";
  public final static String CODERROR_ACCESS_DOCUMENT_NOTEXISTS            = "-5";
   
  public final static String CODERROR_SYNCRO_INTEGRATION_ERROR             = "-6";
  
  public final static String CODERROR_SAVEDOCUMENT_UK                      = "UK";
  public final static String DESCRERROR_SAVEDOCUMENT_UK                    = "Attenzione! Univocità di dati violata";
  
  public final static String CODERROR_SAVEDOCUMENT_TRIGGER                 = "TG";
  
  
  public final static String CODERROR_SAVEDOCUMENT_COMPALLEGATI            = "COMPALL";  
  
  public final static String CODERROR_SAVEDOCUMENT_ALLEGATIMAXDIM          = "MAXDIMALL";
  
  
  //Costanti errore profilo post save
  public final static String CODERROR_POSTSAVEDOCUMENT_FOLDERAUTO          = "FA";
  public final static String DESCRERROR_POSTSAVEDOCUMENT_FOLDERAUTO        = "Attenzione! Cartelle in automatico.";
    
  public final static String MARK_AS_OBJFILE_INSERT                        = "INSERT";
  public final static String MARK_AS_OBJFILE_UPDATE                        = "UPDATE";
  public final static String MARK_AS_OBJFILE_DELETE                        = "DELETE";
  
  //Costanti errore Impronte Allegati
  public final static String CODERROR_IA_NESSUN_ERRORE				       = "1";
  public final static String CODERROR_IA_DOCUMENTO_INESISTENTE             = "0";
  public final static String CODERROR_IA_IMPRONTA_ASSENTE                  = "-1";
  public final static String CODERROR_IA_ALLEGATO_MODIFICATO               = "-2";
  public final static String CODERROR_IA_ALLEGATO_CANCELLATO               = "-3";
  
  //Caratteri speciali da eliminare nel nome del file...per adesso c'è il solo tab....aggiungere qui per altri caratteri    
  public final static String[] specialChrFile = {"\t"};
  
  // ***************** DEFINIZIONI DELLE VARIABILI GLOBALI ***************** //

  public String URL_POSTGRES;
  public String URL_ORACLE;
  public String URL_MYSQL;

  public String PACKAGE = "it.finmatica.dmServer";  
  public String DM;
  public String USE_INTERMEDIA;
  public String REBUILD_IMMEDIATE;  
  
  public String DOCUMENTO;
  public String LIBRERIA;
  public String TIPODOC;
  public String STATIDOC;
  public String VALORI;
  public String OGGETTI_FILE;
  public String CAMPI_DOCUMENTO;
  public String DATI;
  public String CLASSIFICAZIONI;
  public String CRITERIO;
  public String CONDIZIONI;
  public String INIT_DB_TYPE;
  public String INIT_DB_ALIAS;
  public String INIT_DB_HOST;
  public String INIT_DB_PORT;
  public String INIT_DB_SID;
  public String INIT_DB_USER;
  public String INIT_DB_PASSWD;
  public String INIT_JNDI;
  
  public String MANAGE_TYPE_FILE; 
    
  public String INIT_DSN;
  
  public boolean CLOSECONNECTION_EXTERN;
  public boolean IS_PROFILO_OPERATION = false;
  public Connection CONNECTION;
  public JNDIParameter JNDIPARM; 
  
  public String DIR_CLI_TEMP;
  public String EXPLORER_DIR;

  public String WEB_SERVER_TYPE;
  public String DB_SERVER_TYPE;
  public String WEB_ROOT_REPOSITORY;
  public String DB_ROOT_REPOSITORY;
  
  public String DOMINIO_RICERCA_VALORI;
  public String DOMINIO_RICERCA_OGGETTIFILE;
 
  public String USE_MAPPING;
  public String MSYSTEM_PATH;
  public String MAPPING_PATH;
  
  public String MAPPING_ENTE;
  public String MAPPING_APPL;
  
  public String WEB_MODULISTICA_HOST;

  //DOCAREA - PANTAREI
  public String MAPPING_LIBRARY; 
  public String MAPPING_TIPO_DOC_PANTAREI;
  public String WEB_HOST_SERVICE_PANTA ; 
  //public String HUMMINGBIRD_DM; // Humm
  public String DIR_XML_MIME;
  public String APPLICATIVO;
  
  public String LOG_SQL = "N";
  public int    QUERY_TIMEOUT = MSECALIVE;
  public String PRINT_QUERY = "N";
  public String PRINT_WAREA = "N";
  public String PRINT_TREEVIEW = "N";
  public String PARAM_COMPETENZE = "AD4";
  public String PARAM_DEBUG = "1";
  public int    QRYSERVICE_LIMIT = 0;
  
  public long DIM_MAX_ALL_BYTE = -1;
  
  public String URL_ORACLE_PARAM ;

  private boolean usaIniPerTest = false;

  // ********** DEFINIZIONE DEI METODI PUBBLICI ********** //

  /*
   * METHOD:      settaGlobal(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta le variabili globali a partire dal file properties
   *                    
   * RETURN:      void
  */
  public void settaGlobal(String iniFile) throws Exception
  {
	  	//initVar();
	     
         Properties properties = new Properties();
         if (iniFile == null) iniFile = "gd4dm.properties";            



         try {         
             load(properties, iniFile);      
           }
           catch (Exception e)
           {

               //Ricerca nel percorso base
               try {                 
                  load(properties, Global.adjustsPath("U",iniFile));                      
                 }
                 catch (Exception eii) {

                    try 
                    {
                      iniFile = "gd4dm.properties";
                      load(properties, iniFile);
                    }
                    catch (Exception eiii)
                    {

                      try 
                         {    
                            load(properties, "c:\\temp\\gd4dm.properties");                         
                        }
                           catch (Exception eiiii) {

                        }
                    }
                  //throw new Exception("Global::settaGlobal() errore in load\n"+eii.getMessage()) ;
                 }
           }
           
           //Parametri di connessione al db  da definire obbligatoriamente nel .properties           
           INIT_DB_TYPE                = properties.getProperty("INIT_DB_TYPE");
           if (INIT_DB_TYPE==null) INIT_DB_TYPE="2";
           
           INIT_DB_ALIAS               = properties.getProperty("INIT_DB_ALIAS");
           if (INIT_DB_ALIAS==null) INIT_DB_ALIAS="GD4";
           
           INIT_DB_HOST                = properties.getProperty("INIT_DB_HOST");
           
           if (INIT_DB_HOST==null) INIT_DB_HOST="sothix";
           
           INIT_DB_PORT                = properties.getProperty("INIT_DB_PORT");
           if (INIT_DB_PORT==null) INIT_DB_PORT="1521";
           
           INIT_DB_SID                 = properties.getProperty("INIT_DB_SID");
           if (INIT_DB_SID==null) INIT_DB_SID="orcl";
           
           INIT_DB_USER                = properties.getProperty("INIT_DB_USER");
           if (INIT_DB_USER==null) INIT_DB_USER="GDM";
           
           INIT_DB_PASSWD              = properties.getProperty("INIT_DB_PASSWD");
           if (INIT_DB_PASSWD==null) INIT_DB_PASSWD="GDM";


           INIT_JNDI              = properties.getProperty("INIT_JNDI");
           if (INIT_JNDI==null) INIT_JNDI="jdbc/gdm";
           
           CONNECTION=null;
           
           //Oracle cluster
           INIT_DSN                    = properties.getProperty("INIT_DSN");

           if (INIT_DSN	== null)   
              URL_ORACLE    = "jdbc:oracle:thin:@"+INIT_DB_HOST+":"+INIT_DB_PORT+":"+INIT_DB_SID;
           else
              URL_ORACLE = INIT_DSN;
           //URL_ORACLE    = "jdbc:oracle:oci8:@PROT_CLUSTER";
           
           WEB_SERVER_TYPE             = properties.getProperty("WEB_SERVER_TYPE");
           if (WEB_SERVER_TYPE == null)	WEB_SERVER_TYPE		= "W"; 
           
           MANAGE_TYPE_FILE                   = properties.getProperty("MANAGE_TYPE_FILE"); 
           if (MANAGE_TYPE_FILE == null)	MANAGE_TYPE_FILE		= "1"; //1-Blob 2-BFile 3-Misto
           
           if (MANAGE_TYPE_FILE.compareTo("1") != 0){             
              DB_SERVER_TYPE              = properties.getProperty("DB_SERVER_TYPE");
              WEB_ROOT_REPOSITORY         = properties.getProperty("WEB_ROOT_REPOSITORY");
              DB_ROOT_REPOSITORY          = properties.getProperty("DB_ROOT_REPOSITORY");
           }
           
           //USE_INTERMEDIA         = properties.getProperty("USE_INTERMEDIA");
           /*if (USE_INTERMEDIA == null)*/ USE_INTERMEDIA	= "S";//"N";  //S - N
           
           REBUILD_IMMEDIATE="S";
           
           PACKAGE                     = properties.getProperty("PACKAGE"); 
           if (PACKAGE == null)        PACKAGE = "it.finmatica.dmServer"; 
           
           /*FINMATICA_DM                = properties.getProperty("FINMATICA_DM"); 
           if (FINMATICA_DM	== null)   FINMATICA_DM = "GD4";    */         
           
           DM                          = properties.getProperty("DM"); 
           if (DM == null)             DM = FINMATICA_DM;       
           
           DOCUMENTO                   = properties.getProperty("DOCUMENTO"); 
           if (DOCUMENTO == null)   DOCUMENTO = "Documento"; 
           
           LIBRERIA                    = properties.getProperty("LIBRERIA"); 
           if (LIBRERIA == null)       LIBRERIA = "Libreria"; 
           
           TIPODOC                     = properties.getProperty("TIPODOC"); 
           if (TIPODOC == null)        TIPODOC = "Tipo_Documento"; 
           
           STATIDOC                    = properties.getProperty("STATIDOC"); 
           if (STATIDOC == null)       STATIDOC = "Status_Documento"; 
           
           VALORI                      = properties.getProperty("VALORI"); 
           if (VALORI == null) VALORI = "Valori"; 
           
           OGGETTI_FILE                = properties.getProperty("OGGETTI_FILE"); 
           if (OGGETTI_FILE == null)  OGGETTI_FILE = "Oggetti_File"; 
           
           CAMPI_DOCUMENTO             = properties.getProperty("CAMPI_DOCUMENTO"); 
           if (CAMPI_DOCUMENTO == null) CAMPI_DOCUMENTO = "Campi_Documento"; 
           
           CLASSIFICAZIONI             = properties.getProperty("CLASSIFICAZIONI");
           if (CLASSIFICAZIONI == null) CLASSIFICAZIONI = "CLASSIFICAZIONI"; 
           
           DATI                        = properties.getProperty("DATI");
           if (DATI == null)            DATI = "Dati_Modello"; 
           
           CRITERIO                    = properties.getProperty("CRITERIO");
           if (CRITERIO == null)       CRITERIO = "Criterio"; 
           
           CONDIZIONI                  = properties.getProperty("CONDIZIONI");
           if (CONDIZIONI == null)  CONDIZIONI = "Condizioni"; 
           
           DOMINIO_RICERCA_VALORI      = properties.getProperty("DOMINIO_RICERCA_VALORI");
           if (DOMINIO_RICERCA_VALORI  == null)  DOMINIO_RICERCA_VALORI = "Valori"; 
           
           DOMINIO_RICERCA_OGGETTIFILE = properties.getProperty("DOMINIO_RICERCA_OGGETTIFILE");
           if (DOMINIO_RICERCA_OGGETTIFILE == null)  DOMINIO_RICERCA_OGGETTIFILE = "Oggetti_File"; 
                   
           DIR_CLI_TEMP                = properties.getProperty("DIR_CLI_TEMP");
           if (DIR_CLI_TEMP == null)   DIR_CLI_TEMP = "C:\\WINDOWS\\Temp"; 
           
           EXPLORER_DIR                = properties.getProperty("EXPLORER_DIR");
           if (EXPLORER_DIR == null)   EXPLORER_DIR = "C:\\Programmi\\Internet Explorer\\IEXPLORE.EXE";  
           
           WEB_MODULISTICA_HOST        = properties.getProperty("WEB_MODULISTICA_HOST");
           
           USE_MAPPING                 = properties.getProperty("USE_MAPPING"); 
           if (USE_MAPPING == null)    USE_MAPPING = "N"; //= S no - X xml

           MSYSTEM_PATH                 = properties.getProperty("MSYSTEM_PATH"); 
           if (MSYSTEM_PATH == null)    MSYSTEM_PATH = "@"; //= S no - X xml
           
           MAPPING_PATH                 = properties.getProperty("MAPPING_PATH"); 
           if (MAPPING_PATH == null)    MAPPING_PATH = "@"; //= S no - X xml
//********************************************* DOCAREA - PANTAREI***************************************************//
           //HUMMINGBIRD_DM                            = properties.getProperty("HUMMINGBIRD_DM");
           MAPPING_TIPO_DOC_PANTAREI                 = properties.getProperty("MAPPING_TIPO_DOC_PANTAREI");
           if (MAPPING_TIPO_DOC_PANTAREI 	== null)   WEB_HOST_SERVICE_PANTA = "def_proto_pantarei"; 
           WEB_HOST_SERVICE_PANTA                    = properties.getProperty("WEB_HOST_SERVICE_PANTA");
           MAPPING_LIBRARY                           = properties.getProperty("MAPPING_LIBRARY");
           if (MAPPING_LIBRARY 			      == null)  MAPPING_LIBRARY = "PANTAREI"; 
           DIR_XML_MIME                              = properties.getProperty("DIR_XML_MIME"); 
           if (DIR_XML_MIME 			      == null)  DIR_XML_MIME = "";            
//********************************************************************************************************************//  
           APPLICATIVO                              = properties.getProperty("APPLICATIVO"); 
           if (APPLICATIVO 			      == null)  APPLICATIVO = "jgdm";               
        /*
           MAPPING_ENTE	               = properties.getProperty("MAPPING_ENTE");	
           MAPPING_APPL	               = properties.getProperty("MAPPING_APPL");
           MAPPING_DB_TYPE	           = properties.getProperty("MAPPING_DB_TYPE");	
           MAPPING_DB_ALIAS	           = properties.getProperty("MAPPING_DB_ALIAS");
           MAPPING_DB_HOST	           = properties.getProperty("MAPPING_DB_HOST");
           MAPPING_DB_SID	             = properties.getProperty("MAPPING_DB_SID");
           MAPPING_DB_USER 	           = properties.getProperty("MAPPING_DB_USER");
           MAPPING_DB_PASSWD	         = properties.getProperty("MAPPING_DB_PASSWD");
         */
     
  }
  
  public void initVar() throws Exception {       
           //Parametri di connessione al db  da definire obbligatoriamente nel .properties           
           INIT_DB_TYPE="2";
           INIT_DB_ALIAS="GD4";
           INIT_DB_HOST="sothix";
           INIT_DB_PORT="1521";
           INIT_DB_SID="orcl";
           INIT_DB_USER="GDM";
           INIT_DB_PASSWD="GDM";
           
           CONNECTION=null;
           WEB_SERVER_TYPE		= "W"; 
           MANAGE_TYPE_FILE		= "1"; //1-Blob 2-BFile 3-Misto
           USE_INTERMEDIA	= "S";
           REBUILD_IMMEDIATE="S";
           PACKAGE = "it.finmatica.dmServer"; 
           DM = FINMATICA_DM; 
           DOCUMENTO = "Documento";
           LIBRERIA = "Libreria"; 
           TIPODOC = "Tipo_Documento";
           STATIDOC = "Status_Documento";
           VALORI = "Valori"; 
           OGGETTI_FILE = "Oggetti_File";
           CAMPI_DOCUMENTO = "Campi_Documento";
           CLASSIFICAZIONI = "CLASSIFICAZIONI";
           DATI = "Dati_Modello";
           CRITERIO = "Criterio"; 
           CONDIZIONI = "Condizioni"; 
           DOMINIO_RICERCA_VALORI = "Valori";
           DOMINIO_RICERCA_OGGETTIFILE = "Oggetti_File";
           DIR_CLI_TEMP = "C:\\WINDOWS\\Temp";
           EXPLORER_DIR = "C:\\Programmi\\Internet Explorer\\IEXPLORE.EXE"; 
           USE_MAPPING = "N"; //= S no - X xml
           MSYSTEM_PATH = "@"; //= S no - X xml
           MAPPING_PATH = "@"; //= S no - X xml
           WEB_HOST_SERVICE_PANTA = "def_proto_pantarei";
           MAPPING_LIBRARY = "PANTAREI"; 
           DIR_XML_MIME = "";            

  }  

  /*
   * METHOD:      load(Properties, String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica le variabili globali 
   *              a partire dal file properties
   *              e il file path 
   *                    
   * RETURN:      void
  */
  public void load(Properties properties, String filePath) throws Exception {        
          File file = new File(filePath);
  
          FileInputStream fis;
          try {
            fis = new FileInputStream(file) ;
          } catch (IOException e) {
            fis = null;
          }

          try {
            properties.load(fis);
          } 
          catch (Exception e) {
            throw new Exception("Global::load() Attenzione! Impossibile caricare in memoria le impostazioni utente.\nFile path: " +filePath);
          }
  }
  
  /*
   * METHOD:      adjustsPath(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Sistema il path 
   *                    
   * RETURN:      void
  */
  public synchronized static String adjustsPath(String sType, String sPath) throws Exception
  {
     try {
        if (sType.equals("U")) {            
            return replaceAll(sPath,"\\","/");
        }
        else
            return replaceAll(sPath,"/","\\\\");
     }    
     catch (Exception e) {            
           throw new Exception("Global::adjustsPath\n" + e.getMessage());
     }

  }

  /*
   * METHOD:      reverseSlash(Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Converte il simbolo "slash" 
   *                    
   * RETURN:      void
  */
  public synchronized static String reverseSlash(Object o)   {
         char[] a =  o.toString().toCharArray();
         String out = "";

         for(int i=0;i<a.length;i++)
            if (a[i]=='\\')
                out += "/";
            else
                out += a[i];

        return out;
  }

  /*
   * METHOD:      lastTrim(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Estrapola la stringa
   *                    
   * RETURN:      void
  */
   public synchronized static String lastTrim(String s, String cTrim, String WEB_SERVER_TYPE) 
   {
        String sSep;
        String[] sSpezzato;
        if (cTrim == null) return s;
        if  (cTrim.equals("\\\\") || cTrim.equals("/"))
        {
           if (WEB_SERVER_TYPE.equals("W"))
              sSep = "\\\\";
          else
              sSep = "/";
        }
        else 
          /*if( cTrim.equals(".") )
             sSep = "\\.";
          else*/
              sSep = cTrim;

        //sSpezzato=s.split(sSep);
        sSpezzato=Split(s,sSep);
        return sSpezzato[sSpezzato.length-1];
   }
   
  /*
   * METHOD:      getBytesToEndOfStream(InputStream)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce un array di bytes dato inputstream
   *                    
   * RETURN:      byte[] 
  */
  public synchronized static byte[] getBytesToEndOfStream(InputStream in) throws Exception
   { 
      try { 
          int  chunkSize = 2048; 
          ByteArrayOutputStream byteStream = new ByteArrayOutputStream(chunkSize); 
          int val; 
          
          while ((val=in.read()) != -1) 
              byteStream.write(val); 

          return byteStream.toByteArray(); 
      } 
      catch (Exception e) { 
          throw new Exception("ManageDocumento::getBytesToEndOfStream(@) "+e.getMessage());
      } 
 
   } 

 /*
   * METHOD:      replaceAll(final String,
   *                                    final String
   *                                    final String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Sostituisce tutti i caratteri aOldpattern
   *              presenti nella stringa con il nuovo carattere
   *              aInput
   *                    
   * RETURN:      String 
  */
  public synchronized static String replaceAll( final String aInput,
                                    final String aOldPattern,
                                    final String aNewPattern)
  {
     final StringBuffer result = new StringBuffer();
     //startIdx and idxOld delimit various chunks of aInput; these
     //chunks always end where aOldPattern begins
     int startIdx = 0;
     int idxOld = 0;
     while ((idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0) {
       //grab a part of aInput which does not include aOldPattern
       result.append( aInput.substring(startIdx, idxOld) );
       //add aNewPattern to take place of aOldPattern
       result.append( aNewPattern );

       //reset the startIdx to just after the current match, to see
       //if there are any further matches
       startIdx = idxOld + aOldPattern.length();
     }
     //the final chunk will go to the end of aInput
     result.append( aInput.substring(startIdx) );
     return result.toString();
   }

  /*
   * METHOD:      Split(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Split() definito nelle API java
   *                    
   * RETURN:      String[] 
  */
  public synchronized static String[] Split(String splitStr, String delimiter)
  {
   /** Note, delimiter can't be regex-type of argument as 
     * in orginal J2SE implementation!!!*/ 
    int dLen = delimiter.length();
    int p1 = 0;
    int cnt = 0;
  
    if (splitStr.length() == 0){ 
      String[] excepStr = new String[1];
      excepStr[0] = "";
      return excepStr;
    }
  
    if (dLen == 0){ 
      String[] excepStr = new String[splitStr.length()+1];
      excepStr[0] = "";
      for (int i = 0; i<excepStr.length-1; i++){
          excepStr[i+1] = String.valueOf(splitStr.charAt(i)); 
      }
      return excepStr;
    }
  
    p1 = splitStr.indexOf(delimiter, p1);
    while (p1 != -1){
          cnt++;
          p1 = p1 + dLen;
          p1 = splitStr.indexOf(delimiter, p1);
    }
  
    String[] tmp = new String[cnt + 1];
    p1 = 0;
    int p2 = 0;
    for (int i = 0; i<tmp.length; i++){
        p2 = splitStr.indexOf(delimiter, p2);
        if (p2 == -1){
          tmp[i] = splitStr.substring(p1);
        }else{
          tmp[i] = splitStr.substring(p1, p2);
        }
        p1 = p2 + dLen;
        p2 = p2 + dLen;
    }
    cnt = 0;
  
    for (int i = tmp.length-1; i>-1; i--){
        if(tmp[i].length() > 0){
        break;
        } else{
        cnt++; 
        }
    }
    String[] result = new String[tmp.length-cnt];
    for (int i = 0; i<result.length; i++){
        result[i] = tmp[i];
    }
    return result; 
  } 

  /*
   * METHOD:      isNumeric(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica se è numero
   *                    
   * RETURN:      boolean 
  */
  public synchronized static boolean isNumeric(String num1)
  {
    int len1 = num1.length();
    for (int i = 0; i < len1; i++) {
        if ( ! Character.isDigit(num1.charAt(i)) )
           return false;     
    }
    return true;
  } 

  /*
   * METHOD:      replace(String, String, String, boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: 
   *                    
   * RETURN:      boolean 
  */
  public synchronized static String replace(String str, String o, String n, boolean all) { 
    if (str == null || o == null || o.length() == 0 || n == null) 
        throw new IllegalArgumentException("null or empty String"); 
    StringBuffer result = null; 
    int oldpos = 0; 
    do { 
        int pos = str.indexOf(o, oldpos); 
        if (pos < 0) 
            break; 
        if (result == null) 
            result = new StringBuffer(); 
        result.append(str.substring(oldpos, pos)); 
        result.append(n); 
        pos += o.length(); 
        oldpos = pos; 
    } while (all); 
    if (oldpos == 0) { 
        return str; 
    } else { 
        result.append(str.substring(oldpos)); 
        return new String(result); 
    } 
  } 
  
  public synchronized static String leggiClob(IDbOperationSQL dbOp, String colonna)  throws Exception {
		  Class dbOpClass = dbOp.getClass();
		  Class[] cArg = new Class[1];
	      cArg[0] = String.class;
	
	      java.lang.reflect.Method readClobStrMethod = null;
	      try{
	    	readClobStrMethod= dbOpClass.getDeclaredMethod("readClobStr",cArg);
	      }
		  catch(NoSuchMethodException e) {
			  return Global.leggiClobInterna(dbOp,colonna);
		  }
		  catch(Exception e) {
			  throw new Exception("Global::leggiClob()\n" + e.getMessage());     
		  }
		  		  
		  String sRet="";
		  try {
			  sRet=(String)readClobStrMethod.invoke(dbOp,colonna);			  
		  }
		  catch(InvocationTargetException e) {
			  throw new Exception("Global::leggiClob() - Errore in lancio readClobStr: \n" + e.getCause());  
		  }
		  catch(Exception e) {				  
			  throw new Exception("Global::leggiClob() - Errore generico: \n" + e.getCause());  
		  }
		  return  sRet;
  }
      

 /*
   * METHOD:      leggiClob(DbOperationSQL, String sStm)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: dbOpSQL -> CONNECTION
   *              sStm    -> STATEMENT
   *              
   *               
   * 
   * RETURN:      String
  */     
   public synchronized static String leggiClobInterna(IDbOperationSQL dbOp, String colonna)  throws Exception 
    {
          BufferedInputStream bis;
          try 
          {
             try
             {
                bis = dbOp.readClob(colonna);
             }
             catch (java.lang.NullPointerException e)  {                    
                    return  "";              
             }             
            
            try {
              StringBuffer sb = new StringBuffer();
              int ic; 
              while ((ic =  bis.read()) != -1) {
                        //sb.append((char)ic);            	  
                        sb.append((char)ic);
              }
              return sb.toString();
                            
            }
            catch (Exception e) 
            {
              throw new Exception("Global::leggiClobInterna() - Costruzione String Buffer\n" + e.getMessage());    
            }
        	 
        	  
          }          	  
         catch (Exception e)  {         
            throw new Exception("Global::leggiClobInterna()\n" + e.getMessage());              
         }     
    }

  /*
   * METHOD:      selezioneValore(String, String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: str     -> STATEMENT
   *              numero  -> 
   *              sData   ->
   *              
   *              Restituisce la stringa contenente il
   *              valore significativo
   * 
   * RETURN:      String
  */    
  public synchronized static String  selezioneValore(String str,
                                        String numero,
                                        String sData) 
  {
          String sReturn="";
          if (numero==null) {
             if (sData==null) 
                 sReturn=str;
             else
                 sReturn=sData;
          }
          else             
             return sReturn + numero;
          return sReturn;
  } 
  
  public synchronized static String replaceSpecialChrFile(String sNomeFile) {
	     for (int i=0;i<specialChrFile.length;i++) sNomeFile=sNomeFile.replaceAll(specialChrFile[i], ""); 
	     
	     return sNomeFile;	    	  
  }
  
  public synchronized static String nvl(String campo, String valore) {
      if (campo==null) return valore;
      
      return campo;
  }
  
  public synchronized static String stripNonValidXMLCharacters(String in) {
      StringBuffer out = new StringBuffer(); // Used to hold the output.
      char current; // Used to reference the current character.

      if (in == null || ("".equals(in))) return ""; // vacancy test.
      for (int i = 0; i < in.length(); i++) {
          current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
          if ((current == 0x9) ||
              (current == 0xA) ||
              (current == 0xD) ||
              ((current >= 0x20) && (current <= 0xD7FF)) ||
              ((current >= 0xE000) && (current <= 0xFFFD)) ||
              ((current >= 0x10000) && (current <= 0x10FFFF)))
              out.append(current);
      }
      return out.toString();

  }  
  
  public synchronized static String getMimeTypeFile(String fileName) throws java.io.IOException {
	     String type;
	     
	     if (fileName.toUpperCase().endsWith(".P7M"))
	    	 return "application/pkcs7-mime";
	     
	     try {
	      FileNameMap fileNameMap = URLConnection.getFileNameMap();
	      
	      fileName=fileName.replaceAll(".RTFHIDDEN",".RTF");
	      
	      type = fileNameMap.getContentTypeFor(fileName);
	      
	      
	      
	      return type;
	     }
	     catch(Exception e){type="";}
	  	 	  	
	  	 return type;
  }
  
  public static boolean isThisDateValid(String dateToValidate, String dateFromat){
	  
		if(dateToValidate == null){
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);

		try {
			
			 sdf.parse(dateToValidate);

		} catch (ParseException e) {			
			return false;
		}

		return true;
	}

    public synchronized byte[] hashCodeAllegato(String algoritmo , InputStream is) throws Exception {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algoritmo);
        } catch (Exception e) {
            throw new Exception("Global::hashCodeAllegato\n"+e.getMessage());
        }
        try {
            md.reset();

            DigestInputStream in = new DigestInputStream(is, md);

            while ((in.read()) != -1);

        } catch (Exception e) {
            throw new Exception("Global::hashCodeAllegato\n"+e.getMessage());
        }

        return md.digest();
    }

    public boolean isUsaIniPerTest() {
        return usaIniPerTest;
    }

    public void setUsaIniPerTest(boolean usaIniPerTest) {
        this.usaIniPerTest = usaIniPerTest;
    }
}
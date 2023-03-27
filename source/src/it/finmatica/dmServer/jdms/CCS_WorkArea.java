package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.dmServer.monoRecord.MonoRecordIQuery;
import it.finmatica.dmServer.motoreRicerca.GD4_Gestione_Query;
import it.finmatica.dmServer.util.CrypUtility;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.ElapsedTime;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * Gestione della WorkArea.
 * Classe di servizio per la gestione del Client
*/
     
public class CCS_WorkArea 
{
   /**  
    * Costante per immaggini 
   */ 
   private static String _PATHIMG             ="./images/standard/action/";
   private static String _PATHIMG_BOTTONIFUNZ ="./images/BottoniFunzione/";
   private static String _PATHIMG_THEMES_AFC  ="../Themes/AFC/";
   
   private static String _VUOTO               =_PATHIMG+"vuota.png";
   private static String _QRYSEARCH           =_PATHIMG+"search.png";
   private static String _CARTELLAGDC         =_PATHIMG+"folder.png";
   private static String _COLL_CARTELLAGDC    =_PATHIMG+"collfolder.gif";
 
   private static String _EDITFOLDER          =_PATHIMG+"editFolder.png";  
   private static String _PROPRIETAFOLDER     =_PATHIMG+"Proprieta.png";  
   private static String _COMP                =_PATHIMG+"comp.png";   
   private static String _ANNULLA             =_PATHIMG+"delete.png";   
   private static String _COMPVIEW            =_PATHIMG+"comp_view.png";   
   private static String _EDITQRY             =_PATHIMG+"edit.png"; 
   private static String _LINKESTERNO         =_PATHIMG+"modificalinkesterno.png";  
  
   private static String _CHECK_R             =_PATHIMG+"lock_rosso.png";  
   private static String _CHECK_V             =_PATHIMG+"lock_verde.png";  
   private static String _EDIT                =_PATHIMG+"edit.png";   
   private static String _LIST                =_PATHIMG+"folder_into.gif";
   private static String _WORKFLOW            =_PATHIMG+"workflow.png";
   private static String _DOCUMENTATTACHMENT  =_PATHIMG+"attach.png";  
   private static String _ALLEGATI            =_PATHIMG+"document.png";  
   private static String _COMPLETO            =_PATHIMG+"Completo.png";  
   private static String _ANNULLATO           =_PATHIMG+"Annullato.png";   
   private static String _DOT                 =_PATHIMG+"document.png";   
   private static String _IMPR                =_PATHIMG+"fingerprint.png";
   private static String _NUMRECORD			  =_PATHIMG+"summary.png";
   private static String _SETROWS			  =_PATHIMG+"grid.png";
   private static String _SETVIEW			  =_PATHIMG+"filter_box.png";	
   private static String _LOCK			  	  =_PATHIMG+"lock.png";	
   private static String _UNLOCK			  =_PATHIMG+"unlock.png";
   
   /** Pulsanti disabilitati */
   private static String _LISTDS                =_PATHIMG+"folder_into_ds.gif"; 
   private static String _ANNULLADS             =_PATHIMG+"annulla_ds.png";
   private static String _EDITDS                =_PATHIMG+"edit_ds.png";       
   
   private static String _VUOTO_THEMES        =_PATHIMG_THEMES_AFC+"Vuoto.gif";
   private static String _FIRST_THEMES        =_PATHIMG_THEMES_AFC+"FirstOn.gif";
   private static String _PREV_THEMES         =_PATHIMG_THEMES_AFC+"PrevOn.gif";
   private static String _NEXT_THEMES         =_PATHIMG_THEMES_AFC+"NextOn.gif";
   private static String _LAST_THEMES         =_PATHIMG_THEMES_AFC+"LastOn.gif";
  
   /** Costanti per indicatori di conservazione/archiviazione di un Documento */
   private static String _WORKING         		="WORKING";
   private static String _CONS_WORKING     		=_PATHIMG+"conservazione_W.png";   
   private static String _CONS_CONSERVA_LOG    	=_PATHIMG+"conservazione_C.png";   
      
   /**
    * Costante per wrksp di default
   */
   private final static String NUMRIGHE_DEFAULT="NUMRIGHE_DEFAULT";
   
   /**
    * Costante per identificare la variabile di sessione per GDM multilingua
   */
   private final static String MULTILINGUA ="MULTILINGUA";
   
   
   /**
    * Variabili private
   */	 
   private String  _IMGVUOTO;
   private String  PATHIMAGE				="";
   private String  _COMPLETO_EXT            ="CompletoGIF.gif"; //  
   private String  _ANNULLATO_EXT           ="AnnullatoGIF.gif";  // 
   private String  _DOT_EXT                 ="document.png";   
   private String[] reserveWord = {"\\","&","?","{","}",",","(",")","[","]","-",";","~","|","$","!",">","*","_"} ;
   private String escapeCaracter="\\";
   private boolean HTML=false;/** CASO DI QUERY ESTERNA PER GENERARE HTML WORKAREA -Identifica se mi trovo nel caso di generazione HTML della WorkArea */
   private String URL_PATH="";
   private int PAGE_SIZE=10;
   private boolean icona=true;
   private String url_servlet;
   private String utente;
   private IDbOperationSQL dbOp;  
   private Environment vu;  
   protected boolean isExitsRecords;
   private String dirName;   
   private int sequence=1;
   private ElapsedTime elpsTime;  
   private int SFS=100; /** indica la dimensione max del vettore di elementi da estrapolare */
   private boolean succ=false; /** indica se esiste il successivo SFS+1 */
   public  boolean bIsTimeOut=false; 
   String page;
   String Url_page;
   String idQuery;
   String idCartella;
   String tipoOggetto;
   String WhereFullText=null;
   String valoreFullText=null;
   String RicercaAllegati ="N";
   String RicercaOCR ="N";
   String RicercaFT ="N";
   String idCartAppartenenza;
   String VIEW;
   String idCollegamento;
   String NomeQuery="";
   String tabellaLD;
   String Where;
   String WhereCart; 
   String WhereQuery;
   int    num_blocchi;
   String visDataUtente;
   String ordineSEQ="";
   String OrdinaWhere="0 ordinaQuery";
   String OrdinaWhereCart="0 ordinaQuery";
   String OrdinaWhereQuery="0 ordinaQuery";
   String nomeOggettoAttuale;
   String JDMS_LINK="N";
   String linkPage;/** indica il numero di pagina corrente */
   Vector vlistID=null;
   Vector vCartObjects=null;
   Vector vlistIDDocs=null;
   Properties pHTMLRecord=null;
   ResultSet rsCartObjects=null;
   private boolean isExitsSucc=false;
   CCS_HTML h;
   CCS_Common CCS_common;   
   GD4_Gestione_Query q=null;
   HttpServletRequest req;
   Properties parametri;
   Enumeration url;   
   int nextPage=1;/** indica il numero di pagina successiva, viene inizializzato ad 1 */
   boolean submit=false;
   String listaICONE="";
   private int timeout=2;//60;
   private HashMap<String,JDMSLink> mapLink;
   String redirectApp ="";
   String listaCMF;
   Vector vListaCM=null;
   boolean bListaCMP=false;
   boolean isVectorVuoto = false;
   private HashMap<String,String> mapIconaTooltip;   
   private XSS_Encoder xss=null;   
   private String jdmsML = "";
   private String ruolo;
   private String modulo;
   private DMServer4j log;
   
   
   /**
	 * Costruttore generico vuoto.
	 */
   public CCS_WorkArea(){}
   
   /**
	 * Costruttore utilizzato per accedere alla query
	 * esternamente dal Client Documentale.
	 * 
	 */
   public CCS_WorkArea(String newidQuery,GD4_Gestione_Query newQuery,String newWhereFullText,String newRicercaAllegati,
                      String newlinkPage,String pathImage,String url_path,int page_size,
                      boolean newicona,String newurl_servlet,CCS_Common newCommon) throws Exception
   {
	      init(newCommon);
	      log.log_info("Inizio - Costruttore WorkArea a partire da una Ricerca");
	      page="1";
	      HTML=true;
	      idQuery=newidQuery;
	      idCartella=null;
	      idCartAppartenenza="0";
	      	      
	      if((newWhereFullText!=null) && (newWhereFullText!="")){
            WhereFullText=newWhereFullText;
            valoreFullText=newWhereFullText;
	      }  
	      
	      if((newRicercaAllegati!=null) && (newRicercaAllegati.equals("S")))
		   	RicercaAllegati=newRicercaAllegati; 
	     
	      xss = new XSS_Encoder(req,CCS_common);
	      RicercaOCR = verificaParametroGet("ricercaOCR",req.getParameter("ricercaOCR"));         
	      
	      if(RicercaOCR!=null && RicercaOCR.equals("S"))
	       RicercaOCR = "S";	
	      
	      RicercaFT = verificaParametroGet("ricercaFT",req.getParameter("ricercaFT"));         
	      if(RicercaFT!=null && RicercaFT.equals("S"))
	       RicercaFT = "S";	
	      
	      if(newlinkPage==null) 
	        linkPage="1";
	      else
	    	linkPage=newlinkPage; 
	      URL_PATH=url_path;
	      VIEW="T";
	      PAGE_SIZE=page_size;
	      icona=newicona;
	      url_servlet=newurl_servlet;
	      q=newQuery;
	      this.getCondWhere(); 
	      NomeQuery=q.getNomeQuery();	
	      PATHIMAGE=pathImage;
	      JDMS_LINK=retriveParametro("JDMS_LINK");
	      mapLink = new HashMap<String,JDMSLink>();   
	      mapIconaTooltip = new HashMap<String,String>();   
          elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
          //Recupero lista di CM_FIGLIO@CM_PADRE
          listaCMF=q.getListaTipiDoc();    
          if(listaCMF.equals(""))
          	listaCMF=getListaCMF();  
          
          initParametri();
   }
  
   /**
	 * Costruttore utilizzato per accedere alla Query
	 * del Client Documentale.
	 * 
	 */
   public CCS_WorkArea(HttpServletRequest newreq,String newidQuery,String newidCartella,GD4_Gestione_Query newQuery,
                      String newidCartAppartenenza,String newWhereFullText,String newRicercaAllegati,Enumeration newurl,Properties newparametri,
                      String TipoView,String newIdCollegamento,Vector v,String newlinkPage,String newdirName,String newnumrighe,CCS_Common newCommon) throws Exception
   {
	      init(newCommon);
	      log.log_info("Inizio - Costruttore WorkArea a partire da una Ricerca");
          page="1";
          req=newreq;
          
          xss = new XSS_Encoder(req,CCS_common);
          String holdPage = verificaParametroGet("holdPage",req.getParameter("holdPage"));
          String redirect = verificaParametroGet("redirect",req.getParameter("redirect"));
          
          if(holdPage!=null && holdPage.equals("S"))
           	submit=true;  
          if(redirect!=null && !redirect.equals(""))
        	redirectApp = "&redirect="+req.getParameter("redirect");  
          
          idQuery=newidQuery;
          idCartella=newidCartella;
          idCartAppartenenza=newidCartAppartenenza;
          
          if((newWhereFullText!=null) && (newWhereFullText!="")){
            WhereFullText=newWhereFullText;
            valoreFullText=newWhereFullText;
          }  
          
          if((newRicercaAllegati!=null) && (newRicercaAllegati.equals("S")))
  		    RicercaAllegati=newRicercaAllegati;
          
          RicercaOCR = verificaParametroGet("ricercaOCR",req.getParameter("ricercaOCR"));
          RicercaFT = verificaParametroGet("ricercaFT",req.getParameter("ricercaFT"));
          
	      if(RicercaOCR!=null && RicercaOCR.equals("S"))
		    RicercaOCR = "S";
	      
	      if(RicercaFT!=null && RicercaFT.equals("S"))
		    RicercaFT = "S";	
	      
          url=newurl;
          parametri=newparametri;
          
          if(idCartAppartenenza!=null && (idCartAppartenenza.equals("") || idCartAppartenenza.equals("null")))
        	  idCartAppartenenza = parametri.getProperty("idCartAppartenenza");
          
          q=newQuery;
          vlistID=v;
          VIEW=TipoView;
          dirName=newdirName;
          NomeQuery=q.getNomeQuery();
          idCollegamento=newIdCollegamento;
          PAGE_SIZE=Integer.parseInt(newnumrighe);
          
          if(newlinkPage==null) 
            linkPage="1";
          else
           linkPage=newlinkPage; 
         
          isVectorVuoto = false;
          this.getCondWhere();
          JDMS_LINK=retriveParametro("JDMS_LINK");
          mapLink = new HashMap<String,JDMSLink>();
          mapIconaTooltip = new HashMap<String,String>(); 
          elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
          
          //Recupero lista di CM_FIGLIO@CM_PADRE
          listaCMF=q.getListaCMF();  
          if(listaCMF!=null && listaCMF.equals(""))
          	listaCMF=getListaCMF(); 
          
          initParametri();
          
          getParametriSessione();
          
          log.log_info("Fine - Costruttore WorkArea a partire da una Ricerca");          
   }
   
   /**
	 * Costruttore utilizzato per accedere alla Cartella
	 * del Client Documentale.
	 * 
	 */
   public CCS_WorkArea(HttpServletRequest newreq,String newidQuery,String newidCartella,String newWhereFullText,String newRicercaAllegati,Enumeration newurl,
		   			   Properties newparametri,String TipoView,String newIdCollegamento,String newdirName,Vector v,ResultSet rs,String newnumrighe,CCS_Common newCommon) throws Exception
   {
          init(newCommon);
          log.log_info("Inizio - Costruttore WorkArea a partire da una Cartella");
          page="1";
          vCartObjects=v;
          rsCartObjects=rs;
          req=newreq;          
          
          xss = new XSS_Encoder(req,CCS_common);
          String holdPage = verificaParametroGet("holdPage",req.getParameter("holdPage"));
          String redirect = verificaParametroGet("redirect",req.getParameter("redirect"));
          
          if(holdPage!=null && holdPage.equals("S"))
           	submit=true;  
          if(redirect!=null && !redirect.equals(""))
        	redirectApp = "&redirect="+req.getParameter("redirect");  
          
          idQuery=newidQuery;
          idCartella=newidCartella;
          
          if((newRicercaAllegati!=null) && (newRicercaAllegati.equals("S")))
  		    RicercaAllegati=newRicercaAllegati;
    
          RicercaOCR = verificaParametroGet("ricercaOCR",req.getParameter("ricercaOCR"));
          RicercaFT = verificaParametroGet("ricercaFT",req.getParameter("ricercaFT"));
          
	      if(RicercaOCR!=null && RicercaOCR.equals("S"))
		    RicercaOCR = "S";
	      
	      if(RicercaFT!=null && RicercaFT.equals("S"))
		    RicercaFT = "S";	
         
          if((newWhereFullText!=null) && (newWhereFullText!="")){
            setWhereFullText(newWhereFullText);
            valoreFullText=newWhereFullText;
          }
            
          url=newurl;
          parametri=newparametri;
          VIEW=TipoView;
          dirName=newdirName;
          idCollegamento=newIdCollegamento;
          PAGE_SIZE=Integer.parseInt(newnumrighe);
          JDMS_LINK=retriveParametro("JDMS_LINK");
          mapLink = new HashMap<String,JDMSLink>();
          mapIconaTooltip = new HashMap<String,String>(); 
          elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
          
          initParametri();
          
          getParametriSessione();
          
          log.log_info("Fine - Costruttore WorkArea a partire da una Cartella");
   }
    
   /**
	 * Costruttore utilizzato per accedere alla Cartella o Query da FLEX
	 * 
	 */
   public CCS_WorkArea(HttpServletRequest newreq,String newidQuery,String newidCartella,String user,CCS_Common newCommon) throws Exception
   {
         init(newCommon);
         log= new DMServer4j(CCS_WorkArea.class,CCS_common); 
         log.log_info("Inizio - Costruttore WorkArea a partire da FLEX");
         idQuery=newidQuery;
         idCartella=newidCartella;
         req=newreq;  
         
         xss = new XSS_Encoder(req,CCS_common);
         redirectApp = verificaParametroGet("redirect",req.getParameter("redirect"));
         
         if(redirectApp!=null && !redirectApp.equals(""))
          redirectApp = "&redirect="+redirectApp;

         utente=user;
	     h = new CCS_HTML();
	     	     
         elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
         
         initParametri();
         
         getParametriSessione();
         
	     log.log_info("Fine - Costruttore WorkArea a partire da FLEX");
   }
   
   	/**
	 * Costruttore utilizzato dal metodo getCompetenzeScritturaCartella
	 * per controllare le competenze di scrittura sulla Cartella per 
	 * abilitare/disabilitare i pulsanti della WorkArea.
	 * 
	 */
   	public CCS_WorkArea(String newidCartella,CCS_Common newCommon) throws Exception
   	{
	      init(newCommon);
	      idCartella=newidCartella;
          elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
   	}
   
   	/**
	 * Costruttore utilizzato dal metodo getCompetenzeScritturaCartella
	 * per controllare le competenze di scrittura sulla Cartella per 
	 * abilitare/disabilitare i pulsanti della WorkArea.
	 * 
	 */
   	public CCS_WorkArea(String nruolo,String nmodulo,CCS_Common newCommon) throws Exception
   	{
	     init(newCommon);
	     ruolo= nruolo;
	     modulo=nmodulo;
         elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
   	}   
   
   	/**
	* Costruttore utilizzato per la gestione di Stampa PDF.
	*/
   	public CCS_WorkArea(String idOggetto,String newtipoOggetto,String newWhereFullText,GD4_Gestione_Query newQuery,String TipoView,String newidCartAppartenenza,String newIdCollegamento,HttpServletRequest newreq,String newvisDataUtente,CCS_Common newCommon) throws Exception
   	{
	      init(newCommon);	  
	      
	      tipoOggetto=newtipoOggetto;
	      idCollegamento=newIdCollegamento;
	      page="1";	         
	      req=newreq;
	      visDataUtente=newvisDataUtente;
	      
	      if(tipoOggetto.equals("Q"))
	      {
	    	idQuery=idOggetto;
	    	PAGE_SIZE=10;
	    	if((newWhereFullText!=null) && (newWhereFullText!="")){
	          WhereFullText=newWhereFullText;
	          valoreFullText=newWhereFullText;
	    	}  
	    	q=newQuery;
	    	NomeQuery=q.getNomeQuery();
	    	idCartAppartenenza=newidCartAppartenenza;
	    	linkPage="1";
	    	this.buildCondWhereStampa();
	      }
	      else
	      {
	    	  idCartella=idOggetto;
	    	  if((newWhereFullText!=null) && (newWhereFullText!="")){
	           setWhereFullText(newWhereFullText);
	           valoreFullText=newWhereFullText;
	    	  } 
	    	  
	    	  if(TipoView==null || ((TipoView!=null) && (!TipoView.equals(""))))
	    		VIEW="T";  
	    	  else
	    	    VIEW=TipoView;
	      }
	      
	      initParametri();
	      
	      getParametriSessione();
	      
	      elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
   	}
   
   	/**
	 * Costruttore utilizzato per costruire strutture dati in sessione
	 * 
	 */
   	public CCS_WorkArea(HttpServletRequest newreq,CCS_Common newCommon) throws Exception
   	{
        init(newCommon);
        log= new DMServer4j(CCS_WorkArea.class,CCS_common); 
        log.log_info("Inizio - Costruttore WorkArea");
        req=newreq;  
        utente=CCS_common.getUser();
	    
	    initParametri();
	    
	    getParametriSessione();
	    
	    elpsTime = new ElapsedTime("CCS_WORKAREA",vu);
	    log.log_info("Fine - Costruttore WorkArea");
	}
   
   
   /**
    * Inizializzazione di alcuni parametri
    * per la gestione della WorkArea.
    * 
    * @param newCommon    variabile di connessione
   */	   
   private void init(CCS_Common newCommon)  throws Exception
   {
	       h = new CCS_HTML();
           CCS_common=newCommon;
           pHTMLRecord =new Properties();
           vlistIDDocs=new Vector();
           log= new DMServer4j(CCS_WorkArea.class,CCS_common); 
	       utente=CCS_common.user;
	       _IMGVUOTO=h.getImgVUOTO(_VUOTO); 
           if (!CCS_common.dataSource.equals("")) {
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
            vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
           }
           else 
           {
            vu=CCS_common.ev;
            dbOp=CCS_common.ev.getDbOp();
           }           
   }

  
   /***************************************************************************
	* METODI PUBBLICI
	**************************************************************************/
   
   /**
     * Restituisce la lista degli oggetti estrapolati 
     * dalla ricerca e mantenuti in sessione.
     * 
     * @return Vector vettori di identificativi oggetti 
     * 
	 */
   public Vector getvlistID() 
   {
          return vlistID;
   }
   
   /**
    * Restituisce la lista degli oggetti estrapolati 
    * dalla ricerca e mantenuti in sessione.
    * 
    * @return Vector vettori di identificativi oggetti 
    * 
	 */
   public ResultSet getrsCartObjects()
  {
         return rsCartObjects;
  }
  
  /**
   * Restituisce la lista degli oggetti estrapolati 
   * dalla ricerca e mantenuti in sessione.
   * 
   * @return Vector vettori di identificativi oggetti 
   * 
	 */
   public Vector getvCartObjects()
 {
        return vCartObjects;
 }
   
   /**
    * Costruzione del path folder.
    * 
    * @return String path folder  
 * @throws Exception 
    * 
	*/
   public String _getNomeOggettoAttuale() throws Exception
   {
	   String disabilitaC = verificaParametroGet("disabilitaC",req.getParameter("disabilitaC"));  
	   if(disabilitaC!=null && disabilitaC.equals("S"))
	    return "";
	   else 		
	    return nomeOggettoAttuale;
   }
  
   /**
    * Costruzione della pagina nel caso in cui 
    * si è verificato il timeout dell'esecuzione
    * di una Ricerca.
    * 
    * @return String HTML tabella di visualizzazione
    * tempo scaduto e ritorno al link precedente.   
    * 
	*/
   public String getTableTM() throws Exception
   {
	   	  String url;
	   	  String idCartAppartenenza = verificaParametroGet("idCartAppartenenza",req.getParameter("idCartAppartenenza")); 
	   	  String gdc_link="../common/WorkArea.do?idQuery="+idQuery+"&="+idCartAppartenenza+"&tipoUso=R";
	   	  String tipoRicerca = verificaParametroGet("tipoUso",req.getParameter("tipoUso"));  
	      String campo_hidden="";
 	      
		  if((tipoRicerca!=null) && (tipoRicerca.equals("R")))
		  { 	  
		 		String s=getParametriRicercaMod(idQuery);
			    String sarea=s.substring(s.indexOf("_")+1,s.indexOf("@"));
		 		String scm=s.substring(s.indexOf("@")+1,s.length());
		 		String parametri="idQuery="+idQuery+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+idCartAppartenenza+"&idCollegamento="+idCollegamento+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
		 		url="../restrict/ServletRicercaModulistica.do?"+parametri;
		 		campo_hidden="<input id=\"srcRicercaMod\" type=\"hidden\" name=\"srcRicercaMod\" value=\""+url+"\" style=\"WIDTH: 46px; HEIGHT: 19px\" size=\"4\">";
		  }
		  else
			campo_hidden="<input id=\"srcRicercaMod\" type=\"hidden\" name=\"srcRicercaMod\" value=\"0\" style=\"WIDTH: 46px; HEIGHT: 19px\" size=\"4\">";
 	      
		  StringBuffer tableTM = new StringBuffer();
		  tableTM.append("<table class=\"AFCFormTABLE\" border=\"1\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\" width=\"40%\">");
		  tableTM.append("<tr height=\"50\"><td class=\"AFCDataTD\" nowrap>");
		  tableTM.append("<div align=\"center\"><b><font color=\"#FF0000\" size=\"5\">&nbsp;Tempo max di Ricerca raggiunto!</font>&nbsp;&nbsp;&nbsp;</b></div>");
		  tableTM.append("<div align=\"center\"><font class=\"AFCTextHighlight\">&nbsp;Vuoi continuare ad eseguire la Query?&nbsp;&nbsp;&nbsp;</font></div></td></tr>");
		  tableTM.append("<tr height=\"30\"><td class=\"AFCFieldCaptionTD\" nowrap align=\"center\" valign=\"middle\">");
		  tableTM.append(campo_hidden);
		  tableTM.append("<input class=\"AFCButton\" title=\"Riprova Ricerca\" name=\"Link\" type=\"button\" onclick=\"setSRCWorkArea();\" value=\"Riprova Ricerca\" onmouseover=\"this.style.cursor='hand';this.style.color='#FF6600';\" onmouseout=\"this.style.color='black';\"></td></tr>");
		  tableTM.append("</table>");
 	      return tableTM.toString();     
   }  
   
   /**
    * Costruzione della lista di oggetti presenti
    * nella WorkArea per un determinato oggetto Cartella 
    * o Query.
    * 
    * @return String HTML lista di oggetti.   
    * 
	*/
   public String _afterInitialize() throws Exception
   {
          String listaOggetti=null;
          String stile="";         
          try
          {
        	/** Nel caso di query invocata esternamente */
        	if(HTML)
             listaOggetti=this.getListaOggetti();
            else
            {
             if(idQuery!=null){
               listaOggetti=this.getListaOggetti();
               
               if(isVectorVuoto){
            	 createSessionNRecords("Q#@#0");    
               }
               else {
            	   String nrecord = q.getSqlCountQuery();
                   if(nrecord!=null)
                    createSessionNRecords("Q#@#"+nrecord);  
               }              
             }  
             else {
               listaOggetti=this.getCartListaOggetti();
               createSessionNRecords("C#@#"+vCartObjects.size());
             }
             
             nomeOggettoAttuale=getNomeOggettoAttuale();
            }
        	
        	/** Gestione icone */
          	gestioneControlloIcone();
            stile = getStile();
          }
          catch (Exception e) {
        	throw e;
          }
          finally {
              _finally();
          }
          
         return stile+listaOggetti;
  }  
   
   

   /**
    * Costruzione della lista di oggetti presenti
    * nella WorkArea per un determinato oggetto Cartella 
    * o Query.
    * 
    * @return String HTML lista di oggetti.   
    * 
	*/
   public String _xmlWorkArea() throws Exception
   {
          String listaOggetti=null;
                   
          try
          {
             if(idQuery!=null)
               listaOggetti="";
             else
               listaOggetti=getXMLListaOggetti();
          }
          catch (Exception e) {
        	throw e;
          }
          finally {
              _finally();
          }
         return listaOggetti;
  }   

   /**
    * Costruzione della lista di oggetti presenti
    * nella WorkArea per un determinato oggetto Cartella 
    * o Query per la gestione di Stampa.
    * 
    * @return String XHTML lista di oggetti.   
    * 
	*/
   public String _gestioneStampa() throws Exception
   {
          String xbody="";
                    
          try
          {
            xbody=this.getListaOggettiStampa();
          }
          catch (Exception e) {
        	throw e;
          }
          finally {
              _finally();
          }
          return xbody;
   } 
      
   /**
    * Costruzione del ActionKey.
    * 
    * @return String ack 
    * 
	*/
   public String _gestioneActionKey() throws Exception
   {
          String idDoc,ACK="";
  	      vu= new Environment(CCS_common.user,CCS_common.user,"MODULISTICA","ADS",null,dbOp.getConn());
  		  
          /** RECUPERO ID DOCUMENTO(CARTELLA) DALL'ID DELLA CARTELLA */		   
          idDoc=(new DocUtil(vu)).getIdDocumentoByCr(idCartella);            
  		  if (idDoc.equals("")){return ACK;}
      
          try { 		   
  		     AccediDocumento doc = new AccediDocumento(idDoc,vu);
  		     doc.accediDocumentoValori();
  			 ACK = doc.leggiValoreCampo("$ACTIONKEY");
  		   }
          catch (Exception e) {
           throw e;
          }
          finally {
              _finally();
          }
  	      return ACK;
   }
  
   /**
    * Controlla le competenze di scrittura sulla Cartella per 
	* abilitare/disabilitare i pulsanti della WorkArea. 
	* 
    * @return String competenza uguale a (0 o 1)  
    * 
	*/
   public String _getCompetenzeScritturaCartella() throws Exception
   {
          String sql="",competenze=null;
          try {
                ResultSet rs=null;
  	            sql="SELECT ";
                sql+=GDM("VIEW_CARTELLA","F_IDVIEW_CARTELLA( :IDCARTELLA )","C");
                sql+="FROM DUAL";
         	    dbOp.setStatement(sql);
         	    dbOp.setParameter(":IDCARTELLA",idCartella);
         		dbOp.execute();
         	    rs=dbOp.getRstSet();
         	 	rs.next();
    		    competenze=rs.getLong(1)+"";
           }
           catch (Exception e) {  
        	log.log_error("CCS_WorkArea::_getCompetenzeScritturaCartella -- Controllo competenze di creazione della cartella - SQL:"+sql);
        	throw e;
           }
           finally {
              _finally();
           }
           return competenze;   
   }   
   
   
   /**
    * Controlla le competenze di creazione di documenti sulla Query per 
	* abilitare/disabilitare i pulsanti della WorkArea. 
	* 
    * @return String competenza uguale a (0 o 1)  
    * 
	*/
   public String _getCompetenzeScritturaQuery() throws Exception
   {
          String competenze=null;
          GD4_Gestione_Query q = new GD4_Gestione_Query(Integer.parseInt(idQuery),vu);
		  String area=q.getArea();
		  String listaTipiDoc=q.getListaTipiDoc();
		  String sql="";
	         
		  if(area==null)
		   area="";	 
		     
		  if(listaTipiDoc.equals("(' ')"))
		   listaTipiDoc="";	 
		  
		  try 
	      {
            sql="SELECT id_tipodoc FROM modelli "
               +" WHERE  gdm_competenza.gdm_verifica ('TIPI_DOCUMENTO',id_tipodoc,"
               +"        'C', :UTENTE ,f_trasla_ruolo ( :UTENTE ,'GDMWEB','GDMWEB'), "
               +"        TO_CHAR (SYSDATE, 'dd/mm/yyyy')) = 1 "
               +" AND tipo_uso IN ('X', 'D')"
               +" AND area = NVL ( :AREA, area) "
               +" AND (instr(:listaTipiDoc,''''||codice_modello||'''') >0"
               +" or :listaTipiDoc IS NULL )";
      	             
		     dbOp.setStatement(sql);
		     dbOp.setParameter(":UTENTE",utente);
		     dbOp.setParameter(":listaTipiDoc",listaTipiDoc);
		     dbOp.setParameter(":AREA",area);
             dbOp.execute();
             ResultSet rs = dbOp.getRstSet();
			 if(rs.next())
			   competenze=""+1;
			 else
			   competenze=""+0;
          }
          catch ( SQLException e ) {
        		log.log_error("CCS_WorkArea::_getCompetenzeScritturaQuery -- Controllo competenze di creazione documenti sulla Query - idQuery:"+idQuery+" - SQL:"+sql);
            	throw e;
          }
          finally {
              _finally();
          }
         return competenze;   
   } 
   
   /**
    * Recupera dal DB il parametro Ricerca OCR. 
	* 
    * @return S o N (default N)  
    * 
	*/
   public String getParametroRicercaOCR() throws Exception
   {
          String sql="",ocr="N";
          try {
                ResultSet rs=null;
  	            sql="select valore from parametri where codice='RICERCA_OCR' and tipo_modello='@STANDARD' ";
         	    dbOp.setStatement(sql);
         		dbOp.execute();
         	    rs=dbOp.getRstSet();
         	 	if(rs.next())
         	 	  ocr=rs.getString("valore");
           }
           catch (Exception e) {  
        	log.log_error("CCS_WorkArea::getParametroRicercaOCR -- Recupero parametro Ricerca OCR - SQL:"+sql);
        	throw e;
           }
           finally {
              _finally();
           }
           return ocr;
   }   
   
   public String getLabelRicercaOCR(String parametroOCR) throws Exception
   {
          String lb="";
          String isChecked="";
          
          String ricercaOCR = verificaParametroGet("ricercaOCR",req.getParameter("ricercaOCR"));  
          if(ricercaOCR!=null && ricercaOCR.equals("S"))
        	isChecked = "CHECKED";
                    
          if(parametroOCR!=null && parametroOCR.equals("S"))
        	lb = "<td><input id=\"cbRicercaOCR\" title=\"Ricerca OCR\" type=\"checkbox\" value=\"1\" name=\"{cbRicercaOCR_Name}\""+isChecked+">OCR</td>";//+ 
                 //"<td><font title=\"Ricerca OCR\">Ricerca OCR</font></td>";
          else
        	lb="";   
        	  
          return lb;   
   }  
   
   
   /**
    * Recupera dal DB il numero di righe della WorkArea. 
	* 
    * @return String numero di righe  
    * 
	*/
   public String getParametroRigheWorkArea() throws Exception
   {
          String sql="",num="10";
          try {
                ResultSet rs=null;
  	            sql="select valore from parametri where codice='RIGHE_WORKAREA' and tipo_modello='@DMSERVER@' ";
         	    dbOp.setStatement(sql);
         		dbOp.execute();
         	    rs=dbOp.getRstSet();
         	 	if(rs.next())
         	 	  num=rs.getString("valore");
           }
           catch (Exception e) {  
        	log.log_error("CCS_WorkArea::getParametroRigheWorkArea -- Recupero parametro numero di righe - SQL:"+sql);
        	throw e;
           }
           finally {
              _finally();
           }
           return num;
   }   
   
   /**
    * Recupera dal DB tabella REGISTRO il numero di righe della WorkArea. 
	* 
    * @return String numero di righe  
    * 
	*/
   public String getParametroRigheWorkAreaPreferenza() throws Exception
   {
	   	  StringBuffer sql=new StringBuffer("");
	      String num=null;
          
	      try {
                ResultSet rs=null;
  	            sql.append("SELECT AMVWEB.GET_PREFERENZA( :NUMRIGHE_DEFAULT , :MODULO , :UTENTE ) valore from dual");
         	    dbOp.setStatement(sql.toString());
         		dbOp.setParameter(":NUMRIGHE_DEFAULT",NUMRIGHE_DEFAULT);
         		dbOp.setParameter(":MODULO",modulo);
         		dbOp.setParameter(":UTENTE",utente);
         	    dbOp.execute();
         	    rs=dbOp.getRstSet();
         	 	if(rs.next())
         	 	 num=rs.getString(1);
           }
           catch (Exception e) {  
        	log.log_error("CCS_WorkArea::getParametroRigheWorkAreaPreferenza -- Recupero parametro numero di righe - SQL:"+sql);
        	throw e;
           }
           finally {
              _finally();
           }
           return num;   
   }   
      
   /**
    * Setta la workSpace di preferenza per l'utente.
    * 
    */	   
   public void setRigheWorkAreaPreferenza(String nr) throws Exception 
   {
	      StringBuffer sql = new StringBuffer("");
	 
	      try {
		      
	    	  sql.append(" BEGIN ");
	    	  sql.append(" AMVWEB.set_preferenza( :NUMRIGHE_DEFAULT , :NR , :MODULO , :UTENTE ); ");
	    	  sql.append(" END; ");
	    	  dbOp.setStatement(sql.toString());
	    	  dbOp.setParameter(":NUMRIGHE_DEFAULT",NUMRIGHE_DEFAULT);
	    	  dbOp.setParameter(":NR",nr);
	    	  dbOp.setParameter(":MODULO",modulo);
	    	  dbOp.setParameter(":UTENTE",utente);
		      dbOp.execute();
		      CCS_common.closeConnection(dbOp,true);
	      }		      
		  catch (Exception e) {
			  CCS_common.closeConnection(dbOp,false);		
			  log.log_error("CCS_WorkArea::getRigheWorkAreaPreferenza() - SQL:"+sql);
		      throw e;
		  }
          finally {
              if (CCS_common.dataSource.equals("")){
                  try{vu.disconnectClose();}catch(Exception ei){}
              }
          }
	}
   
   public String _getListBoxVisualizza()
   {
	       StringBuffer html =new StringBuffer("");
	       String viewD,viewR,viewT;
	       
	       if( (VIEW.equals("D")) && !(VIEW.equals("")))
	       {
	    	   viewD="<img border=\"0\" src=\"images/arrow2.gif\" width=\"14\" height=\"9\">Documenti</a><br>";
	    	   viewR="<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">Ricerche</a><br>";
	    	   viewT="<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">Tutti</a><br>";
	       }
	       else
	       {
	    	   if( (VIEW.equals("R")) && !(VIEW.equals("")))  
		       {
		    	   viewD="<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">Documenti</a><br>";
		    	   viewR="<img border=\"0\" src=\"images/arrow2.gif\" width=\"14\" height=\"9\">Ricerche</a><br>";
		    	   viewT="<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">Tutti</a><br>";
		       }
	    	   else
	    	   {
		    	   viewD="<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">Documenti</a><br>";
		    	   viewR="<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">Ricerche</a><br>";
		    	   viewT="<img border=\"0\" src=\"images/arrow2.gif\" width=\"14\" height=\"9\">Tutti</a><br>";
		       } 
	       }
	       
    	   html.append("<table cellpadding=\"1\" cellspacing=\"0\"><tr>");
    	   html.append("<td class=\"textIMGDefault\" onmouseover=\"this.className='textIMGMouseOver';apriVoce('view')\" onmouseout=\"this.className='textIMGDefault';chiudiVoce('view')\">");
    	   html.append("<a href=\"#\"><img src=\""+_SETVIEW+"\" width=\"18\" height=\"18\" border=\"0\" onload=\"fixPNG(this,'18','18')\"/></a>"); 
    	   html.append("<img src=\"images/arrow1.gif\" width=\"11\" height=\"11\"><br>");
		   html.append("<div class=\"menu\" id=\"view\">");
		   html.append("<div class=\"divmenu\"><a href=\"#\" onmouseover=\"this.className='menuMouseOver'\" onmouseout=\"this.className='menuMouseDown'\" title=\"Visualizza solo i documenti\" onclick=\"if(document.getElementById('idQuery').value==-1) {setView('D');}\" style=\"text-decoration:None;\">");
		   html.append(viewD);
		   html.append("</div><div class=\"divmenu\"><a href=\"#\" onmouseover=\"this.className='menuMouseOver'\" onmouseout=\"this.className='menuMouseDown'\" title=\"Visualizza solo le ricerche\" onclick=\"if(document.getElementById('idQuery').value==-1) {setView('R');}\" style=\"text-decoration:None;\">");
		   html.append(viewR);
		   html.append("</div><div class=\"divmenu\"><a href=\"#\" onmouseover=\"this.className='menuMouseOver'\" onmouseout=\"this.className='menuMouseDown'\" title=\"Visualizza tutti gli oggetti\" onclick=\"if(document.getElementById('idQuery').value==-1) {setView('T');}\" style=\"text-decoration:None;\" >");
		   html.append(viewT); 
		   html.append("</div></div></td></tr>");
		   html.append("</table>");       
	    	   
	       return html.toString();
   }
   
   public String _getListBoxNumRighe()
   {
	       StringBuffer html = new StringBuffer("");
	       StringBuffer view = new StringBuffer("");
	       
	       for(int i=1; i<=10 ;i++)
	       {
	    	   int num=i*10;
	    	   
	    	   view.append("<div class=\"divmenu\"><a onmouseover=\"this.className='menuMouseOver'\" title=\"Visualizza "+num+" righe\" onclick=\"setRigheView('"+num+"');\" onmouseout=\"this.className='menuMouseDown'\" href=\"#\" style=\"text-decoration:None;\">");
	    	   if(num==PAGE_SIZE)
	    		view.append("<img height=\"9\" src=\"images/arrow2.gif\" width=\"14\" border=\"0\">");
	    	   else
	    		view.append("<img border=\"0\" src=\"images/Vuoto.gif\" width=\"14\" height=\"9\">");
	    	
	    	   view.append(" "+num+" righe</a></div>");    	 
	       }
	    	   
    	   html.append("<table cellpadding=\"1\" cellspacing=\"0\">");
    	   html.append("<tr><td class=\"textIMGDefault\" onmouseover=\"this.className='textIMGMouseOver';apriVoce('numrighe')\" onmouseout=\"this.className='textIMGDefault';chiudiVoce('numrighe')\">");
    	   html.append("<a href=\"#\"><img src=\""+_SETROWS+"\"  width=\"18\" height=\"18\" border=\"0\" onload=\"fixPNG(this,'18','18')\"/></a>"); 
    	   html.append("<img src=\"images/arrow1.gif\" width=\"11\" height=\"11\"><br>");
    	   html.append("<div class=\"menu\" id=\"numrighe\">");
    	   html.append(view);
    	   html.append("</div></td></tr></table>");       
	    
    	   return html.toString();
   }
   
   
   public String _getListBoxCheckIN()
   {
	       StringBuffer html = new StringBuffer("");       
	       StringBuffer corpoJS = new StringBuffer("");
	       StringBuffer lista = new StringBuffer("");
	       
	       corpoJS.append("var ret=false;\nif(seq!='') {\n");
	       corpoJS.append(" if((seq.indexOf('C')!=-1) || (seq.indexOf('X')!=-1) || (seq.indexOf('L')!=-1) || (seq.indexOf('Q')!=-1))\n");
	       corpoJS.append(" ret=false; else ret=true; } ");
	       corpoJS.append(" if(!ret) document.getElementById('check').value=0; else document.getElementById('check').value=1; ");
	       corpoJS.append("	return ret;");   
    	   
	 	   String text="<img name=\"imgLock\" src=\""+_LOCK+"\" width=\"16\" height=\"16\">Blocco<img name=\"img\" src=\"images/arrow1.gif\" width=\"11\" height=\"11\"><input id=\"check\" type=\"hidden\" value=\"1\" name=\"check\">";
	 	   String onclick1="popup('checkDocumenti.do?lista='+document.getElementById('ListaId').value+'&livello=1&tipo=I&idQueryProveninez='+document.getElementById('idQuery').value,500,400,0,50);";
	       String onclick2="popup('checkDocumenti.do?lista='+document.getElementById('ListaId').value+'&livello=2&tipo=I&idQueryProveninez='+document.getElementById('idQuery').value,500,400,0,50);";
	 	   String onclick3="popup('checkDocumenti.do?lista='+document.getElementById('ListaId').value+'&livello=3&tipo=I&idQueryProveninez='+document.getElementById('idQuery').value,500,400,0,50);";
	   	   
    	   lista.append("<div class=\"menuListBox\" id=\"checkin\">");
		   lista.append("<div class=\"divmenu\"><a href=\"#\" onmouseover=\"this.className='menuListBoxMouseOver'\" onmouseout=\"this.className='menuListBoxMouseDown'\" title=\"Esclusivo\" onclick=\"if(linkOggettoPopup()){");
		   lista.append(onclick1);
		   lista.append("}\" style=\"text-decoration:None;\">");
		   lista.append("Esclusivo</a></div>");
		   lista.append("<div class=\"divmenu\"><a href=\"#\" onmouseover=\"this.className='menuListBoxMouseOver'\" onmouseout=\"this.className='menuListBoxMouseDown'\" title=\"Lettura\" onclick=\"if(linkOggettoPopup()){");
		   lista.append(onclick2);
		   lista.append("}\" style=\"text-decoration:None;\">");
		   lista.append("Lettura</a></div>");
		   lista.append("<div class=\"divmenu\"><a href=\"#\" onmouseover=\"this.className='menuListBoxMouseOver'\" onmouseout=\"this.className='menuListBoxMouseDown'\" title=\"Avviso\" onclick=\"if(linkOggettoPopup()){"+onclick3+"}\" style=\"text-decoration:None;\" >");
		   lista.append("Avviso</a></div>"); 
		   lista.append("</div>");
    	   
    	   html.append("<table cellpadding=\"1\" cellspacing=\"0\"><tr>");
    	   html.append("<td onmouseover=\"if(document.getElementById('check').value==1){document.getElementById('checkin').style.visibility = 'Visible';}\" onmouseout=\"document.getElementById('checkin').style.visibility = 'Hidden';\">");
    	   html.append(getListBox("200","Blocco dei documenti","",text,lista.toString(),corpoJS.toString())); 
    	   html.append(h.getInput("","","ICONA","hidden","images/arrow1.gif","ICONA")+h.getInput("","","ICONA_DS","hidden","images/Vuoto.gif","ICONA_DS"));
    	   html.append("</td></tr></table>");       
	       
	       return html.toString();
   }
   
   public String _getListBoxCheckOUT()
   {
	      StringBuffer corpoJS = new StringBuffer("");          
	      corpoJS.append("var ret=false;\nif(seq!='') {\n");
	      corpoJS.append(" if((seq.indexOf('C')!=-1) || (seq.indexOf('X')!=-1) || (seq.indexOf('L')!=-1) || (seq.indexOf('Q')!=-1))\n");
	      corpoJS.append(" ret=false; else ret=true; }	return ret;");   
	      String onclick="popup('checkDocumenti.do?lista='+document.getElementById('ListaId').value+'&livello=0&tipo=O&idQueryProveninez='+document.getElementById('idQuery').value,500,400,0,50);";
	      String text="<img name=\"imgUnlock\" src=\""+_UNLOCK+"\" width=\"16\" height=\"16\">Sblocco<img name=\"img\" src=\"images/Vuoto.gif\" width=\"11\" height=\"11\">";
	 	  String html = getListBox("100","Sblocco dei documenti",onclick,text,"",corpoJS.toString())+h.getInput("","","ICONA","hidden","images/Vuoto.gif","ICONA")+h.getInput("","","ICONA_DS","hidden","images/Vuoto.gif","ICONA_DS");
   	      return html;
   }  
		
   public String getListBox(String index,String title,String onclick,String content,String lista,String corpoJS) 
   {
          StringBuffer button=new StringBuffer();
          button.append("<button id=\"button\" name=\"button"+index+"\" ");
          if(title==null)
           title="";
	      button.append("title=\""+title+"\" type=\"button\"  class=\"textListBoxDefault\" ");
	      button.append("onMouseOver=\"this.className='textListBoxMouseOver'\" ");
	      button.append("onMouseOut=\"this.className='textListBoxDefault'\" ");
	      button.append("onMouseDown=\"this.className='textListBoxMouseDown'\" ");
	      button.append("onMouseUp=\"this.className='textListBoxMouseOver'\" "); 
	      if(!onclick.equals(""))
	       button.append("onClick=\"if(linkOggettoPopup()){"+onclick+"}\" ");
	      button.append("><NOBR>\n<div class=\"textPulsante\" style=\"width:50px;\" align=\"left\"> ");
	      button.append(content);
	      button.append("</div>\n");
	      button.append("<NOBR>\n");
	      button.append("</button>\n");
	      if(!lista.equals(""))
	       button.append(lista);
	      button.append("<script>\nfunction button"+index+"(seq,idCartella,wrksp,tipoOggetto,ruolo,CompOgg){\n"+corpoJS+"}\n</script>\n");
	      return button.toString();
   }
   
   public String _getPulsanteStampa()
   {
	       StringBuffer button = new StringBuffer("");
	       String onclick,srcImg="images/stampa.png";
	       String idOggetto,tipoOggetto;
	       String fulltext="",view="";
	       
	       //Visualizzazione del pulsante di Stampa solo per utente amministratore	   
	       if(req.getSession().getAttribute("Ruolo")!=null && req.getSession().getAttribute("Ruolo").equals("AMM"))
	       {
		       if(idQuery!=null)
		       {
		    	   idOggetto=idQuery;
		    	   tipoOggetto="Q";
		       }
		       else
		       {
		    	   idOggetto=idCartella;
		    	   tipoOggetto="C";
		       }
		       
		       if(VIEW!=null && (!VIEW.equals("")))
			     view="&view="+VIEW;
			             
	           onclick="popup('SceltaOpzioniStampa.do?idOggetto="+idOggetto+"&tipoOggetto="+tipoOggetto+fulltext+view+"&idCartProv="+idCartAppartenenza+"&idColl="+idCollegamento+"',600,250, 0, 50);";
	 		   button.append("<a href=\"#\" onClick=\"if(linkOggettoPopup()){"+onclick+"}\" > ");
	           button.append("<NOBR>\n");
	           button.append("<div class=\"textPulsante\" align=\"left\"> ");
	           button.append("<img name=\"img\" src=\""+srcImg+"\" align=\"absmiddle\" width=\"16\" height=\"16\"></a>\n");
	       
	       }    
    	   return button.toString();
   }

   
   /***************************************************************************
	* METODI PRIVATI
	**************************************************************************/

   //Chiusura della connessione
   private void _finally() throws Exception {
       try
       {
           if (CCS_common.dataSource.equals("")){
               try{vu.disconnectClose();}catch(Exception ei){}
           }
           CCS_common.closeConnection(dbOp);
       }
       catch (Exception e) {
           throw e;
       }
   }


   /**
    * Costruzione dell'ordinamento dall'oggetto in questione
    * 
    * @return Vector 	vettore di (istruzione,tipo_ordinamento) per effettuare il binding
    * 					
    * 
	 */
 /*  private Vector getOrdinamentiFromCartelle(String tipo_oggetto) throws Exception
   {
          String sql="";
          Vector criteri=new Vector();
          ResultSet rs=null;
          
          try
          {
        	sql+=" select seq,criterio,tipo_obj,tipo_ordinamento ";
        	sql+=" from ORDINAMENTI_CARTELLA o,CARTELLE c,DOCUMENTI d ";
        	sql+=" where id_cartella="+idCartella.substring(1,idCartella.length())+" and d.id_documento=c.id_documento_profilo ";
        	sql+=" and o.id_tipodoc=d.id_tipodoc ";
        	if(!tipo_oggetto.equals("TUTTI"))
        	 sql+=" and tipo_obj='"+tipo_oggetto+"' ";
        	sql+=" order by seq ";      	  
            dbOp.setStatement(sql);
      		dbOp.execute();
      	    rs=dbOp.getRstSet();
            while (rs.next())
            {
            	Properties p=new Properties();    
      	        p.put("CRITERIO",rs.getString("criterio"));
      	        p.put("ORD",rs.getString("tipo_ordinamento"));
      	        criteri.add(p);		
            }            
          }
          catch (Exception e) {   
       	   log.log_error("OrdinamentiCartelle - SQL: "+sql);
       	   throw e;
       	  }  
          return criteri;   
   } */
   
   /**
    * Costruzione dell'ordinamento dall'oggetto in questione
    * 
    * @return String 	istruzione per effettuare il binding 
    * 
	 */
  /* private String getOrdinamentiCartelle(String tipo_oggetto) throws Exception
   {
          String sql="";
          String criteri="";
          String ordine=null;
          boolean ord=false;
          ResultSet rs=null;
          
          try
          {
        	sql+=" select seq,criterio,tipo_obj,tipo_ordinamento ";
        	sql+=" from ORDINAMENTI_CARTELLA o,CARTELLE c,DOCUMENTI d ";
        	sql+=" where id_cartella="+idCartella.substring(1,idCartella.length())+" and d.id_documento=c.id_documento_profilo ";
        	sql+=" and o.id_tipodoc=d.id_tipodoc ";
        	if(!tipo_oggetto.equals("TUTTI"))
        	 sql+=" and tipo_obj='"+tipo_oggetto+"' ";
        	sql+=" order by seq ";      	  
            dbOp.setStatement(sql);
      		dbOp.execute();
      	    rs=dbOp.getRstSet();
            while (rs.next())
            {
            	ord=true;
            	criteri+=rs.getString("criterio")+" "+rs.getString("tipo_ordinamento")+",";		
            }
            
            if(ord)
             ordine=" order by "+criteri.substring(0,criteri.length()-1);
          }
          catch (Exception e) {   
       	   log.log_error("OrdinamentiCartelle - SQL: "+sql);
       	   throw e;
       	  }  
          return ordine;   
   }  
   */


    /**
     * Calcola alcune varibili in sessione
     * @param req
     *
     */
    private void getParametriSessione(){
        if(req!=null){
            if(req.getSession().getAttribute(MULTILINGUA)!=null){
                String lingua = req.getSession().getAttribute(MULTILINGUA).toString();
                if (lingua!=null && !lingua.equals(""))
                    jdmsML = lingua;
                else
                    jdmsML = "";
            }
            else
                jdmsML = "";
        }
    }

    private void initParametri() throws Exception {
        String path_inifile = getParametro("PATH_INIFILE", "@DMSERVER@");
        if(path_inifile!=null && !path_inifile.equals("") && req!=null)
            req.getSession().setAttribute("PATH_INIFILE", path_inifile);
    }

    private String verificaParametroGet(String parametro, String valore) throws Exception {
        if (valore == null) {
            return null;
        }
        String newVal=valore;

        if(xss!=null){
            newVal = xss.encodeHtmlAttribute(parametro,valore);
            if (!newVal.equals(valore)) {
                throw new Exception("Parametro "+parametro+" non valido!");
            }
        }
        return newVal;
    }

    private String getParametro(String codice, String tipo) throws Exception
   {
       String sql="",valore="";

       log.log_info("CCS_WorkArea::getParametro(codice,tipo)::("+codice+","+tipo+") - Inizio recupero del parametro");

       try
       {
           sql=" SELECT VALORE FROM PARAMETRI ";
           sql+=" WHERE CODICE = :PARAMETRO ";
           sql+=" AND TIPO_MODELLO= :TIPO_MODELLO";
           dbOp.setStatement(sql);
           dbOp.setParameter(":PARAMETRO",codice);
           dbOp.setParameter(":TIPO_MODELLO",tipo);
           dbOp.execute();
           ResultSet rst = dbOp.getRstSet();

           if (rst.next())
               valore = rst.getString("valore");

       }
       catch ( SQLException e ) {
           log.log_error("CCS_WorkArea::getParametro(codice,tipo)::("+codice+","+tipo+") -- Problema durante il recupero del parametro - SQL:"+sql);
           return "";
       }
       finally {

       }

       log.log_info("CCS_WorkArea::getParametro(codice,tipo)::("+codice+","+tipo+") - Fine recupero del parametro - valore:"+valore);
       return valore;
   }


    private String getStile() throws Exception
    {
        String sql="",link_stile;

        try
        {
     	    sql="SELECT NVL (amvweb.get_preferenza ('Style', 'GDMWEB', :utente),'AFC') mvstile,"+
                    "  '<link id=\"AFC\" name=\"AFC\" href=\"../Themes/'"+
                    "  || NVL (amvweb.get_preferenza ('Style', 'GDMWEB', :utente), 'AFC')"+
                    "  || '/Style.css\" type=\"text/css\" rel=\"stylesheet\">' stile  FROM DUAL";

            dbOp.setStatement(sql);
            dbOp.setParameter(":utente",utente);
            dbOp.execute();
            ResultSet rst = dbOp.getRstSet();

            if (rst.next()) {
                link_stile = rst.getString("stile");
            }
            else {
                link_stile = "<link id=\"AFC\" name=\"AFC\" href=\"../Themes/AFC/Style.css\" type=\"text/css\" rel=\"stylesheet\">";
            }

        }
        catch ( SQLException e ) {
            log.log_error("CCS_WorkArea::getStile -- Problema durante il recupero del foglio di stile - SQL:"+sql);
            return "<link id=\"AFC\" name=\"AFC\" href=\"../Themes/AFC/Style.css\" type=\"text/css\" rel=\"stylesheet\">";
        }
        return link_stile;
    }

    /**
    * Recupero parametri da una query di ricerca Modulistica.
    * 
    * @param  id_oggetto 	id_oggetto Query
    * @return String 		RICERCAMODULISTICA_area@cm
    * 
	 */
 	private String getParametriRicercaMod(String id_oggetto) throws Exception
 	{
 			String sql,src="";

		    try
		    {			    
			   sql="select filtro from query where id_query = :IDOGGETTO";
			   dbOp.setStatement(sql);
               dbOp.setParameter(":IDOGGETTO",id_oggetto);
               dbOp.execute();
			   ResultSet rs = dbOp.getRstSet();
			   if( rs.next() ) {
		         src+= rs.getString(1);
			   }

	       }
	       catch (SQLException e) {
	        throw e;
	       }  
	       return src;
 	}
   
   
   /**
     * Costruzione della condizione del Filtro Fulltext
     * 
     * @param String 	contenuto del testo da filtrare
     * 
	 */
   private void setWhereFullText(String w)  throws Exception
   {
	       String sCondizioneFullText="";
	       String filtro="";
	       boolean bAncheNumero=false;
		  
		   if (w!=null && (!w.equals("")) ) 
		   {
				  try {
					  Long.parseLong(w);
					  sCondizioneFullText="("+w+")";
					  bAncheNumero=true;
				  }
				  catch(Exception e) {
					  java.util.StringTokenizer s = new java.util.StringTokenizer(protectReserveWord(w)," ");			  			  			  
					  sCondizioneFullText+="(" ;
					  while (s.hasMoreTokens())
					  {
						sCondizioneFullText+=s.nextElement();
		   	            if (s.hasMoreTokens()) sCondizioneFullText+=" AND ";
		              }
				     sCondizioneFullText+=")";		     		    				  
				  }
				  
				  filtro = " AND f_filtro_fulltext_warea(d.id_documento,'"+sCondizioneFullText+"'";
				  
				  if(RicercaAllegati.equals("S"))
					filtro+=" ,'S'";
				  else
					filtro+=" ,' '";   
					//WhereFullText=" AND f_filtro_fulltext_warea(d.id_documento,'"+sCondizioneFullText+"','S')>0 ";  
				
				  if(RicercaOCR.equals("S"))
					filtro+=" ,'S'";	   
				  else
					filtro+=" ,' '";  
				  
				  if(RicercaFT.equals("S"))
					filtro+=" ,'S'";	   
				  else
					filtro+=" ,' '";  
				  
				  filtro+=" )>0 "; 
				  
				  WhereFullText = filtro;
				  //WhereFullText=" AND f_filtro_fulltext_warea(d.id_documento,'"+sCondizioneFullText+"')>0 ";
				  /**WhereFullText=" AND EXISTS ( SELECT 1 FROM valori WHERE valori.id_documento = d.id_documento  AND valori.valore_stringa like '%"+sCondizioneFullText.toUpperCase()+"%' )";*/   
		  }
   } 
   
   /**
     * Costruzione della frase da filtrare con la protezione 
     * di alcuni caratteri speciali.
     */
   private String protectReserveWord(String phrase) {
  	   for(int i=0;i<reserveWord.length;i++) {
  		   phrase=vu.Global.replaceAll(phrase,reserveWord[i],escapeCaracter+reserveWord[i]);
  	   }
  
      return phrase;
   }
   
   /**
     * Costruzione select generale
     * 
     * @param String sql select
     * 
	 */
   private String _Select() throws Exception
   {
           StringBuffer sql = new StringBuffer("");
           
           try 
           {
             if (idQuery!=null)
             {
               sql.append(CartSelect());
        	   /** Nel caso di chiamate esterne alla WorkArea non 
        	    * vengono considerati altri Collegamenti */
        	   if(((idCollegamento==null) || (idCollegamento!=null && idCollegamento.equals(""))) && (!HTML))
               {
        		 sql.append("union all ");
        		 sql.append(CollegamentoSelect());
               }
        	   sql.append(" union all ");
        	   sql.append(QuerySelect());
        	   sql.append(" union all ");
        	   sql.append(DocSelect());
        	   sql.append(" order by 1,9,2 desc");
             }
             else
             {
               /** Visualizzazione solo Documenti */
               if(VIEW.equals("D"))
               {
            	 sql.append(DocSelect());
            	 sql.append(" order by 1,12,2 desc");
               }
               else 
               {   /** Visualizzazione solo Ricerche */
            	   if(VIEW.equals("R"))
                   {
            		   sql.append(QuerySelect());                    
                   }
            	   else
                   {
            		   sql.append(CartSelect());
	                   if((idCollegamento==null) || (idCollegamento!=null && idCollegamento.equals("")))
	                   {
	                	 sql.append("union all ");
	                	 sql.append(CollegamentoSelect());
	                   }
	                   sql.append("union all ");
	                   sql.append(QuerySelect());
	                   sql.append("union all ");
	                   sql.append(DocSelect());  
	                   sql.append("union all ");
	                   sql.append(CollegamentiEsterniSelect());  
                   }	   
                }
              }
           } 
           catch (Exception e) {    
        	   throw e;
           }
           return sql.toString();   
   }  
   
   
   /**
    * Costruzione select generale
    * 
    * @param String sql select
    * 
	 */
   private String xml_select() throws Exception
   {
	   	   StringBuffer sql = new StringBuffer("");
                 
           try 
           {
        	   sql.append(CartSelect());
        	   sql.append("union all ");
        	   sql.append(CollegamentoSelect());
        	   sql.append("union all ");
        	   sql.append(QuerySelect());
        	   sql.append("union all ");
        	   sql.append(DocSelect());   
        	   
        	   if (idQuery!=null)
        		 sql.append(" order by 1,9,2 desc");
           } 
           catch (Exception e) {    
        	   throw e;
           }
           return sql.toString();   
   }  
   
   
   /**
    * Costruzione select generale
    * 
    * @param String sql select
    * 
	 */
  private String buildSelectStampa() throws Exception
  {
	      StringBuffer sql = new StringBuffer("");
                
          try 
          {
        	 sql.append(DocSelectStampa());
        	 if (idQuery!=null)
        	 {
        		 if(ordineSEQ.equals(""))
       			   sql.append(" order by 1,9,2 desc"); 
        		 else
        		   sql.append(" order by 1,"+ordineSEQ+" 2 desc");
             }
        	 else
               sql.append(" ORDER BY ordina, modificata, data_aggiornamento DESC ");
          } 
          catch (Exception e) {    
       	   throw e;
          }
          return sql.toString();   
  }     
   
   /**
     * Costruzione di una generica select 
     * 
     * @param String sql select
     * 
	 */
   private String GetSelect(int n,String ordina,String campi,String elencoTabelle,String condwhere,String index_ord) 
   {
           StringBuffer sql = new StringBuffer("SELECT ");
           sql.append(index_ord);
           sql.append(n);
           sql.append(" ");
           sql.append(ordina);
           sql.append(campi);
           sql.append(" FROM ");
           sql.append(elencoTabelle);
           sql.append(" ");
           sql.append(condwhere);
           return sql.toString();      
   } 
   
   /**
     * Costruzione di una generica select 
     * 
     * @param String sql select
     * 
	 */
   private String Select(String campi,String elencoTabelle,String condwhere) 
   {
	       StringBuffer sql = new StringBuffer("");
	       sql.append("SELECT ");
	       sql.append(campi);
           sql.append(" FROM ");
           sql.append(elencoTabelle);
           sql.append(" WHERE ");
           sql.append(condwhere);
           return sql.toString();
   }   
   
   /**
     * Costruzione select per l'oggetto Cartella
     * 
     * @param String sql select
     * 
	 */
   private String CartSelect() throws Exception
   {
           String compL="";
           String ordina,elencoTabelle;
           String idCart,idCartProv=null,idOggetto,nome;
           String id="",profilo="";
           String index_ordinamento="";
           
           StringBuffer campi = new StringBuffer();
           StringBuffer condwhere = new StringBuffer();
           StringBuffer sql = new StringBuffer();
           
           if (idQuery!=null) 
	       {
              id=" ID_VIEWCARTELLA oggetto, ";
        	  idCart="c.id_cartella";
              if (idCartAppartenenza!=null) 
              	idCartProv=" TO_NUMBER(:idCartAppartenenza) ";
               else
              	idCartProv=" TO_NUMBER(NULL) ";
              idOggetto="wc.ID_VIEWCARTELLA";
              nome="c.nome";
              profilo="c.id_documento_profilo profilo, ";
              elencoTabelle="documenti d,cartelle c, view_cartella wc, tipi_documento td, icone ic ";
			  condwhere.append(WhereCart);
			  condwhere.append(" and d.id_documento=c.ID_DOCUMENTO_PROFILO");
              condwhere.append(" and c.id_cartella=wc.id_cartella");
              condwhere.append(" and d.id_tipodoc = td.id_tipodoc "); 
              condwhere.append(" and td.icona = ic.icona(+) ");
              compL=decodeL("VIEW_CARTELLA","oggetto");
              ordina=getOrdina("wc");
	       }	 
	       else
	       { 
        	  id=" id_oggetto oggetto, ";
        	  idCart="id_oggetto";
              idCartProv="l.id_cartella ";
              idOggetto="F_IDVIEW_CARTELLA(ID_OGGETTO)";
              profilo="c2.id_documento_profilo profilo, ";
              nome="F_LINK(ID_OGGETTO, TIPO_OGGETTO)";
              elencoTabelle="documenti d,links l, cartelle c, cartelle c2, tipi_documento td, icone ic ";
              condwhere.append("where l.id_cartella= :id");
              if(WhereFullText!=null)
     		    condwhere.append(WhereFullText);
              condwhere.append(" and c.id_cartella= :id");
              condwhere.append(" and c2.id_cartella=id_oggetto");
              condwhere.append(" and TIPO_OGGETTO = 'C'");
              condwhere.append(" and d.id_documento=c2.ID_DOCUMENTO_PROFILO");
              condwhere.append(" and d.id_tipodoc = td.id_tipodoc ");
              condwhere.append(" and td.icona = ic.icona(+) ");
              condwhere.append(" and nvl(c2.stato,'BO')<>'CA' ");
              compL=decodeL("VIEW_CARTELLA","F_IDVIEW_CARTELLA(OGGETTO)");
              ordina=getOrdina("c2");             
              index_ordinamento=" /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */ ";
           }
	                 
           campi.append(" TO_NUMBER (td.id_tipodoc) ID_TIPODOC, TO_CHAR (td.nome) CM, d.area AREA, d.codice_richiesta CR, ");
           campi.append(OrdinaWhereCart);
           if(jdmsML!=null && !jdmsML.equals("")) 
        	 campi.append(",td.icona, GDM_UTILITY.F_MULTILINGUA (ic.nome,'"+jdmsML+"') icnome, ic.modificata, ");
           else
        	 campi.append(",td.icona, ic.nome icnome, ic.modificata, ");
           campi.append(idCartProv+" id_cart_prov, ");
           campi.append(idCart);
	       campi.append(" ID_OGGETTO, ");
	       campi.append(" TO_CHAR (NULL) conservazione, TO_CHAR (NULL) archiviazione, ");
	       campi.append(profilo);
	       if(jdmsML!=null && !jdmsML.equals("")) campi.append("GDM_UTILITY.F_MULTILINGUA ("+nome+",'"+jdmsML+"')"); else campi.append(nome);
	       campi.append(" nome, ");
	       campi.append("to_char(null) COLLEGAMENTO, ");
	       if(jdmsML!=null && !jdmsML.equals("")) 
	    	 campi.append("upper(GDM_UTILITY.F_MULTILINGUA ("+nome+",'"+jdmsML+"')) as nomeUPPER, ");   
	       else
	         campi.append("upper("+nome+") as nomeUPPER, ");
	       campi.append("'C' TIPO_OGGETTO, ");
	       campi.append("'' tipo, ");
	       campi.append("'-' stato, ");
	       if(jdmsML!=null && !jdmsML.equals("")) 
   	         campi.append("'"+h.getB("'||GDM_UTILITY.F_MULTILINGUA (c.nome,'"+jdmsML+"')||'")+"' nomeOggettoAttuale, ");	
	       else
	         campi.append("'"+h.getB("'||c.nome||'")+"' nomeOggettoAttuale, ");
           campi.append(GDM("VIEW_CARTELLA",idOggetto,"U")+" Modifica,");
    	   campi.append(GDM("VIEW_CARTELLA",idOggetto,"D")+" Elimina,");
    	   campi.append(GDM("VIEW_CARTELLA",idOggetto,"M")+" Competenze, ");
    	   campi.append(" 0 lettura_allegati, 0 modifica_allegati, ");
	       campi.append(id);
           campi.append(" to_char(null) RICERCAMODULISTICA, '' check_livello, 'N' competenze_allegati");
           
           /** Select Generale */
           sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
           sql.append("cr,ordinaQuery,icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
           sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,modifica,");
           sql.append("elimina,competenze,lettura_allegati,modifica_allegati,oggetto,RICERCAMODULISTICA,check_livello,competenze_allegati,'' url, '' tooltip, '' tipo_link,'' funzione_lettura,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
           sql.append(GetSelect(1,ordina,campi.toString(),elencoTabelle,condwhere.toString(),index_ordinamento));
          
           if(!index_ordinamento.equals(""))
             sql.append(" and l.ordinamento > ' ' ");           
           sql.append(" ) ");
           if (idQuery==null) 
            sql.append(" where ("+compL+")");
           return sql.toString();  
   }   
   
   /**
     * Costruzione select per l'oggetto Cartella Collegamento
     * 
     * @param String sql select
     * 
	 */
   private String CollegamentoSelect() throws Exception
   {
           String ordina,elencoTabelle;
           String idCart,idCartProv,idOggetto,nome;
           String index_ordinamento="",cwft="";          
           StringBuffer condwhere = new StringBuffer();
           StringBuffer campi = new StringBuffer();
           StringBuffer sql = new StringBuffer("");
           
           if(idQuery!=null)
        	   cwft="";
           else
           {
           	 if(WhereFullText!=null)
           	   cwft=WhereFullText;
           }
          
           idCart="ID_CARTELLA_COLLEGATA";
           idCartProv="coll.id_cartella";
           idOggetto="F_IDVIEW_CARTELLA(ID_CARTELLA_COLLEGATA)";
           nome="F_LINK(ID_CARTELLA_COLLEGATA, 'C')";
           elencoTabelle="documenti d,collegamenti coll, cartelle c, cartelle c2, tipi_documento td, icone ic ";
           condwhere.append("where coll.id_cartella= :id ");
           condwhere.append(cwft);
           condwhere.append(" and c.id_cartella= :id");
           condwhere.append(" and c2.id_cartella=ID_CARTELLA_COLLEGATA");
           condwhere.append(" and d.id_documento=c2.ID_DOCUMENTO_PROFILO");
           condwhere.append(" and d.id_tipodoc = td.id_tipodoc ");
           condwhere.append(" and td.icona = ic.icona(+) ");
           ordina=getOrdina("c2");
           campi.append(" TO_NUMBER (td.id_tipodoc) ID_TIPODOC, TO_CHAR (td.nome) CM, d.area AREA, d.codice_richiesta CR, ");
           campi.append("0 ordinaQuery,");
           if(jdmsML!=null && !jdmsML.equals(""))
        	   campi.append(" td.icona, GDM_UTILITY.F_MULTILINGUA(ic.nome,'"+jdmsML+"') icnome, ic.modificata, ");
           else
        	   campi.append(" td.icona, ic.nome icnome, ic.modificata, ");
           campi.append(idCartProv+" ID_CART_PROV, ");
           campi.append(idCart+" id_oggetto, ");
           campi.append(" TO_CHAR (NULL) conservazione, TO_CHAR (NULL) archiviazione, ");
           campi.append("c2.id_documento_profilo profilo, ");
           if(jdmsML!=null && !jdmsML.equals(""))
        	   campi.append("GDM_UTILITY.F_MULTILINGUA ("+nome+",'"+jdmsML+"') nome, "); 
           else	   
        	   campi.append(nome+" nome, ");
           campi.append("TO_CHAR(coll.id_collegamento) COLLEGAMENTO, ");
           if(jdmsML!=null && !jdmsML.equals(""))
        	   campi.append("upper(GDM_UTILITY.F_MULTILINGUA ("+nome+",'"+jdmsML+"')) as nomeUPPER, ");
           else
        	   campi.append("upper("+nome+") as nomeUPPER, ");
           campi.append("'X' TIPO_OGGETTO, ");
           campi.append("'' tipo, ");
           campi.append("'-' stato, ");
           if(jdmsML!=null && !jdmsML.equals(""))
           	campi.append("'"+h.getB("'||c.nome||'")+"' nomeOggettoAttuale, ");
           else
        	   campi.append("'"+h.getB("'||GDM_UTILITY.F_MULTILINGUA (c.nome,'"+jdmsML+"')||'")+"' nomeOggettoAttuale, ");
    	   campi.append(GDM("VIEW_CARTELLA",idOggetto,"U")+" Modifica, ");
           campi.append(GDM("VIEW_CARTELLA",idOggetto,"D")+" Elimina, ");
           campi.append(GDM("VIEW_CARTELLA",idOggetto,"M")+" Competenze, ");
           campi.append(" 0 lettura_allegati, 0 modifica_allegati, ");
           campi.append(" id_cartella_collegata oggetto, ");
           campi.append(" to_char(null) RICERCAMODULISTICA, '' check_livello, 'N' competenze_allegati");
           condwhere.append(" and nvl(c2.stato,'BO')<>'CA' ");
           
           
           sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
           sql.append("cr,ordinaQuery,icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione,archiviazione,profilo,nome,collegamento,");
           sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,modifica,");
           sql.append("elimina,competenze,lettura_allegati,modifica_allegati,oggetto,RICERCAMODULISTICA,check_livello,competenze_allegati,'' url, '' tooltip, '' tipo_link,'' funzione_lettura,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
           sql.append(GetSelect(2,ordina,campi.toString(),elencoTabelle,condwhere.toString(),index_ordinamento));
           sql.append(" order by nomeupper ");
           sql.append(" ) ");
           if (idQuery==null) 
        	 sql.append(" where ("+decodeL("VIEW_CARTELLA","F_IDVIEW_CARTELLA(oggetto)")+")");
           return sql.toString();  
   }     
   
   /**
     * Costruzione select per l'oggetto Query
     * 
     * @param String sql select
     * 
	 */
   private String QuerySelect() throws Exception
   {      
           String ordina,elencoTabelle,index_ordinamento="";
           StringBuffer condwhere = new StringBuffer();
           StringBuffer campi = new StringBuffer();
           StringBuffer sql = new StringBuffer("");
           
	       if(idQuery!=null)
	       {
	    	  ordina=getOrdina("q");
	    	  campi.append(" TO_NUMBER (td.id_tipodoc) ID_TIPODOC, TO_CHAR (td.nome) CM, d.area AREA, d.codice_richiesta CR, ");
	          campi.append(OrdinaWhereQuery+",");
	          if(jdmsML!=null && !jdmsML.equals(""))
	        	  campi.append(" td.icona, GDM_UTILITY.F_MULTILINGUA(ic.nome,'"+jdmsML+"') icnome, ic.modificata, ");  
	          else	  
	        	  campi.append(" td.icona, ic.nome icnome, ic.modificata, ");
	          campi.append("TO_NUMBER(NULL) ID_CART_PROV, ");
	          campi.append("q.id_query ID_OGGETTO, ");
	          campi.append(" TO_CHAR (NULL) conservazione, TO_CHAR (NULL) archiviazione, ");
	          campi.append("q.id_documento_profilo profilo, ");
	          if(jdmsML!=null && !jdmsML.equals(""))
	            campi.append("GDM_UTILITY.F_MULTILINGUA(q.nome,'"+jdmsML+"') nome, ");  
	          else	  
	            campi.append("q.nome nome, ");
	          campi.append("to_char(null) COLLEGAMENTO, ");
	          if(jdmsML!=null && !jdmsML.equals(""))
	        	campi.append("upper(GDM_UTILITY.F_MULTILINGUA(q.nome,'"+jdmsML+"')) as nomeUPPER, ");  
	          else	  
	            campi.append("upper(q.nome) as nomeUPPER, ");
	          campi.append("'Q' TIPO_OGGETTO, ");
	          campi.append("'' tipo, ");
	          campi.append("'-' stato, ");
	          if(jdmsML!=null && !jdmsML.equals(""))
	            campi.append("'"+h.getB("'||GDM_UTILITY.F_MULTILINGUA(qStart.nome,'"+jdmsML+"')||'")+"' nomeOggettoAttuale,");
	          else
	            campi.append("'"+h.getB("'||qStart.nome||'")+"' nomeOggettoAttuale,");
	          campi.append(GDM("QUERY","q.id_query","U")+" Modifica,");
	          campi.append(GDM("QUERY","q.id_query","D")+" Elimina,");
	          campi.append(GDM("QUERY","q.id_query","M")+" Competenze, ");
	          campi.append(" 0 lettura_allegati, 0 modifica_allegati, ");
	          campi.append(" q.id_query oggetto, ");
	          campi.append(" decode(instr(q.filtro,'RICERCAMODULISTICA_'),0,'',substr(q.filtro,length('RICERCAMODULISTICA_')+1,length(q.filtro))) RICERCAMODULISTICA ");
	          campi.append(" ,'' check_livello, 'N' competenze_allegati ");
	          elencoTabelle="query q, query qStart, tipi_documento td, documenti d, icone ic ";
	    	  
	          condwhere.append(WhereQuery);
	    	  condwhere.append(" and qStart.id_query ="+idQuery);
			  condwhere.append(" and d.id_tipodoc = td.id_tipodoc ");
	          condwhere.append(" and td.icona = ic.icona(+) ");
	          condwhere.append(" and d.id_documento= q.id_documento_profilo");
	       }
	       else
	       {
		      ordina=getOrdina("q");
		      campi.append(" TO_NUMBER (td.id_tipodoc) ID_TIPODOC, TO_CHAR (td.nome) CM, d.area AREA, d.codice_richiesta CR, ");
	          campi.append(OrdinaWhereQuery+",");
	          if(jdmsML!=null && !jdmsML.equals(""))
	        	campi.append(" td.icona, GDM_UTILITY.F_MULTILINGUA(ic.nome,'"+jdmsML+"') icnome, ic.modificata, ");
	          else
	            campi.append(" td.icona, ic.nome icnome, ic.modificata, ");
	          campi.append("nvl(l.id_cartella,'') ID_CART_PROV, ");
	          campi.append("id_oggetto ID_OGGETTO, ");
	          campi.append(" TO_CHAR (NULL) conservazione, TO_CHAR (NULL) archiviazione, ");
	          campi.append("q.id_documento_profilo profilo, ");
	          if(jdmsML!=null && !jdmsML.equals(""))
	        	campi.append("GDM_UTILITY.F_MULTILINGUA(F_LINK(ID_OGGETTO, TIPO_OGGETTO),'"+jdmsML+"') nome, ");
	          else
	            campi.append("F_LINK(ID_OGGETTO, TIPO_OGGETTO) nome, ");
	          campi.append("to_char(null) COLLEGAMENTO, ");
	          if(jdmsML!=null && !jdmsML.equals(""))
	              campi.append("upper(GDM_UTILITY.F_MULTILINGUA(F_LINK(ID_OGGETTO, TIPO_OGGETTO),'"+jdmsML+"')) as nomeUPPER, ");
	          else
	        	  campi.append("upper(F_LINK(ID_OGGETTO, TIPO_OGGETTO)) as nomeUPPER, ");
	          campi.append("'Q' TIPO_OGGETTO, ");
	          campi.append("'' tipo, ");
	          campi.append("'-' stato, ");
	          if(jdmsML!=null && !jdmsML.equals(""))
	              campi.append("'"+h.getB("'||GDM_UTILITY.F_MULTILINGUA(c.nome,'"+jdmsML+"')||'")+"' nomeOggettoAttuale, ");
	          else
	        	  campi.append("'"+h.getB("'||c.nome||'")+"' nomeOggettoAttuale, ");
	          campi.append(GDM("QUERY","ID_OGGETTO","U")+" Modifica, ");
	          campi.append(GDM("QUERY","ID_OGGETTO","D")+" Elimina, ");
	          campi.append(GDM("QUERY","ID_OGGETTO","M")+" Competenze, ");
	          campi.append(" 0 lettura_allegati, 0 modifica_allegati, ");
	          campi.append(" id_oggetto oggetto, "); 
	          campi.append(" decode(instr(q.filtro,'RICERCAMODULISTICA_'),0,'',substr(q.filtro,length('RICERCAMODULISTICA_')+1,length(q.filtro))) RICERCAMODULISTICA ");
	          campi.append(" ,'' check_livello, 'N' competenze_allegati ");
	          elencoTabelle="links l, cartelle c, query q, tipi_documento td, documenti d, icone ic ";
			  condwhere.append("where l.id_cartella= :id");
	          condwhere.append(" and c.id_cartella= :id");				
	          if(WhereFullText!=null)
	     		condwhere.append(WhereFullText);
	          condwhere.append(" and TIPO_OGGETTO = 'Q'");
	          condwhere.append(" and id_oggetto=q.id_query ");
	          condwhere.append(" and d.id_tipodoc = td.id_tipodoc ");
	          condwhere.append(" and td.icona = ic.icona(+) ");
	          condwhere.append(" and d.id_documento= q.id_documento_profilo");
	          
	          index_ordinamento=" /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */ ";

	      } 
          
	      /** Select Generale */
          sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
          sql.append("cr,ordinaQuery,icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
          sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,modifica,");
          sql.append("elimina,competenze,lettura_allegati,modifica_allegati,oggetto,RICERCAMODULISTICA,check_livello,competenze_allegati,'' url, '' tooltip, '' tipo_link,'' funzione_lettura,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
          sql.append(GetSelect(3,ordina,campi.toString(),elencoTabelle,condwhere.toString(),index_ordinamento));
          if(!index_ordinamento.equals(""))
            sql.append(" and l.ordinamento > ' ' ");           
          sql.append(" ) ");
          if(idQuery==null)
           sql.append(" where ("+decodeL("QUERY","OGGETTO")+")");
	      return sql.toString();
   }
   
   /**
     * Costruzione select per l'oggetto Documento
     * 
     * @param String sql select
     * 
	 */
   private String DocSelect() throws Exception
   {
           int n;
           String ordina,elencoTabelle,idCartProv,idDoc,index_ordinamento="";
           StringBuffer condwhere = new StringBuffer();
           StringBuffer campi = new StringBuffer();
           StringBuffer sql = new StringBuffer("");
                      
           
           if(NomeQuery==null)
        	NomeQuery ="";   
           
           if(idQuery!=null)
           {
             idDoc="d.id_documento";
             if (idCartAppartenenza!=null) 
               	idCartProv=" TO_NUMBER(:idCartAppartenenza) ";
             else
               	idCartProv=" TO_NUMBER(NULL) ";
             n=4;
             elencoTabelle="documenti d, icone ic, check_documenti ck, tipi_documento td "+tabellaLD;
             condwhere.append(Where);
             condwhere.append(" and d.ID_TIPODOC=td.ID_TIPODOC");
             condwhere.append(" and td.icona = ic.icona(+) "); 
             condwhere.append(" and d.id_documento =  ck.id_documento (+) ");
           } 
           else
           {
             idDoc="ID_OGGETTO"; 
             idCartProv="l.id_cartella";
             n=4;
             elencoTabelle="links l, cartelle c,documenti d, icone ic, check_documenti ck , tipi_documento td";
		     condwhere.append("where l.id_cartella= :id ");
		     if(WhereFullText!=null)
		   	   condwhere.append(WhereFullText);
		     condwhere.append(" and c.id_cartella= :id ");
             condwhere.append(" and TIPO_OGGETTO = 'D' ");
             condwhere.append(" and d.ID_TIPODOC=td.ID_TIPODOC");
             condwhere.append(" and td.icona = ic.icona(+) ");
             condwhere.append(" and d.id_documento =  ck.id_documento (+) ");
             condwhere.append(" and d.id_documento=ID_OGGETTO");
             condwhere.append(" and d.STATO_DOCUMENTO not in ('CA','RE','PB')");
             index_ordinamento=" /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */ ";
            }
          
           ordina=getOrdina("d");
           campi.append("d.ID_TIPODOC ID_TIPODOC, ");
           campi.append("td.nome CM, ");
           campi.append("d.area  AREA, ");
           campi.append("d.codice_richiesta CR, ");
           campi.append(OrdinaWhere+",");
           campi.append("td.icona, ic.nome icnome, ic.modificata, ");
           campi.append(idCartProv+" ID_CART_PROV, ");
           campi.append(idDoc+" ID_OGGETTO, ");
           campi.append(" d.conservazione conservazione, d.archiviazione archiviazione, ");
           campi.append("d.id_documento profilo, ");
           campi.append("d.stato_documento nome, ");
           campi.append("to_char(null) COLLEGAMENTO, ");
           campi.append("d.stato_documento as nomeUPPER , ");
           campi.append("'D' TIPO_OGGETTO, ");
           campi.append("'' tipo, ");
           campi.append("'-' stato, ");
           if(idQuery!=null)
        	 campi.append("'"+h.getImg(_QRYSEARCH)+h.getB("'||'"+Global.replaceAll(NomeQuery,"'","''")+"'||'")+"' nomeOggettoAttuale, ");			
           else 
        	 campi.append("'' nomeOggettoAttuale, ");			
           campi.append(GDM("DOCUMENTI",idDoc,"U")+" Modifica,");
           campi.append(GDM("DOCUMENTI",idDoc,"D")+" Elimina,");
           campi.append(GDM("DOCUMENTI",idDoc,"M")+" Competenze,");
           campi.append(GDM("DOCUMENTI",idDoc,"LA")+" lettura_allegati,");
           campi.append(GDM("DOCUMENTI",idDoc,"UA")+" modifica_allegati,");
           campi.append(" d.id_documento oggetto, ");
           campi.append(" to_char(null) RICERCAMODULISTICA");
           campi.append(" ,decode(nvl(ck.livello_checkin,0),0,'',decode('"+utente+"',ck.utente_checkin,'V','R')) check_livello,");
           campi.append(" td.competenze_allegati");
	      
           /** Select Generale */
           sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
           sql.append("cr,ordinaQuery,icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
           sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,modifica,");
           sql.append("elimina,competenze,lettura_allegati,modifica_allegati,oggetto,RICERCAMODULISTICA,check_livello,competenze_allegati,'' url, '' tooltip, '' tipo_link,'' funzione_lettura,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
           
           sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
           sql.append("cr,ordinaQuery,icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
           sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,modifica,");
           sql.append("elimina,competenze,lettura_allegati,modifica_allegati,oggetto,RICERCAMODULISTICA,check_livello,competenze_allegati,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
           sql.append(GetSelect(n,ordina,campi.toString(),elencoTabelle,condwhere.toString(),index_ordinamento));
           if(!index_ordinamento.equals(""))
              sql.append(" and l.ordinamento > ' ' ");           
           sql.append(" ) ");
           sql.append(" union all ");
           sql.append("SELECT TO_NUMBER (NULL), TO_DATE (NULL), TO_CHAR (NULL),");
           sql.append("TO_CHAR (NULL), TO_NUMBER (NULL), TO_CHAR (NULL),");
           sql.append("TO_CHAR (NULL), TO_CHAR (NULL), TO_NUMBER (NULL),");
           sql.append("TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),");
           sql.append("TO_NUMBER (NULL), TO_NUMBER (NULL), TO_CHAR (NULL),");
           sql.append("TO_CHAR (NULL), TO_NUMBER (NULL), TO_CHAR (NULL),");
           sql.append("TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),");
           sql.append("TO_CHAR (NULL), TO_CHAR (NULL), TO_NUMBER (NULL),");
           sql.append(" TO_NUMBER (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL), ");
           sql.append("TO_NUMBER (NULL), TO_NUMBER (NULL),");
           sql.append("TO_CHAR (NULL), TO_CHAR (NULL),TO_CHAR (NULL),");
           sql.append("NLSSORT (dummy, 'NLS_SORT = ASCII7')");
           sql.append(" from dual ) , dual ");
           if (idQuery==null) 
       	    sql.append(" where ("+decodeL("DOCUMENTI","OGGETTO")+")");
           else
       	    sql.append(" where ordina is not null ");
           return sql.toString();
   }

   private String DocSelectStampa() throws Exception
   {
           int n;
           String ordina,elencoTabelle,idCartProv,idDoc,index_ordinamento="";
           StringBuffer condwhere = new StringBuffer();
           StringBuffer campi = new StringBuffer();
           StringBuffer sql = new StringBuffer("");
                   
           if(idQuery!=null)
           {
             idDoc="d.id_documento";
             if (idCartAppartenenza!=null) 
               	idCartProv=" TO_NUMBER(:idCartAppartenenza) ";
             else
               	idCartProv=" TO_NUMBER(NULL) ";
             n=4;
             elencoTabelle="documenti d, icone ic, check_documenti ck ,tipi_documento td "+tabellaLD;
             condwhere.append(Where);
             condwhere.append(" and d.ID_TIPODOC=td.ID_TIPODOC");
             condwhere.append(" and td.icona = ic.icona(+) ");             
           } 
           else
           {
             idDoc="ID_OGGETTO"; 
             idCartProv="l.id_cartella";
             n=4;
             elencoTabelle="links l, cartelle c,documenti d, icone ic, check_documenti ck ,tipi_documento td ";
 		     condwhere.append("where l.id_cartella='"+idCartella.substring(1,idCartella.length())+"'");
 		     if(WhereFullText!=null)
 		   	   condwhere.append(WhereFullText);
 		     condwhere.append(" and c.id_cartella='"+idCartella.substring(1,idCartella.length())+"' ");
             condwhere.append(" and TIPO_OGGETTO = 'D' ");
             condwhere.append(" and d.ID_TIPODOC=td.ID_TIPODOC");
             condwhere.append(" and td.icona = ic.icona(+) ");
             condwhere.append(" and d.id_documento=ID_OGGETTO");
             condwhere.append(" and d.STATO_DOCUMENTO not in ('CA','RE','PB')");
             index_ordinamento=" /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */ ";
            }
          
           ordina=getOrdina("d");
           campi.append("d.ID_TIPODOC ID_TIPODOC, ");
           campi.append("td.nome CM, ");
           campi.append("d.area  AREA, ");
           campi.append("d.codice_richiesta CR, ");
           campi.append(OrdinaWhere+",");
           campi.append("td.icona, ic.nome icnome, ic.modificata, ");
           //if (((""+idCartProv).toUpperCase()).equals("NULL")) idCartProv="TO_NUMBER(NULL)";
           campi.append(idCartProv+" ID_CART_PROV, ");
           campi.append(idDoc+" ID_OGGETTO, ");
           campi.append(" d.conservazione conservazione, d.archiviazione archiviazione, ");
           campi.append("d.id_documento profilo, ");
           campi.append("d.stato_documento nome, ");
           campi.append("to_char(null) COLLEGAMENTO, ");
           campi.append("d.stato_documento as nomeUPPER , ");
           campi.append("'D' TIPO_OGGETTO, ");
           campi.append("'' tipo, ");
           campi.append("'-' stato, ");
           if(idQuery!=null)
        	   campi.append("'"+h.getImg(_QRYSEARCH)+h.getB("'||'"+Global.replaceAll(NomeQuery,"'","''")+"'||'")+"' nomeOggettoAttuale, ");			
           else 
        	   campi.append("'' nomeOggettoAttuale, ");			
         
           campi.append(" d.id_documento oggetto, ");
           campi.append(" to_char(null) RICERCAMODULISTICA");
           campi.append(" ,decode(nvl(ck.livello_checkin,0),0,'',decode('"+utente+"',ck.utente_checkin,'V','R')) check_livello");
 	      
           sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
           sql.append("cr,"+ordineSEQ+" icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
           sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,");
           sql.append("oggetto,RICERCAMODULISTICA,check_livello,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
           
           sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
           sql.append("cr,"+ordineSEQ+" icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
           sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,");
           sql.append("oggetto,RICERCAMODULISTICA,check_livello,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
           sql.append(GetSelect(n,ordina,campi.toString(),elencoTabelle,condwhere.toString(),index_ordinamento));
           if(!index_ordinamento.equals(""))
              sql.append(" and l.ordinamento > ' ' ");           
              sql.append(" ) ");
              sql.append(" union all ");
              sql.append("select to_number(null), to_date(null),");
              sql.append("TO_CHAR (null),");
              sql.append("TO_CHAR (null) ,");
              sql.append("to_number(null), TO_CHAR (null), TO_CHAR (null),");
              sql.append("TO_CHAR (null),");
              for(int i=0;i<num_blocchi;i++)              
               sql.append(" to_number(null),");
              sql.append(" TO_CHAR (null),");
              sql.append("TO_CHAR (null), TO_CHAR (null), to_number(null),");
              sql.append("to_number(null), TO_CHAR (null),TO_CHAR (null),to_number(null),");
              sql.append(" TO_CHAR (null), TO_CHAR (NULL) ,");
              sql.append(" TO_CHAR (null), TO_CHAR (null), TO_CHAR (null),");
              sql.append("TO_CHAR (null), TO_CHAR (null), "); 
              sql.append(" to_number(null),TO_CHAR (NULL),TO_CHAR (NULL), NLSSORT (dummy, 'NLS_SORT = ASCII7')");
              sql.append(" from dual ) , dual ");
              if (idQuery==null) 
           	   sql.append(" where ("+decodeL("DOCUMENTI","OGGETTO")+")");
              else
           	   sql.append(" where ordina is not null ");
              return sql.toString();
   }
  
  /**
   * Costruzione select per l'oggetto Documento
   * 
   * @param String sql select
   * 
	 */
 private String CollegamentiEsterniSelect() throws Exception
 {
         StringBuffer sql = new StringBuffer("");
       
         if(idQuery!=null)
         	sql.append("");
         else
         {
        	 sql.append("SELECT ordina,data_aggiornamento, dataagg,utenteagg,id_tipodoc,cm,area,");
             sql.append("cr,ordinaQuery,icona,icnome,modificata,id_cart_prov,id_oggetto,conservazione, archiviazione,profilo,nome,collegamento,");
             sql.append("nomeupper,tipo_oggetto,tipo,stato,nomeoggettoattuale,modifica,");
             sql.append("elimina,competenze,lettura_allegati,modifica_allegati,oggetto,RICERCAMODULISTICA,check_livello,competenze_allegati,url,tooltip,tipo_link,funzione_lettura,NLSSORT(NOMEUPPER, 'NLS_SORT = ASCII7') from ( ");
             sql.append(" SELECT 5 ordina, ce.data_aggiornamento,TO_CHAR (ce.data_aggiornamento, 'dd/mm/yyyy') dataagg, ");
        	 sql.append(" 		 f_nominativo_utente (ce.utente_aggiornamento) utenteagg,0 id_tipodoc, '' cm, '' area, ");
        	 sql.append(" 		 '' cr, 0 ordinaquery, ce.icona, ic.nome icnome,ic.modificata modificata, ");
        	 sql.append(" 		 l.id_cartella id_cart_prov, l.id_oggetto id_oggetto,TO_CHAR (NULL) conservazione, TO_CHAR (NULL) archiviazione,0 profilo,");
        	 if(jdmsML!=null && !jdmsML.equals("")){ 
        	   sql.append("GDM_UTILITY.F_MULTILINGUA(ce.nome,'"+jdmsML+"') nome,TO_CHAR (NULL) collegamento, upper(GDM_UTILITY.F_MULTILINGUA(ce.nome,'"+jdmsML+"')) AS nomeupper,");
        	   sql.append(" 'L' tipo_oggetto, '' tipo, '-' stato, '<b>'||GDM_UTILITY.F_MULTILINGUA(ce.nome,'"+jdmsML+"')||'</b>' nomeoggettoattuale, ");
        	 }
        	 else {
        	   sql.append("ce.nome nome,TO_CHAR (NULL) collegamento, upper(ce.nome) AS nomeupper,");
        	   sql.append(" 'L' tipo_oggetto, '' tipo, '-' stato, '<b>' || ce.nome || '</b>'  nomeoggettoattuale, ");
        	 }          	 
        	 sql.append(" 		 0 modifica,0 elimina,0 competenze,0 lettura_allegati,0 modifica_allegati, ");
        	 sql.append(" 		 ce.id_collegamento oggetto,TO_CHAR (NULL) ricercamodulistica,'' check_livello, ");
        	 sql.append(" 		 'N' competenze_allegati,");
        	 sql.append("		 ce.url, ce.tooltip, ce.tipo_link, nvl(ce.funzione_lettura,'') funzione_lettura,");
        	 sql.append("		 NLSSORT (upper(ce.nome), 'NLS_SORT = ASCII7') ");
        	 sql.append(" FROM links l, collegamenti_esterni ce, icone ic ");
        	 sql.append(" WHERE l.id_cartella= :id ");
        	 sql.append(" 		and l.id_oggetto = ce.id_collegamento ");
        	 sql.append(" 		and l.tipo_oggetto = 'L' ");
        	 sql.append(" 		and ce.icona = ic.icona(+)  ");
        	 if(valoreFullText!=null)
        	  sql.append(" 		 and upper(ce.nome) like upper('%"+valoreFullText+"%') ");
        	 sql.append(" order by ordina,data_aggiornamento desc,ce.nome )");
         }
         return sql.toString();
 }
  
   
   /**
     * Verifica competenza dell'oggetto
     * 
     * @param oggetto 		da controllare VIEW_CARTELLA, QUERY o DOCUMENTO
     * @param idOggetto 	identificativo oggetto
     * @param azione 		tipo di competenza 
     * @return String 		stringa di verifica competenza
     * 
	 */
   private String GDM(String oggetto,String idOggetto,String azione)
   {     
           StringBuffer decode = new StringBuffer("");
           decode.append("GDM_COMPETENZA.GDM_VERIFICA('"+oggetto+"',"+idOggetto+", '"+azione+"', '");
           decode.append(utente+"',  F_TRASLA_RUOLO('"+utente+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))");
           return decode.toString();            
	}
   

   /**
     * Verifica competenza di lettura dell'oggetto
     * 
     * @param  oggetto 		da controllare VIEW_CARTELLA, QUERY o DOCUMENTO
     * @param  idOggetto 	identificativo oggetto
     * @return String 		stringa competenza uguale a 0 o 1
     * 
	 */
   private String decodeL(String oggetto,String idOggetto) {     
	       StringBuffer decode = new StringBuffer("(GDM_COMPETENZA.GDM_VERIFICA('"+oggetto+"',"+idOggetto+", 'L', '");
           if(oggetto.equals("DOCUMENTI"))
        	decode.append(utente+"',  F_TRASLA_RUOLO('"+utente+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) || dummy = '1X'  ) ");
           else
        	decode.append(utente+"',  F_TRASLA_RUOLO('"+utente+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1) ");
           return decode.toString();     
   }
   
   /**
     * Gestione della Modifica 
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa modifica
     * 
	 */
   private String decodeModificaD(String nome,String idtipoDoc,String id_oggetto,String area,String cm,String cr,String idCartProv,String check_livello) throws Exception
   {     
           String decode=_IMGVUOTO;
           String id,Prov,stato,url,icona;
         
           if (idQuery!=null)
             id=idQuery;       
           else  
             id="-1";
              
           if (idQuery==null) 
             Prov="C";
           else
             Prov="Q";
              
           if (nome.equals("CO"))
             stato="CO";
           else
            if (nome.equals("AN"))
              stato="AN";
            else
              stato="BO";   
         
           if (Prov.equals("Q"))
		     url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
           else
			 url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";  
                      
           /** Controllo se il documento è bloccato */
           if(check_livello.equals("")) {
        	
        	   String src_icona,icona_default,icona_path,title;	 
            	
        	   if (nome.equals("BO")){
        		 icona_default="src=\""+_EDIT+"\"";
        		 title = "Modifica il Documento";
        	   }	 
               else {
            	 icona_default=decodeD(nome,null,"M");	 
            	 title = "Visualizza Documento";
               }
        	   
        	   icona_path = icona_default;
        	   
         	   try {
 	        	 src_icona = getIcona(id_oggetto,idtipoDoc,"5","N",icona_default); 
 	        	if(src_icona.equals(icona_default))
 	        	 icona_path = src_icona;
 	           	else
 	           	 icona_path = "src=\"./icone/"+src_icona+"\"";
 	           }
 	           catch (Exception e) {
 	        	  log.log_error("CCS_WorkArea::decodeModificaD::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
 	        			  		        + "("+id_oggetto+","+idtipoDoc+",5,'N','"+icona_default+"') "
 	        		  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
 	        	  src_icona = icona_default;
 			   }
 	            	           
              icona=h.getImgMultipla("Visualizza Documento",icona_path);	 
 	          
 	       }
           else {
        	   if(check_livello.equals("V"))
        		 icona =h.getImgHand("Modifica Documento Bloccato",_CHECK_V); 
        	   else
        		 icona =h.getImgHand("Documento Bloccato",_CHECK_R);   
           }
           
    	   /** Se il documento modificabile è allo stato BOZZA viene visualizzato il link alla DocumentoView in modalità
            * modifica altrimenti in modalità di lettura non più modificabile dato che lo stato è ANNULLATO o COMPLETO */
           if (nome.equals("BO"))
            decode=h.getAncore("#","if(linkOggettoPopup()){"+h.DocumentoViewPop(idtipoDoc,id_oggetto,"W",cm,area,cr,idCartProv,id,Prov,stato,url)+"}","",icona);
           else
            decode=h.getAncore("text-decoration : none","#","if(linkOggettoPopup()){"+h.DocumentoViewPop(idtipoDoc,id_oggetto,"R",cm,area,cr,idCartProv,id,Prov,stato,"")+"}","",icona);
        	
         return decode;     
   }
   
   /**
     * Gestione della Elimina 
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa elimina
     * 
	 */
   private String decodeEliminaD(String id_oggetto,String idCartProv) throws Exception {     
           StringBuffer decode = new StringBuffer(h.getAncore("#","if(linkOggettoPopup()){"+h.AnnullaDocPop(id_oggetto,idCartProv,idQuery)+"}","",h.getImgHand("Elimina il Documento",_ANNULLA)));
           return decode.toString();     
   }
   
   /**
     * Gestione src icona per l'oggetto Documento
     * 
     * @param  idOggetto   	identificativo oggetto
     * @param  iconaTipiDoc 	icona associata al modello
     * @param  azione 	    tipo di competenza lettura o modifica
     * @return String 		src icona del documento
     * 
	 */
   private String decodeD(String idOggetto,String iconaTipiDoc,String azione) {     
           String decode="";
  
           if(HTML)
           {
             if (idOggetto.equals("CO"))
             {
                decode="src=\""+PATHIMAGE+_COMPLETO_EXT+"\"";
                return decode;
             }
             
             if (idOggetto.equals("AN"))
             {
                decode="src=\""+PATHIMAGE+_ANNULLATO_EXT+"\"";
                return decode;
             }
             
             if (idOggetto.equals("BO"))
             {
                decode="src=\""+PATHIMAGE+_DOT_EXT+"\"";
                return decode;
             }
           }
          else
          {
             if(iconaTipiDoc!=null)
             {
                decode="src=\""+iconaTipiDoc+"\"";
                return decode;
             }
             else 
             { 
               if(azione.equals("L"))
               {
                 decode="src=\""+_DOT+"\"";
                 return decode;
               }
               else
               {
                 if (idOggetto.equals("CO"))
                 {
                    decode="src=\""+_COMPLETO+"\"";
                    return decode;
                 }
                 
                 if (idOggetto.equals("AN"))
                 {
                    decode="src=\""+_ANNULLATO+"\"";
                    return decode;
                 }
                 
                 if (idOggetto.equals("BO"))
                 {
                    decode="src=\""+_DOT+"\"";
                    return decode;
                 }
               }
             }
          }
          return decode;     
   }  
   
   /**
     * Visualizzazione della lista di oggetti della WorkArea
     * 
     * @return String 	HTML della lista di oggetti
     * 
	 */
   private String getListaOggetti() throws Exception
   {
           String msg="Ricerca in corso...";
           String norecords,navigator;         
           int nrecords=0;// indica il numero di record
           StringBuffer list =new StringBuffer();
           StringBuffer header =new StringBuffer();
           StringBuffer contentTBODY =new StringBuffer();
           String numRecord="";
           String imgTotRecord="";
           String sql=_Select();
	       
	       if (vu.Global.PRINT_WAREA.equals("S")) {
	   		   System.out.println("[INFO WorkArea - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
	       }
		   
	       log.log_info("Inizio Visualizzazione della lista di oggetti della WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	     
           /** HEADER */
	       if(HTML)
	       {
	    	 header.append(h.getTDClassStyle("","2%","&nbsp;"));
	    	 header.append(h.getTDClassStyle("","63%","Oggetto"));
	    	 header.append(h.getTDClassStyle("","5%","&nbsp);"));
	    	 header.append(h.getTDClassStyle("","0%",h.getP("right","&nbsp;&nbsp;Aggiornato il&nbsp;")));
	    	 header.append(h.getTDClassStyle("","10%","&nbsp;"));
	       }
	       else
	       {
	    	 header.append(h.getTDClassStyle("","2%",h.getInput("Seleziona tutti gli Oggetti","","checkbox","AllSeleziona","AllSeleziona","","","checkedALLCheckBox(document.forms['elenco'].AllSeleziona.checked);")));
	    	 header.append(h.getTDClassStyle("","63%","Oggetto"));
	    	 header.append(h.getTDClassStyle("","5%","&nbsp;"));
	    	 header.append(h.getTDClassStyle("","5%","&nbsp;"));
	    	 header.append(h.getTDClassStyle("","0%","&nbsp;"));
	    	 header.append(h.getTDClassStyle("","0%",h.getP("center","&nbsp;&nbsp;Aggiornato il&nbsp;")));
	    	 header.append(h.getTDClassStyle("","10%","&nbsp;"));
	       }
	       contentTBODY.append("\n");
	       
	       /** Determina il numero di records della select */
	       if(idQuery!=null)
	    	 nrecords=vlistID.size();
	       else 
	    	 nrecords=this.getCountSELECT(sql);
         
	       /** Setta Url */
	       this.setUrlPath(nrecords);
	       
	       if(JDMS_LINK.equals("S"))
        	costruisciJDMSLink(vlistIDDocs);
   
	       try
	       {    
	    	  log.log_info("Inizio Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          pHTMLRecord=getRigheMonoRecords(vlistIDDocs,req,false);
	    	  log.log_info("Fine Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          
	          String records=this.getRecordsRows(sql);

              if (isExitsRecords)
              {
            	 contentTBODY.append("\n");
            	 contentTBODY.append(records);
              }
              else
              {
               /** Nel caso di nessun elemento */
               norecords=h.getTDClass("","","","7","Nessun oggetto trovato&nbsp;");
               contentTBODY.append("\n");
               contentTBODY.append(h.getTRClass("AFCAltDataTD",norecords)); 
              }
	       }
	       catch (Exception e) {  
	    	 throw e;
   	       }
          
	       /** Costruzione del navigatore distinguendo nei vari casi:
	        *  Nel caso di Query */ 
	       if(idQuery!=null)
	       {   
        	 if(HTML)
        	 {
        		 navigator=h.getTDClass("AFCFooterTD","center","","5",this.getNavigator(nrecords)); 
				 contentTBODY.append(navigator);
				 contentTBODY.append("\n");
        	 }
        	 else 
        	 {
        		 numRecord ="\n<div id=\"numrecord\"></div>";
       	         imgTotRecord=h.getNbsp()+h.getAncore("#","if(linkOggettoPulsanti()){countRecords();}","0",h.getImg("Calcolo numero di record",_NUMRECORD));
        		 
        		 /** Nel caso di nessun elemento */
	        	 if (!isExitsRecords)
	              {
	    	    	  navigator=h.getNavigator("7",Url_page,1,1,nrecords,"",PAGE_SIZE,numRecord,imgTotRecord); 
	        	  }
	        	  else
	        	  {	
	    			/** Calcola il valore di NextPage */
	        		int tot=(int)Math.ceil((double)nrecords/PAGE_SIZE);
	        		String tp=page+"";
	        		int totpage=Integer.parseInt(tp.substring(0,(tp.length()-1))+"0");
	        		int succpage;
	        		
	        		if((totpage==0) || (Integer.parseInt(page)%10==0))
		    		{	
		    			if(totpage==0)
		    			  succpage=11;
		    			else
		    			  succpage=totpage+1;	
		    		}
		    		else
		      		  succpage=totpage+11;
	        		
	        		if(((!q.isLastRowFetch()) && ((tot%10)==0)) || (succpage<=tot) )
		    	    	nextPage=((int)Math.ceil((double)nrecords/PAGE_SIZE))+1;	
	        	    else
	        	      nextPage=0;	
	    		
	        	    navigator=h.getNavigator("7",Url_page,Integer.parseInt(linkPage),nextPage,nrecords,msg,PAGE_SIZE,numRecord,imgTotRecord); 
				  } 
	         }
          }/** Nel caso di Cartella */
          else
       	   navigator=this.getNavigator(nrecords);
          
          list.append("\n"+h.getTable("AFCFormTABLE","5","0","100%",h.getTBODY(contentTBODY.toString())));
          list.append("\n</div>");
          list.append("<div class=\"navigatore\" id=\"navigatore\" >"+h.getTable("AFCFormTABLE","0","0","100%",navigator)+"</div>");
       
          
          log.log_info("Fine Visualizzazione della lista di oggetti della WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
          return list.toString();
   }
   
   /**
    * Visualizzazione della lista di oggetti della WorkArea
    * 
    * @return String 	HTML della lista di oggetti
    * 
	 */
  private String getCartListaOggetti() throws Exception
  {
          String msg="Ricerca in corso...";
          String list,contentTBODY;
          String norecords,navigator;    
          String numRecord="";
          String imgTotRecord="";
          int nrecords=0;// indica il numero di record
          
	      String sql=_Select();
	       
	      if (vu.Global.PRINT_WAREA.equals("S")) {
	   		 System.out.println("[INFO WorkArea - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
	   	  }
		  
	      System.out.println("[INFO WorkArea - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
			     
	      log.log_info("Inizio Visualizzazione della lista di oggetti della WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	      //Visualizzazione Select WorkArea
          //log.log_info("Select WorkArea - "+sql.toString());
          //log.log_info("Select WorkArea - - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] "+sql.toString());
        
          contentTBODY="\n";
	      
	      /** Determina il numero di records della select */
	      if ( ((VIEW.equals("D")) || (VIEW.equals("R"))) && (vCartObjects!=null) && (vCartObjects.size()!=0))
	    	nrecords=vCartObjects.size();
	      
	      /** Setta Url */
	      this.setUrlPath(nrecords);
	      
	      try
	      {    
	    	 String records=this.getCartRecordsRows(sql,rsCartObjects);
             if ((vCartObjects!=null) && (vCartObjects.size()!=0))
             {
           	   contentTBODY+="\n";
           	   contentTBODY+=records;
             }
             else
             {
              /** Nel caso di nessun elemento */
              norecords=h.getTDClass("","","","7","Nessun oggetto trovato&nbsp;");
              contentTBODY+="\n";
              contentTBODY+=h.getTRClass("AFCAltDataTD",norecords); 
             }
	       }
	      catch (Exception e) { 
	    	  throw e;
	      }
	      
	      /** Determina il numero di records della select */
	      if ((vCartObjects!=null) && (vCartObjects.size()!=0))
	        nrecords=vCartObjects.size();
	      
	     
	      numRecord ="\n<div id=\"numrecord\"></div>";
	      imgTotRecord=h.getNbsp()+h.getAncore("#","if(linkOggettoPulsanti()){countRecords();}","0",h.getImg("Calcolo numero di record",_NUMRECORD));
 		 
	       
	      /** Costruzione del navigatore distinguendo nei vari casi */ 
	      if ((vCartObjects!=null) && (vCartObjects.size()!=0))
	      {	
			int tot=(int)Math.ceil((double)nrecords/PAGE_SIZE);
    		String tp=page+"";
    		int totpage=Integer.parseInt(tp.substring(0,(tp.length()-1))+"0");
    		int succpage;
    		
    		if((totpage==0) || (Integer.parseInt(page)%10==0))
    		{	
    			if(totpage==0)
    			  succpage=11;
    			else
    			  succpage=totpage+1;/*PAGE_SIZE*/;	
    		}
    		else
      		  succpage=totpage+10+1/*PAGE_SIZE*/;	
    		
    		int isExitSucc=0;
    		if(Integer.parseInt(page)%10==0)
    		 isExitSucc=vCartObjects.size()%Integer.parseInt(page);
    		
    		if((isExitsSucc || (isExitSucc==1)) || (succpage<=tot) )
	          nextPage=((int)Math.ceil((double)nrecords/PAGE_SIZE))+1;	
    	    else
    	      nextPage=0;	
		
    	    navigator=h.getNavigator("7",Url_page,Integer.parseInt(page),nextPage,nrecords,msg,PAGE_SIZE,numRecord,imgTotRecord); 
		   } 
	       else
	       {
	    	navigator=h.getNavigator("7",Url_page,1,1,nrecords,"",PAGE_SIZE,numRecord,imgTotRecord); 
	       }

         list="\n"+h.getTable("AFCFormTABLE","5","0","100%",h.getTBODY(contentTBODY));
         list+="\n</div>";
         list+="<div class=\"navigatore\" id=\"navigatore\">"+h.getTable("AFCFormTABLE","0","0","100%",navigator)+"</div>";

         log.log_info("Fine Visualizzazione della lista di oggetti della WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
        
         return list;
  }
  
  /**
   * Costruzione della lista di oggetti della WorkArea
   * in formato XML
   * 
   * @return String  XML della lista di oggetti
   * 
	 */
 private String getXMLListaOggetti() throws Exception
 {
         String xlist="";
         String sql=xml_select();
         String nomeOggetto,tipoOggetto,idOggetto,dataagg,profilo;
         Document doc = DocumentHelper.createDocument();
	     Element root=null;
           
	      if (vu.Global.PRINT_WAREA.equals("S")) {
	   		 System.out.println("[INFO WorkArea - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
	   	  }
	       
	      log.log_info("Inizio Costruzione XML della lista di oggetti della WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	      
	      try
	      {    
	    	 elpsTime.start("Esecuzione Select WorkArea",sql);
        	 log.log_info("Esecuzione Select WorkArea- ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - SQL= "+sql.toString());
        	 dbOp.setStatement(sql);
        	 if(idQuery!=null){
          	   dbOp.setParameter(":id",idCartella);  
          	   dbOp.setParameter(":idCartAppartenenza",idCartAppartenenza);
             }	
             else
             {
          	   dbOp.setParameter(":id",idCartella.substring(1,idCartella.length()));            	  			
             }  
                     	 
             dbOp.execute();
             elpsTime.stop();
             ResultSet rs = dbOp.getRstSet();
             if ( rs != null ) 
             {
               root=DocumentHelper.createElement("ROOT");
               while(rs.next())
               {
            	 nomeOggetto=rs.getString("NOME");
            	 idOggetto=rs.getString("ID_OGGETTO");
            	 tipoOggetto=rs.getString("TIPO_OGGETTO");
            	 dataagg=rs.getString("DATAAGG");
            	 profilo=rs.getString("PROFILO");
            	 Element row = DocumentHelper.createElement("ROW");		 
              	 row.addAttribute("ID",idOggetto);
              	 Element nome = DocumentHelper.createElement("NOME");
             	 nome.setText(nomeOggetto);
             	 Element id_oggetto = DocumentHelper.createElement("ID_OGGETTO");
              	 id_oggetto.setText(idOggetto);
              	 Element tipo_oggetto = DocumentHelper.createElement("ID_OGGETTO");
              	 tipo_oggetto.setText(tipoOggetto);
             	 Element data_agg = DocumentHelper.createElement("DATAAGG");
              	 data_agg.setText(dataagg);
              	 Element idprofilo = DocumentHelper.createElement("PROFILO");
              	 idprofilo.setText(profilo);
            	 String mono="";
            	 if(tipoOggetto.equals("D"))
           		   mono=idOggetto;
           		 else
           		   mono=profilo;
           	      log.log_info("Inizio Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
   	              String monoRecord=getRigheMonoRecord(mono,req);
   	              log.log_info("Fine Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
   	              if(monoRecord.equals(""))
     	          {
         	        if (tipoOggetto.equals("D"))
         		      monoRecord="Documento n. "+ idOggetto;
         	        else
         		      monoRecord=nomeOggetto;
     	          }   
     	          Element blocco = DocumentHelper.createElement("BLOCCO");
     	          blocco.setText(monoRecord); 
            	 row.add(nome);
     	         row.add(id_oggetto);
     	         row.add(tipo_oggetto);
            	 row.add(data_agg);
            	 row.add(idprofilo);
            	 row.add(blocco);
            	 root.add(row);
               }               
             }  
             doc.add(root);
  		     xlist=doc.asXML();
          }
	      catch (Exception e) { 
	    	  throw e;
	      }
	      log.log_info("Fine Costruzione XML della lista di oggetti della WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
          return xlist;
 }
 
  
  /**
   * Visualizzazione della lista di oggetti della WorkArea
   * 
   * @return String 	XHTML della lista di oggetti
   * 
  */
  private String getListaOggettiStampa() throws Exception
  {
         String tbody; 
         String sql;
         
         try 
         {
           sql=buildSelectStampa();
	       if (vu.Global.PRINT_WAREA.equals("S")) {
	   	     System.out.println("[INFO WorkArea - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
	   	   }
	       log.log_info("Inizio Costruzione della lista di oggetti della WorkArea per l'operazione di Stampa - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	       tbody=this.getRecordsRowsStampa(sql);
           log.log_info("Fine Costruzione della lista di oggetti della WorkArea per l'operazione di Stampa - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
         }
         catch ( Exception e ) {
      	  throw e;
         } 
         return tbody;
 }
   
   /**
     * Costruzione di ogni singola riga della tabella
     * 
     * @return String 	HTML di ogni singolo elemento della lista
     * 
	 */
   private String getRecordsRows(String sql) throws Exception
   { 
           String result = "";
           int nPages=Integer.parseInt(page);
           int startPos=((nPages-1)*PAGE_SIZE+1);
           ArrayList<RowList> rowsList = new ArrayList<RowList>();
           
           /** Nel caso di oggetto Query */ 
           if(idQuery!=null)
        	 startPos=1;
                          
           ResultSet rs = null;
           try {
        	 elpsTime.start("Esecuzione Select WorkArea",sql);
       	     log.log_info("Esecuzione Select WorkArea- ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - SQL= "+sql.toString());
             dbOp.setStatement(sql);
             if(idQuery!=null){
          	   dbOp.setParameter(":id",idCartella);  
          	   dbOp.setParameter(":idCartAppartenenza",idCartAppartenenza);
             }	
             else
             {
          	   dbOp.setParameter(":id",idCartella.substring(1,idCartella.length()));  
             }
             
             dbOp.execute();
             elpsTime.stop();
             rs = dbOp.getRstSet();

             log.log_info("Inizio Costruzione ArrayList - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]");
             if ( PAGE_SIZE > 0 ) {
                 rowsList = getRowsList(rs, startPos, PAGE_SIZE);
             }
             log.log_info("Fine Costruzione ArrayList - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - Size= "+rowsList.size());

             log.log_info("Inizio Costruzione HTML Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - Numero di Righe per Pagina="+PAGE_SIZE);
             result = getRows(rowsList);
             //if ( PAGE_SIZE > 0 )
             //  result = getRows(rs, startPos, PAGE_SIZE );
             log.log_info("Fine Costruzione HTML Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - Numero di Righe per Pagina="+PAGE_SIZE);
             
           }
           catch ( Exception e ) {
        	  e.printStackTrace();
        	   throw e;
        	  
           } 
           return result;
   }
   
   
   /**
    * Costruzione di ogni singola riga della tabella
    * 
    * @return String 	HTML di ogni singolo elemento della lista
    * 
	 */
  private String getCartRecordsRows(String sql,ResultSet rs) throws Exception
  { 
          StringBuffer result = new StringBuffer("");
          int pageSize=PAGE_SIZE;
          int nPages=Integer.parseInt(page);
          int startPos=((nPages-1)*pageSize+1);
          boolean effettuaExecuted=false;
          int size=0;
          int dim=0,dimblocco=0;
          
          log.log_info("Inizio Costruzione Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
           
          if(vCartObjects!=null)
          {
        	  if(isExitsSucc)
        	   size=vCartObjects.size()-1; 
        	  else
        	   size=vCartObjects.size(); 
          }
       
          if(submit)
          { 
        	  vCartObjects=null; 
        	  String tp=page+"";
      		  dim=Integer.parseInt(tp.substring(0,(tp.length()-1))+"0");
      		  if(dim==0)
      		    dimblocco=PAGE_SIZE*10;
      		  else
                dimblocco=PAGE_SIZE*(dim+10);
           }
          else
        	dimblocco=PAGE_SIZE*10;  
          
          
          if(( ((nPages%10)==1) && ((nPages*pageSize)>size) ) ||  (vCartObjects==null))
          {
        	 rs=null;
        	 effettuaExecuted=true;
        	 if((vCartObjects!=null) && (vCartObjects.size()!=0))
        	  vCartObjects.removeElementAt(size-1);
        	 
        	 if((vCartObjects==null))
          	   startPos=1;
        	 
        	 if(nPages==1)
        	  vCartObjects=null;
          }
          
          if(effettuaExecuted)
          {
        	  try {
             	  elpsTime.start("Esecuzione Select WorkArea",sql);
             	  log.log_info("Esecuzione Select WorkArea- ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - SQL= "+sql.toString());
             	  dbOp.setStatement(sql);
             	 
	      		  if(idQuery!=null){
	            	dbOp.setParameter(":id",idCartella);  
	      			dbOp.setParameter(":idCartAppartenenza",idCartAppartenenza);
	      		  }	
	              else
	              {
	            	dbOp.setParameter(":id",idCartella.substring(1,idCartella.length()));  
	              }             	  
             	 
                  dbOp.execute();
                  elpsTime.stop();
                  rs = dbOp.getRstSet();
                  this.rsCartObjects=rs;
                  if(vCartObjects==null)
                   vCartObjects=new Vector();
                  vlistID=new Vector();
                  vCartObjects = getCartRows(rs,startPos,dimblocco);
                }
                catch ( Exception e ) {
                	throw e;
                } 
          }
          
          if((vCartObjects!=null) && (vCartObjects.size()!=0))
          {
        	  int i=((nPages-1)*pageSize+1)-1;//startPos-1;
        	  int count=0;
        	  int vSize;
        	  if(isExitsSucc)
        		vSize=vCartObjects.size()-1; 
           	  else
           		vSize=vCartObjects.size(); 
        	  
        	  //Costruzione del vettore di id per i MonoRecords
        	  while((count<PAGE_SIZE) && (i<vSize)) 
        	  {
        		  Properties p=(Properties) vCartObjects.elementAt(i);
        		  
        		  if(p.get("TIPO_OGGETTO").equals("D"))
        			vlistIDDocs.add(p.get("ID_OGGETTO").toString());
        		  else
        		    vlistIDDocs.add(p.get("profilo").toString());
        		  
        		  i++;
	        	  count++;
        	  }
        	  
        	  if(JDMS_LINK.equals("S"))
        	   costruisciJDMSLink(vlistIDDocs);
        	  
        	  log.log_info("Inizio Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          pHTMLRecord=getRigheMonoRecords(vlistIDDocs,req,false);
	          log.log_info("Fine Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	         
        	  
        	  log.log_info("Inizio Costruzione HTML Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - Numero di Righe per Pagina="+PAGE_SIZE);
              
        	  //Azzerramaneto dei contatori
        	  i=((nPages-1)*pageSize+1)-1;
        	  count=0;
        	  while((count<PAGE_SIZE) && (i<vSize)) 
        	  {
        		  Properties p=(Properties) vCartObjects.elementAt(i);
        		  
        		  if(p.get("TIPO_OGGETTO").toString().equals("L")){
        			  
        			  String ret="1";  
	            	  String nome_funzione = p.get("FUNZIONE_LETTURA").toString();
	 	 		      if(nome_funzione!=null && !nome_funzione.equals(""))
	 	 		      	 ret = executeFunzioneCollegamentoEsterno(nome_funzione);
	            	  
	 		          if(ret.equals("1"))        			  
        			    result.append(this.getRigaCollegamentiEsterni(p.get("NOME").toString(),p.get("URL").toString(),p.get("TIPO_LINK").toString(),
        					  		p.get("TOOLTIP").toString(),p.get("ID_OGGETTO").toString(),p.get("ICONA").toString(),
        					  		p.get("NOMEICONA").toString(),p.get("DATAAGG").toString(),p.get("UTENTEAGG").toString(),i+1));
        		  }
        		  else {
        		  
        		  /** Controllo Competenza */
        		  int[] vComp=getObjectCompetenza(p.get("ID_OGGETTO").toString(),p.get("TIPO_OGGETTO").toString());  
               	  int Lettura=vComp[0];
        		  int Modifica=vComp[1];
        		  int Elimina=vComp[2];
        		  int Competenze=vComp[3];
        		  int lettura_allegati = Integer.parseInt(p.get("lettura_allegati").toString());
        		  int modifica_allegati = Integer.parseInt(p.get("modifica_allegati").toString());
        		  
        			  
        		  if(Lettura==1)
        		  {	  
        			  if(JDMS_LINK.equals("S")){
        			    result.append(this.getRigaPersonalizzata(count,p.get("TIPO_OGGETTO").toString(),p.get("ID_CART_PROV").toString(),
		       		               	  p.get("COLLEGAMENTO").toString(),p.get("NOME").toString(),
		       		               	  p.get("ID_OGGETTO").toString(),p.get("profilo").toString(),
		       		               	  p.get("ICONA").toString(),p.get("NOMEICONA").toString(),Long.parseLong(p.get("ordina").toString()),
		       		               	  p.get("RICERCAMODULISTICA").toString(),p.get("CM").toString(),
		       		               	  p.get("AREA").toString(),p.get("CR").toString(),
		       		               	  p.get("ID_TIPODOC").toString(),p.get("DATAAGG").toString(),
		       		               	  p.get("UTENTEAGG").toString(),p.get("CONSERVAZIONE").toString(),p.get("ARCHIVIAZIONE").toString(),
		       		               	  Modifica,Elimina,Competenze,lettura_allegati,modifica_allegati,
		       		               	  p.get("check_livello").toString(),p.get("competenze_allegati").toString(),i+1));
        			  }
        			  else {
        			    result.append(this.getRiga(count,p.get("TIPO_OGGETTO").toString(),p.get("ID_CART_PROV").toString(),
	                		          p.get("COLLEGAMENTO").toString(),p.get("NOME").toString(),
	                		          p.get("ID_OGGETTO").toString(),p.get("profilo").toString(),
	                		          p.get("ICONA").toString(),p.get("NOMEICONA").toString(),Long.parseLong(p.get("ordina").toString()),
	                		          p.get("RICERCAMODULISTICA").toString(),p.get("CM").toString(),
	                		          p.get("AREA").toString(),p.get("CR").toString(),
	                		          p.get("ID_TIPODOC").toString(),p.get("DATAAGG").toString(),
	                		          p.get("UTENTEAGG").toString(),p.get("CONSERVAZIONE").toString(),p.get("ARCHIVIAZIONE").toString(),
	                		          Modifica,Elimina,Competenze,lettura_allegati,modifica_allegati,
	                		          p.get("check_livello").toString(),p.get("competenze_allegati").toString(),i+1));
        			  }	
        		  }
        		  
        		  }
        		   
	              i++;
	        	  count++;
        		  
        	  }
         	  
         	 log.log_info("Fine Costruzione HTML Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - Numero di Righe per Pagina="+PAGE_SIZE);
             
          }
         log.log_info("Fine Costruzione Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
         return result.toString();
  }
    
  
  /**
   * Costruzione HTML di ciascuna riga della lista
   * 
   * @param  nrecords		numero di elementi    	 
   * 
	 */
 private String getRigaCollegamentiEsterni(String nome,String url,String tipo_link,String tooltip,String id_oggetto,String icona,String nome_icona,String data_agg,String utente_agg,int count) throws Exception 
 {
         String row,contentTR,contentTD;
         String classTD="";
         String classTR="AFCDataTD";
         if (count%2==0)  classTR="AFCAltDataTD"; else classTR="AFCDataTD";
         String snome ="",smodifica=_IMGVUOTO;
         
         
         if(tipo_link.equals("I")){
        	 //snome = h.getTable("100%",h.getTR(h.getTD("center","5%",h.getAncore("#","linkOggetto('"+url+"');","",h.getImg("",tooltip,"./icone/"+icona+"/"+nome_icona)))+h.getTD("","95%",nome)));
        	 snome = h.getTable("100%",h.getTR(h.getTD("center","5%",h.getAncore(url,"try{parent.setTipoVisualizzazioneModale(false);}catch(e){};","",h.getImg("",tooltip,"./icone/"+icona+"/"+nome_icona)))+h.getTD("","95%",nome)));
         }
         else {
        	 String onclick=h.popup(url,nome.replaceAll(" ",""));
          	 snome = h.getTable("100%",h.getTR(h.getTD("center","5%",h.getAncore("#",onclick,"",h.getImg("",tooltip,"./icone/"+icona+"/"+nome_icona)))+h.getTD("","95%",nome)));
         }
         
         if(req.getSession().getAttribute("Ruolo")!=null && req.getSession().getAttribute("Ruolo").equals("AMM"))
          smodifica = h.getAncore("#","if(linkOggettoPopup()){"+h.CollegamentoEsternoPop(id_oggetto,idCartella.substring(1,idCartella.length()))+"}","",h.getImgHand("Modifica il Collegamento esterno",_LINKESTERNO));
         
         contentTD=h.getInput("WIDTH:  2px;  HEIGHT:  4px","hidden","1","L","TIPO_OGGETTO");
         contentTD+=h.getInput("WIDTH:  1px;  HEIGHT:  8px","hidden","2",id_oggetto,"ID_OGGETTO");
           
         row=h.getTDClass(classTD,"2%",h.getInput("","IDSeleziona","checkbox",id_oggetto,"Seleziona",true));
	     row+=h.getTDClass(classTD,"88%",snome+contentTD);
	     row+=h.getTDClass(classTD,"10%",
	    	  h.getTable("60%","center",
	    	  h.getTR(h.getTD("","",_IMGVUOTO+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",_IMGVUOTO+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",_IMGVUOTO+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",_IMGVUOTO+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",_IMGVUOTO+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",smodifica+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",_IMGVUOTO+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	    	  		+h.getTD("","",_IMGVUOTO)))
	    	  		+h.getTable("100%",h.getTR(h.getTD("top","",h.getP("right","textData",data_agg+h.getNbsp()+utente_agg)))));
	     contentTR=h.getTR(classTR,"15","this.className = 'AFCHoverTR'","this.className = '"+classTR+"'",row);
	     
	 
	     
         /** Costruzione della lista di ICONE da verificare l'esistenza e l'eventuale caricamento nel file system */
        if(icona!=null)
        {	  
         if(listaICONE.indexOf(icona)==-1)
         {
      	 if(listaICONE.equals(""))
      	   listaICONE+=icona;
      	 else
      	   listaICONE+=","+icona;	 
         }
        }
	    return contentTR+"\n";        
 }
  
    
  /**
   * Costruzione di ogni singola riga dell'XHTML
   * 
   * @return String TBODY per XHTML
   * 
  */
  private String getRecordsRowsStampa(String sql) throws Exception
  { 
          ResultSet rs=null;   
          String tbody="<tbody>";
          boolean records=false;
          Vector vDocs=null;
          Vector vIDDocs=null;
          Vector vHTMLDocs=null;
          
          log.log_info("Inizio Costruzione Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
          
          try
          {
             elpsTime.start("Esecuzione Select WorkArea",sql);
             log.log_info(" Esecuzione Select WorkArea - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]");// - SQL= "+sql.toString());
             dbOp.setStatement(sql);
             if(idQuery!=null){
          	   dbOp.setParameter(":id",idCartella);  
          	   dbOp.setParameter(":idCartAppartenenza",idCartAppartenenza);
             }	
             else
             {
          	   dbOp.setParameter(":id",idCartella.substring(1,idCartella.length()));  
             } 
                          
             dbOp.getStmSql().setQueryTimeout(timeout); 
  	         dbOp.execute(); 
 	         rs = dbOp.getRstSet();
             vDocs  = new Vector();
             vIDDocs = new Vector();
             vHTMLDocs = new Vector();
             
             while ( rs.next() )
             {
             	records=true;
             	Properties pDocs=new Properties(); 
             	if(rs.getString("TIPO_OGGETTO").equals("D"))
             	{
             		pDocs.put("ID_OGGETTO",rs.getString("ID_OGGETTO"));
             		vIDDocs.add(rs.getString("ID_OGGETTO"));
             	}
             	else
             	{
             		pDocs.put("ID_OGGETTO",rs.getString("profilo"));
             		vIDDocs.add(rs.getString("profilo"));
             	}
             	pDocs.put("TIPO_OGGETTO",rs.getString("TIPO_OGGETTO"));
             	pDocs.put("NOME",rs.getString("NOME"));
             	pDocs.put("UTENTE",rs.getString("UTENTEAGG"));
             	pDocs.put("DATA",rs.getString("DATAAGG"));
             	
             	vDocs.add(pDocs);
             }
             
           }
           catch ( Exception e ) {
         	  if (e.getMessage().indexOf("ORA-01013")!=-1) {
        		      log.log_error("Esecuzione Ricerca Stampa: Tempo max di esecuzione della query raggiunto!");
        		      throw e;
         	  }  
           } 
       
           //Nel caso di nessun Record
           if(!records)
         	  tbody+="<tr><td>Nessun Documento trovato</td></tr>";
           else 
           {
         	log.log_info("Inizio Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
         	vHTMLDocs=getRigheMonoRecordsFromStampa(vIDDocs,req,true); 
     	    log.log_info("Fine Costruzione MonoRecords - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
         
     	    
     	    for(int i=0;i<vDocs.size();i++)
     	    {
     	    	 Properties p=(Properties) vDocs.elementAt(i);
     	    	 String monoRecord="";
     	    	 
     	    	 if(vHTMLDocs!=null && vHTMLDocs.size()!=0)
     	         {
     	    		 monoRecord=vHTMLDocs.get(i).toString();
     	    		 
     	    		 if(monoRecord.equals("")) 
           	         {
               	      if (p.get("TIPO_OGGETTO").equals("D"))
               		   monoRecord="Documento n. "+ p.get("ID_OGGETTO");
               	      else
               		   monoRecord=p.get("NOME").toString();
           	         }   
     	    	 }
     	    	 else
     	    	 {
     	    	    if (p.get("TIPO_OGGETTO").equals("D"))
                       monoRecord="Documento n. "+ p.get("ID_OGGETTO");
                  	else
                  	  monoRecord=p.get("NOME").toString();
     	    	 }
     	    	monoRecord=monoRecord.replaceAll("&nbsp;"," ");
     	        if(visDataUtente.equals("S"))
     	          tbody+="<tr><td class=\"table-main\">"+monoRecord+"</td><td class=\"table-main\">"+p.get("UTENTE")+" il "+p.get("DATA")+"</td></tr>";
     	        else 
     	          tbody+="<tr><td class=\"table-main\">"+monoRecord+"</td></tr>";
       	    }
     	   }
           tbody+="</tbody>";
           log.log_info("Fine Costruzione Righe - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
           return tbody;
  }

  /**
    * Controllo dell'esistenza ed eventuale distribuizione delle icone
 	*/
  private void gestioneControlloIcone() throws Exception
  {
          try 
          {
	          if(listaICONE!=null && !(listaICONE.equals(""))) 
	          {
	        	String[] slistaIcone = listaICONE.split(",");
	        	for(int i=0;i<slistaIcone.length;i++)
	        		controlloIcona(slistaIcone[i]);
	          }
          }
	      catch ( SQLException e ) {
	         throw e;
	      }  
  }     
  
  private void controlloIcona(String icona) throws Exception
  {
          String sql,nome_icona,pathIcona;
          //IDbOperationSQL dbOpSQL=null;
          File ffile=null, fdir=null;
          java.sql.Date d; 
          Time t;
          long data;
          InputStream  risorseBlob;
 
          try 
          {	
            sql="select nome,data_aggiornamento,risorsa from icone where icona = :ICONA";

            dbOp.setStatement(sql);
            dbOp.setParameter(":ICONA",icona);
            dbOp.execute();
			ResultSet rst = dbOp.getRstSet();
			
			if (!rst.next()) {
		      log.log_error("CCS_Workarea::controlloIcona() - Icona ["+icona +"] non trovata sul DB");          
		      throw new Exception("Icona ["+icona+"] non trovata sul DB");
		    }
			
			nome_icona = rst.getString("nome");			
			
			d= rst.getDate("data_aggiornamento");
			t= rst.getTime("data_aggiornamento");
			if (d != null) 
			  data = d.getTime() + t.getTime();
			else
			  data = 0;
						
			/** Costruzione del path dell'icona */
			pathIcona = req.getSession().getServletContext().getRealPath("")+File.separator+"common"+File.separator+"icone"+File.separator+icona;
            
			/** Eventuale creazione della directory su cui salvare l'icona */
		    fdir = new File(pathIcona);
		   // if (!fdir.isDirectory()) {
		        fdir.mkdirs(); 
		    //}
			
		    /** Creazione del file */
		    ffile = new File(pathIcona+File.separator+nome_icona);
		   
			/** Controllo esistenza e data di ultima modifica */
			if (!ffile.exists() || ffile.lastModified() != data)
			{
				try {
					risorseBlob = dbOp.readBlob("risorsa");
				}
				catch (Exception e) {
					 log.log_error("CCS_Workarea::controlloIcona() - Problemi di caricamento della risorsa icona.InputStream vuoto. Errore: "+e.getMessage());              
				     throw new Exception("Problemi di caricamento della risorsa icona.InputStream vuoto. Errore: "+e.getMessage());
				}
			  
			    if (risorseBlob == null) {
			        log.log_error("CCS_Workarea::controlloIcona() - Problemi di caricamento della risorsa icona.InputStream vuoto.");              
			        throw new Exception("Problemi di caricamento della risorsa icona.InputStream vuoto.");
			    }
			  
			    BufferedInputStream inputS = new BufferedInputStream(risorseBlob);
			    FileOutputStream fos = new FileOutputStream(ffile);
			    byte buf[] = new byte[1];     
			  
			    while( inputS.read(buf) != -1) 
			    { 
			       fos.write(buf);
			    }
			   
			    fos.flush();
			    fos.close();
			    inputS.close();
			    ffile.setLastModified(data);
			      
     	    }
	      }
	      catch ( SQLException e ) {
	        throw e;
	      }  
  }   
  
  /**
   * Recupero parametro dalla tabella PARAMETRI
   * del tipo_moedllo=@DMSERVER@
   * 
   * @param 			nome parametro
   * @return String 	valore
   * 
	 */
 private String retriveParametro(String parametro) throws Exception
 {
         String sql="",rstPar=null;
         log.log_info("Inizio Recupero Parametro: "+parametro+"  - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
         try
         {
            ResultSet rs=null;
            sql=" SELECT VALORE FROM PARAMETRI ";
            sql+=" WHERE CODICE = :PARAMETRO ";
            sql+=" AND TIPO_MODELLO='@DMSERVER@'";
            log.log_info("PATH_FOLDER - SQL - "+sql);
            dbOp.setStatement(sql);
            dbOp.setParameter(":PARAMETRO",parametro);
     		dbOp.execute();
     	    rs=dbOp.getRstSet();
            if (rs.next()) 
              rstPar=rs.getString(1);
            else
              rstPar="N";	
         }
         catch (Exception e) {   
      	   log.log_error("Recupero Parametro: - SQL: "+sql);
      	   throw e;
         }  
         log.log_info("Fine Recupero Parametro: - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
        return  rstPar;   
 }   
  
   /**
     * Costruzione path folder a partire dall'oggetto in questione
     * 
     * @return String 	HTML path folder
     * 
	 */
   private String getNomeOggettoAttuale() throws Exception
   {
           String sql="",nome=null;
           String idC=idCartella;
           String idQ=idQuery;
           String f_pathC="",f_pathQ="";
           
           if(idCartAppartenenza==null)
        	   idCartAppartenenza = req.getParameter("idCartAppartenenza");
           
           if(idCollegamento==null)
        	   idCollegamento = req.getParameter("idCollegamento");
                    
           
           log.log_info("Inizio Costruzione Path_Folder - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
           
           if (idC.indexOf("C")!=-1)
        	 idC=idCartella.substring(1,idCartella.length());
         
           if(idCartAppartenenza!=null && idCartAppartenenza.equals(""))
        	   idCartAppartenenza = idC;
        	
           /** Attivazione dle multilingue */
           if(jdmsML!=null && !jdmsML.equals("")){
        	   
               if((idCollegamento!=null) && (!idCollegamento.equals("")) &&  (!idCollegamento.equals("null")))
               {
            	  f_pathC="F_Path_collegamento_ml("+idCollegamento+",''||to_char(c.id_cartella)||'','W','"+this.utente+"','"+jdmsML+"')";
                  f_pathQ="F_Path_collegamento_ml("+idCollegamento+","+idCartAppartenenza+",'W','"+this.utente+"','"+jdmsML+"')";
               }
               else
               {
                 f_pathC="F_Path_Folder_ml(c.id_cartella,'W','"+this.utente+"','"+jdmsML+"')";
                 f_pathQ="F_Path_Folder_ml("+idCartAppartenenza+",'W','"+this.utente+"','"+jdmsML+"')";
               }
           }
           else {
               if((idCollegamento!=null) && (!idCollegamento.equals("")) &&  (!idCollegamento.equals("null")))
               {
            	  f_pathC="F_Path_collegamento("+idCollegamento+",''||to_char(c.id_cartella)||'','W','"+this.utente+"')";
                  f_pathQ="F_Path_collegamento("+idCollegamento+","+idCartAppartenenza+",'W','"+this.utente+"')";
               }
               else
               {
                 f_pathC="F_Path_Folder(c.id_cartella,'W','"+this.utente+"')";
                 f_pathQ="F_Path_Folder("+idCartAppartenenza+",'W','"+this.utente+"')";
               }
           }
           
           String campoC="'<td>"+h.getImg(_CARTELLAGDC)+"&nbsp;</td><td>"+h.getB("'||"+f_pathC+"||'")+"</td>' nomeOggettoAttuale";
           String campo,campoQ;
           
           if(jdmsML!=null && !jdmsML.equals("")){
        	   campoQ="'<td>"+h.getImg(_QRYSEARCH)+"&nbsp;</td><td>"+h.getB("'||"+f_pathQ+"||'&nbsp;::&nbsp;'||GDM_UTILITY.F_MULTILINGUA(q.nome,'"+jdmsML+"')||'")+"</td>' nomeOggettoAttuale";
        	   campo="'<td>"+h.getImg(_CARTELLAGDC)+"&nbsp;</td><td><b>'||GDM_UTILITY.F_MULTILINGUA('Cartella','"+jdmsML+"')||'</b></td>'";
           }
           else {
        	   campoQ="'<td>"+h.getImg(_QRYSEARCH)+"&nbsp;</td><td>"+h.getB("'||"+f_pathQ+"||'&nbsp;::&nbsp;'||q.nome||'")+"</td>' nomeOggettoAttuale"; 
        	   campo="'<td>"+h.getImg(_CARTELLAGDC)+"&nbsp;</td><td>"+h.getB("Cartella")+"</td>'";
           }
          
           if (!(idCartella == null))
            idC="'"+idC+"'";
         
           if (!(idQuery == null))
            idQ="'"+idQuery+"'";
      
           try
           {
              ResultSet rs=null;
              nome="Cartella UTENTE";
		      sql=Select(campoC,"cartelle c ","c.id_cartella="+idC+" ");
              sql+="UNION ";
              sql+=Select(campoQ,"query q ","id_query="+idQ+" ");
              sql+="UNION ";
              sql+=Select(campo,"DUAL ",idC+" is null and "+idQ+" is null");
              log.log_info("PATH_FOLDER - SQL - "+sql);
              dbOp.setStatement(sql);
       		  dbOp.execute();
       	      rs=dbOp.getRstSet();
              if (rs.next()) 
                nome=rs.getString(1);
              
              String[] sNome=null;
              int offset=475; 
              
              if(nome.length()>offset)
              {
            	  sNome=nome.split("</a>"); 
            	  nome="";
            	  
            	  if(sNome.length<5)
            	  {
            		  if(sNome[0].indexOf("<img")==-1)
            			nome+="...";  
            		  
            		  for(int i=0;i<=sNome.length-1;i++)
            			nome+=sNome[i]+"</a>";   
            	  }
            	  else
            	  {
            		nome="<b>..."+sNome[sNome.length-5]+"</a>"+sNome[sNome.length-4]+"</a>"+sNome[sNome.length-3]+"</a>"+sNome[sNome.length-2]+"</a></b>";
            		
            		if(nome.length()>offset)
            		  nome="<b>..."+sNome[sNome.length-4]+"</a>"+sNome[sNome.length-3]+"</a>"+sNome[sNome.length-2]+"</a></b>";
            	  } 
              }
           }
           catch (Exception e) {   
        	   log.log_error("Path_Folder - SQL: "+sql);
        	   throw e;
           }  
           
           nome = "<td><font color='#000000'>"+nome+"</font></td>";
           
           log.log_info("Fine Costruzione Path_Folder - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
           return nome;   
   }   
   
   /**
     * Costruzione delle righe comprese tra un intervallo
     * 
     * @param  rs  			ResultSet 
     * @param  start e max 	intervallo di estrapolazione righe del ResultSet	
     * @return String 		righe comprese nell'intervallo
     * 
	 */
   private String getRows(ResultSet rs, int start, int max) throws Exception
   {
	       StringBuffer rows = new StringBuffer("");
	       int count=1;
	       int iCounter = 0;
	       int i=0;
	       String tipoDoc;
	       
	       try
	       {
	    	   if ( rs != null ) 
	    	   {
	               if ( start < 1 ) { start = 1; }
	               if ( max < 1 ) { max = 1; }
	               
	               /** Posizionamento alla tupla con indice start */
	               if (count != start)
	               { 
	                 while ( rs.next() ) {
	                   if(count++ >= start-1) break;
	                 }
	               } 
	               /** Generazione delle righe comprese tra start e pagesize */
	               isExitsRecords = false;
	               while ( rs.next() ) {
	            	    if (iCounter++ >= max) break;
	            	    	            	    
		            	if(rs.getString("TIPO_OGGETTO").equals("D"))
		            	  tipoDoc = this.getTipoDoc(rs.getString("CM")); 
		            	else
		            	  tipoDoc = rs.getString("CM");	
		            	    
		            	if(JDMS_LINK.equals("S"))
		            	  rows.append(this.getRigaPersonalizzata(i,rs.getString("TIPO_OGGETTO"),rs.getString("ID_CART_PROV"),rs.getString("COLLEGAMENTO"),rs.getString("NOME"),rs.getString("ID_OGGETTO"),rs.getString("profilo"),rs.getString("ICONA"),rs.getString("ICNOME"),rs.getLong("ordina"),rs.getString("RICERCAMODULISTICA"),tipoDoc,rs.getString("AREA"),rs.getString("CR"),rs.getString("ID_TIPODOC"),rs.getString("DATAAGG"),rs.getString("UTENTEAGG"),rs.getString("CONSERVAZIONE"),rs.getString("ARCHIVIAZIONE"),rs.getInt("Modifica"),rs.getInt("Elimina"),rs.getInt("Competenze"),rs.getInt("lettura_allegati"),rs.getInt("modifica_allegati"),rs.getString("check_livello"),rs.getString("competenze_allegati"),iCounter));
		            	else 
		            	  rows.append(this.getRiga(i,rs.getString("TIPO_OGGETTO"),rs.getString("ID_CART_PROV"),rs.getString("COLLEGAMENTO"),rs.getString("NOME"),rs.getString("ID_OGGETTO"),rs.getString("profilo"),rs.getString("ICONA"),rs.getString("ICNOME"),rs.getLong("ordina"),rs.getString("RICERCAMODULISTICA"),tipoDoc,rs.getString("AREA"),rs.getString("CR"),rs.getString("ID_TIPODOC"),rs.getString("DATAAGG"),rs.getString("UTENTEAGG"),rs.getString("CONSERVAZIONE"),rs.getString("ARCHIVIAZIONE"),rs.getInt("Modifica"),rs.getInt("Elimina"),rs.getInt("Competenze"),rs.getInt("lettura_allegati"),rs.getInt("modifica_allegati"),rs.getString("check_livello"),rs.getString("competenze_allegati"),iCounter));
	                    isExitsRecords = true;     
	                    i++;
	               }
	    	   }
	       	}
	       	catch ( SQLException e ) {
	       		throw e;
	       	}
	       	return rows.toString();
   }

    private String getRows(ArrayList<RowList> lista) throws Exception
    {
        StringBuffer rows = new StringBuffer("");

        try
        {   /** Generazione delle righe comprese tra start e pagesize */
            isExitsRecords = false;
             for(int i=0;i<lista.size();i++){

                 RowList row = lista.get(i);
                 if(JDMS_LINK.equals("S")) {
                     rows.append(this.getRigaPersonalizzata(i, row.getTipoOggetto(), row.getIdCartProv(), row.getCollegamento(),row.getNome(),
                             row.getIdOggetto(),row.getProfilo(), row.getIcona(),row.getNomeIcona(),row.getOrdina(), row.getRicercaMod(),
                             row.getTipoDoc(),row.getArea(), row.getCr(),row.getIdtipoDoc(), row.getDataAgg(), row.getUtenteAgg(),
                             row.getCons(), row.getArch(), row.getModifica(), row.getElimina(), row.getCompetenze(), row.getLetturaAllegati(),
                             row.getModificaAllegati(), row.getCheckLivello(), row.getCompetenzeAllegati(), row.getCount()));
                 }
                 else {
                        rows.append(this.getRiga(i, row.getTipoOggetto(), row.getIdCartProv(), row.getCollegamento(),row.getNome(),
                                row.getIdOggetto(),row.getProfilo(), row.getIcona(),row.getNomeIcona(),row.getOrdina(), row.getRicercaMod(),
                                row.getTipoDoc(),row.getArea(), row.getCr(),row.getIdtipoDoc(), row.getDataAgg(), row.getUtenteAgg(),
                                row.getCons(), row.getArch(), row.getModifica(), row.getElimina(), row.getCompetenze(), row.getLetturaAllegati(),
                                row.getModificaAllegati(), row.getCheckLivello(), row.getCompetenzeAllegati(), row.getCount()));

                 }
                isExitsRecords = true;
                //i++;
            }

        }
        catch ( SQLException e ) {
            throw e;
        }
        return rows.toString();
    }

    private ArrayList getRowsList(ResultSet rs, int start, int max) throws Exception
    {
        int count=1;
        int iCounter = 0;
        String tipoDoc;
        ArrayList<RowList> rowsList = new ArrayList<RowList>();

        try
        {
            if ( rs != null )
            {
                if ( start < 1 ) { start = 1; }
                if ( max < 1 ) { max = 1; }

                /** Posizionamento alla tupla con indice start */
                if (count != start)
                {
                    while ( rs.next() ) {
                        if(count++ >= start-1) break;
                    }
                }
                /** Generazione delle righe comprese tra start e pagesize */
                while ( rs.next() ) {
                    if (iCounter++ >= max) break;

                    RowList row = new RowList();

                    if(rs.getString("TIPO_OGGETTO").equals("D"))
                        tipoDoc = this.getTipoDoc(rs.getString("CM"));
                    else
                        tipoDoc = rs.getString("CM");

                    row.setTipoDoc(tipoDoc);
                    row.setIdtipoDoc(rs.getString("ID_TIPODOC"));
                    row.setTipoOggetto(rs.getString("TIPO_OGGETTO"));
                    row.setIdCartProv(rs.getString("ID_CART_PROV"));
                    row.setCollegamento(rs.getString("COLLEGAMENTO"));
                    row.setNome(rs.getString("NOME"));
                    row.setIdOggetto(rs.getString("ID_OGGETTO"));
                    row.setProfilo(rs.getString("profilo"));
                    row.setIcona(rs.getString("ICONA"));
                    row.setNomeIcona(rs.getString("ICNOME"));
                    row.setOrdina(rs.getLong("ordina"));
                    row.setRicercaMod(rs.getString("RICERCAMODULISTICA"));
                    row.setArea(rs.getString("AREA"));
                    row.setCr(rs.getString("CR"));
                    row.setDataAgg(rs.getString("DATAAGG"));
                    row.setUtenteAgg(rs.getString("UTENTEAGG"));
                    row.setCons(rs.getString("CONSERVAZIONE"));
                    row.setArch(rs.getString("ARCHIVIAZIONE"));
                    row.setModifica(rs.getInt("Modifica"));
                    row.setElimina(rs.getInt("Elimina"));
                    row.setCompetenze(rs.getInt("Competenze"));
                    row.setLetturaAllegati(rs.getInt("lettura_allegati"));
                    row.setModificaAllegati(rs.getInt("modifica_allegati"));
                    row.setCheckLivello(rs.getString("check_livello"));
                    row.setCompetenzeAllegati(rs.getString("competenze_allegati"));
                    row.setCount(iCounter);

                    rowsList.add(row);
                }
            }
        }
        catch ( SQLException e ) {
            throw e;
        }
        return rowsList;
    }


    /**
    * Costruzione delle righe comprese tra un intervallo
    * 
    * @param  rs  			ResultSet 
    * @param  start e max 	intervallo di estrapolazione righe del ResultSet	
    * @return String 		righe comprese nell'intervallo
    * 
	 */
  private Vector getCartRows(ResultSet rs, int start, int max) throws Exception
  {
	       int count=1;
	       int iCounter = 0;
	       
	       try
	       {
	    	   log.log_info("Inizio Costruzione Vettore - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] - Intervallo di estrapolazione righe del ResultSet - Start="+start+" -- Dim. Blocco="+max);
	    	   if ( rs != null ) 
	    	   {
	               if ( start < 1 ) { start = 1; }
	               if ( max < 1 ) { max = 1; }
	               
	               /** Posizionamento alla tupla con indice start */
	               if (count != start)
	               { 
	                 while ( rs.next() ) {
	                   if(count++ >= start-1) break;
	                 }
	               }
	               /** Generazione delle righe comprese tra start e pagesize */
	               isExitsRecords = false;
	               isExitsSucc=false;
	               while ( rs.next() )
	               {
	            	    if (iCounter++ >= max)
	            	    {
	            	    	if(rs.next())
	            	    	{
	            	    	  isExitsSucc=true;
	            	    	  vCartObjects.add(this.retrieveObject(rs));
	            	    	}
	            	    	break;
	            	    }
	            	    vCartObjects.add(this.retrieveObject(rs));
	            	    isExitsRecords = true;        
	               }
	    	   }
	    	   log.log_info("Fine Costruzione Vettore - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	       	}
	       	catch (SQLException e ) {
	       		throw e;         
	       	}
	       	return vCartObjects;
  }
   
   /**
     * Calcolo del numero di elementi della lista per effettuare la 
     * costruzione del navigatore.
     * 
     * @param  sql  	select 
     * @return int 		numero di elementi della lista
     * 
	 */
   private int getCountSELECT(String sql) throws Exception
   {
           int nrecords=0;
           elpsTime.start("Esecuzione Select COUNT WorkArea",sql);
           String count="SELECT COUNT(*) cnt FROM ( :SQL )";
           dbOp.setStatement(count);
           dbOp.setParameter(":SQL",sql);
           if(idQuery!=null){
        	   dbOp.setParameter(":id",idCartella);  
        	   dbOp.setParameter(":idCartAppartenenza",idCartAppartenenza);
           }	
           else
           {
        	   dbOp.setParameter(":id",idCartella.substring(1,idCartella.length()));  
           }      
           
           dbOp.execute();
           elpsTime.stop();
           ResultSet r = dbOp.getRstSet();
           if (r.next())
         	 nrecords=r.getInt("cnt");
           return nrecords;
   }
   
   /**
     * Setta url della pagina dei vari link del navigatore
     * 
     * @param  nrecords		numero di elementi  	 
     * 
	 */   
   private void setUrlPath(int nrecords) throws Exception
   {
         String sParmUrl="";
         String valParm="";
         String Url_parametri="";
         
         if(HTML)
	     {
	        Url_page=URL_PATH; 
	        page=linkPage;
	     }
	     else
	     {
	        page="1";
	        if (url!=null)
	        {
	          while (url.hasMoreElements() )
	          {
	             sParmUrl= url.nextElement().toString();
	             valParm=parametri.getProperty(sParmUrl);
	             
	             if(!sParmUrl.equals("holdPage"))
	             {
		             if (sParmUrl.equals("LINKSPage"))
		             {
		              if( ((VIEW.equals("D")) || (VIEW.equals("R")) ) && (nrecords<=10) )	   
		                 page="1";  
		               else
		                 page=valParm;              
		             }
		             else
		             {
		               if((sParmUrl.equals("fulltext")))
			             valParm = URLEncoder.encode(valParm); 
			           Url_parametri+=sParmUrl+"="+valParm+"&";               
		             }
	             }
	          }
	        }
	        else
	          Url_parametri="";
	        
	        if(Url_parametri.lastIndexOf("&")!=-1)
	        	Url_parametri=Url_parametri.substring(0,Url_parametri.length()-1);
	        Url_page="WorkArea.do?"+Url_parametri;
	     }  
   }
   
   /**
     * Setta url della pagina dei vari link del navigatore
     * 
     * @param  url	  	 
     * 
	 */ 
   private String setURLpage(String url) throws Exception
   {
           String queryURL="";
           String parametro="";
	    
           if(url.indexOf("fulltext")!=-1)
	       { 
		    String[] v=url.split("&");
		    for(int i=0;i<v.length;i++)
	        {
	    	 if(v[i].indexOf("fulltext")==-1)
	    	   queryURL+=v[i]+"&";
	    	 else
	    	 {
	    		String[] par=v[i].split("=");
	    		parametro+=par[0]+"="+URLEncoder.encode(par[1])+"&";
	    	 }
	        }
		    queryURL=queryURL.substring(0,queryURL.length()-1);
           } 
           queryURL+="&"+parametro;
           return  queryURL;
   }
   
   /**
     * Costruzione del navigatore
     * 
     * @param  nrecords		numero di elementi    	 
     * 
	 */
   private String getNavigator(int nrecords) throws Exception
   {
           String navigator;
           String First_URL="";
           String Prev_URL="";
           String Next_URL="";
           String Last_URL="";
           String LINKSPage="LINKSPage=";
           String classNavigator="AFCNavigatorLink";
           int Total_Pages;
        
           if(HTML)
            Total_Pages=(int)Math.ceil((double)nrecords/PAGE_SIZE);
           else
        	if(idQuery!=null)
        	{	
        	  if(WhereFullText!=null)
          	  {
        		Total_Pages=(int)Math.ceil((double)nrecords/10);	
           	    Url_page=setURLpage(Url_page);
          	  } 
        	  else
        		Total_Pages=(int)Math.ceil((double)nrecords/10);
        	} 
        	else
             Total_Pages=(int)Math.ceil((double)nrecords/10);
       
           if (Total_Pages==0) Total_Pages=1;
           String Page_Number=page;
           int npages=Integer.parseInt(page);
           
           /** FIRST */
           if((page!=null) && (npages!=1))
           {
        	 First_URL=Url_page+LINKSPage+"1";
        	 navigator=h.getAncoreClass(classNavigator,First_URL,h.getImg(_FIRST_THEMES));
           }
           else
        	 navigator=h.getImg(_VUOTO_THEMES);
           
	       /** PREV */
	       if((npages > 1) && (page!=null)){
	          Prev_URL=Url_page+LINKSPage+(npages-1);
	          navigator+=h.getAncoreClass(classNavigator,Prev_URL,h.getImg(_PREV_THEMES));
	       }
	       else
	          navigator+=h.getImg(_VUOTO_THEMES);
	          
           /** NUMERI DI PAGINE */
           navigator+=h.getNbsp()+Page_Number+h.getNbsp()+"di"+h.getNbsp()+Total_Pages+h.getNbsp(); 
    
	       /** NEXT */
	       if(HTML)
	       { 
	          if((nrecords - (npages*PAGE_SIZE))>0){
	           Next_URL=Url_page+LINKSPage+(npages+1);
	           navigator+=h.getAncoreClass(classNavigator,Next_URL,h.getImg(_NEXT_THEMES));
	          }
	          else
	           navigator+=h.getImg(_VUOTO_THEMES);
	       }
	       else
	       {
	         if((nrecords - (npages*10))>0){
	          Next_URL=Url_page+LINKSPage+(npages+1);
	          navigator+=h.getAncoreClass(classNavigator,Next_URL,h.getImg(_NEXT_THEMES));
	         }
	         else
	          navigator+=h.getImg(_VUOTO_THEMES);
	       }
	        
           /** LAST */  
	       if((Total_Pages!=1) && (page!=null) && (npages!=Total_Pages)){
	    	   Last_URL=Url_page+LINKSPage+Total_Pages;
	    	   navigator+=h.getAncoreClass(classNavigator,Last_URL,h.getImg(_LAST_THEMES));
	       }
	       else
	    	   navigator+=h.getImg(_VUOTO_THEMES);
    
	       return navigator;
   }
 
   /**
     * Costruzione HTML di ciascuna riga della lista
     * 
     * @param  nrecords		numero di elementi    	 
     * 
	 */
   private String getRiga(int indice,String typeObj,String idCartProv,String collegamento,String nome,String id_oggetto,String profilo,String icona,String nome_icona,long ordina,String ricercaMod,String cm,String area,String cr,String idtipoDoc,String data_agg,String utente_agg,String cons,String arch,int Modifica,int Elimina,int Competenze,int lettura_allegati, int modifica_allegati,String check_livello,String competenze_allegati,int count) throws Exception 
   {
           String row,contentTR,contentTD;
           String classTD="";
           String classTR="AFCDataTD";
           if (count%2==0)  classTR="AFCAltDataTD"; else classTR="AFCDataTD";

           if(check_livello==null)
        	 check_livello="";  
           
           if(HTML)
	       {
	          contentTD=h.getInput("WIDTH:  2px;  HEIGHT:  4px","hidden","1",typeObj,"TIPO_OGGETTO");
	          contentTD+=h.getInput("WIDTH:  1px;  HEIGHT:  8px","hidden","2",id_oggetto,"ID_OGGETTO");
	          row=h.getTDClass(classTD,"2%",h.getNbsp());
	          row+=h.getTDClass(classTD,"63%",getNome(indice,typeObj,idCartProv,collegamento,nome,id_oggetto,profilo,icona,nome_icona,ordina,ricercaMod,cm,area,cr,idtipoDoc,Modifica,check_livello)+contentTD);
	          row+=h.getTDClass(classTD,"5%",getAllButton(typeObj,id_oggetto,lettura_allegati,modifica_allegati,competenze_allegati)); 
	          row+=h.getTDClass(classTD,"30%",h.getP("right",""+data_agg+" da "+utente_agg));
	          contentTR=h.getTR(classTR,"15","this.className = 'AFCHoverTR'","this.className = '"+classTR+"'",row);
	       }
	       else
	       {
	          contentTD=h.getInput("WIDTH:  2px;  HEIGHT:  4px","hidden","1",typeObj,"TIPO_OGGETTO");
	          contentTD+=h.getInput("WIDTH:  1px;  HEIGHT:  8px","hidden","2",id_oggetto,"ID_OGGETTO");
	          row=h.getTDClass(classTD,"2%",h.getInput("","checkBottoniera();","IDSeleziona","checkbox",id_oggetto,"Seleziona"));
	          row+=h.getTDClass(classTD,"88%",getNome(indice,typeObj,idCartProv,collegamento,nome,id_oggetto,profilo,icona,nome_icona,ordina,ricercaMod,cm,area,cr,idtipoDoc,Modifica,check_livello)+contentTD);
	          row+=h.getTDClass(classTD,"10%",
	        		  h.getTable("60%","center",
	        		  h.getTR(h.getTD("","",getAllButton(typeObj,id_oggetto,lettura_allegati,modifica_allegati,competenze_allegati)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getConservazione(typeObj,cons,id_oggetto)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getImpronta(typeObj, id_oggetto, lettura_allegati, modifica_allegati, competenze_allegati)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getElencoCartellePerDoc(typeObj,idCartProv,id_oggetto,nome,profilo)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getFlussiButton(typeObj,area,cm,cr,profilo,id_oggetto)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getModifica(typeObj,Modifica,id_oggetto,idCartProv,nome,idtipoDoc,area,cm,cr,ricercaMod,check_livello,profilo,icona)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getElimina(typeObj,Elimina,id_oggetto,idCartProv)+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())
	        		  		+h.getTD("","",getCompetenze(typeObj,idCartProv,id_oggetto,Competenze)))
	        		  )
	        		  +h.getTable("100%",h.getTR(h.getTD("top","",h.getP("right","textData",data_agg+h.getNbsp()+utente_agg)))
	        		  ));
	          contentTR=h.getTR(classTR,"15","this.className = 'AFCHoverTR'","this.className = '"+classTR+"'",row);
	       }
           
           /** Costruzione della lista di ICONE da verificare l'esistenza e l'eventuale caricamento nel file system */
          if(icona!=null)
          {	  
           if(listaICONE.indexOf(icona)==-1)
           {
        	 if(listaICONE.equals(""))
        	   listaICONE+=icona;
        	 else
        	   listaICONE+=","+icona;	 
           }
          }
       return contentTR+"\n";
   }
   
   /**
    * Costruzione HTML di ciascuna riga della lista
    * 
    * @param  nrecords		numero di elementi    	 
    * 
	 */
  private String getRigaPersonalizzata(int indice,String typeObj,String idCartProv,String collegamento,String nome,String id_oggetto,String profilo,String icona,String nome_icona,long ordina,String ricercaMod,String cm,String area,String cr,String idtipoDoc,String data_agg,String utente_agg,String cons,String arch,int Modifica,int Elimina,int Competenze,int lettura_allegati, int modifica_allegati,String check_livello,String competenze_allegati,int count) throws Exception 
  {
          String row,contentTR,contentTD,contentROW;
          String tag1,tag2,tag3,tag4,tag5,tag6,tag7;
          String idCartProvenienza=null,idQueryProvenienza=null,Provenienza=null,gdc_link=null;
          String classTD="";
          String classTR="AFCDataTD";
          String url,ico,tooltip,srcIcona;
          
          if (count%2==0)  classTR="AFCAltDataTD"; else classTR="AFCDataTD";
      	  
          if(idCartella!=null && !idCartella.equals(""))
          {
        	 if(idCartella.indexOf("C")!=-1)
        	  idCartProvenienza=idCartella.substring(1,idCartella.length());
        	 else
        	  idCartProvenienza=idCartella;	 
          }
          else
           idCartProvenienza = verificaParametroGet("idCartAppartenenza",req.getParameter("idCartAppartenenza"));
          
          if(check_livello==null)
        	check_livello="";  
          
          if(idQuery!=null){ 
           idQueryProvenienza=idQuery;
           Provenienza="Q";
           gdc_link = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
          } 
          else {
           idQueryProvenienza="-1";  
           Provenienza="C";
           gdc_link = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1"; 
          }
          
          /** TAG 1 */
          if(mapLink.get(idtipoDoc+"@"+1)!=null)
          {
        	JDMSLink link=mapLink.get(idtipoDoc+"@"+1);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	String icona_default,path_icona="";
        	
        	if (typeObj.equals("D"))
        	 icona_default=_ALLEGATI;
        	else
        	 icona_default=_IMGVUOTO;	
        	
        	try {
          		srcIcona = getIcona(profilo,idtipoDoc,"1","S",icona_default); 
          		if(srcIcona.equals(icona_default))
          		  path_icona ="";
          		else
          		  path_icona = "../common/icone/";		 
          		 
    		}
    		catch (Exception e) {
    			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
    			  		        + "("+profilo+","+idtipoDoc+",1,'S','"+icona_default+"') "
    			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
    			  srcIcona = icona_default;
  			}
        	        	
        	tag1=getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,"");
          }
          else
           tag1=getAllButton(typeObj,id_oggetto,lettura_allegati,modifica_allegati,competenze_allegati);
         
          /** TAG 2 */
          if(mapLink.get(idtipoDoc+"@"+2)!=null)
          {
        	JDMSLink link=mapLink.get(idtipoDoc+"@"+2);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	String icona_default,path_icona="";
        	if(typeObj.equals("D")&& cons!=null){
        		if(cons.equals(_WORKING))
        		 icona_default=_CONS_WORKING;
        		else
        	 	 icona_default=_CONS_CONSERVA_LOG;	
        	}
          	else
              icona_default=_IMGVUOTO;
         	
        	try {
           		srcIcona = getIcona(profilo,idtipoDoc,"2","S",icona_default); 
           		if(srcIcona.equals(icona_default))
           		  path_icona ="";
           		else
           		  path_icona = "../common/icone/";		 
           		 
     		}
     		catch (Exception e) {
     			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
     			  		        + "("+profilo+","+idtipoDoc+",2,'S','"+icona_default+"') "
     			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
     			  srcIcona = icona_default;
   			}
        	
        	tag2=getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,"");	  
          }
          else
        	tag2=getConservazione(typeObj,cons,id_oggetto);
          
          /** TAG 3 */  
    	  if(mapLink.get(idtipoDoc+"@"+3)!=null)
          {
        	JDMSLink link=mapLink.get(idtipoDoc+"@"+3);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	String icona_default,path_icona="";
        	
        	if(typeObj.equals("Q"))
       		 icona_default=_LISTDS;
       		else
       	 	 icona_default=_LIST;	
        	
        	try {
           		srcIcona = getIcona(profilo,idtipoDoc,"3","S",icona_default); 
           		if(srcIcona.equals(icona_default))
           		  path_icona ="";
           		else
           		  path_icona = "../common/icone/";		 
           		 
     		}
     		catch (Exception e) {
     			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
     			  		        + "("+profilo+","+idtipoDoc+",3,'S','"+icona_default+"') "
     			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
     			  srcIcona = icona_default;
   			}
        	
        	tag3=getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,"");	  
          }
          else
           tag3=getElencoCartellePerDoc(typeObj,idCartProv,id_oggetto,nome,profilo);
    	  
    	  /** TAG 4 */
    	  if(mapLink.get(idtipoDoc+"@"+4)!=null)
          {
        	JDMSLink link=mapLink.get(idtipoDoc+"@"+4);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	String icona_default,path_icona="";
        	if(typeObj.equals("D"))
          	  icona_default=_WORKFLOW;
            else
              icona_default=_IMGVUOTO;
         	
        	try {
           		srcIcona = getIcona(profilo,idtipoDoc,"4","S",icona_default); 
           		if(srcIcona.equals(icona_default))
           		  path_icona ="";
           		else
           		  path_icona = "../common/icone/";		 
           		 
     		}
     		catch (Exception e) {
     			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
     			  		        + "("+profilo+","+idtipoDoc+",4,'S','"+icona_default+"') "
     			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
     			  srcIcona = icona_default;
   			}
        	
        	tag4=getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,"");  
          }
          else
    	    tag4=getFlussiButton(typeObj,area,cm,cr,profilo,id_oggetto);
    	  
    	  /** TAG 5 */ 
    	  //Non viene effettuato il controllo del blocco dei documenti vine demandato
    	  // alla pagina da indirizzarsi
    	  //if(mapLink.get(idtipoDoc+"@"+5)!=null && check_livello.equals(""))
    	  if(mapLink.get(idtipoDoc+"@"+5)!=null)
    	  {
    		String icona_default,path_icona="";
    		  
    		JDMSLink link=mapLink.get(idtipoDoc+"@"+5);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	if(tooltip==null)
        	 tooltip ="";	
        	
            String rw="";
            
            if(Modifica==1)
       		 	rw="W";
       		 else
       			rw="R"; 
        	        	
        	// Nel caso in cui viene specificato nel campo ICONA = @STANDARD@ allora 
        	// viene applicata l'icona standard e se il tooltip è vuoto viene riportato quello
        	// standard.
    	    if(Modifica==1){        		 
    		  icona_default= _EDIT;
         	  if(tooltip.equals(""))
         	    tooltip = "Modifica il Documento";
    	    }
    	    else {
    		  icona_default= _EDITDS;        		 
         	  if(tooltip.equals(""))
         	    tooltip = "Visualizza Documento";   
    	    }
    	
        	try {
        		srcIcona = getIcona(profilo,idtipoDoc,"5","S",icona_default); 
        		if(srcIcona.equals(icona_default))
        		  path_icona ="";
        		else
        		  path_icona = "../common/icone/";		 
        		 
  		    }
  		    catch (Exception e) {
  			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
  			  		        + "("+profilo+","+idtipoDoc+",5,'S','"+icona_default+"') "
  			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
  			  srcIcona = icona_default;
			}
        	        	        	
        	if (typeObj.equals("D")){
        	  if(Modifica==1)
        		tag5 = getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,rw,"ServletModulisticaDocumento",nome,Provenienza,URLEncoder.encode(gdc_link));	
        	  else
        	    tag5 = h.getImg("",_EDITDS);
        	}
        	else
        	 tag5 = getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,rw);	  
          }
          else
    	   tag5=getModifica(typeObj,Modifica,id_oggetto,idCartProv,nome,idtipoDoc,area,cm,cr,ricercaMod,check_livello,profilo,icona);
    	  
    	  /** TAG 6 */
    	  if(mapLink.get(idtipoDoc+"@"+6)!=null)
          {
        	JDMSLink link=mapLink.get(idtipoDoc+"@"+6);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	String icona_default,path_icona="";
        	
        	if(Elimina==1) 
        	 icona_default=_ANNULLA;
        	else
        	 icona_default=_ANNULLADS;
        	
        	try {
          		srcIcona = getIcona(profilo,idtipoDoc,"6","S",icona_default); 
          		if(srcIcona.equals(icona_default))
          		  path_icona ="";
          		else
          		  path_icona = "../common/icone/";		 
          		 
    		}
    		catch (Exception e) {
    			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
    			  		        + "("+profilo+","+idtipoDoc+",6,'S','"+icona_default+"') "
    			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
    			  srcIcona = icona_default;
  			}
        	        	
        	tag6=getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,"");  
          }
          else
    	   tag6=getElimina(typeObj,Elimina,id_oggetto,idCartProv);
    	  
    	  /** TAG 7 */
    	  if(mapLink.get(idtipoDoc+"@"+7)!=null)
          {
        	JDMSLink link=mapLink.get(idtipoDoc+"@"+7);
        	url=link.getURL();
        	ico=link.getICONA();
        	tooltip=link.getTOOLTIP();
        	
        	String icona_default,path_icona="";
        	
            if (Competenze==1)
             icona_default = _COMP;
            else
             icona_default = _COMPVIEW;

            try {
          		srcIcona = getIcona(profilo,idtipoDoc,"7","S",icona_default); 
          		if(srcIcona.equals(icona_default))
          		  path_icona ="";
          		else
          		  path_icona = "../common/icone/";		 
          		 
    		}
    		catch (Exception e) {
    			  log.log_error("CCS_WorkArea::getRigaPersonalizzata::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
    			  		        + "("+profilo+","+idtipoDoc+",7,'S','"+icona_default+"') "
    			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
    			  srcIcona = icona_default;
  			}
        	
        	tag7=getIconaPersonalizzata(url,path_icona+srcIcona,tooltip,id_oggetto,typeObj,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,"");	  
          }
          else
    	   tag7=getCompetenze(typeObj,idCartProv,id_oggetto,Competenze);
            
          contentROW= h.getTD("","",tag1+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+
          h.getTD("","",tag2+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+
          h.getTD("",h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+
          h.getTD("","",tag3+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+
          h.getTD("","",tag4+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+ 
          h.getTD("","",tag5+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+
          h.getTD("","",tag6+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp()+h.getNbsp())+
          h.getTD("","",tag7);
                              
          contentTD = h.getInput("WIDTH:  2px;  HEIGHT:  4px","hidden","1",typeObj,"TIPO_OGGETTO");
	      contentTD+= h.getInput("WIDTH:  1px;  HEIGHT:  8px","hidden","2",id_oggetto,"ID_OGGETTO");
          row = h.getTDClass(classTD,"2%",h.getInput("","checkBottoniera();","IDSeleziona","checkbox",id_oggetto,"Seleziona"));
          row+= h.getTDClass(classTD,"88%",getNome(indice,typeObj,idCartProv,collegamento,nome,id_oggetto,profilo,icona,nome_icona,ordina,ricercaMod,cm,area,cr,idtipoDoc,Modifica,check_livello)+contentTD);
          row+= h.getTDClass(classTD,"10%",h.getTable("60%","center",h.getTR(contentROW))+h.getTable("100%",h.getTR(h.getTD("top","",h.getP("right","textData",data_agg+h.getNbsp()+utente_agg)))));
          contentTR=h.getTR(classTR,"15","this.className = 'AFCHoverTR'","this.className = '"+classTR+"'",row);
          
          if(icona!=null && !icona.equals(""))
           controlloIcona(icona);
          
          return contentTR+"\n";        
  }
  
   
 
   /**
    * Costruzione HTML di ciascuna riga della lista
    * 
    * @param  nrecords		numero di elementi    	 
    * 
	 */
  private Properties retrieveObject(ResultSet rs) throws Exception 
  {
          String collegamento,icona,nomeicona,ricercaMod,cm,cr,area,
                 idtipoDoc,conservazione,archivazione,check_livello,
                 url,tooltip,tipo_link,funzione_lettura;
          
          if(rs.getString("COLLEGAMENTO")==null)
            collegamento="";
  	      else
  	    	collegamento=rs.getString("COLLEGAMENTO");  
  	      
          if(rs.getString("ICONA")==null)
  	    	icona="";
  		  else
  		    icona=rs.getString("ICONA");  
          
          if(rs.getString("ICNOME")==null)
            nomeicona="";
    	  else
    		nomeicona=rs.getString("ICNOME");  
          
          if(rs.getString("RICERCAMODULISTICA")==null)
        	  ricercaMod="";
  		  else
  			ricercaMod=rs.getString("RICERCAMODULISTICA");  
          
          if(rs.getString("CM")==null)
        	  cm="";
  		  else
  			cm=rs.getString("CM");  
          
          if(rs.getString("AREA")==null)
        	  area="";
  		  else
  			area=rs.getString("AREA");  
          
          if(rs.getString("ID_TIPODOC")==null)
        	idtipoDoc="";
  		  else
  			idtipoDoc=rs.getString("ID_TIPODOC"); 
          
          if(rs.getString("CR")==null)
        	cr="";
  		  else
  			cr=rs.getString("CR");  
          
          if(rs.getString("CONSERVAZIONE")==null)
          	conservazione="";
      	  else
      		conservazione=rs.getString("CONSERVAZIONE");  
            
          if(rs.getString("ARCHIVIAZIONE")==null)
          	archivazione="";
          else
        	archivazione=rs.getString("ARCHIVIAZIONE"); 
          
          if(rs.getString("check_livello")==null)
        	check_livello="";
          else
        	check_livello=rs.getString("check_livello"); 
          
          if(rs.getString("URL")==null)
          	url="";
          else
          	url=rs.getString("URL"); 
          
          if(rs.getString("TOOLTIP")==null)
          	tooltip="";
          else
           	tooltip=rs.getString("TOOLTIP"); 
          
          if(rs.getString("TIPO_LINK")==null)
           	tipo_link="";
          else
        	tipo_link=rs.getString("TIPO_LINK"); 
          
          if(rs.getString("funzione_lettura")==null)
        	funzione_lettura="";
          else
        	funzione_lettura=rs.getString("funzione_lettura"); 
          
          Properties p=null;
          p=new Properties();    
	      p.put("TIPO_OGGETTO",rs.getString("TIPO_OGGETTO"));
	      p.put("ID_CART_PROV",rs.getString("ID_CART_PROV"));
	      p.put("COLLEGAMENTO",collegamento);
	      p.put("NOME",rs.getString("NOME"));
	      p.put("ID_OGGETTO",rs.getString("ID_OGGETTO"));
	      p.put("CONSERVAZIONE",conservazione);
	      p.put("ARCHIVIAZIONE",archivazione);
	      p.put("profilo",rs.getString("profilo"));
	      p.put("ICONA",icona);
	      p.put("NOMEICONA",nomeicona);
          p.put("ordina",rs.getString("ordina"));
          p.put("RICERCAMODULISTICA",ricercaMod);
          p.put("CM",cm);
          p.put("AREA",area);
          p.put("CR",cr);
          p.put("ID_TIPODOC",idtipoDoc);
          p.put("DATAAGG",rs.getString("DATAAGG"));
          p.put("UTENTEAGG",rs.getString("UTENTEAGG"));
          p.put("Modifica", rs.getString("Modifica"));
          p.put("Elimina",rs.getString("Elimina"));
          p.put("Competenze",rs.getString("Competenze"));
          p.put("lettura_allegati",rs.getString("lettura_allegati"));
          p.put("modifica_allegati",rs.getString("modifica_allegati"));
          p.put("check_livello",check_livello);
          p.put("competenze_allegati",rs.getString("competenze_allegati"));
          p.put("URL",url);
          p.put("TOOLTIP",tooltip);
          p.put("TIPO_LINK",tipo_link);
          p.put("FUNZIONE_LETTURA",funzione_lettura);
          return p;
  }
   
   /**
     * Costruzione parte sql select
     * 
     * @param  obj	oggetto    	 
     * 
	 */
   private String getOrdina(String obj)
   {
           String ordina="ordina,";
           		  ordina+=obj+".data_aggiornamento,";
           		  ordina+="to_char("+obj+".data_aggiornamento,'dd/mm/yyyy') dataagg,";
           		  ordina+="F_NOMINATIVO_UTENTE("+obj+".utente_aggiornamento) utenteagg,";
           return ordina;
   }
   
   /**
     * Costruzione delle condizioni di ricerca per la Query
     */
   private void getCondWhere() throws Exception
   { 
	       int offset=PAGE_SIZE;
	       int indice=0;
	       int page=0,n=0; 
	       if(linkPage==null) linkPage="1";
	       int numpag=Integer.parseInt(linkPage)%10;
	       int dim=0,dimblocco=0;
	       String pagina = linkPage;
	       
	       log.log_info("Inizio - Costruttore filtro per la Query");
	       
	       if(submit)
	       { 
	    	   vlistID=null; 
	           String tp=Integer.parseInt(linkPage)+"";
	      	   dim=Integer.parseInt(tp.substring(0,(tp.length()-1))+"0");
	      	   if(dim==0)
	      		 dimblocco=PAGE_SIZE*10;
	      	   else
	             dimblocco=PAGE_SIZE*(dim+10);
	       }
	       else
	       	dimblocco=PAGE_SIZE*10;  
	       
	     
	       /** Viene eseguito all'apertura della prima pagina query tranne per il navigatore */ 
	       if((vlistID==null) || (numpag==1) )
	       {
    	     try 
             {         
	    		 log.log_info("Inizio - Costruzione del filtro");  
	    		 
	    		 if(vlistID==null)
	    			 pagina="1";	 
	    		 
	    		 q.setEnvironment(vu);
                 q.setFullTextCondition(null);
                 
                 if (this.WhereFullText!=null) {    
	                 
                	 if(RicercaAllegati.equals("S"))  {
	                   q.setFullTextObjCondition(WhereFullText);
	                 }
	                 
	                 if(RicercaOCR.equals("S"))  {
	 	                 q.setFullTextObjOCRCondition(WhereFullText);
	 	             }	 
	                 
	                 if(RicercaFT.equals("S"))  {
	                	 q.setFullTextCondition(WhereFullText); 
	 	             }
                 }
                 
                 /**
                 Solo per Ricerca Standard
                 Binding dei parametri di sessione nel filtro di ricerca.*/
          /*       if(tipoRicerca==null || (tipoRicerca!=null && !tipoRicerca.equals("R"))){
                	
                	 try {
                	    vParametriSessione = loadParametriSessione();
                	 }
                	 catch (Exception exp) {
                        vParametriSessione = new Vector();       
                      }
                	 
                	 for(int i=0;i<vParametriSessione.size();i++)
                	 {
                		 String parametro = vParametriSessione.get(i).toString();
                		 if(req.getSession().getAttribute(parametro)!=null && !req.getSession().getAttribute(parametro).equals(""))
                	      q.setSessionParameter(parametro,req.getSession().getAttribute(parametro).toString());		 
                	 }                	 
                 }*/
                 
                 
                 /** Nel caso di chiamata Query esterna viene rieseguita tutta la
                     Ricerca senza utilizzare il vettore di sessione */
                 if(HTML)
                 {
                	 vlistID=q.risultatoQuery();
                	 bIsTimeOut=q.bAlive;
                	 offset=PAGE_SIZE;
                 }   
                 else
                 {
                   if(submit) 
                	 q.setFetchInit(0);
                   else
                   {	   
	                  if((Integer.parseInt(pagina)==1))
	                   q.setFetchInit(0);
	                  else 
	                   q.setFetchInit(((Integer.parseInt(pagina)-1)*PAGE_SIZE)+1);
	                  // Se metto +1 non mi duplica il centesimo elemento non me lo so spiegare 
	                  //q.setFetchInit((Integer.parseInt(pagina)-1)*PAGE_SIZE);
	                
                   }
                   
                   q.setFetchSize(dimblocco);
                 
	                if(vlistID!=null)
	                 {   
	                	 if(((Integer.parseInt(pagina)-1)*PAGE_SIZE)>(vlistID.size()-1))              	 
	                	   vlistID.addAll(q.risultatoQuery()); 
	                	 else
	                	 {
	                	   if (Integer.parseInt(pagina)==1)
	                	     vlistID=q.risultatoQuery(); 
	                	 }
	                 }
	                 else {
	                   vlistID=q.risultatoQuery();
	                 }  
	                 
	                 bIsTimeOut=q.bAlive;
	                 succ=!q.isLastRowFetch();
                 }
                 
                 log.log_info("Fine - Costruzione del filtro");  
            }
            catch (Exception exp) {
               /** Se ho un errore sulla query (ad esempio un carattere al posto di un numerico mostro la workarea vuota. */               
               vlistID = new Vector();       
               isVectorVuoto = true;
            }
        }
	       
	   log.log_info("Inizio - Costruzione vettore Query");       
	       
	   page=Integer.parseInt(linkPage);
	   n=(page*offset);
	   indice= n-offset;     
	   
       /** Controllo per l'accesso all'ultima pagina */
       if((n>vlistID.size())) {
         n=vlistID.size(); 
       }
       
       if(indice >= n && indice>0){
    	 if(indice==offset)
    	   indice=0;
    	 else
    	   indice=indice-offset;	 
       }
    	  
       
       /** Costruzione degli vettori: vDoc e vCart e vQuery */
       Vector vDoc=new Vector();
       Vector vCart=new Vector();
       Vector vQuery=new Vector();
       
       //Reset del vettore
       vlistIDDocs =new Vector();
       
       for (int i=indice;i<n;i++)
       {	
          if ((new DocUtil(vu)).isDocumentoCartella((String)vlistID.get(i)))
             vCart.add(vlistID.get(i));
          else 
        	if ((new DocUtil(vu)).isDocumentoQuery((String)vlistID.get(i)))
               vQuery.add(vlistID.get(i));
            else
               vDoc.add(vlistID.get(i));
          
          vlistIDDocs.add(vlistID.get(i));
          
       }		 
 
       int max_list=1000;
       int count=0,s=0;
       int v_size=vDoc.size();
       
       Where="where ( 1<>1";
       tabellaLD="";
       if (vDoc.size()!=0) {
    	 Where="where ( d.id_documento=ld.id_documento ";
    	 tabellaLD=" , ( ";
    	 OrdinaWhere="decode(d.id_documento";
       }
       else
    	 OrdinaWhere="0 ordinaQuery";
       
       for (int i=0;i<vDoc.size();i++)
       {	
    	 OrdinaWhere+=","+vDoc.get(i)+","+i;
    	   
         if (count == max_list)
         {
              count=0;
              s++;
              v_size=vDoc.size()-s*max_list;
              Where+=" or d.id_documento=ld.id_documento ";
         }
         tabellaLD+=" SELECT "+vDoc.get(i)+" id_documento FROM DUAL ";
         if ( vDoc.size() <= max_list )
          tabellaLD+=(i!=vDoc.size()-1)?(" UNION ALL "):(")");	 
         else 
         {
           if(count==v_size-1)
        	   tabellaLD+=")";
           else
            if (count==max_list-1)
            	tabellaLD+=")";
            else 
              tabellaLD+=" UNION ALL ";
         }
         count++;
       }
         
       if(!tabellaLD.equals(""))
    	 tabellaLD+=" ld ";
       Where+=")";
       if (!OrdinaWhere.equals("0 ordinaQuery")) OrdinaWhere+=") ordinaQuery";
       
       count=0;
       s=0;
       v_size=vCart.size();
        
       WhereCart="where ( 1<>1";
         
       if (vCart.size()!=0) {
       	 WhereCart="where ( d.id_documento in (";
       	 OrdinaWhereCart="decode(d.id_documento";
       }
       else {
       	 OrdinaWhereCart="0 ordinaQuery";
         WhereCart="WHERE d.id_documento = -1000000";
       }
       
       for (int i=0;i<vCart.size();i++)
       {
       	  OrdinaWhereCart+=","+vCart.get(i)+","+i;
          if (count == max_list)
          {
              count=0;
              s++;
              v_size=vCart.size()-s*max_list;
              WhereCart+=" or d.id_documento in (";
          }
       
          WhereCart+=" SELECT "+vCart.get(i)+" FROM DUAL ";
          if ( vCart.size() <= max_list)
            WhereCart+=(i!=vCart.size()-1)?(" UNION ALL "):(")");
          else 
          {
           if(count==v_size-1)
             WhereCart+=")";
           else
            if (count==max_list)
              WhereCart+=")";
            else 
              WhereCart+=" UNION ALL ";   
          }
          count++;
       }
       
       if (vCart.size()!=0) 
        WhereCart+=")";
       
       if (!OrdinaWhereCart.equals("0 ordinaQuery")) OrdinaWhereCart+=") ordinaQuery";
       
       count=0;
       s=0;
       v_size=vQuery.size();
        
       WhereQuery="where ( 1<>1";
        
       if (vQuery.size()!=0) {
         WhereQuery="where ( d.id_documento in (";
         OrdinaWhereQuery="decode(d.id_documento";
       }
       else {
       	 OrdinaWhereQuery="0 ordinaQuery";
       	 WhereQuery = "WHERE d.id_documento = -1000000";
       }
       
       for (int i=0;i<vQuery.size();i++)
       {
       	  OrdinaWhereQuery+=","+vQuery.get(i)+","+i;
          if (count == max_list)
          {
             count=0;
             s++;
             v_size=vQuery.size()-s*max_list;
             WhereQuery+=" or d.id_documento in (";
          }
          WhereQuery+=" SELECT "+vQuery.get(i)+" FROM DUAL ";
          if ( vQuery.size() <= max_list)
          	 WhereQuery+=(i!=vQuery.size()-1)?(" UNION ALL "):(")");
          else 
          {
            if(count==v_size-1)
          	  WhereQuery+=")";
            else
             if (count==max_list)
           	   WhereQuery+=")";
             else 
           	   WhereQuery+=" UNION ALL ";   
          }
          count++;
        }
        
        if (vQuery.size()!=0) 
         WhereQuery+=")";
        
        if (!OrdinaWhereQuery.equals("0 ordinaQuery")) OrdinaWhereQuery+=") ordinaQuery";
        
        log.log_info("Fine - Costruzione vettore Query");    
        log.log_info("Fine - Costruttore filtro per la Query");
  } 
   
   /**
    * Costruzione delle condizioni di ricerca per la Query
    */
  private void buildCondWhereStampa() throws Exception
  { 
	  	  int indice=0,n=0;
       
	  	  log.log_info("Inizio - Costruttore filtro per la Query");
       
	      try 
	      {         
	    	log.log_info("Inizio - Costruzione del filtro");  
	    	 
	    	q.setEnvironment(vu);
	        q.setFullTextCondition(null);
	            
	        if (this.WhereFullText!=null)                 	 
	          q.setFullTextCondition(WhereFullText);
	          
	        q.setFetchInit(0);
	        q.setFetchSize(-1);
	        vlistID=q.risultatoQuery();
	        bIsTimeOut=q.bAlive;
	            
	        log.log_info("Fine - Costruzione del filtro");  
	      }
          catch (Exception exp) {
            vlistID = new Vector();                                                 
          }
          
          log.log_info("Inizio - Costruzione vettore Query");       
	       
	      indice=0;
		  n=vlistID.size();
	   
	      Vector vDoc=new Vector();
	      Vector vCart=new Vector();
	      Vector vQuery=new Vector();
      
	      //Reset del vettore
	      vlistIDDocs =new Vector();
      
	      for (int i=indice;i<n;i++)
	      {	
	         if ((new DocUtil(vu)).isDocumentoCartella((String)vlistID.get(i)))
	            vCart.add(vlistID.get(i));
	         else 
	       	if ((new DocUtil(vu)).isDocumentoQuery((String)vlistID.get(i)))
	              vQuery.add(vlistID.get(i));
	           else
	              vDoc.add(vlistID.get(i));
	         
	         vlistIDDocs.add(vlistID.get(i));
	         
	      }		 

	      int max_list=1000;
	      int count=0,s=0;
	      int v_size=vDoc.size();
      
	      Where="where ( 1<>1";
	      tabellaLD="";
	      if (vDoc.size()!=0)
	      {
	    	Where="where ( d.id_documento=ld.id_documento ";
	    	tabellaLD=" , ( ( ";
	    	OrdinaWhere=" decode(d.id_documento ";
	      }
	      else
	    	OrdinaWhere="0 ordinaQuery1";
      
	      int max_dec=100;
	      int count_dec=0;
	      
	      num_blocchi=1;	      
	      
	      for (int i=0;i<vDoc.size();i++)
	      {	
	    	if(count_dec == max_dec)
	    	{
	    		count_dec=0;
	    		OrdinaWhere+=" ) ordinaquery"+num_blocchi+" , decode(d.id_documento ";
	     		num_blocchi++;
	    	}
	    	  
	    	OrdinaWhere+=","+vDoc.get(i)+","+i;
   	   
	        if (count == max_list)
	        {
	             count=0;
	             s++;
	             v_size=vDoc.size()-s*max_list;
	        }
	        
	        tabellaLD+=" SELECT "+vDoc.get(i)+" id_documento FROM DUAL ";
	        
	        if ( vDoc.size() <= max_list )
	         tabellaLD+=(i!=vDoc.size()-1)?(" UNION ALL "):(")");	 
	        else 
	        {
	          if(count==v_size-1)
	       	   tabellaLD+=")";
	          else
	           if (count==max_list-1)
	           	tabellaLD+=") UNION ALL (";
	           else 
	             tabellaLD+=" UNION ALL ";
	        }
	        
	        count_dec++;
	        count++;
	      }
   
      if(!tabellaLD.equals(""))
   	    tabellaLD+=" ) ld ";
      Where+=")";
      if (!OrdinaWhere.equals("0 ordinaQuery1")) OrdinaWhere+=") ordinaQuery"+num_blocchi;
      
      for(int i=1;i<=num_blocchi;i++)
        ordineSEQ+=" ordinaquery"+i+" , ";
  	 
      log.log_info("Fine - Costruzione vettore Query");    
      log.log_info("Fine - Costruttore filtro per la Query");
 } 

   private String executeFunzioneCollegamentoEsterno(String nome_funzione) throws Exception
   {
	          String ret = "1"; 

	          try
	          { 
	           dbOp.setCallFunc(nome_funzione+"()");
               dbOp.execute();
 	    	   ret = dbOp.getCallSql().getString(1);
	         }
             catch(Exception e) 
	         { 
             	 log.log_error("CCS_WorkArea::executeFunzioneCollegamentoEsterno() - Esecuzione della FUNZION_LETTURA:"+nome_funzione+" - Erroe:"+e.getMessage());
		         return "1";
	         }
             return ret;
	  }
 
   /**
     * Gestione della Nome 
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa nome
     * 
	 */
   private String getNome(int indice,String typeObj,String idCartProv,String collegamento,String nome,String id_oggetto,String profilo,String tipo_icona,String nome_icona,long ordina,String ricercaMod,String cm,String area,String cr,String idtipoDoc,int modifica,String check_livello) throws Exception
   {
           String snome="";
           String viewDoc="";
           String sCollegamento="";
           String sIdCartAppartenenza="&idCartAppartenenza="+idCartProv;
           String monoRecord;
           String ico="",tooltipIcona="";
           
           if(pHTMLRecord!=null && pHTMLRecord.size()!=0)
           {
        	   monoRecord=pHTMLRecord.get("ID"+profilo).toString();
        	   //Se non esiste il blocco restituisco
     		   //scritta standard (o nome dell'oggetto
     		   //oppure dicitura per documento)
     	       if(monoRecord.equals(""))
     	       {     	    	 
         	     if (typeObj.equals("D"))
         		   monoRecord="Documento n. "+ id_oggetto;
         	     else
         		   monoRecord=nome;//:URL_DOCUMENTOVIEW
         	    log.log_info("Il blocco monorecord è vuoto per l'oggetto di tipo "+typeObj+" e monorecord calcolato: "+monoRecord);  
     	       }   
           }
           else
	       {
        	 if (typeObj.equals("D"))
    		   monoRecord="Documento n. "+ id_oggetto;
    	     else
    		   monoRecord=nome;
        	 log.log_info("Il blocco monorecord non esiste per l'oggetto di tipo "+typeObj+" e monorecord calcolato: "+monoRecord);  
	       }

           if(idCollegamento == null)
           {
            if(collegamento!=null)
              sCollegamento="&idCollegamento="+collegamento;
           }
           else
            sCollegamento="&idCollegamento="+idCollegamento;
          
           /** Caso di html workArea invocata esternamente */
           if(HTML)
           {
            if (typeObj.equals("C"))
            {
               snome=h.getImg(_CARTELLAGDC);
               snome+=h.getNbsp()+nome; 
            }
            if (typeObj.equals("Q"))
            { 
                snome=h.getImg(_QRYSEARCH);
                snome+=h.getNbsp()+nome; 
            }
            if (typeObj.equals("D"))
            {
               viewDoc=decodeViewD(nome,cm,area,cr,tipo_icona,idtipoDoc,id_oggetto,idCartProv,modifica,check_livello);

               if(icona)
                 snome=h.getTable("100%",h.getTR(h.getTD("center","5%",viewDoc)+h.getTD("","95%",monoRecord)));
               else
                 snome=h.getTable("100%",h.getTR(h.getTD("","100%",monoRecord)));
            }
           }
           else
           {
        	  if ((typeObj.equals("C")) || (typeObj.equals("X")))
        	  {
        		  String link_img,src_icona;
        		  String icona_default,path_icona;
             	  if (ordina==1) 
             		icona_default=_CARTELLAGDC;
     			  else
     				icona_default=_COLL_CARTELLAGDC;
             	
        		  try {
        			src_icona = getIcona(profilo,idtipoDoc,"0","N",icona_default); 
        			if(src_icona.indexOf("/")>0)
        			 ico = src_icona.substring(0,src_icona.indexOf("/"));
        		
        			
        			if(src_icona.equals(icona_default))
        			 path_icona ="";
        			else
        			 path_icona = "./icone/";	
        			
        		  }
        		  catch (Exception e) {
        			  log.log_error("CCS_WorkArea::getNome::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
        			  		        + "("+profilo+","+idtipoDoc+",0,'N','"+icona_default+"') "
        			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
        			  path_icona = "";	 
        			  src_icona = icona_default;
				  }
        		  
          		  if(JDMS_LINK.equals("S")){  
          			  if(ico!=null && !ico.equals("")){
	          			  if(mapIconaTooltip.get(ico)!=null && !mapIconaTooltip.get(ico).equals(""))
	          				  tooltipIcona = mapIconaTooltip.get(ico);
	          			  else {
	          				  tooltipIcona = getTooltipIcona(ico); 
	          				  
	          				  if(tooltipIcona!=null && !tooltipIcona.equals(""))
	          				    mapIconaTooltip.put(ico,tooltipIcona);
	          				  else
	          					tooltipIcona = "Esplora la Cartella";  
	          			  }
          			  }
          			  else
          			   tooltipIcona = "Esplora la Cartella";
          		  }  
          		  else
          			  tooltipIcona = "Esplora la Cartella";
        		  
        		  String disabilitaC = verificaParametroGet("disabilitaC",req.getParameter("disabilitaC"));        		  
        	 
        		  if(disabilitaC!=null && disabilitaC.equals("S")){
                      link_img=h.getImg("",tooltipIcona,path_icona+src_icona);
        		  }	
        		  else
        			link_img=h.getAncore("#","linkOggetto('"+"WorkArea.do?idCartella=C"+id_oggetto+sCollegamento+sIdCartAppartenenza+redirectApp+"');","",h.getImg("cursor: hand",tooltipIcona,path_icona+src_icona));
    
        		  snome=h.getTable("100%",h.getTR(h.getTD("center","5%",link_img)+h.getTD("","95%",monoRecord)));
              }
          
	          if (typeObj.equals("Q"))
	          { 
	        	  String link_img,url;
	        	  String gdc_link="../common/WorkArea.do?idQuery="+id_oggetto+"&idCartAppartenenza="+idCartProv+sCollegamento+"&tipoUso=R"+redirectApp;
	        	  String src_icona,path_icona,icona_default=_QRYSEARCH;
	        	  
	        	  
	        	  String s=ricercaMod;
	        	  if((s!=null) && (!s.equals("")))
	        	  { 	  
	        		String sarea=s.substring(0,s.indexOf("@"));
	        		String scm=s.substring(s.indexOf("@")+1,s.length());
	        		String parametri="idQuery="+id_oggetto+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+idCartProv+sCollegamento+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
	        		url="../restrict/ServletRicercaModulistica.do?"+parametri;
	        	  }
	        	  else
	        	     url="WorkArea.do?idQuery="+id_oggetto+"&idCartAppartenenza="+idCartProv+sCollegamento+redirectApp;
	        	  
        		  try {
        			src_icona = getIcona(profilo,idtipoDoc,"0","N",icona_default); 
        			if(src_icona.indexOf("/")>0)
        			 ico = src_icona.substring(0,src_icona.indexOf("/"));
        			if(src_icona.equals(icona_default))
           			 path_icona ="";
           			else
           			 path_icona = "./icone/";	
        		  }
        		  catch (Exception e) {
        			  log.log_error("CCS_WorkArea::getNome::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
        			  		        + "("+profilo+","+idtipoDoc+",0,'N','"+icona_default+"') "
        			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
        			  path_icona = "";	 
        			  src_icona = icona_default;
				  }
	              
        		  if(JDMS_LINK.equals("S")){  
          			  if(ico!=null && !ico.equals("")){
	          			  if(mapIconaTooltip.get(ico)!=null && !mapIconaTooltip.get(ico).equals(""))
	          				  tooltipIcona = mapIconaTooltip.get(ico);
	          			  else {
	          				  tooltipIcona = getTooltipIcona(ico); 
	          				
	          				  if(tooltipIcona!=null && !tooltipIcona.equals(""))
		          				mapIconaTooltip.put(ico,tooltipIcona);
		          			  else
		          				tooltipIcona = "Esegui la Ricerca";  
	          			  }
          			  }
          			  else
          			   tooltipIcona = "Esegui la Ricerca";
          		  }  
          		  else
          			  tooltipIcona = "Esegui la Ricerca";
        		  
	          	  link_img=h.getAncore("#","linkOggetto('"+url+"');","",h.getImg("",tooltipIcona,path_icona+src_icona));
	              	              
	              snome=h.getTable("100%",h.getTR(h.getTD("center","5%",link_img)+h.getTD("","95%",monoRecord)));
		 	  }
          
	          if (typeObj.equals("D"))
	          {
	        	 viewDoc=decodeViewD(nome,cm,area,cr,tipo_icona,idtipoDoc,id_oggetto,idCartProv,0,check_livello);
	        	 String link = getLinkMonoRecord(nome,idtipoDoc,id_oggetto,area,cm,cr,idCartProv,modifica,check_livello);
	        	 monoRecord = monoRecord.replaceAll(":URL_DOCUMENTOVIEW", link);
	        	 log.log_info("LINK MONORECORD - URL_DOCUMENTOVIEW:"+link);
	             snome=h.getTable("100%",h.getTR(h.getTD("center","5%",viewDoc)+h.getTD("","95%",monoRecord)));
		      }
           }   
           return snome;
   }  
   
   
   private String getIcona(String p_id_documento, String id_tipodoc, String p_num_tag, String p_jdms_link_sn,String p_icona_default) throws Exception
   {
           String sql="",nomeIcona="";
           try 
           {	
        	if(idCartella!=null && idCartella.indexOf("C")!=-1) 
        	  idCartella = idCartella.substring(1,idCartella.length());	
        	   
        	sql="select F_ICONA_WAREA("+p_id_documento+","+id_tipodoc+","+p_num_tag+",'"+p_jdms_link_sn+"','"+p_icona_default+"','N','"+idCartella+"') from dual";
			log.log_info("CCS_WorkArea::getIcona():: Recupero SRC ICONA - sql::"+sql);

            dbOp.setStatement(sql);
            dbOp.execute();
			ResultSet rst = dbOp.getRstSet();
			if(rst.next())
               nomeIcona= rst.getString(1);
           }
           catch ( SQLException e ) {
        	log.log_error("CCS_WorkArea::getIcona():: Recupero SRC ICONA - sql::"+sql+" - Erroe:"+e.getMessage());
            throw e;
           }  
           return nomeIcona;
   }
   
   private String getTooltipIcona(String nomeIcona) throws Exception
   {
           String sql="",tooltip="";

           try 
           {	
        	   
        	sql=" SELECT nvl(tooltip,'') tooltip FROM icone WHERE icona = '"+nomeIcona+"'";
			log.log_info("CCS_WorkArea::getTooltipIcona():: Recupero tooltip dell'icona "+nomeIcona+"- sql::"+sql);
        	
        	dbOp.setStatement(sql);
			dbOp.execute();
			ResultSet rst = dbOp.getRstSet();
			if(rst.next()){
			 tooltip = rst.getString("tooltip");
			} 
           }
           catch ( SQLException e ) {
        	log.log_error("CCS_WorkArea::getTooltipIcona():: Recupero tooltip dell'icona "+nomeIcona+"- sql::"+sql+" - Errore:"+e.getMessage());
	        throw e;
           }  
           log.log_info("CCS_WorkArea::getTooltipIcona():: Restituisco il tooltip dell'icona "+nomeIcona+" - tooltip::"+tooltip);
           return tooltip;
   }
   
   
   /**
     * Gestione della Nome per il Documento
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa nome
     * 
	 */
   private String decodeViewD(String nome,String cm,String area,String cr,String nome_icona,String idtipoDoc,String id_oggetto,String idCartProv,int modifica,String check_livello) throws Exception
   {     
           String decode,id,stato,Prov,url,tooltipIcona="",ico="";
       	   String src_icona, icona_default, icona_path;
                  
	       if (idQuery!=null)
	         id=idQuery;       
	       else  
	         id="-1";
	  
	       if (nome.equals("CO"))
             stato="CO";
	       else
	    	if (nome.equals("AN"))
	    	  stato="AN";
	    	else
	    	  stato="BO";
             
	       if (idQuery==null) 
            Prov="C";
	       else
            Prov="Q";
           
	       if (Prov.equals("Q"))
			     url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
	           else
				 url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";  
	       
	       if(HTML)
	       {
	    	if(icona)
	      	  decode=h.getAncore("text-decoration : none","#",h.ServletModulisticaPop(cm,area,cr,url_servlet,"Visualizza Documento"),"",h.getImgM("Visualizza Documento",decodeD(nome,"","L")));
	    	else
	    	  decode="";
	       }
	       else
	       {
		    	String url_page="";
		    	if(mapLink.get(idtipoDoc+"@"+5)!=null)
			    {
			      	JDMSLink link=mapLink.get(idtipoDoc+"@"+5);
			       	String jdms_url=link.getURL();
			       	url_page=getIconaPersonalizzata(jdms_url,id_oggetto,"D",area,cm,cr,"",idCartProv,id,"R","ServletModulisticaDocumento",stato,Prov,URLEncoder.encode(url));	  
			       	
			    }
		        else {
		        	if (modifica==1)
		            	url_page=decodeModificaD(nome,idtipoDoc,id_oggetto,area,cm,cr,idCartProv,check_livello);
		        	else
		        		url_page=h.DocumentoViewPop(idtipoDoc,id_oggetto,"R",cm,area,cr,idCartProv,id,Prov,stato,url);
			    }
		    	   	        		
	         	icona_default = decodeD(nome,null,"L");
	         	icona_path = icona_default;
	       		  
	    		try {
	        		src_icona = getIcona(id_oggetto,idtipoDoc,"0","N",icona_default); 
	        		if(src_icona.indexOf("/")>0)
	        		 ico = src_icona.substring(0,src_icona.indexOf("/"));	
	        		
	        		if(src_icona.equals(icona_default))
	        		 icona_path = src_icona;
	           		else
	           		 icona_path = "src=\"./icone/"+src_icona+"\"";
	        	}
	        	catch (Exception e) {
	        		  log.log_error("CCS_WorkArea::decodeViewD::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
	        			  		        + "("+id_oggetto+","+idtipoDoc+",0,'N','"+icona_default+"') per il documento id_documento:"+id_oggetto
	        		  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
	        		  src_icona = icona_default;
				}
	    		    		
	    		 if(JDMS_LINK.equals("S")){  
	     			  if(ico!=null && !ico.equals("")){
	         			  if(mapIconaTooltip.get(ico)!=null && !mapIconaTooltip.get(ico).equals(""))
	         				  tooltipIcona = mapIconaTooltip.get(ico);
	         			  else {
	         				  tooltipIcona = getTooltipIcona(ico); 
	         				
	         				  if(tooltipIcona!=null && !tooltipIcona.equals(""))
		          				mapIconaTooltip.put(ico,tooltipIcona);
		          			  else
		          			    tooltipIcona = "Visualizza Documento";  
	         			  }
	     			  }
	     			  else
	     			   tooltipIcona = "Visualizza Documento";   
		    		}  
		    		else
		        	  tooltipIcona = "Visualizza Documento";
	    		
	    		
	    		if (modifica==1) {
	    			decode = url_page;
	    		}	
	    		else
	    			decode=h.getAncore("text-decoration : none","#","if(linkOggettoPopup()){"+url_page+"}","",h.getImgMultipla(tooltipIcona, icona_path));	
	        	
	       }        
	       
	       return decode;     
   } 
   
   private String getLinkMonoRecord(String nome,String idtipoDoc,String id_oggetto,String area,String cm,String cr,String idCartProv,int Modifica,String check_livello) throws Exception
   {     
           String url_page="";
           String id,Prov,stato,url;
         
           if (idQuery!=null)
             id=idQuery;       
           else  
             id="-1";
              
           if (idQuery==null) 
             Prov="C";
           else
             Prov="Q";
              
           if (nome.equals("CO"))
             stato="CO";
           else
            if (nome.equals("AN"))
              stato="AN";
            else
              stato="BO";   
         
           if (Prov.equals("Q"))
		     url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
           else
			 url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";  
                      
           /** Controllo se il documento è bloccato */
           if(check_livello.equals("")) {
        	
        	   String rw = "R";
        	   if ((Modifica==1) && (nome.equals("BO")))
        		   rw="W";
        	   else
        		   rw="R";
        	   
        	   if(JDMS_LINK.equals("S")){  

	   		    	if(mapLink.get(idtipoDoc+"@"+5)!=null)
	   			    {
	   			      	JDMSLink link=mapLink.get(idtipoDoc+"@"+5);
	   			       	String jdms_url=link.getURL();
	   			       	url_page=getIconaPersonalizzata(jdms_url,id_oggetto,"D",area,cm,cr,"",idCartProv,id,rw,"ServletModulisticaDocumento",stato,Prov,URLEncoder.encode(url));	  
	   			    }
	   		    	else
	   		    	  url_page = "popupFullScreen('"+h.DocumentoViewLink(idtipoDoc,id_oggetto,rw,cm,area,cr,idCartProv,id,Prov,stato,url)+"',''); return false;";
	         	   
        	   }
        	   else        	   
        		   url_page = "popupFullScreen('"+h.DocumentoViewLink(idtipoDoc,id_oggetto,rw,cm,area,cr,idCartProv,id,Prov,stato,url)+"',''); return false;";
        	   
 	       }
           else {
        	   url_page=h.MessagePagePop("Documento bloccato.");
           }
           
         return url_page;     
   }   
   
   /**
    * Gestione dell'indice di conservazione di un documento.
    * 
    */
    private String getConservazione(String typeObj,String cons,String id_oggetto) throws Exception
	{
	        String conservazione=_IMGVUOTO;
	        String terna,area=null,cm=null,cr=null;
	        String[] s=null;
	        
	        if (typeObj.equals("D") && cons!=null)
	        {
	        	if(cons.equals(_WORKING))
	        	  conservazione=h.getImgHand("Conservazione in elaborazione", _CONS_WORKING);
	        	else
	        	  if(!cons.equals(""))	
	        	  {	 
	        		 try
	        		 {
	        		   terna= (new DocUtil(vu)).getAreaCmCrByIdDocumento(cons);  
	        		 }
	        		 catch (Exception e) {     
	        			 terna="";
	        	     }
	        		  
	        		 if(terna!=null && !terna.equals(""))
		               s=terna.split("@");
	        		 
 	        		 if(s!=null && s.length!=0)
 	        		 { 
 	        		   area=s[0];
 	        		   cm=s[1];
 	        		   cr=s[2];
 	        		 }
 	        		 
 	        		 if(area!=null && cm!=null && cr!=null)
 	        		  conservazione=h.getAncore("text-decoration : none","#","if(linkOggettoPopup()){"+h.ServletModulisticaPop(cm,area,cr,"../restrict/VisualizzaModello.do?","Gestione conservazione",false)+"}","",h.getImgHand("Documento conservato",_CONS_CONSERVA_LOG));
 	        		 	
 	        	  } 	         
	        }
	        return conservazione;
	}
    
    
    /**
     * Gestione archiviazione documento.
     * 
     *
     private String getArchiviazione(String typeObj,String arch,String id_oggetto) throws Exception
 	{
 	        String archiviazione="",terna,area=null,cm=null,cr=null;
 	        String[] s=null;
 	       
 	        if (typeObj.equals("D") && arch!=null)
 	        {
 	        	if(arch.equals(_WORKING))
 	        		archiviazione=h.getImgHand("Documento da archiviare...",_ARCHI_WORKING);
 	        	else
 	        	  if(!arch.equals(""))	
 	        	  {
 	        		 try
	        		 {
	        		   terna= (new DocUtil(vu)).getAreaCmCrByIdDocumento(arch);  
	        		 }
	        		 catch (Exception e) {     
	        			 terna="";
	        	     }
 	        		 
	        		 if(terna!=null && !terna.equals(""))
	 		           s=terna.split("@");
	        		 
	        		 if(s!=null && s.length!=0)
 	        		 { 
 	        		   area=s[0];
 	        		   cm=s[1];
 	        		   cr=s[2];
 	        		 }
 	        		 
 	        		 if(area!=null && cm!=null && cr!=null)
 	        		   archiviazione=h.getAncore("text-decoration : none","#","if(linkOggettoPopup()){"+h.ServletModulisticaPop(cm,area,cr,"ServletModulisticaDocumento.do?","Getione Archiviazione",true)+"}","",h.getImgHand("Archiviazione del Documento",_ARCHI_CONSERVA_LOG));
 	        		 else
 	        		   archiviazione+=h.getImg(_VUOTO); 
 	        	  }
 	  	        	        	
 	        	archiviazione+=h.getNbsp();
 	        	archiviazione+=h.getNbsp();  
 	         
 	        }
 	        return archiviazione;
 	}
 	*/
   
   /**
     * Gestione degli allegati per il Documento
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa allegati
     * 
	 */
   private String getAllButton(String typeObj,String idDoc, int lettura_allegati, int modifica_allegati,String competenze_allegati) throws Exception
   {
           String allegati="";
           String allButton="";    
           int visualizza_allegati = lettura_allegati + modifica_allegati;
           
  
           if(HTML)
        	 allButton=h.getNbsp();
           else
           {
	         //Solo per i Dcoumenti   
	         if (typeObj.equals("D"))
	         {    
        	 	 //Se non sono applicate le competenze sugli allegati si procede normalmente  
	        	 if(competenze_allegati.equals("N")){
	        		allegati=getAllegati(idDoc);
	             	if (!allegati.equals(""))
	                 {
	             		sequence++;
	                    allButton+="<span id=\""+idDoc+"\" class=\"context-menu-one btn btn-neutral\">"
		             			+"<img title=\"Gestisce gli allegati del documento\" align=\"absMiddle\" style=\"cursor:hand;\"	src=\"./"+_DOCUMENTATTACHMENT+"\">"
		             			+"</span>";
	                 }
	        	 }
	        	 else {
	        		//Altrimenti viene controllato se esiste almeno una delle due competenze LA o UA 
	        		 if(visualizza_allegati>0){
	             		allegati=getAllegati(idDoc);
	             		if (!allegati.equals(""))
	                     {
	                        sequence++;	                        
	                    	allButton+="<span id=\""+idDoc+"\" class=\"context-menu-one btn btn-neutral\">"
			             			+"<img title=\"Gestisce gli allegati del documento\" align=\"absMiddle\" style=\"cursor:hand;\"	src=\"./"+_DOCUMENTATTACHMENT+"\">"
			             			+"</span>";
	                     }
	             	}      
	        	 }   
        	 } 
           }
           
           if (allButton.equals(""))
        	   allButton=_IMGVUOTO;
           
           return allButton;
   }
   
   /**
    * Gestione dei flussi per il Documento
    * 
    * @param  area 		
    * @param  cm
    * @param  cr
    * @return String link alla lista dei flussi
    * 
	 */
  private String getFlussiButton(String typeObj,String area,String cm,String cr,String profilo,String id_oggetto) throws Exception
  {
          String flussiButton=_IMGVUOTO;
          
          if(HTML)
        	  flussiButton=h.getNbsp();
          else
          {
              if(typeObj.equals("D"))
              	flussiButton=h.getAncore("#",h.ElencoFlussiPerDoc(area,cm,cr,profilo),"",h.getImgHand("Elenco di Flussi per il Documento",_WORKFLOW));
          }
          return flussiButton;
  }
  
  /**
   * Gestione delle icone personalizzate
   * 
   * @param  url 	  	url della pagina da indirizzarsi
   * @param  icona	  	src dell'icona 
   * @param  tooltip  	descrizione dell'icona
   * @return String   	link alla pagina
   * 
	 */
  private String getIconaPersonalizzata(String url,String icona,String tooltip,String idOggetto,String tipoOggetto,String area,String cm,String cr,String profilo,String idCartProvenienza,String idQueryProvenienza,String rw) throws Exception {
	      
	  	  JDMSLinkParser p = new JDMSLinkParser(req,idOggetto,tipoOggetto,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,rw);
          String urlpagina = p.bindingDeiParametri(url);
	      return h.getAncore("#","if(linkOggettoPopup()){"+urlpagina+"}","",h.getImgHand(tooltip,icona));
  }
  
  /**
   * Gestione delle icone personalizzate per i documenti
   * 
   * @param  url 	  	url della pagina da indirizzarsi
   * @param  icona	  	src dell'icona 
   * @param  tooltip  	descrizione dell'icona
   * @return String   	link alla pagina
   * 
	 */
  private String getIconaPersonalizzata(String url,String icona,String tooltip,String idOggetto,String tipoOggetto,String area,String cm,String cr,String profilo,String idCartProvenienza,String idQueryProvenienza,String rw,String mvpg,String stato,String provenienza,String gdc_link) throws Exception {	      	     
	  	  JDMSLinkParser p = new JDMSLinkParser(req,idOggetto,tipoOggetto,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,rw,mvpg,stato,provenienza,gdc_link);
	      String urlpagina = p.bindingDeiParametri(url);
	      return h.getAncore("#","if(linkOggettoPopup()){"+urlpagina+"}","",h.getImgHand(tooltip,icona));
  }
  
  /**
   * Gestione delle icone personalizzate per i documenti
   * 
   * @param  url 	  	url della pagina da indirizzarsi
   * @return String   	url alla pagina
   * 
	 */
  private String getIconaPersonalizzata(String url,String idOggetto,String tipoOggetto,String area,String cm,String cr,String profilo,String idCartProvenienza,String idQueryProvenienza,String rw,String mvpg,String stato,String provenienza,String gdc_link) throws Exception {
	      JDMSLinkParser p = new JDMSLinkParser(req,idOggetto,tipoOggetto,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,rw,mvpg,stato,provenienza,gdc_link);
	  	  String urlpagina = p.bindingDeiParametri(url);
	      return urlpagina;
  }
  
   /**
     * Gestione elenco collegamenti a Cartelle e Riferimenti 
     * per l'oggetto di tipo Cartella o Documento
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa elenco
     * 
	 */
   private String getElencoCartellePerDoc(String typeObj,String idCartProv,String id_oggetto,String nome,String profilo) throws Exception
   {
           String elenco=h.getImg("",_LISTDS);
           String idQueryProv,Prov;
         
	       if (idQuery!=null)
	         idQueryProv=idQuery;       
	       else  
	         idQueryProv="-1";
	              
           if (idQuery==null) 
            Prov="C";
           else
            Prov="Q";
         
           /** Nel caso di un Documento */ 
           if (typeObj.equals("D"))
            elenco=h.getAncore("#","if(linkOggettoPopup()){"+h.ElencoCartellePerDoc(id_oggetto,"",idCartProv,idQueryProv,Prov,"D",nome)+"}","",h.getImgHand("Elenco di Collegamenti e Riferimenti per il Documento",_LIST));
         
           /** Nel caso di una Cartella */ 
           if (typeObj.equals("C"))
            elenco=h.getAncore("#","if(linkOggettoPopup()){"+h.ElencoCartellePerDoc(profilo,id_oggetto,idCartProv,idQueryProv,Prov,"C",nome)+"}","",h.getImgHand("Elenco Collegamenti per la Cartella",_LIST));
           return elenco;
   }
   
   /**
    * Gestione modiifca per l'oggetto.
    * 
    * @param  rs 		ResultSet
    * @return String 	stringa modifica
    * 
	 */
   private String getModifica(String typeObj,int Modifica,String id_oggetto,String idCartProv,String nome,String idtipoDoc,String area,String cm,String cr,String ricercaMod,String check_livello,String profilo,String tipo_icona) throws Exception
   {
           String smodifica=h.getImg("",_EDITDS);//_IMGVUOTO;
            
           /** Nel caso di una Cartella o Cartella Collegata */ 
           if ( (typeObj.equals("C") || typeObj.equals("X")))
           {
        	  String src_icona,icona_default,path_icona = "";
          	  if (Modifica==1) 
          		icona_default=_EDITFOLDER;
  			  else
  				icona_default=_PROPRIETAFOLDER;
          	             	  
     		  try {
     			src_icona = getIcona(profilo,idtipoDoc,"5","N",icona_default); 
     			if(src_icona.equals(icona_default))
     	   		  path_icona = "";
     			else
     	          path_icona = "./icone/";  
    	        
     		  }
     		  catch (Exception e) {
     			  log.log_error("CCS_WorkArea::getModifica::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
     			  		        + "("+profilo+","+idtipoDoc+",5,'N','"+icona_default+"') di una Cartella idCartella:"+id_oggetto
     			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
     			  path_icona = "";	 
     			  src_icona = icona_default;
			  }   
        	   
        	 if(Modifica==1)
        	  smodifica=h.getAncore("#","if(linkOggettoPopup()){"+h.CartellaMaintPop("C","","W",id_oggetto,idCartProv)+"}","",h.getImgHand("Modifica la Cartella",path_icona+src_icona));
             else
        	  smodifica=h.getAncore("#","if(linkOggettoPopup()){"+h.CartellaMaintPop("C","","R",id_oggetto,idCartProv)+"}","",h.getImgHand("Proprietà della Cartella",path_icona+src_icona));
           }
         
           /** Nel caso di una Query */ 
           if (typeObj.equals("Q") && Modifica==1)
           {
        	  String src_icona,icona_default="editQry.png",path_icona = _PATHIMG;
   			  try {
      			src_icona = getIcona(profilo,idtipoDoc,"5","N",icona_default); 
      			if(src_icona.equals(icona_default))
       	   		  path_icona = _PATHIMG;
       			else
       	          path_icona = "./icone/";  
      		  }
      		  catch (Exception e) {
      			  log.log_error("CCS_WorkArea::getModifica::getIcona(p_id_documento,id_tipodoc,p_num_tag,p_jdms_link_sn,p_icona_default):" 
      			  		        + "("+profilo+","+idtipoDoc+",5,'N','"+icona_default+"') di una Query idQuery:"+id_oggetto
      			  		        + "- Problemi durante il recupero del nome dell'icona - :"+e.getMessage());
      			  path_icona = _PATHIMG;	 
      			  src_icona = icona_default;
 			  }   
        	   
        	 String s=ricercaMod;
	         if((s!=null) && (!s.equals("")))
        	   smodifica=h.getAncore("#","if(linkOggettoPopup()){"+h.CartellaMaintPop("Q","R","W",id_oggetto,idCartProv)+"}","",h.getImgHand("Modifica la Ricerca",path_icona+src_icona));
	         else
	           smodifica=h.getAncore("#","if(linkOggettoPopup()){"+h.CartellaMaintPop("Q","","W",id_oggetto,idCartProv)+"}","",h.getImgHand("Modifica la Ricerca",path_icona+src_icona));
	       }
         
           /** Nel caso di una Documento */  
          if (typeObj.equals("D"))
              smodifica=decodeViewD(nome,cm,area,cr,tipo_icona,idtipoDoc,id_oggetto,idCartProv,1,check_livello);

          return smodifica;
   }
   
   /**
     * Gestione elimina per l'oggetto.
     * Non esiste l'operazione di eliminazione sulle cartelle collegate.
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa elimina
     * 
	 */
   private String getElimina(String typeObj,int Elimina,String id_oggetto,String idCartProv) throws Exception
   {
           String selimina=h.getImg("",_ANNULLADS);//_IMGVUOTO;
             
           /** Nela caso di una Cartella */ 
           if ( typeObj.equals("C") && Elimina==1 )
             selimina=h.getAncore("#","if(linkOggettoPopup()){"+h.VistaCartDelPop("la Cartella","eliminare",id_oggetto,idCartProv,typeObj)+"}","",h.getImgHand("Elimina la cartella",_ANNULLA));
           
           /** Nela caso di una Query */ 
           if (typeObj.equals("Q") && Elimina==1)
             selimina=h.getAncore("#","if(linkOggettoPopup()){"+h.VistaCartDelPop("la Ricerca","eliminare",id_oggetto,idCartProv,"Q")+"}","",h.getImgHand("Elimina la Ricerca",_ANNULLA));
           
           /** Nela caso di un Documento */ 
           if (typeObj.equals("D") && Elimina==1)
             selimina=decodeEliminaD(id_oggetto,idCartProv);
           return selimina;
   } 
   
   /**
     * Gestione competenze per l'oggetto.
     * 
     * @param  rs 		ResultSet
     * @return String 	stringa competenze
     * 
	 */
   private String getCompetenze(String typeObj,String idCartProv,String id_oggetto,int Competenze) throws Exception
   {
           String scompetenze="";
           String id,Prov;
         
           if (idQuery!=null)
           {
             id=idQuery;       
             Prov="Q";
           }
           else  
           {
        	 id="-1";
        	 Prov="C";
           }
         
          /** Nel caso di Cartella o Cartella Collegata */          
          if (typeObj.equals("C") || typeObj.equals("X"))
          {
             if (Competenze==1 )
               scompetenze=h.getAncore("#","if(linkOggettoPopup()){"+h.CompetenzePop(id_oggetto,"C","N",idCartProv,id,Prov)+"}","",h.getImgHand("Gestisce le competenze della Cartella",_COMP));
             else
               scompetenze=h.getAncore("#","if(linkOggettoPopup()){"+h.CompetenzePop(id_oggetto,"C","Y",idCartProv,id,Prov)+"}","",h.getImgHand("Visualizza le competenze della Cartella",_COMPVIEW));
          }
          
          /** Nel caso di una Query */
          if (typeObj.equals("Q"))
          {
             if(Competenze==1)
               scompetenze=h.getAncore("#","if(linkOggettoPopup()){"+h.CompetenzePop(id_oggetto,"Q","N",idCartProv,id,Prov)+"}","",h.getImgHand("Gestisce le competenze della Ricerca",_COMP));
             else
               scompetenze=h.getAncore("#","if(linkOggettoPopup()){"+h.CompetenzePop(id_oggetto,"Q","Y",idCartProv,id,Prov)+"}","",h.getImgHand("Visualizza le competenze della Ricerca",_COMPVIEW));
          }
             
          /** Nel caso di un Documento */
          if (typeObj.equals("D"))
          {
             if (Competenze==1)
               scompetenze=h.getAncore("#","if(linkOggettoPopup()){"+h.CompetenzePop(id_oggetto,"D","N",idCartProv,id,Prov)+"}","",h.getImgHand("Gestisce le competenze del Documento",_COMP));
             else
               scompetenze=h.getAncore("#","if(linkOggettoPopup()){"+h.CompetenzePop(id_oggetto,"D","Y",idCartProv,id,Prov)+"}","",h.getImgHand("Visualizza le competenze del Documento",_COMPVIEW));
          }          
          return scompetenze;
   } 
   
   /**
    * Gestione della moorecord per un oggetto.
    * 
    * @param  idDoc 		idDocumento
    * @param  tipoOggetto	tipo oggetto
    * @param  nomeOggetto	nome
    * @param  req			HttpServletRequest
    * @return String 		stringa monoRecord
    * 
 	 */
   private String getRigheMonoRecord(String idDoc,HttpServletRequest req) throws Exception
   {
  	       String righe;
           Environment vu = new Environment(utente,utente,"MODULISTICA","ADS",null,dbOp.getConn());
           try
           {
        	   Vector<String> vDoc=new Vector<String>();
        	   vDoc.add(idDoc);
        	   MonoRecordIQuery m = new MonoRecordIQuery(vDoc,vu,req,utente);
        	   righe=m.creaRighe(false).get("ID"+idDoc).toString();
        	 }
           catch (Exception e) {
        	   throw e;
        	  }
           return righe;
   }
     
  /**
   * Gestione della moorecord per un oggetto.
   * 
   * @param  idDoc 			idDocumento
   * @param  tipoOggetto	tipo oggetto
   * @param  nomeOggetto	nome
   * @param  req			HttpServletRequest
   * @return String 		stringa monoRecord
   * 
	 */
  private Properties getRigheMonoRecords(Vector<String> idDoc,HttpServletRequest req,boolean stampa) throws Exception
  {
 	     Properties righe;
          Environment vu = new Environment(utente,utente,"MODULISTICA","ADS",null,dbOp.getConn());
          try
          {
       	   MonoRecordIQuery m = new MonoRecordIQuery(idDoc,vu,req,utente);
       	   righe=m.creaRighe(stampa);  
       	 }
          catch (Exception e) {
       	   throw e;
       	  }
          return righe;
  }
  
  /**
   * Gestione della moorecord per un oggetto.
   * 
   * @param  idDoc 			idDocumento
   * @param  tipoOggetto	tipo oggetto
   * @param  nomeOggetto	nome
   * @param  req			HttpServletRequest
   * @return String 		stringa monoRecord
   * 
	 */ 
 private Vector<String> getRigheMonoRecordsFromStampa(Vector<String> idDoc,HttpServletRequest req,boolean stampa) throws Exception
 {
         Vector<String> righe;
         Environment vu = new Environment(utente,utente,"MODULISTICA","ADS",null,dbOp.getConn());
         try
         {
      	   MonoRecordIQuery m = new MonoRecordIQuery(idDoc,vu,req,utente);
      	   righe=m.creaRigheFromStampa(stampa);  
      	 }
         catch (Exception e) {
      	   throw e;
      	  }
         return righe;
 }
   
   /**
    * Gestione della moorecord per un oggetto.
    * 
    * @param  idDoc 		idDocumento
    * @param  tipoOggetto	tipo oggetto
    * @param  nomeOggetto	nome
    * @param  req			HttpServletRequest
    * @return String 		stringa monoRecord
    * 
	 */
  private int[] getObjectCompetenza(String idOggetto,String tipoOggetto) throws Exception
  {
		  String sql;
		  int[] competenza=new int[4]; 
	      //IDbOperationSQL dbOpSQL=null;
	
	      if((tipoOggetto.equals("C")) || (tipoOggetto.equals("X"))) {
	    	  tipoOggetto="VIEW_CARTELLA";
	    	  idOggetto="f_idview_cartella ("+idOggetto+")";
	      }
	      else
	    	if(tipoOggetto.equals("Q"))
	    		tipoOggetto="QUERY";	  
	    	else
	    		tipoOggetto="DOCUMENTI";		

	      try
	      {			    
		   sql="select "+GDM(tipoOggetto,idOggetto,"L")+" let, "+GDM(tipoOggetto,idOggetto,"U")+" mod,"+GDM(tipoOggetto,idOggetto,"D")+" del, "+GDM(tipoOggetto,idOggetto,"M")+" man from dual";
           dbOp.setStatement(sql);
		   dbOp.execute();
		   ResultSet rs = dbOp.getRstSet();
		   if ( rs.next() ) {
			  competenza[0]= rs.getInt(1);
			  competenza[1]= rs.getInt(2);
			  competenza[2]= rs.getInt(3);
			  competenza[3]= rs.getInt(4);
		    }
	      }
	      catch ( SQLException e ) {
	        throw e;
	      }  
	     return competenza; 
  }
    
   /**
     * Gestione della lista di allegati per un Documento.
     * 
     * @param  idDoc 		idDocumento
     * @return String 		stringa elenco allegati
     * 
	 */
   private String getAllegati(String idDoc) throws Exception
   {
          String sql,row="";
          try
          {			    
		   sql="select allegato from allegati_lista_html where id_documento = :IDDOCUMENTO";
		   dbOp.setStatement(sql);
		   dbOp.setParameter(":IDDOCUMENTO",idDoc);
		   dbOp.execute();
		   ResultSet rs = dbOp.getRstSet();
		   int count=1;
		   while ( rs.next() ) 
		   {
             String div=rs.getString(1);
             String stile="style=\"BORDER-BOTTOM: 1px outset;z-index: "+count+";margin-top: "+(count*25)+"px; \"";
             row+="<div "+stile+div.substring(div.indexOf("<div")+4);
             count++;
		   }

          }
          catch ( SQLException e ) {
             throw e;
          }  
         return row;
  }
   
   
  private void costruisciJDMSLink(Vector<String> vIDDOCS) throws Exception
  {
	  	  String sql="";
	  	  String seqID;
	  	  try
	      {
	  	    seqID=" D.ID_DOCUMENTO IN  "+getSequenza(vIDDOCS);
	    	sql="  SELECT distinct J.ID_TIPODOC,TAG, J.URL, J.ICONA, J.TOOLTIP,I.NOME ";
	    	sql+=" FROM JDMS_LINK J, DOCUMENTI D, ICONE I ";
	    	sql+=" WHERE "+seqID+" AND ";
	    	sql+=" D.ID_TIPODOC = J.ID_TIPODOC";
	    	sql+=" AND J.ICONA=I.ICONA(+) ";
	    	sql+=" order by 1,2 ";
	    	
	    	
	  	    dbOp.setStatement(sql);
	  	    dbOp.execute();
	  	    ResultSet rs = dbOp.getRstSet();
			String idTipoDoc="",tag="",url="",icona="",tooltip="",nomeIcona="";
			
	  	    while ( rs.next() ) 
			{
	  	    	idTipoDoc=rs.getString(1);
	  	    	tag=rs.getString(2);
	  	    	url=rs.getString(3);
	  	    	icona=rs.getString(4);
	  	    	tooltip=rs.getString(5);
	  	    	nomeIcona=rs.getString(6);
	  	    	
	  	    	JDMSLink jlink=new JDMSLink();
	  	    	jlink.setTAG(tag);
	  	    	jlink.setURL(url);
	  	    	jlink.setICONA(icona);
	  	    	jlink.setTOOLTIP(tooltip);
	  	    	jlink.setNOMEICONA(nomeIcona);	
	  	    	
		    	 /** Costruzione della lista di ICONE da verificare l'esistenza
		    	  *  e l'eventuale caricamento nel file system */
	            if(icona!=null && !icona.equals("@STANDARD@") )
	            {	  
	             if(listaICONE.indexOf(icona)==-1)
	             {
		   	       	 if(listaICONE.equals(""))
		   	       	   listaICONE+=icona;
		   	       	 else
		   	       	   listaICONE+=","+icona;	 
	             }
	            }
	  	    	
	  	    	mapLink.put(idTipoDoc+"@"+tag ,jlink);
	  	 	}

	        gestioneControlloIcone();
	        
	        }
	        catch ( SQLException e ) {
	          throw e;
	        }    
  }
  
  
  /**
   * Costruisce la sequanza di ID da inserire nella lista della select.
   * 
   * @param v   		vettore di id
   * @return String 	sequenza (id1,id2,id3,......)  
   * 
 */	   
  private String getSequenza(Vector<String> v)
  {
	       String seq="( ";
	        
	       for(int i=0;i<v.size();i++)
	       {
	    	 seq+="SELECT "+v.get(i)+ " FROM DUAL ";   
	    	 if(i!=(v.size()-1))
		      seq+=" UNION "; 
	       }
	       
	       if (v.size()==0) seq+="0";
	       
	       seq+=" )";
	       return seq;
  }
  
  
  /**
   * Recupera eventuale codice modello figlio selezionato 
   * nella ricerca tra i parametri di ricerca.
   * 
   * @param cm   		codice modello padre
   * @return String 	codice modello figlio oppure il codice modello
   * 					se non possiede modelli figli.  
   * 
  */	 
  private String getTipoDoc(String cm) throws Exception
  {   
	      String[] lista=null;
	      
	      try
	      {
	    	  if(listaCMF!=null && !listaCMF.equals(""))
		      {
	    		lista = listaCMF.split(",");  
		    	for(int i=0;i<lista.length;i++){
		    	    String[] l = lista[i].split("@");
		    		if(cm.equals(l[1]))
		    		 return l[0];
		    	}	
		      }  
	      }
	      catch ( Exception e ) {
	        log.log_error("CCS_WorkArea::getTipoDoc -- Recupero eventuale codice modello figlio - CM:"+cm);
	        throw e;
	      }
	      return cm;
  }
  
  
  private String getListaCMF() throws Exception
  {
	  	  String sql="";
	  	  String lista="";

	  	  try
	      {
	    	sql+=" SELECT DECODE (INSTR (q.filtro, 'RICERCAMODULISTICA_'),0, '', ";
	    	sql+=" ( DECODE (INSTR (q.filtro, '|'),0,'', ";
	    	sql+="   SUBSTR (q.filtro,INSTR (q.filtro, '|') +1,LENGTH (q.filtro)))";
	    	sql+=" )) listaCMF";
	    	sql+=" FROM QUERY q";
	    	sql+=" WHERE q.id_query ="+idQuery;
	  	    dbOp.setStatement(sql);
	  	    dbOp.execute();
	  	    ResultSet rs = dbOp.getRstSet();
			
	  	    if(rs.next()){
	  	    	lista  = rs.getString("listaCMF");
	  	    }

	        }
	        catch ( SQLException e ) {
	          throw e;
	        }   
	        return lista;
  }
  
  private void createSessionNRecords(String valore)
  {
	      String crpval =null;
	      
	      try {
	    	  crpval=CrypUtility.criptare(valore);
	      }
	      catch (Exception e) {
	    	   log.log_error("CCS_WorkArea::createSessionNRecords::Processo per criptare la variabile NRECORD"+e.getMessage());
		  }
	      req.getSession().setAttribute("NRECORD",crpval);
  }
  
  /**
   * Verifica impronta allegati
   * 
   * @param  rs 		ResultSet
   * @return String 	stringa impronta
   * 
 */
  private String getImpronta(String typeObj,String idDoc, int lettura_allegati, int modifica_allegati,String competenze_allegati) throws Exception {
  	String impronta = "";    
  	int visualizza_allegati = lettura_allegati + modifica_allegati;
  	
  	if(HTML || !Parametri.GENERA_IMPRONTA.equalsIgnoreCase("S")) {
  		impronta=h.getNbsp();
  	} else {
  		//Solo per i Dcoumenti   
  		if (typeObj.equals("D")) {    
  			//Se non sono applicate le competenze sugli allegati si procede normalmente  
  			if(competenze_allegati.equals("N")){
          //Creo pulsante verifica
  				impronta=h.getAncore("#","if(linkOggettoPopup()){"+h.popup("common/VerificaAllegati.do?lista="+idDoc,"Verifica impronta allegati","600","600", "0", "50",true)+"}","",h.getImgHand("Verifica allegati",_IMPR));
  			} else {
  				//Altrimenti viene controllato se esiste almeno una delle due competenze LA o UA 
  				if(visualizza_allegati>0){
  					//Creo pulsante verifica
    				impronta=h.getAncore("#","if(linkOggettoPopup()){"+h.popup("common/VerificaAllegati.do?lista="+idDoc,"Verifica impronta allegati","600","600", "0", "50",true)+"}","",h.getImgHand("Verifica allegati",_IMPR));
  				}      
  			}   
  		} 
  	}
  	return impronta;
  }
 
   
}
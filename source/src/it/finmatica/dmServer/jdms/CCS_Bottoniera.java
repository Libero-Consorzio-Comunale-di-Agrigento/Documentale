package it.finmatica.dmServer.jdms;

import it.finmatica.modulistica.connessioni.ConnessioneParser;
import it.finmatica.dmServer.motoreRicerca.GD4_Gestione_Query;
import it.finmatica.modulistica.connessioni.Connessione;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.dmServer.SOA.SOAXMLErrorRet;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.dmServer.SOA.SOAXMLDataRet;
import javax.servlet.http.HttpServletRequest;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.instantads.WrapParser;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.*;
import it.finmatica.jsuitesync.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.dom4j.*;
 
/**
 * Gestione dei Pulsanti.
 * Classe di servizio per la gestione del Client
*/


public class CCS_Bottoniera 
{
	/**
	 * Classe privata.
	 * Gestione della lista di oggetti
	*/ 
	private class ControlliJSINK   
	{  
		/**
		 * Variabili private 
		*/	
	    private Vector ite;
	    private SyncSuite sync;
	    private String msg;
	    
	    /**
		 * Costruttore generico vuoto.
		 */
		public ControlliJSINK(){}
	   
		/**
		 * Costruttore utilizzato per creare la stuttura dati
		 * necessaria per tener traccia delle informazioni di 
		 * ciascun oggetto della lista.
		 *  
	     * @param newite	la lista degli idSyncActivity eseguibili
	     * 					o una lista vuota
	     * @param newsync	oggetto JSYNC per la connessione al database
	     * 
	     * @param newmsg	messaggio di visualizzazione d'operazione eseguita
	     * 					o messagio di segnalazione di errore
		 */
		 public ControlliJSINK(Vector newite,SyncSuite newsync,String newmsg)
	     {
		    this.ite=newite;
		    this.sync=newsync;
		    this.msg=newmsg;
	     }
	  
		 /***************************************************************************
		  * Metodi di GET e SET
		  **************************************************************************/
		 public Vector getIterable() {
		         return ite;
		 } 
		 
		 public void setIterable(Vector i) {
		         ite=i;
		 } 
		  
		 public SyncSuite getSyncSuite() {
		         return sync;
		 } 
		 
		 public void setSyncSuite(SyncSuite s) {
		         sync=s;
		 } 
		  
		 public String getMSGOggetto() {
		         return msg;
		 } 
		 
		 public void setMSGOggetto(String m) {
		         msg=m;
		 } 
    } 	
	
	/**
	 * Classe privata.
	 * Gestione del pulsante
	*/
	private class Bottone   
    {  
		/**
		 * Variabili private 
		*/	
		private String area;
	    private String codice_modello;
	    private String etichetta;
	    private String text;
	    private String controllo_js;
	    private String controllo;
	    private String icona;
	    private String title;
	    private String src;
	    private String icona_ds;
	    private String title_ds;
	    private String src_ds;
	    
	    /**
		 * Costruttore generico vuoto.
		*/
        public Bottone(){ }

        /**
		 * Costruttore utilizzato per creare la stuttura dati
		 * necessaria per tener traccia delle informazioni di 
		 * ciascun pulsante.
		 */
         public Bottone(String newarea,String cm,String newetichetta, String newtext, String newcontrollo_js,
        		 		String newcontrollo,String newicona,String newtitle,String newsrc, String newicona_ds,
        		 		String newtitle_ds, String newsrc_ds)
         {
	       this.area=newarea;
	       this.codice_modello=cm;
	       this.etichetta=newetichetta;         
	       this.text=newtext;  
	       this.controllo_js=newcontrollo_js;
	       this.controllo=newcontrollo;
	       this.icona=newicona;
	       this.title=newtitle;
	       this.src=newsrc;
	       this.icona_ds=newicona_ds;
	       this.title_ds=newtitle_ds;
	       this.src_ds=newsrc_ds;
		 }
         
         /***************************************************************************
		  * Metodi di GET e SET
		  **************************************************************************/
		 public String getArea() {
			 	return area;
		 } 
 
		 public void setArea(String a) {
		        area=a;
		 } 
		  
		 public String getCM() {
		        return codice_modello;
		 } 
		 
		 public void setCM(String c) {
		        codice_modello=c;
		 } 
  
		 public String getEtichetta() {
		        return etichetta;
		 } 
		 
		 public void setEtichetta(String e) {
		        etichetta=e;
		 }  
  
		 public String getText() {
		        return text;
		 } 
		 
		 public void setText(String t) {
		        text=t;
		 } 
		 
		 public String getControllo() {
		        return controllo;
		 } 
		 
		 public void setControllo(String c) {
		        controllo=c;
		 }  
		 
		 public String getControlloJS() {
		        return controllo_js;
		 } 
		 
		 public void setControlloJS(String c) {
		        controllo_js=c;
		 }  
		 
		 public String getIcona() {
		        return icona;
		 } 
		 
		 public void setIcona(String i) {
		        icona=i;
		 }   
		
		 public String getTitle() {
		        return title;
		 } 
		 
		 public void setTitle(String t) {
		        title=t;
		 }  
		 
		 public String getSrc() {
		        return src;
		 } 
 
		 public void setSrc(String s) {
		        src=s;
		 } 
		  
		 public String getIconaDS() {
		        return icona_ds;
		 } 
		 
		 public void setIconaDS(String i) {
		        icona_ds=i;
		 }   
		
		 public String getTitleDS() {
		        return title_ds;
		 } 
		 
		 public void setTitleDS(String t) {
		        title_ds=t;
		 }  
		 
		 public String getSrcDS() {
		        return src_ds;
		 } 
		 
		 public void setSrcDS(String s) {
		        src_ds=s;
		 }   
    } 
   
	/** COSTANTI * */
	private static String _PATHIMG_BOTTONI ="../common/icone/"; 
   
	/**
	 * Variabili private 
	*/	
	private IDbOperationSQL dbOp;  
	private Environment vu;  
    private Vector bottoniera;
    private Vector bottonieraORD;
    private Vector ordineVis;
    private String user;
    CCS_HTML h;
    CCS_Common CCS_common;
    String idOggetto;
    String tipoOggetto;
    private String areaRicerca;
    private String cmRicerca;
    private String tipoRicerca;
    private ElapsedTime elpsTime;   
    private String controllo;
    private String area;
    private String area2;
    private String controllo2;
    private String SBLOCCO_AUTOMATICO;//="N";
    private boolean EFFETTUA_CONTROLLO_FIGLI=false;
    private String ruolo;
    private String modulo;
    private String istanza;
    private String nominativo;
    private String URLserver;
    private String contextPath;
    private String sPath;
    private String inifile; 
    private HttpServletRequest req;
    private String messageRedirect=null;
    private String profiloID="";
    private String profiloArea="";
    private String linkAPP="";
    private String VERIFICA_ALLEGATI="N";
    private boolean compDelega;
    private String jdmsML = "";
    
    /**
     * Costante per identificare la variabile di sessione per GDM multilingua
    */
    private final static String MULTILINGUA ="MULTILINGUA";
    
    
	/**
	  * Variabile gestione logging
	*/
    private DMServer4j log;
   
    /**
	 * Classi utilizzate per la gestione della lista di oggetti.
	*/
    Properties p,p2=null;
    Vector listaOggettiError;
    
    /**
	 * Costruttore utilizzato per gestione dei pulsanti
	 * presenti nella WorkArea del Client Documentale
	 *  
     * @param newidOggetto	idOggetto
     * @param newCommon		variabile di connessione
 	 */
	 public CCS_Bottoniera(String newidOggetto,HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	 {
		    if(newidOggetto!=null){
				idOggetto=newidOggetto.substring(1,newidOggetto.length());
				tipoOggetto=newidOggetto.substring(0,1);
			}
		    req=newreq; 
		    if(req.getParameter("redirect")!=null && req.getParameter("redirect")!="")
		      linkAPP=req.getParameter("redirect");
		    init(newCommon);
		    user=CCS_common.user;
			/** Crea la lista (vuota) dei bottoni per quel tipo di id_tipodoc **/
		 	this.bottoniera = new Vector();
		 	/** Crea la lista (vuota) dei bottoni secondo il criterio di visualizzazione **/
		 	this.bottonieraORD=new Vector();
		 	/** Crea la lista (vuota) dei criteri di visualizzazione dei bottoni **/
		 	this.ordineVis=new Vector();
		    VERIFICA_ALLEGATI=this.retriveParametro("GENERA_IMPRONTA","@STANDARD");
		    getParametriSessione();
		    elpsTime = new ElapsedTime("CCS_Bottoniera",vu);
	 }
	 
	 /**
	 * Costruttore utilizzato per Gestione dei Controlli dei Pulsanti
	 * presenti nella WorkArea del Client Documentale
	 */
	 public CCS_Bottoniera(String newcontrollo,String newarea,String newuser,String newruolo,String newmodulo,
			 			   String newistanza,String newnominativo,String newURLserver,String newcontextPath,
			 			   String newsPath,HttpServletRequest newreq,CCS_Common newCommon) throws Exception
     {
		     controllo=newcontrollo;
		     area=newarea;
		     user=newuser;
		     ruolo=newruolo;
		     modulo=newmodulo;
		     istanza=newistanza;
		     nominativo=newnominativo;
		     URLserver=newURLserver;
		     contextPath=newcontextPath;
		     sPath=newsPath;
		     req=newreq;
		     init(newCommon);
		     initParametri();
		     p=new Properties();
		     p2=new Properties();
		     listaOggettiError=new Vector();
		     getParametriSessione();
		     elpsTime = new ElapsedTime("CCS_Bottoniera",vu);
    }
	 
    /**
	 * Costruttore utilizzato per gestione dei pulsanti
	 * presenti nella WorkArea del Client Documentale 
	 * invocato da un componente FLEX tramite la SOA
	 *  
     * @param newidOggetto	idOggetto
     * @param req			Request
     * @param dbOp			IDbOperationSQL
 	 */
	 public CCS_Bottoniera(String newidOggetto,HttpServletRequest newreq,IDbOperationSQL newdbOp) throws Exception
	 {
			if(newidOggetto!=null){
				idOggetto=newidOggetto.substring(1,newidOggetto.length());
				tipoOggetto=newidOggetto.substring(0,1);
			}
		    req=newreq;
		    if(req!=null)
		     user=""+req.getSession().getAttribute("Utente");
		    dbOp=newdbOp;
		    vu = new Environment(user, null,null,null, null,dbOp.getConn(),false);
		    CCS_common= new CCS_Common(vu,user); 
		    h = new CCS_HTML();
	        log= new DMServer4j(CCS_Bottoniera.class,CCS_common); 
	        /** Crea la lista (vuota) dei bottoni per quel tipo di id_tipodoc **/
	        this.bottoniera = new Vector();
	        /** Crea la lista (vuota) dei bottoni secondo il criterio di visualizzazione **/
	        this.bottonieraORD=new Vector();
	        /** Crea la lista (vuota) dei criteri di visualizzazione dei bottoni **/
	        this.ordineVis=new Vector();
	        getParametriSessione();
	        elpsTime = new ElapsedTime("CCS_Bottoniera",vu);
	 } 
	 
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
 
	 /**
	    * Inizializzazione di alcuni parametri
	    * per la gestione dei pulsanti.
	    * 
	    * @param newCommon    variabile di connessione
	   */	   
	 private void init(CCS_Common newCommon)  throws Exception
	 {
	 	     h = new CCS_HTML();
	         CCS_common=newCommon;
	         log= new DMServer4j(CCS_Bottoniera.class,CCS_common);

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
	   * Costruzione della sequenza di pulsanti.
	   * 
	   * @return Stringa HTML della sequenza di pulsanti
	 */
	 public String getBottoniera() throws Exception
	 {
	        String sbottoniera;
	        String barra=null;
	         
	        elpsTime.start("getBottoniera","");
	         
	        try
	        {
	          /** Recupero di alcune informazioni dell'oggetto */
		      retrieveProfiloDoc(idOggetto,tipoOggetto);
		       
		      /** Verifica competenza di gestione delega */
		      compDelega=verificaCompetenzaDelega();
		      
	          /** Costruzione del vettore di bottoni */
	          buildBottonieraVector();
	          
	          /** Costruzione della sequenza */
	          if( (idOggetto.equals("-1")) || (idOggetto.equals("-2")) )
	            sbottoniera=buildBottonieraViewBS(bottoniera);
	          else  
	          {
	            /** Controllo se esiste già un barra di visualizzazione */
	        	barra=controlloBarra();
	        	
	        	/** Costruzione del vettore di bottoni da visualizzare */
	            if((barra!="") && (barra!=null))
	            {
	              buildBottonieraORD();
	              sbottoniera=buildBottonieraView(bottonieraORD);
	            }
	            else
	              sbottoniera=buildBottonieraViewBS(bottoniera);
	           }
	         }
	         catch (Exception e) {     
		         throw e;
		     }
			 finally {
				_finally();
			 }
	         
	         elpsTime.stop();
	         return sbottoniera;
	 }

	public String getSequenzaPulsanti() throws Exception
	{
		try
		{
			Vector v = new Vector();
			v = getPulsantiXML();

			if(v.size()==0)
				return	"<ROWSET><ROW><TEXT>NESSUN</TEXT></ROW></ROWSET>";
			else
				return new SOAXMLDataRet("1",v,true).getXML();
		}
		catch (Exception e) {
			return (new SOAXMLErrorRet("CostruzionePulsanti: " +e.getMessage())).getXML();
		}
		finally {
			_finally();
		}
	}

	public String getMessaggeRedirect()
	{
		return messageRedirect;
	}

	/**
	 * Gestione dei Controlli.
	 *
	 */
	public String buildContolloXML()  throws Exception
	{
		String redirect="";
		String tipoC="",errMsg="",corpo="",result=null;
		String conDRIVER=null,conCONNESIONE=null,conUTENTE=null,conPASSWD=null,sDsn=null;
		String syncErr,syncErr2;
		String oggetto="";
		IDbOperationSQL dbOpSQL1=null,dbOpSQL2=null,dbOpF=null;

		try
		{
			/** Recupero parametri */
			String idCart=req.getParameter("idCartProveninez");
			if(idCart.indexOf("C")!=-1)
				idCart=idCart.substring(1,idCart.length());
			String idQuery=req.getParameter("idQueryProveninez");
			if(idQuery.equals("-1"))
				oggetto=(new DocUtil(vu)).getIdViewCartellaByIdCartella(idCart);
			else
			{
				oggetto=idQuery;
				idCart=req.getParameter("idCartAppartenenza");
			}
			String[] listaID=null;
			String slista=req.getParameter("listaID").toString();
			if(!slista.equals(""))
				listaID=Global.Split(slista,"@");

			log.log_info("Inizio - Verifica se l'attività JSync è associato all'oggetto area:"+area+" controllo:"+controllo+" oggetto:"+oggetto+"\n");

			/** Verifica se l'attività JSuiteSync è associato all'oggetto padre
			 * (Cartella o Query). Se non esiste allora l'attività è associata agli
			 * oggetti della lista ponendo la variabile EFFETTUA_CONTROLLO_FIGLI=TRUE.
			 */
			SyncSuite sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
			Vector iter = (Vector)sync.isExecutable(area,controllo,oggetto,user);
			Vector ite;
			if (iter==null)
				ite=new Vector();
			else
				ite=iter;
			syncErr = sync.getLastError();
			if (syncErr != null) {
				log.log_error("CCS_Bottoniera::buildContolloXML() -- Esecuzione CONTROLLO: (area,controllo)=("+area+","+controllo+") - UTENTE: "+user+" - OGGETTO: "+oggetto+" - syncErr: "+syncErr);
			}
			if (ite.size()==0)
			{
				EFFETTUA_CONTROLLO_FIGLI=true;
			}

			log.log_info("Fine - Verifica se l'attività JSync è associato all'oggetto.\n");

			log.log_info("Inizio - Recupero informazioni relative al controllo.\n");

			/** Recupero di alcune informazioni relative al Controllo */
			StringBuffer  sql = new StringBuffer("SELECT CORPO, SBLOCCO_AUTOMATICO, TIPO, DRIVER, CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, DSN ");
			sql.append(" FROM LIBRERIA_CONTROLLI WHERE AREA = :AREA AND CONTROLLO = :CONTROLLO");
			try
			{
				dbOpSQL1= SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
				dbOpSQL1.setStatement(sql.toString());
				dbOpSQL1.setParameter(":AREA",area);
				dbOpSQL1.setParameter(":CONTROLLO",controllo);
				dbOpSQL1.execute();
				ResultSet rs = dbOpSQL1.getRstSet();
				if (rs.next())
				{
					tipoC = rs.getString("TIPO");
					errMsg = rs.getString("MSG_ERRORE");
					SBLOCCO_AUTOMATICO=rs.getString("SBLOCCO_AUTOMATICO");
					if (errMsg == null)
						errMsg = "";
					corpo = rs.getString("CORPO");
					conDRIVER = rs.getString("DRIVER");
					conCONNESIONE = rs.getString("CONNESSIONE");
					conUTENTE = rs.getString("UTENTE");
					conPASSWD = rs.getString("PASSWD");
					sDsn = rs.getString("DSN");
				}
				dbOpSQL1.close();
			}
			catch ( SQLException e ) {
				try{closeSync(sync);}catch ( Exception ei ){ }
				dbOpSQL1.close();
				log.log_error("CCS_Bottoniera::buildContolloXML() -- Recupero Parametri Controllo - Area:"+area+" - Controllo:"+controllo+" - SQL:"+sql+"\n");
				throw e;
			}

			log.log_info("Fine - Recupero informazioni relative al controllo.\n");


			log.log_info("Inizio - Costruzione tracciato XML di INPUT\n");

			/** Creazione del tracciato XML di INPUT */
			String sFunInput = tracciatoXMLInput(listaID,idCart,idQuery,errMsg);
			sFunInput=sFunInput.substring(sFunInput.indexOf("<FUNCTION_INPUT>"),sFunInput.length());

			log.log_info("Fine - Costruzione tracciato XML di INPUT sFunInput:+"+sFunInput+"+\n");

			/** Esecuzione del controllo di tipo JAVA */
			if (tipoC.equalsIgnoreCase("J"))
			{
				log.log_info("Inizio - Binding dei parametri ed esecuzione controllo di tipo JAVA - corpo: "+corpo+"\n");

				BottonieraParser p = new BottonieraParser(sFunInput,req);
				String javaStm = p.bindingDeiParametri(corpo);
				try{
					WrapParser wp = new WrapParser(javaStm);
					result = wp.goExtended(req,dbOp);
				}
				catch (Exception ijEx){
					ijEx.printStackTrace();
					try {closeSync(sync);}catch ( Exception ei ){}
					log.log_error("CCS_Bottoniera::buildContolloXML() -- Esecuzione Controllo di tipo Java - XML_Input:"+sFunInput+" - Stm:"+javaStm+"\n");
					throw ijEx;
				}

				log.log_info("Fine - Binding dei parametri ed esecuzione controllo di tipo JAVA.\n");
			}

			/** Esecuzione del controllo di tipo PL/SQL */
			if (tipoC.equalsIgnoreCase("P"))
			{
				log.log_info("Inizio - Esecuzione del controllo di tipo PL/SQL \n");

				String funcSql=null;
				try
				{
					log.log_info("Inizio - Recupero dati per la connessione DSN: "+sDsn+" DRIVER: "+conDRIVER+" \n");

					if (sDsn == null) {
						sDsn = "";
					}
					if (!sDsn.equalsIgnoreCase(""))
					{
						dbOpSQL2 = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
						try {
							Connessione cn = new Connessione(dbOpSQL2,sDsn);
							conDRIVER      = cn.getDriver();
							conCONNESIONE  = cn.getConnessione();
							conUTENTE      = cn.getUtente();
							conPASSWD      = cn.getPassword();
							dbOpSQL2.close();
						}
						catch(Exception eDbOp2) {
							dbOpSQL2.close();
							log.log_error("CCS_Bottoniera::buildContolloXML() -- Esecuzione del controllo di tipo PL/SQL\n");
							throw eDbOp2;
						}
					}
					if (conDRIVER == null) {
						conDRIVER = "";
					}
					if (!conDRIVER.equalsIgnoreCase(""))
					{
						String compConn = completaConnessione(conCONNESIONE);
						ConnessioneParser cp = new ConnessioneParser();
						String connessione = cp.bindingDeiParametri(compConn);
						if (connessione == null){
							connessione = conCONNESIONE;
						}
						dbOpF = SessioneDb.getInstance().createIDbOperationSQL(conDRIVER,connessione,conUTENTE,conPASSWD);
					}
					else
					{
						dbOpF = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
					}

					log.log_info("Fine - Recupero dati per la connessione.\n");

					log.log_info("Inizio - Binding dei parametri ed invocazione della funzione.\n");

					/** Operazione di binding dei parametri */
					BottonieraParser p = new BottonieraParser(sFunInput);
					funcSql = p.bindingDeiParametri(corpo);
					dbOpF.setCallFunc(funcSql);
					dbOpF.execute();
					result = dbOpF.getCallSql().getString(1);
					if (result == null) {
						result = "";
					}
					dbOpF.close();

					log.log_info("Fine - Binding dei parametri ed invocazione della funzione CORPO: "+corpo+" FUNZIONE PL/SQL: "+funcSql+" \n");

				}
				catch ( SQLException e ) {
					try {closeSync(sync);}catch ( Exception ei ){}
					dbOpF.close();
					log.log_error("CCS_Bottoniera::buildContolloXML() -- Esecuzione Controllo di tipo Procedure - XML_Input:"+sFunInput+" - function_SQL:"+funcSql+"\n");
					throw e;
				}

				log.log_info("Fine - Esecuzione del controllo di tipo PL/SQL \n");
			}

			log.log_info("Inizio - Interpetrazione del tracciato XML di OUTPUT:"+result+" \n");

			/** Interpretazione del tracciato XML di OUTPUT **/
			if(result!=null)
			{
				Document dOuput = null;

				try
				{
					log.log_info("Inizio - ParseText di result:"+result+" \n");
					dOuput = DocumentHelper.parseText(result);
					log.log_info("Fine - ParseText di result:"+result+" \n");

					log.log_info("Inizio - Interpetrazione tag RESULT \n");

					String esito = leggiValoreXML(dOuput,"RESULT");
					/** Nel caso in cui RESULT è non ok viene effettuata la redirect
					 * nella MessagePage con il relativo errore generato */
					if (!esito.equalsIgnoreCase("ok"))
					{
						String errore = leggiValoreXML(dOuput,"ERROR");
						redirect= h.MsgBoxPagePop("CONTROLLI_ERROR",errore);

						try {
							CCS_common.closeConnection(dbOp);
						}
						catch(Exception eIntern) {
							closeSync(sync);
							throw new Exception(eIntern);
						}
						closeSync(sync);
						return redirect;
					}

					log.log_info("Fine - Interpetrazione tag RESULT \n");

					log.log_info("Inizio - Interpetrazione tag JSYNC \n");

					/** Caso in cui non è associato nessun tipo di attività JSink al controllo */
					String tagJsink = leggiValoreXML(dOuput,"JSYNC");
					if( (ite.size()==0) && (tagJsink==null) )
					{
						log.log_info("Inizio - Nessuna attivita associata al controllo \n");

						/** Caso FORCE_REDIRECT */
						String force_redirect = leggiValoreXML(dOuput,"FORCE_REDIRECT");

						if (force_redirect!=null && force_redirect.equalsIgnoreCase("Y"))
						{
							redirect =leggiValoreXML(dOuput, "REDIRECT");
							if( (redirect!=null) && (!redirect.equalsIgnoreCase("")) ) {
								try{CCS_common.closeConnection(dbOp);}catch ( Exception ei ){ }
								closeSync(sync);

								String refresh = leggiValoreXML(dOuput,"REFRESH");

								if(refresh!=null && refresh.equalsIgnoreCase("N"))
									return redirect;
								else
									return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery+"&winModale=Y&URLpage="+URLEncoder.encode(redirect);
							}
							else {
								try{CCS_common.closeConnection(dbOp);}catch ( Exception ei ){ }
								closeSync(sync);
								return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
							}
						}
						else
						{
							/** Costruzione del messaggio */
							Element elenco = leggiElementoXML(dOuput,"LISTAID");
							String msg=buildMSGfromListaID(elenco);

							/** Recupero del nodo REDIRECT */
							String url_redirect =leggiValoreXML(dOuput, "REDIRECT");

							/** Se non viene visualizzato nella lista di ID e di messaggi da elencare*/
							if(msg.equals(""))
							{
								if( (url_redirect!=null) && (!url_redirect.equalsIgnoreCase("")) )
									return url_redirect;
								else
									return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
							}

							/** Settaggio del messaggio di ritorno per la pagina da indirizzare */
							setMessaggeRedirect(msg);

							/** Effettua la redirect nella MessagePage visualizzando
							 * l'elenco delle operazioni eseguite/non eseguite
							 */
							if( (url_redirect!=null) && (!url_redirect.equalsIgnoreCase("")) )
								redirect=h.MessagePagePop("",url_redirect);
							else
								redirect=h.MessagePagePop("");

						}

						log.log_info("Fine - Nessuna attivita associata al controllo \n");
					}
					else
					{
						log.log_info("Inizio - Attivita associata al controllo SBLOCCO_AUTOMATICO:"+SBLOCCO_AUTOMATICO+"\n");

						/** Se SBLOCCO_AUTOMATICO=S occorre gestire il Controllo iniziale
						 * altrimenti si passa alla gestione di un secondo Controllo */
						if(SBLOCCO_AUTOMATICO.equals("S"))
						{
							log.log_info("Inizio - Verifica d'esecuzione del Controllo per ogni oggetto della lista.\n");

							/** Verifica d'esecuzione del Controllo per ogni oggetto della lista
							 *  restituendo la lista ristretta agli oggetti autorizzati.
							 */
							String[] checked_listaID=null;
							Element list = leggiElementoXML(dOuput,"LISTAID");
							if(list!=null)
								checked_listaID=verificaControllo(list);

							/** Costruzione dell'elenco di oggetti in caso di errore dell'operazione
							 * e conferma di avvenuta esecuzione dell'operazione degli oggetti */
							if(checked_listaID!=null)
							{
								Element elenco = leggiElementoXML(dOuput,"LISTAID");
								gestioneElencoOperazioni(elenco,p);
							}

							log.log_info("Fine - Verifica d'esecuzione del Controllo per ogni oggetto della lista.\n");


							if(!EFFETTUA_CONTROLLO_FIGLI)
							{
								log.log_info("Inizio - Esecuzione del Controllo sull'oggetto padre.\n");

								/** Conferma di avvenuta esecuzione del Controllo sull'oggetto Padre Cartella o Query
								 * Per ogni elemento (id) della lista lancio la SYNC.executed(id) e controllo che non
								 * vi siano errori.*/
								try {
									boolean okrun = true;
									Iterator i = ite.iterator();
									while (i.hasNext())
									{
										Integer id=(Integer)(i.next());
										okrun = okrun && (sync.executed(id.intValue()) == SyncSuite.SYNC_ESEGUITO);
									}

									if (okrun)
										sync.commit();
									else
										sync.rollback();
								}
								catch (Exception ei) {
									closeSync(sync);
									log.log_error("CCS_Bottoniera::buildContolloXML() -- Errore conferma di avvenuta esecuzione del Controllo\n");
									throw ei;
								}
								closeSync(sync);

								log.log_info("Fine - Esecuzione del Controllo sull'oggetto padre.\n");

							}

							log.log_info("Inizio - Costruzione del re-indirizzamento.\n");

							/** Caso FORCE_REDIRECT */
							String force_redirect = leggiValoreXML(dOuput,"FORCE_REDIRECT");

							if (force_redirect.equalsIgnoreCase("Y"))
							{
								redirect =leggiValoreXML(dOuput, "REDIRECT");
								if( (redirect!=null) && (!redirect.equalsIgnoreCase("")) ) {
									try{CCS_common.closeConnection(dbOp);}catch ( Exception ei ){ }
									closeSync(sync);
									String refresh = leggiValoreXML(dOuput,"REFRESH");

									if(refresh!=null && refresh.equalsIgnoreCase("N"))
										return redirect;
									else
										return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery+"&winModale=Y&URLpage="+URLEncoder.encode(redirect);
								}
								else {
									try{CCS_common.closeConnection(dbOp);}catch ( Exception ei ){ }
									closeSync(sync);
									return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
								}
							}
							else
							{
								/** Costruzione del messaggio */
								String msg="";
								if(listaOggettiError==null)
								{
									String msgObj="";
									Enumeration keys = p.keys();
									while (keys.hasMoreElements())
									{
										Object key = keys.nextElement();
										ControlliJSINK c=(ControlliJSINK)p.get(key);
										if(!c.getMSGOggetto().equals(""))
											msgObj+=" - "+c.getMSGOggetto()+"<br>";
									}

									if(!msgObj.equals(""))
									{
										msg+="Avviso:<br>";
										msg+="Le operazioni sono state tutte eseguite correttamente!<br>";
										msg+= msgObj;
									}
									else
									{
										//Caso particolare in cui falliscono le attivita jsink associati agli oggetti
										//occorre visualizzare un messaggio più corretto.
										msg="Avviso:<br>Problemi durante l'esecuzione dell'operazione.";
									}

								}
								else
								{
									String msgElencoError="";
									String msgElenco="";
									Enumeration keys = p.keys();
									while (keys.hasMoreElements())
									{
										Object key = keys.nextElement();
										ControlliJSINK c=(ControlliJSINK)p.get(key);
										if(ricercaOggetto(listaOggettiError,key.toString()))
											msgElencoError+=" - "+c.getMSGOggetto()+"<br>";
										else
											msgElenco+=" - "+c.getMSGOggetto()+"<br>";
									}
									msg+="Avviso:<br>";
									if(!msgElenco.equals(""))
									{
										msg+="<br>Elenco delle operazioni eseguite correttamente:<br>";
										msg+=msgElenco;
									}
									if(!msgElencoError.equals(""))
									{
										msg+="<br>Elenco delle operazioni non eseguite correttamente:<br>";
										msg+=msgElencoError;
									}

									//Caso particolare in cui falliscono le attivita jsink associati agli oggetti
									//occorre visualizzare un messaggio più corretto.
									if(msgElenco.equals("") && (msgElencoError.equals("")))
										msg+="Problemi durante l'esecuzione dell'operazione.";

								}

								//Settaggio del messaggio di ritorno per la pagina da indirizzare
								setMessaggeRedirect(msg);


								/** Effettua la redirect nella MessagePage visualizzando
								 * l'elenco delle operazioni eseguite/non eseguite
								 */
								String url_redirect =leggiValoreXML(dOuput, "REDIRECT");
								if( (url_redirect!=null) && (!url_redirect.equalsIgnoreCase("")) )
									redirect=h.MessagePagePop("",url_redirect);
								else
									redirect=h.MessagePagePop("");

								try{closeSync(sync);}catch ( Exception ei ){ }

							}

							log.log_info("Fine - Costruzione del re-indirizzamento.\n");
						}
						else
						{
							log.log_info("Inizio - Esecuzione del secondo controllo.\n");

							/** Nel caso di gestione sul secondo controllo */
							Element jsink = leggiElementoXML(dOuput,"JSYNC");
							if(jsink!=null)
							{
								area2 = leggiValoreXML(dOuput,"AREA");
								controllo2 = leggiValoreXML(dOuput,"CONTROLLO");
							}
							else
							{
								area2=area;
								controllo2=controllo;
							}

							log.log_info("Inizio - Verifica se esiste un'attività JSYNC associata al secondo controllo area2:"+area2+" controllo2:"+controllo2+"\n");

							/** Verifica se l'attività JSuiteSync è associato al secondo controllo. */
							SyncSuite sync2 = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
							Vector iter2 = (Vector)sync2.isExecutable(area2,controllo2,oggetto,user);
							syncErr2 = sync2.getLastError();
							if (syncErr2 != null) {
								log.log_error("CCS_Bottoniera::buildContolloXML() -- Esecuzione CONTROLLO: (area,controllo)=("+area2+","+controllo2+") - UTENTE: "+user+" - OGGETTO: "+oggetto+" - syncErr: "+syncErr2);
							}
							Vector ite2;
							if (iter2==null)
								ite2=new Vector();
							else
								ite2=iter2;
							if (ite2.size()==0)
							{
								EFFETTUA_CONTROLLO_FIGLI=true;
							}

							log.log_info("Fine - Verifica se esiste un'attività JSYNC associata al secondo controllo area2:"+area2+" controllo2:"+controllo2+"\n");

							log.log_info("Inizio - Verifica d'esecuzione del Controllo per ogni oggetto della lista.\n");

							/** Verifica d'esecuzione del Controllo per ogni oggetto della lista
							 *  restituendo la lista ristretta agli oggetti autorizzati.
							 */
							String[] checked_listaID2=null;
							Element list = leggiElementoXML(dOuput,"LISTAID");
							if(list!=null)
								checked_listaID2=verificaControllo(list);

							log.log_info("Fine - Verifica d'esecuzione del Controllo per ogni oggetto della lista.\n");



							/** Costruzione dell'elenco di oggetti in caso di errore dell'operazione
							 * e conferma di avvenuta esecuzione dell'operazione degli oggetti */
							if(checked_listaID2!=null)
							{
								log.log_info("Inizio - Verifica ed esecuzione dell'attività a gli oggetti: "+checked_listaID2+"\n");
								Element elenco = leggiElementoXML(dOuput,"LISTAID");
								gestioneElencoOperazioni(elenco,p2);
								log.log_info("Fine - Verifica ed esecuzione dell'attività a gli oggetti figli.\n");
							}



							if(!EFFETTUA_CONTROLLO_FIGLI)
							{
								log.log_info("Inizio - Esecuzione attività all'oggetto padre EFFETTUA_CONTROLLO_FIGLI:"+EFFETTUA_CONTROLLO_FIGLI+"\n");

								/** Conferma di avvenuta esecuzione del Controllo sull'oggetto Padre Cartella o Query
								 * Per ogni elemento (id) della lista lancio la SYNC.executed(id) e controllo che non
								 * vi siano errori.*/
								try
								{
									boolean okrun = true;
									Iterator i = ite2.iterator();
									while (i.hasNext())
									{
										Integer id=(Integer)(i.next());
										okrun = okrun && (sync2.executed(id.intValue()) == SyncSuite.SYNC_ESEGUITO);
									}
									if (okrun)
										sync2.commit();
									else
										sync2.rollback();
								}
								catch (Exception ei) {
									try{closeSync(sync2);}catch ( Exception eii ){ }
									closeSync(sync);
									log.log_error("CCS_Bottoniera::buildContolloXML() -- Errore di conferma di avvenuta esecuzione sul secondo Controllo -Area:"+area2+" - Controllo:"+controllo2+" - Oggetto:"+oggetto+"\n");
									throw ei;
								}

								log.log_info("Fine - Esecuzione attività all'oggetto padre.\n");

							}

							log.log_info("Inizio - Costruzione del re-indirizzamento.\n");

							/** Caso FORCE_REDIRECT */
							String force_redirect = leggiValoreXML(dOuput,"FORCE_REDIRECT");
							if (force_redirect.equalsIgnoreCase("Y"))
							{
								redirect =leggiValoreXML(dOuput, "REDIRECT");
								if( (redirect!=null) && (!redirect.equalsIgnoreCase("")) ) {
									try{CCS_common.closeConnection(dbOp);}catch ( Exception ei ){ }
									try{closeSync(sync2);}catch ( Exception eii ){ }
									closeSync(sync);
									String refresh = leggiValoreXML(dOuput,"REFRESH");

									if(refresh!=null && refresh.equalsIgnoreCase("N"))
										return redirect;
									else
										return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery+"&winModale=Y&URLpage="+URLEncoder.encode(redirect);
								}
								else {
									try{CCS_common.closeConnection(dbOp);}catch ( Exception ei ){ }
									try{closeSync(sync2);}catch ( Exception eii ){ }
									closeSync(sync);
									return "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
								}
							}
							else
							{
								/** Costruzione del messaggio */
								String msg="";
								if(listaOggettiError==null)
								{
									String msgObj="";
									Enumeration keys = p2.keys();
									while (keys.hasMoreElements())
									{
										Object key = keys.nextElement();
										ControlliJSINK c=(ControlliJSINK)p2.get(key);
										if(!c.getMSGOggetto().equals(""))
											msgObj+=" - "+c.getMSGOggetto()+"<br>";
									}

									if(!msgObj.equals(""))
									{
										msg+="Avviso:<br>";
										msg+="Le operazioni sono state tutte eseguite correttamente!<br>";
										msg+=msgObj;
									}
									else
									{
										//Caso particolare in cui falliscono le attivita jsink associati agli oggetti
										//occorre visualizzare un messaggio più corretto.
										msg="Avviso:<br>Problemi durante l'esecuzione dell'operazione.";
									}
								}
								else
								{
									String msgElencoError="";
									String msgElenco="";
									Enumeration keys = p2.keys();
									while (keys.hasMoreElements())
									{
										Object key = keys.nextElement();
										ControlliJSINK c=(ControlliJSINK)p2.get(key);
										if(ricercaOggetto(listaOggettiError,key.toString()))
											msgElencoError+=" - "+c.getMSGOggetto()+"<br>";
										else
											msgElenco+=" - "+c.getMSGOggetto()+"<br>";
									}

									msg+="Avviso:<br>";

									if(!msgElenco.equals(""))
									{
										msg+="<br>Elenco delle operazioni eseguite correttamente:<br>";
										msg+=msgElenco;
									}

									if(!msgElencoError.equals(""))
									{
										msg+="<br>Elenco delle operazioni non eseguite correttamente:<br>";
										msg+=msgElencoError;
									}

									//Caso particolare in cui falliscono le attivita jsink associati agli oggetti
									//occorre visualizzare un messaggio più corretto.
									if(msgElenco.equals("") && (msgElencoError.equals("")))
										msg+="Problemi durante l'esecuzione dell'operazione.";
								}

								//Settaggio del messaggio di ritorno per la pagina da indirizzare
								setMessaggeRedirect(msg);

								/** Effettua la redirect nella MessagePage visualizzando
								 * l'elenco delle operazioni eseguite/non eseguite
								 */
								String url_redirect =leggiValoreXML(dOuput, "REDIRECT");
								if( (url_redirect!=null) && (!url_redirect.equalsIgnoreCase("")) )
									redirect=h.MessagePagePop("",url_redirect);
								else
									redirect=h.MessagePagePop("");

								try{closeSync(sync2);}catch ( Exception ei ){ }
								try{closeSync(sync);}catch ( Exception ei ){ }

							}

							log.log_info("Fine - Costruzione del re-indirizzamento.\n");

						}/** end else Secondo Controllo */

						log.log_info("Fine - Esecuzione del secondo controllo.\n");
					}

					log.log_info("Fine - Interpetrazione attività JSYNC.\n");
				}
				catch ( Exception e ) {
					closeSync(sync);
					log.log_error("CCS_Bottoniera::buildContolloXML() -- Controllo XML Output - XML_Output:"+result+" \n");
					throw e;
				}
			}
			log.log_info("Fine - Interpetrazione del tracciato XML di OUTPUT.\n");
		}
		catch (Exception e) {
			try{ CCS_common.closeConnection(dbOp);}catch (Exception ei) {}
			log.log_error("CCS_Bottoniera::buildContolloXML() -Area:"+area+" - Controllo:"+controllo+" - Oggetto:"+oggetto+" - Sblocco_Automatico:"+SBLOCCO_AUTOMATICO);
			throw e;
		}

		CCS_common.closeConnection(dbOp);
		return redirect;
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
	   * Verifica competenza di gestione delega
	   * Se esiste almeno una competenza sui tipi oggetti
	   * restituisce true per rendere visibile il pulsante 
	   * "DELEGA" altrimenti non viene visualizzato.
	   * @return boolean 	competenza_delega
	   * 
	 */
	 private boolean verificaCompetenzaDelega() throws Exception
	 {
			 StringBuffer sStm = new StringBuffer();
			 
			 try
			 {
				sStm.append("GDM_COMPETENZA.GDM_VERIFICA_GESTIONE_DELEGA(");
				sStm.append("'GESTIONE_DELEGA'");
				sStm.append(",'gd'"); 
				sStm.append(",'"+this.user+"'");
				sStm.append(",TO_CHAR(SYSDATE,'dd/mm/yyyy') )");
				dbOp.setCallFunc(sStm.toString());
				dbOp.execute();
				
				String ret = dbOp.getCallSql().getString(1);
				String[] seq = ret.split("@");
				for(int i=0;i<seq.length;i++) {
					
					String ele = seq[i].toString();
					if(!ele.equals("") && ele.substring(1,ele.length()).equals("1")){
					  return true;
					} 	
				}
			 }
			 catch (Exception e) 
			 {
			   log.log_error("verificaCompetenzaDelega() - Errore verifica competenza delega - SQL: "+sStm.toString()+" - Errore: "+e.getMessage());
			   throw e;
			 }
			 return false;  
	 } 

	  /**
	   * Recupero parametro dalla tabella PARAMETRI
	   * 
	   * @param 			nome parametro
	   * @param 			nome tipo_modello
	   * @return String 	valore
	   * 
		 */
	 private String retriveParametro(String parametro,String tipomodello) throws Exception
	 {
	         String sql="",rstPar=null;
	         log.log_info("Inizio Recupero Parametro: "+parametro+"  - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	         try
	         {
	            ResultSet rs=null;
	            sql=" SELECT VALORE FROM PARAMETRI ";
	            sql+=" WHERE CODICE = :PARAMETRO ";
	            sql+=" AND TIPO_MODELLO= :TIPOMODELLO";
	            log.log_info("Recupero Parametro: - SQL: "+sql);
	            dbOp.setStatement(sql);
	            dbOp.setParameter(":PARAMETRO",parametro);
	            dbOp.setParameter(":TIPOMODELLO",tipomodello);
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

	 private String addButton(String etichetta,String index)  throws Exception
	 {
		     String bottone=""; 
	         String pathIMG=_PATHIMG_BOTTONI;
	         String pathIMGDS=_PATHIMG_BOTTONI;
	         String corpoJS="";
	         String corpo="";
	         String text="",icona="",icona_ds="";
	         	         
	         for(int i=0;i<bottoniera.size();i++)
	         {
	        	 Bottone b = (Bottone) bottoniera.get(i); 
	             if(b.etichetta.equals(etichetta))
	             {
	            	 text="";
		             icona="";
		             icona_ds="";
		             /** Controllo della funzione JS associata */ 
		             if(b.getControlloJS()==null)
		               corpoJS="return true;";
		             else
		             {
		            	String sControlloJS=retrieveControllo(b.getArea(),b.getControlloJS());
		                corpoJS=sControlloJS.substring(sControlloJS.indexOf("@")+1,sControlloJS.length());
		             }
		             
		             /** Controllo del controllo Java o PL/SQL o JavaScript associato */ 
		             if(b.getControllo()!=null)
		             { 
		               String sControllo=retrieveControllo(b.getArea(),b.getControllo());
		               String tipoControllo=sControllo.substring(0,sControllo.indexOf("@"));
		            	if(tipoControllo.equals("S"))
		            	   corpo=sControllo.substring(sControllo.indexOf("@")+1,sControllo.length());
		            	else{
		            		String qs = URLEncoder.encode(req.getQueryString());
		            		corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea()+"&qs="+qs);
		              	    //corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea());
		     		    }	
		              }
		             
		             /** Controllo testo del bottone */
		             if(b.text!=null)
		              text=b.text;
		             
		             /** Controllo icona associata */
		             if((b.getIcona()!=null))
		               icona=pathIMG+b.getIcona()+"/"+b.getSrc();
		             
		             /** Controllo icona disabilitata associata */
		             if(b.getIconaDS()!=null)
		               icona_ds=pathIMGDS+b.getIconaDS()+"/"+b.getSrcDS();
		             else
		               icona_ds=icona;
		             
		             if((b.getIcona()!=null))
			           controlloIcona(b.getIcona());
			         if(b.getIconaDS()!=null)
			           controlloIcona(b.getIconaDS());
		             
		             /** Inserimento del pulsante */ 
		             bottone+=this.getBottone(index,b.getTitle(),corpo,icona,icona_ds,text,corpoJS);
		             break;
	             }
	         }
	         
	         return bottone;
	 }
	 	 
	 /**
	   * Costruzione della sequenza di pulsanti in XML per 
	   * l'invocazione da un componente FLEX.
	   * 
	   * @return Stringa HTML della sequenza di pulsanti
	 */
	 private Vector getPulsantiXML() throws Exception
	 {
		     Vector vPulsanti=new Vector();;
	         String barra=null;
	         
	        elpsTime.start("getBottonieraXML","");
	         
	        try
	        {
	          /** Verifica competenza di gestione delega */
			  compDelega=verificaCompetenzaDelega();	
	        	
	          /** Costruzione del vettore di bottoni */
	          buildBottonieraVector();
	          
	          /** Costruzione della sequenza */
	          if( (idOggetto.equals("-1")) || (idOggetto.equals("-2")) )
	        	  vPulsanti=buildPulsantiStandard(bottoniera);
	          else  
	          {
	            /** Controllo se esiste già un barra di visualizzazione */
	        	barra=controlloBarra();
	        	
	        	/** Costruzione del vettore di bottoni da visualizzare */
	            if((barra!="") && (barra!=null))
	            {
	              buildBottonieraORD();
	              vPulsanti=buildPulsanti(bottonieraORD);
	            }
	            else
	            	vPulsanti=buildPulsantiStandard(bottoniera);
	          }          

	         }
	         catch (Exception e) {     
		         throw e;
	         }
	         
	         elpsTime.stop();
	         return vPulsanti;
	 }  
	 
	    
	 /**
	   * Recupero della barra standard di visualizzazione
	   * 
	   * @return Stringa barra di visualizzazione
	 */
	 private String getBarraStandardSQL() throws Exception
	 { 
	         String barra="";
	         String sql="select ISTRUZIONE from domini where area='GDMSYS' and dominio='BOTTONI_STANDARD'";
		     
	         try
	         {
	             dbOp.setStatement(sql);
	             dbOp.execute();
	             ResultSet rs = dbOp.getRstSet();
	             while(rs.next())
	             {
	               //Lettura del corpo del blocco
	               try {
	                 barra=Global.leggiClob(dbOp,"ISTRUZIONE");
	               }
	               catch (Exception e) {                                
	            	   throw new Exception("CCS_Bottoniera::getBarraStandardSQL() - Errore in lettura Blocco - SQL: "+sql);
	               }
	             }
	          }
	          catch ( SQLException e ) {
	        	  throw e;
	          } 
	         
	          return barra;
	 }  

	 
	 /**
	   * Controllo il tipo di query se si tratta di Query di Ricerca Modulistica o meno 
	   *  
	   * @return Stringa tipo
	 */
	 private String controlloTipoQuery() throws Exception
	 { 
	         StringBuffer sql=new StringBuffer();
	         String tipo="";
	          
	         try 
	         {
	            sql.append("SELECT decode(instr(q.filtro,'RICERCAMODULISTICA_'),0,'',substr(q.filtro,length('RICERCAMODULISTICA_')+1,length(q.filtro))) RICERCAMODULISTICA ");
	            sql.append(" FROM QUERY q WHERE q.id_query = :IDOGGETTO ");
	            dbOp.setStatement(sql.toString());
	            dbOp.setParameter(":IDOGGETTO",idOggetto);
	            dbOp.execute();
	            ResultSet rs = dbOp.getRstSet();
					     
	            if(rs.next())
	            {
	             if(rs.getString("RICERCAMODULISTICA")==null)
			        tipo="";
			     else
			    	tipo=rs.getString("RICERCAMODULISTICA");  
	            }
	            
	            if((tipo!=null) && (!tipo.equals("")))
	        	{ 	  
	        	  areaRicerca=tipo.substring(0,tipo.indexOf("@"));
	        	  cmRicerca=tipo.substring(tipo.indexOf("@")+1,tipo.length());
	        	}
	           
	         }
	         catch ( SQLException e ) {
	             log.log_error("CCS_Bottoniera::controlloTipoQuery() -- Controllo il tipo di query se è di tipo Ricerca Modulistica o meno - SQL: "+sql); 
	        	 throw e;
	         } 
	         return tipo;
	 } 
	 
 
	 /**
	   * Select per la barra di visualizzazione
	   *  
	   * @return Stringa select
	 */
	 private String controlloBarraSQL() throws Exception
	 { 
	         StringBuffer sql=new StringBuffer();
	       
	         if(tipoOggetto.equals("C"))
	         {
		         sql.append(" select dominio ");
		         sql.append(" from cartelle c, documenti d, modelli m, domini dm ");
		         sql.append(" where c.id_cartella="+idOggetto);
		         sql.append(" and c.id_documento_profilo=d.id_documento");
		         sql.append(" and d.id_tipodoc=m.id_tipodoc");
		         sql.append(" and m.area=dm.area");
		         sql.append(" and m.codice_modello = dm.codice_modello");
		         sql.append(" and dm.precarica='C'");
	         }
	         else
	         {
	             if (tipoRicerca.equals(""))
	             {
	            	 sql.append(" select dominio ");
		             sql.append(" from query q, documenti d, modelli m, domini dm ");
		             sql.append(" where q.id_query="+idOggetto);
		             sql.append(" and q.id_documento_profilo=d.id_documento");
		             sql.append(" and d.id_tipodoc=m.id_tipodoc");
		             sql.append(" and m.area=dm.area");
		             sql.append(" and m.codice_modello = dm.codice_modello");
		             sql.append(" and dm.precarica='C'"); 
	             }
	             else
	             {
	            	 sql.append(" select dominio ");
		             sql.append(" from domini dm ");
		             sql.append(" where dm.area = '"+areaRicerca+"'");
		             sql.append(" and dm.codice_modello = '"+cmRicerca+"'");
		             sql.append(" and dm.precarica='C'");
	             }
	        	
	         }
	        return sql.toString();
	 }

	 /**
	   * Controllo dell'esistenza di una barra di visualizzazione dei pulsanti
	   *  
	   * @return Stringa dominio
	 */
	 private String controlloBarra() throws Exception
	 { 
	         String sql=controlloBarraSQL();
	         String barra="";
	          
	         try 
	         {
	        	if(tipoOggetto.equals("C"))
	        		dbOp.setParameter(":idOggetto",idOggetto);
	   	        else
	   	        {
	   	        	if (tipoRicerca.equals(""))
	   	            	dbOp.setParameter(":idOggetto",idOggetto);
	   	            else
	   	            {
	   	            	dbOp.setParameter(":cmRicerca",cmRicerca);
		            	dbOp.setParameter(":areaRicerca",areaRicerca);
	   	            }	   	        	
	   	        } 	        	 
	        	 
	            dbOp.setStatement(sql);
	            dbOp.execute();
	            ResultSet rs = dbOp.getRstSet();
					     
	            if(rs.next())
	            {
	               barra=rs.getString("dominio");
	            }
	         }
	         catch ( SQLException e ) {
	             log.log_error("CCS_Bottoniera::controlloBarra() -- Controllo esistenza barra dei pulsnati - SQL: "+sql); 
	        	 throw e;
	         } 
	         return barra;
	 } 
	 
	 /**
	   * Select per la sequenza di pulsanti
	   *  
	   * @return Stringa select
	 */
	 private String bottonieraSQL() throws Exception
	 { 
		     StringBuffer sql=new StringBuffer();
	        
	         /** Nel caso in cui mi trovo nella Worspace Sistema o Utente
	          *  viene definita la bottoniera standard.
	          */
		     if(idOggetto.equals("-1") || (idOggetto.equals("-2")))
	         {
	           
	           if(jdmsML!=null && !jdmsML.equals("")) 
	        	 sql.append("select e.codice_modello, e.area, e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, ");
	           else
	        	 sql.append("select e.codice_modello, e.area, e.etichetta, e.valore text, ");
	           sql.append("e.CONTROLLO_JS, e.CONTROLLO, e.icona, i1.tooltip title, i1.nome src, ");
	           sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
	           sql.append("decode(e.etichetta,'CREADOC',2,'CREACART',3,'CREAQUERY',4,'COPIA',5,'SPOSTA',6,'ELIMINA',7,'COLLEGA',8,'INCOLLA',9,'RIMUOVI',10,");
	           sql.append("'FIRMA_STANDARD',11,'DELEGA',12,'COLL_DESKTOP',13,'COPIA_CONFORME',14,'VERIFICA_ALLEGATI',15,'LINK_DOC',16,'FIRMA',17,'LINKESTERNO',18) ordine ");
	           sql.append("from etichette e, icone i1, icone i2 ");
	           sql.append("where e.codice_modello='-' ");
	 	       sql.append("and e.area='GDMSYS' ");
		       sql.append("and e.icona=i1.icona ");
	           sql.append("and e.icona_ds=i2.icona ");
	           sql.append("order by ordine");
		     }
	         else
	         {
		        if(tipoOggetto.equals("C"))
		        {
		            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
		            if(jdmsML!=null && !jdmsML.equals("")) 
			        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
			        else
			        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
		            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
		            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
		            sql.append("0 ordine ");
		            sql.append("from cartelle c, documenti d, modelli m, etichette e, icone i1, icone i2 ");
		            sql.append("where c.id_cartella=:idOggetto");
		            sql.append(" and c.id_documento_profilo=d.id_documento");
		            sql.append(" and d.id_tipodoc=m.id_tipodoc");
		            sql.append(" and d.area=m.area");
		            sql.append(" and m.codice_modello = e.codice_modello");
		            sql.append(" and m.area = e.area");
		            sql.append(" and e.icona=i1.icona (+)");
		            sql.append(" and e.icona_ds=i2.icona (+)");
		            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
		            sql.append(" union all ");
		            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
		            if(jdmsML!=null && !jdmsML.equals("")) 
			        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
			        else
			        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
		            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
		            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
		            sql.append("1 ordine ");
		            sql.append("from cartelle c, documenti d, modelli m, etichette e, icone i1, icone i2 ");
		            sql.append("where c.id_cartella=:idOggetto");
		            sql.append(" and c.id_documento_profilo=d.id_documento");
		            sql.append(" and d.id_tipodoc=m.id_tipodoc");
		            sql.append(" and d.area=m.area");
		            sql.append(" and e.codice_modello = '-'");
		            sql.append(" and m.area = e.area");
		            sql.append(" and e.icona=i1.icona (+)");
		            sql.append(" and e.icona_ds=i2.icona (+)");
		            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
		            sql.append(" and m.area <> 'GDMSYS'");
		            sql.append(" AND NOT EXISTS (");
		           	sql.append("  SELECT e1.area, e1.codice_modello, e1.etichetta");
		            sql.append("  FROM etichette e1");
		            sql.append("  WHERE e1.area = m.area");
		            sql.append("  AND e1.codice_modello = m.codice_modello");
		            sql.append("  AND e1.etichetta = e.etichetta)");
		            sql.append(" union all ");
		            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
		            if(jdmsML!=null && !jdmsML.equals("")) 
			        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
			        else
			        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
		            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
		            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
		            sql.append("decode(e.etichetta,'CREADOC',2,'CREACART',3,'CREAQUERY',4,'COPIA',5,'SPOSTA',6,'ELIMINA',7,'COLLEGA',8,'INCOLLA',9,'RIMUOVI',10,");
		            sql.append("'FIRMA_STANDARD',11,'DELEGA',12,'COLL_DESKTOP',13,'COPIA_CONFORME',14,'VERIFICA_ALLEGATI',15,'LINK_DOC',16,'FIRMA',17,'LINKESTERNO',18) ordine ");
		            sql.append("from cartelle c, documenti d, modelli m, etichette e, icone i1, icone i2 ");
		            sql.append("where c.id_cartella=:idOggetto");
		            sql.append(" and c.id_documento_profilo=d.id_documento");
		            sql.append(" and d.id_tipodoc=m.id_tipodoc");
		            sql.append(" and d.area=m.area");
		            sql.append(" and e.codice_modello = '-'");
		            sql.append(" and e.area='GDMSYS'");
		            sql.append(" and e.icona=i1.icona (+)");
		            sql.append(" and e.icona_ds=i2.icona (+)");
		            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
		            sql.append(" order by ordine");
		            
		         }
		         else
		         {
		            tipoRicerca=controlloTipoQuery();
		            
		        	if (tipoRicerca.equals(""))
		            {
		            	sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
		            	if(jdmsML!=null && !jdmsML.equals("")) 
		            		sql.append("e.etichetta,  GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
				        else
				        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
			            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
			            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
			            sql.append("0 ordine ");
			            sql.append("from query q, documenti d, modelli m, etichette e, icone i1, icone i2 ");
			            sql.append("where q.id_query=:idOggetto");
			            sql.append(" and q.id_documento_profilo=d.id_documento");
			            sql.append(" and d.id_tipodoc=m.id_tipodoc");
			            sql.append(" and d.area=m.area");
			            sql.append(" and m.codice_modello = e.codice_modello");
			            sql.append(" and m.area = e.area");
			            sql.append(" and e.icona=i1.icona (+)");
			            sql.append(" and e.icona_ds=i2.icona (+)");
			            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
			            sql.append(" union all ");
			            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
			            if(jdmsML!=null && !jdmsML.equals("")) 
				        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
				        else
				        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
			            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
			            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
			            sql.append("1 ordine ");
			            sql.append("from query q, documenti d, modelli m, etichette e, icone i1, icone i2 ");
			            sql.append("where q.id_query=:idOggetto");
			            sql.append(" and q.id_documento_profilo=d.id_documento");
			            sql.append(" and d.id_tipodoc=m.id_tipodoc");
			            sql.append(" and d.area=m.area");
			            sql.append(" and e.codice_modello = '-'");
			            sql.append(" and m.area = e.area");
			            sql.append(" and e.icona=i1.icona (+)");
			            sql.append(" and e.icona_ds=i2.icona (+)");
			            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
			            sql.append(" and m.area <> 'GDMSYS'");
			            sql.append(" AND NOT EXISTS (");
			           	sql.append("  SELECT e1.area, e1.codice_modello, e1.etichetta");
			            sql.append("  FROM etichette e1");
			            sql.append("  WHERE e1.area = m.area");
			            sql.append("  AND e1.codice_modello = m.codice_modello");
			            sql.append("  AND e1.etichetta = e.etichetta)");
			            sql.append(" union all ");
			            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
			            if(jdmsML!=null && !jdmsML.equals("")) 
				        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
				        else
				        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
			            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
			            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
			            sql.append("decode(e.etichetta,'CREADOC',2,'CREACART',3,'CREAQUERY',4,'COPIA',5,'SPOSTA',6,'ELIMINA',7,'COLLEGA',8,'INCOLLA',9,'RIMUOVI',10,");
			            sql.append("'FIRMA_STANDARD',11,'DELEGA',12,'COLL_DESKTOP',13,'COPIA_CONFORME',14,'VERIFICA_ALLEGATI',15,'LINK_DOC',16,'FIRMA',17) ordine ");
			            sql.append("from query q, documenti d, modelli m, etichette e, icone i1, icone i2 ");
			            sql.append("where q.id_query=:idOggetto");
			            sql.append(" and q.id_documento_profilo=d.id_documento");
			            sql.append(" and d.id_tipodoc=m.id_tipodoc");
			            sql.append(" and d.area=m.area");
			            sql.append(" and e.codice_modello ='-'");
			            sql.append(" and e.area='GDMSYS'");
			            sql.append(" and e.icona=i1.icona (+)");
			            sql.append(" and e.icona_ds=i2.icona (+)");
			            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
			            sql.append(" order by ordine");
		            }
		            else
		            {
		            	sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
		            	if(jdmsML!=null && !jdmsML.equals("")) 
		            		sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
				        else
				        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
			            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
			            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
			            sql.append("0 ordine ");
			            sql.append("from query q, etichette e, icone i1, icone i2 ");
			            sql.append("where q.id_query=:idOggetto");
			            sql.append(" and e.codice_modello = :cmRicerca");
			            sql.append(" and e.area = :areaRicerca");
			            sql.append(" and e.icona=i1.icona (+)");
			            sql.append(" and e.icona_ds=i2.icona (+)");
			            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
			            sql.append(" union all ");
			            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
			            if(jdmsML!=null && !jdmsML.equals("")) 
				        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
				        else
				        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
			            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
			            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
			            sql.append("1 ordine ");
			            sql.append("from query q, etichette e, icone i1, icone i2 ");
			            sql.append("where q.id_query=:idOggetto");
			            sql.append(" and e.codice_modello ='-'");
			            sql.append(" and e.area = :areaRicerca");
			            sql.append(" and e.area <> 'GDMSYS'");
			            sql.append(" and e.icona=i1.icona (+)");
			            sql.append(" and e.icona_ds=i2.icona (+)");
			            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
			            sql.append(" AND NOT EXISTS (");
			           	sql.append("  SELECT e1.area, e1.codice_modello, e1.etichetta");
			            sql.append("  FROM etichette e1");
			            sql.append("  WHERE e1.area =  :areaRicerca");
			            sql.append("  AND e1.codice_modello =  :cmRicerca");
			            sql.append("  AND e1.etichetta = e.etichetta)");
			            sql.append(" union all ");
			            sql.append("select e.codice_modello, e.area, e.tipo_uso, ");
			            if(jdmsML!=null && !jdmsML.equals("")) 
				        	sql.append("e.etichetta, GDM_UTILITY.F_MULTILINGUA (e.valore,'"+jdmsML+"') text, e.CONTROLLO_JS, e.CONTROLLO, ");
				        else
				        	sql.append("e.etichetta, e.valore text, e.CONTROLLO_JS, e.CONTROLLO, ");
			            sql.append("e.icona, i1.tooltip title, i1.nome src, ");
			            sql.append("e.icona_ds, i2.tooltip title_ds, i2.nome src_ds, ");
			            sql.append("decode(e.etichetta,'CREADOC',2,'CREACART',3,'CREAQUERY',4,'COPIA',5,'SPOSTA',6,'ELIMINA',7,'COLLEGA',8,'INCOLLA',9,'RIMUOVI',10,");
			            sql.append("'FIRMA_STANDARD',11,'DELEGA',12,'COLL_DESKTOP',13,'COPIA_CONFORME',14,'VERIFICA_ALLEGATI',15,'LINK_DOC',16,'FIRMA',17) ordine ");
			            sql.append("from query q, etichette e, icone i1, icone i2 ");
			            sql.append("where q.id_query=:idOggetto");
			            sql.append(" and e.codice_modello ='-'");
			            sql.append(" and e.area='GDMSYS'");
			            sql.append(" and e.icona=i1.icona (+)");
			            sql.append(" and e.icona_ds=i2.icona (+)");
			            sql.append(" and (e.tipo_uso='C' OR e.tipo_uso='X')");
			            sql.append(" order by ordine");
		            }
		         }
	         } 
	         return sql.toString();
	 }
	 
 
	 /**
	   * Costruzione del vettore dei pulsanti
	 */
	 private void buildBottonieraVector() throws Exception
	 { 
	     	 String sql=bottonieraSQL();
        
	     	 try
	     	 {
	     		 dbOp.setStatement(sql);
	     		 
	     		 if(tipoOggetto.equals("C"))
			      dbOp.setParameter(":idOggetto",idOggetto);
	     		 else
	     		 {
					if (tipoRicerca.equals(""))
		        		dbOp.setParameter(":idOggetto",idOggetto);
		            else
		            {
		            	dbOp.setParameter(":idOggetto",idOggetto);
		            	dbOp.setParameter(":cmRicerca",cmRicerca);
		            	dbOp.setParameter(":areaRicerca",areaRicerca);
		            }
		         }
		         
	     		 dbOp.execute();
	     		 ResultSet rs = dbOp.getRstSet();
				     
	             while(rs.next())
	             {
	            	if(rs.getString("etichetta").equals("DELEGA") && !compDelega) 
	            	 continue;	
	            	
	            	if(rs.getString("etichetta").equals("LINKESTERNO") && tipoOggetto.equals("Q")) 
		            	 continue;	
	            	 
	            	if(rs.getString("etichetta").equals("LINK_DOC")) {
	            		if(!linkAPP.equals("")) {
		            	   Bottone b = new Bottone();
	      	               b.setArea(rs.getString("area"));
	      	               b.setCM(rs.getString("codice_modello"));
	      	               b.setControllo(rs.getString("controllo"));
	      	               b.setControlloJS(rs.getString("controllo_js"));
	      	               b.setEtichetta(rs.getString("etichetta"));
	      	               b.setIcona(rs.getString("icona"));
	      	               b.setIconaDS(rs.getString("icona_ds"));
	      	               b.setSrc(rs.getString("src"));
	      	               b.setSrcDS(rs.getString("src_ds"));
	      	               b.setText(rs.getString("text"));
	      	               b.setTitle(rs.getString("title"));
	      	               b.setTitleDS(rs.getString("title_ds"));
	      	               bottoniera.add(b);
	            		}	 
	            	}
	            	else {
	            		   Bottone b = new Bottone();
		 	               b.setArea(rs.getString("area"));
		 	               b.setCM(rs.getString("codice_modello"));
		 	               b.setControllo(rs.getString("controllo"));
		 	               b.setControlloJS(rs.getString("controllo_js"));
		 	               b.setEtichetta(rs.getString("etichetta"));
		 	               b.setIcona(rs.getString("icona"));
		 	               b.setIconaDS(rs.getString("icona_ds"));
		 	               b.setSrc(rs.getString("src"));
		 	               b.setSrcDS(rs.getString("src_ds"));
		 	               b.setText(rs.getString("text"));
		 	               b.setTitle(rs.getString("title"));
		 	               b.setTitleDS(rs.getString("title_ds"));
		 	               bottoniera.add(b);	 
	            	}	 
	             }
	             rs.close();
	         }
	     	 catch ( SQLException e ) {
	     		 throw e;
	     	 } 
	 } 
 
	 /**
	   * Costruzione della sequenza dei pulsanti
	   * dato il vettore in input.
	   * 
	   * @param bt Vettore di pulsanti
	   * @return String HTML della barra di visualizzazione 
	   * 
	 */
	 private String buildBottonieraView(Vector bt) throws Exception
	 { 
	         String bottoni=""; 
	         String sbottoniera="";
	         String pathIMG=_PATHIMG_BOTTONI;
	         String pathIMGDS=_PATHIMG_BOTTONI;
	         String corpoJS="";
	         String corpo="";
	         String text="",icona="",icona_ds="";
	         int NUM_PULSANTI = 7;
	         int offset = NUM_PULSANTI;
	         int x = 0;
	         int y = offset;
	         int numPulsanti = 0;
	         Vector<String> vBottoni = new Vector<String>();
	      
	         //Inserimento del pulsante Richiamo Documentale
	         if(!linkAPP.equals("")){
	           vBottoni.add(addButton("LINK_DOC","100"));
	           numPulsanti++;
	         }	 
	         
	         //Inserimento del pulsante Verifica Allegati
	         if(VERIFICA_ALLEGATI.equals("S")) {
	           vBottoni.add(addButton("VERIFICA_ALLEGATI","101"));
	           numPulsanti++;	           
	         }  
	        
	         if(numPulsanti>0){
		          offset=offset-numPulsanti;	 
		          y=offset;
		     }                  
	         
	         while(x<bt.size())
	         {
	           
	           int i=x;
	           while((i<y) && (i<bt.size()))
	           {
	             Bottone b = (Bottone) bt.get(i);
	             text="";
	             icona="";
	             icona_ds="";          
	             
	             /** Controllo della funzione JS associata */ 
	             if(b.getControlloJS()==null)
	             {
	               if( (b.getCM().equals("-")) && ((b.getEtichetta().equals("COPIA")) || (b.getEtichetta().equals("SPOSTA")) || (b.getEtichetta().equals("RIMUOVI")) || (b.getEtichetta().equals("ELIMINA")) || (b.getEtichetta().equals("FIRMA"))))
	                 corpoJS="return false;";
	               else
	                 corpoJS="return true;";
	             }
	             else
	             {
	               String sControlloJS=retrieveControllo(b.getArea(),b.getControlloJS());
	               corpoJS=sControlloJS.substring(sControlloJS.indexOf("@")+1,sControlloJS.length());
	             } 
	             
	             /** Controllo del controllo Java o PL/SQL o JavaScript associato */ 
	             if(b.getControllo()!=null)
	             { 
	               String sControllo=retrieveControllo(b.getArea(),b.getControllo());
	               /** Caso controlli bottoni standard */ 
	               String tipoControllo=sControllo.substring(0,sControllo.indexOf("@"));
	            	
	            	if(tipoControllo.equals("S")){
	            		
	            		 corpo=sControllo.substring(sControllo.indexOf("@")+1,sControllo.length())+" return false;";
	            		 
	            		 if(corpo!=null && corpo.indexOf(":")!=-1){
		            		 Profilo doc=new Profilo(profiloID);
		    	        	 doc.initVarEnv(this.vu);
		    	        	 if (!doc.accedi(Global.ACCESS_NO_ATTACH).booleanValue())
		    	        	 {
		    	        		 log.log_error("Impossibile accedere al Profilo del Documento - idOggetto:"+idOggetto+" - tipoOggetto:"+tipoOggetto+" - Profilo:"+profiloID+" - "+doc.getError()); 
		    	        		 throw new Exception("CCS_Bottoniera::retrieveCriterio() - Impossibile accedere al Profilo del Documento - idOggetto:"+idOggetto+" - tipoOggetto:"+tipoOggetto+" - Profilo:"+profiloID);
		    	        	 }
		            		 
		            		 BottonieraParser p = new BottonieraParser(req,doc);
		            		 corpo = p.bindingDeiParametri(corpo);
	            		 }
	            	}
	            	else {
	             		String qs = URLEncoder.encode(req.getQueryString());
	            		corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea()+"&qs="+qs);
	            		//corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea()+"&qs="+req.getQueryString());
	        		
	            	}
	              }
	             
	             /** Controllo testo del bottone */
	             if(b.text!=null)
	              text=b.text;
	             
	             /** Controllo icona associata */
	             if((b.getIcona()!=null))
	               icona=pathIMG+b.getIcona()+"/"+b.getSrc();
	             
	             /** Controllo icona disabilitata associata */
	             if(b.getIconaDS()!=null)
	               icona_ds=pathIMGDS+b.getIconaDS()+"/"+b.getSrcDS();
	             else
	               icona_ds=icona;
	             
	             if((b.getIcona()!=null))
	              controlloIcona(b.getIcona());
	             if(b.getIconaDS()!=null)
	              controlloIcona(b.getIconaDS());
	             
                 /** Inserimento del pulsante */
	             vBottoni.add(this.getBottone(""+(i+1),b.getTitle(),corpo,icona,icona_ds,text,corpoJS));
	             numPulsanti++;
	             i++;
	            }   
	       
		        x=x+offset;
		        y=y+offset;
		     }
	         
	         
	         //Costruzione delle righe di pulsanti
	         int n=0;
	         int p=NUM_PULSANTI;
	         
	         if(vBottoni.size()>0)
	          bottoni="<tr>";	 
	         
	         for(int i=0;i<vBottoni.size();i++){
	        	         	 
	        	 if(n==p){
	        	  bottoni+="</tr>\n<tr>";	 
	        	  p+=NUM_PULSANTI;
	        	 }
	        	 
	        	 bottoni+=vBottoni.get(i).toString();
	        	 n++;
	         }
	         bottoni+="</tr>\n";
	         
	         if(bottoni.equals(""))
	           bottoni="<tr><td></td></tr>";
	         
	         String input="<input id=\"numpulsanti\" type=\"hidden\" size=\"10\" value=\""+numPulsanti+"\" name=\"{numpulsanti}\" ></input>";
		     
	         sbottoniera+=h.getTable("",input+bottoni);
	         return sbottoniera;
	 }
	 
	 /**
	   * Costruzione della sequenza dei pulsanti standard
	   * dato il vettore in input.
	   * 
	   * @param bt Vettore di pulsanti
	   * @return String HTML della barra standard di visualizzazione 
	   * 
	 */
	 private String buildBottonieraViewBS(Vector bt) throws Exception
	 { 
	         String bottoni=""; 
	         String sbottoniera="";
	         String pathIMG=_PATHIMG_BOTTONI;
	         String pathIMGDS=_PATHIMG_BOTTONI;
	         String corpoJS="";
	         String corpo="";
	         String text="",icona="",icona_ds="";
	         /** Recupero la barra standard */
	         String barra_standard=retrieveCriterio(getBarraStandardSQL());
	         boolean verifica=true;
	         int NUM_PULSANTI = 8;
	         int offset=NUM_PULSANTI;
	         int x=0;
	         int y=offset;
	         int numPulsanti=0;
	         Vector<String> vBottoni = new Vector<String>();
	         	      
	         //Inserimento del pulsante Richiamo Documentale
	         if(!linkAPP.equals(""))
	         {
	        	 vBottoni.add(addButton("LINK_DOC","100"));
	        	 numPulsanti++;
	         }
	         
	         //Inserimento del pulsante Verifica Allegati
	         if(VERIFICA_ALLEGATI.equals("S")){
	        	 vBottoni.add(addButton("VERIFICA_ALLEGATI","101"));
	             numPulsanti++;
	       	 }	         
	         
	         if(numPulsanti>0){
		          offset=offset-numPulsanti;	 
		          y=offset;
		     }
	          
	         while(x<bt.size())
	         {
	           
	           int i=x;
	           while((i<y) && (i<bt.size()))
	           {
	             Bottone b = (Bottone) bt.get(i);
	             text="";
	             icona="";
	             icona_ds="";          
	             
	             /** Vengono visualizzati soltanto i bottoni standard con Codice Modello="-" */
	             if((b.getCM().equals("-") && ( barra_standard.indexOf(b.getEtichetta())!=-1)) && (!b.getEtichetta().equals("FIRMA")))
	     	     {
	            	 	 
	            	 if(tipoOggetto.equals("Q") && (b.getEtichetta().equals("CREADOC")))
	 			       verifica=verificaCompPulsante();	 
	 			      
	            	 if(verifica)
	            	 {
	            	
	            	 /** Controllo della funzione JS associata */ 
		             if(b.getControlloJS()==null)
		             {
		               if( (b.getCM().equals("-")) && ((b.getEtichetta().equals("COPIA")) || (b.getEtichetta().equals("SPOSTA")) || (b.getEtichetta().equals("RIMUOVI")) || (b.getEtichetta().equals("ELIMINA")) || (b.getEtichetta().equals("FIRMA"))))
		                 corpoJS="return false;";
		               else
		                 corpoJS="return true;";
		             }
		             else
		             {
		            	String sControlloJS=retrieveControllo(b.getArea(),b.getControlloJS());
		                corpoJS=sControlloJS.substring(sControlloJS.indexOf("@")+1,sControlloJS.length());
		             }
		             
		             /** Controllo del controllo Java o PL/SQL o JavaScript associato */ 
		             if(b.getControllo()!=null)
		             { 
		               String sControllo=retrieveControllo(b.getArea(),b.getControllo());
		               
		               /** Caso controlli bottoni standard */
		               String tipoControllo=sControllo.substring(0,sControllo.indexOf("@"));
		               if(tipoControllo.equals("S"))
		                 corpo=sControllo.substring(sControllo.indexOf("@")+1,sControllo.length())+" return false;";
		            	else{
		            		String qs = URLEncoder.encode(req.getQueryString());
		            		corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea()+"&qs="+qs);
		              	    //corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea());
		     		    }	
		             }
		             
		             /** Controllo testo del bottone */
		             if(b.text!=null)
		              text=b.text;
		             
		             /** Controllo icona associata */
		             if((b.getIcona()!=null))
		               icona=pathIMG+b.getIcona()+"/"+b.getSrc();
		             
		             /** Controllo icona disabilitata associata */
		             if(b.getIconaDS()!=null)
		               icona_ds=pathIMGDS+b.getIconaDS()+"/"+b.getSrcDS();
		             else
		               icona_ds=icona;
		             
		             if((b.getIcona()!=null))
			           controlloIcona(b.getIcona());
			         if(b.getIconaDS()!=null)
			           controlloIcona(b.getIconaDS());
		             
		             /** Inserimento del pulsante */ 
		             vBottoni.add(this.getBottone(""+(i+1),b.getTitle(),corpo,icona,icona_ds,text,corpoJS));
		             numPulsanti++;
	            	 }
	             }	 
	             
	             verifica=true;
		         i++;
	           }  
	           
	           x=x+offset;
	           y=y+offset;
	         }
	        

	         //Costruzione delle righe di pulsanti
	         int n=0;
	         int p=NUM_PULSANTI;
	         
	         if(vBottoni.size()>0)
	          bottoni="<tr>";	 
	         
	         for(int i=0;i<vBottoni.size();i++){
	        	         	 
	        	 if(n==p){
	        	  bottoni+="</tr>\n<tr>";	 
	        	  p+=NUM_PULSANTI;
	        	 }
	        	 
	        	 bottoni+=vBottoni.get(i).toString();
	        	 n++;
	         }
	         bottoni+="</tr>\n";
	         
	         // Nel caso in cui non esiste nessun pulsante
	         if(bottoni.equals(""))
	           bottoni="<tr><td></td></tr>";
	         
	         String input="<input id=\"numpulsanti\" type=\"hidden\" size=\"10\" value=\""+numPulsanti+"\" name=\"{numpulsanti}\" ></input>";
	         
	         sbottoniera+=h.getTable("",input+bottoni);
	         return sbottoniera;
	 }
	
	 /**
	   * Costruzione della sequenza dei pulsanti standard
	   * dato il vettore in input.
	   * 
	   * @param bt 			Vettore di pulsanti
	   * @return vPulsanti 	Vettore della sequenza di pulsanti in formato XML
	   * 
	 */
	 private Vector buildPulsantiStandard(Vector bt) throws Exception
	 { 
	         Vector vPulsanti = new Vector();
	         String pathIMG=_PATHIMG_BOTTONI;
	         String pathIMGDS=_PATHIMG_BOTTONI;
	         String corpoJS="";
	         String corpo="";
	         String text="",icona="",icona_ds="",title="";
	         
	         /** Recupero la barra standard */
	         String barra_standard=retrieveCriterio(getBarraStandardSQL());
	         
	         int offset=8;
	         int x=0;
	         int y=offset;
	      
	         while(x<bt.size())
	         {
	           int i=x;
	           while((i<y) && (i<bt.size()))
	           {
	             Bottone b = (Bottone) bt.get(i);
	             text="";
	             title="";
	             icona="";
	             icona_ds="";          
	             
	             /** Vengono visualizzati soltanto i bottoni standard con Codice Modello="-" */
	             if((b.getCM().equals("-") && ( barra_standard.indexOf(b.getEtichetta())!=-1)) )
	             {
	           	 	 /** Controllo della funzione JS associata */ 
		             if(b.getControlloJS()==null)
		             {
		               if( (b.getCM().equals("-")) && ((b.getEtichetta().equals("COPIA")) || (b.getEtichetta().equals("SPOSTA")) || (b.getEtichetta().equals("RIMUOVI")) || (b.getEtichetta().equals("ELIMINA")) || (b.getEtichetta().equals("FIRMA"))))
		                 corpoJS="return false;";
		               else
		                 corpoJS="return true;";
		             }
		             else
		             {
		            	String sControlloJS=retrieveControllo(b.getArea(),b.getControlloJS());
		                corpoJS=sControlloJS.substring(sControlloJS.indexOf("@")+1,sControlloJS.length());
		             }
		             
		             /** Controllo del controllo Java o PL/SQL o JavaScript associato */ 
		             if(b.getControllo()!=null)
		             { 
		               String sControllo=retrieveControllo(b.getArea(),b.getControllo());
		               String tipoControllo=sControllo.substring(0,sControllo.indexOf("@"));
		               
		               if(tipoControllo.equals("S"))
		            	 corpo=sControllo.substring(sControllo.indexOf("@")+1,sControllo.length());
		               else{
		            		String qs = URLEncoder.encode(req.getQueryString());
		            		corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea()+"&qs="+qs);
		              	    //corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea());
		     		    }			             }
		             
		             /** Controllo testo del bottone */
		             if(b.text!=null)
		              text=b.text;
		             
		             /** Controllo icona associata */
		             if((b.getIcona()!=null))
		               icona=pathIMG+b.getIcona()+"/"+b.getSrc();
		             
		             /** Controllo icona disabilitata associata */
		             if(b.getIconaDS()!=null)
		               icona_ds=pathIMGDS+b.getIconaDS()+"/"+b.getSrcDS();
		             else
		               icona_ds=icona;
		             
		             /** Costruzione XML del contenuto di un pulsante */
		             String pulsante="";
		             pulsante +="<TITLE><![CDATA["+b.title+"]]></TITLE>";
		             pulsante +="<TEXT><![CDATA["+text+"]]></TEXT>";
		             pulsante +="<ACTION><![CDATA["+corpo+"]]></ACTION>";
		             
		     	     /** Inserimento del pulsante in formato XML */
		             vPulsanti.add(pulsante);
	             } 	
		         i++;
	           }  
	           x=x+offset;
	           y=y+offset;
	         }	        
	         return vPulsanti;
	 }
	 
	 /** Controllo dell'esistenza ed eventuale distribuizione delle icone */
	 private void controlloIcona(String icona) throws Exception
	  {
	          String sql,nome_icona,pathIcona;
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
			      log.log_error("CCS_Bottoniera::controlloIcona() - Icona ["+icona +"] non trovata sul DB");          
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
			    if (!fdir.isDirectory()) {
			        fdir.mkdirs(); 
			    }
				
			    /** Creazione del file */
			    ffile = new File(pathIcona+File.separator+nome_icona);
			   
				/** Controllo esistenza e data di ultima modifica */
				if (!ffile.exists() || ffile.lastModified() != data)
				{
					risorseBlob = dbOp.readBlob("risorsa");
				  
				    if (risorseBlob == null) {
				        log.log_error("CCS_Bottoniera::controlloIcona() - Problemi di caricamento della risorsa icona.InputStream vuoto.");              
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
	   * Costruzione della sequenza dei pulsanti standard
	   * dato il vettore in input.
	   * 
	   * @param bt 			Vettore di pulsanti
	   * @return vPulsanti 	Vettore della sequenza di pulsanti in formato XML
	   * 
	 */
	 private Vector buildPulsanti(Vector bt) throws Exception
	 { 
	         Vector vPulsanti = new Vector();
	         String pathIMG=_PATHIMG_BOTTONI;
	         String pathIMGDS=_PATHIMG_BOTTONI;
	         String corpoJS="";
	         String corpo="";
	         String text="",icona="",icona_ds="",title="";
	         int offset=8;
	         int x=0;
	         int y=offset;
	         
	         while(x<bt.size())
	         {
	           int i=x;
	           while((i<y) && (i<bt.size()))
	           {
	             Bottone b = (Bottone) bt.get(i);
	             text="";
	             title="";
	             icona="";
	             icona_ds="";          
	             
	             /** Controllo della funzione JS associata */ 
	             if(b.getControlloJS()==null)
	             {
	               if( (b.getCM().equals("-")) && ((b.getEtichetta().equals("COPIA")) || (b.getEtichetta().equals("SPOSTA")) || (b.getEtichetta().equals("RIMUOVI")) || (b.getEtichetta().equals("ELIMINA")) || (b.getEtichetta().equals("FIRMA"))))
	                 corpoJS="return false;";
	               else
	                 corpoJS="return true;";
	             }
	             else
	             {
	               String sControlloJS=retrieveControllo(b.getArea(),b.getControlloJS());
	               corpoJS=sControlloJS.substring(sControlloJS.indexOf("@")+1,sControlloJS.length());
	             } 
	             
	             /** Controllo del controllo Java o PL/SQL o JavaScript associato */ 
	             if(b.getControllo()!=null)
	             { 
	               String sControllo=retrieveControllo(b.getArea(),b.getControllo());
	               String tipoControllo=sControllo.substring(0,sControllo.indexOf("@"));
	            	
	            	if(tipoControllo.equals("S"))
	            	   corpo=sControllo.substring(sControllo.indexOf("@")+1,sControllo.length());
	            	else{
	            		String qs = URLEncoder.encode(req.getQueryString());
	            		corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea()+"&qs="+qs);
	              	    //corpo = h.Controllo_Output_Pop("ControlliBottoniera.do?controllo="+b.getControllo()+"&area="+b.getArea());
	     		    }		             }
	             
	             /** Controllo testo del bottone */
	             if(b.text!=null)
	              text=b.text;
	             
	             /** Controllo icona associata */
	             if((b.getIcona()!=null))
	               icona=pathIMG+b.getIcona()+"/"+b.getSrc();
	             
	             /** Controllo icona disabilitata associata */
	             if(b.getIconaDS()!=null)
	               icona_ds=pathIMGDS+b.getIconaDS()+"/"+b.getSrcDS();
	             else
	               icona_ds=icona;
	             
	             /** Costruzione XML del contenuto di un pulsante */
	             String pulsante="";
	             pulsante +="<TITLE><![CDATA["+b.getTitle()+"]]></TITLE>";
	             pulsante +="<TEXT><![CDATA["+text+"]]></TEXT>";
	             pulsante +="<ACTION><![CDATA["+corpo+"]]></ACTION>";
	             
	     	     /** Inserimento del pulsante in formato XML */
	             vPulsanti.add(pulsante);
	            
	             i++;
	            }   
	         
		        x=x+offset;
		        y=y+offset;
	         }      
	        
	         return vPulsanti;
	 }

 
	 /**
	   * Recupero controllo associato al pulsante
	   * data la coppia Area e Controllo.
	   * 
	   * @param area 		Area
	   * @param controllo 	Nome del Controllo
	   * @return String 	corpo del controllo 
	   * 
	 */
	 private String retrieveControllo(String area,String controllo) throws Exception
	 { 
	         StringBuffer sql= new StringBuffer();
	         String corpo="";
	           
	         try 
	         {
	             sql.append("select controllo,corpo,tipo ");
	             sql.append("from libreria_controlli l ");
	             sql.append("where area = :AREA and controllo = :CONTROLLO ");
			     dbOp.setStatement(sql.toString());
			     dbOp.setParameter(":AREA",area);
			     dbOp.setParameter(":CONTROLLO",controllo);
	             dbOp.execute();
	             ResultSet rs = dbOp.getRstSet();
				 if(rs.next())
	             {
	                corpo=rs.getString("tipo")+"@"+rs.getString("corpo");
	             }
	          }
	          catch ( SQLException e ) {
	        	  log.log_error("CCS_Bottoniera::retrieveTipoControllo() - Recupero controllo pulsante - Area: "+area+" - Controllo: "+controllo+" - SQL: "+sql);  
	        	  throw e;
	          } 
	          return corpo;
	 } 
	 
	 /**
	   * Recupero della sequenza di pulsanti secondo
	   * un certo ordine definito dal dominio associato.
	   * 
	   * @return String ordine di sequenza dei pulsanti 
	   * 
	 */
	 private String retrieveOrdineVis() throws Exception
	 { 
		     StringBuffer sql= new StringBuffer();
	         String corpo="";
	           
	         try 
	         {
	        	 if(tipoOggetto.equals("C"))
	             {
	        		 sql.append("select d.dominio, m.area, m.codice_modello, d.istruzione ");
	                 sql.append("from cartelle c, documenti doc, modelli m, domini d ");
	                 sql.append("where c.id_cartella = :IDOGGETTO ");
	                 sql.append(" and c.id_documento_profilo=doc.id_documento");
	    		     sql.append(" and doc.id_tipodoc=m.id_tipodoc");
	                 sql.append(" and doc.area=m.area");
	                 sql.append(" and m.area=d.area");
	                 sql.append(" and m.codice_modello=d.codice_modello");
	                 sql.append(" and d.precarica='C'");
	                 sql.append(" and d.SEQ_DOMINIO_AREA >0 ");
	                 sql.append(" order by seq_dominio_area");
	                 dbOp.setStatement(sql.toString());
	                 dbOp.setParameter(":IDOGGETTO",idOggetto);
	             }
	             else
	             {
	                 if(tipoRicerca.equals(""))
	                 {
	                	 sql.append("select d.dominio, m.area, m.codice_modello, d.istruzione ");
			             sql.append("from query q, documenti doc, modelli m, domini d ");
			             sql.append("where q.id_query = :IDOGGETTO ");
			             sql.append(" and q.id_documento_profilo=doc.id_documento");
					     sql.append(" and doc.id_tipodoc=m.id_tipodoc");
			             sql.append(" and doc.area=m.area");
			             sql.append(" and m.area=d.area");
			             sql.append(" and m.codice_modello=d.codice_modello");
			             sql.append(" and d.precarica='C'");
			             sql.append(" and d.SEQ_DOMINIO_AREA >0 ");
			             sql.append(" order by seq_dominio_area"); 
		                 dbOp.setStatement(sql.toString());
		                 dbOp.setParameter(":IDOGGETTO",idOggetto);
	                 }
	                 else
	                 {
	                	 sql.append("select d.dominio, d.area, d.codice_modello, d.istruzione ");
			             sql.append("from query q, domini d ");
			             sql.append("where q.id_query = :IDOGGETTO ");
			             sql.append(" and d.area = :AREARICERCA ");
			             sql.append(" and d.codice_modello = :CMRICERCA ");
			             sql.append(" and d.precarica='C'");
			             sql.append(" and d.SEQ_DOMINIO_AREA >0 ");
			             sql.append(" order by seq_dominio_area");
		                 dbOp.setStatement(sql.toString());
		                 dbOp.setParameter(":IDOGGETTO",idOggetto);
		                 dbOp.setParameter(":AREARICERCA",areaRicerca);
		                 dbOp.setParameter(":CMRICERCA",cmRicerca);
	                 }
	             }
	        	
	             dbOp.execute();
	             ResultSet rs = dbOp.getRstSet();
				 while(rs.next())
	             {
	               try {
	                 ordineVis.add(Global.leggiClob(dbOp,"istruzione"));
	               }
	               catch (Exception e) {                                
	            	   throw new Exception("CCS_Bottoniera::retrieveOrdineVis() - Errore in lettura Blocco - SQL: "+sql);
	    	       }
	             }
		       }
		       catch ( SQLException e ) {
		    	   log.log_error("CCS_Bottoniera::retrieveOrdineVis() - Errore in lettura Blocco - SQL: "+sql);
		    	   throw e;
		      } 
		       return corpo;
	 } 
	 
	 /**
	   * Costruzione della sequenza dei pulsanti secondo un 
	   * criterio di visualizzazione associato.
	   * 
	 */
 	 private void buildBottonieraORD() throws Exception
	 { 
	         String etichetta;
	         String criterio;
	         Vector bt=new Vector();
	         bt=bottoniera;
	         boolean verifica=true;
	         
	         /** Recupero criterio di visualizzazione */
	         retrieveOrdineVis();
	         
	         for(int i=0;i<ordineVis.size();i++)
	         {
	           criterio=retrieveCriterio(ordineVis.get(i).toString()); 
	           	           
	           if(criterio!=null)
	           {
	        	   StringTokenizer s= new StringTokenizer(criterio,"#");
		           while (s.hasMoreTokens())
			       {
			         etichetta=s.nextToken();
			         
			         if(tipoOggetto.equals("Q") && (etichetta.equals("CREADOC")))
			           verifica=verificaCompPulsante();	 
			         
			         if(verifica && (!etichetta.equals("LINK_DOC")))
			         {
			        	 for(int j=0;j<bt.size();j++)
			             {
			               Bottone b = (Bottone) bt.get(j);
			               if((etichetta.equals(b.getEtichetta())))
			               { 
			                 if(!esisteEtichetta(bottonieraORD,etichetta)) 
			            	   bottonieraORD.add(b);
			                  bt.remove(j);
			                  break;
			               } 
			             } 
			          }
			          verifica=true; 
			       }
	           }
	         }    
	  }
 	 
 	 private boolean esisteEtichetta(Vector v,String etichetta)  throws Exception {
 		     
 		     for(int i=0;i<v.size();i++) {
 		    	Bottone b = (Bottone) v.get(i);
 		    	if(etichetta.equals(b.getEtichetta()))
 		    	  return true;	
 		     }
 		     
 		     return false;
 	 }
 	  	 
 	 private boolean verificaCompPulsante() throws Exception
 	 { 
 		     GD4_Gestione_Query q = new GD4_Gestione_Query(Integer.parseInt(idOggetto),vu);
 		     String area=q.getArea();
 		     String listaTipiDoc=q.getListaTipiDoc();
 		     StringBuffer sql = new StringBuffer();
	         
 		     if(area==null)
 		      area="";	 
 		     
 		     if(listaTipiDoc.equals("(' ')"))
 		      listaTipiDoc="";	 
 		     
	         try 
	         {
	             sql.append("SELECT id_tipodoc  FROM modelli WHERE  gdm_competenza.gdm_verifica ('TIPI_DOCUMENTO',id_tipodoc,");
	             sql.append(" 'C', :USER ,f_trasla_ruolo ( :USER ,'GDMWEB','GDMWEB'),TO_CHAR (SYSDATE, 'dd/mm/yyyy')) = 1 ");
	             sql.append(" AND tipo_uso IN ('X', 'D') AND area = NVL ( :AREA , area) ");
	             sql.append(" AND (instr(:LISTATIPIDOC,''''||codice_modello||'''') >0 OR :LISTATIPIDOC IS NULL )");
	      	             
			     dbOp.setStatement(sql.toString());
			     dbOp.setParameter(":USER",user);
			     dbOp.setParameter(":AREA",area);
			     dbOp.setParameter(":LISTATIPIDOC",listaTipiDoc);
	             dbOp.execute();
	             ResultSet rs = dbOp.getRstSet();
				 if(rs.next())
	               return true;
				 else
			       return false; 		 
	             
	          }
	          catch ( SQLException e ) {
	        	  log.log_error("CCS_Bottoniera::verificaCompPulsante() - Verifica Competenze sul CREADOC - idOggetto: "+idOggetto+" - tipoOggetto: "+tipoOggetto+" - SQL: "+sql);  
	        	  throw e;
	          } 	         		     
 	 }
 	 
 	/**
	   * Recupero della sequenza di etichette concatenate con il simbolo '#'
	   * secondo il criterio di visualizzazione dei pulsanti.
	   * Ad esempio: COPIA#CREACART#CREADOC#.....
	   * 
	   * @return String sequenza
	   *  
	 */
	 private String retrieveCriterio(String sql) throws Exception
	 { 
	         String criterio="";      
	         try 
	         {
	        	 Profilo doc=new Profilo(profiloID);
	        	 doc.initVarEnv(this.vu);
	        	 if (!doc.accedi(Global.ACCESS_NO_ATTACH).booleanValue())
	        	 {
	        		 log.log_error("Impossibile accedere al Profilo del Documento - idOggetto:"+idOggetto+" - tipoOggetto:"+tipoOggetto+" - Profilo:"+profiloID+" - "+doc.getError()); 
	        		 throw new Exception("CCS_Bottoniera::retrieveCriterio() - Impossibile accedere al Profilo del Documento - idOggetto:"+idOggetto+" - tipoOggetto:"+tipoOggetto+" - Profilo:"+profiloID);
	        	 }
	        	 BottonieraParser p = new BottonieraParser(req,doc);
	        	 String queryStm = p.bindingDeiParametri(sql);
	             if (queryStm == null){
	            	 throw new Exception("CCS_Bottoniera::retrieveCriterio() - Attenzione! Parametro mancante su " +sql);
	             }
	             dbOp.setStatement(queryStm);
	             dbOp.execute();
	             ResultSet rs = dbOp.getRstSet();
					     
	             while(rs.next())
	               criterio+=rs.getString(1);
	         }
	         catch ( SQLException e ) {
	        	 log.log_error("CCS_Bottoniera::retrieveCriterio() - Attenzione! Parametro mancante su " +sql);
	        	 throw e;
	         } 
	         return criterio;
	 } 
	 
	 /**
	   * Recupero idDocumento del proifilo associato 
	   * all' oggetto Cartella o WRKSP o Query.
	   * 
	   * @param id 			idOggetto
	   * @param tipo 		tipo
	   * @return String 	idDocumento 
	   * 
	 */
	 private void retrieveProfiloDoc(String id,String tipo) throws Exception
	 { 
	         StringBuffer sql=new StringBuffer();
	           
	         try 
	         {
	             if(tipo.equals("C")){
	              sql.append("select id_documento_profilo as id, m.area "); 
	              sql.append("from cartelle c, documenti doc, modelli m "); 
	              sql.append("where c.id_cartella = :IDOGGETTO AND c.id_documento_profilo = doc.id_documento ");
	              sql.append("AND doc.id_tipodoc = m.id_tipodoc ");
	              sql.append("AND doc.area = m.area ");
	             }
	             else {
				  sql.append("select id_documento_profilo as id , m.area ");
				  sql.append("from QUERY q, documenti doc, modelli m "); 
				  sql.append("where q.id_query = :IDOGGETTO AND q.id_documento_profilo = doc.id_documento ");
				  sql.append("AND doc.id_tipodoc = m.id_tipodoc ");
				  sql.append( "AND doc.area = m.area ");
	             }
	             
			     dbOp.setStatement(sql.toString());
			     dbOp.setParameter(":IDOGGETTO",id);
	             dbOp.execute();
	             ResultSet rs = dbOp.getRstSet();
				 if(rs.next()) {
	               profiloID=rs.getString("id");
	               profiloArea=rs.getString("area");
	             }
	          }
	          catch ( SQLException e ) {
	        	  log.log_error("CCS_Bottoniera::retrieveIDProfiloDoc() - Recupero idDocumento del profilo associato - idOggetto: "+idOggetto+" - tipoOggetto: "+tipoOggetto+" - SQL: "+sql);  
	        	  throw e;
	          } 	         
	 } 
	 
	 /**
	   * Costruzione pulsante
	   * 
	   * @return String HTML pulsante
	   * 
	 */
	 private String getBottone(String index,String title,String onclick,String srcImg,String srcImgDS,String text,String corpoJS) 
	 {
	         return h.getTD("","",h.getButton(index,title,onclick,srcImg,text,corpoJS)+h.getInput("","","ICONA","hidden",srcImg,"ICONA")+h.getInput("","","ICONA_DS","hidden",srcImgDS,"ICONA_DS"));   
	         //return h.getTD("","",h.getButtonJquery(index,title,onclick,srcImg,text,corpoJS)+h.getInput("","","ICONA","hidden",srcImg,"ICONA")+h.getInput("","","ICONA_DS","hidden",srcImgDS,"ICONA_DS"));   
	 }
  
	 /***************************************************************************
	  * Gestione Controlli con integrazione JSYNC
	  **************************************************************************/
	 
	 /**
	   * Inizializzazione di alcuni parametri
	   * per la gestione dei pulsanti.
	   * 
	 */	   
	 private void initParametri()  throws Exception
	 {
	        try {

		         String separa="/";
		         log.log_info("CCS_Bottoniera::initParametri() - Inizio Costruzione del path del file di properties ");
			     
		         /** Definizione del path properties */
		         if(req!=null && req.getSession().getAttribute("PATH_INIFILE")!=null)
		        	 inifile = req.getSession().getAttribute("PATH_INIFILE").toString();
		         		         
		         if(inifile==null || (inifile!=null && inifile.equals("")))
		        	inifile = sPath.replace("jdms","jgdm") + "config" + separa + "gd4dm.properties"; 
		         
		         log.log_info("CCS_Bottoniera::initParametri() - Il parametro inifile:"+inifile);
		         
		         /** Controllo path del file properties */ 
		         File f = new File(inifile);
		         if (!f.exists()) {
		        	 inifile = sPath.replace("jdms","jgdm") + "config" + separa + "gd4dm.properties";
		        	 log.log_info("CCS_Bottoniera::initParametri() - Controllo del file che non esiste - inifile:"+inifile);
		         }
		       
		         Parametri.leggiParametriStandard(inifile);
		         
		         log.log_info("CCS_Bottoniera::initParametri() - Fine Costruzione del path del file di properties - inifile:"+inifile);
			 }
		     catch ( Exception e ) {
		    	 log.log_error("CCS_Bottoniera::initParametri() - Costruzione del file path gd4dm.properties :: "+inifile);
		    	 throw e;
		     } 
	 }
	 	 
	 /**
	   * Per ogni oggetto della lista viene effettuato la verifica
	   * per l'autorizzazzione ad eseguire il controllo.
	   * Nel caso di mancata autorizzazione per qualche oggetto della lista 
	   * l'esecuzione del controllo viene effettuata per una lista più ristretta.
	   * 
	   * @return String[] sequenza di oggetti
	   * 
	 */
	 private String[] verificaControllo(Element lista)  throws Exception
	 {
		  	 String seq="",utente=user; 
		  	 String[] elenco=null;
		  	        
	         try
		     {
		         if(lista!=null)
		         {	   
			       for(Iterator iterator = lista.elementIterator(); iterator != null && iterator.hasNext();)
		           {
			    	   Element listaID = (Element)iterator.next();
			    	   String tipoOggetto=leggiValoreXML(listaID,"TIPOOGGETTO");
			    	   String idOggetto=leggiValoreXML(listaID,"IDOGGETTO");
		      		   String ute=leggiValoreXML(listaID,"UTENTE");
		      		   if(ute!=null)
		      		    utente=ute;
		      		   if(verificaControlloOggetto(idOggetto,tipoOggetto,utente))
		          	   {  
		          	     seq+=tipoOggetto+idOggetto+"@";
		     	       }
		          	 }	  
			         if(seq!="")
			    	   elenco=seq.split("@");
				         
		        }
			 }
	         catch ( Exception e ) {
	          throw e;
	         } 
	         return elenco; 
	 }
	 
	 /**
	   * Per ogni oggetto della lista viene creato un oggetto di tipo JSYNC 
	   * aprendo la connessione verso il workflow solo se l'attività del pulsante
	   * è associata alla lista degli oggetti e non all'oggetto padre (Cartella o Query).
	   * 
	   * @param id 		idoggetto 
	   * @param tipo 	tipo oggetto
	   *  
	   * @return String[] sequenza di oggetti per cui si è ottenuto l'autorizzazione
	   * 				  di esecuzione del controllo.
	   * 
	 */
	 private boolean verificaControlloOggetto(String id,String tipo,String ute)  throws Exception
	 {
 	  	     String idOggetto,syncErr;
 	  	     SyncSuite sync=null;
 	  	     Vector ite=null;
	 	  	    
 	  	     /** Se il tipo oggetto è una Cartella viene recuperato idoggetto
 	  	      * del VIEW_CARTELLA, Se il tipo oggetto è un Documento viene recuperato 
 	  	      * la terna AREA@CM@CR altrimenti id se l'oggetto è di tipo Query. */
 	  	     if((tipo.equals("C")) || (tipo.equals("X")))
 	  	      idOggetto=(new DocUtil(vu)).getIdViewCartellaByIdCartella(id);	
 	  	     else
 	  	      if(tipo.equals("D"))
 	  	       idOggetto=(new DocUtil(vu)).getAreaCmCrByIdDocumento(id);
 	  	      else
 	  	       idOggetto=id;
	 	  	   
 	  	     /** Nel caso del Primo Controllo:
 	  	      *  Se l'attività JSuiteSync è associata ad ogni oggetto della lista
 	  	      *  viene eseguito la verifica di esecuzione del controllo */ 
 	  	     if(SBLOCCO_AUTOMATICO.equals("S"))
 	  	     { 	  	    
 	  	    	if(EFFETTUA_CONTROLLO_FIGLI)
 	  	    	{   
	 	  	      sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
			      Vector iter  = (Vector)sync.isExecutable(area,controllo,idOggetto,ute);
			      if (iter==null)
			        ite=new Vector();
			      else
			        ite=iter;
			      syncErr = sync.getLastError();
			     
	              /** Controllo se ci sono stati errori:
	               *  Se syncErr==NULL non si è verificato ERRORE
	               *  Se syncErr!=NULL si è verificato un ERRORE
	               */ 
		          if (syncErr != null) {
		        	log.log_error("CCS_Bottoniera::verificaControlloOggetto() -- Esecuzione CONTROLLO: (area,controllo)=("+area+","+controllo+") - UTENTE: "+ute+" - OGGETTO: "+idOggetto+" - syncErr: "+syncErr); 
		   		 	closeSync(sync);	 
		       	    return false;
		          }
		          /** Controllo il valore di isExecutable se è nullo non è possibile eseguire il controllo */
			      if (ite.size()==0) 
			      { 
			    	 /** Inserimento dell'oggetto nella struttura dati */
			         p.put(tipo+id,new ControlliJSINK(ite,sync,null));   
			         closeSync(sync);	 
			         return true;
			      }
 	  	    	}
 	  	     	
                /** Inserimento dell'oggetto nella struttura dati */
	            p.put(tipo+id,new ControlliJSINK(ite,sync,null));  
	 	  	 }
		     else  /** Nel Caso del Secondo Controllo */
		     {
        	  if(area2!=null && controllo2!=null)
        	  { 	 
	        	if(EFFETTUA_CONTROLLO_FIGLI)
	        	{  
	        	  sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
	        	  Vector iter = (Vector)sync.isExecutable(area2,controllo2,idOggetto,ute);
	        	  if (iter==null)
				    ite=new Vector();
				  else
				    ite=iter;
	        	  syncErr = sync.getLastError();
		          /** Controllo se ci sono stati errori:
	               *  Se syncErr==NULL non si è verificato ERRORE
	               *  Se syncErr!=NULL si è verificato un ERRORE
	               */ 
		          if (syncErr != null) {
		        	log.log_error("CCS_Bottoniera::verificaControlloOggetto() -- Esecuzione CONTROLLO: (area,controllo)=("+area2+","+controllo2+") - UTENTE: "+ute+" - OGGETTO: "+idOggetto+" - syncErr: "+syncErr); 
				   	closeSync(sync);	 
			        return false;
			      }
			      /** Controllo il valore di isExecutable se è nullo non è possibile eseguire il controllo */
		          if (ite.size()== 0) 
		          { 
		        	/** Inserimento dell'oggetto nella struttura dati */
			        p2.put(tipo+id,new ControlliJSINK(ite,sync,null));
		        	closeSync(sync);
		        	return true;
		            //return false;
		          }
	        	 }
	        	 /** Inserimento dell'oggetto nella struttura dati */
	             p2.put(tipo+id,new ControlliJSINK(ite,sync,null));
        	  }
		     }
			 return true;
	 }
	 
	 /**
	   * Per ogni oggetto della lista viene effettuato la conferma di avvenuta esecuzione 
	   * dell'attività JSuiteSync associata se esiste.
	   * 
	   * @param id 		    idoggetto 
	   * @param Properties 	struttura dati degli oggetti della lista
	   * 
	 */
     private void confermaEsecuzioneControllo(String idOggetto,Properties prop)  throws Exception
     {
    	     ControlliJSINK c=null;
    	     Vector ite;
    	     SyncSuite sync;
    	     
    	     try
             { 
    	      if(prop.containsKey(idOggetto))
    	      { 
    	        c=(ControlliJSINK)prop.get(idOggetto);
    	        ite=c.getIterable();
    	        sync=c.getSyncSuite();
    	        if(sync!=null)
    	        {	
	     	        boolean okrun = true;
	     	        Iterator i = ite.iterator();
	                while (i.hasNext())
	                {
	                  Integer id=(Integer)(i.next());
	                  okrun = okrun && (sync.executed(id.intValue()) == SyncSuite.SYNC_ESEGUITO);
	                }	              
	                
	                if (okrun)
	  				  sync.commit();
	  			    else 
	  				  sync.rollback();
	                closeSync(sync);
    	        }
    	      }
    	      else
    	    	throw new Exception("CCS_Bottoniera::confermaEsecuzioneControllo() - Oggetto: "+idOggetto+" non contenuto nella lista!");           
    	     }
	         catch ( Exception e ) {
                 log.log_error("CCS_Bottoniera::confermaEsecuzioneControllo() - Oggetto:"+idOggetto);
                 throw e;
	         }	         
     }
          
     private String buildMSGfromListaID(Element lista) throws Exception
     {
    	     String elencoMSG="",elencoMSGError=""; 
    	     String message="";
    	     
    	     try
   	         {
	   	         if(lista!=null)
	   	         {	   
	   		       for(Iterator iterator = lista.elementIterator(); iterator != null && iterator.hasNext();)
	   	           {
	   		    	   Element listaID = (Element)iterator.next();
	   		    	   String errore;
	   	      		   String msg=leggiValoreXML(listaID,"MSG");
	   	      		   
	   	      		   Node er = listaID.selectSingleNode("ERROR"); 
	                   if(er==null)
	   	      		    errore=null;
	                   else
	                   	errore="S";
	             
	                   if(errore==null)
	   		      	     elencoMSG+=" - "+msg+"<br>";
	   		      	   else
	   		      		 elencoMSGError+=" - "+msg+"<br>";
	   	       	   }
	   		       
	   		       if(!elencoMSG.equals("") || !elencoMSGError.equals("") )
	   		       {
	   		    	 message+="Avviso:<br>";
	        		 
	        		 if(!elencoMSG.equals(""))
	        		 { 
	        			 message+="<br>Elenco delle operazioni eseguite correttamente:<br>";
	        			 message+=elencoMSG;
	        		 }
	        		 
	        		 if(!elencoMSGError.equals(""))
	        		 { 
	        			 message+="<br>Elenco delle operazioni non eseguite correttamente:<br>";
	        			 message+=elencoMSGError;
	        		 }
	   		       }
	   		     }
   	         }
             catch ( Exception e ) {
           	   throw e;
             } 
             return message;
   	}  
        
	 private void setMessaggeRedirect(String m)
	 {
		       messageRedirect=m;
	 }
    
   /**
     * Costruzione del tracciato XML di INPUT.
     * 
     * @param checked_listaID 	lista di oggetti
     * @param idCart 			idCartella
     * @param idQuery		 	idQuery
     * @param errMsg		 	messaggio di errore
     * @return String  xml
	 * 
   */
   private String tracciatoXMLInput(String[] checked_listaID,String idCart,String idQuery,String errMsg)
   {
	       Element root,elp,elf,elpp,elj;
		   root = DocumentHelper.createElement("FUNCTION_INPUT");
	       Document dDoc = DocumentHelper.createDocument();
	       dDoc.setRootElement(root);
	
	       /** Dati connessione DB */      
	       elp = DocumentHelper.createElement("CONNESSIONE_DB");
	       elf = aggFiglio(elp,"USER",Parametri.USER);
	       elf = aggFiglio(elp,"PASSWORD",Parametri.PASSWD);
	       elf = aggFiglio(elp,"HOST_STRING",Parametri.SPORTELLO_DSN);
	       root.add(elp);
	     
	       /** Dati connessione Tomcat */
	       elp = DocumentHelper.createElement("CONNESSIONE_TOMCAT");
	       elp = aggFiglio(elp,"UTENTE",user);
	       elp = aggFiglio(elp,"NOMINATIVO",nominativo);
	       elp = aggFiglio(elp,"RUOLO",ruolo);
	       elp = aggFiglio(elp,"MODULO",modulo);
	       elp = aggFiglio(elp,"ISTANZA",istanza);
	       elp = aggFiglio(elp,"PROPERTIES",inifile);
	       elp = aggFiglio(elp,"URL_SERVER",URLserver);
	       elp = aggFiglio(elp,"CONTEXT_PATH",contextPath);
	       root.add(elp);
	     
	       /** Nodo CLIENT_GDM */
	       elp = DocumentHelper.createElement("CLIENT_GDM");
	       if(checked_listaID!=null)
	       {
	    	 elpp = DocumentHelper.createElement("LISTAID");
	         for(int i=0;i<checked_listaID.length;i++)
	          {
	         	 String s=checked_listaID[i].toString();
	    		 elf = aggFiglio(elpp,"ID",s.substring(1,s.length()),s.substring(0,1));
	    	  }
	    	 elp.add(elpp);
	       }
	       else
	    	 elp = aggFiglio(elp,"LISTAID",req.getParameter("listaID"));
	       elp = aggFiglio(elp,"IDCARTPROVENINEZ",idCart);
	       elp = aggFiglio(elp,"TIPOWORKSPACE",req.getParameter("tipoworkspace"));
	       elp = aggFiglio(elp,"IDQUERYPROVENINEZ",idQuery);
	       
	       if(req.getParameter("qs")!=null && !req.getParameter("qs").equals("")){
	    	   String qs = URLDecoder.decode(req.getParameter("qs").toString());
	    	   elp = aggFiglio(elp,"QUERYSTRING",qs);	   
	       }
	       
	       root.add(elp);
	       
	       /** Nodo JSINK */
	       elp = DocumentHelper.createElement("JSYNC");
	       elp = aggFiglio(elp,"AREA",area);
	       elp = aggFiglio(elp,"CONTROLLO",controllo);
	       root.add(elp);
	       
	       /** Nodo DOC */
	       elp = DocumentHelper.createElement("DOC");
	       elp.setText("");
	       root.add(elp);
	             
	       /** Nodo ERROR */
	       elp = DocumentHelper.createElement("ERROR");
	       elp.setText(errMsg);
	       root.add(elp);
	       
	       return dDoc.asXML();
   }
 
   /**
    * Ricerca oggetto con chiave key nel vettore.
    * 
    * @param Vector 	vettore
    * @param key 		chiave
    * @return boolean  
	* 
  */
  private boolean ricercaOggetto(Vector v,String key)
  {
		  if(v!=null)
		  {	   
		    for(int i=0;i<v.size();i++)
		    {
			  if(v.get(i).equals(key))
			   return true;  
		    }
		  }
          return false;
  }
  
  /**
   * Costruzione dell'elenco degli oggetti dal tracciato XML di output.
   * 
   * @param lista 	lista
   * @return String elenco di oggetti  
   * 
  */
  private String getListaID(Element lista) throws Exception
  {
   	      String l="";
    	     
    	  try
 	      {
 	         if(lista!=null)
 	         {	   
 	           for(Iterator iterator = lista.elementIterator(); iterator != null && iterator.hasNext();)
 	           {
 		    	  Element listaID = (Element)iterator.next();
 		    	  String oggetto=leggiValoreXML(listaID,"TIPOOGGETTO")+leggiValoreXML(listaID,"IDOGGETTO");
 	      		  l+=oggetto+"@";
 	           }
 		     }
 	         return l;
 	      }
          catch ( Exception e ) {
        	  log.log_error("CCS_Bottoniera::getListaID()");
        	  throw e;
          } 
   }
  
  /**
   * Costruzione dell'elenco degli oggetti che hanno generato errore durante 
   * l'escuzione dell'operazione e conferma di avvenuta esecuzione dell'operazione
   * per gli oggetti che hanno avuto esito positivo.
   * 
   * @param lista 	lista
   * @return String elenco di oggetti  
   * 
  */
  private void gestioneElencoOperazioni(Element lista,Properties prop) throws Exception
  {
	      try
	      {
	         if(lista!=null)
	         {	   
		       for(Iterator iterator = lista.elementIterator(); iterator != null && iterator.hasNext();)
	           {
		    	   Element listaID = (Element)iterator.next();
		    	   String oggetto=leggiValoreXML(listaID,"TIPOOGGETTO")+leggiValoreXML(listaID,"IDOGGETTO");
	      		   String errore;//=leggiValoreXML(listaID,"ERROR");
	      		   String msg=leggiValoreXML(listaID,"MSG");
	      		   
	      		   Node er = listaID.selectSingleNode("ERROR"); 
                   if(er==null)
	      		    errore=null;
                   else
                	errore="S";
          
                   if(prop.containsKey(oggetto))
       	           {
	      			 ControlliJSINK c=(ControlliJSINK)prop.get(oggetto);
	       	         c.setMSGOggetto(msg);
       	           }
	      		   else
	          	   	throw new Exception("CCS_Bottoniera::gestioneElencoOperazioni() - Oggetto: "+oggetto+" non contenuto nella lista.");           
	          	   	 
	       	       /** Nel caso in cui error non esiste allora l'operazione ha avuto successo
	       	         * Allora occorre effettuare la Conferma di avvenuta esecuzione del 
		      	     * Controllo sull'oggetto */  
		      	   if(errore==null)
		      	   {
		      		   confermaEsecuzioneControllo(oggetto,prop); 
		      	   }
		      	   else
	      		     listaOggettiError.add(oggetto);
	       	   }
		     }
	       }
           catch ( Exception e ) {
        	   throw e;
           } 
	}
      
  /**
   * Completa la connessione.
   * 
   * @return String elenco di oggetti  
   * 
  */ 
  protected String completaConnessione(String connessione)
  {
            String connessioneParam = connessione;
            String pCodice = null;
            String retval = null;
            int h = 0;
            int s = 0;
 
            h = connessioneParam.indexOf(":HOST_DOMINIO");
            if (h > -1) {
               pCodice = connessioneParam.substring(h+1,h+15);
               retval = Parametri.getParametriDomini(pCodice);
               connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
            }
            s = connessioneParam.indexOf(":SID_DOMINIO");
            if (s > -1) {
               pCodice = connessioneParam.substring(s+1,s+14);
               retval = Parametri.getParametriDomini(pCodice);
               connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
            }
            return connessioneParam;
   }
  
  /** Inserimento nodo XML */ 
  private Element aggFiglio(Element elp, String nome, String idOggetto,String tipo)
  {
          Element elf = DocumentHelper.createElement(nome);
          elf = aggFiglio(elf,"TIPOOGGETTO",tipo);
          elf = aggFiglio(elf,"IDOGGETTO",idOggetto);
          elp.add(elf);
          return elp;
  }
  
  /** Inserimento nodo XML */
  private Element aggFiglio(Element elp, String nome, String valore)
  {
          Element elf = DocumentHelper.createElement(nome);
          elf.setText(valore);
          elp.add(elf);
          return elp;
  }
    
  /** Inserimento nodo XML */
  private Element aggFiglio(Element elp, String nome, String valore,Element e)
  {
          Element elf = DocumentHelper.createElement(nome);
          elf.setText(valore);
          elf.add(e);
          elp.add(elf);
          return elp;
  }
  
  /** Lettura elemento XML */
  private static String leggiValoreXML(Document xmlDocument, String tagName)
  {
          String valore = null;
          if(xmlDocument == null)
              System.out.println("xml document null");
          Element root = xmlDocument.getRootElement();
          for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
          {
              Element elemento = (Element)iterator.next();
              if(elemento != null && elemento.getName().equals(tagName))
                  valore = elemento.getText();
              else
                  valore = leggiValoreXML(elemento, tagName);
          }
          
          return valore;
  }

  /** Lettura elemento XML */
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

  /** Lettura elemento XML */
  private static Element leggiElementoXML(Element e, String tagName)
  {
          Element elemento = null, eFound = null;;
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
  
  /** Lettura elemento XML */
  private static Element leggiElementoXML(Document xmlDocument, String tagName)
  {
          Element e = null;
          if(xmlDocument == null)
            System.out.println("xml document null");
          Element root = xmlDocument.getRootElement();
          for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && e == null;)
          {
              Element elemento = (Element)iterator.next();
              if(elemento != null && elemento.getName().equals(tagName))
                  e = elemento;
              else
              	   e = leggiElementoXML(elemento, tagName);
          }
  
          return e;
  }
  
  /** Chiusura dell'oggeto JSYNC */
  private void closeSync(SyncSuite sync) {
	  try {
		  sync.close();
	  } catch (Exception e) {}
  }

}
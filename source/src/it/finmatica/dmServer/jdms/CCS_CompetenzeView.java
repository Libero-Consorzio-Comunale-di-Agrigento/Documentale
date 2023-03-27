package it.finmatica.dmServer.jdms;

import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * Gestione delle Competenze.
 * Classe di servizio per la gestione del Client
*/

public class CCS_CompetenzeView 
{ 
	   /**
	    * Variabili private
	   */	
	   private static String _PATHIMG             ="./images/standard/action/";
	   private static String _TRAFFIC_RED         =_PATHIMG+"trafficlight_red.png"; 
	   private static String _TRAFFIC_GREEN       =_PATHIMG+"trafficlight_green.png"; 
	   private static String _SETROWS			  =_PATHIMG+"grid.png";
	   
	   CCS_Common CCS_common;
	   CCS_HTML h;
	   private Environment vu;  
	   private IDbOperationSQL dbOp;
	   private String sUtente;
	   private String sAutore;
	   private String sOggetto;
	   private String sRuolo;
	   private String sIDViewCartella;
	   private String sTipoOggetto;
	   private String sTipoAbil;
	   private String sAccesso;
	   private String sDal;
	   private String sAl;
	   private String VISUALIZZA;
	   private String Provenienza;
	   private String idCartProvenienza;
	   private String idQueryProvenienza; 
	   private String linksPage; 
	   private String queryString;
	   private HttpServletRequest req;
	   boolean isExitsRecords=false;
	   int nextPage=1;
	   private XSS_Encoder xss=null;
	   
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   
	   /**
		 * Costruttore utilizzato per la gestione delle 
		 * competenze.
		 * 
		 */
	   public CCS_CompetenzeView(String newsUtente,String newsAutore,String newsOggetto,String newsTipoOggetto,String newsTipoAbil,
			   					 String newsAccesso,String newsDal,String newsAl,String newProv,String newRuolo,CCS_Common newCommon) throws Exception
       {
		   	  sUtente=newsUtente;
		   	  sAutore=newsAutore;
		   	  sOggetto=newsOggetto;
		   	  sTipoOggetto=newsTipoOggetto;
		   	  sRuolo=newRuolo;
		   	  sTipoAbil=newsTipoAbil;
		   	  sAccesso=newsAccesso;
		   	  sDal=newsDal;
		   	  sAl=newsAl;
		   	  Provenienza=newProv;
		   	  CCS_common=newCommon;
		      log= new DMServer4j(CCS_CompetenzeView.class,CCS_common); 
       }
	   
	   /**
		 * Costruttore utilizzato per la gestione delle 
		 * competenze senza Ruolo.
		 * 
		 */
	   public CCS_CompetenzeView(String newsUtente,String newsAutore,String newsOggetto,String newsTipoOggetto,String newsTipoAbil,
			   					 String newsAccesso,String newsDal,String newsAl,String newProv,CCS_Common newCommon) throws Exception
      {
		   	  sUtente=newsUtente;
		   	  sAutore=newsAutore;
		   	  sOggetto=newsOggetto;
		   	  sTipoOggetto=newsTipoOggetto;
		   	  sTipoAbil=newsTipoAbil;
		   	  sRuolo="GDM";
		   	  sAccesso=newsAccesso;
		   	  sDal=newsDal;
		   	  sAl=newsAl;
		   	  Provenienza=newProv;
		   	  CCS_common=newCommon;
		   	  log= new DMServer4j(CCS_CompetenzeView.class,CCS_common); 
      }
	   
	   /**
		 * Invocato per la visualizzazione dell'elenco delle competenze dell'oggetto.
		 * 
		 */
	   public CCS_CompetenzeView(HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
		      CCS_common=newCommon;
		      req=newreq;   
		   	  queryString= req.getQueryString();
		   	  sUtente=req.getParameter("s_UTENTE");
			  if(sUtente==null) sUtente=""; 
			  			  
			  xss = new XSS_Encoder(req,CCS_common);
			  
			  queryString=xss.encodeHtmlAttribute("queryString",req.getQueryString());
			  queryString = queryString.replaceAll("&amp;", "&");
			  
			  sOggetto=verificaParametroGet("oggetto",req.getParameter("oggetto"));
			  sTipoOggetto=verificaParametroGet("tipoObj",req.getParameter("tipoObj"));
			  VISUALIZZA=verificaParametroGet("Visualizza",req.getParameter("Visualizza"));
			  Provenienza=verificaParametroGet("Provenienza",req.getParameter("Provenienza"));
			  idCartProvenienza=verificaParametroGet("idCartProveninez",req.getParameter("idCartProveninez"));
			  idQueryProvenienza=verificaParametroGet("idQueryProveninez",req.getParameter("idQueryProveninez"));
			  linksPage=verificaParametroGet("LINKSPage",req.getParameter("LINKSPage"));
			  
			  if(linksPage==null) linksPage="1";
			 
			  h=new CCS_HTML();
			  log= new DMServer4j(CCS_CompetenzeView.class,CCS_common); 
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
  
	   /**
		 * Assegnamento delle competenze all'oggetto dato l'utente.
		 * 
		 * @return boolean	assegnamento delle competenze (true o false)
		 */
	   public boolean _afterExecuteUpdate() throws Exception
	   {
		   	  boolean assegna=false; 
		   	  
		   	  try
		   	  {
				 init();
		         vu.connect();		         		         		         
                 UtenteAbilitazione ua = new UtenteAbilitazione(sUtente,null,null,null,sAutore);								
   			     String sIdOggetto=sOggetto;
   			     
   			     if(sTipoAbil.indexOf(";")==-1)
   			    	sTipoAbil+=";"; 	  
   			     
		         Abilitazioni ab = new Abilitazioni(sTipoOggetto,sIdOggetto,sTipoAbil,"0",sAccesso,sDal,sAl);						
                 
		         if(sRuolo!="")
		          ua.setRuolo(sRuolo);
                 
		         if (!Provenienza.equals("W") && !sTipoOggetto.equals("DOCUMENTI"))
		         {
		           (new GDM_Competenze(vu)).assegnaCompentenzaCartellaOrQuery(ua,ab); 
		           assegna=true;
				 }
		         else
		         {
		        	(new GDM_Competenze(vu)).assegnaCompentenza(ua,ab);
		            assegna=true;
		         }
		   	  }
			  catch (Exception e) 
		      {
		         try{vu.disconnectClose();}catch(Exception ei){}
		         CCS_common.closeConnection(dbOp,false);
		         throw e;
		      }
			  finally {
				  try{vu.disconnectClose();}catch(Exception ei){}

				  /** Se è stato effettuato l'assegnamento delle competenze effettuo il Commit */
				  if(assegna)
					  CCS_common.closeConnection(dbOp,true);
				  else
					  CCS_common.closeConnection(dbOp,false);
			  }
		      return assegna;
	   }
	   
	   /**
		 * Visualizzazione dell'elenco delle competenze dell'oggetto.
		 * 
		 * @return String	grid della form delle competenze
		 */
	   public String gridGDMCompetenze() throws Exception
	   {
		   	  String grid="";
		   	  try
		   	  {
				  init();
				  grid=buildGrid();
		   	  }
		   	  catch (Exception e) 
		   	  {
		   		 throw e;
		   	  }
			  finally {
		   	  	_finally();
			  }
		      return grid;
	   }
	   
	   /**
		 * Controllo del parametro delle competenze per utilizzare la pagina delle
		 * comptenze SO4 o quella AD4.
		 * 
		 * @return String	url della pagina delle competenze
		 */
	   public String getURLParametroCompetenze() throws Exception
	   {
		   	  String url="";
		   	  try
		   	  {
				 init();
		   		 if(vu.Global.PARAM_COMPETENZE.equals("SO4"))
		   			url="T_Competenze_New_SO4.do?"+queryString;
		   		    //url="T_Competenze_New_SO4.do?"+req.getQueryString();
		   	  }
		   	  catch (Exception e) 
		   	  {
		   		 throw e;
		   	  }
			  finally {
				  _finally();
			  }
		      return url;
	   }
	   
	   /***************************************************************************
		* METODI PRIVATI
		**************************************************************************/

	   private void init() throws Exception {
		   dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
	   }

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
		 * Costruzione della grid.
		 * 
		 * @return String	HTML grid della form delle competenze
		 */
	   private String buildGrid() throws Exception
	   {
		   	   String sql="";
		   	   String colspan="5";
	      
		   	   /** HEADER */
	   	       String head="Utente@Lettura@Modifica@Elimina@Gest. Comp.";
		   	   
		   	   //Nel caso di Cartelle vine visualizzata l'abilitazione di Inserimento
		   	   if(sTipoOggetto.equals("C"))
		   	   {
		   		   head+="@Inserimento";
		   		   colspan="6";
		   	   }
		   	   //Nel caso di Documento viene visualizzata le abilitazioni delle competenze sugli allegati
		   	   if(sTipoOggetto.equals("D"))
		   	   {
		   		   head+="@Lettura Allegati@Modifica Allegati@Cancellazione Allegati";
		   		   colspan="8";
		   	   }
		   	   
		   	   String content=h.getHeader(head,"AFCColumnTD")+"\n";
	      
		   	   /** Impostazione dell'URL_PAGE*/
		   	   String queryURL="";
			   //if(req.getQueryString().indexOf("LINKSPage")!=-1)
			   if(queryString.indexOf("LINKSPage")!=-1)
			   { 
			   	  //String[] v=req.getQueryString().split("&");
			      String[] v=queryString.split("&");
			   	  for(int i=0;i<v.length;i++)
			      {
			      	 if(v[i].indexOf("LINKSPage")==-1)
			      		queryURL+=v[i]+"&";
			      }
			      queryURL=queryURL.substring(0,queryURL.length()-1);
			   }
			   else 
			   	queryURL=queryString;	  
			   //queryURL=req.getQueryString();	  
			   String url_page="Competenze_New.do?"+queryURL; 
	    	  
			   if((sTipoOggetto.equals("W")) || (sTipoOggetto.equals("C")))
				 sIDViewCartella=getIDViewCartella(sOggetto);
			   
			   /** Assegnazione della query */	  
			   sql=oggettoCompetenzeSQL();
          
			   try
			   {    
				  String records=this.getRecordsRows(sql);
    
				  if (isExitsRecords)
				  {
					  content+=records;
					  content+=h.getNavigator(colspan,url_page,Integer.parseInt(linksPage),nextPage,10)+"\n";
				  }
				  else
				  {
					  content+=h.getNORecords(colspan)+"\n";
					  content+=h.getNavigator(colspan,0,0,0,0,1,1)+"\n";
				  }
			   }
			   catch (Exception e) {           
				  throw e;
				  //throw new Exception("CCS_Competenze::buildGrid\n" + e.getMessage());           
			   }
			   return h.getGrid(content);
	   }
	   
	   /**
		 * Costruzione delle righe della grid.
		 * 
		 * @return String	righe
		 */
	   private String getRecordsRows(String sql) throws Exception
	   { 
		   	   String result="";
		   	   int pageSize=6;
		   	   int nPages=Integer.parseInt(linksPage);
		   	   int startPos=((nPages-1)*pageSize+1);
               ResultSet rs = null;
               try
               {
            	 dbOp.setStatement(sql);
            	 dbOp.execute();
            	 rs = dbOp.getRstSet();
            	 if ( pageSize > 0 ) 
            	   result = getRows(rs, startPos, pageSize );
               }
               catch ( SQLException e ) {
            	   throw e;
            	   //throw new Exception("CCS_Competenze::getRecordsRows\n" + e.getMessage());           
               } 
               return result;
	   }
	   
	   /**
		 * Costruzione delle righe della grid.
		 * 
		 * @return String	righe
		 */
	   private String getRows(ResultSet rs, int start, int max) throws Exception
	   {
		   	   String rows="";
		   	   int count=1;
		   	   int iCounter = 0;
		   	   
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
		   			   rows+=this.getRiga(rs,iCounter);
		   			   isExitsRecords = true;        
		   		   }
            	  if(iCounter>max)
		   			nextPage=Integer.parseInt(linksPage)+1;
		   		  else
		   			nextPage=Integer.parseInt(linksPage); 
		   		 }
		   	   }
		   	   catch ( SQLException e ) {
		   		  throw e;
                  //throw new Exception("CCS_Competenze::getRows\n" + e.getMessage());           
		   	   }
		   	   return rows;
	   } 
	   
	   /**
		 * Costruzione delle singola riga.
		 * 
		 * @return String	HTML riga
		 */
	   private String getRiga(ResultSet rs,int count) throws Exception 
	   {
		   	   String record="";
		   	   if (count%2==0)
		   	   {	 
		   		   String rec=h.getRecord(rs.getString("UTENTE"))+
		   		   			  h.getRecord("LETTURA",rs.getString("LETTURA"),rs.getString("LETTURA_LINK"),rs.getString("LETTURA_LIST_COMPETENZE"))+
		   		   			  h.getRecord("MODIFICA",rs.getString("MODIFICA"),rs.getString("MODIFICA_LINK"),rs.getString("MODIFICA_LIST_COMPETENZE"))+
		   		   			  h.getRecord("CANCELLAZIONE",rs.getString("CANCELLAZIONE"),rs.getString("DELETE_LINK"),rs.getString("DELETE_LIST_COMPETENZE"))+
		   		   			  h.getRecord("GESTIONE",rs.getString("GESTIONE"),rs.getString("GESTIONE_LINK"),rs.getString("GESTIONE_LIST_COMPETENZE"));
	       
		   		   if(sTipoOggetto.equals("C"))	       
		   			 rec+=h.getRecord("INSERIMENTO",rs.getString("INSERIMENTO"),rs.getString("INSERT_LINK"),rs.getString("INSERT_LIST_COMPETENZE"));
		   		   
		   		   if(sTipoOggetto.equals("D")){	       
		   			 rec+=h.getRecord("LETTURA_ALLEGATI",rs.getString("LETTURA_ALLEGATI"),rs.getString("LETTURA_ALLEGATI_LINK"),rs.getString("LA_LIST_COMPETENZE"));
		   			 rec+=h.getRecord("MODIFICA_ALLEGATI",rs.getString("MODIFICA_ALLEGATI"),rs.getString("MODIFICA_ALLEGATI_LINK"),rs.getString("UA_LIST_COMPETENZE"));
		   			 rec+=h.getRecord("CANCELLAZIONE_ALLEGATI",rs.getString("CANCELLAZIONE_ALLEGATI"),rs.getString("CANCELLAZIONE_ALLEGATI_LINK"),rs.getString("DA_LIST_COMPETENZE"));
		   		   }
		   		   record=h.getRecords(rec);
		   	   }
		   	   else
		   	   {	 
		   		   String rec=h.getAltRecord(rs.getString("UTENTE"))+
		   		   			  h.getAltRecord("Alt_LETTURA",rs.getString("LETTURA"),rs.getString("LETTURA_LINK"),rs.getString("LETTURA_LIST_COMPETENZE"))+
		   		   			  h.getAltRecord("Alt_MODIFICA",rs.getString("MODIFICA"),rs.getString("MODIFICA_LINK"),rs.getString("MODIFICA_LIST_COMPETENZE"))+
		   		   			  h.getAltRecord("Alt_CANCELLAZIONE",rs.getString("CANCELLAZIONE"),rs.getString("DELETE_LINK"),rs.getString("DELETE_LIST_COMPETENZE"))+
		   		   			  h.getAltRecord("Alt_GESTIONE",rs.getString("GESTIONE"),rs.getString("GESTIONE_LINK"),rs.getString("GESTIONE_LIST_COMPETENZE"));
             
		   		   if(sTipoOggetto.equals("C"))	
		   			 rec+=h.getAltRecord("Alt_INSERIMENTO",rs.getString("INSERIMENTO"),rs.getString("INSERT_LINK"),rs.getString("INSERT_LIST_COMPETENZE"));
                  
		   		   if(sTipoOggetto.equals("D"))	{
		   			 rec+=h.getAltRecord("Alt_LETTURA_ALLEGATI",rs.getString("LETTURA_ALLEGATI"),rs.getString("LETTURA_ALLEGATI_LINK"),rs.getString("LA_LIST_COMPETENZE"));
		   			 rec+=h.getAltRecord("Alt_MODIFICA_ALLEGATI",rs.getString("MODIFICA_ALLEGATI"),rs.getString("MODIFICA_ALLEGATI_LINK"),rs.getString("UA_LIST_COMPETENZE"));
		   			 rec+=h.getAltRecord("Alt_CANCELLAZIONE_ALLEGATI",rs.getString("CANCELLAZIONE_ALLEGATI"),rs.getString("CANCELLAZIONE_ALLEGATI_LINK"),rs.getString("DA_LIST_COMPETENZE"));
		   		   } 
		   		   record=h.getAltRecords(rec);
		   	   }
		   	   return record+"\n";        
	   }  
	   
	   /**
		 * Recupero dell'identificativo del VIEW_CARTELLA.
		 * 
		 * @param  id		id della Cartella 
		 * @return String	id view_cartella
		 */
	   private String getIDViewCartella(String id) throws Exception
	   { 
		       String sql="select f_idview_cartella (TO_NUMBER (:IDCARTELLA)) idview from dual";
		       String result="";
		       ResultSet rs = null;
		       
		       try 
		       {
		           dbOp.setStatement(sql);
		           dbOp.setParameter(":IDCARTELLA",id);
		           dbOp.execute();
		           rs = dbOp.getRstSet();
		           if(rs.next()) 
		             result=rs.getString("idview");
		       }
		       catch ( SQLException e ) {
		    	   throw e;
		    	   //throw new Exception("CCS_Competenze::getIDViewCartella\n" + e.getMessage());           
		       } 
		       return result;
	   }
	   
	   /**
		 * Select della grid delle competenze.
		 * 
		 * @return String	select
		 */
	   private String oggettoCompetenzeSQL() throws Exception
	   {
		       StringBuffer sql=new StringBuffer();
		       String tipoOggetto;
		       if(sTipoOggetto.equals("W"))
		    	tipoOggetto="C";
		       else
		    	tipoOggetto=sTipoOggetto;  
		          
		       sql.append("select ");
		       sql.append("'<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=TUTTI&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&idQueryProveninez="+idQueryProvenienza+"&LINKSPage="+linksPage+"&user='||us||'''\"  href=\"#\">'||ad4_utenti.nominativo||'</a>' UTENTE, ");
		       sql.append("lettura,modifica,cancellazione,gestione,inserimento,lettura_allegati,modifica_allegati,cancellazione_allegati,");
		       sql.append("lettura_link,modifica_link,delete_link,gestione_link,insert_link,lettura_allegati_link,modifica_allegati_link,cancellazione_allegati_link, ");
		       sql.append("letrecords,modrecords,canrecords,manrecords,crerecords,larecords,uarecords,darecords, ");
		       sql.append("lettura_list_competenze,modifica_list_competenze,delete_list_competenze,gestione_list_competenze,insert_list_competenze, ");
		       sql.append("la_list_competenze, ua_list_competenze, da_list_competenze ");
		       sql.append("from ( ");
		       sql.append("select distinct ");
		       sql.append("comp.utente us, ");
		       sql.append("GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'L',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')) LETTURA, ");
		       sql.append("GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'U',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')) MODIFICA, ");
		       sql.append("GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'D',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')) CANCELLAZIONE, ");
		       sql.append("GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'M',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')) GESTIONE, ");
		       sql.append("DECODE('"+tipoOggetto+"','C',GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',to_char('"+sIDViewCartella+"'),'C',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),'') INSERIMENTO, ");
		       sql.append("DECODE('"+tipoOggetto+"','D',GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI','"+sOggetto+"','LA',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),'') LETTURA_ALLEGATI, ");
		       sql.append("DECODE('"+tipoOggetto+"','D',GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI','"+sOggetto+"','UA',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),'') MODIFICA_ALLEGATI, ");
		       sql.append("DECODE('"+tipoOggetto+"','D',GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI','"+sOggetto+"','DA',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),'') CANCELLAZIONE_ALLEGATI, ");
		       sql.append("DECODE(GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'L',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">') LETTURA_LINK, ");
		       sql.append("DECODE(GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'U',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">') MODIFICA_LINK, ");
		       sql.append("DECODE(GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'D',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">') DELETE_LINK, ");
		       sql.append("DECODE(GDM_COMPETENZA.GDM_VERIFICA(DECODE('"+tipoOggetto+"','D','DOCUMENTI','C','VIEW_CARTELLA','Q','QUERY'),decode('"+sTipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"'),'M',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">') GESTIONE_LINK, ");
		       sql.append("DECODE('"+tipoOggetto+"','C',DECODE(GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',to_char('"+sIDViewCartella+"'),'C',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">'),'') INSERT_LINK,");
		       sql.append("DECODE('"+tipoOggetto+"','D',DECODE(GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI','"+sOggetto+"','LA',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">'),'') LETTURA_ALLEGATI_LINK,");
		       sql.append("DECODE('"+tipoOggetto+"','D',DECODE(GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI','"+sOggetto+"','UA',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">'),'') MODIFICA_ALLEGATI_LINK,");
		       sql.append("DECODE('"+tipoOggetto+"','D',DECODE(GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI','"+sOggetto+"','DA',comp.utente,F_TRASLA_RUOLO(comp.utente,'GDMWEB','GDMWEB'),TO_CHAR(sysdate,'dd/mm/yyyy')),0,'<img src=\""+_TRAFFIC_RED+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">','<img src=\""+_TRAFFIC_GREEN+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\">'),'') CANCELLAZIONE_ALLEGATI_LINK,");
		       sql.append("LETTURA.LETrecords, MODIFICA.MODrecords, CANCELLAZIONE.CANrecords, MANAGE.MANrecords, CREAZIONE.CRErecords, LETTURA_ALLEGATI.LArecords, MODIFICA_ALLEGATI.UArecords, CANCELLAZIONE_ALLEGATI.DArecords, ");
		       sql.append("DECODE(nvl(LETTURA.LETrecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=LETTURA&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\" href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>') LETTURA_LIST_COMPETENZE,");
		       sql.append("DECODE(nvl(MODIFICA.MODrecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=MODIFICA&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\" href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>') MODIFICA_LIST_COMPETENZE,");
		       sql.append("DECODE(nvl(CANCELLAZIONE.CANrecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=CANCELLAZIONE&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\" href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>') DELETE_LIST_COMPETENZE,");
		       sql.append("DECODE(nvl(MANAGE.MANrecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=MANAGE&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\" href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>') GESTIONE_LIST_COMPETENZE,");
		       sql.append("DECODE('"+tipoOggetto+"','C',DECODE(nvl(CREAZIONE.CRErecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=CREAZIONE&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\"  href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>'),'') INSERT_LIST_COMPETENZE, ");
		       sql.append("DECODE('"+tipoOggetto+"','D',DECODE(nvl(LETTURA_ALLEGATI.LArecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=LETTURA_ALLEGATI&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\"  href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>'),'') LA_LIST_COMPETENZE, ");
		       sql.append("DECODE('"+tipoOggetto+"','D',DECODE(nvl(MODIFICA_ALLEGATI.UArecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=MODIFICA_ALLEGATI&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\"  href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>'),'') UA_LIST_COMPETENZE, ");
		       sql.append("DECODE('"+tipoOggetto+"','D',DECODE(nvl(CANCELLAZIONE_ALLEGATI.DArecords,0),0,'','<a style=\"cursor:hand\" onclick=\"this.href=''Competenze_New.do?oggetto="+sOggetto+"&tipoObj="+sTipoOggetto+"&Abilitazione=CANCELLAZIONE_ALLEGATI&Visualizza="+VISUALIZZA+"&Provenienza="+Provenienza+"&idCartProveninez="+idCartProvenienza+"&LINKSPage="+linksPage+"&idQueryProveninez="+idQueryProvenienza+"&user='||comp.utente||'''\"  href=\"#\"><img src=\""+_SETROWS+"\" border=\"0\" onload=\"fixPNG(this,''16'',''16'')\"></a>'),'') DA_LIST_COMPETENZE ");
		       sql.append("from SI4_COMPETENZE comp,");
		       sql.append(" ("+abilitazioneSQL("LETTURA","LET","L",sOggetto,tipoOggetto)+") LETTURA, ");
		       sql.append(" ("+abilitazioneSQL("MODIFICA","MOD","U",sOggetto,tipoOggetto)+") MODIFICA, ");
		       sql.append(" ("+abilitazioneSQL("CANCELLAZIONE","CAN","D",sOggetto,tipoOggetto)+") CANCELLAZIONE, ");
		       sql.append(" ("+abilitazioneSQL("MANAGE","MAN","M",sOggetto,tipoOggetto)+") MANAGE, ");
		       sql.append(" ("+abilitazioneSQL("CREAZIONE","CRE","C",sOggetto,tipoOggetto)+") CREAZIONE, ");
		       sql.append(" ("+abilitazioneSQL("LETTURA_ALLEGATI","LA","LA",sOggetto,tipoOggetto)+") LETTURA_ALLEGATI, ");
		       sql.append(" ("+abilitazioneSQL("MODIFICA_ALLEGATI","UA","UA",sOggetto,tipoOggetto)+") MODIFICA_ALLEGATI, ");
		       sql.append(" ("+abilitazioneSQL("CANCELLAZIONE_ALLEGATI","DA","DA",sOggetto,tipoOggetto)+") CANCELLAZIONE_ALLEGATI ");
		       sql.append(" where  decode('"+tipoOggetto+"','C',to_char('"+sIDViewCartella+"'),'"+sOggetto+"')=oggetto ");
		       sql.append(" AND comp.tipo_competenza = 'U'");
		       sql.append(" and comp.utente=LETTURA.utente (+) ");
		       sql.append(" and comp.utente=MODIFICA.utente (+) ");
		       sql.append(" and comp.utente=CANCELLAZIONE.utente (+) ");
		       sql.append(" and comp.utente=MANAGE.utente (+) ");
		       sql.append(" and comp.utente=CREAZIONE.utente (+) ");
		       sql.append(" and comp.utente=LETTURA_ALLEGATI.utente (+) ");
		       sql.append(" and comp.utente=MODIFICA_ALLEGATI.utente (+) ");
		       sql.append(" and comp.utente=CANCELLAZIONE_ALLEGATI.utente (+) ");
		       sql.append(" and ( nvl(LETTURA.LETrecords,0)<>0 or nvl(MODIFICA.MODrecords,0)<>0 or nvl(CANCELLAZIONE.CANrecords,0)<>0 or nvl(MANAGE.MANrecords,0)<>0 or nvl(CREAZIONE.CRErecords,0)<>0 or nvl(LETTURA_ALLEGATI.LArecords,0)<>0 or nvl(MODIFICA_ALLEGATI.UArecords,0)<>0 or nvl(CANCELLAZIONE_ALLEGATI.DArecords,0)<>0 )");
		 	   sql.append(" ), ad4_utenti");
		 	   sql.append(" where ad4_utenti.utente=us ");
/*ex upper*/   sql.append(" and ad4_utenti.nominativo like '%'||nvl('"+sUtente+"','')||'%' " );
		 	   sql.append(" order by 1");  
		 	   
		 	   System.out.println("SQL_Competenze="+sql);
		 	   return sql.toString();
		}
 
		private String oggettoSQL(String abilitazione,String oggetto,String tipoObj)
		{
			    StringBuffer sql=new StringBuffer();
			    sql.append(" select ID_COMPETENZA,utente ");
			    sql.append(" from SI4_COMPETENZE comp, ");
			    sql.append(" 	  SI4_ABILITAZIONI abil,");
			    sql.append("	  SI4_TIPI_ABILITAZIONE tip_abi,");
			    sql.append("	  SI4_TIPI_OGGETTO tip_obj");
			    sql.append(" where comp.id_abilitazione=abil.id_abilitazione");
			    sql.append(" and tip_abi.ID_TIPO_ABILITAZIONE=abil.ID_TIPO_ABILITAZIONE");
			    sql.append(" and tip_obj.ID_TIPO_OGGETTO=abil.ID_TIPO_OGGETTO");
			    sql.append(" and oggetto = DECODE('"+tipoObj.toUpperCase()+"','C','"+sIDViewCartella+"','"+oggetto+"')");
			    sql.append(" and abil.ID_TIPO_OGGETTO = DECODE('"+tipoObj.toUpperCase()+"','D',32,'C',33,'Q',34)");
			    sql.append(" and tip_abi.tipo_abilitazione = '"+abilitazione+"' and tipo_competenza in ('F','U') ");
			    return sql.toString();
		 }
 
		 private String abilitazioneSQL(String descrizione,String descrizione_records,String abilitazione,String oggetto,String tipoObj)
		 {
			     StringBuffer sql=new StringBuffer();
			     sql.append("select count(*) "+descrizione_records+"records, utente utente ");
			     sql.append("from ( ");
			     sql.append(oggettoSQL(abilitazione,oggetto,tipoObj));
			     sql.append(" ) group by utente");
			     return sql.toString();
		 }
 
}
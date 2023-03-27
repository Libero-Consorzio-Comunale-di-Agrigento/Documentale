package it.finmatica.dmServer.jdms;

import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Vector;

 
/**
 * Gestione delle WorkSpace.
 * Classe di servizio per la gestione del Client
*/

public class CCS_WorkSpace {		
	   /**
	    * Costante per tipi formato
	   */ 
	   public final static int TYPE_COMBO=1;
	   public final static int TYPE_TABLE=2;
	  
	   /**
	    * Costante per ruolo Amministratore
	   */
	   private final static String RUOLO_AMM="AMM";
	   
	   /**
	    * Costante per wrksp di default
	   */
	   private final static String WRKSP_DEFAULT="WRKSP_DEFAULT";
	   
	   /**
	    * Variabili Immagini
	   */
	   private static String _PATHIMG             ="images/standard/action/";
	   private static String _PATHIMG_THEMES_AFC  ="../Themes/AFC/";
	   private static String _PATHIMG_AMV 		  ="images/AMV/";
	   private static String _VUOTO               =_PATHIMG+"vuota.png";
	   private static String _CARTELLAGDC         =_PATHIMG+"folder.png";
	   private static String _ANNULLA             =_PATHIMG+"delete.png"; 
	   private static String _EDITFOLDER          =_PATHIMG+"editFolder.png";//   
	   private static String _COMP                =_PATHIMG+"comp.png";
	   private static String _VUOTO_THEMES        =_PATHIMG_THEMES_AFC+"Vuoto.gif";
	   private static String _FIRST_THEMES        =_PATHIMG_THEMES_AFC+"FirstOn.gif";
	   private static String _PREV_THEMES         =_PATHIMG_THEMES_AFC+"PrevOn.gif";
	   private static String _NEXT_THEMES         =_PATHIMG_THEMES_AFC+"NextOn.gif";
	   private static String _LAST_THEMES         =_PATHIMG_THEMES_AFC+"LastOn.gif";
	   private static String _MANUALI        	  =_PATHIMG+"help.png";
	   private static String _ADD          		  =_PATHIMG+"add.png";
	   private static String _ADD_DS              =_PATHIMG+"add_ds.gif";
	   
	   
	   /**
	    * Variabile Utente
	   */	   
   	   private String user;
   	   
	   /**
	    * Variabile Ruolo
	   */	      	   
	   private String ruolo;
	   
	   /**
	    * Variabile Modulo
	   */	      	   
	   private String modulo;
	
	   /**
	    * Variabili di connessione
	   */
	   private CCS_Common CCS_common;
	   
	   /**
	    * Variabile di HTML
	   */
	   CCS_HTML h;
	   
	   /**
	    * Variabile di lista WorkSpace
	   */
	   Vector vLista=null;
	   
	   /**
	    * Variabile di request per la costruzione dell'URL navigatore
	   */
	   HttpServletRequest  req=null;
	   
	   /**
	    * SRC del link diretto solo alla pagina WorkArea
	   */
	   String  srcPage="";
	   
	   private XSS_Encoder xss=null;
	   
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;

	   private IDbOperationSQL dbOp;
	   private Environment vu;

	   public CCS_WorkSpace(String user, String ruolo, CCS_Common common) throws Exception
	   {
		      this.user=user;
		      this.ruolo=ruolo;
		      init(common);
		      this.h = new CCS_HTML();
	   }
	   
	   public CCS_WorkSpace(String user, String ruolo, String modulo, CCS_Common common) throws Exception
	   {
		      this.user=user;
		      this.ruolo=ruolo;
		      this.modulo=modulo;
		      init(common);
	   }
	  	   
	   public CCS_WorkSpace(String user, String ruolo,HttpServletRequest newreq, CCS_Common common) throws Exception 
	   {
		      this.user=user;
		      this.ruolo=ruolo;
		      init(common);
		      this.req=newreq;
		      this.h = new CCS_HTML();
		      xss = new XSS_Encoder(req,CCS_common);
	   }

	   private void init(CCS_Common newCommon)  throws Exception
	   {
			CCS_common=newCommon;
			log= new DMServer4j(CCS_WorkSpace.class,CCS_common);
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

	    private void _finally(boolean commit) throws Exception {
			try
			{
				if (CCS_common.dataSource.equals("")){
					try{vu.disconnectClose();}catch(Exception ei){}
				}
				CCS_common.closeConnection(dbOp,commit);
			}
			catch (Exception e) {
				throw e;
			}
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
	    * Recupera un parametro dalla tabella REGISTRO.
	    * 
	    * @param chiave    specifica il modulo dell'applicativo
	    * @param stringa   il nome del parametro
	   */	
       public String getParametroFromRegistro(String chiave,String stringa) throws Exception
       {
    	   	  String parametro ="";
    	   	  String sql="";
    	   	  
    	   	  try 
		      {
    	    	dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		        sql="SELECT VALORE FROM REGISTRO WHERE CHIAVE = '"+chiave+"' AND STRINGA = '"+stringa+"'";
		        dbOp.setStatement(sql);
		        dbOp.execute();		     
		        ResultSet rst = dbOp.getRstSet();		      
		        if (rst.next())
		         parametro = rst.getString("valore");
		      }
		      catch (Exception e) {
		    	  log.log_error("CCS_Workspace::getParametroFromRegistro() - Impossibile recuperare il parametro dalla tabella \n");
			      log.log_error("regitro - SQL:"+sql);
				  throw e;
		      }
    	   	  finally {
    	   	  	_finally();
			  }
		      return parametro;
       }
       
	   
	   /**
	    * Creazione della workspace con nome passato in input.
	    * Questo metodo può essere richiamato solo se il ruolo
	    * dell'utente è "Amministratore".
	    * Il metodo, prima di crearla, controlla se non esiste
	    * una wrksp con lo stesso nome
	    * 
	    * @param wrkspName     Nome della workspace da creare
	    * @param area          Area del profilo della workspace
	    * @param codiceModello Codice Modello del profilo della workspace
	   */	   
	   public void insert(String wrkspName, String area, String codiceModello,String nameProfileFolder,String IdentifierFolder) throws Exception {
		      boolean commit=false;
	   	      if (!ruolo.equals(RUOLO_AMM))
		    	  throw new Exception("CCS_WorkSpace::insert\n"+
		    			  			  "Impossibile creare la workspace "+wrkspName+",\n"+
		    			  			  "L'utente "+user+" non è Amministratore");
		      
		      /* Controllo se il nome della workspace esiste già */
		      /*Vector vElencoWrksp = wrkspList(false);
		      for(int i=0;i<vElencoWrksp.size();i++)
		    	  if (((WrkspInformation)vElencoWrksp.get(i)).getNomeWrksp().equals(wrkspName))
		    		  throw new Exception("CCS_WorkSpace::insert() - Impossibile creare la workspace "+wrkspName+"\n.Esiste già!");
		      */		          

		      try {
			      ICartella Ic = new ICartella(area,codiceModello,"","",wrkspName);			      			      
	              Environment vu = new Environment(user, null,null,null, null,dbOp.getConn(),false);	              
	              Ic.initVarEnv(vu);
	              Ic.setIdentifierFolder("-"+IdentifierFolder);
	              Ic.setProfileFolder(nameProfileFolder);  
	              Ic.insert();
	                                        
	              commit=true;
		      }
		      catch (Exception e) {
		    	  commit=false;
		    	  log.log_error("CCS_WorkSpace::insert() - Area:"+area+" - CM:"+codiceModello+" - IdentifierFolder:-"+IdentifierFolder);
		    	  throw e;
		      }
			  finally {
				  _finally(commit);
			  }
	   }

	   /**
	    * Elimina la wrksp con id passato in input.
	    * Viene controllata la presenza di oggetti
	    * (cartelle) all'interno della workspace da
	    * cancellare. Se ce ne sono non si procede
	    * all'eliminazione
	    * 
	    * @param  idWrkSp WorkSpace da eliminare
	   */
       public void delete(String idWrkSp) throws Exception
       {
		      boolean commit= false;

		     try
		     {
				  //Controllo esistenza di oggetti dentro la Workspace
				  String sql="SELECT 'X' FROM LINKS WHERE ID_CARTELLA = :IDWRKSP AND TIPO_OGGETTO='C'";
				  dbOp.setStatement(sql);
				  dbOp.setParameter(":IDWRKSP",idWrkSp);
				  dbOp.execute();
				  ResultSet rst = dbOp.getRstSet();
				  if (rst.next())
				  {
					  commit =false;
					  log.log_error("CCS_Workspace::delete() - Impossibile cancellare Area di lavoro! Esistono elementi all'interno. - idWrkSp:"+idWrkSp+" - SQL:"+sql);
					  throw new Exception("Impossibile cancellare Area di lavoro! Esistono elementi all'interno.");
				  }

			      ICartella Ic = new ICartella(idWrkSp);			      			      
	              Environment vu = new Environment(user, null,null,null, null,dbOp.getConn(),false);	              
	              Ic.initVarEnv(vu);
	              Ic.delete();
	              deleteManuale(idWrkSp);
	              commit=true;
		      }
		      catch (Exception e) {
		    	 commit = false;
		    	 log.log_error("CCS_WorkSpace::delete() - idWrkSp:"+idWrkSp);
		    	 throw e;
		      }
		      finally {
		     	_finally(commit);
			  }
       }

       /**
	    * Setta la workSpace di preferenza per l'utente.
	    * 
	    */	   
	   private void deleteManuale(String wrksp) throws Exception 
	   {
		      String sql="";
		      try {
			      sql=setSQLDeleteManuale(wrksp);
			      dbOp.setStatement(sql);
			      dbOp.execute();
		      }		      
			  catch (Exception e) {
				  log.log_error("CCS_WorkSpace::deleteManuale() - SQL:"+sql);
			      throw e;
			 }
		}
 	   
	   /**
	    * Restituisce la lista delle workspace abilitate 
	    * all'utente in formato "formato" mettendo (se combo)
	    * la wrksppref in cima alla lista (se wrksppref non è nullo)
	    * 
	    * @param  formato   1=COMBO (VARIABILE TYPE_COMBO),
	    * 					2=TABLE (VARIABILE TYPE_TABLE)
	    * @param  wrksppref Workspace di preferenza da mostrare per prima
	    *                   nella tendina (se formato=table il parametro non serve)
	    *                   Se null prendo la prima in lista              
	    * @return Stringa HTML che rappresenta la combo o la table 
	   */	
	   public String wrkspHTMLList(int formato, String wrksppref,boolean tipoOperazione) throws Exception {
		      String sHTMLRet="";
		      		      
		      vLista=wrkspList(true);
		      
		      //Gestione della COMBO		      
		      if (formato==TYPE_COMBO) {
		    	  sHTMLRet=wrkspHTMLListC(wrksppref,tipoOperazione);
		      }
		      //Gestione della TABLE
		      else
		      {
		    	  sHTMLRet=wrkspHTMLListT(wrksppref);
		      }
		      return sHTMLRet.toString();
	   }
	   
	   /**
	    * Restituisce la lista delle workspace abilitate 
	    * all'utente in formato "formato" mettendo (se combo)
	    * la wrksppref in cima alla lista (se wrksppref non è nullo)
	    * 
	    * @param  wrksppref 	  Workspace di preferenza da mostrare per prima nella tendina (se formato=table 
	    * 						  il parametro non serve). Se null prendo la prima in lista     
	    *         tipoOperazione  indica se includere la voce "Gestisci..." anche in base al ruolo dell'utente        
	    * @return Stringa HTML che rappresenta la combo  
	   */	
	   private String wrkspHTMLListC(String wrksppref,boolean tipoOperazione) throws Exception 
	   {
		      StringBuffer sHTMLRet= new StringBuffer("");
		      sHTMLRet.append("<select id=\"WorkspaceList\" class=\"AFCSelect\" name=\"WorkspaceList\" onchange=\"setWorkspace();\" >");
		      
		      if (ruolo.equals(RUOLO_AMM) && tipoOperazione) 
		        sHTMLRet.append("<option value=\"0\">Gestisci...</option>");
		      
		      for(int i=0;i<vLista.size();i++) 
		      {
                 String nome, id;
		    	
                 WrkspInformation wrkspInfo = (WrkspInformation)vLista.get(i);
		    	     
		    	 nome= wrkspInfo.getNomeWrksp();
		    	 id =  wrkspInfo.getidWrksp();
		    	 
		    	 
		    	 String sSelected="";
		    	    	 
    	    	 if (wrksppref==null)
    	    		 if (i==0) sSelected="selected";
    	    		 else sSelected="";
    	    	 else {
    	    		 if (wrksppref.equals(id)) sSelected="selected";
    	    		 else sSelected="";	    	    		 
    	    	 }
		    	    	 
		    	 sHTMLRet.append("<option "+sSelected+" value=\""+id+"\">"+nome+"</option>");
		      }
		      
		      sHTMLRet.append("</select>");
   		      
		      return sHTMLRet.toString();
	   }
	   
	   /**
	    * Restituisce la lista delle workspace abilitate 
	    * all'utente in formato "formato" mettendo (se combo)
	    * la wrksppref in cima alla lista (se wrksppref non è nullo)
	    * 
	    * @param  wrksppref Workspace di preferenza da mostrare per prima
	    *                   nella tendina (se formato=table il parametro non serve)
	    *                   Se null prendo la prima in lista              
	    * @return Stringa HTML che rappresenta la table 
	   */	
	   private String wrkspHTMLListT(String wrksppref) throws Exception 
	   {
		      StringBuffer sHTMLRet= new StringBuffer("");
		      int pageSize=10;
		      int nPages=1;
		      String linkPage=null;
		      if(req!=null)
	    	    linkPage=verificaParametroGet("LINKSPage",req.getParameter("LINKSPage"));
		      if(linkPage!=null)
			    nPages=Integer.parseInt(linkPage);
			  int startPos=(nPages*pageSize)-pageSize;
		      int final_pos=(startPos+pageSize);
		      if(final_pos>vLista.size()) final_pos=vLista.size();
	          
		      //Gestione della TABLE
		      sHTMLRet.append("<table class=\"AFCFormTABLE\" width=\"100%\" cellspacing=\"0\" cellpadding=\"3\">\n");
		      sHTMLRet.append("<tr>\n");
		      sHTMLRet.append("<td class=\"AFCColumnTD\" nowrap colspan=\"2\">Elenco</td>\n"); 
		      sHTMLRet.append("</tr>\n");
		      
		      for(int i=startPos;i<final_pos;i++) 
		      { 
		    	String nome, id,path;
	    	     
	    	    WrkspInformation wrkspInfo = (WrkspInformation)vLista.get(i);
	    	    nome= wrkspInfo.getNomeWrksp();
	    	    id =  wrkspInfo.getidWrksp();
	    	    path = wrkspInfo.getPathManuale();
	    	        	     
	    	    //Gestione della TABLE
	    	    if(i%2==0)
				  sHTMLRet.append("<tr class=\"AFCDataTD\" >\n");
				else
				  sHTMLRet.append("<tr class=\"AFCAltDataTD\" >\n");
				    
	    	    //sHTMLRet.append("  <tr>");
			    sHTMLRet.append("<td colspan=\"2\" border=\"1\">\n");
			    sHTMLRet.append("<table width=\"100%\">\n");
			    sHTMLRet.append("<tr>\n");
			    sHTMLRet.append("<td width=\"85%\">\n");
				sHTMLRet.append("<img border=\"0\" src=\""+_CARTELLAGDC+"\" />\n");
			    sHTMLRet.append("            "+nome);	            
				sHTMLRet.append("</td>\n");
			    sHTMLRet.append("<td width=\"5%\">\n");
			    if (wrkspInfo.getCompUpdate()==1)
			      sHTMLRet.append(getModifica(id));
			    else
			      sHTMLRet.append("&nbsp;");
				sHTMLRet.append("</td>\n");           
			    sHTMLRet.append("<td width=\"5%\">\n");
			    if (wrkspInfo.getCompDelete()==1)
			      sHTMLRet.append(getElimina(id));
			    else
			      sHTMLRet.append("&nbsp;");
				sHTMLRet.append("</td>\n");                        	                        
			    sHTMLRet.append("<td width=\"5%\">\n");
			    
			    if (wrkspInfo.getCompManage()==1 || ruolo.equals(RUOLO_AMM))
			      sHTMLRet.append(getCompetenze(id));
			    else
			      sHTMLRet.append("&nbsp;");
			    sHTMLRet.append("</td>\n");           
			    sHTMLRet.append("<td width=\"5%\">\n");
			    if (ruolo.equals(RUOLO_AMM))
			     sHTMLRet.append(getManuali(id,nome,path));
			    else
			      sHTMLRet.append("&nbsp;");	
				sHTMLRet.append("</td>\n");                        	           
			    sHTMLRet.append("</tr>\n");
			    sHTMLRet.append("</table>\n");    
			    sHTMLRet.append("</td>\n");
			    sHTMLRet.append("</tr>\n");
    	      }
		      
		      sHTMLRet.append("<tr>\n");
			  sHTMLRet.append("<td class=\"AFCFooterTD\" align=\"center\" nowrap>\n");
              sHTMLRet.append(this.getNavigator(vLista.size(),pageSize));
	    	  sHTMLRet.append("</td>\n");
	    	  sHTMLRet.append("<td class=\"AFCFooterTD\" width=\"10%\" align=\"right\" nowrap>\n");
	    	  if (ruolo.equals(RUOLO_AMM)) 
	    	  {
	    		  sHTMLRet.append("<button class=\"textPulsanteDefault\" onclick=\"popupNew('CartellaMaint.do?idCartProveninez=0&Provenienza=W','Inserisci WorkSpace',400,160,0,50);\" onmouseover=\"this.style.cursor='hand';this.style.color='#FF6600';\" onmouseout=\"this.style.color='black';\" title=\"Inserisci una nuova Area di Lavoro\" name=\"Button_Submit\"><nobr>\n");
	    		  sHTMLRet.append("<div class=\"textPulsante\" align=\"left\">\n");
				  sHTMLRet.append("<img height=\"16\" src=\"../common/"+_ADD+"\" width=\"16\" align=\"absMiddle\" name=\"img\">&nbsp;Inserisci");
	    	  }
	    	  else
	    	  {	
	    		sHTMLRet.append("<button class=\"textPulsanteDefault\" onmouseover=\"this.style.cursor='hand';this.style.color='#FF6600';\" onmouseout=\"this.style.color='black';\" title=\"Inserisci una nuova Area di Lavoro\" name=\"Button_Submit\"><nobr>\n");
	    	    sHTMLRet.append("<div class=\"textPulsante\" align=\"left\">\n");
			    sHTMLRet.append("<img height=\"16\" src=\"../common/"+_ADD_DS+"\" width=\"16\" align=\"absMiddle\" name=\"img\">&nbsp;Inserisci");
	    	  } 
			  
			  sHTMLRet.append("</div>\n");
			  sHTMLRet.append("</nobr>\n");
			  sHTMLRet.append("</button>\n");
              sHTMLRet.append("</td>\n"); 
			  sHTMLRet.append("</tr>\n");
			  sHTMLRet.append("</table>"); 
   		    
			  return sHTMLRet.toString();
	   }
	   
	   
	   /**
	    * Restituisce il link per accedere alla form di Modifica della Cartella 
	    * 
	    * @param  id   indica id della cartella WorkSpace
	    * 
	    * @return link per la modifica
	   */	  
	   private String getModifica(String id) throws Exception
	   {
	           String modifica=h.getImg(_VUOTO);
	           modifica=h.getAncore("#",h.CartellaMaintPop("W","","W",id,""),"",h.getImgHand("Modifica Area di Lavoro",_EDITFOLDER));
	           modifica+=h.getNbsp();
	           modifica+=h.getNbsp();
	           return modifica;
	   } 
	   
	   /**
	    * Restituisce il link per accedere alla form di Gestione Manuali  
	    * 
	    * @param  id   indica id della cartella WorkSpace
	    * 
	    * @return link per la modifica
	   */	  
	   private String getManuali(String id,String nome,String path) throws Exception
	   {
	           String manuali=h.getImg(_VUOTO);
	           manuali=h.getAncore("#",h.ManualiPop(id,nome,path),"",h.getImgHand("Gestione Manuali",_MANUALI));
	           manuali+=h.getNbsp();
	           manuali+=h.getNbsp();
	           return manuali;
	   } 
	   
	   
	   /**
	    * Restituisce il link per accedere alla form di Elimina della Cartella 
	    * 
	    * @param  id   indica id della cartella WorkSpace
	    * 
	    * @return link per l'eliminazione
	   */   
	   private String getElimina(String id) throws Exception
	   {
	           String elimina=h.getImg(_VUOTO);
	           elimina=h.getAncore("#",h.VistaCartDelPop("Area di Lavoro","eliminare",id,"","W"),"",h.getImgHand("Elimina Area di Lavoro",_ANNULLA));
	           elimina+=h.getNbsp();
	           elimina+=h.getNbsp();
	           return elimina;
	   } 
	   
	   /**
	    * Restituisce il link per accedere alla form Competenze della Cartella 
	    * 
	    * @param  id   indica id della cartella WorkSpace
	    * 
	    * @return link per la gestione competenze
	   */ 
	   private String getCompetenze(String id) throws Exception
	   {
	           String competenze=h.getImg(_VUOTO);
	           competenze=h.getAncore("#",h.CompetenzePop(id,"C","N","0","-1","W"),"",h.getImgHand("Gestisce le competenze del Area di Lavoro",_COMP));
	           return competenze;
	   } 
	   
	   /**
	    * Restituisce il navigatore della form Gestione WorkSpace 
	    * 
	    * @param  'HttpServletRequest' QueryString
	    *
	    * @return url
	   */ 
	   private String setURLpage() throws Exception
	   {
		       String url="";
		       String queryURL="";
			   String queryString=null;
			   
			   if(req!=null)
				  queryString = req.getQueryString();
			   
			   if(queryString!=null)
			   {
					queryString = xss.encodeHtmlAttribute("queryString",queryString);
					queryString = queryString.replaceAll("&amp;", "&");
				   
					if((queryString.indexOf("LINKSPage")!=-1) && (queryString.indexOf("&")!=-1))
				    { 
				    	  String[] v=queryString.split("&");
				    	  for(int i=0;i<v.length;i++)
				          {
				         	 if(v[i].indexOf("LINKSPage")==-1)
				         		queryURL+=v[i]+"&";
				          }
				          queryURL=queryURL.substring(0,queryURL.length()-1);
				    }
				    else
				    {
				      if(queryString.indexOf("LINKSPage")==-1)
				    	queryURL=queryString; 
				    }
			    } 
			   url="GestioneWRKSPC.do?"+queryURL; 
		       
		       return url;
	   }
	   
	   
	   /**
	    * Restituisce il navigatore della form Gestione WorkSpace 
	    * 
	    * @param  
	    * 
	    * @return navigatore
	   */ 
	   public String getNavigator(int nrecords,int pageSize) throws Exception
	   {
	           String navigator;
	           String First_URL="";
	           String Prev_URL="";
	           String Next_URL="";
	           String Last_URL="";
	           String LINKSPage="LINKSPage=";
	           String classNavigator="AFCNavigatorLink";
	           String page="1";
	           if(req!=null)
	        	 page=verificaParametroGet("LINKSPage",req.getParameter("LINKSPage"));	     
	           if(page==null) page="1";	           
	           String Url_page=setURLpage();
	           int Total_Pages=(int)Math.ceil((double)nrecords/pageSize);
	           if (Total_Pages==0) Total_Pages=1;// caso di lista vuota
	           
	           String Page_Number=page;
	           int npages=Integer.parseInt(page);
	      
	           // FIRST
	           if((page!=null) && (npages!=1))
	           {
	             First_URL=Url_page+LINKSPage+"1";
	             navigator=h.getAncoreClass(classNavigator,First_URL,h.getImg(_FIRST_THEMES));
	           }
	           else
	             navigator=h.getImg(_VUOTO_THEMES);
	              
	           // PREV
	           if((npages > 1) && (page!=null)){
	             Prev_URL=Url_page+LINKSPage+(npages-1);
	             navigator+=h.getAncoreClass(classNavigator,Prev_URL,h.getImg(_PREV_THEMES));
	           }
	           else
	             navigator+=h.getImg(_VUOTO_THEMES);
	             
	           // NUMERI DI PAGINE
	           navigator+=h.getNbsp()+Page_Number+h.getNbsp()+"di"+h.getNbsp()+Total_Pages+h.getNbsp(); 
	       
	           // NEXT
	           if((nrecords - (npages*pageSize))>0){
	             Next_URL=Url_page+LINKSPage+(npages+1);
	             navigator+=h.getAncoreClass(classNavigator,Next_URL,h.getImg(_NEXT_THEMES));
	            }
	            else
	             navigator+=h.getImg(_VUOTO_THEMES);
	           
	           // LAST  
	           if((Total_Pages!=1) && (page!=null) && (npages!=Total_Pages)){
	              Last_URL=Url_page+LINKSPage+Total_Pages;
	              navigator+=h.getAncoreClass(classNavigator,Last_URL,h.getImg(_LAST_THEMES));
	           }
	           else
	              navigator+=h.getImg(_VUOTO_THEMES);
	       
	           return navigator;
	           
	   } 
	   
	   /**
	    * Restituisce l'id della WorkSpace di default
	    * per l'utente.
	    * Se non ne trova torna un'eccezione
	    * (non sono abilitato a nessuna workspace)
	    * Se specificato il parametro wrksppref
	    * lo si cerca di restituire come default
	    * controllando se questo esiste nella lista
	    * delle workspace per l'utente, in caso
	    * contrario si genera un'eccezione
	    * (non sono abilitato alla wrksppref
	    * o questa non esiste) 
	    * 
	    * @param 'wrkspdef' WorkSpace di preferenza
	    * @return id della WorkSpace di default per l'utente 
	    *         o wrkspdef se questo parametro non è nullo
	    *         ed un abilitazione alla workspace wrkspdef        
	   */	   
	   public String retrieveWrkSp(String wrksppref,String idObject,String typeObject) throws Exception {
		      String sRet="";

		      try {

                  //Controllo se l'oggetto è nullo
				  if (idObject == null) {
					  log.log_error("CCS_WorkSpace::retrieveWrkSp() - Attenzione! L'oggetto è nullo! - idOggetto:" + idObject + " - tipoOggetto:" + typeObject);
					  throw new Exception("Attenzione! L'oggetto è nullo!");
				  }

				  try {
					  dbOp.setStatement(retrieveSQLWrkSp(idObject, typeObject));
					  dbOp.execute();

					  ResultSet rst = dbOp.getRstSet();

					  //Se esiste un record...
					  if (rst.next()) {
						  sRet = rst.getString(1);
						  return sRet;
					  }

				  } catch (Exception e) {
					  log.log_error("CCS_WorkSpace::retrieveWrkSp() - idOggetto:" + idObject + " - tipoOggetto:" + typeObject);
					  throw e;
				  }

				  //Altrimenti non coincidono
				  log.log_error("CCS_WorkSpace::retrieveWrkSp() - Attenzione! L'oggetto " + idObject + " non è contenuto in nessun Area di Lavoro!");
				  throw new Exception("Attenzione! L'oggetto " + idObject + " non è contenuto in nessun Area di Lavoro!");
			  }
		      catch (Exception e){
		      	throw e;
			  }
		      finally {
		      	_finally();
			  }
	   }
	   
	   
	   /**
	    * Restituisce l'id della WorkSpace di default
	    * per l'utente.
	    * Se non ne trova torna un'eccezione
	    * (non sono abilitato a nessuna workspace)
	    * Se specificato il parametro wrksppref
	    * lo si cerca di restituire come default
	    * controllando se questo esiste nella lista
	    * delle workspace per l'utente, in caso
	    * contrario si genera un'eccezione
	    * (non sono abilitato alla wrksppref
	    * o questa non esiste) 
	    * 
	    * @param 'wrkspdef' WorkSpace di preferenza
	    * @return id della WorkSpace di default per l'utente 
	    *         o wrkspdef se questo parametro non è nullo
	    *         ed un abilitazione alla workspace wrkspdef        
	   */	   
	   public String getWrkSpDefault(String wrksppref) throws Exception {
		      String sRet;

		      try {
				  if(wrksppref!=null && (wrksppref.indexOf("C")!=-1))
					  wrksppref = wrksppref.substring(1,wrksppref.length());


				  dbOp.setStatement(getSQLListWrkSp(false));
			      dbOp.execute();
			      ResultSet rst = dbOp.getRstSet();
			      
			      //Se esiste un record...
			      if (rst.next()) {
			    	  //...e non ho la preferenza oppure è la stessa, lo restituisco
			    	  if ( wrksppref==null || ( wrksppref!=null && wrksppref.equals(rst.getString(2))) ) {
			    		  sRet=rst.getString(2);
			    		  return sRet;
			    	  }
			      }
			      //Altrimenti non ho accesso da nessuna parte
			      else {
			    	  throw new Exception("Attenzione! Non si possiedono diritti su nessuna workspace");
			      }

			      //Esiste almeno un record ma ho una preferenza diversa
			      //dal primo record....la cerco fra gli altri
			      while (rst.next()) 
			      {	    if ( wrksppref!=null && wrksppref.equals(rst.getString(2))) {
			    	    	return wrksppref;
			    	    }
			      }

				  throw new Exception("Attenzione! Non si possiedono diritti sulla workspace "+wrksppref+" oppure questa non esiste!");
		      }		      
			  catch (Exception e) {
				  log.log_error("CCS_WorkSpace::getWrkSpDefault() - SQL:"+getSQLListWrkSp(false));
			      throw e;
			 }
		     finally {
		      	_finally();
			 }

	   }
	   
	   /**
	    * Setta la workSpace di preferenza per l'utente.
	    * 
	    */	   
	   public void setWrkSpPreferenza(String wrksp) throws Exception 
	   {
		      String sql="";
		      boolean commit= false;
		      try {
			      sql=setSQLWrkSpFromRegistro(wrksp);
		    	  dbOp.setStatement(sql);
			      dbOp.execute();
			      commit=true;
		      }		      
			  catch (Exception e) {
				  commit=false;
				  log.log_error("CCS_WorkSpace::setWrkSpPreferenza() - SQL:"+sql);
			      throw e;
			 }
		     finally {
		      	_finally(commit);
			 }
		}
	   
	   /**
	    * Restituisce l'id della WorkSpace di preferenza per l'utente.
	    * 
	    * @return id della WorkSpace di preferenza per l'utente. 
	   */	   
	   public String getWrkSpPreferenza() throws Exception 
	   {
		      String sRet,idwrksp=null,sql="";
		      boolean vCompW; 

		      try {
			      
		    	  //Caso particolare in cui Utente e il Modulo siano nulli
		    	  if(user==null && modulo==null)
		    	  {
		    		log.log_error("CCS_WorkSpace::getWrkSpPreferenza() - Le variabili sessione Utente e Modulo sono nulli. "+sql);
		    	    throw new Exception("Attenzione! Non si possiedono diritti su nessuna workspace");	  
		    	  } 
		    	  
		    	  sql=getSQLWrkSpFromRegistro();
		    	  dbOp.setStatement(sql);
			      dbOp.execute();
			       
			      ResultSet rst = dbOp.getRstSet();
			      
			      //Se esiste un record...
			      if (rst.next()) {
					  idwrksp = rst.getString(1);
				  }

			      if(idwrksp!=null && !idwrksp.equals("")) 
			      {	  
			        vCompW=verificaCompetenzaWRKSP(idwrksp);
			        
			        //Controllo competenza wrksp di preferenza
			        if(vCompW)
			         sRet=idwrksp;
			        else
			         sRet=getWrkSpPreferenzaFromParametri();	
			      }
			      else
			    	sRet=getWrkSpPreferenzaFromParametri();
		      }		      
			  catch (Exception e) {
				  log.log_error("CCS_WorkSpace::getWrkSpPreferenza() - SQL:"+sql);
			      throw e;
			 }
		     finally {
		      	_finally();
			 }
			 return sRet;
		}
	   
	   /**
	    * Restituisce l'id della WorkSpace di default impostato nella 
	    * tabella PARAMETRI.
	    * 
	    * @return id della WorkSpace di default per l'utente. 
	   */	
	   private String getWrkSpPreferenzaFromParametri() throws Exception 
	   {
		       String sRet,idwrksp=null,sql="";
		       boolean vCompW;

		       try {
		    	  sql=this.getSQLWrkSpFromParametri();
		    	  dbOp.setStatement(sql);
			      dbOp.execute();
			       
			      ResultSet rst = dbOp.getRstSet();
			      
			      //Se esiste un record...
			      if (rst.next()) 
			    	idwrksp=rst.getString(1);
			        
			      if(idwrksp!=null && !idwrksp.equals(""))
			      {	
			    	idwrksp=rst.getString(1);
			        vCompW=verificaCompetenzaWRKSP(idwrksp);
			        
			        //Controllo competenza wrksp di preferenza
			        if(vCompW)
			         sRet=idwrksp;
			        else
			         sRet=getWrkSpDefault(null);	
			      }
			      else
			    	sRet=getWrkSpDefault(null);
			   }		      
			   catch (Exception e) {
				  log.log_error("CCS_WorkSpace::getWrkSpPreferenzaFromParametri() - SQL:"+sql);
			      throw e;
			  }
			  return sRet;
		}
	   
	   /**
	    * Verifica le competenze di lettura sulla wrksp data in input.
	    * Se non esiste nessuna competenza torna false
	    * altrimenti true
	    *  
	    * @param idWrksp id della wrksp
	    * @return true o false della verifica delle competenze         
	   */   
	   private boolean verificaCompetenzaWRKSP(String idWrksp) throws Exception 
	   {
		     boolean sRet;
		     String sql=""; 

		      try {
		    	  sql=this.getSQLCheckCompWrkSp(idWrksp);
		    	  dbOp.setStatement(sql);
			      dbOp.execute();
			       
			      ResultSet rst = dbOp.getRstSet();
			      
			      //Se esiste un record...
			      if (rst.next()) 
			        sRet=true;
			      else
			        sRet=false;
			  }		      
			  catch (Exception e) {
				  log.log_error("CCS_WorkSpace::verificaCompetenzaWRKSP() - SQL:"+sql);
			      throw e;
			 }
			 return sRet;
		}
	   
	   /**
	    * Restituisce src della WorkArea 
	    * dato idOggetto e tipoOggetto.
	    * Se non ne trova torna un'eccezione
	    * (l'oggetto non esiste)
	    *  
	    * @param idObj id dell'oggetto
	    * @param tipoProv tipo proveninza del link GDC=Client Documentale e W=Link su Workarea
	    * @return src della workarea nel caso di Link a GDCAmvMain altrimenti la sequenza
	    * wrksp@idCartProvenienza nel caso di link alla WorkArea         
	   */	   
	   public String getIDCartProvenienza(String idObj,String wrskpref,String tipoProv) throws Exception
	   {
	          String idPadre,wrksp,ricercamodulistica;
	          String tipoObj;
	          String src="../common/WorkArea.do?";
	          String cart="";

	          try
	          {
	        	if(idObj.indexOf("C")!=-1)
	              tipoObj="C";
	            else
	             if(idObj.indexOf("Q")!=-1)
	   	           tipoObj="Q";
	             else
	           	   throw new Exception(" Occorre specificare il tipo di oggetto "+idObj+"!");

	        	//if((wrskpref!=null) && (!wrskpref.equals("")) && (wrskpref.indexOf("-")==-1))
	        	//  wrskpref="-"+wrskpref;	
	        
		        //Controllo se l'oggetto è una cartella workspace
		        if(idObj.indexOf("-")!=-1)
		        {
		        	if(tipoProv.equals("GDC"))
		     	  	  return src+"idCartella="+idObj;	
		        	else
		        	  return idObj+"@"+idObj+"@N";	
		        }
		        
		        
		        StringBuffer sql = new StringBuffer("");
		        if(tipoObj.equals("C"))
		        {	
		         sql.append("select decode(tipo_oggetto,'C',decode(f_wrksp (id_oggetto, tipo_oggetto),0,id_oggetto,f_wrksp (id_oggetto, tipo_oggetto)),decode(f_wrksp (id_cartella, 'C'),0,id_cartella,f_wrksp (id_cartella, 'C'))) wrksp, id_cartella padre,'' ricercamodulistica from links ");
	             sql.append("where tipo_oggetto='"+tipoObj+"' and id_oggetto= :idObj");
		        }
		        else
		        {
		         sql.append(" select '' wrksp, '' padre, ");
		         //sql.append(" DECODE(INSTR (q.filtro, 'RICERCAMODULISTICA_'),0, '',SUBSTR (q.filtro,LENGTH ('RICERCAMODULISTICA_') + 1,LENGTH (q.filtro))) ricercamodulistica ");
		         sql.append(" DECODE(INSTR (q.filtro, 'RICERCAMODULISTICA_'),0, '',");
		         sql.append(" ( DECODE (INSTR (q.filtro, '|'),0,");
		         sql.append(" SUBSTR (q.filtro,");
		         sql.append(" LENGTH ('RICERCAMODULISTICA_') + 1,");
		         sql.append(" LENGTH (q.filtro)");
		         sql.append(" ),");
		         sql.append(" SUBSTR (q.filtro,");
		         sql.append(" LENGTH ('RICERCAMODULISTICA_') + 1,");
		         sql.append("(INSTR (q.filtro, '|')-1-LENGTH ('RICERCAMODULISTICA_')))) ");     
		         sql.append(")) ricercamodulistica ");		         
		         sql.append(" from query q ");
	             sql.append("where q.id_query= :idObj");
                }
				log.log_info("CCS_WorkSpace::getIDCartProvenienza()::("+idObj+","+wrskpref+","+tipoProv+") SQL= "+sql);
	            
		        dbOp.setStatement(sql.toString());
		        dbOp.setParameter(":idObj", idObj.substring(1,idObj.length()));
	            dbOp.execute();
			    ResultSet rs = dbOp.getRstSet();
			    rs.next();
	     	  	idPadre=rs.getString("padre");
	     	  	wrksp=rs.getString("wrksp");
	     	  	ricercamodulistica=rs.getString("ricercamodulistica");

				log.log_info(" idPadre= "+idPadre+" - wrksp= "+wrksp+" ricercamodulistica= "+ricercamodulistica);

	     	  	if(tipoProv.equals("GDC"))
	     	  	{	
		     	  if((wrksp!=null) && (!wrksp.equals(wrskpref)))
		     		 throw new Exception(" L'oggetto non è contenuto nella workspace specificata!");  
		     	  else  
                  {
		     		if(tipoObj.equals("C"))
		  	          src+="idCartella="+idObj+"&idCartAppartenenza="+idPadre;
		  	        else
		  	        {
		  	          if((ricercamodulistica!=null) && (!ricercamodulistica.equals("")))
		  	          {
		  	        	String redirect="";
	     	  			String gdc_link="../common/WorkArea.do?idQuery="+idObj.substring(1,idObj.length())+"&idCartAppartenenza="+idPadre+redirect+"&tipoUso=R";
		  	        	String sarea=ricercamodulistica.substring(0,ricercamodulistica.indexOf("@"));
		        		//String scm=ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());
		        		
		        		String scm="";
		  	        	int ncm = countCharOccurrences(ricercamodulistica,'@');

		        		if(ncm==2){
		        			String listacm = ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());
		        			String[] l = listacm.split("@");
		        			scm = l[1].toString();
		        		}
		        		else
		        		 scm = ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());	
		        		
		        		String parametri="idQuery="+idObj.substring(1,idObj.length())+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+idPadre+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
		        		src="../restrict/ServletRicercaModulistica.do?"+parametri;
		              }
		  	          else
		  	        	src+="idQuery="+idObj.substring(1,idObj.length())+"&idCartAppartenenza="+idPadre;  
		  	        }
		  	      }
	     	  	}  
	     	  	else
	     	  	{
					if((ricercamodulistica!=null) && (!ricercamodulistica.equals("")) && (tipoObj.equals("Q")))
		     	  		{  
		     	  			if(wrksp==null)
		     	  			 wrksp=wrskpref;
		     	  			
		     	  			if(idPadre==null){
		     	  			 
		     	  				if(req!=null && req.getParameter("idCartella")!=null){
		     	  					idPadre=req.getParameter("idCartella");
		     	  				}
		     	  				else {
		     	  					if(req!=null && req.getParameter("idCartAppartenenza")!=null){
			     	  					idPadre=req.getParameter("idCartAppartenenza");
			     	  				}
		     	  					else
		     	  					  idPadre=wrksp;
		     	  				}
		     	  			}

		     	  			if (idPadre == null) {
								if (this.req != null && this.req.getParameter("idCartella") != null) {
									idPadre = this.req.getParameter("idCartella");
								} else if (this.req != null && this.req.getParameter("idCartAppartenenza") != null) {
									idPadre = this.req.getParameter("idCartAppartenenza");
								} else {
									idPadre = wrksp;
								}
							}

							cart = idPadre;
							/*if (idPadre != null && idPadre.indexOf("-") != -1) {
								cart = idPadre.substring(1, idPadre.length());
							}*/

		     	  			String redirect="";
		     	  			if(req!=null && req.getParameter("redirect")!=null){
		     	  				redirect=verificaParametroGet("redirect",req.getParameter("redirect"));
			     	  			
			     	  			if(redirect!=null && !redirect.equals(""))
			     	  				redirect="&redirect="+redirect;	
		     	  			}

							String gdc_link="../common/WorkArea.do?idQuery="+idObj.substring(1,idObj.length())+"&idCartAppartenenza="+cart+"&idCartella="+cart+redirect+"&tipoUso=R";
			  	        	String sarea=ricercamodulistica.substring(0,ricercamodulistica.indexOf("@"));
			        		String scm="";
			  	        	int ncm = countCharOccurrences(ricercamodulistica,'@');

			        		if(ncm==2){
			        			String listacm = ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());
			        			String[] l = listacm.split("@");
			        			scm = l[1].toString();
			        		}
			        		else
			        		 scm = ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());	
			        		
			        		
			        		String parametri="idQuery="+idObj.substring(1,idObj.length())+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+cart
			        		+"&wrskpref="+wrskpref+"&idCartella="+cart+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
			        		srcPage="../restrict/ServletRicercaModulistica.do?"+parametri;

			        		src=wrksp+"@"+idPadre+"@S";
		     	  		}
		     	  		else
		     	  		  src=wrksp+"@"+idPadre+"@N";

					log.log_info(" SRC = "+src);
	     	  	}	

              }
              catch (Exception e) {     
               	throw new Exception("Attenzione! Non si possiedono diritti sull'oggetto "+idObj+"!\n"+e.getMessage());
              }
	          finally {
	          	_finally();
			  }
            
              return src;  
       }  
	   
	   private int countCharOccurrences(String source, char target) { 
		   int counter = 0; 
		   for (int i = 0; i < source.length(); i++) { 
		   if (source.charAt(i) == target) { 
		   counter++; 
		   } 
		   } 
		   return counter; 
	   } 
	   
	   /*
	   public String getIDCartProvenienza(String idObj,String wrskpref,String tipoProv) throws Exception
	   {
	          String idPadre,wrksp,ricercamodulistica;
	          String tipoObj;
	          String src="../common/WorkArea.do?";
	          IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	    	
	          try
	          {
	        	if(idObj.indexOf("C")!=-1)
	              tipoObj="C";
	            else
	             if(idObj.indexOf("Q")!=-1)
	   	           tipoObj="Q";
	             else
	           	   throw new Exception(" Occorre specificare il tipo di oggetto "+idObj+"!");
	        
		        //Controllo se l'oggetto è una cartella workspace
		        if(idObj.indexOf("-")!=-1)
		        {
		        	CCS_common.closeConnection(dbOp);   
		        	if(tipoProv.equals("GDC"))
		     	  	  return src+"idCartella="+idObj;	
		        	else
		        	  return idObj+"@"+idObj+"@N";	
		        }
		        
		        
		        String sql="";
		        if(tipoObj.equals("C"))
		        {	
		         sql="select decode(tipo_oggetto,'C',decode(f_wrksp (id_oggetto, tipo_oggetto),0,id_oggetto,f_wrksp (id_oggetto, tipo_oggetto)),decode(f_wrksp (id_cartella, 'C'),0,id_cartella,f_wrksp (id_cartella, 'C'))) wrksp, id_cartella padre,'' ricercamodulistica from links ";
	             sql+="where tipo_oggetto='"+tipoObj+"' and id_oggetto="+idObj.substring(1,idObj.length());
		        }
		        else
		        {
		         sql=" select '' wrksp, '' padre, ";
		         sql+=" DECODE(INSTR (q.filtro, 'RICERCAMODULISTICA_'),0, '',SUBSTR (q.filtro,LENGTH ('RICERCAMODULISTICA_') + 1,LENGTH (q.filtro))) ricercamodulistica ";
		         sql+=" from query q ";
	             sql+="where q.id_query="+idObj.substring(1,idObj.length());
		        
		        }	
	            //System.out.println("Link diretto SRC= "+sql);
	            
	            dbOp.setStatement(sql);
			    dbOp.execute();
			    ResultSet rs = dbOp.getRstSet();
			    rs.next();
	     	  	idPadre=rs.getString("padre");
	     	  	wrksp=rs.getString("wrksp");
	     	  	ricercamodulistica=rs.getString("ricercamodulistica");
	     	  	
	     	  	if(tipoProv.equals("GDC"))
	     	  	{	
		     	  if((wrksp!=null) && (!wrksp.equals(wrskpref)))
		     		 throw new Exception(" L'oggetto non è contenuto nella workspace specificata!");  
		     	  else  
                  {
		     		if(tipoObj.equals("C"))
		  	          src+="idCartella="+idObj+"&idCartAppartenenza="+idPadre;
		  	        else
		  	        {
		  	          if((ricercamodulistica!=null) && (!ricercamodulistica.equals("")))
		  	          {
		  	        	String gdc_link="../common/WorkArea.do?idQuery="+idObj.substring(1,idObj.length())+"&idCartAppartenenza="+idPadre+"&tipoUso=R";
		  	        	String sarea=ricercamodulistica.substring(0,ricercamodulistica.indexOf("@"));
		        		String scm=ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());
		        		String parametri="idQuery="+idObj.substring(1,idObj.length())+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+idPadre+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
		        		src="../restrict/ServletRicercaModulistica.do?"+parametri;
		              }
		  	          else
		  	        	src+="idQuery="+idObj.substring(1,idObj.length())+"&idCartAppartenenza="+idPadre;  
		  	        }
		  	      }
	     	  	}  
	     	  	else
	     	  	{	
	     	  		if((ricercamodulistica!=null) && (!ricercamodulistica.equals("")) && (tipoObj.equals("Q")))
	     	  		{  
	     	  			if(wrksp==null)
	     	  			 wrksp=wrskpref;
	     	  			
	     	  			if(idPadre==null)
	     	  			 idPadre="";
	     	  			
	     	  			String gdc_link="../common/WorkArea.do?idQuery="+idObj.substring(1,idObj.length())+"&idCartAppartenenza="+idPadre+"&tipoUso=R";
		  	        	String sarea=ricercamodulistica.substring(0,ricercamodulistica.indexOf("@"));
		        		String scm=ricercamodulistica.substring(ricercamodulistica.indexOf("@")+1,ricercamodulistica.length());
		        		String parametri="idQuery="+idObj.substring(1,idObj.length())+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+idPadre
		        		+"&WRKSP="+wrskpref
		        		+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
		        		srcPage="../restrict/ServletRicercaModulistica.do?"+parametri;
		            	
		        		src=wrksp+"@"+idPadre+"@S";	
	     	  		}
	     	  		else
	     	  		  src=wrksp+"@"+idPadre+"@N";	
	     	  	} 
	     	  	
	     	  	//CCS_common.closeConnection(dbOp);
               }
              catch (Exception e) {     
               //CCS_common.closeConnection(dbOp);
               throw new Exception("Attenzione! Non si possiedono diritti sull'oggetto "+idObj+"!\n"+e.getMessage());
              }
            
              return src;  
       }  */
	   
	   /**
	    * Restituisce src della WorkArea. 
	    * 
	    * @return String src della workarea nel caso di Link diretto alla WorkArea
	    * di una Query di tipo Ricerca _Modulistica
	    */	   
	   public String getSRCPage()
	   {
		      return  srcPage; 
	   }
   
	   /**
	    * Verifica le competenze di lettura 
	    * sull'oggetto dato in input.
	    * Se non esiste nessuna competenza torna un'eccezione
	    * (non si possiedonole competenze sull'oggetto)
	    *  
	    * @param idObj id dell'oggetto
	    * @return true verifica delle competenze         
	   */	   
	   public boolean verificaCompetenza(String idObj) throws Exception
	   {
	          String verifica;
	          String tipoObj;

	          try
	          {
	        	  if(idObj.indexOf("C")!=-1)
			        tipoObj="C";
			      else
			        if(idObj.indexOf("Q")!=-1)
			   	      tipoObj="Q";
			        else
			      	  throw new Exception("Occorre specificare il tipo di oggetto "+idObj+"!");
			      
	        	  idObj=idObj.substring(1,idObj.length());
		          
	        	  String sql;
		          sql="  select gdm_competenza.gdm_verifica(decode( :TIPOOBJ ,'C','VIEW_CARTELLA','QUERY'),";
		          sql+=" decode( :TIPOOBJ ,'C',f_idview_cartella ( :IDOBJ ), :IDOBJ ),";
		          sql+=" 'L', :USER ,f_trasla_ruolo ( :USER ,'GDMWEB','GDMWEB'),TO_CHAR (SYSDATE, 'dd/mm/yyyy')) lettura ";
		          sql+=" from dual ";
		          //System.out.println("SQL= "+sql);
	              
		          dbOp.setStatement(sql);
		          dbOp.setParameter(":TIPOOBJ",tipoObj);
		          dbOp.setParameter(":IDOBJ",idObj);
		          dbOp.setParameter(":USER",user);
				  dbOp.execute();
			      ResultSet rs = dbOp.getRstSet();
			      rs.next();
			      verifica=rs.getString("lettura");	    
	              
			      if(verifica.equals("0"))
			        throw new Exception("Non si possiedono diritti sull'oggetto "+idObj+"!");
			      else
			      {
			    	  return true;
			      }
                    
	          }
	          catch (Exception e) {     
	             throw new Exception("Attenzione! \n"+e.getMessage());
	          }
	          finally {
	          	//Non chiudere la dpOp //_finally();
			  }
	   }  
	   
	   
	   /**
	    * Restituisce il path dell'oggetto per effettuare il relativo 
	    * path del TreeView dato idOggetto e tipoOggetto.
	    * Se l'oggetto non è contenuto nella workspace specificata
	    * torna un'eccezione (l'oggetto non esiste)
	    *  
	    * @param idObj id dell'oggetto
	    * @param 'tipoObj' tipo dell'oggetto
	    * @param wrksppref workspace che contiene dell'oggetto
	    * @return sequenza di cartelle contenenti l'oggetto 
	    * Esempio: 1125X523X5658         
	   */	   
	   public String getPathTree(String idObj,String wrksppref) throws Exception
	   {
	          String path,seq="",tipoObj;
	          String [] pathtree;

	          try
	          {
	            //Controllo se l'oggetto è una cartella workspace
		        if(idObj.indexOf("-")!=-1)
		        {
		        	return seq;
		        }
	          
		        if(idObj.indexOf("C")!=-1)
		          tipoObj="C";
		        else
		          if(idObj.indexOf("Q")!=-1)
		   	        tipoObj="Q";
		          else
		            throw new Exception("Attenzione! Occorre specificare il tipo di oggetto "+idObj+"!");
				
		        idObj=idObj.substring(1,idObj.length());
		        
		        String sql="";
		        sql="  select decode(:TIPOOBJ,'C',F_PATH_FOLDER(:IDOBJ,'I',:USER),F_PATH_FOLDER(l.id_cartella,'I',:USER)) path, l.id_cartella idcartProv ";
		        sql+=" from links l,cartelle c, view_cartella v ";
		        sql+=" where tipo_oggetto=:TIPOOBJ and l.id_oggetto=:IDOBJ and l.id_cartella = c.id_cartella and c.id_cartella = v.id_cartella ";
		        sql+=" and gdm_competenza.gdm_verifica ('VIEW_CARTELLA',v.id_viewcartella,'L',:USER,f_trasla_ruolo (:USER, 'GDMWEB', 'GDMWEB'),TO_CHAR (SYSDATE, 'dd/mm/yyyy')) > 0 ";
		        //System.out.println("getPathTree -- SQL= "+sql);
                
                dbOp.setStatement(sql);
                dbOp.setParameter(":TIPOOBJ",tipoObj);
                dbOp.setParameter(":IDOBJ",idObj);
                dbOp.setParameter(":USER",user);
		        dbOp.execute();
		        ResultSet rs = dbOp.getRstSet();
		        
		        if(rs.next())
     	  	    {
		          path=rs.getString(1);	 
     	  	      if(!path.equals(""))
                  {
                    pathtree=path.split("@");
            
		            //Controllo che l'oggetto è contenuto nella workspace di sessione
		            if(!pathtree[0].equals(wrksppref))
		               seq="";//throw new Exception("Attenzione! L'oggetto "+idObj+" non è contenuto nella workspace "+wrksppref+" specificata!");
		            else
		            { 
		              if(pathtree.length==1)
		               seq=pathtree[0];
		              else
		              {
		                for(int i=1;i<pathtree.length;i++)
				        {
				          if(i==(pathtree.length-1))
				           seq+=pathtree[i];
				          else
				           seq+=pathtree[i]+"X";
				        }
		              }
		            }
                  }
     	  	    }  

             }
             catch (Exception e) {     
              throw new Exception("Attenzione! L'oggetto "+idObj+" non è contenuto nella workspace "+wrksppref+" specificata!");
             }
	         finally {
	          	_finally();
			 }
       
             return seq;  
	   }  
	   
	   
	   /**
	    * Restituisce la lista delle workspace abilitate 
	    * all'utente passato nel costruttore
	    * 
	    * @param  retrieveComp   True - Vengono recuperate le competenze per l'utente
	    * 						 False - Non vengono recuperate le competenze per l'utente
	    * @return Vettore delle workspace esistenti 
	   */	   
	   private Vector wrkspList(boolean retrieveComp) throws Exception {
		       Vector vList = new Vector();		      

		       try {
			       dbOp.setStatement(getSQLListWrkSp(retrieveComp));
			       dbOp.execute();
			       
			       ResultSet rst = dbOp.getRstSet();
			       
			       while (rst.next()) {
			    	     WrkspInformation wrkspInfo;
			    	     
			    	     if (!retrieveComp)
			    	    	 wrkspInfo = new WrkspInformation(rst.getString(1),
			    	    		 							  rst.getString(2),
			    	    		 							  rst.getString(3));
			    	     else
			    	    	 wrkspInfo = new WrkspInformation(rst.getString(1),
			    	    		 							  rst.getString(2),
			    	    		 							  rst.getString(3),
			    	    		 							  rst.getInt(4),
			    	    		 							  rst.getInt(5),
			    	    		 							  rst.getInt(6),
			    	    		 							  rst.getString(7));
			    	    		 										   
			    	     vList.add(wrkspInfo);		    	     
			       }
               }
               catch (Exception e) {     
                  throw e;
               }
		       finally {
		       	_finally();
			   }
		       return vList;
	   }	   
	   
	   /**
	    * @return SQL della lista di Wrksp a cui lo user
	    *         è abilitato (se ruolo è AMM sono tutte)
	    *         
	    * @param retrieveCompetenze True - Recupera anche le competenze per l'utente
	    *                           False - Non recupera le comp.
	   */	   
	   private String getSQLListWrkSp(boolean retrieveCompetenze)  {
		       StringBuffer sSql = new StringBuffer("");
		   
		       sSql.append("SELECT NOME,CARTELLE.ID_CARTELLA,VIEW_CARTELLA.ID_VIEWCARTELLA ");
		       if (retrieveCompetenze) {
			       
			    	   sSql.append(",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',ID_VIEWCARTELLA,'U', '"+user+"',");
		               sSql.append("F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy')) UP");
		               sSql.append(",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',ID_VIEWCARTELLA,'D', '"+user+"',");
		               sSql.append("F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy')) DE");
		           if (!ruolo.equals(RUOLO_AMM)) {
		               sSql.append(",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',ID_VIEWCARTELLA,'M', '"+user+"',");
		               sSql.append("F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy')) MA");		               
			       }			       
		           else 
		        	   sSql.append(",1 MA ");
		               sSql.append(", (select GDC_UTILITY_PKG.F_JDMSMANUALI('C'||cartelle.id_cartella) from dual) pathmanuale ");
		       }
		       sSql.append("  FROM CARTELLE, VIEW_CARTELLA");		       		      
               sSql.append(" WHERE CARTELLE.ID_CARTELLA<0 ");
               sSql.append("   AND CARTELLE.ID_CARTELLA = VIEW_CARTELLA.ID_CARTELLA ");
               sSql.append("   AND CARTELLE.ID_CARTELLA <> -3 ");
               sSql.append("   AND CARTELLE.ID_CARTELLA <> -2 ");
               sSql.append("   AND nvl(CARTELLE.stato,'BO')<>'CA' ");
               sSql.append("  AND GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',ID_VIEWCARTELLA,'L', '"+user+"',");
	           sSql.append("                                  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy'))");
	           sSql.append("      >0 ");
               //sSql.append("ORDER BY 2 DESC");
               sSql.append("ORDER BY 1");
		       
                
             // System.out.println("**** "+sSql.toString());
		       return sSql.toString();
	   }
	   
	   /**
	    * @return SQL controllo competenze della wrksp a cui lo user è abilitato 
	    *         
	    * @param id della wrsp
	   */	   
	   private String getSQLCheckCompWrkSp(String id) 
	   {
		       StringBuffer sSql = new StringBuffer("");
		       sSql.append("SELECT NOME,CARTELLE.ID_CARTELLA,VIEW_CARTELLA.ID_VIEWCARTELLA ");
		       sSql.append("  FROM CARTELLE, VIEW_CARTELLA");		       		      
               sSql.append(" WHERE CARTELLE.ID_CARTELLA = "+id);
               sSql.append("   AND CARTELLE.ID_CARTELLA = VIEW_CARTELLA.ID_CARTELLA ");
               sSql.append("   AND CARTELLE.ID_CARTELLA <> -3 ");
               sSql.append("   AND CARTELLE.ID_CARTELLA <> -2 ");
               sSql.append("   AND nvl(CARTELLE.stato,'BO')<>'CA' ");
               sSql.append("  AND GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',ID_VIEWCARTELLA,'L', '"+user+"',");
	           sSql.append("                                  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy'))");
	           sSql.append("      >0 ");
               return sSql.toString();
	   }
	   
	   /**
	    * @return SQL recupero della wrksp di preferenza dalla tabella REGISTRO 
	    *         
	   */	   
	   private String getSQLWrkSpFromRegistro() 
	   {
		       StringBuffer sSql = new StringBuffer("");
		       sSql.append("SELECT AMVWEB.GET_PREFERENZA('"+WRKSP_DEFAULT+"','"+modulo+"','"+user+"') valore from dual");
               return sSql.toString();
	   }
	   
	   /**
	    * @return SQL inserimento/aggiornamento della wrksp di preferenza dalla tabella REGISTRO 
	    *         
	   */	   
	   private String setSQLWrkSpFromRegistro(String idWRKSP) 
	   {
		       StringBuffer sSql = new StringBuffer("");
		       sSql.append(" BEGIN ");
		       sSql.append(" AMVWEB.set_preferenza('"+WRKSP_DEFAULT+"','"+idWRKSP+"','"+modulo+"','"+user+"'); ");
		       sSql.append(" END; ");
               return sSql.toString();
	   }
	   
	   /**
	    * @return SQL eliminazione del manuale associato alla workspace dalla tabella REGISTRO 
	    *         
	   */	   
	   private String setSQLDeleteManuale(String idWRKSP) 
	   {
		       StringBuffer sSql = new StringBuffer("");
		       sSql.append(" BEGIN ");
		       sSql.append(" GDC_UTILITY_PKG.F_JDMSMANUALI_DELETE('GDMWEB/WRKSPC"+idWRKSP+"'); ");
		       sSql.append(" END; ");
               return sSql.toString();
	   }
	   
	   /**
	    * @return SQL recupero della wrksp di preferenza dalla tabella PARAMETRI 
	    *         
	   */	   
	   private String getSQLWrkSpFromParametri() 
	   {
		       StringBuffer sSql = new StringBuffer("");
		       sSql.append("select valore from parametri where codice='WRKSP_DEFAULT'");
               return sSql.toString();
	   }
	   
	   /**
	    * @return SQL della Wrksp a cui l'oggetto è contenuto
	    *         
	    * @param idObject indica id dell'oggetto
	    * @param typeObject indica tipo dell'oggetto.
	   */	   
	   private String retrieveSQLWrkSp(String idObject,String typeObject)  {
		       StringBuffer sSql = new StringBuffer("");
		       sSql.append("SELECT  F_WRKSP("+idObject+",'"+typeObject+"') wrksp ");
		       sSql.append("FROM DUAL");		       		      
               return sSql.toString();
	   }
}

class WrkspInformation {
	  private String nomeWrksp;
	  private String idWrksp;
	  private String idViewWrksp;
	  private String pathManuale;
	  private int    compUpdate = -1;
	  private int    compDelete = -1;
	  private int    compManage = -1;
	  
	  public WrkspInformation(String nome, String id, String idView) {
		  	 nomeWrksp=nome;
		     idWrksp=id;
		     idViewWrksp=idView;	        
	  }

	  public WrkspInformation(String nome, String id, String idView,
			  			      int update, int delete, int manage,String path) {
		     nomeWrksp=nome;
		     idWrksp=id;
		     idViewWrksp=idView;
	         compUpdate = update;
	         compDelete = delete;
	         compManage = manage; 
	         if(path.indexOf("../../../jdms_manuali/")!=-1)
	          path=path.replace("../../../jdms_manuali/","");
	         pathManuale = path;
	  }
	  
	  public String getNomeWrksp() {
		     return nomeWrksp;
	  }
	  
	  public String getidWrksp() {
		     return idWrksp;
	  }	  

	  public String getidViewWrksp() {
		     return idViewWrksp;
	  }

	  public int getCompUpdate() {
		     return compUpdate;
	  }

	  public int getCompDelete() {
		     return compDelete;
	  }	  

	  public int getCompManage() {
		     return compManage;
	  }	  
	  
	  public String getPathManuale() {
		     return pathManuale;
	  }	 
}
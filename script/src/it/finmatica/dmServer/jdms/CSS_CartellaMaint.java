package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import java.util.StringTokenizer;
import it.finmatica.jfc.dbUtil.*;
import java.net.URLEncoder;
import java.sql.*;

/**
 * Gestione inserimento di Cartella e Query.
 * Classe di servizio per la gestione del Client
*/

public class CSS_CartellaMaint 
{
	   /**
	    * Variabili private 
	   */
	   String idCartella, identifierUpFolder;
	   String Provenienza,cm,ar,tipoUso,idtipoDoc;
	   String workspace, rw, Url;  
	   String page;
	   String tipoAb;
	   String cr; 
	   String compM;
	   String folder1="",folder2="";
	   String insertUpdate;
	   /** Caso particolare di creazione modello protocollo per l'oggetto cartella */
	   String protocollo="";
	   String creaLink="";
	   String tipomodello;
	   CCS_Common CCS_common;
	   private static Environment vu;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato per la creazione 
		 * di Cartella e Query.
		 * 
		 */
	   public CSS_CartellaMaint(String newidCartella,String newidentifierUpFolder,String newProvenienza,String newWorkspace,
			   					String newRW,String newUrl,String newcm,String newpage,CCS_Common newCommon) throws Exception
	   {
		   	  this(newidCartella,newidentifierUpFolder,newProvenienza,newWorkspace,newRW,newUrl,newcm,null,null,newpage,false,newCommon);
	   }
	   /** afterInitialize e beforeShow */
	   public CSS_CartellaMaint(String newidCartella,String newidentifierUpFolder,String newProvenienza,String newWorkspace,
                           		String newRW,String newUrl,String newcm,String newArea,String newtipoUso,String newpage,
                           		boolean fittizio,CCS_Common newCommon) throws Exception
	   {
			  idCartella=newidCartella;
			  identifierUpFolder=newidentifierUpFolder;   
			  Provenienza=newProvenienza;
			  workspace=newWorkspace;
			  if(workspace!=null && workspace.indexOf("-")!=-1)
			  	workspace=workspace.substring(1,workspace.length());	
			  rw=newRW;    
			  Url=newUrl;
			  cm=newcm;
			  CCS_common=newCommon;
			  ar=newArea;
			  tipoUso=newtipoUso;
			  page=newpage;
			  tipoAb=Global.ABIL_CARTELLA;
			  log= new DMServer4j(CSS_CartellaMaint.class,CCS_common); 
	   } 
	   
	   /** Onclick */
	   public CSS_CartellaMaint(String newidCartella,String newidentifierUpFolder,String newtipomodello,String newProvenienza,String newWorkspace,
                           		String newRW,String newUrl,String newpage,boolean fittizio,String newcreaLink,CCS_Common newCommon) throws Exception
	   {
			  idCartella=newidCartella;
			  identifierUpFolder=newidentifierUpFolder;   
			  Provenienza=newProvenienza;
			  workspace=newWorkspace;
			  if(workspace.indexOf("-")!=-1)
			  	workspace=workspace.substring(1,workspace.length());	
			  if(creaLink!=null)				  
			    creaLink=newcreaLink;
			  rw=newRW;    
			  Url=newUrl;
			  CCS_common=newCommon;
			  page=newpage;
			  tipomodello=newtipomodello;
			  tipoAb=Global.ABIL_CARTELLA;
			  log= new DMServer4j(CSS_CartellaMaint.class,CCS_common); 
	   } 
	   
	   /**
		 * Costruttore utilizzato dalla ServletModulisticaCartella
		 * per la costruzione dei TabFolder. 
		 * 
		 */
	   public CSS_CartellaMaint(String newidCartella,String newcompM,String newcm,String newArea,String newInsUpd,
                           String newpage,String newProvenienza,String newWorkspace,String newidentifierUpFolder,
                           String newtipoUso,String newUrl,CCS_Common newCommon) throws Exception
	   {
		      idCartella=newidCartella;
		      compM=newcompM;
		      cm=newcm;
		      ar=newArea;
		      insertUpdate=newInsUpd;
		      page=newpage;
		      Provenienza=newProvenienza;
		      workspace=newWorkspace;
		      identifierUpFolder=newidentifierUpFolder;   
		      tipoUso=newtipoUso;
		      Url=newUrl;
		      CCS_common=newCommon;
		      log= new DMServer4j(CSS_CartellaMaint.class,CCS_common); 
	   }
	   
	   /**
		 * Utilizzato dalla ServletModulisticaCartella
		 * per la costruzione dei TabFolder. 
		 * 
		 */
	   public String _afterInitialize() throws Exception
	   {
		   	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
 
		   	  try
		   	  {   
		   		/** Inserimento di un nuovo elemento (Cartella o Query) */ 
	            if (idCartella == null)     
	            {
	        	   /** Nel caso di Creazione WorkSpace */
	        	   if(Provenienza.equals("W")) {
	        		  CCS_common.closeConnection(dbOp);
	        		  return "OK";
	        	   }
	        	   /** Verifica delle competenze di creazione sulla cartella
	        	     * superiore dove tento di creare il nuovo oggetto (cratella o query) */
	               if(identifierUpFolder.indexOf("C")!=-1)
	        	     identifierUpFolder=identifierUpFolder.substring(1,identifierUpFolder.length());
	               String idw = (new DocUtil(vu)).getIdViewCartellaByIdCartella(identifierUpFolder);
	               vu.connect();
	              
	               if (!verificaCompetenza(tipoAb,idw,Global.ABIL_CREA)) {
	                 String sMessaggio = "NONOK";                        
	                 throw new Exception(sMessaggio);
	               }
	               else
	               {
	                try{vu.disconnectClose();}catch(Exception ei){}
	                CCS_common.closeConnection(dbOp);
	                return "OK";
	               }
	           }
	           else 
	           { 
	        	 /** Modifica dell'elemento con la preparazione del messaggio 
	        	   * di ritorno per effettuare la Redirect */
	        	 String tipoCartella=checkTipoCartella();
        	 
	        	 if (identifierUpFolder == null)
                  identifierUpFolder="0";  
	        	
	        	 /** Get Codice Modello */
		         if(cm==null)
		         {
		           if (!Provenienza.equals("W"))
		           {
		            	if (idCartella.indexOf("-")==-1)
		            	  cm=(new DocUtil(vu)).getCodiceModelloFromIdCartella(idCartella);
		                else
		                  cm=(new DocUtil(vu)).getCodiceModelloFromIdQuery(idCartella.substring(1,idCartella.length()));
		           }
		           else
		           	cm=(new DocUtil(vu)).getCodiceModelloFromIdCartella(idCartella);
		         }
		         else
		           compM="S";
	        
		         if(ar==null)
		         {
		           if (!Provenienza.equals("W"))
		        	 ar=(new DocUtil(vu)).getAreaCartellaOrQuery(idCartella,tipoCartella);
		           else
		        	 ar=(new DocUtil(vu)).getAreaCartellaOrQuery(idCartella,"W");
		         }
	               
		         
	             /** Setta URL */
		         String InsUpd="U";
		         SetUrl(tipoCartella,InsUpd,cm);
		         /** Setto il messaggio di ritorno per effettuare la Redirect */
		         String message= SetMessage(tipoCartella,InsUpd);
		         CCS_common.closeConnection(dbOp);
		         return message;
	           } 
		   	 }
	         catch (Exception e) 
	         {
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp); 
	           throw e;
	           //throw new Exception("CSS_CartellaMaint::_afterInitialize\n"+e.getMessage());
	         }
	   }
	   
	   /**
		 * Scelta del tipo di modello e redirect alla pagina
		 * di visualizzazione della Cartella o Query. 
		 * 
		 */
	   public String _OnClick() throws Exception
	   {
		   	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());

		   	  try
		   	  { 
		   		StringTokenizer st = new StringTokenizer(tipomodello,"&");
	            int i=0;
		        while (st.hasMoreTokens()) {
	               String s=st.nextToken();
	                if (i==0)
	              	   tipoUso=s;
	                  if (i==1)
	               	   cm=s;
	                  if (i==2)
	               	   ar=s;
	                  
	                  i++;	 
		        }  
		   		  
	        	if (Provenienza.equals("W"))
	        	  idCartella="-"+dbOp.getNextKeyFromSequence("WRKSP_SQ"); 
	        	else
	        	 if(Provenienza.equals("C"))
	        	 {	 
				    idCartella=""+dbOp.getNextKeyFromSequence("CART_SQ");
				    try {
			               protocollo=this.getProtocollo(cm,dbOp);
			             }
			             catch (Exception e) {
			            	throw new Exception("CSS_CartellaMaint::_OnClick() - getProtocollo - idCartella:"+idCartella+"\n" + e.getMessage());
			             }
			     }
	        	 else
				  idCartella="-"+dbOp.getNextKeyFromSequence("QRY_SQ");

	        	 String tipoCartella= checkTipoCartella();
                 if (identifierUpFolder == null)
	                identifierUpFolder="0";  
	             /** Setta URL */
	             String InsUpd="I";
	             SetUrl(tipoCartella,InsUpd,cm);
	             /** Setto il messaggio di ritorno per effettuare la Redirect */
	             String message= SetMessageForOnClick(tipoCartella,InsUpd);
	             CCS_common.closeConnection(dbOp);
	             return message;
		   	 }
	         catch (Exception e) 
	         {
	           CCS_common.closeConnection(dbOp);   
	           throw e;
	           //throw new Exception("CSS_CartellaMaint::_OnClick\n"+e.getMessage());
	         }
	   }	
	   
	   /**
		 * Se esiste un solo modello per quel tipo di oggetto padre
		 * (Cartella o Query) allora viene effattuata la redirect
		 * alla pagina di creazione di una nuova Cartella o Query.
		 * 
		 */
	   public String _beforeShow(String Tendina) throws Exception 
	   {
		   	  String message; 
		   	  IDbOperationSQL dbOp;
		   	  
		   	  /** Creazione diretta di un oggetto dato area e cm */
		   	  if(ar!=null && cm!=null && !(Provenienza.equals("W")))
		   	  {
		   		dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);

		   		try {
		  		   this.getDatiModelli(ar,cm,dbOp);
	            }
	            catch (Exception e) {
	               throw new Exception("CSS_CartellaMaint::_beforeShow() - getDatiModelli - CM:"+cm+"\n" + e.getMessage());
	            }  
		   	  }
		   	  else
		   	  {
			   	  /** Controllo il valore della Tendina */       
			   	  if (Tendina==null || Tendina.equals("")) return "";			
			   	  /**  Se il valore è un elenco di possibili valori return */
			   	  if (Tendina.indexOf("</OPTION>")!=Tendina.lastIndexOf("</OPTION>")) return "";
			   	  
			   	  dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  }
		   	  
		   	  
		   	  vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
		   	  
		   	  try
		   	  {
		   		if(Provenienza.equals("W"))
		   		{
                 ar="GDMSYS";
                 /** Recupero codice modello,area e tipoUso */     
	             String tenda=Tendina.substring(Tendina.indexOf("=\"")+2,Tendina.indexOf("\">"));
	             String[] t=tenda.split("&amp;");
	             tipoUso=t[0];
	             cm=t[1];
	             idCartella="-"+dbOp.getNextKeyFromSequence("WRKSP_SQ");
	             cr="WRKSP"+idCartella;
                 
	             try
                 {
                   this.getDatiModelli(cm,dbOp);
                 }
                 catch (Exception e) {
                   throw new Exception("CSS_CartellaMaint::_beforeShow() - getDatiModelli - Provenienza:"+Provenienza+" idCartella:"+idCartella+" - CR:"+cr+" - CM:"+cm+"\n" + e.getMessage());
                 }
                 /** Setta il parametro a MODIFICA */
                 rw="W";                 
                 /** Controlla il tipo cartella (USER o SYSTEM) */
                 String tipoCartella= checkTipoCartella();
                 /** Setta URL */
                 String InsUpd="I";
                 SetUrl(tipoCartella,InsUpd,cm);
                 /** Vine settato il messaggio di ritorno per effettuare la Redirect */                
                 message= SetMessageForNew(tipoCartella,InsUpd,ar,cm,cr); 
		   		}
		   		else
		   		{
		   		  /** Verifica delle competenze di creazione sulla cartella
		   		   * superiore dove tento di creare il nuovo oggetto (cratella o query) */
		   		  if(identifierUpFolder.indexOf("C")!=-1)
			        identifierUpFolder=identifierUpFolder.substring(1,identifierUpFolder.length());
                  String idw= (new DocUtil(vu)).getIdViewCartellaByIdCartella(identifierUpFolder);
                  vu.connect();
              
	              if (!verificaCompetenza(tipoAb,idw,Global.ABIL_CREA)) {
	                 String sMessaggio = "NONOK";                        
	                 throw new Exception(sMessaggio);
	              }
	              else
	              {
	                try{vu.disconnectClose();}catch(Exception ei){}
	                
	                if(ar==null && cm==null)
	                {
		                /** Recupero codice modello,area e tipoUso */     
		                String tenda=Tendina.substring(Tendina.indexOf("=\"")+2,Tendina.indexOf("\">"));
		                tenda=tenda.replaceAll("&amp;","@");
		                StringTokenizer st = new StringTokenizer(tenda,"@");
		                int i=0;
			            while (st.hasMoreTokens()) {
			                   String s=st.nextToken();
			                   if (i==0)
			                	   tipoUso=s;
			                   if (i==1)
			                	   cm=s;
			                   if (i==2)
			                	   ar=s;
			                   
			                   i++;	 
			            }
	                }
	                
	                cr=null;              
	
	                try {
	                  this.getDatiModelli(cm,dbOp);
	                }
	                catch (Exception e) {
	                    throw new Exception("CSS_CartellaMaint::_beforeShow() - getDatiModelli - CM:"+cm+"\n" + e.getMessage());
	                }
	 
	                try {
	                  protocollo=this.getProtocollo(cm,dbOp);
	                }
	                catch (Exception e) {
	                  throw new Exception("CSS_CartellaMaint::_beforeShow() - getProtocollo - CM:"+cm+"\n" + e.getMessage());
	   	            }
	
	                /** Recupero dell'id del nuovo oggetto da inserire*/
	                if (Provenienza.equals("C")) {
					   idCartella=""+dbOp.getNextKeyFromSequence("CART_SQ");                   
	                }
				    else {
				       idCartella="-"+dbOp.getNextKeyFromSequence("QRY_SQ");                   
	                }
	                /** Setta il parametro a MODIFICA */
	                rw="W";                 
	                /** Controlla il tipo cartella (USER o SYSTEM) */
	                String tipoCartella= checkTipoCartella();
	                /** Setta URL */
	                String InsUpd="I";
	                SetUrl(tipoCartella,InsUpd,cm);
	                /** Setto il messaggio di ritorno per effettuare la Redirect */                
	                message= SetMessageForNew(tipoCartella,InsUpd,ar,cm,cr);
	              }    
	        	 } 
		   		CCS_common.closeConnection(dbOp);     
		   		return message;
		   	  }
	          catch (Exception e) 
	          {
	           try {vu.disconnectClose();}catch (Exception ei){}
	           CCS_common.closeConnection(dbOp);     
	           throw e;
	           //throw new Exception("CSS_CartellaMaint::_beforeShow\n"+e.getMessage());
	          }   
	   }
	   
	   /**
		 * Recupero alcuni dati del modello.
		 * 
		 */
	   private void getDatiModelli(String area,String cm,IDbOperationSQL dbOp) throws Exception
	   {
	           try
	           {          
	        	   StringBuffer sStm = new StringBuffer();
	        	   sStm.append("select id_tipodoc,tipo_uso ");
	        	   sStm.append("from modelli where codice_modello=:CM and area=:AREA ");
	        	   dbOp.setStatement(sStm.toString());
	        	   dbOp.setParameter(":CM",cm);
	        	   dbOp.setParameter(":AREA",area);
	        	   dbOp.execute();
	        	   ResultSet rst = dbOp.getRstSet();
	        	   if (rst.next()) {				     
	        		   idtipoDoc=rst.getString("id_tipodoc");
	        		   tipoUso=rst.getString("tipo_uso");
	 			   }
	        	   else 
	        		 throw new Exception("CSS_CartellaMaint::getDatiModelli() - Select fallita - SQL:"+sStm.toString());                              
	           }
	           catch (Exception e) {     
	        	throw e;   
	            //throw new Exception("CSS_CartellaMaint::select\n" + e.getMessage());           
	           }
	   } 
	   
	   /**
		 * Recupero alcuni dati del modello.
		 * 
		 */
	   private void getDatiModelli(String cm,IDbOperationSQL dbOp) throws Exception
	   {
	           try
	           {          
	        	   StringBuffer sStm = new StringBuffer();
	        	   sStm.append("select 'GDCLIENT'||to_char(CODR_SQ.nextval) cr ");
	        	   if(tipoUso.equals("R"))
	        		 sStm.append("from modelli where codice_modello='QueryStandard'");
	        	   else
	        		 sStm.append("from modelli where codice_modello= :cm");
	        	   
	        	   dbOp.setStatement(sStm.toString());
	        	   dbOp.setParameter(":cm", cm);
	        	   dbOp.execute();
	        	   ResultSet rst = dbOp.getRstSet();
	        	   if (rst.next()) {				     
				     cr=rst.getString("cr");
	 			   }
	        	   else 
	        		 throw new Exception("CSS_CartellaMaint::getDatiModelli() - Select fallita - SQL:"+sStm.toString());                              
	           }
	           catch (Exception e) {     
	        	throw e;   
	            //throw new Exception("CSS_CartellaMaint::select\n" + e.getMessage());           
	           }
	   } 
	   
	   /**
		 * Recupero alcuni dati del protocollo.
		 * 
		 */
	   private String getProtocollo(String cm,IDbOperationSQL dbOp) throws Exception
	   {
	           try
	           {          
	        	   StringBuffer sStm = new StringBuffer();
	        	   sStm.append("select protocollo ");
	        	   if(tipoUso.equals("R"))
		        	 sStm.append("from modelli where codice_modello='QueryStandard'");
	        	   else	  
	        	     sStm.append("from modelli where codice_modello=:cm");
	        	   dbOp.setStatement(sStm.toString());
	        	   dbOp.setParameter(":cm", cm);
	        	   dbOp.execute();
	        	   ResultSet rst = dbOp.getRstSet();
	        	   if (rst.next()) {
					 return rst.getString(1);
	        	   }
	        	   else 
	        	    throw new Exception("CSS_CartellaMaint::getProtocollo() - Select fallita - SQL:"+sStm.toString());                              
	  	       }
		       catch (Exception e) {    
		    	 throw e;
		         //throw new Exception("CSS_CartellaMaint::getProtocollo\n" + e.getMessage());           
		       }
	   }
	   
	   /**
		 * Setta la variabile URL.
		 * 
		 */
	   private void SetUrl(String tipoCartella,String InsUpd,String codMod)
	   {
	           /** CASO PROTOCOLLO */
	           if( (protocollo.equals("S")) &&  (Provenienza.equals("C")) )
	           { 
	            Url=Url.substring(0,Url.indexOf("common")+6);
			    Url+="/ClosePageAndRefresh.do?idQueryProveninez=-1";
	           }
	           else 
	           {
	            /** Controlla il tipo cartella (USER o SYSTEM) */
	            String TipoWorkSpace= checkTipoCartella();
	            Url=Url.substring(0,Url.indexOf("common")+6);
	            
	            String cl="";
	            
	            if(creaLink!=null && (creaLink.equals("N")))
	             cl="&CREA_LINK=N";		            
	           
	            if((tipoUso!=null) && (tipoUso.equals("R"))) 
	              Url+="/PostCartellaMaint.do?LinkD=N&TipoCartella="+tipoCartella+"&idCartellaProvenienza="+identifierUpFolder+"&idCartella="+idCartella+"&InsUpd="+InsUpd+"&Provenienza="+Provenienza+"&area=GDMSYS&cm=QueryStandard&tipoUso="+tipoUso+"&areaRicerca="+ar+"&cmRicerca="+cm+"&TipoWorkSpace="+TipoWorkSpace+cl+"&Url="+Url;
		        else
	        	  Url+="/PostCartellaMaint.do?LinkD=N&TipoCartella="+tipoCartella+"&idCartellaProvenienza="+identifierUpFolder+"&idCartella="+idCartella+"&InsUpd="+InsUpd+"&Provenienza="+Provenienza+"&area="+ar+"&cm="+codMod+"&tipoUso="+tipoUso+"&TipoWorkSpace="+TipoWorkSpace+cl+"&Url="+Url;
	           }
       }
	   
	   /**
		 * Costruisce il messaggio da ritornare.
		 * 
		 */
	   private String SetMessage(String TipoWorkSpace,String InsUpd) throws Exception
	   {
	           String tipoAbi;
	           String idOggetto;
	          
	           if(compM==null)
	           {
	        	if(Provenienza.equals("W"))
	        	{
	        		compM="S";
	        	}
	        	else
	        	{	
		        	vu.connect();
		        	try {	        	
			            if(Provenienza.equals("C"))
			            {
			             tipoAbi=Global.ABIL_CARTELLA;
			             idOggetto = (new DocUtil(vu)).getIdViewCartellaByIdCartella(idCartella);
			             if (verificaCompetenza(tipoAbi,idOggetto,Global.ABIL_MODI))
			               compM="S";
			            }
			            else 
			            { 
			              tipoAbi=Global.ABIL_QUERY;
			              idOggetto=idCartella.substring(1,idCartella.length());
			              if (verificaCompetenza(tipoAbi,idOggetto,Global.ABIL_MODI))
			                compM="S";
			            }
		        	}
		        	catch (Exception e) {
		        		vu.disconnectClose();
		        		throw e;
		        		//throw new Exception("CC_CartellaMaint::SetMessage\n"+e.getMessage());
		        	}
		        	vu.disconnectClose();
	        	 }
	           	}
	           	
	           String cr=idCartella;
	           /** Sono su una workspace */
	           if (Long.parseLong(idCartella)<0 && (Provenienza.equals("C")) || (Provenienza.equals("W")))
	        	   cr="WRKSP"+idCartella;
	           if((Provenienza.equals("C")) || (Provenienza.equals("W")))
	        	 return "../restrict/ServletModulisticaCartella.do?id="+idCartella+"&rw="+rw+"&cm="+cm+"&area="+ar+"&InsUpd="+InsUpd+"&cr="+cr+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&compM="+compM+"&page="+page+"&GDC_Link="+URLEncoder.encode(Url);
	           else
	         	 return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm="+cm+"&area="+ar+"&InsUpd="+InsUpd+"&cr="+cr+"&Provenienza="+Provenienza+"&tipoUso="+tipoUso+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page="+page+"&compM="+compM+"&GDC_Link="+URLEncoder.encode(Url);
	    }
	   
	   private String SetMessageForOnClick(String TipoWorkSpace,String InsUpd) throws Exception
	   {
	           compM="N";
	           page="M";
	         
	           if(Provenienza.equals("W"))
	          	return "../restrict/ServletModulisticaCartella.do?id="+idCartella+"&rw="+rw+"&cm="+cm+"&area="+ar+"&InsUpd="+InsUpd+"&cr=WRKSP"+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&compM="+compM+"&page="+page+"&GDC_Link="+URLEncoder.encode(Url);
	           else
	        	 if(Provenienza.equals("C"))
	        	   return "../restrict/ServletModulisticaCartella.do?id="+idCartella+"&rw="+rw+"&cm="+cm+"&area="+ar+"&InsUpd="+InsUpd+"&cr="+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&compM="+compM+"&page="+page+"&GDC_Link="+URLEncoder.encode(Url);
	        	 else {
	        	   if (tipoUso.equals("R")) 
	        	   	  // return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm="+cm+"&area="+ar+"&InsUpd="+InsUpd+"&cr="+idCartella+"&tipoUso="+tipoUso+"&areaRicerca="+ar+"&cmRicerca="+cm+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=M&compM="+compM+"&GDC_Link="+URLEncoder.encode(Url);
	          	      return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm=QueryStandard&area=GDMSYS&InsUpd="+InsUpd+"&cr="+idCartella+"&tipoUso="+tipoUso+"&areaRicerca="+ar+"&cmRicerca="+cm+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=M&compM="+compM+"&GDC_Link="+URLEncoder.encode(Url);
		           else
	        		   return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm="+cm+"&area="+ar+"&InsUpd="+InsUpd+"&cr="+idCartella+"&tipoUso="+tipoUso+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=P&compM="+compM+"&GDC_Link="+URLEncoder.encode(Url);
	        	 }
	   }
	   
	   private String SetMessageForNew(String TipoWorkSpace,String InsUpd,String area,String cm,String cr) throws Exception
	   {
	           page="P";
	           compM="N";
	           rw="W";
	           
	           if(Provenienza.equals("W"))
	        	 return "../restrict/ServletModulisticaCartella.do?id="+idCartella+"&rw="+rw+"&cm="+cm+"&area="+area+"&InsUpd="+InsUpd+"&cr=WRKSP"+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&compM="+compM+"&page="+page+"&GDC_Link="+URLEncoder.encode(Url);
	           else
	        	 if(Provenienza.equals("C"))
	        		return "../restrict/ServletModulisticaCartella.do?id="+idCartella+"&rw="+rw+"&cm="+cm+"&area="+area+"&InsUpd="+InsUpd+"&cr="+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&compM="+compM+"&page="+page+"&GDC_Link="+URLEncoder.encode(Url);
	        	 else {
	        		if (tipoUso.equals("R"))  
	        			//return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm="+cm+"&area="+area+"&InsUpd="+InsUpd+"&cr="+idCartella+"&tipoUso="+tipoUso+"&areaRicerca="+ar+"&cmRicerca="+cm+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=M&compM=N&GDC_Link="+URLEncoder.encode(Url);
	        			return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm=QueryStandard&area=GDMSYS&InsUpd="+InsUpd+"&cr="+idCartella+"&tipoUso="+tipoUso+"&areaRicerca="+ar+"&cmRicerca="+cm+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=M&compM=N&GDC_Link="+URLEncoder.encode(Url);
	        		else
	        			return "../restrict/ServletModulisticaCartella.do?id="+idCartella.substring(1,idCartella.length())+"&rw="+rw+"&cm="+cm+"&area="+area+"&InsUpd="+InsUpd+"&cr="+idCartella+"&tipoUso="+tipoUso+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page="+page+"&compM=N&GDC_Link="+URLEncoder.encode(Url);
	        	 }
       }
	   
	   /**
		 * Controlla il tipo cartella (USER o SYSTEM).
		 * 
		 */
	   private String checkTipoCartella()
	   {
	           String t="";
	           if (workspace!=null && workspace.equals("-1")) 
	            t="S"; 	   
	           else
	        	t="U";
	           return t;	
	   }  
	   
	   /**
		 * Verifica la competenza "tipoCompetenza" sull'oggetto.
		 * 
		 */
	   private boolean verificaCompetenza(String tipoAb,String idCartella,String tipoCompetenza) throws Exception
	   {
		   	   try 
		   	   {
		   		 Abilitazioni abilitazione = new Abilitazioni(tipoAb, idCartella , tipoCompetenza); 
		   		 UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(), vu.getPwd(),  vu.getUser(), vu);
		   		 if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,abilitazione)  == 1 ) return true;
		   		 return false;
		   	   }
		   	   catch (Exception e) {
		   		 log.log_error("CSS_CartellaMaint::verificaCompetenza() - idCartella:"+idCartella+" - tipoAbilitazione:"+tipoAb+" - tipoCompetenza:"+tipoCompetenza+"\n");
		   		 throw e;  
		   		 //throw new Exception("CSS_CartellaMaint::verificaCompetenza(@,@)\n" + e.getMessage());
		   	   }                  
	   }
	   
	   /**
		 * Costruzione del tabfolder.
		 * 
		 */
	   public boolean _getTabMenu() throws Exception
	   { 
	          /** Controlla il tipo cartella (USER o SYSTEM) */ 
	          String TipoWorkSpace= checkTipoCartella();
	          IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	          vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
	          String cdr;  
	          if(Provenienza.equals("Q"))
	           cdr="-";
	          else
	           cdr="";
	          String url_page="";
	          if(Url!=null)
	        	url_page="&GDC_Link="+URLEncoder.encode(Url);
	          
	          /** Costruzione dei link ai tabfolder */
	          String linkP="";
	          String linkM="";        
	    
	          
	          if (tipoUso.equals("R"))  
	          {	  
	        	linkP="ServletModulisticaCartella.do?id="+idCartella+"&compM=S&rw=R&cm="+cm+"&area="+ar+"&tipoUso="+tipoUso+"&InsUpd="+insertUpdate+"&cr="+cdr+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=P"+url_page;          
	        	linkM="ServletModulisticaCartella.do?id="+idCartella+"&compM=S&rw=W&cm="+cm+"&area="+ar+"&tipoUso="+tipoUso+"&InsUpd="+insertUpdate+"&cr="+cdr+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=M"+url_page;          
	     	  }
	          else
	          {
	        	linkP="ServletModulisticaCartella.do?id="+idCartella+"&compM=S&rw=R&cm="+cm+"&area="+ar+"&InsUpd="+insertUpdate+"&cr="+cdr+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=P"+url_page;          
	            linkM="ServletModulisticaCartella.do?id="+idCartella+"&compM=S&rw=W&cm="+cm+"&area="+ar+"&InsUpd="+insertUpdate+"&cr="+cdr+idCartella+"&Provenienza="+Provenienza+"&TipoWorkSpace="+TipoWorkSpace+"&idCartProveninez="+identifierUpFolder+"&page=M"+url_page;          
	  	    
	          }   
	         
	          try
	          {          
	            StringBuffer sStm = new StringBuffer();
	            sStm.append("select	decode(:COMPM, 'S', F_TAB_FOLDER('Proprietà',:LINKP");
	            sStm.append(", 'proprietaC.gif', 'Proprietà', decode(:PAGE,'P','S','N')), ");
	            sStm.append("F_TAB_FOLDER('Proprietà', '#', 'proprietaC.gif', 'Proprietà', 'S')) folder1, ");
	            sStm.append("decode(:COMPM,'S',F_TAB_FOLDER('Modifica',:LINKM");
	            sStm.append(", 'modifica.gif', 'Modifica', decode(:PAGE,'M','S','N')), F_TAB_FOLDER('Modifica','#','modifica.gif','Modifica','N')) folder2");
	            sStm.append(" from dual ");	
	            dbOp.setStatement(sStm.toString());
	            dbOp.setParameter(":COMPM",compM);
	            dbOp.setParameter(":LINKP",linkP);
	            dbOp.setParameter(":PAGE",page);
	            dbOp.setParameter(":LINKM",linkM);
	            dbOp.execute();
	            ResultSet rst = dbOp.getRstSet();
	 		 	if (rst.next()) {
				   folder1=rst.getString(1);
				   folder2=rst.getString(2);
	               CCS_common.closeConnection(dbOp);
	               return true;
	            }
	            else 
	              throw new Exception("CSS_CartellaMaint::_getTabMenu() - Select fallita - SQL:"+sStm.toString());      
	          }
	          catch (Exception e) {           
	            CCS_common.closeConnection(dbOp);
	            throw e;
	            //throw new Exception("CSS_CartellaMaint::_getTabMenu\n" + e.getMessage());           
	          }
	   }
	   
	   /**
		 * Restituisce il tabfolder.
		 * 
		 */
	   public String _getFolder1()
	   {
	          return folder1;
	   }
  
	   /**
		 * Restituisce il tabfolder.
		 * 
		 */
	   public String _getFolder2()
	   {
		   	  return folder2;
	   }	
	   	   
}
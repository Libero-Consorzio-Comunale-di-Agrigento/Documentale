package it.finmatica.dmServer.jdms;


/**
 * Gestione inserimento o aggiornamento di una Cartella o Query.
 * Classe di servizio per la gestione del Client
*/
   
import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.jdms.CCS_WorkSpace;
import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.jdms.CCS_Common;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.jfc.dbUtil.*;
import java.net.URLEncoder;


public class CCS_PostCartellaMaint 
{
	   /**
	    * Variabili private
	   */	
	   String nomeObject;
	   String InsUpd,idObject;  
	   String identifierUpFolder;
	   String Provenienza,tipoObject ;
	   String area,cm,tipoUso,areaRicerca,cmRicerca;
	   String TipoWorkSpace;
	   String Url;
	   String ruolo;
	   String LinkD;
	   String creaLink="";
	   CCS_Common CCS_common;
	   private Environment vu;  
	   private IDbOperationSQL dbOp;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;

	   /**
		 * Costruttore utilizzato per inserimento 
		 * o aggiornamento di una Cartella o Query.
		 * 
		 */
	   public CCS_PostCartellaMaint(String newidObject, String newidentifierUpFolder,String newProvenienza, String newtipoObject,
	                                String newInsUpd,String ar,String codMod,String newtipoUso,String newTipoWorkSpace,String newUrl,
	                                String newruolo,String newLinkD,String aRic,String cRic,String clink,CCS_Common newCommon) throws Exception
	   {
		      idObject=newidObject;
		      identifierUpFolder=newidentifierUpFolder;   
		      Provenienza=newProvenienza;
		      tipoObject=newtipoObject;    
		      InsUpd=newInsUpd;
		      area=ar;
		      cm=codMod;
		      areaRicerca=aRic;
		      cmRicerca=cRic;
		      tipoUso=newtipoUso;
		      TipoWorkSpace=newTipoWorkSpace;
		      Url=newUrl;
		      ruolo=newruolo;
		      LinkD=newLinkD;
		      if(clink!=null)
		       creaLink=clink;
		      CCS_common=newCommon;  
		      log= new DMServer4j(CCS_PostCartellaMaint.class,CCS_common); 
	   }
	   
	   /**
		 * Gestione inserimento o aggiornamento di una Cartella o Query.
		 * 
		 */
	   public String _afterInitialize() throws Exception 
	   {
		   	  dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
                        
		   	  try
		   	  { 
		   		/** Controllo se bisogna effettuare la Insert oppure Update dell'oggetto (Cartella o Query) */
		        if (InsUpd.equals("I")) 
		          InsertObject();			
		        else 
		          UpdateObject();
              CCS_common.closeConnection(dbOp,true);               
              /** Controllo la provenienza (Cartella o Query) per effettuare la Redirect */
	          if (Provenienza.equals("C") || Provenienza.equals("W")) 
	            return "../common/ClosePageAndRefresh.do?Provenienza="+Provenienza;
	          else 
	          {
	        	 /** Questo controllo permette di distinguere se mi trovo all'interno del Client Documentale
	        	  	 oppure in un Link Diretto a ServletModulisticaCartella di una Query che non sia di tipo "R" */
	        	 if((LinkD.equals("N")) && (!tipoUso.equals("R")))
	        	  return  "../restrict/ServletModulisticaCartella.do?id="+idObject+"&rw=W&InsUpd=U&Provenienza=Q&page=M&compM=S&idCartProveninez="+identifierUpFolder+"&area="+area+"&cm="+cm+"&cr=-"+idObject+"&TipoWorkSpace="+TipoWorkSpace+"&GDC_Link="+URLEncoder.encode(Url);
	        	 else
	        	  return  "../common/ClosePageAndRefresh.do?idQueryProveninez="+idObject;
	          }    
		     }
		     catch (Exception e) 
		     {
		        CCS_common.closeConnection(dbOp,false);   
		        if(e.getMessage().indexOf("ORA-20008")!=-1 && (Provenienza.equals("W"))){
		          if (InsUpd.equals("I")) 
		        	throw new Exception("Impossibile inserire l'area di lavoro "+nomeObject+"\n. Esiste già un area di lavoro con lo stesso nome! \n");   
		          else
		        	throw new Exception("Impossibile modificare l'area di lavoro "+nomeObject+"\n. Esiste già un area di lavoro con lo stesso nome! \n");   
			    } 
		        else
		         throw e;
		        //throw new Exception("CCS_PostCartellaMaint::_afterInitialize\n"+e.getMessage());
		     }
	   }
	      
	   
	   
	   /**
		 * Gestione inserimento di una Cartella o Query.
		 * 
		 */
	   private void InsertObject() throws Exception {
            
	          String identifierViewFolder;
	          String nome;         
      
	          try 
	          {
	        	  if(Provenienza.equals("W"))	  
	    	    	identifierViewFolder = (new DocUtil(vu)).getIdDocumentoByAreaCmCr(area,cm,"WRKSP-"+idObject);
	    	      else
	    	       if (Provenienza.equals("C"))
	                  identifierViewFolder = (new DocUtil(vu)).getIdDocumentoByAreaCmCr(area,cm,idObject);
	               else
	                  identifierViewFolder = (new DocUtil(vu)).getIdDocumentoByAreaCmCr(area,cm,"-"+idObject);
	        	  
	        	  //E' stata abortito l'inserimento
	        	  if (identifierViewFolder.equals("")) return;
	        	  
	              AccediDocumento ad = new AccediDocumento(identifierViewFolder,vu);
	              ad.accediDocumentoValori();  
	              nome=ad.leggiValoreCampo("NOME");    
	    
	              if (nome==null) {
	                 if( (Provenienza.equals("C")) || (Provenienza.equals("W")))
	                     nomeObject="CARTELLA "+idObject;
	                 else
	                     nomeObject="QUERY "+idObject;
	              }
	              else
	                 nomeObject=nome;
	         
	              /** Controllo per eventuali cararatteri speciali */
	              if(Provenienza.equals("W")) 
	              {
	             	CCS_WorkSpace cart= new CCS_WorkSpace(vu.getUser(),ruolo,new CCS_Common("jdbc/gdm",vu.getUser()));
	                cart.insert(nomeObject,area,cm,identifierViewFolder,idObject);	 
	              }
	              else 
	              {
	            	  if(Provenienza.equals("C"))
	            	  {                 
	            		  ICartella Ic = new ICartella(null,null,null,null,nomeObject); 
	            		  Ic.initVarEnv(vu);
	            		  Ic.setIdentifierFolder(idObject);
	            		  Ic.setProfileFolder(identifierViewFolder);  
	            		  Ic.setNameFolder(nomeObject);
	            		  Ic.setIndentifierUpFolder(identifierUpFolder);
	            		  Ic.setRootFolder(tipoObject);
	            		  if(creaLink!=null && (creaLink.equals("N")))
	            			Ic.insert(false);
	            		  else
	            		    Ic.insert();
		              }
		              else {
		                  if (identifierUpFolder.equals("-2"))
		            	    tipoObject="U";
		            	  else
		            		tipoObject="S";
		                  IQuery Iq = new  IQuery(null,null,tipoObject,nomeObject);
			              Iq.initVarEnv(vu);
			              Iq.setIdentifierQuery(idObject);
			              Iq.setFiltro("RICERCAMODULISTICA_"+areaRicerca+'@'+cmRicerca);
			              Iq.setProfileQuery(identifierViewFolder);
			              Iq.insert();
		                  /** .....La aggiungo alla cartella padre */
			              ICartella Ic = new ICartella(identifierUpFolder);                 
			              Ic.initVarEnv(vu);
			              Ic.addInObject(idObject,"Q");                 
			              Ic.update();
		              }
	              }   
	    	   
	         }
	         catch (Exception e) 
	         {  
	           log.log_error("CCS_PostCartellaMaint::InsertObject() - Provenienza:"+Provenienza+" - Oggetto da inserire - idOggetto:"+idObject+" - Area:"+area+" - CM:"+cm+" - NomeOggetto:"+nomeObject+" - Cartella Padre:"+identifierUpFolder); 
	           throw e; 
	           //throw new Exception("CCS_PostCartellaMaint::_afterInitialize - InsertObject\n"+e.getMessage());
	         }
	   }
	   
	   /**
		 * Gestione aggiornamento di una Cartella o Query.
		 * 
		 */
	   private void UpdateObject()throws Exception
	   {
                
	          String identifierViewFolder;
	          String nome;
	          try
	          {
		    	
	        	  if (Provenienza.equals("C"))
            	  	identifierViewFolder = (new DocUtil(vu)).getIdDocumentoByCr(idObject);
                 else if (Provenienza.equals("W"))
                	 identifierViewFolder = (new DocUtil(vu)).getIdDocumentoByCr("WRKSP-"+idObject);
                 else
                	 identifierViewFolder = (new DocUtil(vu)).getIdDocumentoByCr("-"+idObject);
          
	             AccediDocumento ad = new AccediDocumento(identifierViewFolder,  vu);
	             ad.accediDocumentoValori();  
	             nome= ad.leggiValoreCampo("NOME");    
	             if (nome==null) {
	               if (Provenienza.equals("C"))
                     nomeObject="CARTELLA "+idObject;
	               else
                     nomeObject="QUERY "+idObject;
	             }
	             else
                   nomeObject=nome;
          
	             /** Controllo per eventuali cararatteri speciali */
	             //nome=Global.replaceAll(nome,"'","''");
	             if ((Provenienza.equals("W")) || (Provenienza.equals("C"))) 
	             {
	            	 ICartella Ic;
	            	 if (Provenienza.equals("W"))
	            		Ic = new ICartella("-"+idObject); 
	            	 else
	            		Ic = new ICartella(idObject);
	            	 Ic.initVarEnv(vu);
	            	 Ic.addValue("NOME",nomeObject);
	            	 Ic.update();
	             }
	             else
	             {
	            	 IQuery Iq = new IQuery(idObject);
	            	 Iq.initVarEnv(vu);
	            	 Iq.addValue("NOME",nomeObject);
	            	 Iq.setUpdateFilter(false);
	            	 Iq.update();
	             }
	             
		    	
             }
	         catch (Exception e) 
	         {         
	           //throw new Exception("CCS_PostCartellaMaint::_afterInitialize - UpdateObject\n"+e.getMessage());
	           log.log_error("CCS_PostCartellaMaint::UpdateObject() - Provenienza:"+Provenienza+" - Oggetto da aggiornare - idOggetto:"+idObject+" - NomeOggetto:"+nomeObject); 
		       throw e; 
		     }
	   }
}
package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.*;
import java.util.ArrayList;

/**
 * Gestione eliminazione link agli oggetti .
 * Classe di servizio per la gestione del Client
*/

public class CCS_EliminaLinkDoc 
{
	   /**
	    * Variabili private
	   */	
	   String idDocumento;
	   ArrayList list=null;
	   String idCartella;
	   String[] vLink=null;
	   CCS_Common CCS_common;
	   IDbOperationSQL dbOp =null;
	   Environment vu = null;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato per la gestione dei link
		 * all'oggetto di tipo Documento.
		 * 
		 */
	   public CCS_EliminaLinkDoc(String newidDocumento,ArrayList newlist,CCS_Common newCommon) throws Exception
	   {
		   	  idDocumento=newidDocumento;
		   	  list=newlist;
		   	  CCS_common=newCommon;
		   	  log= new DMServer4j(CCS_EliminaLinkDoc.class,CCS_common); 
	   }
	   
	   /**
		 * Costruttore utilizzato per la gestione dei link
		 * all'oggetto di tipo Cartella.
		 * 
		 */
	   public CCS_EliminaLinkDoc(String newidCartella, String newidLinkToSplit, CCS_Common newCommon) throws Exception
	   {
		   	  idCartella=newidCartella;
		   	  vLink=Global.Split(newidLinkToSplit,"@");    
		   	  CCS_common=newCommon;
		   	  log= new DMServer4j(CCS_EliminaLinkDoc.class,CCS_common); 
	   }
	   
	   /**
		 * Costruzione della lista di collegamenti a Cartelle.
		 * 
		 */
	   private boolean buildList() 
	   {
		   	   String[] v=null;
		   	   String slink="";
		   	   if((list!=null) && (list.size()!=0))
		   	   {	  
		   		 for(int i=0;i<list.size();i++)
		   		 {
		   			v=list.get(i).toString().split(",");
		   			String id="",cb="";
		   			for(int j=0;j<v.length;j++)
		   			{	 
		   			  if(v[j].indexOf("ID_OGGETTO")!=-1)
				       id=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
					  if(v[j].indexOf("CheckBox_Delete")!=-1)
				       cb=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
		   			} 
		   			if(cb.equals("Y"))
		   			  slink+="C"+id+"@";
		   		 }
		   		 if(!(slink.equals(""))) 
                  vLink =Global.Split(slink,"@");
		   		 return true;
		   	   }  
		   	   return false;
	   }
	   
	   /**
		 * Eliminazione della lista di collegamenti a Cartelle.
		 * 
		 */
	   public void _onclick_deleteLink() throws Exception
	   {
		   	  if((buildList())&& (vLink!=null))
		   	  {	 
		   		for(int i=0;i<vLink.length;i++)
		   		{
	             idCartella=vLink[i].substring(1,vLink[i].length());				 
	             deleteLink();
		  		}
		   	  }
       }
	   
	   /**
		 * Eliminazione dell'oggetto dalla lista della Cartella.
		 * 
		 */
	   private void deleteLink() throws Exception 
	   {
		   	   if (!CCS_common.dataSource.equals("")) {
		   		   dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   		   vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
		   	   }
		   	   else 
		   	   {
		   		   vu=CCS_common.ev;
		   		   dbOp=CCS_common.ev.getDbOp();
		   	   }   
	  
		   	   ICartella Ic = null; 
	           try
	           {
	        	   Ic = new ICartella(idCartella);
	        	   Ic.initVarEnv(vu); 
	        	   Ic.deleteInObject(idDocumento,"D");
	           }
	           catch (Exception e) {
	        	   CCS_common.closeConnection(dbOp,false);
	        	   log.log_error("CCS_EliminaLinkDoc::deleteLink() - idCartella:"+idCartella+" - idDocumento:"+idDocumento);
	        	   throw e;
	        	   //throw new Exception("CCS_EliminaLinkDoc::deleteLink, creazione ICartella\n"+e.getMessage());
	           }
	           
	           try {   
	        	   Ic.update();
	        	   CCS_common.closeConnection(dbOp,true);
	           }
	           catch (Exception e) {
	        	   CCS_common.closeConnection(dbOp,false);             
	        	   log.log_error("CCS_EliminaLinkDoc::deleteLink() - idCartella:"+idCartella+" - idDocumento:"+idDocumento);
	        	   throw e;
	        	   //throw new Exception("CCS_EliminaLinkDoc::deleteLink\n"+e.getMessage());
	           }
	   }
	   
	   /**
		 * Eliminazione dell'oggetto dalla lista della Cartella.
		 * 
		 */
	   public void _afterInitialize_deleteLink() throws Exception
	   {
		   	  String tipoOggetto,id;
         
		   	  if (!CCS_common.dataSource.equals("")) {
	             dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	             vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
		   	  }
		   	  else 
		   	  {
	             vu=CCS_common.ev;
	             dbOp=CCS_common.ev.getDbOp();
		   	  }   
         
		   	  ICartella Ic = null;
         
		   	  try 
		   	  {          	          
		   		Ic = new ICartella(idCartella);
		   		Ic.initVarEnv(vu);                                     
		   		for(int i=0;i<vLink.length;i++)
		   		{
	             int index=0;
	             if (vLink[i].indexOf("Q")!=-1) //E' una query
	             {
	                tipoOggetto="Q";
	                index=1;
	             }
	             else
	               if (vLink[i].indexOf("X")!=-1) //E' una Cartella Collegata
	               {
	                  tipoOggetto="X";
	                  index=1;
	               }
	               else //E' un documento
	               {
	            	   tipoOggetto="D";
	                 index=1;
	               }
	             id=vLink[i].substring(index,vLink[i].length());				 
	             Ic.deleteInObject(id,tipoOggetto); 
		   		}
		   	 }
		     catch (Exception e) 
		     {
		    	 CCS_common.closeConnection(dbOp,false); 
		    	 log.log_error("CCS_EliminaLinkDoc::afterInitialize_deleteLink() - idCartella:"+idCartella+" - Lista Oggetti:"+vLink.toString());
	        	 throw e;
	        	 //throw new Exception("CCS_EliminaLinkDoc::_afterInitialize_deleteLink - Ciclo su ICartella\n"+e.getMessage());
		     }	         
         
	         try {   
	           Ic.update();
	           CCS_common.closeConnection(dbOp,true);
	         }
	         catch (Exception e) 
	         {
	           CCS_common.closeConnection(dbOp,false);             
	           log.log_error("CCS_EliminaLinkDoc::afterInitialize_deleteLink() - idCartella:"+idCartella+" - Lista Oggetti:"+vLink.toString());
	           throw e;
	           //throw new Exception("CCS_EliminaLinkDoc::_afterInitialize_deleteLink - Update\n"+e.getMessage());
	         }
  }
}
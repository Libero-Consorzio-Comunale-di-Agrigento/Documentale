package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.GestioneOrdinamentiCartelle;
import it.finmatica.jfc.dbUtil.*;
import java.sql.*;

/**
 * Gestione dei Documenti non utilizzata.
 * Classe di servizio per la gestione del Client
*/

public class CCS_TreeSelezionaDocumento 
{
	   /**
	    * Variabili private
	   */	 
	   String idCartProvenienza,Provenienza;
	   String WkrSp,idQueryProvenienza;
	   String IdDocumento;
	   String area,cr,cm,crea_link="";
	   String IDLink;
	   CCS_Common CCS_common;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   private IDbOperationSQL dbOp;
	   private Environment vu;


	/**
		 * Costruttore generico.
		 */
	   public CCS_TreeSelezionaDocumento(String newidCartProvenienza,String newProvenienza,String newWkrSp,String newidQueryProvenienza,
                                    	 String newarea,String newcr,String newcm,String newcrea_link,CCS_Common newCommon) throws Exception
	   {
	         idCartProvenienza=newidCartProvenienza; 
	         Provenienza=newProvenienza;
	         WkrSp=newWkrSp;
	         idQueryProvenienza=newidQueryProvenienza;                
	         area=newarea;
	         cr=newcr;
	         cm=newcm;
	         if(newcrea_link!=null)
	           crea_link=newcrea_link;
	         CCS_common=newCommon;
	         log= new DMServer4j(CCS_TreeSelezionaDocumento.class,CCS_common);
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
	   
	   /**
		 * Costruttore generico.
		 */
	   public String _afterInitialize() throws Exception
	   {
		   	  String message;
		   	  boolean commit = false;
		   	  
		   	  if(crea_link.equals("N"))
		   	  {
		   		  message=SetMessage();
		   	  }
		   	  else
		   	  {	  
			   	  try
			   	  {   
			   		if(idCartProvenienza.indexOf("C")==-1)
			   		  idCartProvenienza="C"+idCartProvenienza;
	          
		            try {
		                this.getDatiDocumento();
		            }
		            catch (Exception e) {
		                throw new Exception("CCS_TreeSelezionaDocumento::_afterInitialize - getDatiDocumento\n" + e.getMessage());
		            }
	
	    			if (idCartProvenienza==null)
	    			{
		             if (WkrSp.equals("1"))
	                   IDLink="-1";			   
			    	     else 
	                  if (WkrSp.equals("2")) 
			                  IDLink="-2";			   
			    	        else 
				 	          IDLink="-3";
					}
					else
					{                     
					  IDLink=idCartProvenienza;
					  IDLink=IDLink.substring(1,IDLink.length());
					}
	    			
	    			/** L'inserimento del link del Documento alla Cartella avviene soltanto 
	    			 *  se la richiesta di creazione del Documento viene effettuata all'interno 
	    			 *  di una cartella, altrimenti nel caso di una Query il documento non viene 
	    			 *  associato a nessuna cartella. */
	    			//if(idQueryProvenienza.equals("-1"))
	    			
	    			if((idQueryProvenienza.equals("-1")) && (Provenienza.equals("C")))
	    	    		InsertLink();
	    		
	    			/** Setto il messaggio di ritorno per effettuare la Redirect */
	    			message=SetMessage();
	    			commit=true;
		         }
		         catch (Exception e) {
		             commit=false;
		             throw e;
		         }
			   	 finally {
			   	  	_finally(commit);
				 }
		     }
             return message;
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

	   /**
		 * Costruzione del messaggio da ritornare.
		 */
	   private String SetMessage()
	   {
		   	   if (idCartProvenienza!=null)
		   	   if (Provenienza.equals("Q"))
				  return "ClosePageAndRefresh.do?idQueryProveninez="+idQueryProvenienza;
			   else
				  return "ClosePageAndRefresh.do?idQueryProveninez=-1";
		   	   return ""; 
	   }
	   
	   /**
		 * Recupera alcuni dati del Documento.
		 */
	   private void getDatiDocumento() throws Exception
	   {
		       StringBuffer sStm=null; 	  
		       try
		   	   {          
		    	   sStm= new StringBuffer();
		   		   sStm.append("select to_char(id_documento) from documenti d,tipi_documento td ");
		   		   sStm.append("where d.id_tipodoc=td.id_tipodoc and ");
		   		   sStm.append("d.area = :AREA and d.codice_richiesta = :CR and ");
		   		   sStm.append("td.nome=(select nvl(codice_modello_padre,codice_modello) from modelli where codice_modello = :CM and area = :AREA)  ");
		   		   dbOp.setStatement(sStm.toString());
		   		   dbOp.setParameter(":AREA",area);
		   		   dbOp.setParameter(":CR",cr);
		   		   dbOp.setParameter(":CM",cm);
		   		   dbOp.execute();
		   		   ResultSet rst = dbOp.getRstSet();
		   		   if (rst.next()) 
		   			 IdDocumento=rst.getString(1);
		   		   else 
		   			 throw new Exception("Select fallita per cr="+cr+" - cm="+cm+" - area="+area);                              
		   	   }
		   	   catch (Exception e) {           
		   		   log.log_error("CCS_TreeSelezionaDocumento::getDatiDocumento() - SQL:"+sStm.toString());
		   		   throw e;
		   	   }
	   }
	   
	   /**
		 * Inserimento del Collegamento
		 */
	   private void InsertLink() throws Exception
	   {
		   	   StringBuffer sStm=null;
		   	   try
		   	   {
					int i=dbOp.getNextKeyFromSequence("LINK_SQ");
					sStm= new StringBuffer();
					sStm.append("INSERT INTO LINKS (ID_LINK, ID_CARTELLA, ID_OGGETTO, TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)");
					sStm.append(" VALUES ( :SEQUENZA , :IDLINK , :IDDOCUMENTO, 'D', sysdate, :UTENTE ) ");

					dbOp.setStatement(sStm.toString());

					dbOp.setParameter(":SEQUENZA",i);
					dbOp.setParameter(":IDLINK",IDLink);
					dbOp.setParameter(":IDDOCUMENTO",IdDocumento);
					dbOp.setParameter(":UTENTE",vu.getUser());

					dbOp.execute();

					GestioneOrdinamentiCartelle ord =new GestioneOrdinamentiCartelle(vu,IDLink,IdDocumento,"D");
					ord.rebuild(true);
		   	   }
		   	   catch (Exception e) { 
		   		   log.log_error("CCS_TreeSelezionaDocumento::InsertLink() - SQL:"+sStm.toString());
		   		   throw e;
		   	   }
	   }
  
}
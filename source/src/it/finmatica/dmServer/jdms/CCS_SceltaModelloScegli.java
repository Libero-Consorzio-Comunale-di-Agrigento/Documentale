package it.finmatica.dmServer.jdms;

/**
 * Gestione della WorkArea.
 * Classe di servizio per la gestione del Client
*/
 
public class CCS_SceltaModelloScegli 
{
	   String idCartProvenienza,Provenienza;
	   String idQueryProvenienza,idTipoDoc;
	   String cm,area,cr;
	   String Url;
	   CCS_Common CCS_common;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore non al momento utilizzato
		 * dal Client Documentale.
		 * 
		 */
	   public CCS_SceltaModelloScegli(String newidCartProvenienza,String newProvenienza,String newidQueryProvenienza,
			   						  String newidTipoDoc,String newUrl,CCS_Common newCommon) throws Exception
	   {
  
		      idCartProvenienza=newidCartProvenienza;
		      Provenienza=newProvenienza;
		      idQueryProvenienza=newidQueryProvenienza;
		      idTipoDoc=newidTipoDoc;
		      Url=newUrl;     
		      SetUrl();
		      CCS_common=newCommon;
		      log= new DMServer4j(CCS_SceltaModelloScegli.class,CCS_common); 
	   }
	   
	   /**
		 * Setta la variabile URL.
		 * 
		 */
	   private void SetUrl()
	   {
		   	   Url=Url.substring(0,Url.indexOf("common")+6);
		   	   
		   	   if (idCartProvenienza==null) 			    
		          Url+="/TreeSelezionaDocumento.do?cm="+cm+"&area="+area+"&cr="+cr;
		   	   else        
		   		 if (Provenienza.equals("Q")) 
		           Url+="/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProvenienza;
				 else
		           Url+="/ClosePageAndRefresh.do?idQueryProveninez=-1";
	   } 
}
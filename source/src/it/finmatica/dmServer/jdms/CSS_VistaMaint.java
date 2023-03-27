package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;

/**
 * Gestione della Query.
 * Classe di servizio per la gestione del Client
*/

public class CSS_VistaMaint 
{
	   /**
	    * Variabili private
	   */
	   String idOggetto;
	   String Filtro;
	   CCS_Common CCS_common;
	   private Environment vu;  
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore generico vuoto.
		 */
	   public CSS_VistaMaint() {}
	   
	   /**
		 * Costruttore utilizzato per la ricerca.
		 */
	   public CSS_VistaMaint(String newidOggetto,String newfiltro,CCS_Common newCommon) throws Exception
	   {
		      idOggetto= newidOggetto; 
		      Filtro= newfiltro;
		      CCS_common=newCommon;
		      log= new DMServer4j(CSS_VistaMaint.class,CCS_common); 
	   }
	   
	   /**
		 * Viene effettuata l'aggiornamento della Query.
		 */
	   public void _OnClick() throws Exception
	   {
		   	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
	  
		   	  try
		   	  {
	            IQuery Iq = new IQuery(idOggetto);            
	            Iq.initVarEnv(vu);
	            Iq.setFiltro(Filtro);
	            Iq.update();
	            CCS_common.closeConnection(dbOp,true);              
		   	  }
		   	  catch (Exception e) 
		   	  {
	            CCS_common.closeConnection(dbOp,false); 
	            throw e;            
	            //throw new Exception("CSS_VistaMaint::_OnClick\n"+e.getMessage());
		   	  }
	   }
	   
	   /**
		 * Costruzione del tab folder.
		 */
	   public String getTabFolder()
	   {
			  String t="";
			  t+="<table cellspacing=\"0\" cellpadding=\"0\"><tr>\n";
			  t+="<td align=\"left\" id=\"rsL\" valign=\"top\" class=\"AFCGuidaSelL\"><img src=\"../Themes/Default/GuidaBlank.gif\" ></td>";
			  t+="<td align=\"left\" id=\"rs\" class=\"AFCGuidaSel\" valign=\"center\" nowrap>";
			  t+="<a class=\"AFCGuidaLink\" title=\"Ricerca Semplice\" href=\"#\" onclick=\"setRicerca('rs');\">Ricerca Semplice </a></td>";
			  t+="<td align=\"left\" id=\"rsR\" valign=\"top\" class=\"AFCGuidaSelR\"><img src=\"../Themes/Default/GuidaBlank.gif\" ></td>\n";
			  t+="<td align=\"left\" id=\"raL\" valign=\"top\" class=\"AFCGuidaL\"><img src=\"../Themes/Default/GuidaBlank.gif\" ></td>";
			  t+="<td align=\"left\" id=\"ra\" class=\"AFCGuida\" valign=\"center\" nowrap>";
			  t+="<a class=\"AFCGuidaLink\" title=\"Ricerca Avanzata\" href=\"#\" onclick=\"setRicerca('ra');\">Ricerca Avanzata</a></td>";
			  t+="<td align=\"left\" id=\"raR\" valign=\"top\" class=\"AFCGuidaR\"><img src=\"../Themes/Default/GuidaBlank.gif\" ></td>";
			  t+="</tr></table>";
			  return t;
	   } 
}
package it.finmatica.dmServer.management;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;

import java.sql.Connection;
import java.util.Vector;

/**
 * Gestione di una collezione di Query del documentale.
 * 
 * @author  G. Mannella
 * @version 2.9 
 *             
*/
public class IQueryCollection {
	   Vector iQuery;
	   Environment vEnv;
	   
	   public IQueryCollection() {
		      iQuery = new Vector();
	   }
	   
	  /**
	    * Metodo da richiamare subito dopo il costruttore<BR>
	    * user e password sono individuati da "AD4"<BR>
	    * ini rappresenta il percorso del file di properties<BR>
	    * in cui sono specificati i parametri di connessione<BR>
	    * 
	    * @param user utente di AD4
	    * @param passwd password di AD4
	    * @param ini percorso del file di properties
	   */   
	   public void initVarEnv(String user,String passwd, String ini) throws Exception {	          
	          vEnv = new Environment(user,passwd,Global.APPL_STANDARD, Global.ENTE_STANDARD, "", ini );   
	   }
   
	   /**
	    * Metodo da richiamare subito dopo il costruttore<BR>
	    * user e password sono individuati da "AD4"<BR>
	    * cn rappresenta la connection
	    * 
	    * @param user utente di AD4
	    * @param passwd password di AD4
	    * @param cn connection
	   */
	   public void initVarEnv(String user,String passwd, Connection cn) throws Exception  {
	          vEnv = new Environment(user,passwd,Global.APPL_STANDARD, Global.ENTE_STANDARD, "", cn );         
	   }   	  	 
	   
	   public void addIQuery(IQuery iq) {
		      iQuery.add(iq);
	   }
	   
	   public IQuery getIQueryRicerca() throws Exception {
		      StringBuffer sSelect = new StringBuffer("");
		      Vector vCampiReturn, vCampiRicerca;
		      
		      if (iQuery.size()==0)
		    	  throw new Exception("IQueryCollection::ricerca - Specificare almeno un IQuery da eseguire");
		      
		      vCampiReturn  =((IQuery)iQuery.get(0)).getCampiOrdinamentoReturn();
		      vCampiRicerca =((IQuery)iQuery.get(0)).getCampiRicerca();
		      
		      IQuery iqInterna = null;
		   
		      for(int i=0;i<iQuery.size();i++) {	
		    	  iqInterna = (IQuery)iQuery.get(i);
		    	  
		    	  iqInterna.escludiOrdinamento(true);
		    	  
		    	  String sql = (iqInterna).getRicercaFinmatica().getSQLSelect();
		    	  		    	
		    	  sSelect.append(sql);
		    	  
		    	  if (i!=iQuery.size()-1) sSelect.append(" UNION ALL ");
		      }
		      
		      IQuery iq = new IQuery();			      
              iq.setAccessProfile(true);
              iq.setInstanceProfile(true);
              iq.escludiControlloCompetenze(true);              
		      iq.initVarEnv(vEnv);
		      iq.setCampiRicerca(vCampiRicerca);
		      iq.setCampiOrdinamentoReturn(vCampiReturn);
		      
		      iq.setSqlSelect(sSelect.toString());
		      
		      return iq;
	   }
}

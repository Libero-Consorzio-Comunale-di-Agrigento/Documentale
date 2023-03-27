package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;

/**
 * Gestione delle connessioni.
 * Classe di servizio per la gestione del Client
*/

public class CCS_Common 
{
	/**
	  * Variabile dataSource
	 */
	String dataSource;
	
	/**
	  * Variabile di ambiente
	 */
	Environment ev;
	
	/**
	  * Variabile utente
	 */
	String user; 
	
	/**
	  * Costruttori utilizzati per settare i parametri
	  * user e datasource della connessione.
	  * 
	*/
	public CCS_Common(String newdataSource, String newuser)
	{
           dataSource=newdataSource;
           user=newuser;
           ev=null;
	}
	
	public CCS_Common(Environment newEv, String newuser)
	{	  
           dataSource="";
           user=newuser;
           ev=newEv;
    }
	
	/**
	  * Chiusura della connessione: effettua l'operazione
	  * di COMMIT e di ROLLBACK in funzione del valore
	  * di bCommitRollback. In entrambi i casi effettua 
	  * la chiusura della connessione.
	  * 
	*/
	public void closeConnection(IDbOperationSQL dbOp, boolean bCommitRollback) throws Exception 
	{
		   if (dbOp==null) return;
		
           if (bCommitRollback) 
             dbOp.commit();
           else 
             dbOp.rollback();
           dbOp.close();
	}
	
	/**
	  * Chiusura della connessione: se viene passto il 
	  * datasource vuol dire che si proviene da fuori 
	  * e viene effettuato la chiusura della connessione.
	  * 
	*/
	public void closeConnection(IDbOperationSQL dbOp) throws Exception 
	{      
		   if (dbOp==null) return;
		
           if (!dataSource.equals("")) 
            dbOp.close();
    }
	
	
	public String getDataSource()
	{
		   return dataSource;
	}
	
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser()
	{
		   return user;
	}
	
	public Environment getEnvironment()
	{
		   return ev;
	}
}
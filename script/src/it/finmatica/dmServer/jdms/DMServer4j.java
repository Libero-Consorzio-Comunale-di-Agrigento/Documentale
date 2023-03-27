package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import org.apache.log4j.*;

/**
 * Gestione del LOG4J.
 * Classe di servizio per la gestione dei logging 
*/

public class DMServer4j {
	
    /** Definire una variabile logger che si riferisce all'istanza
     *  di logger specificata dalla variabile instance.
     */
	private Logger logger;
    
	/** Classe da istanziare */
	private Class instanza;
	
	/** Classe da istanziare */
	private String nome;
	
	private CCS_Common CCS_common; 
	private IDbOperationSQL dbOp;  
	private Environment vu; 
	
	/**
	 * Costruttore  generico vuoto.
	*/
	public DMServer4j(){}
	
	/**
	 * Costruttore con parametro Class.
	 */
	public DMServer4j(Class nome)
  	{
	   instanza=nome;
	   logger=Logger.getLogger(instanza);
	}
	
	/**
	 * Costruttore con parametro String.
	 */
	public DMServer4j(String n)
  	{
	   nome=n;
	   logger=Logger.getLogger(nome);
	}
	
	/**
	 * Costruttore con parametro String e CCS_Common.
	 */
	public DMServer4j(String n,CCS_Common newCommon) throws Exception
  	{
	   nome=n;
	   logger=Logger.getLogger(nome);
	   init(newCommon);
	}
	
	/**
	 * Costruttore con parametro Class e CCS_Common.
	 */
	public DMServer4j(Class nome,CCS_Common newCommon) throws Exception
  	{
		instanza=nome;
		logger=Logger.getLogger(instanza);
	    init(newCommon);
	}
	
    /**
     * Inizializzazione di alcuni parametri
     * per la gestione della WorkArea.
     * 
     * @param newCommon    variabile di connessione
    */	   
    private void init(CCS_Common newCommon)  throws Exception
    {
	   CCS_common=newCommon;
	   
	   if (!CCS_common.dataSource.equals("")) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
        vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
       }
       else 
       {
        vu=CCS_common.ev;
        dbOp=CCS_common.ev.getDbOp();
       }
	   
	   CCS_common.closeConnection(dbOp);     
    }
    
    
	/**
	  * Gestione log del messaggio a livello ERROR.
	  * 
	*/	 
	public void log_error(String msg,Exception e) 
	{
		if (vu.Global.PARAM_DEBUG.equals("1")) {
			logger.error(msg);
        }
        
		if (vu.Global.PARAM_DEBUG.equals("2")) {
    	    logger.error(msg,e);
        }
	}
	
	/**
	  * Gestione log del messaggio a livello ERROR.
	  * 
	*/	 
	public void log_error(String msg) 
	{
		   logger.error(msg);
	}
	
	/**
	  * Gestione log del messaggio a livello DEBUG.
	  * 
	*/	 
	public void log_debug(String msg) 
	{
		   if(logger.isDebugEnabled())
			logger.debug(msg);
	}
	
	/**
	  * Gestione log del messaggio a livello FATAL.
	  * 
	*/	 
	public void log_fatal(String msg) 
	{
		logger.fatal(msg);
	}
	
	/**
	  * Gestione log del messaggio a livello INFO.
	  * 
	*/	 
	public void log_info(String msg) 
	{
		   if(logger.isInfoEnabled())     
		    logger.info(msg);
	}
	
	/**
	  * Gestione log del messaggio a livello WARN.
	  * 
	*/	 	
	public void log_warn(String msg) 
	{
		logger.warn(msg);
	}
	
}

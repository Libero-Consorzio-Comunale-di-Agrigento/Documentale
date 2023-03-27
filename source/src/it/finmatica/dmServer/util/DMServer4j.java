package it.finmatica.dmServer.util;

import org.apache.log4j.*;

/**
 * Gestione del LOG4J.
 * Classe di servizio per la gestione dei logging 
*/

public class DMServer4j {
	
    /** Definire una variabile statica logger che si riferisce all'istanza
     *   di logger specificata dalla variabile instance.
     */
	private static Logger logger;
    
	/** Classe da istanziare */
	private Class instance;
 
	
	/**
	 * Costruttore  generico vuoto.
	*/
	public DMServer4j(){}
	
	/**
	 * Costruttore.
	 */
	public DMServer4j(Class nome)
  	{
	   instance=nome;
	   //logger=Logger.getLogger(instance);
	   logger=Logger.getLogger(nome);
	 
	}
	
	public void log_error(String msg,Exception e) 
	{
		logger.error(msg,e);
	}
	
	public void log_error(String msg) 
	{
		logger.error(msg);
	}
	
	public void log_debug(String msg) 
	{
		logger.debug(msg);
	}
	
	public void log_fatal(String msg) 
	{
		logger.fatal(msg);
	}
	
	public void log_info(String msg) 
	{
		logger.info(msg);
	}
	
	public void log_warn(String msg) 
	{
		logger.warn(msg);
	}
	
}

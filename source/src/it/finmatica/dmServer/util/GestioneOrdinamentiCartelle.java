package it.finmatica.dmServer.util;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.jdms.CCS_Common;

import java.util.*;
import java.sql.*;


public class GestioneOrdinamentiCartelle
{
	/**
	 * Variabili private
	 */	 
	private String  idCartella;
	private String  idOggetto;
	private String  tipoOggetto;
	private CCS_Common CCS_common; 
	/**
	 * Indica quale package invocare nella seguente logica:
	 * 
	 * PKG=1 --> terna(idOggetto,tipoOggetto,idCartella);
	 * PKG=2 --> idCartella;
	 * PKG=3 --> coppia(idOggetto,tipoOggetto);
	 */
	private String  tipoPKG; 
	IDbOperationSQL  dbOp=null; 
	Environment en;
	
    /**
     * Costruttori.
    */
	public GestioneOrdinamentiCartelle(Environment newEn,String idCart,String idObj,String tipoObj) throws Exception
    {
    	   this.init(newEn);  
    	   idCartella=idCart;
    	   idOggetto=idObj;
    	   tipoOggetto=tipoObj;  
    	   tipoPKG="1";
 	}
	
	public GestioneOrdinamentiCartelle(String idLink,Environment newEn) throws Exception
	{
		   this.init(newEn); 	
		   retrieveParametersFromIDLinks(idLink);
		   tipoPKG="1";
	}
	
    public GestioneOrdinamentiCartelle(Environment newEn,String idCart) throws Exception
    {
    	   this.init(newEn); 
    	   idCartella=idCart;
    	   tipoPKG="2";
	}
    
    public GestioneOrdinamentiCartelle(CCS_Common newCommon,String idCart) throws Exception
    {
    	   CCS_common=newCommon;
    	   idCartella=idCart;
    	   tipoPKG="2";
    	  
    	   if (!CCS_common.getDataSource().equals("")) {
	        dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.getDataSource(),0);
	        en = new Environment(CCS_common.getUser(), null,null,null, null,dbOp.getConn(),false);
	       }
	       else 
	       {
	        en=CCS_common.getEnvironment();
	        dbOp=CCS_common.getEnvironment().getDbOp();
	       }
	}
    
    public GestioneOrdinamentiCartelle(Environment newEn,String idObj,String tipoObj) throws Exception
    {
    	   this.init(newEn);    
    	   idOggetto=idObj;
    	   tipoOggetto=tipoObj;    	
    	   tipoPKG="3";
	}
    
    public void rebuild(boolean reconnect) throws Exception
    {
    	   rebuild(reconnect, true);
    }
    	
    /**
     * Rigenerazione degli Ordinamenti
    */ 
    public void rebuild(boolean reconnect, boolean bCloseDbOp) throws Exception
    {
    	   IDbOperationSQL  dbOpSQL=null; 
		   String sql="";
		   boolean bConnect=false;
	       
	       
	       try
	       {	    
	    	 if (en.getDbOp()==null) {
	    		 //System.out.println("PROVAMANNY--->CONNESSIONE AUTONOMA");
	    		 en.connect();	    		 
	    		 bConnect=true; 
	    	 }	    	
	    	 
	    	 dbOpSQL = en.getDbOp();	    	 
	   	    	 	    	
	         if(tipoPKG.equals("1"))
	    	   sql+="ORDINAMENTO_PKG.GENERA_CHIAVE("+idOggetto+",'"+tipoOggetto+"',"+idCartella+")";  
	     	 else
	     	   if(tipoPKG.equals("2"))
	 	    	 sql+="ORDINAMENTO_PKG.GENERA_CHIAVE("+idCartella+")";  
	 	       else
	 	    	 sql+="ORDINAMENTO_PKG.GENERA_CHIAVE("+idOggetto+",'"+tipoOggetto+"')"; 
	     	 
	        /* java.util.Calendar cal = Calendar.getInstance();
	         java.sql.Timestamp now = new java.sql.Timestamp(cal.getTimeInMillis());
	     	   	     	    
	         if(tipoPKG.equals("1") && tipoOggetto.equals("D")) {
		         System.out.println("------ORDINAMENTO_PKG------ "+now);
		         System.out.println(sql);
		         System.out.println(CallStackUtil.getCallStackAsString());
		         System.out.println("------FINE ORDINAMENTO_PKG------ ");
	         }*/
	         
	         dbOpSQL.setCallFunc(sql);
	        
	     	 dbOpSQL.execute();
	         
	     	 //Se mi sono connesso autonomamente....allora committo autonomamente
	    	 if ( bConnect)	{
	    		 //System.out.println("PROVAMANNY--->CONNESSIONE AUTONOMA - COMMITTO");
	    		 en.disconnectCommit();
	    		 
	    		 //en.disconnectClose();
	    		 
	    	 }
		     	
        	     	 
	       }
	       catch (Exception e) {   
	    	   if ( bConnect) 
	    		   try{en.disconnectRollback();}catch(Exception ei){}
	    	   else
	    		   try{if (bCloseDbOp) en.disconnectClose();}catch(Exception ei){}
	    		   
	    	  throw new Exception("GestioneOrdinamentiCartelle::rebuild - SQL = "+sql+"\n" + e.getMessage());
	       }             
    }
    
    /***************************************************************************
	* METODI PRIVATI
	**************************************************************************/
    
    /**
     * Inizializzazione dei vettori.
     * 					
     * 
 	 */
    private void init(Environment newEn) 
    {
	    	en = newEn;
    }
    
   
    /**
     * Recupera la terna (idCartella,idOggetto,tipoOggetto) dal relativo idLink
     * alla tabella LINKS  
     * 
     * @param	String	idLink 
     * 
 	 */
    private void retrieveParametersFromIDLinks(String idLink) throws Exception
    {
    		String sql="";
    	   	ResultSet rs=null;
    	   	IDbOperationSQL dbOp=null;
    	   	boolean bConnect=false;
    	   	
            try
            {
         	 sql+=" select id_cartella,id_oggetto,tipo_oggetto ";
         	 sql+=" from links ";
         	 sql+=" where id_link = "+idLink;      	  
             
	    	 dbOp = en.getDbOp();
	    	 if (dbOp==null) {
	    		 en.connect();
	    		 bConnect=true;
	    	 }	        	
         	 dbOp.setStatement(sql);
         	 dbOp.execute();
       	     rs=dbOp.getRstSet();
             if (rs.next())
             {
            	 idCartella=rs.getString("id_cartella");
            	 idOggetto=rs.getString("id_oggetto");
            	 tipoOggetto=rs.getString("tipo_oggetto");
             }              
             if ( bConnect)  
		     	en.disconnectClose();
           }
           catch (Exception e) {   
        	  try{en.disconnectClose();}catch(Exception ei){}
        	  throw new Exception("GestioneOrdinamentiCartelle::retrieveParametersFromIDLinks(idLink) - ("+idLink+") - SQL = "+sql+"\n" + e.getMessage());
           }             
    } 
    
    
      
}

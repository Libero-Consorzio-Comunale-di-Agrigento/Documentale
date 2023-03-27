package it.finmatica.dmServer.jdms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

import it.finmatica.dmServer.motoreRicerca.GD4_Gestione_Query;
import it.finmatica.dmServer.util.CrypUtility;
import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;

public class CCS_GestioneQuery_Common 
{
	   /**
	    * Variabili private
	   */
	   CCS_Common CCS_common;
	   GD4_Gestione_Query q; 
	   IDbOperationSQL dbOp;
	   Environment vu;	 
	   Vector vParametriSessione;
	   HttpServletRequest req;
	   
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   public CCS_GestioneQuery_Common(long idQuery,CCS_Common newCommon) throws Exception
	   {
		   	  this(idQuery,newCommon,false);
	   }	   
	   	   
	   public CCS_GestioneQuery_Common(long idQuery,CCS_Common newCommon, boolean bPreserve) throws Exception
	   {
		   	  this(idQuery,newCommon,bPreserve,true);
	   }	   
	   
	   /** Costruttore invocato dalla WorkArea nel caso di una ricerca */
	   public CCS_GestioneQuery_Common(long idQuery,CCS_Common newCommon, HttpServletRequest newreq) throws Exception
	   {
		      this(idQuery,newCommon,false,false);	  
		      req = newreq;		      
	   }
	   
	   /**
		 * Costruttore utilizzato per la gestione della Ricerca.
		 * 
		 */
	   public CCS_GestioneQuery_Common(long idQuery,CCS_Common newCommon, boolean bPreserve, boolean bParse) throws Exception
	   {
	          CCS_common=newCommon;
	          log= new DMServer4j(CCS_GestioneQuery_Common.class,CCS_common); 
	          try
	          {        
	          // dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);  
	           vu = new Environment(CCS_common.user, null,null,null, null,new JNDIParameter(CCS_common.dataSource)); 
	           vu.connect();
	           q = new GD4_Gestione_Query(idQuery,vu,bPreserve,bParse);
	           q.setJndiPar(CCS_common.dataSource);
	           try{vu.disconnectClose();}catch(Exception ei){}
	          // CCS_common.closeConnection(dbOp);
	          }
	          catch (Exception e) {     
	             try{vu.disconnectClose();}catch(Exception ei){}
	             //CCS_common.closeConnection(dbOp);
	             log.log_error("CCS_GestioneQuery_Common::constructor - idQuery:"+idQuery);
	             throw e;
	          }  
	   }
	   
	   /**
		 * Costruttore utilizzato per la gestione della Ricerca.
		 * 
		 */
	   public CCS_GestioneQuery_Common(String Filtro,CCS_Common newCommon) throws Exception
	   {
	          CCS_common=newCommon;
	          log= new DMServer4j(CCS_GestioneQuery_Common.class,CCS_common);
	          try
	          {         
	           dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);  
	           vu = new Environment(CCS_common.user, null,null,null, null,new JNDIParameter(CCS_common.dataSource)); 
	           vu.connect();
	           q = new GD4_Gestione_Query(Filtro,vu);       
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp);           
	          }
	          catch (Exception e) {             
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp);
	           log.log_error("CCS_GestioneQuery_Common::constructor - Filtro:"+Filtro);
	           throw e;
	           //throw new Exception("CCS_GestioneQuery_Common::constructor\n"+e.getMessage());
	          }  
	   }
	   
	   /**
		 * Costruttore utilizzato per la gestione della Ricerca.
		 * 
		 */
	   public CCS_GestioneQuery_Common(long idQuery,String Filtro,CCS_Common newCommon) throws Exception
	   {
	          CCS_common=newCommon;
	          log= new DMServer4j(CCS_GestioneQuery_Common.class,CCS_common);
	          try
	          {         
	           dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);  
	           vu = new Environment(CCS_common.user, null,null,null, null,new JNDIParameter(CCS_common.dataSource)); 
	           vu.connect();
	           q = new GD4_Gestione_Query(idQuery,Filtro,vu);       
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp);           
	          }
	          catch (Exception e) {             
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp);
	           log.log_error("CCS_GestioneQuery_Common::constructor - idQuery:"+idQuery+" - Filtro:"+Filtro);
	           throw e;
	           //throw new Exception("CCS_GestioneQuery_Common::constructor\n"+e.getMessage());
	          }  
	   }
	   
	   /**
		 * Costruttore utilizzato per la gestione della Ricerca.
		 * 
		 */
	   public CCS_GestioneQuery_Common(long idQuery,String filtro,String CCFQ,CCS_Common newCommon) throws Exception
	   {
	          CCS_common=newCommon;
	          log= new DMServer4j(CCS_GestioneQuery_Common.class,CCS_common);
	          try
	          {   
	           filtro = verificaFiltro(filtro, CCFQ.getBytes());
	           dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);  
	           vu = new Environment(CCS_common.user, null,null,null, null,new JNDIParameter(CCS_common.dataSource)); 
	           vu.connect();
	           q = new GD4_Gestione_Query(idQuery,filtro,vu);       
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp);           
	          }
	          catch (Exception e) {             
	           try{vu.disconnectClose();}catch(Exception ei){}
	           CCS_common.closeConnection(dbOp);
	           log.log_error("CCS_GestioneQuery_Common::constructor - idQuery:"+idQuery+" - Filtro:"+filtro);
	           throw e;
	          }  
	   }
	   
	   
	   private String verificaFiltro(String filtro,byte[] CCFQ) throws Exception 
	   {
               String ret="";
		       //Hashing hsc;
		       try
	           {   
			    //hsc =new Hashing();
			    /*if(hsc.isValid(filtro,CCFQ))
			     ret = CrypUtility.decriptare(filtro); 
			    else {
			     log.log_error("VerificaFiltro - La verifica del filtro ha dato esito negativo. filtro:"+filtro);
		         throw new Exception("Problemi durante la fase di verifica del filtro.");	
			    }	*/
		    	ret = CrypUtility.decriptare(filtro);   		    	   
	           }
	           catch (Exception e) {             
	            log.log_error("CCS_GestioneQuery_Common::verificaFiltro - Problemi durante la verifica del filtro.");
	            throw e;
	           } 
	           return ret;
	   }
	   
	   /**
		 * Restituisce oggetto GD4_Gestione_Query.
		 * 
		 */
	   public GD4_Gestione_Query getGestioneQuery() throws Exception {                                      
         return q;        
	   }
	   
	   /**
	    * Effettua il caricamento di eventuali parametri di sessione
	    * e dopo effettua l'operazione di "parse" nel filtro di ricerca
	    * */
	   
	   public void parseQuery() throws Exception {                                      
		  	  
		   
		      /** Caricamento di eventuali parametri di sessione */
          	  try {
          	    vParametriSessione = loadParametriSessione();
          	  }
          	  catch (Exception exp) {
                vParametriSessione = new Vector();       
              }
          	 
          	  for(int i=0;i<vParametriSessione.size();i++)
          	  {
          		 String parametro = vParametriSessione.get(i).toString();
          		 if(req.getSession().getAttribute(parametro)!=null && !req.getSession().getAttribute(parametro).equals(""))
          	      q.setSessionParameter(parametro,req.getSession().getAttribute(parametro).toString());	
          		 else {
          			if(req.getParameter(parametro)!=null && !req.getParameter(parametro).equals(""))
                 	  q.setSessionParameter(parametro,req.getParameter(parametro).toString());		 
          		 }
          	  }                	 
              
          	  
          	  
          	  q.parseQuery();        
	   }
	   
	   /**
	     * Caricamento dei parametri di sessione da sostituire nel 
	     * filtro di ricerca standard
		 */
	   private Vector loadParametriSessione() throws Exception
	   {
	           String sql="";
	           IDbOperationSQL dbOpSQL=null;
	           Vector vParametriSessione = new Vector();
	  
	           if (!CCS_common.dataSource.equals("")) 
	              dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	           else {
	              vu.connect();
	              dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(vu.getDbOp().getConn(),0);
	           }
	       
	           try 
	           {			    
	        	sql="select * from parametri_sessione";
				dbOpSQL.setStatement(sql);
				dbOpSQL.execute();
				ResultSet rst = dbOpSQL.getRstSet();
				while(rst.next()){
					vParametriSessione.add(rst.getString("nome_parametro").toString());
				}
				
				if (CCS_common.dataSource.equals("")) try{vu.disconnectClose();}catch(Exception ei){}
				dbOpSQL.close();                
	           }
	           catch ( SQLException e ) {
	        	if (CCS_common.dataSource.equals("")) try{vu.disconnectClose();}catch(Exception ei){}        	  
	            dbOpSQL.close();
	            throw e;
	           }  
	           return vParametriSessione;
	   }
	   
}
package it.finmatica.log4jsuite;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DatabaseConnection {
	     
	   public boolean  bIsJndi = true;
	   IDbOperationSQL dbOp;
	
	   public DatabaseConnection(Connection cn) throws Exception {		   	    					   		 		   	
	   		  //ByteArrayOutputStream o = new ByteArrayOutputStream(); 
	   		  //PrintStream out = new PrintStream(o);
	   		
	   		  //PrintStream oldOut, oldErr;
	   		
	   		  //oldOut=System.out;
	   		  //oldErr=System.err;
	   		
			  //System.setOut( out );
			  //System.setErr( out );
		   	  int iEsisteJndi=0;
		   	  try { iEsisteJndi=getJNDIConnection("jdbc/gdm");} catch(Exception ex){iEsisteJndi=0;}
		   	    
			  if (iEsisteJndi==1)
				  dbOp = SessioneDb.getInstance().createIDbOperationSQL("jdbc/gdm",0);
			  										
			  //System.setOut(oldOut);
			  //System.setErr(oldErr);
			
   		      if (dbOp.getConn()==null) {
   		 	     bIsJndi = false;
	   		     dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn,0);	   		     
   		      }
		   				      				   	
	   }
	   
	   public IDbOperationSQL getDbOp() {
		      return dbOp;
	   }
	   
	   private int getJNDIConnection(String jndi) {
		   
		   	   Connection result = null;
		       Context initialContext = null;
		       try {
		         initialContext = new InitialContext();
		        
		         if ( initialContext == null) throw new Exception("");
		         
		         DataSource datasource = (DataSource)initialContext.lookup("java:comp/env/"+jndi);
			     if (datasource != null) result = datasource.getConnection();
			     else throw new Exception("");
			     
			     try { result.close();} catch(Exception ex){}
			     try { initialContext.close();} catch(Exception ex){}
		       }
		       catch(Exception e ){
		    	 try { result.close();} catch(Exception ex){}
				 try { initialContext.close();} catch(Exception ex){}
				 
				 return 0;
		       }
		       
		       return 1;
	   }
}

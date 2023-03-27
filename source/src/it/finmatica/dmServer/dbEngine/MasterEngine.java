	package it.finmatica.dmServer.dbEngine;

import java.io.ByteArrayInputStream;
import java.sql.Clob;
import java.sql.ResultSet;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.struct.DbOpSetParameterBuffer;
import it.finmatica.dmServer.util.ElapsedTime;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class MasterEngine { 
	   protected IDbOperationSQL       			dbOpSql;
	   private   StringBuffer           		statementBuffer;
	   private   Vector<DbOpSetParameterBuffer> parVector;
	   private   ElapsedTime 				    elpsTime;
	   protected Environment                    vEnv;	
	   
	   
	   public MasterEngine(IDbOperationSQL dbOp) {
		   	  statementBuffer = new StringBuffer("");
		   	  parVector = new Vector<DbOpSetParameterBuffer>();
		   	  dbOpSql=dbOp; 		   	  
	   }	   
	   
	   public MasterEngine(Environment en,IDbOperationSQL dbOp, String msgElapsedTime) {
		   	  statementBuffer = new StringBuffer("");
		   	  parVector = new Vector<DbOpSetParameterBuffer>();
		   	  dbOpSql=dbOp; 
		   	  vEnv=en;
		   	  
		   	  if (msgElapsedTime!=null)
		   		 elpsTime = new ElapsedTime(msgElapsedTime,vEnv);
	   }
	   
	   protected int executeSqlSequence(String seqName) throws Exception {
		   	     return dbOpSql.getNextKeyFromSequence(seqName);
	   }
	   
	   protected void executeSql() throws Exception {
		         exec(false,null);
	   }
	   
	   protected ResultSet executeSqlResultSet() throws Exception {
		   	     return exec(true,null);
	   }	   
	   
	   protected void executeSql(String msgElapsedTime) throws Exception {
		         exec(false,msgElapsedTime);
	   }
	   
	   protected ResultSet executeSqlResultSet(String msgElapsedTime) throws Exception {
		   	     return exec(true,msgElapsedTime);
	   }
	   
	   private ResultSet exec(boolean bResultSet,String msgElapsedTime) throws Exception {
		   	   dbOpSql.setStatement(statementBuffer.toString());
		   	   //System.out.println("dbEnginbe-->"+dbOpSql.getConn());
		   	   for(int i=0;i<parVector.size();i++) {
		   		   DbOpSetParameterBuffer dbOpSpB=parVector.get(i);
		   		   if (dbOpSpB.getType()==DbOpSetParameterBuffer.IS_ASCIISTREAM) {
		   			   ByteArrayInputStream bais = (ByteArrayInputStream)dbOpSpB.getValue();
		   			   dbOpSql.setAsciiStream(dbOpSpB.getNamePar(),bais,bais.available());
		   		   }
		   		   else {
		   			   if (dbOpSpB.getValue() ==null) {
		   				   if (dbOpSpB.getValueTypeNull() instanceof java.sql.Date)
		   					   dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.sql.Date)null);
       				 
		   				   if (dbOpSpB.getValueTypeNull() instanceof java.math.BigDecimal)
		   					   dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.math.BigDecimal)null);	  	        				 	        				 
		   			   }

		   			   if (dbOpSpB.getValue() instanceof java.lang.String) 
		   				   dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.lang.String)dbOpSpB.getValue());	        					        			 
       				 

		   			   if (dbOpSpB.getValue() instanceof java.sql.Date)	        				 
		   				   dbOpSql.setParameter(dbOpSpB.getNamePar(),(new java.sql.Timestamp(((java.sql.Date)dbOpSpB.getValue()).getTime())));

		   			   if (dbOpSpB.getValue() instanceof java.sql.Timestamp)
		   				   dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.sql.Timestamp)dbOpSpB.getValue());

		   			   if (dbOpSpB.getValue() instanceof java.math.BigDecimal)
		   				   dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.math.BigDecimal)dbOpSpB.getValue());   
		   			   
		   			   if (dbOpSpB.getValue() instanceof Long)
		   				   dbOpSql.setParameter(dbOpSpB.getNamePar(),(Long)dbOpSpB.getValue());	
		   			   
		   			   if (dbOpSpB.getValue() instanceof Clob)
		   				   dbOpSql.setParameter(dbOpSpB.getNamePar(),(Clob)dbOpSpB.getValue());			   			          			 
		   		   }
		   	   }
		   		
		   	   if (msgElapsedTime!=null && elpsTime!=null ) elpsTime.start(msgElapsedTime,statementBuffer.toString());
		   		   
		   	   dbOpSql.execute();
		   	   
		   	   if (msgElapsedTime!=null && elpsTime!=null) elpsTime.stop();
		   	   
		   	   parVector.removeAllElements();
		   	   statementBuffer=null;
		   	   statementBuffer = new StringBuffer("");
		   	   
		   	   if (bResultSet)
		   	   	  return dbOpSql.getRstSet();
		   	   else
		   		  return null;
	   }


	   protected void appendStatement(String statement) {
		   	     statementBuffer.append(statement);
	   }
	   
	   protected void setStatement(String statement) {
		         statementBuffer = new StringBuffer("");
		   	     statementBuffer.append(statement);
	   }	   
	   
	   protected void appendParameter(String par, Object val) {
		      appendParameter( par, val, DbOpSetParameterBuffer.IS_PARAMETER);
	   }
	   
	   protected void appendParameterAscii(String par, Object val) {
		      appendParameter( par, val, DbOpSetParameterBuffer.IS_ASCIISTREAM);		   	  		   	 		      	  
	   }
	   
	   private void appendParameter(String par, Object val, int isParameterOrAsciiStream) {
		   	   DbOpSetParameterBuffer dbOpPar = new DbOpSetParameterBuffer(par,val,isParameterOrAsciiStream);
		   	   
		   	   parVector.add(dbOpPar);
	   }	   
	   
	   protected void connect() throws Exception {
	        	 if (vEnv.getDbOp()==null) 
	        		 dbOpSql = (new ManageConnection(vEnv.Global)).connectToDB();        
	        	 else
	        		 dbOpSql = vEnv.getDbOp();
	  }
	  
	   protected void close() throws Exception {
	          	 if (vEnv.getDbOp()==null) (new ManageConnection(vEnv.Global)).disconnectFromDB(dbOpSql,true,false);
	   }	   
}
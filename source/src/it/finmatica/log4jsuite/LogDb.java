package it.finmatica.log4jsuite;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;

import java.sql.Connection;
import java.sql.ResultSet;
 
public class LogDb {
	   public  static final int   DEBUG_LEVEL     =  1;
	   public  static final int   INFO_LEVEL      =  2;
	   public  static final int   ERROR_LEVEL     =  3; 
	   
	   private static final long  NO_TIME      =  -1;
	
	   private String identifierLog;
	   private int sequenzaRigaLog;
	   
	   private String          userLog;
	   private String          categoryLog;
	   private Connection      cnLog;
	   
	   private int logLevel;
	   
	   /**
	    * Costruttore che inserisce la testata dal log
	    * sulla tabella DM_ERR_LOG.
	    * 
	    * @param user
	    * @param category   Utilizzare i codici del dizionario
	    *                   DM_ERR_LOG_CATEGO	   
	    * @param cn
	    * @throws Exception
	   */
	   public LogDb(String user, String category, 
			        Connection cn) throws Exception {
		      userLog         = user;
		      categoryLog     = category;		     		      
		      cnLog           = cn;
		      sequenzaRigaLog = 1;
		      		      		      
		      checkLivello();		  
		      
		      init();
	   }	   		  
	   
	   /**
	    * Metodo che scrive una riga di Log collegata alla testataLog creata in precedenza
	    * dal costruttore.
	    * 
	    * La riga viene scritta solo se si è abilitati al log rispetto al livello passato
	    * come parametro di input e solo se livello utente letto dalla tabella DM_ERR_LOG_USERLEVEL 
	    * nel costruttore esiste e non è 0.
	    * 
		* @param azione			Azione eseguita (es. Frase SQL o Istruzione Java)
		* @param descrizione	Cosa ha comportato l'azione (es. Errore SQL)
		* @param tag			Il TAG come buona norma dovrebbe essere un codice riconducibile 
		* 						ad un proprio dizionario, ad es il TAG “TIMEOUTQUERY” potrebbe 
		* 						rappresentare sempre la riga di ERROR relativa ad una query che è 
		* 						andata in timeout
		* @param livello		Livello di errore che si vuole andare a scrivere. Può essere:
		* 						LogDb.DEBUG_LEVEL, LogDb.INFO_LEVEL, LogDb.ERROR_LEVEL
		* 
		* @throws Exception
	   */
	   public void ScriviLog(String azione, String descrizione, String tag, int livello) throws Exception {
		      ScriviLog(azione,descrizione,tag,livello,null,null,NO_TIME);
	   }
	   
	   /**
	    * Metodo che scrive una riga di Log collegata alla testataLog creata in precedenza
	    * dal costruttore.
	    * 
	    * La riga viene scritta solo se si è abilitati al log rispetto al livello passato
	    * come parametro di input e solo se livello utente letto dalla tabella DM_ERR_LOG_USERLEVEL 
	    * nel costruttore esiste e non è 0.
	    * 
		* @param azione			Azione eseguita (es. Frase SQL o Istruzione Java)
		* @param descrizione	Cosa ha comportato l'azione (es. Errore SQL)
		* @param tag			Il TAG come buona norma dovrebbe essere un codice riconducibile 
		* 						ad un proprio dizionario, ad es il TAG “TIMEOUTQUERY” potrebbe 
		* 						rappresentare sempre la riga di ERROR relativa ad una query che è 
		* 						andata in timeout
		* @param livello		Livello di errore che si vuole andare a scrivere. Può essere:
		* 						LogDb.DEBUG_LEVEL, LogDb.INFO_LEVEL, LogDb.ERROR_LEVEL
		* @param classe			Classe che ha generato la riga di log (inserire il package completo)
		* @param metodo         Metodo che ha generato la riga di log
		* 
		* @throws Exception
	   */	   
	   public void ScriviLog(String azione, String descrizione, String tag, int livello, String classe, String metodo) throws Exception {
		      ScriviLog(azione,descrizione,tag,livello,classe,metodo,NO_TIME);
	   }	   
	   
	   /**
	    * Metodo che scrive una riga di Log collegata alla testataLog creata in precedenza
	    * dal costruttore.
	    * 
	    * La riga viene scritta solo se si è abilitati al log rispetto al livello passato
	    * come parametro di input e solo se livello utente letto dalla tabella DM_ERR_LOG_USERLEVEL 
	    * nel costruttore esiste e non è 0.
	    * 
		* @param azione			Azione eseguita (es. Frase SQL o Istruzione Java)
		* @param descrizione	Cosa ha comportato l'azione (es. Errore SQL)
		* @param tag			Il TAG come buona norma dovrebbe essere un codice riconducibile 
		* 						ad un proprio dizionario, ad es il TAG “TIMEOUTQUERY” potrebbe 
		* 						rappresentare sempre la riga di ERROR relativa ad una query che è 
		* 						andata in timeout
		* @param livello		Livello di errore che si vuole andare a scrivere. Può essere:
		* 						LogDb.DEBUG_LEVEL, LogDb.INFO_LEVEL, LogDb.ERROR_LEVEL
		* @param time			Tempo di esecuzione dell'azione (es. Tempo di esecuzione dell'SQL)
		* 						espresso in ms
		* 
		* @throws Exception
	   */	   	   
	   public void ScriviLog(String azione, String descrizione, String tag, int livello, long time) throws Exception {
		      ScriviLog(azione,descrizione,tag,livello,null,null,time);
	   }
	   
	   /**
	    * Metodo che scrive una riga di Log collegata alla testataLog creata in precedenza
	    * dal costruttore.
	    * 
	    * La riga viene scritta solo se si è abilitati al log rispetto al livello passato
	    * come parametro di input e solo se livello utente letto dalla tabella DM_ERR_LOG_USERLEVEL 
	    * nel costruttore esiste e non è 0.
	    * 
		* @param azione			Azione eseguita (es. Frase SQL o Istruzione Java)
		* @param descrizione	Cosa ha comportato l'azione (es. Errore SQL)
		* @param tag			Il TAG come buona norma dovrebbe essere un codice riconducibile 
		* 						ad un proprio dizionario, ad es il TAG “TIMEOUTQUERY” potrebbe 
		* 						rappresentare sempre la riga di ERROR relativa ad una query che è 
		* 						andata in timeout
		* @param livello		Livello di errore che si vuole andare a scrivere. Può essere:
		* 						LogDb.DEBUG_LEVEL, LogDb.INFO_LEVEL, LogDb.ERROR_LEVEL
		* @param classe			Classe che ha generato la riga di log (inserire il package completo)
		* @param metodo         Metodo che ha generato la riga di log 
		* @param time			Tempo di esecuzione dell'azione (es. Tempo di esecuzione dell'SQL)
		* 						espresso in ms
		* 
		* @throws Exception
	   */	   	   	   
	   public void ScriviLog(String azione, String descrizione, String tag, int livello, String classe, String metodo, long time) throws Exception {
		      if (livello!=DEBUG_LEVEL && livello!=INFO_LEVEL && livello!=ERROR_LEVEL)
		    	  throw new Exception("LogDb::ScriviLog - il parametro livello deve essere 1,2 o 3!!!");
		      
		      if (logLevel==0) return;

		      //Controllo se il livello passato è incluso nel livello utente
		      if (logLevel>livello) return;
		      		      
		      StringBuffer sStmSql = null;
		      IDbOperationSQL dbOp=null;	
		      DatabaseConnection dbConn=null;
		      try {		    	
		    	dbConn=new DatabaseConnection(cnLog);
		        dbOp = dbConn.getDbOp();		    
		        
		        if (dbOp==null) return;
		        if (dbOp.getConn()==null) return;
		        
		    	String sTime,sClasse,sMetodo;
		    	if (time==NO_TIME)
		      	    sTime="null";
		    	else
		    		sTime=""+time;
		    	 
		    	if (classe==null)
		    		sClasse="null";
		    	else
		    		sClasse="'"+classe+"'";
		    	 
		    	if (metodo==null)
		    		sMetodo="null";
		    	else
		    		sMetodo="'"+metodo+"'";		    	 
		    	 
		    	sStmSql = new StringBuffer("INSERT INTO DM_ERR_LOG_RIGHE ");
		    	sStmSql.append("(SEQUENZA,ID_ERR_LOG,AZIONE,DESCRIZIONE,EXECUTION_TIME,");
		    	sStmSql.append("DATA,TAG_FASE,LIVELLO,CLASSEJAVA,METODOJAVA)");
		    	sStmSql.append("VALUES ");
		    	sStmSql.append("("+(sequenzaRigaLog++)+","+identifierLog+",");
		    	sStmSql.append(":P_AZIONE,:P_DESCRIZIONE,"+sTime+",");
		    	sStmSql.append("sysdate,'"+tag+"',"+livello+",");
		    	sStmSql.append(sClasse+","+sMetodo+")");
		    	//System.out.println("ZOLIGNO---->roghe---->"+sStmSql.toString());
		    	dbOp.setStatement(sStmSql.toString());
		    	
		    	if (azione.length()>4000) azione=azione.substring(0,3999);
		    	dbOp.setParameter(":P_AZIONE",azione);
		    	dbOp.setParameter(":P_DESCRIZIONE",descrizione);
		    	 
		    	dbOp.execute();
		    	
		    	if (dbConn.bIsJndi) dbOp.commit();
		    	
		    	dbOp.close();
		      }		       
		      catch (Exception e) {		    	  
		    	try {if (dbConn.bIsJndi){dbOp.rollback();}dbOp.close();} catch (Exception eDbOp) {}
		    	throw new Exception("LogDb::ScriviLog - Creazione Riga su DM_ERR_LOG_RIGHE. Errore:\n"+e.getMessage()+" sql:"+sStmSql.toString());		    	   
		      }		      
	   }	   
	   
	   /**
	    * Controlla la validità delle variabili di testata
	    * e inserisce la testata in DM_ERR_LOG
	    * 
	    * @throws Exception
	   */
	   private void init() throws Exception {
		       if (logLevel==0) return;
		       
		       IDbOperationSQL dbOp=null;	         
		       DatabaseConnection dbConn=null;
			   try {		    	
			     dbConn=new DatabaseConnection(cnLog);
			     dbOp = dbConn.getDbOp();		
			     
			     if (dbOp==null) return;
			     if (dbOp.getConn()==null) return;
		    	 		    	 		    	 
		    	 identifierLog=dbOp.getNextKeyFromSequence("ERLO_SQ")+"";
		    	 
		    	 StringBuffer sStmSql = new StringBuffer("INSERT INTO DM_ERR_LOG ");
		    	 sStmSql.append("(ID_ERR_LOG,UTENTE,DATA,CATEGORIA) ");
		    	 sStmSql.append("VALUES ");
		    	 sStmSql.append("("+identifierLog+",'"+userLog+"'");
		    	 sStmSql.append(",sysdate,'"+categoryLog+"')");
		    	 
		    	 dbOp.setStatement(sStmSql.toString());
		    	 
		    	 dbOp.execute();
		    	 
		    	 if (dbConn.bIsJndi) dbOp.commit();
		    	 
		    	 dbOp.close();
		       }
		       catch (NullPointerException e) {		    	   
		    	 throw new Exception("LogDb::init - dbOperation non creata. Errore:\n"+e.getMessage());
		       }
		       catch (Exception e) {
		    	 try {if (dbConn.bIsJndi){dbOp.rollback();}dbOp.close();} catch (Exception eDbOp) {}
		    	 throw new Exception("LogDb::init - Creazione Testata su DM_ERR_LOG_CATEGO. Errore:\n"+e.getMessage());		    	   
		       }
	   }
	   
	   private void checkLivello() throws Exception {
		       IDbOperationSQL dbOp = null;
		       
		       DatabaseConnection dbConn=null;
			   try {		    	
			     dbConn=new DatabaseConnection(cnLog);
			     dbOp = dbConn.getDbOp();		
			     
			     if (dbOp==null) return;
			     if (dbOp.getConn()==null) return;
		    	 
		    	 StringBuffer sStmSql = new StringBuffer("");
		    	 sStmSql.append("SELECT 1,LIVELLO ");
		    	 sStmSql.append("FROM DM_ERR_LOG_USERLEVEL ");
		    	 sStmSql.append("WHERE UTENTE='GUEST' AND CATEGORIA='*' AND LIVELLO<>0 ");
		    	 
		    	 sStmSql.append("UNION ALL ");
		    	 
		    	 sStmSql.append("SELECT 2,LIVELLO ");
		    	 sStmSql.append("FROM DM_ERR_LOG_USERLEVEL ");
		    	 sStmSql.append("WHERE UTENTE='"+userLog+"' AND CATEGORIA='"+categoryLog+"' ");
		    	 
		    	 sStmSql.append("UNION ALL ");
		    	 
		    	 sStmSql.append("SELECT 3,LIVELLO ");
		    	 sStmSql.append("FROM DM_ERR_LOG_USERLEVEL ");
		    	 sStmSql.append("WHERE UTENTE='"+userLog+"' AND CATEGORIA='%' ");

		    	 sStmSql.append("UNION ALL ");
		    	 
		    	 sStmSql.append("SELECT 4,LIVELLO ");
		    	 sStmSql.append("FROM DM_ERR_LOG_USERLEVEL ");
		    	 sStmSql.append("WHERE UTENTE='GUEST' AND CATEGORIA='%' AND LIVELLO<>0 ");

		    	 sStmSql.append("ORDER BY 1 ");
		    	 
		    	 dbOp.setStatement(sStmSql.toString());

		    	 dbOp.execute();
		    	 
		    	 ResultSet rst = dbOp.getRstSet();
		    	 
		    	 if (rst.next())
		    		 logLevel=rst.getInt("LIVELLO");
		    	 else
		    		 logLevel=0;		    	 
		    	 
		    	 dbOp.close();
		       }
			   catch (NullPointerException e) {
				 //System.out.println("NON RIESCO A SCRIVERE NEL LOG (LOG4JSUITE)...SKIPPO");
			     return;
			   }
		       catch (Exception e) {
		    	 try {dbOp.close();} catch (Exception eDbOp) {}
		    	 throw new Exception("LogDb::checkLivello. Errore:\n"+e.getMessage());		    	   
		       }
	   } 
	   
}

package it.finmatica.dmServer.util;

/*
 * GESTIONE COMPLETA DELLA CONNESSIONE
 * VERSO IL DATABASE
 * 
 * AUTHOR @MANNELLA
 * DATE   19/09/2005
 * 
 * */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import it.finmatica.jfc.dbUtil.*;

public class ManageConnection 
{
  private Global GL;    
  // Utilizza lo stack tracing[1] per registrare lo stato dello stack di sistema  
  private static final Throwable tracer = new Throwable();
  
  public ManageConnection(Global newGlobal) {
	  	 GL = newGlobal;
  }
 
  public ManageConnection() {	  	 
  }  

  /*
   * METHOD:      connectToDB()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Connessione al Database con restituzione 
   *              di un oggetto di tipo DbOperationSQL
   * 
   * RETURN:      DbOperationSQL
  */  
  public IDbOperationSQL connectToDB() throws Exception 
  { 
         IDbOperationSQL dbOp;
         
         if (GL.DM.equals(GL.FINMATICA_DM)) {
             try {               
               if (GL.CONNECTION==null) { 
            	  if (GL.JNDIPARM==null) {
            		 // System.out.println("ini");
                      //18/05/2021 - Qualsiasi connessione data dal properties e quindi dai parametri di connessione
                      // verrà ribaltata sulla JNDI! parametro INIT_JNDI che viene letto dal prop. e se vuoto è
                      // fisso jdbc/gdm.
                      // Se si vuole quindi fare un test e non si è sul tomcat è necessario "entrare" con una connection
                      // e non più con il file properties .... da scommentare...
                      //dbOp = SessioneDb.getInstance().createIDbOperationSQL(GL.INIT_JNDI,0);
                      //System.out.println("OPEN INI--->"+dbOp);

                      //ATTENZIONE! dovrà continuare a usare l'ini se l'if DI SOTTO E' VERO

                      /*if (GL.isUsaIniPerTest()) {

                      }*/

                      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Global.ALIAS_ORACLE,
									                		  				GL.URL_ORACLE,
									                		  				GL.INIT_DB_USER,
									                		  				GL.INIT_DB_PASSWD);


            	  }
                  else {
                	// System.out.println("jndi");
            		  dbOp = SessioneDb.getInstance().createIDbOperationSQL(GL.JNDIPARM.getJndiString(),0);
            		 // System.out.println("OPEN JNDI--->"+dbOp);
                  }
               }
               else {            	  
                  dbOp = SessioneDb.getInstance().createIDbOperationSQL(GL.CONNECTION,0);
                  dbOp.autoCommitOff();
                 // System.out.println("OPEN CONN--->"+dbOp);
                //  System.out.println("conn");
                  //createSavePoint(dbOp);
               }
              }
              catch (Exception e)
              {
            	e.printStackTrace();
                throw new Exception("ManageConnection::connectToDB\n alias:"+Global.ALIAS_ORACLE+
                                    " url:"+GL.URL_ORACLE+
                                    " user:"+GL.INIT_DB_USER+
                                    " pwd:"+GL.INIT_DB_PASSWD+
                                    " conn: "+GL.CONNECTION+                                    
                                    " jdni: "+((GL.JNDIPARM==null)?"null":GL.JNDIPARM.getJndiString())+ 
                                    " - Errore: "+e.getMessage());
              }
              
              /* STAMPA DEL DEBUG DI CONNESSIONE */
              if (Global.DEBUG) {
                 tracer.fillInStackTrace();// Registra lo stato dello stack di sistema
                 System.out.println("******************* DEBUG CONNESSIONE *******************");
                 
                 if (GL.CONNECTION==null) 
                     System.out.println("Open Connection Standard");
                 else
                     System.out.println("Open Connection External");
                     
                 System.out.println("Called Method:                 "+tracer.getStackTrace()[1].toString());
                 System.out.println("dbOp:                          "+dbOp);       
                 System.out.println("Connection (se esterna):       "+GL.CONNECTION);
                 System.out.println("Jndi 					:       "+GL.JNDIPARM.getJndiString());
                 System.out.println("*********************************************************");
              }
              /* FINE STAMPA DEL DEBUG DI CONNESSIONE */
             
              dbOp.autoCommitOff();
                            
              return dbOp;
         }
         else
             return null;
         
         
  }
  
  /*
   * METHOD:      disconnectFromDB(DbOperationSQL, 
   *                               boolean,
   *                               boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  Disconnessione al Database
   *               onlyClose = true -> Effettua solo la close
   *               commitOrRollback = true -> Effettua la commit
   *               commitOrRollback = false -> Effettua la rollback
   *               
   * RETURN:      void
  */  
  public void disconnectFromDB(IDbOperationSQL dbOp,
                               boolean onlyClose, 
                               boolean commitOrRollback) throws Exception
  { 	  
	
         /* STAMPA DEL DEBUG DI CONNESSIONE */
         if (Global.DEBUG) {
            tracer.fillInStackTrace();// Registra lo stato dello stack di sistema
            System.out.println("******************* DEBUG CONNESSIONE *******************");                        
            System.out.println("Close Connection");
            System.out.println("Called Method:    "+tracer.getStackTrace()[1].toString());
            System.out.println("dbOp:             "+dbOp);
            System.out.println("onlyClose:        "+onlyClose);
            System.out.println("commitOrRollback: "+commitOrRollback);
            System.out.println("Connection (se esterna):       "+GL.CONNECTION);
            System.out.println("*********************************************************");
         }
         /* FINE STAMPA DEL DEBUG DI CONNESSIONE */
  
         if (dbOp == null) return;
         
         if (dbOp.getStmSql()!=null) dbOp.getStmSql().clearParameters();
         
         // Effettua la chisura della connessione
         if (onlyClose || GL.CONNECTION!=null) 
            try {                    
            	//System.out.println("CLOSE--->"+dbOp);
              //if (commitOrRollback==false) rollbackToSavePoint(dbOp);
            	 //  System.out.println("close");            	
            	dbOp.close();
              
             // System.out.println("OPEN-->"+SessioneDb.contaOpen);
            //  System.out.println("CLOSE-->"+SessioneDb.contaClose);
              return;
            } catch (Exception e) {
              throw new Exception("ManageConnection::disconnectFromDB() close" 
                                  + e.getMessage());               
            }
         // Effettua il commit e poi chiude la connessione 
         if (commitOrRollback){
            try {                      
            	//System.out.println("commit");
            	//System.out.println("COMMIT--->"+dbOp);
              dbOp.commit(); 
              
              //rt_rebuild_index(dbOp);

              dbOp.close();
                           
            } 
            catch (Exception e) {
              dbOp.close();
              throw new Exception("ManageConnection::disconnectFromDB() commit and close" 
                                  + e.getMessage());
            }            
         }
         else {  
         // Effettua il roolback e poi chiude la connessione 
             try {              
            	 //System.out.println("ROLLBACK--->"+dbOp);
            	 dbOp.rollback();
            	// System.out.println("rollback");
                 dbOp.close();
             }
             catch (Exception e) {
            	 dbOp.close();
                 throw new Exception("GD4_Documento::disconnectFromDB() rollback and close" 
                                     + e.getMessage()); 
             }       
        }
  }
  
  /*
   * METHOD:      rt_rebuild_index(DbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  Effettua il rebuild degli indici intermedia (VALO)
   *               
   * RETURN:      void
  */  
  public void rt_rebuild_index(IDbOperationSQL dbOp) throws Exception {
	  	 try {
	  		if (GL.USE_INTERMEDIA.equals("S") && 
	  		    GL.REBUILD_IMMEDIATE.equals("S")) {
	     		
	  			dbOp.setStatement("begin "+
	     		  			 	  "si4.sql_execute('alter session set NLS_LANGUAGE=AMERICAN'); "+
	     		  			 	  "RT_REBUILD_VAL_CLOB_INDEX; "+
	                              "end;");
	     		dbOp.execute();     		
	     		
	     	}
	  	 } 
         catch (Exception e) {
           throw new Exception("ManageConnection::rt_rebuild_index() " 
                               + e.getMessage());
         }
	  	 
  }
  
  /*
   * METHOD:      createSavePoint(String, DbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  
   *               
   * RETURN:      void
  */  
  public void createSavePoint(IDbOperationSQL dbOp) throws Exception {
	  	 try {
	  			
	  		/*System.out.println("begin "+
	  			 	  "DBMS_TRANSACTION.SAVEPOINT('SAVEPNT"+dbOp.toString().substring(dbOp.toString().indexOf("@")+1,dbOp.toString().length())+"'); "+
                    "end;");*/
	  		 
	  		    dbOp.setStatement("begin "+
	     		  			 	  "DBMS_TRANSACTION.SAVEPOINT('SAVEPNT"+dbOp.toString().substring(dbOp.toString().indexOf("@")+1,dbOp.toString().length())+"'); "+
	                              "end;");
	  			
	  			
	  			//System.out.println("--->"+"DBMS_TRANSACTION.SAVEPOINT('PIPPO"+dbOp.toString().substring(dbOp.toString().indexOf("@")+1,dbOp.toString().length())+"'); ");	  			
	     		dbOp.execute();     		
	     		
	     	
	  	 } 
         catch (Exception e) {
           throw new Exception("ManageConnection::createSavePoint() " 
                               + e.getMessage());
         }
	  	 
  }
  
  /*
   * METHOD:      rollbackToSavePoint(String, DbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  
   *               
   * RETURN:      void
  */  
  public void rollbackToSavePoint(IDbOperationSQL dbOp) throws Exception {
	  	 try {
	     		
	  		/*System.out.println("begin "+
	  			 	  "DBMS_TRANSACTION.ROLLBACK_SAVEPOINT('SAVEPNT"+dbOp.toString().substring(dbOp.toString().indexOf("@")+1,dbOp.toString().length())+"'); "+
                    "end;");*/
	  		 
	  		    dbOp.setStatement("begin "+
	     		  			 	  "DBMS_TRANSACTION.ROLLBACK_SAVEPOINT('SAVEPNT"+dbOp.toString().substring(dbOp.toString().indexOf("@")+1,dbOp.toString().length())+"'); "+
	                              "end;");
	  			//System.out.println("--->"+"DBMS_TRANSACTION.ROLLBACK_SAVEPOINT('PIPPO"+dbOp.toString().substring(dbOp.toString().indexOf("@")+1,dbOp.toString().length())+"'); ");
	  			
	     		dbOp.execute();     		
	     		
	     	
	  	 } 
         catch (Exception e) {
	  	   //  e.printStackTrace();
           throw new Exception("ManageConnection::rollbackToSavePoint() " 
                               + e.getMessage());
         }
	  	 
  }  
  
   /*
   * METHOD:      creaSessioneDb()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Crea la Sessione
   *
   * RETURN:      void
  */
  public void creaSessioneDb() throws Exception
  {
	 // System.out.println("-->"+SessioneDb.version());
         SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,
                                           Global.DRIVER_ORACLE);
  }
  
  public String retrievePassword(IDbOperationSQL dbOp, String userDb, String istanza) throws Exception {	
		String sql ="select rtrim(translate( translate(substr(PASSWORD_ORACLE,7,3),chr(7),' ')|| "+
		            "translate(substr(PASSWORD_ORACLE,4,3),chr(7),' ')||" +
		           " translate(substr(PASSWORD_ORACLE,1,3),chr(7),' ')||" +
		          "  substr(PASSWORD_ORACLE,10)"+
		           " ,chr(1)||'THE'||chr(5)||'qui'||chr(2)||'k1y2'"+
		           " ||chr(4)||'OX3j~'||chr(3)||'p4@V#R5lazY6D%GS7890'||"+
		            " chr(11)||'the'||chr(15)||'QUI'||chr(12)||'K'||chr(16)||'d'||chr(17)||chr(14)||'ox'||chr(18)||'J'||chr(20)||chr(13)||'P'||chr(19)||chr(21)||'v'||chr(22)||'r'"+
		          " ,'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~@#%'||"+
		            "'abcdefghijklmnopqrstuvwxyz'"+
		          " ))"+
			 	  " from ad4_istanze "+
			 	"where USER_ORACLE = :P_USERDB and istanza =:P_ISTANZA ";
		
		try {
			
			 dbOp.setStatement(sql);
			
			 dbOp.setParameter(":P_USERDB", userDb);
			 dbOp.setParameter(":P_ISTANZA", istanza);
	    	 dbOp.execute();
	    	 
	    	 ResultSet rst = dbOp.getRstSet();
	    	 
	    	 if (!rst.next()) throw new Exception("Impossibile recuperare password per utente oracle "+userDb);
	    	 
	    	 return rst.getString(1);
		}
	    catch (Exception e) {	  
	    	throw new Exception("ManageConnection::retrievePassword Errore in recupero password oracle di utente oracle "+userDb+".\nErrore: "+e.getMessage());
	    }			
	}
  
}
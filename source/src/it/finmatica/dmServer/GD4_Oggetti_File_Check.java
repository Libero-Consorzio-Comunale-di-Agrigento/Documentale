package it.finmatica.dmServer;

import java.sql.ResultSet;

import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class GD4_Oggetti_File_Check {
	   String idOggettoFile;
	   String utente;
	   Environment varEnv;
	   boolean bCloseEnv;
	   IDbOperationSQL dbOp = null;
	   private boolean bIsNew=false;

	   public GD4_Oggetti_File_Check(String idObjFile,String user, Environment vu) throws Exception {
		   	  idOggettoFile=idObjFile;
		   	  utente=user;
		   	  varEnv=vu;
		   	  bCloseEnv=false;
	   }

	   public GD4_Oggetti_File_Check(String ar, String cm, String cr, String nomeFile, String user, Environment vu) throws Exception {		   
		      varEnv=vu;		      
		      idOggettoFile=getIdOggettoFile(ar,cm,cr,nomeFile);		      
		      utente=user;
		      bCloseEnv=true;
	   }	   

	   /**
	    * Metodo che inserisce una registrazione
	    * o aggiorna su oggetti_file_check per utente
	    * e oggetto_file checcando il max della versione 
	    * 
	    * La transazione qui è autonoma e non segue quella
	    * del documento perché questo metodo verrà richiamato
	    * solo in retrieve del file e non in "registrazione"
	    * del documento. Segue che il commit va dato qua stesso
	    * ed è anche giusto perché quando faccio una retrieve
	    * di un file è sicuro che l'ho chiesto e quindi questa
	    * informazione va scritta in tabella OGGETTI_FILE_CHECK
	    * indipendentemente dalle altre operazione effettuate sul
	    * documento stesso.
	    * 
	    * @throws Exception
	   */
	   public void checkObjFileToUser() throws Exception {	
		      if (idOggettoFile==null) return;
		 
		   	  		      		   	 
		      StringBuffer sStm = new StringBuffer("");
		      
		      /*sStm.append("SELECT count(*) ");
		      sStm.append("FROM OGGETTI_FILE_CHECK ");
		      sStm.append("WHERE UTENTE=:USER ");
		      sStm.append("  AND ID_OGGETTO_FILE=:P_OGG_FILE ");*/
		      //System.out.println("BONFY->"+sStm.toString());
		      try {
		        dbOp = connect();
		        
		        /*dbOp.setStatement(sStm.toString());
		        
		        dbOp.setParameter(":USER",utente);
		        dbOp.setParameter(":P_OGG_FILE",idOggettoFile);  

		        dbOp.execute();

		        ResultSet rst = dbOp.getRstSet();
		        rst.next();
		        
		        if (rst.getLong(1)!=0) {*/
		        /*	sStm = new StringBuffer("MANAGE_OGGETTIFILECHECK('UPDATE',:P_OGG_FILE,:USER)");				    				    
		        }
		        else {*/
		        	sStm = new StringBuffer("MANAGE_OGGETTIFILECHECK(:P_OGG_FILE,:USER)");	
		        //}
		        
		        dbOp.setCallFunc(sStm.toString());
		        
		        dbOp.setParameter(":USER",utente);
		        dbOp.setParameter(":P_OGG_FILE",idOggettoFile);

		        dbOp.execute();
		        
		        close();
		        
		        //dbOp.commit();
		        //dbOp.close();
		        //System.out.println("BONFY3->COMMIT");
		      }	   	
		      catch (Exception e) {
		    	
		    	try {close();}catch(Exception ei){}
		    	  //e.printStackTrace();		    
		    	throw new Exception("GD4_Oggetti_File_Check::checkObjFileToUser()\n" + e.getMessage());
		      }   
		      
	   }
	   
	   /**
	    * Metodo che controlla se per la coppia
	    * (utente,id_oggetto_file) il numCheck
	    * è Uguale del max numCheck esistente
	    * 	    
	    * @return Stringa nulla se numCheck della coppia (utente,id_oggetto_file) è UGUALE del max numCheck esistente
	    *         Messaggio non bloccante se numCheck della coppia (utente,id_oggetto_file) è MINORE del max numCheck esistente
	    * @throws Exception
	   */	   
	   public String checkUserToObjFile() throws Exception {
		   	  if (idOggettoFile==null) return null;
		   
		   	  
		   	  long num, maxNum;
		   	  String ret=null,ut="";
		   
		      //Controllo se la versione 
		   	  StringBuffer sStm = new StringBuffer("");
		   	  sStm.append("SELECT ");
		   	  sStm.append("NVL((SELECT NUM_CHECK FROM OGGETTI_FILE_CHECK WHERE UTENTE=:USER   AND ID_OGGETTO_FILE=:P_OGG_FILE),1),");
		   	  sStm.append("NVL((SELECT MAX(NUM_CHECK) FROM OGGETTI_FILE_CHECK WHERE ID_OGGETTO_FILE=:P_OGG_FILE),1) ");		   	 
		   	  sStm.append("FROM DUAL");	
		   	  
		   	  
		   	  StringBuffer	sStmUtente = new StringBuffer("");
		      sStmUtente.append("SELECT nominativo ");
		   	  sStmUtente.append(" FROM OGGETTI_FILE_CHECK,ad4_utenti "); 
		   	  sStmUtente.append("WHERE ID_OGGETTO_FILE=:P_OGG_FILE ");
		   	  sStmUtente.append("AND NUM_CHECK = (SELECT MAX(NUM_CHECK) FROM OGGETTI_FILE_CHECK WHERE ID_OGGETTO_FILE=:P_OGG_FILE) ");
		   	  sStmUtente.append("AND OGGETTI_FILE_CHECK.UTENTE=ad4_utenti.utente ");
		   	  sStmUtente.append(" order by OGGETTI_FILE_CHECK.data_aggiornamento ASC ");
			  		   	 
			  try {												
				dbOp = varEnv.getDbOp();
		        dbOp.setStatement(sStm.toString());
		        
		        dbOp.setParameter(":USER",utente);
		        dbOp.setParameter(":P_OGG_FILE",idOggettoFile);

		        dbOp.execute();
		        
		        ResultSet rst = dbOp.getRstSet();
		        rst.next();
		        
		        num=rst.getLong(1);
		        maxNum=rst.getLong(2);
		        
		        
		        dbOp.setStatement(sStmUtente.toString());
		        
		        dbOp.setParameter(":USER",utente);
		        dbOp.setParameter(":P_OGG_FILE",idOggettoFile);

		        dbOp.execute();
		        
		        rst = dbOp.getRstSet();
		        if (rst.next()) {	
		        	ut=rst.getString(1);
		        }
		        
		        if (num < maxNum) {
		        	ret="Il file è stato modificato dall'utente "+ut+" con una versione più aggiornata.\nVuoi sovrascriverla?";
		        }
		        
		        if (bCloseEnv) varEnv.disconnectClose();
		        			        
		        return ret;
			  }
		      catch (Exception e) {		
		    	try {if (bCloseEnv) varEnv.disconnectClose();}catch (Exception ei) {/*DONTCARE*/}
			    throw new Exception("GD4_Oggetti_File_Check::checkUserToObjFile()\n" + e.getMessage());
			  }  			  
	   }
	    
	   /**
	    * Metodo che aggiorna una registrazione
	    * su oggetti_file_check per utente
	    * e oggetto_file checcando il max della versione +1
	    * 
	    * @throws Exception
	   */	   
	   public void updateCheck() throws Exception {
		      if (idOggettoFile==null) return;
		      
		   	  
		   	  StringBuffer sStm = new StringBuffer("");
		   	  		   	  
		   	  try {
		        dbOp = varEnv.getDbOp();
		        
	        	sStm.append("UPDATE ");
	        	sStm.append("OGGETTI_FILE_CHECK ");
	        	sStm.append("SET NUM_CHECK=OGFI_CHK_SQ.NEXTVAL, ");
	        	sStm.append("    DATA_AGGIORNAMENTO=SYSDATE ");
			    sStm.append("WHERE UTENTE=:USER ");
			    sStm.append("  AND ID_OGGETTO_FILE=:P_OGG_FILE ");			
			    
		        dbOp.setStatement(sStm.toString());
		        
		        dbOp.setParameter(":USER",utente);
		        dbOp.setParameter(":P_OGG_FILE",idOggettoFile);

		        dbOp.execute();			    
		      }	   	
		      catch (Exception e) {		    	
		    	throw new Exception("GD4_Oggetti_File_Check::updateCheck()\n" + e.getMessage());
		      }   		        		        
	   }
	   
	   private String getIdOggettoFile(String area, String cm, String cr, String nomeFile) throws Exception {
		       StringBuffer sStm = new StringBuffer("");
		       
		       String idOggettoFile=null;
	      
		       sStm.append("select oggetti_file.ID_OGGETTO_FILE ");
		       sStm.append("from documenti, tipi_documento, oggetti_file ");
		       sStm.append("where documenti.area=:ar ");
		       sStm.append("and documenti.codice_richiesta=:cr ");
		       sStm.append("and documenti.id_tipodoc=tipi_documento.id_tipodoc ");
		       sStm.append("and tipi_documento.nome=:cm ");
		       sStm.append("and oggetti_file.id_documento=documenti.id_documento ");
		       sStm.append("and oggetti_file.filename=:fn");
		       
		       try {
		    	 varEnv.connect();
				 dbOp = varEnv.getDbOp();
				 
				 dbOp.setStatement(sStm.toString());
			        
			     dbOp.setParameter(":ar",area);
			     dbOp.setParameter(":cr",cr);
			     dbOp.setParameter(":cm",cm);
			     dbOp.setParameter(":fn",nomeFile);
			     
			     dbOp.execute();
			     
			     ResultSet rst = dbOp.getRstSet();
			     if (rst.next())
			    	 idOggettoFile=rst.getString(1);
			     /*else
			    	 throw new Exception("Attenzione! non ho trovato l'id_oggetto file "+
			    			             "per i seguenti parametri passati: ar/cm/cr/nomeFile "+
			    			             area+"/"+cm+"/"+cr+"/"+nomeFile);*/
			     
			     
			   }
		       catch (Exception e) {			    	
			     throw new Exception("GD4_Oggetti_File_Check::getIdOggettoFile()\n" + e.getMessage());
			   }  			
		       
		       return idOggettoFile;
	   }
	   
	   private IDbOperationSQL connect() throws Exception {
	           if (varEnv.getDbOp()==null) {
	              bIsNew=true;
	              return (new ManageConnection(varEnv.Global)).connectToDB();
	           }
	        
	           return varEnv.getDbOp();
	  }
	  
	  private void close() throws Exception {
	          if (bIsNew) (new ManageConnection(varEnv.Global)).disconnectFromDB(dbOp,true,false);        
	  }	   
}

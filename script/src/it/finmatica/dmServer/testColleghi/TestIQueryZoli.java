package it.finmatica.dmServer.testColleghi;

import java.io.BufferedInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.management.IQueryCollection;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

public class TestIQueryZoli {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			String xml = null;
			String[] condizioni;
			String Stato="PROPOSTA#DETERMINA";
			Connection conn= null;
		      
		    
			IQueryCollection Iqc;
			
			try {
			  Class.forName("oracle.jdbc.driver.OracleDriver");
			  conn=DriverManager.getConnection("jdbc:oracle:thin:@jvm-efesto:1521:ORCL","GDM","GDM");
			  
			  String query = "SELECT 1,FILE_ORIGINALE FROM MODELLI "+
              "WHERE AREA='MMA' AND CODICE_MODELLO='MB1'";
              

			  IDbOperationSQL dbOp=SessioneDb.getInstance().createIDbOperationSQL(conn,0);
    
    dbOp.setStatement(query);      
 
    dbOp.execute();

    ResultSet rst = dbOp.getRstSet();
     
     if (rst.next()) {
       BufferedInputStream bis = dbOp.readClob("FILE_ORIGINALE");   
       StringBuffer sb = new StringBuffer();       
       int ic;
       while ((ic =  bis.read()) != -1) {
         sb.append((char)ic);
       }
     }	  
			  /*condizioni = Stato.split("#");
			  
			  IQuery[] Iq = new IQuery[condizioni.length] ;
			  Iqc = new IQueryCollection();
			  
			  Iqc.initVarEnv("GDM", "GDM", conn);
			 	
			  for (int k=0;k<condizioni.length;k++){
					System.out.println(k);
					
					IQuery iq = new IQuery();
					
					iq.initVarEnv("GDM", "GDM", conn);
					
					iq.settaArea("SEGRETERIA.ATTI");
					iq.addCodiceModello("DETERMINA");
					
					iq.addCampo("ANNO_DETERMINA", "2009");
					iq.addCampo("N_DETERMINA","100");
					iq.addCampo("TIPO_ATTO","DETERMINA");
					Iq[k] = iq;																								
					//se va OK gestisco l'array di Iquery
					Iqc.addIQuery(Iq[k]);
												
			  }	
			  
			  IQuery ris= Iqc.getIQueryRicerca();
			  
			  if (ris.ricercaFT().booleanValue()){
				  System.out.println(ris.getProfileNumber());
				  Profilo profilo = (Profilo)ris.getProfileFromIndex(0);
				  
				  System.out.println(profilo.getCampo("TIPO_PROPOSTA"));
			  }
			  else{
				  System.out.println(ris.getError());
			  }*/
				  
			}catch (Exception e) {
			  e.printStackTrace();
			}
		}

}

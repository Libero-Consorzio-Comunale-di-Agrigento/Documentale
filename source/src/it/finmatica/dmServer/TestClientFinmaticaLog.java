package it.finmatica.dmServer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.util.Global;

public class TestClientFinmaticaLog {
	final static String CONNECTION_EXTERN="EXTERN";
    final static String CONNECTION_STANDARD="STANDARD";
    
	public static void main(String[] args) throws Exception 
	{
		                
       String casoConnection=CONNECTION_EXTERN;
       Connection conn=null;

       //------------------------------------CONNESSIONE ESTERNA-------------------------------------
       if (casoConnection.equals(CONNECTION_EXTERN)) {
           Class.forName("oracle.jdbc.driver.OracleDriver");
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.31:1521:GDMTEST","GDM","GDM");
           conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora03:1521:GDMTEST","GDM","GDM");
           //CONNESSIONE A SOTHIX GARDA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.70.80:1521:orcl","GDM","GDM");

           //CONNESSIONE ACHILLE
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@achille:1521:orcl8","GDM","GDM");           
           
           conn.setAutoCommit(false);
       }                   
      
       //ProfiloLog p = new ProfiloLog("F1","MANNY","DMSERVER832");             
       ProfiloLog p = new ProfiloLog("14320183");
       
       //--------------------------------CARICAMENTO DELL'ENVIRONMENT--------------------------------
       if (casoConnection.equals(CONNECTION_EXTERN))
          p.initVarEnv("GDM","GDM", conn);
       else                                 
          p.initVarEnv("GDM","GDM","S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");
       
       /*if (p.accedi().booleanValue()) {
    	   System.out.println(p.getCampo("OGGETTO"));
    	         	   
       }
       else {
           System.out.println("Impossibile accedere");
           System.out.println(p.getError());                      
           if (casoConnection.equals(CONNECTION_EXTERN)) {
               conn.rollback();         
           }
       }*/
           
       Environment vu = new  Environment("GDM","","","","",conn);
       vu.inizializza();
       vu.setUser("GDM");
       AccediDocumento ad = new AccediDocumento("14435271",vu); 
       //ad.accediLogDocumento("16212308");
       ad.accediDocumentoAllegati();
       
       System.out.println(ad.listaOggettiFile());
       
      /* GD4_Oggetti_File ogfi = new GD4_Oggetti_File();
       ogfi.inizializzaDati(vu);
       ogfi.setIdOggettoFile("10682108"); 
       
       ogfi.retrieveLog("16212308");
     
       String nomeFile = ogfi.getFileName();
       InputStream isStream = (java.io.InputStream)ogfi.getFile();
       
       */
       
       /*if (p.accediAllLog().booleanValue()) {
    	   ResultSetValoriLog rst =p.getResultSetValoriLog();
    	  
    	   System.out.println("\tAZIONE\tDATA\t\t\tUTENTE");
    	   while(rst.next()) {
    		   System.out.println("______________________________________________________________");
    		   System.out.println("\t"+rst.getAzione()+"\t"+rst.getDataLog()+"\t"+rst.getUtenteLog());    		   
    		   System.out.println("\n\t\tCOGNOME\t\tNOME\t\tPIVA");
    		   System.out.println( "\t\t"+Global.nvl(rst.getValore("COGNOME"),"NC")
    				              +"\t\t"+
    				                      Global.nvl(rst.getValore("NOME"),"NC")
    				              +"\t\t"+        
    				                      Global.nvl(rst.getValore("PIVA"),"NC"));    		      		  
    	   }
       }
       else {
           System.out.println("Impossibile accedere");
           System.out.println(p.getError());                      
           if (casoConnection.equals(CONNECTION_EXTERN)) {
               conn.rollback();         
           }
       }
*/
	}

}

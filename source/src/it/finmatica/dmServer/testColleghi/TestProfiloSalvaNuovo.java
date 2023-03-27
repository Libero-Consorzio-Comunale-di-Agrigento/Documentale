package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.ElapsedTime;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestProfiloSalvaNuovo {
 

	public static void main(String[] args) throws Exception {
		   Connection conn=null;
		   Class.forName("oracle.jdbc.driver.OracleDriver");
		   
		   conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora03:1521:ORCL","GDM","GDM");          
           conn.setAutoCommit(false);
           
           Profilo p = new Profilo("M_ORIZZONTALE","TESTADS");
           
           p.initVarEnv("GDM","",conn);
           
           p.escludiControlloCompetenze(true);
           p.setSkipAddCompetenzeModello(true);
           p.setSkipReindexFullTextField(true);
           
           p.setAlwaysNew(true);
           
           for(int i=0;i<2;i++) {
	           p.settaValore("TEST_CHECK","1");
	           p.settaValore("TEXT_AREA","saass1");
	           p.settaValore("RICH_TEXT_AREA","aaaa"+i);
	           
	           /*if (i==0)
	        	   p.setFileName("c:/bfile.txt");
	           else
	           	   p.setFileName("c:/InstallationInfo.txt");*/
	           
	     	   ElapsedTime elpsTime = new ElapsedTime("TEST",null);
	  	       elpsTime.start("INSERT","INIZIO");
	           if (p.salva().booleanValue()) {
	        	   elpsTime.stop();
	          	   System.out.println("CODRICH: " + p.getCodiceRichiesta());
	               System.out.println("N° Documento: " + p.getDocNumber());	               
	           }
	           else {
	        	   conn.rollback();
	        	   elpsTime.stop();
	               System.out.println(p.getError());
	               System.exit(0);
	           }
           }
           
           conn.commit();
           conn.close();
          	      	  
	}

}

package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.FileSystemUtility;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestAlVolo {
	public static void main(String[] args) throws Exception {
		   Connection conn=null;
		   Class.forName("oracle.jdbc.driver.OracleDriver");
		   
		   //13897971 documento vecchio modo con record C
		   
		   conn=DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=10.27.46.4)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=10.27.46.3)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=SG)))","ZDM","ZDM");          
		   conn.setAutoCommit(false);
		   
		  // Profilo p = new Profilo("M_ORIZZONTALE","TESTADS");
		//   Profilo p = new Profilo("13898331");
           
         //  p.initVarEnv("GDM","",conn);
           
          // p.setOgfiLog(false);
           
         //  p.setFileName("C:\\exp\\bcmail-jdk15.jar");
         //  p.setFileName("C:\\exp\\finmatica-jsign.jar");
           
         //  p.setFileName("C:\\exp\\exp.xml");
      //    p.setFileName("C:\\exp\\exportSolvin.txt");
       //    p.setFileName("C:\\exp\\modutils.jar");
        //   p.setFileName("C:\\exp\\ProductData.xml");
           
       //  	p.setDeleteFileName("modutils.jar");	
         // 	p.setDeleteFileName("finmatica-jsign.jar");
         	
        //  	
          // LetturaScritturaFileFS fs = new LetturaScritturaFileFS("C:\\exp\\exportSolvin.txt");
         //  p.setFileName("bcmail-jdk15.jar", fs.leggiFile());
           
          //	LetturaScritturaFileFS fs = new LetturaScritturaFileFS("C:\\exp\\modutils.jar");
         // 	p.renameFileName("finmatica-jsign.jar", "nuovo jsign.jar", fs.leggiFile());
           //Testare anche la rename!!!
          
         //  p.salva();
        //   conn.commit();
           
        //   System.out.println(p.getDocNumber());
		
		
		//FileSystemUtility.deleteAllPathifisEmpty("C:\\temp\\11\\22\\gorilla.txt", "C:\\temp");
	}
}

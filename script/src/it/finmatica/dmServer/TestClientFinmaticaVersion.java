package it.finmatica.dmServer;

import it.finmatica.dmServer.management.ProfiloVersion;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

public class TestClientFinmaticaVersion {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		 Connection conn = null;
		 try { 
			 Class.forName("oracle.jdbc.driver.OracleDriver");
			 //conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora04:1521:JSUITE","GDM","GDM");
			 conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora03:1521:GDMTEST","GDM","GDM");

			 conn.setAutoCommit(false);

			 //ProfiloVersion pv = new ProfiloVersion("12416079",2);
			 ProfiloVersion pv = new ProfiloVersion("13898106",1);
			 pv.initVarEnv("GDM","GDM",conn);

			 //VERSIONA
			 pv.versiona();

			 conn.commit();

			 //VERSIONA
			 
			 
			 //ACCEDI
			/* if (pv.accedi().booleanValue()) {
				 Vector<GD4_Oggetti_File> v=pv.getListaFile();
				 
				 for(int i=0;i<v.size();i++) {
					 GD4_Oggetti_File obj = v.get(i);
					 
					 System.out.println(obj.getIcona());
					 System.out.println(obj.getFileName());
					 System.out.println(obj.getIdOggettoFile());
					 System.out.println(obj.getIdLog());
					 System.out.println(obj.getPathObjFile());	
					 System.out.println(obj.isVisibleVariable()); 
					 
					 LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\temp\\"+obj.getFileName());
					 
					 f1.scriviFile(pv.getFileStream(obj.getFileName()));					 
					 
				 }
			 }
			 else {
				 System.out.println("ERRORE ACCESSO VERSIONE -->"+pv.getError());
			 }		*/
			 //ACCEDI			 
			 
			 
		 }
		 catch (Exception e) {
			 try {conn.rollback();}catch (Exception ei) {}
			 e.printStackTrace();
		 }
		 
	}

}

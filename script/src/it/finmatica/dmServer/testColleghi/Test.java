package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.management.ProfiloVersion;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;


public class Test {
	public static void main(String[] args) throws Exception {
		
		//
		Connection conn=null;
		   Class.forName("oracle.jdbc.driver.OracleDriver");
		   
		   //13897971 documento vecchio modo con record C
		   
		   conn=DriverManager.getConnection("jdbc:oracle:thin:@oracleads.provanco.priv:1521:SG","GDM","GDM");          
		   conn.setAutoCommit(false);
		//LetturaScritturaFileFS f = new LetturaScritturaFileFS("/jdocattach/DOCUMENTALE/SEG/$ALPR/9259/9259616/485640");        
       
        //InputStream is = f.leggiFile();
        //LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS("/workarea/testFile/485640"); 
       // fs2.scriviFile(is);
		   
		   Profilo p = new Profilo("9259616");
		   p.initVarEnv("RPI","",conn);
		   
		   p.accedi(Global.ACCESS_ATTACH);
		   InputStream is = p.getFileStream(1);
		   LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS("/workarea/testFile/485640"); 
	        fs2.scriviFile(is);
		   conn.close();
	}
	
	

}

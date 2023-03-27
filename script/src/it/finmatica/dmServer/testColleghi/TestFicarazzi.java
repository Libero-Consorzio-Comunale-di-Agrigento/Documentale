package it.finmatica.dmServer.testColleghi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Array;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Struct;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FilenameUtils;

import oracle.jdbc.OracleTypes;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.gdmSyncro.GDMSyncroCore;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.motoreRicerca.ResultSetIQuery;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.jfc.utility.DateUtility;

public class TestFicarazzi {
		
	public static void main(String[] args) throws Exception {
		
			
				
			
			SessioneDb.getInstance().addAlias("oracle.", "oracle.jdbc.driver.OracleDriver");	
			
			IDbOperationSQL dbOpSql = SessioneDb.getInstance().createIDbOperationSQL("oracle.","jdbc:oracle:thin:@test-efesto:1521:ORCL","GDM","GDM");       
				
			/*sStm.append("select INTEGRAZIONE_GDM_GDMSYNCRO.GET_INFORMAZIONI_SERVIZIO(2) record from dual");
			
			dbOpSql.setStatement(sStm.toString());
			dbOpSql.setOutParameter("record",  Types.ARRAY);
			dbOpSql.execute();
			ResultSet rst = dbOpSql.getRstSet();
			rst.next();
			Object[] data = (Object[]) ((Array) rst.getObject(1)).getArray();
			for (int i=0;i<data.length;i++ ) {
				Struct row = (Struct) data[i];
				
				for(Object attribute : row.getAttributes()) {
					System.out.println(attribute);
				}
			}*/
		/*SessioneDb.getInstance().addAlias("oracle.", "oracle.jdbc.driver.OracleDriver");	
		IDbOperationSQL dbOpSql = SessioneDb.getInstance().createIDbOperationSQL("oracle.","jdbc:oracle:thin:@svi-ora03:1521:GDMTEST","GDM","GDM");
		Environment newEn = new Environment("GDM","","","","",dbOpSql.getConn());
		GDMSyncroCore gdmSync = new GDMSyncroCore("3","12680548","10000043",newEn);*/
		/*String areaOrigine="FATTURAPA.ENTE";
		String areaLike = areaOrigine.substring(areaOrigine.lastIndexOf(".")+1) ;
		System.out.println(areaLike);*/
		
		
			File file = new File("C:\\XAREA_FATTUREPA.ENTE\\SQL\\PREAREA.SQL"); 
		  
		  BufferedReader br = new BufferedReader(new FileReader(file)); 
		  
		  String st; 
		  StringBuffer stm= new StringBuffer("");
		  while ((st = br.readLine()) != null)  stm.append(st);
		    
		 // System.out.println(stm.toString()); 
		 String s = stm.toString();
		s= s.replaceAll(":ENTE_ORIGINALE", "ENTE");
		s= s.replaceAll(":ENTE", "ENTE");
		 
		System.out.println(s); 
		  dbOpSql.setStatement(s);
		  
		  try {	
		  dbOpSql.execute();
		  
		  dbOpSql.rollback();
		  dbOpSql.close();
		  } catch (Exception e) {
				e.printStackTrace();
			  }
		
		
	}
	
}


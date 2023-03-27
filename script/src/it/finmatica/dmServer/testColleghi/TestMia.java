package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.Impronta.SeganalazioniVerificaImpronte;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;
 
public class TestMia {
	  public static void main(String[] args) throws Exception {
		  Class.forName("oracle.jdbc.driver.OracleDriver");
		
		  SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,Global.DRIVER_ORACLE);
		   
		   IDbOperationSQL dbOp;
		   dbOp = SessioneDb.getInstance().createIDbOperationSQL(Global.ALIAS_ORACLE,
												  				 "jdbc:oracle:thin:@test-efesto-lnx:1521:ORCL",
												  				 "GDM",
												  				 "GDM");	
		   dbOp.getConn().setAutoCommit(false);
		  String CODICE_AREA="TESTADS";
		  String CODICE_MODELLO ="M_ORIZZONTALE";
		  
		  Calendar cal = Calendar.getInstance();
	      Timestamp now1 = new Timestamp(cal.getTimeInMillis());	
		  
			  String query = "DECLARE P_RET NUMBER; BEGIN "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'TEST_CHECK', to_clob('XXMANNY') ); "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'TEST_COMBO', to_clob('1') ); "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'TEST_RADIO', to_clob('1') ); "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'TEXT_AREA', to_clob('TEST') ); "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'STATO_AVANZAMENTO_FLUSSO', to_clob('1') ); "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'RICH_TEXT_AREA', to_clob('aa') ); "+
				"GDM_PROFILO_TEST.ADDCAMPO ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', 'CART_AUTO', to_clob('x') ); ";			
				for(int i=0;i<400;i++)  { 
					
					query+="P_RET:= GDM_PROFILO_TEST.crea_documento ( '"+CODICE_AREA+"', '"+CODICE_MODELLO+"', NULL, 'GDM', 1) ; ";
				}
				query+="  END;";
			    //update documenti set conservazione = P_RET where id_documento ="+iddoc+";
			  
			  	dbOp.setStatement(query);
			  	dbOp.execute();
				dbOp.commit();				
		  
		  
		  dbOp.close();
		  cal = Calendar.getInstance();
	      Timestamp now2 = new Timestamp(cal.getTimeInMillis());
	      System.out.println("-->"+(now2.getTime() - now1.getTime() ));
	    }
}

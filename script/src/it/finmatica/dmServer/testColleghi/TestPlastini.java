package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestPlastini {
	public void test(String file) throws Exception  {
		IDbOperationSQL dbOp=null;
		try {
			SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,
	                  Global.DRIVER_ORACLE);
	        Class.forName("oracle.jdbc.driver.OracleDriver");
	        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.14:1521:ORCL","GDM_TEST","GDM_TEST");
	        String sql;
	        sql="INSERT INTO TBL_TESTBLOB (ID,COLONNABLOB) VALUES (6,:P_PAR)";
	        dbOp = SessioneDb.getInstance().createIDbOperationSQL(conn,0);
	        dbOp.autoCommitOff();
	
	        LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS(file);
	     
	        dbOp.setStatement(sql);
	        dbOp.setParameter(":P_PAR",  fs2.leggiFile(),  fs2.leggiFile().available());  
	        dbOp.execute();
	        
	        dbOp.commit();
		}
		catch(Exception e) {
			e.printStackTrace();
			dbOp.commit(); 
			throw e;
			
		}
	}
	
	public static void main(String agrs[]) throws Exception {
		//TestPlastini tp = new TestPlastini();
		//tp.test("c:\\archivio.rar");
		
		System.out.println("dasd sdaERRRE ddasd  asdads".indexOf("ERRORE")!=-1);
		
        //Environment vu = new Environment(user,"","","","",conn);
        //LookUpDMTable lUpDmT = new LookUpDMTable(vu);
	   	       
	   // String idDoc=lUpDmT.lookUpIdDocumentoFromIdAllegato(""+idAllegato);	
	       
      //  Profilo p = new Profilo("1405");
       //  p.initVarEnv("GDM", "" ,conn);
            
            //p.settaValore("CLOB", "taapa-+");
         //  LetturaScritturaFileFS fs = new LetturaScritturaFileFS("c:\\b.txt");
          // p.setFileName("provatabular\tioo",fs.leggiFile());
            
          /*  if (p.salva().booleanValue()) {
                conn.commit();
                System.out.println("OK");
            } else {
                conn.rollback();
               System.out.println("Errore: "+p.getError());
            }
            */
       /*     if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {
            	
            }
            else {
            	
            }
            */
           /* LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS("c:\\b2.txt");
            fs2.scriviFile(p.getFileStream("provatabular\tioo"));*/
            
            
		//LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS("");
		/*for (int i=5001;i<1000000;i++) {
			String Dir = "C:\\Program Files\\Apache Software Foundation\\Tomcat 5.5\\webapps\\jgdm\\upload\\a"+i+"\\"+i;
		    boolean success = (new File(Dir)).mkdirs();
		    System.out.println("dir-> a"+i+", creata="+success);
		}*/


    }
}

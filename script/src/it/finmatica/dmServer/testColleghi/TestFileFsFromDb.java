package it.finmatica.dmServer.testColleghi;

import java.util.Calendar;
import java.util.GregorianCalendar;

import it.finmatica.dmServer.util.FileFsFromDb;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

public class TestFileFsFromDb {
		public static void main(String[] args) throws Exception {
			   SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,Global.DRIVER_ORACLE);
			   
			   IDbOperationSQL dbOp;
			   dbOp = SessioneDb.getInstance().createIDbOperationSQL(Global.ALIAS_ORACLE,
													  				 "jdbc:oracle:thin:@jvm-efesto:1521:ORCL",
													  				 "GDM",
													  				 "GDM");			 
			 		 
		       FileFsFromDb fsDb = new FileFsFromDb("430",dbOp,"C:/temp/filefs");
		       
		       fsDb.makeTransport();
		     
		       dbOp.commit();
		       dbOp.close();
		       
		      /* String s="200000000"; 
		       
		       if (s.length()>2) s=s.substring(0, 2);		
		       
		       System.out.println(s);*/
			
			Calendar calendar = GregorianCalendar.getInstance();
		      System.out.println("ANNO CORRENTE:" + calendar.get( Calendar.YEAR ));
		}
}

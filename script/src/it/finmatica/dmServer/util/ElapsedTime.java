package it.finmatica.dmServer.util;

import java.sql.Timestamp;
import java.util.Calendar;
import it.finmatica.dmServer.Environment;

public class ElapsedTime {
	   private String objectToElapsed;
	   private String methodToElapsed;
	   private String message;
	   private Timestamp now;
	   private Environment varEnv;
	   
	   private long ldifferenz = 0;
	   
	   public ElapsedTime(String objectToEl, Environment vEnv) {
		      objectToElapsed=objectToEl;
		      varEnv=vEnv;
	   }
	   
	   public void start(String methodTo, String mess) {
		      if (varEnv!=null && !varEnv.Global.LOG_SQL.equals("S")) return;
		   
		      Calendar cal = Calendar.getInstance();
		      now = new Timestamp(cal.getTimeInMillis());		     
		      methodToElapsed=methodTo;
		      message=mess;
		      
		      System.out.println("_______________________________________________________________________________________"+
		    		  			 "\n[INFO ELAPSED TIME]\n\tSTART Object: "+objectToElapsed+" - method: "+methodToElapsed+"\n\t\t Azione: "+mess);
	   }

	   public void stop() {	
		      if (varEnv!=null &&!varEnv.Global.LOG_SQL.equals("S")) return;
		   
		      Calendar cal = Calendar.getInstance();
		      Timestamp nowFinish;
		      nowFinish = new Timestamp(cal.getTimeInMillis());
   
		      ldifferenz = nowFinish.getTime() - now.getTime();

		      System.out.println("\n\tSTOP Object: "+objectToElapsed+" - method: "+methodToElapsed+
		    		  			 "\n\t\t ElapsedTime: ("+ldifferenz+" msec)"+
		    		  			 "\n_______________________________________________________________________________________");
	   }
	   
	   public long getLastElpsTime() {
		      return ldifferenz;
	   }
	   
	   public void addMsg(String mess) {
		      if (varEnv!=null &&!varEnv.Global.LOG_SQL.equals("S")) return;
		      
		      System.out.println("\n\t\t "+mess);		   
	   }
}

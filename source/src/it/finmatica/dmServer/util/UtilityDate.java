package it.finmatica.dmServer.util;

import it.finmatica.jfc.utility.DateUtility;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UtilityDate {
       
	    public static String timeStampToString(Timestamp t,String f) {			   
			   SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss.S");
			   String property = t.toString();
			   
			   String out=null;
			   
			   try {					
				java.util.Date dataOut = (java.util.Date) formatter.parse(property, new java.text.ParsePosition(0));
				SimpleDateFormat formatter1 = new SimpleDateFormat (f);
				out = formatter1.format(dataOut);
			   }
			   catch(Exception exp){}
				
			   return out; 
        }
	    
	    public static String dateToStringISO8601(Date jsqlD) {	
	    	   SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");
	    	   String sData=ts.format(jsqlD);
	    	   sData=sData.substring(0,sData.indexOf(".")+1)+"000Z";
	    	   return sData;
	    }
	    
	    public static String now(String format) {
	    	   java.util.Calendar cal = Calendar.getInstance();
        	   java.sql.Timestamp now = new java.sql.Timestamp(cal.getTimeInMillis());
                
               return timeStampToString(now,format);
	    }
	    
	    public static java.sql.Date StringToJavaSQLDate(String sDate) {
	    	   String format="dd/MM/yyyy";
	    	   
	    	   if (sDate.length()==19) format+=" HH:mm:ss";
	    	   
	    	   DateUtility d = new DateUtility();
	    	   if (!d.isDateValid(sDate,format)) return null;
	    	   
	    	   SimpleDateFormat ts = new SimpleDateFormat(format);
        	   
	    	   try {
        	     return new java.sql.Date(ts.parse(sDate).getTime());
	    	   }
	    	   catch(Exception e) {
	    		 return null; 
	    	   }
	    }
	    	    
}

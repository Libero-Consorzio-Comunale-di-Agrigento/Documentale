package it.finmatica.dmServer.testColleghi;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//
//import java.util.regex.Pattern;
//
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.dom4j.Document;
//import org.dom4j.DocumentHelper;
//
public class TestMonoRecord  {
	public static void main(String[] args) throws Exception {
		
	
		Calendar cal = Calendar.getInstance();
		java.sql.Timestamp now = new java.sql.Timestamp(cal.getTimeInMillis());
		java.sql.Date jsqlD = new java.sql.Date(now.getTime());
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dataInizio = "05/10/2016 10:45:21";//df.format(jsqlD);			
		
		System.out.println("--->"+dataInizio);
		
		 SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		 java.util.Date parsed = format.parse(dataInizio);
	     java.sql.Date sql = new java.sql.Date(parsed.getTime());
	     
		
//		String xml11pattern = "[^"
//            + "\u0001-\uD7FF"
//            + "\uE000-\uFFFD"
//            + "\ud800\udc00-\udbff\udfff"
//            + "]+";
//		String sXML="<ROOT>&#16;From: Benedetta Mussini</ROOT>";
//		//&#16;
//		//sXML=sXML.replaceAll(xml11pattern,sXML);
//		sXML=stripNonValidXMLCharacters(sXML);
//		
//		/*StringEscapeUtils s;
//		sXML=s.escapeXml11(sXML);*/
//		Document xml= DocumentHelper.parseText(sXML);
//		
//		
//	}
//	
//	/**
//     * Remove all characters that are valid XML markups.
//     * http://www.w3.org/TR/2000/REC-xml-20001006#syntax
//     *
//     * @param s
//     * @return
//     */
//    public static String removeXMLMarkups(String s)
//    {
//        StringBuffer out = new StringBuffer();
//        char[] allCharacters = s.toCharArray();
//        for (char c : allCharacters)
//        {
//            if ((c == '\'') || (c == '<') || (c == '>') || (c == '&') || (c == '\"'))
//            {
//                continue;
//            }
//            else
//            {
//                out.append(c);
//            }
//        }
//        return out.toString();
//    }
//
//	
//	   public static String stripNonValidXMLCharacters(String in) {
//	        StringBuffer out = new StringBuffer(); // Used to hold the output.
//	        char current; // Used to reference the current character.
//
//	        if (in == null || ("".equals(in))) return ""; // vacancy test.
//	        for (int i = 0; i < in.length(); i++) {
//	            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
//	            if ((current == 0x9) ||
//	                (current == 0xA) ||
//	                (current == 0xD) ||
//	                ((current >= 0x20) && (current <= 0xD7FF)) ||
//	                ((current >= 0xE000) && (current <= 0xFFFD)) ||
//	                ((current >= 0x10000) && (current <= 0x10FFFF)))
//	                out.append(current);
//	        }
//	        return out.toString();
//
	    }     
	   
}

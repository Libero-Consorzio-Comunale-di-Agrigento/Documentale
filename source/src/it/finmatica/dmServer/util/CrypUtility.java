package it.finmatica.dmServer.util;

import it.finmatica.jfc.crypto.TripleDESEncrypter;
import it.finmatica.jfc.utility.Base64;

public class CrypUtility {
	
	 	
	public CrypUtility() {}
		
    public static String criptare(String valore) throws Exception {
	      String result="";
	      try {
	    	  byte[] key = Base64.f_decode("bnC/ubQOdqFuWF6Kdm6BKRE7cGSSW097");
	    	  TripleDESEncrypter enc = new TripleDESEncrypter(key, "ECB", "PKCS5Padding");
	    	  result=enc.encrypt(valore);
	      }
	      catch (Exception e) 
          {
	        throw new Exception("CrypUtility::criptare(valore)::("+valore+") - Errore:"+e.getMessage());
          }   
	      return result;
   }	 
  
   public static String decriptare(String valore) throws Exception {
	      String result="";
	      try {
	    	  byte[] key = Base64.f_decode("bnC/ubQOdqFuWF6Kdm6BKRE7cGSSW097");
	    	  TripleDESEncrypter enc = new TripleDESEncrypter(key, "ECB", "PKCS5Padding");
	    	  result=enc.decrypt(valore);
	      }
	      catch (Exception e) 
          {
	        throw new Exception("CrypUtility::decriptare(valore)::("+valore+") - Errore:"+e.getMessage());
          }  
	      return result;
   }	

}

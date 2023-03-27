package it.finmatica.dmServer;

import it.finmatica.dmServer.util.ValoriLogStruct;
import java.lang.ArrayIndexOutOfBoundsException;
import java.util.HashMap;
import java.util.Vector;

public class ResultSetValoriLog {
	   private Vector<ValoriLogStruct> vValoriLogStruct = null;
	   private int lPointer;	   
	   
	   public ResultSetValoriLog(Vector<ValoriLogStruct> vVal) {
		   	  vValoriLogStruct = vVal;
		   	  lPointer=-1;
	   }
	   
	   public boolean next() {
		  	 try {
		  	   //Cerco di capire se si può accedere al prox valore		  	   		  		
		  		vValoriLogStruct.get(++lPointer);
		  	 }
		  	 catch (Exception e) {
		  	   return false;	 
		  	 }
		  	 
		  	 return true;
	  }
	   
	  public String getAzione() throws Exception {
		     try {
		       return vValoriLogStruct.get(lPointer).getAzione();
		     }
		     catch (ArrayIndexOutOfBoundsException aiobe) {
		       if (lPointer==-1)
		    	   throw new Exception("ResultSetValoriLog:getAzione - Non è stata chiamata la next");
		       else
		    	   throw new Exception("ResultSetValoriLog:getAzione - Elemento n° "+lPointer+" inesistente");
		     }
		     catch (Exception e) {
		       throw new Exception("ResultSetValoriLog:getAzione - Errore:\n"+e.getMessage());
		     }
	  }
	  
	  public String getDataLog() throws Exception {
		     try {
		       return vValoriLogStruct.get(lPointer).getDataAggiornamento();
		     }
		     catch (ArrayIndexOutOfBoundsException aiobe) {
		       if (lPointer==-1)
		    	   throw new Exception("ResultSetValoriLog:getDataLog - Non è stata chiamata la next");
		       else
		    	   throw new Exception("ResultSetValoriLog:getDataLog - Elemento n° "+lPointer+" inesistente");
		     }
		     catch (Exception e) {
		       throw new Exception("ResultSetValoriLog:getDataLog - Errore:\n"+e.getMessage());
		     }
	  }	  

	  public String getUtenteLog() throws Exception {
		     try {
		       return vValoriLogStruct.get(lPointer).getUtenteAgg();
		     }
		     catch (ArrayIndexOutOfBoundsException aiobe) {
	    	   if (lPointer==-1)
		    	   throw new Exception("ResultSetValoriLog:getUtenteLog - Non è stata chiamata la next");
		       else
		    	   throw new Exception("ResultSetValoriLog:getUtenteLog - Elemento n° "+lPointer+" inesistente");
		     }
		     catch (Exception e) {
		       throw new Exception("ResultSetValoriLog:getUtenteLog - Errore:\n"+e.getMessage());
		     }
	  }	  
	  
	  /**
	   * Restituisce  il valore del campo dato un nome di campo. null se il
	   * campo non viene trovato
	   * 
	   * @param nomeCampo
	   * @return
	   * @throws Exception
	  */
	  public String getValore(String nomeCampo) throws Exception {
		  	 try {
		  	   HashMap hmVal = vValoriLogStruct.get(lPointer).getHmCampiValori();
		  	   
		  	   if (!hmVal.containsKey(nomeCampo))
		  		  return null;
		  	   
		  	   if ((""+hmVal.get(nomeCampo)).equals("null"))
		  	      return "";
		  	   else
		  		  return ""+hmVal.get(nomeCampo);		  	   		       
		     }
		     catch (ArrayIndexOutOfBoundsException aiobe) {
		       if (lPointer==-1)
			       throw new Exception("ResultSetValoriLog:getValore - Non è stata chiamata la next");
			   else
			       throw new Exception("ResultSetValoriLog:getValore - Elemento n° "+lPointer+" inesistente");
		     }
		     catch (Exception e) {
		       throw new Exception("ResultSetValoriLog:getValore - Errore:\n"+e.getMessage());
		     }
	  }
}

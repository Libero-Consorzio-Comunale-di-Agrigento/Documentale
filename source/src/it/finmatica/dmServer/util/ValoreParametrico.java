package it.finmatica.dmServer.util;

public class ValoreParametrico {
	   private Object value;
	   private String nomeColonna,nomeParametro;
	   
	   public ValoreParametrico(Object newValue,String newnomeColonna, String newnomeParametro) {
		      value=newValue;
		      nomeColonna=newnomeColonna;
		      nomeParametro=newnomeParametro;
	   }
	   
	   public Object getValue() {
		      return value;
	   }
	   
	   public String getNomeColonna() {
		      return nomeColonna;
	   }

	   public String getNomeParametro() {
		      return nomeParametro;
	   }	
}
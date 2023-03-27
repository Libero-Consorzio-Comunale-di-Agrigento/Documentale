package it.finmatica.dmServer.util;

public class DocumentException extends Exception {
	   private String code, msg;
	   
	   public DocumentException(String cod, String msg) {
		      super(msg);
		      code=cod;
	   }
	   
	   public String getMessage() {
		      return super.getMessage();
	   }
	   
	   public String getCode() {
		      return code;
	   }
}

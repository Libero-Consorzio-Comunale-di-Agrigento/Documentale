package it.finmatica.dmServer.util;

public class ObjFileConditionStruct {
	   private String textSearch;
	   private boolean bIsOcr;
	   
	   public ObjFileConditionStruct(String text, boolean ocr) {
		      textSearch=text;
		      bIsOcr=ocr;		    
	   }
	   
	   public String getTextSearch() {
		      return textSearch;
	   }
	   
	   public boolean isOcr() {
		      return bIsOcr;
	   }
}

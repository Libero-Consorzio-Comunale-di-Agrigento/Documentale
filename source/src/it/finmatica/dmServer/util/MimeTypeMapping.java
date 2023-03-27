package it.finmatica.dmServer.util;

import java.util.HashMap;

public class MimeTypeMapping {
	   public static final String MIME_TEXT_PLAIN = "text/plain";
	   public static final String MIME_IMAGE_JPEG = "image/jpeg"; 
	   public static final String MIME_APPLICATION_PDF = "application/pdf"; 
	   public static final String MIME_IMAGE_GIF = "image/gif"; 
	   public static final String MIME_APPLICATION_VND_MSEXCEL = "application/vnd.ms-excel"; 
	   public static final String MIME_APPLICATION_MSWORD = "application/msword";
	   public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
	   public static final String MIME_APPLICATION_XML = "application/xml";
	   public static final String MIME_TEXT_HTML = "text/html";

	   private HashMap<String, String> mimeTypeMapping = new HashMap<String,String>();
	   
	   public MimeTypeMapping() {
		      mimeTypeMapping.put(MIME_TEXT_PLAIN, "TXT");
		      mimeTypeMapping.put(MIME_IMAGE_JPEG, "JPG");
		      mimeTypeMapping.put(MIME_APPLICATION_PDF, "PDF");
		      mimeTypeMapping.put(MIME_IMAGE_GIF, "GIF");
		      mimeTypeMapping.put(MIME_APPLICATION_VND_MSEXCEL, "XLS");
		      mimeTypeMapping.put(MIME_APPLICATION_MSWORD, "DOC");
		      mimeTypeMapping.put(MIME_APPLICATION_OCTET_STREAM, "EXE");
		      mimeTypeMapping.put(MIME_APPLICATION_XML, "XML");
		      mimeTypeMapping.put(MIME_TEXT_HTML, "HTML");
	   }
	   
	   public String getFileExtFromMime(String mimeType) {
		      if (mimeTypeMapping.containsKey(mimeType))
		    	  return mimeTypeMapping.get(mimeType);
		      else
		    	  return "GENERICO";
	   }
}

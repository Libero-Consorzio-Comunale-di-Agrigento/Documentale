package it.finmatica.dmServer.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.finmatica.stampaunione.connector.DictionaryConnection;
import it.finmatica.stampaunione.engine.EngineASCIITransformer;
import it.finmatica.stampaunione.tag.StringTagDef;
import it.finmatica.stampaunione.tag.transformer.RuleTransformWithOutTag;

public class RtfUtil {
	   private EngineASCIITransformer engine;
	   private DictionaryConnection dictionary =null;
	   private RuleTransformWithOutTag rule = null;
		
	   private static final String TAG_INIZIALE  ="[_";
	   private static final String TAG_FINALE    ="_]";
	   private static final String VARIABLE      ="TAG_FIRMA_DIGITALE";
	   
	   private InputStream iRtf;	
	   private String      textToFooter;
	   private String      idDocumento;
	   
	   public RtfUtil(InputStream iFile, String txtFoot, String id) {
		      iRtf=iFile;
		      textToFooter=txtFoot;
		      idDocumento=id;
	   }
	   
	   public byte[] getRtfFooter() throws Exception {	
		      //ByteArrayOutputStream outPDF = null;
		      byte[] ret;
		      
		      engine = new EngineASCIITransformer();
		     
		      try {
		        engine.setStreamToConvert(iRtf);
		      }
		      catch (Exception e) {			
			    throw new Exception("PdfUtil::getRtfFooter Errore nella lettura del file\nErrore: "+e.getMessage());
   		      }
		      
		      addTagDefinition( TAG_INIZIALE, TAG_FINALE);
		      
		      rule = new RuleTransformWithOutTag();
		
			  addRule(VARIABLE, textToFooter );
			  
			  rule.addOrderListConnector(dictionary);
		
		      engine.addTagRules(rule);
		      
		       try {
				  File file = new File("./"+idDocumento+".rtf");
				  			  
				  engine.parseFile(file,true);			  			 	
				  				  				  
				  ret=getBytesFromFile(file);
				  /*InputStream inputStream = new FileInputStream(file);
				  outPDF = new ByteArrayOutputStream();
	    		  for (int n; (n = inputStream.read()) != -1;) 	    			  
	    			  outPDF.write((byte)n);
	    		  
	    		  inputStream.close();*/
	    		  
	    		  //Tento di eliminare il file
	    		  for(int conta=0;conta<10;conta++) {
	    		     file.delete();
	    		     if (!file.canRead()) conta=10;
	    		  }
    		   }
		       catch (Exception e) {			
			      throw new Exception("PdfUtil::getRtfFooter Errore nel parsing del file\nErrore: "+e.getMessage());
   		       }
		      
    		  return ret;			  
	   }

	   private void addTagDefinition(String startTag, String EndTag){
			   engine.addTag(new StringTagDef(startTag, EndTag, RuleTransformWithOutTag.class.getName()));
	   }
		
		
	   private void addRule(String NOME_VARIABILE, String VALORE_VARIABILE){
			   if(dictionary == null)
				  dictionary = new DictionaryConnection();
			   dictionary.setVariableToValues(NOME_VARIABILE, VALORE_VARIABILE);
	   }	   
	   
	   // Returns the contents of the file in a byte array.
	   private  byte[] getBytesFromFile(File file) throws IOException {
	            InputStream is = new FileInputStream(file);
	    
	            // Get the size of the file
	            long length = file.length();
	    
		        // You cannot create an array using a long type.
		        // It needs to be an int type.
		        // Before converting to an int type, check
		        // to ensure that file is not larger than Integer.MAX_VALUE.
		        if (length > Integer.MAX_VALUE) {
		            throw new IOException("Il file "+file.getName()+" è troppo grosso, non posso caricarlo in memoria ");
		        }
	    
	            // Create the byte array to hold the data
	            byte[] bytes = new byte[(int)length];
	    
	            // Read in the bytes
	            int offset = 0;
	            int numRead = 0;
	            while (offset < bytes.length
	                  && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	                 offset += numRead;
	            }
	    
	            // Ensure all the bytes have been read in
	            if (offset < bytes.length) {
	               throw new IOException("Could not completely read file "+file.getName());
	            }
	    
	            // Close the input stream and return bytes
	            is.close();
	            return bytes;
	    }
	   
}

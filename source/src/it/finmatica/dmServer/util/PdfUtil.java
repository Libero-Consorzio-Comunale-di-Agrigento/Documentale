package it.finmatica.dmServer.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class PdfUtil {
	   public InputStream iPdf;	
	   public String      textToFooter;
	   
	   public PdfUtil(InputStream iFile, String txtFoot) {
		      iPdf=iFile;
		      textToFooter=txtFoot;
	   }
	   
	   public byte[] getPdfFooter() throws Exception { 			  
		      PdfReader reader = null;
		      PdfStamper stamp = null;
		      BaseFont bf = null;
		      ByteArrayOutputStream outPDF = null;
		      int nPage=0;
		      
		      try {
		    	  reader = new PdfReader(iPdf);
		    	  nPage = reader.getNumberOfPages();
		      }
		      catch(Exception e){
				  throw new Exception("PdfUtil::getPdfFooter Errore nella lettura del file\nErrore: "+e.getMessage());
			  }              
              
		      try {
			      outPDF = new ByteArrayOutputStream();
	              stamp = new PdfStamper(reader,outPDF);
	              bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
		      }
		      catch(Exception e){
				  throw new Exception("PdfUtil::getPdfFooter Errore nella creazione dello Stamper o del Font\nErrore: "+e.getMessage());
			  } 	              
              
		      if (!Global.nvl(textToFooter, "").trim().equals("")) {
			      try {
		              int i=0;
		              while (i++ < nPage) {            	              	  
		            	  PdfContentByte over = stamp.getOverContent(i);
		            	  
		            	  over.beginText();
		            	  over.setFontAndSize(bf, 12);
		            	  over.setTextMatrix(10, 5);
		            	  over.showText(textToFooter);             	              
		            	  over.endText();
		              }	  
		                                          
		              stamp.close();
	              }
			      catch(Exception e){
					  throw new Exception("PdfUtil::getPdfFooter Errore nella scrittura del footer nel PDF\nErrore: "+e.getMessage());
				  }    
			      
			    	  
		      } 
		      else {		    	  
		    	  stamp.close();
		      }
		      
		      return outPDF.toByteArray();
	   }
}

package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;

import java.sql.*;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.io.ByteArrayOutputStream;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class TestPDF {
	public InputStream iPdf;
	public String      namePdf;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			try {
			  TestPDF tPDF= (new TestPDF());
			  
			  tPDF.riempiProfilo("49211");
				
			  /*PdfReader reader = new PdfReader(tPDF.iPdf);
              int n = reader.getNumberOfPages();
              
              System.out.println("Il file "+tPDF.namePdf+" trovato con n° pagine: "+n);
              
              ByteArrayOutputStream outPDF = new ByteArrayOutputStream();
              PdfStamper stamp = new PdfStamper(reader,outPDF);
              BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
              
              //Ciclo su tutte le pagine per aggiungere il FOOTER
              int i=0;
              while (i++ < n) {            	              	  
            	  PdfContentByte over = stamp.getOverContent(i);
            	  
            	  over.beginText();
            	  over.setFontAndSize(bf, 18);
            	  over.setTextMatrix(30, 30);
            	  over.showText("page " + i);             	              
            	  over.endText();
              }	  
                                          
              stamp.close();
              
              FileOutputStream f = new FileOutputStream("c:\\pino.pdf");
              
              f.write(outPDF.toByteArray());*/
			}
			catch(Exception e){
				e.printStackTrace();
			}
	}
	
	public void riempiProfilo(String id) throws Exception {
		   try {
		     Class.forName("oracle.jdbc.driver.OracleDriver");
		     //SICILIA
		     Connection conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
           
		     conn.setAutoCommit(false);
		     
		     Profilo p = new Profilo(id);
		     
		     p.initVarEnv("GDM","GDM",conn);
		     
		     if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {
		    	 //iPdf=p.getFileStream(1);
		    	 //namePdf=p.getFileName(1);
		    	 FileOutputStream f = new FileOutputStream("c:\\a.rtf");
              
		    	 f.write(p.getPdfFooter("PROVA.rtf"));
		     }
           }
		   catch(Exception e){
			 e.printStackTrace();
		   }
	}

}

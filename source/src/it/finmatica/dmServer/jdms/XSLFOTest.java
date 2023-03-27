package it.finmatica.dmServer.jdms;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import be.re.css.CSSToXSLFOException;
/*
import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.XMLSerializer;
import org.dom4j.*;*/


public class XSLFOTest {

	
/*	public static void main(String[] args) throws Exception	{
		
	 	try
	 	{

         String intest="<font style=\"FONT-SIZE: 140%;\">JDms - &lt; Gestione documentale</font>";
        
         Document ret= DocumentHelper.parseText (intest);
         //System.out.println("*** "+ret.asXML());
          
         
         
         Document doc = DocumentHelper.createDocument();
         
         
         Element p=DocumentHelper.createElement("p");
       	 p.addText("Ciaoa");
       	 p.addCDATA(ret.asXML());      	 
         doc.add(p);
         
        
         
        
         
         
         //String s=doc.asXML()+ret.asXML();
         String s=doc.asXML();
         String t=ret.asXML();
         System.out.println("1--->"+s);
         System.out.println("2--->"+t);
         
         
         //Document ret= DocumentHelper.parseText (s);
         //System.out.println("*** "+ret.asXML());

		
	 	 }
    	 catch (Exception e){
    		 e.printStackTrace();
    	 }
}	
	
*/	
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws CSSToXSLFOException 
	 * @throws MalformedURLException 
	 * @throws TransformerConfigurationException 
	 */
	/*public static void main(String[] args) throws Exception	{
		
		 	try
		 	{

	         //String intest="<font style=\"FONT-SIZE: 140%;\">JDms - Gestione documentale</font>";
	         
	         String intest="<info><html xmlns=\"http://www.w3.org/1999/xhtml\">Ciao</html></info>";
	         
	          Document doc = new DocumentImpl();
              // Create Root Element
		      Element root = doc.createElement("p");
              root.appendChild(doc.createTextNode(intest));
		      
		      //root.appendChild(doc.createCDATASection(intest));
		      
		      // Add the Root Element to Document
		      doc.appendChild(root);
		      
		      
		      //Serialize DOM
		      OutputFormat format    = new OutputFormat (doc); 
		      // as a String
		      StringWriter stringOut = new StringWriter ();    
		      XMLSerializer serial   = new XMLSerializer (stringOut, format);
		      serial.serialize(doc);
		      // Display the XML
		      System.out.println(stringOut.toString());

			
		 	 }
	    	 catch (Exception e){
	    		 e.printStackTrace();
	    	 }
	}	*/
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws CSSToXSLFOException 
	 * @throws MalformedURLException 
	 * @throws TransformerConfigurationException 
	 */
	public static void main(String[] args) throws TransformerConfigurationException, MalformedURLException, CSSToXSLFOException, SAXException, ParserConfigurationException, TransformerFactoryConfigurationError, IOException 
	{
		
		 	try
		 	{
		 		
		 	FOEngine engine = new FOEngine();
			//create_input_stream()
			String InputFilePath = "C:"+ File.separator + "download"+ File.separator +"XSLFO"+ File.separator +"test"+ File.separator +"Input2.html";
			File InputFile = new File(InputFilePath);
			String OutputFoFilePath = "C:"+ File.separator + "download"+ File.separator +"XSLFO"+ File.separator +"test"+ File.separator +"Output5.fo";
			File OutputFoFile = new File(OutputFoFilePath);
			String OutputPdfFilePath = "C:"+ File.separator + "download"+ File.separator +"XSLFO"+ File.separator +"test"+ File.separator +"Output5.pdf";
			File OutputPdfFile = new File(OutputPdfFilePath);
			FileInputStream fis = new FileInputStream(InputFile);
			FileOutputStream fosFO = new FileOutputStream(OutputFoFile);
			ByteArrayOutputStream baosFO = new ByteArrayOutputStream(); 
			FileOutputStream fosPDF = new FileOutputStream(OutputPdfFile);
			engine.convertXHTML2FO(fis,InputFile.toURL(),baosFO);
			ByteArrayInputStream baisFO = new ByteArrayInputStream(baosFO.toByteArray());
			FileInputStream fisFO = new FileInputStream(OutputFoFile);
			engine.convertFO2PDF(baisFO,fosPDF);
			
		 	 }
	    	 catch (Exception e){
	    		 e.printStackTrace();
	    	 }
	}	
	
}

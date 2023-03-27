package it.finmatica.dmServer.util;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.InputSource;

public class XMLUtilDom4j {
	   private Document xmlDocument;
	   
	   public XMLUtilDom4j(String xml) throws Exception {
		   	  try {		   		
		   		xmlDocument = DocumentHelper.parseText(xml);
		      }
			  catch (Exception e) {
			    throw new Exception("XMLUtilDom4j - Errore in parse XML ("+xml+")\n"+e.getMessage()); 
			  }
			   
			  if (xmlDocument == null)		      
			      throw new Exception("parseDatiXML - Document XML ("+xml+") nullo!");				   
	   }
	   
	   public XMLUtilDom4j(Document xml) {
		      xmlDocument=xml;
	   }
	   
	   public Element getRoot() throws Exception {
		      if (xmlDocument == null)
                  throw new Exception("XMLUtil::getRoot - xml document null");
		      
		      return xmlDocument.getRootElement();
	   }
	   
	   public String leggiValoreXML(String tagName) throws Exception {
		      String valore = null;
		       
		      if (xmlDocument == null)
                 throw new Exception("XMLUtil::leggiValoreXML - xml document null");
		       
		      Element root = xmlDocument.getRootElement();
		      for(Iterator iterator = root.elementIterator(); 
		           iterator != null && iterator.hasNext() && valore == null;) {
		    	   Element elemento = (Element)iterator.next();
		    	   
		    	   if(elemento != null && elemento.getName().toLowerCase().equals(tagName.toLowerCase()))
		    		  valore = elemento.getText();
		    	   else
		    		  valore = leggiValoreXML(elemento, tagName);
		      }

		      return valore;
       }
	   
       public String leggiValoreXML(Element e, String tagName) throws Exception {
    	      String valore = null;
    	      for(Iterator iterator = e.elementIterator(); 
    	          iterator != null && iterator.hasNext() && valore == null;) {
    	    	  Element elemento = (Element)iterator.next();
    	    	  
    	    	  if(elemento != null && elemento.getName().toLowerCase().equals(tagName.toLowerCase()))
    	    		 valore = elemento.getText();
    	    	  else
    	    		 valore = leggiValoreXML(elemento, tagName);
    	      }

    	      return valore;
       }	   
       
       public Element leggiElementoXML(Element e, String tagName) {
    	      Element elemento = null, eFound = null;
    	      
    	      for (Iterator iterator = e.elementIterator(); 
    	           iterator != null && iterator.hasNext() && eFound == null;) {
    	    	   elemento = (Element)iterator.next();
    	    	   if(elemento != null && elemento.getName().toLowerCase().equals(tagName.toLowerCase())) {
    	    		  eFound = elemento;
    	    	   } 
    	    	   else {
    	    		  eFound = leggiElementoXML(elemento, tagName);
    	    		  if ( eFound != null) {
    	    			  return eFound;
    	    		  }
    	    	   }
    	      }

    	      return eFound;
       }   
              
       public Vector<Element> leggiChildElementXML(Element e) {
    	   	  Vector<Element> vElelement=new Vector<Element>();
    	   	  
    	      for (Iterator iterator = e.elementIterator(); 
    	   	       iterator != null && iterator.hasNext();) {
    	    	  vElelement.add((Element)iterator.next());
    	   	  }
    	      
    	      return vElelement;
       }
       	
}

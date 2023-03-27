package it.finmatica.dmServer.mapping;

import java.io.FileInputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.*;
import it.finmatica.dmServer.util.Global;

/**
 * Classe che gestisce il parsing del file GDSYSTEM.XML
 * 
 * @author  G. Mannella
 * @version 2.8
 *
*/
public class XMLSystem {

	   /**
	    * Document XML del file GDSYSTEM.XML
	   */
	   Document XMLSystem;

	   /**
	    * ente e applicativo dai quali cercare il DM
	   */	   
	   String ente, applicativo;

	   /**
	    * Tag standard del file GDSYSTEM.XML
	   */	   
	   public final static String TAG_ROW                     = "ROW";  
  	   public final static String TAG_ROWITEM                 = "ROWITEM";	   
  	   public final static String TAG_VALUE_FINMATICA_DM      = "FINMATICA";

	   /**
	    * Costruttore
	    * 
	    * @param XMLSysFile percorso del file GDSYSTEM.XML
	   */	   
	   public XMLSystem(String XMLSysFile, String ente, String appl) throws Exception {
		   	  this.ente=ente;
		   	  this.applicativo=appl;
		   
		      //Leggo il file XML e lo metto nel document
		      readXMLSystem(XMLSysFile);		      		      
		      		      
	   }

	   /**
	    * Restituisce il DM dal file XML a partire da
	    * Ente ed Applicativo passati nel costruttore
	   */	   
	   public String getDM() {
		      String DM=Global.FINMATICA_DM;
		      NodeList nodes = XMLSystem.getElementsByTagName(TAG_ROW);
		      NodeList nodesChildsDescr;

		      if (nodes == null) return DM;		      		    
		      
		      for (int i=0; i<nodes.getLength(); i++){
		    	  nodesChildsDescr=nodes.item(i).getChildNodes();
		    	  
		    	  String applicativo_XML, ente_XML, DM_XML;
		    	  
		    	  applicativo_XML=nodesChildsDescr.item(0).getTextContent();
		    	  ente_XML=nodesChildsDescr.item(1).getTextContent();
		    	  DM_XML=nodesChildsDescr.item(3).getTextContent();

		    	  if (this.ente.equals(ente_XML) && this.applicativo.equals(applicativo_XML))
		    		  if (DM_XML.endsWith(TAG_VALUE_FINMATICA_DM))
		    		     return Global.FINMATICA_DM;
		    		  else
		    			 return Global.HUMMINGBIRD_DM;
		      }
		      
		      return DM;
	   }	  
	   
	   private void readXMLSystem(String file) throws Exception {
		       DOMResult       domResult;
		       FileInputStream fis;
		       
		       try {
		        	 fis = new FileInputStream(file);
		        	 TransformerFactory tFactory = TransformerFactory.newInstance();
		        	 Transformer transformer = tFactory.newTransformer();
		        	 domResult = new DOMResult();
		        	 transformer.transform(new StreamSource(fis), domResult);
		        	 XMLSystem = (Document)domResult.getNode();
		        	 fis.close();
		       } catch (Exception e) {
		    	  throw new Exception("XMLSystem:readXMLSystem - Errore in fase di lettura del file "+file+"\n Errore esteso: "+e.getMessage());
		       }
		      
	   }

}

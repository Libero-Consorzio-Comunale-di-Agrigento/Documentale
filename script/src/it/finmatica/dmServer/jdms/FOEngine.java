package it.finmatica.dmServer.jdms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
//JAXP
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.SAXException;

import be.re.css.CSSToXSLFOException;
import be.re.css.CSSToXSLFOFilter;

public class FOEngine 
{

    private static FopFactory fopFactory = FopFactory.newInstance();
    
	public FOEngine() {
	}
	
	public void convertXHTML2FO(InputStream is, URL baseUrl, OutputStream os) throws CSSToXSLFOException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, IOException{
		
		 try
		  {
			javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
	
			CSSToXSLFOFilter filter =
				new CSSToXSLFOFilter
				(
				 baseUrl, // base URL. Serve come url base per i css richiamati nel file			 
				 factory.newSAXParser().getXMLReader()
				 );
	
			javax.xml.transform.sax.TransformerHandler handler =
				(
				(javax.xml.transform.sax.SAXTransformerFactory)
				 javax.xml.transform.TransformerFactory.newInstance()
				).newTransformerHandler();
	
			// Imposto OutputStream os come risultato del handler 
			handler.setResult( new javax.xml.transform.stream.StreamResult(os));
			// Legame filter - handler
			filter.setContentHandler(handler);
			// Effettuo trasformazione
			filter.parse(new org.xml.sax.InputSource(is));	
		  } 
		  catch (Exception e) {
		      e.printStackTrace();
		  } 
	}
	
	public void convertXHTML2FO(InputStream is, OutputStream os) throws CSSToXSLFOException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, IOException{
		
		 try
		  {
	
			javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
	
			CSSToXSLFOFilter filter =
				new CSSToXSLFOFilter
				(
				 factory.newSAXParser().getXMLReader()
				 );
	
			javax.xml.transform.sax.TransformerHandler handler =
				(
				(javax.xml.transform.sax.SAXTransformerFactory)
				 javax.xml.transform.TransformerFactory.newInstance()
				).newTransformerHandler();
	
			// Imposto OutputStream os come risultato del handler 
			handler.setResult( new javax.xml.transform.stream.StreamResult(os));
			// Legame filter - handler
			filter.setContentHandler(handler);
			// Effettuo trasformazione
			filter.parse(new org.xml.sax.InputSource(is));	
		  } 
		  catch (Exception e) {
		      e.printStackTrace();
		  } 
	}
    public static void convertFO2PDF(InputStream is, OutputStream out) throws CSSToXSLFOException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, IOException{
		
//throws IOException, FOPException {
        
        
        try {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            // configure foUserAgent as desired
    
            // Setup output stream.  Note: Using BufferedOutputStream
            // for performance reasons (helpful with FileOutputStreams).
            // Può essere semplicemente new OutputStream()
            // o nelle servlet response.getOutputStream()
            //out = new BufferedOutputStream(out);

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer
            
            // Setup input stream
            // si può fare come StreamSource(InputStream inputStream)
            // quindi anzichè fo come file avere fo come InputSream
            Source src = new StreamSource(is);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler()); 
            
            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);            
           
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}

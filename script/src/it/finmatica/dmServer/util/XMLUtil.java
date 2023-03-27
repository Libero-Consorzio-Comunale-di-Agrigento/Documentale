package it.finmatica.dmServer.util;

/*
 * GESTIONE OGGETTI XML
 * 
 * AUTHOR @MANNELLA
 * DATE   19/09/2005
 * 
 * */

import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*; 
import org.w3c.dom.*;
import org.apache.xml.serialize.*;  


public class XMLUtil 
{
    
  /*
   * METHOD:      read_String_Xml(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Legge un stringa formattata e restituisce un
   *              oggetto XML Document.
   *              Usa le librerie java standard.
   * 
   * RETURN:      none
  */  
  public static Document read_String_Xml(String xmlSource) throws Exception {
    
    Document doc = null;
    try {
       DocumentBuilder builder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
             
        InputSource inStream = new InputSource();
        inStream.setCharacterStream(new StringReader(xmlSource));
         doc = builder.parse(inStream);
    } catch (Exception e) {
      throw new Exception(e.getMessage()+"<br>Errore in fase di creazione dell'oggetto xmlSource");
    }

    return doc;
  }
  
  /*
   * METHOD:      write(Document, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Scrive su un file XML il DOM indicato.
   *              La codifica è UTF-8.
   *              PreserveSpace è true.
   * 
   * RETURN:      void
  */ 
  public static void write(Document docXML, String filePath) throws Exception {
    try {
      OutputFormat format = new OutputFormat(docXML);
      format.setEncoding("UTF-8");
      
      format.setPreserveSpace(true); // Conserva i CR (per i campi testo)
      
      FileOutputStream fos = new FileOutputStream(filePath);
      XMLSerializer serializer = new XMLSerializer(fos, format);
      // A questo punto serilizzo il docXml che suppongo non essere nullo altrimenti 
      // la funzione di serialize genera direttamente la Exception che riporto al chiamante.
      serializer.serialize(docXML);		
    } catch (Exception e) {
      throw new Exception(e.getMessage()+"<br>Errore in fase di scrittura del file "+filePath+".");
    }
  } 
}
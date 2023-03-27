package it.finmatica.modulistica.domutility;

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class DomUtility {

	public static Document xmlToDocument(String xml) {
		 Document xmlDocument = null;
		try {
  		xmlDocument = DocumentHelper.parseText(xml);
		} catch (Exception e) {
			
		}
		return xmlDocument;
	}
	
	public static String leggiValoreXML(Document xmlDocument, String tagName) {
      String valore = null;
      if(xmlDocument == null)
          System.out.println("xml document null");
      Element root = xmlDocument.getRootElement();
      for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
      {
          Element elemento = (Element)iterator.next();
          if(elemento != null && elemento.getName().equals(tagName))
              valore = elemento.getText();
          else
              valore = leggiValoreXML(elemento, tagName);
      }

      return valore;
  }

	public static String leggiValoreXML(Element e, String tagName) {
      String valore = null;
      for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
      {
          Element elemento = (Element)iterator.next();
          if(elemento != null && elemento.getName().equals(tagName))
              valore = elemento.getText();
          else
              valore = leggiValoreXML(elemento, tagName);
      }

      return valore;
  }

	public static Element leggiElementoXML(Element e, String tagName) {
      Element elemento = null, eFound = null;;
      for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
      {
          elemento = (Element)iterator.next();
          if(elemento != null && elemento.getName().equals(tagName)) {
             eFound = elemento;
          } else {
              eFound = leggiElementoXML(elemento, tagName);
              if ( eFound != null) {
                return eFound;
              }
          }
      }

      return eFound;
  }

	public static Element aggFiglio(Element elp, String nome) {
    Element elf = DocumentHelper.createElement(nome);
    elp.add(elf);
    return elf;
  }

	public static Element aggFiglio(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    elf.setText(valore);
    elp.add(elf);
    return elf;
  }

}
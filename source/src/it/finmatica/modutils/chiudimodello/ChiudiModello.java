package it.finmatica.modutils.chiudimodello;

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ChiudiModello {

  public String chiudiPagina(String xml) {
    String retVal = "";
    String newLink = "";
    String gdc_link = "";
    Element root;
    
    try {
      int i = xml.indexOf("<FUNC");
      Document dInput = null;
      dInput = DocumentHelper.parseText(xml.substring(i,xml.length()));
      gdc_link = leggiValoreXML(dInput, "GDC_LINK");
    } catch (Exception e) {
      e.printStackTrace();
      gdc_link = "";
    }
    if (gdc_link == null || gdc_link.equalsIgnoreCase("")) {
      newLink = "../common/ClosePage.do";
    } else {
      newLink = gdc_link;
    }

    try {
      root = DocumentHelper.createElement("FUNCTION_OUTPUT");
      Document dDoc = DocumentHelper.createDocument();
      dDoc.setRootElement(root);
      aggFiglio(root,"RESULT","ok");
      aggFiglio(root,"ERROR","");
      aggFiglio(root,"REDIRECT",newLink);
      aggFiglio(root,"FORCE_REDIRECT","Y");
      aggFiglio(root,"DOC","");
      retVal = dDoc.asXML();
    } catch (Exception e) {
      e.printStackTrace();
      retVal = "<FUNCTION_OUTPUT><RESULT>ok</RESULT><ERROR></ERROR>"+
      "<REDIRECT>../common/ClosePage.do</REDIRECT>"+
      "<FORCE_REDIRECT>Y</FORCE_REDIRECT><DOC></DOC></FUNCTION_OUTPUT>";
    }
    return retVal;
  }

  private static String leggiValoreXML(Document xmlDocument, String tagName)
  {
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

  private static String leggiValoreXML(Element e, String tagName)
  {
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

/*  private static Element leggiElementoXML(Element e, String tagName)
  {
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
  }*/

  private Element aggFiglio(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    elf.setText(valore);
    elp.add(elf);
    return elp;
  }

}

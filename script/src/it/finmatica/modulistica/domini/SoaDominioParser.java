package it.finmatica.modulistica.domini;

import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.textparser.AbstractParser;

public class SoaDominioParser extends AbstractParser {
  private Document dInput = null;
  private static Logger logger = Logger.getLogger(SoaDominioParser.class);
  
  public SoaDominioParser(String parametri) throws Exception {
  	String val = "";
  	if (parametri == null) {
  		dInput = DocumentHelper.parseText("<PARAMETRI></PARAMETRI>");
  	} else {
  		val = parametri.substring(9, parametri.length()-3);
  		dInput = DocumentHelper.parseText("<PARAMETRI>"+val+"</PARAMETRI>");
  	}
  }

	@Override
	protected String findParamValue(String nomePar, Properties extraKeys) {
		String result = leggiValoreXML(dInput, nomePar);
		return result;
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

  /**
   * 
   */
  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }

}

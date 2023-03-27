package it.finmatica.modulistica.SOA;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XMLError {
  private final static String _XMLRET_NODE_RESULT_ERROR       = "error";
  private final static String _XMLRET_NODE_RESULT_TYPERROR    = "ERROR_PARAMETER";

  private String message;

  public XMLError(String textMessage) {
    message=textMessage;
  }

  public String getXML() {
    Element root, elp;

		Document docOut = DocumentHelper.createDocument(); 
  	root = DocumentHelper.createElement("message");
  	docOut.setRootElement(root);
  	aggFiglio(root, "result", _XMLRET_NODE_RESULT_ERROR);
  	aggFiglio(root, "type", _XMLRET_NODE_RESULT_TYPERROR);
  	aggFiglio(root, "code", "");
  	aggFiglio(root, "text", message);
     
    return docOut.asXML();
  }	   
  
	private static Element aggFiglio(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    elf.setText(valore);
    elp.add(elf);
    return elf;
  }

}

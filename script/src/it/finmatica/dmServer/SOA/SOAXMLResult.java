package it.finmatica.dmServer.SOA;

import it.finmatica.dmServer.util.keyval;
import java.util.Vector;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class SOAXMLResult
{
  	   
	   /** COSTANTI PER LA CORTUZIONE XML */
	   private final static String _XMLRET_NODE_MESSAGE          = "MESSAGE";
	   private final static String _XMLRET_NODE_RESULT           = "RESULT";
	   private final static String _XMLRET_NODE_TYPE             = "TYPE";
	   private final static String _XMLRET_NODE_CODE             = "CODE";	
	   private final static String _XMLRET_NODE_TEXT             = "TEXT";
	   
	   public SOAXMLResult() {
	   }
	   	   
	   public String getXML()
	   {	   
	          Element root=DocumentHelper.createElement(_XMLRET_NODE_MESSAGE);
	          Element result = DocumentHelper.createElement(_XMLRET_NODE_RESULT);
	          result.addText("OK");
	          Element type = DocumentHelper.createElement(_XMLRET_NODE_TYPE);
	          type.addText("EDIT");
	          Element code = DocumentHelper.createElement(_XMLRET_NODE_CODE);
              code.addText("0");
	          Element text = DocumentHelper.createElement(_XMLRET_NODE_TEXT);
	          text.addText("Operazione eseguita.");
	          
	          root.add(result);
	          root.add(type);
	          root.add(code);
	          root.add(text);	          
		      		      
		      return root.asXML();
	   }			
}

package it.finmatica.dmServer.SOA;

public class SOAXMLErrorRet {
	   private final static String _XMLRET_NODE_RESULT_ERROR       = "error";
	   private final static String _XMLRET_NODE_RESULT_TYPERROR    = "ERROR_PARAMETER";
	   
	   private String message;
	   
	   public SOAXMLErrorRet(String textMessage) {
		      message=textMessage;
	   }
	   
	   public String getXML() {
		      StringBuffer xmlReturn = new StringBuffer("");
		       
		      xmlReturn.append("<message>\n");
		       
		      xmlReturn.append("\t<result>");		       
		      xmlReturn.append(_XMLRET_NODE_RESULT_ERROR);
		      xmlReturn.append("</result>\n");
		       
		      xmlReturn.append("\t<type>");		       
		      xmlReturn.append(_XMLRET_NODE_RESULT_TYPERROR);
		      xmlReturn.append("</type>\n");
		       
		      xmlReturn.append("\t<code/>\n");
		       
		      xmlReturn.append("\t<text>");		       
		      xmlReturn.append(message);
		      xmlReturn.append("</text>\n");
		       
		      xmlReturn.append("</message>\n");
		       
		      return xmlReturn.toString();
	   }	   
}

package it.finmatica.dmServer.SOA;

import it.finmatica.dmServer.dbEngine.struct.dbTable.Area;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.keyval;
import java.util.Vector;

public class SOAXMLDataRet
{
	   /** COSTANTI PER LA CORTUZIONE XML */
	   private final static String _XMLRET_ATTRNODE_REQUESTID      = "requestId";
	   private final static String _XMLRET_ATTRNODE_PAGES          = "pages";
	   private final static String _XMLRET_ATTRNODE_PAGE           = "page";
	   private final static String _XMLRET_ATTRNODE_LASTROW        = "lastRow";
	   private final static String _XMLRET_ATTRNODE_ROWS           = "rows";
	   private final static String _XMLRET_ATTRNODE_FIRSTROW       = "firstRow";
	   private final static String _XMLRET_NODE_NUM                = "num";
	   private final static String _XMLRET_NODE_ROWSET             = "ROWSET";
	   private final static String _XMLRET_NODE_ROW                = "ROW";
	   private final static String _XMLRET_NODE_FIELD              = "FIELD";
	   private final static String _XMLRET_NODE_NOME               = "NOME";
	   private final static String _XMLRET_NODE_VALUE              = "VALUE";
	   
	   public final static String  _XML_TYPE_OGGETTIFILE           = "OGFI";
	   public final static String  _XML_TYPE_VERSIONE              = "VER";
	   public final static String  _XML_TYPE_VALORI                = "VAL";
	   public final static String  _XML_TYPE_AREE                  = "AREE";
	   public final static String  _XML_TYPE_MODELLI               = "MODELLI";
	   public final static String  _XML_TYPE_METADATI              = "METADATI";
	   public final static String  _XML_TYPE_ALLEGATI              = "ALLEGATI";
	   
	   private String reqId;
	   
	   private Vector vDataValue;
	   
	   private String dataString=null;
	   
	   /** Se è settato a true indica che il contenuto di ogni elemento del vettore 
	    * è un XML ben formato */ 
	   private boolean isXML;
	
	   public SOAXMLDataRet(String requestId,Vector vDataValue) {
		      reqId=requestId;
		      this.vDataValue=vDataValue;
	   }
	   
	   public SOAXMLDataRet(String requestId,Vector vDataValue,boolean xml) {
		      reqId=requestId;
		      this.vDataValue=vDataValue;
		      isXML= xml;
	   }
	   
	   public SOAXMLDataRet(String requestId,String sDataString) {
		      reqId=requestId;
		      this.dataString=sDataString;
		      isXML= false;
		      vDataValue = new Vector();
	   }	   
	   	   
	   public String getXML() {	   
	          	   
		      StringBuffer xmlReturn = new StringBuffer("");
		      
		      /** TESTATA */ 
		      xmlReturn.append("<");
		      xmlReturn.append(_XMLRET_NODE_ROWSET);
		      xmlReturn.append(" ");
		      xmlReturn.append(_XMLRET_ATTRNODE_REQUESTID);
		      xmlReturn.append("=\"");
		      xmlReturn.append(reqId);
		      xmlReturn.append("\" ");
		      xmlReturn.append(_XMLRET_ATTRNODE_PAGES);
		      xmlReturn.append("=\"1\" ");
		      xmlReturn.append(_XMLRET_ATTRNODE_PAGE);
		      xmlReturn.append("=\"1\" ");
		      xmlReturn.append(_XMLRET_ATTRNODE_LASTROW);
		      xmlReturn.append("=\""+vDataValue.size()+"\" ");
		      xmlReturn.append(_XMLRET_ATTRNODE_ROWS);
		      xmlReturn.append("=\""+vDataValue.size()+"\" ");
		      xmlReturn.append(_XMLRET_ATTRNODE_FIRSTROW);
		      xmlReturn.append("=\"1\" ");
		      xmlReturn.append(">");
		      /** FINE TESTATA */ 		      
		      
		      /** RIGHE */
		      if (dataString!=null) {
		    	  xmlReturn.append("<");
		    	  xmlReturn.append(_XMLRET_NODE_ROW);
		    	  xmlReturn.append(" num=\"");
		    	  xmlReturn.append("1");
		    	  xmlReturn.append("\"");
		    	  xmlReturn.append(">");
	    		  xmlReturn.append(dataString);
	    		  xmlReturn.append("</");
		    	  xmlReturn.append(_XMLRET_NODE_ROW);
		    	  xmlReturn.append(">");
	    	  }
		      else {
			      for(int i=0;i<vDataValue.size();i++) {
			    	   
			    	  xmlReturn.append("<");
			    	  xmlReturn.append(_XMLRET_NODE_ROW);
			    	  xmlReturn.append(" num=\"");
			    	  xmlReturn.append(i+1);
			    	  xmlReturn.append("\"");
			    	  xmlReturn.append(">");
			    	
			    	  if(isXML)
			    	  {
			    	
			    		xmlReturn.append(vDataValue.elementAt(i));
					  }		    	  
			    	  else
			    	  {	
			    	
			    		Vector<keyval> vKey = (Vector)vDataValue.get(i);
			    					    			    		
			    		for(int iKeyConta=0;iKeyConta<vKey.size();iKeyConta++) {
			    			
			    		    keyval kValore = vKey.get(iKeyConta);	
			    		    
			    		    if (nvl(kValore.getTipoDoc(),"").equals(_XML_TYPE_OGGETTIFILE)) {		    		    
			    		    	xmlReturn.append("<IDOGGETTOFILE>");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append("</IDOGGETTOFILE>");	
						    	xmlReturn.append("<FILENAME>");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</FILENAME>");
			    		    }			    		  
			    		    else if (nvl(kValore.getTipoDoc(),"").equals(_XML_TYPE_VALORI)) {
			    		    	xmlReturn.append("<FIELD>");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append("</FIELD>");	
						    	xmlReturn.append("<VALUE>");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</VALUE>");		    		    	
			    		    }
			    		    else if (nvl(kValore.getTipoDoc(),"").equals(_XML_TYPE_AREE)) {
			    		    	xmlReturn.append("<CODICE>");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append("</CODICE>");	
						    	xmlReturn.append("<DESCRIZIONE>");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</DESCRIZIONE>");	
			    		    }
			    		    else if (nvl(kValore.getTipoDoc(),"").equals(_XML_TYPE_MODELLI)) {
			    		    	xmlReturn.append("<AREA>");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append("</AREA>");	
						    	xmlReturn.append("<CODICE_MODELLO>");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</CODICE_MODELLO>");	
			    		    }			 
			    		    else if (nvl(kValore.getTipoDoc(),"").equals(_XML_TYPE_METADATI)) {
			    		    	xmlReturn.append("<AREA>");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append("</AREA>");	
						    	xmlReturn.append("<CODICE_MODELLO>");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</CODICE_MODELLO>");	
						    	xmlReturn.append("<DATO>");
						    	xmlReturn.append(kValore.getTipoDaClient());
						    	xmlReturn.append("</DATO>");	
						    	xmlReturn.append("<TIPO>");
						    	xmlReturn.append(kValore.getOperator());
						    	xmlReturn.append("</TIPO>");	
			    		    }
			    		    else if (nvl(kValore.getTipoDoc(),"").equals(_XML_TYPE_ALLEGATI)) {
			    		    	xmlReturn.append("<IDALLEGATO>");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append("</IDALLEGATO>");	
						    	xmlReturn.append("<NOMEALLEGATO>");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</NOMEALLEGATO>");	
						    	xmlReturn.append("<CONTENUTO>");
						    	xmlReturn.append(kValore.getTipoDaClient());
						    	xmlReturn.append("</CONTENUTO>");							    	
			    		    }
			    		    else {		    		    	
						    	xmlReturn.append("<");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append(" type=\"");
						    	xmlReturn.append(kValore.getTipoDaClient());
						    	xmlReturn.append("\"");
						    	xmlReturn.append(" size=\"");
						    	xmlReturn.append(kValore.getTipoUguaglianza());
						    	xmlReturn.append("\"");
						    	xmlReturn.append(" >");
						    	xmlReturn.append(kValore.getVal());
						    	xmlReturn.append("</");
						    	xmlReturn.append(kValore.getKey());
						    	xmlReturn.append(">");
			    		    }
			    		}		    				    	
				    			    				    	
			    	  }
			    	 
			    	  xmlReturn.append("</");
			    	  xmlReturn.append(_XMLRET_NODE_ROW);
			    	  xmlReturn.append(">");
		      	  }
		    	 
		    	  
		    	
		      }
		      /** FINE RIGHE */ 
		    
		      xmlReturn.append("</");
		      xmlReturn.append(_XMLRET_NODE_ROWSET);
		      xmlReturn.append(">");
		      
		      return xmlReturn.toString();
	   }			

	   protected String nvl(String campo, String valore) {
		      if (campo==null) return valore;
		      
		      return campo;
	   }	   
}

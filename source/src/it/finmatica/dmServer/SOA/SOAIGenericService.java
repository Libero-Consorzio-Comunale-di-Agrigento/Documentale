package it.finmatica.dmServer.SOA;

import java.util.Iterator;
import java.util.Vector;

import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.XMLUtilDom4j;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class SOAIGenericService {
	   protected HttpServletRequest 	httpRequest		= null;
	   protected IDbOperationSQL		dbOperation		= null;
	   protected String				    areaModel		= null;
	   protected String				    cmModel			= null;
	   protected String				    crModel			= null;
	   protected String 				idDocument		= null;
	   protected String				    user			= null;

	   protected final static int _ERR_INTEGRITY_PAR____NOPARAMETER  = -1;
	   protected final static int _ERR_INTEGRITY_PAR____NOCM         = -2;
	   protected final static int _ERR_INTEGRITY_PAR____NOAR         = -3;
	   protected final static int _ERR_INTEGRITY_PAR____NOAREA       = -4;

	   protected final static int _INTEGRITY_PAR____OK_IDDOC         = 1;
	   protected final static int _INTEGRITY_PAR____OK_ARCM          = 2;
	   protected final static int _INTEGRITY_PAR____OK_ARCMCR        = 3;

	   protected final static String _CDATAINIT                      = "<![CDATA[";
	   protected final static String _CDATAEND                       = "]]>";

	   protected final static String _ADD_ACL_TAG					 = "ADD";
	   protected final static String _REM_ACL_TAG					 = "REM";

	   protected int checkIntegrityParameter() {
		         if (nvl(areaModel,"").trim().equals("") && nvl(crModel,"").trim().equals("") && nvl(cmModel,"").trim().equals("") && nvl(idDocument,"").trim().equals(""))
		    	     return _ERR_INTEGRITY_PAR____NOPARAMETER;

		         if (!nvl(idDocument,"").trim().equals(""))
		    	     return _INTEGRITY_PAR____OK_IDDOC;
		         else {
		    	     if (nvl(areaModel,"").trim().equals(""))
		    		    return _ERR_INTEGRITY_PAR____NOAR;

		    	     if (nvl(cmModel,"").trim().equals(""))
		    		    return _ERR_INTEGRITY_PAR____NOCM;

		    	     if (nvl(crModel,"").trim().equals(""))
		    		    return _INTEGRITY_PAR____OK_ARCM;
		    	     else
		    		    return _INTEGRITY_PAR____OK_ARCMCR;
		       }
       }

	   protected String generateErrorParameterMessage(int iCheckPar) {
	             StringBuffer ret = new StringBuffer("");

	             switch (iCheckPar) {
			       	  case _ERR_INTEGRITY_PAR____NOPARAMETER:
			       		   ret.append("Mancano parametri chiave: ar,cm,cr o iddoc.");
			       		   break;
			       	  case _ERR_INTEGRITY_PAR____NOAR:
			       		   ret.append("Manca parametro chiave: ar.");
			       		   break;
			       	  case _ERR_INTEGRITY_PAR____NOCM:
			       		   ret.append("Manca parametro chiave: cm.");
			       		   break;
	             }

	             return ret.toString();
	   }

	   protected Vector<keyval> parseDatiXML(String dati) throws Exception {
		         return parseDatiXML(dati,"nome","valore",null);
	   }

	   protected Vector<keyval> parseDatiXML(String dati,String campo, String valore, String valore2) throws Exception {

			     Vector<keyval> vRet = new Vector();
			     Document dXMLDati = null;
			     XMLUtilDom4j xmlUt = null;
			   //    System.out.println("DATI="+dati);
			     try {
			       dXMLDati = DocumentHelper.parseText(dati);
			     }
				 catch (Exception e) {
				   throw new Exception("parseDatiXML - Errore in parse XML dati ("+dati+")\n"+e.getMessage());
				 }

				 if (dXMLDati == null)
				     throw new Exception("parseDatiXML - Document XML dati ("+dati+") nullo!");


				 xmlUt = new XMLUtilDom4j(dXMLDati);

				 Vector<Element> vEl=xmlUt.leggiChildElementXML(dXMLDati.getRootElement());

			     for(int i=0;i<vEl.size();i++) {
			     	String valoreCampo = xmlUt.leggiValoreXML(vEl.get(i),valore);
                  //   System.out.println("valoreCampo="+valoreCampo);
			    	 keyval k = new keyval(xmlUt.leggiValoreXML(vEl.get(i),campo),
						 valoreCampo);

			    	 if (valore2!=null)
			    		 k.setValueBetween( xmlUt.leggiValoreXML(vEl.get(i),valore2));

			    	 vRet.add(k);
				 }

			     return vRet;
       }

	   protected Vector<ACL> parseACLXML(String aclString) throws Exception {
		   Document dXMLACL = null;
	       XMLUtilDom4j xmlUt = null;
	       Vector<ACL> vACL = new Vector<ACL>();

	       try {
	    	   dXMLACL = DocumentHelper.parseText(aclString);
	       }
		   catch (Exception e) {
		     throw new Exception("parseACLXML - Errore in parse XML ACL ("+aclString+")\n"+e.getMessage());
		   }

		   if (dXMLACL == null)
		       throw new Exception("parseACLXML - Document XML ACL ("+aclString+") nullo!");


		   xmlUt = new XMLUtilDom4j(dXMLACL);

		   Vector<Element> vEl;
		   vEl=xmlUt.leggiChildElementXML(dXMLACL.getRootElement());

		   for(int i=0;i<vEl.size();i++) {
			   String acl="", type="", versus="";
			   Element eACL = vEl.get(i);

			   int iCiclo=0;
			   for (Iterator iterator = eACL.elementIterator();iterator != null && iterator.hasNext();iCiclo++) {
				   Element elemento = (Element)iterator.next();

				   switch (iCiclo) {
				   	case 0: acl=elemento.getText(); break;
				   	case 1: type=elemento.getText(); break;
				   	case 2: versus=elemento.getText(); break;
				   }
			   }

			   ACL aclVar = new ACL(acl, type, versus);

			   vACL.add(aclVar);
		   }

		   return vACL;

	   }

	   protected String removeCDATATAG(String inputXML) {
		   	   //Tolgo il CDATA dai datiXML
		   if (inputXML.indexOf(_CDATAINIT)==0) {
			   inputXML=inputXML.substring(9,inputXML.length());
			   inputXML=inputXML.substring(0,inputXML.length() - 3);
		   }
			   			   
		       /*if (inputXML.indexOf(_CDATAINIT)!=-1) 
		          inputXML=inputXML.substring(inputXML.indexOf(_CDATAINIT)+_CDATAINIT.length(),inputXML.indexOf(_CDATAEND));*/

		    return inputXML;
	   }

	   protected String nvl(String campo, String valore) {
		      if (campo==null) return valore;

		      return campo;
	   }

	   protected void closeDbOp() {
	       		 try {this.dbOperation.close();}catch(Exception e){}
       }

	   protected void closeDbOpAndRollback() {
		     try {this.dbOperation.rollback();}catch(Exception e){}
     		 try {this.dbOperation.close();}catch(Exception e){}
	   }

	   class ACL {
			  public String user, type, versus;

			  public ACL(String p_user,String p_type,String p_versus) {
				  	 user=p_user;
				  	 type=p_type;
				  	 versus=p_versus;
			  }
		}
}

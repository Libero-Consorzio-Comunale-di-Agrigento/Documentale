package it.finmatica.dmServer.jdms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

public class eliminaDocumenti {
	
	protected Vector<String> 	iddocs = null;
	protected String 			returl = "";
	protected Connection 		cn = null;
	protected IDbOperationSQL 	dbOp = null;
	private DMServer4j log;
	 
	public eliminaDocumenti()
	{
		log= new DMServer4j(eliminaDocumenti.class); 
	}
	
	public String daEliminare(String sXml)throws Exception {
			
		    String errmsg = "";
		    Document dOutput = null;
			Element  root = null;
		  	
		  	try {
		  		leggiXML(sXml);
		  		dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
		  		String sql="";
		  		
		  		for(int i=0;i<iddocs.size();i++)
           	    {
		  			String idDoc = iddocs.get(i);
		  			sql = "f_elimina_documento("+idDoc+")";		
		  			log.log_info("Eliminazione del documento con ID:"+idDoc+" SQL: "+sql);
		  			dbOp.setCallFunc(sql);
		 	    	dbOp.execute();
		 	    	long ret = dbOp.getCallSql().getLong(1);
		 	    	
		 	    	if(ret==1){
		 	    		log.log_info("Il documento con ID:"+idDoc+" è stato eliminato");
		 	    	}		  			
           	    }	 	    		
	 	    	dbOp.commit();
	 	    	dbOp.close();		  			  	
		  	} catch (Exception e) {
		    	try {
		    		dbOp.rollback();
		    	} catch (Exception edbop) {}
		    	try {
		    		dbOp.close();
		    	} catch (Exception edbop) {}
		    	e.printStackTrace();
		    	errmsg = "Errore durante la fase di eliminazione. \n"+ e.getMessage();
		    }
		  	  	
		    
		    try {
		    	root = DocumentHelper.createElement("FUNCTION_OUTPUT");
		    	dOutput = DocumentHelper.createDocument();
			    dOutput.setRootElement(root);
			    if (errmsg.length() == 0) {
			      	root = aggFiglio(root, "RESULT", "OK");
			    } else {
			    root = aggFiglio(root, "RESULT", "NONOK");
			    }
			    root = aggFiglio(root, "ERROR", errmsg);
			    root = aggFiglio(root, "REDIRECT", returl);
			    root = aggFiglio(root, "REFRESH", "N");
			    root = aggFiglio(root, "FORCE_REDIRECT", "Y");
			    root = aggFiglio(root, "LISTAID", "");
		    }  catch (Exception e) {
		    	e.printStackTrace();
		    	throw new Exception ("Errore durante la fase di eliminazione. \n"+ e.getMessage());
		    }
		    return dOutput.asXML();
	}
			 
	protected void leggiXML(String sXml) throws Exception {
		Document dInput = null;
		Element  eListaId = null, root = null, elemento = null;
		String sTipo = "";
		String iddoc = "";
		String host_str = "";
		String usrdb = "";
		String pwddb = "";
		String idQuery="-1";

	  	this.iddocs = new Vector<String>();
	    dInput = DocumentHelper.parseText(sXml);
	    root = dInput.getRootElement();
	    eListaId = leggiElemento(root, "LISTAID");
	    for(Iterator iterator = eListaId.elementIterator(); iterator != null && iterator.hasNext();) {
	    	elemento = (Element)iterator.next();
	    	sTipo = leggiValore(elemento, "TIPOOGGETTO");
	    	if (sTipo.equalsIgnoreCase("D")) {
	    		iddoc = leggiValore(elemento, "IDOGGETTO");
	    		this.iddocs.add(iddoc);
	    	}
	    }
	    
	    usrdb = leggiValore(dInput, "USER");
	    pwddb = leggiValore(dInput, "PASSWORD");
	    host_str = leggiValore(dInput, "HOST_STRING");
	    idQuery = leggiValore(dInput, "IDQUERYPROVENINEZ");
	    if(idQuery!=null)
	    	returl = "./ClosePageAndRefresh.do?idQueryProveninez="+idQuery;
	    else
	    	returl = "./ClosePageAndRefresh.do?idQueryProveninez=-1";
	  	Class.forName("oracle.jdbc.driver.OracleDriver");
	  	cn=DriverManager.getConnection(host_str,usrdb,pwddb);
	  	cn.setAutoCommit(false);
	 }
	 
	 protected Element aggFiglio(Element elp, String nome, String valore) {
		  	if (valore == null) {
		  		valore = "";
		  	}
		    Element elf = DocumentHelper.createElement(nome);
		    elf.setText(valore);
		    elp.add(elf);
		    return elp;
	 }
	 
	 protected static Element leggiElemento(Element e, String tagName) {
	      Element elemento = null, eFound = null;
	      for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
	      {
	          elemento = (Element)iterator.next();
	          if(elemento != null && elemento.getName().equals(tagName)) {
	             eFound = elemento;
	          } else {
	              eFound = leggiElemento(elemento, tagName);
	              if ( eFound != null) {
	                return eFound;
	              }
	          }
	      }

	      return eFound;
	 }
	 
	 protected static String leggiValore(Document xmlDocument, String tagName)
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
	              valore = leggiValore(elemento, tagName);
	      }

	      return valore;
	  }

	  protected static String leggiValore(Element e, String tagName)
	  {
	      String valore = null;
	      for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
	      {
	          Element elemento = (Element)iterator.next();
	          if(elemento != null && elemento.getName().equals(tagName))
	              valore = elemento.getText();
	          else
	              valore = leggiValore(elemento, tagName);
	      }

	      return valore;
	  }

}

package it.finmatica.dmServer.sysIntegration.util;

import java.util.Vector;

import org.dom4j.Element;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.SysIntegrationDbOperation;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.XMLUtilDom4j;

public class SysIntegrationXMLFieldDecode {
	   Environment varEnv;
	   SysIntegrationDbOperation sysIntDbOp;
	
	   public SysIntegrationXMLFieldDecode() {		     
	   }	   
	   
	   public SysIntegrationXMLFieldDecode(Environment en) {
		   	  varEnv=en;
		      if (varEnv!=null)
		    	  sysIntDbOp = new SysIntegrationDbOperation(varEnv,varEnv.getDbOp(),null);
	   }
	   	   
	   /**
	    * Metodo che partendo dall'XML dato in input (OtherKeyDocumentRule)
	    * presente sulla SYSINTEGRATION_TYPE e l'idDocumento relativo restituisce
	    * una lista di XML ognuno dei quali conterrà le singole 
	    * istanze degli oggetti estratti. Esempio:
	    * 
	    * OtherKeyDocumentRule = <root><object table="OGGETTI_FILE">ID_OGGETTO_FILE</object></root>
	    * idDocument = 357
	    * 
	    * L'unico item presente nell'xml passato in input è fatto dalla tabella 
	    * oggetti_file (campo chiave ID_OGGETTO_FILE).
	    * Verrà quindi fatta una select ID_OGGETTO_FILE dalla tabella in questione con idDocumento=357
	    * Per oogni ID_OGGETTO_FILE restituito verrà costruito un XML da inserire nel vettore restituito.
	    * 
	    * Ad es. verrà restituito il seguente vettore:
	    * 
	    * [
	    *  <root><object table="OGGETTI_FILE">765</object></root>,
	    *  <root><object table="OGGETTI_FILE">766</object></root>,
	    *  ......................................................
	    * ]
	    * 
	    */
	   public Vector<String> getXMLOptionOtherKey_From_XMLOtherKeyDocumentRule(String xmlOtherKeyDocumentRule, String idDocument) throws Exception {
		   	  Vector<String> vXMLRet = new Vector<String>();
		   	  
		   	  if (xmlOtherKeyDocumentRule==null) return vXMLRet;
		   	  
		   	  if (!Global.nvl(xmlOtherKeyDocumentRule.trim(),"").equals("")) {
		   		  XMLUtilDom4j xmlDom4j = new XMLUtilDom4j(xmlOtherKeyDocumentRule);
		   		  
		   		  Vector<Element> vEl = xmlDom4j.leggiChildElementXML(xmlDom4j.getRoot());	
		   		  for(int i=0;i<vEl.size();i++) {
		   			  String table, field;
		   			  
		   			  table=vEl.get(i).attribute("table").getText();
		   			  field=vEl.get(i).getText();
		   			  
		   			  Vector<String> valueField; 
		   			  
		   			  try {
		   			    valueField=sysIntDbOp.getValueFieldFromGenericIdDocSql(table,field,idDocument);
		   			  }
		   			  catch (Exception e) {
		   				throw new Exception("\nErrore in esecuzione dell'SQL per tabella="+table+" campo="+field+
		   								    " per il recupero delle informazioni sull'item\n"+e.getMessage());
		   			  }
		   			  		  
		   			  for(int j=0;j<valueField.size();j++)
		   			      vXMLRet.add("<object table=\""+table+"\">"+valueField.get(j)+"</object>");		   			  
		   		  }
		   			 		   		
		   	  }
		   	  
		   	  return vXMLRet;
	   }
	   
	   public SysIntegrationSyncroDescrStruct getSyncroDescrStruct_From_XMLDescrStruct(String XMLDescrStruct) throws Exception {
		   	  SysIntegrationSyncroDescrStruct ret;
		   	  
		   	  XMLUtilDom4j xmlDom4j = new XMLUtilDom4j(XMLDescrStruct);
		   	  
		   	  Vector<Element> vEl = xmlDom4j.leggiChildElementXML(xmlDom4j.getRoot());
		   	  
		   	  if (vEl.size()!=2) throw new Exception("SysIntegrationXMLFieldDecode::getSyncroDescrStruct_From_XMLDescrStruct - XML mal formato al primo livello (sotto root)!!");
		   	  
		      String typeSyncroASyncro, typeObject, nameObject;

		      typeSyncroASyncro=vEl.get(0).getText();
		      
		      if (!typeSyncroASyncro.equals("S") && !typeSyncroASyncro.equals("A"))
		    	  throw new Exception("SysIntegrationXMLFieldDecode::getSyncroDescrStruct_From_XMLDescrStruct - il tag <type> sotto <root> può contenere solo A o S!!");
		      
		      Vector<Element> vElSottoObject = xmlDom4j.leggiChildElementXML(vEl.get(1));
		      
		      if (vElSottoObject.size()!=2) throw new Exception("SysIntegrationXMLFieldDecode::getSyncroDescrStruct_From_XMLDescrStruct - XML mal formato al primo livello (sotto object)!!");
		      		      		      
		      typeObject=vElSottoObject.get(0).getText();
		      nameObject=vElSottoObject.get(1).getText();
		      
		      ret = new SysIntegrationSyncroDescrStruct(typeSyncroASyncro, typeObject, nameObject);
		   	  
		   	  return ret;
	   }
}
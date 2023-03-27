package it.finmatica.dmServer.sysIntegration;

import java.sql.ResultSet;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.SysIntegrationDbOperation;
import it.finmatica.dmServer.sysIntegration.util.SysIntegrationSyncroDescrStruct;
import it.finmatica.dmServer.sysIntegration.util.SysIntegrationXMLFieldDecode;

public class SysIntegrationModel {
	   private String area, codiceModello, type;
	   private SysIntegrationType sysIntType; 
	 
	   private String xmlSincroDescr, xmlDecodeModel;
	   private String logItem;
	   
	   private Environment varEnv;
	   private SysIntegrationDbOperation sysIntModDbOp;
	   private Vector<SysIntegrationField> sysIntFieldVector = new Vector<SysIntegrationField>();
	   private SysIntegrationSyncroDescrStruct sysIntDescrStruct = null;
	   
	   public SysIntegrationModel(String ar,String cm,
			   					  String typeInt,
			   					  Environment en) throws Exception {
		   	  area=ar;
		   	  codiceModello=cm;
		   	  type=typeInt;
		   	  varEnv=en;
		   	  
		   	  sysIntModDbOp = new SysIntegrationDbOperation(varEnv,varEnv.getDbOp(),null);
		   	  
		   	  initStruct();
		   	  initField();		   	
	   }
	   
	   private void initStruct() throws Exception {
		   	   try {
		   	     ResultSet rst = sysIntModDbOp.retrieveModelAndType(area,codiceModello,type);
		   	     
		   	     if (rst.next()) {
		   	    	sysIntType  = new SysIntegrationType(type);
		   	    	sysIntType.setClassImplementation(rst.getString("CLASSIMPLEMENTATION"));
		   	    	sysIntType.setUrlService(rst.getString("URLSERVICE"));
		   	    	sysIntType.setXmlDocumentRule(rst.getString("otherdocrule"));
		   	    	xmlSincroDescr=rst.getString("syncrodescr");
		   	    	
		   	    	try {
		   	    	  sysIntDescrStruct=(new SysIntegrationXMLFieldDecode()).getSyncroDescrStruct_From_XMLDescrStruct(xmlSincroDescr);
		   	    	}
		   	    	catch (Exception e) {
		   	    	  throw new Exception("Errore in decodifica campo \n<xmlSincroDescr>="+xmlSincroDescr+"\n in integrazione per area="+area+",modello="+codiceModello+",typeIntegration="+type+"\n"+e.getMessage());
		   	    	}
		   	    	
		   	    	xmlDecodeModel=rst.getString("decoderemotemodel");
		   	    	logItem=rst.getString("LOGITEM");
		   	     }
		   	     else
		   	    	throw new Exception("Non esiste un integrazione per area="+area+",modello="+codiceModello+",typeIntegration="+type);
		   	   }
		   	   catch (Exception e) {
		   		 throw new Exception("SysIntegrationModel::initStruct - "+e.getMessage());		   		 		   		 
		   	   }		   	   		   	   
	   }
	   
	   private void initField() throws Exception {
			   try {
			   	 ResultSet rst = sysIntModDbOp.retrieveField(area,codiceModello,type);
			   	 
			   	 while (rst.next()) {
			   		SysIntegrationField sysIntField = new SysIntegrationField(area,codiceModello,type,rst.getString("FIELD"));
			   		
			   		sysIntField.setField_remotename(rst.getString("FIELD_REMOTENAME"));			   		
			   		sysIntField.setKeyfield(rst.getInt("KEYFIELD"));
			   		sysIntField.setOrder(rst.getInt("ORDERFIELD"));
			   		sysIntField.setExtraInstruction(rst.getString("optextraisntruct"));
			   					   		
			   		sysIntFieldVector.add(sysIntField);
			   	 }
			   }
		   	   catch (Exception e) {
		   		 throw new Exception("SysIntegrationModel::initField - "+e.getMessage());		   		 		   		 
		   	   }
	   }
	   
	   public String toString() {
		   	  StringBuffer sToStr = new StringBuffer("");
		   	  
		   	  sToStr.append("___________TESTATA MODELLO________\n");
		   	  sToStr.append("Area = "+area+"\n");
		   	  sToStr.append("Codice Modello = "+codiceModello+"\n");
		      sToStr.append("Type = "+type+"\n");
		      sToStr.append("xmlSincroDescr = "+xmlSincroDescr+"\n");
		      sToStr.append("\tType Syncro = "+sysIntDescrStruct.getTypeSyncroASyncro()+"\n");
		      sToStr.append("\tType Object = "+sysIntDescrStruct.getTypeObject()+"\n");
		      sToStr.append("\tName Object = "+sysIntDescrStruct.getNameObject()+"\n");
		      sToStr.append("xmlDecodeModel = "+xmlDecodeModel+"\n");
		      sToStr.append("logItem = "+logItem+"\n");
		      sToStr.append("___________TIPO_INTEGRAZIONE________\n");
		      sToStr.append("Type = "+type+"\n");
		      sToStr.append("ClassImplementation = "+sysIntType.getClassImplementation()+"\n");
		      sToStr.append("UrlService = "+sysIntType.getUrlService()+"\n");
		      sToStr.append("XmlDocumentRule = "+sysIntType.getXmlDocumentRule()+"\n");
		      sToStr.append("___________CAMPI________\n");
		      for(int i=0;i<sysIntFieldVector.size();i++)  {
		    	  sToStr.append("\n\n");
		    	  sToStr.append(sysIntFieldVector.get(i).toString());
		      }
		   	  
		   	  return sToStr.toString();
	   }

	   public String getLogItem() {
		      return logItem;
	   }

	   public Vector<SysIntegrationField> getSysIntFieldVector() {
		      return sysIntFieldVector;
	   }

	   public SysIntegrationType getSysIntType() {
		      return sysIntType;
	   }

	   public String getXmlDecodeModel() {
		   	  return xmlDecodeModel;
	   }

	   public String getXmlSincroDescr() {
		   	  return xmlSincroDescr;
	   }

	   public SysIntegrationSyncroDescrStruct getSysIntDescrStruct() {
		      return sysIntDescrStruct;
	   }

	   public String getArea() {
	 	      return area;
	   }

	   public String getCodiceModello() {
		      return codiceModello;
	   }

	   public Environment getVarEnv() {
		      return varEnv;
	   }
}
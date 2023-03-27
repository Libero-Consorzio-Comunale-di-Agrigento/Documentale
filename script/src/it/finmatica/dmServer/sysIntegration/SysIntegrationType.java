package it.finmatica.dmServer.sysIntegration;

public class SysIntegrationType {
	   private String type , urlService;
	   private String description, xmlDocumentRule;
	   private String classImplementation;	   	   	   
		
	   public SysIntegrationType(String t) {
		   	  type=t;		   	  
	   }

	   public String getClassImplementation() {
			  return classImplementation;
	   }

	   public void setClassImplementation(String classImplementation) {
		      this.classImplementation = classImplementation;
	   }

	   public String getDescription() {
		      return description;
	   }

	   public void setDescription(String description) {
		      this.description = description;
	   }

	   public String getUrlService() {
		      return urlService;
	   }

	   public void setUrlService(String urlService) {
		      this.urlService = urlService;
	   }

	   public String getXmlDocumentRule() {
		      return xmlDocumentRule;
	   }

	   public void setXmlDocumentRule(String xmlDocumentRule) {
		      this.xmlDocumentRule = xmlDocumentRule;
	   }

	   public String getType() {
		      return type;
	   }
	   	   
}

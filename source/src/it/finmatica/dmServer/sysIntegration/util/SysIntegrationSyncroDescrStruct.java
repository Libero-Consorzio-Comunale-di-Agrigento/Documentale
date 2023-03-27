package it.finmatica.dmServer.sysIntegration.util;

public class SysIntegrationSyncroDescrStruct {
	   private String typeSyncroASyncro;
	   private String typeObject, nameObject;
	   
	   public SysIntegrationSyncroDescrStruct(String typeSync, String typeObj, String name) {
		   	  typeSyncroASyncro=typeSync;
		      typeObject=typeObj;
		      nameObject=name;
	   }

	   public String getNameObject() {
		      return nameObject;
	   }

	   public String getTypeObject() {
		      return typeObject;
	   }

	   public String getTypeSyncroASyncro() {
		      return typeSyncroASyncro;
	   }
}

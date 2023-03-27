package it.finmatica.dmServer.mapping;
 
import it.finmatica.dmServer.Environment;

/**
 * Classe che gestisce il mapping
 * 
 * @author  G. Mannella
 * @version 2.8
 *
*/
public class GDMapping {

	   Environment vEnv;
	   XMLMapping  xmlm;
	
	   public GDMapping(Environment env) throws Exception {
		      vEnv=env;
		      
		      //Non faccio mapping
		      if (vEnv.Global.USE_MAPPING==null) return;
		      
		      if (!vEnv.Global.USE_MAPPING.equals("S")) return;
		      
		      //Il mapping è fatto su FILE....mi costruisco la struttra hash 
		      //dentro la classe XMLMapping
		      if (!vEnv.Global.MAPPING_PATH.equals("@")) {
		    	  try {
		    		  xmlm = new XMLMapping(vEnv.Global.MAPPING_PATH,vEnv.getEnte(),vEnv.getApplicativo());
		    	  }
		    	  catch(Exception e) {
		    		  throw new Exception("GDMapping::Costructor - "+e.getMessage());
		    	  }
		      }
		      		    	  
       }
    
	   public String getMappingTipoDoc(String tipoDocumento) throws Exception {
              String mapNomeTipoDoc = tipoDocumento;              
              
		      
	    	  //Non faccio mapping su File
	    	  /*if (xmlm==null) {
	    		  //Cerco di farlo sulle tabelle perché sono su finmatica
	    		  if (vEnv.Global.DM.equals(vEnv.Global.FINMATICA_DM)) {
	    			  mapNomeTipoDoc = (new TableMapping(vEnv,vEnv.getEnte(),vEnv.getApplicativo())).getMappingTipoDoc(tipoDocumento);
	    		  }
	    	  }*/
	    	  //Faccio mapping su file
	    	 /* else {
	    		  if (vEnv.Global.USE_MAPPING.equals("S")) {
	    			  mapNomeTipoDoc= xmlm.getMappingTipoDoc(tipoDocumento);
	    		  }
	    	  }*/
		      
              
              return mapNomeTipoDoc;
	   }

	   public String getMappingCampo(String tipoDocumento,boolean bIsTipoDocDM, String campo) throws Exception {
		   	  String nomedm = campo;
                      
		      
	    	  //Non faccio mapping su File
	    	 /* if (xmlm==null) {
	    		  //Cerco di farlo sulle tabelle perché sono su finmatica
	    		  if (vEnv.Global.DM.equals(vEnv.Global.FINMATICA_DM)) {
	    			  nomedm = (new TableMapping(vEnv,vEnv.getEnte(),vEnv.getApplicativo())).getMappingCampo(tipoDocumento,bIsTipoDocDM,campo);
	    		  }
	    	  }*/
	    	  //Faccio mapping su file
	    	/*  else {
	    		  if (vEnv.Global.USE_MAPPING.equals("S")) {
	    			  nomedm= xmlm.getMappingCampo(tipoDocumento,bIsTipoDocDM, campo);
	    		  }
	    	  }*/
		      		      
		   	  return nomedm;
	   }
  
}

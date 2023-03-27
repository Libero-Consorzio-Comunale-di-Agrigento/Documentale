package it.finmatica.dmServer.motoreRicerca;

import it.finmatica.dmServer.util.keyval;
import java.util.Vector;

/**
 * Classe che contiene e gestisce il
 * resultSet dei campi restituiti dalla 
 * ricerca 
*/
public class ResultSetIQuery {
	  private Vector vCursore;
	  private int lPointer;
	  private boolean bResulsetIQuery_NullValueNullVariable=false;
	  
	  public ResultSetIQuery(Vector vCur) {
		     lPointer=-1;
		     vCursore=vCur;     
	  }
	  
	  public ResultSetIQuery(Vector vCur, boolean bNull) {
		     lPointer=-1;
		     vCursore=vCur;     
		     bResulsetIQuery_NullValueNullVariable=bNull;
	  }	  
	  
	  public Vector getFieldsList() {
		     return getFieldsList(null,null);
	  }
	  
	  public Vector getFieldsList(String area, String cm) {
		     Vector vAppoggio = new Vector();
		     for(int i=0;i<vCursore.size();i++) {
		    	 keyval kAppoggio;
		    	 		    	 
		    	 kAppoggio = (keyval)vCursore.get(i);
		    	
		    	 if (area!=null && (area.equals(kAppoggio.getArea()) && cm.equals(kAppoggio.getCm()) )) {
		    	    vAppoggio.add(kAppoggio.getKey());
		    	 }
		    	 else
		    		vAppoggio.add(kAppoggio.getKey());
		     }
		     
		     return vAppoggio;
	  }
	  
	  public boolean next() {
		  	 try {
		  	   //Cerco di capire se si può accedere al prox valore
		  	   //del cursore dal campo con entry "0" (che esiste sicuro)
		  		
		  	   ((keyval)vCursore.get(0)).valoriCursore.get(++lPointer);
		  	 }
		  	 catch (Exception e) {
		  	   return false;	 
		  	 }
		  	 
		  	 return true;
	  }
	  
	  public String get(String campo, String area, String cm) {
		     return get(campo,area,cm,null);
	  }

	  public String get(String campo, String categoria) {
		     return get(campo,null,null,categoria);
	  }	  
	  
	  public String getId() {
		     return get("ID",null,null,null);
	  }
	  
	  public String getCr() {
		     return get("_CR_",null,null,null);
	  }
	  
	  public String getCm(String campo, String categoria) {
		     return get("CM_"+campo,null,null,categoria);
	  }	  
	   
	  private String get(String campo,String area,String cm,String categoria) {		
		     for(int i=0;i<vCursore.size();i++) {
                 keyval kAppoggio = (keyval)vCursore.get(i);
                 
                 if (kAppoggio.getKey().equals("_CR_") && campo.equals("_CR_")) {
                	 if (kAppoggio.valoriCursore.get(lPointer)==null && bResulsetIQuery_NullValueNullVariable) 
                		 return null;
                	 else
                		 return ""+kAppoggio.valoriCursore.get(lPointer);                                 
                 }
                 
                 if (kAppoggio.getKey().equals("ID")  && campo.equals("ID")){
                	 if (kAppoggio.valoriCursore.get(lPointer)==null && bResulsetIQuery_NullValueNullVariable) 
                		 return null;
                	 else
                		 return ""+kAppoggio.valoriCursore.get(lPointer);                                 
                 }                
                 
                 if (kAppoggio.getKey().equals(campo)) {
                	 if (area!=null) {
                		 if (kAppoggio.getArea().equals(area) && kAppoggio.getCm().equals(cm) ){
		                	 if (kAppoggio.valoriCursore.get(lPointer)==null && bResulsetIQuery_NullValueNullVariable) 
		                		 return null;
		                	 else
		                		 return ""+kAppoggio.valoriCursore.get(lPointer);                                 
		                 }
                			
                	 }
                	 else {
                		 if (kAppoggio.getCategoria().equals(categoria)){
		                	 if (kAppoggio.valoriCursore.get(lPointer)==null && bResulsetIQuery_NullValueNullVariable) 
		                		 return null;
		                	 else
		                		 return ""+kAppoggio.valoriCursore.get(lPointer);                                 
		                 }
                			
                	 }
                 }
		     }
		     
		     return null;
	  }
	  
	  public String getTypePrecision(String campo,String area,String cm,String categoria) {		
		     for(int i=0;i<vCursore.size();i++) {
              keyval kAppoggio = (keyval)vCursore.get(i);                                       
              
              if (kAppoggio.getKey().equals("_CR_") && campo.equals("_CR_")) {
            	  return ""+kAppoggio.valoriTypeCursore.get(lPointer)+"@"+kAppoggio.valoriSizeCursore.get(lPointer);                            
              }
              
              if (kAppoggio.getKey().equals("ID")  && campo.equals("ID")){
            	  return ""+kAppoggio.valoriTypeCursore.get(lPointer)+"@"+kAppoggio.valoriSizeCursore.get(lPointer);                          
              }                 
              
              if (kAppoggio.getKey().equals(campo)) {
             	 if (area!=null) {
             		 if (kAppoggio.getArea().equals(area) && kAppoggio.getCm().equals(cm) ){		                	 
		                		 return ""+kAppoggio.valoriTypeCursore.get(lPointer)+"@"+kAppoggio.valoriSizeCursore.get(lPointer);                                 
		                 }             			
             	 }
             	 else {
             		 if (kAppoggio.getCategoria().equals(categoria)){
             			return ""+kAppoggio.valoriTypeCursore.get(lPointer)+"@"+kAppoggio.valoriSizeCursore.get(lPointer);                                 
		             }             			
             	 }
              }
		     }
		     
		     return "@";
	  }	  
}
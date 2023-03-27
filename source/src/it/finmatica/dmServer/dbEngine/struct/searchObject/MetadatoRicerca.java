package it.finmatica.dmServer.dbEngine.struct.searchObject;

public class MetadatoRicerca {
	   private String metadato, valore;
	   private int operatore;
	   public static final String OP_UGUALE 		 = "=";
	   public static final String OP_LIKE   		 = "LIKE";
	   public static final String OP_MAGGIOREUGUALE  = ">=";
	   public static final String OP_MINOREUGUALE    = "<=";
	   public static final String OP_MAGGIORE    	 = ">";
	   public static final String OP_MINORE	         = "<";
	   public static final String OP_ISNULL          = "is null";
	   public static final String OP_ISNOTNULL       = "is not null";
	   
	   
	   private Object extendMetadatoRicerca;	   

	   public MetadatoRicerca() {
			  
	   }	   
	   
	   public MetadatoRicerca(String m,String v,int op) {
		   	  metadato=m;
		   	  valore=v;		   	 
		   	  operatore=op;		   	  		   	  
	   }	   
	    
	   public String getMetadato() {
		return metadato;
	   }
	
	   public void setMetadato(String metadato) {
			  this.metadato = metadato;
	   }
	
	   public String getValore() {
		  	  return valore;
	   }
	
	   public void setValore(String valore) {
		 	  this.valore = valore;
	   }
		
	   public int getOperatore() {
			  return operatore;
	   }
	
	   public void setOperatore(int operatore) {
			  this.operatore = operatore;
	   }
	
	   public Object getExtendMetadatoRicerca() {
			  return extendMetadatoRicerca;
	   }

	   public void setExtendMetadatoRicerca(Object extendMetadatoRicerca) {
			  this.extendMetadatoRicerca = extendMetadatoRicerca;
	   }
}

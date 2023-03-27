package it.finmatica.dmServer.util;

public class JNDIParameter {
	   private String jndiString = null;
	   
	   public JNDIParameter(String jndi) {		   
		      jndiString = jndi;
	   }

	   public String getJndiString() {
			  return jndiString;
	   }
	
	   public void setJndiString(String jndiString) {
			  this.jndiString = jndiString;
	   }	   	   
}

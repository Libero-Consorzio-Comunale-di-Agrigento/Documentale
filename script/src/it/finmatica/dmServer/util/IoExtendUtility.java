package it.finmatica.dmServer.util;

import java.io.File;

public class IoExtendUtility {	 
	   public IoExtendUtility() {}
	   public boolean DelDir2(File dir) {
		      if (dir.isDirectory()) {
		    	  String[] contenuto = dir.list();
		    	  for (int i=0; i<contenuto.length; i++) {
		    		   boolean success = DelDir2(new File(dir, contenuto[i]));
		    		   if (!success) { return false; }
		    	  }
		      }
		      return dir.delete();
	  }
}

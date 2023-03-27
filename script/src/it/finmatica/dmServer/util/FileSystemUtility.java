package it.finmatica.dmServer.util;

import java.io.File;

public class FileSystemUtility {
	
	/*
	 * Dato un path completo 
	 * Esempio: root/A/B/C.pdf ed un percorso dove fermarsi es. root
	 * Elimina C.pdf e poi B ed A solo se sono vuote, fermandosi a root 
	 * */
	public synchronized static void deleteAllPathifisEmpty(String pathToDelete,String endPath) throws Exception {
		if (pathToDelete.equals(endPath)) return;
		
		File f = new File(pathToDelete);
		
		if (!f.exists()) return;
		if (f.isDirectory() && f.list().length > 0) return;
			
		File parentDir =  f.getParentFile();		
		if (!f.delete()) throw new Exception("FileSystemUtility::deleteAllPathifEmpty Errore in eliminazione "+f.getAbsolutePath());		
	    FileSystemUtility.deleteAllPathifisEmpty(parentDir.getPath(),endPath);				
	}
}

package it.finmatica.dmServer.testColleghi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Filewalker {

    public void walk( String path, ArrayList filelist, boolean bCompletePath ) {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	if (f.getName().indexOf("BACKUP")!=-1) continue;
                walk( f.getAbsolutePath(),filelist,bCompletePath );
                //System.out.println( "Dir:" + f.getAbsoluteFile()  );
            }
            else {
                //System.out.println( "File:" + /*f.getAbsoluteFile()*/ f.getName() );
                filelist.add((bCompletePath)?f.getAbsoluteFile():f.getName());
            }
        }
    }

    public static void main(String[] args) {
        Filewalker fw = new Filewalker();
        ArrayList listaFile = new ArrayList();
        fw.walk("C:\\Users\\gmannella\\Desktop\\Nuova cartella",listaFile,true );
        System.out.println(listaFile);
        
        for(int i=0;i<listaFile.size();i++) {
        	StringBuffer sStm = new StringBuffer("");
        	sStm.append("select ID_OGGETTO_FILE from OGGETTI_FILE");
            sStm.append(" where id_documento = 1");
            sStm.append(" and FILENAME = '" + listaFile.get(i) + "';");
            
            System.out.println(sStm.toString());
        }
    }

}
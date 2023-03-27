package it.finmatica.dmServer;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;

import java.sql.DriverManager;

import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProvaDelKazzo {
    public ProvaDelKazzo() {
    }
    
    public static void main(String[] args) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("c:/temp/merda.zip"));
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@sicilia:1521:si3", "GDM", "GDM");
        String idDocumento = "46192";

        try {
            StringTokenizer identifiers = 
                new StringTokenizer("lippa3.txt@lippa.txt", "@");

            //Ciclo sui file da estrarre
            while (identifiers.hasMoreTokens()) {

                idDocumento = identifiers.nextToken();
                
                System.out.println("identificativo: " + idDocumento);

                //Creazione del profilo
                Profilo p = new Profilo(idDocumento);
                p.initVarEnv("GDM", "GDM",  conn);

                //Accesso al profilo con i suoi allegati
                if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {

                    //Elenco dei file del profilo
                    StringTokenizer files = 
                        new StringTokenizer(p.getlistaFile(), "@");
                    System.out.println("files: " + files);

                    while (files.hasMoreTokens()) {
                        String fileName = files.nextToken();

                        InputStream is = p.getFileStream(fileName);

                        addToZipStream(zos, idDocumento + "_" + fileName, is);

                        System.out.println("lettura file: " + fileName);
                    }
                } else {
                    System.out.println("Attenzione, non posso accedere al documento: " + 
                              idDocumento + "\nErrore: " + p.getError());
                }
            }

            zos.finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (!conn.isClosed())
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }        
    }
    
    protected static void addToZipStream(ZipOutputStream out, String fileName,
      InputStream in) {
      if (in == null) {
        return;
      }

      byte buf[] = new byte[1024];

      try {
        ZipEntry entry = new ZipEntry(fileName);
        out.putNextEntry(entry);

        int len;

        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }

        out.closeEntry();
        in.close();
      } catch (IOException e) {
      }
    }    
}

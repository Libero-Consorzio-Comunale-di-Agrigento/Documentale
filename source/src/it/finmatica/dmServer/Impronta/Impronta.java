package it.finmatica.dmServer.Impronta;

import java.io.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * DOCUMENT ME!
 *
 * @author Giuseppe Mannella da Maurizio Lippa
 * @version 1.0
 */
public class Impronta {
  private String algorithm;

  /**
   * Crea una nuova istanza dell'oggetto Impronta
   *
   * @param algorithm DOCUMENT ME!
   */
  public Impronta(String algorithm) {
         this.algorithm = algorithm;
  }

  /**
   * DOCUMENT ME!
   *
   * @param file DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public byte[] hash(InputStream is) {
         try {
           MessageDigest md = MessageDigest.getInstance(algorithm);
           DigestInputStream in = new DigestInputStream(is, md); 

           byte buf[] = new byte[2048];

           while ((in.read(buf)) != -1);

           in.close();
           is.close();
      
           return md.digest();
         } catch (Exception e) {
            e.printStackTrace();
         }

         return null;
  }

  public static byte[] dbHash(InputStream i/*Blob b*/, String algo) throws SQLException {
         Impronta impronta = new Impronta(algo);
         //return impronta.hash(b.getBinaryStream());
         return impronta.hash(i);
  }
  
  public static boolean compareTo(InputStream iInzi, String algo, byte[] iFine) throws SQLException {
         byte[] toCompare = dbHash(iInzi, algo);
         return Arrays.equals(toCompare, iFine);
  }

  /**
   * DOCUMENT ME!
   *
   * @param args DOCUMENT ME!
   */
  public static void main(String args[]) throws FileNotFoundException, SQLException, IOException {
    // MD5, SHA1

   // ***************** SCRIVE L'IMPRONTA SU DISCO
   // byte[] b1 = dbHash(new FileInputStream(new File("c:\\tmp.txt")), "SHA1");
   // FileOutputStream xOutputStream = new FileOutputStream(new File("c:\\tmp_hash.txt"));
  //  xOutputStream.write(b1);

   // *************** VERIFICA L'IMPRONTA DA DISCO
   //byte[] b2= new byte[20];

   //FileInputStream f = new FileInputStream(new File("c:\\tmp_hash.txt"));
   //f.read(b2);

   //System.out.println(compareTo(new FileInputStream(new File("c:\\tmp.txt")), "SHA1", b2));

    
  }
}
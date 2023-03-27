package it.finmatica.dmServer.util;

/*
 * METODI PER LA GESTIONE IMPRONTA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.SQLException;

import java.util.Arrays;

public class Impronta 
{
  // variabile private
  private String algorithm;

  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Crea una nuova istanza dell'oggetto Impronta
   * 
   * RETURN:      none
  */
  public Impronta(String algorithm) {
         this.algorithm = algorithm;
  }

  /*
   * METHOD:      hash(InputStream)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Funzione hash
   * 
   * RETURN:      byte[]
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

  /*
   * METHOD:      dbHash(InputStream, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: applica la funzione hash al DB 
   *              dato inputstream e algoritmo
   * 
   * RETURN:      byte[]
  */ 
  public static byte[] dbHash(InputStream i/*Blob b*/, String algo) throws SQLException {
         Impronta impronta = new Impronta(algo);
         //return impronta.hash(b.getBinaryStream());
         return impronta.hash(i);
  }
  
  /*
   * METHOD:      compareTo(InputStream, String,  byte[])
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Confronta le due sequenze di byte
   * 
   * RETURN:      boolean
  */ 
  public static boolean compareTo(InputStream iInzi, String algo, byte[] iFine) throws SQLException {
         byte[] toCompare = dbHash(iInzi, algo);
         return Arrays.equals(toCompare, iFine);
  }
 
}
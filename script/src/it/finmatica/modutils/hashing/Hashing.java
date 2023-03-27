package it.finmatica.modutils.hashing;

import it.finmatica.jfc.crypto.TripleDESEncrypter;
import it.finmatica.jfc.utility.Base64;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;

public class Hashing {
	private static final String ALGORTITHM = "SHA-512";
	private MessageDigest md = null;

	public Hashing() throws Exception {
		try {
			md = MessageDigest.getInstance(ALGORTITHM);
		} catch (Exception e) {
			throw new Exception("ImprontaAllegati()\n"+e.getMessage());    
		}
	}

  /**
     * Metodo che che calcola e restituisce l'impronta dell'allegato dell'inputstream
     * specificato
     *  
     * @param is	InputStream su cui calcolare l'hash
     * 
     * @return byte[]
     * @throws Exception
     * 
  */
	public byte[] getHashCode( InputStream is ) throws Exception {
  	try {
      md.reset();
      DigestInputStream in = new DigestInputStream(is, md); 
      
      while ((in.read()) != -1);
  
    } catch (Exception e) {
    	throw new Exception("Hashing::getHashCode\n"+e.getMessage());
    }
    return md.digest();		
  }
	
	public byte[] getHashCode(String str) throws Exception {
  	// convert String into InputStream
  	InputStream is = new ByteArrayInputStream(str.getBytes());
  	return getHashCode(is);
	}
  
  public boolean isValid(InputStream is, byte[] hashcode) throws Exception {
  	byte[] b = getHashCode(is);
  	return  Arrays.equals(hashcode, b);
  }
  
  public boolean isValid(String str, byte[] hashcode) throws Exception {
  	// convert String into InputStream
  	InputStream is = new ByteArrayInputStream(str.getBytes());
  	return isValid(is, hashcode);
  }

  public static void main(String args[]) throws Exception {
  	Hashing h = new Hashing();
  	String str = "This is a String ~ GoGoGo";
  	 
  	// convert String into InputStream
  	InputStream is = new ByteArrayInputStream(str.getBytes());
  	byte[] b = h.getHashCode(is);
  	System.out.println(Base64.f_encode(b));
  	
  	is = new ByteArrayInputStream(str.getBytes());
  	if(h.isValid(is, b)) {
  		System.out.println("Valido");
  	} else {
  		System.out.println("Non Valido");
  	}
    byte[] key = Base64.f_decode("bnC/ubQOdqFuWF6Kdm6BKRE7cGSSW097");
    TripleDESEncrypter enc = new TripleDESEncrypter(key, "ECB", "PKCS5Padding");
  	System.out.println(enc.encrypt("prova"));
  }
}

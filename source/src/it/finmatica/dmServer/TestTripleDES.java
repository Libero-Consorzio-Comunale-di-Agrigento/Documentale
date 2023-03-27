package it.finmatica.dmServer;

import it.finmatica.jfc.crypto.TripleDESEncrypter;
import it.finmatica.jfc.utility.Base64;

/**
 *
 * @author mlippa
 */
public class TestTripleDES {
    
    public static void main(String[] args) throws Exception {
        byte[] key = Base64.f_decode("bnC/ubQOdqFuWF6Kdm6BKRE7cGSSW097");
        TripleDESEncrypter enc = new TripleDESEncrypter(key, "ECB", "PKCS5Padding");
        String result = enc.encrypt("questo è un test che permette di vedere cosa succede con la crittografazione TripleDES");
        System.out.println("result: " + result);
        String original = enc.decrypt(result);
        System.out.println("original: " + original);
        
    }
    
}

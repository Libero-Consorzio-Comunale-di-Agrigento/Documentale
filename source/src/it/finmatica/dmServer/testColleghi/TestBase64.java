package it.finmatica.dmServer.testColleghi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.jfc.utility.Base64;

public class TestBase64 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			Base64 b = new Base64();
			
			//String sStringa, sStringaOut;
			//sStringa="Z2Rt";
			
			//sStringaOut = new String(b.f_decode(sStringa));
			
		
			
			//System.out.println(sStringaOut);
			
			String sIn="gdm";
			
			LetturaScritturaFileFS fs = new LetturaScritturaFileFS("C:\\Users\\gmannella\\Desktop\\foto prova\\2foto.jpg");
			InputStream is = fs.leggiFile();
						
			String s=b.f_encode(Global.getBytesToEndOfStream(is));
			
			
	
			/*LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS("C:\\Users\\gmannella\\Desktop\\test.jpg");
			
			fs2.scriviFile(new ByteArrayInputStream(b.f_decode(s)));*/
			
			LetturaScritturaFileFS fs2 = new LetturaScritturaFileFS("C:\\Users\\gmannella\\Desktop\\test.txt");
	
			fs2.scriviFile(new ByteArrayInputStream(s.getBytes()));
		
		/*it.finmatica.jfc.crypto.DesEncrypter des = 
			new it.finmatica.jfc.crypto.DesEncrypter("la sugna");
		
		System.out.println(des.encrypt("PIPPO"));
		
		System.out.println(des.decrypt("0dEhD+e0Lkg="));*/
		
	
			 
			
	}

}

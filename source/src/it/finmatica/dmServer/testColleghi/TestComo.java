package it.finmatica.dmServer.testColleghi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;


public class TestComo {
	public static void main(String[] args)  {
		InputStream is;
        try {
        	
            URL url = new URL("http://svi-ora03:9080/GdmSyncroWebServlet/GDMSyncroServlet");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            
            BufferedWriter out = 
                new BufferedWriter( new OutputStreamWriter( conn.getOutputStream() ) );
            out.write("DOWNLOAD=1&IDSYNCRO=57&USERGDM=GD&DSN=jdbc:oracle:thin:@svi-ora03:1521:GDMTEST\r\n");
            out.flush();
            out.close();
            
             is= conn.getInputStream();
            BufferedReader in = 
                new BufferedReader( new InputStreamReader( is ) );
            
            String response;
            while ( (response = in.readLine()) != null ) {
            	
                System.out.println( response );
            }
            in.close();
        }
        catch ( MalformedURLException ex ) {
            // a real program would need to handle this exception
        	ex.printStackTrace();
        }        
        catch ( IOException ex ) {
            // a real program would need to handle this exception
        	//System.out.println("AA"+ex.getCause());        	
        	ex.printStackTrace();
        }
        
        
    }
}
	
	


package it.finmatica.dmServer.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CallServlet {
	private String urlStr, par;
	
	public CallServlet(String urlStr, String par) {
		this.urlStr=urlStr;
		this.par=par;
	}
	
	public byte[] call() throws MalformedURLException,IOException,Exception {		   
		   URL url = new URL(urlStr);
           URLConnection conn = url.openConnection();
           conn.setDoOutput(true);
           
           BufferedWriter out = 
               new BufferedWriter( new OutputStreamWriter( conn.getOutputStream() ) );
           out.write(par+"&FAKE=1\r\n");
           out.flush();
           out.close();
           
           InputStream is= conn.getInputStream();
           
           byte[] b = Global.getBytesToEndOfStream(is);
           
           try {is.close();}catch(Exception e){	}
           
           return b;
          
	}
}

/*******************************************************
 * Progetto	= DownloadFile
 * Package 	= it.finmatica.downloadfile
 * File 	= Download.java
 * User 	= fmignogna
 * Date 	= 13/nov/07
 *******************************************************/
package it.finmatica.dmServer.testColleghi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//
//import org.apache.commons.httpclient.Credentials;
//import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
//import org.apache.commons.httpclient.Header;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.HttpStatus;
//import org.apache.commons.httpclient.StatusLine;
//import org.apache.commons.httpclient.UsernamePasswordCredentials;
//import org.apache.commons.httpclient.auth.AuthScope;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.params.HttpClientParams;
//import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @author fmignogna
 * @date 13/nov/07
 */
public class Download {

//	String url = "http://efesto:8080/jdms/restrict/DownloadAll.do?idDoc=49080";
//	//String url = "http://sanas:8080/sa4asp/restrict/menuAspReparto.html";
//	//String url = "http://www.comune.firenze.it/40anniversario.pdf";
//	String user = "gdm";
//	String pass = "gdm";
	
	public Download(){
//		 Create an instance of HttpClient.
//	    HttpClient client = new HttpClient();
//
//	    Credentials defaultcreds = 
//			new UsernamePasswordCredentials(
//					user, 
//					pass);
//        client.getState().setCredentials(AuthScope.ANY, defaultcreds);
//	    
//        
//	    // Create a method instance.
//        GetMethod method = new GetMethod(url);
//        
//        method.setFollowRedirects(true);
//        
//	    
//	    // Provide custom retry handler is necessary
//	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
//	    		new DefaultHttpMethodRetryHandler(3, false));
//	    
//	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
//	    		new DefaultHttpMethodRetryHandler(3, false));
//	    
//	    
//	    try {
//	      // Execute the method.
//	      int statusCode = client.executeMethod(method);
//	      
//	      System.out.println("method.getPath() " + method.getPath());
//	      System.out.println("method.getURI() " + method.getURI());
//	      
//	      System.out.println("\n\nmethod.getResponseBodyAsString() "+method.getResponseBodyAsString());
//	      System.out.println("\n\n");
//	      
//	  /*    if (statusCode != HttpStatus.SC_OK) {
//	        System.err.println("Method failed: " + method.getStatusLine());
//	      }
//	      
//	      System.out.println("method.name " + method.getName());*/
//
//	      //Cerco tra tutti gli header
//	/*      Header headers[] = method.getResponseHeaders();
//	      if(headers != null ){
//	    	  System.out.println("headers size " + headers.length);
//	    	  for(Header h : headers){
//	    		  System.out.println("\nHeader name " + h.getName());
//	    		  System.out.println("Header value " + h.getValue());
//	    	  }
//	    	  
//	      }
//	      
//	      
//	      HttpMethodParams parametri = method.getParams();
//	      Object obj = parametri.getParameter("attachment");
//	      
//	      if(obj == null){
//	    	  System.out.println("Cazzo");
//	      }else{
//	    	  System.out.println("ok c'è un file");
//	      }
//	      
//	      StatusLine line = method.getStatusLine();
//	      
//	      System.out.println("line.toString() " +line.toString());
//	      System.out.println("method.getStatusText " + method.getStatusText());*/
//	      // Read the response body.
//	      //byte[] responseBody = method.getResponseBody();
//	    /* InputStream stream = method.getResponseBodyAsStream();
//	      
//	      File FO = new File("C:\\\\temp\\JBATCH\\page.html");
//	      FileOutputStream FOS = new FileOutputStream(FO);
//	      int i = 0;
//	      while(i != -1){
//	    	 i = stream.read();
//	    	 if(i != -1){
//	    		 FOS.write(i);
//	    	 }
//	    	  
//	      }
//	      stream.close();
//	      FOS.close();*/
//
//	    } catch (HttpException e) {
//	      System.err.println("Fatal protocol violation: " + e.getMessage());
//	      e.printStackTrace();
//	    } catch (IOException e) {
//	      System.err.println("Fatal transport error: " + e.getMessage());
//	      e.printStackTrace();
//	    } finally {
//	      // Release the connection.
//	      method.releaseConnection();
//	    }
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Download dow = new Download();
		

	}

}
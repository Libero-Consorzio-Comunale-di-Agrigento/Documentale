package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.textparser.AbstractParser;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
 
/**
 * Gestione dei pulsanti.
 * Classe di servizio per la gestione del Client
*/

public class BottonieraParser extends AbstractParser
{
  private String xml;
  HttpServletRequest pRequest = null;
  Profilo doc=null;
 
  public BottonieraParser(String newxml)
  {
    xml=newxml;
  }
  
  public BottonieraParser(String newxml,HttpServletRequest request)
  {
    xml=newxml;
    pRequest = request;
  }
  
  public BottonieraParser(HttpServletRequest request) {
	    pRequest = request;
  }
  
  public BottonieraParser(HttpServletRequest request,Profilo p) {
	    pRequest = request;
	    doc=p;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) 
  {
	  		String valore=null;
	  		
	  		if (nomePar.equalsIgnoreCase("XML"))
    	     valore=xml;
	  		
	  		if(valore==null && doc!=null)
		  	  valore=doc.getCampo(nomePar);	
		  	
	  		if((valore==null) || (valore.equals("")))
              valore=(String)pRequest.getSession().getAttribute(nomePar);
	  		
	  		if((valore==null) || (valore.equals("")))
		       valore=(String)pRequest.getParameter(nomePar);
			  	
	  		return valore;
  }

 
}
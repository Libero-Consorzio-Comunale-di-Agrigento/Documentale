package it.finmatica.dmServer.jdms;

import it.finmatica.textparser.AbstractParser;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

public class ParametriSessioneParser extends AbstractParser {

  private HttpServletRequest pRequest = null;
	  
  public ParametriSessioneParser(HttpServletRequest request) {
	    pRequest = request;
  }
  
  protected String findParamValue(String nomePar, Properties extraKeys) 
  {
	  		String valore=null;		  		
	  		
	  		if((valore==null) || (valore.equals("")))
              valore=(String)pRequest.getSession().getAttribute(nomePar);
	  		
	  	    return valore;
  }

}

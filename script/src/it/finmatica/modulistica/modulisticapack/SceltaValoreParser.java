package it.finmatica.modulistica.modulisticapack;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import it.finmatica.textparser.AbstractParser;
 
public class SceltaValoreParser extends AbstractParser {
	private HttpServletRequest request;
  private String valore;
  private String campo;
  
  public SceltaValoreParser(HttpServletRequest pRequest, String pCampo, String pValore) {
  	request = pRequest;
  	campo = pCampo;
    valore = pValore;
  }
  
  protected String findParamValue(String nomePar, Properties extraKeys) {
  	if (!nomePar.equalsIgnoreCase(campo)) {
    	String otherVal = "";
    	otherVal = (String)request.getParameter(nomePar);
  		if (otherVal == null) {
  			otherVal = "";
  		}
  		return otherVal;
  	} else {
  		return valore.replaceAll("'", "''");
  	}
  }
}

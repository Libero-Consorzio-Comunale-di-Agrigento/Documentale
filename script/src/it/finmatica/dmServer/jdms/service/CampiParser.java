package it.finmatica.dmServer.jdms.service;

import it.finmatica.textparser.AbstractParser;
import java.util.Properties;
import java.util.Vector;

public class CampiParser extends AbstractParser
{
  private String sqlCampi=""; 

  public String getSqlCampi() {
	return sqlCampi;
  }

  public void setSqlCampi(String sqlCampi) {
	this.sqlCampi = sqlCampi;
  }
  
  protected String findParamValue(String campo, Properties extraKeys) 
  {
	  	sqlCampi+="F_VALORE_CAMPO (D.ID,'"+campo.trim()+"') "+campo+",";
	  	return ":"+campo;		
  }

}
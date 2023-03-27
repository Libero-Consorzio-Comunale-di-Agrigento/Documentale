package it.finmatica.dmServer.jdms.service;

import it.finmatica.textparser.AbstractParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

public class EtichetteParser extends AbstractParser
{
  public String descrizione;
  
  private HashMap<String,String> mapCampi; 
 
  private ResultSet rsCampi;
  
  public EtichetteParser(ResultSet rs)
  { 
	  rsCampi = rs;
  }
  
  protected String findParamValue(String campo, Properties extraKeys) 
  {	  	
	  	String valore=null;
	
	  	try {
			valore = rsCampi.getString(campo);
		} catch (SQLException e) {
			valore="";
		}
		return valore;
  }

}
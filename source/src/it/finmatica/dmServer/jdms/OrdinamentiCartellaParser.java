package it.finmatica.dmServer.jdms;


import it.finmatica.textparser.AbstractParser;
import java.util.Properties;



/**
 * Gestione dei pulsanti.
 * Classe di servizio per la gestione del Client
*/

public class OrdinamentiCartellaParser extends AbstractParser
{

  public OrdinamentiCartellaParser() {}
  
  protected String findParamValue(String nomePar, Properties extraKeys) 
  {
	        if(nomePar.indexOf("#")==-1)
	        	return "f_valore_campo(profilo,'"+nomePar+"')";
	        else
	        	return "f_valore_campo_multipla(profilo,'"+nomePar+"')";
	      	
  }
  
}
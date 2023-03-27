package it.finmatica.modulistica;

import org.owasp.encoder.Encode;

import it.finmatica.modulistica.parametri.Parametri;

public class Test {

  public static void main(String[] args) throws Exception {
  	String valore = null;
  	Test.verificaParametroGet("1", valore);
  	Test.verificaParametroGet("2", "prova");
  	
  }
  
  private static void verificaParametroGet(String parametro, String valore) throws Exception {
  	if (valore == null) {
  		return;
  	}
  	String newVal =  Test.encode(valore, "S");
  	
  	if (!newVal.equals(valore)) {
  		throw new Exception("Errore. Parametro "+parametro+" non valido!");
  	}
  }

  public static String encode (String string, String tipoCampo) {
    if (string == null) {
      return null;
    }
    if ((tipoCampo.equalsIgnoreCase("S") || tipoCampo.equalsIgnoreCase("T") || tipoCampo.equalsIgnoreCase("F")) && Parametri.CODIFICA_XSS.equalsIgnoreCase("S")) {
    	string = Encode.forHtmlAttribute(string);
    }

    return string;
  }    

}

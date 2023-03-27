package it.finmatica.modutils.multirecord;
import it.finmatica.textparser.AbstractParser;
import java.util.*;


public class MultiRecParser extends AbstractParser {
  protected LinkedList listaDati;
  int riga;
  String utente = "";
  public MultiRecParser(LinkedList pListaDati, int pRiga, String pUtente ) {
    listaDati = pListaDati;
    riga = pRiga;
    utente = pUtente;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) {
    String retval;
    retval = leggiValore(nomePar, riga);
    return retval;
  }

  /**
   * 
   */
  private String leggiValore(String nomeCampo, int riga) {
    String retval ="";
    if (nomeCampo.equalsIgnoreCase("Utente") || nomeCampo.equalsIgnoreCase("UtenteGDM")) {
    	return utente;
    }
    String valori = (String)listaDati.get(riga);
    int i = valori.indexOf("<C>"+nomeCampo.toUpperCase()+"</C>");
    if (i > -1) {
      int j = valori.indexOf("<V>",i)+3;
      int x = valori.indexOf("</V>",j);
      retval = valori.substring(j,x);
    } else {
      //retval = "<span >Campo "+nomeCampo+" non presente!</span>";
    	retval = ":"+nomeCampo;
    }

    return retval;
  }

}
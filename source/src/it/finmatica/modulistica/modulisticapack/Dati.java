package it.finmatica.modulistica.modulisticapack;
import java.util.*;

 
public class Dati {
  protected         List<String>        codici;
  protected         List<String>        valori;

  public Dati() {
    codici          = new ArrayList<String>();
    valori          = new ArrayList<String>();
  }

  public String getCodice(int i) {
    return (String) codici.get(i);
  }

  public String getCodice(String pValore) {
    String  retval = "";
    boolean trovato = false;
    int     i = 0;

    while ((i<valori.size()) && (!trovato)) {
      if (valori.get(i).equals(pValore)) {
        retval = (String) codici.get(i);
        trovato = true;
      }
      i++;
    }
    return retval;
  }

  public String getValore(int i) {
    return (String) valori.get(i);
  }

  /**
   *
   */
  public String getValore(String pCodice) {
    String  retval = "";
    boolean trovato = false;
    int     i = 0;

    while ((i<codici.size()) && (!trovato)) {
      if (codici.get(i).equals(pCodice)) {
        retval = (String) valori.get(i);
        trovato = true;
      }
      i++;
    }
    if (trovato) {
      return retval;
    } else {
      return null;
    }
  }

  public void aggiungiDato(String codice, String valore) {
    codici.add(codice);
    valori.add(valore);
  }

  public int getNumeroValori() {
    return valori.size();
  }
}
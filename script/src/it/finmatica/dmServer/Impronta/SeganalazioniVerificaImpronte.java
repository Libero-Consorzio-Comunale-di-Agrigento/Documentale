/**
 * 
 */
package it.finmatica.dmServer.Impronta;

import java.util.ArrayList;

/**
 * @author mbonforte
 *
 */
public class SeganalazioniVerificaImpronte {
  protected         ArrayList<String>        nomiFile;
  protected         ArrayList<String>        codici;
  protected         ArrayList<String>        dateModifica;

  /**
   * Costruttore per la lista delle segnalazioni di una verifica impronte
   * 
   */
  public SeganalazioniVerificaImpronte() {
  	nomiFile          = new ArrayList<String>();
  	codici          	= new ArrayList<String>();
  	dateModifica			= new ArrayList<String>();
  }

  /**
   * Metodo che aggiunge una segnalazione alla lista
   * 
   * @param nomeFile 	nome del file verificato
   * @param codice	 	risultato della verifica
   */
  public void aggiungiSegnalazione(String nomeFile, String codice, String dataModifica) {
  	nomiFile.add(nomeFile);
  	codici.add(codice);
  	dateModifica.add(dataModifica);
  }

  /**
   * Metodo che restituisce il numero delle segnalazioni presenti nella lista
   * 
   * @return int
   */
  public int getNumeroValori() {
    return nomiFile.size();
  }

  /**
   * Metodo che restituisce l'i-esimo nome file presente nella lista
   * 
   * @param i	posizine nella lista
   * @return 	Stringa contente il nome del file
   */
  public String getNomeFile(int i) {
    return nomiFile.get(i);
  }

  /**
   * Metodo che restituisce l'i-esimo codice presente nella lista
   * 
   * @param i	posizine nella lista
   * @return 	Stringa contente il codice della verifica
   */
  public String getCodice(int i) {
    return codici.get(i);
  }

  /**
   * Metodo che restituisce l'i-esima data modifica presente nella lista
   * 
   * @param i	posizine nella lista
   * @return 	Stringa contente la data dell'ultima modifica dell'allegato
   */
  public String getData(int i) {
    return dateModifica.get(i);
  }

  /**
    * Metodo che restituisce il codice della verifica relativa al file specificato
    * se no presente restituisce valore nullo
    * 
    * @param nomeFile nome del file	
    * @return 	Stringa contente il nome del file
    */
  public String getCodice(String nomeFile) {
    String  retval = null;
    boolean trovato = false;
    int     i = 0;

    while ((i<nomiFile.size()) && (!trovato)) {
      if (nomiFile.get(i).equals(nomeFile)) {
        retval = getCodice(i);
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
  
  /**
   * Metodo che restituisce la data ultima modifica dell'allegato relativa al file specificato
   * se no presente restituisce valore nullo
   * 
   * @param nomeFile nome del file	
   * @return 	Stringa contente la data
   */
 public String getData(String nomeFile) {
   String  retval = null;
   boolean trovato = false;
   int     i = 0;

   while ((i<nomiFile.size()) && (!trovato)) {
     if (nomiFile.get(i).equals(nomeFile)) {
       retval = getData(i);
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
 
}

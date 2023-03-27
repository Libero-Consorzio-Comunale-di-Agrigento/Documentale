package it.finmatica.dmServer.Impronta;

import java.util.ArrayList;
import java.util.List;

public class ListaImpronte {
  protected         ArrayList<String>        nomiFile;
  protected         ArrayList<byte[]>        impronte;

  public ListaImpronte() {
  	nomiFile          = new ArrayList<String>();
  	impronte          = new ArrayList<byte[]>();
  }

  public void aggiungiImpronta(String nomeFile, byte[] impronta) {
  	nomiFile.add(nomeFile);
  	impronte.add(impronta);
  }

  public int getNumeroValori() {
    return nomiFile.size();
  }

  public byte[] getImpronta(int i) {
    return (byte[]) impronte.get(i);
  }

  public String getNomeFile(int i) {
    return (String) nomiFile.get(i);
  }

  public byte[] getImpronta(String nomeFile) {
    byte[]  retval = null;
    boolean trovato = false;
    int     i = 0;

    while ((i<nomiFile.size()) && (!trovato)) {
      if (nomiFile.get(i).equals(nomeFile)) {
        retval = getImpronta(i);
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

  public boolean esisteImpronta(String nomeFile) {
    boolean trovato = false;
    int     i = 0;

    while ((i<nomiFile.size()) && (!trovato)) {
      if (nomiFile.get(i).equals(nomeFile)) {
        trovato = true;
      }
      i++;
    }
    return trovato;
  }

  public void rimuoviImpronta(String nomeFile) {
    boolean trovato = false;
    int     i = 0;

    while ((i<nomiFile.size()) && (!trovato)) {
      if (nomiFile.get(i).equals(nomeFile)) {
      	nomiFile.remove(i);
        impronte.remove(i);
        trovato = true;
      } else {
      	i++;
      }
    }
  }

}

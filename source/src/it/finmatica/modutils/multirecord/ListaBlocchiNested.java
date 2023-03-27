package it.finmatica.modutils.multirecord;

import java.util.ArrayList;

public class ListaBlocchiNested {
	private ArrayList<BloccoNested> blocchi;

	/**
   * 
   */
  public ListaBlocchiNested() throws Exception {
    blocchi = new ArrayList<BloccoNested>();
  }

  public void aggiungiBlocco(BloccoNested bn) {
  	blocchi.add(bn);
  }
  
  public BloccoNested getBlocco(int i) {
  	return blocchi.get(i);
  }
  
  public BloccoNested getBlocco(String nomeBlocco) {
  	BloccoNested bn = null;
  	boolean trovato = false;
  	int i = 0;
  	while (i < blocchi.size() && !trovato) {
  		if (blocchi.get(i).getBlocco().equalsIgnoreCase(nomeBlocco)) {
  			trovato = true;
  			bn = blocchi.get(i);
  		} else {
  			i++;
  		}
  	}
  	
  	return bn;
  }
  
  public int numeroBlocchi() {
  	return blocchi.size();
  }
}

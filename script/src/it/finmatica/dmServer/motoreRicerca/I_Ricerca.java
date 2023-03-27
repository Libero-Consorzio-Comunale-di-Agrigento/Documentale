/***********************************************************************
 * Module:  I_Ricerca.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Interfaccia per la gestione della ricerca                     
 *          a livello molto alto
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

import java.util.*;

// ----------------------------
// Definizione dell'interfaccia
// ----------------------------
public interface I_Ricerca
{
  /* DEFINIZIONE DEI METODI IMPLEMENTATI */  
  
  String getQuery() throws Exception;
  void fillDocumentList() throws Exception;
  Vector getDocumentList();
  String existsDocument() throws Exception;

}
//
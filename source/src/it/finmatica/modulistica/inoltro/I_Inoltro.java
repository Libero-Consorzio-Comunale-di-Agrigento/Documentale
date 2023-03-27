package it.finmatica.modulistica.inoltro;

import xmlpack.*;
/**
 * Interfaccia standard per tutte le tipologie di inoltro di oggetti XML.
 * Il metodo <i>init()</i> dà la possibilità di inizializzare i parametri necessari
 * alla procedura effettiva di inoltro (<b>parametri</b>) in forma codificata.
 * Il tipo di codifica è lasciato a chi implementa la classe.<br> 
 * Il parametro <b>marcatoriDiCampo</b> serve appunto a specificare quali sono i marcatori di inizio campo.
 * Ad esempio se si intende comunicare all'oggetto <B>MioInoltro</B> che implementa l'interfaccia che i parametri
 * <b><i>param_01</i></b> e <b><i>param_02</i></b> valgono rispettivamente <i>100</i> e <i>200</i> 
 * allora si puo scegliere di utilizzare, ad esempio, i marcatori di campo [param_01] [/param_01] e 
 * [param_02] [/param_02] e di conseguenza:
 * <br><br>
 * <code>
 * parametri = "[param_01]100[/param_01][param_02]200[/param_02]"<br>
 * marcatoriDiCampo[] = { "[param_01]","[/param_01]","[param_02]","[/param_02]" }
 * </code>
 * <br><br>
 * sarà poi compito dell'oggetto <b>MioInoltro</b> utilizzare adeguatamente i parametri ed i marcatori di campo
 * per ottenere le informazioni necessarie.
 * Il metodo <code>inoltro()</code> si specifica l'oggetto di tipo OggettoXML che si vuole inoltrare.
 * 
 * @author       Antonio
 * @version      1.0
 * @see          xmlpack
 */
 
public interface I_Inoltro {

  /**
   * Metodo di inizializzazione.
   * 
   * @param        parametri E' la stringa nella quale vengono codificati i parametri
   *                         necessari alla procedura di inoltro.
   * @param        marcatoriDiCampo E' un vettore di stringhe; ognuna delle stringhe delimita uno dei
   *                                campi passati attraverso il parametro <i>parametri</i>.
   * @param        oggettoXML E' l'oggetto di tipo <code>OggettoXML</code> che si vuole inoltrare.
   * @return       void
   * @since        1.0
   */
  public void init(String parametri, String[] marcatoriDiCampo);

  /**
   * Metodo per l'inoltro dell'oggetto
   * 
   * @param  oggettoXML E' l'oggetto di tipo OggettoXML che si vuole inoltrare.
   * @return boolean Vale <code>false</code> se si sono verificati problemi.
   * @since  1.0
   */
  public boolean doInoltro(OggettoXML oggettoXML);
}


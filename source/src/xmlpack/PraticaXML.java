package xmlpack;

import java.util.ArrayList;
import java.sql.*;
//import java.io.*;
import org.w3c.dom.*;
//import org.apache.xerces.*;
import it.finmatica.jfc.dbUtil.*;

/**
 * Classe che rappresenta e costruisce l'albero XML per la rappresentazione del file da
 * passare all'applicazione. E' la radice di un albero di oggetti 'parallelo' a quello DOM
 * utilizzato per la creazione dell'XML.
 * Contiene i dati propri della richiesta (numero identificatico e area) e la lista dei modelli 
 * che appartengono a quella richiesta.
 */
public class PraticaXML extends OggettoXML {
  ArrayList   listaModelli;
  InfoConnessione infoConnesione;
//  DbOperationSQL dbOpSQLTemp;
  
  /** 
   *  Costruttore.
   *  'AD4-147F197DDF925203E930D231E2B3A926','AD4','utente','ADSXML',''
   */
  public PraticaXML(InfoConnessione infoConnesione, String pCodiceRichiesta, String pArea, String pUtente, String pCodiceXML, String pAllegati) {
    super (infoConnesione, pCodiceRichiesta, pArea, pUtente, pCodiceXML, pAllegati);

//    try {
//      dbOpSQLTemp = new DbOperationSQL(dbOpSQL);  // Mi serve per non schifiare la dbOpSQL
      listaModelli = new ArrayList();
      this.infoConnesione = infoConnesione;
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }  
  }

 /**
  * Genera la struttura DOM corrispondente all'XML dei dati attuali.
  */
  public void generaXML() { //  protected
    Element             root,
                        modNodo,
                        elNodo;
    Text                txtVal;
    ModelloXML          modXML;
    DatoXML             datoXML;
    String              nomeAllegato;
    int                 i, j;
    
    DOMImplementation   impl = org.apache.xerces.dom.DOMImplementationImpl.getDOMImplementation();

    docXML = impl.createDocument(null, "Pratica", null);
    root = docXML.getDocumentElement();
    root.setAttribute("codiceRichiesta", codiceRichiesta);
    for (i=0; i<listaModelli.size(); i++){
      // Per ogni modello
      modXML = (ModelloXML)listaModelli.get(i);
      modNodo = docXML.createElement("Modello");
      modNodo.setAttribute("Area", modXML.getArea());
      modNodo.setAttribute("Codice", modXML.getCodice());
      modNodo.setAttribute("Autore", modXML.getAutore());
      modNodo.setAttribute("Progressivo", modXML.getProgressivo());
      for(j=0; j<modXML.getDati().size(); j++){
        // Per ogni singolo dato
        datoXML = (DatoXML)(modXML.getDati()).get(j); 
        elNodo = docXML.createElement("Dato");
        elNodo.setAttribute("NomeDato", datoXML.getNomeCampo()); // setto il nome come attributo
        txtVal = docXML.createTextNode(datoXML.getValore());  // ... e il valore comem nodo filgio di tipo testo
        elNodo.appendChild(txtVal); // poi costruisco l'albero partendo dalla foglia (testo)
        modNodo.appendChild(elNodo); //... e agganciando il sotto-albero al padre
      }
      root.appendChild(modNodo); // appendo il nodo relativo al modulo alla 'radice' (= pratica)
    }
    for (i=0; i<listaAllegati.size(); i++){
      nomeAllegato = (String)listaAllegati.get(i);
      modNodo = docXML.createElement("Allegato"); // Creato allo stesso livello del modello
      modNodo.setAttribute("Nome", nomeAllegato); // Lo metto come attributo in quanto chiave
      root.appendChild(modNodo); // appendo il nodo relativo al modulo alla 'radice' (= pratica)      
    }
  }

  /**
   * Legge i dati del repositoryTemp ed interpretandoli costruisce la corretta lista dei modelli.
   */
   public void caricaDaDB() {
    ResultSet         rst;
    String            query;
    ModelloXML        md = null;
    String            pArea, 
                      pCodiceModello, 
                      pAutore="";
    int               pProgressivo;

    query = "SELECT distinct area,codice_modello, progressivo "+
            "FROM PRE_INOLTRO "+
            "WHERE CODICE_RICHIESTA ='"+codiceRichiesta+"' "+
            "ORDER BY AREA, CODICE_MODELLO DESC";
                        
    try {
      DbOperationSQL dbOpSQL = new DbOperationSQL(infoConnesione.getAlias(), 
                                               infoConnesione.getDsn(), 
                                               infoConnesione.getUser(), 
                                               infoConnesione.getPasswd());
      dbOpSQL.setStatement(query);
      dbOpSQL.execute();
      rst = dbOpSQL.getRstSet();
      
      if (rst.next() == false) {
//        Util.writeLog("PraticaXML::caricaDaDB()","Impossibile trovare dati relativi alla richiesta con codice "+codiceRichiesta);
        throw new Exception("Impossibile trovare dati relativi alla richiesta con codice "+codiceRichiesta);
      }
      
      // Ciclo di caricamento
      do {
        pArea = rst.getString("AREA");
        pCodiceModello = rst.getString("CODICE_MODELLO");
        pProgressivo = rst.getInt("PROGRESSIVO");

        if (md == null) {
          // Creo un modello nuovo
          md = new ModelloXML(infoConnesione, pArea, pCodiceModello, pProgressivo, pAutore); 
          listaModelli.add(md);
          md.caricaDaDb(codiceRichiesta);
        } else {
          if (!md.stessoModello(pArea, pCodiceModello, pProgressivo, pAutore)){
            // Non è lo stesso modello ne creo uno nuovo
//            Util.writeLog("praticaxml::caricadadb()","nuovo modello diverso dal precedente");
            md = new ModelloXML(infoConnesione, pArea, pCodiceModello, pProgressivo, pAutore);
            listaModelli.add(md);          
            md.caricaDaDb(codiceRichiesta);
          }
          else{
            // *** Altrimenti non faccio nulla ***
          }
        }
      } while (rst.next());
      dbOpSQL.close();
    } catch (Exception ex) {
//      Util.writeLog("PraticaXML::caricaDaDB()","Impossibile caricare dati da DB.");
      ex.printStackTrace();
    } 
//    Util.writeLog("PraticaXML::caricaDaDB()","(fine)");
  }
  
  public boolean valido(){
    return true;
  }
  
}
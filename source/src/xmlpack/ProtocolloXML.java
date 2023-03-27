package xmlpack;

import java.util.*;
import org.w3c.dom.*;
//import org.apache.xerces.*;
//import it.finmatica.jfc.dbUtil.*;
//import admindblib.*;


/**
 * Classe che rappresenta e costruisce l'albero XML per la rappresentazione del file da
 * passare al protocollo.
 */
public class ProtocolloXML extends OggettoXML {
  protected Date      data; 
  protected String    documentoPrincipale;
  protected String    oggetto,  
                      tipoDocumento, 
                      movimento,
                      ufficioSmistamento,
                      unitaProtocollo, 
                      applicativoEsterno, 
                      classificazione;
  protected ArrayList mittenteDestinatario;

  /**
   * Costruttore.
   *
   * @param l'unico parametro da commentare è il mittenteDestinatario che passato come stringa
   * sarà il primo elemento della lista dei mittentiDestinatari
   */
  public ProtocolloXML(InfoConnessione infoConnesione,
                       String pCodiceRichiesta, 
                       String pArea,
                       Date   pData, 
                       String pOggetto, 
                       String pTipoDocumento, 
                       String pMovimento,
                       String pUfficioSmistamento, 
                       String pMittenteDestinatario,
                       String pUnitaProtocollo, 
                       String pApplicativoEsterno, 
                       String pUtente, 
                       String pClassificazione, 
                       String pDocumentoPrincipale, 
                       String pCodiceXML, 
                       String pAllegati) {
                       
    super(infoConnesione, pCodiceRichiesta, pArea, pUtente, pCodiceXML, pAllegati);
    
    documentoPrincipale = pDocumentoPrincipale;
    mittenteDestinatario = new ArrayList();
    data = pData;
    oggetto = pOggetto;
    tipoDocumento = pTipoDocumento;
    movimento = pMovimento;
    ufficioSmistamento = pUfficioSmistamento;
    
    if (!pMittenteDestinatario.equals("")) {
      // Puo' essere una stringa o una concatenazione di stringhe, 
      // la sequenza di separatore è @#@
      String tempMitt, tAdd;
      tempMitt = pMittenteDestinatario;
      
      while (tempMitt.indexOf("@#@") != -1) {
        tAdd = tempMitt.substring(0, tempMitt.indexOf("@#@"));
        if (!tAdd.equals("")) {
          mittenteDestinatario.add(tAdd);
        }
        
        tempMitt = tempMitt.substring(tempMitt.indexOf("@#@")+3);
      }
      mittenteDestinatario.add(tempMitt);
    }
    unitaProtocollo = pUnitaProtocollo;
    applicativoEsterno = pApplicativoEsterno;
    classificazione = pClassificazione;
  }

  /**
   * Aggiunge un mittente / destinatario alla lista di quelli presenti.
   *
   * @param mittdest è il mittente destinatario da aggiungere
   */
  public void aggiungiMittDest(String pMittDest){
      mittenteDestinatario.add(pMittDest);
  }
  
  /**
   * Genera la struttura DOM corrispondente all'XML dei dati attuali.
   */
  public void generaXML() {
    Element             root,
                        allNodo,
                        elNodo;
    Text                txtVal;
    String              nomeAllegato, 
                        mittDest;
    int                 i/*, j*/;
    
    DOMImplementation   impl = org.apache.xerces.dom.DOMImplementationImpl.getDOMImplementation();

    docXML = impl.createDocument(null, "Protocollo", null);
    root = docXML.getDocumentElement();
    allNodo = docXML.createElement("Intestazione"); // Creo intestatzione
    elNodo = docXML.createElement("Data");
    txtVal = docXML.createTextNode(data.toString()); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("Oggetto");
    txtVal = docXML.createTextNode(oggetto); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("TipoDocumento");
    txtVal = docXML.createTextNode(tipoDocumento); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("Movimento");
    txtVal = docXML.createTextNode(movimento); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("Classificazione");
    txtVal = docXML.createTextNode(classificazione); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("Smistamento");
    txtVal = docXML.createTextNode(ufficioSmistamento); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("UnitaProtocollante");
    txtVal = docXML.createTextNode(unitaProtocollo); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("Utente");
    txtVal = docXML.createTextNode(utente); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("Applicativo");
    txtVal = docXML.createTextNode(applicativoEsterno); 
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    elNodo = docXML.createElement("DocumentoPrincipale");
    txtVal = docXML.createTextNode(documentoPrincipale);
    elNodo.appendChild(txtVal); 
    allNodo.appendChild(elNodo);
    // Scorro tutti i mittenti destinatari ...
    for(i=0; i<mittenteDestinatario.size(); i++){
      mittDest = (String)mittenteDestinatario.get(i);
      elNodo = docXML.createElement("MittDest");
      elNodo.setAttribute("Tipo", "RAGSOC"); //***************** RAGSOC o NI ******************
      txtVal = docXML.createTextNode(mittDest); 
      elNodo.appendChild(txtVal); 
      allNodo.appendChild(elNodo);
    }
    root.appendChild(allNodo); // Aggancio intestazione alla radice
    for (i=0; i<listaAllegati.size(); i++){
      nomeAllegato = (String)listaAllegati.get(i);
      allNodo = docXML.createElement("Allegato"); // Creato allo stesso livello del modello
      allNodo.setAttribute("Nome", nomeAllegato); // Lo metto come attributo in quanto chiave
      root.appendChild(allNodo); // appendo il nodo relativo al modulo alla 'radice' (= pratica)      
    }
  }

  //***************** 
  public boolean valido(){return true;};

}
package it.finmatica.modulistica.inoltro;

import java.util.*;
import it.finmatica.jfc.authentication.Cryptable;
import xmlpack.*;
//import it.finmatica.modulistica.parametri.Parametri;

/**
 * Classe astratta di base per tutte le tipologie di inoltro che intendono allegare oggetti di tipo 
 * <b>OggettoXML</b>.<br>
 * Rispetto alla semplice interfaccia viene implementato il metodo <code>getHashtableParametri()</code> 
 * di decodifica che utilizza la seguente sintassi di codifica dei parametri:<br>
 * <br>
 * <code>
 * parametri = "[par_01]valoreparametro1[par_02]valoreparametro2 ... [par_<i>n</i>]valoreparametro<i>n</i>"
 * marcatoriDiCampo[] = { "[par_01]","[par_02]", ... ,"[par_<i>n</i>]" }<br>
 * <br>
 * </code>
 * Il metodo restituir� un oggetto di tipo <code>java.lang.Hashtable</code> contenente le coppie (chiave,valore)
 * che, nell'esempio, sarebbero le seguenti:<br>
 * <br>
 * <code>
 * ( [par_01] , valoreparametro1 )   ( [par_02] , valoreparametro2 ) ... ( [par_<i>n</i>] , valoreparametro<i>n</i> )  
 * </code>
 * <br>
 * E' possibile inoltre specificare un ulteriore marcatore (che precede il valore del parametro)
 * tramite il quale � possibile indicare la presenza di un valore criptato. In questo caso il campo
 * viene prima decriptato e successivamente inserito in hashtable.
 * <br>
 * <br>
 * @author       Antonio
 * @version      1.0
 *              
 * @see          it.finmatica.modulistica.inoltro.I_Inoltro
 */
 
public abstract class Inoltro implements I_Inoltro {
  private String     parametri;
  private String[]   marcatoriDiCampo;
  private String     marcatoreDiCrypting = null;
  private String     sDsn = "";
  protected String     errorMessage = "";
 
  private OggettoXML oggettoXML;
  private boolean    initDone = false;

  /**
   * 
   */
  public abstract boolean doInoltro(OggettoXML oggXML);

  
  /**
   * Metodo per l'inizializzazione dei parametri generali.<br>
   * Chiunque voglia creare una propria classe di inoltro deve invocare il metodo <i>init()</i> 
   * in fase iniziale; in caso contrario verr� generato un <code>it.finmatica.modulistica.inoltro.BadInitException</code>
   * 
   * 
   * @param      parametri    E' la stringa contenente i parametri codificati  
   * @param      marcatoriDiCampo E' la lista dei marcatori di campo
   * @return     void  
   * @since      1.0  
   */
  public final void init(String parametri, String[] marcatoriDiCampo) {
    this.parametri = parametri;
    this.marcatoriDiCampo = marcatoriDiCampo;
    this.initDone = true;  // Serve a capire se � stata invocata questa init()
  }


  /**
   * Questo metodo rappresenta lo stumento fornito alla classe derivata da Inoltro
   * (ad esempio InoltroMail) per permettere di invocare la init(parametri, marcatoriDiCampo) 
   * quando i marcatori di campo saranno noti.
   * A questo livello, infatti, si sa soltanto che ci saranno dei particolari marcatori
   * di campo posizionati all'interno della stringa 'parametri', ma non si sa quali saranno.
   * In altri termini, dato che ogni tipo di inoltro (come InoltroMail) sceglie i propri 
   * marcatori di campo, sar� compito della classe derivata invocare la 
   * Inoltro.init(parametri, marcatoriDiCampo).
   * In pi�, quando si utilizzer� la Class.forName() per istanziare oggetti di tipo Inoltro
   * (come InoltroMail) sar� necessario questo metodo per settare la stringa dei parametri,
   * dato che sar� la particolare implementazione di inoltro a decidere i marcatori: il codice
   * chiamante deve solo assicurarsi che la stringa dei parametri rispetti la sintassi
   * indicata per quel paricolare tipo di inoltro.
   */
  public void init(String parametri) {}


  /**
   * Metodo per la definizione del marcatore di crypting.
   * Se il metodo non viene invocato si presuppone che nessun valore � criptato.
   * Se uno o pi� valori presenti fra i parametri sono criptati (e quindi sono 
   * marcati da una qualche sequenza di caratteri che li precede) allora il metodo 
   * DEVE essere invocato PRIMA dell'utilizzo della getHashtableParametri().
   *         
   * @param        mdc E' la sequenza utilizzata per identificare l'inizio di un campo criptato.
   *               Se il valore di un certo campo � criptato e lo si vuole memorizzare in chiaro
   *               nella Hashtable, allora deve essere preceduto dalla stringa <code>mdc</code>.
   *               Ad esempio, se la stringa dei parametri si presenta come:<br><br>
   *               [campo1]valore1[campo2]valore2<B>[campo3][C]%3%&X$1$3$</B>[campo4]valore4<br><br>
   *               allora la stringa mdc deve essere <code>[C]</code>.
   * @exception    BadCryptingMarkerException
   * @since        1.0
   */
  public final void setMarcatoreDiCrypting(String mdc) throws BadCryptingMarkerException {
    if ((mdc != null) && (mdc.trim().length() > 0)) {
      marcatoreDiCrypting = mdc;
      //isMDCDefined = true;
      return;            
    } 
    throw new BadCryptingMarkerException("Attenzione: il marcatore di campo criptato non � valido!");
  }


  /**
   * Getter method
   */
  public final String getMarcatoreDiCrypting() {
    return marcatoreDiCrypting;
  }
  
  
  /**
   * Metodo che restituisce una Hashtable contenente le coppie (campo, valore) 
   * di parametri specificati attraverso 'parametri' del metodo init.
   * I valocri indicati come criptati vengono prima decriptati.
   * 
   * @return       java.util.Hashtable
   */
  public final Hashtable getHashtableParametri() throws BadInitException {
     if (!this.initDone) 
       throw new BadInitException("Attenzione: Mancata invocazione di init().");
       
     Hashtable<Integer,String> hashTemp = new Hashtable<Integer,String>();
     Hashtable<String,String> hashParametri = new Hashtable<String,String>();
     int       temp, 
               i = 0, 
               max = -1, 
               flen = marcatoriDiCampo.length,
               startIndex,
               stopIndex,
               keyLength;
     Integer   a[] = new Integer[flen];

     String    key;
     String    tempParametri = (new String(parametri)).toUpperCase();
     
     // Passo 1: I campi indicati in marcatoriDiCampo e nella stringa dei parametri sono non case-sensitive. 
     //   Preparo una hashtable di appoggio con i campi in maiuscolo e nel frattempo determino l'indice 
     //   dell'ultimo campo indicato nella stringa dei parametri.
     //   Creo pure un array a[] di Integer (le posizioni delle occorrenze dei campi all'interno della stringa 
     //   dei parametri)
     while (i < marcatoriDiCampo.length) {
       marcatoriDiCampo[i] = marcatoriDiCampo[i].toUpperCase();
       temp = tempParametri.indexOf(marcatoriDiCampo[i]);
       if (temp > max) max = temp;
       
       a[i] = new Integer(temp);
       if (temp >= 0) 
         hashTemp.put(a[i], marcatoriDiCampo[i]);
       i++;
     }

     // Passo 2: Ordino l'array delle posizioni.
     Arrays.sort(a);

     // Passo 3: Estraggo i valori di ognuno dei campi. 
     for (int j=0; j<flen; j++) {
       // Se a[j] vale -1 significa che non si � trovata l'occorrenza fra i parametri, quindi passo avanti.
       if (a[j].intValue() < 0) continue;

       // 'Tiro su' la chiave (ovvero il campo j-esimo), la lunghezza della chiave e la posizione della chiave.
       key = (String)hashTemp.get(a[j]);       
       keyLength = ((String)hashTemp.get(a[j])).length();
       startIndex = a[j].intValue()+keyLength;

       // Se il campo � l'ultimo della stringa allora il valore del campo arriva fino a fine stringa,
       //   altrimenti il valore del campo va dalla fine del nome del campo in esame 
       //   fino al carattere che precede l'inizio del prossimo nome di campo.
       //   Man mano che estraggo le coppie le inserisco nell'hashtable.
       if (a[j].intValue() == max) {
         //hashParametri.put(key,parametri.substring(startIndex));
         addPair(hashParametri, key, parametri.substring(startIndex));
       } else {
         stopIndex = a[j+1].intValue();
         //hashParametri.put(key,parametri.substring(startIndex, stopIndex));
         addPair(hashParametri, key, parametri.substring(startIndex, stopIndex));
       }
     }

     return hashParametri;
   }


   /**
    * Metodo per l'aggiunta della coppia chiave-valore all'Hashtable indicata.
    * Se il campo � preceduto dal marcatore di crypting il campo � da considerarsi 
    * codificato in base all'algoritmo standard di cripting.
    * 
    * @param        h
    * @param        key
    * @param        value
    * @see          Cryptable
    * @since        1.0
    */
   private void addPair(Hashtable<String,String> h, String key, String value) {
     // Cerca il marcatore di campo criptato all'inizio del valore di campo
     if ((marcatoreDiCrypting != null) && (value.startsWith(marcatoreDiCrypting))) 
       value = Cryptable.decryptPasswd( value.substring(marcatoreDiCrypting.length()) );
     h.put(key, value); 
     return;
   }

   
  /**
   * Metodo che restituisce l'oggetto di tipo OggettoXML che si vuole inoltrare.
   * 
   * @return OggettoXML
   * @since 1.0
   */
  public final OggettoXML getOggettoXML() {
    return oggettoXML;
  }

  /**
   * Metodo che restituisce l'oggetto di tipo OggettoXML che si vuole inoltrare.
   * 
   * @return OggettoXML
   * @since 1.0
   */
  public final String getDSN() {
    return sDsn;
  }

  /**
   * Metodo che permette di cambiare l'oggetto di tipo OggettoXML che si vuole inoltrare.
   * 
   * @param oggXML 
   * @return void
   * @since 1.0
   */
  public final void setOggettoXML(OggettoXML oggXML) {
    this.oggettoXML = oggXML;
  }

    /**
   * 
   */
  public void parametriRichiesta(String idop, String cr, 
        String ar, String cm, String ctp, String utente, 
        String allegati, InfoConnessione pm) {
        }

   /**
   * 
   */
  public boolean inoltra() {
    return false;
  }

  /**
   * 
   */
  public void setDSN(String pDSN) {
    sDsn = pDSN;
  }


  public String getErrorMessage() {
    return errorMessage;
  }


} // Class Inoltro
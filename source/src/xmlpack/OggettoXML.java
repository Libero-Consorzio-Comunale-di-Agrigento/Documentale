package xmlpack;

import    java.io.*;
import    java.sql.*;
import    org.w3c.dom.*;
import    java.util.ArrayList;
//import    org.apache.xerces.dom.*;
import    org.apache.xml.serialize.*;
import    org.apache.xerces.parsers.DOMParser;
import    org.xml.sax.SAXException;
import    java.io.FileOutputStream;
import    it.finmatica.jfc.dbUtil.*;
//import    it.finmatica.modulistica.parametri.*;

/**
 * Classe base per gli oggetti di tipo XML. Tutto ci� che � standard viene definito 
 * in questa classe gli altri metodi andranno riscritti nelle classi derivate.
 */
 
public class OggettoXML {
  protected String    codiceXML;
  protected String    codiceRichiesta; 
  protected String    area,
                      utente;
  protected ArrayList listaAllegati;
  protected File      fileAssociato;
  protected Document  docXML;
//  protected DbOperationSQL dbOpSQL;
  protected InfoConnessione infoConnessione;
  
  // Da utilizzare per la gestione di Oggetti XML standard - via file-
  public OggettoXML(){
  }

  // Costruttore di un oggetto 
  public OggettoXML(InfoConnessione infoConnessione, String pCodiceRichiesta, String pArea, String pUtente, String pCodiceXML, String pAllegati) {
    this.infoConnessione = infoConnessione;
    this.codiceRichiesta = pCodiceRichiesta;
    this.area = pArea;
    this.utente = pUtente;
    this.codiceXML = pCodiceXML;
    this.listaAllegati = new ArrayList();
//    Util.writeLog("OggettoXML::OggettoXML()","START");

    if ((pAllegati != null) && (pAllegati.trim().length() > 0)) {
    //if (!pAllegati.equals("")){
      // Puo' essere una stringa o una concatenazione di stringhe, 
      // la sequenza di separatore � @#@

      String    tempAll,
                tAdd;

      tempAll = pAllegati;
      while (tempAll.indexOf("@#@") != -1) {
        tAdd = tempAll.substring(0, tempAll.indexOf("@#@"));
        if (!tAdd.equals("")){
          listaAllegati.add(tAdd);
        }
        tempAll = tempAll.substring(tempAll.indexOf("@#@")+3);
      }
      
     listaAllegati.add(tempAll);
    }

//    Util.writeLog("OggettoXML::OggettoXML()", "END");
  }
  
  /**
   * Restituisce un FileInputStream corrispondente al file XML attuale
   */

   private String fInput() throws Exception{
    String            nomeFile;

    nomeFile = "A"+Long.toString(System.currentTimeMillis())+".tmp";
    writeFileXML(nomeFile);
    return nomeFile;    
   }
  
  /**
   * Dovr� essere implementata con la costruzione appropriata 
   * della struttura XML.
   */

//   private void generaXML(){};
   
  /**
   * In pratica un shortcut per la scrittura del XML su file. 
   * Richiede solo il nome del file come parametro e genera lui autonomamente il File di stream 
   * da usare come Output.
   *
   * @param nomeFile nome del file XML che vado a scrivere a partire da questo oggetto
   * @exception puo' generare l'Exception sulla new del file oppure una di quelle generate
   * direttamente dalla writeStream
   * @return nessun valore
   */
   
  public void writeFileXML(String nomeFile) throws Exception{
    FileOutputStream   fout = new FileOutputStream(nomeFile);

    // utilizzo la writeStream per la scrittura dell'oggetto e la sua serilaizzazione
    writeStream(fout);
    fout.close();
  };
  
  /**
   * Scrive l'XML in formato stream su un output predefinito.
   *
   * @param out lo stream di output su cui va 'serilizzato' l'albero XML creato.
   * @exception se una qualsiasi delle funzioni chiamate genera un Exception.
   * @return nessun valore
   */

  public void writeStream(OutputStream out) throws Exception{
    OutputFormat  format=new OutputFormat(docXML);
    XMLSerializer serializer = new XMLSerializer(out, format);

    // A questo punto serilizzo il docXml che suppongo non essere nullo altrimenti 
    // la funzione di serialize genera direttamente la Exception che riporto al chiamante.
    serializer.serialize(docXML);
  }

  /**
   * Legge da un file in formato XML e costruisce direttamente la struttura corrispondente.
   * Utilizza le classi sax.
   * 
   * @param nomeFile nome del file da leggere come input per il parsing dell'oggetto
   * @return nessun valore
   */

   public void readFromFile(String nomeFile){
    DOMParser parser = new DOMParser();
    try {
      parser.parse(nomeFile);
    } catch (SAXException se) { se.printStackTrace(); }
    catch (IOException ioe) { ioe.printStackTrace(); }
    docXML = parser.getDocument();
   }

  /**
   * Aggiunge alla lista degli allegati il codice di uno degli allegati a questo XML, solitamente
   * il nome del file corrispondente.
   *
   * @see listaAllegtai
   * @param codiceAllegato � il terzo parametro della chiave che mi serve per recuperare
   *  gli allegati collegati a queso XML 
   * @return nessun valore
   */
  public void aggiungiAllegato(String codiceAllegato){
    listaAllegati.add(codiceAllegato);
  }

  /**
   * Testo l'esistenza di un record su una tabella a seconda della funzione chiamata
   *
   * @return vero se esiste falso se non esiste
   */

  private boolean esisteRichiesta(DbOperationSQL dbOpSQL) throws Exception{
    String            queryStm;
    ResultSet         rst;
    
    queryStm =  "SELECT * FROM RICHIESTE WHERE "+
                "CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
                "AREA = :AREA";
    dbOpSQL.setStatement(queryStm);
    dbOpSQL.setParameter(":CODICE_RICHIESTA",codiceRichiesta);
    dbOpSQL.setParameter(":AREA",area);
    dbOpSQL.execute();
    rst = dbOpSQL.getRstSet();
    
    if (rst.next() == false){
     // rst.close();
      return false;
    } else {
      //rst.close();
      return true;
    }
  }
  private boolean esisteXML(DbOperationSQL dbOpSQL, String pRichiesta, String pArea, String pCodice) throws Exception{
    String            queryStm;
    ResultSet         rst;
    
    queryStm =  "SELECT * FROM XML "+
                "WHERE CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
                "AREA = :AREA AND "+
                "CODICE_XML = :CODICE_XML";
    dbOpSQL.setStatement(queryStm);
    
    dbOpSQL.setParameter(":CODICE_RICHIESTA", pRichiesta);
    dbOpSQL.setParameter(":AREA", pArea);
    dbOpSQL.setParameter(":CODICE_XML", pCodice);
    dbOpSQL.execute();
    
    rst = dbOpSQL.getRstSet();
    
    if (rst.next() == false) {
      //rst.close();
      return false;
    } else {
      //rst.close();
      return true;
    }
    
    
  }
  private boolean esisteAllegato(DbOperationSQL dbOpSQL, String pRichiesta, String pArea, String pCodice) throws Exception{
    String            queryStm;
    ResultSet         rst;
    
    queryStm =  "SELECT * FROM ALLEGATI WHERE "+
                "CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
                "AREA = :AREA AND "+
                "CODICE_ALLEGATO = :CODICE_ALLEGATO";

    dbOpSQL.setStatement(queryStm);
    dbOpSQL.setParameter(":CODICE_RICHIESTA", pRichiesta);
    dbOpSQL.setParameter(":AREA", pArea);
    dbOpSQL.setParameter(":CODICE_ALLEGATO", pCodice);
    dbOpSQL.execute();

    rst = dbOpSQL.getRstSet();
    
    if (rst.next() == false){

      //rst.close();
      return false;
    } else {
  
      //rst.close();
      return true;
    }
  }

  /**
   * 
   */
  private boolean esisteAllegatoXML(DbOperationSQL dbOpSQL, String pRichiesta, String pArea, String pCodice,
                String pRichiestaAll, String pAreaAll, String pCodiceAll) throws Exception{
    String            queryStm;
    ResultSet         rst;
    
    queryStm =  "SELECT * FROM ALLEGATI_XML WHERE "+
                "CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
                "AREA = :AREA AND "+
                "CODICE_XML = :CODICE_XML AND "+
                "ALL_CODICE_RICHIESTA = :ALL_CODICE_RICHIESTA AND "+
                "ALL_AREA = :ALL_AREA AND "+
                "ALL_CODICE_ALLEGATO = :ALL_CODICE_ALLEGATO";

    dbOpSQL.setStatement(queryStm);        
    
    dbOpSQL.setParameter(":CODICE_RICHIESTA", pRichiesta);
    dbOpSQL.setParameter(":AREA", pArea);
    dbOpSQL.setParameter(":CODICE_XML", pCodice);
    dbOpSQL.setParameter(":ALL_CODICE_RICHIESTA", pRichiestaAll);
    dbOpSQL.setParameter(":ALL_AREA", pAreaAll);
    dbOpSQL.setParameter(":ALL_CODICE_ALLEGATO", pCodiceAll);
    dbOpSQL.execute();

    rst = dbOpSQL.getRstSet();

    if (rst.next() == false){

     // rst.close();
      return false;
    } else {

      //rst.close();
      return true;
    }
  }

  /**
  * Aggiornamento dell'utente nella tabella delle richieste
  *
  */
/*  private void  aggiornaRichieste(DbOperationSQL dbOpSQL) throws Exception {
    String            insStm;
//    ResultSet         rst;  
    Timestamp         toDay;

    // Inserisco la testata della richiesta
//    toDay = new Date(System.currentTimeMillis());
    toDay =dbOpSQL.getSysdate();
    insStm =  "UPDATE RICHIESTE "+
              "SET DATA_INSERIMENTO = :DATA "+
              "CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
              "AREA = :AREA AND ID_TIPO_PRATICA = :ID_TIPO_PRATICA";

    dbOpSQL.setStatement(insStm);

    dbOpSQL.setParameter(":DATA", toDay);

    dbOpSQL.setParameter(":CODICE_RICHIESTA", codiceRichiesta);
    dbOpSQL.setParameter(":AREA", area);
    dbOpSQL.setParameter(":ID_TIPO_PRATICA", area);
    dbOpSQL.execute();
    dbOpSQL.commit(); //Marika 4/11/2003, anche se non la chiamo pi�, visto che l'user non esiste in richieste
  }*/


  /**
   * Inserimento nuovi record l'esistenza di un record su una tabella a seconda della funzione chiamata
   */
  private void  inserisciRichieste(DbOperationSQL dbOpSQL) throws Exception{
    String     insStm;
//    ResultSet  rst;  
    Date       toDay;

    // Inserisco la testata della richiesta
    toDay = new Date(System.currentTimeMillis());
    insStm = "INSERT INTO RICHIESTE "+
              "( CODICE_RICHIESTA,  AREA,  \"USER\",  DATA_SCADENZA,  DATA_INSERIMENTO) VALUES "+
              "(:CODICE_RICHIESTA, :AREA, :USER, :DATA_SCADENZA, :DATA_INSERIMENTO)";
              
    dbOpSQL.setStatement(insStm);
    dbOpSQL.setParameter(":CODICE_RICHIESTA", codiceRichiesta);
    dbOpSQL.setParameter(":AREA", area);
    dbOpSQL.setParameter(":USER", utente);
    dbOpSQL.setParameter(":DATA_SCADENZA", toDay);
    dbOpSQL.setParameter(":DATA_INSERIMENTO", toDay);     
    dbOpSQL.execute();
    dbOpSQL.commit();//Marika 4/11/2003
  } 

  /**
   * 
   */
  private void  inserisciXML(DbOperationSQL dbOpSQL) throws Exception{
    String            insStm;
//    ResultSet         rst;

    // Inserisco la testata del file XML 
    insStm =  "INSERT INTO XML "+
              "(CODICE_RICHIESTA,   AREA,  CODICE_XML) VALUES "+
              "(:CODICE_RICHIESTA, :AREA ,:CODICE_XML)";
              
    dbOpSQL.setStatement(insStm);
    dbOpSQL.setParameter(":CODICE_RICHIESTA", codiceRichiesta);
    dbOpSQL.setParameter(":AREA", area);
    dbOpSQL.setParameter(":CODICE_XML", codiceXML);
    dbOpSQL.execute();
    dbOpSQL.commit();//Marika 4/11/2003
//    dbOpSQL.close();
  }

  /**
   * 
   */
  private void  inserisciAllegatiXML(DbOperationSQL dbOpSQL, String pCodiceRichiesta, String pArea, String pCodiceXML,
                                     String pAllCodiceRichiesta, String pAllArea, String pAllCodiceAllegato) throws Exception{
    String    insStm;
//    ResultSet rst;

    // Inserisco il collegamento fra testata e XML appena inserito
    insStm = "INSERT INTO ALLEGATI_XML "+
             "(CODICE_RICHIESTA,   AREA,  CODICE_XML,  ALL_CODICE_RICHIESTA,  ALL_AREA,  ALL_CODICE_ALLEGATO) VALUES "+
             "(:CODICE_RICHIESTA, :AREA ,:CODICE_XML, :ALL_CODICE_RICHIESTA, :ALL_AREA, :ALL_CODICE_ALLEGATO)";

    dbOpSQL.setStatement(insStm);        
    dbOpSQL.setParameter(":CODICE_RICHIESTA", pCodiceRichiesta);
    dbOpSQL.setParameter(":AREA", pArea);
    dbOpSQL.setParameter(":CODICE_XML", pCodiceXML);
    dbOpSQL.setParameter(":ALL_CODICE_RICHIESTA", pAllCodiceRichiesta);
    dbOpSQL.setParameter(":ALL_AREA", pAllArea);
    dbOpSQL.setParameter(":ALL_CODICE_ALLEGATO", pAllCodiceAllegato);      
    dbOpSQL.execute();
    dbOpSQL.commit();//Marika 4/11/2003

  }
  
  /**
   * Inserimento XML come Blob nella tabella degli allegati
   */
  private void  inserisciXMLInAllegati(DbOperationSQL dbOpSQL) throws Exception{
    String            insStm;
//    ResultSet         rst;
    
    // Inserisco l'XML come allegato 
   /* insStm =  "INSERT INTO ALLEGATI "+
              "(CODICE_RICHIESTA,   AREA,  CODICE_ALLEGATO,  ALLEGATO) VALUES "+
              "(:CODICE_RICHIESTA, :AREA ,:CODICE_ALLEGATO, :ALLEGATO)";
*/

insStm =  "INSERT INTO ALLEGATI "+
              "(CODICE_RICHIESTA,   AREA,  CODICE_ALLEGATO) VALUES "+
              "(:CODICE_RICHIESTA, :AREA ,:CODICE_ALLEGATO)";

    dbOpSQL.setStatement(insStm);
    
    dbOpSQL.setParameter(":CODICE_RICHIESTA", codiceRichiesta);
    dbOpSQL.setParameter(":AREA", area);
    dbOpSQL.setParameter(":CODICE_ALLEGATO", codiceXML);
dbOpSQL.execute();
dbOpSQL.commit();//Marika 4/11/2003

//Util.writeLog("ho creto la riga per l'allegato nella tabella allegati","");

    // Scrivo un file temporaneo con il mio albero in formato XML
    String            nFileTemp = fInput();
    FileInputStream   fIn = new FileInputStream(nFileTemp);
    File              fileDesc = new File(nFileTemp);

insStm =  "UPDATE ALLEGATI SET ALLEGATO = :ALLEGATO WHERE (AREA ='"+area+
              "'AND CODICE_RICHIESTA='"+codiceRichiesta+"'AND CODICE_ALLEGATO='"+codiceXML+"')";
dbOpSQL.setStatement(insStm);     

    //pstmt.setBinaryStream(4, (InputStream)fIn, (int)fileDesc.length()); 
    dbOpSQL.setParameter(":ALLEGATO",(InputStream)fIn, 0);
//dbOpSQL.setBinaryStream(":ALLEGATO",(InputStream)fIn, fIn.available());

    // Fine uso file temporanei
    dbOpSQL.execute();

    dbOpSQL.commit();//Marika 4/11/2003

    // Chiudo il file di input e lo cancello
    fIn.close();
    fileDesc.delete();
  }
  /**
  * Aggiornamento del Blob XML nella tabella degli allegati
  */
  private void  aggiornaXMLInAllegati(DbOperationSQL dbOpSQL) throws Exception{
    String    insStm;
//    ResultSet rst;
    
    // Inserisco l'XML come allegato 
    insStm =  "UPDATE ALLEGATI "+
              "SET ALLEGATO = :ALLEGATO WHERE "+
              "CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
              "AREA = :AREA AND "+
              "CODICE_ALLEGATO = :CODICE_ALLEGATO";
              
    dbOpSQL.setStatement(insStm);
    
    dbOpSQL.setParameter(":CODICE_RICHIESTA", codiceRichiesta);
    dbOpSQL.setParameter(":AREA", area);
    dbOpSQL.setParameter(":CODICE_ALLEGATO", codiceXML);

    // Scrivo un file temporaneo con il mio albero in formato XML
    String          nFileTemp = fInput();
    FileInputStream fIn = new FileInputStream(nFileTemp);
    File            fileDesc = new File(nFileTemp);
      
    //pstmt.setBinaryStream(1, (InputStream)fIn, (int)fileDesc.length()); 
    dbOpSQL.setParameter(":ALLEGATO", (InputStream)fIn, 0); 
    
    // Fine uso file temporanei
    dbOpSQL.execute();
    dbOpSQL.commit();//Marika 4/11/2003
    // Chiudo il file di input e lo cancello
    fIn.close();
    fileDesc.delete();
  }

  /**
  * Scrive la attuale struttura XML sul DB.
  * ATTENZIONE: la leggiDaDB legge la struttura della Pratica dal repositoryTemp e non c'entra nulla
  * con la struttura che invece questa function va a scrivere.
  * Questo metodo � la parte comune a tutti gli XML e per come � strutturata la base dati dovr�
  * svolgere le seguanti operazioni:
  *   1) scrivere un record di testata relativo alla richiesta, all'utente e alle date di inserimento
  *   2) scrivere il record di testata dell'XML
  *   3) scrivere l'XML come un BLOB nella tabella degli allegati
  *   4) scrivere il record di collegamento fra la testata e questo allegato (che � se stesso in pratica).
  *   5) scrive i record id collegamento fra il documento e tutti i suoi allegati.
  * Ogni oggetto figlio si preoccuper� di estendere il metodo per scrivere le parti mancanti.
  *
  * @exception le exception che eventualmente generano i metodi chiamati
  */

  public void scriviXMLSuDb(InfoConnessione infoConnesione) throws Exception{
    String nomeAllegato;
    int    i;
    DbOperationSQL dbOpSQL = new DbOperationSQL(infoConnesione.getAlias(), 
                                   infoConnesione.getDsn(), 
                                   infoConnesione.getUser(), 
                                   infoConnesione.getPasswd());

    try {
      if (esisteRichiesta(dbOpSQL) == false){
        // E' il primo file relativo a questa richiesta quindi devo inserire 
        // anche il record relativo alla richiesta stessa
        inserisciRichieste(dbOpSQL);
      } else {
        // Aggiorno il nome dell'utente (unico dato non in chiave che possa aver senso aggiornare)
        //aggiornaRichieste(dbOpSQL);
      }
      // A questo punto in richieste esiste per forza il giusto record e devo solo scrivere 
      // il file XML: scrivero' o aggiorner' i tre record nelle tre tabelle diverse e nella 
      // tabella centrale scrivero' una riga per ogni allegato presente.

      if (esisteXML(dbOpSQL, codiceRichiesta,  area, codiceXML) == false){
        // Devo inserire nella tabella la testata dell XML che ancora non esiste

        inserisciXML(dbOpSQL);
      }

      if (esisteAllegato(dbOpSQL, codiceRichiesta,  area, codiceXML) == false){
        // Devo inserire il file XML allegandolo come Blob nella tabella degli allegati

        inserisciXMLInAllegati(dbOpSQL);
      } else {
        // Devo aggiornare il file XML allegandolo come Blob nella tabella degli allegati

        aggiornaXMLInAllegati(dbOpSQL);
      }

      if (esisteAllegatoXML(dbOpSQL, codiceRichiesta, area, codiceXML, codiceRichiesta, area, codiceXML) == false){
        // Lo inserisco

        inserisciAllegatiXML(dbOpSQL, codiceRichiesta, area, codiceXML, codiceRichiesta, area, codiceXML);
      }

      //***** Inserisco i collegamento fra testata e XML e i suoi allegati ******     
      // la fase seguente potrebbe essere ottimizzata utlizzandouna funzione ad Hoc 
      // che non reinizializzi ogni volta lo Statement e i suoi parametri

    if (listaAllegati.size()>0){
      for (i=0; i<listaAllegati.size(); i++){
        nomeAllegato = (String)listaAllegati.get(i);  

        if (esisteAllegatoXML(dbOpSQL, codiceRichiesta, area, codiceXML, codiceRichiesta, area, nomeAllegato) == false){
           // Lo inserisco
           inserisciAllegatiXML(dbOpSQL, codiceRichiesta, area, codiceXML, codiceRichiesta, area, nomeAllegato);
        }
      }
}
    } catch (Exception ex) {
      ex.printStackTrace();
      dbOpSQL.close();
    }
    dbOpSQL.close();
  }
  
  /**
   * ABSTRACT METHODES
   */
  //  abstract public boolean valido();
}
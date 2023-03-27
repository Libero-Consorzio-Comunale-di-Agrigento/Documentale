package xmlpack;

import java.util.ArrayList;
import java.sql.*;
//import it.finmatica.modulistica.parametri.*;
import it.finmatica.jfc.dbUtil.*;

/**
 * Rappresenta i dati del singolo modello.
 * Contiene la lista dei dati (campi in maschera) del modello stesso.
 */
public class ModelloXML {
  public final static int MAX_LENGTH_VALORE_S = 1000000;
  
  protected   ArrayList   listaDatiXML;
  protected   String      area;
  protected   String      codice;
  protected   String      autore;
  protected   int         progressivo;

//  protected   DbOperationSQL dbOpSQL;
  protected  InfoConnessione infoConnesione;
  
  /**
  * Castruttore
  */
  public ModelloXML(InfoConnessione infoConnesione, String pArea, String pCodice, int pProgressivo, String pAutore) throws Exception {
//    try {
//      dbOpSQL = new DbOperationSQL(dbop);
//    } catch (Exception e) {
//      Util.writeLog("ModelloXML::ModelloXML()","Attenzione! Impossibile ottenere una nuova connessione al db.");
//      throw new Exception("ModelloXML():Impossibile ottenere una nuova connessione al db");
//    }
//    this.dbOpSQL = dbop;
    this.infoConnesione = infoConnesione;
    
    area = pArea;
    codice = pCodice;
    progressivo = pProgressivo;
    autore = pAutore;
    listaDatiXML = new ArrayList();
  }

  /**
   * Il modello è lo stesso se coincidono area codice revisione e autore con quello passato come
   * parametro. L'eventuale filtro sul numero della richiesta deve essere fatto a monte.
   */
  public boolean stessoModello(String pArea, String pCodice, int pProgressivo, String pAutore){
    boolean   retVal;
    
    retVal = area.equals(pArea) && codice.equals(pCodice) && autore.equals(pAutore) && (progressivo == pProgressivo);
    return(retVal);
  }

  /**
  * Carica da database i dati legati al modello costruendo la lista dei datiXML appropriata.
  * @param il codice della richiesta a cui ci stiamo riferendo.
  */
  public void caricaDaDb(String codiceRichiesta) {
    ResultSet         rst;
    String            queryStm;
    DatoXML           dt = null;
    String            pNomeCampo, 
                      pValore;

    try {
      DbOperationSQL dbOpSQL = new DbOperationSQL(infoConnesione.getAlias(), 
                                               infoConnesione.getDsn(), 
                                               infoConnesione.getUser(), 
                                               infoConnesione.getPasswd());
                                               
      queryStm =  "SELECT * FROM PRE_INOLTRO WHERE "+
                  "CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
                  "AREA = :AREA AND "+
                  "CODICE_MODELLO = :CODICE_MODELLO AND "+
                  "PROGRESSIVO = :PROGRESSIVO";
                  
      dbOpSQL.setStatement(queryStm);

//Util.writeLog("ModelloXML::caricaDaDb()","codiceRichiesta = "+codiceRichiesta);      
//Util.writeLog("ModelloXML::caricaDaDb()","area = "+area);      
//Util.writeLog("ModelloXML::caricaDaDb()","codice = "+codice);      
//Util.writeLog("ModelloXML::caricaDaDb()","revisione = "+revisione);      
//Util.writeLog("ModelloXML::caricaDaDb()","progressivo = "+progressivo);      
     

      dbOpSQL.setParameter(":CODICE_RICHIESTA", codiceRichiesta);
      dbOpSQL.setParameter(":AREA", area);
      dbOpSQL.setParameter(":CODICE_MODELLO", codice);
      dbOpSQL.setParameter(":PROGRESSIVO", progressivo);
      
      dbOpSQL.execute(); // Rieseguo la query che dovrebbe tornare i valori del chiamante 
                         // realtivi al modello letto
      rst = dbOpSQL.getRstSet();
      
      if (rst.next() == false) {
        System.out.println("ModelloXML::caricaDaDB - Impossibile trovare dati relativi al modello: "+codiceRichiesta+"/"+area+
                          "/"+codice+"/"+Integer.toString(progressivo));
                          
        throw new Exception("Impossibile trovare dati relativi al modello: "+codiceRichiesta+"/"+area+
                          "/"+codice+"/"+Integer.toString(progressivo));
      }
      
      do {
        // Creiamo i dati
        pNomeCampo = rst.getString("DATO");
        Clob clobValore = rst.getClob("VALORE");
        long clobLen = clobValore.length();
        pValore = null;
      
        if (clobLen < MAX_LENGTH_VALORE_S) {
          int i_clobLen = (int)clobLen;
          pValore = clobValore.getSubString(1, i_clobLen);
//          Util.writeLog("ModelloXML::caricaDaDb()","valore=["+pValore+"]");
        } else {
          System.out.println("ModelloXML::caricaDaDb() - Attenzione! Si è verificato un errore. Il campo supera i "+MAX_LENGTH_VALORE_S+" caratteri.");
        }
      
        dt = new DatoXML(pNomeCampo, pValore);
        listaDatiXML.add(dt);

      } while (rst.next());
      dbOpSQL.close();
     
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  /**
   * Funzione per la lettura della lista dei dati
   * 
   * @return l'ArrayList che mi contiene i DatiXML 
   */

   public ArrayList getDati(){
    return (listaDatiXML);
   }

   /**
    * Metodo di lettura pubblico
    *
    * @return valore della area
    */
   public String getArea(){
    return area;
   };   
   /**
    * Metodo di lettura pubblico
    *
    * @return valore della codice
    */
   public String getCodice(){
    return codice;
   }

   /**
    * Metodo di lettura pubblico
    *
    * @return valore della autore
    */
   public String getAutore(){
    return autore;
   };

   /**
    * Metodo di lettura pubblico
    *
    * @return valore della progressivo
    */
   public String getProgressivo(){
    return Integer.toString(progressivo);
   };
}
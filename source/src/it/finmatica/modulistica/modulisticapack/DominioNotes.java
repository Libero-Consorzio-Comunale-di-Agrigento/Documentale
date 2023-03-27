package it.finmatica.modulistica.modulisticapack;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import java.util.*;
import java.net.URL;
import it.finmatica.jfc.io.*;
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;
 
/**
 * 
 */
public class DominioNotes extends Dominio {
  private static    Logger      logger = Logger.getLogger(DominioNotes.class);

  /**
   * 
   */
  public DominioNotes(String pArea, String pDominio, String pCm, String pTipo, String pOrdinamento, String pUrl, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) throws Exception {
    super(pArea, pDominio, pCm, pTipo, pOrdinamento,dbOpEsterna);
    connessione = pUrl;
    caricaValori(pRequest);
  }


  /**
   * Carica nelle liste codici e valori le coppie da presentare come scelte possibil 
   * all'utente. Riceve uno stream di INPUT da un agente notes a cui si connette
   * attraverso l'URL specificato nel campo connessione.
   * IL formato dello stream che si aspetta in ingresso � quello standard:
   * [DMNOTES_CODICEBEGIN codice DMNOTES_END DMNOTES_VALOREBEGIN valore DMNOTES_VALOREEND]
   * senza gli spazi e le quadre messi solo per chiarezza.
   * 
   * @author Adelmo
   * @param pRequest stringa di request ricevuta dal servlet chiamante
   * @see Dominio.scriviValoriDaStream(String stringStream)
   */
  protected void caricaValori(AbstractParser mp) {
    //Debug Tempo
    long ptime = stampaTempo("DominioNotes::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    LetturaScritturaFileURL domino;
    URL urlDomino;
    java.io.InputStream dsn;
//    int ci, cf, vi, vf;
    int c;
    char lippa;
    String  istruzione;
    String tuttolippa = "";

    try {

      istruzione = mp.bindingDeiParametri(connessione);
      if (istruzione == null){
        // Ritorno senza riempire alcun valore poich� si �
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        throw new Exception("Area: "+area+" Dominio: "+Dominio+" Istruzione nulla");  // *** exit point ***
      }
      // Verifico la presenza del parametro URL_SERVER_DOMINO per sostiuirlo con
      // quello effettivo.
      int   startPos;
      startPos = istruzione.indexOf("URL_SERVER_DOMINO");  // 17 caratteri
      if (startPos > -1){
        istruzione = istruzione.substring(0, startPos)+Parametri.URL_SERVER_DOMINO
                    + istruzione.substring(startPos+17, istruzione.length());
      }      
      urlDomino = new URL(istruzione);
  
      domino = new LetturaScritturaFileURL(urlDomino);
      dsn = domino.leggiFile();

      while ((c = dsn.read()) != -1) {
        lippa = (char) c;
        tuttolippa = tuttolippa + lippa;
      }
      scriviValoriDaStream(tuttolippa);
    } catch(Exception e) {
      loggerError("DOMINIONOTES - Area: "+area+" Dominio: "+Dominio+" Errore: "+e.getMessage(),e);
    }        
    //Debug Tempo
    stampaTempo("DominioNotes::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  protected void caricaValori(HttpServletRequest pRequest) {
    //Debug Tempo
    long ptime = stampaTempo("DominioNotes::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    LetturaScritturaFileURL domino;
    URL urlDomino;
    java.io.InputStream dsn;
//    int ci, cf, vi, vf;
    int c;
    char lippa;
    String  istruzione;
    String tuttolippa = "";

    try {

      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","N");
      mp.setExtraKeys(pmp);
      istruzione = mp.bindingDeiParametri(connessione);
//      istruzione = bindingDinamico(pRequest, connessione, false);
      if (istruzione == null){
        // Ritorno senza riempire alcun valore poich� si �
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        throw new Exception("Area: "+area+" Dominio: "+Dominio+" Istruzione nulla");  // *** exit point ***
      }
      // Verifico la presenza del parametro URL_SERVER_DOMINO per sostiuirlo con
      // quello effettivo.
      int   startPos;
      startPos = istruzione.indexOf("URL_SERVER_DOMINO");  // 17 caratteri
      if (startPos > -1){
        istruzione = istruzione.substring(0, startPos)+Parametri.URL_SERVER_DOMINO
                    + istruzione.substring(startPos+17, istruzione.length());
      }      
      urlDomino = new URL(istruzione);
  
      domino = new LetturaScritturaFileURL(urlDomino);
      dsn = domino.leggiFile();

      while ((c = dsn.read()) != -1) {
        lippa = (char) c;
        tuttolippa = tuttolippa + lippa;
      }
      scriviValoriDaStream(tuttolippa);
    } catch(Exception e) {
      loggerError("DOMINIONOTES - Area: "+area+" Dominio: "+Dominio+" Errore: "+e.getMessage(),e);
//      e.printStackTrace();
    }        
    //Debug Tempo
    stampaTempo("DominioNotes::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }

}
package it.finmatica.modulistica.domini;

import java.util.*;
import java.sql.*;
import it.finmatica.instantads.WrapParser;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;
import it.finmatica.modulistica.parametri.Parametri;

public class DominioJava extends Dominio {
  private static Logger logger = Logger.getLogger(DominioJava.class);

  public DominioJava(Connection pConn, String pArea, String pDominio, String pTipo, String pOrdinamento, String pIstruzione,AbstractParser pAbPar) throws Exception {
    super(pConn, pArea, pDominio, pTipo, pOrdinamento);
    istruzione  = pIstruzione;
    caricaValori(pAbPar);
  
  }

 /**
  * Viene eseguita la Stringa Java che ritorna una sequenza
  * di coppie che mi rappresenteranno la classica accoppiata CODICE VALORE.
  * 
 * @author  Marco Bonforte
  * @param  mp AbstractParser.
  */
  protected void caricaValori(AbstractParser mp) {
    String              javaStm = null;
    String              strStream = null;

    try {
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","J");
      mp.setExtraKeys(pmp);
      javaStm = mp.bindingDeiParametri(istruzione);
      if (javaStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        return;  // *** exit point ***
      }

      try{
        WrapParser wp = new WrapParser(javaStm);
        strStream = wp.go();
      } catch (Exception ijEx) {
        loggerError("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore Java: "+ijEx.toString(),ijEx);
      }
      scriviValoriDaStream(strStream);
    } catch (Exception e) {
      loggerError("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore: "+e.getMessage(),e);
    } 
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
package it.finmatica.modulistica.modulisticapack;
 
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
import java.util.*;
//import instantj.expression.Expression;
import it.finmatica.instantads.WrapParser;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;

public class DominioJava extends Dominio {
  private static Logger logger = Logger.getLogger(DominioJava.class);

  public DominioJava(String pArea, String pDominio, String pCm, String pTipo, String pOrdinamento, String pIstruzione,HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) throws Exception {
    super(pArea, pDominio, pCm, pTipo, pOrdinamento,dbOpEsterna);
    istruzione  = pIstruzione;
    caricaValori(pRequest);
  
  }

  protected void caricaValori(HttpServletRequest pRequest) {
    caricaValori(pRequest,null);
  }
      
 /**
  * Viene eseguita la Stringa Java che ritorna una sequenza
  * di coppie che mi rappresenteranno la classica accoppiata CODICE VALORE.
  * 
 * @author  Marco Bonforte
  * @param pRequest stringa di request ricevuta dal servlet chiamante.
  */
  protected void caricaValori(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("DominioJava::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    String              javaStm = null;
    String              strStream = null;

    try {
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","J");
      mp.setExtraKeys(pmp);
      javaStm = mp.bindingDeiParametri(istruzione);
//      javaStm = bindingDinamico(pRequest, istruzione, false);
      if (javaStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioJava::caricaValori - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }

      try{
//        Expression myEx = new Expression(javaStm);
//        strStream = (String)myEx.getInstance().evaluate();
//      } catch (instantj.expression.EvaluationFailedException ijEx) { 
        WrapParser wp = new WrapParser(javaStm);
        strStream = wp.go();
      } catch (Exception ijEx) {
        loggerError("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore Java: "+ijEx.toString(),ijEx);
      }
      scriviValoriDaStream(strStream);
    } catch (Exception e) {
      loggerError("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore: "+e.getMessage(),e);
//      e.printStackTrace();
    } 
    //Debug Tempo
    stampaTempo("DominioJava::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  protected void caricaValori(AbstractParser mp) {
    //Debug Tempo
    long ptime = stampaTempo("DominioJava::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    String              javaStm = null;
    String              strStream = null;

    try {
//      ModulisticaParser mp = new ModulisticaParser(pRequest);
      javaStm = mp.bindingDeiParametri(istruzione);
      if (javaStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioJava::caricaValori - Fine",area,Dominio,ptime);
        //Debug Tempo
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
    //Debug Tempo
    stampaTempo("DominioJava::caricaValori - FIne",area,Dominio,ptime);
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
import mailpack.InoltroMail;

import xmlpack.*;
//import it.finmatica.jfc.dbUtil.*;
//import it.finmatica.modulistica.parametri.Parametri;
//import java.sql.*;

public class InoltroMailADSXML extends InoltroMail {

  private String    codRichiesta;
  private String    area;
//  private String    codTipoPratica;
//  private String    codModello;
//  private String    revModello;
//  private String    idOp;
  private String    pUtente;
  private String    pAllegati;
//  private Parametri Param;
  private InfoConnessione   infoConnessione;
//  private Timestamp today;

  public InoltroMailADSXML() {
    super();
  }

  /**
   * 
   */
  public void parametriRichiesta(String idop, String cr, 
        String ar, String cm, String ctp, 
        String utente, String allegati, InfoConnessione pm) {

//    idOp = idop;
    codRichiesta = cr;
    area = ar;
//    codTipoPratica = ctp;
    infoConnessione = pm;
    pAllegati = allegati;
    pUtente = utente;
  }

  /**
   * 
   */
  public boolean inoltra() {
    PraticaXML        praticaXML;

/*    infoConnessione = new InfoConnessione(Param.ALIAS, 
                                          Param.SPORTELLO_DSN, 
                                          Param.USER, 
                                          Param.PASSWD);*/
    praticaXML = new PraticaXML(infoConnessione,
                                codRichiesta,
                                area,
                                pUtente,
                                "ADSXML",
                                pAllegati);
                                     
    praticaXML.caricaDaDB(); // CARICATI I MODELLI IN LISTA MODELLI
    praticaXML.generaXML();
    try {
      praticaXML.scriviXMLSuDb(infoConnessione);
    } catch (Exception ex) {
      errorMessage = "<div id='_gdm_error_small' style='display: block'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a></div>";
      errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a>";
      StackTraceElement[] st1 = ex.getStackTrace();
      for (int i = 0;i < st1.length; i++) {
        errorMessage += "<br/>"+st1[i].toString();
      }
      errorMessage += "</div>";
      return false;
    }
    if (!doInoltro(praticaXML)) {
      return false;
    }
    return true;
  }
}
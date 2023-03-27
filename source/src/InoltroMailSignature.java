import mailpack.InoltroMail;

import xmlpack.*;
import it.finmatica.jfc.dbUtil.*;
//import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;

public class InoltroMailSignature extends InoltroMail {

private String    codRichiesta;
private String    area;
private String    codTipoPratica;
//private String    codModello;
//private String    revModello;
//private String    idOp;
//private String    pUtente;
private String    pAllegati;
private InfoConnessione   infoConnessione;
//private Parametri Param;
private Timestamp today;

  public InoltroMailSignature() {
    super();
  }

  /**
   * 
   */
  public void parametriRichiesta(String idop, String cr, 
        String ar, String cm, String id_doc, String ctp, 
        String utente, String allegati, InfoConnessione pm) {

//    idOp = idop;
    codRichiesta = cr;
    area = ar;
    codTipoPratica = ctp;
    infoConnessione = pm;
    pAllegati = allegati;
//    pUtente = utente;
  }

  /**
   * 
   */
  public boolean inoltra() {
    DbOperationSQL    dbOpSQL = null;
    DbOperationSQL    dbOpSQLProt = null;
    ResultSet         rst, 
                      rstProt;
    String            query = null,
                      queryProt = null;
//    InfoConnessione   infoConnessione;
    String            pOggetto,
                      pTipoDoc,
                      pMovimento,
                      pClassificazione,
                      pUnitaProt,
                      pUtente,
                      pApplicativo,
                      pDocPrinc,
                      pSmistamento,
                      pMittente;

/*    infoConnessione = new InfoConnessione(Param.ALIAS, 
                                          Param.SPORTELLO_DSN, 
                                          Param.USER, 
                                          Param.PASSWD);*/
    try {
//      SessioneDb.getInstance().addAlias(infoConnessione.getAlias(), infoConnessione.getDsn());
      dbOpSQL = new DbOperationSQL(infoConnessione.getAlias(), 
                                    infoConnessione.getDsn(), 
                                    infoConnessione.getUser(), 
                                    infoConnessione.getPasswd());
/*      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
      dbOpSQL = new DbOperationSQL(Param.ALIAS, Param.SPORTELLO_DSN, Param.USER, Param.PASSWD);*/
    } catch (Exception ex) {
      return false;
    }
      
    if (codTipoPratica == null) {
      // Controlliamo se esiste un tipo pratica di default per l'area presa in considerazione
      query = "SELECT CODICE_TIPO_PRATICA "+
              "  FROM TIPI_PRATICHE " +
              " WHERE AREA = '" + area +"' "+
              "   AND IS_DEFAULT = '1' ";
      try {
        dbOpSQL.setStatement(query);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        if (!rst.next()) {
            dbOpSQL.close();
            return false;
        }
        codTipoPratica = rst.getString("CODICE_TIPO_PRATICA");
        dbOpSQL.close();
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
    } 
    queryProt = "SELECT * "+
                "FROM TIPI_PRATICHE "+
                "WHERE CODICE_TIPO_PRATICA ='"+codTipoPratica+"' ";
    try {
      dbOpSQLProt = new DbOperationSQL(infoConnessione.getAlias(), infoConnessione.getDsn(), infoConnessione.getUser(), infoConnessione.getPasswd());
      today = dbOpSQLProt.getSysdate();
      dbOpSQLProt.setStatement(queryProt);
      dbOpSQLProt.execute();
      rstProt = dbOpSQLProt.getRstSet();
      if (!rstProt.next()) {
        dbOpSQLProt.close();
        return false;
      }

      pOggetto = rstProt.getString("OGGETTO");
      if (pOggetto == null)
        pOggetto = "";
      pTipoDoc = rstProt.getString("TIPO_DOCUMENTO");
      if (pTipoDoc == null)
        pTipoDoc = "";
      pMovimento = rstProt.getString("MOVIMENTO");
      if (pMovimento == null)
        pMovimento = "";
      pClassificazione = rstProt.getString("CLASSIFICAZIONE");
      if (pClassificazione == null)
        pClassificazione = "";
      pUnitaProt = rstProt.getString("UNITA_PROTOCOLLANTE");
      if (pUnitaProt == null)
        pUnitaProt = "";
      pUtente = rstProt.getString("UTENTE");
      if (pUtente == null)
        pUtente = "";
      pApplicativo = rstProt.getString("APPLICATIVO");
      if (pApplicativo == null)
        pApplicativo = "";
      pDocPrinc = rstProt.getString("DOCUMENTO_PRINCIPALE");
      if (pDocPrinc == null)
        pDocPrinc = "";
      pSmistamento = rstProt.getString("SMISTAMENTO");
      if (pSmistamento == null)
        pSmistamento = "";
      pMittente = rstProt.getString("MITTENTE");
      if (pMittente == null)
        pMittente = "";

      dbOpSQLProt.close();

      ProtocolloXML protocolloXML = new ProtocolloXML(
                             infoConnessione,
                             codRichiesta,
                             area,
                             today,
                             pOggetto,
                             pTipoDoc,
                             pMovimento,
                             pSmistamento,
                             pMittente,
                             pUnitaProt,
                             pApplicativo,
                             pUtente,
                             pClassificazione,
                             pDocPrinc,
                             "ProtocolloXML",
                             pAllegati);
      protocolloXML.generaXML();
      protocolloXML.scriviXMLSuDb(infoConnessione);
      if (!doInoltro(protocolloXML)) {
        return false;
      }

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

    return true;
  }
}
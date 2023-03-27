package it.finmatica.modulistica.domini;

import java.sql.*;
//import it.finmatica.jfc.dbUtil.*;
import it.finmatica.textparser.AbstractParser;

/**
 * 
 */
public class DominioStandard extends Dominio {

  /**
   * 
   */
  public DominioStandard(Connection pConn, String pArea, String pDominio, String pTipo, String pOrdinamento, AbstractParser pAbPar) throws Exception {
    super(pConn, pArea, pDominio, pTipo, pOrdinamento);

    // Costruisco l'istruzione standard di interrogazione valori
    istruzione = "SELECT CODICE, VALORE FROM VALORI_DOMINIO";
    istruzione = istruzione + " WHERE AREA = '" + pArea + "'";
    istruzione = istruzione + " AND DOMINIO = '" + pDominio + "'";
    
    caricaValori(pAbPar);
  }
}

/***********************************************************************
 * Module:  DominioStandard.java
 * Author:  Nicola
 * Created: lunedì 30 aprile 2002 11.47.51
 * Purpose: Defines the Class DominioStandard
 ***********************************************************************/

package it.finmatica.modulistica.modulisticapack;
 
//import java.sql.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.*;
//import it.finmatica.jfc.dbUtil.*;

/**
 * 
 */
public class DominioStandard extends Dominio {



  /**
   * 
   */
  public DominioStandard(String pArea, String pCm, String pDominio, String pTipo, String pOrdinamento, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) throws Exception {
    super(pArea, pDominio, pCm, pTipo, pOrdinamento,dbOpEsterna);

    // Costruisco l'istruzione standard di interrogazione valori
    istruzione = "SELECT CODICE, VALORE FROM VALORI_DOMINIO";
    istruzione = istruzione + " WHERE AREA = '" + pArea + "'";
    istruzione = istruzione + " AND DOMINIO = '" + pDominio + "'";
    istruzione = istruzione + " AND CODICE_MODELLO = '" + pCm + "'";
    
    caricaValori(pRequest,dbOpEsterna);
  }
}

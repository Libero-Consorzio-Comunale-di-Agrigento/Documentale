package it.finmatica.modulistica.modulisticapack;

import javax.servlet.http.HttpServletRequest;
//import java.util.*;
import java.sql.*;
//import java.util.Calendar;
//import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
 
public class FormulaModello {
  protected String queryHtmlIn    = "";
  protected String corpo          = "";
  protected String conDRIVER      = "";
  protected String conCONNESIONE  = "";
  protected String conUTENTE      = "";
  protected String conPASSWD      = "";
  protected String codMod         = "";

  public FormulaModello(HttpServletRequest pRequest, String pArea, String pModello,IDbOperationSQL dbOp) throws Exception {
    ResultSet       rst     = null;
    String          query   = "";

    byte[] b2 = new byte[1]; 
    b2[0] = 13;
    String ch = new String(b2);

    codMod      = pModello;
    queryHtmlIn = pRequest.getQueryString();
    try {
      query = "SELECT L.CORPO, L.DRIVER, L.CONNESSIONE, L.UTENTE, L.PASSWD, L.DSN "+
              "FROM MODELLI M, LIBRERIA_CONTROLLI L "+
              "WHERE M.AREA = :AREA AND "+
              "M.CODICE_MODELLO = :CODICE_MODELLO AND "+
              "M.REINDIRIZZAMENTO = L.CONTROLLO AND "+
              "M.AREA = L.AREA AND "+
              "L.TIPO = 'P' ";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",pArea);
      dbOp.setParameter(":CODICE_MODELLO",pModello);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        corpo = rst.getString(1);
        corpo = corpo.replaceAll(ch," ");
        corpo = corpo.replaceAll("\n"," ");
        conDRIVER = rst.getString(2);
        conCONNESIONE = rst.getString(3);
        conUTENTE = rst.getString(4);
        conPASSWD = rst.getString(5);
        if (conDRIVER == null) {
          conDRIVER = "";
        }

        String sDsn = rst.getString(6);
        if (sDsn == null) {
          sDsn = "";
        }
        if (sDsn.length() != 0) {
          Connessione cn = new Connessione(dbOp,sDsn);
          conDRIVER      = cn.getDriver();
          conCONNESIONE = cn.getConnessione();
          conUTENTE        = cn.getUtente();
          conPASSWD      = cn.getPassword();
        }


        if (conDRIVER.length() != 0) {
          String compConn = completaConnessione(conCONNESIONE);
          ConnessioneParser cp = new ConnessioneParser();
          String connessione = cp.bindingDeiParametri(compConn);
          if (connessione == null){
            connessione = conCONNESIONE;
          }
          conCONNESIONE = connessione;
        } else {
          conDRIVER = Parametri.ALIAS;
          conCONNESIONE = Parametri.SPORTELLO_DSN;
          conUTENTE = Parametri.USER;
          conPASSWD = Parametri.PASSWD;
        }
      }
    } catch (Exception e){
      throw new Exception (e.toString());
    }

  }

  /**
   * 
   */
  public String nuovoUrl() throws Exception {
    IDbOperationSQL  dbOp    = null;
    String          result  = null;
    ResultSet       rst = null;
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(conDRIVER,conCONNESIONE,conUTENTE,conPASSWD);

      dbOp.setStatement(corpo);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        result = rst.getString(1);
      }
      
      if (result == null) {
        result = "";
      }
    } catch (Exception e){

      throw new Exception (e.toString());
    }
    finally {
      free(dbOp);
    }
    if (result.length() == 0) {
      return "";
    } else {
      return queryHtmlIn.replaceAll("cm="+codMod,"cm="+result);
    }
    
  }

  /**
   * 
   */
  public String getCorpo() {
    return corpo;
  }

  /**
   * 
   */
  public void settaCorpo(String pCorpo) {
    if (pCorpo == null) {
      corpo = "";
    } else {
      corpo = pCorpo;
    }
  }

  /**
   * 
   */
  protected String completaConnessione(String connessione) {
     String connessioneParam = connessione;
     String pCodice = null;
     String retval = null;

     int h = 0;
     int s = 0;

     h = connessioneParam.indexOf(":HOST_DOMINIO");
     if (h > -1) {
       pCodice = connessioneParam.substring(h+1,h+15);
       retval = Parametri.getParametriDomini(pCodice);
       connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
     }
     s = connessioneParam.indexOf(":SID_DOMINIO");
     if (s > -1) {
       pCodice = connessioneParam.substring(s+1,s+14);
       retval = Parametri.getParametriDomini(pCodice);
       connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
     }
     
     return connessioneParam;
     
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }

}
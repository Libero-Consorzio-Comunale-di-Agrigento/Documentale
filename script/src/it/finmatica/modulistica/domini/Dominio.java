package it.finmatica.modulistica.domini;

import java.util.*;
import java.sql.*;
//import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
//import it.finmatica.jfc.bcUtil.*;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;
import it.finmatica.modulistica.parametri.Parametri;


/**
 * Classe Dominio
 *
 * @author Marco Bonforte
 * @version 1.0
 */
public abstract class Dominio {
  public final static int MAXLEN_ISTRUZIONE = 20000;  
                                                      // Numero massimo di caratteri ammessi per un'istruzione
  
  private           Connection  pConn;
  protected         String      area;
  protected         String      Dominio;
  protected         boolean     isDominioDiArea;
  protected         boolean     isDominioDelModello;
  protected         boolean     isDominioFormulaModello;
  protected         String      tipo;
  protected         String      descrizione;
  protected         String      driver;
  protected         String      connessione;
  protected         String      user;
  protected         String      passwd;
  protected         String      istruzione;
  protected         boolean     parametrico;
  protected         List<String>  codici;
  protected         List<String>        valori;
  protected  static Properties  dizionario = null;
  protected         String      textToParsing;
  protected         String      ordinamento = null;
  private static    Logger      logger = Logger.getLogger(Dominio.class);

  /**
   * Costruttore
   */
  public Dominio(Connection pConn, String pArea, String pDominio, String pTipo, String pOrdinamento) throws Exception {
    area            = pArea;
    Dominio = pDominio;
    tipo            = pTipo;
    ordinamento     = pOrdinamento;
    isDominioDiArea = false;
    descrizione     = "";
    istruzione      = "";
    connessione     = "";
    codici          = new ArrayList<String>();
    valori          = new ArrayList<String>();
    parametrico     = false; // Di default il Dominio non è parametrico
    this.pConn  = pConn;
    if (Parametri.PARAM_CARICATI == 0) {
    	Parametri.leggiParametriConnection(pConn);
    }

//    initDizionario();
  }

  /**
   * La funzione suppone di ricevere i valori da scrivere in una stringa
   *
   * @author Marco Bonforte
   * @param stringStream  stringa di input da interpretare per l'estrazione delle
   *                      coppie CODICE VALORE.  IL formato della stringa che si
   *                      aspetta in ingresso è:
   *                      [DMNOTES_CODICEBEGIN codice DMNOTES_END DMNOTES_VALOREBEGIN valore DMNOTES_VALOREEND]
   *                      senza gli spazi e le quadre messi solo per chiarezza.
   */
  protected void scriviValoriDaStream(String stringStream) {
    int ci, cf, vi, vf, nCaratteri = 0;
    String sCodice = null;
    String sValore = null;

    if (stringStream == null) {
      codici.clear();
      valori.clear();
      return;
    }
    if (stringStream.equalsIgnoreCase("")) {
      codici.clear();
      valori.clear();
      return;
    }
    ci = stringStream.indexOf(Parametri.DMNOTES_CODICEBEGIN);
    cf = stringStream.indexOf(Parametri.DMNOTES_CODICEEND);
    vi = stringStream.indexOf(Parametri.DMNOTES_VALOREBEGIN);
    vf = stringStream.indexOf(Parametri.DMNOTES_VALOREEND);

    while (ci != -1) {
      sCodice = stringStream.substring(ci+Parametri.DMNOTES_CODICEBEGIN.length(), cf);
      sValore = stringStream.substring(vi+Parametri.DMNOTES_VALOREBEGIN.length(), vf);
      nCaratteri = nCaratteri + sCodice.length() + sValore.length();
      if (nCaratteri < 32000) {
        caricaOrdinata(sCodice,sValore,ordinamento);
        stringStream = stringStream.substring(vf+Parametri.DMNOTES_VALOREEND.length());
        ci = stringStream.indexOf(Parametri.DMNOTES_CODICEBEGIN);
        cf = stringStream.indexOf(Parametri.DMNOTES_CODICEEND);
        vi = stringStream.indexOf(Parametri.DMNOTES_VALOREBEGIN);
        vf = stringStream.indexOf(Parametri.DMNOTES_VALOREEND);
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
        break;
      }
    }
  }


  /**
   * Nel caso di un DominioStandard non è necessario fare il BINDING dei parametri in quanto
   * l'istruzione da eseguire è sempre la stessa, ma richiamiamo ugualmente la funzione per
   * compatibilità con sviluppi futuri di Domini di questo tipo.
   *
   * @author Marco Bonforte
   * @param mp  AbstractParser
   */
  protected void caricaValori(AbstractParser mp) {
    IDbOperationSQL   dbOp = null;
//    Statement     statement = null;
    ResultSet     resultQuery = null;
    String        rstcodice = null;
    String        rstvalore = null;
    int           nCaratteri = 0;

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      istruzione = mp.bindingDeiParametri(istruzione);
      
      if (istruzione == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        return;  // *** exit point ***
      }

      dbOp.setStatement(istruzione);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();

      while (resultQuery.next()) {
         rstcodice = resultQuery.getString("CODICE");
         rstvalore = resultQuery.getString("VALORE");
         nCaratteri = nCaratteri + rstcodice.length() + rstvalore.length();
         if (nCaratteri < 32000) {
            caricaOrdinata(rstcodice,rstvalore,ordinamento);
         } else {
           codici.clear();
           valori.clear();
           codici.add("TROPPI DATI");
           valori.add("TROPPI DATI");
           break;
        }
      }
    } catch (Exception sqle) {
      loggerError("Dominio::caricaValori - Area: "+area+" Dominio: "+Dominio+" Attenzione: si è verificato un errore: "+sqle.toString(),sqle);
    }
    finally {
      free(dbOp);
    }

  }

  /**
   * La funzione mi dice se in fase di caricamento del Dominio ho interpretato almeno un parametro,
   * in modo che se viene richiesto un aggiornamento del Dominio venga ripetuto il caricamento.
   *
   * @author  Adelmo Gentilini
   * @return  true se il caricamento del Dominio ha richiesto l'interpretazione dei parametri
   *          false se il caricamento del Dominio non ha richiesto il binding di alcun parametro
   */
  public boolean isParametrico() {
    return parametrico;
  }

  public String getArea() {
    return area;
  }

  public String getDominio() {
    return Dominio;
  }

  public boolean isDominioDiArea() {
    return isDominioDiArea;
  }

  public boolean isDominioFormulaModello() {
    return isDominioFormulaModello;
  }

  public boolean isDominioDelModello() {
    return isDominioDelModello;
  }

  public void setDominioDiArea(boolean isDDA) {
    isDominioDiArea = isDDA;
  }

  public void setDominioDelModello(boolean isDDA) {
    isDominioDelModello = isDDA;
  }

  public void setDominioFormulaModello(boolean isDFM) {
    isDominioFormulaModello = isDFM;
  }

  public String getDescrizione() {
    return descrizione;
  }

  public String getCodice(int i) {
    return (String) codici.get(i);
  }

  public String getCodice(String pValore) {
    String  retval = "";
    boolean trovato = false;
    int     i = 0;

    while ((i<valori.size()) && (!trovato)) {
      if (valori.get(i).equals(pValore)) {
        retval = (String) codici.get(i);
        trovato = true;
      }
      i++;
    }
    return retval;
  }

  public String getValore(int i) {
    return (String) valori.get(i);
  }

  /**
   *
   */
  public String getValore(String pCodice) {
    String  retval = "";
    String  codice = "";
    boolean trovato = false;
    int     i = 0;

    while ((i<codici.size()) && (!trovato)) {
      if (codici.get(i).equals(pCodice)) {
        retval = (String) valori.get(i);
        trovato = true;
      }
      codice = (String)codici.get(i);
      if (codice.indexOf(pCodice) > -1 && codice.indexOf("style") > -1 ) {
        retval = (String) valori.get(i);
        trovato = true;
      }
      i++;
    }
    if (trovato) {
      return retval;
    } else {
      return null;
    }
  }

  /**
   *
   */
  public int getNumeroValori() {
    return valori.size();
  }

  /**
   *
   */
  protected void free(IDbOperationSQL dbOp) {

    try {
      dbOp.close();
    } catch (Exception e) {
    }
  }

  /**
   * 
   */
  protected void caricaOrdinata(String codice, String valore, String ordine) {
//    boolean trovato = false;
    int     i = 0;

    if (ordine == null) {
      ordine = "";
    }

    //Ordinamento Codice - Testo
    if (ordine.equalsIgnoreCase("1")) {
      while ((i<codici.size()) && (((String)codici.get(i)).compareTo(codice) < 0)) {
        i++;
      }
      codici.add(i,codice);
      valori.add(i,valore);
      return;
    }

    //Ordinamento Codice - Numero
    if (ordine.equalsIgnoreCase("2")) {
      int iOrd = 0;
      int iCod = 0;
      try {
        iOrd = Integer.parseInt((String)codici.get(i));
      } catch (Exception e) {
        iOrd = -1;
      }
      try {
        iCod = Integer.parseInt(codice);
      } catch (Exception e) {
        iCod = -1;
      }
      while ((i<codici.size()) && (iOrd < iCod)) {
        i++;
        try {
          iOrd = Integer.parseInt((String)codici.get(i));
        } catch (Exception e) {
          iOrd = -1;
        }
      }
      codici.add(i,codice);
      valori.add(i,valore);
      return;
    }

    //Ordinamento Valore - Testo
    if (ordine.equalsIgnoreCase("3")) {
      while ((i<valori.size()) && (((String)valori.get(i)).compareTo(valore) < 0)) {
        i++;
      }
      codici.add(i,codice);
      valori.add(i,valore);
      return;
    }

    //Ordinamento Valore - Numero
    if (ordine.equalsIgnoreCase("4")) {
      int iOrd = 0;
      int iVal = 0;
      try {
        iOrd = Integer.parseInt((String)valori.get(i));
      } catch (Exception e) {
        iOrd = -1;
      }
      try {
        iVal = Integer.parseInt(valore);
      } catch (Exception e) {
        iVal = -1;
      }
      while ((i<valori.size()) && (iOrd < iVal)) {
        i++;
        try {
          iOrd = Integer.parseInt((String)valori.get(i));
        } catch (Exception e) {
          iOrd = -1;
        }
      }
      codici.add(i,codice);
      valori.add(i,valore);
      return;
    }
    codici.add(codice);
    valori.add(valore);

  }

  /**
   * 
   */
   protected String completaConnessione(String connessione){
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
  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }
}

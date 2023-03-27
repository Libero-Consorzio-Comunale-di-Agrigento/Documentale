package it.finmatica.modulistica.modulisticapack;

import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
//import it.finmatica.jfc.bcUtil.*;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;

 
/**
 * Classe Dominio
 *
 * @author Nicola Samoggia
 * @author Antonio Plastini   
 * @version 1.0  
 */
public abstract class Dominio {
  public final static int MAXLEN_ISTRUZIONE = 20000;  // Numero massimo di caratteri ammessi per un'istruzione
  public final static int MAXLEN_VALORI 		= 80000;  // Numero massimo di caratteri ammessi per i codici e valori 
                                                     
  
//  private           boolean     isVerbose = Parametri.ISVERBOSE;
  protected         String      area;
  protected         String      Dominio;
  protected         String      codiceModello;
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
  protected         List<String>        codici;
  protected         List<String>        valori;
  protected  static Properties  dizionario = null;
  protected         String      textToParsing;
  protected         String      ordinamento = null;
  private static    Logger      logger = Logger.getLogger(Dominio.class);
  private  boolean debuglog = logger.isDebugEnabled();

  public Dominio(String pArea, String pDominio, String cm, String pTipo, String pOrdinamento) throws Exception {
    this(pArea,pDominio,cm,pTipo,pOrdinamento,null);
  }

  /**
   * Costruttore
   */
  public Dominio(String pArea, String pDominio, String cm, String pTipo, String pOrdinamento, IDbOperationSQL dbOpEsterna) throws Exception {
    area            = pArea;
    Dominio         = pDominio;
    codiceModello   = cm;
    tipo            = pTipo;
    ordinamento     = pOrdinamento;
    isDominioDiArea = false;
    descrizione     = "";
    istruzione      = "";
    connessione     = "";
    codici          = new ArrayList<String>();
    valori          = new ArrayList<String>();
    parametrico     = false; // Di default il Dominio non è parametrico
    initDizionario();
  }

  /**
   * La funzione suppone di ricevere i valori da scrivere in una stringa
   *
   * @author Adelmo
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
    if (stringStream.length() == 0) {
      codici.clear();
      valori.clear();
      return;
    }
    if (!Parametri.DEBUG.equalsIgnoreCase("0")) {
    	logger.debug("Area: "+getArea()+ " - Dominio: "+getDominio()+" --> "+ stringStream);
    }
    ci = stringStream.indexOf(Parametri.DMNOTES_CODICEBEGIN);
    cf = stringStream.indexOf(Parametri.DMNOTES_CODICEEND);
    vi = stringStream.indexOf(Parametri.DMNOTES_VALOREBEGIN);
    vf = stringStream.indexOf(Parametri.DMNOTES_VALOREEND);

    while (ci != -1) {
      sCodice = stringStream.substring(ci+Parametri.DMNOTES_CODICEBEGIN.length(), cf);
      sValore = stringStream.substring(vi+Parametri.DMNOTES_VALOREBEGIN.length(), vf);
      nCaratteri = nCaratteri + sCodice.length() + sValore.length();
      if (nCaratteri < MAXLEN_VALORI) {
        caricaOrdinata(sCodice,sValore,ordinamento);
//        codici.add(sCodice);
//        valori.add(sValore);
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

   protected void caricaValori(HttpServletRequest pRequest) {
     caricaValori(pRequest,null);
   }



    /**
     * Nel caso di un DominioStandard non è necessario fare il BINDING dei parametri in quanto
     * l'istruzione da eseguire è sempre la stessa, ma richiamiamo ugualmente la funzione per
     * compatibilità con sviluppi futuri di Domini di questo tipo.
     *
     * @author Adelmo
     * @param pRequest  richiesta HTTP oper ottenere la sessione da cui estrarre i valori dei parametri
     */
  protected void caricaValori(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("Dominio::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL   dbOp = null;
//    Statement     statement = null;
    ResultSet     resultQuery = null;
    String        rstcodice = null;
    String        rstvalore = null;
    int           nCaratteri = 0;

    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp =dbOpEsterna;
      }
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      istruzione = mp.bindingDeiParametri(istruzione);
//      istruzione = bindingDinamico(pRequest, istruzione, false);
      
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
         if (nCaratteri < MAXLEN_VALORI) {
            caricaOrdinata(rstcodice,rstvalore,ordinamento);
//          codici.add(rstcodice);
//          valori.add(rstvalore);
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
      if (dbOpEsterna==null) free(dbOp);
    }
    //Debug Tempo
    stampaTempo("Dominio::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  protected void caricaValori(AbstractParser mp) {
    //Debug Tempo
    long ptime = stampaTempo("Dominio::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL   dbOp = null;
    ResultSet     resultQuery = null;
    String        rstcodice = null;
    String        rstvalore = null;
    int           nCaratteri = 0;

    try {

        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

//      ModulisticaParser mp = new ModulisticaParser(pRequest);
      istruzione = mp.bindingDeiParametri(istruzione);
//      istruzione = bindingDinamico(pRequest, istruzione, false);
      
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
         if (nCaratteri < MAXLEN_VALORI) {
            caricaOrdinata(rstcodice,rstvalore,ordinamento);
//          codici.add(rstcodice);
//          valori.add(rstvalore);
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

    //Debug Tempo
    stampaTempo("Dominio::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
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

  public String getCodiceModello() {
    return codiceModello;
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
  * Inizializzo il dizionario di tutte le stringhe (composte da un carattere singolo) 
  * che possono segnalarmi la fine del nome di un parametro.
  * Caratteri considerati come fine parametro quindi non utilizzabili nei 
  * nomi di parametro:
  * 			spazio
  * 			,
  * 			)
  * 			(
  * 			*
  * 			+
  * 			/
  * 			-		(è meno non underscore)
  * 			\
  * 			|
  * 			&
  * 			=
  * 			<
  * 			>
  *       .
  */
  public  static void initDizionario()
  {
      byte[] b2 = new byte[1]; 
      b2[0] = 13;
      String ch = new String(b2);

      if (dizionario == null){
        dizionario = new Properties();
        dizionario.setProperty(ch, "OK");
        dizionario.setProperty(" ", "OK");
        dizionario.setProperty(",","OK"); 
        dizionario.setProperty(")","OK");
        dizionario.setProperty("(","OK");
        dizionario.setProperty("*","OK");
        dizionario.setProperty("+","OK");
        dizionario.setProperty("/","OK");
        dizionario.setProperty("-","OK");
        dizionario.setProperty("|","OK");
        dizionario.setProperty("&","OK");
        dizionario.setProperty("=","OK");
        dizionario.setProperty("<","OK");
        dizionario.setProperty(">","OK");
        dizionario.setProperty("'","OK");
        dizionario.setProperty("\"","OK");
        dizionario.setProperty("\n","OK");
        dizionario.setProperty("\t","OK");
        dizionario.setProperty("\\","OK");
        dizionario.setProperty(":","OK");
        dizionario.setProperty("?","OK");
        dizionario.setProperty(".","OK");
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

     try {
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
     } catch (Exception e) {
       loggerError("Errore in completaConnessione. Connessione: "+connessione,e);
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
  protected long stampaTempo(String sMsg, String area, String dominio, long ptime) {
    if (!debuglog) {
      return 0;
    }
    long adesso = Calendar.getInstance().getTimeInMillis();
    long trascorso = 0;
    if (ptime > 0) {
      trascorso = adesso - ptime;
    }
    long min_time = Long.parseLong(Parametri.MIN_TIME_LOG);
    if (trascorso < min_time) {
      return adesso;
    }
    if (Parametri.DEBUG.equalsIgnoreCase("1") && ptime > 0) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -Dominio:"+dominio+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -Dominio:"+dominio+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }
}




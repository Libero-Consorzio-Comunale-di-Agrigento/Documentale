package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.modulisticapack.*;
import java.util.Properties;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.pdf.PdfUtility;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.dmServer.Environment;
import org.apache.log4j.Logger;

public class GeneraPdf {
  private OutputStream  pdfOut = new ByteArrayOutputStream();
  private String        inifile = null;
  private String        sPath   = "";
  private String        us      = "";
  private Environment vu;
  private static Properties confLogger = null;
  private static Logger logger = Logger.getLogger(GeneraPdf.class);
  
  public GeneraPdf(String sPath) {
    init(sPath);
  }

  private void init(String pPath) {
    try {
      String separa = File.separator;
      sPath = pPath;
      inifile = pPath + separa + "config" + separa + "gd4dm.properties";
      File f = new File(inifile);
      if (!f.exists()) {
        inifile = sPath + separa + ".." + separa + "jgdm" + separa + "config" + separa + "gd4dm.properties";
      }
//      FileInputStream fis = new FileInputStream(inifile);
//      confLogger = new Properties();
//      confLogger.load(fis);
//      String esiste =confLogger.getProperty("log4j.logger.it.finmatica.modulistica");
//      if (esiste == null) {
//        esiste = "";
//      }
//      if (esiste == "") {
//        confLogger.setProperty("log4j.logger.it.finmatica.modulistica","ERROR, S");
//        confLogger.setProperty("log4j.appender.S","org.apache.log4j.RollingFileAppender");
//        confLogger.setProperty("log4j.appender.S.File","${catalina.home}/logs/jgdm.log");
//        confLogger.setProperty("log4j.appender.S.MaxFileSize","10MB");
//        confLogger.setProperty("log4j.appender.S.MaxBackupIndex","10");
//        confLogger.setProperty("log4j.appender.S.layout","org.apache.log4j.PatternLayout");
//        confLogger.setProperty("log4j.appender.S.layout.ConversionPattern","%d{HH:mm:ss} [%p] %c: %m%n");
//      }
//      PropertyConfigurator.configure(confLogger);
      Parametri.leggiParametriStandard(inifile);

      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      logger.error("GeneraPdf::init() - Attenzione! si è verificato un errore: "+e.toString());
    }
  }

    public void generaModello(HttpServletRequest  request,
                              String      ar,
                              String      cm,
                              String      cr,
                              File        bitmap,
                              String      pHeader, 
                              String      pFooter,
                              boolean     pageNumbers) {

    PdfUtility        pdfUti     = null;
    HttpSession       session    = request.getSession();
    String            nominativo = request.getRemoteUser();
    String            corpo      = "";
    String            iddoc      = "";
//    RicercaDocumento  r2        = null;
//    Vector            ldoc      = null;

    String id_session = ar+'-'+cr+'-'+session.getId();
    session.setAttribute("key",cr);
    session.setAttribute("pdo","");
    if (nominativo == null) {
      us      = (String)session.getAttribute("Utente");
    } else {
      us = cercaUtente(nominativo.toUpperCase());
    }

    if (us == null) {
      us = "";
    }
    if (us.length() == 0) {
      us = "GUEST";
    } 
    us = us.toUpperCase();
    session.setAttribute("UtenteGDM",us);
    session.setAttribute("RuoloGDM","GUEST");

    try {
      iddoc = ricercaIdDocumento(ar,cm,cr,us);
      cercaRichiesta(ar,id_session);

      if (loginModulistica(ar, cm, cr, us, iddoc) == true) {
        //Ho i diritti di lettura
        leggiValori(ar,cm,id_session,us,iddoc);
        corpo = costruisciModuloLettura(request,ar,cm,cr,id_session);
        cancellaRepository(ar,cm,id_session);
      } else {
        //Non ho i diritti di lettura
        corpo = "";
      }
      int j = corpo.indexOf("</head>");
      if (j == -1) {
        j = corpo.indexOf("</HEAD>");
      }
      String new_copro = "<HTML><HEAD>"+corpo.substring(j);
      InputStream is = new ByteArrayInputStream(new_copro.getBytes());
      Properties prPr = new Properties();
      prPr.setProperty("htmlconverter",Parametri.HTMLCONVERTER);
      pdfUti = new PdfUtility(prPr);

      if (bitmap != null) {
        pdfUti.HtmlToPdf(is, bitmap, pdfOut, pHeader, pFooter, pageNumbers);
      } else {
        pdfUti.HtmlToPdf(is, pdfOut, pHeader, pFooter, pageNumbers);
      }
      is.close();
    } catch(Exception e) {
      logger.error("GeneraPdf::genera() - Attenzione! si è verificato un errore: "+e.toString());
    }

  }

  public void generaModello(HttpServletRequest  pRequest,
                      File                bitmap,
                      String              pHeader, 
                      String              pFooter,
                      boolean             pageNumbers) {

    PdfUtility pdfUti = null;
    try {
      Modulistica md = new Modulistica(sPath);
      md.genera(pRequest,"");
      String corpo = md.getValue();
      //Controllo se creare il pdf o leggerlo.
      int i = corpo.indexOf("SYS_PDF");
      if ( i > -1) {
        String iddoc = corpo.substring(6,i);
        i = corpo.lastIndexOf("=");
        String ca = corpo.substring(i+1);
        initVu(pRequest);
        AccediDocumento ad = new AccediDocumento(iddoc,vu);
        ad.accediDocumentoAllegati();
        InputStream bis = ad.leggiOggettoFile(ca);


     		int ibit = bis.read();
		   	while ((ibit) >= 0)
		   	{
		       		pdfOut.write(ibit);
		      		ibit = bis.read();
		    }
        
//        pdfOut.flush();
        bis.close();
      } else {
        int j = corpo.indexOf("</head>");
        if (j == -1) {
          j = corpo.indexOf("</HEAD>");
        }
        String new_copro = "<HTML><HEAD>"+corpo.substring(j);
        InputStream is = new ByteArrayInputStream(new_copro.getBytes());
        Properties prPr = new Properties();
        prPr.setProperty("htmlconverter",Parametri.HTMLCONVERTER);
        pdfUti = new PdfUtility(prPr);

        if (bitmap != null) {
          pdfUti.HtmlToPdf(is, bitmap, pdfOut, pHeader, pFooter, pageNumbers);
        } else {
          pdfUti.HtmlToPdf(is, pdfOut, pHeader, pFooter, pageNumbers);
        }
        is.close();
      }
    } catch(Exception e) {
      logger.error("GeneraPdf::genera() - Attenzione! si è verificato un errore: "+e.toString());
    }

  }

  public void generaEditing(HttpServletRequest  pRequest,
                      File                bitmap,
                      String              pHeader, 
                      String              pFooter,
                      boolean             pageNumbers) {

    PdfUtility pdfUti = null;
    try {
      Editing md = new Editing(sPath);
      md.genera(pRequest,"");
      String corpo = md.getValue();
      int j = corpo.indexOf("</head>");
      if (j == -1) {
        j = corpo.indexOf("</HEAD>");
      }
      String new_copro = "<HTML><HEAD>"+corpo.substring(j);
      InputStream is = new ByteArrayInputStream(new_copro.getBytes());
      Properties prPr = new Properties();
      prPr.setProperty("htmlconverter",Parametri.HTMLCONVERTER);
      pdfUti = new PdfUtility(prPr);

      if (bitmap != null) {
        pdfUti.HtmlToPdf(is, bitmap, pdfOut, pHeader, pFooter, pageNumbers);
      } else {
        pdfUti.HtmlToPdf(is, pdfOut, pHeader, pFooter, pageNumbers);
      }
      is.close();
    } catch(Exception e) {
      logger.error("GeneraPdf::genera() - Attenzione! si è verificato un errore: "+e.toString());
    }

  }

  public void genera(Properties          propPr, 
                      InputStream         modelloIn,
                      File                bitmap,
                      String              pHeader, 
                      String              pFooter,
                      boolean             pageNumbers) {

    InputStream is = modelloIn;
    PdfUtility pdfUti = null;
    try {
      pdfUti = new PdfUtility(propPr);

      if (bitmap != null) {
        pdfUti.HtmlToPdf(is, bitmap, pdfOut, pHeader, pFooter, pageNumbers);
      } else {
        pdfUti.HtmlToPdf(is, pdfOut, pHeader, pFooter, pageNumbers);
      }
      is.close();
    } catch(Exception e) {
      logger.error("GeneraPdf::genera() - Attenzione! si è verificato un errore: "+e.toString());
    }

  }

  public void genera(InputStream         propIs, 
                      InputStream         modelloIn,
                      File                bitmap,
                      String              pHeader, 
                      String              pFooter,
                      boolean             pageNumbers) {

    InputStream is = modelloIn;
    PdfUtility pdfUti = null;
    try {
      pdfUti = new PdfUtility(propIs);

      if (bitmap != null) {
        pdfUti.HtmlToPdf(is, bitmap, pdfOut, pHeader, pFooter, pageNumbers);
      } else {
        pdfUti.HtmlToPdf(is, pdfOut, pHeader, pFooter, pageNumbers);
      }
      is.close();
    } catch(Exception e) {
      logger.error("GeneraPdf::genera() - Attenzione! si è verificato un errore: "+e.toString());
    }

  }

  /**
   * 
   */
  public OutputStream getPdf() {
    return pdfOut;
  }

  /**
   * 
   */
  public void salvaFile(String pathFile)  throws Exception {
    try {
      OutputStream fo = new FileOutputStream(pathFile);
      ByteArrayOutputStream bo = (ByteArrayOutputStream)pdfOut;
      bo.writeTo(fo);
      bo.close();
      fo.close();
    } catch (Exception e) {
      logger.error("GeneraPDF::salvaFile() - "+e.toString());
      throw new Exception ("GeneraPDF::salvaFile()"+e.toString());    
    }
  }

  /**
   * 
   */
  public void salvaBlob(LinkedList campi,
                        LinkedList valori,
                        String campoBlob,
                        String tabella, 
                        String condizioni, 
                        String tipoOperazione)  throws Exception {
      ByteArrayOutputStream os = (ByteArrayOutputStream)pdfOut;
      InputStream is = new ByteArrayInputStream(os.toByteArray());
      String query = "";
//      ResultSet rst = null;                
      IDbOperationSQL dbOp = null;

      try {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
        int campi_size = campi.size();
        int valori_size = valori.size();
        
        if (tipoOperazione.equalsIgnoreCase("I")) {
          query = "INSERT INTO "+tabella+" (";
          for (int i=0; i < campi_size; i++) {
            query += (String)campi.get(i)+", "; 
          }
          query += campoBlob+") VALUES (";
          for (int j=0; j < valori_size; j++) {
            query += (String)valori.get(j)+", "; 
          }
          query += ":ALLEGATO)";
        } else {
          query = "UPDATE "+tabella+" SET "+
            campoBlob+" = :ALLEGATO ";
          if (condizioni.length() != 0) {
            query += "WHERE "+condizioni;
          }
        }
        dbOp.setStatement(query);
        dbOp.setParameter(":ALLEGATO",(InputStream)is, -1);
        dbOp.execute();
        dbOp.commit();
        free(dbOp);
      } catch (Exception e) {
        free(dbOp);
        logger.error("GeneraPdf::salvaBlob() - Errore:  ["+ e.toString()+"]");
        throw new Exception("GeneraPDF::salvaBlob() "+e.toString());    
      }
  }

  /**
   * 
   */
  public void salvaAllegato(String area, 
                        String cm, 
                        String cr, 
                        String nomefile,
                        String p_user)  throws Exception {
    String iddoc = "";
    
    try {
      iddoc = ricercaIdDocumento(area,cm,cr,p_user);
      salvaAllegato(iddoc,nomefile,p_user);
    } catch (Exception e) {
      logger.error("GeneraPdf::salvaAllegato() - Errore:  ["+ e.toString()+"]");
      throw new Exception("GeneraPDF::salvaAllegato() "+e.toString());    
    }
  }

  /**
   * 
   */
  public void salvaAllegato(String iddoc, 
                        String nomefile,
                        String p_user) throws Exception {
    try {
      ByteArrayOutputStream os = (ByteArrayOutputStream)pdfOut;
      InputStream is = new ByteArrayInputStream(os.toByteArray());
//      String separa = File.separator;

      Environment vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, inifile);
      vu.setRuolo("GDM");
      AggiornaDocumento ad = new AggiornaDocumento(iddoc,vu);
      ad.aggiornaAllegato(is,nomefile);
      ad.salvaDocumentoBozza();
    } catch (Exception e) {
      logger.error("GeneraPdf::salvaAllegato() - Errore:  ["+ e.toString()+"]");
      throw new Exception("GeneraPDF::salvaBlob() "+e.toString());    
    }
  }

  /**
   * 
   */
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }


  /**
   * 
   */
  private String ricercaIdDocumento (String area, 
                                     String cm, 
                                     String cr, 
                                     String p_user) throws Exception {
    Vector  ll;
    String  idtipodoc = null;
    String  iddoc = null;
//    String  stato_doc = null;
//    String  separa = File.separator;

    idtipodoc = ricavaIdtipodoc(area,cm);

    try {
      Environment vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, inifile);
      vu.setRuolo("GDM");

      RicercaDocumento rd = new RicercaDocumento(idtipodoc,vu);
      rd.settaCodiceRichiesta(cr);
      ll = rd.ricerca();
      if(ll.size() == 1) {
        iddoc = (String)ll.firstElement();
      } else {
        iddoc = null;
        logger.error("GeneraPdf::ricercaIdDocumento() - Errore:  [Esite più di un documento per Area: "+area+" - Modello: "+cm+" Richiesta: "+cr+"]");
        throw new Exception("GeneraPdf::salvaAllegato() Errore:  [Esite più di un documento per Area: "+area+" - Modello: "+cm+" Richiesta: "+cr+"]");    
      }
    } catch (Exception e) {
      logger.error("GeneraPdf::ricercaIdDocumento() - Errore:  ["+ e.toString()+"]");
      throw new Exception("GeneraPDF::salvaAllegato() "+e.toString());    
    }
    return iddoc;
  }

  /**
   * 
   */
  private String ricavaIdtipodoc (String ar, String cm) throws Exception {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          idtipodoc = null;
    String          codmod = null;

    query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE"+
            " FROM MODELLI"+
            " WHERE AREA = :AREA"+
            "   AND CODICE_MODELLO = :CM";
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         idtipodoc = rst.getString("ID_TIPODOC");
         codmod = rst.getString("CODICE_MODELLO_PADRE");
      } else {
        free(dbOp);
        logger.error("GeneraPdf::ricavaIdtipodoc() - Errore in ricerca Identificativo tipo documento. Modello non trovato!");
        throw new Exception("GeneraPDF::ricavaIdtipodoc()Errore in ricerca Identificativo tipo documento. Modello non trovato!");    
//        return "";
      }

      if (idtipodoc == null) {
        idtipodoc = "";
      }

      dbOp.setStatement(query);
      if (idtipodoc.length() == 0) {
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CM",codmod);
        dbOp.execute();
        rst = dbOp.getRstSet();

        if (rst.next() ) {
           idtipodoc = rst.getString("ID_TIPODOC");
        }
      }
      free(dbOp);
      return idtipodoc;
    
    } catch (Exception e) {
      free(dbOp);
      logger.error("GeneraPdf::ricavaIdtipodoc() - Errore:  ["+ e.toString()+"]");
      throw new Exception("GeneraPDF::ricavaIdtipodoc() "+e.toString());    
//      return "";
    }
  }

  public void concatena(HttpServletRequest  request,
                        String[]            documenti,
                        File                bitmap,
                        String              pHeader, 
                        String              pFooter,
                        boolean             pageNumbers) {
    String area = "";
    String cm = "";
    String cr = "";
    String documento = "";
    int numero_docuneti = documenti.length;
    InputStream[] lPdf = new InputStream[numero_docuneti];
    PdfUtility pdfUti = null;
    try {
      InputStream isPr = new FileInputStream(inifile);
      pdfUti = new PdfUtility(isPr);
      isPr.close();
      for (int i=0; i < numero_docuneti; i++) {
        documento = documenti[i];
        int j = documento.indexOf("&");
        if (j != -1) {
          int k = documento.indexOf("&",j+1);
          if (k != -1) {
            area = documento.substring(0,j);
            cm   = documento.substring(j+1,k);
            cr   = documento.substring(k+1);
            generaModello(request,area,cm,cr,bitmap,pHeader,pFooter,true);
            salvaFile("c:\\"+cr+".pdf");
            ByteArrayOutputStream os = (ByteArrayOutputStream)pdfOut;
            lPdf[i] = new ByteArrayInputStream(os.toByteArray());
            pdfOut.close();
            pdfOut = new ByteArrayOutputStream();
          }
        }
      }
      pdfUti.concatPdf(lPdf,pdfOut);
    } catch (Exception e) {
      logger.error("GeneraPdf::concatena() - Attenzione! si è verificato un errore: "+e.toString());
    }
  }

  /**
   * 
   */
  private String cercaUtente(String nominativo) {
    String          retval = null;
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    
    //RICAVO IL RUOLO DELL'UTENTE
    query = "select utente from ad4_utenti where nominativo = :NOMINATIVO";
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      dbOp.setStatement(query);
      dbOp.setParameter(":NOMINATIVO", nominativo);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         retval = rst.getString(1);
      }
      free(dbOp);
      return retval;
    } catch (Exception e) {
      logger.error("GeneraPDF::cercaUtente - "+e.toString());
      free(dbOp);
      return "";
    }
  }

  /**
   * loginSportello()
   * Test se l'utente collegato ha il diritto di richiedere il modulo attuale, quindi testo
   * se può accedere ai dati della attuale pratica in quella particolare area.
   *
   * @author  Marco Bonforte
   * @return  true se il login ha successo, false altrimenti
   */
  boolean loginModulistica(String p_area,String p_cm, String p_richiesta,String p_user, String iddoc) {
//    String          sComp = null;
    String          sAbil = "L";

    try {
      Environment vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, inifile);
      vu.setRuolo("GDM");
//      sComp = "";
      UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(), vu.getPwd(),  vu.getUser());
      Abilitazioni ab = new Abilitazioni("DOCUMENTI", iddoc, sAbil);
      if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,ab) == 0) {
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      logger.error("GeneraPdf::loginModulistica - "+e.toString());
      return false;
    }
  }

  /**
   * preCaricamentoDati()
   * Metodo per il precaricamento dei dati. Si cerca un dominio di area e si richiama la
   * opportuna funzione.
   *
   * @param request
   * @param pArea area di riferimento
   * @param cr codice richiesta
   */
  protected void preCaricamentoDati(HttpServletRequest request, String pArea, String cr, String cm, boolean bArea) throws Exception {
    ListaDomini ld;
//    Dominio     dominioArea;

    try {
      ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
      if (ld == null) {
        // Inizializzo una variabile di tipo ListaDomini a livello di sessione
        // che servirà a contenere i puntatori ai vari domini letti dal database.
        // In questo modo i singoli oggetti Dominio verranno inizializzati solo
        // una volta per ogni coppia di "area , dominio" (pk) e referenziati
        // nelle liste dei campi di volta in volta.
        ld = new ListaDomini();
        request.getSession().setAttribute("listaDomini", ld);
      } else {
        // Aggiorno i valori dei domini già in memoria (la funzione della lista
        // si preoccupa di aggiornare solo quelli che sono parametrici)
        ld.aggiornaDomini(request);
      }

      // Tiro su i dominii di area ordinati per sequenza
      if (bArea) {
        ld.caricaDominiiDiArea(pArea, request, false);
      }
      ld.caricaDominiiDelModello(pArea, cm, request, false);
    } catch (Exception ex) {
      logger.error("ServletModulistica::preCaricamentoDati() - Attenzione! Si è verificato un errore in fase di precaricamento: "+ex.toString());
      throw ex; 
    }
  }

  /**
   * 
   */
  protected boolean isModelloAperto(String pArea, String cr,String cm) {
    IDbOperationSQL dbOp = null;
    ResultSet   rst = null;
    String      query/*,
                revisione = null*/;
    boolean     retVal = true;   // per default è il primo

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      query = "SELECT AREA "+
              "FROM REPOSITORYTEMP "+
              "WHERE AREA = :AREA AND "+
              "CODICE_RICHIESTA = :CODICE_RICHIESTA"+
              "CODICE_MODELLO = :CM";
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",pArea);
      dbOp.setParameter(":CODICE_RICHIESTA",cr);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next() ) {
        // Trovati dei dati nel RepositoryTemp quindi non è il primo per quella richiesta
        retVal = false;
      }
    } catch (Exception ex) {
      retVal = false;
    }

    free(dbOp);
    return retVal;
  }

  /**
   * 
   */
  protected boolean isPrimoModello(String pArea, String cr) {
    IDbOperationSQL dbOp = null;
    ResultSet   rst = null;
    String      query/*,
                revisione = null*/;
    boolean     retVal = true;   // per default è il primo

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      query = "SELECT AREA "+
              "FROM REPOSITORYTEMP "+
              "WHERE AREA = :AREA AND "+
              "CODICE_RICHIESTA = :CODICE_RICHIESTA";
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",pArea);
      dbOp.setParameter(":CODICE_RICHIESTA",cr);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next() ) {
        // Trovati dei dati nel RepositoryTemp quindi non è il primo per quella richiesta
        retVal = false;
      }
    } catch (Exception ex) {
      retVal = false;
    }

    free(dbOp);
    return retVal;
  }

  /**
   * 
   */
  protected String costruisciModuloLettura(HttpServletRequest request,
                                           String             ar,
                                           String             cm,
                                           String             cr,
                                           String             id_session) {
    HttpSession     httpSess;
    Modello         md = null;
    ModelloHTMLOut  mdOut = null;
    ArrayList       modelli;
    ListaControlli  controlli;
    ListaDomini     domini;
    String          sc = "1";
//    boolean         trovato;
//    int             i;

    try {
      httpSess = request.getSession();
      // ***** MODELLI *****
      modelli = (ArrayList) httpSess.getAttribute("modelli");
      if (modelli == null) {
        // Inizializzo la lista dei modelli a livello di sessione.
        // In questo modo mi garantisco che la servlet restituisca il modello
        // al client da cui viene invocato.
        modelli = new ArrayList();
        httpSess.setAttribute("modelli", modelli);
      }

      // ***** CONTROLLI *****
      controlli = (ListaControlli)httpSess.getAttribute("listaControlli");
      
      if (controlli == null) {
        // Inizializzo una variabile di tipo ListaControlli a livello di sessione
        // che servirà a contenere i puntatori ai vari controlli letti dal database.
        // In questo modo i singoli oggetti Controllo verranno inizializzati solo
        // una volta per ogni coppia di "area , controllo" (pk) e referenziati
        // nelle liste dei singoli oggetti di volta in volta.
        controlli = new ListaControlli();
        httpSess.setAttribute("listaControlli", controlli);
      }

      //***** DOMINI *****
      domini = (ListaDomini)httpSess.getAttribute("listaDomini");
     
      if (domini == null) {
        // Inizializzo una variabile di tipo ListaDomini a livello di sessione
        // che servirà a contenere i puntatori ai vari domini letti dal database.
        // In questo modo i singoli oggetti Dominio verranno inizializzati solo
        // una volta per ogni coppia di "area , dominio" (pk) e referenziati
        // nelle liste dei campi di volta in volta.
        domini = new ListaDomini();
        httpSess.setAttribute("listaDomini", domini);
      } else {
        // Aggiorno i valori dei domini già in memoria (la funzione della lista
        // si preoccupa di aggiornare solo quelli che sono parametrici)
        domini.aggiornaDomini(request);
      }

      domini.caricaDominiiDiArea(ar, request, false);
      domini.caricaDominiiDelModello(ar, cm, request, false);

      if (isPrimoModello(ar, cr)) {
        // E' il primo modello quindi devo precaricare i dati necessari
        preCaricamentoDati(request, ar, id_session, cm, true);
        md = new ModelloHTMLIn(request, ar, cm, cr, calcolaScadenza(sc),""+Calendar.getInstance().getTimeInMillis());
        modelli.add(md);
      } else {
        // Non è il primo modello per cui vado a cercare se è già in memoria
        if (isModelloAperto(ar, cr, cm)) {
          preCaricamentoDati(request, ar, id_session, cm, false);
        }
        md = cercaModello(httpSess, ar, cm, cr);

        if (md == null) {
          // Se il modello non esiste devo crearne uno nuovo
          md = new ModelloHTMLIn(request, ar, cm, cr, calcolaScadenza(sc),""+Calendar.getInstance().getTimeInMillis());

          modelli.add(md);
        } else {
          // Se il modello era ancora in memoria dovrebbe essere di tipo HTMLIn quindi
          // risetto la request e ricalcolo i valori di default dei campi
          md.setNewRequest(request);
//         ((ModelloHTMLIn)md).aggiornaValori(request);
          ((ModelloHTMLIn)md).interpretaModello();
        }
         
      }
      mdOut = new ModelloHTMLOut((ModelloHTMLIn)md);
      String corpoMod = mdOut.getPrivPRNValue();
      modelli.remove(md);
      return corpoMod;
    } catch(Exception e) {
      logger.error("GeneraPdf::costruisciModuloLettura() - Attenzione! Si è verificato un errore: "+e.toString());
      return "";
    }
  }

  /**
   * calcolaScadenza()
   * Funzione privata per il calcolo del Timestamp corrispondente alla scadenza di un dato modulo
   *
   * @parameter durata è una stringa che deve rappresentare un intero che sarà il numero di giorni di
   *            validità da associare ad un dato modulo (tutti i campi di quel modulo avranno una
   *            durata di validità dipendente dal valore di questo parametro).
   * @author    Adelmo Gentilini
   * @return    Timestamp che mi rappresenta la data di scadenza del modulo; se il valore di ritorno è
   *            un valore null allora il dato ha scadenza infinita (si suppone coi in tutti quei casi
   *            in cui verrà prima o poi chiamata la BuildXML che provvederà a vuotare la tabella da
   *            tuti irecord corrispondenti ai dati richiesti.
   */
   private Timestamp calcolaScadenza(String durata) {
     int durataInt;
     if (durata == null) {
       return null; //** Exit point: se la durata è null torno null
     }

     try {
       durataInt = (new Integer(durata)).intValue();
     } catch (Exception ex) {
       return null; //** Exit point: se il numero non è traducibile ritorno null
     }

     Calendar scad = Calendar.getInstance();   // today
     scad.add(Calendar.DATE, durataInt);
     return (new Timestamp(scad.getTimeInMillis()));
   }

  /**
   * cercaModello()
   * Ricerca a livello di sessione la presenza del modello richiesto.
   * Se è già presente evita di ricaricarlo da database.
   **/
  private Modello cercaModello(HttpSession pHttpSess, String pArea, String pCodiceModello, String pCodiceRichiesta) throws Exception {
    Modello     md = null;
    ArrayList   modelli;
    boolean     trovato = false;
    int         i = 0;

    try {

      modelli = (ArrayList) pHttpSess.getAttribute("modelli");
      if (modelli == null) {
        logger.error("GeneraPdf::cercaModello() - Attenzione! Lista modelli non ancora inizializzata in memoria!");
        return null;
      }

      while ( (!trovato) && (i < modelli.size()) ) {
        md = (Modello)modelli.get(i);
        if ((md.getArea().equals(pArea)) &&
            (md.getCodiceModello().equals(pCodiceModello)) &&
            (md.getCodiceRichiesta().equals(pCodiceRichiesta))) {
          trovato = true;
        } else {
          i += 1;
        }
      }

      if (!trovato) {
        return null;
      } else {
        return md;
      }

    } catch(Exception e) {
      logger.error("GeneraPdf::cercaModello() - Attenzione! Si è verificato un errore durante la ricerca del modello in memoria: "+e.toString());
      return null;
    }
  }

  /**
   * 
   */
  private boolean cancellaRepository(String ar, String cm, String id_session) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;

    String query = "DELETE REPOSITORYTEMP"+
                   " WHERE AREA = :AREA"+
                   " AND CODICE_RICHIESTA = :IDSESSIONE"+
                   " AND CODICE_MODELLO = :CM";

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", ar);
      dbOp.setParameter(":IDSESSIONE", id_session);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
      dbOp.commit();
      free(dbOp);
    } catch (Exception e) {
      free(dbOp);
      logger.error("Generadf::cancellaRepository() - Errore:  ["+ e.toString()+"]");
      result = false;
    }
    return result;
  }

  /**
   * 
   */
  private boolean leggiValori(String ar, String cm, String id_session, String p_user, String iddoc) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;
    String          query;
    String          dato = null;
    String          valore = null;

    query = "SELECT DATO "+
            "FROM DATI_MODELLO "+
            "WHERE AREA = :AREA AND "+
            "CODICE_MODELLO = :CODICE_MODELLO";
    try {
      Environment vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, inifile);
      vu.setRuolo("GDM");
      AccediDocumento ad = new AccediDocumento(iddoc,vu);
      ad.accediFullDocumento();
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CODICE_MODELLO",cm);
      dbOp.execute();
      rs = dbOp.getRstSet();
      while (rs.next()) {
        dato   = rs.getString(1);
        try {
          valore = ad.leggiValoreCampo(dato);
        } catch (Exception e1) {
          valore = "";
        }
        scriviRepo(ar, id_session, cm, dato, valore);
      }
      free(dbOp);
    }
      catch (Exception e) {
      free(dbOp);
      logger.error("ServletEditing::leggiValori() - Errore:  ["+ e.toString()+"]");
      result = false;
    }
    return result;
  }

  /**
   * 
   */
  private void scriviRepo (String ar, String id_session, String cm, String dato, String valore) {
    IDbOperationSQL  dbOpIns = null;
    IDbOperationSQL  dbOpSel = null;
    ResultSet       rs;
    String          querySel, queryIns;

    querySel = "SELECT 1 FROM REPOSITORYTEMP " +
            " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
            " AND DATO = :DATO";

    try {
      dbOpSel = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      dbOpSel.setStatement(querySel);
      dbOpSel.setParameter(":AREA",ar);
      dbOpSel.setParameter(":CR",id_session);
      dbOpSel.setParameter(":CM",cm);
      dbOpSel.setParameter(":DATO",dato);
      dbOpSel.execute();
      rs = dbOpSel.getRstSet();
      if (valore.length() == 0) {
        queryIns = "DELETE REPOSITORYTEMP " + 
                   " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                   " AND DATO = :DATO";
      } else {
        if (!rs.next()) {
          queryIns = "INSERT INTO REPOSITORYTEMP " +
                     " (AREA, CODICE_RICHIESTA, CODICE_MODELLO, DATO, VALORE) VALUES "+
                     " (:AREA, :CR, :CM, :DATO, :VALORE)";
        } else {
          queryIns = "UPDATE REPOSITORYTEMP SET " +
                     " VALORE = :VALORE "+
                     " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                     " AND DATO = :DATO";
        }
      }
      dbOpIns = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      dbOpIns.setStatement(queryIns);
      dbOpIns.setParameter(":AREA",ar);
      dbOpIns.setParameter(":CR",id_session);
      dbOpIns.setParameter(":CM",cm);
      dbOpIns.setParameter(":DATO",dato);
      if (valore.length() != 0) {
        dbOpIns.setParameter(":VALORE",valore);
      }
      dbOpIns.execute();
      dbOpIns.commit();
      free(dbOpIns);
      free(dbOpSel);
    }
      catch (Exception e) {
      free(dbOpSel);
      free(dbOpIns);
      logger.error("ServletEditing::scriviRepo() - Errore:  ["+ e.toString()+"]");
    }
  }

  /**
   * 
   */
  private void cercaRichiesta(String area, String codice) {
    boolean         result = false;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;

/*    String querySel = "SELECT 1"+
                      "  FROM RICHIESTE "+
                      " WHERE AREA = '" + area + "'"+
                      " AND CODICE_RICHIESTA = '" + codice + "'";
    
    String queryIns = "INSERT INTO richieste (codice_richiesta, area,id_tipo_pratica,  data_inserimento, data_scadenza) "+
                      "VALUES ('"+ codice +"','"+ area +"', null, sysdate, null)";*/

    String querySel = "SELECT 1"+
        "  FROM RICHIESTE "+
        " WHERE AREA = :AREA"+
        " AND CODICE_RICHIESTA = :CR";

    String queryIns = "INSERT INTO richieste (codice_richiesta, area,id_tipo_pratica,  data_inserimento, data_scadenza) "+
        "VALUES (:CR,:AREA, null, sysdate, null)";

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      dbOp.setStatement(querySel);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CR",codice);
      dbOp.execute();
      rs = dbOp.getRstSet();
      result = rs.next();
      if (!result) {
        dbOp.setStatement(queryIns);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":CR",codice);
        dbOp.execute();
        dbOp.commit();
      }
      free(dbOp);
    } catch (Exception e) {
      free(dbOp);
      logger.error("GeneraPdf::cercaRichiesta() - Errore:  ["+ e.toString()+"]");
    }
  }

//  /**
//   * 
//   */
//  private void sysPdf(String id_oggetto_file) {
//    DbOperationSQL dbOp = null;
//    ResultSet rst;
//    String query = "";
//    String retval = "";
//
//    query = "SELECT OGFI.ID_OGGETTO_FILE " + 
//            "FROM OGGETTI_FILE OGFI, FORMATI_FILE FOFI " + 
//            "WHERE OGFI.ID_FORMATO = FOFI.ID_FORMATO "+
//            "AND FOFI.NOME = 'SYS_PDF' "+
//            "AND OGFI.ID_OGGETTO_FILE = '"+id_oggetto_file+"' "+
//            "ORDER BY 3 ASC";
//
//    try {
//      dbOp = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//      dbOp.setStatement(query);
//      dbOp.execute();
//      rst = dbOp.getRstSet();
//      if (rst.next()) {
//        retval = rst.getString(1);
//        if (retval == null) {
//          retval = "";
//        }
//      }
//      free(dbOp);
//    } catch (Exception e) {
//      free(dbOp);
//      Util.writeErr("ServletModulistica::sysPdf() ","Area: "+area+" - Modello: "+cm+"- Errore:  ["+ e.toString()+"]");
//      corpoHtml += "Errore in fase di controllo!";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpoHtml += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpoHtml += e.getStackTrace().toString();
//      }
//    }
//    return retval;
//  }

  private void initVu(HttpServletRequest  request) {
    try {
      String nominativo = request.getRemoteUser();
      if (nominativo == null) {
        us      = (String)request.getSession().getAttribute("Utente");
      } else {
        us = cercaUtente(nominativo.toUpperCase());
      }

      if (us == null) {
        us = "";
      }
      if (us.length() == 0) {
        us = "GUEST";
      } 
      us = us.toUpperCase();
      request.getSession().setAttribute("UtenteGDM",us);
      request.getSession().setAttribute("RuoloGDM","GDM");
      vu = new Environment(us, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo("GDM");
    } catch (Exception e) {
      logger.error("Upload::initVu - "+e.toString());
    }
  }

}
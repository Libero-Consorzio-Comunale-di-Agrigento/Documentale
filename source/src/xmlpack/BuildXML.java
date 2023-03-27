/**
 * @Module  Servlet BuildXML
 * @Author  adelmo
 * @Created mercoledì 19 Giugno 2002
 * @Purpose E' il servlet che si occupa di fare l'upload dei file allegati
 * ad una determinata richiesta e che genera i vari oggetti richiesti.
 **/
 
package xmlpack;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
//import java.util.*;
import it.finmatica.modulistica.parametri.*;
import it.finmatica.jfc.dbUtil.*;

public class BuildXML extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

//  private static DbOperationSQL dbOpSQL = null;
  private static InfoConnessione infoConnesione;

  /**
   * 
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    String setupFile = "";
    
    try {
      setupFile = getServletConfig().getInitParameter("setupfile"); 
      Parametri.leggiParametriStandard(setupFile);

      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {   
      // throw new ServletException("File di configurazione non trovato"+ setupFile);
      e.printStackTrace();
    }
  }

  /**
   * Ritorno la variabile SessionDb legata a questo tipo di Servlet.
   */
// COMMENTATA perchè si pensa sia inutile ai fini della servlet

//  static synchronized public SessioneDb getSession() throws Exception{
//    if (sess == null) {
//      // Da qui passa solo se non sono nel servlet (test)
//      java.lang.String setupFile = "c:\\temp\\Setup.ini";
//      Parametri.leggiParametriStandard(setupFile);
//      sess = SessioneDb.getNewTempInstance(
//                  Parametri.SPORTELLO_DRIVER, 
//                  Parametri.SPORTELLO_DSN, 
//                  Parametri.USER, 
//                  Parametri.PASSWD);
//    }
//    return sess;
//  }

  /**
  * Tramite il paraemtro type posso richiedere diversi tipi di elaborazione.
  * @parameter tipo
  *   1  ->> creazione del solo file per il protocollo (in cui l'ADSXML sarà un parametro vuoto
  *   2  ->> creazione del solo file ADSXML
  *   3  ->> creazione di entrambi i file 
  * @parameter redir pagina a cui passare una volta finito di elaborare la richiesta
  */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    DbOperationSQL dbOpSQL = null;
    String tipo,
           idPr,
           codXML,
           lAllegati,
           ar;
                
    response.setContentType(CONTENT_TYPE);
    PrintWriter out = response.getWriter();

    // Creo l'oggetto DbOperationSQL
//    try {
//      dbOpSQL = new DbOperationSQL(Parametri.ALIAS,Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//    } catch (Exception e) {
//      out.println("BuildXML::doGet() (1) - Attenzione: si è verificato un errore:"+e.toString());
//      out.close();
//      return;
//    }

    // Creo l'oggetto InfoConnesione
    infoConnesione = new InfoConnessione(Parametri.ALIAS,
                                         Parametri.SPORTELLO_DSN, 
                                         Parametri.USER, 
                                         Parametri.PASSWD);
      
    tipo = request.getParameter("type");
    if (tipo == null){
      // Se non specificato lo metto a 0 per non elaborare nulla
      tipo = "0";
    }
    idPr = request.getParameter("pr");            // id di pratica: mandatory
    ar = request.getParameter("area");            // area di competenza: mandatory
    if (idPr == null || ar == null){
      // Non elaboro nulla perchè manca un dato
      tipo = "0"; 
    }
    codXML = request.getParameter("codxml");
    if (codXML == null){
      // Default value
      codXML = "ADSXML";
    }
    lAllegati = request.getParameter("allegati");
    if (lAllegati == null){
      // Default value
      lAllegati = "";
    }
      
    switch (Integer.valueOf(tipo).intValue()) {
      case 1:
        protocolloXML(idPr, ar, "", lAllegati, request);
      break;
        
      case 2:
        adsXML(idPr, ar, lAllegati, request);
      break;
        
      case 3:
        adsXML(idPr, ar, lAllegati, request);
        protocolloXML(idPr, ar, codXML, lAllegati, request);            
      break;
      
      default:
        // tipo = 0 deve essere uno dei casi di default
        out.println("<html>");
        out.println("<head><title>BuildXML</title></head>");
        out.println("<body>");
        out.println("<p>ELABORAZIONE NON SUPPORTATA (MANCANO DATI O TIPO NON SUPPORTATO)</p>");
        out.println("</body></html>");
        out.close();
      break;
    }

    // Prima di uscire libero la connessione.
//    try { 
//      dbOpSQL.close(); 
//    } catch (Exception e) {  }  
  }

  /**
  * La doPost fa la stessa cosa della doget in questo modo il servlet puo' essere richiamato dal client
  * con il metodo che peferisce.
  */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  /**
  * Scrittura del file XML per il passaggio dei dati alle procedure ADS
  */
  private void adsXML(String idPr, String area, String lAllegati, HttpServletRequest request){
    String      userId,
                codXML;

    userId = request.getParameter("userId");      // id utente
    if (userId == null){
      // Default value
      userId = "";
    }
    codXML = request.getParameter("codxml");
    if (codXML == null){
      // Default value
      codXML = "ADSXML";
    }

    PraticaXML pxml = new PraticaXML(infoConnesione, idPr, area, userId, codXML, lAllegati);
    pxml.caricaDaDB();
    pxml.generaXML();
    try {
      pxml.scriviXMLSuDb(infoConnesione);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }

  /**
   * Scrittura del file XML per il passaggio dei dati al protocollo 
   */
  private void protocolloXML(String idPr, String area, String adsxml, String lAllegati, HttpServletRequest request){
    String      userId,
//                codXML,
                oggetto,
                tDoc,
                mov,
                uffSm,
                mittDest,
                uniProt,
                appl,
                classif,
                alle;
                
    userId = request.getParameter("userId");      // id utente
    if (userId == null){
      // Default value
      userId = "";
    }
    oggetto = request.getParameter("oggetto");    // Oggetto
    if (oggetto == null){
      // Default value
      oggetto = "";
    }
    tDoc = request.getParameter("tDoc");          // tipo documento
    if (tDoc == null){
      // Default value
      tDoc = "";
    }
    mov = request.getParameter("mov");            // movimento
    if (mov == null){
      // Default value: ARR
      mov = "ARR";
    }
    uffSm = request.getParameter("uffSm");        // ufficio smistamento
    if (uffSm == null){
      // Default value
      uffSm = "";
    }
    mittDest = request.getParameter("mittDest");   // mittente Destinatario
    if (mittDest == null){
      // Default value
      mittDest = "";
    }
    uniProt = request.getParameter("uniProt");      // unità protocollo
    if (uniProt == null){
      // Default value
      uniProt = "";
    }
    appl = request.getParameter("appl");            // applicativo esterno
    if (appl == null){
      // Default value
      appl = "";
    }
    classif = request.getParameter("classif");      // classificazione
    if (classif == null){
      // Default value
      classif = "";
    }
    
    alle = request.getParameter("alle");            // allegati
    if (alle == null){
      // Default value
      alle = "";
    }
    
    ProtocolloXML prXML = new ProtocolloXML(infoConnesione, idPr, area,
                      new java.sql.Date(System.currentTimeMillis()), oggetto, 
                      tDoc, mov, uffSm,
                      mittDest,uniProt, appl, 
                      userId, classif, adsxml, "ProtocolloXML", lAllegati);
    try {
      prXML.generaXML();
      prXML.scriviXMLSuDb(infoConnesione);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
}
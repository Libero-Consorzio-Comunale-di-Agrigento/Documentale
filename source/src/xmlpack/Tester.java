package xmlpack;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.modulistica.parametri.*;
import java.io.FileOutputStream;

/**
 * 
 */
public class Tester {

  /**
   * 
   */
  public Tester() {
  }

  /**
   * 
   */
  public static void main(String[] args) {
    String      setupFile,
                area,
                keyRichiesta;               
//    Tester tester = new Tester();
//    DbOperationSQL dbOpSQL = null;
    InfoConnessione  infoConnesione;

    setupFile = "c:\\temp\\Setup.ini";
    keyRichiesta = "SERJ";
    area = "GSD";
    
    try {
      Parametri.leggiParametriStandard(setupFile);
      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    
//      dbOpSQL = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      infoConnesione = new InfoConnessione(Parametri.ALIAS, 
                                           Parametri.SPORTELLO_DSN, 
                                           Parametri.USER, 
                                           Parametri.PASSWD);
      
      PraticaXML pxml = new PraticaXML(infoConnesione, keyRichiesta, area, "SERJ", "ProtocolloXML","A1@#@a2");
      
      pxml.aggiungiAllegato("AllegatoTest1");
      pxml.aggiungiAllegato("AllegatoTest2");
      System.out.println("Testmail: 2");
      pxml.caricaDaDB();
      System.out.println("Testmail: 3");
      pxml.generaXML();
      System.out.println("Testmail: 4");
      pxml.scriviXMLSuDb(infoConnesione);
      System.out.println("Testmail: 5");
      
      FileOutputStream fo = new FileOutputStream("C:\\temp\\prova.xml");
      pxml.writeStream(fo);

      /*
       // Parametri per un nuovo oggetto di tipo ProtocolloXML:
       //   String pCodiceRichiesta 
       //   String pArea
       //   Date   pData 
       //   String pOggetto 
       //   String pTipoDocumento 
       //   String pMovimento
       //   String pUfficioSmistamento 
       //   String pMittenteDestinatario
       //   String pUnitaProtocollo 
       //   String pApplicativoEsterno 
       //   String pUtente 
       //   String pClassificazione 
       //   String pDocumentoPrincipale
       //   String pCodiceXML 
       //   String pAllegati
                       
      ProtocolloXML   prXML = new ProtocolloXML(
                                     keyRichiesta, 
                                     area,
                                     new java.sql.Date(System.currentTimeMillis()), 
                                     "Oggetto di test", 
                                     "Tipo documento di test", 
                                     "Movimento di test",
                                     "Ufficio smistamento di test", 
                                     "Mittente 1@#@Mittente 2",
                                     "UP di  test", 
                                     "APP ESTERNO di test", 
                                     "ADELMO", 
                                     "Class di TEST", 
                                     "ADSXML", 
                                     "ProtocolloXML", 
                                     "Allegato 1@#@Allegato 2@#@Allegato 3");

      prXML.generaXML();
      prXML.scriviXMLSuDb();
      FileOutputStream  fo2=new FileOutputStream("C:\\temp\\protoprova.xml");
      prXML.writeStream(fo2);      
      

      OggettoXML  og = new OggettoXML();
      og.readFromFile("C:\\temp\\protoprova.xml");
      FileOutputStream fo3=new FileOutputStream("C:\\temp\\protodaproto.xml");
      og.writeStream(fo3);
      */

    } catch(Exception e) {   
      e.printStackTrace();
    }

  }
}
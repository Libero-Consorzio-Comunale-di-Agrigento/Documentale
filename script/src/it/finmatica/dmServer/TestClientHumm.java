package it.finmatica.dmServer;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

public class TestClientHumm 
{
  
  public static void main(String[] args) throws Exception 
  {

       // CASO HUMM
       Profilo p = new Profilo("DEF_PROTO_PANTAREI", "AREAGDM");
       //Profilo p2 = new Profilo("DEF_PROTO_PANTAREI", "AREAGDM");
       //Profilo p = new Profilo("DEF_DOC_GENERICO", "AREAGDM");
       //p.initVarEnv("UT_PROTO_ADS","UT_PROTO_ADS","Fatturazione", "ADS_HUMM", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_humm.properties");         
       //p.initVarEnv("UT_PROTO_BO","UT_PROTO_BO","Fatturazione", "ADS_HUMM", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_humm_alternativo.properties");         
       p.initVarEnv("UT_PROTO_BO","UT_PROTO_BO","Fatturazione", "ADS_HUMM", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_humm_minimale.properties");         
       //p2.initVarEnv("UT_PROTO_ADS","UT_PROTO_ADS","Fatturazione", "ADS_HUMM", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_humm.properties");         

       if ("1".equals("1")) {   //AGGIUNGI
         
                   try {
                     p.settaValore("DOCNAME","MARIASSUNTA.doc");
                     p.settaValore("TYPE_ID","documento");
                     p.settaValore("COD_ENTE","C_BO");
                     //p.settaValore("COD_ENTE","PROTO_BO");
                    
                    // p.settaValore("COD_ENTE","E000");
                    // p.settaValore("COD_AOO","AOO-BO");
                    
                    // p.settaValore("COD_AOO","AOO_CBO");
                     //p.settaValore("COD_AOO","AOO1");
                     //p.settaValore("COD_AREA","AREA1");
                     //p.settaValore("DES_AREA","DESCRIZIONE AREA1-AOO1-E001");
                     p.settaValore("STATO_PANTAREI","2");
                     
                     /*p.settaValore("COD_TITOLARIO","965");

                     p.settaValore("ANNO_PG","2005");
                     p.settaValore("NUM_PG","404");
                     p.settaValore("OGGETTO_PG","Ciao");
                     p.settaValore("NUM_FASCICOLO","1");
                     p.settaValore("ANNO_FASCICOLO","2004");                              
                     p.settaValore("DES_FASCICOLO","ciao pantaciata funziona");  
                     p.settaValore("STATO_PANTAREI","4");
                     
                     p.settaACL("G.STANZA",Global.COMPLETE_ACCESS);*/
                     //p.settaACL("R.BISIO",Global.COMPLETE_ACCESS);

                     // In fase di inserimento prende al massimo solo 2 file, gli altri vengono
                     // ignorati.
                     // Per inserire gli allegati bisogna fare la salva, settare gli allegati
                     // e rifare salva (Hummingbird la prende come un'aggior\na)
                    // p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\DOCUMENTO.doc"); // AGGIUNGE IL FILE PRINCIPALE
                    // p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\DOCUMENTO.doc"); // AGGIUNGE IL FILE PRINCIPALE
                     
                     LetturaScritturaFileFS f = new LetturaScritturaFileFS("C:\\TOMETA\\francesconi\\DatiDoc.xml");
                     
                     p.setFileName("DatiDoc.xml",f.leggiFile());   
                     
                     
                     LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("C:\\TOMETA\\francesconi\\apertura_albergo.pdf.p7m");
                     p.setFileName("apertura_albergo.pdf.p7m",f2.leggiFile()); // AGGIUNGE IL FILE P7M    
                     
                     //p.setFileName("c:\\temp\\pino.doc");
            
                     if (p.salva().booleanValue())
                         System.out.println("N° Documento: " + p.getDocNumber());
                     else {
                         System.out.println(p.getError());
                         System.out.println(p.getCleanError());  
                     }
                     }
                     catch (Exception e) 
                     {
                       System.out.println(p.getError());
                       System.out.println(p.getCleanError()); 
                     }                
                     
       }   
 
       if ("2".equals("")) {        // AGGIORNAMENTO DOC 
          
              try {
                   p.setDocNumber("88");
                   p.settaValore("DOCNAME","MARIASSUNTA.doc");
                   p.settaValore("TYPE_ID","DOCUMENTO");
                   p.settaValore("COD_ENTE","C_BO");

                   p.settaValore("COD_AOO","AOO_CBO");

                   p.settaValore("COD_TITOLARIO","3");
                   p.settaValore("NUM_FASCICOLO","1");
                   p.settaValore("ANNO_FASCICOLO","2005");
                   p.settaValore("DES_FASCICOLO","fascicolo 1 per hummingbird");

                   p.settaValore("ANNO_PG","2005");
                   p.settaValore("NUM_PG","800");
                   p.settaValore("OGGETTO_PG","Ciaoaaaa");
                   p.settaValore("STATO_PANTAREI","4");

                   //p.settaACL("M.BONVI",Profilo.REANDONLY_ACCESS);
                  LetturaScritturaFileFS f = new LetturaScritturaFileFS("C:\\TOMETA\\francesconi\\Segnatura_cittadino.xml");
                    
                  p.setFileName("Segnatura_cittadino.xml",f.leggiFile()); // AGGIUNGE UN ALLEGATO
                   //p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\Allegato2.DOC"); // AGGIUNGE UN ALLEGATO

                   if (!p.salva().booleanValue()) {
                       System.out.println(p.getError());
                       System.out.println(p.getCleanError()); 
                   }
         
               }
               catch (Exception e) 
               {
                   System.out.println(p.getError());
                   System.out.println(p.getCleanError()); 
               }
          
      }

      if ("3".equals("")) {        // ACCEDI DOC TRAMITE ID UNIVOCO
      
              try {

               p.setDocNumber("1728");

               if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {
         
                   System.out.println(p.getCampo("COD_ENTE"));
                   System.out.println(p.getCampo("STATO_PANTAREI"));

                   System.out.println(p.getFile());
                   System.out.println(p.getRiferimenti());
               }
               else
                   System.out.println("Impossibile accedere");
               }
               catch (Exception e) 
               {
                   System.out.println(p.getError());
               }

             
          
       
     }   

     if ("4".equals("")) {   //AGGIUNGI E AGGIORNA
         try { 
           p.settaValore("DOCNAME","doc_prova.doc");
           p.settaValore("TYPE_ID","DOCUMENTO");
           p.settaValore("COD_ENTE","C_BO");
           p.settaValore("STATO_PANTAREI","2");                              

           p.settaACL("G.STANZA1",Global.COMPLETE_ACCESS);
           //p.settaACL("R.BISIO",Global.COMPLETE_ACCESS);
     
           p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\DOCUMENTO.doc"); // AGGIUNGE IL FILE PRINCIPALE
           p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\DOCUMENTO.p7m"); // AGGIUNGE IL FILE P7M         
            
           if (p.salva().booleanValue())
               System.out.println("N° Documento: " + p.getDocNumber());
           else {
               System.out.println(p.getError());
               System.out.println(p.getCleanError());  
           }

           p.settaValore("STATO_PANTAREI","4");
           p.settaValore("ANNO_PG","2004");
           p.settaValore("REGISTRO_PG","339");
           p.settaValore("NUM_PG","330");    
           p.settaValore("OGGETTO_PG","FFFAAA");
           p.settaValore("COD_AOO","AOO_CBO");

           p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\Allegato1.DOC"); // AGGIUNGE UN ALLEGATO
           //p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\ADOCUMENTO.p7m");
           //p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\Allegato2.DOC"); // AGGIUNGE UN ALLEGATO

           if (!p.salva().booleanValue()) {
             System.out.println(p.getError());
             System.out.println(p.getCleanError()); 
           }
           
           }
           catch (Exception e) 
           {
             System.out.println(p.getError());
             System.out.println(p.getCleanError()); 
           }
           
      }   

      if ("5".equals("")) {        /* acl nominali */
        try {

               p.CreaSincro("PROV_BOLOGNA-000");
               p.AggiungiCompetenze("g.stanza", "35", "O", "0");
               p.AggiungiCompetenze("f.prediucci", "35", "N", "2");
               p.AggiungiCompetenze("g.stanza", "41", "O", "0");
               //p.AggiungiCompetenze("g.stanza", "43", "O", "2");              
               //p.AggiungiCompetenze("f.prediucci", "41", "N", "2");
               //p.AggiungiCompetenze("g.stanza", "35", "N", "2");
               //p.AggiungiCompetenze("g.stanza", "41", "N", "2");
               //p.AggiungiCompetenze("g.stanza", "43", "N", "-1");
              
               p.GeneraXML("S:\\SI4\\GD4\\DMCLient\\Allegati\\xmlprova.xml");
       }
        catch (Exception e) 
        {
             System.out.println(p.getError());
        }  

     }

  } // End Main
  
} // End Class
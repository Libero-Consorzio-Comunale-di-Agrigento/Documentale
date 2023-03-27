package it.finmatica.dmServer;

import it.finmatica.dmServer.management.*;
import java.sql.*;

public class NewMain 
{
  final static String AGGIUNGIDOC="1";
  final static String AGGIORNADOC="2";  
  final static String ACCEDIDOC  ="3"; 
  final static String CONNECTION_EXTERN="EXTERN";
  final static String CONNECTION_STANDARD="STANDARD";
  static Environment  vu;
  
  public NewMain(){}

  public static void main(String[] args)
  {
         String caso=AGGIORNADOC;
         String casoConnection=CONNECTION_STANDARD;
         Connection conn=null;
         
         try {

           //------------------------------------CONNESSIONE ESTERNA-------------------------------------
           if (casoConnection.equals(CONNECTION_EXTERN)) {
               Class.forName("oracle.jdbc.driver.OracleDriver");
               conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
               //conn=DriverManager.getConnection("jdbc:oracle:thin:@achille:1521:orcl8","GDM","GDM");
           }                      
           
           //--------------------------------CARICAMENTO DELL'ENVIRONMENT--------------------------------
           if (casoConnection.equals(CONNECTION_EXTERN)) 
               vu = new Environment("GDM","GDM","MODULISTICA", "ADS", null,conn);                                                  
           else
               vu = new Environment("GDM","GDM","MODULISTICA", "ADS", null,"S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");                                   
               //vu = new Environment("GDM","GDM","MODULISTICA", "ADS", null,"jdbc/gdm");                                   
               //vu = new Environment("GDM","GDM","MODULISTICA", "ADS", null,"S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_achille.properties");                                   
               //vu = new Environment("GDM","GDM","MODULISTICA", "ADS", null,"S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_sothixzoli.properties");                                   
               
           //--------------------------------Caso 1 - Aggiungi Documento--------------------------------
           if (caso.equals(AGGIUNGIDOC)) {
              //Costruttore
              AggiungiDocumento ad = new AggiungiDocumento("F1","MANNY",vu);              
              
              
              //Aggiunta Dati
              ad.aggiungiDati("COGNOMEPILOTA","Elton Senna");
              ad.aggiungiDati("NOMEPILOTA","Ayrton Mannella");
                                          
              //Aggiunta Oggetti File
              /*LetturaScritturaFileFS f = new LetturaScritturaFileFS("c:\\sqlnet.log"); 
              ad.aggiungiAllegato(f.leggiFile(), "sqlnet.fil");*/
              
              /*LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("c:\\tmp.txt"); 
              ad.aggiungiAllegato(f2.leggiFile(), "tmp.fil");*/
                            
              //LetturaScritturaFileFS fImpronta = new LetturaScritturaFileFS("c:\\tmp.txt");               
              //ad.aggiungiAllegatoeImpronta(fImpronta.leggiFile(),fImpronta.leggiFile(),"tmp.txt");
              
              //Aggiunta di ACL
              //ad.aggiungiACL("AA4",Global.COMPLETE_ACCESS);
              //ad.aggiungiACL("ALE",Global.NO_ACCESS);
                              
              //Salva Documento              
              System.out.println("-->"+ad.salvaDocumentoBozza());              
           }
           //--------------------------------Caso 2 - Aggiorna Documento--------------------------------
           else if (caso.equals(AGGIORNADOC)) {
              //Costruttore
              AggiornaDocumento ad = new AggiornaDocumento("2907",vu);
              
              //ad.cancellaAllegato("das.P7M");
              
              //Aggiornamento dati
              ad.aggiornaDati("COGNOMEPILOTA",null);
              ad.aggiornaDati("COGNOMEPILOTA","aSennap");
              
              //Aggiunta Oggetti File
              /*LetturaScritturaFileFS f = new LetturaScritturaFileFS("c:\\CCS.log"); 
              LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("c:\\sqlnet.log"); 
              LetturaScritturaFileFS f3 = new LetturaScritturaFileFS("c:\\poma.txt"); 
              
              ad.aggiungiAllegato(f.leggiFile(), "COMLOG.log");*/
            
              //ad.aggiornaAllegato("1346",f.leggiFile(),"CCS.log");
              
              //ad.aggiungiAllegato(f.leggiFile(),"CCS.log");
              //ad.aggiungiAllegato(f2.leggiFile(),"sqlnet.log",null,null,"S");///*,"Allegato2.DOC"*/);
              //ad.aggiungiAllegato(f3.leggiFile(),"poma.txt",null,null,"S");
              
              //ad.aggiungiAllegato(f3.leggiFile(),"poma_.txt");
              //ad.aggiungiAllegato(f2.leggiFile(),"sqlnet.log","COMLOG.log");
              //ad.aggiungiAllegato(f3.leggiFile(),"poma.txt","COMLOG.log");
              
              //KOFAX
              //ad.setScannedDocument();
              
              //Riferimenti
              //ad.aggiungiRiferimento("1963",Global.RIF_VERSIONE);
              
              //Aggiunta di ACL
              //ad.aggiungiACL("AA4",Global.COMPLETE_ACCESS);
              //ad.aggiungiACL("ALE",Global.NO_ACCESS);
              
              //ad.cancellaAllegato("08X30007.P7M");
              //ad.cancellaAllegato("Nuova_Croma_32p.P7M");
              
              //Salva Documento              
              System.out.println("-->"+ad.salvaDocumentoBozza());                                       
           }
           else if (caso.equals(ACCEDIDOC)) {
           
              AccediDocumento ad = new AccediDocumento("842",vu);
               
              ad.accediDocumentoAllegati();
                           
             // System.out.println("-->"+ad.leggiValoreCampo("COGNOMEPILOTA")); 
              java.util.Vector v = ad.listaIdOggettiFile();

				  for(int i=0;i<v.size();i++) 
          System.out.println("--->"+ad.isOggettoFileVisibile((String)v.get(i)));
                    /* if (ad.isOggettoFileVisibile((String)v.get(i))) {
					    //isStream = ad.leggiOggettoFile((String)v.get(i));
					    //nomeFile = ad.nomeOggettoFile((String)v.get(i));
						//Forzo l'usicta
						i=v.size();
					 }*/
           }
         
           if (casoConnection.equals(CONNECTION_EXTERN)) {           
              conn.rollback();
              //conn.commit();
              conn.close();
           }
         }
         catch (Exception e) 
         {    
        
              e.printStackTrace();
         }
      
         
       
 }
}
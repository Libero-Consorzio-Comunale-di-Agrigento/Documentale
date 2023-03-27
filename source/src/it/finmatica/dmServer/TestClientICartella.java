package it.finmatica.dmServer;

import java.sql.*;

import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.management.*;
import java.util.*;


public class TestClientICartella 
{
  final static String CONNECTION_EXTERN="EXTERN";
  final static String CONNECTION_STANDARD="STANDARD";

  public static void main(String[] args) throws Exception 
  {
       long caso=2;            
       String casoConnection=CONNECTION_EXTERN;
       Connection conn=null;
       Connection conn2=null;

       //------------------------------------CONNESSIONE ESTERNA-------------------------------------
       if (casoConnection.equals(CONNECTION_EXTERN)) {
           Class.forName("oracle.jdbc.driver.OracleDriver");
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
           
           //JVMSEGR
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.30.39:1521:ORCL","GDM","GDM");
           //PANDORA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@pandora:1521:gs","GDM","GDM");
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.70.111:1521:ORCL","GDM","GDM");
           
           //DBSEGR
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.31:1521:GDMTEST","GDM","GDM");                                 
           
           //JVMEFESTO
        //   conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");
           
           //PANDORA-PRMOD
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.7:1521:PRMOD","GDM","GDM");

           //BONETTI
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.31.24:1521:FITRA","GDM","GDM");
           
           conn=DriverManager.getConnection("jdbc:oracle:thin:@test-efesto:1521:ORCL","GDM","GDM");
           conn.setAutoCommit(false);
           
           //conn2=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");
           //conn2.setAutoCommit(false);
       }                      
       
       //--------------------------------CARICAMENTO DELL'ENVIRONMENT--------------------------------
       
       //COSTRUTTORI NEW


          //Inserimento con path folder
           /*ICartella Ic = new ICartella("GDMSYS","CartellaStandard",
                                          Global.ROOT_SYSTEM_FOLDER,"ancora",
                                          "BONU4");
           ICartella Ic2 = new ICartella("GDMSYS","CartellaStandard",
                                          Global.ROOT_SYSTEM_FOLDER,"ancora",
                                          "BONU5");  */    
          //Inserimento Cartella di area AD4
          /*ICartella Ic = new ICartella("GDMSYS","CartellaStandard",
                                          Global.ROOT_SYSTEM_FOLDER,"",
                                          "CIAO A TUTTI"); */
       
       /*ICartella Ic = new ICartella("SEGRETERIA","DIZ_UNITA",
                                          Global.ROOT_SYSTEM_FOLDER,"",
                                          "CIAO A TUTTI"); */
       
      /* ICartella Ic = new ICartella("GDMSYS","Fascicolo",
                                          Global.ROOT_SYSTEM_FOLDER,"",
                                          "AIUTOOOOO");
          */
          //Inserimento di una workspace ... passo come root e percorso la stringa vuota ""
          /*ICartella Ic = new ICartella("GDMSYS","WRKSPStandard",
                                       "","",
                                       "WRKSP 88888");*/
          
          //Inserimento di una nuova cartella in una workspace diversa 
          //da sys o user, in tal caso specifico il nome della wrksp
          /*ICartella Ic = new ICartella("GDMSYS",
        		  					   "CartellaStandard",
                                       "Esempio di wrksp",
                                       "",
                                       "Sotto la nuova wrksp");*/
       
         //Inserimento sapendo idUpFolder             
         /*ICartella Ic = new ICartella("GDMSYS","CartellaStandard","Workspace moficata nel nome"
        		                      ,"","Sotto la mia workspace 2");     */                                                                      
          
          //Inserimento sapendo idUpFolder, cm ,area e cr del profilo cartella (già creato prima)   
         /* ICartella Ic = new ICartella("SEGRETERIA","DIZ_CLASSIFICAZIONE","27497",
                                       "Protocollo"*//*Global.ROOT_SYSTEM_FOLDER*//*,27496);*/    
          
          //Inserimento sapendo  cm ,area e cr del profilo cartella (già creato prima) 
          //La cartella verrà inserita sotto la wrksp...manca idUpFolder
       /*   ICartella Ic = new ICartella("SEGRETERIA","DIZ_CLASSIFICAZIONE","27498",
                                       "Protocollo");*/            
       
        /* ICartella Ic = new ICartella("GDMSYS","CartellaStandard","4538",
                                     Global.ROOT_SYSTEM_FOLDER,275);*/

       //COSTRUTTORI MODIFY
    //   ICartella Ic = new ICartella("FASCICOLO_PERSONALE","CART_FASC","Fascicolo Personale","Elenco Fascicoli", "CARRARO SARA (1100883)");
     //  ICartella Ic = new ICartella("10438637");
       
      // 
     
       // ICartella Ic = new ICartella("GDMSYS", "CartellaStandard", "TESTADS","","MANNYX");
       
       ICartella Ic = new ICartella("316");
       
       //ICartella Ic = new ICartella("SEGRETERIA","FASCICOLO","10015547","Titolario",28);
       
       //ICartella Ic2 = new ICartella("4563");
       //ICartella Ic = new ICartella("SEGRETERIA","DIZ_CLASSIFICAZIONE","633");           
   //    ICartella Ic = new ICartella("SEGRETERIA","FASCICOLO","SEGRETERIA-888FGTY-A","Titolario",10000461);
       //Costruttore Modify ma a partire dal path
       
        //ICartella Ic = new ICartella(Global.ROOT_SYSTEM_FOLDER,"sandro");
       // ICartella Ic = new ICartella("Fascicolo Personale","Elenco Fascicoli\\CARRARO SARA (1100883)");
       //ICartella Ic = new ICartella("URP_GALLIERA","CARTELLA_SEGN_URP","Gestione RM","Cartella Risoluzioni","prova2");
     //  ICartella Ic = new ICartella("GESTIONE_FIRME","CARTELLE_FIRME", "Firme digitali","2. Cartelle firma", "Pippo" + " (" + "PAPPO" + ")");
    
       if (casoConnection.equals(CONNECTION_EXTERN)) 
          Ic.initVarEnv("GDM",null, conn);
       else
          Ic.initVarEnv("GDM","GDM", "c:\\temp\\gd4dm.properties");

      // Ic2.initVarEnv("GDM","GDM", conn);
       //Ic2.initVarEnv("GDM","GDM", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");
       // Caso inserimento cartella standard
       if (caso==1) {
           try {         
        	   
        	 //  Ic.settaACL("UTENT_PR1", Global.READONLY_ACCESS);
      		 //Ic.settaACL("GDM", Global.COMPLETE_ACCESS);
      		 //Ic.settaACL("GDM", Global.INFOLDER_ACCESS);
        	 //  Ic.insert();
          
        	 //  Ic.addValue("NOMINATIVO", "PIPPO");
        	 //  Ic.addValue("MATRICOLA", "1440");
        	  // Ic.settaACL("GDM", Global.COMPLETE_ACCESS);
        	  
        	   System.out.println("************** "+Ic.getIdentifierFolder());
        	   
        	   
        	   
        	   //Ic.addValue("NOME","Prova Cartella da ICartella HHHHAAAA");
             //Ic.addValue("ANOME","Prova Cartella da ICartella HHHHAAAA");
             /*Ic.addInObject("2193","D");
             Ic.addInObject("4751","C");
             Ic.addInObject("293","Q");*/
        	 //Ic2.addValue("aa","aa");
             //System.out.println(Ic.getStato());
             //Ic.addInObject("198","Q");             
             /*Ic.settaACL("AA4",Global.COMPLETE_ACCESS);
             Ic.settaACL("ALE",Global.NO_ACCESS);
             Ic.settaACL("ALAN",Global.NORMAL_ACCESS);*/
        // System.out.println( Ic.getRootFolder());
          //   Ic.insert();
            // conn.rollback();
             //conn.rollback();
             /*System.out.println("--->FINE");
             System.out.println("--->"+Ic.getProfileFolder());
             
             Profilo p = new Profilo(Ic.getProfileFolder());
             p.initVarEnv("RPI","RPI",conn);
             if (p.accedi(Global.ACCESS_NO_ATTACH).booleanValue())
            	 System.out.println("-->"+p.getCampo("NOME"));
             else
            	 System.out.println("ERRORE-->"+p.getError());*/
             Ic.insert();
            // Ic2.insert();
           }
           catch (Exception e) {
              if (casoConnection.equals(CONNECTION_EXTERN)) {
                 //conn.rollback();            
                 conn.close();
              }
             e.printStackTrace();
             return;
           }
      }
      // Caso update cartella
      if (caso==2) {
            try {
       
            // Ic.addValue("NOME","controlli");
             //Ic2.addValue("NOMEbbb","DDD");
                    
            /*	Calendar cal = Calendar.getInstance();
            	java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());*/
                 //java.sql.Date jsqlD = new java.sql.Date(now.getTime());
                 
              //   Ic.addValue("DAL",now);
                 
                 //java.math.BigDecimal d = new java.math.BigDecimal(9999999999.99999);
                 
            // Ic.addInObject("643","C");
             /*Ic.addInObject("548","C");
             Ic.addInObject("250","Q");             
             Ic.addInObject("199","Q");
             Ic.addInObject("243","Q");  
             Ic.addInObject("135","Q");  */
             //Ic.settaCollegamento("862");
             //Ic.copiaOggetti("D32052");
             //Ic.settaACL("AA4",Global.NO_ACCESS);
             //Ic.spostaOggetti("43389");
            	
           // 	 System.out.println(Ic.getElementInFolder("T",null,null));
            	
            	/*Vector v = Ic.getInElementList();
            	
            	for (int i=0;i<v.size();i++) {
            		 System.out.println("-->"+((Valori)v.get(i)).getKey());*/
            	//}
            	
            	//Ic.settaRiferimento("4604",Global.RIF_VERSIONE);
            // Ic.setDeleteRiferimento("4604",Global.RIF_VERSIONE);
             //Ic.creaCollegamentoDesktop("NUOVO@TEST@TEST");
            	
            	//Ic.spostaOggetti("D15823768,10438838");
          // Ic.addValue("NOME", "ejjj"); 	
           // Ic.update();
            	//Ic.removeCompetenza("MARIKA", "L");
            	Ic.settaACL("MARIKA", Global.COMPLETE_ACCESS);
            //  Ic.addInObject("687", "D")  ;          
              Ic.update();
             //Ic2.update();
           }
           catch (Exception e) {
              if (casoConnection.equals(CONNECTION_EXTERN)) {
                 conn.rollback();            
                 conn.close();
              }
             e.printStackTrace();
             return;
           }
      }
      // Caso elimina cartella
      if (caso==3) {
            try {            
           //  Ic2.delete();
            // Ic.delete();
           }
           catch (Exception e) {
              if (casoConnection.equals(CONNECTION_EXTERN)) {
                 //conn.rollback();            
                // conn.close();
              }
             e.printStackTrace();
             return;
           }
      }          
      if (casoConnection.equals(CONNECTION_EXTERN)) {
         //conn.rollback();
        // conn.commit();
        // conn.close();
      }
      
   /*   Profilo p = new Profilo("15823768");
      
      
      p.initVarEnv("GDM",null,"c:\\temp\\gd4dm.properties");
      
      p.accedi(Global.ACCESS_NO_ATTACH);
      
      p.settaValore("TEXT_AREA", "ssgggsssdddcfcfssaa");
      
      
      p.salva();*/
      
      conn.commit();
      
  }
}
package it.finmatica.dmServer;

//import it.finmatica.dmServer.util.Global;
import java.sql.*;

import it.finmatica.dmServer.util.Global;

import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.motoreRicerca.*;
import it.finmatica.dmServer.util.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestClientIQueryFinmatica
{
  final static String CONNECTION_EXTERN="EXTERN";
  final static String CONNECTION_STANDARD="STANDARD";

  public static void main(String[] args) throws Exception 
  {
         String caso="4";         
         String casoConnection=CONNECTION_EXTERN;
         String ini="S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties";
         ElapsedTime elpsTime;
         Connection conn= null;
         //Environment vu = null;

         //------------------------------------CONNESSIONE ESTERNA-------------------------------------
         if (casoConnection.equals(CONNECTION_EXTERN)) {
           Class.forName("oracle.jdbc.driver.OracleDriver");
                      
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.30.20:1521:orcl","GDM","GDM");       
          
           //CONNESSIONE SICILIA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");       
           
           //JVMSEGR
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.19:1521:PRMOD","GDM","GDM");                                 

           //DBTESTSEGR
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.31:1521:GDMTEST","GDM","GDM");                                 
                      
           //PANDORA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.64.170:1521:GDMTEST","GDM","GDM");
           
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.8:1521:ORCL","GDM","GDM");       

          //CONNESSIONE ACHILLE
          //conn=DriverManager.getConnection("jdbc:oracle:thin:@achille:1521:orcl8","GDM","GDM");
           
           //PANDORA-PRMOD
       //    conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.7:1521:PRMOD","GDM","GDM");
           
           //JVMEFESTO
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");
           
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@SVI-ORA03:1521:GDMTEST","GDM","GDM");
             //MANNELLA73
           //  conn=DriverManager.getConnection("jdbc:oracle:thin:@10.96.60.105:1521:ORCL","GDM","GDM");


        // //as02
             conn=DriverManager.getConnection("jdbc:oracle:thin:@test-agspr-db02.finmatica.local:1521:ORCL","GDM","GDM");
           
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora04:1521:jsuite","GDM","GDM");

         //  conn=DriverManager.getConnection("jdbc:oracle:thin:@10.27.45.50:1521:PERV10","GDM","GDM");
  
        // conn=DriverManager.getConnection("jdbc:oracle:thin:@masterags:1521:ORCL","GDM","GDM");
           

         }                      
         //----------------------------------FINE CONNESSIONE ESTERNA----------------------------------
                                   
         
       /*  IQuery iq;
         try {
        	   iq = new IQuery("MAIL", "FATTUREPA.ENTE");
        	  iq.initVarEnv("gdm", "gdm", conn);
        	  iq.escludiControlloCompetenze(true);
        	  iq.addCampo("MESSAGE_ID", "<1177857296.1360219366.1427982105485vliaspec06@legalmail.it>");
          iq.escludiControlloCompetenze(true);
          System.out.println(iq.ricerca().booleanValue());
          System.out.println(iq.isQueryTimeOut());
         } catch (Exception e) {
          e.printStackTrace();
         }
         
         if (1==1) return;*/
         //COSTRUTTORI NEW
              
       //  IQuery Iq = new IQuery("GDMSYS","QueryStandard",Global.ROOT_SYSTEM_FOLDER,"Provsddda Query Avanzata");
                           
         //COSTRUTTORI MODIFY, DELETE
         
      //   IQuery Iq = new IQuery();
         
        // IQuery Iq =  new IQuery("M_ORIZZONTALE", "TESTADS");
        // IQuery Iq = new IQuery("GDMSYS","QueryStandard","-47");
        // IQuery Iq =  new IQuery("M_ORIZZONTALE", "TESTADS");
             //  Environment vu = new Environment("GDM","GDM",null,null,null,conn);
          // elpsTime = new ElapsedTime("RicercaFinmatica",vu);
           
         //  elpsTime.start("PROVA","" );
         //COSTRUTTORI RICERCA
                           
         
      // IQuery Iq = new IQuery();               
      //  IQuery Iq = new IQuery("M_PROTOCOLLO","SEGRETERIA.PROTOCOLLO");      
        //IQuery Iq = new IQuery("FASCICOLO","SEGRETERIA");
         
          IQuery Iq = new IQuery();
        
      //   IQuery Iq = new IQuery("F1","MANNY");
         //IQuery Iq = new IQuery("CartellaStandard","GDMSYS");
         
         //IQuery Iq = new IQuery("BARCODE12","GDMSYS");
   
         if (casoConnection.equals(CONNECTION_EXTERN))
            Iq.initVarEnv(/*vu*/"GDM","", conn);
         else
            Iq.initVarEnv("GDM","GDM", ini);
    
         try {
                 //-------CASO AGGIORNAMENTO-----//
                 if (caso.equals("1")) {
                    //Iq.addValue("NOME","AAAAAAAAA");
                    //Iq.addCampo("POLEPOSITION",":@",":@"); 
                	
                	// Iq.addCampo("CAMPO","--","<>");
                	 
                //	 Iq.addCampoFT("","");
                	 
                    //Iq.setFiltro("aaa");
                    //Iq.settaArea("MANNY");
                    //Iq.addCodiceModello("AAAA");
                    //Iq.addCampo("COGNOMEPILOTA",":@");
                    /*Iq.addCampo("VITTORIE","12");
	                    Iq.addCampo("DATA_NASCITA","01/01/2005");
	                    Iq.addCampo("COGNOMEPILOTA","is null");
	                    Iq.addCampo("POLEPOSITION","1","2");                    
	                    Iq.addCampo("VITTORIE","3","<>");
	                    Iq.addCampo("VITTORIE","1",">");
	                    Iq.addCampo("VITTORIE","2","<");*/
                    //Iq.addCampo("VITTORIE",":@");
                    //Iq.settaACL("PRES",Global.READONLY_ACCESS);                	 
                //	System.out.println(Iq.getStato());
                	// Iq.addCampo("UFFICIO_SMISTAMENTO","2.0.3");
                	 //Iq.settaACL("RPI", Global.READONLY_ACCESS);
                	 // Iq.setQueryMaster(true);
                    //Iq.update();
                 }
                 
                 //-------CASO CANCELLAZIONE-----//
                 if (caso.equals("2")) {          
        
                    Iq.delete();

                 }        
        
                 //-------CASO INSERIMENTO-----//
                 if (caso.equals("3")) {          
                    
                	/*Iq.settaArea("MANNY");                    
                    Iq.addCodiceModello("F1");                                                            
                    Iq.addCampo("COGNOMEPILOTA","dd");
                    Iq.addCampoOrdinamentoAsc("COGNOMEPILOTA");
                    Iq.addCampoOrdinamentoDesc("VITTORIE");
                    Iq.addCampo("VITTORIE","12");
                    Iq.addCampo("DATA_NASCITA","01/01/2005");
                    Iq.addCampo("COGNOMEPILOTA","is null");
                    Iq.addCampo("POLEPOSITION",":@",":@");                    
                    Iq.addCampo("VITTORIE","3","<>");
                    Iq.addCampo("VITTORIE","1",">");
                    Iq.addCampo("VITTORIE",":@","<");
                    Iq.settaCondizioneAnd("A B");
                    Iq.settaCondizioneOr(":@");*/
                    //Iq.settaACL("AA4",Global.COMPLETE_ACCESS);
                	 //Iq.addCampo("NOME","dd");
                	  //Iq.setQueryMaster(true);

                	/* Iq.settaArea("SEGRETERIA");
                	 Iq.addCodiceModello("M_SOGGETTO");
                	 Iq.addCampo("COGNOME","LANDUZZI");
                	 
                	 Iq.addCampoCategoria("ANNO","is null","PROTO");
                	 Iq.addCampo("COGNOME","LANDUZZI","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                	 Iq.addCampo("NUMERO","is null","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	 //PROVA CAMPO RETURN CHE NN DEVE FINIRE NEL FILTRO
                	 Iq.addCampoReturn("NUMERO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	
                	 Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF","SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF");
                	 Iq.addCampoOrdinamentoDesc("COGNOME","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                	 Iq.addCampoOrdinamentoDesc("NUMERO","PROTO");
                	 Iq.addCampoOrdinamentoAsc("NOME");*/
                	 Iq.setFiltro("RICERCAMODULISTICA_taap'a");

                     //Iq.prepareXMLFilter();
                	 Iq.insert();
                 }            
                 //----COSE VARIE----//
                 if (caso.equals("5")) {
                	 provaConversione("{ANNO}||mid(to_char({NUMERO}),1,2)",1);
                 }
                 //-------CASO RICERCA---------//
                 if (caso.equals("4")) {
                	// Iq.escludiControlloCompetenze(true);
                    //Iq.setAccessProfile(false);
                    //Iq.setQueryMaster(true);
                    //Iq.setInstanceProfile(false);

                   //  Iq.addCampo("TEST_CHECK", "2#","TESTADS","M_VERTICALE" );

                     Iq.addCampo("TEST_CHECK", "2#","TESTADS","M_ORIZZONTALE" );

                    /*Iq.addCampo("NOME", "U.O. U.M.A.");
                    Iq.addCampo("DAL","01/02/2011", "<=");
                    Iq.addCampoNvl("AL","01/02/2011","01/02/2011",">=");
                    Iq.addCampoOrdinamentoAsc("NOME");*/

                   //  Iq.addCampo("TEST_COMBO", "2","=", "TESTADS","M_ORIZZONTALE");
                    
                    //Iq.addCampo("TEST_COMBO", "2","=", "TESTADS","M_ORIZZONTALE");
                
                    //Iq.addCampoFT("class_descr", "%PRO*");
                    
                    //Iq.addCampo("TEST_CHECK", "2#");
                 //   Iq.setOggettoFileConditionOcr("Prova");
                   // Iq.setOggettoFileConditionOcr("ProvaOCR");
                   // Iq.setOggettoFileCondition("Prova22");
                    //Iq.setOggettoFileCondition("Prova22OCR");
                    
                    /* Iq.addCampoReturn("COD_AMM", "SEGRETERIA","M_LISTA_DISTRIBUZIONE" );
                    Iq.addCampoReturn("COD_AOO", "SEGRETERIA","M_LISTA_DISTRIBUZIONE" );
                    Iq.addCampoReturn("NI", "SEGRETERIA","M_LISTA_DISTRIBUZIONE" );*/
                    
                    
                    /*Iq.addCampo("UNITA", "2");
            		Iq.addCampo("CODICE_AMMINISTRAZIONE", "P_MO");
            		Iq.addCampo("CODICE_AOO", "AOOPMO");*/

                    //Iq.addCampoOrdinamentoDesc("TEST_DATA","TESTADS","M_VERTICALE","dd/mm/yyyy hh:mi:ss");

                   //  Iq.addCampoOrdinamentoDesc("TEST_DATA","TESTADS","M_VERTICALE","dd/mm/yyyy hh:mi:ss");

                     Iq.addCampoOrdinamentoDesc("TEST_DATA","TESTADS","M_ORIZZONTALE","dd/mm/yyyy");

                   //  Iq.addCampoOrdinamentoAsc("TEST_DATA","TESTADS","M_ORIZZONTALE","dd/mm/yyyy hh:mi:ss");
                   //  Iq.addCampoOrdinamentoAsc("TEST_DATA","TESTADS","M_ORIZZONTALE");

                     //Iq.addCampoReturn("TEST_DATA","TESTADS","M_ORIZZONTALE");


                    // Iq.addCampoReturnConFormato("TEST_DATA","TESTADS","M_ORIZZONTALE", "dd/mm/yyyy hh:mi:ss");

                    // Iq.addCampoReturnConFormato("TEST_DATA","TESTADS","M_VERTICALE", "dd/mm/yyyy hh:mi:ss");

                     Iq.addCampoReturnConFormato("TEST_DATA","TESTADS","M_ORIZZONTALE", "dd/mm/yyyy");

                     //Iq.addCampoOrdinamentoAsc("TEST_DATA","TESTADS","M_ORIZZONTALE");


                     //Iq.addCampoReturn("TEST_DATA","TESTADS","M_ORIZZONTALE");


                     //  Iq.enableAddCampoNoCaseSensitive();
                  //  Iq.addCampo("COGNOME","bonforte5%");
                    //Iq.disableAddCampoNoCaseSensitive();
                  //  Iq.addCampo("NOME","marco5");
                   //Iq.setOggettoFileCondition("SELECT");
                  /*  Iq.settaArea("TESTIMPORTEXPORT_VERT");
                    Iq.addCodiceModello("M_SOCIETA_HOR");
                    Iq.addCampo("COGNOME","Pippo");*/
                   // Iq.setOggettoFileCondition("M4 CAMBIATO");
                     
                    //Iq.addCodiceModello("M_SOGGETTO"); 
                   // Iq.setTypeAbilityDocument(Global.ABIL_MODI);
                 //   Iq.addCampo("COGNOME","=","is null");
             
                    //Iq.setExtraConditionSearch(" rownum<=3 ");
                    
                    //Iq.controllaPadre(true);
                    
                   // Iq.addCampo("COGNOME_DIPENDENTE","A","B");
                    
                 //   Iq.addCampo("DATA","14/11/2008 23:59:59","<=");
                    
                  //  Iq.addCampo("INSTR ({OGGETTO}, 'IN')","ii");
                 //  Iq.settaIdDocumentoRicerca("2142");
             /*Vector v2 = new Vector();
                    
                    v2.add("4567");
                    v2.add("4568");
                    v2.add("4569");
                   Iq.settaIdDocumentoRicerca(v2);*/
                   
               //    Iq.addCampo("IDRIF","8048","SEGRETERIA","M_SOGGETTO");
             // Iq.settaIdDocumentoRicerca("19");              
             // Iq.addCampoReturn("COGNOME","SEGRETERIA","M_SOGGETTO");
             // Iq.addCampoReturn("NOME","SEGRETERIA","M_SOGGETTO");
              //    Iq.settaArea("SEGRETERIA.PROTOCOLLO");
                    //        Iq.addCodiceModello("M_PROTOCOLLO");
                      //Iq.addCampo("ANNO","2005");   
                      //Iq.addCampoNvl("ANNO","2000","2005","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                   //   Iq.addCampoNvl("NUMERO","22","21","40"/*,"SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO"*/);
                    //  Iq.addCampoNvl("DATA_DOCUMENTO","SYSDATE","SYSDATE");
                    //  Iq.addCampoNvl("CLASS_COD","P'IPPO","05");
                    //  Iq.addCampoReturn("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                      
                    // Iq.addCampoOrdinamentoAsc("DATA_DOCUMENTO");    
                    // Iq.addCampoOrdinamentoAsc("NUMERO");
                   
                   // Iq.addCodiceModello("SEGRETERIA","M_CARICO_ASSEGNA");
                    /*Iq.addCodiceModello("SEGRETERIA","M_SMISTAMENTO");*/
                    
                  //   Iq.addCampo("ANNO","2007","SEGRETERIA","M_SOGGETTO");  
                     
                   //  Iq.addCampo("XXXXX","2007");
              /*       Iq.addCampo("STATO_PR","X","SEGRETERIA","M_SMISTAMENTO");                    
                     Iq.addCampoCategoria("NUMERO","4","PROTO");*/
                  //  Iq.addCampoFT("PR_NUMERO","2","SEGRETERIA","M_ALLEGATO_PROTOCOLLO");
               //    Iq.addCampo("TEST_CHECK","1");
                   // Iq.addCampoCategoria("NUMERO","4","PROTO");
                   // Iq.addCampoCategoria("ANNO","2012","PROTO");
                    /*Iq.addCampoFT("ANNO","2005","SEGRETERIA","M_SMISTAMENTO");
                      Iq.addCampoFT("NUMERO","19","SEGRETERIA","M_SMISTAMENTO");*/
                /*   Iq.addCampoOrdinamentoDesc("ANNO");
                   Iq.addCampoOrdinamentoAsc("NUMERO");
                   Iq.addCampoCategoria("NUMERO","2","PROTO");*/
                    
                //     Iq.addCampo("ANNO","2005","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                 //    Iq.addCampo("NUMERO","21","100","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                   //   Iq.addCampo("NUMERO","45","56"); 
                   // Iq.addCampoCategoria("ANNO","2006","PROTO");
                   //  Iq.addCampo("STATO_PR","B");                                      
                     
                  /*  Iq.addCampo("COGNOMEPILOTA","dsss","MANNY","F1");
                     Iq.addCampoReturn("COGNOMEPILOTA","MANNY","F1");*/
                    /* Iq.addJoinMix("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                                   "PROTO","IDRIF");*/                

                  /*   Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                                     "SEGRETERIA","M_SMISTAMENTO","IDRIF");*/
                     
                     
                /*     Iq.addJoinMix("SEGRETERIA","M_SOGGETTO","IDRIF",
                                       "PROTO","IDRIF");*/
                     
                   
                   
                   //  Iq.settaCondizioneAnd("STRADE STATALI");
                    // Iq.settaCondizioneOr("STRADE STATALI GIARDINI");
                     
                     // Iq.addCampoOrdinamentoAsc("CLASS_COD","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                     
                    //Iq.addCampoReturn("CLASS_COD","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                   /* Iq.addCampoOrdinamentoAsc("CLASS_COD","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    Iq.addCampoReturn("ANNO","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                    Iq.addCampoOrdinamentoAsc("ANNO");*/
             /*       Iq.addCampoOrdinamentoAsc("PR_DATA");
                    Iq.addCampoOrdinamentoAsc("CLASS_COD");*/
                    
              //      Iq.addCampoReturn("APPL","PROTO");
                           
                    
                  //  Iq.addCampoOrdinamentoAsc("NUMERO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    
                    
               //     Iq.addCampoOrdinamentoAsc("NUMERO");
                   
                    /*Iq.addCodiceModello("LETTERA_USCITA");
                    Iq.addCodiceModello("M_PROTOCOLLO_INTEROPERABILITA");
                    Iq.addCodiceModello("M_PROVVEDIMENTO");                    
                    Iq.addCodiceModello("M_PROTOCOLLO");
                    
                    Iq.addCampo("DATA","SYSDATE - 7","SYSDATE");
                    Iq.addCampoOrdinamentoAsc("ANNO");
                    Iq.addCampoOrdinamentoAsc("NUMERO");*/
                    
                   // Iq.addCampoFT("MESSAGE_ID","<17209926X1186057340570LXM04@LEGALMAIL.IT>");
                    //Iq.setQueryMaster(true);
                  /*   Iq.addCampo("COGNOMEPILOTA","%s%","MANNY", "F1");
                     Iq.addCampo("NOMEPILOTA","PIPPO","MANNY", "F1");*/
                  /*   Iq.addCampo("NOMEPILOTA","%s%","MANNY", "F1");*/
                    /*  Iq.addCampo("VITTORIE","1","MANNY", "F1");
                      Iq.addCampo("VITTORIE","1","<>","MANNY", "F1");
                      Iq.addCampo("POLEPOSITION","1","2","MANNY", "F1");
                      Iq.addCampo("DATA_NASCITA","01/01/2009","01/01/2010","MANNY", "F1");
                      Iq.addCampo("DATA_NASCITA","01/01/2011","<>","MANNY", "F1");
                      Iq.addCampo("PROVA","444","=","MANNY", "F1");*/
                  //   Iq.addCampo("COGNOMEPILOTA","%S%","MANNY", "F1");
                  //   Iq.addCampo("NOMEPILOTA","%s%","MANNY", "F1");
                 //    Iq.addCampo("COGNOME","%A%","AD4", "MB1");
                 //    Iq.addCampo("NOME","%M%","AD4", "MB1");
                 //    Iq.addCampo("PROVINCIA","84","AD4", "MB1");
                //      Iq.addCampo("RAGIONE_SOCIALE","%H%","AD4", "MB1");
                     //Iq.addCampo("ANNO","2005","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                     
                  /*  Iq.addCampoFT("NOME","CRIS","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                    Iq.addCampoFT("COGNOME","LAND","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                    Iq.addCampoOrdinamentoAsc("COGNOME","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                    Iq.addCampoReturn("COGNOME","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                    
                    Iq.addCampoCategoriaFT("OGGETTO","PIPPO","PROTO");
                    
                    Iq.addJoinMix("SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF",
                                   "M_PROTOCOLLO","IDRIF"
                     		        );*/
                   // Iq.addCampoFT("ANNO","2005","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                   // Iq.addCampoFT("COGNOME","MANNELLA","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                   // Iq.addCampoFT("NUMERO","1","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    /*Iq.addCampoOrdinamentoAsc("NUMERO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    Iq.addCampoOrdinamentoAsc("COGNOME","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    Iq.addCampoOrdinamentoAsc("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");                                       
                    Iq.addCampoReturn("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    Iq.addCampoReturn("NUMERO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    
                    Iq.addJoinModel("SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO","IDRIF",
                    		        "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO","IDRIF");*/
                    
                    //Iq.addCampoFT("ANNO","2005","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    //Iq.addCampoFT("NUMERO","2005","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    
                   //Iq.addCampoOrdinamentoAsc("{ANNO}||substr(to_char({NUMERO}),1,2)","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    
                   //Iq.addCampoOrdinamentoAsc("{COGNOME}||substr(to_char({NOME}),1,2)","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                    
                     //Iq.addCampoOrdinamentoAsc("{ANNO}+10","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    
                    //Iq.addCampoFT("NUMERO","3","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                    
                     //Iq.addCampoCategoria("ANNO","is null","PROTO");
                	 //Iq.addCampoFT("COGNOME","LAND% PIPPO","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                	 //Iq.addCampoFT("NOME","A% B","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
                                        
                	/* Iq.addCampo("ANNO","2005","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	 Iq.addCampoOrdinamentoAsc("NUMERO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	 Iq.addCampoOrdinamentoAsc("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	 Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                     		        "SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF");*/
                    
                   
                //    Iq.addCampo("DATA","01/01/2006","=","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	 //PROVA CAMPO RETURN CHE NN DEVE FINIRE NEL FILTRO
                //	Iq.addCampoReturn("DATA","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO"); 
                  //  Iq.addCampoReturn("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                 //   Iq.addCampo("DATA","01/01/2006","=","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                	 
                	
                    //Iq.addCampo("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                //	 Iq.addCampoOrdinamentoDesc("COGNOME","SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
               // 	 Iq.addCampoOrdinamentoAsc("ANNO","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO");
                //	 Iq.addCampoOrdinamentoAsc("NOME");
                    
                   // Iq.setQueryTimeOut(15000);                                        
                    //Iq.setQueryMaster(true);
                    //Iq.addCampoReturn("COGNOME","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");   
  /* Iq.addCampo("IDRIF", "1",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");*/
 /*  Iq.addCampoReturn("TIPO_SOGGETTO",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");*/
  /* Iq.addCampoReturn("COGNOME_PER_SEGNATURA",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("NOME_PER_SEGNATURA",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("DESCRIZIONE_AMM",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("INSEGNA", "SEGRETERIA.PROTOCOLLO",
     "M_SOGGETTO");
   Iq.addCampoReturn("INSEGNA_EXTRA",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("COGNOME_DIPENDENTE",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("NOME_DIPENDENTE",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");*/
   /* Iq.addCampoOrdinamentoAsc("TIPO_SOGGETTO",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoOrdinamentoAsc("TIPO_SOGGETTO",*/
    // "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
 /*  Iq.addCampoReturn("COMUNE_RES", "SEGRETERIA.PROTOCOLLO",
     "M_SOGGETTO");
   Iq.addCampoReturn("COMUNE_IMPRESA",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("COMUNE_IMPRESA_EXTRA",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");
   Iq.addCampoReturn("COMUNE_RES_DIPENDENTE",
     "SEGRETERIA.PROTOCOLLO", "M_SOGGETTO");*/
                    
                 /*   Iq.addCampo("IDRIF","2005PRGE000021","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
                    Iq.addCampoReturn("IDRIF","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
                    Iq.addCampoReturn("COGNOME_PER_SEGNATURA","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
                    Iq.addCampoOrdinamentoAsc("COGNOME_PER_SEGNATURA","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");*/
                   
                 /*   Iq.addCampo("NUMERO", "9999999", "<=");
                    Iq.addCampo("NUMERO", "1", ">=");
                    
                    Iq.addCampo("DATA", "01/01/2003", ">=");
                    Iq.addCampo("DATA", "01/03/2004", "<=");
                    
                    Iq.addCampo("TIPO_REGISTRO", "PROT");*/
                    //Iq.addCampo("TIPO_REGISTRO", "PROT");
                    
                   // Iq.addCampo("COGNOME","GAZZETTI","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");                  
                    
                    //**********************PROVA CON RETURN
                  /*  Iq.addCampoCategoria("NUMERO","1","999999","PROTO");
                    Iq.addCampoCategoria("DATA","05/01/2004","06/01/2004","PROTO");
                    Iq.addCampoCategoria("TIPO_REGISTRO","PRGE","PROTO");
					Iq.addCampoReturn("IDRIF", "PROTO");
					Iq.addCampoReturn("TIPO_REGISTRO", "PROTO");
					Iq.addCampoReturn("NUMERO", "PROTO");
					Iq.addCampoReturn("ANNO", "PROTO");
					Iq.addCampoReturn("DATA", "PROTO");
					Iq.addCampoReturn("MODALITA", "PROTO");
					Iq.addCampoReturn("CLASS_COD", "PROTO");
					Iq.addCampoReturn("FASCICOLO_ANNO", "PROTO");
					Iq.addCampoReturn("FASCICOLO_NUMERO", "PROTO");
					Iq.addCampoReturn("OGGETTO", "PROTO");
					Iq.addCampoReturn("NUMERO_DOCUMENTO", "PROTO");
					Iq.addCampoReturn("DATA_DOCUMENTO", "PROTO");
					Iq.addCampoReturn("STATO_PR", "PROTO");*/
                    
                 //*   Iq.addCampoOrdinamentoAsc("NUMERO","PROTO");*/
                   // Iq.addCampoCategoria("NUMERO","1","=","PROTO");

                    /*Iq.addJoinMix("SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF",
                     		        "PROTO","IDRIF");                                                                               
                    
                    Iq.addCampoReturn("COGNOME","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
                    Iq.addCampoReturn("NUMERO","PROTO");*/
                   // Iq.addCampoReturn("ANNO","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                    
                   // Iq.addCampoOrdinamentoDesc("COGNOME","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");                 

                    //Iq.addCampoReturn("NUMERO","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
               //     Iq.addCampoOrdinamentoDesc("ANNO","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
                    //Iq.addCampoReturn("NUMERO","PROTO");
                                        
                   // Iq.addCampoReturn("ANNO","SEGRETERIA.PROTOCOLLO","M_ALLEGATO_PROTOCOLLO");
                    //Iq.addCampoReturn("ANNO","ALL_PROTO");
                    
                    /*Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                     		        "SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF");*/
                                        
                    
                  //  Iq.setTypeModelReturn("SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
                    /*Iq.addJoinClass("PROTO","IDRIF",
                     		        "ALL_PROTO","IDRIF");*/
                    
                    //**********************FINE PROVA CON RETURN                     
                    
                   // Iq.addCampoReturn("NUMERO","PROTO");
                   // Iq.addCampoOrdinamentoDesc("NUMERO","PROTO");
                    
                   // Iq.test();
                    
                   // Iq.setQueryMaster(true);
                    //Iq.execRebuildIndex();
                      //Iq.addCampoFT("FASCICOLO_OGGETTO","PINOOOOO");
                    
                  //   Iq.addCampoFT("CLASS_COD","01-01-01");

                //    Iq.addCampo("TIPO_REGISTRO","PRGE");
                    
                //   Iq.settaArea("FASCICOLO");
                  
               /*     Iq.addCodiceModello("PROTOCOLLO_TEST");*/
                   /* Iq.addCodiceModello("ENDOPROC_SUAP");*/
                   // Iq.addCodiceModello("MANNY","___F1");
                 //   Iq.addCampo("COGNOMEPILOTA","%");
                 //   Iq.addCampo("___COGNOMEPILOTA","Senna","MANNY","___F1");
                 //   Iq.addCampoOrdinamentoDesc("NOMEPILOTA","MANNY","F1");
                    //Iq.addCodiceModello("GDMSYS","M_CLASSIFICAZIONE");
                //   Iq.addCodiceModello("F1_ALTERNATIVE");
                //   Iq.addCampo("NUMERO","2006",">");
               // Iq.addCampo("UFFICIO_SMISTAMENTO","2.0.3");
              //   Iq.addCampo("TIPO_REGISTRO","PRGE");
           // Iq.addCampo("COGNOME","%landuzzi%","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
           // Iq.addCampoFT("UFFICIO_SMISTAMENTO","2.0.3");
       // Iq.addCampoFT("TIPO_REGISTRO","%");
            //Iq.addCampo("UFFICIO_SMISTAMENTO","2.0.3","SEGRETERIA.PROTOCOLLO","M_SMISTAMENTO");
           //Iq.addCampoFT("COGNOME","G'AZZETTI");
           //Iq.addCampoFT("ANNO","2005");
           /*Iq.addCampoOrdinamentoDesc("ANNO");
           Iq.addCampoOrdinamentoAsc("NUMERO");*/
           //     Iq.setTypeModelReturn("SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
               // Iq.setOggettoFileCondition("A AND B");
             //   Iq.setOggettoFileCondition("C AND D");
           //     Iq.setOggettoFileCondition("XXXX","PROTO");
//            Iq.setOggettoFileCondition("landuzzi"/*,"SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO"*/);
          //      Iq.setOggettoFileCondition("landuzzi","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
            
         //     Iq.setOggettoFileCondition("landuzzi","PROTO");

                //Iq.setOggettoFileCondition("xxxx","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");
           //     Iq.setOggettoFileCondition("YYaaYY","c","d");
       //           Iq.addCampoOrdinamentoDesc("COGNOME");
           //         Iq.addCampoFT("COGNOME","GAZZET,,\\(\\TI");
               /*    Iq.addCampo("NOME","is null","SEGRETERIA.PROTOCOLLO","M_SOGGETTO");*/
       //           Iq.addCampo("ANNO","2007","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
              //     Iq.addCampoOrdinamentoDesc("ANNO");
                   //Iq.setTypeModelReturn("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");
               //     Iq.addCampoOrdinamentoDesc("NUMERO_VITTORE","SEGRETERIA.PROTOCOLLO","PROTOCOLLO_TEST");
               //    Iq.addCampoOrdinamentoDesc("ANNO","PROTO");
                    
             /*       Iq.addCampoCategoria("ANNO","2007","PROTO");
                    Iq.setTypeModelReturn("PROTO");*/
                    //Iq.addCampoCategoria("NUMERO","12","PROTO");
                  //  Iq.addCampoCategoria("TIPO_ALLEGATO","C","PROTO");
               //     Iq.addCampoCategoria("IDRIF","1","ALL_PROTO");
                  //  Iq.addCampo("ANNO","2007","SEGRETERIA.PROTOCOLLO","M_ALLEGATO_PROTOCOLLO");
                    
                   /* Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                    		        "SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF");
                    
                    Iq.setTypeModelReturn("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO");*/
                    
                    
                 /*   Iq.addJoinMix("SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF",
                    		        "PROTO","IDRIF");*/
                    /*  Iq.addJoinClass("PROTO","IDRIF",
                    		          "ALL_PROTO","IDRIF");*/
                      
                   /* Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                    		        "SEGRETERIA.PROTOCOLLO","M_ALLEGATO_PROTOCOLLO","IDRIF");*/
                    
                /*    Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
                    		        "SEGRETERIA.PROTOCOLLO","M_ALLEGATO_PROTOCOLLO","IDRIF");*/
                   // Iq.addCampo("COGNOMEPILOTA","S%");
                  //  Iq.addCampoOrdinamentoAsc("IDRIF");
                  //  Iq.addCampo("DATA_NASCITA","01/01/2005");
                   /* Iq.addCampo("COGNOMEPILOTA","is null");
                    Iq.addCampo("POLEPOSITION","1","2");                    
                    Iq.addCampo("VITTORIE","3","<>");
                    Iq.addCampo("VITTORIE","1",">");
                    Iq.addCampo("VITTORIE","2","<");*/
                   // Iq.setExtraConditionSearch(extraConditionSearch)
                    /*Vector v = Iq.getFiltroCampo();
                    for(int i=0;i<v.size();i++) {
                    	keyval k = (keyval)v.get(i);
                    	
                    	System.out.println("chiave-->"+k.getKey());
                    	System.out.println("operatore-->"+k.getOperator());
                    	System.out.println("valore-->"+k.getVal());
                    }*/
                    	
                    
                    //Iq.addCodiceModello("NOTIFICA");                                      
                                       
                    //ESEMPIO UTILIZZO ADDCAMPO
                    
                    /* con 2 parametri 
                     * 			(campo,valore) oppure 
                     * 			(campo,operatoreNULL_NOTNULL) oppure
                     * */
                    	/* Stringa */
                    	//Iq.addCampo("CR","3170");
                    	//Iq.addCampo("CR","GDCLIENT2747");
                    	//Iq.addCampo("CLASS_DAL","01/01/2005");  
                    	//Iq.addCampo("COGNOMEPILOTA","A%");
                   // Iq.addCampo("BARCODE_KEY","000000000001");
                    	//Iq.addCampoOrdinamentoAsc("NOMEPILOTA");
                        //Iq.addCampo("ANNO","2005");
                   //     Iq.addCampo("NUMERO","1");
                        //Iq.addCampo("FASCICOLO_OGGETTO","%");
                    	//Iq.addCampo("COGNOMEPILOTA","Ayrton");
                    	//Iq.addCampoOrdinamentoAsc("COGNOMEPILOTA");
                    	/* Numero */
                    	//Iq.addCampo("VITTORIE","1");         
                		//Iq.addCampo("VITTORIE","is null");
                    	/* Data */
                    	//Iq.addCampo("DATA_NASCITA","01/01/2006");
                    	//Iq.addCampo("DATA_NASCITA","is null");
                                
                   /* con 3 parametri 
                    * 			(campo,valore,operatore) oppure 
                    * 			(campo,valore,valore2) 
                    * */
                    	/* Numero */
                    	/*Iq.addCampo("VITTORIE","1",">");*/
                    	//Iq.addCampoOrdinamentoDesc("NUMERO");
                    	
                    	//Iq.addCampoOrdinamentoDesc("DATA_NASCITA");
                    	//Iq.addCampo("VITTORIE","1","5");
                    	/* Data */
                    	//Iq.addCampo("DATA_NASCITA","01/01/2006","<>");
                    	//Iq.addCampo("DATA_NASCITA","01/01/2006","01/01/2007");                                                        
                  // Iq.settaCondizioneAnd("POLIZIA MUNICIPALE");  
                       //Iq.settaCondizioneOr("A B");
                   
                    //**********************************************VECCHIO MODO
                    //Iq.settaChiave("COGNOMEPILOTA","FICAR%");               
                    //Iq.settaChiave("F1","VITTORIE","12","=");               
                    //Iq.settaChiave("F1","DATA_NASCITA","01/01/2005","01/01/2006");
                  // Iq.settaChiave("F1","COGNOMEPILOTA","");               
                    //Iq.settaChiave("F1","COGNOMEPILOTA","");               
                    //Iq.settaChiave("F1","DATA_NASCITA","");               
                    //Iq.settaChiave("F1","VITTORIE","1",">");               
                    //Iq.settaChiave("F1","COGNOMEPILOTA","a123456789");  
                    //Iq.settaChiave("N_DESTINATARIO","NICOLA"); 
                    //Iq.settaChiave("N_DESTINATARIO","NICOLA"); 
                    //Iq.settaChiave("ANNO","2006");
                    //Iq.settaChiave("NOTIFICA","NUM_PG_ENT","0",">");
                    //Iq.settaChiave("F1","NOMEPILOTA","123456789");               
                    //Iq.settaChiave("F1_ALTERNATIVE","NOMEPILOTA","123456789");  
                    //Iq.settaDato("DESCRIZIONE_ENTE","TORINO");
                    //Iq.settaDato("IDRIF","3484");
                    //Iq.settaDato("NUM_PG_ENT","0",">");
                    //Iq.settaDato("COGNOMEPILOTA","a123456789");
                    //Iq.settaDato("NOMEPILOTA","123456789");
                    //Iq.settaCondizioneAnd("A B");
                    //Iq.settaCondizioneOr("C D");
                    //Iq.settaCondizioneNot("X");
                    //Iq.settaCondizioneSingle("Y");
                     
                   // Iq.setFetchInit(20);
                   // Iq.setFetchSize(2);
                    
                   /* Iq.addCampo("RAGSOCIALE","AGFA%","CE4","Fattura");
                    Iq.settaIdDocumentoRicerca("48973");
                    
                    Iq.addCampoReturn("RAGSOCIALE","CE4","Fattura");*/
                    
                    //Iq.settaArea("SEGRETERIA.ATTI");
                    
                    //Iq.addCampoFT("DELIBERA_PDF","atto grosso","SEGRETERIA","DIZ_TIPI_FRASE");
                    //Iq.addCampoFT("TIPO_FRASE","TRRR","SEGRETERIA","DIZ_TIPI_FRASE");
                    
                 /*  String s;
                   s="[1971-01-01T00:00:00.000+02:00 TO 2013-08-06T00:00:00.000+02:00]";
                   s=s.substring(1);
   	    		
                   String first=s.substring(0,s.indexOf("TO"));
                   String last=s.substring(s.indexOf("TO")+3);
                   
                   first=first.substring(0,first.indexOf("T"));
                   last=last.substring(0,last.indexOf("T"));
                   if (first.equals("1971-01-01")) first="1900-01-01";
                   if (last.equals("1971-01-01")) last="2900-01-01";
                   
                    Iq.setExtraConditionSearch("(select count(*) " +
                    							"   from ACTIVITY_LOG where ACTIVITY_LOG.id_documento=DOCU.id_documento and ACTIVITY_LOG.TIPO_AZIONE='C' and ACTIVITY_LOG.data_aggiornamento between to_date('"+first+"','yyyy-mm-dd') and to_date('"+last+"','yyyy-mm-dd') ) > 0 AND "+
                    							" (select count(*) from oggetti_file where DOCU.id_documento=oggetti_file.id_documento and filename like '%AAA%' ) >0  "); 
                    */
                    
                	 
                	  // Iq.settaArea("SEGRETERIA.PROTOCOLLO");
                	   // Iq.addCampo("MATRICOLA", "32156");
                	  //  Iq.addCampoCategoria("ANNO", "2016","PROTO");
                	   // Iq.addCampoCategoria("NUMERO", "2","PROTO");
                	   // Iq.addCampo("MENSILITA", "OTT");
                    
                	    /*if (Iq.ricerca()) {
                	        ResultSetIQuery rsiq = Iq.getResultSet();
                	        if (rsiq.next()) {
                	         String idLog = rsiq.getId();
                	         Profilo pl = new Profilo(idLog, "GDM", "", "", conn);
                	         pl.escludiControlloCompetenze(true);
                	        }
                	    }
                	    */
                	//   if (1==1) return;

                    // Iq.addCampo("COGNOME","=","is null");
                	    
                    if (Iq.ricercaFT().booleanValue()) { 
                    //if (Iq.ricercaFT().booleanValue()) { 
                               System.out.println("N. Totale->"+Iq.getProfileNumber());
                               
                               System.out.println("Sql->"+Iq.getSqlQuery());
                               System.out.println("SqlCount->"+Iq.getSqlCountQuery());
                               
                               ResultSetIQuery  rst = Iq.getResultSet();
                               
                              /* Vector v = rst.getFieldsList();
                               
                               for(int k=0;k<v.size();k++) {
                            	   System.out.println("campi--->"+v.get(k));
                               }
                               */
                               /*if (rst.next()) System.out.println("aaaa--->");
                               if (!rst.next()) System.out.println("bb--->");
                               System.out.println("cc--->");*/
                               
                         //      while (rst.next()) {
                          //  	      System.out.println("id--->"+rst.getId());
                          //  	      System.out.println("cr--->"+rst.getCr());  
                            	     
                            	     // System.out.println("class cod--->"+rst.get("CLASS_COD","SEGRETERIA.PROTOCOLLO", "M_PROTOCOLLO"));
                            	   //   System.out.println("cogn--->"+rst.get("COGNOME","SEGRETERIA","M_SOGGETTO"));
                            	//      if (rst.get("gggg","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO")==null)
                            	//    	  System.out.println("AA");
                            	//      System.out.println("rag--->"+rst.get("RAGSOCIALE","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO"));
                            	    //  System.out.println("rag--->"+rst.get("RAGSOCIALE","CE4","Fattura"));
                            	  //    System.out.println("numero--->"+rst.get("NUMERO","PROTO"));
                            	  //    System.out.println("numero--->"+rst.getCm("NUMERO","PROTO"));
                            	  //    System.out.println("anno--->"+rst.get("ANNO","SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO"));
                            //   }
                               
                     //  System.out.println("-->"+Iq.getProfileFromIndex(0).getDocNumber());
                     // System.out.println("-->"+Iq.getProfileFromIndex(0).getCampo("NOME"));
                     // System.out.println("-->"+Iq.getCampoFromDocNum("47049","NOME"));
                     // elpsTime.stop( );
                    }
                    else 
                    	if (Iq.isQueryTimeOut())
                    	   System.out.println("Sono andato in timeout");
                       else
                    	   System.out.println("Non ho trovato nulla");
         
                    
                    System.out.println("Sql->"+Iq.getSqlQuery());
                    System.out.println("SqlCount->"+Iq.getSqlCountQuery());
                  
                 }            
         }
         catch (Exception e) {
            e.printStackTrace();
         }
         
         if (!Iq.getError().equals("@")) 
            System.out.println("Errore->"+Iq.getError());

         if (casoConnection.equals(CONNECTION_EXTERN)) {
             //conn.rollback();
             conn.commit();
             conn.close();
         }
   
  }
  
  private static String provaConversione(String sInput,int indexDoc) {	   
	      Pattern pattern = Pattern.compile("\\{\\w+\\}");
	      
	      String sStringa = sInput;
	      
		  Matcher matcher = pattern.matcher(sInput);
		  
		  while (matcher.find()) {
		     String sReg=matcher.group();
		     sStringa=Global.replaceAll(sStringa,sReg,"f_valore(docu"+indexDoc+".id_documento,'"+Global.replaceAll(Global.replaceAll(sReg,"{",""),"}","")+"')");
		  }
    
		  System.out.println(sStringa);
	      return sStringa;
  }
}
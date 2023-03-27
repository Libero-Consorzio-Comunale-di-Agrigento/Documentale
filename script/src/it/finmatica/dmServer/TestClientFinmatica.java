package it.finmatica.dmServer;

import it.finmatica.alfresco.ws.finmaticaDmComponent.AlfrescoProfilo;
import it.finmatica.dmServer.Impronta.ImprontaAllegati;
import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.dmServer.management.AggiornaDocumento;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.ElapsedTime;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.util.MimeTypeMapping;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.*;
import it.finmatica.jfc.utility.DateSqlUtility;
//import it.finmatica.jfc.dbUtil.DbOperationSQL;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.text.*;
import java.math.*;
//import java.util.GregorianCalendar;

public class TestClientFinmatica 
{
  final static String CONNECTION_EXTERN="EXTERN";
  final static String CONNECTION_STANDARD="STANDARD";
  
  public void prova(String dontcare) 
  {
	  try {
		  Class.forName("oracle.jdbc.driver.OracleDriver");
		  Connection conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");
		  conn.setAutoCommit(false);
		  
	      System.out.println("*********************PROVA ALFRESCO***********************");
	      Profilo p = new Profilo("M_SOCIETA_HOR","TESTIMPORTEXPORT_VERT");
	      System.out.println("*********************1***********************");
	      p.initVarEnv("GDM","",conn);
	      System.out.println("*********************2***********************");
	      p.settaValore("COGNOME","Bonforte5");
		  p.settaValore("NOME","Marco5");        	
		//p.settaValore("DATANASCITA","01/03/1987 10:10:00");
		  System.out.println("*********************3***********************");
		  Calendar cal = Calendar.getInstance();
		  java.sql.Timestamp now =
	             new java.sql.Timestamp(cal.getTimeInMillis());
	         java.sql.Date jsqlD = new java.sql.Date(now.getTime());
		  p.settaValore("DATANASCITA",jsqlD);
		  java.math.BigDecimal d = new java.math.BigDecimal(1232);     
			p.settaValore("PIVA",d);
			System.out.println("*********************4***********************");
			p.attivaAlfresco("http://10.97.31.51:8080/alfresco/api",
							 "admin",
							 "admin",
							 "/app:company_home/cm:Soggetti",
							 "Bonforte5 Marco5",
							 "ads.customSocietaModel.model",
							 "csm",
							 "campiSocieta",
							 "allegati_societa");
			
			
			System.out.println("*********************5***********************");
			boolean b = p.salva().booleanValue();
			System.out.println("*********************6***********************"+b);
			 if (b) {        	 
	        	 System.out.println("CODRICH: " + p.getCodiceRichiesta());
	             System.out.println("N° Documento: " + p.getDocNumber());
	             
	             System.out.println("Errore cod post save: " + p.getCodeErrorPostStave());
	             System.out.println("Errore post save: " + p.getErrorPostSave());
	             
	             /*System.out.println("--->"+p.getStringDataUltimoAggiornamento());
	             
	              p.settaValore("CODICE_AMMINISTRAZIONE","Phh_MO");
	              
	              p.salva();
	              System.out.println(p.getError());*/
	             conn.commit();
	             System.out.println("...ALFRESCO->"+p.getAlfrescoXMLRet());
	         }
	         else {        	 
	        	 conn.rollback();
	             System.out.println(p.getError());
	             System.out.println(p.getCleanError());             	             
	         }		
			 
			 System.out.println("*********************FINE PROVA ALFRESCO***********************");
	  }
	  catch (Exception e) {
		  System.out.println("*********************ERRORE***********************");
		  e.printStackTrace();
	  }
		      
  }
  public static boolean isThisDateValid(String dateToValidate, String dateFromat){
	  
		if(dateToValidate == null){
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);

		try {

			//if not valid, it will throw ParseException
			 sdf.parse(dateToValidate);
			//System.out.println(date);

		} catch (ParseException e) {

			e.printStackTrace();
			return false;
		}

		return true;
	}
  
  public static void main(String[] args) throws Exception 
  {
	   
	//  DateSqlUtility.toDate("","dd/MM/yyyy");
	 /* try {
		  //isThisDateValid("30/09/2013 23:01:22", "dd/MM/yyyy HH:mm:ss");
		  java.sql.Date d = DateSqlUtility.toDate("30/09/2013 25:01:22","dd/MM/yyyy hh:mm:ss");
		  
		  int i;
		  i=0;
	  }
	 catch(Exception e ){
		 e.printStackTrace();
	 }
	 
	 if (1==1) return;*/
       //CASO FINMATICA           
       String casoConnection=CONNECTION_EXTERN;
       Connection conn=null;
       it.finmatica.dmServer.Environment ev ;

       //------------------------------------CONNESSIONE ESTERNA-------------------------------------
       if (casoConnection.equals(CONNECTION_EXTERN)) {
           Class.forName("oracle.jdbc.driver.OracleDriver");
           //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
           //SICILIA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
           
           //RIVOLI
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.8:1521:orcl","GDM","GDM");           
           
           //JVMSEGR
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.19:1521:PRMOD","GDM","GDM");
           
          	
           //CONNESSIONE A SOTHIX GARDA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.70.80:1521:orcl","GDM","GDM");

           //CONNESSIONE ACHILLE
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@achille:1521:orcl8","GDM","GDM");           

            //PANDORA
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.64.170:1521:GDMTEST","GDM","GDM");
          
           //DBSEGR TEST 
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.31:1521:GDMTEST","GDM","GDM");
   
           
           //PANDORA-PRMOD
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@pandora_prmod:1521:PRMOD","GDM","GDM");
           
           //JVMEFESTO
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");
           
           //SVI-ORA04
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.30.38.33:1521:CASTENASO","GDM","GDM");
           
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora03:1521:GDMTEST","GDM","GDM");
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@demo-affge:1521:DEMOAGS","GDM","GDM");
           conn=DriverManager.getConnection("jdbc:oracle:thin:@test-efesto-lnx:1521:ORCL","GDM","GDM");
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.27.78.17:1521:C","GDM","GDM");
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.27.22.2:1521:orcl","GDM","GDM"); 
            //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.29.101.1:1521:SIGEDOT","GDM","GDM");

           //conn=DriverManager.getConnection("jdbc:oracle:thin:@jvm-sportello:1521:orcl","GDM","GDM");
         //  conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.41:1521:PRMOD","GDM","GDM");
           
         // conn=DriverManager.getConnection("jdbc:oracle:thin:@test-consags:1521:AGGAGS","GDM","GDM");
           
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.27.93.31:1521:C","GDM","GDM");
           
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@test-efesto:1521:ORCL","GDM","GDM");
           
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-affge:1521:DEMOAGS","GDM","GDM");
           
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.27.181.172:9010:stest","GDM","GDM");
            //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.29.14.10:1521:PR_MOD","GDM","GDM");
           
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@test-efesto:1521:ORCL","GDM","GDM");
           
          // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.27.62.1:1521:ORCL1","GDM","GDM");
         // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.29.14.10:1521:PRMOD","GDM","GDM");
           //conn=DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=10.97.64.111)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=10.97.64.112)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=CRAC)))","GDM","GDM");
           conn.setAutoCommit(false);
       }    
   //
       //COSTRUTTORE NEW       
      // Profilo p = new Profilo("M_SMISTAMENTO","SEGRETERIA"); 
   //    Profilo p = new Profilo("M_PROTOCOLLO","SEGRETERIA.PROTOCOLLO");
       //Profilo p = new Profilo ("FASCICOLO","SEGRETERIA", "RPI", "RPI", null, conn);
      // Profilo p = new Profilo("CartellaStandard","GDMSYS");
    //   Profilo p = new Profilo("DIZ_CLASSIFICAZIONE","SEGRETERIA");
        //Profilo p = new Profilo("F1","MANNY");
      
     //  Profilo p = new Profilo("MANNELLA_F1","SEGRETERIA.PROTOCOLLO");
      //Profilo p = new Profilo("M_ORIZZONTALE","TESTADS");
       Profilo p = new Profilo("617355");
       
      // Profilo p = new Profilo("DIZ_CLASSIFICAZIONI","SEGRETERIA");
       
       //COSTRUTTORI MODIFY
    //  System.out.println("*affffffffffaaaaaa".indexOf("*"));
      
   //   System.out.println("*affffffffffaaaaaa".replaceAll("\\*","TAAMA"));s
    // Profilo p = new Profilo("FASCICOLO","SEGRETERIA","10002007");             
      // Profilo p = new Profilo("TEST","SEGRETERIA.PROTOCOLLO","SEGRETERIA.PROTOCOLLO-7115-A");
      // 

			//	"GDCLIENT12140621", modello semplice
				
   //  Profilo p = new Profilo("AGENTE","CAS_RAPPORTI_SERVIZIO","GDCLIENT2551");//new Profilo("12417118");
    //  Profilo p = new Profilo("M_SOCIETA_HOR","TESTIMPORTEXPORT_VERT");
       
    //   Profilo p = new Profilo("M_PROTOCOLLO", "SEGRETERIA.PROTOCOLLO", "SEGRETERIA.PROTOCOLLO-4517-A");
      // Profilo p = new Profilo("M_SOCIETA_HOR","TESTIMPORTEXPORT_VERT","DMSERVER2405");
    //   Profilo p = new Profilo("LISTA_PRATICHE","ROMEO_GESTIONI","DMSERVER12394230");
     // System.out.println("*********************PROVA ALFRESCO***********************");
 //   Profilo p = new Profilo("ALLEGATI_REP","SEGRETERIA.ATTI");
     //  Profilo p = new Profilo("ALBO","MESSI");
      // Profilo p = new Profilo("M_SCARICO_IPA", "SEGRETERIA.PROTOCOLLO", "GDCLIENT15364771");
    // Profilo p = new Profilo("2");
       
     //  Profilo p = new Profilo("TESTIMPORTEXPORT_HOR","GDMSYS"); 
     /*  SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");
       IDbOperationSQL dbOpTest = SessioneDb.getInstance().createIDbOperationSQL("oracle.",
				 "jdbc:oracle:thin:@svi-ora03:1521:GDMTEST",
				 "GDM",
				 "GDM");*/
       
       
    //   Profilo p = new Profilo("585603");
      // Profilo p = new Profilo("M_SOGGETTO","SEGRETERIA");

       
      // System.out.println(Global.getMimeTypeFile("ciccio.pdf.p7m"));       
       //if (1==1) return;
       
//           Profilo p = new Profilo("10706828");
     // Profilo p = new Profilo("12676700");
       //Profilo p = new Profilo("FATTURA","JCF4");
       
     //  Profilo p = new Profilo("LETTERA_USCITA","SEGRETERIA.PROTOCOLLO","SEGRETERIA.PROTOCOLLO-222023-A");
      // Profilo p = new Profilo("VISTO","SEGRETERIA.ATTI");
      
      
       //Profilo p = new Profilo("PRIVA","PROVA");
    //   Profilo p = new Profilo("855");
      // Profilo p = new Profilo("15826971");
   //   Profilo p = new Profilo("M_SOGGETTO","ROMEO_GESTIONI");
       
     
	    
      // Profilo p = new Profilo("M_ORIZZONTALE","TESTADS");
      // Profilo p = new Profilo("TESTADS","M_ORIZZONTALE","GDCLIENT7");
      
     /*    Calendar cal = Calendar.getInstance();
        	  java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());
                 java.sql.Date jsqlD = new java.sql.Date(now.getTime());
        	java.text.SimpleDateFormat s;*/
        	
        	//s  = new SimpleDateFormat();
        	

        	//  System.out.println("---->"+timeStampToString(now));
      
     //   Profilo p = new Profilo("X");
       //   Profilo p = new Profilo("216");
       
      // p.setAccessCmCrArea(true);
      
     // System.out.println("pippo\\aaa".substring("pippo\\aaa".indexOf("\\")+1) );
      
       //--------------------------------CARICAMENTO DELL'ENVIRONMENT--------------------------------
      
       
     //  AccediDocumento ad = new AccediDocumento("802",new Environment("GDM","GDM","","","",conn));
       
   //    ad.accediDocumentoAllegati();
    //  System.out.println(ad.leggiOggettoFile("308"));
   //   System.out.println( ad.nomeOggettoFile("308"));
       
      // if (1==1) return;
    
       if (casoConnection.equals(CONNECTION_EXTERN))
           p.initVarEnv("GDM","",conn);
       else                                 
    	   p.initVarEnv("GDM","GDM","c:\\to_meta\\gd4dm_faenza.properties");
       
      // p.escludiControlloCompetenze(true);
      
     /*  File fOrigine;
       fOrigine = new File("C:\\temp\\tmp\\catalina.out");

       File fDest;
       fDest = new File("C:\\temp\\tmp\\test\\catalina.out");

       fOrigine.renameTo(fDest);
       
       if (1==1) return;*/

 	  
 	  
       //System.out.println(p.getDocNumber());
      // p.escludiControlloCompetenze(true);
       //AGGIUNGI
      
   
       if ("1".equals("1")) {   
          try {        	     
        	  
        	  
    		   
    		  /* IDbOperationSQL dbOpAmm = null;
    		   dbOpAmm = SessioneDb.getInstance()
			     .createIDbOperationSQL("oracle.",
			       "jdbc:oracle:thin:@agsflex:1521:AGSFLEX", "GDM",
			       "GDM");
    		   
    		   dbOpAmm.setStatement("select column_name,data_type from user_tab_columns where table_name='SEG_SOGGETTI_PROTOCOLLO' and column_name not in ('FULL_TEXT','TXT','MODINVIO','CFP','COGNOME_IMPRESA','NOME_IMPRESA','ID_DOCUMENTO','TXT_AMM','IDRIF','NUMERO') order by column_id");
    		       		   
			   dbOpAmm.execute();
			   ResultSet rst =dbOpAmm.getRstSet();
		*/
			  // while (rst.next()) {
				   //System.out.println(rst.getString("column_name"));
				  // System.out.println(rst.getString("data_type"));
			/*	   if (rst.getString("data_type").equals("DATE")) 
					   p.settaValore(rst.getString("column_name"), "01/01/2012");
				   else
					   p.settaValore(rst.getString("column_name"), "1");*/
				   
			  // }
    		   
    		 //  dbOpAmm.close();
    		  // p.appendiValore("MAIL_IMPRESA", " - 6 -");
    		   //p.settaValore("MAIL_IMPRESA", "1");
    		   //p.settaValore("MAIL_IMPRESA", "2");
    		   //p.settaValore("ANNO", "2012");
    		   
        	 //p.setSkipUnknowField(true);
        	// p.setFileName("C:\\gd4dm.properties");
        	//  p.setFileName("C:\\a.pdf");
        	  
        	  
        	// p.settaValore("TEST_CHECK","Y");
        	 
        	  //p.addSkipunknowField("TAAPA");
        	// p.addSkipunknowField("TAAPA222");
        	  /*String nomeDir="c:\\pippo";
        	  File f = new File(nomeDir);
        	  File[] listaFile; 
        	  listaFile = f.listFiles();
        	  for (int i=0;i <listaFile.length;i++) {
        		  
        		  String nomefile = listaFile[i].getName();
        		  LetturaScritturaFileFS f1 = new LetturaScritturaFileFS(nomeDir+"\\"+nomefile);
            	  InputStream is =f1.leggiFile(); 
            	 
        	  }*/
        	
        	        	  
        	 /* String sProva=""; 
        	  for(int i=0 ;i<500;i++) sProva+="aaX";*/
        	//  p.settaValore("DESCRIZIONE","111aaa444aaaaaa22e4222");
        	//  p.settaValore("DECIMALE","636");
        	 // 
        	  //LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\logWsGalileo.txt");
        	 //// p.renameFileName("log.txt","logWsGalileo.txt",f1.leggiFile());
        	 // p.setFileName("test.txt",f1.leggiFile());
        	  
        	 // LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("c:\\pagina_flex.xls");
         	 //// p.renameFileName("log.txt","logWsGalileo.txt",f1.leggiFile());
         	//  p.setFileName("3° testacapo\n\racapo.png",f2.leggiFile());
        //	  p.generaImpronta512("DICHIARAZIONE_REGIME AIUTI ALL''''''''OCCUPAZIONE_DEF.pdf");
        	  
        	 // p.settaValore("TAAPA","66");
        	 // p.settaValore("TAAPA222","66");
        	/*  p.settaValore("MESSAGE_ID", "<2FAE7A55.000A836C.87453D79.E7DEBFBE.posta-certificata@postecert.it>");
        	  p.settaValore("MITTENTE", "\"a.s.so.farm segreteria (posta certificata)\" <assofarmsegreteria@assofarm.postecert.it>");
        	  p.settaValore("OGGETTO", "INVITO Convegno Roma 19 aprile 2012 su Decreto Legge \"Cresci Italia\" - Opportunità di sviluppo o declino per le Farmacie Comunali ? Riflessi giuridici ed economici - Opinioni a confronto");
        	  p.settaValore("DESTINATARI", sProva);  
        	  p.settaValore("DATA_SPEDIZIONE", "Fri Apr 06 12:47:53 CEST 2012");
        	  p.settaValore("DATA_RICEZIONE", "5000/01/01");        
        	  p.settaValore("CORPO", " ");     
        	  */
        	// p.generaImpronta512("SI_0000001404_0.tif");
        	 
        	  
        	//  p.settaValore("TEXT_AREA","c");
        	  
         	   //p.setFileName("c:/to_meta/InstallationInfo.txt");
              //	p.setFileName("c:/to_meta/ojdbc14.jar");
        	  
        	  //p.setFileName("c:/ctapi_out_gr.txt");
        	  
        	//  p.setFileName("c:/to_meta/SA-Integrazione-SIAR-SIURP-20101102_v3.1.1.doc");
        	  
               //	  p.setFileName("c:/201012281244.zip");
        	  
        	 // p.settaValore("TEST_DATA","30/04/2010");
        	//  p.settaValore("TEST_CHECK","1");
        	 
                // p.settaValore("TEST_DATA",now);
                 
              /*  p.settaValore("TEXT_AREA","b"); 
        	  java.math.BigDecimal d = new java.math.BigDecimal(1232); */
        	//  p.settaValore("ID_TRANSAZIONE",d); 
        	  
        	 // java.math.BigDecimal d2 = new java.math.BigDecimal(1232.89); 
        	//  p.settaValore("DECIMALE",d2); 
        	  
       	  // p.setFileName("c:/bfile.txt");
    //	   p.setFileName("c:/Cud_2010.pdf");
    	  // p.settaValore("ULTIMO_NUMERO_REG","");
        	  
        	 /* p.settaValore("IDRIF","PRES-US-2007-1");
        	  p.settaValore("CODICE_ANAGRAFICA","SOF");
        	  p.settaValore("CODICE_RAPPORTO_ANAG","51");
        	  p.settaValore("TIPO_RAPPORTO","DEST");*/
        	//  p.setSkipUnknowField(true);
        	 /* String nome="Prastini2"; 
        	  String cognome="PRASTINI2";
        	  p.settaValore("COGNOME",cognome);
        	  p.settaValore("NOME",nome);
        	  
        	  p.settaACL("ROMAGNOL",Global.COMPLETE_ACCESS);*/
        	  
        	  
        	  
        	 // p.addCompetenza("MARIKA","L");
        	//  p.addCompetenza("MARIKA","U");
        	//p.settaValore("DATANASCITA","01/03/1987 10:10:00");
        	  
        	  
        	  
        	 /* Calendar cal = Calendar.getInstance();
        	  java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());
                 java.sql.Date jsqlD = new java.sql.Date(now.getTime());*/
        	//  p.settaValore("DATANASCITA",jsqlD);
        	 // java.math.BigDecimal d = new java.math.BigDecimal(1232);     
    		//	p.settaValore("PIVA",d);
    			
    	//		p.setFileName("c:\\a.xls");
    			
    		/*	p.attivaAlfresco("http://10.97.31.51:8080/alfresco/api",
    							 "admin",
    							 "admin",
    							 "/app:company_home/cm:Soggetti",
    							 cognome+" "+nome,
    							 "ads.customSocietaModel.model",
    							 "csm",
    							 "campiSocieta",
    							 "allegati_societa",
    							 new ACL[]{new ACL("gmannella",Global.ABIL_CONTRIBUTOR),     						
    									   new ACL("aplastini",Global.ABIL_CONSUMER)},    									   
    					         null);*/
    			
        	 // p.settaValore("CART_AUTO","xxxx");
        	 // p.settaValore("CART_AUTO","xxxx555");
        	 // p.settaValore("SALDO","12");
        	//  p.setStato("CO");
        	 // p.settaValore("CART_AUTO","Y#Garda WrkSp@GDMSYS@WRKSPStandard\\Sotto Garda@GDMSYS@WRKSPStandard");
        	//  p.escludiControlloCompetenze(true);
        	  //p.settaACL("RPI",Global.NORMAL_ACCESS);
        	 /* LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\dabytearray.pdf");
        	  InputStream in =f1.leggiFile();
        	  p.setFileName("dabytearray.pdf", in);*/
//MimeTypeMapping.MIME_APPLICATION_XML,
        	  
        	  //p.renameFileName("a2.rtf","a3.rtf",in);
       // 	  p.setFileName("modello_ordinario.pdf",in);
        	//  p.setFileName("D:\\Progetti ADS\\RepositoryFilePerProveProfilo\\m1.doc");
        	//***********  p.setFileName("D:\\Progetti ADS\\RepositoryFilePerProveProfilo\\m2.doc");
        //	  p.setFileName("D:\\m2.doc");
        	  //p.setFileName("D:\\m4.doc");
        	 // p.setDeleteFileName("m1.doc");
        	  // p.settaValore("COGNOME",a);
        	/*  StringBuffer out = new StringBuffer();
        	         byte[] b = new byte[4096];
        	         for (int n; (n = in.read(b)) != -1;) {
        	             out.append(new String(b, 0, n));
        	        }

        	  p.settaValore("LISTA_PRATICHE",out.toString());*/
        	 /* p.settaValore("COGNOMEPILOTA","SSSS");
        	  p.settaValore("NOMEPILOTA","PIPPO");*/
        	 // p.settaValore("FIRMA","Y");
        /*	  p.settaValore("COD_COMPONENTE","1266");
        	  p.settaValore("AMM_DESC","MECCIO");
        	  p.settaValore("PESO","14");
        	  p.settaValore("DATA_MANNELLA","01/01/2007");*/
        
        	 /* p.settaValore("CODICE_AOO","AOOPMO");
        	  p.settaValore("STATO_SMISTAMENTO","M");
        	  p.settaValore("ASSOCIATO_A_FLUSSO","1");
        	  p.settaValore("UFFICIO_SMISTAMENTO","1");
        	  p.settaValore("DES_UFFICIO_SMISTAMENTO","1 - DIREZIONE GENERALE E DIPARTIMENTO DI PRESIDENZA");
        	   Calendar cal = Calendar.getInstance();
        	  java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());
                 java.sql.Date jsqlD = new java.sql.Date(now.getTime());
        	  p.settaValore("SMISTAMENTO_DAL",now);        	  
        	  p.settaValore("TIPO_SMISTAMENTO","COMPETENZA");*/
        	 // p.settaValore("IDRIF","3444");
        	  // p.settaValore("IDRIF","3444");
              //  p.settaValore("COGNOMEPILOTA","A");
                /* p.settaValore("___NOMEPILOTA","PRGE");
                 p.settaValore("VITTORIE","545");*/
        	  //p.settaValore("NOME","MANNY DIRETTA SOTTO WRKSP");
                 //p.settaValore("TIPO_REGISTRO","PRGE");
                // p.settaValore("NOME","PROVAxxxxxx");
        	  	//p.settaValore("ANNO_REG","2006");
        	    //p.settaValore("NOME","Daniela");
        	//  p.settaValore("UTENTE_PROTOCOLLANTE","PORTàLEàààà€[@ò°ù§{");
                 ////p.removeCompetenza("AA4","XX");
                 //p.settaValore("DATA_NASCITA","01/01/2005 11:11:23");
                 //p.settaValore("VITTORIE","33");
        	 //p.settaValore("UTENTE_ANN","PROVA");
        	  
        	  //STRINGA
        	  // p.settaValore("VISUALIZZA_STATO_FIRMA","G");
        //	  p.settaValore("CODICE_AMMINISTRAZIONE","555");
        	 
        	
        	  //NUMERO
        	 //  p.settaValore("ANNO","1,34");
        	  
        	//   p.settaValore("VISUALIZZA_STATO_FIRMA","Y");
        	/*  java.math.BigDecimal d = new java.math.BigDecimal(1232);     
        	  p.settaValore("ANNO",d);*/
        	          	   
        	  //DATE
        	//  p.settaValore("DATA_DOCUMENTO","30/05/2007");
        	  
        	  //CLOB
        	//  p.settaValore("LOG_PROTOCOLLO","AAAAAAAAAA   aaaaa");
        	  
        	 // p.settaValore("BARCODE2","sssXXXX");
        	  
        	  
        	 //p.settaValore("OGGETTO","PINO");
        	/* Calendar cal = Calendar.getInstance();
        	  java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());
                 java.sql.Date jsqlD = new java.sql.Date(now.getTime());                                  
        	  p.settaValore("DATA_DOCUMENTO",jsqlD);*/
        	  
        	/*  p.settaValore("OGGETTO","PINO");*/
        	  
             //    Calendar cal = Calendar.getInstance();
                                
                /* java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());*/
                 //java.sql.Date jsqlD = new java.sql.Date(now.getTime());
                 
                //p.settaValore("DATA_DOCUMENTO",now);
                 
                 //System.out.println(jsqlD.toString());
                 
                // System.out.println("--->"+jsqlD.getHours());
                 
                 /*java.math.BigDecimal d = new java.math.BigDecimal(13.123456789);             
                 p.settaValore("VITTORIE",d);*/
                               //    p.setFileName("c:\\clntr32.txt");                                               
                /* p.setFileName("c:\\modello_ordinario.pdf");
                  p.setFileName("c:\\nocache.reg");
                  p.setFileName("c:\\clntr32.txt");*/
                 
               /*  p.setFileName("c:\\tmp.txt.p7m","tmp.txt");
                 p.setFileName("c:\\ALTER.sql","tmp.txt");
                 
                 p.setFileName("c:\\5.jpg");*/
                 
                 //p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\Allegato2.DOC");     
                 //p.setFileNameImpronta("c:\\tmp.txt");
                // p.setSysPDF("c:\\tmp.txt");
                
                 //p.settaACL("AA4",Global.NO_ACCESS);
                 //p.settaACL("ALE",Global.COMPLETE_ACCESS);
                // p.getStringDataUltimoAggiornamento()

                // p.setFileName("");
               //  p.settaPadre("43131");
                 //p.setStato(Global.STATO_COMPLETO);
        	/*  p.settaValore("NOME","ffffx");
        	  p.settaValore("COGNOME","pippo");
        	  p.settaValore("PIVA","3");
        	  p.settaValore("SALDO","77");*/
        	//  p.setFileName("c:\\Select.txt");
        	 // p.setSysPDF("c:\\modello_ordinario.pdf");
        	  p.setFileName("k:\\temp\\AdobeARMasdasdaaa.log");
        	  
        	 // p.settaValore("IDRIF", "8743");
        	   //  ElapsedTime elpsTime = new ElapsedTime("TEST",null);
        	   // // elpsTime.start("INSERT","INIZIO");        	            	   
        	            	    
        	    // String s="";
        	    // for(int i=0;i<2000;i++) s+="“pippo”";
        	     
        	     //p.settaValore("TEST_CHECK", "1");  
        	     
        	 //    p.addCompetenza("PIGNATTI", "D");  //349

        	     
        	    // p.settaValore("ANAMNESI_FAMILIARE", s);
        	     //p.settaValore("LABEL_VIDIMATO", "xx");
        	   //  p.settaValore("ANAMNESI_FISIOLOGICA", s);  
        	     
        	    // p.setFileName("C:\\temp\\exp\\exp.xml");
        	     
        	  /*	p.settaValore("CLASS_COD", "01-02");
        	  	p.settaValore("CLASS_DAL", "08/06/2011");
        	  	
        	  	p.settaValore("CODICE_AMMINISTRAZIONE", "P_PC");
        	  	p.settaValore("CODICE_AOO", "AOO_PROVPC");
        	  	p.settaValore("MODALITA", "INT");
        	  
        	  p.settaACL("686", Global.COMPLETE_ACCESS);*/
        	  
        /*	  p.settaValore("ANNO","2018");	
        	  p.settaValore("APRI_IN_STESSA_FINESTRA","1");			
        	  p.settaValore("CAP_AMM","TEST");							
        	  p.settaValore("CAP_AOO","TEST");							
        	  p.settaValore("CAP_DOM","TEST");							
        	  p.settaValore("CAP_DOM_DIPENDENTE","TEST");				
        	  p.settaValore("CAP_IMPRESA","TEST");				
        	  p.settaValore("CAP_IMPRESA_EXTRA","TEST");	
        	  p.settaValore("CAP_PER_SEGNATURA","TEST");				
        	  p.settaValore("CAP_RES","TEST");							
        	  p.settaValore("CAP_RES_DIPENDENTE","TEST");				
        	  p.settaValore("CFP_EXTRA","TEST");						
        	  p.settaValore("CF_PER_SEGNATURA","TEST");				
        	  p.settaValore("CODICE_AMMINISTRAZIONE",	"TEST");		
        	  p.settaValore("CODICE_AOO",		"TEST");				
        	  p.settaValore("CODICE_FISCALE",	"TEST");				
        	  p.settaValore("CODICE_FISCALE_DIPENDENTE","TEST");		
        	  p.settaValore("COD_AMM",	"TEST");						
        	  p.settaValore("COD_AOO","TEST");							
        	  p.settaValore("COD_UO",	"TEST");						
        	  p.settaValore("COGNOME",		"TEST");					
        	  p.settaValore("COGNOME_DIPENDENTE",	"TEST");			
        	  p.settaValore("COGNOME_IMPRESA_EXTRA",	"TEST");		
        	  p.settaValore("COGNOME_PER_SEGNATURA","TEST");			
        	  p.settaValore("COMUNE_AMM",		"TEST");			
        	  p.settaValore("COMUNE_AOO",	"TEST");					
        	  p.settaValore("COMUNE_DOM",	"TEST");					
        	  p.settaValore("COMUNE_DOM_DIPENDENTE","TEST");			
        	  p.settaValore("COMUNE_IMPRESA",		"TEST");		
        	  p.settaValore("COMUNE_IMPRESA_EXTRA","TEST");			
        	  p.settaValore("COMUNE_NASCITA",		"TEST");			
        	  p.settaValore("COMUNE_NASCITA_EXTRA",	"TEST");	
        	  p.settaValore("TIPO_RAPPORTO",	"PAR");
        	  */
        	  p.settaValore("RICH_TEXT_AREA","2022");	
        	  p.settaValore("STATO_AVANZAMENTO_FLUSSO","TEST");			
        	 p.settaValore("TEST_CHECK","0");							
        	  p.settaValore("TEST_COMBO","1");							
        	 p.settaValore("TEST_DATA","01/01/1900");							
        	 p.settaValore("TEST_RADIO","1");				
        	  p.settaValore("TEXT_AREA","TEST");	
        	  
        //	if (1==1 ) return;
        	  
        	//  p.creaVersione(true);
        	  
        /*	  LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\temp\\de4ReWebConfig.ini");
        	  InputStream in =f1.leggiFile();
        	  p.setFileName("de4ReWebConfig.ini", in);*/
        	  //p.settaValore("CART_AUTO","Y");	
        	  
        /*	  Calendar cal = Calendar.getInstance();
        	  java.sql.Timestamp now =
                  new java.sql.Timestamp(cal.getTimeInMillis());
        	  System.out.println(now);*/
                 if (p.salva().booleanValue()) {
                	 
                	// System.out.println("Ult versione creata: "+p.getUltimaVersioneCreata());
                	/* Calendar cal2 = Calendar.getInstance();
                	 now =
                         new java.sql.Timestamp(cal2.getTimeInMillis());
                	 System.out.println(now);*/
                	// elpsTime.stop();
                	 System.out.println("CODRICH: " + p.getCodiceRichiesta());
                     System.out.println("N° Documento: " + p.getDocNumber());
                     
                  /*   p.removeCompetenza("ROMAGNOL", Global.ABIL_LETT);
                     p.removeCompetenza("ROMAGNOL", Global.ABIL_MODI);
                     p.removeCompetenza("ROMAGNOL", Global.ABIL_GEST);
                     p.removeCompetenza("ROMAGNOL", Global.ABIL_CANC);
                     p.removeCompetenza("ROMAGNOL", Global.ABIL_LETTALL);
                     p.removeCompetenza("ROMAGNOL", Global.ABIL_MODIALL);
                     p.removeCompetenza("ROMAGNOL", Global.ABIL_CANCALL);
                     
                     p.salva();
                     */
                     //p.accedi();
                     
                     //p.getFileStream("bfile.txt");
                     
                    // System.out.println("Errore cod post save: " + p.getCodeErrorPostStave());
                   //  System.out.println("Errore post save: " + p.getErrorPostSave());
                     
                     /*System.out.println("--->"+p.getStringDataUltimoAggiornamento());
                     
                      p.settaValore("CODICE_AMMINISTRAZIONE","Phh_MO");
                      
                      p.salva();
                      System.out.println(p.getError());*/
                    
                     System.out.println("...ALFRESCO->"+p.getAlfrescoXMLRet());
                 }
                 else {
                	 //elpsTime.stop();
                     System.out.println(p.getError());
                     System.out.println(p.getCleanError());
                     System.out.println("Trigghi ndo mari: " + p.getDescrCleanError());
                     if (casoConnection.equals(CONNECTION_EXTERN)) {
                        conn.rollback();         
                     }
                     
                 }

             }
             catch (Exception e) 
             {
                if (casoConnection.equals(CONNECTION_EXTERN)) {
                     // conn.rollback();         
                  }
               System.out.println(p.getError());
               System.out.println(p.getCleanError()); 
               e.printStackTrace();
             }         
             
             System.out.println("*********************FINE PROVA ALFRESCO***********************");
                       
       } 
 
       if ("2".equals("")) {        // AGGIORNAMENTO DOC          
           try {
        	   
        	 //  p.setSkipUnknowField(true);
        	//  p.settaValore("NUMERO","182");
        	//   p.setFileName("c:\\Carta identità Michele valida al 24-03-2015.pdf");
         	//  p.setFileName("D:\\Progetti ADS\\RepositoryFilePerProveProfilo\\m2.doc");
        	 //  p.settaValore("COGNOME","PROVA CARTAUTO hhhh");
        	 //  p.setStato("CA");
        	//   p.settaValore("SESSO","M");
        	//   p.settaValore("CART_AUTO","Y#Garda WrkSp@GDMSYS@WRKSPStandard\\Sotto Garda@GDMSYS@WRKSPStandard\\Ancora più sotto@GDMSYS@WRKSPStandard\\Ultimo Livello@GDMSYS@WRKSPStandard");
        	   //p.addCompetenza()
        	//   p.addCompetenza("GP4WEB","GDM","D");
        	  // p.addCompetenza("RPI","GDM","L");
        	   //p.removeCompetenza("RPI","L","06/02/2007","");
        	//   p.escludiControlloCompetenze(true);
        	  /* p.settaValore("CODICE_AMMINISTRAZIONE","SSSXX");
        	   p.settaValore("IDRIF","53");*/
        	   
        	//  p.settaValore("DATA","07/06/2008 24:45:59");	
                //STRINGA
        	   // p.settaValore("VISUALIZZA_STATO_FIRMA","N");  
        	  
        	 //    p.settaValore("CODICE_AMMINISTRAZIONE","P_MO");
        	   /*    p.settaValore("UNITA_RICHIESTA_ANN","6.2.1");
        	         p.settaValore("DATA_RICHIESTA_ANN","24/09/2007");*/
        	//    p.settaValore("ANNO","2007");
        	  /*java.math.BigDecimal d = new java.math.BigDecimal(132);     
        	  p.settaValore("ANNO",d); */
        	    
        	  //DATE
        //	 p.settaValore("DATA_DOCUMENTO","25/12/1987");
        	  //  p.settaValore("DATA_DOCUMENTO","");
        	 /*Calendar cal = Calendar.getInstance();
        	  java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());
                 java.sql.Date jsqlD = new java.sql.Date(now.getTime());                                  
        	  p.settaValore("DATA_DOCUMENTO",jsqlD);  */      
        	  
        	  //CLOB
        //	  p.settaValore("LOG_PROTOCOLLO","LOGA222JJA");
        	  
       // 	  p.settaValore("BARCODE2","BARCODE222GG");        	  
        	    
        	  // p.removeCompetenza("RPI","AMM","VS");
        	   
        	 //  p.appendiValore("OGGETTO","vv");
        	 //  p.settaACL("GDM",Global.NO_ACCESS,"AMM");
        	   
        	  // p.settaRiferimento("46200","RIF");
        	   
        	  /* if (!p.salva().booleanValue()) {
              	 
                   System.out.println(p.getError());
                   System.out.println(p.getCleanError());
                   if (casoConnection.equals(CONNECTION_EXTERN)) {
                       conn.rollback();         
                   }                    
               }
               else {
              	 System.out.println("1.Aggiornato!");
              	 System.out.println("--->"+p.getStringDataUltimoAggiornamento());
               }
        	  
               p.settaRiferimento("46201","RIF");
        	   
        	   if (!p.salva().booleanValue()) {
              	 
                   System.out.println(p.getError());
                   System.out.println(p.getCleanError());
                   if (casoConnection.equals(CONNECTION_EXTERN)) {
                       conn.rollback();         
                   }                    
               }
               else {
              	 System.out.println("2.Aggiornato!");
              	 System.out.println("--->"+p.getStringDataUltimoAggiornamento());
               }
        	   */
        	   
               // p.appendiValore("NOMEPILOTA","APPEND");
                
           /*     p.settaValore("COGNOMEPILOTA","AAddd");
                p.settaValore("NOMEPILOTA","AAAAdd");*/
               // p.getStringDataUltimoAggiornamento();
                
                // p.settaACL("AA4",Global.NORMAL_ACCESS);   
                //p.addCompetenza("AA4","XX");
                // p.addCompetenza("AA4","YY");
                //p.removeCompetenza("AA4","XX");
                 /*File f = new File("c:\\tmp.txt.p7m");
                   FileInputStream fis = new FileInputStream(f);*/
                //  fileP7M = (InputStream)fis;
        	    // p.setDeleteFileName("tmp.txt.p7m");
                //  p.setFileName("c:\\tmp.txt.p7m.p7m","tmp.txt");
                // p.setFileNameImpronta("c:\\tmp.txt");
             //    p.settaValore("NOMEPILOTA","Senna2");
        	   
        		//  LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("d:\\m2.doc");
        	 //  LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("d:\\m2.doc");
        	  //   	  LetturaScritturaFileFS f3 = new LetturaScritturaFileFS("d:\\m3_figlio_di_m1.doc");
        	
        	   
        	//	  LetturaScritturaFileFS f4 = new LetturaScritturaFileFS("d:\\m4.doc");
        		 
        		/*  LetturaScritturaFileFS fBlob = new LetturaScritturaFileFS("C:\\dabytearray.txt");
        		  p.setFileName("a.pdf",fBlob.leggiFile());
        		  */
        	   
        		  /*LetturaScritturaFileFS fBlob2 = new LetturaScritturaFileFS("C:\\test.txt");
        		  p.setFileName("dabytearray.pdf",MimeTypeMapping.MIME_APPLICATION_XML,fBlob2.leggiFile());*/
        		  
        		  
        	//   p.removeCompetenza("GDM", "LA");
        	   
        		//  p.setDeleteFileName("m1.doc");
        	//	  p.setFileName("m1.doc",f1.leggiFile());
        		//  p.renameFileName("openpdf235.pdf","openpdf25555555.pdf",fBlob.leggiFile()); 
        		//  p.setDeleteFileName("m2.doc");
        		   //            p.setFileName("m2.doc",f2.leggiFile());
        		 // p.setFileName("m3_figlio_di_m1.doc",f3.leggiFile(),"m1.doc");
        		  //p.setFileName("m4.doc",f4.leggiFile());
 //p.setDeleteFileName("mBlob.doc");
////                  
        		//  p.setFileName("mBlob.doc",fBlob.leggiFile());
////                  
//                //  p.renameFileName("m1.doc","m1Ren.doc",f1.leggiFile());
//                  
//                  
//        		  p.setFileName("mBlob.doc",fBlob.leggiFile());
        	   
        	   
//        		  p.renameFileName("m1_persergio.doc","m1.doc",f.leggiFile());
//                  LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("d:\\m2.doc");
//                  p.renameFileName("m2.doc","m2_ren.doc",f2.leggiFile());
//                 // p.setFileName("m2.doc",f2.leggiFile());
        	   
        	  // p.setDeleteFileName("m2_ren.doc");
        	 //  p.settaValore("DATANASCITA","");
                 /*Calendar cal = new GregorianCalendar();
                 java.sql.Date jsqlD = new java.sql.Date(cal.getTime().getTime());
                 p.settaValore("DATA_NASCITA",jsqlD);*/
                 
               //   Calendar cal = Calendar.getInstance();
                                
                // java.sql.Timestamp now = new java.sql.Timestamp(cal.getTimeInMillis());
        	  /* String strTmp = "1972-01-31 00:00:00.0 ";
        	   SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd");
        	   
        	   java.sql.Date sqlDate = new java.sql.Date(ts.parse(strTmp).getTime());

       		       		
                 p.settaValore("DATANASCITA",sqlDate);*/
                 
                 /*java.math.BigDecimal d = new java.math.BigDecimal(9999999999.99999);             
                 p.settaValore("VITTORIE",d);*/
                 
                 //java.math.BigDecimal d2 = new java.math.BigDecimal(9999999999.99999);
                 
                 
                 //System.out.println(d.toString());
                 //System.out.println(d2.toString());
                 //System.out.println("-->"+d.doubleValue());
                 //p.settaValore("DATA_NASCITA","10/10/2005");
                 //p.settaValore("VITTORIE","34");
             
                 // p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\Allegato2.DOC");
                 //p.setFileName("S:\\SI4\\GD4\\DMCLient\\Allegati\\Allegato1.DOC");                 
                 
                 //p.setSysPDF("c:\\tmp2.txt");   
                   
                 //p.setSysPDF("c:\\sqlnet.log"); 
                 
                 //LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\CCS.log");
                 //p.setSysPDF("CCS.log",f1.leggiFile());
                                                   
                // p.setFileName("c:\\letteraprova.pdf","clntr32.txt");
               
            //     p.setFileName("c:\\2foto.jpg");
                // p.setFileName("c:\\modello_ordinario.pdf");
                // p.setFileName("c:\\ORIGINE.rtf");
              //   p.setFileName("c:\\avenger.txt");
               // p.setFileName("c:\\CREAZIONE.sql");
                 //p.setFileName("C:\\WINDOWS\\nview\\default.tvp");
                 
                 //LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("c:\\nocache.reg");
                 //p.setFileName("c:\\ORIGINE.rtf",f2.leggiFile());
                 
                 //p.renameFileName("asilvia.JPG","asilvia.JPG",f.leggiFile());
               //  p.setFileName("clntr32.txt",f.leggiFile());
               //  p.setDeleteFileName("clntr32.txt");
              //   p.setFileName("sqlnet.p7m",f.leggiFile(),"clntr32.txt");
                 
                 //p.setFileName("c:\\clntr32.txt","DOCUMENTO.doc");
                 //p.setFileName("c:\\CCS.log");
                 
                // p.settaACL("AA4",Global.COMPLETE_ACCESS);                
                 //p.setDeleteFileName("das.txt");
                // p.setDeleteFileName("tmp2.txt");                 
                 //p.setScannedDocument();
                // p.settaRiferimento("43141","PROT_SOGG");
                 
        	  /* p.settaValore("TRASF_REPARTO_RICEVENTE","4");
        	   p.settaValore("DIAGNOSI_MEDICA","4");
        	   p.settaValore("PATOLOGIE_CONCOMITANTI_ALTRO","4");
        	   p.settaValore("ALLERGIE","4");
        	   p.settaValore("TRASF_ISOLAMENTO","4");
        	   p.settaValore("TRASF_PARENTI_AVVISATI","4");
        	   p.settaValore("TRASF_PARENTI_TEL","4");
        	   p.settaValore("TRASF_TERAPIE","4");
        	   p.settaValore("TRASF_TERAPIE_ULTIMA","4");
        	   p.settaValore("TRASF_INDAGINI","4");
        	   p.settaValore("TRASF_INDAGINI_TIPO","4");
        	   p.settaValore("TRASF_INDAGINI_UO","4");
        	   
        	   
        	   p.settaValore("TRASF_INDAGINI_ORA","2");
        	   p.settaValore("TRASF_FAMILIARI","2");
        	   p.settaValore("STATO_COSCIENZA_1","2");
        	   p.settaValore("TRASF_COMUNICAZIONE_ORIENTATO","2");
        	   p.settaValore("TRASF_COMUNICAZIONE_COLL","2");
        	   p.settaValore("TRASF_COMUNICAZIONE_LINGUAGGIO","2");
        	   p.settaValore("COMUNICAZIONE_TESTO","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_1","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_1B","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_2B","2");
        	   
        	   
        	   p.settaValore("TRASF_ALIMENTAZIONE_3","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_4B","2");
        	   
        	  
        	   p.settaValore("TRASF_ALIMENTAZIONE_5","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_5B","2");
        	   
        	   
        	   p.settaValore("TRASF_ALIMENTAZIONE_7","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_8","2");
        	   p.settaValore("TRASF_ALIMENTAZIONE_9","2");
        	   p.settaValore("TRASF_MOBIL_1","2");
        	   p.settaValore("TRASF_MOBIL_2","2");
        	   p.settaValore("TRASF_MOBIL_3","2");
        	   p.settaValore("TRASF_MOBIL_4","2");
        	   p.settaValore("TRASF_MOBIL_5","2");
        	   p.settaValore("TRASF_MOBIL_6","2");
        	   p.settaValore("DOLORE","2");
        	   p.settaValore("DOLORE_SEDE","2");
        	   p.settaValore("TRASF_DOLORE_ANTI","2");
        	   p.settaValore("SONNO","2");
        	   p.settaValore("SONNO_TESTO","2");
        	   p.settaValore("TRASF_ELIM_1","2");
        	   p.settaValore("TRASF_ELIM_2","2");
        	   p.settaValore("TRASF_ELIM_3","2");
        	   
        	   
        	   p.settaValore("TRASF_ELIM_5","2");
        	   p.settaValore("TRASF_ELIM_6","2");
        	   p.settaValore("TRASF_ELIM_7","2");
        	   p.settaValore("TRASF_ELIM_8","2");
        	   p.settaValore("TRASF_ELIM_9","2");
        	   p.settaValore("TRASF_ELIM_10","2");
        	   p.settaValore("TRASF_ELIM_11","2");
        	   p.settaValore("TRASF_ELIM_12","2");
        	   p.settaValore("TRASF_ELIM_13","2");
        	   p.settaValore("TRASF_ELIM_14","2");
        	   p.settaValore("TRASF_ELIM_15","2");
        	   p.settaValore("TRASF_ELIM_16","2");
        	   
        	   
        	   p.settaValore("TRASF_ELIM_18","2");
        	   p.settaValore("TRASF_ELIM_19","2");
        	   p.settaValore("TRASF_ELIM_20","2");
        	   p.settaValore("TRASF_RESP_1","2");
        	   p.settaValore("TRASF_RESP_2A","2");
        	   p.settaValore("TRASF_RESP_2","2");
        	   p.settaValore("TRASF_RESP_5","2");
        	   p.settaValore("TRASF_RESP_6","2");
        	   p.settaValore("TRASF_RESP_7","2");
        	   p.settaValore("TRASF_RESP_8","2");
        	   
        	   
        	   p.settaValore("TRASF_RESP_10","2");
        	   p.settaValore("TRASF_RESP_11","2");
        	   p.settaValore("TRASF_RESP_12","2");
        	   
        	    
        	   p.settaValore("TRASF_RESP_14","2");
        	   p.settaValore("TRASF_RESP_15","2");
        	   p.settaValore("TRASF_RESP_16","2");
        	   p.settaValore("TRASF_MED_1","2");
        	   p.settaValore("TRASF_MED_2","2");
        	   
        	   
        	   p.settaValore("TRASF_MED_4","2");
        	   p.settaValore("TRASF_MED_5A","2");
        	   p.settaValore("TRASF_MED_6A","2");
        	   p.settaValore("TRASF_MED_7A","2");
        	   p.settaValore("TRASF_MED_8A","2");
        	   
        	   
        	   p.settaValore("TRASF_MED_5B","2");
        	   p.settaValore("TRASF_MED_6B","2");
        	   p.settaValore("TRASF_MED_7B","2");
        	   p.settaValore("TRASF_MED_8B","2");
        	   
        	   
        	   p.settaValore("TRASF_MED_12","2");
        	   p.settaValore("TRASF_MED_13","2");
        	   p.settaValore("CUTE_ALTERATA_TESTO","2");

        	   
        	   p.settaValore("TRASF_MED_15","2");
        	   p.settaValore("TRASF_MED_16","2");
        	   p.settaValore("CUTE_ULCERE_SACRO_SINO","2");
        	   p.settaValore("CUTE_ULCERE_SACRO","2");
        	   p.settaValore("CUTE_ULCERE_TROC_DX_SINO","2");
        	   p.settaValore("CUTE_ULCERE_TROCANTERE_DX","2");
        	   p.settaValore("CUTE_ULCERE_TROC_SN_SINO","2");
        	   p.settaValore("CUTE_ULCERE_TROCANTERE_SN","2");
        	   p.settaValore("CUTE_ULCERE_TALLONE_DX_SINO","2");
        	   p.settaValore("CUTE_ULCERE_TALLONE_DX","2");
        	   p.settaValore("CUTE_ULCERE_TALLONE_SN_SINO","2");
        	   p.settaValore("CUTE_ULCERE_TALLONE_SN","2");
        	   p.settaValore("CUTE_ULCERE_ALTRO_TESTO","2");
        	   p.settaValore("CUTE_ULCERE_ALTRO","2");*/

               //p.setDeleteRiferimento("42495","RIF");
               //p.settaPadre("43141");
              //p.setStato(Global.STATO_COMPLETO);
              // p.removeCompetenza("GHELFI","U");
        	  // p.settaValore("UTENTE_ANN","PROVA");
        	  //  p.settaValore("ANNO","1255");
        	/*  java.math.BigDecimal d = new java.math.BigDecimal(132);     
        	  p.settaValore("ANNO",d);*/
        	  
        	  /* Calendar cal = Calendar.getInstance();
                                
                 java.sql.Timestamp now =
                     new java.sql.Timestamp(cal.getTimeInMillis());
                 java.sql.Date jsqlD = new java.sql.Date(now.getTime());
                 
                 p.settaValore("DATA",jsqlD);
        	  */
        	  
        	  // p.appendiValore("OGGETTO"," - PIppo");
        	   //p.appendiValore("CODICE_AMMINISTRAZIONE","333");
        	   //p.settaValore("ANNO","2222");
        	   
        	   //p.settaValore("LOG_PROTOCOLLO","FFFFF");
        	//p.creaFileNameImpronta("modello_ordinario.pdf");
        //	p.settaValore("NOMEPILOTA","ddddddddsdasdsa");
        	 /* p.settaValore("COD_COMPONENTE","12663");
        	  p.settaValore("AMM_DESC","MECCIA__");
        	  p.settaValore("PESO","153");
        	  p.settaValore("DATA_MANNELLA","01/01/2006 10:12:00");*/
        	//System.out.println(p.getTimeStampDataUltimoAggiornamento());
        	   
        	//  p.setFileName("c:\\Prova Parentesi (aaaa).txt");
        	   //LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\TO META\\Pino.rtf");
       	  // String s = slurp(f1.leggiFile());
//               
        	 /*  p.settaValore("COGNOME","ZZZZ444");
        	   
        	   p.setAggiornaDataUltAggiornamento(false);
        	   
                p.setFileName("LETTERAUNIONE.RTFHIDDEN",f1.leggiFile());*/
        	   
        	   /*p.settaValore("TEST_CHECK","123456789012");
        	   
        	   Calendar cal = Calendar.getInstance();
        	   java.sql.Timestamp now =
                   new java.sql.Timestamp(cal.getTimeInMillis());
               java.sql.Date jsqlD = new java.sql.Date(now.getTime());
               
               p.settaValore("TEST_DATA",jsqlD);*/
        	  // p.settaValore("DESCRIZIONE","aaa"); 
        	 //  p.setFileName("c:/bfile.txt");
        	
        	  // p.setFileName("c:/to_meta/post chiusura doc.jpg");
        	  
       	//p.setFileName("c:/ojdbc14.jar");
        	//    LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:/pipp'o.txt");
           	  // p.renameFileName("DELIBERA_2011_70_DELG.pdf","DELIBERA_2011_70_DELG.pdf.P7M",f1.leggiFile());
    //	p.setFileName("c:/pipp'o.txt",f1.leggiFile());
        //	 p.setDeleteFileName("pippo.jpg");
        	   
        	   //p.settaValore("TEXT_AREA","bbb"); 
        	   //p.setDontRepeatExistsRif(true);
        	   //p.settaRiferimento("856","LINK_DOC");
        	   
        	 //  p.generaImpronta512("Dichiarazione_regime aiuti all''''occupazione_DEF.doc.P7M");
        	  // p.settaValore("ANNO_CORRENTE","2009");
        	 //  p.setFileName("c:\\a.txt");
        	   
        	  // ImprontaAllegati ia = new ImprontaAllegati(p);
        	   //Per tutti i file presenti:
        	  
        	 //  String nomeFile="b.txt";
        	 //  String verifica = ia.verificaImpronta(nomeFile);
        	 /*  if (verifica.equals(Global.CODERROR_IA_IMPRONTA_ASSENTE)  )  
        		   ia.generaImpronta(nomeFile);        	  
        	   else if (verifica.equals(Global.CODERROR_IA_ALLEGATO_MODIFICATO)) {
        		   ia.cancellaImpronta(nomeFile);
        		   ia.generaImpronta(nomeFile);
        	   }
        	   else if (verifica.equals(Global.CODERROR_IA_ALLEGATO_CANCELLATO))  
        		   ia.cancellaImpronta(nomeFile);
        	   
        	   */
        	  /* LetturaScritturaFileFS f1 = new LetturaScritturaFileFS("c:\\dabytearray.txt");
        	   p.renameFileName("ctapi_out_gr.txt", "pippo.txt", f1.leggiFile());*/
        	   
        	   //p.settaValore("INTESTAZIONE_PDF_2", "");
        	   //p.settaValore("TEST_DATA", "31/09/2013");
        	  // p.addCompetenza("PIGNATTI", "D"); 
        	 
        	   if (!p.salva().booleanValue()) { 
                     System.out.println(p.getError());                     
                     System.out.println(p.getCleanError());
                     
                     System.out.println(p.getErrorPostSave());
                     
                     if (casoConnection.equals(CONNECTION_EXTERN)) {
                         conn.rollback();         
                     }                    
                     
                   
               }
               else {
                	
                	 System.out.println("Aggiornato!");
                	 System.out.println("--->"+p.getStringDataUltimoAggiornamento());
                	 System.out.println(p.getDocNumber());
                	 
                     System.out.println("Errore cod post save: " + p.getCodeErrorPostStave());
                     System.out.println("Errore post save: " + p.getErrorPostSave());
                	  //p.accedi();
                     //System.out.println(p.getError());
//                	 p.accedi(Global.ACCESS_ATTACH);
//                	 
//                	  LetturaScritturaFileFS fBlob2 = new LetturaScritturaFileFS("c:\\temp\\mBlob.doc");
//                      fBlob2.scriviFile(p.getFileStream("mBlob.doc"));
//                      
//                      LetturaScritturaFileFS f111 = new LetturaScritturaFileFS("c:\\temp\\m1.doc");
//                      f111.scriviFile(p.getFileStream("m1.doc"));                	 
               }
         
             }
             catch (Exception e) 
             {  
            	 e.printStackTrace();
                  if (casoConnection.equals(CONNECTION_EXTERN)) {
                     // conn.rollback();         
                  }
                 System.out.println(p.getError());
                 System.out.println(p.getCleanError()); 
             }
        
      }          
      
      if ("3".equals("")) {        // ACCEDI DOC TRAMITE ID UNIVOCO
             try {
            	 //	System.out.println( p.getArea());
            	 //System.out.println(p.getlistaAllegatiTemp());
            //	p.removeTypeAclReturn(Global.ABIL_LETT);
            	//p.addTypeAclReturn(Global.ABIL_MODI);
            	 //System.out.println( p.getGestioneCompetenzeAllegati());
            	// p.addTypeAclReturn(Global.ABIL_LETT);
            	// p.addTypeAclReturn(Global.ABIL_MODI);
            	// p.addTypeAclReturn(Global.ABIL_CANC);
            	 
                // if (p.accedi(Global.ACCESS_ATTACH,false,Profilo.RETRIEVE_ALLACL_USER).booleanValue()) {
            	// ElapsedTime elpsTime = new ElapsedTime("TEST",null);
        	   //  elpsTime.start("INSERT","INIZIO");    
            
            	 
            	 
            	 if (p.accedi(Global.ACCESS_ATTACH)) {
            		 System.out.println("TUTTO OK");
            		 
            		 p.getFileStream("INTERMANDATI_2013_12_25_09_30_46_173_2.xml.p7m");
            		 //LetturaScritturaFileFS fs = new LetturaScritturaFileFS("c:\\temp\\ecccccodi.pdf");
            		 //fs.scriviFile( p.getFileStream("ANF COMPILATO.pdf"));
            		// String data=p.getCampo("DATA");
            		 
            		// String orario= data.substring(11);
            		// System.out.println(data.length()+" "+orario);
            		// System.out.println( p.getIdFile("/iride_file/FILE2017/003236/3235843.pdf.p7m"));
            		// System.out.println("-->"+p.getlistaFile());
            		 
            		// p.getFileStream("all. 1_allegato A).doc.p7m");
            		 
            	//	 if(1==1) return;
            		 
            		 /*GD4_Oggetti_File oFile = new GD4_Oggetti_File();
                	 
            		 Environment env = new Environment("","","","","",conn,true);
            		 env.connect();
            		 oFile.inizializzaDati(env);
            		 
            		 oFile.setIdOggettoFile("10000043");
            		 
            		 InputStream is = oFile.downloadBlobBFile_ToInputStream();
            		 
            		
            		 LetturaScritturaFileFS f = new LetturaScritturaFileFS("c:\\xxx.pdf");
            		 f.scriviFile(is);
            		 is.close();
            		 oFile.closeBFile();
            		 
            		 env.disconnectClose();
            		 
            		 System.out.println(p.getCampo("NUMERO"));*/
            		 
            		// elpsTime.stop();
            		// System.out.println("ENTOR-->");
                	//System.out.println( p.getRiferimenti());
                	// System.out.println( p.getlistaFile());
                	// System.out.println( "AAAA-->11-->"+p.getCampo("DESCRIZIONE"));
                	// System.out.println( p.getCompLettura() );
                	// System.out.println( p.getCompManage() );
                	 
                	 
                	/* LetturaScritturaFileFS f = new LetturaScritturaFileFS("c:\\piccolo.txt");
                     f.scriviFile(p.getFileStream("PippO.txt"));
                     LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("c:\\grande.txt");
                     f2.scriviFile(p.getFileStream("pippo.txt"));*/
                	// System.out.println("-->"+p.getCampo("OGGI"));
                	 
                	/* HashMapSet hms = p.getCompetenze();
                	 Iterator i =  hms.getIterator();
       		       
                     while (i.hasNext()) {
                   	  //System.out.println(i.next());
                   	 String user=""+i.next();
                   	 System.out.println("-->"+user);
                   	  Iterator iIntern = hms.getHashSet(user);
                   	  while (iIntern.hasNext()) {                    		 
                   		 System.out.println(iIntern.next());
                   	  }
                     }*/
                	 
                	 
                	/*System.out.println(p.getStringDataUltimoAggiornamento());
                	String data= p.getStringDataUltimoAggiornamento();
                	String gg = data.substring(6,8);
                	String mm = data.substring(4,6);
                	String aa = data.substring(0,4);
                	System.out.println(gg+"/"+mm+"/"+aa);*/
                	// System.out.println(p.getCampo("OGGETTO"));
                	 
                	 
                	/* Vector<ACL> v = p.getListaCompetenze();                	
                	 String nominativo="", nominativoAppenaPassato="";
                	 
                	 String tipoCompDaTrattare="L";
                	 boolean bCicloUtente=false;
                	 HashMap<String,String> hpUte = new HashMap<String,String>();
                	 for(int i=0;i<v.size();i++) {
                		ACL acl =  v.get(i);
                		
                		String livello="-1";*/
                		
                		//System.out.println("1--->"+acl.getNominativo());
                		//System.out.println("1--->"+acl.getTipoCompetenza());
                		//System.out.println("1--->"+acl.getAccesso() );  
                		
                		//if (acl.getNominativo().equals("Relatore(AGDRELA)")) 
                	//		livello="-1";
                		
                		
                		                                                		
            		/*	if (acl.getTipoCompetenza().equals("L") && (acl.getAccesso()==1))
            				livello="2";

            			if (acl.getTipoCompetenza().equals("U") && (acl.getAccesso()==1))
            				livello="1";
            			
            			if (acl.getTipoCompetenza().equals("D") && (acl.getAccesso()==1))
            				livello="0";  
            		
            			if (!hpUte.containsKey(acl.getPersonGroup()) || (hpUte.containsKey(acl.getPersonGroup()) && (!livello.equals("-1"))  )) 
                			hpUte.put(acl.getPersonGroup(), livello);
            			
                	 }*/
                	 
                	 
                	/* Iterator it = hpUte.keySet().iterator();
                	 while (it.hasNext()) {
                		    String element =  (String)it.next();
                		    System.out.println(element + " " + hpUte.get(element));
                	 }*/
                	 
                	 
                	/* Iterator it = hpUte.keySet().iterator();
                	 
                	 IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(conn,0);
                	 Vector vGruppiEUteNominativi= new Vector();
                	 Vector vUtenti= new Vector();
                	 Vector vUtentiNominativi= new Vector();
                	 String listaGruppi="";
                 	 while (it.hasNext()) {
                 			String element =  (String)it.next();
                 			if (!hpUte.get(element).equals("-1")) {
                 				System.out.println(element + " " + hpUte.get(element));
                 				
                 				                 				                				
                 				dbOp.setStatement("select nominativo,tipo_utente from ad4_utenti where ad4_utenti.utente = '"+element+"'");
                 				dbOp.execute();
                 				ResultSet rst = dbOp.getRstSet();
                 				rst.next();
                 				
                 				if (rst.getString(2).equals("U")) {
                 					vUtenti.add(element);
                 					vUtentiNominativi.add(rst.getString(1));
                 				}
                 				else {
                 					vGruppiEUteNominativi.add(rst.getString(1));
                 					if (!listaGruppi.equals("")) listaGruppi+=",";
                 					listaGruppi+="'"+element+"'";
                 				}                 				
                 					
                 					
                 			}
                 				
                 	 }*/
                 	 
                 	 /*for(int i=0;i<vUtenti.size();i++) {
                 		String sqlCheck="select descrizione from ad4_struttura_utenti, ad4_utenti grup "+
                 				        "where grup.utente in ("+listaGruppi+") and figlio= '"+vUtenti.get(i)+"' "+
                 				        "and (instr(struttura,'#'||grup.utente||'#') <>0 or instr(struttura,'#'||grup.utente||'[')<>0) "+
                 				        "group by descrizione";
                 		System.out.println(sqlCheck);
                 		dbOp.setStatement(sqlCheck);
                 		dbOp.execute();
                 		ResultSet rst = dbOp.getRstSet();
                 		if (!rst.next()) {
                 			vGruppiEUteNominativi.add(vUtentiNominativi.get(i));
                 		}
                 	 }*/
                 	 
                 	/*for(int i=0;i<vGruppiEUteNominativi.size();i++) {
                 		System.out.println("NOMIN--->"+vGruppiEUteNominativi.get(i));
                 	}
                 	
                 	dbOp.close();*/
                 	/* if (!sListaUtenti.equals("")) {
                 		sListaUtenti="("+sListaUtenti+")";
                 		
                 		String sqlCheck=" select utente from ad4_utenti "+
										" where utente in ( "+
										"	 select figlio from ad4_struttura_utenti, ad4_utenti grup, ad4_utenti ute where " +
										"	 grup.utente in "+sListaUtenti+"  and  grup.tipo_utente in ('O','G') and "+
										"	  ute.utente in "+sListaUtenti+"  and ute.tipo_utente ='U' and "+
										"	  figlio = ute.utente  and "+
										"	 (instr(struttura,'#'||grup.utente||'#') <>0 or instr(struttura,'#'||grup.utente||'[')<>0)  "+
										"	 group by figlio "+
										"	 union " +
										"	 select grup.utente "+
										"	 from ad4_utenti grup "+
										"	 where grup.utente in "+sListaUtenti+"  and  grup.tipo_utente in ('O','G') "+
										"	 )";
                 		System.out.println(sqlCheck);
                 		IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(conn,0);
                 		dbOp.setStatement(sqlCheck);
                 		dbOp.execute();
                 		
                 		ResultSet rst = dbOp.getRstSet();
                 		
                 		while(rst.next()) {
                 			System.out.println("NOMIN--> "+rst.getString(1));
                 		}
                 		dbOp.close();
                 	 }*/
                 		                  	 
              	         
                	 
                	 /*
                	 Vector<ACL> v2 = p.getListaTutteCompetenze("GDM");
                	 
                	 for(int i=0;i<v2.size();i++) {
                		System.out.println("2--->"+v2.get(i).getNominativo());
                	 }
                	 */
                	/* System.out.println("3--->"+p.verificaCompetenza("GDM",Global.ABIL_LETT));*/
                	 
                	// p.getFileStream("m1.doc");
                	// p.getFileStream("m2.doc");
                	 /* LetturaScritturaFileFS f = new LetturaScritturaFileFS("d:\\m1.doc");
                      f.scriviFile(p.getFileStream("m1.doc"));
                      LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("d:\\m2.doc");
                      f2.scriviFile(p.getFileStream("m2.doc"));*/
                	// System.out.println(p.getStato());
                	// System.out.println(p.getlistaFile());
                	 //p.settaValore("CODICE_AMMINISTRAZIONE",""+new java.math.BigDecimal(0));
                	 
                /*	 if (p.getCampo("NOMEPILOTA")==null)
                		 System.out.println( "NULL");
                	 else
                		 System.out.println( "-->"+ p.getCampo("NOMEPILOTA")+ "<--");*/
        	   
                     //System.out.println(p.getCampo("___COGNOMEPILOTA"));
                    // System.out.println(p.getCampo("___NOMEPILOTA"));
                                          
                     //System.out.println("CAMPO-->"+p.getCampo("IDRIF"));
                     //System.out.println(p.getIdFile("aaa.txt"));
                     //System.out.println(p.visualizzaFile("ACK.txt"));
                     //System.out.println(p.getCodiceRichiesta());
                     //System.out.println(p.getCampo("NOMEPILOTA"));
                     
                    // System.out.println(p.getPadre());
                    // System.out.println("--->"+p.getStringDataUltimoAggiornamento());

                     // System.out.println(p.getlistaFile());
                      
                      //System.out.println(p.getFile(2));
                      //System.out.println(p.getFileStream(2));
                      //System.out.println(p.getFileName(2));
                      

                     //System.out.println(p.verificaImpronta("tmp.txt"));
                     //
                    // System.out.println(p.getSystemFileStream("tmp2.txt",Global.SYS_PDF));
                     
                     //System.out.println("-->"+p.getP7MFileStream("tmp.txt"));
                    // System.out.println("-->"+p.getFileNameP7M("tmp.txt"));
                   
                     
                        // System.out.println(p.getRiferimenti());
                        // System.out.println(p.getRiferimentiFrom("SEGRETERIA.PROTOCOLLO","PROT_SOGG"));
                   
                    /* p.inizioLetturaRiferimentiDocPrincipale();
                     Riferimento chiave=p.successivoRiferimentoDocPrincipale();
                     while(chiave!=null)
                     {
                     
                        System.out.println("-->"+chiave.getIdDocumentoRif());
                        chiave=p.successivoRiferimentoDocPrincipale();                        
                   
                     }
                     System.out.println("--xxxxxx----");
                         */
                 /*    p.inizioLetturaRiferimentiDocRif();
                     Riferimento chiave1=p.successivoRiferimentoDocRif();
                     while(chiave1!=null) {                     
                        System.out.println("**-->"+chiave1.getIdDocumentoRif());
                        System.out.println("**-->"+chiave1.getIdDocumento());
                        System.out.println("************************");
                        Profilo pr=chiave1.getDocPrincipale();
                        System.out.println("+++-->"+pr.getDocNumber());
              
                        chiave1=p.successivoRiferimentoDocRif();                                           
                     }*/
                     
                                
                   
                     /*
                     Riferimento chiave1=p.successivoRiferimentoDocPrincipale();
                     System.out.println("-->"+chiave1.getIdDocumentoRif());
                     Riferimento chiave2=p.successivoRiferimentoDocPrincipale();
                     System.out.println("-->"+chiave2.getIdDocumentoRif());
                     Riferimento chiave3=p.successivoRiferimentoDocPrincipale();
                     System.out.println("-->"+chiave3.getIdDocumentoRif());
                     try {
                       Riferimento chiave4=p.successivoRiferimentoDocPrincipale();
                       System.out.println("-->"+chiave4.getIdDocumentoRif());
                     }
                     catch (NullPointerException e) {
                       System.out.println("-->FINENU");
                     }
                   */
                     /*while(p.successivoRiferimentoDocPrincipale()!=null) 
                     {
                       Riferimento chiave=p.successivoRiferimentoDocPrincipale();
                       System.out.println("-->"+chiave.getIdDocumentoRif());
                     }*/
                
      /*            
                     p1.initVarEnv("GDM","GDM", conn);
      
                     if (p1.accedi(Global.ACCESS_ATTACH).booleanValue())
                     {
                     
                     p1.();
                     Riferimento chiave1=p1.successivoRiferimento();
                     String idDocrif1=chiave1.getIdDocumentoRif();
                     System.out.println("44-->"+idDocrif1);
                    }
                  //  LetturaScritturaFileFS f = new LetturaScritturaFileFS("d:\\sqlnet.log");
                     //f.scriviFile(p.getFileStream("sqlnet.log"));    
          */         
                	 
                //	 p.settaValore("UTENTE_ANN","PROVA");
                	 
                //	InputStream i= p.getFileStream("LETTERAUNIONE.RTFHIDDEN"); 
                	//System.out.println(i);
                /*	LetturaScritturaFileFS f = new LetturaScritturaFileFS("C:\\LETTERAUNIONE.RTFHIDDEN.RTF");
                    f.scriviFile(i);*/ 
                    
                    
                   /* InputStream i2= p.getFileStream("modello_ordinario.pdf");
                   
                    LetturaScritturaFileFS f2 = new LetturaScritturaFileFS("C:\\PIPPPPO2.PDF");
                    f2.scriviFile(i2); 
                    */
                	
                	//System.out.println(p.getFile(1));
                	// System.out.println(p.getCampo("COGNOME"));
                	 
                	
                	/* if (!p.salva().booleanValue()) {
                    	 
                         System.out.println(p.getError());
                         System.out.println(p.getCleanError());
                         if (casoConnection.equals(CONNECTION_EXTERN)) {
                             conn.rollback();         
                         }                  
                         
                        
                         
                   }
                   else {
                    	
                    	 System.out.println("Aggiornato!");
                    	 System.out.println("--->"+p.getStringDataUltimoAggiornamento());
                    	 
                    	
                         System.out.println(p.getError());
                   }*/
                	/* 
                	System.out.println( p.getCampo("COD_COMPONENTE"));
                	System.out.println( p.getCampo("AMM_DESC"));
                	System.out.println( p.getCampo("PESO"));
                	System.out.println( p.getCampo("DATA_MANNELLA"));*/
                	/* Hashtable v = p.getLinks("GDMSYS","CartellaStandard");
                	 Iterator it = v.keySet().iterator();

                	while (it.hasNext()) {
                			String element =  (String)it.next();
                				System.out.println(element + " " + (String)v.get(element));
                		}*/
             	         
                	 
                	// String allegati=p.getlistaFile("@");
                	// System.out.print(allegati);	

                	// String[] arr = allegati.split("@");
                	 
                	// if(!arr[0].equals("") || arr[0].length()>1) 
                	// System.out.print("AAA"+arr.length);
                	 
                	 //InputStream is=p.getFileStream("carta carb 2° trim.txt");
                	 //System.out.println( p.getFileStream("Carta identità Michele valida al 24-03-2015.pdf"));
                	 
                	// System.out.println(""+p.getDocNumber());
                	/* System.out.println( p.getError());
                	 
                	 
                	 System.out.println(p.getCampo("COD_AMM"));
                	 if (p.getCampo("COD_AMM")==null)
                		 System.out.print("AAA"+p.getCampo("COD_AMM"));
                	 else
                		 System.out.print("BBB"+p.getCampo("COD_AMM"));
                	 
                	 System.out.println(p.getDataCreazione());*/
            		 
            		// System.out.print("---->*"+p.getCampo("ANAMNESI_FAMILIARE")+"*");
            		 
            		/*Vector<ACL> v =p.getListaCompetenze();
            		for (int i=0;i<v.size();i++) {
            			System.out.println("-->"+v.get(i).getNominativo());
            		}*/
            		 
            		 if (p.getCampo("ID_DOCUMENTO_ADOCER")==null)
            			 System.out.print("--->xxx");
            		 else
            			 System.out.print("--->"+p.getCampo("ID_DOCUMENTO_ADOCER"));
                	 
            		 
            		 
                	// System.out.println("FILE->"+p.getlistaFile("#"));
                	// System.out.println("il 1° FILE->"+p.getFile(1));
                 }
                 else {
                     System.out.println("Impossibile accederesssss");
                     try {
                    	 if  (p.getLastException().getMessage().indexOf("ORA-00054")!=-1) System.out.println("Errore11-->");
                    	 //java.sql.SQLException sqEx = (java.sql.SQLException) p.getLastException();
                    	// System.out.println("Errore-->"+sqEx.getErrorCode());
                     }
                     catch (Exception e) 
                     {
                    	 e.printStackTrace();
                     }
                     
                     System.out.println("Impossibile accederesssss222");
                     
                     System.out.println("ERROR->"+p.getError());
                     System.out.println(p.getCodeError());
                     System.out.println(p.getCleanError()); 
                     if (casoConnection.equals(CONNECTION_EXTERN)) {
                      conn.rollback();         
                    }
                     
                    
                 }
               }
               catch (Exception e) 
               {
                  if (casoConnection.equals(CONNECTION_EXTERN)) {
                      conn.rollback();         
                  }
                   System.out.println("-->"+p.getError());
                
                   System.out.println(e.getMessage());
               }
               
              /* System.out.println("-->"+p.getDocNumber());
               
               p.settaRiferimento("48988",Global.RIF_VERSIONE);
               
               System.out.println("ERROR-->"+p.getError());
               
               if (p.salva().booleanValue()) {
            	   System.out.println("A SI TROPPU PUPPA");
            	  
               }
               else
            	   System.out.println("HAI RAGIONE....SONO UN PUPPO");*/
        
      }      
            
      if (casoConnection.equals(CONNECTION_EXTERN)) {
         //conn.rollback();
         conn.commit();
        conn.close();
    	  
    	 // System.out.println("AAAAA");
         
      }
           
    }   
  

  
  static String timeStampToString(Timestamp t) {
		String format = "dd/MM/yyyy HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss.S");
		String property = t.toString();
		String out=null;
		try
		{
			
		java.util.Date dataOut = (java.util.Date) formatter.parse(property, new java.text.ParsePosition(0));
		SimpleDateFormat formatter1 = new SimpleDateFormat (format);
		out = formatter1.format(dataOut);
		}
		catch(Exception exp){exp.printStackTrace();}
		return out; 
  }
  
  public static String slurp (InputStream in) throws IOException {
	         StringBuffer out = new StringBuffer();
	         byte[] b = new byte[4096];
	         for (int n; (n = in.read(b)) != -1;) {
	             out.append(new String(b, 0, n));
	         }
	         return out.toString();
	     }

}
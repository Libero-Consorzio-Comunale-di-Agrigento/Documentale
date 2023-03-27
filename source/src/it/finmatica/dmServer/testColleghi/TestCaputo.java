package it.finmatica.dmServer.testColleghi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.competenze.Abilitazioni;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.competenze.UtenteAbilitazione;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.management.*;
import java.util.Properties;


public class TestCaputo {
	   private  static String                      keyValuemodel="";
	   private  static Hashtable<String,Hashtable> keyValueStructure;
	   
	   public static void main(String[] args) throws Exception {
		      
		 /*  Hashtable p = new Hashtable();
		   
		   p.put("A","1");
		   p.put("A","2");
		   p.put("B","2");
		   p.put("B","3");
		   
		   System.out.println("AAAA");*/
		   
		/*   
		    HashMap map = new HashMap();
		    map.put( "cat", "Meow" );
		    map.put( "ape", "Squeak" );		 
		    //map.put( "dog", "Woof2" );
		    map.put( "bat", "Squeak" );
		    	System.out.print( "Enumerate the Hashtable: "+map.get("cat"));
		    	if (map.get("niente")==null)
		    	System.out.print( "Enumerate the Hashtable: "+map.get("niente"));*/
		/*   Calendar cal = Calendar.getInstance();
		    java.sql.Timestamp now = new java.sql.Timestamp(cal.getTimeInMillis());
           java.sql.Date jsqlD = new java.sql.Date(now.getTime());
		    
		   SimpleDateFormat formatter = new SimpleDateFormat ("dd/MM/yyyy hh:mm:ss");
		   System.out.println(formatter.format(jsqlD));*/
		   
		   	   Connection conn=null;
		       Class.forName("oracle.jdbc.driver.OracleDriver");
		   	   conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.95:1521:GAM","CF4ARM","CF4ARM");
		   	   
		   	conn.close();
		       //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.19:1521:PRMOD","GDM","GDM");
		   	  // conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.10.31:1521:GDMTEST","GDM","GDM");
		   	   
	/*	   	   try {

    Profilo  p=new Profilo("M_ORIZZONTALE","TESTADS");
    // p.escludiControlloCompetenze(true);
      p.initVarEnv("GDM", null,conn);
      p.setAlwaysNew(true);
      
      p.setAlwaysNew(true);
      
      for (int i=0;i<3;i++) {
      p.settaValore("TEXT_AREA","c");
      p.settaPadre("10");
       if(p.salva().booleanValue()){
       		conn.rollback();  
          System.out.println("Salvato");
       }
        else{
          System.out.println("NON Salvato "+p.getError());
      }
      }
		}
	   catch (Exception e) { 
	     e.printStackTrace();
	     conn.rollback();  
	   }
		   	   
   conn.commit();
   conn.close();

		   	   */
		   	   
		   	   
		   	  /* Environment varEnv = new Environment("GHELFI", "GHELFI", "", "", "", conn);
		   	   
		   	   GDM_Competenze gdmComp = new GDM_Competenze(varEnv);
		   	   
		   	   UtenteAbilitazione ua = new UtenteAbilitazione("GHELFI", null, null, null);
		   	   
		   	   Abilitazioni ab = new Abilitazioni("DOCUMENTI", "225849", "U");
		   	   
		   	   System.out.println((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua,ab));*/
		   	  // gdmComp.si4AssegnaCompetenza(ua, ab);
			     // conn.commit();
		   /*HashSet h = new HashSet();
		   
		   h.add( "Woof");
		   h.add( "Woof2");
		   
		   
		    HashMap map = new HashMap();
		    map.put( "cat", "Meow" );
		    map.put( "ape", "Squeak" );
		    map.put( "dog", h );
		    //map.put( "dog", "Woof2" );
		    map.put( "bat", "Squeak" );
		    	System.out.print( "Enumerate the Hashtable: "+(HashSet)map.get("dogf"));
		    System.out.print( "Enumerate the Hashtable: "+((HashSet)map.get("dog")).contains("Woof2") );*/
    /*Enumeration e = (Enumeration)map.get("dog");
    while ( e.hasMoreElements() )
      System.out.print( e.nextElement() + " ");
    System.out.println();*/
           
		   
		 /*   HashMapSet hms = new HashMapSet();
              
              hms.add("ferrari","schumacher");
              hms.add("mclaren","raikkonen");
              hms.add("ferrari","massa");
              hms.add("mclaren","hamilton");
              hms.add("mclaren","de la rosa");
              
              Iterator i =  hms.getHashSet("ferrari");

              if (i==null) return;

              while (i.hasNext())
            	   System.out.println("-->"+i.next());*/
		   
		   
		   //HashMap hMap = new HashMap();
		   //keyval k = new keyval("a","b");*/
		   /*if (!hMap.containsKey("MANNY@F1"))
		       hMap.put("MANNY@F1",""+(hMap.size()+1));
		   
		   if (!hMap.containsKey("PINO@F1"))
		       hMap.put("PINO@F1",""+(hMap.size()+1));
		   
		   if (!hMap.containsKey("MANNY@F1"))
			   hMap.put("MANNY@F1",""+(hMap.size()+1));*/
		 /*  hMap.put("1",k);
		   Iterator i = hMap.entrySet().iterator();
		   
		   while (i.hasNext()) {
			   System.out.println("*-->"+i.next());
		   }*/
		   
		   //System.out.println("-->"+hMap.get("MANNY@F1"));
		  
		   
		   /*
		   keyValueStructure = new Hashtable<String,Hashtable>();
		   keyValuemodel="ROMEO#M_PROTOCOLLO|ANNO#N#=#2008|DATA#D#BETWEEN#01/01/2009#01/01/2010|NUMERO#N#<>#67§SEGRETERIA#M_DETERMINE|NUMERO_DET#N#>#56";
			   
		   StringTokenizer bloccoModello = new StringTokenizer(keyValuemodel, "§");
		   
		   while (bloccoModello.hasMoreTokens()) {
			     int conta=0;
			     String arcm="";
			     String blocco = bloccoModello.nextToken();
			     
			     StringTokenizer bloccoCampi = new StringTokenizer(blocco, "|");
			     
			     while (bloccoCampi.hasMoreTokens()) {
			    	   String campoValore=bloccoCampi.nextToken();
			    //	   System.out.println("AAA"+conta);
			    	   //Creo
			    	   if (conta++==0) {
			    		   Hashtable<String,valoriOperatori> hmc = new Hashtable<String,valoriOperatori>();
			    		   arcm=campoValore;
			    		   keyValueStructure.put(campoValore,hmc);
			    		//   System.out.println(arcm);
			    	   }
			    	   else {
			    		   
			    		   Hashtable<String,valoriOperatori> hmc = keyValueStructure.get(arcm);
			    	//	   System.out.println(campoValore.substring(0,campoValore.indexOf("#")));
			    	//	   System.out.println(campoValore.substring(campoValore.indexOf("#")+1));
			    		   //hmc.put(campoValore.substring(0,campoValore.indexOf("#")),campoValore.substring(campoValore.indexOf("#")+1));
			    		   
			    		   StringTokenizer valoriOperatori = new StringTokenizer(campoValore, "#");
			    		   int contaValoriOp=1;
			    		   String value=null, value2=null;
			    		   String operatore=null;
			    		   String campo=null, tipo=null;
			    		   while (valoriOperatori.hasMoreTokens()) {
			    			      String key=valoriOperatori.nextToken();
			    			      
			    			      switch(contaValoriOp) {
			    			      case 1:
			    			    	   campo=key;
			    			    	   break;
			    			      case 2:
			    			    	   tipo=key;
			    			      case 3:
			    			    	   operatore=key;
			    			    	   break;
			    			      case 4:
			    			    	   value=key;
			    			    	   break;
			    			      case 5:
			    			    	   value2=key;
			    			    	   break;
			    			      }
			    			      
			    			      contaValoriOp++;
			    		   }
			    		   
			    		   hmc.put(campo,new valoriOperatori(campo,value,operatore,value2,tipo));
			    		   //keyValueStructure.remove(campoValore);
			    		   keyValueStructure.put(arcm,hmc);
			    	   }
			    	//   System.out.println("_______________");
			    	   
			     }
			     
			     //System.out.println("AAA");
		   }
		   
		   Iterator myVeryOwnIterator = keyValueStructure.keySet().iterator();
		   while(myVeryOwnIterator.hasNext()) {
		       String sArCm=""+myVeryOwnIterator.next();
		       System.out.println(sArCm);
		       Hashtable<String,valoriOperatori> hmc = keyValueStructure.get(sArCm);
		       Iterator myVeryOwnIteratorIntern = hmc.keySet().iterator();
		  
		       while(myVeryOwnIteratorIntern.hasNext()) {
		    	   String sCampo=""+myVeryOwnIteratorIntern.next();
		    	   System.out.println(sCampo);
		    	   System.out.println(hmc.get(sCampo).value);
		    	   System.out.println(hmc.get(sCampo).operatore);
		    	   System.out.println(hmc.get(sCampo).value2);
		    	   System.out.println(hmc.get(sCampo).tipo);
		       }
		       
		       System.out.println("_______");
		   }
		   
		   /*keyValuemodel="C&amp;CartellaStandard&amp;GDMSYS";
StringTokenizer bloccoModello = new StringTokenizer(keyValuemodel, "&amp;");
		   
		   while (bloccoModello.hasMoreTokens()) {
			     int conta=0;
			     String arcm="";
			     String blocco = bloccoModello.nextToken();
			     System.out.println(blocco);
		   } */

		   
	   }
}

class valoriOperatori {
	  String value=null, value2=null;
	  String operatore=null;
	  String campo=null, tipo=null;
	  
	  public valoriOperatori(String campo,String value,String operatore,String value2,String tipo) {
		     this.value=value;
		     this.value2=value2;
		     this.operatore=operatore;
		     this.tipo=tipo;
	  }
}

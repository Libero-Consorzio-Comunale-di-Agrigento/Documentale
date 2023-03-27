package it.finmatica.dmServer.jdms;



public class Main 
{
  final static String CONNECTION_EXTERN="EXTERN";
  final static String CONNECTION_STANDARD="STANDARD";

  
  public static void main(String[] args) throws Exception {
     
	/*String s= "\u0007\u0001THE\u0005qui\u0002k1y2\u0004OX3j~\u0003p4@V#R5lazY6D%GS7890" + 
    (char)11+ "the" + (char)15 +"QUI" + (char)12 + "K" + (char)16 +"d" + (char)17 + (char)14 + "ox" + (char)18 +"J" + (char)20 + (char)13 + "P" + (char)19 + (char)21 + "v" + (char)22 + "r";
	System.out.println(s);   
	   */
	  
//	  int n=12;
//	  int p=8;
//	  
//	  
//	  int r = n%p;
//	  
//	  System.out.println("1) "+n%p);
//	  
	  
	/*  Cryptable cr = new Cryptable();
	  String v="daniela";
	  String s =cr.cryptPasswd(v);
	  System.out.println(v); 
	  System.out.println(s); 
	  System.out.println(cr.decryptPasswd(s)); */
	  
	  /*System.out.println("[PORTA][_C_][SERVER]b[LOGIN][_C_]h[TESTO]ciao");
		//String lista="[LOGIN]jwf[PASSWORD]jwf[DRIVER]oracle.jdbc.driver.OracleDriver[DSN]jdbc:oracle:thin:@masterdoc:1521:ORCL[ALIAS]oracle.[NOMEITER]CURRICULUM";
		String lista="[PORTA][_C_][SERVER]b[LOGIN][_C_]h[TESTO]ciao";
		String tokenC="[_C_]";
		String tokenD="{_C_}";
		String token="@@@";
		String xmlLista="";
		String s=lista;
		
		s=s.replace(tokenC,"{_C_}");
		s=s.substring(1,s.length());
		
		if(s.indexOf(token)!=-1)
		 token="###";	
		
		s=s.replace("[",token);
		String l[]=s.split(token);
		
		
		for(int i=0;i<l.length;i++){
			String e=l[i];
			if(e.indexOf(tokenD)!=-1){
			  String nome=e.substring(0,e.indexOf(tokenD)-1);
			  String valore=e.substring(e.indexOf(tokenD)+5,e.length());
			  xmlLista+="<PARAMETRO><NOME>"+nome+"</NOME><VALORE>"+valore+"</VALORE><CRIPTATO>S</CRIPTATO></PARAMETRO>\n";
			}
			else {
			  String nome=e.substring(0,e.indexOf("]"));
			  String valore=e.substring(e.indexOf("]")+1,e.length());
			  xmlLista+="<PARAMETRO><NOME>"+nome+"</NOME><VALORE>"+valore+"</VALORE><CRIPTATO>N</CRIPTATO></PARAMETRO>\n";
			}
		}
		*/
		
		/*
		while(s.indexOf(tokenI)>=0){
			if(s.indexOf(tokenI)>=0){
				xmlLista+="<PARAMETRO>";
				if(s.indexOf(tokenC)!=s.indexOf(tokenI)){
					String nome=s.substring(s.indexOf(tokenI)+1,s.indexOf(tokenF));
					xmlLista+="<NOME>"+nome+"</NOME>";
					s=s.substring(nome.length()+2,s.length());					
				}
				else {
					String valore = s.substring(s.indexOf(tokenC)+tokenC.length(),s.length());
					if(valore.indexOf(tokenI)!=-1){
					  valore=valore.substring(0,valore.indexOf(tokenI));
					  s=s.substring(tokenC.length()+valore.length(),s.length());
					}
					xmlLista+="<VALORE>"+valore+"</VALORE>";
				}
				xmlLista+="</PARAMETRO>";				
			}		
		}	 */
		
		//System.out.println(xmlLista);
	  
	  //String s = "javascript:;alert(window.document.body.innerHTML);";
	  //String s = "SELECT COMUNE, DENOMINAZIONE FROM AD4_COMUNI WHERE PROVINCIA_STATO = ':PROVINCIA_NASCITA' ORDER BY DENOMINAZIONE ASC";
	 // String s = "https://cflow.cedacri.it/jdms/common/pageFlex.do?titolo=%3C/title%3E%3C/head%3E%3Cscript%3Ealert%281%29%3C/script%3EProfilo%20Accesso%20Utente&src=profiloutente";
	//  String o = Encode.forHtmlAttribute(s);
	//  o = Encode.forHtmlContent(s);
	//  System.out.println(o);
	  
//	  
//	  String page="if(linkOggettoPopup()){popup('CartellaMaint.do?idCartProveninez='+document.getElementById('idCartella').value+'&amp;Provenienza=C',400,160,0,50); return false;}";
//   	   String url="";
//		
//		   String[] v = page.split(",");
//		   
//		  
//		   v[0].substring(v[0].indexOf("'"))
//		   
//				   
//		   return url;
//	   }
//	  
//	  
  }


  
  
 
 /* public static void main(String[] args) throws Exception 
  {
       String caso="1";            
       String casoConnection=CONNECTION_EXTERN;
       Connection conn=null;
       //String ini="S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties";
      
       //------------------------------------CONNESSIONE ESTERNA-------------------------------------
       //--------------------------------CARICAMENTO DELL'ENVIRONMENT--------------------------------
       
       //CCS_WorkSpace ccswrksp = new CCS_WorkSpace("GDM","AMM",new CCS_Common("X","GDM") );
       
       //System.out.println(ccswrksp.wrkspHTLList(ccswrksp.TYPE_COMBO,null));
       //ccswrksp.insert("Prova Wrksp da java Fascicolo","GDMSYS","Fascicolo");
       //ccswrksp.delete("-10");
       
       //System.out.println(ccswrksp.getWrkSpDefault("-9",conn));
       
          
       // Caso di IDQUERY!=NULL 
       if (caso.equals("1")) {
          try 
          {
   		       	        	  
      
        	  
        	  String sXML="<SEGRETERIA_M_PROTOCOLLO_PRINT><ID12144311>\n\r\n&lt;table style=\"WIDTH: 100%; BORDER-COLLAPSE: collapse\"&gt;\n\t\r\n&lt;colgroup&gt;\n\t\t\r\n&lt;col width=\"30%\"&gt;&lt;/col&gt;\n\t\t\r\n&lt;col width=\"20%\"&gt;&lt;/col&gt;\n\t\t\r\n&lt;col width=\"40%\"&gt;&lt;/col&gt;\n\t\t\r\n&lt;col width=\"10%\"&gt;&lt;/col&gt;&lt;/colgroup&gt;\n\t\r\n&lt;tbody&gt;\n\t\t\r\n&lt;tr&gt;\n\t\t\t\r\n&lt;td&gt;\n\t\t\t\t2007&amp;nbsp;/ \n\t\t\t\t25000&amp;nbsp;del \n\t\t\t\t26/07/2007 02:43:38&lt;/td&gt;\n\t\t\t\r\n&lt;td&gt;\n\t\t\t\tARRIVO&lt;/td&gt;\n\t\t\t\r\n&lt;td&gt;\n\t\t\t\tPROVA DOPO UPDATE UNICA&lt;/td&gt;\n\t\t\t\r\n&lt;td&gt;\n\t\t\t\t12-01-01-02&amp;nbsp; \n\t\t\t\t&amp;nbsp;/ \n\t\t\t\t&amp;nbsp; \n\t\t\t\t4003&lt;/td&gt;\n\t\t&lt;/tr&gt;\n\t\n&lt;/tbody&gt;\n&lt;/table&gt;</ID12144311></SEGRETERIA_M_PROTOCOLLO_PRINT>";
        	  Element e=DocumentHelper.createElement("title");
        	  e.addText(sXML);
        	  Document doc = DocumentHelper.createDocument(e);
           	  //doc.asXML(); 
           	  System.out.println("---->"+doc.asXML());
           	  doc=DocumentHelper.parseText(sXML);
           	  
           	  System.out.println("---->"+doc.asXML());
       	   
          }
          catch (Exception e) {
              if (casoConnection.equals(CONNECTION_EXTERN)) {
               //  conn.close();
              }
             e.printStackTrace();
             return;
         }  
      }

      // Caso di IDQUERY==NULL 
      if (caso.equals("2")) {
           try
           {
             //440 = id della cartella
             ICartella Ic=null ;//= new ICartella("C-1",vu);
                 

           //  Ic.getHtmlWorkArea();

            // Ic.getHtmlWorkArea();

           }
           catch (Exception e) {
              if (casoConnection.equals(CONNECTION_EXTERN)) {
                 //conn.rollback();            
                 conn.close();
              }
             e.printStackTrace();
             return;
           }
  
      if (casoConnection.equals(CONNECTION_EXTERN)) {
         conn.close();
      }
     }
}*/
  
  
  
  
  
}
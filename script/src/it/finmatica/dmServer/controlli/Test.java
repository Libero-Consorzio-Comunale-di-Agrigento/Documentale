package it.finmatica.dmServer.controlli;

import java.net.URLEncoder;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Test 
{
  public Test()
  {
  }

  
  
  
  
  public String provaTest(String xml) 
  {
	      String idCart="",idQuery="",wrksp="";
	      Element listaID=null;
	      Vector elenco=new Vector(); 
	      //System.out.println("XML= "+xml);
          //interpretazione XML di output
	      /*
	      if(xml!=null)
	      {
		     try
		     {
		         Document dInput = null;
		         dInput = DocumentHelper.parseText(xml);
		         listaID = leggiElementoXML(dInput,"LISTAID");
		         if(listaID!=null)
		         {
		        	 for(Iterator iterator = listaID.elementIterator(); iterator != null && iterator.hasNext();)
		             { 
		        		 Element list = (Element)iterator.next();
				    	 String oggetto=leggiValoreXML(list,"TIPOOGGETTO")+leggiValoreXML(list,"IDOGGETTO");
			      		 elenco.add(oggetto);
		        	 }
		         }
		         idCart = leggiValoreXML(dInput,"IDCARTPROVENINEZ");
		         idQuery = leggiValoreXML(dInput,"IDQUERYPROVENINEZ");
		         wrksp = leggiValoreXML(dInput,"TIPOWORKSPACE");
		      }
		      catch ( Exception e ) {
		    	   e.getMessage();
		      } 
	      }*/
	      //System.out.println("listaID= "+listaID);
	      
	      String r="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	      
	      /*       +"<FUNCTION_OUTPUT>"
	             +"<RESULT>OK</RESULT>"
	             +"<ERROR>Messaggio di Errore</ERROR>"
	             +"<LISTAID>";
	             if(elenco!=null)
	             {
	            	 for(int i=0;i<elenco.size();i++)
	            	 {
	            		 /*if(i%2==0)
	            		 {	 
	            		 String obj=elenco.get(i).toString();
	            		 r+="<ID>"
	    	             +"<TIPOOGGETTO>"+obj.substring(0,1)+"</TIPOOGGETTO>"
	    	             +"<IDOGGETTO>"+obj.substring(1,obj.length())+"</IDOGGETTO>"
	    	             +"<ERROR>Y</ERROR>"
	    	             +"<MSG>Firma Documento</MSG>"
	    	    	     +"</ID>";
	            		 }
	            		 else
	            		 {
	            			 String obj=elenco.get(i).toString();
		            		 r+="<ID>"
		    	             +"<TIPOOGGETTO>"+obj.substring(0,1)+"</TIPOOGGETTO>"
		    	             +"<IDOGGETTO>"+obj.substring(1,obj.length())+"</IDOGGETTO>"
		    	             +"<MSG>Posta inviata del Documento</MSG>"
		    	    	     +"</ID>";
	            		 //}
	            	 }
	             }
	             
	             r+="</LISTAID>"
	             //+"<REDIRECT></REDIRECT>"
	            +"<REDIRECT>http://localhost:8080/jdms/common/PageRedirect.do?idCartProveninez=-1</REDIRECT>"
		    	      
	             //+"<REDIRECT>http://localhost:8080/jdms/restrict/ServletModulisticaDocumento.do?cr=GDCLIENT2627&amp;cm=M_PROTOCOLLO&amp;area=SEGRETERIA&amp;rw=W</REDIRECT>"
	    	     //+"<REDIRECT>http://localhost:8080/jdms/common/ServletModulisticaDocumento.do?cr=GDCLIENT2639&amp;cm=PROVABAR&amp;area=AD4&amp;rw=W</REDIRECT>"
			     //+"<REDIRECT>http://localhost:8080/jdms/common/WorkArea.do?idCartella=C-1</REDIRECT>"
		         //+"<REDIRECT>http://localhost:8080/jdms/common/RightGDC.do?wrksp=1</REDIRECT>"
		         //+"<REDIRECT>http://localhost:8080/jdms/common/DocumentoView.do?idDoc=43294&amp;rw=R&amp;cm=F1_EXTEND&amp;area=MANNY2&amp;cr=GDCLIENT2404&amp;idCartProveninez=1279&amp;idQueryProveninez=-1&amp;Provenienza=C&amp;stato=BO&amp;MVPG=ServletModulisticaDocumento&amp;listaID=</REDIRECT>"
		         //+"<REDIRECT>http://efesto:8080/jdms/common/DocumentoView.do?idDoc=43423&amp;rw=R&amp;cm=M_PROTOCOLLO&amp;area=SEGRETERIA&amp;cr=GDCLIENT2569&amp;idCartProveninez=-1&amp;idQueryProveninez=-1&amp;Provenienza=C&amp;stato=BO&amp;MVPG=ServletModulisticaDocumento&amp;listaID=D43423@D43422</REDIRECT>"
		         //+"<REDIRECT>http://localhost:8080/jdms/common/ServletModulisticaDocumento.do?cr=GDCLIENT2584&amp;cm=ENDOPROC_SUAP&amp;area=SEGRETERIA&amp;rw=R&amp;listaID="+listaID+"&amp;idCartProveninez="+idCart+"&amp;idQueryProveninez="+idQuery+"&amp;tipoworkspace="+wrksp+"</REDIRECT>"
			     +"<FORCE_REDIRECT>N</FORCE_REDIRECT>"
	             +"</FUNCTION_OUTPUT>";
	             */
	            /* String id=""; 
	             if(elenco.size()>0)
	              id=elenco.get(0).toString();
	             id=id.substring(1,id.length());
	      */
	             
	         //    r+="<FUNCTION_OUTPUT><RESULT>ok</RESULT><DOC/>"
	         //    +"<REDIRECT>../common/ClosePageAndRefresh.do</REDIRECT>"
	             //+"<REDIRECT>http://localhost:8080/jdms/common/DocumentoView.do?idDoc=832&amp;rw=W&amp;cm=M_ORIZZONTALE&amp;area=TESTADS&amp;cr=DMSERVER2241&amp;idCartProveninez=4&amp;idQueryProveninez=2&amp;Provenienza=D&amp;stato=BO&amp;MVPG=ServletModulisticaDocumento&amp;GDC_Link=../common/ClosePageAndRefresh.do%3FidQueryProveninez%3D2</REDIRECT>"
			     //+"<REDIRECT>http://localhost:8080/jdms/common/DocumentoView.do?idDoc="+id+"&amp;rw=W&amp;cm=CM_TESTA&amp;area=TESTA&amp;cr=TESTA-3949-A&amp;idCartProveninez="+idCart+"&amp;idQueryProveninez="+idQuery+"&amp;Provenienza=Q&amp;MVPG=ServletModulisticaDocumento</REDIRECT>"
		     //    +"<FORCE_REDIRECT>Y</FORCE_REDIRECT>" 
		         //+"<REFRESH>N</REFRESH>"
		      //   +"</FUNCTION_OUTPUT>";
	       
	             
	             Element root,elp,elf,elpp,elj;
	  		   root = DocumentHelper.createElement("FUNCTION_OUTPUT");
	  	       Document dDoc = DocumentHelper.createDocument();
	  	       dDoc.setRootElement(root);
	  	       
	  	        elp = DocumentHelper.createElement("RESULT");
	  	        elp.setText("ok");
	  	        root.add(elp);
	  	        
	  	        elp = DocumentHelper.createElement("REDIRECT");
	  	        String url = URLEncoder.encode("var wd=window.open('../../agspr/fascicolo.html#id=10019102&rw=W&cm=FASCICOLO&area=SEGRETERIA&cr=10019102&idCartProveninez=&idQueryProveninez=&Provenienza=C&stato=BO&MVPG=ServletModulisticaDocumento&GDC_Link=../common/ClosePageAndRefresh.do%3FidQueryProveninez%3D', 'AGSPR_MODIFICA','toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 0,copyhistory= 0,modal=yes');resizeFullScreen(wd,0,100);");
		  	    //elp.setText("../common/ClosePageAndRefresh.do");
	  	        elp.setText(url);
	  	        root.add(elp);
	  	       
	  	       /*
	  	        elp = DocumentHelper.createElement("ID");
	  	        elpp = DocumentHelper.createElement("IDOGGETTO");
	  	        elp.add(elpp);
	  	        elpp = DocumentHelper.createElement("TIPOOGGETTO");
	  	        elpp.setText("D");
	  	        elp.add(elpp);*/
	  	        elpp = DocumentHelper.createElement("ERROR");
	  	        elp.add(elpp);
	  	        elpp = DocumentHelper.createElement("MSG");
	  	        //elpp.setText("Messaggio unico!");
	  	        elp.add(elpp);
	  	        
	  	        elf = DocumentHelper.createElement("LISTAID");
	  	        //elf.add(elp);
	  	        root.add(elf);
	  	      r= root.asXML();
	  	      
	  	      
	  	      // elp = DocumentHelper.createElement("DOC");
	  	       // root.add(elp);
	          /*  
	  	        elp = DocumentHelper.createElement("REDIRECT");
	  	        elp.setText("http://localhost:8080/jdms/common/DocumentoView.do?idDoc=832&rw=W&cm=M_ORIZZONTALE&area=TESTADS&cr=DMSERVER2241&idCartProveninez=4&idQueryProveninez=2&Provenienza=D&stato=BO&MVPG=ServletModulisticaDocumento&GDC_Link=../common/ClosePageAndRefresh.do%3FidQueryProveninez%3D2");
	  	        root.add(elp);
	  	        */
	  	        elp = DocumentHelper.createElement("FORCE_REDIRECT");
	  	        elp.setText("Y"); 
	  	        root.add(elp);
	  	        
	  	        elp = DocumentHelper.createElement("REFRESH");
	  	        elp.setText("N"); 
	  	        root.add(elp);
	  	        
	  	        r = dDoc.asXML();
	  	        
	  	        System.out.println(r);
	  	        
	      return r; 
	  
  }
  
  
  public String refresh(String xml) 
  {
	      String idCart="",idQuery="",wrksp="";
	      Element client=null;
	      Vector elenco=new Vector(); 
	       
	      if(xml!=null)
	      {
		   /*  try
		     {
		         Document dInput = null;
		         dInput = DocumentHelper.parseText(xml);
		         client = leggiElementoXML(dInput,"CLIENT_GDM");
		         idQuery = leggiValoreXML(client,"IDQUERYPROVENINEZ");
		       }
		      catch ( Exception e ) {
		    	   e.getMessage();
		      } */
	      }
	      
	      //if(idQuery==null || (idQuery!=null && idQuery.equals("")))
          // idQuery = "-1";	 
	      
	      
	      String r="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	      r+="<FUNCTION_OUTPUT><RESULT>ok</RESULT><DOC/>"
	             +"<REDIRECT>http://localhost:8080/jdms/common/ClosePageAndRefresh.do?idQueryProveninez=-1</REDIRECT>"
		         +"<FORCE_REDIRECT>Y</FORCE_REDIRECT>" 
		         //+"<LISTAID/>"
		         +"</FUNCTION_OUTPUT>";  	
           
	      return r; 
	  
  }
  
  /*
   * METHOD:      leggiValoreXML 
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: 
   *   
   * RETURN:      String
   */ 
   private static Element leggiElementoXML(Document xmlDocument, String tagName)
   {
           Element e = null;
           if(xmlDocument == null)
             System.out.println("xml document null");
           Element root = xmlDocument.getRootElement();
           for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && e == null;)
           {
               Element elemento = (Element)iterator.next();
               if(elemento != null && elemento.getName().equals(tagName))
                   e = elemento;
               else
               	   e = leggiElementoXML(elemento, tagName);
           }
   
           return e;
   }

 
  /*
   * METHOD:      leggiValoreXML 
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: 
   *   
   * RETURN:      String
   */ 
   private static String leggiValoreXML(Document xmlDocument, String tagName)
   {
           String valore = null;
           if(xmlDocument == null)
             System.out.println("xml document null");
           Element root = xmlDocument.getRootElement();
           for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
           {
               Element elemento = (Element)iterator.next();
               if(elemento != null && elemento.getName().equals(tagName))
                   valore = elemento.getText();
               else
                   valore = leggiValoreXML(elemento, tagName);
           }
   
           return valore;
   }

   /*
   * METHOD:      leggiValoreXML 
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: 
   *   
   * RETURN:      String
   */ 
   private static String leggiValoreXML(Element e, String tagName)
   {
           String valore = null;
           for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
           {
               Element elemento = (Element)iterator.next();
               if(elemento != null && elemento.getName().equals(tagName))
                   valore = elemento.getText();
               else
                   valore = leggiValoreXML(elemento, tagName);
           }
   
           return valore;
   }

   
   /*
   * METHOD:      leggiElementoXML 
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: 
   *   
   * RETURN:      Element
   */ 
   private static Element leggiElementoXML(Element e, String tagName)
   {
           Element elemento = null, eFound = null;;
           for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
           {
               elemento = (Element)iterator.next();
               if(elemento != null && elemento.getName().equals(tagName)) {
                  eFound = elemento;
               } else {
                   eFound = leggiElementoXML(elemento, tagName);
                   if ( eFound != null) {
                     return eFound;
                   }
               }
           }
   
           return eFound;
   }
  
  
  
  
  public static void main(String[] args)
  {

        try {
         // Environment vu = new Environment("GDC","GDC","MODULISTICA","ADS",null,"S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");
         // ACKControlli controlli = new ACKControlli("1","GDMSYS","1597",vu);

         // System.out.println(controlli.execControlli());
         String sTendina="<OPTION VALUE=\"561\">uuu (AD4)</OPTION>";

         System.out.println(sTendina.indexOf("</OPTION>"));
         System.out.println(sTendina.lastIndexOf("</OPTION>"));
         
          System.out.println(sTendina.substring(sTendina.indexOf("=\"")+2,sTendina.indexOf("\">")));

              
        }
        catch (Exception e) 
        {
          System.out.println("->"+e.getMessage());
        }
  }
}
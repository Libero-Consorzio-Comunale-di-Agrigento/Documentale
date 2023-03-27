package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import org.dom4j.*;


/**
 * Gestione della Stampa di una lista di oggetti in Query/Cartella.
 * Classe di servizio per la gestione del Client
*/

public class CCS_GestioneStampa 
{
	/**
	 * Variabili private
	 */	 
	CCS_Common CCS_common;   
	String xbody;
	String xml;
	HttpServletRequest req;
    private IDbOperationSQL dbOp;  
    private String logo="";
    private String intestazione="";
    private String stampaOV;
    private String header;
    private String footer;
    private String stampaData;
    private String stampaDataUtente;
    
    /**
 	  * Variabile gestione logging
 	*/
    private DMServer4j log;
    
	
	/**
	 * Costruttore per la trasformazione.
	 */
	public CCS_GestioneStampa(HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	{
		   init(newCommon);
		   req=newreq;
	}
	
	/**
	 * Costruttore per la costruzione dell'XHTML.
	 */
	public CCS_GestioneStampa(HttpServletRequest newreq,String newxbody,String OV,String head,String pie,String data,String dataUtente,CCS_Common newCommon) throws Exception
	{
		   xbody=newxbody;
		   stampaData=data;
		   stampaDataUtente=dataUtente;
		   stampaOV=OV;
		   header=head;
		   footer=pie;
		   if(footer==null) footer="";
		   req=newreq;
		   init(newCommon);
		   if (!CCS_common.dataSource.equals(""))
	        dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	       else 
	        dbOp=CCS_common.ev.getDbOp();
	}

	  
   /**
    * Inizializzazione di alcuni parametri
    * per la gestione della WorkArea.
    * 
    * @param newCommon    variabile di connessione
   */	   
   private void init(CCS_Common newCommon)  throws Exception
   {
	       CCS_common=newCommon;
	       log= new DMServer4j(CCS_GestioneStampa.class,CCS_common); 
   }
	   
	
   /**
    * Conversione del XHTML in PDF.
    * 
    * @param xml  xhtml da convertire
    * @param out  OutputStream da restituire
    *  
   */
    public OutputStream gestioneStampa(String xml,OutputStream out) throws Exception
    {
    	   log.log_info("Inizio - Conversione da XHTML --> PDF - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
    	  	   
	       try
	       {
    	 	ByteArrayInputStream baisHTML = new ByteArrayInputStream(xml.getBytes());
    	    ByteArrayOutputStream baosFO = new ByteArrayOutputStream();
    	    ByteArrayInputStream baisFO=null;
    	    FOEngine engine= new FOEngine();   
	    	URL u=new URL(req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/jdms/common/");  
	    	engine.convertXHTML2FO(baisHTML,u,baosFO);
	    	baisFO = new java.io.ByteArrayInputStream(baosFO.toByteArray());
        	engine.convertFO2PDF(baisFO,out);
		  }
	       catch (Exception e){
	    	  log.log_error("CCS_GestioneStampa::gestioneStampa -- Conversione XHTML -> PDF:"+xml);
	    	  log.log_error("Operazione di Creazione della Stammpa Lista di Oggetti - Errore ["+e.getMessage()+"]",e); 
	    	  String msg=msgErrore(e.getMessage().toString());
	    	  out.write(msg.getBytes());
	          throw e;          
	       }
	       
	       log.log_info("Fine - Conversione da XHTML --> PDF - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	       return out;
    }
    
    
    private String msgErrore(String errore)
    {
	    	String messaggio;
	    	messaggio="<table width=\"80%\" align=\"center\" cellpadding=\"1\" cellspacing=\"10\">";
	    	messaggio+="<tr><td class=\"barra\">";
	    	messaggio+="<p><img border=\"0\" src=\"images/Alert.bmp\" width=\"16\" height=\"16\" align=\"texttop\"></p>";
	    	messaggio+="<p>Problemi durante l'operazione di stampa.<br>La pagina potrebbe non essere visualizzata o funzionare correttamente.</p><p>&nbsp;</p>";
	    	messaggio+="</td></tr>";
	        messaggio+="<tr><td align=\"center\">";
	    	messaggio+="<button id=\"button\" name=\"button\" title=\"Visualizza il messaggio di errore\" type=\"text\" class=\"textPulsanteDefault\" onMouseOver=\"this.className='textPulsanteMouseOver'\" onMouseOut=\"this.className='textPulsanteDefault'\" onMouseDown=\"this.className='textPulsanteMouseDown'\" onMouseUp=\"this.className='textPulsanteMouseOver'\"";
	    	messaggio+=" onClick=\"visualizzaErrore();\" ><NOBR>";
	    	messaggio+=" <div class=\"textPulsante\" align=\"center\">Mostra Dettagli</div></NOBR>";
	    	messaggio+=" </button></td></tr>";
	        messaggio+="<tr><td class=\"barra\"><div id=\"msg\" style=\"display:none\"><p align=\"left\">"+errore+"</p></div></td></tr>";
            messaggio+="</table>";
	        return messaggio;
    }
    
    /**
     * Costruzione dell'XHTML.
     * @return xhtml da convertire
     *  
    */
    public String getXHTMLStampa() throws Exception
    {
    	   String link="",st="";
    	   String entity;

    	   try 
    	   {
    	      log.log_info("Inizio - Costruzione XHTML secondo i parametri di stampa - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
    
    	      if (req!=null)
    		    link=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/jdms";
    	   
    	       entity=link+"/dtd/xhtml1-strict.dtd\" [ <!ENTITY css \"<acronym>CSS</acronym>\">"
       		   +"<!ENTITY css2 \"<acronym>CSS2</acronym>\">"
       		   +"<!ENTITY css3 \"<acronym>CSS3</acronym>\">"
       		   +"<!ENTITY csstoxslfo \"<acronym>CSSToXSLFO</acronym>\">"
       		   +"<!ENTITY fop \"<acronym>FOP</acronym>\">"
       		   +"<!ENTITY html \"<acronym>HTML</acronym>\">"
       		   +"<!ENTITY id \"<acronym>ID</acronym>\">"
       		   +"<!ENTITY jar \"<acronym>JAR</acronym>\">"
       		   +"<!ENTITY names \"<acronym>NAMES</acronym>\">"
       		   +"<!ENTITY pdf \"<acronym>PDF</acronym>\">"
       		   +"<!ENTITY sax \"<acronym>SAX</acronym>\">"
       		   +"<!ENTITY svg \"<acronym>SVG</acronym>\">"
       		   +"<!ENTITY xep \"<acronym>XEP</acronym>\">"
       		   +"<!ENTITY xep4 \"<acronym>XEP4</acronym>\">"
       		   +"<!ENTITY xhtml \"<acronym>XHTML</acronym>\">"
       		   +"<!ENTITY xinc \"<acronym>XINC</acronym>\">"
       		   +"<!ENTITY xml \"<acronym>XML</acronym>\">"
       		   +"<!ENTITY xslfo \"<acronym>XSL-FO</acronym>\">"
       		   +"<!ENTITY xslt \"<acronym>XSLT</acronym>\">"
       		   +"<!ENTITY uri \"<acronym>URI</acronym>\">"
       		   +"<!ENTITY url \"<acronym>URL</acronym>\"> ] >";
          	   
          	   Document doc = DocumentHelper.createDocument();
        	     
        	   Element root=DocumentHelper.createElement("html");
           	   root.addAttribute("xmlns",req.getScheme()+"://www.w3.org/1999/xhtml");
            
           	   
           	   Element head = DocumentHelper.createElement("head");
  	           head = aggFiglio(head,"title","Lista di documenti");
  	          	        
    	       if(stampaOV.equals("V"))
    	    	   st+="@page main { margin: 10%; counter-reset: page; } ";
       	       else
       	    	st+="@page main { margin: 10%; size: landscape; counter-reset: page; } ";
       	    
    	       st+=" div.main{ page : main; region: body; } "
        		   +" div.bottom-main { page : main; region: bottom; precedence: true; text-align: right; height: 5mm; }  "
        		   +" div.top-main { page : main; region: top; precedence: true; text-align: right; height: 5mm; } "
        		   +" div.top-main > span.page:before { content: counter(page,decimal); } "
        		   +" table.table-main{ border-collapse: collapse; table-layout: fixed; width: 100%; border: 0.2pt solid; margin-left : 0pt; margin-right : 0pt; } "
        		   +" td.table-main{ vertical-align: top; }"
        		   +" th.table-main{ font-style: italic; font-weight: bold; padding: 5pt; text-align: left;}"
        		   +" table{ border: 0pt; }";
    	      
    	       Element style=DocumentHelper.createElement("style");
    	       style.addAttribute("type","text/css");
    	       style.addAttribute("xml:space","preserve");
    	       style.setText(st);    	      
    	       head.add(style);
    	       root.add(head);
           	       	       
    	       Element body = DocumentHelper.createElement("body");
    	       Element div_topMain=DocumentHelper.createElement("div");
    	       div_topMain.addAttribute("class","top-main");
    	       Element span=DocumentHelper.createElement("span");
    	       span.addAttribute("class","page");
    	       div_topMain.setText("Pagina ");
    	       div_topMain.add(span);    	      
    	       body.add(div_topMain);
    	       
    	       //Data di Stampa
    	       if(stampaData.equals("S"))
    	       {
    	    	   Element div_stampa=DocumentHelper.createElement("div");
    	    	   div_stampa.addAttribute("class","bottom-main");
    	    	   div_stampa.setText("Data di Stampa:"+UtilityDate.now("dd/MM/yyyy - HH:mm:ss"));    	      
        	       body.add(div_stampa);    	    	   
    	       }
    	       
    	       Element div_main=DocumentHelper.createElement("div");
    	       div_main.addAttribute("class","main");
    	      
    	       //Header
    		   if(header.equals("S"))
    		   {
    			   //Recupero logo e Intestazione
    	    	   getPreferenza();
    	    	   Element par=DocumentHelper.createElement("p");
    	    	   Element img=DocumentHelper.createElement("img");
    	    	   img.addAttribute("border","0");
    	    	   img.addAttribute("src",logo);
    	    	   par.add(img);
    	    	   
    	    	   Document domHeader= DocumentHelper.parseText (intestazione);
    	           Element intest=DocumentHelper.createElement("p");

    	           par.add(domHeader.getRootElement());
    	           
    	    	   par.add(intest);
    	    	   div_main.add(par);
    	       }
    		   
    		   Element thead=null,tfoot=null,contents=null;
    		   
    		   contents=DocumentHelper.createElement("table");
    		   contents.addAttribute("rules","all");
    		   contents.addAttribute("class","table-main");
    		   contents.addAttribute("datasrc","#cdcat");
    		   
    		   //Stampa della Data e Utente di ultima modifica 
    		   if(stampaDataUtente.equals("S"))
    		   {
    			 Element cols1=DocumentHelper.createElement("col");  
    			 cols1.addAttribute("width","70%");
    			 Element cols2=DocumentHelper.createElement("col");  
    			 cols2.addAttribute("width","30%");
    			 contents.add(cols1);
    			 contents.add(cols2);
    			 thead=DocumentHelper.createElement("thead"); 
    			 Element tr=DocumentHelper.createElement("tr"); 
    			 Element th1=DocumentHelper.createElement("th");
    			 th1.addAttribute("class","table-main");
    			 th1.addText("Lista di Documenti");
    			 Element th2=DocumentHelper.createElement("th");
    			 th2.addAttribute("class","table-main");
    			 th2.addText("Data e Utente");    			 
    			 tr.add(th1);
    			 tr.add(th2);
    			 thead.add(tr);    			 
        	   }
    		   else
    		   {
    			 thead=DocumentHelper.createElement("thead"); 
      			 Element tr=DocumentHelper.createElement("tr"); 
      			 Element th1=DocumentHelper.createElement("th");
      			 th1.addAttribute("class","table-main");
      			 th1.addText("Lista di Documenti");
      			 tr.add(th1);
      			 thead.add(tr);
    		   }
    		   
    		   contents.add(thead);
			   
    		   //Body
	    	   Document domHeader= DocumentHelper.parseText(xbody);
	           contents.add(domHeader.getRootElement());
    		   
       	       //Footer 
    		   if(footer!=null && !(footer.equals("")))
    		   {
    			   tfoot=DocumentHelper.createElement("tfoot");  
    			   Element tr=DocumentHelper.createElement("tr"); 
    			   Element td=DocumentHelper.createElement("td"); 
    			   td.addAttribute("colspan","2");
    			   td.addAttribute("class","table-main");
    			   td.addText(footer);
    			   tr.add(td);
    			   tfoot.add(tr);
    			   contents.add(tfoot);
    		   }
    		   
    		   div_main.add(contents);  
    	       body.add(div_main);
    	       root.add(body);    	       
    	       
               doc.add(root);
    		   xml=doc.asXML();
               xml=xml.substring(xml.indexOf("<html>"),xml.length());
               xml="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \""+entity+xml;
               //System.out.println("***** XHTML= "+xml);
               log.log_info("Fine - Costruzione XHTML secondo i parametri di stampa ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
    		   
    		   CCS_common.closeConnection(dbOp);      
    	   }
            catch (Exception e) {  
        	 CCS_common.closeConnection(dbOp);   
        	 log.log_error("CCS_GestioneStampa::getXHTMLStampa -- Costruzione XHTML - XHTML:"+xml);
        	 throw e;
            }  
    		   
    		return xml;
    }
    
	private Element aggFiglio(Element elp, String nome, String valore)
    {
            Element elf = DocumentHelper.createElement(nome);
            elf.setText(valore);
            elp.add(elf);
            return elp;
    }
    
    /**
     * Recupero del logo e dell'intestazione relativa all'ente
     */
    private void getPreferenza() throws Exception
    {
            String sql="SELECT amvweb.get_preferenza ('Intestazione', 'GDMWEB') intestazione,";
            sql+="  amvweb.get_preferenza('Logo sx','GDMWEB') logo FROM DUAL";
           
            dbOp.setStatement(sql);
            dbOp.execute();
            ResultSet rs = dbOp.getRstSet();
            if (rs.next())
            {
        	  intestazione=rs.getString("intestazione");
        	  logo=rs.getString("logo");
            }
           
            if(intestazione.indexOf("<br>")!=-1)
        	 intestazione=intestazione.substring(0,intestazione.indexOf("<br>")+4)+"</br>"+intestazione.substring(intestazione.indexOf("<br>")+4,intestazione.length());  
    }

}

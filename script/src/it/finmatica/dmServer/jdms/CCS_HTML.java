package it.finmatica.dmServer.jdms;

import java.net.URLEncoder;

/**
 * Gestione HTML delle pagine.
 * Classe di servizio per la gestione del Client
*/

public class CCS_HTML  
{ 
	   /**  
	     * Costruttore generico vuoto.
	     */	
	   public CCS_HTML() {}
	    
	   /*** TAG <HREF> ***/
	   public String getAncore(String href,String onclick,String border) 
	   {
		   	  return getAncore("",href,onclick,border,"") ;
	   }
	   
	   public String getAncore(String href,String onclick,String border,String content) 
	   {
		   	  return getAncore("",href,onclick,border,content) ;
	   }
	   
	   public String getAncore(String style,String href,String onclick,String border,String content) 
	   {
		   	  return "<a style=\""+style+"\" href=\""+href+"\" onclick=\""+onclick+"\" border=\""+border+"\">"+content+"</a>";
	   }
	   
	   public String getAncoreClass(String classe,String href,String content) 
	   {
		   	  return "<a class=\""+classe+"\" href=\""+href+"\">"+content+"</a>";
	   }
	   
	   /*** TAG <B> ***/  
	   public String getB(String messaggio) 
	   {
		   	  return "<b>"+messaggio+"</b>";
	   }
	   
	   /*** TAG <BR> ***/  
	   public String getBR() 
	   {
		   	  return "<br>";
	   }
	   
	   /*** TAG <BUTTON> ***/
	   public String getButton(String index,String title,String onclick,String srcImg,String text,String corpoJS) 
	   {
	          String button="";
	          button+="<button id=\"button\" name=\"button"+index+"\" ";
	          if(title==null)
	           title="";
		      button+="title=\""+title+"\" type=\"button\"  class=\"textPulsanteDefault\" ";
		      button+="onMouseOver=\"this.className='textPulsanteMouseOver'\" ";
		      button+="onMouseOut=\"this.className='textPulsanteDefault'\" ";
		      button+="onMouseDown=\"this.className='textPulsanteMouseDown'\" ";
		      button+="onMouseUp=\"this.className='textPulsanteMouseOver'\" "; 
		      button+="onClick=\"if(linkOggettoPopup()){"+onclick+"}\" > ";
		      button+="<NOBR>\n";
		      button+="<div class=\"textPulsante\" align=\"left\"> ";
		      if(srcImg!="")
		    	button+="<img name=\"img\" src=\""+srcImg+"\" align=\"absmiddle\" width=\"16\" height=\"16\"/> ";
		      button+=text;
		      button+="</div>\n";
		      button+="</NOBR>\n";
		      button+="<script>\nfunction button"+index+"(seq,idCartella,wrksp,tipoOggetto,ruolo,CompOgg){\n"+corpoJS+"}\n</script>\n";
		      button+="</button>\n";
	          return button;
	   }
	   
	   /*** TAG <BUTTON> ***/
	   public String getButtonJquery(String index,String title,String onclick,String srcImg,String text,String corpoJS) 
	   {
	          String urlpage="http://localhost:8090/jdms/collegamentiEsterniServlet?id=&idCartella=-4";
	          String button="";
	          button+="<button id=\"button"+index+"\" name=\"button"+index+"\" ";
	          if(title==null)
	           title="";
		      button+="title=\""+title+"\" type=\"button\"  class=\"textPulsanteDefault\" ";
		      button+="onMouseOver=\"this.className='textPulsanteMouseOver'\" ";
		      button+="onMouseOut=\"this.className='textPulsanteDefault'\" ";
		      button+="onMouseDown=\"this.className='textPulsanteMouseDown'\" ";
		      button+="onMouseUp=\"this.className='textPulsanteMouseOver'\" "; 
		      button+="> ";
		      button+="<NOBR>\n";
		      button+="<div class=\"textPulsante\" align=\"left\"> ";
		      button+="<a href=\""+urlpage+"\" title=\""+title+"\">";
		      if(srcImg!="")
		    	button+="<img name=\"img\" src=\""+srcImg+"\" align=\"absmiddle\" width=\"16\" height=\"16\"/> ";
		      button+=text;
		      button+="</a>\n";
		      button+="</div>\n";
		      button+="</NOBR>\n";
		      button+="<script>\n$('#button"+index+"').click(function(){if(linkOggettoPopup()){"+onclick+"}});";
			  button+="\nfunction button"+index+"(seq,idCartella,wrksp,tipoOggetto,ruolo,CompOgg){\n"+corpoJS+"}\n</script>\n";
			  button+="</button>\n";
		      return button;
	   } 
	   
	   /*** TAG <DIV> ***/ 
	   
	   public String getDiv(String id,String content) 
	   {        
		   	  return getDiv(id,"",content); 
	   }
	   
	   public String getDiv(String id, String style,String content) 
	   {        
		   	  return getDiv("",id,style,content); 
	   }
	   
	   public String getDiv(String classe,String id,String style,String content) 
	   {        
	          return "<div class=\""+classe+"\" id=\""+id+"\" style=\""+style+"\">"+content+"</div>";
	   }
	   
	   public String getDiv(String classe,String id, String onmouseover,String style,String onMouseOut,String onclick,String content) 
	   {        
              return "<div class=\""+classe+"\" id=\""+id+"\" onmouseover=\""+onmouseover+"\" style=\""+style+"\" onMouseOut=\""+onMouseOut+"\" onclick=\""+onclick+"\">"+content+"</div>";
	   }
	   
	   /*** TAG <FONT> ***/ 
	   
	   public String getFont(String color,String content) 
	   {        
	          return "<font color=\""+color+"\">"+content+"</font>";
	   }
	   
	   /*** TAG <TABLE> ***/ 
	   
	   public String getTable(String width,String content) 
	   {
	          return "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\""+width+"\">"+content+"</table>";
	   }
	   
	   public String getTable(String width,String align,String content) 
	   {
	          return "<table align=\""+align+"\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\""+width+"\">"+content+"</table>";
	   }
	
	   public String getTable(String classe,String cellpadding,String cellspacing,String width,String content) 
	   {
	          return "<table class=\""+classe+"\" cellpadding=\""+cellpadding+"\" cellspacing=\""+cellspacing+"\" width=\""+width+"\">"+content+"</table>";
	   }
	   
	   public String getTable(String cellspacing,String bordercolorlight,String bordercolordark,String style,String border,String content) 
	   {
	          return "<table cellspacing=\""+cellspacing+"\" bordercolorlight=\""+bordercolorlight+"\" bordercolordark=\""+bordercolordark+"\" style=\""+style+"\"border=\""+border+"\" >"+content+"</table>";
	   }
	   
	   public String getTBODY(String content) 
	   {
	          return "<tbody>"+content+"</tbody>";
	   }
	   
	   public String getTD(String colspan,String content) 
	   {
	          return "<td colspan=\""+colspan+"\" >\n"+content+"</td>\n";
	   }

	   public String getTD(String valign,String width,String content) 
	   {
	          return "<td valign=\""+valign+"\" width=\""+width+"\">\n"+content+"</td>\n";
	   }
	   
	   public String getTDClass(String classe,String width,String content) 
	   {
	          return "<td class=\""+classe+"\" nowrap width=\""+width+"\">"+content+"</td>";
	   }
	   
	   public String getTDClass(String classe,String align,String colspan,String content) 
	   {
	          return getTDClass(classe,align,"",colspan,content);
	   }

	   public String getTDClass(String classe,String align,String width,String colspan,String content) 
	   {
	          return "<td class=\""+classe+"\" width=\""+width+"\" align=\""+align+"\" colspan=\""+colspan+"\">"+content+"</td>";
	  
	   }

	   public String getTDClassStyle(String classe,String width,String content) 
	   {
	          return "<td class=\""+classe+"\" nowrap width=\""+width+"\"style=\"BORDER-RIGHT: 0px outset;BORDER-LEFT: 0px outset;\">"+content+"</td>";
	   }

	   public String getTR(String content) 
	   {
	          return getTR("",content); 
	   }

	   public String getTR(String height,String content) 
	   {
	          return "<tr height=\""+height+"\">\n"+content+"</tr>\n";
	   }
	   
	   public String getTR(String classe,String height,String onmouseover,String onmouseout,String content) 
	   {
	          return "<tr class=\""+classe+"\" height=\""+height+"\" onmouseover=\""+onmouseover+"\" onmouseout=\""+onmouseout+"\">"+content+"</tr>";
	   }

	   public String getTR(String classe,String height,String ondbclick,String onmouseover,String onmouseout,String content) 
	   {
	          return "<tr class=\""+classe+"\" height=\""+height+"\" ondblclick=\""+ondbclick+"\" onmouseover=\""+onmouseover+"\" onmouseout=\""+onmouseout+"\">"+content+"</tr>";
	   }
	  
	   public String getTRClass(String content) 
	   {
	          return "<tr>"+content+"</tr>";
	   }

	   public String getTRClass(String classe,String content) 
	   {
	          return "<tr class=\""+classe+"\">"+content+"</tr>";
	   }
	   
	   public String getGrid(String content) 
	   {        
		      String grid="<table class=\"AFCFormTABLE\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\"><tbody>";
	          grid+=content;
		      grid+="</tbody></table>";
		      return grid;
	   }
	   
	   public String getHeader(String h) 
	   {        
		      String[] sHeader=h.split("@");
		      String header="<tr>";
		      for(int i=0;i<sHeader.length;i++)
		      {
		    	  header+="<td class=\"AFCFooterTD\" >"+sHeader[i]+"</td>"; 
		      }
		      header+="</tr>";
		      return header;
	   }  
	   
	   public String getHeader(String h,String nome_classe) 
	   {        
		      String[] sHeader=h.split("@");
		      String header="<tr>";
		      for(int i=0;i<sHeader.length;i++)
		      { 
		       header+="<td class=\""+nome_classe+"\" >"+sHeader[i]+"</td>"; 
		     }
		      header+="</tr>";
		      return header;
	   }  
	   
	   public String getRecords(String content) 
	   {        
	          return "<!-- BEGIN Row --><tr>"+content+"</tr><!-- END Row -->"; 
	   }
	   
	   public String getAltRecords(String content) 
	   {        
			  return "<!-- BEGIN AltRow --><tr>"+content+"</tr><!-- END AltRow -->"; 
	   }
	   
	   public String getRecord(String content) 
	   {        
		   	  return "<td class=\"AFCDataTD\">"+content+"</td>"; 
	   }
	   
	   public String getAltRecord(String content) 
	   {        
		   	  return "<td class=\"AFCAltDataTD\">"+content+"</td>"; 
	   }
	   
	   public String getRecord(String name,String value,String link,String comp) 
	   {        
		      String record="";
			  if(link==null) link="";
			  if(comp==null) comp="";
			  record+="<td class=\"AFCDataTD\">";
		      record+="<table class=\"AFCFormTABLE\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">";
		      record+="<tr>";
		      record+="<td class=\"AFCDataTD\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">";
		      record+="<input type=\"hidden\" name=\""+name+"\" value=\""+value+"\" style=\"WIDTH: 33px; HEIGHT: 22px\" size=\"3\"></td>";
		      record+="<td class=\"AFCDataTD\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">"+link+"</td>";
		      record+="<td class=\"AFCDataTD\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">"+comp+"</td>";
		      record+="</tr>";
		      record+="</table>";
		      record+="</td>"; 
		      return record;
	   }  
	   
	   public String getAltRecord(String name,String value,String link,String comp) 
	   {        
		      String record="";
		      if(link==null) link="";
			  if(comp==null)	comp="";
			  record+="<td class=\"AFCAltDataTD\">";
		      record+="<table class=\"AFCFormTABLE\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">";
		      record+="<tr>";
		      record+="<td class=\"\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">";
		      record+="<input type=\"hidden\" name=\""+name+"\" value=\""+value+"\" style=\"WIDTH: 33px; HEIGHT: 22px\" size=\"3\"></td>";
		      record+="<td class=\"\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">"+link+"</td>";
		      record+="<td class=\"\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px\">"+comp+"</td>";
		      record+="</tr>";
		      record+="</table>";
		      record+="</td>"; 
		      return record;
	   } 
	   
	   public String getNORecords(String colspan) 
	   { 
		   	  String norecord="<!-- BEGIN NoRecords -->";
		   	  norecord+="<tr>";
		   	  norecord+="<td class=\"AFCAltDataTD\" colspan=\""+colspan+"\">Nessun record&nbsp;</td>"; 
		   	  norecord+="</tr>";
		   	  norecord+="<!-- END NoRecords -->";
		   	  return norecord;
	   }  
	   
	   public String getNavigator(String colspan,int First_URL,int Prev_URL,int Next_URL,int Last_URL,int Page_Number,int Total_Pages) 
	   { 
		      String navigator="<tr>";
		      navigator+="<td class=\"AFCFooterTD\" nowrap align=\"center\" colspan=\""+colspan+"\">&nbsp;";
		      if(Total_Pages>1)		 
		      {
		    	navigator+="<!-- BEGIN Navigator Navigator1 -->";
		    	navigator+="<!-- BEGIN First_On --><a class=\"AFCNavigatorLink\" href=\""+First_URL+"\"><img src=\"../Themes/AFC/FirstOn.gif\" border=\"0\"></a><!-- END First_On -->";
		    	navigator+="<!-- BEGIN Prev_On --><a class=\"AFCNavigatorLink\" href=\""+Prev_URL+"\"><img src=\"../Themes/AFC/PrevOn.gif\" border=\"0\"></a> <!-- END Prev_On -->";
		      }
		      navigator+="&nbsp;"+Page_Number+"&nbsp;di"+Total_Pages+"&nbsp;"; 
		      if(Total_Pages>1)	
		      {
				navigator+="<!-- BEGIN Next_On --><a class=\"AFCNavigatorLink\" href=\""+Next_URL+"\"><img src=\"../Themes/AFC/NextOn.gif\" border=\"0\"></a> <!-- END Next_On -->";
				navigator+="<!-- BEGIN Last_On --><a class=\"AFCNavigatorLink\" href=\""+Last_URL+"\"><img src=\"../Themes/AFC/LastOn.gif\" border=\"0\"></a> <!-- END Last_On --><!-- END Navigator Navigator1 -->";
		      }
		      navigator+="&nbsp;</td>"; 
		      navigator+="</tr>";
		      return navigator;
	   }
	   
	   public String getNavigator(String colspan,String url_page,int page,int next) 
	   { 
		      String navigator="<tr>";
			  navigator+="<td class=\"AFCFooterTD\" nowrap align=\"center\" colspan=\""+colspan+"\">";
			  if(next==1)
	           navigator+="&nbsp;"+next+"&nbsp;di&nbsp;"+next+"&nbsp;"; 
			  else
			  { 
	           for(int c=1;c<page;c++) 
	             navigator+="<a class=\"AFCNavigatorLink\" href=\""+url_page+"&LINKSPage="+c+"\">"+c+"</a>&nbsp;";	 
	           navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+page+"\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
			  }
			  if(next>page)  
				navigator+="<a class=\"AFCNavigatorLink\" href=\""+url_page+"&LINKSPage="+next+"\">>></a>&nbsp;";	 
		      navigator+="</td>"; 
		      navigator+="</tr>";
		      return navigator;
	   }
	   
	   public String getNavigator(String colspan,String url_page,int page,int next,int nrecords,String msg,int PAGE_SIZE,String content,String contentLink) 
	   { 
		   	  int tot=(int)Math.ceil((double)nrecords/PAGE_SIZE);
		   	  String tp=page+"";
		   	  int totpage=Integer.parseInt(tp.substring(0,(tp.length()-1))+"0");
		   	  String message="";
		   	  String navigator="<tr class=\"AFCColumnTD\" height=\"20\">";
		   	
		   	  navigator+="<td width=\"80%\" nowrap align=\"center\" colspan=\""+colspan+"\">";
			  
			  if(next==1)
	           navigator+="&nbsp;"+next+"&nbsp;di&nbsp;"+next+"&nbsp;"; 
	          else
	          { 
	        	//First
	        	if(page!=1)
	        	  navigator+="<a class=\"AFCNavigatorLink\" title=\"Primo\" onclick=\"linkOggetto('"+url_page+"&LINKSPage=1');\" href=\"#\"><img border=\"0\" src=\"../Themes/AFC/FirstOn.gif\" /></a>"+this.getNbsp();	 
	        	else
	        	  navigator+="<img border=\"0\" src=\"../Themes/AFC/Vuoto.gif\" />"+this.getNbsp();	 
	            	
	        	
	        	//Prev 
	        	if(page>10/*PAGE_SIZE*/)
	        	{
	        	  if(page%10/*PAGE_SIZE*/==0)
	       		 	navigator+="<a class=\"AFCNavigatorLink\"  title=\"Precedente\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+(totpage-10/*PAGE_SIZE*/)+"');\" href=\"#\"><img border=\"0\" src=\"../Themes/AFC/PrevOn.gif\" /></a>"+this.getNbsp();	 
	        	  else
	        		navigator+="<a class=\"AFCNavigatorLink\" title=\"Precedente\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+totpage+"');\" href=\"#\"><img border=\"0\" src=\"../Themes/AFC/PrevOn.gif\" /></a>"+this.getNbsp();	 
	         	}
	        	else
	        	 navigator+="<img border=\"0\" src=\"../Themes/AFC/Vuoto.gif\" />"+this.getNbsp();
	        		
	        	//Numero di pagina
	        	if(nrecords<(PAGE_SIZE*10)/*100*/)
	        	{
	        	  if(nrecords<=PAGE_SIZE/*10*/)
	        		navigator+="&nbsp;1&nbsp;di&nbsp;1&nbsp;";   
	        	  else
	        	  {
	        		for(int c=1;c<=tot;c++) 
	        	   	{
	        		 if (c==page)
	        		   navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+page+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+page+"');\" href=\"#\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
	        		 else   	 
	        		   navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+c+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+c+"');\" href=\"#\">"+c+"</a>&nbsp;";	 
	        	   	}  	
	          	  }
	        	}
	        	else
	        	{
	        		if(totpage==0)
	        		{ 
	        		  for(int c=1;c<=10;c++) 
	            	  {
	           		   if (c==page)
	           		     navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+page+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+page+"');\" href=\"#\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
	           		   else   	 
	           		     navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+c+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+c+"');\" href=\"#\">"+c+"</a>&nbsp;";	 
	                  }
	            	}
	        		else
	        		{
	        		  if(page%10==0)
	    	          {
	        			 for(int c=(totpage-10/*PAGE_SIZE*/+1);c<=page;c++) 
	                  	 {
	                		if (c==page)
	                		   navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+page+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+page+"');\" href=\"#\" href=\""+url_page+"&LINKSPage="+page+"\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
	                		else  	 
	                		   navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+c+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+c+"');\" href=\"#\">"+c+"</a>&nbsp;";	 
	                  	 }
	        		  }
	        		  else
	        		  {	  
		        		if(tot<(totpage+10/*PAGE_SIZE*/))
		        		{
		        		 for(int c=totpage+1;c<=tot;c++) 
		             	 {
		           		   if (c==page)
		           		     navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+page+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+page+"');\" href=\"#\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
		           		   else  	 
		           		     navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+c+"\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+c+"');\" href=\"#\">"+c+"</a>&nbsp;";	 
		            	 }
		        		}
		        		else
		        		{
		        			for(int c=totpage+1;c<=totpage+10/*PAGE_SIZE*/;c++) 
			             	 {
			           		   if (c==page)
			           		     navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+page+"\" style=\"text-decoration: none\"   onclick=\"linkOggetto('"+url_page+"&LINKSPage="+page+"');\" href=\"#\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
			           		   else  	 
			           		     navigator+="<a class=\"AFCNavigatorLink\" title=\"Page "+c+"\" style=\"text-decoration: none\"  onclick=\"linkOggetto('"+url_page+"&LINKSPage="+c+"');\" href=\"#\">"+c+"</a>&nbsp;";	 
			             	 }
		        		}
	        		  }
	        		}
	        	}
	        	
	        	// Next
	            if(next>page)  
	            { 
	               if(page%10/*PAGE_SIZE*/==0)
	          		 navigator+="<a class=\"AFCNavigatorLink\" title=\"Prossimo\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+(totpage+1)+"');\" href=\"#\"><img border=\"0\" src=\"../Themes/AFC/NextOn.gif\" /></a>"+this.getNbsp();	 
	           	   else
	           		 navigator+="<a class=\"AFCNavigatorLink\" title=\"Prossimo\" style=\"text-decoration: none\" onclick=\"linkOggetto('"+url_page+"&LINKSPage="+(totpage+10+1)+"');\" href=\"#\"><img border=\"0\" src=\"../Themes/AFC/NextOn.gif\" /></a>"+this.getNbsp();	 
		        }
	            else
	              navigator+="<img border=\"0\" src=\"../Themes/AFC/Vuoto.gif\" />"+this.getNbsp();	
		        
	         }	
        	 navigator+="</td>"; 
        	 
        	 navigator+="<td width=\"20%\" nowrap align=\"right\">"+content+"</td>"; 
        	 navigator+="<td width=\"10%\" nowrap align=\"right\">"+contentLink+"</td>"; 
			 navigator+="</tr>";
	         return navigator;
	   }
	   
	   
	   public String getNavigator(String colspan,String url_page,int page,int next,int numLinks) 
	   { 
		   	  String navigator="<tr>";
		   	  navigator+="<td class=\"AFCFooterTD\" nowrap align=\"center\" colspan=\""+colspan+"\">";
		 
			  if(next==1)
	           navigator+="&nbsp;"+next+"&nbsp;di&nbsp;"+next+"&nbsp;"; 
	          else
	          { 
	        	if(page>numLinks)
	            {
	            	int n=page-numLinks;
	            	boolean prev=false;
	            	  for(int c=1;c<n;c++) 
	            	  {
	            		 if(c==1)
	            		 {
	            			 navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+((c+numLinks)-1)+"\"><<</a>&nbsp;";	 
	            		     prev=true;
	            		 }
	                 	    
	            		  navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+(c+numLinks)+"\">"+(c+numLinks)+"</a>&nbsp;";	 
	            	  }
	            	if(!prev)  
	            	 navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+(page-1)+"\"><<</a>&nbsp;";	 
	               	navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+page+"\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
	                            	
	            }
	            else
	            {
	            	for(int c=1;c<page;c++) 
	                    navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+c+"\">"+c+"</a>&nbsp;";	 
	                  
	                navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+page+"\"><font color=\"#FEB658\">"+page+"</font></a>&nbsp;";	 
	            }
	           	 
	          }
			  
			  if(next>page)  
				navigator+="<a class=\"AFCNavigatorLink\" style=\"text-decoration: none\" href=\""+url_page+"&LINKSPage="+next+"\">>></a>&nbsp;";	 
			  navigator+="</td>"; 
			  navigator+="</tr>";
			  return navigator;
	   }
	   
	   /*** TAG <IMG> ***/

	   public String getImg(String src) 
	   {
              return "<img border=\"0\" src=\""+src+"\"/>";
	   } 
	   
	   public String getImgVUOTO(String src) 
	   {
              return "<img border=\"0\" src=\""+src+"\" width=\"16\" height=\"16\"/>";
	   } 
	   
	   public String getImg(String title,String src) 
	   {
		   	  return getImage("",title,src);
	   }  
	   
	   public String getImg(String style,String title,String src) 
	   {
              return getImage(style,title,src);
       } 
	   
	   public String getImg(String style,String onload,String align,String title,String src,String oncontextmenu,String content) 
	   {        
              return "<img style=\""+style+"\" onload=\""+onload+"\" title=\""+title+"\" align=\""+align+"\" src=\""+src+"\" oncontextmenu=\""+oncontextmenu+"\" >"+content+"</img>";
	   }
	   
	   public String getImg(String style,String onload,String align,String title,String src,String content) 
	   {        
              return "<img style=\""+style+"\" onload=\""+onload+"\" title=\""+title+"\" align=\""+align+"\" src=\""+src+"\">"+content+"</img>";
	   }
	   
	   private String getImage(String style, String title,String src) 
	   {        
		   	   return "<img style=\""+style+"\" title=\""+title+"\" border=\"\" src=\""+src+"\" onload=\"fixPNG(this,'16','16')\" />";
	   }
	   
	   public String getImageWFix(String id,String title,String src,String align,String border) 
	   {        
              return "<img id=\""+id+"\" style=\"cursor: hand\" title=\""+title+"\" align=\""+align+"\" border=\""+border+"\" src=\""+src+"\" />";
	   }
	   
	   public String getImgHand(String title,String src) 
	   {
              return getImage("cursor: hand",title,src);         
	   }
	   
	   public String getImgM(String title,String content) 
	   {
              return "<img style=\"cursor: hand\"  title=\""+title+"\" border=\"\" "+content+"></img>";
	   }
	   
	   public String getImgMultipla(String content) 
	   {
              return "<img style=\"cursor: hand\" title=\"\" border=\"\" "+content+" onload=\"fixPNG(this,'16','16')\"></img>";
	   }
	   
	   public String getImgMultipla(String title,String content) 
	   {
              return "<img style=\"cursor: hand\"  title=\""+title+"\" border=\"\" "+content+" onload=\"fixPNG(this,'16','16')\"></img>";
	   }
	   
	   /*** TAG <INPUT> ***/  
	   
	   public String getInput(String id,String type,String value) 
	   {
		      return getInput("","",id,type,value,"");
	   }
	 
	   public String getInput(String style,String id,String type,String value,String name,boolean disabled)  
	   {
		      if(disabled)
			   return "<input style=\""+style+"\" id=\""+id+"\" type=\""+type+"\" value=\""+value+"\" name=\""+name+"\" disabled></input>";
              else
       		   return "<input style=\""+style+"\" id=\""+id+"\" type=\""+type+"\" value=\""+value+"\" name=\""+name+"\"></input>";
	   }
	   
	   public String getInput(String style,String onclick,String id,String type,String value,String name) 
	   {
              return "<input style=\""+style+"\" onclick=\""+onclick+"\" id=\""+id+"\" type=\""+type+"\" value=\""+value+"\" name=\""+name+"\">\n";
	   }
	   
	   public String getInput(String style,String type,String size,String value,String name) 
	   {
		      return "<input style=\""+style+"\" type=\""+type+"\" size=\""+size+"\" value=\""+value+"\" name=\""+name+"\"></input>";
	   }
	   
	   public String getInput(String style,String type,String id,String name,String size,String value,String onclick) 
	   {
              return "<input style=\""+style+"\" id=\""+id+"\" type=\""+type+"\" size=\""+size+"\" value=\""+value+"\" name=\""+name+"\" onclick=\""+onclick+"\"></input>";
	   } 
	   
	   public String getInput(String title,String style,String type,String id,String name,String size,String value,String onclick) 
	   {
              return "<input title=\""+title+"\" style=\""+style+"\" id=\""+id+"\" type=\""+type+"\" size=\""+size+"\" value=\""+value+"\" name=\""+name+"\" onclick=\""+onclick+"\"></input>";
	   }
	   
	   /*** TAG SPACE ***/ 
	   
	   
	   public String getNbsp() 
	   {
              return "&nbsp;";
	   }
	   
	   /*** TAG <P> ***/
	   
	   public String getP(String align,String content) 
	   {
              return "<p align=\""+align+"\">"+content+"</p>";
	   }
	   
	   public String getP(String align,String classe,String content) 
	   {
              return "<p align=\""+align+"\" class=\""+classe+"\">"+content+"</p>";
	   }
	   
	   
	   /***************************************************************************
		* METODI POPUP PAGE	
		**************************************************************************/
	
		/** ANNULLADOC **/ 
		public String AnnullaDocPop(String idDocumento,String idCartProveninez,String idQuery) 
		{
		       String idQueryProvenienza;       
		       if(idQuery==null)
		         idQueryProvenienza="-1";
		       else
		         idQueryProvenienza=idQuery;
		            
		       String sParametri="idDocumento="+idDocumento;
		              sParametri+="&idCartProveninez="+idCartProveninez;
		              sParametri+="&idQueryProveninez="+idQueryProvenienza;
		       return popupHConfirm("eliminare","il documento","AnnullaDoc.do?"+sParametri);
		 } 
		  
		 /** ALLEGATIVIEW **/ 
		 public String AllegatiViewPop(String idDocumento) 
		 {
		        String sParametri="idDoc="+idDocumento;
		        return popup("../restrict/MenuAllegatiView.do?"+sParametri,"700","150","0","50");
		 }  
		 
		 /** DOCUMENTOVIEW **/ 
		 public String DocumentoViewPop(String idTipoDoc,String idDoc,String rw,String cm,String area,String cr,
				 						String idCartProveninez,String idQueryProvenienza,String Provenienza,String stato,String url) 
		 {
				String sParametri="idDoc="+idDoc;
				sParametri+="&rw="+rw;
				sParametri+="&cm="+cm;
				sParametri+="&area="+area;
				sParametri+="&cr="+cr;  
				if(idCartProveninez==null)
				  idCartProveninez="";	
				sParametri+="&idCartProveninez="+idCartProveninez;
				sParametri+="&idQueryProveninez="+idQueryProvenienza;
				sParametri+="&Provenienza="+Provenienza;
				sParametri+="&stato="+stato;
				sParametri+="&MVPG=ServletModulisticaDocumento";
				String par="'&listaID='+document.getElementById('ListaId').value";
				if(url!=null && !(url.equals("")))
				  sParametri+="&GDC_Link="+URLEncoder.encode(url);
				
				return popupFullScreen("DocumentoView.do?"+sParametri,par);
		 }
		 
		 public String DocumentoViewLink(String idTipoDoc,String idDoc,String rw,String cm,String area,String cr,
					String idCartProveninez,String idQueryProvenienza,String Provenienza,String stato,String url) 
		{
			String sParametri="idDoc="+idDoc;
			sParametri+="&rw="+rw;
			sParametri+="&cm="+cm;
			sParametri+="&area="+area;
			sParametri+="&cr="+cr;  
			if(idCartProveninez==null)
			idCartProveninez="";	
			sParametri+="&idCartProveninez="+idCartProveninez;
			sParametri+="&idQueryProveninez="+idQueryProvenienza;
			sParametri+="&Provenienza="+Provenienza;
			sParametri+="&stato="+stato;
			sParametri+="&MVPG=ServletModulisticaDocumento";
			if(url!=null && !(url.equals("")))
				sParametri+="&GDC_Link="+URLEncoder.encode(url);
			
			return "DocumentoView.do?"+sParametri;
		}
		 
		 /** ELENCO CARTELLE COLLEGATE PER DOCUMENTO **/ 
		 public String ElencoCartellePerDoc(String idDoc,String idCartella,String idCartProveninez,String idQueryProvenienza,String Provenienza,String tipo,String stato) 
		 {
		        String sParametri="idDoc="+idDoc;
		        sParametri+="&Provenienza="+Provenienza;
		        sParametri+="&idCartProveninez="+idCartProveninez;
		        sParametri+="&idQueryProveninez="+idQueryProvenienza;
		        if(tipo.equals("D"))
		        	sParametri+="&tipo=D&stato="+stato;
		        else
		        {
		        	sParametri+="&tipo=C";
		        	sParametri+="&idCartella="+idCartella;
		        }
		        
		        return popup("common/ElencoCartellePerDoc.do?"+sParametri,"Elenco cartelle collegate","700","700","0","50",true);
		 }
		 
		 /** ELENCO FLUSSI PER DOCUMENTO **/ 
		 public String ElencoFlussiPerDoc(String area,String cm,String cr,String idDoc) 
		 {
		        String sParametri="terna="+area+"@"+cm+"@"+cr;
		        sParametri+="&idDoc="+idDoc;
		        return popup("./flussoDocumentoServlet?"+sParametri,"Elenco flussi","800","600","0","50",true);
		 }
		 
		 /** GESTIONE MANUALI **/ 
		public String ManualiPop(String id,String nome,String path) 
		{
		       String sParametri="";
		       sParametri="id=GDMWEB/WRKSPC"+id;
		       sParametri+="&nome="+nome; 
		       sParametri+="&path="+path;
		       return popup("common/GestioneManuali.do?"+sParametri,"Gestione manuali","600","150","0","50",true);
		}
		 
		 /** CARTELLAMAINT **/ 
		public String CartellaMaintPop(String provenienza,String tipoUso,String rw,String id,String idCartProvenienza) 
		{
		       String segno="";
		       String sParametri="";
		       sParametri="Provenienza="+provenienza;
		       sParametri+="&rw="+rw;
		       if (provenienza.equals("Q")) segno="-";
		       sParametri+="&id="+segno+id;
		       sParametri+="&idCartProveninez="+idCartProvenienza;
		       sParametri+="&page=M&compM=S";
		       if(tipoUso.equals("R"))
		    	 sParametri+="&tipoUso=R";  
		       if(provenienza.equals("W"))
		         return popupHiddenNew("CartellaMaint.do?"+sParametri,"CartellaW");
		       else
		         return popupH("CartellaMaint.do?"+sParametri);
		}
		
		/** COMPETENZE **/ 
		public String CompetenzePop(String oggetto,String tipoObj,String Visualizza,String idCartProveninez,
									String idQueryProvenienza,String Provenienza ) 
		{
		       String sParametri="oggetto="+oggetto;
		              sParametri+="&tipoObj="+tipoObj;
		              sParametri+="&Visualizza="+Visualizza;
		              sParametri+="&idCartProveninez="+idCartProveninez;
		              sParametri+="&idQueryProveninez="+idQueryProvenienza;
		              sParametri+="&Provenienza="+Provenienza;
		        
		       if(Provenienza.equals("W"))
		         return popupNew("Competenze_New.do?"+sParametri,"Gestione Competenze WorkSpace","800","650","0","50",true);
		       else
		       {
		    	 if(tipoObj.equals("D"))
		    	   return popup("common/Competenze_New.do?"+sParametri,"Gestione Competenze","800","650","0","50",true); 
		    	 else {
			    	 if (Visualizza.equals("Y"))
			          return popup("common/Competenze_New.do?"+sParametri,"Gestione Competenze","800","650","0","50",true);
			         else
			          return popup("common/Competenze_New.do?"+sParametri,"Gestione Competenze","800","650","0","50",true);
		    	 }
		       }
		} 
		
		/** CONTROLLO_OUTPUT **/ 
		public String Controllo_Output_Pop(String url_page) 
		{
			   String par="'&listaID='+document.getElementById('ListaId').value+";
			   		  par+="'&idCartProveninez='+document.getElementById('idCartella').value+";
			   		  par+="'&idQueryProveninez='+document.getElementById('idQuery').value+";
			   		  par+="'&idCartAppartenenza='+document.getElementById('idCartAppartenenza').value+";
			   		  par+="'&tipoworkspace='+document.getElementById('wrksp').value";
	           return popup_parametri(url_page,par,"800","600","0","50");
		}
		
		
		 /** MODIFICA COLLEGAMENTO ESTERNO **/ 
		public String CollegamentoEsternoPop(String id,String idCartProvenienza) 
		{
		       String sParametri="";
		       sParametri+="id="+id;
		       sParametri+="&idCartella="+idCartProvenienza;
		       return popup("./collegamentiEsterniServlet?"+sParametri,"Modifica/Elimina Collegamento Esterno","700","300","0","50",true);
		}
		
		/** MSGBOXPAGE **/ 
		public String MsgBoxPagePop(String from) 
		{
			   String sParametri="From="+from;
               return  popup_Error("MsgBoxPage.do?"+sParametri);         
		} 
		
		/** AMVLOADING **/ 
		public String AmvLoadingPop(String msg) 
		{
			   String sParametri="msg="+URLEncoder.encode(msg);
			   return  "../common/AmvLoading.do?"+sParametri;         
		} 
  
		public String AmvLoadingPop(String msg,String redirect) 
		{
			   String sParametri="msg="+URLEncoder.encode(msg);
			   sParametri+="&redirect="+URLEncoder.encode(redirect);
			   return  "../common/AmvLoading.do?"+sParametri;         
		}
		
		public String MessagePagePop(String msg,String redirect) 
		{
			   String sParametri="msg="+URLEncoder.encode(msg);
			   sParametri+="&redirect="+URLEncoder.encode(redirect);
			   return  "../common/MessagePage.do?"+sParametri;         
		}
		
		public String MessagePagePop(String msg) 
		{
			   String sParametri="msg="+URLEncoder.encode(msg);
			   return  "../common/MessagePage.do?"+sParametri;         
		}
		
		public String MsgBoxPagePop(String from,String msg_ERROR) 
		{
			   String sParametri="From="+from+"&MSG_ERROR="+msg_ERROR;
               return  "MsgBoxPage.do?"+sParametri;         
		}
		
		public String MsgBoxPagePopConfirm(String from,String azione,String oggetto) 
		{
               String sParametri="From="+from;
               return  popup_ErrorConfirm(azione,oggetto,"MsgBoxPage.do?"+sParametri);         
		} 
  
		/** SCELATIPODOCUMENTOFORNEW **/ 
		public String SceltaTipoDocumentoForNewPop(String idDocumento,String idCartProveninez,String idQueryProvenienza,String Provenienza ) 
		{
			   String sParametri="idDocumento="+idDocumento;
	                  sParametri+="&idCartProveninez="+idCartProveninez;
	                  sParametri+="&idQueryProveninez="+idQueryProvenienza;
	                  sParametri+="&Provenienza="+Provenienza;
	           return popup("SceltaTipoDocumentoForNew.do?"+sParametri,"500","400","0","50");
		} 
		
		/** VISTACARTDEL **/ 
		public String VistaCartDelPop(String oggetto,String azione,String idOggetto,String idCartProvenienza,String tipoOggetto) 
		{
			   String sParametri="idOggetto="+idOggetto;
			   		  sParametri+="&idCartProveninez="+idCartProvenienza;
			   		  sParametri+="&tipoOggetto="+tipoOggetto;
			   return popupHConfirm(oggetto,azione,"VistaCartDel.do?"+sParametri);
		}
		
		/** VISTAMAINT **/ 
		public String VistaMaintPop(String id,String idCartProvenienza) 
		{
			   String sParametri="id="+id;
                      sParametri+="&idCartProveninez="+idCartProvenienza+"";
               return popupH("VistaMaint.do?"+sParametri);
		}
		
		/** SERVLETMODULISTICA **/ 
		public String ServletModulisticaPop(String cm,String area,String cr,String url_servlet,String title) 
		{
			return ServletModulisticaPop(cm,area,cr,url_servlet,title,false);
		}
		
		public String ServletModulisticaPop(String cm,String area,String cr,String url_servlet,String title,boolean modal) 
		{
			   String sParametri="cr="+cr;  
               		  sParametri+="&cm="+cm;
               		  sParametri+="&area="+area;
               		  sParametri+="&rw=R";
               String nomefile=url_servlet+sParametri;
               
               if(modal)
            	   return "parent.openWindowModale('"+nomefile+"','"+title+"','700','600');";  
               else
            	   return "window.open('"+nomefile+"','popup','toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=700,height=600,top=50,left=50');";
		}
		
		/** SERVLETMODULISTICADOC **/
		public String ServletModulisticaDocRifPop(String cm,String area,String cr,String url_servlet) 
		{
		       String sParametri="cr="+cr;  
		               sParametri+="&cm="+cm;
		               sParametri+="&area="+area;
		               sParametri+="&rw=R";
		        
		       String nomefile=url_servlet+sParametri;
		       return "window.open('"+nomefile+"','popup_ServletModulistica','toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=700,height=600,top=50,left=50');";
		}
		
		public String DocumentoViewRifPop(String idTipoDoc,String idDoc,String rw,String cm,String area,String cr,
										  String idCartProveninez,String idQueryProvenienza,String Provenienza,String stato,String url) 
		{
			   String sParametri="idDoc="+idDoc;
			   sParametri+="&rw="+rw;
			   sParametri+="&cm="+cm;
			   sParametri+="&area="+area;
			   sParametri+="&cr="+cr;  
			   sParametri+="&idCartProveninez="+idCartProveninez;
			   sParametri+="&idQueryProveninez="+idQueryProvenienza;
			   sParametri+="&Provenienza="+Provenienza;
			   sParametri+="&stato="+stato;
		       sParametri+="&MVPG=ServletModulisticaDocumento";
			   String par="'&listaID='";//'+document.getElementById('ListaId').value";
			   if(rw.equals("W"))
				 sParametri+="&GDC_Link="+URLEncoder.encode(url);
			   //String nome="DocumentoView.do?"+sParametri+par;
			   return popupFullScreenNew("DocumentoView.do?"+sParametri,par);
		}
		
		/** POPUP DI BASE **/

		public String popup(String win)
		{
			   return "window.open('"+win+"');";
		}
		
		public String popup(String win,String nome)
		{
			   return "window.open('"+win+"','"+nome+"');";
		}
		public String popup(String win,String nome,String style)
		{
			   return "window.open('"+win+"','"+nome+"','"+style+"');";
		}
		
		private String popupFullScreen(String win,String par)
		{
	         	return "popupFullScreen('"+win+"',"+par+");";
		}
	  
		private String popupFullScreenNew(String win,String par)
		{
		        return "popupFullScreenNew('"+win+"',"+par+");";
		}
		  
		public String popup_parametri(String win,String par,String w,String h, String x, String y)
		{
				return "popup_parametri('"+win+"',"+par+","+w+","+h+","+x+","+y+");";
		}
	  
		public String popup(String win,String w,String h, String x, String y)
		{
			return popup(win,"",w,h,x,y,false);
		}
		
		public String popup(String win,String title,String w,String h, String x, String y,boolean modal)
		{
			if(modal){
				return "popupModali('"+win+"','"+title+"',"+w+","+h+","+x+","+y+");";
				//return "parent.openWindowModale('"+win+"','"+title+"',"+w+","+h+")"; 
			}
			else
				return "popup('"+win+"',"+w+","+h+","+x+","+y+");";
		}
	  
		public String popupNew(String win,String title,String w,String h, String x, String y)
		{
			return popupNew(win,title,w,h,x,y,false);
		}
		
		public String popupNew(String win,String title,String w,String h, String x, String y,boolean modal)
		{
			if(modal){
				return "popup('"+win+"','"+title+"',"+w+","+h+","+x+","+y+");";
				//return "parent.openWindowModale('"+win+"','"+title+"',"+w+","+h+")"; 
			}
			else
				return "popupNew('"+win+"','"+title+"',"+w+","+h+","+x+","+y+");";
		}
	  
		private String popupH(String win)
		{
				return "popup_hidden('"+win+"');";
		}
	  
		private String popupHiddenNew(String win,String nome)
		{
				return "popup_hidden_New('"+win+"','"+nome+"');";
		}
	  
		private String popup_Error(String win)
		{
				return "popup_hidden_Error('"+win+"');";
		}
	  
		private String popup_ErrorConfirm(String azione,String oggetto,String win)
		{
				String confirm;
				confirm="if (confirm('Sei sicuro di voler "+azione+" "+oggetto+"?')==true) ";
		        return confirm + popup_Error(win);
		}
  
		private String popupConfirm(String azione,String oggetto,String win,String w,String h, String x, String y)
		{
	         	String confirm;
	         	confirm="if (confirm('Sei sicuro di voler "+azione+" "+oggetto+"?')==true) ";
	         	return confirm + popup(win, w, h, x, y);
		}
 
		private String popupHConfirm(String oggetto,String azione,String win)
		{
				String confirm;
				confirm="if (confirm('Sei sicuro di voler "+oggetto+" "+azione+"?')==true) ";
                if(oggetto.equals("Area di Lavoro"))
		          return confirm + popupHiddenNew(win,"azioneW");
		        else
		          return confirm + popupH(win);
		}
}
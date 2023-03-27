package it.finmatica.dmServer.jdms;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.dmServer.check.*;

/**
 * Gestione check in/out dei documenti.
 * Classe di servizio per la gestione del Client
*/

public class CCS_CheckDocumenti {
	
	
	private static String _PATHIMG             ="./images/standard/action/";
	private static String _ALERT                =_PATHIMG+"warning.png";  
	private String[] listaId;
	private String seqDoc="";
	private int check_livello;
	private String tipoOperazione;
	private String utente;
	CCS_Common CCS_common;
	CCS_HTML h;
	HttpServletRequest request;
	private IDbOperationSQL dbOp;
	private DMServer4j log;
	private StringBuffer messageError;
	private int lockDoc=0;
	
	
	public CCS_CheckDocumenti(String seq,String livello,HttpServletRequest req,CCS_Common newCommon) throws Exception
	{
		   if(seq!=null){   
		    listaId=seq.split("@");
		    seqDoc=seq;
		   }
		   check_livello=Integer.parseInt(livello);
		   request=req;
		   
		   messageError=new StringBuffer();
	       CCS_common=newCommon;
	       utente=CCS_common.user;
	       log= new DMServer4j(CCS_EliminaOggetti.class,CCS_common); 
	       h = new CCS_HTML();
	       dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	       
	       XSS_Encoder xss = new XSS_Encoder(request,CCS_common);
	       tipoOperazione = xss.encodeHtmlAttribute("tipo",req.getParameter("tipo"));	       
    }	
	
    public void checkINDocumenti() throws Exception {
    	   CheckDocumento ck;  
    	   int result;
    	
    	   try
		   {
    		 if(listaId.length>0) {
		       ck = new CheckDocumento(dbOp.getConn());
		       
		       for(int i=0; i<listaId.length; i++) {
		    	  String doc=listaId[i].substring(1,listaId[i].length());
		    	  
		    	  ck.settaDocumento(doc);
		    	  
		    	  if(ck.getLivello()!=0)
			    	messageError.append("- Il Documento è stato già bloccato dall'utente "+ck.getNominativo()+"<br>");  
			      else {
		    	    
			    	  result=ck.checkOut(utente,check_livello);
		    	  	  if(result==-1)
			    	   messageError.append("- "+ck.getErrorMessage()+"<br>");
			    	  else { 
			    	   lockDoc++;
			    	   ck.commit();
			    	  }
		    	  } 
		       }
		    	 
		     }
		   }
		   catch (Exception e) 
		   {
			 log.log_error("CCS_CheckDocumenti::checkINDocumenti -- Check IN sulla sequenza di documenti: "+seqDoc+" "+e.getMessage());
			 throw e;
		   }
		   finally {
			   _finally();
		   }
     }
    
     public void checkOUTDocumenti() throws Exception {
    	   CheckDocumento ck;  
	 	   int result;
	 	
	 	   try
			   {
	 		 if(listaId.length>0) {
			       ck = new CheckDocumento(dbOp.getConn());
			       
			       for(int i=0; i<listaId.length; i++) {
			    	  String doc=listaId[i].substring(1,listaId[i].length());
			    	  
			    	  ck.settaDocumento(doc);
			    	  
			    	  if(ck.getLivello()==0)
			    	 	messageError.append("- Documento non bloccato.<br>");
			    	  else {
				    	  if(utente.equals(ck.getUtente())) {
				    		  result=ck.checkIn();
					    	  if(result==-1)
					    	   messageError.append("- "+ck.getErrorMessage()+"<br>");
					    	  else { 
					    	   lockDoc++; 
					    	   ck.commit();
					    	  }
				    	  }
				    	  else
				    	   messageError.append("- IL documento è bloccato dall'utente "+ck.getNominativo()+"<br>");
			         }
			       } 
			     }
			   }
			   catch (Exception e) 
			   {
				 log.log_error("CCS_CheckDocumenti::checkINDocumenti -- Check OUT sulla sequenza di documenti: "+seqDoc+" "+e.getMessage());
				 throw e;
			   }
			   finally {
				   _finally();
			   }
    }    
   
    public String getMessageReport() {
    	    StringBuffer msg =new StringBuffer();
    	    
    	    msg.append("<table width=\"80%\" align=\"center\" cellpadding=\"1\" cellspacing=\"10\">");
    	    msg.append("<tr><td class=\"barraTD\" >");
	    	msg.append("<p><img border=\"0\" src=\""+_ALERT+"\" width=\"16\" height=\"16\" align=\"texttop\"></p>");
	    	msg.append("<p>");
	    	msg.append("Avviso:<br>");
    	    if(lockDoc>0 && tipoOperazione!=null) {
    	      if(tipoOperazione.equals("I")){
    	    	if(lockDoc==1)
    	    	  msg.append("<br>Il documento è stato bloccato."); 	
    	    	else
    	    	  msg.append("<br>Sono stati bloccati "+lockDoc+" documenti."); 	
    	      }	
    	      else{
    	    	if(lockDoc==1)
        	     msg.append("<br>Il documento è stato sbloccato."); 	
        	    else
        	     msg.append("<br>Sono stati sbloccati "+lockDoc+" documenti."); 
    	      }	
    	    }  
    	    
    	    if(!messageError.toString().equals("")){
    	    	if(tipoOperazione.equals("I"))
    	    	  msg.append("<br>Alcuni documenti non sono stati bloccati.");
    	    	else
    	    	  msg.append("<br>Alcuni documenti non sono stati sbloccati.");	
    	    } 
			 
    	    if(lockDoc==0 && messageError.toString().equals(""))
    	      msg.append(" <p>Problemi durante l'operazione.<br>La pagina potrebbe non essere visualizzata o funzionare correttamente.</p><p>&nbsp;</p>");
			msg.append("</p>");
	    	msg.append("</td></tr>");
	    	if(!messageError.toString().equals("")) {
		        msg.append("<tr><td align=\"center\">");
		    	msg.append("<button id=\"button\" name=\"button\" title=\"Visualizza messaggi di errore\" type=\"button\" class=\"textPulsanteDefault\" onMouseOver=\"this.className='textPulsanteMouseOver'\" onMouseOut=\"this.className='textPulsanteDefault'\" onMouseDown=\"this.className='textPulsanteMouseDown'\" onMouseUp=\"this.className='textPulsanteMouseOver'\"");
		    	msg.append(" onclick=\"visualizzaErrore();\" ><NOBR>");
		    	msg.append(" <div class=\"textPulsante\" align=\"center\">Mostra Dettagli</div></NOBR>");
		    	msg.append(" </button></td></tr>");
		        msg.append("<tr><td class=\"barra\"><div id=\"msg\" style=\"display:none\"><p align=\"left\">"+messageError+"</p></div></td></tr>");
	    	}
	        msg.append("</table>");

    	    return msg.toString();
    }

	//Chiusura della connessione
	private void _finally() throws Exception {
		try
		{
			CCS_common.closeConnection(dbOp);
		}
		catch (Exception e) {
			throw e;
		}
	}
}

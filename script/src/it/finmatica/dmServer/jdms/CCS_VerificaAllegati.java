package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Impronta.SeganalazioniVerificaImpronte;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import javax.servlet.http.HttpServletRequest;

public class CCS_VerificaAllegati {

  private String doc="";
  CCS_Common CCS_common;
  CCS_HTML h;
  HttpServletRequest request;
  private IDbOperationSQL dbOp;
  private DMServer4j log;
  private String lista="";
  private String user;
  private Global g;
		
	public CCS_VerificaAllegati(String idDoc,HttpServletRequest req,CCS_Common newCommon) throws Exception {
		if(idDoc != null) {   
			doc = idDoc;
		}
		request			=	req;
		user				= req.getSession().getAttribute("Utente").toString();
		CCS_common	= newCommon;
		log					= new DMServer4j(CCS_EliminaOggetti.class,CCS_common); 
		dbOp				= SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		g						= new Global();
	}	
	
	public void verificaAllegati() throws Exception {
		try {
			Profilo p = new Profilo(doc);
			p.initVarEnv(user,user,dbOp.getConn());
			SeganalazioniVerificaImpronte ver = p.verificaImpronte512();
			for(int i=0;i<ver.getNumeroValori();i++) {
				if(i%2==0){
					lista+="<tr><td class=\"AFCDataTD\" width=\"100%\">"+getDescrizioneVerificaAllegati(ver,i)+"</td></tr>";
				} else {
					lista+="<tr><td class=\"AFCAltDataTD\" width=\"100%\">"+getDescrizioneVerificaAllegati(ver,i)+"</td></tr>";
				}
        /*if(i%2==0){
          lista+="<tr><td class=\"AFCDataTD\" width=\"40%\">"+ver.getNomeFile(i)+"</td>";
          lista+="<td class=\"AFCDataTD\" width=\"60%\">"+getDescrizioneVerificaAllegati(ver.getCodice(i))+"</td></tr>";
        } else {
          lista+="<tr><td class=\"AFCAltDataTD\" width=\"40%\">"+ver.getNomeFile(i)+"</td>";
          lista+="<td class=\"AFCAltDataTD\" width=\"60%\">"+getDescrizioneVerificaAllegati(ver.getCodice(i))+"</td></tr>";
        }*/
			}
	 	    
			CCS_common.closeConnection(dbOp);
		} catch (Exception e) {
			log.log_error("CCS_VerificaAllegati::verificaAllegati per il documento: "+doc+" "+e.getMessage());
			CCS_common.closeConnection(dbOp); 
			throw e;
		}
	}    
	
/*	private String getDescrizioneVerificaAllegati(String cod){
		String desc=""; 
	   
	     if(g.CODERROR_IA_NESSUN_ERRORE.equals(cod))
			desc="Il file è corretto.";
		
	     //Non utilizzato perchè il documento esiste 
	     //if(g.CODERROR_IA_DOCUMENTO_INESISTENTE.equals(cod))
		 //		desc="Il documento non esiste";
	     
	     if(g.CODERROR_IA_IMPRONTA_ASSENTE.equals(cod))
				desc="L'impronta del file non è presente.";
	     
	     if(g.CODERROR_IA_ALLEGATO_MODIFICATO.equals(cod))
				desc="Il file è stato modificato.";
	     
	     if(g.CODERROR_IA_ALLEGATO_CANCELLATO.equals(cod))
				desc="Il file è stato cancellato.";
 	    	 
	    return desc; 
 }*/

	private String getDescrizioneVerificaAllegati(SeganalazioniVerificaImpronte ver, int i){
		String desc=""; 
		String cod = "";
   
		cod = ver.getCodice(i);
		if(g.CODERROR_IA_NESSUN_ERRORE.equals(cod)) {
			desc="L'allegato "+ver.getNomeFile(i)+" è stato inserito il "+ver.getData(i)+". Il confronto con l'impronta conferma che non ha subito modifiche successive.";
		}

		if(g.CODERROR_IA_IMPRONTA_ASSENTE.equals(cod)) {
			desc="L'allegato "+ver.getNomeFile(i)+" è stato inserito il "+ver.getData(i)+". L'impronta non è presente.";
		}
   
		if(g.CODERROR_IA_ALLEGATO_MODIFICATO.equals(cod)) {
			desc="L'allegato "+ver.getNomeFile(i)+" è stato inserito il "+ver.getData(i)+". l confronto con l'impronta indica che l'allegato è stato modificato.";
		}
   
		if(g.CODERROR_IA_ALLEGATO_CANCELLATO.equals(cod)) {
			desc="L'allegato "+ver.getNomeFile(i)+" è stato cancellato.";
		}
   	 
		return desc; 
	}

	public String getMessageReport() {
		StringBuffer msg =new StringBuffer();
 	    
		if(lista.equals("")) {
			msg.append("<table align=\"center\" cellpadding=\"50\" cellspacing=\"5\"><tr><td><p align=\"center\"><font class=\"AFCFormHeaderFont\">Non ci sono allegati associati al documento!</font></p></td></tr></table>");
		} else {
			msg.append("<p><font class=\"AFCFormHeaderFont\">Verifica degli allegati:</font></p>");
		  msg.append("<table class=\"AFCFormTABLE\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">");
	 	  msg.append("<tbody>");
	 	  msg.append("<tr><td class=\"AFCColumnTD\" nowrap align=\"center\" width=\"100%\">Segnalazioni</td>");
//	 	msg.append("<tr><td class=\"AFCColumnTD\" nowrap align=\"center\" width=\"40%\">Allegato</td>");
//	 	msg.append("<td class=\"AFCColumnTD\" nowrap align=\"center\" width=\"60%\">Esito</td></tr>");
	 	  msg.append(lista);
	    msg.append("</tbody>");
	     msg.append("</table>");
		}
		return msg.toString();
	}
	
}

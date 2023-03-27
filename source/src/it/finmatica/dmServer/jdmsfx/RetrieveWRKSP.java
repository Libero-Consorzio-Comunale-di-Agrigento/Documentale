package it.finmatica.dmServer.jdmsfx;

import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.jdms.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

/**
 * RetrieveWRKSP
 * 
 * Questa classe è utlizzata da un servizio JAVASERVICE SOA 
 * per recuperare la lista delle workspace o settare 
 * una specifica workaspace.
 * 
 * AUTHOR @SCANDURRA
 *  
 * */

public class RetrieveWRKSP {
	
	private HttpServletRequest request;
	private String user;
    private String ruolo;
    private String modulo;
    private String wrksp;
    private String wrkspPref;
    private String idOggetto;
    private String srcWorkArea;
    private String idParent;
    
	public RetrieveWRKSP(HttpServletRequest req,String swrkspPref,String sWRKSP,String sidOggetto,IDbOperationSQL newdbOp) throws Exception
	{
		   request=req;
		   user=""+request.getSession().getAttribute("Utente");
		   ruolo=""+request.getSession().getAttribute("Ruolo");
	       modulo=""+request.getSession().getAttribute("Modulo");
	       if(swrkspPref!=null && !swrkspPref.equals("0"))
	        wrkspPref=swrkspPref;
	       if(sWRKSP!=null && !sWRKSP.equals("0"))
	        wrksp=sWRKSP;
	       if(sidOggetto!=null && !sidOggetto.equals("0"))
	        idOggetto=sidOggetto;
	       try {newdbOp.close();} catch(Exception e) {}
	}
	
	
	public String getRetrieveWRKSP() throws Exception {
		   String ret="";
		   try
	        {
	            if(wrksp != null && !wrksp.equals(""))
	            {
	                if(idOggetto == null)
	                    idOggetto = (new StringBuilder("C")).append(wrksp).toString();
	                ret = getWRKSPLinkDiretto();
	            }
	            else
	            {
	                if(wrkspPref == null || wrkspPref != null && wrkspPref.equals(""))
	                {
	                    wrksp = getWRKSP();
	                } 
	                else
	                {
	                    setWRKSP();
	                    wrksp = wrkspPref;
	                }
	                ret= wrksp;
	            }
			  
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	        return ret;
	}
	
	
	public String setRetrieveWRKSP() throws Exception {
	   try
        {
            setWRKSP();
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	   return wrkspPref;
	}
		
	private String getWRKSPLinkDiretto() throws Exception {
		     String ret = "";
		     try
		     {
		         CCS_WorkSpace w = new CCS_WorkSpace(user, ruolo, new CCS_Common("jdbc/gdm", user));
		         wrksp = w.getWrkSpDefault(wrksp);
		         if(idOggetto != null && idOggetto != "")
		             if(idOggetto.indexOf("C") != -1 || idOggetto.indexOf("Q") != -1)
		             {
		                 boolean ver = w.verificaCompetenza(idOggetto);
		                 if(ver)
		                     srcWorkArea = w.getIDCartProvenienza(idOggetto, wrksp, "GDC");
		                 else
		                     throw new Exception((new StringBuilder("Attenzione! Non si possiedono diritti di accesso sull'oggetto ")).append(idOggetto).append(" nel Documentale. <br> Contattare l'amministratore.").toString());
		                 if(idOggetto.indexOf("Q") != -1)
		                 {
		                     idParent = w.getPathTree(idOggetto, wrksp);
		                     if(idParent.equals(""))
		                         return "";
		                 } else
		                 {
		                     idParent = "";
		                 }
		                 if(!idParent.equals("") && idParent.indexOf("X") != -1)
		                     idParent = idParent.substring(idParent.lastIndexOf("X") + 1, idParent.length());
		                 ret = (new StringBuilder(String.valueOf(srcWorkArea))).append("@").append(idParent).toString();
		             } else
		             {
		                 throw new Exception("Attenzione! Non si possiedono i diritti sull'oggetto del Documentale. Occore specificare il tipo. <br> Contattare l'amministratore.");
		             }
		     }
		     catch(Exception e)
		     {
		         e.printStackTrace();
		         throw new Exception((new StringBuilder("getWRKSPLinkDiretto - retrievewrksp(WRKSP,idOggetto) - (")).append(wrksp).append(",").append(idOggetto).append(") : - Errore [Non \350 possibile accedere al Documentale ]\n").toString());
		     }
		     return ret;
	 }

	 private String getWRKSP() throws Exception
	 {
	     try
	     {
	         CCS_WorkSpace w = new CCS_WorkSpace(user, ruolo, modulo, new CCS_Common("jdbc/gdm", user));
	         return w.getWrkSpPreferenza();
	     }
	     catch(Exception e)
	     {
	         e.printStackTrace();
	     }
	     throw new Exception((new StringBuilder("Recupero Area di Lavoro - retrievewrksp(user,ruolo,modulo) - (")).append(user).append(",").append(ruolo).append(",").append(modulo).append(") : - Errore [Non \350 possibile accedere a nessun oggetto del Documentale ]\n").toString());
	 }
	
	 private void setWRKSP() throws Exception
	 {
	     try
	     {
	         CCS_WorkSpace w = new CCS_WorkSpace(user, ruolo, modulo, new CCS_Common("jdbc/gdm", user));
	         w.setWrkSpPreferenza(wrkspPref);
	     }
	     catch(Exception e)
	     {
	         e.printStackTrace();
	         throw new Exception("Attenzione! Non è possibile accedere a nessun oggetto del Documentale. Conttare l'amministratore.");
	     }
	 }

}

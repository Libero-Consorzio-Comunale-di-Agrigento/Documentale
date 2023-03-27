package it.finmatica.dmServer.elaborazioniBatch;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class AbstrElaborazioneBatch implements IElaborazioniBatch {
	private String jndi;
	private String user;
	private Connection cn;	
	private String tipoElaborazione;
	private String idElaborazione;
	private String nomeElaborazione;
	private String dataInizioElaborazione;
	
	private static Logger logger = Logger.getLogger(AbstrElaborazioneBatch.class);
			
	public final static String _STATO_ELAB_INIZIATA = "ATTIVA";
	public final static String _STATO_ELAB_FINITA   = "CONCLUSA";
	public final static String _STATO_ELAB_ERRORE   = "ERRORE";
		
	public AbstrElaborazioneBatch(String user,String jndi,String tipoElaborazione,String idElaborazione) {
		this.jndi=jndi;
		this.user=user;
		this.tipoElaborazione=tipoElaborazione;
		this.idElaborazione=idElaborazione;
	}
	
	public AbstrElaborazioneBatch(String user,Connection cn,String tipoElaborazione,String idElaborazione) {
		this.cn=cn;
		this.user=user;
		this.tipoElaborazione=tipoElaborazione;
		this.idElaborazione=idElaborazione;		
	}
	
	public AbstrElaborazioneBatch(String user,Connection cn,String tipoElaborazione) {
		this.cn=cn;
		this.user=user;
		this.tipoElaborazione=tipoElaborazione;
	}
	
	public AbstrElaborazioneBatch(String user,String jndi,String tipoElaborazione) {
		this.jndi=jndi;
		this.user=user;
		this.tipoElaborazione=tipoElaborazione;
	}

	public String getNomeElaborazione() {
		return nomeElaborazione;
	}

	public void setNomeElaborazione(String nomeElaborazione) {
		this.nomeElaborazione = nomeElaborazione;
	}
	
	public void creaElaborazione() throws Exception {		
		   creaElaborazione(null);
	}
		
	public void creaElaborazione(String idElaborazione) throws Exception {			
		boolean bEsisteElabAttiva;
		IDbOperationSQL dbOp;
		Profilo p;
		
		try {
			dbOp=getConn();
			
			//Verifica se esiste già un elaborazione di questo tipo per questo utente ed attiva
		    try {		    	
			    String sql="SELECT 1 FROM GDM_ELAB_BATCH "+ 
						   "WHERE UTENTE=:UTENTE AND "+
						   "STATO_ELABORAZIONE=:STATOELAB AND "+
						   "TIPO_ELABORAZIONE=:TIPOELAB "+
						   "FOR UPDATE";
			    
			    dbOp.setStatement(sql);
			    dbOp.setParameter(":UTENTE", user);
			    dbOp.setParameter(":STATOELAB",_STATO_ELAB_INIZIATA);
			    dbOp.setParameter(":TIPOELAB", tipoElaborazione);
			    dbOp.execute();
			    
			    bEsisteElabAttiva=dbOp.getRstSet().next(); 
			    
				try{dbOp.close();}catch (Exception ei) {}
		    }
			catch (Exception e) {
				try{dbOp.close();}catch (Exception ei) {}
				throw new Exception("Errore nel tenativo di verificare se esiste già un elaborazione attiva di tipo "+tipoElaborazione+" per l'utente "+user+".\nErrore="+e.getMessage());
			}
			
			if (bEsisteElabAttiva) throw new Exception("Attenzione! Esiste già un elaborazione attiva di tipo "+tipoElaborazione+" per l'utente "+user);
			
			
			p = new Profilo("ELABORAZIONI_BATCH","GDMSYS");
			
			if(idElaborazione==null)
				idElaborazione=getNextIdElaborazione();				
			
			if (cn==null)
				p.initVarEnv(user, "", new JNDIParameter(jndi));
			else
				p.initVarEnv(user, "", cn);
			
			p.settaValore("ID_ELABORAZIONE", idElaborazione);
			p.settaValore("TIPO_ELABORAZIONE", tipoElaborazione);
			p.settaValore("DESCRIZIONE", this.getNomeElaborazione());
			p.settaValore("UTENTE", user);
			
			if(dataInizioElaborazione!=null && !dataInizioElaborazione.equals(""))
				p.settaValore("DATA_INIZIO", dataInizioElaborazione);
			else
				p.settaValore("DATA_INIZIO", getNow());	
			
			p.settaValore("STATO_ELABORAZIONE", _STATO_ELAB_INIZIATA);			
			
			if (!(p.salva().booleanValue()))  throw new Exception("Errore in registrazione documento profilo: "+p.getError());					
			
			try{if(cn!=null) cn.commit();}catch (Exception ei) {}
			
			
		}
		catch (Exception e) {
			try{if(cn!=null) cn.rollback();}catch (Exception ei) {}
			throw new Exception("AbstrElaborazioneBatch::creaElaborazione - Errore: "+e.getMessage());
		}				
		
	}
		
	public void setDataInizioElaborazione(String dataInizioElaborazione) {
		this.dataInizioElaborazione = dataInizioElaborazione;
	}
	
	public String getDataInizioElaborazione()throws Exception {
		String data="";
		ResultSet rs = null;
		String sql="";
		
		try {
			if(idElaborazione!=null && !idElaborazione.equals("")) {
			
				IDbOperationSQL dbOp=getConn();
			
				try {		    	
				    sql="  SELECT  to_char(data_inizio,'DD/MM/YYYY HH24:MI:SS') dataInizio FROM GDM_ELAB_BATCH WHERE UTENTE=:UTENTE AND TIPO_ELABORAZIONE=:TIPOELAB "
					   +"  AND ID_ELABORAZIONE=:IDELAB";
					
				    logger.info("getDataInizioElaborazione:: SQL:"+sql);
				    dbOp.setStatement(sql);
				    dbOp.setParameter(":UTENTE", user);
				    dbOp.setParameter(":TIPOELAB", tipoElaborazione);
				    dbOp.setParameter(":IDELAB", this.idElaborazione);
				    
				    dbOp.execute();			    
				    rs=dbOp.getRstSet();
				    if(rs.next()) {
				    	data = rs.getString("dataInizio");
				    }
				   
					try{dbOp.close();}catch (Exception ei) {}
			    }
				catch (Exception e) {
					try{dbOp.close();}catch (Exception ei) {}
					throw new Exception("Errore nel tentativo di recupero della data di una elaborazione di tipo "+tipoElaborazione+" per l'utente "+user+". SQL:"+sql+"\nErrore="+e.getMessage());
				}		
			}
			logger.info("AbstrElaborazioneBatch::getDataInizioElaborazione - Recupero dataInizio:"+data);
		}
		catch (Exception e) {
			throw new Exception("AbstrElaborazioneBatch::getDataInizioElaborazione - Errore: "+e.getMessage());
		}
		
		return data;	
	}
	
	
	public void setStatoElaborazione(String stato)throws Exception{
		
		String idDocumento="";	
		
		try {
			logger.info("setStatoElaborazione::Recupero idDocumento");			
			idDocumento = getIdDocumentoElaborazione();
			Profilo p = new Profilo(idDocumento);
			
			if (cn==null)
				p.initVarEnv(user, "", new JNDIParameter(jndi));
			else
				p.initVarEnv(user, "", cn);
			logger.info("Accedi al profilo con idDocumento:"+idDocumento);
			
			p.settaValore("STATO_ELABORAZIONE", stato);
				
			if(stato.equals(_STATO_ELAB_FINITA) || stato.equals(_STATO_ELAB_ERRORE))
				p.settaValore("DATA_FINE", getNow());
			
			if (!(p.salva().booleanValue()))  throw new Exception("Errore in registrazione documento profilo: "+p.getError());							
			logger.info("Salvataggio dello stato");
			try{if(cn!=null) cn.commit();}catch (Exception ei) {}
		}
		catch (Exception e) {
			try{if(cn!=null) cn.rollback();}catch (Exception ei) {}
			throw new Exception("AbstrElaborazioneBatch::setStatoElaborazione - Errore: "+e.getMessage());
		}				
	}
	
	public String getStatoElaborazione()throws Exception {
		String stato="";
		ResultSet rs = null;
		String sql="";
		
		try {
			IDbOperationSQL dbOp=getConn();
		
			try {		    	
			    sql="  SELECT * FROM GDM_ELAB_BATCH WHERE UTENTE=:UTENTE AND TIPO_ELABORAZIONE=:TIPOELAB "
				   +"  AND ID_ELABORAZIONE=:IDELAB";
				
			    logger.info("getStatoElaborazione:: SQL:"+sql);
			    dbOp.setStatement(sql);
			    dbOp.setParameter(":UTENTE", user);
			    dbOp.setParameter(":TIPOELAB", tipoElaborazione);
			    dbOp.setParameter(":IDELAB", this.idElaborazione);
			    
			    dbOp.execute();			    
			    rs=dbOp.getRstSet();
			    if(rs.next()) {
			    	stato = rs.getString("STATO_ELABORAZIONE");
			    	this.nomeElaborazione = rs.getString("DESCRIZIONE");
			    }
			   
				try{dbOp.close();}catch (Exception ei) {}
		    }
			catch (Exception e) {
				try{dbOp.close();}catch (Exception ei) {}
				throw new Exception("Errore nel tentativo di recupero lo stato di una elaborazione di tipo "+tipoElaborazione+" per l'utente "+user+". SQL:"+sql+"\nErrore="+e.getMessage());
			}										
			
			if(stato!=null && stato.equals(""))
				stato =_STATO_ELAB_INIZIATA;
			logger.info("AbstrElaborazioneBatch::getStatoElaborazione - Recupero stato:"+stato);
		}
		catch (Exception e) {
			throw new Exception("AbstrElaborazioneBatch::getStatoElaborazione - Errore: "+e.getMessage());
		}
		
		return stato;	
	}
	
	
	
	public void startElaborazione()throws Exception{
		creaElaborazione();
	}
	
	public void startElaborazione(String idElaborazione)throws Exception{
		
		creaElaborazione(idElaborazione);
	}
	
	public InputStream getFileElaborazione()throws Exception{
		InputStream is=null;
		String idDocumento="";	
		
		try {
				
			idDocumento = getIdDocumentoElaborazione();
			Profilo p = new Profilo(idDocumento);
			
			if (cn==null)
				p.initVarEnv(user, "", new JNDIParameter(jndi));
			else
				p.initVarEnv(user, "", cn);
			
			if (p.accedi().booleanValue()) {
				is = p.getFileStream(1);
			}			
			
		}
		catch (Exception e) {
			throw new Exception("AbstrElaborazioneBatch::getFileElaborazione - Errore: "+e.getMessage());
		}		
		
		return is;
	}
	
	public void setFileElaborazione(String nomefile, InputStream is) throws Exception{
		
		String idDocumento="";	
		
		try {
				
			idDocumento = getIdDocumentoElaborazione();
			Profilo p = new Profilo(idDocumento);
			
			if (cn==null)
				p.initVarEnv(user, "", new JNDIParameter(jndi));
			else
				p.initVarEnv(user, "", cn);
			
			p.setFileName(nomefile, is);
			
			if (!(p.salva().booleanValue()))  throw new Exception("Errore in registrazione documento profilo: "+p.getError());							
		  	
			try{if(cn!=null) cn.commit();}catch (Exception ei) {}
		}
		catch (Exception e) {
			try{if(cn!=null) cn.rollback();}catch (Exception ei) {}
			throw new Exception("AbstrElaborazioneBatch::setFileElaborazione - Errore: "+e.getMessage());
		}		
	}
	
	public String getErroreElaborazione()throws Exception{
		
		String msg="";
		String idDocumento="";
		
		try {
			logger.info("getErroreElaborazione::Recupero idDocumento");		
			idDocumento = getIdDocumentoElaborazione();
			Profilo p = new Profilo(idDocumento);
			
			if (cn==null)
				p.initVarEnv(user, "", new JNDIParameter(jndi));
			else
				p.initVarEnv(user, "", cn);

   			logger.info("Accedi al profilo con idDocumento:"+idDocumento);
   			if (p.accedi().booleanValue()) {			
				msg = p.getCampo("ERRORE_ELABORAZIONE");
			}	
   			logger.info("Messaggio di errore:"+msg);   			
		}
		catch (Exception e) {
			throw new Exception("AbstrElaborazioneBatch::getErroreElaborazione - Errore: "+e.getMessage());
		}					
		return msg;	
	}
	
	public void setErroreElaborazione(String idElab, String msg) throws Exception {
		
		ResultSet rs = null;
		String sql="";
		String idDocumento="";
		
		try {
			    logger.info("setErroreElaborazione::Recupero idDocumento per id_eleaborazione:"+idElab);		
				IDbOperationSQL dbOp = getConn();
		
				try {		    	
				    sql="SELECT ID_DOCUMENTO FROM GDM_ELAB_BATCH WHERE ID_ELABORAZIONE=:IDELAB ";			    
				    dbOp.setStatement(sql);
				    dbOp.setParameter(":IDELAB", idElaborazione);			    
				    dbOp.execute();
				    
				    rs=dbOp.getRstSet();
				    if(rs.next()) {
				    	idDocumento = rs.getString("ID_DOCUMENTO");
				    }	
				    
				    logger.info("idDocumento:"+idDocumento);		
					
					try{dbOp.close();}catch (Exception ei) {}
			    }
				catch (Exception e) {
					try{dbOp.close();}catch (Exception ei) {}
					throw new Exception("Errore nel tentativo di recupero di una elaborazione attiva di tipo "+tipoElaborazione+" per l'utente "+user+". SQL:"+sql+"\nErrore="+e.getMessage());
				}										
		
				Profilo p = new Profilo(idDocumento);
				
				if (cn==null)
					p.initVarEnv(user, "", new JNDIParameter(jndi));
				else
					p.initVarEnv(user, "", cn);
				
				logger.info("Accedi al profilo con idDocumento:"+idDocumento);
				p.settaValore("ERRORE_ELABORAZIONE", msg);					
							
				if (!(p.salva().booleanValue()))  throw new Exception("Errore in registrazione documento profilo: "+p.getError());							
			  	
				try{if(cn!=null) cn.commit();}catch (Exception ei) {}
				logger.info("Salvataggio del messaggio di errore");
		  }
		  catch (Exception e) {
			  throw new Exception("AbstrElaborazioneBatch::setErroreElaborazione - Errore: "+e.getMessage());
		  }		
	}
	    
    public String getUrlFileElaborazione() throws Exception{
    	
    	   String url_file="";
    	   String idDocumento="";
   		
	   		try {
	   			logger.info("getUrlFileElaborazione::Recupero idDocumento");		
	   			idDocumento = getIdDocumentoElaborazione();
	   			Profilo p = new Profilo(idDocumento);
	   			
	   			if (cn==null)
	   				p.initVarEnv(user, "", new JNDIParameter(jndi));
	   			else
	   				p.initVarEnv(user, "", cn);
	   			
	   			logger.info("Accedi al profilo con idDocumento:"+idDocumento);
	   			if (p.accedi().booleanValue()) {	   			
	   				Vector<String> va = p.getlistaIdOggettiFile();	   				
	   				if(va.size()==0)
	   					throw new Exception("Nessun file elaborato.");
	   				else{
	   					url_file ="./common/ServletVisualizza.do?ar="+p.getArea()+"&amp;cm="+p.getCodiceModello()+"&amp;cr="+p.getCodiceRichiesta();
	   			   		url_file+="&amp;ca="+va.get(0)+"&amp;iddoc="+idDocumento+"&amp;firma=";
	   				}
	   			}		
	   			logger.info("Url del file elaborato:"+url_file);
	   		}
	   		catch (Exception e) {
	   			throw new Exception("AbstrElaborazioneBatch::getUrlFileElaborazione - Errore: "+e.getMessage());
	   		}			
   		
    	   return url_file;    	
    }
			
	public String getIdElaborazione() {
		return idElaborazione;
	}
	
	public void setIdElaborazione(String id) {
		idElaborazione=id;
	}
	
	public void terminaElaborazione(InputStream is)throws Exception {}

		
	private String getIdDocumentoElaborazione() throws Exception {
		
		ResultSet rs = null;
		String sql="";
		String idDocumento="";		
		boolean bEsisteElabAttiva = false;
		
		try {
			IDbOperationSQL dbOp=getConn();
		
			try {		    	
			    sql="SELECT ID_DOCUMENTO FROM GDM_ELAB_BATCH "+ 
						   "WHERE UTENTE=:UTENTE "+
						   "AND TIPO_ELABORAZIONE=:TIPOELAB ";
			    
			    if(idElaborazione!=null && !idElaborazione.equals(""))
			    	sql+= "AND ID_ELABORAZIONE=:IDELAB ";
			    
			    logger.info("getIdDocumentoElaborazione:: SQL:"+sql);
			    dbOp.setStatement(sql);
			    dbOp.setParameter(":UTENTE", user);
			    dbOp.setParameter(":TIPOELAB", tipoElaborazione);
			    
			    if(idElaborazione!=null && !idElaborazione.equals(""))
			       dbOp.setParameter(":IDELAB", idElaborazione);
			    
			    dbOp.execute();			    
			    rs=dbOp.getRstSet();
			    if(rs.next()) {
			    	idDocumento = rs.getString("ID_DOCUMENTO");
			    	bEsisteElabAttiva = true;
			    }	
			    else
			    	bEsisteElabAttiva = false;	
			    
			    logger.info("idDocumento:"+idDocumento);
			    
				if (!bEsisteElabAttiva) throw new Exception("Attenzione! Non esiste un elaborazione attiva di tipo "+tipoElaborazione+" per l'utente "+user);
				
				try{dbOp.close();}catch (Exception ei) {}
		    }
			catch (Exception e) {
				try{dbOp.close();}catch (Exception ei) {}
				throw new Exception("Errore nel tentativo di recupero di una elaborazione attiva di tipo "+tipoElaborazione+" per l'utente "+user+". SQL:"+sql+"\nErrore="+e.getMessage());
			}										
		}
		catch (Exception e) {
			throw new Exception("AbstrElaborazioneBatch::getIdDocumentoElaborazione - Errore: "+e.getMessage());
		}	
		return idDocumento;
	}
		
	private String getNextIdElaborazione() throws Exception {
		String sNext="";
		
		IDbOperationSQL dbOp=null;   
		try {
			dbOp=getConn();
			
		    sNext=""+dbOp.getNextKeyFromSequence("ELAB_BATCH_SQ");
		    
			try{dbOp.commit();}catch (Exception ei) {}
			
			return sNext;
	    }
		catch (Exception e) {
			try{dbOp.close();}catch (Exception ei) {}
			throw new Exception("AbstrElaborazioneBatch::getNextIdElaborazione .\nErrore="+e.getMessage());
		}		
	}
	
	private java.sql.Date getNow()  {
			Calendar cal = Calendar.getInstance();
			java.sql.Timestamp now =
           new java.sql.Timestamp(cal.getTimeInMillis());
			java.sql.Date jsqlD = new java.sql.Date(now.getTime());
			
			return jsqlD;
	}
	private IDbOperationSQL getConn() throws Exception {
		 return (cn==null)?(SessioneDb.getInstance().createIDbOperationSQL(jndi,0)):
			 			   (SessioneDb.getInstance().createIDbOperationSQL(cn,0));
	}
	
}

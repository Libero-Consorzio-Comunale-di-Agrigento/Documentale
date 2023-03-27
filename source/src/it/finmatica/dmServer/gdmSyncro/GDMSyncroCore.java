package it.finmatica.dmServer.gdmSyncro;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.Struct;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.gdmSyncro.struct.Servizi;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class GDMSyncroCore {
	private Environment newEn;
	IDbOperationSQL  dbOp;
	private boolean bIsNew=false;
	private String idDocumento, idOggettoFile, idSyncro, idServizio;	
	private Servizi infoServizio;	
	private boolean onInsertErrorContinue=false;
	private String lastError=null;



	public GDMSyncroCore(String idServizio, String idDocumento, String idOggettoFile, Environment newEn) throws Exception {
		this.newEn=newEn;		
		this.idDocumento=idDocumento;
		this.idOggettoFile=idOggettoFile;
		this.idSyncro=null;
		this.idServizio=idServizio;	
		init(this.idServizio);	
		//System.out.println(infoServizio);
		
		//infoServizio.getInfoServizioParametri();
		
		 
	}
	
	public GDMSyncroCore(String idServizio, String idSyncro, String idDocumento, String idOggettoFile,Environment newEn) throws Exception {
		this.newEn=newEn;		
		this.idDocumento=idDocumento;
		this.idOggettoFile=idOggettoFile;
		this.idSyncro=idSyncro;
		this.idServizio=idServizio;
		init(this.idServizio);
	}
	

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}	
	
	public boolean isOnInsertErrorContinue() {
		return onInsertErrorContinue;
	}

	public void setOnInsertErrorContinue(boolean onInsertErrorContinue) {
		this.onInsertErrorContinue = onInsertErrorContinue;
	}	
	
	public boolean esisteServizio() {
		return (infoServizio==null)?false:true;
	}
	
	public String getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(String idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getIdOggettoFile() {
		return idOggettoFile;
	}

	public void setIdOggettoFile(String idOggettoFile) {
		this.idOggettoFile = idOggettoFile;
	}
		
	public String syncro(InputStream is)  throws Exception {
		Class classe =null;
		String idDocumentoRemoto="";
		
		lastError=null;
		try {
			try {
				 classe = Class.forName(infoServizio.getClasseImplementazione());			 			
			} catch (Exception e) {
				throw new Exception("Errore in inzializzazione classe di implementazione ("+
									infoServizio.getClasseImplementazione()+".\nErrore= "+e.getMessage());
			}
			
			dbOp = connect();
			
			if (this.idSyncro!=null) {
				
				//Sono in update
				
				//1. uso la GET_ID_DOCUMENTO_REMOTO per sapere l'idremoto....anche se dovrebbe essere lo stesso visto che sono in update
				idDocumentoRemoto=getIdRemoto(this.idSyncro,dbOp);
				
				/*if (idDocumentoRemoto.equals(""))
					throw new Exception("Attenzione! non esiste alcun documento remoto associato all'idsyncro="+this.idSyncro);*/
				
				//2. richiamo il metodo di syncro remoto passando anche l'idremoto così lui da che 
				//   è una update. il metodo si deve chiamare syncro e la classe deve implementale la ISyncro
				Object o = classe.newInstance();
				Method  method = classe.getDeclaredMethod ("syncro", Servizi.class,InputStream.class, String.class );
				boolean bErrore=false;
				try {
					idDocumentoRemoto = (String)method.invoke(o, this.infoServizio,is,idDocumentoRemoto );
				}								
				catch (InvocationTargetException e) {
					// 3. registro con la REGISTRA_DOCUMENTO_REMOTO l'event log ERRORE
					String errore=e.getTargetException().getMessage();
					lastError=errore;
					try {
						registraDocumentoRemoto(this.idSyncro,  idDocumento,  idOggettoFile,
											    this.infoServizio.getNomeServizio(),"",
											    idDocumentoRemoto, errore,dbOp);						
					}
					catch (Exception ei) {						
						errore=ei.getMessage()+"\n"+errore;
					}
					
					if (onInsertErrorContinue==false) 						
						throw new Exception(errore);
					
					else
						bErrore=true;
				}
				
				if (bErrore==false) {
				// 3. registro con la REGISTRA_DOCUMENTO_REMOTO l'event log  OK
					try {
						if (is==null) {
							registraDocumentoRemoto(this.idSyncro,  idDocumento,  idOggettoFile,
												    this.infoServizio.getNomeServizio(),"",
												    idDocumentoRemoto, "CANCELLATO",dbOp);
						}
						else {
							registraDocumentoRemoto(this.idSyncro,  idDocumento,  idOggettoFile,
												    this.infoServizio.getNomeServizio(),"",
												    idDocumentoRemoto, "OK",dbOp);
						}
					}
					catch (Exception ei) {						
						//dontcare
					}
				}
			}
			else {
				//Sono in insert
				
				//1. richiamo il metodo di syncro remoto passando l'idremoto null così lui da che 
				//   è una insert
				Object o = classe.newInstance();
				Method  method = classe.getDeclaredMethod ("syncro", Servizi.class,InputStream.class, String.class );
				String action="OK";
				try {
					idDocumentoRemoto = (String)method.invoke(o, this.infoServizio,is,null );									
				}								
				catch (InvocationTargetException e) {	
					if (onInsertErrorContinue) {
						action="Errore: "+e.getTargetException().getMessage();
						idDocumentoRemoto="null";
						lastError=e.getTargetException().getMessage();
					}						
					else
						throw new Exception(e.getTargetException().getMessage());
				}
				
				//2. registro con la REGISTRA_DOCUMENTO_REMOTO tutte le chiavi 			
				try {
					this.idSyncro=registraDocumentoRemoto(null,  idDocumento,  idOggettoFile,
													      this.infoServizio.getNomeServizio(),"",
													      idDocumentoRemoto, action,dbOp);
				}
				catch (Exception ei) {						
					throw new Exception(ei.getMessage());
				}							
			}
						
			close();
			return this.idSyncro;
			
		} catch (Exception e) {			
			close();
			throw new Exception("GDMSyncroCore::syncro - \nErrore= "+e.getMessage());
		}				
		
	}
	
	public InputStream download()  throws Exception {
		Class classe =null;
		InputStream is=null;
		String idDocumentoRemoto;
		
		if (this.idSyncro==null) return null;
		
		try {
			try {
				 classe = Class.forName(infoServizio.getClasseImplementazione());			 			
			} catch (Exception e) {
				throw new Exception("Errore in inzializzazione classe di implementazione ("+
									infoServizio.getClasseImplementazione()+".\nErrore= "+e.getMessage());
			}			
			
			dbOp = connect();
			idDocumentoRemoto=getIdRemoto(this.idSyncro,dbOp);
			close();
			
			Object o = classe.newInstance();
			Method  method = classe.getDeclaredMethod ("download",Servizi.class,String.class );
			try {
				is = (InputStream)method.invoke(o,  this.infoServizio,idDocumentoRemoto );
			}								
			catch (InvocationTargetException e) {
				
				String errore=e.getTargetException().getMessage();												
				throw new Exception(errore);
			}
			
			return is;
		} catch (Exception e) {		
			//e.printStackTrace();
			close();
			throw new Exception("GDMSyncroCore::download - \nErrore= "+e.getMessage());
		}
	}
	
	
	
	
	
	
	
	private void init(String idServ) throws Exception {
		infoServizio = new Servizi();
		
		try {
			dbOp = connect();
			
			StringBuffer sStm = new StringBuffer();
			sStm.append("select INTEGRAZIONE_GDM_GDMSYNCRO.GET_INFORMAZIONI_SERVIZIO("+idServ+") record from dual");
			
			dbOp.setStatement(sStm.toString());
			dbOp.setOutParameter("record",  Types.ARRAY);
			dbOp.execute();
			ResultSet rst = dbOp.getRstSet();
			rst.next();
			Object[] data = (Object[]) ((Array) rst.getObject(1)).getArray();
			
			if (data.length==0)
				throw new Exception("Servizio con id "+idServ+" inesistente su GDMSYNCRO" ); 
			
			for (int i=0;i<data.length;i++ ) {
				Struct row = (Struct) data[i];
				String nomePar =null, valorePar="";
				int numColonna=0;
				for(Object attribute : row.getAttributes()) {
					if (i==0 && numColonna==0) 
						infoServizio.setNomeServizio(""+attribute);
					else if (i==0 && numColonna==1) 
						infoServizio.setClasseImplementazione(""+attribute);
					else if (numColonna==2)   {
						nomePar=""+attribute;
					}
					else if (numColonna==3)   {
						valorePar=bindValoriParametri(""+attribute);
					}
										
					numColonna++;
				}
				
				if (nomePar!=null) infoServizio.addParametro(nomePar,valorePar);
			}
			
			//Ciclo sui parametri per fare il binging dinamico su eventuali funzione per estrarre i relativi valori
			for (Map.Entry<String, String> entry : infoServizio.getInfoServizioParametri().entrySet()){
				String valore=entry.getValue();
				
				if (valore.startsWith("@FUNCTION=")) {					
					valore=valore.substring(valore.indexOf("@FUNCTION=")+"@FUNCTION=".length());
					
					valore=getValoreParametroDaFunzione(valore);
					infoServizio.getInfoServizioParametri().put(entry.getKey(), valore);
				}
			}										
			
			close();
		}
		catch (Exception e) {
			e.printStackTrace();
            close();
            throw new Exception("GDMSyncroCore::init\n" + e.getMessage());                    
        }
	}
	
	private String bindValoriParametri(String valore) {
		if (this.idDocumento!=null)
			valore=valore.replaceAll(":IDDOCUMENTO", this.idDocumento);
		else
			valore=valore.replaceAll(":IDDOCUMENTO", "-100");
		
		if (this.idOggettoFile!=null)  
			valore=valore.replaceAll(":IDOGGETTOFILE", this.idOggettoFile);
		else
			valore=valore.replaceAll(":IDOGGETTOFILE", "-100");
		
		return valore;
	}
	
	private String getValoreParametroDaFunzione(String func) throws Exception {
		String ret="";
		
		try {
			dbOp = connect();
			
			dbOp.setCallFunc(func);			
			dbOp.execute();
			
			ret=dbOp.getCallSql().getString(1);	
			
			close();
			return ret;
		}
		catch (Exception e) {
            close();
            throw new Exception("GDMSyncroCore::getValoreParametroDaFunzione("+func+")\n" + e.getMessage());                    
        }
		
		
	}
	
	private String getIdRemoto(String idSyncro,IDbOperationSQL dbOperation ) throws Exception {
		String idRemoto="";
		
		StringBuffer sStm = new StringBuffer();
		sStm.append("select INTEGRAZIONE_GDM_GDMSYNCRO.GET_ID_DOCUMENTO_REMOTO("+idSyncro+") iddocremoto from dual");
		
		try {			
			
			dbOperation.setStatement(sStm.toString());			
			dbOperation.execute();
			ResultSet rst = dbOp.getRstSet();
			rst.next();
			
			idRemoto=rst.getString(1);			
			
		}
		catch (Exception e) {
            close();
            throw new Exception("GDMSyncroCore::getIdRemoto("+idSyncro+")\n" + e.getMessage());                    
        }
		
		return idRemoto;
	}
	
	private String registraDocumentoRemoto(String idSyncro, String idDocumento, String idOggettoFile,
										 String applicativo,String ente,
										 String idDocumentoRemoto, String reportAzione,
										 IDbOperationSQL dbOperation) throws Exception {
		String idDocumentoRemotoRet="";
		StringBuffer sStm = new StringBuffer();
		sStm.append("INTEGRAZIONE_GDM_GDMSYNCRO.REGISTRA_DOCUMENTO_REMOTO("+
					idSyncro+","+idDocumento+","+idOggettoFile+",'"+applicativo+"','"+ente+"'," +
					idDocumentoRemoto+",'"+reportAzione.replaceAll("'", "''")+"')");
		
		try {
			//dbOperation = connect();
			dbOperation.setCallFunc(sStm.toString());
			dbOperation.execute();
			
			idDocumentoRemotoRet = dbOperation.getCallSql().getString(1);
		}
		catch (Exception e) {      
			e.printStackTrace();
            throw new Exception("GDMSyncroCore::registraDocumentoRemoto "+sStm.toString()+"\n" + e.getMessage());                    
        }
		
		return idDocumentoRemotoRet;
	}	
	
	private IDbOperationSQL connect() throws Exception {
        if (newEn.getDbOp()==null) {
           bIsNew=true;
           return (new ManageConnection(newEn.Global)).connectToDB();
        }
        
        return newEn.getDbOp();
    }
  
    private void close() throws Exception {
        if (bIsNew) (new ManageConnection(newEn.Global)).disconnectFromDB(dbOp,true,false);        
    }
	
}

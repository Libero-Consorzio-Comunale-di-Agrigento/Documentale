package it.finmatica.dmServer.management;

import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

import it.finmatica.dmServer.A_Oggetti_File;
import it.finmatica.dmServer.GD4_Oggetti_File;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;

public class ProfiloVersion extends ProfiloLog {
	   private long lVersione;   	  
	   private long idLog;
	   private boolean bSoloModifiche=false;
	   private Date dataAggiornamentoLog=null;
	  



	   public ProfiloVersion(String idProfilo) {
		   super(idProfilo);	 
	   }
	   	   	
	   /**
	    * Costruttore da utilizzare in fase di
	    * accesso di un documento versionato conoscendone la
	    * chiave primaria e il numero di versione
	    *
	    * @param idProfilo identificativo del doc versionato da accedere
	   */
	   public ProfiloVersion(String idProfilo, long lVersione) {
	          super(idProfilo);	 
	          
	          this.lVersione=lVersione;
	   }  
	   	 
	   
	   public void versiona() throws Exception {
		      AggiornaDocumento ad;
      
              ad = new AggiornaDocumento(idDocumento, en );
              
              ad.versionaDocumento(lVersione,bSoloModifiche,dataAggiornamentoLog);  
              
              idLog=Long.parseLong(""+ad.getIdLog());
	   }
	   
	   public long getIdLog() {
			  return idLog;
	   }		   	   
	   
	   public void setIdLog(long idLog) {
		   	  this.idLog = idLog;
	   }

	   public void setSoloModifiche(boolean bSoloModifiche) {
			this.bSoloModifiche = bSoloModifiche;
	   }

	   public Boolean accedi() {
		   	  try {
	             ad = new AccediDocumento(idDocumento,en);
	             
	             String idLog=null;
	             
	             if (this.idLog==0) {
		             try {
		            	idLog=(new LookUpDMTable(en)).lookUpIdLogFromVersion(idDocumento,lVersione+"");
		            	
		            	if (idLog==null) {
				             error="Versione "+lVersione+" inesistente o non corretta per il documento "+idDocumento;
				             return new Boolean(false);
				         }
		             }
			         catch (Exception e) {
			             error="Errore nel recupero dell'idLog dalla versione"+e.getMessage();
			             return new Boolean(false);
			         }
	             }
	             else {
	            	 idLog=""+this.idLog;
	             }	            	 	             
		         
	             ad.accediLogDocumento(idLog);
	                                       
	             if (ad.aDocumento!=null) {
	                cr=ad.aDocumento.getCodiceRichiesta();   
	                area=(new DocUtil(en)).getAreaByIdDocumento(""+idDocumento);
	                tipoDocumento=(new DocUtil(en)).getModelloByIdDocumento(""+idDocumento);
	             }
	             
	             this.idLog=Long.parseLong(idLog);
	             
	             return new Boolean(true);
	          }
	          catch (Exception e) {
	             error="ProfiloVersion::accedi()\n"+e.getMessage();
	             return new Boolean(false);
	          }
	   }
	   
	   public Vector<GD4_Oggetti_File> getListaFile()  {
		      return ad.listaOggettiFile();
	   }
	   
	   public void setDataAggiornamentoLog(Date dataAggiornamentoLog) {
			this.dataAggiornamentoLog = dataAggiornamentoLog;
	   }

	public String getFileHash(String nomeFile) throws Exception {
		return getFileHash(nomeFile,null);
	}

	public String getFileHash(Long idOggettoFile) throws Exception {
		return getFileHash(null,idOggettoFile);
	}


	/**
	 * Metodo che restituisce l'hash dell'allegato del profilo
	 * dato il nome del file
	 * Override della funzione presente nella ProfiloLog e che gestisce anche
	 * la possibilità che per la versione richiesta non sia presente il file
	 * perché magari ho chiamato la versione con la setSoloModifiche
	 *
	 * @param nomeFile nome dell'allegato
	 * @return InputStream dell'allegato
	 * @see <a href="Profilo.html#getFileStream(int)">getFileStream(index)</a>
	 */
	public String getFileHash(String nomeFile, Long idOggettoFile) throws Exception {
		String idLogVersioneAttuale=null;

		if (this.idLog==0) {
			try {
				idLogVersioneAttuale=(new LookUpDMTable(en)).lookUpIdLogFromVersion(idDocumento,lVersione+"");

				if (idLogVersioneAttuale==null) {
					error="Versione "+lVersione+" inesistente o non corretta per il documento "+idDocumento;
					throw new Exception(error);
				}
			}
			catch (Exception e) {
				error="Errore nel recupero dell'idLog dalla versione"+e.getMessage();
				throw new Exception(error);
			}
		}
		else {
			idLogVersioneAttuale=""+this.idLog;
		}

		String hash=null;
		//1. provo a tirarmi l'impronta dall'oggetti_file_log con l'idlog effettivo
		if (this.idLog==0) hash=(new LookUpDMTable(en)).getHashLog(nomeFile,idDocumento,idLogVersioneAttuale,idOggettoFile);

		if (hash==null && (lVersione!=0 || this.idLog!=0)) {
			//Se ho specificato la versione non l'ho trovato, non esco con un eccezione,
			// ma lo cerco nella prima revisione possibile piena

			boolean bCasoLog=false;
			if (this.idLog!=0) bCasoLog=true;

			String idLogVersioneMinimaFile=(new LookUpDMTable(en)).lookUpMinIdLogOggettoLogFromVersion(idDocumento,idLogVersioneAttuale+"", nomeFile,idOggettoFile,bCasoLog);
			if (idLogVersioneMinimaFile==null && !bCasoLog) {
				return null;
			}

			if (Global.nvl(idLogVersioneMinimaFile,"X").equals("0") && bCasoLog) {
				return null;
			}

			if (idLogVersioneMinimaFile==null && bCasoLog)
				hash=(new LookUpDMTable(en)).getHashLog(nomeFile,idDocumento,null,idOggettoFile);
			else
				hash=(new LookUpDMTable(en)).getHashLog(nomeFile,idDocumento,idLogVersioneMinimaFile,idOggettoFile);

		}


		return hash;
	}

	public InputStream getFileStream(String nomeFile) throws Exception {
		return getFileStream(nomeFile,null);
	}

	public InputStream getFileStream(Long id) throws Exception {
		return getFileStream(null,id);
	}


	/**
	 * Metodo che restituisce l'InputStream dell'allegato del profilo
	 * dato il nome del file
	 * Override della funzione presente nella ProfiloLog e che gestisce anche
	 * la possibilità che per la versione richiesta non sia presente il file
	 * perché magari ho chiamato la versione con la setSoloModifiche
	 *
	 * @param nomeFile nome dell'allegato
	 * @return InputStream dell'allegato
	 * @see <a href="Profilo.html#getFileStream(int)">getFileStream(index)</a>
	 */
	public InputStream getFileStream(String nomeFile, Long idOggettoFile) throws Exception {
		//Per prima cosa cerco di tirare giù l'allegato dalla profiloLog
		InputStream is=null;
		boolean nonTrovatoFile=false;
		if (this.idLog==0) {
			try {
				if (idOggettoFile==null) {
					is=super.getFileStream(nomeFile);
				}
				else {
					is=super.getFileStream(idOggettoFile);
				}
				return is;
			}
			catch (Exception e) {
				//Se ho specificato la versione non l'ho trovato, non esco con un eccezione,
				// ma lo cerco nella prima revisione possibile piena
				if (e.getMessage().indexOf("Non trovato il file")!=-1 && (lVersione!=0 || this.idLog!=0) )
					nonTrovatoFile=true;
				else
					throw e;
			}
		}
		else
			nonTrovatoFile=true;

		if (nonTrovatoFile) {
			//lo cerco nella prima revisione possibile piena
			String idLogVersioneAttuale;
			boolean bCasoLog=false;
			String errMsg;
			if (this.idLog==0) {
				idLogVersioneAttuale=(new LookUpDMTable(en)).lookUpIdLogFromVersion(idDocumento,lVersione+"");
				if (idOggettoFile==null)
					errMsg="File "+nomeFile+" non trovato per versione "+lVersione+" per il documento "+idDocumento;
				else
					errMsg="File con id "+idOggettoFile+" non trovato per versione "+lVersione+" per il documento "+idDocumento;
			}
			else  {
				idLogVersioneAttuale=""+this.idLog;
				if (lVersione==0) bCasoLog=true;
				if (idOggettoFile==null)
					errMsg="File "+nomeFile+" non trovato per idLog "+idLog+" per il documento "+idDocumento;
				else
					errMsg="File con id "+nomeFile+" non trovato per idLog "+idLog+" per il documento "+idDocumento;
			}

			if (idLogVersioneAttuale==null) {
				throw new Exception("Versione "+lVersione+" inesistente o non corretta per il documento "+idDocumento);
			}

			String idLogVersioneMinimaFile=(new LookUpDMTable(en)).lookUpMinIdLogOggettoLogFromVersion(idDocumento,idLogVersioneAttuale+"", nomeFile,idOggettoFile,bCasoLog);
			if (idLogVersioneMinimaFile==null && !bCasoLog) {
				throw new Exception(errMsg);
			}

			if (Global.nvl(idLogVersioneMinimaFile,"X").equals("0") && bCasoLog) {
				if (idOggettoFile==null)
					throw new Exception("File "+nomeFile+" per Versione "+idLogVersioneAttuale+" inesistente per il documento "+idDocumento);
				else
					throw new Exception("File con id "+idOggettoFile+" per Versione "+idLogVersioneAttuale+" inesistente per il documento "+idDocumento);
			}

			//Accedo alla versione trovata ed accedo al file
			AccediDocumento adLocale = new AccediDocumento(idDocumento,en);
			//Se non trovo il log nel caso NON REVISIONE, allora devo andare sulla oggetti file
			if (idLogVersioneMinimaFile==null && bCasoLog)
				adLocale.accediFullDocumento();
			else
				adLocale.accediLogDocumento(idLogVersioneMinimaFile);

			if (adLocale.aDocumento!=null) {
				int size = adLocale.listaOggettiFile().size();

				for(int i=0;i<size;i++) {
					if (idOggettoFile==null) {
						String nome=((A_Oggetti_File)adLocale.listaOggettiFile().elementAt(i)).getFileName();

						if (nome.toUpperCase().equals(nomeFile.toUpperCase())) {
							return (InputStream)((A_Oggetti_File)adLocale.listaOggettiFile().elementAt(i)).getFile();
						}
					}
					else {
						String idFile=((A_Oggetti_File)adLocale.listaOggettiFile().elementAt(i)).getIdOggettoFile();

						if (idFile.toUpperCase().equals(idOggettoFile.toString())) {
							return (InputStream)((A_Oggetti_File)adLocale.listaOggettiFile().elementAt(i)).getFile();
						}
					}
				}

				throw new Exception(errMsg);
			}
		}

		return null;
	}
}

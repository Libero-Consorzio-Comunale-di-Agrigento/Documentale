package it.finmatica.dmServer.Impronta;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.GD4_Oggetti_File_Log;
import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.motoreRicerca.ResultSetIQuery;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author mbonforte
 *
 */
public class ImprontaAllegati {
	private static final String ALGORTITHM = "SHA-512";
	private MessageDigest md = null;
	private Profilo p = null;
	
	private String idDocumento;
	private Connection cn;	
	private IDbOperationSQL dbOpGlobal;

	private String listaFile;
	private ListaImpronte limp = new ListaImpronte();
	private Hashtable<String,InputStream> hIs = null;
	private LinkedList<String> iddocFigli = new LinkedList<String>();
	private String gestisciImprontePar="N";
	
	public ImprontaAllegati() throws Exception {
		try {
			md = MessageDigest.getInstance(ALGORTITHM);
		} catch (Exception e) {
			throw new Exception("ImprontaAllegati()\n"+e.getMessage());    
		}
	}
	
	public ImprontaAllegati (String idDocument, IDbOperationSQL dbOp) throws Exception {
		this.idDocumento=idDocument;
		this.dbOpGlobal=dbOp;	
	}
	
	public ImprontaAllegati (String idDocument, IDbOperationSQL dbOp, String listaFile) throws Exception {
		   this(idDocument,dbOp,listaFile,true);
	}
	
	public ImprontaAllegati (String idDocument, IDbOperationSQL dbOp, String listaFile, boolean bCarica) throws Exception {
		   this();
		   
		   try {			
			 idDocumento=idDocument;
			 this.dbOpGlobal=dbOp;
			 this.listaFile=listaFile;
			 			
			 hIs = new Hashtable<String,InputStream>();
			 if (bCarica) caricaImpronteDB();
			 
		   } catch (Exception e) {
    	     throw new Exception("ImprontaAllegati(Profilo profilo)\n"+e.getMessage());    
		   }
	}

	public ImprontaAllegati (Profilo profilo) throws Exception {
		   this();
		   
		   try {			
			 p = profilo;			
			 if (p.accedi().booleanValue()) {
				 idDocumento=p.getDocNumber();
				 listaFile=p.getlistaFile("@_#_@");
				 
				 cn=p.getCn();
				 
				 
				 caricaImpronteDB();
			 } 
			 else {
				 throw new Exception("Documento non presente.");
			 }
		   } catch (Exception e) {
    	     throw new Exception("ImprontaAllegati(Profilo profilo)\n"+e.getMessage());
           }
	}
		
	public void sistemaImpronte() throws Exception {
		 //Ciclo su tutti gli idlog della ogfilog e mi calcolo l'impronta
	      try {
	    	  GD4_Oggetti_File_Log gd4Ogfi = new GD4_Oggetti_File_Log(dbOpGlobal);
	    	  gd4Ogfi.retrieveOggettiFileLog(idDocumento, "");
	    	  gd4Ogfi.generaImpronte();
	      }
	      catch(Exception e){
	    	  throw new Exception("ImprontaAllegati::sistemaImpronte Errore in generazione impronte per documento "+idDocumento+". Errore = "+e.getMessage());
	      }		  
	}
	
	private String bIsImprontaAttiva(IDbOperationSQL dbOp) throws Exception {			
			try {
	            StringBuffer sStm = new StringBuffer();
	
	            sStm.append("select valore from PARAMETRI");
	            sStm.append(" where codice =  'GENERA_IMPRONTA'");
	            sStm.append(" and TIPO_MODELLO='@STANDARD'");
	
	            dbOp.setStatement(sStm.toString());
	            dbOp.execute();  
	
	            ResultSet rst = dbOp.getRstSet();
	
	            if (rst.next()) {
	            	gestisciImprontePar=rst.getString(1);
	            }		   
	            
	            return gestisciImprontePar;
	        }
	        catch (Exception e) {	            
	            throw new Exception("ImprontaAllegati::bIsImprontaAttiva\n" + e.getMessage());	            
	        }
	}
	
    /**
     * Metodo che calcola le impronte degli allegati del documento
     * memorizzandole sul DB
     * 
	 * @throws Exception
    */
	public void generaImpronte() throws Exception {
		   String nomeFile = null;
		   String query = "";
		   IDbOperationSQL dbOp = null;
		   
		  
		
		   try {
			 if (limp.getNumeroValori() > 0) {
				throw new Exception("Impronte allegati già presenti.");
				
			 }
			 if (this.dbOpGlobal==null) {
	             dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
	             dbOp.autoCommitOff();
			 }
			 else {
				 dbOp=dbOpGlobal;
			 }
			 String s = bIsImprontaAttiva(dbOp);
			 if (s.equals("N")) return;
			 
			 
			 String lf = listaFile;
			 String[] arrLf = lf.split("@_#_@");
			 //StringTokenizer st = new StringTokenizer(lf,"@_#_@");
			 
			 for(int indexArr=0;indexArr<arrLf.length;indexArr++) {
				nomeFile = arrLf[indexArr];
				if (nomeFile.equals("")) continue;
				byte[] b = hashCodeAllegato(nomeFile);
                limp.aggiungiImpronta(nomeFile, b);				
			}
			 
			
			 
		     for (int i=0; i < limp.getNumeroValori(); i++) {
		    	 query = "INSERT INTO IMPRONTE_FILE (ID_DOCUMENTO, FILENAME, HASHCODE) VALUES ("+idDocumento+",:P_FILE,:IMPRONTA)";                  
                 
		    	 dbOp.setStatement(query);
                 dbOp.setParameter(":P_FILE",limp.getNomeFile(i));
                 dbOp.setParameter(":IMPRONTA", limp.getImpronta(i));
                 dbOp.execute();                 
			}
			
		    if (this.dbOpGlobal==null) {dbOp.close();}
		    
		  } catch (Exception e) {
			if (this.dbOpGlobal==null) {try {dbOp.close();} catch (Exception edb) {}}
			throw new Exception("ImprontaAllegati::generaImpronte()\n"+e.getMessage());
		  }
	}
	
	public void generaImpronta(String nomeFile) throws Exception {
		   generaImpronta(nomeFile,null,false);
	}
	
	public void generaImpronta(String nomeFile, boolean bNonControllareImpostazione) throws Exception {
		   generaImpronta(nomeFile,null,bNonControllareImpostazione);
	}
	
	public void generaImpronta(String nomeFile, InputStream is, boolean bNonControllareImpostazione) throws Exception {
		generaImpronta( nomeFile,  is,  bNonControllareImpostazione,false,"-1");
	}
	
	public void generaImpronta(String nomeFile, InputStream is, boolean bNonControllareImpostazione, boolean bNonControllareEsistenza) throws Exception {
		generaImpronta( nomeFile,  is,  bNonControllareImpostazione,  bNonControllareEsistenza,"-1");
	}
	
    /**
     * Metodo che calcola l'impronta dell'allegato specificato
     * memorizzandola sul DB
     *  
	 * @param nomeFile Nome dell'allegato di cui si vuole generare l'impronta
	 * @throws Exception
	*/
	public void generaImpronta(String nomeFile, InputStream is, boolean bNonControllareImpostazione, boolean bNonControllareEsistenza, String idLog) throws Exception {
		   String query = "";
		   IDbOperationSQL dbOp = null;
		   boolean bEsiste=false;
		   		  
		   try {
			 if (idLog.equals("-1")) {
				 if (!bNonControllareEsistenza) {
					 if (limp.esisteImpronta(nomeFile)) {
						//throw new Exception("Impronta allegato già presente.");
						 //vado in update
						 bEsiste=true;
					 }
				 }
			 }
			 
			 if (this.dbOpGlobal==null) {
	             dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
	             dbOp.autoCommitOff();
			 }
			 else {
				 dbOp=dbOpGlobal;
			 }
			 
			 if (idLog.equals("-1")) {
				 if (!bNonControllareImpostazione) {
					 if (bIsImprontaAttiva(dbOp).equals("N")) return;
				 }
			 }
			 
			 byte[] b = hashCodeAllegato(nomeFile,is);
			 
			 if ( b!=null)  {
				 if (idLog.equals("-1")) {
					 if (bEsiste)
						 query = "UPDATE IMPRONTE_FILE SET HASHCODE = :IMPRONTA  WHERE FILENAME = :P_FILE AND ID_DOCUMENTO = "+idDocumento;
					 else
						 query = "INSERT INTO IMPRONTE_FILE (ID_DOCUMENTO, FILENAME, HASHCODE) VALUES ("+idDocumento+",:P_FILE,:IMPRONTA)";
				 }
				 else {
					 query = "UPDATE OGGETTI_FILE_LOG SET IMPRONTA = :IMPRONTA  WHERE ID_OGGETTO_FILE_LOG = "+idLog;
				 }
					 
				 dbOp.setStatement(query);
				 dbOp.setParameter(":P_FILE",nomeFile);
				 dbOp.setParameter(":IMPRONTA", b);
				 try {
					 dbOp.execute();
				 } catch (Exception ei) {
					 if (ei.getMessage().indexOf("ORA-00001")!=-1 && idLog.equals("-1")) {
						 query = "UPDATE IMPRONTE_FILE SET HASHCODE = :IMPRONTA  WHERE FILENAME = :P_FILE AND ID_DOCUMENTO = "+idDocumento;
						 
						 dbOp.setStatement(query);
						 dbOp.setParameter(":P_FILE",nomeFile);
						 dbOp.setParameter(":IMPRONTA", b);
						 
						 dbOp.execute();
					 }
					 else
						 throw ei;
				 }
				 
				 if (idLog.equals("-1")) limp.aggiungiImpronta(nomeFile, b);
			 }
			 if (this.dbOpGlobal==null) {dbOp.close();}

		   } catch (Exception e) {

		   	 e.printStackTrace();
			 try {dbOp.close();} catch (Exception edb) {}
    	     throw new Exception("ImprontaAllegati::generaImpronta(nomeFile,log="+idLog+")\n"+e.getMessage());
		   }
	}
	
    /**
     * Metodo che cancella le impronte degli allegati del documento
     * 
    */
	public void cancellaImpronte() throws Exception {
		   String query = "";
		   IDbOperationSQL dbOp = null;
		
		   try {
			 if (limp.getNumeroValori() == 0) {
				 return;
			 }
			 
			 dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
			 dbOp.autoCommitOff();
			 
			 query = "DELETE IMPRONTE_FILE WHERE ID_DOCUMENTO = "+idDocumento;
			 dbOp.setStatement(query);
			 dbOp.execute();			 
			 dbOp.close();
			 limp = new ListaImpronte();
		   } catch (Exception e) {
			 try {dbOp.close();} catch (Exception edb) {}
    	     throw new Exception("ImprontaAllegati::cancellaImpronte()\n"+e.getMessage());
		   }
	}
	
	public void cancellaImpronta(String nomeFile) throws Exception {
		   cancellaImpronta(nomeFile,true);
	}
	
    /**
     * Metodo che cancella l'impronte dell'allegato del documento
     * specificato
     * 
     * @param nomFile	Nome dell'allegato di cui si vuole eliminare l'impronta
     * 
    */
	public void cancellaImpronta(String nomeFile, boolean bControllaNumeroValori) throws Exception {
		   String query = "";
		   IDbOperationSQL dbOp = null;		
		   
		   try {
			 if (bControllaNumeroValori && limp.getNumeroValori() == 0) {
				return;
		     }
			 
			 if (this.dbOpGlobal==null) {
				 dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
				 dbOp.autoCommitOff();
			 }
			 else 
				 dbOp=dbOpGlobal;
				 
			 
			 query = "DELETE IMPRONTE_FILE WHERE ID_DOCUMENTO = "+idDocumento+
      				 " AND FILENAME = :P_FILE";
			 
			 dbOp.setStatement(query);
			 dbOp.setParameter(":P_FILE",nomeFile);
			 dbOp.execute();
			 if (this.dbOpGlobal==null) {dbOp.close();}
			 limp.rimuoviImpronta(nomeFile);
			 
		   } catch (Exception e) {
			 if (this.dbOpGlobal==null) {try {dbOp.close();} catch (Exception edb) {}}
    	     throw new Exception("ImprontaAllegati::cancellaImpronta()\n"+e.getMessage());
		   }
	}
	
	private byte[] hashCodeAllegato(String nomeFile) throws Exception {
			return 	hashCodeAllegato(nomeFile,null);
	}
	
    /**
     * Metodo che che calcola e restituisce l'impronta dell'allegato del documento
     * specificato
     *  
     * @param nomFile	Nome dell'allegato di cui calcolare l'impronta
     * 
	 * @return byte[]
	 * @throws Exception
	 * 
    */
	public byte[] hashCodeAllegato(String nomeFile, InputStream inS) throws Exception {
		InputStream is=null;
		try {
		  md.reset();

		  if (inS==null)
			  try {
				is = getFileStream(nomeFile);
			  }
		  	  catch (Exception e) {
		  		  //il file ha dato errore!!.... lo salto
		  	  }
		  else
			  is=inS;
		  
		  if (is==null) return null;
		  
	      DigestInputStream in = new DigestInputStream(is, md);
			byte[] buff = new byte[4096];
	      while ((in.read(buff)) != -1);
		   
	      try {	  if (inS==null)  is.close();}catch (Exception ei){}
		} catch (Exception e) {
			try {	if (inS==null)     is.close();}catch (Exception ei){}
    	throw new Exception("ImprontaAllegati::hashCodeAllegato(String nomeFile)\n"+e.getMessage());
		}

        return md.digest();		
	}
	
	/**
	 * Il metodo confronta per  l'allegato specificato l'eventuale impronta
	 * presente sul DB con quella calcolata. 
	 * 
	 * @param nomeFile Nome dell'allegato da verificare
	 * @return Stringa contente il codice di errore, i valori possibili sono:<BR>
	 * 				 GLobal.CODERROR_IA_NESSUN_ERRORE						la verifica è andata a buon fine<BR> 
	 * 				 GLobal.CODERROR_IA_DOCUMENTO_INESISTENTE		la profilo fà riferimento ad un documento non ancora memorizzato<BR> 
	 * 				 GLobal.CODERROR_IMPRONTA_ASSENTE						l'impronta dell'allegato non è stata generata<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_MODIFICATO				l'allegato risulta modificato<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_CANCELLATO				l'allegato risulta cancellato<BR> 
	 *
	 * @throws Exception
	 */
	public String verificaImpronta(String nomeFile) throws Exception {
		boolean retval = false;
		
		try {
			if (!p.accedi()) return Global.CODERROR_IA_DOCUMENTO_INESISTENTE;
			if (!limp.esisteImpronta(nomeFile)) return Global.CODERROR_IA_IMPRONTA_ASSENTE;
			
			String listaFile = p.getlistaFile("@_#_@");
			//StringTokenizer st = new StringTokenizer(listaFile,"@_#_@");
			String[] arrLf = listaFile.split("@_#_@");
			boolean trovato = false;
			for(int indexArr=0;indexArr<arrLf.length && !trovato;indexArr++) {					
				if (arrLf[indexArr].equals(nomeFile)) {
					trovato = true;
				}
			}
			if (!trovato) {
				return Global.CODERROR_IA_ALLEGATO_CANCELLATO;
			} 

			byte[] b = hashCodeAllegato(nomeFile);
			retval = Arrays.equals(limp.getImpronta(nomeFile), b);
		} catch (Exception e) {
    	throw new Exception("ImprontaAllegati::hashCodeAllegato(String nomeFile)\n"+e.getMessage());
		}
		if (!retval) {
			return Global.CODERROR_IA_ALLEGATO_MODIFICATO;
		} else {
			return Global.CODERROR_IA_NESSUN_ERRORE;
		}
	}
	
	/**
	 * Il metodo calcola le impronte per gli allegati del documento e le
	 * confronta con  quelle memorizzate sul DB . 
	 * 
	 * @return Lista SeganalazioniVerificaImpronte.<BR>
	 * 				 La lista conterra le coppie nome file - codice segnalazione.<BR>
	 * 				 Elenco codici:<BR>
	 * 				 GLobal.CODERROR_IA_NESSUN_ERRORE						la verifica è andata a buon fine<BR> 
	 * 				 GLobal.CODERROR_IA_DOCUMENTO_INESISTENTE		la profilo fà riferimento ad un documento non ancora memorizzato<BR> 
	 * 				 GLobal.CODERROR_IMPRONTA_ASSENTE						l'impronta dell'allegato non è stata generata<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_MODIFICATO				l'allegato risulta modificato<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_CANCELLATO				l'allegato risulta cancellato<BR> 
	 *
	 * @throws Exception
	 */
	public SeganalazioniVerificaImpronte verificaImpronte() throws Exception {
		SeganalazioniVerificaImpronte retval = new SeganalazioniVerificaImpronte();
		String nomeFile = ""; 
		String listaFile = "";
		String data = "";
		boolean trovato = false;
		int k = 0;
		try {
			if (!p.accedi(Global.ACCESS_ATTACH)) return null;
			//Controllo che tutti gli allegati del modello abbiano un impronta e sia coerente
			listaFile = p.getlistaFile("@_#_@");			
			//StringTokenizer st = new StringTokenizer(listaFile,"@_#_@");
			String[] arrLf = listaFile.split("@_#_@");
			for(int indexArr=0;indexArr<arrLf.length;indexArr++) {				
				nomeFile = arrLf[indexArr];
				
				if (nomeFile.equals("")) continue;
				
				long idFile = Long.parseLong(p.getIdFile(nomeFile));
				data = p.getUpdateDateFile(idFile);
				if (!limp.esisteImpronta(nomeFile)) {
					retval.aggiungiSegnalazione(nomeFile, Global.CODERROR_IA_IMPRONTA_ASSENTE, data);
				} else {
					byte[] b = hashCodeAllegato(nomeFile);
					if (!Arrays.equals(limp.getImpronta(nomeFile), b)) {
						retval.aggiungiSegnalazione(nomeFile, Global.CODERROR_IA_ALLEGATO_MODIFICATO, data);
					} else {
						retval.aggiungiSegnalazione(nomeFile, Global.CODERROR_IA_NESSUN_ERRORE, data);
					}
				}
				k++;
			}
			
			//Controllo se qualche allegato è stato cancellato
			for (int i =0; i< limp.getNumeroValori(); i++) {
				nomeFile = limp.getNomeFile(i);
				//st = new StringTokenizer(listaFile,"@_#_@");
				String[] arrListaFile = listaFile.split("@_#_@");
				trovato = false;
				for(int indexArr=0;indexArr<arrListaFile.length && !trovato;indexArr++) {				
					if (arrListaFile[indexArr].equals(nomeFile)) {
						trovato = true;
					}
				}
				
				if (!trovato) {
					retval.aggiungiSegnalazione(nomeFile, Global.CODERROR_IA_ALLEGATO_CANCELLATO, "");
				}
				k++;
			}
			
			//Carico i documenti Figli
			for (int i = 0; i < iddocFigli.size(); i++) {
				Profilo p2 = new Profilo(iddocFigli.get(i));
				p2.initVarEnv(p.getUser(), "", cn);
				ImprontaAllegati ia = new ImprontaAllegati(p2);
				SeganalazioniVerificaImpronte segn_figlio = ia.verificaImpronte();
				for (int j=0; j < segn_figlio.getNumeroValori(); j++) {
					retval.aggiungiSegnalazione(segn_figlio.getNomeFile(j),segn_figlio.getCodice(j),segn_figlio.getData(j));
				}
			}
		} catch (Exception e) {
    	  throw new Exception("ImprontaAllegati::verificaImpronte()\n"+e.getMessage());
		}
		
		return retval;
	}
	
	private void caricaImpronteDB() throws Exception {
		String query = "";
		IDbOperationSQL dbOp = null;
		ResultSet rst = null;
		
		try {
			limp = new ListaImpronte();
			
			if (this.dbOpGlobal==null) dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
			else dbOp=dbOpGlobal;
				
			query = "SELECT FILENAME, HASHCODE FROM IMPRONTE_FILE WHERE ID_DOCUMENTO = "+idDocumento;
			dbOp.setStatement(query);
			dbOp.execute();
			rst = dbOp.getRstSet();
			while (rst.next()) {
		      	String chunk=null;
		      	int numBytes =64;
		      	String hashCode = rst.getString(2);
		
		      	byte[] rawToByte = new byte[numBytes];
		      	int offset=0;
		      	for(int i =0; i <numBytes; i++) {
			      	chunk = hashCode.substring(offset,offset+2);
			      	offset+=2;
			      	rawToByte[i] = (byte) (Integer.parseInt(chunk,16) & 0x000000FF); 
		      	}		      	
		      	limp.aggiungiImpronta(rst.getString(1), rawToByte);
			}
			
			if (this.dbOpGlobal==null) dbOp.close();
		 } catch (Exception e) {
			try {
			  if (this.dbOpGlobal==null) dbOp.close();
			} catch (Exception edb) {}
    	 
			throw new Exception("ImprontaAllegati::caricaImpronteDB()\n"+e.getMessage());
		 }		
	}
	
	public void setFile(String nome,InputStream is) {
		   hIs.put(nome,is);
	}
	
	public InputStream getFileStream(String nome) throws Exception {
		   if (p!=null) return p.getFileStream(nome);
		  
		   if (hIs!=null) {
			   if (hIs.containsKey(nome)) 
				   return hIs.get(nome);
			   else
				   throw new Exception("ImprontaAllegati::getFileStream - Non esiste il file con nome="+nome);
		   }
		   
		   throw new Exception("ImprontaAllegati::getFileStream - Strutture di file (profilo e Hash) vuote...non riesco a trovare il file "+nome);
	}
	
	public boolean esistonoImpronte() {
		if (limp.getNumeroValori() > 0) {
			return true;
		}
		return false;
	}
	
	public void caricaDocumentiFigli(String area, String cm) throws Exception {
		String area_figlio = "";
		String cm_figlio = "";
		String legame = "";
		String query = "";
		IDbOperationSQL dbOp = null;
		ResultSet rst = null;
		
		try {
			if (this.dbOpGlobal==null) dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
			else dbOp=dbOpGlobal;
			
			ImprontaParser ap = new ImprontaParser(p);
			query = "SELECT BM.condizioni_legame, b.area, b.codice_modello "+
							"FROM blocchi_modello BM, BLOCCHI B "+
							"WHERE BM.area = :AREA "+
							"AND BM.CODICE_MODELLO = :CM "+
							"AND BM.AREA_BLOCCO = B.AREA "+
							"AND BM.BLOCCO = B.BLOCCO";
			dbOp.setStatement(query);
			dbOp.setParameter(":AREA", area);
			dbOp.setParameter(":CM", cm);
			dbOp.execute();
			rst = dbOp.getRstSet();
			while (rst.next()) {
				legame = rst.getString(1);
				area_figlio = rst.getString(2);
				cm_figlio = rst.getString(3);
				
	      IQuery iq = new IQuery(cm_figlio, area_figlio);
	      iq.initVarEnv(p.getUser(),"",cn);
	      iq.setAccessProfile(false);
	      iq.setInstanceProfile(false);
//	      iq.escludiControlloCompetenze(true); //da commentare??????
//	      iq.setTypeModelReturn(area_figlio,cm_figlio);

	      //Condizioni di legame
	      String lega = ap.bindingDeiParametri(legame);
	      lega = lega.replaceAll(" and "," AND ");
	      lega = lega.replaceAll(" like "," LIKE ");
	      lega = lega.replaceAll(" between "," BETWEEN ");
	      String strval= "";
	      int posAnd = 0;
	      int lung = lega.length();
	      while (lung > 0) {
	        posAnd = lega.indexOf(" AND ");
	        if (posAnd > 0) {
	         strval = lega.substring(0,posAnd);
	         lega = lega.substring(posAnd+5);
	        } else {
	          strval = lega;
	          lega = "";
	        }
	        lung = lega.length();

	        String campoF = "";
	        String valoreF = "";
	        int posDiv    = strval.indexOf("<>");
	        int posMinUg  = strval.indexOf("<=");
	        int posMagUg  = strval.indexOf(">=");
	        int posUg     = strval.indexOf("=");
	        int posLike   = strval.indexOf("LIKE");

	        if (posDiv > 0) {
	          posUg = -1;
	          campoF = strval.substring(0,posDiv);
	          campoF = campoF.replaceAll("'","");
	          campoF = campoF.replaceAll(" ","");
	          valoreF = strval.substring(posDiv+2);
	          valoreF = valoreF.replaceAll("'","");
	          valoreF = valoreF.replaceAll(" ","");
	          iq.addCampo(campoF,valoreF,"<>");
	        }
	        if (posMinUg > 0) {
	          posUg = -1;
	          campoF = strval.substring(0,posMinUg);
	          campoF = campoF.replaceAll("'","");
	          campoF = campoF.replaceAll(" ","");
	          valoreF = strval.substring(posMinUg+2);
	          valoreF = valoreF.replaceAll("'","");
	          valoreF = valoreF.replaceAll(" ","");
	          iq.addCampo(campoF,valoreF,"<=");
	        }
	        if (posMagUg > 0) {
	          posUg = -1;
	          campoF = strval.substring(0,posMagUg);
	          campoF = campoF.replaceAll("'","");
	          campoF = campoF.replaceAll(" ","");
	          valoreF = strval.substring(posMagUg+2);
	          valoreF = valoreF.replaceAll("'","");
	          valoreF = valoreF.replaceAll(" ","");
	          iq.addCampo(campoF,valoreF,">=");
	        }
	        if (posUg > 0) {
	          campoF = strval.substring(0,posUg);
	          campoF = campoF.replaceAll("'","");
	          campoF = campoF.replaceAll(" ","");
	          valoreF = strval.substring(posUg+1);
	          valoreF = valoreF.replaceAll("'","");
	          valoreF = valoreF.replaceAll(" ","");
	          iq.addCampo(campoF,valoreF);
	        }
	        if (posLike > 0) {
	          campoF = strval.substring(0,posLike);
	          campoF = campoF.replaceAll("'","");
	          campoF = campoF.replaceAll(" ","");
	          valoreF = strval.substring(posLike+4);
	          valoreF = valoreF.replaceAll("'","");
	          valoreF = valoreF.replaceAll(" ","");
	          iq.addCampo(campoF,valoreF);
	        }
	      }
	      if (iq.ricerca().booleanValue()) {
	      	 ResultSetIQuery  rstiq = iq.getResultSet();
	         while (rstiq.next()) {
	        	 iddocFigli.add(rstiq.getId());
	         }
	      }
	      
			}
			
			if (this.dbOpGlobal==null) dbOp.close();
		 } catch (Exception e) {
			try {
			  if (this.dbOpGlobal==null) dbOp.close();
			} catch (Exception edb) {}
    	 
			throw new Exception("ImprontaAllegati::caricaImpronteDB()\n"+e.getMessage());
		 }	
	}
	
	public void setDbOpGlobal(IDbOperationSQL dbOpGlobal) {
		   this.dbOpGlobal = dbOpGlobal;
	}
	
	public void setIdDocumento(String idDocumento) {
		   this.idDocumento = idDocumento;
	}

    public static void main(String args[]) throws FileNotFoundException, SQLException, IOException {
		 Connection conn = null;
		 try { 
			 Class.forName("oracle.jdbc.driver.OracleDriver");
			 //conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora04:1521:JSUITE","GDM","GDM");
			 conn=DriverManager.getConnection("jdbc:oracle:thin:@JVM-EFESTO:1521:ORCL","GDM","GDM");

			 conn.setAutoCommit(false);

			 //ProfiloVersion pv = new ProfiloVersion("12416079",2);
			 System.out.println("Inizio");
			 Profilo p = new Profilo("371");
			 p.initVarEnv("GDM","",conn);
			 if (p.accedi()) {
  			 SeganalazioniVerificaImpronte svi = p.verificaImpronte512();
  			 for (int i=0; i < svi.getNumeroValori(); i++) {
  				 System.out.println(svi.getNomeFile(i)+" del "+ svi.getData(i)+ " ---> "+svi.getCodice(i));
  			 }
			 
			 } else {
				 System.out.println(p.getError());
			 }
			 conn.commit();
			 conn.close();
		 }
		 catch (Exception e) {
			 try {conn.rollback();}catch (Exception ei) {}
			 e.printStackTrace();
		 }
  }
}

package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI DOCUMENTI
 * DIPENDENZE CON: GD4_Documento
 *                 Humm_Documento
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.dmServer.util.DMActivity_Log;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.util.FieldInformation;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

import java.util.*;

public abstract class A_Documento implements I_Documento
{
  // Variabili private 
  protected String idDocumento = "0";
  protected String areaDoc = "";
  protected A_Libreria libreria;
  protected A_Tipo_Documento tipoDocumento;
  protected A_Status_Documento statusDocumento;
  protected Vector oggettiFile;                           //Documento è costituito da più oggetti file
  protected Vector valori;                               //Documento è costituito da più valori
  protected String codRich, dataAggiornamento, dataCreazione;            // INTERFACCIAMENTO MODULISTICA


  protected Object token;
  protected Vector vACL;
  protected HashMapSet hashMapExtraCompetenze;
  protected Vector related;
  protected Vector relatedFrom;
  protected Hashtable links;
  protected String idDocumentoPadre;
  protected boolean bLock = false;    
  protected String typeLock = "nowait";
  
  protected String conservazione = null;
  protected String archiviazione = null;  
  
  protected boolean bAggiornaDataUltAgg=true;
  protected boolean bAllegatiTempModulistica=false;
  
  protected String crAllegatiTempModulistica=null;
  
  protected String codeErrorSaveDoc = null;
  protected String descrErrorSaveDoc = null;
  
  protected String pathFileArea="";
  protected String pathFileAreaAree="";
  protected String arcmcr="";  
  
  protected Vector<String> vElencoNomiAllegatiModificati = new Vector<String>();
  protected Vector<String> vElencoNomiAllegatiRinominati = new Vector<String>();
  
  //DbOp per la lettura degli stream dalla Allegati_temp
  protected IDbOperationSQL dbOpSqlAllegatiTemp; 
  
  protected boolean skipReindexFullTextField=false;
  
  protected boolean competenzeAllegati=false;
  
  protected int letturaAllegati=1;
  protected int modificaAllegati=1;
  protected int cancellaAllegati=1;
  
  protected boolean bDontRepeatExistsRif=false;
  
  protected DMActivity_Log dmALog;

  // ***************** METODI DI SET E GET ***************** //

  protected boolean bogfilog =true;

  protected boolean creaVersione = false;

  protected long ultimaVersione = 0;

    protected long lastIdLog;


public DMActivity_Log getDmALog() {
	  return dmALog;
  }

public String getCodiceRichiesta()
  {
         return codRich;
  }
   
  public void setCodiceRichiesta(String newCodRich)
  {
         codRich = newCodRich;
  }

  public void setArea(String newArea)
  {
         areaDoc = newArea;
  }

  public String getArea()
  {
         return areaDoc;
  }
  
  public String getIdDocumento()
  {
         return idDocumento;
  }
   
  public void setIdDocumento(String newIdDocumento)
  {
         idDocumento = newIdDocumento;
  }
  
  public void setAggiornaDataUltAgg(boolean bFlag)
  {
         bAggiornaDataUltAgg = bFlag;
  }  

  public String getDataAggiornamento()
  {
         return dataAggiornamento;
  }
   
  public void setDataAggiornamento(String newDataAggiornamento)
  {
         dataAggiornamento = newDataAggiornamento;
  }
   
  public A_Libreria getLibreria()
  {
          return libreria;
  }
  
  public void setLock(boolean bLockDoc) {
  		 bLock=bLockDoc;
  }  
  
  
  public void setTypeLock(String typeL) {
		 typeLock=typeL;
  } 
   
  public void setLibreria(A_Libreria newLibreria)
  {
         libreria = newLibreria;
  }
   
  public A_Tipo_Documento getTipoDocumento()
  {
         return tipoDocumento;
  }
 
  public void setTipoDocumento(A_Tipo_Documento newTipoDocumento)
  {
         tipoDocumento = newTipoDocumento;
  }
   
  public Vector getValori()
  {
         return valori;
  }
  
  public void setValori(Vector newValori)
  {
         valori = newValori;
  }
   
  public Vector getOggettiFile()
  {
         return oggettiFile;
  }
   
  public void setOggettiFile(Vector newOggettiFile)
  {
         oggettiFile = newOggettiFile;
  }
  
  public void settaPadre(String idPadre) {
	     idDocumentoPadre=idPadre;
  }
  
  public String getPadre() {
	     return idDocumentoPadre;
  }
  
  public void aggiungiACL(String user,String type) 
  {
         ACL acl = new ACL(user,type);
         vACL.addElement(acl);
  }
  
  public void aggiungiACL(String user,String type,String ruolo) 
  {
         ACL acl = new ACL(user,type,ruolo);
         vACL.addElement(acl);
  }  

  public Object getACL() 
  {      
         return vACL;
  }
  
  public void annullaACL() 
  {
         vACL.removeAllElements();
  }
  
  public void aggiungiExtraCompetenze(String user,String competenza) 
  {
         hashMapExtraCompetenze.add(user,competenza);         
  }

  public Object getExtraCompetenze() 
  {      
         return hashMapExtraCompetenze;
  }

  public A_Status_Documento getStatusDocumento()
  {
         return statusDocumento;
  }
   
  public void setStatusDocumento(A_Status_Documento newstatusDocumento)
  {
         statusDocumento = newstatusDocumento;
  }

  public void setToken(Object newToken)
  {
         token = newToken;
  }

  public Object getToken()
  {
         return token;
  }
  
  public void setConservazione(String sConservazione) {	    
	     conservazione=sConservazione;	      
  }

  public void setArchiviazione(String sArchiviazione) {	    
	     archiviazione=sArchiviazione;	      
  }  
  
  public String getConservazione() {	    
	     return conservazione;
  }

  public String getArchiviazione() {	    
	     return archiviazione;
  }

  public String toString() 
  {
         StringBuffer objectState= new StringBuffer();
         String sOggettiFile, sValori, sLibreria, sTipoDoc;

         try {
           sOggettiFile = oggettiFile.toString();           
         }
         catch (NullPointerException e) {
           sOggettiFile = "(nulla)";
         }

         try {
           sValori = valori.toString();
         }
         catch (NullPointerException e) {
           sValori = "(nulla)";
         }
         
         try {
           sLibreria = libreria.toString();
         }
         catch (NullPointerException e) {
           sLibreria = "(nulla)";
         }

         try {
           sTipoDoc = tipoDocumento.toString();
         }
         catch (NullPointerException e) {
           sTipoDoc = "(nulla)";
         }

         objectState.append("Classe: " + this.getClass().getName() + "\n");
         objectState.append("<oggettiFile> = " + sOggettiFile + "\n");
         objectState.append("<valori> = " + sValori + "\n");
         objectState.append("<libreria> = " + sLibreria + "\n");
         objectState.append("<tipoDocumento> = " + sTipoDoc);
         
         return objectState.toString();
  }
  
 // ***************** METODI DI GESTIONE VALORI ***************** //
  /*
   * METHOD:      findValore(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idValore -> id Valore
   *              Ricerca e restituisce A_Valore dalla lista
   *              dei valori dato l'id del valore
   *              
   * RETURN:      A_Valori
  */  
  public A_Valori findValore(String idValore) 
  {
         int conta=0,size=valori.size();
         Object obj;
         
         while (conta!=size) {
               obj = valori.elementAt(conta++);
               A_Valori val = (A_Valori)obj;
               if (val.getIdValore().compareTo(idValore) == 0) return val;
         }
         return null;         
  }

 /*
   * METHOD:      findValoreByCampi(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idCampo -> id Campo
   *              Ricerca e restituisce A_Valore dalla lista
   *              dei valori dato l'id del campo
   *              
   * RETURN:      A_Valori
  */  
  public A_Valori findValoreByCampi(String idCampo) 
  {
         int conta=0,size=valori.size();
         Object obj;

         A_Valori val;

         while (conta!=size) {
               obj = valori.elementAt(conta++);
               val = (A_Valori)obj;
               if (val.getCampo().getIdCampo().compareTo(idCampo)==0) return val;
         }
         return null;         
  }

 /*
   * METHOD:      aggiornaValore(A_Valori)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: valUpd -> A_Valore
   *              Sostituisce un A_Valore con un altro A_Valore
   *              avente lo stesso id. Serve per aggiornare tutti
   *              i riferminenti interni e quindi aggiornare
   *              un valore esistente all'interno di un documento
   *  
   * RETURN:      TRUE  -> valore trovato e aggiornato
   *              FALSE -> Altrimenti  
  */  
   public boolean aggiornaValore(A_Valori valUpd, FieldInformation fi)
  {
         int conta=0,size=valori.size();
         Object obj;
         
         while (conta!=size) {
               obj = valori.elementAt(conta);
               A_Valori val = (A_Valori)obj;
               if (val.getCampo().getIdCampo().equals(valUpd.getCampo().getIdCampo())) {
                   this.getValori().set(conta,valUpd);
                   val.setModificato("S"); 
                   val.setFieldInformation(fi);
                   return true;
                }
               conta++;                    
         }
         return false;         
  }

 /*
   * METHOD:      svuotaListaValori()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Svuota la lista dei valori
   * RETURN:      void 
   *              
  */  
  public void svuotaListaValori()
  {
         valori.removeAllElements();
  }

 // ***************** METODI DI GESTIONE OGGETTI FILE ***************** //
 
 /*
   * METHOD:      findOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idOggettoFile -> Id Oggetto File
   *              Ricerca e restituisce un A_Oggetti_File dalla lista
   *              degli oggettiFile dato l'id dell'Oggetto File
   *              
   * RETURN:      A_Oggetti_File  
   * 
  */  
  public A_Oggetti_File findOggettoFile(String idOggettoFile) throws Exception
  {      
         int conta=0,size=oggettiFile.size();
         Object obj;

         if (size==0)
            return null;
            
         if ((idOggettoFile==null) && (size==1)) 
            return (A_Oggetti_File)oggettiFile.elementAt(0);

         if ((idOggettoFile==null) && (size!=1))
            throw new Exception("A_Documento::findOggettoFile() esistono più oggetti file e non è stato specificato l'id da aggiornare!!" );
                     
         while (conta!=size) {
               obj = oggettiFile.elementAt(conta++);
               A_Oggetti_File oFile = (A_Oggetti_File)obj;
               if (oFile.getIdOggettoFile().compareTo(idOggettoFile) == 0) return oFile;
         }
         
         throw new Exception("A_Documento::findOggettoFile() non esiste l'oggetto file !!" );     
  }

  /*
   * METHOD:      aggiornaOggettoFile(A_Oggetti_File)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: oFileUpd -> Id Oggetto File
   *              Sostituisce un A_Oggetti_File con un altro
   *              A_Oggetti_File con lo stesso id. 
   *              Serve per aggiornare tutti i riferminenti interni
   *              e quindi aggiornare un oggetto file esistente 
   *              all'interno di un documento.
   *              
   * RETURN:      TRUE  -> valore trovato e aggiornato
   *              FALSE -> Altrimenti  
  */  
  public boolean aggiornaOggettoFile(A_Oggetti_File oFileUpd)
  {
         int conta=0,size=oggettiFile.size();
         Object obj;
         
         while (conta!=size) {
               obj = oggettiFile.elementAt(conta);
               A_Oggetti_File oFile = (A_Oggetti_File)obj;
               if (oFile.getIdOggettoFile() == oFileUpd.getIdOggettoFile()) {
                   oggettiFile.set(conta,oFileUpd);
                   oFile.setModificato("S");
                   return true;
               }
               conta++;                    
         }
         return false;         
  }

 /*
   * METHOD:      svuotaListaOggettiFile()
   * SCOPE:       PUBLIC
   * DESCRIPTION: Svuota la lista degli oggetti file
   * RETURN:      void  
  */  
  public void svuotaListaOggettiFile()
  {
         oggettiFile.removeAllElements();
  }

 /*
   * METHOD:      settaAllModificatoAndReset(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: sTipoOggetto='O' -> OggettiFile
   *              sTipoOggetto='V' -> Valori
   * RETURN:      void  
   * 
  */                                  
  public void settaAllModificatoAndReset(String sTipoOggetto, String sMod)
  {
         settaAllModificato(sTipoOggetto,sMod,true,true);
  }

 /*
   * METHOD:      settaAllModificato(String,String,boolean,boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: sTipoOggetto='O' -> OggettiFile
   *              sTipoOggetto='V' -> Valori
   * RETURN:      void  
   * 
  */                                  
  public void settaAllModificato(String sTipoOggetto,
                                 String sMod, 
                                 boolean flagAzzeraIdOgg,
                                 boolean flagAzzeraIdVal)
  {   
         if (sTipoOggetto.equals("O")) {
            int conta=0,size=oggettiFile.size();
            Object obj;
         
            while (conta!=size) {
                  obj = oggettiFile.elementAt(conta);
                  A_Oggetti_File oFile = (A_Oggetti_File)obj;
                  oFile.setModificato(sMod);                    
                  if (flagAzzeraIdOgg) oFile.setIdOggettoFile("0");
                  conta++;                    
            }
         }
         else {
            int conta=0,size=valori.size();
            Object obj;
         
            while (conta!=size) {
                  obj = valori.elementAt(conta);
                  A_Valori val = (A_Valori)obj;
                  val.setModificato(sMod);
                  if (flagAzzeraIdVal) val.setIdValore("0");
                  conta++;                    
            }
         }
  }
  
  public boolean getAllegatiTempModulistica() {
	  	 return bAllegatiTempModulistica;
  } 

  public void setAllegatiTempModulistica(boolean allegatiTempModulistica) {
	     bAllegatiTempModulistica = allegatiTempModulistica;
  }    
  
  public void setAllegatiCrTempModulistica(String cr) {
	     crAllegatiTempModulistica = cr;
  }   
  
  public String getCodeErrorSaveDoc() {
	     return codeErrorSaveDoc;
  }

  public String getDescrErrorSaveDoc() {
	     return descrErrorSaveDoc;
  }
  
 public Object getRiferimento() { return related;}  
 
 public Object getRiferimentoFrom() { return relatedFrom;}
 
 public Hashtable getLinks() { return links;} 
 
 public int getLetturaAllegati() {
		return letturaAllegati;
 }

 public void setLetturaAllegati(int letturaAllegati) {
		this.letturaAllegati = letturaAllegati;
 }

 public int getModificaAllegati() {
		return modificaAllegati;
 }

 public void setModificaAllegati(int modificaAllegati) {
		this.modificaAllegati = modificaAllegati;
 }

 public int getCancellaAllegati() {
		return cancellaAllegati;
 }

 public void setCancellaAllegati(int cancellaAllegati) {
		this.cancellaAllegati = cancellaAllegati;
 }
 
 public boolean isCompetenzeAllegati() {
		return competenzeAllegati;
 }

 public void setCompetenzeAllegati(boolean competenzeAllegati) {
		this.competenzeAllegati = competenzeAllegati;
 } 
 
  // ***************** DEFINIZIONE METODI ASTRATTI ***************** //
 public abstract boolean retrieve(boolean flagTipoDocumento,
                                   boolean flagValori,
                                   boolean flagOggettiFile,
                                   boolean flagLog,
                                   String  idLog) throws Exception;
  public abstract boolean addOggettoFile(String idFormato,
                                         String fileName,
                                         String allegato,
                                         Object file) throws Exception;
  public abstract boolean addOggettoFile(String idFormato,
                                         String fileName,
                                         String allegato,
                                         String idFilePadre,
                                         Object file) throws Exception;                                         
  public abstract void inizializzaDati(Object vUtente) throws Exception;
  public abstract boolean addValore(String idCampo, Object valore) throws Exception; 
  public abstract boolean addValore(String nomeCampo, String idCampo, Object valore) throws Exception;
  public abstract boolean addValore(String nomeCampo, String idCampo, Object valore,FieldInformation fi) throws Exception;
  public abstract boolean retrieveAbstract() throws Exception;                                   
  public abstract boolean insertDocument(String stato) throws Exception;
  public abstract boolean updateDocument() throws Exception; 
  public abstract boolean deleteDocument() throws Exception;
  public abstract boolean cancellaOggettiFile(String idOggettoFile,boolean flagCancellaLista) throws Exception;
  public abstract boolean cancellaAllValori() throws Exception; 
  public abstract boolean visualizza();
  public abstract String getPercorsoKFX() throws Exception;
  public abstract void logDocument(String sTipoAzione) throws Exception;
  public abstract boolean cambiaStatoDocumento(String newStato, boolean bUpdDoc) throws Exception;  
  public abstract boolean cambiaStatoDocumento(String newStato) throws Exception;  
  public abstract String  getUltAggiornamento() throws Exception;
  public abstract Vector listaDiscendenti(int livelloDiscendenti) throws Exception;
  public abstract void retrieveLinks(String area, String cm) throws Exception;
  public abstract void syncroFS(boolean bSonoInInsert) throws Exception;
  public abstract void finalizzaGestioneAllegatiTemp() throws Exception;
  public abstract void disconnectDbOpAllegatiTemp();
  public abstract boolean saveVersion(long lVersion, boolean bNonRipetereUguali, Date dataAggiornamentoLog) throws Exception;
  public abstract List<String> moveFile(String idObjFile) throws Exception;
  /************************* UTILIZZATE DA HUMM  ******************************/
  
  public abstract void aggiungiRiferimento(String doc, String tipoRif) throws Exception;
  public abstract void eliminaRiferimento(String doc, String tipoRif) throws Exception; 
  public abstract void annullaRiferimento();
  
  public abstract void settaFileP7M(Object file);  
  public abstract void setSearchXml(String s);
  public abstract String getCodeError();
  public abstract Vector retrieveListaValoriLog() throws Exception;

  public String getArcmcr() {
		 return arcmcr; 
  }
	
  public void setArcmcr(String arcmcr) {
		 this.arcmcr = arcmcr;
  }
	
  public String getPathFileArea() {
		 return pathFileArea;
  }
	
  public void setPathFileArea(String pathFileArea) {
		 this.pathFileArea = pathFileArea;
  }
  
  public void cleanExtraCompetenze() {
	  	 hashMapExtraCompetenze = null;
	     hashMapExtraCompetenze = new HashMapSet();
  }  
	
  public IDbOperationSQL getDbOpSqlAllegatiTemp() {
		 return dbOpSqlAllegatiTemp;
  }
	
  public void setDbOpSqlAllegatiTemp(IDbOperationSQL dbOpSqlAllegatiTemp) {
		 this.dbOpSqlAllegatiTemp = dbOpSqlAllegatiTemp;
  }

  public Vector<String> getVElencoNomiAllegatiModificati() {
	     return vElencoNomiAllegatiModificati;
  }
  
  public Vector<String> getVElencoNomiAllegatiRinominati() {
	     return vElencoNomiAllegatiRinominati;
  }

  public void setSkipReindexFullTextField(boolean skipReindexFullTextField) {
	     this.skipReindexFullTextField = skipReindexFullTextField;
  }
  
  public String getDataCreazione() {
		 return dataCreazione;
  }
 
  public void setbDontRepeatExistsRif(boolean bDontRepeatExistsRif) {
		this.bDontRepeatExistsRif = bDontRepeatExistsRif;
  }
  
  public boolean isOgfiLog() {
	    return bogfilog;
  }


  public void setOgfiLog(boolean bogfilog) {
		this.bogfilog = bogfilog;
  }   	
  
  public void creaVersione(boolean creaVersione) {
		this.creaVersione = creaVersione;
  }
  

  public long getUltimaVersione() {
  		return ultimaVersione;
  }

    public long getLastIdLog() {
        return lastIdLog;
    }
}
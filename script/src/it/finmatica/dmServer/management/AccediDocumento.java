package it.finmatica.dmServer.management;

/*
 * GESTIONE DOCUMENTO
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   15/09/2005
 * 
 * */

import java.io.*;
import java.util.*;
import it.finmatica.dmServer.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.mapping.GDMapping;
import it.finmatica.dmServer.Riferimento;
import it.finmatica.dmServer.check.CheckDocumento;

public class AccediDocumento extends ManageDocumento
{  
   private final String ACCEDI_FULL     = "FULL"; 
   private final String ACCEDI_VALORI   = "VALO"; 
   private final String ACCEDI_ALLEGATI = "ALLE"; 
   private final String ACCEDI_ABSTRACT = "ABST"; 
   private final String ACCEDI_LOG      = "LOG";      
   
   private String idLog=null;
   
   private boolean bIsNew=false; 
   
   
   
   private ElapsedTime elpsTime; 
   
   private String codeError = Global.CODERROR_NOT_DEFINED;
   
   private int setRetrieveAllAcl = Profilo.NORETRIEVE_ALLACL;
   private HashMapSet hmsACL;
   private HashSet<String> hsListAclToRetrieve;
   private String dataCreazione;      
            
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 


/* 
   * METHOD:      Constructor(String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede ad un documento a partire
   *              dall'idDocumento e variabili di
   *              ambiente
   * 
   * RETURN:      none
  */
  public AccediDocumento(String idDocument, Environment vEnv) throws Exception 
  {
           super(idDocument,vEnv);
          
           // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************* //
           /* */ if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
           /* */     //Verifica competenze di lettura sul documento           
           /* */     try {
           /* */	              	  
           /* */       if (!varEnv.getByPassCompetenze()) {
           /* */           Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC, idDocument , "L"); 
           /* */           UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(), varEnv.getPwd(),  varEnv.getUser(), varEnv);
           /* */            if ((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua, abilitazione) == 0) {
        	   					codeError=Global.CODERROR_ACCESS_NOCOMPETENZE_READ;
           /* */                throw new Exception("Accesso al documento ("+idDocument+") Fallito - Utente non autorizzato. Controllare le competenze di lettura dell'utente ("+varEnv.getUser()+")");
           					}
           /* */       }
           				
                                  
           			   //Controllo check in / check out
           			   CheckDocumento chkDoc = null;
           			   try {
           				 chkDoc  = new CheckDocumento(idDocument,varEnv.getDbOp().getConn());           				              				   
           			   }
           			   catch (Exception e) {
           				   throw new Exception("Accesso al documento ("+idDocument+") Fallito (errore di controllo check in).\n"+e.getMessage());
           			   }
           			   
           			   int icheck=chkDoc.verificaCheck(varEnv.getUser());           			   
    				   
    				   if (icheck==1)
    					   throw new Exception("Accesso al documento ("+idDocument+") Fallito (errore check in)"+chkDoc.getErrorMessage());
    				   //Controllo check in / check out
    				   
    				   
    				   
           /* */       
           			   elpsTime = new ElapsedTime("ACCESSO AL DOCUMENTO "+idDocument,varEnv);
           
           			   elpsTime.start("********* ACCESSO AL DOCUMENTO *********","CARICAMENTO INFORMAZIONI INIZIALI");
           /* */       super.loadDocument();
           /* */	   elpsTime.stop();
           /* */	   
           /* */     }
           /* */     catch (Exception e) 
           /* */     {
        	   			   codeError=Global.CODERROR_NOT_DEFINED;
           /* */           varEnv.disconnectClose();
           /* */           throw new Exception("Accesso al documento ("+idDocument+") - Controllo delle competenze e check in/out\n"+e.getMessage());
           /* */     }
           /* */}
           // *************************************************************************************** //
           /* */
           // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
           /* */ else if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) {
           /* */      try {
           /* */        pantareiLogin();   
           /* */      }
           /* */      catch (Exception e) 
           /* */      {
           /* */        throw new Exception("AccediDocumento::Constructor() pantareiLogin\n"+e.getMessage());
           /* */      }
           /* */
           /* */      try {
           /* */        super.loadDocument();
           /* */      }
           /* */      catch (Exception e) 
           /* */      {
           /* */        throw new Exception("AccediDocumento::Constructor() load Documento HUMM\n"+e.getMessage());
           /* */      }                      
           /* */ }
           // *************************************************************************************** //
  }
  
  public void setControlloCompetenze(boolean bFlag) {
	     if (bFlag)
	    	 varEnv.byPassCompetenzeON();
	     else
	         varEnv.byPassCompetenzeOFF();
  }
  
  public void setLock(boolean bLock) {
	     aDocumento.setLock(bLock);
  }
  
  public void setTypeLock(String type) {
	     aDocumento.setTypeLock(type);
  }
  

  // ***************** METODI DI ACCESSO AL DOCUMENTO ***************** // 

  /*
   * METHOD:      accediFullDocumento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando l'oggetto 
   *              per la sua totalita
   * 
   * RETURN:      void
  */
  public void accediFullDocumento() throws Exception 
  {
         accediDocumento(ACCEDI_FULL);
  }

  /*
   * METHOD:      accediLogDocumento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando i valori
   *              dalla valori_log (solo i + recenti)
   * 
   * RETURN:      void
  */
  public void accediLogDocumento() throws Exception 
  {
         accediDocumento(ACCEDI_LOG);
  }
  
  /*
   * METHOD:      accediLogDocumento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando i valori
   *              dalla valori_log (solo i + recenti)
   * 
   * RETURN:      void
  */
  public void accediLogDocumento(String idL) throws Exception 
  {
	  	 idLog=idL;
         accediDocumento(ACCEDI_LOG);
  }  
  
  /*
   * METHOD:      accediDocumentoValori()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando solo
   *              i valori e non gli oggetti file
   * 
   * RETURN:      void
  */
  public void accediDocumentoValori() throws Exception 
  {
         accediDocumento(ACCEDI_VALORI);         
  }
  
  /*
   * METHOD:      accediDocumentoAllegati()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando 
   *              l'oggetto con i suoi file e non
   *              i suoi valori
   * 
   * RETURN:      void
  */
  public void accediDocumentoAllegati() throws Exception 
  {
         accediDocumento(ACCEDI_ALLEGATI);         
  }

  /*
   * METHOD:      accediAbstractDocumento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando 
   *              l'oggetto con il suo abstract
   * 
   * RETURN:      void
  */      
  public void accediAbstractDocumento() throws Exception 
  {
         accediDocumento(ACCEDI_ABSTRACT);         
  }
    
  /*
   * METHOD:      accediDocumento(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Accede ad un documento a seconda il tipo
   *              di stato
   * 
   * RETURN:      void
  */
  private void accediDocumento(String stato) throws Exception 
  {
          try { 
            // ************* SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************** //      
            if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
               elpsTime.start("********* ACCESSO AL DOCUMENTO *********","INIZIO ACCESSO");
               super.resetDocument(aDocumento); 
               if ( 
                    (stato.equals(ACCEDI_ABSTRACT) && aDocumento.retrieveAbstract()) ||
                    (stato.equals(ACCEDI_VALORI)   && aDocumento.retrieve(true, true, false,false,idLog))   ||
                    (stato.equals(ACCEDI_FULL)     && aDocumento.retrieve(true, true, true,false,idLog) ) ||
                    (stato.equals(ACCEDI_ALLEGATI) && aDocumento.retrieve(true, false, true,false,idLog)) || 
                    (stato.equals(ACCEDI_LOG)      && aDocumento.retrieve(true, true, true,true,idLog))
                  ) 
               { 
                  if (!stato.equals(ACCEDI_LOG)) 
                  {
	            	  this.setUltAggiornamento(aDocumento.getDataAggiornamento());
	                  if (aDocumento.getTipoDocumento().getTipoLog().equals(Global.TYPE_MAX_LOG) ||
	                	  aDocumento.getTipoDocumento().getTipoLog().equals(Global.TYPE_MAXVAL_LOG)) 
	                      try {
	                        aDocumento.logDocument(Global.TYPE_AZIONE_LETTURA);                        
	                      }
	                      catch (Exception e) {  
	                        throw new Exception("Errore nella scrittura del LOG\n" +e.getMessage());
	                      }
                  }
                  
                  //Gestisco la retrieve di tutte le competenze per tutti gli utenti se richiesto
                  if (setRetrieveAllAcl!=Profilo.NORETRIEVE_ALLACL) {
                	  GDM_Competenze gdmCom = new GDM_Competenze(varEnv);
        			  
                	  boolean bUser=false;
                	  if (setRetrieveAllAcl==Profilo.RETRIEVE_ALLACL_USER) bUser=true;
                	                  	
                	  Iterator i =  hsListAclToRetrieve.iterator();
                	  String aclList="";
                	  
                      while (i.hasNext()) {           	  
                    	   if (!aclList.equals("")) aclList+=";";
                    	   aclList+=""+i.next();                  	                      	   
                      }
                	  
                	  hmsACL=gdmCom.getElencoCompetenzeDocumento(new Abilitazioni(Global.ABIL_DOC,
                			  													  aDocumento.getIdDocumento(),
                			  													  aclList),bUser);
                  }
                  
                  dataCreazione=aDocumento.getDataCreazione();
               }
              elpsTime.stop(); 
            }
            // ************* SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD ************************** //
            else if( 
                    (stato.equals(ACCEDI_ABSTRACT) && aDocumento.retrieveAbstract()) ||
                    (stato.equals(ACCEDI_VALORI)   && aDocumento.retrieve(true, true, false,false,idLog))   ||
                    (stato.equals(ACCEDI_FULL)     && aDocumento.retrieve(true, true, true,false,idLog) ) ||
                    (stato.equals(ACCEDI_ALLEGATI) && aDocumento.retrieve(true, false, true,false,idLog)) 
                 ) {}
         }
         catch (Exception e) {
        	   codeError=aDocumento.getCodeError();
               varEnv.disconnectClose();
               throw new Exception("Accesso al documento ("+aDocumento.getIdDocumento()+") \n" +e.getMessage());
         }

         varEnv.disconnectClose();
  }

  /*
   * METHOD:      accediDocumentoAllegatiAndBody(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede ad un documento estraendo i
   *              suoi allegati con il tipo body
   *              
   *              Restituisce il vettore di OggettiFile
   * 
   * RETURN:      Vector
  */
  public Vector accediDocumentoAllegatiAndBody(String body) throws Exception 
  {      
         try { 
           try {
             this.accediDocumentoAllegati();
           }
           catch (Exception e)
           {
             throw new Exception("AccediDocumento::accediDocumentoAllegatiAndBody() accediDocumentoAllegati\n" +e.getMessage());
           }

           Vector v = aDocumento.getOggettiFile();
           
           Vector ret = new Vector();

           int index=-1;

           for(int i=0;i<v.size();i++) {
              if (((A_Oggetti_File)v.elementAt(i)).getFileName().equals(body)) {
                 index=i;
                 continue;
              }

              ret.addElement(((A_Oggetti_File)v.elementAt(i)).getIdOggettoFile());
           }

           if (index!=-1) {
              int k;
              byte[] buf2 = new byte[4096];
              OutputStream xOutputStream = new ByteArrayOutputStream(4096); 

              while ( (k=((InputStream)((A_Oggetti_File)v.elementAt(index)).getFile()).read(buf2) ) != -1) 
                    xOutputStream.write(buf2,0,k);
        
              String fileBodyString = "";

              fileBodyString = fileBodyString + xOutputStream.toString();
              ret.insertElementAt(fileBodyString,0);
             
           }
           
           return ret;
         }
         catch (Exception e)
         {
           varEnv.disconnectClose();
           throw new Exception("AccediDocumento::accediDocumentoAllegatiAndBody()\n" +e.getMessage());
         }
  }
  
  
  // ***************** METODI DI GESTIONE DEI VALORI  ***************** // 

  /*
   * METHOD:      aggiungiDati(Object,Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Data la coppia (campo,valore) questa viene inserita
   *              sulla lista dei valori
   *              Vale per tutti i DM.
   *              L'operazione non serve al fine dell'aggiornamento
   *              del dato stesso sul DB.
   *                    
   * RETURN:      void
  */  
  public void aggiungiDati(Object campo, Object valore) throws Exception
  {
         String idCampo = "-1";
         
         idCampo = campo.toString();

         if (valore==null) valore = "";
             
         try {
           aDocumento.addValore(idCampo, valore.toString());        
         }                  
         catch (Exception e)
         {
           throw new Exception("AggiungiDocumento::aggiungiDati() errore addValore\n"+ e.getMessage());
         }
  }

  // ***************** METODI DI GESTIONE DEI RIFERIMENTI ***************** //  
  
  /*
   * METHOD:      aggiungiRiferimento(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento riferimento di tipo "tipoRif"
   *              fra il documento attuale ed il documento "docRif"
   *              L'operazione non serve al fine dell'aggiornamento
   *              del riferimento stesso sul DB.
   * 
   * RETURN:      void
  */    
  public void aggiungiRiferimento(String docRif, String tipoRif) throws Exception
  {
         try {
           aDocumento.aggiungiRiferimento(docRif, tipoRif);
         }                  
         catch (Exception e)
         {
           throw new Exception("AccediDocumento::aggiungiRiferimento()\n"+ e.getMessage());
         }
  }

  /*
   * METHOD:      leggiRiferimenti()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la stringa così formata:
   *              idDocRif1@idDoc2Rif@....@idDocnRif
   * 
   * RETURN:      String
  */      
  public String leggiRiferimenti() 
  {
         String sRif="";
         Object oRife = aDocumento.getRiferimento();
         
         if (oRife != null)
         {
            int size = ((Vector)oRife).size();
            for(int i=0;i<size;i++) {
                // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
               if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) 
                  sRif += ((Related)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getNumber();
               // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA *********************** //                  
               else
                  sRif += ((Riferimento)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getDocRiferito().getDocNumber()
                          +","+
                          ((Riferimento)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getTipoRelazione();
                  ;
            
               if (i+1!=size) sRif += "@";
            }
         }
         
         return sRif;  
  }
  
  /*
   * METHOD:      leggiRiferimenti(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la stringa così formata:
   *              idDocRif1@idDoc2Rif@....@idDocnRif
   *              filtrata per (Area,TipoRelazione) 
   * RETURN:      String
  */      
  public String leggiRiferimenti(String area,String tipoRel) 
  {
         String sRif="";
         Object oRife = aDocumento.getRiferimento();
         
         if (oRife != null)
         {
            int size = ((Vector)oRife).size();
            for(int i=0;i<size;i++) {
                // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
               if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM))
               {	   
                  sRif += ((Related)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getNumber();
                  if (i+1!=size) sRif += "@";
               }
               // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA *********************** //                  
               else
               {  
            	 if((area.equals(((Riferimento)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getArea()))&& (tipoRel.equals(((Riferimento)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getTipoRelazione())))
            	 {	  
            	   sRif += ((Riferimento)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getDocRiferito().getDocNumber()
                          +","+
                          ((Riferimento)(((Vector)aDocumento.getRiferimento()).elementAt(i))).getTipoRelazione();
                   ;
                  if (i+1!=size) sRif += "@";
            	 }
               }             
               
            }
         }
         if(sRif.substring(sRif.length()-1,sRif.length()).equals("@"))
           sRif=sRif.substring(0,sRif.length()-1);
         
         return sRif;  
  }
  
  
  
  /*
   * METHOD:      leggiRiferimentiFrom()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la stringa così formata:
   *              idDocRif1@idDoc2Rif@....@idDocnRif
   * 
   * RETURN:      String
  */      
  public String leggiRiferimentiFrom() 
  {
         String sRif="";
         Object oRife = aDocumento.getRiferimentoFrom();
         
         if (oRife != null)
         {
            int size = ((Vector)oRife).size();
            for(int i=0;i<size;i++) {
                // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
               if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) 
                  sRif += ((Related)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getNumber();
               // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA *********************** //                  
               else
                  sRif += ((Riferimento)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getDocPrincipale().getDocNumber()
                          +","+
                          ((Riferimento)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getTipoRelazione();
                  ;
            
               if (i+1!=size) sRif += "@";
            }
         }
         
         return sRif;  
  }
  
  /*
   * METHOD:      leggiRiferimentiFrom()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la stringa così formata:
   *              idDocRif1@idDoc2Rif@....@idDocnRif
   * 
   * RETURN:      String
  */      
  public String leggiRiferimentiFrom(String area,String tipoRel) 
  {
         String sRif="";
         Object oRife = aDocumento.getRiferimentoFrom();
         
         if (oRife != null)
         {
            int size = ((Vector)oRife).size();
            for(int i=0;i<size;i++) {
                // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
               if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) { 
                  sRif += ((Related)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getNumber();
                  if (i+1!=size) sRif += "@";
               }
               // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA *********************** //                  
               else {
            	  if((area.equals(((Riferimento)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getArea()))&& (tipoRel.equals(((Riferimento)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getTipoRelazione()))) { 
	            	   
	                  sRif += ((Riferimento)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getDocPrincipale().getDocNumber()
	                          +","+
	                          ((Riferimento)(((Vector)aDocumento.getRiferimentoFrom()).elementAt(i))).getTipoRelazione();
	                  ;
	            
	                  if (i+1!=size) sRif += "@";
            	  }
               }
            }
         }
         
         return sRif;  
  }  
  
  public Hashtable leggiLinks(String area,String cm) throws Exception {
	     connect();
	     
	     Hashtable vRet;
	  	 
	  	 try {
	  	   aDocumento.retrieveLinks(area,cm);
	  	   
	  	   vRet=aDocumento.getLinks();
	  	   
	  	   disconnect();	  	   	 
	  		 
	  	   return vRet;
	  	 }
	  	 catch(Exception e) {	  		 
	  	   try {disconnect();}catch(Exception ei) {}
	  	   throw new Exception("AccediDocumento::leggiLinks("+area+","+cm+"): \n"+ e.getMessage());
	  	 }	  	 
  }
  
  /*
   * METHOD:      getListaFigli()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il vettore di figli del documento
   * 
   * RETURN:      String
  */      
  private Vector getListaFigliDiscendenti(int liv) throws Exception
  {  
	  	 connect();
	  	 Vector vRet;
	  	 
	  	 try {
	  	   vRet=aDocumento.listaDiscendenti(liv);
	  	   
	  	   disconnect();
	  	   
	  	   return vRet;
	  	 }
	  	 catch(Exception e) {	  		 
	  	   try {disconnect();}catch(Exception ei) {}
	  	   throw new Exception("AccediDocumento::getListaFigliDiscendenti("+liv+"): \n"+ e.getMessage());
	  	 }
  }
  
  public Vector<ValoriLogStruct> getListaValoriLog() throws Exception {	  		    
	  	 connect();	  
	  	 Vector<ValoriLogStruct> vRet;
	  	 
	  	 try {
	  	   vRet=aDocumento.retrieveListaValoriLog();
	  	   
	  	   disconnect();
	  	   
	  	   return vRet;
	  	 }
	  	 catch(Exception e) {	  		 
	  	   try {disconnect();}catch(Exception ei) {}
	  	   throw new Exception("AccediDocumento::getListaValoriLog(): \n"+ e.getMessage());
	  	 }
  }
  
  public Vector getListaFigli() throws Exception
  {	  	 
	  	 return getListaFigliDiscendenti(1);	  	 
  }
  
  public Vector getListaDiscendenti() throws Exception
  {	  	 
	  	 return getListaFigliDiscendenti(0);	  	 
  }  
  
 /*
   * METHOD:      getVectorRiferimenti()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il vettore dei related
   * 
   * RETURN:      String
  */      
  public Vector getVectorRiferimenti() {
         return (Vector)aDocumento.getRiferimento();
  }
  
   /*
   * METHOD:      getVectorRiferimentiFrom()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il vettore dei relatedFrom
   * 
   * RETURN:      String
  */      
  public Vector getVectorRiferimentiFrom() {
         return (Vector)aDocumento.getRiferimentoFrom();
  }
  
  // ***************** METODI DI LETTURA DELLE INFORMAZIONI (VALORI) RECUPERATE ***************** //  
  
  /*
   * METHOD:      leggiValoreCampo(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Legge il valore di un campo "sNome" del documento                
   * 
   * RETURN:      String
  */    
  public String leggiValoreCampo(String sNome) throws  Exception
  {
         A_Valori aVal;
         Vector valori ;
         int conta=0,size;
         String nomeCampo = sNome;
         
         //Gestione del Mapping
         try {        	          	 	
               nomeCampo = varEnv.getGDMapping().getMappingCampo(aDocumento.getTipoDocumento().getNome(),true,nomeCampo);
         }
         catch (Exception e) {
           throw new Exception("AccediDocumento::leggiValoreCampo() mapping "+nomeCampo+": \n"+ e.getMessage());
         }
      
         valori =  aDocumento.getValori();
         
         //Scorrimento dei valori 
         //per la ricerca
         //del campo
         size=valori.size();
         while (conta!=size) {
              aVal = (A_Valori) valori.elementAt(conta++);
  
              if ( aVal.getCampo().getNomeCampo().equals(nomeCampo)){
            	  if (aVal.getValore()==null)
            		  return null;
            	  else
            		  return aVal.getValore().toString();
              }            	                     
         }
  
         return null;
  }
  

  // ***************** METODI DI LETTURA DELLE INFORMAZIONI (OGGETTI_FILE) RECUPERATE ***************** //  

  /*
   * METHOD:      listaOggettiFile()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il vettore degli oggetti file              
   * 
   * RETURN:      Vector
  */
  public Vector listaOggettiFile() 
  {
         return aDocumento.getOggettiFile();
  }
  
  public Vector listaValori()   
  {
         return aDocumento.getValori();
  }

  /*
   * METHOD:      listaIdOggettiFile()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il vettore degli id oggetti file              
   * 
   * RETURN:      Vector
  */  
  public Vector listaIdOggettiFile() 
  {
      Vector idOgg = new Vector();
      int conta=0,  size;
      size=aDocumento.getOggettiFile().size();
      while (conta!=size) {
          idOgg.addElement(((A_Oggetti_File)aDocumento.getOggettiFile().elementAt(conta++)).getIdOggettoFile());
      }

      return idOgg;
  }

  /*
   * METHOD:      nomeOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'id oggetto file restituisce
   *              il nome del file
   * 
   * RETURN:      String
  */
  public String nomeOggettoFile(String idOggettoFile) 
  {
       A_Oggetti_File aOgg;
       Vector oggetti;
       int conta=0,  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       while (conta!=size) { 
            aOgg = (A_Oggetti_File)oggetti.elementAt(conta++);

            if (aOgg.getIdOggettoFile().equals(idOggettoFile))
                return aOgg.getFileName();
       }
       
       return null;
  }
  
  /*
   * METHOD:      nomeOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'id oggetto file restituisce
   *              il nome del file
   * 
   * RETURN:      String
  */
  public String formatoOggettoFile(String idOggettoFile) 
  {
       A_Oggetti_File aOgg;
       Vector oggetti;
       int conta=0,  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       while (conta!=size) { 
            aOgg = (A_Oggetti_File)oggetti.elementAt(conta++);

            if (aOgg.getIdOggettoFile().equals(idOggettoFile))
                try {
                  return aOgg.getIdFormato();
                }
                catch (Exception e)
                {
                  return "";
                }
       }
       
       return null;
  }  
  
  /*
   * METHOD:      nomeOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'id oggetto file restituisce
   *              il nome del file
   * 
   * RETURN:      String
  */
  public String nomeFormatoOggettoFile(String idOggettoFile) 
  {
       A_Oggetti_File aOgg;
       Vector oggetti;
       int conta=0,  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       while (conta!=size) { 
            aOgg = (A_Oggetti_File)oggetti.elementAt(conta++);

            if (aOgg.getIdOggettoFile().equals(idOggettoFile))
                try {
                  return (new LookUpDMTable(varEnv)).lookUpFormatoById(aOgg.getIdFormato());
                }
                catch (Exception e)
                {
                  return "";
                }
       }
       
       return null;
  }    
  
  /*
   * METHOD:      leggiOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'id oggetto file restituisce
   *              il file
   * 
   * RETURN:      InputStream
  */    
  public InputStream leggiOggettoFile(String idOggettoFile)  throws Exception
  {
       A_Oggetti_File aOgg;
       Vector oggetti ;
       int conta=0,  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       while (conta!=size) { 
             aOgg = (A_Oggetti_File) oggetti.elementAt(conta++);

            if (aOgg.getIdOggettoFile().equals(idOggettoFile))
                return (InputStream)aOgg.getFile();
       }
    
       return null;
  }

  /*
   * METHOD:      isOggettoFileVisibile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'id oggetto file restituisce
   *              TRUE  -> se il file è visibile
   *              FALSE -> se il file non è visibile
   * 
   * RETURN:      boolean
  */    
  public boolean isOggettoFileVisibile(String idOggettoFile) throws Exception
  {
       A_Oggetti_File aOgg;
       Vector oggetti;
       int conta=0, size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       while (conta!=size) { 
            aOgg = (A_Oggetti_File) oggetti.elementAt(conta++);

            if (aOgg.getIdOggettoFile().equals(idOggettoFile))                
                try {
                  return aOgg.getIsVisible(); 
                }
                catch (Exception e) {
                  throw new Exception("AccediDocumento::isOggettoFileVisibile()\n" + e.getMessage());
                }
                
       }
       
       return false;
  }
  
  /*
   * METHOD:      esimoNomeFile(int)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'indice "progr" del vettore
   *              di oggetti file, ne restituisce il nome
   * 
   * RETURN:      String
  */
  public String esimoNomeFile(int progr) 
  {
       Vector oggetti ;
       int  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       if (progr > size)
          return null;
       else
          return ((A_Oggetti_File) oggetti.elementAt(progr-1)).getFileName();
  }

  /*
   * METHOD:      esimoNomeFile(int)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Dato l'indice "progr" del vettore
   *              di oggetti file, ne restituisce il file
   * 
   * RETURN:      InputStream
  */  
  public InputStream esimoOggettoFile(int progr)  throws Exception
  {
       A_Oggetti_File aOgg;
       Vector oggetti ;
       int  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       if (progr > size)
          return null;
       else
       {
         aOgg = (A_Oggetti_File) oggetti.elementAt(progr-1);
         return (InputStream)aOgg.getFile();
       }
  }
  
  /*
   * METHOD:      caricaOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: 
   * 
   * RETURN:      InputStream
  */  
  public void caricaOggettoFile(String idOgfi) throws Exception {             
       
       //Ora cerco l'id e ne faccio la retrieve
       A_Oggetti_File aOgg;
       Vector oggetti ;
       int conta=0,  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       while (conta!=size) { 
            aOgg = (A_Oggetti_File) oggetti.elementAt(conta++);

            if (aOgg.getIdOggettoFile().equals(idOgfi))                
                try {
                  aOgg.retrieve(); 
                  break;
                }
                catch (Exception e) {
                  throw new Exception("AccediDocumento::caricaOggettoFile() Errore in retrieve\n" + e.getMessage());
                }                
       }
       
   }

  public String getConservazione() {
	     return aDocumento.getConservazione();
  }
  
  public String getArchiviazione() {
	     return aDocumento.getArchiviazione();
  }    
  
  public void connect() throws Exception {
	     //Per prima cosa mi riconnetto se non lo sono
         if (varEnv.getDbOp()==null) 
            try {
              varEnv.connect();
              bIsNew=true;
            }
            catch (Exception e) {
              throw new Exception("Accesso al documento ("+aDocumento.getIdDocumento()+") - Errore di connessione\n" + e.getMessage());
            }
  }
  
  public void disconnect() throws Exception {
	     if (bIsNew) 
	    	 try {	    		 
	           varEnv.disconnectClose();
	         }
             catch (Exception e) {
               throw new Exception("Accesso al documento ("+aDocumento.getIdDocumento()+") - Errore di disconnessione\n" + e.getMessage());
             }
  }

  public String getCodeError() {
		 return codeError;
  }

  public void setSetRetrieveAllAcl(int setRetrieveAllAcl) {
	     this.setRetrieveAllAcl = setRetrieveAllAcl;
  }

  public HashMapSet getHmsACL() {
	     return hmsACL;
  }

  public void setHsListAclToRetrieve(HashSet<String> hs) {
	     this.hsListAclToRetrieve = hs;
  }
  
  public String getDataCreazione() {
	     return dataCreazione;
  }
  
  public boolean isCompetenzeAllegati()  {	     	     
		 return aDocumento.isCompetenzeAllegati();
  }

  public long getIdLog()  {
        return aDocumento.getLastIdLog();
  }
}
package it.finmatica.dmServer.management;

/*
 * GESTIONE COMPLETA DI UN DOCUMENTO
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   14/09/2005
 * 
 * */

import java.util.*;
import it.finmatica.dmServer.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.motoreRicerca.*;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.wsPantarei.*;
import it.finmatica.jfc.utility.DateUtility;
import it.finmatica.jfc.io.*;

public class RicercaDocumento extends ManageDocumento
{
  // varibili  
  A_Condizioni conds;
  String idTipoDocumento = "0"; 
  String sTipoDocumento = null;
  String xmlNote = null;
  
  private int 		queryTimeOut;   
  private boolean   bQueryTimeOut=false;
  
  Vector users;  
  Vector groupUsers;  

  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
      
  /*
   * METHOD:      Constructor(Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Accede al documento caricando l'oggetto 
   *              per la sua totalita. Utilizza il motore 
   *              di ricerca per ricercare
   *              Se collegati al DM HummingBird viene 
   *              effettuato il login tramite il ws di pantarei
   * 
   * RETURN:      none
  */ 
  public RicercaDocumento(Environment vEnv) throws Exception 
  {        
         super("0",vEnv);
         
         try {
           
           
           String DM;
  
           // PER LA RICERCA USO SEMPRE LA CLASSE GD4_RICERCA
           if (vEnv.Global.DM.equals(vEnv.Global.HUMMINGBIRD_DM))
              DM=vEnv.Global.FINMATICA_DM;
           else
              DM=vEnv.Global.DM;
  
           this.varEnv = vEnv;
           users = new Vector();
           groupUsers = new Vector();
           conds = (A_Condizioni)Class.forName(vEnv.Global.PACKAGE + ".motoreRicerca." + 
                                 DM + "_" + vEnv.Global.CONDIZIONI).newInstance();
 
           conds.inizializzaCondizioni();
          // ************* SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD ************************ //
           /* */ if (vEnv.Global.DM.equals(vEnv.Global.HUMMINGBIRD_DM)) 
           /* */    try {
           /* */      pantareiLogin();
           /* */    }
           /* */    catch (Exception e) 
           /* */    {
           /* */      throw new Exception("RicercaDocumento::costructor(@) pantareiLogin\n"+e.getMessage());
           /* */    }
           // *************************************************************************************** //
         }
         catch (Exception e) 
         {
            vEnv.disconnectClose();
            throw new Exception("RicercaDocumento::costructor(@) \n"+e.getMessage());
         }
  }

  /*
   * METHOD:      Constructor(String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Creazione di un documento a partire
   *              dall'idTipoDocumento e variabili
   *              di ambiente
   * 
   * 
   * RETURN:      none
  */ 
  public RicercaDocumento(String tipoDocumento, Environment vEnv) throws Exception 
  {             
         this(tipoDocumento,  "",  vEnv);
  }
  
  /*
   * METHOD:      Constructor(String,String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Creazione di un documento a partire
   *              dall'idTipoDocumento, area e variabili
   *              di ambiente
   * 
   * RETURN:      none
  */ 
  public RicercaDocumento(String tipoDocumento, String sArea, Environment vEnv) throws Exception 
  {    
         this(vEnv);
         
         try {
           this.inizializza(tipoDocumento, sArea, vEnv);  
         }
         catch (Exception e) 
         {
            vEnv.disconnectClose();          
         }
  }

 /*
   * METHOD:      inizializza(String,String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Crea un documento nuovo e tentando
   *              di recuperare la libreria dal tipoDocumento
   * 
   * RETURN:      void
  */
  public void inizializza(String tipoDocumento, String sArea, Environment vEnv) throws Exception 
  {           
         sTipoDocumento = tipoDocumento;
         String sTipoDocDM="";
         String sTipoLibreria="";


         try {
           sTipoDocDM = vEnv.getGDMapping().getMappingTipoDoc(tipoDocumento);
         }
         catch (Exception e) 
         {
           idTipoDocumento=null;
           //throw new Exception("RicercaDocumento::costructor(@,@) mapping\n"+e.getMessage());
         }
         
         ModelInformation mi = null;
         try {
           mi = (new LookUpDMTable(varEnv)).lookUpTipoDoc(sTipoDocDM, sArea);
         }
         catch (Exception e) 
         { 
           idTipoDocumento=null;
           //throw new Exception("RicercaDocumento::costructor(@,@) lookUp\n"+e.getMessage());
         }

         try { 
           //int i = sTipoLibreria.indexOf("@");
           idTipoDocumento = mi.getIdTipoDoc();//sTipoLibreria.substring(0,i);
         }
         catch (Exception e) {           
           idTipoDocumento=null;
           //throw new Exception("RicercaDocumento::costructor(@,@)\n"+ e.getMessage());
         }  

         if ( (sArea != null) && (!sArea.equals("")) )
             conds.setArea(sArea);            
        /*if (idTipoDocumento.equals("0")) 
             throw new Exception("RicercaDocumento::settaChiavi() Manca il tipo documento");
         else*/
         conds.setIdTipoDoc(idTipoDocumento);
            
  }

  // ***************** METODI DI GETS E SET ***************** // 

 /*
   * METHOD:      settaChiavi(Object,Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta le chiavi di ricerca data 
   *              la coppia (campo, valore)
   * 
   * RETURN:      void
  */
  public void settaChiavi(Object campo, Object valoreCampo) throws Exception 
  { 
        settaChiavi( campo,  valoreCampo, "") ;
  }
  
  /*
   * METHOD:      settaChiavi(Object,Object,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta le chiavi di ricerca data 
   *              la coppia (campo, valore)e l'operatore
   * 
   * RETURN:      void
  */
  public void settaChiavi(Object campo, Object valoreCampo, String operatore) throws Exception 
  { 
       String idCampo;
       if (campo instanceof String)
       {
           String sCampo="";
           try {           
             sCampo = varEnv.getGDMapping().getMappingCampo(sTipoDocumento,false,campo.toString());
           }
           catch (Exception e) {
             throw new Exception("RicercaDocumento::settaChiavi() mapping\n"+ e.getMessage());
           }  

           try {
              idCampo = (new LookUpDMTable(varEnv)).lookUpCampi(sCampo, idTipoDocumento);
           }
           catch (Exception e) {
             throw new Exception("RicercaDocumento::settaChiavi() lookUp\n"+ sCampo + " " + e.getMessage());
           } 
       }   
       else
           idCampo = campo.toString();
 
       conds.addInListaCondizioniCampi(idCampo+"", valoreCampo, operatore) ;
   
  }

  /*
   * METHOD:      settaDati(Object,Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta i dati data la
   *              coppia (dato, valore)
   * 
   * RETURN:      void
  */
  public void settaDati(Object sDato, Object valoreDato) throws Exception 
  {   
         //MAPPING DA FARE
         settaDati(sDato, valoreDato, "");
  }

 /*
   * METHOD:      settaDati(Object,Object,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta i dati data la
   *              coppia (dato, valore) e operatore
   * 
   * RETURN:      void
  */
  public void settaDati(Object sDato, Object valoreDato, String operatore) throws Exception 
  {   
         //MAPPING DA FARE
         conds.addInListaCondizioniDati(sDato+"", valoreDato, operatore);
  }

  /*
   * METHOD:      aggiungiUtente(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge un utente nel DB
   * 
   * RETURN:      void
  */
  public void aggiungiUtente(String user, String isUserOrGroup) 
  {
         users.add(user);
         groupUsers.add(isUserOrGroup);
  }

  /*
   * METHOD:      settaFileName(Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta il nome del file
   * 
   * RETURN:      void
  */
  public void settaFileName(Object sFileName) throws Exception 
  {   
         conds.setFileName(sFileName);
  }

  /*
   * METHOD:      settaCondizioni(int,Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta le condizioni
   * 
   * RETURN:      void
  */
  public void settaCondizioni(int tipoCondizione, Object condizione) throws Exception 
  {          
         conds.addCondizione(tipoCondizione,(String)condizione);
  }

 /*public void settaArea(String newArea)
  {
         conds.setArea(newArea);
         conds.setIdTipoDoc(idTipoDocumento);
  }*/

  /*
   * METHOD:      settaCodiceRichiesta(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta il codice richiesta 
   * 
   * RETURN:      void
  */
  public void settaCodiceRichiesta(String newCodRich)
  {
         conds.setRichiesta(newCodRich);
         conds.setIdTipoDoc(idTipoDocumento);
  }
  
  public void setQueryTimeOut(int time) {	
	  	 queryTimeOut=time;
  }
  
  // ***************** METODI DI RICERCA ***************** // 
  
  /*
   * METHOD:      ricercaBozza()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Ricerca un documento con 
   *              stato bozza
   * 
   * RETURN:      Vector
  */
  public Vector ricercaBozza() throws Exception
  { 
         // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************ //         
         /* */ return ricercaStato(Global.STATO_BOZZA);
         // *************************************************************************** //
  }
  
  /*
   * METHOD:      ricercaCompleto()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Ricerca un documento con 
   *              stato completo
   * 
   * RETURN:      Vector
  */
  public Vector ricercaCompleto() throws Exception
  { 
         // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************ //         
         /* */ return ricercaStato(Global.STATO_COMPLETO);
         // *************************************************************************** //   
  }
  
  /*
   * METHOD:      ricercaStato(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Ricerca un documento con 
   *              stato dato in input
   * 
   * RETURN:      Vector
  */
  public Vector ricercaStato(String stato) throws Exception
  { 
         // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************ //         
         /* */    Ricerca r = new Ricerca(conds,varEnv);
         /* */    try
         /* */    {
         /* */      UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(), varEnv.getPwd(),  varEnv.getUser(), varEnv);
         /* */      Abilitazioni abil  = new Abilitazioni(Global.ABIL_DOC, "ID_DOCUMENTO" , "L");
         /* */      r.setVerificaCompetenze(ua,abil);
         /* */      r.fillDocumentListStato(stato);
         /* */   }
         /* */    catch  (Exception e){
         /* */      varEnv.disconnectClose();
         /* */      throw new Exception("RicercaDocumento::ricercaStato("+stato+")\n"+e.getMessage());
         /* */    }
         /* */
         /* */    varEnv.disconnectClose();
         /* */    //xmlNote = r.getQuery();
         /* */    return r.getDocumentList(); 
         // *************************************************************************** //
         
  }
  
  /*
   * METHOD:      ricerca()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Effettua la ricerca
   * 
   * RETURN:      Vector
  */
  public Vector ricerca() throws Exception
  { 
         // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************ //
         /* */ if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
         /* */    Ricerca r = new Ricerca(conds,varEnv);
         /* */	  boolean bAlive=false;
         /* */    try {  
         /* */          UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(), varEnv.getPwd(),  varEnv.getUser(), varEnv);
         /* */          Abilitazioni abil  = new Abilitazioni(Global.ABIL_DOC, "ID_DOCUMENTO" , "L");
         /* */        				
         /* */          r.setVerificaCompetenze(ua,abil);
         /* */			r.start();
         /* */			r.join(queryTimeOut);
         /* */
         /* */			while (!varEnv.Global.bExitThreadRicerca) 
         /* */			 	 	if (r.isAlive()) {        	         	 
         /* */						bAlive=true;         
         /* */						bQueryTimeOut=true;
         /* */						r.resetDocumentList();    
         /* */                      break;         					
         /* */					}         		
         /* */
         /* */			varEnv.Global.bExitThreadRicerca=true;
         /* */				
         /* */			if (!bAlive && r.getError()!=null)          					
         /* */	 			throw new Exception(r.getError());
         /* */			
         /* */    }
         /* */    catch (Exception e) {        	 			
         /* */			if (!bAlive) {
         /* */ 				varEnv.disconnectClose();
         /* */ 				throw new Exception("RicercaDocumento::ricerca() FINMATICA-PART\n"+e.getMessage());
         /* */ 			}         
         /* */    }
         /* */
         /* */    varEnv.disconnectClose();
         /* */    return r.getDocumentList(); 
         /* */ }
         // *************************************************************************** //

         // ************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD ************ //
         /* */ else if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) {
         /* */   StringBuffer sXml = new StringBuffer();
         /* */
         /* */   sXml.append(creaXml("TESTA"));
         /* */   sXml.append(creaXml("BODY"));
         /* */   sXml.append(creaXml("CODA"));   
         /* */
         /* */   getDocumentsRet eRet=null;
         /* */
         /* */   try {
         /* */     DMPantaReiStub dms = new DMPantaReiStub(varEnv.Global.WEB_HOST_SERVICE_PANTA);
         /* */
         /* */     //Attivata la dir di debug
         /* */     if (!varEnv.Global.DIR_XML_MIME.equals("")) {
         /* */            String now;
         /* */            
         /* */            now=DateUtility.getTodayStringDate("dd-MM-yyyy-hh-mm-ss");
         /* */            LetturaScritturaFileFS f = new LetturaScritturaFileFS(varEnv.Global.DIR_XML_MIME+"\\GetDocumentsProfile_"+now+".xml");
         /* */            f.scriviFile(new java.io.ByteArrayInputStream(sXml.toString().getBytes()));
         /* */     }
         /* */
         /* */     eRet = dms.getDocuments(varEnv.getLibrary(),varEnv.getUser(),
         /* */                            ((LoginRet)token).getStrDST(), 
         /* */                             sXml.toString(),varEnv.Global.DIR_XML_MIME);
         /* */
         /* */     if (eRet.getLngErrNumber().longValue()!=0) 
         /* */         if (eRet.getLngErrNumber().longValue()!=1)
         /* */            throw new Exception("PANTAERROR:" + eRet.getLngErrNumber().longValue() + " -- " + eRet.getStrErrString());        
         /* */         else
         /* */            throw new Exception(eRet.getLngErrNumber().longValue() + " - " + eRet.getStrErrString());         
         /* */   }
         /* */   catch (Exception e)
         /* */   {
         /* */     throw new Exception("RicercaDocumento::ricerca() HUMM-PART (getDocuments)\n"+e.getMessage());
         /* */   }
         /* */   
         /* */   Vector lRet = new Vector();
         /* */
         /* */   lRet.addElement(eRet.getXml());
         /* */
         /* */   return lRet;
         /* */ }
         // *************************************************************************** //

         return null;
  }
 
 // ***************** METODI DI GESTIONE DOCUMENTI XML ***************** //
  
  public Object getXMLDocument(Vector documenti) throws Exception
  {
          GD4_Documento_XML gdx = new GD4_Documento_XML(documenti, xmlNote,varEnv);
          return gdx.getDocumentoXML();
  }
  
  public String getXMLString(Vector documenti) throws Exception
  {
          GD4_Documento_XML gdx = new GD4_Documento_XML(documenti, xmlNote,varEnv);
          return gdx.visualizza();   
  }
  
  public boolean getQueryTimeOut() {
	  	 return bQueryTimeOut;
  }

 
  public String creaXml(String tipo) throws Exception
  {
          StringBuffer sXml = new StringBuffer();

          if (tipo.equals("TESTA")) {
              sXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
              sXml.append("<DOC_INFO xmlns:xsi='http://www.w3.org/2000/10/XMLSchema-instance'");
              sXml.append(" xsi:noNamespaceSchemaLocation=\"doc_info_v1.4.1.xsd\">");
             
          }
          else if (tipo.equals("BODY"))
          {   
             sXml.append("<DOC_DESCR>");          
             
             sXml.append("<PROFILE_INFOS>");
             if (idTipoDocumento!= null)
                sXml.append("<PROFILE_FORM_NAME name=\""+ idTipoDocumento +"\"/>");
             for(int i=0;i<conds.getListaCondizioniCampi().size();i++)
                 sXml.append("<PROFILE_INFO name=\""+
                            conds.getIdCampoLista(i)+"\" value=\""+
                            conds.getValoreCampoLista(i)+"\"/>");  
             sXml.append("</PROFILE_INFOS>");

             if (users.size()!=0) sXml.append("<USERGROUPS>"); 

             for(int i=0;i<users.size();i++)
                sXml.append("<USERGROUP name=\""+groupUsers.get(i)+"\" value=\""+users.get(i)+"\" />");

             if (users.size()!=0) sXml.append("</USERGROUPS>"); 
             
             sXml.append("</DOC_DESCR>");
          }
          else {
                sXml.append("</DOC_INFO>");
          }

          return sXml.toString();
  }

}
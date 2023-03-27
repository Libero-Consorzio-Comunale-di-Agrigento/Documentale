package it.finmatica.dmServer;

/*
 * GESTIONE DEI DOCUMENTI
 * NEL DM DI HUMMINGBIRD
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.jfc.utility.DateUtility;
import java.util.*;

import it.finmatica.wsPantarei.*;
import it.finmatica.jfc.io.*;
import java.io.*;
import it.finmatica.dmServer.util.*;

public class Humm_Documento extends A_Documento
{
  // variabili private
  private Environment varEnv;
  private String sSearchXML;
  private InputStream fileP7M;

 // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
 /*
   * METHODS:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  
  public Humm_Documento()
  {
  }


  public Humm_Documento(A_Documento oDoc) {
         this();
  }

 /*
   * METHOD:      inizializzaDati(Object, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  public void inizializzaDati(Object vUtente) throws Exception
  {
         this.inizializzaDati((Environment)vUtente);         
  }

  /*
   * METHOD:      inizializzaDati(Environment)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  private void inizializzaDati(Environment vUtente) throws Exception
  {
         // Crea la libreria del DM da utilizzare
         try {
           libreria = (A_Libreria)Class.forName(vUtente.Global.PACKAGE + 
                                                "." + vUtente.Global.DM + 
                                                "_" + vUtente.Global.LIBRERIA).newInstance();
         }
         catch (Exception e) {
               throw new Exception("Humm_Documento::inizializzaDati() Non riesco a creare l'oggetto di Classe: " + 
                                   vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_" + vUtente.Global.LIBRERIA);
         }

         // Crea il tipo documento del DM da utilizzare
         try {
           tipoDocumento = (A_Tipo_Documento)Class.forName(vUtente.Global.PACKAGE + 
                                                           "." + vUtente.Global.DM + 
                                                           "_" + vUtente.Global.TIPODOC).newInstance();
         }
         catch (Exception e) {
               throw new Exception("Humm_Documento::inizializzaDati() Non riesco a creare l'oggetto di Classe: " + 
                                  vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_" + vUtente.Global.TIPODOC);
         }        

         // Crea la lista (vuota) dei valori del documento
         valori = new Vector();
         // Crea la lista (vuota) degli oggetti file del documento
         oggettiFile = new Vector();        
         related = new Vector();
         // Crea la lista (vuota) degli ACL del documento
         vACL = new Vector();

         this.varEnv = vUtente;
        
  }
  
  // ***************** METODI DI GESTIONE DEI DOCUMENTI ***************** //
  
  /*
   * METHOD:      insertDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce un documento chiamando il web services
   *              Import di pantarei
   * 
   * RETURN:      boolean
  */  
  public boolean insertDocument(String stato) throws Exception
  { 
       try {
         DMPantaReiStub dms = new DMPantaReiStub(varEnv.Global.WEB_HOST_SERVICE_PANTA);
         ImportRet iRet;
         String xmlInsert;
         
         xmlInsert=creaInsertXml();
         
         //Attivata la dir di debug
         if (!varEnv.Global.DIR_XML_MIME.equals("")) {
             String now;
             
             now=DateUtility.getTodayStringDate("dd-MM-yyyy-hh-mm-ss");
             LetturaScritturaFileFS f = new LetturaScritturaFileFS(varEnv.Global.DIR_XML_MIME+"\\ImportProfile_"+now+".xml");
             f.scriviFile(new ByteArrayInputStream(xmlInsert.getBytes()));
         }

         iRet = dms.Import(((LoginRet)token).getStrLibraryName(),
                           varEnv.getUser(),((LoginRet)token).getStrDST(),
                           xmlInsert,(InputStream)getOggettoFile(0),fileP7M,varEnv.Global.DIR_XML_MIME);

         try {
           ((InputStream)getOggettoFile(0)).close(); 
           fileP7M.close();
         }
         catch (Exception e) {}

         if (iRet.getLngErrNumber().longValue()==0) {
            this.setIdDocumento(iRet.getLngDocID()+"");
            return true;     
         }
         else 
            // Significa che è un errore tornato dal file xml
            // e quindi non dovuto a java ma puramente ad Humminbird
            if (iRet.getLngErrNumber().longValue()!=1)
                throw new Exception("PANTAERROR:" + iRet.getLngErrNumber().longValue() + " -- " + iRet.getStrErrString());        
            else
                throw new Exception(iRet.getLngErrNumber().longValue() + " - " + iRet.getStrErrString());         

       
       }
       catch (Exception e) 
       {
          throw new Exception("Humm_Documento::insertDocument()\n"+e.getMessage());
       }
       
  }

  /*
   * METHOD:      updateDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce un documento chiamando il web services
   *              Modify di pantarei
   *              
   * RETURN:      boolean
  */  
  public boolean updateDocument() throws Exception
  {
       try {
         DMPantaReiStub dms = new DMPantaReiStub(varEnv.Global.WEB_HOST_SERVICE_PANTA);
         ModifyRet mRet;       
         Integer intModType;
         Object objDocument;
         if (this.getValori().size()*this.getOggettiFile().size() == 0)  {
            if (this.getValori().size()+this.getOggettiFile().size() == 0) 
                throw new Exception("Mancano i dati per aggiornare");
            else{
                if (this.getValori().size() != 0) {
                  intModType = new Integer(1);
                  objDocument = null;
                }
                else {
                    intModType = new Integer(2);
                    objDocument = this.getOggettoFile(0);
                }
            }
         }
         else{
            intModType = new Integer(3);
            objDocument = this.getOggettoFile(0);
         }
        String s=creaModifyXml();
        
        //Attivata la dir di debug
         if (!varEnv.Global.DIR_XML_MIME.equals("")) {
             String now;
             
             now=DateUtility.getTodayStringDate("dd-MM-yyyy-hh-mm-ss");
             LetturaScritturaFileFS f = new LetturaScritturaFileFS(varEnv.Global.DIR_XML_MIME+"\\ModifyProfile_"+now+".xml");
             f.scriviFile(new ByteArrayInputStream(s.getBytes()));
         }
        
        mRet = dms.Modify(((LoginRet)token).getStrLibraryName(), new Long(this.getIdDocumento()),
                           varEnv.getUser(),((LoginRet)token).getStrDST(),
                           intModType, s, objDocument, varEnv.Global.DIR_XML_MIME);

         if (mRet.getLngErrNumber().longValue()==0)
            return true;     
         else 
            // Significa che è un errore tornato dal file xml
            // e quindi non dovuto a java ma puramente ad Humm
            if (mRet.getLngErrNumber().longValue()!=1)
                throw new Exception("PANTAERROR:" + mRet.getLngErrNumber().longValue() + " -- " + mRet.getStrErrString());        
            else
                throw new Exception(mRet.getLngErrNumber().longValue() + " - " + mRet.getStrErrString());     
       }
       catch (Exception e) 
       {
          throw new Exception("Humm_Documento::updateDocument()\n"+e.getMessage());
       }            
  }

  /*
   * METHOD:      deleteDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Elimina il documento 
   *              
   * RETURN:      boolean
  */  
  public boolean deleteDocument() throws Exception 
  {                  
         return true;
  }
  
  /*
   * METHOD:      cambiaStatoDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Resgistra il nuovo stato per il documento  
   *              
   * RETURN:      boolean
  */ 
  public boolean cambiaStatoDocumento(String newStato) throws Exception
  {
         return true;
  }
  
  /*
   * METHOD:      cambiaStatoDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Resgistra il nuovo stato per il documento  
   *              
   * RETURN:      boolean
  */ 
  public boolean cambiaStatoDocumento(String newStato, boolean b) throws Exception
  {
         return true;
  }  

  /*
   * METHOD:      retrieve(boolean, boolean, boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un documento dal Database.
   *              
   * RETURN:      boolean
  */ 
  public boolean retrieve(boolean flagTipoDocumento, 
                          boolean flagValori,
                          boolean flagOggettiFile,
                          boolean flagLog,
                          String  idLog) throws Exception 
  {
       try {
         DMPantaReiStub dms = new DMPantaReiStub(varEnv.Global.WEB_HOST_SERVICE_PANTA);
         ExportRet iRet;

         String intExpType;

         if (flagOggettiFile)
             intExpType="1";
         else
             intExpType="0";
         
         String exp;
         
         exp= creaRetrieveXml();
         
         //Attivata la dir di debug
         if (!varEnv.Global.DIR_XML_MIME.equals("")) {
             String now;
             
             now=DateUtility.getTodayStringDate("dd-MM-yyyy-hh-mm-ss");
             LetturaScritturaFileFS f = new LetturaScritturaFileFS(varEnv.Global.DIR_XML_MIME+"\\ExportProfile_"+now+".xml");
             f.scriviFile(new ByteArrayInputStream(exp.getBytes()));
         }
         
         iRet = dms.Export(((LoginRet)token).getStrLibraryName(),
                            new Long(this.getIdDocumento()),
                            varEnv.getUser(),((LoginRet)token).getStrDST(),
                            exp,intExpType,varEnv.Global.DIR_XML_MIME);

         if (iRet.getLngErrNumber().longValue()==0) {          
            if (flagTipoDocumento) {
               this.getTipoDocumento().setIdTipodoc(iRet.getFormName());
            }
            if (flagValori) 
               for(int i=0;i<iRet.getVectorProfInfo().size();i++) {                        
                  String name=iRet.getNameProfInfo(i);
                  String value=iRet.getValueProfInfo(i);                
                  this.addValore(name,value);      
               }
    
            for(int i=0;i<iRet.getVectorRelated().size();i++) {
                  String number=(String)iRet.getVectorRelated().elementAt(i);     
                  Related r = new Related(number,"1");
                  this.related.addElement(r);
            } 

            vACL.clear();
            for(int i=0;i<iRet.getACLname().size();i++) 
               this.aggiungiACL((String)iRet.getACLname().elementAt(i),(String)iRet.getACLvalue().elementAt(i));                           

            this.addOggettoFile("0",iRet.getNomeAllegato(),"N",iRet.getAllegato());

            return true;     
         }
         else 
            // Significa che è un errore tornato dal file xml
            // e quindi non dovuto a java ma puramente ad Humm
            if (iRet.getLngErrNumber().longValue()!=1)
                throw new Exception("PANTAERROR:" + iRet.getLngErrNumber().longValue() + " -- " + iRet.getStrErrString());        
            else
                throw new Exception(iRet.getLngErrNumber().longValue() + " - " + iRet.getStrErrString());         
       }
       catch (Exception e) 
       {
          throw new Exception("Humm_Documento::retrieve()\n"+e.getMessage());
       }       
  }
  
  /*
   * METHOD:      retrieveAbstract()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un documento dal Database.
   *              
   * RETURN:      boolean
  */ 
  public boolean retrieveAbstract() throws Exception 
  {         
         return retrieve(true,true,true,false,null);
  }

    public List<String> moveFile(String idObjFile) throws Exception {
            return null;
    }

  public boolean visualizza()
  {
         return false;
  }

  // ***************** METODI DI GESTIONE DEGLI OGGETTI FILE ***************** //

  /*
   * METHOD:      addOggettoFile(String, String, 
   *                             String, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge oggetto file  
   *              
   * RETURN:      boolean
  */ 
  public boolean addOggettoFile(String idFormato,
                                String fileName,
                                String allegato,                                
                                Object file) throws Exception
  {
         return addOggettoFile(idFormato,
                              fileName,
                              allegato,
                              null,
                              file);                         
  }

  /*
   * METHOD:      addOggettoFile(String, String, String,
   *                             String, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge oggetto file  
   *              
   * RETURN:      boolean
  */ 
  public boolean addOggettoFile(String idFormato,
                                String fileName,
                                String allegato,
                                String idFilePadre,
                                Object file) throws Exception
  {
          A_Oggetti_File oFile;

          try {     
            oFile = (A_Oggetti_File)Class.forName(varEnv.Global.PACKAGE + "." + varEnv.Global.DM + 
                                                   "_" + varEnv.Global.OGGETTI_FILE).newInstance();
   
            oFile.inizializzaDati(varEnv);

            oFile.setIdOggettoFile("0");
            oFile.setIdFormato(idFormato);
            oFile.setFileName(fileName);
            oFile.setAllegato(allegato);
            if (idFilePadre!=null)
               oFile.setIdOggettoFilePadre(idFilePadre);
            oFile.setFile(file);
            oFile.setModificato("S");
 
          }
          catch (Exception e) {
                throw new Exception("Humm_Documento::addOggettoFile Non riesco a creare l'oggetto di Classe: " + 
                                   varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + varEnv.Global.OGGETTI_FILE);
          }

          this.getOggettiFile().addElement(oFile);
       
          return true;
  }    

  /*
   * METHOD:      cancellaOggettiFile(String, boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancella oggetti file  
   *              
   * RETURN:      boolean
  */ 
  public boolean cancellaOggettiFile(String idOggettoFile,boolean flagCancellaLista) throws Exception 
  {
         return false;
  }

  
// ***************** METODI DI GESTIONE DEI VALORI ***************** //

  public boolean addValore(String nomeCampo, String idCampo, Object valore) throws Exception 
  {
         return addValore(nomeCampo,valore);  
  }
  
  public boolean addValore(String nomeCampo, String idCampo, Object valore, FieldInformation fi) throws Exception 
  {
         return addValore(nomeCampo,valore);  
  }  

  /*
   * METHOD:      addValore(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge valore dato id Campo 
   *              
   * RETURN:      boolean
  */ 
  public boolean addValore(String name, Object value) throws Exception 
  {             
         A_Valori val;

         try {      
            val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + varEnv.Global.DM + 
                                          "_" + varEnv.Global.VALORI).newInstance();
            val.inizializzaDati( varEnv);
            val.getCampo().setNomeCampo(name);
            val.setValore(value);
            val.setModificato("S");           
          }
          catch (Exception e) {
                throw new Exception("Humm_Documento::addValore non riesco a creare l'oggetto di Classe: " + 
                                    varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
                                    varEnv.Global.VALORI);
          }

          this.getValori().addElement(val);
          return true;
  }
  
  /*
   * METHOD:      cancellaAllValori()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancella tutti valori  
   *              
   * RETURN:      boolean
  */  
  public boolean cancellaAllValori() throws Exception 
  {
         return false;
  }
  
  // ***************** METODI DI GESTIONE DEI RIFERIMENTI ***************** //
  
  /*
   * METHOD:      aggiungiRiferimento(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un riferimento fra il documento
   *              attuale e quello passato con l'id documento 
   *              
   * RETURN:      void
  */    
  public void aggiungiRiferimento(String number, String tipoRif) 
  {
         Related rel=new Related(number,tipoRif);
         related.addElement(rel);
  }
  
  /*
   * METHOD:      eliminaRiferimento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Elimina un riferimento  
   *              
   * RETURN:      void
  */ 
  public void eliminaRiferimento(String number,String tipoRif)
  {
         Related rel=new Related(number,"0");
         related.addElement(rel);
  }
  
  /*
   * METHOD:      annullaRiferimento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Annulla un riferimento  
   *              
   * RETURN:      void
  */ 
  public void annullaRiferimento() 
  {
         related.removeAllElements();
  }

  // ***************** METODI DI GESTIONE CONNESSIONE ***************** // 
  
  /*
   * METHOD:      reopenConnection() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Riapre la connessione
   *
   * RETURN:      String
  */
  public void reopenConnection() throws Exception
  {
    
  }

  /*
   * METHOD:      commitAndCloseConnection(boolean) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Effettua un COMMIT e chiude la connessione
   *
   * RETURN:      boolean
  */
  public boolean commitAndCloseConnection(boolean b) throws Exception
  {
     return true;
  }
 
 // ***************** METODI DI GET E SET ***************** //
 
  public void setSearchXml(String s) 
  {
        sSearchXML = s;
  }

  public void settaFileP7M(Object file) 
  {
         fileP7M=(InputStream)file;
  }

  public String getUltAggiornamento()
  {
         return "";
  }
  
  public void logDocument(String sTipoAzione) throws Exception {}
 
  // ***************** METODI PRIVATI ***************** //
 
  private String profileInfoValori(String sTipoSalvataggio) throws Exception 
  {
         int conta=0,size=this.getValori().size();
         Object obj;
  
         StringBuffer stb = new StringBuffer();
         StringBuffer stbAppoggio = new StringBuffer();
         String sForm=this.getTipoDocumento().getIdTipodoc();
                  
         while (conta!=size) {
              obj = this.getValori().elementAt(conta++);
              // Controlla se effettivamente il valore
              // contenuto nella lista è di tipo GD4_Valori
            
              Humm_Valori val = (Humm_Valori)obj;

              if (val.getCampo().getNomeCampo().equals("STATO_PANTAREI") && (val.getValore().equals("1")||val.getValore().equals("2")))                                     
                  sForm="DEF_DOC_GENERICO";                                           
              
              if (val.getModificato().equals("S"))
              {       
                if (val.getCampo().getNomeCampo().equals("COD_ENTE") || val.getCampo().getNomeCampo().equals("COD_AOO")) 
                   stbAppoggio.append("<PROFILE_INFO name=\""+val.getCampo().getNomeCampo()+"\" value=\""+((String)val.getValore()).toUpperCase()+"\"/>");                                
                else
                    stbAppoggio.append("<PROFILE_INFO name=\""+val.getCampo().getNomeCampo()+"\" value=\""+val.getValore()+"\"/>");                                
                val.setModificato("N");
              }
         }
         
         stb.append("<PROFILE_INFOS>");         
         stb.append("<PROFILE_FORM_NAME name=\""+sForm+"\"/>");

         stb.append("<PROFILE_CREATE_VALIDATION name=\"1\"/>");
         stb.append(stbAppoggio.toString());   
         stb.append("</PROFILE_INFOS>");

         // ACL
         size=vACL.size();
         conta=0;

         if (size>0) {
             stb.append("<SECURITY_INFOS>");

             while (conta!=size) 
             {//System.out.println(((ACL)vACL.elementAt(conta)).getPersonGroup()+"-"+((ACL)vACL.elementAt(conta)).getMask());
                   stb.append("<SECURITY_INFO ACLPersonGroup=\""+ ((ACL)vACL.elementAt(conta)).getPersonGroup() +"\" ACLMask=\""+ ((ACL)vACL.elementAt(conta)).getMask() +"\"/>");                                
                   conta++;
             }

            stb.append("</SECURITY_INFOS>");
         }
         
         // RELATED
         size=related.size();
         conta=0;

         if (size>0) {
             stb.append("<RELATED>");

             while (conta!=size) 
             {
                   stb.append("<item Number=\""+ ((Related)related.elementAt(conta)).getNumber() +"\" Op=\""+ ((Related)related.elementAt(conta)).getOp() +"\"/>");                                
                   conta++;
             }

            stb.append("</RELATED>");
         }

         return stb.toString();
  }
  
  private String creaInsertXml() throws Exception
  {
          StringBuffer sXml = new StringBuffer();
  
          sXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          sXml.append("<DOC_INFO xmlns:xsi='http://www.w3.org/2000/10/XMLSchema-instance'");
          sXml.append(" xsi:noNamespaceSchemaLocation=\"doc_info_v1.4.xsd\">");
          sXml.append("<DOC_DESCR>");
          sXml.append(profileInfoValori("INSERT"));   
          //sXml.append(securityInfo());
          sXml.append("</DOC_DESCR>");
          sXml.append("</DOC_INFO>");

          return sXml.toString();
  }

  private String creaRetrieveXml() throws Exception
  {
          StringBuffer sXml = new StringBuffer();
  
          sXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          sXml.append("<DOC_INFO xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"doc_info_v1.4.1.xsd\">");
          //sXml.append(searchInfoValori(this.getTipoDocumento().getIdTipodoc()));   
          sXml.append("<SEARCH_INFOS>");
          sXml.append("</SEARCH_INFOS>");
          sXml.append("</DOC_INFO>");

          return sXml.toString();
  }

  private String creaModifyXml() throws Exception
  {
          StringBuffer sXml = new StringBuffer();
  
          sXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
          sXml.append("<DOC_INFO xmlns:xsi='http://www.w3.org/2000/10/XMLSchema-instance'");
          sXml.append(" xsi:noNamespaceSchemaLocation=\"doc_info_v1.4.xsd\">\n");
          sXml.append("<DOC_DESCR>");
          sXml.append(profileInfoValori("INSERT"));   
          //sXml.append(securityInfo());
          if (this.getIdDocumento().equals("-1"))
              sXml.append(sSearchXML); 
          sXml.append("</DOC_DESCR>");
          sXml.append("</DOC_INFO>");

          return sXml.toString();
  }

  private Object getOggettoFile(int index) throws Exception 
  {
          return  ((A_Oggetti_File)this.getOggettiFile().elementAt(index)).getFile();
  }

  public String getPercorsoKFX() throws Exception {return "";}
  
  public Vector listaDiscendenti(int livelloDiscendenti)  throws Exception {return null;}
  public void retrieveLinks(String area, String cm) throws Exception {}
  public String getCodeError() {return "";}
  public Vector retrieveListaValoriLog() throws Exception {return null;}
  public void syncroFS(boolean bSonoInInsert) throws Exception {}
  public void finalizzaGestioneAllegatiTemp() throws Exception {}
  public void disconnectDbOpAllegatiTemp() {}
  public boolean saveVersion(long lVersion, boolean bNonRipetereUguali, Date dataAggiornamentoLog) throws Exception {return true;}
}


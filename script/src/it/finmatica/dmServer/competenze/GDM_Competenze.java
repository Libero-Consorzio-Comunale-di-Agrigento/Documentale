package it.finmatica.dmServer.competenze;

/*
 * GESTIONE COMPLETA DELLE COMPETENZE 
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   13/09/2005
 * 
 * */

import java.io.BufferedReader;
import java.sql.*;
import java.util.Vector;

import it.finmatica.dmServer.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.util.*;

public class GDM_Competenze
{
  private Environment varEnv;     
  private IDbOperationSQL dbOp;
  private boolean bIsNew=false;  
  private ElapsedTime elpsTime;
  
  public GDM_Competenze(Environment newVarEnv) {
         varEnv=newVarEnv;           
         
         elpsTime = new ElapsedTime("GDM_COMPETENZE su Utente/Gruppo: "+varEnv.getUser(),newVarEnv);
  }


  // ***************** METODI DI VERIFICA DELLA COMPETENZA ***************** //
  
  /*
   * METHOD:      long verifica_SI4_Compentenza(UtenteAbilitazione,Abilitazioni)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica abilitazione oggetto per un determinato utente
   *              utilizzando il package SI4_COMPETENZE
   * 
   * RETURN:      1 -> Abilitato
   *              0 -> Non abilitato
  */
  public long verifica_SI4_Compentenza(UtenteAbilitazione ua, 
                                       Abilitazioni abil) throws Exception 
  { 
  
         return verificaCompentenza(ua,abil,"GDM_COMPETENZA.SI4_VERIFICA");
  }
  
  /*
   * METHOD:      long verifica_Modulistica_Compentenza(Environment,Abilitazioni)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica abilitazione oggetto per un determinato utente
   *              utilizzando la function F_VERIFICA
   * 
   * RETURN:      1 -> Abilitato
   *              0 -> Non abilitato
  */
  public long verifica_Modulistica_Compentenza(Abilitazioni abil) throws Exception 
  { 
  
         UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), 
                                                        varEnv.getGruppo(), 
                                                        varEnv.getRuolo(), 
                                                        varEnv.getPwd(),  
                                                        varEnv.getUser());
         return verificaCompentenza(ua,abil,"F_VERIFICA");
  }
  
  /*
   * METHOD:      long verifica_GDM_Compentenza(UtenteAbilitazione,Abilitazioni)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica abilitazione oggetto per un determinato utente
   *              utilizzando il package GDM_COMPETENZE
   * 
   * RETURN:      1 -> Abilitato
   *              0 -> Non abilitato
  */
  public long verifica_GDM_Compentenza(UtenteAbilitazione ua, 
                                       Abilitazioni abil) throws Exception { 
         return verificaCompentenza(ua,abil,"GDM_COMPETENZA.GDM_VERIFICA");
  }
  
  /*
   * METHOD:      long verificaCompentenza(UtenteAbilitazione,Abilitazioni,String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Verifica abilitazione oggetto per un determinato utente
   * 
   * RETURN:      1 -> Abilitato
   *              0 -> Non abilitato
  */
   private long verificaCompentenza(UtenteAbilitazione ua, 
                                    Abilitazioni abil, 
                                    String _package) throws Exception 
   {     
                    
          long ritorno = 0;
        
          if (ua.getRuolo() == null)
              ua.setRuolo(Global.RUOLO_GDM);
          
          try {                      
            
             //Per modulistica
             if (!_package.equals("GDM_COMPETENZA.GDM_VERIFICA"))
                if (((Abilitazioni)abil).getOggetto().equals("-1"))  return 1;                

             StringBuffer sStm = new StringBuffer();

             dbOp = connect();             

             sStm.append("SELECT "+_package+"(");
             sStm.append("'"+abil.getTipoOggetto()+"'");
             sStm.append(",'"+abil.getOggetto()+"'" );
             sStm.append(",'"+abil.getTipoAbilitazione()+"'");
             sStm.append(",'"+ua.getUtente()+"'");
             
             if ( ua.getRuolo() == null )
                sStm.append(",NULL");
             else
                sStm.append(",'"+ ua.getRuolo()+"'");
             
             sStm.append(",TO_CHAR(SYSDATE,'dd/mm/yyyy') ) FROM DUAL");
             
             dbOp.setStatement(sStm.toString());

             elpsTime.start("verificaCompentenza",sStm.toString());
             dbOp.execute();
             elpsTime.stop();   

             ResultSet rst = dbOp.getRstSet();

             if (rst.next()) 
                ritorno = rst.getLong(1);
             else   
                ritorno = 0;

             close();     

             return ritorno;
        }
        catch (Exception e)
        {   
            close();
            throw new Exception("Util_Competenze::verifica_"+_package+"_Competenza "+e.getMessage());
        } 
  }
  
  // ***************** METODI PER ASSEGNARE LE COMPETENZE ***************** //
  
  /*
   * METHOD:      void assegnaCompentenza(UtenteAbilitazione,Abilitazioni,IDbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Assegna le competenze di un oggetto generico ad un dato utente      
   * 
   * RETURN:      void              
  */
  public void assegnaCompentenza(UtenteAbilitazione ua, 
                                 Abilitazioni abil) throws Exception
  {
         StringBuffer sStm = new StringBuffer();
         
         if (ua.getRuolo() == null)
             ua.setRuolo(Global.RUOLO_GDM);
         
         try {                               
             dbOp = connect();

             sStm.append("BEGIN GDM_COMPETENZA.SI4_ASSEGNA(");
             sStm.append("'"+abil.getTipoOggetto()+"'");
             sStm.append(",'"+abil.getOggetto()+"'" );
             sStm.append(",'"+abil.getTipoAbilitazione()+"' ");
            /* sStm.append(",UPPER('"+ua.getUtente()+"')");
             sStm.append(",UPPER('"+ ua.getRuolo()+"')");*/
             sStm.append(",'"+ua.getUtente()+"'");
             sStm.append(",'"+ ua.getRuolo()+"'");             
             sStm.append(",'"+ua.getAutore()+"'");
             sStm.append(",'"+abil.getAccesso()+"'");                                 
             sStm.append(","+abil.getDataInizio()+"");
             sStm.append(","+abil.getDataFine()+" ); END; ");
             dbOp.setStatement(sStm.toString());
             
             elpsTime.start("assegnaCompentenza",sStm.toString());
             dbOp.execute();
             elpsTime.stop();
             
  
             close();
         }
         catch (Exception e)
         {   
             try{close();}catch(Exception ei){}
             throw new Exception("Util_Competenze::assegna "+e.getMessage());
         } 
  }
  
    /*
   * METHOD:      void assegnaCompentenza(UtenteAbilitazione,Abilitazioni,IDbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Assegna le competenze di un oggetto generico ad un dato utente      
   * 
   * RETURN:      void              
  */
  public void assegnaCompentenzaMultipla(UtenteAbilitazione ua, 
                                 Abilitazioni abil) throws Exception
  {
         StringBuffer sStm = new StringBuffer();
         
         if (ua.getRuolo() == null)
             ua.setRuolo(Global.RUOLO_GDM);
         
         try {                               
             dbOp = connect();

             sStm.append("BEGIN GDM_COMPETENZA.SI4_ASSEGNA_MULTIPLA(");
             sStm.append("'"+abil.getTipoOggetto()+"'");
             sStm.append(",'"+abil.getOggetto()+"'" );
             sStm.append(",'"+abil.getTipoAbilitazione()+"' ");
             /*sStm.append(",UPPER('"+ua.getUtente()+"')");
             sStm.append(",UPPER('"+ ua.getRuolo()+"')");*/             
             sStm.append(",'"+ua.getUtente()+"'");
             sStm.append(",'"+ ua.getRuolo()+"'");             
             sStm.append(",'"+ua.getAutore()+"'");
             sStm.append(",'"+abil.getAccesso()+"'");                                 
             sStm.append(","+abil.getDataInizio()+"");
             sStm.append(","+abil.getDataFine()+" ); END; ");
             dbOp.setStatement(sStm.toString());
             
             elpsTime.start("assegnaCompentenzaMultipla",sStm.toString());
             dbOp.execute();
             elpsTime.stop();
  
             close();
         }
         catch (Exception e)
         {   
             try{close();}catch(Exception ei){}
             throw new Exception("Util_Competenze::assegna "+e.getMessage());
         } 
  }

  /*
   * METHOD:      void si4AssegnaCompentenza(UtenteAbilitazione,Abilitazioni,IDbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Assegna le competenze di un oggetto generico ad un dato utente      
   * 
   * RETURN:      void              
  */
  public void si4AssegnaCompetenza(UtenteAbilitazione ua, 
                                    Abilitazioni abil) throws Exception
  {
         StringBuffer sStm = new StringBuffer();
         
         if (ua.getRuolo() == null)
             ua.setRuolo(Global.RUOLO_GDM);
         
         try {                               
             dbOp = connect();
        
             sStm.append("SI4_COMPETENZA.ASSEGNA(");
             sStm.append("'"+abil.getTipoOggetto()+"'");
             sStm.append(",'"+abil.getOggetto()+"'" );
             sStm.append(",'"+abil.getTipoAbilitazione()+"' ");
             /*sStm.append(",UPPER('"+ua.getUtente()+"')");
             sStm.append(",UPPER('"+ ua.getRuolo()+"')");*/
             sStm.append(",'"+ua.getUtente()+"'");
             sStm.append(",'"+ ua.getRuolo()+"'");             
             sStm.append(",'"+ua.getUtente()+"'");
             sStm.append(",'"+abil.getAccesso()+"'");                                 
             sStm.append(","+abil.getDataInizio()+"");
             sStm.append(","+abil.getDataFine()+" )");
           
             dbOp.setCallFunc(sStm.toString());

             elpsTime.start("si4AssegnaCompetenza",sStm.toString());
             dbOp.execute();
             elpsTime.stop();
  
             close();
         }
         catch (Exception e)
         {   
             try{close();}catch(Exception ei){}
             throw new Exception("Util_Competenze::si4assegna "+e.getMessage());
         } 
  }
  
  /*
   * METHOD:      void assegnaCompetenzaDocumento(UtenteAbilitazione,Abilitazioni)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Assegna le competenze di un documento ad un dato utente seguendo
   *              le specifiche dettate dal tipo documento nel campo
   *              GESTIONE_COMPETENZE
   * 
   * RETURN:      void              
  */
  public void assegnaCompetenzaDocumento(UtenteAbilitazione ua, 
                                         Abilitazioni abil) throws Exception
  {
         StringBuffer sStm = new StringBuffer();         
        
         if (ua.getRuolo() == null)
             ua.setRuolo(Global.RUOLO_GDM);       

         try {                        
             dbOp = connect();
                          
             //ASSEGNO LE COMPETENZE DEL TIPO_DOCUMENTO AL NUOVO DOCUMENTO
             sStm.append("BEGIN GDM_COMPETENZA.GDM_ASSEGNA(");
             sStm.append("'"+abil.getTipoOggetto()+"'");
             sStm.append(",'"+abil.getOggetto()+"'" );
             sStm.append(",'"+abil.getEreditaTipoOggetto()+"'");
             sStm.append(",'"+abil.getEreditaOggetto()+"'" );
             sStm.append(",'"+ ua.getUtente()+"'); END;");

             elpsTime.start("assegnaCompetenzaDocumento - Eredito le competenze dal tipo documento sul documento",sStm.toString());
             dbOp.setStatement(sStm.toString());
             dbOp.execute();
             elpsTime.stop();
             
             //ASSEGNO LE COMPETENZE DEL TIPO_DOCUMENTO AL NUOVO DOCUMENTO PER GLI ALLEGATI
             sStm = new StringBuffer();    
             sStm.append("BEGIN GDM_COMPETENZA.gdm_comp_tipodoc_doc_allegati(");             
             sStm.append("'"+abil.getOggetto()+"'" );             
             sStm.append(",'"+ ua.getUtente()+"'); END;");

             elpsTime.start("assegnaCompetenzaDocumento - Eredito le competenze per gli allegati dal tipo documento sul documento",sStm.toString());
             dbOp.setStatement(sStm.toString());
             dbOp.execute();
             elpsTime.stop();
             
             //TESTO IL CAMPO "GESTIONE_COMPETENZE" DEL TIPO DOCUMENTO
             String valAb = abil.getTipoAbilitazione();             
             
             //CASO 1,3,5 - Assegno le competenze all'utente
             if ( (valAb.equals("1")) || (valAb.equals("3")) || (valAb.equals("5"))) {        
                 sStm = new StringBuffer();
                 sStm.append("BEGIN GDM_COMPETENZA.SI4_ASSEGNA_MULTIPLA(");
                 sStm.append("'"+abil.getTipoOggetto()+"'");
                 sStm.append(",'"+abil.getOggetto()+"'" );
                 
                 if (valAb.equals("1") )
                    sStm.append(",'"+Global.ABIL_LETT+";"+Global.ABIL_MODI);
                 else if (valAb.equals("3") )
                    sStm.append(",'"+Global.ABIL_LETT);
                 else if (valAb.equals("5") )
                    sStm.append(",'"+Global.ABIL_LETT+";"+Global.ABIL_MODI+";"+Global.ABIL_CANC);
                 
                 if (abil.getManageAbilitazione().equals(Global.GEST_UTEN))
                	sStm.append(";"+Global.ABIL_GEST);
                 
                 sStm.append(";','"+ua.getUtente()+"'");
                 sStm.append(",'"+ ua.getRuolo()+"'");
                 sStm.append(",'"+ua.getAutore()+"'");
                 sStm.append(",'"+abil.getAccesso()+"'");
                 sStm.append(","+abil.getDataInizio()+"");
                 sStm.append(","+abil.getDataFine()+"); END; ");
                 
                 elpsTime.start("Inserisco le competenze dell'utente sul documento",sStm.toString());
                 dbOp.setStatement(sStm.toString());       
                 dbOp.execute();
                 elpsTime.stop();
             }
             
             //CASO 2,4,6 - Assegno le competenze al gruppo
             else if ( (valAb.equals("2")) || (valAb.equals("4")) || (valAb.equals("6")))   
             {                        
                 sStm = new StringBuffer();
                 sStm.append("BEGIN GDM_COMPETENZA.GDM_ASSEGNA_GRUPPO_MULTIPLA(");
                 sStm.append("'"+abil.getTipoOggetto()+"'");
                 sStm.append(",'"+abil.getOggetto()+"'" );
                 
                 if (valAb.equals("2") )
                    sStm.append(",'"+Global.ABIL_LETT+";"+Global.ABIL_MODI);
                 else if (valAb.equals("4") )
                    sStm.append(",'"+Global.ABIL_LETT);
                 else if (valAb.equals("6") )
                    sStm.append(",'"+Global.ABIL_LETT+";"+Global.ABIL_MODI+";"+Global.ABIL_CANC);

                 if (abil.getManageAbilitazione().equals(Global.GEST_UTEN))
                	sStm.append(";"+Global.ABIL_GEST);
                 
                 sStm.append(";','"+ua.getUtente()+"'");
                 sStm.append(",'"+ua.getRuolo()+"'");
                 sStm.append(",'"+ua.getAutore()+"'");
                 sStm.append(",'"+abil.getAccesso()+"'");
                 sStm.append(","+abil.getDataInizio()+"");
                 sStm.append(","+abil.getDataFine()+"); END; ");
                 
                 elpsTime.start("Inserisco le competenze del gruppo sul documento",sStm.toString());
                 dbOp.setStatement(sStm.toString());       
                 dbOp.execute();
                 elpsTime.stop();
             }
             
             //Assegno la competenza di tipo MANAGE_COMPETENZA all'utente
          /*   if (abil.getManageAbilitazione().equals(Global.GEST_UTEN))  
             {
                 sStm = new StringBuffer();
                 sStm.append("BEGIN GDM_COMPETENZA.SI4_ASSEGNA(");
                 sStm.append("'"+abil.getTipoOggetto()+"'");
                 sStm.append(",'"+abil.getOggetto()+"'" );
                 sStm.append(",'"+Global.ABIL_GEST+"' ");
                 sStm.append(",'"+ua.getUtente()+"'");
                 sStm.append(",'"+ ua.getRuolo()+"'");
                 sStm.append(",'"+ua.getAutore()+"'");
                 sStm.append(",'"+abil.getAccesso()+"'");
                 sStm.append(","+abil.getDataInizio()+"");
                 sStm.append(","+abil.getDataFine()+"); END; ");
                 
                 elpsTime.start("Inserisco la competenza di 'MANAGE COMPETENZA' dell'utente sul documento",sStm.toString());        
                 dbOp.setStatement(sStm.toString());       
                 dbOp.execute();
                 elpsTime.stop();
             }
             //Assegno la competenza di tipo MANAGE_COMPETENZA al gruppo
             else if (abil.getManageAbilitazione().equals(Global.GEST_GRUP))
             {
                 sStm = new StringBuffer();
                 sStm.append("BEGIN GDM_COMPETENZA.GDM_ASSEGNA_GRUPPO(");
                 sStm.append("'"+abil.getTipoOggetto()+"'");
                 sStm.append(",'"+abil.getOggetto()+"'" );
                 sStm.append(",'"+Global.ABIL_GEST+"' ");
                 sStm.append(",'"+ua.getUtente()+"'");
                 sStm.append(",'"+ua.getRuolo()+"'");
                 sStm.append(",'"+ua.getAutore()+"'");
                 sStm.append(",'"+abil.getAccesso()+"'");
                 sStm.append(","+abil.getDataInizio()+"");
                 sStm.append(","+abil.getDataFine()+"); END; ");*/

                /* elpsTime.start("Inserisco la competenza di 'MANAGE COMPETENZA' del gruppo sul documento",sStm.toString());
                 dbOp.setStatement(sStm.toString());       
                 dbOp.execute();
                 elpsTime.stop();*/
            // }
             
             close();
                          
         }
         catch (Exception e)
         {    
              close();
              throw new Exception("GDM_Competenze::assegnaCompDoc "+e.getMessage());
         } 
  }

  /*
   * METHOD:      assegnaCompentenzaCartellaOrQuery(UtenteAbilitazione,Abilitazioni)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Assegna le competenze di una cartella o query ad un dato utente 
   *              Le competenze vengono ribaltate anche sul profilo associato
   *              all'oggetto cartella o query
   * 
   * RETURN       void
  */
  public void assegnaCompentenzaCartellaOrQuery(UtenteAbilitazione ua, 
                                                Abilitazioni abil) throws Exception
  {
      String idDocProfiloAssociato;
            
      assegnaCompentenza(ua,abil);      

      //Nel caso in cui i tipi di abilitazione è presente "C;" viene sostituita con "" perchè questa competenza 
      //non è disponibile per i tipi oggetto "DOCUMENTI"      
      abil.setTipoAbilitazione(Global.replaceAll(abil.getTipoAbilitazione(),Global.ABIL_CREA+";", ""));
      
      //RIBALTO LE COMPETENZE ANCHE SUL PROFILO ASSOCIATO ALL'OGGETTO
      if (!Global.replaceAll(abil.getTipoAbilitazione(),Global.ABIL_CREA, "").equals("")){
        try{
           //SONO SU UNA CARTELLA
           if (abil.getTipoOggetto().equals("VIEW_CARTELLA")) {
              DocUtil du= new DocUtil(varEnv);
              idDocProfiloAssociato=du.getIdDocumentoByCr(du.getIdCartellaByIdViewCartella(abil.getOggetto()));          
           }
           else
           //SONO SU UNA QUERY
              idDocProfiloAssociato=(new DocUtil(varEnv)).getIdDocumentoByCr("-"+abil.getOggetto());          
        }
        catch (Exception e) {
             throw new Exception("GDM_Competenze::aggiornaCompentenzaCartella "+e.getMessage());
        }		

        abil.setOggetto(idDocProfiloAssociato);
        abil.setTipoOggetto(Global.ABIL_DOC);
        assegnaCompentenza(ua,abil);
      }
  }
  
  /*
   * METHOD:      void eliminaCompetenza(Abilitazioni)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Eliminazione di tutte le competenze relative ad un oggetto
   * 
   * RETURN       void
  */
  public void eliminaCompetenza(Abilitazioni abil) throws Exception
  {                           
         try {                      
         
             dbOp = connect();
             
             StringBuffer sStm = new StringBuffer();             
                          
             sStm.append("BEGIN GDM_COMPETENZA.GDM_ELIMINA(");
             sStm.append("'"+abil.getTipoOggetto()+"'");
             sStm.append(",'"+abil.getOggetto()+"') END;" );                          

             dbOp.setStatement(sStm.toString());
             
             elpsTime.start("eliminaCompetenza",sStm.toString());                     
             dbOp.execute();
             elpsTime.stop();
             
             close();
                                                    
         }
         catch (Exception e)
         {
             close();
             throw new Exception("GDM_Competenze::eliminaGDM "+e.getMessage());
         } 
  }
  
  // ***************** METODI DI UTILITA' SULLE COMPETENZE ***************** //
  
  /*
   * METHOD:      String gdmAllineaCompCartQueryDoc(String, String, String, String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idViewCart -> ID VIEW
   *              idDoc      -> ID DOCUMENTO
   *              autore     -> AUTORE
   *              tipo       -> TIPO
   *              dbOp       -> CONNECTION
   *              
   *              Allinea le competenze fra la cartella ed il documento-cartella
   * 
   * RETURN:      void
  */ 
  public void allineaCompetenzaCartQueryDoc(String idViewCart,
                                            String idDoc,
                                            String autore,
                                            String tipo) throws Exception
  {       
         try {                     
           dbOp = connect();
           
           StringBuffer sStm = new StringBuffer();
           
           sStm.append("BEGIN GDM_COMPETENZA.GDM_ALLINEA_COMP_CQ_DOC(");
           sStm.append(idViewCart + "," + idDoc + ",");	
           sStm.append("'" + autore + "','"+tipo+"'); END;");      

           dbOp.setStatement(sStm.toString());

           elpsTime.start("allineaCompetenzaCartQueryDoc",sStm.toString());                     
           dbOp.execute();
           elpsTime.stop();
 

           close();
         }
         catch (Exception e)
         {
           close();
           throw new Exception("GDM_Competenze::gdmAllineaCompCartDoc "+e.getMessage());
         } 
  }

  // ***************** METODI DI UTILITA' SULLE COMPETENZE DEI DOCUMENTI ***************** //
  // *****************    CON CONNESSIONE GESTITA IN MANIERA AUTONOMA    ***************** //
  public void consentiDocumento (String user,
                                 String idDocumento,
                                 String tipoAbil,
                                 String author) throws Exception {
	  	 consentiDocumento (user,
                            idDocumento,
                            tipoAbil,
                            author,
                            "GDM");
  }
  
  
  /*
   * METHOD:      consentiDocumento(String, String, String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: user        -> Utente
   *              idDocumento -> ID_DOCUMENTO
   *              tipoAbil    -> TIPO_ABILITAZIONE (es. L, LUC,...)
   *              author      -> AUTORE
   *              
   *              Assegna in maniera multipla le abilitazioni sul documento
   *              idDocumento all'utente passato in input   
   * 
   * RETURN:      void
  */ 
  public void consentiDocumento (String user,
                                 String idDocumento,
                                 String tipoAbil,
                                 String author,
                                 String ruolo) throws Exception 
  {
         String  sStm = "";         
         
         try  {      
           
           sStm += "GDM_PROFILO.CONSENTIDOCUMENTO(";
           sStm += "'"+user+"'";
           sStm += ",'"+tipoAbil+"' ";
           sStm += ",'"+idDocumento+"'";
           sStm += ",'"+author+"'";
           sStm += ",'"+ruolo+"'";
           if (varEnv.getByPassCompetenze())
        	   sStm += ",'S'";
           sStm+=")";

           elpsTime.start("Gestione delle ACL - consenti documento a "+user,sStm.toString());
           eseguiCompetenza(sStm);           
           elpsTime.stop();
           
         }
         catch (Exception e)
         {
           throw new Exception("GestioneDocumenti::consentiDocumento\n"+e.getMessage());
         } 
    }
   
   /*
    * METHOD:      consentiDocumentoDaA(String, String, String, String, String, String)
    * SCOPE:       PUBLIC
    *
    * DESCRIPTION: user        -> Utente
    *              idDocumento -> ID_DOCUMENTO
    *              tipoAbil    -> TIPO_ABILITAZIONE (es. L, LUC,...)
    *              dal         -> Data di inizio validità
    *              al          -> Data di fine validità
    *              author      -> AUTORE
    *              
    *              Assegna in maniera multipla le abilitazioni sul documento
    *              idDocumento all'utente passato in input per il periodo indicato
    *              
    *              Solo in questo caso la connessione viene tutta gestista
    *              internamente (gestione commit o rollback)
    * 
    * RETURN:      void
   */ 
   public void consentiDocumentoDaA (String user ,
                                     String idDocumento,
                                     String tipoAbil,
                                     String dal ,
                                     String al,
                                     String author) throws Exception 
   {
          String  sStm = "";          
          
          try {      
           
            sStm += "GDM_PROFILO.consentiDocumentoDaA(";
            sStm += "'"+user+"'";
            sStm += ",'"+tipoAbil+"' ";
            sStm += ",'"+idDocumento+"'" ;
            sStm += ",'"+dal+"'" ;
            
            if (al == null)
                sStm += ",null" ;
            else
                sStm += ",'"+al+"'" ;
            sStm += ",'"+author+"' ";
            if (varEnv.getByPassCompetenze())
        	    sStm += ",'S'";
            sStm+=")";            
  
            eseguiCompetenza(sStm);
                        
          }
          catch (Exception e)
          {            
            throw new Exception("GestioneDocumenti::consentiDocumentoDaA\n"+e.getMessage());
          } 
   }

   public void negaDocumento(String user ,
                             String idDocumento,
                             String tipoAbil,
                             String author) throws Exception 
   {
	      negaDocumento(user ,
                        idDocumento,
                        tipoAbil,
                        author,
                        "GDM");
   }

   /*
    * METHOD:      negaDocumento(String, String, String, String)
    * SCOPE:       PUBLIC
    *
    * DESCRIPTION: user         -> UTENTE
    *              idDocumento  -> ID DOCUMENTO
    *              tipoAbil     -> TIPO ABILITAZIONE (es. L, LUC,...)
    *              author       -> AUTORE
    *              
    *              Elimina in maniera multipla le abilitazioni sul documento
    *              idDocumento all'utente passato in input
    *              
    *              Solo in questo caso la connessione viene tutta gestista
    *              internamente (gestione commit o rollback)
    * 
    * RETURN:      Boolean
   */   
   public void negaDocumento(String user ,
                             String idDocumento,
                             String tipoAbil,
                             String author,
                             String ruolo) throws Exception 
   {
          String  sStm = "";          
          
          try {      
           
            sStm += "GDM_PROFILO.negaDocumento(";
            sStm += "'"+user+"'";
            sStm += ",'"+tipoAbil+"'";
            sStm += ",'"+idDocumento+"'" ;
            sStm += ",'"+author+"'";
            sStm += ",'"+ruolo+"'";
            if (varEnv.getByPassCompetenze())
        	    sStm += ",'S'";
            sStm+=")";
           
            elpsTime.start("Gestione delle ACL - negaDocumento a "+user,sStm.toString());
            eseguiCompetenza(sStm);
            elpsTime.stop();
            
          }
          catch (Exception e)
          {
             throw new Exception("GestioneDocumenti::negaDocumento\n"+e.getMessage());             
          } 
   }


   /*
    * METHOD:      negaDocumentoDaA(String, String, String, String, String, String)
    * SCOPE:       PUBLIC
    *
    * DESCRIPTION: user         -> UTENTE
    *              idDocumento  -> ID DOCUMENTO
    *              tipoAbil     -> TIPO ABILITAZIONE (es. L, LUC,...)
    *              author       -> AUTORE
    *              
    *              Elimina in maniera multipla le abilitazioni sul documento
    *              idDocumento all'utente passato in input per il periodo indicato
    *              
    *              Solo in questo caso la connessione viene tutta gestista
    *              internamente (gestione commit o rollback)
    * 
    * RETURN:      Boolean
   */   
   public void negaDocumentoDaA (String user ,
                                 String idDocumento,
                                 String tipoAbil,
                                 String dal ,
                                 String al,
                                 String author)  throws Exception 
   {
          String  sStm = "";          
          
          try {                  
                       
            sStm += "GDM_PROFILO.negaDocumentoDaA(";
            sStm += "'"+user+"'";
            sStm += ",'"+tipoAbil+"' ";
            sStm += ",'"+idDocumento+"'" ;
            sStm += ",'"+dal+"'" ;
           
            if (al == null)
                sStm += ",null" ;
            else
                sStm += ",'"+al+"'" ;
            sStm += ",'"+author+"'";
            if (varEnv.getByPassCompetenze())
        	    sStm += ",'S'";
            sStm+=")";            
  
            eseguiCompetenza(sStm);
            
          }
          catch (Exception e)
          {
            throw new Exception("GestioneDocumenti::negaDocumentoDaA\n"+e.getMessage());
          } 
   }
         
   public String getCompetenzeDocumento(String idDoc) throws Exception {
	   	  String ret="0@0@0@0";
	   	  
	   	  StringBuffer sStm = new StringBuffer("SELECT ");
	   	  sStm.append("nvl(GDM_COMPETENZA.GDM_VERIFICA( 'DOCUMENTI' , '"+idDoc+"', 'L' ,'"+varEnv.getUser()+"','GDM'  ),0) lettura_cmp, ");
	   	  sStm.append("nvl(GDM_COMPETENZA.GDM_VERIFICA( 'DOCUMENTI' , '"+idDoc+"', 'U' ,'"+varEnv.getUser()+"' ,'GDM' ),0) aggiornamento_cmp, ");
	   	  sStm.append("nvl(GDM_COMPETENZA.GDM_VERIFICA( 'DOCUMENTI' , '"+idDoc+"', 'D' ,'"+varEnv.getUser()+"' ,'GDM' ),0) delete_cmp, ");
	   	  sStm.append("nvl(GDM_COMPETENZA.GDM_VERIFICA( 'DOCUMENTI' , '"+idDoc+"', 'M' ,'"+varEnv.getUser()+"' ,'GDM' ),0) manager_cmp ");
	   	  sStm.append("FROM DOCUMENTI WHERE ID_DOCUMENTO= "+idDoc);
	   	  
	   	  try {
		   	  dbOp = connect();
		   	  
		   	  dbOp.setStatement(sStm.toString());
		   	  
		   	  dbOp.execute();
		   	  
		   	  ResultSet rst = dbOp.getRstSet();
		   	  
		   	  if (rst.next()) {
		   		  ret=rst.getString(1)+"@"+rst.getString(2)+"@"+rst.getString(3)+"@"+rst.getString(4);
		   	  }
		   	  
		   	  close();
	   	  } 
	   	  catch (Exception e) {
            throw new Exception("GDM_Competenze::getCompetenzeDocumento("+idDoc+")\n"+e.getMessage());
          } 
	   	  
	   	  return ret;
   }
   
   public HashMapSet getElencoCompetenzeDocumento(Abilitazioni abil, boolean soloUtenti) throws Exception {
	      HashMapSet hms = new HashMapSet();
	   	  
	   	  StringBuffer sStm = new StringBuffer("");
	   	  
	   	  String soloUte="N";
	   	  if (soloUtenti) soloUte="Y";
	   	  sStm.append("GDM_COMPETENZA.GETELENCOUTENTIACCESSO( '"+abil.getTipoOggetto()+"', '"+abil.getOggetto()+"', '"+
	   			                                                 abil.getTipoAbilitazione()+"', '"+soloUte+"' )");
	   	  ResultSet rs =null;
	   	  Clob ret;
	   	  StringBuffer strSQL = new StringBuffer();
	   	  String aux;

	   	  try {
	   		 // System.out.println(sStm.toString());
		   	  dbOp = connect();
		   	  
		   	  dbOp.setTypeRetFunction(oracle.jdbc.driver.OracleTypes.CLOB);
		   	  dbOp.setCallFunc(sStm.toString());
		   	  
		   	  dbOp.execute();
		   
		      ret = (Clob)dbOp.getCallSql().getObject(1);
		      		      
		      BufferedReader br = new BufferedReader(ret.getCharacterStream());
		      while ((aux=br.readLine())!=null) strSQL.append(aux);
		     		     
		      if (!strSQL.toString().trim().equals("")) {
		    	  dbOp.setStatement(strSQL.toString());
		    	  //System.out.println(strSQL.toString());
		    	  dbOp.execute();
		    	  
		    	  rs = dbOp.getRstSet();
		    	  
		    	  while(rs.next()) { hms.add(rs.getString(1),new ACL(rs.getString(1),rs.getString(2),rs.getString(4),Integer.parseInt(rs.getString(5))));}
		      }
		      		     	     		   	  
		   	  close();		   	  
	   	  } 
	   	  catch (Exception e) {
	   		if (rs!=null)  try {rs.close();} catch (Exception ei) {}
            throw new Exception("GDM_Competenze::getElencoCompetenzeDocumento GDM_COMPETENZA.GETELENCOUTENTIACCESSO( '"+abil.getTipoOggetto()+"', '"+
            					 abil.getOggetto()+"', '"+abil.getTipoAbilitazione()+"', '"+soloUte+"' )\n"+e.getMessage());
          }
	   	  
	   	  return hms;
   }

   /*
    * METHOD:      eseguiCompetenza(IDbOperationSQL, String)
    * SCOPE:       PRIVATE
    *
    * DESCRIPTION: sStm    -> STATEMENT
    *              
    *              Esegue lo statement sulla dbOp passata
    *              Viene richiamata dalle consenti e nega documento
    * 
    * RETURN:      Boolean
   */ 
   private void eseguiCompetenza(String sStm) throws Exception
   {
           dbOp = connect();
           
           dbOp.setCallFunc(sStm);
           dbOp.execute();
           int ris = dbOp.getCallSql().getInt(1);
           
           close();
           
           switch ( ris ) {
               case 0:
                  break;
               case -1:
                  throw new Exception("Tipo di abilitazione incompatibile con l'oggetto indicato");
               case -2: 
                  throw new Exception("Non esiste l'oggetto");
               case -3: 
                  throw new Exception("Non esiste il tipo di abilitazione ");
               case -4: 
                  throw new Exception("Documento inesistente");
               case -5: 
                  throw new Exception("Utente senza le competenze necessarie");
               default: 
                  throw new Exception("Profilo::Errore non codificato");
           }
                          
   }
   
  private IDbOperationSQL connect() throws Exception {
        if (varEnv.getDbOp()==null) {
           bIsNew=true;
           return (new ManageConnection(varEnv.Global)).connectToDB();
        }
        
        return varEnv.getDbOp();
  }
  
  private void close() throws Exception {
        if (bIsNew) (new ManageConnection(varEnv.Global)).disconnectFromDB(dbOp,true,false);        
  }

}

package it.finmatica.dmServer;

/*
 * GESTIONE DEGLI STATI DOCUMENTO
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.sql.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.competenze.Abilitazioni;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.competenze.UtenteAbilitazione;
import it.finmatica.dmServer.util.*;

public class GD4_Status_Documento extends A_Status_Documento 
{
  // Variabili private
  private Date dataAgg;
  private String utenteAgg;
  private String commento;
 

  private ElapsedTime elpsTime;

  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public GD4_Status_Documento() { }

  /*
   * METHOD:      inizializzaDati(Object, String, IDbOperationSQL)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  public void inizializzaDati( Object vEnv, String idDoc)
  {
         this.inizializzaDati( (Environment) vEnv, idDoc);
  }

  /*
   * METHOD:      inizializzaDati(Environment, String, IDbOperationSQL)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */ 
  private void inizializzaDati( Environment vEnv, String idDoc)
  {
         this.varEnv = vEnv;              
         super.setIdDocumento(idDoc);
         
         elpsTime = new ElapsedTime("GD4_STATUS_DOCUMENTO",vEnv);
  }
  
  // ***************** METODI DI GESTIONE STATO DOCUMENTO ***************** //
 
  /*
   * METHOD:      registraStato() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Memorizza uno stato nella tabella stati_documenti
   *
   * RETURN:      boolean
  */
  public boolean registraStato() throws Exception 
  {               
         try {
             StringBuffer sStm = new StringBuffer();
             StringBuffer sStmDoc=null;
             
             if ( this.getStato().equals(verificaStato(this.getIdDocumento())) )
             {
                 sStm.append("update stati_documento a set ");
                 sStm.append(" data_aggiornamento = sysdate, ");
                 sStm.append(" utente_aggiornamento= '"+ varEnv.getUser() +"'" );
                 sStm.append(" where a.id_documento = " +  this.getIdDocumento() );
                 sStm.append(" and a.id_stato = (select max(id_stato) from stati_documento b ");
                 sStm.append(" where a.id_documento = b.id_documento ) ");               
             }
             else
             { 
               //Prima di passare allo stato cancellato controlla le competenze di elimina
               if (this.getStato().equals(Global.STATO_CANCELLATO)) {
            	   Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC, this.getIdDocumento() , "D"); 
                   UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(), varEnv.getPwd(),  varEnv.getUser(), varEnv);
                   
                   if (!varEnv.getByPassCompetenze() && (new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua, abilitazione) == 0)    
                        throw new Exception("Competenza del documento ("+this.getIdDocumento()+") Fallita - Utente non autorizzato. Controllare le competenze di cancellazione per l'utente ("+varEnv.getUser()+")");
               }
               
               int id_stato = varEnv.getDbOp().getNextKeyFromSequence("STDO_SQ");               
               sStm.append("insert into stati_documento (id_stato, id_documento, stato,  commento,");
               sStm.append("data_aggiornamento,utente_aggiornamento) values ");
               sStm.append("("+ id_stato );
               sStm.append(","+ this.getIdDocumento()  );
               sStm.append(",'"+ this.getStato()+"'");
               if (this.getCommento()!=null)
                 sStm.append(",'"+ Global.replaceAll(this.getCommento(),"'","''")+"'");
               else
                   sStm.append(", null");
               sStm.append(",sysdate,'"+ varEnv.getUser() +"')");
               
               if (!bSaltaUpdateDoc) {
	               sStmDoc = new StringBuffer();
	               sStmDoc.append("update documenti set ");
	               sStmDoc.append(" stato_documento = '"+ this.getStato()+"',");
	               sStmDoc.append(" data_aggiornamento = sysdate, ");
	               sStmDoc.append(" utente_aggiornamento= '"+ varEnv.getUser() +"' " );
	               sStmDoc.append(" where id_documento = "+this.getIdDocumento());   
               } 
             }   
             
             elpsTime.start("Inserimento/Aggiornamento Stato Documento",sStm.toString());
             
             varEnv.getDbOp().setStatement(sStm.toString());
             varEnv.getDbOp().execute();             
             
             if (sStmDoc!=null) {
            	varEnv.getDbOp().setStatement(sStmDoc.toString());
                varEnv.getDbOp().execute();   
             }
             
             elpsTime.stop();
            
             return true;
         }    
         catch (Exception e) {      
             throw new Exception("GD4_Status_Documento::registraStato() " + e.getMessage());
         }

  }

  /*
   * METHOD:      loadStato() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica e aggiorna in memoria l'ultimo stato
   *              registrato per quel documento
   *
   * RETURN:      void
  */
  public void loadStato()  throws  Exception
  {
        IDbOperationSQL dbOpSQL = null;
         
        try {
           StringBuffer sStm = new StringBuffer();
           dbOpSQL = varEnv.getDbOp();

           sStm.append("select stato, commento, data_aggiornamento, utente_aggiornamento from stati_documento a");
           sStm.append(" where id_documento = " +  this.getIdDocumento() );
           sStm.append(" and a.id_stato = (select max(id_stato) from stati_documento b ");
           sStm.append(" where a.id_documento = b.id_documento ) ");
                          
           dbOpSQL.setStatement(sStm.toString());

           dbOpSQL.execute();

           ResultSet rst = dbOpSQL.getRstSet();

           if (rst.next()) {
              this.setStato(rst.getString(1));
              this.setCommento(rst.getString(2));
              this.setDataAgg(rst.getDate(3));
              this.setUtenteAgg(rst.getString(4));              
           }
           else {              
              throw new Exception("Select fallita per idDocumento: " + 
                                     this.getIdDocumento());                   
           }           
         }
         catch (Exception e) {               
               throw new Exception("GD4_Status_Documento::loadStato() " + e.getMessage());
         }      
                  
  }

  /*
   * METHOD:      verificaStato(String) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica l'ultimo stato registrato per quel documento
   *
   * RETURN:      String
  */
  public String verificaStato(String idDoc)  throws  Exception
  {
         IDbOperationSQL dbOpSQL = null;
         
         try {
           StringBuffer sStm = new StringBuffer();
           String sStato;
           dbOpSQL = varEnv.getDbOp();                      

           sStm.append("select stato  from stati_documento a");
           sStm.append(" where id_documento = " +  idDoc );
           sStm.append(" and a.id_stato = (select max(id_stato) from stati_documento b ");
           sStm.append(" where a.id_documento = b.id_documento ) ");          
                          
           dbOpSQL.setStatement(sStm.toString());
           
           elpsTime.start("Verifica Stato Documento",sStm.toString());          
           dbOpSQL.execute();
           elpsTime.stop();
           
           ResultSet rst = dbOpSQL.getRstSet();

           if (rst.next()) {
              sStato = rst.getString(1);              
           }
           else {
              sStato="";                   
           }
                      
           return sStato;
         }
         catch (Exception e) {               
               throw new Exception("GD4_Status_Documento::verificaStato() idDoc: " +idDoc + " "+ e.getMessage());
         }     

  }
 
  // ***************** METODI DI GET E SET ***************** //

  public String getCommento()
  {
         return commento;
  }
   
  public void setCommento(String newCommento)
  {
         commento = newCommento;
  }

  public String getUtenteAgg()
  {
         return utenteAgg;
  }
  
  public void setUtenteAgg(String newUtenteAgg)
  {
         utenteAgg = newUtenteAgg;
  }

  public void setDataAgg(Date newDataAgg)
  {
         dataAgg = newDataAgg;
  }

   public Date getDataAgg()
  {
         return dataAgg;
  }



}
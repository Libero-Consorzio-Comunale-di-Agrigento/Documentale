package it.finmatica.dmServer.competenze;

import java.sql.*;
import it.finmatica.dmServer.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.*;
import it.finmatica.jfc.dbUtil.*;
import instantj.expression.Expression;

public class CompetenzeControlli 
{
  String elementId;
  String idTipoId;
  String abil;
  String condizione;
  Environment en;

  private static String COND_MAI = "0";
  private static String COND_VALIDATO = "2";    
  private static String COND_NONVALIDATO = "3";      
  
  public CompetenzeControlli(String newElementId, String newAbil, Environment newEn)
  {
         en= newEn;
         elementId = newElementId;
         abil=newAbil;
  }

  public boolean execControlli() throws Exception
  {
        IDbOperationSQL dbOp = null;
        String queryC = null;
        String corpo, driver, connessione, utente, passwd;

        byte[] b2 = new byte[1];
        b2[0] = 13;
        String ch = new String(b2);

        try 
        {
           idTipoId = (new DocUtil(en)).getIdTipoDocByIdDocumento(elementId);
        }
        catch (Exception e)
        {
           throw new Exception("CompetenzeControlli::execControlli() Errore recupero idTipoDoc\n"+e.getMessage());
        }
        
        queryC = "SELECT CORPO, TIPO, DRIVER, CONNESSIONE, UTENTE, PASSWD, CONDIZIONE "+
                 "FROM COMPETENZE_FUNZIONALI,SI4_TIPI_OGGETTO,SI4_TIPI_ABILITAZIONE, "+
                 "     SI4_ABILITAZIONI "+               
                 "WHERE ID_TIPODOC = :P_ID_TIPODOC AND "+
                 "      SI4_TIPI_OGGETTO.TIPO_OGGETTO = 'TIPI_DOCUMENTO' AND "+
                 "      SI4_TIPI_ABILITAZIONE.TIPO_ABILITAZIONE = '"+abil+"' AND "+
                 "      SI4_ABILITAZIONI.ID_ABILITAZIONE = COMPETENZE_FUNZIONALI.ID_ABILITAZIONE AND "+
                 "      SI4_ABILITAZIONI.ID_TIPO_OGGETTO = SI4_TIPI_OGGETTO.ID_TIPO_OGGETTO AND "+
                 "      SI4_ABILITAZIONI.ID_TIPO_ABILITAZIONE = SI4_TIPI_ABILITAZIONE.ID_TIPO_ABILITAZIONE";               

        try {
           dbOp = en.getDbOp();

           dbOp.setStatement(queryC);
           dbOp.setParameter(":P_ID_TIPODOC",idTipoId);

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {   

               corpo=rst.getString(1);
               driver=rst.getString(3);
               connessione=rst.getString(4);
               utente=rst.getString(5);
               passwd=rst.getString(6);
               condizione=rst.getString(7);

               //Controllo condizione con stato del documento
               if (condizione.equals(COND_MAI)) return false;

               if (condizione.equals(COND_VALIDATO)) 
               {
                  GD4_Status_Documento stat = new GD4_Status_Documento();
                  stat.inizializzaDati(en,elementId);

                  if (!stat.verificaStato(elementId).equals(Global.STATO_COMPLETO)) return false;
               }

               if (condizione.equals(COND_NONVALIDATO)) 
               {
                  GD4_Status_Documento stat = new GD4_Status_Documento();
                  stat.inizializzaDati(en,elementId);

                  if (stat.verificaStato(elementId).equals(Global.STATO_COMPLETO)) return false;
               }

               try {             
                 corpo = Global.replaceAll(corpo,ch," ");
                 corpo = Global.replaceAll(corpo,"\n"," ");
               }
               catch (NullPointerException e) {                              
                  throw new Exception("Corpo COMPETENZE vuoto!");
               }

               if (rst.getString(2).equals("J")) {                  
                  return execControlloJava(corpo);
               }
               else {                  
                  return execControlloPLSQL(corpo,driver,connessione,utente,passwd);                
               }
            }
            else {               
               return false;
            }
        }
        catch (Exception e)
        {           
           throw new Exception("CompetenzeControlli::execControlli() "+e.getMessage());
        }
  }

  private boolean execControlloJava(String corpo) throws Exception
  {   
      CompetenzeParser cmpPar = new CompetenzeParser(elementId,en);
      String istruzioneJava = cmpPar.bindingDeiParametri(corpo);
     
      if (!cmpPar.getError().equals("@"))
          throw new Exception("execControlloJava - Errore in bindingDeiParametri\n"+cmpPar.getError());  

      try {
          Expression myEx = new Expression(istruzioneJava);
          return ((Boolean)myEx.getInstance().evaluate()).booleanValue();
      } 
      catch (Exception ijEx) { 
             throw new Exception("execControlloJava - Errore esecuzione "+ istruzioneJava + "\n"+ijEx.getMessage());
      }
         
  }

  private boolean execControlloPLSQL(String corpo,String driver,String connessione,String utente,String passwd) throws Exception
  {
      String result = "0";
      IDbOperationSQL dbOp = null;
      CompetenzeParser cmpPar = new CompetenzeParser(elementId,en);
      String istruzionePLSQL = cmpPar.bindingDeiParametri(corpo);
       
      if (!cmpPar.getError().equals("@"))
          throw new Exception("execControlloPLSQL - Errore in bindingDeiParametri\n"+cmpPar.getError());  
 
      if (connessione==null) 
         try {
           dbOp = en.getDbOp();
         }
         catch (Exception e)
         {        
           throw new Exception("execControlloPLSQL - Errore connessione al DB (parametri standard)\n"+e.getMessage());  
         }
      else 
         try {        
            dbOp =  SessioneDb.getInstance().createIDbOperationSQL(driver,connessione,utente,passwd);
         }
         catch (Exception e)
         {           
           throw new Exception("execControlloPLSQL - Errore connessione al DB (driver,connessione,utente,passwd)\n"+" ("+driver+","+connessione+","+utente+","+passwd+") "+e.getMessage());  
         }      

      dbOp.setCallFunc(istruzionePLSQL);
      dbOp.execute();
      result = dbOp.getCallSql().getString(1);

      if (result == null) return false;

      if (connessione!=null) dbOp.close();

      if (result.equals("1")) return true;
      else return false;

  }
  
}
package it.finmatica.dmServer.controlli;

import java.sql.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.*;
import it.finmatica.jfc.dbUtil.*;
import instantj.expression.Expression;
import it.finmatica.dmServer.util.ManageConnection;

public class ACKControlli 
{
  String ack;
  String area;
  String elementId;
  Environment en;
  IDbOperationSQL dbOp;  
  private boolean bIsNew=false;  
  
  public ACKControlli(String newAck, String newArea, String newElementId, Environment newEn) throws Exception
  {
      ack = newAck;
      area = newArea;
      en= newEn;
      elementId = newElementId;              
  }

  public String execControlli() throws Exception
  {      
      String queryC, sRet = null;
      String corpo, driver, connessione, utente, passwd;

      byte[] b2 = new byte[1]; 
      b2[0] = 13;
      String ch = new String(b2);
      
      queryC = "SELECT L.CORPO,L.TIPO,L.DRIVER, L.CONNESSIONE, L.UTENTE, L.PASSWD "+
               "FROM CONTROLLI_CARTELLE C, LIBRERIA_CONTROLLI L "+
               "WHERE C.ACTION_KEY = :P_ACK AND "+
               "C.AREA = :P_AREA AND "+
               "C.CONTROLLO = L.CONTROLLO AND "+
               "C.AREA = L.AREA ";

      try {
         dbOp = connect();

         dbOp.setStatement(queryC);
         dbOp.setParameter(":P_AREA",area);
         dbOp.setParameter(":P_ACK",ack);

         dbOp.execute();

         ResultSet rst = dbOp.getRstSet();

         if (rst.next()) {   

             corpo=rst.getString(1);
             driver=rst.getString(3);
             connessione=rst.getString(4);
             utente=rst.getString(5);
             passwd=rst.getString(6);

             try {             
               corpo = Global.replaceAll(corpo,ch," ");
               corpo = Global.replaceAll(corpo,"\n"," ");
             }
             catch (NullPointerException e) {                             
                throw new Exception("Corpo del bottone vuoto!");
             }

             if (rst.getString(2).equals("J"))
                sRet = execControlloJava(corpo);
             else
                sRet = execControlloPLSQL(corpo,driver,connessione,utente,passwd);                
          }
          
          close();
          return sRet;
      }
      catch (Exception e)
      {     
         close();
         throw new Exception("ACKControlli::execControlli() "+e.getMessage());
      }
  }

  private String execControlloJava(String corpo) throws Exception
  {   
      String result="";
      ACKParser ackPar = new ACKParser(elementId,en);
      String istruzioneJava = ackPar.bindingDeiParametri(corpo);
      
      if (!ackPar.getError().equals("@"))
          throw new Exception("execControlloJava - Errore in bindingDeiParametri\n"+ackPar.getError());  
          
      try {
          Expression myEx = new Expression(istruzioneJava);
          result = (String)myEx.getInstance().evaluate();
      } 
      catch (Exception ijEx) { 
             throw new Exception("execControlloJava - Errore esecuzione "+ istruzioneJava + "\n"+ijEx.getMessage());
      }
         
      return result;
  }

  private String execControlloPLSQL(String corpo,String driver,String connessione,String utente,String passwd) throws Exception
  {
      String result="";      
      ACKParser ackPar = new ACKParser(elementId,en);
      String istruzionePLSQL = ackPar.bindingDeiParametri(corpo);
       
      if (!ackPar.getError().equals("@"))
          throw new Exception("execControlloPLSQL - Errore in bindingDeiParametri\n"+ackPar.getError());  
 
      if (connessione!=null) 
         try {        
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver,connessione,utente,passwd);
         }
         catch (Exception e)
         {
           throw new Exception("execControlloPLSQL - Errore connessione al DB (driver,connessione,utente,passwd)\n"+" ("+driver+","+connessione+","+utente+","+passwd+") "+e.getMessage());  
         }      

      dbOp.setCallFunc(istruzionePLSQL);
      dbOp.execute();
      result = dbOp.getCallSql().getString(1);
      if (result == null) {
          result = "";
      }
      
      if (connessione!=null)  
         dbOp.close();

      return result;

  }
  
  private IDbOperationSQL connect() throws Exception {
        if (en.getDbOp()==null) {
           bIsNew=true;
           return (new ManageConnection(en.Global)).connectToDB();
        }
        
        return en.getDbOp();
  }
  
  private void close() throws Exception {
        if (bIsNew) (new ManageConnection(en.Global)).disconnectFromDB(dbOp,true,false);        
  }

  
}
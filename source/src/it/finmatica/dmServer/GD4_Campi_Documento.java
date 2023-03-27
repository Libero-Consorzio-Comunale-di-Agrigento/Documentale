package it.finmatica.dmServer;

/*
 * GESTIONE DEI CAMPI DOCUMENTO
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.sql.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.Environment;

public class GD4_Campi_Documento extends A_Campi_Documento
{
  private Environment env;


  public void inizializza(Environment newEnv) {
         env = newEnv;
  }

  /*
   * METHOD:      retrieve()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un campo documento dal DB 
   * 
   * RETURN:      void
  */  
  public void retrieve() throws Exception 
  {
         if (this.getIdCampo().equals("0")) return;

         IDbOperationSQL dbOpSQL = null;
         
         try {
           StringBuffer sStm = new StringBuffer();
           dbOpSQL = env.getDbOp();

           sStm.append("SELECT NOME FROM CAMPI_DOCUMENTO ");
           sStm.append("WHERE ID_CAMPO = " + this.getIdCampo());

           dbOpSQL.setStatement(sStm.toString());

           dbOpSQL.execute();

           ResultSet rst = dbOpSQL.getRstSet();

           if (rst.next()) {
              this.setNomeCampo(rst.getString(1));              
           }
           else 
              throw new Exception("Select fallita per idCampo: " + this.getIdCampo());                                         
         }
         catch (Exception e) {
               throw new Exception("GD4_Campi_Documento::retrieve() " + e.getMessage());
         }
  }

   public String toString() 
  {         
         return super.toString();
  }
 
}
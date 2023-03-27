package it.finmatica.dmServer;

/*
 * GESTIONE DELLE LIBRERIE
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
 
import it.finmatica.jfc.dbUtil.*;
import java.sql.*;

public class GD4_Libreria extends A_Libreria
{
  
  public boolean creaDirectory()
  {
         return false;
  }

 // ***************** METODI DI GESTIONE DELLE LIBRERIE ***************** //
 
   /*
   * METHOD:      retrieve()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica una libreria dal Database
   *              
   * RETURN:      boolean
  */ 
  public void retrieve() throws Exception 
  {
         StringBuffer sStm = null;
         IDbOperationSQL dbOpSQL = null;
                  
         if (this.getIdLibreria().equals("0")) return;
         
         try {
            sStm = new StringBuffer();
           
           dbOpSQL = env.getDbOp();

           sStm.append("select libreria, directory from librerie");
           sStm.append(" where id_libreria = " + this.getIdLibreria());

           dbOpSQL.setStatement(sStm.toString());

           dbOpSQL.execute();

           ResultSet rst = dbOpSQL.getRstSet();

           if (rst.next()) {
              this.setLibreria(rst.getString(1));
              this.setDirectory(rst.getString(2));
           }
           else 
           {             
             throw new Exception("Select fallita per idLibreria: " + this.getIdLibreria());                   
           }
         }
         catch (Exception e) {               
               throw new Exception("GD4_Libreria::retrieve() "+ e.getMessage());
         }

  }

  public String toString() 
  {
         return super.toString();
  }

}
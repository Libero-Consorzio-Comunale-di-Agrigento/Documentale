package it.finmatica.GDMTreeViewWeb.TreeView;

import it.finmatica.jfc.dbUtil.*;
//import it.finmatica.jfc.utility.DateUtility;

public class Main 
{
  public Main()
  {
  }

  public static void main(String[] args)
  {

        TreeView t = new TreeView();

    t.setDefaultTarget("");
    t.setMarginLeft("12");               
    t.setFont("verdana");
    t.setFontPt("7");
    t.settaFiltro("");
    DbOperationSQL dbOpSQL;
    try {
      SessioneDb.getInstance().addAlias("oracle.", "oracle.jdbc.driver.OracleDriver");
     dbOpSQL= new DbOperationSQL("oracle.", "jdbc:oracle:thin:@10.98.0.5:1521:si3", "GDM", "GDM");
      String sql="glossario_tree_insert";

      t.loadFromDb(dbOpSQL,sql,"AD4");
      t.display();
		      		   
      System.out.println(t.getOut());
      //free(dbOpSQL);
    } catch(Exception ex) {
		 // free(dbOpSQL);   
      ex.printStackTrace();
    }


        
  }
}
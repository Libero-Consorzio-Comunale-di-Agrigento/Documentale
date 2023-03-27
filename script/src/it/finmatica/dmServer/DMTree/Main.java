package it.finmatica.dmServer.DMTree;

//import it.finmatica.dmServer.util.Global;
//import java.io.*;
//import java.sql.*;
//import java.util.*;
import it.finmatica.jfc.dbUtil.*;
//import it.finmatica.jfc.utility.DateUtility;

public class Main       
{ 
  public static String getTreeViewSQL()
  {
         String user="GDM";
         String ruolo="AMM";
    	   String sWrkSp=null;
    	   
         String sql,sqlVerElimina,sqlVerModifica,sqlVerEliminaQ,sqlVerModificaQ,sqlVerDocCartel,sqlVerCompetenzeQ,sqlVerCompetenze;

		     sqlVerElimina=",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(ID_OGGETTO), 'D', '";
		     sqlVerElimina+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompDelOgg ";

  		   sqlVerModifica=",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(ID_OGGETTO), 'U', '";
		     sqlVerModifica+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompModOgg ";

		     sqlVerCompetenze=",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(ID_OGGETTO), 'M', '";
		     sqlVerCompetenze+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompCompOgg ";

		     sqlVerEliminaQ=",GDM_COMPETENZA.GDM_VERIFICA('QUERY',ID_OGGETTO, 'D', '";
		     sqlVerEliminaQ+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompDelOgg ";

  		   sqlVerModificaQ=",GDM_COMPETENZA.GDM_VERIFICA('QUERY',ID_OGGETTO, 'U', '";
		     sqlVerModificaQ+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompModOgg ";

		     sqlVerCompetenzeQ=",GDM_COMPETENZA.GDM_VERIFICA('QUERY',ID_OGGETTO, 'M', '";
		     sqlVerCompetenzeQ+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompCompOgg ";

		     sqlVerDocCartel=",GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(ID_OGGETTO), 'C', '";
		     sqlVerDocCartel+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy')) CompDocCartel ";

		     if (sWrkSp==null)
         {
		       sql= "SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'2' wrksp,'"+ruolo+"' ruolo "+sqlVerElimina+sqlVerModifica+sqlVerDocCartel+sqlVerCompetenze+" FROM GD4_TREE_USER ";
		       sql+="WHERE GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(ID_OGGETTO), 'L', '"+user+"',  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
  			   sql+="AND TIPO_OGGETTO = 'C' ";
           sql+="UNION ALL ";
			     sql+="SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'2' wrksp,'"+ruolo+"' ruolo "+sqlVerEliminaQ+sqlVerModificaQ+",-1"+sqlVerCompetenzeQ+" FROM GD4_TREE_USER ";
			     sql+="WHERE  GDM_COMPETENZA.GDM_VERIFICA('QUERY',ID_OGGETTO, 'L', '"+user+"',  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
			     sql+="AND TIPO_OGGETTO = 'Q'"; 	
			     sql+="UNION ALL ";
			     sql+="SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'2' wrksp,'"+ruolo+"' ruolo "+sqlVerElimina+sqlVerModifica+",-1"+sqlVerCompetenze+" FROM GD4_TREE_USER ";
			     sql+="WHERE  GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',ID_OGGETTO, 'L', '"+user+"' , F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
			     sql+="AND TIPO_OGGETTO = 'D'"; 			   
			  }
		    else if (sWrkSp.equals("1")) {
		       sql= "SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, tree.ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'1' wrksp,'"+ruolo+"' ruolo "+sqlVerElimina+sqlVerModifica+sqlVerDocCartel+sqlVerCompetenze+" FROM GD4_TREE_SYS tree,cartelle c ";
		       sql+="WHERE tree.ID_OGGETTO=c.ID_CARTELLA and (GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(tree.ID_OGGETTO), 'L', '"+user+"',  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1)";
  			   sql+="AND tree.TIPO_OGGETTO = 'C' ";
			     sql+="UNION ALL ";
			     sql+="SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'1' wrksp,'"+ruolo+"' ruolo "+sqlVerEliminaQ+sqlVerModificaQ+",-1"+sqlVerCompetenzeQ+" FROM GD4_TREE_SYS ";
			     sql+="WHERE  GDM_COMPETENZA.GDM_VERIFICA('QUERY',ID_OGGETTO, 'L', '"+user+"',  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
			     sql+="AND TIPO_OGGETTO = 'Q'"; 		  
			     sql+="UNION ALL ";
			     sql+="SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'1' wrksp,'"+ruolo+"' ruolo "+sqlVerElimina+sqlVerModifica+",-1"+sqlVerCompetenze+" FROM GD4_TREE_SYS ";
			     sql+="WHERE  GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',ID_OGGETTO, 'L', '"+user+"' , F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1";
			     sql+="AND TIPO_OGGETTO = 'D'";
			 }
		   else if (sWrkSp.equals("2")) {
		      sql= "SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'2' wrksp,'"+ruolo+"' ruolo "+sqlVerElimina+sqlVerModifica+sqlVerDocCartel+sqlVerCompetenze+" FROM GD4_TREE_USER ";
		      sql+="WHERE GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(ID_OGGETTO), 'L', '"+user+"',  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
  			  sql+="AND TIPO_OGGETTO = 'C' ";
			    sql+="UNION ALL ";
			    sql+="SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'2' wrksp,'"+ruolo+"' ruolo "+sqlVerEliminaQ+sqlVerModificaQ+",-1"+sqlVerCompetenzeQ+" FROM GD4_TREE_USER ";
			    sql+="WHERE  GDM_COMPETENZA.GDM_VERIFICA('QUERY',ID_OGGETTO, 'L', '"+user+"',  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
			    sql+="AND TIPO_OGGETTO = 'Q'"; 		   
			    sql+="UNION ALL ";
			    sql+="SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'2' wrksp,'"+ruolo+"' ruolo "+sqlVerElimina+sqlVerModifica+",-1"+sqlVerCompetenzeQ+" FROM GD4_TREE_USER ";
			    sql+="WHERE  GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',ID_OGGETTO, 'L', '"+user+"' , F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 ";
			    sql+="AND TIPO_OGGETTO = 'D'";      
		   }
		   else {
		      sql= "SELECT PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO,'3' wrksp,'"+ruolo+"' ruolo "+sqlVerEliminaQ+sqlVerModificaQ+",-1"+sqlVerCompetenzeQ+" FROM GD4_TREE_QUERY ";
          sql+="WHERE GDM_COMPETENZA.SI4_VERIFICA ";
			    sql+="('QUERY',ID_OGGETTO, 'L',  "+user+",  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1";
			    //sql+="('QUERY',ID_OGGETTO, 'L',  upper('"+user+"'),  F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1";
				
		   }	       		   
	return sql;
    
  }
  

  public static void main(String[] args)
  {
         TreeView t = new TreeView();
 
         try
         {
//          SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");
//
//          DbOperationSQL dbOp = new DbOperationSQL("oracle.","jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
//
//          String sql=getTreeViewSQL();
//	        //System.out.println(" SQL= "+sql);
//          t.setWorkSpace(null);
//          t.loadFromDb(dbOp,sql,null,null);
//          
//          t.display(dbOp);
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }

          
        
         // String sToggle=t.findNodeToggle(t.getNodes(),"C"+IDToggle);
		  //  System.out.println("TOGGLE= "+sToggle);
          //System.out.println(t.getOut());
 }
}
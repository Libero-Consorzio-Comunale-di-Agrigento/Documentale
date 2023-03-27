package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global; 
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestServiziEsterni {
	  public static void main(String[] args) throws Exception  {
		  String fileA="A.txt", pathFileA="C:\\temp\\FILE\\A.txt";
		  String fileAMod="AModificato.txt", pathFileAMod="C:\\temp\\FILE\\AModificato.txt";
		  String fileAMod2="AModificato2.txt", pathFileAMod2="C:\\temp\\FILE\\AModificato2.txt";
		  String fileB="B.txt", pathFileB="C:\\temp\\FILE\\B.txt";
		  String fileC="C.txt", pathFileC="C:\\temp\\FILE\\C.txt";
		  String fileCMod="CModificato.txt", pathFileCMod="C:\\temp\\FILE\\CModificato.txt";
		  
		  Class.forName("oracle.jdbc.driver.OracleDriver");
		  
		  Connection conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora03:1521:GDMTEST","GDM","GDM");
		
		 
		  
		  
		  //14430191
		  //Profilo p = new Profilo("M_ORIZZONTALE","TESTADS");
		  Profilo p = new Profilo("14430191");
		  p.initVarEnv("GDM","",conn);
		  
		  p.settaValore("RICH_TEXT_AREA","2018");	
		  
		  stepCaricaFile(p, conn,pathFileA, fileA ,"1"); 
		  leggiFile( p, conn, fileA,"2");
		  stepCaricaFile(p, conn,pathFileB, fileB ,"3"); 
		  renameFile(p,fileA,fileAMod,pathFileAMod,"4" );
		  leggiFile( p, conn, fileAMod,"5");
		  leggiFile( p, conn, fileB,"6");
		  updateModello(false,conn);
		  stepCaricaFile(p, conn,pathFileC, fileC ,"7"); 
		  updateModello(true,conn);
		  leggiFile( p, conn, fileAMod,"8");
		  leggiFile( p, conn, fileB,"9");
		  leggiFile( p, conn, fileC,"10");
		  
		  renameFile(p,fileAMod,fileAMod2,pathFileAMod2,"11" );
		  renameFile(p,fileC,fileCMod,pathFileCMod,"12" );
		  
		  leggiFile( p, conn, fileAMod2,"13");
		  leggiFile( p, conn, fileB,"14");
		  leggiFile( p, conn, fileCMod,"15");
		  
		  deleteFile( p, fileAMod2, "16");
		  deleteFile( p, fileCMod, "17");
		  
		  p.accedi(Global.ACCESS_ATTACH);
		  System.out.println("FILE PRESENTI-->"+p.getlistaFile());
		  
		  deleteFile( p, fileB, "18");
		  
		  p.accedi(Global.ACCESS_ATTACH);
		  System.out.println("FILE PRESENTI-->"+p.getlistaFile());
 		  
		  
    	  conn.commit();
		  conn.close();
	  }
	  
	  private static void stepCaricaFile(Profilo p,Connection conn,String pathFile, String nomeFile, String step ) throws Exception {
		  System.out.println("STEP N. "+step+" CARICA "+nomeFile);
		  LetturaScritturaFileFS f1 = new LetturaScritturaFileFS(pathFile);
    	  InputStream in =f1.leggiFile();
    	  
    	  p.setFileName(nomeFile, in);
    	  if (!p.salva().booleanValue()) throw new Exception(p.getError());
    	  //System.out.println(p.getDocNumber());
	  }
	  
	  private static void leggiFile(Profilo p,Connection conn,String nomeFile, String step) throws Exception {
		  System.out.print("STEP N. "+step+" LEGGI "+nomeFile);
		  p.accedi(Global.ACCESS_ATTACH);
		  InputStream in =p.getFileStream(nomeFile);
		  byte[] b = Global.getBytesToEndOfStream(in);
		  String s = new String(b);
		  System.out.println(" --> "+s);
	  }
	  
	  private static void deleteFile(Profilo p, String nomeFile, String step) throws Exception {
		  System.out.println("STEP N. "+step+" CANCELLA "+nomeFile);
		  
		  p.setDeleteFileName(nomeFile);
		  if (!p.salva().booleanValue()) throw new Exception(p.getError());
	  }
	  
	  private static void renameFile(Profilo p, String nomeFile, String nuovoFile, String nuovoPathFile, String step) throws Exception {		  
		  System.out.println("STEP N. "+step+" RINOMINA "+nomeFile+" IN "+nuovoFile);
		  LetturaScritturaFileFS f1 = new LetturaScritturaFileFS(nuovoPathFile);
    	  InputStream in =f1.leggiFile();
		  p.renameFileName(nomeFile, nuovoFile, in);
		  if (!p.salva().booleanValue()) throw new Exception(p.getError());
	  }
	  
	  private static void updateModello(boolean bServEsterno,Connection conn) throws Exception {
		  String valore=(bServEsterno)?"3":"null";
		  String sql="UPDATE MODELLI SET id_servizio_gdmsyncro="+valore+" where codice_modello='M_ORIZZONTALE'";
		  SessioneDb.getInstance().addAlias("oracle.", "oracle.jdbc.driver.OracleDriver");	
		  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(conn, 0);
		  
		  
		  dbOp.setStatement(sql);     			   
		  dbOp.execute();
		  conn.commit();
	  }
}

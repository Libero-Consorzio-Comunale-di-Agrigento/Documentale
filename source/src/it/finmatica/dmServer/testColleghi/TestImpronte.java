package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.Impronta.ImprontaAllegati;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestImpronte {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception  {
		 Connection conn=null;
		 Class.forName("oracle.jdbc.driver.OracleDriver");
		 conn=DriverManager.getConnection("jdbc:oracle:thin:@svi-ora03:1521:GDMTEST","GDM","GDM");       
		 conn.setAutoCommit(false);
		
		 //creaDocConImpronte(conn);
		 allineaTutteImpronteDiUnDoc("12864124", conn);
	}
	public static void creaDocConImpronte(Connection cn) throws Exception  {
		Profilo p = new Profilo("M_ORIZZONTALE","TESTADS"/*"12864124"*/);
		 
		 p.initVarEnv("GDM","",cn);
		 
		 p.setFileName("C:/temp/testImpronta/Impronta1.txt");
		 //p.setFileName("C:/temp/testImpronta/Impronta2.txt");
		 
		// LetturaScritturaFileFS fs = new LetturaScritturaFileFS("C:/temp/testImpronta/Impronta3.txt");
		 
		 //p.renameFileName("Impronta1.txt", "Impronta3.txt", fs.leggiFile());
		 
		// p.setDeleteFileName("Impronta3.txt");
		 
		 p.salva();
		 System.out.println(p.getError());
		 cn.commit();
		 System.out.println(p.getDocNumber());
	}
	
	public static void allineaTutteImpronteDiUnDoc(String idDoc, Connection cn) throws Exception  {
		 IDbOperationSQL dbOp = null;
		 
		 try {
			 dbOp=SessioneDb.getInstance().createIDbOperationSQL(cn,0);
			  dbOp.autoCommitOff();
			  
			  ImprontaAllegati ia = new ImprontaAllegati(idDoc,dbOp);
			  ia.sistemaImpronte();
			  
			  try{dbOp.commit();}catch(Exception e){}
			  try{cn.commit();}catch(Exception e){}			
			  try{dbOp.close();}catch(Exception e){}
		 }
		 catch (Exception e) {
			 try{dbOp.rollback();}catch(Exception ei){}
			 try{cn.rollback();}catch(Exception ei){}	
			 try{dbOp.close();}catch(Exception ei){}
			 throw e;
		 }
	}

}

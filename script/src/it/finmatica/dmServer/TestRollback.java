package it.finmatica.dmServer;

import it.finmatica.dmServer.management.Profilo;
//import it.finmatica.jfc.dbUtil.DbOperationSQL;
import java.sql.*;

public class TestRollback {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Connection conn=null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
		}
		catch (Exception e) {
			System.out.println("Errore in connessione ");
			e.printStackTrace();
			return;
		}
		
		System.out.println("cn--->"+conn);
		
		/*Profilo p1 = new Profilo("F1","MANNY"); 		
		Profilo p2 = new Profilo("F1","MANNY");*/
		
		Profilo p1 = new Profilo("43239"); 		
		Profilo p2 = new Profilo("43240");
		
		p1.initVarEnv("GDM","GDM", conn);
		p2.initVarEnv("GDM","GDM", conn);
		
		p1.settaValore("COGNOMEPILOTA","CognomeProfilo1");
		p2.settaValore("COGNOMEPILOTA","CognomeProfilo2");
		
		p1.settaValore("NOMEPILOTA","NomeProfilo1:::");
		
		p2.settaValore("NOMEPILOTA","NomeProfilo2:::");
		//p2.settaValore("DATA_NASCITA","3333");		
		
		try {
			if (!p2.salva().booleanValue()) {
				System.out.println("Errore in salva 2\n"+p2.getError());
			}
		}		
		catch (Exception e) {
			System.out.println("Errore in salva 2");
			e.printStackTrace();
			//return;
		}
		
		try {
			if (!p1.salva().booleanValue()) {
				System.out.println("Errore in salva 1\n"+p1.getError());
			}
		}
		catch (Exception e) {
			System.out.println("Errore in salva 1");
			e.printStackTrace();
			//return;
		}
		
		conn.commit();		
		conn.close();

	}

}

package it.finmatica.dmServer.testColleghi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import it.finmatica.jfc.dbUtil.*;

public class TestXMLTYPE  {
	
	    public static void main(String[] args) throws Exception {
	    	   Connection conn=null;
		       Class.forName("oracle.jdbc.driver.OracleDriver");
		   	   //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");
		       //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.19:1521:PRMOD","GDM","GDM");
		   	   conn=DriverManager.getConnection("jdbc:oracle:thin:@10.29.102.2:1521:DOC01","GDM","GDM");
		   	   
		   	   IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(conn,0);
		   	   
		   	String s ="select F_USER('GDM') from dual";
		   	   //System.out.println(dbOp.getNextKeyFromSequence("CODR_SQ"));
		   	dbOp.setStatement(s);
		   	dbOp.execute();
		   	ResultSet rst = dbOp.getRstSet();
		   	
		   	if (rst.next()) System.out.println(rst.getString(1));
		   	  
		   	   dbOp.close();
		   	   conn.close();
	    }
}

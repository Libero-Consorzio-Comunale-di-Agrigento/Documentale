package it.finmatica.dmServer.util;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;

import java.sql.Connection;
import java.sql.DriverManager;

public class ManageToken {
	private String user;
	private Global gl;
	private IDbOperationSQL dbOp;
	
	public ManageToken(String user, Global gl, IDbOperationSQL dbOp) {
		this.user=user;
		this.gl=gl; 
		this.dbOp=dbOp;
	}
	
	public String generateToken() throws Exception {
		String token="";
		ManageConnection mc = new ManageConnection();
		
		String ad4Passwd=mc.retrievePassword(dbOp,"AD4","AD4");
		
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection conn=DriverManager.getConnection(this.gl.URL_ORACLE_PARAM,"AD4",ad4Passwd);
		conn.setAutoCommit(false);
		it.finmatica.login.TokenManager tk = new it.finmatica.login.TokenManager(conn,this.user);
				
		token=tk.getToken();
		
		try {conn.close();}catch (Exception e){}
		
		return token;
	}
}

package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import javax.servlet.http.HttpServletRequest;

public class Collegamenti {
	
	private IDbOperationSQL dbOp;  
	private Environment vu;  
	private CCS_Common CCS_common;  
	private HttpServletRequest req;
	private DMServer4j log;
	private String area="GDMSYS";
	private String tipoRelazione="LINK_DOC";
    private String lista_coll;
	private String lista_doc;

	public Collegamenti(HttpServletRequest newreq,CCS_Common newCommon,String coll,String docs) throws Exception {
		req=newreq;
		CCS_common=newCommon;
		log= new DMServer4j(Collegamenti.class,CCS_common); 
		if (!CCS_common.dataSource.equals("")) {
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
            vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
		}
		else 
		{
			vu=CCS_common.ev;
			dbOp=CCS_common.ev.getDbOp();
		}
		
		if(docs!=null && !docs.equals("")){ 
		  lista_doc="@"+docs.replaceAll("D","")+"@";
		}  
		if(coll!=null && !coll.equals("")){ 
		  lista_coll="@"+coll.replaceAll("D","")+"@";
		}  
	}
	
	
	public void eseguiCollegamento() throws Exception{
		    if(lista_doc!=null && lista_coll!=null) {
		       insertCollegamento();	
		    }		
	}
	
	private void insertCollegamento() throws Exception {
			StringBuffer sql=new StringBuffer();
		    IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		      
		    try {
		      sql.append(" BEGIN ");
		      sql.append("  GDC_UTILITY_PKG.F_CREA_COLLEGAMENTI(:LISTADOC,:LISTACOLL,:USER);");
		      sql.append(" END;");
		      dbOpSQL.setStatement(sql.toString());
		      dbOpSQL.setParameter(":LISTADOC",lista_doc);
		      dbOpSQL.setParameter(":LISTACOLL",lista_coll);
		      dbOpSQL.setParameter(":USER",CCS_common.user);
		      dbOpSQL.execute();
		      CCS_common.closeConnection(dbOpSQL,true);
		    }		      
			catch (Exception e) {
			  CCS_common.closeConnection(dbOpSQL,false);		
			  log.log_error("Collegamenti::insertCollegamento() - SQL:"+sql);
			  throw e;
			}
	}

}

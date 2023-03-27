package it.finmatica.dmServer.jdms;

import java.sql.ResultSet;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

public class JDMSLink {
	
	/**
	 * Variabili private 
	*/	
    private String tag;
    private String url;
    private String icona;
    private String tooltip;
    private String nomeIcona;
    
    
    // Integrazione 
    private String id_tipo_documento;
    private String numtag;
    private String parametro;
    private IDbOperationSQL	dbOpSQL;
    private DMServer4j log;
    
    
    /**
	 * Costruttore generico vuoto.
	 */
	public JDMSLink(){}
	
	
	public JDMSLink(String id_tipodoc,String tag,String par,IDbOperationSQL dbOp){
		   id_tipo_documento = id_tipodoc;
		   numtag = tag;
		   parametro = par;
		   dbOpSQL = dbOp;
		   log= new DMServer4j(JDMSLink.class);
	}
	
	
   /**
	 * Costruttore generico vuoto.
	 */
	public void retrieveJDMLink() throws Exception {
		   StringBuffer sStm = new StringBuffer();
		   IDbOperationSQL	dbConn = null;
	   	   String ico="",tooltip="",urlpage="";
			
		   try
			{
			   String result=retrieveParametro(parametro);
			   if(result.equals("S")){
				   ResultSet rs=null;
				   dbConn = SessioneDb.getInstance().createIDbOperationSQL(dbOpSQL.getConn(),0);
				   sStm.append("select url,icona,tooltip from jdms_link where id_tipodoc = :IDTIPODOC and tag = :TAG");
				   dbConn.setStatement(sStm.toString());       
				   dbConn.setParameter(":IDTIPODOC",id_tipo_documento);
				   dbConn.setParameter(":TAG",numtag);
				   dbConn.execute();
				   rs=dbConn.getRstSet();
		           if (rs.next()) {
		              urlpage=rs.getString("url");
		           	  ico=rs.getString("icona");
		              tooltip=rs.getString("tooltip");
		           }  
				   dbConn.close();				   
			   } 
			   
			   this.setICONA(ico);
			   this.setTOOLTIP(tooltip);
			   this.setURL(urlpage);
		   }
		   catch (Exception e) 
		   {
			  dbConn.close();  
			  log.log_error("JDMSLink()::getURLPage() - SQL: "+sStm.toString());
		   }   
	}
	
	 /**
	   * Recupero parametro dalla tabella PARAMETRI
	   * del tipo_moedllo=@DMSERVER@
	   * 
	   * @param 			nome parametro
	   * @return String 	valore
	   * 
	 */
	 private String retrieveParametro(String parametro) throws Exception
	 {
		     IDbOperationSQL dbConn = null;
		     String rstPar="";
		     StringBuffer sStm = new StringBuffer();
		     
	         log.log_info("Inizio Recupero Parametro: "+parametro+"  - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	         try
	         {
	            ResultSet rs=null;
	            dbConn = SessioneDb.getInstance().createIDbOperationSQL(dbOpSQL.getConn(),0);
	            sStm.append(" SELECT VALORE FROM PARAMETRI ");
	            sStm.append(" WHERE CODICE = :PARAMETRO ");
	            sStm.append(" AND TIPO_MODELLO='@DMSERVER@'");
	            dbConn.setStatement(sStm.toString());
	            dbConn.setParameter(":PARAMETRO",parametro);
	            dbConn.execute();
	     	    rs=dbConn.getRstSet();
	            if (rs.next()) 
	              rstPar=rs.getString(1);
	            else
	              rstPar="N";	
	            dbConn.close();
	         }
	         catch (Exception e) {   
	           dbConn.close();	 
	      	   log.log_error("JDMSLink()::retriveParametro("+parametro+") - SQL: "+sStm.toString());
	      	   throw e;
	         }  
	         log.log_info("Fine Recupero Parametro: - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	         return  rstPar;   
	 }    
	 
	
	/** Metodi di GET e SET  utilizzati dalla WorkArea*/
	public String getTAG() {
		   return tag;
	} 
		 
	public void setTAG(String t) {
	 	   tag=t;
	} 
	
	public String getURL() {
		   return url;
	} 
		 
	public void setURL(String u) {
	 	   url=u;
	}
	
	public String getICONA() {
		   return icona;
	} 
		 
	public void setICONA(String i) {
	 	   icona=i;
	}
	
	public String getTOOLTIP() {
		   return tooltip;
	} 
		 
	public void setTOOLTIP(String t) {
		   tooltip=t;
	} 
	
	public String getNOMEICONA() {
		   return nomeIcona;
	} 
		 
	public void setNOMEICONA(String n) {
	 	   nomeIcona=n;
	}
	

}

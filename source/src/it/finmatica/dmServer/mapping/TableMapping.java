package it.finmatica.dmServer.mapping;

import java.sql.ResultSet;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class TableMapping {
	   Environment env;
       IDbOperationSQL  dbOp;
       boolean bIsNew;
       
	   /**
	    * ente e applicativo dai quali cercare il DM
	   */	   
	   private String ente, applicativo;	   
	   
	   public TableMapping(Environment newEnv, String ente, String appl) {
		      env=newEnv;
		      this.ente=ente;
		      this.applicativo=appl;
	   }
	   
	   
	   /**
	    * Restituisce il tipoDoc mappato sul parametro
	    * tipoDoc input a partire da ente/applicativo
	    * passati nel costruttore.
	    * Restituisce se stesso se non trova corrispondenza
	    * 
	    * @param tipoDoc tipoDoc da mappare
	    * @return tipoDoc mappato
	   */	 	   
	   public String getMappingTipoDoc(String tipoDoc) throws Exception {
	          String tipo="";
	          	         
           
              try {
                StringBuffer sStm = new StringBuffer();
          
                dbOp = connect();

                sStm.append("select nome_dm ");
                sStm.append("from MAPPING_TIPIDOC ");
                sStm.append("where applicativo='"+applicativo+"'");
                sStm.append("  and ente='"+ente+"'");
                sStm.append("  and nome='"+tipoDoc+"'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();
             
                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() ) 
                    tipo = rst.getString(1);               
                else {
                    tipo = tipoDoc;     
                }
                         
         	    close();
         	  }
              catch (Exception e) {               
                close();
                throw new Exception("TableMapping::getMappingTipoDoc('"+tipoDoc+"')\n" + e.getMessage());
              }    
           
              return tipo;
	   }
	   
	   /**
	    * Restituisce il tipoDoc mappato sul parametro
	    * tipoDoc input a partire da ente/applicativo
	    * passati nel costruttore.
	    * Restituisce se stesso se non trova corrispondenza
	    * 
	    * @param tipoDoc tipoDoc da mappare
	    * @return tipoDoc mappato
	   */	 	   
	   public String getMappingCampo(String tipoDoc, boolean bIsTipoDocDM, String campo) throws Exception {
	          String nomeCampo=campo;	          	        
           
              try {
                StringBuffer sStm = new StringBuffer();
          
                dbOp = connect();

                sStm.append("select c.nome_dm ");
                sStm.append("from MAPPING_TIPIDOC td,MAPPING_CAMPI c ");
                sStm.append("where applicativo='"+applicativo+"'");
                sStm.append("  and ente='"+ente+"'");
                if (bIsTipoDocDM) 
                	sStm.append("  and td.nome_dm='"+tipoDoc+"'");
                else
                	sStm.append("  and td.nome='"+tipoDoc+"'");
                sStm.append("  and td.ID_MAPPING_TIPODOC=c.ID_MAPPING_TIPODOC");
                sStm.append("  and c.nome='"+campo+"'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();
             
                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() ) 
                    nomeCampo = rst.getString(1);               
                else {
                    nomeCampo = campo;     
                }
                         
         	    close();
         	  }
              catch (Exception e) {               
                close();
                throw new Exception("TableMapping::getMappingCampo('"+campo+"')\n" + e.getMessage());
              }    
           
              return nomeCampo;
	   }	   
	   
	     private IDbOperationSQL connect() throws Exception {
	    	 	if (env.getDbOp()==null) {
	    	 		bIsNew=true;
	    	 		return (new ManageConnection(env.Global)).connectToDB();
	    	 	}
        
	    	 	return env.getDbOp();
	     }
  
	     private void close() throws Exception {
	    	 	if (bIsNew) (new ManageConnection(env.Global)).disconnectFromDB(dbOp,true,false);        
	     }
}

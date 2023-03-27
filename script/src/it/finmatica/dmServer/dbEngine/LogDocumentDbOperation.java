package it.finmatica.dmServer.dbEngine;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.GD4_Oggetti_File_Log;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class LogDocumentDbOperation extends MasterEngine {
	   private long ultimaVersione=0;



	   

	   public LogDocumentDbOperation(Environment en,IDbOperationSQL dbOp, String msgElapsedTime) {
		      super(en,dbOp,msgElapsedTime);
	   }	   
	   
	   public int getACLO_SQ() throws Exception {
		      return executeSqlSequence("ACLO_SQ");
	   }
	   
	   public void insertActivityLog(long lVersione, int id_log, String idDoc, String tipoAzione) throws Exception {
		   insertActivityLog( lVersione,  id_log,  idDoc,  tipoAzione, false);
	   }
	   
	   public void insertActivityLog(long lVersione, int id_log, String idDoc, String tipoAzione, boolean creaVersione) throws Exception {      
		   	  super.appendStatement("insert into activity_log (");
	          super.appendStatement("id_log, id_documento, tipo_azione,");
	          if (lVersione!=0 || creaVersione) {
	        	  super.appendStatement("VERSIONE,");
	          }	          
	          super.appendStatement("data_aggiornamento,utente_aggiornamento) values ");
	          super.appendStatement("("+ id_log );
	          super.appendStatement(","+ idDoc  );
	          super.appendStatement(",'"+ tipoAzione+"'");
	          if (lVersione!=0) {
	        	 super.appendStatement(","+lVersione);
	        	 ultimaVersione=lVersione;
	         } 
	          else if (creaVersione) {
	        	  super.appendStatement(","+id_log);
	        	  ultimaVersione=id_log;
	          }
	         super.appendStatement(",sysdate,'"+ super.vEnv.getUser() +"')");
	         
	         super.executeSql("insertActivityLog");
	   } 
	   
	   public void insertAllVaLog(String idDocumento, Vector vListaIdModificati, String typeLog, int id_log) throws Exception {  
		   	  super.appendStatement("insert into VALORI_LOG (");
	          super.appendStatement("id_valore_log, id_log, id_valore, valore_numero,valore_data,valore_clob) ");		         
	          super.appendStatement("select VALOG_SQ.nextval,"+id_log+",id_valore, valore_numero,valore_data,valore_clob ");
	          super.appendStatement("from valori v ,campi_documento c, modelli m, dati d where id_documento="+idDocumento+" and ");
	          super.appendStatement("v.id_campo=c.id_campo and m.id_tipodoc=c.id_tipodoc and d.area=f_area_dato(m.area,c.nome) and c.nome=d.dato ");
	          for(int i=0;i<vListaIdModificati.size();i++) {
	        	 if (i==0) super.appendStatement("and v.id_valore in (");
	        	 super.appendStatement(""+vListaIdModificati.get(i));
	        	 if (i==vListaIdModificati.size()-1) 
	        		 super.appendStatement(")");
	        	 else
	        		 super.appendStatement(",");
	          }
	         
	         if (typeLog.equals(Global.TYPE_MAX_LOG) || typeLog.equals(Global.TYPE_STD_LOG))
	             super.appendStatement("and nvl(d.tipo_log,0)=1");
	         
	         super.executeSql("insertAllVaLog - INSERIMENTO DI TUTTI I VALORI LOG");
	   }
	   
	   public void insertAllVaLogHorizontal(String idDocumento, String nomeTabHor, Vector vListaCampiModificati, Vector vlistaTipiCampoModificati,
			   								int id_log, boolean bNonRipetereUguali) throws Exception { 
		   	  //par passato dall'esterno...
		     
		      String maxIdLogRipUguali="";
		      List<String> listaIdLogMaxCampiModificati = new ArrayList<String>();
		      
		      if (bNonRipetereUguali) {		    	  		    	  
		    	  for(int i=0;i<vListaCampiModificati.size();i++) {
		    		  		    		  
		    		  super.appendStatement("select max(activity_log.id_log) " +
	  						"	from activity_log,valori_log " +
	  						"   where id_documento="+idDocumento+" " +
	  						"    and activity_log.id_log<"+id_log +
	  						"    and activity_log.id_log=valori_log.id_log   "+
	  						"    and valori_log.colonna||''='"+vListaCampiModificati.get(i)+"' " );
		    		  
		    		  if (i!=vListaCampiModificati.size() -1 ) super.appendStatement(" UNION ALL ");
	   	   
				   	 
		    	  }
		    	  
		    	  ResultSet rst = super.executeSqlResultSet();
			   	  while (rst.next()) {
			   		listaIdLogMaxCampiModificati.add(Global.nvl(rst.getString(1),""));
			   	  }
		    	 
		      }
		   
			  super.appendStatement("INSERT INTO VALORI_LOG (ID_VALORE_LOG,ID_LOG,VALORE_CLOB,VALORE_NUMERO,VALORE_DATA,COLONNA) ");
			  super.appendStatement("select VALOG_SQ.nextval,"+id_log+",vaclob,vanumero,vadata,colonna from ( ");
				
			  for(int i=0;i<vListaCampiModificati.size();i++) {
					String nomeCampo=""+vListaCampiModificati.get(i);	
					if (listaIdLogMaxCampiModificati.size()>0)
						maxIdLogRipUguali= listaIdLogMaxCampiModificati.get(i);
					else
						maxIdLogRipUguali="";
					String sSelect=sceltaColonna(nomeCampo,""+vlistaTipiCampoModificati.get(i));
					
					if (maxIdLogRipUguali.equals("")) {					
						
						super.appendStatement("SELECT "+sSelect+",");
						super.appendStatement("       '"+nomeCampo+"' AS COLONNA ");
						super.appendStatement("       FROM "+nomeTabHor+" WHERE "); 
						super.appendStatement("       ID_DOCUMENTO="+idDocumento+" ");					
					}
					else {
						
						
						super.appendStatement("SELECT "+sSelect+",");
						super.appendStatement("       '"+nomeCampo+"' AS COLONNA  ");
						super.appendStatement("       FROM "+nomeTabHor+", valori_log WHERE "); 
						super.appendStatement("       ID_DOCUMENTO="+idDocumento+" AND ");
						super.appendStatement("        valori_log.id_log="+maxIdLogRipUguali+" AND ");	
						super.appendStatement("        valori_log.colonna='"+nomeCampo+"' AND ");
						
						String colonnaValog="", colonnaTabella="";
						if ((""+vlistaTipiCampoModificati.get(i)).equals("D")) {
							colonnaValog="VALORE_DATA";
							colonnaTabella=nomeCampo;
						}
						else if ((""+vlistaTipiCampoModificati.get(i)).equals("N")) {
							colonnaValog="VALORE_NUMERO";
							colonnaTabella=nomeCampo;
						}
						else {
							colonnaValog="to_char(valori_log.VALORE_CLOB)";
							colonnaTabella="to_char("+nomeCampo+")";
						}
						
						super.appendStatement(" (  ");
						super.appendStatement("    (  "+colonnaValog+" <> "+colonnaTabella+" ) or ");
						super.appendStatement("    ( "+colonnaValog+" is null and  "+colonnaTabella+"  is not null ) or ");
						super.appendStatement("    (  "+colonnaValog+" is not null and  "+colonnaTabella+" is null )  ");
			            super.appendStatement("  ) ");
					}
					
					if (i!=vListaCampiModificati.size() -1 ) super.appendStatement(" UNION ALL ");
			  }
				
			  super.appendStatement(" )");
			  			  
			  super.executeSql("insertAllVaLogHorizontal - INSERIMENTO DI TUTTI I VALORI LOG (MODALITA' ORIZZONTALE)");
	   }
	   
	   public void insertAllOgfiLog(String idDocumento, Vector vListaIdModificati, boolean bElimina, int id_log, String tipoAzione, boolean bisNameIdModificati, boolean bNonRipetereUguali, Date dataLog) throws Exception {
		   	  super.appendStatement("INSERT INTO OGGETTI_FILE_LOG (");
		   	  super.appendStatement("ID_OGGETTO_FILE_LOG,");
		   	  super.appendStatement("ID_LOG,");
	          super.appendStatement("ID_OGGETTO_FILE,"); 
	          super.appendStatement("FILENAME,");
	          super.appendStatement("NOME_FORMATO,");
	          super.appendStatement("TESTOOCR,");
	          //PER ADESSO NON INDICIZZO
	          //super.appendStatement("\"FILE\",");
	          super.appendStatement("ALLEGATO,");
	          super.appendStatement("DATA_AGGIORNAMENTO,");
	          super.appendStatement("UTENTE_AGGIORNAMENTO,");
	          super.appendStatement("DATA_OPERAZIONE,");
	          super.appendStatement("UTENTE_OPERAZIONE,");
	          super.appendStatement("TIPO_OPERAZIONE,");
	          super.appendStatement("PATH_FILE, IMPRONTA, PATH_FILE_ROOT, PATH_FILE_ROOT_ORACLE) ");

	          super.appendStatement("SELECT ");
	          super.appendStatement("OGFI_LOG_SQ.NEXTVAL,");
	          super.appendStatement(id_log+",");
	          super.appendStatement("ID_OGGETTO_FILE,"); 
	          super.appendStatement("OGF.FILENAME,");
	          super.appendStatement("FF.NOME,");
	          super.appendStatement("TESTOOCR,");
	          //super.appendStatement("decode(nvl(PATH_FILE,''),'',\"FILE\",bfilename(F_GETDIRECTORY_AREA_NAME("+idDocumento+"),'"+idDocumento+"/LOG_"+id_log+"/'||FILENAME )),");
	          super.appendStatement("ALLEGATO,");
	          if (dataLog==null)
	        	  super.appendStatement("OGF.DATA_AGGIORNAMENTO,");
	          else {
	        	  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");	        	  
	        	  super.appendStatement("to_date('"+dateFormat.format(dataLog)+"','dd/mm/yyyy HH24:mi:ss'),");
	          }
	          super.appendStatement("OGF.UTENTE_AGGIORNAMENTO,");
	          super.appendStatement("SYSDATE,");
	          super.appendStatement("'"+vEnv.getUser()+"',");
	          super.appendStatement("decode(DA_CANCELLARE,'S','"+Global.TYPE_AZIONE_ELIMINA+"','"+tipoAzione+"'), ");
	          super.appendStatement("decode(nvl(OGF.PATH_FILE,''),'','',AREE.ACRONIMO||'\\LOG_'||"+id_log+"), ");
	          super.appendStatement("impronte_file.HASHCODE, PATH_FILE_ROOT, PATH_FILE_ROOT_ORACLE ");
	          super.appendStatement("FROM OGGETTI_FILE OGF, FORMATI_FILE FF, DOCUMENTI , AREE , impronte_file ");
	          super.appendStatement("WHERE OGF.ID_FORMATO=FF.ID_FORMATO ");
	          super.appendStatement("  AND OGF.ID_DOCUMENTO="+idDocumento+" ");
	          super.appendStatement("  AND DOCUMENTI.ID_DOCUMENTO="+idDocumento+" ");
	          super.appendStatement("  AND OGF.ID_DOCUMENTO = impronte_file.ID_DOCUMENTO (+) ");
	          super.appendStatement("  AND OGF.FILENAME = impronte_file.FILENAME (+)  ");	          
	          super.appendStatement("  AND AREE.AREA=DOCUMENTI.AREA ");
	          super.appendStatement("  AND ((nvl(dbms_lob.getlength(OGF.testoocr),0)>0 and id_syncro is not null)  or id_syncro is  null) ");
	          
		      if (!bElimina) {
			        for(int i=0;i<vListaIdModificati.size();i++) {
			        	String colonna="FILENAME";
			        	if (!bisNameIdModificati) colonna="ID_OGGETTO_FILE";
			        	if (i==0) super.appendStatement("AND OGF."+colonna+" in (");
			        	super.appendStatement(""+vListaIdModificati.get(i));
			        	if (i==vListaIdModificati.size()-1) 
			        		super.appendStatement(")");
			        	else
			        		super.appendStatement(",");
			        } 
		      }
		      else {
		    	  super.appendStatement("  AND OGF.DA_CANCELLARE='S' ");
		      }
		      
		      if (bNonRipetereUguali)
	        	  super.appendStatement(" AND impronte_file.HASHCODE <> " +        		 
		          					    " nvl(  ( select  DISTINCT FIRST_VALUE (impronta) OVER (ORDER BY activity_log.id_log desc   ) " +
						        		"     from activity_log,oggetti_file_log  " +
						        		"     where  activity_log.id_log=oggetti_file_log.id_log and " +
						        		"                 oggetti_file_log.id_oggetto_file = OGF.id_oggetto_file  and  activity_log.id_log + 0 <"+id_log+"), 'XXXXXX' )");
		      
		      super.executeSql("insertAllOgfiLog - INSERIMENTO DI TUTTI GLI OGGETTI FILE LOG");
		      
		      //Ciclo su tutti gli idlog della ogfilog e mi calcolo l'impronta
		      //27/10/2016 spostata gestione fuori , nella aggiornadocumento perché se i file sono su FS ancora nn sono stati scritti
		      //           quindi non posso calcolarmi certo qui  l'impronta
		      /*try {
		    	  GD4_Oggetti_File_Log gd4Ogfi = new GD4_Oggetti_File_Log(super.dbOpSql);
		    	  gd4Ogfi.retrieveOggettiFileLog(idDocumento, ""+id_log);
		    	  gd4Ogfi.generaImpronte();
		      }
		      catch(Exception e){
		    	  throw new Exception("LogDocumentDbOperation::insertAllOgfiLog Errore in generazione impronte per oggetti_file_log. Errore = "+e.getMessage());
		      }	*/	   		      
	   }
	   
	   public void f_log_documento(int id_log) throws Exception { 
		   	  /*super.appendStatement("select f_log_documento("+id_log+") from dual");
		   	  
		   	  super.executeSql("f_log_documento");		*/
	   }
	   
	   public ResultSet retrieveActivityLog(int id_log) throws Exception {
		   	  super.appendStatement("select id_documento, tipo_azione from activity_log where id_log="+id_log);
		   	   
		   	  return super.executeSqlResultSet();
	   }

	   public ResultSet retrieveLastActivityLog(String idDocumento) throws Exception {
		      super.appendStatement("select max(id_log) from activity_log where id_documento="+idDocumento+" and tipo_azione in ('M','C','E') ");

		      return super.executeSqlResultSet();
	   }
	   
	   public ResultSet retrieveActivityLogData(String id_documento,String tipoAzione) throws Exception {
		   	  super.appendStatement("select to_char(DATA_AGGIORNAMENTO,'dd/MM/yyyy HH:mi:ss') from activity_log where id_documento="+id_documento+" and tipo_azione='"+tipoAzione+"'");
		   	   
		   	  return super.executeSqlResultSet();
	   }

		private String sceltaColonna(String nomeCampo, String tipoCampo) {
			String sSelect="";

			if (tipoCampo.equals("D"))
				sSelect="TO_CLOB(NULL) vaclob ,TO_NUMBER(NULL) vanumero,"+nomeCampo+" vadata  ";
			else if (tipoCampo.equals("N"))
				sSelect="TO_CLOB(NULL) vaclob ,"+nomeCampo+" vanumero,TO_DATE(NULL) vadata ";
			else
				sSelect="TO_CLOB("+nomeCampo+") vaclob ,TO_NUMBER(NULL) vanumero,TO_DATE(NULL) vadata ";

			return sSelect;
		}
	   
	   public long getUltimaVersione() {
			return ultimaVersione;
	  }
}

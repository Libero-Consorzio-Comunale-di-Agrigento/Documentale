package it.finmatica.dmServer.util;

import java.sql.*;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import it.finmatica.dmServer.jdms.DMServer4j;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

public class EstraiIDDocumenti {

	protected String user;
	protected IDbOperationSQL dbOp;  
    private DMServer4j log; 
    private Vector vlistID;
    private HashMapSet hms;
    private Vector vTipiDocumento;
    private String sWhereFullText="";
    
	public EstraiIDDocumenti(String newuser,Vector v,IDbOperationSQL newdbOp) 
	{
		   inizializza(newuser,v,newdbOp);		   
	}

	protected  void inizializza(String newuser,Vector v,IDbOperationSQL newdbOp)
	{
		   	   log= new DMServer4j(EstraiIDDocumenti.class);
		   	   try {    
		   		 hms = new HashMapSet(); 
		   		 vTipiDocumento = new Vector();
		   		 user = newuser;
				 dbOp = newdbOp;
				 if(dbOp==null)
				   throw new Exception("Problemi durante l'estrazione degli idDocumenti - Connessione al DB nulla.");  	 
				 vlistID = v;
				 if(vlistID==null) {
					 throw new Exception("Problemi durante l'estrazione degli idDocumenti  - Vettore di idDocumenti nullo.");
				 }
				 else {
				 	//Viene ripulito il vettore da eventuali doppioni
					vlistID = clearVector(vlistID);
				 }
			   }
		       catch (Exception e) {
		    	   log.log_error("EstraiIDDocumenti::inizializza - Errore: "+e.getMessage());
	           }
    }

    private Vector clearVector(Vector v){
		Set<String> set = new HashSet<String>();
		set.addAll(v);
		v.clear();
		v.addAll(set);
		return v;
	}

	public HashMapSet estrai() throws Exception 
	{
		   try 
		   {    
			 log.log_info("Inizio - Costruzione HMS iniziale - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");  
			 //Costruzione HMS e vettore di id_tipo_documnto
			 try 
			 {
			   for(int i=0;i<vlistID.size();i++){

			   	   String elemento = vlistID.get(i).toString();
				   if(elemento.indexOf("@")!=-1) {
					   String[] seq = elemento.split("@");
					   hms.add(seq[0], seq[1]);
					   vTipiDocumento.add(seq[0]);
				   }
			   }
	      	 } 
			 catch (Exception e) {
				 log.log_error("Errore durante la costruzione della struttura HMS.");
	             throw e; 
	         }
			 log.log_info("Fine - Costruzione HMS iniziale - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
			 log.log_info("Inizio - Estrazione id_documenti - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
			 //Estrazione per tipo_uso
			 try { 
				 estraiIDDocumentiPerTipouso(); 
			 } 
			 catch (Exception e) {
				 log.log_error("Errore durante l'estrazione degli iddocumenti.");
	             throw e; 
	         }
			 log.log_info("Fine - Estrazione id_documenti - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	       }
	       catch (Exception e) {
	    	   log.log_error("EstraiIDDocumenti::estrai - Errore: "+e.getMessage());
	    	   throw e;
           }   
		
           return hms; 
	}
	
	public HashMapSet estrai(String tipo_oggetto,String fulltext) throws Exception 
	{
		   try 
		   {    
			 log.log_info("Inizio - Costruzione HMS iniziale - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
			 
			 sWhereFullText = fulltext;
			 
			 //Costruzione HMS e vettore di id_tipo_documnto
			 try 
			 {
			   for(int i=0;i<vlistID.size();i++){
	      		   
				   String elemento = vlistID.get(i).toString();
				   if(elemento.indexOf("@")!=-1) {
					   String[] seq = elemento.split("@");
					   hms.add(seq[0],seq[1]);
					   vTipiDocumento.add(seq[0]);
				   }
	      	   }
	      	 } 
			 catch (Exception e) {
				 log.log_error("Errore durante la costruzione della struttura HMS.");
	             throw e; 
	         }
			 
			 if(hms.size()==0)
              throw new Exception("Problemi durante l'estrazione degli id documenti. Hash Map Set vuoto.");  
			 
			 log.log_info("Fine - Costruzione HMS iniziale - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
			 log.log_info("Inizio - Estrazione id_documenti - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
			 //Estrazione dati
			 try { 
			   if(tipo_oggetto.equals("Q"))	
				 estraiIDDocumentiPerTipouso(); 
			   else 
				 estraiIDDocumentiPerCartella();  
			 } 
			 catch (Exception e) {
				 log.log_error("Errore durante l'estrazione degli iddocumenti.");
	             throw e; 
	         }
			 log.log_info("Fine - Estrazione id_documenti - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	       }
	       catch (Exception e) {
	    	   log.log_error("EstraiIDDocumenti::estrai - Errore: "+e.getMessage());
	    	   throw e;
           }   
		
           return hms; 
	}
	
	private void estraiIDDocumentiPerCartella() throws Exception
	{
			try
			{
			    String elemento = vlistID.get(0).toString();
			    String[] seq = elemento.split("@");    
			    getIDDocumentiPerCartella(seq[1],true);
			    hms.remove(seq[0]);
        	}
	        catch (Exception e) {
	        	log.log_error("EstraiIDDocumenti::estraiIDDocumentiPerCartella - Errore: "+e.getMessage());  
	            throw e;
	        } 
	}
	
	private void estraiIDDocumentiPerTipouso() throws Exception
	{
	        StringBuffer sql = new StringBuffer();
	        IDbOperationSQL dbOpSQL=null;  
	        try
	        {
	           log.log_info("Inizio - Estrazione id_documenti per TIPO_USO - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	           if(vTipiDocumento.size()==0)
	   			 throw new Exception("Problemi durante l'estrazione - Vettore di tipi_documento vuoto.");  

	           //Prima viene ripulito da ventuali doppioni
				vTipiDocumento = clearVector(vTipiDocumento);

			   sql.append("select id_tipodoc,tipo_uso from modelli where ");
			   if(vTipiDocumento.size()>1000)
				sql.append(getSequenzaBlocchi("id_tipodoc",vTipiDocumento));
			   else
				sql.append("id_tipodoc in  "+getSequenza(vTipiDocumento));

			   log.log_info("SQL senza doppioni - "+sql);

				dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
			   dbOpSQL.setStatement(sql.toString());
			   dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while ( rs.next() ) 
			   {
	             String tipo_uso=rs.getString("tipo_uso");
	             String id_tipodoc=rs.getString("id_tipodoc");
	             
	             if(tipo_uso.equals("F") || tipo_uso.equals("C") ||tipo_uso.equals("W")){
	            	 Vector vDocs =getIDDocumentiProfili(id_tipodoc);
	            	 for(int i=0;i<vDocs.size();i++){
	            		 getIDDocumentiPerCartella(vDocs.get(i).toString(),true);
	            	 }
	            	 hms.remove(id_tipodoc);
	             } 
	             else
	               if(tipo_uso.equals("R") || tipo_uso.equals("Q") || tipo_uso.equals("V") || tipo_uso.equals("N"))	 
	                 hms.remove(id_tipodoc);
			   }	
			   dbOpSQL.close();
			   log.log_info("Fine - Estrazione id_documenti per TIPO_USO - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	           
	        }
	        catch (SQLException e) {
	        	dbOpSQL.close();
	        	log.log_error("EstraiIDDocumenti::estraiIDDocumentiPerTipouso - Errore: "+e.getMessage());  
	            throw e;
	        } 
	}	
	
	private Vector getIDDocumentiProfili(String id_tipodoc){
	        Vector<String> v = new Vector<String>();
		    Iterator iIntern = hms.getHashSet(id_tipodoc);
            while (iIntern.hasNext()) {    
                 v.add(""+iIntern.next());         
            }		   
		    return v;
	}  
	
	private void getIDDocumentiPerCartella(String id_documento_profilo,boolean isRadice) throws Exception
	{
		    IDbOperationSQL dbOpSQL=null;  
		    StringBuffer sql = new StringBuffer();
		    try
	        {			  
	           log.log_info("Inizio - Estrazione id_documenti per CARTELLA - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
		           	
	           if(isRadice){	  
		           sql.append("SELECT d.id_tipodoc || '@' || d.id_documento documento, d.id_tipodoc,d.id_documento ");
				   sql.append("FROM links l, documenti d, cartelle c, view_cartella v ");
				   sql.append("WHERE c.id_documento_profilo = "+id_documento_profilo);
				   sql.append(" AND l.id_cartella = c.id_cartella");
				   sql.append(" AND c.id_cartella = v.id_cartella");
				   sql.append(" AND l.tipo_oggetto = 'D'");
				   sql.append(" AND d.id_documento = l.id_oggetto");
				   sql.append(" AND d.stato_documento NOT IN ('CA', 'RE')");
				   sql.append(" AND "+GDM_VERIFICA_COMP("DOCUMENTI","d.id_documento","L",user)+" = 1 ");
				   sql.append(" AND "+GDM_VERIFICA_COMP("VIEW_CARTELLA","v.id_viewcartella","L",user)+" = 1 ");
				   if(sWhereFullText!=null)
		    		sql.append(sWhereFullText);
	           }
	           else {
	        	   sql.append(" SELECT d.id_tipodoc || '@' || d.id_documento documento, d.id_tipodoc,d.id_documento,0 num_cartelle");
	        	   sql.append(" FROM links l, documenti d, cartelle c");
	        	   sql.append(" WHERE c.id_documento_profilo = "+id_documento_profilo);
	        	   sql.append(" AND l.id_cartella = c.id_cartella");
	        	   sql.append(" AND l.tipo_oggetto = 'D'");
	        	   sql.append(" AND d.id_documento = l.id_oggetto");
	        	   sql.append(" AND d.stato_documento NOT IN ('CA', 'RE')");
	        	   sql.append(" AND "+GDM_VERIFICA_COMP("DOCUMENTI","d.id_documento","L",user)+" = 1 ");
	        	   //if(sWhereFullText!=null)
	    			//sql.append(sWhereFullText);
	           }
	           dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
	           dbOpSQL.setStatement(sql.toString());
	           dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while ( rs.next() ) 
			   {
	             String id_documento=rs.getString("id_documento");
	             String id_tipodoc=rs.getString("id_tipodoc");
	             hms.add(id_tipodoc,id_documento);
	           }
			   
			   if(isRadice){
			    estraiFascicoli(id_documento_profilo);
			    estraiSottoCartelle(id_documento_profilo);
	           }
			   
			   dbOpSQL.close();
			   log.log_info("Fine - Estrazione id_documenti per CARTELLA - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
				  
	          }
	          catch (SQLException e) {
	        	dbOpSQL.close();
	        	log.log_error("EstraiIDDocumenti::getIDDocumentiPerCartella(id_documento_profilo,isRadice):("+id_documento_profilo+","+isRadice+") - SQL: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	          } 
	 }
		
	 private void estraiSottoCartelle(String id_documento_profilo) throws Exception
	 {
		     IDbOperationSQL dbOpSQL=null;  
		     StringBuffer sql = new StringBuffer();
	         try
	         {	
	           log.log_info("Inizio - Estrazione sotto_cartelle - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	   		   sql.append("SELECT id_cartella cartellapadre,id_oggetto,id_tipodoc,id_documento_profilo,id_tipodoc||'@'||id_documento_profilo sequenza ");
			   sql.append(" FROM (SELECT haschildren, parentid, parentid idcartprov, nodeid,UPPER (nome) nome, text, id_cartella, tipo_oggetto, id_oggetto, ");
			   sql.append(" id_oggetto_comp, idcart, id_tipodoc, id_documento_profilo");
			   sql.append(" FROM (SELECT tree.haschildren haschildren, parentid,parentid idcartprov, nodeid, c.nome nome, text,");
			   sql.append(" 	         tree.id_cartella, tipo_oggetto, id_oggetto,vw.id_viewcartella id_oggetto_comp,");
			   sql.append(" 			 vw.id_viewcartella idcart,td.id_tipodoc id_tipodoc, c.id_documento_profilo");
			   sql.append("       FROM (SELECT ROWNUM ordine, parentid, nodeid, text,id_cartella, id_documento_profilo,tipo_oggetto, id_oggetto,haschildren");
			   sql.append("             FROM (SELECT parentid parentid, nodeid, text,cart_foglie.id_cartella, 'C' tipo_oggetto,id_oggetto,cc.id_documento_profilo,");
			   sql.append(" 				  NVL((SELECT distinct 1 c FROM links,documenti,cartelle,modelli");
			   sql.append("                        WHERE links.id_cartella = cart_foglie.id_oggetto");
			   sql.append(" 			           AND links.tipo_oggetto = 'C' AND cartelle.id_cartella =links.id_oggetto");
			   sql.append(" 			           AND cartelle.id_documento_profilo =documenti.id_documento");
			   sql.append("                        AND documenti.id_tipodoc =modelli.id_tipodoc");
			   sql.append("                        AND modelli.tipo_uso IN ('Q', 'C', 'W')");
			   sql.append("                        AND NVL (stato_documento,'BO') NOT IN ('CA', 'RE')");
			   sql.append("                   ),0) haschildren");
			   sql.append(" 	             FROM cart_foglie, cartelle cc");
			   sql.append("                  where cc.id_cartella = cart_foglie.id_cartella)");
			   sql.append(" 			CONNECT BY PRIOR nodeid = parentid");
			   sql.append("             START WITH id_documento_profilo = "+id_documento_profilo);
			   sql.append("             ORDER BY ordine) tree,");
			   sql.append("        cartelle c,cartelle c2,tipi_documento td,documenti d,view_cartella vw");
			   sql.append(" WHERE tree.id_oggetto = c.id_cartella(+)");
			   sql.append(" AND c.id_cartella = vw.id_cartella(+)");
			   sql.append(" AND d.id_tipodoc = td.id_tipodoc");
			   sql.append(" AND c2.id_documento_profilo = "+id_documento_profilo);
			   sql.append(" AND d.id_documento = c.id_documento_profilo");
			   sql.append(" AND NVL (c.stato, 'BO') <> 'CA'");
			   //if(sWhereFullText!=null)
	    		//sql.append(sWhereFullText);
			   sql.append(" ) UNION ALL");
			   sql.append(" SELECT TO_NUMBER (NULL), TO_CHAR (NULL), TO_CHAR (NULL),TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),TO_NUMBER (NULL),");
			   sql.append("        TO_CHAR (NULL), TO_NUMBER (NULL),TO_NUMBER (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL),TO_NUMBER (NULL)");
			   sql.append(" FROM DUAL),");
			   sql.append(" dual WHERE "+GDM_VERIFICA_COMP("VIEW_CARTELLA","idcart","L",user)+" = 1 ");
			   dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
			   dbOpSQL.setStatement(sql.toString());
			   dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while ( rs.next() ) 
			   {
	             String id_profilo=rs.getString("id_documento_profilo");
	             getIDDocumentiPerCartella(id_profilo,false);
	             estraiFascicoli(id_profilo);
			   }
	   
			   dbOpSQL.close();
			   log.log_info("Fine - Estrazione sotto_cartelle - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          }
	          catch (SQLException e) {
	        	 dbOpSQL.close();
	        	 log.log_error("EstraiIDDocumenti::estraiSottoCartelle(id_documento_profilo):("+id_documento_profilo+") - SQL: "+sql+"- Errore: "+e.getMessage());
		         throw e;
	          } 
	   }
	 
	 private void estraiFascicoli(String id_documento_profilo) throws Exception
	 {
		     IDbOperationSQL dbOpSQL=null;  
		     StringBuffer sql = new StringBuffer();
	         try
	         {	
	           log.log_info("Inizio - Estrazione sotto_cartelle - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	   		   sql.append("SELECT id_cartella cartellapadre,id_oggetto,id_tipodoc,id_documento_profilo,id_tipodoc||'@'||id_documento_profilo sequenza ");
			   sql.append(" FROM (SELECT haschildren, parentid, parentid idcartprov, nodeid,UPPER (nome) nome, text, id_cartella, tipo_oggetto, id_oggetto, ");
			   sql.append(" id_oggetto_comp, idcart, id_tipodoc, id_documento_profilo");
			   sql.append(" FROM (SELECT tree.haschildren haschildren, parentid,parentid idcartprov, nodeid, c.nome nome, text,");
			   sql.append(" 	         tree.id_cartella, tipo_oggetto, id_oggetto,vw.id_viewcartella id_oggetto_comp,");
			   sql.append(" 			 vw.id_viewcartella idcart,td.id_tipodoc id_tipodoc, c.id_documento_profilo");
			   sql.append("       FROM (SELECT ROWNUM ordine, parentid, nodeid, text,id_cartella, id_documento_profilo,tipo_oggetto, id_oggetto,haschildren");
			   sql.append("             FROM (SELECT parentid parentid, nodeid, text,cart_fascicoli.id_cartella, 'C' tipo_oggetto,id_oggetto,cc.id_documento_profilo,");
			   sql.append(" 				  NVL((SELECT 1 c FROM links,documenti,cartelle,modelli");
			   sql.append("                        WHERE links.id_cartella = cart_fascicoli.id_oggetto");
			   sql.append(" 			           AND links.tipo_oggetto = 'C' AND cartelle.id_cartella =links.id_oggetto");
			   sql.append(" 			           AND cartelle.id_documento_profilo =documenti.id_documento");
			   sql.append("                        AND documenti.id_tipodoc =modelli.id_tipodoc");
			   sql.append("                        AND modelli.tipo_uso IN ('Q', 'C', 'W')");
			   sql.append("                        AND NVL (stato_documento,'BO') NOT IN ('CA', 'RE')");
			   sql.append("                   ),0) haschildren");
			   sql.append(" 	             FROM ( ");
			   sql.append("						SELECT 'C' || l1.id_cartella parentid, tipo_oggetto || id_oggetto nodeid,c.nome text, id_oggetto, l1.id_cartella");
			   sql.append("						FROM links l1, cartelle c, documenti d, modelli m");
			   sql.append("						WHERE tipo_oggetto = 'C' AND c.id_cartella = l1.id_oggetto");
			   sql.append("						AND c.id_documento_profilo = d.id_documento AND d.id_tipodoc = m.id_tipodoc");
			   sql.append("						AND m.tipo_uso = 'F'");
			   sql.append(" 				 ) cart_fascicoli, cartelle cc");
			   //sql.append("                  where cc.id_cartella = cart_fascicoli.id_cartella)");
			   sql.append("                  where cc.id_cartella = cart_fascicoli.id_oggetto)");
			   sql.append(" 			CONNECT BY PRIOR nodeid = parentid");
			   sql.append("             START WITH id_documento_profilo = "+id_documento_profilo);
			   sql.append("             ORDER BY ordine) tree,");
			   sql.append("        cartelle c,cartelle c2,tipi_documento td,documenti d,view_cartella vw");
			   sql.append(" WHERE tree.id_oggetto = c.id_cartella(+)");
			   sql.append(" AND c.id_cartella = vw.id_cartella(+)");
			   sql.append(" AND d.id_tipodoc = td.id_tipodoc");
			   sql.append(" AND c2.id_documento_profilo = "+id_documento_profilo);
			   sql.append(" AND d.id_documento = c.id_documento_profilo");
			   sql.append(" AND NVL (c.stato, 'BO') <> 'CA'");
			   //if(sWhereFullText!=null)
	    		//sql.append(sWhereFullText);
			   sql.append(" ) UNION ALL");
			   sql.append(" SELECT TO_NUMBER (NULL), TO_CHAR (NULL), TO_CHAR (NULL),TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),TO_NUMBER (NULL),");
			   sql.append("        TO_CHAR (NULL), TO_NUMBER (NULL),TO_NUMBER (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL),TO_NUMBER (NULL)");
			   sql.append(" FROM DUAL),");
			   sql.append(" dual WHERE "+GDM_VERIFICA_COMP("VIEW_CARTELLA","idcart","L",user)+" = 1 ");
			   dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
			   dbOpSQL.setStatement(sql.toString());
			   dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while ( rs.next() ) 
			   {
	             String id_profilo=rs.getString("id_documento_profilo");
	             getIDDocumentiPerCartella(id_profilo,false);
			   }
	   
			   dbOpSQL.close();
			   log.log_info("Fine - Estrazione sotto_cartelle - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          }
	          catch (SQLException e) {
	        	 dbOpSQL.close();
	        	 log.log_error("EstraiIDDocumenti::estraiSottoCartelle(id_documento_profilo):("+id_documento_profilo+") - SQL: "+sql+"- Errore: "+e.getMessage());
		         throw e;
	          } 
	   }
	
	   private String GDM_VERIFICA_COMP(String oggetto,String idOggetto,String azione,String utente)
	   {     
	           StringBuffer decode = new StringBuffer("");
	           decode.append("GDM_COMPETENZA.GDM_VERIFICA('"+oggetto+"',"+idOggetto+", '"+azione+"', '");
	           decode.append(utente+"',  F_TRASLA_RUOLO('"+utente+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))");
	           return decode.toString();            
	   }	
	
	   private String getSequenza(Vector v)
	   {
			    String seq="( ";
			        
			    for(int i=0;i<v.size();i++)
			    {
			      if(i==(v.size()-1))
			    	seq+=v.get(i);
			      else
			        seq+=v.get(i)+ ","; 
			    }
			       
			    if (v.size()==0) seq+="0";
			       
			    seq+=" )";
			    return seq;
	   }
		  
	   private String getSequenzaBlocchi(String condizione,Vector v)
	   {
			    String seq="";
			    int count=0,max_list=1000,v_size,s;
			       
		        if (v.size()==0)
		     	 return seq;
		    
		        v_size=v.size(); 
		        s=0;
		        seq=" (  "+condizione+" IN ( ";  
		        for(int i=0;i<v.size();i++)
		        {
		    	 if (count == max_list){
		              count=0;
		              s++;
		              v_size=v.size()-s*max_list;
		              seq+=" or "+condizione+" IN ( ";
		         }
		    	 seq+=v.get(i);
		    	 if ( v.size() <= max_list)
		          	seq+=(i!=v.size()-1)?(" , "):(")");
		         else 
		         {
		            if(count==v_size-1)
		              seq+=" ) ";
		            else
		             if (count==(max_list-1))
		              seq+=" ) ";
		             else 
		              seq+=" , ";  
		         }
		    	 count++;
		        }
		       seq+=" )";
		       return seq;
		}
}

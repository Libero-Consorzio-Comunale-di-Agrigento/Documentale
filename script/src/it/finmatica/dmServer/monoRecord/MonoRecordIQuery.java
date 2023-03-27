package it.finmatica.dmServer.monoRecord;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import it.finmatica.modutils.multirecord.*;
import it.finmatica.dmServer.controlli.*;
import it.finmatica.dmServer.jdms.CCS_Common;
import it.finmatica.dmServer.jdms.CCS_WorkArea;
import it.finmatica.dmServer.jdms.DMServer4j;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;


public class MonoRecordIQuery
{ 
	   private IDbOperationSQL dbOp;
	   private String docNumber;
	   private Environment en;
	   private String sCorpoBlocco;
	   private String sNomeBlocco;
	   private String tipoOggetto;
	   private String nomeOggetto;	 
	   private String cr;
	   private String area;
	   private String cm; 
	   private String sJoin;
	   private HttpServletRequest req;
       private boolean bIsNew=false;
       private Vector docNumbers;
       private Vector vListaID;
       private Vector vMonoRecord;
       private Vector vHTMLDOCS;
       private String seqID;
       private Document xml;
       private String sXML="";
       private String ente="";
       private String utente;
       private Personalizzazioni pers = null;
       CCS_Common CCS_common; 
       
       /**
	  	  * Variabile gestione logging
	   */
       private DMServer4j log;
       
      
       /**
	   	 * Costruttori.
	   */
	   public MonoRecordIQuery(String newDocNumber,String newtipoOggetto,String newnome,Environment newEn,HttpServletRequest newReq) throws Exception {
		      
		      docNumber = newDocNumber;
		      en = newEn;
  	          tipoOggetto=newtipoOggetto;
	          nomeOggetto=newnome;		      
		      req=newReq;
		      
		      init();
	          
		      String  query;
                  
		      query="SELECT D.AREA, M.CODICE_MODELLO,D.CODICE_RICHIESTA, B.CORPO,B.BLOCCO,NVL(DBMS_LOB.SUBSTR(B.ISTRUZIONE,1000),'') AS ISTRUZIONE ";
		      query+="FROM DOCUMENTI D,";
			  query+="TIPI_DOCUMENTO TD,";
		      query+="MODELLI M,";
		      query+="BLOCCHI B ";
		      query+="WHERE D.ID_DOCUMENTO = :P_DOCNUM AND ";
		      query+="D.ID_TIPODOC = TD.ID_TIPODOC AND ";
		      query+="D.ID_TIPODOC = M.ID_TIPODOC AND ";
		      query+="D.AREA = M.AREA AND ";
		      query+="M.BLOCCO_JDMS=B.BLOCCO ";
		      query+=" AND m.area=b.area";
	     
		      try {
                dbOp = connect();

                dbOp.setStatement(query);
                dbOp.setParameter(":P_DOCNUM",docNumber);
                dbOp.execute();     
                
                ResultSet rst = dbOp.getRstSet();

                if (rst.next()) {   
	            	
                	try {
	                  sCorpoBlocco=Global.leggiClob(dbOp,"CORPO");
	                }
	                catch (Exception e) {                                
	                  throw new Exception("MonoRecordIQuery::costructor() Errore in lettura Corpo del Blocco");
	                }
	                
	                //Il blocco non può essere vuoto
                    if (sCorpoBlocco==null) {                 
                        throw new Exception("MonoRecordIQuery::costructor() Corpo del Blocco non trovato");
                    }
                    
                    sNomeBlocco=rst.getString("BLOCCO");
                    
                    area=rst.getString("AREA");                   
                    cm=rst.getString("CODICE_MODELLO");
                    cr=rst.getString("CODICE_RICHIESTA");
                    sJoin=rst.getString("ISTRUZIONE");
                    
                    if (sJoin==null) sJoin="";
                }
                else {
                	sCorpoBlocco="";
                }
                
                close();
               
		      }
		      catch (Exception e) {
		    	 
                try {close();} catch (Exception ei) {}
                throw new Exception("MonoRecordIQuery::costructor() "+e.getMessage());
              }
	   }
	   
	   /** Utilizzato dalla workarea */
	   public MonoRecordIQuery(Vector newDocNumbers,Environment newEn,HttpServletRequest newReq,String newutente) throws Exception 
	   {
		      docNumbers = newDocNumbers;
		      en = newEn;		     
		      req=newReq;
	          utente = newutente; 
	          vMonoRecord=new Vector();
	          vHTMLDOCS=new Vector();
	          init();
	          
	          try
	          {
	           dbOp = connect();
	           pers = (Personalizzazioni)req.getSession().getAttribute("_personalizzazioni_gdm");
	           if (pers == null) {
	             try {
	               pers = new Personalizzazioni(ente, utente,dbOp.getConn());
	             } catch (Exception e) {
	               pers = null;
	             }
	             req.getSession().setAttribute("_personalizzazioni_gdm",pers);
	           }      
	           close();
	          }
		      catch (Exception e) {
		        try {close();} catch (Exception ei) {}
                throw new Exception("MonoRecordIQuery::costructor() "+e.getMessage());
              }
	   }
	   
	   private void init(){
		   	   log= new DMServer4j(MonoRecordIQuery.class);
	   }
	   
	   /**
	    * Costruisce la sequanza di ID da inserire nella lista della select.
	    * 
	    * @param v   		vettore di id
	    * @return String 	sequenza (id1,id2,id3,......)  
	    * 
      */	   
	   private String getSequenza(Vector v)
	   {
	 	       String seq="( ";
	 	        
	 	       for(int i=0;i<v.size();i++)
	 	       {
	 	    	 seq+="SELECT "+v.get(i)+ " FROM DUAL ";   
	 	    	 if(i!=(v.size()-1))
	 		      seq+=" UNION "; 
	 	       }
	 	       
	 	       if (v.size()==0) seq+="0";
	 	       
	 	       seq+=" )";
	 	       return seq;
	   }

	   /**
	    * Costruisce la sequanza di ID da inserire nella lista della select.
	    * 
	    * @param v   		vettore di id
	    * @return String 	sequenza (id1,id2,id3,......)  
	    * 
      */	   
	   private String getSequenzaBlocchi(Vector v)
	   {
		       String seq="";
		       int count=0,max_list=1000,v_size,s;
		       
		       if (v.size()==0)
		    	return seq;
		    
		       v_size=v.size(); 
		       s=0;
		       
		       seq=" (  d.id_documento IN ( ";  
		       		        
		       for(int i=0;i<v.size();i++)
		       {
		    	 if (count == max_list)
		         {
		              count=0;
		              s++;
		              v_size=v.size()-s*max_list;
		              seq+=" or d.id_documento IN ( ";
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
	   
	   
	   /**
	    * Costruisce la sequanza di ID da inserire nella lista della select.
	    * 
	    * @param area  		area del blocco
	    * @param blocco		nome del blocco
	    * 
	    * @return String 	corpo del relativo (area,blocco)
	    * 
       */	
	   private String getCorpo(String area, String blocco) throws Exception
	   { 
		      String  query, corpo="";
		      IDbOperationSQL dbOpSQL=null;
              query="SELECT corpo ";
		      query+="FROM BLOCCHI ";
		      query+="WHERE BLOCCO= '"+blocco+"'";
		      query+=" AND AREA = '"+area+"'";
		      query+=" AND PERSONALIZZAZIONE = 'N'";
	     
		      try
		      {
		    	  dbOpSQL = connect();
		    	  dbOpSQL.setStatement(query);
		    	  dbOpSQL.execute();     
	             
	             ResultSet rst = dbOpSQL.getRstSet();
	
	             if(rst.next())
	             {   
	            	 try {
		                  corpo=Global.leggiClob(dbOpSQL,"CORPO");
		             }
		             catch (Exception e) {                                
		              throw new Exception("MonoRecordIQuery::costructor() Errore in lettura Corpo del Blocco");
		             }
	             }
	            
	             dbOpSQL.close();
              }
		      catch (Exception e)
		      {
		    	try {dbOpSQL.close();} catch (Exception ei) {}
                throw new Exception("MonoRecordIQuery::getCorpo() "+e.getMessage());
              }
		      return corpo;
	   }
	   
	   /**
	    * Costruisce la sequanza di ID da inserire nella lista della select.
	    * 
	    * @param area  		area del blocco
	    * @param blocco		nome del blocco
	    * 
	    * @return String 	corpo del relativo (area,blocco)
	    * 
       */	
	   private String getCorpoPers(String area, String blocco) throws Exception
	   { 
		      String  query, corpo="";
		      IDbOperationSQL dbOpSQL=null;
              query="SELECT corpo ";
		      query+="FROM BLOCCHI ";
		      query+="WHERE BLOCCO= '"+blocco+"'";
		      query+=" AND AREA = '"+area+"'";
		     
	     
		      try
		      {
		    	  dbOpSQL = connect();
		    	  dbOpSQL.setStatement(query);
		    	  dbOpSQL.execute();     
	             
	             ResultSet rst = dbOpSQL.getRstSet();
	
	             if(rst.next())
	             {   
	            	 try {
		                  corpo=Global.leggiClob(dbOpSQL,"CORPO");
		             }
		             catch (Exception e) {                                
		              throw new Exception("MonoRecordIQuery::costructor() Errore in lettura Corpo del Blocco");
		             }
	             }
	            
	             dbOpSQL.close();
              }
		      catch (Exception e)
		      {
		    	try {dbOpSQL.close();} catch (Exception ei) {}
                throw new Exception("MonoRecordIQuery::getCorpo() "+e.getMessage());
              }
		      return corpo;
	   }
	   
	   /**
	    * Costruisce la struttra hastTable 
	    * HASH_TABLE(area_blocco,nome_blocco,corpo,vettoreListaDocs,area_modello,codice_modello).
	    * 
       */	
	   private void costruisciMonoRecords(boolean stampa) throws Exception
	   { 
		      String  query;
		      boolean exitsPers=false; 
		      
		      log.log_info("[INFO - Inizio Costruzione Struttura - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
		      /*
		      if(docNumbers.size()>1000)
		    	seqID=getSequenzaBlocchi(docNumbers);
		      else*/
		        seqID=" D.ID_DOCUMENTO IN  "+getSequenza(docNumbers);
              
              query="SELECT B.BLOCCO, B.area AREABLOCCO, d.id_documento, m.area, m.codice_modello CM ";
		      query+="FROM DOCUMENTI D,";
			  query+="TIPI_DOCUMENTO TD,";
		      query+="MODELLI M,";
		      query+="BLOCCHI B ";
		      query+="WHERE "+seqID+" AND ";
		      query+="D.ID_TIPODOC = TD.ID_TIPODOC AND ";
		      query+="D.ID_TIPODOC = M.ID_TIPODOC AND ";
		      query+="D.AREA = M.AREA AND ";
		      if(stampa)
		       query+="decode(m.blocco_stampa_jdms,null,m.BLOCCO_JDMS,m.blocco_stampa_jdms)=B.BLOCCO AND ";
		      else
               query+="M.BLOCCO_JDMS=B.BLOCCO AND ";
	 		  query+="M.area=B.area ";
		      query+="order by 1,2 ";
		      
		      log.log_info("Esecuzione SQL - "+query);
		      	     
		      try
		      {
	             dbOp = connect();
	             dbOp.setStatement(query);
	             dbOp.execute();     
	             ResultSet rst = dbOp.getRstSet();
	             String area="",nomeblocco="",areaOLD="",nomebloccoOLD="",corpo="",area_modello="",codice_modello="",area_modelloOLD="",codice_modelloOLD="";
	             vListaID =null;
	             
	             while (rst.next())
	             {   
            		nomeblocco=rst.getString("BLOCCO");
	            	area=rst.getString("AREABLOCCO");
	            	area_modello=rst.getString("AREA");
	            	codice_modello=rst.getString("CM");
	            	
	            	/** Controllo esistenza delle personalizzazione per il relativo blocco */
	                pers = (Personalizzazioni)req.getSession().getAttribute("_personalizzazioni_gdm");
	                if (pers != null)
	                {
	                	exitsPers = pers.existPersonalizzazione(Personalizzazioni.BLOCCHI, area+"#"+nomeblocco);
		                   
	                	if(exitsPers)
	                	{
	                		String persBlocco = pers.getPersonalizzazione(Personalizzazioni.BLOCCHI, area+"#"+nomeblocco);
		                    int j = persBlocco.indexOf("#");
		                    area = persBlocco.substring(0,j);
		                    nomeblocco = persBlocco.substring(j+1);
		                }
	                }
	            		
	            	if((!nomeblocco.equals(nomebloccoOLD)) || (!area.equals(areaOLD)))
	            	{
		            	if(vListaID!=null)
		            	{
		            		Properties p=new Properties();  
		            		p.put("AREA_BLOCCO",areaOLD);
		            		p.put("BLOCCO",nomebloccoOLD);
		            		p.put("CORPO",corpo);
		            		p.put("LISTADOC",vListaID);
		            		p.put("AREA",area_modelloOLD);
		            		p.put("CM",codice_modelloOLD);
		            		vMonoRecord.add(p);
		                }
	            		nomebloccoOLD=nomeblocco;
		            	areaOLD=area;
		            	area_modelloOLD=area_modello;
		            	codice_modelloOLD=codice_modello;
		            	if(exitsPers)
		            	  corpo=getCorpoPers(area,nomeblocco); 
		            	else	
		            	  corpo=getCorpo(area,nomeblocco);
	                	 	
		            	vListaID=new Vector();
		             } 
	            	 
	            	 vListaID.add(rst.getString("ID_DOCUMENTO"));
	            	 
	             }
	             
	             if(vListaID!=null && (vListaID.size()!=0))
	             {
            		Properties p=new Properties();  
            		p.put("AREA_BLOCCO",area);
            		p.put("BLOCCO",nomeblocco);
            		p.put("CORPO",corpo);
            		p.put("LISTADOC",vListaID);
            		p.put("AREA",area_modello);
            		p.put("CM",codice_modello);
            		vMonoRecord.add(p);
	             }
	             
	             close();
	             log.log_info("[INFO - Fine Costruzione Struttura - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
	  		   
              }
		      catch (Exception e)
		      {
		    	try {close();} catch (Exception ei) {}
		    	throw e;//throw new Exception("MonoRecordIQuery::costruisciMonoRecords() "+e.getMessage());
              }
	   }
	   
	   private void costruisciMonoRecord(boolean stampa) throws Exception
	   { 
		      String  query;
		      
		      //System.out.println("[INFO - Inizio Costruzione Struttura - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
		      /*
		      if(docNumbers.size()>1000)
		    	seqID=getSequenzaBlocchi(docNumbers);
		      else*/
		        seqID=" D.ID_DOCUMENTO IN  "+getSequenza(docNumbers);
              
              query="SELECT B.BLOCCO, B.area AREABLOCCO, d.id_documento, m.area, m.codice_modello CM ";
		      query+="FROM DOCUMENTI D,";
			  query+="TIPI_DOCUMENTO TD,";
		      query+="MODELLI M,";
		      query+="BLOCCHI B ";
		      query+="WHERE "+seqID+" AND ";
		      query+="D.ID_TIPODOC = TD.ID_TIPODOC AND ";
		      query+="D.ID_TIPODOC = M.ID_TIPODOC AND ";
		      query+="D.AREA = M.AREA AND ";
		      if(stampa)
		       query+="decode(m.blocco_stampa_jdms,null,m.BLOCCO_JDMS,m.blocco_stampa_jdms)=B.BLOCCO AND ";
		      else
               query+="M.BLOCCO_JDMS=B.BLOCCO AND ";
	 		  query+="M.area=B.area ";
		      query+="order by 1,2 ";
	     
		      try
		      {
	             dbOp = connect();
	             dbOp.setStatement(query);
	             dbOp.execute();     
	             ResultSet rst = dbOp.getRstSet();
	             String area="",nomeblocco="",areaOLD="",nomebloccoOLD="",corpo="",area_modello="",codice_modello="",area_modelloOLD="",codice_modelloOLD="";
	             vListaID =null;
	             
	             while (rst.next())
	             {   
            		nomeblocco=rst.getString("BLOCCO");
	            	area=rst.getString("AREABLOCCO");
	            	area_modello=rst.getString("AREA");
	            	codice_modello=rst.getString("CM");
	            	
	            	 if((!nomeblocco.equals(nomebloccoOLD)) || (!area.equals(areaOLD)))
	            	 {
		            	if(vListaID!=null)
		            	{
		            		Properties p=new Properties();  
		            		p.put("AREA_BLOCCO",areaOLD);
		            		p.put("BLOCCO",nomebloccoOLD);
		            		p.put("CORPO",corpo);
		            		p.put("LISTADOC",vListaID);
		            		p.put("AREA",area_modelloOLD);
		            		p.put("CM",codice_modelloOLD);
		            		vMonoRecord.add(p);
		                }
	            		nomebloccoOLD=nomeblocco;
		            	areaOLD=area;
		            	area_modelloOLD=area_modello;
		            	codice_modelloOLD=codice_modello;
		            	corpo=getCorpo(area,nomeblocco);
	                	 	
		            	vListaID=new Vector();
		             } 
	            	 
	            	 vListaID.add(rst.getString("ID_DOCUMENTO"));
	            	 
	             }
	             
	             if(vListaID!=null && (vListaID.size()!=0))
	             {
            		Properties p=new Properties();  
            		p.put("AREA_BLOCCO",area);
            		p.put("BLOCCO",nomeblocco);
            		p.put("CORPO",corpo);
            		p.put("LISTADOC",vListaID);
            		p.put("AREA",area_modello);
            		p.put("CM",codice_modello);
            		vMonoRecord.add(p);
	             }
	             
	             close();
	             //System.out.println("[INFO - Fine Costruzione Struttura - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
	  		   
              }
		      catch (Exception e)
		      {
		    	try {close();} catch (Exception ei) {}
		    	throw e;//throw new Exception("MonoRecordIQuery::costruisciMonoRecords() "+e.getMessage());
              }
	   }
	   
	   
	   
	   /**
	    * Costruisce XML della MonoRecord della lista di id
       */	
	   private void costruisciMonoRiga(boolean stampa) throws Exception 
	   {
		      String ret;
		      //String sXML="";
		      MonoParser monoPar= new MonoParser();
		      
		      log.log_info("[INFO - Inizio Costruzione Struttura XML - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
			    
		      try 
		      {
		    	  dbOp = connect();
		       	  
		          if(vMonoRecord.size()!=0)
			      {
			    	 for(int i=0;i<vMonoRecord.size();i++)
			    	 {
			    		 Properties p=(Properties) vMonoRecord.elementAt(i);
			    		 Vector<String> vDoc=(Vector<String>)p.get("LISTADOC");
			    		 
			    		 Multirecord mr = new  Multirecord(req,p.get("BLOCCO").toString(),p.get("CORPO").toString(),"",1,monoPar);
			    	 	 mr.setProteggi(true);
			             mr.setThemesPath("..");
			             mr.setDomini(true);  	           
			             mr.setAggiungi("");
			           	 mr.setJoin(sJoin);
			           	 mr.setMonoRecord(true);
			           	 mr.setIdDocs(vDoc);
			           	 //String s=mr.creaHtmlMonoRecord(dbOp,req,monoPar,false,p.get("AREA").toString(),p.get("CM").toString());
			           	 
			           	 String s;
			           	 if(stampa)
			           	  s=mr.creaHtmlMonoRecord(dbOp,req,monoPar,false,p.get("AREA").toString(),p.get("CM").toString(),true);
			           	 else
			           	  s=mr.creaHtmlMonoRecord(dbOp,req,monoPar,false,p.get("AREA").toString(),p.get("CM").toString());
			           	 		 
			           	 s=s.substring(s.indexOf("<"+p.get("BLOCCO").toString()),s.length());
			           	 
			           	 sXML+=s;
				 	}
			      }
		          		         
		          sXML="<ROOT>"+encode(sXML,255)+"</ROOT>";
		          
		          log.log_info("Struttura XML - "+sXML);
		          
		          xml= DocumentHelper.parseText(sXML);
		         
		          close();
			     
		          log.log_info("[INFO - Fine Costruzione Struttura XML - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: XML - "+xml.asXML());
					 
	          }
	          catch (Exception e) {
	        	e.printStackTrace();
	        	try {close();} catch (Exception ei) {}
	        	throw e; 
	          }
	   }
	   
	   /** Lettura elemento XML */
	   private static String leggiValoreXML(Document xmlDocument, String tagName)
	   {
	           String valore = null;
	           if(xmlDocument == null)
	               System.out.println("xml document null");
	           Element root = xmlDocument.getRootElement();
	           for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
	           {
	               Element elemento = (Element)iterator.next();
	               if(elemento != null && elemento.getName().equals(tagName))
	                   valore = elemento.getText();
	               else
	                   valore = leggiValoreXML(elemento, tagName);
	           }
	           
	           return valore;
	   }
   
	   /** Lettura elemento XML */
	   private static String leggiValoreXML(Element e, String tagName)
	   {
	           String valore = null;
	           for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
	           {
	               Element elemento = (Element)iterator.next();
	               if(elemento != null && elemento.getName().equals(tagName))
	                 valore = elemento.getText();
                    else
	                   valore = leggiValoreXML(elemento, tagName);
	           }
	   
	           return valore;
	   }
	   
	   /**
	    * Costruisce il vettore di HTML delle MonoRecords
	    * 
	    * @return Vector vettore di stringhe contenenti HTML
	    * 
       */	
	   public Properties creaRighe(boolean stampa) throws Exception
	   {
		      Properties pHTML=null;
		      try
	          {
	        	  if(this.docNumbers.size()!=0)
	        	  {   
	        	  //System.out.println("[INFO - Inizio Costruzione RIGHE - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
	        	  log.log_info("Dimensione vettore Monorecords - "+this.docNumbers.size());	 
	       			
	        		  costruisciMonoRecords(stampa);
		        	  costruisciMonoRiga(stampa);
		        	  pHTML=new Properties();
		        	  
		        	  for(int i=0;i<docNumbers.size();i++)
			          { 
			           	  String htmlDOC= leggiValoreXML(xml,"ID"+docNumbers.get(i));
			           	  
			           	  if(htmlDOC==null) 
			           	  {	  
			           		log.log_info("Il MonoRecord associato al Documento id="+docNumbers.get(i)+" non trovato.");  
			           		pHTML.put("ID"+docNumbers.get(i),"");
			           		log.log_error("MonoRecordIQuery::creaRighe -- Il MonoRecord associato al Documento id="+docNumbers.get(i)+" non trovato.");			            	
			           	  } 	
			           	  else {
			           		log.log_info("Il MonoRecord associato al Documento id="+docNumbers.get(i)+"-- "+htmlDOC);  
			           		pHTML.put("ID"+docNumbers.get(i),htmlDOC);
			           	  }	
			          }
		           //System.out.println("[INFO - Fine Costruzione RIGHE - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
			     		
	        	  }
	          }
	          catch (Exception e) {
	        	  e.printStackTrace();
	        	  throw e;//throw new Exception("MonoRecordIQuery::creaRighe() "+e.getMessage());	 
	          }
	         
	         return pHTML;
	   }
	   
	   public Vector creaRigheFromStampa(boolean stampa) throws Exception
	   {
		      Vector sHTML=null;
		      try
	          {
	        	  if(this.docNumbers.size()!=0)
	        	  {   
	        	  //System.out.println("[INFO - Inizio Costruzione RIGHE - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
	     			 
	       			
	        		  costruisciMonoRecords(stampa);
		        	  costruisciMonoRiga(stampa);
		        	  sHTML=new Vector();
		        	  
		        	  for(int i=0;i<docNumbers.size();i++)
			          { 
			           	  String htmlDOC= leggiValoreXML(xml,"ID"+docNumbers.get(i));
			           	  
			           	  if(htmlDOC==null) 
			           	  {	  
			           		sHTML.add(i,"");
			           		log.log_error("MonoRecordIQuery::creaRighe -- Il MonoRecord associato al Documento id="+docNumbers.get(i)+" non trovato.");			            	
			           	  } 	
			           	  else
			           	    sHTML.add(i,htmlDOC);
			          }
		           //System.out.println("[INFO - Fine Costruzione RIGHE - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: ");
			     		
	        	  }
	          }
	          catch (Exception e) {
	        	  throw e;//throw new Exception("MonoRecordIQuery::creaRighe() "+e.getMessage());	 
	          }
	         
	         return sHTML;
	   }
	   
	   public String creaRiga() throws Exception {
		     
		      String ret;
		   
	          //Se non esiste il blocco restituisco
		      //scritta standard (o nome dell'oggetto
		      //oppure dicitura per documento)
	          if (sCorpoBlocco.equals("")) {
	         	if(tipoOggetto.equals("D"))
	         	  return "Documento n. "+docNumber;
	         	else
	              return nomeOggetto;		 
	          }
	         
	          try {
	        	ACKParser ackPar= new ACKParser(docNumber,en);  
	        	  
	            Multirecord mr = new  Multirecord(req,sNomeBlocco, sCorpoBlocco, 
	        	                                "CR="+cr,1,ackPar);
	           
	            mr.setProteggi(true);
	            mr.setThemesPath("..");
	            mr.setDomini(true);  	           
	           	mr.setAggiungi("");
	           	mr.setJoin(sJoin);
	           	mr.setMonoRecord(true);
	           	mr.setIdDoc(docNumber);
	           	 
	            dbOp = connect();
	            
	            ret = mr.creaHtmlIQuery(dbOp,req,ackPar,false,area,cm);
	            
	            close();
	            
	          }
	          catch (Exception e) {
	        	 
	        	try {close();} catch (Exception ei) {}
	            throw new Exception("MonoRecordIQuery::creaRiga() "+e.getMessage());	 
	          }
	         
	          return ret;
	   }
	   
  
	   private IDbOperationSQL connect() throws Exception {
	        if (en.getDbOp()==null) {
	           bIsNew=true;
	           return (new ManageConnection(en.Global)).connectToDB();
	        }
	        
	        return en.getDbOp();
	   }
  
       private void close() throws Exception {
            if (bIsNew) 
            	(new ManageConnection(en.Global)).disconnectFromDB(dbOp,true,false);      
       } 
       
       private static String encode (String string, int inizio) throws  Exception {
		   	    if (string==null) return "";
		   	   
		   	  	String fine = "0000";
		   	  	String valore = Integer.toHexString(inizio);
		   	  	fine = fine.substring(0, (fine.length()-valore.length())) + valore;
		   	    String regex = "[^\u0000-\\u"+fine+"]";
		   			Pattern pattern = Pattern.compile(regex);
		   			Matcher matcher = pattern.matcher(string);
		   			char c;
		   	    int startpos = 0;
		   	    int endpos = 0;
		   	    StringBuffer sb = null;
		   			while (matcher.find()) {
		   				endpos = matcher.start();
		   				if (endpos >= startpos) {
		   					c = string.charAt(endpos);
		   					
		   		      if( sb == null ) {
		   		        sb = new StringBuffer( endpos );
		   		      }
		   		      sb.append( string.substring(startpos,endpos) );
		   		      sb.append("&#" + ((int)c) + ";");
		   		      startpos = endpos+1;
		   				}
		   			}
		 		      if( sb == null ) {
		 		        sb = new StringBuffer( string );
		 		      } else {
		   			sb.append( string.substring(startpos,string.length()) );
		 		      }
		   			return sb != null ? sb.toString() : string;
   	  }
}

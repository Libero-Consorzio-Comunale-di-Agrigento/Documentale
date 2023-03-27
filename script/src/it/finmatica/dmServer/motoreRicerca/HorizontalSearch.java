package it.finmatica.dmServer.motoreRicerca;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.ElapsedTime;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.dmServer.util.ModelInformation;
import it.finmatica.dmServer.util.ObjFileConditionStruct;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.dmServer.util.FieldInformation;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.log4jsuite.LogDb;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Classe che gestisce la Ricerca mediante le tabelle Orizzontali.
 * 
 * @author  G. Mannella
 * @version 3.0
 *  
*/
public class HorizontalSearch extends AbstractSearch {
	   /**
	    * Strutture dati di input relative alle chiamate effettuate 
	    * a partire dalla Classe IQuery
	   **/
	   private String area;        /* Area derivata dalla chiamata settaArea */
	
	   private Vector vCm;         /* 
	                                  Vettore derivato dalla chiamata
	                                  addCodiceModello senza area
	   							   */
	   private Vector vCmArea;	   /* 
	                                  Vettore derivato dalla chiamata
	                                  addCodiceModello con area
	   							   */
	   
	   private Vector campi;       /* 
	                                  Vettore (di keyval) derivato dalla chiamata
	                                  addCampo: semplice/con area e cm/con catego
	   							   */
	   
	   private Vector campiOrdinamento;
	   
	   private Vector OjfiCondition = new Vector(); //Vector per gli oggetti file	   
	   
	   /**
	    * CONDIZIONI AND-OR DI FULLTEXT
	   */	   	   	   
	   private String condAnd;
	   private String condOr;	   
	   
	   /**
	    * Strutture dati di output che serviranno,
	    * a partire da quelle di input, a generare 
	    * le varie select in UNION  
	   **/	   
	   private Vector vJoin;      /* 
	                                  Vettore (di JOIN) che raggruppa gli 
	                                  AREA/CM o CATEGORIE (+campi) che stanno in JOIN
	                                  in un unica struttura.
	                                  
	                                  Questa struttura rappresenta un'unica SELECT	
	                                  in union con le altre strutture                                  
	   							   */
	   
	   private Vector vArCm;      /* 
	                                  Vettore (di ArCmCatego) che raggruppa gli 
	                                  in un unica struttura AREA/CM (+campi)
	                                  che non stanno in JOIN
	                                  
	                                  Questa struttura rappresenta tante SELECT	                                 	                                  Questa struttura rappresenta un'unica SELECT	
	                                  in union per ogni elemento del vettore a loro
	                                  volta in union con le altre strutture.	                                   
	   							   */
	   
	   private Vector vCatego;    /* 
	                                  Vettore (di ArCmCatego) che raggruppa le 
	                                  in un unica struttura CATEGORIE (+campi)
	                                  che non stanno in JOIN
	                                  
	                                  Questa struttura rappresenta tante SELECT	  	                                  	                                  Questa struttura rappresenta tante SELECT	                                 	                                  Questa struttura rappresenta un'unica SELECT	
	                                  in union per ogni elemento del vettore a loro
	                                  volta in union con le altre strutture.
	   							   */
	   
	   private Vector vOrdRet;     /* 
	                                  Vettore (di FieldInformation) che raggruppa le 
	                                  in un unica struttura tutti i campi da restituire
	                                  e/o da ordinare
	   							   */ 
	   
	   /**
	    * Strutture dati di Ambiente
	   **/	   
	   private final static int    _LEFT                 = 0;
	   private final static int    _RIGHT                = 1;
	   private final static String _MARK_TO_DELETE       = "_______MARKED_____";
	   private final static String _NO_ORDER_FIELD       = "DONTCARE";
	   private final static String _SUFFIX_VIEWNAME      = "_VIEW";	  
	   private final static String _ALIAS_RET_FIELD      = "RET";
	   private final static String _STATE_DOC_NOTIN_PB   = "('CA','RE')";
	   private final static String _STATE_DOC_NOTIN      = "('CA','RE','PB')";
	   private String stateDocNotIn = _STATE_DOC_NOTIN;
	   
	   private String[] reserveWord                   = {"\\","&","?","{","}",",","(",")","[","]",
			   						                     "-",";","~","|","$","!",">","*","_"} ;
	   private String   escapeCaracter                = "\\";
	   
	   private final static String _TONUMBER_NULL     = "TO_NUMBER(NULL)";
	   private final static String _TOCHAR_NULL       = "TO_CHAR(NULL)";
	   private final static String _TODATE_NULL       = "TO_DATE(NULL)";

	   private final static String _NUMBER_FIELD_TYPE = "NUMBER";
	   private final static String _CHAR_FIELD_TYPE   = "VARCHAR2";
	   private final static String _CLOB_FIELD_TYPE   = "CLOB";
	   private final static String _DATE_FIELD_TYPE   = "DATE";
	   
	   private final static int _ARCM                 = 0;
	   private final static int _CATEGO               = 1;
	   
	   private boolean bIsRicercaPuntualeGlobale      = false;
	   private boolean bControllaPadre                = false;
	   
	   private String extraConditionSearch            = "";
	   
	   private int timeout=60;
	   private ElapsedTime elpsTime;
	   	   
	   private Vector idDoc=null;	
	   	 
	   /**
	    * Se siamo in presenza di ricerca con più modelli o categorie divise
	    * su più campi. Questi parametri mi aiutano a restituire gli idDocumento
	    * del tipo che l'utente preferisce.  SERVE SOLO NEL CASO DI MARCO
	   */
	   private String IdTipoDocIdRicercaReturn=null;
	   private String aliasReturn=null;
	   
	   /**
	    * Se vengo da un caso IBRIDO di ricerca, la select
	    * mi viene passara direttamente dal modello di
	    * ricerca di modulistica
	   */
	   private String sSelect=null;	   
	   
	   /**
	    * Se vengo da un caso di una IQueryCollection di ricerca, la select
	    * mi viene passara direttamente dalla Classe IQuery
	   */	   
	   private String sSelectByIQueryCollection=null;	 	   
	   
	   /**
	    * Record dal quale partire a fare la fetch
	    * dei risultati sul vettore restituito dalla ricerca
	   */
	   private int fetchInit=0;

  	   /**
	    * Dimensione di fetch dei risultati sul vettore
	    * restituito dalla ricerca (-1=TUTTI)
	   */
	   private int fetchSize=-1;	  
	   
	   /**
	    * True  - Resultset esaurito
	    * False - Esiste ancora almeno un record
	   */
	   private boolean bIsLastRow = false;	   
	   	   
	   private Environment vu;
	   private IDbOperationSQL dbOp;
	   private boolean bIsNew=false;  
	   
	   /**
	    * Vettore di keyval che contiene i campi
	    * restituiti dalla ricerca.
	    * I campi sono stati impostati sulla 
	    * IQuery dal metodo addCampoReturn
	    * 
	    * La struttura keyval sarà riempita come segue
	    * k.key           = campo
	    * k.value         = nome alias campo Query
	    * k.area          = area del campo
	    * k.cm            = cm del campo
	    * k.categoria     = categoria del campo
	    * k.valoriCursore = valori dei campi
	   */
	   private Vector vAliasCampiReturn = new Vector();	   
	   
	   /**
	    * documentList -> Vettore di ID documento trovati
	   **/	   	   	   	   
	   private Vector documentList;
	   
	   /**
	    * documentList -> Vettore di ID documento trovati preceduti da idTipoDoc@
	   **/	   	   	   	   
	   private Vector documentListWithIdTipoDoc;
	   
	   private boolean bIsMaster=false;	   
	   
	   private String sCatMaster="";	   
	   
	   private boolean bEscludiOrdinamento=false;
	   
	   /**
	    * Variabile per la gestione dei log su DB
	   */   
	   private LogDb log4jSuiteDb = null;
	   
	   private boolean bTrovaAnchePreBozza = false;

	   
	   public HorizontalSearch(String newArea, Vector cm, Vector cmArea, Environment newVu){
		      area=newArea;
		      vu=newVu;
		      vCm=(Vector)cm.clone();
		      vCmArea=(Vector)cmArea.clone();
		      
		      elpsTime = new ElapsedTime("HorizontalSearch",vu);
		      
		      documentList = new Vector();
		      documentListWithIdTipoDoc = new Vector();
		      
		      vJoin        = new Vector();
		      vArCm        = new Vector();
		      vCatego      = new Vector();
		      vOrdRet      = new Vector();
	   }



	/**
	    * Metodi di set e get
	   **/



	   public void setEnvironment(Environment newVu){
		      vu=newVu;
	   }	  
	   
	   public void setCampi(Vector vCampi) {
		   	  campi=vCampi;
	   }
	   
	   public void setCampiOrdinamento(Vector vCampiOrdinamento) {
		   	  campiOrdinamento=vCampiOrdinamento;
	   }	
	   
	   public void setEscludiOrdinamento(boolean bFlag) {
	   		  bEscludiOrdinamento=bFlag;	   
	   }	   
	   
	   public void setControllaPadre(boolean bFlag) {
		      bControllaPadre=bFlag;
	   }
	   
	   public void setCondizioneAnd(String sCondAnd) {
		   	  condAnd=sCondAnd;
	   }
	   
	   public void setCondizioneOr(String sCondOr) {
		   	  condOr=sCondOr;
	   }
	   
	   public void setCondizioneNot(String sCondNot) {}

	   public void setCondizioneFullText(String sCondFullText) {}
	   
	   public void setRicercaWeb(boolean bIsRicercaWeb) {}	   
	    
	   public void setIsRicercaPuntuale(boolean isRicercaPuntuale) {	
	  	      bIsRicercaPuntualeGlobale=isRicercaPuntuale;
	   }	   
	   
       public void setTimeOut(int iTime) {
    	      timeout=iTime;
       }	   
       
	   public void setFetchSize(int newFetchSize) {
		      fetchSize=newFetchSize;
	   }

	   public void setFetchInit(int newFetchInit) {
		      fetchInit=newFetchInit;
	   }       	    
	   
	   public void setMaster(boolean bMaster) {	
	  	      bIsMaster=bMaster;
	   } 	  
	   
	   public void setCatMaster(String newSCatMaster) {
		   	  sCatMaster=newSCatMaster;
	   }
	   
	   
	   public void setIdDocumento(Vector id) {
		   	  idDoc=id;
	   }	   

       public void setSqlSelect(String sel) {
    	      sSelect=sel;
       }	   

       public void setSqlCollectionIQuerySelect(String sel) {
    	   	  sSelectByIQueryCollection=sel;
       }        
       
	   public void setTypeModelReturn(String area, String cm) {
	          if (area==null) return;
	        
	          ModelInformation mi = null;
	          try {
	        	  mi= (new LookUpDMTable(vu)).lookUpTipoDoc(cm,area);
	        	  IdTipoDocIdRicercaReturn=mi.getIdTipoDoc();
	        	  aliasReturn=cm;
	          }
	          catch(Exception e) {
	        	  IdTipoDocIdRicercaReturn=null;
	          }
       }

       public void setTypeModelReturn(String categoria) {
    	      
       }      
       
	   public void setCondFiltroWAreaCasoMaster(String cond) {}     
	   

	   public void setExtraConditionSearch(String extraConditionSearch) {
			  this.extraConditionSearch = extraConditionSearch;
	   }	   	   
       
	   public void setObjFileCondition(Vector objFile) {
		      OjfiCondition=objFile;
	   }       
	   
	   public void setLog4JSuite(LogDb log4jsuite) {
		      log4jSuiteDb=log4jsuite;
	   }
	   
	   public boolean isLastRowFetch() {
		   	  return bIsLastRow;
	   }	   
	   
	   public Vector getVAliasCampiReturn() {
		      return vAliasCampiReturn;
	   }	   

	   public Vector getDocumentList(){		      		      
		   	  return documentList;
	   }	  
	   
	   public Vector getDocumentListWithIdTipoDoc(){		      		      
		   	  return documentListWithIdTipoDoc;
	   }	   
	   
	   public boolean isBTrovaAnchePreBozza() {
			  return bTrovaAnchePreBozza;
	   }

	   public void setBTrovaAnchePreBozza(boolean trovaAnchePreBozza) {
		      bTrovaAnchePreBozza = trovaAnchePreBozza;
	   }	   
	   
	   public void resetDocumentList() {
	         documentList.removeAllElements();
	         documentListWithIdTipoDoc.removeAllElements();
	   }	   
	   
	   public String getError() {
		  	  return "";
	   }	   
	   
	   public String getSQLSelect() throws Exception {
		      String sql="";
		      
		      if (sSelect==null) {
			      try {
			    	if (idDoc!=null && idDoc.size()>0) {
		    	       sql=createSQLidDoc();
				    }				    
				    else {
					   buildOutputStructure();			      			      
					   sql=createSQL();
				    } 		
			      }
			      catch (Exception e){			    	
			    	throw new Exception("HorizontalSearch:getSQLSelect() - createSQLSelect. Costruzione della ricerca. "+e.getMessage());
			      }
		      }
		      else
		    	  throw new Exception("HorizontalSearch:getSQLSelect(). Impossibile tornare la select: TRATTASI DI SELECT ESTERNA!!!");
		       
		      return sql;
	   }

	   /**
	    * Esegue la ricerca 
	   **/	   
	   public void ricerca() throws Exception {		    
		      String select;
		      
		      if (bTrovaAnchePreBozza) stateDocNotIn=_STATE_DOC_NOTIN_PB;
		     
		      if (sSelectByIQueryCollection!=null) {
		    	  //buildOutputStructure();
		    	  select=sSelectByIQueryCollection;
		      }
		      else {
			      //Caso passaggio select ibrida
			      if (sSelect!=null) {
			    	  String sOpt="/*+ OPT_PARAM('_optimizer_cost_based_transformation' 'off') */";
			    	  //Se sono nel caso del full_text, devo togliere l'optimizer a off
			    	  if (condAnd!=null)
			    	     if (sSelect.indexOf(sOpt)!=-1) {
			    			 String sPrimaParte,sSecondaParte;
			    			 
			    	    	 int len=sOpt.length();
			    			 sPrimaParte=sSelect.substring(0,sSelect.indexOf(sOpt));
			    			 sSecondaParte=sSelect.substring(sSelect.indexOf(sOpt)+len+1);
			    			 
			    			 sSelect=sPrimaParte+" "+sSecondaParte;
			    	     }	
			    	  
			    	  select=sSelect;		    	  
			    	  		      			      
			          //Controllo il filtro della warea (full text)
			    	  if (condAnd!=null) {
			    		 int indexUnion=select.indexOf("UNION");
			    		 String sSelect;
			    		 String sUnion;
			    		 //String cmAlias="";
			    		 String condizione="";
			    		 
			    		 /*if (IdTipoDocIdRicercaReturn!=null) {
			    			 cmAlias=""+(new LookUpDMTable(vu)).lookUpNomeTabellaOrizontaleByIdTipdoc(IdTipoDocIdRicercaReturn);
			    			 cmAlias=cmAlias.substring(4,cmAlias.length());
			    		 }*/
			    		 
			    		 condizione=Global.replaceAll(protectReserveWord(calcolaCondizoneFullText()),"'","''");
			    		 
			    		 if (indexUnion!=-1) {
			    			sSelect=select.substring(0,indexUnion);
			    			sUnion=select.substring(indexUnion,select.length());
			    			
			    			if (aliasReturn==null)
			    				select=sSelect+" and CONTAINS(FULL_TEXT,'"+condizione+"')>0 "+sUnion;
			    			else
			    				select=sSelect+" and CONTAINS("+aliasReturn+".FULL_TEXT,'"+condizione+"')>0 "+sUnion;
			    		 }
			    		 else {
			    			select+="and CONTAINS("+aliasReturn+".FULL_TEXT,'"+condizione+"')";
			    		 }
			    	  }
			      }
			      //Caso passaggio idDoc
			     else if (idDoc!=null && idDoc.size()>0) {
			    	  select=createSQLidDoc();
			      }
			      //Caso Nuova SELECT TUTTA ORIZZONTALE
			      else {
				      buildOutputStructure();					     
				      
				      select=createSQL();
			      }
		      }
		      //System.out.println(select);
		      execSQL(select);		      		      
		      
	   }
	   	   
	   public String toString() {
		      String sRet=null;
		      
		      sRet ="******* CASO JOIN *******\n\n";
		      for(int i=0;i<vJoin.size();i++) {
		    	  sRet+="\n******* JOIN N° ("+(i+1)+") *******\n\n";
		    	  sRet+=((Join)vJoin.get(i)).toString();		    	  
		      }
		      sRet+="******* FINE CASO JOIN *******\n\n";
		      
		      sRet+="******* CASO ARCM *******\n\n";
		      for(int i=0;i<vArCm.size();i++) {
		    	  sRet+="\n******* ARCM N° ("+(i+1)+") *******\n\n";
		    	  sRet+=((ArCmCatego)vArCm.get(i)).toString();		    	  
		      }
		      sRet+="******* FINE CASO ARCM *******\n\n";	     
		      
		      sRet+="******* CASO CATEGO *******\n\n";
		      for(int i=0;i<vCatego.size();i++) {
		    	  sRet+="\n******* CATEGO N° ("+(i+1)+") *******\n\n";
		    	  sRet+=((ArCmCatego)vCatego.get(i)).toString();		    	  
		      }
		      sRet+="******* FINE CATEGO *******\n\n";
		      
		      sRet+="******* ORDINAMENTO E RETURN *******\n\n";
		      for(int i=0;i<vOrdRet.size();i++) {
		    	  sRet+="\n******* ORDRET N° ("+(i+1)+") *******\n\n";
		    	  sRet+=((FieldInformation)vOrdRet.get(i)).toString();		    	  
		      }
		      sRet+="******* FINE ORDINAMENTO E RETURN *******\n\n";				      
		      
		      return sRet;
	   }
	   
	   /**
	    * Esegue la select principale della ricerca
	    * @throws Exception
	   */	   
	   private void execSQL(String sSql) throws Exception {	   
		       try {
		   		   dbOp = connect();		   		   
		   		    
			       if (vu.Global.PRINT_QUERY.equals("S")) {
			   		   System.out.println("[INFO Query di Ricerca (ORIZZONTALE) - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sSql.toString());
			   	   }
			      // System.out.println("[INFO Query di Ricerca (ORIZZONTALE) - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sSql.toString());
			       if (log4jSuiteDb!=null) 
		    		   log4jSuiteDb.ScriviLog(sSql.toString(),"Costruzione SQL",Global.TAG_RICERCA_SEMPLICE_CREATESQL,LogDb.INFO_LEVEL);			       
			       
			       lastQueryExecuted=sSql.toString();
			       dbOp.setStatement(sSql.toString());
		           
		           dbOp.getStmSql().setQueryTimeout(timeout);		
			       //dbOp.getStmSql().setQueryTimeout(3600);		

		           if (vu.Global.PRINT_QUERY.equals("S")) elpsTime.start("Esecuzione della Query"," ");
		           dbOp.execute(); 
		           if (vu.Global.PRINT_QUERY.equals("S")) elpsTime.stop();		           		           		           			       

			       if (log4jSuiteDb!=null) {
			    	   if (elpsTime!=null)
			    		   log4jSuiteDb.ScriviLog(sSql.toString(),"Esecuzione SQL",Global.TAG_RICERCA_SEMPLICE_EXECSQL,LogDb.INFO_LEVEL,elpsTime.getLastElpsTime());
			    	   else
			    		   log4jSuiteDb.ScriviLog(sSql.toString(),"Esecuzione SQL",Global.TAG_RICERCA_SEMPLICE_EXECSQL,LogDb.INFO_LEVEL);
			       }		           
		           
		           ResultSet rst = dbOp.getRstSet();

		           if (fetchInit!=0) {
		        	  int conta=1;
		        	  while(conta++!=fetchInit) rst.next();		        	  
		           }		        
		           
		           addToAliasCampiReturn("ID","ID",null,null,null);
		           addToAliasCampiReturn("_CR_","CR",null,null,null);
		           		          
		           int conta=0;
                   while (rst.next() && conta++!=fetchSize) {   
                	     documentList.add(""+rst.getLong(1));
                	     documentListWithIdTipoDoc.add(rst.getLong("ti")+"@"+rst.getLong(1));
                	     int offsetCampo=4;
                      
                         //Estraggo i campi da restituire
                         for(int i=0;i<vAliasCampiReturn.size();i++) {
                    	    keyval kAppoggio = (keyval)vAliasCampiReturn.get(i);
                    	    
                    	                        	    
                    	    kAppoggio.valoriCursore.add(rst.getString(kAppoggio.getVal()));
                    	   
                    	    int indexCampo;                    	    
                    	    
                    	    if (kAppoggio.getVal().equals("ID"))
                    	    	indexCampo=1;
                    	    else if (kAppoggio.getVal().equals("CR"))
                    	    	indexCampo=4;
                    	    else                    	    	
                    	         indexCampo=Integer.parseInt(kAppoggio.getVal().substring(_ALIAS_RET_FIELD.length()))+offsetCampo;
                    	    
                    	    kAppoggio.valoriTypeCursore.add(rst.getMetaData().getColumnTypeName(indexCampo));
                    	    kAppoggio.valoriSizeCursore.add(rst.getMetaData().getPrecision(indexCampo));
                    	    
                    	    vAliasCampiReturn.set(i,kAppoggio);
                         }
                   }
                   
                   bIsLastRow=rst.isAfterLast();                               
                       		          
	               disconnect();                   
		   	   }
		   	   catch (Exception e){
			       if (log4jSuiteDb!=null) {
			    	   if (elpsTime!=null)
			    		   log4jSuiteDb.ScriviLog(sSql.toString(),e.getMessage(),Global.TAG_RICERCA_SEMPLICE_ERRORSQL,LogDb.ERROR_LEVEL,elpsTime.getLastElpsTime());
			    	   else
			    		   log4jSuiteDb.ScriviLog(sSql.toString(),e.getMessage(),Global.TAG_RICERCA_SEMPLICE_ERRORSQL,LogDb.ERROR_LEVEL);
			       }	
		   		   
		           disconnect();			           
		   		   throw new Exception("Esecuzione query ricerca: "+ sSql.toString() +"\nErrore: "+ e.getMessage());
		   	   }			       
	   }
	   
	   /**
	    * Vengono riempite le strutture dati di output che serviranno,
	    * a partire da quelle di input, a generare le varie select in UNION  
	   **/	   
	   private void buildOutputStructure() throws Exception {
		   	   try {
		   		 AdjustArCmStructure();  
		   	   }
		       catch (Exception e) {
		   		 throw new Exception("HorizontalSearch::AdjustArCmStructure\nErrore: "+
		   				             e.getMessage());
		   	   }		   	   
		   
		       try {
		         buildJoinStructure();
		       }
		       catch (Exception e) {
		   		 throw new Exception("HorizontalSearch::buildOutputStructure\nErrore: "+
		   				             e.getMessage());
		   	   }
		       
		       try {
		         buildArCmStructure();
		       }
		       catch (Exception e) {
		   		 throw new Exception("HorizontalSearch::buildArCmStructure\nErrore: "+
		   				             e.getMessage());
		   	   }
		       
		       try {
		         buildCategoStructure();
		       }
		       catch (Exception e) {
		   		 throw new Exception("HorizontalSearch::buildCategoStructure\nErrore: "+
		   				             e.getMessage());
		   	   }		
		       
		       try {
		         buildReturnOrdinamentoStructure();
		       }
		       catch (Exception e) {
		   		 throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructure\nErrore: "+
		   				             e.getMessage());
		   	   }
	   }
	   
	   /**    
	    * Prima di sistemare le strutture passate dalla IQuery controllo
	    * se ho passato solo campi di tipo return e ordinamento (non anonimi)
	    * ma non ho nessun ar/cm passati. In tal caso li aggiungo su 
	    * vCmArea,vCm come se avessi fatto una Iq.addCodiceModello(ar,cm)
	    * dove ar e cm sono presi dai campi
	   **/	   
	   private void AdjustArCmStructure() throws Exception {
		   	   if (vCm.size()==0 && campi.size()==0 && campiOrdinamento.size()>0) {
		    	   for(int i=0;i<campiOrdinamento.size();i++) {
			    	   //E' anonimo, lo salto
			    	   if (campiOrdinamento.get(i) instanceof String) continue;
			    	   
			    	   keyval kCampo = (keyval)campiOrdinamento.get(i);
			    	   
			    	   //E' una categoria....TODO IMPLEMENTATION
			    	   if ((kCampo.getArea()==null) || kCampo.getArea().equals("")) {
			    		   
			    	   }
			    	   else {
			    		   if (!isStringInVector(vCmArea,kCampo.getArea())) vCmArea.add(kCampo.getArea());
			    		   if (!isStringInVector(vCm,kCampo.getCm()))       vCm.add(kCampo.getCm());
			    	   }
		    	   }
		       }
	   }
	   
	   /**
	    * Riempimento del vettore di strutture di tipo JOIN  
	   **/	   
	   private void buildJoinStructure() throws Exception {
		       //Ciclo sui campi per cercare i JOIN  	    	   
		       int joinAttuale=0;		       		       
		       
   		   	   for(int i=0;i<campi.size();i++) {
   		   		   keyval[] kLeftRight = new keyval[2];
   		   		   
   		   		   kLeftRight[_LEFT] = (keyval)campi.get(i);
   		   		  
   		   		   int indexJoin=kLeftRight[_LEFT].getIndexJoin();
		   		   
		   		   //Se non si tratta di un campo di join skippo
		   		   if (indexJoin==0 || joinAttuale>=indexJoin) continue;
		   		   		   		   
		   		   //Se ho trovato un campo di join sicuramente il campo
		   		   //successivo sarà la chiave di join con quello trovato
		   		   kLeftRight[_RIGHT] = (keyval)campi.get(++i);
		   		   
		   		   //Qui setto i valori per il prox ciclo (inizio
		   		   //nuovamente dal primo campo a cercare altri join
		   		   //ma con indice maggiore dell'attuale)
		   		   i=0;
		   		   joinAttuale=indexJoin;
		   		   
		   		   //Ora marko i due keyval per la successiva 
		   		   //cancellazione (non li devrò ripescare più)
		   		   kLeftRight[_LEFT].markForDelete();
		   		   kLeftRight[_RIGHT].markForDelete();
		   		   
		   		   //*************Riempimento della struttura di JOIN************
		   		   Join joinStructure = new Join(); 
		   		   
		   		   for (int index=0;index<2;index++) {
			   		   if (kLeftRight[index].getArea()!=null) {		   			   
			   			   //Eventuale Mapping su tipoDoc
			   			   try {
			 			     kLeftRight[index].setCm(vu.getGDMapping().getMappingTipoDoc(kLeftRight[index].getCm()));
			   			   }
			   			   catch (Exception e) {
			   				 throw new Exception("HorizontalSearch::buildJoinStructure "+
			   						             "- Errore sul mapping del Codice Modello JOIN ("+
			   						             kLeftRight[index].getCm()+"\nErrore: "+e.getMessage());
			   			   }
	
			   			   //Recupero la tabella orizzontale
			   			   String nomeTab;
			   			   try {
			 			     nomeTab=""+(new LookUpDMTable(vu)).lookUpNomeTabellaOrizontaleByArCm(kLeftRight[index].getArea(),kLeftRight[index].getCm());
			   			   }
			   			   catch (Exception e) {
			   				 throw new Exception("HorizontalSearch::buildJoinStructure "+
			   						             "- Errore nel recupero del nome Tabella Orizzontale in JOIN"+
			   						             "\nErrore: "+e.getMessage());
			   			   }
			   			   
			   			   if (index==_LEFT) {
				   			   joinStructure.setArLeft(kLeftRight[index].getArea());
				   			   joinStructure.setCmLeft(kLeftRight[index].getCm());
				   			   joinStructure.setTab_ArCmLeft(nomeTab);
				   			   joinStructure.setJoinFieldLeft(kLeftRight[index].getKey());
			   			   }
			   			   else {
			   				   joinStructure.setArRight(kLeftRight[index].getArea());
				   			   joinStructure.setCmRight(kLeftRight[index].getCm());
				   			   joinStructure.setTab_ArCmRight(nomeTab);
				   			   joinStructure.setJoinFieldRight(kLeftRight[index].getKey());			   				   
			   			   }
			   				   
			   		   }
			   		   else {
			   			   if (index==_LEFT) {
				   			   joinStructure.setCategoLeft(kLeftRight[index].getCategoria());
				   			   joinStructure.setView_CategoLeft(kLeftRight[index].getCategoria()+_SUFFIX_VIEWNAME);
				   			   joinStructure.setJoinFieldLeft(kLeftRight[index].getKey());
			   			   }
				   		   else {
			   				   joinStructure.setCategoRight(kLeftRight[index].getCategoria());
				   			   joinStructure.setView_CategoRight(kLeftRight[index].getCategoria()+_SUFFIX_VIEWNAME);
				   			   joinStructure.setJoinFieldRight(kLeftRight[index].getKey());			   				   
			   			   }				   			   
			   		   }
		   		   }
		   		   //*************FINE Riempimento della struttura di JOIN************
		   		   
		   		   vJoin.add(joinStructure);
   		   	   }
   		   	   
   		   	   //Ciclo per eliminare i keyval markati in precedenza   	
   		   	   removeMarkedFieldElements(0);
   		   	      		   	      		   	   
   		   	   String sElencoTabViewInJoin=getJoinTableView_List();
   		   	   
   		   	   if (sElencoTabViewInJoin==null) return;   		   	      		   	  
   		   	   
   		   	   //Ora controllo i campi rimasti (NON JOIN) per capire
   		   	   //quali di questi prendere
   		   	   for(int i=0;i<campi.size();i++) {
   		   		   Vector fieldInfo;
   		   		   keyval kCampo = (keyval)campi.get(i);
   		   		   
   		   		   //Campo ar/cm o catego
   		   		   if (kCampo.getArea()!=null || kCampo.getCategoria()!=null) {
   		   			   String tabView=null;
   		   			   if (kCampo.getArea()!=null) {
   		   				   //Cerco ar/cm nella struttura di join. Se esiste
   		   				   //significa che il campo deve essere preso nel join...
   		   				   tabView=getTabViewFromJoinVector(kCampo.getArea(),kCampo.getCm(),null);   		   				   
   		   			   }
   		   			   else {
   		   				   //Cerco categoria nella struttura di join. Se esiste
   		   				   //significa che il campo deve essere preso nel join...   		   				   
   		   				   tabView=getTabViewFromJoinVector(null,null,kCampo.getCategoria());
   		   			   }
   		   			   
   		   			   if (tabView!=null) {
   		   				   try { 
	   		   				 //Recupero le Info del campo (se esiste in una delle tab di JOIN)
	   		   			     fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(kCampo.getKey(),tabView);
	   		   			     //Il campo esiste in una delle tab di join
    		   			     //lo inserisco nel vettore di Join al posto 
   		   			         //giusto. Marko come delete il campo 
   		   			         //perché non è un campo anonimo e non dovrà essere 
   		   			         //riusato da altri criteri (oltre il JOIN)			   		   			     
	   		   			     if (fieldInfo!=null) {	   		   			    	
	   		   			    	 //Passo solo il campo con indice 0 perché sono
	   		   			    	 //sicuro che ce n'è uno solo (siamo nel caso non
	   		   			    	 //anonimo)
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setOp(getOperatorKeyVal(((keyval)campi.get(i))));
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setValue(((keyval)campi.get(i)).getVal());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setValueNvl(((keyval)campi.get(i)).getValueNvl());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setTipoUguaglianza(((keyval)campi.get(i)).getTipoUguaglianza());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setRicercaPuntuale(((keyval)campi.get(i)).getIsRicercaPuntuale());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setNoCaseSensitive(((keyval)campi.get(i)).isNoCaseSensitive());
	   		   			    	 setField_Into_JoinVector((FieldInformation)fieldInfo.get(0));
	   		   			    	 ((keyval)campi.get(i)).markForDelete();
	   		   			     }
	   		   			  }
				   	      catch (Exception e) {
				   		    throw new Exception("HorizontalSearch::buildJoinStructure "+
				   						        "- Errore nel recupero delle informazioni per il campo ("+kCampo.getKey()+")"+
				   						        "\nErrore: "+e.getMessage());
				   		  }
   		   			   }
   		   		   }
   		   		   //Campo Anonimo
   		   		   else {
   		   			  
   		   			  try { 
   		   				//Recupero le Info del campo (se esiste in una delle tab di JOIN)
   		   			    fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(kCampo.getKey(),sElencoTabViewInJoin);
   		   			    //Il campo esiste in una delle tab di join
   		   			    //lo inserisco nel vettore di Join al posto 
   		   			    //giusto. Non marko come delete il campo però
   		   			    //perché è un campo anonimo e potrebbe essere 
   		   			    //riusato da altri criteri (oltre il JOIN)
   		   			    if (fieldInfo!=null)   
   		   			    	for(int idxField=0;idxField<fieldInfo.size();idxField++) {
   		   			    	    ((FieldInformation)fieldInfo.get(idxField)).setOp(getOperatorKeyVal(((keyval)campi.get(i))));
	   		   			    	((FieldInformation)fieldInfo.get(idxField)).setValue(((keyval)campi.get(i)).getVal());
	   		   			    	((FieldInformation)fieldInfo.get(idxField)).setValueNvl(((keyval)campi.get(i)).getValueNvl());
	   		   			    	((FieldInformation)fieldInfo.get(idxField)).setTipoUguaglianza(((keyval)campi.get(i)).getTipoUguaglianza());
	   		   			    	((FieldInformation)fieldInfo.get(idxField)).setRicercaPuntuale(((keyval)campi.get(i)).getIsRicercaPuntuale());
	   		   			    	((FieldInformation)fieldInfo.get(idxField)).setNoCaseSensitive(((keyval)campi.get(i)).isNoCaseSensitive());
   		   			    		setField_Into_JoinVector((FieldInformation)fieldInfo.get(idxField));
   		   			    	}
   		   			  }
			   	      catch (Exception e) {
			   		    throw new Exception("HorizontalSearch::buildJoinStructure "+
			   						        "- Errore nel recupero delle informazioni per il campo 'anonimo' ("+kCampo.getKey()+")"+
			   						        "\nErrore: "+e.getMessage());
			   		  }
   		   		   }
   		   			   
   		   	   }
   		   	   
   		   	   //Ciclo per eliminare i keyval markati in precedenza   	
   		   	   removeMarkedFieldElements(0);
   		   	   
   		   	   //Controllo gli oggettiFile
   		   	   for(int indexOjfi=0;indexOjfi<OjfiCondition.size();indexOjfi++) {
   		   		   keyval kCampo = (keyval)OjfiCondition.get(indexOjfi);
   		   		   
   		   		   boolean iCheck=false;
	   			   //Trovo un campo NON ANONIMO (AR E CM SETTATI E SONO QUELLI MIEI)
	   			   //Ok! lo considero e lo marko da cancellare
	   			   if (kCampo.getArea()!=null && !kCampo.getArea().equals("")) {
	   				   if (getTabViewFromJoinVector(kCampo.getArea(),kCampo.getCm(),null)!=null) {
	   					   iCheck=true;
	   					   kCampo.markForDelete();
	   				   }
	   			   }
	   			   //E' un campo ANONIMO (se nn  ha categoria)
	   			   //Ok! lo considero ma non lo cancello
	   			   else {
	   				  if (kCampo.getCategoria()==null || kCampo.getCategoria().equals("")) 
	   				      iCheck=true; 
	   				  else if (getTabViewFromJoinVector(null,null,kCampo.getCategoria())!=null) {
	   					   iCheck=true;
	   					   kCampo.markForDelete();	   					  
	   				  }
	   			   } 

	   			   if (iCheck) fileJoinCondition.add(kCampo.getKey()); 
   		   	   }   		   	      		   	      		   	      		   	      		   	    		   		 
   		   	   
   		   	   //Ciclo per cercare di markare come eliminati gli ar/cm
   		   	   //presenti nei join   		   	  
   		   	   for(int i=0;i<vCm.size();i++) {
   		   		   String sArea;
   		   		   String sCm=(String)vCm.get(i);
   		   		   
   		   		   if (!(vCmArea.get(i).equals(""))) 
   		   			   sArea=(String)vCmArea.get(i);
   		   		   else
   		   			   sArea=area;
   		   		   
   		   		   //Ho trovato ar/cm
   		   		   if (getTabViewFromJoinVector(sArea,sCm,null)!=null) 
   		   			   vCm.set(i,_MARK_TO_DELETE+vCm.get(i));   		   		   
   		   	   }
   		   	   
   		   	   //Ciclo per eliminare gli ar/cm markati in precedenza 
   		   	   removeMarkedArCmElements();
	   }
	   
	   /**
	    * Riempimento del vettore di strutture di tipo ARCM  
	   **/	   
	   private void buildArCmStructure() throws Exception {
		       final int _IS_ANONYMOUS_FIELD = 1;
		       final int _IS_NOT_ANONYMOUS_FIELD = 2;
		       
		       Vector vObjFileCondition = new Vector();
		       
		       //Per prima cosa ciclo sui campi con AR/CM
		       //controllando quelli che non stanno nei vettori
		       //vCm e vCmArea. Se non ci stanno li inserisco
		       //altrimenti questi non verrebbero mai considerati
		       //nel ciclo
		       for(int i=0;i<campi.size();i++) {
		    	   keyval k = (keyval)campi.get(i);
		    	   if (k.getArea()!=null) {
		    		   if (indexCmInVector(k.getArea(),k.getCm())==-1) {
		    			   vCm.add(k.getCm());
		    			   vCmArea.add(k.getArea());
		    		   }		    			   
		    	   }		    		   
		       }
		   
		       //Ciclo sui cm rimasti dopo il passaggio dei JOIN	   	  
   		   	   for(int i=0;i<vCm.size();i++) {
   		   		   //Determino AR/CM
   		   		   String sArea;
   		   		   String sCm=(String)vCm.get(i);
   		   		   
   		   		   if (!(vCmArea.get(i).equals(""))) 
   		   			   sArea=(String)vCmArea.get(i);
   		   		   else
   		   			   sArea=area;   
   		   		   
   		   		   //Recupero il nome della tabella Orizzontale
	   			   String nomeTab;
	   			   try {
	 			     nomeTab=""+(new LookUpDMTable(vu)).lookUpNomeTabellaOrizontaleByArCm(sArea,sCm);
	   			   }
	   			   catch (Exception e) {
	   				 throw new Exception("HorizontalSearch::buildArCmStructure "+
	   						             "- Errore nel recupero del nome Tabella Orizzontale in ARCM"+
	   						             "\nErrore: "+e.getMessage());
	   			   }   		   		   
   		   		   
   		   		   //Aggiungo la testata AR/CM al vettore
   		   		   ArCmCatego arCmCat = new ArCmCatego();
   		   		   arCmCat.setAr(sArea);
   		   		   arCmCat.setCm(sCm);
   		   		   arCmCat.setTab_ArCm(nomeTab);
   		   		   vArCm.add(arCmCat);
   		   		   
   		   		   //Lo marco per la cancellazione
   		   		   vCm.set(i,_MARK_TO_DELETE+vCm.get(i));   		   		   
   		   		      		   		   
   		   		   //Cerco i campi corrispondenti
   		   		   for(int indexCampo=0;indexCampo<campi.size();indexCampo++) {
   		   			   keyval kCampo = (keyval)campi.get(indexCampo);
   		   			   Vector fieldInfo;   		   			   
   		   			   int iTestField=0;
   		   			   //Trovo un campo NON ANONIMO (AR E CM SETTATI E SONO QUELLI MIEI)
   		   			   //Posso recuperare le INFO
   		   			   if (kCampo.getArea()!=null) {
   		   				   if (kCampo.getArea().equals(sArea) &&
   		   				       kCampo.getCm().equals(sCm)) {
   		   					   iTestField=_IS_NOT_ANONYMOUS_FIELD;
   		   				   }
   		   			   }
   		   			   //E' un campo ANONIMO (se nn  ha categoria)
   		   			   //Posso recuperare le INFO
   		   			   else {
   		   				  if (kCampo.getCategoria()==null) 
   		   				      iTestField=_IS_ANONYMOUS_FIELD; 
   		   			   }
   		   			   
   		   			   if (iTestField>0) {
   		   				   try { 
	   		   				 //Recupero le Info del campo (se esiste in una delle tab di JOIN)
	   		   			     fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(kCampo.getKey(),nomeTab);
	   		   			     //Il campo esiste nella tabella
    		   			     //lo inserisco nel vettore di ARCM al posto 
   		   			         //giusto.		   		   			     
	   		   			     if (fieldInfo!=null) {	   		   			    	
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setOp(getOperatorKeyVal(kCampo));
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setValue(kCampo.getVal());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setValueNvl(kCampo.getValueNvl());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setTipoUguaglianza(kCampo.getTipoUguaglianza());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setRicercaPuntuale(kCampo.getIsRicercaPuntuale());
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setNoCaseSensitive(kCampo.isNoCaseSensitive());
	   		   			    	 ((ArCmCatego)vArCm.get(i)).getField().add(fieldInfo.get(0));	   		   			    	
	   		   			     }
	   		   			  }
				   	      catch (Exception e) {
				   		    throw new Exception("HorizontalSearch::buildArCmStructure "+
				   						        "- Errore nel recupero delle informazioni per il campo ("+kCampo.getKey()+")"+
				   						        "\nErrore: "+e.getMessage());
				   		  }
				   	      
				   	      //Se il campo NON è anonimo lo marko come cancellato
				   	      //Perché non lo dovrò utilizzare per altre condizioni
				   	      if (iTestField!=_IS_ANONYMOUS_FIELD)
				   	    	  kCampo.markForDelete();
   		   			   }
   		   		   }//END FOR CAMPI   		   		  		   		   
   		   		   
   		   		   //Ciclo per eliminare i keyval campi markati in precedenza   	
   		   		   removeMarkedFieldElements(0);
   		   		   
   		   		   
   		   		   
   		   		   
   		   		   //Cerco le condizioni file corrispondenti
   		   		   for(int indexOjfi=0;indexOjfi<OjfiCondition.size();indexOjfi++) {
   		   			   keyval kCampo = (keyval)OjfiCondition.get(indexOjfi);
   		   			   
   		   			   boolean iCheck=false;
   		   			   //Trovo un campo NON ANONIMO (AR E CM SETTATI E SONO QUELLI MIEI)
   		   			   //Ok! lo considero e lo marko da cancellare
   		   			   if (kCampo.getArea()!=null && !kCampo.getArea().equals("")) {
   		   				   if (kCampo.getArea().equals(sArea) &&
   		   				       kCampo.getCm().equals(sCm)) {
   		   					   iCheck=true;
   		   					   kCampo.markForDelete();
   		   				   }
   		   			   }
   		   			   //E' un campo ANONIMO (se nn  ha categoria)
   		   			   //Ok! lo considero ma non lo cancello
   		   			   else {
   		   				  if (kCampo.getCategoria()==null || kCampo.getCategoria().equals("")) 
   		   				      iCheck=true; 
   		   			   }   		   		
   		   			  
   		   			   if (iCheck)  ((ArCmCatego)vArCm.get(i)).getFileCondition().add(new ObjFileConditionStruct(kCampo.getKey(),kCampo.getIsOcr()));   		   			   
   		   			  
   		   		   }//END FOR OGGETTI FILE      		   		      		   		   
   		   		   
   		   		   //Ciclo per eliminare i keyval objfile markati in precedenza   	
   		   		   removeMarkedFieldElements(1);
   		   		   
   		   		   
   		   	   }//End AR/CM
   		   	   
   		   	   //Ciclo per eliminare gli ar/cm markati in precedenza 
   		   	   removeMarkedArCmElements();   	   		   	   
   		   	      		   	   
	   }
	   
	   /**
	    * Riempimento del vettore di strutture di tipo CATEGO 
	   **/	   
	   private void buildCategoStructure() throws Exception {		   	 
		       //Ciclo sui campi per vedere se ci sono
		       //Categorie derivate dalla addCampoCategoria
		       //e non prese nel join
		       for(int i=0;i<campi.size();i++) {
		    	   keyval kCampo = (keyval)campi.get(i);
		    	   int indexvCatego;
		    	   		    	   
		    	   if (kCampo.getCategoria()==null) continue;
		    	   
		    	   //Prima di inserirlo nel vettore controllo
		    	   //se non esiste già nel vCatego
		    	   indexvCatego=indexCategoInVector(kCampo.getCategoria());
		    	   if (indexvCatego==-1) {
		    		   //Se non esiste già inserisco la testata
		    		   ArCmCatego arCmCat = new ArCmCatego();
	   		   		   arCmCat.setCatego(kCampo.getCategoria());
	   		   		   arCmCat.setView_Catego(kCampo.getCategoria()+_SUFFIX_VIEWNAME);
		    	       vCatego.add(arCmCat);
		    	       indexvCatego=vCatego.size()-1;
		    	   }   				    	   
		    	   
		    	   //Recupero le Info del campo
		    	   try {
	   		   	     Vector fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(kCampo.getKey(),kCampo.getCategoria()+_SUFFIX_VIEWNAME);
	   		   	     
	   		   	     if (fieldInfo!=null) {
	   			    	 ((FieldInformation)fieldInfo.get(0)).setOp(getOperatorKeyVal(kCampo));	   			    	
	   			    	 ((FieldInformation)fieldInfo.get(0)).setValue(kCampo.getVal());
	   			    	 ((FieldInformation)fieldInfo.get(0)).setValueNvl(kCampo.getValueNvl());
	   			    	 ((FieldInformation)fieldInfo.get(0)).setTipoUguaglianza(kCampo.getTipoUguaglianza());
	   			    	 ((FieldInformation)fieldInfo.get(0)).setRicercaPuntuale(kCampo.getIsRicercaPuntuale());
	   			    	 ((FieldInformation)fieldInfo.get(0)).setNoCaseSensitive(kCampo.isNoCaseSensitive());
	   			    	 ((ArCmCatego)vCatego.get(indexvCatego)).getField().add(fieldInfo.get(0));	   		   	    	 
	   		   	     }
	   		   	   }
				   catch (Exception e) {
				   	  throw new Exception("HorizontalSearch::buildCategoStructure "+
				   						  "- Errore nel recupero delle informazioni per il campo ("+kCampo.getKey()+")"+
				   						  "\nErrore: "+e.getMessage());
				   }
				   
	   		   	   //Cerco le condizioni file corrispondenti
	   		   	   for(int indexOjfi=0;indexOjfi<OjfiCondition.size();indexOjfi++) {
	   		   			   keyval kCampoObjFile = (keyval)OjfiCondition.get(indexOjfi);
	   		   			   
	   		   			   boolean iCheck=false;
	   		   			   //Trovo un campo NON ANONIMO (AR E CM SETTATI E SONO QUELLI MIEI)
	   		   			   //Ok! lo considero e lo marko da cancellare
	   		   			   if (kCampoObjFile.getCategoria()!=null && !kCampoObjFile.getCategoria().equals("")) {
	   		   				   if (kCampoObjFile.getCategoria().equals(kCampo.getCategoria())) {
		   		   				   iCheck=true;
		   		   				   kCampoObjFile.markForDelete();   		
	   		   				   }
	   		   			   }
	   		   			   //E' un campo ANONIMO (se nn  ha categoria)
	   		   			   //Ok! lo considero ma non lo cancello
	   		   			   else {
	   		   				  if (kCampoObjFile.getArea()==null || kCampoObjFile.getArea().equals("")) 
	   		   				      iCheck=true; 
	   		   			   }   		   		
	   		   			  	   		   			       
	   		   			   if (iCheck) ((ArCmCatego)vCatego.get(indexvCatego)).getFileCondition().add(new ObjFileConditionStruct(kCampoObjFile.getKey(),kCampoObjFile.getIsOcr()));
	   		   		}//END FOR OGGETTI FILE   	   		   		   
	   		   	   					   		   	   
   		   		   //Ciclo per eliminare i keyval objfile markati in precedenza   	
   		   		   removeMarkedFieldElements(1);	   		   	   
	   		   	   
		       }
		       	       
	   }
	   
	   /**
	    * Riempimento il vettore di ordinamento e return 
	   **/	   
	   private void buildReturnOrdinamentoStructure() throws Exception {
		       //Scorro i campi di return ed ordinamento
		       //per sistemarli nelle strutture vOrdRet
		       for(int i=0;i<campiOrdinamento.size();i++) {
		    	   String sCampo, sOrdina, sFormatoCampoDaKeyval=null;
		    	   keyval kRetOrd=null;
		    	   
		    	   if (campiOrdinamento.get(i) instanceof String) {
		    		  String sNomeCompleto=""+campiOrdinamento.get(i);
		    		  
		    		  sCampo=sNomeCompleto.substring(0,sNomeCompleto.indexOf("@"));
		    	      sOrdina=sNomeCompleto.substring(sNomeCompleto.indexOf("@")+1);	
		    	   }
		    	   else {
		    	      kRetOrd = (keyval)campiOrdinamento.get(i);
		    	      sCampo=kRetOrd.getKey().substring(0,kRetOrd.getKey().indexOf("@"));
		    	      sOrdina=kRetOrd.getKey().substring(kRetOrd.getKey().indexOf("@")+1);

					   sFormatoCampoDaKeyval = kRetOrd.getFormatoCampo();
		    	   }   
		    	   		    	   		    	   	    	   		    	   
		    	   //E' un campo ANONIMO di ordinamento
		    	   //Mi scorro tutte le strutture ARCM / JOIN / CATEGO
		    	   //per cercare dov'è presente e che tipo è (se si capisce)
		    	   //dove lo trovo inserisco il suo index (di vettore)
		    	   //come riferimento sulle strutture
		    	   if (kRetOrd==null) {
		    		   buildReturnOrdinamentoStructureAnonymousField(sCampo,sOrdina);
		    	   }
		    	   //E' un campo NON ANONIMO
		    	   //Mi limito a capire la tabella orizzontale ed il tipo di campo
		    	   else {
		    		   Vector fieldInfo;
		    		   //CASO ARCM
		    		   if (kRetOrd.getArea()!=null) {
		    			   //Recupero il nome della tabella orizzontale
		    			   String nomeTab;
			   			   try {
			 			     nomeTab=""+(new LookUpDMTable(vu)).lookUpNomeTabellaOrizontaleByArCm(kRetOrd.getArea(),kRetOrd.getCm());
			   			   }
			   			   catch (Exception e) {
			   				 throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructure "+
			   						             "- Errore nel recupero del nome Tabella Orizzontale in ARCM"+
			   						             "\nErrore: "+e.getMessage());
			   			   }
			   			   
			   			   try { 
	   		   				 //Recupero le Info del campo 	   				 
	   		   			     fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(sCampo,nomeTab);

	   		   			     String formatoData;
	   		   			     String tipoDato=""+(new LookUpDMTable(vu)).lookUpTipoDato(kRetOrd.getArea(),sCampo);
	   		   			     if (sFormatoCampoDaKeyval!=null) {
								 formatoData=sFormatoCampoDaKeyval;
							 }
	   		   			     else {
								 formatoData=tipoDato.substring(tipoDato.indexOf("@")+1);
							 }
	   		   			     
	   		   			     if (fieldInfo!=null) {
	   		   			    	 //Il campo esiste nella tabella
    		   			         //lo inserisco nel vettore di vOrdRet   	   		   			     	   		   			    
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setCampoReturn(true);
	   		   			    	 if (!sOrdina.equals(_NO_ORDER_FIELD))
		    		                 ((FieldInformation)fieldInfo.get(0)).setTipoOrdinamento(sOrdina);
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setNomeCampo(sCampo);
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setFormatoData(formatoData);
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setTableViewName(nomeTab);	   		   			    	 
	   		   			    	 vOrdRet.add((FieldInformation)fieldInfo.get(0));
	   		   			    	 addToAliasCampiReturn(sCampo,_ALIAS_RET_FIELD+(vOrdRet.size()),kRetOrd.getArea(),kRetOrd.getCm(),null);
	   		   			     }
	   		   			  }
				   	      catch (Exception e) {
				   		    throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructure "+
				   						        "- Errore nel recupero delle informazioni per il campo ARCM ("+sCampo+")"+
				   						        "\nErrore: "+e.getMessage());
				   		  }
		    		   }
		    		   //CASO CATEGO
		    		   else {
			   			   try { 
	   		   				 //Recupero le Info del campo 			   				 
	   		   			     fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(sCampo,kRetOrd.getCategoria()+_SUFFIX_VIEWNAME);
	   		   			     
	   		   			     if (fieldInfo!=null) {
	   		   			    	 //Il campo esiste nella tabella
    		   			         //lo inserisco nel vettore di vOrdRet   	   		   			     	   		   			    
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setCampoReturn(true);
	   		   			    	 if (!sOrdina.equals(_NO_ORDER_FIELD))
		    		                 ((FieldInformation)fieldInfo.get(0)).setTipoOrdinamento(sOrdina);
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setNomeCampo(sCampo);
	   		   			    	 ((FieldInformation)fieldInfo.get(0)).setTableViewName(kRetOrd.getCategoria()+_SUFFIX_VIEWNAME);

								 if (sFormatoCampoDaKeyval!=null) {
									 ((FieldInformation)fieldInfo.get(0)).setFormatoData(sFormatoCampoDaKeyval);
								 }

	   		   			    	 vOrdRet.add((FieldInformation)fieldInfo.get(0));
	   		   			    	 addToAliasCampiReturn(sCampo,_ALIAS_RET_FIELD+(vOrdRet.size()),null,null,kRetOrd.getCategoria());
	   		   			     }
	   		   			  }
				   	      catch (Exception e) {
				   		    throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructure "+
				   						        "- Errore nel recupero delle informazioni per il campo CATEGORIA ("+sCampo+")"+
				   						        "\nErrore: "+e.getMessage());
				   		  }		    			   
		    		   }		    			   
		    	   }		    		   
		       }
	   }
	   
	   /**
	    * Metodo di supporto alla buildReturnOrdinamentoStructure che gestisce
	    * il riempimento il vettore di ordinamento e return nel caso dei campi 
	    * di ordinamento di tipo anonimo. 
	   **/	   
	   private void buildReturnOrdinamentoStructureAnonymousField(String nomeCampo,String tipoOrdinamento) throws Exception {
		       //Mi scorro tutte le strutture ARCM / JOIN / CATEGO
		       //per cercare dov'è presente il campo e che tipo è (se si capisce)
		       //dove lo trovo inserisco il suo index (di vettore)
		       //come riferimento sulle strutture
		   
		   	   Vector fieldInfo;
		   	   FieldInformation fi = null;
		   
		       //Scorro la struttura dei JOIN
		       for(int i=0,jInterno=0;i<vJoin.size();) {
		    	   Join j = (Join)vJoin.get(i);
		    	   
		    	   String ar=null,cm=null,catego=null;
		    	   String sNomeTabOrView;
		    	   
		    	   if (jInterno==0) {
			    	   //Testo il lato sinistro del join
			    	   if (j.getArLeft()!=null) {
			    		   sNomeTabOrView=j.getTab_ArCmLeft();	
			    		   ar=j.getArLeft();
			    		   cm=j.getCmLeft();
			    	   }
			    	   else {
			    		   sNomeTabOrView=j.getView_CategoLeft();
			    		   catego=j.getCategoLeft();			    		   
			    	   }
			    	   
			    	   jInterno=1;
		    	   }
		    	   else {
		    		   //Testo il lato destro del join
			    	   if (j.getArRight()!=null) {
			    		   sNomeTabOrView=j.getTab_ArCmRight();	
			    		   ar=j.getArRight();
			    		   cm=j.getCmRight();			    		   
			    	   }
			    	   else {
			    		   sNomeTabOrView=j.getView_CategoRight();
			    		   catego=j.getCategoRight();
			    	   }
			    	   
			    	   jInterno=0;
			    	   i++;
		    	   }
		    	   
		    	   try { 
		    		 fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(nomeCampo,sNomeTabOrView);
		    	   
		    	   
			    	 //Il campo è stato trovato! quindi inserisco il riferimento (indice del vettore di ordinamento)
			    	 //nella struttura di join e metto il campo in lista ordinamento/return
			    	 //A questo punto forzo l'uscita dal ciclo perché non ha senso cercare altri riferimenti,
			    	 //uso il primo che trovo.
			    	 if (fieldInfo!=null) {
			             ((FieldInformation)fieldInfo.get(0)).setCampoReturn(true);
		   		   		 ((FieldInformation)fieldInfo.get(0)).setTipoOrdinamento(tipoOrdinamento);
		   		   		 ((FieldInformation)fieldInfo.get(0)).setNomeCampo(nomeCampo);
		   		   		 //In questo modo capisco che si tratta di un campo anonimo
		   		   		 ((FieldInformation)fieldInfo.get(0)).setTableViewName(null);
		   		   		 fi = ((FieldInformation)fieldInfo.get(0));
		   		   		 vOrdRet.add(fi);
		   		   		 addToAliasCampiReturn(nomeCampo,_ALIAS_RET_FIELD+(vOrdRet.size()),ar,cm,catego);
		   		   		 j.getFieldIndexOrdinamento().put(""+(vOrdRet.size() - 1),sNomeTabOrView);
		   		   		 i=vJoin.size();
		   		   		 continue;
			    	 }
			       }
			       catch (Exception e) {
				     throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructureAnonymousField "+
				   						 "- Errore nel recupero delle informazioni per il campo ("+nomeCampo+") (CASO JOIN)"+
				   						 "\nErrore: "+e.getMessage());
				   }	
		       }
		       //FINE SCRORRIMENTO STRUTTURA DEI JOIN
		       
		       FieldInformation fiRif=fi;
		       
		       //Scorro la struttura degli ARCM
		       for(int i=0;i<vArCm.size();i++) {
		    	   ArCmCatego arcm = (ArCmCatego)vArCm.get(i);
		    	   
		    	   fiRif=buildReturnOrdinamentoStructureAnonymousFieldCategoArCm(arcm,fiRif,nomeCampo,tipoOrdinamento);
		       } 
		       
		       //Scorro la struttura dei catego
		       for(int i=0;i<vCatego.size();i++) {
		    	   ArCmCatego catego = (ArCmCatego)vCatego.get(i);
		    	   
		    	   fiRif=buildReturnOrdinamentoStructureAnonymousFieldCategoArCm(catego,fiRif,nomeCampo,tipoOrdinamento);
		       } 		       
	   }
	   
	   /**
	    * Metodo di supporto alla buildReturnOrdinamentoStructureAnonymousField che gestisce
	    * il riempimento il vettore di ordinamento e return nel caso dei campi 
	    * di ordinamento di tipo anonimo per categorie e arcm
	   **/	   
	   private FieldInformation buildReturnOrdinamentoStructureAnonymousFieldCategoArCm(ArCmCatego arCmCatego,FieldInformation fi,String nomeCampo,String tipoOrdinamento) throws Exception {		       		    	   		    	   
		    	   String sNomeTabOrView;
		    	   Vector fieldInfo;
		    	   
		    	   if (arCmCatego.getAr()!=null)
		    		   sNomeTabOrView=arCmCatego.getTab_ArCm();
		    	   else
		    		   sNomeTabOrView=arCmCatego.getView_Catego();
		    	   
		    	   try { 
		    		 fieldInfo = (new LookUpDMTable(vu)).lookUpInfoCampoOrizontalTable(nomeCampo,sNomeTabOrView);
		    	   		    	   
			    	 //Il campo è stato trovato! quindi inserisco il riferimento (indice del vettore di ordinamento)
			    	 //nella struttura di arcm e metto il campo in lista ordinamento/return (se non era già presente			    	
			    	 if (fieldInfo!=null) {
			    		 //Il campo era già stato trovato quindi devo solo mettere il riferimento
			    		 if (fi!=null) {	
			    			 
			   		   		 arCmCatego.getFieldIndexOrdinamento().put(""+(vOrdRet.size() - 1),sNomeTabOrView);
			   		   		 //Controllo la congruità dei tipi
			   		   		 if (!fi.getTipo().equals(((FieldInformation)fieldInfo.get(0)).getTipo()))
			   		   			throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructureAnonymousFieldCategoArCm "+
				   						 "- Il campo ("+nomeCampo+") inserito come ordinamento esiste in più tabelle ma è in esso definito"+
				   						 " in maniera ambigua ");			   		   		 			   		   		
			    		 }
			    		 else {
			    			 ((FieldInformation)fieldInfo.get(0)).setCampoReturn(true);
			   		   		 ((FieldInformation)fieldInfo.get(0)).setTipoOrdinamento(tipoOrdinamento);
			   		   		 ((FieldInformation)fieldInfo.get(0)).setNomeCampo(nomeCampo);
			   		   		 //In questo modo capisco che si tratta di un campo anonimo
			   		   		 ((FieldInformation)fieldInfo.get(0)).setTableViewName(null);
			   		   		 fi = ((FieldInformation)fieldInfo.get(0));
			   		   		 vOrdRet.add(fi);
			   		   		 addToAliasCampiReturn(nomeCampo,_ALIAS_RET_FIELD+(vOrdRet.size()),arCmCatego.getAr(),arCmCatego.getCm(),arCmCatego.getCatego());
			   		   		 arCmCatego.getFieldIndexOrdinamento().put(""+(vOrdRet.size() - 1),sNomeTabOrView);
			    		 }
			    	 }
			    	 
			    	 return fi;
			       }
			       catch (Exception e) {
				     throw new Exception("HorizontalSearch::buildReturnOrdinamentoStructureAnonymousFieldCategoArCm "+
				   						 "- Errore nel recupero delle informazioni per il campo ("+nomeCampo+") (CASO JOIN)"+
				   						 "\nErrore: "+e.getMessage());
				   }
	   }

	   /**
	    * Crea la frase di select con tutte le union fra i vari casi (JOIN/ARCM-CATEGO)
	   **/		   
	   private String createSQL() {
		       StringBuffer sSelect        = new StringBuffer("");
		       StringBuffer sSelectInterna = new StringBuffer("");
		       
		       String sRet="",sOrder="", sRetEsternaLimit="";
		       for(int i=0;i<vOrdRet.size();i++) {		    	   		    	   
		    	   String sFormatoData;
		    	   
		    	   if (((FieldInformation)vOrdRet.get(i)).getTipo().equals(_DATE_FIELD_TYPE)) {
		    	       sFormatoData=((FieldInformation)vOrdRet.get(i)).getFormatoData();
		    	       if (sFormatoData==null)
		    	    	   sFormatoData="dd/mm/yyyy";
		    	       else
		    	    	   sFormatoData=sFormatoData.replaceAll("HH:","hh24:");
		    	   }
		    	   else
		    		   sFormatoData="X";
		    	   		    	  
		    	   if (!sFormatoData.equals("X"))
		    		    sRet+=",TO_CHAR("+_ALIAS_RET_FIELD+(i+1)+",'"+sFormatoData+"') "+_ALIAS_RET_FIELD+(i+1)+" ";		    		   		    	   
		    	   else
		    			sRet+=","+_ALIAS_RET_FIELD+(i+1)+" ";

				   sRetEsternaLimit+=","+_ALIAS_RET_FIELD+(i+1)+" ";
		    		
		    	   if (((FieldInformation)vOrdRet.get(i)).getTipoOrdinamento()!=null) {
		    		   if (((FieldInformation)vOrdRet.get(i)).getTipo().equals(_DATE_FIELD_TYPE))
		    			   sOrder+="TO_DATE("+_ALIAS_RET_FIELD+(i+1)+",'"+sFormatoData+"') "+((FieldInformation)vOrdRet.get(i)).getTipoOrdinamento()+",";
		    		   else if (((FieldInformation)vOrdRet.get(i)).getTipo().equals(_NUMBER_FIELD_TYPE))
		    			   sOrder+="TO_NUMBER("+_ALIAS_RET_FIELD+(i+1)+") "+((FieldInformation)vOrdRet.get(i)).getTipoOrdinamento()+",";
		    		   else
		    			   sOrder+=_ALIAS_RET_FIELD+(i+1)+" "+((FieldInformation)vOrdRet.get(i)).getTipoOrdinamento()+",";
		    	   }
		       }
		       		       
		       sOrder+=" da DESC";

		       if (queryServiceLimit >0) {
				   sSelect.append("SELECT ID, ti, da, cr "+sRetEsternaLimit);
				   sSelect.append(" FROM ( ");
			   }
		        
		       sSelect.append("SELECT ID, ti, da, cr ");
		       sSelect.append(sRet);
		       
		       sSelect.append("FROM ( ");
		       
		       if (vJoin.size()!=0)
		    	   sSelectInterna.append(createSQLJoinSelect());
		       
		       if (vArCm.size()!=0) {
			       Vector vSelectARCM = createArCmCategoSelect(_ARCM);
			       int i=0;
			       for(;i<vSelectARCM.size();i++) {
			    	   if (i>0 || !sSelectInterna.toString().equals(""))
			    		   sSelectInterna.append(" UNION ");
			    	   sSelectInterna.append(vSelectARCM.get(i));				    	 
			       }
			       
			       if (i==1 && (!extraConditionSearch.equals("")) ) {
			    	   sSelectInterna.append(" AND "+extraConditionSearch+" "); 
			       }
		       }
		       
		       if (vCatego.size()!=0) {
			       Vector vSelectCatego = createArCmCategoSelect(_CATEGO);		       
			       for(int i=0;i<vSelectCatego.size();i++){	
			    	   if (i>0 || !sSelectInterna.toString().equals(""))
			    		    sSelectInterna.append(" UNION ");
			    	   sSelectInterna.append(vSelectCatego.get(i));					    	  
			       }
		       }
		       
		       if (!sSelectInterna.toString().equals("")) {
		           sSelect.append(sSelectInterna);
		           if (!vu.getByPassCompetenze()) sSelect.append(" UNION ");
		       }
		       if (!vu.getByPassCompetenze()) {	
		    	   sSelect.append(createDualSelect());
		       
		    	   sSelect.append(" ) A, DUAL ");
		       }
		       else {
		    	   sSelect.append(" ) A ");
		       }
		    	   
		       
		       if (!vu.getByPassCompetenze()) {
		    	   sSelect.append("  WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',");
		    	   sSelect.append("  								    A.ID,");
		    	   sSelect.append("  								    '"+controlloCompetenzaQuery+"',");
		    	   sSelect.append("  								    '"+vu.getUser()+"',");
		    	   sSelect.append("  								    F_TRASLA_RUOLO('"+vu.getUser()+"','GDMWEB','GDMWEB'),");
		    	   sSelect.append("  								    TO_CHAR(SYSDATE,'dd/mm/yyyy')");
			   	   if (bControllaPadre)
			   	   sSelect.append("                                    ,'Y'");
			   	   sSelect.append("                                    )||DUMMY = '1X' ");
			   	   sSelect.append("");			   	   			   	   
		   	   }
		   	   else
		   		   sSelect.append(" WHERE ID IS NOT NULL ");
		       
		       if (!sFullTextWarea.equals("")) sSelect.append(" AND " + sFullTextWarea); 
		       
		       if (bIsMaster && (!sCatMaster.equals(""))) {
		    	   sSelect.append(" AND F_DOCUMENTO_IN_CATEGORIA(A.ID,'"+Global.replaceAll(sCatMaster, "'", "''")+"') <> 0 "); 
		       }
		       
		       if (!bEscludiOrdinamento)
		    	   sSelect.append(" ORDER BY "+sOrder);

			   if (queryServiceLimit >0) {
				   sSelect.append(" ) ");
				   sSelect.append(" WHERE ROWNUM <= "+queryServiceLimit);
			   }

		       return sSelect.toString();
	   }
	   
	   /**
	    * Crea la frase di select quando viene passato direttamente l'id Documento
	   **/		   
	   private String createSQLidDoc() {
		       StringBuffer sSelect        = new StringBuffer("");		       
		       String nomeTab;
		       
		       /*try {
 			     nomeTab=""+(new LookUpDMTable(vu)).lookUpNomeTabellaOrizontaleByIdDoc(idDoc);
   			   }
   			   catch (Exception e) {
   				 nomeTab="";
   			   }*/		       		       
		       
   			   //Per adesso non utilizzo la versione con tabella orizzontale, forzo ad utilizzo della F_VALORE_CAMPO
   			   nomeTab="";
		       
   			   sSelect.append("SELECT ID, ti, da, cr ");
		       for(int i=0;i<campiOrdinamento.size();i++)
		    	   sSelect.append(","+_ALIAS_RET_FIELD+(i+1)+" ");
		       
		       sSelect.append("FROM ( ");
		       
		       sSelect.append("SELECT DOCU.ID_DOCUMENTO ID, DOCU.ID_TIPODOC TI,");
			   sSelect.append("DOCU.DATA_AGGIORNAMENTO DA, DOCU.CODICE_RICHIESTA CR ");			   
			   for(int i=0;i<campiOrdinamento.size();i++) {
				   String sCampo=((keyval)campiOrdinamento.get(i)).getKey();
				   sCampo=sCampo.substring(0,sCampo.indexOf("@"));
				   if (nomeTab.equals(""))
					   sSelect.append(",F_VALORE_CAMPO(DOCU.ID_DOCUMENTO,'"+sCampo+"') "+_ALIAS_RET_FIELD+(i+1)+" ");	
				   else 
					   sSelect.append(",TO_CHAR("+nomeTab+"."+sCampo+") "+_ALIAS_RET_FIELD+(i+1)+" ");
				   
				   addToAliasCampiReturn(sCampo,_ALIAS_RET_FIELD+(i+1),
						                 ((keyval)campiOrdinamento.get(i)).getArea(),
						                 ((keyval)campiOrdinamento.get(i)).getCm(),
						                 ((keyval)campiOrdinamento.get(i)).getCategoria());
			   }
			   
			   sSelect.append("FROM DOCUMENTI DOCU ");
			   
			   if (!nomeTab.equals("")) sSelect.append(","+nomeTab+" ");
			   
		   	   if (idDoc!=null) {
		   		   if (idDoc.size()>0) {
			   		   if (idDoc.size()==1)
			   			   sSelect.append(" WHERE DOCU.ID_DOCUMENTO="+idDoc.get(0)+" ");
			   		   else
			   			  // sSelect.append(" WHERE DOCU.ID_DOCUMENTO in "+generaListaId()+" ");
			   			    sSelect.append(" WHERE "+getSequenzaBlocchi(idDoc)+" ");
		   		   }	   
		   	   }
			   
			   if (!nomeTab.equals("")) sSelect.append(" AND DOCU.ID_DOCUMENTO = "+nomeTab+".ID_DOCUMENTO");
			   
			   sSelect.append(" AND DOCU.STATO_DOCUMENTO NOT IN "+stateDocNotIn);
			   
			   if (!vu.getByPassCompetenze()) {	
				   sSelect.append(" UNION ");
			       
				   sSelect.append("SELECT "+_TONUMBER_NULL+" ID, "+_TONUMBER_NULL+" ti, "+_TODATE_NULL+" da, "+_TOCHAR_NULL+" cr ");
				   for(int i=0;i<campiOrdinamento.size();i++)		    	  
			    	    sSelect.append(",TO_CHAR(NULL) "+_ALIAS_RET_FIELD+(i+1)+" ");
			       
				   sSelect.append("FROM DUAL ");
			       
				   sSelect.append(") A, DUAL ");
			   }
			   else
				   sSelect.append(") A ");
			   
		       if (!vu.getByPassCompetenze()) {		    	       	   		    	   
		    	   sSelect.append("  WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',");
		    	   sSelect.append("  								    A.ID,");
		    	   sSelect.append("  								    '"+controlloCompetenzaQuery+"',");
		    	   sSelect.append("  								    '"+vu.getUser()+"',");
		    	   sSelect.append("  								    F_TRASLA_RUOLO('"+vu.getUser()+"','GDMWEB','GDMWEB'),");
		    	   sSelect.append("  								    TO_CHAR(SYSDATE,'dd/mm/yyyy')");
			   	   if (bControllaPadre)
			   	   sSelect.append("                                    ,'Y'");
			   	   sSelect.append("                                    )||DUMMY = '1X' ");
			   	   sSelect.append("");			   	   
		   	   }
		   	   else
		   		   sSelect.append(" WHERE ID IS NOT NULL ");		       
		       
		       return sSelect.toString();
	   }

	   /**
	    * Crea la frase di select per i JOIN
	   **/	   
	   private String createSQLJoinSelect() {
		       StringBuffer sSql     = new StringBuffer("");
		       StringBuffer sSelect  = new StringBuffer("SELECT ");
		       StringBuffer sFrom    = new StringBuffer("FROM DOCUMENTI DOCU");
		       StringBuffer sWhere   = new StringBuffer("");
		       
		       /*
		        * HASH MAP CHE SERVE AD INGLOBARE 
		        * LE TABELLE/VISTE MESSE GIA' NELLA
		        * FROM, PER EVITARE DI PRENDERLE 
		        * PIU' DI UNA VOLTA 
		       */
		       HashMap hFrom  = new HashMap();
		       /*
		        * HASH MAP CHE SERVE AD INGLOBARE 
		        * LE WHERE CONDITION MESSE GIA' NELLA
		        * FROM, PER EVITARE DI PRENDERLE 
		        * PIU' DI UNA VOLTA 
		       */		       
		       HashMap hWhere = new HashMap();
		       
		       if (!bIsMaster) {
		    	   sSelect.append("DOCU.ID_DOCUMENTO ID, DOCU.ID_TIPODOC TI,");
			       sSelect.append("DOCU.DATA_AGGIORNAMENTO DA, DOCU.CODICE_RICHIESTA CR");			      
		       }
		       else {
		    	   sSelect.append("DOCU.ID_DOCUMENTO_PADRE ID, DOCUMASTER.ID_TIPODOC TI,");
			       sSelect.append("DOCUMASTER.DATA_AGGIORNAMENTO DA, DOCUMASTER.CODICE_RICHIESTA CR");	
			       sFrom.append(",DOCUMENTI DOCUMASTER");
		       }
			       
		       		       
		       //Ciclo sul vettore di JOIN per la parte di from e where
		       for(int i=0,indexAlias=1;i<vJoin.size();i++,indexAlias++) {
		    	   Join j = (Join)vJoin.get(i);
		    	   
		    	   String sTabViewLeft,sTabViewRight;
		    	   
		    	   if (j.getTab_ArCmLeft()==null) {
		    		   if (i==0) {
		    		       sWhere  = new StringBuffer("WHERE "+j.getView_CategoLeft()+".ID_DOCUMENTO = DOCU.ID_DOCUMENTO AND DOCU.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" AND ");
		    		       if (bIsMaster) 
		    		    	   sWhere.append("DOCUMASTER.ID_DOCUMENTO = DOCU.ID_DOCUMENTO_PADRE AND DOCUMASTER.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" AND ");
		    		   	}
		    		   sTabViewLeft=j.getView_CategoLeft();		    		 
		    	   }		    		   
		    	   else {
		    		   if (i==0) { 
		    		       sWhere  = new StringBuffer("WHERE "+j.getTab_ArCmLeft()+".ID_DOCUMENTO = DOCU.ID_DOCUMENTO AND DOCU.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" AND ");
		    		       if (bIsMaster) 
		    		    	   sWhere.append("DOCUMASTER.ID_DOCUMENTO = DOCU.ID_DOCUMENTO_PADRE AND DOCUMASTER.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" AND ");		    		       		    		       
		    		   }		    		       
		    		   sTabViewLeft=j.getTab_ArCmLeft();		    		   
		    	   }
		    	   
		    	   if (!hFrom.containsKey(sTabViewLeft)) {
		    		   hFrom.put(sTabViewLeft,sTabViewLeft);
		    		   sFrom.append(","+sTabViewLeft);		    		   
		    	   }   		    	   
		    	   		    	   
		    	   indexAlias++;
		    	   
		    	   if (j.getTab_ArCmRight()==null) 		    		  
		    		   sTabViewRight=j.getView_CategoRight();		    	   
		    	   else 		    		 
		    		   sTabViewRight=j.getTab_ArCmRight();
		    	   		    	   
		    	   if (!hFrom.containsKey(sTabViewRight)) {
		    		   hFrom.put(sTabViewRight,sTabViewRight);
		    		   sFrom.append(","+sTabViewRight);		    		   
		    	   }  		    	   		    	   		    	   
		    	   
		    	   //Aggiungo la condizione di JOIN (WHERE)
		    	   String sAnd="";
		    	   if (i>0) sAnd=" AND ";
		    	   
		    	   sWhere.append(sAnd+sTabViewLeft+"."+j.getJoinFieldLeft()
		    			             +"="
		    			             +sTabViewRight+"."+j.getJoinFieldRight());
		    	   
		    	   //Aggiungo le condizioni degli altri campi
		    	   Vector vField=j.getField();
		    	   for(int indexField=0;indexField<vField.size();indexField++) {		    		   
		    		   FieldInformation f = (FieldInformation)vField.get(indexField);

		    		   String sWhereCampo=f.getFieldSearchCondition(bIsRicercaPuntualeGlobale);
		    		   if (!hWhere.containsKey(sWhereCampo)) {
		    			   hWhere.put(sWhereCampo,sWhereCampo);
		    			   sWhere.append(" AND ");
		    			   sWhere.append(sWhereCampo);
		    		   }		    		    		    		    		    		   		    		   
		    	   }
		    	   
		    	   String sFullText="";
		    	   //Aggiungo le condizioni di FULL TEXT (Se ce ne sono)
		    	   if (condAnd!=null || condOr!=null) 
		    		   sFullText=vu.Global.replaceAll(protectReserveWord(calcolaCondizoneFullText()),"'","''");
		    	   
		    	   if (condAnd!=null || condOr!=null) {
		    		   if (bIsMaster) {
		    			   sWhere.append(" AND f_filtro_fulltext_warea(documaster.id_documento,'"+sFullText+"')>0 ");
		    		   }
		    		   else {
			    		   sWhere.append("  AND CONTAINS("+sTabViewLeft+".FULL_TEXT,'"+sFullText+"')>0 ");
			    		   sWhere.append("  AND CONTAINS("+sTabViewRight+".FULL_TEXT,'"+sFullText+"')>0 ");
		    		   }
		    	   }
		    	   
		    	   
		       }		      		       
		       //Fine parte di from e where
		       		       
		       //Ciclo sugli ogfi inseriti per questo join
		       if (fileJoinCondition.size()>0) {
		    	   sFrom.append(",oggetti_file ogfi ");
		    	    if (bIsMaster)
		    	    	sWhere.append(" AND ogfi.ID_DOCUMENTO=DOCUMASTER.ID_DOCUMENTO ");
		    	    else
		    	    	sWhere.append(" AND ogfi.ID_DOCUMENTO=DOCU.ID_DOCUMENTO ");
		       }
		       
    		   for(int indexField=0;indexField<fileJoinCondition.size();indexField++) {
    			   String sText=(String)fileJoinCondition.get(indexField);
    			   String testo = vu.Global.replaceAll(protectReserveWord(sText),"'","''");    			   
    			   sWhere.append(" AND decode(nvl(OGFI.path_file,' '),' ',CONTAINS(OGFI.TESTOOCR,'"+testo+"'),CONTAINS(OGFI.\"FILE\",'"+testo+"'))>0 ");
    		   }		       
		       
		       //Aggiungo alla SELECT i campi di ritorno
		       sSelect.append(createReturnFieldPhrase_JoinSelect());
		       
		       sSql.append(sSelect.toString());
		       sSql.append(" ");
		       sSql.append(sFrom.toString());
		       sSql.append(" ");
		       sSql.append(sWhere.toString());	       		      
		       
		       return sSql.toString();
	   }
	   
	   /**
	    * Crea la parte di frase SELECT per i campi di ritorno
	    * da mettere nella parte JOIN
	   **/		   
	   private String createReturnFieldPhrase_JoinSelect() {
		       StringBuffer sSelect = new StringBuffer("");
		       
		       for(int i=0;i<vOrdRet.size();i++) {
		    	   FieldInformation fi = (FieldInformation)vOrdRet.get(i);
		    	   
		    	   if (bIsMaster) {
		    		   //Se è di tipo master uso la F_VALORE_CAMPO del master per ottenere il valore
			    	   sSelect.append(",F_VALORE_CAMPO(DOCUMASTER.ID_DOCUMENTO,'"+fi.getNomeCampo()+"') "+_ALIAS_RET_FIELD+(i+1));
		    	   }
		    	   else {
			    	   //E' un campo NON ANONIMO
			    	   if (fi.getTableViewName()!=null) {
			    		   String sListaTabelleViste=getJoinTableView_List();
			    		   
			    		   //Trovata la tabella/vista del campo fra la lista di quelle in JOIN
			    		   if (sListaTabelleViste.indexOf("'"+fi.getTableViewName()+"'")!=-1)
			    			   //Aggiungo il campo fra la lista dei return/order dandogli un alias
			    			   sSelect.append(","+fi.getTableViewName()+"."+fi.getNomeCampo()+" "+_ALIAS_RET_FIELD+(i+1));
			    		   else
			    			   //Aggiungo il campo FITTIZIO fra la lista dei return/order dandogli un alias
			    			   sSelect.append(","+_TO_X_NULL(fi.getTipo())+" "+_ALIAS_RET_FIELD+(i+1));
			    	   }
			    	   //E' un campo ANONIMO
			    	   else {
			    		   //Mi scorro il vettore dei JOIN per capire se in una delle hashmap
			    		   //contenute c'è l'indice del vettore di ordinamento.
			    		   //Se esiste allora il campo deve essere inserito
			    		   String tableViewName=isIndexReturnOrderField_InJoinVector(i);
			    		   
			    		   //Trovata la tabella/vista del campo fra la lista di quelle in JOIN
			    		   if (tableViewName!=null)
			    			   //Aggiungo il campo fra la lista dei return/order dandogli un alias
			    			   sSelect.append(","+tableViewName+"."+fi.getNomeCampo()+" "+_ALIAS_RET_FIELD+(i+1));
			    		   else
			    			   //Aggiungo il campo FITTIZIO fra la lista dei return/order dandogli un alias
			    			   sSelect.append(","+_TO_X_NULL(fi.getTipo())+" "+_ALIAS_RET_FIELD+(i+1));		    			   
			    	   }
		    	   }
		       }
		       
		       return sSelect.toString();
	   }
	   
	   /**
	    * Crea le frasi di select per gli ARCM o CATEGO (a seconda del parametro)
	   **/	   
	   private Vector createArCmCategoSelect(int tipo) {
		       Vector vSql          = new Vector();		       
		       StringBuffer sFrom;
		       StringBuffer sWhere;		   
		       boolean bIsOggettiFile;
		       
		       Vector vApp;
		       if (tipo==_ARCM) 
			      vApp=vArCm;			   
			   else 
			      vApp=vCatego;	   
			   			       		     
		       for(int i=0;i<vApp.size();i++) {
		    	   bIsOggettiFile=false;
		    	   ArCmCatego arcm = (ArCmCatego)vApp.get(i);
		    	   String sTabView="";
		    	   
		    	   StringBuffer sSelect = new StringBuffer("SELECT ");
		    	   
			       if (!bIsMaster) {
			    	   sSelect.append("DOCU.ID_DOCUMENTO ID, DOCU.ID_TIPODOC TI,");
				       sSelect.append("DOCU.DATA_AGGIORNAMENTO DA, DOCU.CODICE_RICHIESTA CR");			      
			       }
			       else {
			    	   sSelect.append("DOCU.ID_DOCUMENTO_PADRE ID, DOCUMASTER.ID_TIPODOC TI,");
				       sSelect.append("DOCUMASTER.DATA_AGGIORNAMENTO DA, DOCUMASTER.CODICE_RICHIESTA CR");					      
			       }
		    	   
		    	   sFrom   = new StringBuffer("FROM DOCUMENTI DOCU,");
		    	   if (bIsMaster)  sFrom.append("DOCUMENTI DOCUMASTER,");
		    	   
		    	   if (arcm.getFileCondition()!=null && arcm.getFileCondition().size()>0) {
		    		   bIsOggettiFile=true;
		    		   sFrom.append("OGGETTI_FILE OGFI,");
		    	   }
		    	   
		    	   if (tipo==_ARCM) 
		    		   sWhere  = new StringBuffer("WHERE "+arcm.getTab_ArCm()+".ID_DOCUMENTO = DOCU.ID_DOCUMENTO AND DOCU.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" ");		    		   		    	   
		    	   else 
		    		   sWhere  = new StringBuffer("WHERE "+arcm.getView_Catego()+".ID_DOCUMENTO = DOCU.ID_DOCUMENTO AND DOCU.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" ");
		    	   
		    	   if (bIsOggettiFile) {
		    		   if (bIsMaster) 
		    			   sWhere.append(" AND OGFI.ID_DOCUMENTO=DOCUMASTER.ID_DOCUMENTO ");
		    		   else
		    			   sWhere.append(" AND OGFI.ID_DOCUMENTO=DOCU.ID_DOCUMENTO ");
		    	   }
		    		   
		    	   
		    	   if (bIsMaster) 
		    		   sWhere.append(" AND DOCUMASTER.ID_DOCUMENTO = DOCU.ID_DOCUMENTO_PADRE AND DOCUMASTER.STATO_DOCUMENTO NOT IN "+stateDocNotIn+" ");
			       
		    	   if (tipo==_ARCM) {
		    		   sFrom.append(arcm.getTab_ArCm());
		    		   sTabView=arcm.getTab_ArCm();
		    	   }		    	       
		    	   else {
		    		   sFrom.append(arcm.getView_Catego());
		    		   sTabView=arcm.getView_Catego();
		    	   }
		    	   
		    	   //Aggiungo le condizioni di FULL TEXT (Se ce ne sono)
		    	   if (condAnd!=null || condOr!=null) {
		    		   String sFullText;
		    		   sFullText=vu.Global.replaceAll(protectReserveWord(calcolaCondizoneFullText()),"'","''");
		    		   if (bIsMaster)
		    		      sWhere.append(" AND f_filtro_fulltext_warea(documaster.id_documento,'"+sFullText+"')>0 ");
		    		   else
		    		      sWhere.append(" AND CONTAINS("+sTabView+".FULL_TEXT,'"+sFullText+"')>0 ");		    		   
		    	   }		    	   
		    	   
		    	   sSelect.append(createReturnFieldPhrase_ArCmCategoSelect(tipo,i));		    	   
		    	   
		    	   //Aggiungo le condizioni degli altri campi
		    	   Vector vField=arcm.getField();		    	   		    	   
		    	   
		    	   for(int indexField=0;indexField<vField.size();indexField++) {
		    		   FieldInformation f = (FieldInformation)vField.get(indexField);
	    		   
		    		   sWhere.append(" AND ");
		    		   
		    		   sWhere.append(f.getFieldSearchCondition(bIsRicercaPuntualeGlobale)); 
		    	   }
		    	   		    	   
		    	   if (bIsOggettiFile) {
		    		   Vector<ObjFileConditionStruct> vObjFile = arcm.getFileCondition();
		    		   for(int indexField=0;indexField<vObjFile.size();indexField++) {
		    			   ObjFileConditionStruct objS = vObjFile.get(indexField);
		    			   String sText=objS.getTextSearch();
		    			   String testo = vu.Global.replaceAll(protectReserveWord(sText),"'","''");
		    			   
		    			   String sCondition="";
		    			   
		    			   if (vObjFile.get(indexField).isOcr())
		    				   sCondition=" AND CONTAINS(OGFI.OCR_FILE,'"+testo+"')>0 ";
		    			   else
		    				   sCondition=" AND decode(nvl(OGFI.path_file,' '),' ',CONTAINS(OGFI.TESTOOCR,'"+testo+"'),CONTAINS(OGFI.\"FILE\",'"+testo+"'))>0 ";
		    			   
		    			   if (sWhere.toString().indexOf(sCondition)==-1)
		    				   sWhere.append(sCondition);
		    		   }
		    	   }		    	   		    	  		    	   
		    	   
		    	   vSql.add(sSelect.toString()+" "+sFrom.toString()+" "+sWhere.toString());
		       }		       

		       
		       return vSql;
	   }
	   
	   /**
	    * Crea la parte di frase SELECT (di indice index) per i campi di ritorno
	    * da mettere nella parte ARCM o CATEGO (a seconda del parametro)
	   **/		   
	   private String createReturnFieldPhrase_ArCmCategoSelect(int tipo, int index) {
		       StringBuffer sSelect = new StringBuffer("");
		       String tableView;
		       ArCmCatego arcm;
		       
		       Vector vApp;
		       if (tipo==_ARCM) {
			      vApp=vArCm;
			      arcm = (ArCmCatego)vApp.get(index);
			      tableView=arcm.getTab_ArCm();
		       }
			   else {
			      vApp=vCatego;
			      arcm = (ArCmCatego)vApp.get(index);
			      tableView=arcm.getView_Catego();
			   }
		       		       		       
		       for(int i=0;i<vOrdRet.size();i++) {
		    	   FieldInformation fi = (FieldInformation)vOrdRet.get(i);
		    	   
		    	   if (bIsMaster) {
		    		   //Se è di tipo master uso la F_VALORE_CAMPO del master per ottenere il valore
			    	   sSelect.append(",F_VALORE_CAMPO(DOCUMASTER.ID_DOCUMENTO,'"+fi.getNomeCampo()+"') "+_ALIAS_RET_FIELD+(i+1));
		    	   }
		    	   else {
			    	   //E' un campo NON ANONIMO
			    	   if (fi.getTableViewName()!=null) {
			    		  //Trovata la tabella/vista del campo è in questa arcm o catego SELECT
			    		  if (fi.getTableViewName().equals(tableView)) 
			    			  //Aggiungo il campo fra la lista dei return/order dandogli un alias
			    			  sSelect.append(","+fi.getTableViewName()+"."+fi.getNomeCampo()+" "+_ALIAS_RET_FIELD+(i+1));
			    		  else
			    			  //Aggiungo il campo FITTIZIO fra la lista dei return/order dandogli un alias
			    			  sSelect.append(","+_TO_X_NULL(fi.getTipo())+" "+_ALIAS_RET_FIELD+(i+1));
			    	   }
			    	   //E' un campo ANONIMO
			    	   else {
			    		  //Controllo se nella hashmap di questa arcm o catego
			    		  //è presente l'indice di vOrdRet
			    		  if (arcm.getFieldIndexOrdinamento().containsKey(""+i)) 		    			  
			    			  //Aggiungo il campo fra la lista dei return/order dandogli un alias
			    			  sSelect.append(","+arcm.getFieldIndexOrdinamento().get(""+i)+"."+fi.getNomeCampo()+" "+_ALIAS_RET_FIELD+(i+1));
			    		  else 
			    			  //Aggiungo il campo FITTIZIO fra la lista dei return/order dandogli un alias
			    			  sSelect.append(","+_TO_X_NULL(fi.getTipo())+" "+_ALIAS_RET_FIELD+(i+1));
			    	   }
		    	   }
		       }
		       
		       return sSelect.toString();
	   }   
	   
	   /**
	    * Crea la frase standard di select from dual da appendere in fondo alle UNION
	   **/	   
	   private String createDualSelect() {
		       String sDualSelect;
		       
		       sDualSelect="SELECT "+_TONUMBER_NULL+" ID, "+_TONUMBER_NULL+" ti, "+_TODATE_NULL+" da, "+_TOCHAR_NULL+" cr";
		       for(int i=0;i<vOrdRet.size();i++)
		    	   if (bIsMaster) 		    	
		    		   //Se è di tipo master devo per forzare le cose a TO_CHAR(NULL) perché il valore
		    		   //viene restituito con la F_VALORE_CAMPO che torna sempre un VARCHAR
		    		   sDualSelect+=",TO_CHAR(NULL) "+_ALIAS_RET_FIELD+(i+1);
		    	   else
		    		   sDualSelect+=","+_TO_X_NULL(((FieldInformation)vOrdRet.get(i)).getTipo())+" "+_ALIAS_RET_FIELD+(i+1);
		       
		       sDualSelect+=" FROM DUAL";
		       
		       return sDualSelect;

	   }

	   /** ************************* METODI PRIVATI DI UTILITA' **************************** **/
	   
	   /**
	    * Elimino i campi markati con delete  
	   **/	 	   
	   private void removeMarkedFieldElements(int tipo) {
		   	   if (tipo==0) {
			       for(int i=campi.size();i>0;i--) {
	   		   		   keyval kToDelete = (keyval)campi.get(i-1);
	   		   		   
	   		   		   if (kToDelete.isMarkForDelete()) 
	   		   			   campi.remove(i-1);   		   			      		   		   
	   		   	   }
		   	   }
		   	   else {
			       for(int i=OjfiCondition.size();i>0;i--) {
	   		   		   keyval kToDelete = (keyval)OjfiCondition.get(i-1);
	   		   		   
	   		   		   if (kToDelete.isMarkForDelete()) 
	   		   			   OjfiCondition.remove(i-1);   		   			      		   		   
	   		   	   }		   		   
		   	   }
	   }
	   
	   /**
	    * Elimino gli ar/cm markati con delete  
	   **/	 	   
	   private void removeMarkedArCmElements() {
		       for(int i=vCm.size();i>0;i--) {
   		   		   String sCm = (String)vCm.get(i-1);   		   		  
   		   		   
   		   		   if (sCm.indexOf(_MARK_TO_DELETE)!=-1) {
   		   			   vCm.remove(i-1);
   		   			   vCmArea.remove(i-1);
   		   		   }
   		   	   }
	   }	   
	   
	   /**
	    * Restituisce la lista delle tabelle/viste messe in 
	    * JOIN nella forma ('TABELLA1','TABELLA2','VISTA1')
	    * pronte per essere messe in una IN.
	    * Se i JOIN sono vuoti restituisce null	    
	   **/		   
	   private String getJoinTableView_List() {
		       if (vJoin.size()==0) return null;
		       
		       String sRet="('";
		       
		       for(int i=0;i<vJoin.size();i++) {
		    	   Join j = (Join)vJoin.get(i);
		    	   
		    	   if (j.getTab_ArCmLeft()==null)
		    		   sRet+=j.getView_CategoLeft();
		    	   else
		    		   sRet+=j.getTab_ArCmLeft();
		    	   		    	   
		    	   if (j.getTab_ArCmRight()==null)
		    		   sRet+="','"+j.getView_CategoRight();
		    	   else
		    		   sRet+="','"+j.getTab_ArCmRight();
		    	   
		    	   if (i!=vJoin.size()-1) 
		    		   sRet+="','";
		    	   else
		    		   sRet+="')";
		       }
		       
		       return sRet;
	   }
	   
	   /**
	    * Inserisce il campo (di tipo fieldInformation)
	    * nella struttura (vettore) dei field del nodo
	    * del vettore di Join in funzione della chiave che
	    * li lega
	   **/			   
	   private void setField_Into_JoinVector(FieldInformation fi) {		      
		       for(int i=0;i<vJoin.size();i++) {
		    	   Join j = (Join)vJoin.get(i);
		    	   
		    	   //Controllo la parte sinistra
	    		   if (j.getTab_ArCmLeft()!=null && 
	    			   j.getTab_ArCmLeft().equals(fi.getTableViewName())) {	    			   
	    			   j.getField().add(fi);	    			   
	    			   continue;
	    		   }	  
	    		   if (j.getView_CategoLeft()!=null && 
	    			   j.getView_CategoLeft().equals(fi.getTableViewName())) {	    			  
	    			   j.getField().add(fi);	    			   
	    			   continue;
	    		   }	    		   
	    		   
	    		   //Controllo la parte destra
	    		   if (j.getTab_ArCmRight()!=null && 
	    			   j.getTab_ArCmRight().equals(fi.getTableViewName())) {	    			  
	    			   j.getField().add(fi);	    			   
	    			   continue;
	    		   }		    		   
		    		      		    	   		    	   
	    		   if (j.getView_CategoRight()!=null && 
	    			   j.getView_CategoRight().equals(fi.getTableViewName())) {	    			 
	    			   j.getField().add(fi);	    			   
	    			   continue;
	    		   }		    	   		    			  
		       }		       		      		    	  
	   }	  	  

	   /**
	    * Restituisce la tabella/vista dal vettore Join che contiene
	    * AR/CM oppure Catego passata.
	    * Rstituisce null se non viene trovato nulla
	   **/	   
	   private String getTabViewFromJoinVector(String area, String cm, String catego) {
		       String ret=null;
		       for(int i=0;i<vJoin.size();i++) {
		    	   Join j = (Join)vJoin.get(i);
		    	   
		    	   if (area!=null) {
		    		   if (j.getArLeft()!=null && 
		    			   j.getArLeft().equals(area) &&
		    			   j.getCmLeft().equals(cm))
		    			   return j.getTab_ArCmLeft();
		    		   if (j.getArRight()!=null && 
		    			   j.getArRight().equals(area) &&
		    			   j.getCmRight().equals(cm))
		    			   return j.getTab_ArCmRight();		    		   
		    	   }
		    	   else {
		    		   if (j.getCategoLeft()!=null && 
		    			   j.getCategoLeft().equals(catego))
		    			   return j.getView_CategoLeft();
		    		   if (j.getCategoRight()!=null && 
		    			   j.getCategoRight().equals(catego))
		    			   return j.getView_CategoRight();		    		   
		    	   }
		       }
		       
		       return ret;
	   }

	   /**
	    * Controlla se l'indice del vettore vOrdRet è presente
	    * in una delle hashmap del vettore vJoin.
	    * Se presente viene restituito il valore della
	    * hashmap (tabella o vista del campo di ordinamento/ritorno)
	    * Null altrimenti
	   **/		   
	   private String isIndexReturnOrderField_InJoinVector(int index) {
		       String sTabOrViewRet=null;
		       
		       for(int i=0;i<vJoin.size();i++) {
		    	   HashMap h = ((Join)vJoin.get(i)).getFieldIndexOrdinamento();
		    	   
		    	   if (h.containsKey(""+index))
		    		   return ""+h.get(""+index);
		       }
		       
		       return sTabOrViewRet;
	   }
	   
	   /**
	    * Restituisce l'indice del vettore vCatego se la categoria esiste, -1 se
	    * non viene trovata
	   **/	   
	   private int indexCategoInVector(String categoria) {
		       for(int i=0;i<vCatego.size();i++)
		    	   if (((ArCmCatego)vCatego.get(i)).getCatego().equals(categoria))
		    		   return i;
		       
		       return -1;
	   }
	   
	   /**
	    * Restituisce l'indice del vettore  se il cm esiste, -1 se
	    * non viene trovato
	   **/		   
	   private int indexCmInVector(String ar,String cm) {
		       for(int i=0;i<vCm.size();i++) {
   		   		   //Determino AR/CM
   		   		   String sArea;
   		   		   String sCm=(String)vCm.get(i);
   		   		   
   		   		   if (!(vCmArea.get(i).equals(""))) 
   		   			   sArea=(String)vCmArea.get(i);
   		   		   else
   		   			   sArea=area;
   		   		   
   		   		   if (ar.equals(sArea) && cm.equals(sCm)) return i;   		   		
		       }  
		       
		       return -1;
	   }
	   
	   /**
	    * A seconda del tipo oracle passato restituisce
	    * il corrispettivo NULL.
	   **/		   
	   private String _TO_X_NULL(String type) {
		       String sRet=null;
		       
		       if (type.equals(_CHAR_FIELD_TYPE) || type.equals(_CLOB_FIELD_TYPE)) {
		    	   sRet=_TOCHAR_NULL;
		       }
		       
		       if (type.equals(_NUMBER_FIELD_TYPE)) {
		    	   sRet=_TONUMBER_NULL;
		       }
		       
		       if (type.equals(_DATE_FIELD_TYPE)) {
		    	   sRet=_TODATE_NULL;
		       }		       		       
		       
		       return sRet;
	   }
	   
	   /**
	    * Aggiunge un keyval nella struttura del vettore di ritorno utilizzato
	    * dal resultSetIQuery.
	   **/
	   private void addToAliasCampiReturn(String nomeCampo, String aliasSelectCampo,
			                              String area, String cm, String catego) {
		       keyval kCampiReturn = new keyval();
			   kCampiReturn.setKey(nomeCampo);
			   kCampiReturn.setValue(aliasSelectCampo);
			   kCampiReturn.setArea(area);
			   kCampiReturn.setCm(cm);
			   kCampiReturn.setCategoria(catego);
				
			   kCampiReturn.valoriCursore = new Vector();
			   kCampiReturn.valoriTypeCursore = new Vector();
			   kCampiReturn.valoriSizeCursore = new Vector();
				
			   vAliasCampiReturn.add(kCampiReturn);
	   }
	   
	   private String protectReserveWord(String phrase) {
		   	   for(int i=0;i<reserveWord.length;i++) {
		   		   phrase=vu.Global.replaceAll(phrase,reserveWord[i],escapeCaracter+reserveWord[i]);
		   	   }
		   
		       return phrase;
	   }	   
	   
	   private String calcolaCondizoneFullText(){
		   	   StringBuffer sCondizioneFullText = new StringBuffer("");
		   	   String condizioneOrSistemata="",condizioneAndSistemata="";
		   	 
		   	   if (condAnd!=null && (!condAnd.equals("")) ) {
	   			  java.util.StringTokenizer s = new java.util.StringTokenizer(protectReserveWord(condAnd)," ");
	   			  condizioneAndSistemata="(" ;
	   			  while (s.hasMoreTokens()) {
	   				     condizioneAndSistemata+=s.nextElement();
            	         if (s.hasMoreTokens()) condizioneAndSistemata+=" AND ";
                  }
	   			 
	   			 sCondizioneFullText.append(condizioneAndSistemata+")");
	   		   }
   		  
	   		   /**** CALCOLO DELL'OR *****/
	   		   
	   		   if (condOr!=null) {
	   			 	   			 
	   			  condOr=condOr.trim();
                  
	   			  if (!condOr.equals("")) {
	                  java.util.StringTokenizer s = new java.util.StringTokenizer(protectReserveWord(condOr)," ");
	                 
	                  while (s.hasMoreTokens()) {
	                 	    condizioneOrSistemata+=s.nextElement();
	                	    if (s.hasMoreTokens()) condizioneOrSistemata+=" OR ";
	                  }
	                 
	                  if (!sCondizioneFullText.toString().equals(""))
	                	  sCondizioneFullText.append(" AND ");
	                 
	                  sCondizioneFullText.append("("+condizioneOrSistemata+")");
	   			  }
	   		   }
	   		   /**** FINE CALCOLO OR *****/
	   		   
		   	   return sCondizioneFullText.toString();		   		
	   }	   
	   
	   private String generaListaId() {
		       String sRet="(";
		      
		       for(int i=0;i<idDoc.size();i++) {
		    	  sRet+=""+idDoc.get(i);
		     	  
		    	  if (i != idDoc.size()-1)
		    		  sRet+=",";
		      }
		      
		      sRet+=")";
		      
		      return sRet;
	   }		   
	   
	   private String getSequenzaBlocchi(Vector v)
	   {
		       String seq="";
		       int count=0,max_list=1000,v_size,s;
		       
		       if (v.size()==0)
		    	return seq;
		    
		       v_size=v.size(); 
		       s=0;
		       
		       seq=" (  DOCU.id_documento IN ( ";  
		       		        
		       for(int i=0;i<v.size();i++)
		       {
		    	 if (count == max_list)
		         {
		              count=0;
		              s++;
		              v_size=v.size()-s*max_list;
		              seq+=" or DOCU.id_documento IN ( ";
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
		    
		       seq+=" ) ";
		       return seq;
	   }	   
	   
	   /**
	    * Ricerca la Stringa searchString all'interno di un vettore di 
	    * Stringhe vInput
	    * 
	    * @param vInput
	    * @param searchString
	    * @return True se la stringa esiste nel vettore, False altrimenti
	   */
	   private boolean isStringInVector(Vector vInput,String searchString) {		       
		       for(int i=0;i<vInput.size();i++) {
		    	   if (((String)vInput.get(i)).equals(searchString)) return true;
		       }
		       
		       return false;
	   }
	   
	   private String getOperatorKeyVal(keyval k) {
	       String op;
	       
	       if (k.getOperator()!=null) {
	    	   op=k.getOperator().toUpperCase();
		       if (op.equals(FieldInformation._OPERATOR_IS_NULL.toUpperCase()) || 
		       	   op.equals(FieldInformation._OPERATOR_IS_NOT_NULL.toUpperCase()) ||
		           op.equals(FieldInformation._OPERATOR_CONTAINS.toUpperCase()) ||
		           op.equals(FieldInformation._OPERATOR_EQUALS.toUpperCase()) ||
		           op.equals(FieldInformation._OPERATOR_NOT_EQUALS.toUpperCase()) ||
		           op.equals(FieldInformation._OPERATOR_NOT_GREATHEN.toUpperCase()) ||
		           op.equals(FieldInformation._OPERATOR_NOT_LESSTHEN.toUpperCase()) ||
		           op.equals(FieldInformation._OPERATOR_BETWEEN.toUpperCase()))		    	   		       
		    	   return k.getOperator();		       
	       }
	       
	       if (k.getTipoDaClient()!=null) {
	    	   if (k.getTipoDaClient().toUpperCase().equals(FieldInformation._OPERATOR_BETWEEN.toUpperCase()))
	    		   return k.getOperator();
	       	   return k.getTipoDaClient();
	       }
	       else
	    	  return k.getOperator(); 
       }
	   
	   /**
	    * Connessione al DB 
	   **/
	   private IDbOperationSQL connect() throws Exception {
	           if (vu.getDbOp()==null) {
	              bIsNew=true;
	              return (new ManageConnection(vu.Global)).connectToDB();
	           }
	        
	           return vu.getDbOp();
	   }
	  
	   /**
	    * Disconnessione dal DB 
	   **/	   
	   private void disconnect() throws Exception {
	           if (bIsNew) (new ManageConnection(vu.Global)).disconnectFromDB(dbOp,true,false);        
	   } 	   
	   
	   /**
	    * Vettore di Stringhe (Condizioni degli oggettiFile sui Join)
       **/	 		       
	   private Vector fileJoinCondition = new Vector();	
	   
	   /**
	    * Classe accessoria che serve a costruire la struttura dati per
	    * ottenere la SELECT di tipo JOIN   
	   **/	   
	   private class Join {
			   /**
			    * Queste quattro varibili sono piene a due
			    * a due in maniera esclusiva a seconda del tipo di
			    * join: ARCM con ARCM / ARCM con CATEGO / CATEGO con CATEGO
			    * 
			    * Nella varibile verrà scritta direttamente il nome della 
			    * TABELLA/VISTA orizzontale corrispondente ad ARCM o CATEGO,
			    * ad es: TAB_M_PROTOCOLLO oppure PROTO_VIEW 
			   **/		       
		       private String tab_ArCmLeft=null, tab_ArCmRight=null;
		       private String view_CategoLeft=null, view_CategoRight=null;		      
		       
			   /**
			    * Varibili che contengono ARCM/CATEGO.
			    * Accessorie rispetto ai tab_/view_ di cui sopra 
			   **/			       
		       private String arLeft=null, arRight=null;
		       private String cmLeft=null, cmRight=null;
		       private String categoLeft=null, categoRight=null;		  		       		      
		       
			   /**
			    * Campi coinvolti nel Join
			    * joinFieldLeft si riferisce a tab_ArCm1/view_Catego1
			    * joinFieldRight si riferisce a tab_ArCm2/view_Catego2
			   **/		       
		       private String joinFieldLeft=null, joinFieldRight=null;
		       
			   /**
			    * Vettore di campi di tipo fieldInformation.
			   **/	 		       
		       private Vector field;		       	       
		       
		       /**
			    * HashMap di indici
			    * Gli indici puntano a quei campi Ordinamento
			    * ANONIMI ma che hanno corrispondenza con
			    * uno dei AR/CM o CATEGO (left o right)
			    * 
			    * l'HASHMAP vettore sarà così composto:
			    * 3@TAB_M_PROTOCOLLO;5@TAB_M_SOGGETTO....etc....etc... 
			   **/	 		       
		       private HashMap fieldIndexOrdinamento;
		       
		       public Join() {
		    	      field                 = new Vector();	
		    	      fieldIndexOrdinamento = new HashMap();
		       }

			   /**
			    * Metodi di getter e setter
			   **/	
		       public String getArLeft() {
				  	  return arLeft;
			   }
	
			   public String getArRight() {
					  return arRight;
			   }
	
			   public String getCategoLeft() {
					  return categoLeft;
			   }
	
			   public String getCategoRight() {
					  return categoRight;
			   }
	
			   public String getCmLeft() {
					  return cmLeft;
			   }
	
			   public String getCmRight() {
					  return cmRight;
			   }
	
			   public Vector getField() {
					  return field;
			   }			   	  
	
			   public String getJoinFieldLeft() {
				  	  return joinFieldLeft;
			   }
	
			   public String getJoinFieldRight() {
					  return joinFieldRight;
			   }
	
			   public String getTab_ArCmLeft() {
					  return tab_ArCmLeft;
			   }
	
			   public String getTab_ArCmRight() {
					  return tab_ArCmRight;
			   }
	
			   public String getView_CategoLeft() {
					  return view_CategoLeft;
			   }
	
			   public String getView_CategoRight() {
					  return view_CategoRight;
			   }

			   public void setArLeft(String arLeft) {
					  this.arLeft = arLeft;
			   }
	
			   public void setArRight(String arRight) {
					  this.arRight = arRight;
			   }
	
			   public void setCategoLeft(String categoLeft) {
					  this.categoLeft = categoLeft;
			   }
	
			   public void setCategoRight(String categoRight) {
					  this.categoRight = categoRight;
			   }
	
			   public void setCmLeft(String cmLeft) {
				  	  this.cmLeft = cmLeft;
			   }
	
			   public void setCmRight(String cmRight) {
					  this.cmRight = cmRight;
			   }
	
			   public void setField(Vector field) {
					  this.field = field;
			   }		   
	
			   public void setJoinFieldLeft(String joinFieldLeft) {
					  this.joinFieldLeft = joinFieldLeft;
			   }
	
			   public void setJoinFieldRight(String joinFieldRight) {
					  this.joinFieldRight = joinFieldRight;
			   }
	
			   public void setTab_ArCmLeft(String tab_ArCmLeft) {
					  this.tab_ArCmLeft = tab_ArCmLeft;
			   }
	
			   public void setTab_ArCmRight(String tab_ArCmRight) {
					  this.tab_ArCmRight = tab_ArCmRight;
			   }
	
			   public void setView_CategoLeft(String view_CategoLeft) {
					  this.view_CategoLeft = view_CategoLeft;
			   }
	
			   public void setView_CategoRight(String view_CategoRight) {
					  this.view_CategoRight = view_CategoRight;
			   }			   

			   public HashMap getFieldIndexOrdinamento() {
					return fieldIndexOrdinamento;
			   }			   

			   public String toString() {
				      String joinInformation=null;
				      
				      // AR/CM - CATEGO
				      joinInformation =" -**************  Left   **************- \n";
				      joinInformation+=" Area = "+arLeft+" - Cm = "+cmLeft+" / Catego = "+categoLeft+"\n";
				      joinInformation+=" Tabella = "+tab_ArCmLeft+" / Vista = "+view_CategoLeft+"\n";				      
				      joinInformation+=" Campo = "+joinFieldLeft+"\n";
				      joinInformation+=" -**************  Right  **************- \n";
				      joinInformation+=" Area = "+arRight+" - Cm = "+cmRight+" / Catego = "+categoRight+"\n";
				      joinInformation+=" Tabella = "+tab_ArCmRight+" / Vista = "+view_CategoRight+"\n";
				      joinInformation+=" Campo = "+joinFieldRight+"\n";				      
				      // CAMPI
				      joinInformation+="\n\t_____________CAMPI____________\n\n";
				      for(int i=0;i<field.size();i++) {
				    	 joinInformation+="\t\t"+((FieldInformation)field.get(i)).toString(); 
				      }		
				      joinInformation+="\n\t_____________FINE CAMPI____________\n\n";
				      // INDICI ORDINAMENTO ANONIMO
				      joinInformation+="\n\t_____________INDICI HASHMAP ORDINAMENTO PER CAMPI ANONIMI____________\n\n";
				      for (Iterator it=fieldIndexOrdinamento.entrySet().iterator(); it.hasNext(); ) { 
				    	   Map.Entry entry = (Map.Entry)it.next(); 
				    	   Object key = entry.getKey(); 
				    	   Object value = entry.getValue(); 
				    	   joinInformation+="\t\t"+(String)key + "-" + (String)value; 
				      }			
				      joinInformation+="\n\t_____________FINE INDICI VETTORE____________\n\n";				      
				      
				      return joinInformation;
			   }

	   }
	   
	   /**
	    * Classe accessoria che serve a costruire la struttura dati per
	    * ottenere le SELECT di tipo ArCm o Catego
	   **/	   
	   private class ArCmCatego {	   
			   /**
			    * Queste due varibili sono piene
			    * in maniera esclusiva a seconda del tipo 
			    * oggetto: ARCM o CATEGO
			    * 
			    * Nella varibile verrà scritta direttamente il nome della 
			    * TABELLA/VISTA orizzontale corrispondente ad ARCM o CATEGO,
			    * ad es: TAB_M_PROTOCOLLO oppure PROTO_VIEW 
			   **/		       
		       private String tab_ArCm=null;
		       private String view_Catego=null;
		       
			   /**
			    * Varibili che contengono ARCM/CATEGO.
			    * Accessorie rispetto ai tab_/view_ di cui sopra 
			   **/			       
		       private String ar=null, cm=null;
		       private String catego=null;
		       
			   /**
			    * Vettore di campi di tipo fieldInformation.
			   **/	 		       
		       private Vector field;
		       
		       /**
			    * Vettore di Stringhe (Condizioni degli oggettiFile)
			   **/	 		       
		       private Vector<ObjFileConditionStruct> fileCondition;
		       
		       /**
			    * HashMap di indici
			    * Gli indici puntano a quei campi Ordinamento
			    * ANONIMI ma che hanno corrispondenza con
			    * uno dei AR/CM o CATEGO (left o right)
			    * 
			    * l'HASHMAP vettore sarà così composto:
			    * 3@TAB_M_PROTOCOLLO;5@TAB_M_SOGGETTO....etc....etc... 
			   **/	 		       
		       private HashMap fieldIndexOrdinamento;		       
		       
		       public ArCmCatego() {
		    	      field                 = new Vector();	
		    	      fileCondition         = new Vector<ObjFileConditionStruct>(); 
		    	      fieldIndexOrdinamento = new HashMap();
		       }

			   public String getAr() {
					  return ar;
			   }
	
			   public String getCatego() {
					  return catego;
			   }
	
			   public String getCm() {
					  return cm;
			   }
	
			   public Vector getField() {
					  return field;
			   }
			   
			   public Vector<ObjFileConditionStruct> getFileCondition() {
				      return fileCondition;
			   }			   
	
			   public String getTab_ArCm() {
					  return tab_ArCm;
			   }
	
			   public String getView_Catego() {
					  return view_Catego;
			   }
	
			   public void setAr(String ar) {
				  	  this.ar = ar;
			   }
	
			   public void setCatego(String catego) {
					  this.catego = catego;
			   }
	
			   public void setCm(String cm) {
					  this.cm = cm;
			   }
	
			   public void setField(Vector field) {
					  this.field = field;
			   }
			   
			   public void setFileCondition(Vector<ObjFileConditionStruct> fCond) {
				      this.fileCondition=fCond;
			   }
	
			   public void setTab_ArCm(String tab_ArCm) {
					  this.tab_ArCm = tab_ArCm;
			   }
	
			   public void setView_Catego(String view_Catego) {
					  this.view_Catego = view_Catego;
			   }
			   
			   public HashMap getFieldIndexOrdinamento() {
					return fieldIndexOrdinamento;
			   }				   
			   
			   public String toString() {
				      String joinInformation=null;
				      
				      // AR/CM
				      if (ar!=null) {
					      joinInformation =" -**************  Area/Cm   **************- \n";
					      joinInformation+=" Area = "+ar+" - Cm = "+cm+"\n";
					      joinInformation+=" Tabella = "+tab_ArCm+"\n";
					  }
				      // CATEGO
				      else {
					      joinInformation+=" -**************  Catego  **************- \n";
					      joinInformation+=" Catego = "+catego+"\n";
					      joinInformation+=" Vista = "+view_Catego+"\n";					      	
				      } 
				      // CAMPI
				      joinInformation+="\n\t_____________CAMPI____________\n\n";
				      for(int i=0;i<field.size();i++) {
				    	 joinInformation+="\t\t"+((FieldInformation)(field.get(i))).toString(); 				    
				      }		
				      joinInformation+="\n\t_____________FINE CAMPI____________\n\n";
				      // INDICI ORDINAMENTO ANONIMO
				      joinInformation+="\n\t_____________INDICI HASHMAP ORDINAMENTO PER CAMPI ANONIMI____________\n\n";
				      for (Iterator it=fieldIndexOrdinamento.entrySet().iterator(); it.hasNext(); ) { 
				    	   Map.Entry entry = (Map.Entry)it.next(); 
				    	   Object key = entry.getKey(); 
				    	   Object value = entry.getValue(); 
				    	   joinInformation+="\t\t"+(String)key + "-" + (String)value; 
				      }		
				      joinInformation+="\n\t_____________FINE INDICI VETTORE____________\n\n";				      
				      				      				      
				      return joinInformation;
			   }
		   
	   }

}

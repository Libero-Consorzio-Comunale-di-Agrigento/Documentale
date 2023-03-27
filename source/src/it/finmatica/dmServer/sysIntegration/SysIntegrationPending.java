package it.finmatica.dmServer.sysIntegration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.SysIntegrationDbOperation;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.sysIntegration.util.SysIntegrationConstant;
import it.finmatica.dmServer.sysIntegration.util.SysIntegrationXMLFieldDecode;
import it.finmatica.dmServer.util.Global;

public class SysIntegrationPending {
	   private String idDocumento = null;
	   private String typeIntegrationPending = null;	   
	   private Environment varEnv;
	   private Vector<SysIntegrationModel> sysModelList = new Vector<SysIntegrationModel>();
	   private SysIntegrationDbOperation sysDbOp;
	   private boolean bConnectInThisClass=false;
	   
	   /**
	    * Costruttore utilizzato esclusivamente 
	    * per poter lanciare la make su tutti
	    * gli item in pending del tipo di integrazione
	    * passato
	    * 
	   */
	   public SysIntegrationPending(String type,Environment en) throws Exception {
		      init(null,type,en);
	   }	
	
	   /**
	    * Costruttore utilizzato esclusivamente 
	    * per poter inserire in pending
	    * gli item relativi all'idDocumento
	    * passato in input per ogni tipo di integrazione
	    * 
	    * Se, per ogni tipo di integrazione, è prevista
	    * la sincronizzazione immediata, verrà lanciata
	    * la relativa make per tutti gli item
	    * dell'idDocumento-tipo di integrazione.
	    * 
	   */	   
	   public SysIntegrationPending(long idDoc,Environment en) throws Exception {		   	  
		      init(""+idDoc,null,en);
	   }
	   
	   /**
	    * Caso SINCRONO
	    * 
	    * 1. Inserisco tutti gli item per ar-cm-type (li inserisco in pending)
	    * 2. Lancio la make per ogni item trovato solo se il tipo di integrazione è sincrona
	    *    (altrimenti poi quelli inseriti verranno lasciati in pending e poi processati
	    *     dal metodo asincrono checkPending)
	    * 
	    * @throws Exception
	   */	   
	   public void insertPending() throws Exception {	
		      connect();
		   
		      try {		    	  
		    	initializeStruct();
		    	
		    	insert();
		      }
		      catch (Exception e) {
		    	try {closeRollback();}catch (Exception ei) {}
		    	throw (e);
		      }
		      
		      closeCommit();
	   }	   
	   
	   /**
	    * Caso ASINCRONO
	    * 
	    * 1. Ricerco tutti gli item pending per ar-cm-type
	    * 2. Lancio la make per ogni item trovato
	    * 
	    * @throws Exception
	   */	   
	   public void checkPending() throws Exception {
		      connect();
		      
		      try {		    	
		    	initializeStruct();
		    	
		    	check();
		      }
		      catch (Exception e) {
		    	try {closeRollback();}catch (Exception ei) {}
		    	throw (e);
		      }
		      
		      closeCommit();
	   }	   
	   

	   private void check() throws Exception {		 
		   	   if (typeIntegrationPending==null) return;
		   
		   	   //Se non ci sono integrazioni da fare...esco.
		   	   if (sysModelList.size()==0) return;
		   	   		   	   
		   	   //1. Ricerco tutti gli item pending per il tipo di integrazione richiesto
		   	   Vector<Object[]> vRet = sysDbOp.getPendingItemList(typeIntegrationPending);
		   	   
		   	   //2. Lancio la make per ogni item di ogni tipo di sysModel 		   	   
		   	   for(int i=0;i<vRet.size();i++) {
		   		   Object[] objInfo = vRet.get(i);
		   		   
		   		   String idDoc=(String)objInfo[0],xmlOther=(String)objInfo[1];
		   		   String area=(String)objInfo[2],cm=(String)objInfo[3];
		   		   String sIdentifierRemObject=(String)objInfo[4];
		   		   
		   		   SysIntegrationModel syMo = getSysIntModelFromArCmType(area,cm,typeIntegrationPending);
		   		   
		   		   if (syMo!=null) {
		   			   try {
			   			 executeMakeUpdatePending(syMo,idDoc,xmlOther,sIdentifierRemObject);
			   		   }
			   		   catch (Exception e) {
			   			 String sError="SysIntegrationPending::insertPending - Errore nel lancio make pending ASINCRONA per item: "+
		    		 		  		   "(idDoc,xmlotherkey) ("+idDoc+","+xmlOther+") - type ("+syMo.getSysIntType().getType()+")\n"+
		    		 		  		   e.getMessage();
			   			 				   			
		   				
			   			 throw new Exception(sError);
		   			   }	
		   		   }
		   	   }
	   }
	   

	   
	   private void insert() throws Exception {		   
		   	   if (idDocumento==null) return;
		   		
		   	   //Se non ci sono integrazioni da fare...esco.
		   	   if (sysModelList.size()==0) return;
		   	   		   	   
		   	   //TODO
		   	   //1. Inserisco i vari item per ogni sysModel caricato nella lista
		   	   //(controllando che l'item non ci sia già....se c'è lo si aggiorna->IN PENDING)
		   	   for(int i=0;i<sysModelList.size();i++) {
		   		   //Mi carico le regole (se ce n'è) extra (derivanti dal tipo di integrazione selezionata dal ciclo) per sapere cos'è un item in pending
		   		   Vector<String> vOtherKey = null;		   		   
		   		   
		   		   try {
		   			   SysIntegrationXMLFieldDecode sysXMLFieldDecode = new SysIntegrationXMLFieldDecode(varEnv);
		   			   vOtherKey=sysXMLFieldDecode.getXMLOptionOtherKey_From_XMLOtherKeyDocumentRule(sysModelList.get(i).getSysIntType().getXmlDocumentRule(),idDocumento);
		   		   }
		   		   catch (Exception e) {
		   			   throw new Exception("SysIntegrationPending::insertPending - Errore nella decodifica del campo XML (XML_OTHERKEY_DOCUMENT_RULE): "+
		   					   			    sysModelList.get(i).getSysIntType().getXmlDocumentRule()+"\n"+e.getMessage());
		   		   }
		   		   
		   		   if (vOtherKey==null) vOtherKey = new Vector<String>();
		   		   
		   		   vOtherKey.add(""); //Stringa vuota significa mettere null sulla XML_OPTION_OTHERKEY della PENDING TABLE, ovvero
		   		   					  //significa inserire SEMPRE ALMENO il record di testa item=id_documento (senza allegato-chiave esempio)
		   		   		   		   
		   		   for(int j=0;j<vOtherKey.size();j++) {
		   			   //TODO
		   			   //Adesso inserisco un record sulla pending (se non c'è già) per ogni elemento della vOtherKey
		   			   //e dentro il metodo di inserimento, se il log è Y inserire anche il record di LOG		 
		   			   //OKKKIO---> se la chiave TYPE_INTEGRATION-ID_DOCUMENTO-elemento della vOtherKey esiste
		   			   //già sulla pending, si fa update aggiornado le date ed inserendo altro record di log
		   			   //SOLO se lancerò la 2. (make) Ovvero solo se il tipo di integrazione è sincrona.
		   			   boolean bTest=false;
		   			   String sIdentifierRemObject=null;
		   			   
		   			   try {
		   				 String retParm[] = new String[1];
		   				 bTest=sysDbOp.checkSysIntegrationPending(sysModelList.get(i).getSysIntType().getType(),
		   						   							      idDocumento,vOtherKey.get(j),retParm);
		   				
		   				 sIdentifierRemObject=retParm[0];		   				 
		   			   }
		   			   catch (Exception e) {
			   			   throw new Exception("SysIntegrationPending::insertPending - Errore nel check tabella pending per item: "+
  					   			    		   "(idDoc,xmlotherkey) ("+idDocumento+","+vOtherKey.get(j)+") - type ("+sysModelList.get(i).getSysIntType().getType()+")\n"+
  					   			    		   e.getMessage());
		   			   }
		   			   
		   			   if (!bTest) {
		   				   //Inserisco l'oggetto in attesa della make che poi verrà lanciata sotto oppure da un successivo flusso
		   				   try {
		   				     sysDbOp.insertSysIntegrationPending(sysModelList.get(i).getSysIntType().getType(),
	   							      						     idDocumento,vOtherKey.get(j));
		   				   }
			   			   catch (Exception e) {
				   			 throw new Exception("SysIntegrationPending::insertPending - Errore nell'insert in tabella pending per item: "+
	  					   			    		 "(idDoc,xmlotherkey) ("+idDocumento+","+vOtherKey.get(j)+") - type ("+sysModelList.get(i).getSysIntType().getType()+")\n"+
	  					   			    		 e.getMessage());
			   			   }
			   			   
			   			   //GESTIONE INSERIMENTO LOG
			   			   if (sysModelList.get(i).getLogItem().equals("H"))
			   				   try {
			   				     sysDbOp.insertSysIntegrationPendingLog(sysModelList.get(i).getSysIntType().getType(),
		   							      						        idDocumento,vOtherKey.get(j),
		   							      						        SysIntegrationConstant._PENDINGLOG_ACTION_CREATE,
		   							      						        sysModelList.get(i).getSysIntDescrStruct().getNameObject(),
		   							      						        varEnv.getUser(),SysIntegrationConstant._RESULT_LOG_OK,"");
			   				   }
				   			   catch (Exception e) {
					   			 throw new Exception("SysIntegrationPending::insertPendingLog - Errore nell'insert in tabella pendingLog per item: "+
		  					   			    		 "(idDoc,xmlotherkey) ("+idDocumento+","+vOtherKey.get(j)+") - type ("+sysModelList.get(i).getSysIntType().getType()+")\n"+
		  					   			    		 e.getMessage());
				   			   }
		   			   }	
		   			   else {
		   				   if (!sysModelList.get(i).getSysIntDescrStruct().getTypeSyncroASyncro().equals("S")) {
		   					   //Se non è sincrona l'azione significa che poi ci penserà + avanti un flusso
		   					   //o qualche agente. Io per adesso quindi aggiorno solo l'oggetto in pending e basta
			   				   try {
				   				  sysDbOp.updateSysIntegrationPending(sysModelList.get(i).getSysIntType().getType(),
			   							      						  idDocumento,vOtherKey.get(j),
			   							      						  SysIntegrationConstant._PENDING,SysIntegrationConstant._LASTSTATUS_PENDING_WAIT,
			   							      						  "",null);
			   				   }
				   			   catch (Exception e) {
					   			 throw new Exception("SysIntegrationPending::updatePendingLog - Errore nell'update in tabella pendingLog per item: "+
		  					   			    		 "(idDoc,xmlotherkey) ("+idDocumento+","+vOtherKey.get(j)+") - type ("+sysModelList.get(i).getSysIntType().getType()+")\n"+
		  					   			    		 e.getMessage());
				   			   }
				   			   
				   			   if (sysModelList.get(i).getLogItem().equals("H"))
					   			   try {
				   				     sysDbOp.insertSysIntegrationPendingLog(sysModelList.get(i).getSysIntType().getType(),
			   							      						        idDocumento,vOtherKey.get(j),
			   							      						        SysIntegrationConstant._PENDINGLOG_ACTION_MODIFY,
			   							      						        sysModelList.get(i).getSysIntDescrStruct().getNameObject(),
			   							      						        varEnv.getUser(),SysIntegrationConstant._RESULT_LOG_OK,"");
				   				   }
					   			   catch (Exception e) {
						   			 throw new Exception("SysIntegrationPending::insertPendingLog - Errore nell'insert in tabella pendingLog per item: "+
			  					   			    		 "(idDoc,xmlotherkey) ("+idDocumento+","+vOtherKey.get(j)+") - type ("+sysModelList.get(i).getSysIntType().getType()+")\n"+
			  					   			    		 e.getMessage());
					   			   }
		   				   }
		   			   }
		   			
				   	   //2. Lancio la make per ogni item di ogni tipo di sysModel 
				   	   //solo se il tipo di integrazione è sincrona		   	   
				   	   if (sysModelList.get(i).getSysIntDescrStruct().getTypeSyncroASyncro().equals("S")) {				   		 
				   		   try {
				   			 executeMakeUpdatePending(sysModelList.get(i),idDocumento,vOtherKey.get(j),sIdentifierRemObject);
				   		   }
				   		   catch (Exception e) {
				   			 String sError="SysIntegrationPending::insertPending - Errore nel lancio make pending SINCRONA per item: "+
			    		 		  		   "(idDoc,xmlotherkey) ("+idDocumento+","+vOtherKey.get(j)+") - type ("+sysModelList.get(i).getSysIntType().getType()+")\n"+
			    		 		  		   e.getMessage();
				   			 				   			
			   				
				   			 throw new Exception(sError);
			   			   }				   		   				   		  
				   		   
				   	   }
		   		   }
		   	   }
		   	   		   	  
	   }
	   
	   private void init(String idDoc, String type,Environment en) throws Exception {
		       if (type==null && idDoc==null) return;
		       
		       idDocumento=idDoc;
		       typeIntegrationPending=type;
			   varEnv=en;		       
	   }
	   
	   private void initializeStruct() throws Exception {
		   	   sysDbOp =  new SysIntegrationDbOperation(varEnv,varEnv.getDbOp(),null);		       			  
		   
		   	   try {
	   		     ResultSet rst = sysDbOp.checkDocumentIntegration(idDocumento,typeIntegrationPending);
	   		 		   		
	   		     fillSysModelList(rst);		
	   		 
		   	   }
		   	   catch (Exception e) {		   		
		         throw new Exception("SysIntegrationPending::init - Ricerca delle integrazioni (da idDoc="+idDocumento+",type="+typeIntegrationPending+") - "+e.getMessage());		   		 		   		 
		   	   }	
	   }
	   
	   private void fillSysModelList(ResultSet rst) throws Exception {
		       Vector<ArCmType> vArCmTypeTemp = new Vector<ArCmType>();
		       
		       while (rst.next()) {		    	 
		    	   vArCmTypeTemp.add(new ArCmType(rst.getString("AREA"),rst.getString("CODICE_MODELLO"),rst.getString("TYPE_INTEGRATION")));
		       }
	   			   
	   		 
	   		   for(int i=0;i<vArCmTypeTemp.size();i++) 
	   			   sysModelList.add(new SysIntegrationModel(vArCmTypeTemp.get(i).ar,
	   													    vArCmTypeTemp.get(i).cm,
	   													    vArCmTypeTemp.get(i).type,
	   													    varEnv));
       }	  
	   
	   private void executeMakeUpdatePending(SysIntegrationModel sm,String idDocumento,String otherKey,String idRemObj) throws Exception {
		   	   String sError="";
		   	   String idRemObjRet=idRemObj;
		   	   
		   	   Profilo pDocumento = null;
		       
		       try {
		    	   pDocumento = new Profilo(idDocumento);
		    	   pDocumento.initVarEnv(varEnv.getUser(),varEnv.getPwd(), varEnv.getDbOp().getConn());		    	   
		    	   pDocumento.setSkipUnknowField(true);
		    	   pDocumento.addTypeAclReturn(Global.ABIL_MODI);
		    	   pDocumento.addTypeAclReturn(Global.ABIL_CANC);
		    	  
		    	   if (!pDocumento.accedi(Global.ACCESS_ATTACH,false,Profilo.RETRIEVE_ALLACL_USER).booleanValue())
		    		   throw new Exception(pDocumento.getError());		    	   		    	 
		       }		         
		       catch(Exception e) {
		    	   throw new Exception("SysIntegrationPending::executeMakeUpdatePending - Attenzione! non riesco a recuperare le informazioni del documento: "+idDocumento+"\n"+e.getMessage());
		       }
		   	   
		   	   try {
		   		 idRemObjRet=executeMake(sm,pDocumento,otherKey,idRemObj);
	   		   }
	   		   catch (Exception e) {	   			
	   			 sError="SysIntegrationPending::executeMakeUpdatePending - Errore nel lancio make pending\n"+
	   			 		e.getMessage();
	   		   }	   		   
	   		   
	   		   if (sError.equals("")) {
	   			   //E' andato tutto ok....
	   			   try {
		   		     sysDbOp.updateSysIntegrationPending(sm.getSysIntType().getType(),
							                             idDocumento,otherKey,
							                             SysIntegrationConstant._NOPENDING,SysIntegrationConstant._LASTSTATUS_PENDING_OK,
						                                 "",idRemObjRet);
		   		   }
		   		   catch (Exception e) {
		   			 throw new Exception("SysIntegrationPending::executeMakeUpdatePending - si è verificato un errore nell'aggiornamento della tabella pending per aggiornare il valore di pending a 0: \n"+				   			   
				   			   			 e.getMessage());		   			   	  
				   }
		   			 
		   		   if (sm.getLogItem().equals("H")) {
		   			   try {sysDbOp.insertSysIntegrationPendingLog(sm.getSysIntType().getType(),
		   					   								  idDocumento,otherKey,
		   					   								  SysIntegrationConstant._PENDINGLOG_ACTION_MODIFY,
		   					   							      sm.getSysIntDescrStruct().getNameObject(),
		   					   								  varEnv.getUser(),SysIntegrationConstant._RESULT_LOG_OK,"");}
			   		   catch (Exception e) {
			   			   throw new Exception("SysIntegrationPending::executeMakeUpdatePending - si è verificato un errore nell'aggiornamento della tabella pendingLog per inserire ok nel risultato dell'operazione (pending a 0): \n"+				   			   
		   			   			 			   e.getMessage());		
			   		   }
		   		   }
	   		   }
	   		   else {
	   			   //Qualcosa è andato storto.....lo scrivo ma non blocco ovviamente
	   			   try {
	   				   sysDbOp.updateSysIntegrationPending(sm.getSysIntType().getType(),
	   						   							   idDocumento,otherKey,
							      					       SysIntegrationConstant._NOMODIFYNOPENDING,SysIntegrationConstant._LASTSTATUS_PENDING_ER,
							      					       sError,idRemObjRet);
	   			   }
	   			   catch (Exception e) {
	   				   throw new Exception("SysIntegrationPending::executeMakeUpdatePending - si è verificato anche un errore nell'aggiornamento della tabella pending per scrivere l'errore qui sotto: \n"+
	   						   			   "Errore che doveva essere scritto: "+sError+"\n"+
	   						   			   "Errore che si è verificato nell'aggiornare la tabella pending: "+e.getMessage());	   				   	 
	   			   }
	   			 
	   			   if (!sm.getLogItem().equals("O")) 				   								   				
	   				   try {sysDbOp.insertSysIntegrationPendingLog(sm.getSysIntType().getType(),
	   													   		   idDocumento,otherKey,
	   													   		   SysIntegrationConstant._PENDINGLOG_ACTION_MODIFY,
	   													   		   sm.getSysIntDescrStruct().getNameObject(),
	   													   		   varEnv.getUser(),SysIntegrationConstant._RESULT_LOG_ER,sError);}
		   			   catch (Exception e) {
		   				   throw new Exception("SysIntegrationPending::executeMakeUpdatePending - si è verificato anche un errore nell'aggiornamento della tabella pendingLog per scrivere l'errore qui sotto: \n"+
		   						   			   "Errore che doveva essere scritto: "+sError+"\n"+
		   						   			   "Errore che si è verificato nell'aggiornare la tabella pendingLog: "+e.getMessage());	
		   			   }	   		
	   		   }
	   			   
	   		   
	   }
	   
	   private String executeMake(SysIntegrationModel sm,Profilo pDocumento,String otherKey,String idRemObj) throws Exception {
		       Class classe;
		       Object istanza=null;
		       String idRemObjRet=idRemObj;
		       
		       
		       try {
		    	   classe = Class.forName(sm.getSysIntType().getClassImplementation());  
		    	   Class[] argsClass = new Class[] {SysIntegrationModel.class, Profilo.class, String.class, String.class};
			       Object[] argomenti = new Object[] {sm, pDocumento,otherKey,idRemObj};
			       Constructor argsConstructor = classe.getConstructor(argsClass);
			       
			       istanza = argsConstructor.newInstance(argomenti);
		       }		         
		       catch(ClassNotFoundException clnfex) {
		    	   throw new Exception("Attenzione! non riesco a trovare la classe per l'integrazione: "+sm.getSysIntType().getClassImplementation()+
		    			               "\nVerificare di avere nel classpath il jar appropriato");
		       }
		       
		       try {		       			       			     			  
			       Method mthd = classe.getMethod("make");
			       idRemObjRet = (String)mthd.invoke(istanza,null);
		       }		         
		       catch (NoSuchMethodException nsm) {
		    	   throw new Exception("Attenzione! errore non riesco a trovare il metodo make nella classe: "+sm.getSysIntType().getClassImplementation());
		       }
		       catch (InvocationTargetException ite) {		    	  
		    	   throw new Exception("Attenzione! errore richiamando il metodo make nella classe: "+sm.getSysIntType().getClassImplementation()+
			               			   "\nErrore: "+ ite.getTargetException().getMessage());
		       }
		       catch(Exception mnte) {		 		    	   
		    	   throw new Exception("Attenzione! errore generico richiamando nel metodo make nella classe: "+sm.getSysIntType().getClassImplementation()+
		    			               "\nErrore: "+mnte.getMessage());
		       }
		       
		       return idRemObjRet;
		       
	   }
	   
	   private SysIntegrationModel getSysIntModelFromArCmType(String ar,String cm,String type) {
		       for(int i=0;i<sysModelList.size();i++) {
		    	   if (sysModelList.get(i).getSysIntType().getType().equals(type) &&
		    	      sysModelList.get(i).getArea().equals(ar) &&
		    	      sysModelList.get(i).getCodiceModello().equals(cm)) 
		    		  return sysModelList.get(i);
		       }
		       
		       return null;
	   }
	   
	   private void connect() throws Exception {
      	 	  if (varEnv.getDbOp()==null) {
      	 		  varEnv.connect();
        	 	  bConnectInThisClass=true;
      	 	  }      	 		        	 		
	   }

	   private void closeCommit() throws Exception {
        	 if (bConnectInThisClass) {varEnv.disconnectCommit(); }
	   }	
	   
	   private void closeRollback() throws Exception {
		     if (bConnectInThisClass)  {varEnv.disconnectClose(); }
	   }	   
	   
	   public String toString() {
		   	  StringBuffer sToStr = new StringBuffer("");
		   	  
		   	  if (idDocumento==null)
		   		sToStr.append(" INGRESSO COME .... RICERCA E PROCESSAMENTO DI TUTTI I PENDING PER TYPE= "+typeIntegrationPending+"\n\n");
		   	  else
		   		sToStr.append(" INGRESSO COME .... INSERIMENTO ED EVENTUALE PROCESSAMENTO DI ID_DOCUMENTO= "+idDocumento+"\n\n");  
		   	  sToStr.append("-------- MODELLO x INTEGRAZIONE ---------\n");
		   	  for(int i=0;i<sysModelList.size();i++) {
		   		 sToStr.append(sysModelList.get(i).toString());
		   	  }
		   	  sToStr.append("-------- END MODELLO x INTEGRAZIONE ---------\n\n\n");
		   	  
		   	  return sToStr.toString();
	   }		 	     

	   
}

class ArCmType {
	  String ar,cm,type;
	  
	  public ArCmType(String a,String c,String t) {
		     ar=a;
		     cm=c;
		     type=t;
	  }
}

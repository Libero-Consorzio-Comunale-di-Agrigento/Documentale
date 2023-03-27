package it.finmatica.dmServer.util;

import it.finmatica.jfc.utility.DateUtility;

public class FieldInformation {
	    
	   //***************Servono per l'ins./agg.
	   private String idCampo;
	   private String tipo,formatoData,inUso;
	   private String senzaSalvataggio;
	   private String senzaAggiornamento;
	   private int    log;
	   private String area,cm;
	   
	   //***************Servono per la ricerca
	   private String nomeCampo=null;	 
	   private String tableViewName=null;
	   private long   lunghezza; 
	   private String value=null,op=null, value2=null;
	   private String tipoUguaglianza=null;
	   private boolean isRicercaPuntuale=true;
	   private String valueNvl=null;
	   private boolean noCaseSensitive=false;
	   
	   /** 
	    * se campoReturn è true potrebbe anche essere
	    * di ordinamento. Se tipoOrdinamento=null
	    * allora è solo di tipo return, altrimenti
	    * è anche di ordinamento
	   **/
	   private String tipoOrdinamento=null;
	   private boolean isCampoReturn=false;
	   	   
	   private String sFormatoData="dd/mm/yyyy";
	   private String sFormatoDataOraOracle="dd/mm/yyyy hh24:mi:ss";
	   private String sFormatoDataOraJava="dd/MM/yyyy HH:mm:ss";
	   
	   private String sFormatoDataScelto="";
	   
	   private String[] reserveWord    = {"\\","&","?","{","}",",","(",")","[","]",
			   						      "-",";","~","|","$","!",">","*","_"} ;
	   private String[] reserveWordCatSearch    = {"\\","&","?","{","}",",","(",")","[","]",
			      								   "-",";","~","|","$","!",">","_"} ;
	   private String   escapeCaracter = "\\";
	   
	   public  final static String _OPERATOR_IS_NULL       = "is null";
	   public  final static String _OPERATOR_IS_NOT_NULL   = "is not null";
	   public  final static String _OPERATOR_CONTAINS      = "contains";
	   public  final static String _OPERATOR_EQUALS        = "=";
	   public  final static String _OPERATOR_NOT_EQUALS    = "<>";
	   public  final static String _OPERATOR_NOT_GREATHEN  = ">";
	   public  final static String _OPERATOR_NOT_LESSTHEN  = "<";
	   public  final static String _OPERATOR_MAGGUGUALE    = ">=";
	   public  final static String _OPERATOR_MINUGUALE     = "<=";
	   public  final static String _OPERATOR_BETWEEN       = "between";	   	   
	   
	   public FieldInformation(String idC,String tP,String fD,String iU,String sS, String sA) {
		      idCampo=idC;
		      tipo=tP;
		      formatoData=fD;
		      inUso=iU;
		      senzaSalvataggio=sS;
		      senzaAggiornamento=sA;
	   }	
	   
	   public FieldInformation(String nCampo) {		      
		      nomeCampo=nCampo;
	   }
	   
	   public String getTipo() {
			  return tipo;
	   }

	   public String getIdCampo() {
	 	      return idCampo;
	   }
	   
	   public String getFormatoData() {
	 	      return formatoData;
	   }
	   
	   public String getInUso() {
	 	      return inUso;
	   }	 
	   
	   public String getSenzaSalvataggio() {
	 	      return senzaSalvataggio;
	   }	   

	   public void setSenzaAggiornamento(String sA) {
	 	      senzaAggiornamento=sA;
	   }
	   
	   public String getSenzaAggiornamento() {
	 	      return senzaAggiornamento;
	   }	  
	
	   public long getLunghezza() {
			  return lunghezza;
	   }
	   
	   public int getLog() {
		      return log;
	   }
	
	   public String getNomeCampo() {
		  	  return nomeCampo;
	   }
	
	   public void setLunghezza(long lunghezza) {
			  this.lunghezza = lunghezza;
	   }
	
	   public void setNomeCampo(String nomeCampo) {
			  this.nomeCampo = nomeCampo;
	   }

	   public String getTableViewName() {
			  return tableViewName;
	   }
	
	   public void setTableViewName(String tableViewName) {
			  this.tableViewName = tableViewName;
	   }

	   public void setTipo(String tipo) {
			this.tipo = tipo;
	   }	 	
	   
	   public void setLog(int newlog) {
		      log=newlog;
	   }	   
	   
	   public String getOp() {
			  return op;
	   }

	   public String getValue() {
			  return value;
	   }
	
	   public void setOp(String op) {
			  this.op = op;
			  setEffectiveOperatorSelect();
	   }
	
	   public void setValue(String value) {
			  this.value = value;
	   }	
	   
	   public String getTipoUguaglianza() {
			  return tipoUguaglianza;
	   }

	   public void setTipoUguaglianza(String tipoUguaglianza) {
		      this.tipoUguaglianza = tipoUguaglianza;
	   }	   
	   
	   public boolean isRicercaPuntuale() {
			  return isRicercaPuntuale;
	   }
	
	   public void setRicercaPuntuale(boolean isRicercaPuntuale) {
			  this.isRicercaPuntuale = isRicercaPuntuale;
	   }	
	   
	   public String getTipoOrdinamento() {
		      return tipoOrdinamento;
	   }

	   public void setTipoOrdinamento(String tipoOrdinamento) {
		      this.tipoOrdinamento = tipoOrdinamento;
	   }	   
 	
	   public boolean isCampoReturn() {
		      return isCampoReturn;
	   }
	   
	   public void setFormatoData(String formatoData) {
			  this.formatoData = formatoData;
	   }	   

	   public void setCampoReturn(boolean isCampoReturn) {
		      this.isCampoReturn = isCampoReturn;
	   }	   
	    
	   public String getFieldSearchCondition(boolean bRicercaPuntualeGlobale) {
		      StringBuffer sCondition = new StringBuffer("");
		      
		      if (
		    		 (op.equals(_OPERATOR_IS_NULL) || op.equals(_OPERATOR_IS_NOT_NULL)) 
		    		 ||
		    		 (op.equals(_OPERATOR_EQUALS) &&( value.equals(_OPERATOR_IS_NULL) || value.equals(_OPERATOR_IS_NOT_NULL)))
		    	  ) {
		    	  if (value.equals(_OPERATOR_IS_NULL) || value.equals(_OPERATOR_IS_NOT_NULL))
		    	  	  sCondition.append(tableViewName+"."+nomeCampo+" "+value);
		    	  else
		    		  sCondition.append(tableViewName+"."+nomeCampo+" "+op);
		      }
		    	  		      
		      else {
		    	  if (tipo.equals("VARCHAR2") || tipo.equals("CLOB"))
		    		  sCondition.append(stringFieldSearchCondition(bRicercaPuntualeGlobale,tipo,1));
		    	  else if (tipo.equals("NUMBER"))
		    		  sCondition.append(numberFieldSearchCondition());
		    	  else if (tipo.equals("DATE"))
		    		  sCondition.append(dateFieldSearchCondition());		    	  
		      }
		    	  
		      
		      return sCondition.toString();
	   }		   	   
	   
	   public String toString() {
		      String sRet="\n-**************  FieldInformation   **************- \n\n";;
		      
		      sRet+="Campo:           "+nomeCampo+"\n";
		      sRet+="TabView:         "+tableViewName+"\n";
		      sRet+="tipo:            "+tipo+"\n";
		      sRet+="valore:          "+value+"\n";
		      sRet+="valore2:         "+value2+"\n";
		      sRet+="operatore:       "+op+"\n";
		      sRet+="tipo Ug. :       "+tipoUguaglianza+"\n";
		      sRet+="Ric. Puntuale. : "+isRicercaPuntuale+"\n";
		      sRet+="Tipo Ordinam. :   "+tipoOrdinamento+"\n";
		      sRet+="Campo di return.: "+isCampoReturn+"\n";
		      
		      return sRet;
	   }
	   
	   private String stringFieldSearchCondition(boolean bRicercaPuntualeGlobale,String tipo, int isHorizontal) {
		       StringBuffer sCondition = new StringBuffer("");	
		       
		       String upperBegin, upperEnd;
		       
		       if (noCaseSensitive) {
		    	   upperBegin="upper(";
		    	   upperEnd=")";
		       }
		       else {
		    	   upperBegin="";
		    	   upperEnd="";
		       }
		    	   
		       
		       String sCampo=tableViewName+"."+nomeCampo;
		       
		       if (valueNvl!=null)
		    	   sCampo="NVL("+tableViewName+"."+nomeCampo+",'"+Global.replaceAll(valueNvl,"'","''")+"') ";
		    	   
		    
		       //SE IL CAMPO E' CLOB LO FORZO ALLA CONTAINS IN OGNI CASO
		       if (tipo.equals("CLOB")) {
		       	  sCondition.append("CONTAINS("+tableViewName+"."+nomeCampo+",'"+Global.replaceAll(protectReserveWord(value),"'","''")+"')=0 ");
		       	  return sCondition.toString();
		       }
		       
		       //VENGO DALLA PAGINA DEI PARAMETRI DELLA QUERY PARAMETRICA
	   		   //E QUINDI DEVO DARE RETTA ALLA TENDINA CON I TRE POSSIBILI VALORI:
	   		   //Inizia per,Contiene,Frase Esatta
		       if (tipoUguaglianza!=null) {
				   if (tipoUguaglianza.equals("LIKE")) 
					   sCondition.append(tableViewName+"."+nomeCampo+" like '"+Global.replaceAll(value,"'","''")+"%' ");
				   else if (tipoUguaglianza.equals("ESATTA"))
					   sCondition.append(tableViewName+"."+nomeCampo+"='"+Global.replaceAll(value,"'","''")+"' ");
				   else if (tipoUguaglianza.equals("CONTAINS")) {
					   if (isHorizontal==0)
						   sCondition.append("CATSEARCH("+tableViewName+"."+nomeCampo+",'"+Global.replaceAll(protectReserveWordCatSearch(value),"'","''")+"','')>0 ");
					   else
						   sCondition.append(tableViewName+"."+nomeCampo+" like '%"+Global.replaceAll(value,"'","''")+"%' ");
				   }
					   					   			  
			   }
		       //CASO NORMALE: VENGO DALLA QUERY NON PARAMETRICA (WEB O JAVA)
			   else {
				   if ((isRicercaPuntuale==false && bRicercaPuntualeGlobale==false && value.length()>=3 )) {
					   if (op.equals("<>")) {
						   sCondition.append("CATSEARCH("+tableViewName+"."+nomeCampo+",'"+Global.replaceAll(protectReserveWordCatSearch(value),"'","''")+"','')=0 ");
					   }
					   else {
						   sCondition.append("CATSEARCH("+tableViewName+"."+nomeCampo+",'"+Global.replaceAll(protectReserveWordCatSearch(value),"'","''")+"','')>0 ");
					   }
			       }
			       else {
	   			       if (op.equals("<>"))
						   sCondition.append(upperBegin+sCampo+upperEnd+"<>"+upperBegin+"'"+Global.replaceAll(value,"'","''")+"'"+upperEnd);
					   else
		   				   if (value.indexOf("%")!=-1)
		   					   sCondition.append(upperBegin+sCampo+upperEnd+" like "+upperBegin+"'"+Global.replaceAll(value,"'","''")+"'"+upperEnd);
		   				   else {
		   					   if (isOperator())
		   						   sCondition.append(upperBegin+sCampo+upperEnd+" "+op+" "+upperBegin+"'"+Global.replaceAll(value,"'","''")+"'"+upperEnd);
		   					   else {
		   						   if (op.equals(_OPERATOR_IS_NULL) || op.equals(_OPERATOR_IS_NOT_NULL))
		   							   sCondition.append(sCampo+" is null ");
		   						   else
		   							sCondition.append(upperBegin+sCampo+upperEnd+" between"+" "+upperBegin+"'"+Global.replaceAll(value,"'","''")+"'"+upperEnd+" AND "+" "+upperBegin+"'"+Global.replaceAll(op,"'","''")+"'"+upperEnd);
		   					   }
		   				   }		   
				   }
			   }
		       
		       return sCondition.toString();
	   }
	   
	   private String numberFieldSearchCondition() {
		       StringBuffer sCondition = new StringBuffer("");
		       
		       String sCampo=tableViewName+"."+nomeCampo;
		       
		       if (valueNvl!=null)
		    	   sCampo="NVL("+tableViewName+"."+nomeCampo+","+valueNvl+") ";		       
		       
		       if (op.equals(_OPERATOR_BETWEEN.toUpperCase()))
		    	   sCondition.append(sCampo+" "+_OPERATOR_BETWEEN.toUpperCase()+" "+value+" AND "+value2);
		       else
		    	   sCondition.append(sCampo+" "+op+" "+value);
		       
		       return sCondition.toString();
	   }	 
	   
	   private String dateFieldSearchCondition() {
		       StringBuffer sCondition = new StringBuffer("");		       
		       String sData1,sData2="";		       		       
		       
		       String sCampo=tableViewName+"."+nomeCampo;
		       
		       if (sFormatoDataScelto.equals("")) {
			       if (value.toUpperCase().indexOf("SYSDATE")!=-1 || DateUtility.isDateValid(value,sFormatoData))
		    		   sFormatoDataScelto=sFormatoData;
		    	   else
		    		   sFormatoDataScelto=sFormatoDataOraOracle;
		       }
		       
		       if (valueNvl!=null) {
		    	   if (valueNvl.toUpperCase().indexOf("SYSDATE")!=-1)
		    		   sCampo="NVL("+tableViewName+"."+nomeCampo+","+valueNvl+") ";
		    	   else
		    		   sCampo="NVL("+tableViewName+"."+nomeCampo+",TO_DATE('"+valueNvl+"','"+sFormatoDataScelto+"')) ";
		       }
		    	   		       
		       
		       if (value.toUpperCase().indexOf("SYSDATE")==-1) 
			   	   sData1="TO_DATE('"+value+"','"+sFormatoDataScelto+"')";
		       else
		    	   sData1=value;
		       
		       if (op.equals(_OPERATOR_BETWEEN.toUpperCase())) {
			       if (value2.toUpperCase().indexOf("SYSDATE")==-1) 
				   	   sData2="TO_DATE('"+value2+"','"+sFormatoDataScelto+"')";
			       else
			    	   sData2=value2;		    	   
		       }
		       
		       if (op.equals(_OPERATOR_BETWEEN.toUpperCase()))
		    	   sCondition.append(sCampo+" "+_OPERATOR_BETWEEN.toUpperCase()+" "+sData1+" AND "+sData2);
		       else
		    	   sCondition.append(sCampo+" "+op+" "+sData1);		       
		       
		       return sCondition.toString();
	   }	   
	   
	   /**
	    * Sceglie l'effettivo operatore per la select. Ad esempio contains viene
	    * sostituito con =, oppure se l'operatore è un numero o una data
	    * questi viene sostituito con  un between ed il valore prima contenuto
	    * va a finire in value2
	   **/
	   private void setEffectiveOperatorSelect() {
		       if (op.equals(_OPERATOR_CONTAINS)) {
		    	   op="=";
		    	   return;
		       }		      		       		       
		   
		       if (DateUtility.isDateValid(op,sFormatoData) || DateUtility.isDateValid(op,sFormatoDataOraJava) || (op.toUpperCase().indexOf("SYSDATE")!=-1)) {
		    	   if (op.toUpperCase().indexOf("SYSDATE")!=-1 || DateUtility.isDateValid(op,sFormatoData))
			    	      sFormatoDataScelto=sFormatoData;
			    	   else
			    		  sFormatoDataScelto=sFormatoDataOraOracle;
		    	   
		    	   
		    	   value2=op;
		    	   op=_OPERATOR_BETWEEN.toUpperCase();
		    	   
		    	   
		       }
		       else {		    	  
		    	   
		    	   try {
		    		 Long.parseLong(op); 
		    	     value2=op;
		    	     op=_OPERATOR_BETWEEN.toUpperCase();
		    	   } 
		    	   catch (NumberFormatException ex){		    		  
		    		//Se il 2° valore non è ne data ne numero allora non lo considero proprio
		    		if (op.toUpperCase().equals(FieldInformation._OPERATOR_IS_NULL.toUpperCase()) || 
		 		       	op.toUpperCase().equals(FieldInformation._OPERATOR_IS_NOT_NULL.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_CONTAINS.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_EQUALS.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_NOT_EQUALS.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_NOT_GREATHEN.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_NOT_LESSTHEN.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_MAGGUGUALE.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_MINUGUALE.toUpperCase()) ||
		 		        op.toUpperCase().equals(FieldInformation._OPERATOR_BETWEEN.toUpperCase())) {
		    			//DONTCARE
		    		}		    			
		    		else	
		    			op="=";
		    		
		    		value2=null;
		    	   }
		       }
	   }
	   
	   public void setValueNvl(String valueNvl) {
		      this.valueNvl = valueNvl;
	   }	   
	   
	   private String protectReserveWord(String phrase) {
		   	   for(int i=0;i<reserveWord.length;i++) {
		   		   phrase=Global.replaceAll(phrase,reserveWord[i],escapeCaracter+reserveWord[i]);
		   	   }
		   
		       return phrase;
	   }
	   
	   private String protectReserveWordCatSearch(String phrase) {		   		  
	   	   for(int i=0;i<reserveWordCatSearch.length;i++) {
	   		   phrase=Global.replaceAll(phrase,reserveWordCatSearch[i],escapeCaracter+reserveWordCatSearch[i]);
	   	   }
	   	   	   	  	   	 
	   	   phrase=Global.replaceAll(phrase,"%","*");
	   
	   	   if (phrase.substring(0,1).equals("*")) phrase="**"+phrase.substring(1,phrase.length());
	   	
	       return phrase;
	   }
	   
	   

	   public String getArea() {
		   	  return area;
	   }

	   public void setArea(String area) {
		   	  this.area = area;
	   }

	   public String getCm() {
		   	  return cm;
	   }

	   public void setCm(String cm) {
		   	  this.cm = cm;
	   }	 
	   
	   private boolean isOperator() {
	          if (op.equals(_OPERATOR_EQUALS) || op.equals(_OPERATOR_NOT_EQUALS) || 
	              op.equals(_OPERATOR_NOT_GREATHEN) || op.equals(_OPERATOR_NOT_LESSTHEN) ||
	              op.equals(_OPERATOR_MAGGUGUALE) || op.equals(_OPERATOR_MINUGUALE) )   return true;
	    
	         return false;
	   }

	   public boolean isNoCaseSensitive() {
			  return noCaseSensitive;
	   }

	   public void setNoCaseSensitive(boolean noCaseSensitive) {
		      this.noCaseSensitive = noCaseSensitive;
	   }
}

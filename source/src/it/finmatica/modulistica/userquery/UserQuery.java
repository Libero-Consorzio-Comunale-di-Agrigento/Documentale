package it.finmatica.modulistica.userquery;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

public class UserQuery {   
	   private final String _TAG_PAR           		 ="PAR";
	   private final String _TAG_USER_QUERY          ="USER_QUERY"; 
	   private final String _TAG_PAR_INTERNO         =":@";
	   
	   
	   private final String _TAG_PARAMETER           ="@";
	   private final String _AL_PREFIX               ="_2_";
	   private final String _AL_SUFFIX_SELECT        ="_AL";	  	  
	   private final String _DAL_SUFFIX_SELECT       ="_DAL";
	   private final String _SPACE                   =" ";
	   private final String _APICE                   ="'";	  
	   
	   private final String _TAG_PARAMETER_PROTECT   ="<CHIOC>";	
	   
	   private final String _SELECT                  ="SELECT";
	   private final String _FROM                	 =" FROM";
	   private final String _BETWEEN                 =" BETWEEN";
	   private final String _AND                     =" AND";
	   private final String _WHERE                   =" WHERE";	  	  
	   private final String _INTERSECT               =" INTERSECT ";	  	  
	   private final String _UNION                   =" UNION ";
	   private final String _ALL                     =" ALL ";
	   private final String _MINUS                   =" MINUS ";
	   
	   private final String _ENDSELECT               =" AND 1=1";	   
	   
	   private final int TYPE_CONDITION_PARAM        = 0;
	   private final int TYPE_CONDITION_LEGAME       = 1;
	   private final int TYPE_CONDITION_FISSA        = 2;	   
	   
	   private final String[] listMergeSqlKey	     = new String[]{_INTERSECT,_UNION,_MINUS," "+_UNION.trim()+" "+_ALL.trim()+" "};
	   
	   Vector<String> vPar = new Vector<String>();
	   HttpServletRequest request;
	   String strSelect;
	   
	   Vector<NodoCondizione> vNodiCond = new Vector<NodoCondizione>();
	   Vector<NodoTabella>    vNodiTab  = new Vector<NodoTabella>();
	   NTL					  tree      = null;	   
	   
	   public UserQuery(HttpServletRequest r, String s) {
		   	  request=r;
		   	  strSelect=s;
	   }
	   
	   public String bindSearch() throws Exception { 
		   	  StringBuffer sSqlResult= new StringBuffer("");
		   	  
		   	  strSelect=strSelect.replaceAll("\t"," ");
		   			   	  
		   	  //System.out.println(strSelect);
		   	
		   	  //**********TRATTO I PARAMETRI
		   	  estraiParametri(strSelect);		   	  
		   	  //LOG....Stampa parametri...
		   	  for(int i=0;i<vPar.size();i++) System.out.println(vPar.get(i));
		   	  //**********FINE PARAMETRI
		   	  
		   	  int indexOfPar, indexOfUserQuery;
		   	  indexOfPar=strSelect.toUpperCase().indexOf("</"+_TAG_PAR+">");
		   	  indexOfUserQuery=strSelect.toUpperCase().indexOf("<"+_TAG_USER_QUERY+">");
		   	  
		   	  if (indexOfPar==-1)
		   		  strSelect=strSelect.substring(indexOfUserQuery+("<"+_TAG_USER_QUERY+">").length() + 1);
		   	  else
		   		  strSelect=strSelect.substring(indexOfPar+("</"+_TAG_PAR+">").length() + 1);
		   	  
		   	  //**********ESTRAGGO LE SINGOLE SELECT
		   	  int indexSelect, indexOtherSelect, indexMergeKey;
		   	  int indexAbsolutePosition,indexAbsolutePositionMenoUno;
		   	  
		   	  indexSelect=strSelect.toUpperCase().indexOf(_SELECT);
		   	  indexOtherSelect=strSelect.toUpperCase().indexOf(_SELECT,indexSelect+1);
		   	  indexMergeKey=indexOfFirstMergeSqlKey(strSelect,indexSelect);
			  /* 	System.out.println("indexSelect-->"+indexSelect);
			   	System.out.println("indexOtherSelect"+indexOtherSelect);
			   	System.out.println("indexOtherSelect"+indexMergeKey);*/
		   	
		   	  if (indexOtherSelect<indexMergeKey || (indexOtherSelect!=-1 && indexMergeKey==-1)) {
		   		  //Significa che ho una select fatta così:
		   		  //Select ..... from (Select ..........), .....
		   		  //con una from con dentro una subquery.
		   		  //devo quindi non considerare la select + esterna
		   		  //e lavorare quella interna
		   		  //System.out.println("dentro k");
		   		  sSqlResult.append(strSelect.substring(0,indexOtherSelect -1 ));
		   		  indexAbsolutePosition=indexOtherSelect;
		   	  }
		   	  else
		   		  indexAbsolutePosition=indexSelect;
		   	  		   	  
		   	  //System.out.println("OUT-->"+sSqlResult);
		   	  
		   	  int indexSelectPar=0;
		   	  while (indexMergeKey!=-1) {		   		
		   		  
		   		  String sSqlDaTattare;
		   		  
		   		 // System.out.println("indexAbsolutePosition-->"+""+(indexAbsolutePosition - 1));
		   		 // System.out.println("indexMergeKey-->"+""+indexMergeKey);
		   		  
		   		  if (indexAbsolutePosition==0)
		   			  sSqlDaTattare=strSelect.substring(0,indexMergeKey );
		   		  else
		   			  sSqlDaTattare=strSelect.substring(indexAbsolutePosition -1,indexMergeKey );
		   		  
		   		  //System.out.println("TRATTO CICLO-->"+sSqlDaTattare);
		   		  sSqlResult.append(executeBinding(sSqlDaTattare,indexSelectPar++));
		   		  
		   		  indexAbsolutePosition=indexMergeKey + 1;
		   		  indexMergeKey=indexOfFirstMergeSqlKey(strSelect,indexAbsolutePosition);		   		  
		   	  }
		   	  
		   	  //System.out.println("TRATTO FINALE-->"+strSelect.substring(indexAbsolutePosition - 1));
		   	  if (indexAbsolutePosition==0)
		   		  sSqlResult.append(executeBinding(strSelect.substring(0),indexSelectPar));	
		   	  else
		   		  sSqlResult.append(executeBinding(strSelect.substring(indexAbsolutePosition -1),indexSelectPar));
		   	  //**********FINE ESTRAGGO LE SINGOLE SELECT
		   	  
		   	  return sSqlResult.toString();
	   }
	
	   private void estraiParametri(String select) throws Exception { 
		    	   int indexTagPar, indexEndTagPar;
		    	   
		    	   //Recupero  <PAR> e </PAR>
		    	   indexTagPar=select.toUpperCase().indexOf("<"+_TAG_PAR+">");
		    	   indexEndTagPar=select.toUpperCase().indexOf("</"+_TAG_PAR+">");
		    	   
		    	   //Non ci sono parametri, significa che non tratterò la select
		    	   //nel nuovo modo ma utilizzerò per tutte le subquery
		    	   //il meccanismo vecchio solo
		    	   //di sostituzione dei parametri ed eliminazione di quelli non 
		    	   //presenti solo dalla where e non dalla from
		    	   if (indexTagPar==-1) return; 
		    	   
		    	   if (indexEndTagPar==-1) 
		    		   throw new Exception("Errore in estrazione parametri <PAR>! Non esiste il tag </PAR> mentre esiste aperto <PAR>");
		    	   
		    	   int indexStart=indexTagPar;
		    	   int indexEndTagParInterno=-1; 
		    	   int indexTagParInterno=-1;
		    	   
		    	   //Ciclo su tutti i <:@Numero> e </:@Numero>
		    	   do {
		    		 indexTagParInterno=select.toUpperCase().indexOf("<"+_TAG_PAR_INTERNO,indexStart);
		    		 indexEndTagParInterno=select.toUpperCase().indexOf("</"+_TAG_PAR_INTERNO,indexStart);
		    		 
		    		 if (indexTagParInterno==-1 && indexEndTagParInterno!=-1)
		    			 throw new Exception("Errore in estrazione parametri <PAR>! esistono tag chiusi ma non aperti");
		    		 
		    		 if (indexTagParInterno!=-1 && indexEndTagParInterno==-1)
		    			 throw new Exception("Errore in estrazione parametri <PAR>! esistono tag aperti ma non chiusi");		    		 
		    		 
		    		 //Estraggo la tabella fra <:@Numero> e </:@Numero>
		    		 if (indexEndTagParInterno!=-1) {
		    			 String sTable=select.substring(indexTagParInterno+("<"+_TAG_PAR_INTERNO).length()+1,indexEndTagParInterno );
				    		
			    		 vPar.add(sTable);
			    		 
			    		 indexStart=indexEndTagParInterno+4;
		    		 }
			    		 		    		 		    		 		    		 
		    	   } while(indexEndTagParInterno!=-1);
	   }
	   
	   private String executeBinding(String sql, int indexSelectPar) throws Exception {
		   	   StringBuffer result = new StringBuffer("");
		   	   
		   	   if ( (vPar.size() -1 )<indexSelectPar || vPar.size()==0) 
		   		   result.append(executeBindingNoPar(sql));
		   	   else {
		   		   if (vPar.get(indexSelectPar).equals("X"))
		   			   result.append(executeBindingNoPar(sql));
		   		   else
		   			   result.append(executeBindingPar(sql,vPar.get(indexSelectPar)));
		   	   }		   		   
		   	   
		       return result.toString();
	   }
	   
	   /**
	    * CASO SEMPLICE (come nella user_query versione 3.3 in giù)
	    * Metodo che esamina il "pezzo" di Select passato in input,
	    * costruisce un array ordinato di condizioni (dalla where per tutte
	    * le condizioni da AND a AND) e ne effettua il binding a partire
	    * dai parametri della httpresponse.
	    * Restituisce alla fine la select con la WHERE bindata
	    * 
	   */
	   private String executeBindingNoPar(String sql) throws Exception {
		   	   StringBuffer sRet= new StringBuffer("");
		       //Spezzo la select per trovare la condizione di where
		   	   Vector<String> vWhereCondition = new Vector<String>();
		   
		   	   int indexWhere;
		   	   indexWhere=sql.toUpperCase().indexOf(_WHERE.trim());
		   	   
		   	   if (indexWhere!=-1) {
		   		   vWhereCondition=extractWhereCondition(sql.substring(indexWhere+_WHERE.trim().length()+1));
		   		   sRet.append(sql.substring(0,indexWhere - 1)+_WHERE);
		   	   }
		   	   else {
		   		   sRet.append(sql);
		   	   }
		   	   
		   	   //System.out.println(vWhereCondition);
		   		   		   	   
		   	   for(int i=0;i<vWhereCondition.size();i++) {
		   		   //System.out.println("-->"+vWhereCondition.get(i));		   		   		   				   		  
		   		   String nomeParametro;
		   		   
		   		   nomeParametro=extractParameterName(vWhereCondition.get(i));
		   		   //Se non esiste il parametro allora è una condizione non parametrica
		   		   //quindi va lasciata così com'è
		   		   if (nomeParametro.equals("")) {		   			   
		   			   sRet.append(" "+vWhereCondition.get(i));
		   			   continue;		   		   		   		   
		   		   }
		   		
				  // System.out.println("PARAMETRO-->"+nomeParametro);
				   vWhereCondition.set(i,bindingExpression(vWhereCondition.get(i),nomeParametro));
				  // System.out.println("SOSTITUITO-->"+vWhereCondition.get(i));
				   sRet.append(" "+vWhereCondition.get(i));
				   
		   	   }
		   		   
		   	   
		   	   return sRet.toString();
	   }
	   
	   /**
	    * CASO NUOVO (user_query 3.4 in su)
	    * Metodo che esamina il "pezzo" di Select passato in input,
	    * costruisce una foresta di alberi n-ari di tabelle e condizioni
	    * a partire dalla from e dalla where. Si effettua quindi il binding a partire
	    * dai parametri della httpresponse eliminando i nodi che non hanno match
	    * con le condizioni esistenti, generando in questo modo una select 
	    * con una from ed una where diverse.
	    * Restituisce alla fine la select con la FROM e la WHERE bindata
	    * 
	   */	   
	   private String executeBindingPar(String sql, String sPar) throws Exception {
		   	   vNodiTab.clear();
		   	   vNodiCond.clear();
		   	   tree=null;
		   	   
		   	   //Estraggo le tabelle dalla from costruendo la lista di nodiTabelle
		       try {
		    	 createTreeTableNode(sql);
		    	 
		    	 if (vNodiTab.size()==0) return sql;
		       }
		       catch(Exception e) {
		    	 throw new Exception("Errore in binding (con parametri) - Parte estrazione FROM.\nErrore: "+e.getMessage()+"\nSQL="+sql);  
		       }		   		       
		       
		       /*for(int i=0;i<vNodiTab.size();i++)		      
		    	   System.out.println(vNodiTab.get(i).getNomeTabella()+"|"+vNodiTab.get(i).getAliasTabella());*/
		   
		   	   //Estraggo le condizioni dalla where costruendo i nodiCondizioni e legandoli ai nodiTabelle
		       try {
		    	 createTreeConditionNode(sql,sPar);
			    	 
		    	 if (tree==null) return sql;
		       }
		       catch(Exception e) {
		    	 throw new Exception("Errore in binding (con parametri) - Parte estrazione WHERE.\nErrore: "+e.getMessage()+"\nSQL="+sql);  
		       }	     
		       //tree.postorder(tree);
		       
		       try {
		    	 bindTreeSingoleNode(tree,true);
		       }
		       catch(Exception e) {
		    	 throw new Exception("Errore in binding (con parametri) - Parte costruzione albero FROM/WHERE.\nErrore: "+e.getMessage()+"\nSQL="+sql);  
		       }
		       
		       String sqlRet=sql.substring(0,sql.toUpperCase().indexOf(_FROM));
		       
		       
		       //tree.preorder(tree);
		       Vector<String> from= new Vector<String>(),where=new Vector<String>();
		       try {
		    	 makeSqlFromTree(tree,from,where);  
		       }
		       catch(Exception e) {
			     throw new Exception("Errore in binding (con parametri) - Parte costruzione vettori FROM/WHERE.\nErrore: "+e.getMessage()+"\nSQL="+sql);  
			   }
		       
		       //System.out.println("FROM");
		       sqlRet+=" FROM ";
		       for(int i=0;i<from.size();i++) {
		    	   if (i>0) sqlRet+=",";
		    	   sqlRet+=from.get(i);		    	   		    	   
		       }		    	   
		       
		       //System.out.println("\n\nWHERE");
		       sqlRet+=" WHERE ";
		       for(int i=0;i<where.size();i++) {
		    	   int indexAnd = where.get(i).toUpperCase().indexOf(_AND.trim()+_SPACE);
		    	   //System.out.println(indexAnd);
		    	   if (i==0) {		    
		    		   if (indexAnd!=-1)
		    			   sqlRet+=where.get(i).substring(indexAnd+_AND.length()); 
		    		   else
		    			   sqlRet+=where.get(i);
		    	   }		    		   
		    	   else {
		    		   String sAnd="";
		    		   if (indexAnd==-1) sAnd=_AND+_SPACE;
		    		
		    		   sqlRet+=sAnd+where.get(i);
		    	   }		    	   		    	   
		       }		    	   
		       
		       //Inserisco la parte finale (se c'è)
		       int indexEndSelect=sql.toUpperCase().indexOf(_ENDSELECT);
		       if (indexEndSelect!=-1) sqlRet+=sql.substring(indexEndSelect);
		      
	       	   return sqlRet;
	   }	   
	   
	   private void createTreeTableNode(String sqlPiece) throws Exception {
		       int indexFrom=sqlPiece.indexOf(_FROM);
		       int indexWhere=sqlPiece.indexOf(_WHERE);
		       String sqlFrom;
		       
		       if (indexFrom==-1) throw new Exception("Select mal formata, manca la clausola FROM");
		       
		       if (indexWhere==-1) return;
		       
		       sqlFrom=sqlPiece.substring(indexFrom+_FROM.length(),indexWhere);
		       
		       StringTokenizer strToken = new StringTokenizer(sqlFrom,",");
		       
		       while (strToken.hasMoreTokens()) {
		    	   	 String sTableWithAlias=strToken.nextToken().trim();
		    	   	 
		    	   	 if (sTableWithAlias.indexOf(_SPACE)==-1)
		    	   		 vNodiTab.add(new NodoTabella(sTableWithAlias,""));
		    	   	 else		    	   		 
		    	   		 vNodiTab.add(new NodoTabella(sTableWithAlias.substring(0,sTableWithAlias.indexOf(_SPACE) ),
		    	   				 					  sTableWithAlias.substring(sTableWithAlias.indexOf(_SPACE) + 1)));		    	   	 		    	   	 
		       }
		   
	   }
	   
	   private void createTreeConditionNode(String sqlPiece,String sPar) throws Exception {
		       int indexPos=sqlPiece.indexOf(_WHERE)+_WHERE.length();
		       
		       Vector<String> vCond =extractWhereCondition(sqlPiece.substring(indexPos)); 
		       
		       if (vCond.size()==0) return;
		       
		       //Cerco la tabella principale
		       int indexMaster;
		       indexMaster=getIndexMasterTable(sPar);
		       if (indexMaster==-1) 
		    	   throw new Exception("Non trovata tabella principale per parametro <:@>"+sPar+"</:@>");
		       
		       //Parto dalla tabella principale e mi costruisco la radice dell'albero
		       //e tutti i suoi figli diretti. Quindi ciclo sui figli diretti e mi cerco
		       //a sua volta le altre condizioni ad esso inerenti...
		       String tab, alias;
		       tab=vNodiTab.get(indexMaster).getNomeTabella().toLowerCase();
		       alias=vNodiTab.get(indexMaster).getAliasTabella().toLowerCase();
		       
		       tree = new NTL(vNodiTab.get(indexMaster));
		       		       
		       fillNTLLevel(tree,vCond,tab,alias,null);		       		    	   
		    	   
	   }
	   
	   private String bindingExpression(String sExpr, String sParameter) throws Exception {
		   	   String sRet=sExpr;
		   	  
		   	   
		   	   try {
		   		 //CASO BETWEEN
		   		 if (sExpr.toUpperCase().indexOf(_BETWEEN)!=-1) {
		   			if (sParameter.lastIndexOf(_DAL_SUFFIX_SELECT)==-1) 
	  		    		throw new Exception("Attenzione! nella condizione di between il parametro "+sParameter+" è mal formato: specificare _DAL e _AL (MAIUSCOLI) come suffissi dei pametri stessi.\nCONDIZIONE="+sExpr);
		   			
		   			String nomePar=sParameter.replace("@","").substring(0,sParameter.indexOf(_DAL_SUFFIX_SELECT) - 1);
	  		    	String valoreParametro_dal=nvl(request.getParameter(nomePar),"");
	  		    	String valoreParametro_al=nvl(request.getParameter(_AL_PREFIX+nomePar),"");
	  		    	
	  		    	//Devo aggiungere i minuti
	  		    	String sMinutiDal="", sMinutiAl="";
	  		    	if (sExpr.toLowerCase().indexOf("hh24:")!=-1 || sExpr.toLowerCase().indexOf("hh:")!=-1) {
	  		    		sMinutiDal=" 00:00:00";
	  		    		sMinutiAl=" 23:59:59";
	  		    		sRet=sRet.replaceAll("hh24:","HH24:").replaceAll("hh:","HH24:");
	  		    	}
	  		    	
	  		    	if (valoreParametro_dal.length() == 0) 
	  		    		valoreParametro_dal = valoreParametro_al;
	  		            
	  		        if (valoreParametro_al.length() == 0) 
	  		            valoreParametro_al = valoreParametro_dal;
	  		        
	  		        if (valoreParametro_dal.length() != 0) {
	  		        	sRet=sRet.replaceAll(sParameter,valoreParametro_dal+sMinutiDal);
	  		        	sRet=sRet.replaceAll(sParameter.substring(0,sParameter.lastIndexOf(_DAL_SUFFIX_SELECT))+_AL_SUFFIX_SELECT,valoreParametro_al+sMinutiAl);	  		        	
	  		        } 
	  		        else
	  		        	sRet="";
		   		 }
		   		 else {
		   			 String valoreParametro=nvl(request.getParameter(sParameter.replace("@","")),"");
		  		   	
		  		     if (valoreParametro.equals(""))
		  		    	sRet="";
			  		 else {		  		    			  		    		
			  			sRet=sRet.replaceAll(sParameter,valoreParametro.replaceAll("'","''"));
			  			//sRet=sRet.replaceAll(_TAG_PARAMETER,_TAG_PARAMETER_PROTECT);
			  		 }
		   		 }
		   	   }
		   	   catch(Exception e) {
		   		 throw new Exception("Attenzione! Errore in bindingExpression.\nErrore: "+e.getMessage());
		   	   }
		   	   
		   	   return sRet;
	   }
	   
	   /**
	    * Metodo che estrae da una clausola where un array ordinato
	    * di condizioni (separatore da AND a AND...tenendo anche conto
	    * delle between)
  	   */
	   private Vector<String> extractWhereCondition(String sWhere) throws Exception {
		   	   Vector<String> vWhereCondition = new Vector<String>();
		   	   		   	   
	   		   int indexAnd,indexBetween, indexAndPostBetween, indexPosition;
	   		   
	   		   indexPosition=0;
	   		   
	   		   do {
	   			 String condition;  
	   			 indexAnd=sWhere.toUpperCase().indexOf(_AND,indexPosition);	   			 
	   			 indexBetween=sWhere.toUpperCase().indexOf(_BETWEEN.trim(),indexPosition);
	   			 indexAndPostBetween=-1;
	   			 
	   			 if (indexPosition<indexBetween && indexBetween<indexAnd && indexAnd!=-1) {
	   				 //C'è un between
	   				 indexAndPostBetween=sWhere.toUpperCase().indexOf(_AND,indexAnd+1);
	   				 
	   				if (indexAndPostBetween==-1)
			    		throw new Exception("Attenzione! select mal formata: manca and dopo between nella parte di condizione "+sWhere.substring(indexAnd)+" .\nSELECT="+sWhere);			    		   				
	   			 }
	   			 
	   			 if (indexAnd==-1)
	   				condition=sWhere.substring(indexPosition);
	   			 else {
	   				if (indexAndPostBetween==-1)
	   					condition=sWhere.substring(indexPosition,indexAnd );
		   			 else
		   				condition=sWhere.substring(indexPosition,indexAndPostBetween);	   				 
	   			 }
	   					   			 	   			 
	   			 vWhereCondition.add(condition);
	   			 
	   			 if (indexAndPostBetween==-1)
	   				 indexPosition=indexAnd+1;
	   			 else
	   				 indexPosition=indexAndPostBetween+1;
	   			 
	   		   }
	   		   while (indexAnd!=-1);
		   	   
		   	   return vWhereCondition;
	   }
	   
	   private int getTypeCondition(String condition, Vector<NodoTabella> vTabelle, String escludiQuestaTabella, int[] indexTabellaLeganeTrovata) {
		   	  
		   	   //Se c'è uno SPAZIO@ oppure '@ allora è parametrica
		   	   if (condition.indexOf(_SPACE+_TAG_PARAMETER)!=-1 || condition.indexOf(_APICE+_TAG_PARAMETER)!=-1)
		   	   	   return TYPE_CONDITION_PARAM;
		   	   else {
		   		   //Controllo se dentro la condizione ci sia una tabella fra quelle esistenti
		   		   //escludendo quella passata come terzo parametro
		   		   for(int i=0;i<vTabelle.size();i++) {
		   			   NodoTabella n = vTabelle.get(i);
		   			   if (n.getNomeTabella().equals(escludiQuestaTabella)) continue;
		   			   
		   			   if (condition.toLowerCase().indexOf(n.getNomeTabella().toLowerCase()+".")!=-1 || 
		   				   condition.toLowerCase().indexOf(n.getAliasTabella().toLowerCase()+".")!=-1 ) {
		   				   indexTabellaLeganeTrovata[0]=i;
		   				   return TYPE_CONDITION_LEGAME;
		   			   }
		   				   
		   		   }
		   	   }
		   	   		   		   	   
		   	   return TYPE_CONDITION_FISSA;
	   }
	   
	   private int indexOfFirstMergeSqlKey(String sql, int indexFrom) {
		   	   int index=sql.length()+1;
		   	   boolean bFound=false;
		   	   
		   	   for(int i=0;i<listMergeSqlKey.length;i++) {
		   		   int indexThis;
		   		   
		   		   indexThis=sql.toUpperCase().indexOf(listMergeSqlKey[i],indexFrom);
		   		   
		   		   if (indexThis<index && indexThis!=-1) {
		   			   index=indexThis;
		   			   bFound=true;
		   		   }
		   	   }		   		    
		   	   
		   	   if (!bFound) index=-1; 
		   	   
		   	   return index;
	   }
	   
	   private int getIndexMasterTable(String sPar) {
		       for(int i=0;i<vNodiTab.size();i++) {
		    	   if ((vNodiTab.get(i).getNomeTabella()+" "+vNodiTab.get(i).getAliasTabella()).equals(sPar))
		    		   return i;
		       }
		       
		       return -1;
	   }
	   
	   private void fillNTLLevel(NTL tree, Vector<String> vCond, String tab, String alias, NodoTabella nPadre) {
		   	   for(int jCond=0;jCond<vCond.size();jCond++) {
		   		   //System.out.println(tab);
		   		   if (vCond.get(jCond).toLowerCase().indexOf(tab+".")!=-1 ||
		    		   vCond.get(jCond).toLowerCase().indexOf(alias+".")!=-1 ) {
		    		   
		    		   //Aggiungo le condizioni
		    		   NodoCondizione nCond = new NodoCondizione(vCond.get(jCond));
		    		   int indexTabLegame[] = new int[1];
		    		   indexTabLegame[0]=-1;
		    		   nCond.setType(getTypeCondition(vCond.get(jCond),vNodiTab,tab,indexTabLegame));
		    		   
		    		  // System.out.println(vCond.get(jCond));
		    		  /* System.out.println(nCond.getType());
		    		   if (indexTabLegame[0]!=-1)
		    			   System.out.println(vNodiTab.get(indexTabLegame[0]));*/
		    		   
		    		   if (nCond.getType()!=TYPE_CONDITION_LEGAME) {
			    		   //Se è una condizione di tipo fissa o di tipo parametrica
		    			   //appartiene a se stesso , quindi gliela aggiungo
		    			   tree.info.addCondizione(nCond);
		    		   }
		    		   else {
		    			   NodoTabella nFiglio = vNodiTab.get(indexTabLegame[0]);

		    			   //Controllo se questo nFiglio non è il padre...cioè si riferisce ad una condizione
		    			   //di legame fra questo nodo ed il padre e non fra lui ed un successivo figlio
		    			   if (nPadre==null || nPadre!=nFiglio) {		    			   
			    			   nFiglio.addCondizione(nCond);
			    			   
			    			   NTL ntlFiglio = new NTL(nFiglio);		    			   
			    			   tree.figli.add(ntlFiglio);		    
		    			   }	   
		    		   }
		    			   
		    	   }
	    	   }
		   	   
		   	   //Ciclo sui figli
		   	   Iterator i=tree.figli.iterator();
			   while(i.hasNext()) {
					NTL nSottoAlbero = ((NTL) i.next());
					
					fillNTLLevel(nSottoAlbero,vCond, nSottoAlbero.info.getNomeTabella(),nSottoAlbero.info.getAliasTabella(),tree.info);				
			   }
	   }	   	   
	   
	   private void bindTreeSingoleNode(NTL tree, boolean bRoot) throws Exception {
	          if(tree==null) return; 		    
	          
	          tree.setBRoot(bRoot);
	          
	          Iterator i=tree.figli.iterator();
	          while(i.hasNext())
	        	  bindTreeSingoleNode((NTL) i.next(),false);
	          
	          //TODO QUA' (IN POSTORDER)
	          Vector<NodoCondizione> vCond = tree.info.vCondizioni;
	          Boolean bToDelete=true;
	          Boolean bIsNodePar=false;
	          
	          for(int k=0;k<vCond.size();k++) {
	        	  NodoCondizione nodo = vCond.get(k);	        	 
	        	  
	        	  if (nodo.getType()==TYPE_CONDITION_PARAM) {	   
	        		  bIsNodePar=true;
	        		  String nomeParametro = extractParameterName(nodo.getCondizione());
	        		  
	        		  if (nomeParametro.equals("")) continue;
	        		  
	        		  nomeParametro=nomeParametro.replace("@","");
	        		  /*System.out.println(nomeParametro);
	        		  System.out.println(request.getParameter(nomeParametro));*/
	        		  
	        		  //Se finisce con il _dal, il parametro è quello prima
	        		  String realName = nomeParametro;
	        		  if ((nomeParametro.substring(nomeParametro.length() - 4)).toUpperCase().equals(_DAL_SUFFIX_SELECT)) 
	        			  realName=realName.substring(0,realName.length() - 4);
	        				  	        				  	        		  
	        		  if  ((nvl(request.getParameter(realName),"")).equals("")) {	       
	        			  tree.info.vCondizioni.get(k).setBMark(true);	        			 
	        			  continue;
	        		  }  
	        		  
	        		  try {
	        		    String newCondition=bindingExpression(nodo.getCondizione(),"@"+nomeParametro);
	        		    
	        		    if (newCondition.equals("")) {
	        		    	tree.info.vCondizioni.get(k).setBMark(true);	        		    	
	        		    	continue;
	        		    }
	        		    
	        		    bToDelete=false;
	        		    tree.info.vCondizioni.get(k).setCondizione(newCondition);
	        		  }
	        		  catch (Exception e) {
	        			throw new Exception("Errore in valutazione parametro "+nomeParametro+" per condizione "+nodo.getCondizione()+"\nErrore: "+e.getMessage());
	        		  }	        		 
	        	  }
	        	  
	          }
	          
	          if (bIsNodePar==false) bToDelete=false;
	          tree.info.setBMark(bToDelete);
	          tree.info.setIsNodePar(bIsNodePar);
	          
	          //cerco di capire se tutti i figli sono NON parametrici o cancellabili e in tal caso segno il booleano
	          boolean bTuttiFigliCancellabiliOrNonParametrici=true;
	          Iterator iFigli=tree.figli.iterator();
	          while(iFigli.hasNext() ) {
	        	   NTL figlio = (NTL) iFigli.next();
	        	   
	        	   if ((figlio.info.isBMark() || !figlio.info.isNodeParam()) && figlio.info.isBTuttiFigliCancellabiliOrNonParametrici() )  {
	        		   bTuttiFigliCancellabiliOrNonParametrici=true;
	        	   }
	        	   else {
	        		   bTuttiFigliCancellabiliOrNonParametrici=false;
	        		   break;
	        	   }	        	   
	          }
	          
	          tree.info.setBTuttiFigliCancellabiliOrNonParametrici(bTuttiFigliCancellabiliOrNonParametrici);
	          
 	   }	
	   
	   private void makeSqlFromTree(NTL tree, Vector from, Vector where) throws Exception {
	          if(tree==null) return; 		    
	          
	          
	          if (!tree.bRoot && tree.info.isBMark() && tree.info.isBTuttiFigliCancellabiliOrNonParametrici()) {
	        	 //Lo salto.... non è radice, è eliminabile, tutti i figli sono o cancellabili o non parametrici	        	  
	          }
	          else {
	        	 //Lo inserisco: inserisco la sua tabelle e tutte le sue condizioni marcate con delete=false
	        	 NodoTabella node = tree.info;
	        	 //System.out.println("Tabella-->" + node.getNomeTabella()+" "+node.getAliasTabella());	        	 
	        	 from.add(node.getNomeTabella()+" "+node.getAliasTabella());
	        	 
	        	 for(int k=0;k<node.vCondizioni.size();k++) {
	        		 NodoCondizione nCond = node.vCondizioni.get(k);
	        		 
	        		 if (!nCond.isBMark()) {
	        			 //System.out.println("Condizione-->" +nCond.getCondizione());
	        			 where.add(nCond.getCondizione());
	        		 }
	        			 
	        	 }
	        	 
	        	 //System.out.println("\n_______________________________________________\n");
	        	 
	        	 Iterator i=tree.figli.iterator();
		          while(i.hasNext())
		        	    makeSqlFromTree((NTL) i.next(),from,where);
	          }
	          	          
	          
	   }
	   
	   private String extractParameterName(String condition) {
			   int indiceInizioParametro,indiceFineParametroSpace,indiceFineParametroAt,indiceFineParametro;
				   String nomeParametro;
				   				   
				   if (!condition.substring(condition.length()-1).equals(_SPACE)) 
					   condition+=_SPACE;
				   
				   indiceInizioParametro=condition.indexOf(_TAG_PARAMETER);
				   
				   
				   
				   //Se non esiste il parametro allora è una condizione non parametrica
				   //quindi va lasciata così com'è
				   if (indiceInizioParametro==-1) {		   			   
					   return "";		   		   		   		   
				   }
					   
				   indiceFineParametroSpace=condition.indexOf(_SPACE,indiceInizioParametro);
				   indiceFineParametroAt=condition.indexOf(_APICE,indiceInizioParametro);			   		  
				   
				   
			   	   if (indiceFineParametroAt!=-1 && indiceFineParametroAt<indiceFineParametroSpace)
				    	indiceFineParametro=indiceFineParametroAt;
				   else if (indiceFineParametroSpace!=-1)
				    	indiceFineParametro=indiceFineParametroSpace;
				   else
				    	indiceFineParametro=0;
			   	   
			 
				   if (indiceFineParametro==0)
				    	nomeParametro=condition.substring(indiceInizioParametro).replace("\n","").replace("\r","");
				   else
				    	nomeParametro=condition.substring(indiceInizioParametro,indiceFineParametro ).replace("\n","").replace("\r","");
			   
				   
				   return nomeParametro;
	   }
	   
	   private String nvl(String campo, String valore) {
		       if (campo==null) return valore;
		    
		       return campo;
	   }	  	   
	   	  
}

package it.finmatica.dmServer.motoreRicerca;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.utility.DateUtility;
import it.finmatica.log4jsuite.LogDb;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import it.finmatica.dmServer.util.ManageConnection;

public class GD4_Gestione_Query 
{
  private String jndiPar=null;
  private String idQuery;
  private String sFiltro;
  private String sFullText="";
  private String sFullObjText="";
  private String sFullObjOCRText="";
  private String nomeQuery,tipoQuery;  
  private GD4_Condizioni conds; 
  private Vector parametri = new Vector(); 
  private Environment varEnv;
  private IDbOperationSQL dbOp; 
  private int queryTimeOut=0;
  private String Master="0";
  private String CatMaster="";
  private boolean bIsNew=false;  
  private boolean bProtectReserveParameter=false;

  private int joinCounter=1;
  //Condizioni di Ricerca
  private Vector vTipiDoc,vTipiDocArea, vCampi, vCampiOrdinamento;
  private String sArea, sAnd, sOr, sCategoria, sListaCMF="";
  private String sAreaReturn=null, sCmReturn=null, sCatReturn=null;
  
  private String sSelect=null;
  private boolean casoCategoriaorSelect=false;

  public boolean bAlive=false;
  
  private AbstractSearch ric=null;
  
  private Properties pSessionParameter= new Properties();
  
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
   * True  - Resultset di ricerca esaurito
   * False - Esiste ancora almeno un record in ricerca 
   *         successivo all'ultimo elemento estratto
  */
  private boolean bIsLastRow = false;    
  
  /**
   * Variabile per la gestione dei log su DB
  */   
  private LogDb log4jSuiteDb = null;  

  public GD4_Gestione_Query(String filtro, Environment env) 
  {
         sFiltro=(String)filtro;
         this.varEnv=env;
  }

  public GD4_Gestione_Query(long idQuery,String filtro, Environment env) throws Exception
  {
	     this(filtro,env);
	     this.idQuery = ""+idQuery;	     
         try {   	  	
 	        leggiDBQuery(true);
 	     } catch (Exception e)
 	     {
 	          throw new Exception("GD4_Gestione_Query::\n " + e.getMessage()); 
 	     }
  }
  
  public GD4_Gestione_Query(long idQuery,Environment env) throws Exception
  {
		 this(idQuery,env,false);
  }
  
  public GD4_Gestione_Query(long idQuery,Environment env,boolean bReserve) throws Exception
  {
	  	this.bProtectReserveParameter=bReserve;
	    this.idQuery = ""+idQuery;
	    this.varEnv=env;
	    
	    try {   	  	
	        leggiDBQuery(false);
	        parseQuery();
	    } catch (Exception e)
	    {
	          throw new Exception("GD4_Gestione_Query::\n " + e.getMessage()); 
	    }
  } 
  
  public GD4_Gestione_Query(long idQuery,Environment env,boolean bReserve, boolean bParse) throws Exception
  {
	  	this.bProtectReserveParameter=bReserve;
	    this.idQuery = ""+idQuery;
	    this.varEnv=env;
	    
	    try {   	  	
	        leggiDBQuery(false);
	        if (bParse) parseQuery();
	    } catch (Exception e)
	    {
	          throw new Exception("GD4_Gestione_Query::\n " + e.getMessage()); 
	    }
  } 
  
  public void setJndiPar(String jndi) {
	  this.jndiPar=jndi;
  }
  
  public void setSessionParameter(String par, String val){
	  	 pSessionParameter.put(par,val);
  }
   
  
  
  public boolean isParametrica() 
  {
         if (parametri.size() > 0) 
            return true;
         else
            return false;
  }
  
  public void setAndCondition(String and) {	  	 	     
	     if (sAnd!=null)
	    	 sAnd=sAnd+" "+and;
	     else
	    	 sAnd=and;
  }
  
  public void setFullTextCondition(String text) {	  	 
	     if (text==null) { 
	    	 sFullText=null;
	         return;
	     }
	     
	     if (sFullText!=null && !sFullText.equals(""))
	    	 sFullText=sFullText+" "+text;
	     else
	    	 sFullText=text;
  }  
  
  public void setFullTextObjCondition(String text) {	  	 
	  	 sFullObjText=text;	     	     
  }    
  
  public void setFullTextObjOCRCondition(String text) {	  	 
	  	 sFullObjOCRText=text;	     	     
}   
  
  public void setProtectReserveParameter(boolean bFlag) {
	     bProtectReserveParameter=bFlag;
  }
  
  public String getTableValoriHtmlForQuery() throws Exception
  {
        // DATI
        String sHtml = "";
        
        dbOp = connect();
        
        String primaTab = "<table class=\"AFCFormTable\">";
        String primoBlocco = "";
        String secondoBlocco = "";
        
        //TIPO DOCUMENTO
        for(int i=0;i<vTipiDoc.size();i++)
        {
          primoBlocco += "<tr>"+"\n";
          primoBlocco += "<td class=\"AFCFieldCaptionTD\"><b>Codice Modello:</b>"+"</td> <td class=\"AFCDataTD\" colspan=\"2\">"+ vTipiDoc.get(i)  +"</td>";
          primoBlocco += "</tr>"+"\n";
        }
        
        primoBlocco += "<tr>\n";
        primoBlocco += "<td  colspan=\"3\" >Valori fissi:</td>";
        primoBlocco += "</tr>";
        
        varEnv.connect();
        LookUpDMTable lkup= new LookUpDMTable(varEnv);
        
        //CAMPI
        for(int i=0; i<vCampi.size(); i++){
           String sOp=((keyval)vCampi.get(i)).getOperator();
           
           if (sOp==null) sOp="contains";
           
           String key=((keyval)vCampi.get(i)).getKey();
           
           key=lkup.lookUpLabelDato(sArea,key,null);
        
           if (sOp.equals("uguale") || sOp.equals("contains"))
        	   sOp="=";
           if (((keyval)vCampi.get(i)).getTipoDaClient().equals("Between")) {
        	   primoBlocco += "<tr>"+"\n";
               primoBlocco += "<td class=\"AFCFieldCaptionTD\" width=\"40%\">" + key + "</td>";
               primoBlocco += "<td class=\"AFCFieldCaptionTD\" width=\"20%\"  align=\"center\">Compreso fra</td>";
               primoBlocco += "<td class=\"AFCDataTD\" width=\"40%\">" + ((keyval)vCampi.get(i)).getVal() +" e "+ sOp + "</td>";
               primoBlocco += "</tr>"+"\n";
           }
           else {
        	   
        	   String operatoreDaVisualizzare;
        	   String valoreDaVisualizzare;
        	   
        	   operatoreDaVisualizzare=sOp;
        	   valoreDaVisualizzare=((keyval)vCampi.get(i)).getVal();
        	   
        	   if (valoreDaVisualizzare.equals("is null")) {
        		   operatoreDaVisualizzare="nullo";
        		   valoreDaVisualizzare="";
        	   }
        	   
        	   if (valoreDaVisualizzare.equals("is not null")) {
        		   operatoreDaVisualizzare="non nullo";
        		   valoreDaVisualizzare="";
        	   }        	   
        	   
        	   primoBlocco += "<tr>"+"\n";
        	   primoBlocco += "<td class=\"AFCFieldCaptionTD\" width=\"40%\">" + key + "</td>";
        	   primoBlocco += "<td class=\"AFCFieldCaptionTD\" width=\"20%\"  align=\"center\">" + operatoreDaVisualizzare + "</td>";
        	   primoBlocco += "<td class=\"AFCDataTD\" width=\"40%\">" + valoreDaVisualizzare + "</td>";
        	   primoBlocco += "</tr>"+"\n";
           }
        }
        
        //AND
        if (sAnd != null)
        {
             secondoBlocco += "<tr>"+"\n";
             secondoBlocco += "<td class=\"AFCFieldCaptionTD\" width=\"40%\"> Tutte le parole: "  +"</td> <td class=\"AFCFieldCaptionTD\">&nbsp;</td><td class=\"AFCDataTD\">"+ sAnd +"</td>";
             secondoBlocco += "</tr>"+"\n";
        }
        
        //OR
        if (sOr != null)
        {
             secondoBlocco += "<tr>"+"\n";
             secondoBlocco += "<td class=\"AFCFieldCaptionTD\" width=\"40%\"> Alcune delle parole: " +"</td> <td class=\"AFCFieldCaptionTD\">&nbsp;</td><td class=\"AFCDataTD\">"+ sOr +"</td>";
             secondoBlocco += "</tr>"+"\n";
        }
      
        sHtml +="<div id=\"valori\">";
        if (!primoBlocco.equals("") || !secondoBlocco.equals(""))
        {
           sHtml+=primaTab+primoBlocco+secondoBlocco;
        }
        sHtml+= "</div>";
        
        //System.out.println("TableValori="+sHtml);
        varEnv.disconnectClose();

        return   sHtml;
        
  }
  
  public void setFetchSize(int size) {
         fetchSize=size;
  }
  
  public void setFetchInit(int init) {
         fetchInit=init;
  }
  
  public boolean isLastRowFetch() {
         return bIsLastRow;
  }
  
  
  public String lookUpLabelDato(String area,String dato, IDbOperationSQL dbOpLocal) throws Exception
  {        
	   String sRet=null;
	   
          	   
      try {
           StringBuffer sStm = new StringBuffer();
                          
           sStm.append("select F_LABEL_DATO('"+area+"','"+dato+"') from DUAL");
           
           ResultSet rst;             
           	dbOpLocal.setStatement(sStm.toString());
           	dbOpLocal.execute();

           rst = dbOpLocal.getRstSet();
           	
           if (rst.next()) {
           	sRet=rst.getString(1);                	
           }
           else
           	sRet=dato;

       }
       catch (Exception e) {
           close();
           throw new Exception("Gd4GestQuery::lookUpLabelDato\n" + e.getMessage());
           
        }
      return sRet;
  }
  
  
  public String createPageHtmlForQuery() throws Exception
  {
        ParametriQuery pq = null;
        // DATI
        String sHtml = "";
        String bloccoParametri="";
        IDbOperationSQL dbOpLocal = null;
        dbOpLocal = SessioneDb.getInstance().createIDbOperationSQL(this.jndiPar,0);       
        	
        
        //dbOp = connect();
         LookUpDMTable lkup= new LookUpDMTable(varEnv);
        
        //PARAMETRI
        bloccoParametri += "<table border=\"1\" class=\"AFCFormTable\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" id=\"Parametri\">"+"\n";
        for(int i=0; i<parametri.size(); i++){
        	bloccoParametri += "<tr>"+"\n";
           pq = (ParametriQuery)parametri.get(i);
           
           String par, ope;
                     
           if (pq.getParametro().equals("AND")){
        	   par="Tutte le parole:  ";
        	   ope="";
           }
           else if (pq.getParametro().equals("OR")) {
        	   par="Alcune delle parole:  ";
        	   ope="";
           }
           else if (pq.getParametro().equals("SINGLE")) {
        	   par="Tutte le parole:  ";
        	   ope="";
           }
           else {
        	   par=pq.getParametro();
        	    
        	   par=lkup.lookUpLabelDato(sArea,par,dbOpLocal);
        	   ope=pq.getOperatore();
               if (ope.equals("uguale") || ope.equals("contains"))
            	   ope="=";   
           }                             
          
           bloccoParametri += "<td class=\"AFCFieldCaptionTD\" >"+par+"<input name=\"idQuery\" type=\"hidden\" value=\""+this.idQuery+"\"></input></td>";
           
           if (ope.trim().equals("Between")) 
        	   bloccoParametri += "<td class=\"AFCFieldCaptionTD\" align=\"center\">&nbsp;nell'intervallo&nbsp;</td>";
           else
        	   bloccoParametri += "<td class=\"AFCFieldCaptionTD\" align=\"center\">&nbsp;" + ope + "&nbsp;</td>";
                      
           String sSelectSceltaTipoUguaglianza="";
           
           if (ope.equals("=")) {
        	   sSelectSceltaTipoUguaglianza="&nbsp;&nbsp;";
        	   sSelectSceltaTipoUguaglianza+="<select class=\"AFCSelect\" size=\"1\" name=\"SceltaUguaglianza_"+pq.getChiave()+"\">";
           	   sSelectSceltaTipoUguaglianza+="  <option selected value=\"LIKE\">Inizia per</option>";
           	   sSelectSceltaTipoUguaglianza+="  <option value=\"CONTAINS\">Contiene</option>";
           	   sSelectSceltaTipoUguaglianza+="  <option value=\"ESATTA\">Frase Esatta</option>";
           	   sSelectSceltaTipoUguaglianza+="</select>";
           }
           
           if (ope.trim().equals("Between")) {
        	   String colonna=pq.getColonna();
        	   String valore1,valore2;
      
        	   valore1=colonna.substring(0,colonna.indexOf("$"));        	   
        	   valore2=colonna.substring(colonna.indexOf("$")+1,colonna.length());
        	   
        	   if (valore1.equals(":@")) valore1="";
        	   if (valore2.equals(":@")) valore2="";
        	   
        	   bloccoParametri += "<td class=\"AFCDataTD\" >";
        	   bloccoParametri += "<input id=\"" + pq.getChiave()    + "_beetw1\" class=\"AFCInput\" name=\"" + pq.getChiave()    + "_beetw1\" value=\""+valore1+"\" align=\"right\" size=\"22%\">";
        	   bloccoParametri += "&nbsp;,&nbsp;<input id=\"" + pq.getChiave()    + "_beetw2\" class=\"AFCInput\" name=\"" + pq.getChiave()    + "_beetw2\" value=\""+valore2+"\" align=\"right\" size=\"22%\">";
        	   bloccoParametri += "</td>";
           }
           else {
        	   if (ope.equals("=") && pq.getSelectItem()!=null) {
        		   StringTokenizer st = new StringTokenizer(pq.getSelectItem(),"#");
        		   
        		   bloccoParametri += "<td class=\"AFCDataTD\" ><select class=\"AFCSelect\" size=\"1\" name=\""+pq.getChiave()+"\">";
        		   int index=0;
        		   while (st.hasMoreTokens()) {
        			     String elemento = st.nextToken();
        			     String selected="selected";
        			     
        			     if (index++>0) selected="";
        			     bloccoParametri+="  <option "+selected+" value=\""+elemento.substring(0,elemento.indexOf("@"))+"\">"+elemento.substring(elemento.indexOf("@")+1)+"</option>";
        			     
        		   }
        		   bloccoParametri += "</select></td>";
        	   }
        	   else
        		   bloccoParametri += "<td class=\"AFCDataTD\"><input id=\"" + pq.getChiave()    + "\" class=\"AFCInput\" value=\"\" name=\"" + pq.getChiave()    + "\" align=\"right\" size=\"49\">"+sSelectSceltaTipoUguaglianza+"</td>";
           }	   
        	   
           bloccoParametri += "</tr>"+"\n";
        }       
        
        bloccoParametri += "</table>";
        
        sHtml +="<table>";
        sHtml +="<tr><td><p align=\"center\"><strong><img src=\"images/qrysearch.gif\" border=\"0\"></img>&nbsp;"+this.getNomeQuery()+"</strong></p></td></tr>"; 
        sHtml +="<tr><td><p><strong>Inserimento Parametri:</strong></p>"+ bloccoParametri+ "</td></tr>";
        sHtml+="</table>";
        
        //System.out.println("TableParametri="+sHtml);
        
       // varEnv.disconnectClose();
        try{dbOpLocal.close();}catch(Exception e){}
         dbOpLocal=null;        

        return   sHtml;
        
  } 
  
  public void setEnvironment(Environment env)
  {
         this.varEnv=env;
  }
  
  public String getFrasiParametriche() throws Exception
  {
        String str = "";
        ParametriQuery pq = null;
        for(int i=0; i<parametri.size(); i++){
           pq = (ParametriQuery)parametri.get(i);
           str += pq.getParametro()+pq.getOperatore()+pq.getChiave()+"\n"; 
        }
        return   str;
        
  }
  public Vector getParametryQuery() throws Exception
  {
         return   parametri;
        
  }
   
  public keyval setCondizioniParametriche(String chiave, String valore) throws Exception {
	     return setCondizioniParametriche(chiave,valore,null);
  }
  
  public keyval setCondizioniParametriche(String chiave, String valore, String tipoUguaglianza) throws Exception
  {
	   keyval k;
       int i = 0;  
     
       
       String colonna = "", operatore = "", version="";
       String area=null, cm=null, categoria=null;
       if (chiave.startsWith("PAR_CAMPI")){
          i = Integer.parseInt(Global.lastTrim(chiave, "PAR_CAMPI",varEnv.Global.WEB_SERVER_TYPE));
          colonna = ((ParametriQuery) parametri.elementAt(i)).getColonna();
          operatore = ((ParametriQuery) parametri.elementAt(i)).getOperatore();
          version = ((ParametriQuery) parametri.elementAt(i)).getVersion();
          area = ((ParametriQuery) parametri.elementAt(i)).getArea();
          cm   = ((ParametriQuery) parametri.elementAt(i)).getCm();
          categoria  = ((ParametriQuery) parametri.elementAt(i)).getCategoria();          
          
          if (valore.indexOf("$")!=-1) {
        	  colonna=((ParametriQuery) parametri.elementAt(i)).getParametro();
        	  operatore=valore.substring(valore.indexOf("$")+1,valore.length());
        	  valore=valore.substring(0,valore.indexOf("$"));        	  
          }
          
          //Trattasi di stringa
          if (operatore.equals("uguale") || (operatore.equals("=") && version.equals("1.0")) )  {
        	  k = new keyval(colonna,valore);
        	  k.setTipoUguaglianza(tipoUguaglianza);
        	  if (area!=null) {
        		  k.setArea(area);
        		  k.setCm(cm);
        	  }
        	  if (categoria!=null) 
        		  k.setCategoria(categoria);
        	  
        	  
          }        	  
          //Campo numerico o data o between
          else {
        	  /*System.out.println("-->"+colonna);
        	  System.out.println("-->"+valore);
        	  System.out.println("-->"+operatore);*/
        	  k = new keyval(colonna,valore,null,operatore);      
        	  if (area!=null) {
        		  k.setArea(area);
        		  k.setCm(cm);
        	  }
        	  if (categoria!=null) 
        		  k.setCategoria(categoria);        	  
          }

          vCampi.addElement(k);         
          
          return k;   
          //conds.addInListaCondizioniCampi(colonna, valore, operatore); 
       }
       else {
          /*if (chiave.startsWith("PAR_DATI")){
             i = Integer.parseInt(Global.lastTrim(chiave, "PAR_DATI",varEnv.Global.WEB_SERVER_TYPE));
             colonna = ((ParametriQuery) parametri.elementAt(i)).getColonna();
             operatore = ((ParametriQuery) parametri.elementAt(i)).getOperatore();

             conds.addInListaCondizioniDati(colonna, valore, operatore);  
          }
          else {*/
             i = Integer.parseInt(Global.lastTrim(chiave, "PAR_COND",varEnv.Global.WEB_SERVER_TYPE));
             colonna = ((ParametriQuery) parametri.elementAt(i)).getColonna();             
             
             if (Integer.parseInt(colonna)==Global.COND_AND) sAnd=valore;
             if (Integer.parseInt(colonna)==Global.COND_OR) sOr=valore;
             //Compatibilità con filtro 1.0 (v. 2.5 in giù)
             if (Integer.parseInt(colonna)==Global.COND_SINGLE) sAnd=valore;
             
             ///conds.addCondizione(Integer.parseInt(colonna), valore);  
          }     
          
          return null;
  }
  
  public String getDBQuery() throws Exception
  {
        /*ric = new Ricerca(conds,varEnv);
        if (sStati.equals("") )
           return ric.getQuery();
        else
            return  ric.getQueryStato(sStati); */
	  return "";
  }
  
  public Vector risultatoQuery() throws Exception {
	     return risultatoQuery(false);
  }
  
  public String getSqlQuery() throws Exception {
	     if (ric==null) return null;
	     
	     return ric.getLastQueryExecuted();
  }
  
  public String getSqlCountQuery() throws Exception {
	     if (ric==null) return null;
	     
	     if (ric.getLastQueryExecuted()==null) return null;
	     
	     return "Select count(*) from ("+ric.getLastQueryExecuted()+")";
  }
  
  public Vector risultatoQuery(boolean bTipoDoc) throws Exception
  {
	  
	  	 int tipoRicerca;	  	 	  	 
	  	
	  	 //Nel caso di marco o daniela uso ancora la Classe RicercaFinmatica
	  	 if (casoCategoriaorSelect)
	  		tipoRicerca=0;
	  	 else	  		 
	  		 tipoRicerca=(new LookUpDMTable(varEnv)).lookUpTipoRicercaHorV(sArea,vTipiDoc,vTipiDocArea,vCampi,vCampiOrdinamento,null,null,null);
         
         
         if (tipoRicerca==1)
    	     ric = new HorizontalSearch(sArea,vTipiDoc,vTipiDocArea,varEnv);
         else if (tipoRicerca==0)
    	     ric = new RicercaFinmatica(sArea,vTipiDoc,vTipiDocArea,varEnv);
         //Caso solo area e campi anonimi (non cm)
         else if (tipoRicerca==-2) {
         //Mi calcolo i modelli dell'area e li inserisco
    	     Vector vCm = (new LookUpDMTable(varEnv)).lookUpElencoCodiciModelliOrizzontaliByArea(sArea);
    	    
    	     if (vCm.size()==0) {
        	     throw new Exception("La ricerca non ha prodotto alcun risultato");
    	     }
    	    
    	     for (int i=0;i<vCm.size();i++) {
    	    	 vTipiDoc.add(""+vCm.get(i));
	             vTipiDocArea.add(sArea);
    	     }    	     	
    	    
    	     ric = new HorizontalSearch(sArea,vTipiDoc,vTipiDocArea,varEnv);
         }         
         else {
    	     throw new Exception("I modelli inseriti in ricerca non sono compatibili tra di loro.\nInserire o tutti modelli orizzontali o tutti verticali!");
         }         
	     
	     ric.setCampi(vCampi);
	     ric.setCampiOrdinamento(vCampiOrdinamento);
	     
	     if (sAreaReturn!=null) 
	    	 ric.setTypeModelReturn(sAreaReturn,sCmReturn);	     
	     else if (sCatReturn!=null)
	    	 ric.setTypeModelReturn(sCatReturn);
	     
	     //Se non sono col master setto la condizione 
	     //di filtro fulltext della workarea come aggiunta
	     //alla condizione and
	    // if (!Master.equals("1")) {
	     
	     String sTextFullToFunction=null;
	     String sAllegati=" ";
	     String sFullTextCheck=" ";
	     String sAllegatiOCR=" ";
	     String sFullTextModulistica=null;
	     
	    // ric.setObjFileCondition()
	     if (sFullObjText!=null && !sFullObjText.equals("")) {
	    	 	/*keyval k = new keyval();
	   			
	   			k.setKey(sFullObjText);
	   			k.setCm("");
	   			k.setArea("");	 
	   			k.setCategoria("");
	   			k.setNoCaseSensitive(false);*/
	   			
	   			sTextFullToFunction=sFullObjText;
	   			sAllegati="Y";
	   			
	   			/*Vector v = new Vector();
	   			v.add(k);
	   			ric.setObjFileCondition(v);*/
	     }
	     
	     if (sFullObjOCRText!=null && !sFullObjOCRText.equals("")) {
	    	 	/*keyval k = new keyval();
	   			
	   			k.setKey(sFullObjOCRText);
	   			k.setCm("");
	   			k.setArea("");	 
	   			k.setCategoria("");
	   			k.setNoCaseSensitive(false);
	   			k.setIsOcr(true);*/
	   			
	   			sTextFullToFunction=sFullObjOCRText;
	   			sAllegatiOCR="Y";
	   			
	   			/*Vector v = new Vector();
	   			v.add(k);
	   			ric.setObjFileCondition(v);*/
	     }
	     
	     if (sFullText==null || sFullText.equals("")){
	    	// if (Global.nvl(sSelect, "").equals("")) ric.setCondizioneAnd(sAnd);
	     }	    	 
	     else {		    	 
	    	 /*if (sAllegati.equals(" ")) sTextFullToFunction=sFullText;*/
	    	 /*if (Global.nvl(sSelect, "").equals(""))  {
	    		 if (sAnd==null)
		    		 ric.setCondizioneAnd(sFullText);
		    	 else
		    		 ric.setCondizioneAnd(sAnd+" "+sFullText);		    	
	    	 }*/
	    		 	    	 
	    	 sTextFullToFunction=sFullText;
	    	 sFullTextCheck="Y";
	     }
	    // }
	     //Se sono col master la aggiungo a parte
	     //per poter fare la query più esterna
	    /* else {
	    	 ric.setCondizioneAnd(sAnd);
	    	 ric.setCondFiltroWAreaCasoMaster(sFullText);
	     }*/
	     if (sTextFullToFunction!=null)	    {
	    	 sTextFullToFunction.replaceAll("'", "''");
	    	 sFullTextModulistica="F_FILTRO_FULLTEXT_WAREA(id,'"+sTextFullToFunction+"','"+sAllegati+"','"+sAllegatiOCR+"','"+sFullTextCheck+"')" ;
	     }
	     
	     
    	 ric.setCondizioneOr(sOr);

    	 ric.setFetchInit(fetchInit);
         ric.setFetchSize(fetchSize);

	     ric.setRicercaWeb(true);

    	 ric.setMaster(Master.equals("1"));
    	 ric.setCatMaster(CatMaster); 
    	 
    	 if (sFullTextModulistica!=null) {
    		 if (Global.nvl(sSelect, "").equals(""))  
    			 ric.setCondizioneFullTextWArea(sTextFullToFunction,sAllegati,sAllegatiOCR,sFullTextCheck);
    		 else
    			 sSelect+=" AND "+sFullTextModulistica+"='1'";
    	 }
    	     		 
    	 ric.setSqlSelect(sSelect);
    	     	    	 
    	  if (queryTimeOut!=0) 
			  ric.setTimeOut((int)(queryTimeOut/1000));
		  else 
			  ric.setTimeOut((int)(varEnv.Global.QUERY_TIMEOUT/1000));    	      	 
    	  
          try {
        	 varEnv.connect();
        	 if (ric instanceof HorizontalSearch)
            	 log4jSuiteDb = new LogDb(varEnv.getUser(),                   
            			                  varEnv.Global.CATEGO_RICERCA_SEMPLICE_DAWEB_HORIZ,
            			                  varEnv.getDbOp().getConn());
        	 else
            	 log4jSuiteDb = new LogDb(varEnv.getUser(),                   
            			                  varEnv.Global.CATEGO_RICERCA_SEMPLICE_DAWEB_VERT,
            			                  varEnv.getDbOp().getConn());        	
        	 
        	 ric.setLog4JSuite(log4jSuiteDb);
        	 varEnv.disconnectClose();
          }                                       
          catch(Exception e) {         
        	 throw new Exception("Errore creazione LOG\n"+e.getMessage());	 
          }	  
    	  
    	  try {
	   	      ric.ricerca();		 
			  
	   	      /*if (queryTimeOut!=0) 
	   	    	  ric.join(queryTimeOut);	   	    		   	      
	   	      else 
	   	    	  ric.join(varEnv.Global.MSECALIVE);  	      
	   	      
			  while (!varEnv.Global.bExitThreadRicerca) {
	
			       	if (ric.isAlive()) {  
			       		bAlive=true;        	       		
			       		ric.resetDocumentList();        						
			       		break;
			        }
			  }
			  
			  varEnv.Global.bExitThreadRicerca=true;
			  
			  if (!bAlive && ric.getError()!=null)          					
			      throw new Exception(ric.getError());
	    	      			
	          
			  */
	          bIsLastRow=ric.isLastRowFetch();
	          
	          if (bTipoDoc)
	        	  return ric.getDocumentListWithIdTipoDoc();
	          else
	        	  return ric.getDocumentList();
    	  }    
	      catch(Exception e) {                	
           	   if (e.getMessage().indexOf("ORA-01013")!=-1) {
           		   log4jSuiteDb.ScriviLog("Esecuzione Ricerca","Tempo max di esecuzione della query raggiunto!",
                				          Global.TAG_RICERCA_SEMPLICE_TIMEOUTSQL,
                				          LogDb.ERROR_LEVEL);           		   
                   bAlive=true;        	       		
			       ric.resetDocumentList();
			       return ric.getDocumentList();
           	   }
        	   log4jSuiteDb.ScriviLog("Esecuzione Ricerca",e.getMessage(),
        				              Global.TAG_RICERCA_SEMPLICE_GENERRORSQL,
        				              LogDb.ERROR_LEVEL);            	   
           	   throw new Exception(ric.getError());
          }            
  }
  
 /* public Object risultatoXMLDocument() throws Exception
  {
          ric = new Ricerca(conds,varEnv);
        if (sStati.equals("") )
           ric.fillDocumentList();
        else
           ric.fillDocumentListStato(sStati);
        return ric.getXMLDocument();
  }
  
  public String risultatoXMLString() throws Exception
  {
        ric = new Ricerca(conds,varEnv);
        if (sStati.equals("") )
           ric.fillDocumentList();
        else
           ric.fillDocumentListStato(sStati);  
        return ric.getXMLString();
  }*/

  public String htmlResult(IDbOperationSQL dbOp, String sServlet) throws Exception
  {
         StringBuffer sHtml = new StringBuffer();
         String sElement, sHRef;
         try {
            Vector vDoc = risultatoQuery();
                               
            sHtml.append("<html>\n");
            sHtml.append("<head>\n");
            sHtml.append("<title>Elenco Documenti</title>\n");
            sHtml.append("</head>\n");
            sHtml.append("<body>\n");
            sHtml.append("<ul>\n");       
            sHtml.append("<p align=\"center\"><font face=\"Verdana\">Risultati Ricerca</font></p>");
            sHtml.append("<p>&nbsp;</p>");
          
            for(int i =0;i<vDoc.size();i++) {
                 sElement =vDoc.elementAt(i).toString();
                 sHRef="<a href=\""+sServlet+"\""+sElement+"\" target=\"_blank\">";
                 sHtml.append("<li>"+sHRef+sElement+"</a></li>\n");
            }    
          
            sHtml.append("</ul>\n");
            sHtml.append("</body>\n");
            sHtml.append("</html>\n");         
            
         } catch (Exception e)
         {
              throw new Exception("GD4_Gestione_Query::htmlResult errore:\n " + e.getMessage()); 
         }
         return sHtml.toString();
  }
  
  public String getNomeQuery()
  {
    return nomeQuery;
  }

  public String getTipoQuery()
  {
    return tipoQuery;
  }

  public Vector getTipoDoc()
  {
         return vTipiDoc;
  }
  
  public String getListaCMF()
  {
    return sListaCMF;
  }
  
  public String getListaTipiDoc()
  {		 String sLista;
  
  		 sLista="(' '";
	  	
	  	 for (int i=0;i<vTipiDoc.size();i++) {
	  		sLista+=",'"+vTipiDoc.get(i)+"'";
	  	 }
	  	 
	  	 sLista+=")";
	  	 
	  	 return sLista;
	  		 
  }  
  
  public String getListaTipiDocCMF() {
  		 String sLista="";
  		 String listaCMFSelezionati="";
  		 String[] seq = null;
  		 
  		 listaCMFSelezionati = getCMFselezionati();
  	  		 
  	  	 if(!listaCMFSelezionati.equals(""))
  	       seq = listaCMFSelezionati.split(",");    
  	     
  	  	 //Nel caso di ricerca modulistica
  	  	 if(this.sFiltro.indexOf("RICERCAMODULISTICA_")!=-1) {
  	  		 for(int j=0;j<seq.length;j++){
  	  			sLista+=",'"+seq[j].substring(0,seq[j].indexOf("@"))+"'";
  	  		 }
  	  	 }
  	  	 else {
	  	  	 for (int i=0;i<vTipiDoc.size();i++) {
	  		  		String id = vTipiDoc.get(i).toString();
	  		  		String cmf="";
	  		  		
	  		  		if(seq!=null)
	  		  		 cmf = ricercaCMF(seq,id);
	  		  		
	  		  		if(cmf.equals(""))
	  		  		 sLista+=",'"+vTipiDoc.get(i)+"'";
	  		  		else
	  		  		 sLista+=",'"+cmf+"'";	
	  		 }
  	  	 } 	
  	  	 
  		 if(!sLista.equals(""))
          sLista="(' '"+sLista+")";
	  	
  		return sLista;
	  		 
  }  
 
  public String getArea()
  {
         return sArea;
         
  }
  
  public String getCategoria()
  {
         return sCategoria;
         
  }  
  
  public String getMaster()
  {
         return Master;
         
  }  
  
  public String getCatMaster()
  {
         return CatMaster;
         
  }  
  
  public String getRichiesta()
  {
         if (conds.getRichiesta()!=null)
             return conds.getRichiesta().toString();         
         else
             return null;
         
  }

  public Vector getDati()
  {
    return conds.getListaCondizioniDati();
  }   

  public Vector getCampi()
  {
    return vCampi;//conds.getListaCondizioniCampi();
  }
  
  public Vector getOrdinamenti()
  {
    return vCampiOrdinamento;//conds.getListaCondizioniCampi();
  }     
  
  public String getCondizione(String cond)
  { 
	  if (cond.equals("AND")) return sAnd;
	  if (cond.equals("OR")) return sOr;
	  if (cond.equals("TIMEOUT")) return queryTimeOut+"";
	  
	  return "";
    //return conds.getCondizione(tipoCondizione(cond));
  } 

  private String getCMFselezionati() {
	      String sequenza = "";
	      String token="RICERCAMODULISTICA_";
	      
	      //Ricerca di tipo modulistica
	      if(this.sFiltro.indexOf(token)!=-1){
	    	  if(sFiltro.indexOf("|")!=-1) 
	    	   sequenza = sFiltro.substring(sFiltro.indexOf("|")+1,sFiltro.length());
	    	  else
	    	   sequenza = "";
	      }
	      else // Ricerca standard 
	      {
	    	sequenza = sListaCMF;  
	      }
	      return sequenza;
  }  
  
  private String ricercaCMF(String[] lista,String id){
		  String cmf="";
		  String cmp;
		  
	      for(int i=0;i<lista.length;i++){
			  cmf = lista[i].substring(0,lista[i].indexOf("@"));
			  cmp = lista[i].substring(lista[i].indexOf("@")+1,lista[i].length());
			  
			  if(id.equals(cmp))
			   return cmf;	  
				
		  }
	      return "";
  }
  
  
  
  private void leggiDBQuery(boolean bOnlyName)  throws Exception
  {
                       
        try {
              StringBuffer sStm = new StringBuffer();

              
              
              dbOp = connect();

              sStm.append("select filtro,nome,tipo  from query");
              sStm.append(" where id_query = " + this.idQuery);

              dbOp.setStatement(sStm.toString());
              dbOp.execute();  

              ResultSet rst = dbOp.getRstSet();

              if ( rst.next() ) {
            	 if (!bOnlyName)
            		 this.sFiltro = rst.getString(1);
                 this.nomeQuery = rst.getString(2);
                 this.tipoQuery = rst.getString(3);
              }              
              
              close();

            }
            catch (Exception e) {
               close();
               throw new Exception("Gestione_Query::leggiDBQuery errore:\n " + e.getMessage());
            }
  }
  
  private String getIstruzioneCategoria() throws Exception
  {
	      String sIstruzione=null;
                       
          try {
              StringBuffer sStm = new StringBuffer();              
              
              dbOp = connect();

              sStm.append("select istruzione from categorie ");
              sStm.append("where categoria = '" + sCategoria +"'");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();  

              ResultSet rst = dbOp.getRstSet();

              if ( rst.next() ) {
                 sIstruzione = rst.getString(1);               
              }              
              
              close();
              return sIstruzione;
            }
            catch (Exception e) {
               close();
               throw new Exception("Gestione_Query::getIstruzioneCategoria errore:\n " + e.getMessage());
            }
  } 
  
  public void parseQuery() throws Exception
  {	  	
	   
        	     	  
        if (this.sFiltro==null || this.sFiltro.equals("<?xml version='1.0'encoding='UTF-8' ?><DOC_INFO xmlns:xsi='http://www.w3.org/2000/10/XMLSchema-instance' xsi:noNamespaceSchemaLocation='doc_info_v1.4.1.xsd'></DOC_INFO>")) return;
                
        try {        
              String  dato, campo,   value, operatore,tipo=null, dummy, tipoCond, ordinamento;
              String area=null, cm=null, categoria=null;
              String area1=null, cm1=null, categoria1=null;
              String area2=null, cm2=null, categoria2=null;
              String nvlValue=null;
              int condiz, j = 0;
              String sWhereCategoriesCase="";
              String sIstruzioneCategoria="";
              
              
              vTipiDoc = new Vector();
              vTipiDocArea = new Vector();
              vCampi = new Vector();
              vCampiOrdinamento = new Vector();

              DocumentBuilder builder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
              Document doc = null;
              try{
                InputSource inStream = new InputSource();
                
                inStream.setCharacterStream(new StringReader(this.sFiltro));
                
                doc = builder.parse(inStream);
                
              }
              catch(Exception e){ return;}
      
              NodeList nodes;
  
              //********************************Recupero da XML la lista dei tipi documento
              nodes = doc.getElementsByTagName("TIPO_DOC");   
                            
              for (int i = 0; i < nodes.getLength(); i++) {   
            	  String version;
            	  if (nodes.item(i).getAttributes().getNamedItem("version")!=null)
            		  version=nodes.item(i).getAttributes().getNamedItem("version").getNodeValue();
            	  else
            		  version="1.0";
            	  dato=nodes.item(i).getAttributes().getNamedItem("value").getNodeValue();
            	  
            	  //Se la versione è la 1.0 (quindi dal DMServer 2.5 in giù)
            	  //il tipo doc è passato tramite ID quindi devo fare la conversione 
            	  //con il nome            	  
            	  if (version.equals("1.0")) {
            		  int iTipoDoc;
            		  //Per sicurezza faccio il cast per capire se è un numerico
            		  try{iTipoDoc=Integer.parseInt(dato);} catch(Exception e){iTipoDoc=-1;}
            		  
            		  if (iTipoDoc!=-1) {
            			  dato=(new DocUtil(varEnv)).getCodiceModelloFromIdTipoDoc(dato);            			  
            		  }            			  
            	  }
            	  //FINE CONTROLLO VECCHIA VERSIONE
            	  //System.out.println("dato-->"+dato);
            	  vTipiDoc.add(dato); 
            	  //Per adesso setto l'area sempre vuota...da cambiare
            	  vTipiDocArea.add(""); 
              }
  
              //********************************Recupero da XML l'area
              nodes = doc.getElementsByTagName("AREA");     
              if (nodes.item(0) != null) {
                 sArea=nodes.item(0).getAttributes().item(0).getNodeValue();              
              }    
              
              sCategoria=null;
              //CASO DANIELA TAG SELECT
              nodes = doc.getElementsByTagName("CATEGORIA");
              if (nodes.item(0) != null) {
            	  sCategoria=nodes.item(0).getAttributes().item(0).getNodeValue();                	  
              }
              
              nodes = doc.getElementsByTagName("MASTER");     
              if (nodes.item(0) != null) {            	              	  
                 Master=nodes.item(0).getAttributes().getNamedItem("value").getNodeValue(); // item(0).getNodeName(); // getNodeValue();     
                 try {
                   CatMaster=nodes.item(0).getAttributes().getNamedItem("categoriamaster").getNodeValue();
                 }
                 catch(NullPointerException nle ) {
                	 CatMaster="";
                 }
              }                  
              
              //********************************Recupero da XML la lista CMF
              nodes = doc.getElementsByTagName("CMF");     
              if (nodes.item(0) != null) {
                 sListaCMF=nodes.item(0).getAttributes().item(0).getNodeValue();              
              }                 
              
              
              //********************************Recupero da XML i campi              
              nodes = doc.getElementsByTagName("CAMPI"); 
              
              //System.out.println("lungh nodo campi-->"+nodes.getLength());
              
              for (int i = 0; i < nodes.getLength(); i++) {
            	  // System.out.println("ENTRO");
            	  nvlValue=null;
            	  String version, tipoUguaglianza=null, selectItem=null;
            	  if (nodes.item(i).getAttributes().getNamedItem("version")!=null)
            		  version=nodes.item(i).getAttributes().getNamedItem("version").getNodeValue();
            	  else
            		  version="1.0";
            	  
            	  campo=nodes.item(i).getAttributes().getNamedItem("campo").getNodeValue();
                  dummy = nodes.item(i).getAttributes().getNamedItem("value").getNodeValue();
                  operatore=nodes.item(i).getAttributes().getNamedItem("oper").getNodeValue().replace('#','<');                  
                  
                  if (nodes.item(i).getAttributes().getNamedItem("area")!=null)
                	  area=nodes.item(i).getAttributes().getNamedItem("area").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("cm")!=null)
                	  cm=nodes.item(i).getAttributes().getNamedItem("cm").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("categoria")!=null)
                	  categoria=nodes.item(i).getAttributes().getNamedItem("categoria").getNodeValue();                  
                  
                  if (cm==null && categoria==null && sCategoria!=null) categoria=sCategoria;
                  
                  if (nodes.item(i).getAttributes().getNamedItem("tipo")!=null)
                	  tipo= nodes.item(i).getAttributes().getNamedItem("tipo").getNodeValue();

                  if (nodes.item(i).getAttributes().getNamedItem("tipoUguaglianza")!=null)
                	  tipoUguaglianza= nodes.item(i).getAttributes().getNamedItem("tipoUguaglianza").getNodeValue();   
                  
                  if (nodes.item(i).getAttributes().getNamedItem("nvlvalue")!=null)
                	  nvlValue=nodes.item(i).getAttributes().getNamedItem("nvlvalue").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("selectItem")!=null) 
                	  selectItem=nodes.item(i).getAttributes().getNamedItem("selectItem").getNodeValue();
                  
                  // System.out.println("CAMPO*****************-->"+campo);
                  if (dummy.indexOf(":@")!=-1) {           //siamo in presenza di un parametro          
                	  if (version.equals("1.0") && operatore.equals("=")) operatore="uguale"; 
                	  //Caso Between
                      if (dummy.indexOf("$")!=-1) 
                    	  parametri.addElement(creaFraseParametrica("PAR_CAMPI"+j++, campo, dummy,  operatore,version,area,cm,categoria,selectItem));
                      else	  
                    	  parametri.addElement(creaFraseParametrica("PAR_CAMPI"+j++, campo, campo,  operatore,version,area,cm,categoria,selectItem));
                      value="PAR_CAMPI"+i;                      
                                          	
                  }
                  else
                      value = dummy;

                  if (   ((campo != null)  || (!campo.equals("") ))                     
                      && dummy.indexOf(":@")==-1 ) {
                      
                      /*if (dummy.toUpperCase().equals(":UTENTE")) 
                         conds.addInListaCondizioniCampi((new LookUpDMTable(varEnv)).lookUpCampi(campo, idtipo), varEnv.getUser(), operatore);
                      else   
                         conds.addInListaCondizioniCampi((new LookUpDMTable(varEnv)).lookUpCampi(campo, idtipo), value, operatore);
                      
                      
                      ((CondizioniCampi)conds.getListaCondizioniCampi().lastElement()).setNomeCampo(campo);*/
                /*  System.out.println("campo--->"+campo);
                  System.out.println("valore--->"+dummy);
                  System.out.println("op--->"+operatore);
                  System.out.println("tipo--->"+tipo);
                  System.out.println("tipoUg--->"+tipoUguaglianza);*/    
                  
                      if (sCategoria!=null) {
                    	  if (i == nodes.getLength()-1)
                    		  sWhereCategoriesCase+=getConditionCategoriesCase(campo,dummy,operatore,tipo);
                    	  else
                    		  sWhereCategoriesCase+=getConditionCategoriesCase(campo,dummy,operatore,tipo)+" AND ";
                      }
 
                	  keyval k;
                      /*System.out.println("campo-->"+campo);
                      System.out.println("value-->"+value);
                      System.out.println("operatore-->"+operatore);
                      System.out.println("tipo-->"+tipo); */

                	  //Trattasi di stringa
                	  if (operatore.equals("uguale") || (version.equals("1.0") && operatore.equals("=")) ) {
                		  if (   (value.toUpperCase().equals(":UTENTE") ||
                				  value.toUpperCase().equals(":USER")) &&
                				  !bProtectReserveParameter
                		      ) {
                			    value=varEnv.getUser();
                		  		
                		  	  }
                		  
                		  if (!bProtectReserveParameter) value=parseSessionVariable(value);
                		  
                		  k = new keyval(campo,value);
                		  k.setTipoDaClient("uguale");
                	  }
                	  else if (operatore.equals("Between")) {
                		  String val1, val2;
                		  val1=value.substring(0,value.indexOf("$"));
                		  val2=value.substring(value.indexOf("$")+1,value.length());
                		  
                		  if (!bProtectReserveParameter) {
	                		  val1=parseSessionVariable(val1);
	                		  val2=parseSessionVariable(val2);
                		  }
                		  
                		  k = new keyval(campo,val1,null,val2);
                		  k.setTipoDaClient("Between");
                	  }
                	  else if (operatore.equals("is null") || operatore.equals("is not null")) {
                		  if (tipo.equals("S")) tipo=null;                   		
                		  k = new keyval(campo,operatore,null,tipo);
                		  k.setTipoDaClient(operatore);
                	  }
                	  //Campo numerico o data
                	  else {
                		  
                		  if (!bProtectReserveParameter)  value=parseSessionVariable(value);
                		  
                		  k = new keyval(campo,value,null,operatore);
                		  k.setTipoDaClient("altro");
                	  }
                	  
                	  if (area!=null) {
                		  k.setArea(area);
                	  	  k.setCm(cm);
                	  }
                	  else if (categoria!=null) 
                		  k.setCategoria(categoria);
                	  else if (sCategoria!=null)
                		  k.setCategoria(sCategoria);
                	  
                	  if (nvlValue!=null) {
                		  if (   (nvlValue.toUpperCase().equals(":UTENTE") ||
                				  nvlValue.toUpperCase().equals(":USER")) &&
                				  !bProtectReserveParameter
                		      )  {
                			  nvlValue=varEnv.getUser();
                			  
                		  }    
                		  
                		  if (!bProtectReserveParameter)  nvlValue=parseSessionVariable(nvlValue);
                		  
                		  k.setValueNvl(nvlValue);
                	  }
                	  
                	  if (tipoUguaglianza!=null) 
                		  k.setTipoUguaglianza(tipoUguaglianza);

                	  vCampi.addElement(k);

                	  area=null;
                	  cm=null;
                	  categoria=null;                	  

                  }
                 // System.out.println("ESCO");                             
                                                      
              }    
              
              //********************************Recupero da XML i campi di Join          
              nodes = doc.getElementsByTagName("JOIN"); 
              
              //System.out.println("lungh nodo campi-->"+nodes.getLength());
              
              for (int i = 0; i < nodes.getLength(); i++) {
            	  String campo1=null,campo2=null;
            	  
            	  area1=null;
                  cm1=null;
                  categoria1=null;
                  area2=null;
                  cm2=null;
                  categoria2=null; 
            	  
            	  if (nodes.item(i).getAttributes().getNamedItem("campo1")!=null)
                	  campo1=nodes.item(i).getAttributes().getNamedItem("campo1").getNodeValue();
            	  else
            		  continue;

            	  if (nodes.item(i).getAttributes().getNamedItem("campo2")!=null)
                	  campo2=nodes.item(i).getAttributes().getNamedItem("campo2").getNodeValue();
            	  else
            		  continue;            	  
            	  
                  if (nodes.item(i).getAttributes().getNamedItem("area1")!=null)
                	  area1=nodes.item(i).getAttributes().getNamedItem("area1").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("cm1")!=null)
                	  cm1=nodes.item(i).getAttributes().getNamedItem("cm1").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("categoria1")!=null)
                	  categoria1=nodes.item(i).getAttributes().getNamedItem("categoria1").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("area2")!=null)
                	  area2=nodes.item(i).getAttributes().getNamedItem("area2").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("cm2")!=null)
                	  cm2=nodes.item(i).getAttributes().getNamedItem("cm2").getNodeValue();
                  
                  if (nodes.item(i).getAttributes().getNamedItem("categoria2")!=null)
                	  categoria2=nodes.item(i).getAttributes().getNamedItem("categoria2").getNodeValue();
                  
                  keyval k = new keyval(campo1,"DONTCARE");
				  k.setArea(area1);
				  k.setCm(cm1);
				  k.setCategoria(categoria1);
				  k.setIndexJoin(joinCounter);
				      
				  vCampi.add(k);	
				      
				  keyval k2 = new keyval(campo2,"DONTCARE");
				  k2.setArea(area2);
				  k2.setCm(cm2);
				  k2.setCategoria(categoria2);
				  k2.setIndexJoin(joinCounter);
				      
				  vCampi.add(k2);	
				      
				  joinCounter++;
				      				  
              }
              
              //********************************Recupero da XML i campi di Join          
              nodes = doc.getElementsByTagName("TYPERETURN"); 
              
              //System.out.println("lungh nodo campi-->"+nodes.getLength());
              
              for (int i = 0; i < nodes.getLength(); i++) {
            	   if (nodes.item(i).getAttributes().getNamedItem("area")!=null) {
            		   sAreaReturn=nodes.item(i).getAttributes().getNamedItem("area").getNodeValue();
            		   sCmReturn=nodes.item(i).getAttributes().getNamedItem("cm").getNodeValue();
            	   }
            	   
            	   if (nodes.item(i).getAttributes().getNamedItem("categoria")!=null) 
            		   sCatReturn=nodes.item(i).getAttributes().getNamedItem("categoria").getNodeValue();
              }
                               
              nodes = doc.getElementsByTagName("ORDINAMENTO"); 
              
              //System.out.println("lungh nodo ordina-->"+nodes.getLength());
              
              for(int i = 0; i < nodes.getLength(); i++) {
            	  campo = nodes.item(i).getAttributes().getNamedItem("campo").getNodeValue();
                  ordinamento = nodes.item(i).getAttributes().getNamedItem("ordinamento").getNodeValue();                                    
                                                      
                  if (nodes.item(i).getAttributes().getNamedItem("area")!=null || 
                	  nodes.item(i).getAttributes().getNamedItem("categoria")!=null) {
                	  keyval k = new keyval(); 
                	  k.setKey(campo+"@"+ordinamento);
                	  
                	  if (nodes.item(i).getAttributes().getNamedItem("area")!=null) {
                		  k.setArea(nodes.item(i).getAttributes().getNamedItem("area").getNodeValue());
                		  k.setCm(nodes.item(i).getAttributes().getNamedItem("cm").getNodeValue());
                	  }
                	  else
                		  k.setCategoria(nodes.item(i).getAttributes().getNamedItem("categoria").getNodeValue());
                	  
                	  vCampiOrdinamento.add(k);
                  }
                  else
                	  vCampiOrdinamento.add(campo+"@"+ordinamento);
              }
              
              //Recupero dei campi di RETURN
              nodes = doc.getElementsByTagName("RETURN");
              
              for(int i = 0; i < nodes.getLength(); i++) {
              	  campo = nodes.item(i).getAttributes().getNamedItem("campo").getNodeValue();
           	      
              	  keyval k = new keyval();
           	      
              	  k.setKey(campo+"@DONTCARE");
           	      
              	  if (nodes.item(i).getAttributes().getNamedItem("area")!=null || 
                	  nodes.item(i).getAttributes().getNamedItem("categoria")!=null) {
              		  area=nodes.item(i).getAttributes().getNamedItem("area").getNodeValue();
              		  cm=nodes.item(i).getAttributes().getNamedItem("cm").getNodeValue();
              		  
              		  if (controllaVettoreCampoOrdinamento(campo,area,cm,null,"DONTCARE")) continue;
              		  
              		  k.setArea(area);
				      k.setCm(cm);
				      k.setCampoReturn(k.ISCAMPO_RETURN);
              	  }
              	  else {
              		  categoria=nodes.item(i).getAttributes().getNamedItem("categoria").getNodeValue();
              		  
              		  if (controllaVettoreCampoOrdinamento(campo,null,null,categoria,"DONTCARE")) continue;
              		                		  
				      k.setCategoria(categoria);				      
				      k.setCampoReturn(k.ISCAMPO_RETURN);
              	  }
              	  
              	  vCampiOrdinamento.add(k);
              	  
              }

              //********************************Recupero da XML le Condizioni
              nodes = doc.getElementsByTagName("CONDIZIONI");     
              for (int i = 0; i < nodes.getLength(); i++) {
                  tipoCond = nodes.item(i).getAttributes().item(0).getNodeValue();
                  condiz = tipoCondizione(tipoCond);
                  dummy = nodes.item(i).getAttributes().item(1).getNodeValue();
                  if (dummy.equals(":@")) {           //siamo in presenza di un parametro
                      parametri.addElement(creaFraseParametrica("PAR_COND"+j++, tipoCond, condiz+"","contains","",null,null,null,null));
                      value="PAR_COND"+i;
                  }
                  else
                      value = dummy;
                  
                  if ((condiz >= 0) && !dummy.equals(":@")) {
                     /* if (dummy.toUpperCase().equals(":UTENTE")) 
                          conds.addCondizione(condiz , varEnv.getUser());
                      else                      
                          conds.addCondizione(condiz , value);*/
                	  if (condiz==Global.COND_AND) sAnd=value;
                	  if (condiz==Global.COND_OR) sOr=value; 
                      
                	  //Compatibilità con filtro 1.0 (v. 2.5 in giù)
                	  if (condiz==Global.COND_SINGLE) sAnd=value; 
                	  
                	  if (condiz==Global.COND_TIMEOUT) queryTimeOut=Integer.parseInt(value);
                	  
                  }
              }    
              
              //********************************Recupero da XML la SELECT (CASO MARCO)
              nodes = doc.getElementsByTagName("SELECT");  
              boolean bSelect=false;
              for (int i = 0; i < nodes.getLength(); i++) {       
            	  casoCategoriaorSelect=true;
            	  bSelect=true;
            	  sSelect=Global.replaceAll(Global.replaceAll(nodes.item(i).getAttributes().item(0).getNodeValue(),"&gt;",">"),"&lt;","<");
            	  sSelect=sSelect.replaceAll(":UtenteGDM",varEnv.getUser().toUpperCase());
              }
              
              //********************************USO COME TAG SELECT LA CATEGORIA (CASO IBRIDO)
              if (sCategoria!=null && bSelect) {
            	  casoCategoriaorSelect=true;
            	  String sPrimaUnion, sSecondaUnion;
	        	  
            	  sIstruzioneCategoria=getIstruzioneCategoria();
            	  
            	  if (sIstruzioneCategoria==null || sIstruzioneCategoria.equals("")) 
            		  throw new Exception("E' stata specificata nel filtro di ricerca una categoria senza istruzione o inesistente");
            	  
	        	  sIstruzioneCategoria=sIstruzioneCategoria.toUpperCase();
	        	  
	        	  sPrimaUnion=sIstruzioneCategoria.substring(0,sIstruzioneCategoria.indexOf("UNION ALL"));
	        	  sSecondaUnion=sIstruzioneCategoria.substring(sIstruzioneCategoria.indexOf("UNION ALL")+("UNION ALL".length()));
	              
	        	  if (!(sWhereCategoriesCase.equals(""))) {		        	  
		        	  if (sPrimaUnion.indexOf("WHERE")>0)
		        		  sWhereCategoriesCase=" AND "+sWhereCategoriesCase;
		        	  else
		        		  sWhereCategoriesCase=" WHERE "+sWhereCategoriesCase;
		        	  
		        	  sSelect=sPrimaUnion+sWhereCategoriesCase+" UNION ALL "+sSecondaUnion;		        	  
	        	  }
	        	  else
	        		  sSelect=sIstruzioneCategoria;
	        	 
	        	  sSelect="SELECT ID,TI,DA,CR FROM ("+sSelect+"),DUAL ";
	              
	              StringBuffer sSqlCompetenze = new StringBuffer("");
	              
	              sSqlCompetenze.append("  WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',");
	 			  sSqlCompetenze.append("  								    ID,");
	 			  sSqlCompetenze.append("  								    'L',");
	 			  sSqlCompetenze.append("  								    '"+varEnv.getUser()+"',");
	 			  sSqlCompetenze.append("  								    F_TRASLA_RUOLO('"+varEnv.getUser()+"','GDMWEB','GDMWEB'),");
	 			  sSqlCompetenze.append("  								    TO_CHAR(SYSDATE,'dd/mm/yyyy'))||DUMMY = '1X' ");
	 			  sSqlCompetenze.append("");
	              
	 			  sSelect+=sSqlCompetenze.toString();	        	  
             }
              
             if (sCategoria!=null){	
        	    vTipiDoc.clear();
        	    vTipiDocArea.clear();
             }
             
         }
         catch (Exception e) {
        	 
              throw new Exception("GD4_Gestione_Query::parseQuery errore:\n " + e.getMessage()); 
              
         }
  }
  
  private String parseSessionVariable(String var) {
	      String sRet=var;
	      
	      if (var.indexOf(":")!=-1 && pSessionParameter.containsKey(var.substring(1)))
	    	  sRet=pSessionParameter.getProperty(var.substring(1));
	      
	      return sRet;
  }
  
  private ParametriQuery creaFraseParametrica(String chiave, String parametro, String colonna, String operatore,String version, String area,String cm,String categoria, String selectItem)
  {
    ParametriQuery pq = new ParametriQuery(chiave,  parametro, colonna, operatore, "", version,area,cm, categoria);
    pq.setSelectItem(selectItem);
    return pq;
  }

  private int tipoCondizione(String cond)
  {
    if (cond.equals("AND"))
      return Global.COND_AND;
    else if (cond.equals("OR"))
      return Global.COND_OR;
    else if(cond.equals("NOT"))
      return Global.COND_NOT;
    else if(cond.equals("SINGLE"))
      return Global.COND_SINGLE;
    else if(cond.equals("TIMEOUT"))
      return Global.COND_TIMEOUT;    
    else    	
      return 0;
  }
  
	  private IDbOperationSQL connect() throws Exception {
	        if (varEnv.getDbOp()==null) {
	           bIsNew=true;
	           return (new ManageConnection(varEnv.Global)).connectToDB();
	        }
	        
	        return varEnv.getDbOp();
	  }
	  
	  private void close() throws Exception {
	        if (bIsNew) (new ManageConnection(varEnv.Global)).disconnectFromDB(dbOp,true,false);        
	  }
  
	  
   //Verifica se esiste la chiave nel vettore di ordinamento
   //ed eventualmente la aggiorna per evitare di doppiarla
   //quando si usa la addCampoReturn
   private boolean controllaVettoreCampoOrdinamento(String sKey,String area, String cm, String categoria, String tipoOrdinamento) {
	       for(int i=0;i<vCampiOrdinamento.size();i++) {
	    	   
	    	   if (vCampiOrdinamento.get(i) instanceof String) continue;
	    	   
	    	   String chiave = ((keyval)vCampiOrdinamento.get(i)).getKey();
	    	   
	    	   //Gli tolgo il pezzo dopo la @
	    	   chiave=chiave.substring(0,chiave.indexOf("@"));
	    	   
	    	   if (chiave.equals(sKey)) {
	    		   //Ho trovato la chiave nel vettore
	    		   if (
	    				 (area==null && cm==null && categoria==null) ||    			  
	    			     (area!=null && (((keyval)vCampiOrdinamento.get(i)).getArea()).equals(area)  &&
	    				                ((keyval)vCampiOrdinamento.get(i)).getCm().equals(cm) ) ||
	    			     (categoria!=null && (((keyval)vCampiOrdinamento.get(i)).getCategoria()).equals(categoria) )) {
	    			   	
	    			     keyval k = ((keyval)vCampiOrdinamento.get(i));  
	    			     
	    			     k.setCampoReturn(k.ISCAMPO_ORDINAMENTO_AND_RETURN);
	    			     
	    			     //Vengo da una addCampoOrdinamentoDesc o addCampoOrdinamentoAsc
	    			     if (tipoOrdinamento.equals("ASC") || tipoOrdinamento.equals("DESC")) 
	    			    	 k.setKey(chiave+"@"+tipoOrdinamento);	    			    
	    			    
	    			     vCampiOrdinamento.set(i,k);
	    			     
	    			     return true;
	    		   }
	    	   }
	       }
	       
	       return false;
   }	  
   
   private String getConditionCategoriesCase(String campo,String valore,String operatore,String tipo) {
	       String sCondition="";
	       
	       //E' SICURAMENTE UN CAMPO DI TIPO STRINGA
	       if (operatore.equals("uguale")) {
	    	  if (valore.indexOf("%")!=-1)
		   	     sCondition=" "+ campo +" like '"+Global.replaceAll(transformReserveKey(valore),"'","''")+"' ";
		      else
		         sCondition=" "+ campo +" = '"+Global.replaceAll(transformReserveKey(valore),"'","''")+"' "; 
	       }	       
	       else  {
	    	   //SE IS NULL O IS NOT NULL LO METTO SUBITO IN CONDIZIONE
	    	   if (operatore.equals("is null") || operatore.equals("is not null")) {
	    		   sCondition=" "+ campo +" "+operatore+" ";
	    	   }
	    	   else {
	    		   String valore1=valore,valore2=null;
	    		   //SE L'OPERATORE E' BETWEEN SPLITTO IL VALORE DOPPIO SEPARATO DA $
	    		   if (operatore.equals("Between")) {	    			   
	    			   valore1=valore.substring(0,valore.indexOf("$"));        	   
	    			   valore2=valore.substring(valore.indexOf("$")+1,valore.length());	    			   	    			   
	    		   }
	    		   
	    		   //TESTO SE SONO NUMERI O DATE
	    		   if (DateUtility.isDateValid(valore1,"dd/mm/yyyy")) {
	    			   sCondition=" "+ campo +" "+operatore+" to_date('"+valore1+"','dd/mm/yyyy') ";
	    			   if (valore2!=null) 
	    				   sCondition+="and to_date('"+valore2+"','dd/mm/yyyy') ";
	    		   }	
	    		   else {
	    			   sCondition=" "+ campo +" "+operatore+" "+valore1+" ";
	    			   if (valore2!=null) 
	    				   sCondition+="and "+valore2+" ";
	    		   }
	    	   }
	       }
	       
	       return sCondition;
   }
   
   private String transformReserveKey(String value) {
	       String sValueRet=value;
	       
	       if (value.toUpperCase().equals(":UTENTE") || 
 			   value.toUpperCase().equals(":USER"))
	    	   sValueRet=varEnv.getUser();
	       
	       return sValueRet;
   }
	  
}

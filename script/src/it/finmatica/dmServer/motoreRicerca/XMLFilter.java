package it.finmatica.dmServer.motoreRicerca;

import it.finmatica.dmServer.util.Global;

import java.util.Vector;

public class XMLFilter {
	   private final String XML_HEAD="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	   private final String XMLNODE_HEAD="<DOC_INFO xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"doc_info_v1.4.1.xsd\">";
	   private final String XMLNODE_TAIL="</DOC_INFO>";
	   
	   private String area; 
	   private String categoria;
	   private String categoriaMaster; 
	   private Vector tipoDoc;
	   private Vector campi;
	   private Vector campiRA;
	   private Vector campiOrdinamento;
	   private Vector campiOrdinamentoRA;
	   private Vector joinCondition;
	   private String andCondition;
	   private String orCondition;
	   private String timeOut;
	   private String master;
	   private String listaCMF;
	   
	   public XMLFilter() {
	    	  tipoDoc = new Vector();
		      campi = new Vector();
		      campiRA = new Vector();
		      campiOrdinamento = new Vector();
		      campiOrdinamentoRA = new Vector();
		      joinCondition = new Vector();
	   }
	   
	   public XMLFilter(String newarea,String newtimeOut,String newmaster) {
		      this();
		      area=newarea;
		      timeOut=newtimeOut;
		      master=newmaster;		      
	   }
	   
	   public void setArea(String newArea) {
		      area=newArea;
	   }
	   
	   public void setCategoria(String newCategoria) {
		      categoria=newCategoria;
	   }	   
	   
	   public void setMaster(String newmaster) {
		      master=newmaster;
	   }
	   
	   public void setCategoriaFromMaster(String newCategoria) {
		      categoriaMaster=newCategoria;
	   }

	   public void setTimeOut(String newtimeout) {
		      timeOut=newtimeout;
	   }
	   
	   public void addTipoDoc(String tipoDocumento) {
		      tipoDoc.add(tipoDocumento);
	   }
	   
	   public void addCampo(String campo, String tipo,  String valore, String valorenvl, String operatore) {		      
		   campi.add(new Value(campo,tipo,valore,valorenvl,operatore));
	   }
	  	   
	   public void addCampo(String area,String cm,String categoria,String campo, String tipo, String valore, String valorenvl, String operatore) {		      
		      campiRA.add(new Value(area,cm,categoria,campo,tipo,valore,operatore,valorenvl));
	   }
	   
	   public void addCampoOrdinamento(String campo,  String ordinamento) {
		      campiOrdinamento.add(new Value(campo,ordinamento));
	   }
	   
	   public void addCampoOrdinamento(String area,String cm,String categoria,String campo,String ordinamento) {
		      campiOrdinamentoRA.add(new Value(area,cm,categoria,campo,ordinamento));
	   }	
	   
	   public void setAndCondition(String and) {
		      andCondition=and;
	   }

	   public void setOrCondition(String or) {
		      orCondition=or;
	   }
	   
	   public void setJoinCondition(String area1,String cm1,String categoria1,String campo1,String tipo1,String area2,String cm2,String categoria2,String campo2,String tipo2) {
		      joinCondition.add(new Value(area1,cm1,categoria1,campo1,tipo1,area2,cm2,categoria2,campo2,tipo2));
	   }
	   
	   public void setListaCMF(String newlistaCMF) {
		      listaCMF=newlistaCMF;
	   }
	   
	   
	   public String doFilter() {
		      StringBuffer sXMLFilter = new StringBuffer("");
		      
		      sXMLFilter.append(XML_HEAD);
		      sXMLFilter.append(XMLNODE_HEAD);
		      
		      /**INSERIMENTO AREA**/
		      if (area!=null)
		    	  sXMLFilter.append("<AREA value=\"" + area + "\" />");
		      
		      /**INSERIMENTO CATEGORIA**/
		      if (categoria!=null)
		    	  sXMLFilter.append("<CATEGORIA value=\"" + categoria + "\" />");
		      
		      /**INSERIMENTO TIPI DOCUMENTO**/
		      for(int i=0;i<tipoDoc.size();i++) 
		    	  sXMLFilter.append("<TIPO_DOC version=\"1.1\" value=\"" + tipoDoc.get(i) + "\" />");
		      
		      /**INSERIMENTO LISTA CODICE MODELLI FIGLLI**/
		      if (listaCMF!=null && !listaCMF.equals(""))
		    	sXMLFilter.append("<CMF value=\"" + listaCMF + "\" />");
		      
		      /**INSERIMENTO CAMPI**/
		      for(int i=0;i<campi.size();i++) {
		    	  String sTipo,sValoreNVL;		    	  
		    	  
		    	  if ((((Value)campi.get(i)).getVal(2).equals(""))) 
		    		   sTipo="";
		    	  else
		    		   sTipo=" tipo=\""  + ((Value)campi.get(i)).getVal(2) +	"\"";
		    	 
		    	  if ((((Value)campi.get(i)).getVal(4).equals(""))) 
		    		sValoreNVL="";
		    	  else
		    		sValoreNVL=" nvlvalue=\""  + ((Value)campi.get(i)).getVal(4) +	"\"";
		    	
		    	  
		    	  sXMLFilter.append("<CAMPI version=\"1.1\" " + sValoreNVL + " campo=\"" + 
		    			                    ((Value)campi.get(i)).getVal(1) + "\"" +		    			                   
		    			                    sTipo +   			                    
		    			                    " value=\"" + ((Value)campi.get(i)).getVal(3) + 
		    			                    "\" oper=\""+ ((Value)campi.get(i)).getVal(5)+"\" />");
		      }
		      
		      /**INSERIMENTO CAMPI RICERCA AVANZATA**/
		      for(int i=0;i<campiRA.size();i++) {
		    	  String sTipo;
		    	  String sArea;
		    	  String sCM;
		    	  String sCategoria;
		    	  String sValoreNVL;
		    	  
		    	  if ((!(((Value)campiRA.get(i)).getVal(1).equals("")))  && (!(((Value)campiRA.get(i)).getVal(2).equals(""))))
		    	  {	  
		    		 sArea=" area=\"" + ((Value)campiRA.get(i)).getVal(1) +"\"";
		    		 sCM=" cm=\"" + ((Value)campiRA.get(i)).getVal(2) +"\"";
		    	  }   
		    	  else
		    	  {
		    		  sArea="";
		    	      sCM=""; 
		    	  }
		    	  
		    	  if(!(((Value)campiRA.get(i)).getVal(3).equals("")))
		    	    sCategoria=" categoria=\"" + ((Value)campiRA.get(i)).getVal(3) +"\"";
		    	  else
		    		sCategoria="";
		    	  
		    	  if ((((Value)campiRA.get(i)).getVal(9).equals(""))) 
		    		sValoreNVL="";
		    	  else
		    		sValoreNVL=" nvlvalue=\""  + ((Value)campiRA.get(i)).getVal(9) +	"\"";		    	  
		    	  
		    	  
		    	  if ((((Value)campiRA.get(i)).getVal(6).equals(""))) 
		    		   sTipo="";
		    	  else
		    		   sTipo=" tipo=\""  + ((Value)campiRA.get(i)).getVal(6) +	"\"";
		    	 
		    	  sXMLFilter.append("<CAMPI version=\"1.1\" campo=\"" + 
		    			                    ((Value)campiRA.get(i)).getVal(5) + "\"" +		    			                   
		    			                    sTipo +  
		    			                    sCategoria +
		    			                    sArea +
		    			                    sCM +
		    			                    sValoreNVL +
		    			                    " value=\"" + ((Value)campiRA.get(i)).getVal(7) + 
		    			                    "\" oper=\""+ ((Value)campiRA.get(i)).getVal(8)+"\" />");
		      }
		      
		      /**INSERIMENTO CAMPI ORDINAMENTO**/
		      for(int i=0;i<campiOrdinamentoRA.size();i++) 
		      {	  
		    	  String sArea;
		    	  String sCM;
		    	  String sCategoria;
		    	  
		    	  if ((!(((Value)campiOrdinamentoRA.get(i)).getVal(1).equals("")))  && (!(((Value)campiOrdinamentoRA.get(i)).getVal(2).equals(""))))
		    	  {	  
		    		 sArea=" area=\"" + ((Value)campiOrdinamentoRA.get(i)).getVal(1) +"\"";
		    		 sCM=" cm=\"" + ((Value)campiOrdinamentoRA.get(i)).getVal(2) +"\"";
		    	  }   
		    	  else
		    	  {
		    		  sArea="";
		    	      sCM=""; 
		    	  }
		    	  
		    	  if(!(((Value)campiOrdinamentoRA.get(i)).getVal(3).equals("")))
			        sCategoria=" categoria=\"" + ((Value)campiOrdinamentoRA.get(i)).getVal(3) +"\"";
			      else
			    	sCategoria="";
		    	
		    	  sXMLFilter.append("<ORDINAMENTO campo=\"" +     ((Value)campiOrdinamentoRA.get(i)).getVal(4) + 
		    			                    "\" ordinamento=\"" + ((Value)campiOrdinamentoRA.get(i)).getVal(5) + "\"" +		    			                   
		    			                    sCategoria +
		    			                    sArea +
		    			                    sCM +
		    			                    " />");
		      }
		      
		      /**INSERIMENTO CAMPI ORDINAMENTO**/
		      for(int i=0;i<campiOrdinamento.size();i++) 
		    	  sXMLFilter.append("<ORDINAMENTO campo=\"" +     ((Value)campiOrdinamento.get(i)).getVal(1) + 
		    			                    "\" ordinamento=\"" + ((Value)campiOrdinamento.get(i)).getVal(2) + "\" />");
		      
		      /**INSERIMENTO JOIN**/
		      for(int i=0;i<joinCondition.size();i++) {
		    	  String join="";
		    	  
		    	  if ((!((Value)joinCondition.get(i)).getVal(1).equals("")) && (!((Value)joinCondition.get(i)).getVal(2).equals(""))) 
		    		join+=" area1=\""+((Value)joinCondition.get(i)).getVal(1)+"\""+" cm1=\""+((Value)joinCondition.get(i)).getVal(2)+"\"";
		    	  else
		    		if ((!((Value)joinCondition.get(i)).getVal(3).equals("")))
		    			join+=" categoria1=\""+((Value)joinCondition.get(i)).getVal(3)+"\"";
		    	 
		    	  join+=" campo1=\""+((Value)joinCondition.get(i)).getVal(4)+"\"";
		    	  if (!((Value)joinCondition.get(i)).getVal(5).equals("")) 
		    		  join+=" tipo1=\""+((Value)joinCondition.get(i)).getVal(5)+"\"";
		    	  
		    	  if ((!((Value)joinCondition.get(i)).getVal(6).equals("")) && (!((Value)joinCondition.get(i)).getVal(7).equals(""))) 
			    		join+=" area2=\""+((Value)joinCondition.get(i)).getVal(6)+"\""+" cm2=\""+((Value)joinCondition.get(i)).getVal(7)+"\"";
			      else
			    	if ((!((Value)joinCondition.get(i)).getVal(8).equals("")))
			    		join+=" categoria2=\""+((Value)joinCondition.get(i)).getVal(8)+"\"";
			    	 
			    	  join+=" campo2=\""+((Value)joinCondition.get(i)).getVal(9)+"\"";
			    	  if (!((Value)joinCondition.get(i)).getVal(10).equals(""))
			    		  join+=" tipo2=\""+((Value)joinCondition.get(i)).getVal(10)+"\"";
			    	  
		    	  sXMLFilter.append("<JOIN version=\"1.1\" "+join+" />");
		      }
		      
		      /**INSERIMENTO CONDIZIONE AND**/
		      if (andCondition!=null)
		          sXMLFilter.append("<CONDIZIONI tipo=\"AND\" value=\"" + andCondition + "\" />");
		      
		      /**INSERIMENTO CONDIZIONE OR**/
		      if (orCondition!=null)
		          sXMLFilter.append("<CONDIZIONI tipo=\"OR\" value=\"" + orCondition + "\" />");
		      
		      /**INSERIMENTO CONDIZIONE TIMEOUT**/
		      if (timeOut!=null)
		          sXMLFilter.append("<CONDIZIONI tipo=\"TIMEOUT\" value=\"" + timeOut + "\" />");

		      /**INSERIMENTO CONDIZIONE MASTER O NON MASTER**/		      
		      sXMLFilter.append("<MASTER value=\""+master+"\" categoriamaster=\""+Global.nvl( categoriaMaster,"")+"\" />");		
		      
		      sXMLFilter.append(XMLNODE_TAIL);
		      
		      return sXMLFilter.toString();
	   }
}

class Value {
	  private String val1,val2,val3,val4,val5,val6,val7,val8,val9,val10;
	  
	  public Value(String newval1,String newval2,String newval3,String newval4,String newval5,String newval6,String newval7,String newval8,String newval9,String newval10) {
		     val1=newval1;
		     val2=newval2;
		     val3=newval3;
		     val4=newval4;
		     val5=newval5;
		     val6=newval6;
		     val7=newval7;
		     val8=newval8;
		     val9=newval9;
		     val10=newval10;
	  }
	  
	  public Value(String newval1,String newval2,String newval3,String newval5,String newval6,String newval7,String newval8, String newval9) {
		     val1=newval1;
		     val2=newval2;
		     val3=newval3;		     
		     val5=newval5;
		     val6=newval6;
		     val7=newval7;
		     val8=newval8;
		     val9=newval9;
	  }
      
	  public Value(String newval1,String newval2,String newval3,String newval4,String newval5) {
		     val1=newval1;
		     val2=newval2;
		     val3=newval3;
		     val4=newval4;
		     val5=newval5;
	  }
	  
	  public Value(String newval1,String newval2,String newval3,String newval4) {
		     val1=newval1;
		     val2=newval2;
		     val3=newval3;
		     val4=newval4;
	  }
	  
	  public Value(String newval1,String newval2) {
		     val1=newval1;
		     val2=newval2;		     
	  }
	  
	  public String getVal(int index) {
		     
		     switch(index){
		        case 1: return val1;
		        case 2: return val2;
		        case 3: return val3;
		        case 4: return val4;
		        case 5: return val5;
		        case 6: return val6;
		        case 7: return val7;
		        case 8: return val8;
		        case 9: return val9;
		        case 10: return val10;
		     }
		     
		     return null;
	  }
}
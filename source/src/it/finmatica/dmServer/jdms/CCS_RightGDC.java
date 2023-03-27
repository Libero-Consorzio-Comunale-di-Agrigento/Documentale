package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.DMTree.*;
import java.util.StringTokenizer;
import it.finmatica.jfc.dbUtil.*;

/**
 * TreeView.
 * Classe di servizio per la gestione del Client
*/ 
 
public class CCS_RightGDC  
{
	/**
	  * Variabili di connessione
	*/ 
	CCS_Common CCS_common;
   
    /**
      * Variabile di generazione HTML
    */
	CCS_HTML h; 
	
	/**
	  * Variabili di IDbOperationSQL
	*/ 
	private IDbOperationSQL dbOp;
    
	/**
	  * Variabile di ambiente
	*/
	private Environment vu; 
   
	/**
	  * Variabile Utente
	*/	 
	private String user;
 
	/**
	  * Variabile Ruolo
	*/	
    private String ruolo;
	
    /**
	  * Variabile wrksp
	*/	
    private String sWrkSp;
    
    /**
	  * Variabile idTog
	*/
    private String idTog;     
   
    /**
	  * Variabile IDToggle
	*/
    private String IDToggle;

    /**
	  * Variabile idCartAppartenenza
	*/
    private String idCartAppartenenza;
   
    /**
	  * Variabile idCollegamento
	*/
    private String idCollegamento;
   
    /**
	  * Variabile slistaNodi
	*/
    private String slistaNodi;

    /**
	  * Variabile sToggle
	*/
    private String sToggle="";

    /**
	  * Variabile sTorna
	*/
    private String sTorna="";

    /**
	  * Variabile sWorkSpace
	*/
    private String sWorkSpace=""; 

    /**
	  * Variabile nodo
	*/
    private String nodo;

    /**
	  * Variabile nParent
	*/
    private String nParent;
    
    /**
	  * Variabile gestione logging
	*/
    private DMServer4j log;
	
   
    public CCS_RightGDC(String newnParent,String newnodo)
    {
	   this.nodo=newnodo;
	   this.nParent=newnParent;
    }
   
	public CCS_RightGDC(String newruolo,String newsWrkSp,String newidTog,String newIDToggle,String newidCartAppartenenza,String newidCollegamento,String newlistaNodi, CCS_Common newCommon) throws Exception
	{
		    ruolo=newruolo;
		   sWrkSp=newsWrkSp;
		   if(sWrkSp.indexOf("-")!=-1)
			 sWrkSp=sWrkSp.substring(1,sWrkSp.length());  
	       idTog=newidTog;
		   IDToggle=newIDToggle;
		   idCartAppartenenza=newidCartAppartenenza;
		   idCollegamento=newidCollegamento;
		   CCS_common=newCommon;
		   log= new DMServer4j(CCS_RightGDC.class,CCS_common); 
		   user=CCS_common.user;
		   h = new CCS_HTML();
		   slistaNodi=newlistaNodi;
		   if((slistaNodi=="null") || (slistaNodi==null))
	      	slistaNodi="0";
	      
	       if (!CCS_common.dataSource.equals("")) {
	          dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	          vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
           }
           else 
           {
              vu=CCS_common.ev;
              dbOp=CCS_common.ev.getDbOp();
           }
	}
  
	/**
	  * Creazione e visualizzazione del TreeView per il CLient Documentale.
	  * Il metodo crea il treeview in funzione anche della sequenza di nodi 
	  * da espandere data in input.
	  * 
	*/	   
	public void _BeforeShow() throws Exception
	{         
		try
        {
           /** Costruzione SELECT */ 
           String sql=getTreeViewSQL();
           String sListaNodi = buildListaNodi(sWrkSp);
           if (vu.Global.PRINT_TREEVIEW.equals("S")) {
        	   System.out.println("[INFO TreeView - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
	 	   }        
           System.out.println("[INFO TreeView - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
           //Visualizzazione Select TreeView
           log.log_info("Select TreeView - "+sql.toString());
            
           TreeView t = new TreeView();
           t.setDefaultTarget("content");
           t.setFont("verdana");
           t.setFontPt("7");
           /** Settaggio di parametri */
           t.setWorkSpace(sWrkSp);
           t.setRuolo(ruolo);
           t.setUtente(user);
           t.setListaID(slistaNodi);
           /** Occorre settare a "S" per visualizzare il TreeView anche con le Cartelle Collegate */
           t.setProvenienza("S");
           /** Caricamento e costruzione del TREEVIEW */
           t.loadFromDb(dbOp,sql,null,null,sWrkSp,sListaNodi);
           t.display(dbOp);
           sTorna=t.getOut();
            
                    
           /** Costruzione della sequenza di nodi da espandere */
           String[] s=null;           
           if((!slistaNodi.equals("0")))
              s=Global.Split(slistaNodi,"X");
           
           if (idTog!=null) 
             sToggle="";
		   else
           {
            	if(s!=null)
            	{
            	  for(int n=0;n<s.length;n++)
            		sToggle+=t.findSequenza(s[n].toString()); 
            	}
          
            	if (sToggle!=null) 
            	{
            		if((IDToggle==null) || (IDToggle==""))	 
            		  sToggle="<script>sToggle='"+sToggle+"';idCart=\"\";</script>";
            		else 
            		  sToggle="<script>sToggle='"+sToggle+"';idCart=\""+IDToggle+"\";</script>";
            	}
            	else
            	  sToggle="<script>sToggle=\"\";</script>";
   	       }
		  
           CCS_common.closeConnection(dbOp);        
         }
		catch (Exception e) 
        {
			try { CCS_common.closeConnection(dbOp); } catch (Exception ei) {}
			log.log_error("CCS_RightGDC:_BeforeShow() - TreeView_HTML:"+sTorna+" - Toggle:"+IDToggle);
			throw e;
			//throw new Exception("CCS_RightGDC:_BeforeShow -- Costruzione TreeView\n"+e.getMessage());
	                    
        }   
	}

	/**
	 * Restituisce HTML campo hidden ruolo
	 * 
	 * @return Stringa 
	 */ 
	public String _getUser() 
	{
		   String utente=h.getInput("","hidden","ruolo","ruolo","3",ruolo,"");  
		   return utente;
	}
  
	/**
	 * Restituisce HTML campo hidden user
	 * 
	 * @return Stringa 
	 */ 
	public String _getUserUtente()
	{
		   String UserUtente= h.getInput("","hidden","user","user","3",user,"");
		   return UserUtente;
	}
		
	/**
	 * Restituisce la lista dei nodi da espandere
	 *	 
	 * @return Stringa sequenza di nodi
	 */ 
	public String _getToggle() 
	{
		   return sToggle;
	}
	
	/**
	 * Restituisce HTML campo hidden idCartAppartenenza
	 * 
	 * @return Stringa 
	 */ 
	public String _getCartAppartenenza() 
	{
		   return  h.getInput("","hidden","idCartAppartenenza","idCartAppartenenza","3",idCartAppartenenza,"");
	}
	
	/**
	 * Restituisce HTML del TreeView
	 * 
	 * @return Stringa HTML che rappresenta il treeview
	 */ 
	public String _getTorna() 
	{
		   return sTorna;
	}
	 
	/**
	 * Restituisce workspace
	 * 
	 * @return Stringa 
	 */ 
	public String _getWorkSpace() 
	{
           return sWorkSpace;
	}

	/**
	 * Costruzione della lista di nodi da espandere 
	 * 
	 * @param  id   indica id della cartella WorkSpace
	 * 
	 * @return String lista di nodi
	 */   
	private String buildListaNodi(String wrskp)
	{     
			String lista="";
   
	        if(slistaNodi.equals("0"))
	           lista="-"+wrskp;	 
	        else
	        {
	        	StringTokenizer st = new StringTokenizer(slistaNodi,"X");
		        while (st.hasMoreTokens())
		        {
		              String s=st.nextToken();
		              lista+=s+","; 
		        }
		        lista=lista.substring(0,lista.length()-1);
	        }    
	        return lista;     
	}  
  
   /**
    * Verifica delle competenze 
    * 
    * @param  oggetto     	indica tipo oggetto
    * @param  idOggetto   	indica id dell'oggetto
    * @param  tipoAbi   	indica tipo di abilitazione
    * 
    * @return String select di verifica competenza
    */ 
	private String GDM(String oggetto,String idOggetto,String azione)
	{     
	        String decode="";
	        decode="GDM_COMPETENZA.GDM_VERIFICA("+oggetto+","+idOggetto+", '"+azione+"', '";
	        decode+=user+"',  F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))";
	        return decode;     
	}

   /**
    * Verifica delle competenze di LETTURA
    * 
    * @param  oggetto     	indica tipo oggetto
    * @param  idOggetto   	indica id dell'oggetto
    * 
    * @return String select di verifica competenza
    */ 
	private String GDM_L(String oggetto,String idOggetto)
	{     
	        String decode="";
	        decode="GDM_COMPETENZA.GDM_VERIFICA("+oggetto+","+idOggetto+", 'L', ";
/*EX UPPER*/	        decode+="'"+user+"', F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 "; 
	 	    return decode;     
	}
 
   /**
    * Costruzione di una generica select
    * 
    * @param  campi     	indica campi
    * @param  elencoTabelle indica tabelle
    * @param  condwhere   	indica condioni
    * @param  typeObj   	indica tipo oggetto
    * 
    * @return String select 
    */ 
	private String Select(String campi,String elencoTabelle,String condwhere,String typeObj) 
	{
         	String 	sql="SELECT "+campi+" ";
         			sql+="FROM "+elencoTabelle+" ";
         			sql+="WHERE "+condwhere+" ";
            return sql;
	}        
         
   /**
    * Restituisce select del TreeView in base al 
    * valore di WRKSP
    * 
    * @return Stringa 
   */ 
   private String getTreeViewSQL() throws Exception
   {
           String sql="";
           
           /** Assegnazione di WorkSpace */
           sWorkSpace=sWrkSp;
           
           try
           {
        	  String campi,tabelle,where;
        	  campi="icona,PARENTID,PARENTID IDCARTPROV, NODEID,";
              campi+="decode(tree.tipo_oggetto,'C',c.nome,'Q',q.nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";
              campi+="DEFAULT_IMAGE, CUSTOM_STYLE,";
              campi+="tree.ID_CARTELLA, TIPO_OGGETTO,";
              campi+="ID_OGGETTO,:sWrkSp wrksp,:ruolo ruolo, ";
              campi+="DECODE (tree.tipo_oggetto, 'C','','Q',decode(instr(q.filtro,'RICERCAMODULISTICA_'),0,'',substr(q.filtro,length('RICERCAMODULISTICA_')+1,length(q.filtro)))) RICERCAMODULISTICA ";
              campi+=","+GDM("decode(tree.tipo_oggetto,'C','VIEW_CARTELLA','Q','QUERY')","decode(tree.tipo_oggetto,'C',vw.ID_VIEWCARTELLA,'Q',id_oggetto)","D")+" CompDelOgg ";        
              campi+=","+GDM("decode(tree.tipo_oggetto,'C','VIEW_CARTELLA','Q','QUERY')","decode(tree.tipo_oggetto,'C',vw.ID_VIEWCARTELLA,'Q',id_oggetto)","U")+" CompModOgg ";        
              campi+=","+"decode(tree.tipo_oggetto,'C',"+GDM("'VIEW_CARTELLA'","vw.ID_VIEWCARTELLA","C")+",'Q',-1) CompDocCartel ";
              campi+=","+GDM("decode(tree.tipo_oggetto,'C','VIEW_CARTELLA','Q','QUERY')","decode(tree.tipo_oggetto,'C',vw.ID_VIEWCARTELLA,'Q',id_oggetto)","M")+" CompCompOgg ";        
              campi+=", decode(tree.tipo_oggetto,'C',vw.ID_VIEWCARTELLA,'Q',id_oggetto) idcart, tree.ordine ";
              tabelle="(SELECT  rownum ordine, PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO";
              tabelle+=" from (";
              tabelle+="        SELECT PARENTID PARENTID, NODEID, TEXT, URL,TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA,'C' TIPO_OGGETTO, ID_OGGETTO";
              tabelle+="          FROM CART_FOGLIE";
              tabelle+="         WHERE ID_CARTELLA IN (-:sWrkSp,:sListaNodi)";
              tabelle+="         UNION ALL";
              tabelle+="        SELECT PARENTID PARENTID, NODEID, TEXT, URL,TOOLTIP, DEFAULT_IMAGE, CUSTOM_IMAGE, CUSTOM_STYLE, ID_CARTELLA,TIPO_OGGETTO, ID_OGGETTO";
              tabelle+="          FROM FOGLIE";
              tabelle+="         WHERE ID_CARTELLA IN (-:sWrkSp,:sListaNodi)";
              tabelle+="       )"; 
              tabelle+=" connect by prior  NODEID = PARENTID AND ID_CARTELLA IN (-:sWrkSp,:sListaNodi)"; 
              tabelle+=" start with  ID_CARTELLA = -:sWrkSp) tree";
              tabelle+=", cartelle c, QUERY q, tipi_documento td, documenti d, view_cartella vw";
              where="tree.id_oggetto = c.id_cartella (+) and tree.id_oggetto = q.id_query (+) and c.id_cartella= vw.id_cartella(+)";          
              where+=" and d.id_tipodoc = td.id_tipodoc ";
              where+=" and d.id_documento = decode(tree.tipo_oggetto,'C',c.id_documento_profilo,'Q',q.id_documento_profilo)";
              where+=" AND decode(tree.tipo_oggetto,'C', NVL (c.stato, 'BO'),'BO') <> 'CA' ";
              
              sql="SELECT icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,"
            	  +"DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO,wrksp,ruolo,RICERCAMODULISTICA," 
            	  +"CompDelOgg,CompModOgg,CompDocCartel,CompCompOgg,idcart,ordine  "
            	  +"FROM ( "+Select(campi,tabelle,where,"")+") WHERE "
            	  +GDM_L("decode(tipo_oggetto,'C','VIEW_CARTELLA','Q','QUERY')","decode(tipo_oggetto,'C',idcart,'Q',id_oggetto)")
            	  +" order by ordine";
              
           } 
           catch (Exception e) { 
        	   throw e;
        	   //throw new Exception("CCS_RightGDC::_Select -- Select Fallita\n" + e.getMessage());           
           }
           
          return sql;   
   }    

  /**
   * Esempio di utilizzo di AJAX
   * 
   * @return Stringa 
  */ 
  public String getNodoHTML() throws Exception
  {          
         String br="<BR clear=\"left\">";  
         String l="<IMG SRC=\"images/i.gif\" ALIGN=\"left\" BORDER=\"0\" width=\"16\" height=\"16\">";
	   	 String s="";
	    	    
	     if(nodo.equals("0"))/** root */
	      s=nodoHTMLApri("A","A","1")+br+nodoHTMLApri("D","D","1")+br+nodoHTMLApri("E","E","1");
	     else
	     {	
	      if(nParent.equals("B"))
	         s=nodoHTMLFoglia("B","B","2");	
	      else	    	
	       {if(nodo.equals("1"))  
	         s=nodoHTMLApri(nParent,nParent,"1");
	        else
	         {
	    	   if(nParent.equals("C"))	
	    	     s=nodoHTMLChiudi(nParent,nParent,"1")+br+l+l+nodoHTMLApri("X","X","1")+br+l+l+nodoHTMLApri("Y","Y","1");
	    	   else
	    	     s=nodoHTMLChiudi(nParent,nParent,"1")+br+"<IMG SRC=\"images/i.gif\" ALIGN=\"left\" BORDER=\"0\" width=\"16\" height=\"16\">"+nodoHTMLApri("B","B","1")+br+"<IMG SRC=\"images/i.gif\" ALIGN=\"left\" BORDER=\"0\" width=\"16\" height=\"16\">"+nodoHTMLApri("C","C","1");
    	     }
	       }  
	      }
	      nodo="<span id='gdmIdAjax_"+nParent+"'>"+s+"</span>";
	      return nodo;
  }
  private String nodoHTMLApri(String id,String nome,String liv)
  {
		 return "<span id=\""+id+"\">"
	     +"<A href=\"#\" onclick=\"apri('"+id+"');\" ><IMG title=\"Apri\" SRC=\"images/xl.gif\"  ALIGN=\"left\" BORDER=\"0\" width=\"16\" height=\"16\">"
		 +"<a class=treeview ><IMG SRC=\"images/plusGDC.gif\" style=\"width:16px; height:16px;\"  onload=\"\" ALIGN=\"left\" BORDER=0>"
		 +"<span UNSELECTABLE=\"on\" onmouseover=\"this.style.cursor='hand';this.style.color='#FF6600';\" onmouseout=\"this.style.color='black';\">"
		 +nome+"</span></a><input type=\"hidden\" id=\"livello"+id+"\" name=\"livello"+id+"\" size=\"3\" value=\""+liv+"\"></A></span>";
  }
  private String nodoHTMLChiudi(String id,String nome,String liv)
  {
		 return "<span id=\""+id+"\">"
	     +"<A href=\"#\" onclick=\"chiudi('"+id+"');\" ><IMG title=\"Apri\" SRC=\"images/-l.gif\"  ALIGN=\"left\" BORDER=\"0\" width=\"16\" height=\"16\">"
		 +"<a class=treeview ><IMG SRC=\"images/plusGDC.gif\" style=\"width:16px; height:16px;\"  onload=\"\" ALIGN=\"left\" BORDER=0>"
		 +"<span UNSELECTABLE=\"on\" onmouseover=\"this.style.cursor='hand';this.style.color='#FF6600';\" onmouseout=\"this.style.color='black';\">"
		 +nome+"</span></a><input type=\"hidden\" id=\"livello"+id+"\" name=\"livello"+id+"\" size=\"3\" value=\""+liv+"\"></A></span>";
  }
  private String nodoHTMLFoglia(String id,String nome,String liv)
  {
		 return "<span id=\""+id+"\">"
	     +"<IMG title=\"Foglia\" SRC=\"images/l.gif\"  ALIGN=\"left\" BORDER=\"0\" width=\"16\" height=\"16\">"
		 +"<a class=treeview ><IMG SRC=\"images/folder.gif\" style=\"width:16px; height:16px;\"  onload=\"\" ALIGN=\"left\" BORDER=0>"
		 +"<span UNSELECTABLE=\"on\" onmouseover=\"this.style.cursor='hand';this.style.color='#FF6600';\" onmouseout=\"this.style.color='black';\">"
		 +nome+"</span></a><input type=\"hidden\" id=\"livello"+id+"\" name=\"livello"+id+"\" size=\"3\" value=\""+liv+"\"></A></span>";
  }
  
}
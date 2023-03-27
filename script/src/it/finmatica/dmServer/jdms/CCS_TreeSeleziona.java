package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.DMTree.*;
import java.util.StringTokenizer;
import it.finmatica.jfc.dbUtil.*;

/**
 * TreeView per la gestione di Operazione Copia/Sposta.
 * Classe di servizio per la gestione del Client
*/
  
public class CCS_TreeSeleziona 
{
	/**
	    * Variabili di connessione
	*/
    CCS_Common CCS_common;
    
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
	    * Variabile idCartella
	*/
    private String idCartella;
    
    /**
	    * Variabile listaIDdaEscludere
	*/
    private String listaIDdaEscludere;     
   
    /**
	    * Variabile provenienza S=Operazione di SPOSTA o C=operazione di COPIA
	*/
    private String provenienza="S";
   
    /**
	    * Variabile slistaNodi
	*/
    private String slistaNodi;
   
    /**
	    * Variabile sToggle
	*/
    private String sToggle="";
  
    /**
	    * Variabile nodoDaSaltare
	*/
    private String nodoDaSaltare="'XXX'";

    /**
	    * Variabili di sTorna
	*/
    private String sTorna="";
   
	/**
	  * Variabile gestione logging
	*/
    private DMServer4j log;
  
    public CCS_TreeSeleziona(String newruolo,String newsWrkSp,String newidCartella,String newlistaIDdaEscludere,String newProvenienza,String newlistaNodi,String newidCartAppartenenza,CCS_Common newCommon) throws Exception
    {
         ruolo=newruolo;
         sWrkSp=newsWrkSp;
         if(sWrkSp.indexOf("-")!=-1)
           sWrkSp=sWrkSp.substring(1,sWrkSp.length());  
         idCartella=newidCartella;
         listaIDdaEscludere=newlistaIDdaEscludere;
         //Settare la lista di nodi da escludere       
         if (idCartella!=null)
     	    nodoDaSaltare="'C"+idCartella+"'";
     	 else if (listaIDdaEscludere!=null)
     	    nodoDaSaltare="'"+listaIDdaEscludere+"'";
         provenienza=newProvenienza;
         CCS_common=newCommon;
         log= new DMServer4j(CCS_TreeSeleziona.class,CCS_common); 
         user=CCS_common.user;
         slistaNodi=newlistaNodi;
         if((slistaNodi=="null") || (slistaNodi==null))
           slistaNodi="0";
         dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
         if (!CCS_common.dataSource.equals("")) 
           vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
         else 
           vu=CCS_common.ev;
      }
  
   /**
    * Creazione e visualizzazione del TreeView per l'operazione di Copia e Sposta.
    * Il metodo crea il treeview in funzione anche della sequenza di nodi selezionati
    * da escludere nella visualizzazione data in input.
    * 
    */	   
   public void _BeforeShow() throws Exception
   {
     try
     {
       /* Costruzione SELECT */
       String sql=getTreeViewSQL();
       String sListaNodi=buildListaNodi(sWrkSp);
       if (vu.Global.PRINT_TREEVIEW.equals("S")) {
 		   System.out.println("[INFO TreeView - COPIA/SPOSTA]: "+sql.toString());
       }  
       
       //Visualizzazione Select TreeView
       log.log_info("Select TreeView COPIA/SPOSTA - "+sql.toString());
       
       TreeView t = new TreeView();
       t.setDefaultTarget("content");
	   t.setFont("verdana");
       t.setFontPt("7");
       /* Occorre settare il tipo di provenienza "C"=COPIA oppure "S"=SPOSTA
        * La visualizzazione dei Collegamenti nel TreeView viene effettuata soltanto se
        * l'operazione è di tipo SPOSTA, senza altrimenti */
       t.setProvenienza(provenienza);       
       /* Settaggio di parametri */
       t.setWorkSpace(sWrkSp);
       t.setRuolo(ruolo);
       t.setUtente(user);
       t.setListaID(slistaNodi);
       t.setNodoDaSaltare(nodoDaSaltare);
       /*Caricamento e costruzione del TREEVIEW*/
       t.loadFromDb(dbOp,sql,null,null,sWrkSp,sListaNodi);
       t.display(dbOp);
       sTorna=t.getOut();
       /* Costruzione della sequenza di nodi da espandere */
       String[] s=null;           
       if((!slistaNodi.equals("0")))
          s=Global.Split(slistaNodi,"X");          
       if(s!=null)
 	   {
 		for(int n=0;n<s.length;n++)
 		   sToggle+=t.findSequenza(s[n].toString()); 
	    }
        sToggle="<script>sToggle='"+sToggle+"';</script>";
     }
	 catch (Exception e) 
     {
        log.log_error("CCS_TreeSeleziona:_BeforeShow() - TreeView_HTML Copia/Sposta:"+sTorna);
		throw e;
     }
     finally {
         _finally();
     }
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
    * Restituisce HTML del TreeView
    * 
    * @return Stringa HTML che rappresenta il treeview
   */ 
   public String _getTorna() 
   {
         return sTorna;
   }


    //Chiusura della connessione
    private void _finally() throws Exception {
        try
        {
            if (CCS_common.dataSource.equals("")){
                try{vu.disconnectClose();}catch(Exception ei){}
            }
            CCS_common.closeConnection(dbOp);
        }
        catch (Exception e) {
            throw e;
        }
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
           try
	  	   {
	  		String campi,tabelle,where;
	        campi="icona,PARENTID,PARENTID IDCARTPROV, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";
	        campi+="c.nome NOME, ";
	        campi+="DEFAULT_IMAGE, CUSTOM_STYLE,";
	        campi+="tree.ID_CARTELLA, TIPO_OGGETTO,";
	        campi+="ID_OGGETTO, :nodoDaSaltare NODEIDESCLUSO,:sWrkSp wrksp,'DONTCARE' ruolo";
	        campi+=",'-1' CompDelOgg ";        
	        campi+=",'-1' CompModOgg ";        
	        campi+=",'-1' CompDocCartel ";
	        campi+=",'-1' CompCompOgg "; 
	        campi+=", vw.id_viewcartella idcart ";
	        tabelle="(SELECT  PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO";
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
          	tabelle+=", cartelle c, documenti d, tipi_documento td, view_cartella vw ";
          	where="tree.ID_OGGETTO = c.ID_CARTELLA";
          	where+=" and c.ID_CARTELLA = vw.id_cartella";
          	where+=" and d.id_tipodoc = td.id_tipodoc  ";
          	where+=" and tree.TIPO_OGGETTO = 'C' ";
          	where+=" and d.id_documento = c.id_documento_profilo ";
          	where+=" and nvl(c.stato,'BO')<>'CA' ";
            sql="SELECT icona, parentid, idcartprov, nodeid, text, url, tooltip,"+
	        	"custom_image, upper(nome) nome, default_image, custom_style,id_cartella, "+
	          	"tipo_oggetto, id_oggetto, nodeidescluso,wrksp,ruolo,compdelogg,"+
	          	"compmodogg,compdoccartel,compcompogg,idcart "+
	          	 "FROM ( " +Select(campi,tabelle,where)+" ) where "+
	          	 GDM("VIEW_CARTELLA","idcart","L")+
	             " and "+GDM("VIEW_CARTELLA","idcart","C");  
	  	  } 
	  	  catch (Exception e) {           
	  		throw e;
	  	  }
	  	  return sql;
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
	         lista="-"+wrskp+"";	 
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
   private String GDM(String oggetto,String idOggetto,String tipoAbi)
   {     
	       String decode="";
	       decode="GDM_COMPETENZA.GDM_VERIFICA('"+oggetto+"',"+idOggetto+", '"+tipoAbi+"', ";
/*EX UPPER*/	       decode+="'"+user+"', F_TRASLA_RUOLO('"+ruolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 "; 
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
   private String Select(String campi,String elencoTabelle,String condwhere) 
   {
	       String sql="SELECT "+campi+" ";
	              sql+="FROM "+elencoTabelle+" ";
	              sql+="WHERE "+condwhere+" ";
	       return sql;
   }        
}
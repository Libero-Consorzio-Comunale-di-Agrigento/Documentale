package it.finmatica.dmServer.jdmsfx;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.dmServer.util.UtilityDate;
import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.jdmsfx.NodeTree;
import it.finmatica.dmServer.jdmsfx.QuickSort;
import it.finmatica.dmServer.jdms.CCS_Common;
import it.finmatica.dmServer.jdms.DMServer4j;
import it.finmatica.dmServer.jdms.XSS_Encoder;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.dmServer.Environment;
import java.util.StringTokenizer;
import org.dom4j.DocumentHelper;
import java.sql.Connection;
import java.sql.ResultSet;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Vector;

/**
 * Tree
 * 
 * Questa classe è utlizzata da un servizio JAVASERVICE SOA 
 * per visualizzare il treeview, componente a sx del documentale.
 * 
 * AUTHOR @SCANDURRA
 *  
 * */

public class Tree {
	
	  //Vettore di nodi	
	  private Vector vNodes;	
	  private String xmlTree;
	  CCS_Common CCS_common;
	  Environment en;
	  private HttpServletRequest req;
	  private DMServer4j log;
	  private IDbOperationSQL dbOp;
	  private String parentID;
	  private String sWrksp;
	  private String nomeWrksp;
	  private String ruolo;
	  private String slistaNodi;
	  private String sequenza="";
	  private String idNodo;
	  private String user;
	  private String tipo_operazione="";
	  private String listaDaEscludere="";
	  private boolean expandNode=false;
	  Element root;
	  private XSS_Encoder xss=null;
	  private String jdmsML = "";
	   
	   /**
	    * Costante per identificare la variabile di sessione per GDM multilingua
	   */
	   private final static String MULTILINGUA ="MULTILINGUA";
	  
	  /**
	   * Costruttore
	   */
	  public Tree(String newruolo,String newsWrkSp,String newlistaNodi,CCS_Common newCommon) throws Exception
	  {
		  	 vNodes = new Vector();
		  	 xmlTree="";
		  	 CCS_common=newCommon;	
		  	 ruolo=newruolo;
		  	 sWrksp=newsWrkSp;
		  	 if(sWrksp.indexOf("-")!=-1)
		  	   sWrksp=sWrksp.substring(1,sWrksp.length());  
		  	 user=CCS_common.getUser();
		  	 slistaNodi=newlistaNodi;
		  	 if((slistaNodi=="null") || (slistaNodi==null))
		  	  slistaNodi="0";
		  		   
		  	 if (!CCS_common.getDataSource().equals("")) {
		  		 dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.getDataSource(),0);
		  		 en = new Environment(CCS_common.getUser(), null,null,null, null,dbOp.getConn(),false);
		  	 }
		  	 else 
		  	 {
		  		 en=CCS_common.getEnvironment();
		  		 dbOp=CCS_common.getEnvironment().getDbOp();
		  	 }
		  	 
		  	 log= new DMServer4j(Tree.class,CCS_common); 
      }
	  
	  /**
	   * Costruttore
	   */
	  public Tree(String newruolo,String newsWrkSp,String newNodo,String expNode,HttpServletRequest newreq,IDbOperationSQL newdbOp) throws Exception
	  {
		  	 vNodes = new Vector();
		  	 xmlTree="";
		  	 req=newreq;
			 user=""+req.getSession().getAttribute("Utente");
		  	 dbOp=newdbOp;//SessioneDb.getInstance().createIDbOperationSQL(newdbOp); 
		  	 en = new Environment(user, null,null,null, null,dbOp.getConn(),false);
			 CCS_common= new CCS_Common(en,user); 			 
		  	 ruolo=newruolo;
		  	 if(expNode.equals("1"))
		  	  expandNode=true;
		  	 sWrksp=newsWrkSp;
		  	 if(sWrksp.indexOf("-")!=-1)
		  	  sWrksp=sWrksp.substring(1,sWrksp.length());  
		  	 
		  	 xss = new XSS_Encoder(req,CCS_common);
		  	 
		  	 if(req.getParameter("parametri")!=null && !req.getParameter("parametri").equals(""))
		  	   tipo_operazione=verificaParametroGet("parametri",req.getParameter("parametri"));
		  	 
		  	if(req.getParameter("listaID")!=null && !req.getParameter("listaID").equals(""))
			  listaDaEscludere=verificaParametroGet("listaID",req.getParameter("listaID"));
		  	 
		  	 if(newNodo!=null && !newNodo.equals("") && !newNodo.equals("0"))
		  	   idNodo=newNodo;
		  	 else
		  	   idNodo="-"+sWrksp;	  
 		  	 
		  	 if(slistaNodi==null || (slistaNodi!=null && slistaNodi.equals("0")) )
		  	  slistaNodi="-"+sWrksp;
		  	 
		  	 getParametriSessione();
		  	
		  	 log= new DMServer4j(Tree.class,CCS_common); 
      }
	  
	  private String verificaParametroGet(String parametro, String valore) throws Exception {
		   if (valore == null) {
		    return null;
		   }
		   String newVal=valore;
		   
		   if(xss!=null){
			   newVal = xss.encodeHtmlAttribute(parametro,valore);
			   if (!newVal.equals(valore)) {
			    throw new Exception("Parametro "+parametro+" non valido!");
			   }
		   }
		   return newVal;
	  }
	  
	  /**
	   * Costruttore
	   */
	  public Tree(String newruolo,String newsWrkSp,String newNodo,String expNode,String tipoOp,String listaDoc,HttpServletRequest newreq,IDbOperationSQL newdbOp) throws Exception
	  {
		  	 vNodes = new Vector();
		  	 xmlTree="";
		  	 req=newreq;
			 user=""+req.getSession().getAttribute("Utente");
		  	 dbOp=newdbOp;//SessioneDb.getInstance().createIDbOperationSQL(newdbOp); 
		  	 en = new Environment(user, null,null,null, null,dbOp.getConn(),false);
			 CCS_common= new CCS_Common(en,user); 			 
		  	 ruolo=newruolo;
		  	 if(expNode.equals("1"))
		  	  expandNode=true;
		  	 sWrksp=newsWrkSp;
		  	 if(sWrksp.indexOf("-")!=-1)
		  	  sWrksp=sWrksp.substring(1,sWrksp.length());  

		  	tipo_operazione=tipoOp;
		  	listaDaEscludere=listaDoc;
		  	 
		  	 if(newNodo!=null && !newNodo.equals("") && !newNodo.equals("0"))
		  	   idNodo=newNodo;
		  	 else
		  	   idNodo="-"+sWrksp;	  
 		  	 
		  	 if(slistaNodi==null || (slistaNodi!=null && slistaNodi.equals("0")) )
		  	  slistaNodi="-"+sWrksp;
		  	 
		  	 getParametriSessione();
		  	
		  	 log= new DMServer4j(Tree.class,CCS_common); 
      }
	  
	   /**
	    * Calcola alcune varibili in sessione
	    * @param req
	    * 
	    */   
	   private void getParametriSessione(){
	       if(req!=null){
	    	if(req.getSession().getAttribute(MULTILINGUA)!=null){   
			     String lingua = req.getSession().getAttribute(MULTILINGUA).toString();
			     if (lingua!=null && !lingua.equals(""))
			    	 jdmsML = lingua;
			     else
			    	 jdmsML = "";
	        }  
	    	else
	    		jdmsML = "";
	       }	
	   }
 
	  

	  /**
	   * METHOD:      loadFromDb
	   * SCOPE:       PUBLIC
	   *
	   * DESCRIPTION: Caricamento della struttura treeview 
	   * 
	   * RETURN:      la struttura del tree in xml
	  */  
	  public String loadFromDb() throws Exception
	  {
		     String xml;
		     NodeTree parentNode;
	         IDbOperationSQL  dbOpSQL=null; 
	         boolean bConnect=false;
			 
	         /** Costruzione SELECT */
	         String sql;
	         
	         if(tipo_operazione.equals(""))
	           sql=getTreeViewSQLPerLivelli();
	         else
	           sql=getTreeViewSQLPerTipoCartella();	  
	         
	         if (en.Global.PRINT_TREEVIEW.equals("S")) {
	           System.out.println("[****INFO FX TreeView - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
		 	 }        
	         
            //System.out.println("[****INFO FX TreeView - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sql.toString());
			
	        String[] sLista=slistaNodi.split("@");
			for(int i=0;i<sLista.length;i++)
			{
				  if(i==sLista.length-1)
					sequenza+="@"+sLista[i]+"@";
				  else
					sequenza+="@"+sLista[i]+"@,";	  
			}

	        try
	        { 
        	   if (en.getDbOp()==null) {
	    		 en.connect();
	    		 bConnect=true;  
	    	   }
        	   
        	   dbOpSQL = en.getDbOp();	
	    	   dbOpSQL.setStatement(sql);
        	   
               String nodo;
	    	   
	    	   if(tipo_operazione.equals("")){
	    		   
	    		    if(expandNode)
			            nodo=idNodo;
			        else
			        	nodo= "-"+sWrksp; 			      
	    	   }
	    	   else {
	    		  
	   	           if(expandNode)
	   	            nodo=idNodo;
	   	           else
	   	        	nodo= "-"+sWrksp; 
	    	   }
	    	   	    	     
		       dbOpSQL.setParameter(":startNodo", nodo);
		       dbOpSQL.setParameter(":sWrksp", sWrksp);
		       dbOpSQL.setParameter(":ruolo", ruolo);
        	  
	    	   dbOpSQL.execute();
	           ResultSet rst = dbOpSQL.getRstSet();
	     
	           while (rst.next()) 
	           { 
        	     try {                
                   if (rst.getString("NODEIDESCLUSO").indexOf(rst.getString("NodeID"))!=-1)
                    continue;       
                 }
                 catch(Exception e) 
                 {}
                 
                 parentID=rst.getString("ParentID");     
	             sWrksp=rst.getString("wrksp");
	             nomeWrksp=rst.getString("nomewrksp");
	              
	             NodeTree node = new NodeTree();
	             node.setId(rst.getString("id_oggetto"));
	             node.setText(rst.getString("text"));
	             node.setTypeNode(rst.getString("tipo_oggetto"));
	             node.setTextQuickSort(rst.getString("nome"));
	             node.setIdOggettoComp(rst.getString("id_oggetto_comp"));
	             node.setIcona(rst.getString("icona"));
	             node.setHasChildren(rst.getInt("haschildren")+"");
	             node.setParentId(parentID);
	              
	             String rm=rst.getString("RICERCAMODULISTICA");
		         if((rm!=null) && (!rm.equals("")))
		           node.setTipoUso("R");
		        
	              
		         if((parentID.equals("C-"+sWrksp)) || expandNode)
			     	vNodes.addElement(node);                 
	             else
	             {
	               parentNode = findNode(vNodes,parentID,true);               
	              
	               if (parentNode!=null)
	                parentNode.add(node);
	             }
		         
		         //if(slistaNodi.indexOf(node.getId())!=-1 && tipo_operazione.equals("") && sWrksp.indexOf(node.getId())==-1 ){
		         String id = "@"+node.getId()+"@";
		         String sequenza = (slistaNodi.substring(0,1).equals("@"))? (slistaNodi+"@"):("@"+slistaNodi+"@");
		         
		         if(sequenza.indexOf(id)!=-1 && tipo_operazione.equals("")){
				   //Inserimento delle CATEGORIA 
		           insertCategorieNode(dbOpSQL.getConn(),node.getId(),node);
		           //Inserimento dei COLLEGAMENTI ESTERNI
		           insertCollegamentiEsterniNode(dbOpSQL.getConn(),node.getId(),node);
		         }
		       }
	           
	           xml= this.displayTree();
	           
	           if ( bConnect)	     	  
				 en.disconnectClose(); 
	          
	           try {dbOp.close();} catch(Exception e) {}
	          
	           return xml;
	         }
	         catch(Exception e) 
	         { 
	           try{en.disconnectClose();}catch(Exception ei){}
	     	   log.log_error("Tree::loadFromDb() - Problemi durante la costruzione del Tree:"+e.getMessage());
			   throw new Exception("Tree::loadFromDb()\n"+e.getMessage());
	         }         
	  }  
	  
	  /**
	   * METHOD:      getPathFolder
	   * SCOPE:       PRIVATE
	   *
	   * DESCRIPTION: Determina il cammino del sottoalbero dato il nodo
	   * 
	   * RETURN:      String
	   */ 
	  private String getPathFolder(Connection conn,String id,String collegamento) throws Exception
	  {
	          String sql="",seq="";
	          String f_pathC;
	          IDbOperationSQL idbOp = SessioneDb.getInstance().createIDbOperationSQL(conn,0);
	          
	          if(jdmsML!=null && !jdmsML.equals("")){
	        	  if (collegamento!=null)
	 	             f_pathC="F_Path_collegamento_ml(:COLLEGAMENTO,''||to_char(c.id_cartella)||'','I',:USER,'"+jdmsML+"')";
	 	          else
	 	             f_pathC="F_Path_Folder_ml(c.id_cartella,'I',:USER,'"+jdmsML+"')";
	          }
	          else {
	        	  if (collegamento!=null)
	 	             f_pathC="F_Path_collegamento(:COLLEGAMENTO,''||to_char(c.id_cartella)||'','I',:USER)";
	 	          else
	 	             f_pathC="F_Path_Folder(c.id_cartella,'I',:USER)";
	          }
	         
	          try {
	               ResultSet rs=null;
	               sql="  select decode(c.id_cartella,-1,'-1',-2,'-2',"+f_pathC+") PATH ";
	               sql+=" FROM cartelle c ";
	               sql+=" WHERE c.id_cartella =:IDCARTELLA";
	               
	               
	               idbOp.setStatement(sql);
	        	   idbOp.setParameter(":COLLEGAMENTO",collegamento);
	        	   idbOp.setParameter(":USER",this.user);
	        	   idbOp.setParameter(":IDCARTELLA",id);
	               idbOp.execute();
	        	   rs=idbOp.getRstSet();
	               if (rs.next()) 
	                seq=rs.getString(1);
	               idbOp.close();
	          }
	          catch (Exception e) {
	           try { idbOp.close(); } catch (Exception ei) {}
	           log.log_error("Tree::getPathFolder() - Costruzione Path SQL: "+sql+" - Erroe:"+e.getMessage());
	           throw new Exception("Tree::getPathFolder -- Select fallita\n" + e.getMessage());           
	          }  
	          return seq;   
	  }   
	  
	  private void insertCategorieNode(Connection conn,String idCartella,NodeTree n) throws Exception
	  {
		      String sql="";
		      IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(conn,0);
			  
	          try
	          { 
	           if(jdmsML!=null && !jdmsML.equals(""))	  
	        	 sql="SELECT distinct GDM_UTILITY.F_MULTILINGUA(c.categoria_collegata,'"+jdmsML+"') categoria_collegata, d.utente utente";
	           else
	        	   sql="SELECT distinct c.categoria_collegata, d.utente utente";
		       
        	   sql+=" from collegamenti c, desktop d ";
		       sql+=" where c.id_cartella=:IDCARTELLA";
		       sql+=" and c.categoria_collegata is not null ";
		       sql+=" and c.categoria_collegata = d.categoria";
		       sql+=" and d.utente = :USER";     
		       sql+=" union all ";
		       if(jdmsML!=null && !jdmsML.equals(""))	  
  		         sql+="  SELECT distinct GDM_UTILITY.F_MULTILINGUA(c.categoria_collegata,'"+jdmsML+"') categoria_collegata ,'@TUTTI@' utente";
		       else
		    	 sql+="  SELECT distinct c.categoria_collegata ,'@TUTTI@' utente";
		       sql+=" from collegamenti c, desktop d ";
		       sql+=" where c.id_cartella=:IDCARTELLA";
		       sql+=" and c.categoria_collegata is not null ";
		       sql+=" and c.categoria_collegata = d.categoria";
		       sql+=" and d.utente = '@TUTTI@'";  
		      
               dbOpSQL.setStatement(sql);
               dbOpSQL.setParameter(":USER",user);
               dbOpSQL.setParameter(":IDCARTELLA",idCartella);
 	    	   dbOpSQL.execute();
 	           ResultSet rst = dbOpSQL.getRstSet();	  
	               
	           while (rst.next()) {
	        	 addNodetree(n,rst.getString("categoria_collegata"),rst.getString("categoria_collegata"),"JWL",rst.getString("categoria_collegata"),idCartella,rst.getString("utente"),"","0");
	           }
	           dbOpSQL.close();
	         }
             catch(Exception e) 
	         { 
            	 try { dbOpSQL.close(); } catch (Exception ei) {}
            	 log.log_error("Tree::insertCategorie() - Inserimento CATEGORIE SQL: "+sql+" - Erroe:"+e.getMessage());
            	 throw new Exception("Tree::insertCategorie()\n"+e.getMessage());
	         }         
	  }
	  
	  private void insertCollegamentiEsterniNode(Connection conn,String idCartella,NodeTree n) throws Exception
	  {
		      String sql="";
		      String sTooltip="";
		      IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(conn,0);
			  
	          try
	          {  
        	     if(jdmsML!=null && !jdmsML.equals(""))
        	       sql=" SELECT GDM_UTILITY.F_MULTILINGUA(ce.nome,'E') nome, ce.url url, ce.tooltip tooltip, ce.icona icona, './icone/'||ce.icona||'/'||ic.nome nome_icona, ";
        	     else   	
        		   sql=" SELECT ce.nome nome, ce.url url, ce.tooltip tooltip, ce.icona icona, './icone/'||ce.icona||'/'||ic.nome nome_icona, ";
               		
        		  sql+=" 		l.id_cartella id_cartella,l.tipo_oggetto tipo_oggetto, "
	        		   +" 		l.id_oggetto id_oggetto, 0 haschildren, codiceads, "
	        		   +" 		id_collegamento_sostituito,tipo_link, nvl(funzione_lettura,'') funzione_lettura"
	        		   +" FROM links l, collegamenti_esterni ce, icone ic "
	        		   +" WHERE id_cartella = :IDCARTELLA "
	        		   +" 		AND l.id_oggetto = ce.id_collegamento "
	        		   +" 		AND l.tipo_oggetto = 'L'"
	        		   +"		AND ce.icona = ic.icona(+)"	
	        		   +" order by nome"; 
		      
               dbOpSQL.setStatement(sql);
               dbOpSQL.setParameter(":IDCARTELLA",idCartella);
 	    	   dbOpSQL.execute();
 	           ResultSet rst = dbOpSQL.getRstSet();	  
	               
	           while (rst.next()) {
	        	 if(rst.getString("tooltip")!=null)
	              sTooltip = rst.getString("tooltip");    
	        	
	        	 String ret="1";  
	        	 String nome_funzione = rst.getString("funzione_lettura");
	        	 if(nome_funzione!=null && !nome_funzione.equals(""))
	        	  ret = executeFunzioneCollegamentoEsterno(conn,nome_funzione);
	        	 
	        	 if(ret.equals("1"))
	        	  addNodetree(n,rst.getString("id_oggetto"),rst.getString("nome"),rst.getString("tipo_oggetto"),rst.getString("nome"),rst.getString("url"),sTooltip,rst.getString("icona"),rst.getString("nome_icona"),rst.getString("haschildren"), rst.getString("tipo_link"));
	           
	           }
	           
	           dbOpSQL.close();
	         }
             catch(Exception e) 
	         { 
            	 try { dbOpSQL.close(); } catch (Exception ei) {}
            	 log.log_error("Tree::insertCollegamentiEsterniNode() - Inserimento COLLEGAMENTI_ESTERNI SQL: "+sql+" - Erroe:"+e.getMessage());
            	 throw new Exception("Tree::insertCollegamentiEsterniNode()\n"+e.getMessage());
	         }         
	  }
	  
	  
	  private void retrieveNomeWRKSP(Connection conn) throws Exception
	  {
		      String sql="";
		      IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(conn,0);
			  
	          try
	          { 
	           if(jdmsML!=null && !jdmsML.equals(""))	  
	        	sql="select GDM_UTILITY.F_MULTILINGUA(nome,'"+jdmsML+"') nome from cartelle where id_cartella= -:WRKSP";
	           else	   
	        	sql="select nome from cartelle where id_cartella= -:WRKSP";
	           
		       dbOpSQL.setStatement(sql);
		       dbOpSQL.setParameter(":WRKSP",sWrksp);
 	    	   dbOpSQL.execute();
 	           ResultSet rst = dbOpSQL.getRstSet();	  
	               
	           if(rst.next()) 
	        	nomeWrksp=rst.getString("nome");
	        	   
	           dbOpSQL.close();
	         }
             catch(Exception e) 
	         { 
            	 try { dbOpSQL.close(); } catch (Exception ei) {}
            	 log.log_error("Tree::retrieveNomeWRKSP() - Recupero nome della workspace SQL: "+sql+" - Erroe:"+e.getMessage());
            	 throw new Exception("Tree::retrieveNomeWRKSP()\n"+e.getMessage());
	         }         
	  }
	  
	  private void insertCollegamentiEsterniElement(Connection conn,String idCartella,Element elp) throws Exception
	  {
		      String sql="";
		      String sTooltip="";
		      Element elf=null;
		      IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(conn,0);
			  
	          try
	          { 
	        	  if(jdmsML!=null && !jdmsML.equals(""))
	        		  sql=" SELECT GDM_UTILITY.F_MULTILINGUA(ce.nome,'"+jdmsML+"') nome, ce.url url, ce.tooltip tooltip, ce.icona icona, './icone/'||ce.icona||'/'||ic.nome nome_icona, ";
	        	  else	  
	        		  sql=" SELECT ce.nome nome, ce.url url, ce.tooltip tooltip, ce.icona icona, './icone/'||ce.icona||'/'||ic.nome nome_icona, ";
        		  
	        	   sql+=" 		l.id_cartella id_cartella,l.tipo_oggetto tipo_oggetto, "
	        		   +" 		l.id_oggetto id_oggetto, 0 haschildren, codiceads, "
	        		   +" 		id_collegamento_sostituito,tipo_link, nvl(funzione_lettura,'') funzione_lettura"
	        		   +" FROM links l, collegamenti_esterni ce, icone ic "
	        		   +" WHERE id_cartella = :IDCARTELLA "
	        		   +" 		AND l.id_oggetto = ce.id_collegamento "
	        		   +" 		AND l.tipo_oggetto = 'L'" 
	        		   +"		AND ce.icona = ic.icona(+)"
	        		   +" order by nome";  
			      
		       dbOpSQL.setStatement(sql);
               dbOpSQL.setParameter(":IDCARTELLA",idCartella);
 	    	   dbOpSQL.execute();
 	           ResultSet rst = dbOpSQL.getRstSet();	  
	               
	           while (rst.next()){	        	   
	        	   String ret="1";  
		           String nome_funzione = rst.getString("funzione_lettura");
		           if(nome_funzione!=null && !nome_funzione.equals(""))
		        	ret = executeFunzioneCollegamentoEsterno(conn,nome_funzione);
		        
		           if(ret.equals("1")){
		            if(rst.getString("tooltip")!=null)
                     sTooltip = rst.getString("tooltip");  
	        	   
		            elf = buildNodo(rst.getString("id_oggetto"),rst.getString("nome"),rst.getString("tipo_oggetto"),sTooltip,
	        			 "","",rst.getString("haschildren"),0,idCartella,"",rst.getString("tipo_link"),rst.getString("url"),rst.getString("icona"),rst.getString("nome_icona"));
	        	    elp.add(elf);
	        	  }
	           }	           
	           dbOpSQL.close();
	         }
             catch(Exception e) 
	         { 
            	 try { dbOpSQL.close(); } catch (Exception ei) {}
            	 log.log_error("Tree::insertCollegamentiEsterniElement() - Inserimento COLLEGAMENTI_ESTERNI SQL: "+sql+" - Erroe:"+e.getMessage());
            	 throw new Exception("Tree::insertCollegamentiEsterniElement()\n"+e.getMessage());
	         }         
	  }
	  
	  
	  private void insertCategorieElement(Connection conn,String idCartella,Element elp) throws Exception
	  {
		      String sql="";
		      Element elf=null;
		      IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(conn,0);
			  
	          try
	          { 
	           if(jdmsML!=null && !jdmsML.equals(""))	  
	             sql="SELECT distinct GDM_UTILITY.F_MULTILINGUA(c.categoria_collegata,'"+jdmsML+"') categoria_collegata , d.utente utente ";
	           else
	        	 sql="SELECT distinct c.categoria_collegata , d.utente utente ";	           
		       sql+=" from collegamenti c, desktop d ";
		       sql+=" where c.id_cartella = :IDCARTELLA";
		       sql+=" and c.categoria_collegata is not null ";
		       sql+=" and c.categoria_collegata = d.categoria";
		       sql+=" and d.utente = :USER ";
		       sql+=" union all ";
		       if(jdmsML!=null && !jdmsML.equals(""))	  
			     sql+=" SELECT distinct  GDM_UTILITY.F_MULTILINGUA(c.categoria_collegata,'"+jdmsML+"') categoria_collegata ,'@TUTTI@' utente ";
		       else
		    	sql+="  SELECT distinct c.categoria_collegata ,'@TUTTI@' utente ";
		       sql+=" from collegamenti c, desktop d ";
		       sql+=" where c.id_cartella = :IDCARTELLA";
		       sql+=" and c.categoria_collegata is not null ";
		       sql+=" and c.categoria_collegata = d.categoria";
		       sql+=" and d.utente = '@TUTTI@'";  
		      
		       dbOpSQL.setStatement(sql);
               dbOpSQL.setParameter(":USER",user);
               dbOpSQL.setParameter(":IDCARTELLA",idCartella);
 	    	   dbOpSQL.execute();
 	           ResultSet rst = dbOpSQL.getRstSet();	  
	               
	           while (rst.next()){
	        	 elf = buildNodo(rst.getString("categoria_collegata"),rst.getString("categoria_collegata"),"JWL","",rst.getString("utente"),null,"0",0,idCartella,"",null,null,null,null);
	             elp.add(elf);
	           }	           
	           dbOpSQL.close();
	         }
             catch(Exception e) 
	         { 
            	 try { dbOpSQL.close(); } catch (Exception ei) {}
            	 log.log_error("Tree::insertCategorie() - Inserimento CATEGORIE SQL: "+sql+" - Erroe:"+e.getMessage());
             	 throw new Exception("Tree::insertCategorie()\n"+e.getMessage());
	         }         
	  }
	  
	  private void addNodetree(NodeTree padre,String id,String text,String tipo,String nome,String id_oggetto_comp,String utente,String icona,String hasChild) 
	  {
			  NodeTree node = new NodeTree();
	          node.setId(id);
	          node.setText(text);
	          node.setTypeNode(tipo);
	          node.setTextQuickSort(nome);
	          node.setIdOggettoComp(id_oggetto_comp);
	          node.setUtenteDesk(utente);
	          node.setIcona(icona);
	          node.setHasChildren(hasChild);
	          padre.add(node);
	  }
	  
	  private void addNodetree(NodeTree padre,String id,String text,String tipo,String nome,String url,String tooltip,String icona,String nome_icona,String hasChild,String tipo_link) 
	  {
			  NodeTree node = new NodeTree();
	          node.setId(id);
	          node.setText(text);
	          node.setTextQuickSort(nome);
	          node.setTypeNode(tipo);
	          node.setHRef(url);
	          node.setTooltip(tooltip);
	          node.setIcona(icona);
	          node.setHasChildren(hasChild);
	          node.setTipoLink(tipo_link);
	          node.setNomeIcona(nome_icona);
	          padre.add(node);
	  }
	  
	  /**
	   * METHOD:      displayTree
	   * SCOPE:       PUBLIC
	   *
	   * DESCRIPTION: Costruzione XML del TreeView
	   * 
	   * RETURN:      String
	  */  
	  public String displayTree() throws Exception
	  {
		     if(vNodes.size()>0) {
		       sort(vNodes);
		       if(!this.expandNode)
		    	 tracciatoXMLLivelloRadice(vNodes);
		       else
			     tracciatoXMLNodo(vNodes);
		     }
		     else {
		       if(idNodo.equals("-"+sWrksp)) 
		    	tracciatoXMLLivelloRadice(); 	
		       else
			    tracciatoXMLNodo(vNodes);	
		     }
		     return xmlTree;
	  }
	  
	  private String getTreeViewSQLPerLivelli() throws Exception
	  {
	           String sql="";
	           String sListaNodi;
	           
	           if(idNodo!=null && !idNodo.equals("-"+sWrksp))
	        	sListaNodi=buildListaNodi(sWrksp);
	           else
	        	sListaNodi="-"+sWrksp;
	           
	           try
	           {
	        	  String campi,tabelle,where,union;
	              
	              campi="tree.haschildren haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID,";
	              campi+="decode(tree.tipo_oggetto,'C',c.nome,'Q',q.nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";
	              campi+="DEFAULT_IMAGE, CUSTOM_STYLE,";
	              campi+="tree.ID_CARTELLA, TIPO_OGGETTO,";
	              campi+="ID_OGGETTO, decode(tree.tipo_oggetto,'C',vw.id_viewcartella,'Q',q.id_query) id_oggetto_comp, :sWrksp wrksp, c2.nome nomeWRKSP, :ruolo ruolo, ";
	              campi+="DECODE (tree.tipo_oggetto, 'C','','Q',decode(instr(q.filtro,'RICERCAMODULISTICA_'),0,'',substr(q.filtro,length('RICERCAMODULISTICA_')+1,length(q.filtro)))) RICERCAMODULISTICA ";
	              campi+=", decode(tree.tipo_oggetto,'C',vw.ID_VIEWCARTELLA,'Q',id_oggetto) idcart, tree.ordine ";
	              tabelle="(SELECT   ROWNUM ordine,PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO,haschildren";
	              tabelle+=" from (";	              
	              tabelle+="        SELECT PARENTID PARENTID, NODEID, TEXT, URL,TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA,'C' TIPO_OGGETTO, ID_OGGETTO,";
	              tabelle+=" nvl((select 1 c ";
	              tabelle+=" from links, documenti, cartelle, modelli ";
	              tabelle+=" where links.id_cartella=cart_foglie.id_oggetto and links.tipo_oggetto='C' and ";
	              tabelle+=" cartelle.id_cartella=links.id_oggetto and cartelle.id_documento_profilo=documenti.id_documento ";
	              tabelle+=" AND documenti.id_tipodoc=modelli.id_tipodoc  AND modelli.tipo_uso in ('Q','C','W')";
	              tabelle+=" and nvl(stato_documento,'BO') not in ('CA','RE') ";
	              tabelle+=" union ";
	              tabelle+=" select 1 c ";
	              tabelle+=" from links, documenti, query ";
	              tabelle+=" where links.id_cartella=cart_foglie.id_oggetto and links.tipo_oggetto='Q' and ";
	              tabelle+=" query.id_query=links.id_oggetto and query.id_documento_profilo=documenti.id_documento and ";
	              tabelle+=" nvl(stato_documento,'BO') not in ('CA','RE') ";
	              tabelle+=" UNION SELECT 1 c FROM COLLEGAMENTI coll WHERE coll.id_Cartella=cart_foglie.id_oggetto ";
	              tabelle+=" UNION SELECT 1 c FROM links,collegamenti_esterni ";
	              tabelle+=" WHERE links.id_cartella = cart_foglie.id_oggetto AND links.tipo_oggetto = 'L' ";
	              tabelle+=" AND collegamenti_esterni.id_collegamento =links.id_oggetto) ";
	              tabelle+="		,0) haschildren ";
	              tabelle+="          FROM CART_FOGLIE";
	              tabelle+="         WHERE ID_CARTELLA IN ("+sListaNodi+")";
	              tabelle+="         UNION ALL";
	              tabelle+="        SELECT PARENTID PARENTID, NODEID, TEXT, URL,TOOLTIP, DEFAULT_IMAGE, CUSTOM_IMAGE, CUSTOM_STYLE, ID_CARTELLA,TIPO_OGGETTO, ID_OGGETTO,0 haschildren";
	              tabelle+="          FROM FOGLIE";
	              tabelle+="         WHERE ID_CARTELLA IN ("+sListaNodi+")";
	              tabelle+="       )"; 
	              tabelle+=" connect by prior  NODEID = PARENTID AND ID_CARTELLA IN ("+sListaNodi+")";  
	              tabelle+=" start with  ID_CARTELLA = :startNodo ORDER BY ordine ) tree";
	              tabelle+=", cartelle c, cartelle c2, QUERY q, tipi_documento td, documenti d, view_cartella vw";
	              where="tree.id_oggetto = c.id_cartella (+) and tree.id_oggetto = q.id_query (+) and c.id_cartella= vw.id_cartella(+)";          
	              where+=" and d.id_tipodoc = td.id_tipodoc AND c2.id_cartella = -:sWrksp";
	              where+=" and d.id_documento = decode(tree.tipo_oggetto,'C',c.id_documento_profilo,'Q',q.id_documento_profilo)";
	              where+=" AND decode(tree.tipo_oggetto,'C', NVL (c.stato, 'BO'),'BO') <> 'CA' ";
	              where+="  order by ordine ";
	              union=" ) UNION ALL ";
	              union+=" SELECT TO_NUMBER (NULL),TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), ";
	              union+=" TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), ";
	              union+=" TO_NUMBER (NULL), TO_CHAR (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL), TO_CHAR (NULL), TO_CHAR (NULL), ";
	              union+=" TO_CHAR (NULL),TO_CHAR (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL) ";
	              union+="  FROM DUAL ";
	             
	              if(jdmsML!=null && !jdmsML.equals("")){
	            	sql="SELECT haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, GDM_UTILITY.F_MULTILINGUA(TEXT,'"+jdmsML+"') TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";  
	            	sql+="DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO, id_oggetto_comp, wrksp,GDM_UTILITY.F_MULTILINGUA(nomeWRKSP,'"+jdmsML+"') nomeWRKSP,ruolo,RICERCAMODULISTICA,"; 
	              }  
	              else {
	            	sql="SELECT haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";
	            	sql+="DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO, id_oggetto_comp, wrksp,nomeWRKSP,ruolo,RICERCAMODULISTICA,"; 
	              }	  
	              sql+="idcart,ordine "
	            	  +"FROM ( "
	                  +"SELECT haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,"
	            	  +"DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO, id_oggetto_comp, wrksp,nomeWRKSP,ruolo,RICERCAMODULISTICA," 
	            	  +"idcart,ordine "
	            	  +"FROM ( "+Select(campi,tabelle,where,"")+union
	                  +" ), DUAL WHERE "
	            	  +GDM_L("decode(tipo_oggetto,'C','VIEW_CARTELLA','Q','QUERY')","decode(tipo_oggetto,'C',idcart,'Q',id_oggetto)");
	                  
	           } 
	           catch (Exception e) { 
	        	   log.log_error("Tree::getTreeViewSQLPerLivelli() - Costruzione tree per livelli SQL: "+sql+" - Erroe:"+e.getMessage());
	        	   throw e;
		       }
	          
	          return sql;   
	   } 
	
	   private String getTreeViewSQLPerTipoCartella() throws Exception
	   {
	           String sql="";
	           String sListaNodi;
	           
	           if(idNodo!=null && !idNodo.equals("-"+sWrksp))
	        	sListaNodi=buildListaNodi(sWrksp);
	           else
	        	sListaNodi="-"+sWrksp;
	           
	           if(listaDaEscludere!=null && !listaDaEscludere.equals(""))
	        	 listaDaEscludere=buildListaNodiDaEscludere(listaDaEscludere);
	        	   
	           try
	           {
	        	  String campi,tabelle,where,union;
	              
	              campi="tree.haschildren haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID,";
	              campi+="c.nome nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";
	              campi+="DEFAULT_IMAGE, CUSTOM_STYLE,";
	              campi+="tree.ID_CARTELLA, TIPO_OGGETTO,";
	              campi+="ID_OGGETTO, vw.id_viewcartella id_oggetto_comp, :sWrksp wrksp, c2.nome nomeWRKSP, :ruolo ruolo, ";
	              campi+="'' RICERCAMODULISTICA ";
	              campi+=", vw.ID_VIEWCARTELLA idcart, tree.ordine ";
	              tabelle="(SELECT   ROWNUM ordine,PARENTID, NODEID, TEXT, URL, TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO,haschildren";
	              tabelle+=" from (";	              
	              tabelle+="        SELECT PARENTID PARENTID, NODEID, TEXT, URL,TOOLTIP, CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, ID_CARTELLA,'C' TIPO_OGGETTO, ID_OGGETTO,";
	              tabelle+=" nvl((select distinct 1 c ";
	              tabelle+=" from links, documenti, cartelle, modelli ";
	              tabelle+=" where links.id_cartella=cart_foglie.id_oggetto and links.tipo_oggetto='C' and ";
	              tabelle+=" cartelle.id_cartella=links.id_oggetto and cartelle.id_documento_profilo=documenti.id_documento ";
	              tabelle+=" AND documenti.id_tipodoc=modelli.id_tipodoc  AND modelli.tipo_uso in ('Q','C','W')";
	              tabelle+=" and nvl(stato_documento,'BO') not in ('CA','RE') ),";
	              tabelle+="		0) haschildren ";
	              tabelle+="          FROM CART_FOGLIE";
	              tabelle+="         WHERE ID_CARTELLA IN ("+sListaNodi+")";
	              tabelle+="         UNION ALL";
	              tabelle+="        SELECT PARENTID PARENTID, NODEID, TEXT, URL,TOOLTIP, DEFAULT_IMAGE, CUSTOM_IMAGE, CUSTOM_STYLE, ID_CARTELLA,TIPO_OGGETTO, ID_OGGETTO,0 haschildren";
	              tabelle+="          FROM FOGLIE";
	              tabelle+="         WHERE ID_CARTELLA IN ("+sListaNodi+")";
	              tabelle+="       )"; 
	              tabelle+=" connect by prior  NODEID = PARENTID AND ID_CARTELLA IN ("+sListaNodi+")";  
	              tabelle+=" start with  ID_CARTELLA = :startNodo ORDER BY ordine ) tree";
	              tabelle+=", cartelle c, cartelle c2, tipi_documento td, documenti d, view_cartella vw ";
	              where="tree.id_oggetto = c.id_cartella (+) and tree.tipo_oggetto = 'C'";
	              if(!listaDaEscludere.equals(""))
		           where+=" and tree.id_oggetto not in ("+listaDaEscludere+") ";
	              where+=" and c.id_cartella= vw.id_cartella(+)";          
	              where+=" and d.id_tipodoc = td.id_tipodoc AND c2.id_cartella = -:sWrksp";
	              where+=" and d.id_documento = c.id_documento_profilo";
	              where+=" AND NVL (c.stato, 'BO') <> 'CA' ";
	              where+="  order by ordine ";
	              union=" ) UNION ALL ";
	              union+=" SELECT TO_NUMBER (NULL),TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), ";
	              union+=" TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL), ";
	              union+=" TO_NUMBER (NULL), TO_CHAR (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL), TO_CHAR (NULL), TO_CHAR (NULL), ";
	              union+=" TO_CHAR (NULL),TO_CHAR (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL) ";
	              union+="  FROM DUAL ";
	              
	              if(jdmsML!=null && !jdmsML.equals("")){
	               sql="SELECT haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, GDM_UTILITY.F_MULTILINGUA(TEXT,'"+jdmsML+"') TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";  
	               sql +="DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO, id_oggetto_comp, wrksp,GDM_UTILITY.F_MULTILINGUA(nomeWRKSP,'"+jdmsML+"') nomeWRKSP,ruolo,RICERCAMODULISTICA,";	
	              }
	              else {
		            sql="SELECT haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,";
		            sql +="DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO, id_oggetto_comp, wrksp,nomeWRKSP,ruolo,RICERCAMODULISTICA,";			            	
	              } 
	              sql +="idcart,ordine "
	            	  +"FROM ( "
	                  +"SELECT haschildren,icona,PARENTID,PARENTID IDCARTPROV, NODEID, upper(nome) nome, TEXT, URL, TOOLTIP, CUSTOM_IMAGE,"
	            	  +"DEFAULT_IMAGE, CUSTOM_STYLE,ID_CARTELLA, TIPO_OGGETTO,ID_OGGETTO, id_oggetto_comp, wrksp,nomeWRKSP,ruolo,RICERCAMODULISTICA," 
	            	  +"idcart,ordine "
	            	  +"FROM ( "+Select(campi,tabelle,where,"")+union
	                  +" ), DUAL WHERE "
	            	  +GDM_L("'VIEW_CARTELLA'","idcart");
	          	
	                  
	           } 
	           catch (Exception e) { 
	        	   log.log_error("Tree::getTreeViewSQLPerTipoCartella() - Costruzione tree per tipo cartella SQL: "+sql+" - Erroe:"+e.getMessage());
	        	   throw e;
	           }
	          
	          return sql;   
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
/*EX UPPER*/ decode+="'"+user+"', F_TRASLA_RUOLO('"+user+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 "; 

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
         	 String sql="SELECT "+campi+" ";
         			sql+="FROM "+elencoTabelle+" ";
         			sql+="WHERE "+condwhere+" ";
         	 return sql;
 	 }     
		
	/**
	  * Costruzione della lista di nodi da espandere 
	  * 
	  * @param  id   indica id della cartella WorkSpace
	  * 
	  * @return String lista di nodi
	 */   
	private String buildListaNodi(String wrskp) throws Exception
	{     
			String lista="";
            
			if(idNodo!=null && idNodo.indexOf("@")!=-1){
				slistaNodi="";
				StringTokenizer t = new StringTokenizer(idNodo,"@");
		        while (t.hasMoreTokens())
		        { 
		             slistaNodi+="@"+this.getPathFolder(dbOp.getConn(),t.nextToken(),null);
		        }
			}
			else
				slistaNodi=this.getPathFolder(dbOp.getConn(),idNodo,null);	
							
			
			if(slistaNodi.indexOf("-"+wrskp)==-1)
			  slistaNodi="0";
			
	        if(slistaNodi.equals("0"))
	           lista="-"+wrskp;	 
	        else
	        {
	        	StringTokenizer st = new StringTokenizer(slistaNodi,"@");
		        while (st.hasMoreTokens())
		        { 
		              String s=st.nextToken();
		              lista+=s+","; 
		        }
		        lista=lista.substring(0,lista.length()-1);
	        }  
	       
	        return lista;    
	 } 
	
	private String buildListaNodiDaEscludere(String listaDaEscludere) throws Exception
	{     
			String lista="";
		    StringTokenizer st = new StringTokenizer(listaDaEscludere,"@");
		    while (st.hasMoreTokens())
		    { 
		      String s=st.nextToken();
		      if(s.indexOf("C")!=-1){
		        s=s.substring(1,s.length());
		        lista+=s+","; 
		      }
		    }
		    if(!lista.equals(""))
		     lista=lista.substring(0,lista.length()-1);
	        return lista;    
	 } 
	  
	  /**
	   * METHOD:      sort
	   * SCOPE:       PRIVATE
	   *
	   * DESCRIPTION: Ordinamento della struttura TreeView
	   * 
	   * RETURN:      void
	  */  
	  private void sort(Vector nodi)
	  {
	        
	          Vector vCartelle = new Vector();
	          Vector vQuery = new Vector();
	
	          for(int i=0;i<nodi.size();i++)
	          {
	            if ((((NodeTree)nodi.get(i)).getTypeNode().equals("C")) || (((NodeTree)nodi.get(i)).getTypeNode().equals("X")))
	               vCartelle.add((NodeTree)nodi.get(i));
	            else
	               vQuery.add((NodeTree)nodi.get(i));                       
	          }
	         
	          QuickSort q = new QuickSort(vCartelle);
	          QuickSort q2 = new QuickSort(vQuery);
	         
	          for(int j=0;j<vQuery.size();j++) 
	             vCartelle.add((NodeTree)vQuery.get(j));
	
	          nodi.clear();
	
	          for(int j=0;j<vCartelle.size();j++){ 
	             nodi.add((NodeTree)vCartelle.get(j));
	          }
	          
	          for(int i=0;i<nodi.size();i++) {
	             sort(((NodeTree)nodi.get(i)).getChildNodes());
	          }
	  }
	  
	  /**
	   * METHOD:      findNode
	   * SCOPE:       PRIVATE
	   *
	   * DESCRIPTION: Ricerca riscorsiva del nodo tramite id
	   * 
	   * RETURN:      Node
	  */  
	  private NodeTree findNode(Vector nodes,String id, boolean isRicercaNOSottoAlbero) 
	  {
		      NodeTree temp=null;
	  
	          for(int i=0;i<nodes.size();i++) 
	          {
	              temp=(NodeTree)nodes.elementAt(i);
	              
	              if (("C"+temp.getId()).equals(id) && isRicercaNOSottoAlbero )
	                return temp;
	              else {
	                 if (temp.getChildNodes().size()>0) 
	                 {
						 temp = findNode(temp.getChildNodes(),id,isRicercaNOSottoAlbero);
	      				 if (temp!=null)
	      				   return temp;
	                 }
	              }
	          }
	          return null;
	  }
	  
	  private void tracciatoXMLLivelloRadice() throws Exception
	   {
		       try
	           {
			       Element eRoot,eRowset;
			       
			       if(nomeWrksp==null)
			        retrieveNomeWRKSP(dbOp.getConn());
			       
			       root = DocumentHelper.createElement("ROWSET");
				   Document dDoc = DocumentHelper.createDocument();
			       dDoc.setRootElement(root);
			       
			       eRoot=DocumentHelper.createElement("ROW");
	               Element idRoot,nomeRoot,tipoRoot;
	               
	               idRoot=DocumentHelper.createElement("ID");
	               idRoot.setText("-"+sWrksp);
	               nomeRoot=DocumentHelper.createElement("NOME");
	               nomeRoot.setText(nomeWrksp);
	               tipoRoot=DocumentHelper.createElement("TIPO");
	               tipoRoot.setText("W");		   
	               eRoot.add(idRoot);
	               eRoot.add(nomeRoot);
	               eRoot.add(tipoRoot);
	               
	               eRowset=DocumentHelper.createElement("ROWSET");
	               
	               if(tipo_operazione.equals("")){
	                insertCategorieElement(dbOp.getConn(),idNodo,eRowset);
	                insertCollegamentiEsterniElement(dbOp.getConn(),idNodo,eRowset);
	               }
	               
	               if(eRowset.elements().size()>0)
			        eRoot.add(eRowset);
			       root.add(eRoot);
			       xmlTree=dDoc.asXML();
                   // System.out.println(xmlTree);
	           }
		       catch(Exception e) 
		       { 
		    	   log.log_error("Tree::tracciatoXMLLivelloRadice() - Costruzione xml tree - idOggetto:"+idNodo+" - xmlTree: "+xmlTree+" - Erroe:"+e.getMessage());
	        	   throw new Exception("Tree::tracciatoXMLLivelloRadice() - idOggetto:"+idNodo+"\n"+e.getMessage());
		       }     
		       	       
	   }
	   
	   
	   private void tracciatoXMLLivelloRadice(Vector tree) throws Exception
	   {
		       try
	           {
			       Element eRoot,eRowset,elp=null;
			       
			       if(nomeWrksp==null)
			        retrieveNomeWRKSP(dbOp.getConn());
			       
			       root = DocumentHelper.createElement("ROWSET");
				   Document dDoc = DocumentHelper.createDocument();
			       dDoc.setRootElement(root);
			       
			       eRoot=DocumentHelper.createElement("ROW");
	               Element idRoot,nomeRoot,tipoRoot;
	               
	               idRoot=DocumentHelper.createElement("ID");
	               idRoot.setText("-"+sWrksp);
	               nomeRoot=DocumentHelper.createElement("NOME");
	               nomeRoot.setText(nomeWrksp);
	               tipoRoot=DocumentHelper.createElement("TIPO");
	               tipoRoot.setText("W");		   
	               eRoot.add(idRoot);
	               eRoot.add(nomeRoot);
	               eRoot.add(tipoRoot);               
			       
	               eRowset=DocumentHelper.createElement("ROWSET");
	               
			       for(int i=0;i<tree.size();i++)
			       {
			    	  NodeTree node=(NodeTree)tree.get(i);
			          elp = buildNodo(node.getId(),node.getText(),node.getTypeNode(),node.getTextQuickSort(),node.getUtenteDesk(),node.getTipoUso(),node.getHasChildren(),node.getChildNodes().size(),node.getParentId(),node.getIdOggettoComp(),null,node.getHRef(),node.getIcona(),node.getNomeIcona());
			          if (node.getChildNodes().size()>0)
			          {	 
			        	 buildNodo(elp,node.getChildNodes());
			          }	
			          eRowset.add(elp);
			           		             
			       }
			      
			       if(tipo_operazione.equals("")) {
			    	   if(!this.expandNode){
				    	 insertCategorieElement(dbOp.getConn(),"-"+sWrksp,eRowset);
				    	 insertCollegamentiEsterniElement(dbOp.getConn(),"-"+sWrksp,eRowset);
			    	   }
				       else{ 
				    	 insertCategorieElement(dbOp.getConn(),idNodo,eRowset);
				    	 insertCollegamentiEsterniElement(dbOp.getConn(),"-"+sWrksp,eRowset);
				       }
				   }
			       
			       eRoot.add(eRowset);
			       root.add(eRoot);
			       xmlTree=dDoc.asXML();
                   //System.out.println(xmlTree);
	           }
		       catch(Exception e) 
		       { 
		    	   log.log_error("Tree::tracciatoXMLLivelloRadice() - Costruzione xml tree - idOggetto:"+idNodo+" - xmlTree: "+xmlTree+" - Erroe:"+e.getMessage());
	        	   throw new Exception("Tree::tracciatoXMLLivelloRadice() - idOggetto:"+idNodo+"\n"+e.getMessage());
		       }     
		       	       
	   }
	   
	   private void tracciatoXMLNodo(Vector tree) throws Exception
	   {
		       try
	           {
			       Element elp=null;
			       root = DocumentHelper.createElement("ROWSET");
				   Document dDoc = DocumentHelper.createDocument();
			       dDoc.setRootElement(root);
			       
			       for(int i=0;i<tree.size();i++)
			       {
			    	     NodeTree node=(NodeTree)tree.get(i);
			    	     elp = buildNodo(node.getId(),node.getText(),node.getTypeNode(),node.getTextQuickSort(),node.getUtenteDesk(),node.getTipoUso(),node.getHasChildren(),node.getChildNodes().size(),node.getParentId(),node.getIdOggettoComp(),null,node.getHRef(),node.getIcona(),node.getNomeIcona());
			             root.add(elp);		             
			       }
			       
                   if(tipo_operazione.equals("")){
			        insertCategorieElement(dbOp.getConn(),idNodo,root);
			        insertCollegamentiEsterniElement(dbOp.getConn(),idNodo,root);
                   }
			       xmlTree=dDoc.asXML();
			       //System.out.println(xmlTree);
	           }
		       catch(Exception e) 
		       { 
		    	   log.log_error("Tree::tracciatoXMLNodo() - Costruzione xml nodo - idOggetto:"+idNodo+" - xmlTree: "+xmlTree+" - Erroe:"+e.getMessage());
		           throw new Exception("Tree::tracciatoXMLNodo() - idOggetto:"+idNodo+"\n"+e.getMessage());
		       }	       
	   }
	   
	   private void buildNodo(Element r,Vector tree)
	   {
		       Element e,eRowset;
		       
		       eRowset=DocumentHelper.createElement("ROWSET");
		       for(int i=0;i<tree.size();i++)
		       {
		    	     NodeTree node=(NodeTree)tree.get(i);
		             e = buildNodo(node.getId(),node.getText(),node.getTypeNode(),node.getTextQuickSort(),node.getUtenteDesk(),node.getTipoUso(),node.getHasChildren(),node.getChildNodes().size(),node.getParentId(),node.getIdOggettoComp(),null,node.getHRef(),node.getIcona(),node.getNomeIcona());
		            
		             if (node.getChildNodes().size()>0)
		             {
		            	 buildNodo(e,node.getChildNodes());
		             }
		             eRowset.add(e);
		       }
		       r.add(eRowset);
	   }
		   
	   private Element buildNodo(String id,String nome,String tipo, String tooltip,String utentedesk,String tipouso,String hasChildren,
			   int child,String idParent,String idoggettocomp,String tipo_link,String url,String icona,String nome_icona)
	   {
		       Element eNodo,eId,eNome,eTipo,eTooltip,eUtentedesk,eTipouso,eHasChildren,eParent,eIdoggettocomp,eTipo_link,eUrl,eIcona,eNomeIcona;
		       eNodo=DocumentHelper.createElement("ROW");
		       eId=DocumentHelper.createElement("ID");
		       eId.setText(id);
		       eNome=DocumentHelper.createElement("NOME");
		       eNome.setText(nome);
		       eTipo=DocumentHelper.createElement("TIPO");
		       eTipo.setText(tipo);
		       eTooltip=DocumentHelper.createElement("TOOLTIP");
		       eTooltip.setText(tooltip);	
		       eHasChildren=DocumentHelper.createElement("HASCHILDREN");
		       eHasChildren.setText(hasChildren);		       
		       eNodo.add(eId);
		       eNodo.add(eNome);
		       eNodo.add(eTipo); 
		       eNodo.add(eTooltip);		
		       eNodo.add(eHasChildren);		
		       
		       if(idParent!=null)
		       {
		    	   if(idParent.indexOf("C")!=-1)
		    		idParent=idParent.substring(1,idParent.length());  
		    	   eParent=DocumentHelper.createElement("IDPARENT");
		    	   eParent.setText(idParent);
			       eNodo.add(eParent);
		       }
		       
		       if(tipouso!=null)
		       {
		    	   eTipouso=DocumentHelper.createElement("TIPOUSO");
			       eTipouso.setText(tipouso);
			       eNodo.add(eTipouso);
		       }
		       
		       if(idoggettocomp!=null)
		       {
		    	   eIdoggettocomp=DocumentHelper.createElement("IDOGGETTOCOMP");
		    	   eIdoggettocomp.setText(idoggettocomp);
			       eNodo.add(eIdoggettocomp);
		       }
		       
		       if(utentedesk!=null)
		       {
		    	   eUtentedesk=DocumentHelper.createElement("UTENTEDESK");
		    	   eUtentedesk.setText(utentedesk);	
		    	   eNodo.add(eUtentedesk);	
		       }
		       
		       if((hasChildren!=null && hasChildren.equals("1")) && (child==0) && (sequenza.indexOf("@"+id+"@")==-1))
		       {
		    	 Element efolder=DocumentHelper.createElement("ROWSET"); 
		    	 eNodo.add(efolder);
		       }
		       
		       if(tipo_link!=null)
		       {
		    	   eTipo_link=DocumentHelper.createElement("TIPOLINK");
		    	   eTipo_link.setText(tipo_link);
			       eNodo.add(eTipo_link);
		       }
		       
		       if(url!=null)
		       {
		    	   eUrl=DocumentHelper.createElement("URL");
		    	   eUrl.setText(url);
			       eNodo.add(eUrl);
		       }
		       
		       if(icona!=null)
		       {
		    	   eIcona=DocumentHelper.createElement("ICONA");
		    	   eIcona.setText(icona);
			       eNodo.add(eIcona);
		       }
		       
		       if(nome_icona!=null)
		       {
		    	   eNomeIcona=DocumentHelper.createElement("NOMEICONA");
		    	   eNomeIcona.setText(nome_icona);
			       eNodo.add(eNomeIcona);
		       }
		       
		       return eNodo;
	   }
	   
	   private String executeFunzioneCollegamentoEsterno(Connection conn,String nome_funzione) throws Exception
	   {
		          String ret = "1"; 
			      IDbOperationSQL dbOpSQL= null;
				  
		          try
		          { 
		           dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(conn,0);	  
		           dbOpSQL.setCallFunc(nome_funzione+"()");
	 	    	   dbOpSQL.execute();
	 	    	   ret = dbOpSQL.getCallSql().getString(1);
		           dbOpSQL.close();
		         }
	             catch(Exception e) 
		         { 
	            	 try { dbOpSQL.close(); } catch (Exception ei) {}
	            	 log.log_error("Tree::executeFunzioneCollegamentoEsterno() - Esecuzione della FUNZION_LETTURA:"+nome_funzione+" - Erroe:"+e.getMessage());
			         return "1";
		         }
	             return ret;
		  }
	   
}

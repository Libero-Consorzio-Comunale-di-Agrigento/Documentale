package it.finmatica.dmServer.DMTree;

/*
 * GESTIONE TREEVIEW
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   05/01/2006
 * 
 * */

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.*;

import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URLEncoder;
import java.sql.*;

  
public class TreeView  
{
  /* Costanti Immagini */ 
  private static String _PATHIMG               ="images/";
  private static String _PATHIMG_ICONE         ="icone/";
  
  private static String _QRYSEARCH             =_PATHIMG+"qrysearch.gif";
  private static String _QRYSEARCH_COLL        =_PATHIMG+"collqrysearch.gif";
  private static String _CARTELLAPADRE         =_PATHIMG+"plusGDC.gif";
  private static String _CARTELLAPADRE_COLL    =_PATHIMG+"collplusGDC.gif";  
  private static String _CARTELLAFOLGLIA       =_PATHIMG+"folder.gif";
  private static String _CARTELLAFOLGLIA_COLL  =_PATHIMG+"collfolder.gif";
  
             
  // Variabili private
  private String mFolder,color;
  private Vector vNodes;
  private String defaultTarget,id;
  private StringBuffer sOut;
  private String marginLeft;
  private String plusImage,minusImage,leafImage,leafImageOpen;
  private String font,fontPt;  
  private String sTemp="";
  private String idCartellaSingola;
  private String sWrksp;
  private String ruolo;
  private String sUtente;
  private String sRuolo;
  private String sProvenienza="S";
  private String nodoDaSaltare="";
  private String slistaNodi;
  private String message;

  // Variabili di appoggio per la costruzione del treview
  private Vector vListNodes = new Vector();
  private Vector vDiv = new Vector();
  private String idToggle="";
  private String wrksp;
  private String toggle="";
 
  // Vettore di Collegamenti 
  private Vector vCollegamenti= new Vector();
  
  private class Collegamenti
  {
     private String id_collegamento=null;
     private String id_cartella_collegata=null;
     
     /*
      * METHOD:      Constructor
      *
      * DESCRIPTION: Inizializza dati
      * 
      * RETURN:      none
      */
      public Collegamenti(String newIdCollegamento, String newIdCartellaCol)  throws Exception {
               
          this.id_collegamento=newIdCollegamento;
          this.id_cartella_collegata=newIdCartellaCol;
      
      }
  
      // ***************** METODI GET E SET ***************** //
    
      public String getIdCollegamento() {
         return id_collegamento;
      } 

      public String getIdCartCollegata() {
         return id_cartella_collegata;
      }  
    
    
  }
   

  /*
   * METHOD:      Constructors
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: 
   * 
   * RETURN:      none
  */
  public TreeView()
  {
        this("black","images");
  }

  public TreeView(String newColor,String newMFolder)
  {
        vNodes = new Vector();
  		color=newColor;
		mFolder=newMFolder;
        sOut = new StringBuffer("");
        marginLeft="1";
        plusImage="plusGDC.gif";
        minusImage="minusGDC.gif";
        leafImage="dotGDC.gif";
        leafImageOpen="minusGDC.gif";
        font="tahoma";
        fontPt="9";
        id="";
        //message="window.showModelessDialog('AmvMessaggi.do?msg=Ricerca in corso...','','dialogHeight: 100px; dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; resizable: No; status: No;');";
        message="popupModelessDialog();";
  }

  /*
   * METHOD:      addSimpleNode
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento nodo semplice
   * 
   * RETURN:      void
  */  
  public void addSimpleNode(String text) {
	   	 Node tn = new Node(text);
		   vNodes.addElement(tn);
  }

  /*
   * METHOD:      addSimpleNode
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento nodo
   * 
   * RETURN:      void
  */  
  public void addNode(String text,String textQuickSort,String strHref,String strToolTipText) {
	   	 Node tn = new Node(text,strHref,strToolTipText);
		   vNodes.addElement(tn);
  }


  /*
   * METHOD:      display
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: 
   * 
   * RETURN:      void
  */ 
  public void display(IDbOperationSQL dbOp) throws Exception
  {
	       // Determina la relativa workspace SISTEMA o UTENTE 
         rootView();
           
         sOut.append("<script>\n");       
         sOut.append("if (navigator.appName == \"Microsoft Internet Explorer\") {\n");
         sOut.append("document.write('<style>');\n");
         sOut.append("document.write('   a.homepage{color:" + color + ";font-family:"+font+";font-size:10pt;} ');\n");
         sOut.append("document.write('   a.treeview{color:" + color + ";font-family:"+font+";font-size:"+fontPt+"pt;} ');\n");
         sOut.append("document.write('   a.treeview:link {text-decoration:none;} ');\n");
         sOut.append("document.write('   a.treeview:visited{text-decoration:none;} ');\n");
         sOut.append("document.write('   a.treeview:po{text-decoration:none;} ');\n");
         sOut.append("document.write('   a.treeview:hover {text-decoration:underline;} ');\n");       
         sOut.append("document.write('</style> ');\n");
         sOut.append("} else {\n");
         sOut.append("document.write('<style>');\n");
         sOut.append("document.write('   a.homepage{color:" + color + ";font-family:"+font+";font-size:10pt;cursor:pointer;} ');\n");
         sOut.append("document.write('   a.treeview{color:" + color + ";font-family:"+font+";font-size:"+fontPt+"pt;cursor:pointer;} ');\n");
         sOut.append("document.write('   a.treeview:link {text-decoration:none;cursor:pointer;} ');\n");
         sOut.append("document.write('   a.treeview:visited{text-decoration:none;cursor:pointer;} ');\n");
         sOut.append("document.write('   a.treeview:hover {text-decoration:underline;cursor:pointer;} ');\n");       
         sOut.append("document.write('</style> ');\n");
         sOut.append("}\n");
         sOut.append("</script>\n");
         
         sOut.append("<script src=\"include/Common.js\" type=\"text/javascript\" language=\"JavaScript\"></script>\n");
         
         // Ordinamento dei nodi del TreeView
         sort(vNodes);
        
         //Serve per visualizzare il treeView
         //ViewVector(vNodes);
         
         // Settaggio dei livelli del TreeView
         //SetLivTree(vNodes,1);
         //Costruzione del vettore d'appoggio
         LoadVector(vNodes,1);
         //LoadHidden(vListNodes);
         ViewListNodes(vListNodes,dbOp);
  }
  
  /*
   * METHOD:      LoadHidden
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Caricamento del valore hidden su ciascun nodo
   * 
   * RETURN:      void
  */  
  private void LoadHidden(Vector tree)
  {
          for(int i=0;i<tree.size();i++)
          {
             Node node=(Node)tree.get(i);
             
             if ( node.getChildNodes().size()>0 )   
                node.setHidden(false);
             else
                node.setHidden(true);
          }
  }
  
  /*
   * METHOD:      SetLivTree
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Settaggio dei livelli dei nodi
   * 
   * RETURN:      void
  */  
  private void SetLivTree(Vector tree,int liv) 
  {
          String pid;
          
          for(int j=0;j<tree.size();j++)
          {
             Node node=(Node) tree.get(j);
             pid=node.getParentId();
             
             // Nodo ROOT       
             if (pid.equals("0"))
               node.setLivello(1);
             else
               node.setLivello(liv);
     
             if (node.getChildNodes().size()>0)
               SetLivTree(node.getChildNodes(),node.getLivello()+1);
          }
  }  
  
  /*
   * METHOD:      LoadVector
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Costruzione del vettore d'appoggio vListNodes[]
   * 
   * RETURN:      void
  */  
  private void LoadVector(Vector tree,int liv)
  {
          for(int i=0;i<tree.size();i++)
          {
             Node node=(Node)tree.get(i);
            
             vListNodes.add(node);
             
             if (node.getParentId().equals("0"))
               node.setLivello(1);
             else
               node.setLivello(liv);
             
             if (node.getChildNodes().size()>0)
             {	 
               node.setHidden(false);	 
               LoadVector(node.getChildNodes(),node.getLivello()+1);
             }
             else
               node.setHidden(true);	 
          }
  }
  
  /*
   * METHOD:      LoadVector
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Costruzione del vettore d'appoggio vListNodes[]
   * 
   * RETURN:      void
  */  
  /*private void LoadVector(Vector tree)
  {
          for(int i=0;i<tree.size();i++)
          {
             Node node=(Node)tree.get(i);
             // Inserimento dell'elemento nel vettore d'appoggio 
             vListNodes.add(node);
             if (node.getChildNodes().size()>0)
             {	 
               node.setHidden(false);	 
               LoadVector(node.getChildNodes());
             }
             else
               node.setHidden(true);	 
          }
  }*/
  
  
  /*
   * METHOD:      ViewVector
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Visualizzazione del vettore vNodes[]
   * 
   * RETURN:      void
  */  
  private void ViewVector(Vector tree)
  {
          for(int i=0;i<tree.size();i++)
          {
             Node node=(Node)tree.get(i);
             
             // Visualizzazione dei nodi 
             System.out.println("Nodo= "+node.getText()+"-- ID= "+node.getId());
             
             if (node.getChildNodes().size()>0)
               ViewVector(node.getChildNodes());
          }
  }
 
  /*
   * METHOD:      ViewListNodes
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Visualizzazione della Lista dei nodi
   * 
   * RETURN:      void
  */ 
  private void ViewListNodes(Vector tree, IDbOperationSQL dbOp) throws Exception
  {
          String sImageURL="",sText="";
          String padre;
          disegnaTreeView();
         
          sOut.append("<script>\n");
          sOut.append("function voce(livello,nome,link,icona,foglia,padre) {\n");
          sOut.append("this.livello = livello;\n");
          sOut.append("this.nome = nome;\n");
          sOut.append("this.link = link;\n");
          sOut.append("this.icona = icona;\n");
          sOut.append("this.foglia = foglia;\n");
          sOut.append("this.padre = padre;}\n");
          sOut.append("var elemento = new Array();\n");
          sOut.append("var i = 0;\n");
          sOut.append("elemento[++i] = new voce(1,'Home Page','','home.gif',true,'1');\n");
          
          // Inserimento degli elementi nel vettore ELEMENTO[i]
          for(int i=0;i<tree.size();i++)
          {
             Node n=(Node)tree.get(i);
             
             String ntext=n.getText();
             //ntext=Global.replaceAll(Global.replaceAll(ntext,"'","\\'"),"\"","&quot;");
             ntext=Global.replaceAll(Global.replaceAll(Global.replaceAll(Global.replaceAll(ntext,"'","\\'"),"\"","&quot;"),"\n",""),"\r","");
             
             // Settaggio dell'immagine effettuando il controllo 
             // sul'icona del tipo documento             
             if( (n.getIcona()!=null))
             {
               // Controllare se si tratta di una Cartella Collegata
               if(n.getIdCollegamento() == null)
               {
            	  try{
                   sImageURL=_PATHIMG_ICONE+"/"+n.getIcona()+"/"+getIcona(dbOp.getConn(),n.getIcona());
            	  }
                  catch (Exception e) {
                    sImageURL=_PATHIMG_ICONE+_CARTELLAFOLGLIA; 
                  }
               }               
               else
               {
                  try {
                	  sImageURL=_PATHIMG_ICONE+"/"+n.getIcona()+"/"+getIcona(dbOp.getConn(),n.getIcona());
                  }
                  catch (Exception e) {
                      sImageURL=_PATHIMG_ICONE+_CARTELLAFOLGLIA; 
                  }
               }  
             }
             else
             {  
                 // Settaggio standard dell'immagine
                 if (n.getChildNodes().size()>0) // se possiede figli è un nodo cartella padre
                    if (n.getIdCollegamento()!=null && ("C"+n.getIdCartellaCollegata()).equals(n.getId()))
                    	sImageURL=_CARTELLAFOLGLIA_COLL;//sImageURL=_CARTELLAPADRE_COLL;
                    else
                        sImageURL=_CARTELLAPADRE;
                 else // se è nodo foglia
                   if(n.getTypeNode().equals("C")) // se è un nodo cartella
                     sImageURL=_CARTELLAFOLGLIA;
                   else
                     if(n.getTypeNode().equals("X")) // se è un nodo cartella Collegata
                        sImageURL=_CARTELLAFOLGLIA_COLL;
                     else // se è un nodo query
                        sImageURL=_QRYSEARCH;
                
             }
            
             //Settaggio se un nodo possiede figli
             if(slistaNodi.equals("0"))
               padre="0";
             else
             {
               if(n.getChildNodes().size()>0)
                 padre="1";	
               else
                 padre="-1";
             }
             
             
             if (ruolo.equals("DONTCARE"))
             {
                StringTokenizer s= new StringTokenizer(n.getText(),"<");
                if (s.hasMoreTokens())
                  sText=s.nextToken();
             
                String stext=Global.replaceAll(Global.replaceAll(Global.replaceAll(Global.replaceAll(sText,"'","\\'"),"\"","&quot;"),"\n","&nbsp;"),"\r","");
                sOut.append("elemento[++i] = new voce("+n.getLivello()+",'"+stext+"','"+n.getId().substring(1,n.getId().length())+"','"+sImageURL+"',false,'"+padre+"');\n"); 
             }
             else
             {
            	 //sOut.append("elemento[++i] = new voce("+n.getLivello()+",'"+ntext+"','"+n.getId().substring(1,n.getId().length())+"','"+sImageURL+"',"+n.getHidden()+");\n"); 
                 /*if(n.getTypeNode().equals("C"))
            	   sOut.append("elemento[++i] = new voce("+n.getLivello()+",'"+ntext+"','"+n.getId().substring(1,n.getId().length())+"','"+sImageURL+"',false,'"+padre+"');\n"); 
                 else
              	   sOut.append("elemento[++i] = new voce("+n.getLivello()+",'"+ntext+"','"+n.getId().substring(1,n.getId().length())+"','"+sImageURL+"',true,'0');\n"); 
                 */
                 
                 if(n.getTypeNode().equals("C"))
              	   sOut.append("elemento[++i] = new voce("+n.getLivello()+",'"+ntext+"','"+n.getId().substring(1,n.getId().length())+"','"+sImageURL+"',false,'"+padre+"');\n"); 
                 else
                   sOut.append("elemento[++i] = new voce("+n.getLivello()+",'"+ntext+"','"+n.getId().substring(1,n.getId().length())+"','"+sImageURL+"',true,'0');\n"); 
                   
             }
       
          }// end for
          
          // Inserimento dell'ultimo elemento nel vettore per 
          // risolvere un caso particolare nel treeview
          sOut.append("elemento[++i] = new voce(1,'','','',false,'0');\n");
          sOut.append("var nascosto = new Array();\n");
          sOut.append("for (c=1;c<i;c++) {\n");
          sOut.append("if (elemento[c].livello==1) nascosto[c] = false;\n");
          sOut.append("else nascosto[c] = true;}\n");
          sOut.append("</script>\n");
 }        

  /*
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
            if ((((Node)nodi.get(i)).getTypeNode().equals("C")) || (((Node)nodi.get(i)).getTypeNode().equals("X")))
               vCartelle.add((Node)nodi.get(i));
            else
               vQuery.add((Node)nodi.get(i));                       
          }
         
          QuickSort q = new QuickSort(vCartelle);
          QuickSort q2 = new QuickSort(vQuery);
         
          for(int j=0;j<vQuery.size();j++) 
             vCartelle.add((Node)vQuery.get(j));

          nodi.clear();

          for(int j=0;j<vCartelle.size();j++){ 
             nodi.add((Node)vCartelle.get(j));
          }
          
          for(int i=0;i<nodi.size();i++) {
             sort(((Node)nodi.get(i)).getChildNodes());
          }
  }
  
  
   /*
   * METHOD:      getCollegamenti
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Controlla l'esistenza di uno o più collegamenti caricandoli
   *              nel vettore vCollegamenti
   * 
   * RETURN:      void
  */  
  private void getCollegamenti(Connection conn,String idCartella) throws Exception
  {
          /*String sql;

          try
          { 
           sql="select id_collegamento,id_cartella_collegata ";
           sql+=" from collegamenti ";
           sql+=" where id_cartella= :idCartella";
           //System.out.println("sql-Collegamenti= "+sql);
           dbOp.setStatement(sql.toString());
           dbOp.setParameter(":idCartella", idCartella);
           dbOp.execute();
           ResultSet rst = dbOp.getRstSet();
         
           // Pulisce il vettore 
           vCollegamenti.clear();
          
           // caricamento dei collegamenti nel vettore vCollegamenti       
           while (rst.next()) 
           { 
              Collegamenti coll= new Collegamenti(rst.getString(1),rst.getString(2));  
              vCollegamenti.add(coll); 
           }

         }
         catch(Exception e) 
         {
           throw new Exception("TreeView::getCollegamenti()\n"+e.getMessage());
         }    */
  }
  
  /*
   * METHOD:      getIcona
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Reupero del nome di icona da associare al tipo documento
   * 
   * RETURN:      String
  */  
  private String getIcona(Connection conn,String icona) throws Exception
  {
          String sql,nomeIcona="";
          IDbOperationSQL dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(conn,0);
          
          try {			    
            
            sql="select nome from icone where icona = :icona";
            dbOpSQL.setStatement(sql);
            dbOpSQL.setParameter(":icona", icona);
            dbOpSQL.execute();
		    ResultSet rst = dbOpSQL.getRstSet();
			if(rst.next())
              nomeIcona= rst.getString(1);
			dbOpSQL.close();
         }
         catch ( SQLException e ) {
            try { dbOpSQL.close(); } catch (Exception ei) {}
            throw new Exception("TreeView::getIcona\n" + e.getMessage());           
         }  
          
         return nomeIcona;
  }
  
  /*
   * METHOD:      loadFromDb
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Caricamento della struttura TreeView 
   * 
   * RETURN:      void
  */  
  public void loadFromDb(IDbOperationSQL dbOp,String select,String idCollegamento,String idParent,String sWrkSp,String sListaNodi) throws Exception
  {
         String idNode;
         Collegamenti coll;
         String parentID;
         String idCartProv;
         Node parentNode;
         Node collegamentoNode;
         
         try
         { 
        
           dbOp.setParameter(":sWrkSp",sWrkSp);
           dbOp.setParameter(":sListaNodi",sListaNodi);
           dbOp.setParameter(":nodoDaSaltare",nodoDaSaltare);
           dbOp.setStatement(select);
           dbOp.execute();
           ResultSet rst = dbOp.getRstSet();
     
           while (rst.next()) 
           { 
              try {                
                if (rst.getString("NODEIDESCLUSO").indexOf(rst.getString("NodeID"))!=-1)
                 continue;       
              }
              catch(Exception e)
              {}
              
              if(idParent!=null)
                parentID=idParent;
              else {
                parentID=rst.getString("ParentID");                
              }
              
              idCartProv=rst.getString("IDCARTPROV");
              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setTextQuickSort(rst.getString("NOME"));
              node.setId(rst.getString("NodeID"));
              node.setTypeNode(rst.getString("NodeID").substring(0,1));
              node.setCompDel(rst.getString("CompDelOgg"));
              node.setCompMod(rst.getString("CompModOgg"));          
              node.setCompDocCart(rst.getString("CompDocCartel"));
              node.setCompComp(rst.getString("CompCompOgg"));
              // Setta l'icona 
              node.setIcona(rst.getString("icona"));
              // Setta il parentID
              node.setParentId(idCartProv);
              // Setta il collegamento a null
              node.setIdCollegamento(null);
              sWrksp=rst.getString("wrksp");
              ruolo=rst.getString("ruolo");
              
              if (rst.getString("NodeID").substring(0,1).equals("Q"))
              {
                 node.setTarget("");
                 node.setHRef(null);
                 
                 //Caso di Query di ricerca Modulistica
                 String rm=rst.getString("RICERCAMODULISTICA");
	        	 if((rm!=null) && (!rm.equals("")))
	        	 { 	 
	        	   String sarea=rm.substring(0,rm.indexOf("@"));
		           String scm=rm.substring(rm.indexOf("@")+1,rm.length());
		           String gdc_link="../common/WorkArea.do?idQuery="+rst.getString("ID_OGGETTO")+"&idCartAppartenenza="+rst.getString("IDCARTPROV")+"&tipoUso=R";
		           String parametri="idQuery="+node.getId().substring(1,node.getId().length())+"&area="+sarea+"&cm="+scm+"&idCartAppartenenza="+rst.getString("IDCARTPROV")+"&cr=RICERCA&rw=Q&GDC_Link="+URLEncoder.encode(gdc_link);
		           String url="../restrict/ServletRicercaModulistica.do?"+parametri;
		           node.setOnClick("onclick=\\\"if(window.parent.document.getElementById('disableOnclick').value=='1'){"+message+"if (parent.document.getElementById('workArea')) parent.document.getElementById('workArea').src='"+url+"'}else return false;\\\" ");
		           node.setTipoUso("R");
	        	 }
	          	 else
	          	   node.setOnClick("onclick=\\\"if(window.parent.document.getElementById('disableOnclick').value=='1'){"+message+"if (parent.document.getElementById('workArea')) "+ rst.getString("url") + "}else return false;\\\"");
	          }	
              
              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));              
              
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setDefaultImage(rst.getString("DEFAULT_IMAGE"));
          
              // ID del nodo            
              idNode=node.getId().substring(1,node.getId().length());
              
              //if (parentID.equals("0"))
              if (parentID.equals("C-"+wrksp))
             	  vNodes.addElement(node);                 
              else
              {
                parentNode = findNode(vNodes,parentID,true);               
                if (parentNode!=null)
                    parentNode.add(node);
              }
           
             // Se il tipo di operazione che si vuole effettuare nel
             // TreeViewCopiaSposta è l'operazione di COPIA allora
             // occorre escludere la visualizzazione delle Cartelle Collegate
             if((sProvenienza.equals("C")) && ((sProvenienza!=null))) 
                continue;
             else  
             {
               // Nel caso di operazione di sposta di almeno una cartella collegata
               // non vengono visualizzati tutti i collegamenti
               if(nodoDaSaltare.indexOf("X")!=-1) 
                 continue;
            	
             } 
          }//end while

         }
         catch(Exception e) 
         {
             throw new Exception("TreeView::loadFromDb()\n"+e.getMessage());
         }         
  }
 
   /*
   * METHOD:      findCollegamenti
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Ricerca se esistono uno o più collegamenti per la Cartella idNode
   * 
   * RETURN:      void
  */  
  private void findCollegamenti(Connection conn,String idNode,Node n) throws Exception
  {
          Collegamenti coll;
          
          // Controlla l'esistenza di uno o più collegamenti caricandoli
          // nel vettore vCollegamenti
          //getCollegamenti(conn,idNode);
                	 
          if( vCollegamenti.size()>0 )
          {  
         	 for(int i=0;i<vCollegamenti.size();i++)
             {
               coll=(Collegamenti)vCollegamenti.get(i);
               //appendiNodo(conn,getNodoCollegato(idNode,coll.getIdCartCollegata(),coll.getIdCollegamento()),coll.getIdCollegamento(),coll.getIdCartCollegata(),idNode,n);
                           
             } 
          } 
  }

  /*
   * METHOD:      appendiSottoAlbero
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Caricamento del sottoalbero collegamento della Cartella  idParent
   * 
   * RETURN:      void
  */  
  private void appendiSottoAlbero(Connection conn,String select,String idCollegamento,String idCartellaCollegata,String idParent, Node nRoot) throws Exception
  {
          /*String idNode;
          Collegamenti coll;
          String parentID;
          String idCartProv;
          Node parentNode;
          Node collegamentoNode;
          Vector vNRoot = new Vector();
          vNRoot.addElement(nRoot);
          int i=0;
         
          try
          { 
           dbOp.setStatement(select);
           dbOp.execute();
           ResultSet rst = dbOp.getRstSet();
           while (rst.next()) 
           {               
             
              try {                
                if(rst.getString("NODEIDESCLUSO").indexOf(rst.getString("NodeID"))!=-1)
                {
                  //System.out.println("NodeIDESC "+rst.getString("NODEIDESCLUSO")+" -- ID "+rst.getString("NodeID"));
                  continue;
                }
              }
              catch(Exception e)
              {}
              
              
              parentID=rst.getString("ParentID");
              idCartProv=rst.getString("IDCARTPROV");
              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setTextQuickSort(rst.getString("NOME"));
              node.setId(rst.getString("NodeID"));
              
              if(i==0)
               node.setTypeNode("X");
              else
               node.setTypeNode(rst.getString("NodeID").substring(0,1));
              
              node.setCompDel(rst.getString("CompDelOgg"));
              node.setCompMod(rst.getString("CompModOgg"));          
              node.setCompDocCart(rst.getString("CompDocCartel"));
              node.setCompComp(rst.getString("CompCompOgg"));
              // Setta l'icona 
              node.setIcona(rst.getString("icona"));
              // Setta il parentID
              node.setParentId(idCartProv);
              // Setta idCollegamento
              node.setIdCollegamento(idCollegamento);
              // Setta idCartellaCollegata
              node.setIdCartellaCollegata(idCartellaCollegata);
              // Setta idParent della Cartella Collegata
              node.setParentIDCollegata(idParent);
              sWrksp=rst.getString("wrksp");
              ruolo=rst.getString("ruolo");
              
              if (rst.getString("NodeID").substring(0,1).equals("Q"))
              {
                 node.setTarget("");
                 node.setHRef(null);
                 node.setOnClick("href=\\\"#\\\" onclick=\\\"if(window.parent.document.getElementById('disableOnclick').value=='1'){"+message+rst.getString("url") + "}else return false;\\\"");
              }
             
              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));              
              
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setDefaultImage(rst.getString("DEFAULT_IMAGE"));
            
              idNode=node.getId().substring(1,node.getId().length());
              
              //Sono sul primo nodo: cartella collegata   
              //La aggiungo al padre del collegamento
              if (i==0)
                 nRoot.add(node);                                  
      		  else
              {
                parentNode = findNode(vNRoot,parentID,false);               
                if (parentNode!=null)
                    parentNode.add(node);
              }
    
              // findCollegamenti(dbOp.getConn(),idNode,node);
              i++;
         }//end while

         }
         catch(Exception e) 
         {
           throw new Exception("TreeView::appendiSottoAlbero()\n"+e.getMessage());
         }         */
  }
 
   /*
   * METHOD:      getSottoAlbero
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Costruzione Select del sottoalbero
   * 
   * RETURN:      String
  */  
  private String getSottoAlbero(String idCartCollegata,String idCollegamento) throws Exception
  {
          String sql;
          String swrksp="";
          
          if(wrksp.equals("2")) swrksp="U"; else swrksp="S";
        
          if (ruolo.equals("DONTCARE"))
          {
            sql="  SELECT F_IconaFromCartellaOrQuery(id_oggetto,tipo_oggetto) icona, "; 
            sql+=" 'C'||l.ID_CARTELLA  PARENTID, ";
            sql+=" 'C'||to_char(l.ID_CARTELLA) IDCARTPROV, TIPO_OGGETTO||ID_OGGETTO NODEID, ";
            sql+=" F_LINK(ID_OGGETTO, TIPO_OGGETTO) TEXT, ";
            sql+=  Global.replaceAll(nodoDaSaltare,"X","C")+" NODEIDESCLUSO, ";
            sql+=" to_char(null) URL, ";
            sql+=" TO_CHAR(NULL) TOOLTIP, 'collplusGDC.gif' CUSTOM_IMAGE, F_LINK(ID_OGGETTO, TIPO_OGGETTO) NOME, 'collplusGDC.gif' DEFAULT_IMAGE, ";
            sql+=" TO_CHAR(NULL) CUSTOM_STYLE , l.ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO, '"+wrksp+"' wrksp, ";
            sql+=" 'DONTCARE' ruolo , ";
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","D")+" CompDelOgg, ";        
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","U")+" CompModOgg, ";        
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","C")+" CompDocCartel, ";
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","M")+" CompCompOgg ";        
            sql+=" FROM LINKS l ";
            sql+=" WHERE "+GDM("'VIEW_CARTELLA'","F_IDVIEW_CARTELLA("+idCartCollegata+")","L")+"=1 ";        
            sql+=" START WITH  "+idCartCollegata+" = ID_OGGETTO ";
            sql+=" CONNECT BY PRIOR ID_OGGETTO = l.ID_CARTELLA ";
            sql+=" AND  PRIOR TIPO_OGGETTO = 'C' ";
            sql+=" AND  F_TIPO_COMPETENZA(ID_OGGETTO, TIPO_OGGETTO)='"+swrksp+"'";
            sql+=" AND  l.tipo_oggetto <> 'Q' ";
          }
          else
          {
            sql="  SELECT F_IconaFromCartellaOrQuery(id_oggetto,tipo_oggetto) icona, "; 
            sql+=" 'C'||l.ID_CARTELLA  PARENTID, ";
            sql+=" 'C'||to_char(l.ID_CARTELLA) IDCARTPROV, TIPO_OGGETTO||ID_OGGETTO NODEID, ";
            sql+=" F_LINK(ID_OGGETTO, TIPO_OGGETTO) NOME, F_LINK(ID_OGGETTO, TIPO_OGGETTO) TEXT, ";
            sql+=" decode(tipo_oggetto,'Q','parent.document.getElementById(''workArea'').src=''WorkArea.do?idQuery='||ID_OGGETTO||'&idCollegamento='||"+idCollegamento+"||'&idCartAppartenenza='||ID_CARTELLA||'''',to_char(null)) URL, ";
            sql+=" TO_CHAR(NULL) TOOLTIP, 'collplusGDC.gif' CUSTOM_IMAGE, 'collplusGDC.gif' DEFAULT_IMAGE, ";
            sql+=" TO_CHAR(NULL) CUSTOM_STYLE , l.ID_CARTELLA, TIPO_OGGETTO, ID_OGGETTO, '"+wrksp+"' wrksp, ";
            sql+=" '"+sRuolo+"' ruolo , ";
            sql+=" TO_CHAR(NULL) NODEIDESCLUSO, ";
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","D")+" CompDelOgg, ";        
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","U")+" CompModOgg, ";        
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","C")+" CompDocCartel, ";
            sql+=GDM("decode(tipo_oggetto,'C','VIEW_CARTELLA','QUERY')","decode(tipo_oggetto,'C',f_idview_cartella (id_oggetto),id_oggetto)","M")+" CompCompOgg ";        
            sql+=" FROM LINKS l ";
            sql+=" WHERE "+GDM("'VIEW_CARTELLA'","F_IDVIEW_CARTELLA("+idCartCollegata+")","L")+"=1 ";        
            sql+=" START WITH  "+idCartCollegata+" = ID_OGGETTO ";
            sql+=" CONNECT BY PRIOR ID_OGGETTO = l.ID_CARTELLA ";
            sql+=" AND  PRIOR TIPO_OGGETTO = 'C' ";
            sql+=" AND  F_TIPO_COMPETENZA(ID_OGGETTO, TIPO_OGGETTO)='"+swrksp+"'";
          }
          return sql;
  } 
  
  /*
   * METHOD:      appendiNodo
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Caricamento del sottoalbero collegamento della Cartella  idParent
   * 
   * RETURN:      void
  */  
  private void appendiNodo(Connection conn,String select,String idCollegamento,String idCartellaCollegata,String idParent, Node nRoot) throws Exception
  {
         /* try
          { 
           dbOp.setStatement(select);
           dbOp.execute();
           ResultSet rst = dbOp.getRstSet();
           while (rst.next()) 
           {               
              try {                
                if(rst.getString("NODEIDESCLUSO").indexOf(rst.getString("NodeID"))!=-1)
                  continue;
              }
              catch(Exception e)
              {}
              
              //Costruzione del Nodo Collegato
              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setTextQuickSort(rst.getString("NOME"));
              node.setId(rst.getString("NodeID"));
              node.setTypeNode("X");
              node.setCompDel(rst.getString("CompDelOgg"));
              node.setCompMod(rst.getString("CompModOgg"));          
              node.setCompDocCart(rst.getString("CompDocCartel"));
              node.setCompComp(rst.getString("CompCompOgg"));
              // Setta l'icona 
              node.setIcona(rst.getString("icona"));
              // Setta il parentID
              node.setParentId(rst.getString("IDCARTPROV"));
              // Setta idCollegamento
              node.setIdCollegamento(idCollegamento);
              // Setta idCartellaCollegata
              node.setIdCartellaCollegata(idCartellaCollegata);
              // Setta idParent della Cartella Collegata
              node.setParentIDCollegata(idParent);
              sWrksp=rst.getString("wrksp");
              ruolo=rst.getString("ruolo");
              node.setTarget("");
              node.setHRef(null);
              //node.setOnClick("href=\\\"#\\\" onclick=\\\""+message+rst.getString("url") + "\\\"");
              //node.setOnClick("href=\\\"#\\\" onclick=\\\"if(document.getElementById('disableOnclick').value=='1'){"+message+rst.getString("url") + "}else return false;\\\"");
              node.setOnClick("href=\\\"#\\\" onclick=\\\"if(window.parent.document.getElementById('disableOnclick').value=='1'){"+message+rst.getString("url") + "}else return false;\\\"");
          		           
              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));              
              
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setDefaultImage(rst.getString("DEFAULT_IMAGE"));
            
              //Assegnamento del nodo Collegato 
              nRoot.add(node);        
              
              
          }//end while

         }
         catch(Exception e) 
         {
           throw new Exception("TreeView::appendiNodo()\n"+e.getMessage());
         }  */
  }
  
  
  
  /*
   * METHOD:      getNodoCollegato
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Costruzione Select del sottoalbero
   * 
   * RETURN:      String
  */  
  private String getNodoCollegato(String idpadre,String idCartCollegata,String idCollegamento) throws Exception
  {
          String sql;
          
          sql="  select F_IconaFromCartellaOrQuery(id_cartella_collegata,'C') icona,"; 
          sql+=" 'C'||cl.id_cartella  PARENTID,'C'||to_char(cl.id_cartella) IDCARTPROV, ";
          sql+=" 'C'||id_cartella_collegata nodeid,c.nome nome,c.nome text, ";
          sql+=" 'getLinkDiretto(''C'||id_cartella_collegata||''','''||F_WRKSP(id_cartella_collegata,'C')||''');' url, ";
          sql+=" TO_CHAR(NULL) TOOLTIP, 'collfolder.gif' CUSTOM_IMAGE, 'collfolder.gif' DEFAULT_IMAGE, ";
          sql+=" TO_CHAR(NULL) CUSTOM_STYLE , cl.ID_CARTELLA,'C' tipo_oggetto,id_cartella_collegata id_oggetto,";
	      sql+=" '"+wrksp+"' wrksp, '"+sRuolo+"' ruolo , ";
	      sql+=GDM("'VIEW_CARTELLA'","vw.id_viewcartella","D")+" CompDelOgg, ";        
	      sql+=GDM("'VIEW_CARTELLA'","vw.id_viewcartella","U")+" CompModOgg, ";        
	      sql+=GDM("'VIEW_CARTELLA'","vw.id_viewcartella","C")+" CompDocCartel, ";        
	      sql+=GDM("'VIEW_CARTELLA'","vw.id_viewcartella","M")+" CompCompOgg, ";        
	      sql+="vw.id_viewcartella idcart ";
	      sql+=" from collegamenti cl,cartelle c,view_cartella vw ";
	      sql+=" WHERE ("+GDM("'VIEW_CARTELLA'","F_IDVIEW_CARTELLA("+idCartCollegata+")","L")+"=1) ";        
	      sql+=" and cl.id_cartella="+idpadre;
	      sql+=" and id_cartella_collegata="+idCartCollegata;
	      sql+=" and id_cartella_collegata=c.id_cartella ";
	      sql+=" and id_cartella_collegata=vw.id_cartella";
          return sql;
  } 
   
  /*
   * METHOD:      GDM
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica le competenze dell'oggetto
   * 
   * RETURN:      String
  */  
  private String GDM(String oggetto,String idOggetto,String azione)
  {     
          String decode="";
          decode="GDM_COMPETENZA.GDM_VERIFICA("+oggetto+","+idOggetto+", '"+azione+"', '";
          decode+=sUtente+"',  F_TRASLA_RUOLO('"+sRuolo+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))";
          return decode;     
  }
  
  /*
   * METHOD:      findNode
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Ricerca riscorsiva del nodo tramite id
   * 
   * RETURN:      Node
  */  
  private Node findNode(Vector nodes,String id, boolean isRicercaNOSottoAlbero) 
  {
          Node temp=null;
  
          for(int i=0;i<nodes.size();i++) 
          {
              temp=(Node)nodes.elementAt(i);
              
              if (temp.getId().equals(id) && 
                   ( 
                     (isRicercaNOSottoAlbero && temp.getIdCollegamento()==null) ||
                     (!isRicercaNOSottoAlbero && temp.getIdCollegamento()!=null)
                   )
                 )
                return temp;
              else {
                 if (temp.getChildNodes().size()>0) {
					           temp = findNode(temp.getChildNodes(),id,isRicercaNOSottoAlbero);
      				    	 if (temp!=null)
      						       return temp;
                 }
              }
          }
          return null;
  }

  /*
   * METHOD:      displayLivello
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Visualizzazione dei livelli del TreeView
   * 
   * RETURN:      void
  */  
  private void displayLivello(Vector tree)
  {
          for(int j=0;j<tree.size();j++)
           {
             Node node=(Node) tree.get(j);
             if (node.getChildNodes().size()>0)
               displayLivello(node.getChildNodes());
           }
  }

  /*
   * METHOD:      disegnaTreeView
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Costruzione e caricamento dei blocchi dei nodi
   * 
   * RETURN:      void
  */ 
  private void disegnaTreeView() throws Exception	
  {
          // Costruzione del blocco di ciascun nodo
          loopThru(vListNodes,"0");
          
          // Caricamento nel vettore vDiv[] 
          sOut.append("<script>\n");
          sOut.append("var vDiv = new Array(\n");
          if(vDiv.size()==0)
            sOut.append(");\n");
          else
            for (int i=0;i<vDiv.size();i++)
            {
              if (i<vDiv.size()-1)
                sOut.append("\""+vDiv.get(i)+"\",\n");
              else
                sOut.append("\""+vDiv.get(i)+"\");\n");
            }    
          sOut.append("</script>\n");  
  }
  
  /*
   * METHOD:      rootView
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Setta il valore di workspace
   *              WORKSPACE SISTEMA o UTENTE  
   * 
   * RETURN:      void
  */ 
  private void rootView()	
  {
          sOut.append("<script>\n");
          if ((wrksp==null) || (wrksp.equals("2")))
            sOut.append("root=\"Utente\";\n");
          else
           if ((wrksp==null) || (wrksp.equals("1")))
            sOut.append("root=\"Sistema\";\n");
          sOut.append("</script>\n"); 
  }        
  
  /*
   * METHOD:      loopThru
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Costruzione dei blocchi di ciascun nodo
   * 
   * RETURN:      void
  */ 
  private void loopThru(Vector nodeList,String parent)	throws Exception
  {
          boolean blnHasChild=false;
          String strStyle="";
          String styleNode="";
          String sToolTip="";   
          String sDiv="";
          Node n;
          String sCompModifica="-1",sCompElimina="-1",sCompCompetenze="-1",sEspandiComprimi="-1",sCompDocCart="-1";                          
          String idPadre="";
          
                
          for(int i=0;i<nodeList.size();i++) 
          {
             n = (Node)nodeList.elementAt(i);

             sCompElimina="2";
             sCompModifica="2";             

             if (n.getCompComp().equals("1")) 
                sCompCompetenze="1";
             else
                sCompCompetenze="2";
         
             if (n.getCompDocCart().equals("-1") || n.getCompDocCart().equals("0"))
                sCompDocCart="2";
             else
                sCompDocCart="1";
             
             // Nodo di tipo Cartella
             if((n.getTypeNode().equals("C")))
             {
                 //Competenze di Eliminazione
                 if (n.getCompDel().equals("1"))
                    sCompElimina="1";
                 else
                    sCompElimina="2";
         
                 //Competenze di Modifica
                 if (n.getCompMod().equals("1"))
                    sCompModifica="1";
                 else
                    sCompModifica="2";
             }
            
             // Nodo di tipo Cartella Collegata
             if((n.getTypeNode().equals("X")))
             {
                // Occorre disabilitare la funzione di ELIMINA in una Cartella Collegata
                sCompElimina="2";
         
                 //Competenze di Modifica
                 if (n.getCompMod().equals("1"))
                    sCompModifica="1";
                 else
                    sCompModifica="2";
             }
            
             
             // Nodo di tipo Query
             if (n.getTypeNode().equals("Q")) 
             {
                 //Competenze di Eliminazione
                 if (n.getCompDel().equals("1"))
                    sCompElimina="1";
                 else
                    sCompElimina="2";
                 
                 //Competenze di Modifica
                 if (n.getCompMod().equals("1"))
                    sCompModifica="1";
                 else
                    sCompModifica="2";
             }
         
             if (n.getStyle()!=null)
                styleNode=n.getStyle();
                 
             // Espandi e Comprimi (non servono)
             if (n.getChildNodes().size()>0) {
                blnHasChild=true;             
                sEspandiComprimi="'1'";
             }
        	 else
             {
      		   blnHasChild=false;
               sEspandiComprimi="'2'";                
               if (n.getDefaultImage()!= null)
                 n.setImageUrl(mFolder + "/" + n.getDefaultImage());     
             }
           
             String ntext=n.getText();
             String text=Global.replaceAll(Global.replaceAll(Global.replaceAll(Global.replaceAll(Global.replaceAll(ntext,"'","\\'"),"\"","&quot;"),"\\","\\\\"),"\n",""),"\r","");
             
             
             // Setta il IDpadre
             if(n.getTypeNode().equals("X"))
               idPadre=n.getParentIDCollegata();
             else
               idPadre=n.getParentId().substring(1,n.getParentId().length());
            
             
             String sIdCartAppartenenza="&idCartAppartenenza="+idPadre;
         
             // Se il nodo possiede figli
             if (blnHasChild)
             {   
                String sCollegamento="";
                  
                if (n.getIdCollegamento()!=null) {
                   sCollegamento="&idCollegamento="+n.getIdCollegamento();
                }

                String sOnClick;
                if(n.getTypeNode().equals("X"))
                  sOnClick=n.getOnClick();
                else
                  sOnClick="onclick=\\\"if(window.parent.document.getElementById('disableOnclick').value=='1'){"+message+"if (parent.document.getElementById('workArea')) parent.document.getElementById('workArea').src='WorkArea.do?idCartella="+n.getId()+sIdCartAppartenenza+sCollegamento+"'}else return false;\\\"";
         
                n.setSToggle((i+2)+"@");
                
                // Se mi trovo nella costruzione del blocco del TreeViewCopiaSposta
                if(ruolo.equals("DONTCARE"))
                {
                   String stext=Global.replaceAll(Global.replaceAll(Global.replaceAll(ntext,"'","\'"),"\"","&quot;"),"\\","\\\\");
                   stext=Global.replaceAll(Global.replaceAll(stext,"\n","&nbsp;"),"\r","");	
                 	
                   String href="<a style=\\\"text-decoration: none;\\\" href=\\\"javascript:riportaIDCartella('"+n.getId().substring(1,n.getId().length())+"','"+text+"')\\\" >"+stext+"</a>";
                   sDiv="<span UNSELECTABLE=\\\"on\\\" onmouseover=\\\"this.style.cursor='hand';this.style.color='#FF6600';\\\" onmouseout=\\\"this.style.color='black';\\\" "+styleNode+">"+href+"</span>";                 
                }
                else{  
                	
                	String stext=Global.replaceAll(Global.replaceAll(Global.replaceAll(ntext,"'","\'"),"\"","&quot;"),"\\","\\\\");
               	    stext=Global.replaceAll(Global.replaceAll(stext,"\n","&nbsp;"),"\r","");	
                	sDiv="<span UNSELECTABLE=\\\"on\\\" onContextMenu=\\\"MenuTxDx('"+n.getId().substring(1,n.getId().length())+"','"+sCompModifica+"','"+sCompElimina+"','"+sCompCompetenze+"','"+sCompDocCart+"','"+n.getTypeNode()+"','N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"',"+sEspandiComprimi+",'"+text+"','"+idPadre+"','"+n.getIdCollegamento()+"',false,event)\\\" onmouseover=\\\"this.style.cursor='hand';this.style.color='#FF6600';\\\" onmouseout=\\\"this.style.color='black';\\\" "+styleNode+" "+sOnClick +" >" + stext+ "</span>"; 
                }            
                
             }
         	 else // Se il nodo non possiede figli 
             {
				if (n.getTarget()==null)
                  n.setTarget(defaultTarget);

                String sHRef = "",sOnClick="";

                if (n.getHRef()!=null)
                   sHRef=" href=\\\"" + n.getHRef() + "\\\" ";              

                if (n.getOnClick()!=null) 
                   sOnClick=n.getOnClick();
                sToolTip="";

                // Se nodo di tipo Cartella o Cartella Collegata
                if((n.getTypeNode().equals("C")) || (n.getTypeNode().equals("X"))) 
                {
                   sHRef="href=\\\"#\\\" ";
                   n.setTarget("");
                   n.isFolderOnlyDocument=true;
                   
                   String sCollegamento="";
                   
                   if (n.getIdCollegamento()!=null) {
                      sCollegamento="&idCollegamento="+n.getIdCollegamento();
                   }
                   
                   if(n.getTypeNode().equals("X"))
                	 sOnClick=n.getOnClick();//sOnClick="onclick=\\\"getLinkDiretto('"+n.getId()+"','-1');\\\"";
                   else
                     sOnClick="onclick=\\\"if(window.parent.document.getElementById('disableOnclick').value=='1'){"+message+"if (parent.document.getElementById('workArea')) parent.document.getElementById('workArea').src='WorkArea.do?idCartella="+n.getId()+sIdCartAppartenenza+sCollegamento+"'}else return false;\\\"";
                    
                   n.setSToggle((i+2)+"@");
                }
           
                if (n.getToolTipText()!=null)
                    sToolTip=" title=\\\"" + n.getToolTipText() + "\\\" ";                 

                if (!ruolo.equals("DONTCARE"))    
                { 
                	String stext=Global.replaceAll(Global.replaceAll(Global.replaceAll(ntext,"'","\'"),"\"","&quot;"),"\\","\\\\");
                	stext=Global.replaceAll(Global.replaceAll(stext,"\n","&nbsp;"),"\r","");	
                	if(n.getTipoUso().equals("R"))
                	  sDiv="<span UNSELECTABLE=\\\"on\\\" onContextMenu=\\\"MenuTxDx('"+n.getId().substring(1,n.getId().length())+"','"+sCompModifica+"','"+sCompElimina+"','"+sCompCompetenze+"','"+sCompDocCart+"','"+n.getTypeNode()+"','N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"',"+sEspandiComprimi+",'"+text+"','"+idPadre+"','"+n.getIdCollegamento()+"',true,event);\\\" onmouseover=\\\"this.style.cursor='hand';this.style.color='#FF6600';\\\" onmouseout=\\\"this.style.color='black';\\\" "+styleNode+" "+sOnClick+">"+stext+"</span>";             
                	else		
                	  sDiv="<span UNSELECTABLE=\\\"on\\\" onContextMenu=\\\"MenuTxDx('"+n.getId().substring(1,n.getId().length())+"','"+sCompModifica+"','"+sCompElimina+"','"+sCompCompetenze+"','"+sCompDocCart+"','"+n.getTypeNode()+"','N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"',"+sEspandiComprimi+",'"+text+"','"+idPadre+"','"+n.getIdCollegamento()+"',false,event);\\\" onmouseover=\\\"this.style.cursor='hand';this.style.color='#FF6600';\\\" onmouseout=\\\"this.style.color='black';\\\" "+styleNode+" "+sOnClick+">"+stext+"</span>";             
                }
                else
                {
                   String stext=Global.replaceAll(Global.replaceAll(Global.replaceAll(ntext,"'","\'"),"\"","&quot;"),"\\","\\\\");
                   stext=Global.replaceAll(Global.replaceAll(stext,"\n","&nbsp;"),"\r","");	
                   String href="<a style=\\\"text-decoration: none;\\\" href=\\\"javascript:riportaIDCartella('"+n.getId().substring(1,n.getId().length())+"','"+text+"')\\\" >"+stext+"</a>";
                   sDiv="<span UNSELECTABLE=\\\"on\\\" onmouseover=\\\"this.style.cursor='hand';this.style.color='#FF6600';\\\" onmouseout=\\\"this.style.color='black';\\\" "+styleNode+">"+href+"</span>";                 
                }
              }
     
            // Inserimento del blocco del relativo i-nodo 
            vDiv.add(i,sDiv);  
         } // END FOR
       
 }
 
  
  /*
   * METHOD:      formatString
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: 
   * 
   * RETURN:      String
   */ 
  private String formatString(String stext)
  {
          String fs="";
          
		  	 StringTokenizer st = new StringTokenizer(stext,"\n");
	         while (st.hasMoreTokens())
	         {
	            String s=st.nextToken();
	            s=Global.replaceAll(Global.replaceAll(s,"\n",""),"\r",""); 
	            fs+="<div>"+s+"</div>";
	         } 
	      return fs; 
  }	  
  
  
  // ********** COSTRUZIONE DEL TOGGLE ********** //
 
 
 /*
  * METHOD:      getPathFolder
  * SCOPE:       PRIVATE
  *
  * DESCRIPTION: Determina il cammino del sottoalbero dato il nodo
  * 
  * RETURN:      String
  */ 
 private String getPathFolder(IDbOperationSQL dbOp,String id,String idParent,String collegamento) throws Exception
 {
         String sql=null,seq="";
         String f_pathC;
         String idcartella=id.substring(1,id.length());
         
         if (collegamento!=null)
            f_pathC="F_Path_collegamento("+collegamento+",''||to_char(c.id_cartella)||'','I','"+this.sUtente+"')";
         else
            f_pathC="F_Path_Folder(c.id_cartella,'I','"+this.sUtente+"')";
        
         try {
              ResultSet rs=null;
              sql="  select decode(c.id_cartella,-1,'-1',-2,'-2',"+f_pathC+") PATH ";
              sql+=" FROM cartelle c ";
              sql+=" WHERE c.id_cartella = :idcartella";
              
              dbOp.setStatement(sql);
              dbOp.setParameter(":idcartella", idcartella);
       		  dbOp.execute();
       	      rs=dbOp.getRstSet();
              if (rs.next()) {
                 seq=rs.getString(1);
                
              }
         }
         catch (Exception e) {
            throw new Exception("CCS_WorkArea::getPathFolder -- Select fallita\n" + e.getMessage());
         }  
       return seq;   
 }   
 
 
   /*
   * METHOD:      findPath
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Costruzione del toggle
   * 
   * RETURN:      String
  */  
  public String findPath(IDbOperationSQL dbOp,String id,String idParent,String collegamento,String seqToggle) throws Exception
  {
         String sequenza="";
         int offset=0;
         String seq="";
         
         // Restituisce il path di ID
         //System.out.println("ID= "+id+" - Parent= "+idParent+" - Coll="+collegamento);
         String path=getPathFolder(dbOp,id,idParent,collegamento);
         StringTokenizer st = new StringTokenizer(path,"@");
         while (st.hasMoreTokens())
         {
              String s=st.nextToken();
              //Costruzione della sequenza di indici escludendo il nodo radice
              if(s.indexOf("-")==-1)
              {
               int index=getIndexById(s,offset);
               offset=index;
               
               seq=(index+2)+"";
               
               if(seqToggle.indexOf(seq)==-1)
                sequenza+=seq+"@";          
              }
         }
         
         return sequenza;
  }  
 
  
  /*
   * METHOD:      findPath
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Costruzione del toggle
   * 
   * RETURN:      String
  */  
  public String findSequenza(String id) throws Exception
  {
         String sequenza="";
         int index=getIndexById(id,0);
         if(index!=-1)
          sequenza=(index+2)+"@";    
         
         return sequenza;
  } 
  
  /*
   * METHOD:      getIndexById
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Ricerca posizione del nodo tramite id
   * 
   * RETURN:      int
  */  
  private int getIndexById(String id,int offset) 
  {
          Node n;
         
          for(int i=offset;i<vListNodes.size();i++)
          {
             n=(Node)vListNodes.get(i);
             if (n.getId().equals("C"+id))
               return i; 
          }
          return -1;  
  } 
  
  // ********** DEFINIZIONE DEI METODI GET E SET ********** //
 
  public String getIdToggle()
  {
        return idToggle;
  }
 
  public String getOut()
  {
       return sOut.toString();
  }
 
  public void setDefaultTarget(String dt) 
  {
       defaultTarget=dt;
  }
  
  public Vector getNodes() 
  {
         return vNodes;
  }
  
  public String getRuolo() 
  {
         return sRuolo;
  }
  
  public String getUtente() 
  {
         return sUtente;
  }
  
  public String getProvenienza()
  {
         return sProvenienza;
  }

  public String getNodoDaSaltare()
  {
         return nodoDaSaltare;
  }

  public void setNodoDaSaltare(String list)
  {
         nodoDaSaltare=list;
  }
  
  public void setMarginLeft(String margin)
  {
         marginLeft=margin;
  }

  public void setImagePlus(String plus) 
  {
         plusImage=plus;
  }

  public void setImageLeaf(String leaf) 
  {
         leafImage=leaf;
  }

  public void setImageMinus(String minus) 
  {
         minusImage=minus;
  }

  public void setFont(String newFont) 
  {
         font=newFont;
  }

  public void setFontPt(String newFontPt) 
  {
         fontPt=newFontPt;
  }  
  
  public void setWorkSpace(String w)
  {
         wrksp=w;
  }
  
  public void setRuolo(String r)
  {
         sRuolo=r;
  }
  
  public void setUtente(String u)
  {
         sUtente=u;
  }
  
  public void setProvenienza(String p)
  {
         sProvenienza=p;
  }
  
  public void setListaID(String lista)
  {
	     slistaNodi=lista;
  }
  
}
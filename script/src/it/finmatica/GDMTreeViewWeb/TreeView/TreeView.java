package it.finmatica.GDMTreeViewWeb.TreeView;

import java.util.Vector;
import java.sql.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.GDMTreeViewWeb.TreeView.Node;

public class TreeView 
{
  private String mFolder,color;
	private Vector<Node> vNodes;
	private String defaultTarget,id;
  private StringBuffer sOut;
  private String marginLeft;
  private String plusImage,minusImage,leafImage;
  private String font,fontPt;  
  private String sTemp="";
  private String filtro="PARENTID = '0'";
  
  public TreeView()
  {
        this("black","images");        //Navy
  }


  public TreeView(String newColor,String newMFolder)
  {
        vNodes = new Vector<Node>();
  		  color=newColor;
		    mFolder=newMFolder+"/gdm";
        sOut = new StringBuffer("");
        marginLeft="0";
        plusImage="plus.gif";
        minusImage="minus.gif";
        leafImage="dot.gif";
        font="tahoma";
        fontPt="9";
        id="";
  }

  public void addSimpleNode(String text) {
	   	 Node tn = new Node(text);
		   vNodes.addElement(tn);
  }

  public void addNode(String text,String strHref,String strToolTipText) {
	   	 Node tn = new Node(text,strHref,strToolTipText);
		   vNodes.addElement(tn);
  }

  public void display() {
		   //sOut.append("<script>function toggle"+ id +"(id,p,idCart){var myChild = document.getElementById(id);if(myChild.style.display!='block'){myChild.style.display='block';document.getElementById(p).className='folderOpen';}else{myChild.style.display='none';document.getElementById(p).className='folder';} }</script>");
		   //sOut.append("<style>ul.tree{display:none;margin-left:"+marginLeft+"px;}li.folder{list-style-image: url("+ mFolder +"/"+plusImage+");}li.folderOpen{list-style-image: url("+ mFolder +"/"+minusImage+");}li.file{list-style-image: url("+ mFolder +"/"+leafImage+");}a.treeview{color:" + color + ";font-family:"+font+";font-size:"+fontPt+"pt;}a.treeview:link {text-decoration:none;}a.treeview:visited{text-decoration:none;}a.treeview:hover {text-decoration:underline;}</style>");
       sOut.append("<script>function toggle"+ id +"(id,p,idCart){var myChild = document.getElementById(id);if(myChild.style.display!='block'){myChild.style.display='block';document.getElementById(p).className='folderOpen';}else{myChild.style.display='none';document.getElementById(p).className='folder';} }");
		   sOut.append("if (navigator.appName == \"Microsoft Internet Explorer\") {");
		   sOut.append("document.write('<style>');");
       sOut.append("document.write('   ul.tree{display:none;margin-left:"+marginLeft+"px;} ');");
       sOut.append("document.write('   li.folder{list-style-image: url("+ mFolder +"/"+plusImage+");} ');");
       sOut.append("document.write('   li.folderOpen{list-style-image: url("+ mFolder +"/"+minusImage+");} ');");
       sOut.append("document.write('   li.file{list-style-image: url("+ mFolder +"/"+leafImage+");} ');");
       //sOut.append("document.write('   li.fileOpen{list-style-image: url("+ mFolder +"/"+leafImageOpen+");} ');");
       sOut.append("document.write('   a.treeview{color:" + color + ";font-family:"+font+";font-size:"+fontPt+"pt;} ');");
       sOut.append("document.write('   a.treeview:link {text-decoration:none;} ');");
       sOut.append("document.write('   a.treeview:visited{text-decoration:none;} ');");
       sOut.append("document.write('   a.treeview:hover {text-decoration:underline;} ');");       
       sOut.append("document.write('</style> ');");
       sOut.append("} else {");
       sOut.append("document.write('<style>');");
       sOut.append("document.write('   ul.tree{display:none;margin-left:"+marginLeft+"px;cursor:pointer;} ');");
       sOut.append("document.write('   li.folder{list-style-image: url("+ mFolder +"/"+plusImage+");margin-top:8px;margin-left:-40px;cursor:pointer;} ');");
       sOut.append("document.write('   li.folderOpen{list-style-image: url("+ mFolder +"/"+minusImage+");margin-top:8px;margin-left:-40px;cursor:pointer;} ');");
       sOut.append("document.write('   li.file{list-style-image: url("+ mFolder +"/"+leafImage+");margin-top:8px;margin-left:-40px;cursor:pointer;} ');");
       //sOut.append("document.write('   li.fileOpen{list-style-image: url("+ mFolder +"/"+leafImageOpen+");margin-top:8px;cursor:pointer;} ');");
       sOut.append("document.write('   a.treeview{color:" + color + ";font-family:"+font+";font-size:"+fontPt+"pt;cursor:pointer;} ');");
       sOut.append("document.write('   a.treeview:link {text-decoration:none;cursor:pointer;} ');");
       sOut.append("document.write('   a.treeview:visited{text-decoration:none;cursor:pointer;} ');");
       sOut.append("document.write('   a.treeview:hover {text-decoration:underline;cursor:pointer;} ');");       
       sOut.append("document.write('</style> ');");
       sOut.append("}");
       sOut.append("</script>");
		   loopThru(vNodes,"0");
  }

  /*public void displayToggle() {
		   sOut.append("<script>function toggle"+ id +"(id,p){var myChild = document.getElementById(id);if(myChild.style.display!='block'){myChild.style.display='block';document.getElementById(p).className='folderOpen';}else{myChild.style.display='none';document.getElementById(p).className='folder';}");
       sOut.append("document.idTree.value=id;document.pTree.value=p} ");
       sOut.append("function popup(nomefile,larghezza, altezza, x, y) {");
       sOut.append("win_popup = window.open(nomefile,\"popup\",\"toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 0,resizable= 1,copyhistory= 0,width=\" + larghezza + \",height=\" + altezza);");
       sOut.append(" if(x && y)");
       sOut.append("{ x = parseInt(x);");
       sOut.append(" y = parseInt(y);");
       sOut.append(" win_popup.moveTo(x, y);"); 
       sOut.append("} }</script>");
		   sOut.append("<style>ul.tree{display:none;margin-left:"+marginLeft+"px;}li.folder{list-style-image: url("+ mFolder +"/"+plusImage+");}li.folderOpen{list-style-image: url("+ mFolder +"/"+minusImage+");}li.file{list-style-image: url("+ mFolder +"/"+leafImage+");}a.treeview{color:" + color + ";font-family:"+font+";font-size:"+fontPt+"pt;}a.treeview:link {text-decoration:none;}a.treeview:visited{text-decoration:none;}a.treeview:hover {text-decoration:underline;}</style>");
		   loopThru(vNodes,"0");
  }*/

  public void setDefaultTarget(String dt) 
  {
       defaultTarget=dt;
  }

  public String getOut()
  {
       return sOut.toString();
  }

  public Vector getNodes() 
  {
         return vNodes;
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

  public void loadFromDb(IDbOperationSQL dbOp,String select, String area) throws Exception
  {
//         String parentID;
//         Node parentNode;
        
         try {
           dbOp.setStatement("SELECT * FROM "+ select+ " WHERE "+filtro+" AND NODEID LIKE '"+area+"-%'");
           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           while (rst.next()) 
           {
//              parentID=rst.getString("ParentID");

              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setId(rst.getString("NodeID"));

              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setOnClick(null);
              if (rst.getString("url")!=null && rst.getString("url").substring(0,10).equals("javascript")) {                
                 node.setTarget("");
                 node.setHRef(null);
                 node.setOnClick("href=\"#\" onclick=\""+ rst.getString("url") + "\" ");
              }

     				 vNodes.addElement(node);

             caricaFigli(dbOp, select, area, rst.getString("NodeID"));
           }
           dbOp.close();
         }
         catch(Exception e) 
         {
           try { 
            dbOp.close();
           } catch (Exception e2) {}
           throw new Exception("TreeView::loadFromDb()\n"+e.getMessage());
         }         
  }

  public void loadFromDbC(Connection dbConn,String select, String area) throws Exception
  {
//         String parentID;
//         Node parentNode;
         DbOperationSQL dbOp = null;
        
         try {
           dbOp = new DbOperationSQL(dbConn);
           dbOp.setStatement("SELECT * FROM "+ select+ " WHERE PARENTID = '0' AND NODEID LIKE '"+area+"-%'");
           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           while (rst.next()) {
//              parentID=rst.getString("ParentID");

              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setId(rst.getString("NodeID"));

              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setOnClick(null);
              if (rst.getString("url")!=null && rst.getString("url").substring(0,10).equals("javascript")) {                
                 node.setTarget("");
                 node.setHRef(null);
                 node.setOnClick("href=\"#\" onclick=\""+ rst.getString("url") + "\" ");
              }

     				 vNodes.addElement(node);

             caricaFigli(dbConn, select, area, rst.getString("NodeID"));
           }
           dbOp.autoCommitOn();
           dbOp.close();
         }
         catch(Exception e) 
         {
           try { 
            dbOp.autoCommitOn();
            dbOp.close();
           } catch (Exception e2) {}
           throw new Exception("TreeView::loadFromDb()\n"+e.getMessage());
         }         
  }

  private Node findNode(Vector nodes,String id) 
  {
          Node temp=null;
  
          for(int i=0;i<nodes.size();i++) 
          {
              temp=(Node)nodes.elementAt(i);
              if (temp.getId().equals(id))
                  return temp;
              else {
                 if (temp.getChildNodes().size()>0) {
					           temp = findNode(temp.getChildNodes(),id);
      				    	 if (temp!=null)
      						       return temp;
                 }
              }
          }
          return null;
  }

  private void loopThru(Vector nodeList,String parent)	
  {
         boolean blnHasChild=false;
         String strStyle="";
         String styleNode="";
         String sToolTip="";         
         String /*sHRef = "",*/sOnClick="";
 
         if (!parent.equals("0"))
      			sOut.append("<ul class=tree id=\"N" + parent + "\">");
      	 else
      			sOut.append("<ul style='margin-left:30px;' id=\"N" + parent + "\">");

         Node n;

         for(int i=0;i<nodeList.size();i++) 
         {
            n = (Node)nodeList.elementAt(i);
            sOnClick = "";
//            sHRef = "";

             if (n.getStyle()!=null)
                styleNode=n.getStyle();
                 
             if (n.getChildNodes().size()>0)
                blnHasChild=true;
        		 else
      				  blnHasChild=false;

             if (n.getImageUrl()==null)
                strStyle="";
             else
        				strStyle="style='list-style-image: url("+ n.getImageUrl() +");'";

             if (blnHasChild) {               
                n.setSToggle("toggle"+ id +"('N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"');");
//                sOut.append("<li "+ strStyle +" class=folder id=\"P" + parent + i + "\"><a class=treeview "+ styleNode +" href=\"javascript:toggle"+ id +"('N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"')\">" + n.getText() + "</a>");
                if (n.getOnClick()!=null)  {
                   sOnClick=n.getOnClick();
                   int k = sOnClick.indexOf("onclick");
                   int j = sOnClick.length() - 2;
                   String snewClick = sOnClick.substring(k,j);
                   sOnClick = snewClick+"\"";
                } 
                   
//                sOut.append("<li "+ strStyle +" class=folder id=\"P" + parent + i + "\"><a class=treeview "+ styleNode +" href=\"#\" ondblclick=\"toggle"+ id +"('N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"');\" "+sOnClick+">" + n.getText() + "</a>");
                sOut.append("<li "+ strStyle +" class=folder id=\"P" + parent + i + "\"><a class=treeview "+ styleNode +"><div unselectable='on' onmouseout=\"this.style.textDecoration='none'\" onmouseover=\"this.style.cursor='hand'; this.style.textDecoration='underline';\" ondblclick=\"toggle"+ id +"('N" + parent + "_" + i + "','P" + parent + i + "','"+n.getId()+"');\" "+sOnClick+"> " + n.getText() + "</div></a>");
             }
         		 else {
				        if (n.getTarget()==null)
                   n.setTarget(defaultTarget);

//                if (n.getHRef()!=null) 
//                   sHRef = " href=\"" + n.getHRef() + "\" ";

                if (n.getOnClick()!=null)  {
                   sOnClick=n.getOnClick();
                   int k = sOnClick.indexOf("onclick");
                   int j = sOnClick.length() - 2;
                   String snewClick = sOnClick.substring(k,j);
                   sOnClick = snewClick+"\"";
                } 

                sToolTip = "";

                if (n.getToolTipText()!=null)
                    sToolTip=" title=\"" + n.getToolTipText() + "\" ";

//      		   	  sOut.append("<li "+ strStyle +" class=file><a class=treeview "+ styleNode + sHRef + sOnClick + " target=\"" + n.getTarget() + "\"" + sToolTip + ">" + n.getText() + "</a>");             }
                if ((sOnClick.equalsIgnoreCase("")) || (sOnClick.indexOf("'P'") > -1)) {
                  sOut.append("<li "+ strStyle +" class=folder><a class=treeview "+ styleNode + "><div unselectable='on' onmouseout=\"this.style.textDecoration='none'\" onmouseover=\"this.style.cursor='hand'; this.style.textDecoration='underline';\" " + sOnClick + " target=\"" + n.getTarget() + "\"" + sToolTip + ">" + n.getText() + "</div></a>");
                } else {
                  sOut.append("<li "+ strStyle +" class=file><a class=treeview "+ styleNode + "><div unselectable='on' onmouseout=\"this.style.textDecoration='none'\" onmouseover=\"this.style.cursor='hand'; this.style.textDecoration='underline';\" " + sOnClick + " target=\"" + n.getTarget() + "\"" + sToolTip + ">" + n.getText() + "</div></a>");
                }
            }

            if (blnHasChild)
          		 loopThru(n.getChildNodes(),parent + "_" + i);
      
						sOut.append("</li>");
         } // END FOR

         sOut.append("</ul>");
  }


  public String findNodeToggle(Vector nodes,String id) 
  {
          Node temp=null;
          for(int i=0;i<nodes.size();i++) 
          {
              temp=(Node)nodes.elementAt(i);
              if (temp.getSToggle() != null)
                  sTemp = sTemp + temp.getSToggle();
              if (temp.getId().equals(id)) {
                  return sTemp;
              }
              else {
                 if (temp.getChildNodes().size()>0) {
					           String t = findNodeToggle(temp.getChildNodes(),id);  
                     if ((t==null)||t.equals(""))
                         sTemp = "";
                     else
                          return sTemp;  
                 }
              }
          }
        return "";
  }

  private void caricaFigli(IDbOperationSQL dbOpSql, String nome_vista, String area, String idNode) throws Exception
  {
         IDbOperationSQL dbOp = null;
         String parentID;
         Node parentNode;
         String select = "select * from "+nome_vista+" where parentid = '"+idNode+"' AND NODEID LIKE '"+area+"-%'";
        
         try {
//           dbOp = new DbOperationSQL(dbOpSql);
           dbOp = SessioneDb.getInstance().createIDbOperationSQL(dbOpSql);
           dbOp.setStatement(select);
           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           while (rst.next()) 
           {
              parentID=rst.getString("ParentID");
              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setId(rst.getString("NodeID"));

              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setOnClick(null);
              if (rst.getString("url")!=null && rst.getString("url").substring(0,10).equals("javascript")) {                
                 node.setTarget("");
                 node.setHRef(null);
                 node.setOnClick("href=\"#\" onclick=\""+ rst.getString("url") + "\" ");
              }
              

              if (parentID.equals("0"))                 
         				 vNodes.addElement(node);
      				else {
    					  parentNode = findNode(vNodes,parentID);
					      if (parentNode!=null) 
						       parentNode.add(node);
              }

             caricaFigli(dbOp, nome_vista, area, rst.getString("NodeID"));
           }
           dbOp.close();
         }
         catch(Exception e) 
         {
           try { 
            dbOp.close();
           } catch (Exception e2) {}
           throw new Exception("TreeView::caricaFigli()\n"+e.getMessage());
         }         
  }

  private void caricaFigli(Connection dbConn, String nome_vista, String area, String idNode) throws Exception
  {
         DbOperationSQL dbOp = null;
         String parentID;
         Node parentNode;
         String select = "select * from "+nome_vista+" where parentid = '"+idNode+"' AND NODEID LIKE '"+area+"-%'";
        
         try {
           dbOp = new DbOperationSQL(dbConn);
           dbOp.setStatement(select);
           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           while (rst.next()) 
           {
              parentID=rst.getString("ParentID");

              Node node = new Node(rst.getString("text"),rst.getString("url"),rst.getString("toolTip"));
              node.setId(rst.getString("NodeID"));

              if (rst.getString("CUSTOM_IMAGE")!=null)
                  node.setImageUrl(mFolder + "/" + rst.getString("CUSTOM_IMAGE"));
              if (rst.getString("CUSTOM_STYLE")!=null)
                  node.setStyle(rst.getString("CUSTOM_STYLE"));

              node.setOnClick(null);
              if (rst.getString("url")!=null && rst.getString("url").substring(0,10).equals("javascript")) {                
                 node.setTarget("");
                 node.setHRef(null);
                 node.setOnClick("href=\"#\" onclick=\""+ rst.getString("url") + "\" ");
              }

              if (parentID.equals("0"))                 
         				 vNodes.addElement(node);
      				else {
    					  parentNode = findNode(vNodes,parentID);
					      if (parentNode!=null) 
						       parentNode.add(node);
              }

             caricaFigli(dbConn, nome_vista, area, rst.getString("NodeID"));
           }
           dbOp.close();
         }
         catch(Exception e) 
         {
           try { 
            dbOp.close();
           } catch (Exception e2) {}
           throw new Exception("TreeView::caricaFigli()\n"+e.getMessage());
         }         
  }

  /**
   * 
   */
  public void settaFiltro (String pFiltro) {
    settaFiltro(pFiltro,false);
  }

  /**
   * 
   */
  public void settaFiltro (String pFiltro, boolean pCaseSensitive) {
    String newFiltro = "";
    if (pCaseSensitive) {
      newFiltro = pFiltro;
      if (newFiltro == null) {
        newFiltro = "";
      }
      if (newFiltro.equalsIgnoreCase("")) {
        filtro = "PARENTID = '0'";
      } else {
        filtro = "";
        int j = 0;
        int i = newFiltro.indexOf(" ");
        while (i > -1) {
          filtro += "URL LIKE '%"+newFiltro.substring(j,i)+"%' AND ";
          j = i + 1;
          i = newFiltro.indexOf(" ",j);
        }
        filtro += "URL LIKE '%"+newFiltro.substring(j)+"%' ";
      }
    } else {
      newFiltro = pFiltro.toUpperCase();
      if (newFiltro == null) {
        newFiltro = "";
      }
      if (newFiltro.equalsIgnoreCase("")) {
        filtro = "PARENTID = '0'";
      } else {
        filtro = "";
        int j = 0;
        int i = newFiltro.indexOf(" ");
        while (i > -1) {
          filtro += "UPPER(URL) LIKE '%"+newFiltro.substring(j,i)+"%' AND ";
          j = i + 1;
          i = newFiltro.indexOf(" ",j);
        }
        filtro += "UPPER(URL) LIKE '%"+newFiltro.substring(j)+"%' ";
      }
    }
  }

}
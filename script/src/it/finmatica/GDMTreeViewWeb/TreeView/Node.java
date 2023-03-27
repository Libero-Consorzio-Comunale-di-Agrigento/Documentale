package it.finmatica.GDMTreeViewWeb.TreeView;

import java.util.Vector;

public class Node 
{
  private String text,hRef,target,toolTipText,onclick;
  private Vector<Node> childNodes;
	private String imageUrl,id;
  private String style;
  private String sToggle;

  public Node(String strText)
  {
         this(strText,null,null);
  }

  public Node(String strText,String strHref,String strToolTipText)
  {
         text=strText;
		     hRef=strHref;
    		 toolTipText=strToolTipText;
         id="";
         childNodes = new Vector<Node>();         
  }

  public void add(Node objNode) {
       	 childNodes.addElement(objNode);
  }	

  public void setImageUrl(String newImageUrl) 
  {
         imageUrl=newImageUrl;
  }

  public void setHRef(String newHRef) 
  {
         hRef=newHRef;
  }

  public void setOnClick(String newOnClick) 
  {
         onclick=newOnClick;
  }

  public String getOnClick() 
  {
         return onclick;
  }

  public String getImageUrl() 
  {
         return imageUrl;
  }

  public void setId(String newId) 
  {
         id=newId;
  }

  public String getId() 
  {
         return id;
  }

  public Vector getChildNodes() 
  {
         return childNodes;
  }

  public String getText() 
  {
         return text;
  }

  public void setTarget(String newTarget) 
  {
         target=newTarget;
  }

  public String getTarget() 
  {
         return target;
  }

  public String getHRef() 
  {
         return hRef;
  }

  public String getToolTipText() 
  {
         return toolTipText;
  }

  public void setStyle(String newStyle) 
  {
         style=newStyle;
  }

  public String getStyle() 
  {
         return style;
  }  

  public String getSToggle() 
  {
         return sToggle;
  }
  
  public void setSToggle(String newSToggle) 
  {
         sToggle=newSToggle;
  }
}
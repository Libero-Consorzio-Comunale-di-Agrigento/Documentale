package it.finmatica.dmServer.jdmsfx;

/*
 * GESTIONE NODE
 * 
 * AUTHOR @SCANDURRA
 * DATE   06/06/2008
 * 
 * */
 
import java.util.Vector;

public class NodeTree 
{
  private String text,hRef,target,toolTipText,onclick;
  private Vector childNodes;
  private String imageUrl, defaultImage, id;
  private String style;
  private String sToggle;
  private String typeNode;
  private String compMod,compDel,compDocCart,compComp;
  public boolean isFolderOnlyDocument;
  private String idCollegamento;
  private String tipoUso;
  private String idCartellaCollegata;
  private String parentIDColl;
  private String idOggettoComp;
  private String utenteDesk;
  private String hasChildren;
  private String tooltip;
  private String tipo_link;
  private String nome_icona;
  
  private String textQuickSort;
  
  // Variabili per la gestione del nuovo TeeeView
  private int livello;// Indica livello del nodo (0=ROOT ,1,2,...)
  private boolean hidden;// indica se il nodo è nascosto o meno
  private String parentID;
  
  // Varibiale contenente l'icona del tipo documento come associato al modello.
  private String icona;

  public NodeTree()
  {
	  childNodes = new Vector(); 
  }
  
  public NodeTree(String strText)
  {
         this(strText,null,null);
  }

  public NodeTree(String strText,String strHref,String strToolTipText)
  {
         text=strText;
         hRef=strHref;
    	 toolTipText=strToolTipText;
         id="";
         childNodes = new Vector();   
         tipoUso="";
         isFolderOnlyDocument = false;
  }

  public void add(NodeTree objNode) {
       	 childNodes.addElement(objNode);
  }	

  public void setImageUrl(String newImageUrl) 
  {
         imageUrl=newImageUrl;
  }

  public void setDefaultImage(String newDefaultImage) 
  {
         defaultImage=newDefaultImage;
  }

  public String getDefaultImage() 
  {
         return defaultImage;
  }

  public void setCompMod(String comp) 
  {
         compMod=comp;
  }

  public String getCompComp() 
  {
         return compComp;
  }
  
  public void setHasChildren(String child) 
  {
	     hasChildren=child;
  }

  public String getHasChildren() 
  {
         return hasChildren;
  }
  
  
  public void setIdCollegamento(String id) 
  {
         idCollegamento=id;
  }

  public String getIdCollegamento() 
  {
         return idCollegamento;
  }
  
  public void setTipoUso(String t) 
  {
	  tipoUso=t;
  }

  public String getTipoUso() 
  {
         return tipoUso;
  }
  
  public void setIdCartellaCollegata(String id) 
  {
         idCartellaCollegata=id;
  }

  public String getIdCartellaCollegata() 
  {
         return idCartellaCollegata;
  }
  
    
  public void setParentIDCollegata(String id) 
  {
         parentIDColl=id;
  }

  public String getParentIDCollegata() 
  {
         return parentIDColl;
  }
  
  public void setCompComp(String comp) 
  {
         compComp=comp;
  }

  public String getCompMod() 
  {
         return compMod;
  }

  public void setCompDocCart(String comp) 
  {
         compDocCart=comp;
  }

  public String getCompDocCart() 
  {
         return compDocCart;
  }

  public void setCompDel(String comp) 
  {
         compDel=comp;
  }

  public String getCompDel() 
  {
         return compDel;
  }
  
  public void setIdOggettoComp(String id) 
  {
	     idOggettoComp=id;
  }
  
  public void setUtenteDesk(String ute) 
  {
	     utenteDesk=ute;
  }
  
  public String getUtenteDesk() 
  {
	     return utenteDesk;
  }

  public String getIdOggettoComp() 
  {
         return idOggettoComp;
  }

  public void setTypeNode(String newTypeNode) 
  {
         typeNode=newTypeNode;
  }

  public String getTypeNode() 
  {
         return typeNode;
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
  
  public String getTextQuickSort() 
  {
         return textQuickSort;
  }
  
  public void setTextQuickSort(String newtextQ) 
  {
         textQuickSort=newtextQ;
  }

  public void setText(String newtext) 
  {
         text=newtext;
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

  public void setLivello(int liv) 
  {
         livello=liv;
  }

  public int getLivello() 
  {
         return livello;
  }
 
  public void setParentId(String pid) 
  {
         parentID=pid;
  }

  public String getParentId() 
  {
         return parentID;
  }

  public void setHidden(boolean h) 
  {
         hidden=h;
  }

  public boolean getHidden() 
  {
         return hidden;
  }
  
  public String getIcona() 
  {
         return icona;
  }

  public void setIcona(String newicona) 
  {
         icona=newicona;
  }
  
  public String getTipoLink() 
  {
         return tipo_link;
  }

  public void setTipoLink(String newtl) 
  {
         tipo_link=newtl;
  }
  
  public String getTooltip() 
  {
         return tooltip;
  }

  public void setTooltip(String newtooltip) 
  {
         tooltip=newtooltip;
  }
  
  public String getNomeIcona() 
  {
         return nome_icona;
  }

  public void setNomeIcona(String newnome) 
  {
	     nome_icona=newnome;
  }

}
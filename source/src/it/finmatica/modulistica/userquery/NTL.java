package it.finmatica.modulistica.userquery;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class NTL {
		NodoTabella info;    
		HashSet figli; 
		boolean bRoot=false; 
	
	 	public NTL(NodoTabella n) { 
	 		this.info=n;
	 		this.figli=new HashSet();
	 	}
	 	
	 	public void preorder(NTL a) {
	 		    if(a==null)
	 		      return;

	 		    System.out.print("---- TABELLA ----\n"+a.info+"----(ROOT="+a.bRoot+")-----------\n\n");

	 		    Iterator i=a.figli.iterator();
	 		    while(i.hasNext())
	 		    	 preorder((NTL) i.next());
	 	}
	 	
	 	public void postorder(NTL a) {
	 		    if(a==null)
	 		      return; 		    
	
	 		    Iterator i=a.figli.iterator();
	 		    while(i.hasNext())
	 		    	postorder((NTL) i.next());
	 		    
	 		   System.out.print("\n-----\n"+a.info+"\n--(ROOT="+a.bRoot+")----\n");
	 	}	  
	 	
	 	public NTL search(NTL a,String tabellaSearch, String aliasSearch) {	 			
	 			if (a!=null) {
	 				if (a.info.getAliasTabella().equals(aliasSearch) ||
		 				a.info.getNomeTabella().equals(tabellaSearch))
		 				return a;
	 				else {
	 					Iterator i=a.figli.iterator();
	 					while(i.hasNext()) {
	 						NTL nSottoAlbero = ((NTL) i.next());
	 						if (search(nSottoAlbero,tabellaSearch,aliasSearch)!=null) return nSottoAlbero;	 						
	 					}
	 					
	 				}	 				
	 			}
	 			return null;
	 	}

		public boolean isBRoot() {
			   return bRoot;
		}

		public void setBRoot(boolean root) {
			   bRoot = root;
		}	 	
}

class NodoCondizione {
	  private String condizione;	  
	  private int type;    //0=CONDIZIONE PARAMETRICA, 1=CONDIZIONE DI LEGAME, 2=CONDIZIONE FISSA
	  private boolean bMarkToDelete=false;
	  
	  public NodoCondizione(String c) {
		  	 condizione=c;		     
	  }

	  public String getCondizione() {
		     return condizione;
	  }

	  public void setCondizione(String condizione) {
		     this.condizione = condizione;
	  }
	  
	  public int getType() {
		     return type;
	  }

	  public void setType(int t) {
		     type=t;
	  }	  
	  
	  
	  public boolean isBMark() {
		     return bMarkToDelete;
	  }

	  public void setBMark(boolean mark) {
		     bMarkToDelete = mark;
	  }	 	  

}

class NodoTabella {
	  private String nomeTabella, aliasTabella;
	  private boolean bMark=false, bNodeParam=false, bTuttiFigliCancellabiliOrNonParametrici=true;
	  public Vector<NodoCondizione> vCondizioni = new Vector<NodoCondizione>();
	  
	  public NodoTabella(String n, String a) {
		     nomeTabella=n;
		     aliasTabella=a;
	  }

	  public String getAliasTabella() {
			 return aliasTabella;
	  }
	
	  public void setAliasTabella(String aliasTabella) {
			 this.aliasTabella = aliasTabella;
	  }
	
	  public String getNomeTabella() {
			 return nomeTabella;
	  }
	
	  public void setNomeTabella(String nomeTabella) {
			 this.nomeTabella = nomeTabella;
	  }
	  
	  public void addCondizione(NodoCondizione condizione) {
		     vCondizioni.add(condizione);
	  }
	  
	  public boolean isBMark() {
		     return bMark;
	  }

	  public void setBMark(boolean mark) {
		     bMark = mark;
	  }	  

	  public boolean isNodeParam() {
		     return bNodeParam;
	  }

	  public void setIsNodePar(boolean mark) {
		  	 bNodeParam = mark;
	  }	  	  
  
	  public boolean isBTuttiFigliCancellabiliOrNonParametrici() {
		  	 return bTuttiFigliCancellabiliOrNonParametrici;
	  }

	  public void setBTuttiFigliCancellabiliOrNonParametrici(boolean tuttiFigliCancellabiliOrParametrici) {
		  	 bTuttiFigliCancellabiliOrNonParametrici = tuttiFigliCancellabiliOrParametrici;
	  }
	  	  
	  public String toString() {
		  	 String sRet = nomeTabella+" as "+aliasTabella+" -TO DELETE Mark "+bMark+" -ISNODEPAR "+bNodeParam+" -TUTTI I FIGLI CANCELLABILI O NON PARAMETRICI "+bTuttiFigliCancellabiliOrNonParametrici;
		  	 
		  	 sRet+="\nCondizioni: ";
		  	 for(int i=0;i<vCondizioni.size();i++) 
		  		sRet+="\n\t- "+vCondizioni.get(i).getCondizione()+" |TO DELETE "+vCondizioni.get(i).isBMark()+" | "+vCondizioni.get(i).getType();
		  	 
		  	 sRet+="\n";
		     return sRet;
	  }



	
}


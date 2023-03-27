package it.finmatica.dmServer.management.macroAction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

/**
 * Classe che serve a creare automaticamente 
 * cartelle in base al valore del campo $CART_AUTO
 * contenuto in un documento.
 * 
 * Se il documento contiene questo campo ed è pieno
 * la sintassi sarà la seguente:
 * 
 * Y#WorkspaceA@Area@Cm\CartellaA@Area@Cm\....;WorkspaceB@Area@Cm\CartellaB@Area@Cm\....
 * 
 * La Stringa deve iniziare con i caratteri:
 * 
 * Y#
 * N#
 * 
 * dove # è il separatore e Y/N indica o meno il fatto che i link al documento
 * (AUTOMATICI) che non sono più presenti nei path a seguire dovranno essere
 * cancellati
 * 
 * Ogni stringa separata dal carattere ';' rappresenta un percorso.
 * Il separatore di ogni percorso è il carattere '\'.
 * Ogni elemento del percorso contiene le seguenti informazioni:
 * 		Nome della cartella @ Area del modello Cartella @ Cod. Modello della Cartella
 * 
 * Chiamando il metodo make verranno create le cartelle relative ai
 * percorsi di cui sopra (se queste non esistono ancora) ed il documento
 * passato nel costruttore verrà inserito in ognuno di questi path (come foglia)
 * 
 * @author Ringhio
 *
 */
public class AutomaticFolder {
		
	   private final String _LISTPATH_SEPARATOR       =  ";";
	   private final String _PATH_SEPARATOR           =  "\\";
	   private final String _ITEMPATH_SEPARATOR       =  "@";
	
	   private final String _CART_AUTO_FIELD  =  "CART_AUTO";
	   
	   private final int    _WRKSPCHECK_MODEL_NOT_EXIST  =  0;
	   private final int    _WRKSPCHECK_NOT_WRKSP        =  1;
	   private final int    _WRKSPCHECK_WRKSP_NOT_EXIST  =  2;
	   
	   private String  idDoc;	   
	   private String  folderAutoString=null;
	   
	   Environment en;
	   private IDbOperationSQL dbOpSql;	   
	   private boolean bIsNew=false;	   	   
	   
	   /**
	    * 
		* @param idDocument        Documento da inserire nelle cartelle
		* @param folderAutoString  Stringa contenuta nel campo $CART_AUTO
		* @param newEn
	   */
	   public AutomaticFolder(String idDocument, String folderAutoString, Environment newEn) {
		   	  idDoc=idDocument;
		   	  this.folderAutoString=folderAutoString;
		   	  
		   	  this.en=newEn;
	   }
	   
	   /**
	    * Costruttore da utilizzare se si possiede un profilo
	    * con la initVarEnv già chiamata
	    * 
	    * Il metodo provvederà a recuperare l'informazione del
	    * campo $CART_AUTO
	    * 
		* @param p		         Profilo che contiene l'informazione
		* @param bInitProfile    Se True verrà lanciata l'accedi e l'initVarEnv del profilo
		* @param newEn
		* @throws Exception
	   */
	   public AutomaticFolder(Profilo p, boolean bInitProfile, Environment newEn) throws Exception {		   	  
		   	  if (bInitProfile) {
		   		  p.initVarEnv(newEn);
		   		  
		   		  if (!p.accedi(Global.ACCESS_NO_ATTACH).booleanValue())
		   		     throw new Exception("AutomaticFolder - Errore in accesso al profilo.\nErrore: "+p.getError());
		   	  }
		   	  
		   	  idDoc=p.getDocNumber();
		   	  folderAutoString=Global.nvl(p.getCampo(_CART_AUTO_FIELD),"");
		   	  
		   	  this.en=newEn;		   	  
	   }	   
	   
	   public void make() throws Exception {
		      if (folderAutoString.trim().length()==0) return;
		      
		      if (folderAutoString.charAt(1)!='#') 
		    	  throw new Exception("Attenzione! Errore in cartelle automatiche: la stringa deve iniziare per Y# oppure N#:\n"+
		    			              "Path che ha generato l'errore: "+folderAutoString);			      		      
		      		      
		      try {
		    	  dbOpSql=connect();
		    	  
		    	  String sCheckDelete=folderAutoString.substring(0,1);
		    	  
		    	  folderAutoString=folderAutoString.substring(2);
		    	  Vector<String> idFolderRicavati = new Vector<String>();
			      StringTokenizer sElencoPath = new StringTokenizer(folderAutoString,_LISTPATH_SEPARATOR);			  

			      //Spezzo i path tramite il carattere ';'
				  while (sElencoPath.hasMoreTokens()) {
					    String sPath = sElencoPath.nextToken();					    
					    boolean bIsNewFolder=false;
					    					    
					    int i=0;
					    //Spezzo il path tramite il carattere '\'
					    StringTokenizer sElencoItemPath = new StringTokenizer(sPath,_PATH_SEPARATOR);
					    String idFolderActual=null;
					    Folder f = null;
					    String sNomeWrkSp=null;
					    String sPathTotale=null;
					    Vector<Properties> idFolderChildList = new Vector<Properties>();
					    
					    while (sElencoItemPath.hasMoreTokens()) {
					    	  String sItemPath = sElencoItemPath.nextToken();
					    	  
					    	  idFolderActual="";
					    	  
					    	  f = new Folder("","","");
					    	  try {
					    		  parseStringFolder(sItemPath,f);
					    	  }
							  catch (Exception e) {							  
							      throw new Exception("Errore nel parse dell'Item del Path:\n"+e.getMessage());							      
							  }						
				    		  
					    	  i++;
					    	  //Sono sul primo item del path....è una workspace
					    	  if (i==1)  {
					    		 int icheck=checkWorkspace(f);
					    		 
					    		 sNomeWrkSp=f.nameFolder;
					    		 sPathTotale="";
					    		 
						    	 switch (icheck) {
						    	   	case _WRKSPCHECK_MODEL_NOT_EXIST:
						    	   		 throw new Exception("Errore in cartelle automatiche.\n"+
						    	   				 			 "area e codice modello ("+f.area+","+f.cm+") inesistenti nel path.\n"+
						    	   				 			 "Path completo: "+folderAutoString);						    	   		 
						    	   	case _WRKSPCHECK_NOT_WRKSP:  
						    	   		 throw new Exception("Errore in cartelle automatiche.\n"+
						    	   							 "area e codice modello ("+f.area+","+f.cm+") inseriti nel path non sono una workspace.\n"+
						    	   							 "Path completo: "+folderAutoString);
						    	   	case _WRKSPCHECK_WRKSP_NOT_EXIST:
						    	   		 bIsNewFolder=true;
						    	   		 
						    	   		 try {
						    	   		   ICartella ic = new ICartella(f.area,f.cm,"","",f.nameFolder);
						    	   		   ic.initVarEnv(en);
						    	   		   ic.escludiControlloCompetenze(true);
						    	   		   ic.insert();
						    	   		   
						    	   		   idFolderActual=ic.getIdentifierFolder();
						    	   		 }
						   		         catch (Exception e) {
						   		           throw new Exception("Errore nell'inserimento della workspace "+f.nameFolder+" con area="+f.area+" e cm="+f.cm+":\n"+e.getMessage());	
						   		         }
						    	   		 break;
						    	   	default:
						    	   		idFolderActual=""+icheck;
						    	   		try {	
						    	   		  idFolderChildList.clear();		    	   		
						    	   		  idFolderChildList=getChildFolder(idFolderActual);
						    	   		}
						   		        catch (Exception e) {
						   		          throw new Exception("Errore nel recupero dei figli della cartella (workspace): "+f.nameFolder+" con area="+f.area+" e cm="+f.cm+":\n"+e.getMessage());	
						   		        }						    	   		  
						    	 }
					    	  }
					    	  //Cartelle Interne del Path (NON WORKSPACE)
					    	  else {
					    		 ICartella ic = null;
					    		 if (bIsNewFolder) {
					    			 try {
						    			 ic = new ICartella(f.area,f.cm,sNomeWrkSp,sPathTotale,f.nameFolder);
						    			 
						    			 ic.initVarEnv(en);
						    	   		 ic.escludiControlloCompetenze(true);
						    	   		 ic.insert();			
						    	   		 idFolderActual=ic.getIdentifierFolder();
					    			 }
					   		         catch (Exception e) {
					   		           throw new Exception("Errore nell'inserimento della cartella "+f.nameFolder+" con area="+f.area+" e cm="+f.cm+":\n"+e.getMessage());	
					   		         }						    	   		 
					    		 }
					    		 else {
					    			 String idFolderNew=isInVector(idFolderChildList,f.nameFolder);
					    			 
					    			 //Non esiste....ne creo una nuova
					    			 if (idFolderNew==null) {
					    				 try {
						    				 ic = new ICartella(f.area,f.cm,sNomeWrkSp,sPathTotale,f.nameFolder);
							    			 
							    			 ic.initVarEnv(en);
							    	   		 ic.escludiControlloCompetenze(true);
							    	   		 ic.insert();	
							    	   		 idFolderActual=ic.getIdentifierFolder();
					    				 }
						   		         catch (Exception e) {
						   		           throw new Exception("Errore nell'inserimento della cartella "+f.nameFolder+" con area="+f.area+" e cm="+f.cm+":\n"+e.getMessage());	
						   		         }									    	   		 
					    			 }
					    			 //Esiste
					    			 else {
					    				 idFolderActual=""+idFolderNew;
						    	   		 try {				
						    	   		   idFolderChildList.clear();
						    	   		   idFolderChildList=getChildFolder(idFolderActual);
						    	   		 }
						   		         catch (Exception e) {
						   		           throw new Exception("Errore nel recupero dei figli della cartella (workspace): "+f.nameFolder+" con area="+f.area+" e cm="+f.cm+":\n"+e.getMessage());	
						   		         }	 
					    			 }
					    		 }
					    		 
					    		 if (sPathTotale.equals(""))
					    		 	 sPathTotale+=f.nameFolder;
					    		 else 
					    			 sPathTotale+="\\"+f.nameFolder;
					    	  }
					    } //Fine del Ciclo per un Path
					    
					    //Inserisco il documento nel path
					    if (idFolderActual!=null && f!=null) {
						    try {
						    	idFolderRicavati.add(idFolderActual);
						    	
							    ICartella icInsertedObject = new ICartella(idFolderActual);
							    icInsertedObject.initVarEnv(en);	
							    icInsertedObject.escludiControlloCompetenze(true);
							    icInsertedObject.addInObject(idDoc,"D",true);		
							    
							    icInsertedObject.update();
			    	   		 }
			   		         catch (Exception e) {
			   		           throw new Exception("Errore nell'inserimento del documento "+idDoc+" nella cartella con id "+idFolderActual+" e con (nome,area,cm) = ("+f.nameFolder+","+f.area+","+f.cm+"):\n"+e.getMessage());	
			   		         }						    
					    }
					    //Fine inserimento del documento nel path
				  
				  } // Fine del Ciclo per tutti i Path
				  
				  //Cancello i link AUTO 
				  try {
				    deleteOtherLinks(sCheckDelete,idFolderRicavati);
 	   		 	  }
		          catch (Exception e) {
		            throw new Exception("Errore nella cancellazione dei link automatici al documento non più in uso:\n"+e.getMessage());	
		          }				  
				  //Fine cancella link AUTO
				  
				  close();
		      }
		      catch (Exception e) {
		    	  try{close();}catch (Exception ei) {}
		    	  throw new Exception(e);
		      }
	   }
	  
	   /**
	    * Metodo che testa se la workspace passata
	    * e verifica le seguenti cose:
	    * 
	    * 1) area/modello passati esistono o non esistono?
	    * 2) il modello è di tipo workspace?
	    * 3) la workspace esiste? 
	    * 
	    * Restituisce
	    * 
	    * 0 (_WRKSPCHECK_MODEL_NOT_EXIST)   Il modello non esiste
	    * 1 (_WRKSPCHECK_NOT_WRKSP)		  	Non è un modello WorkSpace
	    * 2 (_WRKSPCHECK_WRKSP_NOT_EXIST)   La WorkSpace non esiste ancora
	    * idCartella  						id della workSpace (che esiste)
	    * 
	    * @throws Exception
	   */
	   private int checkWorkspace(Folder f) throws Exception {
		   	   StringBuffer sStm = new StringBuffer("");
		   	   
		   	   sStm.append("SELECT TIPO_USO, ");
		   	   sStm.append("       (SELECT MAX(ID_CARTELLA)"); 
		   	   sStm.append("          FROM CARTELLE, DOCUMENTI");
		   	   sStm.append("         WHERE DOCUMENTI.ID_TIPODOC=MODELLI.ID_TIPODOC");
		   	   sStm.append("           AND DOCUMENTI.ID_DOCUMENTO=CARTELLE.ID_DOCUMENTO_PROFILO");
		   	   sStm.append("           AND CARTELLE.NOME='"+f.nameFolder.replaceAll("'","''")+"'");
		   	   sStm.append("           ) IDCART");
		   	   sStm.append("  FROM MODELLI");		   	   
		   	   sStm.append(" WHERE MODELLI.AREA = '"+f.area+"'");
		   	   sStm.append("   AND MODELLI.CODICE_MODELLO = '"+f.cm+"'");		   	   
		   	   
		   	   dbOpSql.setStatement(sStm.toString());
		   	   dbOpSql.execute();  

		   	   ResultSet rst = dbOpSql.getRstSet();
		   	   if (rst.next()) {
		   		   String tipoUso, idCartella;
		   		   
		   		   tipoUso=rst.getString(1);
		   		   idCartella=Global.nvl(rst.getString(2),"null");
		   		   
		   		   if (!tipoUso.equals("W")) 
		   			   return _WRKSPCHECK_NOT_WRKSP;
		   		   else {
		   			   if (idCartella.equals("null")) 
		   				   return _WRKSPCHECK_WRKSP_NOT_EXIST;
		   			   else
		   				   return Integer.parseInt(idCartella);
		   		   }
		   	   }
		   	   else
		   		   return _WRKSPCHECK_MODEL_NOT_EXIST;
	   }
	   
	   public void parseStringFolder(String str, Folder f) throws Exception {
		      if (str.length()==0) return;
		      		     		      
		      String[] sSplittato;
		      sSplittato=str.split(_ITEMPATH_SEPARATOR);
		      
		      if (sSplittato.length!=3)
		    	  throw new Exception("Attenzione! La Stringa "+str+" (item del path) non è ben formata.\n La sintassi corretta prevede: NomeCartella@Area@CodiceModello");
		      
		      for (int i=0;i<sSplittato.length;i++) {
		    	   switch (i) {
		    	   	case 0: f.nameFolder=sSplittato[i]; break;
		    	   	case 1: f.area=sSplittato[i]; break;
		    	   	case 2: f.cm=sSplittato[i]; break;
		    	   }
		      }
	   }
	   
	   private Vector<Properties> getChildFolder(String idFolder) throws Exception {
		   	   Vector<Properties> vRet = null;
		   	   
		   	   StringBuffer sStm = new StringBuffer("");
		   	   
		   	   sStm.append("SELECT ID_OGGETTO,NOME ");
		   	   sStm.append("  FROM LINKS,CARTELLE ");
		   	   sStm.append(" WHERE LINKS.ID_CARTELLA="+idFolder);
		   	   sStm.append("   AND TIPO_OGGETTO='C'");
		   	   sStm.append("   AND CARTELLE.ID_CARTELLA=ID_OGGETTO");
		       sStm.append("   AND nvl(CARTELLE.STATO,'BO')<>'CA'");
		   	
		   	   
		   	   dbOpSql.setStatement(sStm.toString());
		   	   dbOpSql.execute();  

		   	   ResultSet rst = dbOpSql.getRstSet();
		   	   while (rst.next()) {
		   		   if (vRet==null) vRet = new Vector<Properties>();
		   		   Properties p = new Properties();
		   		   
		   		   p.put(rst.getString(2),rst.getString(1));
		   		   vRet.add(p);		   	   
		   	   }
		   	   
		   	   return vRet;
	   }
	   
	   private void deleteOtherLinks(String checkDelete, Vector vListaIdDontDelete) throws Exception {
		   	   if (checkDelete.equals("N")) return;
		   
		   	   StringBuffer sStm = new StringBuffer("");
		   	   
		   	   sStm.append("DELETE LINKS ");
		   	   sStm.append(" WHERE ID_OGGETTO="+idDoc);		
		   	   sStm.append("   AND TIPO_OGGETTO='D' ");
		   	   sStm.append("   AND AUTOMATICO='Y' ");
		   	   for(int i=0;i<vListaIdDontDelete.size();i++) {
		   		   if (i==0) sStm.append("   AND ID_CARTELLA NOT IN (");
		   		   
		   		   sStm.append(vListaIdDontDelete.get(i));
		   		   
		   		   if (i==vListaIdDontDelete.size() - 1 )  
		   			   sStm.append(")");
		   		   else
		   			   sStm.append(",");
		   	   }		   		   
		   	   
		   	   dbOpSql.setStatement(sStm.toString());
		   	   dbOpSql.execute();  		   	   		   	   
	   }
	   
	   private String isInVector(Vector<Properties> listFolder, String nameFolder) {
		   	   if (listFolder==null) return null;
		   
		       for(int i=0;i<listFolder.size();i++) {
		    	   Properties p = listFolder.get(i);
		    	   
		    	   if (p.containsKey(nameFolder)) return p.getProperty(nameFolder);
		       }		    	   
		       
		       return null;
		       
	   }
	   
	   private IDbOperationSQL connect() throws Exception {
	           if (en.getDbOp()==null) {
	               bIsNew=true;
	               return (new ManageConnection(en.Global)).connectToDB();
	           }
	        
	           return en.getDbOp();
	  }

	  private void close() throws Exception {
	          if (bIsNew) (new ManageConnection(en.Global)).disconnectFromDB(dbOpSql,false,true);        
	  }	   
	  
	  class Folder {
		    public String nameFolder, area, cm;
		    		    		    
		    public Folder(String nameFolder,String area,String cm) {
		    	   this.nameFolder=nameFolder;
		    	   this.area=area;
		    	   this.cm=cm;
		    }
	  }
}

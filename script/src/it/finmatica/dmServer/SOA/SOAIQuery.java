package it.finmatica.dmServer.SOA;

import it.finmatica.dmServer.util.DMServer4j;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Allegato;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Area;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Documento;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Metadato;
import it.finmatica.dmServer.dbEngine.struct.searchObject.MetadatoRicerca;
import it.finmatica.dmServer.dbEngine.struct.searchObject.RisultatoRicerca;
import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.motoreRicerca.ResultSetIQuery;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.XMLUtilDom4j;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


public class SOAIQuery extends SOAIGenericService {	  	   
	   private final static String _CDATAINIT                      = "<![CDATA[";
	   private final static String _CDATAEND                       = "]]>";	   
	     
	   private final static String _OPERATORE_UGUALE               = "=";
	   private final static String _OPERATORE_MAGGIORE             = ">";
	   private final static String _OPERATORE_MINORE               = "<";
	   private final static String _OPERATORE_DIVERSO              = "<>";
	   private final static String _OPERATORE_NULLO                = "is null";
	   private final static String _OPERATORE_NONNULLO             = "is not null";
       private DMServer4j dmS4j;

	   //Costructor for SOA	   
	   public SOAIQuery(HttpServletRequest request,
	          IDbOperationSQL dbOp,
	          String area,
	          String cm) {
		   	  
		      this.httpRequest=request;		     
		      this.user="GDM";//+request.getSession().getAttribute("Utente");
		      this.dbOperation=dbOp;
		      this.areaModel=area;
	  		  this.cmModel=cm;

              dmS4j = new DMServer4j(SOAIQuery.class);
	   }
	   
	   //Costructor for WS
	   public SOAIQuery(String us, String jndi) throws Exception {

              dmS4j = new DMServer4j(SOAIQuery.class);

		   	  user=us;
		   	  
		   	  dbOperation = SessioneDb.getInstance().createIDbOperationSQL(jndi,0);
		   	  
		   	  if (dbOperation==null)
		   		  throw new Exception("Attenzione! Problemi nel creare la connessione verso il database.\nVerificare che la connessione jndi="+jndi+" sia stata definita per il servizio");
	   }

        public SOAIQuery(String us, IDbOperationSQL dbOp) throws Exception {
            user=us;
            dmS4j = new DMServer4j(SOAIQuery.class);

            dbOperation = dbOp;
        }

	   /////////PARTE PUBLIC (PER SOA)
	   public String ricerca(String datiXML) {
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		      		      
		      try {
		    	 Vector<Vector> v = (Vector<Vector>)execRicerca(areaModel,cmModel,null,datiXML,null,-1);
		    	 closeDbOp();
		    	 return (new SOAXMLDataRet(httpRequest.getRequestedSessionId(),v)).getXML();
		      }
			  catch (Exception e) {
				  
				  closeDbOp();
				  return (new SOAXMLErrorRet(e.getMessage())).getXML();		    			                   
			  }	 
	   }
	   
	   	   
	   /////////PARTE PUBLIC (PER WS) -- DA NON METTERE NELLA PUBBL. AUTOMATICA DELLA SOA
	   public RisultatoRicerca ricercaForWS(String area,String modello,String stato,MetadatoRicerca[] mdArray,int maxNumDoc) throws Exception {
		   	  try {
                 dmS4j.log_info("ricercaForWS area="+area+",modello="+modello);
		    	 RisultatoRicerca r = (RisultatoRicerca)execRicerca(area,modello,stato,null,mdArray,maxNumDoc);
		    	 closeDbOp();
		    	 return r;
		      }
			  catch (Exception e) {				  
				  closeDbOp();
				  throw e;		    			                   
			  }	 
	   }
	   
	   	   
	   
	   /////////PARTE PRIVATA O SOLO DI QUESTA CLASSE (NON ESPOSTA A NULLA)	   
	   public Object execRicerca(String area,String modello, String stato,String datiXML,MetadatoRicerca[] mdArray,int maxNumDoc) throws Exception {
		   	  Vector<Object> vDoc=null;
		   	  boolean bIsWs;
		
		   	  IQuery q;
		   	  Vector<keyval> vDati;
		   	  
		   	  //////PARSE DEI DATI INIZIALI
		   	  try {		   		 
		   		if (datiXML==null) {
		   			vDati=parseMetadatiDatiRicercaToKeyVal(mdArray);
		   			bIsWs=true;
		   		}		   			
		   		else {
		   			vDati=parseDatiXML(datiXML);
		   			bIsWs=false;
		   		}
		   			
		   	  }
		   	  catch (Exception e) {	
		   		  throw new Exception("Prima della ricerca - Errore in parse dei metadati di ricerca: " +e.getMessage());		   		  		    			                  
		   	  }	
		   	  
		   	  //////COSTRUZIONE OGGETTO QUERY
		   	  try {
		        q = new IQuery();
		  								  
		        q.initVarEnv(user,user,dbOperation.getConn());

		        modello = Global.nvl(modello, "");

				if (q.getQueryServiceLimit()!=0 && q.getQueryServiceLimit() < maxNumDoc) {
					maxNumDoc = q.getQueryServiceLimit();
					q.setAbilitaQueryServiceLimit(true);
				}
		        
		        String categoria=null;
		        
		        if (modello.indexOf("@")==-1) {		        
			        q.settaArea(area);
			        q.addCodiceModello(modello);	
		        }
		        else 
		        	categoria = modello.substring(1);		        	
		        
		        	
		      
		        for(int i=0;i<vDati.size();i++) {
			        keyval k = vDati.get(i);
			        
			        if (area.equals("SEGRETERIA") && modello.equals("M_SOGGETTO"))
			        	q.addCampo("TIPO_RAPPORTO", "DUMMY", "<>");
			  	  
				  	if (k.getTipoDoc().equals("CONDITION")) {
				  		  if (nvl(k.getKey(),"").equals("ID_DOCUMENTO")) {
				  			q.settaIdDocumentoRicerca(k.getVal());
				  		  }
				  		  else {
					  		  if (nvl(k.getTipoUguaglianza(),"").trim().equals("")) {
					  			  if (k.getOperator().equals("LIKE"))  {
					  				  if (categoria==null) q.addCampo(k.getKey(),k.getVal()+"%");
					  				  else q.addCampoCategoria(k.getKey(),k.getVal()+"%",categoria);
					  			  }
					  			  else if (k.getVal().indexOf("%")!=-1 || k.getOperator().equals("=")) {
					  				  if (categoria==null) q.addCampo(k.getKey(),k.getVal());
					  				  else q.addCampoCategoria(k.getKey(),k.getVal(),categoria);
					  			  }
					  			  else  {
					  				 if (categoria==null)  q.addCampo(k.getKey(),k.getVal(),k.getOperator());
					  				 else q.addCampoCategoria(k.getKey(),k.getVal(),k.getOperator(),categoria);
					  			  }
					  		  }
					  		  else {
					  			  if (categoria==null) q.addCampo(k.getKey(),k.getVal(),k.getTipoUguaglianza());
					  			  else  q.addCampoCategoria(k.getKey(),k.getVal(),k.getTipoUguaglianza(),categoria);
					  		  }
				  		  }
				  	}
				  	else
				  		  q.addCampoReturn(k.getKey(),areaModel,cmModel);
				    }
		     }
		     catch (Exception e) {		  
			    throw new Exception("Prima della ricerca - Errore in costruzione oggetto ricerca: " +e.getMessage());		  	  		    			                  
		     }

             dmS4j.log_info("execRicerca ESECUZIONE QUERY");
		     //////ESECUZIONE QUERY		     
		     try {
		       q.setInstanceProfile(false);
		       q.setAccessProfile(false);
		    		
		       if (q.ricercaFT().booleanValue()) {
		    	   if (!bIsWs)
		    		   return fieldSOAReturn(q,maxNumDoc);		
		    	   else
		    		   return fieldWSReturn(q,maxNumDoc,stato);
		       }
		       else {		    		
		    	    if (bIsWs) {
		    	    	if (q.isQueryTimeOut())
		    	    		throw new Exception(q.getError());
		    	    	else
		    	    		return (new RisultatoRicerca(null,0,0));
		    	    }
		    	    else
		    	    	throw new Exception(URLEncoder.encode(q.getError()));		    		 		    	   
		       }
		     }
		     catch (Exception e) {				  
		    	throw new Exception("Errore in Ricerca: " +e.getMessage());    		        				 
		     }						     
	   }
	   
	   private RisultatoRicerca fieldWSReturn(IQuery q,int maxNumDoc,String statoFilter) throws Exception {
		   	   ResultSetIQuery  rst = q.getResultSet();

               dmS4j.log_info("execRicerca fieldWSReturn");
		   	   Documento[] listaDoc =null;

               if (q.getProfileNumber()>maxNumDoc) {
				   listaDoc = new Documento[maxNumDoc];
			   }
               else {
				   listaDoc = new Documento[q.getProfileNumber()];
			   }

		   	   int numDoc=0;
		   	   int indexToRemove=0;
		   	   int numDocTot=q.getProfileNumber();
		   	   while (rst.next()) {
		   		    try {		   		    	
		   		    	
		   		    	if (maxNumDoc==numDoc) break;
		   		    			   		    	
			   		   	Profilo p = new Profilo(rst.getId());			   		   	
			   		  
			   		   	p.initVarEnv(user, "", dbOperation.getConn());		   		   		   		   
			   		   	
			   		   	if (!p.accedi(Global.ACCESS_ATTACH, false).booleanValue()) {
			   		   		throw new Exception("Accesso al documento.\nErrore: "+p.getError());
			   		   	}
			   		   	
			   		   	String stato=null;			   		 
			   		   	try {
			   		      stato=p.getStato(); 
			   		    }
			   		    catch(Exception e) {
			   		      throw new Exception("Attenzione! Errore nell'estrapolare lo stato del documento.\nErrore:"+e.getMessage());
			   		    }
			   		    
			   		    if (!stato.equals(statoFilter) && !statoFilter.equals("T")) {
			   		    	indexToRemove++;
			   		    	continue;
			   		    }
			   		    
			   		    numDoc++;
			   		 
			   		   	//TODO....da implementare la retrive della data di creazione
			   		   	String sDataCreazione;
			   		   
			   		    String area,cm;
			   		    
			   		    area=p.getArea();
			   		    cm=p.getCodiceModello();
			   		    sDataCreazione=p.getDataCreazione();
			   			 
			   		    Documento d=null;
			   		    d = new Documento(Long.parseLong(rst.getId()),area,cm,sDataCreazione,stato);
			   		    
			   		    //Ciclo sui metadati
			   		    Metadato[] mtDati=null;
			   		    Vector<Metadato> vMeta=null;
			   		    
			   		    try {
				   		    LookUpDMTable lUpDmT = new LookUpDMTable(new Environment(user,"","","","",dbOperation.getConn()));			   		 			   		    
	
				   		    vMeta = lUpDmT.lookUpListaMetaDati(area, cm);
				   		    mtDati = new Metadato[vMeta.size()];
			   		    }
			   		    catch(Exception e) {
			   		      throw new Exception("Attenzione! Errore nell'estrapolare la lista dei metadati del del documento.\nErrore:"+e.getMessage());
			   		    }  
			   		
			   		    for(int i=0;i<vMeta.size();i++) {
			   		    	try {
			   		    	  vMeta.get(i).setValore(p.getCampo(vMeta.get(i).getCodice()));
			   		    	}
					   		catch(Exception e) {
					   			throw new Exception("Attenzione! Errore nell'estrapolare il valore del dato <"+vMeta.get(i).getCodice()+">.\nErrore:"+e.getMessage());
					   		}
					   		
					   		mtDati[i]=vMeta.get(i);
			   		    }
			   		
			   		    d.setMetadati(mtDati);
			   		    
			   		    Allegato[] alleg=null;

			   		    /*if (p.getListaFilesize()>0) {
			   		    	alleg = new Allegato[(int)p.getListaFilesize()];

				   		    //Ciclo sugli allegati
				   		    for(int i=0;i<p.getListaFilesize();i++) {
				   		    	Allegato a;
				   		    	byte file[]=null;
				   		    	long idFile;

				   		    	//TODO....da implementare la retrive della data di creazione
				   		    	String dataIns=p.getUpdateDateFile(i+1);

				   		        String nome=p.getFileName(i+1);

						   		try {
						   		  idFile=Long.parseLong(p.getIdFile(nome));
						   		}
						   		catch(Exception e) {
						   		  throw new Exception("Attenzione! Errore nell'estrapolare l'identificativo del file <"+nome+">.\nErrore:"+e.getMessage());
						   		}

				   		    	String ext;
				   		    	ext=Global.lastTrim(nome,".","").toUpperCase();

				   		    	a = new Allegato(idFile,nome,ext,dataIns,null);

				   		    	alleg[i]=a;
				   		    }
			   		    }*/

			   		    //....Modifica per vedere anche i file del repository. 11/01/2021
						//....Modifica per vedere anche i file del repository degli allegati figli. 02/09/2021 #52208
						String sql = "SELECT oggetti_file.id_oggetto_file, "
							+ "       oggetti_file.filename, "
							+ "       to_char(oggetti_file.data_aggiornamento,'dd/mm/yyyy HH24:mi:ss') "
							+ "  FROM ALLEGATI_LISTA_HTML, oggetti_file "
							+ " WHERE ALLEGATI_LISTA_HTML.id_documento = "+rst.getId()
							+ "       AND oggetti_file.id_oggetto_file = "
							+ "              ALLEGATI_LISTA_HTML.id_oggetto_file and  ALLEGATI_LISTA_HTML.id_documento_figlio=0  "
							+ "  UNION "
							+ " SELECT oggetti_file.id_oggetto_file, "
							+ "       oggetti_file.filename, "
							+ "       to_char(oggetti_file.data_aggiornamento,'dd/mm/yyyy HH24:mi:ss') "
							+ "  FROM ALLEGATI_LISTA_HTML, oggetti_file "
							+ " WHERE  oggetti_file.id_oggetto_file = "
							+ "              ALLEGATI_LISTA_HTML.id_oggetto_file and  ALLEGATI_LISTA_HTML.id_documento_figlio=  "+rst.getId()
							+ " order by 2";
                        dmS4j.log_info("execRicerca sql="+sql);

						dbOperation.setStatement(sql);
						dbOperation.execute();

						ResultSet rstAllegati = dbOperation.getRstSet();
						int i=0;
						List<Allegato> allegatoList = new ArrayList<Allegato>();
						while (rstAllegati.next()) {
							Allegato a;


							long idFile=rstAllegati.getLong(1);
							String fileName=rstAllegati.getString(2);

							String ext;
							ext=Global.lastTrim(fileName,".","").toUpperCase();

							String dataIns =rstAllegati.getString(3);

							a = new Allegato(rstAllegati.getLong(1),fileName,ext,dataIns,null);

							i++;
							allegatoList.add(a);
						}

						alleg = allegatoList.toArray(new Allegato[i]);
			   		
			   		    d.setAllegati(alleg);
			   		    			   		    
			   		    listaDoc[numDoc - 1]=d;

                        dmS4j.log_info("execRicerca FINE ALLEGATI");
		   		    }
			   		catch(Exception e) {
			   		    throw new Exception("Attenzione! Errore nell'accesso dal documento "+rst.getId()+".\n:"+e.getMessage());
			   		}
		   	   }		   	   		

		   	   Documento[] listaDocEffettivo = new Documento[listaDoc.length - indexToRemove];
		   	   for(int i=0, j=0;i<listaDoc.length;i++) 
		   		  if (listaDoc[i]!=null) listaDocEffettivo[j++]=listaDoc[i];
		   	   		   	   
		   	   return (new RisultatoRicerca(listaDocEffettivo,numDocTot,numDoc));
	   }
	   
	   private Vector<Vector> fieldSOAReturn(IQuery q,int maxNumDoc)  {
		        ResultSetIQuery  rst = q.getResultSet();
		   		Vector<String> vElencoCampi = rst.getFieldsList();
		   		Vector<Vector> vRet = new Vector<Vector>();					
		   			
		   		while (rst.next()) {
		   			  Vector<keyval> vKeyVal = new Vector<keyval>();
		   				
		   			  for(int iCampo=0;iCampo<vElencoCampi.size();iCampo++) {
		   					if (vElencoCampi.get(iCampo).equals("_CR_")) continue;
		   					
		   					String value=rst.get(vElencoCampi.get(iCampo),areaModel, cmModel);
		   					
		   					String typeAndPrecision="";
		   					String type="";
		   					String precision="";							
		   						
		   					try {
		   					  typeAndPrecision=rst.getTypePrecision(vElencoCampi.get(iCampo),areaModel, cmModel,null);
		   					  type=typeAndPrecision.substring(0,typeAndPrecision.indexOf("@"));
		   					  precision=typeAndPrecision.substring(typeAndPrecision.indexOf("@")+1);							  
		   					}
		   					catch (java.lang.NoSuchMethodError eN) {
		   					  type="";
		   					  precision="";
		   					}
		   					catch(Exception expI) {
		   					  type="";
		   					  precision="";
		   					}
		   																				
		   					if (value.equals("null")) value="";
		   					
		   					keyval kRet = new keyval(vElencoCampi.get(iCampo),
		   											 value);
		   					
		   					kRet.setTipoDaClient(type);
		   					
		   					kRet.setTipoUguaglianza(precision);
		   					
		   					if (vElencoCampi.get(iCampo).equals("ID")) {
		   						kRet.setKey("IDDOCUMENT");
		   						vKeyVal.add(0,kRet);
		   					}
		   					else
		   						vKeyVal.add(kRet);
		   				}
		   				
		   				vRet.add(vKeyVal);
		   			}
		   		
		   			return vRet;
	   }
	   
	   protected String generateErrorParameterMessage(int iCheckPar) {
	       StringBuffer ret = new StringBuffer("");
	       
	       switch (iCheckPar) {
	       	  case _ERR_INTEGRITY_PAR____NOPARAMETER:
	       		   ret.append("Mancano parametri chiave: ar,cm");
	       		   break;	       	 
	       	  case _ERR_INTEGRITY_PAR____NOAREA:
	       		   ret.append("Indicare area");
	       		   break;
	       	case _ERR_INTEGRITY_PAR____NOCM:
	       		   ret.append("Indicare cm");
	       		   break;
	       		
	       }
	       
	       return ret.toString();
       }		   
	   
	   protected int checkIntegrityParameter() {
	       if (nvl(areaModel,"").trim().equals("") && nvl(cmModel,"").trim().equals("") )
	    	   return _ERR_INTEGRITY_PAR____NOPARAMETER;	   
	       
	       if (nvl(areaModel,"").trim().equals("") )
	    	   return _ERR_INTEGRITY_PAR____NOAREA;
	       
	       if (nvl(cmModel,"").trim().equals("") )
	    	   return _ERR_INTEGRITY_PAR____NOCM;
	       
	       return 0;
       }   
	   
	   public Vector<keyval> parseDatiXML(String dati) throws Exception {
		   	   Vector<keyval> vRet = new Vector<keyval>();
		   	   
		   	   Document dXMLDati = null;
		       XMLUtilDom4j xmlUt = null;
		       
		       try {
		    	 dXMLDati = DocumentHelper.parseText(dati);
		       }
			   catch (Exception e) {
			     throw new Exception("parseDatiXML - Errore in parse XML dati \n"+e.getMessage()); 
			   }
			   
			   if (dXMLDati == null)		      
			       throw new Exception("parseDatiXML - Document XML dati nullo!");	
			   
			   
			   xmlUt = new XMLUtilDom4j(dXMLDati);
			   
			   Vector<Element> vEl=xmlUt.leggiChildElementXML(dXMLDati.getRootElement());
		    	  
		       for(int i=0;i<vEl.size();i++) {
		    	   String op=xmlUt.leggiValoreXML(vEl.get(i),"operatore");
		    	   String nome=xmlUt.leggiValoreXML(vEl.get(i),"nome");
		    	   String valore=xmlUt.leggiValoreXML(vEl.get(i),"valore");
		    	   String valore2=xmlUt.leggiValoreXML(vEl.get(i),"valore2");
		    	   String tipo=xmlUt.leggiValoreXML(vEl.get(i),"type");
		    	
		    	   if (!tipo.equals("RETURN") && nvl(valore2,"").trim().equals("")) {
		    		   if (!bIsOperator(op)) 
		    			   throw new Exception("parseDatiXML - Document XML dati - Operatore "+op+" non applicabile!");
		    	   }
		    	   
		    	   keyval k = new keyval(nome,
		    			   				 Global.replaceAll(valore,"$","%"));
		    	  
		    	   k.setOperator(op);
		    	   //Lo uso per il between
		    	   k.setTipoUguaglianza(valore2);
		    	   k.setTipoDoc(tipo);

		    	   vRet.add(k);		    	   		    		  			    	  
			   }
		   	   
		       return vRet;
	   }
	   
	   public Vector<keyval> parseMetadatiDatiRicercaToKeyVal(MetadatoRicerca[] metaDati) throws Exception {
		   	  Vector<keyval> vRet = new Vector<keyval>();
		   	  
		   	  for(int i=0;i<metaDati.length;i++) {
		   		  if (metaDati[i]==null) throw new Exception("Attenzione! la struttura dei metadati contiene un oggetto nullo nella posizione "+i);
		   		  keyval k = new keyval(metaDati[i].getMetadato(),metaDati[i].getValore());
		   		  
		   		  String operatore=MetadatoRicerca.OP_UGUALE;
			   	  switch (metaDati[i].getOperatore()) {
			   	  	case 1: {
			   	  				operatore=MetadatoRicerca.OP_UGUALE;
			   	  				break;
			   	  			}
			   	  	case 2: {
	   	  						operatore=MetadatoRicerca.OP_LIKE;
	   	  						break;
	   	  					}	
			   	  	case 3: {
	 							operatore=MetadatoRicerca.OP_MAGGIOREUGUALE;
		  						break;
		  					}
			   	  	case 4: {
			   	  				operatore=MetadatoRicerca.OP_MAGGIORE;
			   	  				break;
			   	  			}		
			   	  	case 5: {
	   	  						operatore=MetadatoRicerca.OP_MINOREUGUALE;
	   	  						break;
	   	  					}	
			   	  	case 6: {
	 							operatore=MetadatoRicerca.OP_MINORE;
	 							break;
	 						}		
			   	  	case 7: {
			   	  				operatore=MetadatoRicerca.OP_ISNULL;
			   	  				break;
						    }		
			   	  	case 8: {
	   	  						operatore=MetadatoRicerca.OP_ISNOTNULL;
	   	  						break;
			   	  			}			   	  	
			   	  }
		   		  k.setOperator(operatore);		    	  
		    	  k.setTipoUguaglianza("");
		    	  k.setTipoDoc("CONDITION");
		    	  
		    	  vRet.add(k);
		   	  }
		   	  
		   	  return vRet;
	   }
	   
	   private boolean bIsOperator(String op) {
		       boolean bIsOp=true;
		       
		       if (
		    	    !op.equals(_OPERATORE_UGUALE) && 
		    	    !op.equals(_OPERATORE_MAGGIORE) &&
		    	    !op.equals(_OPERATORE_DIVERSO) &&
		    	    !op.equals(_OPERATORE_UGUALE) &&
		    	    !op.equals(_OPERATORE_NULLO) &&
		    	    !op.equals(_OPERATORE_NONNULLO) 
		    	   )
		    	   bIsOp= false;
		       
		       return bIsOp;
	   }
}

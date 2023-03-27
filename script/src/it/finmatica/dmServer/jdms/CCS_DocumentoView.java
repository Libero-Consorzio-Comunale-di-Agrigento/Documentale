package it.finmatica.dmServer.jdms;

import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.monoRecord.*;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.*;

import java.net.URLEncoder;
import java.util.*;
import java.sql.*;


/**
 * Gestione visualizzazione del Documento.
 * Classe di servizio per la gestione del Client
*/  

public class CCS_DocumentoView 
{
	   /**
	    * Costante per immaggini
	   */
	   private static String _PATHIMG             ="./images/standard/action/";
	   private static String _EDIT                =_PATHIMG+"edit.png";      
	   private static String _COMPLETO            =_PATHIMG+"CompletoGIF.gif";   
	   private static String _ANNULLATO           =_PATHIMG+"AnnullatoGIF.gif"; 
	   private static String _ANNULLA             =_PATHIMG+"delete.png";   
	   private static String _CART           	  =_PATHIMG+"folder.png";   
	   private static String _CARTELLAGDC         =_PATHIMG+"folder.png";
	   private static String _DOT                 =_PATHIMG+"document.png";   
    
	   /**
	    * Variabili private
	   */	 
	   private String area;
	   private String cm;
	   private String cr;
	   private String rw;
	   private String idDoc;
	   private String idCartella;
	   private String nomeCart;
	   private String idDocRif;
	   HttpServletRequest req;
	   private String wrkspRIF;
	   private String wrksp;
	   private String tipoProv;
	   private String idCartProv;
	   private String idQueryProv;
	   private String Prov;
	   private String stato;
	   private String MVPG;
	   private String[] vDocRif;
	   private String tipo_rif;
	   private String listaID;
	   private String queryString;
	   private ArrayList list=null;
	   CCS_Common CCS_common;
	   CCS_HTML h;
	   private IDbOperationSQL dbOp;
	   private Environment vu;  
	   private String user;
	   private String sModifica=""; 
	   private String sCompleto=""; 
	   private String sAnnullato=""; 
	   private String sCancellato=""; 
	   private String seq="";
	   private String spulsanti="";
	   Vector vlistaID=null;
	   private String tipoOggetto;
	   private String nome;
	   private String onclick;
	   private String ute_agg;
	   private String data_agg;
	   
	   private XSS_Encoder xss=null; 
	   
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato dalle operazioni di Modifica,
		 * Annulla,Completa,Elimina.
		 * 
		 */
	   public CCS_DocumentoView(String newMVPG,String newarea,String newcm,String newcr,String newrw,String newidCartProv,
			   					String newidQueryProv,String newProv,CCS_Common newCommon) throws Exception
       {
		       MVPG=newMVPG; 
		       area=newarea;
			   cm=newcm;
			   cr=newcr;
			   rw=newrw; 
			   idCartProv=newidCartProv;
			   idQueryProv=newidQueryProv;
			   Prov=newProv;
               //idDoc=newidDoc;		   
			   //stato=newstato;
			   //spulsanti=newpulsanti;
			   CCS_common=newCommon;
			   log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			   user=CCS_common.user;
			   h = new CCS_HTML();
		       init(CCS_common);
			   
			   if(cr!=null && !cr.equals(""))
			    idDoc=(new DocUtil(vu)).getIdDocumentoByAreaCmCr(area,cm,cr);
			   
			   if(idDoc!=null && !idDoc.equals(""))
			    stato=(new DocUtil(vu)).getStatoByIdDocumento(idDoc);
       }
	  
	
	   
	   public CCS_DocumentoView(String newMVPG,String newarea,String newcm,String newcr,String newrw,String newidDoc,String newidCartProv,
					String newidQueryProv,String newProv,String newstato,String newpulsanti,CCS_Common newCommon) throws Exception
		{
			MVPG=newMVPG; 
			area=newarea;
			cm=newcm;
			cr=newcr;
			rw=newrw; 
			idDoc=newidDoc;		   
			idCartProv=newidCartProv;
			idQueryProv=newidQueryProv;
			Prov=newProv;
			stato=newstato;
			spulsanti=newpulsanti;
			CCS_common=newCommon;
			log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			user=CCS_common.user;
			h = new CCS_HTML();
			init(CCS_common);
		}
		
	   
	   /**
		 * Costruttore utilizzato dalle visualizzazione dei pulsanti.
		 * 
		 */
	   public CCS_DocumentoView(CCS_Common newCommon) throws Exception
      {
		      CCS_common=newCommon;
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common);
		      init(CCS_common);
      }
	   
	   /**
		 * Costruttore utilizzato per la visualizzazione del monorecord 
		 * di un Documento.
		 * 
		 */
	   public CCS_DocumentoView(String newprofilo,HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
		   	  idDoc=newprofilo; 
		      req=newreq;
		      CCS_common=newCommon;
		      h = new CCS_HTML();
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			  xss = new XSS_Encoder(req,CCS_common);
			  init(CCS_common);
      } 
	   
	   /**
		 * Costruttore utilizzato per la visualizzazione del monorecord 
		 * di un oggetto di tipo Cartella.
		 * 
		 */
	   public CCS_DocumentoView(String newprofilo,String newtipoOggetto,String newnome,String newonclick,String newute_agg,String  newdata_agg,HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
		      idDoc=newprofilo; 
		      tipoOggetto=newtipoOggetto;
		      nome=newnome;
		      onclick=newonclick;
		      ute_agg=newute_agg;
		      data_agg=newdata_agg;
		      req=newreq;
		      CCS_common=newCommon;
		      h = new CCS_HTML();
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			  xss = new XSS_Encoder(req,CCS_common);
		      init(CCS_common);
       }
	   
	   
	   /**
		 * Costruttore dalla Tabella Collegamenti per quanto riguarda 
		 * la visualizzazione dei Riferimenti del Documento.
		 * 
		 */
	   public CCS_DocumentoView(String newidDoc,String newidDocRif,String newidCartella,String newtipoProv,String tipo_relazione,String newstato,String newwrkspRIF,String newwrksp,String newnomeCart,HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
			  idDoc=newidDoc;
			  idDocRif=newidDocRif;
			  tipo_rif=tipo_relazione;
			  idCartella=newidCartella;
			  tipoProv=newtipoProv;
			  wrkspRIF=newwrkspRIF;
			  wrksp=newwrksp;
			  nomeCart=newnomeCart;
			  stato=newstato;
			  req=newreq;
			  CCS_common=newCommon;
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			  user=CCS_common.user;
			  h = new CCS_HTML();
			  xss = new XSS_Encoder(req,CCS_common);
		      init(CCS_common);
	   } 
	   
	   /**
		 * Costruttore dalla Tabella Collegamenti per quanto riguarda 
		 * l'inserimento dei Riferimenti al Documento.
		 * 
		 */
	   public CCS_DocumentoView(String newidDoc,String listDocRif,String newtipo_rif,String newlistaID,CCS_Common newCommon) throws Exception
	   {
			  idDoc=newidDoc;
			  if(listDocRif!=null)
			    vDocRif=Global.Split(listDocRif,",");
			  tipo_rif=newtipo_rif;
			  listaID=newlistaID;
			  CCS_common=newCommon;
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			  user=CCS_common.user;
			  h = new CCS_HTML();
	   }
	   
	   /**
		 * Costruttore dalla Tabella Collegamenti per quanto riguarda 
		 * la cancellazione dei Riferimenti al Documento.
		 * 
		 */
	   public CCS_DocumentoView(String newidDoc,String newidCartella,ArrayList listDocRif,CCS_Common newCommon) throws Exception
	   {
			  idDoc=newidDoc;
			  if(newidCartella.equals("null"))
			   idCartella="";
			  else
			   idCartella=newidCartella;	
			  list=listDocRif;
			  CCS_common=newCommon;
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			  user=CCS_common.user;
			  h = new CCS_HTML();
		      init(CCS_common);
	   } 
	   
	   public CCS_DocumentoView(HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
			  req=newreq;
			  CCS_common=newCommon;
			  log= new DMServer4j(CCS_DocumentoView.class,CCS_common); 
			  user=CCS_common.user;
			  init(CCS_common);
			  xss = new XSS_Encoder(req,CCS_common);
	   }

		private void init(CCS_Common newCommon)  throws Exception
		{
			CCS_common=newCommon;
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
	   
	   /***************************************************************************
		* VISUALIZZAZIONE BOTTONI
		**************************************************************************/
	   
	   /**
		 * Gestione annulla del Documento.
		 * 
		 */
	   public String getTableFolder() throws Exception 
	   {
	          String table="",l="",gdc_link,hrefD,hrefS,hrefC;
	          String sql="",parametro="S";
	          
	          try
	          {
		         sql="select valore from parametri where codice='JDMS_TABFOLDER_DOC' and tipo_modello='@DMSERVER@'";
				 dbOp.setStatement(sql);
				 dbOp.execute();
				 ResultSet rs = dbOp.getRstSet();
				 if(rs.next())
				  parametro=rs.getString("valore");
	          }
	          catch (Exception e) 
	          {
	        	throw e;
	          }
			  finally {
				  _finally();
			  }
	          
	          if(parametro!=null && parametro.equals("S")){
	          
		          if (Prov.equals("Q"))
		            gdc_link = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProv;
	              else
	               	gdc_link = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";                    
		          gdc_link=URLEncoder.encode(gdc_link);
	         	          
		          if(listaID!=null)
		        	l="&listaID="+listaID;  
		          
		          hrefD="DocumentoView.do?MVPG=ServletModulisticaDocumento&idDoc="+idDoc+"&rw="+rw+"&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+"&Provenienza="+Prov+"&stato="+stato+"&listaID="+l+spulsanti+"&GDC_Link="+gdc_link;
		          hrefC="DocumentoView.do?MVPG=DocumentoRiferimenti&idDoc="+idDoc+"&rw="+rw+"&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+"&Provenienza="+Prov+"&stato="+stato+"&listaID="+l+spulsanti+"&GDC_Link="+gdc_link;
		          hrefS="DocumentoView.do?MVPG=StatiDocumento&idDoc="+idDoc+"&rw="+rw+"&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+"&Provenienza="+Prov+"&stato="+stato+"&listaID="+l+spulsanti+"&GDC_Link="+gdc_link;
		            		
		          
		          if(MVPG.equals("ServletModulisticaDocumento"))
		          {
		        	  table+="<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td>";
		        	  table+=this.getFolder("sel","documento","Documento","#");
		        	  table+="</td><td>";
			          table+=this.getFolder("","collegamenti","Collegamenti",hrefC);
			          table+="</td><td>";
			          table+=this.getFolder("","cronologia","Cronologia",hrefS);
			          table+="</td></tr></table>";
			          
		          }
		          else
		          {
		        	  if(MVPG.equals("DocumentoRiferimenti"))  
		        	  {
		        		  table+="<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td>";
			        	  table+=this.getFolder("","documento","Documento",hrefD);
			        	  table+="</td><td>";
			        	  table+=this.getFolder("sel","collegamenti","Collegamenti",hrefC);
			        	  table+="</td><td>";
			        	  table+=this.getFolder("","cronologia","Cronologia",hrefS);
			        	  table+="</td></tr></table>";
			          }
		        	  else
		        	  {
		        		  table+="<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td>";
		        		  table+=this.getFolder("","documento","Documento",hrefD);
		        		  table+="</td><td>";
			        	  table+=this.getFolder("","collegamenti","Collegamenti",hrefC);
			        	  table+="</td><td>";
			        	  table+=this.getFolder("sel","cronologia","Cronologia",hrefS);
			        	  table+="</td></tr></table>";
			          }
		          }
	          }    
		      return table;
	   }
	   
	   
	   /**
		 *
		 * 
		 */
	   public String getFolder(String sel,String img,String titolo,String href) throws Exception 
	   {
	          return "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr>" +
	          		 "<td align=\"left\" valign=\"top\" CLASS=\"AFCGuida"+sel+"L\"><img src=\"../Themes/Default/GuidaBlank.gif\" ></td>" +
	          		 "<td align=\"left\" valign=\"bottom\" nowrap CLASS=\"AFCGuida"+sel+"\"><img src=\"../common/images/tabfolder/"+img+".gif\" ></td>" +
	          		 "<td align=\"left\" valign=\"center\" nowrap CLASS=\"AFCGuida"+sel+"\"><a class=\"AFCGuidaLink\" title=\""+titolo+"\" href=\""+href+"\">"+titolo+"</a></td>" +
	          		 "<td align=\"left\" valign=\"top\" CLASS=\"AFCGuida"+sel+"R\"><img src=\"../Themes/Default/GuidaBlank.gif\" ></td>" +
	          		 "</tr></table>";
	   }
	   
	   
	   /**
		 * Gestione Logo
		 * 
		 */
	   public String getLOGO() throws Exception 
	   {
		      String sql,logo="";
			        
			  try
	          {
		         sql="select gdc_utility_pkg.F_LOGO_DOCUMENTO(:MODULO) logo from dual";
				 dbOp.setStatement(sql);
				 dbOp.setParameter(":MODULO",req.getSession().getAttribute("Modulo").toString());
				 dbOp.execute();
				 ResultSet rs = dbOp.getRstSet();
				 if(rs.next())
				  logo=rs.getString("logo");
				 
				 if(logo==null)
                  logo="";
	          }
	          catch (Exception e) 
	          {
	        	throw e;
	          }
			  finally {
				  _finally();
			  }
	          return logo;
	   }  

	   /**
		 * Gestione Visualizzazione Pulsanti.
		 * 
		 */
	   public String getParametroPulsanti() throws Exception 
	   {
		      String sql,val,par;
			        
			  try
	          {
		         sql="select valore from parametri where codice='PULSANTI_DOCVIEW' and tipo_modello='@DMSERVER@'";
				 dbOp.setStatement(sql);
				 dbOp.execute();
				 ResultSet rs = dbOp.getRstSet();
				 if(rs.next())
				 {
					 val= rs.getString("valore");
					 if(val==null || (val!=null && val.equals("")))
					  par="";
					 else
					  par=val;	 
				 }
				 else
				   par=null;

	          }
	          catch (Exception e) 
	          {
	        	throw e;
	          }
			  finally {
				  _finally();
			  }
		      return par;
	   }  
	   
	   /**
		 * Costruzione del nome del documento.
		 * 
		 * @return String nome
		 */
	   public String getBloccoDoc() throws Exception
	   {
		      String nome="";
	          
	          if(idDoc!=null)
	          {
	        	try
		        {
	        	  nome=getRigaMonoRecord(idDoc,"D","",req);

		        }
		        catch (Exception e) 
		        {
		           throw e;
		        }
				finally {
					_finally();
				}
			}
	        return nome;
	   }
	   
	   /**
		 * Gestione modifica del Documento.
		 * 
		 */
	   public String _beforeShow_Modifica() throws Exception 
	   {
	          try
	          {
	           if(idDoc != null)
	             modificaHandler();
	          }
	          catch (Exception e) 
	          {
	        	throw e;
	          }
			  finally {
				  _finally();
			  }
		      return sModifica;
	   }  
	   
	   /**
		 * Gestione cancella del Documento.
		 * 
		 */
	   public String _beforeShow_CancellaDoc() throws Exception 
	   {
		      try
		      {
		        if(idDoc != null)
		          changeStatoCancellato();
		      }
		      catch (Exception e) 
		      {
		        throw e;
		      }
		      finally {
				  _finally();
			  }
		      return sCancellato;
	   }
	   
	   /**
		 * Gestione completo del Documento.
		 * 
		 */
	   public String _beforeShow_CompletoDoc() throws Exception 
	   {
	          try
	          {
	            if(idDoc != null)
	             changeStatoCompleto();
	          }
	          catch (Exception e) 
	          {
	        	 throw e;
	          }
			  finally {
				  _finally();
			  }
		      return sCompleto;
	   }
	   
	   /**
		 * Gestione annulla del Documento.
		 * 
		 */
	   public String _beforeShow_AnnullatoDoc() throws Exception 
	   {
	          try
	          {
	           if(idDoc != null)
	             changeStatoAnnullato();
	          }
	          catch (Exception e) 
	          {
	            throw e;
	          }
			  finally {
				  _finally();
			  }
		      return sAnnullato;
	   }
	   
	   /**
		 * Verifica se il documento è nello stato BOZZA
		 * verifica se possiede le competenze di modifica
		 * se true inserisce il link, altrimenti nulla.
		 * 
		 */
	   private void modificaHandler() throws Exception
	   {
		   	   String url;
                    
	           try
	           {
	            if (stato.equals("BO"))
	            { 
	              vu.connect();
	              if(!verificaCompetenzaDocumento(idDoc,Global.ABIL_MODI))
	                sModifica="";                        
	              else
	              { 
	                if (Prov.equals("Q"))
				      url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProv;
	                else
				      url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";                    
	                /** Link alla pagina SceltaTipoDocumentoForNew */
	                sModifica=h.getAncore("#","popup('DocumentoView.do?idDoc="+idDoc+"&rw=W&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+"&Provenienza="+Prov+"&stato="+stato+"&GDC_Link="+URLEncoder.encode(url)+"&MVPG=ServletModulisticaDocumento');","",h.getImgHand("Modifica il Documento",_EDIT));
	              } 
	              //vu.disconnectClose();
	            }
	           }
	           catch (Exception e) {
	        	//vu.disconnectClose();
	        	throw e;
	           }
	   } 
	   
	   /**
		 * Setta lo stato del Documento a COMPLETO.
		 * 
		 */
	   private void changeStatoCompleto() throws Exception
	   {
		       String url;
		       try
		       {
		           if (stato.equals("BO"))
		           {
		              vu.connect();
		              if(!verificaCompetenzaDocumento(idDoc,Global.ABIL_MODI))
		                sCompleto="";                        
		              else
		              {	  
		                 if (Prov.equals("Q"))
						   url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProv;
				         else
						   url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";                    
				        
		                 sCompleto=h.getAncore("#","if (confirm('Sei sicuro di voler VALIDARE il Documento?\\t\\t\\nAttenzione il Documento non è piu modificabile!\\t\\t\\n')==true) {popup('DocumentoView.do?idDoc="+idDoc+"&rw=R&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+"&Provenienza="+Prov+"&stato="+stato+spulsanti+"&GDC_Link="+URLEncoder.encode(url)+"&conferma=COMPLETO&MVPG=ServletModulisticaDocumento');}","",h.getImgHand("Valida il Documento",_COMPLETO));
		              }
		              //vu.disconnectClose();
		           }
		       }
		       catch (Exception e) 
		       {
		    	 //vu.disconnectClose();
		    	 throw e;
		       }
	   }  
	   
	   /**
		 * Setta lo stato del Documento a CANCELLATO.
		 * 
		 */
	   private void changeStatoCancellato() throws Exception
	   {
	           String url;
		   	   try
	           {
	             vu.connect();
	             if(!verificaCompetenzaDocumento(idDoc,Global.ABIL_CANC))
	               sCancellato="";                        
	             else
	             {	 
	            	  if (Prov.equals("Q"))
					    url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProv;
		              else
					    url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";    
	            	  
	            	 sCancellato=h.getAncore("#","if (confirm('Sei sicuro di voler eliminare il Documento?\\t')==true) popup_hidden('AnnullaDoc.do?idDocumento="+idDoc+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+spulsanti+"&GDC_Link="+URLEncoder.encode(url)+"');","",h.getImgHand("Elimina il Documento",_ANNULLA));
	             }
	             //vu.disconnectClose();
	           }
	           catch (Exception e) 
	           {
	        	 //vu.disconnectClose();
	        	 throw e;
	           }
	   }  
	   
	   /**
		 * Setta lo stato del Documento a ANNULLATO.
		 * 
		 */
	   private void changeStatoAnnullato() throws Exception
	   {
	           String url;
		       try
	           {
	            vu.connect();
	            if(!verificaCompetenzaDocumento(idDoc,Global.ABIL_MODI))
	              sAnnullato="";                        
	            else
	            {
	              if ((stato.equals("BO")) || (stato.equals("CO")))
	              {	  
	                if (Prov.equals("Q"))
					  url = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProv;
		            else
					  url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";                    
		            
	               sAnnullato=h.getAncore("#","if (confirm('Sei sicuro di voler ANNULLARE il Documento?\\t\\t\\nAttenzione il Documento non è piu modificabile!\\t\\t\\n')==true) {popup('DocumentoView.do?idDoc="+idDoc+"&rw=R&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez="+idCartProv+"&idQueryProveninez="+idQueryProv+"&Provenienza="+Prov+"&stato="+spulsanti+stato+"&GDC_Link="+URLEncoder.encode(url)+"&conferma=ANNULLATO&MVPG=ServletModulisticaDocumento');}","",h.getImgHand("Annulla il Documento",_ANNULLATO));
	              }
	            } 
	            //vu.disconnectClose();
	           }
	           catch (Exception e) 
	           {
	        	 //vu.disconnectClose();
	        	 throw e;
	           }
	   }  
	   
	   /**
		 * Verifica la competenza "tipoCompetenza" sul Documento.
		 * 
		 */
	   private boolean verificaCompetenzaDocumento(String idOggetto,String tipoCompetenza) throws Exception
	   {
	           try
	           {
	        	   Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC,idOggetto,tipoCompetenza); 
	        	   UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(),vu.getPwd(),  vu.getUser(), vu);
	   	           if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,abilitazione)  == 1 )               
	                 return true;
	   	           return false;
	           }
	           catch (Exception e) {        
	        	   throw e;
	           }                  
	   }
	   
	   /***************************************************************************
		* OPERAZIONI DEI BOTTONI
		**************************************************************************/
	   
	   /**
		 * Cambia lo stato del Documento.
		 * 
		 */
	   private String changeStatoAnnullatoOnClick() throws Exception
	   {
	           String conferma="";
	           try
	           {
	             vu.connect();
	             AggiornaDocumento ad = new AggiornaDocumento(idDoc,vu);
	             if (!ad.salvaDocumentoAnnullato()) 
	               conferma=h.MsgBoxPagePop("NOT_ANNULLATO");
	             else
	               conferma="OK";
	             //vu.disconnectClose();

	           }
	           catch (Exception e) 
	           {
	        	//vu.disconnectClose();
	        	throw e;
	           }
		       return conferma;
	   }  
	   
	   private String changeStatoCompletoOnClick() throws Exception
	   {
	           String conferma="";
	           try
	           {
	             vu.connect();
	             AggiornaDocumento ad = new AggiornaDocumento(idDoc,vu);
	             if (!ad.salvaDocumentoCompleto()) 
	              conferma=h.MsgBoxPagePop("NOT_COMPLETO");
	             else
	              conferma="OK";
	             //vu.disconnectClose();

	           }
	           catch (Exception e) 
	           {
	        	 //vu.disconnectClose();
	        	 throw e;
	           }
		       return conferma;
	   } 
	   
	   public String _AnnullatoDoc_OnClick() throws Exception 
	   {
	          String conferma="";
	          try
	          {
	            conferma=changeStatoAnnullatoOnClick();
	          }
	          catch (Exception e) 
	          {
	        	throw e;
	          }
			  finally {
				  if(conferma.equals("OK"))
					  _finally(true);
				  else
					  _finally(false);
			  }
		      return conferma;
	   }
	   
	   public String _CompletoDoc_OnClick() throws Exception 
	   {
		      String conferma="";
		      try
		      {
		         conferma=changeStatoCompletoOnClick();
		      }
		      catch (Exception e) 
		      {
		        throw e;
		      }
			  finally {
				  if(conferma.equals("OK"))
					  _finally(true);
				  else
					  _finally(false);
			  }
		      return conferma;
	   }
	   
	   /**
		 * Elenca la lista di cartelle che contengono il documento idDoc.
		 * 
		 */
	   public String _getElencoCartelle() throws Exception
	   {
	          String sql,elenco="";
	          String nome="";
	          
	          try
	          {			    
			   sql=" select l.id_cartella ID_OGGETTO,c.nome NOME, decode(substr(F_Path_Folder(c.id_cartella,'',:USER),instr(F_Path_Folder(c.id_cartella,'',:USER),'>')+1,1),'S','1','2') WORKSPACE ";
	           sql+="from links l,cartelle c, documenti d ";
	           sql+="where d.id_documento = :IDDOC and d.id_documento = l.id_oggetto ";
	           sql+="and l.tipo_oggetto ='D' and l.id_cartella = c.id_cartella and nvl(c.stato,'BO')<>'CA' ";
	           dbOp.setStatement(sql);
	           dbOp.setParameter(":USER",user);
	           dbOp.setParameter(":IDDOC",idDoc);
			   dbOp.execute();
			   ResultSet rs = dbOp.getRstSet();
			   while (rs.next() )
	           {
	             if(rs.getString("NOME").equals("WRKSPUTENTE"))
	                 nome="Utente";
	             else
	              if(rs.getString("NOME").equals("WRKSPSISTEMA"))
	                nome="Sistema";
	              else
	                nome=rs.getString("NOME");
	             elenco+=h.getAncore("text-decoration : none","","refreshLinkCartella("+rs.getString("ID_OGGETTO")+","+rs.getString("WORKSPACE")+");","",h.getImg(_CARTELLAGDC)+h.getNbsp()+nome+h.getBR());
	           }

	          }
	          catch ( SQLException e ) {
	            throw e;
	          }
			  finally {
				  _finally();
			  }
	          return elenco;
	   }
	   
	   /***************************************************************************
		* GESTIONE RIFERIMENTI DOCUMENTO
		**************************************************************************/
	
	   
	   /**
		 * Costruzione del nome del documento.
		 * 
		 * @return String nome
		 */
	   public String getNomeDocRif() throws Exception
	   {
		      String nome="";
	          String viewDoc="";
	          
	          log.log_info("Inizio Costruzione nome del documento - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
		        
	          
	          if( (tipo_rif!=null) && (idDoc!=null))
	          {
	        	try
		        {
		          if(tipoProv.equals("D"))
		          {
		        	 String jdms_link = retriveParametro("JDMS_LINK"); 
		        	  
		        	 if(jdms_link.equals("S"))
		        	  viewDoc=visualizzaJdmslinkDocumento();	 
		        	 else
		        	  viewDoc=visualizzaLinkDocumento();
		        	  
		             nome=h.getTable("100%",h.getTR(h.getTD("top","3%",viewDoc)+h.getTD("","5%"," ")+h.getTD("","65%",getRigaMonoRecord(idDocRif,"D","",req))+h.getTD("","27%",tipo_rif)));
			      }
		          else
		          {
		             nome=h.getTable("100%",h.getTR(h.getTD("","73%",nomeCart)+h.getTD("","27%",tipo_rif)));
			      } 

		        }
		        catch (Exception e) 
		        {
		            throw e;
		        }
				finally {
					_finally();
				}
			}
	        
	        log.log_info("Fine Costruzione nome del documento nome:: "+nome+" - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");  
	        return nome;
	   }
	   
	   
	   /**
	    * Recupero parametro dalla tabella PARAMETRI
	    * del tipo_moedllo=@DMSERVER@
	    * 
	    * @param 			nome parametro
	    * @return String 	valore
	    * 
	 	 */
	  private String retriveParametro(String parametro) throws Exception
	  {
	          String sql="",rstPar=null;
	          log.log_info("Inizio Recupero Parametro: "+parametro+"  - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");

	          try
	          {
	             ResultSet rs=null;
	             sql=" SELECT VALORE FROM PARAMETRI ";
	             sql+=" WHERE CODICE = :PARAMETRO ";
	             sql+=" AND TIPO_MODELLO='@DMSERVER@'";
	             log.log_info("PATH_FOLDER - SQL - "+sql);
	             dbOp.setStatement(sql);
	             dbOp.setParameter(":PARAMETRO",parametro);
	             dbOp.execute();
	      	     rs=dbOp.getRstSet();
	             if (rs.next()) 
	               rstPar=rs.getString(1);
	             else
	               rstPar="N";
		 	   }
		 	   catch ( SQLException e ) {
		 	      log.log_error("Recupero Parametro: - SQL: "+sql);
			      throw e;
		 	   }    
	          
	          log.log_info("Fine Recupero Parametro: - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	         return  rstPar;   
	  } 
	   
	   
	   /**
		 * Costruzione del monorecord di una Cartella.
		 * 
		 * @return String monorecord
		 */
	   public String getNomeCartella() throws Exception
	   {
		      String mr="";
	        	
        	  try
	          {
        	    mr=h.getTable("100%",h.getTR(h.getTD("","","<a title=\"Collegamento a Cartella\" style=\"text-decoration : none\" href=\"\" "+onclick+" border=\"\"><img border=\"0\" src=\""+_CARTELLAGDC+"\" />"+getRigaMonoRecord(idDoc,tipoOggetto,nome,req)+"</a> inserito da "+ute_agg+" il "+data_agg)));
		      }
	         catch (Exception e) 
	         {
	           throw e;
	         }
        	  finally {
        	  	_finally();
			  }
			 return mr;
	   }
	   
	   
	   /**
		 * Determina il tipo immaggine appropiata allo
		 * stato del documento.
		 * 
		 * @return String src
		 */
	   private String decodeStato(String idOggetto)
	   {     
		       String decode="";
		       if (idOggetto.equals("CO"))
		       {
		         decode="src=\""+_COMPLETO+"\"";
		         return decode;
		       }
		       if (idOggetto.equals("AN"))
		       {
		         decode="src=\""+_ANNULLATO+"\"";
		         return decode;
		       }
		       if (idOggetto.equals("BO"))
		       {
		         decode="src=\""+_DOT+"\"";
		         return decode;
		       }
		       return decode;     
	   }  

	   private String visualizzaJdmslinkDocumento() throws Exception
	   {
	 	  	  String linkDoc = "";
		      String sql="";

	 	  	  log.log_info("Inizio visualizzaJdmslinkDocumento() - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");

	 	  	  try
	 	      {
	 	    	sql="  SELECT distinct J.ID_TIPODOC,TAG, J.URL, J.ICONA, J.TOOLTIP,I.NOME ";
	 	    	sql+=" FROM JDMS_LINK J, DOCUMENTI D, ICONE I ";
	 	    	sql+=" WHERE D.ID_DOCUMENTO = :IDDOCRIF AND ";
	 	    	sql+=" D.ID_TIPODOC = J.ID_TIPODOC";
	 	    	sql+=" AND J.ICONA=I.ICONA(+) ";
	 	    	sql+=" AND J.TAG = 5 ";
	 	    	
	 	    	log.log_info("Recupero relativo jdms_link SQL::"+sql+" - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	 	    	
	 	    	dbOp.setStatement(sql);
		 	  	dbOp.setParameter(":IDDOCRIF",idDocRif);
	 	  	    dbOp.execute();
	 	  	    ResultSet rs = dbOp.getRstSet();
	 			String idTipoDoc="",tag="",url="",icona="",tooltip="",nomeIcona="",gdc_link="";
	 			
	 	  	    if(rs.next()) 
	 			{
	 	  	    	idTipoDoc=rs.getString(1);
	 	  	    	tag=rs.getString(2);
	 	  	    	url=rs.getString(3);
	 	  	    	icona=rs.getString(4);
	 	  	    	tooltip=rs.getString(5);
	 	  	    	nomeIcona=rs.getString(6);
	 	  	    	String area=(new DocUtil(vu)).getAreaByIdDocumento(""+idDocRif);
			        String cm=(new DocUtil(vu)).getModelloByIdDocumento(""+idDocRif);
			        String cr=(new DocUtil(vu)).getCrByIdDocumento(""+idDocRif);
			        
			        idQueryProv = xss.encodeHtmlAttribute("idQueryProveninez",req.getParameter("idQueryProveninez").toString());
			        idCartProv = xss.encodeHtmlAttribute("idCartProveninez",req.getParameter("idCartProveninez").toString());
			    	JDMSLinkParser p = new JDMSLinkParser(req,idDocRif,tipoOggetto,area,cm,cr,idDocRif,idCartProv,idQueryProv,"R","ServletModulisticaDocumento",stato,Prov,gdc_link);
	 	 	        String urlpagina = p.bindingDeiParametri(url);
	 	 	        linkDoc=h.getAncore("text-decoration : none","#",urlpagina,"",h.getImgM("Visualizza Documento",decodeStato(stato)));
			    }
	 	  	    else
	 	  	     linkDoc = visualizzaLinkDocumento();	

	 	        }
	 	        catch ( SQLException e ) {
	 	          throw e;
	 	        }    
	 	  	  log.log_info("Fine visualizzaJdmslinkDocumento() - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	 	  	  return linkDoc;
	   }

	   /**
		 * Link alla Visualizzazione del Documento.
		 * 
		 * @return String url
		 */
	   private String visualizzaLinkDocumento() throws Exception
	   {     
		       String decode;
		       String url_servlet="../common/ServletModulisticaDocumento.do?";     
		       String url = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";
		       String area=(new DocUtil(vu)).getAreaByIdDocumento(""+idDocRif);
		       String cm=(new DocUtil(vu)).getModelloByIdDocumento(""+idDocRif);
		       String cr=(new DocUtil(vu)).getCrByIdDocumento(""+idDocRif);
		       decode=h.getAncore("text-decoration : none","#",h.DocumentoViewRifPop("",idDocRif,"R",cm,area,cr,"","-1","",stato,url),"",h.getImgM("Visualizza Documento",decodeStato(stato)));
		       return decode;     
	   } 
	   
	   /**
		 * Link diretto alla Cartella.
		 * 
		 * @return String url
		 */
	   private String decodeViewCart() throws Exception
	   {     
		       String decode;
		       if(wrkspRIF.equals(wrksp))
		         decode=h.getAncore("text-decoration : none","#","refreshLinkCartella("+idCartella+","+wrksp+",null,null);","",h.getImgM("Collegamento Cartella","src=\""+_CART+"\"")+" <b>"+nomeCart+"</b>");
		       else
		         decode=h.getAncore("text-decoration : none","#","","",h.getImgM("Collegamento Cartella","src=\""+_CART+"\"")+" <b>"+nomeCart+"</b>");
		       return decode;     
	   } 
	   
	   /**
		 * Costruzione del monorecord di un oggetto
		 * 
		 * @return String monorecord
		 */
	   private String getRigaMonoRecord(String idDoc,String tipoOggetto,String nomeOggetto,HttpServletRequest req) throws Exception
	   {
	           String riga="Documento n. "+idDoc;;
	           Environment env = new Environment(user,user,"MODULISTICA","ADS",null,dbOp.getConn());
	           try
	           {
	        	 MonoRecordIQuery m = new MonoRecordIQuery(idDoc,tipoOggetto,nomeOggetto,env,req);
	        	 /** Monorecord m = new Monorecord(idDoc,"D","",env);*/
	        	 riga=m.creaRiga();
	        	 if(riga==null)
	        	  riga="Documento n. "+idDoc; 
	           }
	           catch (Exception e) {     
	        	   throw e;
	           }
	           return riga;
	   }
	   
	   /**
		 * costruzione della lista di documenti per
		 * la gestione dell'eliminazione dei i riferimenti.
		 * 
		 * @return boolean lista
		 */
	   private boolean buildListToDeleteRif() 
	   {
		   	  String[] v=null;
		   	  String slink="";
		  
		   	  if((list!=null) && (list.size()!=0))
		   	  {	  
		   	    for(int i=0;i<list.size();i++)
		   	    {
				 v=list.get(i).toString().split(",");
				 String id="",idR="",tr="",cb="",idCartRIF="",idCart="";
				
				 for(int j=0;j<v.length;j++)
		  		 {	 
				    if(v[j].indexOf("TIPO_RELAZIONE")!=-1)
				      tr=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
					    
				    if(v[j].indexOf("ID_DOCUMENTO_RIF")!=-1)
				      id=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
				    
				    if(v[j].indexOf("CheckBox_Delete")!=-1)
					  cb=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
				    
				    if(v[j].indexOf("idCartellaRif")!=-1)
				    {
				    	idCartRIF=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
				        if(idCartRIF.indexOf("}")!=-1)
				        	idCartRIF=idCartRIF.substring(0,idCartRIF.length()-1);
				    }
				    
				    if(v[j].indexOf("idCart=")!=-1)
				    {
				    	idCart=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
				        if(idCart.indexOf("}")!=-1)
				          idCart=idCart.substring(0,idCart.length()-1);
				    }
				    
				    if(v[j].indexOf("ID_DOCUMENTO")!=-1)
				      idR=v[j].substring(v[j].indexOf("=")+1,v[j].length());	
				 } 
				 
				 if(cb.equals("Y"))
				 {
				  if(idCartella!=null && idCartella!="" && idCartella!="null" && idCart!="")
				  {
					  slink+=idCart+"#"+tr+"#"+idCartRIF+"@";   
				  }
				  else
				  {
					if(idDoc.equals(id))
					 slink+=idR+"#"+tr+"#"+id+"@";
					else
					 slink+=idDoc+"#"+tr+"#"+idCart+"@";
				  }  
				 }
			   }
		   	   
		   	   if(!(slink.equals(""))) 
		    	vDocRif =Global.Split(slink,"@");
		   	   return true;
		   	  }  
		   	  return false;
	   }
	   
	   /**
		 * Cancellazione del Riferimento.
		 * 
		 */
	   public void deleteRif() throws Exception
	   {
	   	      boolean commit=false;
		   	  if((buildListToDeleteRif())&& (vDocRif!=null))
		   	  {
		   		try
		   		{
	        	  if(idCartella!=null && idCartella!="")
	        	  {
	        		  for(int i=0;i<vDocRif.length;i++)
	            	  {
	            		  String [] s=vDocRif[i].split("#");
	            		  String cart=s[0];
	            		  String tr=s[1];
	            		  String cartrif= s[2];
	         		      ICartella c = null; 
	        		      c = new ICartella(cart);
	        		      c.initVarEnv(vu.getUser(),vu.getPwd(), dbOp.getConn());
	        		      c.setDeleteRiferimento(cartrif,tr);
	        			  c.update();
	        	      }
	          	  	  commit= true;
	        	 }
	        	 else
	        	 {
	        		for(int i=0;i<vDocRif.length;i++)
	          		{
	          		  String [] s=vDocRif[i].split("#");
	          		  String idDocumento=s[0];
	          		  String tr=s[1];
	          		  String idrif= s[2];
	          		  Profilo p = null; 
	      		      p = new Profilo(idDocumento);
	      		      p.initVarEnv(vu.getUser(),vu.getPwd(), dbOp.getConn());
	      		      p.setDeleteRiferimento(idrif,tr);
	      			  if (!p.salva().booleanValue())
	      		    	throw new Exception("CCS_DocumentoView::deleteRif() - Cancellazione Riferimento non eseguita");
	      		    }
					commit= true;
	        	 }
		   		}
		   		catch (Exception e) 
		   		{
					commit= false;
		   			throw e;
		   		}
		   		finally {
		   			_finally(commit);
				}
		   	  }
	   }  
	   
	   /**
		 * Inserimento del Riferimento.
		 * 
		 * @return String lista di riferimenti inseriti
		 * 
		 */
	   public String insertRif() throws Exception
	   {
		      String s="";
		      boolean commit=false;
		      try {

				  if ((listaID != "") && (listaID != null))
					  vlistaID = new Vector(Arrays.asList(listaID.split("@")));

				  if ((tipo_rif == null) || (tipo_rif == ""))
					  tipo_rif = "RIF";

				  if ((tipo_rif != "") && (vDocRif.length != 0) && (idDoc != "")) {

				  	  if(idDoc.indexOf("D")!=-1) {
						  idDoc = idDoc.substring(1, idDoc.length());
					  }

					  for (int i = 0; i < vDocRif.length; i++) {
						  String srif = vDocRif[i].substring(vDocRif[i].indexOf("#") + 1, vDocRif[i].length());
						  if ((srif != "") && (srif != null)) {
							  Profilo p = new Profilo(idDoc);
							  p.initVarEnv(vu.getUser(),vu.getPwd(), dbOp.getConn());
							  p.settaRiferimento(srif,tipo_rif);
							  if (!p.salva().booleanValue())
								 throw new Exception("CCS_DocumentoView::insertRif() - Inserimento Riferimento non eseguita");
							  buildListaID(srif);
						  }
					  }
					  if (vlistaID != null) {
						  for (int i = 0; i < vlistaID.size(); i++) {
							  s += vlistaID.get(i).toString() + "@";
						  }
					  }
				  }
				  else{
					  s += listaID;
				  }

				  commit= true;
			  }
			  catch (Exception e)
			  {
				  commit= false;
				  throw e;
			  }
			  finally {
				  _finally(commit);
			  }
		      return s;
	   	}  
	   
	   /**
		 * Costruzione della listaID.
		 * 
		 */
	   private void buildListaID(String docrif) 
	   {
		   	   if(vlistaID!=null)
		   	   {	 
		   		 for(int j=0;j<vlistaID.size();j++)
		   		 {
		   			if(vlistaID.get(j).equals(docrif))
		   			  vlistaID.remove(j);
		   		 }
		   	   }  
	   }

	   /**
		 * Visualizzazione della listBox contenente l'elenco dei documenti.
		 * 
		 * @return String HTML listbox 
		 */	
	   public String getListBox() throws Exception
	   {
	          String listBox="",nome="";
	          int i=0;

	          listBox+="<select class=\"AFCSelect\" id=\"listInput\" ondblclick=\"passAcrossLB(document.getElementById('listInput'),document.getElementById('listOutput'));\" style=\"WIDTH: 250px; HEIGHT: 107px\" multiple name=\"listInput\">";
	          try
	          {
		        if(listaID!=null)
		        {
		          StringTokenizer st = new StringTokenizer(listaID,"@");
		          while (st.hasMoreTokens())
		          {
		            String s=st.nextToken();
		            if(s.indexOf("D")!=-1)
		             s=s.substring(1,s.length());	
		            if(!s.equals(idDoc))
		            {
		             if(!(checkExistRiferimento(s))) 
		             {	
		            	 nome=getRigaMonoRecord(s,"D","",req); 
			             if(i==0)
			              listBox+="<option value=\""+s+"\" selected>"+nome+"</option>";
			             else
			               listBox+="<option value=\""+s+"\">"+nome+"</option>";
			             i++;
			             nome="";
		             }
		            }
		          }
		         }
		         listBox+="</select>";
		          
	          }
	          catch (Exception e) {
	        	throw e;
	          }
	          finally {
	          	_finally();
			  }
	          return listBox;
	   }   
	   
	   /**
		 * Controllo esistenza dei riferimento su un Docuemnto.
		 * 
		 */	
	   private boolean checkExistRiferimento(String rif) throws Exception
	   {
		       String sql="";
		       boolean c=false;

		       try
		       {	
		        if(tipo_rif==null)
				 tipo_rif="RIF";
			 		 
		        sql=" SELECT ID_DOCUMENTO_RIF FROM RIFERIMENTI";
			    sql+=" WHERE ID_DOCUMENTO = :IDDOCUMENTO AND ID_DOCUMENTO_RIF = :IDDOCUMENTORIF";
			    sql+=" AND TIPO_RELAZIONE= :tipo_rif";
			    			   
	            dbOp.setStatement(sql);
	            dbOp.setParameter(":IDDOCUMENTO",idDoc);
	            dbOp.setParameter(":IDDOCUMENTORIF",rif);
	            dbOp.setParameter(":tipo_rif",tipo_rif);
	            dbOp.execute();
	            ResultSet rs = dbOp.getRstSet();
	            if(rs.next())
	             c=true;
	   	       }
		       catch ( SQLException e ) {
		    	  throw e;
		       }  
	           return c;
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

		private void _finally(boolean commit) throws Exception {
			try
			{
				if (CCS_common.dataSource.equals("")){
					try{vu.disconnectClose();}catch(Exception ei){}
				}
				CCS_common.closeConnection(dbOp,commit);
			}
			catch (Exception e) {
				throw e;
			}
		}

}
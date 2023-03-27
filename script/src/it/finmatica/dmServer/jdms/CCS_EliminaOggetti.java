package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.jfc.dbUtil.*;
import java.util.*;

/**
 * Gestione della cancellazione degli oggetti.
 * Classe di servizio per la gestione del Client
*/
     
public class CCS_EliminaOggetti 
{
	   /**
	    * Variabili private
	   */	 
	   private String listaId;
	   CCS_Common CCS_common;
	   CCS_HTML h;
	   private IDbOperationSQL dbOp;
	   private Environment vu;  
	   private Vector<String> vCart  = new Vector<String>(); 
	   private Vector<String> vQuery = new Vector<String>(); 
	   private Vector<String> vDoc   = new Vector<String>(); 
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato per eliminare oggetti.
		 * 
		 */
	   public CCS_EliminaOggetti(String newlistaId,String newidQuery,String newProv,String newidCartella,CCS_Common newCommon) throws Exception
	   {
		      listaId=newlistaId;
		      CCS_common=newCommon;
		      log= new DMServer4j(CCS_EliminaOggetti.class,CCS_common); 
		      h = new CCS_HTML();
		      dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		      vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
	   }
	   
	   /**
		 * Eliminazione della lista di oggetti.
		 * 
		 */
	   public boolean _afterInitialize() throws Exception
	   {
		      boolean elimina=false;
		         
		      try
		      {
		        if(!listaId.equals(""))
		        {
		          elencoOggetto();
		          elimina=eliminaListaOggetti();
		          elimina=true;
		        }
		        CCS_common.closeConnection(dbOp);
		        return elimina;
		      }
		      catch (Exception e) 
		      {
		        CCS_common.closeConnection(dbOp); 
		        throw e;
		        //throw new Exception("CCS_EliminaOggetti::_afterInitialize\n"+e.getMessage());
		      }
	   }
	   
	   /**
		 * Costruzione dei vettori di oggetti da eliminare.
		 * 
		 */
	   private void elencoOggetto() throws Exception
	   {
		   	   StringTokenizer st = new StringTokenizer(listaId,"@");
		   	   String id="",tipoObj="";
		   	   int countC=0,countQ=0,countD=0;
          
		   	   while (st.hasMoreTokens())
		   	   {
		   		   String s=st.nextToken();
		   		   tipoObj=s.substring(0,1);
		           id=s.substring(1,s.length());
		           if(tipoObj.equals("C"))
		           {
		              vCart.add(countC,id);
		              countC++;
		           }
		           else
		            if(tipoObj.equals("Q"))
		            {
		              vQuery.add(countQ,id);
		              countQ++;
		            }
		            else
		            {
		              vDoc.add(countD,id);
		              countD++; 
		            }    
               }
	   }
	   
	   /**
		 * Per ciascun oggetto della lista verifica se
		 * possiede le competenze di eliminazione
		 * se true elimina l'oggetto, altrimenti nulla.
		 * 
		 */
	   private boolean eliminaListaOggetti() throws Exception
	   {
	           boolean elimina=true;
	           
	           for(int j=0;j<vCart.size();j++)
	           {
	        	 eliminaHandler((String)vCart.get(j),"C");
	           }
	           
	           for(int j=0;j<vQuery.size();j++)
	           {
	        	 eliminaHandler((String)vQuery.get(j),"Q");
	           }
	          
	           for(int j=0;j<vDoc.size();j++)
	           {
	        	 eliminaHandler((String)vDoc.get(j),"D");
	           }
	           return elimina; 
	   }
	   
	   private boolean eliminaHandler(String idOggetto,String tipoObj) throws Exception
	   {
	           boolean sElimina=false;
	           String tipoAbilitazione="";
	           try
	           {
	             /** Indica il tipo di Abilitazione */
	             if (tipoObj.equals("C"))
	               tipoAbilitazione=Global.ABIL_CARTELLA;
	             else
	              if (tipoObj.equals("Q"))
	                tipoAbilitazione=Global.ABIL_QUERY;
	              else
	                tipoAbilitazione=Global.ABIL_DOC;
	             
	             vu.connect();
	             /** Verifica delle compentenze di Eliminazione */
	             if(!verificaCompetenzaOggetto(idOggetto,Global.ABIL_CANC,tipoAbilitazione))
	              {
	                sElimina=false;                        
	                vu.disconnectClose();
	                return sElimina;
	              } 
	              else
	              { 
	                /** Eliminazione del oggetto Documento */
	                if (tipoObj.equals("D"))
	                {
	                 CCS_AnnullaDoc doc=new CCS_AnnullaDoc(idOggetto,CCS_common);
	                 doc._afterInitialize_setStatoToCA(); 
	                }
	                else /** Eliminazione dell'oggetto Query o Cartella */
	                {
	                 CCS_VistaCartDel obj=new CCS_VistaCartDel(idOggetto,tipoObj,CCS_common);
	                 obj._afterInitialize(); 
	                }
	                sElimina=true; 
	                vu.disconnectClose();
	                return sElimina;
	              }
	          }
	          catch (Exception e) 
	          {
	           vu.disconnectClose();
	           log.log_error("CCS_EliminaOggetti::eliminaHandler() - idOggetto:"+idOggetto+" - tipoOggetto:"+tipoObj);
	           throw e;
	           //throw new Exception("CCS_EliminaOggetti::eliminaHandler\n"+e.getMessage());
	         }
	   }  
	   
	   /**
		 * Verifica la competenza "tipoCompetenza" sull'oggetto.
		 * 
		 */
	   private boolean verificaCompetenzaOggetto(String idOggetto,String tipoCompetenza,String tipoAbilitazione) throws Exception
	   {
	           String id=idOggetto;
		       try
		       {
		    	  if (tipoAbilitazione.equals(Global.ABIL_CARTELLA)) 
		    		id = (new DocUtil(vu)).getIdViewCartellaByIdCartella(idOggetto);
		    	  Abilitazioni abilitazione = new Abilitazioni(tipoAbilitazione,id,tipoCompetenza); 
		    	  UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(),vu.getPwd(),  vu.getUser(), vu);
		    	  if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,abilitazione)  == 1 ) {               
	               return true;
		    	  }
	              return false;
		       }
		       catch (Exception e) {     
		    	 log.log_error("CCS_EliminaOggetti::verificaCompetenzaOggetto() - idOggetto:"+idOggetto+" - tipoCompetenza:"+tipoCompetenza+" - tipoAbilitazione:"+tipoAbilitazione);
		         throw e;
		         //throw new Exception("CCS_EliminaOggetti::verificaCompetenzaOggetto\n"+e.getMessage());
		       }                  
	   }
 }
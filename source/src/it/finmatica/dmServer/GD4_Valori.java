package it.finmatica.dmServer;

/*
 * GESTIONE DEGLI VALORI
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Vector;
import java.text.SimpleDateFormat;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.utility.DateSqlUtility;
import it.finmatica.jfc.utility.DateUtility;
import it.finmatica.dmServer.dbEngine.struct.DbOpSetParameterBuffer;
import it.finmatica.dmServer.util.*;
  
public class GD4_Valori extends A_Valori
{    
  // variabili private
  private A_Campi_Documento campo;
  private DMActivity_Log dmALog;
  private String isInsertOrUpdate;
  
  private IDbOperationSQL dbOpSqlBatchInsert;
  private IDbOperationSQL dbOpSqlBatchUpdate;
  
  private ElapsedTime elpsTime;
  
  private StringBuffer sUpdateHorizontal = new StringBuffer("");
  private int indexParHorizontalTable;
  private DbOpSetParameterBuffer dbOpSpB;
  
  private  ValoreParametrico val;
  
  private boolean isLastValue=false;
  
  //Gestione del CLOB > 4000 caratteri
  private String sValore4000=null;  
  private String sIdValore4000=null;
  
  private String valoreFull=null;
  

 // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
    
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public GD4_Valori()
  {
            
  }

  /*
   * METHOD:      inizializzaDati(Object, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  public void inizializzaDati(Object vUtente) throws Exception
  {
         this.inizializzaDati((Environment) vUtente);
  }

    /*
   * METHOD:      inizializzaDati(Object, Environment)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  private void inizializzaDati(Environment vUtente) throws Exception
  {         
         this.variabiliUtente = vUtente;
         this.modificato = "N";
   
         elpsTime = new ElapsedTime("GD4_VALORI",vUtente);
         // Crea la libreria del DM da utilizzare
         try {
                campo = (A_Campi_Documento)Class.forName(vUtente.Global.PACKAGE + "." + vUtente.Global.DM + 
                                              "_"+ vUtente.Global.CAMPI_DOCUMENTO).newInstance();
          }
           catch (Exception e) {
               throw new Exception("GD4_Valori:inizializzaDati() non riesco a creare l'oggetto di Classe: " + 
                                   vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_"+ vUtente.Global.CAMPI_DOCUMENTO);
          }       
  }
  
  // ***************** METODI DI GESTIONE VALORI ***************** //
 
  /*
   * METHOD:      retrieve() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un valore dal Database
   *
   * RETURN:      void
  */
  public void retrieve() throws Exception 
  {
         IDbOperationSQL dbOp = null;

         if (this.getIdValore().equals("0")) return;
        
         try {
           StringBuffer sStm = new StringBuffer();

           dbOp = variabiliUtente.getDbOp();
   
           sStm.append("select id_campo,valore_clob,valore_numero,to_char(valore_data,f_formato_data(id_campo)) from valori");
           sStm.append(" where id_valore = " + this.getIdValore());

           dbOp.setStatement(sStm.toString());

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();
           if (rst.next()) {
              
              this.setValore(Global.selezioneValore(rst.getString(2),rst.getString(3),rst.getString(4)));
              this.setModificato("N");

             /* try {
                this.inizializzaDati(dbOpSql, variabiliUtente);
              }
              catch (Exception e) {
                CloseAllConnection(dbOp);
                throw new Exception("GD4_Valori::retrieve() inizializzaDati\n" + e.getMessage());
              }
              */
              this.getCampo().setIdCampo(rst.getString(1));

              try {
                this.getCampo().retrieve();
              }
              catch (Exception e) {                
                throw new Exception("GD4_Valori::retrieve() retrieve campo\n" + e.getMessage());
              }                             

              try {
                Global.leggiClob(dbOp,"valore_clob");
              }
              catch (Exception e) {                
                throw new Exception("GD4_Valori::retrieve() leggiClob\n" + e.getMessage());
              }

            }
           else {              
              throw new Exception("Select fallita per idValore: " +  this.getIdValore());                            
           }
         }
         catch (Exception e) {
               throw new Exception("GD4_Valori::retrieve() " + e.getMessage());
         }

  }

  /*
   * METHOD:      retrieveFromCampi(String, String) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un valore dal Database a partire dal campo e documento
   *
   * RETURN:      void
  */
  public void retrieveFromCampi(String idCampo, String idDocumento) throws Exception 
  {
        IDbOperationSQL dbOp = null;
        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = variabiliUtente.getDbOp();
           
           sStm.append("select id_valore,valore_clob,valore_numero,to_char(valore_data',f_formato_data(id_campo)) from valori");
           sStm.append(" where id_campo = " + idCampo);
           sStm.append("   and id_documento = " + idDocumento);

           dbOp.setStatement(sStm.toString());

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {
              this.setValore(Global.selezioneValore(rst.getString(2),rst.getString(3),rst.getString(4)));

              try {
                this.inizializzaDati(variabiliUtente);
              }
              catch (Exception e) {                
                throw new Exception("GD4_Valori::retrieveFromCampi() inizializzaDati\n" + e.getMessage());
              }
              
              this.getCampo().setIdCampo(idCampo);
              this.setIdValore(rst.getString(1));

              try {
                this.getCampo().retrieve();
              }
              catch (Exception e) {                
                throw new Exception("GD4_Valori::retrieveFromCampi() retrieve campo\n" + e.getMessage());
              }
           }
           else {              
              throw new Exception("Select fallita per idValore: " + this.getIdValore());                              
           }
         }
         catch (Exception e) {
               throw new Exception("GD4_Valori::retrieveFromCampi() " + e.getMessage());
         }

  }
   
  /*
   * METHOD:      insert(Object) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce nella tabella valori un valore di tipo
   *              GD4_Valori.
   *              Viene passato l'id del documento di appartenenza
   *              Viene controllato anche il tipo di campo per
   *              scegliere la colonna su cui registrare tra:
   *              valore_clob, valore_numero, valore_data
   *              ATTENZIONE il valore_clob viene inserito mediante update
   *              il codice è fatto in modo da ignorare l'inserimento dei
   *              clob rimandandolo all'update
   *
   * RETURN:      boolean
  */
  public boolean insert(Object idDocumento) throws Exception 
  {     
	     sValore4000=null;
	  
	     if (dbOpSqlBatchInsert!=null) {
	    	 if (existsCampo(idDocumento)) return update(idDocumento);
	     }	    	                                              
         
         isInsertOrUpdate="INSERT";
         
         String               sValore           = null;
         java.sql.Date        sValoreData       = null;
         java.sql.Timestamp   sValoreDateTime   = null;
         java.math.BigDecimal sValoreDouble     = null;
         
         String sType="";
         String sValoreDato = "";
         String sFormatoData="dd/mm/yyyy";
         
         valoreFull=null;
         
         if ( this.getValore() instanceof java.sql.Date) {
        	 sValoreData=(java.sql.Date)this.getValore();        	 
                      
        	 if (this.getValore()!=null) {
        		 SimpleDateFormat  formatter = new SimpleDateFormat("dd/MM/yyyy");
        		 valoreFull=formatter.format(sValoreData);
        	 }
         }                      
         else if ( this.getValore() instanceof java.sql.Timestamp) {
        	 sValoreDateTime=(java.sql.Timestamp)this.getValore();
        	 
        	 if (this.getValore()!=null) {
        		 java.sql.Date jsqlD = new java.sql.Date(sValoreDateTime.getTime());
        		 SimpleDateFormat  formatter = new SimpleDateFormat("dd/MM/yyyy");
        		 valoreFull=formatter.format(jsqlD);
        	 }
         }
         else if (this.getValore() instanceof java.math.BigDecimal) {
        	 sValoreDouble=(java.math.BigDecimal)this.getValore();
        	 if (this.getValore()!=null) valoreFull=this.getValore().toString().replace(".",",");
         }                      
         else {
        	 sValore = (String)(this.getValore());
        	
        	 if ((""+sValore).equals("") && dbOpSqlBatchInsert==null) return true; 
        	 
        	 if (this.getValore()!=null && !sValore.equals("")) valoreFull=sValore;
         }
		 
         //System.out.println("-->"+valoreFull);
         //String sNomeCampo="valore_clob,";
          String sNomeCampo="";                                
          
          String idValoreInInsert = this.getIdValore();

          String sTipoCampo="",sTipo;
          String sCampoInUso;
          long lunghezza;

          // Scelta della colonna sulla quale registrare in funzione del tipo di dato
          try {
        	 FieldInformation fi = this.getFieldInformation();
        	 
        	 if (fi==null || fi.getTipo()==null) {
	             /*sTipoCampo=(new LookUpDMTable(variabiliUtente)).lookUpTipoCampo(this.getCampo().getIdCampo());
	             sCampoInUso=sTipoCampo.substring(0,sTipoCampo.indexOf("#"));
	             sTipoCampo=sTipoCampo.substring(sTipoCampo.indexOf("#")+1,sTipoCampo.length());*/
        		 //fi=(new LookUpDMTable(variabiliUtente)).lookUpInfoCampo(this.getCampo().getIdCampo());
        		 throw new Exception("Errore! Per il campo ("+this.getCampo().getNomeCampo()+") non ho le informazioni caricate in memoria!\n");
        	 }
        	 
        	 sTipoCampo=fi.getTipo();
        	 sCampoInUso=fi.getInUso(); 
        	 sFormatoData=fi.getFormatoData();
        	 lunghezza=fi.getLunghezza();
        	 
        	 if (fi.getSenzaSalvataggio().equals("S")) {
        		 isInsertOrUpdate="NESSUNA";
        		 return true;
        	 }
          }
          catch (Exception e) {
                throw new Exception("Errore nel recupero informazioni per il campo ("+this.getCampo().getNomeCampo()+")\n"+e.getMessage());
          } 
          
          String tipoCampoHorizontal="";
          String lunghezzaCampoHorizontal="";          
          Object valoreParametricoCampoHoriziontal="";          
          //GESTIONE DELLE TABELLE ORIZZONTALI - TIPO DI CAMPO
          /*if (this.getUpdateOrizzontalTable()) { 	          	          	          
	          try {
	        	  tipoCampoHorizontal = (new LookUpDMTable(variabiliUtente)).lookUpTipoCampoOrizontalTable(this.getCampo().getNomeCampo(),""+idDocumento);

	        	  if (tipoCampoHorizontal.equals("")) {
	        		 String cm = (new LookUpDMTable(variabiliUtente)).lookUpNomeTabellaOrizontaleByIdDoc(""+idDocumento); 
	        		 throw new Exception("GD4_Valori:insert() errore lookUpCampi Horizontal Table - MANCA IL CAMPO ("+this.getCampo().getNomeCampo()+") sulla TABELLA ORIZZONTALE ("+cm+")\n");
	        	  }
	        	  
	        	  //Campo non in uso
	        	  if (tipoCampoHorizontal.equals("N")) {
	        		  this.setUpdateOrizzontalTable(false);
	        	  }
	        	  else {	        	  
		              lunghezzaCampoHorizontal=tipoCampoHorizontal.substring(tipoCampoHorizontal.indexOf("@")+1,tipoCampoHorizontal.length());
		              tipoCampoHorizontal=tipoCampoHorizontal.substring(0,tipoCampoHorizontal.indexOf("@"));
	        	  }
	          }
	          catch (Exception e) {
	                throw new Exception("GD4_Valori:insert() errore lookUpCampi Horizontal Table\n"+e.getMessage());
	          }
          }*/
          //FINE GESTIONE DELLE TABELLE ORIZZONTALI - TIPO DI CAMPO	
          
          try {  
              int i = sTipoCampo.indexOf("@");
  
              if (i > 0 ) {
                 sTipo = sTipoCampo.substring(0,i);
                 sFormatoData = sTipoCampo.substring(i+1);
              }
              else
                 sTipo = sTipoCampo;
                
              if (sTipo.equals("N")) {
                 sNomeCampo="VALORE_NUMERO,";
                 tipoCampoHorizontal="NUMBER";
                 if (sValoreDouble==null ) {
	                 if ((sValore==null) ||(sValore.equals(""))) {
	                    sValore = "NULL";
	                    sValoreDouble = new java.math.BigDecimal(0);
	                 } 
	                 else {
	                    sValore = sValore.replace(',','.');
	                    sValoreDouble = new java.math.BigDecimal(sValore);
	                 } 
                 }
              }
              else if (sTipo.equals("D")) {
                   sNomeCampo="VALORE_DATA,";
                   tipoCampoHorizontal="DATE";                   
                   if (sValoreData==null) {
	                   if ((sValore==null) ||(sValore.equals(""))) {
	                	    sValore = "NULL";	                	    
	                        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	                        java.util.Date parsedDate = df.parse("01/01/1900");
	                        sValoreData= new java.sql.Date(parsedDate.getTime());               	    
	                   }                     
	                   else {	             
	                	  try {
	                		 /* SimpleDateFormat df=new SimpleDateFormat(sFormatoData);
	                          java.util.Date parsedDate = df.parse(sValore);
	                          sValoreData= new java.sql.Date(parsedDate.getTime());*/	 
	                		  sFormatoData=Global.replaceAll(sFormatoData,"dd/mm/yyyy","dd/MM/yyyy");
	                		  
	                		  sFormatoData=Global.replaceAll(sFormatoData,"mi","mm");
	                		  	                		  
	                          sValoreData=DateSqlUtility.toDate(sValore,sFormatoData);// new java.sql.Date(parsedDate.getTime());	                          
	                	  }
	                	  catch(Exception eii) {
	                		  throw new Exception("Errore nel parsing della data ("+sValore+","+sFormatoData+") - campo ("+this.getCampo().getNomeCampo()+"): \n"+eii.getMessage()); 
	                	  }
	                      sValore = "TO_DATE('" + sValore + "','"+sFormatoData+"')";
	                   }
                   }
              }      
          }              
          catch (Exception e) {
                throw new Exception(e.getMessage());
          }     
          
          //Controllo sulle colonne Date e Number
          if (sValoreData!=null && !sNomeCampo.equals("VALORE_DATA,")) 
             throw new Exception("E' stato erroneamente specificato "+
                                 "un valore data per il campo ("+this.getCampo().getNomeCampo()+")");

          if (sValoreDateTime!=null && !sNomeCampo.equals("VALORE_DATA,")) 
              throw new Exception("E' stato erroneamente specificato "+
                                  "un valore data per il campo ("+this.getCampo().getNomeCampo()+")");
          
          if (sValoreDouble!=null && !sNomeCampo.equals("VALORE_NUMERO,")) 
              throw new Exception("E' stato erroneamente specificato "+
                                 "un valore numerico per il campo ("+this.getCampo().getNomeCampo()+")");
                                 
          StringBuffer sStm = null;
          // Lancio dell'insert sul Database                    
          
          try {
            //IDbOperationSQL dbOpSql  = variabiliUtente.getDbOp();            
            
            sStm = new StringBuffer();
            
            /* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
            if (idValoreInInsert.equals("0") && dbOpSqlBatchInsert!=null) {
               elpsTime.start("Creazione sequence del valore","(CAMPO,VALORE)=("+this.getCampo().getNomeCampo()+","+sValore+")");
               IDbOperationSQL dbOpSqlSeq = SessioneDb.getInstance().createIDbOperationSQL(dbOpSqlBatchInsert);
               idValoreInInsert=dbOpSqlSeq.getNextKeyFromSequence("VALO_SQ")+"";
               dbOpSqlSeq.close();
               elpsTime.stop();
            }
            
            if (sNomeCampo.equals(""))
               sValoreDato="";
            else {
               sValoreDato="," +sValore;
               valoreParametricoCampoHoriziontal=sValore;
            }
            
            if (sValoreData!=null) {            	
               sValoreDato=",:P_DATA";
               tipoCampoHorizontal="DATE";
               sNomeCampo="VALORE_DATA,";
            }
            if (sValoreDateTime!=null) {            	
               sValoreDato=",:P_DATA";
               tipoCampoHorizontal="DATE";
               sNomeCampo="VALORE_DATA,";
            }            
            if (sValoreDouble!=null) {
               tipoCampoHorizontal="NUMBER";
               sValoreDato=",:P_NUMBER";
               sNomeCampo="VALORE_NUMERO,";
            }                 
            
            if (sValoreData==null && sValoreDouble==null && sValoreDateTime==null && sNomeCampo.equals("")) {
               sValoreDato=",:P_VALORE_CLOB,:P_VALORE_STRINGA";
               sNomeCampo="VALORE_CLOB,VALORE_STRINGA,";
            }
            
            /* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
            if (dbOpSqlBatchInsert!=null) {
	            sStm.append("INSERT INTO VALORI (ID_VALORE,ID_DOCUMENTO,ID_CAMPO,");
	            sStm.append("VALORE_NUMERO,VALORE_DATA,VALORE_STRINGA,VALORE_CLOB,UTENTE_AGGIORNAMENTO");
	            sStm.append(") VALUES ");            
	            sStm.append("(:P_IDVALORE,:P_IDDOCUMENTO,:P_IDCAMPO" );
	            sStm.append(",:P_NUMBER,:P_DATA,:P_VALORE_STRINGA,:P_VALORE_CLOB" );
	            sStm.append(",'" + variabiliUtente.getUser() + "')");                                  
                         
	            dbOpSqlBatchInsert.setStatementBatch(sStm.toString());                        

	            dbOpSqlBatchInsert.setParameter(":P_IDVALORE",Long.parseLong(idValoreInInsert));
	            idValore=idValoreInInsert;
	            dbOpSqlBatchInsert.setParameter(":P_IDDOCUMENTO",Long.parseLong(""+idDocumento));
	            dbOpSqlBatchInsert.setParameter(":P_IDCAMPO",Long.parseLong(this.getCampo().getIdCampo()));
            }
	            
            if (sValoreData!=null) {
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
               if (sValore!=null && sValore.equals("NULL")) { 
            	   if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_DATA",(java.sql.Date)null);
            	   sType="java.sql.Date";
                   valoreParametricoCampoHoriziontal=null;            	   
               }
               else {
            	   if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_DATA",(new java.sql.Timestamp(sValoreData.getTime())) );
            	   valoreParametricoCampoHoriziontal=sValoreData;
               }            
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_STRINGA",(java.lang.String)null);
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_CLOB",(Clob)null);               
            }  
            if (sValoreDateTime!=null) {
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_DATA",sValoreDateTime);
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_STRINGA",(java.lang.String)null);
               if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_CLOB",(Clob)null);                
               valoreParametricoCampoHoriziontal=sValoreDateTime;
            }
            if (sValoreDouble!=null) {       
            	if (sValore!=null && sValore.equals("NULL")) {
                   if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
            	   sType="java.math.BigDecimal";
                   valoreParametricoCampoHoriziontal=null;
            	}
            	else {
            	   if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_NUMBER",sValoreDouble);
            	   valoreParametricoCampoHoriziontal=sValoreDouble;
            	}            	   
                if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_DATA",(java.sql.Date)null);
                if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_STRINGA",(java.lang.String)null);
                if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_CLOB",(Clob)null);                                               
            } 
            
            if (sValoreData==null && sValoreDouble==null && sValoreDateTime==null && sNomeCampo.equals("VALORE_CLOB,VALORE_STRINGA,")) {

            	/*if (lunghezza<=4000 && sValore.length()>4000) {
            		sValore=sValore.substring(1,10)+"[....("+sValore.length()+" caratteri)]";
            		throw new Exception("il campo specificato. Il campo può contenere al massimo "+lunghezza+" caratteri. Si sta cercando di inserirne "+sValore.length());
            	}
            	*/
            	if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
                if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_DATA",(java.sql.Date)null);
                
                if (sValore.length()>1000) {
            	    if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_STRINGA",sValore.toUpperCase().substring(0,1000));
            	    valoreParametricoCampoHoriziontal=sValore.toUpperCase().substring(0,1000);
            	    tipoCampoHorizontal="VARCHAR2";
                }
                else {
            	    if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_STRINGA",sValore.toUpperCase());
            	    valoreParametricoCampoHoriziontal=sValore.toUpperCase();
            	    tipoCampoHorizontal="VARCHAR2";
                }            	
            	
                if (sValore.length()<4000) {          	                                            
            		if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setParameter(":P_VALORE_CLOB",sValore);
            		valoreParametricoCampoHoriziontal=sValore;
            		tipoCampoHorizontal="CLOB";
            	}
            	else {
            		 sValore4000=sValore;
            		 sIdValore4000=idValoreInInsert;
	            	 byte bValore[] = sValore.getBytes();
	                 ByteArrayInputStream bais = new ByteArrayInputStream(bValore);             	                                               
	                 if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.setAsciiStream(":P_VALORE_CLOB", bais, bais.available());
	                 valoreParametricoCampoHoriziontal=sValore;
	                 tipoCampoHorizontal="CLOB";
            	}        	            	               
            }
            
            if (dbOpSqlBatchInsert!=null) dbOpSqlBatchInsert.addStatementBatch();                       	
                                   
            if (this.getUpdateOrizzontalTable() && sCampoInUso.equals("Y")) {            	            	            	
            	updateHorizontal(this.getCampo().getNomeCampo(),""+idDocumento,
            			         valoreParametricoCampoHoriziontal,tipoCampoHorizontal
            			         ,lunghezzaCampoHorizontal,sType);            	
            }
                       
          }    
          catch (Exception e) {            	      
                  throw new Exception("Errore costruzione frase SQL per "+ e.getMessage()+
                                      "\n(CAMPO,VALORE)=("+this.getCampo().getNomeCampo()+","+sValore+")\n Errore: "+e.getMessage());
          }
          
          // Lancio dell'update sul Database per il clob 
          // solo se non è una data ed ho un clob
          /*if (sValoreData==null && sValoreDouble==null && sValoreDateTime==null && sNomeCampo.equals("")) {
               updateClob(idValoreInInsert, sValore);      
               elpsTime.stop();
          }
          else*/
          
                                      	           
          return true;
  }
  

  /*
   * METHOD:      update(Object) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiorna nella tabella valori un valore di tipo
   *              GD4_Valori.
   *              Viene passato l'id del documento di appartenenza
   *              che magari vogliamo aggiornare
   *              Viene controllato anche il tipo di campo per
   *              scegliere la colonna su cui registrare tra:
   *              valore_stringa, valore_numero, valore_data
   *
   * RETURN:      boolean
  */
  public boolean update(Object idDocumento) throws Exception 
  {
	     sValore4000=null;
	  
	     if (dbOpSqlBatchUpdate!=null) {
	         if (this.getIdValore().equals("0"))
	             return insert(idDocumento);	    	 
	     }

         isInsertOrUpdate="UPDATE";
         
         String               sValore       	= null;
         java.sql.Date        sValoreData   	= null;
         java.sql.Timestamp   sValoreDateTime   = null;
         java.math.BigDecimal sValoreDouble 	= null;

         String sType="";
         String sValoreDato = "";
         String sFormatoData="dd/mm/yyyy";         
        
         if ( this.getValore() instanceof java.sql.Date) 
             sValoreData=(java.sql.Date)this.getValore();
         else if ( this.getValore() instanceof java.sql.Timestamp) 
           	 sValoreDateTime=(java.sql.Timestamp)this.getValore();         
         else if (this.getValore() instanceof java.math.BigDecimal)
             sValoreDouble=(java.math.BigDecimal)this.getValore();         
         else
             sValore = (String)(this.getValore());
          
          //String sNomeCampo="valore_clob";
          String sNomeCampo="";
          String sNomeCampoAlt1="valore_numero";
          String sNomeCampoAlt2="valore_data";

          String sTipoCampo="",sTipo;       
          String sCampoInUso;

          // Scelta della colonna sulla quale registrare in funzione del tipo di dato
          try {
        	 FieldInformation fi = this.getFieldInformation();
        	 
        	 if (fi==null || fi.getTipo()==null) {
	             /*sTipoCampo=(new LookUpDMTable(variabiliUtente)).lookUpTipoCampo(this.getCampo().getIdCampo());
	             sCampoInUso=sTipoCampo.substring(0,sTipoCampo.indexOf("#"));
	             sTipoCampo=sTipoCampo.substring(sTipoCampo.indexOf("#")+1,sTipoCampo.length());*/        		 
        		 //fi=(new LookUpDMTable(variabiliUtente)).lookUpInfoCampo(this.getCampo().getIdCampo());
        		 throw new Exception("Errore! Per il campo ("+this.getCampo().getNomeCampo()+") non ho le informazioni caricate in memoria!\n");
        	 }
        	 
        	 sTipoCampo=fi.getTipo();
        	 sCampoInUso=fi.getInUso(); 
        	 sFormatoData=fi.getFormatoData();

        	 if (fi.getSenzaSalvataggio().equals("S")) {
        		 isInsertOrUpdate="NESSUNA";
        		 return true;
        	 }
        	 
        	 if (fi.getSenzaAggiornamento().equals("S"))
        		throw new Exception("Errore! Il campo ("+this.getCampo().getNomeCampo()+") non è aggiornabile!\n"); 
          }
          catch (Exception e) {
                throw new Exception("Errore nel recupero informazioni per il campo ("+this.getCampo().getNomeCampo()+")\n"+e.getMessage());
          } 
          
          String tipoCampoHorizontal="";
          String lunghezzaCampoHorizontal="";          
          Object valoreParametricoCampoHoriziontal="";          
          //GESTIONE DELLE TABELLE ORIZZONTALI - TIPO DI CAMPO
          /*if (this.getUpdateOrizzontalTable()) { 	          	          	          
	          try {
	        	  tipoCampoHorizontal = (new LookUpDMTable(variabiliUtente)).lookUpTipoCampoOrizontalTable(this.getCampo().getNomeCampo(),""+idDocumento);              

	        	  if (tipoCampoHorizontal.equals("")) {
	        		 String cm = (new LookUpDMTable(variabiliUtente)).lookUpNomeTabellaOrizontaleByIdDoc(""+idDocumento); 
	        		 throw new Exception("GD4_Valori:insert() errore lookUpCampi Horizontal Table - MANCA IL CAMPO ("+this.getCampo().getNomeCampo()+") sulla TABELLA ORIZZONTALE ("+cm+")\n");
	        	  }
	        	  
	        	  //Campo non in uso
	        	  if (tipoCampoHorizontal.equals("N")) {
	        		  this.setUpdateOrizzontalTable(false);
	        	  }
	        	  else {	        	  
		              lunghezzaCampoHorizontal=tipoCampoHorizontal.substring(tipoCampoHorizontal.indexOf("@")+1,tipoCampoHorizontal.length());
		              tipoCampoHorizontal=tipoCampoHorizontal.substring(0,tipoCampoHorizontal.indexOf("@"));
	        	  }
	          }
	          catch (Exception e) {
	                throw new Exception("GD4_Valori:insert() errore lookUpCampi Horizontal Table\n"+e.getMessage());
	          }
          }*/
          //FINE GESTIONE DELLE TABELLE ORIZZONTALI - TIPO DI CAMPO	          
          
          try {
            int i = sTipoCampo.indexOf("@");

            if (i > 0 ) {
               sTipo = sTipoCampo.substring(0,i);
               sFormatoData = sTipoCampo.substring(i+1);
            }               
            else
               sTipo = sTipoCampo;
               
            if (sTipo.equals("N")) {
               tipoCampoHorizontal="NUMBER";
               sNomeCampo="VALORE_NUMERO,";
               if (sValoreDouble==null ) {
	                 if ((sValore==null) ||(sValore.equals(""))) {
	                    sValore = "NULL";
	                    sValoreDouble = new java.math.BigDecimal(0);
	                 } 
	                 else {
	                    sValore = sValore.replace(',','.');
	                    sValoreDouble = new java.math.BigDecimal(sValore);
	                 } 
                 }
            }
            else if (sTipo.equals("D")) {
                 tipoCampoHorizontal="DATE";
                 sNomeCampo="VALORE_DATA,";
                 if (sValoreData==null) {
	                   if ((sValore==null) ||(sValore.equals(""))) {
	                	    sValore = "NULL";	                	    
	                        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	                        java.util.Date parsedDate = df.parse("01/01/1900");
	                        sValoreData= new java.sql.Date(parsedDate.getTime());               	    
	                   }                     
	                   else {	             
	                	  try {
	                		  /*System.out.println(Global.replaceAll(sFormatoData,"dd/mm/yyyy","dd/MM/yyyy"));
	                		  SimpleDateFormat df=new SimpleDateFormat(Global.replaceAll(sFormatoData,"dd/mm/yyyy","dd/MM/yyyy"));
	                          java.util.Date parsedDate = df.parse(sValore);*/	 	  
	                		  
	                		  sFormatoData=Global.replaceAll(sFormatoData,"dd/mm/yyyy","dd/MM/yyyy");
	                		  
	                		  sFormatoData=Global.replaceAll(sFormatoData,"mi","mm");
	                		  
	                          sValoreData=DateSqlUtility.toDate(sValore,sFormatoData);// new java.sql.Date(parsedDate.getTime());	                          
	                	  }
	                	  catch(Exception eii) {
	                		  throw new Exception("Errore nel parsing della data ("+sValore+","+sFormatoData+") - campo ("+this.getCampo().getNomeCampo()+"): \n"+eii.getMessage()); 
	                	  }
	                      sValore = "TO_DATE('" + sValore + "','"+sFormatoData+"')";
	                   }
                 }
            } 
            
            valoreParametricoCampoHoriziontal=sValoreDato;
          }              
          catch (Exception e) {
                throw new Exception(e.getMessage());
          }         
          
          //Controllo sulle colonne Date e Number
          if ((sValoreData!=null || sValoreDateTime!=null) && !sNomeCampo.equals("VALORE_DATA,")) 
             throw new Exception("E' stato erroneamente specificato "+
                                 "un valore data per il campo ("+this.getCampo().getNomeCampo()+")");
          
          if (sValoreDouble!=null && !sNomeCampo.equals("VALORE_NUMERO,")) 
              throw new Exception("E' stato erroneamente specificato "+
                                 "un valore numerico per il campo ("+this.getCampo().getNomeCampo()+")");          

          if (sValoreData!=null || sValoreDateTime!=null) {
             sValoreDato=":P_DATA";
             sNomeCampo="VALORE_DATA,";
             tipoCampoHorizontal="DATE";                  
          }                
          
          if (sValoreDouble!=null) {
             sValoreDato=":P_NUMBER";
             sNomeCampo="VALORE_NUMERO,";
             tipoCampoHorizontal="NUMBER";
          }           
          if (sValoreData==null && sValoreDouble==null && sValoreDateTime==null && sNomeCampo.equals("")) {
               sValoreDato=",:P_VALORE_CLOB,:P_VALORE_STRINGA";
               sNomeCampo="VALORE_CLOB,VALORE_STRINGA,";
          }          
             
          // Lancio dell'update sul Database
          try {            
        	/* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
        	if (dbOpSqlBatchUpdate!=null) {
	            String query = "UPDATE VALORI SET "+
	                          //"ID_DOCUMENTO = :P_IDDOCUMENTO"+
	                          "ID_CAMPO = :P_IDCAMPO" +
	                          ",VALORE_NUMERO = :P_NUMBER" +
	                          ",VALORE_DATA = :P_DATA" +
	                          ",VALORE_STRINGA = :P_VALORE_STRINGA" +
	                          ",VALORE_CLOB = :P_VALORE_CLOB" +
	                          ",UTENTE_AGGIORNAMENTO = '"+ variabiliUtente.getUser() + "'"+
	                          ",DATA_AGGIORNAMENTO = SYSDATE"+
	                          " WHERE ID_VALORE = :P_IDVALORE";            
	
	             dbOpSqlBatchUpdate.setStatementBatch(query);
                          
	             //dbOpSqlBatch.setParameter(":P_IDDOCUMENTO",Long.parseLong(""+idDocumento));
	             dbOpSqlBatchUpdate.setParameter(":P_IDCAMPO",Long.parseLong(this.getCampo().getIdCampo()));
	             dbOpSqlBatchUpdate.setParameter(":P_IDVALORE",Long.parseLong(this.getIdValore()));
        	 }
             if (sValoreData!=null) {
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
                 if (sValore!=null && sValore.equals("NULL")) { 
             	    if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_DATA",(java.sql.Date)null);
            	    sType="java.sql.Date";
                    valoreParametricoCampoHoriziontal=null;            	   
                 }
                 else {      
             	    if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_DATA",(new java.sql.Timestamp(sValoreData.getTime())) );
            	    valoreParametricoCampoHoriziontal=sValoreData;
                 }            
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_STRINGA",(java.lang.String)null);
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_CLOB",(Clob)null);               
             }
             if (sValoreDateTime!=null) {
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_DATA",sValoreDateTime);
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_STRINGA",(java.lang.String)null);
                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_CLOB",(Clob)null);                
                 valoreParametricoCampoHoriziontal=sValoreDateTime;
             }              
             if (sValoreDouble!=null) {       
            	if (sValore!=null && sValore.equals("NULL")) {
                   if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
            	   sType="java.math.BigDecimal";
                   valoreParametricoCampoHoriziontal=null;
            	}
            	else {
            	   if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_NUMBER",sValoreDouble);
            	   valoreParametricoCampoHoriziontal=sValoreDouble;
            	}            	   
                if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_DATA",(java.sql.Date)null);
                if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_STRINGA",(java.lang.String)null);
                if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_CLOB",(Clob)null);                                               
             }
             if (sValoreData==null && sValoreDouble==null && sValoreDateTime==null && sNomeCampo.equals("VALORE_CLOB,VALORE_STRINGA,")) {

            	if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_NUMBER",(java.math.BigDecimal)null);
                if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_DATA",(java.sql.Date)null);
                
                if (sValore.length()>1000) {
            	    if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_STRINGA",sValore.toUpperCase().substring(0,1000));
            	    valoreParametricoCampoHoriziontal=sValore.toUpperCase().substring(0,1000);
            	    tipoCampoHorizontal="VARCHAR2";
                }
                else {
            	    if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_STRINGA",sValore.toUpperCase());
            	    valoreParametricoCampoHoriziontal=sValore.toUpperCase();
            	    tipoCampoHorizontal="VARCHAR2";
                }            	
            	
            	if (sValore.length()<4000) {          	                                            
            		if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setParameter(":P_VALORE_CLOB",sValore);
            		valoreParametricoCampoHoriziontal=sValore;
            		tipoCampoHorizontal="CLOB";
            	}
            	else {
            		 sValore4000=sValore;
            		 sIdValore4000=this.getIdValore();
	            	 byte bValore[] = sValore.getBytes();
	                 ByteArrayInputStream bais = new ByteArrayInputStream(bValore);             	                                               
	                 if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.setAsciiStream(":P_VALORE_CLOB", bais, bais.available());
	                 valoreParametricoCampoHoriziontal=sValore;
	                 tipoCampoHorizontal="CLOB";
            	}           	            	               

             }                                      
                        
             if (dbOpSqlBatchUpdate!=null) dbOpSqlBatchUpdate.addStatementBatch();
                          
             /*if (sTipo.equals("S") && sValoreData==null && sValoreDouble==null) {
            	 tipoCampoHorizontal="CLOB";
            	 valoreParametricoCampoHoriziontal=sValore;
                 updateClob(this.getIdValore(), sValore);
                 elpsTime.stop();
             }
             else*/
                         
             if (this.getUpdateOrizzontalTable() && sCampoInUso.equals("Y")) {            	
            	updateHorizontal(this.getCampo().getNomeCampo(),""+idDocumento,
            			         valoreParametricoCampoHoriziontal,tipoCampoHorizontal
            			         ,lunghezzaCampoHorizontal,sType);            	
            }
                        
            	 
          }
          catch (Exception e) {                        	
                throw new Exception("Errore costruzione frase SQL per "+ e.getMessage()+
                                    "(CAMPO,VALORE)=("+this.getCampo().getNomeCampo()+","+sValore+")\n Errore: "+e.getMessage());
          }                   
    
          return true;
  }
  
  public void executeBatch(String idDocumento,String type,Vector vClob) throws Exception {
	     try {	         
	        	if (type.equals("UPDATE")) {
		    	 	elpsTime.start("Aggiornamento di tutti i valori (in tabella Valori) ","");           
		        	dbOpSqlBatchUpdate.executeBatch();	        	            
		        	elpsTime.stop();		        	
	        	}
	        	else {
		    	 	elpsTime.start("Inserimento di tutti i valori (in tabella Valori) ","");           
		        	dbOpSqlBatchInsert.executeBatch();	        	            
		        	elpsTime.stop();	        		
	        	}
	        	
	        	for(int i=0;i<vClob.size();i++) {
	        		String idValore=((keyval)vClob.get(i)).getKey();
	        		String valore=((keyval)vClob.get(i)).getVal();
		        	updateClob(idValore, valore);
	        	}	          		           
          }    
          catch (Exception e) {
        	  throw new Exception("Errore lancio frase SQL per "+type+" valori.\nErrore: "+ e.getMessage());        	  
          } 
  }
  
  public void executeLog(String idDocumento,Vector vListaIdModificati) throws Exception {
	        //Inserisco i Log sui campi
	        try {
	          if (dmALog!=null) dmALog.insertAllVaLog(idDocumento,vListaIdModificati);
	        }    
	        catch (Exception e) {        	
	     	  throw new Exception("GD4_Valori::executeLog() Errore in scrittura Valori Log");
	       }	     
  }
  
  public void executeLogHorizontal(String idDocumento, String nomeTabH, Vector vListaCampiModificati, Vector vlistaTipiCampoModificati) throws Exception {
	        //Inserisco i Log sui campi
	        try {
	          if (dmALog!=null) dmALog.insertAllVaLogHorizontal(idDocumento,nomeTabH,vListaCampiModificati,vlistaTipiCampoModificati,false);
	        }    
	        catch (Exception e) {        	
	     	  throw new Exception("GD4_Valori::executeLogHorizontal() Errore in scrittura Valori Log - "+e.getMessage());
	       }	     
  }  
  
  private void updateHorizontal(String nomeCampo,String idDocumento,
		  						Object valoreParametricoCampoHoriziontal,
		  						String tipoCampoHorizontal,
            			        String lunghezzaCampoHorizontal,
            			        String sClassName) throws Exception {
	  	  
	  	 // try {
	          String sVirgola=",";
	        	  
	  		  if (tipoCampoHorizontal.equals("VARCHAR2")) {	  				 
	  				sUpdateHorizontal.append(nomeCampo+"=:P_PAR"+indexParHorizontalTable+sVirgola);	  			  
	  		  }
	  		  
	  		  if (tipoCampoHorizontal.equals("DATE") || tipoCampoHorizontal.equals("NUMBER")) {
	  			  if (valoreParametricoCampoHoriziontal instanceof String) {
	  				  sUpdateHorizontal.append(nomeCampo+"="+valoreParametricoCampoHoriziontal+sVirgola);	 
	  				  return;
	  			  }
	  			  else
	  				  sUpdateHorizontal.append(nomeCampo+"=:P_PAR"+indexParHorizontalTable+sVirgola);	  			  	  				         			         					 	  				 
	  		  }
	  		  
	  		  if (tipoCampoHorizontal.equals("CLOB")) {
	  			  sUpdateHorizontal.append(nomeCampo+"=:P_PAR"+indexParHorizontalTable+sVirgola);	
	  		  }	  		  	  			  		 
	  		  
	  	      if (tipoCampoHorizontal.equals("VARCHAR2"))  {	  			   
	  	    	  dbOpSpB = new DbOpSetParameterBuffer(":P_PAR"+indexParHorizontalTable,
	  	    			  							   ""+valoreParametricoCampoHoriziontal,
	  	    			  							   DbOpSetParameterBuffer.IS_PARAMETER);	  	    	  
	  		  }
	  		  
        	  if ((valoreParametricoCampoHoriziontal==null && sClassName.equals("java.sql.Date")) ||
        		  valoreParametricoCampoHoriziontal instanceof java.sql.Date) {
	  			  dbOpSpB = new DbOpSetParameterBuffer(":P_PAR"+indexParHorizontalTable,
	  	    			  							   (java.sql.Date)valoreParametricoCampoHoriziontal,
	  	    			  							   DbOpSetParameterBuffer.IS_PARAMETER);
	  			  
	  			  if (valoreParametricoCampoHoriziontal==null) 
	  				  dbOpSpB.setValueTypeNull(new java.sql.Date(0));
        	  }       			 
        	  if (valoreParametricoCampoHoriziontal instanceof java.sql.Timestamp)
        		  dbOpSpB = new DbOpSetParameterBuffer(":P_PAR"+indexParHorizontalTable,
	  	    			  							   (java.sql.Timestamp)valoreParametricoCampoHoriziontal,
	  	    			  							   DbOpSetParameterBuffer.IS_PARAMETER);        		 

        	  if ((valoreParametricoCampoHoriziontal==null && sClassName.equals("java.math.BigDecimal")) ||
        		  valoreParametricoCampoHoriziontal instanceof java.math.BigDecimal) {
        		  dbOpSpB = new DbOpSetParameterBuffer(":P_PAR"+indexParHorizontalTable,
	  	    			  							   (java.math.BigDecimal)valoreParametricoCampoHoriziontal,
	  	    			  							   DbOpSetParameterBuffer.IS_PARAMETER);  
        	  	  
        		  if (valoreParametricoCampoHoriziontal==null) 
	  				  dbOpSpB.setValueTypeNull(new java.math.BigDecimal(0));
        	  }
        	  
        	  if (tipoCampoHorizontal.equals("CLOB")) {
        		  if ((""+valoreParametricoCampoHoriziontal).length()<4000) {            		 
        		     dbOpSpB = new DbOpSetParameterBuffer(":P_PAR"+indexParHorizontalTable,
	  	    			  							   ""+valoreParametricoCampoHoriziontal,
	  	    			  							   DbOpSetParameterBuffer.IS_PARAMETER);        		          		 
            		 
            	 }
            	 else {
	            	 byte bValore[] = (""+valoreParametricoCampoHoriziontal).getBytes();
	                 ByteArrayInputStream bais = new ByteArrayInputStream(bValore);  	                 
	                 dbOpSpB = new DbOpSetParameterBuffer(":P_PAR"+indexParHorizontalTable,
	                		 ""+valoreParametricoCampoHoriziontal,
  							   DbOpSetParameterBuffer.IS_ASCIISTREAM
	  	    			  							   /*bais,
	  	    			  							   DbOpSetParameterBuffer.IS_ASCIISTREAM*/);  	                 
	                 dbOpSpB.setNameColumn(nomeCampo);
            	 }     
        	  }
        	          	
	  		  /*dbOpSql.execute();*/
	  		  
	  		  //FULL TEXT

	  	 /* }
          catch (Exception e) {*/          
        	    /*if (e.getMessage().indexOf("ORA-12899")!=-1)
        	    	throw new Exception("GD4_Valori::updateHorizontal() E' STATO INSERITO UN VALORE TROPPO LUNGO PER IL CAMPO:  (campo="+nomeCampo+") - "+e.getMessage());
        	  
        	    if (e.getMessage().indexOf("ORA-00904")!=-1) {        	    	
        	    	String tipoUso = (new LookUpDMTable(variabiliUtente)).lookUpDatoInUso(nomeCampo,""+idDocumento);
        	    	
        	    	if (tipoUso.equals("Y")) {
        	    		String cm = (new LookUpDMTable(variabiliUtente)).lookUpNomeTabellaOrizontaleByIdDoc(""+idDocumento);
	        		    throw new Exception("GD4_Valori:updateHorizontal() errore lookUpCampi Horizontal Table - MANCA IL CAMPO ("+nomeCampo+") sulla TABELLA ORIZZONTALE ("+cm+")\n");
        	    	}
        	    }
        	    else {       	    
        	    	throw new Exception("GD4_Valori::updateHorizontal() (campo="+nomeCampo+") - "+e.getMessage());
        	    }*/
          //}
	  	  	  
  }

  /*
   * METHOD:      updateClob(Object, String) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiorna nella tabella valori un valore di tipo
   *              VALORE_CLOB.
   *              
   * RETURN:      boolean
  */
  private boolean updateClob(Object IdValore, String valore) throws Exception 
  {
          try {
          
              IDbOperationSQL dbOpSql = variabiliUtente.getDbOp();
              
              String  query = "UPDATE valori "+
                              "SET VALORE_CLOB = :VALORE "+                             
                              "WHERE ID_VALORE = "+ IdValore;
                                            
              //********* FINE CONTROLLO SE IL VALORE ESISTE ***************//
              
              if (valore==null) 
                  valore = "";

              dbOpSql.setStatement(query);
         
              byte bValore[] = valore.getBytes();
              ByteArrayInputStream bais = new ByteArrayInputStream(bValore);  
              dbOpSql.setAsciiStream(":VALORE", bais, bais.available());
                          
              elpsTime.addMsg("SottoAzione: "+query);
              
              dbOpSql.execute();
                                                                               
          }    
          catch (Exception e) {                
              throw new Exception("GD4_Valori::updateClob() (idValore,valore)=("+IdValore+","+valore+")\nErrore:"+ e.getMessage());
          }
          
          return true;
  }
  
  // ***************** METODI DI GET E SET ***************** //

  public A_Campi_Documento getCampo()
  {
         return campo;
  }
  
  public String toString() 
  {
         return super.toString();
  }
  
  public void setLog(DMActivity_Log dmALog) {
	     this.dmALog=dmALog;
  }
  
  public String getHorizontalPhrase() {
         return sUpdateHorizontal.toString();
  }
  
  public String getIsInsertOrUpdate() {
         return isInsertOrUpdate;
  }
  
  public String getValore4000() {
         return sValore4000;
  }
  
  public String getIdValore4000() {
         return sIdValore4000;
  }  
  
  public DbOpSetParameterBuffer getHorizontalParameterBuffer() {
         return dbOpSpB;
  }  
  
  public ValoreParametrico getValoreParametrico() {
	     return val;
  }  

  public void setIndexParHorizontalTable(int index) {
         indexParHorizontalTable=index;
  }  
  
  public void isLastValue(boolean bFlag) {
	     isLastValue=bFlag;
  } 
  
  public void setDbOpBatchInsert(IDbOperationSQL dbOpSql) {
	  	 dbOpSqlBatchInsert = dbOpSql;
  }

  public void setDbOpBatchUpdate(IDbOperationSQL dbOpSql) {
	  	 dbOpSqlBatchUpdate = dbOpSql;
  }
  
  
  // ***************** METODI PRIVATI ***************** //
  
  private boolean existsCampo(Object idDocumento) throws Exception
  {
         IDbOperationSQL dbOp = null;
       
         try {
           StringBuffer sStm = new StringBuffer();

           dbOp = variabiliUtente.getDbOp();
   
           sStm.append("select id_valore from valori");
           sStm.append(" where id_campo = " + this.getCampo().getIdCampo() +" and id_documento="+idDocumento);

           dbOp.setStatement(sStm.toString());
           
           elpsTime.start("Test se esiste il campo",sStm.toString());
           dbOp.execute();
           elpsTime.stop();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {
               this.setIdValore(""+rst.getLong(1));               
               return true;
           }
           else{
               return false;
           }
         }
         catch (Exception e) {
               throw new Exception("GD4_Valori::existsCampo(@) " + e.getMessage());
         }
 }

	public String getValoreFull() {
		return valoreFull;
	}
  
}
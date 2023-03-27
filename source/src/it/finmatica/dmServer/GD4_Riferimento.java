package it.finmatica.dmServer;
/*
 * GESTIONE DEI RIFERIMENTI
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   27/04/2006
 * 
 * */

import java.sql.*; 
import java.util.Vector;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.dmServer.util.*;

public class GD4_Riferimento 
{
  // variabili private
  private Vector riferimenti;
  private Vector riferimentiFrom;
  private Environment varEnv;
  private String idDocumento="0";
  private String tipo_relazione="";
  private String area="";
  private String data="";
  
  private ElapsedTime elpsTime;  
   
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public GD4_Riferimento(String idDoc) throws Exception
  {
         try {
           inizializzaDati(varEnv);
         }
         catch (Exception e) {          
           throw new Exception("GD4_Riferimento::Costructor\n"+e.getMessage());
         }
         
         this.idDocumento=idDoc;
  }


  /*
   * METHOD:      inizializzaDati(Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di riferimenti del documento
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
         this.varEnv= vUtente;
         // Crea la lista (vuota) dei riferimenti del documento
         this.riferimenti = new Vector();
         this.riferimentiFrom = new Vector();
         
         elpsTime = new ElapsedTime("GD4_RIFERIMENTO",vUtente);
  }
 
  public boolean insertRiferimento(String idDocRif, String rif) throws Exception
  {
	  	 return insertRiferimento(idDocRif,rif,false);
  }
  
  // ***************** METODI DI GESTIONE DEI RIFERIMENTI ***************** //

  /*
   * METHOD:      insertRiferimento(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento di un riferimento 
   *              
   * RETURN:      boolean
  */  
  public boolean insertRiferimento(String idDocRif, String rif, boolean bCheckExists) throws Exception
  {
         String areaRif="";
         try
         {
           /*if(rif.equals("RIF"))
             areaRif="GDMSYS";
           else*/
           areaRif=(new DocUtil(varEnv)).getAreaByIdDocumento(idDocumento);
           
           //Se non esiste la coppia AREA/TIPO RELA , imposto l'area a GDMSYS
           if ((new LookUpDMTable(varEnv)).lookUpTipoRela(areaRif,rif).equals("")) {
        	   areaRif="GDMSYS";
           }                     
               
           if (insert(idDocRif,rif,areaRif,bCheckExists))
           {
             if(addRif(idDocRif,rif))
                return true;
           }     
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Riferimento::insertRiferimento() " + e.getMessage());
         }  
        return true; 
  }

   /*
   * METHOD:      deleteRiferimento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Eliminazione di un riferimento 
   *              
   * RETURN:      void
  */  
  public boolean deleteRiferimento(String idDocRif, String rif) throws Exception
  {
         try
         {
           if (delete(idDocRif,rif))
           {
             if(removeRif(idDocRif,rif))
                return true;
           }     
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Riferimento::deleteRiferimento() " + e.getMessage());
         }  
        return true; 
  }

   /*
   * METHOD:      retrieveRiferimento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Retrieve tipo_relazione e data_aggiornamento 
   *              
   * RETURN:      boolean
  */  
  public boolean retrieveRiferimento() throws Exception
  {
         try
         {
           if (retrieve())
             return true;
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Riferimento::retrieveRiferimento() " + e.getMessage());
         }  
         return true;
  }


  /*
   * METHOD:      retrieveRiferimentoFrom()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Retrieve tipo_relazione e data_aggiornamento 
   *              
   * RETURN:      boolean
  */  
  public boolean retrieveRiferimentoFrom() throws Exception
  {
         try
         {
           if (retrieveFrom())
             return true;
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Riferimento::retrieveRiferimentoFrom() " + e.getMessage());
         }  
         return true;
  }
  
 // ***************** METODI GET E SET ***************** //
 
 
 public String getIdDocumento()
 {
   return this.idDocumento;
 }

 public String getData()
 {
   return this.data;
 }
 
 public String getArea()
 {
   return this.area;
 }
 
 public String getRelazione()
 {
   return this.tipo_relazione;
 }
 
 public Vector getVectorRiferimenti()
 {
   return this.riferimenti;
 }
 
 public Vector getVectorRiferimentiFrom()
 {
   return this.riferimentiFrom;
 }
 public void setData(String d)
 {
        this.data=d;
 }
  
 public void setArea(String areaRif) 
 {
        area=areaRif;
 }  

 public void setRelazione(String type)
 {
        this.tipo_relazione=type;
 }
 
 public Riferimento getRiferimento(int i) {
	      return (Riferimento)riferimenti.get(i);
 }
 
  public Riferimento getRiferimentoFrom(int i) {
	      return (Riferimento)riferimentiFrom.get(i);
 }
 
  /*
   * METHOD:      getRiferimento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce l'oggetto Riferimento dato idDocRif e tipo_relazione
   *              del vettore Riferimenti 
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getRiferimento(String idDocRif,String tipo_relazione)
  {
	       int index=indexOf(idDocRif,tipo_relazione);
         if(index == -1) 
           return null;
         else
           return (Riferimento)riferimenti.get(index);
  }

  /*
   * METHOD:      getRiferimentoFrom()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce l'oggetto Riferimento dato idDocRif e tipo_relazione
   *              del vettore RiferimentiFrom
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getRiferimentoFrom(String idDocRif,String tipo_relazione)
  {
	       int index=indexOf(idDocRif,tipo_relazione);
         if(index == -1) 
           return null;
         else
           return (Riferimento)riferimentiFrom.get(index);
  }
  
  /*
   * METHOD:      getRiferimentoDoc(int)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce l'oggetto Riferimento dato indice
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getRiferimentoDoc(int i) throws  Exception
  {
         Riferimento rif;
           try {
             this.retrieveRiferimento();
             rif=this.getRiferimento(i);
           }
           catch(Exception e) 
           {
            throw new Exception("GD4_Riferimento::getRiferimentoDoc()\n"+ e.getMessage());
         }
        return rif; 
  } 
 
   /*
   * METHOD:      getRiferimentoFromDoc(int)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce l'oggetto Riferimento dato indice
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getRiferimentoFromDoc(int i) throws  Exception
  {
         Riferimento rif;
           try {
             this.retrieveRiferimentoFrom();
             rif=this.getRiferimentoFrom(i);
           }
           catch(Exception e) 
           {
            throw new Exception("GD4_Riferimento::getRiferimentoFromDoc()\n"+ e.getMessage());
         }
        return rif; 
  } 
  
  /*
   * METHOD:      getFirstRiferimentoDoc()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il primo oggetto Riferimento 
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getFirstRiferimentoDoc() throws  Exception
  {
         try {
            if( this.getVectorRiferimenti().size() == 0 )
                return null;
            else
                return this.getRiferimentoDoc(0);
         }
         catch(Exception e) 
         {
            throw new Exception("GD4_Riferimento::getFirstRiferimentoDoc()\n"+ e.getMessage());
         }
  }
  
    /*
   * METHOD:      getFirstRiferimentoFromDoc()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il primo oggetto Riferimento 
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getFirstRiferimentoFromDoc() throws  Exception
  {
         try {
            if( this.getVectorRiferimentiFrom().size() == 0 )
                return null;
            else
                return this.getRiferimentoFromDoc(0);
         }
         catch(Exception e) 
         {
            throw new Exception("GD4_Riferimento::getFirstRiferimentoFromDoc()\n"+ e.getMessage());
         }
  }
 
  /*
   * METHOD:      getFirstRiferimentoDoc()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il successivo oggetto Riferimento alla posizione pos
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getNextRiferimentoDoc(int pos) throws  Exception
  {
         try {
              if( pos == this.getVectorRiferimenti().size() )
               return null;
              else
               return this.getRiferimentoDoc(pos); 
         }
         catch(Exception e) 
         {
           throw new Exception("GD4_Riferimento::getNextRiferimentoDoc()\n"+ e.getMessage());
         }
         
  }
  
   /*
   * METHOD:      getNextRiferimentoFromDoc()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il successivo oggetto Riferimento alla posizione pos
   * 
   * RETURN:      Riferimento
  */ 
  public Riferimento getNextRiferimentoFromDoc(int pos) throws  Exception
  {
         try {
              if( pos == this.getVectorRiferimentiFrom().size() )
               return null;
              else
               return this.getRiferimentoFromDoc(pos); 
         }
         catch(Exception e) 
         {
           throw new Exception("GD4_Riferimento::getNextRiferimentoFromDoc()\n"+ e.getMessage());
         }
         
  }

 // ***************** METODI PRIVATI ***************** //

  /*
   * METHOD:      insert(String, String, String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica un riferimento fra il documento
   *              attuale e quello passato con l'id documento 
   *              
   * RETURN:      boolean
  */  
  private boolean insert(String idDoc, String rif,String areaRif, boolean bCheckExists) throws Exception
  {
          IDbOperationSQL dbOpSql = null;
  
          try {
           StringBuffer sStm = new StringBuffer();                      
           
           //Controllo che id_documento non coincide con id_documento da riferire
           if(idDoc.equals(this.getIdDocumento()))
        	throw new Exception("GD4_Documento::insert() - Impossibile inserire un Riferimento di un Documento su se stesso ");                                  
           
           dbOpSql = varEnv.getDbOp();                      
           
           try {
	           sStm.append("insert into riferimenti (id_documento_rif, id_documento, libreria_remota, area, tipo_relazione,");
	           sStm.append("data_aggiornamento,utente_aggiornamento) values ");
	           sStm.append("("+ idDoc );
	           sStm.append(","+ this.getIdDocumento());
	           sStm.append(",null,'"+areaRif+"'");     
	           sStm.append(",'"+rif+"'");     
	           sStm.append(",sysdate,'"+ varEnv.getUser() +"')");
	           
	           dbOpSql.setStatement(sStm.toString());
	           
	           elpsTime.start("Insert in tabella Riferimenti",sStm.toString());
	           dbOpSql.execute();
	           elpsTime.stop();
           }    
           catch (java.sql.SQLException eSqlExcp) {  
        	   if (!bCheckExists || eSqlExcp.getErrorCode()!=1)
        		   throw new Exception(eSqlExcp);
           }
                    
         }    
         catch (Exception e) {             	
             
               throw new Exception("GD4_Documento::insert() " + e.getMessage());
         }  
         return true;
  }
  

  
  /*
   * METHOD:      eliminaRiferimento(String, String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Elimina un riferimento  
   *              
   * RETURN:      boolean
  */ 
  private boolean delete(String idDocRif,String tipo_relazione) throws Exception
  {
          IDbOperationSQL dbOpSql = null;
          
          try {
           StringBuffer sStm = new StringBuffer();   
           
           dbOpSql = varEnv.getDbOp();
           
           sStm.append("delete riferimenti where ");
           sStm.append(" id_documento = " + this.getIdDocumento());
           sStm.append(" and id_documento_rif = " +idDocRif);
           sStm.append(" and tipo_relazione = '" +tipo_relazione+"'");

           dbOpSql.setStatement(sStm.toString());

           elpsTime.start("Delete da tabella Riferimenti",sStm.toString());
           dbOpSql.execute();
           elpsTime.stop();
          }    
          catch (Exception e) {        
              // dbOpSql.rollback();
               throw new Exception("GD4_Riferimento::delete() " + e.getMessage());
          } 
          return true;
  }
 
  /*
   * METHOD:      retrieve()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Retrieve tipo_relazione e data_aggiornamento
   *              data la coppia idDocumento e idDocumentoRif
   *              
   * RETURN:      boolean
  */ 
  private boolean retrieve() throws Exception
  {
          try {
           
           StringBuffer sStm = new StringBuffer();   
           
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
           
           sStm.append("select r.id_documento_rif, tipo_relazione, r.area, libreria_remota, to_char(r.data_aggiornamento, 'dd/mm/yyyy') dataagg, r.utente_aggiornamento, ");
           sStm.append("F_CM_AREA_CR_FROM_IDDOC(r.id_documento_rif),F_CM_AREA_CR_FROM_IDDOC(r.ID_DOCUMENTO) ");
           sStm.append("from riferimenti r, documenti d ");
           sStm.append("where r.id_documento = " + this.getIdDocumento()+" and ");
           sStm.append(" r.id_documento_rif = d.id_documento ");
           sStm.append(" AND d.STATO_DOCUMENTO NOT IN ('CA','RE') ");
            
           dbOpSql.setStatement(sStm.toString());

           elpsTime.start("Retrieve da tabella Riferimenti",sStm.toString());
           dbOpSql.execute();
           elpsTime.stop();
           
           ResultSet rst = dbOpSql.getRstSet();
           while (rst.next()) {
                                        
              this.addRif( this.getIdDocumento(),rst.getString(1),rst.getString(2),
                           rst.getString(7), rst.getString(8),rst.getString(3));              
           }
           
           rst.close();
          }    
          catch (Exception e) {                              
               throw new Exception("GD4_Riferimento::retrieve() " + e.getMessage());
          }  
          return true;
  }

 /*
   * METHOD:      retrieveFrom()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Retrieve tipo_relazione e data_aggiornamento
   *              data la coppia idDocumento e idDocumentoRif
   *              del vettore riferimentiFrom di cui sono il documento riferito
   *              
   * RETURN:      boolean
  */ 
  private boolean retrieveFrom() throws Exception
  {
          try {
           StringBuffer sStm = new StringBuffer();   
           
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
           
           sStm.append("select r.id_documento, tipo_relazione, r.area, libreria_remota, to_char(r.data_aggiornamento, 'dd/mm/yyyy') dataagg, r.utente_aggiornamento, ");
           sStm.append("F_CM_AREA_CR_FROM_IDDOC(id_documento_rif),F_CM_AREA_CR_FROM_IDDOC(r.ID_DOCUMENTO) ");
           sStm.append("from riferimenti r, documenti d ");
           sStm.append("where id_documento_rif = " + this.getIdDocumento()+" and ");
           sStm.append("r.id_documento = d.id_documento ");
           sStm.append("  AND d.STATO_DOCUMENTO NOT IN ('CA','RE') ");

           dbOpSql.setStatement(sStm.toString());

           elpsTime.start("Retrieve da tabella Riferimenti",sStm.toString());
           dbOpSql.execute();
           elpsTime.stop();
           
           ResultSet rst = dbOpSql.getRstSet();
                         
           while (rst.next()) {
              // vengono scambiati gli id                           
              this.addRifFrom( rst.getString(1),this.getIdDocumento(),rst.getString(2),
                           rst.getString(7), rst.getString(8),rst.getString(3));              
           }
           
           rst.close();
          }    
          catch (Exception e) {                              
               throw new Exception("GD4_Riferimento::retrieveFrom() " + e.getMessage());
          }  
          return true;
  }


  /*
   * METHOD:      addRif
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Inserimento coppia Riferimento(id_riferimento,tipo_relazione)
   * 
   * RETURN:      boolean
  */  
  private boolean addRif(String idDoc, String id_rif,String type_rel,String descrRif,String descrDoc,String area ) throws Exception
  {
	   	   
          Riferimento rif = new Riferimento(idDoc,id_rif,type_rel,descrRif,descrDoc,area);
       
          rif.inizializzaDati(varEnv);
      
          return riferimenti.add(rif);
  }

  private boolean addRif(String id_rif,String type_rel)  throws Exception 
  {
	   	  Riferimento rif=new Riferimento(id_rif,type_rel);
       
          rif.inizializzaDati(varEnv);
       
          return riferimenti.add(rif);
  }  
  
  /*
   * METHOD:      addRifFrom
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Inserimento coppia Riferimento(id_riferimento,tipo_relazione)
   *              del vettore riferimenti di cui sono il documento riferito
   *              
   * RETURN:      boolean
  */  
  private boolean addRifFrom(String idDoc, String id_rif,String type_rel,String descrRif,String descrDoc,String area )  throws Exception
  {
	   	  Riferimento rif=new Riferimento(idDoc,id_rif,type_rel,descrRif,descrDoc,area);
          
          rif.inizializzaDati(varEnv);
          
          return riferimentiFrom.add(rif);
  }

 /*
   * METHOD:      indexOf
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Ricerca della coppia Riferimento(id_riferimento,tipo_relazione)
   * 
   * RETURN:      int
  */  
  private int indexOf(String id_rif,String type_rel) {
	       Riferimento rif;
       
         for (int i=0;i<=riferimenti.size();i++)
         {
           rif=(Riferimento)riferimenti.get(i);
           if(rif.getDocRiferito().equals(id_rif) && rif.getTipoRelazione().equals(type_rel))
            return i;
         }
         return -1;  
  }
 
  /*
   * METHOD:      removeRif
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Elimina un riferimento coppia(id_riferimento,tipo_relazione)
   * 
   * RETURN:      boolean
  */  
  private boolean removeRif(String id_rif,String type_rel) {
	   	 
         if (riferimenti.size()==0) return true;
       
         int index=indexOf(id_rif,type_rel);
         if(index == -1) 
           return false;
         else
           riferimenti.removeElementAt(index);
         
         return true;       
  }
 
}
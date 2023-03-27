package it.finmatica.dmServer.util;

/*
 * METODI DI UTILITA' SUGLI OGGETTI
 * DOCUMENTI/CARTELLE/QUERY
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   13/09/2005
 * 
 * */

import it.finmatica.dmServer.dbEngine.struct.dbTable.Allegati_temp_percorsi;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.dmServer.Environment;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class DocUtil 
{  
  Environment en;
  IDbOperationSQL  dbOp; 
  
  public DocUtil() {
	     
  }
  
  public DocUtil(Environment newEn) {
         en = newEn;
  }
  
  public String getSQLElencoAllegatiDocumento(String idDocumento)  {
	     StringBuffer sStm = new StringBuffer();      

	     sStm.append("select id_oggetto_file,");
	     sStm.append("o.id_formato,filename,\"FILE\",testoocr,allegato,id_oggetto_file_padre,NVL(F.VISIBILE,'S'), nvl(PATH_FILE,''),");
	     sStm.append("NVL(F.ICONA, 'generico.gif'),TO_NUMBER(NULL),nvl(o.DA_CANCELLARE,'N') dacancellare,to_char(o.DATA_AGGIORNAMENTO,'dd/MM/yyyy HH24:mi:ss') dataAgg, ");
	     sStm.append("F_GETIMPOSTAZIONI_GDMSYNCRO impostsyncto, o.id_syncro,ID_SERVIZIO_ESTERNO,    CHIAVE_SERVIZIO_ESTERNO, nvl(o.PATH_FILE_ROOT,'') pathFileRoot ");
	     sStm.append(" from oggetti_file o, formati_file f");
	     sStm.append(" where id_documento = " +idDocumento );
	     sStm.append(" and o.ID_FORMATO = f.ID_FORMATO");
	     sStm.append(" order by filename");
	     
	     //vecchia senza idsyncro 
	     /*sStm.append("select id_oggetto_file,");
	     sStm.append("o.id_formato,filename,\"FILE\",testoocr,allegato,id_oggetto_file_padre,NVL(F.VISIBILE,'S'), nvl(PATH_FILE,''),");
	     sStm.append("NVL(F.ICONA, 'generico.gif'),TO_NUMBER(NULL),nvl(o.DA_CANCELLARE,'N') dacancellare,to_char(o.DATA_AGGIORNAMENTO,'dd/MM/yyyy HH24:mi:ss') dataAgg, ");
	     sStm.append("'' impostsyncto, null id_syncro ");
	     sStm.append(" from oggetti_file o, formati_file f");
	     sStm.append(" where id_documento = " +idDocumento );
	     sStm.append(" and o.ID_FORMATO = f.ID_FORMATO");
	     sStm.append(" order by filename");*/
	     
	     return sStm.toString();
  }  

  // ***************** METODI DI UTILITA' SUI DOCUMENTI ***************** //
  
  /*
   * METHOD:      long getIdTipodoc(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: ar -> AREA
   *              cm -> CODICE MODELLO
   *              
   *              Restituisce l'idTipoDocumento a partire da "ar" e "cm"
   * 
   * RETURN:      String
  */
  public String getIdTipoDoc(String ar, String cm) throws Exception
  {      
         ResultSet       rst = null;
         String          query,idtipodoc = null,codmod = null;

         query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE "+
                 "FROM MODELLI "+
                 "WHERE AREA = :AREA "+
                 "AND CODICE_MODELLO = :CM";
         try {
           //GET IDTIPODOC DAL MODELLO/AREA
           dbOp = connect();

           dbOp.setStatement(query);
           dbOp.setParameter(":AREA",ar);
           dbOp.setParameter(":CM",cm);
           dbOp.execute();
           rst = dbOp.getRstSet();

           if (rst.next() ) {
              idtipodoc = rst.getString("ID_TIPODOC");
              codmod = rst.getString("CODICE_MODELLO_PADRE");
           } 
           else 
              idtipodoc = "";
      
           //IL MODELLO DATO IN INPUT E' UN MODELLO FIGLIO
           //EFFETTUO LA QUERY SUL PADRE PER RECUPERARE 
           //L'IDTIPODOC
           if (idtipodoc == null) {
              idtipodoc = "";
            
              dbOp.setStatement(query);
              if (idtipodoc.equalsIgnoreCase("")) {
                 dbOp.setParameter(":AREA",ar);
                 dbOp.setParameter(":CM",codmod);
                 dbOp.execute();
                 rst = dbOp.getRstSet();
      
                 if (rst.next() ) 
                    idtipodoc = rst.getString("ID_TIPODOC");              
              }
           }
      
           close();
           return idtipodoc;
         } 
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getIdTipoDoc - (area/codiceModello) (" + ar + "/" + cm + ")\n" + e.getMessage());
         }
  }
  
  /*
   * METHOD:      long getNomiCampi(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: ar -> AREA
   *              cm -> CODICE MODELLO
   *              
   *              Restituisce l'elenco dei nomi di campo del modello
   * 
   * RETURN:      String
  */
  public Vector getNomiCampi(String ar, String cm) throws Exception
  {      
         ResultSet       rst = null;
         String          query,nome = null;
         Vector<String>  vNomiCampi = new Vector<String>();

         query =  "select nome ";
         query += "from campi_documento,";
         query += "modelli,";
         query += "dati_modello ";
         query += "where modelli.AREA=:AREA and modelli.CODICE_MODELLO=:CM and modelli.ID_TIPODOC=campi_documento.ID_TIPODOC and ";
         query += "modelli.AREA=dati_modello.AREA and modelli.CODICE_MODELLO=dati_modello.CODICE_MODELLO and dati_modello.dato=campi_documento.nome ";            
         query += "and nvl(in_uso,'N')='Y'";
         
         try {
           //GET IDTIPODOC DAL MODELLO/AREA
           dbOp = connect();

           dbOp.setStatement(query);
           dbOp.setParameter(":AREA",ar);
           dbOp.setParameter(":CM",cm);
           dbOp.execute();
           rst = dbOp.getRstSet();

           while (rst.next() ) {
              vNomiCampi.add(rst.getString("nome"));             
           }            
           
           close();
           return vNomiCampi;
         } 
         catch (Exception e) {
           close();
           throw new Exception("DocUtil::getNomiCampi - (area/codiceModello) (" + ar + "/" + cm + ")\n" + e.getMessage());
         }
  }  
  
  /*
   * METHOD:      String getIdDocumento(String,String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: ar     -> AREA
   *              cm     -> CODICE MODELLO
   *              codric -> CODICE RICHIESTA 
   *              
   *              Restituisce idDocumento a partire da "ar", "cm", "codric"
   * 
   * RETURN:      String
  */
  public  String getIdDocumento(String ar, String cm, String codric) throws Exception
  {      
         //Recupero l'id del tipo documento
         String idTipoDoc = getIdTipoDoc(ar,cm);        
         
         String idDoc     = getIdDocumento(idTipoDoc, codric);  
         return idDoc;
  }

  /*
   * METHOD:      String getIdDocumento(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idTipoDoc -> ID TIPO DOCUMENTO
   *              codric -> CODICE RICHIESTA 
   *              
   *              Restituisce idDocumento a partire da "ar", "cm" e "codric"
   * 
   * RETURN:      String
  */
  public  String getIdDocumento (String idTipoDoc, String codric) throws Exception
  {        
         ResultSet       rst = null;
         String          query;
         String          idDoc = "";
    
         query = "SELECT ID_DOCUMENTO "+
                 "FROM DOCUMENTI "+
                 "WHERE ID_TIPODOC = :IDTIPODOC "+
                 "AND CODICE_RICHIESTA = :CR";
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
           dbOp.setParameter(":IDTIPODOC",idTipoDoc);
           dbOp.setParameter(":CR",codric);
           dbOp.execute();
           
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              idDoc = rst.getString("ID_DOCUMENTO");
               
           close();
           return idDoc;
         } 
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getIdDocumento - (tipodoc/codric) (" + idTipoDoc + "/" + codric + ")\n" + e.getMessage());
         }
  }

  /*
   * METHOD:      String getIdTipoDocByIdDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO 
   *              
   *              Restituisce Id_Tipodocumento a partire da "idDoc"
   * 
   * RETURN:      String
  */
  public  String getIdTipoDocByIdDocumento (String idDoc) throws Exception
  {       
         ResultSet       rst = null;
         String          query;
         String          idTipoDoc = "";
    
         query = "SELECT ID_TIPODOC "+
                 "FROM DOCUMENTI "+
                 "WHERE ID_DOCUMENTO = :IDDOC ";
                
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
           
           dbOp.setParameter(":IDDOC", Long.parseLong(idDoc));
           dbOp.execute();
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
               idTipoDoc = rst.getString("ID_TIPODOC");
        
           close();
           return idTipoDoc;
         } 
         catch (Exception e) {
           close();
           throw new Exception("DocUtil::getIdTipoDocByIdDocumento - (idDoc) (" + idDoc + ")\n" + e.getMessage());
        }
  }  
  
  // ***************** METODI DI UTILITA' PER ESTRARRE DATI A PARTIRE DAI DOCUMENTI **************** //

  /*
   * METHOD:      String getAreaByIdDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO 
   *              
   *              Restituisce Area a partire da "idDoc"
   * 
   * RETURN:      String
  */
  public  String getAreaByIdDocumento(String idDoc) throws Exception
  {        
         ResultSet       rst = null;
         String          query;
         String          area = "";
    
         query = "SELECT AREA "+
                 "FROM DOCUMENTI "+
                 "WHERE ID_DOCUMENTO = "+idDoc;
         try {

           dbOp = connect();

           dbOp.setStatement(query);     

           dbOp.execute();
           rst = dbOp.getRstSet();

           if (rst.next() ) 
              area = rst.getString("AREA");

           close();
           return area;
        } 
        catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getAreaByIdDocumento - (idDoc) (" + idDoc + ")\n" + e.getMessage());
        }
  }    

  /*
   * METHOD:      String getAreaCartellaOrQuery(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idCartOrQuery -> ID CARTELLA O QUERY
   *              tipo          -> Q = Query
   *                               C = Cartella
   *              
   *              Restituisce Area a partire da "idDoc"
   * 
   * RETURN:      String
  */
  public  String getAreaCartellaOrQuery(String idCartOrQuery,String tipoOggetto) throws Exception
  {        
         ResultSet       rst = null;
         String          query;
         String          area = "";

         if(tipoOggetto.equals("W"))
         {
        	 query = "SELECT AREA "+
             "FROM DOCUMENTI, CARTELLE "+
             "WHERE ID_DOCUMENTO = ID_DOCUMENTO_PROFILO and "+
             " ID_CARTELLA = " +idCartOrQuery;
         }
         else
         {
        	 if (idCartOrQuery.indexOf("-")==-1) { 
                 query = "SELECT AREA "+
                 "FROM DOCUMENTI, CARTELLE "+
                 "WHERE ID_DOCUMENTO = ID_DOCUMENTO_PROFILO and "+
                 " ID_CARTELLA = " +idCartOrQuery;
	         }
	         else {
	            idCartOrQuery=idCartOrQuery.substring(1,idCartOrQuery.length());
	            query = "SELECT AREA "+
	                 "FROM DOCUMENTI, QUERY "+
	                 "WHERE ID_DOCUMENTO = ID_DOCUMENTO_PROFILO and "+
	                 "ID_QUERY = "+ idCartOrQuery;
	         }
         }
        	 
         try {

           dbOp = connect();

           dbOp.setStatement(query);     

           dbOp.execute();
           rst = dbOp.getRstSet();

           if (rst.next() ) {

              area = rst.getString("AREA");
           }
           close();
           return area;
        } 
        catch (Exception e) {
           close();
           throw new Exception("DocUtil::getAreaCartellaOrQuery - (idCartOrQuery) (" + idCartOrQuery+")\n" + e.getMessage());
        }
  }


  /*
   * METHOD:      String getModelloByIdDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO 
   *              
   *              Restituisce Codice Modello a partire da "idDoc"
   * 
   * RETURN:      String
  */
  public  String getModelloByIdDocumento (String idDoc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          codMod = "";
    
         query = "SELECT CODICE_MODELLO "+
                "FROM DOCUMENTI, MODELLI "+
                "WHERE ID_DOCUMENTO = "+idDoc+
                " AND MODELLI.ID_TIPODOC=DOCUMENTI.ID_TIPODOC";
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);     
           dbOp.execute();
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              codMod = rst.getString("CODICE_MODELLO");
        
           close();
           return codMod;
         }
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getModelloByIdDocumento - (idDoc) (" + idDoc + ")\n" + e.getMessage());
         }
  }

  /*
   * METHOD:      String getCrByIdDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO 
   *              
   *              Restituisce Codice Richiesta a partire da "idDoc"
   * 
   * RETURN:      String
  */
  public  String getCrByIdDocumento(String idDoc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          cr = "";
    
         query = "SELECT CODICE_RICHIESTA "+
                 "FROM DOCUMENTI "+
                 "WHERE ID_DOCUMENTO = "+idDoc;
                
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);     
           dbOp.execute();
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              cr = rst.getString("CODICE_RICHIESTA");
         
           close();
           return cr;
         }
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getCrByIdDocumento - (idDoc) (" + idDoc + ")\n" + e.getMessage());
         }
  }

  /*
   * METHOD:      String getAreaCmCrByIdDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO 
   *              
   *              Restituisce la terna AREA@CM@CR a partire da "idDoc"
   * 
   * RETURN:      String
  */
  public  String getAreaCmCrByIdDocumento(String idDoc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          terna = "";
    
         query = "SELECT d.AREA||'@'||CODICE_MODELLO||'@'||CODICE_RICHIESTA terna "+
                 "FROM DOCUMENTI d, MODELLI m "+
                 "WHERE ID_DOCUMENTO = "+idDoc+" AND m.ID_TIPODOC=d.ID_TIPODOC";      
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);     
           dbOp.execute();
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              terna = rst.getString("terna");
         
           close();
           return terna;
         }
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getAreaCmCrByIdDocumento - (idDoc) (" + idDoc + ")\n" + e.getMessage());
         }
  }
  
  
  
  // ***************** METODI DI UTILITA' PER LE CARTELLE **************** //
  
  /*
   * METHOD:      String getIdCartellaByIdViewCartella(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idw -> ID VIEW 
   *              
   *              Restituisce Id_Cartella a partire da "idw"
   * 
   * RETURN:      String
  */  
  public  String getIdCartellaByIdViewCartella (String idw) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          idCart = "";
    
         query =  "SELECT F_Idcartella_Idview("+idw+") idc ";
         query += "FROM DUAL";
         
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
      
           dbOp.execute();
         
           rst = dbOp.getRstSet();
      
           if (rst.next()) 
              idCart = rst.getString("idc");
        
           close();
           return idCart;
         } 
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getIdDocumentoByIdViewDocumento - (idw) (" + idw + ")\n" + e.getMessage());
         }
  }

  /*
   * METHOD:      String getIdViewCartellaByIdCartella(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idc -> ID CARTELLA 
   *              
   *              Restituisce Id_View a partire da "idc"
   * 
   * RETURN:      String
  */  
  public  String getIdViewCartellaByIdCartella(String idc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          idw = "";
    
         query =  "SELECT F_IDVIEW_CARTELLA("+idc+") idc ";
         query += "FROM DUAL";
      
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
      
           dbOp.execute();
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              idw = rst.getString("idc");
        
           close();
           return idw;
       } 
       catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getIdViewCartellaByIdCartella - (idc) (" + idc + ")\n" + e.getMessage());
       }
  }

  /*
   * METHOD:      String getCodiceModelloFromIdCartella(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idc -> ID CARTELLA 
   *              
   *              Restituisce Codice Modello a partire da "idc"
   * 
   * RETURN:      String
  */ 
  public  String getCodiceModelloFromIdCartella (String idc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          codMod = "";
    
         query =  "SELECT F_CODICEMOD_FROM_IDCARTELLA('"+idc+"') cm ";
         query += "FROM DUAL";
         
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
      
           dbOp.execute();
         
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              codMod = rst.getString(1);
        
           close();
           return codMod;
        }
        catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getCodiceModelloFromIdCartella - (idc) (" + idc + ")\n" + e.getMessage());
        }
  }

  /*
   * METHOD:      String getCodiceModelloFromIdQuery(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idc -> ID CARTELLA 
   *              
   *              Restituisce Codice Modello a partire da "idq"
   * 
   * RETURN:      String
  */ 
  public  String getCodiceModelloFromIdQuery (String idq) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          codMod = "";
    
         query =  "SELECT F_CODICEMOD_FROM_IDQUERY("+idq+") cm ";
         query += "FROM DUAL";
         
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
      
           dbOp.execute();
         
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              codMod = rst.getString("cm");
        
           close();
           return codMod;
        }
        catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getCodiceModelloFromIdQuery - (idq) (" + idq + ")\n" + e.getMessage());
        }
  }

  /*
   * METHOD:      String getCodiceModelloFromIdTipoDoc(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idc -> ID CARTELLA 
   *              
   *              Restituisce Codice Modello a partire da "idq"
   * 
   * RETURN:      String
  */ 
  public  String getCodiceModelloFromIdTipoDoc (String itipoDoc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          codMod = "";
    
         query =  "SELECT codice_modello cm ";
         query += "FROM MODELLI ";
         query += "WHERE ID_TIPODOC="+itipoDoc;
         
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
      
           dbOp.execute();
         
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              codMod = rst.getString("cm");
        
           close();
           return codMod;
        }
        catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getCodiceModelloFromIdTipoDoc - (itipoDoc) (" + itipoDoc + ")\n" + e.getMessage());
        }
  }  
  
  // ***************** METODI DI UTILITA' PER I DOCUMENTI-CARTELLA **************** //

  /*
   * METHOD:      String getIdDocumentoByAreaCmCr(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: area   -> AREA
   *              cm     -> CODICE MODELLO
   *              codric -> CODICE RICHIESTA 
   *              
   *              Restituisce idDocumento a partire da "codice richiesta"
   *              "area" e "codice modello"
   * 
   * RETURN:      String
  */
  public  String getIdDocumentoByAreaCmCr(String area, String cm, String codric) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          idDoc = "";
    
         query = "SELECT ID_DOCUMENTO "+
                 "FROM DOCUMENTI, TIPI_DOCUMENTO "+
                 "WHERE AREA = '"+area+ "'"+
                 "AND CODICE_RICHIESTA = '"+codric+"'"+
                 "AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC "+
                 "AND TIPI_DOCUMENTO.NOME = (select nvl(codice_modello_padre,codice_modello) from modelli where codice_modello='"+cm+"' and area='"+area+"') ";
           
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
                  
           dbOp.execute();
            
           rst = dbOp.getRstSet();
      
           if (rst.next() ) {           
              idDoc = ""+rst.getLong("ID_DOCUMENTO");
           }

           close();      
           return idDoc;
         }
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getIdDocumentoByAreaCmCr - "+
                               "(area, codmod, codric) (" + area + " , " + cm + " , " + codric + ")\n" + e.getMessage());
         }
  }
  
  /*
   * METHOD:      String getStatoByIdDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: id   -> ID_DOCUMENTO
   *               
   *              Restituisce lo stato del documento
   * 
   * RETURN:      String
  */
  public  String getStatoByIdDocumento(String id) throws Exception
  {
          ResultSet       rst = null;
          String          query;
          String          stato = "BO";
    
          query = "SELECT STATO_DOCUMENTO "+
          		  "FROM DOCUMENTI "+
          		  "WHERE ID_DOCUMENTO = "+id;  
          try {
           dbOp = connect();
      
           dbOp.setStatement(query);
                  
           dbOp.execute();
            
           rst = dbOp.getRstSet();
      
           if (rst.next() ) {           
        	   stato = rst.getString("STATO_DOCUMENTO");
           }

           close();      
           return stato;
          }
          catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getStatoByIdDocumento - "+"(idDocumento) (" + id + ")\n" + e.getMessage());
          }
  }

  
  /*
   * METHOD:      String getIdDocumentoByCr(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: codric -> CODICE RICHIESTA 
   *              
   *              Restituisce idDocumento a partire da "codric"
   *              Utilizzato dalle cartelle e query il cui idDocProfiloRiferito 
   *              è conosciuta sulle relative tabelle
   * 
   * RETURN:      String
  */
  public  String getIdDocumentoByCr(String codric) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         String          idDoc = "";
        
         //è una cartella
         if (codric.indexOf("-")==-1 || codric.indexOf("WRKSP")!=-1) {    
        	 if (codric.indexOf("WRKSP")!=-1)
        		 codric=codric.substring("WRKSP".length(),codric.length());
        		 
             query = "SELECT ID_DOCUMENTO_PROFILO "+
                     "FROM CARTELLE "+
                     "WHERE ID_CARTELLA= :CR";
         }
         //è una query
         else {
           codric=codric.substring(1,codric.length());
           query = "SELECT ID_DOCUMENTO_PROFILO "+
                     "FROM QUERY "+
                     "WHERE ID_QUERY = :CR";
         }
         
         try {
           dbOp = connect();
      
           dbOp.setStatement(query);
           dbOp.setParameter(":CR",codric);
           dbOp.execute();
           rst = dbOp.getRstSet();
      
           if (rst.next() ) 
              idDoc = rst.getString("ID_DOCUMENTO_PROFILO");

           close();      
           return idDoc;
         }
         catch (Exception e) {
           close();
           throw new Exception("GestioneDocumenti::getIdDocumentoByCr - (codric) (" + codric + ")\n" + e.getMessage());
         }
  }

  /*
   * METHOD:      String isDocumentoCartella(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO
   *              
   *              Verifica se il documento è un documento-cartella 
   *              sfruttando l'area fissa "GDMSYS"
   * 
   * RETURN:      boolean
  */ 
  public  boolean isDocumentoCartella(String idDoc) throws Exception
  {
         ResultSet       rst = null;
         String          query;
         boolean         bRet=false;
    
        query = "SELECT 1 "+
                "FROM CARTELLE "+
                "WHERE ID_DOCUMENTO_PROFILO = :IDDOC ";
        try {
          dbOp = connect();
    
          dbOp.setStatement(query);
          dbOp.setParameter(":IDDOC",idDoc);
          dbOp.execute();
          rst = dbOp.getRstSet();                  
          
          if (rst.next()) 
        	  bRet=true;
          else
        	  bRet=false;
      
          close();
          
          return bRet;
        
        } catch (Exception e) {
          close();
          throw new Exception("GestioneDocumenti::isDocumentoCartella - (idDoc) (" + idDoc + ")\n" + e.getMessage());
        }
  }
  
  /*
   * METHOD:      String isDocumentoQuery(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idDoc -> ID DOCUMENTO
   *              
   *              Verifica se il documento è un documento-query 
   *              sfruttando l'area fissa "GDMSYS"
   * 
   * RETURN:      boolean
  */ 
  public  boolean isDocumentoQuery(String idDoc) throws Exception
  {
          ResultSet       rst = null;
          String          query;
          boolean         bRet=false;
    
          query = "SELECT 1 "+
                  "FROM QUERY "+
                  "WHERE ID_DOCUMENTO_PROFILO = :IDDOC ";
          try {
           dbOp = connect();
    
           dbOp.setStatement(query);
           dbOp.setParameter(":IDDOC",idDoc);
           dbOp.execute();
           rst = dbOp.getRstSet();                  
          
           if (rst.next()) 
        	  bRet=true;
           else
        	  bRet=false;
      
           close();
          
           return bRet;
        
          } catch (Exception e) {
            close();
          throw new Exception("GestioneDocumenti::isDocumentoQuery - (idDoc) (" + idDoc + ")\n" + e.getMessage());
        }
  }
  
  public void cancellaAllegatoTempPercorsi(String area, String cm, String cr, String nf, 
		  								   String user, String stato,
		  								   boolean bDeleteOld,
		  								   boolean bCommit,
		  								   boolean bCloseDbOp) throws Exception {	
	  	 ResultSet    rst  = null;   
	  	 StringBuffer sStm = new StringBuffer("SELECT ");
	     	     
	     sStm.append("AREA, CODICE_MODELLO, CODICE_RICHIESTA, NOMEFILE,PERCORSO,PERCORSO_ROOT, PERCORSO_NOFILE ");
	     sStm.append("FROM ALLEGATI_TEMP_PERCORSI ");
	     if (!bDeleteOld) {
	    	 sStm.append("WHERE ALLEGATI_TEMP_PERCORSI.AREA = :P_AREA  ");
	    	 sStm.append("  AND ALLEGATI_TEMP_PERCORSI.CODICE_MODELLO = :P_CM  ");
	    	 sStm.append("  AND ALLEGATI_TEMP_PERCORSI.CODICE_RICHIESTA = :P_CR  ");
	    	 if (nf!=null) sStm.append("  AND ALLEGATI_TEMP_PERCORSI.NOMEFILE = :P_NF ");
	    	 if (user!=null) sStm.append("  AND ALLEGATI_TEMP_PERCORSI.UTENTE_AGGIORNAMENTO = :P_USER ");
	    	 if (stato!=null) sStm.append("  AND ALLEGATI_TEMP_PERCORSI.STATO = :P_STATO ");
	     }
	     else {
	    	 sStm.append("WHERE trunc(sysdate - ALLEGATI_TEMP_PERCORSI.DATA_AGGIORNAMENTO )>=1  ");
	     }
	     
	     try {
	    	 dbOp = connect();
	    	 
	    	 dbOp.setStatement(sStm.toString());
	    	 if (!bDeleteOld) {
	    		 dbOp.setParameter(":P_AREA",area);
	    		 dbOp.setParameter(":P_CM",cm);
	    		 dbOp.setParameter(":P_CR",cr);
	    		 if (nf!=null) dbOp.setParameter(":P_NF",nf);
	    		 if (user!=null) dbOp.setParameter(":P_USER",user);
	    		 if (stato!=null) dbOp.setParameter(":P_STATO",stato);
	    	 }	    	    		 
	         dbOp.execute();
	         rst = dbOp.getRstSet(); 
	         
	         ArrayList<Allegati_temp_percorsi> listaAllPercorsi = new ArrayList<Allegati_temp_percorsi>();
	         while(rst.next()) listaAllPercorsi.add(new Allegati_temp_percorsi(rst.getString(1),
	        		 														   rst.getString(2),
	        		 														   rst.getString(3),
	        		 														   rst.getString(4),
	        		 														   rst.getString(5),
	        		 														   rst.getString(6),
	        		 														   rst.getString(7)
	         															       ));
	         
	         for(int i=0;i<listaAllPercorsi.size();i++)	{
	        	 Allegati_temp_percorsi alltmppercorsi = listaAllPercorsi.get(i);
	        	 	        	 
	        	 sStm = new StringBuffer("DELETE FROM ALLEGATI_TEMP_PERCORSI ");
	        	 sStm.append(" WHERE ALLEGATI_TEMP_PERCORSI.AREA = :P_AREA AND ");
		    	 sStm.append("   ALLEGATI_TEMP_PERCORSI.CODICE_MODELLO = :P_CM AND ");
		    	 sStm.append("   ALLEGATI_TEMP_PERCORSI.CODICE_RICHIESTA = :P_CR AND ");
		    	 sStm.append("   ALLEGATI_TEMP_PERCORSI.NOMEFILE = :P_NF ");
		    	 
		    	 dbOp.setStatement(sStm.toString());
		    	 dbOp.setParameter(":P_AREA",alltmppercorsi.getArea());
	    		 dbOp.setParameter(":P_CM",alltmppercorsi.getCm());
	    		 dbOp.setParameter(":P_CR",alltmppercorsi.getCr());
	    		 dbOp.setParameter(":P_NF",alltmppercorsi.getNf());		
	    		 
	    		 try {
	    			 dbOp.execute();
	    		 } catch (Exception e) {
	    			 throw new Exception("Errore in eliminazione da tabella ALLEGATI_TEMP_PERCORSI. Errore="+e.getMessage());
	    		 }
	    		 
	    		 if (bDeleteOld) {
		    		 //Elimino anche sulla tabella ALLEGATI_TEMP (anche se nella maggior parte dei casi il record sarà già
		    		 //stato eliminato... ma se sto eliminanto  dalla servlet di pulizia (bDeleteOld a true)
		    		 //va cmq eliminato qui quello dell'allegati_temp
		    		 sStm = new StringBuffer("DELETE FROM ALLEGATI_TEMP ");
		        	 sStm.append(" WHERE ALLEGATI_TEMP.AREA = :P_AREA AND ");
			    	 sStm.append("   ALLEGATI_TEMP.CODICE_MODELLO = :P_CM AND ");
			    	 sStm.append("   ALLEGATI_TEMP.CODICE_RICHIESTA = :P_CR AND ");
			    	 sStm.append("   ALLEGATI_TEMP.NOMEFILE = :P_NF ");
			    	 
			    	 dbOp.setStatement(sStm.toString());
			    	 dbOp.setParameter(":P_AREA",alltmppercorsi.getArea());
		    		 dbOp.setParameter(":P_CM",alltmppercorsi.getCm());
		    		 dbOp.setParameter(":P_CR",alltmppercorsi.getCr());
		    		 dbOp.setParameter(":P_NF",alltmppercorsi.getNf());		
		    		 
		    		 try {
		    			 dbOp.execute();
		    		 } catch (Exception e) {
		    			 throw new Exception("Errore in eliminazione da tabella ALLEGATI_TEMP. Errore="+e.getMessage());
		    		 }	    		 
	    		 }
	    		 
	    		 File f = new File(alltmppercorsi.getPercorso());
	    		 if (f.exists())
	    			 FileSystemUtility.deleteAllPathifisEmpty(alltmppercorsi.getPercorso(), alltmppercorsi.getPercorso_root());
	    		 else
	    			 FileSystemUtility.deleteAllPathifisEmpty(alltmppercorsi.getPercorso_nofile(), alltmppercorsi.getPercorso_root());
	        	 if ( bCommit) try{dbOp.commit();} catch (Exception ei) {}
	         }
	         	         
	         if (bCloseDbOp) close();
	     } catch (Exception e) {	
	    	 if ( bCommit) try{dbOp.rollback();} catch (Exception ei) {}
	    	 if (bCloseDbOp) close();
             throw new Exception("GestioneDocumenti::cancellaAllegatoTempPercorsi - (area,cm,cr,nf,user,stato,bDeleteOld) ("
            		 			  + area + ","+cm+","+cr+","+nf+","+user+","+stato+","+bDeleteOld+"  )\n" + e.getMessage());
         }  
  }
  
  
  public String listaAllegatiTemp(String idDoc, String separator) throws Exception {
	  	 String 	  ret  = "";	
	  	 ResultSet    rst  = null;   
	  	 StringBuffer sStm = new StringBuffer("SELECT ");
	     	     
	     sStm.append("NOMEFILE ");
	     sStm.append("FROM ALLEGATI_TEMP,DOCUMENTI,TIPI_DOCUMENTO ");
	     sStm.append("WHERE ALLEGATI_TEMP.AREA=DOCUMENTI.AREA AND ");
	     sStm.append("      ALLEGATI_TEMP.CODICE_MODELLO=TIPI_DOCUMENTO.NOME AND ");
	     sStm.append("      ALLEGATI_TEMP.CODICE_RICHIESTA=DOCUMENTI.CODICE_RICHIESTA AND ");
	     sStm.append("      DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND ");
	     sStm.append("      DOCUMENTI.ID_DOCUMENTO=:IDDOC");
	     
	     try {
		     dbOp = connect();
		     
	         dbOp.setStatement(sStm.toString());
	         dbOp.setParameter(":IDDOC",idDoc);
	         dbOp.execute();
	         rst = dbOp.getRstSet();       
	         
	         int i=0;
	         while(rst.next()) {
	        	  if (i>0) ret+=separator;
	        	  ret+=rst.getString(1);
	        	  
	        	  i++;
	         }
	         
		     close();
	         return ret;
	         
         } catch (Exception e) {
             close();
             throw new Exception("GestioneDocumenti::listaAllegatiTemp - (idDoc) (" + idDoc + ")\n" + e.getMessage());
         }         
  }
  
  private IDbOperationSQL connect() throws Exception {
        if (en.getDbOp()==null) 
           return (new ManageConnection(en.Global)).connectToDB();        
        
        return en.getDbOp();
  }
  
  private void close() throws Exception {
          if (en.getDbOp()==null) (new ManageConnection(en.Global)).disconnectFromDB(dbOp,true,false);
  }

}
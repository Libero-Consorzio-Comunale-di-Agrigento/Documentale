package it.finmatica.dmServer;

/*
 * GESTIONE DEI DOCUMENTI
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
 
import java.sql.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.Environment;

public class GD4_Documento_XML  extends A_Documento_XML
{
   Environment env = null;
   String xmlDocument = "";
   String xmlNote     = "";
  
 // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
  
  /*
   * METHODS:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */  
  
  public GD4_Documento_XML(String idDoc, Environment newEnv)
  {
    idDocumento = idDoc;
    env=newEnv;    
  }
  
  public GD4_Documento_XML(Vector vDoc, String note, Environment newEnv)
  {
    documenti = vDoc;
    xmlNote   = note;
    env=newEnv;
  }
  
  /*
   * METHOD:      getDocumentoXML()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce un oggetto documento XML 
   *              
   * RETURN:      Object
  */ 
  public Object getDocumentoXML()throws Exception
  {
         return XMLUtil.read_String_Xml(visualizza());
  }
  
  /*
   * METHOD:      visualizza()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Visualizza il documento XML 
   * 
   * RETURN:      String
  */  
  public  String visualizza() throws Exception 
  {   
          if (xmlNote.equals(""))
             xmlDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <DOC_INFO>";
          else
             xmlDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <DOC_INFO sql=\"" + xmlNote + "\">";
      
           if (idDocumento != null)
              xmlDocument += visualizza(idDocumento);
          else if (documenti != null)
               xmlDocument += visualizza(documenti);            
          
          xmlDocument +="</DOC_INFO>";
          return xmlDocument ;
  }
  
  /*
   * METHOD:      visualizza(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Visualizza il documento XML 
   *              dato id Documento
   * 
   * RETURN:      String
  */ 
  private  String visualizza(String idDoc) throws Exception 
  {
     return retrieve(idDoc, 1);
  }
  
  /*
   * METHOD:      visualizza(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Visualizza il documento XML 
   *              dato vettore Documento
   * 
   * RETURN:      String
  */ 
  private  String visualizza(Vector vDoc) throws Exception 
  {
     int size = documenti.size();
     String listaDocumenti = "";
     for(int i=0;i<size;i++) 
     {          
         listaDocumenti +=  retrieve(documenti.elementAt(i).toString(), i+1);                              
     }
     return listaDocumenti;
  }
  
  /*
   * METHOD:      retrieve(String, int)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica un documento dal DB
   *              e ne genera il relativo file XML  
   * 
   * RETURN:      String
  */  
  private String retrieve(String idDoc, int rowcount) throws Exception 
  {
         String xmlDoc = ""; 
         try {           
           IDbOperationSQL dbOp;

           String sQuery = "SELECT T.NOME FROM DOCUMENTI D, TIPI_DOCUMENTO T "+
                           "WHERE T.ID_TIPODOC = D.ID_TIPODOC "+
                           "AND D.ID_DOCUMENTO = " + idDoc ;

           dbOp = env.getDbOp();

           dbOp.setStatement(sQuery);

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {
              xmlDoc =  "<DOCUMENTO rowcount = \""+rowcount+"\" tipo_documento=\""+rst.getString(1)+"\">";
              rst.close();
              // SEZIONE VALORI  
              xmlDoc += retrieveValori(idDoc);  
              // SEZIONE OGGETTI FILE 
              xmlDoc += retrieveOggettiFile(idDoc);
              // SEZIONE RIFERIMENTI   
              xmlDoc += retrieveRiferimenti(idDoc);  
              // SEZIONE COMPETENZE
              xmlDoc += retrieveCompetenze(idDoc);
              return xmlDoc+"</DOCUMENTO>";
           }
           else {              
              throw new Exception("GD4_Documento_XML::retrieve() -> Select fallita per idDocumento: " + idDoc);                   
           }
         }
         catch (Exception e) {               
               throw new Exception("GD4_Documento_XML::retrieve()\n" + e.getMessage());
         }

  }

  /*
   * METHOD:      retrieveValori(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica i valori obbligatori del
   *              documento dato id Documento
   *              
   * RETURN:      String
  */  
  private String retrieveValori(String idDoc) throws Exception 
  {
          /*<VALORI>
              <ITEM nome="xx" tipo="S">valore</ITEM>
            </VALORI>
          */
          String xmlValori = "<VALORI>";
          try {
            IDbOperationSQL dbOp;

            String sQuery = "select cd.NOME, d.tipo, "+  
                            " NVL(dbms_lob.substr(VALORE_CLOB), NVL(TO_CHAR(VALORE_DATA, 'dd/mm/yyyy'),to_char(VALORE_NUMERO) ))"+
                            "  from valori v, campi_documento cd , dati_modello dm, dati d" +
                            " where v.id_campo = cd.id_campo" +
                            "   and cd.id_campo = dm.id_campo"+
                            "   and dm.AREA_DATO = d.area"+
                            "   and dm.DATO = d.dato"+
                            "   and v.id_documento = " + idDoc +
                            "   and cd.FLAG_VIEW = 'S'" ;
            
            dbOp = env.getDbOp();

            dbOp.setStatement(sQuery);

            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                xmlValori += "<ITEM nome=\""+rst.getString(1)+"\" tipo=\""+rst.getString(2)+"\">"+rst.getString(3)+"</ITEM>";
            }

            xmlValori += "</VALORI>";
          return xmlValori;
          }
          catch (Exception e) {
              throw new Exception("GD4_Documento_XML::retrieveValori() " + e.getMessage());
          }
  }

  /*
   * METHOD:      retrieveOggettiFile(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti gli oggetti file del
   *              documento dato id Documento 
   *              
   * RETURN:      String
  */  
  private String retrieveOggettiFile(String idDoc) throws Exception 
  {
          /*
           * <ALLEGATI>
           *     <ITEM chiave="yy">nome_allegato</ITEM>
           *  </ALLEGATI>
          */
          String xmlAllegati= "<ALLEGATI>";
                              
          try {          
            IDbOperationSQL dbOp;

            String sQuery = "select id_oggetto_file, FILENAME from oggetti_file "+ 
                            " where id_documento = " + idDoc  ;
                      
            dbOp = env.getDbOp();
            
            dbOp.setStatement(sQuery);

            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                xmlAllegati += "<ITEM chiave=\""+rst.getString(1)+"\">"+rst.getString(2)+"</ITEM>";
            }

            xmlAllegati += "</ALLEGATI>";
          return xmlAllegati;
          }
          catch (Exception e) {
              throw new Exception("GD4_Documento_XML::retrieveOggettiFile() " + e.getMessage());
          }
  }

  /*
   * METHOD:      retrieveRiferimenti(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica i riferimenti del
   *              documento dato id Documento 
   *              
   * RETURN:      String
  */  
  private String retrieveRiferimenti(String idDoc) throws Exception 
  {
          /*
           *  <RIFERIMENTI>
           *      <ITEM riferimento="XXX" tipo="Y"> </ITEM>
           *  </RIFERIMENTI>
          */

          String xmlRiferimenti= "<RIFERIMENTI>";
          try {
            IDbOperationSQL dbOp;

            String sQuery = "select ID_DOCUMENTO_RIF, TIPO_RELAZIONE from riferimenti "+ 
                            " where id_documento = " + idDoc  ;
                      
            dbOp = env.getDbOp();
            
            dbOp.setStatement(sQuery);

            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                xmlRiferimenti += "<ITEM riferimento=\""+rst.getString(1)+"\" tipo=\""+rst.getString(2)+"\"></ITEM>";
            }
            xmlRiferimenti += "</RIFERIMENTI>";
            return xmlRiferimenti;
          }
          catch (Exception e) {
               throw new Exception("GD4_Documento_XML::retrieveRiferiementi() " + e.getMessage());
          }
  }

  /*
   * METHOD:      retrieveCompetenze(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica le competenze del
   *              documento dato id Documento 
   *              
   * RETURN:      String
  */  
  private String retrieveCompetenze(String idDoc) throws Exception 
  {
          /*
           *  <COMPETENZE>
           *      ITEM riferimento="XXX" tipo="Y"> </ITEM>
           *  </COMPETENZE>
          */
           String xmlRiferimenti= "<COMPETENZE>";
          try {

            IDbOperationSQL dbOp;

            String sQuery = "select UTENTE, RUOLO, tip_abi.DESCRIZIONE,   to_char(dal, 'dd/mm/yyyy') , decode(AL,null,'continua', to_char(al, 'dd/mm/yyyy')) "+
                            "  from SI4_COMPETENZE comp,"+
                            "       SI4_ABILITAZIONI abil,"+
                            "       SI4_TIPI_ABILITAZIONE tip_abi,"+
                            "       SI4_TIPI_OGGETTO tip_obj"+
                            " where comp.id_abilitazione=abil.id_abilitazione "+
                            "   and tip_abi.ID_TIPO_ABILITAZIONE=abil.ID_TIPO_ABILITAZIONE "+
                            "   and tip_obj.ID_TIPO_OGGETTO=abil.ID_TIPO_OGGETTO "+
                            "   and abil.ID_TIPO_OGGETTO = 32  "+
                            "   and sysdate between DAL and nvl(al, sysdate+1)"+
                            "   and ACCESSO = 'S'"+
                            "   and oggetto = " + idDoc+
                            "   order by 1 "  ;

            dbOp = env.getDbOp();
            
            dbOp.setStatement(sQuery);

            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                xmlRiferimenti += "<ITEM utente=\""+rst.getString(1)+"\" ruolo=\""+rst.getString(2)+"\""+
                                       " tipo=\""+rst.getString(3)+"\" dal=\""+rst.getString(4)+"\""+
                                       " al=\""+rst.getString(5)+"\"></ITEM>";
            }
            xmlRiferimenti += "</COMPETENZE>";
            return xmlRiferimenti;
          }
          catch (Exception e) {
               throw new Exception("GD4_Documento_XML::retrieveCompetenze() " + e.getMessage());
          }
  }

}
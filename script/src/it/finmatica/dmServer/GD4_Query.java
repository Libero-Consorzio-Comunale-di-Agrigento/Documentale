package it.finmatica.dmServer;

/*
 * GESTIONE DELLE QUERY
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import java.sql.*;

public class GD4_Query 
{
  // variabili
  String idQuery;
  String nomeQuery;
  String tipoQuery;    // S=Sistema, U=Utente
  String filtroQuery;
  Environment vEnv;

  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHODS:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public GD4_Query(Environment newEnv)
  {
         idQuery=null;
         nomeQuery=null;
         tipoQuery=null;
         filtroQuery=null;
         vEnv=newEnv;
  }

  public GD4_Query(String newIdQuery,String newNomeQuery,
                      String newTipoQuery,String newFiltroQuery,
                      Environment newEnv)
  {
         idQuery=newIdQuery;
         nomeQuery=newNomeQuery;
         tipoQuery=newTipoQuery;
         filtroQuery=newFiltroQuery;
         vEnv = newEnv;
  }

 // ***************** METODI DI GESTIONE QUERY ***************** //
 
   /*
   * METHOD:      retrieve() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica una Query dal DB a partire dall'idQuery
   *
   * RETURN:      void
  */
  public void retrieve() throws Exception 
  {
        IDbOperationSQL dbOp = null;
        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = vEnv.getDbOp();
   
           sStm.append("select nome,tipo,filtro from query");
           sStm.append(" where id_query = " + idQuery);

           dbOp.setStatement(sStm.toString());

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {
              this.nomeQuery=rst.getString(1);
              this.tipoQuery=rst.getString(2);
              this.filtroQuery=rst.getString(3);
           }
           else {              
              throw new Exception("Select fallita per idQuery: " + idQuery);                              
           }
         }
         catch (Exception e) {
               throw new Exception("GD4_Query::retrieve() " + e.getMessage());
         }
         
  }

  /*
   * METHOD:      insert() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce una nuova query
   *
   * RETURN:      void
  */
  public void insert() throws Exception 
  {
        IDbOperationSQL dbOp = null;
        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = vEnv.getDbOp();

           if (idQuery==null)
               idQuery=dbOp.getNextKeyFromSequence("QRY_SQ")+"";
   
           sStm.append("insert into query (id_query,nome,tipo,filtro,");
           sStm.append("data_aggiornamento,utente_aggiornamento)");
           sStm.append(" values ("+idQuery+",'"+Global.replaceAll(nomeQuery,"'","''")+"','");
           sStm.append(tipoQuery+"','"+Global.replaceAll(filtroQuery,"'","''")+"',");
           sStm.append("sysdate,'"+vEnv.getUser()+"')");

           dbOp.setStatement(sStm.toString());

           dbOp.execute();          
         }
         catch (Exception e) {
               throw new Exception("GD4_Query::insert() " + e.getMessage());
         }
         
  }

  /*
   * METHOD:      update() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiorna i valori di una query esistente
   *
   * RETURN:      void
  */
  public void update() throws Exception 
  {
        IDbOperationSQL dbOp = null;
        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = vEnv.getDbOp();
   
           sStm.append("update query set ");
           sStm.append("nome='"+Global.replaceAll(nomeQuery,"'","''")+"',");
           sStm.append("tipo='"+Global.replaceAll(tipoQuery,"'","''")+"',");
           sStm.append("filtro='"+filtroQuery+"',");
           sStm.append("utente_aggiornamento = '"+ vEnv.getUser() + "',");
           sStm.append("data_aggiornamento = sysdate ");
           sStm.append("where id_query = " + idQuery);

           dbOp.setStatement(sStm.toString());

           dbOp.execute();          
         }
         catch (Exception e) {
               throw new Exception("GD4_Query::update() " + e.getMessage());
         }
         
  }
  // ***************** METODI DI GET E SET ***************** //

  public String getIdQuery() 
  {
         return idQuery;
  }

  public void setIdQuery(String newIdQuery) 
  {
         idQuery=newIdQuery;
  }

  public String getNomeQuery() 
  {
         return nomeQuery;
  }

  public void setNomeQuery(String newNomeQuery) 
  {
         nomeQuery=newNomeQuery;
  }

  public String getTipoQuery() 
  {
         return tipoQuery;
  }

  public void setTipoQuery(String newTipoQuery) 
  {
         tipoQuery=newTipoQuery;
  }

  public String getFiltroQuery() 
  {
         return filtroQuery;
  }

  public void setFiltroQuery(String newFiltroQuery) 
  {
         filtroQuery=newFiltroQuery;
  }

}
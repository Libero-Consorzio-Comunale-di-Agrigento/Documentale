package it.finmatica.dmServer;

/*
 * GESTIONE DELLE TABELLE LINKS
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.dmServer.util.GestioneOrdinamentiCartelle;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class GD4_Links 
{
   String idLinks;
   String idCartella;
   String idOggetto;
   String tipoOggetto;
   Environment vEnv;

   // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHODS:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
   public GD4_Links(Environment newEnv)
   {
          idLinks=null;
          idCartella=null;
          idOggetto=null;
          tipoOggetto=null;
          vEnv=newEnv;
   }

   public GD4_Links(String newIdLinks,String newIdCartella,
                    String newIdOggetto,String newTipoOggetto,
                    Environment newEnv)
   {
          idLinks=newIdLinks;
          idCartella=newIdCartella;
          idOggetto=newIdOggetto;
          tipoOggetto=newTipoOggetto;
          vEnv=newEnv;
   }

   // ***************** METODI DI GET E SET ***************** //
 
   public String getIdLinks() 
   {
          return idLinks;
   }

   public void setIdQuery(String newIdLinks) 
   {
          idLinks=newIdLinks;
   }

   public String getIdOggetto() 
   {
          return idOggetto;
   }

   public void setIdOggetto(String newIdOggetto) 
   {
          idOggetto=newIdOggetto;
   }

   public String getIdCartella() 
   {
          return idCartella;
   }

   public void setIdCartella(String newIdCartella) 
   {
          idCartella=newIdCartella;
   }

   public String getTipoOggetto() 
   {
          return tipoOggetto;
   }

   public void setTipoOggetto(String newTipoOggetto) 
   {
          tipoOggetto=newTipoOggetto;
   }

   // ***************** METODI DI GESTIONE DEI LINKS ***************** //
   
   /*
    * METHOD:      insert()
    * SCOPE:       PUBLIC
    *
    * DESCRIPTION: Inserisce una nuovo Links
    *              
    * RETURN:      void
   */ 
   public void insert() throws Exception 
   {
        IDbOperationSQL dbOp = null;
        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = vEnv.getDbOp();

           if (idLinks==null)
               idLinks=dbOp.getNextKeyFromSequence("LINK_SQ")+"";
   
           sStm.append("insert into links (id_link,id_cartella,id_oggetto,tipo_oggetto,");
           sStm.append("data_aggiornamento,utente_aggiornamento)");
           sStm.append(" values ("+idLinks+","+idCartella+",");
           sStm.append(idOggetto+",'"+tipoOggetto+"',");
           sStm.append("sysdate,'"+vEnv.getUser()+"')");

           dbOp.setStatement(sStm.toString());

           dbOp.execute();
           
   	       GestioneOrdinamentiCartelle ord =new GestioneOrdinamentiCartelle(vEnv,idCartella,idOggetto,"D");
	       ord.rebuild(true);           
         }
         catch (Exception e) {               
               throw new Exception("GD4_Links::insert() " + e.getMessage());
         }        

   } 

  /*
    * METHOD:      update()
    * SCOPE:       PUBLIC
    *
    * DESCRIPTION: Aggiorna i valori di un Link esistente
    *              
    * RETURN:      void
   */ 
  public void update() throws Exception 
  {
        IDbOperationSQL dbOp = null;
        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = vEnv.getDbOp();
   
           sStm.append("update Links set ");
           sStm.append("idCartella="+idCartella+",");
           sStm.append("idOggetto="+idOggetto+",");
           sStm.append("tipoOggetto='"+tipoOggetto+"',");
           sStm.append("utente_aggiornamento = '"+ vEnv.getUser() + "',");
           sStm.append("data_aggiornamento = sysdate ");
           sStm.append("where id_link = " + idLinks);

           dbOp.setStatement(sStm.toString());

           dbOp.execute();          
         }
         catch (Exception e) {               
               throw new Exception("GD4_Links::update() " + e.getMessage());
         }
                  
  }

}
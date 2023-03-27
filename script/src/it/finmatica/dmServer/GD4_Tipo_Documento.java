package it.finmatica.dmServer;

/*
 * GESTIONE DEGLI TIPI DOCUMENTO
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.jfc.dbUtil.*;
import java.sql.*;
import it.finmatica.dmServer.util.ElapsedTime;

public class GD4_Tipo_Documento extends A_Tipo_Documento
{
   // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
  
  
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public GD4_Tipo_Documento()
  {
	  	 
  }

  // ***************** METODI DI GESTIONE TIPO DOCUMENTO ***************** //
 
  public String toString() 
  {        
         return super.toString();
  }

  /*
   * METHOD:      retrieve(boolean) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un tipo documento dal Database
   *
   * RETURN:      void
  */
  public void retrieve(boolean flagLibreria) throws Exception 
  {
         if (this.getIdTipodoc().equals("0")) return;

         IDbOperationSQL dbOpSQL = null;
         ElapsedTime elpsTime;
         
         try {
           StringBuffer sStm = new StringBuffer();
           dbOpSQL = vu.getDbOp();

           sStm.append("select td.nome,td.id_libreria, td.tipo_log,");
           sStm.append("td.gestione_competenze||'@'||td.MANAGE_COMPETENZE,");
           sStm.append("nvl(td.ISHORIZONTAL_MODEL,0),nvl(td.log_file,'N'),");
           sStm.append("nvl(m.NUM_MAX_ALLEGATI,-1), nvl(td.competenze_Allegati,'N') compAll,");
           sStm.append("decode(nvl(td.competenze_Allegati,'N'),'N',1, GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO', '"+ this.getIdTipodoc()+"', 'LA', '"+vu.getUser()+"', F_TRASLA_RUOLO('"+vu.getUser()+"','GDMWEB','GDMWEB'))) letturaAllegati, ");
           sStm.append("decode(nvl(td.competenze_Allegati,'N'),'N',1, GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO', '"+ this.getIdTipodoc()+"', 'UA', '"+vu.getUser()+"', F_TRASLA_RUOLO('"+vu.getUser()+"','GDMWEB','GDMWEB'))) aggiornaAllegati, ");           
           sStm.append("decode(nvl(td.competenze_Allegati,'N'),'N',1, GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO', '"+ this.getIdTipodoc()+"', 'DA', '"+vu.getUser()+"', F_TRASLA_RUOLO('"+vu.getUser()+"','GDMWEB','GDMWEB'))) cancellaAllegati ");
           sStm.append("from tipi_documento td,modelli m");
           sStm.append(" where td.id_tipodoc = " + this.getIdTipodoc()+" and td.id_tipodoc=m.id_tipodoc");
          
           dbOpSQL.setStatement(sStm.toString());
           
           elpsTime = new ElapsedTime("GD4_TIPO_DOCUMENTO",vu);
           
           elpsTime.start("Retrieve da tabella Tipi Documento",sStm.toString());
           dbOpSQL.execute();
           elpsTime.stop();

           ResultSet rst = dbOpSQL.getRstSet();

           if (rst.next()) {
              String nome=rst.getString(1);
              String idLibreria=rst.getString(2);
              String tipoLog=rst.getString(3);
              String comp=rst.getString(4);
              
              this.setIsHorizontalModel(rst.getInt(5));
              this.setTipoLogFile(rst.getString(6));
              this.setNMaxAllegati(rst.getLong(7));
              this.setCompetenzeAllegati(rst.getString("compAll"));
              this.letturaAllegati=rst.getInt("letturaAllegati");
              this.modificaAllegati=rst.getInt("aggiornaAllegati");
              this.cancellaAllegati=rst.getInt("cancellaAllegati");              
              //rst.close();
           
              this.setNome(nome);

              try {
                this.inizializzaDati(vu);
              }
              catch (Exception e) {
                throw new Exception("GD4_Tipo_Documento.retrieve() - inizializzaDati\n" + e.getMessage());
              }
     
              this.getLibreria().setIdLibreria(idLibreria);
              this.getLibreria().inizializza(vu);

              // Riempio l'oggetto libreria con 
              // i dati presi dal Database    
              if (flagLibreria)
                 try {
                   this.getLibreria().retrieve();
                 }
                 catch (Exception e) {                   
                   throw new Exception("GD4_Tipo_Documento.retrieve() - Retrieve Libreria\n" + e.getMessage());
                 }

                this.setTipoLog(tipoLog);
                this.setCompetenze(comp);           
           }
           else {              
              throw new Exception("GD4_Tipo_Documento.retrieve() - Select fallita per idTipodoc: " + 
                                     this.getIdTipodoc());                   
           }      
         }
         catch (Exception e) {               
               throw new Exception("GD4_Tipo_Documento.retrieve() " + e.getMessage());
         }
                  
  }

}
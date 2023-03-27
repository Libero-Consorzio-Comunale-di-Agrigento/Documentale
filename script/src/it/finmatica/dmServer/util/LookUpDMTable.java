package it.finmatica.dmServer.util;

/*
 * GESTIONE DELLE LOOKUP SULLE TABELLE
 *
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 *
 * */

import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.jfc.utility.DateUtility;

import java.io.InputStream;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.Vector;
import java.util.HashMap;
import it.finmatica.dmServer.controlli.*;
import it.finmatica.dmServer.dbEngine.LogDocumentDbOperation;
import it.finmatica.dmServer.dbEngine.LookUpDbOperation;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Area;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Metadato;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Modello;

public class LookUpDMTable
{
  Environment en;
  IDbOperationSQL  dbOp;
  private ElapsedTime elpsTime;
  private boolean bIsNew=false;
  private LookUpDbOperation lookUpDbOp;

  public LookUpDMTable(Environment newEn) {
         en = newEn;
         elpsTime = new ElapsedTime("LookUpDMTable",en);

         lookUpDbOp = new LookUpDbOperation(en,en.getDbOp(),"LookUpDMTable");
  }

  public LookUpDMTable(IDbOperationSQL  dbOper) {
	  	 dbOp = dbOper;
  }

  public Vector<Area> lookUpListaAree() throws Exception {
	  	 return lookUpDbOp.getListaAree();
  }

  public Vector<Modello> lookUpListaAree(String area) throws Exception {
	  	 return lookUpDbOp.getListaModelli(area);
  }

  public Vector<Metadato> lookUpListaMetaDati(String area,String cm) throws Exception {
	  	 return lookUpDbOp.getListaMetadati(area, cm);
  }

  public String lookUpIdDocumentoFromIdAllegato(String idAllegato) throws Exception {
	  	 return lookUpDbOp.getIdDocumentoFromIdAllegato(idAllegato);
  }

  /*
   * METHOD:      lookUpNomeTipoDocByID(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: sIdTipoDoc -> id Tipo Documento
   *              Restituisce il nome del tipo documento
   *
   * RETURN:      String
  */
  public String lookUpNomeTipoDocByID(String sIdTipoDoc) throws Exception
   {
         String sNome = "";

         if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
         {
             try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();
                sStm.append("select t.nome  ");
                sStm.append("  from tipi_documento t ");
                sStm.append(" where t.ID_TIPODOC = '" + sIdTipoDoc + "'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();
                if ( rst.next() )
                   sNome = rst.getString(1);

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpNomeTipoDocByID\n" + e.getMessage());
            }
         }
         return sNome;
   }

  /*
   * METHOD:      lookUpTipoUsoAreaModello(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il tipo uso del tipo documento
   *
   * RETURN:      String
  */
  public String lookUpTipoUsoAreaModello(String idTipoDoc) throws Exception
   {
         String sNome = "";

         if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
         {
             try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();
                sStm.append("select m.tipo_uso ");
                sStm.append("  from modelli m ");
                sStm.append(" where m.ID_TIPODOC="+idTipoDoc);

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();
                if ( rst.next() )
                   sNome = rst.getString(1);

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpNomeTipoDocByID\n" + e.getMessage());
            }
         }
         return sNome;
   }

  /*
   * METHOD:      lookUpTipoDoc(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: sIdTipoDoc -> id Tipo Documento
   *              sArea      -> Area
   *              Restituisce il nome del tipo libreria.
   *              ATTENZIONE: Può prendere in input sia il nome
   *              che l'id del tipo documento. Se viene passato
   *              il nome occorre valorizzare anche l'area.
   *
   * RETURN:      String
  */
  public ModelInformation lookUpTipoDoc(String sTipoDocumento, String sArea) throws Exception
   {     ModelInformation mi = null;
         String sTipoLibreria = sTipoDocumento+"@";

         if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
         {

             try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select t.ID_TIPODOC, t.ID_LIBRERIA, nvl(t.ISHORIZONTAL_MODEL,0),nvl(m.NUM_MAX_ALLEGATI,-1)  ");
                sStm.append("  from tipi_documento t, modelli m ");
                sStm.append(" where m.ID_TIPODOC = t.ID_TIPODOC " );
                sStm.append("   and m.codice_modello = '" + sTipoDocumento + "'");
                sStm.append("   and m.area  = '" + sArea + "'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() ) {
                	mi = new ModelInformation(""+rst.getLong(1),sArea,sTipoDocumento,rst.getLong(2),rst.getInt(3));
                	mi.setNMaxAllegato(rst.getLong(4));
                }
                else {        // è il caso in cui passo un idTipoDocumento
                   sStm = new StringBuffer();
                   sStm.append("select td.id_tipodoc, ID_LIBRERIA,nvl(td.ISHORIZONTAL_MODEL,0),nvl(m.NUM_MAX_ALLEGATI,-1) from TIPI_DOCUMENTO td, MODELLI m ");
                   sStm.append("where td.ID_TIPODOC=m.ID_TIPODOC ");
                   if (Global.isNumeric(sTipoDocumento))
                      sStm.append("and m.id_tipodoc = " + sTipoDocumento );
                   else
                      sStm.append("and nome = '" + sTipoDocumento + "' and area='"+sArea+"'" );

                   dbOp.setStatement(sStm.toString());
                   dbOp.execute();

                   rst = dbOp.getRstSet();

                   if ( rst.next() ) {
                      mi = new ModelInformation(""+rst.getLong(1),sArea,sTipoDocumento,rst.getLong(2),rst.getInt(3));

                      mi.setNMaxAllegato(rst.getLong(4));
                   }
                   else
                   {
                      throw new Exception("LookUpDMTable::lookUpTipoDoc errore lookUp("+sTipoDocumento+","+sArea+")\n");
                   }

                }

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpTipoDoc\n" + e.getMessage());
            }
         }
         return mi;
   }

  /*
   * METHOD:      lookUpArea(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: sIdTipoDoc -> id Tipo Documento
   *
   *              Restituisce il nome dell'area
   *              ATTENZIONE: Può prendere in input sia il nome
   *              che l'id del tipo documento.
   *
   * RETURN:      String
  */
  public String lookUpArea(String sTipoDocumento) throws Exception
  {
     String sArea = Global.AREAGDM;
     int dim = 0;

     if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
     {
         try {
            String  sStm = null;

            dbOp = connect();
            if (Global.isNumeric(sTipoDocumento)){
                sStm = "select m.area ";
                sStm +="  from modelli m ";
                sStm +=" where m.id_tipodoc = :TD" ;
            }
            else {
                sStm = "select m.area ";
                sStm +="  from tipi_documento t, modelli m ";
                sStm +=" where t.ID_TIPODOC = m.ID_TIPODOC ";
               sStm +="   and t.NOME  = :TD" ;
            }

            dbOp.setStatement(sStm);
            dbOp.setParameter(":TD",sTipoDocumento);

            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();
/*
 * ************** OCIO MEGA ACCROCCHIO PER BACO RESULTSET *********** */
            dim = 0;
            if (  rst.next() ){
                dim = 1;
                sArea = rst.getString(1);
                try{
                  if (  rst.next() )
                      dim = 2;
                }catch (Exception e) {}
            }
            if (dim != 1)
                sArea = null;
/*
* ***************** FINE MEGA ACCROCCHIO PER BACO RESULTSET ****************/

            close();
        }
        catch (Exception e) {
            close();
            throw new Exception("LookUpDMTable::lookUpArea\n" + e.getMessage());
        }
     }
     return sArea;
  }

  /*
   * METHOD:      lookUpTipoByIdDoc(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: sIdTipoDoc -> id Tipo Documento
   *
   *              Restituisce l'id tipo documento
   *
   * RETURN:      String
  */
  public String lookUpTipoByIdDoc(String IdDocumento)  throws Exception
   {
       String idTipoDocumento = IdDocumento;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select ID_TIPODOC from documenti");
                sStm.append(" where ID_DOCUMENTO = " + IdDocumento );

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() )
                   idTipoDocumento = rst.getString(1);
                else
                   idTipoDocumento = IdDocumento;

                close();
           }
           catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpTipoByIdDoc\n" + e.getMessage());

           }
       }

       return idTipoDocumento;
   }

  /*
   * METHOD:      lookUpIstruzioneTestoFooterPDFModello(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: IdDocumento -> id Documento
   *
   *              Restituisce la query
   *
   * RETURN:      String
  */
  public String lookUpIstruzioneTestoFooterPDFModello(String IdDocumento)  throws Exception
   {
       String testo=null;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();
                String istruzione;

                dbOp = connect();

                sStm.append("select ISTRUZIONI,codice_modello,modelli.area from documenti, modelli ");
                sStm.append(" where documenti.id_tipodoc=modelli.id_tipodoc");
                sStm.append(" and ID_DOCUMENTO = " + IdDocumento );

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() && rst.getString(1)!=null )
                   istruzione = rst.getString(1);
                else
                   throw new Exception("Non è stata specificata l'istruzione sul modello ("+rst.getString(2)+") area ("+rst.getString(3)+") per il recupero del TESTO PDF!! Impossibile continuare");

                //Parso L'istruzione
                ACKParser ackPar = new ACKParser(IdDocumento,en);
			    String istruzioneJava = ackPar.bindingDeiParametri(istruzione);

			    if (!ackPar.getError().equals("@")) {
			         throw new Exception("execControlloJava - Errore in bindingDeiParametri\n"+ackPar.getError());
			    }

                try {
                   dbOp.setStatement(istruzioneJava);
                   dbOp.execute();
                }
                catch (Exception e) {
                	throw new Exception("Errore nel lancio dell'istruzione sul modello per il recupero del TESTO PDF!! Impossibile continuare\nFrase SQL: "+istruzione);
                }

                rst = dbOp.getRstSet();

                if (rst.next() )
                   testo = rst.getString(1);
                else
                   throw new Exception("L'istruzione SQL sul modello per il recupero del TESTO PDF non restituisce nulla!! Impossibile continuare\nFrase SQL: "+istruzione);


                close();
           }
           catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpIstruzioneTestoFooterPDFModello\n" + e.getMessage());
           }
       }

       return testo;
   }

  /*
   * METHOD:      lookUpTipoByIdDoc(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: nome -> id oggetto file
   *              idTipoDoc -> id Documento
   *
   *              Restituisce l'id oggetto file
   *
   * RETURN:      String
  */
  public String lookUpOggettoByName(String nome, String idDocumento) throws Exception
   {
       String idOggettoFile = nome;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select ID_OGGETTO_FILE from oggetti_file");
                sStm.append(" where ID_DOCUMENTO = " + idDocumento );
                sStm.append("   and FILENAME = '" + Global.replaceAll(nome,"'","''") + "'" );

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();
                if ( rst.next() ){
                   idOggettoFile = rst.getString(1);
                   if (rst.next())
                       throw new Exception("Errore file doppio: "  +  nome);
                }
                else
                   idOggettoFile = nome;

            close();
           }
           catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpOggettoByName\n" + e.getMessage());

           }
       }
       return idOggettoFile;
    }

  /*
   * METHOD:      lookUpOggettoPadre(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: idOggettoFile -> id oggetto file
   *
   *              Restituisce l'id oggetto file Padre
   *
   * RETURN:      String
  */
  public String lookUpOggettoPadre(String idOggettoFile) throws Exception
   {
       String idOggettoFilePadre = "-1";

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select ID_OGGETTO_FILE_PADRE from oggetti_file");
                sStm.append(" where ID_OGGETTO_FILE = " + idOggettoFile );

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();
                if ( rst.next() ) idOggettoFilePadre = rst.getString(1);

                close();
           }
           catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpOggettoPadre\n" + e.getMessage());
           }
       }
       return idOggettoFilePadre;
   }

  /*
   * METHOD:      lookUpOggettoByNameEOggettoPadre(String, String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  nome -> id oggetto file
   *               idDocumento -> id documento
   *               idOggettoPadre -> id oggetto padre
   *
   *               Restituisce l'id oggetto file
   *
   * RETURN:      String
  */
  public String lookUpOggettoByNameEOggettoPadre(String nome, String idDocumento, String idOggettoPadre) throws Exception
   {
       String idOggettoFile = nome;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select ID_OGGETTO_FILE from oggetti_file");
                sStm.append(" where ID_DOCUMENTO = " + idDocumento );
                sStm.append("   and FILENAME = '" + Global.replaceAll(nome,"'","''") + "' " );
                sStm.append("   and ID_OGGETTO_FILE_PADRE = " + idOggettoPadre);

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();
                if ( rst.next() ){
                   idOggettoFile = rst.getString(1);
                   if (rst.next())
                       throw new Exception("Errore file doppio: "  +  nome);
                }
                else
                   idOggettoFile = nome;

                close();
           }
           catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpOggettoByName\n" + e.getMessage());

           }
       }
       return idOggettoFile;
   }

   /*
   * METHOD:      lookUpCampi(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sNomeCampo -> id campo
   *               idTipoDoc -> id tipo documento
   *
   *               Restituisce l'id campo
   *
   * RETURN:      String
  */
  public String lookUpCampi(String sNomeCampo, String idTipoDoc) throws Exception
   {
      String idCampo = sNomeCampo;

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select ID_CAMPO from campi_documento");
              sStm.append(" where NOME = '" + sNomeCampo + "'");
              sStm.append("   and ID_TIPODOC = " + idTipoDoc );

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() )
                 idCampo = rst.getString(1);
              else
                 throw new Exception("LookUpDMTable::lookUpCampi(@,@) - campo "  + sNomeCampo+" per tipo documento "+idTipoDoc + " non trovato\n");

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpCampi(@,@) - (nomeCampo/idTipoDoc) ("  + sNomeCampo+"/"+idTipoDoc + ")\n" + e.getMessage());
          }
      }
      return idCampo;
   }

  /*
   * METHOD:      lookUpLibreria(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sLibreria -> id libreria
   *
   *               Restituisce l'id libreria
   *
   * RETURN:      String
  */
  public String lookUpLibreria(String sLibreria) throws Exception
   {
      String idLibreria = sLibreria;

      if (en.Global.DM.equals(en.Global.FINMATICA_DM))
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select ID_LIBRERIA  from librerie");
              sStm.append(" where LIBRERIA = '" + sLibreria + "'");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() )
                 idLibreria = rst.getString(1);
              else
                 idLibreria = sLibreria;

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpLibreria\n" + e.getMessage());
          }
      }
      return idLibreria;
   }

  /*
   * METHOD:      lookUpFormato(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sFormato -> id formato
   *
   *               Restituisce l'id formato
   *
   * RETURN:      String
  */
  public String lookUpFormato(String sFormato) throws Exception
   {
     String idFormato = sFormato;

     if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
     {
        try {
            StringBuffer sStm = new StringBuffer();

            dbOp = connect();

            sStm.append("select ID_FORMATO  from FORMATI_FILE");
            sStm.append(" where upper(NOME) = upper('" + sFormato.replaceAll("'", "''") + "')");

            dbOp.setStatement(sStm.toString());
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            if (rst.next() )
               idFormato = rst.getString(1);
            else
               idFormato = "0";  //formato file generico

            close();
        }
        catch (Exception e) {
            close();
            throw new Exception("LookUpDMTable::lookUpFormato\n" + e.getMessage());
        }
     }
      return idFormato;
   }

  /*
   * METHOD:      lookUpFormato(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sFormato -> id formato
   *
   *               Restituisce l'id formato
   *
   * RETURN:      String
  */
  public String lookUpFormatoById(String idFormato) throws Exception
  {
     String nome="";
     if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
     {
        try {
            StringBuffer sStm = new StringBuffer();

            dbOp = connect();

            sStm.append("select NOME  from FORMATI_FILE");
            sStm.append(" where ID_FORMATO = upper('" + idFormato + "')");

            dbOp.setStatement(sStm.toString());
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            if (rst.next() )
            	nome = rst.getString(1);
            else
            	nome = "";  //formato file generico

            close();
        }
        catch (Exception e) {
            close();
            throw new Exception("LookUpDMTable::lookUpFormatoById\n" + e.getMessage());
        }
     }
      return nome;
   }

  /*
   * METHOD:      lookUpNomeDocByIdDoc(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  IdDocumento -> id documento
   *
   *               Restituisce il nome del documento
   *
   * RETURN:      String
  */
  public String lookUpNomeDocByIdDoc(String IdDocumento) throws Exception
   {
       String nomeDocumento = IdDocumento;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select valore_stringa from valori, campi");
                sStm.append(" where ID_DOCUMENTO = " + IdDocumento );
                sStm.append(" and campi.id_campo = valori.id_campo ");
                sStm.append(" and campi.nome='NOME DOCUMENTO' ");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() )
                   nomeDocumento = rst.getString(1);
                else
                   nomeDocumento = IdDocumento;

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpNomeDocByIdDoc\n" + e.getMessage());

            }
       }
       return nomeDocumento;
   }

  /*
   * METHOD:      lookUpTipoCampo(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  idCampo -> id campo
   *
   *               Restituisce il tipo di campo leggendo dalla
   *               tabella dati a partire dalla tabella campi
   *
   * RETURN:      String
  */
  public String lookUpTipoCampo(String idCampo) throws Exception
  {
        String sTipo = "S#S"+"@";

        try {
           StringBuffer sStm = new StringBuffer();
           dbOp = connect();

           sStm.append("select tipo, formato_data, nvl(in_uso,'N') from campi_documento,dati_modello, dati ");
           sStm.append("where campi_documento.id_campo = dati_modello.id_campo and ");
           sStm.append("dati_modello.AREA_DATO = dati.area and ");
           sStm.append("dati_modello.dato = dati.dato and ");
           sStm.append("campi_documento.id_campo = " + idCampo);

           dbOp.setStatement(sStm.toString());

           elpsTime.start("lookUpTipoCampo",sStm.toString());

           dbOp.execute();

           elpsTime.stop();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next())
             if (rst.getString(1).equals("D"))
                 sTipo=rst.getString(3)+"#D"+"@"+Global.replaceAll(rst.getString(2), "hh:", "HH:");
              else
                 sTipo=rst.getString(3)+"#"+rst.getString(1);

           close();

        }
        catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::lookUpTipoCampo - (idCampo) (" + idCampo+ ")\n" + e.getMessage());
        }
        return   sTipo;

  }


  public FieldInformation lookUpInfoCampo(String nomeCampo, String idTipoDoc) throws Exception
  {
	  	 return lookUpInfoCampo(nomeCampo,idTipoDoc,null);
  }

  public FieldInformation lookUpInfoCampo(String idCampo) throws Exception
  {
	  	 return lookUpInfoCampo(null,null,idCampo);
  }

  /*
   * METHOD:      lookUpTipoCampo(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  idCampo -> id campo
   *
   *               Restituisce il tipo di campo leggendo dalla
   *               tabella dati a partire dalla tabella campi
   *
   * RETURN:      String
  */
  private FieldInformation lookUpInfoCampo(String nomeCampo, String idTipoDoc, String idCampo) throws Exception
  {

        try {
           StringBuffer sStm = new StringBuffer();

           dbOp = connect();

           sStm.append(getSQLInfoCampo( nomeCampo,  idTipoDoc,  idCampo));

           dbOp.setStatement(sStm.toString());


           elpsTime.start("lookUpInfoCampo",sStm.toString());

           dbOp.execute();

           elpsTime.stop();

           ResultSet rst = dbOp.getRstSet();

           FieldInformation fi = null;

           if (rst.next()) {

              if (rst.getString(1).equals("D")) {
            	  if (rst.getString(2)==null)
            		  throw new Exception("il campo "  + nomeCampo+" per tipo documento "+idTipoDoc +" è di tipo DATA, ma non è stato specificato alcun formato_data sul dizionario dati!\nImpossibile continuare");

                  fi = (new FieldInformation(rst.getString(4),
                		                       rst.getString(1),
                		                       Global.replaceAll(rst.getString(2), "hh:", "HH:"),
                		                       rst.getString(3),
                		                       rst.getString(5),
                		                       rst.getString(6)));
              }
              else {
                  fi = (new FieldInformation(rst.getString(4),
                		                       rst.getString(1),
                		                       "",
                		                       rst.getString(3),
                		                       rst.getString(5),
                		                       rst.getString(6)));
              }
           }
           else
                 throw new Exception("LookUpDMTable::lookUpInfoCampo(@,@) - campo "  + nomeCampo+" per tipo documento "+idTipoDoc + " non trovato\n");

           fi.setLog(rst.getInt(8));
           fi.setNomeCampo(rst.getString(7));

           close();
           return fi;

        }
        catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::lookUpInfoCampo\n"+ e.getMessage());
        }

  }

  public String getSQLInfoCampo(String nomeCampo, String idTipoDoc, String idCampo) {
	      StringBuffer sStm = new StringBuffer();

          sStm.append("select dati.tipo, formato_data, nvl(in_uso,'N'),campi_documento.id_campo,nvl(SENZA_SALVATAGGIO,'N'),nvl(SENZA_AGGIORNAMENTO,'N'),campi_documento.nome,nvl(tipo_log,0),nvl(dati.lunghezza,0) from campi_documento,dati_modello, dati,modelli ");
          sStm.append("where campi_documento.id_campo = dati_modello.id_campo and ");
          sStm.append("dati_modello.AREA_DATO = dati.area and ");
          sStm.append("dati_modello.dato = dati.dato and ");
          if (idCampo==null) {
        	  sStm.append("campi_documento.nome = '" + nomeCampo +"' and ");
           	  sStm.append("campi_documento.ID_TIPODOC = " + idTipoDoc );
          }
          else {
        	  sStm.append("campi_documento.id_campo = " + idCampo);
          }
          sStm.append(" AND modelli.id_tipodoc="+idTipoDoc);
          sStm.append(" and dati_modello.area=modelli.area");

          return sStm.toString();
  }

  public Vector lookUpVectorInfoCampo(String sql ) throws Exception
  {
	  	 Vector vInfoCampo = new Vector();

         try {
           StringBuffer sStm = new StringBuffer();

           dbOp = connect();

           sStm.append(sql);

           dbOp.setStatement(sStm.toString());

           elpsTime.start("lookUpVectorInfoCampo",sStm.toString());
           //System.out.println(sStm.toString());
           dbOp.execute();

           elpsTime.stop();

           ResultSet rst = dbOp.getRstSet();

           FieldInformation fi = null;

           while (rst.next()) {

              if (rst.getString(1).equals("D")) {
            	  if (rst.getString(2)==null)
            		  throw new Exception("il campo "  + rst.getString(7) + " è di tipo DATA, ma non è stato specificato alcun formato_data sul dizionario dati!\nImpossibile continuare");

                  fi = (new FieldInformation(rst.getString(4),
                		                       rst.getString(1),
                		                       Global.replaceAll(rst.getString(2), "hh:", "HH:"),
                		                       rst.getString(3),
                		                       rst.getString(5),
                		                       rst.getString(6)));
              }
              else {
                  fi = (new FieldInformation(rst.getString(4),
                		                       rst.getString(1),
                		                       "",
                		                       rst.getString(3),
                		                       rst.getString(5),
                		                       rst.getString(6)));
              }

              fi.setLog(rst.getInt(8));
              fi.setNomeCampo(rst.getString(7));
              fi.setLunghezza(rst.getLong(9));

              vInfoCampo.add(fi);
           }

           close();
           return vInfoCampo;
        }
        catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::lookUpInfoCampo\n"+ e.getMessage());
        }
  }

  /*
   * METHOD:      lookUpClassificazione(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sClass -> classificazione
   *
   *               Restituisce il tipo di campo leggendo dalla
   *               tabella dati a partire dalla tabella campi
   *
   * RETURN:      String
  */
  public String lookUpClassificazione(String sClass) throws Exception
  {
        String sTipo = "S"+"@";

        try {
          StringBuffer sStm = new StringBuffer();

          dbOp = connect();

          sStm.append("select tipo, formato_data");
          sStm.append("  from dati d, classificazioni  c ");
          sStm.append(" where d.area = c.area ");
          sStm.append("   and d.dato = c.dato ");
          sStm.append("   and c.classificazione = '" + sClass +"'");
          sStm.append("   and rownum = 1");

          dbOp.setStatement(sStm.toString());

          dbOp.execute();

          ResultSet rst = dbOp.getRstSet();
          while (rst.next())
          {
            if (rst.getString(1).equals("D") )
            {
              sTipo= "D"+"@"+Global.replaceAll(rst.getString(2), "hh:","HH:");
              break;
            }
            else if (rst.getString(1).equals("N") ){
               sTipo="N"+"@";
               break;
            }
            else
               sTipo="S"+"@";
          }

          close();
        }
        catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::lookUpClassificazione - (Classif.) (" + sClass + ")\n" + e.getMessage());
        }
        return sTipo;
  }

  /*
   * METHOD:      lookUpTipoDato(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sArea -> area
   *               sDato -> dato
   *
   *               Restituisce il tipo di campo leggendo dalla tabella dati
   *
   * RETURN:      String
  */
  public String lookUpTipoDato(String sArea, String sDato) throws Exception
  {
        String sTipo = "S"+"@";

        try {
          StringBuffer sStm = new StringBuffer();

          dbOp = connect();

          sStm.append("select tipo, formato_data");
          sStm.append("  from dati d ");
          sStm.append(" where d.area = '" + sArea +"'");
          sStm.append("   and d.dato = '" + sDato +"'");
          sStm.append("   and rownum = 1");

          dbOp.setStatement(sStm.toString());

          dbOp.execute();

          ResultSet rst = dbOp.getRstSet();
          while (rst.next())
          {
            if (rst.getString(1).equals("D") )
            {
              sTipo= "D"+"@"+Global.replaceAll(rst.getString(2), "hh:","HH:");
              break;
            }
            else if (rst.getString(1).equals("N") ){
               sTipo="N"+"@";
               break;
            }
            else
               sTipo="S"+"@";
          }

          close();
        }
        catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::lookUpTipoDato - (dato) (" + sDato + ")\n" + e.getMessage());
        }

        return sTipo;
  }

  /*
   * METHOD:      lookUpExistsValore(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica se per il valore clob sulla tabella
   * 			  valori corrisponde con il valore passato come
   * 			  2° parametro
   *
   *
   * RETURN:      String
  */
  public boolean lookUpExistsValore(String IdValore,String valore) throws Exception
   {

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {
           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select valore_clob from valori");
                sStm.append(" where id_valore = " + IdValore);

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if (rst.next()) {
                	InputStream r = rst.getAsciiStream(1);
    		        String s = "";

    		        try {
	    		        for (int c=r.read(); c!=-1; c=r.read()) {
	    		             s = s + (char)c;
	    		        }
    		        }
    		        catch (NullPointerException e) {
    		        	return false;
    		        }


              	    if (s.equals(valore)) return true;
                }

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpExistsValore\n" + e.getMessage());

            }
       }
       return false;
   }

  /*
   * METHOD:      lookUpParametro(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il valore corrispondente
   * 			  al parametro passato in input
   * 			  La tabella è PARAMETRI
   * 			  Se il parametro non esiste restituisce null
   *
   * RETURN:      String
  */
  public String lookUpParametro(String nomeParametro) throws Exception
   {
	     return lookUpParametro(nomeParametro,"@DMSERVER@");
   }

  /*
   * METHOD:      lookUpParametro(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il valore corrispondente
   * 			  al parametro passato in input e il tipo di
   * 			  modello.
   * 			  La tabella è PARAMETRI
   * 			  Se il parametro non esiste restituisce null
   *
   * RETURN:      String
  */
  public String lookUpParametro(String nomeParametro,String tipo_modello) throws Exception
   {
	   String sRet=null;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {

           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select valore from PARAMETRI");
                sStm.append(" where codice = '" + nomeParametro+"'");
                sStm.append(" and TIPO_MODELLO='"+tipo_modello+"'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if (rst.next()) {
                	sRet=rst.getString(1);
                }

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpParametro\n" + e.getMessage());

            }
       }
       return sRet;
   }

  public HashMap<String,String> lookUpParametro(Vector<String> nomiParametro, Vector<String> tipoModello) throws Exception {
      HashMap<String,String> hRet = new HashMap<String,String>();
     if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
     {

      try {
           StringBuffer sStm = new StringBuffer();

           dbOp = connect();

           for(int i=0;i<nomiParametro.size();i++) {
        	   sStm.append(" select codice,valore from PARAMETRI");
               sStm.append(" where codice = '" + nomiParametro.get(i)+"'");
               sStm.append(" and TIPO_MODELLO='"+tipoModello.get(i)+"'");

               if (i<nomiParametro.size() - 1) sStm.append(" UNION ");
           }

           dbOp.setStatement(sStm.toString());
           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           while (rst.next()) {
        	   hRet.put(rst.getString(1),rst.getString(2));
           }

           close();
       }
       catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::lookUpParametro con vettore\n" + e.getMessage());

       }
     }
     return hRet;
}


  /*
   * METHOD:      lookUpLabelDato(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la label del dato
   * 			  a partire dall'area
   *
   * RETURN:      String
  */
  public String lookUpLabelDato(String area,String dato, IDbOperationSQL dbOpLocal) throws Exception
   {
	   String sRet=null;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {

           try {
                StringBuffer sStm = new StringBuffer();

                if (dbOpLocal==null) dbOp = connect();

                sStm.append("select F_LABEL_DATO('"+area+"','"+dato+"') from DUAL");

                ResultSet rst;
                if (dbOpLocal==null) {
	                dbOp.setStatement(sStm.toString());
	                dbOp.execute();

	                rst = dbOp.getRstSet();
                }
                else {
                	dbOpLocal.setStatement(sStm.toString());
                	dbOpLocal.execute();

	                rst = dbOpLocal.getRstSet();
                }

                if (rst.next()) {
                	sRet=rst.getString(1);
                }
                else
                	sRet=dato;

                if (dbOpLocal==null) close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpLabelDato\n" + e.getMessage());

            }
       }
       return sRet;
   }

  /*
   * METHOD:      lookUpRelazioneDipendenza(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la colonna dipendenza della tabella
   * 			  tipi_relazione data la coppia (area,tipo_relazione)
   *
   * RETURN:      String
  */
  public String lookUpRelazioneDipendenza(String area,String tipoRelazione) throws Exception
   {
	   String sRet=null;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {

           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select DIPENDENZA from TIPI_RELAZIONE");
                sStm.append(" where AREA = '" + area+"'");
                sStm.append(" and TIPO_RELAZIONE='"+tipoRelazione+"'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if (rst.next()) {
                	sRet=rst.getString(1);
                }
                else
                	sRet="N";

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpRelazioneDipendenza\n" + e.getMessage());

            }
       }
       return sRet;
  }

    /*
   * METHOD:      lookUpTipoRela(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce la colonna dipendenza della tabella
   * 			  tipi_relazione data la coppia (area,tipo_relazione)
   *
   * RETURN:      String
  */
  public String lookUpTipoRela(String area,String tipoRelazione) throws Exception
   {
	   String sRet=null;

       if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
       {

           try {
                StringBuffer sStm = new StringBuffer();

                dbOp = connect();

                sStm.append("select 1 from TIPI_RELAZIONE");
                sStm.append(" where AREA = '" + area+"'");
                sStm.append(" and TIPO_RELAZIONE='"+tipoRelazione+"'");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if (rst.next()) {
                	sRet=rst.getString(1);
                }
                else
                	sRet="";

                close();
            }
            catch (Exception e) {
                close();
                throw new Exception("LookUpDMTable::lookUpTipoRela\n" + e.getMessage());

            }
       }
       return sRet;
  }

  /**
   * Dato un campo ed una o più aree restituisce il tipo
   * S=Stringa
   * D=Data
   * N=Numerico
   * @param valore
   * @return
  */
  public FieldInformation retrieveTipo(String campo,Vector vCmArea,String area) throws Exception {
		 FieldInformation fi=null;

         try {
        	if (vCmArea==null) vCmArea = new Vector();

            StringBuffer sStm = new StringBuffer();
            dbOp = connect();

            sStm.append("select tipo,FORMATO_DATA from campi_documento,dati_modello, dati ");
            sStm.append("where campi_documento.id_campo = dati_modello.id_campo and ");
            sStm.append("dati_modello.AREA_DATO = dati.area and ");
            sStm.append("dati_modello.dato = dati.dato and ");
            sStm.append("campi_documento.nome = '" + campo + "'");

            String sAreaWhere="";
            if (vCmArea.size()!=0 || area!=null) {
	            if (area!=null && !area.equals("null") && vCmArea.size()==0) {
	            	sAreaWhere=" and dati.area ='"+area+"'";
	            }
	            else {
	            	sAreaWhere=" and dati.area in (";

		            if (area!=null && !area.equals("null")) {
		            	sAreaWhere+="'"+area+"'";

		            	if (vCmArea.size()!=0)
		            		sAreaWhere+=",";
		            	else
		            		sAreaWhere+=")";
		            }

		            for(int i=0;i<vCmArea.size();i++) {

		            	sAreaWhere+="'"+vCmArea.get(i)+"'";

		            	if (i==vCmArea.size()-1)
		            		sAreaWhere+=")";
		            	else
		            		sAreaWhere+=",";
		            }
	            }
            }

            sStm.append(sAreaWhere);

            dbOp.setStatement(sStm.toString());
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            String tipo=null;
            if (rst.next()) {
            	tipo=rst.getString(1);
               fi = new FieldInformation(null,rst.getString(1),rst.getString(2),null,null,null);
            }

            //Non ho trovato nulla....magari è un campo di GDMSYS...PROVIAMO
            if (tipo==null) {
            	sStm = new StringBuffer();

            	sStm.append("select tipo,1,formato_data from dati where area='GDMSYS' and dati.dato = '"+campo+"'");
            	sStm.append("union ");
            	sStm.append("select tipo,2,formato_data from dati where dati.dato = '"+campo+"' ");
            	sStm.append("order by 2");

            	dbOp.setStatement(sStm.toString());
            	dbOp.execute();

                rst = dbOp.getRstSet();

                tipo=null;
	            if (rst.next())  {
	            	tipo=rst.getString(1);
	            	fi = new FieldInformation(null,rst.getString(1),rst.getString(3),null,null,null);
	            }
	            //Se il tipo è ancora nullo assumo che sia stringa!
	            if (tipo==null) {
	            	fi = new FieldInformation(null,"S","dd/mm/yyyy",null,null,null);
	            }
            }

            close();
        }
        catch (Exception e) {
           close();
           throw new Exception("LookUpDMTable::retrieveTipo - (NomeCampo) (" +campo+ ")\n" + e.getMessage());
        }

       return fi;
  }

  /**
    * Dato un valore restituisce il tipo di operatore
    * S=Stringa
    * D=Data
    * N=Numerico
    * @param valore
    * @return
  */
   public String lookupTipoOperatore(String valore) {
		   String tipo;

		   if (DateUtility.isDateValid(valore,"dd/mm/yyyy") || valore.toUpperCase().indexOf("SYSDATE")!=-1)
			   tipo="D";
		   else {
			 try {
               tipo="N";
               Long.parseLong(valore);
             }
             catch (NumberFormatException e) {
               tipo="S";
             }
		   }

       	   return tipo;
   }

   /*
   * METHOD:      lookUpAliasOrizzontalTable(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  idDoc -> id documento
   *
   *
   * RETURN:      String
  */
  public String lookUpAliasOrizzontalTable(String idDoc) throws Exception
   {
	  String alias="";

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) ");
              sStm.append("	 from user_objects,tipi_documento td,DOCUMENTI,AREE ");
              sStm.append(" where object_type='TABLE'");
              sStm.append("   and object_name=upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X'))");
              sStm.append("   and ID_DOCUMENTO = "+idDoc);
              sStm.append("   and td.ID_TIPODOC=DOCUMENTI.ID_TIPODOC");
              sStm.append("   and AREE.AREA=td.AREA_MODELLO");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() )
            	  alias=rst.getString(1);

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpAliasOrizzontalTable(@) - (idTipoDoc) ("  + idDoc + ")\n" + e.getMessage());
          }
      }
      return alias;
   }

     /*
   * METHOD:      lookUpCampi(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sNomeCampo -> id campo
   *               idTipoDoc -> id tipo documento
   *
   *               Restituisce il tipo di campo
   *
   * RETURN:      String
  */
  public String lookUpTipoCampoOrizontalTable(String sNomeCampo, String idDoc) throws Exception
   {
      String tipoCampo = sNomeCampo;

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select DATA_TYPE,DATA_LENGTH from user_tab_columns,tipi_documento td,DOCUMENTI,AREE ");
              sStm.append(" where table_name=upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) ");
              sStm.append("   and ID_DOCUMENTO = "+idDoc);
              sStm.append("   and td.ID_TIPODOC=DOCUMENTI.ID_TIPODOC");
              sStm.append("   and column_name='" + sNomeCampo.toUpperCase() + "'");
              sStm.append("   and AREE.AREA=td.AREA_MODELLO");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() ) {
            	  String tipo,lunghezza;

            	  tipo=rst.getString(1);
            	  lunghezza=rst.getString(2);

             	  tipoCampo = tipo+"@"+lunghezza;

              }
              else {
            	  sStm = new StringBuffer();

            	  sStm.append("select NVL(DATI_MODELLO.IN_USO,'Y') from DOCUMENTI,DATI_MODELLO,MODELLI ");
	              sStm.append(" where ID_DOCUMENTO = "+idDoc);
	              sStm.append("   and modelli.ID_TIPODOC=DOCUMENTI.ID_TIPODOC");
	              sStm.append("   and modelli.AREA=DATI_MODELLO.AREA");
	              sStm.append("   and modelli.CODICE_MODELLO=DATI_MODELLO.CODICE_MODELLO");
	              sStm.append("   and DATI_MODELLO.DATO='" + sNomeCampo + "'");

	              dbOp.setStatement(sStm.toString());
	              dbOp.execute();

	              rst = dbOp.getRstSet();

	              if (rst.next()) {
	            	  if (rst.getString(1).equals("Y"))
	            		  tipoCampo = "";
	            	  else
	            		  tipoCampo = "N";
	              }
	              else
	            	  tipoCampo="";
              }

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpTipoCampoOrizontalTable(@,@) - (nomeCampo/idDoc) ("  + sNomeCampo+"/"+idDoc + ")\n" + e.getMessage());
          }
      }
      return tipoCampo;
   }

    /*
   * METHOD:      lookUpDatoInUso(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  Verifica se il dato è in uso
   *
   * RETURN:      String
  */
  public String lookUpDatoInUso(String sNomeCampo,String idDoc) throws Exception
   {
      String tipoCampo = sNomeCampo;

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

           	  sStm.append("select NVL(DATI_MODELLO.IN_USO,'Y') from DOCUMENTI,DATI_MODELLO,MODELLI ");
              sStm.append(" where ID_DOCUMENTO = "+idDoc);
              sStm.append("   and modelli.ID_TIPODOC=DOCUMENTI.ID_TIPODOC");
              sStm.append("   and modelli.AREA=DATI_MODELLO.AREA");
              sStm.append("   and modelli.CODICE_MODELLO=DATI_MODELLO.CODICE_MODELLO");
              sStm.append("   and DATI_MODELLO.DATO='" + sNomeCampo + "'");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

	          if (rst.next())
	             tipoCampo = rst.getString(1);
	          else
	             tipoCampo = "";

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpDatoInUso(@,@) - (nomeCampo/idDoc) ("  + sNomeCampo+"/"+idDoc + ")\n" + e.getMessage());
          }
      }
      return tipoCampo;
   }

    /*
   * METHOD:      lookUpCampi(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sNomeCampo -> id campo
   *               idTipoDoc -> id tipo documento
   *
   *               Restituisce il tipo di campo
   *
   * RETURN:      String
  */
  public String lookUpNomeTabellaOrizontaleByIdDoc(String idDoc) throws Exception
   {
      String nomeTab = "";

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) from user_tab_columns,tipi_documento td,DOCUMENTI,AREE ");
              sStm.append(" where table_name=upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) ");
              sStm.append("   and ID_DOCUMENTO = "+idDoc);
              sStm.append("   and td.ID_TIPODOC=DOCUMENTI.ID_TIPODOC");
              sStm.append("   and AREE.AREA=td.AREA_MODELLO");


              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() )
                 nomeTab = rst.getString(1);
              else
                 nomeTab="";

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpNomeTabellaOrizontaleByIdDoc(@) - (idDoc) ("  +idDoc + ")\n" + e.getMessage());
          }
      }
      return nomeTab;
   }

    /*
   * METHOD:      lookUpCampi(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:  sNomeCampo -> id campo
   *               idTipoDoc -> id tipo documento
   *
   *               Restituisce il tipo di campo
   *
   * RETURN:      String
  */
  public String lookUpNomeTabellaOrizontaleByIdTipdoc(String idTipoDoc) throws Exception
   {
      String nomeTab = "";

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) ");
              sStm.append("  from tipi_documento td,AREE ");
              sStm.append(" where td.id_tipodoc="+idTipoDoc);
              sStm.append("   and AREE.AREA=td.AREA_MODELLO");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() )
                 nomeTab = rst.getString(1);
              else
                 nomeTab="";

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpNomeTabellaOrizontaleByIdTipdoc(@) - (td) ("  +idTipoDoc + ")\n" + e.getMessage());
          }
      }
      return nomeTab;
   }

    /*
   * METHOD:      lookUpNomeTabellaOrizontaleByArCm(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:
   *
   * RETURN:      String
  */
  public String lookUpNomeTabellaOrizontaleByArCm(String ar,String cm) throws Exception
   {
      String nomeTab = "";

      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
      {
          try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("select upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) ");
              sStm.append("  from tipi_documento td,modelli m,AREE ");
              sStm.append(" where td.id_tipodoc=m.id_tipodoc");
              sStm.append("   and m.area='"+ar+"' and codice_modello='"+cm+"'");
              sStm.append("   and AREE.AREA=td.AREA_MODELLO");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              if (rst.next() )
                 nomeTab = rst.getString(1);
              else
                 nomeTab="";

              //if (nomeTab.equals("_X"))
              //    throw new Exception("Per (ar,cm) ("+ar+","+cm+") non è stato specificata la tabella orizzontale nella colonna alias_view");

              if (nomeTab.equals(""))
            	  throw new Exception("Non esiste la coppia (ar,cm) ("+ar+","+cm+") impostata");

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpNomeTabellaOrizontaleByArCm(@,@) - (ar,cm) ("+ar+","+cm+")\n" + e.getMessage());
          }
      }
      return nomeTab;
   }

  public Vector lookUpInfoCampoOrizontalTable(String sNomeCampo, String nomeTabella) throws Exception
   {
         Vector infoCampo=null;

         if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0) {
            try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              String sWhere;
              //C'e una IN
              if (nomeTabella.indexOf("(")!=-1)
            	 sWhere=" where table_name in "+nomeTabella+" ";
              else
            	 sWhere=" where table_name = '"+nomeTabella+"'";

              sStm.append("select DATA_TYPE,DATA_LENGTH,table_name from user_tab_columns tc ");
              sStm.append(sWhere);
              sStm.append("   and column_name='" + sNomeCampo.toUpperCase() + "' ");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              while (rst.next() ) {
            	  if (infoCampo==null) infoCampo=new Vector();
            	  FieldInformation fi = (new FieldInformation(sNomeCampo.toUpperCase()));
            	  fi.setLunghezza(rst.getLong(2));
            	  fi.setTipo(rst.getString(1));
            	  fi.setTableViewName(rst.getString(3));

            	  infoCampo.add(fi);
              }

              close();
              return infoCampo;

          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpInfoCampoOrizontalTable(@,@) - (nomeCampo/nomeTabella) ("  + sNomeCampo+"/"+nomeTabella + ")\n" + e.getMessage());
          }
      }
      return infoCampo;
   }

  public void lookUpElencoCampiTabellaHoriz(String nomeTabella, Vector<String> vCampi, Vector<String> vTipoCampi) throws Exception {
		 StringBuffer sStm = new StringBuffer();

		 vCampi.clear();
		 vTipoCampi.clear();

		 try {
			 dbOp = connect();

			 sStm.append("select COLUMN_NAME, decode(DATA_TYPE,'DATE','D','NUMBER','N','S') ");
			 sStm.append("from user_tab_columns ");
			 sStm.append("where table_name='"+nomeTabella+"' and column_name not in ('ID_DOCUMENTO','FULL_TEXT')");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();

             ResultSet rst = dbOp.getRstSet();

             while (rst.next() ) {
            	 vCampi.add(rst.getString(1));
            	 vTipoCampi.add(rst.getString(2));
             }

			 close();
		 }
         catch (Exception e) {
             close();
             throw new Exception("LookUpDMTable::lookUpElencoCampiTabellaHoriz(@) - (nomeTabella) ("  +nomeTabella + ")\n" + e.getMessage());
         }
  }

    /*
   *
   * 0=Verticale
   * 1=Orizzontale
   * -1=Errore ... è mista
  */
  public int lookUpTipoRicercaHorV(String area, Vector vCm, Vector vCmArea, Vector campi, Vector campiOrdinamento, String arRet, String cmRet, String categoRet) throws Exception {
	     long contaModelliOrizzontali = 0;
	     int ret;

	     StringBuffer sSelect = new StringBuffer("");
	     String sNomeTabellaHoriz   ="upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X'))";
	     String sSelectTestata      ="select 1 from tipi_documento td,modelli m, user_objects u,AREE where td.id_tipodoc=m.id_tipodoc and AREE.AREA=td.AREA_MODELLO ";
	     String sSelectTestataVista ="select 1 from user_objects u ";

	     //Modelli aggiunti con addCodiceModello / settaArea
	     for(int i=0;i<vCm.size();i++) {
	   		 String sArea;
	   		 String sCm=(String)vCm.get(i);

	   		 if (!(vCmArea.get(i).equals("")))
	   		     sArea=(String)vCmArea.get(i);
	   		 else
	   		   	 sArea=area;

	   		 if (i>0) sSelect.append(" UNION ALL ");

	   		 contaModelliOrizzontali++;

	   		 sSelect.append(sSelectTestata);
	   		 sSelect.append("and m.area='"+sArea+"' and m.codice_modello='"+sCm+"' ");
	   		 sSelect.append("and u.object_name="+sNomeTabellaHoriz+" and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
	     }

	     //Modelli/Categorie aggiunti con addCampo
	     for(int i=0;i<campi.size();i++) {
	    	 keyval k = (keyval)campi.get(i);

	    	 if (!sSelect.toString().equals("") && (k.getArea()!=null || k.getCategoria()!=null)) sSelect.append(" UNION ALL ");

	    	 if (k.getArea()!=null) {
	    		  contaModelliOrizzontali++;
	    		 sSelect.append(sSelectTestata);
	    		 sSelect.append("and m.area='"+k.getArea()+"' and m.codice_modello='"+k.getCm()+"' ");
	    		 sSelect.append("and u.object_name="+sNomeTabellaHoriz+" and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
	    	 }

	    	 if (k.getCategoria()!=null) {
	    		  contaModelliOrizzontali++;
	    		 sSelect.append(sSelectTestataVista);
	    		 sSelect.append("where u.object_name='"+k.getCategoria()+"_VIEW' and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
	    	 }
	     }

	     //Modelli/Categorie aggiunti con addCampoOrdinamento
	     for(int i=0;i<campiOrdinamento.size();i++) {
	    	 if (campiOrdinamento.get(i) instanceof keyval) {
		    	 keyval k = (keyval)campiOrdinamento.get(i);

		    	 if (!sSelect.toString().equals("") && (k.getArea()!=null || k.getCategoria()!=null)) sSelect.append(" UNION ALL ");

		    	 if (k.getArea()!=null) {
		    		  contaModelliOrizzontali++;
		    		 sSelect.append(sSelectTestata);
		    		 sSelect.append("and m.area='"+k.getArea()+"' and m.codice_modello='"+k.getCm()+"' ");
		    		 sSelect.append("and u.object_name="+sNomeTabellaHoriz+" and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
		    	 }

		    	 if (k.getCategoria()!=null) {
		    		  contaModelliOrizzontali++;
		    		 sSelect.append(sSelectTestataVista);
		    		 sSelect.append("where u.object_name='"+k.getCategoria()+"_VIEW' and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
		    	 }
	    	 }
	     }

	     if (arRet!=null) {
	    	 if (!sSelect.toString().equals("")) sSelect.append(" UNION ALL ");

	    	 contaModelliOrizzontali++;
	    	 sSelect.append(sSelectTestata);
	    	 sSelect.append("and m.area='"+arRet+"' and m.codice_modello='"+cmRet+"' ");
	    	 sSelect.append("and u.object_name="+sNomeTabellaHoriz+" and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
	     }

	     if (categoRet!=null) {
	    	 if (!sSelect.toString().equals("")) sSelect.append(" UNION ALL ");

	    	 contaModelliOrizzontali++;
	    	 sSelect.append(sSelectTestataVista);
	    	 sSelect.append("where u.object_name='"+categoRet+"_VIEW' and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
	     }

    	 //Succede nei casi limite (ad esempio quando ho impostato solo l'area)
    	 //in tal caso restituisco un codice (-2) che significa, vatti a
	     //cercare i modelli dall'area e mettili nel vettore
    	 if (sSelect.toString().equals("")) return -2;

         try {

              dbOp = connect();

              dbOp.setStatement("select count(*) from ( "+sSelect.toString()+")");
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              rst.next();

              if (rst.getLong(1)==0)
            	  ret=0;
              else if (rst.getLong(1)==contaModelliOrizzontali)
            	  ret=1;
              else
            	  ret=-1;

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpTipoRicercaHorV()\n" + e.getMessage());
          }

      return ret;
   }

    /*
   * METHOD:      lookUpNomeTabellaOrizontaleByArCm(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION:
   *
   * RETURN:      String
  */
  public Vector lookUpElencoCodiciModelliOrizzontaliByArea(String ar) throws Exception
   {
         Vector vElenco = new Vector();

         try {
              StringBuffer sStm = new StringBuffer();

              dbOp = connect();

              sStm.append("SELECT CODICE_MODELLO ");
              sStm.append("FROM MODELLI M , user_objects u, TIPI_DOCUMENTO TD,AREE ");
              sStm.append("WHERE M.CODICE_MODELLO_PADRE IS NULL ");
              sStm.append("AND M.AREA='"+ar+"'");
              sStm.append("AND u.object_name=upper(nvl(AREE.ACRONIMO,'X'))||'_'||upper(nvl(ALIAS_MODELLO,'X')) and u.OBJECT_TYPE IN ('VIEW','TABLE') ");
              sStm.append("AND TD.ID_TIPODOC = M.ID_TIPODOC and TD.ALIAS_VIEW IS NOT NULL ");
              sStm.append("AND AREE.AREA=TD.AREA_MODELLO");

              dbOp.setStatement(sStm.toString());
              dbOp.execute();

              ResultSet rst = dbOp.getRstSet();

              while (rst.next() )
                    vElenco.add(rst.getString(1));

              close();
          }
          catch (Exception e) {
              close();
              throw new Exception("LookUpDMTable::lookUpElencoCodiciModelliOrizzontaliByArea(@,@) - (ar) ("+ar+")\n" + e.getMessage());
          }

      return vElenco;
   }

  public Vector<String> lookUpInfoAr_Cm_Cr_Area(String idDoc, String idObjFile, boolean bIsObjFileLog) throws Exception
  {
	  	  Vector<String> vRet= new Vector<String>();

	      if (en.Global.DM.compareTo(en.Global.FINMATICA_DM)==0)
	      {
	          try  {
	              StringBuffer sStm = new StringBuffer();

	              dbOp = connect();

	              sStm.append("Select decode(aree_path.ID_PATH_AREE_FILE,null,  nvl(aree.path_file,''),  nvl(aree_path.path_file,'')), TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || DOCUMENTI.ID_DOCUMENTO , aree.acronimo,tipi_documento.ACRONIMO_MODELLO  ");
	              sStm.append(" ,to_char(nvl(FORCE_FILE_ON_BLOB,0)), nvl(aree.DIM_MAX_ALL_BYTE,-1)  areeDimMaxAll, nvl(modelli.DIM_MAX_ALL_BYTE,-1)  modelliDimMaxAll, ");
	              sStm.append(" F_GET_MAXDIM_ATTACH(aree.area, modelli.codice_modello ),decode(INTEGRAZIONE_GDM_GDMSYNCRO.ping,1,nvl(modelli.ID_SERVIZIO_GDMSYNCRO,-1),-1),  ");
                  sStm.append(" decode(aree_path.ID_PATH_AREE_FILE,null,  nvl(aree.path_file_oracle,''),  nvl(aree_path.path_file_oracle,'')) , nvl(aree.path_file,'') ");

	              if (idObjFile==null) {
		              sStm.append(" from documenti, aree, tipi_documento, modelli, aree_path");
		              sStm.append(" where documenti.id_documento="+idDoc+" and documenti.area=aree.area and");
		              sStm.append(" documenti.id_tipodoc=tipi_documento.ID_TIPODOC and ");
		              sStm.append(" modelli.id_tipodoc=tipi_documento.ID_TIPODOC and ");
                      sStm.append(" aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+)");
	              }
	              else {
	            	  if (!bIsObjFileLog) {
                          sStm.append(" ,nvl(oggetti_file.PATH_FILE_ROOT,nvl(aree.path_file,'')) ");

			              sStm.append(" from documenti, aree, tipi_documento, oggetti_file, modelli, aree_path");
			              sStm.append(" where documenti.id_documento=oggetti_file.id_documento and documenti.area=aree.area and");
			              sStm.append(" documenti.id_tipodoc=tipi_documento.ID_TIPODOC and ");
			              sStm.append(" oggetti_file.ID_OGGETTO_FILE="+idObjFile+ " and ");
			              sStm.append(" modelli.id_tipodoc=tipi_documento.ID_TIPODOC and ");
                          sStm.append(" aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+)");
	            	  }
	            	  else {
                          sStm.append(" ,nvl(oggetti_file_log.PATH_FILE_ROOT,nvl(aree.path_file,'')) ");

	            		  sStm.append(" from documenti, aree, tipi_documento, oggetti_file_log, activity_log, modelli, aree_path");
			              sStm.append(" where documenti.id_documento=activity_log.id_documento and documenti.area=aree.area and");
			              sStm.append(" documenti.id_tipodoc=tipi_documento.ID_TIPODOC and ");
			              sStm.append(" oggetti_file_log.id_log=activity_log.id_log and ");
			              sStm.append(" oggetti_file_log.ID_OGGETTO_FILE_LOG="+idObjFile+ " and ");
			              sStm.append(" modelli.id_tipodoc=tipi_documento.ID_TIPODOC and ");
                          sStm.append(" aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+)");
	            	  }
	              }

	              dbOp.setStatement(sStm.toString());
	              dbOp.execute();

	              ResultSet rst = dbOp.getRstSet();

	              if (rst.next() ) {

	            	  vRet.add(rst.getString(1));
	            	  vRet.add(rst.getString(2));
	            	  vRet.add(rst.getString(3));
	            	  vRet.add(rst.getString(4));
	            	  vRet.add(rst.getString(5));
	            	  vRet.add(rst.getString(6));
	            	  vRet.add(rst.getString(7));
	            	  vRet.add(rst.getString(8));
	            	  vRet.add(rst.getString(9));
                      vRet.add(rst.getString(10));
                      vRet.add(rst.getString(11));
                      if (idObjFile!=null) {
                          vRet.add(rst.getString(12));
                      }

	              }
	              else {
	            	  if (idObjFile==null)
	            		  throw new Exception("LookUpDMTable::lookUpInfoAr_Cm_Cr_Area(@,@) - Informazione non trovata per idDoc="+idDoc+"\n");
	            	  else
	            		  throw new Exception("LookUpDMTable::lookUpInfoAr_Cm_Cr_Area(@,@) - Informazione non trovata per idOggettoFile="+idObjFile+"\n");
	              }


	              close();
	          }
	          catch (Exception e) {
	              close();
	              throw new Exception("LookUpDMTable::lookUpInfoAr_Cm_Cr_Area(@) - (idDoc) ("+idDoc+")\n" + e.getMessage());
	          }
      }

      return vRet;
   }

    public String getHashLog(String nomeFile,String idDocumento, String idLog, Long idOggettoFile) throws Exception {
        String hash=null;
        try {
            dbOp = connect();
            String sql, where;

            if (idOggettoFile==null) {
                where="FILENAME = '"+nomeFile.replaceAll("'","''")+"'";
            }
            else {
                where="ID_OGGETTO_FILE = "+idOggettoFile;
            }

            if (idLog==null)
                sql="SELECT hashcode FROM IMPRONTE_FILE WHERE id_documento = "+idDocumento+" and "+where;
            else
                sql="SELECT IMPRONTA FROM OGGETTI_FILE_lOG WHERE ID_LOG = "+idLog+" and "+where;

            dbOp.setStatement(sql);
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();
            if (rst.next()) hash=rst.getString(1);

            close();
        }
        catch (Exception e) {
            close();
            throw new Exception("LookUpDMTable::getHashLog(@,@) - (nomeFile,idDoc,idLog) ("+nomeFile+","+idDocumento+","+idLog+")\n" + e.getMessage());
        }

        return hash;
    }

  public String lookUpIdLogFromVersion(String idDoc,String idVersion) throws Exception
  {

        try {
             StringBuffer sStm = new StringBuffer();

             dbOp = connect();

             sStm.append("SELECT ID_LOG ");
             sStm.append("  FROM ACTIVITY_LOG ");
             sStm.append(" WHERE ID_DOCUMENTO= "+idDoc);
             sStm.append("   AND VERSIONE= "+idVersion);
             sStm.append("   AND TIPO_AZIONE= '"+Global.TYPE_AZIONE_REVISIONE+"'");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();

             ResultSet rst = dbOp.getRstSet();

             if (rst.next() ) return rst.getString(1);

             close();
         }
         catch (Exception e) {
             close();
             throw new Exception("LookUpDMTable::lookUpIdLogFromVersion(@,@) - (idDoc,idVersion) ("+idDoc+","+idVersion+")\n" + e.getMessage());
         }

         return null;
  }

    public String lookUpMinIdLogOggettoLogFromVersion(String idDoc,String idLog, String filename, Long idOggettoFile, boolean bCasoLog) throws Exception
    {

        String ret=null;
        try {
            StringBuffer sStm = new StringBuffer();

            dbOp = connect();

            sStm.append("SELECT ACTIVITY_LOG.ID_LOG ");
            sStm.append("  FROM ACTIVITY_LOG, OGGETTI_FILE_LOG ");
            sStm.append(" WHERE ID_DOCUMENTO= "+idDoc);
            /* con la revisione cerco all'indietro. Con il log standard cerco avanti */
            if (bCasoLog) {
                if (idOggettoFile==null)
                    sStm.append("   AND ACTIVITY_LOG.ID_LOG> "+idLog);
                else
                    sStm.append("   AND ACTIVITY_LOG.ID_LOG>= "+idLog);
            }
            else
                sStm.append("   AND ACTIVITY_LOG.ID_LOG<= "+idLog);
            if (bCasoLog)
                sStm.append("   AND ACTIVITY_LOG.TIPO_AZIONE in ('"+Global.TYPE_AZIONE_MODIFICA+"','"+Global.TYPE_AZIONE_ELIMINA+"','"+Global.TYPE_AZIONE_CREA+"')");
            else
                sStm.append("   AND ACTIVITY_LOG.TIPO_AZIONE= '"+Global.TYPE_AZIONE_REVISIONE+"'");
            sStm.append("   AND activity_log.id_log=oggetti_file_log.id_log ");
            if (idOggettoFile==null) {
                sStm.append("   AND oggetti_file_log.FILENAME = :FILENAME ");
            }
            else {
                sStm.append("   AND oggetti_file_log.ID_OGGETTO_FILE = "+idOggettoFile.toString()+" ");
            }


            if (!bCasoLog)
                sStm.append("   ORDER BY ACTIVITY_LOG.ID_LOG DESC ");
            else
                sStm.append("   ORDER BY ACTIVITY_LOG.ID_LOG ASC ");

            dbOp.setStatement(sStm.toString());
            if (idOggettoFile==null) dbOp.setParameter(":FILENAME",filename);
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            if (rst.next() ) ret= rst.getString(1);

            if (bCasoLog && ret==null) {
                //SOLO NEL CASO LOG (non caso REVISIONE)
                //Se non l'ho trovato fra i log sicuramente ci sarà l'unica riga
                //sulla oggetti file e la data_aggiornamento sarà la sua data di inserimento
                //perché se ci fosse stata una modifica ci sarebbe stato il log (se acceso)
                //quindi controllo che la data_aggiornamento dell'id_log cercato sia
                //maggiore della data_aggiornamento dell'oggetto_file. Questo
                //ci da la certezza che il file è stato inserito prima del log cercato
                //e quindi che per quella versione il file esisteva già.
                //In tal caso, invece di tornare NULL, torno 0, così il chiamante sa distinguere
                //i due casi
                //NULL= vallo a cercare nella oggetti_file
                //0= il file per quella versione non esisteva ancora
                sStm = new StringBuffer();
                sStm.append("SELECT 1 ");
                sStm.append("FROM ACTIVITY_LOG, OGGETTI_FILE ");
                sStm.append("WHERE ACTIVITY_LOG.ID_LOG ="+idLog);
                sStm.append("  AND ACTIVITY_LOG.ID_DOCUMENTO =OGGETTI_FILE.ID_DOCUMENTO");
                if (idOggettoFile==null) {
                    sStm.append("  AND OGGETTI_FILE.FILENAME =:FILENAME");
                }
                else {
                    sStm.append("  AND OGGETTI_FILE.ID_OGGETTO_FILE = "+idOggettoFile.toString()+" ");
                }
                sStm.append("  AND (NVL(OGGETTI_FILE.DATA_INSERIMENTO,to_date('01/01/1900','dd/mm/yyyy')) <= ACTIVITY_LOG.DATA_AGGIORNAMENTO OR ACTIVITY_LOG.TIPO_AZIONE='C') ");
                // System.out.println(sStm.toString());
                dbOp.setStatement(sStm.toString());
                if (idOggettoFile==null) dbOp.setParameter(":FILENAME",filename);
                dbOp.execute();

                rst = dbOp.getRstSet();

                if (!rst.next() ) ret= "0";
            }

            close();
        }
        catch (Exception e) {
            close();
            throw new Exception("LookUpDMTable::lookUpMinIdLogOggettoLogFromVersion(@,@) - (idDoc,idLog) ("+idDoc+","+idLog+")\n" + e.getMessage());
        }

        return ret;
    }

  public String lookUpCreationDate(String idDoc,String format) throws Exception
  {

        try {
             StringBuffer sStm = new StringBuffer();

             dbOp = connect();

             sStm.append("SELECT TO_CHAR(DATA_AGGIORNAMENTO,'"+format+"') ");
             sStm.append("  FROM ACTIVITY_LOG ");
             sStm.append(" WHERE ID_DOCUMENTO= "+idDoc);
             sStm.append("   AND TIPO_AZIONE= '"+Global.TYPE_AZIONE_CREA+"'");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();

             ResultSet rst = dbOp.getRstSet();

             if (rst.next() ) return rst.getString(1);

             close();
         }
         catch (Exception e) {
             close();
             throw new Exception("LookUpDMTable::lookUpCreationDate(@) - (idDoc) ("+idDoc+")\n" + e.getMessage());
         }

         return null;
  }

  private IDbOperationSQL connect() throws Exception {
        if (en.getDbOp()==null) {
           bIsNew=true;
           return (new ManageConnection(en.Global)).connectToDB();
        }

        return en.getDbOp();
  }

  private void close() throws Exception {
        if (bIsNew) (new ManageConnection(en.Global)).disconnectFromDB(dbOp,true,false);
  }

}

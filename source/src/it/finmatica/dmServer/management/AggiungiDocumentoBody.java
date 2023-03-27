package it.finmatica.dmServer.management;

/*
 * AGGIUNTA DI UN DOCUMENTO
 * PER MODULISTICA 2.2
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   19/09/2005
 * 
 * */

import java.io.*;
import it.finmatica.dmServer.*;

public class AggiungiDocumentoBody extends AggiungiDocumento 
{

  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
      
  /*
   * METHOD:      Constructor(String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Creazione di un documento a partire
   *              dall'idTipoDocumento e variabili di
   *              ambiente
   * 
   * RETURN:      none
  */   
   public AggiungiDocumentoBody(String idTipoDocumento, Environment vEnv) throws Exception
  {
         super (idTipoDocumento,  vEnv);
  }
  
  /*
   * METHOD:      Constructor(String,String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Creazione di un documento a partire
   *              dall'idTipoDocumento, area e variabili di
   *              ambiente
   * 
   * RETURN:      none
  */ 
  public AggiungiDocumentoBody(String tipoDocumento, String vArea, Environment vEnv) throws Exception
  {  
         super (tipoDocumento, vArea, vEnv);
  }
  
 // ***************** METODI DI GESTIONE DEI DOCUMENTI ***************** //
 
  /*
   * METHOD:      aggiungiDatiBody(String,Object,Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce nel vettore dei valori di aDocumento
   *              la coppia (idCampo,valore).
   *              idCampo è ottenuto a partire da campo e fromIdTipoDoc
   *              mediante lookUp
   * 
   * RETURN:      void
  */    
  public void aggiungiDatiBody(String fromIdTipoDoc, Object campo, Object valore) throws Exception
  {
        try {
              super.aggiungiDati( campo, valore);
        }                  
        catch (Exception e)
        {
           throw new Exception("AggiungiDocumentoBody::aggiungiDatiBody()\n"+ e.getMessage());
        }
  }
 
  /*
   * METHOD:      caricaDocumentoBody(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge un allegato al documento dato in input come
   *              InputStream.
   *              Il path serve per memorizzare anche il nome del file
   *              Viene passato anche il flag "allegato" (S o N)
   * 
   * RETURN:      void
  */   
  public void caricaDocumentoBody( String sHtml, String pathFile)  throws Exception
  {     
         try {
           super.aggiungiAllegato(new ByteArrayInputStream(sHtml.getBytes()), pathFile);
         }                  
         catch (Exception e)
         {
           //varEnv.
           throw new Exception("AggiungiDocumento::aggiungiAllegato()\n"+ e.getMessage());
         }

  }
  
  /*
   * METHOD:      caricaAllegatiBody(InputStream,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge un allegato al documento dato in input come
   *              InputStream.
   *               
   * RETURN:      void
  */   
  public void caricaAllegatiBody( InputStream istream, String pathFile, String padre)  throws Exception
  {     
         try {
           super.aggiungiAllegato(istream, pathFile, padre, "S");
         }                  
         catch (Exception e)
         {
           varEnv.disconnectClose();
           throw new Exception("AggiornaDocumento::aggiungiAllegato()\n"+ e.getMessage());
         }

  }
  
}
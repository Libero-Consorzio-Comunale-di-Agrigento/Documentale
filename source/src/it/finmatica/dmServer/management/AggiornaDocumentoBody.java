package it.finmatica.dmServer.management;

/*
 * GESTIONE DOCUMENTO
 * PER MODULISTICA 2.2
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   19/09/2005
 * 
 * */

import java.io.*;
import java.util.*;
import it.finmatica.dmServer.*;

public class AggiornaDocumentoBody extends AggiornaDocumento 
{
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
      
  /*
   * METHOD:      Constructor(String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiornamento di un documento a partire
   *              dall'idDocumento e variabili di
   *              ambiente
   * 
   * RETURN:      none
  */
  public AggiornaDocumentoBody(String idTipoDocumento, Environment vEnv) throws Exception
  {
         super (idTipoDocumento,  vEnv);
  }
  
   // ***************** METODI DI GESTIONE DEI DOCUMENTI ***************** // 

  /*
   * METHOD:      aggiungiDatiBody(String, Object, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce nel vettore dei valori di aDocumento
   *              la coppia (idCampo,valore).
   *              idCampo è ottenuto a partire da campo e fromIdTipoDoc
   *              mediante lookUp
   * 
   * RETURN:      void
  */
  public void aggiungiDatiBody(String fromIdTipoDoc,
                               Object campo, 
                               Object valore) throws Exception
  {
        try {
              super.aggiornaDati( campo, valore);
        }                  
        catch (Exception e)
        {
           throw new Exception("AggiornaDocumentoBody::aggiungiDatiBody()\n"+ e.getMessage());
        }
  }
  
  /*
   * METHOD:      caricaDocumentoBody(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge un allegato al documento dato in input come
   *              InputStream.
   * 
   * RETURN:      void
  */
  public void caricaDocumentoBody( String sHtml, String pathFile)  throws Exception
  {     
         try {
             super.aggiungiAllegato(null,new ByteArrayInputStream(sHtml.getBytes()), pathFile);
         }                  
         catch (Exception e)
         {
           varEnv.disconnectClose();
           throw new Exception("AggiornaDocumentoBody::caricaDocumentoBody()\n"+ e.getMessage());
         }

  } 
 
  /*
   * METHOD:      caricaAllegatiBody(InputStream, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge un allegato al documento dato in input come
   *              InputStream.
   * 
   * RETURN:      void
  */
  public void caricaAllegatiBody( InputStream istream, String pathFile, String padre )  throws Exception
  {     
         try {
           super.aggiungiAllegato(istream, pathFile, padre, "S");
         }                  
         catch (Exception e)
         {
           varEnv.disconnectClose();
           throw new Exception("AggiornaDocumentoBody::caricaAllegatiBody()\n"+ e.getMessage());
         }

  }
  
  /*
   * METHOD:      cancellaTuttiValoriWithBody()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancella tutti i valori.
   * 
   * RETURN:      void
  */
  public void cancellaTuttiValoriWithBody() throws Exception
  {     
         try { 
           aDocumento.cancellaAllValori();
         }
         catch (Exception e)
         {
           varEnv.disconnectClose();
           throw new Exception("AggiornaDocumento::cancellaTuttiValoriWithBody() cancellaAllValori\n"+ e.getMessage());
         }    
         
         aDocumento.getValori().clear();                   
  }
  
  /*
   * METHOD:      cancellaTuttiAllegatiWithBody(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancella tutti gli allegati.
   * 
   * RETURN:      void
  */
  public void cancellaTuttiAllegatiWithBody(String body) throws Exception
  {     
         Vector v = aDocumento.getOggettiFile();
         String bodyFlag="-1";

         if (v==null) return;

         for(int i=0;i<v.size();i++){            
            if (((A_Oggetti_File)v.elementAt(i)).getFileName().equals(body)) {
                bodyFlag=((A_Oggetti_File)v.elementAt(i)).getIdOggettoFile();
                continue;
            }

            try {
              aDocumento.cancellaOggettiFile(((A_Oggetti_File)v.elementAt(i)).getIdOggettoFile(),false);
            }
            catch (Exception e)
            {
              varEnv.disconnectClose();
              throw new Exception("AggiornaDocumento::cancellaTuttiAllegatiWithBody() cancellaOggettiFile non principali\n"+ e.getMessage());
            }
         }
         
         if (!bodyFlag.equals("-1")) { 
            try {
              aDocumento.cancellaOggettiFile(bodyFlag,false);
            }
            catch (Exception e)
            {
              varEnv.disconnectClose();
              throw new Exception("AggiornaDocumento::cancellaTuttiAllegatiWithBody() cancellaOggettiFile principale\n"+ e.getMessage());
            }  
         }

         aDocumento.getOggettiFile().clear();        
  }
}
package it.finmatica.dmServer.management;

import java.io.InputStream;
import java.util.Vector;

import it.finmatica.dmServer.A_Oggetti_File;
import it.finmatica.dmServer.ResultSetValoriLog;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.ValoriLogStruct;

/**
 * Gestione dello "storico" di un profilo (Documento del documentale).<BR>
 * <BR>
 * Esempio di utilizzo:<BR>
 * <BR> 		 
 * <BR>		   
 * 		   // Accesso al profiloLog con identificativo 43353<BR>
 * 		   // In alternativa si potrebbe utilizzare il costruttore<BR>
 * 		   // con tre parametri (area,codMod,codRich)<BR>	
 * <BR>
 * 		   ProfiloLog pLog = new ProfiloLog("43353");<BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User Ad4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection è possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   pLog.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>
 * 		   if (pLog.accedi().booleanValue()) <BR>   
 * 			  &nbsp;&nbsp;&nbsp;System.out.println(pLog.getCampo("ANNO_REG"));<BR>
 * 		   else<BR>
 * 			  &nbsp;&nbsp;&nbsp;System.out.println("Errore in accesso documento:\n"+pLog.getError());<BR>
 * <BR> 
 * I valori dei campi restituiti dopo aver effettuato l'accesso al profilo si riferiscono
 * ai valori conservati nella tabella valori_log come storico del documento.
 * Vengono estratti attraverso il seguente principio:
 * <BR>
 * Si cerca, su valori_log, il valore più recente relativo al campo 
 * richiesto (nell'esempio ANNO_REG), escludendo l'ultimo record
 * cronologicamente inserito perché questi farà riferimento all'attuale
 * valore del profilo e non al suo storico; se esiste il record richesto
 * questo sarà il valore restituito, in caso contrario viene fornito il 
 * valore attuale del campo per il profilo in questione. 
 * 
 * @author  D. Scandurra, G. Mannella
 * @version 2.8
*/ 

public class ProfiloLog extends ProfiloBase 
{       
   private ResultSetValoriLog rstVLog;
	
   /**
    * Costruttore da utilizzare in fase di
    * accesso di un profiloLog conoscendone la
    * chiave primaria
    *
    * @param idProfilo identificativo del profiloLog da accedere
   */
   public ProfiloLog(String idProfilo) {
          super(idProfilo);
   }

   /**
    * Costruttore da utilizzare in fase di
    * accesso di un profiloLog conoscendone la
    * tripla area/codice modello/codice richiesta
    *
    * @param codiceModello codice modello del profiloLog da accedere
    * @param area area del profiloLog da accedere
    * @param codiceRichiesta codice richiesta del profiloLog da accedere
   */   
   public ProfiloLog(String codiceModello, String area, String codiceRichiesta ) {
          super(codiceModello, area, codiceRichiesta);    
   }

   /**
    * Metodo che effettua l'accesso alle proprietà di un profiloLog.
    *
    * @return (True o False) Esito accesso
   */    
   public Boolean accedi() {
	      try{
           
             ad = new AccediDocumento(idDocumento,en);

             ad.accediLogDocumento();
                                       
             if (ad.aDocumento!=null) {
                cr=ad.aDocumento.getCodiceRichiesta();   
                area=(new DocUtil(en)).getAreaByIdDocumento(""+idDocumento);
                tipoDocumento=(new DocUtil(en)).getModelloByIdDocumento(""+idDocumento);
             }
             
             return new Boolean(true);
          }
          catch (Exception e) {
             error="ProfiloLog::accedi()\n"+e.getMessage();
             return new Boolean(false);
          }
   }    
   
   /**
    * Metodo che restituisce l'InputStream dell'allegato del profiloLog
    * dato l'indice nella lista dei file ultimamente loggati
    * 
    * @param index indice dell'allegato nella lista degli allegati.
    * 			   <B>L'indice si considera partente dalla posizione uno</B>
    * @return InputStream dell'allegato
   */   
   public InputStream getFileStream(int index) throws Exception {       
          try {
            if (index>ad.listaOggettiFile().size()) {
               error="ProfiloLog::getFileStream() - Si sta cercando di accedere al file n° "+index+". Il file non esiste sul documento!!";          
               throw new Exception(error);
            }

            ad.connect();
            
            //Se il file non è stato caricato (ACCESS_NO_ATTACH), lo carico "al volo"
            if ((((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile())==null)
                ad.caricaOggettoFile(((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getIdOggettoFile());          
                                   
            return (InputStream)((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile();
          }
          catch(Exception e) 
          {
            error="ProfiloLog::getFileStream(int index)\n"+e.getMessage();
            throw new Exception(error);
          }
   }

    public InputStream getFileStream(Long id) throws Exception {
        try {
            int i = getIndexFileById(""+id);

            return getFileStream(i+1);
        }
        catch (Exception e)
        {
            error="ProfiloLog::getFileStream(Long id)\n"+e.getMessage();
            throw new Exception(error);
        }
    }
   
   /**
    * Metodo che restituisce l'InputStream dell'allegato del profilo
    * dato il nome del file
    * 
    * @param nomeFile nome dell'allegato
    * @return InputStream dell'allegato
    * @see <a href="Profilo.html#getFileStream(int)">getFileStream(index)</a>
   */
   public InputStream getFileStream(String nomeFile) throws Exception {         
          try {
            int i = getIndexFileByName(nomeFile);

            return getFileStream(i+1);                                                           
          }
          catch (Exception e) 
          {
            error="ProfiloLog::getFileStream(String nomeFile)\n"+e.getMessage();
            throw new Exception(error);
          }              
   }        
   
   private int getIndexFileByName(String nomeFile) throws Exception {
           int size = ad.listaOggettiFile().size();
                              
           for(int i=0;i<size;i++) {
              String nome=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName();

              //if (nome.equals(nomeFile)) return i;
              //Confronto tra stringhe non case sensitive 
              if (nome.toUpperCase().equals(nomeFile.toUpperCase())) return i;
              //if (nome.equalsIgnoreCase(nomeFile)) return i;
                            
           }
           
           throw new Exception("ProfiloLog::getIndexFileByName(String nomeFile) - Non trovato il file "+nomeFile);
   }

    private int getIndexFileById(String idOggettoFile) throws Exception {
        int size = ad.listaOggettiFile().size();

        for(int i=0;i<size;i++) {
            String idFile=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getIdOggettoFile();

            //if (nome.equals(nomeFile)) return i;
            //Confronto tra stringhe non case sensitive
            if (idFile.toUpperCase().equals(idOggettoFile.toUpperCase())) return i;
            //if (nome.equalsIgnoreCase(nomeFile)) return i;

        }

        throw new Exception("ProfiloLog::getIndexFileById(String nomeFile) - Non trovato il file con id: "+idOggettoFile);
    }
   
   /**
    * Metodo che effettua l'accesso a tutti i log
    *
    * @return (True o False) Esito accesso
   */    
   public Boolean accediAllLog() {
	      try{
           
             ad = new AccediDocumento(idDocumento,en);
             
             rstVLog = new ResultSetValoriLog(ad.getListaValoriLog());
             
             return new Boolean(true);
          }
          catch (Exception e) {
             error="ProfiloLog::accediAllLog()\n"+e.getMessage();
             return new Boolean(false);
          }
   }       
   
   /**
    * Metodo che restituisce una struttura stile resultset
    * per scorrere i vari valoriLog del documento.
    * Ogni riga del resultset conterrà: tipo di azione
    * (creazione, modifica, cancellazione), data, utente
    * e il valore di ogni singolo campo richiesto  
   */
   public ResultSetValoriLog getResultSetValoriLog() {	   	  
	   
          try {        	  
        	if (ad==null || rstVLog==null) throw new Exception("E' necessario accedere a tutti i log!");
        	
        	return rstVLog;
          }
          catch(Exception e) 
          {
             error="ProfiloLog::getResultSetValoriLog()\n"+e.getMessage();
             return null;
          }
   }    
  
}

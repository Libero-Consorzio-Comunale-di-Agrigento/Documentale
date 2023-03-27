package it.finmatica.modulistica.deploy.cc;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileDB;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.jfc.utility.FileUtility;
import it.finmatica.jfc.zipper.ZipUtil;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;
import it.finmatica.modutils.informazionicampo.InformazioniCampo;
import it.finmatica.modutils.modificamodello.ModificaModello;

import java.io.*;
import java.sql.*;


/**
 * Classe per la gestione della pubblicazione di nuovi modelli 
 * (o l'update di vecchi)
 */
public class Deployer {  
  private ZipUtil         zipUtil = null;
 
  private Connection      sConn;
  private String          jndiStr;
  private int             tipoConn;
  private String          zipFileName = null;
  private String          absPathToUploadFolder = null;

  private String          sessionId = null;

  private String          contenutoFileModello = null;

  private IDbOperationSQL dbOpSQLMaster;

  public final static int MAX_LENGTH_MODELLO = 1000000;
  
 
  /**
   * Costruttore usato nel Caricatore.
   */
  public Deployer(String p_driver, String p_connstr, String p_utente, String p_passwd) throws Exception {
//System.out.println("Deployer"); 
    try {
      Class.forName(p_driver).newInstance();
      sConn = DriverManager.getConnection(p_connstr,p_utente,p_passwd);

      Parametri.leggiParametriConnection(sConn);
      dbOpSQLMaster = SessioneDb.getInstance().createIDbOperationSQL(sConn,0);
  
    } catch (Exception e) {
      throw new Exception("Deployer(): Errore in fase di costruzione del Deployer: "+e.toString());
    }

  }
  
  
  /**
   * 
   */
  public Deployer(String jndiStr, int tipoConn, String p_driver, String p_server, String p_utente, String p_passwd) throws Exception {
//System.out.println("Deployer"); 
    try {
      Class.forName(p_driver).newInstance();
      sConn = DriverManager.getConnection(p_server,p_utente,p_passwd);
      this.jndiStr = jndiStr;
      this.tipoConn = tipoConn;

      Parametri.leggiParametriConnection(sConn);
      dbOpSQLMaster = SessioneDb.getInstance().createIDbOperationSQL(jndiStr,tipoConn);
    
    } catch (Exception e) {
      throw new Exception("Deployer(): Errore in fase di costruzione del Deployer: "+e.toString());
    }
  }
  
  
    /**
   * Costruttore con Datasource JNDI. Usato dal Duplicatore.
   **/
  public Deployer(String jndiStr, int tipoConn, String zipFileName,
                  String absPathToUploadFolder,
                  String sessionId) throws Exception {
//System.out.println("Deployer");    
    this.jndiStr = jndiStr;
    this.tipoConn = tipoConn;
    this.zipFileName = zipFileName;
    this.absPathToUploadFolder = absPathToUploadFolder;
    this.sessionId = sessionId;

    dbOpSQLMaster = SessioneDb.getInstance().createIDbOperationSQL(jndiStr,tipoConn);
    
    this.sConn = dbOpSQLMaster.getConn();

    Parametri.leggiParametriConnection(this.sConn);   
  }

  
  /**
   * Costruttore
   **/
  public Deployer(Connection sConn, String zipFileName,
                  String absPathToUploadFolder,
                  String sessionId) throws Exception {
//System.out.println("Deployer");                           
    this.sConn=sConn;
    this.zipFileName = zipFileName;
    this.absPathToUploadFolder = absPathToUploadFolder;
    this.sessionId = sessionId;

    Parametri.leggiParametriConnection(sConn);

    dbOpSQLMaster = SessioneDb.getInstance().createIDbOperationSQL(sConn,0);

  }


  /**
   * Crea la directory <sessionId> (per distinguere le richieste di deploy fatte
   * da utenti web diversi) al percorso indicato <absPathToUploadFolder> e 
   * scompatta il file zip <zipFileName> indicato.
   * **/  
  public void trattaZip()throws Exception {
//System.out.println("trattaZip"); 
    String absPathToSessionFolder = absPathToUploadFolder+File.separator+sessionId;

      String absPathToZipFile = absPathToSessionFolder+File.separator+zipFileName;
    // Crea la directory e scompatta il file   

    try {
      File fileFolder=new File(absPathToSessionFolder);
      if (fileFolder.exists()){

        zipUtil = new ZipUtil();

        zipUtil.estraiZip(absPathToZipFile, absPathToSessionFolder);

      } 
    }catch (Exception e) {
      throw new Exception("trattaZip(): Errore in fase di unzip del modello: " + e.toString());
      }   
    }
  

  /**
   * Crea la directory <sessionId> (per distinguere le richieste di download fatte
   * da utenti web diversi) al percorso indicato <absPathToUploadFolder> e 
   * crea il file zip <zipFileName> indicato.
   **/
  public void creaModelloZip()throws Exception {
//System.out.println("creaModelloZip");     
    String absPathToSessionFolder = absPathToUploadFolder+File.separator+sessionId;

    String absPathToZipFile = absPathToSessionFolder+File.separator+zipFileName;
    String codice = getCodiceModello();
    String absPathToModello = absPathToSessionFolder+File.separator+codice+".html";
    String absPathToGrafici = absPathToSessionFolder+File.separator+codice+"_file";
    // Crea la directory e scompatta il file   

    try {
      File fileFolder=new File(absPathToSessionFolder);
      if (!fileFolder.exists()){
        /*boolean ok = */new File(absPathToSessionFolder).mkdirs();
        fileFolder=new File(absPathToSessionFolder);
      }  
      zipUtil = new ZipUtil();
      zipUtil.creaZip(absPathToZipFile);
      zipUtil.add(absPathToModello);
      zipUtil.add(absPathToGrafici); 
      zipUtil.close();
              
    }catch (Exception e) {
      throw new Exception("creaModelloZip(): Errore in fase di compressione del modello: " + e.toString());
      }   
    }
  
  
  /**
   * Carica il contenuto del file modello in forma di stringa.
   **/
   public void caricaModello()throws Exception{
//System.out.println("caricaModello");      
    // Crea la stringa che contiene il modello. 

    String absPathToSessionFolder = "";

    if (sessionId.compareTo("") == 0){
      absPathToSessionFolder = absPathToUploadFolder;
    }else{
      absPathToSessionFolder = absPathToUploadFolder+File.separator+sessionId;
    }
 
    String percorsoCompletoAlFileModello = "";

    int i = zipFileName.lastIndexOf(".zip");
    if (i>0){
      percorsoCompletoAlFileModello = absPathToSessionFolder+File.separator+zipFileName.substring(0,i);
    }else{
      percorsoCompletoAlFileModello = absPathToSessionFolder+File.separator+zipFileName;
    }
    // Verifica che si tratta di un file esistente.
    File file = new File(percorsoCompletoAlFileModello);

    if ( (!file.exists() || (!file.isFile())) ) {
      throw new Exception("caricaModello(): Non trovato il file del modello.");
    }
        
   try {     
     setContenutoFileModello(FileUtility.fileReader(percorsoCompletoAlFileModello));
     } catch (Exception e) {
       throw new Exception("caricaModello(): Impossibile accedere al file del modello. "+ e.toString());
     }
   }


  /**
   * Scrive il contenuto della stringa in un file (il nuovo file modello)
   **/
   public void scaricaModello()throws Exception{
//System.out.println("scaricaModello");     
    // Crea la stringa che contiene il modello. 

    String absPathToSessionFolder = "";

    if (sessionId.compareTo("") == 0){
      absPathToSessionFolder = absPathToUploadFolder;
    }else{
      absPathToSessionFolder = absPathToUploadFolder+File.separator+sessionId;
    }

   String percorsoCompletoAlFileModello = "";

    int i = zipFileName.lastIndexOf(".zip");
    if (i>0){
      percorsoCompletoAlFileModello = absPathToSessionFolder+File.separator+zipFileName.substring(0,i);
    }else{
      percorsoCompletoAlFileModello = absPathToSessionFolder+File.separator+zipFileName;
    }
           
    try {      
      String contenuto = getContenutoFileModello();
      char buffer[] = new char[contenuto.length()];
      contenuto.getChars(0,contenuto.length(),buffer,0);
      FileWriter f = new FileWriter(percorsoCompletoAlFileModello);
      f.write(buffer);
      f.close();
    } catch (Exception e) {
        throw new Exception("scaricaModello(): Errore nella scrittura del nuovo file modello: "+ e.toString());
    }
  }

   
  /**
   * Ripristina i Tag ADS per la preelaborazione del modello
  */
  private File ripristinaTagIn(File fileModello) throws Exception {
  
  try {      
      String contenuto = getContenutoFileModello();
      char buffer[] = new char[contenuto.length()];
      contenuto.getChars(0,contenuto.length(),buffer,0);
      FileWriter f = new FileWriter(fileModello.getAbsolutePath());
      f.write(buffer);
      f.close();
      return (fileModello);
    } catch (Exception e) {
        throw new Exception("ripristinaTagIn(): Errore nella scrittura del nuovo file modello: "+ e.toString());
    }
  
  }  
  
  
   /**
   * Testa l'esistenza o meno del modello sul DB
   **/   
   public boolean esisteModello(String area, String codice) throws Exception {
//System.out.println("esisteModello"); 
    
    ResultSet       rst;
    String          query;
    boolean         result;
    
    query = "SELECT * FROM MODELLI WHERE "+
              "AREA = '"+area+"' and "+
              "CODICE_MODELLO = '"+codice+"'";
    try {
               
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
    
      result = rst.next();
      
    } catch(Exception e) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      throw new Exception("esisteModello(): Errore nella SELECT ["+query+"]: "+e.toString());
    }
    return result;
  }

   
  /**
   * Testa l'esistenza o meno della vista sul DB nel caso in cui non c'è ancora il modello
   */   
   public boolean esisteVista(String vista) throws Exception {
//System.out.println("esisteVista");      
  
    ResultSet       rst;
    String          query;
    boolean         result = false;

    query = "SELECT * FROM TIPI_DOCUMENTO WHERE "+
              "ALIAS_VIEW = '"+vista+"'";
    try {
                  
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
    
      if (rst.next()){
        result=true;    
      }
    } catch(Exception e) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      throw new Exception("esisteVista(): Errore nella SELECT ["+query+"]: "+e.toString());
    }
    return result;
   }
   

   /**
   * Testa l'esistenza o meno della vista sul DB per un modello e quindi un tipo documento diverso
   * (nel caso in cui non c'è ancora il modello)
   **/   
   public boolean esisteAltraVista(String vista, String area, String codice) throws Exception {   
    ResultSet       rst;
    String          query;
    boolean         result = false;
//System.out.println("esisteAltraVista");
    query = "SELECT * FROM TIPI_DOCUMENTO td, MODELLI m WHERE "+
              "td.ALIAS_VIEW = '"+vista+"' "+
              "AND td.ID_TIPODOC <> m.ID_TIPODOC "+
              "AND m.AREA = '"+area+"' "+
              "AND m.CODICE_MODELLO = '"+codice+"'";
    try {
                  
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
    
      if (rst.next()){
        result=true;    
      }
     
    } catch(Exception e) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      throw new Exception("esisteAltraVista(): Errore nella SELECT ["+query+"]: "+e.toString());
    }
    return result;
   }
   

   /**
   * Testa l'esistenza o meno  per il tipo documento di una vista sul DB con nome diverso
   * (nel caso in cui c'è già il modello) ed eventualmente fa la DROP
   **/   
   public boolean eliminaAltroNomeVista(String vista, String area, String codice) throws Exception {
    ResultSet       rst;
    String          query, oldview, dropStm;
    boolean         result = false;
//System.out.println("eliminaAltroNomeVista");
    query = "SELECT td.ALIAS_VIEW FROM TIPI_DOCUMENTO td, MODELLI m WHERE "+
              "td.ALIAS_VIEW <> '"+vista+"' "+
              "AND td.ALIAS_VIEW IS NOT NULL "+
              "AND td.ID_TIPODOC = m.ID_TIPODOC "+
              "AND m.AREA = '"+area+"' "+
              "AND m.CODICE_MODELLO = '"+codice+"'";

    try {      
            
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();

      if (rst.next()){
        oldview = rst.getString(1);
        dropStm = "DROP VIEW GDM_"+oldview;

        dbOpSQLMaster.setStatement(dropStm);
        dbOpSQLMaster.execute();
      }    
      
      result = true;     
      
    } catch(Exception e) {
     //Non restituiamo nessun errore
    }
    return result;
   }
   
   
   /**
   * Testa l'esistenza o meno del modello sul DB
   */   
   public boolean esisteTipoDoc(String codice) throws Exception {
//System.out.println("esisteTipoDoc");    
   
    ResultSet       rst;
    String          query;
    boolean         result=false;
    
    query = "SELECT * FROM TIPI_DOCUMENTO WHERE "+
              "NOME = '"+codice+"'";
    try {      
            
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
    
      if (rst.next()){
        result=true;    
      }
      
    } catch(Exception e) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      throw new Exception("esisteTipoDoc(): Errore nella SELECT ["+query+"]: "+e.toString());
    }
    return result;
  }
   
  
   /*
    * Testa l'esistenza di un valore non nullo 
    * per il campo <nomeCampo> nella tabella <nomeTabella>
    */
   private boolean giaSettato(String nomeCampo, String nomeTabella, String sWhere)throws Exception{
     //System.out.println("giaSettato"); 
     boolean retVal = false;
     String valoreCampo = ""; 
     ResultSet rst = null;
     
     String query = "SELECT "+nomeCampo+" FROM " +nomeTabella+ " WHERE " +sWhere;
     try{
       dbOpSQLMaster.setStatement(query);
       dbOpSQLMaster.execute();
       rst = dbOpSQLMaster.getRstSet();
       if (rst.next()){
         valoreCampo = rst.getString(nomeCampo);
         
         if ((valoreCampo == null) || (valoreCampo.length() == 0)){
           retVal = false;
         }else{
           retVal = true;
         }
       }
      
 //      System.out.println("nomeCampo= "+nomeCampo+"valoreCampo= "+valoreCampo+"retVal= "+retVal);
     } catch(Exception e) {
    	 rollbackDeployer();
       free(dbOpSQLMaster);
       throw new Exception("giaSettato(): Errore nella ricerca del campo "+
                           nomeCampo+" in tabella "+nomeTabella+": "+e.toString());       
     }
     return retVal;   
   
   }
   
   
   /*
    * Testa l'esistenza di almeno un documento di tipo_documento <idTipoDoc>     
    */
   private boolean esisteUnDocumento(int idTipoDoc)throws Exception{
     //System.out.println("esisteUnDocumento"); 
     boolean retVal = false;
     
     String query = "SELECT 1 FROM DUAL "+
                    "WHERE EXISTS( "+
                      "SELECT 1 FROM DOCUMENTI WHERE ID_TIPODOC = "+idTipoDoc+" )";
     try{
       dbOpSQLMaster.setStatement(query);
     
       dbOpSQLMaster.execute();
       if (dbOpSQLMaster.getRstSet().next()){
         retVal = true;
       }
     }catch (Exception e) {
       System.out.println("DeployCC::esisteUnDocumento: "+e.toString());   
       e.printStackTrace();
     }
     return retVal;
   }

   
   /**
   * Pre-elaborazione di un modello prima del deploy
   * @param modello è il modello da elaborare
   * @param tipo è il tipo di modello che stiamo esaminando
   * @exception genera un exception se non è possibile interpretare il modello
   * @return modello preelaborato secondo il tipo passato, di default torna il modello stesso
   *         se non è in grado di interpretare correttamente il modello passato torna il modello stesso
   **/
  private String preElabora(String modello, String tipo) throws Exception {
    int       startChar,
              j,
              k,
              tempIndex;
    String    subModello,
              parteModello1,
              parteModello2,
              parteModello3;
    String nuovoModello = "";             
//System.out.println("preElabora");            
 
      // Se un campo si presenta nella forma {nomecampo} <!ADSPROPERTY campo ADSFORMAT>
      // per evitare problemi di interpretazione trasforma le stringhe in:
      //    {nomecampo} <!ADSPROPERTY ==> ADSPROPERTY
      //    ADSFORMAT> ==> ADSFORMAT
      // isolando cosi la sola definizione del campo fra i due tag di campo.
      startChar = 0;
      subModello = modello.substring(startChar);
      // Primo ciclo di esame ed estrazione dei campi
      j = 0;
      while ((j != -1) && (!subModello.equals(""))) {   
        j = subModello.indexOf(Parametri.getNameFieldBegin()); //&lt;<!-- ADSPROPERTY
        if (j != -1) {      
          // C'è almeno un altro campo e j è la posizione iniziale del tag di identificazione <!ADSPROPERTY)
          parteModello1 = subModello.substring(0, j);
          k = subModello.indexOf(Parametri.getNameFieldEnd())+(Parametri.getNameFieldEnd()).length(); //ADSFORMAT -->
          if (k != -1) {
            parteModello2 = subModello.substring(j, k);
            int startCh = 0,endChar;
            String sHtml = "";
            startCh = subModello.indexOf(Parametri.getTagFieldEnd())+(Parametri.getTagFieldEnd()).length(); //<!-- ADSTFE -->
            endChar = startCh;
            sHtml = subModello.substring(startCh,startCh + 10);
            if (sHtml.equalsIgnoreCase("<!-- style") ||
                sHtml.equalsIgnoreCase("&gt;<!-- s")) {
              startCh = subModello.indexOf("<!-- style");
              endChar = subModello.indexOf("-->",startCh);
              if (endChar != -1) {
                endChar += 3;
              } else {
                endChar = startCh;
              }
            }
            parteModello2 += subModello.substring(startCh,endChar);
            parteModello3 = subModello.substring(endChar);
            // Ora esamino la parte 1 e la parte 3 e le "pulisco" poi ricompongo il modello ...
            tempIndex = parteModello1.lastIndexOf(Parametri.getTagFieldBegin()); //&lt;<!-- ADSTFB -->
            if (tempIndex != -1) {
              //parteModello1 = parteModello1.substring(0, tempIndex);
              parteModello1=parteModello1.replaceAll(Parametri.getTagFieldBegin(),""); 
              
              int indchiudi = parteModello3.indexOf("&gt;");
              if (indchiudi > 0){
                String parteModello3New="&gt;"+parteModello3.substring(0,indchiudi)+parteModello3.substring(indchiudi+4);           
                parteModello3=parteModello3New;
              }
              
              subModello = parteModello3; 
              nuovoModello = nuovoModello + parteModello1 + parteModello2;
            } else {
              // ERRORE
              System.out.println("Traduzione del modello non riuscita correttamente !!");
              System.out.println("Localizzazione errore: "+subModello.substring(0, 20));
              throw new Exception("preElabora(): Traduzione del modello non riuscita correttamente. Localizzazione errore: "+subModello.substring(0, 20));

            }
          } else {
            // ERRORE 
            System.out.println("Traduzione del modello non riuscita correttamente !!");
            System.out.println("Localizzazione errore: "+subModello.substring(0, 20));
            throw new Exception("preElabora(): Traduzione del modello non riuscita correttamente. Localizzazione errore: "+subModello.substring(0, 20));
           
          }        
        } else {
            // la parte restante va attaccata in coda comunque 
            nuovoModello = nuovoModello + subModello;
        }
      }     
    
    return nuovoModello;
  }

  
  /**
   * Pre-elaborazione di un modello prima del deploly
   */
  private String preElaboraBlocchi(String modello) throws Exception {
    int       startChar,
              j,
              k,
              tempIndex;
    String    subModello,
              parteModello1,
              parteModello2,
              parteModello3,
              nuovoModello;
//System.out.println("preElaboraBlocchi");           
    nuovoModello = "";
         
      //    trasforma le stringhe in:
      //    {nomeblocco} <!BLOCCOPROPERTY ==> BLOCCOPROPERTY
      //    BLOCCOFORMAT> ==> BLOCCOFORMAT
     
      startChar = 0;
      subModello = modello.substring(startChar);
      // Primo ciclo di esame ed estrazione dei blocchi
      j = 0;
      while ((j != -1) && (!subModello.equals(""))) {   
        j = subModello.indexOf(Parametri.getNameBlockBegin());
        if (j != -1) {      

          // C'è almeno un altro blocco e j è la posizione iniziale del tag di identificazione <!ADSPROPERTY)
          parteModello1 = subModello.substring(0, j);
          k = subModello.indexOf(Parametri.getNameBlockEnd())+(Parametri.getNameBlockEnd()).length();
          if (k != -1) {

            parteModello2 = subModello.substring(j, k);
            int startCh = 0,endChar;
            String sHtml = "";
            startCh = subModello.indexOf(Parametri.getTagBlockEnd())+(Parametri.getTagBlockEnd()).length();
            endChar = startCh;
            sHtml = subModello.substring(startCh,startCh + 10);
            if (sHtml.equalsIgnoreCase("<!-- style") ||
                sHtml.equalsIgnoreCase("&gt;<!-- s")) {
              startCh = subModello.indexOf("<!-- style");
              endChar = subModello.indexOf("-->",startCh);
              if (endChar != -1) {
                endChar += 3;
              } else {
                endChar = startCh;
              }
            }
            parteModello2 += subModello.substring(startCh,endChar);
            parteModello3 = subModello.substring(endChar);
            
            // Ora esamino la parte 1 e la parte 3 e le "pulisco" poi ricompongo il modello ...
            tempIndex = parteModello1.lastIndexOf(Parametri.getTagBlockBegin()); //&lt;<!-- ADSTBB -->
            if (tempIndex != -1) {
              //parteModello1 = parteModello1.substring(0, tempIndex);
              parteModello1=parteModello1.replaceAll(Parametri.getTagBlockBegin(),""); 

              int indchiudi = parteModello3.indexOf("&gt;");
              if (indchiudi > 0){
                String parteModello3New="&gt;"+parteModello3.substring(0,indchiudi)+parteModello3.substring(indchiudi+4);           
                parteModello3=parteModello3New;
              }     
              subModello = parteModello3; 
              nuovoModello = nuovoModello + parteModello1 + parteModello2;
            } else {
              // ERRORE
              System.out.println("PreelaboraBlocchi: 1. Traduzione del modello non riuscita correttamente !!");
              System.out.println("Localizzazione errore: "+subModello.substring(0, 20));
              throw new Exception("preElaboraBlocchi(): Traduzione del modello non riuscita correttamente. Localizzazione errore: "+subModello.substring(0, 20));
            
            }
          } else {
            // ERRORE 
            System.out.println("PreelaboraBlocchi: 2. Traduzione del modello non riuscita correttamente !!");
            System.out.println("Localizzazione errore: "+subModello.substring(0, 20));
            throw new Exception("preElaboraBlocchi(): Traduzione del modello non riuscita correttamente. Localizzazione errore: "+subModello.substring(0, 20)); 
           
          }        
        } else {
            // la parte restante va attaccata in coda comunque 
            nuovoModello = nuovoModello + subModello;
        }
      }     
   
    return nuovoModello;
  }


  public boolean pubblica(String area,
        String codice,
        String codicePadre,
        String revisione,
        String autore,
        String tipo,
        String uso,
        String mComp,
        String gComp,
        String gCrea,
        String vista,
        String modello_successivo,
        String utente,
        File   fileModello,
        String stile,
        String prov,
        String acronimo_modello,
        String alias_modello) throws Exception {
      return pubblica(area,codice,codicePadre,revisione,
          autore,tipo,uso,mComp,gComp,gCrea,vista,modello_successivo,utente,
          fileModello,null,stile,prov,acronimo_modello,alias_modello);
  }

   public boolean pubblica(String area,
        String codice,
        String codicePadre,
        String revisione,
        String autore,
        String tipo,
        String uso,
        String mComp,
        String gComp,
        String gCrea,
        String vista,
        String modello_successivo,
        String utente,
        String stringaModello,
        String stile,
        String prov,
        String acronimo_modello,
        String alias_modello) throws Exception {
        return pubblica(area,codice,codicePadre,revisione,
            autore,tipo,uso,mComp,gComp,gCrea,vista,modello_successivo,utente,
            null,stringaModello,stile,prov,acronimo_modello,alias_modello);
   }

/**
   * Lancia il deploy sul modello (attenzione: il file zip arriverà 
   * nel formato <nomemodello.estensione.zip>, così il modello da
   * deployare sarà <nomemodello.estensione>).
   **/
  public boolean pubblica(String area, 
                                String codice, 
                                String codicePadre,
                                String revisione, 
                                String autore,
                                String tipo, 
                                String uso,
                                String mComp,
                                String gComp,
                                String gCrea,
                                String vista,
                                String modello_successivo,
                                String utente,
                                File   fileModello,
                                String stringaModello,
                                String stile,
                                String prov,
                                String acronimo_modello,
                                String alias_modello) throws Exception {
//System.out.println("pubblica1");  

    String            modStm, viewStm;
    boolean           nuovo = false, figlio = false;
    Timestamp         dataDiSistema = dbOpSQLMaster.getSysdate();
    File              newfileModello = null;
   
  
    // ----------------------------------------------------------------------------------------
    // 1. Verifica se inserire o aggiornare il modello. L'inserimento/aggiornamento vero e proprio
    // avverrà solo attraverso un update finale sul campo CLOB
    // ----------------------------------------------------------------------------------------
    try {
      if (codicePadre.compareTo("") == 0){ //se è stringa vuota, non c'è un padre
        figlio = false;
      }else{
        figlio = true;
      }
      ///
//    Se proviene da Caricatore o Duplicatore, sostituisce l'area nel modello
      if (prov.equalsIgnoreCase("C") || prov.equalsIgnoreCase("D")) {

        try 
        {
          String myPath = fileModello.getAbsolutePath();
          String myMod = FileUtility.fileReader(myPath);
          int i = myMod.lastIndexOf("<o:_AREA");
          int j = myMod.lastIndexOf("</o:_AREA>");
          String sArea = "";
          if ((i != -1) && (j != -1)) {
            sArea = myMod.substring(i+24, j);
          }
          if ((i != -1) && (j != -1)) {
            myMod = myMod.substring(0,i+24)+area+ myMod.substring(j);
          }
          myMod = myMod.replaceAll("modelli/"+sArea+"/","modelli/"+area+"/" );
          FileUtility.createDataFile(myPath,myMod,true);
          newfileModello = new File(myPath);
        } catch(Exception e) {   
//        fa rollback, chiude e esce
        	rollbackDeployer();
          free(dbOpSQLMaster);
          throw new Exception("pubblica(): Errore in fase di sostituzione area del modello. "+ e.toString());
        }
      } else {
      	 // altrimenti (non proviene dal caricatore o dal duplicatore) 
      	//  ripristina i tag del modello
      	try {
      	    if (fileModello!=null) newfileModello = ripristinaTagIn(fileModello);
      } catch(Exception e) {   
//        fa rollback, chiude e esce
      	  rollbackDeployer();
          free(dbOpSQLMaster);
          throw new Exception("pubblica(): "+ e.toString());
        }
      }
      ///
      
      if (esisteModello(area,codice) == false) {  // Il modello non esiste, quindi lo inserisco

        if ( (codicePadre.compareTo("") == 0) && (vista.compareTo("") != 0) && esisteVista(vista)){

          if ((!prov.equalsIgnoreCase("C")) && (!prov.equalsIgnoreCase("D"))) {
            System.out.println("DeployerModel::pubblica(): Esiste già la vista "+vista+". Pubblicazione non effettuata.");
            throw new Exception("pubblica(): Esiste già la vista "+vista+". Pubblicazione non effettuata.");
          }else{          
            System.out.println("DeployerModel::pubblica(): Esiste già la vista "+vista+". Creazione vista non effettuata.");
            vista = "";
          }
        }            
              
        
        if (!prov.equalsIgnoreCase("C")){
          modStm = "INSERT INTO MODELLI "+
                   "(AUTORE,  VALIDO, DATA_VARIAZIONE,   DATA_INSERIMENTO,  DATA_PUBBLICAZIONE,  TIPO,  TIPO_USO, AREA,  CODICE_MODELLO, CODICE_MODELLO_PADRE, MODELLO_SUCCESSIVO, STILE, BLOCCO_JDMS, FILE_ORIGINALE) VALUES "+
                   "(:AUTORE, :VALIDO, :DATA_VARIAZIONE, :DATA_INSERIMENTO, :DATA_PUBBLICAZIONE, :TIPO, :TIPO_USO, :AREA, :CODICE_MODELLO, :CODICE_MODELLO_PADRE, :MODELLO_SUCCESSIVO, :STILE, :BLOCCO_JDMS, :FILE_ORIGINALE)";
        }else{
          modStm = "INSERT INTO MODELLI "+
                   "(AUTORE,  VALIDO, DATA_VARIAZIONE,   DATA_INSERIMENTO,  DATA_PUBBLICAZIONE,  TIPO,  TIPO_USO, AREA,  CODICE_MODELLO, CODICE_MODELLO_PADRE, MODELLO_SUCCESSIVO, STILE, FILE_ORIGINALE) VALUES "+
                   "(:AUTORE, :VALIDO, :DATA_VARIAZIONE, :DATA_INSERIMENTO, :DATA_PUBBLICAZIONE, :TIPO, :TIPO_USO, :AREA, :CODICE_MODELLO, :CODICE_MODELLO_PADRE, :MODELLO_SUCCESSIVO, :STILE, :FILE_ORIGINALE)";
        }
        dbOpSQLMaster.setStatement(modStm);
        nuovo = true;
         
      } else {  // Il modello esiste, quindi lo aggiorno ed eventualmente lo storicizzo
 

        if ( (codicePadre.compareTo("") == 0) && (vista.compareTo("") != 0) && esisteAltraVista(vista, area, codice)){
          if ((!prov.equalsIgnoreCase("C")) && (!prov.equalsIgnoreCase("D"))) {          
          	System.out.println("DeployerModel::pubblica() - Esiste già la vista "+vista+" per un altro Tipo Documento. Pubblicazione non effettuata.");
            throw new Exception("pubblica(): Esiste già la vista "+vista+" per un altro Tipo Documento. Pubblicazione non effettuata.");
          }else{
            System.out.println("DeployerModel::pubblica() - Esiste già la vista "+vista+" per un altro Tipo Documento. Creazione vista non effettuata.");
            vista = ""; 
          }
        }
        else{   

          if( codicePadre.compareTo("") == 0){
            try{   
              eliminaAltroNomeVista(vista, area, codice);
            }catch(Exception ev){
              System.out.println("DeployerModel::pubblica() - Errore (non bloccante) nella cancellazione della vecchia vista: "+ev.toString());
            }    
          }
          if (revisione!= "0"){  
            try{
              storicizza(area, codice, revisione);       
            }catch(Exception est){            	
              System.out.println("DeployerModel::pubblica() - Errore in fase di salvataggio revisione: "+est.toString());
              throw new Exception("pubblica(): Errore in fase di salvataggio revisione: "+est.getMessage());
            } 
          } 
          modStm = "UPDATE MODELLI "+ 
                 "SET "+
                    "AUTORE = :AUTORE, "+
                    "VALIDO = :VALIDO, "+
                    "DATA_PUBBLICAZIONE = :DATA_PUBBLICAZIONE, "+
                    "DATA_VARIAZIONE = :DATA_VARIAZIONE, "+
                    "TIPO = :TIPO, "+
                    "TIPO_USO = :TIPO_USO, "+
                    "MODELLO_SUCCESSIVO = :MODELLO_SUCCESSIVO, "+
                    "STILE = :STILE, "+  
                    "FILE_ORIGINALE = :FILE_ORIGINALE "+
                  "WHERE "+
                    "AREA = :AREA AND "+
                    "CODICE_MODELLO = :CODICE_MODELLO";
                 
          dbOpSQLMaster.setStatement(modStm);
          nuovo = false;     
        }
      }
      
      
//System.out.println(bloccoJdms);
      ///
        FileInputStream fInput = null;
//    	System.out.println("percorso nuovo file modello "+newfileModello.getPath());
//    	if (newfileModello.exists()){     
//    		System.out.println("il file esiste");
//			}else{
//			System.out.println("il file non esiste");
//			}
        if (stringaModello!=null) {
            ByteArrayInputStream newfileModelloStringa = new ByteArrayInputStream(stringaModello.getBytes());
            dbOpSQLMaster.setAsciiStream(":FILE_ORIGINALE", newfileModelloStringa, newfileModelloStringa.available());
        }
        else {
            fInput = new FileInputStream(newfileModello);
            dbOpSQLMaster.setAsciiStream(":FILE_ORIGINALE", (InputStream)fInput, fInput.available());
        }

      ///
      dbOpSQLMaster.setParameter(":AUTORE",autore);
      dbOpSQLMaster.setParameter(":VALIDO","S");
      dbOpSQLMaster.setParameter(":DATA_PUBBLICAZIONE",dataDiSistema);
      dbOpSQLMaster.setParameter(":DATA_VARIAZIONE",dataDiSistema);
      dbOpSQLMaster.setParameter(":TIPO",tipo);
      dbOpSQLMaster.setParameter(":TIPO_USO",uso);
      dbOpSQLMaster.setParameter(":AREA",area);
      dbOpSQLMaster.setParameter(":CODICE_MODELLO",codice);
      dbOpSQLMaster.setParameter(":MODELLO_SUCCESSIVO",modello_successivo);
      dbOpSQLMaster.setParameter(":STILE",stile);      
            
      if (nuovo) {
        dbOpSQLMaster.setParameter(":DATA_INSERIMENTO",dataDiSistema);
        dbOpSQLMaster.setParameter(":CODICE_MODELLO_PADRE",codicePadre);
      
        if (!prov.equalsIgnoreCase("C")){
          String bloccoJdms = sostituisci(area,".","")+"_"+codice;
          dbOpSQLMaster.setParameter(":BLOCCO_JDMS",bloccoJdms);
        }  
      }    
      
      dbOpSQLMaster.execute();

    } catch (Exception e) {
    	// fa rollback, chiude e esce
    	  System.out.println("DeployerModel::pubblica(): Errore nell'inserimento/aggiornamento del Modello: "+dbOpSQLMaster.getStatementString());
    	  System.out.println(e.toString());
    	  e.printStackTrace();
    	  rollbackDeployer();
        free(dbOpSQLMaster);
        throw new Exception("DeployerModel::pubblica(): Errore nell'inserimento/aggiornamento del Modello: "+e.getMessage());
    }
      
   
    //eliminazione vecchi campi e blocchi
    try {
      fuoriUsoCampiModello(area, codice);
    } catch (Exception ec) {
    	// Rollback e chiusura sono nella chiamata. Esce
      System.out.println("DeployerModel::pubblica() - Errore in fase di eliminazione (mettere fuori uso) dei vecchi campi: "+ec.toString());
      throw new Exception("pubblica(): Errore in fase di eliminazione (mettere fuori uso) dei vecchi campi: "+ec.getMessage());
    } 
    
    try {
      eliminaBlocchiModello(area, codice);
    } catch (Exception eb) {
//    Rollback e chiusura sono nella chiamata. Esce
      System.out.println("DeployerModel::pubblica() - Errore in fase di eliminazione dei vecchi blocchi: "+eb.toString());
      throw new Exception("pubblica(): Errore in fase di eliminazione dei vecchi blocchi: "+eb.getMessage());
    } 
   
    if (!figlio){
      
      if(acronimo_modello == null){acronimo_modello = "";}
      if(alias_modello == null){alias_modello = "";}
      
      if (nuovo){
        try {
          inserisciTipoDocumento(area, codice, vista, uso, mComp, gComp, gCrea, prov, acronimo_modello, alias_modello);
        }
        catch (Exception e) {
//        Rollback e chiusura sono nella chiamata. Esce
          throw new Exception("pubblica():inserisciTipoDocumento1("+area+","+codice+")\n"+e.getMessage()); 
        }
      }else{
         if (esisteTipoDoc(codice) == false) {
           try {
             inserisciTipoDocumento(area, codice, vista, uso, mComp, gComp, gCrea, prov, acronimo_modello, alias_modello);
           }
           catch (Exception e) {
//           Rollback e chiusura sono nella chiamata. Esce
             throw new Exception("pubblica():inserisciTipoDocumento2()\n"+e.getMessage()); 
           }
         }else{   
           try {
//           Rollback e chiusura sono nella chiamata. Esce
             aggiornaTipoDocumento(area, codice, vista, uso, mComp, gComp, gCrea, prov, acronimo_modello, alias_modello);
            }
            catch (Exception e)
            {
              throw new Exception("pubblica():aggiornaTipoDocumento()\n"+e.getMessage()); 
            }
         }       
      }   
    }  
    
    // A questo punto il modello va interpretato e i suoi dati vanno memorizzati.
    // Vanno aggiunti al db i vari campi e le varie bitmap.
    try {
      interpretaModello(area, codice, tipo, newfileModello, prov);
      interpretaBitmap(area, codice, tipo, newfileModello);
    } catch (Exception e) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      System.out.println("DeployerModel::pubblica() - Errore in fase di interpretazione del modello:"+e.toString());
      throw new Exception("pubblica(): Errore in fase di interpretazione del modello: "+e.getMessage());
    } 
    
    if (!nuovo){
      try {
        allineaIDCampo(area, codice);       
      } catch (Exception e) {
      	rollbackDeployer();
        free(dbOpSQLMaster);
        System.out.println("DeployerModel::pubblica() - Errore in fase di allineamento degli ID_CAMPO dei vecchi campi del modello:"+e.toString());
        throw new Exception("pubblica(): Errore in fase di allineamento degli ID_CAMPO dei vecchi campi del modello: "+e.getMessage());
      } 
    }
    
    if (!figlio){
      if ((acronimo_modello.compareTo("") != 0) && (alias_modello.compareTo("") != 0)){
        try {
          creaTabella(dbOpSQLMaster.getConn(), area, codice);
        } catch (Exception ect) {
        	rollbackDeployer();
          free(dbOpSQLMaster);
          System.out.println("DeployerModel::pubblica() - Errore in fase di creazione della tabella per il Tipo Documento ["+alias_modello+"]:"+ect.getMessage());
          throw new Exception("pubblica(): Errore in fase di creazione della tabella per il Tipo Documento ["+alias_modello+"]: "+ect.getMessage());
        }
      }

      if (vista.compareTo("") != 0){
  
        viewStm = "F_CREA_VISTA_TIPODOC('"+area+"', '"+codice+"', '"+vista+"')";
       
        try {
          dbOpSQLMaster.setCallFunc(viewStm);
          dbOpSQLMaster.execute();
          
        }catch (Exception eeev) {
        	rollbackDeployer();
          free(dbOpSQLMaster);
          
          System.out.println("DeployerModel::pubblica() - Errore in fase di creazione della vista per il Tipo Documento ["+viewStm+"]:"+eeev.getMessage());
          throw new Exception("pubblica(): Errore in fase di creazione della vista per il Tipo Documento ["+viewStm+"]: "+eeev.getMessage());
        }
      }
     
    }
   
    try {
      scriviLog_GDM(area, codice, codicePadre, revisione, autore, tipo, mComp, gComp, gCrea, modello_successivo, utente);
    } catch (Exception elog) {
    	// Rollback e chiusura sono nella chiamata
      System.out.println("DeployerModel::pubblica() - Errore in fase di insert in LOG_GDM:"+elog.toString());
      throw new Exception("pubblica(): Errore in fase di di insert in LOG_GDM: "+elog.getMessage());
    }
   
    if (!figlio){
      if (gCrea.equalsIgnoreCase("S")){
        try{ 
          assegnaCreaGuest(area,codice);
        }catch(Exception eg){
        	rollbackDeployer();
          free(dbOpSQLMaster);
          System.out.println("DeployerModel::pubblica() - Errore in fase di assegnazione competenze:"+eg.toString());
          throw new Exception("pubblica(): Errore in fase di assegnazione competenze: "+eg.getMessage());
        }
      }
    }
   
    try{
      dbOpSQLMaster.commit();      
    } catch(Exception e){  
    	rollbackDeployer();   
       throw new Exception("pubblica(): Errore in fase di commit delle modifiche\n"+e.getMessage());
    } finally {
       free(dbOpSQLMaster);
    }
    return true;                             
  }
  
  
  /**metodo replaceAll**/
  private String sostituisci(String testo, String s1, String s2){
    int i = 0;
    int ind = testo.indexOf(s1);
    String testoNew="";
    do {
      if (ind >= 0){
        testoNew = testoNew + testo.substring(i, ind) + s2;
	      i = ind + s1.length();
	      ind = testo.indexOf(s1, i);    
      }
    } while (ind >= 0);
    testoNew = testoNew + testo.substring(i);
    testo = testoNew;

    return(testo);
  }

  /**
   Inserisce una riga di LOG nella tabella LOG_GDM
   */
  public boolean scriviLog_GDM(String area, 
                                String codice, 
                                String codicePadre,
                                String revisione, 
                                String autore,
                                String tipo, 
                                String mComp,
                                String gComp,
                                String gCrea,
                                String modello_successivo,
                                String utente)throws Exception {
//System.out.println("scriviLog_GDM"); 
    String         insStm, revStm, creStm;
    Timestamp      dataDiSistema = dbOpSQLMaster.getSysdate();

    if (revisione!= "0"){
      revStm = "Archiviata revisione precedente. ";
    }else{
      revStm = "";
    }  
    if (gCrea != "N"){
      creStm = "Assegnata Competenza di Creazione a utente GUEST. ";
    }else{
      creStm = "";
    }  
    insStm = "INSERT INTO LOG_GDM "+
             "(AZIONE, DATA, UTENTE, DESCRIZIONE) "+
             "VALUES('Pubblicazione Modello', :DATA_LOG, '"+utente+"', "+ 
             "'Pubblicato il Modello "+codice+", Area "+area+". "+
             "Autore = "+autore+", Modello Padre = "+codicePadre+
             ", Tipo = "+tipo+", Modello Successivo = "+modello_successivo+
             ", Manage Competenze = "+mComp+", Gestione Competenze = "+gComp+". "+revStm+creStm+"')";
    try {
      dbOpSQLMaster.setStatement(insStm);
      dbOpSQLMaster.setParameter(":DATA_LOG",dataDiSistema);        
      dbOpSQLMaster.execute();
      
    } catch (Exception eee) {
      try{
      	rollbackDeployer();
       free(dbOpSQLMaster);
      }catch(Exception eeer) {}
      System.out.println("DeployerModel::scriviLog_GDM() - Errore in fase di inserimento: "+eee.toString());
      throw new Exception("scriviLog_GDM(): Errore in fase di INSERT in LOG_GDM ["+insStm+"]: "+eee.toString());
    } 
    
    return true;
  }  

  
  /*
   * Crea il TipoDocumento e gli oggetti ad esso collegati(
   * blocco principale e campo $ACTIONKEY per cartelle e query),
   * in caso di un modello nuovo.
   */
  public boolean inserisciTipoDocumento(String area, String codice, 
                                        String vista, String uso, 
                                        String mComp, String gComp, 
                                        String gCrea, String prov, 
                                        String acronimo_modello, 
                                        String alias_modello) throws Exception {    
    String         insStm, updStm,bloccoStm;
    Timestamp      dataDiSistema = dbOpSQLMaster.getSysdate();
    int            id_tipo;
//System.out.println("inserisciTipoDocumento");     
    try {
      id_tipo = dbOpSQLMaster.getNextKeyFromSequence("TIDO_SQ");
        
      if (!prov.equalsIgnoreCase("C") && !prov.equalsIgnoreCase("D")){
        if (!acronimo_modello.equals("")) {          
          if (Parametri.SVILUPPO_FINMATICA.equals("S")){      
            acronimo_modello="$"+acronimo_modello;
          }else{
            acronimo_modello="_"+acronimo_modello;
          }
        }
      }

      String isH_M="";
      if ((acronimo_modello.compareTo("") != 0) && (alias_modello.compareTo("") != 0)){
      	isH_M = "1";
      }

      insStm = "INSERT INTO TIPI_DOCUMENTO "+
               "(ID_TIPODOC, AREA_MODELLO, ID_LIBRERIA, NOME, VERSIONABILE, TIPO_LOG, ALIAS_VIEW, "+
               "MANAGE_COMPETENZE, GESTIONE_COMPETENZE, ATTIVAPRATICA, DATA_AGGIORNAMENTO, "+
               "UTENTE_AGGIORNAMENTO, ACRONIMO_MODELLO, ALIAS_MODELLO, ISHORIZONTAL_MODEL) "+
               "VALUES ("+ id_tipo+ ", '"+area+"', 1, '"+codice+"', 'S', 'N', '"+vista+"', "+
               "'"+mComp+"', '"+gComp+"', 'S', :DATA_AGGIORNAMENTO, 'GDM', "+
               "'"+acronimo_modello.toUpperCase()+"', '"+alias_modello.toUpperCase()+"', "+
               ":ISHORIZONTAL_MODEL )";
      dbOpSQLMaster.setStatement(insStm);
      dbOpSQLMaster.setParameter(":DATA_AGGIORNAMENTO",dataDiSistema);
      dbOpSQLMaster.setParameter(":ISHORIZONTAL_MODEL",isH_M);
      dbOpSQLMaster.execute();
        
    } catch (Exception eee) {
      try{
      	rollbackDeployer(); 
        free(dbOpSQLMaster);
      }catch(Exception eeer) {}
        System.out.println("DeployerModel::inserisciTipoDocumento() - Errore in fase di inserimento: "+eee.toString());
        throw new Exception("inserisciTipoDocumento(): Errore in fase di inserimento: "+eee.toString());
    }
    
    updStm = "UPDATE MODELLI SET "+
             "ID_TIPODOC = " +id_tipo+ " "+
             "WHERE AREA = '" +area+ "' AND CODICE_MODELLO= '" +codice+ "' ";

    try {
      dbOpSQLMaster.setStatement(updStm);
      dbOpSQLMaster.execute();
    } catch (Exception eeeu) {
      try{
      	rollbackDeployer();  
        free(dbOpSQLMaster);
      }catch(Exception eeer) {}
        System.out.println("DeployerModel::inserisciTipoDocumentoU() - Errore in fase di inserimentodell'ID_TIPODOC nel Modello ["+updStm+"]: "+eeeu.toString());
        throw new Exception("inserisciTipoDocumento(): Errore in fase di inserimento dell'ID_TIPODOC nel Modello ["+updStm+"]: "+eeeu.toString());
    }
    
    //1. Inseriamo anche il blocco principale per la visualizzazione dei documenti nel GDClient
    if (!prov.equalsIgnoreCase("C")){
      bloccoStm = "F_ALLINEA_BLOCCHI('"+id_tipo+"')";
     
      try {
        dbOpSQLMaster.setCallFunc(bloccoStm);
        dbOpSQLMaster.execute();
      }catch (Exception eeeb) {
        try{
        	rollbackDeployer();    
           free(dbOpSQLMaster);
        }catch(Exception eeer) {}
        System.out.println("DeployerModel::inserisciTipoDocumento() - Errore in fase di inserimento del blocco principale ["+bloccoStm+"]:"+eeeb.getMessage());
        throw new Exception("inserisciTipoDocumento(): Errore in fase di inserimento del blocco principale per il Tipo Documento ["+bloccoStm+"]: "+eeeb.getMessage());
      }
    }
    // 2. Per le cartelle inserisco il campo fisso $ACTIONKEY

    if ((uso.equalsIgnoreCase("C") || uso.equalsIgnoreCase("Q"))){
      try{
        scriviCampoACTIONKEY(codice, area, id_tipo);
      }catch(Exception ek){
//      Rollback e chiusura sono nella chiamata. Esce
      	 System.out.println("inserisciTipoDocumento(): Errore in fase di inserimento del campo $ACTIONKEY: "+ek.getMessage());
        throw new Exception("inserisciTipoDocumento(): Errore in fase di inserimento del campo $ACTIONKEY: "+ek.getMessage());
      }
    }      
         
    return true;
  }

  
  /*
   * Aggiorna il TipoDocumento e gli oggetti ad esso collegati(
   * blocco principale e campo $ACTIONKEY per cartelle e query),
   * in caso di un modello già presente.
   */
  public boolean aggiornaTipoDocumento(String area, String codice, 
                                       String vista, String uso, 
                                       String mComp, String gComp, 
                                       String gCrea, String prov, 
                                       String acronimo_modello, String alias_modello ) throws Exception {
    String         idStm, updStm, /*funcStm, */ bloccoStm;
    ResultSet       rst;
    Timestamp      dataDiSistema = dbOpSQLMaster.getSysdate();
    int            id_tipo=0;
//System.out.println("aggiornaTipoDocumento");    
    try {
      idStm = "SELECT ID_TIPODOC FROM MODELLI "+
              "WHERE AREA = '"+area+"' AND "+
                    "CODICE_MODELLO = '"+codice+"'";
      dbOpSQLMaster.setStatement(idStm);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
      if (rst.next()){
        id_tipo = rst.getInt("ID_TIPODOC");
        
        String sAcronimo_modello = "";
        String sAlias_modello = "";
        String sVista = "";
        
        if ((!acronimo_modello.equals("")) && (!alias_modello.equals(""))){
          String sWhere ="ID_TIPODOC="+id_tipo;
          if (!giaSettato("ACRONIMO_MODELLO","TIPI_DOCUMENTO",sWhere)){
            if (!prov.equalsIgnoreCase("C") && !prov.equalsIgnoreCase("D")){
              if (Parametri.SVILUPPO_FINMATICA.equals("S")){
                acronimo_modello = "$"+acronimo_modello;
              }else{
                acronimo_modello = "_"+acronimo_modello;
              }            
            }
            sAcronimo_modello = "ACRONIMO_MODELLO = '"+acronimo_modello.toUpperCase()+"', ";                       
            
          }       
              
          if (!giaSettato("ALIAS_MODELLO","TIPI_DOCUMENTO",sWhere)){
            
            sAlias_modello = "ALIAS_MODELLO = '"+alias_modello.toUpperCase()+"', ";
          }
        }
                
        
        if (vista.compareTo("") != 0){
          sVista="ALIAS_VIEW = '"+vista+"', ";
        }
        
        String sIsHorizontal_model="";
        if (((acronimo_modello.compareTo("") != 0) && (alias_modello.compareTo("") != 0))
        		&& (!esisteUnDocumento(id_tipo))){        	
        	sIsHorizontal_model = "ISHORIZONTAL_MODEL = 1, ";
        }

        
        updStm = "UPDATE TIPI_DOCUMENTO SET "+
                 "AREA_MODELLO = '"+area+"', "+
                 sVista+
                 sAcronimo_modello+
                 sAlias_modello+
                 sIsHorizontal_model+
                 "MANAGE_COMPETENZE = '"+mComp+"', "+
                 "GESTIONE_COMPETENZE = '"+gComp+"', "+
                 "DATA_AGGIORNAMENTO = :DATA_AGGIORNAMENTO "+
                 "WHERE ID_TIPODOC = "+id_tipo;
        
        dbOpSQLMaster.setStatement(updStm);
     //   System.out.println(updStm);
        dbOpSQLMaster.setParameter(":DATA_AGGIORNAMENTO",dataDiSistema);
        dbOpSQLMaster.execute();
      
      }  
    } catch (Exception eee) {
      try{
      	rollbackDeployer();
        free(dbOpSQLMaster);
      }catch (Exception eeer){}
      System.out.println("DeployerModel::aggiornaTipoDocumento() - Errore nella prima fase dell'aggiornamento: "+eee.toString());
      throw new Exception("aggiornaTipoDocumento(): Errore nella prima fase dell'aggiornamento: "+eee.toString());
    } 
    
    updStm = "UPDATE MODELLI SET "+
             "ID_TIPODOC = " +id_tipo+ " "+
             "WHERE AREA = '" +area+ "' AND CODICE_MODELLO= '" +codice+ "' ";

    try {
      dbOpSQLMaster.setStatement(updStm);
      dbOpSQLMaster.execute();
    
    } catch (Exception eeeu) {
     
    	rollbackDeployer();   
      free(dbOpSQLMaster);
     
      System.out.println("DeployerModel::inserisciTipoDocumento() - Errore in fase di UPDATE dell'ID_TIPODOC sul Modello ["+updStm+"]:"+eeeu.toString());
      throw new Exception("inserisciTipoDocumento(): Errore in fase di UPDATE dell'ID_TIPODOC sul Modello ["+updStm+"]: "+eeeu.toString());
    }   

    //1. Inseriamo anche (se non c'è già) il blocco principale per la visualizzazione dei documenti nel GDClient
    if (!prov.equalsIgnoreCase("C")){
      bloccoStm = "F_ALLINEA_BLOCCHI('"+id_tipo+"')";
     
      try {
        dbOpSQLMaster.setCallFunc(bloccoStm);
        dbOpSQLMaster.execute();
      }catch (Exception eeeb) {
        
      	rollbackDeployer();    
        free(dbOpSQLMaster);
        System.out.println("DeployerModel::inserisciTipoDocumento() - Errore in fase di inserimento del blocco principale ["+bloccoStm+"]:"+eeeb.getMessage());
        throw new Exception("inserisciTipoDocumento(): Errore in fase di inserimento del blocco principale per il Tipo Documento ["+bloccoStm+"]: "+eeeb.getMessage());
      }
    }
    
    // 2. Per le cartelle inserisco il campo fisso $ACTIONKEY

    if ((uso.equalsIgnoreCase("C") || uso.equalsIgnoreCase("Q"))){
      try{
//      Rollback e chiusura sono nella chiamata. Esce
        scriviCampoACTIONKEY(codice, area, id_tipo);
      }catch(Exception ek){
        throw new Exception("inserisciTipoDocumento(): "+ek.getMessage());
      }
    }     
         
    return true;
  }
  
  
  /*
   * Assegna la competenza di creazione all'utente GUEST.
   * E' l'opzione di default
   */
  public boolean assegnaCreaGuest(String area, 
                                  String codice)throws Exception {
//System.out.println("assegnaCreaGuest"); 
    String         selStm, funcStm="", funcVerStm="";
    int            id_tipo=0, abil=0 ;
    ResultSet      rst,rstVer;

    
    selStm = "SELECT ID_TIPODOC "+
               "FROM MODELLI "+
               "WHERE AREA = '"+area+"' "+
               "AND CODICE_MODELLO = '"+codice+"'";

    try{           

      dbOpSQLMaster.setStatement(selStm);
      dbOpSQLMaster.execute();

      rst = dbOpSQLMaster.getRstSet();
      if (rst.next()){
        id_tipo = rst.getInt("ID_TIPODOC");

        funcVerStm = "SELECT SI4_COMPETENZA.VERIFICA('TIPI_DOCUMENTO','" +id_tipo+ 
                     "',  'C', 'GUEST', 'GDM', to_char(SYSDATE,'dd/mm/yyyy')) ABIL "+
                     "FROM DUAL";
        dbOpSQLMaster.setStatement(funcVerStm);
//System.out.println("funcVerStm = "+funcVerStm);
        dbOpSQLMaster.execute();     
//System.out.println("funcVerStm eseguita"); 
        rstVer = dbOpSQLMaster.getRstSet();
        if (rstVer.next()){
          abil = rstVer.getInt("ABIL");
          if (abil==0){       
            funcStm = "F_ASSEGNA('TIPI_DOCUMENTO', '" +id_tipo+ 
                      "', 'C', 'GUEST', 'GDM', 'GDM', 'S', to_char(SYSDATE,'dd/mm/yyyy'), '')";
        
            dbOpSQLMaster.setCallFunc(funcStm);
//System.out.println("funcStm = "+funcStm);
            dbOpSQLMaster.execute();     
//System.out.println("funcStm eseguita");            
          }
        }  
      }  
      
    }catch (Exception eeef) {
     
    	rollbackDeployer(); 
      free(dbOpSQLMaster);
      
      System.out.println("assegnaCreaGuest(): Errore in fase di inserimento della competenza di creazione all'utente GUEST ["+funcStm+"]: "+eeef.toString());
      throw new Exception("assegnaCreaGuest(): Errore in fase di inserimento della competenza di creazione all'utente GUEST ["+funcStm+"]: "+eeef.toString());
    }  
             
    return true;
  }  

 
  /*
   * Se on esiste il campo_documento, lo inserisce
   * e aggiorna il dato_modello con l'id_campo
   */
  public boolean inserisciCampoDocumento(String area, String codice, String nome, int id_tipo ) throws Exception {
    String         query, insStm, updStm;
    Timestamp      dataDiSistema = dbOpSQLMaster.getSysdate();
    ResultSet      rst;
    boolean        esiste;
//System.out.println("inserisciCampoDocumento");  
    query = "SELECT * "+
              "FROM CAMPI_DOCUMENTO "+
             "WHERE ID_TIPODOC = '"+ id_tipo +"' AND "+
                   "NOME = '"+ nome +"'";
    try {
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
      esiste = rst.next();
      // Commit alla fine del metodo (se tutto ok)
    } catch (Exception e1) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      e1.printStackTrace();
      System.out.println("inserisciCampoDocumento(): Attenzione! Errore in fase di SELECT ["+query+"]: "+e1.toString());
      throw new Exception("inserisciCampoDocumento(): Errore in fase di SELECT ["+query+"]: "+e1.toString());
    }
            
    if (!esiste){
      int id_campo;

      insStm = "INSERT INTO CAMPI_DOCUMENTO "+
                 "(ID_CAMPO, ID_TIPODOC, NOME, OBBLIGATORIO, "+
                 "DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO) "+
                 "VALUES (:ID_CAMPO, " +id_tipo+ " ,'"+nome+"', 'N', "+
                 ":DATA_AGGIORNAMENTO, 'GDM')";

      try {
        id_campo = dbOpSQLMaster.getNextKeyFromSequence("CADO_SQ");
        dbOpSQLMaster.setStatement(insStm);
        dbOpSQLMaster.setParameter(":ID_CAMPO",id_campo);
        dbOpSQLMaster.setParameter(":DATA_AGGIORNAMENTO",dataDiSistema);
        dbOpSQLMaster.execute();
        
      } catch (Exception eee) {
       
      	rollbackDeployer();
        free(dbOpSQLMaster);
       
        System.out.println("DeployerModel::inserisciCampoDocumento() - Errore in fase di inserimento ["+insStm+"]: "+eee.toString());
        throw new Exception("inserisciCampoDocumento(): Errore in fase di inserimento ["+insStm+"]: "+eee.toString());
      } 
      
      updStm = "UPDATE DATI_MODELLO SET "+
                 "ID_CAMPO = " +id_campo+ " "+
                 "WHERE AREA = '" +area+ "' AND CODICE_MODELLO= '" +codice+ "' AND "+
                 "DATO= '" +nome+ "' ";
      try {
        dbOpSQLMaster.setStatement(updStm);
        dbOpSQLMaster.execute();
      } catch (Exception eeeu) {
        
      	rollbackDeployer(); 
        free(dbOpSQLMaster);
        
        System.out.println("DeployerModel::inserisciCampoDocumentoU() - Errore in fase di inserimento dell'ID_CAMPO ["+updStm+"]: "+eeeu.toString());
        throw new Exception("inserisciCampoDocumento(): Errore in fase di inserimento dell'ID_CAMPO ["+updStm+"]: "+eeeu.toString());
      } 
    }    
    return true;
  }
  
  public void allineaIDCampo(String area, String codice) throws Exception{
//  System.out.println("allineaIDCampo");   	
  	String AllStm = "F_ALLINEA_IDCAMPO('"+area+"', '"+codice+"')";
     
     try {
       dbOpSQLMaster.setCallFunc(AllStm);
       dbOpSQLMaster.execute();
     }catch (Exception eeeb) {
      
    	 rollbackDeployer(); 
       free(dbOpSQLMaster);
      
       System.out.println("DeployerModel::allineaIDCampo() - Errore in fase di allineamento ID_CAMPO ["+AllStm+"]:"+eeeb.getMessage());
       throw new Exception("allineaIDCampo(): Errore in fase di allineamento ID_CAMPO ["+AllStm+"]: "+eeeb.getMessage());
     } 	
  }
  
  /**
   * Se si è scelto di storicizzare la vecchia versione del modello, si inserisce nella tabella
   * REVISIONI_MODELLO
   **/   
  public boolean storicizza(String area, String codice, String revisione ) throws Exception {
//System.out.println("storicizza"); 
    
    String            insStm;
    Timestamp         dataDiSistema = dbOpSQLMaster.getSysdate();
  
    // ----------------------------------------------------------------------------------------
    // Verifica se inserire o aggiornare il modello. L'inserimento/aggiornamento vero e proprio
    // avverrà solo attraverso un update finale sul campo CLOB
    // ----------------------------------------------------------------------------------------
 
    insStm = "INSERT INTO REVISIONI_MODELLO "+
                "(AREA,  CODICE_MODELLO, REVISIONE, DATA_REVISIONE,  VALIDO, "+
                " CODICE_MODELLO_PADRE, AUTORE, DATA_VARIAZIONE,   DATA_INSERIMENTO,  DATA_PUBBLICAZIONE,  TIPO, "+
                " MODELLO_SUCCESSIVO) "+
                "SELECT '"+ area +"', '"+ codice +"', :REVISIONE, :DATA_REVISIONE, :VALIDO, "+
                "CODICE_MODELLO_PADRE, AUTORE, DATA_VARIAZIONE, DATA_INSERIMENTO, DATA_PUBBLICAZIONE, TIPO, "+
                "MODELLO_SUCCESSIVO FROM MODELLI "+
                "WHERE AREA = :AREA AND CODICE_MODELLO = :CODICE_MODELLO";

    dbOpSQLMaster.setStatement(insStm);
        
    try {
      dbOpSQLMaster.setParameter(":DATA_REVISIONE",dataDiSistema);
      dbOpSQLMaster.setParameter(":AREA",area);
      dbOpSQLMaster.setParameter(":CODICE_MODELLO",codice);
      dbOpSQLMaster.setParameter(":REVISIONE",revisione);
      dbOpSQLMaster.setParameter(":VALIDO","S");
      dbOpSQLMaster.execute();
      
    } catch (Exception eee) {
     
    	rollbackDeployer();  
      free(dbOpSQLMaster);
      
      System.out.println("DeployerModel::storicizza()1 - Errore in fase di INSERT ["+insStm+"]: "+eee.toString());
      throw new Exception("storicizza(): Errore in INSERT ["+insStm+"]: "+eee.toString());
    } 
       
   // FileInputStream fInput = new FileInputStream(fileModello);
      
    String queryUpdateModello =
      "UPDATE REVISIONI_MODELLO "+
         "SET MODELLO = (SELECT MODELLO FROM MODELLI  "+
            "WHERE  AREA = :AREA AND "+
                   "CODICE_MODELLO = :CODICE_MODELLO)"+
         "WHERE  AREA = '"+ area +"' AND "+
                "CODICE_MODELLO = '" + codice + "' AND "+
                "REVISIONE = :REVISIONE";
                      
    dbOpSQLMaster.setStatement(queryUpdateModello);

   try {
      dbOpSQLMaster.setParameter(":AREA",area);
      dbOpSQLMaster.setParameter(":CODICE_MODELLO",codice);
      dbOpSQLMaster.setParameter(":REVISIONE",revisione);
      dbOpSQLMaster.execute();
    } catch (Exception eee1) { 
     
    	rollbackDeployer();  
      free(dbOpSQLMaster);
      
      System.out.println("DeployerModel::storicizza()1 - sql= "+dbOpSQLMaster.getStatementString());
      System.out.println("DeployerModel::storicizza()1 - Errore in fase di scrittura del campo MODELLO: "+eee1.toString());
      throw new Exception("storicizza(): Errore in fase di scrittura del campo MODELLO ["+dbOpSQLMaster.getStatementString()+"]: "+eee1.toString());
    } 
   
    // Update campo MODELLO
                       
    dbOpSQLMaster.setStatement(queryUpdateModello);
   
    queryUpdateModello =
      "UPDATE REVISIONI_MODELLO "+
         "SET FILE_ORIGINALE = (SELECT FILE_ORIGINALE FROM MODELLI  "+
            "WHERE  AREA = :AREA AND "+
                   "CODICE_MODELLO = :CODICE_MODELLO)"+
         "WHERE  AREA = '"+ area +"' AND "+
                "CODICE_MODELLO = '" + codice + "' AND "+
                "REVISIONE = :REVISIONE";
                   
    dbOpSQLMaster.setStatement(queryUpdateModello);

    try {
      dbOpSQLMaster.setParameter(":AREA",area);
      dbOpSQLMaster.setParameter(":CODICE_MODELLO",codice);
      dbOpSQLMaster.setParameter(":REVISIONE",revisione);
      dbOpSQLMaster.execute();
    } catch (Exception eee2) { 
     
    	rollbackDeployer();   
      free(dbOpSQLMaster);
     
      System.out.println("DeployerModel::storicizza()2 - sql= "+dbOpSQLMaster.getStatementString());
      System.out.println("DeployerModel::storicizza()2 - Errore in fase di scrittura del FILE_ORIGINALE:"+eee2.toString());
      throw new Exception("Errore in fase di UPDATE del campo FILE_ORIGINALE ["+queryUpdateModello+"]: "+eee2.toString());
    } 
   
    return true;      
  
  }
  

  /**
   * Cancella la directory che aveva creato all'inizio.
   **/   
  public void pulisciTutto(String absPathToFolder) {
//System.out.println("pulisciTutto");    
    File fDir = new File(absPathToFolder);
    if (fDir.exists() && fDir.isFile()) {
      fDir.delete();
      
      String absPathToFolderText = absPathToFolder.replaceAll(".html",".txt");
      File gDir = new File(absPathToFolderText);
      if (gDir.exists() && gDir.isFile()) {
        gDir.delete();
      }
    }
    else{
      String[] list = fDir.list();
      int nList = list.length;
      for  (int i=0; i < nList; i++){
        pulisciTutto(absPathToFolder+File.separator+list[i]);
      }
      fDir.delete();
    }
  }
 
  
  /** 
   * Metodi per estrarre le informazioni dal modello passato.
   **/  
  public String getCodiceModello(){
//System.out.println("getCodiceModello");
    String extFileName = "";
    String sCodice = "";
  
    int i = zipFileName.lastIndexOf(".zip");
    if (i > 0){
      extFileName = zipFileName.substring(0,i);
    }else{
      extFileName = zipFileName;
    }
    int j = extFileName.lastIndexOf(".");
    if (j > 0){
      sCodice = extFileName.substring(0,j);
    }else{
      sCodice = extFileName;
    }
    return sCodice;
  }
  
  
  /*
   * Cerca l'informazione AREA all'interno del file HTML
   */
  public String getArea(){

//System.out.println("getArea");   
      int i = contenutoFileModello.lastIndexOf("<o:_AREA");
      int j = contenutoFileModello.lastIndexOf("</o:_AREA>");
      String sArea = "";
      
      if ((i != -1) && (j != -1)) {
        sArea = contenutoFileModello.substring(i+24, j);
      }
      return sArea;
   }

  
  /*
   * Cerca l'informazione LastAuthor all'interno del file HTML
   */
  public String getAutore() {
   
//System.out.println("getAutore");                  
    int i = contenutoFileModello.lastIndexOf("<o:LastAuthor");
    int j = contenutoFileModello.lastIndexOf("</o:LastAuthor>");
    String sAutore = "";
      
    if ((i != -1) && (j != -1)) {
      sAutore = contenutoFileModello.substring(i+14, j);
    }
    return sAutore;
  }

  
  /*
   * Cerca l'informazione modPadre all'interno del file HTML
   */
  public String getCMPadre(){
    
//System.out.println("getCMPadre");   
      int i = contenutoFileModello.lastIndexOf("<o:modPadre>");
      int j = contenutoFileModello.lastIndexOf("</o:modPadre>");
      String sCMP = "";
      
      if ((i != -1) && (j != -1)) {
        sCMP = contenutoFileModello.substring(i+12, j);
      }
      return sCMP;
  }
  
  
  /*
   * Cerca l'informazione Stile all'interno del file HTML
   */
  public String getStile() {
   
//System.out.println("getStile");                  
    int i = contenutoFileModello.lastIndexOf("<o:Stile");
    int j = contenutoFileModello.lastIndexOf("</o:Stile>");
    String sStile = "";
      
    if ((i != -1) && (j != -1)) {
      sStile = contenutoFileModello.substring(i+8, j);
    }
    return sStile;
  }

  
  /*
   * return "0";
   */
  public String getRevisione(String area, String codice_modello) {
//System.out.println("getRevisione");    
    return "0";
  }

  
  /*
   * Setta il contenutoFileModello ripristinando i tag ADS
   */
  public void setContenutoFileModello(String contenutoFile){
    ModificaModello mm = new ModificaModello();
    contenutoFileModello=mm.ripristinaTagAds(contenutoFile);
    } 
  
  
  /*
   * Restituisce il contenutoFileModello 
   */
  public String getContenutoFileModello(){
//System.out.println("getContenutoFileModello");  
    return contenutoFileModello;
  }
  

  /**
   * Calcola il nuovo numero di revisione
   */
  public String calcolaNuovaRevisione(String pArea, String pCodiceModello) throws Exception {
//System.out.println("calcolaNuovaRevisione");                                
    ResultSet      rst = null;
    String         query;
    String         retval = null;
        
    query = "SELECT REVISIONE FROM REVISIONI_MODELLO "+
            "WHERE AREA = :AREA AND CODICE_MODELLO = :CODICE_MODELLO ";

    try {
    
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.setParameter(":AREA",pArea);
      dbOpSQLMaster.setParameter(":CODICE_MODELLO",pCodiceModello);   
      
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();

    } catch (Exception e) {
      free(dbOpSQLMaster);
      throw new Exception("calcolaNuovaRevisione(): Errore durante la ricerca mirata del numero di revisione ["+query+"]: "+e.toString());
    }
    
    try {
      if (rst.next() == true) {
        // Esiste già il modello per cui diventa una nuova revisione: cerco la massima
        int maxRev = 0;
        do {
          if (rst.getInt("REVISIONE") > maxRev){
            maxRev = rst.getInt("REVISIONE");
          }
        } while (rst.next()==true);
        retval = Integer.toString(maxRev + 1);
      } else {
        // Non esiste ancora il modello per cui assume revisione = 1
        retval = "1";
      }
    } catch (SQLException sqle) {
      free(dbOpSQLMaster);
      sqle.printStackTrace();
      throw new Exception("calcolaNuovaRevisione(): Errore nel ciclo di ricerca della revisione: "+sqle.toString());
    }
            
    return retval;
  }


  /**
   * Legge il file interpreta il modello scrivendo i vari campi (dati_modello)
   * e blocchi (blocchi_modello) sul db.
   **/
  void interpretaModello(String area, 
                         String codice, 
                         String tipo, 
                         File   fileModello,
                         String prov) throws Exception {
//System.out.println("interpretaModello");  
    
    ResultSet            rst;
    String               querySelect,
                         modello;
    int                  startChar, j, j1, k, i, i1, h, id_tipo;
    String               subModello, campoHtml, bloccoHtml;
    Clob                 clob;

    // Imposta i parametri per l'interpretazione del modello
  
    Parametri.settaParametriModello(tipo);
    
    // Rileggo il modello dal deploy sul DB
    querySelect = "SELECT ID_TIPODOC, FILE_ORIGINALE FROM MODELLI WHERE "+
                    "AREA = '"+area+"' and "+
                    "CODICE_MODELLO = '"+codice+"'";

    try {
      dbOpSQLMaster.setStatement(querySelect);
      dbOpSQLMaster.execute();
     
      rst = dbOpSQLMaster.getRstSet();
    } catch (Exception e) {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaModello(): Errore in fase di SELECT ["+querySelect+"]: "+e.toString());
    }
     
    if (!rst.next()) {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaModello(): Errore nella ricerca del modello: il modello non è presente sul DB.");
    }
    
    // Ricavo l'ID del tipo documento
    // Ricavo il modello (il campo è un CLOB).
    try {
      id_tipo = rst.getInt("ID_TIPODOC");
      clob = rst.getClob("FILE_ORIGINALE");
    } catch (Exception e) {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaModello(): Errore nell'estrazione dell'ID_TIPODOC e del FILE_ORIGINALE: "+e.toString());
    }
    
    long clobLen = clob.length();
    modello = "";
    if (clobLen < MAX_LENGTH_MODELLO) {
      int i_clobLen = (int)clobLen;
      modello = clob.getSubString(1, i_clobLen);
    } else {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaModello(): Il modello supera la dimensione massima consentita ("+MAX_LENGTH_MODELLO +" byte)");
    }
    // Prova di reinterpretazione e successivo aggiornamento sul db
   
    try{
      modello = preElabora(modello, tipo); 
    }catch (Exception em){
      modello = "";
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaModello(): "+em.getMessage());
    }
    
    try{   
      modello = preElaboraBlocchi(modello);
    }catch(Exception emb){
      modello = "";
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaModello(): "+emb.getMessage());
    }
   
    //CALCOLO IL NUMERO DI PAGINE DEL MODELLO
    int numeroPagine = 0;
    int posTagPag = modello.indexOf(Parametri.getTagTabBegin());
    while (posTagPag > -1) {
      numeroPagine++;
      posTagPag = modello.indexOf(Parametri.getTagPageBegin(),posTagPag+Parametri.getTagPageBegin().length());
    }
    if (numeroPagine == 0) {
      numeroPagine = 1;
    }
    String queryPagina = "UPDATE MODELLI SET PAGINE = :PAGINE " +
                          "WHERE AREA = '"+area+"' and "+
                          "CODICE_MODELLO = '"+codice+"'";
    
    try {
      dbOpSQLMaster.setStatement(queryPagina);
      dbOpSQLMaster.setParameter(":PAGINE", numeroPagine);
      dbOpSQLMaster.execute();  
    } catch (Exception ePag) {
//      dbOpSQLMaster.rollback();
//      free(dbOpSQLMaster);      
      System.out.println("interpretaModello(): Errore in fase di riscrittura sul DB ["+queryPagina+"]: "+ePag.toString());
      throw new Exception("interpretaModello(): Errore in fase di riscrittura del modello interpretato sul DB ["+queryPagina+"]: "+ePag.toString());      
    }
    // RISCRIVO IL MODELLO INTERPRETATO SU DB
    
    String query = "UPDATE MODELLI SET MODELLO = :MODELLO " +
                      "WHERE AREA = '"+area+"' and "+
                      "CODICE_MODELLO = '"+codice+"'";

    try {
      byte bModello[] = modello.getBytes();
      ByteArrayInputStream bais = new ByteArrayInputStream(bModello);
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.setAsciiStream(":MODELLO", bais, bais.available());
      dbOpSQLMaster.execute();  
      
    } catch (Exception e3) {
//      dbOpSQLMaster.rollback();
//      free(dbOpSQLMaster);      
      System.out.println("interpretaModello(): Errore in fase di riscrittura sul DB ["+query+"]: "+e3.toString());
      throw new Exception("interpretaModello(): Errore in fase di riscrittura del modello interpretato sul DB ["+query+"]: "+e3.toString());      
    }
    
    // Ciclo di lettura e interpretazione del testo del modello:
    //    -->estrapolazione e creazione di tutti i campi sul database
    startChar = 0;
    subModello = modello.substring(startChar);
    // Ciclo di esame ed estrazione dei campi
    j = 0;
    while ((j != -1) && (!subModello.equals(""))) {   
      j = subModello.indexOf(Parametri.getNameFieldBegin());
      if (j != -1) {      
      
        // Trovato un campo
        k = subModello.indexOf(Parametri.getNameFieldEnd())+ (Parametri.getNameFieldEnd()).length();
        j1 = subModello.indexOf(Parametri.getNameFieldBegin(),j+1);
       
        if (j1 == -1) {
          j1 = k + 1;
        }
       
        if (k != -1) {
          if (k < j1) {          
            // Da j a k+TAGFIELDEND.length() è un CAMPO          
            campoHtml = subModello.substring(j, k);  
           
            try {            	
            
              scriviCampo(area, codice, campoHtml, id_tipo);
           
            } catch (Exception ee) {     
            
//            	dbOpSQLMaster.rollback(); 
//              free(dbOpSQLMaster);
             throw new Exception("interpretaModello(): Errore in fase di scrittura campo: "+ee.toString());
            }
           
            // Mi resta da analizzare tutto quanto si trova dopo il TAGFIELDEND
            subModello = subModello.substring(k);
            
          } else {
            subModello = subModello.substring(j+1);
          }
          
        } else {        	
          // ERRORE: Manca il tag di fine campo
          // Blocco la ricerca
          j = -1;
        }
        
      }
     
    }  // while
   
    subModello = modello.substring(startChar);
   
    if (!prov.equalsIgnoreCase("C")){ //se provengo dal caricatore non inserisco i blocchi.
      // ciclo per i blocchi
      i = 0;
      while ((i != -1) && (!subModello.equals(""))) {   
        i = subModello.indexOf(Parametri.getNameBlockBegin());
        if (i != -1) {      
          // Trovato un blocco
          h = subModello.indexOf(Parametri.getNameBlockEnd())+ (Parametri.getNameBlockEnd()).length();
          i1 = subModello.indexOf(Parametri.getNameBlockBegin(),i+1);
          if (i1 == -1) {
            i1 = h + 1;
          }
          if (h != -1) {
            if (h < i1) {
            // Da i a h+TAGBlockEND.length() è un blocco
              bloccoHtml = subModello.substring(i, h);
              try {
                scriviBlocco(area, codice, bloccoHtml);
              } catch (Exception eeb) {
//              	dbOpSQLMaster.rollback(); 
//                free(dbOpSQLMaster);
                throw new Exception("interpretaModello(): Errore in fase di scrittura blocco: "+eeb.toString());
              }
            // Mi resta da analizzare tutto quanto si trova dopo il TAGblockEND
              subModello = subModello.substring(h);
            } else {
              subModello = subModello.substring(i+1);
            }
          } else {
            // ERRORE: Manca il tag di fine blocco
            // Blocco la ricerca
            i = -1;
          }
        }
      }  // while
    }  
  }
 
  
  
  /**
   * Estrazione delle bitmap e inserimento nel database.
   */
  void interpretaBitmap(String area, String codice, 
                        String tipo, 
                        File fileModello) throws Exception {
//System.out.println("interpretaBitmap");  
    
    ResultSet         rst;
    String            querySelect,
                      queryInsert,
                      modello,
                      tempImg,
                      nomeImg;
    int               startChar, j, k, i;
    String            subModello/*, 
                      campoHtml*/;
    StringBuffer      tempPath;
    File              fImg;
    FileInputStream   fBmp;

    querySelect = "SELECT MODELLO FROM MODELLI WHERE "+
                    "AREA = '"+area+"' and "+
                    "CODICE_MODELLO = '"+codice+"'";

    try {
      dbOpSQLMaster.setStatement(querySelect);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
    } catch (Exception e) {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      throw new Exception("interpretaBitmap(): Errore in fase di SELECT ["+querySelect+"]: "+e.toString());
    }

    if (!rst.next()) {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      System.out.println("interpretaBitmap(): Errore nella ricerca del modello. Il modello non è presente sul DB.");
      throw new Exception("interpretaBitmap(): Il modello non è presente sul DB.");
    }

    // Ricavo il modello (il campo è un CLOB).
    Clob clob = rst.getClob("MODELLO");
    long clobLen = clob.length();

    modello = "";
    if (clobLen < MAX_LENGTH_MODELLO) {
      int i_clobLen = (int)clobLen;
      modello = clob.getSubString(1, i_clobLen);
    } else {
//      dbOpSQLMaster.rollback(); 
//      free(dbOpSQLMaster);
      System.out.println("interpretaBitmap(): Il modello supera la dimensione massima consentita ("+MAX_LENGTH_MODELLO +" byte)");
      throw new Exception("interpretaBitmap(): Il modello supera la dimensione massima consentita ("+MAX_LENGTH_MODELLO +" byte)");
    }
   
    // Imposta i parametri per l'interpretazione del modello
    Parametri.settaParametriModello(tipo);
    
    // Esame ed estrazione delle bitmap / include generici.
    // In pratica: tutte le volte che troviamo il tag di inclusione di un file esterno salviamo
    // lo stesso in fomrato binario nella tabella grafici modello (nella versione HTML è cio' che
    // segue il tag src=).
    startChar = 0;
    subModello = modello.substring(startChar);
    j = 0;

    while ((j != -1) && (subModello != "")) {
      j = subModello.indexOf(Parametri.getTagImgBegin());
      if (j!=-1) {
        subModello = subModello.substring(j+Parametri.getTagImgBegin().length());
        k = subModello.indexOf(Parametri.getTagImgEnd());  
        tempImg = subModello.substring(0,k);
        nomeImg = tempImg;
        // tempImg contiene la stringa con il path (relativo rispetto a fileModello) delle immagini
        // verifico se è un path relativo o assoluto guardando il primo carattere: se è un punto
        // il path è relativo
        if (tempImg.substring(0,1).equals(".")){
           // il Path è relativo
          if (tempImg.substring(0,2).equals("./")){
            // Calcolo il nome assoluto del file immagine sostituendo ./ con il path
            // assoluto di file modello
            tempImg = fileModello.getParent()+File.separator+tempImg.substring(2);
          } else if (tempImg.substring(0,3).equals("../")) {
            // Calcolo il nome assoluto del file immagine sostituendo a ../ il path 
            // del file modello a meno dell'ultima directory
            tempImg = fileModello.getParentFile().getParent()+File.separator+tempImg.substring(2);
          }
        } else {
          // Suppongo sia un path assoluto e quindi non faccio nulla
         
              tempImg = tempImg.substring(tempImg.indexOf("modelli/")+8);
              tempImg = tempImg.substring(tempImg.indexOf("/")+1);
              tempImg = fileModello.getParent()+File.separator+tempImg;        
        }
        tempPath = new StringBuffer(tempImg);
        // A questo punto devo vedere se sostituire il carattere di separazione dei nomi del file
        if (!File.separator.equals("/")){
          // Devo sostituire il carattere "/" con il carattere File.separator
          for(i = 0; i < tempPath.length(); i++) {
            if (tempPath.charAt(i) == '/'){
              tempPath.replace(i, i+1, File.separator);
            }
          }
        }

        k = tempPath.length();
        i=0;
        try {
          // Devo sostituire il %20 con uno spazio
          while (i < k-3) {
            if (tempPath.substring(i,i+3).equals("%20")) {
              tempPath.replace(i, i+3, " ");
              k = k - 2;
            }
            i++;
          }
        } catch (Exception e) {
//          dbOpSQLMaster.rollback(); 
//          free(dbOpSQLMaster);
          System.out.println("DeployerModel::interpretaBitmap() - Si è verificato un errore: "+e.toString());
          throw new Exception("interpretaBitmap(): Si è verificato un errore: "+e.toString());
        }

        try {
          fImg = new File(tempPath.toString());
          fBmp = new FileInputStream(fImg);
        } catch (Exception e) {
//          dbOpSQLMaster.rollback();
//          free(dbOpSQLMaster);
         
          System.out.println("DeployerModel::interpretaBitmap() - Si è verificato un errore: "+e.toString());
          throw new Exception("interpretaBitmap(): Errore in fase di lettura immagine: "+e.toString());
        }
        

        querySelect = "SELECT  * "+
                        "FROM GRAFICI_MODELLO "+
                        "WHERE AREA = '"+area+"' AND "+
                              "CODICE_MODELLO = '"+codice+"' AND "+
                              "NOMEFILE = '"+ nomeImg +"'";
                          
            
        queryInsert = "INSERT INTO GRAFICI_MODELLO "+
                        "(AREA, CODICE_MODELLO, NOMEFILE, GRAFICO) VALUES "+
                        "('"+ area +"','"+ codice +"','"+ nomeImg + "', :GRAFICO)";
        
        String queryUp =  "UPDATE GRAFICI_MODELLO SET GRAFICO = :GRAFICO " +
        									"WHERE AREA = '"+area+"' AND "+
        												"CODICE_MODELLO = '"+codice+"' AND "+
        												"NOMEFILE = '"+ nomeImg +"'";

        // Verifica la presenza dell'immagine e esegue l'insert o l'update 
        try {
          dbOpSQLMaster.setStatement(querySelect);
          dbOpSQLMaster.execute();    
          rst = dbOpSQLMaster.getRstSet();
         
          if (!rst.next()) {
            dbOpSQLMaster.setStatement(queryInsert);
          }else{
          	dbOpSQLMaster.setStatement(queryUp);
          }
          dbOpSQLMaster.setParameter(":GRAFICO", (InputStream)fBmp, -1);
          
          dbOpSQLMaster.execute();
          
          fBmp.close();
          k = subModello.indexOf(Parametri.getTagImgEnd());  
          subModello = subModello.substring(k+1);
          
        } catch (Exception e) {
//          dbOpSQLMaster.rollback();
//          free(dbOpSQLMaster);        
          System.out.println("DeployerModel::interpretaBitmap() - Si è verificato un errore: "+e.toString());
          throw new Exception("interpretaBitmap(): Errore in fase di registrazione dell'immagine ["+dbOpSQLMaster.getStatementString()+"]: "+e.toString());
        }
        
        // Verifica la presenza dell'immagine appena inserita
//        try {
//          dbOpSQLMaster.setStatement(querySelect);
//          dbOpSQLMaster.execute();    
//          rst = dbOpSQLMaster.getRstSet();
//         
//          if (!rst.next()) {
////            dbOpSQLMaster.rollback();
////            free(dbOpSQLMaster);            
//            throw new Exception("interpretaBitmap(): Non trovata sul DB l'immagine "+nomeImg+" appena inserita.");
//          }
//
//          String query =  "UPDATE GRAFICI_MODELLO SET GRAFICO = :GRAFICO " +
//                          "WHERE AREA = '"+area+"' AND "+
//                          "CODICE_MODELLO = '"+codice+"' AND "+
//                          "NOMEFILE = '"+ nomeImg +"'";
//          dbOpSQLMaster.setStatement(query);
//
//          dbOpSQLMaster.setParameter(":GRAFICO", (InputStream)fBmp, -1);
//          dbOpSQLMaster.execute();  
                  
//          fBmp.close();
//          k = subModello.indexOf(Parametri.getTagImgEnd());  
//          subModello = subModello.substring(k+1);
//        } catch (Exception e) {
////          dbOpSQLMaster.rollback();
////          free(dbOpSQLMaster);        
//          throw new Exception("interpretaBitmap(): Errore in UPDATE di GRAFICI_MODELLO: "+e.toString());
//        }
      }
    }
    rst = null;    
  }
  
  
  /**
   * Scrive sul database il campo $ACTIONKEY per i tipidocumento con uso='C' o 'Q';
   * controlla anche l'esistenza del campo nella tabella dei dati associati all'area 
   * GDMSYS 
   */
    protected void scriviCampoACTIONKEY(String codice, String area,
                                      int id_tipo)throws Exception {
//System.out.println("scriviCampoACTIONKEY");   
   
    ResultSet      rst;
    boolean        esiste;
    
    String         areadato="GDMSYS", dato="$ACTIONKEY";
    String         tipoCampo="S", tipoAccesso="S";
    String         queryInsert, querySelect, query;
    long           lunghezza = 0, decimali = 0;
      
    // ------------------------------------------------------------
    // Controllo se esiste già il dato $ACTIONKEY in GDMSYS
    // ------------------------------------------------------------
    querySelect = "SELECT LUNGHEZZA FROM DATI "+
                  "WHERE AREA = '"+ areadato +"' and DATO ='" + dato + "'";
    try {

      dbOpSQLMaster.setStatement(querySelect);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
      
      if (!rst.next()) {
        // Il dato non esiste in tabella per quell'area
      	rollbackDeployer();
        free(dbOpSQLMaster);
        throw new Exception("scriviCampoACTIONKEY(): Dato "+dato+" non previsto nell'area "+area+".");
      }else{
        lunghezza = rst.getLong("LUNGHEZZA");
      }
    } catch (Exception eSelect) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      throw new Exception("scriviCampoACTIONKEY(): Errore in fase di SELECT ["+querySelect+"]: "+eSelect.toString());
    }
  
    // Posso inserire il dato vero e proprio nella tabella DATI_MODELLO.
    // Se il dato era già presente, lo sovrascrivo.
    
    query = "SELECT * "+
              "FROM DATI_MODELLO "+
             "WHERE AREA = '"+ area +"' AND "+
                   "CODICE_MODELLO = '"+ codice +"' AND "+
                   "DATO = '"+ dato +"'";
    try {
      dbOpSQLMaster.setStatement(query);   
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();     
      esiste = rst.next();
  
      // Commit alla fine del metodo (se tutto ok)
    } catch (Exception e1) {
    	rollbackDeployer();   
      free(dbOpSQLMaster);
      e1.printStackTrace();
      throw new Exception("scriviCampoACTIONKEY(): Errore in fase di SELECT ["+query+"]: "+e1.toString());
    }
  
    // Inserimento.      
    if (!esiste){
      queryInsert = "INSERT INTO DATI_MODELLO "+
                    "(AREA, CODICE_MODELLO, DATO, LUNGHEZZA, DECIMALI, TIPO_CAMPO, TIPO_ACCESSO, AREA_DATO) VALUES "+
                    "('"+ area +"','"+ codice +"', '"+ dato +"',"+lunghezza+","+decimali+",'"+tipoCampo+"','"+tipoAccesso+"', '"+ areadato +"' )";
    }else{
      queryInsert = "UPDATE DATI_MODELLO SET "+
                      "LUNGHEZZA = "+ lunghezza +", DECIMALI = "+ decimali +", "+
                      "TIPO_CAMPO = '"+ tipoCampo +"', TIPO_ACCESSO ='"+ tipoAccesso +"', "+
                      "AREA_DATO = '"+ areadato +"' "+
                    "WHERE AREA = '"+ area +"' AND "+
                           "CODICE_MODELLO = '"+ codice +"' AND "+
                           "DATO = '"+ dato +"'";                  
    }
    try {
      dbOpSQLMaster.setStatement(queryInsert);
      dbOpSQLMaster.execute();      
      /*// Commit alla fine del metodo (se tutto ok)      
      if (dbOpSQL.getExecInt() == 0) {
        free(dbOpSQL);
        throw new Exception("scriviCampoACTIONKEY(): Errore in fase di INSERT/UPDATE ["+queryInsert+"].");
      }*/
    } catch(Exception eInsert) {
      
      rollbackDeployer();
      free(dbOpSQLMaster);
       
      throw new Exception("scriviCampoACTIONKEY(): Errore in fase di INSERT/UPDATE ["+queryInsert+"]: "+eInsert.toString());
    } 
    
    try {
      String id = Integer.toString(id_tipo);
      if (id != null && !id.equalsIgnoreCase("") && !id.equalsIgnoreCase("0")) {
        inserisciCampoDocumento(area, codice, dato, id_tipo);
      }
    } catch(Exception eInsertCampo) {
//    Rollback e chiusura sono nella chiamata. Esce
      throw new Exception("scriviCampoACTIONKEY(): Errore in fase di inserimento del campo: "+eInsertCampo.getMessage());
    }
  }

 

/**
   * Interpreta la stringa che rappresenta il campo e scrive il dato_modello su database;
   * Controlla anche l'esistenza del campo nella tabella dei dati associati all'area 
   * che si vuole utilizzare.
   * Se si è scelto controllare i campi doppi, se il campo esiste già in uso, blocca la pubblicazione.
   * Se si è scelto di non controllare i campi doppi, se il campo esste già in uso, procede senza inserire 
   * il dato modello che c'è già.
   */
  protected void scriviCampo(String area, 
                             String codice, 
                             String pTestoHtml, 
//                             Parametri parametri,
                             int id_tipo) throws Exception {
//System.out.println("scriviCampo");
                                     
    ResultSet      rst;
    boolean        esiste,esisteInUso,ignoracontrollocampidoppi;
    
    int            startChar, endChar;
    String         dato, areaDato;
    String         queryInsert, querySelect, query, query1;
    
    startChar = pTestoHtml.indexOf(Parametri.getNameFieldBegin());
    endChar   = pTestoHtml.indexOf(Parametri.getNameFieldEnd());

    // Cerco i marcatori di campo
    if ((startChar == -1) || (endChar == -1))  {
      // Problema di identificazione del campo
//      dbOpSQLMaster.rollback(); // nuo
//      free(dbOpSQLMaster);
      throw new Exception("scriviCampo(): Campo non identificabile all'interno di ["+pTestoHtml+"]");
    }

 // Estraggo il nome del campo e le informazioni aggiuntive.
 
    InformazioniCampo infoCampo = new InformazioniCampo(pTestoHtml,Parametri.getNameFieldEnd());
    
    dato = infoCampo.getDato();
   
    areaDato = infoCampo.getAreaDato();
   
    if ((areaDato == null) || (areaDato.equalsIgnoreCase(""))) {
      areaDato = area;
    }
    String domForm = "";
    
    // Controllo se esiste questo nome di campo per l'area indicata
    
    querySelect = "SELECT * FROM DATI "+
                  "WHERE AREA = '"+ areaDato +"' and DATO ='" + dato + "'";
  
    try {
      dbOpSQLMaster.setStatement(querySelect);
      dbOpSQLMaster.execute();
     
      rst = dbOpSQLMaster.getRstSet();
      
      if (!rst.next()) {
        // Il dato non esiste in tabella per quell'area
        //la dbOpSQLMaster.rollback(); e la free(dbOpSQLMaster); le farò nel catch
        throw new Exception("scriviCampo(): Dato non previsto nell'area indicata. area:["+areaDato+"]  dato:["+dato+"]");
      }
      
      domForm = rst.getString("DOMINIO_FORMULA");
      if (domForm == null) {
        domForm = "";
      }
     
    } catch (Exception eSelect) {
//      dbOpSQLMaster.rollback(); // nuo
//      free(dbOpSQLMaster);
      throw new Exception("scriviCampo(): Errore in fase di SELECT ["+querySelect+"]: "+eSelect.toString());
    }
    
    // Il tipo di dato che si vuole inserire è previsto nell'ambito dell'area indicata.
    // In questo caso posso inserire il dato vero e proprio nella tabella DATI_MODELLO.
    // Se il dato era già presente, lo sovrascrivo.
   
    query = "SELECT * "+
              "FROM DATI_MODELLO "+
             "WHERE AREA = '"+ area +"' AND "+
                   "CODICE_MODELLO = '"+ codice +"' AND "+
                   "DATO = '"+ dato +"'";
    
    try {
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
     
      esiste = rst.next();
      // Commit alla fine del metodo (se tutto ok)
    } catch (Exception e1) {
//      dbOpSQLMaster.rollback(); // nuo
//      free(dbOpSQLMaster);
      e1.printStackTrace();
      throw new Exception("scriviCampo(): Errore in fase di SELECT ["+query+"]: "+e1.toString());
    }
    // Estraggo le caratteristiche del campo 
  
    String tipoCampo   = infoCampo.getTipoCampo(); //metaInformazioniCampo.substring(0,1);
    String tipoAccesso = infoCampo.getTipoAccesso(); //metaInformazioniCampo.substring(1,2);
    String calcolato = infoCampo.getTipoCampoCalcolato();
    //Modifica MMA - Permette di caricare i domini formula anche per i vecchi modelli 
    //su cui non è indicata l'informazione del campo calcolato.

    if (calcolato.equalsIgnoreCase("X") ) {
      if (!domForm.equalsIgnoreCase("")) {
        calcolato = "S";
      } else {
        calcolato = "N";
      }
    }
   
    String blocco = infoCampo.getBlocco();
    int    lunghezza = 0, 
           decimali = 0;
    try {
      lunghezza = Integer.parseInt(infoCampo.getLunghezza()); //Integer.parseInt(metaInformazioniCampo.substring(2,5));
      decimali = Integer.parseInt(infoCampo.getDecimali()); //Integer.parseInt(metaInformazioniCampo.substring(5,8));
    } catch (Exception bne) {
//      dbOpSQLMaster.rollback(); // nuo
//      free(dbOpSQLMaster);
      throw new Exception("scriviCampo(): Informazioni non valide per il campo da inserire.\n"+
                          "campo ["+dato+"]  lunghezza ["+infoCampo.getLunghezza()+"] decimali ["+infoCampo.getDecimali()+"]");
    }
    
    ignoracontrollocampidoppi = false;
   
    if (esiste){
    
      String queryControlloCampiDoppi = "SELECT VALORE FROM PARAMETRI "+
                                        "WHERE CODICE = 'CONTROLLO_CAMPI' "+
                                        "AND TIPO_MODELLO = 'ADS'";
      try {
        dbOpSQLMaster.setStatement(queryControlloCampiDoppi);
        dbOpSQLMaster.execute();
        rst = dbOpSQLMaster.getRstSet();    
        if( rst.next()){
        
          String valore = rst.getString("VALORE");
          if (valore.equalsIgnoreCase("N")){
            ignoracontrollocampidoppi = true;
          }
        }
      } catch (Exception e1) {
//        dbOpSQLMaster.rollback(); // nuo
//        free(dbOpSQLMaster);
        e1.printStackTrace();
        throw new Exception("scriviCampo(): Errore in fase di SELECT ["+queryControlloCampiDoppi+"]: "+e1.toString());
      }   
    }
   
   if ((esiste) && (!ignoracontrollocampidoppi)){
    query1 = "SELECT * "+
             "FROM DATI_MODELLO "+
             "WHERE AREA = '"+ area +"' AND "+
                 "CODICE_MODELLO = '"+ codice +"' AND "+
                 "DATO = '"+ dato +"' AND "+
                 "IN_USO = 'Y'";
  
     try {
       dbOpSQLMaster.setStatement(query1);
       dbOpSQLMaster.execute();
       rst = dbOpSQLMaster.getRstSet();    
       esisteInUso = rst.next();
     
     } catch (Exception e1) {
//       dbOpSQLMaster.rollback(); 
//       free(dbOpSQLMaster);
       e1.printStackTrace();
       throw new Exception("scriviCampo(): Errore in fase di SELECT ["+query1+"]: "+e1.toString());
     }       
     
    
     if (esisteInUso){    
    	
//       dbOpSQLMaster.rollback(); 
     //       free(dbOpSQLMaster);
      
       throw new Exception("scriviCampo(): Errore: si sta cercando di inserire due campi con lo stesso nome ("+dato+")" );
     }         
   }
 
    // Inserimento.      
   if (!esiste){
       queryInsert = "INSERT INTO DATI_MODELLO "+
                    "(AREA, CODICE_MODELLO, DATO, AREA_DATO, LUNGHEZZA, DECIMALI, TIPO_CAMPO, TIPO_ACCESSO, CAMPO_CALCOLATO, BLOCCO, IN_USO) VALUES "+
                    "('"+ area +"','"+ codice +"', '"+ dato +"', '"+ areaDato +"',"+lunghezza+","+decimali+",'"+tipoCampo+"','"+tipoAccesso+"','"+calcolato+"','"+blocco+"', 'Y')";
    }else{
    
      queryInsert = "UPDATE DATI_MODELLO SET "+
                      "AREA_DATO = '"+ areaDato +"', LUNGHEZZA = "+ lunghezza +", DECIMALI = "+ decimali +", "+
                      "TIPO_CAMPO = '"+ tipoCampo +"', TIPO_ACCESSO ='"+ tipoAccesso +"', "+
                      "CAMPO_CALCOLATO = '"+ calcolato +"', BLOCCO ='"+ blocco +"', IN_USO = 'Y' "+
                    "WHERE AREA = '"+ area +"' AND "+
                           "CODICE_MODELLO = '"+ codice +"' AND "+
                           "DATO = '"+ dato +"'";                  
    
    }
    try {
      dbOpSQLMaster.setStatement(queryInsert);
      dbOpSQLMaster.execute();      
      /*// Commit alla fine del metodo (se tutto ok)      
      if (dbOpSQL.getExecInt() == 0) {
        free(dbOpSQL);
        throw new Exception("scriviCampo(): Errore in fase di INSERT/UPDATE ["+queryInsert+"].");
      }*/
    } catch(Exception eInsert) {
//      try{
//       dbOpSQLMaster.rollback(); 
//       free(dbOpSQLMaster);
//      }catch(Exception eeer) {}  
      throw new Exception("scriviCampo(): Errore in fase di INSERT/UPDATE ["+queryInsert+"]: "+eInsert.toString());
    } 
 
    try {
      String id = Integer.toString(id_tipo);
      if (id != null && !id.equalsIgnoreCase("") && !id.equalsIgnoreCase("0")) {
        inserisciCampoDocumento(area, codice, dato, id_tipo);
      }
    } catch(Exception eInsertCampo) {
      throw new Exception("scriviCampo(): Errore in fase di inserimento del campo: "+eInsertCampo.getMessage());
    }
  }


/**
   * Interpreta la stringa che rappresenta il blocco e scrive il blocco_modello su database;
   * Controlla anche l'esistenza del blocco per la stessa area del modello.
   */
  protected void scriviBlocco(String area, 
                             String codice, 
                             String pTestoHtml) throws Exception {
//                             Parametri parametri) throws Exception {
//System.out.println("scriviBlocco");
  
                                 
    ResultSet      rst;
    boolean        esiste;
    
    int            startChar, endChar;
    String         blocco, areaBlocco/*, tipoDato, lunghezzaDato*/;
    String         queryInsert, querySelect, query;           

    startChar = pTestoHtml.indexOf(Parametri.getNameBlockBegin());
    endChar   = pTestoHtml.indexOf(Parametri.getNameBlockEnd());

    // Cerco i marcatori di blocco
    if ((startChar == -1) || (endChar == -1))  {
      // Problema di identificazione del blocco
    	rollbackDeployer(); 
      free(dbOpSQLMaster);
      throw new Exception("scriviBlocco(): Blocco non identificabile all'interno di ["+pTestoHtml+"]");
    }

    InformazioniBlocco infoBlocco = new InformazioniBlocco(pTestoHtml);
    blocco = infoBlocco.getBlocco();
    areaBlocco = infoBlocco.getAreaBlocco();
    if (areaBlocco.equalsIgnoreCase("")) {
      areaBlocco = area;
    }
    
    querySelect = "SELECT * FROM BLOCCHI "+
                  "WHERE AREA = '"+ areaBlocco +"' and BLOCCO ='" + blocco + "'";
    try {
      dbOpSQLMaster.setStatement(querySelect);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
      
      if (rst.next() == false) {
        // Il blocco non esiste in tabella per quell'area
        //dbOpSQLMaster.rollback(); e free(dbOpSQLMaster); nel catch
        throw new Exception("scriviBlocco(): Blocco non previsto nell'area indicata. area:["+areaBlocco+"]  blocco:["+blocco+"]");
      }  
    } catch (Exception eSelect) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      throw new Exception("scriviBlocco(): Errore in fase di SELECT ["+querySelect+"]: "+eSelect.toString());
    }

    query = "SELECT * "+
              "FROM BLOCCHI_MODELLO "+
             "WHERE AREA = '"+ area +"' AND "+
                   "CODICE_MODELLO = '"+ codice +"' AND "+
                   "AREA_BLOCCO = '"+ areaBlocco +"' AND "+
                   "BLOCCO = '"+ blocco +"'";
    try {
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
      rst = dbOpSQLMaster.getRstSet();
    
      esiste = rst.next();
      // Commit alla fine del metodo (se tutto ok)
    } catch (Exception e1) {
    	rollbackDeployer();
      free(dbOpSQLMaster);
      e1.printStackTrace();
      throw new Exception("scriviBlocco(): Errore in fase di SELECT ["+query+"]: "+e1.toString());
    }
    // Estraggo le caratteristiche del blocco 
    String legame   = infoBlocco.getLegame().replaceAll("'","''"); 
    legame = legame.replaceAll("&lt;", "<");
    legame = legame.replaceAll("&gt;", ">");
    String numeroRecord = infoBlocco.getNumeroRecord(); 
    String aggiungi = infoBlocco.getAggiungi();
    String ordinamento = infoBlocco.getOrdinamento();
   
    // Inserimento.      
    if (!esiste){
      queryInsert = "INSERT INTO BLOCCHI_MODELLO "+
                    "(AREA, CODICE_MODELLO, AREA_BLOCCO, BLOCCO, CONDIZIONI_LEGAME, CONDIZIONI_NAVIGAZIONE, AGGIUNGI, ORDINAMENTO) VALUES "+
                    "('"+ area +"','"+ codice +"','"+ areaBlocco +"', '"+ blocco +"','"+legame+"',"+numeroRecord+",'"+aggiungi+"','"+ordinamento+"')";
    }else{
      queryInsert = "UPDATE BLOCCHI_MODELLO SET "+
                      "CONDIZIONI_LEGAME = '"+ legame +"', CONDIZIONI_NAVIGAZIONE = "+ numeroRecord +", "+
                      "AGGIUNGI = '"+ aggiungi +"', ORDINAMENTO ='"+ ordinamento +"' "+
                    "WHERE AREA = '"+ area +"' AND "+
                           "CODICE_MODELLO = '"+ codice +"' AND "+
                           "AREA_BLOCCO = '"+ areaBlocco +"' AND "+
                           "BLOCCO = '"+ blocco +"'";                  
    }

    try {
      dbOpSQLMaster.setStatement(queryInsert);
      dbOpSQLMaster.execute();

      /*// Commit alla fine del metodo (se tutto ok)      
      if (dbOpSQL.getExecInt() == 0) {
        free(dbOpSQL);
        throw new Exception("scriviBlocco(): Errore in fase di INSERT/UPDATE ["+queryInsert+"].");
      }*/
    } catch(Exception eInsert) {
     
    	rollbackDeployer(); 
      free(dbOpSQLMaster);
    
      throw new Exception("scriviBlocco(): Errore in fase di INSERT/UPDATE ["+queryInsert+"]: "+eInsert.toString());
    }
  }
  
  
 /*
  * Inserisce il file del modello nel campo FILE_ORIGINALE nella tabella MODELLI e 
  * i file delle immagini del modello nella tabella GRAFICI_MODELLO.
  */                            
  public static void caricaModelloDB(Connection dbConn, String area, 
                                   String codice, String realPath, 
                                   String idSess){
  
//System.out.println("caricaModelloDB");   
	  String tabella = "MODELLI";
	  String colonna = "FILE_ORIGINALE";
	  String condiz = "WHERE AREA = '"+ area +"' AND "+
				    "CODICE_MODELLO = '"+ codice +"'";

	  LetturaScritturaFileDB reader = null;
   
    IDbOperationSQL dbOpSQL = null;
			
	  try {
      dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(dbConn,0);
      dbOpSQL.autoCommitOff();
	    reader = new LetturaScritturaFileDB(dbConn, tabella, colonna, condiz);
	    InputStream bis = reader.leggiFile();

	    String dirName = realPath;
	    String workingFolder = "common" + File.separator + "folder_upload";
	    dirName = dirName + workingFolder;
	    File f; 

	    String nomeEPathFile = dirName + File.separator + idSess + File.separator + codice+".html";
		
	    f = new File(nomeEPathFile);
	    if (f.exists()) {
	      f.delete();
	    }else{
//	    boolean ok = new File(dirName + File.separator + idSess).mkdirs();
        File fdir = new File(dirName + File.separator + idSess);
		   		   	
        if (!fdir.exists()) {
          System.out.println("Directory NON creata!");
          return;
        }
	    }

	    f = new File(nomeEPathFile);

	    LetturaScritturaFileFS writer = null;
	 	
	    writer = new LetturaScritturaFileFS(nomeEPathFile);
	    writer.scriviFile(bis);

      String nomeAll = null,
             nomeFile = null;
      String query = "SELECT NOMEFILE FROM GRAFICI_MODELLO "+
                   " WHERE AREA = '"+area+"' AND "+
                   "CODICE_MODELLO = '"+ codice +"'";
      ResultSet rstSQL = null;
      dbOpSQL.setStatement(query);
      dbOpSQL.execute();
      rstSQL = dbOpSQL.getRstSet();
//    boolean ok = new File(dirName + File.separator + idSess + File.separator + codice +"_file").mkdirs();
      while (rstSQL.next()) {
        nomeAll = rstSQL.getString("NOMEFILE");

        tabella = "GRAFICI_MODELLO";
        colonna = "GRAFICO";
        condiz = "WHERE AREA = '"+ area +"' AND "+
				    "CODICE_MODELLO = '"+ codice +"' AND "+
				    "NOMEFILE = '"+ nomeAll + "'";

        reader = new LetturaScritturaFileDB(dbConn, tabella, colonna, condiz);
        bis = reader.leggiFile();

        int i = nomeAll.lastIndexOf("/");
        if (i != -1) {
          nomeFile = nomeAll.substring(i);
        } else {
          nomeFile = nomeAll;
        }
        nomeEPathFile = dirName + File.separator + idSess  + File.separator + codice +"_file" + File.separator + nomeFile;
        f = new File(nomeEPathFile);
        if (!f.exists()) {
          writer = new LetturaScritturaFileFS(nomeEPathFile);
          writer.scriviFile(bis);
        }
      }
      free(dbOpSQL);
  
	  }catch (Exception exc) {
      free(dbOpSQL);
	    System.out.println("caricaModelloDB(): Errore nell'upload del file: "+exc.toString());
	  }
  }

   
  /**
   * Elimina i vecchi record da blocchi_modello
   */   
   public boolean eliminaBlocchiModello(String area, String codice) throws Exception {
//System.out.println("eliminaBlocchiModello"); 
    String          query;
    
    query = "DELETE FROM BLOCCHI_MODELLO WHERE "+
              "AREA = '"+area+"' AND "+
              "CODICE_MODELLO = '"+codice+"'";
    try {
//System.out.println("query="+query);               
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
          
    } catch(Exception e) {
       
    	rollbackDeployer();
      free(dbOpSQLMaster);
       
      throw new Exception("eliminaBlocchiModello(): Errore nella DELETE ["+query+"]: "+e.toString());
    } 
    
    return true;
  }
   

   /**
   * Elimina logicamente i vecchi record da dati_modello, mettendoli non in uso
   */   
   public boolean fuoriUsoCampiModello(String area, String codice) throws Exception {
//System.out.println("fuoriUsoCampiModello"); 
    String          query;
    
    query = "UPDATE DATI_MODELLO SET "+
            "IN_USO = 'N' WHERE "+
              "AREA = '"+area+"' AND "+
              "CODICE_MODELLO = '"+codice+"'";
    try {
//System.out.println("query="+query);               
      dbOpSQLMaster.setStatement(query);
      dbOpSQLMaster.execute();
          
    } catch(Exception e) {
      
    	rollbackDeployer();
      free(dbOpSQLMaster);
      
      throw new Exception("fuoriUsoCampiModello(): Errore nella UPDATE ["+query+"]: "+e.toString());
    } 
    
    return true;
  }
  
   
 /*
 * Chiude la connessione aperta dal costruttore 
 */
  public void closeDeployer(){
    try{
      dbOpSQLMaster.close();
    }catch(Exception ec){}       
  }


  /*
   * main
   */
  public static void main(String[] args) {

//    Deployer deployer = null;
   
    try {
    Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
//    Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:SI3","ads","ads");
  

//    deployer = new Deployer(;
 /*   SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");
    
    DbOperationSQL dbop = new DbOperationSQL(conn,0);
    
    
    }*/

//      deployer = new Deployer(conn,"ad4.htm.zip", "c:\\temp", "MarikaID94837983948738");
//      deployer.trattaZip();
//      deployer.caricaModello();
  // File f1 = new File("C:\\Programmi\\Tomcat\\webapps\\GDMWEB\\common\\modelli\\AD4\\MOD.html");
//if ( (!f1.exists()) || (!f1.isFile()) ) {
//      System.out.println("Errore: Attenzione! Il file indicato non è valido.");
//        f1 = null;
//        return ;
//      }
//     
  //deployer.pubblica("AD4", "MOD", "2", "mtorrisi", "ADS", 1, "", f1);
//   deployer.pulisciTutto("c:\\temp"+"\\"+"MarikaID94837983948738");

      
    } catch (Exception myEx) {
      System.out.println("Errore: "+myEx.toString());
    }
 
    //contenutoFileModello = deployer.getContenutoFileModello();
  }

  
  /*
   * Crea la tabella orizzontale per il TipoDocumento del modello
   */
  private String creaTabella(Connection cn, String area, String cm) throws Exception {
    String query = "";
    String creatable = "";
    int esiste = 0;
    String nometabella = "";
    String nomeAlias = "";
    String acroArea = "";
    String acroMod = "";
    String aliasMod = "";
    String aliasTab = "";
    String nomeCol = "";
    ResultSet rst = null;
    ResultSet rst2 = null;
    IDbOperationSQL dbOp = null;
    IDbOperationSQL dbOp2 = null;
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn,0);
      query = "SELECT UPPER(nvl(T.ALIAS_VIEW,'')), upper(A.ACRONIMO), upper(T.ACRONIMO_MODELLO), upper(T.ALIAS_MODELLO) "+
              "  FROM TIPI_DOCUMENTO T,"+
              "       AREE A"+
              " WHERE T.AREA_MODELLO = '"+area+"'"+
              "   AND NOME = '"+cm+"'"+
              "   AND A.AREA = T.AREA_MODELLO"+
              "   AND A.ACRONIMO IS NOT NULL"+
              "   AND T.ACRONIMO_MODELLO IS NOT NULL"+
              "   AND T.ALIAS_MODELLO IS NOT NULL";
      dbOp.setStatement(query);
      dbOp.execute();
      rst = dbOp.getRstSet(); 
      if (rst.next()) {
        nomeAlias = rst.getString(1);
        acroArea  = rst.getString(2);
        acroMod   = rst.getString(3);
        aliasMod  = rst.getString(4);
      } else {
        dbOp.close();
        throw new Exception("Informazioni non presenti per il modello: "+cm+" dell'area: "+area);
      }
      if (nomeAlias == null) {
        nomeAlias = "";
      }

      nometabella = acroArea+'_'+aliasMod;
      aliasTab = acroArea+acroMod;
      
      query = "SELECT COUNT(1)"+ 
              "  FROM COLS"+ 
              " WHERE TABLE_NAME = '"+nometabella+"'"+ 
              "   AND COLUMN_NAME = 'ID_DOCUMENTO'";
      dbOp.setStatement(query);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        esiste = rst.getInt(1);
      }
      if (esiste == 0) {
        //Creo la tabella
        creatable = "CREATE TABLE "+nometabella+"( ID_DOCUMENTO NUMBER(10) NOT NULL";
        query = "SELECT UPPER(d.dato) C_NOME_COLONNA,"+ 
                " decode(d.tipo,'S','VARCHAR2('||TO_CHAR(d.lunghezza)||')','D','DATE',"+
                        "'N','NUMBER('||TO_CHAR(d.lunghezza)||','||TO_CHAR(d.decimali)||')') C_TIPO_COLONNA"+
                "  FROM dati_modello dm,"+
                "       dati d"+
                "  WHERE dm.area = '"+area+"' AND"+ 
                " dm.codice_modello = '"+cm+"' AND"+ 
                " nvl(dm.in_uso,'Y') = 'Y' AND"+
                " d.area = dm.area_dato AND"+ 
                " d.dato = dm.dato AND"+
                " upper(d.dato) <> 'ID_DOCUMENTO' and"+
                " d.lunghezza <= 4000 "+
                "UNION "+
                "SELECT UPPER(d.dato) C_NOME_COLONNA, 'CLOB' C_TIPO_COLONNA"+
                "  FROM dati_modello dm,"+
                "       dati d"+
                " WHERE dm.area = '"+area+"' AND"+ 
                " dm.codice_modello = '"+cm+"' AND"+ 
                " nvl(dm.in_uso,'Y') = 'Y' AND"+
                " d.area = dm.area_dato AND"+ 
                " upper(d.dato) <> 'ID_DOCUMENTO' and"+
                " d.dato = dm.dato AND"+
                " d.lunghezza > 4000";
        dbOp.setStatement(query);
        dbOp.execute();
        rst = dbOp.getRstSet();
        while (rst.next()) {
          creatable += ", \""+  rst.getString(1) + "\" " + rst.getString(2);
        }
        creatable += ",FULL_TEXT CLOB";
        creatable += ", CONSTRAINT "+aliasTab+"_ID_DOCUMENTO_UK UNIQUE (ID_DOCUMENTO))";
        dbOp.setStatement(creatable);
        dbOp.execute();
        query = "ALTER TABLE "+nometabella+" ADD CONSTRAINT "+aliasTab+"_DOCU_FK"+
                " FOREIGN KEY (ID_DOCUMENTO) REFERENCES DOCUMENTI (ID_DOCUMENTO) ON DELETE CASCADE";
        dbOp.setStatement(query);
        dbOp.execute();
        query = "CREATE INDEX "+aliasTab+"_FULL_TEXT_CTX ON "+nometabella+
                " (FULL_TEXT) INDEXTYPE IS CTXSYS.CONTEXT "+
                "PARAMETERS('filter ctxsys.null_filter "+
                "lexer italian_lexer "+
                "wordlist italian_wordlist "+
                "stoplist italian_stoplist') "+
                "NOPARALLEL";
        dbOp.setStatement(query);
        dbOp.execute();
      } else {
        //Aggiungo le colonne nuove
        query = "SELECT d.dato, d.tipo, d.lunghezza, d.decimali"+ 
                "  FROM dati_modello dm,"+
                "       dati d"+
                "  WHERE dm.area = '"+area+"' AND"+ 
                " dm.codice_modello = '"+cm+"' AND"+ 
                " dm.in_uso = 'Y' AND"+
                " d.area = dm.area_dato AND"+ 
                " d.dato = dm.dato";
        dbOp.setStatement(query);
        dbOp.execute();
        rst = dbOp.getRstSet();
        dbOp2 = SessioneDb.getInstance().createIDbOperationSQL(cn,0);
        while (rst.next()) {
          nomeCol = rst.getString(1);
          String tipoCol = rst.getString(2);
          int lungCol = rst.getInt(3);
          int deciCol = rst.getInt(4);
          query = "SELECT DATA_TYPE, DATA_LENGTH, DATA_PRECISION, NVL(DATA_SCALE,0)"+ 
                  "  FROM COLS"+ 
                  " WHERE TABLE_NAME = '"+nometabella+"'"+ 
                  "   AND COLUMN_NAME = UPPER('"+nomeCol+"')";
          dbOp2.setStatement(query);
          dbOp2.execute();
          rst2 = dbOp2.getRstSet();
          if (rst2.next()) {
           query = "";
           if (tipoCol.equalsIgnoreCase("S") && lungCol <= 4000 && 
                (!rst2.getString(1).equalsIgnoreCase("VARCHAR2") || lungCol > rst2.getInt(2))) {
              query = "ALTER TABLE "+nometabella+" MODIFY "+nomeCol+" VARCHAR2("+lungCol+")";
            }
            if (tipoCol.equalsIgnoreCase("S") && lungCol > 4000 && !rst2.getString(1).equalsIgnoreCase("CLOB")) {
              query = "ALTER TABLE "+nometabella+" ADD TMP"+nomeCol+" CLOB";
              dbOp2.setStatement(query);
              dbOp2.execute();
              query = "UPDATE "+nometabella+" SET TMP"+nomeCol+" = "+nomeCol;
              dbOp2.setStatement(query);
              dbOp2.execute();
              dbOp2.commit();
              query = "ALTER TABLE "+nometabella+" DROP COLUMN "+nomeCol;
              dbOp2.setStatement(query);
              dbOp2.execute();
              query = "ALTER TABLE "+nometabella+" RENAME COLUMN TMP"+nomeCol+" TO "+nomeCol;
            }
            if (tipoCol.equalsIgnoreCase("D") && !rst2.getString(1).equalsIgnoreCase("DATE")) {
              query = "ALTER TABLE "+nometabella+" MODIFY "+nomeCol+" DATE";
            }
            if (tipoCol.equalsIgnoreCase("N")) {  
              if (lungCol != rst2.getInt(3) || deciCol != rst2.getInt(4) || !rst2.getString(1).equalsIgnoreCase("NUMBER")) {
                query = "ALTER TABLE "+nometabella+" MODIFY "+nomeCol+" NUMBER("+lungCol+","+deciCol+")";
              }
            }
          } else {
            if (tipoCol.equalsIgnoreCase("S") && lungCol <= 4000) {
              query = "ALTER TABLE "+nometabella+" ADD "+nomeCol+" VARCHAR2("+lungCol+")";
            }
            if (tipoCol.equalsIgnoreCase("S") && lungCol > 4000) {
              query = "ALTER TABLE "+nometabella+" ADD "+nomeCol+" CLOB";
            }
            if (tipoCol.equalsIgnoreCase("D")) {
              query = "ALTER TABLE "+nometabella+" ADD "+nomeCol+" DATE";
            }
            if (tipoCol.equalsIgnoreCase("N")) {
              query = "ALTER TABLE "+nometabella+" ADD "+nomeCol+" NUMBER("+lungCol+","+deciCol+")";
            }
          }
          if (!query.equalsIgnoreCase("")) {
            dbOp2.setStatement(query);
            dbOp2.execute();
          }
        }
        dbOp2.close();
      }
      dbOp.close();
      return "";
    } catch (Exception e) {
      System.out.println(e.getMessage());
      try {
        dbOp.close();
        dbOp2.close();
      } catch (Exception e2) {}
      throw e;
    }
    
  }
  
  

  /**
   * Libera la connessione, chiudendo la IdbOperationSQL
   */
  private static void free(IDbOperationSQL dbOpSQL) {
//System.out.println("free");   
    try 
    {      
      dbOpSQL.close();
    } catch (Exception exc) {
//      System.out.println("Errore: "+exc.toString());
    }
  }

  
  
  /**
   * Esegue il rollback sulla connessione su cui si sta lavorando
   */
  public void rollbackDeployer() {
    try {
      dbOpSQLMaster.rollback(); 
    } catch(Exception eeer) {} 
  }
}

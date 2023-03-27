package it.finmatica.dmServer;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
/*
 * RIFERIMENTO
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   27/04/2006
 * 
 * */

public class Riferimento 
{
  // Indicano i due tipi di connessione
  final static String CONNECTION_EXTERN="EXTERN";
  final static String CONNECTION_STANDARD="STANDARD";

  // variabili private
  private Environment varEnv;
  private String idDocumentoRif=null;
  private String tipoRelazione=null;
  private String idDocumento=null;
  private String area=null;
  private String descrRif, descrDoc; 
   
  // Variabile che indica il tipo di connessione
  String casoConnection="";
       
  
    
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public Riferimento(String id_documento, String id_documento_rif, String tipo_relazione,
                           String descrRif,String descrDoc, String area)  throws Exception {
                
         this.idDocumento=id_documento;
         this.idDocumentoRif=id_documento_rif;          
         this.tipoRelazione=tipo_relazione;
         this.area=area;
         this.descrRif=descrRif;
         this.descrDoc=descrDoc;
  }

  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public Riferimento(String id_documento_rif, String tipo_relazione)  throws Exception {         
        
         this.idDocumentoRif  = id_documento_rif;          
         this.tipoRelazione   = tipo_relazione;
  }  
  
  
  /*
   * METHOD:      inizializzaDati(Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              
   * 
   * RETURN:      void
  */  
  public void inizializzaDati(Object vUtente) throws Exception
  {
         this.inizializzaDati((Environment) vUtente);
        
  }

  /*
   * METHOD:      inizializzaDati(Object, Environment)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  private void inizializzaDati(Environment vUtente) throws Exception
  {         
          this.varEnv= vUtente;
                 
          // Controllo il tipo di connessione interna o esterna
          if( varEnv.Global.CONNECTION != null )
             casoConnection=CONNECTION_EXTERN;
          else
             casoConnection=CONNECTION_STANDARD;
  }
  
  
  // ***************** METODI GET E SET ***************** //
  
  public String getTipoRelazione() {
         return tipoRelazione;
  } 

  public String getChiaveDocPrincipale() {
         return descrDoc;
 }

  public String getChiaveDocRiferito() {
         return descrRif;
  } 
  
  public String getIdDocumento() {
         return idDocumento;
  }   

  public String getIdDocumentoRif() {
         return idDocumentoRif;
  }     

  public String getArea() {
         return area;
  }  
  
  /*
   * METHOD:      getDocPrincipale()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il Profilo del documento principale
   *              
   * RETURN:      Profilo
  */
  public Profilo getDocPrincipale() 
  {
         return getProfilo(this.getIdDocumento());
  }   

  /*
   * METHOD:      getDocRiferito()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il Profilo del documento riferito
   *              
   * RETURN:      Profilo
  */
  public Profilo getDocRiferito() 
  {
         return getProfilo(this.getIdDocumentoRif());
  } 
  

  // ***************** METODI PRIVATI ***************** //
  
   /*
   * METHOD:      getDocProfilo(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il Profilo del documento dato da idDoc
   *              
   * RETURN:      Profilo
  */
  private Profilo getProfilo(String idDoc) 
  {
         Profilo p = null;
 
         try {
              p= new Profilo(idDoc);                           
              
              // Inizializzazione variabili d'ambiente secondo il tipo di connessione 
              if( casoConnection.equals(CONNECTION_EXTERN) )
                 p.initVarEnv(varEnv.getUser(),varEnv.getPwd(), varEnv.Global.CONNECTION);
              else
                 p.initVarEnv(varEnv.getUser(),varEnv.getPwd(),varEnv.getIniFile());
              
              p.escludiControlloCompetenze(varEnv.getByPassCompetenze());
              
              if (p.accedi(Global.ACCESS_NO_ATTACH).booleanValue())
              {
                 return p;
              }
              else
              {
                 System.out.println("Impossibile accedere");
                 System.out.println(p.getError());
                 System.out.println(p.getCleanError()); 
                 return null;
              }
          }
          catch (Exception e) 
          {         
             System.out.println(e.getMessage());
             return null;
          }
  }   

} 
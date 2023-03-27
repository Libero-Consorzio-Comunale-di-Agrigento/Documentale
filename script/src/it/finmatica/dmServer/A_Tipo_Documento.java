package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI TIPI DOCUMENTI
 * DIPENDENZE CON: GD4_Tipo_Documento
 *                 Humm_Tipo_Documento
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import it.finmatica.dmServer.util.ElapsedTime;

import java.util.*;

public abstract class A_Tipo_Documento
{
  // Variabili private
  private String idTipodoc = "0";
  private String tipoLog;
  private String tipoLogFile;
  private A_Libreria libreria;
  private int isHorizontalModel = 0;
  private String nome;
  private Vector campi;
  private String competenze;
  protected String competenzeAllegati="N";
  protected int letturaAllegati=1;
  protected int modificaAllegati=1;
  protected int cancellaAllegati=1;


private long nMaxAllegati = -1;
  protected Environment vu;
  

 // ***************** METODO DI INIZIALIZZAZIONE ***************** // 
      
  /*
   * METHOD:      inizializzaDati()
   * SCOPE:       PUBLIC
   * DESCRIPTION: Inizializzazione degli oggetti vuoti: (libreria,campi)  
   * RETURN:      void
  */  
  public void inizializzaDati(Environment newVu) throws Exception
  {
         vu=newVu;
          
         //Crea la libreria del DM da utilizzare
         try {
           libreria = (A_Libreria)Class.forName(vu.Global.PACKAGE + 
                                                "." + vu.Global.DM + 
                                                "_" + vu.Global.LIBRERIA).newInstance();
         }
         catch (Exception e) {
               throw new Exception("A_Tipo_Documento::inizializzaDati - Non riesco a creare l'oggetto di Classe: " + 
                                  vu.Global.PACKAGE + "." + vu.Global.DM + "_" + 
                                  vu.Global.LIBRERIA);
         }
         campi = new Vector();
         
  }

  // ***************** METODI DI SET E GET ***************** //
  
  public String getIdTipodoc()
  {
         return idTipodoc;
  }
   
  public void setIdTipodoc(String newIdTipodoc)
  {
         idTipodoc = newIdTipodoc;
  }    
   
  public int getIsHorizontalModel() {
	     return isHorizontalModel;
  }

  public void setIsHorizontalModel(int isHorizontalModel) {
	     this.isHorizontalModel = isHorizontalModel;
   }

public A_Libreria getLibreria()
  {
         return libreria;
  }
   
  public void setLibreria(A_Libreria newLibreria)
  {
         libreria = newLibreria;
  }
   
  public String getNome()
  {
         return nome;
  }
   
  public void setNome(String newNome)
  {
         nome = newNome;
  }

    public String getTipoLog()
  {
         return tipoLog;
  }
   
  public void setTipoLog(String newTipoLog)
  {
         tipoLog = newTipoLog;
  }
  	
  public String getTipoLogFile() {
		 return tipoLogFile;
  }

  public void setTipoLogFile(String tipoLogFile) {
	     this.tipoLogFile = tipoLogFile;
  }  
  
  public Vector getCampi()
  {
         return campi;
  }
   
  public void setCampi(Vector newCampi)
  {
         campi = newCampi;
  }

  public String getCompetenze()
  {
         return competenze;
  }
  
  public void setCompetenze(String newCompetenze)
  {
         competenze = newCompetenze;
  }
  
  public long getNMaxAllegati() {
	  	 return nMaxAllegati;
  }

  public void setNMaxAllegati(long maxAllegati) {
	  	 nMaxAllegati = maxAllegati;
  }  
  
  public String getCompetenzeAllegati() {
		 return competenzeAllegati;
  }

  public void setCompetenzeAllegati(String competenzeAllegati) {
		 this.competenzeAllegati = competenzeAllegati;
  }  

  public String toString() 
  {
         StringBuffer objectState= new StringBuffer();
         String sLibreria, sCampi;

         try {
           sLibreria = libreria.toString();           
         }
         catch (NullPointerException e) {
           sLibreria = "(nulla)";
         }

         try {
           sCampi = campi.toString();
         }
         catch (NullPointerException e) {
           sCampi = "(nulla)";
         }

         objectState.append("Classe: " + this.getClass().getName() + "\n");
         objectState.append("idTipoDoc= " + idTipodoc + "\n");
         objectState.append("nome= " + nome + "\n");
         objectState.append("<libreria> = " + sLibreria + "\n");
         objectState.append("<campi> = " + sCampi);
         
         return objectState.toString();
  }

  // ***************** DEFINIZIONE METODI ASTRATTI ***************** //
  public abstract void retrieve(boolean flagLibreria) throws Exception;


  
}
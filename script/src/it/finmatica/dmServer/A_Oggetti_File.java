package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEGLI OGGETTI FILE
 * DIPENDENZE CON: GD4_Oggetti_File
 *                 HummingBird_Oggetti_File
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public abstract class A_Oggetti_File implements I_Oggetti_File
{
  // Variabili private
  protected Environment varEnv;  
  protected String      idOggettoFile     = "0";
  protected String      idLog             = null;
  protected String      idFormato         = "0";
  
  //Gestione del replace
  protected String      oldFileName       = "";

  //
  protected String      fileName;
  protected Object      file;
  protected oracle.sql.BFILE bFile;
  
  //Gestione del replace 
  protected Object      oldFile;
  
  protected String      allegato;
  protected String      modificato;
  protected boolean     isVisible         = true;
  protected boolean     isFileFs          = false;
  protected String      icona             = "";  
  
  protected String      dacancellare      = "N";
  
  protected boolean     isOggettoFileTemp       = false;
  
  protected String      dataAggiornamento      ;
  
  protected String 		forzaPerMidMax    = "N";
  
  protected String percorsoFileFS =null;

  //protected boolean     fileStreamAllinizio=false;
  // ***************** METODI DI SET E GET ***************** //



  public String getPercorsoFileFS() {
	return percorsoFileFS;
	}
	
	public void setPercorsoFileFS(String percorsoFileFS) {
		this.percorsoFileFS = percorsoFileFS;
	}

public String getForzaPerMidMax() {
	     return forzaPerMidMax;
  }

  public void setForzaPerMidMax(String forzaPerMidMax) {
	     this.forzaPerMidMax = forzaPerMidMax;
  }

  public String getIdOggettoFile()
  {
         return idOggettoFile;
  }
   
  public void setIdOggettoFile(String newIdOggettoFile)
  {
         idOggettoFile = newIdOggettoFile;
  }

  public String getIdFormato()
  {
         return idFormato;
  }
   
  public void setIdFormato(String newIdFormato)
  {
         idFormato = newIdFormato;
  }
  
  /*public boolean isFileStreamAllinizio()
  {
         return fileStreamAllinizio;
  }*/
   
  /*public void setFileStreamAllinizio(boolean isLetto)
  {
	  fileStreamAllinizio = isLetto;
  }*/
  
  
  public void setVisible(String vis)
  {
         if (vis.toUpperCase().equals("S"))
             isVisible=true;
         else
             isVisible=false;
  }
  
  public void setOldFileName(String oldfn) {
	     oldFileName=oldfn;
  }

  public boolean getIsVisible()
  {
         return isVisible;
         
  }  
      
  public String getFileName()
  {
         return fileName;
  }

  public void setFileName(String newFileName)
  {
         fileName=newFileName;
  }

  public String getAllegato()
  {
         return allegato;
  }

  public void setAllegato(String newAllegato)
  {
         allegato=newAllegato;
  }

  public Object getFile() throws Exception // throws Exception
  { 	          
         return file;
  }
  
  public abstract Object getFile(boolean bCheck) throws Exception;

  public Object getOldFile() throws Exception 
  {
         return oldFile;
  }  
  
  public oracle.sql.BFILE getBFile() {
	  return bFile;
  }
  
  public void closeBFile() {
	  try{if (bFile!=null)  bFile.closeFile();}catch (Exception ei) {  } 
	  bFile=null;
  }

  public void setFile(Object newFile)
  {
         file=newFile;
  }
  
  public void setOldFile(Object oFile)
  {
         oldFile=oFile;
  }  

  public String getModificato()
  {
         return modificato;
  }
  
  public String getOldFileName() 
  {
	     return oldFileName;
  }

  public void setModificato(String newModificato)
  {
         modificato = newModificato;
  }

  public abstract void setIdOggettoFilePadre(String idOggettoPadre);
  
  public abstract String getIdOggettoFilePadre();
  
  public boolean isFileFs() {
		 return isFileFs;
  }

  public void setFileFs(boolean isFileFs) {
		 this.isFileFs = isFileFs;
  }  
  
  public String toString() 
  {
         StringBuffer objectState= new StringBuffer();

         objectState.append("Classe: " + this.getClass().getName() + "\n");
         objectState.append("idOggettoFile = " + idOggettoFile + "\n");
         objectState.append("fileName = " + fileName + "\n");
         objectState.append("allegato = " + allegato + "\n");
         objectState.append("file = " + file.toString() + "\n");
         
         return objectState.toString();
  }

  // ***************** DEFINIZIONE METODI ASTRATTI ***************** //
   
  public abstract void retrieve() throws Exception;
  public abstract boolean isVisible() throws Exception;
  public abstract boolean delete(Object sDirectory, String idDocumento) throws Exception;
  public abstract void inizializzaDati(Object vUtente) throws Exception;
  public abstract boolean update(Object idDocumento, A_Libreria aLibreria) throws Exception;
  public abstract boolean insert(Object idDocumento, A_Libreria aLibreria) throws Exception;

	public boolean isOggettoFileTemp() {
		return isOggettoFileTemp;
	}

	public void setOggettoFileTemp(boolean isOggettoFileTemp) {
		this.isOggettoFileTemp = isOggettoFileTemp;
	}

	public String getIcona() {
		return icona;
	}

	public void setIcona(String icona) {
		this.icona = icona;
	}

	public String getIdLog() {
		   return idLog;
	}

	public void setIdLog(String idLog) {
		   this.idLog = idLog;
	}

	public String getDacancellare() {
		return dacancellare;
	}

	public void setDacancellare(String dacancellare) {
		this.dacancellare = dacancellare;
	}

	public String getDataAggiornamento() {
		   return dataAggiornamento;
	}

	public void setDataAggiornamento(String dataAggiornamento) {
		   this.dataAggiornamento = dataAggiornamento;
	}
}
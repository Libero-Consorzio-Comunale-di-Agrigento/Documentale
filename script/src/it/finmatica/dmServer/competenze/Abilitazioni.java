package it.finmatica.dmServer.competenze;

public class Abilitazioni 
{
   private String tipoOggetto;
   private String oggetto    ;
   private String tipoAbilitazione;
   private String manageAbilitazione ; 
   private String accesso    ;  
   private String dataInizio ;
   private String dataFine   ; 


   private String ereditaTipoOggetto;
   private String ereditaOggetto    ;
 

  public Abilitazioni(String to,  String o, String ta )
  {
        this( to,   o,  ta, "0", "S", "to_char(sysdate,'dd/mm/yyyy')",  "NULL") ;
  }
  
 
    
  
  public Abilitazioni(String to,  String o, String ta , String ma)
  {
        this( to,   o,  ta, ma,  "S", "to_char(sysdate,'dd/mm/yyyy')",  "NULL") ;
  }
  
  public Abilitazioni(String to,  String o, String ta, String ac, String di, String df)
  {
      this( to,   o,  ta, "0",  ac, di,  df) ;
  }
  
  public Abilitazioni(String to,  String o, String ta, String ma, String ac, String di, String df)
  {
    tipoOggetto   = to;
    oggetto       = o ;
    tipoAbilitazione = ta; 
    manageAbilitazione = ma;
    accesso       = ac;
    if (di!=null)
      if (di.equals("to_char(sysdate,'dd/mm/yyyy')") || di.toLowerCase().equals("null"))
         dataInizio    = di;
      else
         dataInizio    = "'"+di+"'";
    else
       dataInizio="NULL";

    if (df!=null)
      if (df.toLowerCase().equals("null"))
         dataFine      = df;
      else
         dataFine      = "'"+df+"'";
    else
       dataFine = "NULL";

  }

  // -------------------------
  // Metodi di getter e setter
  // ------------------------- 
  public void setTipoOggetto(String to)
  {
         tipoOggetto = to;
  }

  public String getTipoOggetto()
  {
         return tipoOggetto;
  }

  public void setOggetto(String o)
  {
         oggetto = o;
  }

  public String getOggetto()
  {
         return oggetto;
  }

  public void setEreditaTipoOggetto(String to)
  {
         ereditaTipoOggetto = to;
  }

  public String getEreditaTipoOggetto()
  {
         return ereditaTipoOggetto;
  }

  public void setEreditaOggetto(String o)
  {
         ereditaOggetto = o;
  }

  public String getEreditaOggetto()
  {
         return ereditaOggetto;
  }

  public void setTipoAbilitazione(String ta)
  {
         tipoAbilitazione = ta;
  }

  public String getTipoAbilitazione()
  {
       return tipoAbilitazione;
  }
  
  public void setManageAbilitazione(String ma)
  {
         manageAbilitazione = ma;
  }

  public String getManageAbilitazione()
  {
       return manageAbilitazione;
  }
  
   public void setDataInizio(String da)
  {
         dataInizio = da;
  }

  public String getDataInizio()
  {
       return dataInizio;
  }
  
  public void setDataFine(String df)
  {
         dataFine = df;
  }

  public String getDataFine()
  {
       return dataFine;
  }

   public void setAccesso(String ac)
  {
         accesso = ac;
  }

  public String getAccesso()
  {
       return accesso;
  }
}
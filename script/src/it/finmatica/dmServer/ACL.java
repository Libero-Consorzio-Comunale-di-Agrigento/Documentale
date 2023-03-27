package it.finmatica.dmServer;

/*
 * GESTIONE ACL
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public class ACL 
{
  // Variabili private
  private String personGroup,mask,tipoCompetenza,nominativo;
  private String ruolo="GDM";
  private int accesso=0;
  
  public ACL(String newPersonGroup,String newMask)
  {
         mask=newMask;
         personGroup=newPersonGroup;
         ruolo="GDM";
  }
  
  public ACL(String newPersonGroup,String nomin,String tipoComp,int acc)
  {
	     tipoCompetenza=tipoComp;
         personGroup=newPersonGroup;
         ruolo="GDM";
         accesso=acc;
         nominativo=nomin;
  }  
  
  public ACL(String newPersonGroup,String newMask,String newRuolo)
  {
         mask=newMask;
         personGroup=newPersonGroup;
         ruolo=newRuolo;         
  }  

  // ***************** METODI DI SET E GET ***************** //

  public String getPersonGroup() 
  {
         return personGroup;
  }

  public String getMask() 
  {
         return mask;
  }

  public String getRuolo() 
  {
         return ruolo;
  }

  public String getTipoCompetenza() {
		 return tipoCompetenza;
  }

  public void setTipoCompetenza(String tipoCompetenza) {
	     this.tipoCompetenza = tipoCompetenza;
  }

  public int getAccesso() {
	     return accesso;
  }

  public void setAccesso(int accesso) {
	     this.accesso = accesso;
  }

  public String getNominativo() {
	     return nominativo;
  }

  public void setNominativo(String nominativo) {
	     this.nominativo = nominativo;
  }  
}
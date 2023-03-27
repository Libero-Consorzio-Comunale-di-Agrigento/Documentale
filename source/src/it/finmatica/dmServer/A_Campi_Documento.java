package it.finmatica.dmServer; 

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI CAMPI DOCUMENTO vv
 * DIPENDENZE CON: GD4_Campi_Documento
 *                 Humm_Campi_Documento
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 *  
 * */
    
public abstract class A_Campi_Documento implements I_Campi_Documento
{
   //Variabili private   
   private String idCampo="0";
   private String nomeCampo="0";

  // ***************** METODI DI SET E GET ***************** //
 
   public String getIdCampo()
   {
          return idCampo;
   }

   public void setIdCampo(String newIdCampo)
   {
          idCampo = newIdCampo;
   }
   
   public String getNomeCampo()
   {
          return nomeCampo;
   }
   
   public void setNomeCampo(String newNomeCampo)
   {
          nomeCampo = newNomeCampo;
   }

   public String toString() 
   {
          StringBuffer objectState = new StringBuffer();

          objectState.append("Classe: " + this.getClass().getName() + "\n");
          objectState.append("idCampo = " + idCampo + "\n");
          objectState.append("nomeCampo = " + nomeCampo);
         
          return objectState.toString();
   }

  // ***************** DEFINIZIONE METODI ASTRATTI ***************** //

    public abstract void retrieve() throws Exception;
   
}
package it.finmatica.dmServer;

import java.math.BigDecimal;
import it.finmatica.dmServer.util.FieldInformation;
/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI VALORI DEL DOCUMENTO
 * DIPENDENZE CON: GD4_Valori
 *                 Humm_Valori
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public abstract class A_Valori implements I_Valori
{
  protected Environment variabiliUtente;
  protected String idValore = "0";
  protected Object valore; 
  protected String modificato;
  protected String nome;
  protected boolean bUpdateOrizzontalTable=false;
  protected FieldInformation fi;

  protected BigDecimal valoreBD;

  // ***************** METODI DI SET E GET ***************** //

  public void setIdValore(String idValore) 
  {
         this.idValore=idValore;
  }

  public String getIdValore() 
  {
         return idValore;
  }
  
  public void setFieldInformation(FieldInformation fi) 
  {
         this.fi=fi;
  }

  public FieldInformation getFieldInformation() 
  {
         return fi;
  }   
  
  public void setUpdateOrizzontalTable(boolean bFlag) 
  {
         this.bUpdateOrizzontalTable=bFlag;
  }

  public boolean getUpdateOrizzontalTable() 
  {
         return this.bUpdateOrizzontalTable;
  }  
   
  public Object getValore()
  {
         return valore;
  }
   
  public void setValore(Object newValore)
  {
         valore = newValore;
  }

  public void setValoreBD(BigDecimal newValore)
  {
         valoreBD = newValore;
  }

  public BigDecimal getValoreBD()
  {
         return valoreBD;
  }
  
  public String getModificato()
  {
         return modificato;
  }

  public void setModificato(String newModificato)
  {
         modificato = newModificato;
  }

   public String getNome()
  {
         return nome;
  }

  public void setNome(String newNome)
  {
         nome = newNome;
  }
  
  public String toString() 
  {
         StringBuffer objectState= new StringBuffer();
         
         objectState.append("Classe: " + this.getClass().getName() + "\n");
         objectState.append("idValore = " + idValore + "\n");
         objectState.append("valore = " + valore.toString() + "\n");         
         
         return objectState.toString();
  }

 // ***************** DEFINIZIONE METODI ASTRATTI ***************** //
  public abstract A_Campi_Documento getCampo();
  public abstract void retrieve() throws Exception;
  public abstract boolean insert(Object idDocumento) throws Exception;
  public abstract boolean update(Object idDocumento) throws Exception;
  public abstract void inizializzaDati(Object vUtente) throws Exception;
 
}
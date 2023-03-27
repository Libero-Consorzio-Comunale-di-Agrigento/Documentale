package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI DOCUMENTI
 * DIPENDENZE CON: GD4_Documento_XML
 *
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
 
import java.util.*;  
   
public abstract class A_Documento_XML 
{
  //Variabili private 
  protected String idDocumento = null;
  protected Vector documenti = null;
 
  // ***************** METODI DI SET E GET ***************** //
   
   public void setIdDocumento(String id)
  {
          idDocumento = id;
  }
  
  public String getIdDocumento()
  {
         return idDocumento;
  }
  
  // ***************** DEFINIZIONE METODI ASTRATTI ***************** //
 
    public abstract String visualizza()throws Exception;
    public abstract Object getDocumentoXML()throws Exception;
}
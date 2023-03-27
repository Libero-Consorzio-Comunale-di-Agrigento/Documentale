package it.finmatica.dmServer.competenze;

import java.util.*;
import it.finmatica.textparser.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.management.AccediDocumento;

public class CompetenzeParser extends AbstractParser
{
  private String idDoc;
  private Environment en;
  private String error="@";

  public CompetenzeParser(String newIdDoc, Environment newEnv) 
  {
      idDoc =  newIdDoc;
      en = newEnv;  
  }

  protected String findParamValue(String nomePar, Properties extraKeys) 
  {
      if (nomePar.equals("UTENTE")) return en.getUser();

      try {
        en.byPassCompetenzeON();
        AccediDocumento ad = new AccediDocumento(idDoc,en);
      
        ad.accediDocumentoValori();

        return Global.replaceAll(ad.leggiValoreCampo(nomePar),"\n"," ");
      }
      catch(Exception e)
      {
        error = "Lettura Parametro " + nomePar + " " +e.getMessage();
        return "";
      }
  }

  public String getError() 
  {
      return error;
  }

}
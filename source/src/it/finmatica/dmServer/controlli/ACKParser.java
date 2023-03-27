package it.finmatica.dmServer.controlli;

import java.util.*;
import it.finmatica.textparser.*;
import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.*;

public class ACKParser extends AbstractParser
{
  private String idDoc;
  private Environment en;
  private String error="@";

  public ACKParser(String newIdDoc, Environment newEnv)
  {
       idDoc =  newIdDoc;
       en = newEnv;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) 
  {
  
      if (nomePar.equals("UTENTE")) return en.getUser();
      if (nomePar.equals("PASSWORD")) return en.getPwd();
      if (nomePar.equals("APPLICATIVO")) return en.getApplicativo();
      if (nomePar.equals("ENTE")) return en.getEnte();      
      if (nomePar.equals("INIFILE"))  return Global.replaceAll(en.getIniFile(),"\\\\","/");
      if (nomePar.equals("DOCNUMBER")) return idDoc;      

      if (nomePar.equals("TIPODOCNUMBER")) {
         try {
           en.connect();
           String tipoDoc=(new DocUtil(en)).getIdTipoDocByIdDocumento(idDoc);
           en.disconnectClose();
           return tipoDoc;            
         }
         catch(Exception e) 
         {
            error = "Lettura Parametro TIPODOCNUMBER: "+e.getMessage();
            return "";
         }
      }
            
      if (nomePar.equals("AREA")) {
         try {
           en.connect();
           String area=(new DocUtil(en)).getAreaByIdDocumento(idDoc);
           en.disconnectClose();
           return area;           
         }
         catch(Exception e) 
         {
            error = "Lettura Parametro AREA: "+e.getMessage();
            return "";
          }
      }

      if (nomePar.equals("CODICE_MODELLO")) {
         try {
           en.connect();
           String cm=(new DocUtil(en)).getModelloByIdDocumento(idDoc);
           en.disconnectClose();
           return cm;
         }
         catch(Exception e) 
         {
            error = "Lettura Parametro CODICE_MODELLO: "+e.getMessage();
            return "";
          }
      }

      if (nomePar.equals("CODICE_RICHIESTA")) {
         try {
           
           en.connect();
           String cr=(new DocUtil(en)).getCrByIdDocumento(idDoc);           
           en.disconnectClose();
           return cr;
         }
         catch(Exception e) 
         {
            error = "Lettura Parametro CODICE_RICHIESTA: "+e.getMessage();            
            return "";
          }
      }

      try {
        AccediDocumento ad = new AccediDocumento(idDoc,en);

        ad.accediDocumentoValori();
        
        if (ad.leggiValoreCampo(nomePar)==null)
        	return "";
        
        return  Global.replaceAll(ad.leggiValoreCampo(nomePar),"\n"," ");
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
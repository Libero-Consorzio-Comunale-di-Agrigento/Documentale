package it.finmatica.dmServer.controlli;

import it.finmatica.dmServer.management.Profilo;

public class TestControlli 
{
  public TestControlli()
  {
  }

    
  public String prova(String sUtente,String passwd,String appl,String ente,String sIni,String area, String codMod,String idDoc,String valcampo, String nomeCampo,String valoredaAggiungere) 
  {
         try {
             Profilo p = new Profilo(area,codMod);
             p.initVarEnv(sUtente,passwd,appl, ente, sIni);

             p.setDocNumber(idDoc);

             p.settaValore(nomeCampo,valcampo+" - ADD: "+valoredaAggiungere);

             if (p.salva().booleanValue())
                return "N° Documento aggiornato: " + p.getDocNumber();
             else {
                return p.getCleanError();  
             }
         }
         catch (Exception e) 
         {
             return e.getMessage();
         }
  }
}
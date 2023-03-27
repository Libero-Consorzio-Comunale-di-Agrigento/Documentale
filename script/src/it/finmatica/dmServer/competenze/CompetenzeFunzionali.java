package it.finmatica.dmServer.competenze;

public class CompetenzeFunzionali 
{
  public CompetenzeFunzionali()
  {
  }

  public static synchronized Boolean CompetenzeAggiuntive_DocumentoF1(String user,String sValori) 
  {
         CompetenzeValoriParser c = new CompetenzeValoriParser(sValori);

         try 
         {
            if (c.getValue("COGNOME").equals("HERBERT__")) return new Boolean(true);
         }
         catch (Exception e)
         {            
            return new Boolean(false);
         }
         
         return new Boolean(false);
  }

}
package it.finmatica.dmServer.competenze;

import java.util.*;

public class CompetenzeValoriParser 
{
  Vector vParametri;

  private static final String SEPARATOR = ",";
  private static final String EQUALS = "=";  

  public CompetenzeValoriParser(String sValori)
  {
         vParametri = new Vector();
         parse(sValori);
  }

  public String getValue(String par) throws Exception
  {
         for(int i=0;i<vParametri.size();i++) 
         {
            String sValue;

            sValue = ((Properties)vParametri.get(i)).getProperty(par);

            if (sValue!=null) return sValue;
         }
         throw new Exception("CompetenzeValoriParser::getValue("+par+") - Il parametro non esiste")
;
  }

  private void parse(String valori) 
  {
        Properties p = new Properties();

        //PASSO BASE
        if (valori.indexOf(SEPARATOR)==-1) 
        {           
           p.setProperty(valori.substring(0,valori.indexOf(EQUALS)),
                         valori.substring(valori.indexOf(EQUALS)+1,valori.length()));
           vParametri.add(p);
           return;
        }

        //PASSO INDUTTIVO
        p.setProperty(valori.substring(0,valori.indexOf(EQUALS)),
                      valori.substring(valori.indexOf(EQUALS)+1,valori.indexOf(SEPARATOR)));
        vParametri.add(p);

        parse(valori.substring(valori.indexOf(SEPARATOR)+1,valori.length()));
  }
}
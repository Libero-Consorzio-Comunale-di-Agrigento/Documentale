package it.finmatica.dmServer.workflow;

import java.io.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.dmServer.util.DocUtil;


public class GestioneDocumenti 
{
  private String area, codiceModello, codice_richiesta;
  AccediDocumento ad;
  
  public GestioneDocumenti(String ar, String cm, String cr, String iniPath) throws Exception
  {
      area = ar;
      codiceModello = cm;
      codice_richiesta = cr;
      inizializza(iniPath);
  }
  
  
  public ByteArrayInputStream getAllegato(int i) throws Exception
  {
        int k;
        try {
            InputStream is = ad.esimoOggettoFile(i);
            if (is != null)
            {
                    byte[] buf2 = new byte[4096];
                    ByteArrayOutputStream xOutputStream = new ByteArrayOutputStream(4096); 
                
                    while ( (k=is.read(buf2) ) != -1) 
                          xOutputStream.write(buf2,0,k);
                    
                    ByteArrayInputStream xInputStream = new ByteArrayInputStream(xOutputStream.toByteArray());        
                    return xInputStream;
            }
            else
                return null;
        } catch (Exception e) {
            throw new Exception("GestioneDocumenti::getAllegato\n" + e.getMessage());
        }
  }
   public String getNomeAllegato(int i) throws Exception
  {
        return ad.esimoNomeFile(i);
  }
  
  private void inizializza(String iniPath) throws Exception
  {
      String idDoc = "0";
      Environment env = null;
      try {
          env = new Environment(Global.UTEWKF, null, Global.APPLWKF, Global.ENTEWKF, null,  iniPath) ;
          idDoc = (new DocUtil(env)).getIdDocumento (area,  codiceModello, codice_richiesta); 
      }
      catch (Exception e) {
        throw new Exception("GestioneDocumenti::inizializza - " + e.getMessage());
      }    
      
      if (idDoc.equals("")) 
          throw new Exception("GestioneDocumenti::inizializza - idDoc non trovato per (area=" + area + ", codMod=" + codiceModello + ", codice_richiesta=" + codice_richiesta + ")\n");
            
      try{    
         ad = new AccediDocumento(idDoc,env);
         ad.accediDocumentoAllegati();
      } catch (Exception e) {
        throw new Exception("GestioneDocumenti::inizializza - AccediDocumento\n" + e.getMessage());
      }
  }
  
  
  
 
  
 /*public static  void main(String[] args) throws Exception
  {
      Environment vu = new Environment("GDM","GDM","MODULISTICA", "ADS", null, "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");                 
      GestioneDocumenti gd = new GestioneDocumenti("MANNY", "F1","GDCLIENT181", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");
      System.out.println(gd.getAllegato(2).toString());
  }*/
}
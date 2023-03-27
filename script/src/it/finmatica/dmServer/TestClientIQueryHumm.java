package it.finmatica.dmServer;

//import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.management.IQuery;

import java.util.*;

public class TestClientIQueryHumm 
{
  public static void main(String[] args) throws Exception 
  {
         String caso="1";
         //IQuery Iq = new IQuery("def_doc_generico", "AREAGDM");
         	IQuery Iq = new IQuery("DEF_PROTO_PANTAREI", "");

         Iq.initVarEnv("UT_PROTO_ADS","UT_PROTO_ADS","SEGRETERIA", 
                       "ADS_HUMM", "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm_humm.properties");     

         //Iq.settaChiave("DOCNUMBER","702");
         
         Iq.settaChiave("type_id","documento");
         //Iq.settaChiave("type_id","fattura");
         //Iq.settaChiave("stato_pantarei","2");
         Iq.settaChiave("stato_pantarei","4");
         //Iq.settaUtente("G.STANZA1",Global.IS_USER);
         //Iq.settaUtente("ut-proto",Global.IS_GROUP);

         try{
            if (Iq.ricerca().booleanValue()) {
              
               if (caso.equals("1")) {
                   Vector v = Iq.getProfili();

                   for(int i=0;i<v.size();i++) 
                   {
                      Profilo p = (Profilo)v.get(i);
                  

                      System.out.println("->"+p.getCampo("DOCNUM"));
                      System.out.println("->"+p.getCampo("DOCNAME"));
                      System.out.println("RIF->"+p.getRiferimenti());               
                    }

                   System.out.println("N. Totale->"+Iq.getProfileNumber());               
               }
               else if (caso.equals("2")) {
                  Profilo p = Iq.getProfileFromIndex(0);
                  if (!Iq.getError().equals("@")) 
                  {
                      System.out.println(Iq.getError());
                  }
                  else 
                  {
                      System.out.println("->"+p.getCampo("DOCNUM"));
                      System.out.println("->"+p.getCampo("DOCNAME"));
                      System.out.println("RIF->"+p.getRiferimenti());      
                  }
                  
               }
               else if (caso.equals("3")) {
                  Profilo p = Iq.getProfileFromDocNum("45");
                  if (!Iq.getError().equals("@")) 
                  {
                      System.out.println(Iq.getError());
                  }
                  else 
                  {
                      System.out.println("->"+p.getCampo("DOCNUM"));
                      System.out.println("->"+p.getCampo("DOCNAME"));
                      System.out.println("RIF->"+p.getRiferimenti());                       
                  }
                  
               }
               else if (caso.equals("4")) {    
                  String sCampo;

                  sCampo=Iq.getCampoFromIndex(0,"DOCNUM");
                  if (!Iq.getError().equals("@")) 
                  {
                      System.out.println(Iq.getError());
                  }
                  else 
                  {
                      System.out.println("->"+sCampo);
                     
                  }
                  
               }
                else if (caso.equals("5")) {    
                  String sCampo;

                  sCampo=Iq.getCampoFromDocNum("45","DOCNAME");
                  if (!Iq.getError().equals("@")) 
                  {
                      System.out.println(Iq.getError());
                  }
                  else 
                  {
                      System.out.println("->"+sCampo);
                     
                  }
                  
               }
               else if (caso.equals("6")) {    
                  String sRif;

                  sRif=Iq.getRiferimentiFromIndex(1);
                  if (!Iq.getError().equals("@")) 
                  {
                      System.out.println(Iq.getError());
                  }
                  else 
                  {
                      System.out.println("->"+sRif);
                     
                  }
                  
               }
                else if (caso.equals("7")) {    
                  String sRif;

                  sRif=Iq.getRiferimentiFromDocNum("55");
                  if (!Iq.getError().equals("@")) 
                  {
                      System.out.println(Iq.getError());
                  }
                  else 
                  {
                      System.out.println("->"+sRif);
                     
                  }
                  
               }
            }
            else {
                 System.out.println(Iq.getError());
            }
         }
         catch (Exception e) 
         {
              System.out.println(Iq.getError());
         }         
  }
}
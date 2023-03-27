package it.finmatica.dmServer.modulistica;

/*
 * CLASSE PERSONALIZZATA PER MODULISTICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   13/09/2005
 * 
 * */

import java.util.*;
import it.finmatica.dmServer.*;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.util.JNDIParameter;

public class AccessoModulistica 
{
  private Environment vu;
  private String idDoc;
  private String idTipoDoc;
  
  /*
   * METHOD:      Constructor
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Valorizza la variabile "idDoc" con il primo
   *              documento restituito dal motore di ricerca
   * 
   * RETURN:      none
  */
  public AccessoModulistica(String sUtente,String ruolo, 
                            String idTipoDoc,String sCodRichiesta, 
                            String iniDM) throws Exception
  {
         this.idTipoDoc = idTipoDoc;

         vu = new Environment(sUtente,sUtente,
                              "MODULISTICA", 
                              "MODULISTICA", 
                              null, iniDM);
         vu.setRuolo(ruolo);
   
         /*RicercaDocumento rd = new RicercaDocumento(idTipoDoc,"AREAGDM",vu);

         rd.settaCodiceRichiesta(sCodRichiesta);

         Vector l = rd.ricerca();

         if (l.size()==0) {
            vu.connect();
            l = rd.ricercaBozza();            
         }*/

         //if (l.size()==0) 
            idDoc="-1";
         /*else
            idDoc=(String)l.elementAt(0);*/
  }

    public AccessoModulistica(String sUtente,String ruolo,
        String idTipoDoc,String sCodRichiesta,
        Environment vu) throws Exception
    {
        this.vu = vu;

        this.idTipoDoc = idTipoDoc;
        idDoc="-1";
    }
  
  public AccessoModulistica(String sUtente,String ruolo, 
                            String idTipoDoc,String sCodRichiesta, 
                            JNDIParameter jndi) throws Exception
  {
         this.idTipoDoc = idTipoDoc;

         vu = new Environment(sUtente,sUtente,
                              "MODULISTICA", 
                              "MODULISTICA", 
                              null, jndi);
         vu.setRuolo(ruolo);
   
         /*RicercaDocumento rd = new RicercaDocumento(idTipoDoc,"AREAGDM",vu);

         rd.settaCodiceRichiesta(sCodRichiesta);

         Vector l = rd.ricerca();

         if (l.size()==0) {
            vu.connect();
            l = rd.ricercaBozza();            
         }*/

         //if (l.size()==0) 
            idDoc="-1";
         /*else
            idDoc=(String)l.elementAt(0);*/
  }  

  /*
   * METHOD:      String getMaxComp()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: RESTITUISCE LA COMPETENZA PIU' SIGNIFICATIVA PER IL DOCUMENTO "idDoc"
   * 
   * RETURN:      LA COMPETENZA PIU' SIGNIFICATIVA PER IL DOCUMENTO "idDoc"
  */
  public String getMaxComp() throws Exception
  {
         Abilitazioni abilitazione; 
         
         String sRet;

         //Se il documento non è stato trovato sul costruttore
         //viene restituita la competenza di Creazione sul
         //Tipo Documento se questa è presente
         if (idDoc.equals("-1")) {
            abilitazione = new Abilitazioni("TIPI_DOCUMENTO", idTipoDoc , "C"); 

            if ((new GDM_Competenze(vu)).verifica_Modulistica_Compentenza(abilitazione) == 1) 
               sRet= "C";
            else
               sRet= "-1";

         }
         //Se il documento è stato trovato, 
         //cerco la competenza più significativa
         else 
         {
            abilitazione = new Abilitazioni("DOCUMENTI", idDoc , "U"); 
            if ((new GDM_Competenze(vu)).verifica_Modulistica_Compentenza(abilitazione) == 1) 
               sRet= "U";
            else 
            { 
               //Test delle competenze funzionali su U
               /*try {
                   vu.connect();
                   CompetenzeControlli controlli = new CompetenzeControlli(idDoc,"U",vu);

                   if (controlli.execControlli()) {
                      vu.disconnectClose();
                      return "U";
                   }
                }
                catch (Exception e) 
                {
                     throw new Exception("getMaxComp - Errore in COMPETENZE FUNZIONALI: "+e.getMessage());
                }    */

               //Se non è passata la U neanche con le comp. funzionali
               //allora testo la L
               abilitazione = new Abilitazioni("DOCUMENTI", idDoc , "L"); 
               if ((new GDM_Competenze(vu)).verifica_Modulistica_Compentenza(abilitazione) == 1) 
                   sRet = "L";
               else {

                  //Test delle competenze funzionali su L
                   /*try {
                         vu.connect();
                         CompetenzeControlli controlli = new CompetenzeControlli(idDoc,"L",vu);

                         if (controlli.execControlli()) {
                            vu.disconnectClose();
                            return "L";
                         }
                   }
                   catch (Exception e) 
                   {
                           throw new Exception("getMaxComp - Errore in COMPETENZE FUNZIONALI: "+e.getMessage());
                   }   */
                   
                   sRet=  "-1";
               }
            }
               
         }
                  
         return sRet;
                 
  }

  public Environment getEnvironment() 
  {
         return vu;
  }
  
}
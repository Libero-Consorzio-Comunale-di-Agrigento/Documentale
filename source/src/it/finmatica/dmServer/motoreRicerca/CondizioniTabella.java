/***********************************************************************
 * Module:  Condizioni_Tabella.java
 * @author  Andrea Alì, Giuseppe Mannella
 * Purpose: Classe per la costruzione della frase di exists su una
 *          determinata tabella per un valore.
 *          esempio:
 *              select 1 
 *              from valori v 
 *              where valore_stringa = '45454445' and 
 *                    d.ID_DOCUMENTO = v.ID_DOCUMENTO
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// --------------
// Sezione Import
// --------------
import it.finmatica.jfc.utility.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.Environment;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class CondizioniTabella
{
   /* DEFINIZIONE DELLE COSTANTI */
   private final static String sCondT  = " and d.ID_DOCUMENTO = t.ID_DOCUMENTO ";
   private final static String sCondVDM = " and t.ID_CAMPO     = dm.ID_CAMPO ";
   private final static String sCondDMCL = " and cl.AREA      = dm.AREA and cl.DATO = dm.DATO";
   
   private Environment vu; 

  /* DEFINIZIONE DEI METODI PUBBLICI */
  
  public CondizioniTabella(Environment newVu) {         
         vu=newVu;
  }

  // ----------------------------
  // Costruzione where su tabelle
  // Valori o Oggetti_File
  // ----------------------------
  public String whereTabella(String aTabella, Object aValore) throws Exception
  {
         if (aTabella.equals(vu.Global.VALORI))
            return whereTabellaValori(aValore, "");
         else
            return whereTabellaOggettiFile(aValore);
  }

  // --------------------------
  // Costruzione where su campi
  // --------------------------
  public String whereCampiValori(String idCampo, Object aValore, String operatore, int index) throws Exception
  { 
      StringBuffer sSel = new StringBuffer();
      String sCol = null;
      String sOp = null;
      String sLook="",sFormCampo="";
      int i=-1;
      
      if (operatore.equals("M")) operatore=">";
      if (operatore.equals("m")) operatore="<";
      if (operatore.equals("MU")) operatore=">=";
      if (operatore.equals("mu")) operatore="<=";
      
      String alias = "t"+(index+1);

       try {
           sLook = (new LookUpDMTable(vu)).lookUpTipoCampo(idCampo);
           i = sLook.indexOf("@");
           if (i!=-1)
              sFormCampo = sLook.substring(0,i);
           else
              sFormCampo = sLook;
       }
       catch (Exception e)
       {
              throw new Exception("Condizioni_Tabella::whereCampiValori() errore nella lookup\n"+e.getMessage());
       }
             
        if (sFormCampo.equals("D")) {
                 if (aValore.equals("")) {
                      sCol = alias+".valore_data"; 
                      sOp=" is null ";
                 }
                 else {
                   sCol = alias+".valore_data";  
                   aValore = "to_date('" + aValore.toString() + "','"+sLook.substring(i+1)+"')";
                   if ((operatore == null ) || (operatore.equals("")) )
                      sOp= " = ";        
                   else 
                      sOp= operatore;
                 }
        }
        else if (sFormCampo.equals("N")) {
                   if (aValore.equals("")) {
                       sCol = alias+".valore_numero";
                       sOp=" is null ";
                   }
                   else {
                     Long.valueOf((String)aValore);
                     sCol = alias+".valore_numero";
                     
                     aValore = aValore.toString();
                 
                     if ((operatore == null ) || (operatore.equals("")) )
                        sOp= " = ";        
                     else 
                        sOp= operatore;
                   }                   
                   
        } else{
                   if (vu.Global.USE_INTERMEDIA.equals("S")) {
                      sCol = "contains("+alias+".valore_clob," ;
                      sOp = "'"+aValore.toString()+"')";
                      aValore = "> 0";
                   }
                   else {
                      if (aValore.toString().equals("")) {
                         sCol = " dbms_lob.getlength("+alias+".valore_clob)" ;
                         sOp = "";
                         aValore = "= 0"; 
                      }
                      else {
                        sCol = " dbms_lob.instr("+alias+".valore_clob," ;
                        sOp = "'"+aValore.toString()+"')";
                        aValore = "> 0";
                      }
                   }
           }
       
       /*sSel.append("(select 1 from "+ vu.Global.VALORI +" t, "+ vu.Global.CAMPI_DOCUMENTO + " c where ");*/
       sSel.append(sCol + sOp + aValore);
       /*sSel.append(" and c.id_campo = " + idCampo +  sCondT + sCondCV  + ")" );  */           

       return sSel.toString();
  }

  // --------------------------
  // Costruzione where su dati
  // --------------------------
  public String whereDatiValori(String sArea, String sDato, Object aValore, String operatore) throws Exception
  {
       StringBuffer sSel = new StringBuffer();
       String sCol = null;
       String sOp = null;
       String sLook="",sFormCampo="";
       int i=-1;

       try {
             sLook = (new LookUpDMTable(vu)).lookUpTipoDato( sArea,  sDato); //errore
             i = sLook.indexOf("@");
             sFormCampo = sLook.substring(0,i);
       }
       catch (Exception e)
       {
              throw new Exception("Condizioni_Tabella::whereDatiValori() errore nella lookup\n"+e.getMessage());
       }

                        
        if (sFormCampo.equals("D")) {
                  if (aValore.equals("")) {
                      sCol = "valore_data"; 
                      sOp=" is null ";
                   }
                   else {
                       sCol = "valore_data";  
                       aValore = "to_date('" + aValore.toString() + "','"+sLook.substring(i+1)+"')";
                       if ((operatore == null ) || (operatore.equals("")) )
                          sOp= " = ";        
                       else 
                          sOp= operatore;
                   }
        }
        else if (sFormCampo.equals("N")) {
                   if (aValore.equals("")) {
                       sCol = "valore_numero";
                       sOp=" is null ";
                   }
                   else {
                     
                       Long.valueOf((String)aValore);
                       sCol = "valore_numero";
                       aValore = aValore.toString();
                       if ((operatore == null ) || (operatore.equals("")) )
                          sOp= " = ";        
                       else 
                          sOp= operatore;
                  }                    
       }
       else{
                 if (vu.Global.USE_INTERMEDIA.equals("S")) {
                    sCol = "contains(valore_clob," ;
                    sOp = "'"+aValore.toString()+"')";
                    aValore = "> 0";
                 }
                 else {
                     if (aValore.toString().equals("")) {
                         sCol = " dbms_lob.getlength(valore_clob)" ;
                         sOp = "";
                         aValore = "= 0"; 
                      }
                      else {
                        sCol = " dbms_lob.instr(valore_clob," ;
                        sOp = "'"+aValore.toString()+"')";
                        aValore = "> 0";
                      }
                 }
            }

         sSel.append("(select 1 from "+ vu.Global.VALORI +" t,  " );
         sSel.append(                   vu.Global.DATI  + " dm ");
         sSel.append("  where " + sCol + sOp + aValore);
         sSel.append(   " and dm.area = '" + sArea + "'");
         sSel.append(   " and dm.dato = '" + sDato + "'");
         sSel.append( sCondT + sCondVDM  + ")" );

 //System.out.println("sSel.toString():"+sSel.toString());     

         return sSel.toString();
  }
  
  // --------------------------
  // Costruzione where su dati
  // --------------------------
  public String whereClassificazioniValori(String sClass, Object aValore, String operatore) throws Exception
  {
       StringBuffer sSel = new StringBuffer();
       String sCol = null;
       String sOp = null;    
       String sLook="",sFormCampo="";
       int i=-1;

       try {
             sLook = (new LookUpDMTable(vu)).lookUpClassificazione( sClass); //errore
             i = sLook.indexOf("@");
             sFormCampo = sLook.substring(0,i); 
       }
       catch (Exception e)
       {
              throw new Exception("Condizioni_Tabella::whereDatiValori() errore nella lookup\n"+e.getMessage());
       }
                        
        if (sFormCampo.equals("D")) {
                   if (aValore.equals("")) {
                      sCol = "valore_data"; 
                      sOp=" is null ";
                   }
                   else {
                       sCol = "valore_data";  
                       aValore = "to_date('" + aValore.toString() + "','"+sLook.substring(i+1)+"')";
                       if ((operatore == null ) || (operatore.equals("")) )
                          sOp= " = ";        
                       else 
                          sOp= operatore;
                   }
        }
        else if (sFormCampo.equals("N")) {
                   if (aValore.equals("")) {
                       sCol = "valore_numero";
                       sOp=" is null ";
                   }
                   else {
                     Long.valueOf((String)aValore);
                     sCol = "valore_numero";
                     aValore = aValore.toString();
                     if ((operatore == null ) || (operatore.equals("")) )
                        sOp= " = ";        
                     else 
                        sOp= operatore;
                   }
                    
                    
       }
       else{
                 if (vu.Global.USE_INTERMEDIA.equals("S")) {
                    sCol = "contains(valore_clob," ;
                    sOp = "'"+aValore.toString()+"')";
                    aValore = "> 0";
                 }
                 else {
                     if (aValore.toString().equals("")) {
                         sCol = " dbms_lob.getlength(valore_clob)" ;
                         sOp = "";
                         aValore = "= 0"; 
                      }
                      else {
                        sCol = " dbms_lob.instr(valore_clob," ;
                        sOp = "'"+aValore.toString()+"')";
                        aValore = "> 0";
                      }
                 }
            }

         sSel.append("(select 1 from "+ vu.Global.VALORI +" t,  " );
         sSel.append(                   vu.Global.DATI  + " dm, " + vu.Global.CLASSIFICAZIONI + " cl ");
         sSel.append("  where " + sCol + sOp + aValore);
         sSel.append(   " and cl.classificazione = '" + sClass + "'");
         sSel.append( sCondT + sCondVDM + sCondDMCL + ")" );

// System.out.println("sSel.toString():"+sSel.toString());     

         return sSel.toString();
  }
  /* DEFINIZIONE DEI METODI PRIVATI */

  // --------------------------------
  // Costruisce la condizione interna
  // select 1 from tabella where cond
  // sulla tabella valori
  // --------------------------------
  private String whereTabellaValori(Object aValore, String operatore)  throws Exception {

          String sSel = null;
          String sCol = null;
          String sOp = null;
         try {
              if (DateUtility.isDateValid((String)aValore,"dd/mm/yyyy")) {
                 if (aValore.equals("")) {
                      sCol = "valore_data"; 
                      sOp=" is null ";
                   }
                 else {
                   sCol = "valore_data";  
                   aValore = "to_date('" + aValore.toString() + "','dd/mm/yyyy')";
                   if ((operatore == null ) || (operatore.equals("")) )
                      sOp= " = ";        
                   else 
                      sOp= operatore;
                 }
              }
              else {
                 try {
                  if (aValore.equals("")) {
                       sCol = "valore_numero";
                       sOp=" is null ";
                   }
                   else {
                     Long.valueOf((String)aValore);
                     sCol = "valore_numero";
                     aValore = aValore.toString();
                     if ((operatore == null ) || (operatore.equals("")) )
                        sOp= " = ";        
                     else 
                        sOp= operatore;
                   }
                   
                 }
                 catch (NumberFormatException nfe) {
                    if (vu.Global.USE_INTERMEDIA.equals("S")) {
                        sCol = "contains(valore_clob," ;
                        sOp = "'"+aValore.toString()+"')";
                        aValore = "> 0";
                    }
                    else {
                      if (aValore.toString().equals("")) {
                         sCol = " dbms_lob.getlength(valore_clob)" ;
                         sOp = "";
                         aValore = "= 0"; 
                      }
                      else {
                        sCol = " dbms_lob.instr(valore_clob," ;
                        sOp = "'"+aValore.toString()+"')";
                        aValore = "> 0";
                      }
                    }
                 }
              }
                  
              sSel = "(select 1 from "+ vu.Global.VALORI +" t where " + sCol + sOp  + aValore + sCondT  + ")";
              return sSel;
         }
         catch (Exception e)
         {
              throw new Exception("Condizioni_Tabella::whereTabellaValori() \n"+e.getMessage());
         }
  }

  // --------------------------------
  // Costruisce la condizione interna
  // select 1 from tabella where cond
  // sulla tabella Oggetti_File
  // --------------------------------
   private String whereTabellaOggettiFile(Object aValore) 
   {
           String sSel1 = null, sSel2 = null,  sSel = null;
           String sCol1 = null, sCol2 = null;
           String sOpStr = null, sOpEnd  = null;

           sCol1 = "testoocr";
           sCol2 = "\"FILE\"";
           aValore = "'"+aValore.toString()+"'";
           sOpStr = " CONTAINS (";           
           sOpEnd = " ) > 0";                     

           sSel1 = "select 1 from "+ vu.Global.OGGETTI_FILE +" t where " + sOpStr + sCol1 + ", "+aValore + sOpEnd + sCondT;
           sSel2 = "select 1 from "+ vu.Global.OGGETTI_FILE +" t where " + sOpStr + sCol2 + ", "+aValore + sOpEnd + sCondT;

           sSel = "(" +sSel1 + " union all  "+ sSel2+")";

           return sSel;
   }
}
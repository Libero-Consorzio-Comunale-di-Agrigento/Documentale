package it.finmatica.dmServer.monoRecord;

/*
 * GESTIONE COMPLETA DEL MONORECORD
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   13/09/2005
 * 
 * */
 
import java.sql.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.modutils.informazionicampo.*;

import it.finmatica.dmServer.util.ManageConnection;

public class Monorecord 
{
  protected final static  String BEGIN_CAMPO      = "<a href=\"#taglayout\""; 
  protected final static  String BEGIN_FUNCT      = "<a href=\"#tagfunc\""; 
  protected final static  String END_CAMPO        = "</a>"; 
  protected final static  String BEGIN_NAME_CAMPO = "<!-- ADSPROPERTY"; 
  protected final static  String END_NAME_CAMPO   = "ADSFORMAT -->"; 
  protected final static  String CATEGORIE        = "<p><a href=\"#categorie\">--- Categorie ---</a></p>"; 
  protected final static  String BEGIN_CORPO      = "<p><a href=\"#inizio\">--- Inizio corpo ---</a></p>"; 
  protected final static  String END_CORPO        = "<p><a href=\"#fine\">--- Fine corpo ---</a></p>"; 
 
  private String categorie = ""; 
  private String corpoHtml = "";
  private String docNumber;
  private String tipoOggetto;
  private String nomeOggetto;
  private String alias_view;
  private Environment en;
  private IDbOperationSQL dbOp;
  
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Legge il blocco relativo al documento
   *              passato in input
   * 
   * RETURN:      none
  */
  public Monorecord(String newDocNumber,String newtipoOggetto,String newnome, Environment newEn) throws Exception
  {
         en = newEn;
         docNumber = newDocNumber;
         tipoOggetto=newtipoOggetto;
         nomeOggetto=newnome;

         String sBloccoHTML = "";
        
         String  query;
                  
         query="SELECT D.AREA, M.CODICE_MODELLO, 'GDM_'||TD.ALIAS_VIEW, B.CORPO,B.BLOCCO ";
         query+="FROM DOCUMENTI D,";
	     query+="TIPI_DOCUMENTO TD,";
         query+="MODELLI M,";
         query+="BLOCCHI B ";
         query+="WHERE D.ID_DOCUMENTO = :P_DOCNUM AND ";
         query+="D.ID_TIPODOC = TD.ID_TIPODOC AND ";
         query+="D.ID_TIPODOC = M.ID_TIPODOC AND ";
         query+="D.AREA = M.AREA AND ";
         query+="M.BLOCCO_JDMS=B.BLOCCO ";
     	 /*query+="B.AREA = M.AREA AND ";
		 query+="B.CODICE_MODELLO = M.CODICE_MODELLO"*/;

         try {
            dbOp = connect();

            dbOp.setStatement(query);
            dbOp.setParameter(":P_DOCNUM",docNumber);
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            if (rst.next()) {   
               //area=rst.getString(1);
               //cm=rst.getString(2);
               alias_view=rst.getString(3);
                
               //Lettura del corpo del blocco
               try {
                 sBloccoHTML=Global.leggiClob(dbOp,"CORPO");
               }
               catch (Exception e) {                                
                 throw new Exception("Monorecord::costructor() Errore in lettura Blocco");
               }

               //Il blocco non può essere vuoto
               if (sBloccoHTML==null) {                 
                  throw new Exception("Monorecord::costructor() Blocco non trovato");
               }
              
            }
            else {
               sBloccoHTML="";
               alias_view="GDM_";
            }
            
            close();
           
         }
         catch (Exception e)
         {
           close();
           throw new Exception("Monorecord::costructor() "+e.getMessage());
         }
                 
         setCorpo(sBloccoHTML);  
        
  }

  /*
   * METHOD:      creaRiga
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce il codice HTML
   *              interpretazione del blocco
   *              del documento passato in input
   * 
   * RETURN:      String
  */
  public String creaRiga() 
  {
         //Se la vista del modello non esiste
         //restituisco una label standard
         if (alias_view.equals("GDM_")) 
         {
        	if(tipoOggetto.equals("D"))
        	  return "Documento n. "+docNumber;
        	else
              return nomeOggetto;		 
         }
    
         String retval = "";
         String pCampo = "";
         String nomeCampo = "";
         String stileCampo = "";
         String tipoCampo = "";
         String valoreCampo = "";
         String valoreDato = "";
         int i,j,inizioCampo,fineCampo = 0;
         String stringaDaElaborare = Global.replaceAll(corpoHtml,BEGIN_FUNCT,BEGIN_CAMPO);
         Properties extraKeys = new Properties();

         stringaDaElaborare = Global.replaceAll(stringaDaElaborare,BEGIN_CORPO,"");
         stringaDaElaborare = Global.replaceAll(stringaDaElaborare,END_CORPO,"");
         i = stringaDaElaborare.indexOf(BEGIN_CAMPO);
         j = stringaDaElaborare.indexOf(END_CAMPO,i) + END_CAMPO.length();
         inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO);
         fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
         while(inizioCampo > -1) { 

              retval += stringaDaElaborare.substring(0,i);
              try {
                //Creo la stringa per il campo
                pCampo = stringaDaElaborare.substring(inizioCampo,fineCampo);
                InformazioniCampo infoCampo = new InformazioniCampo(pCampo,END_NAME_CAMPO);
                nomeCampo =infoCampo.getDato();
                stileCampo =infoCampo.getStile();
                tipoCampo = infoCampo.getTipoCampo();
                valoreDato = leggiValore(nomeCampo);
                if (stileCampo.equalsIgnoreCase("")) 
                {
                  valoreCampo =  valoreDato+"\n";
                } else {
                  valoreCampo =  "<FONT style='"+stileCampo+"' >"+valoreDato+"</FONT>\n";
                }

                if (tipoCampo.equalsIgnoreCase("K")) {
                  extraKeys.put(nomeCampo,valoreDato);
                  valoreCampo = "<INPUT TYPE='HIDDEN' NAME='"+nomeCampo+"' VALUE='"+valoreDato+"'>\n";
                }
        
                if (tipoCampo.equalsIgnoreCase("L")) {
                  retval += "<A HREF='"+infoCampo.getHref()+"' target='_blank' >"+valoreCampo+"</A>\n";
                } else {
                  retval += valoreCampo;
                }
              } catch (Exception e) {
                nomeCampo = "<!-- Errore "+pCampo+" -->\n";
              }
              stringaDaElaborare = stringaDaElaborare.substring(j);
              i = stringaDaElaborare.indexOf(BEGIN_CAMPO);
              j = stringaDaElaborare.indexOf(END_CAMPO,i) + END_CAMPO.length();
              inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO);
              fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
         }
         retval += stringaDaElaborare;
  
         return retval;
  }

  /*
   * METHOD:      setCorpo(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Parsing dei tag speciali ADS del blocco
   * 
   * RETURN:      void
  */
  private void setCorpo(String pCorpo) throws Exception 
  {

    int x = pCorpo.indexOf(CATEGORIE);
    int i = pCorpo.indexOf(BEGIN_CORPO);
    int j = pCorpo.indexOf(END_CORPO);

    try {
      if (x > -1) {
        //headerHtml = pCorpo.substring(0,x);
        if (i > -1) {
          categorie = pCorpo.substring(x+CATEGORIE.length(),i);
          if (categorie == null) {
            categorie = "";
          }
        } else {
          i = x + CATEGORIE.length();
        }
      } else {
        categorie = "";
        if (i > -1) { 
          //headerHtml = pCorpo.substring(0,i);
        } else {
          //headerHtml = "";
          i = 0;
        }
      }

      if ( j > -1) {
        j = j + END_CORPO.length();
        //footerHtml = pCorpo.substring(j);
      } else {
        j = pCorpo.length();
        //footerHtml = "";
      }
      corpoHtml = pCorpo.substring(i,j);
    } catch (Exception e) {
      throw new Exception ("Errore setCorpo! "+e.toString());
    }
  }

  /*
   * METHOD:      leggiValore(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Sostituzione del valore per "nomeCampo"
   *              utilizzando la vista modello
   * 
   * RETURN:      String
  */
  private String leggiValore(String nomeCampo) 
  {        
        String  query, sRet = null;

        query  = "SELECT nvl(" + nomeCampo+",' ')";
        query += " FROM " + alias_view;
        query += " WHERE DOCUMENTO = " + docNumber;

        try {           

            dbOp = connect();

            dbOp.setStatement(query);
            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            if (rst.next()) sRet=rst.getString(1);
            else sRet = "<!errore lettura campo!>";
              
            close();          
            return sRet;
         }
         catch (Exception e)
         {  
           try{close();}catch (Exception exp){}
           return "<!errore lettura campo!>";
         }        
       
  }

  private IDbOperationSQL connect() throws Exception {
         if (en.getDbOp()==null) 
            return (new ManageConnection(en.Global)).connectToDB();        
        
         return en.getDbOp();
  }
  
  private void close() throws Exception {
         if (en.getDbOp()==null) (new ManageConnection(en.Global)).disconnectFromDB(dbOp,true,false);
  }
  
 
}
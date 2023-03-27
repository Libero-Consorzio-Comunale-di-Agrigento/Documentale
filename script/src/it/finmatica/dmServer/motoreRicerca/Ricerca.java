/***********************************************************************
 * Module:  Ricerca.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione della ricerca                     
 *          a livello molto alto
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

import java.util.*;
import java.sql.*;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.GD4_Documento_XML;
import it.finmatica.dmServer.util.ManageConnection;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class Ricerca extends Thread
{
  private Vector Criteri;
  private Vector documentList;
  private ResultSet resDoc;
  private String error=null;
  
  private Environment vu;
  private IDbOperationSQL dbOp;
  private boolean bIsNew=false;  
  
  String verificaCompetenze = null;
      
  public Ricerca(Environment newVu)
  { 
         documentList = new Vector();
  }

  public Ricerca(A_Condizioni conds, Environment newVu) throws Exception
  {
         Criteri = new Vector();
         documentList = new Vector();
         
         vu = newVu;
         
         try {
             this.setCondizioniCriteri(conds);
         }
         catch (Exception e) {
            throw new Exception("Ricerca::Costructor() \n"+e.getMessage());
         }
  }

  private void setCondizioniCriteri(A_Condizioni conds) throws Exception
  {
        A_Criterio criterio = null;
        try {
            //AGGIUNGO LE CONDIZIONI GENERICHE TESTUALI
        
   
            for(int i=0;i<Global.MAX_COND;i++){
               if (conds.getCondizione(i)!=null) {
                    switch (i) 
                    {
                      case Global.COND_AND: criterio=creaCriterio(conds, "_And"); break;
                      case Global.COND_OR: criterio=creaCriterio(conds, "_Or"); break;
                      case Global.COND_SINGLE: criterio=creaCriterio(conds, "_Singolo"); break;
                      case Global.COND_NOT: criterio=creaCriterio(conds, "_Not"); break;
                      case Global.COND_TIMEOUT: criterio=null; break;
                    }        
                    
                    if (criterio!=null) Criteri.add(criterio);
                 }
             }  
             
             // AGGIUNGO LE CONDIZIONI SUI CAMPI-VALORI
             if (conds.getListaCondizioniCampi().size() > 0){
               criterio=creaCriterio(conds, "_Campi");
               Criteri.add(criterio);
             }
             
             // AGGIUNGO LE CONDIZIONI SUI DATI-CAMPI-VALORI
             if (conds.getListaCondizioniDati().size() > 0){
               criterio=creaCriterio(conds, "_Dati");
               Criteri.add(criterio);
             }
             
             // AGGIUNGO LE CONDIZIONI SUL DOCUMENTO
             if ((conds.getIdTipoDoc()!=null) ||
                 (conds.getArea()!=null) ||
                 (conds.getRichiesta()!=null) ) {
     
                criterio=creaCriterio(conds, "_Documento");

                Criteri.add(criterio);
             }

             // AGGIUNGO LE CONDIZIONI SUI CAMPI NON BLOB O BFILE DEGLI OGGETTI FILE
             if (conds.getFileName()!=null) 
             {
                 criterio=creaCriterio(conds, "_OggettiFile");

                Criteri.add(criterio);
             }
        }     
        catch (Exception e) 
        {
            throw new Exception("Ricerca::setCondizioniCriteri() \n"+e.getMessage());
        }     
  }

  public String getQuery() throws Exception
  {
         StringBuffer sSelect = new StringBuffer();
         String sUSENL="/*+ use_nl(t1) */";
         
         if (Criteri.size()==1) {
        	 sUSENL="";
         }
         
         sSelect.append("Select "+sUSENL+" d.id_documento from documenti d ");                 
         
         //Ciclo per costruire la form dinamica
         //Per adesso solo per i campi
         for(int i=0;i<Criteri.size();i++) {
        	
             if  (Criteri.elementAt(i) instanceof GD4_Criterio_Campi) {
            
                  int size=((A_Criterio)Criteri.elementAt(i)).getCondizioni().getListaCondizioniCampi().size();                 
                  for (int j=0;j<size;j++) {                  	  
                      sSelect.append(",valori t"+(j+1));
                      sSelect.append(",campi_documento c"+(j+1));
                  }
             }
         }
         
         sSelect.append(" where 1=1 ");
         
         for(int i=0;i<Criteri.size();i++) {
                                       
             A_Criterio criterio;
             
            // if (!(Criteri.elementAt(i) instanceof GD4_Criterio_Campi)) sSelect.append(" and ");
                                   
             criterio=(A_Criterio)Criteri.elementAt(i);

             try {            	  
                  sSelect.append(criterio.montaCriterio());
             }
             catch (Exception e)
             {
                throw new Exception("Ricerca::getQuery()\n"+e.getMessage());
             }

             if (i+1!=Criteri.size()) sSelect.append(" and ");
         }

       //  sSelect.append(")");        
         sSelect.append(" and STATO_DOCUMENTO not in (");
         sSelect.append("'"+Global.STATO_CANCELLATO+"',");
         sSelect.append("'"+Global.STATO_REVISIONATO+"')");
         //sSelect.append("   and id_stato = (select max(id_stato)  ");
        // sSelect.append("   from stati_documento s where s.id_documento = d.id_documento) ");

         //Se l'abilitazione cercata è quella per un documento, questa viebe controllata dopo
         //sulla lista dei documenti lanciando la verificaGDM perché questa passa anche
         //dalle competenze funzionali
      //   if (/*!(abil.getTipoOggetto().equals(Global.ABIL_DOC)) &&*/ verificaCompetenze != null)
            sSelect.append(" and " + verificaCompetenze +" = 1");
    
         return sSelect.toString();
  }

  public String getQueryStato(String stato) throws Exception
  {
         StringBuffer sSelect = new StringBuffer();

         sSelect.append("Select d.id_documento from documenti d where ( 1=1 ");

         for(int i=0;i<Criteri.size();i++) {
             A_Criterio criterio;
             criterio=(A_Criterio)Criteri.elementAt(i);

             try {
                sSelect.append(criterio.montaCriterio());
             }
             catch (Exception e)
             {
                throw new Exception("Ricerca::getQueryStato()\n"+e.getMessage());
             }
 
             if (i+1!=Criteri.size()) sSelect.append(") and (  ");
         }
         sSelect.append(")");
         //sSelect.append(" and sd.id_documento = d.id_documento ");
         if ( (stato != null) && (!stato.equals("")) ) {
            sSelect.append("  and STATO_DOCUMENTO in (");
            sSelect.append("'"+stato+"')");
           // sSelect.append("  and id_stato = (select max(id_stato)  ");
           // sSelect.append("          from stati_documento s where s.id_documento = d.id_documento) ");
         }

         //Se l'abilitazione cercata è quella per un documento, questa viebe controllata dopo
         //sulla lista dei documenti lanciando la verificaGDM perché questa passa anche
         //dalle competenze funzionali
         if ( /*!(abil.getTipoOggetto().equals(Global.ABIL_DOC)) &&*/ verificaCompetenze != null)
            sSelect.append(" and " + verificaCompetenze +" = 1");

         return sSelect.toString();
  }

  public void  setVerificaCompetenze(UtenteAbilitazione uteAbil, Abilitazioni abil ) throws Exception
  {
     verificaCompetenze = getQueryVerifica(uteAbil, abil);
  }
  
  private A_Criterio creaCriterio(A_Condizioni conds, String sClasse) throws Exception
  {
          try {
            A_Criterio criterio;

            criterio=(A_Criterio)Class.forName(vu.Global.PACKAGE + ".motoreRicerca." + vu.Global.DM + "_" +
                                               vu.Global.CRITERIO + sClasse).newInstance();

            criterio.setCondizioni(conds);
            criterio.setEnvironment(vu);

            return criterio;
          }
          catch (Exception e)             
          { 
            throw new Exception("Ricerca::creaCriterio() Impossibile creare l'oggetto: "+
                                vu.Global.PACKAGE + ".motoreRicerca." + vu.Global.DM + "_" +
                                vu.Global.CRITERIO + sClasse);
          }

  }
  
  //This method is called when the thread runs
  public void run()  {
	  	 try {
	   		fillDocumentList();
	   		vu.Global.bExitThreadRicerca=true;
	  	 }
         catch (Exception e) {
        	 vu.Global.bExitThreadRicerca=true;
        	 error=e.getMessage();            
         }
  }

  public void fillDocumentList() throws Exception
  {
          documentList.clear();
          
          StringBuffer sStm = new StringBuffer(this.getQuery());
          
          try {
           
           IDbOperationSQL dbOp = connect();
           
           dbOp.setStatement(sStm.toString());
        //  System.out.println("A PATATA-->"+sStm.toString());
           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();
          
           while (rst.next()) {        	   
        	   	documentList.add(rst.getString(1));          
           }           
           
           close();
         }
         catch (Exception e) {
               close();
               vu.Global.bExitThreadRicerca=true;
               throw new Exception("Ricerca::fillDocumentList() SQL: " + sStm.toString() + " - " + e.getMessage());
         }
  }

  /* existsDocument 
   * Se il documento è unico torna l'ID documento+/
   * se non esiste torna 0
   * altrimenti se ce ne sono + di uno torna -1 */
  public String existsDocument() throws Exception
  {
      fillDocumentList();
      int s = getDocumentList().size();
      if (s == 1)
          return getDocumentList().elementAt(1).toString();
      else if (s == 0)
          return "0";
      else
          return "-1";
  }
  
  public void fillDocumentListStato(String stato) throws Exception
  {
          documentList.clear();

          try {
           StringBuffer sStm = new StringBuffer(this.getQueryStato(stato));
           IDbOperationSQL dbOp = connect();
           
           dbOp.setStatement(sStm.toString());

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           while (rst.next()) {
        	   	 documentList.add(rst.getString(1));          
           }      
           
           close();
                  
         }
         catch (Exception e) {
               close();
               throw new Exception("Ricerca::fillDocumentListStato() " + e.getMessage());
         }
  }

  public Vector getDocumentList() 
  {
          return documentList;
  }
 
  public void resetDocumentList() 
  {
         documentList.removeAllElements();
  }
  
  public ResultSet getRstDoc() 
  {
          return resDoc;
  }
  
  public String getError() {
	  	 return error;
  }
  
  public Object getXMLDocument() throws Exception
  {
          if (getDocumentList().size() == 0)
              fillDocumentList();
          GD4_Documento_XML gdx = new GD4_Documento_XML(getDocumentList(), this.getQuery(),vu);
          return gdx.getDocumentoXML();
  }
  
  public String getXMLString() throws Exception
  {
          if (getDocumentList().size() == 0)
              fillDocumentList();
          GD4_Documento_XML gdx = new GD4_Documento_XML(getDocumentList(), this.getQuery(),vu);
          return gdx.visualizza();          
  }
  
   private String getQueryVerifica(UtenteAbilitazione ua, Abilitazioni abil) throws Exception
  {
           if (ua.getRuolo() == null)
              ua.setRuolo(Global.RUOLO_GDM);
           String sQuey = " GDM_COMPETENZA.gdm_verifica(";
                  sQuey += "'"+abil.getTipoOggetto()+"'";
               if (Global.isNumeric(abil.getOggetto()))
                  sQuey += ",'"+abil.getOggetto()+"'";  
               else if (abil.getTipoOggetto().equals(Global.ABIL_DOC))
                  sQuey += ",D.ID_DOCUMENTO";  //OCIO  il valore deve essere ID_DOCUMENTO
               else if (abil.getTipoOggetto().equals(Global.ABIL_TIPIDOC))
                  sQuey += ",D.ID_TIPODOC";  //OCIO  il valore deve essere ID_TIPODOC                  
                  sQuey += ",'"+abil.getTipoAbilitazione()+"'" ;
                  sQuey += ",'"+ua.getUtente()+"'";
                  sQuey +=",'"+ ua.getRuolo()+"'";
                  sQuey +=",to_char(sysdate,'dd/mm/yyyy') )";
           return sQuey;
  }
      

  private IDbOperationSQL connect() throws Exception {
        if (vu.getDbOp()==null) {
           bIsNew=true;
           return (new ManageConnection(vu.Global)).connectToDB();
        }
        
        return vu.getDbOp();
  }
  
  private void close() throws Exception {
        if (bIsNew) (new ManageConnection(vu.Global)).disconnectFromDB(dbOp,true,false);        
  }
      
      
}
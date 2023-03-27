package it.finmatica.dmServer;

/*
 * GESTIONE ACL
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.util.Vector;

public class Acl_Nominali 
{
  // Variabili private
  private String codiceEnte;
  private Vector vAclUte;

  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza il Codice Ente 
   *              e il vettore ACL utente
   * 
   * RETURN:      none
  */
  public Acl_Nominali(String ente)
  {
        codiceEnte = ente;
        vAclUte = new Vector();
  }

  /*
   * METHOD:      aggiungiCompetenze(String, String, String, String)
   *
   * DESCRIPTION: Aggiunge le competenze al soggetto
   *              relativo al' id Documento
   *              
   * RETURN:      void
  */
  public void aggiungiCompetenze(String soggetto,String documento,String NuovaVecchia,String valore) 
  {
         Acl_Utente aclUte = null;
         
         for (int i=0; i<vAclUte.size(); i++)
              if ( ((Acl_Utente)vAclUte.elementAt(i)).getUtente().equals(soggetto) ){
                  aclUte = ((Acl_Utente)vAclUte.elementAt(i));
                  break;
              } 
         if (aclUte == null)    
         {
            aclUte = new Acl_Utente(soggetto);
            aclUte.settaCompetenze(documento, NuovaVecchia, valore);
            vAclUte.addElement(aclUte);
         }
         else
            aclUte.settaCompetenze(documento, NuovaVecchia, valore);
  }


  public String generaXML()
  {
        String sFileXML;

         sFileXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         sFileXML = sFileXML + "<security_change codice_ente=\""+codiceEnte+"\">";
         for (int i=0; i<vAclUte.size(); i++)
             sFileXML = sFileXML + competenzeUtente(((Acl_Utente)vAclUte.elementAt(i)));

         sFileXML = sFileXML + "</security_change>";

         return sFileXML;
  }

  private String competenzeUtente(Acl_Utente utente)
  {
        String compUser;
        compUser = "<user name=\""+utente.getUtente()+"\">";
      
        compUser = compUser + oldCompDoc( utente.getAclCompetenze() );
        compUser = compUser + newCompDoc( utente.getAclCompetenze() );

        compUser = compUser + "</user>";   
        return compUser;
  }

  private String oldCompDoc(Vector aclComp)
  {
        /*String oldComp;
        oldComp = "<old_doc>";
        for (int i=0; i<aclComp.size(); i++)
            if ( ((Acl_Competenze)aclComp.elementAt(i)).getCompetenza().equals("O"))
               oldComp = oldComp + "<item docnumber=\""+ ((Acl_Competenze)aclComp.elementAt(i)).getDocId()
                         +"\" mask=\""+((Acl_Competenze)aclComp.elementAt(i)).getMask() + "\" />";  
        oldComp = oldComp + "</old_doc>";   
        return oldComp;*/
        return "";
  }

  private String newCompDoc(Vector aclComp)
  {
        String newComp;
        newComp = "<new_doc>";
        for (int i=0; i<aclComp.size(); i++)
             if ( ((Acl_Competenze)aclComp.elementAt(i)).getCompetenza().equals("N"))
                newComp = newComp + "<item docnumber=\""+ ((Acl_Competenze)aclComp.elementAt(i)).getDocId()
                          +"\" mask=\""+((Acl_Competenze)aclComp.elementAt(i)).getMask() + "\" />";  

        newComp = newComp + "</new_doc>";     
        return newComp;
  }

  private class Acl_Utente
  {
      private String utente;
      private Vector  vAclComp;
  
      private Acl_Utente(String soggetto)
      {
            utente = soggetto;
            vAclComp = new Vector();
      }
  
      private void settaCompetenze( String documento, String NuovaVecchia, String mask)
      {
            Acl_Competenze aclComp = new Acl_Competenze( NuovaVecchia, documento,  mask);
            vAclComp.addElement(aclComp);
      }

      private String getUtente( )
      {
            return utente;
      }
      
      private Vector getAclCompetenze( )
      {
            return vAclComp;
      }
  }

  private class Acl_Competenze
  {
      private String competenza,  docId,   mask;

      private Acl_Competenze( String c, String d,  String m)
      {
            competenza = c;
            docId      = d;
            mask       = m;
        
      }
      private String getDocId() 
      {
             return docId;
      }
  
      private String getCompetenza() 
      {
             return competenza;
      } 
  
      private String getMask() 
      {
             return mask;
      }

  }
}
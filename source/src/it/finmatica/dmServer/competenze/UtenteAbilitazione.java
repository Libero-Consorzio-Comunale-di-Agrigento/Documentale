package it.finmatica.dmServer.competenze;

import it.finmatica.dmServer.*;

public class UtenteAbilitazione 
{
  private String utente;
  private String ruolo;
  private String gruppo;
  private String pwd;
  private String autore;

  private Environment ev;

  public UtenteAbilitazione(String newUtente, String newGruppo, String newRuolo, String newPwd )
  {
    utente = newUtente;
    gruppo = newGruppo;
    ruolo =  newRuolo;
    pwd    = newPwd;
    autore = newUtente;
  }
  
  public UtenteAbilitazione(String newUtente, String newGruppo, String newRuolo, String newPwd, String newAutore )
  {
    utente = newUtente;
    gruppo = newGruppo;
    ruolo =  newRuolo;
    pwd    = newPwd;
    autore = newAutore;
  }

   public UtenteAbilitazione(String newUtente, String newGruppo, String newRuolo, String newPwd, String newAutore, Environment newEv )
  {
    utente = newUtente;
    gruppo = newGruppo;
    ruolo =  newRuolo;
    pwd    = newPwd;
    autore = newAutore;
    ev = newEv;
  }

  public void setUtente(String newUtente)
  {
        utente = newUtente;
  }
  
  public String getUtente()
  {
         return utente;
  }

  public Environment getEnvironment()
  {
         return ev;
  }
  
  public String getAutore()
  {
         return autore;
  }
  
  
  public String getRuolo()
  {
         return ruolo;
  }

  public String getGruppo()
  {
         return gruppo;
  }

  public String getPwd()
  {
         return pwd;
  }
  public void setRuolo(String newRuolo)
  {
         ruolo  = newRuolo;
  }

}
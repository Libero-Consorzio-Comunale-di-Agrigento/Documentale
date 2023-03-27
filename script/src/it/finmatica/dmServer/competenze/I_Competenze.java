package it.finmatica.dmServer.competenze;

public interface I_Competenze 
{
    //la ManageDocumento la implementa.. creata per forzare la verifica 
    long verificaCompentenza(Object utente, Object abilitazione) throws Exception ;
    void assegnaCompentenza(Object utente, Object newUtente, Object abilitazione) throws Exception ;
}
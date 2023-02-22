ALTER TABLE SI4_COMPETENZE ADD (
  CONSTRAINT SI4_COMP_ABIL_FK 
  FOREIGN KEY (ID_ABILITAZIONE) 
  REFERENCES SI4_ABILITAZIONI (ID_ABILITAZIONE)
  ENABLE VALIDATE);

ALTER TABLE SI4_COMPETENZE ADD (
  CONSTRAINT SI4_COMP_FUNZ_FK 
  FOREIGN KEY (ID_FUNZIONE) 
  REFERENCES SI4_FUNZIONI (ID_FUNZIONE)
  ENABLE VALIDATE);

ALTER TABLE SI4_COMPETENZE ADD (
  CONSTRAINT SI4_COMP_UTEN_FK 
  FOREIGN KEY (UTENTE) 
  REFERENCES AD4.UTENTI (UTENTE)
  ENABLE VALIDATE);


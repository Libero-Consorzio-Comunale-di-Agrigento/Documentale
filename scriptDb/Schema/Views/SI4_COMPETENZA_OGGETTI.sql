CREATE OR REPLACE FORCE VIEW SI4_COMPETENZA_OGGETTI
(ID_COMPETENZA, ID_TIPO_OGGETTO, OGGETTO, UTENTE, ACCESSO, 
 NOMINATIVO_UTENTE, ID_TIPO_ABILITAZIONE, DAL, AL, ID_FUNZIONE)
BEQUEATH DEFINER
AS 
SELECT
   COMP.ID_COMPETENZA,
   ABIL.ID_TIPO_OGGETTO,
   COMP.OGGETTO,
   COMP.UTENTE,
   COMP.ACCESSO,
   UTEN.NOMINATIVO NOMINATIVO_UTENTE,
   ABIL.ID_TIPO_ABILITAZIONE,
   COMP.DAL,
   COMP.AL,
   COMP.ID_FUNZIONE
FROM
   SI4_COMPETENZE COMP,
   SI4_ABILITAZIONI ABIL,
   AD4_UTENTI UTEN
WHERE
   COMP.ID_ABILITAZIONE = ABIL.ID_ABILITAZIONE
   AND COMP.UTENTE = UTEN.UTENTE
 UNION
SELECT
   COMP.ID_COMPETENZA,
   ABIL.ID_TIPO_OGGETTO,
   COMP.OGGETTO,
   COMP.UTENTE,
   COMP.ACCESSO,
   NULL NOMINATIVO_UTENTE,
   ABIL.ID_TIPO_ABILITAZIONE,
   COMP.DAL,
   COMP.AL,
   COMP.ID_FUNZIONE
FROM
   SI4_COMPETENZE COMP,
   SI4_ABILITAZIONI ABIL
WHERE
   COMP.ID_ABILITAZIONE = ABIL.ID_ABILITAZIONE
   AND COMP.UTENTE IS NULL;



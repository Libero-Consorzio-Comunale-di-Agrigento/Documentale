CREATE OR REPLACE PROCEDURE ASSEGNA_COMPETENZE(P_TIPO_OGGETTO IN VARCHAR2
, P_OGGETTO IN VARCHAR2
, P_UTENTE IN VARCHAR2 DEFAULT NULL
, P_RUOLO IN VARCHAR2 DEFAULT NULL
, P_AUTORE IN VARCHAR2
, P_ACCESSO IN VARCHAR2 DEFAULT 'S'
, P_DAL IN VARCHAR2 DEFAULT NULL
, P_AL IN VARCHAR2 DEFAULT NULL
, P_CREAZIONE IN VARCHAR2 DEFAULT 'N'
, P_MODIFICA IN VARCHAR2 DEFAULT 'N'
, P_LETTURA IN VARCHAR2 DEFAULT 'N'
, P_CANCELLAZIONE IN VARCHAR2 DEFAULT 'N'
, P_ESECUZIONE IN VARCHAR2 DEFAULT 'N'
, P_MANAGE IN VARCHAR2 DEFAULT 'N'
, P_ID_FUNZIONE IN VARCHAR2 DEFAULT NULL)
AS
   RETVAL NUMBER (2) := 0;
BEGIN
   IF P_CREAZIONE = 'S' THEN
       RETVAL := SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO, P_OGGETTO, 'C', P_UTENTE, P_RUOLO, P_AUTORE, P_ACCESSO, P_DAL, P_AL, P_ID_FUNZIONE);
      IF RETVAL = -1 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
      ELSIF RETVAL = -2 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
      ELSIF RETVAL = -3 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
      END IF;
   END IF;
   IF P_MODIFICA = 'S' THEN
       RETVAL := SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO, P_OGGETTO, 'U', P_UTENTE, P_RUOLO, P_AUTORE, P_ACCESSO, P_DAL, P_AL, P_ID_FUNZIONE);
      IF RETVAL = -1 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
      ELSIF RETVAL = -2 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
      ELSIF RETVAL = -3 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
      END IF;
   END IF;
   IF P_LETTURA = 'S' THEN
       RETVAL := SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO, P_OGGETTO, 'L', P_UTENTE, P_RUOLO, P_AUTORE, P_ACCESSO, P_DAL, P_AL, P_ID_FUNZIONE);
      IF RETVAL = -1 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
      ELSIF RETVAL = -2 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
      ELSIF RETVAL = -3 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
      END IF;
   END IF;
   IF P_CANCELLAZIONE = 'S' THEN
       RETVAL := SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO, P_OGGETTO, 'D', P_UTENTE, P_RUOLO, P_AUTORE, P_ACCESSO, P_DAL, P_AL, P_ID_FUNZIONE);
      IF RETVAL = -1 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
      ELSIF RETVAL = -2 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
      ELSIF RETVAL = -3 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
      END IF;
   END IF;
   IF P_ESECUZIONE = 'S' THEN
       RETVAL := SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO, P_OGGETTO, 'X', P_UTENTE, P_RUOLO, P_AUTORE, P_ACCESSO, P_DAL, P_AL, P_ID_FUNZIONE);
      IF RETVAL = -1 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
      ELSIF RETVAL = -2 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
      ELSIF RETVAL = -3 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
      END IF;
   END IF;
   IF P_MANAGE = 'S' THEN
       RETVAL := SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO, P_OGGETTO, 'M', P_UTENTE, P_RUOLO, P_AUTORE, P_ACCESSO, P_DAL, P_AL, P_ID_FUNZIONE);
      IF RETVAL = -1 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
      ELSIF RETVAL = -2 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
      ELSIF RETVAL = -3 THEN
              RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
      END IF;
   END IF;
END;
/


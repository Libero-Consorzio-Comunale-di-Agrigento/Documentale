CREATE OR REPLACE PROCEDURE ASSEGNA_COMPETENZE_MULTIPLE(
  P_TIPO_OGGETTO IN VARCHAR2
, P_OGGETTO IN VARCHAR2
, P_LISTAUTENTI IN VARCHAR2
, P_LISTATIPOABILITAZIONE IN VARCHAR2
, P_AUTORE IN VARCHAR2
, P_RUOLO IN VARCHAR2 DEFAULT NULL
, P_ACCESSO IN VARCHAR2 DEFAULT 'S'
, P_DAL IN VARCHAR2 DEFAULT NULL
, P_AL IN VARCHAR2 DEFAULT NULL
, P_ERROR OUT VARCHAR2
)
AS
BEGIN
  P_ERROR := ' ';
  -- INIZIO CICLO UTENTE
  DECLARE
  UTENTE SI4_COMPETENZE.UTENTE%TYPE;
  cicle_index NUMBER := 1;
  index_separator NUMBER := 0;
  old_index_separator NUMBER := 1;
  BEGIN
    LOOP BEGIN
     index_separator:=instr(p_ListaUtenti,',',1,cicle_index);
     IF index_separator<>0 THEN
         utente:=substr(p_ListaUtenti,old_index_separator,index_separator-old_index_separator);
     ELSE
        utente:=substr(p_ListaUtenti,old_index_separator,(length(p_ListaUtenti)-(old_index_separator-1)));
       END IF;
     --******************** INIZIO CICLO SUL TIPO DI ABILITAZIONE
     DECLARE
     abilitazione SI4_TIPI_ABILITAZIONE.TIPO_ABILITAZIONE%TYPE;
     CICLE_INDEX_AB NUMBER := 1;
     INDEX_SEPARATOR_AB NUMBER := 0;
     OLD_INDEX_SEPARATOR_AB NUMBER := 1;
     DESCRIZIONE_TIPOAB SI4_TIPI_ABILITAZIONE.DESCRIZIONE%TYPE;
     RETVAL NUMBER(2) := 0;
     BEGIN
         LOOP BEGIN
          INDEX_SEPARATOR_AB:=INSTR(P_LISTATIPOABILITAZIONE,',',1,CICLE_INDEX_AB);
         IF INDEX_SEPARATOR_AB<>0 THEN
            ABILITAZIONE:=SUBSTR(P_LISTATIPOABILITAZIONE,OLD_INDEX_SEPARATOR_AB,INDEX_SEPARATOR_AB-OLD_INDEX_SEPARATOR_AB);
         ELSE
            ABILITAZIONE:=SUBSTR(P_LISTATIPOABILITAZIONE,OLD_INDEX_SEPARATOR_AB,(LENGTH(P_LISTATIPOABILITAZIONE)-(OLD_INDEX_SEPARATOR_AB-1)));
           END IF;
         IF LENGTH(NVL(ABILITAZIONE,''))<>0 AND ABILITAZIONE<>',' THEN
               RETVAL:=SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO
                                        ,P_OGGETTO
                                      ,ABILITAZIONE
                                      ,UTENTE
                                      ,P_RUOLO
                                      ,P_AUTORE
                                      ,P_ACCESSO
                                      ,P_DAL
                                      ,P_AL);
               IF RETVAL<0 THEN
                  BEGIN
                     SELECT DESCRIZIONE
                       INTO DESCRIZIONE_TIPOAB
                       FROM SI4_TIPI_ABILITAZIONE
                         WHERE TIPO_ABILITAZIONE=ABILITAZIONE;
                  EXCEPTION WHEN TOO_MANY_ROWS THEN
                        DESCRIZIONE_TIPOAB:=ABILITAZIONE;
                  END;
                  IF P_ERROR = ' ' THEN
                     P_ERROR := '<b>Alcune competenze non sono state assegnate a causa dei seguenti errori:</b><BR><BR>';
                  END IF;
                  P_ERROR := P_ERROR || '<BR>Utente: '||UTENTE||' - Abilitazione: '||DESCRIZIONE_TIPOAB;
               END IF;
               IF RETVAL = -1 THEN
                          P_ERROR := P_ERROR || '<BR>Errore: Tipo di abilitazione incompatibile con l''oggetto indicato<BR>';
               ELSIF RETVAL = -2 THEN
                          P_ERROR := P_ERROR || '<BR>Errore: Non esiste l''oggetto<BR>';
               ELSIF RETVAL = -3 THEN
                  P_ERROR := P_ERROR || '<BR>Errore: Non esiste il tipo di abilitazione<BR>';
                   END IF;
         END IF;
         EXIT WHEN INDEX_SEPARATOR_AB=0;
           OLD_INDEX_SEPARATOR_AB:=INDEX_SEPARATOR_AB+1;
         CICLE_INDEX_AB:=CICLE_INDEX_AB+1;
        END; END LOOP;
     END;
     --******************** FINE CICLO SUL TIPO DI ABILITAZIONE
     EXIT WHEN INDEX_SEPARATOR=0;
     OLD_INDEX_SEPARATOR:=INDEX_SEPARATOR+1;
      CICLE_INDEX:=CICLE_INDEX+1;
    END; END LOOP;
  END;
  IF P_ERROR = ' ' THEN
     P_ERROR := '';
  END IF;
END;
/


CREATE OR REPLACE procedure AMV_VOCI_PIU
/**************************************************************************************
 NOME:        AMV_VOCI_PIU
 DESCRIZIONE: Procedure di insert/update sulla tabella AMV_VOCI.
              Se il parametro P_ABILITAZIONI e valorizzato a 1 o -1 si procede con
              l'insert/update o delete sulla tabella AMV_ABILITAZIONI in corrispondenza
              di voce, ruolo e modulo selezionati (parametri P_VOCE, P_RUOLO E P_MODULO)
              ed eventualmente sul ruolo * utilizzato per costruire la struttura
              dell'albero delle abilitazioni.
 ARGOMENTI:   a_<arg1> IN OUT <type> <Descrizione argomento 1>
              a_<arg2> IN OUT <type> <Descrizione argomento 2>
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ----------------------------------------------------------------
 0    __/__/____ __     Prima emissione.
 1    28/11/2002 AO     Abilitazione con cambio di modulo corretta
 2    17/12/2002 AO     Abilitazione per tutti i ruoli
 3    14/02/2002 AO     Gestione in update e modifica dei record collegati in AMV_GUIDE
****************************************************************************************/
( P_INSERT_UPDATE IN VARCHAR2, P_VOCE IN VARCHAR2, P_PROGETTO IN VARCHAR2, P_ACRONIMO IN VARCHAR2
, P_ACRONIMO_AL1 IN VARCHAR2, P_ACRONIMO_AL2 IN VARCHAR2, P_TITOLO IN VARCHAR2, P_TITOLO_AL1 IN VARCHAR2
, P_TITOLO_AL2 IN VARCHAR2
, P_TIPO_VOCE IN VARCHAR2, P_TIPO IN VARCHAR2, P_MODULO IN VARCHAR2, P_STRINGA IN VARCHAR2
, P_PROFILO IN VARCHAR2, P_VOCE_GUIDA IN VARCHAR2, P_PROPRIETA IN VARCHAR2
, P_NOTE IN VARCHAR2, P_PADRE IN NUMBER, P_PADRE_OLD IN NUMBER, P_ABIL_MODULO IN VARCHAR2, P_SEQUENZA IN NUMBER, P_RUOLO IN VARCHAR2, P_ABILITAZIONI IN NUMBER, P_VOCE_OLD IN VARCHAR2
)
is
d_stessa_pk        varchar2(8);
d_stesso_padre     number(8);
d_abilitazione     number(8);
d_temp             number(8);
d_temp_voce_padre  varchar2(8);
d_temp_seq         number(8);
d_temp_mod         number(8);
d_temp_abil        number(8);
BEGIN
   IF P_INSERT_UPDATE = 'I' THEN
      INSERT INTO AMV_VOCI ( VOCE, PROGETTO, ACRONIMO, ACRONIMO_AL1, ACRONIMO_AL2
                          , TITOLO, TITOLO_AL1, TITOLO_AL2, TIPO_VOCE, TIPO, MODULO
                     , STRINGA, PROFILO, VOCE_GUIDA, PROPRIETA, NOTE)
                VALUES ( P_VOCE, P_PROGETTO, P_ACRONIMO, P_ACRONIMO_AL1, P_ACRONIMO_AL2
                          , P_TITOLO, P_TITOLO_AL1, P_TITOLO_AL2, P_TIPO_VOCE, P_TIPO, P_MODULO
                     , P_STRINGA, P_PROFILO, P_VOCE_GUIDA, P_PROPRIETA, P_NOTE)
     ;
     IF P_ABILITAZIONI = 1 OR P_ABILITAZIONI = 9 THEN
       select AMV_ABIL_SEQ.NEXTVAL
         INTO d_temp_abil
         from dual;
         --d_abilitazione := SI4.NEXT_ID('AMV_ABILITAZIONI','ABILITAZIONE');
           for ruoli in (SELECT distinct RUOLO
                           FROM AD4_RUOLI
                      WHERE (MODULO = P_ABIL_MODULO OR MODULO IS NULL)
                        AND (PROGETTO = P_PROGETTO OR PROGETTO IS NULL)
                       AND NOT EXISTS (SELECT 1
                                          FROM AMV_ABILITAZIONI
                                            WHERE VOCE_MENU = P_VOCE
                                             AND PADRE = nvl(P_PADRE,0)
                                             AND RUOLO = DECODE(P_ABILITAZIONI,9,AD4_RUOLI.RUOLO, nvl(P_RUOLO,'*'))
                                             AND MODULO = P_ABIL_MODULO
                                     )
                        AND EXISTS (SELECT 1
                                           FROM AMV_ABILITAZIONI
                                                 WHERE (ABILITAZIONE = nvl(P_PADRE,0)
                                                  AND RUOLO = DECODE(P_ABILITAZIONI,9,AD4_RUOLI.RUOLO, nvl(P_RUOLO,'*'))
                                                  AND MODULO = P_ABIL_MODULO)
                                        OR nvl(P_PADRE,0) = 0
                                     )
                   AND RUOLO = DECODE(P_ABILITAZIONI,9,AD4_RUOLI.RUOLO, nvl(P_RUOLO,'*'))
                  ORDER BY RUOLO
                     )
          LOOP
       DBMS_OUTPUT.PUT_LINE('MODULO '||P_ABIL_MODULO||', RUOLO '||RUOLI.RUOLO||', VOCE_MENU '||P_VOCE||', PADRE '||P_padre);
            INSERT INTO AMV_ABILITAZIONI (ABILITAZIONE, MODULO, RUOLO, VOCE_MENU, PADRE, SEQUENZA, DISPOSITIVO)
                                    VALUES (D_TEMP_ABIL, P_ABIL_MODULO, RUOLI.RUOLO, P_VOCE, nvl(P_PADRE,0), nvl(P_SEQUENZA,0), TO_CHAR(NULL))
            ;
          END LOOP;
     END IF;
   END IF;
   IF P_INSERT_UPDATE = 'U' THEN
        BEGIN
     /* Controllo se si sta modificando la chiave; in questo caso inserisco un nuovo record, modifico
     il corrispondente record sulla tabella AMV_ABILITAZIONI e quindi elimino il vecchio record.
     L'update diventa quindi una INSERT + UPDATE sulla tabella correlata + DELETE del vecchio record*/
     IF NVL(P_VOCE,' ') = NVL(P_VOCE_OLD,' ') THEN
          UPDATE AMV_VOCI SET
             VOCE = P_VOCE,
             PROGETTO = P_PROGETTO,
             ACRONIMO = P_ACRONIMO,
             ACRONIMO_AL1 = P_ACRONIMO_AL1,
             ACRONIMO_AL2 = P_ACRONIMO_AL2,
             TITOLO = P_TITOLO,
             TITOLO_AL1 = P_TITOLO_AL1,
             TITOLO_AL2 = P_TITOLO_AL2,
             TIPO_VOCE = P_TIPO_VOCE,
             TIPO = P_TIPO,
             MODULO = P_MODULO,
             STRINGA = P_STRINGA,
             PROFILO = P_PROFILO,
             VOCE_GUIDA = P_VOCE_GUIDA,
             PROPRIETA = P_PROPRIETA,
             NOTE = P_NOTE
         WHERE VOCE = P_VOCE_OLD;
     ELSE
          INSERT INTO AMV_VOCI ( VOCE, PROGETTO, ACRONIMO, ACRONIMO_AL1, ACRONIMO_AL2
                          , TITOLO, TITOLO_AL1, TITOLO_AL2, TIPO_VOCE, TIPO, MODULO
                     , STRINGA, PROFILO, VOCE_GUIDA, PROPRIETA, NOTE)
                VALUES ( P_VOCE, P_PROGETTO, P_ACRONIMO, P_ACRONIMO_AL1, P_ACRONIMO_AL2
                          , P_TITOLO, P_TITOLO_AL1, P_TITOLO_AL2, P_TIPO_VOCE, P_TIPO, P_MODULO
                     , P_STRINGA, P_PROFILO, P_VOCE_GUIDA, P_PROPRIETA, P_NOTE)
          ;
     END IF;
      DELETE FROM AMV_ABILITAZIONI A  -- ELIMINO ABILITAZIONI INCOMPATIBILI, PER RUOLO, COL NUOVO PADRE
              WHERE
            VOCE_MENU = P_VOCE_OLD
            AND MODULO = P_ABIL_MODULO
            AND PADRE = P_PADRE_OLD
            AND RUOLO NOT IN (SELECT RUOLO
                                FROM AMV_ABILITAZIONI B
                               WHERE B.ABILITAZIONE = P_PADRE
                          )
     ;
     -- VERIFICA CHE L'AGGIORNAMENTO FACCIA RIFERIMENTO ALLO STESSO MODULO
     D_TEMP_MOD := 0;
     BEGIN
        SELECT COUNT(1) INTO D_TEMP_MOD
          FROM AMV_ABILITAZIONI
         WHERE VOCE_MENU = P_VOCE_OLD
           AND MODULO = P_ABIL_MODULO
           AND PADRE = P_PADRE_old
      ;
        IF D_TEMP_MOD = 0 THEN
           INSERT INTO AMV_ABILITAZIONI (MODULO, RUOLO, VOCE_MENU, PADRE, SEQUENZA, DISPOSITIVO)
                  VALUES (P_ABIL_MODULO, nvl(P_RUOLO,'*'), P_VOCE, P_PADRE, P_SEQUENZA, TO_CHAR(NULL))
           ;
        END IF;
     END;
-- Aggiorna Abilitazioni con nuovo codice voce
     UPDATE AMV_ABILITAZIONI
        SET VOCE_MENU = P_VOCE
      WHERE VOCE_MENU = P_VOCE_OLD
    ;
-- Aggiorna Voci con nuovo codice voce su colonna VOCE_GUIDA
     UPDATE AMV_VOCI
        SET VOCE_GUIDA = P_VOCE
      WHERE VOCE_GUIDA = P_VOCE_OLD
    ;
-- Aggiorna Guide con nuovo codice voce
     UPDATE AMV_GUIDE
        SET GUIDA = decode(GUIDA, P_VOCE_OLD, P_VOCE, GUIDA)
        , VOCE_GUIDA = decode(VOCE_GUIDA, P_VOCE_OLD, P_VOCE, VOCE_GUIDA)
        , VOCE_MENU  = decode(VOCE_MENU, P_VOCE_OLD, P_VOCE, VOCE_MENU)
        , VOCE_RIF   = decode(VOCE_RIF, P_VOCE_OLD, P_VOCE, VOCE_RIF)
      WHERE GUIDA      = P_VOCE_OLD
        OR VOCE_GUIDA = P_VOCE_OLD
        OR VOCE_MENU  = P_VOCE_OLD
        OR VOCE_RIF   = P_VOCE_OLD
    ;
     -- AGGIORNA PADRE E SEQUENZA SOLO IN RIFERIMENTO AL MODULO PASSATO ALLA PROCEDURE
     IF P_ABILITAZIONI != 0 THEN
        UPDATE AMV_ABILITAZIONI
           SET PADRE = P_PADRE,
               SEQUENZA = nvl(P_SEQUENZA,0)
         WHERE VOCE_MENU = P_VOCE_OLD
           AND MODULO = P_ABIL_MODULO
           AND PADRE = P_PADRE_OLD
        ;
     END IF;
     IF NVL(P_VOCE,' ') <> NVL(P_VOCE_OLD,' ') THEN
        DELETE AMV_VOCI
         WHERE VOCE = P_VOCE_OLD
        ;
     END IF;
     IF P_ABILITAZIONI = -1 THEN     -- DISABILITAZIONE DELLA VOCE
        DELETE FROM AMV_ABILITAZIONI
             WHERE VOCE_MENU = P_VOCE
              AND PADRE = P_PADRE
             AND RUOLO = P_RUOLO
             AND MODULO = P_ABIL_MODULO
       ;
      -- raise_application_error(-20999,SQL%rowcount);
     ELSE
/*        BEGIN -- Controllo che la voce PADRE della voce in Update non sia gia abilitata
           SELECT 1
           INTO d_temp
           FROM AMV_ABILITAZIONI
           WHERE abilitazione = P_PADRE
            AND RUOLO = P_RUOLO
         ;
       EXCEPTION WHEN NO_DATA_FOUND THEN
          BEGIN -- Voce non abilitata, preleva dati per ruolo *
              SELECT voce_menu, padre, sequenza
              INTO d_temp_voce_padre, d_temp, d_temp_seq
              FROM AMV_ABILITAZIONI
              WHERE abilitazione = P_PADRE
               AND RUOLO = '*'
            ;
            -- Inserimento abilitazione del Padre
               INSERT INTO AMV_ABILITAZIONI (ABILITAZIONE, MODULO, RUOLO, VOCE_MENU, PADRE, SEQUENZA, DISPOSITIVO)
                     VALUES (P_PADRE, decode(P_PADRE,0,'',P_ABIL_MODULO), nvl(P_RUOLO,'*'), d_temp_voce_padre, d_temp, d_temp_seq, TO_CHAR(NULL))
               ;
         EXCEPTION
            WHEN OTHERS THEN
                 d_temp := NULL;
         END;
       END;
*/
       IF P_ABILITAZIONI = 1 OR P_ABILITAZIONI = 9 THEN -- ABILITAZIONE DELLA VOCE
           BEGIN -- Controllo che la voce in Update non sia gia abilitata
                SELECT ABILITAZIONE INTO d_temp
                FROM AMV_ABILITAZIONI
                WHERE VOCE_MENU = P_VOCE
                 AND PADRE = P_PADRE
                 AND RUOLO = DECODE(P_ABILITAZIONI,9,'xxx',P_RUOLO)
                 AND MODULO = P_ABIL_MODULO
              ;
             EXCEPTION
         WHEN NO_DATA_FOUND THEN
               BEGIN -- Voce non abilitata, controllo sia abilitata per ruolo *
                   SELECT ABILITAZIONE INTO d_temp
                    FROM AMV_ABILITAZIONI
                    WHERE VOCE_MENU = P_VOCE
                     AND PADRE = P_PADRE
                     AND RUOLO = '*'
                     AND MODULO = P_ABIL_MODULO
                  ;
            EXCEPTION
            WHEN OTHERS THEN
                 d_temp := NULL;
            END;
            for ruoli in (SELECT distinct RUOLO
                            FROM AD4_RUOLI
                       WHERE (MODULO = P_ABIL_MODULO OR MODULO IS NULL)
                         AND (PROGETTO = P_PROGETTO OR PROGETTO IS NULL)
                        AND NOT EXISTS (SELECT 1
                                           FROM AMV_ABILITAZIONI
                                                 WHERE VOCE_MENU = P_VOCE
                                                  AND PADRE = nvl(P_PADRE,0)
                                                  AND RUOLO = DECODE(P_ABILITAZIONI,9,AD4_RUOLI.RUOLO, nvl(P_RUOLO,'*'))
                                                  AND MODULO = P_ABIL_MODULO
                                     )
                        AND EXISTS (SELECT 1
                                           FROM AMV_ABILITAZIONI
                                                 WHERE (ABILITAZIONE = nvl(P_PADRE,0)
                                                  AND RUOLO = DECODE(P_ABILITAZIONI,9,AD4_RUOLI.RUOLO, nvl(P_RUOLO,'*'))
                                                  AND MODULO = P_ABIL_MODULO)
                                        OR nvl(P_PADRE,0) = 0
                                     )
                                AND RUOLO = DECODE(P_ABILITAZIONI,9,AD4_RUOLI.RUOLO, nvl(P_RUOLO,'*'))
                      )
             LOOP
                    INSERT INTO AMV_ABILITAZIONI (ABILITAZIONE, MODULO, RUOLO, VOCE_MENU, PADRE, SEQUENZA, DISPOSITIVO)
                                           VALUES (d_temp, P_ABIL_MODULO, RUOLI.RUOLO, P_VOCE, nvl(P_PADRE,0), nvl(P_SEQUENZA,0), TO_CHAR(NULL))
                      ;
             END LOOP;
          END;
      END IF;
        IF P_ABILITAZIONI = 0 THEN -- INSERIMENTO DI NUOVA ABILITAZIONE DELLA VOCE COMUNQUE.
           BEGIN
         INSERT INTO AMV_ABILITAZIONI (MODULO, RUOLO, VOCE_MENU, PADRE, SEQUENZA, DISPOSITIVO)
                     VALUES (P_ABIL_MODULO, nvl(P_RUOLO,'*'), P_VOCE, P_PADRE, P_SEQUENZA, TO_CHAR(NULL))
           ;
          END;
      END IF;
     END IF;
     END;
   END IF;
END AMV_VOCI_PIU;
/* End Procedure: AMV_VOCI_PIU */
/


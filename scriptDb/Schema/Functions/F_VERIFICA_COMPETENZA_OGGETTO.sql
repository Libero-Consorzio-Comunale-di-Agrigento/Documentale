CREATE OR REPLACE FUNCTION F_VERIFICA_COMPETENZA_OGGETTO(P_WRKSP IN VARCHAR2,P_AREA IN VARCHAR2,P_CM IN VARCHAR2,P_CR IN VARCHAR2,P_CPROT IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2)
   RETURN NUMBER
 IS
   RESULT        NUMBER:= 1;
   ID_DOCUMENTO  DOCUMENTI.ID_DOCUMENTO%TYPE;
 BEGIN
   BEGIN
     BEGIN
      IF LENGTH(NVL(P_WRKSP,'')) > 0 THEN
          --dbms_output.put_line('VERIFICA '||P_WRKSP);
          SELECT gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                    id_viewcartella,
                                    'L',
                                    P_UTENTE,
                                    f_trasla_ruolo (P_RUOLO,
                                                    'GDMWEB',
                                                    'GDMWEB'
                                                   ),
                                    TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                   ) COMP_LETTURA
          INTO RESULT
          FROM cartelle, view_cartella
          WHERE cartelle.id_cartella = P_WRKSP
           AND cartelle.id_cartella = view_cartella.id_cartella
           AND NVL (cartelle.stato, 'BO') <> 'CA';
      ELSE
        IF (NVL(P_CPROT,'N') = 'S') THEN
         --dbms_output.put_line('VERIFICA '||P_CPROT||' - '||P_UTENTE);
         BEGIN
            EXECUTE IMMEDIATE 'BEGIN :RESULT := AG_COMPETENZE_PROTOCOLLO.CREAZIONE ('''||P_UTENTE||'''); END; ' USING OUT RESULT;
            IF RESULT IS NULL THEN
             RETURN NVL(RESULT,0);
            END IF;
            EXCEPTION
             WHEN OTHERS THEN
              IF SQLCODE=-6550 THEN
                RESULT := -1;
                RETURN RESULT;
              ELSE
                RAISE;
              END IF;
         END;
        ELSE
          IF LENGTH(NVL(P_CR,'')) > 0 THEN
           BEGIN
            --dbms_output.put_line('VERIFICA '||P_AREA||' - '||P_CM||' - '||P_TIPO_ABILITAZIONE);
            ID_DOCUMENTO := F_IDDOC_FROM_CM_AREA_CR ( P_CM, P_AREA, P_CR );
            IF ID_DOCUMENTO = -1 THEN
              RESULT := -1;
              RETURN RESULT;
            END IF;
            SELECT gdm_competenza.gdm_verifica ('DOCUMENTI',
                                       ID_DOCUMENTO,
                                       P_TIPO_ABILITAZIONE,
                                       P_UTENTE,
                                       f_trasla_ruolo (P_RUOLO,
                                                       'GDMWEB',
                                                       'GDMWEB'
                                                      ),
                                       TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                      ) COMP
             INTO RESULT
             FROM DUAL;
             EXCEPTION
              WHEN NO_DATA_FOUND THEN
                 RESULT := -1;
                 RETURN RESULT;
              WHEN OTHERS THEN
               IF SQLCODE=-6550 THEN
                 RESULT := -1;
                 RETURN RESULT;
               ELSE
                  RAISE;
               END IF;
           END;
          ELSE
           BEGIN
           --dbms_output.put_line('VERIFICA '||P_AREA||' - '||P_CM||' - '||P_TIPO_ABILITAZIONE);
           SELECT gdm_competenza.gdm_verifica ('TIPI_DOCUMENTO',
                                     id_tipodoc,
                                     P_TIPO_ABILITAZIONE,
                                     P_UTENTE,
                                     f_trasla_ruolo (P_RUOLO,
                                                     'GDMWEB',
                                                     'GDMWEB'
                                                    ),
                                     TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                    ) COMP
           INTO RESULT
           FROM tipi_documento tp
           WHERE tp.area_modello = P_AREA AND tp.nome = P_CM;
           EXCEPTION
            WHEN NO_DATA_FOUND THEN
               RESULT := -1;
               RETURN RESULT;
            WHEN OTHERS THEN
             IF SQLCODE=-6550 THEN
               RESULT := -1;
               RETURN RESULT;
             ELSE
                RAISE;
             END IF;
           END;
          END IF;
        END IF;
      END IF;
     END;
    EXCEPTION
         WHEN OTHERS THEN
         RAISE;
   END;
   RETURN NVL(RESULT,-1);
  END;
/


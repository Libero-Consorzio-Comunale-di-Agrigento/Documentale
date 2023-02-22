CREATE OR REPLACE FUNCTION F_ELIMINA_QUERY(A_IDQUERY NUMBER)
RETURN VARCHAR2 IS
/*****************************************************************************************
   NAME:       F_ELIMINA_QUERY
   PURPOSE:    SCOPO DELLA FUNZIONE E' QUELLO DI ELIMINARE LE QUERY
               IDENTIFICATE DA:A_IDQUERY.
               SE A_IDQUERY VIENE PASSATO NULLO, VERRANNO ELIMINATE
               TUTTE LE QUERY DEL DOCUMENTALE
    RETURN:    1 SE L'ELIMINAZIONE E' AVVENUTA CON SUCCESSO
               "RAISE" IN CASO CONTRARIO
   REVISIONS:
   VER   VER DMSERVER.COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          14/05/2005  MANNELLA G,      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       D_RETURN VARCHAR2(32000);
       CURSOR C_QUERY IS
         SELECT ID_QUERY
           FROM QUERY
          WHERE A_IDQUERY IS NULL
         UNION ALL
         SELECT  ID_QUERY
           FROM QUERY
          WHERE ID_QUERY = A_IDQUERY;
       CONTATORE NUMBER := 0;
BEGIN
  D_RETURN:='1';
  FOR CQ IN C_QUERY LOOP
      BEGIN
        CONTATORE := CONTATORE + 1;
        /* CERCO IL DOCUMENTO PROFILO ASSOCIATO */
        /* SE NON ESISTE, IGNORO....            */
        DECLARE
          D_IDDOC     DOCUMENTI.ID_DOCUMENTO%TYPE;
          RETVAL      NUMBER;
          BEGIN
            SELECT ID_DOCUMENTO_PROFILO
              INTO D_IDDOC
              FROM QUERY
             WHERE ID_QUERY=A_IDQUERY;
            RETVAL := F_ELIMINA_DOCUMENTO(D_IDDOC);
          EXCEPTION WHEN NO_DATA_FOUND THEN  NULL;
                    WHEN OTHERS THEN
                         D_RETURN:=SQLERRM;
                         RAISE_APPLICATION_ERROR('-20990',SQLERRM);
          END;
          /* ELIMINO QUERY-NODO ALBERO-COMPETENZE */
          BEGIN
            DELETE
              FROM SI4_COMPETENZE
             WHERE OGGETTO = TO_CHAR(CQ.ID_QUERY)
               AND ID_ABILITAZIONE IN (SELECT ID_ABILITAZIONE
                                         FROM SI4_ABILITAZIONI A, SI4_TIPI_OGGETTO O
                                        WHERE A.ID_TIPO_OGGETTO = O.ID_TIPO_OGGETTO
                                          AND TIPO_OGGETTO = 'QUERY'
                                      );
            DELETE
              FROM LINKS
             WHERE TIPO_OGGETTO='Q'
               AND ID_OGGETTO=CQ.ID_QUERY;
            DELETE
              FROM QUERY
             WHERE ID_QUERY = CQ.ID_QUERY;
          EXCEPTION WHEN OTHERS THEN
                    D_RETURN:=SQLERRM;
                    ROLLBACK;
                    RAISE_APPLICATION_ERROR('-20990',  SQLERRM);
          END;
        IF CONTATORE = 100 THEN
           COMMIT;
           CONTATORE := 0;
        END IF;
      END;
  END LOOP;
  IF A_IDQUERY IS NULL THEN
   COMMIT;
  END IF;
  RETURN D_RETURN;
END;
/


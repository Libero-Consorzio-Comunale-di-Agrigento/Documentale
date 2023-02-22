CREATE OR REPLACE FUNCTION F_ELIMINA_DOCUMENTO(A_DOC NUMBER, A_ROLLBACK NUMBER DEFAULT 1,A_DEL_FILEFS_COMMIT NUMBER DEFAULT 0)
RETURN NUMBER IS
/***********************************************************************************************
   NAME:          F_ELIMINA_DOCUMENTO
   PURPOSE:    ELIMINA IL DOCUMENTO CON IDENTIFICATIVO A_DOC
   PARAMETER:
                       A_DOC                             IDENTIFICATIVO DEL DOCUMENTO
                       A_ROLLBACK                    SE IMPOSTATO A 1 , IN CASO DI ERRORE EFFETTUA IL ROLLBACK (default 1)
                       A_DEL_FILEFS_COMMIT    SE IMPOSTATO A 1 , IN CASO DI FILE SU FS CANCELLA ANCHE IL FILE SU FS   (default 0)
   RETURN:      1 SE ELIMINA IL DOCUMENTO
                      0 ALTRIMENTI
   REVISIONS:
   VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          09/02/2007  MANNELLA G.      1. CREATED THIS FUNCTION.
   1.1   TUTTE LE VERSIONI          12/12/2013  MANNELLA G.      1. ADD A_DEL_FILEFS_COMMIT AND A_ROLLBACK PARAMETER
**********************************************************************************************/
       TMPVAR NUMBER(1) := 0;
       AR     DOCUMENTI.AREA%TYPE;
       CR     DOCUMENTI.CODICE_RICHIESTA%TYPE;
BEGIN
    BEGIN
           DELETE FROM SI4_COMPETENZE
           WHERE ID_COMPETENZA IN (
                 SELECT ID_COMPETENZA
                   FROM SI4_COMPETENZE S, SI4_ABILITAZIONI A,SI4_TIPI_OGGETTO  O
                  WHERE OGGETTO = TO_CHAR(A_DOC)
                    AND TIPO_COMPETENZA in ('U','F')
                    AND S.ID_ABILITAZIONE = A.ID_ABILITAZIONE
                    AND A.ID_TIPO_OGGETTO = O.ID_TIPO_OGGETTO
                    AND TIPO_OGGETTO = 'DOCUMENTI');

          DELETE FROM RIFERIMENTI
           WHERE ID_DOCUMENTO = A_DOC
              OR ID_DOCUMENTO_RIF = A_DOC;

          DELETE FROM LINKS
           WHERE ID_OGGETTO = A_DOC
             AND TIPO_OGGETTO = 'D';

          SELECT AREA,CODICE_RICHIESTA
          INTO AR,CR
          FROM DOCUMENTI
           WHERE ID_DOCUMENTO = A_DOC;

          IF  A_DEL_FILEFS_COMMIT=1 THEN
              FOR I IN (SELECT ID_OGGETTO_FILE
                            FROM OGGETTI_FILE
                            WHERE ID_DOCUMENTO=A_DOC)
              LOOP
                    GDM_OGGETTI_FILE.DELETEOGGETTOFILE(I.ID_OGGETTO_FILE );
              END LOOP;
          END IF;

          DELETE FROM DOCUMENTI
           WHERE ID_DOCUMENTO = A_DOC;

          DELETE FROM RICHIESTE
          WHERE AREA=AR AND CODICE_RICHIESTA=CR;



          tmpVar := 1;
     EXCEPTION WHEN OTHERS THEN
         IF A_ROLLBACK=1 THEN
            ROLLBACK;
         END IF;

         RAISE_APPLICATION_ERROR('-20990',SQLERRM);
     END;

    RETURN TMPVAR;
END F_ELIMINA_DOCUMENTO;
/


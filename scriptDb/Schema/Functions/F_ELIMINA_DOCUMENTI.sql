CREATE OR REPLACE FUNCTION F_ELIMINA_DOCUMENTI(A_AREA VARCHAR2,A_MODELLO VARCHAR2,
                                               A_CARTORQUERY VARCHAR2 DEFAULT 'N')
RETURN NUMBER IS
/*****************************************************************************************
   NAME:       F_ELIMINA_DOCUMENTO
   PURPOSE:    SCOPO DELLA FUNZIONE E QUELLO DI ELIMINARE I DOCUMENTI RINTRACCIABILI
               MEDIANTE:
                          AREA
                          CODICE MODELLO
               IL PARAMETRO A_CARTORQUERY SERVE PER SPECIFICARE SE SI STA
               ELIMINANDO UN PROFILO DOCUMENTO OPPURE PROFILO-CARTELLA-QUERY
            SE MESSO A N NON VERRANNO CONSIDERATI I DOC_PROFILO
            SE MESSO A S VERRANNO CONSIDERATI I DOC_PROFILO
   RETURN:     1 SE ELIMINA I DOCUMENTI
               0 ALTRIMENTI
   REVISIONS:
   VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          09/02/2007  MANNELLA G.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       TMPVAR NUMBER(1) := 0;
       CURSOR C_2 IS
         SELECT ID_DOCUMENTO
           FROM DOCUMENTI D, MODELLI M
          WHERE D.AREA=A_AREA AND CODICE_MODELLO=A_MODELLO
            AND D.ID_TIPODOC=M.ID_TIPODOC
            AND (( (M.TIPO_USO NOT IN ('C','F','W')) AND (M.TIPO_USO NOT IN ('Q','V')) ) OR A_CARTORQUERY='Y');
       CONTATORE NUMBER := 0;
BEGIN
    FOR C2 IN C_2 LOOP
        BEGIN
          tmpVar := F_ELIMINA_DOCUMENTO(C2.ID_DOCUMENTO);
        CONTATORE:=CONTATORE+1;
          IF CONTATORE = 100 THEN
             COMMIT;
             CONTATORE := 0;
          END IF;
        EXCEPTION WHEN OTHERS THEN
          ROLLBACK;
          RAISE_APPLICATION_ERROR('-20990',SQLERRM);
        END;
    END LOOP;
  COMMIT;
  RETURN TMPVAR;
END F_ELIMINA_DOCUMENTI;
/


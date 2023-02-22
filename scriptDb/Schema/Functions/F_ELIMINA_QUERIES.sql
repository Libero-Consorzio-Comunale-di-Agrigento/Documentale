CREATE OR REPLACE FUNCTION F_ELIMINA_QUERIES(A_AREA VARCHAR2, A_CODICE_MODELLO VARCHAR2)
RETURN NUMBER IS
/*****************************************************************************************
   NAME:       F_ELIMINA_QUERIES
   PURPOSE:    ELIMINA LE QUERY DI AREA A_AREA E MODELLO A_CODICE_MODELLO
   RETURN:     1 SE ELIMINA LE QUERY
               0 ALTRIMENTI
   REVISIONS:
   VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          09/02/2007  MANNELLA G.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       TMPVAR NUMBER(1) := 0;
       CURSOR C_QUERYLIST IS
         SELECT ID_QUERY
             FROM QUERY Q, DOCUMENTI D, MODELLI M
           WHERE Q.ID_DOCUMENTO_PROFILO = D.ID_DOCUMENTO
          AND D.ID_TIPODOC = M.ID_TIPODOC
          AND D.AREA=A_AREA AND CODICE_MODELLO=A_CODICE_MODELLO
          ORDER BY 1 DESC;
      CONTATORE NUMBER := 0;
BEGIN
  TMPVAR:=1;
  FOR CQUERYLIST IN C_QUERYLIST LOOP
      TMPVAR:=F_ELIMINA_QUERY(CQUERYLIST.ID_QUERY);
     CONTATORE:=CONTATORE+1;
      IF CONTATORE = 100 THEN
         COMMIT;
         CONTATORE := 0;
      END IF;
  END LOOP;
  COMMIT;
  RETURN TMPVAR;
END F_ELIMINA_QUERIES;
/


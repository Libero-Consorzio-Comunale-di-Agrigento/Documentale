CREATE OR REPLACE FUNCTION F_ISPADRE(P_PADRE VARCHAR2, P_FIGLIO NUMBER)
RETURN NUMBER IS
/*****************************************************************************************
   NAME:       F_IS_PADRE
   PURPOSE:    VERIFICA SE P_PADRE E' UN PADRE DI P_FIGLIO NELL'ALBERO FORMATO DAI LINKS
   RETURN:     1 SE P_PADRE E' PADRE DI P_FIGLIO
               0 ALTRIMENTI
   REVISIONS:
   VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   2.8                       13/06/2007  MANNELLA G.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
   N_ISPADRE NUMBER(1);
 BEGIN
     SELECT 1
       INTO N_ISPADRE
       FROM dual
      WHERE P_PADRE IN
            (SELECT ID_CARTELLA
               FROM LINKS
              START WITH  LINKS.ID_OGGETTO = P_FIGLIO
            CONNECT BY PRIOR LINKS.ID_CARTELLA=ID_OGGETTO and TIPO_OGGETTO='C');
     RETURN N_ISPADRE;
 EXCEPTION WHEN OTHERS THEN
     RETURN 0;
 END  F_ISPADRE;
/


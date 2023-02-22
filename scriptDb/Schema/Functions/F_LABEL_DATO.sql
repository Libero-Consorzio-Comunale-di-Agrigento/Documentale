CREATE OR REPLACE FUNCTION F_LABEL_DATO(A_AREA VARCHAR2,A_DATO VARCHAR2)
RETURN VARCHAR2 IS
/*****************************************************************************************
   NAME:       F_LABEL_DATO
   PURPOSE:    SCOPO DELLA FUNZIONE E QUELLO DI RESTITUIRE LA LABEL DEL DATO A_DATO
               PER UNA DATA AREA A_AREA
            LA LABEL VIENE RICERCATA RICORSIVAMENTE SULLE AREE PADRE NEL CASO
            IN CUI A_AREA SIA UNA SOTTOAREA
   RETURN:     LA LABEL DEL DATO A_DATO DELL'AREA A_AREA
   REVISIONS:
   VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          18/04/2007  MANNELLA G.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       D_LABEL DATI.LABEL%TYPE := A_DATO;
BEGIN
    BEGIN
       SELECT NVL(LABEL,A_DATO) ETICHETTA
        INTO D_LABEL
        FROM DATI
        WHERE AREA = A_AREA
          AND DATO=A_DATO;
      EXCEPTION WHEN NO_DATA_FOUND THEN
             D_LABEL:=A_DATO;
   END;
   IF D_LABEL=A_DATO AND INSTR(A_AREA,'.')>0 THEN
   DBMS_OUTPUT.PUT_LINE(SUBSTR(A_AREA,1,INSTR(A_AREA,'.',-1)-1));
      RETURN F_LABEL_DATO(SUBSTR(A_AREA,1,INSTR(A_AREA,'.',-1)-1),A_DATO);
   END IF;
    RETURN D_LABEL;
END F_LABEL_DATO;
/


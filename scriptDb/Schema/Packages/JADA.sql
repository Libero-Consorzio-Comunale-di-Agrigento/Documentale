CREATE OR REPLACE PACKAGE JADA AS
   FUNCTION VERIFICA_UTENTE(A_AREA IN VARCHAR2, A_UTENTE IN VARCHAR2 )
      RETURN NUMBER;
   PROCEDURE GEN_VIEW(P_CATEGORIA CATEGORIE_MODELLO.CATEGORIA%TYPE);
   PROCEDURE GEN_WRKSPS(P_AREA IN VARCHAR2, P_NOME IN VARCHAR2);
   PROCEDURE GEN_ALL_VIEW(P_AREA IN VARCHAR2);
END JADA;
/

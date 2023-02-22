CREATE OR REPLACE FUNCTION F_Elimina_Documento_Logico (
   A_DOC     NUMBER,
   A_USER    VARCHAR2,
   a_roll    NUMBER DEFAULT 1)
   RETURN NUMBER
IS
   /*****************************************************************************************
      NAME:       F_ELIMINA_DOCUMENTO
      PURPOSE:    ELIMINA IL DOCUMENTO CON IDENTIFICATIVO A_DOC IN MANIERA LOGICA (METTE LO STATO A 'CA')
      RETURN:     1 SE ELIMINA IL DOCUMENTO
                  0 ALTRIMENTI
      REVISIONS:
      VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
      ----  -------------------------  ----------  ---------------  -------------------------
      1.0   2.8                        06/06/2007  MANNELLA G.      1. CREATED THIS FUNCTION.
   *****************************************************************************************/
   TMPVAR    NUMBER (1) := 0;
   EXSTATO   NUMBER (1) := 0;
BEGIN
   BEGIN
      BEGIN
         SELECT 1
           INTO EXSTATO
           FROM STATI_DOCUMENTO
          WHERE ID_DOCUMENTO = A_DOC AND STATO = 'CA';
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            EXSTATO := 0;
      END;

      IF EXSTATO = 0
      THEN
         INSERT INTO STATI_DOCUMENTO (ID_STATO,
                                      ID_DOCUMENTO,
                                      STATO,
                                      DATA_AGGIORNAMENTO,
                                      UTENTE_AGGIORNAMENTO)
              VALUES (STDO_SQ.NEXTVAL,
                      A_DOC,
                      'CA',
                      SYSDATE,
                      A_USER);
      END IF;

      UPDATE DOCUMENTI
         SET STATO_DOCUMENTO = 'CA'
       WHERE ID_DOCUMENTO = A_DOC;

      tmpVar := 1;
   EXCEPTION
      WHEN OTHERS
      THEN
         IF a_roll = 1
         THEN
            ROLLBACK;
         END IF;

         RAISE_APPLICATION_ERROR ('-20990', SQLERRM);
   END;

   RETURN TMPVAR;
END F_Elimina_Documento_Logico;
/


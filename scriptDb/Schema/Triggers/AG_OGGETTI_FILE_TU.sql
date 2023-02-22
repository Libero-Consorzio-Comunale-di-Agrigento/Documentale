CREATE OR REPLACE TRIGGER AG_OGGETTI_FILE_TU
   BEFORE UPDATE
   ON OGGETTI_FILE
   FOR EACH ROW
DECLARE
   d_id_formato   NUMBER;
BEGIN
   IF :NEW.FILENAME <> :OLD.FILENAME
   THEN
      UPDATE impronte_file
         SET filename = :NEW.FILENAME
       WHERE id_documento = :NEW.id_documento AND filename = :OLD.FILENAME;

      IF UPPER (
            NVL (SUBSTR (:OLD.FILENAME, INSTR (:OLD.FILENAME, '.', -1) + 1),
                 ' ')) <>
            UPPER (
               NVL (
                  SUBSTR (:NEW.FILENAME, INSTR (:NEW.FILENAME, '.', -1) + 1),
                  ' '))
      THEN                                               -- ESTENSIONE DIVERSA
         BEGIN
            SELECT ID_FORMATO
              INTO d_id_formato
              FROM FORMATI_FILE
             WHERE     UPPER (NOME) =
                          UPPER (
                             NVL (
                                SUBSTR (:NEW.FILENAME,
                                        INSTR (:NEW.FILENAME, '.', -1) + 1),
                                ' '))
                   AND ROWNUM = 1;
         EXCEPTION
            WHEN NO_DATA_FOUND
            THEN
               d_id_formato := 0;
            WHEN OTHERS
            THEN
               RAISE;
         END;

         :new.id_formato := d_id_formato;
      END IF;
   END IF;
EXCEPTION
   WHEN OTHERS
   THEN
      RAISE;
END;
/



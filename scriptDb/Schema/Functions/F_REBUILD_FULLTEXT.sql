CREATE OR REPLACE FUNCTION F_REBUILD_FULLTEXT (A_NOME_TABELLA VARCHAR2, A_SOLOVUOTI NUMBER) RETURN NUMBER
IS
TYPE SelCur is ref cursor;
utentiCur SelCur;
selString VARCHAR2(1000);
stringErr VARCHAR2(4000);
idDoc     DOCUMENTI.ID_DOCUMENTO%TYPE;
BEGIN
   selString := 'select id_documento from '||A_NOME_TABELLA;
   IF NVL(A_SOLOVUOTI,0)=1 THEN
      selString := selString || ' where full_text is null';
   END IF;
   OPEN UtentiCur for selString;
   LOOP
       FETCH UtentiCur INTO idDoc;
       EXIT WHEN UtentiCur%NOTFOUND;
       BEGIN
            stringErr:=F_FULL_TEXT_HORIZ(idDoc,A_NOME_TABELLA);
            IF length(stringErr)>0 THEN
                raise_application_error (-20999,stringErr);
            END IF;
       EXCEPTION
         WHEN OTHERS
         THEN
            ROLLBACK;
            raise_application_error (
               '-20999',
                  'IMPOSSIBILE AGGIORNARE il documento '
               || idDoc
               || ' '
               || SQLERRM);
            RETURN -1;
       END;
      COMMIT;
   END LOOP;
   CLOSE UtentiCur;
   RETURN 0;
END  F_REBUILD_FULLTEXT;
/


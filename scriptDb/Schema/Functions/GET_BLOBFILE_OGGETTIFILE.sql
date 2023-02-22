CREATE OR REPLACE FUNCTION get_blobfile_oggettifile (
   p_id_oggettofile   IN NUMBER)
   RETURN BLOB
IS
   -- --------------------------------------------------------------------------
   PRAGMA AUTONOMOUS_TRANSACTION;
   l_data   BLOB;
   l_fd     UTL_FILE.FILE_TYPE;
   l_buff   RAW (32767);
   p_dir    VARCHAR2 (2000);
   p_esisteblob number(1):=1;
BEGIN

    BEGIN
    SELECT TESTOOCR
       INTO l_data
      FROM oggetti_file
    WHERE PATH_FILE IS NULL AND ID_OGGETTO_FILE=p_id_oggettofile;
    EXCEPTION WHEN NO_DATA_FOUND THEN
       p_esisteblob:=0;
                    WHEN OTHERS THEN
        RAISE;
    END;

    IF p_esisteblob=1 THEN
        RETURN l_data;
    END IF;

   SELECT F_GETPATH_FILE_FS (id_documento, id_oggetto_file,1), testoocr
    INTO p_dir, l_data
     FROM oggetti_file
    WHERE id_oggetto_file = p_id_oggettofile;

   IF l_data IS NULL
   THEN
      EXECUTE IMMEDIATE
         'create or replace directory TEMP_FILE_X as  ''' || p_dir || '''';

      DBMS_LOB.createtemporary (lob_loc   => l_data,
                                cache     => TRUE,
                                dur       => DBMS_LOB.call);

      l_fd :=
         UTL_FILE.FOPEN ('TEMP_FILE_X',
                         p_id_oggettofile,
                         'RB',
                         32767);

      LOOP
         BEGIN
            UTL_FILE.GET_RAW (l_fd, l_buff);
            EXIT WHEN l_buff IS NULL;

            DBMS_LOB.WRITEAPPEND (l_data, UTL_RAW.LENGTH (l_buff), l_buff);
         EXCEPTION
            WHEN NO_DATA_FOUND
            THEN
               EXIT;
         END;
      END LOOP;

      UTL_FILE.FCLOSE (l_fd);
   END IF;

   RETURN l_data;
END;
/


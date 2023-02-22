CREATE OR REPLACE PROCEDURE P_MANAGE_ORACLEDIR(A_ACR_AREA VARCHAR2, A_ACTION VARCHAR2, A_PATH VARCHAR2 DEFAULT NULL, A_ID_PATH_AREE VARCHAR2 DEFAULT NULL) IS
tmpVar NUMBER;
A_PATH_ORACLE VARCHAR2(1000);
A_ACRONIMO      VARCHAR2(1000);
BEGIN
   A_PATH_ORACLE := A_PATH;
   A_ACRONIMO := A_ACR_AREA;
   IF A_PATH_ORACLE IS NULL THEN
       A_PATH_ORACLE:='/jdocattach/DOCUMENTALE';
   END IF;

   IF A_ID_PATH_AREE IS NOT NULL THEN
       DECLARE
           A_ACRONIMO_AREA_PATH VARCHAR2(100);
       BEGIN
           SELECT PATH_FILE_ORACLE, PREFIX_ACRONIMO_DIRECTORY
           INTO A_PATH_ORACLE, A_ACRONIMO_AREA_PATH
           FROM AREE_PATH
           WHERE ID_PATH_AREE_FILE= A_ID_PATH_AREE;

           A_ACRONIMO := A_ACRONIMO || '_'||A_ACRONIMO_AREA_PATH;
        END;
   END IF;



   IF A_ACTION='I' THEN
        EXECUTE IMMEDIATE 'create or replace directory DIR_'||A_ACRONIMO||' as '''||A_PATH_ORACLE||'/'||A_ACR_AREA||'''';
   --ELSE
   --     EXECUTE IMMEDIATE 'drop directory DIR_'||A_ACRONIMO||'';
   END IF;
   EXCEPTION
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       NULL;
END P_MANAGE_ORACLEDIR;
/

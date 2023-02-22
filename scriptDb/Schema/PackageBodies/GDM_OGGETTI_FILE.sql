CREATE OR REPLACE PACKAGE BODY gdm_oggetti_file
AS
    PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER,A_PULISCIBLOB NUMBER,A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX')
    AS
    BEGIN
          OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE, -1,A_PULISCIBLOB,0,EMPTY_BLOB(),A_TYPE_FILESYS);
    END;
    PROCEDURE OGGETTO_FILE_TO_FS_NOCOMMIT(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX')
    AS
    A_ESISTE NUMBER(1):=1;
    A_PATH_FILE_ORACLE               VARCHAR2(1000);
    A_PATH_FILE                             VARCHAR2(1000);
    A_ACR_AREA                            VARCHAR2(50);
    A_ACR_AREA_PATH                   VARCHAR2(100);
    A_ACR_MODELLO                      VARCHAR2(50);
    A_IDDOC_FRATTOMILLE            VARCHAR2(50);
    A_PERCORSO                           VARCHAR2(1000);
    A_IDDOCUMENTO                     NUMBER(10);
    A_BLOB                                   BLOB;
    A_ACR_AREA_DIR_ORACLE  VARCHAR2(200);

   l_pos INTEGER := 1;
   l_blob_len INTEGER;
   l_file UTL_FILE.FILE_TYPE;
   l_buffer RAW(32767);
   l_amount BINARY_INTEGER := 32767;
   l_separator varchar2(1) :='/';
    BEGIN
        BEGIN
           IF A_TYPE_FILESYS<>'LNX' THEN
               l_separator:='\';
           END IF;

            IF A_ID_LOG=-1 THEN
                --CASO OGGETTO_FILE

                SELECT decode(aree_path.ID_PATH_AREE_FILE,null,  aree.path_file_oracle,  aree_path.path_file_oracle), AREE.ACRONIMO,TIPI_DOCUMENTO.ACRONIMO_MODELLO, TO_CHAR(TRUNC(DOCUMENTI.ID_DOCUMENTO/1000)) ,DOCUMENTI.ID_DOCUMENTO, TESTOOCR,
                            decode(aree_path.ID_PATH_AREE_FILE,null,  aree.path_file,  aree_path.path_file), PREFIX_ACRONIMO_DIRECTORY
                   INTO A_PATH_FILE_ORACLE,A_ACR_AREA, A_ACR_MODELLO,A_IDDOC_FRATTOMILLE,A_IDDOCUMENTO,A_BLOB, A_PATH_FILE, A_ACR_AREA_PATH
                FROM TIPI_DOCUMENTO, AREE , OGGETTI_FILE, DOCUMENTI, AREE_PATH
                WHERE OGGETTI_FILE.ID_OGGETTO_FILE=A_IDOGGETTOFILE AND
                            DOCUMENTI.ID_DOCUMENTO=OGGETTI_FILE.ID_DOCUMENTO AND
                            DOCUMENTI.AREA = AREE.AREA AND
                            DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND
                            (OGGETTI_FILE.PATH_FILE IS NULL) AND
                            (decode(aree_path.ID_PATH_AREE_FILE,null,  aree.path_file_oracle,  aree_path.path_file_oracle) IS NOT NULL) AND
                            (AREE.ACRONIMO IS NOT NULL) AND
                            (TIPI_DOCUMENTO.ACRONIMO_MODELLO IS NOT NULL) AND
                            decode(A_USABLOB,1,1,nvl(DBMS_LOB.getlength(TESTOOCR),0))>0 AND
                            aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+)
                            ;
            ELSE
                --CASO OGGETTO_FILE_LOG
                SELECT decode(aree_path.ID_PATH_AREE_FILE,null,  aree.path_file_oracle,  aree_path.path_file_oracle), AREE.ACRONIMO,TIPI_DOCUMENTO.ACRONIMO_MODELLO, TO_CHAR(TRUNC(DOCUMENTI.ID_DOCUMENTO/1000)) ,DOCUMENTI.ID_DOCUMENTO,TESTOOCR,
                             decode(aree_path.ID_PATH_AREE_FILE,null,  aree.path_file,  aree_path.path_file), PREFIX_ACRONIMO_DIRECTORY
                   INTO A_PATH_FILE_ORACLE,A_ACR_AREA, A_ACR_MODELLO,A_IDDOC_FRATTOMILLE, A_IDDOCUMENTO,A_BLOB, A_PATH_FILE,A_ACR_AREA_PATH
                FROM TIPI_DOCUMENTO, AREE , OGGETTI_FILE_LOG, DOCUMENTI, ACTIVITY_LOG, AREE_PATH
                WHERE OGGETTI_FILE_LOG.ID_OGGETTO_FILE=A_IDOGGETTOFILE AND
                            OGGETTI_FILE_LOG.ID_LOG=A_ID_LOG AND
                            ACTIVITY_LOG.ID_LOG = OGGETTI_FILE_LOG.ID_LOG AND
                            DOCUMENTI.ID_DOCUMENTO=ACTIVITY_LOG.ID_DOCUMENTO AND
                            DOCUMENTI.AREA = AREE.AREA AND
                            DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND
                            (OGGETTI_FILE_LOG.PATH_FILE IS NULL) AND
                            (decode(aree_path.ID_PATH_AREE_FILE,null,  aree.path_file_oracle,  aree_path.path_file_oracle) IS NOT NULL) AND
                            (AREE.ACRONIMO IS NOT NULL) AND
                            (TIPI_DOCUMENTO.ACRONIMO_MODELLO IS NOT NULL)
                            AND rownum=1    AND
                            decode(A_USABLOB,1,1,nvl(DBMS_LOB.getlength(TESTOOCR),0))>0 AND
                            aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+)
                            ;
                NULL;
            END IF;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            A_ESISTE:=0;
        END;

        IF A_ESISTE=1 THEN
            IF A_USABLOB=1 THEN
                A_BLOB := A_USAQUESTOBLOB;
            END IF;

            A_PERCORSO:=A_PATH_FILE_ORACLE||l_separator||A_ACR_AREA;
           -- GDM_UTILITY.MKDIR(A_PERCORSO);
           -- GDM_UTILITY.CHMOD(A_PERCORSO);

            A_PERCORSO:=A_PERCORSO||l_separator||A_ACR_MODELLO;
          --  GDM_UTILITY.MKDIR(A_PERCORSO);
          --  GDM_UTILITY.CHMOD(A_PERCORSO);

            A_PERCORSO:=A_PERCORSO||l_separator||A_IDDOC_FRATTOMILLE;
           -- GDM_UTILITY.MKDIR(A_PERCORSO);
          --  GDM_UTILITY.CHMOD(A_PERCORSO);

            A_PERCORSO:=A_PERCORSO||l_separator||A_IDDOCUMENTO;
          --  GDM_UTILITY.MKDIR(A_PERCORSO);
          --  GDM_UTILITY.CHMOD(A_PERCORSO);



            IF A_ID_LOG<>-1 THEN
                A_PERCORSO:=A_PERCORSO||l_separator||'LOG_'||TO_CHAR(A_ID_LOG);

               -- GDM_UTILITY.MKDIR(A_PERCORSO);
               -- GDM_UTILITY.CHMOD(A_PERCORSO);
            END IF;

          GDM_UTILITY.MKDIR(A_PERCORSO);
          IF A_ACR_AREA_PATH IS NULL THEN
                A_ACR_AREA_DIR_ORACLE := A_ACR_AREA;
          ELSE
                A_ACR_AREA_DIR_ORACLE := A_ACR_AREA||'_'||A_ACR_AREA_PATH;
          END IF;

           BEGIN
                     execute immediate 'create or replace directory DIR_'||A_ACR_AREA_DIR_ORACLE||' as '''||A_PATH_FILE_ORACLE||l_separator||A_ACR_AREA||'''';
            EXCEPTION WHEN OTHERS THEN
                 RAISE_APPLICATION_ERROR(-20999,'Errore in creazione dir oracle di area per path '||A_PATH_FILE_ORACLE||l_separator||A_ACR_AREA||': '||sqlerrm);
            END;


           BEGIN
              execute immediate 'create or replace directory TEMP_FILE as '''||A_PERCORSO||'''';
           EXCEPTION WHEN OTHERS THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore in creazione dir oracle temporanea per path '||A_PERCORSO||': '||sqlerrm);
           END;

           l_blob_len := DBMS_LOB.getlength(A_BLOB);
           l_pos:= 1;

           BEGIN
              l_file := UTL_FILE.fopen('TEMP_FILE',A_IDOGGETTOFILE,'wb', 32767);
                -- dbms_output.put_line(l_blob_len);
              WHILE l_pos <= l_blob_len LOOP
                    DBMS_LOB.read(A_BLOB, l_amount, l_pos, l_buffer);
                    UTL_FILE.put_raw(l_file, l_buffer, TRUE);
                    --dbms_output.put_line(l_amount);
                    l_pos := l_pos + l_amount;

                     --dbms_output.put_line(l_pos);
              END LOOP;
              UTL_FILE.fclose(l_file);
           EXCEPTION WHEN OTHERS THEN
              RAISE_APPLICATION_ERROR(-20999,'Errore in scrittura file '||A_PERCORSO||l_separator||A_IDOGGETTOFILE||': '||sqlerrm);
           END;


           --IF A_TYPE_FILESYS='LNX' THEN
           -- GDM_UTILITY.CHMOD(A_PERCORSO||l_separator||A_IDOGGETTOFILE);
           --END IF;

           --Controllo esistenza e dimensione del file corretti
           DECLARE
           A_FAKE   NUMBER(1);
           A_PATH   VARCHAR2(32000);
           BEGIN
                A_PATH := A_ACR_MODELLO||l_separator||A_IDDOC_FRATTOMILLE||l_separator||A_IDDOCUMENTO;
                 IF A_ID_LOG<>-1 THEN
                 A_PATH := A_PATH || l_separator||'LOG_'||TO_CHAR(A_ID_LOG);
                 END IF;

                 A_PATH := A_PATH || l_separator ||A_IDOGGETTOFILE;

                SELECT 1
                INTO A_FAKE
                FROM DUAL
                WHERE  dbms_lob.fileexists(bfilename('DIR_'||A_ACR_AREA_DIR_ORACLE,A_PATH))<>0  AND
                            dbms_lob.getlength(bfilename('DIR_'||A_ACR_AREA_DIR_ORACLE,A_PATH))=l_blob_len;
           EXCEPTION WHEN NO_DATA_FOUND THEN
                    RAISE_APPLICATION_ERROR(-20999,'Il file non esiste sul file system dopo il trasporto, oppure la dimensione dei file tra db e fs non ¿ la stessa. Verificare. Il file su FS si trova qui: '||
                                                                            A_PERCORSO||l_separator||A_IDOGGETTOFILE);
                            WHEN OTHERS THEN
                   RAISE_APPLICATION_ERROR(-20999,'Errore nel controllo presenza e dimensione e file su FS dopo il trasporto. Il file su FS si trova qui: '||
                                                                            A_PERCORSO||l_separator||A_IDOGGETTOFILE||' - Errore: '||sqlerrm);
           END;

           IF A_PULISCIBLOB=1 THEN
              BEGIN
                  IF A_ID_LOG=-1 THEN
                  --null;
                     --METTO LA DATA_ULTIMA_ESTERNALIZZAZIONE A 01/01/2900 FAKE IN MODO CHE QUESTA UPDATE NON MI MODIFICHI LA DATA_AGGIORNAMENTO DELL'OGGETTO FILE.
                      --TANTO POI PASSERA' IL TRIGGER (_PU) CHE METTERA LA DATA_ULTIMA_ESTERNALIZZAZIONE A SYSDATE
                      UPDATE OGGETTI_FILE SET PATH_FILE=A_ACR_AREA, TESTOOCR=NULL,DATA_ULTIMA_ESTERNALIZZAZIONE = TO_DATE('01/01/2900','dd/mm/yyyy'), PATH_FILE_ROOT = A_PATH_FILE , PATH_FILE_ROOT_ORACLE=A_PATH_FILE_ORACLE WHERE ID_OGGETTO_FILE=A_IDOGGETTOFILE;
                 ELSE
                   --null;
                      UPDATE OGGETTI_FILE_LOG SET PATH_FILE=A_ACR_AREA||'\LOG_'||TO_CHAR(A_ID_LOG), TESTOOCR=NULL, PATH_FILE_ROOT = A_PATH_FILE , PATH_FILE_ROOT_ORACLE=A_PATH_FILE_ORACLE WHERE ID_OGGETTO_FILE=A_IDOGGETTOFILE AND ID_LOG=A_ID_LOG;
                 END IF;

              EXCEPTION WHEN OTHERS THEN
                 RAISE_APPLICATION_ERROR(-20999,'Errore in aggiornamento path_file e annullamento blob su oggetti file: '||sqlerrm);
              END;
           END IF;

        END IF;



    EXCEPTION WHEN OTHERS  THEN
       RAISE_APPLICATION_ERROR(-20999,'Errore in OGGETTO_FILE_TO_FS per IDOGGETTOFILE='||A_IDOGGETTOFILE||' e ID_LOG '||A_ID_LOG||'. Errore: '||sqlerrm);
    END;

    PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX')
    AS
   PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        OGGETTO_FILE_TO_FS_NOCOMMIT(A_IDOGGETTOFILE,A_ID_LOG,A_PULISCIBLOB,A_USABLOB,A_USAQUESTOBLOB,A_TYPE_FILESYS);
        COMMIT;
    EXCEPTION WHEN OTHERS  THEN
        ROLLBACK;
       RAISE;
    END;
    FUNCTION IS_FS_FILE(P_IDOGGETTO_FILE NUMBER,P_ISLOG NUMBER DEFAULT 0) RETURN NUMBER
    IS
    A_RET NUMBER(1) := 0;
    BEGIN
        IF P_ISLOG=0 THEN
            SELECT DECODE(PATH_FILE,NULL, 0 ,DECODE(PATH_FILE,'',0,1))
               INTO  A_RET
               FROM OGGETTI_FILE
               WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;
        ELSE
            SELECT DECODE(PATH_FILE,NULL, 0 ,DECODE(PATH_FILE,'',0,1))
               INTO  A_RET
               FROM OGGETTI_FILE_LOG
               WHERE ID_OGGETTO_FILE_LOG = P_IDOGGETTO_FILE;
        END IF;

        RETURN A_RET;
    END;
    PROCEDURE GETPATH_FILE_FS( P_IDOGGETTO_FILE NUMBER, P_DIRECTORY IN OUT VARCHAR2, P_PATH_DIR_FS IN OUT  VARCHAR2 ,  P_PATH_FILE IN OUT  VARCHAR2, P_ISLOG NUMBER DEFAULT 0  )
    AS
        P_PATH_FILE_NO_ROOT  VARCHAR2(1000);
        P_PATH_FILE_DA_AREA   VARCHAR2(1000);
     BEGIN
         GETPATH_FILE_FS( P_IDOGGETTO_FILE , P_DIRECTORY  , P_PATH_DIR_FS  ,  P_PATH_FILE , P_PATH_FILE_NO_ROOT ,P_PATH_FILE_DA_AREA, P_ISLOG   );
    END;
    PROCEDURE GETPATH_FILE_FS( P_IDOGGETTO_FILE NUMBER, P_DIRECTORY IN OUT VARCHAR2, P_PATH_DIR_FS IN OUT  VARCHAR2 ,  P_PATH_FILE IN OUT  VARCHAR2, P_PATH_FILE_CON_ROOT IN OUT  VARCHAR2,P_PATH_FILE_DA_AREA IN OUT  VARCHAR2,  P_ISLOG NUMBER  )
        AS
        A_PATH_ROOT_IN_OGFI  VARCHAR2(1000);
        A_PATH_ROOT_IN_AREE  VARCHAR2(1000);
        A_PATH_ROOT_SCELTO   VARCHAR2(1000);
        A_ACR_AREA                    VARCHAR2(100);
        BEGIN
            IF P_ISLOG=0 THEN
                select F_GETDIRECTORY_AREA_NAME(DOCUMENTI.ID_DOCUMENTO),
                        TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' ||
                         DOCUMENTI.ID_DOCUMENTO ||  '/' || P_IDOGGETTO_FILE,
                         OGGETTI_FILE.PATH_FILE_ROOT_ORACLE, AREE.PATH_FILE_ORACLE, AREE.ACRONIMO
                   into P_DIRECTORY,P_PATH_FILE,A_PATH_ROOT_IN_OGFI, A_PATH_ROOT_IN_AREE, A_ACR_AREA
                  from TIPI_DOCUMENTO, AREE, DOCUMENTI, OGGETTI_FILE
                WHERE      DOCUMENTI.AREA=AREE.AREA
                         AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC
                         AND OGGETTI_FILE.ID_OGGETTO_FILE  = P_IDOGGETTO_FILE
                         AND OGGETTI_FILE.ID_DOCUMENTO = DOCUMENTI.ID_DOCUMENTO;
             ELSE
                select F_GETDIRECTORY_AREA_NAME(DOCUMENTI.ID_DOCUMENTO),
                         TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' ||
                         DOCUMENTI.ID_DOCUMENTO ||  '/LOG_' || TO_CHAR(OGGETTI_FILE_LOG.ID_LOG) || '/' || OGGETTI_FILE_LOG.ID_OGGETTO_FILE,
                         OGGETTI_FILE_LOG.PATH_FILE_ROOT_ORACLE,AREE.PATH_FILE_ORACLE, AREE.ACRONIMO
                   into P_DIRECTORY,P_PATH_FILE,A_PATH_ROOT_IN_OGFI, A_PATH_ROOT_IN_AREE, A_ACR_AREA
                  from TIPI_DOCUMENTO, AREE, DOCUMENTI, ACTIVITY_LOG, OGGETTI_FILE_LOG
                WHERE      DOCUMENTI.AREA=AREE.AREA
                         AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC
                         AND ACTIVITY_LOG.ID_LOG = OGGETTI_FILE_LOG.ID_LOG
                         AND ACTIVITY_LOG.ID_DOCUMENTO = DOCUMENTI.ID_DOCUMENTO
                         AND OGGETTI_FILE_LOG.ID_OGGETTO_FILE_LOG =P_IDOGGETTO_FILE;
             END IF;


             IF  A_PATH_ROOT_IN_OGFI IS NULL THEN
                  A_PATH_ROOT_SCELTO := A_PATH_ROOT_IN_AREE;
                  P_PATH_FILE_CON_ROOT :=   A_PATH_ROOT_IN_AREE||'/'||A_ACR_AREA||'/'||P_PATH_FILE;
            ELSE
                 A_PATH_ROOT_SCELTO := A_PATH_ROOT_IN_OGFI;
                  P_PATH_FILE_CON_ROOT :=   A_PATH_ROOT_IN_OGFI||'/'||A_ACR_AREA||'/'||P_PATH_FILE;
            END IF;

            P_PATH_FILE_DA_AREA:=A_ACR_AREA||'/'||P_PATH_FILE;

              BEGIN
                SELECT  directory_path
               INTO P_PATH_DIR_FS
               FROM ALL_DIRECTORIES
               WHERE  upper(directory_name) = P_DIRECTORY;
              EXCEPTION WHEN NO_DATA_FOUND THEN
                   P_PATH_DIR_FS := '';
              END;


               SELECT max( directory_name)
                 INTO P_DIRECTORY
                FROM DBA_DIRECTORIES
                  WHERE  directory_path = A_PATH_ROOT_SCELTO ||'/'||A_ACR_AREA;



    END;
    PROCEDURE DELETEOGGETTOFILE(P_IDOGGETTO_FILE NUMBER)
    AS
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_dir_fs_conroot VARCHAR2(1000);
    a_path_dir_fs_daarea VARCHAR2(1000);
    a_path_file VARCHAR2(1000);
    a_isFileFs NUMBER(1) :=0;
    BEGIN
        IF IS_FS_FILE(P_IDOGGETTO_FILE)=1 THEN
            GETPATH_FILE_FS( P_IDOGGETTO_FILE, a_dir,a_path_dir_fs ,  a_path_file,a_path_dir_fs_conroot,a_path_dir_fs_daarea,0 );
            a_isFileFs:=1;
        END IF;

      DELETE FROM OGGETTI_FILE WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;

       IF a_isFileFs=1 THEN
             --SYS.DBMS_BACKUP_RESTORE.DELETEFILE(a_path_dir_fs||'/'||replace(a_path_file,'$','\$'));
             gdm_utility.RMDIR( a_path_dir_fs_conroot  ,1);
        END IF;
    END;
    FUNCTION DOWNLOADOGGETTOFILE_LOG(P_IDOGGETTO_FILE_LOG NUMBER) RETURN BLOB
    IS
    RET BLOB := EMPTY_BLOB();
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_dir_fs_conroot VARCHAR2(1000);
    a_path_dir_fs_daarea VARCHAR2(1000);
    a_path_file VARCHAR2(1000);
    fils       BFILE ;
    BEGIN
        IF IS_FS_FILE(P_IDOGGETTO_FILE_LOG,1)=1 THEN
             GETPATH_FILE_FS( P_IDOGGETTO_FILE_LOG, a_dir,a_path_dir_fs ,  a_path_file,a_path_dir_fs_conroot,a_path_dir_fs_daarea,1 );
              fils  := BFILENAME(a_dir,a_path_file);
              if  nvl(dbms_lob.getlength(fils),0)>0 then
                  dbms_lob.fileopen(fils, dbms_lob.file_readonly);
                   DBMS_LOB.CREATETEMPORARY(RET,TRUE,dbms_lob.call);
                  dbms_lob.loadfromfile(RET, fils,DBMS_LOB.LOBMAXSIZE);
                  dbms_lob.fileclose(fils);
              ELSE
                    RET:=NULL;
              end if;
        ELSE
            SELECT TESTOOCR
                INTO RET
               FROM OGGETTI_FILE_LOG
             WHERE ID_OGGETTO_FILE_LOG = P_IDOGGETTO_FILE_LOG;

             if  nvl(dbms_lob.getlength(RET),0)=0 then
                 RET := null;
             end if;
        END IF;


        RETURN RET;
    END;

    FUNCTION DOWNLOADOGGETTOFILE(P_IDOGGETTO_FILE NUMBER) RETURN BLOB
    IS
    RET BLOB := EMPTY_BLOB();
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_dir_fs_conroot VARCHAR2(1000);
    a_path_dir_fs_daarea VARCHAR2(1000);
    a_path_file VARCHAR2(1000);
    fils       BFILE ;
    BEGIN
        IF IS_FS_FILE(P_IDOGGETTO_FILE)=1 THEN

              GETPATH_FILE_FS( P_IDOGGETTO_FILE, a_dir,a_path_dir_fs , a_path_file , a_path_dir_fs_conroot,a_path_dir_fs_daarea,0);
          --    raise_application_error(-20999,a_dir|| ' xxxxx  | '|| a_path_dir_fs_daarea);
              fils  := BFILENAME(a_dir,a_path_file);
              if  nvl(dbms_lob.getlength(fils),0)>0 then
                  dbms_lob.fileopen(fils, dbms_lob.file_readonly);
                   DBMS_LOB.CREATETEMPORARY(RET,TRUE,dbms_lob.call);
                  dbms_lob.loadfromfile(RET, fils,DBMS_LOB.LOBMAXSIZE);
                  dbms_lob.fileclose(fils);
              ELSE
                    RET:=NULL;
              end if;
        ELSE
            SELECT TESTOOCR
                INTO RET
               FROM OGGETTI_FILE
             WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;

             if  nvl(dbms_lob.getlength(RET),0)=0 then
                 RET := null;
             end if;
        END IF;


        RETURN RET;
    END DOWNLOADOGGETTOFILE;
    PROCEDURE DOWNLOADOGGETTOFILE_TMP(P_IDOGGETTO_FILE NUMBER)
    AS
    RET BLOB := EMPTY_BLOB();
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_dir_fs_conroot VARCHAR2(1000);
    a_path_dir_fs_daarea VARCHAR2(1000);
    a_path_file VARCHAR2(1000);
    fils       BFILE ;
    PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
         execute immediate 'TRUNCATE TABLE TMP_FILE DROP STORAGE';
         insert into  TMP_FILE (FILE_TEMPORANY) values (EMPTY_BLOB());
         COMMIT;

       IF IS_FS_FILE(P_IDOGGETTO_FILE)=1 THEN
              GETPATH_FILE_FS( P_IDOGGETTO_FILE, a_dir,a_path_dir_fs ,  a_path_file,a_path_dir_fs_conroot,a_path_dir_fs_daarea,0 );

              fils  := BFILENAME(a_dir,a_path_file);
              if  nvl(dbms_lob.getlength(fils),0)>0 then

                    SELECT FILE_TEMPORANY INTO RET FROM TMP_FILE  FOR UPDATE;
                  dbms_lob.fileopen(fils, dbms_lob.file_readonly);
                 --  DBMS_LOB.CREATETEMPORARY(RET,TRUE,dbms_lob.call);
                  dbms_lob.loadfromfile(RET, fils,DBMS_LOB.LOBMAXSIZE);
                  dbms_lob.fileclose(fils);
              ELSE
                    RET:=NULL;
              end if;
        ELSE
            SELECT TESTOOCR
                INTO RET
               FROM OGGETTI_FILE
             WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;

             if  nvl(dbms_lob.getlength(RET),0)=0 then
                 RET := null;
             end if;
        END IF;

        IF RET IS NOT NULL THEN
            update TMP_FILE
            set FILE_TEMPORANY=RET;

            BEGIN
                  dbms_lob.fileclose(fils);
            EXCEPTION WHEN OTHERS THEN
                    NULL;
            END;
        END IF;

        COMMIT;


    EXCEPTION WHEN OTHERS THEN
        BEGIN
              dbms_lob.fileclose(fils);
        EXCEPTION WHEN OTHERS THEN
                NULL;
        END;
        ROLLBACK;
        RAISE;
    END DOWNLOADOGGETTOFILE_TMP;
END;
/


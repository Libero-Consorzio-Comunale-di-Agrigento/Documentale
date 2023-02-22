CREATE OR REPLACE PACKAGE gdm_oggetti_file
AS
/*
   Per il corretto funzionamento e' necessario lanciare come sys:
   grant select on dba_directories to gdm;
   grant execute on dbms_backup_restore to gdm;
*/
PROCEDURE GETPATH_FILE_FS(P_IDOGGETTO_FILE NUMBER, P_DIRECTORY IN OUT VARCHAR2, P_PATH_DIR_FS IN OUT  VARCHAR2 ,  P_PATH_FILE IN OUT  VARCHAR2,  P_ISLOG NUMBER DEFAULT 0 );
PROCEDURE GETPATH_FILE_FS( P_IDOGGETTO_FILE NUMBER, P_DIRECTORY IN OUT VARCHAR2, P_PATH_DIR_FS IN OUT  VARCHAR2 ,  P_PATH_FILE IN OUT  VARCHAR2, P_PATH_FILE_CON_ROOT IN OUT  VARCHAR2,P_PATH_FILE_DA_AREA IN OUT  VARCHAR2, P_ISLOG NUMBER  );
FUNCTION IS_FS_FILE(P_IDOGGETTO_FILE NUMBER,P_ISLOG NUMBER DEFAULT 0) RETURN NUMBER;
PROCEDURE DELETEOGGETTOFILE(P_IDOGGETTO_FILE NUMBER);
FUNCTION DOWNLOADOGGETTOFILE(P_IDOGGETTO_FILE NUMBER) RETURN BLOB;
FUNCTION DOWNLOADOGGETTOFILE_LOG(P_IDOGGETTO_FILE_LOG NUMBER) RETURN BLOB;
PROCEDURE DOWNLOADOGGETTOFILE_TMP(P_IDOGGETTO_FILE NUMBER);
PROCEDURE OGGETTO_FILE_TO_FS_NOCOMMIT(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER  DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX');
PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER  DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX');
PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER,A_PULISCIBLOB NUMBER,A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX');
END;
/

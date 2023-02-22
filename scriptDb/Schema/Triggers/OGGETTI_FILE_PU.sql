CREATE OR REPLACE TRIGGER OGGETTI_FILE_PU
AFTER INSERT OR UPDATE
ON OGGETTI_FILE
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
DECLARE
TMPVAR NUMBER;
/******************************************************************************
   NAME:       OGGETTI_FILE_PU
   PURPOSE:
   REVISIONS:
   VER        DATE        AUTHOR           DESCRIPTION
   ---------  ----------  ---------------  ------------------------------------
   1.0        19/12/2007             1. CREATED THIS TRIGGER.
   NOTES:
   AUTOMATICALLY AVAILABLE AUTO REPLACE KEYWORDS:
      OBJECT NAME:     OGFI_TIU
      SYSDATE:         19/12/2007
      DATE AND TIME:   19/12/2007, 15.39.36, AND 19/12/2007 15.39.36
      USERNAME:         (SET IN TOAD OPTIONS, PROC TEMPLATES)
      TABLE NAME:      OGGETTI_FILE (SET IN THE "NEW PL/SQL OBJECT" DIALOG)
      TRIGGER OPTIONS:  (SET IN THE "NEW PL/SQL OBJECT" DIALOG)
******************************************************************************/
  A_DIRAREA_NAME   VARCHAR2(30);
  A_DIRAREA_NAME_STORICA VARCHAR2(30) := NULL;
  A_CARTELLA       VARCHAR2(100);
  INTEGRITY_ERROR  EXCEPTION;
  ERRNO            INTEGER;
  ERRMSG           CHAR(200);
  DUMMY            INTEGER;
  FOUND            BOOLEAN;
  A_MESSAGGIO      VARCHAR2(2000);
  A_ISTR_BFILE     VARCHAR2(2000) := ' ';
  A_ISTR_OCR       VARCHAR2(2000) := ' ';
  A_ISTR_DATA_ESTERNALIZZ       VARCHAR2(2000) := ' ';
  A_CHECKOCR       NUMBER(1);
BEGIN
 BEGIN
      -- SET FUNCTIONAL INTEGRITY
      IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 THEN
         IF ( NVL(:OLD.OCR_PENDING,0)=0 AND NVL(:NEW.OCR_PENDING,-1)=NVL(:OLD.OCR_PENDING,-1)
            AND NVL(:NEW.DA_CANCELLARE,'N')='N' AND GDM_OCR.CHECK_GEN_OCR_PARAMETRI=1)
            AND  NVL(:NEW.OCR_FILE,' ')=NVL(:OLD.OCR_FILE,' ')
            THEN
            A_CHECKOCR := GDM_OCR.CHECK_GEN_OCR_OGFI(:NEW.FILENAME, :NEW.ID_DOCUMENTO );

            IF A_CHECKOCR=1 THEN
               A_ISTR_OCR := ' OCR_PENDING = 1 ';
            END IF;
         END IF;


         -- INTEGRITYPACKAGE.NEXTNESTLEVEL;
         BEGIN  -- GLOBAL FUNCTIONAL INTEGRITY AT LEVEL 0
            if NVL(:NEW.PATH_FILE ,' ')<> ' '  AND NVL(:NEW.PATH_FILE ,' ')<>NVL(:OLD.PATH_FILE ,' ')  /*and length(TO_CHAR(:NEW.ID_DOCUMENTO) || '/' || :NEW.FILENAME)<=50*/ THEN

              IF  NVL(:NEW.PATH_FILE_ROOT_ORACLE ,' ')<> ' ' THEN
                      BEGIN
                          SELECT max(directory_name)
                            INTO A_DIRAREA_NAME_STORICA
                            FROM DBA_DIRECTORIES
                           WHERE directory_path = :NEW.PATH_FILE_ROOT_ORACLE||'/'||:NEW.PATH_FILE;
                       EXCEPTION WHEN NO_DATA_FOUND THEN
                          A_DIRAREA_NAME_STORICA := NULL;
                       END;
              END IF;


              select F_GETDIRECTORY_AREA_NAME(:NEW.ID_DOCUMENTO),
                    TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || DOCUMENTI.ID_DOCUMENTO || '/' || :NEW.ID_OGGETTO_FILE
              into A_DIRAREA_NAME,A_CARTELLA
              from TIPI_DOCUMENTO, AREE, DOCUMENTI
              WHERE DOCUMENTI.ID_DOCUMENTO=:NEW.ID_DOCUMENTO AND DOCUMENTI.AREA=AREE.AREA AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC;

             IF A_DIRAREA_NAME_STORICA IS NOT NULL THEN
                  --se trovo la DIR virtuale rispetto al PATH_ROOT storicizzato, allora prendo quella
                  A_DIRAREA_NAME := A_DIRAREA_NAME_STORICA;
             END IF;

               A_MESSAGGIO := 'Errore in  '||:NEW.FILENAME;
               A_ISTR_BFILE :=' "FILE" = bfilename('''||A_DIRAREA_NAME||''','''||A_CARTELLA||''') ';
               IF  A_ISTR_OCR<>' ' THEN
                  A_ISTR_BFILE:=A_ISTR_BFILE||' , ';
               END IF;

            END IF;
         END;

         IF  A_ISTR_OCR<>' ' OR A_ISTR_BFILE<>' ' THEN
              IF A_ISTR_BFILE<>' ' AND   NVL(:NEW.PATH_FILE ,' ')<>  NVL(:OLD.PATH_FILE ,' ' ) THEN
                  A_ISTR_DATA_ESTERNALIZZ:= ' DATA_ULTIMA_ESTERNALIZZAZIONE = SYSDATE,  ';
              END IF;

             INTEGRITYPACKAGE.SET_POSTEVENT('update oggetti_file set '||A_ISTR_DATA_ESTERNALIZZ||A_ISTR_BFILE||A_ISTR_OCR||
                                          ' where ID_OGGETTO_FILE='||:NEW.ID_OGGETTO_FILE, A_MESSAGGIO);
         END IF;
      END IF;

   END;
EXCEPTION
   WHEN INTEGRITY_ERROR THEN
        INTEGRITYPACKAGE.INITNESTLEVEL;
        RAISE_APPLICATION_ERROR(ERRNO, ERRMSG);
   WHEN OTHERS THEN
        INTEGRITYPACKAGE.INITNESTLEVEL;
        RAISE;
END OGGETTI_FILE_PU;
/



CREATE OR REPLACE FUNCTION F_PATH_FOLDER_ML(A_FOLDER VARCHAR2, A_TYPE VARCHAR2,A_UTENTE VARCHAR2,A_LINGUA VARCHAR2)
RETURN VARCHAR2 IS
/*****************************************************************************************
   NAME:       F_PATH_FOLDER_ML
   PURPOSE:    RESTITUISCE IL PATH DELLA CARTELLA A_FOLDER IN FUNZIONE DELLA LINGUA SELEZIONATA
               IL PARAMETRO A_TYPE PUO' PRENDERE DUE VALORI:
                   W: PERCORSO IN HTML CON ANCHORE CONTENENTI LINK ALLE CARTELLE
                      DEL PATH
                   I: PERCORSO SEMPLICE
                   N: PERCORSO CON I NOMI
              IL PARAMETRO A_LINGUA INDICA LA LINGUA SELEZIONATA (DEFAULT SE VUOTA INDICA LA LINGUA ITALIANA)
   RETURN:     PATH DELLA CARTELLA A_FOLDER IN FUNZIONE MULTILINGUA
   REVISIONS:
   VER   VER DMSERVER.COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          24/02/2017  SCANDURRA D.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       DESCR VARCHAR2(32000)                    := NULL;
       PARENTID_ONCLICK VARCHAR2(32000) := NULL;
       NODEID_ONCLICK VARCHAR2(32000)    := NULL;
       IDCARTPROVCHILD VARCHAR2(32000)  := NULL;
       IDCARTPROVPARENT VARCHAR2(32000) := NULL;
       CURSOR C_TREE IS
         SELECT ROWNUM RIGA,
                'C'||LINKS.ID_CARTELLA PARENTID,
                TIPO_OGGETTO||ID_OGGETTO NODEID,
                LINKS.ID_CARTELLA AS ID_CARTELLA,
                LINKS.ID_OGGETTO AS ID_OGGETTO,
                DECODE((gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                        f_idview_cartella (LINKS.ID_CARTELLA),
                                        'L',
                                        A_UTENTE,
                                        f_trasla_ruolo (A_UTENTE,
                                                        'GDMWEB',
                                                        'GDMWEB'
                                                       ),
                                        TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                       )),0,'N','S') AS COMP_PARENTID,
                DECODE((gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                f_idview_cartella (ID_OGGETTO),
                'L',
                A_UTENTE,
                f_trasla_ruolo (A_UTENTE,
                                'GDMWEB',
                                'GDMWEB'
                               ),
                TO_CHAR (SYSDATE, 'dd/mm/yyyy')
               )),0,'N','S') AS COMP_NODEID
           FROM LINKS
          WHERE TIPO_OGGETTO = 'C'
          START WITH  ID_OGGETTO = TO_NUMBER(A_FOLDER)
          CONNECT BY PRIOR   LINKS.ID_CARTELLA = ID_OGGETTO
            AND PRIOR TIPO_OGGETTO = 'C';
BEGIN
  BEGIN
    FOR CTREE IN C_TREE LOOP
      DECLARE
        NOMEPARENT      CARTELLE.NOME%TYPE;
        NOMECHILD       CARTELLE.NOME%TYPE;
        IDCHILD         CARTELLE.NOME%TYPE;
        IDPARENT        CARTELLE.NOME%TYPE;
        IDCARTPROVP     CARTELLE.NOME%TYPE;
      BEGIN
        SELECT C1.NOME AS NOMEPARENT,
               C2.NOME AS NOMECHILD,
               C2.ID_CARTELLA AS IDCHILD,
               C1.ID_CARTELLA AS IDPARENT
          INTO NOMEPARENT,NOMECHILD,IDCHILD,IDPARENT
          FROM CARTELLE C1, CARTELLE C2
         WHERE CTREE.ID_CARTELLA=C1.ID_CARTELLA
           AND CTREE.ID_OGGETTO=C2.ID_CARTELLA;
           IF A_TYPE = 'N' THEN
           --dbms_output.put_line('CTREE.RIGA='||CTREE.RIGA);
                  IF CTREE.RIGA=1 THEN
                     DESCR := GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA )||' :: '||GDM_UTILITY.F_MULTILINGUA (NOMECHILD,A_LINGUA);
                  ELSE
                     DESCR := GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA )||' :: '||DESCR;
                  END IF;
            ELSE
                IF A_TYPE = 'W' THEN
                   BEGIN
                     SELECT LINKS.ID_CARTELLA
                       INTO IDCARTPROVP
                       FROM LINKS
                      WHERE LINKS.ID_OGGETTO = CTREE.ID_CARTELLA  AND TIPO_OGGETTO='C';
                   EXCEPTION WHEN NO_DATA_FOUND THEN
                     IDCARTPROVPARENT:='';
                     IDCARTPROVP:=0;
                   END;
                   IF IDCARTPROVP<>0 THEN
                      IDCARTPROVPARENT:='&idCartAppartenenza='||IDCARTPROVP;
                   END IF;
                   IDCARTPROVCHILD:='&idCartAppartenenza='||CTREE.ID_CARTELLA;
                   IF CTREE.COMP_PARENTID='N' THEN
                      PARENTID_ONCLICK:='<font color="gray">'||GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA )||'</font>';
                   ELSE
                      PARENTID_ONCLICK:= '<a href="#" onclick="linkOggetto(''WorkArea.do?idCartella='||CTREE.PARENTID||IDCARTPROVPARENT||''');" >'||GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA)||'</a>';
                   END IF;
                   IF CTREE.COMP_NODEID='N' THEN
                      NODEID_ONCLICK:='<font color="gray">'||GDM_UTILITY.F_MULTILINGUA (NOMECHILD,A_LINGUA)||'</font>';
                   ELSE
                      NODEID_ONCLICK:= '<a href="#" onclick="linkOggetto(''WorkArea.do?idCartella='||CTREE.NODEID||IDCARTPROVCHILD||''');" >'||GDM_UTILITY.F_MULTILINGUA (NOMECHILD,A_LINGUA)||'</a>';
                   END IF;
                   IF CTREE.RIGA=1 THEN
                      DESCR := PARENTID_ONCLICK||'&nbsp;::&nbsp;'||NODEID_ONCLICK;
                   ELSE
                      DESCR := PARENTID_ONCLICK||'&nbsp;::&nbsp;'||DESCR;
                   END IF;
                ELSE
                   IF A_TYPE = 'I' THEN
                      IF CTREE.RIGA=1 THEN
                         DESCR := IDPARENT||'@'||IDCHILD;
                      ELSE
                         DESCR := IDPARENT||'@'||DESCR;
                      END IF;
                   ELSE
                      IF CTREE.RIGA=1 THEN
                         DESCR := GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA)||'&nbsp;::&nbsp;'||GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA);
                      ELSE
                         DESCR := GDM_UTILITY.F_MULTILINGUA (NOMEPARENT,A_LINGUA)||'&nbsp;::&nbsp;'||DESCR;
                      END IF;
                   END IF;
                END IF;
              END IF;
      END;
    END LOOP;
    IF DESCR IS NULL THEN
       BEGIN
         SELECT DECODE(A_TYPE,'W',GDM_UTILITY.F_MULTILINGUA (NOME,A_LINGUA),'N',GDM_UTILITY.F_MULTILINGUA (NOME,A_LINGUA),TO_CHAR(ID_CARTELLA))
           INTO DESCR
           FROM CARTELLE
          WHERE ID_CARTELLA=TO_NUMBER(A_FOLDER);
       EXCEPTION WHEN NO_DATA_FOUND THEN
         RETURN '';
       END;
       RETURN DESCR;
    END IF;
  END;
  IF LENGTH(DESCR)<=475 THEN
    RETURN DESCR;
  ELSE
    RETURN DESCR;
  END IF;
END F_PATH_FOLDER_ML;
/


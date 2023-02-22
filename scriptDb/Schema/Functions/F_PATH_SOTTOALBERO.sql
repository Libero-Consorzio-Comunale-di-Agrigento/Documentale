CREATE OR REPLACE FUNCTION F_PATH_SOTTOALBERO(A_FOLDER VARCHAR2,A_FOLDER_STOP VARCHAR2,
                                              A_COLLEGAMENTO NUMBER,A_TYPE VARCHAR2)
RETURN VARCHAR2 IS
/*****************************************************************************************
   NAME:       F_PATH_SOTTOALBERO
   PURPOSE:    RESTITUISCE IL PATH FISICO DEL SOTTOALBERO A PARTIRE DAL NODO A_FOLDER
               FINO AL NODO A_FOLDER_STOP  (0 PER NON OTTENERE LO STOP ED ARRIVARE
               ALLA FOGLIA)
   RETURN:     PATH FISICO DEL SOTTOALBERO
   REVISIONS:
   VER   VER DMSERVER.COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          30/05/2006  MANNELLA G.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       DESCR VARCHAR2(2000)            := NULL;
       IDCARTPROVCHILD VARCHAR2(2000)  := NULL;
       IDCARTPROVCOLL  VARCHAR2(2000)  := NULL;
       IDCOLLEGAMENTO  VARCHAR2(2000)  := NULL;
       CURSOR C_TREE IS
         SELECT ROWNUM RIGA,
                'C'||LINKS.ID_CARTELLA PARENTID,
                TIPO_OGGETTO||ID_OGGETTO NODEID,
                LINKS.ID_CARTELLA AS ID_CARTELLA,
                LINKS.ID_OGGETTO AS ID_OGGETTO
           FROM LINKS
          WHERE TIPO_OGGETTO = 'C'
          START WITH TO_NUMBER(A_FOLDER_STOP) = ID_OGGETTO
          CONNECT BY PRIOR   LINKS.ID_CARTELLA = ID_OGGETTO AND
          PRIOR TIPO_OGGETTO =  'C';
BEGIN
  BEGIN
    FOR CTREE IN C_TREE LOOP
        DECLARE
          NOMEPARENT   CARTELLE.NOME%TYPE;
          NOMECHILD    CARTELLE.NOME%TYPE;
          IDNODE       CARTELLE.NOME%TYPE;
          IDCARTPROVX  CARTELLE.NOME%TYPE;
        BEGIN
          SELECT DECODE(C1.NOME,'WRKSPUTENTE','Utente','WRKSPSISTEMA','Sistema',C1.NOME) AS NOMEPARENT,
                 C2.NOME AS NOMECHILD,
                 C2.ID_CARTELLA AS IDNODE
            INTO NOMEPARENT,NOMECHILD,IDNODE
            FROM CARTELLE C1, CARTELLE C2
           WHERE CTREE.ID_CARTELLA=C1.ID_CARTELLA
             AND CTREE.ID_OGGETTO=C2.ID_CARTELLA;
          IF A_TYPE='W' THEN
             BEGIN
               SELECT COLLEGAMENTI.ID_CARTELLA AS ID_CARTPROV
                 INTO IDCARTPROVX
                 FROM COLLEGAMENTI
                WHERE COLLEGAMENTI.ID_COLLEGAMENTO = A_COLLEGAMENTO;
             EXCEPTION WHEN NO_DATA_FOUND THEN
               IDCARTPROVCOLL:='';
               IDCARTPROVX:=0;
             END;
             IF IDCARTPROVX<>0 THEN
                IDCARTPROVCOLL:='&idCartAppartenenza='||IDCARTPROVX;
             END IF;
             IDCARTPROVCHILD:='&idCartAppartenenza='||CTREE.ID_CARTELLA;
             IDCOLLEGAMENTO :='&idCollegamento='||A_COLLEGAMENTO;
             IF CTREE.RIGA=1 THEN
                IF A_FOLDER=CTREE.ID_OGGETTO THEN
                   DESCR := '<a style="color:red"  href="#" onclick="linkOggetto(''WorkArea.do?idCartella='||CTREE.NODEID||IDCARTPROVCOLL||IDCOLLEGAMENTO||''');" >'||NOMECHILD||'</a>';
                ELSE
                   DESCR := '<a href="#" onclick="linkOggetto(''WorkArea.do?idCartella='||CTREE.NODEID||IDCARTPROVCHILD||IDCOLLEGAMENTO||''');">'||NOMECHILD||'</a>';
                END IF;
             ELSE
                IF A_FOLDER=CTREE.ID_OGGETTO THEN
                   DESCR := '<a style="color:red" href="#" onclick="linkOggetto(''WorkArea.do?idCartella='||CTREE.NODEID||IDCARTPROVCOLL||IDCOLLEGAMENTO||''');" >'||NOMECHILD||'</a>'||'&nbsp;::&nbsp;'||DESCR;
                ELSE
                   DESCR := '<a href="#" onclick="linkOggetto(''WorkArea.do?idCartella='||CTREE.NODEID||IDCARTPROVCHILD||IDCOLLEGAMENTO||''');" >'||NOMECHILD||'</a>'||'&nbsp;::&nbsp;'||DESCR;
                END IF;
             END IF;
          ELSE
             IF A_TYPE='I' THEN
                IF CTREE.RIGA=1 THEN
                   DESCR := IDNODE;
                ELSE
                   DESCR := IDNODE||'@'||DESCR;
                END IF;
             ELSE
                IF CTREE.RIGA=1 THEN
                   DESCR := NOMECHILD;
                 ELSE
                   DESCR := NOMECHILD||'&nbsp;::&nbsp;'||DESCR;
                END IF;
              END IF;
          END IF;
          IF A_FOLDER=CTREE.ID_OGGETTO THEN
             RETURN DESCR;
          END IF;
        END;
    END LOOP;
    IF DESCR IS NULL THEN
       BEGIN
         SELECT DECODE(NOME,'WRKSPUTENTE','Utente','WRKSPSISTEMA','Sistema',NOME)
           INTO DESCR
           FROM CARTELLE
         WHERE ID_CARTELLA=TO_NUMBER(A_FOLDER);
       EXCEPTION WHEN NO_DATA_FOUND THEN
         RETURN '';
       END;
       RETURN DESCR;
    END IF;
  END;
  RETURN DESCR;
END F_PATH_SOTTOALBERO;
/


CREATE OR REPLACE FUNCTION F_Elimina_Cartella_LF(A_IDCARTELLA NUMBER,A_USER VARCHAR2, A_CONTROLLA_UTENTE VARCHAR2 DEFAULT 'S', A_TYPE_DELETE VARCHAR2)
RETURN NUMBER IS
       TMPVAR NUMBER(1) := 0;
       CURSOR C_TREE IS
        SELECT r,TIPO_OGGETTO,
                ID_OGGETTO,
                ID_CARTELLA,IDW_OGGETTO
          from (SELECT ROWNUM as r,
                TIPO_OGGETTO,
                ID_OGGETTO,
                ID_CARTELLA,
                F_Idview_Cartella(ID_OGGETTO) IDW_OGGETTO
           FROM LINKS
                START WITH ID_CARTELLA = A_IDCARTELLA AND TIPO_OGGETTO='C'
                CONNECT BY PRIOR    ID_OGGETTO = ID_CARTELLA
                 AND PRIOR TIPO_OGGETTO = 'C'
          UNION
         SELECT 0,
                'C',
                A_IDCARTELLA,
                A_IDCARTELLA,
                F_Idview_Cartella(A_IDCARTELLA) IDW_OGGETTO
           FROM DUAL)
         order by 1 desc;
         CHECK_COMPETENZA NUMBER(1):=0;
BEGIN
  FOR CTREE IN C_TREE LOOP
    IF CTREE.TIPO_OGGETTO='C' THEN
       DECLARE
         IDDOC        DOCUMENTI.ID_DOCUMENTO%TYPE;
         RETVAL       NUMBER(1);
         CHECK_CHILD  VARCHAR2(1) := 1;
         STATO        VARCHAR2(2) := 'BO';
       BEGIN
         SELECT NVL(STATO,'BO')
           INTO STATO
           FROM CARTELLE
          WHERE ID_CARTELLA=CTREE.ID_OGGETTO;
         --IF STATO<>'CA' THEN
            SELECT DECODE(COUNT(*),0,'N','Y'),
                   DECODE(A_CONTROLLA_UTENTE,'S',1,Gdm_Competenza.GDM_VERIFICA('VIEW_CARTELLA',CTREE.IDW_OGGETTO,'D',
                                               A_USER,F_Trasla_Ruolo(A_USER,'GDM','GDM')))
              INTO CHECK_CHILD,
                   CHECK_COMPETENZA
              FROM LINKS
             WHERE LINKS.ID_CARTELLA=CTREE.ID_OGGETTO AND
                   (
                    (TIPO_OGGETTO='C' AND 'CA'<>(SELECT NVL(CARTELLE.STATO,'BO') FROM CARTELLE WHERE LINKS.ID_OGGETTO=CARTELLE.ID_CARTELLA ) )
                     OR
                    (TIPO_OGGETTO<>'C' AND 1<>1) );
              IF (CHECK_CHILD='N' AND CHECK_COMPETENZA=1) THEN
                 IDDOC:=F_Iddoc_From_Cartella(CTREE.ID_OGGETTO);
                 IF A_TYPE_DELETE='F' THEN
                     Gdm_Competenza.GDM_ELIMINA('VIEW_CARTELLA',CTREE.IDW_OGGETTO);
                     DELETE FROM VIEW_CARTELLA
                     WHERE ID_CARTELLA=CTREE.ID_OGGETTO;
                     DELETE FROM LINKS
                     WHERE ID_CARTELLA=CTREE.ID_OGGETTO;
                     DELETE FROM LINKS
                     WHERE ID_OGGETTO=CTREE.ID_OGGETTO AND TIPO_OGGETTO='C';
                     DELETE FROM COLLEGAMENTI
                     WHERE ID_CARTELLA=CTREE.ID_OGGETTO;
                     DELETE FROM COLLEGAMENTI
                     WHERE ID_CARTELLA_COLLEGATA=CTREE.ID_OGGETTO;
                     DELETE FROM CARTELLE
                     WHERE ID_CARTELLA=CTREE.ID_OGGETTO;
                 ELSE
                     UPDATE CARTELLE
                     SET STATO='CA'
                     WHERE ID_CARTELLA=CTREE.ID_OGGETTO;
                 END IF;
                 IF IDDOC<>-1 THEN
                    IF A_TYPE_DELETE='F' THEN
                       RETVAL:=F_Elimina_Documento(IDDOC);
                    ELSE
                       RETVAL:=F_Elimina_Documento_Logico(IDDOC,A_USER);
                    END IF;
                    IF RETVAL=0 THEN
                       ROLLBACK;
                       RAISE_APPLICATION_ERROR('-20999', 'PROBLEMI IN ELIMINA PROFILO CARTELLA '||CTREE.ID_OGGETTO
                                                         ||' PER F_ELIMINA_CARTELLA('||A_IDCARTELLA
                                                         ||') DESCRIZIONE ERRORE: '||SQLERRM);
                    END IF;
                 END IF;
              END IF;
         -- END IF;
       EXCEPTION WHEN OTHERS THEN
                 ROLLBACK;
                 RAISE_APPLICATION_ERROR('-20999','PROBLEMI IN ELIMINA CARTELLA '||CTREE.ID_OGGETTO
                                                  ||' PER F_ELIMINA_CARTELLA('||A_IDCARTELLA
                                                  ||') DESCRIZIONE ERRORE: '||SQLERRM);
       END;
    END IF;
    IF CTREE.TIPO_OGGETTO='Q' THEN
       DECLARE
         RETVAL       VARCHAR2(32000);
         CHECK_LINKS  VARCHAR2(1) := 1;
       BEGIN
         SELECT DECODE(COUNT(*),1,'N','Y'),
                DECODE(A_CONTROLLA_UTENTE,'S',1,Gdm_Competenza.GDM_VERIFICA('QUERY',CTREE.ID_OGGETTO,'D',A_USER,F_Trasla_Ruolo(A_USER,'GDMWEB','GDMWEB')))
           INTO CHECK_LINKS,
                CHECK_COMPETENZA
           FROM LINKS
          WHERE ID_OGGETTO=CTREE.ID_OGGETTO;
             IF CHECK_COMPETENZA=1  THEN
                --IF A_TYPE_DELETE='F' THEN
                    BEGIN
                       DELETE FROM LINKS
                        WHERE ID_OGGETTO=CTREE.ID_OGGETTO AND
                              ID_CARTELLA=CTREE.ID_CARTELLA;
                       EXCEPTION WHEN OTHERS THEN
                           ROLLBACK;
                           RAISE_APPLICATION_ERROR('-20999','PROBLEMI IN ELIMINA LINK DOCUMENTO '
                                                            ||CTREE.ID_OGGETTO||' PER F_ELIMINA_CARTELLA('||A_IDCARTELLA
                                                            ||') DESCRIZIONE ERRORE: '||SQLERRM);
                    END;
                    --RETVAL:=F_Elimina_Query(CTREE.ID_OGGETTO);
                --ELSE
                  --  null;
               -- END IF;
             END IF;
       END;
    END IF;
    IF CTREE.TIPO_OGGETTO='D' THEN
       BEGIN
         DELETE FROM LINKS
         WHERE ID_OGGETTO=CTREE.ID_OGGETTO AND
               ID_CARTELLA=CTREE.ID_CARTELLA;
       EXCEPTION WHEN OTHERS THEN
         ROLLBACK;
         RAISE_APPLICATION_ERROR('-20999','PROBLEMI IN ELIMINA LINK DOCUMENTO '
                                          ||CTREE.ID_OGGETTO||' PER F_ELIMINA_CARTELLA('||A_IDCARTELLA
                                          ||') DESCRIZIONE ERRORE: '||SQLERRM);
       END;
    END IF;
  END LOOP;
  TMPVAR:=1;
  RETURN TMPVAR;
END F_Elimina_Cartella_LF;
/


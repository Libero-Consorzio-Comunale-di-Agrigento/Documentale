CREATE OR REPLACE PACKAGE BODY gdm_profilo
AS
   PROCEDURE DUPLICA_TBL_ORIZZONTALE(p_iddoc NUMBER, p_newiddoc NUMBER)
   AS
   a_nometabella varchar2(200);
    a_colonnaiddoc varchar2(100) :='ID_DOCUMENTO';
    a_nomecolonna varchar2(100);
    selString  varchar2(1000);
    selStringCampi varchar2(4000) :=' ';
    selStringInsert varchar2(32000) :='';
    TYPE SelCur is ref cursor;
    colonneCur SelCur;
   BEGIN
            select  f_nome_tabella(documenti.area,TIPI_DOCUMENTO.nome)
            into a_nometabella
            from documenti, TIPI_DOCUMENTO
            where documenti.id_tipodoc=TIPI_DOCUMENTO.id_tipodoc and
                      documenti.id_documento=p_iddoc;

            selString := 'select COLUMN_NAME  '||
                              '   from  user_tab_columns ' ||
                              ' where table_name='''||a_nometabella||''' and COLUMN_NAME<>'''||a_colonnaiddoc||'''';

             OPEN colonneCur for selString;

             LOOP
               FETCH colonneCur INTO   a_nomecolonna;
               EXIT WHEN colonneCur%NOTFOUND;

               if selStringCampi<>' ' then
                  selStringCampi := selStringCampi||',';
               end if;

               selStringCampi:= selStringCampi || a_nomecolonna;

             END LOOP;
             CLOSE colonneCur;


            selStringInsert := ' insert into '||a_nometabella||'  ('||a_colonnaiddoc||','||selStringCampi||') select '||p_newidDoc||','||selStringCampi||' from '||a_nometabella||' where '||a_colonnaiddoc||' = '||p_iddoc;

            EXECUTE IMMEDIATE selStringInsert;
   END;
   PROCEDURE RIEMPI_TYPE_CAMPO_EXTRAFIELD(A_INFOCAMPO VARCHAR2)
   AS
   A_TOTALE VARCHAR2(200);
   A_PEZZO  VARCHAR2(50);
   A_CONTA  NUMBER(2) := 1;
   BEGIN
        A_TOTALE := A_INFOCAMPO;
        LOOP
            A_PEZZO  := SUBSTR(A_TOTALE,1,INSTR(A_TOTALE,'#@#')-1);
            A_TOTALE := SUBSTR(A_TOTALE,INSTR(A_TOTALE,'#@#')+3);
            CASE (A_CONTA)
             WHEN 1 THEN
                  NULL;
             WHEN 2 THEN
                  vTypeCampo.FORMATO_DATA:=A_PEZZO;
             WHEN 3 THEN
                  vTypeCampo.IN_USO:=A_PEZZO;
             WHEN 4 THEN
                  vTypeCampo.ID_CAMPO:=TO_NUMBER(A_PEZZO);
             WHEN 5 THEN
                  vTypeCampo.SENZA_SALVATAGGIO:=A_PEZZO;
             WHEN 6 THEN
                  vTypeCampo.SENZA_AGGIORNAMENTO:=A_PEZZO;
             WHEN 7 THEN
                  NULL;
             WHEN 8 THEN
                  vTypeCampo.TIPO_LOG:=A_PEZZO;
            END CASE;
            A_CONTA := A_CONTA + 1;
            --dbms_output.put_line(A_PEZZO);
            IF INSTR(A_TOTALE,'#@#')=0 THEN
                vTypeCampo.LUNGHEZZA:=TO_NUMBER(A_TOTALE);
               --dbms_output.put_line(A_TOTALE);
               EXIT;
            END IF;
        END LOOP;
   END;
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE CLOB)
   AS
      A_INFOCAMPO VARCHAR2(200);
   BEGIN
      IF P_VALORE IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! impossibile inserire valore nullo per campo '||P_NOME_CAMPO||' (area='||P_AREA||',modello='||P_MODELLO||')');
      END IF;
      A_INFOCAMPO := F_INFO_CAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO);
      IF NVL(A_INFOCAMPO,'X')='X' THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! campo '||P_NOME_CAMPO||' inesistente per area='||P_AREA||',modello='||P_MODELLO);
      END IF;
      IF SUBSTR(A_INFOCAMPO,1,1)<>'S' THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! il campo '||P_NOME_CAMPO||' non è di tipo stringa (tipo='||SUBSTR(A_INFOCAMPO,1,1)||')');
      END IF;
      vTypeCampo.VALORE_STRINGA := P_VALORE;
      vTypeCampo.VALORE_NUMERO  := NULL;
      vTypeCampo.VALORE_DATA    := NULL;
      vTypeCampo.TIPO           := 'S';
      RIEMPI_TYPE_CAMPO_EXTRAFIELD(A_INFOCAMPO);
      vCampo(P_AREA||'@'||P_MODELLO||'@'||P_NOME_CAMPO) := vTypeCampo;
   END;
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE NUMBER)
   AS
      A_INFOCAMPO VARCHAR2(200);
   BEGIN
      IF P_VALORE IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! impossibile inserire valore nullo per campo '||P_NOME_CAMPO||' (area='||P_AREA||',modello='||P_MODELLO||')');
      END IF;
      A_INFOCAMPO := F_INFO_CAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO);
      IF NVL(A_INFOCAMPO,'X')='X' THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! campo '||P_NOME_CAMPO||' inesistente per area='||P_AREA||',modello='||P_MODELLO);
      END IF;
      IF SUBSTR(A_INFOCAMPO,1,1)<>'N' THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! il campo '||P_NOME_CAMPO||' non è di tipo numerico (tipo='||SUBSTR(A_INFOCAMPO,1,1)||')');
      END IF;
      vTypeCampo.VALORE_STRINGA := NULL;
      vTypeCampo.VALORE_NUMERO  := P_VALORE;
      vTypeCampo.VALORE_DATA    := NULL;
      vTypeCampo.TIPO           := 'N';
      RIEMPI_TYPE_CAMPO_EXTRAFIELD(A_INFOCAMPO);
      vCampo(P_AREA||'@'||P_MODELLO||'@'||P_NOME_CAMPO) := vTypeCampo;
   END;
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE DATE)
   AS
      A_INFOCAMPO VARCHAR2(200);
   BEGIN
      IF P_VALORE IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! impossibile inserire valore nullo per campo '||P_NOME_CAMPO||' (area='||P_AREA||',modello='||P_MODELLO||')');
      END IF;
      A_INFOCAMPO := F_INFO_CAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO);
      IF NVL(A_INFOCAMPO,'X')='X' THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! campo '||P_NOME_CAMPO||' inesistente per area='||P_AREA||',modello='||P_MODELLO);
      END IF;
      IF SUBSTR(A_INFOCAMPO,1,1)<>'D' THEN
         RAISE_APPLICATION_ERROR(-20999,'Attenzione! il campo '||P_NOME_CAMPO||' non è di tipo data (tipo='||SUBSTR(A_INFOCAMPO,1,1)||')');
      END IF;
      vTypeCampo.VALORE_STRINGA := NULL;
      vTypeCampo.VALORE_NUMERO  := NULL;
      vTypeCampo.VALORE_DATA    := P_VALORE;
      vTypeCampo.TIPO           := 'D';
      RIEMPI_TYPE_CAMPO_EXTRAFIELD(A_INFOCAMPO);
      vCampo(P_AREA||'@'||P_MODELLO||'@'||P_NOME_CAMPO) := vTypeCampo;
   END;
   PROCEDURE RESETCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2)
   AS
     nome VARCHAR2(1000);
   BEGIN
     nome := vCampo.FIRST;
     WHILE nome IS NOT NULL LOOP
           IF (nome = P_AREA||'@'||P_MODELLO||'@'||P_NOME_CAMPO) OR (P_AREA='X' AND P_MODELLO='X' AND P_NOME_CAMPO='X') THEN
              vCampo.delete(nome);
           END IF;
           nome := vCampo.NEXT( nome );
     END LOOP;
   END;
   PROCEDURE RESETCAMPI
   AS
   BEGIN
      RESETCAMPO('X','X','X');
   END;
   FUNCTION get_allegati (p_id_documento IN NUMBER)
      RETURN afc.t_ref_cursor
   IS
      d_result   afc.t_ref_cursor;
   BEGIN
      OPEN d_result FOR
         SELECT   id_oggetto_file, o.id_formato, filename,
                  allegato, id_oggetto_file_padre
             FROM oggetti_file o, formati_file f
            WHERE id_documento = p_id_documento
              AND o.id_formato = f.id_formato
         ORDER BY filename;
      RETURN d_result;
   EXCEPTION
      WHEN OTHERS
      THEN
         raise_application_error (-20999,
                                  'GDM_PROFILO.GET_ALLEGATI: ' || SQLERRM
                                 );
   END get_allegati;
   FUNCTION getdocumento (
      p_codice_modello     IN   VARCHAR2,
      p_area               IN   VARCHAR2,
      p_codice_richiesta   IN   VARCHAR2
   )
      RETURN NUMBER
   AS
   BEGIN
      DECLARE
         iddoc   NUMBER (10) := NULL;
      BEGIN
         SELECT d.id_documento
           INTO iddoc
           FROM modelli m, documenti d
          WHERE m.area = p_area
            AND m.codice_modello = p_codice_modello
            AND m.id_tipodoc = d.id_tipodoc
            AND d.codice_richiesta = p_codice_richiesta
            AND d.area = p_area
            UNION
           SELECT d.id_documento
          FROM modelli m, documenti d
          WHERE m.area = p_area
            AND m.codice_modello = (select codice_modello_padre FROM MODELLI WHERE area = p_area and codice_modello = p_codice_modello )
            AND m.id_tipodoc = d.id_tipodoc
            AND d.codice_richiesta = p_codice_richiesta
            AND d.area = p_area;
         RETURN iddoc;
      EXCEPTION
         WHEN OTHERS
         THEN
            raise_application_error ('-20999',
                                     'Impossibilee individuare il documento'
                                    );
      END;
   END getdocumento;
   FUNCTION valida (p_documento IN NUMBER, p_utente IN VARCHAR2)
      RETURN NUMBER
   AS
   BEGIN
      RETURN cambia_stato (p_documento, p_utente, 'CO');
   END valida;
   FUNCTION cancella (p_documento IN NUMBER, p_utente IN VARCHAR2)
      RETURN NUMBER
   AS
   BEGIN
      RETURN cambia_stato (p_documento, p_utente, 'CA');
   END cancella;
   FUNCTION annulla (p_documento IN NUMBER, p_utente IN VARCHAR2)
      RETURN NUMBER
   AS
   BEGIN
      RETURN cambia_stato (p_documento, p_utente, 'AN');
   END annulla;
   FUNCTION cambia_stato (
      p_documento   IN   NUMBER,
      p_utente      IN   VARCHAR2,
      p_stato       IN   VARCHAR2
   )
      RETURN NUMBER
   AS
   BEGIN
      BEGIN
         INSERT INTO stati_documento
                     (id_documento, stato, commento, data_aggiornamento,
                      utente_aggiornamento
                     )
              VALUES (p_documento, p_stato, 'PLSQL', SYSDATE,
                      p_utente
                     );
      EXCEPTION
         WHEN OTHERS
         THEN
            raise_application_error
               ('-20997',
                   'Impossibile modificare lo stato documento del documento:'
                || p_documento
                || ' - STATO: '
                || p_stato
                || SQLERRM
               );
      END;
      BEGIN
         EXECUTE IMMEDIATE    'UPDATE DOCUMENTI SET STATO_DOCUMENTO='''
                           || p_stato
                           || ''' WHERE ID_DOCUMENTO='
                           || p_documento;
      EXCEPTION
         WHEN OTHERS
         THEN
           RAISE;
      END;
      RETURN 1;
   END cambia_stato;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2, p_crea_record_orizzontale NUMBER, p_creaCompetenzeUtente BOOLEAN) RETURN NUMBER
   IS
       A_CR             RICHIESTE.CODICE_RICHIESTA%TYPE;
       A_IDCR           VARCHAR2(50);
       A_IDDOCUMENTO    DOCUMENTI.ID_DOCUMENTO%TYPE;
       A_IDTIPODOC      TIPI_DOCUMENTO.ID_TIPODOC%TYPE;
       A_ACTLOG         ACTIVITY_LOG.ID_LOG%TYPE;
       A_GESTCOMP       TIPI_DOCUMENTO.GESTIONE_COMPETENZE%TYPE;
       A_TIPO_USO       MODELLI.TIPO_USO%TYPE;
       A_STRINGACOMP    VARCHAR2(50);
       A_NOMETAB        VARCHAR2(200);
       A_CAMPO          VARCHAR2(1000);
       A_NOME_CAMPO     CAMPI_DOCUMENTO.NOME%TYPE;
   BEGIN
       ----******************* PASSO 0: RECUPERO INFORMAZIONI INIZIALI *******************----
       BEGIN
         SELECT MODELLI.ID_TIPODOC,GESTIONE_COMPETENZE, decode(
                                                        GESTIONE_COMPETENZE,'1','L;U;',
                                                                            '2','L;U;',
                                                                            '3','L;',
                                                                            '4','L;',
                                                                            '5','L;U;D;',
                                                                            '6','L;U;D;',
                                                                            ' '
                                                       ) || decode(MANAGE_COMPETENZE,'1','M;',''),
                TIPO_USO
           INTO A_IDTIPODOC,A_GESTCOMP,A_STRINGACOMP, A_TIPO_USO
           FROM MODELLI , TIPI_DOCUMENTO
          WHERE AREA = p_area AND CODICE_MODELLO = p_modello
            AND TIPI_DOCUMENTO.ID_TIPODOC=MODELLI.ID_TIPODOC;
          SELECT DECODE(p_cr,NULL,TO_CHAR(DECODE(A_TIPO_USO,'F',CART_SQ.NEXTVAL,'C',CART_SQ.NEXTVAL,'V',QRY_SQ.NEXTVAL,'Q',QRY_SQ.NEXTVAL, CODR_SQ.NEXTVAL)),p_cr),
                DOCU_SQ.NEXTVAL,ACLO_SQ.NEXTVAL
           INTO A_IDCR,A_IDDOCUMENTO,A_ACTLOG
           FROM DUAL;
         EXCEPTION WHEN NO_DATA_FOUND THEN
             RAISE_APPLICATION_ERROR(-20999,'Attenzione! Area ('||p_area||') / Modello ('||p_modello||') inesistente');
                   WHEN OTHERS THEN
             RAISE_APPLICATION_ERROR(-20999,'Attenzione! Errore in recupero informazioni modello. Errore: '||sqlerrm);
       END;
       IF NVL(p_cr,' ')=' ' THEN
           IF A_TIPO_USO IN ('F','C') THEN
                A_CR := TO_CHAR(A_IDCR);
           ELSE
                IF A_TIPO_USO IN ('V','Q') THEN
                    A_CR := '-'||TO_CHAR(A_IDCR);
                ELSE
                    A_CR := 'DMSERVER'||TO_CHAR(A_IDCR);
                END IF;
           END IF;
       ELSE
           A_CR := TO_CHAR(A_IDCR);
       END IF;
       ----******************* PASSO 1: CREAZIONE DEL CODICE_RICHIESTA *******************----
       BEGIN
         INSERT INTO RICHIESTE
         (codice_richiesta, area,data_inserimento)
         VALUES
         (A_CR, p_area,sysdate  );
         EXCEPTION WHEN OTHERS THEN
             --ROLLBACK;
             RAISE_APPLICATION_ERROR(-20999,'Errore al passo 1: Generazione del codice richiesta. Errore:'||sqlerrm);
       END;
       ----******************* PASSO 2: CREAZIONE DELlA RIGA SULLA TABELLA DOCUMENTI *******************----
       BEGIN
         INSERT INTO DOCUMENTI
         (id_documento, id_libreria, id_tipodoc,codice_richiesta, area, data_aggiornamento,utente_aggiornamento, STATO_DOCUMENTO)
         VALUES
         (A_IDDOCUMENTO,1,A_IDTIPODOC,A_CR,P_AREA,SYSDATE,P_UTENTE,'BO');
         EXCEPTION WHEN OTHERS THEN
             --ROLLBACK;
             RAISE_APPLICATION_ERROR(-20999,'Errore al passo 2: Generazione della riga in tabella documenti. Errore:'||sqlerrm);
       END;
       ----******************* PASSO 3: CREAZIONE DELlA RIGA SULLA TABELLA ACTIVITY_LOG *******************----
       BEGIN
         INSERT INTO ACTIVITY_LOG
         (id_log, id_documento, tipo_azione, data_aggiornamento,utente_aggiornamento)
         VALUES
         (A_ACTLOG,A_IDDOCUMENTO,'C',SYSDATE,P_UTENTE);
         EXCEPTION WHEN OTHERS THEN
             --ROLLBACK;
             RAISE_APPLICATION_ERROR(-20999,'Errore al passo 3: Generazione della riga in tabella Activity_Log. Errore:'||sqlerrm);
       END;
       ----******************* PASSO 4: CREAZIONE DELlA RIGA SULLA TABELLA STATI_DOCUMENTO *******************----
       BEGIN
         INSERT INTO STATI_DOCUMENTO
         (id_documento, stato,  data_aggiornamento,utente_aggiornamento)
         VALUES
         (A_IDDOCUMENTO, 'BO', SYSDATE, P_UTENTE);
         EXCEPTION WHEN OTHERS THEN
             --ROLLBACK;
             RAISE_APPLICATION_ERROR(-20999,'Errore al passo 4: Generazione della riga in tabella Stati_Documento. Errore:'||sqlerrm);
       END;
       ----******************* PASSO 5: CREAZIONE DELLE COMPETENZE IN CREAZIONE*******************----
       BEGIN
          -----PASSO 5.1 EREDITO LE COMPETENZE DAL TIPO DOCUMENTO
          BEGIN
            GDM_COMPETENZA.GDM_ASSEGNA('DOCUMENTI',TO_CHAR(A_IDDOCUMENTO),'TIPI_DOCUMENTO',TO_CHAR(A_IDTIPODOC),P_UTENTE);
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore al passo 5.1: Generazione delle competenze dal modello. Errore:'||sqlerrm);
          END;
          -----PASSO 5.2 SETTO LE COMPETENZE PER GLI ALLEGATI
          BEGIN
            GDM_COMPETENZA.gdm_comp_tipodoc_doc_allegati(A_IDDOCUMENTO,P_UTENTE);
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore al passo 5.2: Generazione delle competenze per gli allegati del documento. Errore:'||sqlerrm);
          END;
         -----PASSO 5.3 SETTO LE COMPETENZE PER IL DOCUMENTO
          IF p_creaCompetenzeUtente THEN
              BEGIN
                  IF A_GESTCOMP IN ('1','3','5') THEN
                     GDM_COMPETENZA.SI4_ASSEGNA_MULTIPLA('DOCUMENTI',TO_CHAR(A_IDDOCUMENTO),A_STRINGACOMP,P_UTENTE,'GDM',P_UTENTE,'S',to_char(sysdate,'dd/mm/yyyy'),NULL);
                  ELSE
                     IF A_GESTCOMP IN ('2','4','6') THEN
                        GDM_COMPETENZA.GDM_ASSEGNA_GRUPPO_MULTIPLA('DOCUMENTI',TO_CHAR(A_IDDOCUMENTO),A_STRINGACOMP,P_UTENTE,'GDM',P_UTENTE,'S',to_char(sysdate,'dd/mm/yyyy'),NULL);
                     END IF;
                  END IF;
                  EXCEPTION WHEN OTHERS THEN
                    RAISE_APPLICATION_ERROR(-20999,'Errore al passo 5.3: Generazione delle competenze per il documento. Errore:'||sqlerrm);
              END;
          END IF;
          ----PASSO 6: CREO LA RIGA ORIZZONTALE SE RICHIESTO ED IL MODELLO E' ORIZZONTALE
          IF NVL(p_crea_record_orizzontale,0)<>0 THEN
             A_NOMETAB := F_NOME_TABELLA(P_AREA,P_MODELLO);
             IF LENGTH(NVL(A_NOMETAB,''))>0 THEN
             DECLARE
                A_INSERT VARCHAR2(32000);
                A_VALUES VARCHAR2(32000);
                RETVAL   VARCHAR2(4000);
                d_cursor INTEGER;
                d_rows_processed INTEGER;
                d_conta  NUMBER(5) :=1 ;
             BEGIN
                --Carico i valori orizzontali
                A_INSERT := 'INSERT INTO '||A_NOMETAB||' (ID_DOCUMENTO ';
                A_VALUES := ' VALUES ('||A_IDDOCUMENTO;
                A_CAMPO := vCampo.FIRST;
                WHILE A_CAMPO IS NOT NULL LOOP
                   IF SUBSTR(A_CAMPO,1,INSTR(A_CAMPO,'@',-1) -1 )  = P_AREA || '@' || P_MODELLO THEN
                      A_NOME_CAMPO := SUBSTR(A_CAMPO,INSTR(A_CAMPO,'@',-1) + 1 );
                      A_INSERT := A_INSERT || ','||A_NOME_CAMPO;
                      A_VALUES := A_VALUES || ',:PAR'||d_conta;
                      d_conta := d_conta+1;
                   END IF;
                   A_CAMPO := vCampo.NEXT( A_CAMPO );
                END LOOP;
                A_INSERT := A_INSERT || ')';
                A_VALUES := A_VALUES || ')';
                d_cursor := DBMS_SQL.OPEN_CURSOR;
                DBMS_SQL.PARSE( d_cursor, A_INSERT||A_VALUES, dbms_sql.native );
                d_conta :=1 ;
                A_CAMPO := vCampo.FIRST;
                WHILE A_CAMPO IS NOT NULL LOOP
                   IF SUBSTR(A_CAMPO,1,INSTR(A_CAMPO,'@',-1) -1 )  = P_AREA || '@' || P_MODELLO THEN
                      A_NOME_CAMPO := SUBSTR(A_CAMPO,INSTR(A_CAMPO,'@',-1) + 1 );
                      IF vCampo(A_CAMPO).TIPO='S' THEN
                         DBMS_SQL.BIND_VARIABLE (d_cursor, ':PAR'||d_conta, vCampo(A_CAMPO).VALORE_STRINGA);
                      ELSE
                         IF vCampo(A_CAMPO).TIPO='N' THEN
                            DBMS_SQL.BIND_VARIABLE (d_cursor, ':PAR'||d_conta, vCampo(A_CAMPO).VALORE_NUMERO);
                         ELSE
                            DBMS_SQL.BIND_VARIABLE (d_cursor, ':PAR'||d_conta, vCampo(A_CAMPO).VALORE_DATA);
                         END IF;
                      END IF;
                      d_conta := d_conta+1;
                   END IF;
                   A_CAMPO := vCampo.NEXT( A_CAMPO );
                END LOOP;
                d_rows_processed := DBMS_SQL.EXECUTE( d_cursor );
                DBMS_SQL.CLOSE_CURSOR( d_cursor );
                RETVAL := F_FULL_TEXT_HORIZ(A_IDDOCUMENTO,A_NOMETAB);
             EXCEPTION WHEN OTHERS THEN
                DBMS_SQL.CLOSE_CURSOR( d_cursor );
                RAISE_APPLICATION_ERROR(-20999,'Errore in inserimento riga orizzontale per sql='||A_INSERT||A_VALUES||'. Errore:'||sqlerrm);
             END;
             END IF;
          ELSE
             --Carico i valori verticali
             A_CAMPO := vCampo.FIRST;
             WHILE A_CAMPO IS NOT NULL LOOP
                   IF SUBSTR(A_CAMPO,1,INSTR(A_CAMPO,'@',-1) -1 )  = P_AREA || '@' || P_MODELLO THEN
                      A_NOME_CAMPO := SUBSTR(A_CAMPO,INSTR(A_CAMPO,'@',-1) + 1 );
                      DECLARE
                      A_STRINGA VALORI.VALORE_STRINGA%TYPE := NULL;
                      BEGIN
                         IF (vCampo(A_CAMPO).VALORE_STRINGA IS NOT NULL) AND (LENGTH(vCampo(A_CAMPO).VALORE_STRINGA))>1000 THEN
                            A_STRINGA := SUBSTR(UPPER(vCampo(A_CAMPO).VALORE_STRINGA),1,1000);
                         ELSE
                            A_STRINGA := UPPER(vCampo(A_CAMPO).VALORE_STRINGA);
                         END IF;
                         INSERT INTO VALORI
                         (ID_VALORE,ID_DOCUMENTO,ID_CAMPO,VALORE_NUMERO,VALORE_DATA,
                          VALORE_STRINGA,VALORE_CLOB,UTENTE_AGGIORNAMENTO)
                         VALUES
                         (VALO_SQ.NEXTVAL,A_IDDOCUMENTO,vCampo(A_CAMPO).ID_CAMPO,vCampo(A_CAMPO).VALORE_NUMERO,vCampo(A_CAMPO).VALORE_DATA,
                         A_STRINGA,vCampo(A_CAMPO).VALORE_STRINGA,P_UTENTE);
                      EXCEPTION WHEN OTHERS THEN
                         RAISE_APPLICATION_ERROR(-20999,'Inserimento valore verticale per campo '||A_NOME_CAMPO||'. Errore:'||sqlerrm);
                      END;
                   END IF;
                   A_CAMPO := vCampo.NEXT( A_CAMPO );
             END LOOP;
          END IF;
          EXCEPTION WHEN OTHERS THEN
                --ROLLBACK;
                RAISE;
       END;
       RETURN A_IDDOCUMENTO;
   END crea_documento;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2) RETURN NUMBER
   IS
   BEGIN
      RETURN crea_documento(p_area, p_modello, p_cr, p_utente, 0,TRUE);
   END;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2, p_creaCompetenzeUtente BOOLEAN) RETURN NUMBER
   IS
   BEGIN
      RETURN crea_documento(p_area, p_modello, p_cr, p_utente, 0,p_creaCompetenzeUtente);
   END;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2, p_crea_record_orizzontale NUMBER) RETURN NUMBER
   IS
   BEGIN
      RETURN crea_documento(p_area, p_modello, p_cr, p_utente, p_crea_record_orizzontale,TRUE);
   END;
    FUNCTION duplica_documento (p_documento NUMBER, p_utente VARCHAR2) RETURN NUMBER
     IS
     BEGIN
      RETURN duplica_documento (p_documento , p_utente , 0);
   END;
   FUNCTION duplica_documento (p_documento NUMBER, p_utente VARCHAR2, p_duplica_tabella_oriz NUMBER)
      RETURN NUMBER
   AS
      n_iddoc   documenti.id_documento%TYPE;
   BEGIN

      BEGIN
         /* INSERISCO LA NUOVA RICHIESTA */
         SELECT docu_sq.NEXTVAL
            INTO n_iddoc
           FROM DUAL;

         INSERT INTO richieste
         (codice_richiesta, area, id_tipo_pratica, data_scadenza,data_inserimento)
         SELECT 'PLSQL-' || n_iddoc || '-DUPLICA-' || p_documento, area,NULL, NULL, SYSDATE
              FROM documenti
             WHERE id_documento = p_documento;

      EXCEPTION WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20990', 'Impossibile duplicare il documento - Errore in insert richieste' || SQLERRM);
      END;



      BEGIN
         /* INSERISCO IL NUOVO DOCUMENTO */
         INSERT INTO documenti  (id_documento, id_libreria, id_tipodoc, codice_richiesta, area,
                                               data_aggiornamento,utente_aggiornamento,STATO_DOCUMENTO)
            SELECT n_iddoc, id_libreria, id_tipodoc,   'PLSQL-' || n_iddoc || '-DUPLICA-' || p_documento, area,
                        SYSDATE, p_utente,'BO'
              FROM documenti
             WHERE id_documento = p_documento;

      EXCEPTION WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20991', 'Impossibile duplicare il documento - Errore in insert Documenti ' || SQLERRM);
      END;

    BEGIN
         /* INSERISCO IL NUOVO DOCUMENTO  ORIZZONTALE*/
         IF p_duplica_tabella_oriz=1 THEN
             DUPLICA_TBL_ORIZZONTALE(p_documento, n_iddoc)  ;
         END IF;
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20991','Impossibile duplicare il documento - Errore in insert tabella orizzontale '|| SQLERRM  );
      END;

      BEGIN
         /* SETTO L'ACTIVITY_LOG  */
         INSERT INTO activity_log
                     (id_log, id_documento, tipo_azione, data_aggiornamento,
                      utente_aggiornamento)
            SELECT aclo_sq.NEXTVAL, n_iddoc, 'C', SYSDATE, p_utente
              FROM activity_log
             WHERE id_documento = p_documento;
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20992', 'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert Activity_log' || SQLERRM);
      END;

      BEGIN
         /* Copio i VALORI */
         INSERT INTO valori
                     (id_valore, id_documento, id_campo, valore_numero,
                      valore_data, valore_clob, data_aggiornamento,
                      utente_aggiornamento)
            SELECT valo_sq.NEXTVAL, n_iddoc, id_campo, valore_numero,
                       valore_data, valore_clob, SYSDATE, p_utente
              FROM valori
             WHERE id_documento = p_documento;
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20993', 'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert Valori ' || SQLERRM  );
      END;

      BEGIN
         /* Copio gli OGGETTI_FILE */
         INSERT INTO oggetti_file
                     (id_oggetto_file, id_documento, id_oggetto_file_padre,
                      id_formato, filename, "FILE", testoocr, allegato,
                      data_aggiornamento, utente_aggiornamento)
            SELECT ogg_file_sq.NEXTVAL, n_iddoc, id_oggetto_file_padre,
                     id_formato, filename, "FILE", testoocr, allegato, SYSDATE,
                      p_utente
              FROM oggetti_file
             WHERE id_documento = p_documento and (path_file is null);
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20994',  'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert oggetti_file '|| SQLERRM);
      END;

      BEGIN
         /* Setto lo sato a Bozza in STATI_DOCUMENTO  */
         INSERT INTO stati_documento
                     (id_documento, stato, commento, data_aggiornamento,
                      utente_aggiornamento )
              VALUES (n_iddoc, 'BO', 'DUPLICATO', SYSDATE,
                           p_utente);
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20995',
                                        'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert stati_documento ' || SQLERRM  );
      END;

      BEGIN
         /* Inserisco i  RIFERIMENTI */
         INSERT INTO riferimenti
                     (id_documento, id_documento_rif, libreria_remota,
                      tipo_relazione, data_aggiornamento,
                      utente_aggiornamento)
            SELECT n_iddoc, id_documento_rif, libreria_remota,
                   tipo_relazione, SYSDATE, p_utente
              FROM riferimenti
             WHERE id_documento = p_documento;
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20996', 'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert riferimenti'  || SQLERRM  );
      END;

      BEGIN
         /* Assegno le COMPETENZE  */
         INSERT INTO si4_competenze
                     (id_competenza, id_abilitazione, utente, oggetto,
                      accesso, ruolo, dal, al, data_aggiornamento,
                      utente_aggiornamento)
            SELECT comp_sq.NEXTVAL, id_abilitazione, utente, n_iddoc, 'S',
                   ruolo, dal, al, SYSDATE, p_utente
              FROM si4_competenze
             WHERE oggetto = to_char(p_documento)
               AND id_abilitazione IN (
                      SELECT id_abilitazione
                        FROM si4_abilitazioni a, si4_tipi_oggetto o
                       WHERE a.id_tipo_oggetto = o.id_tipo_oggetto
                         AND tipo_oggetto = 'DOCUMENTI')
               AND accesso = 'S'
               AND SYSDATE BETWEEN dal AND nvl(al,sysdate);
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20997', 'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert si4_competenze'  || SQLERRM  );
      END;

      BEGIN
         /* drop dei LINKS */
         INSERT INTO links
                     (id_link, id_cartella, id_oggetto, tipo_oggetto,
                      data_aggiornamento, utente_aggiornamento)
            SELECT link_sq.NEXTVAL, id_cartella, n_iddoc, tipo_oggetto,
                      SYSDATE, p_utente
              FROM links
             WHERE id_oggetto = p_documento AND tipo_oggetto = 'D';
      EXCEPTION  WHEN OTHERS THEN
            ROLLBACK;
            raise_application_error ('-20997', 'IMPOSSIBILE DUPLICARE IL DOCUMENTO - Errore in insert links'  || SQLERRM  );
      END;


      COMMIT;
      RETURN n_iddoc;

   END duplica_documento;
   FUNCTION consentidocumento (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_ruolo               IN   VARCHAR2 DEFAULT 'GDM',
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER
   AS
   BEGIN
      IF esistedocumento (p_id_documento) = 'N'
      THEN
         RETURN -4;
      ELSIF     gdm_competenza.si4_verifica ('DOCUMENTI',
                                             p_id_documento,
                                             'M',
                                             p_autore,
                                             p_ruolo,
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                            ) = 0
            AND p_escludicomp = 'N'
      THEN
         RETURN -5;
      ELSE
         RETURN gdm_competenza.assegna_comp ('DOCUMENTI',
                                             p_id_documento,
                                             p_tipi_abilitazione,
                                             p_utente,
                                             p_ruolo,
                                             NVL (p_autore, p_utente),
                                             'S',
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy'),
                                             NULL
                                            );
      END IF;
   END consentidocumento;
   FUNCTION consentidocumentodaa (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_dal                      VARCHAR2,
      p_al                       VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER
   AS
   BEGIN
      IF esistedocumento (p_id_documento) = 'N'
      THEN
         RETURN -4;
      ELSIF     gdm_competenza.si4_verifica ('DOCUMENTI',
                                             p_id_documento,
                                             'M',
                                             p_autore,
                                             'GDM',
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                            ) = 0
            AND p_escludicomp = 'N'
      THEN
         RETURN -5;
      ELSE
         RETURN gdm_competenza.assegna_comp ('DOCUMENTI',
                                             p_id_documento,
                                             p_tipi_abilitazione,
                                             p_utente,
                                             'GDM',
                                             NVL (p_autore, p_utente),
                                             'S',
                                             p_dal,
                                             p_al
                                            );
      END IF;
   END consentidocumentodaa;
   FUNCTION negadocumentodaa (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_dal                      VARCHAR2,
      p_al                       VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER
   AS
   BEGIN
      IF esistedocumento (p_id_documento) = 'N'
      THEN
         RETURN -4;
      ELSIF     gdm_competenza.si4_verifica ('DOCUMENTI',
                                             p_id_documento,
                                             'M',
                                             p_autore,
                                             'GDM',
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                            ) = 0
            AND p_escludicomp = 'N'
      THEN
         RETURN -5;
      ELSE
         RETURN gdm_competenza.assegna_comp ('DOCUMENTI',
                                             p_id_documento,
                                             p_tipi_abilitazione,
                                             p_utente,
                                             'GDM',
                                             NVL (p_autore, p_utente),
                                             'N',
                                             p_dal,
                                             p_al
                                            );
      END IF;
   END negadocumentodaa;
   FUNCTION negadocumento (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_ruolo               IN   VARCHAR2 DEFAULT 'GDM',
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER
   AS
   BEGIN
      IF esistedocumento (p_id_documento) = 'N'
      THEN
         RETURN -4;
      ELSIF     gdm_competenza.si4_verifica ('DOCUMENTI',
                                             p_id_documento,
                                             'M',
                                             p_autore,
                                             p_ruolo,
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                            ) = 0
            AND p_escludicomp = 'N'
      THEN
         RETURN -5;
      ELSE
         RETURN gdm_competenza.assegna_comp ('DOCUMENTI',
                                             p_id_documento,
                                             p_tipi_abilitazione,
                                             p_utente,
                                             p_ruolo,
                                             NVL (p_autore, p_utente),
                                             'N',
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy'),
                                             NULL
                                            );
      END IF;
   END negadocumento;
   FUNCTION esistedocumento (p_id_documento NUMBER)
      RETURN VARCHAR2
   AS
      ret   VARCHAR2 (1);
   BEGIN
      SELECT 'S'
        INTO ret
        FROM documenti
       WHERE id_documento = p_id_documento;
      RETURN ret;
   EXCEPTION
      WHEN OTHERS
      THEN
         RETURN 'N';
   END esistedocumento;
   FUNCTION cambiamodelloarea_documento (
      p_codice_modello   IN   VARCHAR2,
      p_area             IN   VARCHAR2,
      p_iddocumento      IN   NUMBER,
      p_user                  VARCHAR2
   )
      RETURN NUMBER
   AS
      a_ret              NUMBER (1);
      a_id_tipodoc       tipi_documento.id_tipodoc%TYPE;
      a_id_tipodoc_old   tipi_documento.id_tipodoc%TYPE;
      d_area             documenti.area%TYPE              := NULL;
   BEGIN
      --RICAVO IDTIPODOC DA P_CODICE_MODELLO
      BEGIN
         SELECT id_tipodoc
           INTO a_id_tipodoc
           FROM modelli
          WHERE codice_modello = p_codice_modello AND area = p_area;
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            raise_application_error (-20999,
                                        'CODICE MODELLO: '
                                     || p_codice_modello
                                     || ' INESISTENTE'
                                    );
      END;
      --GESTIONE TABELLA RICHIESTE, DOCUMENTI
      DECLARE
         d_cod_rich         documenti.codice_richiesta%TYPE   := NULL;
         d_area_controllo   documenti.area%TYPE               := NULL;
         d_check            NUMBER (1)                        := 0;
      BEGIN
         BEGIN
            SELECT codice_richiesta, area, id_tipodoc
              INTO d_cod_rich, d_area, a_id_tipodoc_old
              FROM documenti
             WHERE id_documento = p_iddocumento;
         EXCEPTION
            WHEN NO_DATA_FOUND
            THEN
               raise_application_error (-20999,
                                           'DOCUMENTO '
                                        || p_iddocumento
                                        || ' INESISTENTE'
                                       );
            WHEN OTHERS
            THEN
               raise_application_error (-20999,
                                           'ERRORE IN LETTURA DOCUMENTO '
                                        || p_iddocumento
                                        || ' '
                                        || SQLERRM
                                       );
         END;
         IF p_area = d_area
         THEN
            d_area_controllo := d_area;
         ELSE
            d_area_controllo := p_area;
         END IF;
         --CONTROLLO SE ESISTE LA TRIPLA
         --SU DOCUMENTI. SIA CHE ABBIA
         --CAMBIATO AREA CHE ABBIA
         --LASCIATO LA STESSA AREA
         --OVVIAMENTE HO CAMBIATO
         --TIPO_DOCUMENTO
         BEGIN
            SELECT COUNT (*)
              INTO d_check
              FROM documenti
             WHERE codice_richiesta = d_cod_rich
               AND area = d_area_controllo
               AND id_tipodoc = a_id_tipodoc;
            IF d_check >= 1
            THEN
               raise_application_error
                  (-20999,
                      'IMPOSSIBILE CAMBIARE IL MODELLO. ESISTE GIA'' UN CODICE RICHIESTA '
                   || d_cod_rich
                   || ' PER (AREA: '
                   || d_area_controllo
                   || ',TIPO_DOCUMENTO: '
                   || p_iddocumento
                   || ')'
                  );
            END IF;
         END;
         d_check := 0;
         --SE L'AREA E' DIVERSA DEVO AGGIORNARLA SU DOCUMENTI
         --E FACCIO DEI CONTROLLI SU RICHIESTE
         IF p_area <> d_area
         THEN
            --CONTROLLO SE ESISTE GIA'
            --LA RICHIESTA SULLA NUOVA AREA
            SELECT COUNT (*)
              INTO d_check
              FROM richieste
             WHERE codice_richiesta = d_cod_rich AND area = p_area;
            --NON ESISTE LA RICHIESTA
            --LA INSERISCO
            IF d_check = 0
            THEN
               INSERT INTO richieste
                           (codice_richiesta, area, id_tipo_pratica,
                            data_scadenza, data_inserimento
                           )
                    VALUES (d_cod_rich, p_area, NULL,
                            NULL, SYSDATE
                           );
            END IF;
         END IF;
         UPDATE documenti
            SET area = p_area,
                id_tipodoc = a_id_tipodoc
          WHERE id_documento = p_iddocumento;
      END;
      --SETTAGGIO DEI CAMPI E VALORI
      DECLARE
         d_tipo_new   dati.tipo%TYPE;
         d_lung_new   dati.lunghezza%TYPE;
         d_tipo_old   dati.tipo%TYPE;
         d_lung_old   dati.lunghezza%TYPE;
         CURSOR c_campi_identici
         IS
            SELECT c1.id_campo AS id_campo_old, c2.id_campo AS id_campo_new,
                   c1.nome AS nome
              FROM campi_documento c1, campi_documento c2
             WHERE c1.nome = c2.nome
               AND c1.id_tipodoc = a_id_tipodoc_old
               AND c2.id_tipodoc = a_id_tipodoc;
      BEGIN
         --PER OGNI CAMPO COINCIDENTE FRA I DUE MODELLI
         --TESTO SE SONO COMPATIBILI
         FOR c_campi IN c_campi_identici
         LOOP
            SELECT d_old.tipo, d_old.lunghezza, d_new.tipo, d_new.lunghezza
              INTO d_tipo_old, d_lung_old, d_tipo_new, d_lung_new
              FROM dati d_old, dati d_new
             WHERE d_old.area = d_area
               AND d_old.dato = c_campi.nome
               AND d_new.area = p_area
               AND d_new.dato = c_campi.nome;
            --AGGIORNO L'IDCAMPO SULLA TABELLA VALORI
            UPDATE valori
               SET id_campo = c_campi.id_campo_new
             WHERE id_campo = c_campi.id_campo_old
               AND id_documento = p_iddocumento;
            --NON SONO COMPATIBILI - ELIMINO IL CONTENUTO
            IF d_tipo_old <> d_tipo_new
            THEN
               UPDATE valori
                  SET valore_clob = EMPTY_CLOB (),
                      valore_data = NULL,
                      valore_numero = NULL
                WHERE id_campo = c_campi.id_campo_new
                  AND id_documento = p_iddocumento;
            END IF;
         END LOOP;
         --ELIMINO I CAMPI/VALORI PRESENTI NEL MODELLO VECCHIO
         --MA CHE NON SONO PRESENTI SUL NUOVO MODELLO
         DELETE FROM valori
               WHERE id_campo IN (
                        SELECT c2.id_campo
                          FROM campi_documento c2
                         WHERE c2.id_campo NOT IN (
                                  SELECT c1.id_campo
                                    FROM campi_documento c1,
                                         campi_documento c2
                                   WHERE c1.nome = c2.nome
                                     AND c1.id_tipodoc = a_id_tipodoc_old
                                     AND c2.id_tipodoc = a_id_tipodoc)
                           AND c2.id_tipodoc = a_id_tipodoc_old)
                 AND id_documento = p_iddocumento;
         --INSERISCO I CAMPI/VALORI PRESENTI NEL MODELLO NUOVO
         --MA CHE NON ERANO PRESENTI NEL VECCHIO MODELLO
         INSERT INTO valori
                     (id_valore, id_documento, id_campo, valore_numero,
                      valore_data, valore_clob, data_aggiornamento,
                      utente_aggiornamento)
            SELECT valo_sq.NEXTVAL, p_iddocumento, c2.id_campo,
                   TO_NUMBER (NULL), TO_DATE (NULL), EMPTY_CLOB (), SYSDATE,
                   p_user
              FROM campi_documento c2
             WHERE c2.id_campo NOT IN (
                      SELECT c2.id_campo
                        FROM campi_documento c1, campi_documento c2
                       WHERE c1.nome = c2.nome
                         AND c1.id_tipodoc = a_id_tipodoc_old
                         AND c2.id_tipodoc = a_id_tipodoc)
               AND c2.id_tipodoc = a_id_tipodoc;
      END;
      RETURN 1;
   END;
   PROCEDURE delete_allegati(P_ID_DOCUMENTO IN NUMBER)
   AS
   BEGIN
        FOR I IN (SELECT ID_OGGETTO_FILE FROM OGGETTI_FILE  WHERE ID_DOCUMENTO = P_ID_DOCUMENTO)
        LOOP
            GDM_OGGETTI_FILE.DELETEOGGETTOFILE(I.ID_OGGETTO_FILE);
        END LOOP;
   END;
      PROCEDURE insert_update_allegato
   (    p_id_documento in number,
        p_filename in varchar2,
        p_estensione in varchar2,
        p_file     in BLOB,
        p_utente in varchar2)
    AS
    a_ret number(10);
    BEGIN
       a_ret:=  insert_update_allegato
        (    p_id_documento,
        p_filename ,
        p_estensione ,
        p_file  ,
        p_utente)  ;
    END;
   FUNCTION insert_update_allegato(P_ID_DOCUMENTO IN NUMBER,
                                    P_FILENAME     IN VARCHAR2,
                                    P_ESTENSIONE   IN VARCHAR2,
                                    P_FILE         IN BLOB,
                                    P_UTENTE       IN VARCHAR2,
                                    p_fileonfs in number default 0,
                                    p_typefs in varchar2 default 'LNX') RETURN NUMBER
   AS
   A_ID         OGGETTI_FILE.ID_OGGETTO_FILE%TYPE;
   A_IDFORMATO  FORMATI_FILE.ID_FORMATO%TYPE;
   A_TIPO       VARCHAR2(50) := 'aggiornamento';
   BEGIN
      BEGIN
       SELECT ID_OGGETTO_FILE
         INTO A_ID
         FROM OGGETTI_FILE
        WHERE ID_DOCUMENTO=P_ID_DOCUMENTO AND FILENAME=P_FILENAME;
      EXCEPTION WHEN NO_DATA_FOUND THEN
         A_ID   := NULL;
         A_TIPO := 'inserimento';
      END;
      IF A_ID IS NULL THEN
         SELECT OGG_FILE_SQ.NEXTVAL
           INTO A_ID
           FROM DUAL;
         BEGIN
           SELECT ID_FORMATO
           INTO A_IDFORMATO
           FROM FORMATI_FILE
           WHERE UPPER(NOME)=UPPER(P_ESTENSIONE);
         EXCEPTION WHEN NO_DATA_FOUND THEN
              A_IDFORMATO:=0;
         END;
         INSERT INTO OGGETTI_FILE
         (ID_OGGETTO_FILE, ID_DOCUMENTO, ID_OGGETTO_FILE_PADRE,
          ID_FORMATO, FILENAME, ALLEGATO, DATA_AGGIORNAMENTO,
          UTENTE_AGGIORNAMENTO, TESTOOCR
         )
         VALUES
         (A_ID, P_ID_DOCUMENTO, NULL,
          A_IDFORMATO, P_FILENAME, 'N', SYSDATE,
          P_UTENTE, decode(p_fileonfs,0,P_FILE,empty_blob())
         );
      ELSE
         UPDATE OGGETTI_FILE
            SET DATA_AGGIORNAMENTO = SYSDATE,
                UTENTE_AGGIORNAMENTO = P_UTENTE,
                TESTOOCR = decode(p_fileonfs,0,P_FILE,empty_blob())
          WHERE ID_OGGETTO_FILE = A_ID;
      END IF;

      IF p_fileonfs=1 THEN
          GDM_OGGETTI_FILE.OGGETTO_FILE_TO_FS_NOCOMMIT(A_ID, -1,1, 1, P_FILE,p_typefs );
      END IF;

      RETURN A_ID;

   EXCEPTION WHEN OTHERS THEN
      RAISE_APPLICATION_ERROR(-20999,'Errore in '||A_TIPO||' oggetto_file. Errore: '||sqlerrm);
   END;
END gdm_profilo;
/


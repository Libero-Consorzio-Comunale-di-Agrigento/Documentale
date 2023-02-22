CREATE OR REPLACE PACKAGE BODY GDM_COMPETENZA
AS

PROCEDURE set_true IS
BEGIN
    g_diritto := 1;
END;

PROCEDURE set_false IS
BEGIN
    g_diritto := 0;
END;

PROCEDURE resetta IS
BEGIN
    g_diritto := NULL;
END;


PROCEDURE
/******************************************************************************
 NOME:        si4_assegna_multipla
 DESCRIZIONE: Richiama la si4_competenza.assegna_multipla
              a cui bisogna passare la lista delle abilitazioni separate da ;
******************************************************************************/
si4_assegna_multipla (
                                            p_tipo_oggetto  IN VARCHAR2,
                                            p_oggetto IN VARCHAR2,
                                            p_tipo_abilitazione IN VARCHAR2,
                                            p_utente IN VARCHAR2,
                                            p_ruolo  IN VARCHAR2 DEFAULT NULL,
                                            p_autore IN VARCHAR2,
                                            p_accesso IN VARCHAR2 DEFAULT 'S',
                                            p_dal IN VARCHAR2 DEFAULT NULL,
                                            p_al IN VARCHAR2 DEFAULT NULL)
AS
 retval     NUMBER (2) := 0;
 sabil VARCHAR2 (1);
BEGIN

    BEGIN
    retval := si4_competenza.assegna_multipla (p_tipo_oggetto,
                                                                                    p_oggetto,
                                                                                    p_tipo_abilitazione,
                                                                                    p_utente,
                                                                                    p_ruolo,
                                                                                    p_autore,
                                                                                    p_accesso,
                                                                                    p_dal,
                                                                                    p_al);
    END;

    IF RETVAL<0 THEN
        raise_application_error('-20999',getStringException(retval,p_tipo_abilitazione,p_oggetto));
    END IF;

END si4_assegna_multipla;

PROCEDURE
/******************************************************************************
 NOME:        si4_assegna
 DESCRIZIONE: Richiama la si4_competenza.assegna
              a cui bisogna passare la lista delle abilitazioni:
                    - O separate da ;
                    - Se senza ; verranno separati i caratteri 1 ad 1.
******************************************************************************/
si4_assegna (p_tipo_oggetto IN VARCHAR2,
                        p_oggetto IN VARCHAR2,
                        p_tipo_abilitazione IN VARCHAR2,
                        p_utente IN VARCHAR2,
                        p_ruolo IN VARCHAR2 DEFAULT NULL,
                        p_autore IN VARCHAR2,
                        p_accesso IN VARCHAR2 DEFAULT 'S',
                        p_dal IN VARCHAR2 DEFAULT NULL,
                        p_al IN VARCHAR2 DEFAULT NULL)
AS
 retval NUMBER (2) := 0;
 arrayListAbil ARRSTRING;
BEGIN
        RETVAL := assegna_comp(p_tipo_oggetto,
                                                     p_oggetto,
                                                     p_tipo_abilitazione,
                                                     p_utente,
                                                     p_ruolo,
                                                     p_autore,
                                                     p_accesso,
                                                     p_dal,
                                                     p_al,
                                                     NULL);

        IF RETVAL<0 THEN
            raise_application_error('-20999',getStringException(retval,p_tipo_abilitazione,p_oggetto));
        END IF;
END si4_assegna;

 FUNCTION
 /******************************************************************************
 NOME:        assegna_comp
 DESCRIZIONE: Richiama la si4_competenza.assegna
              a cui bisogna passare la lista delle abilitazioni:
                    - O separate da ;
                    - Se senza ; verranno separati i caratteri 1 ad 1.
               rispetto alla GDM_COMPETENZA.si4_assegna ha in pi¿
               il parametro p_id_funzione e la restituzione del retval piuttosto
               che l'exception
******************************************************************************/
assegna_comp (p_tipo_oggetto IN VARCHAR2,
                             p_oggetto IN VARCHAR2,
                             p_tipo_abilitazione IN VARCHAR2,
                             p_utente IN VARCHAR2,
                             p_ruolo IN VARCHAR2 DEFAULT NULL,
                             p_autore IN VARCHAR2,
                             p_accesso IN VARCHAR2 DEFAULT 'S',
                             p_dal IN VARCHAR2 DEFAULT NULL,
                             p_al IN VARCHAR2 DEFAULT NULL,
                             p_id_funzione IN VARCHAR2 DEFAULT NULL)
RETURN NUMBER
AS
 retval NUMBER (2) := 0;
 arrayListAbil ARRSTRING;
BEGIN

    arrayListAbil:=getArrayListAbil(p_tipo_abilitazione);

    for i in arrayListAbil.first..arrayListAbil.last
    loop
   dbms_output.put_line('TA= '||arrayListAbil(i));

        retval := si4_competenza.assegna(p_tipo_oggetto,
                                                                      p_oggetto,
                                                                      arrayListAbil(i),
                                                                      p_utente,
                                                                      p_ruolo,
                                                                      p_autore,
                                                                      p_accesso,
                                                                      p_dal,
                                                                      p_al,
                                                                      p_id_funzione);

        IF RETVAL<0 THEN
            RETURN RETVAL;
        END IF;
    end loop;

    RETURN 0;
 END assegna_comp;
PROCEDURE
/******************************************************************************
 NOME:        gdm_assegna
 DESCRIZIONE:
                    RICHIAMATA SOLO IN CREAZIONE DI UN DOCUMENTO PER DUPLICARE
                    LE COMPETENZE DEFINITE SUL TIPO_DOCUMENTO
                    PER IL MOMENTO LA COPIA DELLE COMPETENZE FUNZIONALI E' DISATTIVATA
                    IN QUANTO E' DEMANDATA ALLA FUNZIONE DI VERIFICA
******************************************************************************/
gdm_assegna (p_tipo_oggetto IN VARCHAR2,
                           p_oggetto IN VARCHAR2,
                           p_da_tipo_oggetto IN VARCHAR2,
                           p_da_oggetto IN VARCHAR2,
                           p_autore IN VARCHAR2,
                           p_ruolo IN VARCHAR2 DEFAULT NULL)
AS
 retval NUMBER (2) := 0;
 d_nome_funzione si4_funzioni.nome%TYPE;
 CURSOR c_listaAbilDaTipoOggetto IS
                 SELECT DISTINCT d.utente, d.ruolo, d.accesso, c.tipo_abilitazione,
                              SYSDATE dal, d.al, d.tipo_competenza, d.id_funzione
                 FROM si4_abilitazioni a,
                             si4_tipi_oggetto b,
                             si4_tipi_abilitazione c,
                             si4_competenze d
                 WHERE a.id_tipo_oggetto = b.id_tipo_oggetto
                      AND a.id_tipo_abilitazione = c.id_tipo_abilitazione
                      AND a.id_abilitazione = d.id_abilitazione
                      AND d.oggetto = p_da_oggetto
                      AND b.tipo_oggetto = p_da_tipo_oggetto
                      AND c.tipo_abilitazione <> 'C'
                      AND SYSDATE BETWEEN d.dal AND NVL (d.al, SYSDATE)
                      AND d.accesso = 'S'
                      AND d.tipo_competenza IN ('U');
 BEGIN

    FOR clistaAbilDaTipoOggetto IN c_listaAbilDaTipoOggetto LOOP
             retval := 0;
            IF (clistaAbilDaTipoOggetto.tipo_abilitazione not in ('X','be','bl','ba','LA','UA','DA') ) THEN
                   retval := si4_competenza.assegna (p_tipo_oggetto,
                                                                                 p_oggetto,
                                                                                 clistaAbilDaTipoOggetto.tipo_abilitazione,
                                                                                 clistaAbilDaTipoOggetto.utente,
                                                                                 clistaAbilDaTipoOggetto.ruolo,
                                                                                 p_autore,
                                                                                 clistaAbilDaTipoOggetto.accesso,
                                                                                 TO_CHAR (clistaAbilDaTipoOggetto.dal, 'dd/mm/yyyy'),
                                                                                 TO_CHAR (clistaAbilDaTipoOggetto.al, 'dd/mm/yyyy'),
                                                                                 d_nome_funzione,
                                                                                 0);
            END IF;

           IF RETVAL<0 THEN
                raise_application_error('-20999',getStringException(retval, clistaAbilDaTipoOggetto.tipo_abilitazione,p_oggetto));
           END IF;
    END LOOP;

END gdm_assegna;
PROCEDURE
/******************************************************************************
 NOME:        gdm_assegna_gruppo
 DESCRIZIONE:
                    DATO UN UTENTE X ED UNA COMPETENZA, QUEST'ULTIMA VIENE SPALMATA
                    SU TUTTI  I GRUPPI A CUI APPARTIENE X (LE ABILITAZIONE POSSONO ESSERE
                    ESSERE MULTIPLE O SINGOLE)
******************************************************************************/
gdm_assegna_gruppo (p_tipo_oggetto IN VARCHAR2,
                                         p_oggetto IN VARCHAR2,
                                         p_tipo_abilitazione IN VARCHAR2,
                                         p_utente IN VARCHAR2,
                                         p_ruolo IN VARCHAR2 DEFAULT NULL,
                                         p_autore IN VARCHAR2,
                                         p_accesso IN VARCHAR2 DEFAULT 'S',
                                         p_dal IN VARCHAR2 DEFAULT NULL,
                                         p_al IN VARCHAR2 DEFAULT NULL)
AS
 retval NUMBER (2) := 0;
CURSOR c_ElencoGruppi IS
                SELECT gruppo
                FROM ad4_utenti_gruppo
                WHERE utente = p_utente
                UNION ALL
                SELECT p_utente
                FROM DUAL;
BEGIN
    FOR cElencoGruppi IN c_ElencoGruppi LOOP
              si4_assegna (p_tipo_oggetto,
                                         p_oggetto,
                                         p_tipo_abilitazione,
                                         cElencoGruppi.gruppo,
                                         p_ruolo,
                                         p_autore,
                                         p_accesso,
                                         p_dal,
                                         p_al);
    END LOOP;
 END gdm_assegna_gruppo;

PROCEDURE
 /******************************************************************************
 NOME:        gdm_assegna_gruppo
 DESCRIZIONE:
                    DATO UN UTENTE X ED UNA COMPETENZA, QUEST'ULTIMA VIENE SPALMATA
                    SU TUTTI  I GRUPPI A CUI APPARTIENE X (LE ABILITAZIONE DEVONO
                    ESSERE SEPARATE DA ;)
******************************************************************************/
gdm_assegna_gruppo_multipla (p_tipo_oggetto IN VARCHAR2,
                                                             p_oggetto IN VARCHAR2,
                                                             p_tipo_abilitazione IN VARCHAR2,
                                                             p_utente IN VARCHAR2,
                                                             p_ruolo IN VARCHAR2 DEFAULT NULL,
                                                             p_autore IN VARCHAR2,
                                                             p_accesso IN VARCHAR2 DEFAULT 'S',
                                                             p_dal IN VARCHAR2 DEFAULT NULL,
                                                             p_al IN VARCHAR2 DEFAULT NULL)
AS
 retval NUMBER (2) := 0;
 CURSOR c_ElencoGruppi IS
                SELECT gruppo
                FROM ad4_utenti_gruppo
                WHERE utente = p_utente
                UNION ALL
                SELECT p_utente
                FROM DUAL;
BEGIN
    FOR cElencoGruppi IN c_ElencoGruppi LOOP
            si4_assegna_multipla(p_tipo_oggetto,
                                                     p_oggetto,
                                                     p_tipo_abilitazione,
                                                     cElencoGruppi.gruppo,
                                                     p_ruolo,
                                                     p_autore,
                                                     p_accesso,
                                                     p_dal,
                                                     p_al);

    END LOOP;
END gdm_assegna_gruppo_multipla;

FUNCTION
 /******************************************************************************
 NOME:        gdm_aggiungi_a_tutti
 DESCRIZIONE:
                    DATO UN TIPO OGGETTO VENGONO RICERCATI TUTTI GLI UTENTI CHE GIA'
                    HANNO QUALCHE COMPETENZA PER QUEL TIPO OGGETTO E GLI VENGONO
                    AGGIUNTE/TOLTE QUELLE SPECIFICATE IN  p_oggetto / p_tipo_abilitazione
******************************************************************************/
gdm_aggiungi_a_tutti (p_tipo_oggetto IN VARCHAR2,
                                         p_oggetto IN VARCHAR2,
                                         p_tipo_abilitazione IN VARCHAR2,
                                         p_accesso IN VARCHAR2 DEFAULT 'S',
                                         p_autore IN VARCHAR2,
                                         p_dal IN VARCHAR2 DEFAULT NULL,
                                         p_al IN VARCHAR2 DEFAULT NULL)
RETURN NUMBER
AS
 retval NUMBER (2) := 0;
 len NUMBER (2) := 0;
 sabil VARCHAR2 (10);
 CURSOR c_ElencoUtenti IS
                 SELECT DISTINCT utente, ruolo
                 FROM si4_competenze comp,
                             si4_abilitazioni abil,
                             si4_tipi_oggetto tiog
                WHERE oggetto = p_oggetto
                     AND comp.id_abilitazione = abil.id_abilitazione
                     AND abil.id_tipo_oggetto = tiog.id_tipo_oggetto
                     AND tiog.tipo_oggetto = p_tipo_oggetto;
BEGIN
    FOR cElencoUtenti IN c_ElencoUtenti LOOP
              si4_assegna (p_tipo_oggetto,
                                         p_oggetto,
                                         p_tipo_abilitazione,
                                         cElencoUtenti.utente,
                                         cElencoUtenti.ruolo,
                                         p_autore,
                                         p_accesso,
                                         p_dal,
                                         p_al);
    END LOOP;
   RETURN 0;
END gdm_aggiungi_a_tutti;

PROCEDURE
 /******************************************************************************
 NOME:        gdm_allinea_comp_cq_doc
 DESCRIZIONE:
                    CURSORE CHE TIRA FUORI TUTTI LE X DAL TIPO DOC E CHE LE ASSEGNA COME C ALLA
                    VIEW_CARTELLA. QUESTE X NON ERANO PRESENTI SUI DOCUMENTI PERCHE'
                    LA C SUL DOCUMENTO NON PUO' ESISTERE
******************************************************************************/
gdm_allinea_comp_cq_doc (p_idview_cartella NUMBER,
                                                    p_id_doc NUMBER,
                                                    p_autore VARCHAR2,
                                                    p_cart_query VARCHAR2)
AS
BEGIN
    DECLARE
        retval NUMBER (2) := 0;
        tipooggetto VARCHAR2 (15);
        tipoabil VARCHAR2 (100) := '';

        CURSOR c_xtipidoc IS
                        SELECT DISTINCT a.utente, a.ruolo, a.accesso, SYSDATE dal, a.al
                        FROM si4_competenze a,
                                    si4_abilitazioni abil,
                                    si4_tipi_oggetto b,
                                    documenti doc
                        WHERE doc.id_documento = p_id_doc
                        AND a.oggetto = TO_CHAR (doc.id_tipodoc)
                        AND a.tipo_competenza = 'U'
                        AND a.id_abilitazione = abil.id_abilitazione
                        AND abil.id_tipo_oggetto = b.id_tipo_oggetto
                        AND b.tipo_oggetto = 'TIPI_DOCUMENTO'
                        AND abil.id_tipo_abilitazione = 5
                        AND SYSDATE BETWEEN a.dal AND NVL (a.al, SYSDATE);
 BEGIN

    IF p_cart_query = 'C' THEN
            tipooggetto := 'VIEW_CARTELLA';
    ELSE
            tipooggetto := 'QUERY';
    END IF;

    IF p_cart_query = 'C' THEN
         FOR cxtipidoc IN c_xtipidoc LOOP
                  retval := si4_competenza.assegna ('VIEW_CARTELLA',
                                                                                  TO_CHAR (p_idview_cartella),
                                                                                  'C',
                                                                                  cxtipidoc.utente,
                                                                                  cxtipidoc.ruolo,
                                                                                  p_autore,
                                                                                  cxtipidoc.accesso,
                                                                                  TO_CHAR (cxtipidoc.dal,'dd/mm/yyyy'),
                                                                                  TO_CHAR (cxtipidoc.al, 'dd/mm/yyyy'));
                 IF RETVAL<0 THEN
                      raise_application_error('-20999',getStringException(retval,'C',TO_CHAR (p_idview_cartella)));
                 END IF;
         END LOOP;
    END IF;

    -- INFINE AGGIUNGO LA COMPETENZA DI CREAZIONE SULLA VIEW_CARTELLA PER L'AUTORE
    INSERT INTO si4_competenze
    (id_abilitazione, utente, oggetto, accesso, ruolo, dal,al,
      data_aggiornamento, utente_aggiornamento,
      id_funzione, tipo_competenza)
    SELECT abilview.id_abilitazione, utente,
    TO_CHAR (p_idview_cartella), accesso, ruolo, dal, al,
    data_aggiornamento, utente_aggiornamento, id_funzione,tipo_competenza
    FROM si4_competenze,
                si4_abilitazioni,
                si4_tipi_abilitazione,
                si4_tipi_oggetto,
                si4_abilitazioni abilview,
                si4_tipi_oggetto tiogview
    WHERE oggetto = TO_CHAR (p_id_doc)
        AND tipo_competenza IN ('U', 'F')
        AND si4_abilitazioni.id_abilitazione = si4_competenze.id_abilitazione
        AND si4_tipi_abilitazione.id_tipo_abilitazione = si4_abilitazioni.id_tipo_abilitazione
        AND si4_abilitazioni.id_tipo_oggetto = si4_tipi_oggetto.id_tipo_oggetto
        AND si4_tipi_oggetto.tipo_oggetto = 'DOCUMENTI'
        AND tiogview.tipo_oggetto = tipooggetto
        AND abilview.id_tipo_oggetto = tiogview.id_tipo_oggetto
        AND abilview.id_tipo_abilitazione = si4_abilitazioni.id_tipo_abilitazione;

        retval := si4_competenza.assegna (tipooggetto,
                                                                        TO_CHAR (p_idview_cartella),
                                                                        'C',
                                                                        p_autore,
                                                                        f_trasla_ruolo (p_autore,'GDMWEB','GDMWEB'),
                                                                        p_autore,
                                                                        'S',
                                                                        TO_CHAR (SYSDATE, 'DD/MM/YYYY'),
                                                                        NULL);
         IF RETVAL<0 THEN
              raise_application_error('-20999',getStringException(retval,'C',TO_CHAR (p_idview_cartella)));
         END IF;

 END;
 END gdm_allinea_comp_cq_doc;


PROCEDURE gdm_comp_tipodoc_doc_allegati(id_doc NUMBER, p_utente VARCHAR2)
 /******************************************************************************
 NOME:        gdm_comp_tipodoc_doc_allegati
 DESCRIZIONE: CURSORE CHE TIRA FUORI TUTTI LE COMPETENZE SUGLI ALLEGATI
              DEI TIPODOC DEL DOC PASSATO IN INPUT E LE METTE
              SUL DOCUMENTO. PROC. RICHIAMATA IN FASE DI CREAZIONE DEL
              DOCUMENTO.
              VIENE INOLTRE AGGIUNTA LA COMPETENZA A S SU MODIFICA,
              CANCELLAZIONE, LETTURA PER L'UTENTE CHIAMANTE (p_utente)
******************************************************************************/
AS
BEGIN
    DECLARE
        idTipoDoc       TIPI_DOCUMENTO.ID_TIPODOC%TYPE;
        compAll         TIPI_DOCUMENTO.COMPETENZE_ALLEGATI%TYPE;
        retval          NUMBER (2) := 0;
        tipoUso         MODELLI.TIPO_USO%TYPE;
        abilArrayList   ARRSTRING;
        i               NUMBER (1);
        accesso         VARCHAR2 (1);

        CURSOR c_xtipidoc(p_oggetto VARCHAR2) IS
                SELECT DISTINCT a.utente, a.ruolo, a.accesso, SYSDATE dal, a.al, TIPIAB.TIPO_ABILITAZIONE
                FROM si4_competenze a,
                     si4_abilitazioni abil,
                     si4_tipi_oggetto b ,
                     si4_tipi_abilitazione tipiAb
                WHERE a.oggetto = p_oggetto
                AND a.tipo_competenza = 'U'
                AND a.id_abilitazione = abil.id_abilitazione
                AND abil.id_tipo_oggetto = b.id_tipo_oggetto
                AND b.tipo_oggetto = 'TIPI_DOCUMENTO'
                AND abil.id_tipo_abilitazione = TIPIAB.ID_TIPO_ABILITAZIONE
                AND TIPIAB.TIPO_ABILITAZIONE in ('LA','UA','DA')
                AND trunc(Sysdate )  BETWEEN a.dal  AND NVL (a.al, trunc(Sysdate ))
                Order by 1,2,3,6;
    BEGIN

        SELECT DOCUMENTI.ID_TIPODOC, TIPI_DOCUMENTO.COMPETENZE_ALLEGATI, MODELLI.TIPO_USO
          INTO idTipoDoc, compAll, tipoUso
          FROM DOCUMENTI, TIPI_DOCUMENTO, MODELLI
         WHERE DOCUMENTI.ID_DOCUMENTO = id_doc
           AND DOCUMENTI.ID_TIPODOC = TIPI_DOCUMENTO.ID_TIPODOC
           AND MODELLI.ID_TIPODOC = TIPI_DOCUMENTO.ID_TIPODOC;

        IF compAll<>'N' AND (tipoUso not in ('C','Q','F','V','W','R'))  THEN
           abilArrayList:= ARRSTRING();
           abilArrayList.extend;
           abilArrayList(1) := 'LA';
           abilArrayList.extend;
           abilArrayList(2) := 'UA';
           abilArrayList.extend;
           abilArrayList(3) := 'DA';


           FOR cxtipidoc IN c_xtipidoc(idTipoDoc) LOOP
                 retval := si4_competenza.assegna ('DOCUMENTI',
                                                     TO_CHAR (id_doc),
                                                     cxtipidoc.TIPO_ABILITAZIONE,
                                                     cxtipidoc.utente,
                                                     cxtipidoc.ruolo,
                                                     p_utente,
                                                     cxtipidoc.accesso,
                                                     TO_CHAR (cxtipidoc.dal,'dd/mm/yyyy'),
                                                     TO_CHAR (cxtipidoc.al, 'dd/mm/yyyy'));
                 IF RETVAL<0 THEN
                      raise_application_error('-20999',getStringException(retval,cxtipidoc.TIPO_ABILITAZIONE,TO_CHAR (id_doc)));
                 END IF;
            END LOOP;

            -- ASSEGNO LA COMP ANCHE PER L'UTENTE CHIAMANTE
            FOR i in 1..3 LOOP

                 select decode(GDM_COMPETENZA.GDM_VERIFICA ( 'TIPI_DOCUMENTO', TO_CHAR(idTipoDoc), abilArrayList(i), p_utente, f_trasla_ruolo(p_utente,'GDMWEB','GDMWEB')),
                                1,'S','N')
                 into accesso
                 from dual;


                 retval := si4_competenza.assegna ('DOCUMENTI',
                                                     TO_CHAR (id_doc),
                                                     abilArrayList(i),
                                                     p_utente,
                                                     f_trasla_ruolo(p_utente,'GDMWEB','GDMWEB'),
                                                     p_utente,
                                                     accesso,
                                                     TO_CHAR (SYSDATE,'dd/mm/yyyy'));
                 IF RETVAL<0 THEN
                      raise_application_error('-20999',getStringException(retval,abilArrayList(i),TO_CHAR (id_doc)));
                 END IF;
            END LOOP;
        END IF;
    END;
END;

PROCEDURE
/******************************************************************************
 NOME:        gdm_elimina
 DESCRIZIONE:
                    ELIMINA FISICAMENTE LE COMPETENZE PER OGGETTO E TIPO OGGETTO
                    PASSATI IN INPUT
******************************************************************************/
gdm_elimina (p_tipo_oggetto IN VARCHAR2,
                          p_oggetto IN VARCHAR2)
AS
 BEGIN
    DELETE FROM si4_competenze
    WHERE oggetto = p_oggetto
    AND id_abilitazione IN (
            SELECT id_abilitazione
            FROM si4_abilitazioni, si4_tipi_oggetto
            WHERE si4_tipi_oggetto.id_tipo_oggetto = si4_abilitazioni.id_tipo_oggetto
            AND si4_tipi_oggetto.tipo_oggetto = p_tipo_oggetto);

    EXCEPTION WHEN OTHERS THEN raise_application_error ('-20999', SQLERRM);
 END;

FUNCTION
/******************************************************************************
 NOME:        si4_verifica
 DESCRIZIONE:
                    VERIFICA DELLE COMPETENZE (RICHIAMO DELLA GDM_VERIFICA)
******************************************************************************/
si4_verifica (p_tipo_oggetto IN VARCHAR2,
                                            p_oggetto IN VARCHAR2,
                                            p_tipo_abilitazione IN VARCHAR2,
                                            p_utente IN VARCHAR2,
                                            p_ruolo IN VARCHAR2 DEFAULT NULL,
                                            p_data IN VARCHAR2 DEFAULT TO_CHAR (SYSDATE,'dd/mm/yyyy'))
RETURN NUMBER
AS
BEGIN
    RETURN gdm_verifica (p_tipo_oggetto,
                                             p_oggetto,
                                             p_tipo_abilitazione,
                                             p_utente,
                                             p_ruolo,
                                             p_data);
END si4_verifica;

FUNCTION
/*****************************************************************************************************
 NOME:       gdm_verifica
 DESCRIZIONE: Verifica l'abilitazione di accesso ad una certa data restituendo il valore 1 o 0
                             in funzione del contenuto nel campo ACCESSO.
                             Ordine di esecuzione delle verifiche (appena trova la competenza interrompe l'operazione):
                             a) competenza sull'oggetto (comprese le sue competenze funzionali ---> lo fa SI4_COMPETENZE)
                             b) competenza sul tipo documento se l'oggetto ¿ un DOCUMENTO
                             c) competenza per l'utente GUEST, ma ¿ da PARAMETRIZZARE in base al TIPO_DOCUMENTO
                             Ritorna:
                             1 : se esiste il diritto di accesso
                             0 : se non esiste diritto di accesso
*****************************************************************************************************/
gdm_verifica (p_tipo_oggetto IN VARCHAR2,
                         p_oggetto IN VARCHAR2,
                         p_tipo_abilitazione IN VARCHAR2,
                         p_utente IN VARCHAR2,
                         p_ruolo IN VARCHAR2 DEFAULT NULL,
                         p_data IN VARCHAR2 DEFAULT TO_CHAR (SYSDATE,'dd/mm/yyyy'),
                         P_CONTROLLA_IDPADRE IN VARCHAR2 DEFAULT 'N')
RETURN NUMBER
AS
 retval NUMBER (1) := null;
 padre DOCUMENTI.ID_DOCUMENTO_PADRE%TYPE;
 a number(1);


 BEGIN
    --select count(1) into a from documenti , documenti, documenti , ad4_utenti;

    --SE E' UNA COMPETENZA SU DOC/QUERY/CARTELLA CONTROLLO PRIMA LA SUA COMPETENZA FUNZIONALE
    IF (p_tipo_oggetto = 'DOCUMENTI') OR (p_tipo_oggetto = 'QUERY') OR (p_tipo_oggetto = 'VIEW_CARTELLA') THEN
            retval := gdm_competenza.gdm_ver_funz_tipodoc ('1',
                                                                                                        p_tipo_oggetto,
                                                                                                        p_oggetto,
                                                                                                        p_tipo_abilitazione,
                                                                                                        p_utente,
                                                                                                        p_ruolo,
                                                                                                        p_data);
    END IF;

    IF (retval IS NULL) THEN
        retval := si4_competenza.verifica ('1',
                                                                      p_tipo_oggetto,
                                                                      p_oggetto,
                                                                      p_tipo_abilitazione,
                                                                      p_utente,
                                                                      p_ruolo,
                                                                      p_data);
    END IF;

 IF (RETVAL IS NULL) THEN
    RETVAL := Si4_Competenza.VERIFICA('1', P_TIPO_OGGETTO, P_OGGETTO,P_TIPO_ABILITAZIONE, 'GUEST','GDM', P_DATA);
 END IF;
 IF (RETVAL IS NULL AND p_tipo_oggetto = 'DOCUMENTI' AND (NVL(F_VALORE_PARAMETRI('ABIL_COMP_PADRE@DMSERVER@'),'N')='S' OR P_CONTROLLA_IDPADRE='Y')) THEN
    SELECT NVL(ID_DOCUMENTO_PADRE,-1)
    INTO PADRE
    FROM DOCUMENTI
    WHERE ID_DOCUMENTO=p_oggetto;
    IF PADRE<>-1 THEN
       retval:=gdm_competenza.gdm_verifica (p_tipo_oggetto,
                                            PADRE,
                                            p_tipo_abilitazione,
                                            p_utente,
                                            p_ruolo,
                                            p_data,
                                            'N');
    END IF;
 END IF;
 IF RETVAL IS NULL THEN
    RETURN 0;
 END IF;
 RETURN retval;
 END gdm_verifica;
 FUNCTION gdm_tipodoc_acl (
 p_id_tipodoc IN VARCHAR2,
 p_tipi_abilitazione IN VARCHAR2,
 p_utente IN VARCHAR2,
 p_autore IN VARCHAR2,
 p_accesso IN VARCHAR2 DEFAULT 'S',
 p_ruolo IN VARCHAR2 DEFAULT 'GDM',
 p_dal IN VARCHAR2 DEFAULT TO_CHAR (SYSDATE,
 'DD/MM/YYYY'
 ),
 p_al IN VARCHAR2 DEFAULT NULL,
 p_id_funzione IN VARCHAR2 DEFAULT NULL
 )
 RETURN NUMBER
 AS
 retval NUMBER (1);
 CURSOR c_1
 IS
 SELECT id_documento oggetto
 FROM documenti docu
 WHERE id_tipodoc = p_id_tipodoc
 AND gdm_competenza.gdm_verifica ('DOCUMENTI',
 id_documento,
 'M',
 p_autore,
 p_ruolo,
 TO_CHAR (SYSDATE, 'DD/MM/YYYY')
 ) = 1
 AND EXISTS (
 SELECT 1
 FROM activity_log aclo
 WHERE aclo.id_documento = docu.id_documento
 AND tipo_azione = 'C'
 AND TRUNC (aclo.data_aggiornamento)
 BETWEEN NVL (TO_DATE (p_dal, 'DD/MM/YYYY'),
 TRUNC (data_aggiornamento)
 )
 AND NVL (TO_DATE (p_al, 'DD/MM/YYYY'),
 TRUNC (data_aggiornamento)
 ));
 BEGIN
 FOR c1 IN c_1
 LOOP
 BEGIN
 retval :=
 gdm_competenza.assegna_comp ('DOCUMENTI',
 c1.oggetto,
 p_tipi_abilitazione,
 p_utente,
 p_ruolo,
 p_autore,
 p_accesso,
 TO_CHAR (SYSDATE, 'DD/MM/YYYY'),
 NULL,
 p_id_funzione
 );
 IF retval < 0
 THEN
 RETURN retval;
 END IF;
 EXCEPTION
 WHEN OTHERS
 THEN
 raise_application_error ('-20998',
 'DOCUMENTO:'
 || c1.oggetto
 || ' '
 || SQLERRM
 );
 END;
 END LOOP;
 RETURN 0;
 END gdm_tipodoc_acl;
 FUNCTION gdm_ver_funz_tipodoc
/******************************************************************************
 NOME: verifica
 DESCRIZIONE: Verifica l'abilitazione di accesso ad una certa data restituendo il valore 1 o 0 in funzione del contenuto nel campo ACCESSO.
 Controlla la competenza funzionale associata al tipo documento
 Ritorna:
 1 : se esiste il diritto di accesso
 0 : se non esiste diritto di accesso
******************************************************************************/
 (
 p_null_gestito IN NUMBER,
 p_tipo_oggetto IN VARCHAR2,
 p_oggetto IN VARCHAR2,
 p_tipo_abilitazione IN VARCHAR2,
 p_utente IN VARCHAR2,
 p_ruolo IN VARCHAR2 DEFAULT NULL,
 p_data IN VARCHAR2 DEFAULT TO_CHAR (SYSDATE,
 'dd/mm/yyyy'
 )
 )
 RETURN NUMBER
 IS
 d_diritto NUMBER (1);
 d_funzione NUMBER (10);
 d_tipodoc NUMBER (10);
 d_nome VARCHAR2 (30);
 d_testo VARCHAR2 (4000);
 d_new_testo VARCHAR2 (4000);
 d_cursor INTEGER;
 d_rows_processed INTEGER;
 d_tipo_abil si4_tipi_abilitazione.tipo_abilitazione%TYPE;
 d_tipo_uso modelli.tipo_uso%TYPE;
 d_oggetto si4_competenze.oggetto%TYPE;
 BEGIN
 d_tipo_abil := p_tipo_abilitazione;
 d_oggetto := p_oggetto;
 BEGIN
 IF (p_tipo_oggetto = 'DOCUMENTI')
 THEN
 SELECT docu.id_tipodoc, tipo_uso
 INTO d_tipodoc, d_tipo_uso
 FROM documenti docu, modelli m
 WHERE docu.id_documento = d_oggetto
 AND docu.id_tipodoc = m.id_tipodoc
 AND m.codice_modello_padre IS NULL;
 IF d_tipo_uso IN ('C', 'F', 'W')
 THEN
 SELECT id_viewcartella
 INTO d_oggetto
 FROM cartelle c, view_cartella vw
 WHERE c.id_documento_profilo = d_oggetto
 AND vw.id_cartella = c.id_cartella;
 END IF;
 IF d_tipo_uso IN ('Q', 'V')
 THEN
 SELECT id_query
 INTO d_oggetto
 FROM QUERY q
 WHERE q.id_documento_profilo = d_oggetto;
 END IF;
 END IF;
 IF (p_tipo_oggetto = 'QUERY')
 THEN
 SELECT id_tipodoc
 INTO d_tipodoc
 FROM QUERY q, documenti docu
 WHERE q.id_query = d_oggetto
 AND docu.id_documento = q.id_documento_profilo;
 END IF;
 IF (p_tipo_oggetto = 'VIEW_CARTELLA')
 THEN
 SELECT id_tipodoc
 INTO d_tipodoc
 FROM view_cartella vica, cartelle cart, documenti docu
 WHERE vica.id_viewcartella = d_oggetto
 AND vica.id_cartella = cart.id_cartella
 AND docu.id_documento = cart.id_documento_profilo;
 IF d_tipo_abil='C' THEN
 d_tipo_abil:='X';
 END IF;
 END IF;
-- IF (p_tipo_oggetto = 'VIEW_CARTELLA' AND d_tipo_abil = 'C')
-- THEN
-- d_tipodoc := d_oggetto;
---- SELECT id_tipodoc
---- INTO d_tipodoc
---- FROM VIEW_CARTELLA vica
---- , CARTELLE cart
---- , DOCUMENTI docu
---- WHERE vica.ID_VIEWCARTELLA = d_oggetto
---- AND vica.id_cartella = cart.ID_CARTELLA
---- AND docu.id_documento = cart.id_documento_profilo;
---- IF d_tipo_abil = 'C'
---- THEN
---- d_tipo_abil := 'X';
---- END IF;
-- END IF;
 END;
 BEGIN
 /* Competenza Funzionale sull'Oggetto*/
  --DBMS_OUTPUT.PUT_LINE ('VER FUNZ TIPO' || d_tipodoc || ' utente=' || p_Utente || '-->'||d_tipo_abil);
 SELECT funz.id_funzione, funz.nome, funz.testo
 INTO d_funzione, d_nome, d_testo
 FROM si4_competenze comp,
 si4_abilitazioni abil,
 si4_tipi_oggetto tiog,
 si4_tipi_abilitazione tiab,
 si4_funzioni funz
 WHERE abil.id_tipo_oggetto + 0 = tiog.id_tipo_oggetto
 AND abil.id_tipo_abilitazione + 0 = tiab.id_tipo_abilitazione
 AND comp.id_abilitazione = abil.id_abilitazione
 AND comp.id_funzione = funz.id_funzione
 AND comp.utente IS NULL
 AND TO_DATE (p_data, 'dd/mm/yyyy') BETWEEN NVL
 (dal,
 TO_DATE ('2222222',
 'j'
 )
 )
 AND NVL
 (al,
 TO_DATE ('3333333',
 'j'
 )
 )
 AND tiog.tipo_oggetto = 'TIPI_DOCUMENTO'
 AND tiab.tipo_abilitazione = d_tipo_abil
 AND comp.oggetto = d_tipodoc
 AND NVL (comp.ruolo, 'x') = NVL (p_ruolo, 'x')
 AND tipo_competenza = 'F';
  --DBMS_OUTPUT.PUT_LINE (d_funzione || ', ' || d_nome);
 EXCEPTION
 WHEN NO_DATA_FOUND
 THEN
 -- DBMS_OUTPUT.PUT_LINE ('COMP. FUNZ. x TIPODOC non trovata');
 RAISE;
 END;
 /* Eseguo il Binding del testo trovato*/
 d_testo := si4comp_binding (d_testo, d_oggetto, p_tipo_oggetto);
 /* Eseguo il testo della funzione sostituendo :UTENTE con l'utente e gli eventuali gruppi a cui appartiene*/
 FOR c_utente IN (SELECT p_utente utente
 FROM DUAL
 -- UNION ALL
 -- SELECT gruppo
 -- FROM ad4_utenti_gruppo utgr
 -- CONNECT BY PRIOR gruppo = utente
 -- START WITH utente = p_utente
 )
 LOOP
 d_new_testo :=
 REPLACE (REPLACE (d_testo, ':UTENTE', c_utente.utente),
 CHR (13),
 ' '
 );
 /* Controllo se si tratta di un blocco Begin End, di una funzione o di una Procedura */
 -- controllo ed eventuale modifica statement
 IF UPPER (SUBSTR (LTRIM (d_new_testo), 1, 2)) = ':='
 THEN
 /* AGGIUNGO LA GESTIONE DEL RITORNO */
 d_new_testo :=
 'DECLARE d_ritorno number(1);'
 || 'BEGIN d_ritorno'
 || RTRIM (d_new_testo, ' ;')
 || '; if d_ritorno = 1 then gdm_competenza.set_true; '
 || ' elsif d_ritorno = 0 then gdm_competenza.set_false; '
 || 'end if; END;';
 ELSIF UPPER (SUBSTR (LTRIM (d_new_testo), 1, 5)) != 'BEGIN'
 THEN
 /* in caso di declare mette un altro begin end esterno */
 d_new_testo :=
 'DECLARE d_ritorno number(1);'
 || 'BEGIN '
 || RTRIM (d_new_testo, ' ;')
 || '; if d_ritorno = 1 then gdm_competenza.set_true; '
 || ' elsif d_ritorno = 0 then gdm_competenza.set_false; '
 || 'end if; END;';
 END IF;
 BEGIN
--DBMS_OUTPUT.PUT_LINE (SUBSTR (d_new_testo
--, 1
--, 255));
 resetta;
-- d_cursor := DBMS_SQL.OPEN_CURSOR;
-- DBMS_SQL.PARSE (d_cursor
-- , d_new_testo
-- , DBMS_SQL.native);
-- d_rows_processed := DBMS_SQL.EXECUTE (d_cursor);
-- DBMS_SQL.CLOSE_CURSOR (d_cursor);
 EXECUTE IMMEDIATE d_new_testo;
 EXCEPTION
 WHEN OTHERS
 THEN
 --DBMS_SQL.CLOSE_CURSOR (d_cursor);
 RAISE;
 END;
 --DBMS_OUTPUT.PUT_LINE ('g_diritto ' || g_diritto);
 d_diritto := g_diritto;
 IF d_diritto IS NOT NULL
 THEN
 EXIT;
 END IF;
 END LOOP;
--IF d_diritto IS NULL
-- THEN
-- raise_application_error (-20999,
-- 'La funzione restituisce il valore null'
-- );
-- END IF;
 --DBMS_OUTPUT.PUT_LINE ('d_diritto ' || d_diritto);
 RETURN d_diritto;
 EXCEPTION
 WHEN NO_DATA_FOUND
 THEN
 IF p_null_gestito = 0
 THEN
 d_diritto := 0;
 END IF;
 RETURN d_diritto;
 END;
 FUNCTION gdm_verifica_delega (
 p_tipo_oggetto         IN VARCHAR2,
 p_oggetto              IN VARCHAR2,
 p_tipo_abilitazione    IN VARCHAR2,
 p_utente               IN VARCHAR2,
 p_ruolo                IN VARCHAR2 DEFAULT NULL,
 p_data                 IN VARCHAR2 DEFAULT TO_CHAR (SYSDATE,'dd/mm/yyyy')
 )
 RETURN NUMBER
 AS
 /******************************************************************************
 NOME: gdm_verifica_delega
 DESCRIZIONE: Verifica l'abilitazione di delega ad una certa data restituendo il valore 1 o 0
 in funzione del contenuto nel campo ACCESSO.
 Ritorna:
 1 : se esiste il diritto di accesso
 0 : se non esiste diritto di accesso
******************************************************************************/
 retval NUMBER (1) := null;
  BEGIN
     retval := si4_competenza.verifica (p_tipo_oggetto,
                                        p_oggetto,
                                        p_tipo_abilitazione,
                                        p_utente,
                                        p_ruolo,
                                        p_data
                                       );
 IF RETVAL IS NULL THEN
    RETURN 0;
 END IF;
 RETURN retval;
 END gdm_verifica_delega;
 FUNCTION gdm_get_abilitazioni (
 p_tipo_oggetto         IN VARCHAR2,
 p_oggetto              IN VARCHAR2,
 p_tipo_abilitazione    IN VARCHAR2,
 p_utente               IN VARCHAR2,
 p_data                 IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')
 )
 RETURN DelCurTyp
 AS
 /******************************************************************************
 NOME:        gdm_get_abilitazioni
 DESCRIZIONE: La funzione restituisce un REF_CURSOR contenente l'elenco delle
              abilitazioni che rispondono alle caratteristiche richieste.
 RITORNA:  REF_CURSOR: DelCurTyp
******************************************************************************/
 DelCur DelCurTyp;
 BEGIN
  DelCur := SI4_COMPETENZA.GET_ABILITAZIONI(p_tipo_oggetto,
                                                p_oggetto,
                                                p_tipo_abilitazione,
                                                p_utente,
                                                p_data
                                               );
 RETURN DelCur;
 END gdm_get_abilitazioni;
 PROCEDURE GDM_CHIUDI (
   p_utente            IN VARCHAR2,
   p_tipo_oggetto      IN VARCHAR2 DEFAULT NULL,
   p_data              IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'),
   p_oggetto           IN VARCHAR2 DEFAULT NULL,
   p_tipo_abilitazione IN VARCHAR2 DEFAULT NULL,
   p_data_al           IN VARCHAR2 DEFAULT NULL)
   AS
   /******************************************************************************
    NOME:        gdm_chiudi
    DESCRIZIONE: Chiude le registrazioni di competenza previste per p_utente alla
                 data prevista in parametro.
                 Se il p_tipo_oggetto non ¿ indicato opera su tutti gli oggetti.
                 Se la p_data non ¿ indicata le chiude alla data di oggi.
    NOTE:        .
   ******************************************************************************/
    BEGIN
      SI4_COMPETENZA.CHIUDI(p_utente,p_tipo_oggetto,p_data,p_oggetto,p_tipo_abilitazione,p_data_al);
 END GDM_CHIUDI;
 PROCEDURE GDM_ASSEGNA_DELEGA (
   P_TIPO_OGGETTO IN VARCHAR2,
   P_OGGETTO IN VARCHAR2,
   P_TIPO_ABILITAZIONE IN VARCHAR2,
   P_UTENTE IN VARCHAR2,
   P_RUOLO IN VARCHAR2 DEFAULT NULL,
   P_AUTORE IN VARCHAR2,
   P_ACCESSO IN VARCHAR2 DEFAULT 'S',
   P_DAL IN VARCHAR2 DEFAULT NULL,
   P_AL IN VARCHAR2 DEFAULT NULL)
   IS
   /******************************************************************************
    NOME:        gdm_assegna_delega
    DESCRIZIONE: Registra la competenza per il soggetto (utente o gruppo) indicato, relativamente alla abilitazione di un determinato oggetto.
                 - chiude periodo precedente alla data DAL - 1
                 - sposta periodo successivo alla data AL+ 1
                 - elimina periodi contenuti
                 Return:
                  0 : Inserimento corretto
                 -1 : Tipo di abilitazione incompatibile con l'oggetto indicato
                 -2 : Non esiste l'oggetto
                 -3 : Non esiste il tipo di abilitazione
   ******************************************************************************/
    retval NUMBER;
   BEGIN
   IF(P_TIPO_OGGETTO IN ('QUERY','VIEW_CARTELLA') ) THEN
    BEGIN
      retval :=  SI4_COMPETENZA.ASSEGNA('DOCUMENTI',P_OGGETTO,P_TIPO_ABILITAZIONE,P_UTENTE,P_RUOLO,P_AUTORE,P_ACCESSO,P_DAL,P_AL);
    END;
   END IF;
   retval :=  SI4_COMPETENZA.ASSEGNA(P_TIPO_OGGETTO,P_OGGETTO,P_TIPO_ABILITAZIONE,P_UTENTE,P_RUOLO,P_AUTORE,P_ACCESSO,P_DAL,P_AL);
 END;
 FUNCTION  gdm_get_abilitazioni_nome (p_utente IN VARCHAR2, p_data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'))
    RETURN VARCHAR2
   IS
     d_stringa   VARCHAR2 (4000);
     cursore     afc.t_ref_cursor;
     a           VARCHAR2 (400);
     b           VARCHAR2 (400);
     c           VARCHAR2 (400);
     d           VARCHAR2 (400);
     e           VARCHAR2 (400);
     f           VARCHAR2 (400);
     g           VARCHAR2 (400);
     h           VARCHAR2 (400);
     i           VARCHAR2 (400);
     l           VARCHAR2 (400);
     m           VARCHAR2 (400);
     n           VARCHAR2 (400);
   BEGIN
     cursore := SI4_COMPETENZA.GET_ABILITAZIONI('DOCUMENTI',p_utente,'dt',null,p_data);
     LOOP
       FETCH cursore INTO   a,b,c,d,e,f,g,h,i,l,m,n;
       EXIT WHEN cursore%NOTFOUND;
       d_stringa := d_stringa || '<C>' || f || '</C><V>' || g || '</V>';
     END LOOP;
     RETURN d_stringa;
     CLOSE cursore;
 END gdm_get_abilitazioni_nome;

 FUNCTION  gdm_get_abilitazioni_elenco (p_utente IN VARCHAR2, p_data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'))
     RETURN VARCHAR2
   IS
      d_stringa   VARCHAR2 (4000);
      cursore     afc.t_ref_cursor;
      a           VARCHAR2 (400);
      b           VARCHAR2 (400);
      c           VARCHAR2 (400);
      d           VARCHAR2 (400);
      e           VARCHAR2 (400);
      f           VARCHAR2 (400);
      g           VARCHAR2 (400);
      h           VARCHAR2 (400);
      i           VARCHAR2 (400);
      l           VARCHAR2 (400);
      m           VARCHAR2 (400);
      n           VARCHAR2 (400);
   BEGIN
      cursore := SI4_COMPETENZA.GET_ABILITAZIONI('DOCUMENTI',p_utente,'dt',null,p_data);
      LOOP
         FETCH cursore INTO   a,b,c,d,e,f,g,h,i,l,m,n;
         EXIT WHEN cursore%NOTFOUND;
         d_stringa := d_stringa || g || '<br>';
      END LOOP;
      d_stringa:='<C>DELEGATO</C><V>' || d_stringa || '</V>';
      RETURN d_stringa;
      CLOSE cursore;
 END gdm_get_abilitazioni_elenco;

FUNCTION  gdm_get_deleganti_elenco (p_utente IN VARCHAR2, p_data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'))
    RETURN VARCHAR2
IS
     attivazione_deleghe VARCHAR2(1) := 'N';
     d_stringa           VARCHAR2 (4000) := '' ;
     cursore             afc.t_ref_cursor;
     a           VARCHAR2 (400);
     b           VARCHAR2 (400);
     c           VARCHAR2 (400);
     d           VARCHAR2 (400);
     e           VARCHAR2 (400);
     f           VARCHAR2 (400);
     g           VARCHAR2 (400);
     h           VARCHAR2 (400);
     i           VARCHAR2 (400);
     l           VARCHAR2 (400);
     m           VARCHAR2 (400);
     n           VARCHAR2 (400);
BEGIN

     attivazione_deleghe:= F_VALORE_PARAMETRI ('ATTIVAZIONE_DELEGHE@WORKFLOW@');

     IF (attivazione_deleghe = 'S') THEN
      cursore := SI4_COMPETENZA.GET_ABILITAZIONI('DOCUMENTI',null,'dt',p_utente,p_data);

      LOOP
         FETCH cursore INTO   a,b,c,d,e,f,g,h,i,l,m,n;
         EXIT WHEN cursore%NOTFOUND;
         d_stringa := d_stringa ||'<C>DELEGANTE</C><V>'|| c || '</V>';
      END LOOP;

     END IF;


     RETURN d_stringa;
     CLOSE cursore;
END gdm_get_deleganti_elenco;


FUNCTION addSeparatorToAbil(p_abilitazioni VARCHAR2)
 RETURN VARCHAR2
IS
 len  NUMBER (2) := 0;
 sRet VARCHAR2 (200) := '';
BEGIN

    len := LENGTH (p_abilitazioni);

    FOR i IN 1 .. len LOOP
    BEGIN

        sRet := SUBSTR (p_abilitazioni, i, 1) || ';' || sRet;

    END;
    END LOOP;

    RETURN sRet;
END addSeparatorToAbil;

FUNCTION getArrayListAbil(p_abilitazioni VARCHAR2)
 RETURN ARRSTRING
IS
 sabil          VARCHAR2 (200);
 sSingleAbil    VARCHAR2 (10);
 abilArrayList  ARRSTRING;
 conta          NUMBER(3) := 0;
BEGIN

    IF INSTR(p_abilitazioni,';')=0 THEN
       sabil:= GDM_COMPETENZA.ADDSEPARATORTOABIL(p_abilitazioni);
    ELSE
       sabil:= p_abilitazioni;
    END IF;

    abilArrayList := ARRSTRING();

    WHILE (LENGTH(sabil) > 0) LOOP

          IF INSTR(sabil,';')=0 THEN
             sSingleAbil := sabil;
             sabil       := '';
          ELSE
             sSingleAbil := SUBSTR(sabil,1,INSTR(sabil,';')-1);
          END IF;

          conta := conta +1;

          abilArrayList.extend;
          abilArrayList(conta) := sSingleAbil;

          sabil := SUBSTR(sabil,INSTR(sabil,';')+1);
    END LOOP;

    RETURN abilArrayList;
END getArrayListAbil;

FUNCTION getStringException(P_CODE NUMBER, P_ABIL VARCHAR2, P_OGGETTO VARCHAR2)
 RETURN VARCHAR2
IS
BEGIN
     IF P_CODE = -1 THEN
        RETURN 'Tipo di abilitazione '||P_ABIL||' incompatibile con l''oggetto indicato';
     ELSIF P_CODE = -2 THEN
        RETURN 'Non esiste l''oggetto '||P_OGGETTO;
     ELSIF P_CODE = -3 THEN
        RETURN 'Non esiste il tipo di abilitazione '||P_ABIL;
     END IF;

     RETURN P_CODE||'';
END getStringException;

FUNCTION getElencoUtentiAccesso(P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_LISTAABILITAZIONI IN VARCHAR2, P_SOLOUTENTI IN VARCHAR2)
  RETURN CLOB
IS
TYPE SelCur is ref cursor;
utentiCur SelCur;
selString varchar2(32000);
abilString varchar2(100) := '';
abilStringFunz varchar2(500) := '';
retClob CLOB := EMPTY_CLOB();
arrayListAbil ARRSTRING;
utente si4_competenze.utente%TYPE;
nominativo ad4_utenti.nominativo%TYPE;
tipoUtente ad4_utenti.tipo_utente%TYPE;
tipoAbil SI4_TIPI_ABILITAZIONE.TIPO_ABILITAZIONE%TYPE;
abil NUMBER(1);
BEGIN

     arrayListAbil:=getArrayListAbil(P_LISTAABILITAZIONI);
      for i in arrayListAbil.first..arrayListAbil.last loop
            if i>1 then
               abilString := abilString ||  ',';
               abilStringFunz := abilStringFunz ||  ',';
            end if;

            abilStringFunz := abilStringFunz ||  'decode(oggetti.tipo,''TIDO'',decode('''||arrayListAbil(i)||''',''X'',''C'','''||arrayListAbil(i)||'''),'''||arrayListAbil(i)||''')' ;
            abilString := abilString ||  ''''||arrayListAbil(i)||'''';
      end loop;

     selString :='select utente ute,nominativo nomin,tipo_utente tipoUte,TIPO_ABILITAZIONE tipoAbil, '||chr(10)||
                         'GDM_COMPETENZA.GDM_VERIFICA ( '''||P_TIPO_OGGETTO||''', '''||P_OGGETTO||''', TIPO_ABILITAZIONE, utente,''GDM'' )  comp '||chr(10)||
                         'from '||chr(10)||
                         ' (select ad4_utenti.utente, ad4_utenti.tipo_utente,TIPO_ABILITAZIONE,nominativo '||chr(10)||
                         'from si4_competenze,'||chr(10)||
                         'si4_abilitazioni,'||chr(10)||
                         'si4_tipi_oggetto,'||chr(10)||
                         'si4_tipi_abilitazione,'||chr(10)||
                         'ad4_utenti '||chr(10)||
                         'where oggetto='''||P_OGGETTO||''' ' ||chr(10)||
                         'and si4_abilitazioni.id_abilitazione=si4_competenze.id_abilitazione '||chr(10)||
                         'and si4_abilitazioni.ID_TIPO_OGGETTO=SI4_TIPI_OGGETTO.ID_TIPO_OGGETTO '||chr(10)||
                         'and si4_abilitazioni.ID_TIPO_ABILITAZIONE  =si4_tipi_abilitazione.ID_TIPO_ABILITAZIONE '||chr(10)||
                         'and SI4_TIPI_OGGETTO.TIPO_OGGETTO='''||P_TIPO_OGGETTO||''' '||chr(10)||
                         'and SI4_TIPI_ABILITAZIONE.TIPO_ABILITAZIONE in ('||abilString||') '||chr(10)||
                         'and ad4_utenti.utente=si4_competenze.utente and tipo_competenza = ''U'' '||chr(10)||
                         ' union '||chr(10)||
                         'select ad4_utenti.utente, ad4_utenti.tipo_utente,tipoabil TIPO_ABILITAZIONE,nominativo '||chr(10)||
                         'from ('||chr(10)||
                         '   select TIPO_ABILITAZIONE tipoabil'||chr(10)||
                         '   from si4_competenze,'||chr(10)||
                         '   si4_abilitazioni,'||chr(10)||
                         '   si4_tipi_oggetto,'||chr(10)||
                         '   si4_tipi_abilitazione,'||chr(10)||
                         '   (    SELECT TO_CHAR(docu.id_tipodoc) obj, ''TIDO'' tipo'||chr(10)||
                         '         FROM documenti docu'||chr(10)||
                         '        WHERE docu.id_documento = '''||P_OGGETTO||''' ' ||chr(10)||
                         '         AND ''DOCUMENTI'' = '''||P_TIPO_OGGETTO||''' ' ||chr(10)||
                         '       UNION'||chr(10)||
                         '       SELECT TO_CHAR(id_tipodoc) obj, ''TIPO'' tipo '||chr(10)||
                         '         FROM QUERY q, documenti docu'||chr(10)||
                         '        WHERE q.id_query = '''||P_OGGETTO||''' ' ||chr(10)||
                         '          AND docu.id_documento = q.id_documento_profilo'||chr(10)||
                         '          AND ''QUERY'' = '''||P_TIPO_OGGETTO||''' ' ||chr(10)||
                         '       UNION'||chr(10)||
                         '       SELECT TO_CHAR(id_tipodoc) obj, ''TIDO'' tipo'||chr(10)||
                         '         FROM view_cartella vica, cartelle cart, documenti docu'||chr(10)||
                         '        WHERE vica.id_viewcartella = '''||P_OGGETTO||''' ' ||chr(10)||
                         '          AND vica.id_cartella = cart.id_cartella'||chr(10)||
                         '          AND docu.id_documento = cart.id_documento_profilo'||chr(10)||
                         '          AND ''VIEW_CARTELLA'' = '''||P_TIPO_OGGETTO||''' ' ||chr(10)||
                         '       UNION'||chr(10)||
                         '       SELECT '''||P_OGGETTO||''' obj,''OBJ'' tipo'||chr(10)||
                         '       FROM DUAL) oggetti'||chr(10)||
                         '   where oggetto=oggetti.obj'||chr(10)||
                         '   and si4_abilitazioni.id_abilitazione=si4_competenze.id_abilitazione'||chr(10)||
                         '   and si4_abilitazioni.ID_TIPO_OGGETTO=SI4_TIPI_OGGETTO.ID_TIPO_OGGETTO '||chr(10)||
                         '   and si4_abilitazioni.ID_TIPO_ABILITAZIONE  =si4_tipi_abilitazione.ID_TIPO_ABILITAZIONE '||chr(10)||
                         '   and SI4_TIPI_OGGETTO.TIPO_OGGETTO=decode(oggetti.tipo,''TIDO'',''TIPI_DOCUMENTO'','''||P_TIPO_OGGETTO||''')'||chr(10)||
                         '   and SI4_TIPI_ABILITAZIONE.TIPO_ABILITAZIONE in ('||abilStringFunz||') '||chr(10)||
                         '   and tipo_competenza = ''F'''||chr(10)||
                         ' ), ad4_utenti where ad4_utenti.utente like ''%'' ) order by 1,2,3';
    --dbms_output.put_line(selString);
    --dbms_output.put_line('APERTO CUR');
    IF P_SOLOUTENTI='N' THEN
        retClob:=GDM_CLOB.ADD_CHAR(retClob,selString);
        RETURN retClob;
    END IF;

    OPEN UtentiCur for selString;
    selString:=' ';

     LOOP
       FETCH UtentiCur INTO   utente,nominativo,tipoUtente,tipoAbil,abil;
       EXIT WHEN UtentiCur%NOTFOUND;

       if selString<>' ' then
          if selString='X' then selString:=''; end if;

          selString:=selString||' UNION ALL ';
       else
          selString:='select ute, max(nomina) nomin,tipoute,tipoabil,DECODE (sum(prov), 0, MAX (to_number(abil)),   MAX (to_number(compDontCare)) ) comp from (';
       end if;

       IF tipoUtente in ('G','O') THEN
           selString:=selString||'SELECT ad4_utenti.utente ute,ad4_utenti.nominativo nomina,ad4_utenti.tipo_utente tipoute,'''||tipoAbil||''' tipoabil,'''||abil||''' abil, 0 prov, ''0'' compDontCare from ad4_utenti_gruppo, ad4_utenti '||chr(10)||
                               'where ad4_utenti.utente=ad4_utenti_gruppo.utente '||chr(10)||
                               'connect by prior  ad4_utenti_gruppo.UTENTE = ad4_utenti_gruppo.GRUPPO '||chr(10)||
                               'start with gruppo In ('''||utente||''')';
       ELSE
           selString:=selString||'SELECT '''||utente||''' ute,'''||nominativo||''' nomina,''U'' tipoute,'''||tipoAbil||''' tipoabil,'''||abil||''' abil, 1 prov, '''||abil||''' compDontCare  FROM DUAL';
       END IF;

       if Length(selString)>30000 then
          retClob:=GDM_CLOB.ADD_CHAR(retClob,selString);
          selString := 'X';
       end if;
     END LOOP;

     CLOSE UtentiCur;
     IF  selString=' ' THEN
         RETURN '';
     END IF;

     if selString<>'X' then retClob:=GDM_CLOB.ADD_CHAR(retClob,selString); end if;
     selString:=') where tipoute=''U'' group by ute,tipoute,tipoabil';
     retClob:=GDM_CLOB.ADD_CHAR(retClob,selString);
     --dbms_output.put_line(selString);
     --OPEN UtentiCur for selString;
     --RETURN UtentiCur;
     return retClob;

END getElencoUtentiAccesso;

PROCEDURE GDM_DISABILITA_COMP_ALLEGATI(P_ID_TIPODOC NUMBER,P_AUTORE VARCHAR2, P_COMPETENZA_ATTUALE_ALLEGATO VARCHAR2)
AS
BEGIN
    DECLARE

        retval                NUMBER (2) := 0;
        selString             VARCHAR2(32000);
        TYPE SelCur           is ref cursor;
        utentiCur             SelCur;
        utente                si4_competenze.utente%TYPE;
        nominativo            ad4_utenti.nominativo%TYPE;
        tipoUtente            ad4_utenti.tipo_utente%TYPE;
        tipoAbil              SI4_TIPI_ABILITAZIONE.TIPO_ABILITAZIONE%TYPE;
        abil                  NUMBER(1);
        competenza_allegato   varchar2(1);

    BEGIN

        select GDM_COMPETENZA.GETELENCOUTENTIACCESSO ( 'TIPI_DOCUMENTO', ''||P_ID_TIPODOC||'', 'LA;UA;DA;','N' ) INTO selString from dual;

        OPEN UtentiCur for selString;

        LOOP
         FETCH UtentiCur INTO   utente,nominativo,tipoUtente,tipoAbil,abil;
         EXIT WHEN UtentiCur%NOTFOUND;
              retval := si4_competenza.assegna ('TIPI_DOCUMENTO',
                                                TO_CHAR (P_ID_TIPODOC),
                                                tipoAbil,
                                                utente,
                                                f_trasla_ruolo(utente,'GDM','GDM'),
                                                P_AUTORE,
                                                'N',
                                                TO_CHAR (SYSDATE, 'DD/MM/YYYY'),
                                                NULL);

         IF RETVAL<0 THEN

            BEGIN

             IF (P_COMPETENZA_ATTUALE_ALLEGATO = 'S') THEN
               competenza_allegato:= 'N';
             ELSE
               competenza_allegato:= 'S';
             END IF;

             BEGIN
              UPDATE TIPI_DOCUMENTO SET
                 UTENTE_AGGIORNAMENTO = P_AUTORE,
                 DATA_AGGIORNAMENTO = SYSDATE,
                 COMPETENZE_ALLEGATI = competenza_allegato
              WHERE  ID_TIPODOC = P_ID_TIPODOC;
              COMMIT;
              EXCEPTION WHEN OTHERS THEN
               ROLLBACK;
               raise_application_error('-20998','IMPOSSIBILE AGGIORNARE IL TIPO DOCUMENTO (ID_TIPODOC, COMPETENZA_ALLEGATO):('||P_ID_TIPODOC||','||competenza_allegato||') '||SQLERRM);
             END;

            END;

            raise_application_error('-20999',getStringException(retval,tipoAbil,TO_CHAR (P_ID_TIPODOC)));
         END IF;

        END LOOP;

        CLOSE UtentiCur;

    END;
END GDM_DISABILITA_COMP_ALLEGATI;


FUNCTION  GDM_VERIFICA_GESTIONE_DELEGA (
 P_TIPO_OGGETTO IN VARCHAR2,
 P_TIPO_ABILITAZIONE IN VARCHAR2,
 P_UTENTE IN VARCHAR2,
 P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')
 )
 RETURN VARCHAR2
 AS
 /******************************************************************************
 NOME: GDM_VERIFICA_GESTIONE_DELEGA
 DESCRIZIONE: Verifica l'abilitazione di gestione delega 'gd' sugli oggetti DOCUEMNTI,QUERY,VIEW_CARTELLA
 ad una certa data restituendo il valore 1 o 0 in funzione del contenuto nel campo ACCESSO.
 Ritorna:
 1 : se esiste il diritto di accesso
 0 : se non esiste diritto di accesso
******************************************************************************/
 retval VARCHAR2(100);
 gdD    NUMBER (1);
 gdQ    NUMBER (1);
 gdC    NUMBER (1);
 gd_tp  NUMBER(1);
  BEGIN

     SELECT gdm_competenza.gdm_verifica_delega
                                     (P_TIPO_OGGETTO,
                                      'DOCUMENTI',
                                      P_TIPO_ABILITAZIONE,
                                      P_UTENTE,
                                      f_trasla_ruolo (P_UTENTE,
                                                      'GDMWEB',
                                                      'GDMWEB'
                                                     ),
                                      P_DATA
                                     ) gd_documenti,
       gdm_competenza.gdm_verifica_delega
                                         (P_TIPO_OGGETTO,
                                          'QUERY',
                                          P_TIPO_ABILITAZIONE,
                                          P_UTENTE,
                                          f_trasla_ruolo (P_UTENTE,
                                                          'GDMWEB',
                                                          'GDMWEB'
                                                         ),
                                          P_DATA
                                         ) gd_query,
       gdm_competenza.gdm_verifica_delega
                                      (P_TIPO_OGGETTO,
                                       'VIEW_CARTELLA',
                                       P_TIPO_ABILITAZIONE,
                                       P_UTENTE,
                                       f_trasla_ruolo (P_UTENTE,
                                                       'GDMWEB',
                                                       'GDMWEB'
                                                      ),
                                       P_DATA
                                      ) gd_cartelle,
       gdm_competenza.gdm_verifica_delega
                                      (P_TIPO_OGGETTO,
                                       'TIPI_DOCUMENTO',
                                       P_TIPO_ABILITAZIONE,
                                       P_UTENTE,
                                       f_trasla_ruolo (P_UTENTE,
                                                       'GDMWEB',
                                                       'GDMWEB'
                                                      ),
                                       P_DATA
                                      ) gd_tipi_documento
      INTO gdD,gdQ,gdC,gd_tp
      FROM DUAL;

     retval := 'D'||gdD||'@'||'Q'||gdQ||'@'||'C'||gdC||'@'||'T'||gd_tp;

 IF RETVAL IS NULL THEN
    RETURN 0;
 END IF;
 RETURN retval;
 END GDM_VERIFICA_GESTIONE_DELEGA;

END GDM_COMPETENZA;
/


CREATE OR REPLACE PACKAGE BODY ANALISI_SPAZIOFILE AS
   FUNCTION GETNOMETIPOMODELLO(P_TIPO VARCHAR2)  RETURN VARCHAR2
   IS
        A_TIPO   VARCHAR2(100);
   BEGIN
         A_TIPO:='PROTOCOLLI';

        IF P_TIPO='INTEROP' THEN
            A_TIPO := 'PROTOCOLLI_INTERO';
        END IF;
        IF P_TIPO='LETTERA' THEN
            A_TIPO := 'LETTERE_USCITA';
        END IF;
        IF P_TIPO='DETERMINA' THEN
            A_TIPO := 'DETERMINE';
        END IF;
        IF P_TIPO='DELIBERA' THEN
            A_TIPO := 'DELIBERE';
        END IF;
        IF P_TIPO='DETERMINA_20' THEN
            A_TIPO := P_TIPO;
        END IF;
        IF P_TIPO='DELIBERA_20' THEN
            A_TIPO := P_TIPO;
        END IF;
        IF P_TIPO='REVISIONEATTI' THEN
            A_TIPO := 'REVISIONE AREA ATTI';
        END IF;
        IF P_TIPO='REVISIONEPROTO' THEN
            A_TIPO := 'REVISIONE AREA PROTOCOLLO';
        END IF;
        IF P_TIPO='ALBO' THEN
            A_TIPO := 'ALBO PRETORIO';
        END IF;
         IF P_TIPO='FASC_PERS_FI' THEN
            A_TIPO := 'FASCICOLO PERS. FIRENZE';
        END IF;
        IF P_TIPO='MEMO_PROTOCOLLO' THEN
            A_TIPO := 'MEMO_PROTOCOLLO';
        END IF;
        IF P_TIPO='STREAM_MEMO_PROTOCOLLO' THEN
            A_TIPO := 'STREAM_MEMO_PROTOCOLLO';
        END IF;
        IF P_TIPO='PEC_ENTRATA' THEN
            A_TIPO := P_TIPO;
        END IF;
        IF P_TIPO='PEC_USCITA' THEN
            A_TIPO := P_TIPO;
        END IF;
        IF P_TIPO='RICEVUTE_PEC_USCITA' THEN
            A_TIPO := P_TIPO;
        END IF;
        RETURN A_TIPO;
   END;
   FUNCTION GETQRY_FATTELETTR(A_DATA_DA VARCHAR2,A_DATA_A VARCHAR2) RETURN VARCHAR2
   IS
        A_QRY VARCHAR2(32767);
        A_QRY_INTERNA VARCHAR2(32767);
        A_SELECT VARCHAR2(1000);
        A_WHERE_DATA VARCHAR2(1000) := ' trunc( to_Date(FATTURE_VIEW.data_creazione,''dd/mm/yyyy hh24:mi:ss'') )  between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        A_WHERE_DATA_ACQ VARCHAR2(1000) := ' trunc( to_Date(FATTURE_ACQ_VIEW.data_creazione,''dd/mm/yyyy hh24:mi:ss'') )  between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        A_BETWEEN_DATA VARCHAR2(100) := 'TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
   BEGIN
        A_SELECT:='select tipoModello,tipoOggettoFile, posizione, count(*) as numeroOggettiFile ,  decode ( posizione, ''SU DB'' ,  round(sum(nvl(dbms_lob.getlength(testoocr),0)/1048576),2)  , round(sum(nvl(GETLENGTHBFILE("FILE"),0)/1048576),2)  )  as DimMb';

        A_QRY_INTERNA:= 'select  ''PADRI_FATT'' as tipo, ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_VIEW.ENTE  as tipoModello  , decode(ogfiTestata.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiTestata.testoocr, ogfiTestata."FILE" '||
                                    ' from FATTURE_VIEW,  oggetti_file ogfiTestata '||
                                    '  where  '||A_WHERE_DATA||
                                     '          and  FATTURE_VIEW.id_documento=  ogfiTestata.id_documento     ' ||
                                     ' union all '||
                                     'select  ''FIGLI_FATT'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from FATTURE_VIEW, FAT_DETTAGLIO_VIEW , oggetti_file ogfiDettaglio '||
                                     ' where  FATTURE_VIEW.idrif=FAT_DETTAGLIO_VIEW.idrif and '||
                                     '           '||A_WHERE_DATA||
                                     '          AND  FAT_DETTAGLIO_VIEW.id_documento=  ogfiDettaglio.id_documento '||
                                     ' union all '||
                                     'select  ''FIGLI_FATT_ALLEGATI'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglioAllegato.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglioAllegato.testoocr, ogfiDettaglioAllegato."FILE"'||
                                     ' from FATTURE_VIEW, FAT_DETTAGLIO_VIEW , FAT_ALLEGATI_VIEW, oggetti_file ogfiDettaglioAllegato '||
                                     ' where  FATTURE_VIEW.idrif=FAT_DETTAGLIO_VIEW.idrif and '||
                                     '           FAT_DETTAGLIO_VIEW.idrif = FAT_ALLEGATI_VIEW.idrif and  '||
                                     '          '||A_WHERE_DATA||
                                     '          and FAT_ALLEGATI_VIEW.id_documento=  ogfiDettaglioAllegato.id_documento        '||
                                     ' union  all '||
                                     'select  ''NOTIFICHE_FATT'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from FATTURE_VIEW, NOTIFICHE_VIEW , oggetti_file ogfiDettaglio '||
                                     ' where  FATTURE_VIEW.nomefile=NOTIFICHE_VIEW.nomefile and '||
                                     '           '||A_WHERE_DATA||
                                     '          and NOTIFICHE_VIEW.id_documento=  ogfiDettaglio.id_documento          '||
                                     ' union all '||
                                     ' select  ''NOTIFICHE_VISUALIZZA_FATT'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from FATTURE_VIEW, NOTIFICHE_VIEW, FATT_VISUALIZZA , oggetti_file ogfiDettaglio'||
                                     ' where  FATTURE_VIEW.nomefile=NOTIFICHE_VIEW.nomefile and  '||
                                     '           NOTIFICHE_VIEW.id_documento=FATT_VISUALIZZA.ID_DOC_PADRE and '||
                                     '           '||A_WHERE_DATA||
                                     '          and FATT_VISUALIZZA.ID_OGGETTO_FILE=  ogfiDettaglio.ID_OGGETTO_FILE     '||
                                     ' union all '||
                                     ' select  ''PADRI_FATT_ACQ'' as tipo, ''OGGETTI_FILE'' as tipooggettofile,  ''FATT_ELETTR_ENTE_''||FATTURE_ACQ_VIEW.ENTE  as tipoModello  , decode(ogfiTestata.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiTestata.testoocr, ogfiTestata."FILE"'||
                                     ' from FATTURE_ACQ_VIEW,  oggetti_file ogfiTestata '||
                                     ' where    '||A_WHERE_DATA_ACQ||
                                     '          and FATTURE_ACQ_VIEW.id_documento=  ogfiTestata.id_documento       '||
                                     ' union all '||
                                     ' select  ''FIGLI_FATT_ACQ'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_ACQ_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from FATTURE_ACQ_VIEW, FAT_DETTAGLIO_VIEW , oggetti_file ogfiDettaglio '||
                                     '  where  FATTURE_ACQ_VIEW.idrif=FAT_DETTAGLIO_VIEW.idrif and '||
                                     '              '||A_WHERE_DATA_ACQ||
                                     '              and FAT_DETTAGLIO_VIEW.id_documento=  ogfiDettaglio.id_documento                                      '||
                                     ' union all '||
                                     ' select  ''FIGLI_FATT_ACQ_ALLEGATI'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_ACQ_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglioAllegato.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglioAllegato.testoocr, ogfiDettaglioAllegato."FILE"'||
                                     ' from FATTURE_ACQ_VIEW, FAT_DETTAGLIO_VIEW , FAT_ALLEGATI_VIEW, oggetti_file ogfiDettaglioAllegato '||
                                     ' where  FATTURE_ACQ_VIEW.idrif=FAT_DETTAGLIO_VIEW.idrif and '||
                                     '           FAT_DETTAGLIO_VIEW.idrif = FAT_ALLEGATI_VIEW.idrif and '||
                                     '          '||A_WHERE_DATA_ACQ||
                                     '          and FAT_ALLEGATI_VIEW.id_documento=  ogfiDettaglioAllegato.id_documento ' ||
                                     ' union all '||
                                     ' select  ''NOTIFICHE_FATT_ACQ'' as tipo,  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||FATTURE_ACQ_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from FATTURE_ACQ_VIEW, NOTIFICHE_VIEW , oggetti_file ogfiDettaglio '||
                                     ' where  FATTURE_ACQ_VIEW.nomefile=NOTIFICHE_VIEW.nomefile and '||
                                     '            '||A_WHERE_DATA_ACQ||
                                     '          and NOTIFICHE_VIEW.id_documento=  ogfiDettaglio.id_documento    '||
                                     ' union all '||
                                     ' select  ''NOTIFICHE_VISUALIZZA_FATT_ACQ'' as tipo, ''OGGETTI_FILE'' as tipooggettofile,  ''FATT_ELETTR_ENTE_''||FATTURE_ACQ_VIEW.ENTE  as tipoModello  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from FATTURE_ACQ_VIEW, NOTIFICHE_VIEW, FATT_VISUALIZZA , oggetti_file ogfiDettaglio '||
                                     ' where  FATTURE_ACQ_VIEW.nomefile=NOTIFICHE_VIEW.nomefile and  '||
                                     '           NOTIFICHE_VIEW.id_documento=FATT_VISUALIZZA.ID_DOC_PADRE and '||
                                     '          '||A_WHERE_DATA_ACQ||
                                     '          and FATT_VISUALIZZA.ID_OGGETTO_FILE=  ogfiDettaglio.ID_OGGETTO_FILE   '||
                                     ' union all '||
                                     ' Select ''MAIL'',   ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||fat_mail_view.ENTE  as tipo  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from fat_mail_view , oggetti_file ogfiDettaglio '||
                                     ' where (select trunc(min(stati_documento.data_aggiornamento)) from stati_documento where stati_documento.id_documento=fat_mail_view.id_documento)     between  '||A_BETWEEN_DATA||
                                     '           and fat_mail_view.ID_DOCUMENTO=  ogfiDettaglio.ID_DOCUMENTO      '||
                                     ' union all '||
                                     ' Select ''DISTINTE'',  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||DISTINTE_view.ENTE  as tipo  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from DISTINTE_view , oggetti_file ogfiDettaglio '||
                                     ' where (select trunc(min(stati_documento.data_aggiornamento)) from stati_documento where stati_documento.id_documento=DISTINTE_view.id_documento)     between  '||A_BETWEEN_DATA||
                                     '           and  DISTINTE_view.ID_DOCUMENTO=  ogfiDettaglio.ID_DOCUMENTO       '||
                                     ' union all '||
                                     ' Select ''STORNI'',  ''OGGETTI_FILE'' as tipooggettofile, ''FATT_ELETTR_ENTE_''||STORNI_view.ENTE  as tipo  , decode(ogfiDettaglio.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ,ogfiDettaglio.testoocr, ogfiDettaglio."FILE"'||
                                     ' from STORNI_view , oggetti_file ogfiDettaglio '||
                                     ' where (select trunc(min(stati_documento.data_aggiornamento)) from stati_documento where stati_documento.id_documento=STORNI_view.id_documento)     between  '||A_BETWEEN_DATA||
                                     '           and STORNI_view.ID_DOCUMENTO=  ogfiDettaglio.ID_DOCUMENTO        ';

           A_QRY :=        A_SELECT || ' from ('||A_QRY_INTERNA||') group by      tipoModello,tipoOggettoFile, posizione';
        --dbms_output.put_line(A_QRY);
            RETURN A_QRY;

   END;
   FUNCTION GETQRY_PROTOTYPE(P_TIPO VARCHAR2,A_DATA_DA VARCHAR2,A_DATA_A VARCHAR2) RETURN VARCHAR2
    IS
        A_QRY VARCHAR2(32767);
        A_TABELLA VARCHAR2(100):='spr_protocolli';
        A_TIPO VARCHAR2(100) := 'PROTOCOLLI';
        A_TABELLA_FIGLIA  VARCHAR2(100):='seg_allegati_protocollo';
        A_WHERECOND  VARCHAR2(1000) := 'TRUNC(padri.data) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        A_WHERECONDPADRE_FIGLIO  VARCHAR2(1000) := 'padri.idrif=figli.idrif';
        A_BFILE_LOG  VARCHAR2(1000) := '     decode( ogfilog.path_file ,  null,     "FILE", bfilename( ''''||F_GETDIRECTORY_AREA_NAME(documenti.ID_DOCUMENTO) ||''''  ,  '''' || TIPI_DOCUMENTO.ACRONIMO_MODELLO || ''/'' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || ''/'' || DOCUMENTI.ID_DOCUMENTO || ''/LOG_''||acl.id_log||''/'' || ID_OGGETTO_FILE||   ''''  ) )  ';


    BEGIN
        IF P_TIPO='INTEROP' OR P_TIPO='PEC_ENTRATA' THEN
            A_TABELLA:='spr_protocolli_intero';
            A_TIPO := 'PROTOCOLLI_INTERO';
        END IF;
        IF P_TIPO='LETTERA' THEN
            A_TABELLA:='spr_lettere_uscita';
            A_TIPO := 'LETTERE_USCITA';
        END IF;
        IF P_TIPO='DETERMINA' THEN
            A_TABELLA:='sat_determina';
            A_TIPO := 'DETERMINE';
            A_TABELLA_FIGLIA :='sat_allegato';
            A_WHERECOND   := 'TRUNC(padri.DATA_CREAZIONE_DOC) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        END IF;
        IF P_TIPO='DETERMINA_20' THEN
            A_TABELLA:='gat_determina';
            A_TIPO := 'DETERMINE_20';
            A_TABELLA_FIGLIA :='gat_allegato';
            A_WHERECOND   := 'TRUNC(padri.DATA_INS) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
            A_WHERECONDPADRE_FIGLIO   := 'padri.id_documento_grails=figli.id_determina_grails';
        END IF;
        IF P_TIPO='DELIBERA' THEN
            A_TABELLA:='sat_delibera';
            A_TIPO := 'DELIBERE';
            A_TABELLA_FIGLIA :='sat_allegato';
            A_WHERECOND   := 'TRUNC(padri.DATA_REG_DETERMINA) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        END IF;
        IF P_TIPO='DELIBERA_20' THEN
            A_TABELLA:='gat_delibera';
            A_TIPO := 'DELIBERE_20';
            A_TABELLA_FIGLIA :='gat_allegato';
            A_WHERECOND   := 'TRUNC(padri.DATA_INS) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
            A_WHERECONDPADRE_FIGLIO   := 'padri.id_documento_grails=figli.id_determina_grails';
        END IF;
        IF P_TIPO='REVISIONEATTI' THEN
            A_TABELLA:='SAT_REVISIONE';
            A_TIPO := 'REVISIONE AREA ATTI';
            A_TABELLA_FIGLIA :='sat_allegato';
            A_WHERECOND   := 'trunc(padri.DATA_REVISIONE) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        END IF;
        IF P_TIPO='REVISIONEPROTO' THEN
            A_TABELLA:='SPR_REVISIONI';
            A_TIPO := 'REVISIONE AREA PROTOCOLLO';
            A_TABELLA_FIGLIA :='X';
            A_WHERECOND   := 'to_date(substr( nvl(DATA_REVISIONE,''01/01/1900''),1,10) ,''dd/mm/yyyy'') between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        END IF;
        IF P_TIPO='ALBO' THEN
            A_TABELLA:='MES_ALBI';
            A_TIPO := 'ALBO PRETORIO';
            A_TABELLA_FIGLIA :='MES_ALLEGATI_ALBO';
            A_WHERECOND   := ' (select trunc(min(stati_documento.data_aggiornamento)) from stati_documento where stati_documento.id_documento=padri.id_documento)  between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
            A_WHERECONDPADRE_FIGLIO  := 'padri.ID_ALBO=figli.ID_ALBO';
        END IF;
         IF P_TIPO='FASC_PERS_FI' THEN
            A_TABELLA:='FPF_FASCICOLO_PERSONALE';
            A_TIPO := 'FASCICOLO PERS. FIRENZE';
            A_TABELLA_FIGLIA :='X';
            A_WHERECOND   := 'TRUNC(data_inserimento) between TO_DATE('''||A_DATA_DA||''',''dd/mm/yyyy'') and TO_DATE('''||A_DATA_A||''',''dd/mm/yyyy'')';
        END IF;

        IF A_TABELLA='spr_protocolli' AND (P_UTENTI_PROTOCOLLANTI IS NOT NULL) THEN
            A_WHERECOND := A_WHERECOND || ' AND  instr('''||P_UTENTI_PROTOCOLLANTI||''',UTENTE_PROTOCOLLANTE)>0 ';
        END IF;

        IF A_TABELLA='spr_protocolli' AND (P_TIPO='PEC_USCITA') THEN
            A_WHERECOND := A_WHERECOND || ' AND  SPEDITO=''Y'' ';
        END IF;


        A_TIPO:=GETNOMETIPOMODELLO(P_TIPO);

        A_QRY:='
            select '''||A_TIPO||''' as tipoModello ,tipoOggettoFile,   posizione, count(*) as numeroOggettiFile ,  decode ( posizione, ''SU DB'' ,  round(sum(dbms_lob.getlength(testoocr)/1048576),2)  , round(sum(GETLENGTHBFILE("FILE")/1048576),2)  )  as DimMb
            from (
                select opadri.id_oggetto_file, ''PADRI'' tipo,  ''OGGETTI_FILE'' as  tipoOggettoFile ,    opadri.testoocr, "FILE" , decode(opadri.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione
                    from '||A_TABELLA||' padri, oggetti_file opadri
                where padri.id_documento=opadri.id_documento   and
                          '||A_WHERECOND||'';
        if A_TABELLA_FIGLIA<>'X' then
            A_QRY:=A_QRY||'    union all
                select ofigli.id_oggetto_file,  ''FIGLI'' tipo,  ''OGGETTI_FILE'' as  tipoOggettoFile ,   ofigli.testoocr, "FILE" , decode(ofigli.path_file,    null  ,   ''SU DB'', ''SU FS'') as posizione
                from '||A_TABELLA||' padri, '||A_TABELLA_FIGLIA||' figli, oggetti_file ofigli
                where '||A_WHERECONDPADRE_FIGLIO||' and
                          figli.id_documento=ofigli.id_documento and
                           '||A_WHERECOND||'';
        end if;

        IF P_ABILITA_SOLO_CONSERVAZIONE=0 THEN
             A_QRY:=A_QRY||'    union all
                select ogfilog.id_oggetto_file, ''PADRILOG'' tipo, ''OGGETTI_FILE_LOG'' as  tipoOggettoFile ,  ogfilog.testoocr,  '||A_BFILE_LOG||'  , decode(ogfilog.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione
                    from '||A_TABELLA||' padri,   activity_log acl  , oggetti_file_log ogfilog, DOCUMENTI, TIPI_DOCUMENTO
                where   '||A_WHERECOND||' and
                             documenti.id_documento=    padri.id_documento and
                            TIPI_DOCUMENTO.id_tipodoc =  documenti.id_tipodoc and
                           padri.id_documento  = acl.id_documento and
                           acl.id_log=ogfilog.id_log';
        END IF;
        if A_TABELLA_FIGLIA<>'X' AND P_ABILITA_SOLO_CONSERVAZIONE=0 then
            A_QRY:=A_QRY||'           union all
                select ogfilog.id_oggetto_file,  ''FIGLILOG'' tipo,  ''OGGETTI_FILE_LOG'' as  tipoOggettoFile ,  ogfilog.testoocr, '||A_BFILE_LOG||' , decode(ogfilog.path_file,    null  ,   ''SU DB'', ''SU FS'') as posizione
                from '||A_TABELLA||' padri, '||A_TABELLA_FIGLIA||' figli,  activity_log acl  , oggetti_file_log ogfilog, DOCUMENTI, TIPI_DOCUMENTO
                where '||A_WHERECONDPADRE_FIGLIO||' and
                           '||A_WHERECOND||' and
                             documenti.id_documento=    figli.id_documento and
                            TIPI_DOCUMENTO.id_tipodoc =  documenti.id_tipodoc and
                          figli.id_documento =   acl.id_documento and
                          acl.id_log=ogfilog.id_log  ';
        end if;
            A_QRY:=A_QRY|| ' )
            group By tipoOggettoFile, posizione';
     --   dbms_output.put_line(A_QRY);
            RETURN A_QRY;
    END;

  FUNCTION GETQRY_MEMO(P_TIPO VARCHAR2) RETURN VARCHAR2
    IS
        A_QRY VARCHAR2(32767);
        A_QRY_LOG VARCHAR2(32767);
        A_TAB VARCHAR2(100);
        A_TIPO VARCHAR2(100);
        A_BFILE_LOG  VARCHAR2(1000) := '     decode( ogfilog.path_file ,  null,     "FILE", bfilename( ''''||F_GETDIRECTORY_AREA_NAME(documenti.ID_DOCUMENTO) ||''''  ,  '''' || TIPI_DOCUMENTO.ACRONIMO_MODELLO || ''/'' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || ''/'' || DOCUMENTI.ID_DOCUMENTO || ''/LOG_''||acl.id_log||''/'' || ID_OGGETTO_FILE||   ''''  ) )  ';
        A_WHERE_AGGIUNTA  VARCHAR2(4000)  :='';
        P_DA VARCHAR2(10) :=P_DATA_PROTOCOLLO_MEMO_DA;
        P_A VARCHAR2(10) :=P_DATA_PROTOCOLLO_MEMO_A;
        P_MEMO_PART VARCHAR2(1);

        A_SELECT_MASTER VARCHAR2(3200);
        A_FROM_MASTER  VARCHAR2(3200);
        A_WHERE_MASTER VARCHAR2(3200);
        A_SELECT_LOG VARCHAR2(3200);
        A_FROM_LOG  VARCHAR2(3200);
        A_WHERE_LOG VARCHAR2(3200);

        A_UNION_STREAMFROMSEG VARCHAR2(30):='';
        A_SELECT_MASTER_STREAMFROMSEG VARCHAR2(3200):='';
        A_FROM_MASTER_STREAMFROMSEG  VARCHAR2(3200):='';
        A_WHERE_MASTER_STREAMFROMSEG VARCHAR2(3200):='';
        A_SELECT_LOG_STREAMFROMSEG VARCHAR2(3200):='';
        A_FROM_LOG_STREAMFROMSEG  VARCHAR2(3200):='';
        A_WHERE_LOG_STREAMFROMSEG VARCHAR2(3200):='';
    BEGIN


        IF P_TIPO='MEMO_PROTOCOLLO'  OR P_TIPO='PEC_ENTRATA' OR P_TIPO='PEC_USCITA' OR P_TIPO='RICEVUTE_PEC_USCITA' THEN
           A_TAB := 'seg_memo_protocollo';
           IF P_TIPO='PEC_ENTRATA' OR P_TIPO='PEC_USCITA' THEN
               IF P_TIPO='PEC_ENTRATA' THEN
                       P_MEMO_PART:='N';
                       P_DA :=P_DATA_PEC_ENTRATA_DA;
                       P_A    :=P_DATA_PEC_ENTRATA_A;
               ELSE
                        P_MEMO_PART:='Y';
                        P_DA :=P_DATA_PEC_USCITA_DA;
                         P_A    :=P_DATA_PEC_USCITA_A;
               END IF;

               A_WHERE_AGGIUNTA:=A_WHERE_AGGIUNTA || ' AND padri.MEMO_IN_PARTENZA='''||P_MEMO_PART||''' AND (padri.oggetto not like ''ACCETTAZIONE:%'' and padri.oggetto not like ''CONSEGNA:%'')  AND upper(filename)<>''DATICERT.XML''';
           ELSE
                IF P_TIPO='RICEVUTE_PEC_USCITA' THEN
                    P_DA :=P_DATA_RICEVUTE_PEC_USCITA_DA;
                    P_A    :=P_DATA_RICEVUTE_PEC_USCITA_A;
                     A_WHERE_AGGIUNTA:=A_WHERE_AGGIUNTA || ' AND (padri.oggetto  like ''ACCETTAZIONE:%'' OR padri.oggetto  like ''CONSEGNA:%'')  AND upper(filename)=''DATICERT.XML''';
                END IF;
           END IF;
        ELSE
            A_TAB := 'seg_stream_memo_proto';
        END IF;

        A_TIPO:=GETNOMETIPOMODELLO(P_TIPO);

        A_SELECT_MASTER:=' select opadri.id_oggetto_file, ''PADRI'' tipo,  ''OGGETTI_FILE'' as  tipoOggettoFile ,     opadri.testoocr, "FILE"  , decode(opadri.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ';
        A_FROM_MASTER:=' from '||A_TAB||' padri, oggetti_file opadri, documenti ';
        A_WHERE_MASTER := ' where  padri.id_documento=opadri.id_documento and documenti.id_documento=padri.id_documento  and
                              (select trunc(nvl(min(stati_documento.data_aggiornamento),documenti.data_aggiornamento)) from stati_documento where stati_documento.id_documento=padri.id_documento)
                              between TO_DATE('''||P_DA||''',''dd/mm/yyyy'') and TO_DATE('''||P_A||''',''dd/mm/yyyy'')  '||A_WHERE_AGGIUNTA;
         IF P_ABILITA_SOLO_CONSERVAZIONE=1 AND P_TIPO='MEMO_PROTOCOLLO'  THEN
                          A_WHERE_MASTER := A_WHERE_MASTER||' and UPPER (opadri.filename) IN
                                                              (''SEGNATURA.XML'',
                                                               ''CONFERMA.XML'',
                                                               ''ECCEZIONE.XML'',
                                                               ''AGGIORNAMENTO.XML'',
                                                               ''ANNULLAMENTO.XML'',
                                                               ''DATICERT.XML'',''CARTCERT.XML'') ';
         END IF;
         IF P_ABILITA_SOLO_CONSERVAZIONE=1 AND A_TAB = 'seg_stream_memo_proto'  THEN
               A_FROM_MASTER:=A_FROM_MASTER||' , riferimenti rife_stream,riferimenti rife,proto_view prot ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and opadri.id_documento = rife_stream.id_documento_rif ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and rife.id_documento_rif = rife_stream.id_documento ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and rife_stream.tipo_relazione = ''STREAM'' ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and rife.tipo_relazione = ''MAIL''  ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and rife.area = ''SEGRETERIA.PROTOCOLLO''  ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and prot.id_documento = rife.id_documento  ';
               A_WHERE_MASTER := A_WHERE_MASTER||' and prot.modalita = ''ARR''  ';
         END IF;


         A_SELECT_LOG := ' select ogfilog.id_oggetto_file, ''PADRILOG'' tipo, ''OGGETTI_FILE_LOG'' as  tipoOggettoFile ,  ogfilog.testoocr, '||A_BFILE_LOG||'  , decode(ogfilog.path_file,    null  ,  ''SU DB'', ''SU FS'') as posizione ';
         A_FROM_LOG := '   from '||A_TAB||' padri,   activity_log acl  , oggetti_file_log ogfilog , DOCUMENTI, TIPI_DOCUMENTO ';
         A_WHERE_LOG := ' where  (select trunc(nvl(min(stati_documento.data_aggiornamento),documenti.data_aggiornamento)) from stati_documento where stati_documento.id_documento=padri.id_documento)
                                 between TO_DATE('''||P_DA||''',''dd/mm/yyyy'') and TO_DATE('''||P_A||''',''dd/mm/yyyy'')  and
                                  documenti.id_documento=padri.id_documento and
                            TIPI_DOCUMENTO.id_tipodoc =  documenti.id_tipodoc and
                               padri.id_documento=acl.id_documento and
                               acl.id_log=ogfilog.id_log         '||A_WHERE_AGGIUNTA ;

        IF P_TIPO='PEC_ENTRATA' THEN
              A_UNION_STREAMFROMSEG := ' union all ';
              A_SELECT_MASTER_STREAMFROMSEG :=  A_SELECT_MASTER;
              A_FROM_MASTER_STREAMFROMSEG := A_FROM_MASTER || ', seg_stream_memo_proto figli, riferimenti ';
              A_WHERE_MASTER_STREAMFROMSEG := A_WHERE_MASTER;
              A_WHERE_MASTER_STREAMFROMSEG :=  REPLACE(A_WHERE_MASTER_STREAMFROMSEG,'padri.id_documento=opadri.id_documento','figli.id_documento=opadri.id_documento');
              A_WHERE_MASTER_STREAMFROMSEG :=  REPLACE(A_WHERE_MASTER_STREAMFROMSEG,'documenti.id_documento=padri.id_documento','documenti.id_documento=figli.id_documento');
              A_WHERE_MASTER_STREAMFROMSEG := A_WHERE_MASTER_STREAMFROMSEG ||
                                                                             ' and riferimenti.area=''SEGRETERIA'' and riferimenti.tipo_relazione = ''STREAM'' and '||
                                                                             ' figli.id_documento = riferimenti.ID_DOCUMENTO_RIF and  padri.id_documento = riferimenti.ID_DOCUMENTO ' ;

              A_SELECT_LOG_STREAMFROMSEG :=  A_SELECT_LOG;
              A_FROM_LOG_STREAMFROMSEG :=    A_FROM_LOG || ', seg_stream_memo_proto figli, riferimenti ';
              A_WHERE_LOG_STREAMFROMSEG := A_WHERE_LOG;
               A_WHERE_LOG_STREAMFROMSEG :=  REPLACE(A_WHERE_LOG_STREAMFROMSEG,'padri.id_documento=acl.id_documento','figli.id_documento=acl.id_documento');
              A_WHERE_LOG_STREAMFROMSEG :=  REPLACE(A_WHERE_LOG_STREAMFROMSEG,'documenti.id_documento=padri.id_documento','documenti.id_documento=figli.id_documento');
              A_WHERE_LOG_STREAMFROMSEG  :=    A_WHERE_LOG_STREAMFROMSEG ||
                                                                             ' and riferimenti.area=''SEGRETERIA'' and riferimenti.tipo_relazione = ''STREAM'' and '||
                                                                             ' figli.id_documento = riferimenti.ID_DOCUMENTO_RIF and  padri.id_documento = riferimenti.ID_DOCUMENTO ' ;
        END IF;

        IF P_ABILITA_SOLO_CONSERVAZIONE=0   THEN
            A_QRY_LOG:=' union all '||
                                   A_SELECT_LOG||'
                               '||A_FROM_LOG||'
                               '||A_WHERE_LOG||' ';

        END IF;

        A_QRY:='
            select '''||A_TIPO||''' as tipoModello ,tipoOggettoFile,   posizione, count(*) as numeroOggettiFile ,   decode ( posizione, ''SU DB'' ,  round(sum(dbms_lob.getlength(testoocr)/1048576),2)  , round(sum(GETLENGTHBFILE("FILE")/1048576),2)  )  as DimMb
                from (
                    '||A_SELECT_MASTER||'
                    '||A_FROM_MASTER||'
                    '||A_WHERE_MASTER||

                     A_QRY_LOG||

                        A_UNION_STREAMFROMSEG||'
                    '||A_SELECT_MASTER_STREAMFROMSEG||'
                    '||A_FROM_MASTER_STREAMFROMSEG||'
                    '||A_WHERE_MASTER_STREAMFROMSEG||'

                    '||A_UNION_STREAMFROMSEG||'
                    '||A_SELECT_LOG_STREAMFROMSEG||'
                    '||A_FROM_LOG_STREAMFROMSEG||'
                    '||A_WHERE_LOG_STREAMFROMSEG||'
                )
                group By tipoOggettoFile, posizione';
         -- dbms_output.put_line(A_QRY);
            RETURN A_QRY;
    END;



     PROCEDURE ABILITA_PROTOCOLLO(P_DA VARCHAR2, P_A VARCHAR2, P_LISTAUTE_PROTOCOLLANTE VARCHAR2 DEFAULT NULL)
     AS
     BEGIN
        P_ABILITA_PROTOCOLLO := 1;
        P_DATA_PROTOCOLLO_DA := P_DA;
        P_DATA_PROTOCOLLO_A := P_A;
        IF P_LISTAUTE_PROTOCOLLANTE IS NOT NULL THEN
            P_UTENTI_PROTOCOLLANTI :=  '#'||P_LISTAUTE_PROTOCOLLANTE||'#';
        END IF;
     END;

    PROCEDURE ABILITA_PROTOCOLLO_INTERO(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_PROTOCOLLO_INTERO := 1;
        P_DATA_PROTOCOLLO_INTERO_DA := P_DA;
        P_DATA_PROTOCOLLO_INTERO_A := P_A;
   END;

    PROCEDURE ABILITA_MEMO_PROTOCOLLO(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_PROTOCOLLO_MEMO := 1;
        P_DATA_PROTOCOLLO_MEMO_DA := P_DA;
        P_DATA_PROTOCOLLO_MEMO_A := P_A;
   END;

   PROCEDURE ABILITA_LETTERA_USCITA(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_LETTERA_USCITA := 1;
        P_DATA_LETTERA_USCITA_DA := P_DA;
        P_DATA_LETTERA_USCITA_A := P_A;
   END;

   PROCEDURE ABILITA_DETERMINA(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_DETERMINA := 1;
        P_DATA_DETERMINA_DA := P_DA;
        P_DATA_DETERMINA_A := P_A;
   END;

   PROCEDURE ABILITA_DELIBERA(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_DELIBERA := 1;
        P_DATA_DELIBERA_DA := P_DA;
        P_DATA_DELIBERA_A := P_A;
   END;

   PROCEDURE ABILITA_DETERMINA_20(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_DETERMINA_20 := 1;
        P_DATA_DETERMINA_20_DA := P_DA;
        P_DATA_DETERMINA_20_A := P_A;
   END;

   PROCEDURE ABILITA_DELIBERA_20(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_DELIBERA_20 := 1;
        P_DATA_DELIBERA_20_DA := P_DA;
        P_DATA_DELIBERA_20_A := P_A;
   END;


   PROCEDURE ABILITA_REVISIONE_ATTI(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_REVISIONE_ATTI := 1;
        P_DATA_REVISIONE_ATTI_DA := P_DA;
        P_DATA_REVISIONE_ATTI_A := P_A;
   END;

   PROCEDURE ABILITA_REVISIONE_PROTO(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_REVISIONE_PROTO := 1;
        P_DATA_REVISIONE_PROTO_DA := P_DA;
        P_DATA_REVISIONE_PROTO_A := P_A;
   END;

   PROCEDURE ABILITA_ALBO(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_ALBO := 1;
        P_DATA_ALBO_DA := P_DA;
        P_DATA_ALBO_A := P_A;
   END;

   PROCEDURE ABILITA_FASCICOLO_PERS_FIRENZE(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_FASC_PERS_FI := 1;
        P_DATA_FASC_PERS_FI_DA := P_DA;
        P_DATA_FASC_PERS_FI_A   := P_A;
   END;

   PROCEDURE ABILITA_PEC_ENTRATA(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_PEC_ENTRATA := 1;
        P_DATA_PEC_ENTRATA_DA := P_DA;
        P_DATA_PEC_ENTRATA_A   := P_A;
   END;

   PROCEDURE ABILITA_PEC_USCITA(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_PEC_USCITA := 1;
        P_DATA_PEC_USCITA_DA := P_DA;
        P_DATA_PEC_USCITA_A   := P_A;
   END;

   PROCEDURE ABILITA_RICEVUTE_PEC_USCITA(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_RICEVUTE_PEC_USCITA := 1;
        P_DATA_RICEVUTE_PEC_USCITA_DA := P_DA;
        P_DATA_RICEVUTE_PEC_USCITA_A   := P_A;
   END;

   PROCEDURE ABILITA_FATT_ELETTRONICHE(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        P_ABILITA_FATT_ELETTRONICHE := 1;
        P_DATA_FATT_ELETTRONICHE_DA := P_DA;
        P_DATA_FATT_ELETTRONICHE_A   := P_A;
   END;

   PROCEDURE ABILITA_SOLO_CONSERVAZIONE(P_DA VARCHAR2, P_A VARCHAR2)
    AS
    BEGIN
        ABILITA_PROTOCOLLO(P_DA , P_A );
        ABILITA_PROTOCOLLO_INTERO(P_DA , P_A );
        ABILITA_MEMO_PROTOCOLLO(P_DA , P_A );
        ABILITA_DETERMINA_20(P_DA , P_A );
        ABILITA_DELIBERA_20(P_DA , P_A );
        ABILITA_FATT_ELETTRONICHE(P_DA , P_A );
        P_ABILITA_SOLO_CONSERVAZIONE := 1;
   END;

   PROCEDURE MAKE_SINGLE_QRY (tipo VARCHAR2, tipoModello  VARCHAR2, qry VARCHAR2, parametri_ric VARCHAR2)
   AS
   TYPE cur_typ IS REF CURSOR;
    c cur_typ;
    v_tipoModello   GDM_ANALISI_SPAZIOFILE.TIPO_MODELLO%TYPE;
    v_tipoOggettoFile  GDM_ANALISI_SPAZIOFILE.TIPO_OGGETTO_FILE%TYPE;
    v_posizione    GDM_ANALISI_SPAZIOFILE.POSIZIONE%TYPE;
    v_numeroOggettiFile   GDM_ANALISI_SPAZIOFILE.NUMERO_OGGETTI_FILE%TYPE;
    v_dimMb   GDM_ANALISI_SPAZIOFILE.DIMENSIONE_MB%TYPE;
    v_conta  NUMBER(1) :=0;
     A_TIPO VARCHAR2(100);
   BEGIN
        A_TIPO:=GETNOMETIPOMODELLO(tipoModello);

         OPEN c FOR qry;

         LOOP
            FETCH c INTO v_tipoModello, v_tipoOggettoFile, v_posizione,v_numeroOggettiFile, v_dimMb;
            EXIT WHEN c%NOTFOUND;



            --IF v_conta=0 then
                -- LA PRIMA VOLTA
                   --Ricopio tutto sulla log

                      INSERT INTO GDM_ANALISI_SPAZIOFILE_LOG
                        SELECT * FROM GDM_ANALISI_SPAZIOFILE
                        WHERE TIPO_MODELLO=v_tipoModello ;


                      --Pulisco la tabella
                     DELETE  FROM GDM_ANALISI_SPAZIOFILE
                      WHERE TIPO_MODELLO=v_tipoModello ;

           -- end if;


            INSERT INTO GDM_ANALISI_SPAZIOFILE
            (TIPO_MODELLO,TIPO_OGGETTO_FILE,POSIZIONE,NUMERO_OGGETTI_FILE,DIMENSIONE_MB, PAR_RICERCA,DATA_ELABORAZIONE )
            VALUES
            (v_tipoModello, v_tipoOggettoFile, v_posizione,v_numeroOggettiFile, v_dimMb,parametri_ric, sysdate);
            v_conta:=1;
         END LOOP;
         CLOSE c;

         if v_conta=0  then
            --Se non ho trovato nulla, inserisco delle righe VUOTE
                INSERT INTO GDM_ANALISI_SPAZIOFILE_LOG
                    SELECT * FROM GDM_ANALISI_SPAZIOFILE
                    WHERE TIPO_MODELLO=NVL(v_tipoModello,A_TIPO) ;


                  --Pulisco la tabella
                 DELETE  FROM GDM_ANALISI_SPAZIOFILE
                  WHERE TIPO_MODELLO=NVL(v_tipoModello,A_TIPO) ;

                 INSERT INTO GDM_ANALISI_SPAZIOFILE
                (TIPO_MODELLO,TIPO_OGGETTO_FILE,POSIZIONE,NUMERO_OGGETTI_FILE,DIMENSIONE_MB, PAR_RICERCA,DATA_ELABORAZIONE )
                VALUES
                (NVL(v_tipoModello,A_TIPO), 'OGGETTI_FILE', 'SU DB',0,0,parametri_ric, sysdate);

                INSERT INTO GDM_ANALISI_SPAZIOFILE
                (TIPO_MODELLO,TIPO_OGGETTO_FILE,POSIZIONE,NUMERO_OGGETTI_FILE,DIMENSIONE_MB, PAR_RICERCA,DATA_ELABORAZIONE )
                VALUES
                (NVL(v_tipoModello,A_TIPO), 'OGGETTI_FILE', 'SU FS',0,0,parametri_ric, sysdate);

                IF P_ABILITA_SOLO_CONSERVAZIONE=0 THEN
                     INSERT INTO GDM_ANALISI_SPAZIOFILE
                    (TIPO_MODELLO,TIPO_OGGETTO_FILE,POSIZIONE,NUMERO_OGGETTI_FILE,DIMENSIONE_MB, PAR_RICERCA,DATA_ELABORAZIONE )
                    VALUES
                    (NVL(v_tipoModello,A_TIPO), 'OGGETTI_FILE_LOG', 'SU DB',0, 0,parametri_ric, sysdate);

                    INSERT INTO GDM_ANALISI_SPAZIOFILE
                    (TIPO_MODELLO,TIPO_OGGETTO_FILE,POSIZIONE,NUMERO_OGGETTI_FILE,DIMENSIONE_MB, PAR_RICERCA,DATA_ELABORAZIONE )
                    VALUES
                    (NVL(v_tipoModello,A_TIPO), 'OGGETTI_FILE_LOG', 'SU FS',0, 0,parametri_ric, sysdate);
                END IF;
         end if;
   END;

    PROCEDURE ANALIZZA
    AS

    qstr  VARCHAR2(32767);
    p_type VARCHAR2(100);
    p_parametri_ric VARCHAR2(32000);

    BEGIN
        /*  PROTOCOLLO */
        IF P_ABILITA_PROTOCOLLO=1 THEN
            p_type:='PROTOCOLLO';
            p_parametri_ric := 'DATA TRA '|| P_DATA_PROTOCOLLO_DA|| ' E '|| P_DATA_PROTOCOLLO_A;
            IF P_UTENTI_PROTOCOLLANTI IS NOT NULL THEN
                p_parametri_ric := p_parametri_ric || ' E Utenti protocollanti='||P_UTENTI_PROTOCOLLANTI;
            END IF;
            BEGIN
                qstr := GETQRY_PROTOTYPE('',P_DATA_PROTOCOLLO_DA,P_DATA_PROTOCOLLO_A);

               MAKE_SINGLE_QRY (p_type,'PROTOCOLLI',qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  PROTOCOLLO_INTERO */
        IF P_ABILITA_PROTOCOLLO_INTERO=1 THEN
            p_type:='PROTOCOLLO_INTEROP';
            p_parametri_ric := 'DATA TRA '|| P_DATA_PROTOCOLLO_INTERO_DA|| ' E '|| P_DATA_PROTOCOLLO_INTERO_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE('INTEROP',P_DATA_PROTOCOLLO_INTERO_DA,P_DATA_PROTOCOLLO_INTERO_A);

               MAKE_SINGLE_QRY (p_type,'INTEROP',qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  SEG_MEMO_PROTO */
        IF P_ABILITA_PROTOCOLLO_MEMO=1 THEN

            p_parametri_ric := 'DATA CREAZIONE TRA '|| P_DATA_PROTOCOLLO_MEMO_DA|| ' E '|| P_DATA_PROTOCOLLO_MEMO_A;
            BEGIN
                p_type:='MEMO_PROTOCOLLO';
                qstr := GETQRY_MEMO(p_type);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               p_type:='STREAM_MEMO_PROTOCOLLO';
               qstr := GETQRY_MEMO(p_type);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  LETTERA */
        IF P_ABILITA_LETTERA_USCITA=1 THEN
            p_type:='LETTERA';
            p_parametri_ric := 'DATA TRA '|| P_DATA_LETTERA_USCITA_DA|| ' E '|| P_DATA_LETTERA_USCITA_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_LETTERA_USCITA_DA,P_DATA_LETTERA_USCITA_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  DETERMINA */
        IF P_ABILITA_DETERMINA=1 THEN
            p_type:='DETERMINA';
            p_parametri_ric := 'DATA TRA '|| P_DATA_DETERMINA_DA|| ' E '|| P_DATA_DETERMINA_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_DETERMINA_DA,P_DATA_DETERMINA_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  DETERMINA 2.0*/
         IF P_ABILITA_DETERMINA_20=1 THEN
            p_type:='DETERMINA_20';
            p_parametri_ric := 'DATA TRA '|| P_DATA_DETERMINA_20_DA|| ' E '|| P_DATA_DETERMINA_20_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_DETERMINA_20_DA,P_DATA_DETERMINA_20_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);
              --   dbms_output.put_line(qstr);
               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  DELIBERA */
        IF P_ABILITA_DETERMINA=1 THEN
            p_type:='DELIBERA';
            p_parametri_ric := 'DATA TRA '|| P_DATA_DELIBERA_DA|| ' E '|| P_DATA_DELIBERA_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_DELIBERA_DA,P_DATA_DELIBERA_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  DELIBERA 2.0*/
        IF P_ABILITA_DETERMINA=1 THEN
            p_type:='DELIBERA 2.0';
            p_parametri_ric := 'DATA TRA '|| P_DATA_DELIBERA_20_DA|| ' E '|| P_DATA_DELIBERA_20_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_DELIBERA_20_DA,P_DATA_DELIBERA_20_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  REVISIONE ATTI */
        IF P_ABILITA_REVISIONE_ATTI=1 THEN
            p_type:='REVISIONEATTI';
            p_parametri_ric := 'DATA TRA '|| P_DATA_REVISIONE_ATTI_DA|| ' E '|| P_DATA_REVISIONE_ATTI_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_REVISIONE_ATTI_DA,P_DATA_REVISIONE_ATTI_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  REVISIONE PROTO */
        IF P_ABILITA_REVISIONE_PROTO=1 THEN
            p_type:='REVISIONEPROTO';
            p_parametri_ric := 'DATA TRA '|| P_DATA_REVISIONE_PROTO_DA|| ' E '|| P_DATA_REVISIONE_PROTO_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_REVISIONE_PROTO_DA,P_DATA_REVISIONE_PROTO_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /*  ALBO*/
        IF P_ABILITA_ALBO=1 THEN
            p_type:='ALBO';
            p_parametri_ric := 'DATA TRA '|| P_DATA_ALBO_DA|| ' E '|| P_DATA_ALBO_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_ALBO_DA,P_DATA_ALBO_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;


        /*  FASC_PERS_FIRENZE*/
        IF P_ABILITA_FASC_PERS_FI=1 THEN
            p_type:='FASC_PERS_FI';
            p_parametri_ric := 'DATA TRA '|| P_DATA_FASC_PERS_FI_DA|| ' E '|| P_DATA_FASC_PERS_FI_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_FASC_PERS_FI_DA,P_DATA_FASC_PERS_FI_A);

               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /* PEC_ENTRATA */
        IF P_ABILITA_PEC_ENTRATA=1 THEN
            p_type:='PEC_ENTRATA';
            p_parametri_ric := 'DATA TRA '|| P_DATA_PEC_ENTRATA_DA|| ' E '|| P_DATA_PEC_ENTRATA_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_PEC_ENTRATA_DA,P_DATA_PEC_ENTRATA_A);

                qstr := qstr || ' UNION ALL ' || GETQRY_MEMO(p_type);

                qstr := 'select tipoModello,tipoOggettoFile, posizione, sum( numeroOggettiFile), sum(DimMb) from ('||qstr||') group By tipoModello,tipoOggettoFile, posizione';
           --   dbms_output.put_line(qstr);


               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

        /* PEC_USCITA */
        IF P_ABILITA_PEC_USCITA=1 THEN
            p_type:='PEC_USCITA';
            p_parametri_ric := 'DATA TRA '|| P_DATA_PEC_USCITA_DA|| ' E '|| P_DATA_PEC_USCITA_A;
            BEGIN
                qstr := GETQRY_PROTOTYPE(p_type,P_DATA_PEC_USCITA_DA,P_DATA_PEC_USCITA_A);

                qstr := qstr || ' UNION ALL ' || GETQRY_MEMO(p_type);

                 qstr := 'select tipoModello,tipoOggettoFile, posizione, sum( numeroOggettiFile), sum(DimMb) from ('||qstr||') group By tipoModello,tipoOggettoFile, posizione';

            --dbms_output.put_line(qstr);
               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;


        /* PEC_USCITA */
        IF P_ABILITA_RICEVUTE_PEC_USCITA=1 THEN
            p_type:='RICEVUTE_PEC_USCITA';
            p_parametri_ric := 'DATA TRA '|| P_DATA_RICEVUTE_PEC_USCITA_DA|| ' E '|| P_DATA_RICEVUTE_PEC_USCITA_A;
            BEGIN
                qstr := GETQRY_MEMO(p_type);
                              --       dbms_output.put_line(qstr);
               MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;


        /*  DETERMINA */
        IF P_ABILITA_FATT_ELETTRONICHE=1 THEN
            p_type:='FATT_ELETTRONICHE';
            p_parametri_ric := 'DATA TRA '|| P_DATA_FATT_ELETTRONICHE_DA|| ' E '|| P_DATA_FATT_ELETTRONICHE_A;
            BEGIN
                qstr := GETQRY_FATTELETTR(P_DATA_FATT_ELETTRONICHE_DA,P_DATA_FATT_ELETTRONICHE_A);

                MAKE_SINGLE_QRY (p_type,p_type,qstr,p_parametri_ric);

               COMMIT;
            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in analisi '||p_type||'. Errore= '||sqlerrm);
            END;
        END IF;

    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
    END;
END ANALISI_SPAZIOFILE;
/


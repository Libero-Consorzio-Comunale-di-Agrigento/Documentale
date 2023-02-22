CREATE OR REPLACE PACKAGE BODY GDC_UTILITY_PKG AS
 FUNCTION F_TREEVIEW (
      a_wrksp    IN   VARCHAR2,
      a_utente   IN   VARCHAR2,
      a_ruolo    IN   VARCHAR2
   )
      RETURN VARCHAR2
   AS
      tree   VARCHAR2 (32000);
      CURSOR c_tree
      IS
         SELECT icona, parentid, parentid idcartprov, nodeid,
                UPPER (nome) nome, text, url, tooltip, custom_image,
                default_image, custom_style, id_cartella, tipo_oggetto,
                id_oggetto, wrksp, nomewrksp, ruolo, ricercamodulistica,
                compdelogg, compmodogg, compdoccartel, compcompogg, idcart
           FROM (SELECT icona, parentid, parentid idcartprov, nodeid,
                        DECODE (tree.tipo_oggetto,
                                'C', c.nome,
                                'Q', q.nome
                               ) nome,
                        text, url, tooltip, custom_image, default_image,
                        custom_style, tree.id_cartella, tipo_oggetto,
                        id_oggetto, a_wrksp wrksp, c2.nome nomewrksp, a_ruolo ruolo,
                        DECODE
                           (tree.tipo_oggetto,
                            'C', '',
                            'Q', DECODE
                                      (INSTR (q.filtro, 'RICERCAMODULISTICA_'),
                                       0, '',
                                       SUBSTR (q.filtro,
                                                 LENGTH ('RICERCAMODULISTICA_')
                                               + 1,
                                               LENGTH (q.filtro)
                                              )
                                      )
                           ) ricercamodulistica,
                        gdm_competenza.gdm_verifica
                                     (DECODE (tree.tipo_oggetto,
                                              'C', 'VIEW_CARTELLA',
                                              'Q', 'QUERY'
                                             ),
                                      DECODE (tree.tipo_oggetto,
                                              'C', vw.id_viewcartella,
                                              'Q', id_oggetto
                                             ),
                                      'D',
                                      a_utente,
                                      f_trasla_ruolo (a_ruolo,
                                                      'GDMWEB',
                                                      'GDMWEB'
                                                     ),
                                      TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                     ) compdelogg,
                        gdm_competenza.gdm_verifica
                                     (DECODE (tree.tipo_oggetto,
                                              'C', 'VIEW_CARTELLA',
                                              'Q', 'QUERY'
                                             ),
                                      DECODE (tree.tipo_oggetto,
                                              'C', vw.id_viewcartella,
                                              'Q', id_oggetto
                                             ),
                                      'U',
                                      a_utente,
                                      f_trasla_ruolo (a_ruolo,
                                                      'GDMWEB',
                                                      'GDMWEB'
                                                     ),
                                      TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                     ) compmodogg,
                        DECODE
                           (tree.tipo_oggetto,
                            'C', gdm_competenza.gdm_verifica
                                                   ('VIEW_CARTELLA',
                                                    vw.id_viewcartella,
                                                    'C',
                                                    a_utente,
                                                    f_trasla_ruolo (a_ruolo,
                                                                    'GDMWEB',
                                                                    'GDMWEB'
                                                                   ),
                                                    TO_CHAR (SYSDATE,
                                                             'dd/mm/yyyy'
                                                            )
                                                   ),
                            'Q', -1
                           ) compdoccartel,
                        gdm_competenza.gdm_verifica
                                    (DECODE (tree.tipo_oggetto,
                                             'C', 'VIEW_CARTELLA',
                                             'Q', 'QUERY'
                                            ),
                                     DECODE (tree.tipo_oggetto,
                                             'C', vw.id_viewcartella,
                                             'Q', id_oggetto
                                            ),
                                     'M',
                                     a_utente,
                                     f_trasla_ruolo (a_ruolo, 'GDMWEB',
                                                     'GDMWEB'),
                                     TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                    ) compcompogg,
                        DECODE (tree.tipo_oggetto,
                                'C', vw.id_viewcartella,
                                'Q', id_oggetto
                               ) idcart
                   FROM (SELECT     parentid, nodeid, text, url, tooltip,
                                    custom_image, default_image, custom_style,
                                    id_cartella, tipo_oggetto, id_oggetto
                               FROM (SELECT parentid parentid, nodeid, text,
                                            url, tooltip, custom_image,
                                            default_image, custom_style,
                                            id_cartella, 'C' tipo_oggetto,
                                            id_oggetto
                                       FROM cart_foglie
                                     UNION ALL
                                     SELECT parentid parentid, nodeid, text,
                                            url, tooltip, default_image,
                                            custom_image, custom_style,
                                            id_cartella, tipo_oggetto,
                                            id_oggetto
                                       FROM foglie)
                         CONNECT BY PRIOR nodeid = parentid
                         START WITH id_cartella = - a_wrksp) tree,
                        cartelle c,
                        cartelle c2,
                        QUERY q,
                        tipi_documento td,
                        documenti d,
                        view_cartella vw
                  WHERE tree.id_oggetto = c.id_cartella(+)
                    AND tree.id_oggetto = q.id_query(+)
                    AND c.id_cartella = vw.id_cartella(+)
                    AND d.id_tipodoc = td.id_tipodoc
                    AND c2.id_cartella = - a_wrksp
                    AND d.id_documento =
                           DECODE (tree.tipo_oggetto,
                                   'C', c.id_documento_profilo,
                                   'Q', q.id_documento_profilo
                                  )
                    AND DECODE (tree.tipo_oggetto,
                                'C', NVL (c.stato, 'BO'),
                                'BO'
                               ) <> 'CA')
          WHERE gdm_competenza.gdm_verifica (DECODE (tipo_oggetto,
                                                     'C', 'VIEW_CARTELLA',
                                                     'Q', 'QUERY'
                                                    ),
                                             DECODE (tipo_oggetto,
                                                     'C', idcart,
                                                     'Q', id_oggetto
                                                    ),
                                             'L',
                                             UPPER (a_utente),
                                             f_trasla_ruolo (a_utente,
                                                             'GDMWEB',
                                                             'GDMWEB'
                                                            ),
                                             TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                            ) = 1;
   BEGIN
      tree := '';
      FOR ctree IN c_tree
      LOOP
         tree := tree || '@' || ctree.nome;
      END LOOP;
      RETURN tree;
   END;
   FUNCTION F_LOGO ( a_modulo IN VARCHAR2 ) RETURN VARCHAR2
   AS
      src  VARCHAR2 (32000);
   BEGIN
       SELECT NVL (amvweb.get_preferenza ('Logo sx', a_modulo),'../common/images/logo.gif') logo
       INTO src
       FROM DUAL;
       RETURN SRC;
   END;
   FUNCTION F_LOGO_DOCUMENTO ( a_modulo IN VARCHAR2 ) RETURN VARCHAR2
   AS
      LOGO  VARCHAR2 (32000);
      GDMFX_BACKGROUNDIMAGE VARCHAR2 (32000);
   BEGIN
    SELECT amvweb.get_preferenza('GDMFX_BACKGROUNDIMAGE',a_modulo) logo
     INTO GDMFX_BACKGROUNDIMAGE
    FROM DUAL;
    LOGO:='';
    IF GDMFX_BACKGROUNDIMAGE IS NOT NULL THEN
      LOGO:='<table width="100%" border="0" cellpadding="0" cellspacing="0">'
            ||'<tr height="80">'
            ||' <td valign="bottom" align="left" width="100%" '
            ||'  background='||GDMFX_BACKGROUNDIMAGE||'></td></tr></table>';
    END IF;
    RETURN LOGO;
   END;
  PROCEDURE F_CREA_COLLEGAMENTI (LISTA_DOC IN VARCHAR2, LISTA_COLL IN VARCHAR2, UTENTE IN VARCHAR2)
  AS
  BEGIN
    DECLARE
        CURSOR SEQ_DOC
           IS
           select id_documento
            from documenti
            where instr(LISTA_DOC,'@'||id_documento||'@')>0;
       CURSOR SEQ_COLL
           IS
           select id_documento
            from documenti
            where instr(LISTA_COLL,'@'||id_documento||'@')>0;
   BEGIN
       BEGIN
           FOR C1 IN SEQ_DOC LOOP
            BEGIN
            FOR C2 IN SEQ_COLL LOOP
             BEGIN
               INSERT INTO RIFERIMENTI
                   (ID_DOCUMENTO, ID_DOCUMENTO_RIF, AREA, TIPO_RELAZIONE,
                    DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
                VALUES
                   (C1.ID_DOCUMENTO,C2.ID_DOCUMENTO,'GDMSYS','LINK_DOC',SYSDATE,UTENTE);
                EXCEPTION
              WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR('-20998','IMPOSSIBILE CREARE UN COLLEGAMENTO, COLLEGAMENTO GIÀ ESISTENTE ');
               --DBMS_OUTPUT.PUT_LINE(C1.ID_DOCUMENTO||'--'||C2.ID_DOCUMENTO);
             END;
            END LOOP;
            END;
           END LOOP;
       END;
       --COMMIT;
   EXCEPTION WHEN OTHERS THEN
     --ROLLBACK;
     RAISE_APPLICATION_ERROR('-20990',SQLERRM);
   END;
 END;
   FUNCTION F_JDMSMANUALI(WRKSP VARCHAR2) RETURN VARCHAR2
   AS
      MPATH  VARCHAR2 (2000);
   BEGIN
       BEGIN
          SELECT VALORE
          INTO MPATH
          FROM REGISTRO
          WHERE CHIAVE='GDMWEB/WRKSP'||WRKSP;
          RETURN MPATH
          ;
          EXCEPTION WHEN NO_DATA_FOUND THEN
          RETURN 'index.htm';
        END;
   RETURN MPATH;
   END;
   FUNCTION F_JDMSMANUALI_EXIST(WRKSP VARCHAR2) RETURN VARCHAR2
   AS
     MPATH  VARCHAR2 (2000);
    BEGIN
      SELECT VALORE
      INTO MPATH
      FROM REGISTRO
      WHERE CHIAVE=WRKSP;
      RETURN 'S';
      EXCEPTION
       WHEN NO_DATA_FOUND THEN
        RETURN 'N';
   END;
   PROCEDURE F_JDMSMANUALI_INSERT_UPDATE(WRKSP IN VARCHAR2,PATHM IN VARCHAR2)
    AS
    BEGIN
      DECLARE
       MPATH VARCHAR2(2000);
      BEGIN
      IF  F_JDMSMANUALI_EXIST(WRKSP) = 'S' THEN
         BEGIN
             UPDATE REGISTRO SET
             VALORE = PATHM
             WHERE CHIAVE = WRKSP;
             EXCEPTION
              WHEN OTHERS THEN
              ROLLBACK;
               RAISE_APPLICATION_ERROR('-20998',  'IMPOSSIBILE AGGIORNARE IL PATH MANUALE '||SQLERRM);
          END;
      ELSE
         BEGIN
             INSERT INTO REGISTRO (CHIAVE, STRINGA, COMMENTO, VALORE)
                           VALUES (WRKSP, 'MANUALE', '',PATHM);
             EXCEPTION
              WHEN OTHERS THEN
              ROLLBACK;
               RAISE_APPLICATION_ERROR('-20999',  'IMPOSSIBILE INSERIRE IL PATH MANUALE '||SQLERRM);
         END;
      END IF;
      END;
      BEGIN
        COMMIT;
          EXCEPTION
                 WHEN OTHERS THEN
                    ROLLBACK;
                     RAISE_APPLICATION_ERROR('-20999',  'IMPOSSIBILE EFFETTUARE L''OPERAZIONE '||SQLERRM);
      END;
   END;
   PROCEDURE F_JDMSMANUALI_DELETE(WRKSP IN VARCHAR2)
    AS
    BEGIN
      DECLARE
       MPATH VARCHAR2(2000);
      BEGIN
      IF  F_JDMSMANUALI_EXIST(WRKSP) = 'S' THEN
         BEGIN
            DELETE FROM REGISTRO
             WHERE CHIAVE = WRKSP;
             EXCEPTION
              WHEN OTHERS THEN
              ROLLBACK;
               RAISE_APPLICATION_ERROR('-20998',  'IMPOSSIBILE ELIMINARE IL MANUALE ASSOCIATO'||SQLERRM);
          END;
      END IF;
      END;
      BEGIN
        COMMIT;
          EXCEPTION
                 WHEN OTHERS THEN
                    ROLLBACK;
                     RAISE_APPLICATION_ERROR('-20999',  'IMPOSSIBILE ELIMINARE IL MANUALE ASSOCIATO '||SQLERRM);
      END;
   END;
   FUNCTION F_SETWRKSP(a_wrksp IN VARCHAR2,a_modulo IN VARCHAR2,a_utente IN VARCHAR2) RETURN NUMBER
   IS
   BEGIN
      AMVWEB.set_preferenza('WRKSP_DEFAULT',a_wrksp,a_modulo,a_utente);
      RETURN 1;
      EXCEPTION WHEN OTHERS THEN
        RETURN 0;
   END;
   FUNCTION F_LISTWRKSP(
        a_utente   IN   VARCHAR2,
        a_ruolo    IN   VARCHAR2)  RETURN wrk_rif
   IS
      d_result   wrk_rif;
    BEGIN
    OPEN d_result FOR
         SELECT 1 ORDINE,'Gestisci...' NOME, 0 ID_CARTELLA, 0 ID_VIEWCARTELLA,0 UP ,0 DE, 0 MA
         FROM DUAL
         WHERE A_RUOLO = 'AMM'
         UNION
         SELECT  2 ORDINE, nome, cartelle.id_cartella, view_cartella.id_viewcartella,
                 gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                              id_viewcartella,
                                              'U',
                                              a_utente,
                                              f_trasla_ruolo (a_ruolo,
                                                              'GDMWEB',
                                                              'GDMWEB'
                                                             ),
                                              TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                             ) UP,
                 gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                              id_viewcartella,
                                              'D',
                                              a_utente,
                                              f_trasla_ruolo (a_ruolo,
                                                              'GDMWEB',
                                                              'GDMWEB'
                                                             ),
                                              TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                             ) de,
                 1 ma
            FROM cartelle, view_cartella
           WHERE cartelle.id_cartella < 0
             AND cartelle.id_cartella = view_cartella.id_cartella
             AND cartelle.id_cartella <> -3
             AND cartelle.id_cartella <> -2
             AND NVL (cartelle.stato, 'BO') <> 'CA'
             AND gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                              id_viewcartella,
                                              'L',
                                              a_utente,
                                              f_trasla_ruolo (a_ruolo,
                                                              'GDMWEB',
                                                              'GDMWEB'
                                                             ),
                                              TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                             ) > 0
          ORDER BY ORDINE,NOME;
         RETURN d_result;
    END;
    FUNCTION F_LISTWRKSP_ML(
        a_utente   IN   VARCHAR2,
        a_ruolo    IN   VARCHAR2,
        a_lingua  IN VARCHAR2)  RETURN wrk_rif
   IS
      d_result   wrk_rif;
    BEGIN
    OPEN d_result FOR
         SELECT 1 ORDINE,GDM_UTILITY.F_MULTILINGUA ('Gestisci',A_LINGUA )||'...' NOME, 0 ID_CARTELLA, 0 ID_VIEWCARTELLA,0 UP ,0 DE, 0 MA
         FROM DUAL
         WHERE A_RUOLO = 'AMM'
         UNION
         SELECT  2 ORDINE, GDM_UTILITY.F_MULTILINGUA (nome,A_LINGUA ), cartelle.id_cartella, view_cartella.id_viewcartella,
                 gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                              id_viewcartella,
                                              'U',
                                              a_utente,
                                              f_trasla_ruolo (a_ruolo,
                                                              'GDMWEB',
                                                              'GDMWEB'
                                                             ),
                                              TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                             ) UP,
                 gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                              id_viewcartella,
                                              'D',
                                              a_utente,
                                              f_trasla_ruolo (a_ruolo,
                                                              'GDMWEB',
                                                              'GDMWEB'
                                                             ),
                                              TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                             ) de,
                 1 ma
            FROM cartelle, view_cartella
           WHERE cartelle.id_cartella < 0
             AND cartelle.id_cartella = view_cartella.id_cartella
             AND cartelle.id_cartella <> -3
             AND cartelle.id_cartella <> -2
             AND NVL (cartelle.stato, 'BO') <> 'CA'
             AND gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                              id_viewcartella,
                                              'L',
                                              a_utente,
                                              f_trasla_ruolo (a_ruolo,
                                                              'GDMWEB',
                                                              'GDMWEB'
                                                             ),
                                              TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                             ) > 0
          ORDER BY ORDINE,NOME;
         RETURN d_result;
    END;
FUNCTION F_GETWORKAREA (
      p_oggetto        VARCHAR2,
      p_tipo_oggetto   VARCHAR2,
      p_utente         VARCHAR2
   )
      RETURN wrk_rif
   IS
      d_result   wrk_rif;
   BEGIN
      OPEN d_result FOR
         SELECT ordina, data_aggiornamento, dataagg, utenteagg, id_tipodoc,
                cm, area, cr, ordinaquery, icona, icnome, modificata,
                id_cart_prov, id_oggetto, profilo, nome, nome as blocco, collegamento,
                nomeupper, tipo_oggetto, tipo, stato, nomeoggettoattuale,
                modifica, elimina, competenze, oggetto, ricercamodulistica
           --, NLSSORT (nomeupper, 'NLS_SORT = ASCII7')
         FROM   (SELECT /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */
                        1 ordina, c2.data_aggiornamento,
                        TO_CHAR (c2.data_aggiornamento, 'dd/mm/yyyy') dataagg,
                        f_nominativo_utente
                                           (c2.utente_aggiornamento)
                                                                    utenteagg,
                        TO_NUMBER (NULL) id_tipodoc, TO_CHAR (NULL) cm,
                        '' area, '' cr, 0 ordinaquery, td.icona,
                        ic.nome icnome, ic.modificata,
                        l.id_cartella id_cart_prov, id_oggetto id_oggetto,
                        c2.id_documento_profilo profilo,
                        f_link (id_oggetto, tipo_oggetto) nome,
                        TO_CHAR (NULL) collegamento,
                        UPPER (f_link (id_oggetto, tipo_oggetto))
                                                                 AS nomeupper,
                        'C' tipo_oggetto, '' tipo, '-' stato,
                        '<b>' || c.nome || '</b>' nomeoggettoattuale,
                        gdm_competenza.gdm_verifica
                                     ('VIEW_CARTELLA',
                                      f_idview_cartella (id_oggetto),
                                      'U',
                                      p_utente,
                                      f_trasla_ruolo (p_utente,
                                                      'GDMWEB',
                                                      'GDMWEB'
                                                     ),
                                      TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                     ) modifica,
                        gdm_competenza.gdm_verifica
                                      ('VIEW_CARTELLA',
                                       f_idview_cartella (id_oggetto),
                                       'D',
                                       p_utente,
                                       f_trasla_ruolo (p_utente,
                                                       'GDMWEB',
                                                       'GDMWEB'
                                                      ),
                                       TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                      ) elimina,
                        gdm_competenza.gdm_verifica
                                   ('VIEW_CARTELLA',
                                    f_idview_cartella (id_oggetto),
                                    'M',
                                    p_utente,
                                    f_trasla_ruolo (p_utente,
                                                    'GDMWEB',
                                                    'GDMWEB'
                                                   ),
                                    TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                   ) competenze,
                        id_oggetto oggetto, TO_CHAR (NULL) ricercamodulistica
                   FROM documenti d,
                        links l,
                        cartelle c,
                        cartelle c2,
                        tipi_documento td,
                        icone ic
                  WHERE l.id_cartella = p_oggetto
                    AND c.id_cartella = p_oggetto
                    AND c2.id_cartella = id_oggetto
                    AND tipo_oggetto = 'C'
                    AND d.id_documento = c2.id_documento_profilo
                    AND d.id_tipodoc = td.id_tipodoc
                    AND td.icona = ic.icona(+)
                    AND NVL (c2.stato, 'BO') <> 'CA'
                    AND l.ordinamento > ' ')
          WHERE ((gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                               f_idview_cartella (oggetto),
                                               'L',
                                               p_utente,
                                               f_trasla_ruolo (p_utente,
                                                               'GDMWEB',
                                                               'GDMWEB'
                                                              ),
                                               TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                              ) = 1
                 )
                )
         UNION ALL
         SELECT ordina, data_aggiornamento, dataagg, utenteagg, id_tipodoc,
                cm, area, cr, ordinaquery, icona, icnome, modificata,
                id_cart_prov, id_oggetto, profilo, nome, nome as blocco, collegamento,
                nomeupper, tipo_oggetto, tipo, stato, nomeoggettoattuale,
                modifica, elimina, competenze, oggetto,
                ricercamodulistica --,NLSSORT (nomeupper, 'NLS_SORT = ASCII7')
           FROM (SELECT 2 ordina, c2.data_aggiornamento,
                        TO_CHAR (c2.data_aggiornamento, 'dd/mm/yyyy') dataagg,
                        f_nominativo_utente
                                           (c2.utente_aggiornamento)
                                                                    utenteagg,
                        TO_NUMBER (NULL) id_tipodoc, TO_CHAR (NULL) cm,
                        '' area, '' cr, 0 ordinaquery, td.icona,
                        ic.nome icnome, ic.modificata,
                        coll.id_cartella id_cart_prov,
                        id_cartella_collegata id_oggetto,
                        c2.id_documento_profilo profilo,
                        f_link (id_cartella_collegata, 'C') nome,
                        TO_CHAR (coll.id_collegamento) collegamento,
                        UPPER (f_link (id_cartella_collegata, 'C')
                              ) AS nomeupper,
                        'X' tipo_oggetto, '' tipo, '-' stato,
                        '<b>' || c.nome || '</b>' nomeoggettoattuale,
                        gdm_competenza.gdm_verifica
                           ('VIEW_CARTELLA',
                            f_idview_cartella (id_cartella_collegata),
                            'U',
                            p_utente,
                            f_trasla_ruolo (p_utente, 'GDMWEB', 'GDMWEB'),
                            TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                           ) modifica,
                        gdm_competenza.gdm_verifica
                           ('VIEW_CARTELLA',
                            f_idview_cartella (id_cartella_collegata),
                            'D',
                            p_utente,
                            f_trasla_ruolo (p_utente, 'GDMWEB', 'GDMWEB'),
                            TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                           ) elimina,
                        gdm_competenza.gdm_verifica
                           ('VIEW_CARTELLA',
                            f_idview_cartella (id_cartella_collegata),
                            'M',
                            p_utente,
                            f_trasla_ruolo (p_utente, 'GDMWEB', 'GDMWEB'),
                            TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                           ) competenze,
                        id_cartella_collegata oggetto,
                        TO_CHAR (NULL) ricercamodulistica
                   FROM documenti d,
                        collegamenti coll,
                        cartelle c,
                        cartelle c2,
                        tipi_documento td,
                        icone ic
                  WHERE coll.id_cartella = p_oggetto
                    AND c.id_cartella = p_oggetto
                    AND c2.id_cartella = id_cartella_collegata
                    AND d.id_documento = c2.id_documento_profilo
                    AND d.id_tipodoc = td.id_tipodoc
                    AND td.icona = ic.icona(+)
                    AND NVL (c2.stato, 'BO') <> 'CA')
          WHERE ((gdm_competenza.gdm_verifica ('VIEW_CARTELLA',
                                               f_idview_cartella (oggetto),
                                               'L',
                                               p_utente,
                                               f_trasla_ruolo (p_utente,
                                                               'GDMWEB',
                                                               'GDMWEB'
                                                              ),
                                               TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                              ) = 1
                 )
                )
         UNION ALL
         SELECT ordina, data_aggiornamento, dataagg, utenteagg, id_tipodoc,
                cm, area, cr, ordinaquery, icona, icnome, modificata,
                id_cart_prov, id_oggetto, profilo, nome, nome as blocco, collegamento,
                nomeupper, tipo_oggetto, tipo, stato, nomeoggettoattuale,
                modifica, elimina, competenze, oggetto,
                ricercamodulistica --,NLSSORT (nomeupper, 'NLS_SORT = ASCII7')
           FROM (SELECT /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */
                        3 ordina, q.data_aggiornamento,
                        TO_CHAR (q.data_aggiornamento, 'dd/mm/yyyy') dataagg,
                        f_nominativo_utente (q.utente_aggiornamento)
                                                                    utenteagg,
                        TO_NUMBER (NULL) id_tipodoc, TO_CHAR (NULL) cm,
                        '' area, '' cr, 0 ordinaquery, td.icona,
                        ic.nome icnome, ic.modificata,
                        NVL (l.id_cartella, '') id_cart_prov,
                        id_oggetto id_oggetto, q.id_documento_profilo profilo,
                        f_link (id_oggetto, tipo_oggetto) nome,
                        TO_CHAR (NULL) collegamento,
                        UPPER (f_link (id_oggetto, tipo_oggetto))
                                                                 AS nomeupper,
                        'Q' tipo_oggetto, '' tipo, '-' stato,
                        '<b>' || c.nome || '</b>' nomeoggettoattuale,
                        gdm_competenza.gdm_verifica
                                         ('QUERY',
                                          id_oggetto,
                                          'U',
                                          p_utente,
                                          f_trasla_ruolo (p_utente,
                                                          'GDMWEB',
                                                          'GDMWEB'
                                                         ),
                                          TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                         ) modifica,
                        gdm_competenza.gdm_verifica
                                          ('QUERY',
                                           id_oggetto,
                                           'D',
                                           p_utente,
                                           f_trasla_ruolo (p_utente,
                                                           'GDMWEB',
                                                           'GDMWEB'
                                                          ),
                                           TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                          ) elimina,
                        gdm_competenza.gdm_verifica
                                       ('QUERY',
                                        id_oggetto,
                                        'M',
                                        p_utente,
                                        f_trasla_ruolo (p_utente,
                                                        'GDMWEB',
                                                        'GDMWEB'
                                                       ),
                                        TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                       ) competenze,
                        id_oggetto oggetto,
                        DECODE
                           (INSTR (q.filtro, 'RICERCAMODULISTICA_'),
                            0, '',
                            SUBSTR (q.filtro,
                                    LENGTH ('RICERCAMODULISTICA_') + 1,
                                    LENGTH (q.filtro)
                                   )
                           ) ricercamodulistica
                   FROM links l,
                        cartelle c,
                        QUERY q,
                        tipi_documento td,
                        documenti d,
                        icone ic
                  WHERE l.id_cartella = p_oggetto
                    AND c.id_cartella = p_oggetto
                    AND tipo_oggetto = 'Q'
                    AND id_oggetto = q.id_query
                    AND d.id_tipodoc = td.id_tipodoc
                    AND td.icona = ic.icona(+)
                    AND d.id_documento = q.id_documento_profilo
                    AND l.ordinamento > ' ')
          WHERE ((gdm_competenza.gdm_verifica ('QUERY',
                                               oggetto,
                                               'L',
                                               p_utente,
                                               f_trasla_ruolo (p_utente,
                                                               'GDMWEB',
                                                               'GDMWEB'
                                                              ),
                                               TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                              ) = 1
                 )
                )
         UNION ALL
         SELECT ordina, data_aggiornamento, dataagg, utenteagg, id_tipodoc,
                cm, area, cr, ordinaquery, icona, icnome, modificata,
                id_cart_prov, id_oggetto, profilo, nome, nome as blocco, collegamento,
                nomeupper, tipo_oggetto, tipo, stato, nomeoggettoattuale,
                modifica, elimina, competenze, oggetto, ricercamodulistica--,NLSSORT (nomeupper, 'NLS_SORT = ASCII7')
           FROM (SELECT ordina, data_aggiornamento, dataagg, utenteagg,
                        id_tipodoc, cm, area, cr, ordinaquery, icona, icnome,
                        modificata, id_cart_prov, id_oggetto, profilo, nome,
                        collegamento, nomeupper, tipo_oggetto, tipo, stato,
                        nomeoggettoattuale, modifica, elimina, competenze,
                        oggetto, ricercamodulistica,
                        NLSSORT (nomeupper, 'NLS_SORT = ASCII7')
                   FROM (SELECT /*+ INDEX(l ORDINAMENTO_IK) USE_NL(td)  */
                                4 ordina, d.data_aggiornamento,
                                TO_CHAR (d.data_aggiornamento,
                                         'dd/mm/yyyy'
                                        ) dataagg,
                                f_nominativo_utente
                                            (d.utente_aggiornamento)
                                                                    utenteagg,
                                d.id_tipodoc id_tipodoc, td.nome cm,
                                d.area area, d.codice_richiesta cr,
                                0 ordinaquery, td.icona, ic.nome icnome,
                                ic.modificata, l.id_cartella id_cart_prov,
                                id_oggetto id_oggetto, d.id_documento profilo,
                                d.stato_documento nome,
                                TO_CHAR (NULL) collegamento,
                                d.stato_documento AS nomeupper,
                                'D' tipo_oggetto, '' tipo, '-' stato,
                                '' nomeoggettoattuale,
                                gdm_competenza.gdm_verifica
                                         ('DOCUMENTI',
                                          id_oggetto,
                                          'U',
                                          p_utente,
                                          f_trasla_ruolo (p_utente,
                                                          'GDMWEB',
                                                          'GDMWEB'
                                                         ),
                                          TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                         ) modifica,
                                gdm_competenza.gdm_verifica
                                          ('DOCUMENTI',
                                           id_oggetto,
                                           'D',
                                           p_utente,
                                           f_trasla_ruolo (p_utente,
                                                           'GDMWEB',
                                                           'GDMWEB'
                                                          ),
                                           TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                          ) elimina,
                                gdm_competenza.gdm_verifica
                                       ('DOCUMENTI',
                                        id_oggetto,
                                        'M',
                                        p_utente,
                                        f_trasla_ruolo (p_utente,
                                                        'GDMWEB',
                                                        'GDMWEB'
                                                       ),
                                        TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                       ) competenze,
                                d.id_documento oggetto,
                                TO_CHAR (NULL) ricercamodulistica
                           FROM links l,
                                cartelle c,
                                documenti d,
                                icone ic,
                                tipi_documento td
                          WHERE l.id_cartella = p_oggetto
                            AND c.id_cartella = p_oggetto
                            AND tipo_oggetto = 'D'
                            AND d.id_tipodoc = td.id_tipodoc
                            AND td.icona = ic.icona(+)
                            AND d.id_documento = id_oggetto
                            AND d.stato_documento NOT IN ('CA', 'RE', 'PB')
                            AND l.ordinamento > ' ')
                 UNION ALL
                 SELECT TO_NUMBER (NULL), TO_DATE (NULL), TO_CHAR (NULL),
                        TO_CHAR (NULL), TO_NUMBER (NULL), TO_CHAR (NULL),
                        TO_CHAR (NULL), TO_CHAR (NULL), TO_NUMBER (NULL),
                        TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),
                        TO_NUMBER (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL),
                        TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),
                        TO_CHAR (NULL), TO_CHAR (NULL), TO_CHAR (NULL),
                        TO_CHAR (NULL), TO_NUMBER (NULL), TO_NUMBER (NULL),
                        TO_NUMBER (NULL), TO_NUMBER (NULL), TO_CHAR (NULL),
                        NLSSORT (dummy, 'NLS_SORT = ASCII7')
                   FROM DUAL),
                DUAL
          WHERE ((   gdm_competenza.gdm_verifica ('DOCUMENTI',
                                                  oggetto,
                                                  'L',
                                                  p_utente,
                                                  f_trasla_ruolo (p_utente,
                                                                  'GDMWEB',
                                                                  'GDMWEB'
                                                                 ),
                                                  TO_CHAR (SYSDATE,
                                                           'dd/mm/yyyy'
                                                          )
                                                 )
                  || dummy = '1X'
                 )
                );
      RETURN d_result;
   END;
   procedure set_password_utente
    /******************************************************************************
     NOME:        set_password_utente
     DESCRIZIONE: Esegue modifica della password dell'utente.
     PARAMETRI:   p_new_password  varchar2
                  p_old_password  varchar2
     ECCEZIONI:   -
     ANNOTAZIONI: L'utente da modificare è quello attualmente identificato sul
                  package SI4.
     REVISIONI:
     Rev. Data       Autore Descrizione
     ---- ---------- ------ ------------------------------------------------------
     1    20/08/2009 MF     Prima emissione.
    ******************************************************************************/
   (    p_new_password  VARCHAR2
       , p_old_password  VARCHAR2
   ) IS
      d_utente VARCHAR2(40) := SI4.UTENTE;
   BEGIN
      if ( d_utente is null ) then
          RAISE_APPLICATION_ERROR(-20901,'Utente non identificato');
      end if;
      AD4_UTENTE.INITIALIZE(d_utente);
      AD4_UTENTE.SET_PASSWORD( p_new_password, p_old_password );
      AD4_UTENTE.SET_DATA_PASSWORD(TO_CHAR(SYSDATE,'dd/mm/yyyy'));
      AD4_UTENTE.SET_UTENTE_AGG(d_utente);
      AD4_utente.UPDATE_UTENTE(p_modifica_sogg => 'N');
   END set_password_utente;
  PROCEDURE P_APPEND_FILTRO_QUERY(P_AREA IN VARCHAR2,P_CODICE_MODELLO IN VARCHAR2,P_SEQUENZA IN VARCHAR2)
   AS
   BEGIN
     --IF P_SEQUENZA IS NOT NULL THEN
      BEGIN
       UPDATE QUERY SET
        FILTRO = 'RICERCAMODULISTICA_'||P_AREA||'@'||P_CODICE_MODELLO||P_SEQUENZA
       WHERE FILTRO like 'RICERCAMODULISTICA_'||P_AREA||'@'||P_CODICE_MODELLO||'%';
      EXCEPTION
        WHEN OTHERS THEN
          ROLLBACK;
           RAISE_APPLICATION_ERROR(-20998,'IMPOSSIBILE AGGIORNARE IL FILTRO (AREA,CM,SEQUENZA):('||P_AREA||','||P_CODICE_MODELLO||','||P_SEQUENZA||') '||SQLERRM);
      END;
     --END IF;
     EXCEPTION WHEN OTHERS THEN
      RAISE_APPLICATION_ERROR(-20999,SQLERRM);
    END P_APPEND_FILTRO_QUERY;
  PROCEDURE P_INSERT_COLLEGAMENTO_ESTERNO(P_ID_CARTELLA IN NUMBER, P_NOME IN VARCHAR2, P_URL IN VARCHAR2, P_ICONA IN VARCHAR2,
                                          P_TOOLTIP IN VARCHAR2, P_TIPO_LINK IN VARCHAR2, P_UTENTE IN VARCHAR2)
  AS
  BEGIN
   DECLARE
       P_ID_COLLEGAMENTO NUMBER;
       P_TIPO_OGGETTO VARCHAR2(1);
   BEGIN
       P_TIPO_OGGETTO:='L';
       BEGIN
           BEGIN
             SELECT COES_SQ.NEXTVAL INTO P_ID_COLLEGAMENTO FROM DUAL;
           EXCEPTION WHEN OTHERS THEN
            raise_application_error (-20988,'IMPOSSIBILE RECUPERARE ID DALLA SEQUENCE PER INSERIRE IL COLLEGAMENTO.'||SQLERRM);
           END;
           --dbms_output.put_line('P_ID_COLLEGAMENTO= '||P_ID_COLLEGAMENTO);
           BEGIN
               INSERT INTO COLLEGAMENTI_ESTERNI
                  (ID_COLLEGAMENTO,   NOME,   URL,   ICONA,   TOOLTIP,   TIPO_LINK,   DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
               VALUES (P_ID_COLLEGAMENTO, P_NOME, P_URL, P_ICONA, P_TOOLTIP, P_TIPO_LINK, SYSDATE, P_UTENTE );
           EXCEPTION WHEN OTHERS THEN
               raise_application_error (-20989,'IMPOSSIBILE INSERIRE IL COLLEGAMENTO ESTERNO(NOME,ID_COLLEGAMENTO,P_URL,P_ICONA,P_TOOLTIP,P_UTENTE):('||P_NOME||','||P_ID_COLLEGAMENTO||','||P_URL||','||P_ICONA||','||P_TOOLTIP||','||P_UTENTE||')'||SQLERRM);
           END;
           --dbms_output.put_line('dopo insert collegamenti='||P_ID_COLLEGAMENTO);
           BEGIN
                INSERT INTO LINKS
                   (ID_LINK, ID_CARTELLA, ID_OGGETTO, TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
                VALUES ( LINK_SQ.NEXTVAL , P_ID_CARTELLA , P_ID_COLLEGAMENTO, P_TIPO_OGGETTO, SYSDATE, P_UTENTE );
           EXCEPTION WHEN OTHERS THEN
               raise_application_error (-20990,'IMPOSSIBILE INSERIRE IL COLLEGAMENTO ESTERNO NELLA CARTELLA SELEZIONATA(ID_CARTELLA,ID_OGGETTO):('||P_ID_CARTELLA||','||P_ID_COLLEGAMENTO||')'||SQLERRM);
           END;
           --dbms_output.put_line('dopo insert link='||P_ID_COLLEGAMENTO);
           COMMIT;
        EXCEPTION
         WHEN OTHERS THEN
          ROLLBACK;
          RAISE;
       END;
   END;
  END P_INSERT_COLLEGAMENTO_ESTERNO;
   FUNCTION F_GET_URL_OGGETTO(P_SERVER_URL IN VARCHAR2,P_CONTEXT_PATH IN VARCHAR2,P_ID_OGGETTO IN VARCHAR2,
                            P_TIPO_OGGETTO IN VARCHAR2,P_AREA IN VARCHAR2,P_CM IN VARCHAR2,
                            P_CR IN VARCHAR2,P_RW IN VARCHAR2,P_ID_CARTPROVENIENZA IN VARCHAR2,
                            P_ID_QUERYPROVENIENZA IN VARCHAR2,P_TAG IN VARCHAR2 DEFAULT '1',
                            P_JAVASCRIPT IN VARCHAR2 DEFAULT 'S')
  RETURN VARCHAR2
 IS
   URL           VARCHAR2(4000):='';
   QUERY_STRING  VARCHAR2(4000):='';
   TERNA         VARCHAR2(200);
   AREA          DOCUMENTI.AREA%TYPE;
   CM            TIPI_DOCUMENTO.NOME%TYPE;
   CR            DOCUMENTI.CODICE_RICHIESTA%TYPE;
   ID_DOC        DOCUMENTI.ID_DOCUMENTO%TYPE;
   ID_OGGETTO    CARTELLE.ID_CARTELLA%TYPE;
   STATO         DOCUMENTI.STATO_DOCUMENTO%TYPE;
   P_JDMS_LINK   PARAMETRI.VALORE%TYPE;
   VTERNA        STRING_ARRAY;
   PROVENIENZA   VARCHAR2(1);
 BEGIN
   BEGIN
   ID_OGGETTO := P_ID_OGGETTO;
    -- RECUPERO ID_DOCUMENTO O PROFILO
    IF LENGTH(NVL(ID_OGGETTO,'')) > 0  THEN
     BEGIN
      IF P_TIPO_OGGETTO = 'C' THEN
        BEGIN
         SELECT F_IDDOC_FROM_CARTELLA(ID_OGGETTO)
         INTO ID_DOC
         FROM DUAL;
         IF ID_DOC = -1 THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore in F_IDDOC_FROM_CARTELLA. PROFILO NON TROVATO (P_ID_OGGETTO)::('||ID_OGGETTO||')');
         END IF;
        END;
      ELSE
        IF P_TIPO_OGGETTO = 'Q' THEN
          BEGIN
           SELECT F_IDDOC_FROM_QUERY(ID_OGGETTO)
           INTO ID_DOC
           FROM DUAL;
           IF ID_DOC = -1 THEN
              RAISE_APPLICATION_ERROR(-20998,'Errore in F_IDDOC_FROM_QUERY. PROFILO NON TROVATO (P_ID_OGGETTO)::('||ID_OGGETTO||')');
           END IF;
          END;
        ELSE
         ID_DOC:= ID_OGGETTO;
        END IF;
      END IF;
     END;
    END IF;
    -- RECUPERO TERNA CM@AREA@CR
    IF LENGTH(NVL(ID_OGGETTO,'')) > 0  THEN
      BEGIN
       SELECT F_CM_AREA_CR_FROM_IDDOC(ID_DOC)
       INTO TERNA
       FROM DUAL;
      EXCEPTION WHEN NO_DATA_FOUND THEN
          RAISE_APPLICATION_ERROR(-20997,'Errore in F_CM_AREA_CR_FROM_IDDOC. TERNA NON TROVATA (ID_DOC)::('||ID_DOC||')');
      END;
    ELSE
      IF LENGTH(NVL(P_CR,'')) > 0  THEN
      -- RECUPERO ID_OGGETTO
      BEGIN
        SELECT F_IDDOC_FROM_CM_AREA_CR(P_CM,P_AREA,P_CR)
        INTO ID_DOC
        FROM DUAL;
        IF ID_DOC = -1 THEN
          RAISE_APPLICATION_ERROR(-20996,'Errore in F_IDDOC_FROM_CM_AREA_CR ID_DOCUMENTO NON TROVATO (P_CM,P_AREA,P_CR)::('||P_CM||','||P_AREA||','||P_CR||')');
        END IF;
        -- RECUPERO ID_QUERY O ID_CARTELLA
        BEGIN
         IF P_TIPO_OGGETTO = 'Q' THEN
           SELECT ID_OGGETTO
           INTO ID_OGGETTO
           FROM QUERY WHERE ID_DOCUMENTO_PROFILO = ID_DOC;
         ELSE
           SELECT ID_CARTELLA
           INTO ID_OGGETTO
           FROM CARTELLE
           WHERE ID_DOCUMENTO_PROFILO = ID_DOC;
         END IF;
        END;
       END;
       END IF;
     END IF;
   -- RECUPERO STATO DEL DOCUEMNTO
    BEGIN
      SELECT F_STATO_DOCUMENTO(ID_DOC)
      INTO STATO
      FROM DUAL;
    END;
    IF LENGTH(NVL(TERNA,'')) > 0  THEN
       VTERNA := SPLIT_STRING(TERNA,'@');
       CM:= VTERNA(1);
       AREA:= VTERNA(2);
       CR:= VTERNA(3);
    ELSE
       CM:=P_CM;
       AREA:=P_AREA;
       CR:=P_CR;
    END IF;
    -- RECUPRO PROVENIENZA
    IF  NVL(P_ID_QUERYPROVENIENZA,'-1') = '-1' THEN
     PROVENIENZA := 'C';
    ELSE
     PROVENIENZA := 'Q';
    END IF;
    -- RECUPERO PARAMETRO JDMS_LINK
    BEGIN
      SELECT NVL(F_VALORE_PARAMETRI('JDMS_LINK@DMSERVER@'),'N')
      INTO P_JDMS_LINK
      FROM DUAL;
    END;
    IF P_JDMS_LINK = 'S' AND LENGTH(NVL(ID_DOC,'')) > 0 THEN
      IF P_JAVASCRIPT = 'N' THEN
       BEGIN
         SELECT J.URL
         INTO URL
         FROM DOCUMENTI D, JDMS_LINK J
         WHERE D.ID_DOCUMENTO = ID_DOC
         AND D.ID_TIPODOC = J.ID_TIPODOC
         AND J.TAG = '-'||P_TAG;
         EXCEPTION WHEN NO_DATA_FOUND THEN
          BEGIN
           URL:='';
          END;
       END;
      END IF;
      IF NVL(LENGTH(URL),0) = 0 THEN
        BEGIN
          SELECT J.URL
          INTO URL
          FROM DOCUMENTI D, JDMS_LINK J
          WHERE D.ID_DOCUMENTO = ID_DOC
          AND D.ID_TIPODOC = J.ID_TIPODOC
          AND J.TAG = P_TAG;
          EXCEPTION WHEN NO_DATA_FOUND THEN
              BEGIN
             -- COSTRUZIONE URL ASSOLUTO
             IF LENGTH(NVL(P_SERVER_URL,'')) > 0 AND LENGTH(NVL(P_CONTEXT_PATH,'')) > 0 THEN
              URL := P_SERVER_URL||'/'||P_CONTEXT_PATH;
             ELSE
              URL:='../jdms/common/';
             END IF;
             QUERY_STRING := 'idDoc='||ID_OGGETTO||'&rw='||P_RW||'&cm='||+CM||'&area='||AREA||'&cr='||CR
                            ||'&idCartProveninez='||P_ID_CARTPROVENIENZA||'&idQueryProveninez='||P_ID_QUERYPROVENIENZA
                            ||'&Provenienza='||PROVENIENZA||'&stato='||NVL(STATO,'BO')||'&MVPG=ServletModulisticaDocumento'
                            ||'&GDC_Link=..%2Fcommon%2FClosePageAndRefresh.do%3FidQueryProveninez%3D'||P_ID_QUERYPROVENIENZA;
             URL:= URL || 'DocumentoView.do?'||QUERY_STRING;
             IF P_JAVASCRIPT = 'S' THEN
               URL := 'var wd=window.open('''||URL||''', '''',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 0,copyhistory= 0,modal=yes'');resizeFullScreen(wd,0,100);';
             END IF;
            END;
             --RAISE_APPLICATION_ERROR(-20995,'Errore in recupero del relativo link (ID_DOC,TAG)::('||ID_DOC||','||P_TAG||')');
        END;
      END IF;
      URL := REPLACE(URL,':idOggetto',ID_OGGETTO);
      URL := REPLACE(URL,':tipoOggetto',P_TIPO_OGGETTO);
      URL := REPLACE(URL,':area',AREA);
      URL := REPLACE(URL,':cm',CM);
      URL := REPLACE(URL,':cr',CR);
      URL := REPLACE(URL,':profilo',ID_DOC);
      URL := REPLACE(URL,':idCartProvenienza',P_ID_CARTPROVENIENZA);
      URL := REPLACE(URL,':idQueryProvenienza',P_ID_QUERYPROVENIENZA);
      URL := REPLACE(URL,':rw',P_RW);
      URL := REPLACE(URL,':MVPG','ServletModulisticaDocumento');
      URL := REPLACE(URL,':stato',NVL(STATO,'BO'));
      URL := REPLACE(URL,':Provenienza',PROVENIENZA);
      URL := REPLACE(URL,':GDC_Link','..%2Fcommon%2FClosePageAndRefresh.do%3FidQueryProveninez%3D'||P_ID_QUERYPROVENIENZA);
      URL := gdm_binding(URL,ID_DOC);
    ELSE
     BEGIN
      -- COSTRUZIONE URL ASSOLUTO
      IF LENGTH(NVL(P_SERVER_URL,'')) > 0 AND LENGTH(NVL(P_CONTEXT_PATH,'')) > 0 THEN
       URL := P_SERVER_URL||'/'||P_CONTEXT_PATH;
      ELSE
       URL:='../jdms/common/';
      END IF;
      QUERY_STRING := 'idDoc='||ID_OGGETTO||'&rw='||P_RW||'&cm='||+CM||'&area='||AREA||'&cr='||CR
                     ||'&idCartProveninez='||P_ID_CARTPROVENIENZA||'&idQueryProveninez='||P_ID_QUERYPROVENIENZA
                     ||'&Provenienza='||PROVENIENZA||'&stato='||NVL(STATO,'BO')||'&MVPG=ServletModulisticaDocumento'
                     ||'&GDC_Link=..%2Fcommon%2FClosePageAndRefresh.do%3FidQueryProveninez%3D'||P_ID_QUERYPROVENIENZA;
      URL:= URL || 'DocumentoView.do?'||QUERY_STRING;
      IF P_JAVASCRIPT = 'S' THEN
        URL := 'var wd=window.open('''||URL||''', '''',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 0,copyhistory= 0,modal=yes'');resizeFullScreen(wd,0,100);';
      END IF;
     END;
    END IF;
   END;
   RETURN URL;
   EXCEPTION
     WHEN OTHERS THEN
       RAISE;
   END;
  FUNCTION SPLIT_STRING (STR IN VARCHAR2, DELIMETER IN CHAR)
     RETURN STRING_ARRAY
  IS
     RESULT         STRING_ARRAY := STRING_ARRAY ();
     SPLIT_STR      LONG         DEFAULT STR || DELIMETER;
     i              NUMBER;
  BEGIN
     LOOP
        i := INSTR (SPLIT_STR, DELIMETER);
        EXIT WHEN NVL (i, 0) = 0;
        RESULT.EXTEND;
        RESULT (RESULT.COUNT) := TRIM (SUBSTR (SPLIT_STR, 1, i - 1));
        SPLIT_STR := SUBSTR (SPLIT_STR, i + LENGTH (DELIMETER));
     END LOOP;
     RETURN RESULT;
  END SPLIT_STRING;
  END;
/


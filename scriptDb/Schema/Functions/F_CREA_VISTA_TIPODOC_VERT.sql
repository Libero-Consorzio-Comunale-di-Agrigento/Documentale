CREATE OR REPLACE FUNCTION f_crea_vista_tipodoc_vert (
   a_area     VARCHAR2,
   a_codmod   VARCHAR2,
   a_nome     VARCHAR2 DEFAULT NULL
)
   RETURN VARCHAR2
IS
/* VERSIONE 1.1 MODIFICATA IL 30/12/2004 DA ANDREA AGGIUNTO CONTROLLO FLAG_VIEW = 'S'  SUI CAMPI */
/* VERSIONE 1.2 MODIFICATA IL 01/02/2005 DA ANDREA ALLARGATA LA DIMENSIONE DELLE VARIABILI */
/* VERSIONE 1.3 MODIFICATA IL 09/02/2005 DA ANDREA SETTANDO IL NOME CON IL CODICE TIPO_DOCUMENTO*/
/* VERSIONE 1.5 MODIFICATA IL 31/03/2005 DA ANDREA IL NOME DELLA VIEW E PASSATO COME PARAMETRO */
/* VERSIONE 1.6 MODIFICATA IL 04/05/2005 DA ANDREA MODIFICATO IL COMMENTO ALLA VISTA */
/* VERSIONE 1.7 MODIFICATA IL 23/11/2005 DA MARIKA AGGIUNTE VIRGOLETTE PER COLONNE CON '$' E CONTROLLO IN_USO!='N'*/
/* VERSIONE 1.8 MODIFICATA IL 23/11/2005 DA MARIKA AGGIUNTA MODIFICA PER DATI DI AREE DIVERSE */
/* VERSIONE 1.9 MODIFICATA IL 27/03/2006 DA MARIKA AGGIUNTO VALORE DI RITORNO 3 PER CAMPI PROTETTI */
/*RITORNA 1 SE VA TUTTO BENE
        0 SE IL NOME E TROPPO LUNGO
        2 SE IL NOME E GIA IN USO
        3 SE UN CAMPO HA LO STESSO NOME DI UNA DELLE COLONNE STANDARD*/
   s_vista        VARCHAR2 (32767);
   s_colonne      VARCHAR2 (32767);
   s_corpo        VARCHAR2 (32767);
   s_tipodoc      VARCHAR2 (20);
   s_grantable    VARCHAR2 (5);
   n_alias_view   NUMBER (1)       := 0;
   CURSOR c_1
   IS
      SELECT   c.nome campo, c.id_campo,
               NVL (TO_CHAR (m.id_tipodoc), '@') tipodoc
          FROM modelli m, campi_documento c, dati_modello dm, dati d
         WHERE m.id_tipodoc = c.id_tipodoc
           AND m.area = a_area
           AND m.codice_modello = a_codmod
           AND c.flag_view = 'S'
           AND dm.area = a_area
           AND dm.codice_modello = a_codmod
           AND dm.dato = c.nome
           AND dm.area_dato = d.area
           AND dm.dato = d.dato
           AND NVL (dm.in_uso, 'Y') = 'Y'
      ORDER BY c.id_campo ASC;
BEGIN
   BEGIN
      IF (a_nome IS NOT NULL)
      THEN
         IF (LENGTH (a_nome) > 26)
         THEN
            raise_application_error
                                  (-20997,
                                   'Errore creazione view: nome troppo lungo'
                                  );
            RETURN '0';                                    --NOME TROPPO LUNGO
         ELSE
            BEGIN
               SELECT 2
                 INTO n_alias_view
                 FROM tipi_documento td
                WHERE td.alias_view = a_nome
                  AND a_nome IS NOT NULL
                  AND td.id_tipodoc <>
                          (SELECT id_tipodoc
                             FROM modelli
                            WHERE area = a_area AND codice_modello = a_codmod);
               IF n_alias_view = 2
               THEN
                  raise_application_error
                                    (-20998,
                                     'Errore creazione view: nome gi¿ in uso'
                                    );
                  RETURN '2';
               END IF;
            EXCEPTION
               WHEN OTHERS
               THEN
                  NULL;
            END;
            BEGIN
               FOR c1 IN c_1
               LOOP
                  BEGIN
                     IF c1.campo IN
                           ('AREA',
                            'STATO',
                            'CODICE_MODELLO',
                            'CODICE_RICHIESTA',
                            'DOCUMENTO'
                           )
                     THEN
                        raise_application_error
                              (-20999,
                               'Errore creazione view: nome campo non valido'
                              );
                        RETURN '3';                  --CAMPO CON NOME PROTETTO
                     END IF;
                  END;
               END LOOP;
            END;
         END IF;
      END IF;
   END;
   BEGIN
      FOR c1 IN c_1
      LOOP
         BEGIN
            IF (SUBSTR (c1.campo, 1, 1) = '$')
            THEN
               c1.campo := '"' || c1.campo || '"';
            END IF;
            s_tipodoc := c1.tipodoc;
            s_colonne := s_colonne || REPLACE (c1.campo, ' ', '_') || ',';
         END;
      END LOOP;
      IF (s_tipodoc = '@')
      THEN
         RETURN '@';
      END IF;
      s_colonne :=
            '(DOCUMENTO,CODICE_RICHIESTA, AREA, CODICE_MODELLO,STATO,'
         || RTRIM (s_colonne, ',')
         || ')';
      s_colonne :=
            s_colonne
         || ' AS SELECT D.ID_DOCUMENTO,D.CODICE_RICHIESTA, D.AREA,'''
         || a_codmod
         || ''',F_STATO_DOCUMENTO(D.ID_DOCUMENTO),';
      s_vista :=
          'CREATE OR REPLACE VIEW GDM_' || NVL (a_nome, s_tipodoc)
          || s_colonne;
      FOR c1 IN c_1
      LOOP
         BEGIN
            s_corpo :=
                 s_corpo || 'F_VALORE(D.ID_DOCUMENTO,' || c1.id_campo || '),';
         END;
      END LOOP;
      s_corpo :=
            RTRIM (s_corpo, ',')
         || ' FROM DOCUMENTI D WHERE D.ID_TIPODOC = '
         || s_tipodoc;
      s_vista := s_vista || s_corpo;
      si4.sql_execute (s_vista);
   EXCEPTION
      WHEN OTHERS
      THEN
         RETURN    'Errore creazione view Area: '
                || a_area
                || ' Modello: '
                || a_codmod
                || ' Errore: '
                || SQLERRM;
   END;
   BEGIN
      s_vista :=
            'COMMENT ON TABLE GDM_'
         || NVL (a_nome, s_tipodoc)
         || ' IS '
         || '''Area: '
         || a_area
         || ' - Modello: '
         || a_codmod
         || '''';
      si4.sql_execute (s_vista);
   EXCEPTION
      WHEN OTHERS
      THEN
         NULL;
   END;
   RETURN '1';
END f_crea_vista_tipodoc_vert;
/


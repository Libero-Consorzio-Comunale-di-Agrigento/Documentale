CREATE OR REPLACE FUNCTION f_crea_vista_tipodoc_oriz (
   a_area     VARCHAR2,
   a_codmod   VARCHAR2,
   a_nome     VARCHAR2 DEFAULT NULL
)
   RETURN VARCHAR2
IS
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
      SELECT   dm.dato campo, NVL (TO_CHAR (m.id_tipodoc), '@') tipodoc,
               d.tipo, d.formato_data
          FROM modelli m, dati_modello dm, dati d
         WHERE m.area = a_area
           AND m.codice_modello = a_codmod
           AND dm.area = m.area
           AND dm.codice_modello = m.codice_modello
           AND dm.area_dato = d.area
           AND dm.dato = d.dato
           AND NVL (dm.in_uso, 'Y') = 'Y'
      ORDER BY dm.id_campo ASC;
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
                                     'Errore creazione view: nome già in uso'
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
         || ''',D.STATO_DOCUMENTO,';
      s_vista :=
          'CREATE OR REPLACE VIEW GDM_' || NVL (a_nome, s_tipodoc)
          || s_colonne;

      FOR c1 IN c_1
      LOOP
         BEGIN
            IF c1.tipo = 'S'
            THEN
               s_corpo := s_corpo || ' T.' || c1.campo || ',';
            END IF;

            IF c1.tipo = 'N'
            THEN
               s_corpo := s_corpo || ' TO_CHAR(T.' || c1.campo || '),';
            END IF;

            IF c1.tipo = 'D'
            THEN
               s_corpo :=
                     s_corpo
                  || ' TO_CHAR(T.'
                  || c1.campo
                  || ','''
                  || c1.formato_data
                  || '''),';
            END IF;
         END;
      END LOOP;

      s_corpo :=
            RTRIM (s_corpo, ',')
         || ' FROM DOCUMENTI D, '
         || F_NOME_TABELLA(a_area,a_codmod)
         || ' T WHERE D.ID_DOCUMENTO = T.ID_DOCUMENTO AND D.ID_TIPODOC = '
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
END f_crea_vista_tipodoc_oriz;
/


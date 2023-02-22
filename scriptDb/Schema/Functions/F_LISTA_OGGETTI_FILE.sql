CREATE OR REPLACE FUNCTION F_LISTA_OGGETTI_FILE(P_CM IN VARCHAR2,P_AREA IN VARCHAR2,P_CR IN VARCHAR2,P_UTENTE IN VARCHAR2)
   RETURN VARCHAR2
 IS
   LISTA  VARCHAR2(32000):='';
   ID_DOC DOCUMENTI.ID_DOCUMENTO%TYPE;
   C_LETTURA NUMBER;
   C_LETTURA_ALLEGATI NUMBER;
   C_MODIFICA_ALLEGATI NUMBER;
   COMP_ALLEGATI VARCHAR2(1);
   NUM    NUMBER:= 0;
   CURSOR C_LISTA(VAR_ID_DOC DOCUMENTI.ID_DOCUMENTO%TYPE)
   IS
     SELECT ID_OGGETTO_FILE, FILENAME
     FROM OGGETTI_FILE
     WHERE ID_DOCUMENTO = VAR_ID_DOC
       AND UPPER (filename) <> 'LETTERAUNIONE.RTFHIDDEN'
       AND UPPER (filename) not like 'SU%';
  BEGIN
   BEGIN
     /** RECUPERO ID DOCUMENTO DALLA TERNA AREA@CM@CR */
     ID_DOC := F_IDDOC_FROM_CM_AREA_CR ( P_CM, P_AREA, P_CR );
     /** VERIFCO L'ESISTENZA DEL DOCUMENTO */
     BEGIN
      IF ID_DOC = -1 THEN
        raise_application_error (-20991,'IMPOSSIBILE EFFETTUARE L''OPERAZIONE. IL DOCUMENTO NON ESISTE! IDDOC:'||ID_DOC||' '||SQLERRM);
      END IF;
     END;
     /** CONTROLLO COMPETENZE DI LETTURA SUL DOCUMENTO E ALLEGATI */
     BEGIN
      SELECT gdm_competenza.gdm_verifica ('DOCUMENTI',
                                    ID_DOC,
                                    'L',
                                    P_UTENTE,
                                    f_trasla_ruolo (P_UTENTE, 'GDMWEB', 'GDMWEB'),
                                    TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                   ) lettura,
             gdm_competenza.gdm_verifica
                                       ('DOCUMENTI',
                                        ID_DOC,
                                        'LA',
                                        P_UTENTE,
                                        f_trasla_ruolo (P_UTENTE, 'GDMWEB', 'GDMWEB'),
                                        TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                       ) lettura_allegati,
             gdm_competenza.gdm_verifica
                                      ('DOCUMENTI',
                                       ID_DOC,
                                       'UA',
                                       P_UTENTE,
                                       f_trasla_ruolo (P_UTENTE, 'GDMWEB', 'GDMWEB'),
                                       TO_CHAR (SYSDATE, 'dd/mm/yyyy')
                                      ) modifica_allegati,
             td.competenze_allegati
      INTO C_LETTURA, C_LETTURA_ALLEGATI, C_MODIFICA_ALLEGATI, COMP_ALLEGATI
      FROM documenti d, tipi_documento td
      WHERE d.id_documento = ID_DOC AND d.id_tipodoc = td.id_tipodoc;
      /** SE L'UTEBNTE NON POSSIEDE LA COMPETENZA DI LETTURA AL DOCUMENTO */
      IF C_LETTURA = 0  THEN
        raise_application_error (-20991,'IMPOSSIBILE EFFETTUARE L''OPERAZIONE. L''UTENTE NON POSSIEDE LE COMPETENZE DI LETTURA AL DOCUMENTO! IDDOC:'||ID_DOC||' '||SQLERRM);
      END IF;
      /** NEL CASO IN CUI ESISTE LA LOGICA DELLA COMPETENZA DEGLI ALLEGATI SUL TIPO_DOCUMENTO
      VIENE VERIFICATO SE L'UTENTE POSSIEDE ALMENO UNA COMPETENZA DI LETTURA O MODIFICA ALLEGATI */
      IF COMP_ALLEGATI = 'S' THEN
        IF (( C_LETTURA_ALLEGATI + C_MODIFICA_ALLEGATI) = 0) THEN
          raise_application_error (-20991,'IMPOSSIBILE EFFETTUARE L''OPERAZIONE. L''UTENTE NON POSSIEDE LE COMPETENZE ALLEGATI DEL DOCUMENTO! IDDOC:'||ID_DOC||' '||SQLERRM);
        END IF;
      END IF;
      END;
      /**L'UTENTE POSSIEDE LE COMPETENZE */
      FOR cl IN C_LISTA(ID_DOC)
       LOOP
        BEGIN
         NUM := NUM +1;
         LISTA := LISTA || '<OGGETTI_FILE num='''||NUM||'''><ID>' || cl.ID_OGGETTO_FILE || '</ID><FILENAME>' || cl.FILENAME || '</FILENAME></OGGETTI_FILE>';
        END;
      END LOOP;
      IF LENGTH(LISTA)>0 THEN
       LISTA := '<ROW>'||'<DOC>'||ID_DOC||'</DOC>'|| LISTA ||'</ROW>';
      END IF;
      --dbms_output.put_line(LISTA);
    EXCEPTION
         WHEN OTHERS THEN
         RAISE;
   END;
   RETURN LISTA;
  END;
/


CREATE OR REPLACE PROCEDURE Assegna_Competenze_Mp(
  P_TIPO_OGGETTO IN VARCHAR2
, P_OGGETTO IN VARCHAR2
, P_LISTAUTENTI IN VARCHAR2
, P_LISTATIPOABILITAZIONE IN VARCHAR2
, P_AUTORE IN VARCHAR2
, P_RUOLO IN VARCHAR2 DEFAULT NULL
, P_ACCESSO IN VARCHAR2 DEFAULT 'S'
, P_DAL IN VARCHAR2 DEFAULT NULL
, P_AL IN VARCHAR2 DEFAULT NULL
, P_PROPAGA IN VARCHAR2 DEFAULT 'N'
, P_ERROR OUT VARCHAR2
)
AS
D_ERROR_1                     VARCHAR2(4000) := '';
D_ERROR_1_D               VARCHAR2(4000) := '';
D_ERROR_2                 VARCHAR2(4000) := '';
D_ERROR_2_D               VARCHAR2(4000) := '';
P_LISTATIPOABILITAZIONE_NO_C VARCHAR2(100) := '';
BEGIN
   SELECT decode(length(P_LISTATIPOABILITAZIONE),1,REPLACE(P_LISTATIPOABILITAZIONE,'C',','),REPLACE(P_LISTATIPOABILITAZIONE,'C',''))
   INTO P_LISTATIPOABILITAZIONE_NO_C
   FROM DUAL;
   Assegna_Competenze_Multiple(
                        P_TIPO_OGGETTO
                        , P_OGGETTO
                        , P_LISTAUTENTI
                        , P_LISTATIPOABILITAZIONE
                        , P_AUTORE
                        , P_RUOLO
                        , P_ACCESSO
                        , P_DAL
                        , P_AL
                        , D_ERROR_1
                        );
   --SE STO ASSEGNANDO LE COMPETENZE ALLA CARTELLA
   --ALLORA DOVRO' RIPORTARLE ANCHE SUL DOCUMENTO
   --ASSOCIATO
   IF   p_Tipo_Oggetto='VIEW_CARTELLA' THEN
       -- GLI TOLGO LA CREAZIONE SE C'E
       Assegna_Competenze_Multiple(
                        'DOCUMENTI'
                        , F_Iddoc_From_Cartella(F_Idcartella_Idview(TO_NUMBER(P_OGGETTO)))
                        , P_LISTAUTENTI
                        , P_LISTATIPOABILITAZIONE_NO_C
                        , P_AUTORE
                        , P_RUOLO
                        , P_ACCESSO
                        , P_DAL
                        , P_AL
                        , D_ERROR_1_D
                        );
      D_ERROR_1 := D_ERROR_1 || D_ERROR_1_D;
   END IF;
   --SE STO ASSEGNANDO LE COMPETENZE ALLA QUERY
   --ALLORA DOVRO' RIPORTARLE ANCHE SUL DOCUMENTO
   --ASSOCIATO
   IF   p_Tipo_Oggetto='QUERY' THEN
       -- GLI TOLGO LA CREAZIONE SE C'E
       Assegna_Competenze_Multiple(
                        'DOCUMENTI'
                        , F_Iddoc_From_Query(P_OGGETTO)
                        , P_LISTAUTENTI
                        , P_LISTATIPOABILITAZIONE_NO_C
                        , P_AUTORE
                        , P_RUOLO
                        , P_ACCESSO
                        , P_DAL
                        , P_AL
                        , D_ERROR_1_D
                        );
      D_ERROR_1 := D_ERROR_1 || D_ERROR_1_D;
   END IF;
   IF P_PROPAGA='Y' THEN
      FOR CUR_ALBERO IN (SELECT  TO_CHAR(DECODE(TIPO_OGGETTO,'C',F_Idview_Cartella(ID_OGGETTO),ID_OGGETTO)) IDOBJ,DECODE(TIPO_OGGETTO,'C','VIEW_CARTELLA','D','DOCUMENTI','QUERY') TIPOBJ
                     FROM LINKS
                     WHERE Gdm_Competenza.SI4_VERIFICA( DECODE(TIPO_OGGETTO,'C','VIEW_CARTELLA','D','DOCUMENTI','QUERY'),
                                                        DECODE(TIPO_OGGETTO,'C',F_Idview_Cartella(ID_OGGETTO),ID_OGGETTO),
                                                        'L',P_AUTORE,'GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy') ) = 1
                          AND TIPO_OGGETTO='C'
                     START WITH ID_CARTELLA = TO_NUMBER(F_Idcartella_Idview(TO_NUMBER(P_OGGETTO)))
                      CONNECT BY  PRIOR   ID_OGGETTO = ID_CARTELLA AND  PRIOR Tipo_oggetto =  'C')
      LOOP
          Assegna_Competenze_Multiple(
                        CUR_ALBERO.TIPOBJ
                        , CUR_ALBERO.IDOBJ
                        , P_LISTAUTENTI
                        , P_LISTATIPOABILITAZIONE
                        , P_AUTORE
                        , P_RUOLO
                        , P_ACCESSO
                        , P_DAL
                        , P_AL
                        , D_ERROR_2
                        );
         --SE STO ASSEGNANDO LE COMPETENZE ALLA CARTELLA
         --ALLORA DOVRO' RIPORTARLE ANCHE SUL DOCUMENTO
         --ASSOCIATO
         IF   p_Tipo_Oggetto='VIEW_CARTELLA' THEN
             -- GLI TOLGO LA CREAZIONE SE C'E
            Assegna_Competenze_Multiple(
                        'DOCUMENTI'
                        , F_Iddoc_From_Cartella(F_Idcartella_Idview(TO_NUMBER(CUR_ALBERO.IDOBJ)))
                        , P_LISTAUTENTI
                        , P_LISTATIPOABILITAZIONE_NO_C
                        , P_AUTORE
                        , P_RUOLO
                        , P_ACCESSO
                        , P_DAL
                        , P_AL
                        , D_ERROR_2_D
                        );
            D_ERROR_2 := D_ERROR_2 || D_ERROR_2_D;
         END IF;
      END LOOP;
   END IF;
   P_ERROR := D_ERROR_1;
END;
/


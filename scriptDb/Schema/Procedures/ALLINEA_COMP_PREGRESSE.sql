CREATE OR REPLACE PROCEDURE ALLINEA_COMP_PREGRESSE (P_TIPO_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_DAL IN VARCHAR2 DEFAULT '01/01/2004',P_RUOLO IN VARCHAR2 DEFAULT 'GDM',P_AL IN VARCHAR2 DEFAULT NULL) AS
   RETVAL NUMBER (2) := 0;
   OK_OP  VARCHAR2(1) := 'N';
BEGIN
   DECLARE
        CURSOR C_1 IS
     SELECT ID_TIPODOC OGGETTO
        FROM TIPI_DOCUMENTO
      WHERE P_TIPO_OGGETTO = 'TIPI_DOCUMENTO'
    UNION ALL
    SELECT ID_VIEWCARTELLA
        FROM VIEW_CARTELLA
      WHERE P_TIPO_OGGETTO = 'VIEW_CARTELLA'
    UNION ALL
    SELECT ID_DOCUMENTO OGGETTO
        FROM DOCUMENTI
      WHERE P_TIPO_OGGETTO = 'DOCUMENTI'
    UNION ALL
    SELECT ID_QUERY OGGETTO
        FROM QUERY
      WHERE  P_TIPO_OGGETTO = 'QUERY';
   BEGIN
        FOR C1 IN C_1  LOOP
        BEGIN
          BEGIN
         SELECT 'N'
           INTO OK_OP
          FROM LINKS
         WHERE ID_CARTELLA           = -2
           AND TIPO_OGGETTO         = 'C'
           AND P_TIPO_OGGETTO       = 'VIEW_CARTELLA'
         START WITH  ID_OGGETTO       = C1.OGGETTO
           AND TIPO_OGGETTO          = 'C'
        CONNECT BY PRIOR ID_CARTELLA = ID_OGGETTO
         UNION ALL
         SELECT 'N'
          FROM LINKS
         WHERE ID_CARTELLA          = -2
           AND TIPO_OGGETTO          = 'C'
           AND P_TIPO_OGGETTO       = 'QUERY'
         START WITH  ID_OGGETTO       = C1.OGGETTO
           AND TIPO_OGGETTO          = 'Q'
        CONNECT BY PRIOR ID_CARTELLA = ID_OGGETTO
            ;
            EXCEPTION
            WHEN NO_DATA_FOUND THEN
                OK_OP := 'S';
        END;
        IF OK_OP = 'S' THEN
          BEGIN
           BEGIN
                    RETVAL := SI4_COMPETENZA.ASSEGNA( P_TIPO_OGGETTO
                                    , C1.OGGETTO
                                    , P_TIPO_ABILITAZIONE
                                    , P_UTENTE
                                    , P_RUOLO
                                    , P_AUTORE
                                    , P_ACCESSO
                                    , P_DAL
                                    , P_AL ) ;
               END;
               IF RETVAL = -1 THEN
                    RAISE_APPLICATION_ERROR('-20999',  '-1 : Tipo di abilitazione incompatibile con l''oggetto indicato');
               ELSIF RETVAL = -2 THEN
                    RAISE_APPLICATION_ERROR('-20999',  '-2 : Non esiste l''oggetto');
               ELSIF RETVAL = -3 THEN
                    RAISE_APPLICATION_ERROR('-20999',  '-3 : Non esiste il tipo di abilitazione');
               END IF;
         END;
         END IF;
         END;
      END LOOP;
END;
END ALLINEA_COMP_PREGRESSE ;
/


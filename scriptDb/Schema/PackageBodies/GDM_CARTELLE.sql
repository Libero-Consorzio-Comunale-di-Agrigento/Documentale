CREATE OR REPLACE PACKAGE BODY GDM_CARTELLE AS

/******************************************************************************
   NOME:        CREA_CARTELLA
   DESCRIZIONE: Crea la cartella secondo area/cm/nome e la "appende" sotto alla A_ID_CARTELLA_PADRE
   RITORNA:     idCartella creata
   NOTE:        Se A_ID_CARTELLA_PADRE=0 allora verr� creata una workspace
  ******************************************************************************/
   FUNCTION CREA_CARTELLA(A_AREA IN VARCHAR2, A_MODELLO IN VARCHAR2, A_NOME_CARTELLA IN VARCHAR2, A_ID_CARTELLA_PADRE IN NUMBER, A_UTENTE IN VARCHAR2) RETURN NUMBER
   IS
   BEGIN
      RETURN CREA_CARTELLA(A_AREA, A_MODELLO , A_NOME_CARTELLA , A_ID_CARTELLA_PADRE, A_UTENTE, 0);
   END;

   /******************************************************************************
   NOME:        CREA_CARTELLA
   DESCRIZIONE: Crea la cartella secondo area/cm/nome e la "appende" sotto alla A_ID_CARTELLA_PADRE
   RITORNA:     idCartella creata
   NOTE:        Se A_ID_CARTELLA_PADRE=0 allora verr� creata una workspace
  ******************************************************************************/
   FUNCTION CREA_CARTELLA(A_AREA IN VARCHAR2, A_MODELLO IN VARCHAR2, A_NOME_CARTELLA IN VARCHAR2, A_ID_CARTELLA_PADRE IN NUMBER, A_UTENTE IN VARCHAR2, A_CREA_RECORD_ORIZZONTALE IN NUMBER) RETURN NUMBER
   IS
        A_ID_CARTELLA               CARTELLE.ID_CARTELLA%TYPE;
        A_ID_VIEW_CARTELLA          VIEW_CARTELLA.ID_VIEWCARTELLA%TYPE;
        A_ID_LINK                   LINKS.ID_LINK%TYPE;
        A_ID_CAMPONOME              CAMPI_DOCUMENTO.ID_CAMPO%TYPE;
        A_CR                        DOCUMENTI.CODICE_RICHIESTA%TYPE;
        A_ID_PROFILO                DOCUMENTI.ID_DOCUMENTO%TYPE;
        A_NOME_CARTELLA_EFFETTIVO   CARTELLE.NOME%TYPE;
        A_NOMETAB                   VARCHAR2(200);
        A_RET_CHAR                  VARCHAR2(4000);
   BEGIN
        SELECT decode(nvl(A_ID_CARTELLA_PADRE,0),0,WRKSP_SQ.NEXTVAL * -1,CART_SQ.NEXTVAL),
               VWCART_SQ.NEXTVAL,
               decode(nvl(A_ID_CARTELLA_PADRE,0),0,NULL,LINK_SQ.NEXTVAL)
          INTO A_ID_CARTELLA,A_ID_VIEW_CARTELLA,A_ID_LINK
          FROM DUAL;



        IF NVL(A_ID_CARTELLA_PADRE,0)=0 THEN
           A_CR:='WRKSP'||A_ID_CARTELLA;
        ELSE
           A_CR:=''||A_ID_CARTELLA;
        END IF;

        A_NOME_CARTELLA_EFFETTIVO := A_NOME_CARTELLA;
        IF NVL(A_NOME_CARTELLA_EFFETTIVO,' ')=' ' THEN
           A_NOME_CARTELLA_EFFETTIVO := 'Cartella '||A_ID_CARTELLA;
        ELSE
           IF LENGTH(A_NOME_CARTELLA_EFFETTIVO)>100 THEN
              A_NOME_CARTELLA_EFFETTIVO := SUBSTR(A_NOME_CARTELLA_EFFETTIVO,1,99);
           END IF;
        END IF;

        BEGIN
            SELECT F_CAMPO('NOME',MODELLI.ID_TIPODOC)
              INTO A_ID_CAMPONOME
              FROM MODELLI
             WHERE CODICE_MODELLO=A_MODELLO AND AREA=A_AREA;

            IF NVL(A_ID_CAMPONOME,0)<>0 THEN
               GDM_PROFILO.ADDCAMPO(A_AREA ,A_MODELLO , 'NOME',TO_CLOB(A_NOME_CARTELLA_EFFETTIVO));
            END IF;
        EXCEPTION WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20999,'Errore in recupero/creazione valore per campo NOME del modello. Errore: '||sqlerrm);
        END;

        BEGIN
            A_ID_PROFILO:=GDM_PROFILO.CREA_DOCUMENTO(A_AREA , A_MODELLO, A_CR, A_UTENTE , A_CREA_RECORD_ORIZZONTALE);

            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in creazione documento. Errore: '||sqlerrm);
        END;

        BEGIN
            INSERT INTO CARTELLE
            (ID_CARTELLA, NOME, TIPO,ID_DOCUMENTO_PROFILO,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
            VALUES
            (A_ID_CARTELLA,A_NOME_CARTELLA_EFFETTIVO,'U',A_ID_PROFILO,sysdate,A_UTENTE);

            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in inserimento tabella cartelle. Errore: '||sqlerrm);
        END;

        BEGIN
            INSERT INTO VIEW_CARTELLA
            (ID_VIEWCARTELLA,ID_CARTELLA,TIPO_VISUALIZZAZIONE,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
            VALUES
            (A_ID_VIEW_CARTELLA,A_ID_CARTELLA,'P',sysdate,A_UTENTE);

            EXCEPTION WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20999,'Errore in inserimento tabella view_cartella. Errore: '||sqlerrm);
        END;

        IF NVL(A_ID_CARTELLA_PADRE,0)<>0 THEN
           INSERISCI_OGGETTO(A_ID_CARTELLA_PADRE,A_ID_CARTELLA,'C',A_UTENTE);
        END IF;


        BEGIN
           --Allineo le comp. fra cartella e documento profilo
           IF NVL(A_ID_CARTELLA_PADRE,0)<>-2 THEN
              BEGIN
                GDM_COMPETENZA.GDM_ALLINEA_COMP_CQ_DOC(A_ID_VIEW_CARTELLA,A_ID_PROFILO,A_UTENTE,'C');

                EXCEPTION WHEN OTHERS THEN
                    RAISE_APPLICATION_ERROR(-20999,'Errore in allineamento competenze CARTELLA<->DOCUMENTO PROFILO. Errore: '||sqlerrm);
              END;
           END IF;
        END;

        RETURN A_ID_CARTELLA;

        EXCEPTION WHEN OTHERS THEN
                --ROLLBACK;
                RAISE_APPLICATION_ERROR(-20999,'Attenzione! Errore in creazione cartella Area='||A_AREA||' / Cm='||A_MODELLO||' / Nome='||A_NOME_CARTELLA
                                               ||'. Errore: '||sqlerrm);
   END;

   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE CLOB)
   AS
   BEGIN
      GDM_PROFILO.ADDCAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO,P_VALORE);
   END;

   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE NUMBER)
   AS
   BEGIN
      GDM_PROFILO.ADDCAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO,P_VALORE);
   END;

   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE DATE)
   AS
   BEGIN
      GDM_PROFILO.ADDCAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO,P_VALORE);
   END;

   PROCEDURE RESETCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2)
   AS
   BEGIN
      GDM_PROFILO.RESETCAMPO(P_AREA,P_MODELLO,P_NOME_CAMPO);
   END;

   PROCEDURE RESETCAMPI
   AS
   BEGIN
      GDM_PROFILO.RESETCAMPI();
   END;

   PROCEDURE INSERISCI_OGGETTO(A_CARTELLA IN NUMBER, A_OGGETTO IN NUMBER, A_TIPO_OGGETTO IN VARCHAR2, A_UTENTE IN VARCHAR2)
   AS
   A_ID_LINK    LINKS.ID_LINK%TYPE;
   BEGIN
      BEGIN
         SELECT LINK_SQ.NEXTVAL
           INTO A_ID_LINK
           FROM DUAL;

         INSERT INTO LINKS
         (ID_LINK, ID_CARTELLA, ID_OGGETTO,TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
         VALUES
         (A_ID_LINK,A_CARTELLA,A_OGGETTO,A_TIPO_OGGETTO,sysdate,A_UTENTE);

      EXCEPTION WHEN OTHERS THEN
          IF SQLCODE=-2291 THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore in creazione link con la cartella padre. La cartella padre con id = '
                                               ||A_CARTELLA|| ' � inesistente');
          ELSE
             RAISE_APPLICATION_ERROR(-20999,'Errore in creazione link con la cartella padre. Errore: '||sqlerrm);
          END IF;
      END;

      DECLARE
          A_RET NUMBER(10);
      BEGIN
          A_RET := ORDINAMENTO_PKG.GENERA_CHIAVE(A_OGGETTO,A_TIPO_OGGETTO,A_CARTELLA);

      EXCEPTION WHEN OTHERS THEN
          RAISE_APPLICATION_ERROR(-20999,'Errore in creazione chiave di ordinamento per la tabella link. Errore: '||sqlerrm);
      END;

   EXCEPTION WHEN OTHERS THEN
      --ROLLBACK;
      RAISE_APPLICATION_ERROR(-20999,'Attenzione! Errore in inserimento oggetto con id='||A_OGGETTO||' di tipo='||A_TIPO_OGGETTO||
                                     'in cartella='||A_CARTELLA||' . Errore: '||sqlerrm);
   END;
   PROCEDURE CREA_COLLEGAMENTO_DESKTOP (A_CATEGORIA IN VARCHAR2,A_CARTELLA_PADRE IN NUMBER,A_USER IN VARCHAR2) AS
   BEGIN
         INSERT INTO COLLEGAMENTI
         (ID_COLLEGAMENTO,ID_CARTELLA,ID_CARTELLA_COLLEGATA,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO,CATEGORIA_COLLEGATA)
         VALUES
         (COLL_SQ.NEXTVAL,A_CARTELLA_PADRE,NULL,SYSDATE,A_USER,A_CATEGORIA);
   end;
   PROCEDURE CREA_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER,A_USER IN VARCHAR2) AS
   BEGIN
           --VERIFICO LA LEGGE1 (NON POSSO CREARE COLLEGAMENTI NEL SOTTOALBERO DI A_CARTELLA)
         IF F_VERIFICA_LEGGE1_COLLEGAMENTO(A_CARTELLA,A_CARTELLA_PADRE)=0 THEN
              RAISE_APPLICATION_ERROR('-20999','Impossibile creare un collegamento in un sottoalbero della cartella di partenza o sulla radice dell''albero');
         END IF;
          --VERIFICO L'esistenza del collegamento (non posso creare collegamenti gi� esistenti)
         IF F_VERIFICA_COLLEGAMENTO(A_CARTELLA,A_CARTELLA_PADRE)>0 THEN
              RAISE_APPLICATION_ERROR('-20998','IMPOSSIBILE CREARE UN COLLEGAMENTO, COLLEGAMENTO GI� ESISTENTE ');
         END IF;
           INSERT INTO COLLEGAMENTI
         (ID_COLLEGAMENTO,ID_CARTELLA,ID_CARTELLA_COLLEGATA,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
         VALUES
         (COLL_SQ.NEXTVAL,A_CARTELLA_PADRE,A_CARTELLA,SYSDATE,A_USER);
   end;
   PROCEDURE ELIMINA_COLLEGAMENTO_DESKTOP (A_CATEGORIA IN VARCHAR2,A_CARTELLA_PADRE IN NUMBER) AS
   BEGIN
         DELETE COLLEGAMENTI
         WHERE ID_CARTELLA=A_CARTELLA_PADRE AND
               CATEGORIA_COLLEGATA=A_CATEGORIA;
   end;
   procedure ELIMINA_COLLEGAMENTO (A_CARTELLA in NUMBER,A_CARTELLA_PADRE in NUMBER) as
   begin
           DELETE COLLEGAMENTI
         WHERE ID_CARTELLA=A_CARTELLA_PADRE AND
                ID_CARTELLA_COLLEGATA=A_CARTELLA;
   end;
   procedure SPOSTA_COLLEGAMENTO (A_CARTELLA in NUMBER,A_CARTELLA_PADRE in NUMBER,A_NEW_CARTELLA_PADRE in NUMBER,A_USER in VARCHAR2) as
   begin
           BEGIN
               ELIMINA_COLLEGAMENTO(A_CARTELLA,A_CARTELLA_PADRE);
               EXCEPTION WHEN OTHERS THEN
                       RAISE_APPLICATION_ERROR('-20999','ERRORE IN SPOSTA COLLEGAMENTO RICHIAMANDO LA ELIMINA COLLEGAMENTO:'||sqlerrm);
         END;
         BEGIN
               CREA_COLLEGAMENTO(A_CARTELLA,A_NEW_CARTELLA_PADRE,A_USER);
               EXCEPTION WHEN OTHERS THEN
                       RAISE_APPLICATION_ERROR('-20999','ERRORE IN SPOSTA COLLEGAMENTO RICHIAMANDO LA CREA COLLEGAMENTO:'||sqlerrm);
         END;
   end;
   function F_VERIFICA_LEGGE1_COLLEGAMENTO (A_CARTELLA_ORIGINE in NUMBER,A_CARTELLA_PADRE_DESTINAZIONE in NUMBER) return NUMBER as
     A_CHECK NUMBER(1):=0;
     BEGIN
         IF A_CARTELLA_PADRE_DESTINAZIONE<0 THEN
          RETURN 0;
        END IF;
         BEGIN
            SELECT 1
          INTO A_CHECK
          FROM DUAL
          WHERE A_CARTELLA_PADRE_DESTINAZIONE NOT IN( SELECT LINKS.ID_OGGETTO AS ID_OGGETTO
            FROM LINKS
           WHERE TIPO_OGGETTO = 'C'
           START WITH  ID_OGGETTO = A_CARTELLA_ORIGINE
           CONNECT BY PRIOR    ID_OGGETTO = LINKS.ID_CARTELLA AND
           PRIOR TIPO_OGGETTO =  'C');
          EXCEPTION WHEN NO_DATA_FOUND THEN A_CHECK:=0;
        END;
       RETURN A_CHECK;
   END;
   FUNCTION F_VERIFICA_LEGGE2_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER) RETURN NUMBER AS
   BEGIN
           NULL;
   END;
   FUNCTION F_VERIFICA_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER) RETURN NUMBER AS
     A_COUNT NUMBER(1):=0;
     BEGIN
          SELECT COUNT(*)
        INTO A_COUNT
        FROM COLLEGAMENTI
        WHERE ID_CARTELLA=A_CARTELLA_PADRE AND
              ID_CARTELLA_COLLEGATA=A_CARTELLA;
      RETURN A_COUNT;
   END;
END GDM_CARTELLE;
/


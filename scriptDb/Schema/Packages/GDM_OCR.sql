CREATE OR REPLACE PACKAGE GDM_OCR AS
    FUNCTION CHECK_GEN_OCR_PARAMETRI RETURN NUMBER;
    FUNCTION CHECK_GEN_OCR_AREA(A_AREA VARCHAR2) RETURN NUMBER;
    FUNCTION CHECK_GEN_OCR_MODELLO(A_AREA VARCHAR2, A_MODELLO VARCHAR2) RETURN NUMBER;
    FUNCTION CHECK_GEN_OCR_OGFI(A_ID_OGGETTO_FILE NUMBER) RETURN NUMBER;
    FUNCTION CHECK_GEN_OCR_OGFI(A_NOME_FILE VARCHAR2, A_ID_DOC NUMBER) RETURN NUMBER;
    PROCEDURE P_GENERAZIONE_OCR_OGFI(P_ID_DOCUMENTO VARCHAR2 DEFAULT NULL, P_ID_OGGETTO_FILE VARCHAR2 DEFAULT NULL,
                                     P_FORMATO VARCHAR2 DEFAULT NULL, P_TIPO_ORD_DATA_AGG  VARCHAR2 DEFAULT NULL,
                                     P_COMMIT VARCHAR2 DEFAULT 'N',P_LIMIT VARCHAR2 DEFAULT '',
                                     P_AREA VARCHAR2 DEFAULT NULL, P_CODICE_MODELLO VARCHAR2 DEFAULT NULL );
    PROCEDURE P_SET_AREE_GENERAZIONE_OCR(P_COMMIT IN VARCHAR2 DEFAULT 'N');
    PROCEDURE P_SET_MODELLI_GENERAZIONE_OCR(P_AREA VARCHAR2,P_COMMIT IN VARCHAR2 DEFAULT 'N');
END GDM_OCR;
/


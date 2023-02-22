CREATE INDEX GDM$MTES_GDM$DESC_CTX ON GDM_T_MODELLO_TESTI
(DESCRIZIONE)
INDEXTYPE IS CTXSYS.CTXCAT
PARAMETERS('lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M');



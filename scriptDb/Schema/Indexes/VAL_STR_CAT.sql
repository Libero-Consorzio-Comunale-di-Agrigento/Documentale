CREATE INDEX VAL_STR_CAT ON VALORI
(VALORE_STRINGA)
INDEXTYPE IS CTXSYS.CTXCAT
PARAMETERS('index set valori_iset lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M');



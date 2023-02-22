CREATE INDEX VAL_CLOB_CTX ON VALORI
(VALORE_CLOB)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



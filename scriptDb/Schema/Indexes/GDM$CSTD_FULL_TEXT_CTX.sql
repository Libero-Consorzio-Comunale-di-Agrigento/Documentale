CREATE INDEX GDM$CSTD_FULL_TEXT_CTX ON GDM_C_CARTELLASTANDARD
(FULL_TEXT)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



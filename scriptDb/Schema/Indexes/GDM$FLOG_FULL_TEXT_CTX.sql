CREATE INDEX GDM$FLOG_FULL_TEXT_CTX ON GDM_T_FIRMA_LOG
(FULL_TEXT)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



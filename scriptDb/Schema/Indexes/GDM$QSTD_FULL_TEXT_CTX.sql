CREATE INDEX GDM$QSTD_FULL_TEXT_CTX ON GDM_Q_QUERYSTANDARD
(FULL_TEXT)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



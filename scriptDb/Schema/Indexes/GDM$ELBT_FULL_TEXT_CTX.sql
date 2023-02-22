CREATE INDEX GDM$ELBT_FULL_TEXT_CTX ON GDM_ELAB_BATCH
(FULL_TEXT)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



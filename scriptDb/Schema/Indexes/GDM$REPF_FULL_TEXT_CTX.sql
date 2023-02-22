CREATE INDEX GDM$REPF_FULL_TEXT_CTX ON GDM_T_REPORT_FOOTER
(FULL_TEXT)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



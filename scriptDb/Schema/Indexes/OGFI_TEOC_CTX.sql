CREATE INDEX OGFI_TEOC_CTX ON OGGETTI_FILE
(TESTOOCR)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



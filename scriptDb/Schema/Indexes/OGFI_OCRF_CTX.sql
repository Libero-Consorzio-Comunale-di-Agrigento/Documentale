CREATE INDEX OGFI_OCRF_CTX ON OGGETTI_FILE
(OCR_FILE)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
	lexer italian_lexer
    wordlist italian_wordlist
    stoplist italian_stoplist');



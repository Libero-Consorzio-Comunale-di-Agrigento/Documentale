CREATE INDEX OGFI_FILE_CTX ON OGGETTI_FILE
("FILE")
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS('filter ctxsys.null_filter
			lexer italian_lexer
				wordlist italian_wordlist
				stoplist italian_stoplist');



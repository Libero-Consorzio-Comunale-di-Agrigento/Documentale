CREATE OR REPLACE FORCE VIEW RADICI_JS
(PARENTID, NODEID, TEXT, URL, TOOLTIP, 
 CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, COMPETENZA, ID_CARTELLA)
BEQUEATH DEFINER
AS 
SELECT '0' PARENTID, 'C'||ID_CARTELLA NODEID,NOME||'</a><a href="javascript:riportaIDCartella('''||ID_CARTELLA||''','''||NOME||''')" ><img name="cartellaselezione'||ID_CARTELLA||'" border="" height="16" src="images/sendto.gif" width="16"></a>' TEXT,
	   TO_CHAR(NULL) URL,TO_CHAR(NULL)  TOOLTIP,
	   TO_CHAR(NULL) CUSTOM_IMAGE, TO_CHAR(NULL) DEFAULT_IMAGE, TO_CHAR(NULL) CUSTOM_STYLE, TIPO COMPETENZA, ID_CARTELLA  FROM CARTELLE
WHERE  ID_CARTELLA  < 0;



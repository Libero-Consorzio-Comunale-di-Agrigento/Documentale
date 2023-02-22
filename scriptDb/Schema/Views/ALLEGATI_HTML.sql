CREATE OR REPLACE FORCE VIEW ALLEGATI_HTML
(ID_DOCUMENTO, ALLEGATO)
BEQUEATH DEFINER
AS 
SELECT ID_DOCUMENTO, '<td valign="bottom"> <table border="0"><tr><td align="center" valign="bottom">'||'<img src="../common/images/formati/'||NVL(ICONA, 'generico,gif')||'" /></td> </tr>'||'<tr><td class="AFCDataTD" align="center" valign="bottom"><a href="../common/DownloadAll.do?idOgfi='||OGFI.ID_OGGETTO_FILE||'" onclick="popup(''../common/DownloadAll.do?idOgfi='||OGFI.ID_OGGETTO_FILE||''',400,400,0,50)">'||FILENAME||'</a></td></tr></table></td>' ALLEGATO
 FROM OGGETTI_FILE OGFI, FORMATI_FILE FOFI
 WHERE OGFI.ID_FORMATO = FOFI.ID_FORMATO AND FOFI.VISIBILE='S';



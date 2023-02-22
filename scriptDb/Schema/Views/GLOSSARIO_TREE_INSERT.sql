CREATE OR REPLACE FORCE VIEW GLOSSARIO_TREE_INSERT
(PARENTID, NODEID, TEXT, URL, TOOLTIP, 
 CUSTOM_IMAGE, CUSTOM_STYLE)
BEQUEATH DEFINER
AS 
SELECT
  DECODE(ID_FRASE_PADRE,0,'0',AREA || '-' || TO_CHAR(ID_FRASE_PADRE)) PARENTID,
  AREA || '-' ||TO_CHAR(ID_FRASE) NODEID,
  TITOLO TEXT,
  'javascript:inserimentoCartella('||ID_FRASE||','''||TITOLO||''','''||AREA||''')' URL,
  '' TOOLTIP,
  '' CUSTOM_IMAGE,
  '' CUSTOM_STYLE
 FROM
  FRASI_GLOSSARIO
WHERE FRASE IS NULL;



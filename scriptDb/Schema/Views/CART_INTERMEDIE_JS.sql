CREATE OR REPLACE FORCE VIEW CART_INTERMEDIE_JS
(PARENTID, NODEID, TEXT, URL, TOOLTIP, 
 CUSTOM_IMAGE, DEFAULT_IMAGE, CUSTOM_STYLE, COMPETENZA, ID_CARTELLA, 
 ID_OGGETTO)
BEQUEATH DEFINER
AS 
SELECT     'C' || id_cartella parentid, tipo_oggetto || id_oggetto nodeid,
           f_link_js (id_oggetto, tipo_oggetto) text, TO_CHAR (NULL) url,
           TO_CHAR (NULL) tooltip, TO_CHAR (NULL) custom_image,
           TO_CHAR (NULL) default_image, TO_CHAR (NULL) custom_style,
           f_tipo_competenza (id_oggetto, tipo_oggetto) competenza,
           id_cartella, id_oggetto
FROM links
WHERE tipo_oggetto = 'C'
AND exists (select 'x' from links l2
       where l2.id_cartella = links.id_oggetto)
--AND id_oggetto IN (SELECT id_cartella FROM links)
--START WITH 'C' || id_cartella IN (SELECT nodeid
start with id_cartella in (select to_number(replace(nodeid,'C',''))
                                    FROM radici
                                   WHERE nodeid IS NOT NULL)
CONNECT BY PRIOR id_oggetto = id_cartella AND PRIOR tipo_oggetto = 'C';



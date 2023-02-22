CREATE OR REPLACE FORCE VIEW GD4_TREE_JS
(PARENTID, NODEID, TEXT, URL, TOOLTIP, 
 CUSTOM_IMAGE, CUSTOM_STYLE, COMPETENZA)
BEQUEATH DEFINER
AS 
SELECT PARENTID,NODEID,TEXT,URL,TOOLTIP,CUSTOM_IMAGE,CUSTOM_STYLE, COMPETENZA FROM RADICI_JS
 UNION ALL
 SELECT PARENTID,NODEID,TEXT,URL,TOOLTIP,CUSTOM_IMAGE,CUSTOM_STYLE, COMPETENZA FROM CART_INTERMEDIE_JS
 UNION ALL
SELECT PARENTID,NODEID,TEXT,URL,TOOLTIP,CUSTOM_IMAGE,CUSTOM_STYLE, COMPETENZA FROM CART_FOGLIE_JS;


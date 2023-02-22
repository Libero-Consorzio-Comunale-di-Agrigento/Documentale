CREATE OR REPLACE FUNCTION F_SUBTAB_FOLDER
(IN_LINK     IN VARCHAR2,
 IN_ONCLICK     IN VARCHAR2,
 IN_ICONA    IN VARCHAR2,
 IN_TITLE    IN VARCHAR2 DEFAULT '',
 IN_ACTIVE   IN VARCHAR2 DEFAULT 'N'
)RETURN VARCHAR2
IS
  CLASS_BEGIN VARCHAR2(32000);
  CLASS_BODY  VARCHAR2(32000):= 'AFCGuida';
  CLASS_END   VARCHAR2(32000);
  CLASS_LINK  VARCHAR2(32000);
  IMAGE       VARCHAR2(32000):= '../Themes/Default/GuidaBlank.gif';
  ICONA       VARCHAR2(32000):= IN_ICONA;
  OUT_HTML    VARCHAR2(32000):= NULL;
BEGIN
  IF IN_ACTIVE = 'S' THEN
    CLASS_BODY  := CLASS_BODY||'Sel';
  END IF;
  CLASS_BEGIN := CLASS_BODY||'L';
  CLASS_END   := CLASS_BODY||'R';
  --CLASS_LINK  := CLASS_BODY||'Link';
  CLASS_LINK  := 'AFCGuidaLink';
  OUT_HTML := OUT_HTML||'
<table cellpadding="0" cellspacing="0" border="0"><tr>
<td align="left" valign="top" class="'||CLASS_BEGIN||'"><img src="'||IMAGE||'"></td>
<td align="left" valign="bottom" nowrap class="'||CLASS_BODY||'"><img src="'||ICONA||'" ></td>
<td align="left" valign="center" nowrap class="'||CLASS_BODY||'"><a class="'||CLASS_LINK||'" title="'||IN_TITLE||'" href="#" onclick="'||IN_ONCLICK||'">'||IN_LINK||'</a></td>
<td align="left" valign="top" class="'||CLASS_END||'"><img src="'||IMAGE||'" ></td>
</tr></table>
';
  RETURN OUT_HTML;
END;
/


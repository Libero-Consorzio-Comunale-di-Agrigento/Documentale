CREATE OR REPLACE FUNCTION F_TAB_FOLDER
(IN_LINK     IN VARCHAR2,
 IN_HREF     IN VARCHAR2,
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
  ICONA       VARCHAR2(32000):= '../common/images/tabfolder/'||IN_ICONA;
  OUT_HTML    VARCHAR2(32000):= NULL;
  HREF        VARCHAR2(32000);
BEGIN
  IF IN_ACTIVE = 'S' THEN
    CLASS_BODY  := CLASS_BODY||'Sel';
  END IF;
  CLASS_BEGIN := CLASS_BODY||'L';
  CLASS_END   := CLASS_BODY||'R';
  --CLASS_LINK  := CLASS_BODY||'Link';
  CLASS_LINK  := 'AFCGuidaLink';
  IF IN_HREF = '#' THEN
    HREF  := '<p><font style="text-decoration:none;border-top: 0px solid #808080;border-bottom: 0px solid #808080;color: #000000;font-size: 100%;font-weight: normal;">'||IN_LINK||'</font></p>';
  ELSE
    HREF  := '<a class="'||CLASS_LINK||'" title="'||IN_TITLE||'" href="'||IN_HREF||'">'||IN_LINK||'</a>';
  END IF;
  OUT_HTML := OUT_HTML||'
    <TABLE cellpadding="0" cellspacing="0" border="0"><tr>
   <td align="left" valign="top" CLASS="'||CLASS_BEGIN||'"><img src="'||IMAGE||'" ></td>
   <td align="left" valign="bottom" nowrap CLASS="'||CLASS_BODY||'"><img src="'||ICONA||'" ></td>
   <td align="left" valign="center" nowrap CLASS="'||CLASS_BODY||'">'||HREF||'</td>
   <td align="left" valign="top" CLASS="'||CLASS_END||'"><img src="'||IMAGE||'" ></td>
   </tr></TABLE>
   ';
  RETURN OUT_HTML;
END;
/


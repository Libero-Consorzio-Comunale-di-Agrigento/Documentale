CREATE OR REPLACE FUNCTION GDM_Binding (
   P_TESTO          IN       VARCHAR2
 , P_ID_DOC         IN       VARCHAR2)
   RETURN VARCHAR2
IS
BEGIN
   RETURN SI4COMP_BINDING(REPLACE(P_TESTO,':ID_DOCUMENTO',':OGGETTO'),P_ID_DOC,'DOCUMENTI');
END;
/


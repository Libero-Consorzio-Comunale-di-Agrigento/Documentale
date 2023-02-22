CREATE OR REPLACE PACKAGE GDM_CLOB is
/******************************************************************************
 NOME:        HL7_XML
 DESCRIZIONE: PACKAGE HL7 TRASFORMAZIONE XML
 REVISIONI:
 Rev. Data        Autore    Descrizione
 -- ----------  --------  --------------------------------------------------
 1    30/10/2010  GMANNELLA
******************************************************************************/
type list_char is table of varchar2(32000);
debug  boolean := false;
function ADD_CHAR
(in_clob            in clob
,in_text            in varchar2
) return clob;
function INSTR
(in_clob            in clob
,in_pattern         in varchar2
,in_start           in number default 1
,in_nth             in number default 1
,in_reverse         in number default 0
) return number;
function IS_EQUAL
(in_clob_1              in clob
,in_clob_2              in clob
) return number;
function LENGTH
(in_clob            in clob
) return number;
function LPAD
(in_clob            in clob
,in_padded_length   in number
,in_pad_string      in varchar2 default ' '
) return clob;
function LTRIM
(in_clob             in clob
,in_trim_string      in varchar2 default ' '
) return clob;
function RTRIM
(in_clob             in clob
,in_trim_string      in varchar2 default ' '
) return clob;
function REPLACE
(in_clob            in clob
,in_search          in varchar2
,in_replace         in varchar2
) return clob;
function SUBSTR
(in_clob            in clob
,in_start           in number
,in_amount          in number default null
) return  clob;
function TO_CHAR
(in_clob              in clob
) return varchar2;
function TO_CLOB
(in_char              in varchar2
) return clob;
function FROM_BLOB(i_blob blob)
return clob;
function TO_BLOB(i_clob clob)
return blob;
end;
/


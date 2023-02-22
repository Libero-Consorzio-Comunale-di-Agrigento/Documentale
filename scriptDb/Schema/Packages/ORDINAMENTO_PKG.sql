CREATE OR REPLACE PACKAGE ORDINAMENTO_PKG AS
/******************************************************************************
   NAME:       ORDINAMENTO_PKG
   PURPOSE:
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        21/11/2007             1. Created this package.
******************************************************************************/
  FUNCTION genera_chiave( p_oggetto      IN links.id_oggetto%type
                        , p_tipo_oggetto IN links.tipo_oggetto%type
                        , p_cartella IN links.id_cartella%type default null) RETURN NUMBER;
END ORDINAMENTO_PKG;
/


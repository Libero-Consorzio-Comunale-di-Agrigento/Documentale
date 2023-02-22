CREATE OR REPLACE PACKAGE Utilitypackage
/******************************************************************************
 NOME:        UtilityPackage
 DESCRIZIONE: Contiene oggetti di utilita generale.
 REVISIONI:
 Rev. Data        Autore  Descrizione
 ---- ----------  ------  ----------------------------------------------------
 1    23/01/2001  MF      Inserimento commento.
 2    17/12/2003  MM      Aggiunta compilazione classi java
 3    19/10/2006  VA      Inserite procedure Disable_all e Enable_All, inseriti
                          i metodi presenti nel Package SIAREF.
 4    14/12/2006  MM      Introduzione del parametro p_java_class in compile_all.
 5    08/10/2007  FT      Aggiunta compilazione synonym
 6    12/12/2007  FT      compile_all: esclusione degli oggetti il cui nome
                          inizia con 'BIN$'
******************************************************************************/
AS
   PROCEDURE Compile_All( p_java_class IN NUMBER DEFAULT 1 );
/******************************************************************************
 DESCRIZIONE: Compila tutti gli oggetti invalidi del Schema
 ARGOMENTI:
 ECCEZIONI:   -
******************************************************************************/
   PROCEDURE Disable_All;
/******************************************************************************
 DESCRIZIONE: Disabilita tutti i trigger e i constraint di Foreign Key e di Check dello Schema
 ARGOMENTI:
 ECCEZIONI:   -
******************************************************************************/
   PROCEDURE Enable_All(p_validate NUMBER DEFAULT 1);
/******************************************************************************
 DESCRIZIONE: Abilita tutti i trigger e i constraint di Foreign Key e di Check dello Schema
 ARGOMENTI:   p_validate      IN     NUMBER   Forza la validate o la novalidate
 ECCEZIONI:   -
******************************************************************************/
   PROCEDURE CREATE_GRANT  (p_grantee IN VARCHAR2,
                             p_object  IN VARCHAR2 := '%',
                             p_type    IN VARCHAR2 := '',
                             p_grant   IN VARCHAR2 := '',
                             p_option  IN VARCHAR2 := '',
                             p_grantor IN VARCHAR2 := USER);
   PROCEDURE GRANT_LIKE     (p_object     IN VARCHAR2,
                             p_likeobject IN VARCHAR2,
                             p_grantor    IN VARCHAR2 DEFAULT USER);
/******************************************************************************
 DESCRIZIONE: Assegna ad un dato oggetto le stesse grant di un'altro esistente
 ARGOMENTI:   p_object      IN     VARCHAR2   Oggetto su cui dare le grant
              p_likeobject  IN     VARCHAR2   Oggetto template
              p_grantor     IN     VARCHAR2   Utente proprietario dell'oggetto
 ECCEZIONI:   -
******************************************************************************/
   PROCEDURE CREATE_SYNONYM  (p_object         IN  VARCHAR2 := '%',
                                p_prefix         IN  VARCHAR2 := '',
                                p_grantor        IN  VARCHAR2 := '%',
                                p_grantee        IN  VARCHAR2 := USER );
/******************************************************************************
 DESCRIZIONE: Crea i sinonimi per gli oggetti a cui si ha accesso
 ARGOMENTI:   p_object  IN     VARCHAR2   Oggetto per cui creare il sinonimo
              p_prefix  IN     VARCHAR2   Eventuale prefisso da apporre al sinonimo
              p_grantee IN     VARCHAR2   Utente proprietario del sinonimo
 ECCEZIONI:   20999, Occorre dare la grant <Create synonym> direttamente all'utente
******************************************************************************/
   PROCEDURE CREATE_VIEW (p_owner            IN  VARCHAR2 ,
                            p_object           IN  VARCHAR2 := '%',
                            p_prefix           IN  VARCHAR2 := '',
                            p_db_link          IN  VARCHAR2 := '');
/******************************************************************************
 DESCRIZIONE: Crea le viste per gli oggetti di un dato utente accessibili
 ARGOMENTI:   p_owner    IN     VARCHAR2   Proprietario degli oggetti
              di cui creare la vista
              p_object   IN     VARCHAR2   Oggetto di cui creare la vista
              p_db_link  IN     VARCHAR2   Eventuale dblink
 ECCEZIONI:   20999, Occorre dare la grant <Create view> direttamente all'utente
******************************************************************************/
   FUNCTION  VERSIONE         RETURN VARCHAR2;
END Utilitypackage;
/* End Package: UtilityPackage */
/


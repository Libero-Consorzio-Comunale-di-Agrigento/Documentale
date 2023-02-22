CREATE OR REPLACE FUNCTION F_GETSTATOCONSERVAZIONE(P_IDDOCUMENTO NUMBER) RETURN VARCHAR2 IS
/*
   NP = Non presente il sistema di conservazione
   NN = Mai passato in conservazione
   DC = Da Conservare
   FC = Fallita Conservazione
   CC = Conservato Correttamente

*/
RET varchar2(10);
no_table_942        EXCEPTION;
PRAGMA               EXCEPTION_INIT(no_table_942, -00942);
SQL_LOG_CONS    VARCHAR2(2000);
A_CHECK              NUMBER(1);
BEGIN
  SQL_LOG_CONS :=' select  ID_DOCUMENTO_RIF, stato_conservazione,nvl(data_inizio,to_Date(''01/01/1900 11:00:00'',''dd/mm/yyyy HH24:mi:ss'')) dtinizio '||
                              '                from gdm_t_log_conservazione '||
                              '                where id_documento_rif=  '||P_IDDOCUMENTO||
                              '    UNION ';

  BEGIN
    SELECT 1 INTO A_CHECK FROM USER_TABLES WHERE TABLE_NAME='GDM_T_LOG_CONSERVAZIONE';

  EXCEPTION  WHEN NO_DATA_FOUND THEN
      SQL_LOG_CONS :='';
  END;

   BEGIN
        EXECUTE IMMEDIATE 'select stato_conservazione '||
                                        '    from (   '||SQL_LOG_CONS||
                                        ' select ID_DOCUMENTO,decode(nvl(STATO_CONSERVAZIONE,''0'')  ,''0'', ''NN'' ,''1'' ,''DC'' ,''2'',''DC'' ,''4'' ,''CC'' ,''5'' ,''FC''  ) ,to_Date(''01/01/5900 11:00:00'',''dd/mm/yyyy HH24:mi:ss'') dtinizio '||
                                        '    from docer_statoconsev_view '||
                                        '    where ID_DOCUMENTO=  '||P_IDDOCUMENTO||
                                        '               order by 3 desc '||
                                        '            ) '||
                                        ' where rownum=1' INTO RET;

    EXCEPTION WHEN no_table_942  THEN
        RET := 'NP';
                     WHEN NO_DATA_FOUND THEN
        RET := 'NN';
    END;

    return RET;

EXCEPTION WHEN OTHERS THEN
       RAISE;
END F_GETSTATOCONSERVAZIONE;
/


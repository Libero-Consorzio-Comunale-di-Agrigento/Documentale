CREATE OR REPLACE package body os_command is
  function exec_CLOB(p_command in varchar2, p_stdin in blob) return clob
  is language java name 'ExternalCall.execClob(java.lang.String, oracle.sql.BLOB) return oracle.sql.CLOB';

  function exec_CLOB(p_command in varchar2, p_stdin in clob) return clob
  is language java name 'ExternalCall.execClob(java.lang.String, oracle.sql.CLOB) return oracle.sql.CLOB';

  function exec_BLOB(p_command in varchar2, p_stdin in blob) return blob
  is language java name 'ExternalCall.execBlob(java.lang.String, oracle.sql.BLOB) return oracle.sql.BLOB';

  function exec_BLOB(p_command in varchar2, p_stdin in clob) return blob
  is language java name 'ExternalCall.execBlob(java.lang.String, oracle.sql.CLOB) return oracle.sql.BLOB';

  function exec_CLOB(p_command in varchar2) return Clob
  is language java name 'ExternalCall.execClob(java.lang.String) return oracle.sql.CLOB';

  function exec_BLOB(p_command in varchar2) return blob
  is language java name 'ExternalCall.execBlob(java.lang.String) return oracle.sql.BLOB';

  function exec(p_command in varchar2, p_stdin in blob) return number
  is language java name 'ExternalCall.exec(java.lang.String, oracle.sql.BLOB) return int';

  function exec(p_command in varchar2, p_stdin in clob) return number
  is language java name 'ExternalCall.exec(java.lang.String, oracle.sql.CLOB) return int';

  function exec(p_command in varchar2) return number
  is language java name 'ExternalCall.exec(java.lang.String) return int';

  function exec(p_command in varchar2, p_stdin in clob, p_stdout in clob) return number
  is language java name 'ExternalCall.execOut(java.lang.String, oracle.sql.CLOB, oracle.sql.CLOB) return int';

  function exec(p_command in varchar2, p_stdin in clob, p_stdout in blob) return number
  is language java name 'ExternalCall.execOut(java.lang.String, oracle.sql.CLOB, oracle.sql.BLOB) return int';

  function exec(p_command in varchar2, p_stdin in blob, p_stdout in blob) return number
  is language java name 'ExternalCall.execOut(java.lang.String, oracle.sql.BLOB, oracle.sql.BLOB) return int';

  function exec(p_command in varchar2, p_stdin in blob, p_stdout in clob) return number
  is language java name 'ExternalCall.execOut(java.lang.String, oracle.sql.BLOB, oracle.sql.CLOB) return int';

  function exec(p_command in varchar2, p_stdout in clob) return number
  is language java name 'ExternalCall.execOut(java.lang.String, oracle.sql.CLOB) return int';

  function exec(p_command in varchar2, p_stdout in blob) return number
  is language java name 'ExternalCall.execOut(java.lang.String, oracle.sql.BLOB) return int';



  function get_file_list(p_directory in varchar2) return FILE_LIST_TYPE
  is language java name 'ExternalCall.getFileList(java.lang.String) return oracle.sql.ARRAY';

end os_command;
/


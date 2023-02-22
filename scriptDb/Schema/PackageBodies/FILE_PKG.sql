CREATE OR REPLACE package body file_pkg is
  function get_file(
    p_file_path in varchar2
  ) return file_type
  is language java name 'FileType.getFile(java.lang.String) return oracle.sql.STRUCT';

  function get_file_list(
    p_directory in file_type
  ) return file_list_type
  is language java name 'FileType.getFileList(oracle.sql.STRUCT) return oracle.sql.ARRAY';

  function get_recursive_file_list(
    p_directory in file_type
  ) return file_list_type
  is language java name 'FileType.getRecursiveFileList(oracle.sql.STRUCT) return oracle.sql.ARRAY';
end file_pkg;
/


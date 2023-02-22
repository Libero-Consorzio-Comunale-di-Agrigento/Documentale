CREATE OR REPLACE package file_pkg
is
  function get_file(
    p_file_path in varchar2
  ) return file_type;
  function get_file_list(
    p_directory in file_type
  ) return file_list_type;
  function get_recursive_file_list(
    p_directory in file_type
  ) return file_list_type;
end file_pkg;
/


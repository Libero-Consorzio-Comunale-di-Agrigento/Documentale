Insert into KEY_ERROR
   (ERRORE, DESCRIZIONE, TIPO, KEY)
 Values
   ('A00001', 'Registrazione gia'' presente $table:in', 'E', NULL);
Insert into KEY_ERROR
   (ERRORE, DESCRIZIONE, TIPO, KEY)
 Values
   ('A02290', 'Registrazione $table:in con informazioni incongruenti', 'E', NULL);
Insert into KEY_ERROR
   (ERRORE, DESCRIZIONE, TIPO, KEY)
 Values
   ('A02291', 'Riferimento $table:di non presente $ref_table:in', 'E', NULL);
Insert into KEY_ERROR
   (ERRORE, DESCRIZIONE, TIPO, KEY)
 Values
   ('A02292', 'Esistono riferimenti $table:in alla registrazione $ref_table:di', 'E', NULL);
COMMIT;

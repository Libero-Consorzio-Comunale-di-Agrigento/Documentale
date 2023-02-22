CREATE OR REPLACE TRIGGER "DR$VAL_STR_CATTC" after insert or update on "GDM"."VALORI" for each row
declare   reindex boolean := FALSE;   updop   boolean := FALSE; begin   ctxsys.drvdml.c_updtab.delete;   ctxsys.drvdml.c_numtab.delete;   ctxsys.drvdml.c_vctab.delete;   ctxsys.drvdml.c_rowid := :new.rowid;   if (inserting or updating('VALORE_STRINGA') or       :new."VALORE_STRINGA" <> :old."VALORE_STRINGA") then     reindex := TRUE;     updop := (not inserting);     ctxsys.drvdml.c_text_vc2 := :new."VALORE_STRINGA";   end if;   ctxsys.drvdml.c_cntab(0) := 'ID_CAMPO';  ctxsys.drvdml.c_cttab(0) := 'NUMBER';  ctxsys.drvdml.c_updtab(0) := updating('ID_CAMPO');   ctxsys.drvdml.c_numtab(0) := :new."ID_CAMPO";  ctxsys.drvdml.ctxcat_dml('GDM','VAL_STR_CAT', reindex, updop); end;
/



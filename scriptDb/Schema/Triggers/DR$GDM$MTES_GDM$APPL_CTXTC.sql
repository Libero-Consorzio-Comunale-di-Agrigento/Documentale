CREATE OR REPLACE TRIGGER "DR$GDM$MTES_GDM$APPL_CTXTC" after insert or update on "GDM"."GDM_T_MODELLO_TESTI" for each row
declare   reindex boolean := FALSE;   updop   boolean := FALSE; begin   ctxsys.drvdml.c_updtab.delete;   ctxsys.drvdml.c_numtab.delete;   ctxsys.drvdml.c_vctab.delete;   ctxsys.drvdml.c_rowid := :new.rowid;   if (inserting or updating('APPLICATIVO') or       :new."APPLICATIVO" <> :old."APPLICATIVO") then     reindex := TRUE;     updop := (not inserting);     ctxsys.drvdml.c_text_vc2 := :new."APPLICATIVO";   end if;   ctxsys.drvdml.ctxcat_dml('GDM','GDM$MTES_GDM$APPL_CTX', reindex, updop); end;
/



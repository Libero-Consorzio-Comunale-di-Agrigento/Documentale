CREATE OR REPLACE FUNCTION F_FIND_IDRIF_TABLE(P_IDRIF VARCHAR2)  RETURN VARCHAR2
IS
TYPE cur_typ IS REF CURSOR;
C cur_typ;
A_CONTA NUMBER(10) := 0;
A_QRY VARCHAR2(5000) := '';
A_RET  VARCHAR2(32000):='';
BEGIN
    FOR I IN (select TABLE_NAME
                    from user_tab_columns , obj
                    where column_name='IDRIF' and instr(table_name,'$')=0 and
                                object_name=user_tab_columns.table_name and object_type='TABLE'  )  LOOP

            A_QRY := 'SELECT COUNT(1) FROM '||I.TABLE_NAME||' WHERE IDRIF='''||P_IDRIF||'''';

            OPEN C FOR A_QRY;
            FETCH C INTO A_CONTA;
            CLOSE C;

            IF A_CONTA>0 THEN
                IF LENGTH(A_RET)>0 THEN
                    A_RET := A_RET || ',';
                END IF;

                A_RET := A_RET || I.TABLE_NAME;
            END IF;


    END LOOP;

    RETURN  A_RET;
END;
/


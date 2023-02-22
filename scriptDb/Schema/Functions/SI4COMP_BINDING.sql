CREATE OR REPLACE FUNCTION Si4comp_Binding (
   P_TESTO          IN       VARCHAR2
 , P_OGGETTO        IN       VARCHAR2
 , P_TIPO_OGGETTO   IN       VARCHAR2 DEFAULT NULL)
   RETURN VARCHAR2
IS
   D_TESTO             VARCHAR2 (4000);
BEGIN
   DECLARE
      D_STRINGA_RIMASTA   D_TESTO%TYPE;
      d_stringa_temp_prima d_testo%TYPE;
      D_PARAMETRO         D_TESTO%TYPE;
      d_valore            d_testo%TYPE;
      D_VALORE_SOSTITUITO D_TESTO%TYPE;
      dizionario          VARCHAR2 (100);
      carseg              VARCHAR2 (1);
      k                   NUMBER;
      kspec1              NUMBER;
      kspec2              NUMBER;
   BEGIN
      dizionario       := ' ,)(*+/-|&=<>''\:?.!%{}' || CHR (10) || CHR (13);
      d_stringa_rimasta := p_testo;
      d_testo          := '';
      k                := INSTR (d_stringa_rimasta, ':');
      WHILE (k <> 0) LOOP
         BEGIN
            kspec1           := INSTR (d_stringa_rimasta, '::');
            kspec2           := INSTR (d_stringa_rimasta, ':=');
            d_stringa_temp_prima := SUBSTR (d_stringa_rimasta
                                          , 1
                                          , k - 1);
            d_parametro      := '';
            d_valore         := '';
            IF (    (   k < kspec1
                     OR kspec1 = 0)
                AND (   k < kspec2
                     OR kspec2 = 0)) THEN
               -- Sono in presenza di un parametro
               carseg           := SUBSTR (d_stringa_rimasta
                                         , k + 1
                                         , 1);
               d_parametro      := carseg;
               k                := k + 2;
               carseg           := SUBSTR (d_stringa_rimasta
                                         , k
                                         , 1);
               WHILE (    k <= LENGTH (d_stringa_rimasta)
                      AND INSTR (dizionario, carseg) = 0) LOOP
                  BEGIN
                     d_parametro      := d_parametro || carseg;
                     k                := k + 1;
                     carseg           := SUBSTR (d_stringa_rimasta
                                               , k
                                               , 1);
                  END;
               END LOOP;
               k                := k - 1;
               DBMS_OUTPUT.PUT_LINE (UPPER (d_parametro));
               IF (LENGTH (d_parametro) > 0) THEN
                  IF (    UPPER (d_parametro) <> 'UTENTE'
                      AND UPPER (d_parametro) <> 'OGGETTO') THEN
                     IF (p_tipo_oggetto = 'DOCUMENTI') THEN
                        -- L'OGGETTO Ô UN DOCUMENTO
                        D_VALORE         := F_Valore_Campo (P_OGGETTO, D_PARAMETRO);
                     ELSE
                        -- L'oggetto non ¿ un documento
                        d_valore         := 'PARNONINTERPRETATO';
                     END IF;
                  ELSE
                     IF UPPER (d_parametro) = 'OGGETTO' THEN
                        d_valore         := p_oggetto;
                     ELSE
                        d_valore         := ':' || UPPER (d_parametro);      --':UTENTE';
                     END IF;
                  END IF;
               ELSE
                  d_valore         := 'NOMEPARNONINTERPRETATO';
               END IF;
            ELSIF (k = kspec1) THEN
               -- Sono in presenza di un : protetto
               d_stringa_temp_prima := SUBSTR (d_stringa_rimasta
                                             , 1
                                             , k);
               k                := k + 1;
            ELSIF (k = kspec2) THEN
               -- Sono in presenza di una sequenza ammessa
               d_stringa_temp_prima := SUBSTR (d_stringa_rimasta
                                             , 1
                                             , k + 1);
               k                := k + 1;
            END IF;
            d_testo          := d_testo || d_stringa_temp_prima || d_valore;
            d_stringa_rimasta := SUBSTR (d_stringa_rimasta, k + 1);
            k                := INSTR (d_stringa_rimasta, ':');
         END;
      END LOOP;
      D_TESTO          := D_TESTO || D_STRINGA_RIMASTA;
      RETURN D_TESTO;
   END;
EXCEPTION
   WHEN OTHERS THEN
      -- CONSIDER LOGGING THE ERROR AND THEN RE-RAISE
      RAISE;
END Si4comp_Binding;
/


package it.finmatica.gestionedocumenti.integrazioni.gdm;

import it.finmatica.jdmsutil.data.ProfiloExtend;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModelloGdm {

    /**
     * Codice del Modello GDM
     */
    String codiceModello() default "";

    /**
     * Area del Modello GDM
     */
    String codiceArea() default "";

    Class<? extends ModelloGdmAdapter> adapter() default ModelloGdm.EMPTY_ADAPTER.class;

    class EMPTY_ADAPTER<T extends Object> implements ModelloGdmAdapter<T> {

        @Override
        public String getCodiceArea(T o) {
            return o.getClass().getAnnotation(ModelloGdm.class).codiceArea();
        }

        @Override
        public String getCodiceModello(T o) {
            return o.getClass().getAnnotation(ModelloGdm.class).codiceModello();
        }

        @Override
        public void afterSave(T o, ProfiloExtend p) {
            // do nothing
        }

        @Override
        public void beforeSave(T o, ProfiloExtend p) {
            // do nothing
        }
    }
}

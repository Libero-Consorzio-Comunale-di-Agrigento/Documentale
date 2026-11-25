package it.finmatica.gestionedocumenti.integrazioni.gdm;

import it.finmatica.jdmsutil.data.ProfiloExtend;

/**
 * Created by esasdelli on 18/01/2017.
 */
public interface ModelloGdmAdapter<T> {

    String getCodiceArea (T object);

    String getCodiceModello (T object);

    void afterSave (T object, ProfiloExtend profiloExtend);

    void beforeSave (T object, ProfiloExtend profiloExtend);
}

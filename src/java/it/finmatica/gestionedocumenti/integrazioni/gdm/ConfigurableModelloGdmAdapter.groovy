package it.finmatica.gestionedocumenti.integrazioni.gdm

import it.finmatica.gestionedocumenti.Holders
import it.finmatica.jdmsutil.data.ProfiloExtend

/**
 * Classe che rende configurabile l'annotation ModelloGdm.
 * Per utilizzarla, è sufficiente annotare la propria classe con @ModelloGdm(adapter=ConfigurableModelloGdmAdapter)
 * quindi nel application.properties è necessario scrivere la configurazione per quella specifica classe, la configurazione ha il formato:
 * PREFISSO.FULL_QUALIFIED_CLASS_NAME.[area|modello|adapter]
 *
 * Ad esempio:
 * finmatica.gestionedocumenti.modellogdm.it.finmatica.gestionedocumenti.documenti.Allegato.area=SEGRETERIA
 * finmatica.gestionedocumenti.modellogdm.it.finmatica.gestionedocumenti.documenti.Allegato.modello=M_ALLEGATO
 * finmatica.gestionedocumenti.modellogdm.it.finmatica.gestionedocumenti.documenti.Allegato.adapter=ClasseAdapter
 *
 * Da notare che se viene specificata l'area od il modello, allora verrà usata quella senza invocare l'eventuale adapter specificato.
 *
 * Created by esasdelli on 20/01/2017.
 */
class ConfigurableModelloGdmAdapter<T> implements ModelloGdmAdapter<T> {

    public static String PREFIX = "finmatica.gestionedocumenti.modellogdm"

    @Override
    String getCodiceArea(T object) {
        String area = getConfiguredArea(object)
        if (area != null && area.trim().length() > 0) {
            return area
        }
        return getConfiguredAdapter(object).getCodiceArea(object)
    }

    @Override
    String getCodiceModello(T object) {
        String modello = getConfiguredModello(object)
        if (modello != null && modello.trim().length() > 0) {
            return modello
        }
        return getConfiguredAdapter(object).getCodiceModello(object)
    }

    @Override
    void afterSave(T object, ProfiloExtend profiloExtend) {
        getConfiguredAdapter(object).afterSave(object, profiloExtend)
    }

    @Override
    void beforeSave(T object, ProfiloExtend profiloExtend) {
        getConfiguredAdapter(object).beforeSave(object, profiloExtend)
    }

    String getConfiguredArea(T object) {
        return getConfigPerClass(object.class, "area")
    }

    String getConfiguredModello(T object) {
        return getConfigPerClass(object.class, "modello")
    }

    ModelloGdmAdapter getConfiguredAdapter(T object) {
        String className = getConfigPerClass(object.class, "adapter")
        if (className != null) {
            return (ModelloGdmAdapter) Class.forName(className).newInstance()
        } else {
            return ModelloGdm.EMPTY_ADAPTER.newInstance()
        }
    }

    String getConfigPerClass(Class clazz, String propertyName) {
        String key = "${PREFIX}.${clazz.canonicalName}.${propertyName}"
        return Holders.getApplicationContext().getEnvironment().getProperty(key)
    }
}

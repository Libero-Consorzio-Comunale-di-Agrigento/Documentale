package it.finmatica.gestionedocumenti.integrazioni.gdm

import it.finmatica.gestionedocumenti.Holders
import it.finmatica.jdmsutil.data.ProfiloExtend
import org.hibernate.SessionFactory

/**
 * Created by esasdelli on 18/01/2017.
 */
class VistaModelloGdmAdapter extends ModelloGdm.EMPTY_ADAPTER<Object> {

    @Override
    void afterSave (Object object, ProfiloExtend profiloExtend) {

        // svuoto la cache di Hibernate relativa alla domainObject perché altrimenti in caso di domain che mappano viste di gdm
        // il valore della domain non viene correttamente aggiornato.
        SessionFactory sessionFactory = Holders.applicationContext.getBean("sessionFactory")
        sessionFactory.currentSession.evict (object)
    }
}

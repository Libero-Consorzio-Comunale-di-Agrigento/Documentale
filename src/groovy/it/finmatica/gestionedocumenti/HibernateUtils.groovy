package it.finmatica.gestionedocumenti

import groovy.transform.CompileStatic
import org.hibernate.proxy.HibernateProxy

// Con hibernat 5.2.10 non ci sarà più bisogno di questo metodo e si potrà usare: Hibernate.unwrap()
@CompileStatic
class HibernateUtils {

    static Object unwrapIfProxy(Object object) {
        if (object instanceof HibernateProxy) {
            return object.getHibernateLazyInitializer().getImplementation()
        } else {
            return object
        }
    }
}

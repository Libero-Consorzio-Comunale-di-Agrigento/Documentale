package it.finmatica.gestionedocumenti

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

// TODO[SPRINGBOOT]: classe per accedere staticamente al contesto spring.
class Holders implements ApplicationContextAware {

    private static ApplicationContext applicationContext

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext
    }

    static ApplicationContext getApplicationContext() {
        return applicationContext
    }
}

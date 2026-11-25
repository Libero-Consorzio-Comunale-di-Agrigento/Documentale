package it.finmatica.gestionedocumenti.commons;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.AbstractRequestAttributesScope;

/**
 * Presa ispirazione da qui: http://php.sabscape.com/blog/?p=454
 *
 * Questo è un custom-scope di spring: serve per poter gestire quei bean thread-bounded che normalmente sarebbero "request" scope
 * ma che a volte sono invocati da Thread non legati alla web-request e quindi lo scope "request" non andrebbe bene.
 * Esempi di questi bean sono:
 *   - notificheDispatcher 	-> invia le notifiche in background
 *   - successHandler 		-> gestisce i messaggi di successo delle azioni dell'iter (viene invocato ad esempio in job notturni)
 *
 * Praticamente quello che fa questo custom-scope è un wrapper di un altro scope.
 * Cioè: se il bean richiesto non viene trovato nello scope "proxiedScope", allora viene creato da zero e messo in questo scope (che a questo punto sarà thread).
 * altrimenti viene ritornato il bean originale.
 *
 * Così facendo: se trovo il bean nello scope request, lo ritorno, altrimenti ne creo uno nuovo.
 *
 * @author esasdelli
 *
 */
public class FallbackScope implements Scope {
	public final static String DEFAULT_NAME = "fallback";
	private Scope proxiedScope;
	private ConcurrentHashMap<String, Object> objectMap = new ConcurrentHashMap<String, Object>();
	private static final Logger log = Logger.getLogger(FallbackScope.class);

	public FallbackScope (Scope proxiedScope) {
		this.proxiedScope = proxiedScope;
	}

	public Object get (String name, ObjectFactory<?> factory) {
		try {
			return proxiedScope.get(name, factory);
		} catch (Throwable t) {
			if (objectMap.get(name) == null) {
				Object o = factory.getObject();
				objectMap.put(name, o);
			}

			return objectMap.get(name);
		}
	}

	public String getConversationId () {
		try {
			return proxiedScope.getConversationId();
		} catch (Throwable t) {
			log.warn("Fallback: getConversationId");
			return null;
		}
	}

	public void registerDestructionCallback (String name, Runnable callback) {
		try {
			proxiedScope.registerDestructionCallback(name, callback);
		} catch (Throwable t) {
			log.warn("Fallback: registerDestructionCallback");
		}
	}

	public Object remove (String name) {
		try {
			return proxiedScope.remove(name);
		} catch (Throwable t) {
			log.warn("Fallback: remove");
			return objectMap.remove(name);
		}
	}

	public Object resolveContextualObject (String arg0) {
		try {
			return proxiedScope.resolveContextualObject(arg0);
		} catch (Throwable t) {
			log.warn("Fallback: resolveContextualObject");
			return null;
		}
	}
}


package it.finmatica.gestionedocumenti.notifiche.calcolo.annotated

import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.notifiche.Allegato
import it.finmatica.gestionedocumenti.notifiche.NotificaDestinatario
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RegolaCalcoloNotifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica
import org.springframework.beans.factory.annotation.Autowired

import java.lang.reflect.Method

class AnnotatedRepositoryRegoleCalcolo implements RepositoryRegoleCalcolo {

    @Autowired(required = false)
	private List<AnnotatedRegolaCalcolo> regoleCalcolo

	@Override
	String getTipoDocumento (Object documento) {
		for (AnnotatedRegolaCalcolo regolaCalcolo : regoleCalcolo) {
			String tipoOggetto = regolaCalcolo.getTipoDocumento(documento)
			if (tipoOggetto != null) {
				return tipoOggetto
			}
		}

		throw new IllegalArgumentException("Non ho trovato il tipoOggetto per il documento: ${documento}")
	}

	@Override
	String getIdRiferimento(Object documento) {
		for (AnnotatedRegolaCalcolo regolaCalcolo : regoleCalcolo) {
			String idRiferimento = regolaCalcolo.getIdRiferimento(documento)
			if (idRiferimento != null) {
				return idRiferimento
			}
		}

		throw new IllegalArgumentException("Non ho trovato l'id riferimento per il documento: ${documento}")
	}

	@Override
	Object getDocumento(String idRiferimento) {
		for (AnnotatedRegolaCalcolo regolaCalcolo : regoleCalcolo) {
			Object documento = regolaCalcolo.getDocumento(idRiferimento)
			if (documento != null) {
				return documento
			}
		}

		throw new IllegalArgumentException("Non ho trovato il documento per l'id riferimento: ${idRiferimento}")
	}

	@Override
    List<TipoNotifica> getListaTipiNotifica() {
        return (regoleCalcolo*.listaTipiNotifica).flatten()
    }

    List<RegolaCalcoloNotifica> getRegoleCalcoloTag () {
		return getListaRegoleCalcolo (RegolaCalcoloNotifica.TipoMetodo.TAG)
	}

	List<RegolaCalcoloNotifica> getRegoleCalcoloDestinatari () {
		return getListaRegoleCalcolo (RegolaCalcoloNotifica.TipoMetodo.DESTINATARI)
	}

	List<RegolaCalcoloNotifica> getRegoleCalcoloAllegati () {
		return getListaRegoleCalcolo (RegolaCalcoloNotifica.TipoMetodo.ALLEGATO)
	}

	private List<RegolaCalcoloNotifica> getListaRegoleCalcolo (RegolaCalcoloNotifica.TipoMetodo tipoMetodo) {
		List<RegolaCalcoloNotifica> listaRegoleCalcolo = new ArrayList<RegolaCalcoloNotifica>()
		for (AnnotatedRegolaCalcolo repository : regoleCalcolo) {

			List<Method> methods = repository.getClass().getDeclaredMethods()
			for (Method method : methods) {

				if (!method.isAnnotationPresent(RegolaCalcolo.class)) {
					continue
				}

				RegolaCalcolo ann = method.getAnnotation(RegolaCalcolo.class)
                if (ann.tipo() != tipoMetodo) {
                    continue
                }

				listaRegoleCalcolo << new RegolaCalcoloNotifica (
                    codice:         ann.codice(),
                    tipiDocumento:  ann.tipiDocumento(),
                    tipo:           ann.tipo(),
                    titolo:         ann.titolo(),
                    descrizione:    ann.descrizione())
			}
		}

		return listaRegoleCalcolo
	}

	List<NotificaSoggetto> calcolaDestinatari(String codiceRegolaCalcolo, Object documento, NotificaDestinatario destinatario) {
		return invokeMetodoCalcolo(codiceRegolaCalcolo, RegolaCalcoloNotifica.TipoMetodo.DESTINATARI, getTipoDocumento(documento), documento, destinatario)
	}

	String calcolaTag(String codiceRegolaCalcolo, Object documento) {
		return invokeMetodoCalcolo(codiceRegolaCalcolo, RegolaCalcoloNotifica.TipoMetodo.TAG, getTipoDocumento(documento), documento)
	}

	List<Allegato> calcolaAllegati (String codiceRegolaCalcolo, Object documento) {
		return invokeMetodoCalcolo(codiceRegolaCalcolo, RegolaCalcoloNotifica.TipoMetodo.ALLEGATO, getTipoDocumento(documento), documento)
	}

	private Object invokeMetodoCalcolo (String metodoCalcolo, RegolaCalcoloNotifica.TipoMetodo tipoMetodo, String tipoDocumento, Object... args) {
		for (AnnotatedRegolaCalcolo repository : regoleCalcolo) {
			List<Method> methods = repository.getClass().getDeclaredMethods()
			for (Method method : methods) {
				if (!method.isAnnotationPresent(RegolaCalcolo.class)) {
					continue
				}

				RegolaCalcolo ann = method.getAnnotation(RegolaCalcolo.class)
				if (ann.codice() == metodoCalcolo && ann.tipo() == tipoMetodo && (ann.tipiDocumento().contains(tipoDocumento) || ann.tipiDocumento().contains(RegolaCalcolo.TUTTI_DOCUMENTI))) {
					return method.invoke(repository, args)
				}
			}
		}

		throw new GestioneDocumentiRuntimeException("Non ho trovato il metodo di calcolo ${metodoCalcolo} richiesto per il documento con tipo: ${tipoDocumento}")
	}
}
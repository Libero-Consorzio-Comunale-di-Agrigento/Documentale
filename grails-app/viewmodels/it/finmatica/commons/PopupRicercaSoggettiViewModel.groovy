package it.finmatica.commons

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class PopupRicercaSoggettiViewModel {

    // componenti
    Window self

    // dati
    def listaSoggetti
    def soggetto
    String nome = "";
    String cognome = "";
    String codiceFiscale = "";
    Date dataNascita = null;
    boolean ricercaInterna = true;

    // stato
    int activePage = 0;
    int pageSize = 30;
    int totalSize;

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w;
    }

    @NotifyChange(["listaSoggetti", "totalSize", "activePage"])
    @Command
    void onCerca(@BindingParam("paginate") boolean paginate) {
        if (paginate == false) {
            activePage = 0;
        }

        if (ricercaInterna) {
            ricercaSuSo4();
        } else {
            ricercaSuAs4();
        }
    }

    private def ricercaSuAs4() {
        listaSoggetti = As4SoggettoCorrente.createCriteria().list(max: pageSize, offset: pageSize * (activePage)) {

            if (cognome.trim().length() > 0) {
                ilike("cognome", "%" + cognome + "%")
            }

            if (nome.trim().length() > 0) {
                ilike("nome", "%" + nome + "%")
            }

            if (codiceFiscale.trim().length() > 0) {
                ilike("codiceFiscale", "%" + codiceFiscale + "%")
            }

            if (dataNascita != null) {
                eq("dataNascita", dataNascita)
            }

            order("cognome", "asc")
            order("nome", "asc")

            fetchMode("utenteAd4", FetchMode.JOIN)
        }

        totalSize = listaSoggetti.totalCount;
    }

    private def ricercaSuSo4() {
        listaSoggetti = So4ComponentePubb.createCriteria().listDistinct {
            projections {
                property("soggetto")
            }
            soggetto {
                if (cognome.trim().length() > 0) {
                    ilike("cognome", "%" + cognome + "%")
                }

                if (nome.trim().length() > 0) {
                    ilike("nome", "%" + nome + "%")
                }

                if (codiceFiscale.trim().length() > 0) {
                    ilike("codiceFiscale", "%" + codiceFiscale + "%")
                }

                if (dataNascita != null) {
                    eq("dataNascita", dataNascita)
                }

                maxResults(pageSize)
                firstResult(pageSize * activePage)

                order("cognome", "asc")
                order("nome", "asc")
            }

            fetchMode("soggetto", FetchMode.JOIN)
            fetchMode("utenteAd4", FetchMode.JOIN)
        }

        totalSize = So4ComponentePubb.createCriteria().get {
            projections {
                countDistinct("soggetto.id")
            }
            soggetto {
                if (cognome.trim().length() > 0) {
                    ilike("cognome", "%" + cognome + "%")
                }

                if (nome.trim().length() > 0) {
                    ilike("nome", "%" + nome + "%")
                }

                if (codiceFiscale.trim().length() > 0) {
                    ilike("codiceFiscale", "%" + codiceFiscale + "%")
                }

                if (dataNascita != null) {
                    eq("dataNascita", dataNascita)
                }
            }
        }
    }

    @Command
    void onAnnulla() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    void onSeleziona() {
        Events.postEvent(Events.ON_CLOSE, self, soggetto.toDTO())
    }
}

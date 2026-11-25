package it.finmatica.gestionedocumenti.commons

import groovy.sql.Sql
import it.finmatica.ad4.config.Ad4Properties
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.gestionedocumenti.Holders
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiMultiEnteService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.hibernatefilter.resolvablefilter.ResolvableFilterDefinition
import it.finmatica.multiente.DefaultEnte
import org.hibernate.SessionFactory
import org.springframework.security.core.GrantedAuthority

import javax.sql.DataSource
import java.nio.charset.Charset

class Utils {

    static boolean controllaCharset(String stringa, String charsetName) {
        Charset charset = Charset.forName(charsetName)
        String decoded = charset.decode(charset.encode(stringa)).toString()
        return (decoded.equals(stringa))
    }

    static boolean controllaCharset(String stringa) {
        Charset charset = Charset.forName(Impostazioni.DB_CHARSET.getValore())
        String decoded = charset.decode(charset.encode(stringa)).toString()
        return (decoded.equals(stringa))
    }

    static void controllaCharsetCampo(String nomeCampo, String stringa) {
        if (!controllaCharset(stringa, Impostazioni.DB_CHARSET.getValore())) {
            throw new GestioneDocumentiRuntimeException("Attenzione! Il campo " + nomeCampo + " contiene dei caratteri non supportati!")
        }
    }

    static Date dataOra(Date data) {
        return dataOra(data, null)
    }

    static Date dataOra(Date data, String ora) {
        String[] hhmm = (ora != null) ? ora.split(":") : new String[0]
        String hh = "0"
        String mm = "0"
        if (hhmm.length > 1) {
            hh = hhmm[0]
            mm = hhmm[1]
        }
        Calendar cal = GregorianCalendar.getInstance()
        cal.setTime(data)
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hh))
        cal.set(Calendar.MINUTE, Integer.parseInt(mm))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.getTime()
    }

    static boolean isUtenteAmministratore() {
        Ad4Properties ad4Properties = Holders.getApplicationContext().getBean(Ad4Properties)
        SpringSecurityService springSecurityService = Holders.getApplicationContext().getBean(SpringSecurityService)
        String ruoloAmministratore = ad4Properties.modulo + "_AMM"
        for (GrantedAuthority auth : springSecurityService.principal.authorities) {
            if (auth.authority == ruoloAmministratore) {
                return true
            }
        }
        return false
    }

	static void eseguiAutenticazione (String utente, String amministrazione) {
		List<Ente> enti = Ente.findAllByValido(true, true,  [sort:'sequenza', order:'asc'])
		Ente ente = enti.find { it.amministrazione.codice == amministrazione}
		eseguiAutenticazione(utente, ente.id)
	}

    static void eseguiAutenticazione(String utente, long idEnte) {
        GestioneDocumentiSpringSecurityService springSecurityService = Holders.applicationContext.getBean(GestioneDocumentiSpringSecurityService)

        // da' errore se non viene trovato l'utente,
        // carica automaticamente tutto quanto dell'utente.
        springSecurityService.reauthenticate(utente)

        // trovo l'ente con l'amministrazione richiesta:
        Ente ente = Ente.get(idEnte)

        // se non ho un ente da selezionare, ne imposto uno di default
        GestioneDocumentiMultiEnteService multiEnteService = Holders.applicationContext.getBean(GestioneDocumentiMultiEnteService)
        multiEnteService.selezionaEnte(new DefaultEnte(ente.id, ente.descrizione))

        // In questo punto potrei avere una transazione e una sessione hibernate già attiva ma senza i filtri di default attivi
        // perché l'ente non era disponibile al momento della creazione della sessione hibernate.
        // Quindi tento di riabilitare i filtri di default:
        ResolvableFilterDefinition.enableDefaultFilters(Holders.getApplicationContext().getBean(SessionFactory).getCurrentSession())
    }

	static String[] eseguiAutenticazione (String utente) {
        List<Ente> ente = Ente.findAllByValidoAndInterno(true, true,  [sort:'sequenza', order:'asc'])
		eseguiAutenticazione(utente, ente[0].id)
		return ente.collect { it.amministrazione.codice }
	}

	static void setAmministrazioneOttica (String amministrazione = null, String ottica = null) {
		// TODO[SPRINGBOOT]
	}

    static Date getCurrentDate() {
        return new Date(new Sql(Holders.getApplicationContext().getBean(DataSource)).firstRow('select sysdate as DATA from dual').DATA.getTime())
    }
}

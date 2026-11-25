package it.finmatica.gestionedocumenti.commons

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.login.TokenManager
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource

/** Servizio che fornisce token per gestire sessioni concorrenti su risorse generiche.
 * 	Definisce metodi per ottenere, aggiornre ed eliminare i token di sessione.
 * 	Può operare sia con storico dei token (usando lo stato del token per indicare le sessioni chiuse)
 * 	sia senza, semplicemente eliminando i token una volta che la risorsa è rilasciata.*/
@Service
class TokenIntegrazioneService {

    @Qualifier("ad4DataSource")
    @Autowired
    DataSource ad4DataSource

    // -------------------------- Metodi Generici --------------------------
    /** Ritorna token per autenticazione AD4*/
    @Transactional(value = "transactionManagerAd4", readOnly = true)
    String getTokenAutenticazioneAd4(String username) {
        return new TokenManager(DataSourceUtils.getConnection(ad4DataSource), username).getToken()
    }


    // --------------- Metodi di gestione diretta dei token ----------------

    /** Se per la risorsa indicata è già presente un token del tipo indicato, ritorna quello se questo è in stato SUCCESSO.
     * 	Altrimenti se il token esiste ma in un altro stato (stato IN_CORSO/ERRORE) solleva un eccezione.
     * 	Se non esiste un token per la risorsa indicata, lo crea.*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione beginTokenTransaction(String idRiferimento, String tipo) {
        // per prima cosa cerco se ce ne è già uno:
        TokenIntegrazione token = getToken(idRiferimento, tipo)

        if (token != null) {
            if (token.stato == TokenIntegrazione.STATO_SUCCESSO) {
                return token
            } else {
                // in qualunque altro caso, sta succedendo qualcosa di anomalo, blocco tutto e lo segnalo:
                throw new GestioneDocumentiRuntimeException("ATTENZIONE: Non è possibile procedere. Trovato token ${token.id} in stato ${token.stato} per l'id riferimento ${token.idRiferimento}. Contattare l'assistenza.")
            }
        }

        token = new TokenIntegrazione(idRiferimento: idRiferimento, tipo: tipo, stato: TokenIntegrazione.STATO_IN_CORSO)
        token.save()

        return token
    }

    /** Rimuove il token di sessione con stato != SUCCESSO del tipo indicato sulla risorsa indicata.*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione stopTokenTransaction(String idRiferimento, String tipo) {
        TokenIntegrazione token = TokenIntegrazione.findByIdRiferimentoAndTipoAndStatoNot(idRiferimento, tipo, TokenIntegrazione.STATO_SUCCESSO)
        token?.delete()
        return token
    }

    /** Rimuove il token di sessione del tipo indicato sulla risorsa indicata.*/
    @Transactional
    TokenIntegrazione endTokenTransaction(String idRiferimento, String tipo) {
        TokenIntegrazione token = TokenIntegrazione.findByIdRiferimentoAndTipo(idRiferimento, tipo)
        token?.delete()
        return token
    }


    /** Aggiorna lo stato del token di sessione del tipo indicato, sulla risorsa indicata, a SUCCESSO. Inoltre persiste nel token i dati passati.*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione setTokenSuccess(String idRiferimento, String tipo, String dati) {
        TokenIntegrazione token = getToken(idRiferimento, tipo)
        if (token == null) {
            throw new GestioneDocumentiRuntimeException("ATTENZIONE: Non è possibile procedere. Nessun token trovato in fase di ultimazione per l'id riferimento ${idRiferimento} e il tipo ${tipo}. Contattare l'assistenza.")
        }

        token.stato = TokenIntegrazione.STATO_SUCCESSO
        token.dati = dati
        token.save()
        return token
    }

    /** Aggiorna lo stato del token di sessione del tipo indicato, sulla risorsa indicata, ad ERRORE. Inoltre persiste nel token i dati passati.*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione setTokenError(String idRiferimento, String tipo, String dati) {
        TokenIntegrazione token = getToken(idRiferimento, tipo)
        if (token == null) {
            throw new GestioneDocumentiRuntimeException("ATTENZIONE: Non è possibile procedere. Nessun token trovato per l'id riferimento ${idRiferimento} e il tipo ${tipo}. Contattare l'assistenza.")
        }

        token.stato = TokenIntegrazione.STATO_ERRORE
        token.dati = dati
        token.save()
        return token
    }

    /** Ritorna il token per la sessione del tipo indicato sulla risorsa indicata.*/
    @Transactional(readOnly = true)
    TokenIntegrazione getToken(String idRiferimento, String tipo) {
        return TokenIntegrazione.findByIdRiferimentoAndTipo(idRiferimento, tipo)
    }

    /** Ritorna il token attivo (stato IN_CORSO) per la sessione del tipo indicato sulla risorsa indicata.*/
    @Transactional(readOnly = true)
    TokenIntegrazione getTokenAttivo(String idRiferimento, String tipo) {
        return TokenIntegrazione.findByIdRiferimentoAndTipoAndStato(idRiferimento, tipo, TokenIntegrazione.STATO_IN_CORSO)
    }

    /** Rimuove i token di sessione del tipo indicato sulla risorsa indicata più vecchi di 2 giorni.*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione rimuoviVecchioToken(String idRiferimento, String tipo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        Date oldDate = cal.getTime();

        TokenIntegrazione token = TokenIntegrazione.findByIdRiferimentoAndTipoAndDateCreatedLessThan(idRiferimento, tipo, oldDate)
        token?.delete()
        return token
    }

    /** Rimuove il token per la sessione del tipo indicato sulla risorsa indicata*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione rimuoviToken(String idRiferimento, String tipo) {
        TokenIntegrazione token = TokenIntegrazione.findByIdRiferimentoAndTipo(idRiferimento, tipo)
        token?.delete()
        return token
    }

    /** Rimuove TUTTI i token per le sessioni del tipo indicato nello stato indicato.
     * Se lo stato non è indicato rimuove TUTTI i token del tipo indicato, ignorando lo stato*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    List<TokenIntegrazione> rimuoviTuttiToken(String tipo, String stato = "%") {
        List<TokenIntegrazione> tokens = TokenIntegrazione.findAllByTipoAndStatoLike(tipo, stato)
        tokens*.delete()
        return tokens
    }

}

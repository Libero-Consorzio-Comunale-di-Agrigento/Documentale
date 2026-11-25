package it.finmatica.gestionedocumenti.registri
import org.springframework.stereotype.Service

import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import org.springframework.transaction.annotation.Transactional

import java.sql.SQLException

@Transactional
@Service
class RegistroService {

    void numera(TipoRegistro tipoRegistro, Date data, Closure assegnaNumero) {

        try {

            Registro registro = getRegistroAperto(tipoRegistro, data)
            registro.ultimoNumero = registro.ultimoNumero + 1;
            registro.dataUltimoNumero = Calendar.getInstance().getTime()
            registro.save()

            assegnaNumero(registro.ultimoNumero, registro.anno, registro.dataUltimoNumero, registro)
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    void numera(TipoRegistro tipoRegistro, Closure assegnaNumero) {
        numera(tipoRegistro, Calendar.getInstance().getTime(), assegnaNumero)
    }

    /**
     * Ritorna il registro valido per la data richiesta.
     *
     * Se il registro richiesto è presente ed è valido, viene ritornato.
     * Se il registro richiesto è presente ma non valido, viene restituito errore.
     * Se il registro richiesto non è presente ed è configurato per essere creato automaticamente, viene creato e ritornato, altrimenti viene lanciato un errore.
     *
     * @param tipoRegistro
     * @param data
     * @return
     */
    Registro getRegistroAperto(TipoRegistro tipoRegistro, Date data) {

        try {

            // l'anno per cui voglio il registro
            int annoRegistro = data.toCalendar().get(Calendar.YEAR)

            // cerco il registro aperto per l'anno richiesto
            Registro registro = (Registro) Registro.createCriteria().get {
                eq("tipoRegistro", tipoRegistro)
                eq("anno", annoRegistro)
                eq("aperto", true)
                delegate.lock(true)    // utilizzo la select for update
            }

            // se il registro non esiste, lo creo se possibile altrimenti do' errore
            if (registro == null) {
                if (tipoRegistro.rinnovoAutomatico) {
                    return rinnovaRegistro(tipoRegistro, annoRegistro)
                } else {
                    throw new GestioneDocumentiRuntimeException("Non esiste nessun registro aperto con codice ${tipoRegistro.codice} per l'anno ${annoRegistro}. Il tipo di registro non è rinnovabile automaticamente.")
                }
            }

            // infine, se il registro è presente e aperto, lo ritorno
            return registro

        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    Registro rinnovaRegistro(TipoRegistro tipoRegistro, int anno) {
        chiudiUltimoRegistro(tipoRegistro)

        return apriRegistro(tipoRegistro, anno, 0, new Date())
    }

    Registro apriRegistro(TipoRegistro tipoRegistro, int anno, int numero, Date data) {

        try {
            return new Registro(tipoRegistro: tipoRegistro
                    , anno: anno
                    , ultimoNumero: numero
                    , valido: true
                    , dataUltimoNumero: data).save()
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    Registro chiudiRegistro(TipoRegistro tipoRegistro, int anno) {

        try {
            Registro registro = Registro.createCriteria().get {
                eq("tipoRegistro", tipoRegistro)
                eq("valido", true)
                eq("anno", anno)
                lock true    // utilizzo la select for update
            }

            if (registro != null) {
                registro.valido = false;
                registro.save()
            }

            return registro
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    private Registro chiudiUltimoRegistro(TipoRegistro tipoRegistro) {

        try {

            // se il registro non ha la chiusura automatica, non lo chiudo.
            if (!tipoRegistro.chiusuraAutomatica) {
                return null;
            }

            int ultimoAnno = Registro.createCriteria().get {
                projections {
                    max("anno")
                }
                eq("tipoRegistro", tipoRegistro)
                eq("valido", true)
            } ?: -1

            // se non c'è nessun registro da chiudere:
            if (ultimoAnno < 0)
                return null;

            return chiudiRegistro(tipoRegistro, ultimoAnno);

        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    Registro riapriRegistro(Registro registro) {
        try {
            Registro r = Registro.createCriteria().get {
                eq("anno", registro.anno)
                eq("tipoRegistro", registro.tipoRegistro)
                eq("valido", true)
            }

            if (r != null) {
                throw new GestioneDocumentiRuntimeException("Non è possibile riaprire il registro richiesto perché esiste già un registro valido per l'anno ${registro.anno}.")
            }

            registro.valido = true
            registro.save(failOnError: true)

            return registro
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    List<TipoRegistroDTO> tipiRegistroValidiConRegistroAperto() {
        return Registro.createCriteria().listDistinct {
            projections {
                property("tipoRegistro")
            }

            eq('aperto', true)

            tipoRegistro {
                order('commento', 'asc')
                eq("valido", true)
            }
        }.toDTO()
    }

}

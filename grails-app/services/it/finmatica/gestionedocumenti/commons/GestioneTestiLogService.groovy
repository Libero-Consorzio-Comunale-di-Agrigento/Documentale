package it.finmatica.gestionedocumenti.commons

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Transactional
@Service
class GestioneTestiLogService {

    @Autowired
    GestioneTestiLogRepository gestioneTestiLogRepository

    List<GestioneTestiLog> ricerca(long idDocumento, Date dal, Date al) {
        return gestioneTestiLogRepository.ricerca(idDocumento, dal, al)
    }

    /**
     * Da inserire nel finally del metodo che converte il pdf o che esegue l'operazione che si sta loggando
     *
     * @param log
     */
    void log(GestioneTestiLog log) {

        log.applicativo = log.documento.applicativo
        log.estremiDocumento = log.documento.estremi

        gestioneTestiLogRepository.saveAndFlush(log)
    }

    void delete(List<GestioneTestiLog> logs) {
        gestioneTestiLogRepository.delete(logs)
    }
}

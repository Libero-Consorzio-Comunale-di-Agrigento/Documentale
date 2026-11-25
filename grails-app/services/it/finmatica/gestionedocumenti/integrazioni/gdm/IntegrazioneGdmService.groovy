package it.finmatica.gestionedocumenti.integrazioni.gdm

import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import it.finmatica.dmServer.management.Profilo
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.jdmsutil.data.ProfiloVersionExtend
import it.finmatica.smartdoc.api.DocumentaleService
import it.finmatica.so4.login.So4SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp

@Slf4j
@Service
@Transactional
@CompileStatic
class IntegrazioneGdmService {

    @Autowired
    private So4SpringSecurityService springSecurityService

    @Qualifier("dataSource_gdm")
    @Autowired
    private DataSource dataSource_gdm

    @Autowired
    private DocumentaleService documentaleService

    /**
     * Ottiene l'ultima versione del documento da GDM
     *
     * @param documento
     * @return il numero dell'ultima versione del documento su gdm.
     */
    long getUltimaRevisioneDocumento(Documento documento) {
        it.finmatica.smartdoc.api.struct.Documento smartDoc = getDocumentoSmartDoc(documento)
        return Long.parseLong(smartDoc.getUltimaVersione())
    }

    /**
     * Ottiene un file dallo storico.
     * @deprecated
     * Questa funzione era utile per lo storico JSON che va eliminato.
     * Utilizzare la funzione "getFileStorico" che ha in input anche l'id del file
     *
     * @param idDocumentoEsterno
     * @param revisione
     * @param filename
     * @return
     */
    @Deprecated
    @Transactional(readOnly = true)
    InputStream getFileStorico(long idDocumentoEsterno, long revisione, String filename) {
        it.finmatica.smartdoc.api.struct.Documento documentoSmartDoc = getDocumentoSmartDoc(idDocumentoEsterno, revisione?.toString())
        return documentaleService.getFile(documentoSmartDoc, new it.finmatica.smartdoc.api.struct.File(nome: filename, versione: revisione?.toString())).getInputStream()
    }

    @Transactional(readOnly = true)
    it.finmatica.smartdoc.api.struct.File getFileStorico(long idDocumentoEsterno, Long revisione, long idFile) {
        it.finmatica.smartdoc.api.struct.Documento documentoSmartDoc = getDocumentoSmartDoc(idDocumentoEsterno, revisione?.toString())
        return documentaleService.getFile(documentoSmartDoc, new it.finmatica.smartdoc.api.struct.File(id: idFile.toString(), versione: revisione?.toString()))
    }

    private it.finmatica.smartdoc.api.struct.Documento getDocumentoSmartDoc(Documento documento) {
        return getDocumentoSmartDoc(documento.idDocumentoEsterno)
    }

    private it.finmatica.smartdoc.api.struct.Documento getDocumentoSmartDoc(long idDocumentoEsterno, String versione = null) {
        it.finmatica.smartdoc.api.struct.Documento documentoSmart = new it.finmatica.smartdoc.api.struct.Documento(id: idDocumentoEsterno.toString(), versione: versione)
        documentoSmart.addChiaveExtra("ESCLUDI_CONTROLLO_COMPETENZE", "Y")
        it.finmatica.smartdoc.api.struct.Documento documentoSmartDoc = documentaleService.getDocumento(documentoSmart, [it.finmatica.smartdoc.api.struct.Documento.COMPONENTI.VERSIONI])
        if (documentoSmartDoc == null) {
            throw new GestioneDocumentiRuntimeException("Non ho trovato il documento su SmartDoc con id: ${idDocumentoEsterno}")
        }

        return documentoSmartDoc
    }

    // TODO: utilizzata in un unico punto su Protocollo ma forse questa va bene che sia qui.
    int getIdQueryByCodiceAds(String codiceAds) {
        try {
            Sql sql = new Sql(dataSource_gdm)

            String select = """SELECT max(id_query) ID
                                 FROM query, documenti docu
                                WHERE query.codiceads = :CODICEADS
                                  AND docu.id_documento = query.id_documento_profilo
                                  AND docu.stato_documento NOT IN ('CA', 'RE')"""

            Map params = [CODICEADS: codiceAds]
            int idQuery = (int) sql.rows(select, params)[0].ID
            return idQuery
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    String getDenominazioneCorr(String idDocumentoEsterno) {

        try {
            String denom = ""
            Connection conn = DataSourceUtils.getConnection(dataSource_gdm)
            Sql sql = new groovy.sql.Sql(conn)
            sql.call("""BEGIN 
                          ? := AG_UTILITIES_CRUSCOTTO.GET_DENOMINAZIONE_CORR (?);
                        END; """,
                    [Sql.VARCHAR, idDocumentoEsterno]) {

                row -> denom = row
            }

            return denom
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

}

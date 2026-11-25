package it.finmatica.gestionedocumenti.commons

import it.finmatica.gestionedocumenti.impostazioni.Impostazione
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

import groovy.sql.Sql
import it.finmatica.ad4.config.Ad4Properties
import it.finmatica.ad4.security.SpringSecurityService
import org.springframework.jdbc.datasource.DataSourceUtils

import javax.sql.DataSource
import java.sql.Connection

@Service
@Transactional
class Ad4Service {

    private @Qualifier("ad4DataSource") @Autowired DataSource ad4DataSource
    private @Autowired DataSource dataSource
    private @Autowired SpringSecurityService springSecurityService
    private @Autowired Ad4Properties ad4Properties

    void logAd4(String note, String testoMsg) {
        String testo = testoMsg
        String istanza = ad4Properties.istanza;
        String modulo = ad4Properties.modulo;
        String utente = springSecurityService.getCurrentUser().id;
        Connection conn = DataSourceUtils.getConnection(dataSource);
        String utenteDb = conn.metaData.userName;
        Sql sql = new Sql(ad4DataSource);
        sql.call("{ $Sql.NUMERIC = call ad4_evento.INSERT_EVENTO ($testo, $utenteDb, to_char(sysdate, 'dd/mm/yyyy hh24:mi:ss'), 0, 'I', 'APPTRC', $note, $utente, $modulo, $istanza) }");
    }

    void eventoLogAd4(String testoMsg, String idEntita = null, String annotazioni = null, String tipoEntita = "PROTOCOLLO", def documento = null, String urlEntita = null, String entita = null){

        Impostazione attivaLog = Impostazione.getImpostazione("ATTIVA_LOG", springSecurityService.getPrincipal().getEnte()).get()
        //Aggiungere annotazione e metterci gli id dei sotto oggetti (ALLEGATO/ FILE) e il testo lasciarlo parlante riporatando sempre anno numero e registro del prot
        if(attivaLog?.valore == "Y"){
            String testo = testoMsg
            String istanza = ad4Properties.istanza;
            String modulo = ad4Properties.modulo;
            String utente = springSecurityService.getCurrentUser().id;
            Connection conn = DataSourceUtils.getConnection(dataSource);
            String utenteDb = conn.metaData.userName;
            Sql sql = new Sql(ad4DataSource);
            sql.call("{ $Sql.NUMERIC = call ad4_evento.INSERT_EVENTO_LOG ($testo, $utenteDb, to_char(sysdate, 'dd/mm/yyyy hh24:mi:ss'), 0, 'I', 'APPTRC', ${annotazioni}, $utente, $modulo, $istanza, $tipoEntita, $idEntita, $urlEntita, $entita) }")

        }
    }
}

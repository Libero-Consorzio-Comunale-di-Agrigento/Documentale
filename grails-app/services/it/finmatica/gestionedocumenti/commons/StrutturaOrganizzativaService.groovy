package it.finmatica.gestionedocumenti.commons

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.so4.struttura.So4Ottica
import it.finmatica.so4.struttura.So4SuddivisioneStruttura
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubbService
import it.finmatica.so4.strutturaPubblicazione.So4RuoloComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StrutturaOrganizzativaService {

    public static final String TIPO_ASSEGNAZIONE = "%"
    public static final String ASSEGNAZIONE_PREVALENTE = "%"

    private @Autowired
    So4ComponentePubbService so4ComponentePubbService

    /**
     * Ritorna TRUE se il soggetto ha il ruolo richiesto per l'unità richiesta alla data odierna.
     * Ritorna TRUE se il soggetto ha un qualunque ruolo (parametro ruolo = null) per l'unità richiesta in data odierna.
     * Ritorna FALSE in tutti gli altri casi.
     *
     * @param niSoggetto NI del soggetto da cercare
     * @param ruolo il codice del ruolo, può essere NULL in tal caso sarà sufficiente che l'utente abbia un ruolo qualunque per l'unità
     * @param progr il progressivo dell'unità. Può essere -1, in tal caso sarà sufficiente che l'utente abbia il ruolo specificato in una qualunque unità.
     * @return
     */
    boolean soggettoHaRuoloPerUnita(long niSoggetto, String ruolo, long progr, String codiceOttica, Long idComponente = null) {
        Date date = new Date().clearTime()
        int count = So4RuoloComponentePubb.createCriteria().count {
            componente {
                eq("soggetto.id", niSoggetto)

                if(idComponente != null){
                    eq("id", idComponente)
                }

                if (progr > 0) {
                    eq("progrUnita", progr)
                    le("dal", date)
                    or {
                        isNull("al")
                        ge("al", date)
                    }
                }

                eq("ottica.codice", codiceOttica)
            }

            if (ruolo != null) {
                eq("ruolo.ruolo", ruolo)
            }

            le("dal", date)
            or {
                isNull("al")
                ge("al", date)
            }
        }

        return (count > 0)
    }

    /**
     * Ritorna il componente dato l'utenteAd4 e l'unità di appartenenza.
     *
     * @param utenteAd4 utente del componente
     * @param unitaSo4 unità di appartenenza
     * @return il componente trovato oppure null
     */
    So4ComponentePubb getComponente(Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) {
        def componenti = So4ComponentePubb.componentiUnitaPubb(unitaSo4.progr, unitaSo4.ottica.codice, unitaSo4.al ?: new Date()) {
            soggetto {
                eq("utenteAd4.id", utenteAd4.id)
            }
        }

        if (componenti.size() == 0)
            return null

        return componenti[0]
    }

    /**
     * Ritorna tutte le UO valide alla data richiesta (default sysdate) a cui appartiene un soggetto.
     *
     * @param soggettoAs4 l'id del soggetto da cercare
     * @param codiceOttica il codice dell'ottica
     * @return ritorna l'elenco delle So4UnitaPubb trovate.
     */
    List<So4UnitaPubb> getUnitaSoggetto(long idSoggetto, String codiceOttica, Date date = new Date()) {
        return So4ComponentePubb.executeQuery("""
			select uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
			 where c.progrUnita = uo.progr
			   and uo.ottica.codice = c.ottica.codice
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )
			   and c.soggetto.id = :idSoggetto
			   and c.ottica.codice = :codiceOttica
			 order by uo.descrizione asc
			""", [dataRif: date, idSoggetto: idSoggetto, codiceOttica: codiceOttica])
    }

    /**
     * Ritorna tutte le UO padri fino alla radice di una certa UO
     *
     * @param soggettoAs4 l'id del soggetto da cercare
     * @param codiceOttica il codice dell'ottica
     * @return ritorna l'unità di partenza e tutte le sue unità padri fino al vertice
     */
    List<So4UnitaPubb> getUnitaPadri(long progrUo, String codiceOttica, Date date = new Date()) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, date).get()
        def listaUo = []
        while (uo != null) {
            listaUo << uo
            uo = uo.unitaPubbPadre
        }

        return listaUo
    }

    /**
     * Ritorna tutte le UO  di una certa UO
     *
     * @param codiceOttica il codice dell'ottica
     * @return ritorna tutte le unità che appartengono all'ottica
     */
    List<So4UnitaPubb> getUnitaInOttica(String codiceOttica, Date date = new Date()) {
        return So4UnitaPubb.allaData(date).perOttica(codiceOttica).list()
    }

    /**
     * Ritorna i componenti che hanno il ruolo specificato per un certo utente all'interno dell'ottica ordinati per assegnazione prevalente ASC.
     *
     * @param utenteAd4 il codice dell'utente ad4
     * @param codiceRuolo il codice del ruolo
     * @param codiceOttica il codice dell'ottica
     * @return i componenti relativi all'utente per l'ottica con il ruolo richiesto Ordinate per Assegnazione Prevalente ASC.
     */
    List<So4ComponentePubb> getComponentiPerUtenteConRuoloInOttica(String utenteAd4, String codiceRuolo, String codiceOttica, Date date = new Date()) {
        return So4ComponentePubb.executeQuery("""
			select c
			  from So4ComponentePubb 		c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponente 		a
			 where rc.ruolo.ruolo = :ruolo
			   and rc.dal <= trunc(:dataRif)
			   and (rc.al is null or rc.al >= trunc(:dataRif))
			   and rc.componente  = c
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif))
			   and c.soggetto.utenteAd4.id = :utenteAd4
 			   and a.componente = c
			   and (a.al is null or a.al >= trunc(:dataRif))
   			   and a.dal <= trunc(:dataRif)
			   and c.ottica.codice = :ottica
		  order by a.assegnazionePrevalente asc
""", [dataRif: date, ruolo: codiceRuolo, ottica: codiceOttica, utenteAd4: utenteAd4])
    }

    /**
     * Ritorna tutte le UO valide alla data richiesta (default sysdate) a cui appartiene un utente.
     *
     * @param utenteAd4 il codice dell'utente ad4
     * @param codiceOttica il codice dell'ottica
     * @return ritorna l'elenco delle So4UnitaPubb trovate.
     */
    List<So4UnitaPubb> getUnitaUtente(String utenteAd4, String codiceOttica, Date date = new Date()) {
        return So4ComponentePubb.executeQuery("""
			select uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
			 where c.progrUnita = uo.progr
			   and uo.ottica.codice = c.ottica.codice
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif))
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif))
			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica.codice = :codiceOttica
			 order by uo.descrizione asc
			""", [dataRif: date, utenteAd4: utenteAd4, codiceOttica: codiceOttica])
    }

    /**
     *
     * Ritorna tutte le UO valide alla data richiesta (default sysdate) a cui appartiene un utente con ruolo attivo e che inizi con un particolare prefisso.
     *
     * @param idSoggetto
     * @param codiceOttica
     * @param date
     * @param filtroRuolo
     * @return
     */
    List<So4UnitaPubb> getUnitaSoggettoConPrefissoRuolo(String utenteAd4, String codiceOttica, String filtroRuolo, Date date = new Date()) {

        if (!filtroRuolo) filtroRuolo = ""

        return So4ComponentePubb.executeQuery("""
			select distinct uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
			     , So4RuoloComponentePubb rc
			 where c.progrUnita = uo.progr
			   and uo.ottica.codice = c.ottica.codice
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )
			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica.codice = :codiceOttica
			   and rc.ruolo.ruolo like :filtroRuolo
			   and rc.dal <= trunc(:dataRif)
               and (rc.al is null or rc.al >= trunc(:dataRif) )
               and rc.componente = c
			 order by uo.descrizione asc
			""", [dataRif: date, utenteAd4: utenteAd4, codiceOttica: codiceOttica, filtroRuolo: filtroRuolo + "%"])
    }

    /**
     * Ricerca tutte le UO valide alla data richiesta (default sysdate) a cui appartiene un utente con una descrizione che contiene la stringa data
     *
     * @param utenteAd4 il codice dell'utente ad4
     * @param codiceOttica il codice dell'ottica
     * @return ritorna l'elenco delle So4UnitaPubb trovate.
     */
    List<So4UnitaPubb> ricercaUnitaUtente(String utenteAd4, String codiceOttica, Date date = new Date(), String filtro) {

        if (!filtro) filtro = ""

        return So4ComponentePubb.executeQuery("""
			select uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
			 where c.progrUnita = uo.progr
			   and uo.ottica.codice = c.ottica.codice
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )
			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica.codice = :codiceOttica
			   and upper(uo.descrizione)	like upper(:filtro) 
			 order by uo.descrizione asc
			""", [dataRif: date, utenteAd4: utenteAd4, codiceOttica: codiceOttica, filtro: "%" + filtro + "%"])

    }

    /**
     * Ritorna tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
     *
     * @param utenteAd4 il codice dell'utenteAd4
     * @param codiceRuolo il codice del ruolo
     * @param codiceOttica il codice dell'ottica
     * @return tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
     */
    List<So4UnitaPubb> getUnitaUtenteConRuolo(String utenteAd4, String codiceRuolo, String codiceOttica, Date date = new Date(), String assegnazionePrevalente = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE, String tipoAssegnazione = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE) {
        return So4ComponentePubb.executeQuery("""
			select distinct uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponentePubb 	att
			 where uo.ottica.codice = :codiceOttica
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )

			   and c.progrUnita = uo.progr
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
   			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica = uo.ottica

			   and att.componente 						= c
			   and att.assegnazionePrevalente			like :assegnazionePrevalente
			   and att.tipoAssegnazione		    		like :tipoAssegnazione
			   and att.dal <= trunc(:dataRif)
			   and (att.al is null or att.al >= trunc(:dataRif) )

			   and rc.ruolo.ruolo = :ruolo
			   and rc.dal <= trunc(:dataRif)
			   and (rc.al is null or rc.al >= trunc(:dataRif) )
			   and rc.componente = c
			 order by uo.descrizione asc
			""", [dataRif: date, ruolo: codiceRuolo, utenteAd4: utenteAd4, codiceOttica: codiceOttica, assegnazionePrevalente: assegnazionePrevalente, tipoAssegnazione: tipoAssegnazione])
    }

    /**
     * Ritorna tutte le Unità di un'ottica che contiengono almeno un utente con il ruolo richiesto
     *
     * @param codiceRuolo il codice del ruolo
     * @param codiceOttica il codice dell'ottica
     * @return tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
     */
    List<So4UnitaPubb> getUnitaConRuolo(String codiceRuolo, String codiceOttica, Date date = new Date(), String assegnazionePrevalente = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE, String tipoAssegnazione = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE) {
        return So4ComponentePubb.executeQuery("""
			select distinct uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponentePubb 	att
			 where uo.ottica.codice = :codiceOttica
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )

			   and c.progrUnita = uo.progr
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
			   and c.ottica = uo.ottica

			   and att.componente 						= c
			   and att.assegnazionePrevalente			like :assegnazionePrevalente
			   and att.tipoAssegnazione		    		like :tipoAssegnazione
			   and att.dal <= trunc(:dataRif)
			   and (att.al is null or att.al >= trunc(:dataRif) )

			   and rc.ruolo.ruolo = :ruolo
			   and rc.dal <= trunc(:dataRif)
			   and (rc.al is null or rc.al >= trunc(:dataRif) )
			   and rc.componente = c
			 order by uo.descrizione asc
			""", [dataRif: date, ruolo: codiceRuolo, codiceOttica: codiceOttica, assegnazionePrevalente: assegnazionePrevalente, tipoAssegnazione: tipoAssegnazione])
    }

    /**
     * Ricerca tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
     *
     * @param utenteAd4 il codice dell'utenteAd4
     * @param codiceRuolo il codice del ruolo
     * @param codiceOttica il codice dell'ottica
     * @return tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
     */
    List<So4UnitaPubb> ricercaUnitaUtenteConRuolo(String utenteAd4, String codiceRuolo, String codiceOttica, Date date = new Date(), String assegnazionePrevalente = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE, String tipoAssegnazione = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE, String filtro) {

        Map<String, String> parametri = [dataRif               : date,
                                         utenteAd4             : utenteAd4,
                                         codiceOttica          : codiceOttica,
                                         assegnazionePrevalente: assegnazionePrevalente,
                                         tipoAssegnazione      : tipoAssegnazione,
                                         filtro                : "%" + filtro + "%"]
        boolean ruoloPresente = !StringUtils.isEmpty(codiceRuolo)
        if (ruoloPresente) {
            parametri.putAll([ruolo: codiceRuolo])
        }
        return So4ComponentePubb.executeQuery("""
			select distinct uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponentePubb 	att
			 where uo.ottica.codice = :codiceOttica
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )
			   and ( ( upper(uo.descrizione) like upper(:filtro) ) or ( upper(uo.codice) like upper(:filtro) ) ) 
			   
			   and c.progrUnita = uo.progr
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
   			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica = uo.ottica

			   and att.componente 						= c
			   and att.assegnazionePrevalente			like :assegnazionePrevalente
			   and att.tipoAssegnazione		    		like :tipoAssegnazione
			   and att.dal <= trunc(:dataRif)
			   and (att.al is null or att.al >= trunc(:dataRif) )"""
                + (ruoloPresente ? "and rc.ruolo.ruolo = :ruolo" : "") +
                """
			   and rc.dal <= trunc(:dataRif)
			   and (rc.al is null or rc.al >= trunc(:dataRif) )
			   and rc.componente = c
			 order by uo.descrizione asc
			""", parametri)
    }


    /**
     *  Restituisce true o false a seconda che l'utente in input abbia il ruolo definito nella tabella Impostazioni per il codice in input
     *  su almeno un'unità dell'ottica di default definita sempre nella tabella delle Impostazioni
     *
     * @param utenteAd4
     * @param codiceXOtticaSuImpostazioni
     * @param codiceXRuoloSuImpostazioni
     * @return
     */
    boolean utenteHasRuoloDaImpostazioni(String utenteAd4, String codiceXOtticaSuImpostazioni, String codiceXRuoloSuImpostazioni, Date date = new Date()) {
        def tot = So4UnitaPubb.executeQuery("""
			select count(*)
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
                 , Impostazione i1
				 , Impostazione i2
			 where i1.codice = :codiceXOtticaSuImpostazioni
               and i2.codice = :codiceXRuoloSuImpostazioni

               and uo.ottica.codice = i1.valore
			   and uo.dal <= trunc(:dataRif)
			   and (uo.al is null or uo.al >= trunc(:dataRif) )

			   and c.progrUnita = uo.progr
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
   			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica = uo.ottica

			   and rc.ruolo.ruolo = i2.valore
			   and rc.dal <= trunc(:dataRif)
			   and (rc.al is null or rc.al >= trunc(:dataRif) )
			   and rc.componente = c
			""", [dataRif: date, codiceXOtticaSuImpostazioni: codiceXOtticaSuImpostazioni, utenteAd4: utenteAd4, codiceXRuoloSuImpostazioni: codiceXRuoloSuImpostazioni])
        return (tot[0] > 0)
    }


    /**
     *  Ricerca, attraverso la stringa in input,  i componenti dell'ottica passata come parametro aventi il ruolo indicato alla data indicata
     *
     * @param ruolo codice o istanza del ruolo da cercare
     * @param ottica ottica di appartenenza dell'unità e quindi del componente
     * @param dataRif data di riferimento per la ricerca del componente e del ruolo; se null, verrà gestita come sysdate.
     * @return
     */
    List<So4ComponentePubb> ricercaComponentiPubbOtticaConRuolo(ruolo, ottica, String filtro) {

        String assegnazionePrevalente = StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE
        String tipoAssegnazione = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE
        Date dataRif = new Date()

        Ad4Ruolo ad4Ruolo = (ruolo instanceof Ad4Ruolo ? ruolo : Ad4Ruolo.get(ruolo))

        So4Ottica so4Ottica = (ottica instanceof So4Ottica ? ottica : So4Ottica.get(ottica))

        return So4ComponentePubb.executeQuery("""
	   select c
		  from So4ComponentePubb 					c
			 , So4RuoloComponentePubb 				rc
	         , So4AttrComponentePubb	 			att
		 where rc.ruolo = :ruolo
		   and rc.componente 						= c
		   and rc.dal <= trunc(:dataRif)
		   and (rc.al is null or rc.al >= trunc(:dataRif) )
		   and c.dal <= trunc(:dataRif)
		   and (c.al is null or c.al >= trunc(:dataRif) )
	   	   and att.componente 						= c
	   	   and att.assegnazionePrevalente			like :assegnazionePrevalente
	       and att.tipoAssegnazione		    		like :tipoAssegnazione
		   and upper(c.nominativoSoggetto)     		like upper(:filtro)  
	       and att.dal <= trunc(:dataRif)
	       and (att.al is null or att.al >= trunc(:dataRif) )
		   and c.ottica = :ottica""", [dataRif: dataRif, ruolo: ad4Ruolo, ottica: so4Ottica, assegnazionePrevalente: assegnazionePrevalente, tipoAssegnazione: tipoAssegnazione, filtro: "%" + filtro + "%"])
    }

    /**
     * Ritorna l'elenco di unità organizzative che appartengono all'amministrazione e all'ottica alla data indicata (opzionale, default sysdate)
     * Il risultato è ordinato alfabeticamente.
     *
     * @param codiceAmministrazione il codice dell'amministrazione
     * @param codiceOttica il codice dell'ottica
     * @param data la data a cui cercare (default: sysdate)
     * @param filtroDescrizione la stringa su cui si filtra per like %${filtro}% nel campo descrizione + codice (default null: non filtra)
     * @return l'elenco piatto ordinato alfabeticamente delle unità organizzative trovate.
     */
    List<So4UnitaPubb> cercaUnitaPubb(String codiceAmministrazione, String codiceOttica, Date data = new Date(), String filtroDescrizione = null) {
        return So4UnitaPubb.executeQuery("""select u
			 from So4UnitaPubb u 
			 where u.amministrazione.codice = :codiceAmministrazione 
			 and u.ottica.codice = :codiceOttica
			 and u.dal <= trunc(:data)
			 and (u.al is null or u.al >= trunc(:data) )
			 and ( ( upper(u.descrizione) like upper(:filtro) )  or ( upper(u.codice) like upper(:filtro)) )
             order by u.descrizione asc""", [codiceAmministrazione: codiceAmministrazione, codiceOttica: codiceOttica, data: data, filtro: "%" + filtroDescrizione + "%"])
    }

    /**
     * Ritorna tutti i componenti con un certo ruolo all'interno dell'ottica richiesta.
     *
     * Documentazione Filippo:
     * a) Visualizzare tutti i funzionari dell'ente (cioè i componenti con il ruolo in input
     *    appartenenti all'ottica passata come parametro)
     *
     * @param codiceRuolo il codice del ruolo
     * @param codiceOttica il codice dell'ottica
     * @param dataRif data di riferimento (default: sysdate)
     * @return Elenco dei componenti con il ruolo richiesto che appartengono all'ottica
     */
    List<So4ComponentePubb> getComponentiConRuoloInOttica(ruolo, String codiceOttica, Date dataRif = new Date()) {
        return so4ComponentePubbService.getComponentiPubbOtticaConRuolo(ruolo, codiceOttica, dataRif, StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE)
    }

    /**
     * Ritorna tutti i componenti con un certo ruolo all'interno dell'ottica richiesta.
     *
     * Documentazione Filippo:
     * a) Visualizzare tutti i funzionari dell'ente (cioè i componenti con il ruolo in input
     *    appartenenti all'ottica passata come parametro)
     *
     * @param codiceRuolo il codice del ruolo
     * @param codiceOttica il codice dell'ottica
     * @param dataRif data di riferimento (default: sysdate)
     * @return Elenco dei componenti con il ruolo richiesto che appartengono all'ottica
     */
    List<So4ComponentePubb> getComponentiConRuoliInOttica(Collection ruoli, String codiceOttica, Date dataRif = new Date()) {
        return so4ComponentePubbService.getComponentiPubbOtticaConRuoli(ruoli, codiceOttica, dataRif, StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE)
    }

    /**
     * Ritorna tutti i componenti con un certo ruolo all'interno di una data unità.
     *
     * Documentazione Filippo:
     * b) Visualizzare solo funzionari presenti nella uo del redattore ( cioè i componenti con un determinato ruolo
     *    presenti nella uo del del componente passato in input )
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataRif data di riferimento (default: sysdate) per estrarre componente e ruoli
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo.
     */
    List<So4ComponentePubb> getComponentiConRuoloInUnita(String codiceRuolo, long progrUo, String codiceOttica, Date dataRif = new Date()) {
        return So4ComponentePubb.executeQuery("""
			select c
			  from So4ComponentePubb 		c
				 , So4RuoloComponentePubb 	rc
				 , So4UnitaPubb				uo
			 where rc.ruolo.ruolo = :ruolo
			   and rc.dal <= trunc(:dataRif)
			   and (rc.al is null or rc.al >= trunc(:dataRif) )
			   and rc.componente = c
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
			   and c.ottica.codice = :ottica
			   and c.progrUnita = :progrUnita
			   and uo.progr = c.progrUnita
			   and uo.dal <= trunc(:dataRif)
			   and uo.ottica.codice = :ottica
               and (uo.al is null or uo.al >= trunc(:dataRif) )""", [dataRif: dataRif, ruolo: codiceRuolo, ottica: codiceOttica, progrUnita: progrUo])
    }


    /**
     * Ritorna tutti i componenti con un certo ruolo all'interno di una data unità (filtrati per nominativo).
     *
     * Documentazione Filippo:
     * b) Visualizzare solo i firmatari presenti nella uo del redattore ( cioè i componenti con un determinato ruolo
     *    presenti nella uo del del componente passato in input )
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataRif data di riferimento (default: sysdate) per estrarre componente e ruoli
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo.
     */
    List<So4ComponentePubb> ricercaComponentiConRuoloInUnita(String codiceRuolo, long progrUo, String codiceOttica, Date dataRif = new Date(), String filtro) {
        return So4ComponentePubb.executeQuery("""
			select c
			  from So4ComponentePubb 		c
				 , So4RuoloComponentePubb 	rc
				 , So4UnitaPubb				uo
			 where rc.ruolo.ruolo = :ruolo
			   and rc.dal <= :dataRif
			   and (rc.al is null or rc.al >= trunc(:dataRif) )
			   and rc.componente = c
			   and c.dal <= trunc(:dataRif)
			   and (c.al is null or c.al >= trunc(:dataRif) )
			   and c.ottica.codice = :ottica
			   and c.progrUnita = :progrUnita
			   and uo.progr = c.progrUnita
			   and uo.dal <= trunc(:dataRif)
			   and uo.ottica.codice = :ottica
			   and upper(c.nominativoSoggetto)  like upper(:filtro)
               and (uo.al is null or uo.al >= trunc(:dataRif) )""", [dataRif: dataRif, ruolo: codiceRuolo, ottica: codiceOttica, progrUnita: progrUo, filtro: "%" + filtro + "%"])
    }


    /**
     * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta e in tutta la catena dei suoi "padri" fino al vertice.
     *
     * Documentazione Filippo:
     * c) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
     *    ( componente in input ) e nella stessa codiceArea del redattore (navigando la struttura SOLO verso l'alto fino
     *    ad arrivare al vertice)
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @param fermatiAlPrimoRisultato se true, la ricerca si ferma al primo componente con il ruolo richiesto che viene trovato.
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo.
     */
    List<So4ComponentePubb> getComponentiConRuoloInUnitaPadri(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, boolean fermatiAlPrimoRisultato = false) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        List<So4ComponentePubb> elencoComponenti = []
        while (uo != null) {
            elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uo, uo.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
            if (fermatiAlPrimoRisultato && elencoComponenti.size() > 0)
                return elencoComponenti

            uo = uo.getUnitaPubbPadre(uo.al ?: new Date())
        }
        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta e in tutta la catena dei suoi "padri" fino al vertice.
     *
     * Documentazione Filippo:
     * c) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
     *    ( componente in input ) e nella stessa codiceArea del redattore (navigando la struttura SOLO verso l'alto fino
     *    ad arrivare al vertice)
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @param fermatiAlPrimoRisultato se true, la ricerca si ferma al primo componente con il ruolo richiesto che viene trovato.
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo.
     */
    List<So4ComponentePubb> ricercaComponentiConRuoloInUnitaPadri(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, String filtro, boolean fermatiAlPrimoRisultato = false) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        List<So4ComponentePubb> elencoComponenti = []
        while (uo != null) {
            elencoComponenti.addAll(ricercaComponentiConRuoloInUnita(codiceRuolo, uo.progr, uo.ottica.codice, filtro))
            if (fermatiAlPrimoRisultato && elencoComponenti.size() > 0)
                return elencoComponenti

            uo = uo.getUnitaPubbPadre(uo.al ?: new Date())
        }
        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta e in tutta la catena dei suoi "padri" fino ad arrivare a una certa suddivisione.
     *
     * Documentazione Filippo:
     * d) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
     * ( componente in input ) e nella stessa codiceArea del redattore (navigando la struttura SOLO verso l'alto ma
     * fino ad arrivare ad una suddivisione della struttura che non sia necessariamente il vertice)
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @param idSuddivisione l'id della suddivisione a cui fermarsi
     * @param fermatiAlPrimoRisultato se true, la ricerca si ferma al primo componente con il ruolo richiesto che viene trovato.
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo fino ad arrivare a una certa suddivisione.
     */
    List<So4ComponentePubb> getComponentiConRuoloInPadriFinoASuddivisione(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione, boolean fermatiAlPrimoRisultato = false) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        List<So4ComponentePubb> elencoComponenti = []
        while (uo != null) {
            elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uo, uo.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
            if ((fermatiAlPrimoRisultato && elencoComponenti.size() > 0) || uo.suddivisione.id == idSuddivisione)
                return elencoComponenti

            uo = uo.getUnitaPubbPadre(uo.al ?: new Date())
        }
        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta e in tutta la catena dei suoi "padri" fino ad arrivare a una certa suddivisione.
     * (filtrati per nominativo)
     *
     * Documentazione Filippo:
     * d) Visualizzare firmatari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
     * ( componente in input ) e nella stessa codiceArea del redattore (navigando la struttura SOLO verso l'alto ma
     * fino ad arrivare ad una suddivisione della struttura che non sia necessariamente il vertice)
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @param idSuddivisione l'id della suddivisione a cui fermarsi
     * @param fermatiAlPrimoRisultato se true, la ricerca si ferma al primo componente con il ruolo richiesto che viene trovato.
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo fino ad arrivare a una certa suddivisione.
     */
    List<So4ComponentePubb> ricercaComponentiConRuoloInPadriFinoASuddivisione(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione, String filtro, boolean fermatiAlPrimoRisultato = false) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        List<So4ComponentePubb> elencoComponenti = []
        while (uo != null) {
            elencoComponenti.addAll(ricercaComponentiConRuoloInUnita(codiceRuolo, uo.progr, uo.ottica.codice, filtro))
            if ((fermatiAlPrimoRisultato && elencoComponenti.size() > 0) || uo.suddivisione.id == idSuddivisione)
                return elencoComponenti

            uo = uo.getUnitaPubbPadre(uo.al ?: new Date())
        }
        return elencoComponenti
    }

    List<So4ComponentePubb> getComponentiConRuoloInSuddivisione(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione, boolean fermatiAlPrimoRisultato = false) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        // ottengo l'unità suddivisione "padre":
        while (uo != null && uo.suddivisione.id != idSuddivisione) {
            uo = uo.getUnitaPubbPadre(uo.al ?: new Date())
        }

        // se non ho trovato la suddivisone, esco e ritorno una lista vuota.
        if (uo == null) {
            return []
        }

        List<So4ComponentePubb> elencoComponenti = []
        // prendo tutti i componenti con i ruoli richiesti.
        visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
            elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuoli([codiceRuolo], unita, unita.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
        }

        return elencoComponenti
    }

    List<So4ComponentePubb> ricercaComponentiConRuoloInSuddivisione(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione, String filtro, boolean fermatiAlPrimoRisultato = false) {
        So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        // ottengo l'unità suddivisione "padre":
        while (uo != null && uo.suddivisione.id != idSuddivisione) {
            uo = uo.getUnitaPubbPadre(uo.al ?: new Date())
        }

        // se non ho trovato la suddivisone, esco e ritorno una lista vuota.
        if (uo == null) {
            return []
        }

        List<So4ComponentePubb> elencoComponenti = []
        // prendo tutti i componenti con i ruoli richiesti.
        visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
            elencoComponenti.addAll(ricercaComponentiConRuoloInUnita(codiceRuolo, unita.progr, unita.ottica.codice, filtro))
        }

        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta, nelle sue sorelle e in tutta la catena dei suoi "padri" fino ad arrivare a una certa suddivisione.
     *
     * Documentazione Filippo:
     * e) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
     * ( componente in input ) e nella stessa codiceArea del redattore ( presentando tutti i componenti con ruolo
     * funzionario dell'codiceArea di appartenenza del redattore quindi anche in unità di pari livello del redattore
     * ed in generale di altri rami dell'albero dell'codiceArea)
     * A posteriori si è deciso di implementare anche la possibiltà di ricercare fino ad un certa suddivisione
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @param idSuddivisione l'id della suddivisione a cui fermarsi, default: -1 (indica di ricercare per tutta l'codiceArea e non fermarsi alla suddivisione)
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo fino ad arrivare a una certa suddivisione.
     */
    List<So4ComponentePubb> getComponentiConRuoloInUnitaEPadri(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione = -1) {
        List<So4ComponentePubb> elencoComponenti = []

        // per prima cosa ottengo l'uo di partenza:
        So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        // ne prendo i componenti:
        elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uoPartenza, uoPartenza.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))

        // quindi prendo l'uo padre e i suoi componenti con ruolo richiesto:
        So4UnitaPubb uoPadre = uoPartenza.getUnitaPubbPadre(uoPartenza.al ?: new Date())

        if (uoPadre != null) {
            // prendo le unità sorelle dell'unità di partenza:
            List<So4UnitaPubb> uoSorelle = uoPadre.getUnitaPubbFiglie(uoPadre.al ?: new Date())

            // e prendo i componenti per ogni uo figlia (e quindi sorella di quella di partenza)
            for (So4UnitaPubb uo : uoSorelle) {
                // salto l'uo da cui sono partito.
                if (uo.progr == uoPartenza.progr)
                    continue

                elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uo, uo.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
            }
        }

        // infine, risalgo la catena dei padri fintanto che non arrivo al vertice o incontro la suddivisione richiesta:
        while (uoPadre != null) {
            elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uoPadre, uoPadre.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))

            if (uoPadre.suddivisione.id == idSuddivisione) {
                break
            }
            uoPadre = uoPadre.getUnitaPubbPadre(uoPadre.al ?: new Date())
        }

        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta, nelle sue sorelle e in tutta la catena dei suoi "padri" fino ad arrivare a una certa suddivisione.
     * (filtrare per nominativo)
     *
     * Documentazione Filippo:
     * e) Visualizzare firmatari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
     * ( componente in input ) e nella stessa codiceArea del redattore ( presentando tutti i componenti con ruolo
     * funzionario dell'codiceArea di appartenenza del redattore quindi anche in unità di pari livello del redattore
     * ed in generale di altri rami dell'albero dell'codiceArea)
     * A posteriori si è deciso di implementare anche la possibiltà di ricercare fino ad un certa suddivisione
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @param idSuddivisione l'id della suddivisione a cui fermarsi, default: -1 (indica di ricercare per tutta l'codiceArea e non fermarsi alla suddivisione)
     * @return elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo fino ad arrivare a una certa suddivisione.
     */
    List<So4ComponentePubb> ricercaComponentiConRuoloInUnitaEPadri(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, String filtro, long idSuddivisione = -1) {
        List<So4ComponentePubb> elencoComponenti = []

        // per prima cosa ottengo l'uo di partenza:
        So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        // ne prendo i componenti:
        elencoComponenti.addAll(ricercaComponentiConRuoloInUnita(codiceRuolo, uoPartenza.progr, uoPartenza.ottica.codice, filtro))
        // quindi prendo l'uo padre e i suoi componenti con ruolo richiesto:
        So4UnitaPubb uoPadre = uoPartenza.getUnitaPubbPadre(uoPartenza.al ?: new Date())

        if (uoPadre != null) {
            // prendo le unità sorelle dell'unità di partenza:
            List<So4UnitaPubb> uoSorelle = uoPadre.getUnitaPubbFiglie(uoPadre.al ?: new Date())

            // e prendo i componenti per ogni uo figlia (e quindi sorella di quella di partenza)
            for (So4UnitaPubb uo : uoSorelle) {
                // salto l'uo da cui sono partito.
                if (uo.progr == uoPartenza.progr)
                    continue
                elencoComponenti.addAll(ricercaComponentiConRuoloInUnita(codiceRuolo, uo.progr, uo.ottica.codice, filtro))
            }
        }

        // infine, risalgo la catena dei padri fintanto che non arrivo al vertice o incontro la suddivisione richiesta:
        while (uoPadre != null) {
            elencoComponenti.addAll(ricercaComponentiConRuoloInUnita(codiceRuolo, uoPadre.progr, uoPadre.ottica.codice, filtro))
            if (uoPadre.suddivisione?.id == idSuddivisione) {
                break
            }
            uoPadre = uoPadre.getUnitaPubbPadre(uoPadre.al ?: new Date())
        }

        return elencoComponenti
    }

    List<So4ComponentePubb> getComponentiInUnita(So4UnitaPubb unitaSo4) {
        return So4ComponentePubb.componentiUnitaPubb(unitaSo4.progr, unitaSo4.ottica.codice, unitaSo4.al ?: new Date()).list()
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     *
     * Documentazione Filippo:
     * f) Consentire la selezione dei funzionari ( componenti con il ruolo in input ) di tutta la struttura organizzativa
     * tenendo come riferimento l'unità di appartenenza del firmatario ( componente in input ) e gestendo la selezione
     * dei funzionari dalle unità di 1 livello inferiore rispetto al firmatario fino all'ultimo livello della struttura
     * (in questo caso, essendo la gestione del funzionario strettamente legata al firmatario, non potrà essere consentita
     * la selezione del funzionario fino a quando il campo del firmatario non è stato riempito).
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @return elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     */
    List<So4ComponentePubb> getComponentiConRuoloInUnitaFiglie(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal) {
        // per prima cosa ottengo l'uo di partenza:
        So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        // ottengo i componenti dell'unità su cui sono
        List<So4ComponentePubb> elencoComponenti = []
        elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uoPartenza, uoPartenza.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))

        // ciclo sulle unità figlie e le "visito" per ottenere tutti i relativi componenti e unità figlie
        List<So4UnitaPubb> unitaFiglie = uoPartenza.getUnitaPubbFiglie(uoPartenza.al ?: new Date())
        for (So4UnitaPubb uo : unitaFiglie) {
            visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
                elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, unita, unita.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
            }
        }
        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti nell'unità scelta e nelle unità figlie.
     *
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @return elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     */
    List<So4ComponentePubb> getComponentiInUnitaEFiglie(long progrUo, String codiceOttica, Date dataDal) {
        // per prima cosa ottengo l'uo di partenza:
        So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        // ottengo i componenti dell'unità su cui sono
        List<So4ComponentePubb> elencoComponenti = []
        elencoComponenti.addAll(So4ComponentePubb.componentiUnitaPubb(uoPartenza.progr, codiceOttica, uoPartenza.al ?: new Date()).list())

        // ciclo sulle unità figlie e le "visito" per ottenere tutti i relativi componenti e unità figlie
        List<So4UnitaPubb> unitaFiglie = uoPartenza.getUnitaPubbFiglie(uoPartenza.al ?: new Date())
        for (So4UnitaPubb uo : unitaFiglie) {
            visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
                elencoComponenti.addAll(So4ComponentePubb.componentiUnitaPubb(unita.progr, codiceOttica, unita.al ?: new Date()).list())
            }
        }
        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     *
     * Documentazione Filippo:
     * f) Consentire la selezione dei funzionari ( componenti con il ruolo in input ) di tutta la struttura organizzativa
     * tenendo come riferimento l'unità di appartenenza del firmatario ( componente in input ) e gestendo la selezione
     * dei funzionari dalle unità di 1 livello inferiore rispetto al firmatario fino all'ultimo livello della struttura
     * (in questo caso, essendo la gestione del funzionario strettamente legata al firmatario, non potrà essere consentita
     * la selezione del funzionario fino a quando il campo del firmatario non è stato riempito).
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @return elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     */
    List<So4ComponentePubb> getComponentiConRuoliInUnitaFiglie(ruoli, long progrUo, String codiceOttica, Date dataDal) {
        // per prima cosa ottengo l'uo di partenza:
        So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        List<So4ComponentePubb> elencoComponenti = []

        visitDepthFirst(uoPartenza, -1) { So4UnitaPubb unita ->
            elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuoli(ruoli, unita, unita.al ?: new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
        }

        return elencoComponenti
    }

    /**
     * Ritorna tutti i componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     *
     * Documentazione Filippo:
     * f) Consentire la selezione dei funzionari ( componenti con il ruolo in input ) di tutta la struttura organizzativa
     * tenendo come riferimento l'unità di appartenenza del firmatario ( componente in input ) e gestendo la selezione
     * dei funzionari dalle unità di 1 livello inferiore rispetto al firmatario fino all'ultimo livello della struttura
     * (in questo caso, essendo la gestione del funzionario strettamente legata al firmatario, non potrà essere consentita
     * la selezione del funzionario fino a quando il campo del firmatario non è stato riempito).
     *
     * @param codiceRuolo il codice del ruolo
     * @param progrUo il progressivo della uo
     * @param codiceOttica il codice dell'ottica
     * @param dataDal data dell'unità
     * @return elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
     */
    List<So4UnitaPubb> getUnitaFiglieNLivello(long progrUo, String codiceOttica, Date dataDal, int livello = -1) {
        // per prima cosa ottengo l'uo di partenza:
        So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

        List<So4UnitaPubb> elencoUnita = []

        List<So4UnitaPubb> unitaFiglie = uoPartenza.getUnitaPubbFiglie(uoPartenza.al ?: new Date())
        for (So4UnitaPubb uo : unitaFiglie) {
            if (livello == 0) {
                return elencoUnita
            }
            visitDepthFirst(uo, livello - 1) { So4UnitaPubb unita ->
                elencoUnita.add(unita)
            }
        }
        return elencoUnita
    }

    /**
     * Ritorna l'elenco delle unità figlie del ramo della suddivisione a partire dall'unità richiesta.
     * Data l'unità e il codice suddivisione, si risale il ramo della struttura partendo dall'unità data fino alla suddivisione.
     * Una volta raggiunta, si ritornano tutte le unità figlie.
     *
     * @param uo unità di partenza
     * @param codiceSuddivisione codice della suddivisione
     * @return elenco delle unità figlie della suddivisione.
     */
    List<So4UnitaPubb> getUnitaFiglieSuddivisione(So4UnitaPubb uo, String codiceSuddivisione) {
        // ottengo la suddivisione
        So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.getSuddivisione(codiceSuddivisione, Impostazioni.OTTICA_SO4.valore).get()

        // ottengo l'unità vertice della suddivisione richiesta.
        So4UnitaPubb uoServizio = getUnitaVertice(uo, suddivisione.id)

        // ottengo le unità figlie dell'unità vertice
        List<So4UnitaPubb> uoFiglie = getUnitaFiglieNLivello(uoServizio.progr, uoServizio.ottica.codice, uoServizio.dal)

        return uoFiglie
    }

    private void visitDepthFirst(So4UnitaPubb unita, int livello, Closure visitor) {
        // prima visito l'unità su cui sono
        visitor(unita)

        // se sono arrivato al livello, esco.
        if (livello == 0) {
            return
        }

        // poi ricorsivamente agisco sulle figlie, livello per livello.
        List<So4UnitaPubb> unitaFiglie = unita.getUnitaPubbFiglie(unita.al ?: new Date())
        for (So4UnitaPubb uo : unitaFiglie) {
            visitDepthFirst(uo, livello - 1, visitor)
        }
    }

    So4UnitaPubb getUnitaVertice(So4UnitaPubb unita, long idSuddivisione = -1) {
        while (unita != null && unita.suddivisione?.id != idSuddivisione && unita.progrPadre > 0) {
            unita = unita.getUnitaPubbPadre(unita.al ?: new Date())
        }
        return unita
    }

    So4UnitaPubb getUnitaVerticeAllaData(So4UnitaPubb unita, Date data, long idSuddivisione = -1) {
        while (unita != null && unita.suddivisione?.id != idSuddivisione && unita.progrPadre > 0) {
            if (data) {
                unita = unita.getUnitaPubbPadre(data)
            } else {
                unita = unita.getUnitaPubbPadre(new Date())
            }
        }
        return unita
    }
}

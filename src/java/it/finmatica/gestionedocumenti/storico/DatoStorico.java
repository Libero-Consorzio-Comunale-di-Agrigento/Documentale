package it.finmatica.gestionedocumenti.storico;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by esasdelli on 07/02/2017.
 */
public class DatoStorico {

    public enum TipoStorico {
        MODIFICATO, CANCELLATO, AGGIUNTO;
    }

    private long        revisione           = -1;
    private Date        dataModifica        = null;
    private String      campo               = "";
    private Object      valoreNuovo         = null;
    private Object      valoreVecchio       = null;
    private TipoStorico tipoStorico         = TipoStorico.MODIFICATO;
    private String      nominativoUtente    = "";
    private List<DatoStorico> datiStorici   = null;
    private Map<?, ?> dati = null;


    public Map<?, ?> getDati() {
        return dati;
    }

    public void setDati(Map<?, ?> dati) {
        this.dati = dati;
    }

    public long getRevisione() {
        return revisione;
    }

    public void setRevisione(long revisione) {
        this.revisione = revisione;
    }

    public Object getValore () {
        if (tipoStorico == TipoStorico.MODIFICATO) {
            return valoreNuovo;
        } else if (tipoStorico == TipoStorico.AGGIUNTO) {
            return valoreNuovo;
        } else if (tipoStorico == TipoStorico.CANCELLATO) {
            return valoreVecchio;
        } else {
            // qui non dovrebbe capitarci mai
            throw new IllegalArgumentException("Il campo TipoStorico ha un valore non riconosciuto: "+tipoStorico);
        }
    }

    private void addToDatiStorici (DatoStorico datoStorico) {
        if (datiStorici == null) {
            datiStorici = new ArrayList<DatoStorico>();
        }
        datiStorici.add(datoStorico);
    }

    public List<DatoStorico> getDatiStorici() {
        return datiStorici;
    }

    public void setDatiStorici(List<DatoStorico> datiStorici) {
        this.datiStorici = datiStorici;
    }

    public Date getDataModifica() {
        return dataModifica;
    }

    public void setDataModifica(Date dataModifica) {
        this.dataModifica = dataModifica;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public Object getValoreNuovo() {
        return valoreNuovo;
    }

    public void setValoreNuovo(Object valoreNuovo) {
        this.valoreNuovo = valoreNuovo;
    }

    public Object getValoreVecchio() {
        return valoreVecchio;
    }

    public void setValoreVecchio(Object valoreVecchio) {
        this.valoreVecchio = valoreVecchio;
    }

    public TipoStorico getTipoStorico() {
        return tipoStorico;
    }

    public void setTipoStorico(TipoStorico tipoStorico) {
        this.tipoStorico = tipoStorico;
    }

    public String getNominativoUtente() {
        return nominativoUtente;
    }

    public void setNominativoUtente(String nominativoUtente) {
        this.nominativoUtente = nominativoUtente;
    }
}

package it.finmatica.dmServer.util;

import java.util.Vector;

public class keyval {

    private String key, value, tipoDoc, op, tipoDaClient;
    private Object valoreInsertUpdate;

    private String formatoCampo;

    private String dimension = null;

    private String valueBetween = null;

    private String valueNvl = null;

    private boolean noCaseSensitive = false;

    private String area = null, cm = null, categoria = null;
    private boolean isRicercaPuntuale = true;
    private int indexJoin = 0;
    private String tipoUguaglianza = null;
    public Vector valoriCursore;
    public Vector valoriTypeCursore;
    public Vector valoriSizeCursore;

    public static String ISCAMPO_ORDINAMENTO = "1";
    public static String ISCAMPO_RETURN = "2";
    public static String ISCAMPO_ORDINAMENTO_AND_RETURN = "3";

    private String campoOrdinamentoAndOrReturn = ISCAMPO_ORDINAMENTO;

    private boolean bMarkedForDelete = false;

    private boolean bMarkedInUse = false;

    private boolean bIsOcr = false;

    public keyval() {
        isRicercaPuntuale = true;
    }

    public keyval(String newkey, String newval) {
        this(newkey, newval, null, "contains");
    }

    public keyval(String newkey, Object newval, boolean bCheck) {
        key = newkey;
        valoreInsertUpdate = newval;
        tipoDoc = null;
        op = "contains";
        isRicercaPuntuale = true;
    }

    public keyval(String newkey, String newval, String newTipoDoc) {
        this(newkey, newval, newTipoDoc, "contains");
    }

    public keyval(String newkey, String newval, String newTipoDoc, String newOp) {
        key = newkey;
        value = newval;
        tipoDoc = newTipoDoc;
        op = newOp;
        isRicercaPuntuale = true;
    }

    public String getFormatoCampo() {
        return formatoCampo;
    }

    public void setFormatoCampo(String formatoCampo) {
        this.formatoCampo = formatoCampo;
    }

    public String getTipoDaClient() {
        return tipoDaClient;
    }

    public void setKey(String newkey) {
        key = newkey;
    }

    public void setOperator(String newop) {
        op = newop;
    }

    public void setValue(String newValue) {
        value = newValue;
    }

    public void setTipoDoc(String newtipoDoc) {
        tipoDoc = newtipoDoc;
    }

    public void setTipoDaClient(String tipo) {
        tipoDaClient = tipo;
    }

    public void setTipoUguaglianza(String newTipo) {
        tipoUguaglianza = newTipo;
    }

    public void setIsRicercaPuntuale(boolean is) {
        isRicercaPuntuale = is;
    }

    public void setIsOcr(boolean is) {
        bIsOcr = is;
    }

    public void setArea(String ar) {
        area = ar;
    }

    public void setCm(String codMod) {
        cm = codMod;
    }

    public void setCategoria(String cat) {
        categoria = cat;
    }

    public void setIndexJoin(int idx) {
        indexJoin = idx;
    }

    public void setCampoReturn(String tipo) {
        campoOrdinamentoAndOrReturn = tipo;
    }

    public void markForDelete() {
        bMarkedForDelete = true;
    }

    public String getKey() {
        return key;
    }

    public String getVal() {
        return value;
    }

    public Object getValInsUpd() {
        return valoreInsertUpdate;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public String getOperator() {
        return op;
    }

    public String getCampoReturn() {
        return campoOrdinamentoAndOrReturn;
    }

    public String getTipoUguaglianza() {
        return tipoUguaglianza;
    }

    public String getArea() {
        return area;
    }

    public String getCm() {
        return cm;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getIndexJoin() {
        return indexJoin;
    }

    public boolean getIsRicercaPuntuale() {
        return isRicercaPuntuale;
    }

    public boolean isMarkForDelete() {
        return bMarkedForDelete;
    }

    public String getValueNvl() {
        return valueNvl;
    }

    public void setValueNvl(String valueNvl) {
        this.valueNvl = valueNvl;
    }

    public Object getValoreInsertUpdate() {
        return valoreInsertUpdate;
    }

    public void setValoreInsertUpdate(Object valoreInsertUpdate) {
        this.valoreInsertUpdate = valoreInsertUpdate;
    }

    public String getValueBetween() {
        return valueBetween;
    }

    public void setValueBetween(String valueBetween) {
        this.valueBetween = valueBetween;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public boolean isNoCaseSensitive() {
        return noCaseSensitive;
    }

    public boolean getIsOcr() {
        return bIsOcr;
    }

    public void setNoCaseSensitive(boolean noCaseSensitive) {
        this.noCaseSensitive = noCaseSensitive;
    }

    public boolean isBMarkedInUse() {
        return bMarkedInUse;
    }

    public void setBMarkedInUse(boolean markedInUse) {
        bMarkedInUse = markedInUse;
    }
}

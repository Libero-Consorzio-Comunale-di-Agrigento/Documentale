package it.finmatica.afc

import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command

interface IAfcRecord {

    @Command
    void onChiudi();

    @Command
    void onSalva();

    @Command
    void onSalvaChiudi();

    @Command
    void onSettaValido(@BindingParam("valido") boolean valido);
}

package it.finmatica.gestionedocumenti.documenti;

import java.util.Date;

/**
 * Created by DScandurra on 12/12/2017.
 */
public interface IFileDocumento {

    String getNome();

    String getContentType();

    long getDimensione();

    String getTesto ();

    Long getIdFileEsterno();

    boolean isPdf ();

    boolean isP7m ();

    boolean isTsd();

    boolean isSbustabile();

    boolean isFirmato ();

    // ritorna true se il file è un odt, doc o docx.
    boolean isModificabile ();

    String getNomeFileSbustato ();

    String getNomePdf ();

    Date getDataVerifica();

    String getEsitoVerifica();


// TODO[SPRINGBOOT]: rimosso perché fa sbagliare il mapping su db. Valutare se sostituirlo con qualcos'altro
//    IDocumentoEsterno getDocumento ();

    //aggiunto getDataVerifica e  getEsitoVerifica il 15/09/2020

}

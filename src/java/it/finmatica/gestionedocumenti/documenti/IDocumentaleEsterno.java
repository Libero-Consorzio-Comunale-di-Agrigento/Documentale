package it.finmatica.gestionedocumenti.documenti;

public interface IDocumentaleEsterno {

	public void salvaDocumento (IDocumentoEsterno documento);

	public void storicizzaDocumento (IDocumentoStoricoEsterno documento);
}

package it.finmatica.gestionedocumenti.documenti
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

import org.hibernate.criterion.CriteriaSpecification

@Service
@Transactional
class DocumentoCollegatoService {

    def salvaDocumentiCollegati (documentoPrincipaleDto, DocumentoCollegatoDTO documentoCollegatoDto) {
		def documentoPrincipale 	= documentoPrincipaleDto.domainObject

		DocumentoCollegato delCol 	= new DocumentoCollegato()
		delCol.documento 			= documentoPrincipaleDto?.domainObject
		delCol.collegato 			= documentoCollegatoDto.collegato?.domainObject
		delCol.tipoCollegamento     = documentoCollegatoDto.tipoCollegamento?.domainObject
		documentoPrincipale.addToDocumentiCollegati (delCol)
		documentoPrincipale.save()

		return documentoPrincipale.toDTO()
	}

    def eliminaDocumentoCollegato (documentoPrincipaleDto, long idDocumentoCollegato) {
		Documento delCol 				= Documento.get(idDocumentoCollegato)
		Documento documentoPrincipale 	= documentoPrincipaleDto.domainObject

		DocumentoCollegato dc = DocumentoCollegato.findByCollegatoAndDocumento(delCol, documentoPrincipale)

		documentoPrincipale.removeFromDocumentiCollegati(dc)
        dc.delete()
        documentoPrincipale.save ()

		return documentoPrincipale.toDTO()
    }

	//TODO!!!
	def getListaDocumentiCollegati (documentoPrincipaleDto) {
		def listaDocumentoPrincipale = DocumentoCollegato.createCriteria().list {

			createAlias("protocolloCollegato",  "protCol", 	CriteriaSpecification.LEFT_JOIN)
			createAlias("docCol.registro",  "regDocCol", 	CriteriaSpecification.LEFT_JOIN)

			projections {
				property ("id") 					// 0
				property ("operazione") 			// 1

				property ("protCol.id") 				// 2
				property ("protCol.oggetto") 			// 5
				property ("tipoProtCol.titolo") 		// 6
				property ("regProtCol.descrizione") 	// 7

				property ("documentoPrincipale.id")			// 14
			}

			if (documentoPrincipaleDto instanceof DocumentoDTO) {
				or {
					eq ("documentoPrincipale.id", 	documentoPrincipaleDto.id)
				}
			}

			order ("dateCreated", "asc")

		}.collect { row ->
			[ id: 			row[0]
			, operazione:	row[1]
			, tipologia:	(row[2] > 0 ? row[6] : row[12])
			, registro: 	(row[2] > 0 ? row[7] : row[13])
			, eliminabile:  (row[14] == documentoPrincipaleDto.id || row[15] == documentoPrincipaleDto.id)
			//TODO
			, tipo: 		(row[2] > 0 ? "LETTERA" : "LETTERA")
			, idDocumento:	(row[2] > 0 ? row[2] : row[8])]
		}

		def listaDocumentoPassivo  = DocumentoCollegato.createCriteria().list {
			createAlias("documentoCollegata", 	"docCol", 		CriteriaSpecification.LEFT_JOIN)
			createAlias("documentoPrincipale", 	"docPrinc", 	CriteriaSpecification.LEFT_JOIN)
			createAlias("docPrinc.tipologia", 	"tipoDocPrinc",CriteriaSpecification.LEFT_JOIN)
			createAlias("docPrinc.registro", 	"regDocPrinc", CriteriaSpecification.LEFT_JOIN)

			projections {
				property ("id") 							// 0
				property ("operazione") 					// 1

				property ("docPrinc.id") 					// 2
				property ("docPrinc.oggetto") 				// 5
				property ("tipoDocPrinc.titolo") 			// 6
				property ("regDocPrinc.descrizione") 		// 7

			}

			if (documentoPrincipaleDto instanceof DocumentoDTO) {

				eq ("docCol.id", documentoPrincipaleDto.id)
				or {
					eq ("docPrinc.stato", StatoDocumento.ESECUTIVO)
					eq ("docPrinc.stato", StatoDocumento.INTEGRATO)
					eq ("docPrinc.stato", StatoDocumento.ANNULLATO)
					eq ("docPrinc.stato", StatoDocumento.PUBBLICATO)
				}
			}

			order ("dateCreated", "asc")

		}.collect { row ->
			[ id: 			row[0]
			, operazione:	row[1]=="INTEGRA"?"INTEGRATA DA": row[1]=="ANNULLA"?"ANNULLATA DA":row[1]=="COLLEGA"?"COLLEGATA CON":""
			, oggetto:		(row[2] > 0 ? row[5] : row[11])
			, tipologia:	(row[2] > 0 ? row[6] : row[12])
			, registro: 	(row[2] > 0 ? row[7] : row[13])
			, eliminabile:  (row[14] == documentoPrincipaleDto.id || row[15] == documentoPrincipaleDto.id)
			//TODO
			, tipo: 		(row[2] > 0 ? "LETTERA" : "LETTERA")
			, idDocumento:	(row[2] > 0 ? row[2] : row[8])]
		}


		def lista = new ArrayList<> ()
		for (i in listaDocumentoPrincipale) {
			lista.add(i)
		}
		for (i in listaDocumentoPassivo) {
			lista.add(i)
		}
		return lista
	}
}

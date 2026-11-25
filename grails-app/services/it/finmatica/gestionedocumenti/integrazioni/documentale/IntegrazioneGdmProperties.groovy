package it.finmatica.gestionedocumenti.integrazioni.documentale

import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.smartdoc.api.struct.Documento
import it.finmatica.smartdoc.integrazioni.gdm.IntegrazioneGdm

/**
 * Created by gmannella on 24/05/2018.
 */
class IntegrazioneGdmProperties implements IntegrazioneGdm {

    @Override
    String getCodiceModello(Documento documento) {
        if (documento.getTipo() != null) {
            Properties properties = new Properties()
            properties.load(IntegrazioneGdmProperties.class.getClassLoader().getResourceAsStream("smartdoc.properties"))
            return properties.get("integrazioneGdm.modello." + documento.tipo)
        } else {
            Map<String, String> mappaChiaviExtra = documento.getMappaChiaviExtra();
            if (mappaChiaviExtra != null) {
                if (mappaChiaviExtra.containsKey("MODELLO")) {
                    return mappaChiaviExtra.get("MODELLO")
                }
            }
        }

        //throw new GestioneDocumentiRuntimeException("Non ho trovato il CodiceModello per il documento ${documento}")
    }

    @Override
    String getCodiceArea(Documento documento) {
        if (documento.getTipo() != null) {
            Properties properties = new Properties()
            properties.load(IntegrazioneGdmProperties.class.getClassLoader().getResourceAsStream("smartdoc.properties"))
            return properties.get("integrazioneGdm.area." + documento.tipo)
        } else {
            Map<String, String> mappaChiaviExtra = documento.getMappaChiaviExtra();
            if (mappaChiaviExtra != null) {
                if (mappaChiaviExtra.containsKey("AREA")) {
                    return mappaChiaviExtra.get("AREA")
                }
            }
        }
        //throw new GestioneDocumentiRuntimeException("Non ho trovato il codice-area per il documento ${documento}")
    }
}

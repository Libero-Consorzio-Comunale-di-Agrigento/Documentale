package it.finmatica.modutils.multirecord;

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class DatiBlocchiNested {
	private Document 	dDoc = null;
	private Element 	root = null;
	
	public DatiBlocchiNested() {
    root = DocumentHelper.createElement("ROOT");
    dDoc = DocumentHelper.createDocument();
    dDoc.setRootElement(root);
		
	}
	
	public Element getRoot() {
		return dDoc.getRootElement();
	}
	
	public Element aggiungiRecord(String iddoc, String sRecord) {
		Element el_rec = null;
		Element el_doc = getElement(iddoc);
		if (el_doc == null) {
			el_doc = addElement(root, iddoc, "");
		}
		int i = 0;
    for(Iterator iterator = el_doc.elementIterator(); iterator != null && iterator.hasNext();) {
    	iterator.next();
    	i++;
    }
    el_rec = addElement(el_doc, ""+i, sRecord);
    return el_rec;
	}
	
  private Element addElement(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    elf.setText(valore);
    elp.add(elf);
    return elf;
  }
	
  private Element getElement(Element e, String tagName) {
    Element elemento = null, eFound = null;;
    for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
    {
        elemento = (Element)iterator.next();
        if(elemento != null && elemento.getName().equals(tagName)) {
           eFound = elemento;
        } else {
            eFound = getElement(elemento, tagName);
            if ( eFound != null) {
              return eFound;
            }
        }
    }

    return eFound;
  }
  
  private Element getElement(String tagName) {
  	return getElement(root, tagName);
  }
  
  public Element getIdDocumento(String iddoc) {
  	return getElement(iddoc);
  }
  
  public String asXML() {
  	return dDoc.asXML();
  }
}

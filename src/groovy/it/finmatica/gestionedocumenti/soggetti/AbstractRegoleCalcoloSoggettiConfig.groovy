package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.gestionedocumenti.Holders

import java.lang.reflect.Method

public abstract class AbstractRegoleCalcoloSoggettiConfig {
			
	public abstract List<LayoutSoggetti> getLayoutSoggettiList();
	public abstract List<TipoSoggetto> getTipoSoggettoList();
	
	List<IRegoleCalcoloSoggettiRepository> regoleCalcoloSoggettiRepositoryList;
	
	public List<RegolaCalcoloSoggetti> getListaRegoleComponenteDefault() {
		return getListaRegole(MetodoCalcoloSoggetti.Tipo.DEFAULT, MetodoCalcoloSoggetti.Categoria.COMPONENTE);
	}
	
	public List<RegolaCalcoloSoggetti> getListaRegoleUnitaDefault() {
		return getListaRegole(MetodoCalcoloSoggetti.Tipo.DEFAULT, MetodoCalcoloSoggetti.Categoria.UNITA);
	}
	
	public List<RegolaCalcoloSoggetti> getListaRegoleComponenteLista() {
		return getListaRegole(MetodoCalcoloSoggetti.Tipo.LISTA, MetodoCalcoloSoggetti.Categoria.COMPONENTE);
	}
	
	public List<RegolaCalcoloSoggetti> getListaRegoleUnitaLista () {
		return getListaRegole(MetodoCalcoloSoggetti.Tipo.LISTA, MetodoCalcoloSoggetti.Categoria.UNITA);
	}
	
	private List<RegolaCalcoloSoggetti> getListaRegole(MetodoCalcoloSoggetti.Tipo tipo, MetodoCalcoloSoggetti.Categoria categoria) {
		
		List<RegolaCalcoloSoggetti> regolaCalocoloList = new ArrayList<RegolaCalcoloSoggetti>()
		for(IRegoleCalcoloSoggettiRepository regoleCalcoloRepository : getRegoleCalcoloSoggettiRepositoryList()){

			List<Method> methods = regoleCalcoloRepository.getClass().getDeclaredMethods()
			for(Method method : methods){
				
				if(method.isAnnotationPresent(MetodoCalcoloSoggetti.class)){
		
					MetodoCalcoloSoggetti ann = method.getAnnotation(MetodoCalcoloSoggetti.class)
					MetodoCalcoloSoggetti.Tipo tipoAnn = ann.tipo()
					MetodoCalcoloSoggetti.Categoria categoriaAnn = ann.categoria()
					if (tipoAnn == tipo && categoriaAnn == categoria){
						RegolaCalcoloSoggetti regolaCalcolo = [nomeBean:    Holders.getApplicationContext().getBeanNamesForType(regoleCalcoloRepository.class)[0],
													   nomeMetodo:  method.getName(),
													   categoria:   categoriaAnn,
													   tipo:        tipoAnn,
													   titolo:      ann.titolo(),
													   descrizione: ann.descrizione()]
						regolaCalocoloList.add(regolaCalcolo)
					}
				}
			}
		}
		return regolaCalocoloList;
	}

}

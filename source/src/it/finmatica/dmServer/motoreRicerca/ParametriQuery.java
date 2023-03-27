package it.finmatica.dmServer.motoreRicerca;

public class ParametriQuery 
{
  private String chiave, colonna, parametro, operatore, valore, version;
  private String area,cm,categoria;
  private String selectItem;
  
  public ParametriQuery(String ch, String pa, String co, String op, String va, String versione, String ar, String codmod, String cat )
  {
      chiave = ch;
      parametro = pa;
      colonna = co;
      operatore = op;
      valore = va;
      version=versione;
      area=ar;
      cm=codmod;
      categoria=cat;
  }
  public String getChiave()
  {
    return chiave;
  }

  public String getVersion()
  {
    return version;
  }
  
  public String getColonna()
  {
    return colonna;
  }
  
  public String getParametro()
  {
    return parametro;
  }
  
  public String getOperatore()
  {
    return operatore;
  }
  
  public String getValore()
  {
    return valore;
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
  
  public void setValore(String val)
  {
     valore = val;
  }
  
  public void setColonna(String col)
  {
     colonna = col;
  }
  
  public String getSelectItem() {
		return selectItem;
  }
  
  public void setSelectItem(String selectItem) {
		 this.selectItem = selectItem;
  }  
}
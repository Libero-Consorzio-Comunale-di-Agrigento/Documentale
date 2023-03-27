package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class Allegati_temp_percorsi {
	private String area, cm, cr, nf, percorso, percorso_root, percorso_nofile;	


	public Allegati_temp_percorsi(String area, String cm, String cr, String nf, String percorso, String percorso_root, String percorso_nofile) {
		this.area=area;
		this.cm=cm;
		this.cr=cr;
		this.nf=nf;
		this.percorso=percorso;
		this.percorso_root=percorso_root;
		this.percorso_nofile=percorso_nofile;
	}
		
	
	public String getPercorso_nofile() {
		return percorso_nofile;
	}

	public void setPercorso_nofile(String percorso_nofile) {
		this.percorso_nofile = percorso_nofile;
	}	
	
	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getCm() {
		return cm;
	}

	public void setCm(String cm) {
		this.cm = cm;
	}

	public String getCr() {
		return cr;
	}

	public void setCr(String cr) {
		this.cr = cr;
	}

	public String getNf() {
		return nf;
	}

	public void setNf(String nf) {
		this.nf = nf;
	}

	public String getPercorso() {
		return percorso;
	}

	public void setPercorso(String percorso) {
		this.percorso = percorso;
	}

	public String getPercorso_root() {
		return percorso_root;
	}

	public void setPercorso_root(String percorso_root) {
		this.percorso_root = percorso_root;
	}
	
}

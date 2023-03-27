package it.finmatica.dmServer.sysIntegration;

public class SysIntegrationField {
		private String area, codiceModello, type;
		private String field,field_remotename,extraInstruction;
		private long order;
		private int keyfield;				
		
		public SysIntegrationField(String ar, String cm, 
								   String typeInt,String fieldName) {
			   area=ar;
			   codiceModello=cm;
			   type=typeInt;
			   field=fieldName;
		}
		
		public String getExtraInstruction() {
			   return extraInstruction;
		}
		
		public void setExtraInstruction(String extraInstruction) {
			   this.extraInstruction = extraInstruction;
		}
		 
		public String getField() {
			   return field;
		}
		
		public void setField(String field) {
			   this.field = field;
		}
		
		public String getField_remotename() {
			   return field_remotename;
		}
		
		public void setField_remotename(String field_remotename) {
			   this.field_remotename = field_remotename;
		}
		
		public int getKeyfield() {
			   return keyfield;
		}
		
		public void setKeyfield(int keyfield) {
			   this.keyfield = keyfield;
		}
		
		public long getOrder() {
			   return order;
		}
		
		public void setOrder(long order) {
			   this.order = order;
		}
		
		public String getArea() {
			   return area;
		}
		
		public String getCodiceModello() {
			   return codiceModello;
		}
		
		public String getType() {
			   return type;
		}				
		
		public String toString() {
			   StringBuffer sToStr = new StringBuffer("");
		   	  
		   	   sToStr.append("___________CAMPO________\n");
		   	   sToStr.append("Nome campo: "+field+"\n");
		   	   sToStr.append("Nome campo remoto: "+field_remotename+"\n");
		   	   sToStr.append("Order: "+order+"\n");
		   	   sToStr.append("keyField: "+keyfield+"\n");
		   	   sToStr.append("extraInstruction: "+extraInstruction+"\n");
		   	  
		   	   return sToStr.toString();
		}
}

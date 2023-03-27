package it.finmatica.dmServer.dbEngine.struct;

public class DbOpSetParameterBuffer {
	   
	   private String namePar;
	   private String nameColumn;
	   private Object value;
	   private Object valueTypeNull;
	   private int isParameterOrAsciiStream;
	   
	   public static final int IS_PARAMETER = 1;
	   public static final int IS_ASCIISTREAM = 2;
	   
	   public DbOpSetParameterBuffer(String newNamePar,Object newValue,int type) {
		      namePar=newNamePar;
		      value=newValue;
		      isParameterOrAsciiStream=type;
	   }
	   
	   public String getNamePar() {
		      return namePar;
	   }
	   
	   public Object getValue() {
		      return value;
	   }

	   public int getType() {
		      return isParameterOrAsciiStream;
	   }	   
	   
	   public void setValueTypeNull(Object type) {
		      valueTypeNull=type;
	   }
	   
	   public Object getValueTypeNull() {
		      return valueTypeNull;
	   }

	   public String getNameColumn() {
		      return nameColumn;
	   }

	   public void setNameColumn(String nameColumn) {
		   	  this.nameColumn = nameColumn;
	   }	   
}

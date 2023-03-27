package it.finmatica.dmServer.management;

public class Valori
{ 

    public Valori(Object key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    public Object getKey()
    {
        return key;
    }

    public Object getVal()
    {
        return value;
    }

    Object key;
    Object value;
    
    boolean bCheck;

	public boolean isBCheck() {
		return bCheck;
	}

	public void setBCheck(boolean check) {
		bCheck = check;
	}
}

package it.finmatica.dmServer;

public class Related 
{
  private String number,op;

  public Related(String newNumber,String newOp)
  {
         number=newNumber;
         op=newOp;
  }

  public String getNumber() 
  {
         return number;
  }

  public String getOp() 
  {
         return op;
  }
}
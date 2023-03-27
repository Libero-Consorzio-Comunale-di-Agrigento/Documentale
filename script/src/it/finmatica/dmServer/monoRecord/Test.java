package it.finmatica.dmServer.monoRecord;

import it.finmatica.dmServer.Environment;

public class Test 
{
  public static void main(String[] args)
  {            
       try {
         Environment vu = new Environment("GDC","GDC","MODULISTICA","ADS",null,"S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");
         Monorecord m = new Monorecord("1561","D","",vu);

         System.out.println(m.creaRiga());
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        
  }
}
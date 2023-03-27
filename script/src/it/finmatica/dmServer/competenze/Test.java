package it.finmatica.dmServer.competenze;

import it.finmatica.dmServer.Environment;

public class Test 
{
  public Test()
  {
  }

  public static void main(String[] args)
  {
        try {
           Environment vu = new Environment("GDM","GDM","MODULISTICA","ADS",null,"S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties");
           CompetenzeControlli controlli = new CompetenzeControlli("1738","L",vu);

           System.out.println(controlli.execControlli());
        }
        catch (Exception e) 
        {
           e.printStackTrace();
        }      
  }
}
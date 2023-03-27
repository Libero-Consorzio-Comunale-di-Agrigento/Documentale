package it.finmatica.dmServer.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

public class HashMapLinkedSet {
	   private HashMap hMap;
	   
	   public HashMapLinkedSet() {		   
		      hMap = new HashMap();
		   
	   }

	   public void add(String key, Object value) {
		      LinkedHashSet hSet;
		      
		      if ((LinkedHashSet)hMap.get(key)==null) {
		    	  hSet = new LinkedHashSet();
		    	  hSet.add(value);		    	
		    	  hMap.put(key,hSet);
		      }
		      else {
		    	  hSet = (LinkedHashSet)hMap.get(key);
		    
		    	  if (!hSet.contains(value)) hSet.add(value);
		      }
	   }
	   
	   public void remove(String key) {
		      hMap.remove(key);		      
	   }
	   
	   public void remove(String key,String value) {
		      if (hMap.containsKey(key)) {
		    	  LinkedHashSet hSet = (LinkedHashSet)hMap.get(key); 
		      
		          hSet.remove(value);
		      }
	   }
	   
	   public Iterator getHashMap() {
		      return hMap.keySet().iterator();
	   }
	   
	   public Iterator getHashSet(String key) { 
		      if (hMap.containsKey(key)) {
		    	  LinkedHashSet hSet = (LinkedHashSet)hMap.get(key); 
		    	  
		    	  return hSet.iterator();
		      }
		      
		      return null;
	   }
	   
	   public Vector getAllHashSet() {
		      Iterator i =  hMap.keySet().iterator();   
		      Vector vRet = new Vector();
		      
           while (i.hasNext()) {
         	  //System.out.println(i.next());
         	  LinkedHashSet hSet = (LinkedHashSet)hMap.get(i.next()); 
         	  
         	  Iterator iIntern = hSet.iterator();
         	  while (iIntern.hasNext()) {                    		 
         		  vRet.add(iIntern.next());
         	  }
           }
           
           return vRet;
	   }	   
	   
	   public Iterator getIterator() {
		      return hMap.keySet().iterator();   	   
	   }
	   
	   
	   public int size() {
		      return hMap.size();
	   }
}

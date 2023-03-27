package it.finmatica.dmServer.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Vector;

public class HashMapSet {
	  
	   private HashMap hMap;
	   
	   public HashMapSet() {		   
		      hMap = new HashMap();
		   
	   }

	   public void add(String key, Object value) {
		      HashSet hSet;
		      
		      if ((HashSet)hMap.get(key)==null) {
		    	  hSet = new HashSet();
		    	  hSet.add(value);		    	
		    	  hMap.put(key,hSet);
		      }
		      else {
		    	  hSet = (HashSet)hMap.get(key);
		    
		    	  if (!hSet.contains(value)) hSet.add(value);
		      }
	   }
	   
	   public void remove(String key) {
		      hMap.remove(key);		      
	   }
	   
	   public void remove(String key,String value) {
		      if (hMap.containsKey(key)) {
		    	  HashSet hSet = (HashSet)hMap.get(key); 
		      
		          hSet.remove(value);
		      }
	   }
	   
	   public Iterator getHashMap() {
		      return hMap.keySet().iterator();
	   }
	   
	   public Iterator getHashSet(String key) { 
		      if (hMap.containsKey(key)) {
		    	  HashSet hSet = (HashSet)hMap.get(key); 
		    	  
		    	  return hSet.iterator();
		      }
		      
		      return null;
	   }
	   
	   public Vector getAllHashSet() {
		      Iterator i =  hMap.keySet().iterator();   
		      Vector vRet = new Vector();
		      
              while (i.hasNext()) {
            	  //System.out.println(i.next());
            	  HashSet hSet = (HashSet)hMap.get(i.next()); 
            	  
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

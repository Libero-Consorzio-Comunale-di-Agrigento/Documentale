package it.finmatica.dmServer.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class CallStackUtil {

  public synchronized static String getCallStackAsString() {
    StringBuilder sb = new StringBuilder();

    StackTraceElement[] stackTraceElements = 
      Thread.currentThread().getStackTrace();
    
    String[] array = getCallStackAsStringArray(stackTraceElements);
    
    for (int i = 0; i < array.length; i++) {
      sb.append(array[i] + "\n");
    }
    return sb.toString();
  }

  public synchronized static String[] getCallStackAsStringArray() {
    StackTraceElement[] stackTraceElements = 
      Thread.currentThread().getStackTrace();
    
    String[] array = getCallStackAsStringArray(stackTraceElements);
    
    return array;
  }

  private synchronized static String[] getCallStackAsStringArray(StackTraceElement[] stackTraceElements) {
    ArrayList<String> list = new ArrayList<String>();
    String[] array = new String[1];

    for (int i = 0; i < stackTraceElements.length; i++) {
      StackTraceElement element = stackTraceElements[i];
      String classname = element.getClassName();
      String methodName = element.getMethodName();
      int lineNumber = element.getLineNumber();
      list.add(classname + "." + methodName + ":" + lineNumber);
    }
    return list.toArray(array);
  }
  
  public synchronized static String stack2string(Exception e) {
	  try {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    return "------\r\n" + sw.toString() + "------\r\n";
	  }
	  catch(Exception e2) {
	    return "bad stack2string";
	  }
  }

} 
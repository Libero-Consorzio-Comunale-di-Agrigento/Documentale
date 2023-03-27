/***********************************************************************
 * Module:  IElementoModello.java
 * Author:  adelmo
 * Created: marted� 19 marzo 2002 12.06.02
 * Purpose: Defines the Interface IElementoModello
 ***********************************************************************/

package it.finmatica.modulistica.modulisticapack;
 
//import java.util.*;
//import javax.servlet.http.HttpServletRequest;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;

/**
 * 
 */
public interface IElementoModello {
  public abstract String getValue(IDbOperationSQL dbOpEsterna);
  public abstract String getValue();
  public abstract String getZValue();
  public abstract String getPRNValue();
  public abstract String getPRNValue(IDbOperationSQL dbOpEsterna);
  public abstract String getPRNComValue();
  public abstract String getPRNComValue(IDbOperationSQL dbOpEsterna);
  public abstract void release();
  public abstract void settaProtetto(boolean b_protetto);
  public void settaListFields(String l_fields);
}


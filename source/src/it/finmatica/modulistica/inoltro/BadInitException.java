package it.finmatica.modulistica.inoltro;
/**
 * @author       Antonio
 * @version      1.0
 */

public class BadInitException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public BadInitException() {
  }

  public BadInitException(String msg) {
    super(msg);
  }
}
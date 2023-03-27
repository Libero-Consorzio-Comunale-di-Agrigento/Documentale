package it.finmatica.modulistica.inoltro;
/**
 * @author       Antonio
 * @version      1.0
 */

public class BadCryptingMarkerException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public BadCryptingMarkerException() {
  }

  public BadCryptingMarkerException(String msg) {
    super(msg);
  }
}
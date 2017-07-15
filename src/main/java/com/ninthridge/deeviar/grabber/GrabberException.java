package com.ninthridge.deeviar.grabber;

public class GrabberException extends Exception {

  private static final long serialVersionUID = 1L;

  public GrabberException(String message) {
    super(message);
  }

  public GrabberException(Throwable e) {
    super(e);
  }
}

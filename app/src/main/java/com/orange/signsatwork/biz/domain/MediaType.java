package com.orange.signsatwork.biz.domain;

public enum MediaType {

  LSF("L.S.F."), LPC("Lf.P.C.");

  private String type;

  MediaType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }

}

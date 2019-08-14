package com.orange.signsatwork.biz.domain;

public enum MediaType {

  LSF("L.S.F."), LPC("Lf.P.C."), BOTH("Les deux médias"), UNIMPORTANT("Indifférent");

  private String type;

  MediaType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }

}

package com.orange.signsatwork.biz.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WiktionaryRedirect {

  @JsonProperty("index")
  private Integer index;

  @JsonProperty("from")
  private String from;

  @JsonProperty("to")
  private String to;

  @JsonProperty("index")
  public Integer getIndex() {
    return index;
  }

  @JsonProperty("index")
  public void setIndex(Integer index) {
    this.index = index;
  }

  @JsonProperty("from")
  public String getFrom() {
    return from;
  }

  @JsonProperty("from")
  public void setFrom(String from) {
    this.from = from;
  }

  @JsonProperty("to")
  public String getTo() {
    return to;
  }

  @JsonProperty("to")
  public void setTo(String to) {
    this.to = to;
  }

}

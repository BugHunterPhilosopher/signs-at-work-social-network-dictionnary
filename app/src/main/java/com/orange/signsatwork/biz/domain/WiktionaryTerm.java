package com.orange.signsatwork.biz.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WiktionaryTerm {

  @JsonProperty("description")
  private String[] description;

  @JsonProperty("description")
  public String[] getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String[] description) {
    this.description = description;
  }

}

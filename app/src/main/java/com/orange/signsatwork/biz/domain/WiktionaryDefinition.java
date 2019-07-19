package com.orange.signsatwork.biz.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WiktionaryDefinition {

  @JsonProperty("batchcomplete")
  private String batchcomplete;

  @JsonProperty("query")
  private WiktionaryQuery query;


  @JsonProperty("batchcomplete")
  public String getBatchcomplete() {
    return batchcomplete;
  }

  @JsonProperty("batchcomplete")
  public void setBatchcomplete(String batchcomplete) {
    this.batchcomplete = batchcomplete;
  }

  @JsonProperty("query")
  public WiktionaryQuery getQuery() {
    return query;
  }

  @JsonProperty("query")
  public void setQuery(WiktionaryQuery query) {
    this.query = query;
  }

}

package com.orange.signsatwork.biz.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WiktionaryQuery {

  @JsonProperty("pages")
  private WiktionaryPage[] pages;

  @JsonProperty("pages")
  public WiktionaryPage[] getPages() {
    return pages;
  }

  @JsonProperty("pages")
  public void setPages(WiktionaryPage[] pages) {
    this.pages = pages;
  }

}

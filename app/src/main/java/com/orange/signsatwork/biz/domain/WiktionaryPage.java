package com.orange.signsatwork.biz.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WiktionaryPage {

  @JsonProperty("ns")
  private Integer ns;

  @JsonProperty("title")
  private String title;

  @JsonProperty("pageid")
  private Long pageid;

  @JsonProperty("index")
  private Long index;

  @JsonProperty("terms")
  private WiktionaryTerm terms;

  @JsonProperty("ns")
  public Integer getNs() {
    return ns;
  }

  @JsonProperty("ns")
  public void setNs(Integer ns) {
    this.ns = ns;
  }

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("pageid")
  public Long getPageid() {
    return pageid;
  }

  @JsonProperty("pageid")
  public void setPageid(Long pageid) {
    this.pageid = pageid;
  }

  @JsonProperty("index")
  public Long getIndex() {
    return index;
  }

  @JsonProperty("index")
  public void setIndex(Long index) {
    this.index = index;
  }

  @JsonProperty("terms")
  public WiktionaryTerm getTerms() {
    return terms;
  }

  @JsonProperty("terms")
  public void setTerms(WiktionaryTerm terms) {
    this.terms = terms;
  }

}

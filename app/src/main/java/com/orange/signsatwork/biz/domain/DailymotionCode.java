package com.orange.signsatwork.biz.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class DailymotionCode {
  public final long id;
  private final String code;

  public static DailymotionCode create(String code) {
    return new DailymotionCode(-1, code);
  }

}

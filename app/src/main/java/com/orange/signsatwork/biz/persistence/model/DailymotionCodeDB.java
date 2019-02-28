package com.orange.signsatwork.biz.persistence.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "code")
@Entity
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
public class DailymotionCodeDB {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public long id;
  @Column
  @NotNull
  private String code;

  public DailymotionCodeDB(String code) {
    this.code = code;
  }

}

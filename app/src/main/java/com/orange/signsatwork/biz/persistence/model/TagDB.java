package com.orange.signsatwork.biz.persistence.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

// we want to save 'Tag' objects in the 'tags' DB table
@Table(name = "tags")
@Entity
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
public class TagDB {

  // An autogenerated id (unique for each tag in the db)
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(unique = true)
  @NotNull
  private String name;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinColumn(name="sign_id")
  private Set<SignDB> signs;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TagDB tagDB = (TagDB) o;
    return id.equals(tagDB.id) &&
      name.equals(tagDB.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

}

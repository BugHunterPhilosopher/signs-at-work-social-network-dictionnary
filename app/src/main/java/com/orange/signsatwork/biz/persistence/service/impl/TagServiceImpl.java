package com.orange.signsatwork.biz.persistence.service.impl;

/*
 * #%L
 * Signs at work
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.model.TagDB;
import com.orange.signsatwork.biz.persistence.repository.TagRepository;
import com.orange.signsatwork.biz.persistence.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepository;

  @Override
  public TagDB withId(long id) {
    return tagRepository.findOne(id);
  }

  @Override
  public TagDB withName(String name) {
    Set<TagDB> byName = tagRepository.findByName(name);
    return byName.isEmpty() ? null : byName.iterator().next();
  }

  @Override
  public TagDB create(String tagName, SignDB signDB) {
    TagDB tag = new TagDB();
    tag.setName(tagName);

    Set<SignDB> signs = new HashSet<>();
    signs.add(signDB);
    tag.setSigns(signs);

    return tagRepository.save(tag);
  }

  @Override
  public TagDB save(TagDB tag) {
    return tagRepository.save(tag);
  }

  @Override
  public Set<TagDB> all() {
    return new HashSet<>(tagRepository.findAll());
  }

  @Override
  public List<TagDB> allOrderedByName() {
    return new ArrayList<>(tagRepository.findAll(new Sort("name")));
  }

  @Override
  public void delete(TagDB tag) {
    tagRepository.delete(tag);
  }

}

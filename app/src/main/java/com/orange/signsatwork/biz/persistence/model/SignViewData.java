package com.orange.signsatwork.biz.persistence.model;

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

import com.orange.signsatwork.biz.domain.MediaType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SignViewData {

  public final long id;
  public final String name;
  public final Date createDate;
  public final long lastVideoId;
  public final String url;
  public final String pictureUri;
  public final long nbVideo;
  public final String tags;
  public final String mediaTypes;

  public SignViewData(Object[] queryResultItem) {
    id = toLong(((SignDB)queryResultItem[1]).getId());
    name = toString(((SignDB)queryResultItem[1]).getName());
    createDate = toDate(((SignDB)queryResultItem[1]).getCreateDate());
    lastVideoId = toLong(((SignDB)queryResultItem[1]).getLastVideoId());
    url = toString(((VideoDB)queryResultItem[0]).getUrl());
    pictureUri = toString(((VideoDB)queryResultItem[0]).getPictureUri());
    nbVideo = toLong(((SignDB)queryResultItem[1]).getNbVideo());
    tags = toTags(((SignDB)queryResultItem[1]).getTags());
    mediaTypes = toMediaTypes(((SignDB)queryResultItem[1]).getVideos());
  }

  public SignViewData(SignDB queryResultItem) {
    id = queryResultItem.getId();
    name = queryResultItem.getName();
    createDate = queryResultItem.getCreateDate();
    lastVideoId = queryResultItem.getLastVideoId();
    url = queryResultItem.getUrl();
    pictureUri = null; // In sync with UI
    nbVideo = queryResultItem.getNbVideo();
    tags = toTags(queryResultItem.getTags());
    mediaTypes = "";
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    return ((Long)o).longValue();
  }

  private String toTags(Set<TagDB> l) {
    return l.stream().map(TagDB::getName).collect(Collectors.joining(","));
  }

  private String toMediaTypes(List<VideoDB> l) {
    return l.stream().filter(distinctByKey(VideoDB::getMediaType)).
      map(VideoDB::getMediaType).map(MediaType::toString).
      collect(Collectors.joining(","));
  }

  private Date toDate(Object o) {
    Timestamp timestamp = ((Timestamp)o);
    return new Date(timestamp.getTime());
  }

  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

}

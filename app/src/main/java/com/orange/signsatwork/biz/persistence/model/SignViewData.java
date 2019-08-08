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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
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

  public SignViewData(Object[] queryResultItem) {
    id = ((SignDB)queryResultItem[0]).getId();
    name = ((SignDB)queryResultItem[0]).getName();
    createDate = ((SignDB)queryResultItem[0]).getCreateDate();
    lastVideoId = ((SignDB)queryResultItem[0]).getLastVideoId();
    url = ((SignDB)queryResultItem[0]).getUrl();
    pictureUri = "";
    nbVideo = ((SignDB)queryResultItem[0]).getNbVideo();
    tags = toTags(((SignDB)queryResultItem[0]).getTags());
  }

  public SignViewData(Object queryResultItem) {
    id = ((SignDB)queryResultItem).getId();
    name = ((SignDB)queryResultItem).getName();
    createDate = ((SignDB)queryResultItem).getCreateDate();
    lastVideoId = ((SignDB)queryResultItem).getLastVideoId();
    url = ((SignDB)queryResultItem).getUrl();
    pictureUri = "";
    nbVideo = ((SignDB)queryResultItem).getNbVideo();
    tags = toTags(((SignDB)queryResultItem).getTags());
  }

  private String toTags(Set<TagDB> l) {
    return l.stream().map(TagDB::getName).collect(Collectors.joining(","));
  }

}

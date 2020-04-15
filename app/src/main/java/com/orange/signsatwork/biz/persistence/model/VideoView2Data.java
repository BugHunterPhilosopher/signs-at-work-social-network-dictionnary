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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class VideoView2Data extends VideoViewData {

  public VideoView2Data(Object[] queryResultItem) {
    signId = ((BigInteger)queryResultItem[0]).longValue();
    signName = toString(queryResultItem[1]);
    createDate = toDate(queryResultItem[2]);
    videoId = ((BigInteger)queryResultItem[3]).longValue();
    url = toString(queryResultItem[4]);
    pictureUri = queryResultItem[5] != null ? toString(queryResultItem[5]) : "";
    nbView = ((BigInteger)queryResultItem[6]).longValue();
    averageRate = ((BigInteger)queryResultItem[7]).longValue();
    nbComment = ((BigInteger)queryResultItem[8]).longValue();
    idForName = ((BigInteger)queryResultItem[9]).longValue();
    nbVideo = ((BigInteger)queryResultItem[10]).longValue();
    mediaTypes = MediaType.UNIMPORTANT.toString();
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    return ((Long)o).longValue();
  }

  private Date toDate(Object o) {
    Timestamp timestamp = ((Timestamp)o);
    return new Date(timestamp.getTime());
  }

}

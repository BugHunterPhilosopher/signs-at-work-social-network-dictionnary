package com.orange.signsatwork.biz.domain;

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

import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class User {
  public long id;
  public String username;
  public String firstName;
  public String lastName;
  public String nameVideo;
  public String email;
  public String entity;
  public String job;
  public String jobTextDescription;
  public String jobVideoDescription;
  public Date lastDeconnectionDate;
  public Communities communities;
  public Requests requests;
  public Favorites favorites;
  public Videos videos;
  public String password;
  public String matchingPassword;

  private Services services;

  public User(long id, String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastDeconnectionDate, Communities forUser, Requests requestsforUser, Favorites favoritesforUser, Videos videos, Services services, String password, String matchingPassword) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.nameVideo = nameVideo;
    this.email = email;
    this.entity = entity;
    this.job = job;
    this.jobTextDescription = jobTextDescription;
    this.jobVideoDescription = jobVideoDescription;
    this.lastDeconnectionDate = lastDeconnectionDate;
    communities = forUser;
    requests = requestsforUser;
    favorites = favoritesforUser;
    this.videos = videos;
    this.services = services;
    this.password = password;
    this.matchingPassword = matchingPassword;
  }

  public String name() {
    if ((lastName == null || lastName.length() == 0) && (firstName == null || firstName.length() == 0)) {
      return username;
    } else if (lastName == null || lastName.length() == 0) {
      return firstName;
    } else if (firstName == null || firstName.length() == 0) {
      return lastName;
    } else {
      return firstName + " " + lastName;
    }
  }

  public String firstName() {
    return firstName ;
  }

  public String lastName() {
    return lastName ;
  }



  public User loadCommunitiesRequestsFavorites() {
    return communities != null || requests != null || favorites != null ?
            this :
            new User(
                    id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastDeconnectionDate,
                    services.community().forUser(id),  services.request().requestsforUser(id), services.favorite().favoritesforUser(id), videos,
                    services, password, matchingPassword);
  }

  public User loadVideos() {
    return videos != null ? this :
            new User(
                    id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastDeconnectionDate,
                    communities, requests, favorites, services.video().forUser(id),
                    services, password, matchingPassword);
  }

  public List<Long> communitiesIds() {
    return communities.ids();
  }


  public static User create(String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription) {
    return create(username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription,  null);
  }

  public static User create(String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastDeconnectionDate) {
    return create(-1, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastDeconnectionDate);
  }

  public static User create(long id, String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastDeconnectionDate) {
    return create(
            id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastDeconnectionDate,
            null);
  }

  public static User create(long id, String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastDeconnectionDate,
                            Services services) {
    return new User(
            id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastDeconnectionDate,
            null, null, null, null, services, "", "");
  }
}

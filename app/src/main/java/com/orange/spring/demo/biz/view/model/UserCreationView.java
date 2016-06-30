package com.orange.spring.demo.biz.view.model;

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

import com.orange.spring.demo.biz.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sun.reflect.CallerSensitive;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreationView {
  public static final String EMPTY_PASSWORD = "";

  private String username;
  private String password;
  private String firstName;
  private String lastName;
  private String email;
  private String entity;
  private String activity;

  public User toUser() {
    return new User(
            0, username, firstName, lastName, email, entity, activity,
            null, null, null, null, null, null, null);
  }
}
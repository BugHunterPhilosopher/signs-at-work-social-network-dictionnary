package com.orange.signsatwork.biz.storage;

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

import com.orange.signsatwork.SignsAtWorkApplication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.context.EnvironmentAware;
import org.springframework.stereotype.Component;

@ConfigurationProperties("storage")
@Component("merchandisingMasterDataItemsProxy")
@Scope("singleton")
@Configurable
public class StorageProperties implements EnvironmentAware {

  private Environment environment;

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Folder location for storing files
   */
  private String location;

  //private String location = "src/main/resources/public/video";

  public String getLocation() {
    return environment.getProperty("file.uploads.path");
  }

  public void setLocation(String location) {
    this.location = location;
  }

}

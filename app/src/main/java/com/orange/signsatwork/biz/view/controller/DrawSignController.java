package com.orange.signsatwork.biz.view.controller;

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

import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class DrawSignController {

  private static final String REQUEST_URL = "/sec/draw-sign/{signName}/{signId}";

  @Autowired
  private StorageProperties storageProperties;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = REQUEST_URL)
  public String favorite(HttpServletRequest req, @PathVariable String signName, @PathVariable long signId, Principal principal, Model model) {

    model.addAttribute("signName", signName);
    model.addAttribute("signId", signId);
    return "draw-sign";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = REQUEST_URL + "/create", method = RequestMethod.POST)
  @ResponseBody
  public Map<String,Object> createDrawSign(@RequestParam(value="imageBase64", defaultValue="")String imageBase64) {
    Map<String,Object> res = new HashMap<>();

    File imageFile = new File(storageProperties.getLocation() + UUID.randomUUID() + ".png");
    try{
      byte[] decodedBytes = DatatypeConverter.parseBase64Binary(imageBase64.replaceAll("data:image/.+;base64,", ""));
      BufferedImage bfi = ImageIO.read(new ByteArrayInputStream(decodedBytes));
      ImageIO.write(bfi , "png", imageFile);
      bfi.flush();
      res.put("ret", 0);
    } catch(Exception e) {
      res.put("ret", -1);
      res.put("msg", "Cannot process due to the image processing error.");
      log.error("Error in draw sign processing!", e);
      return res;
    }

    return res;
  }

}
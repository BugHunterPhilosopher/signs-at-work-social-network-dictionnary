package com.orange.signsatwork.biz.webservice.controller;

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

import com.orange.signsatwork.AppProfile;
import com.orange.signsatwork.DailymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import com.orange.signsatwork.biz.domain.FileUploadDailymotion;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.UrlFileUploadDailymotion;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.VideoDailyMotion;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageProperties;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.Principal;
import java.util.Arrays;
import java.util.OptionalLong;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@Controller
public class FileUploadNonRestController {

  @Autowired
  private StorageService storageService;
  @Autowired
  private StorageProperties storageProperties;

  @Autowired
  Services services;
  @Autowired
  DailymotionToken dailymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Environment environment;
  @Autowired
  private AppProfile appProfile;


  public static final String REST_SERVICE_URI = "https://api.dailymotion.com";
  public static final String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  public static final String VIDEO_EMBED_FIELD = "embed_url";

  public static long parseSize(String text) {
    double d = Double.parseDouble(text.replaceAll("[GMK]B$", ""));
    long l = Math.round(d * 1024 * 1024 * 1024L);
    switch (text.charAt(Math.max(0, text.length() - 2))) {
      default:  l /= 1024;
      case 'K': l /= 1024;
      case 'M': l /= 1024;
      case 'G': return l;
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadSelectedVideoFile(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_GIF_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadSelectedGifFile(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedGifFileUpload(file, OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_GIF_FILE_UPLOAD_FOR_VARIANT, method = RequestMethod.POST)
  public String uploadSelectedGifFileForVariant(@RequestParam("file") MultipartFile file, @PathVariable String sign, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedGifFileUploadForVariant(file, sign, signCreationView, principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String createSignFromUploadondailymotion(@RequestParam("file") MultipartFile file,@PathVariable long requestId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedVideoFileUpload(file, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);

  }
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionFromSign(@RequestParam("file") MultipartFile file,@PathVariable long signId, @PathVariable long videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationView, principal, response);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionForNewVideo(@RequestParam("file") MultipartFile file,@PathVariable long signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationView, principal, response);

  }

  private String handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {

    if ((!file.getOriginalFilename().endsWith(".mp4")) && (!file.getOriginalFilename().endsWith(".MP4"))) {
      log.error(file.getOriginalFilename() + " filename doesn't ends with '.mp4'");
      return "";
    }

    String dailymotionId;
    String pictureUri = null;
    User user = services.user().withUserName(principal.getName());
    AuthTokenInfo authTokenInfo = null;
    File inputFile = null;

    try {

      authTokenInfo = dailymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dailymotionToken.retrieveToken();
        authTokenInfo = dailymotionToken.getAuthTokenInfo();
      }
    } catch (Exception errorDailymotionUploadFile) {
      log.error("error while uploading!", errorDailymotionUploadFile);
    }


    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();

      if (null != authTokenInfo) {
        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        if (signId.isPresent()) {
          body.add("title", services.sign().withId(signId.getAsLong()).name);
        } else {
          body.add("title", signCreationView.getSignName());
        }
        body.add("channel", messageByLocaleService.getMessage("video_category"));
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        int i = 0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


        if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
          pictureUri = videoDailyMotion.thumbnail_360_url;
          log.warn("handleSelectedVideoFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
        }

        if (!videoDailyMotion.embed_url.isEmpty()) {
          signCreationView.setVideoUrl(videoDailyMotion.embed_url);
          log.warn("handleSelectedVideoFileUpload : embed_url = {}", videoDailyMotion.embed_url);
        }
      }
    } catch(Exception errorDailymotionUploadFile){
      log.error("error while uploading!", errorDailymotionUploadFile);
    }

    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl() == null ? inputFile.getName() : signCreationView.getVideoUrl(), pictureUri);
    log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl() == null ? inputFile.getName() : signCreationView.getVideoUrl());

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);

    return "redirect:/sec/sign/" + sign.id + "/" + sign.lastVideoId + "/detail";
  }


  private String handleSelectedGifFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    if ((!file.getOriginalFilename().endsWith(".gif")) && (!file.getOriginalFilename().endsWith(".GIF"))) {
      log.error(file.getOriginalFilename() + " filename doesn't ends with '.gif'");
      return "";
    }

    User user = services.user().withUserName(principal.getName());
    File inputFile;

    storageService.store(file);
    inputFile = storageService.load(file.getOriginalFilename()).toFile();

    Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
    parts.add("file", resource);

    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), inputFile.getName(), inputFile.getName());

    log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / file name = {}", user.username, signCreationView.getSignName(), inputFile.getName());

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);

    return "redirect:/sec/sign/" + sign.id + "/" + sign.lastVideoId + "/detail";
  }

  private String handleSelectedGifFileUploadForVariant(@RequestParam("file") MultipartFile file, String signName, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    if ((!file.getOriginalFilename().endsWith(".gif")) && (!file.getOriginalFilename().endsWith(".GIF"))) {
      log.error(file.getOriginalFilename() + " filename doesn't ends with '.gif'");
      return "";
    }

    User user = services.user().withUserName(principal.getName());
    File inputFile;

    storageService.store(file);
    inputFile = storageService.load(file.getOriginalFilename()).toFile();

    Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
    parts.add("file", resource);

    Sign sign = services.sign().create(user.id, signName, inputFile.getName(), inputFile.getName());

    log.info("handleSelectedVideoFileUploadForVariant : username = {} / sign name = {} / file name = {}", user.username, signName, inputFile.getName());

    response.setStatus(HttpServletResponse.SC_OK);

    return "redirect:/sec/sign/" + sign.id + "/" + sign.lastVideoId + "/detail";
  }

}

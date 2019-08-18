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
import com.orange.signsatwork.biz.domain.MediaType;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.RequestBodyForRequest;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.UrlFileUploadDailymotion;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.VideoDailyMotion;
import com.orange.signsatwork.biz.domain.VideoFile;
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageProperties;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.RequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class FileUploadRestController {

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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadRecordedVideoFile(@RequestBody VideoFile videoFile, @RequestParam("mediaType") String mediaType, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, mediaType, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), principal, response);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromRequest(@RequestBody VideoFile videoFile, @RequestParam String mediaType, @PathVariable long requestId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, mediaType, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromSign(@RequestBody VideoFile videoFile, @RequestParam String mediaType, @PathVariable long signId, @PathVariable long videoId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, mediaType, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForNewVideo(@RequestBody VideoFile videoFile, @RequestParam("mediaType") String mediaType, @PathVariable("signId") long signId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, mediaType, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_WITH_ID, method = RequestMethod.POST)
  public String uploadRecordedVideoFileWithId(@RequestBody VideoFile videoFile, @RequestParam("mediaType") String mediaType, @PathVariable("signId") long signId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, mediaType, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), principal, response);
  }

  private String handleRecordedVideoFile(VideoFile videoFile, String mediaType, OptionalLong requestId,OptionalLong signId, OptionalLong videoId, Principal principal, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String videoUrl = null;
    String file = storageProperties.getLocation() + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.http.multipart.max-file-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.http.multipart.max-file-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }

    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      log.error("error!", errorUploadFile);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    String dailymotionId;
    String pictureUri = null;
    User user = services.user().withUserName(principal.getName());
    File fileWebm = new File("");

    try {
      // Youtube
      // UploadVideoToYoutubeService.uploadToYoutube(fileOutput, appProfile.youtubeAccess());

      // Dailymotion
      AuthTokenInfo authTokenInfo = dailymotionToken.retrieveToken();
      log.info("authTokenInfo: " + authTokenInfo);

      if (authTokenInfo.isExpired()) {
        dailymotionToken.retrieveToken();
        authTokenInfo = dailymotionToken.getAuthTokenInfo();
      }

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();

      fileWebm = new File(file);
      Resource resource = new FileSystemResource(fileWebm.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (signId.isPresent()){
        body.add("title",services.sign().withId(signId.getAsLong()).name);
      }else{
        body.add("title", videoFile.signNameRecording);
      }
      body.add("channel", messageByLocaleService.getMessage("video_category"));
      body.add("published", true);
      body.add("private", true);

      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/me/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();

      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      int i=0;
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
        log.warn("handleFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }

      if (!videoDailyMotion.embed_url.isEmpty()) {
        videoUrl = videoDailyMotion.embed_url;
        log.warn("handleFileUpload : embed_url = {}", videoDailyMotion.embed_url);
      }
    }
    catch(Exception errorDailymotionUploadFile)
    {
      log.error("error!", errorDailymotionUploadFile);
    }
    Sign sign;

    if (signId.isPresent() && (videoId.isPresent())) {
      sign = services.sign().withId(signId.getAsLong());
      dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        log.error("error!", errorDailymotionDeleteVideo);
      }
      sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl == null ? fileWebm.getName() : videoUrl, pictureUri, com.orange.signsatwork.biz.domain.MediaType.valueOf(mediaType));
    } else if (signId.isPresent() && !(videoId.isPresent())) {
      sign = services.sign().withId(signId.getAsLong());
      if (sign != null) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl == null ? fileWebm.getName() : videoUrl, pictureUri, com.orange.signsatwork.biz.domain.MediaType.valueOf(mediaType));
      } else {
        sign = services.sign().create(user.id, videoFile.signNameRecording, videoUrl == null ? fileWebm.getName() : videoUrl, pictureUri , mediaType);
      }
    } else {
      sign = services.sign().create(user.id, videoFile.signNameRecording, videoUrl == null ? fileWebm.getName() : videoUrl, pictureUri , mediaType);
      log.info("handleFileUpload : username = {} / sign name = {} / video url = {}", user.username, videoFile.signNameRecording, videoUrl);
    }

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/sign/" + sign.id + "/" + sign.lastVideoId + "/detail";
  }

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
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForJobDescription(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForProfil(file, principal, "JobDescription", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForName(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForProfil(file, principal, "Name", response);
  }

  private String handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      try {
        String dailymotionId;
        AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dailymotionToken.retrieveToken();
          authTokenInfo = dailymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        if (inputType.equals("JobDescription")) {
          body.add("title", "Description du métier de " + user.name());
        } else {
          body.add("title", user.name());
        }
        body.add("channel", messageByLocaleService.getMessage("video_category"));
        body.add("published", true);
        body.add("private", true);

        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (inputType.equals("JobDescription")) {
            if (user.jobVideoDescription != null) {
              dailymotionId = user.jobVideoDescription.substring(user.jobVideoDescription.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
            services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url);
          } else {
            if (user.nameVideo != null) {
              dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
            services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url);
          }

          log.warn("handleSelectedVideoFileUploadForProfil : embed_url = {}", videoDailyMotion.embed_url);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        if (inputType.equals("JobDescription")) {
          return "/sec/your-job-description";
        } else {
          return "/sec/who-are-you";
        }
      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionForNewVideo(@RequestBody MultipartFile file, @RequestParam("mediaType") String mediaType, @PathVariable("signId") long signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedVideoFileUpload(file, mediaType, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationView, principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_GIF_FILE_UPLOAD_FOR_VARIANT, method = RequestMethod.POST)
  public String uploadSelectedGifFileForVariant(@RequestBody MultipartFile file, @PathVariable("mediaType") String mediaType, @PathVariable("signId") long signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
    return handleSelectedGifFileUploadForVariant(file, mediaType, signId, signCreationView, principal, response);
  }

  private String handleSelectedGifFileUploadForVariant(MultipartFile file, String mediaType, long signId, SignCreationView signCreationView, Principal principal, HttpServletResponse response) {
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

    Sign sign = services.sign().addNewVideo(user.id, signId, inputFile.getName(), inputFile.getName(), com.orange.signsatwork.biz.domain.MediaType.valueOf(mediaType));

    log.info("handleSelectedVideoFileUploadForVariant : username = {} / file name = {}", user.username, inputFile.getName());

    response.setStatus(HttpServletResponse.SC_OK);

    return "/sec/sign/" + sign.id + "/" + sign.lastVideoId + "/detail";
  }

  private String handleSelectedVideoFileUpload(MultipartFile file, String mediaType, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) {

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
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


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
        headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

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

    Sign sign = services.sign().addNewVideo(user.id, signId.getAsLong(), signCreationView.getVideoUrl() == null ? inputFile.getName() : signCreationView.getVideoUrl(), pictureUri, com.orange.signsatwork.biz.domain.MediaType.valueOf(mediaType));
    log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl() == null ? inputFile.getName() : signCreationView.getVideoUrl());

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);

    return "/sec/sign/" + sign.id + "/" + sign.lastVideoId + "/detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForJobDescription(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForProfil(videoFile, principal, "JobDescription", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForName(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForProfil(videoFile, principal, "Name", response);
  }

  private String handleRecordedVideoFileForProfil(VideoFile videoFile, Principal principal, String inputType, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String file = "/data/" + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.http.multipart.max-file-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.http.multipart.max-file-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      log.info("file: " + file);
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));
      log.info("videoByte.length: " + videoByte.length);
      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      log.error("error!", errorUploadFile);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      String dailymotionId;

      AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dailymotionToken.retrieveToken();
        authTokenInfo = dailymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (inputType.equals("JobDescription")) {
        body.add("title", "Description du métier de " + user.name());
      } else {
        body.add("title", user.name());
      }

      body.add("channel", messageByLocaleService.getMessage("video_category"));
      body.add("published", true);
      body.add("private", true);

      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
      }
      while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


      if (!videoDailyMotion.embed_url.isEmpty()) {
        if (inputType.equals("JobDescription")) {
          if (user.jobVideoDescription != null) {
            dailymotionId = user.jobVideoDescription.substring(user.jobVideoDescription.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url);
        } else {
          if (user.nameVideo != null) {
            dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url);
        }

        log.warn("handleRecordedVideoFileForProfil : embed_url = {}", videoDailyMotion.embed_url);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      if (inputType.equals("JobDescription")) {
        return "/sec/your-job-description";
      } else {
        return "/sec/who-are-you";
      }

    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  public void DeleteVideoOnDailyMotion(String dailymotionId) {
    AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dailymotionToken.retrieveToken();
      authTokenInfo = dailymotionToken.getAuthTokenInfo();
    }

    final String uri = "https://api.dailymotion.com/video/"+dailymotionId;
    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + authTokenInfo.getAccess_token());

    HttpEntity<?> request = new HttpEntity<Object>(headers);

    restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class );

    return;
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION, method = RequestMethod.POST)
  public RequestResponse uploadSelectedVideoFileForRequestDescription(@RequestParam("file") MultipartFile file, @RequestParam("mediaTypeBody") String mediaType, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForRequestDescription(file, mediaType, requestId, requestCreationView, principal, response);
  }

  private RequestResponse handleSelectedVideoFileUploadForRequestDescription(@RequestParam("file") MultipartFile file, @RequestParam("mediaTypeBody") String mediaType, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response) throws InterruptedException {
    {
      Request request = null;
      RequestResponse requestResponse = new RequestResponse();
      VideoDailyMotion videoDailyMotion = new VideoDailyMotion();

      User user = services.user().withUserName(principal.getName());

      storageService.store(file);
      File inputFile = storageService.load(file.getOriginalFilename()).toFile();

      try {
        String dailymotionId;
        AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dailymotionToken.retrieveToken();
          authTokenInfo = dailymotionToken.getAuthTokenInfo();
        }

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();

        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", "Description LSF de la demande " + requestCreationView.getRequestName());
        body.add("channel", messageByLocaleService.getMessage("video_category"));
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));

        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (requestId != 0) {
            request = services.request().withId(requestId);
            if (request.requestVideoDescription != null) {
              dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return requestResponse;
              }
            }
            services.request().changeRequestVideoDescription(requestId, videoDailyMotion.embed_url);

          }
        }
      } catch (Exception errorDailymotionUploadFile) {
        log.error("Error uploading file to Dailymotion!", errorDailymotionUploadFile);
      }

      if (requestId == 0) { {
        List<String> emails;
        String title, bodyMail;

        if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
          if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
            request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription(), ((videoDailyMotion.embed_url == null) || "".equals(videoDailyMotion.embed_url.trim()) ? inputFile.getName() : videoDailyMotion.embed_url), MediaType.valueOf(mediaType));
            log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
            emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
            title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
            bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, "https://signes.bougetesmains.club"});

            Request finalRequest = request;
            Runnable task = () -> {
              log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
              services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, "https://signes.bougetesmains.club" );
            };

            new Thread(task).start();
          } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            requestResponse.errorType = 1;
            requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
            return requestResponse;
          }
        } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          requestResponse.errorType = 2;
          requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
          requestResponse.signId = services.sign().withName(requestCreationView.getRequestName()).list().get(0).id;
          return requestResponse;
        }
        log.warn("handleSelectedVideoFileUploadForRequestDescription : embed_url = {}", videoDailyMotion.embed_url);
      }
      }

      response.setStatus(HttpServletResponse.SC_OK);
      requestResponse.requestId = request.id;
      return requestResponse;
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION , method = RequestMethod.POST)
  public RequestResponse uploadRecordedVideoFileForRequestDescription(@RequestBody RequestBodyForRequest requestBody, @PathVariable long requestId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForRequestDescription(requestBody, requestId, principal, response);
  }

  private RequestResponse handleRecordedVideoFileForRequestDescription(RequestBodyForRequest requestBody, long requestId, Principal principal, HttpServletResponse response) {
    log.info("VideoFile " + requestBody);
    log.info("VideoFile name"+requestBody.name);
    RequestResponse requestResponse = new RequestResponse();
    String file = storageProperties.getLocation() + requestBody.name;

    log.info("taille fichier "+requestBody.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.http.multipart.max-file-size")));

    if (requestBody.contents.length() > parseSize(environment.getProperty("spring.http.multipart.max-file-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorFileSize");
      return requestResponse;
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(requestBody.contents.substring(requestBody.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      log.error("Error writing file to disk!", errorUploadFile);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return requestResponse;
    }

/*
    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorEncondingFile");
      return requestResponse;
    }
*/
    File fileWebm = new File(file);
    VideoDailyMotion videoDailyMotion = null;
    Request request;
    User user = services.user().withUserName(principal.getName());
    List<String> emails;
    String title, bodyMail;

    try {
      String dailymotionId;

      AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dailymotionToken.retrieveToken();
        authTokenInfo = dailymotionToken.getAuthTokenInfo();
      }


      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();

      Resource resource = new FileSystemResource(fileWebm.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", "Description LSF de la demande " + requestBody.requestNameRecording);
      body.add("channel", messageByLocaleService.getMessage("video_category"));
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      videoDailyMotion = response1.getBody();

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

      if (!videoDailyMotion.embed_url.isEmpty()) {
        if (requestId != 0) {
          request = services.request().withId(requestId);
          if (request.requestVideoDescription != null) {
            dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            } catch (Exception errorDailymotionDeleteVideo) {
              log.error("error deleting Dailymotion video!", errorDailymotionDeleteVideo);
            }
          }
          services.request().changeRequestVideoDescription(requestId, videoDailyMotion.embed_url);
        }
      }
    } catch (Exception errorDailymotionUploadFile) {
      log.error("error uploading to Dailymotion!", errorDailymotionUploadFile);
    }

    if (services.sign().withName(requestBody.requestNameRecording).list().isEmpty()) {
      if (services.request().withName(requestBody.requestNameRecording).list().isEmpty()) {
        request = services.request().create(user.id, requestBody.requestNameRecording, requestBody.requestTextDescriptionRecording, (((videoDailyMotion == null) || (videoDailyMotion.embed_url == null)) ? fileWebm.getName() : videoDailyMotion.embed_url), MediaType.valueOf(requestBody.mediaTypeBody));
        log.info("createRequest: username = {} / request name = {}", user.username, requestBody.requestNameRecording, requestBody.requestTextDescriptionRecording);
        emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
        title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
        bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, "https://signes.bougetesmains.club"});

        Request finalRequest = request;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
          services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, "https://signes.bougetesmains.club" );
        };

        new Thread(task).start();
      } else {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        requestResponse.errorType = 1;
        requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
        return requestResponse;
      }
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponse.errorType = 2;
      requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
      requestResponse.signId = services.sign().withName(requestBody.requestNameRecording).list().get(0).id;
      return requestResponse;
    }
    log.warn("handleRecordedVideoFileForRequestDescription : embed_url = {}", (((videoDailyMotion == null) || (videoDailyMotion.embed_url == null)) ? fileWebm.getName() : videoDailyMotion.embed_url));

    response.setStatus(HttpServletResponse.SC_OK);
    requestResponse.requestId = request.id;
    return requestResponse;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION , method = RequestMethod.POST)
  public String uploadRecordedVideoFileForSignDefinition(@RequestBody VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForSignDefinition(videoFile, signId, principal, response);
  }

  private String handleRecordedVideoFileForSignDefinition(VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String videoUrl = null;
    String file = "/data/" + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.http.multipart.max-file-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.http.multipart.max-file-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      String dailymotionId;
      Sign sign = null;
      sign = services.sign().withId(signId);

      AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dailymotionToken.retrieveToken();
        authTokenInfo = dailymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", "Définition LSF du signe " + sign.name);
      body.add("channel", messageByLocaleService.getMessage("video_category"));
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
      }
      while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


      if (!videoDailyMotion.embed_url.isEmpty()) {

        if (sign.videoDefinition != null) {
          dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          }
          catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
        }
        services.sign().changeSignVideoDefinition(signId, videoDailyMotion.embed_url);

      }

      response.setStatus(HttpServletResponse.SC_OK);
      return Long.toString(sign.id);

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForSignDefinition(file, signId, principal, response);
  }

  private String handleSelectedVideoFileUploadForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws InterruptedException {
    {
      Sign sign = null;
      sign = services.sign().withId(signId);

      try {
        String dailymotionId;
        AuthTokenInfo authTokenInfo = dailymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dailymotionToken.retrieveToken();
          authTokenInfo = dailymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", "Définition LSF du signe " + sign.name);
        body.add("channel", messageByLocaleService.getMessage("video_category"));
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));

        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (sign.videoDefinition != null) {
            dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.sign().changeSignVideoDefinition(signId, videoDailyMotion.embed_url);

        }

        response.setStatus(HttpServletResponse.SC_OK);
        return Long.toString(sign.id);

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

}

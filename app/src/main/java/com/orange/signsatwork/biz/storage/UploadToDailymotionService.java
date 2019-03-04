package com.orange.signsatwork.biz.storage;

import com.orange.signsatwork.DailymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.webservice.controller.FileUploadRestController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.Principal;
import java.util.OptionalLong;

@Slf4j
public class UploadToDailymotionService {
  private Services services;
  private SpringRestClient springRestClient;
  private boolean myResult;
  private FileUploadRestController fileUploadRestController;
  private DailymotionToken dailymotionToken;
  private VideoFile videoFile;
  private OptionalLong signId;
  private OptionalLong videoId;
  private Principal principal;
  private HttpServletResponse response;
  private String fileOutput;
  private Sign sign;

  @Autowired
  public UploadToDailymotionService(Services services, SpringRestClient springRestClient,
                                    FileUploadRestController fileUploadRestController, DailymotionToken dailymotionToken,
                                    VideoFile videoFile, OptionalLong signId, OptionalLong videoId, Principal principal,
                                    HttpServletResponse response, String fileOutput) {
    this.services = services;
    this.springRestClient = springRestClient;
    this.fileUploadRestController = fileUploadRestController;
    this.dailymotionToken = dailymotionToken;
    this.videoFile = videoFile;
    this.signId = signId;
    this.videoId = videoId;
    this.principal = principal;
    this.response = response;
    this.fileOutput = fileOutput;
  }

  public boolean hasError() {
    return myResult;
  }

  public Sign getSign() {
    return sign;
  }

  public UploadToDailymotionService upload() throws InterruptedException {
    String dailymotionId;
    AuthTokenInfo authTokenInfo = dailymotionToken.retrieveToken();
    log.info("authTokenInfo: " + authTokenInfo);

    if (authTokenInfo.isExpired()) {
      dailymotionToken.retrieveToken();
      authTokenInfo = dailymotionToken.getAuthTokenInfo();
    }

    log.info("Dailymotion info: services: {}, ", services);
    log.info("Dailymotion info: user: {},", services.user());
    log.info("Dailymotion info: principal: {}", principal);
    log.info("Dailymotion info: name: {}", principal.getName());
    User user = services.user().withUserName(principal.getName());

    UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


    File fileMp4 = new File(fileOutput);
    Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("file", resource);

    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);


    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

    ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
      HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
    FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("url", fileUploadDailyMotion.url);
    if (signId.isPresent()){
      body.add("title",services.sign().withId(signId.getAsLong()).name);
    }else{
      body.add("title", videoFile.signNameRecording);
    }
    body.add("channel", "tech");
    body.add("published", true);
    body.add("private", true);


    RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
    HttpHeaders headers1 = new HttpHeaders();
    headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
    //headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

    HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<>(body, headers1);
    ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/me/videos",
      HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
    VideoDailyMotion videoDailyMotion = response1.getBody();


    String url = FileUploadRestController.REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + FileUploadRestController.VIDEO_THUMBNAIL_FIELDS + FileUploadRestController.VIDEO_EMBED_FIELD;
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


    String pictureUri = null;
    if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
      pictureUri = videoDailyMotion.thumbnail_360_url;
      log.warn("handleFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
    }

    if (!videoDailyMotion.embed_url.isEmpty()) {
      log.warn("handleFileUpload : embed_url = {}", videoDailyMotion.embed_url);
    }
    if (signId.isPresent() && (videoId.isPresent())) {
      sign = services.sign().withId(signId.getAsLong());
      dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
      try {
        fileUploadRestController.DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        myResult = true;
        return this;
      }
      sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoDailyMotion.embed_url, pictureUri);
    } else if (signId.isPresent() && !(videoId.isPresent())) {
      sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoDailyMotion.embed_url, pictureUri);
    } else {
      sign = services.sign().create(user.id, videoFile.signNameRecording, videoDailyMotion.embed_url, pictureUri);
      log.info("handleFileUpload : username = {} / sign name = {} / video url = {}", user.username, videoFile.signNameRecording, videoDailyMotion.embed_url);
    }
    myResult = false;
    return this;
  }

}

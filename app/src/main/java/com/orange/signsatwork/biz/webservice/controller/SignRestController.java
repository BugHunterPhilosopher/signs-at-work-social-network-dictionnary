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

import com.orange.signsatwork.DailymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.Signs;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Video;
import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.model.TagDB;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.impl.SignServiceImpl;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.SignId;
import com.orange.signsatwork.biz.webservice.model.SignView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class SignRestController {

  @Autowired
  Services services;
  @Autowired
  DailymotionToken dailymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping(value = RestApi.WS_OPEN_SIGN + "/{id}")
  public SignView sign(@PathVariable long id) {
    Sign sign = services.sign().withId(id);
    return new SignView(sign);
  }

  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = "/sec/sign/{signId}/tags", method = RequestMethod.POST)
  public String signTags(@PathVariable long signId, @ModelAttribute SignCreationView signCreationView)  {
    String[] tagNames = signCreationView.getTags().split(",");
    log.info("### " + Arrays.toString(tagNames));

    List<String> allTagNames = Arrays.asList(tagNames);
    Sign sign = services.sign().withId(signId);
    SignDB signDB = SignServiceImpl.signDBFrom(sign);
    signDB.setId(sign.id);

    List<TagDB> allAddedTags = new ArrayList<>();

    // Add tags provided in Ajax
    for (String tagName : allTagNames) {
      if (!"".equals(tagName)) {
        TagDB aTag = services.tag().withName(tagName);
        if (null == aTag) {
          aTag = services.tag().create(tagName, signDB);
        } else {
          Set<SignDB> signs = aTag.getSigns();
          signs.add(signDB);
          aTag.setSigns(signs);
        }
        allAddedTags.add(aTag);
      }
    }

    // Remove tags not provided in Ajax but present on the sign (as Tagmanager is set up to always send all tags)
    for (TagDB tag : services.tag().all()) {
      if (!allAddedTags.contains(tag)) {
        boolean removed = tag.getSigns().remove(signDB);
        log.info("### removed? " + removed);
        if (removed) {
          if (tag.getSigns().isEmpty()) {
            services.tag().delete(tag);
          } else {
            services.tag().save(tag);
          }
        }
      }
    }

    return "";
  }

  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = "/sec/sign/{signId}/completeSynonyms", method = RequestMethod.POST)
  public String completeSynonyms(@PathVariable long signId, @ModelAttribute SignCreationView signCreationView)  {
    String[] synonymNames = signCreationView.getTags().split(",");
    log.info("### " + Arrays.toString(synonymNames));

    List<String> allSynonymNames = Arrays.asList(synonymNames);
    Sign sign = services.sign().withId(signId);
    SignDB signDB = SignServiceImpl.signDBFrom(sign);
    signDB.setId(sign.id);

    Set<SignDB> allAddedSynonyms = new HashSet<>();

    // Add synonyms provided in Ajax
    for (String synonymName : allSynonymNames) {
      if (!"".equals(synonymName)) {
        Signs aSign = services.sign().withName(synonymName);
        if (null == aSign) {
          // We don't create synonyms on-the-fly
        } else {
          Sign first = aSign.list().get(0);
          log.info("### " + first);
          SignDB signDb = SignServiceImpl.signDBFrom(first);
          signDb.setId(first.id);
          allAddedSynonyms.add(signDb);
        }
      }
    }
    signDB.setSynonyms(allAddedSynonyms);
    services.sign().save(signDB);

    return "";
  }

  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = "/sec/sign/{signId}/completeOpposites", method = RequestMethod.POST)
  public String completeOpposites(@PathVariable long signId, @ModelAttribute SignCreationView signCreationView)  {
    String[] oppositesNames = signCreationView.getTags().split(",");
    log.info("### " + Arrays.toString(oppositesNames));

    List<String> allOppositeNames = Arrays.asList(oppositesNames);
    Sign sign = services.sign().withId(signId);
    SignDB signDB = SignServiceImpl.signDBFrom(sign);
    signDB.setId(sign.id);

    Set<SignDB> allAddedOpposites = new HashSet<>();

    // Add opposites provided in Ajax
    for (String oppositeName : allOppositeNames) {
      if (!"".equals(oppositeName)) {
        Signs aSign = services.sign().withName(oppositeName);
        if (null == aSign) {
          // We don't create opposites on-the-fly
        } else {
          Sign first = aSign.list().get(0);
          log.info("### " + first);
          SignDB signDb = SignServiceImpl.signDBFrom(first);
          signDb.setId(first.id);
          allAddedOpposites.add(signDb);
        }
      }
    }
    signDB.setOpposites(allAddedOpposites);
    services.sign().save(signDB);

    return "";
  }

  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = "/sec/sign/{signId}/completeRelated", method = RequestMethod.POST)
  public String completeRelated(@PathVariable long signId, @ModelAttribute SignCreationView signCreationView)  {
    String[] relatedNames = signCreationView.getTags().split(",");
    log.info("### " + Arrays.toString(relatedNames));

    List<String> allRelatedNames = Arrays.asList(relatedNames);
    Sign sign = services.sign().withId(signId);
    SignDB signDB = SignServiceImpl.signDBFrom(sign);
    signDB.setId(sign.id);

    Set<SignDB> allAddedRelated = new HashSet<>();

    // Add opposites provided in Ajax
    for (String relatedName : allRelatedNames) {
      if (!"".equals(relatedName)) {
        Signs aSign = services.sign().withName(relatedName);
        if (null == aSign) {
          // We don't create related on-the-fly
        } else {
          Sign first = aSign.list().get(0);
          log.info("### " + first);
          SignDB signDb = SignServiceImpl.signDBFrom(first);
          signDb.setId(first.id);
          allAddedRelated.add(signDb);
        }
      }
    }
    signDB.setRelated(allAddedRelated);
    services.sign().save(signDB);

    return "";
  }

  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = "/sec/sign/autocompleteRelations")
  public String autocompleteRelations(@ModelAttribute SignCreationView signCreationView)  {
    return "[" + services.sign().all().stream().
      map(sign -> "\"" + sign.name + "\"").
      collect(Collectors.joining(", ")) + "]";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SIGN_CREATE, method = RequestMethod.POST)
  public SignId createSign(@RequestParam String mediaType, @RequestBody SignCreationView signCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "", mediaType);

    log.info("createSign: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

    return new SignId(sign.id);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_DELETE, method = RequestMethod.POST)
  public String deleteVideo(@PathVariable long signId, @PathVariable long videoId, HttpServletResponse response) {
    String dailymotionId;
    Sign sign = services.sign().withId(signId);
    if (sign.videos.list().size() == 1) {
      Request request = services.sign().requestForSign(sign);
      if (request != null) {
        if (request.requestVideoDescription != sign.videoDefinition) {
          String dailymotionIdForSignDefinition;
          dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
          } catch (Exception errorDailymotionDeleteVideo) {
            log.error(messageByLocaleService.getMessage("errorDailymotionDeleteVideo"), errorDailymotionDeleteVideo);
          }
        }
      }

      services.sign().delete(sign);
      dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        log.error(messageByLocaleService.getMessage("errorDailymotionDeleteVideo"), errorDailymotionDeleteVideo);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/";

    } else {
      Video video = services.video().withId(videoId);
      services.video().delete(video);
      dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        log.error(messageByLocaleService.getMessage("errorDailymotionDeleteVideo"), errorDailymotionDeleteVideo);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/sign/" + signId;
    }
  }

  private void DeleteVideoOnDailyMotion(String dailymotionId) {

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
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_ASSOCIATE, method = RequestMethod.POST)
  public String associateVideo(@RequestBody List<Long> associateVideosIds, @PathVariable long signId, @PathVariable long videoId) {
    services.video().changeVideoAssociates(videoId, associateVideosIds);

    log.info("Change video (id={}) associates, ids={}", videoId, associateVideosIds);
      return "/sign/" + signId + "/" + videoId;
  }

}

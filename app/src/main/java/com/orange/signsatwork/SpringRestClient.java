package com.orange.signsatwork;

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

import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class SpringRestClient {

  @Autowired
  Services services;

  @Autowired
  private AppProfile appProfile;

  public static final String AUTH_SERVER_URI = "https://api.dailymotion.com/oauth/token";



  /*
   * Prepare HTTP Headers.
   */
  private static HttpHeaders getHeaders(){
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    return headers;
  }

  /*
   * Add HTTP Authorization header, using Basic-Authentication to send client-credentials.
   */
  private static HttpHeaders getHeadersWithClientCredentials(){

    HttpHeaders headers = getHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    return headers;
  }

  /*
   * Send a POST request [on /oauth/token] to get an access-token, which will then be send with each request.
   */
  @SuppressWarnings({ "unchecked"})
  public AuthTokenInfo sendTokenRequest(){
    LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) retrieveDailymotionTokens();
    AuthTokenInfo tokenInfo = null;

    if(map!=null){
      tokenInfo = newAuthTokenInfo(map);
    }else{
      System.out.println("No user exist----------");
    }
    return tokenInfo;
  }

  @NotNull
  private AuthTokenInfo newAuthTokenInfo(LinkedHashMap<String, Object> map) {
    AuthTokenInfo tokenInfo;
    tokenInfo = new AuthTokenInfo();
    tokenInfo.setAccess_token((String)map.get("access_token"));
    tokenInfo.setToken_type((String)map.get("token_type"));
    tokenInfo.setRefresh_token((String)map.get("refresh_token"));
    tokenInfo.setExpires_in((Integer)map.get("expires_in"));
    tokenInfo.setScope((String)map.get("scope"));
    tokenInfo.setUid((String)map.get("uid"));

    log.warn("sendTokenRequest : refresh_token = {}, expires_in = {}, scope = {}, uid = {}",
      tokenInfo.getRefresh_token(), tokenInfo.getExpires_in(), tokenInfo.getScope(), tokenInfo.getUid());
    return tokenInfo;
  }

  public Map retrieveDailymotionTokens() {
    RestTemplate restTemplate = buildRestTemplate();

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", appProfile.dailymotionAccess().grantType);
    body.add("client_id", appProfile.dailymotionAccess().clientId);
    body.add("client_secret", appProfile.dailymotionAccess().clientSecret);
    body.add("username", appProfile.dailymotionAccess().username);
    body.add("password", appProfile.dailymotionAccess().password);
    body.add("scope", "manage_videos");

    HttpEntity<?> request = new HttpEntity<Object>(body, getHeadersWithClientCredentials());
    ResponseEntity<LinkedHashMap> response = restTemplate.exchange(AUTH_SERVER_URI, HttpMethod.POST, request, LinkedHashMap.class);
    return response.getBody();
  }

  public RestTemplate buildRestTemplate() {
    return appProfile.proxy().noProxy ?
      new RestTemplate() :
      buildRestTemplateForProxy();
  }

  private RestTemplate buildRestTemplateForProxy() {
    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    Proxy proxy = new Proxy(Proxy.Type.HTTP,
      new InetSocketAddress(appProfile.proxy().proxyHost, appProfile.proxy().proxyPort));
    clientHttpRequestFactory.setProxy(proxy);
    return new RestTemplate(clientHttpRequestFactory);
  }
}

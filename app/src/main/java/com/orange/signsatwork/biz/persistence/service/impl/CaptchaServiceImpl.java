package com.orange.signsatwork.biz.persistence.service.impl;

import com.orange.signsatwork.AppProfile;
import com.orange.signsatwork.biz.persistence.service.CaptchaService;
import com.orange.signsatwork.biz.persistence.service.ReCaptchaInvalidException;
import com.orange.signsatwork.biz.persistence.service.ReCaptchaUnavailableException;
import com.orange.signsatwork.biz.webservice.controller.GoogleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.regex.Pattern;


@Service("captchaService")
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private AppProfile appProfile;

  @Autowired
  private ReCaptchaAttemptService reCaptchaAttemptService;

  private static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

  @Override
  public void processResponse(final String response) {
    log.debug("Attempting to validate response {}", response);

    if (reCaptchaAttemptService.isBlocked(getClientIP())) {
      throw new ReCaptchaInvalidException("Client exceeded maximum number of failed attempts");
    }

    if (!responseSanityCheck(response)) {
      throw new ReCaptchaInvalidException("Response contains invalid characters");
    }

    final URI verifyUri = URI.create(String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s", getReCaptchaSecret(), response, getClientIP()));
    try {
      final GoogleResponse googleResponse = new RestTemplate().getForObject(verifyUri, GoogleResponse.class);
      log.debug("Google's response: {} ", googleResponse.toString());

      if (!googleResponse.isSuccess()) {
        if (googleResponse.hasClientError()) {
          reCaptchaAttemptService.reCaptchaFailed(getClientIP());
        }
        throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
      }
    } catch (RestClientException rce) {
      throw new ReCaptchaUnavailableException("Registration unavailable at this time.  Please try again later.", rce);
    }
    reCaptchaAttemptService.reCaptchaSucceeded(getClientIP());
  }

  private boolean responseSanityCheck(final String response) {
    return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
  }

  @Override
  public String getReCaptchaSite() {
    return appProfile.googleAccess().site;
  }

  @Override
  public String getReCaptchaSecret() {
    return appProfile.googleAccess().secret;
  }

  private String getClientIP() {
    final String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
      return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
  }

}

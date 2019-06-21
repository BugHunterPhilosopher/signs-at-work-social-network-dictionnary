package com.orange.signsatwork.biz.persistence.service;

public interface CaptchaService {

  void processResponse(final String response) throws ReCaptchaInvalidException;

  String getReCaptchaSite();

  String getReCaptchaSecret();

}

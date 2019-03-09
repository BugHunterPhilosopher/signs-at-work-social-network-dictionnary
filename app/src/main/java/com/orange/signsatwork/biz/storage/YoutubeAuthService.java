package com.orange.signsatwork.biz.storage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.orange.signsatwork.YoutubeAccess;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class YoutubeAuthService {

  /**
   * Authorizes the installed application to access user's protected data.
   *
   */
  public static Credential authorize(final YoutubeAccess youtubeAccess)
    throws IOException, GeneralSecurityException {
    GoogleCredential credential = new GoogleCredential.Builder()
      .setTransport(new NetHttpTransport())
      .setJsonFactory(new JacksonFactory())
      .setServiceAccountId(youtubeAccess.accountId)
      .setServiceAccountPrivateKeyFromP12File(new File(youtubeAccess.privateKey))
      .setServiceAccountScopes(Collections.singleton(YouTubeScopes.YOUTUBE_UPLOAD))
      .setServiceAccountUser(youtubeAccess.username)
      .build();

    return credential;
  }

}

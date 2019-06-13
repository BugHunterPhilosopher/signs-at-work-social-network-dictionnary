package com.orange.signsatwork.biz.storage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.orange.signsatwork.YoutubeAccess;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class YoutubeAuthService {

  /**
   * Authorizes the installed application to access user's protected data.
   *
   */
  public static Credential authorize(final YoutubeAccess youtubeAccess)
    throws IOException {

    // This creates the credentials datastore at ~/sign-credentialDatastore}
    FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/sign-credentialDatastore"));
    DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore("credentialDatastore");

    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        new NetHttpTransport(), new JacksonFactory(), youtubeAccess.accountId, youtubeAccess.username,
      Collections.singleton(YouTubeScopes.YOUTUBE_UPLOAD))
      .setAccessType("offline").setApprovalPrompt("force").setCredentialDataStore(datastore)
      .build();

    // Build the local server and bind it to port 8985
    LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8985).setHost("signes.bougetesmains.club").build();

    // Authorize.
    return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
  }

}

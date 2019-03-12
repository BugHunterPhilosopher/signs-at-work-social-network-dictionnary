package com.orange.signsatwork.biz.storage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Upload a video to the authenticated user's channel. Use OAuth 2.0 to
 * authorize the request. Note that you must add your video files to the
 * project folder to upload them with this application.
 *
 * @author Jeremy Walker
 */
@Slf4j
public class UploadVideoToYoutubeService {
  /**
   * Define a global variable that specifies the MIME type of the video
   * being uploaded.
   */
  private static final String VIDEO_FILE_FORMAT = "video/*";

  /**
   * Upload the user-selected video to the user's YouTube channel. The code
   * looks for the video in the application's project folder and uses OAuth
   * 2.0 to authorize the API request.
   *
   */
  public static void uploadToYoutube(final String videoFile) {

    // This OAuth 2.0 access scope allows an application to upload files
    // to the authenticated user's YouTube channel, but doesn't allow
    // other types of access.
    List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

    try {
      // Authorize the request.
      Credential credential = YoutubeAuthService.authorize(scopes, "uploadvideo");

      // This object is used to make YouTube Data API requests.
      final YouTube youtube = new YouTube.Builder(YoutubeAuthService.HTTP_TRANSPORT, YoutubeAuthService.JSON_FACTORY, credential).setApplicationName(
        "bouge-tes-mains").build();

      // Add extra information to the video before uploading.
      Video videoObjectDefiningMetadata = new Video();

      // Set the video to be publicly visible. This is the default
      // setting. Other supporting settings are "unlisted" and "private."
      VideoStatus status = new VideoStatus();
      status.setPrivacyStatus("private");
      videoObjectDefiningMetadata.setStatus(status);

      // Most of the video's metadata is set on the VideoSnippet object.
      VideoSnippet snippet = new VideoSnippet();

      // This code uses a Calendar instance to create a unique name and
      // description for test purposes so that you can easily upload
      // multiple files. You should remove this code from your project
      // and use your own standard names instead.
      Calendar cal = Calendar.getInstance();
      snippet.setTitle("Test Upload via Java on " + cal.getTime());
      snippet.setDescription(
        "Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

      // Set the keyword tags that you want to associate with the video.
      List<String> tags = new ArrayList<>();
      tags.add("Bouge Tes Mains");
      tags.add("LSF");
      snippet.setTags(tags);

      // Add the completed snippet object to the video resource.
      videoObjectDefiningMetadata.setSnippet(snippet);

      InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, new FileInputStream(videoFile));

      // Insert the video. The command sends three arguments. The first
      // specifies which information the API request is setting and which
      // information the API response should return. The second argument
      // is the video resource that contains metadata about the new video.
      // The third argument is the actual video content.
      YouTube.Videos.Insert videoInsert = youtube.videos()
        .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

      // Set the upload type and add an event listener.
      MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

      // Indicate whether direct media upload is enabled. A value of
      // "True" indicates that direct media upload is enabled and that
      // the entire media content will be uploaded in a single request.
      // A value of "False," which is the default, indicates that the
      // request will use the resumable media upload protocol, which
      // supports the ability to resume an upload operation after a
      // network interruption or other transmission failure, saving
      // time and bandwidth in the event of network failures.
      uploader.setDirectUploadEnabled(false);

      MediaHttpUploaderProgressListener progressListener = uploader1 -> {
        switch (uploader1.getUploadState()) {
          case INITIATION_STARTED:
            log.info("Initiation Started");
            break;
          case INITIATION_COMPLETE:
            log.info("Initiation Completed");
            break;
          case MEDIA_IN_PROGRESS:
            log.info("Upload in progress");
            log.info("Upload percentage: " + uploader1.getProgress());
            break;
          case MEDIA_COMPLETE:
            log.info("Upload Completed!");
            break;
          case NOT_STARTED:
            log.info("Upload Not Started!");
            break;
        }
      };
      uploader.setProgressListener(progressListener);

      // Call the API and upload the video.
      Video returnedVideo = videoInsert.execute();

      // Print data about the newly inserted video from the API response.
      log.info("\n================== Returned Video ==================\n");
      log.info("  - Id: " + returnedVideo.getId());
      log.info("  - Title: " + returnedVideo.getSnippet().getTitle());
      log.info("  - Tags: " + returnedVideo.getSnippet().getTags());
      log.info("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
      log.info("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

    } catch (GoogleJsonResponseException e) {
      log.error("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
        + e.getDetails().getMessage(), e);
    } catch (IOException e) {
      log.error("IOException: " + e.getMessage(), e);
    } catch (Throwable t) {
      log.error("Throwable: " + t.getMessage(), t);
    }
  }

}


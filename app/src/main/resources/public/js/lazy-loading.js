/*
 * #%L
 * Telsigne
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

var SIGN_HIDDEN_CLASS = 'sign-view-hidden';
var VIDEO_HIDDEN_CLASS = 'video-view-hidden';
var NB_SIGN_VIEWS_INC = 8;
var NB_VIDEO_VIEWS_INC = 8;
var REVEAL_DURATION_MS = 1000;

var modeSearch;

var addNewSuggestRequest = document.getElementById("add-new-suggest-request");
var signsContainer = document.getElementById("signs-container");
/** Live node list (updated while we iterate over it...) */
if (signsContainer != null) {
  var signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);
  var signsCount =  $("#signs-container").children("div").length;
}

var displayedSignsCount = 0;
var videosContainer = document.getElementById("videos-container");
/** Live node list (updated while we iterate over it...) */
if (videosContainer != null) {
  var videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
  var videosCount =  $("#videos-container").children("div").length;
}

var signAvailable = document.getElementById("sign_available");
var videoAvailable = document.getElementById("video_available");

var displayedVideosCount = 0;

var search_criteria1 = document.getElementById("search-criteria1");
var mediaTypeCriteria1 = document.getElementsByClassName("my-button-lsf")[0];
var mediaTypeCriteria2 = document.getElementsByClassName("my-button-lpc")[0];
var conditions = document.getElementById("conditions");

var accentMap = {
  "é": "e",
  "è": "e",
  "ê": "e",
  "à": "a",
  "â": "a",
  "î": "i",
  "ô": "o",
  "ù": "u",
  "î": "i",
  "ç": "c"
};

var dropdownFilter = document.getElementById("dropdown-filter");

var normalize = function( term ) {
  if (typeof term == "undefined") {
    return ""
  }

  var ret = "";
  for ( var i = 0; i < term.length; i++ ) {
    ret += accentMap[ term.charAt(i) ] || term.charAt(i);
  }
  return ret;
};

var nb = document.getElementById("nb");

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position){
    position = position || 0;
    return this.substr(position, searchString.length) === searchString;
  };
}

function showSignView(signView) {
  signView.style.opacity = "0";
  signView.className = signView.className.replace(SIGN_HIDDEN_CLASS, '');
  var img = signView.getElementsByTagName('img')[0];

  if (typeof img != 'undefined') {
    if ((typeof img.src == 'undefined') || (img.src.endsWith("null"))) {
      if (typeof img.dataset.src == 'undefined') {
        img.src = '/img/video_thumbnail.png';
      } else {
        var thumbnailUrl = img.dataset.src;
        if (thumbnailUrl.indexOf('/files/') != -1) {
          img.src = thumbnailUrl;
        } else {
          img.src = '/files/' + thumbnailUrl;
        }
      }
    } else if ((typeof img.src != 'undefined') && (img.src.endsWith('.gif') || img.src.endsWith('.png'))) {
      // Nothing special to do here
    } else if ((typeof img.src != 'undefined') && (img.src.indexOf('/files/') != -1)) {
      img.src = img.src.substring(img.src.indexOf("/files/") + 7); // 7 = (length of "/files/")
    }
  }

  $(signView).fadeTo(REVEAL_DURATION_MS, 100);
}

function showVideoView(videoView) {
  videoView.style.opacity = "0";
  videoView.className = videoView.className.replace(VIDEO_HIDDEN_CLASS, '');
  var img = videoView.getElementsByTagName('img')[0];
  var thumbnailUrl = img.dataset.src;
  img.src = thumbnailUrl;
  $(videoView).fadeTo(REVEAL_DURATION_MS, 1);
}

function showNextSignViews() {
  var viewsToReveal = [];
  for (var i = 0; i < NB_SIGN_VIEWS_INC && i < signViewsHidden.length; i++) {
    viewsToReveal.push(signViewsHidden[i]);
  }
  for (var i = 0; i < viewsToReveal.length; i++) {
    showSignView(viewsToReveal[i]);
    displayedSignsCount++;
  }
  console.log("total: " + signsCount + ", hidden: " + signViewsHidden.length + ", displayedSignsCount: " + displayedSignsCount);
}

function showNextVideoViews() {
  var viewsToReveal = [];
  if (videoViewsHidden != null) {
    for (var i = 0; i < NB_VIDEO_VIEWS_INC && i < videoViewsHidden.length; i++) {
      viewsToReveal.push(videoViewsHidden[i]);
    }
    for (var i = 0; i < viewsToReveal.length; i++) {
      showVideoView(viewsToReveal[i]);
      displayedVideosCount++;
    }
    console.log("total: " + videosCount + ", hidden: " + videoViewsHidden.length + ", displayedVideosCount: " + displayedVideosCount);
  }
}

function onScroll(event) {
  if (!dropdownFilter.classList.contains("open")) {
    if (signsContainer != null) {
      var noMoreHiddenSigns = signViewsHidden.length === 0;
      var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height() / 5;

      if (search_criteria1.value == "") {
        if (!noMoreHiddenSigns && closeToBottom) {
          showNextSignViews();
        }
      }
    } else {
      if (videoViewsHidden != null) {
        var noMoreHiddenVideos = videoViewsHidden.length === 0;
        var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height() / 5;
        if (search_criteria1.value == "") {
          if (!noMoreHiddenVideos && closeToBottom) {
            showNextVideoViews();
          }
        }
      }
    }
  }

  refreshLogos();
}

var resetSearch = function() {
    $(addNewSuggestRequest).hide();
    $(signAvailable).hide();
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $("#signs-container").children("div").each(function () {
      if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
        $(this).addClass(SIGN_HIDDEN_CLASS);
        $(this).hide();
      }
    });
    displayedSignsCount = 0;
    if (modeSearch === "false") {
      initWithFirstSigns();
    } else {
      $(nb).hide();
    }

  $('#search-criteria1').val('');
}

var conditionsState = "or";

function condition(event) {
  if ($('.my-button-lpc').hasClass('my-button-lpc-active') && $('.my-button-lsf').hasClass('my-button-lsf-active')) {

    if ($('#conditions').hasClass('my-button-conditions-and')) {
      conditionsState = "and";
    } else if ($('#conditions').hasClass('my-button-conditions-or')) {
      conditionsState = "or";
    } else {
      conditionsState = "none";
    }

    var conditionsNextState = "";

    if (conditionsState == 'and') {
      conditionsNextState = "or";
    } else if (conditionsState == 'or') {
      conditionsNextState = "and";
    } else {
      conditionsNextState = "and";
    }

    if (conditionsNextState == 'and') {
      $('#conditions').addClass('my-button-conditions-and')
      $('#conditions').removeClass('my-button-conditions-or')
      $('#conditions').removeClass('my-button-conditions-none')
      resetSearch();
    } else if (conditionsNextState == 'or') {
      $('#conditions').removeClass('my-button-conditions-and')
      $('#conditions').addClass('my-button-conditions-or')
      $('#conditions').removeClass('my-button-conditions-none')
      resetSearch();
    } else {
      $('#conditions').removeClass('my-button-conditions-and')
      $('#conditions').removeClass('my-button-conditions-or')
      $('#conditions').addClass('my-button-conditions-none')
    }

    conditionsState = conditionsNextState;
  }
}

function search(event) {
  function arraysEqual(a, b) {
    if (a.sort) {
      a.sort();
    }

    if (b.sort) {
      b.sort();
    }

    if (a === b) return true;
    if (a == null || b == null) return false;
    if (a.length != b.length) return false;

    for (var i = 0; i < a.length; ++i) {
      if (a[i] !== b[i]) return false;
    }
    return true;
  }

  console.log(event.target.className);
  var searchToReset = false;

  if (event.target.className.indexOf('lsf') != -1) {
    if ($('.my-button-lsf').hasClass('my-button-lsf-active') && !$('.my-button-lpc').hasClass('my-button-lpc-active')) {
      $('.my-button-lsf').toggleClass('my-button-lsf-active');
      $('.my-button-lpc').toggleClass('my-button-lpc-active');
    } else {
      $('.my-button-lsf').toggleClass('my-button-lsf-active');
    }

    searchToReset = true;
  }

  if (event.target.className.indexOf('lpc') != -1) {
    if ($('.my-button-lpc').hasClass('my-button-lpc-active') && !$('.my-button-lsf').hasClass('my-button-lsf-active')) {
      $('.my-button-lpc').toggleClass('my-button-lpc-active');
      $('.my-button-lsf').toggleClass('my-button-lsf-active');
    } else {
      $('.my-button-lpc').toggleClass('my-button-lpc-active');
    }

    searchToReset = true;
  }

  if (searchToReset) {
    if ($('.my-button-lpc').hasClass('my-button-lpc-active') && $('.my-button-lsf').hasClass('my-button-lsf-active')) {
      conditionsState = 'or';
      $('#conditions').addClass('my-button-conditions-or');
      $('#conditions').removeClass('my-button-conditions-and');
      $('#conditions').removeClass('my-button-conditions-none');
    } else {
      conditionsState = 'none';
      $('#conditions').removeClass('my-button-conditions-and');
      $('#conditions').removeClass('my-button-conditions-or');
      $('#conditions').addClass('my-button-conditions-none');
    }

    resetSearch();
    return false;
  }

  var display = 0;
  $(addNewSuggestRequest).hide();

  function getMediaTypesInSearchField() {
    var lpc = $('.my-button-lpc').hasClass('my-button-lpc-active') ? 'Lf.P.C.' : '';
    var lsf = $('.my-button-lsf').hasClass('my-button-lsf-active') ? 'L.S.F.' : '';
    var mediaTypesToSearch = [];
    if (lpc != '') {
      mediaTypesToSearch.push(lpc);
    }
    if (lsf != '') {
      mediaTypesToSearch.push(lsf);
    }
    return mediaTypesToSearch;
  }

  function matchesNameOrTag(nameOrTag) {
    return nameOrTag.toUpperCase().indexOf(searchString.toUpperCase()) != -1;
  }

  function matchesSearchCondition(mediaTypesInSign, mediaTypesInSearchField) {
    return (conditionsState == 'and' && mediaTypesInSign != '' && arraysEqual(mediaTypesInSearchField, mediaTypesInSign.split(','))) ||
      (conditionsState == 'or' && mediaTypesInSign != '' && (mediaTypesInSign.indexOf(',') != -1) && mediaTypesInSearchField.some(r => mediaTypesInSign.split(',').includes(r))) ||
      (conditionsState == 'or' && mediaTypesInSign != '' && (mediaTypesInSign.indexOf(',') == -1) && mediaTypesInSearchField.includes(mediaTypesInSign)) ||
      (conditionsState == 'none' && mediaTypesInSign != '' && (mediaTypesInSign.indexOf(',') == -1) && mediaTypesInSearchField == mediaTypesInSign) ||
      (conditionsState == 'none' && mediaTypesInSign != '' && (mediaTypesInSign.indexOf(',') != -1) && mediaTypesInSign.split(',').some(r => mediaTypesInSearchField.includes(r)));
  }

  function processHidden(img) {
    if ($(this).hasClass(SIGN_HIDDEN_CLASS)) {
      $(this).removeClass(SIGN_HIDDEN_CLASS);
      var thumbnailUrl = img.dataset.src;
      img.src = thumbnailUrl;
      displayedSignsCount++;
    }
  }

  function process(elem, wasShown) {
    elem.show();
    display++;
    wasShown = true;
    return wasShown;
  }

  if (signsContainer != null) {
    var searchString = normalize($(this).val());

    if (searchString != "") {
      $("#signs-container").children("div").each(function () {
        var wasShown = false;
        var elem = $(this);

        $("#reset").css("visibility", "visible");

        var signName = normalize($(this).attr("id"));
        var mediaTypesInSign = normalize($(this).attr("data-media-types"));
        var img = $(this).find("img")[0];

        if (matchesNameOrTag(signName)) {
          var mediaTypesInSearchField = getMediaTypesInSearchField();

          if (matchesSearchCondition(mediaTypesInSign, mediaTypesInSearchField)) {
            processHidden.call(this, img);
            wasShown = process(elem, wasShown);
          }
        }

        var tags = normalize($(this).attr("data-tags"));

        tags.split(',').forEach(function(tag) {
          var img2 = $(this).find("img")[0];
          if (matchesNameOrTag(tag)) {
            var mediaTypesInSearchField = getMediaTypesInSearchField();

            if (matchesSearchCondition(mediaTypesInSign, mediaTypesInSearchField)) {
              processHidden.call(this, img2);
              wasShown = process(elem, wasShown);
            }
          }
        });

        if (!wasShown) {
          elem.hide();
        }
      });

      console.log("display "+display);
      nb.innerHTML = "("+display+")";
      $(nb).show();
      if (display == 0) {
        $(signAvailable).hide();
        $(addNewSuggestRequest).show();
      } else {
        $(signAvailable).show();
        $(addNewSuggestRequest).hide();
      }
    } else {
      resetSearch();
    }
  } else {
    var searchString = normalize($(this).val());

    if (searchString!="") {
      $("#videos-container").children("div").each(function () {
        $("#reset").css("visibility", "visible");
        /*$("#reset").show();*/
        var s = normalize($(this).attr("id"));
        var img = $(this).find("img")[0];

        if (s.toUpperCase().indexOf(searchString.toUpperCase()) != -1) {
          if ($(this).hasClass(VIDEO_HIDDEN_CLASS)) {
            $(this).removeClass(VIDEO_HIDDEN_CLASS);
            var thumbnailUrl = img.dataset.src;
            img.src = thumbnailUrl;
            displayedVideosCount++;
          }
          $(this).show();
          display++;
        } else {
          $(this).hide();
        }
      });
      console.log("display "+displayedSignsCount);
      nb.innerHTML = "("+displayedSignsCount+")";
      $(nb).show();
      if (display == 0) {
        $(videoAvailable).hide();
        $(addNewSuggestRequest).show();
      } else {
        $(videoAvailable).show();
        $(addNewSuggestRequest).hide();
      }
    } else {
      $(addNewSuggestRequest).hide();
      $(videoAvailable).hide();
      $("#reset").css("visibility", "hidden");
      /*$("#reset").hide();*/
      $("#videos-container").children("div").each(function () {
        if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          $(this).addClass(VIDEO_HIDDEN_CLASS);
          $(this).hide();
        }});
      displayedVideosCount = 0;
      if (modeSearch === "false") {
        initWithFirstVideos();
      } else {
        $(nb).hide();
      }
    }
  }

  refreshLogos();
}

var refreshLogos = function() {
  $('div.logo').each(function () {
    if ($(this).data("media-types").indexOf("L.S.F.") == -1) {
      $(this).find("img.logolsf").hide();
    } else {
      $(this).find("img.logolsf").attr('src', '/img/lsf.jpg')
    }
    if ($(this).data("media-types").indexOf("Lf.P.C.") == -1) {
      $(this).find("img.logolpc").hide();
    } else {
      $(this).find("img.logolpc").attr('src', '/img/lpc.jpg')
    }
  });
}

$(refreshLogos());

function searchSignAfterReload(search_value) {
  var display = 0;
  $(addNewSuggestRequest).hide();
  console.log("search_value "+search_value);
  var g = normalize(search_value);

  if (g!="") {
    $("#signs-container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
     /* $("#reset").show();*/
      var s = normalize($(this).attr("id"));
      var img = $(this).find("img")[0];
      /*if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {*/
      if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
        if ($(this).hasClass(SIGN_HIDDEN_CLASS)) {
          $(this).removeClass(SIGN_HIDDEN_CLASS);
          var thumbnailUrl = img.dataset.src;
          img.src = thumbnailUrl;
          displayedSignsCount++;
        }
        $(this).show();
        display++;
      }
      else {
        $(this).hide();
      }
    });
    console.log("display "+display);
    nb.innerHTML = "("+display+")";
    $(nb).show();
    if (display == 0) {
      $(signAvailable).hide();
      $(addNewSuggestRequest).show();
    } else {
      $(signAvailable).show();
      $(addNewSuggestRequest).hide();
    }
  } else {
    $(addNewSuggestRequest).hide();
    $(signAvailable).hide();
    $("#reset").css("visibility", "hidden");
    $("#reset").hide();
    $("#signs-container").children("div").each(function () {
      if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
        $(this).addClass(SIGN_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedSignsCount = 0;
    if (modeSearch === "false") {
      initWithFirstSigns();
    } else {
      $(nb).hide();
    }
  }
}

function searchVideoAfterReload(search_value) {
  var display = 0;
  $(addNewSuggestRequest).hide();
  console.log("search_value "+search_value);
  var g = normalize(search_value);

  if (g!="") {
    $("#videos-container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
      /*$("#reset").show();*/
      var s = normalize($(this).attr("id"));
      var img = $(this).find("img")[0];
      /*if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {*/
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
        if ($(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          $(this).removeClass(VIDEO_HIDDEN_CLASS);
          var thumbnailUrl = img.dataset.src;
          img.src = thumbnailUrl;
          displayedVideosCount++;
        }
        $(this).show();
        display++;
      }
      else {
        $(this).hide();
      }
    });
    console.log("display "+display);
    nb.innerHTML = "("+display+")";
    $(nb).show();
    if (display == 0) {
      $(videoAvailable).hide();
      $(addNewSuggestRequest).show();
    } else {
      $(videoAvailable).show();
      $(addNewSuggestRequest).hide();
    }
  } else {
    $(addNewSuggestRequest).hide();
    $(videoAvailable).hide();
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $("#videos-container").children("div").each(function () {
      if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
        $(this).addClass(VIDEO_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedVideosCount = 0;
    if (modeSearch === "false") {
      initWithFirstVideos();
    } else {
      $(nb).hide();
    }
  }
}

function scrollBarVisible() {
  return $(document).height() > $(window).height();
}

function initWithFirstSigns() {
  nb.innerHTML = "("+signsCount+")";
  $(nb).show();
  do {
    showNextSignViews();
  } while(!scrollBarVisible() && displayedSignsCount != signsCount);

}

function initWithFirstVideos() {
  nb.innerHTML = "("+videosCount+")";
  $(nb).show();
  do {
    showNextVideoViews();
  } while(!scrollBarVisible() && displayedVideosCount != videosCount);

}

function onReset(event) {

  $(addNewSuggestRequest).hide();
  if (signsContainer != null) {
    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $(signAvailable).hide();
    $("#signs-container").children("div").each(function () {
      if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
        $(this).addClass(SIGN_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedSignsCount = 0;
    if (modeSearch === "false") {
      initWithFirstSigns();
    } else {
      $(nb).hide();
    }
  } else {
    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $(videoAvailable).hide();
    $("#videos-container").children("div").each(function () {
      if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
        $(this).addClass(VIDEO_HIDDEN_CLASS);
        $(this).hide();
    }});
    displayedVideosCount = 0;
    if (modeSearch === "false") {
      initWithFirstVideos();
    } else {
      $(nb).hide();
    }
  }

}



function main() {
  // show first signs at load
  console.log("main "+window.location.href+" "+window.location.search);
  var url_param = window.location.search;
  var isSearch = url_param.indexOf("isSearch");
  console.log("isSearch "+isSearch);
  modeSearch = url_param.substring(url_param.indexOf("isSearch") +9);
  console.log("modeSearch "+modeSearch);
  if (modeSearch === "false") {
    console.log("false");
  } else {
    console.log("true");
  }

  document.addEventListener('scroll', onScroll);
  search_criteria1.addEventListener('keyup', search);

  mediaTypeCriteria1.addEventListener('click', search);
  mediaTypeCriteria2.addEventListener('click', search);
  conditions.addEventListener('click', condition);

  var button_reset = document.getElementById("reset");
  if (button_reset != null) {
    button_reset.addEventListener('click', onReset);
  }

  if (modeSearch === "false") {
    if (signsContainer != null) {
      initWithFirstSigns();
    } else {
      if (videosContainer != null) {
        initWithFirstVideos();
      }
    }
  } else {
    $(nb).hide();
  }
}

function onFiltreSign(event, href) {
  console.log("onFiltre");
  event.preventDefault();
  console.log("href "+href);
  $.ajax({
    url: href,
    context: document.body,
    success: function (response) {
      console.log("Success ");
      document.getElementById("frame-signs").innerHTML = response;
      signsContainer = document.getElementById("signs-container");
      signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);
      signsCount = $("#signs-container").children("div").length;
      displayedSignsCount = 0;
      videosContainer = null;
      addNewSuggestRequest = document.getElementById("add-new-suggest-request");
      nb = document.getElementById("nb");
      signAvailable = document.getElementById("sign_available");
      dropdownFilter = document.getElementById("dropdown-filter");
      if (signsCount == 0) {
        $(search_criteria1).hide();
        $("#reset").css("visibility", "hidden");
      } else {
        $(search_criteria1).show();
        if (search_criteria1.value != "") {
          console.log("search value " + search_criteria1.value);
          searchSignAfterReload(search_criteria1.value);
        } else {
          $("#signs-container").children("div").each(function () {
            $(this).hide();
          });
          main();
        }
      }
    },
    error: function (response) {
      console.log("Erreur ");
    }
  })
}


function onFiltreVideo(event, href) {
  console.log("onFiltre");
  event.preventDefault();
  console.log("href "+href);
  $.ajax({
    url: href,
    context: document.body,
    success: function (response) {
      console.log("Success ");
      document.getElementById("frame-signs").innerHTML = response;
      videosContainer = document.getElementById("videos-container");
      if (videosContainer != null) {
        videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
        videosCount = $("#videos-container").children("div").length;
      } else {
        videosCount = 0;
      }
      displayedVideosCount = 0;
      signsContainer = null;
      addNewSuggestRequest = document.getElementById("add-new-suggest-request");
      nb = document.getElementById("nb");
      videoAvailable = document.getElementById("video_available");
      dropdownFilter = document.getElementById("dropdown-filter");
      if (videosCount == 0) {
        $(search_criteria1).hide();
        $("#reset").css("visibility", "hidden");
      } else {
        $(search_criteria1).show();
        if (search_criteria1.value != "") {
          console.log("search value " + search_criteria1.value);
          searchVideoAfterReload(search_criteria1.value);
        } else {
          $("#videos-container").children("div").each(function () {
            $(this).hide();
          });
          main();
        }
      }
    },
    error: function (response) {
      console.log("Erreur ");
    }
  })
}

(function signOrvideoViewsLazyLoading($) {

  main();


})($);

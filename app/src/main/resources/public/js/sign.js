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
console.log("Cool, sign.js is loaded :)");

var errorSelectedSpan = document.getElementById('errorSelectedSpan');


var $formUploadSelectedVideoFile = $('#uploadSelectedVideoFile');
$formUploadSelectedVideoFile.on('submit', function(event) {
  //document.getElementById('submitButtonFileDailymotion').disabled = true;
  if (document.getElementById("InputFile").value) {
    $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
    $(".spinner").css("z-index", "1500").visibility = "visible";
    $("#submitButtonFileDailymotion").css("color", "black");
    $("#submitButtonGifFile").css("color", "black");
    var $form = $(this);
    var formdata = new FormData($form[0]);
    var data = (formdata !== null) ? formdata : $form.serialize();

    event.preventDefault();
    $.ajaxSetup({
      headers:
        { 'X-CSRF-TOKEN': $("input[name='_csrf']").attr("value") }
    });
    var token = $("input[name='_csrf']").attr("value");
    $.ajax({
      url: $formUploadSelectedVideoFile.attr('action'),
      type: 'post',
      data: data,
      contentType: false,
      processData: false,
      //dataType: 'json',
      beforeSend: function(xhr){
      xhr.setRequestHeader("X-CSRF-TOKEN", token);
      },
      success: function (response) {
        //var url = "/sign/"+response;
        var url = response;
        window.location = url;
        errorSelectedSpan.style.visibility = "hidden";
        $(".spinner").visibility = "hidden";
        console.log("Success " + response);
      },
      error: function (response) {
        errorSelectedSpan.textContent = response.responseText;
        errorSelectedSpan.style.visibility = "visible";
        $(".spinner").css("z-index", "-1").css("opacity", "0.1");
        $(".spinner").visibility = "hidden";
        console.log("Erreur " + response.responseText);
      }
    })
  } else {
    event.preventDefault();
    errorSelectedSpan.textContent = "Vous devez séléctionner un fichier";
    errorSelectedSpan.style.visibility = "visible";
  }

});

var $uploadSelectedGifFile = $('#uploadSelectedGifFile');
$uploadSelectedGifFile.on('submit', function(event) {
  //document.getElementById('submitButtonFileDailymotion').disabled = true;
  if (document.getElementById("InputFileGif").value) {
    $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
    $(".spinner").css("z-index", "1500").visibility = "visible";
    $("#submitButtonFileDailymotion").css("color", "black");
    $("#submitButtonGifFile").css("color", "black");
    var $form = $(this);
    var formdata = new FormData($form[0]);
    var data = (formdata !== null) ? formdata : $form.serialize();

    event.preventDefault();
    $.ajaxSetup({
      headers:
        { 'X-CSRF-TOKEN': $("input[name='_csrf']").attr("value") }
    });
    var token = $("input[name='_csrf']").attr("value");
    $.ajax({
      url: $uploadSelectedGifFile.attr('action') + '/' + ($('#lsf:checked').val() == "LSF" ? "LSF" : "LPC"),
      type: 'post',
      data: data,
      contentType: false,
      processData: false,
      //dataType: 'json',
      beforeSend: function(xhr){
      xhr.setRequestHeader("X-CSRF-TOKEN", token);
      },
      success: function (response) {
        //var url = "/sign/"+response;
        errorSelectedSpan.style.visibility = "hidden";
        $(".spinner").visibility = "hidden";
        console.log("Success " + response);

        if (response.startsWith('/sec/sign')) {
          window.location = response;
        } else {
          $("html").html(response);
        }
      },
      error: function (response) {
        errorSelectedSpan.textContent = response.responseText;
        errorSelectedSpan.style.visibility = "visible";
        $(".spinner").css("z-index", "-1").css("opacity", "0.1");
        $(".spinner").visibility = "hidden";
        console.log("Erreur " + response.responseText);
      }
    })
  } else {
    event.preventDefault();
    errorSelectedSpan.textContent = "Vous devez séléctionner un fichier";
    errorSelectedSpan.style.visibility = "visible";
  }

});

$formUploadSelectedVideoFile.on('input', function(event) {
  document.getElementById('errorSelectedSpan').style.visibility="hidden";
});

var $add_video_file_dailymotion = $('#add_video_file_dailymotion');
$add_video_file_dailymotion.on('hidden.bs.modal', function() {
 console.log("hidden add_video_file_dailymotion modal");
  //document.getElementById('submitButtonFileDailymotion').disabled = true;
  if ($('#uploadSelectedVideoFile').find('#errorSelectedSpan').length) {
    errorSelectedSpan.style.visibility="hidden";
    $('#signNameSelected').val("");
  }
});

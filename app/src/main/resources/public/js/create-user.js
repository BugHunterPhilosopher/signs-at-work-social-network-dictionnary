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

var token = $("input[name='_csrf']").attr("value");
var alertClass = "alert alert-danger";

function onAccepted() {
  $('#cguAccepted').css('display', 'none');
  $('#accountRequest').removeClass("hidden");
  $('#accountRequest #accountProfile ').removeClass("hidden");
};

$(function() {
  console.log("Cool, create_user.js is loaded :)");

  window.inputLastName = document.getElementById('lastName');
  window.inputFirstName = document.getElementById('firstName');
// var inputEntity =document.getElementById('entity');
  window.inputEmail = document.getElementById('mail');
  window.buttonOnNext = document.getElementById('buttonOnNext');

  window.lastName = new Boolean(false);
  window.firstName = new Boolean(false);
// var entity = new Boolean(false);
  window.mail = new Boolean(false);


  window.regexName = new RegExp('[A-Za-z]');
  window.regexEntity = new RegExp('[\\sA-Za-z_:-\\\\/\\\\]');
  window.regexEmail = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');

  window.inputLastName.addEventListener('keyup', checkLastName);
  window.inputFirstName.addEventListener('keyup', checkFirstName);
// inputEntity.addEventListener('keyup',checkEntity);
  window.inputEmail.addEventListener('keyup', checkEmail);
  window.buttonOnNext.addEventListener('click', onNext);

  $('html').bind('keypress', function (e) {
    if (e.keyCode == 13) {
      return false;
    }
  });
});

function checkLastName() {
  var valueLastName = inputLastName.value;

  if (!regexName.test(valueLastName)) {
    $('#lastName').addClass(alertClass);
    $('.blink_me.errorRegexLastName').removeClass("hidden");
    lastName = false;
  }else {
    lastName = true;
    $('#lastName').removeClass(alertClass);
    $('.errorRegexLastName').addClass("hidden");
  }
}

function checkFirstName() {
  var valueFirstName = inputFirstName.value;

  if (!regexName.test(valueFirstName)) {
    $('#firstName').addClass(alertClass);
    $('.errorRegexFirstName').removeClass("hidden");
    firstName = false;
  } else {
    firstName = true;
    $('#firstName').removeClass(alertClass);
    $('.errorRegexFirstName').addClass("hidden");
  }
}

function checkEntity() {
  var valueEntity = inputEntity.value;

  if(!regexEntity.test(valueEntity)) {
    $('#entity').addClass(alertClass);
    $('.errorRegexEntity').removeClass("hidden");
    entity = false;
  }else {
    entity = true;
    $('#entity').removeClass(alertClass);
    $('.errorRegexEntity').addClass("hidden");
  }
}

function checkEmail() {
  var valueEmail = inputEmail.value;

  if(!regexEmail.test(valueEmail)) {
    $('#mail').addClass(alertClass);
    $('.errorRegexEmail').removeClass("hidden");
    mail = false;
  }else {
    mail = true;
    $('#mail').removeClass(alertClass);
    $('.errorRegexEmail').addClass("hidden");
  }
}

function onNext(){

  if( (lastName !=false) &&
    (firstName !=false) &&
    // (entity !=false) &&
    (mail !=false)) {
    $('#accountRequest #accountProfile').addClass("hidden");
    $('#privacySettings').removeClass("hidden");
    $('.btn').removeClass("hidden");
  }else{
    $('.errorSubmit').removeClass("hidden");
    $('#mail').addClass(alertClass);
    // $('#entity').addClass("alert alert-warning");
    $('#firstName').addClass(alertClass);
    $('#lastName').addClass(alertClass);

  }

};

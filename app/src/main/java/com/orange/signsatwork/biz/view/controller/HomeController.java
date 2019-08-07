package com.orange.signsatwork.biz.view.controller;

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

import com.orange.signsatwork.AppProfile;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.persistence.service.impl.CaptchaServiceImpl;
import com.orange.signsatwork.biz.persistence.service.impl.EmailServiceImpl;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.FavoriteCreationView;
import com.orange.signsatwork.biz.view.model.FavoriteModalView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.view.model.SignView2;
import com.orange.signsatwork.biz.view.model.SignsViewSort2;
import com.orange.signsatwork.biz.view.model.UserCreationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;
  @Autowired
  private AppProfile appProfile;
  @Autowired
  private Services services;
  @Autowired
  private UserService userService;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  public EmailServiceImpl emailService;
  @Autowired
  public CaptchaServiceImpl captchaService;


  private static final String HOME_URL = "/";

  @Value("${cgu-url}")
  private String cgu_url;

  @Value("${display-url}")
  private String display_url;

  @RequestMapping("/")
  public String index(HttpServletRequest req, Principal principal, Model model) {
    long t0 = System.currentTimeMillis();
    String pageName;
    if (AuthentModel.isAuthenticated(principal)) {
      pageName = doIndex(req, principal, model);
    } else {
      pageName = "login";
    }

    long dt = System.currentTimeMillis() - t0;
    log.info("[PERF] took " + dt + " ms to process root page request");
    return pageName;
  }

  @RequestMapping(value = "/register", method = RequestMethod.GET)
  public String register(@ModelAttribute UserCreationView userCreationView, Model model) {
    model.addAttribute("title", messageByLocaleService.getMessage("label.form.title"));
    User blankUser = User.create("", "", "", "", "", "", "", "", "");
    UserDB u = userService.register(blankUser, "user");
    model.addAttribute("user", u);
    model.addAttribute("firstName", u.getFirstName());
    model.addAttribute("lastName", u.getLastName());
    model.addAttribute("email", u.getEmail());
    model.addAttribute("password", "");
    model.addAttribute("matchingPassword", "");
    model.addAttribute("site", appProfile.googleAccess().site);
    return "register";
  }

  @RequestMapping(value = "/createUser", method = RequestMethod.POST)
  public String createUser(HttpServletRequest req, @ModelAttribute UserCreationView userCreationView, Model model, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("message", "Failed");
    redirectAttributes.addFlashAttribute("alertClass", "alert-danger");

    try {
      if ((userCreationView.getUsername() == null) || (services.user().withUserName(userCreationView.getUsername()) != null)) {
        return "redirect:/register";
      }
    }
    catch (IllegalStateException e) {
      log.error("Error (mostly expected) retrieving user by username!", e);
    }

    if ((userCreationView.getPassword() == null) || ("".equals(userCreationView.getPassword().trim())) ||
    (userCreationView.getMatchingPassword() == null) || ("".equals(userCreationView.getMatchingPassword().trim())) ||
      (!userCreationView.getPassword().equals(userCreationView.getMatchingPassword()))) {
      return "redirect:/register";
    }

    String response = req.getParameter("g-recaptcha-response");
    captchaService.processResponse(response);

    User user= services.user().create(userCreationView.toUser(), userCreationView.getPassword(), "user");
    model.addAttribute("user", user);

    redirectAttributes.addFlashAttribute("message", "Success");
    redirectAttributes.addFlashAttribute("alertClass", "alert-success");
    return "redirect:/login";
  }

  private String doIndex(HttpServletRequest req, Principal principal, Model model) {
    boolean admin = appSecurityAdmin.isAdmin(principal);
    User user = AuthentModel.addAuthentModelWithUserDetails(model, principal, admin, services.user());
    StringBuffer location = req.getRequestURL();

    model.addAttribute("dailymotion_client_id", appProfile.getDailymotionClientId());
    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));

    if (user != null) {
      model.addAttribute("mail_body", messageByLocaleService.getMessage("share_application_body", new Object[]{user.name(), location}));
    }

//    List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));
    model.addAttribute("signCreationView", new SignCreationView());

    if (AuthentModel.isAuthenticated(principal)) {
      fillModelWithFavorites(model, user);
    }
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    model.addAttribute("isDevProfile", appProfile.isDevProfile());
    model.addAttribute("isAlphabeticAsc", true);
    model.addAttribute("isSearch", true);
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("display_url", display_url);


    return "index";
  }



  private void fillModelWithFavorites(Model model, User user) {
    List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }

  @RequestMapping("/cgu")
  public String cgu(Model model) {

    model.addAttribute("title", messageByLocaleService.getMessage("condition_of_use"));
    model.addAttribute("backUrl", HOME_URL);
    model.addAttribute("cgu_url", cgu_url);
    model.addAttribute("user", new UserCreationView());
    return "cgu";
  }

  @RequestMapping("/sendMail")
  public String sendMail(@ModelAttribute UserCreationView userCreationView) {

    User admin = services.user().getAdmin();

    String body = messageByLocaleService.getMessage("ask_to_create_user_text", new Object[]{userCreationView.getLastName(), userCreationView.getFirstName(), userCreationView.getEntity(),  userCreationView.getEmail(), userCreationView.getUsername(), userCreationView.getPassword()});

    emailService.sendSimpleMessage(admin.email.split(""), messageByLocaleService.getMessage("ask_to_create_user_title"), body );

    return "redirect:/";
  }
}

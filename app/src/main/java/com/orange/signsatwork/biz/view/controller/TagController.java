package com.orange.signsatwork.biz.view.controller;

import com.orange.signsatwork.biz.persistence.model.TagDB;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class TagController {

  @Autowired
  private Services services;
  private static final String HOME_URL = "/";

  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = "/sec/tags")
  public String listTags(Principal principal, Model model) {
    List<TagDB> tags = services.tag().allOrderedByName();

    model.addAttribute("title", "tags");
    model.addAttribute("backUrl", HOME_URL);
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));

    List<String> allTags = tags.stream().map(TagDB::getName).collect(Collectors.toList());
    model.addAttribute("tags", allTags);

    return "tags-list";
  }

}

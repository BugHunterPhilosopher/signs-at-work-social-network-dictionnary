package com.orange.signsatwork;;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class LaunchServerApplication extends SpringBootServletInitializer {

 /* public static void main(String[] args) {
    System.out.println("#################### Startup parameters ##############");
    for (String s : args) {
      System.out.println("Parameter: " + s);
    }
    System.out.println("######################################################");
    SpringApplication.run(LaunchServerApplication.class, args);
  }*/

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(LaunchServerApplication.class);
  }

}

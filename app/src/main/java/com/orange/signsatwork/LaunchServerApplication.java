package com.orange.signsatwork;;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class LaunchServerApplication extends SpringBootServletInitializer {

//	    @Autowired
//	    Environment env;

	        public static void main(String[] args) {
			        System.out.println("#################### Startup parameters ##############");
				        for (String s : args) {
						            System.out.println("Parameter: " + s);
							            }
					        System.out.println("######################################################");
						        SpringApplication.run(LaunchServerApplication.class, args);
							    }

		    @Override
		        protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
				        return application.sources(LaunchServerApplication.class);
					    }

}

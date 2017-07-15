package com.ninthridge.deeviar.spring;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.ninthridge.deeviar.config.Config;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  protected final Log log = LogFactory.getLog(getClass());
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .authorizeRequests()
      .antMatchers("/config**")
      .hasRole("ADMIN")
      .and().httpBasic()
      ;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    try {
      Map<String, String> adminUsers = new Config().getAdminCredentials();
      for(String username : adminUsers.keySet()) {
        String password = adminUsers.get(username);
        auth.inMemoryAuthentication().withUser(username).password(password).roles("ADMIN");
      }
    } catch (Exception e) {
      log.error(e, e);
    }
  }
}
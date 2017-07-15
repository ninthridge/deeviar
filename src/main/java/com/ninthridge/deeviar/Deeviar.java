package com.ninthridge.deeviar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.manager.JobManager;
import com.ninthridge.deeviar.manager.MediaProcessingManager;
import com.ninthridge.deeviar.manager.StreamManager;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.service.ProfileService;
import com.ninthridge.deeviar.util.NetworkUtil;
import com.ninthridge.deeviar.webserver.WebServer;
import com.ninthridge.deeviar.webserver.impl.UnderTowWebServer;

public class Deeviar {
  public static final int DEFAULT_PORT = 7111;
  private static final String CONFIG_LOCATION = "com.ninthridge.deeviar.spring";

  public enum Mode {normal, webonly}
  public enum LogLevel {FATAL, ERROR, WARN, INFO, DEBUG}
  public enum LogAppender {console, rollingfile}
  
  private void start(String host, Integer port, File configDir, Mode mode, LogLevel loglevel, List<LogAppender> logAppenders) throws Exception {
    
    Properties systemProperties = System.getProperties();
    systemProperties.setProperty("configDir", configDir.getCanonicalPath());
    
    String logAppendersStr = null;
    for(LogAppender logAppender : logAppenders) {
      if(logAppendersStr == null) {
        logAppendersStr = logAppender.name();
      }
      else {
        logAppendersStr += "," + logAppender.name();
      }
    }
    
    System.setProperty("log.appenders", logAppendersStr);
    System.setProperty("root.logger.level", loglevel.name());
    System.setProperty("log.dir", new File(configDir.getAbsolutePath(), "logs").getAbsolutePath());
    WebApplicationContext context = getContext();
    
    WebServer webServer = new UnderTowWebServer(context, port);
    
    webServer.start();
    
    context.getBean(Config.class).setHost(host);
    context.getBean(Config.class).setStartingTunerPort(port+1);
    
    ProfileService profileService = context.getBean(ProfileService.class);
    
    Set<Profile> profiles = profileService.findAll();
    if(profiles == null || profiles.isEmpty()) {
      Profile profile = new Profile();
      profile.setTitle("Main");
      profileService.saveProfile(profile);
    }
    
    if(!mode.equals(Mode.webonly)) {
      new Thread(context.getBean(StreamManager.class)).start();
      new Thread(context.getBean(MediaProcessingManager.class)).start();
      new Thread(context.getBean(JobManager.class)).start();
    }
  }

  private WebApplicationContext getContext() {
    AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
    context.setConfigLocation(CONFIG_LOCATION);
    return context;
  }

  public static void main(String[] args) throws Exception {
    String logLevelsStr = null;
    for(LogLevel logLevel : LogLevel.values()) {
      if(logLevelsStr == null) {
        logLevelsStr = logLevel.name();
      }
      else {
        logLevelsStr += "," + logLevel.name();
      }
    }
    
    String logAppendersStr = null;
    for(LogAppender logAppender : LogAppender.values()) {
      if(logAppendersStr == null) {
        logAppendersStr = logAppender.name();
      }
      else {
        logAppendersStr += "," + logAppender.name();
      }
    }
    
    String modesStr = null;
    for(Mode m : Mode.values()) {
      if(modesStr == null) {
        modesStr = m.name();
      }
      else {
        modesStr += "," + m.name();
      }
    }
    
    Deeviar deeviar = new Deeviar();
    if(args.length > 0 && "--help".equals(args[0])) {
      System.out.println("Usage: ");
      System.out.println("  java -jar deeviar.jar [-host <host>] [-port <port>] [-dir <dir>] [-modes < + " + modesStr + ">] [-loglevel < + " + logLevelsStr + ">] [-logappenders < + " + logAppendersStr + ">]");
    }
    else {
      
      String host = NetworkUtil.getLocalIpAddress();
      int port = DEFAULT_PORT;
      File configDir = new File(System.getProperty("user.home"), ".deeviar");
      LogLevel loglevel = LogLevel.INFO;
      List<LogAppender> logAppenders = new ArrayList<>();
      logAppenders.add(LogAppender.rollingfile);
      Mode mode = Mode.normal;
      
      for(int i=0; i<args.length; i+=2) {
        if(i+1 < args.length) {
          if(args[i].equals("-host")) {
            host = args[i+1];
          }
          else if(args[i].equals("-port")) {
            try {
              port = new Integer(args[i+1]);
            }
            catch(NumberFormatException e) {
              System.out.println("Invalid port number " + args[i+1]);
              return;
            }
          }
          else if(args[i].equals("-dir")) {
            try {
              configDir = new File(args[i+1]);
            }
            catch(Exception e) {
              System.out.println("Invalid directory " + args[i+1]);
              return;
            }
          }
          else if(args[i].equals("-loglevel")) {
            try {
              loglevel = LogLevel.valueOf(args[i+1]);
            }
            catch(Exception e) {
              System.out.println("Invalid log level " + args[i+1] + ".  Valid log levels are " + logLevelsStr);
              return;
            }
          }
          else if(args[i].equals("-logappenders")) {
            try {
              logAppenders.clear();
              for(String s : args[i+1].split(",")) {
                logAppenders.add(LogAppender.valueOf(s));
              }
            }
            catch(Exception e) {
              System.out.println("Invalid log appender " + args[i+1] + ".  Valid log appenders are " + logAppendersStr);
              return;
            }
          }
          else if(args[i].equals("-mode")) {
            try {
              mode = Mode.valueOf(args[i+1]);
            }
            catch(Exception e) {
              System.out.println("Invalid mode " + args[i+1] + ".  Value modes are " + modesStr);
              return;
            }
          }
        }
      }
      
      deeviar.start(host, port, configDir, mode, loglevel, logAppenders);
    }
  }
}

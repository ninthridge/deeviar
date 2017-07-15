package com.ninthridge.deeviar.webserver.impl;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;

import java.util.Arrays;

import javax.servlet.DispatcherType;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.webserver.WebServer;

public class UnderTowWebServer implements WebServer {

  private Undertow server;

  public UnderTowWebServer(WebApplicationContext context, int port) throws Exception {
    Config config = new Config();
    
    DeploymentInfo deploymentInfo = Servlets
        .deployment()
        .setClassLoader(UnderTowWebServer.class.getClassLoader())
        .setContextPath("/")
        .setDeploymentName("deeviar")
        .addListener(new ListenerInfo(ContextLoaderListener.class, new ContextLoaderListenerInstanceFactory(context)))
        .addServlets(
            Servlets.servlet("DispatcherServlet", DispatcherServlet.class, new DispatcherServletInstanceFactory(context)).addMapping("/*")
                .addInitParam("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext")
                .addInitParam("contextConfigLocation", "com.ninthridge.deeviar.spring"))
        .addFilters(Arrays.asList(
            new FilterInfo("hiddenHttpMethodFilter", HiddenHttpMethodFilter.class),
            new FilterInfo("springSecurityFilterChain", DelegatingFilterProxy.class)
        ))
        .addFilterUrlMapping("hiddenHttpMethodFilter", "/*", DispatcherType.REQUEST)
        .addFilterUrlMapping("springSecurityFilterChain", "/*", DispatcherType.REQUEST)
        .addFilterUrlMapping("springSecurityFilterChain", "/*", DispatcherType.ERROR)
        ;

    DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
    deploymentManager.deploy();

    PathHandler pathHandler = new PathHandler();
    pathHandler.addPrefixPath("/bifs", new ResourceHandler(new FileResourceManager(config.getBifsDir(), 0)));
    pathHandler.addPrefixPath("/images", new ResourceHandler(new FileResourceManager(config.getImagesDir(), 0)));
    pathHandler.addPrefixPath("/subtitles", new ResourceHandler(new FileResourceManager(config.getSubtitlesDir(), 0)));
    pathHandler.addPrefixPath("/streams", new ResourceHandler(new FileResourceManager(config.getStreamsDir(), 0)));
    pathHandler.addPrefixPath("/videos", new ResourceHandler(new FileResourceManager(config.getVideosDir(), 0)));
    pathHandler.addPrefixPath("/", deploymentManager.start());

    server = Undertow.builder().addHttpListener(port, "0.0.0.0").setHandler(pathHandler).build();
  }

  @Override
  public void start() throws Exception {
    server.start();
  }

  class ContextLoaderListenerInstanceFactory implements InstanceFactory<ContextLoaderListener> {

    private WebApplicationContext context;

    public ContextLoaderListenerInstanceFactory(WebApplicationContext context) {
      this.context = context;
    }

    @Override
    public InstanceHandle<ContextLoaderListener> createInstance() throws InstantiationException {
      return new InstanceHandle<ContextLoaderListener>() {

        @Override
        public ContextLoaderListener getInstance() {
          return new ContextLoaderListener(context);
        }

        @Override
        public void release() {

        }
      };
    }
  }

  class DispatcherServletInstanceFactory implements InstanceFactory<DispatcherServlet> {

    private WebApplicationContext context;

    public DispatcherServletInstanceFactory(WebApplicationContext context) {
      this.context = context;
    }

    @Override
    public InstanceHandle<DispatcherServlet> createInstance() throws InstantiationException {
      return new InstanceHandle<DispatcherServlet>() {

        @Override
        public DispatcherServlet getInstance() {
          return new DispatcherServlet(context);
        }

        @Override
        public void release() {

        }
      };
    }
  }
}

package com.ninthridge.deeviar.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class SystemResource {

  protected final Log log = LogFactory.getLog(getClass());

  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> get() {
    Map<String, Object> map = new HashMap<>();
    map.put("sysdate", new Date());
    map.put("version", 1.0);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }
}

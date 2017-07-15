package com.ninthridge.deeviar.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.model.Device;
import com.ninthridge.deeviar.service.DeviceService;

@Controller
@RequestMapping("/api/devices")
public class DeviceResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private DeviceService deviceService;
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> getDevices() {
    log.info("getDevices");
    try {
      return new ResponseEntity<>(deviceService.findAllDevices(), HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method=RequestMethod.POST)
  public @ResponseBody ResponseEntity<?> addDevice(@RequestBody Device device) {
    log.info("addDevice " + device.getIpAddress());
    try {
      Device d = deviceService.addDevice(device.getIpAddress()); 
      if(d != null) {
        return new ResponseEntity<>(d, HttpStatus.OK);
      }
      else {
        String error = "Unable to discover an HDHomeRun at " + device.getIpAddress();
        log.error(error);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
      }
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.PATCH)
  public @ResponseBody ResponseEntity<?> saveDevice(@RequestBody Device device) {
    log.info("saveDevice " + device.getId() + " " + device.getLineupId());
    try {
      Device d = deviceService.findDeviceById(device.getId());
      if(d != null) {
        d.setLineupId(device.getLineupId());
        deviceService.saveDevice(d);
      }
      
      return new ResponseEntity<>(d, HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method=RequestMethod.POST, path="/{deviceId}/scan")
  public @ResponseBody ResponseEntity<?> scanDevice(@PathVariable("deviceId") String deviceId) {
    log.info("scanDevice " + deviceId);
    try {
      deviceService.queueForScan(deviceId);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

package com.ninthridge.deeviar.device.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.device.DeviceController;
import com.ninthridge.deeviar.model.Device;
import com.ninthridge.deeviar.model.DeviceStation;
import com.ninthridge.deeviar.model.Tuner;
import com.ninthridge.deeviar.model.Device.DeviceStatus;
import com.ninthridge.deeviar.util.CmdUtil;
import com.ninthridge.deeviar.util.TitleUtil;

//TODO: Replace this with an internal controller
@Service("deviceController")
public class HdhomerunCliController implements DeviceController {

  protected final Log log = LogFactory.getLog(getClass());

  private static String HDHOMERUN_DISCOVER_ALL_COMMAND = "hdhomerun_config discover";
  private static String HDHOMERUN_DISCOVER_COMMAND = "hdhomerun_config discover ${ipAddress}";
  private static String HDHOMERUN_HELP_COMMAND = "hdhomerun_config ${device} get help";
  private static String HDHOMERUN_HWMODEL_COMMAND = "hdhomerun_config ${device} get /sys/hwmodel";
  private static String HDHOMERUN_TUNER_GET_PROGRAM_COMMAND = "hdhomerun_config ${device} get /tuner${tuner}/program";
  private static String HDHOMERUN_TUNER_GET_STATUS_COMMAND = "hdhomerun_config ${device} get /tuner${tuner}/status";
  private static String HDHOMERUN_TUNER_GET_VSTATUS_COMMAND = "hdhomerun_config ${device} get /tuner${tuner}/vstatus";
  private static String HDHOMERUN_TUNE_FREQUENCY_COMMAND = "hdhomerun_config ${device} set /tuner${tuner}/channel ${frequency}";
  private static String HDHOMERUN_TUNE_PROGRAM_COMMAND = "hdhomerun_config ${device} set /tuner${tuner}/program ${program}";
  private static String HDHOMERUN_TUNE_VCHANNEL_COMMAND = "hdhomerun_config ${device} set /tuner${tuner}/vchannel ${channel}";
  private static String HDHOMERUN_UNTUNE_COMMAND = "hdhomerun_config ${device} set /tuner${tuner}/channel none";
  private static String HDHOMERUN_TARGET_COMMAND = "hdhomerun_config ${device} set /tuner${tuner}/target ${url}";
  private static String HDHOMERUN_SCAN_COMMAND = "hdhomerun_config ${device} scan ${tuner}";

  public Device discoverDevice(String ipAddress) {
    log.info("Starting device discovery on " + ipAddress);
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("ipAddress", ipAddress);
      
      Process p = CmdUtil.execute(HDHOMERUN_DISCOVER_COMMAND, params, null, null);
      p.waitFor();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line = null;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if(line.startsWith("hdhomerun device ")) {
            Device device = toDevice(line);
            if(device != null) {
              device.setDiscovered(false);
              return device;
            }
          }
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected tuner discovery exception", e);
    }
    log.info("Unable to find a device on " + ipAddress);
    return null;
  }

  public Set<Device> discoverDevices() {
    log.info("Starting device discovery");
    Set<Device> devices = new HashSet<Device>();
    try {
      Process p = CmdUtil.execute(HDHOMERUN_DISCOVER_ALL_COMMAND, null, null, null);
      p.waitFor();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line = null;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if(line.startsWith("hdhomerun device ")) {
            Device device = toDevice(line);
            if(device != null) {
              device.setDiscovered(true);
              devices.add(device);
            }
          }
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected tuner discovery exception", e);
    }
    log.info("Device discovery complete.  Discovered " + devices.size() + " devices");
    return devices;
  }

  protected Device toDevice(String line) {
    String[] s = line.split(" ");
    Device device = new Device();
    device.setId(s[2]);
    device.setIpAddress(s[5]);
    device.setHwmodel(getHwmodel(device.getIpAddress()));
    device.setVirtualChannelSupport(getVirtualChannelSupport(device.getIpAddress()));
    device.setStatus(DeviceStatus.New);
    
    Set<Tuner> deviceTuners = new HashSet<Tuner>();
    int numberOfTuners = getNumberOfTuners(device.getIpAddress());
    for(int i=0; i<numberOfTuners; i++) {
      Tuner tuner = new Tuner();
      tuner.setId(i);
      tuner.setDevice(device);
      deviceTuners.add(tuner);
    }
    device.setTuners(deviceTuners);
    if(!deviceTuners.isEmpty()) {
      log.info("Discovered device id:" + device.getId() + " ipAddress:" + device.getIpAddress() + " hwmodel:" + device.getHwmodel() + " tuners:" + device.getTuners().size() + " virtualChannelSupport:" + device.getVirtualChannelSupport());
      return device;
    }
    
    return null;
  }
  
  protected boolean getVirtualChannelSupport(String device) {
    boolean virtualChannelsSupported = false;
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("device", device);
      params.put("tuner", "0");
      
      Process process = CmdUtil.execute(HDHOMERUN_HELP_COMMAND, params, null, null);
      process.waitFor();
      
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line = null;
        while((line = reader.readLine()) != null) {
          if(line.contains("vchannel")) {
            virtualChannelsSupported = true;
          }
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception determining virtual channel support for " + device, e);
    }
    
    return virtualChannelsSupported;
  }

  protected String getHwmodel(String device) {
    String hwmodel = null;
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("device", device);
      
      Process process = CmdUtil.execute(HDHOMERUN_HWMODEL_COMMAND, params, null, null);
      process.waitFor();
      
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line = reader.readLine();
        if(line != null) {
          hwmodel = line.trim();
          log.info(hwmodel);
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception getting the device hardware model for " + device, e);
    }
    
    return hwmodel;
  }
  
  protected int getNumberOfTuners(String device) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", device);
    int numberOfTuners = 0;
    while(true) {
      try {
        params.put("tuner", new Integer(numberOfTuners).toString());
        
        Process process = CmdUtil.execute(HDHOMERUN_TUNER_GET_STATUS_COMMAND, params, null, null);
        process.waitFor();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
          String line = reader.readLine();
          if(line != null && line.contains("ch=")) {
            numberOfTuners++;
          }
          else {
            break;
          }
        }
      } catch (IOException | InterruptedException e) {
        log.error("Unexpected exception getting the number of tuners for " + device, e);
      }
    }
    return numberOfTuners;
  }
  
  public void tune(Tuner tuner, DeviceStation station) {
    try {
      final Map<String, String> params = createTuneParams(tuner, station);
      
      if(tuner.getDevice().getVirtualChannelSupport()) {
        Process p = CmdUtil.execute(HDHOMERUN_TUNE_VCHANNEL_COMMAND, params, null, null);
        p.waitFor();
      }
      else {
        Process p = CmdUtil.execute(HDHOMERUN_TUNE_FREQUENCY_COMMAND, params, null, null);
        p.waitFor();
        
        Process p2 = CmdUtil.execute(HDHOMERUN_TUNE_PROGRAM_COMMAND, params, null, null);
        p2.waitFor();
      }
      
      Process p = CmdUtil.execute(HDHOMERUN_TARGET_COMMAND, params, null, null);
      p.waitFor();
      
      //Not sure why, but the target command sometimes fails on the first attempt.  
      //Since it doesn't hurt to run it more than once, run it a second time a couple of seconds later
      Thread.sleep(2000); 
      Process p2 = CmdUtil.execute(HDHOMERUN_TARGET_COMMAND, params, null, null);
      p2.waitFor();
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception tuning " + tuner.getDevice().getIpAddress() + " to " + station.getCallSign(), e);
      //TODO: throw DeviceException
    }
  }

  private Map<String, String> createTuneParams(Tuner tuner, DeviceStation station) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", tuner.getDevice().getDiscovered() ? tuner.getDevice().getId() : tuner.getDevice().getIpAddress());
    params.put("tuner", tuner.getId().toString());
    params.put("channel", station.getChannel());
    params.put("frequency", station.getFrequency().toString());
    params.put("program", station.getProgram().toString());
    params.put("url", tuner.getTargetUrl());
    return params;
  }
  
  public Long getBitRate(Tuner tuner) {
    Long bitRate = null;
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("device", tuner.getDevice().getDiscovered() ? tuner.getDevice().getId() : tuner.getDevice().getIpAddress());
      params.put("tuner", tuner.getId().toString());
      params.put("url", tuner.getTargetUrl());

      Process p = CmdUtil.execute(HDHOMERUN_TUNER_GET_STATUS_COMMAND, params, null, null);
      p.waitFor();
      
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line = reader.readLine();
        if(line != null) {
          for(String str : line.split(" ")) {
            if(str.startsWith("bps=")) {
             bitRate = new Long(str.split("=")[1]);
            }
          }
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception getting bit rate " + tuner.getDevice().getIpAddress() + " " + tuner.getId(), e);
    }
    
    return bitRate;
  }
  
  public void unTune(Tuner tuner) {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("device", tuner.getDevice().getDiscovered() ? tuner.getDevice().getId() : tuner.getDevice().getIpAddress());
      params.put("tuner", new Integer(tuner.getId()).toString());

      Process p = CmdUtil.execute(HDHOMERUN_UNTUNE_COMMAND, params, null, null);
      p.waitFor();
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception untuning " + tuner.getDevice().getId(), e);
    }
  }

  public Set<DeviceStation> scanStations(Tuner tuner) {
    return scanByInternalDeviceScanner(tuner);
  }
  
  protected Set<DeviceStation> scanByInternalDeviceScanner(Tuner tuner) {
    log.info("Starting station scan on " + tuner.getDevice().getId());
    Set<DeviceStation> stations = new HashSet<>();
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("device", tuner.getDevice().getDiscovered() ? tuner.getDevice().getId() : tuner.getDevice().getIpAddress());
      params.put("tuner", new Integer(tuner.getId()).toString());

      Process p = CmdUtil.execute(HDHOMERUN_SCAN_COMMAND, params, null, null);
      p.waitFor();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        Long frequency = null;
        String modulation = null;
        String line = null;
        while ((line = reader.readLine()) != null) {
          if(line.startsWith("SCANNING")) {
            frequency = new Long(line.split(" ")[1]);
          }
          
          if(line.startsWith("LOCK")) {
            modulation = line.split(" ")[1];
          }
          
          if(line.startsWith("PROGRAM ")) {
            String s[] = line.split(" ");
            if(s.length >= 4) {
              Integer program = new Integer(s[1].split(":")[0]);
              String channel = s[2];
              String callSign = null;
              for(int i=3; i<s.length; i++) {
                if(!s[i].startsWith("(") || !s[i].endsWith(")")) {
                  if(callSign != null) {
                    callSign += " " + s[i];
                  }
                  else {
                    callSign = s[i];
                  }
                }
              }
              if(channel != null && callSign != null) {
                while(channel.charAt(0) == '0') {
                  channel = channel.substring(1);
                }
                
                channel = channel.replace('-', '.');
                
                if(channel.length() > 0) {
                  DeviceStation station = new DeviceStation();
                  station.setDeviceId(tuner.getDevice().getId());
                  station.setChannel(channel);
                  station.setCallSign(TitleUtil.cleanse(callSign).toUpperCase());
                  station.setFrequency(frequency);
                  station.setModulation(modulation);
                  station.setProgram(program);
                  stations.add(station);
                  log.info("Found " + station + " on " + tuner.getDevice());
                }
              }
            }
          }
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception scanning " + tuner.getDevice().getId(), e);
    }
    log.info("Station scan complete.  Found " + stations.size() + " stations on " + tuner.getDevice().getId());
    return stations;
  }
  
  protected Set<DeviceStation> scanByVirtualStations(Tuner tuner) {
    log.info("Starting station scan on " + tuner.getDevice().getId());
    Set<DeviceStation> stations = new HashSet<>();
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", tuner.getDevice().getDiscovered() ? tuner.getDevice().getId() : tuner.getDevice().getIpAddress());
    params.put("tuner", new Integer(tuner.getId()).toString());

    for(int i=1; i<1000; i++) {
      try {
        log.info("Scanning channel " + i);
        params.put("channel", new Integer(i).toString());
        Process p = CmdUtil.execute(HDHOMERUN_TUNE_VCHANNEL_COMMAND, params, null, null);
        p.waitFor();
        
        Thread.sleep(1000);
        
        DeviceStation station = new DeviceStation();
        station.setDeviceId(tuner.getDevice().getId());
        Process p2 = CmdUtil.execute(HDHOMERUN_TUNER_GET_VSTATUS_COMMAND, params, null, null);
        p2.waitFor();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p2.getInputStream()))) {
          Map<String, String> map = splitKeyValuePairsLine(reader.readLine());
          String channel = map.get("vch");
          String callSign = map.get("name");
          
          if(channel != null && callSign != null) {
            while(channel.charAt(0) == '0') {
              channel = channel.substring(1);
            }
            
            channel = channel.replace('-', '.');
            
            if(channel.length() > 0) {
              station.setChannel(channel);
              station.setCallSign(TitleUtil.cleanse(callSign).toUpperCase());
            }
          }
        }
        
        if(station.getChannel() != null) {
          Process p3 = CmdUtil.execute(HDHOMERUN_TUNER_GET_STATUS_COMMAND, params, null, null);
          p3.waitFor();
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(p3.getInputStream()))) {
            Map<String, String> map = splitKeyValuePairsLine(reader.readLine());
            String ch = map.get("ch");
            if(ch != null) {
              String[] s = ch.split(":");
              station.setModulation(s[0]);
              station.setFrequency(new Long(s[1]));
            }
          }
          
          Process p4 = CmdUtil.execute(HDHOMERUN_TUNER_GET_PROGRAM_COMMAND, params, null, null);
          p4.waitFor();
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(p4.getInputStream()))) {
            String line = reader.readLine();
            if(line != null) {
              station.setProgram(new Integer(line));
            }
          }
          
          log.info("Found station " + station.getChannel() + " " + station.getCallSign() + " " + station.getModulation() + " " + station.getFrequency() + " " + station.getProgram());
          stations.add(station);
        }
      } catch (IOException | InterruptedException e) {
        log.error("Unexpected exception untuning " + tuner.getDevice().getIpAddress(), e);
      }
    }
    log.info("Station scan complete.  Found " + stations.size() + " stations on " + tuner.getDevice().getIpAddress());
    return stations;
  }
  
  private Map<String, String> splitKeyValuePairsLine(String line) {
    Map<String, String> map = new HashMap<>();
    if(line != null) {
      List<String> pairs = new LinkedList<>(Arrays.asList(line.split(" ")));
      
      int i=1;
      while(i<pairs.size()) {
        if(!pairs.get(i).contains("=")) {
          pairs.set(i-1, pairs.get(i-1) + " " + pairs.get(i));
          pairs.remove(i);
        }
        else if(pairs.get(i).trim().equals("")) {
          pairs.remove(i);
        }
        else {
          i++;
        }
      }
      
      for(String pair : pairs) {
        String[] keyValue = pair.split("=");
        if(keyValue.length == 2) {
          map.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return map;
  }
}

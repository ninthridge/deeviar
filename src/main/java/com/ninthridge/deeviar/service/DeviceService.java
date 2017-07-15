package com.ninthridge.deeviar.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.device.DeviceController;
import com.ninthridge.deeviar.model.Device;
import com.ninthridge.deeviar.model.Device.DeviceStatus;
import com.ninthridge.deeviar.model.DeviceStation;
import com.ninthridge.deeviar.model.Tuner;
import com.ninthridge.deeviar.repository.DeviceRepository;
import com.ninthridge.deeviar.repository.TunerRepository;

@Service("deviceService")
public class DeviceService {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private TunerRepository tunerRepository;

  @Autowired
  private DeviceController deviceController;

  @Autowired
  private DeviceRepository deviceRepository;

  @Autowired
  private LineupService lineupService;
  
  @Autowired
  private Config config;
  
  public boolean refreshTunerCache() {
    log.info("Starting tuner refresh");
  
    boolean changed = false;
    Set<Device> discoveredDevices = deviceController.discoverDevices();
    Set<Device> persistedDevices = deviceRepository.getAll();
    if(persistedDevices == null) {
      persistedDevices = new HashSet<>();
    }
    for(Device persistedDevice : persistedDevices) {
      if(!discoveredDevices.contains(persistedDevice)) {
        Device device = deviceController.discoverDevice(persistedDevice.getIpAddress());
        if(device != null) {
          discoveredDevices.add(device);
        }
      }
    }
    
    int tunerCount = 0;
    for (Device discoveredDevice : discoveredDevices) {
      Device persistedDevice = deviceRepository.get(discoveredDevice.getId());
      if(persistedDevice != null) {
        boolean persist = false;
        if (!persistedDevice.getIpAddress().equals(discoveredDevice.getIpAddress())) {
          persistedDevice.setIpAddress(discoveredDevice.getIpAddress());
          persist = true;
        }

        if (!persistedDevice.getDiscovered() != discoveredDevice.getDiscovered()) {
          persistedDevice.setDiscovered(discoveredDevice.getDiscovered());
          persist = true;
        }
        
        if(persistedDevice.getScanTimestamp() == null) {
          if(!DeviceStatus.WaitingToScan.equals(persistedDevice.getStatus())) {
            persistedDevice.setStatus(DeviceStatus.WaitingToScan);
            persist = true;
          }
        }
        else{
          if(DeviceStatus.Offline.equals(persistedDevice.getStatus())) {
            persistedDevice.setStatus(DeviceStatus.Available);
            persist = true;
          }
        }
        
        if (persist) {
          deviceRepository.save(persistedDevice);
          changed = true;
        }
      }
      else {
        persistedDevice = discoveredDevice;
        persistedDevice.setStatus(DeviceStatus.WaitingToScan);
        deviceRepository.save(persistedDevice);
        changed = true;
      }
      
      if(findCachedDevice(persistedDevice) == null) {
        if(persistedDevice.getStatus().equals(DeviceStatus.Scanning)) {
          persistedDevice.setStatus(DeviceStatus.WaitingToScan);
          deviceRepository.save(persistedDevice);
        }
        
        Integer nextPort = nextPort();
        Set<Tuner> tuners = new HashSet<>();
        for(Tuner discoveredTuner : discoveredDevice.getTuners()) {
          Tuner tuner = new Tuner();
          tuner.setDevice(persistedDevice);
          tuner.setId(discoveredTuner.getId());
          tuner.setPort(nextPort);
          tuner.setTargetUrl("udp://" + config.getHost() + ":" + nextPort);
          tuners.add(tuner);
          
          log.info("Tuner " + tuner.getId() + " " + tuner.getDevice().getId() + " " + tuner.getTargetUrl());
          tunerRepository.save(tuner);
          
          nextPort++;
          
        }
        persistedDevice.setTuners(tuners);

        changed = true;
      }
      tunerCount += persistedDevice.getTuners().size();
    }

    persistedDevices.removeAll(discoveredDevices);
    
    for (Device device : persistedDevices) {
      Device cachedDevice = findCachedDevice(device);
      if (cachedDevice != null) {
        device = cachedDevice;
      }
      if(!DeviceStatus.Offline.equals(device.getStatus())) {
        device.setStatus(DeviceStatus.Offline);
        deviceRepository.save(device);
        changed = true;
      }
    }
    
    log.info("Completed tuner refresh.  Found " + discoveredDevices.size() + " devices, with " + tunerCount + " tuners");
    return changed;
  }

  public boolean performScanOnWaitingDevices() {
    boolean scanned = false;
    for(Device device : deviceRepository.getAll()) {
      if(DeviceStatus.WaitingToScan.equals(device.getStatus())) {
        scanStations(device);
        scanned = true;
      }
    }
    
    return scanned;
  }
  
  public void queueForScan(String deviceId) {
    Device device = deviceRepository.get(deviceId);
    device.setStatus(DeviceStatus.WaitingToScan);
    deviceRepository.save(device);
  }
  
  protected void scanStations(Device device) {
    log.info("Performing station scan on " + device.getId());
    device.setStatus(DeviceStatus.Scanning);
    deviceRepository.save(device);
    
    Set<DeviceStation> stations = deviceController.scanStations(device.getTuners().iterator().next());
    device.setStations(stations);
    device.setScanTimestamp(new Date());

    device.setStatus(DeviceStatus.Available);
    deviceRepository.save(device);
  }

  public List<Tuner> findAllAvailableTuners() {
    List<Tuner> activeTuners = new ArrayList<>();
    Set<Tuner> tuners = tunerRepository.getAll();
    if(tuners != null) {
      for (Tuner tuner : tuners) {
        if (tuner.getDevice().getStatus().equals(DeviceStatus.Available)) {
          activeTuners.add(tuner);
        }
      }
    }
    return activeTuners;
  }

  public List<Device> findAllAvailableDevices() {
    Set<Device> activeDevices = new HashSet<>();
    for (Tuner tuner : findAllAvailableTuners()) {
      activeDevices.add(tuner.getDevice());
    }
    return new ArrayList<>(activeDevices);
  }

  public List<Device> findAllDevices() {
    Set<Device> devices = deviceRepository.getAll();
    if(devices != null) {
      return new ArrayList<>(devices);
    }
    else {
      return new ArrayList<>();
    }
  }

  public Device findDeviceById(String id) {
    for (Device device : findAllDevices()) {
      if (device.getId().equals(id)) {
        return device;
      }
    }
    return null;
  }

  public Device addDevice(String ipAddress) {
    Device device = deviceController.discoverDevice(ipAddress);
    if(device != null) {
      Device d = deviceRepository.get(device.getId());
      if(d != null) {
        if(!d.getIpAddress().equals(ipAddress)) {
          d.setIpAddress(ipAddress);
          deviceRepository.save(d);
        }
      }
      else {
        device.setStatus(DeviceStatus.WaitingToScan);
        deviceRepository.save(device);
      }
    }
    
    return device;
  }
  
  public void saveDevice(Device device) {
    deviceRepository.save(device);
    lineupService.refreshProfileLineups();
  }

  public void tune(Tuner tuner, DeviceStation station) {
    deviceController.tune(tuner, station);
    tuner.setLastTuned(new Date());
  }

  public Long getBitRate(Tuner tuner) {
    return deviceController.getBitRate(tuner);
  }

  public void unTune(Tuner tuner) {
    deviceController.unTune(tuner);
    tuner.setLastUntuned(new Date());
  }

  protected Device findCachedDevice(Device device) {
    Set<Tuner> tuners = tunerRepository.getAll();
    if(tuners != null) {
      for (Tuner tuner : tuners) {
        if (tuner.getDevice().equals(device)) {
          return tuner.getDevice();
        }
      }
    }
    return null;
  }

  protected Integer nextPort() {
    Integer maxPort = null;
    Set<Tuner> tuners = tunerRepository.getAll();
    if(tuners != null) {
      for (Tuner tuner : tuners) {
        if (maxPort == null || tuner.getPort() > maxPort) {
          maxPort = tuner.getPort();
        }
      }
    }
    
    if (maxPort == null) {
      return config.getStartingTunerPort();
    } else {
      return maxPort + 1;
    }
  }
}
package com.ninthridge.deeviar.device;

import java.util.Set;

import com.ninthridge.deeviar.model.Device;
import com.ninthridge.deeviar.model.DeviceStation;
import com.ninthridge.deeviar.model.Tuner;

public interface DeviceController {

  Device discoverDevice(String ipAddress);
  Set<Device> discoverDevices();
  void tune(Tuner tuner, DeviceStation station);
  Long getBitRate(Tuner tuner);
  void unTune(Tuner tuner);
  Set<DeviceStation> scanStations(Tuner tuner);
}

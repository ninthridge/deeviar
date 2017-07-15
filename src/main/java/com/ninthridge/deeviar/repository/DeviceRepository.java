package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Device;

@Repository("deviceRepository")
public class DeviceRepository extends CachedMultiJsonRepository<Device> {

  public DeviceRepository() {
    super(new TypeReference<Device>(){});
  }
  
  @Override
  protected String getId(Device device) {
    return device.getId();
  }

  @Override
  protected String getDirName() {
    return "devices";
  }

}

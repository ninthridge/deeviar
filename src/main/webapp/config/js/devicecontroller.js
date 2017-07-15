function displayDevices() {
  getDevices(function(devices) {
    var model = {};
    model.devices = devices;
    $('#maincontainer').html(Handlebars.templates.devices(model)); 
    setTimeout(function() {
      if($(location).attr('href').endsWith("/config/#devices")) {
        displayDevices();
      }
    }, 10000);
  });
}

function submitScanDevice(deviceId) {
  scanDevice(deviceId, function () {
    displayDevices()
  });
}

function displayAddDevice() {
  var model = {};
  $('#maincontainer').html(Handlebars.templates.adddevice(model));
}

function submitAddDevice() {
  var device = {};
  device.ipAddress = $("#ipAddress").val().trim();
  if(device.ipaddress == "") {
    toastr.error("Please enter an ip address");
  }
  else {
  	createDevice(device, function() {
      $(location).attr('href','/config/#devices');
    });
  }
}

function displayGrabberConfig(deviceId) {
  window.deviceId = deviceId;
  getGrabberConfig(function(grabberConfig) {
    var model = grabberConfig;
    $('#maincontainer').html(Handlebars.templates.grabberconfig(model)); 
  });
}

function submitGrabberConfig() {
  var data = {};
  $("#grabberconfigform").serializeArray().map(function(x){data[x.name] = x.value;}); 
  
  var countryCode = data["countryCode"];
  var postalCode = data["postalCode"];
  
  saveGrabberConfig(data, function() {
    getGrabberLineups(countryCode, postalCode, function(lineups) {
      if(lineups.length > 0) {
        var model = {};
        model.id = window.deviceId;
        model.lineups = lineups;
        $('#maincontainer').html(Handlebars.templates.devicelineups(model));
      }
      else {
        toastr.error("Unable to retrieve any lineups");
      }
    });
  });
}

function submitDeviceLineup() {
  var data = {};
  $("#devicelineupform").serializeArray().map(function(x){data[x.name] = x.value;}); 
  
  if(data.lineupId != null) {
    saveDevice(data, function() {
      toastr.success('Device configuration has been saved');
      $(location).attr('href','/config/#devices');
    });
  }
  else {
    toastr.error('Please select a lineup');
  }
}

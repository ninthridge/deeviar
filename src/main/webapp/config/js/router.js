$(document).ready(function() {
  routie({
    '': function() {
      displayDashboard();    
    },
    'devices': function() {
      $('#navbar ul li').removeClass('active');
      $('#devicesNav').addClass('active');
      displayDevices();
    },
    'devices/:id/lineup': function(id) {
      displayGrabberConfig(id);
    },
    'devices/:id/scan': function(id) {
      submitScanDevice(id);
    },
    'devices/!add': function() {
      displayAddDevice();
    },
    'profiles': function() {
      $('#navbar ul li').removeClass('active');
      $('#profilesNav').addClass('active');
      displayProfiles();
    },
    'profiles/!new': function() {
      displayCreateProfile();
    },
    'profiles/:title/!delete': function(title) {
      submitDeleteProfile(title);
    },
    'profiles/:title/image': function(title) {
      displayEditProfileImage(title);
    },
    'profiles/:title/pin': function(title) {
      displayEditProfilePin(title);
    },
    'profiles/:title/permissions': function(title) {
      displayEditProfilePermissions(title);
    },
    'profiles/:title/libraryimports': function(title) {
      displayLibraryImports(title);
    },
    'profiles/:title/libraryimports/:category': function(title, category) {
      displayLibraryImportCategory(title, category);
    },
    'profiles/:title/libraryimports/:category/!delete': function(title, category) {
      submitDeleteLibraryImportCategory(title, category);
    },
    'channels': function() {
      $('#navbar ul li').removeClass('active');
      $('#channelsNav').addClass('active');
      displayChannels();    
    }
  });
  
  $("#maincontainer").on('submit', '#profilepinform', function(event) {
    event.preventDefault();
    submitProfilePin();
  });
  $("#maincontainer").on('submit', '#profilepermissionsform', function(event) {
    event.preventDefault();
    submitProfilePermissions();
  });
  $("#maincontainer").on('submit', '#libraryimportcategoryform', function(event) {
    event.preventDefault();
    submitLibraryImportCategory();
  });
  $("#maincontainer").on('submit', '#profiletitleform', function(event) {
    event.preventDefault();
    submitCreateProfile();
  });
  $("#maincontainer").on('submit', '#profileimageform', function(event) {
    event.preventDefault();
    submitProfileImage();  
  });
  $("#maincontainer").on('submit', '#grabberconfigform', function(event) {
    event.preventDefault();
    submitGrabberConfig();
  });
  $("#maincontainer").on('submit', '#devicelineupform', function(event) {
    event.preventDefault();
    submitDeviceLineup();
  });
  $("#maincontainer").on('submit', '#adddeviceform', function(event) {
	event.preventDefault();
	submitAddDevice();
  });
  $("#maincontainer").on('submit', '#channelsform', function(event) {
    event.preventDefault();
    submitChannels();
  });
});
function getHealth(callback) {
  $.ajax({
    url: '/api/health',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(health, status, jqXHR) {
      callback(health);
    }
  });
}

function getChannels(callback) {
  $.ajax({
    url: '/api/lineup/channels',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(channels, status, jqXHR) {
      callback(channels);
    }
  });
}

function saveChannels(channels, callback) {
  $.ajax({
    url: '/api/lineup/channels',
    type: 'PUT',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(channels),
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success('Your channels have been successfully updated');
      callback();
    }
  });
}

function getProfiles(callback) {
  $.ajax({
    url: '/api/profiles?all=true',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(profiles, status, jqXHR) {
      callback(profiles);
    }
  });
}

function saveProfile(profile, callback) {
  $.ajax({
    url: '/api/profiles/' + profile.title,
    type: 'PATCH',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(profile),
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success(profile.title + ' has been saved');
      callback();
    }
  });
}

function deleteProfile(profileTitle, callback) {
  $.ajax({
    url: '/api/profiles/' + profileTitle,
    type: 'DELETE',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success(profileTitle + ' has been deleted');
      callback();
    }
  });
}

function getProfile(profileTitle, callback) {
  $.ajax({
    url: '/api/profiles/' + profileTitle,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(profile, status, jqXHR) {
      callback(profile);
    }
  });
}

function saveProfileImage(profileTitle, file, callback) {
  var formData = new FormData();
  formData.append('file', file);
  
  $.ajax({
    type:'POST',
    url: "/api/profiles/" + profileTitle + "/image",
    data: formData,
    cache: false,
    contentType: false,
    processData: false,
    success: function(data) {
      callback(); 
    }
  });
}

function getLibraryCategories(profileTitle, callback) {
  getProfile(profileTitle, function(profile) {
    $.ajax({
      headers: {
        'token': profile.token
      },
      url: '/api/library/categories',
      type: 'GET',
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      success: function(categories, status, jqXHR) {
        callback(categories);
      }
    });
  });
}

function createDevice(device, callback) {
  $.ajax({
    url: '/api/devices',
    type: 'POST',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(device),
    dataType: "json",
    success: function(data, status, jqXHR) {
      callback();
    }
  });
}

function scanDevice(deviceId, callback) {
  $.ajax({
    url: '/api/devices/' + deviceId + "/scan",
    type: 'POST',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(data, status, jqXHR) {
      callback();
    }
  });
}

function getDevices(callback) {
  $.ajax({
    url: '/api/devices',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(devices, status, jqXHR) {
      callback(devices);
    }
  });
}

function getGrabberConfig(callback) {
  $.ajax({
    url: '/api/grabber',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(grabberConfig, status, jqXHR) {
      callback(grabberConfig);
    }
  });
}

function saveGrabberConfig(grabberConfig, callback) {
  $.ajax({
    url: '/api/grabber',
    type: 'PUT',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(grabberConfig),
    dataType: "json",
    success: function(data, status, jqXHR) {
      callback();
    }
  });
}

function getContentConfig(callback) {
  $.ajax({
    url: '/api/content',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(contentConfig, status, jqXHR) {
      callback(contentConfig);
    }
  });
}

function saveContentConfig(configConfig, callback) {
  $.ajax({
    url: '/api/content',
    type: 'PUT',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(configConfig),
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success('Your content configuration has been saved');
      callback();
    }
  });
}

function getGrabberLineups(countryCode, postalCode, callback) {
  $.ajax({
    url: '/api/grabber/lineups/' + countryCode + '/' + postalCode,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(lineups, status, jqXHR) {
      callback(lineups);
    }
  });
}

function saveDevice(device, callback) {
  $.ajax({
    url: '/api/devices',
    type: 'PATCH',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(device),
    dataType: "json",
    success: function(data, status, jqXHR) {
      callback();
    } 
  });
}
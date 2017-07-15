function getProfiles(callback) {
  $.ajax({
    url: '/api/profiles',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(profiles, status, jqXHR) {
      callback(profiles);
    }
  });
}

function getToken(profileTitle, pin, callback) {
  var url = "/api/token?profileTitle=" + profileTitle
  if(pin != null && pin.length > 0) {
    url = url + "&pin=" + forge_sha256(pin)
  }
  
  $.ajax({
    url: url,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(timers, status, jqXHR) {
      callback(timers);
    }
  });
}

function getTimers(callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/timers',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(timers, status, jqXHR) {
      callback(timers);
    }
  });
}

function deleteTimer(timerId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/timers/' + timerId,
    type: 'DELETE',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success('Your timer has been successfully deleted');
      callback();
    }
  });
}

function getCategories(callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/library/categories',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(categories, status, jqXHR) {
      callback(categories);
    }
  });
}

function getCategory(category, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/library?category=' + category,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(library, status, jqXHR) {
      callback(library);
    }
  });
}

function getLibrary(callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/library',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(library, status, jqXHR) {
      callback(library);
    }
  });
}

function getSeries(seriesId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/library/series/' + seriesId,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(series, status, jqXHR) {
      callback(series);
    }
  });
}

function getVideo(videoId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/library/videos/' + videoId,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(video, status, jqXHR) {
      callback(video);
    }
  });
}

function deleteVideo(videoId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
     'token': sessionStorage.getItem("token")
    },
    url: '/api/library/videos/' + videoId,
    type: 'DELETE',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success('Your video has been successfully deleted');
      callback();
    }
  });
}

function deleteSubtitleTrack(videoId, subtitleTrackId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
     'token': sessionStorage.getItem("token")
    },
    url: '/api/library/videos/' + videoId + '/subtitles/' + subtitleTrackId,
    type: 'DELETE',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(data, status, jqXHR) {
      toastr.success('Your subtitle track has been successfully deleted');
      callback();
    }
  });
}

function getAirings(hours, offset, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/lineup/airings?hours=' + hours + '&offset=' + offset,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(airings, status, jqXHR) {
      callback(airings);
    }
  });
}

function getStations(callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/lineup/stations',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(stations, status, jqXHR) {
      callback(stations);
    }
  });
}

function getStation(stationId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/lineup/stations/' + stationId,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(station, status, jqXHR) {
      callback(station);
    }
  });
}

function startOrRefreshStationStream(stationId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/streams/stations/' + stationId,
    type: 'PUT',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(stream, status, jqXHR) {
      callback(stream);
    }
  });
}

function getStationStream(stationId, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: '/api/streams/stations/' + stationId,
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(stream, status, jqXHR) {
      callback(stream);
    }
  });
}

function saveSubtitleTrack(videoId, subtitleTrack, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    url: "/api/library/videos/" + videoId + "/subtitles/",
    type: 'PUT',
    contentType: "application/json; charset=utf-8",
    data: JSON.stringify(subtitleTrack),
    dataType: "json",
    success: function(subtitleTrack, status, jqXHR) {
      callback();
    }
  });
}

function saveSubtitleTrackFile(videoId, language, description, file, callback) {
  if(sessionStorage.getItem("token") == null) {
    $(location).attr('href','/#');
    return;
  }
  
  var formData = new FormData();
  formData.append('language', language);
  formData.append('description', description);
  formData.append('file', file);
  
  $.ajax({
    headers: {
      'token': sessionStorage.getItem("token")
    },
    type:'POST',
    url: "/api/library/videos/" + videoId + "/subtitles/",
    data: formData,
    cache: false,
    contentType: false,
    processData: false,
    success: function(data) {
      callback(); 
    }
  });
}
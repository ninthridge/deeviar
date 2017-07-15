$(document).ready(function() {
  routie({
    '': function() {
      hideNav();
      displayProfileSelector();    
    },
    'profile/:profileTitle': function(profileTitle) {
      hideNav();
      submitSelectProfile(profileTitle);    
    },
    'token/:token': function(token) {
      hideNav();
      setToken(token);    
    },
    'tv': function() {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#tvNav').addClass('active');
      displayTv();    
    },
    'tv/guide': function() {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#tvNav').addClass('active');
      displayGuide();    
    },
    'tv/stations': function() {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#tvNav').addClass('active');
      displayStations();    
    },
    'tv/stations/:id': function(id) {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#tvNav').addClass('active');
      displayStation(id);    
    },
    'library': function() {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#libraryNav').addClass('active');
      displayLibrary();    
    },
    'library/videos/:id': function(id) {
      showNav();
      displayVideo(id);
    },
    'library/videos/:id/subtitles': function(id) {
      showNav();
      displaySubtitleTracks(id);
    },
    'library/videos/:id/subtitles/!new': function(id) {
      showNav();
      displayUploadSubtitleTrack(id);
    },
    'library/videos/:videoId/subtitles/:subtitleId/!delete': function(videoId, subtitleId) {
      showNav();
      submitDeleteSubtitleTrack(videoId, subtitleId);
    },
    'library/videos/:id/!delete': function(id) {
      showNav();
      submitDeleteVideo(id);
    },
    'library/series/:id': function(id) {
      showNav();
      displaySeries(id);
    },
    'library/categories/:category': function(category) {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#libraryNav').addClass('active');
      displayCategory(category);    
    },
    'timers': function() {
      showNav();
      $('#navbar ul li').removeClass('active');
      $('#timersNav').addClass('active');
      displayTimers();
    },
    'timers/:id/!delete': function(id) {
      showNav();
      submitDeleteTimer(id);
    }
  });
  
  $("#maincontainer").on('submit', '#pinform', function(event) {
    event.preventDefault();
    submitPin();
  });
  $("#maincontainer").on('submit', '#subtitletrackform', function(event) {
    event.preventDefault();
    submitUploadSubtitleTrack();
  });
});

function hideNav() {
  $('#tvNav').hide();
  $('#libraryNav').hide();
  $('#timersNav').hide();
  $('#switchProfileNav').hide();
}

function showNav() {
  $('#tvNav').show();
  $('#libraryNav').show();
  $('#timersNav').show();
  getProfiles(function(profiles) {
    if(profiles.length > 1) {
      $('#switchProfileNav').show();
    }
    else {
      $('#switchProfileNav').hide();
    }
  });
}
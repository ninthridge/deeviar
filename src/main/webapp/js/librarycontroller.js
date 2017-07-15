function displayLibrary() {
  getCategories(function(categories) {
    if(categories.length > 0) {
      $(location).attr('href','/#library/categories/' + categories[0]);
    }
    else {
      var model = {};
      model.categories = categories;
      model.contents = [];
      $('#maincontainer').html(Handlebars.templates.library(model)); 
    }
  });
}

function displayCategory(category) {
  getCategories(function(categories) {
    getCategory(category, function(contents) {
      var model = {};
      model.category = category;
      model.categories = categories;
      model.contents = contents;
      $('#maincontainer').html(Handlebars.templates.library(model)); 
    });
  });
}

function displaySeries(seriesId) {
  getSeries(seriesId, function(series) {
    var model = {};
    model.contents = series.episodes;
    $('#maincontainer').html(Handlebars.templates.library(model)); 
  });
}

function displayVideo(videoId) {
  getVideo(videoId, function(video) {
    var model = {};
    model.video = video;
    model.dimensions = calculateVideoDimensions(video);
    $('#maincontainer').html(Handlebars.templates.videoscreen(model)); 
  });
}

function displaySubtitleTracks(videoId) {
  getVideo(videoId, function(video) {
    var model = {};
    model.video = video;
    $('#maincontainer').html(Handlebars.templates.subtitletracks(model)); 
  });
}

function displayUploadSubtitleTrack(videoId) {
  var model = {};
  model.videoId = videoId;
  $('#maincontainer').html(Handlebars.templates.subtitletrackupload(model)); 
  
  $('#language').change(function() {
    $('#description').val($("#language option:selected").text());
  });
}

function submitUploadSubtitleTrack(subtitleTrackId) {
  videoId = $("#videoId").val();
  
  var language = $("#language").val();
  var description = $("#description").val();
  var file = $('input[type=file]')[0].files[0];
  var url = $("#url").val();
  
  if(url != null && url != "") {
    var subtitleTrack = {};
    subtitleTrack.uri = url;
    subtitleTrack.language = language;
    subtitleTrack.description = description;
    subtitleTrack.uri = url;
    saveSubtitleTrack(videoId, subtitleTrack, function() {
      toastr.success("Your subtitle track has been successfully uploaded");
      $(location).attr('href','/#library/videos/' + videoId + '/subtitles');
    });
  }
  else {
    saveSubtitleTrackFile(videoId, language, description, file, function() {
      toastr.success("Your subtitle track has been successfully uploaded");
      $(location).attr('href','/#library/videos/' + videoId + '/subtitles');
    });
  }
}

function submitDeleteSubtitleTrack(videoId, subtitleTrackId) {
  deleteSubtitleTrack(videoId, subtitleTrackId, function() {
    $(location).attr('href','/#library/videos/' + videoId + '/subtitles');
  });
}

function calculateVideoDimensions(video) {
  var dimensions = {};
    
  if(video.streams[0].height != null && video.streams[0].width != null) {
    dimensions.height = 240;
    dimensions.width = Math.round((dimensions.height * video.streams[0].width) / video.streams[0].height);
      
    var maxWidth = 320;
    var windowWidth = $(window).width();
    if(windowWidth * .9 < maxWidth) {
      maxWidth = Math.round(windowWidth * .9);
    }
      
    if(dimensions.width > maxWidth) {
      dimensions.width = maxWidth;
      dimensions.height = Math.round((dimensions.width * video.streams[0].height) / video.streams[0].width);
    }
  }
  return dimensions;
}

function submitDeleteVideo(videoId) {
  deleteVideo(videoId, function() {
    $(location).attr('href','/#library');
  });
}
function displayTv() {
  $(location).attr('href','/#tv/stations');
}

function displayGuide() {
  var colOffset = 0;
  var visibleCols = 4;
  
  getAirings(2, 0, function(airings) {
    //TODO: get serverDateAsSeconds from the server
    var serverDateAsSeconds = Date.now()/1000;
      
    var guideStart = serverDateAsSeconds + (colOffset * visibleCols * 1800) - (serverDateAsSeconds % 1800)
    var guideEnd = guideStart + (visibleCols * 1800)
      
    $.each(airings, function(key, value) {
      for(i=0; i<value.length; i++) {
        var airing = value[i];
        var guideDuration = airing.duration;
        airingStart = new Date(airing.start).getTime()/1000;
        console.log(airingStart);
        airingEnd = airingStart + airing.duration;
          
        if(airingStart < guideStart) {
          guideDuration -= guideStart - airingStart;
        }
        if(guideEnd < airingEnd) {
          guideDuration -= airingEnd - guideEnd;
        }
        if(guideDuration < 0) {
          guideDuration = 0;
        }
        
        airing.guideWidthPercent = (guideDuration * 100.0) / (1800 * (visibleCols+2));
      }
    });
      
    var guideHeaders = [];
    for(i=0; i<visibleCols; i++) {
      guideHeaders[i] = (guideStart+(i*1800))*1000;
    }
      
    var model = {};
    model.airings = airings;
    model.guideHeaders = guideHeaders;
    $('#maincontainer').html(Handlebars.templates.guide(model)); 
  });
}

function displayStations() {
  getStations(function(stations) {
    var model = {};
    model.stations = stations;
    $('#maincontainer').html(Handlebars.templates.stations(model));
  });
}

var streamingIntervalVariable = null;

function displayStation(stationId) {
  getStation(stationId, function(station) {
    startOrRefreshStationStream(stationId, function(stream) {
      getStationStream(stationId, function(stream) {
        var model = {};
        model.station = station;
        model.streamContent = stream.streams[0];
    
        model.width = Math.round($(window).width() * .9);
    
        if(((model.width * 3) / 4) > ($(window).height() * .9)) {
          model.width = Math.round((($(window).height() * .9) * 4) / 3);
        }
    
        if(model.width > 720) {
          model.width = 720;
        }
    
        $('#maincontainer').html(Handlebars.templates.stationscreen(model));
        
        streamingIntervalVariable = setInterval(function() {
          var player = $('#streamingplayer');
	      if(player != null && !player.ended) {
	        startOrRefreshStationStream(stationId, function() {});
	      }
	    }, 30000);
        
        var streamingPlayer = videojs('streamingplayer');
        streamingPlayer.play();
      });
    });
  });
}
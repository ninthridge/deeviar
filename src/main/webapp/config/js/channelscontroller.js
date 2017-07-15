function displayChannels() {
  getChannels(function(channels) {
    getProfiles(function(profiles) {
      var model = {};
      model.channels = channels;
      model.profiles = profiles;
      $('#maincontainer').html(Handlebars.templates.channels(model)); 
    });
  });
}

function submitChannels() {
  var data = {};
  $("#channelsform").serializeArray().map(function(x){data[x.name] = x.value;}); 
  
  var channels = [];
  $.each(data, function(key, value) {
    var splitKey = key.split("|");
    if(splitKey.length == 2) {
      var channel = {};
      channel.lineupId = splitKey[0];
      channel.stationId = splitKey[1];
      channel.channel = value;
      channel.profileTitles = [];
      channels.push(channel);
    }
  });
  $.each(data, function(key, value) {
    var splitKey = key.split("|");
    if(splitKey.length == 3) {
      var lineupId = splitKey[0];
      var stationId = splitKey[1];
      var profileTitle = splitKey[2];
      
      for(i=0; i<channels.length; i++) {
        if(channels[i].lineupId == lineupId && channels[i].stationId == stationId) {
          channels[i].profileTitles.push(profileTitle);
        }
      }
    }
  });
  
  saveChannels(channels, function() {
    displayChannels();
  });
}
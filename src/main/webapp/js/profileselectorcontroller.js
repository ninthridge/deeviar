function displayProfileSelector() {
  getProfiles(function(profiles) {
    if(profiles.length == 1 && !profiles[0].restricted) {
      submitSelectProfile(profiles[0].title);
    }
    else {
      var model = {};
      model.profiles = profiles;
      $('#maincontainer').html(Handlebars.templates.profileselector(model));
    }
  }); 
}

function submitSelectProfile(profileTitle) {
  getProfiles(function(profiles) {
    var profile = null;
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("profile");
    
    for(i=0; i<profiles.length; i++) {
      if(profiles[i].title == profileTitle) {
        profile = profiles[i];
        sessionStorage.setItem("profile", JSON.stringify(profile));
      }
    }
    
    if(profile != null) {
      if(profile.restricted) {
        displayEnterPin(profile.title);
      }
      else {
        requestToken(profile.title, "");
      }
    }
    else {
      toastr.error("Invalid profile");
    }
  });
}

function displayEnterPin(profileTitle) {
  var model = {};
  model.profileTitle = profileTitle;
  $('#maincontainer').html(Handlebars.templates.enterpin(model));
}

function submitPin() {
  requestToken($("#profileTitle").val(), $("#pin").val());
}

function requestToken(profileTitle, pin) {
  getToken(profileTitle, pin, function(token) {
    setToken(token);
  }); 
}

function setToken(token) {
  sessionStorage.setItem("token", token);
  $(location).attr('href','/#tv'); 
}
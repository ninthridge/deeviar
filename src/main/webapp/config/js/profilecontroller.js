function displayProfiles() {
  getProfiles(function(profiles) {
    var model = {};
    model.profiles = profiles;
    
    //force the image to not use the browser cache
    for(i=0; i<model.profiles.length; i++) {
      var profile = model.profiles[i];
      if(profile.hdPosterUri != null && profile.hdPosterUri != "") {
        profile.hdPosterUri = profile.hdPosterUri + "?timestamp=" + new Date().getTime();
      }
    }
    
    $('#maincontainer').html(Handlebars.templates.profiles(model)); 
  });
}

function displayCreateProfile() {
  var model = {};
  $('#maincontainer').html(Handlebars.templates.profiletitle(model));
}

function submitCreateProfile() {
  var profile = {};
  profile.title = $("#title").val().trim();
  if(profile.title.length == 0) {
    toastr.error("Please enter a title");
 }
 else {
  	saveProfile(profile, function() {
      $(location).attr('href','/config/#profiles');
    });
 }
}

function displayEditProfilePin(profileTitle) {
  var model = {};
  model.title = profileTitle;
  $('#maincontainer').html(Handlebars.templates.profilepin(model));
}

function submitProfilePin() {
  var profile = {};
  profile.title = $("#title").val();
  profile.pin = $("#pin").val();
  if($("#verifypin").val() != profile.pin) {
    toastr.error("The entered pins do not match");
  }
  else {
    if(profile.pin.length > 0) {
      profile.pin = forge_sha256(profile.pin);
    }
    saveProfile(profile, function() {
      $(location).attr('href','/config/#profiles');
    });
  }
}

function displayEditProfilePermissions(profileTitle) {
  getProfile(profileTitle, function (profile) {
    var model = profile;
    $('#maincontainer').html(Handlebars.templates.profilepermissions(model)); 
  });
}

function submitProfilePermissions() {
  var profile = {};
  profile.title = $("#title").val();
  profile.permissions = [];
  if($("#deletePermission").is(':checked')) {
    profile.permissions.push("DELETE");
  }
    
  saveProfile(profile, function() {
    $(location).attr('href','/config/#profiles');
  });
}

function displayEditProfileImage(profileTitle) {
  if(profileTitle != null) {
    getProfile(profileTitle, function (profile) {
      var model = profile;
      $('#maincontainer').html(Handlebars.templates.profileimage(model)); 
    });
  }
  else {
    toastr.error("Invalid profile");
  }
}

function submitProfileImage() {
  profileTitle = $("#title").val();
  
  var file = $('input[type=file]')[0].files[0];
  var url = $("#url").val();
  
  if(url != null && url != "") {
    var profile = {};
    profile.title = profileTitle;
    profile.hdPosterUri = url;
    
    saveProfile(profile, function() {
      toastr.success("Your image has been successfully uploaded");
      $(location).attr('href','/config/#profiles');
    });
  }
  else {
    saveProfileImage(profileTitle, file, function() {
      toastr.success("Your image has been successfully uploaded");
      $(location).attr('href','/config/#profiles');
    });
  }
}

function displayLibraryImports(profileTitle) {
  if(profileTitle != null) {
    getProfile(profileTitle, function(profile) {
      var model = profile;
      $('#maincontainer').html(Handlebars.templates.libraryimports(model));
    });
  }
  else {
    toastr.error("Invalid profile");
  }
}

function displayLibraryImportCategory(profileTitle, category) {
  if(profileTitle != null) {
    getProfile(profileTitle, function(profile) {
      var model = {};
      model.title = profile.title;
      if(category != "!new") {
        model.category = category;
        model.locations = profile.libraryImportLocations[category];
      }
        
      $('#maincontainer').html(Handlebars.templates.libraryimportcategory(model)); 
      $('#addlocationbutton').click(function(event) {
        event.preventDefault();
        $('#locationscontainer').append('<div><div class="col-xs-11"><input type="text" class="form-control" name="location" placeholder="File system or smb location"></div><div class="col-xs-1"><button type="button" class="btn btn-danger">X</button></div> </div>'); 
      });
      $('#deletelibraryimportcategory').click(function(event) {
        event.preventDefault();
        submitDeleteLibraryImportCategory();
      });
      $('#locationscontainer').on("click", "button", function(event) {
        event.preventDefault();
        $(event.target).parent().parent().remove()
      });
    });
  }
  else {
    var model = {};
    $('#maincontainer').html(Handlebars.templates.profile(model)); 
  }
}

function submitLibraryImportCategory() {
  var profileTitle = $("#title").val();
  var category = $("#category").val();
  
  if(category.trim().length == 0) {
    toastr.error("Please enter a category");
  }
  else {
    if(profileTitle != null) {
      getProfile(profileTitle, function(profile) {
        var profile = profile;
        profile.libraryImportLocations[category] = [];
          
        var locationFields = $('input[name="location"]');
         if(locationFields.length > 0) {
         locationFields.each(function () {
            var val = $(this).val();
            if(val.trim().length > 0) {
              profile.libraryImportLocations[category].push(val);
            }
          });
        }
        
        saveProfile(profile, function() {
          toastr.success(profile.title + ' has been saved');
          $(location).attr('href','/config/#profiles/' + profile.title + '/libraryimports');
        });
      });
    }
  }
}

function submitDeleteLibraryImportCategory(profileTitle, category) {
  if(profileTitle != null) {
    getProfile(profileTitle, function(profile) {
      delete profile.libraryImportLocations[category];
      saveProfile(profile, function() {
        toastr.success(profile.title + ' has been saved');
        $(location).attr('href','/config/#profiles/' + profile.title + '/libraryimports');
      });
    });
  }
}

function submitDeleteProfile(profileTitle) {
  deleteProfile(profileTitle, function() {
    $(location).attr('href','/config/#profiles');
  });
}


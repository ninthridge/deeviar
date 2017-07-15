function displayContentConfig() {
  getContentConfig(function(contentConfig) {
    var model = contentConfig;
    $('#maincontainer').html(Handlebars.templates.contentconfig(model)); 
  });
}

function submitContentConfig() {
  var data = {};
  $("#contentconfigform").serializeArray().map(function(x){data[x.name] = x.value;}); 
  saveContentConfig(data, function() { } );
}
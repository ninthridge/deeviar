function displayTimers() {
  getTimers(function(timers) {
    var model = {};
    model.timers = timers;
    $('#maincontainer').html(Handlebars.templates.timers(model)); 
  });
}

function submitDeleteTimer(id) {
  deleteTimer(id, function() {
    $(location).attr('href','/#timers');
  });
}
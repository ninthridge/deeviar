Handlebars.registerHelper("contains", function( value, array, options ){
  array = ( array instanceof Array ) ? array : [array];
  return (array.indexOf(value) > -1) ? options.fn( this ) : "";
});

Handlebars.registerHelper("equals", function( value1, value2, options ){
  return (value1 == value2) ? options.fn( this ) : "";
});

Handlebars.registerHelper("notequals", function( value1, value2, options ){
  return (value1 != value2) ? options.fn( this ) : "";
});

Handlebars.registerHelper("formatDate", function(datetime, format) {
  return moment(datetime).format(format);
});

Handlebars.registerHelper("formatDayOfWeek", function(dayOfWeek) {
  if(dayOfWeek == 1) {
    return "Sun";
  }
  if(dayOfWeek == 2) {
    return "Mon";
  }
  if(dayOfWeek == 3) {
    return "Tue";
  }
  if(dayOfWeek == 4) {
    return "Wed";
  }
  if(dayOfWeek == 5) {
    return "Thu";
  }
  if(dayOfWeek == 6) {
    return "Fri";
  }
  if(dayOfWeek == 7) {
    return "Sat";
  }
});

Handlebars.registerHelper("formatTime", function(hours, minutes) {
  var am = true;
  if(hours > 12) {
    am = false;
    hours -= 12;
  }
  var str = hours + ":" + minutes;
  if(minutes == 0) {
    str += "0";
  }
  if(am) {
    str += " AM";
  }
  else {
    str += " PM";
  }
  return str;
});

Handlebars.registerHelper("formatDuration", function(seconds) {
  return Math.round(seconds / 60); 
});

Handlebars.registerHelper("randomId", function() {
  return Math.floor(Math.random() * 1000000); 
});
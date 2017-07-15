$( document ).ajaxError(function( event, jqxhr, settings, thrownError ) {
  toastr.error(thrownError + " - " + jqxhr.responseText);
});
function displayDashboard() {
  getHealth(function(health) {
    $(location).attr('href','/config/#profiles');
    
    if(health.missingRequiredDependencies != null) {
      for(i=0; i<health.missingRequiredDependencies.length; i++) {
        toastr.error("<b>" + health.missingRequiredDependencies[i] + "</b> must be installed on the server.", "Missing Required Dependency", {"timeOut": "300000", "extendedTimeOut": "100000", "closeButton": true, "positionClass": "toast-bottom-full-width"});
      }
    }
    
    if(health.missingRecommendedDependencies != null) {
      for(i=0; i<health.missingRecommendedDependencies.length; i++) {
        toastr.warning("<b>" + health.missingRecommendedDependencies[i] + "</b> is recommended to be installed on the server.", "Missing Recommended Dependency", {"timeOut": "300000", "extendedTimeOut": "100000", "closeButton": true, "positionClass": "toast-bottom-full-width"});
      }
    }
    
    if(health.incompatibleDependencies != null) {
      for(i=0; i<health.incompatibleDependencies.length; i++) {
        toastr.error("An incompatible version of <b>" + health.incompatibleDependencies[i] + "</b> is installed on the server.", "Incompatible Dependency", {"timeOut": "300000", "extendedTimeOut": "100000", "closeButton": true, "positionClass": "toast-bottom-full-width"});
      }
    }
    
    if(!health.connectivity) {
      toastr.error("Please verify that the server's dns settings are configured correctly.", "Connectivity Error", {"timeOut": "300000", "extendedTimeOut": "100000", "closeButton": true, "positionClass": "toast-bottom-full-width"});
    }
    
    if(!health.schedulesDirectConnectivity) {
      toastr.error("Unable to connect to schedulesdirect.org", "Connectivity Error", {"timeOut": "300000", "extendedTimeOut": "100000", "closeButton": true, "positionClass": "toast-bottom-full-width"});
    }
    
    if(!health.tmdbConnectivity) {
      toastr.error("Unable to connect to tmdb.org", "Connectivity Error", {"timeOut": "300000", "extendedTimeOut": "100000", "closeButton": true, "positionClass": "toast-bottom-full-width"});
    }
  });
}

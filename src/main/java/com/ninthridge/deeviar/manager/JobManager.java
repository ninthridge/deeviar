package com.ninthridge.deeviar.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.Lineup;
import com.ninthridge.deeviar.repository.LineupRepository;
import com.ninthridge.deeviar.service.DeviceService;
import com.ninthridge.deeviar.service.LibraryService;
import com.ninthridge.deeviar.service.LineupService;
import com.ninthridge.deeviar.service.TimerService;
import com.ninthridge.deeviar.util.NetworkUtil;

@Component("jobManager")
public class JobManager implements Runnable {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private LineupService lineupService;

  @Autowired
  private LineupRepository lineupRepository;

  @Autowired
  private LibraryService libraryService;

  @Autowired
  private TimerService timerService;

  @Override
  public void run() {
    log.info("ip address = " + NetworkUtil.getLocalIpAddress());
    
    Date lineupRefreshLastAttempted = null;
    Date libraryLastScanned = null;
    Date timersLastCleaned = null;
    Date tunersLastRefreshed = null;
    Date devicesLastScanned = null;
    Date profileLineupsLastRefreshed = null;
    
    // TODO: make all of the timeframes configurable
    while (true) {
      try {
        // scan for devices every 10 minutes and update the lineups and timers if anything has changed
        boolean refreshProfileLineups = (profileLineupsLastRefreshed == null);
        if (tunersLastRefreshed == null || new Date().getTime() - tunersLastRefreshed.getTime() > (10 * 60 * 1000)) {
          refreshProfileLineups = deviceService.refreshTunerCache() || refreshProfileLineups;
          tunersLastRefreshed = new Date();
        }
        
        if (devicesLastScanned == null || new Date().getTime() - devicesLastScanned.getTime() > 10000) {
          refreshProfileLineups = deviceService.performScanOnWaitingDevices() || refreshProfileLineups;
          devicesLastScanned = new Date();
        }
        
        // TOOD: run this at the recommended time
        Lineup<GrabberStation> lineup = lineupRepository.get();
        Date lineupTimestamp = (lineup != null) ? lineup.getTimestamp() : null;
        // refresh only during the maintenance window and only if we haven't already refreshed within the last 12 hours
        if (lineup == null || !lineup.getLineupIds().containsAll(lineupService.getAllLineupIds())
            || (isInMaintenanceWindow()
                && (new Date().getTime() - lineup.getTimestamp().getTime()) > (12 * 60 * 60 * 1000))) {
          // only attempt once per hour
          if (lineupRefreshLastAttempted == null || lineup == null || lineup.getTimestamp() == null
              || (new Date().getTime() - lineup.getTimestamp().getTime()) > (1 * 60 * 60 * 1000)) {
            lineupService.refreshLineup();
            if (lineupRepository.get() != null && lineupRepository.get().getTimestamp() != null
                && (lineupTimestamp == null || lineupRepository.get().getTimestamp().after(lineupTimestamp))) {
              lineup = lineupRepository.get();
              lineupTimestamp = lineup.getTimestamp();
              refreshProfileLineups = true;
            }
            lineupRefreshLastAttempted = new Date();
          }
        }
        
        if(refreshProfileLineups) {
          lineupService.refreshProfileLineups();
          profileLineupsLastRefreshed = new Date();
        }
        
        // scan every 5 minutes
        if (libraryLastScanned == null || new Date().getTime() - libraryLastScanned.getTime() > (5 * 60 * 1000)) {
          libraryService.scan();
          libraryLastScanned = new Date();
        }

        // clean our old timers every 5 minutes
        if (timersLastCleaned == null || new Date().getTime() - timersLastCleaned.getTime() > (5 * 60 * 1000)) {
          timerService.clean();
          timersLastCleaned = new Date();
        }

      } catch (Exception e) {
        log.error(e, e);
      }

      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        log.error(e);
      }

    }
  }

  private boolean isInMaintenanceWindow() {
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    return hour >= 2 && hour <= 5;
  }
}

package com.ninthridge.deeviar.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.model.Airing;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.Time;
import com.ninthridge.deeviar.model.Timer;
import com.ninthridge.deeviar.model.TimerOccurrence;
import com.ninthridge.deeviar.model.TimerType;
import com.ninthridge.deeviar.repository.TimerOccurrenceRepository;
import com.ninthridge.deeviar.repository.TimerRepository;
import com.ninthridge.deeviar.util.IdUtil;

@Service("timerService")
public class TimerService {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private TimerRepository timerRepository;

  @Autowired
  private TimerOccurrenceRepository timerOccurrenceRepository;
  
  @Autowired
  private LineupService lineupService;

  @Autowired
  private ProfileService profileService;

  public Set<TimerOccurrence> add(String profileTitle, String airingId, TimerType timerType) {
    Airing airing = lineupService.findAiring(profileTitle, airingId);
    if (airing != null) {
      Timer timer = new Timer();
      timer.setProfile(profileTitle);
      timer.setStationId(airing.getStationId());
      timer.setCallSign(lineupService.findProfileStationById(profileTitle, airing.getStationId()).getCallSign());
      if (TimerType.Once.equals(timerType)) {
        timer.setDate(airing.getStart());
        timer.setDuration(airing.getDuration());
      } else {
        Calendar start = new GregorianCalendar();
        start.setTime(airing.getStart());

        if (TimerType.Title.equals(timerType) || TimerType.TitleDaily.equals(timerType)
            || TimerType.TitleWeekdays.equals(timerType) || TimerType.TitleWeekly.equals(timerType)) {
          timer.setTitle(airing.getTitle());
        }

        if (TimerType.Weekdays.equals(timerType) || TimerType.TitleWeekdays.equals(timerType)) {
          timer.setDaysOfWeek(
              Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY));
        } else if (TimerType.Weekly.equals(timerType) || TimerType.TitleWeekly.equals(timerType)) {
          timer.setDaysOfWeek(Arrays.asList(start.get(Calendar.DAY_OF_WEEK)));
        }

        if (!TimerType.Title.equals(timerType)) {
          Time startTime = new Time();
          startTime.setHours(start.get(Calendar.HOUR_OF_DAY));
          startTime.setMinutes(start.get(Calendar.MINUTE));

          timer.setStartTime(startTime);
          timer.setDuration(airing.getDuration());
        }
      }
      timer.setId(IdUtil.id(timer)); // TODO: this might not be unique enough, maybe use a UUID?
      Set<Timer> timersSet = getTimers(profileTitle);
      if(timersSet == null) {
        timersSet = new HashSet<>();
      }
      timersSet.add(timer);
      timerRepository.save(profileTitle, timersSet);
      return createTimerOccurrences(timer);
    }

    return new HashSet<>();
  }

  public Set<TimerOccurrence> getTimerOccurrences(String profileTitle, String stationId, Date date) {
    Set<TimerOccurrence> activeTimerOccurrences = new HashSet<>();
    Set<TimerOccurrence> timerOccurrences = timerOccurrenceRepository.get(profileTitle);
    if(timerOccurrences != null) {
      for (TimerOccurrence timerOccurrence : timerOccurrences) {
        if (stationId == null || stationId.equals(timerOccurrence.getStationId())) {
          if (date == null
              || (!date.before(timerOccurrence.getStartDate()) && date.before(timerOccurrence.getEndDate()))) {
            activeTimerOccurrences.add(timerOccurrence);
          }
        }
      }
    }
    return activeTimerOccurrences;
  }

  public Set<Timer> getTimers(String profileTitle) {
    Set<Timer> timers = timerRepository.get(profileTitle);
    if(timers != null) {
      return timers;
    }
    else {
      return new HashSet<>();
    }
    
  }

  public TimerOccurrence getTimerOccurrence(String profileTitle, String timerOccurrenceId) {
    Set<TimerOccurrence> timerOccurrences = timerOccurrenceRepository.get(profileTitle);
    if(timerOccurrences != null) {
      for(TimerOccurrence timerOccurrence : timerOccurrences) {
        if(timerOccurrence.getId().equals(timerOccurrenceId)) {
          return timerOccurrence;
        }
      }
    }
    return null;
  }
  
  public void deleteTimerOccurrence(String profileTitle, String timerOccurrenceId) {
    TimerOccurrence timerOccurrence = getTimerOccurrence(profileTitle, timerOccurrenceId);
    if(timerOccurrence != null) {
      timerOccurrenceRepository.get(profileTitle).remove(timerOccurrence);
    }
  }

  public void deleteTimer(String profileTitle, String timerId) {
    Set<TimerOccurrence> timerOccurrences = getTimerOccurrences(profileTitle, null, null);
    for (TimerOccurrence timerOccurrence : timerOccurrences) {
      if (timerOccurrence.getTimerId().equals(timerId)) {
        deleteTimerOccurrence(profileTitle, timerOccurrence.getId());
        // TODO: retrieve airings by timer id
        Airing airing = lineupService.findAiring(profileTitle, timerOccurrence.getStationId(),
            timerOccurrence.getStartDate());
        if (airing != null) {
          airing.getTimerIds().remove(timerId);
        }
      }
    }

    Set<Timer> timers = getTimers(profileTitle);
    if(timers != null) {
      for (Iterator<Timer> it = timers.iterator(); it.hasNext();) {
        Timer timer = it.next();
        if (timer.getId().equals(timerId)) {
          it.remove();
        }
      }
    }
    timerRepository.save(profileTitle, timers);
  }

  public void refreshTimerOccurrences() {
    log.info("Refreshing timer occurrences");
    for (Profile profile : profileService.findAll()) {
      if(timerOccurrenceRepository == null) {
        System.out.println("1");
      }
      if(profile == null) {
        System.out.println("2");
      }
      timerOccurrenceRepository.delete(profile.getTitle());
      Set<Timer> timersSet = getTimers(profile.getTitle());
      if(timersSet != null) {
        for (Timer timer : timersSet) {
          createTimerOccurrences(timer);
        }
      }
    }
    log.info("Completed loading timers");
  }

  public void clean() {
    for (Profile profile : profileService.findAll()) {
      Set<Timer> timers = getTimers(profile.getTitle());
      Set<Timer> deleteableTimers = new HashSet<>();
      if(timers != null) {
        for (Timer timer : timers) {
          if (timer.getDate() != null && new Date().after(timer.getDate())) {
            deleteableTimers.add(timer);
          }
        }
      }
      
      for(Timer timer : deleteableTimers) {
        deleteTimer(profile.getTitle(), timer.getId());
      }

      Set<TimerOccurrence> timerOccurrences = getTimerOccurrences(profile.getTitle(), null, null);
      Set<TimerOccurrence> deleteableTimerOccurrences = new HashSet<>();
      if(timerOccurrences != null) {
        for (TimerOccurrence timerOccurrence : timerOccurrences) {
          if (new Date().after(timerOccurrence.getEndDate())) {
            deleteableTimerOccurrences.add(timerOccurrence);
          }
        }
      }

      for(TimerOccurrence timerOccurrence : deleteableTimerOccurrences) {
        deleteTimerOccurrence(profile.getTitle(), timerOccurrence.getId());
      } 
    }
  }

  protected Set<TimerOccurrence> createTimerOccurrences(Timer timer) {
    Set<TimerOccurrence> timerOccurrences = new HashSet<>();

    Date date = new Date();
    log.info("Creating timer occurrences for " + timer);
    if (timer.getDate() != null) {
      Calendar endDate = new GregorianCalendar();
      endDate.setTime(timer.getDate());
      endDate.add(Calendar.SECOND, timer.getDuration().intValue());
      if (endDate.getTime().after(date)) {
        timerOccurrences.add(createTimerOccurrence(timer.getProfile(), timer.getStationId(), timer.getDate(),
            timer.getDuration(), timer.getId()));
        Airing airing = lineupService.findAiring(timer.getProfile(), timer.getStationId(), timer.getDate());
        if (airing != null) {
          airing.getTimerIds().add(timer.getId());
        }
      }
    } else if (timer.getTitle() == null) {
      if (timer.getDaysOfWeek() != null && timer.getStartTime() != null) {
        for (Integer dayOfWeek : timer.getDaysOfWeek()) {
          Calendar start = new GregorianCalendar();
          start.set(Calendar.DAY_OF_WEEK, dayOfWeek);
          start.set(Calendar.HOUR_OF_DAY, timer.getStartTime().getHours());
          start.set(Calendar.MINUTE, timer.getStartTime().getMinutes());
          start.set(Calendar.SECOND, 0);
          start.set(Calendar.MILLISECOND, 0);
          timerOccurrences.add(createTimerOccurrence(timer.getProfile(), timer.getStationId(), start.getTime(),
              timer.getDuration(), timer.getId()));
          Airing airing1 = lineupService.findAiring(timer.getProfile(), timer.getStationId(), start.getTime());
          if (airing1 != null) {
            airing1.getTimerIds().add(timer.getId());
          }

          start.add(Calendar.WEEK_OF_YEAR, 1);
          timerOccurrences.add(createTimerOccurrence(timer.getProfile(), timer.getStationId(), start.getTime(),
              timer.getDuration(), timer.getId()));
          Airing airing2 = lineupService.findAiring(timer.getProfile(), timer.getStationId(), start.getTime());
          if (airing2 != null) {
            airing2.getTimerIds().add(timer.getId());
          }
        }
      } else {
        log.error("Invalid timer " + timer);
      }
    } else {
      List<Airing> airings = lineupService.findAirings(timer.getProfile(), timer.getStationId());
      for (Airing airing : airings) {
        if (airing.getEnd().after(date)) {
          if (timer.getTitle().equals(airing.getTitle())) {
            Calendar start = new GregorianCalendar();
            start.setTime(airing.getStart());
            if (timer.getDaysOfWeek() == null || timer.getDaysOfWeek().contains(start.get(Calendar.DAY_OF_WEEK))) {
              if (timer.getStartTime() == null
                  || (Math.abs(((start.get(Calendar.HOUR_OF_DAY) * 60) + start.get(Calendar.MINUTE))
                      - ((timer.getStartTime().getHours() * 60) + timer.getStartTime().getMinutes())) <= 5)) {
                timerOccurrences.add(createTimerOccurrence(timer.getProfile(), timer.getStationId(), airing.getStart(),
                    airing.getDuration(), timer.getId()));
                airing.getTimerIds().add(timer.getId());
              }
            }
          }
        }
      }
    }
    log.info("Created " + timerOccurrences.size() + " timer occurrences from " + timer);
    return timerOccurrences;
  }

  protected TimerOccurrence createTimerOccurrence(String profileTitle, String stationId, Date startDate, long duration,
      String timerId) {
    TimerOccurrence timerOccurrence = new TimerOccurrence();
    timerOccurrence.setStationId(stationId);
    timerOccurrence.setStartDate(startDate);
    timerOccurrence.setDuration(duration);
    timerOccurrence.setTimerId(timerId);
    timerOccurrence.setId(IdUtil.id(timerOccurrence)); // TODO: this might not be unique enough, maybe use a UUID?
    
    Set<TimerOccurrence> timerOccurrences = timerOccurrenceRepository.get(profileTitle);
    if(timerOccurrences == null) {
      timerOccurrences = new HashSet<>();
    }
    timerOccurrences.add(timerOccurrence);
    timerOccurrenceRepository.save(profileTitle, timerOccurrences);
    return timerOccurrence;
  }
}

package com.ninthridge.deeviar.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutorFactory;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.library.scanner.FileScanner;
import com.ninthridge.deeviar.library.scanner.FileScannerFactory;
import com.ninthridge.deeviar.library.scanner.impl.ScannedFile;
import com.ninthridge.deeviar.manager.MediaProcessingManager;
import com.ninthridge.deeviar.model.BifProcessingItem;
import com.ninthridge.deeviar.model.Content;
import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Movie;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.model.SubtitleTrack;
import com.ninthridge.deeviar.model.Video;
import com.ninthridge.deeviar.model.VideoContent;
import com.ninthridge.deeviar.model.VideoStream;
import com.ninthridge.deeviar.model.id.EpisodeId;
import com.ninthridge.deeviar.model.id.MovieId;
import com.ninthridge.deeviar.model.id.VideoId;
import com.ninthridge.deeviar.repository.DeletedRepository;
import com.ninthridge.deeviar.repository.EpisodeRepository;
import com.ninthridge.deeviar.repository.ErroredRepository;
import com.ninthridge.deeviar.repository.MovieRepository;
import com.ninthridge.deeviar.repository.SeriesRepository;
import com.ninthridge.deeviar.repository.VideoRepository;
import com.ninthridge.deeviar.util.FileNameUtil;
import com.ninthridge.deeviar.util.FileUtil;
import com.ninthridge.deeviar.util.IdUtil;
import com.ninthridge.deeviar.util.TitleUtil;

@Service("libraryService")
public class LibraryService {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private ProfileService profileService;

  @Autowired
  private MovieRepository movieRepository;
  
  @Autowired
  private SeriesRepository seriesRepository;
  
  @Autowired
  private EpisodeRepository episodeRepository;
  
  @Autowired
  private VideoRepository videoRepository;
  
  @Autowired
  private ErroredRepository erroredRepository;
  
  @Autowired
  private DeletedRepository deletedRepository;
  
  @Autowired
  private FileScannerFactory fileScannerFactory;

  @Autowired
  private MediaProcessingManager mediaProcessingManager;

  @Autowired
  private OperatingSystemCmdExecutorFactory operatingSystemCmdExecutorFactory;

  public Map<String, List<Content>> findAllActive(String profileTitle) {
    Map<String, List<Content>> map = new HashMap<String, List<Content>>();

    Set<Movie> movies = movieRepository.getAll(profileTitle);
    if(movies != null) {
      removeNonActiveContent(movies);
      for (Movie movie : movies) {
        if (movie.getActive()) {
          String category = movie.getCategory();
          List<Content> list = map.get(category);
          if (list == null) {
            list = new ArrayList<Content>();
            map.put(category, list);
          }
          list.add(movie);
        }
      }
    }

    Set<Series> seriesList = seriesRepository.getAll(profileTitle);
    if(seriesList != null) {
      removeNonActiveContent(seriesList);
      for (Series series : seriesList) {
        if (series.getEpisodes() != null && !series.getEpisodes().isEmpty()) {
          String category = series.getCategory();
          List<Content> list = map.get(category);
          if (list == null) {
            list = new ArrayList<Content>();
            map.put(category, list);
          }
          list.add(series);
        }
      }
    }

    Set<Video> videos = videoRepository.getAll(profileTitle);
    if(videos != null) {
      removeNonActiveContent(videos);
      for (Video video : videos) {
        if (video.getActive()) {
          String category = video.getCategory();
          List<Content> list = map.get(category);
          if (list == null) {
            list = new ArrayList<Content>();
            map.put(category, list);
          }
          list.add(video);
        }
      }
    }
    
    return map;
  }

  public List<Content> findAllActive(String profileTitle, String category) {
    List<Content> contents = new ArrayList<Content>();
    
    if (category != null && category.trim().length() > 0) {
      
      Set<Movie> movies = movieRepository.getAll(profileTitle);
      if(movies != null) {
        removeNonActiveContent(movies);
        for (Movie movie : movies) {
          if (category.equals(movie.getCategory())) {
            contents.add(movie);
          }
        }
      }

      Set<Series> seriesList = seriesRepository.getAll(profileTitle);
      if(seriesList != null) {
        removeNonActiveContent(seriesList);
        for (Series series : seriesList) {
          if (category.equals(series.getCategory())) {
            if (series.getEpisodes() != null && !series.getEpisodes().isEmpty()) {
              contents.add(series);
            }
          }
        }
      }

      Set<Video> videos = videoRepository.getAll(profileTitle);
      if(videos != null) {
        removeNonActiveContent(videos);
        for (Video video : videos) {
          if (category.equals(video.getCategory())) {
            contents.add(video);
          }
        }
      }
    }

    return contents;
  }

  public List<String> findActiveCategories(String profileTitle) {
    List<String> categories = new ArrayList<>(findAllActive(profileTitle).keySet());
    Collections.sort(categories, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        if(s1.equals("Movies")) {
          return -3;
        }
        else if(s2.equals("Movies")) {
          return 3;
        }
        else if(s1.equals("Series")) {
          return -2;
        }
        else if(s2.equals("Series")) {
          return 2;
        }
        else if(s1.equals("Episodes")) {
          return -1;
        }
        else if(s2.equals("Episodes")) {
          return 1;
        }
        else {
          return s1.compareTo(s2);
        }
      }
    });
    return categories;
  }

  public Series findSeriesById(String profileTitle, String seriesId) {
    return seriesRepository.get(profileTitle, seriesId);
  }

  public Video findById(String profileTitle, String id) {
    Movie movie = findMovieById(profileTitle, id);
    if (movie != null) {
      return movie;
    }
    Episode episode = findEpisodeById(profileTitle, id);
    if (episode != null) {
      return episode;
    }
    return findVideoById(profileTitle, id);
  }

  public Movie findMovieById(String profileTitle, String id) {
    return movieRepository.get(profileTitle, id);
  }

  public Episode findEpisodeById(String profileTitle, String id) {
    return episodeRepository.get(profileTitle, id);
  }

  public Video findVideoById(String profileTitle, String id) {
    return videoRepository.get(profileTitle, id);
  }

  public SubtitleTrack addSubtitleTrack(String profileTitle, String videoId, String language, String description,
      String url) throws IOException {
    try (InputStream is = new URL(url).openStream()) {
      byte[] b = IOUtils.toByteArray(is);
      return addSubtitleTrack(profileTitle, videoId, language, description, b, FileNameUtil.parseExtension(url));
    }
  }

  public SubtitleTrack addSubtitleTrack(String profileTitle, String videoId, String language, String description,
      byte[] b, String extension) throws IOException {
    Video video = findById(profileTitle, videoId);
    if (video != null) {
      File dir = new File(config.getSubtitlesDir(), profileTitle);
      if (!dir.exists()) {
        dir.mkdir();
      }
      File file = new File(dir, videoId + "_" + language + "_" + description + "." + extension);
      Files.write(file.toPath(), b);
      SubtitleTrack subtitleTrack = new SubtitleTrack();
      subtitleTrack.setLanguage(language);
      subtitleTrack.setDescription(description);
      subtitleTrack.setUri("/subtitles/" + profileTitle + "/" + file.getName());
      subtitleTrack.setId(IdUtil.id(subtitleTrack));
      if (video.getSubtitleTracks() == null) {
        video.setSubtitleTracks(new HashSet<SubtitleTrack>());
      }
      video.getSubtitleTracks().add(subtitleTrack);
      
      save(profileTitle, video);
      
      return subtitleTrack;
    }
    return null;
  }
  
  public Date getTimestamp(String profileTitle) {
    Date timestamp = null;
    Date movieTimestamp = movieRepository.getTimestamp(profileTitle);
    Date seriesTimestamp = seriesRepository.getTimestamp(profileTitle);
    Date episodeTimestamp = episodeRepository.getTimestamp(profileTitle);
    Date videoTimestamp = videoRepository.getTimestamp(profileTitle);
    Date deletedTimestamp = deletedRepository.getTimestamp(profileTitle);
    
    if(timestamp == null || (movieTimestamp != null && movieTimestamp.after(timestamp))) {
      timestamp = movieTimestamp;
    }
    if(timestamp == null || (seriesTimestamp != null && seriesTimestamp.after(timestamp))) {
      timestamp = seriesTimestamp;
    }
    if(timestamp == null || (episodeTimestamp != null && episodeTimestamp.after(timestamp))) {
      timestamp = episodeTimestamp;
    }
    if(timestamp == null || (videoTimestamp != null && videoTimestamp.after(timestamp))) {
      timestamp = videoTimestamp;
    }
    if(timestamp == null || (deletedTimestamp != null && deletedTimestamp.after(timestamp))) {
      timestamp = deletedTimestamp;
    }
    
    return timestamp;
  }

  public void save(String profileTitle, VideoContent videoContent) {
    if (videoContent instanceof Movie) {
      Movie movie = (Movie) videoContent;
      movieRepository.save(profileTitle, movie);
      log.info("Saved " + movie.getTitle() + " (" + movie.getYear() + ")");
    } else if (videoContent instanceof Series) {
      Series series = (Series) videoContent;
      seriesRepository.save(profileTitle, series);
      log.info("Saved " + series.getTitle());
    } else if (videoContent instanceof Episode) {
      Episode episode = (Episode) videoContent;
      Series series = findSeriesById(profileTitle, episode.getSeriesId());
      if (series != null) {
        series.getEpisodes().add(episode);
        episodeRepository.save(profileTitle, episode);
        log.info("Saved " + episode.getTitle());
      }
    } else if (videoContent instanceof Video) {
      Video video = (Video) videoContent;
      videoRepository.save(profileTitle, video);
      log.info("Saved " + video.getTitle());
    }
    else {
      throw new RuntimeException("Invalid content " + videoContent.getClass());
    }
  }

  public void addErrored(String profileTitle, String canonicalPath) {
    Map<String, Date> errored = erroredRepository.get(profileTitle);
    if(errored == null) {
      errored = new HashMap<>();
    }
    errored.put(canonicalPath, new Date());
    erroredRepository.save(profileTitle, errored);
  }

  public void delete(String profileTitle, String id) {
    Video video = findById(profileTitle, id);
    if (video != null) {
      for (VideoStream videoStream : video.getStreams()) {
        if (videoStream.getUri() != null) {
          FileUtil.deleteFile(new File(config.getVideosDir().getParentFile(), videoStream.getUri()));
        }
      }

      if (video.getSubtitleTracks() != null) {
        for (SubtitleTrack subtitleTrack : video.getSubtitleTracks()) {
          if (subtitleTrack.getUri() != null) {
            FileUtil.deleteFile(new File(config.getSubtitlesDir().getParentFile(), subtitleTrack.getUri()));
          }
        }
      }

      if (video.getHdBifUri() != null) {
        FileUtil.deleteFile(new File(config.getBifsDir().getParentFile(), video.getHdBifUri()));
      }

      if (video.getSdBifUri() != null) {
        FileUtil.deleteFile(new File(config.getBifsDir().getParentFile(), video.getSdBifUri()));
      }

      if (video.getHdPosterUri() != null) {
        FileUtil.deleteFile(new File(config.getImagesDir().getParentFile(), video.getHdPosterUri()));
      }

      if (video.getSdPosterUri() != null) {
        FileUtil.deleteFile(new File(config.getImagesDir().getParentFile(), video.getSdPosterUri()));
      }

      if(video instanceof Movie ) {
        movieRepository.delete(profileTitle, id);
      }
      else if (video instanceof Episode) {
        episodeRepository.delete(profileTitle, id);
        String seriesId = ((Episode) video).getSeriesId();
        if (seriesId != null) {
          Series series = findSeriesById(profileTitle, seriesId);
          if (series != null) {
            series.getEpisodes().remove(video);
            if(series.getEpisodes() == null || series.getEpisodes().isEmpty()) {
              seriesRepository.delete(profileTitle, series.getId());
              if (series.getHdPosterUri() != null) {
                FileUtil.deleteFile(new File(config.getImagesDir().getParentFile(), series.getHdPosterUri()));
              }
  
              if (series.getSdPosterUri() != null) {
                FileUtil.deleteFile(new File(config.getImagesDir().getParentFile(), series.getSdPosterUri()));
              }
  
              FileUtil.deleteRecursively(new File(config.getVideosDir(), getRelativeSeriesDir(profileTitle, series)));
            }
          }
        }
      }
      else if(video instanceof Video) {
        videoRepository.delete(profileTitle, id);
      }

      Map<String, Date> deleted = deletedRepository.get(profileTitle);
      if(deleted == null) {
        deleted = new HashMap<>();
      }
      deleted.put(id, new Date());
      deletedRepository.save(profileTitle, deleted);
    }
  }

  public void deleteSubtitleTrack(String profileTitle, String videoId, String subtitleTrackId) {
    Video video = findById(profileTitle, videoId);
    if(video != null) {
      if (video.getSubtitleTracks() != null) {
        for (Iterator<SubtitleTrack> it = video.getSubtitleTracks().iterator(); it.hasNext();) {
          SubtitleTrack subtitleTrack = it.next();
          if (subtitleTrack.getId().equals(subtitleTrackId)) {
            if (subtitleTrack.getUri() != null) {
              FileUtil.deleteFile(new File(config.getSubtitlesDir().getParentFile(), subtitleTrack.getUri()));
              it.remove();
            }
          }
        }
      }
      save(profileTitle, video);
    }
    else {
      throw new RuntimeException("Unable to find video " + videoId);
    }
  }

  public void scan() {
    if (mediaProcessingManager.isProcessingQueueEmpty()) {
      log.info("Starting library scan");
      Set<Profile> profiles = profileService.findAll();
      for (Profile profile : profiles) {
        Map<String, Set<String>> libraryImportLocations = new HashMap<>(profile.getLibraryImportLocations());
        
        if(new File(config.getVideosDir(), profile.getTitle()).exists()) {
          for(File categoryDir : new File(config.getVideosDir(), profile.getTitle()).listFiles()) {
            String category = categoryDir.getName();
            Set<String> categoryLocations = libraryImportLocations.get(category);
            if(categoryLocations == null) {
              categoryLocations = new HashSet<String>();
              libraryImportLocations.put(category, categoryLocations);
            }
            categoryLocations.add(categoryDir.getAbsolutePath());
          }
        }
        
        if (libraryImportLocations != null) {
          for (String category : libraryImportLocations.keySet()) {
            Set<String> locations = libraryImportLocations.get(category);
            for (String location : locations) {
              log.info("Scanning " + category + " " + location);
              scan(profile.getTitle(), category, location);
            }
          }
        }
      }
      log.info("Completed library scan");
    } else {
      log.info("Media processor queue size: " + mediaProcessingManager.processingQueueSize());
    }
  }

  protected void scan(String profileTitle, String category, String location) {
    if (location.endsWith("/")) {
      location = location.substring(0, location.length() - 1);
    }
    FileScanner fileScanner = fileScannerFactory.getScanner(location);
    if (fileScanner != null) {
      List<ScannedFile> files = fileScanner.scan(location);
      if (files != null) {
        log.info("Found " + files.size() + " items in " + location);
        for (ScannedFile file : files) {
          MediaProcessingItem mediaProcessingItem = new MediaProcessingItem();
          mediaProcessingItem.setProfileTitle(profileTitle);
          mediaProcessingItem.setCategory(category);
          mediaProcessingItem.setCanonicalPath(location + file.getUri());
          mediaProcessingItem.setTimestamp(file.getTimestamp());
          mediaProcessingItem.setReleaseDate(file.getTimestamp());
          // TODO: configurable delete source
          mediaProcessingItem.setDeleteSource(false);
          mediaProcessingItem.setCompress(false);
          mediaProcessingItem.setPriority(2);
          
          String id = null;
          
          if (category.equals("Movies")) {
            MovieId movieId = FileNameUtil.parseMovieFromUri(file.getUri());
            mediaProcessingItem.setTitle(movieId.getTitle());
            mediaProcessingItem.setYear(movieId.getYear());
            id = movieId.getId();
          } else if (category.equals("Series")) {
            EpisodeId episodeId = FileNameUtil.parseEpisodeFromUri(file.getUri());
            mediaProcessingItem.setTitle(episodeId.getSeriesId().getTitle());
            mediaProcessingItem.setYear(episodeId.getSeriesId().getYear());
            mediaProcessingItem.setSeason(episodeId.getSeason());
            mediaProcessingItem.setEpisode(episodeId.getEpisode());
            id = episodeId.getId();
          } else {
            VideoId videoId = FileNameUtil.parseVideoFromUri(file.getUri());
            mediaProcessingItem.setTitle(videoId.getTitle());
            id = videoId.getId();
          }
          
          Date timestamp = getTimestamp(profileTitle, id);
          
          if(timestamp == null || mediaProcessingItem.getTimestamp().after(timestamp)) {
            mediaProcessingManager.addToProcessingQueue(mediaProcessingItem);
          }
        }
      }
    }
  }

  public Video bookmark(String profileTitle, String id, Integer bookmarkPosition) {
    Video video = findById(profileTitle, id);
    if (video != null) {
      video.setBookmarkPosition(bookmarkPosition);
      video.setBookmarkDate(new Date());
      double percentComplete = new Double(bookmarkPosition) / new Double(video.getStreams().get(0).getLength());
      log.info(profileTitle + " " + id + " " + video.getTitle() + " " + bookmarkPosition + " of " + video.getStreams().get(0).getLength() + " " + Math.floor(percentComplete * 100.0) + "%");
      if (percentComplete >= .92) {
        video.setWatched(true);
      } else {
        video.setWatched(false);
      }
      save(profileTitle, video);
      return video;
    }
    else {
      throw new RuntimeException("Unable to find video " + id);
    }
  }

  public Video favorite(String profileTitle, String id, boolean favorite) {
    Video video = findById(profileTitle, id);
    if (video != null) {
      if (favorite) {
        video.setFavorite(true);
      } else {
        video.setFavorite(false);
      }
      save(profileTitle, video);
      return video;
    }
    else {
      throw new RuntimeException("Unable to find video " + id);
    }
  }

  public void loadBifProcessingQueue() {
    try {
      if (operatingSystemCmdExecutorFactory.getExecutor().programExists("biftool")) {
        for (Profile profile : profileService.findAll()) {
          Set<Movie> movies = movieRepository.getAll(profile.getTitle());
          if(movies != null) {
            for (Movie movie : movies) {
              if (movie.getHdBifUri() == null) {
                mediaProcessingManager.addToPostProcessingQueue(new BifProcessingItem(profile.getTitle(), movie, 2));
                log.info("Added " + movie + " to the post processing queue");
  
              }
            }
          }
          
          Set<Episode> episodes = episodeRepository.getAll(profile.getTitle());
          if(episodes != null) {
            for (Episode episode : episodes) {
              if (episode.getHdBifUri() == null) {
                mediaProcessingManager.addToPostProcessingQueue(new BifProcessingItem(profile.getTitle(), episode, 2));
                log.info("Added " + episode + " to the post processing queue");
              }
            }
          }
          
          Set<Video> videos = videoRepository.getAll(profile.getTitle());
          if(videos != null) {
            for (Video video : videos) {
              if (video.getHdBifUri() == null) {
                mediaProcessingManager.addToPostProcessingQueue(new BifProcessingItem(profile.getTitle(), video, 2));
                log.info("Added " + video + " to the post processing queue");
              }
            }
          }
        }
      }
    } catch (IOException e) {
      log.error(e, e);
    }
  }

  public Video updateBif(String profileTitle, String videoId, String hdBifUri, String sdBifUri) {
    Video video = findById(profileTitle, videoId);
    
    if (video != null) {
      video.setHdBifUri(hdBifUri);
      video.setSdBifUri(sdBifUri);
      save(profileTitle, video);
      return video;
    }
    else {
      throw new RuntimeException("Unable to find video " + videoId);
    }
  }

  public Date getTimestamp(String profileTitle, String id) {
    Video video = findById(profileTitle, id);
    if (video != null) {
      return video.getTimestamp();
    }
    else {
      Date deletedTimestamp = getDeletedTimestamp(profileTitle, id);
      Date erroredTimestamp = getErroredTimestamp(profileTitle, id);
      
      if(deletedTimestamp == null) {
        return erroredTimestamp;
      }
      else if(erroredTimestamp == null) {
        return deletedTimestamp;
      }
      else if(deletedTimestamp.after(erroredTimestamp)) {
        return deletedTimestamp;
      }
      else {
        return erroredTimestamp;
      }
    }
  }

  public Date getDeletedTimestamp(String profileTitle, String id) {
    Map<String, Date> deleted = deletedRepository.get(profileTitle);
    if(deleted != null) {
      return deleted.get(id);
    }
    return null;
  }

  public Date getErroredTimestamp(String profileTitle, String canonicalPath) {
    Map<String, Date> errored = erroredRepository.get(profileTitle);
    if(errored != null) {
      return errored.get(canonicalPath);
    }
    return null;
  }

  public String getRelativeSeriesDir(String profileTitle, Series series) {
    return File.separator + profileTitle + File.separator + "Series" + File.separator
        + TitleUtil.cleanse(series.getTitle());
  }

  public String getRelativeMovieFile(String profileTitle, Movie movie) {
    String fileName = TitleUtil.cleanse(movie.getTitle());
    if (movie.getYear() != null) {
      fileName += " (" + movie.getYear() + ")";
    }
    fileName += ".mp4";

    return File.separator + profileTitle + File.separator + movie.getCategory() + File.separator + fileName;
  }

  public String getRelativeEpisodeFile(String profileTitle, Episode episode) {

    Series series = findSeriesById(profileTitle, episode.getSeriesId());

    String seriesDir = getRelativeSeriesDir(profileTitle, series);

    String fileName = TitleUtil.cleanse(series.getTitle());
    if (episode.getSeason() != null && episode.getEpisode() != null) {
      fileName += " - S" + String.format("%02d", episode.getSeason()) + "E"
          + String.format("%02d", episode.getEpisode());
    } else {
      log.warn("Episode has no season and/or episode number information: " + episode.getTitle());
      fileName += " - " + episode.getId();
    }

    if (episode.getShortDescription() != null && episode.getShortDescription().trim().length() > 0) {
      fileName += " - " + TitleUtil.cleanse(episode.getShortDescription());
    }

    fileName += ".mp4";

    return seriesDir + File.separator + fileName;
  }

  public String getRelativeVideoFile(String profileTitle, Video video) {
    String fileName = TitleUtil.cleanse(video.getTitle()) + ".mp4";
    return File.separator + profileTitle + File.separator + video.getCategory() + File.separator + fileName;
  }

  private void removeNonActiveContent(Collection<? extends Content> contentCollection) {
    for (Iterator<? extends Content> it = contentCollection.iterator(); it.hasNext();) {
      Content content = it.next();
      if (!content.getActive()) {
        it.remove();
      }
    }
  }
}
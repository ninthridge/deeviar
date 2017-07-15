package com.ninthridge.deeviar.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ninthridge.deeviar.model.id.EpisodeId;
import com.ninthridge.deeviar.model.id.MovieId;
import com.ninthridge.deeviar.model.id.SeriesId;
import com.ninthridge.deeviar.model.id.VideoId;

public class FileNameUtil {
  
  //TODO: move to config
  public static List<String> VIDEO_FILE_EXTENSIONS = Arrays.asList(new String[]{"avi", "mpg", "vob", "mp4", "m2ts","mov","3gp","mkv", "wmv"});
  public static List<String> IMAGE_FILE_EXTENSIONS = Arrays.asList("gif", "png", "jpg", "jpeg");
  public static List<String> SUBTITLE_FILE_EXTENSIONS = Arrays.asList("srt");
  
  
  private static Pattern[] EPISODE_PATTERNS = new Pattern[]{
    Pattern.compile("s[0-9]{1,4}[\\._]?e[0-9]{1,4}"),
    Pattern.compile("[0-9]{1,4}x[0-9]{1,4}")
  };

  public static boolean isVideoFile(String fileName) {
    String extension = parseExtension(fileName);
    return (extension != null && VIDEO_FILE_EXTENSIONS.contains(extension));
  }

  public static boolean isImageFile(String fileName) {
    String extension = FileNameUtil.parseExtension(fileName);
    return (extension != null && IMAGE_FILE_EXTENSIONS.contains(extension.toLowerCase()));
  }
  
  public static boolean isSubtitleTrackFile(String fileName) {
    String extension = FileNameUtil.parseExtension(fileName);
    return (extension != null && SUBTITLE_FILE_EXTENSIONS.contains(extension.toLowerCase()));
  }
  
  public static String parseExtension(String fileName) {
    int index = fileName.lastIndexOf(".");
    if(index > 0) {
      return fileName.substring(index+1).toLowerCase();
    }
    return null;
  }

  public static String trimExtension(String fileName) {
    int index = fileName.lastIndexOf(".");
    if(index > 0) {
      return fileName.substring(0, index);
    }
    return fileName;
  }

  public static String parseFileName(String uri) {
    String[] splitUri = splitUri(uri);
    return splitUri[splitUri.length-1];
  }

  private static String[] splitUri(String uri) {
    return uri.replace('\\', '/').split("/");
  }

  /*
   * Kodi's naming convention.  uri[0] is the category, uri[1] represents the title
   */
  public static MovieId parseMovieFromUri(String uri) {
    String[] splitUri = splitUri(uri);
    String title = null;
    if(splitUri.length == 2) {
      title = trimExtension(splitUri[1]);
    }
    else if(splitUri.length > 2){
      title = splitUri[1];
    }
    
    Integer year = parseYear(title);
    if(year != null) {
      int yearStartIndex = title.lastIndexOf("(" + year);
      title = title.substring(0, yearStartIndex);
    }
    
    return new MovieId(TitleUtil.cleanse(title), year);
  }

  /*
   * Kodi's naming convention.  uri[0] is the category, uri[1] is series name, uri[uri.length-1] is the file
   * TV Shows <<Source folder, Content: TV shows>>
   |----TV Show 1
   |       |----Season #
   |            |--Files
   |----TV Show 2 (year)
   |       |--Files
   |----TV Show 3
   |       |----2008
   |            |--Files
   */
  public static SeriesId parseSeriesFromUri(String uri) {
    String[] splitUri = splitUri(uri);
    String title = null;
    if(splitUri.length > 2) {
      title = splitUri[1];
    }
    
    Integer year = parseYear(title);
    if(year != null) {
      int yearStartIndex = title.lastIndexOf("(" + year);
      title = title.substring(0, yearStartIndex);
    }

    return new SeriesId(TitleUtil.cleanse(title), year);
  }

  public static EpisodeId parseEpisodeFromUri(String uri) {
    SeriesId seriesId = parseSeriesFromUri(uri);
    if(seriesId != null) {
      String s = null;
      for(Pattern pattern : EPISODE_PATTERNS) {
        Matcher matcher = pattern.matcher(parseFileName(uri.toLowerCase()));
        if(matcher.find()) {
          s = matcher.group();
          break;
        }
      }
      if(s != null) {
        int episodeDigits = 0;
        while(Character.isDigit(s.charAt(s.length()-(episodeDigits+1)))) {
          episodeDigits++;
        }
        
        Integer episodeNumber = new Integer(s.substring(s.length()-episodeDigits));
        s = s.substring(0, s.length()-episodeDigits);
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(s);
        if(matcher.find()) {
          Integer seasonNumber = new Integer(matcher.group());
          return new EpisodeId(seriesId, seasonNumber, episodeNumber);
        }
      }
    }
    return null;
  }

  public static VideoId parseVideoFromUri(String uri) {
    String title = FileNameUtil.trimExtension(FileNameUtil.parseFileName(uri));
    return new VideoId(TitleUtil.cleanse(title.trim()));
  }

  public static Integer parseYear(String str) {
    Pattern pattern = Pattern.compile("[(][0-9]{4}.*[)]");
    Matcher matcher = pattern.matcher(str);
    if(matcher.find()) {
      try {
        return new Integer(matcher.group().substring(1, 5));
      }
      catch(NumberFormatException e) {

      }
    }
    return null;
  }
}

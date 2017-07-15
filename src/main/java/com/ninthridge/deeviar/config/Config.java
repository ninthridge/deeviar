package com.ninthridge.deeviar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.ninthridge.deeviar.util.FileUtil;

@Repository("config")
public class Config {

  protected final Log log = LogFactory.getLog(getClass());

  private static String STREAM_STATION_COMMAND = "transcode.station.command";
  private static String SINGLE_IMAGE_EXTRACTION_COMMAND = "image.single.extraction.command";
  private static String MULTIPLE_IMAGE_EXTRACTION_COMMAND = "image.multiple.extraction.command";
  private static String BIF_CREATION_COMMAND = "bif.creation.command";
  private static String FFPROBE_COMMAND = "ffprobe.command";

  private static String FFMPEG_LOG = "ffmpeg.log";

  private File configDir;
  private File dataDir;
  private File profilesDir;
  private File configFile;
  private File adminFile;
  private File tmpDir;
  private File imagesDir;
  private File profileImagesDir;
  private File stationImagesDir;
  private File subtitlesDir;
  private File streamsDir;
  private File videosDir;
  private File bifsDir;
  
  private String host;
  private Integer startingTunerPort;

  public Config() {
    configDir = new File(System.getProperty("configDir"));
    
    configFile = new File(configDir, "deeviar.config");
    adminFile = new File(configDir, "admin.properties");
    
    dataDir = new File(configDir, "data");
    
    profilesDir = new File(dataDir, "profiles");
    
    tmpDir = new File(configDir, "tmp" + File.separator);
    
    subtitlesDir = new File(configDir, "subtitles" + File.separator);
    
    streamsDir = new File(configDir, "streams" + File.separator);
    
    videosDir = new File(configDir, "videos" + File.separator);
    
    bifsDir = new File(configDir, "bifs" + File.separator);
    
    imagesDir = new File(configDir, "images" + File.separator);
    profileImagesDir = new File(imagesDir, "profiles" + File.separator);
    stationImagesDir = new File(imagesDir, "stations" + File.separator);

    if (!configDir.exists()) {
      configDir.mkdir();
    }

    if (!dataDir.exists()) {
      dataDir.mkdir();
    }

    if (!profilesDir.exists()) {
      dataDir.mkdir();
    }

    if (!streamsDir.exists()) {
      streamsDir.mkdir();
    } else {
      FileUtil.deleteAll(streamsDir);
    }

    if (!subtitlesDir.exists()) {
      subtitlesDir.mkdir();
    }

    if (!videosDir.exists()) {
      videosDir.mkdir();
    }

    if (!bifsDir.exists()) {
      bifsDir.mkdir();
    }

    if (!tmpDir.exists()) {
      tmpDir.mkdir();
    } else {
      FileUtil.deleteAll(tmpDir);
    }

    if (!imagesDir.exists()) {
      imagesDir.mkdir();
    }

    if (!profileImagesDir.exists()) {
      profileImagesDir.mkdir();
    }

    if (!stationImagesDir.exists()) {
      stationImagesDir.mkdir();
    }

    if (!configFile.exists()) {
      Properties properties = new Properties();

      properties.setProperty(STREAM_STATION_COMMAND,
          "ffmpeg -i ${input} -analyzeduration 2000000 -force_key_frames expr:gte(t,n_forced*2) -vcodec libx264 -acodec aac -ac 2 -maxrate 8M -bufsize 8M -hls_time 2 -hls_wrap 7200 -hls_list_size 0 -preset superfast -loglevel quiet -y -strict -2 ${output}");

      properties.setProperty(SINGLE_IMAGE_EXTRACTION_COMMAND, "ffmpeg -ss ${second} -i ${input} -t 1 ${target}");

      properties.setProperty(MULTIPLE_IMAGE_EXTRACTION_COMMAND,
          "ffmpeg -i ${input} -r .1 -s ${width}x${height} -loglevel quiet -y ${target}/%08d.jpg");

      properties.setProperty(BIF_CREATION_COMMAND, "biftool -t 10000 ${target}");

      properties.setProperty(FFPROBE_COMMAND,
          "ffprobe -v quiet -print_format json -show_format -show_streams ${input}");

      FileUtil.writePropertiesFile(configFile, properties, "");
    }

    if (!adminFile.exists()) {
      Properties properties = new Properties();

      properties.setProperty("admin", "password");

      FileUtil.writePropertiesFile(adminFile, properties, "");
    }
  }

  public File getConfigDir() {
    return configDir;
  }
  
  public File getConfigFile() {
    return configFile;
  }

  public File getAdminFile() {
    return adminFile;
  }

  public File getDataDir() {
    return dataDir;
  }
  
  public File getProfilesDir() {
    return profilesDir;
  }
  
  public File getImagesDir() {
    return imagesDir;
  }

  public File getProfileImagesDir() {
    return profileImagesDir;
  }

  public File getStationImagesDir() {
    return stationImagesDir;
  }

  public File getStreamsDir() {
    return streamsDir;
  }

  public File getSubtitlesDir() {
    return subtitlesDir;
  }

  public File getVideosDir() {
    return videosDir;
  }

  public File getBifsDir() {
    return bifsDir;
  }

  public File getTmpDir() {
    return tmpDir;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getStartingTunerPort() {
    return startingTunerPort;
  }

  public void setStartingTunerPort(Integer startingTunerPort) {
    this.startingTunerPort = startingTunerPort;
  }

  public String getStreamStationCommand() {
    return FileUtil.readPropertiesFile(configFile).getProperty(STREAM_STATION_COMMAND);
  }

  public String getSingleImageExtractionCommand() {
    return FileUtil.readPropertiesFile(configFile).getProperty(SINGLE_IMAGE_EXTRACTION_COMMAND);
  }

  public String getMultipleImageExtractionCommand() {
    return FileUtil.readPropertiesFile(configFile).getProperty(MULTIPLE_IMAGE_EXTRACTION_COMMAND);
  }

  public String getBifCreationCommand() {
    return FileUtil.readPropertiesFile(configFile).getProperty(BIF_CREATION_COMMAND);
  }

  public String getFfprobeCommand() {
    return FileUtil.readPropertiesFile(configFile).getProperty(FFPROBE_COMMAND);
  }

  public Map<String, String> getAdminCredentials() {
    Properties properties = FileUtil.readPropertiesFile(adminFile);
    Map<String, String> adminCredentials = new HashMap<>();
    for (final String name : properties.stringPropertyNames()) {
      adminCredentials.put(name, properties.getProperty(name));
    }
    return adminCredentials;
  }
  
  public boolean ffmpegLog() {
    String ffmpegLogStr = FileUtil.readPropertiesFile(configFile).getProperty(FFMPEG_LOG);
    if (ffmpegLogStr != null) {
      try {
        return new Boolean(ffmpegLogStr);
      } catch (Exception e) {
        log.error("Invalid property value: " + FFMPEG_LOG + "=" + ffmpegLogStr);
      }
    }
    return false;
  }
}

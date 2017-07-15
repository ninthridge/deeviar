package com.ninthridge.deeviar.transcoder.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutorFactory;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.medianalyzer.MediaAnalyzer;
import com.ninthridge.deeviar.medianalyzer.model.MediaInfo;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Stream;
import com.ninthridge.deeviar.model.StreamContent;
import com.ninthridge.deeviar.model.Tuner;
import com.ninthridge.deeviar.service.StreamService;
import com.ninthridge.deeviar.transcoder.Transcoder;
import com.ninthridge.deeviar.util.CmdUtil;
import com.ninthridge.deeviar.util.FileNameUtil;
import com.ninthridge.deeviar.util.FileUtil;
import com.ninthridge.deeviar.util.IdUtil;

@Service("transcoder")
public class FfmpegTranscoder implements Transcoder {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private MediaAnalyzer mediaAnalyzer;
  
  @Autowired
  private StreamService streamService;
  
  @Autowired
  private OperatingSystemCmdExecutorFactory operatingSystemCmdExecutorFactory;
  
  public Stream start(Tuner tuner, String stationId) {
    //TODO multiple output streams for supporting different types of clients
    Stream stream = null;
    try {
      stream = new Stream();
      stream.setTuner(tuner);
      stream.setStationId(stationId);
      stream.setStarted(new Date());
      stream.setStreamFormat("hls");
      stream.setId(IdUtil.id(stream)); //TODO: this might not be unique enough, maybe use a UUID?
      
      File dir = streamService.getDir(stream);
      if(dir.exists()) {
        FileUtil.deleteAll(dir);
      }
      else {
        dir.mkdirs();
      }
      
      File file = streamService.getFile(stream);
      
      List<StreamContent> streams = new ArrayList<>();
      StreamContent streamContent = new StreamContent();
      streamContent.setUri("/streams/" + stream.getId() + "/" + file.getName());
      streamContent.setQuality(true);
      streamContent.setBitRate(0); // adaptive bitrate
      streams.add(streamContent);
      stream.setStreams(streams);
      
      Map<String, String> params = new HashMap<String, String>();
      params.put("input", stream.input());
      params.put("output", file.getCanonicalPath());

      Process process = CmdUtil.execute(config.getStreamStationCommand(), params, null, config.getTmpDir());
      stream.setProcess(process);
    } catch (IOException e) {
      log.error("Unable to start transcoder", e);
    }
    return stream;
  }
  
  public void transcode(MediaProcessingItem mediaProcessingItem, File output) {
    try {
      File destinationFile = new File(output.getCanonicalPath());
      if(!destinationFile.exists()) {
        if(!destinationFile.getParentFile().exists()) {
          destinationFile.getParentFile().mkdirs();
        }
        
        File tmpDir = config.getTmpDir();
        File tmpFile = new File(tmpDir, destinationFile.getName());
        Map<String, String> params = new HashMap<String, String>();
        params.put("output", tmpFile.getCanonicalPath());
        
        String inputFileExtension = FileNameUtil.parseExtension(mediaProcessingItem.getCanonicalPath());
        String outputFileExtension = FileNameUtil.parseExtension(output.getName());
        
        MediaInfo mediaInfo = null;
        String cmd = "ffmpeg";
        
        File tsFile = null;
        if("m3u8".equals(inputFileExtension)) {
          tsFile = new File(tmpDir, FileNameUtil.trimExtension(FileNameUtil.parseFileName(mediaProcessingItem.getCanonicalPath())) + "_" + new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date()) + ".ts");
          
          convertM3u8File(mediaProcessingItem, tsFile);
          
          if(!tsFile.exists()) {
            log.error("Unable to create " + tsFile.getCanonicalPath() + " from " + mediaProcessingItem);
            //TODO throw an exception here?
            return;
          }
          mediaInfo = mediaAnalyzer.analyze(tsFile.getCanonicalPath());
          
          params.put("input", tsFile.getCanonicalPath());
        }
        else {
          mediaInfo = mediaAnalyzer.analyze(mediaProcessingItem.getCanonicalPath());
          
          if(mediaProcessingItem.getStart() != null && mediaProcessingItem.getStart() > 0) {
            cmd += " -ss " + mediaProcessingItem.getStart();
          }
          if(mediaProcessingItem.getDuration() != null && mediaProcessingItem.getDuration() > 0) {
            cmd += " -t " + mediaProcessingItem.getDuration();
          }

          params.put("input", mediaProcessingItem.getCanonicalPath());
        }
        
        log.info(mediaInfo);
        
        cmd += " -i ${input}";
        
        if(mediaProcessingItem.getCompress() || !supportedVideo(mediaInfo)) {
          cmd += " -vcodec libx264 ";
        }
        else {
          cmd += " -vcodec copy";
        }
        
        if(!supportedAudio(mediaInfo)) {
          cmd += " -acodec aac -ac 2";
        }
        else {
          cmd += " -acodec copy";
        }
        
        //TODO: there are probably more conditions where this applies
        if("mp4".equals(outputFileExtension) && (("m3u8".equals(inputFileExtension) || "ts".equals(inputFileExtension)))) {
          cmd += " -bsf:a aac_adtstoasc";
        }
        
        if(mediaProcessingItem.getCompress()) {
          cmd += " -preset medium";
        }
        else {
          cmd += " -preset superfast";
        }
        
        if(config.ffmpegLog()) {
          cmd += " -report";
        }
        else {
          cmd += " -loglevel quiet";
        }
        
        cmd += " -y -strict -2 ${output}";
        
        Process process = CmdUtil.execute(cmd, params, null, config.getTmpDir());
        process.waitFor();

        Files.move(tmpFile.toPath(), destinationFile.toPath(), REPLACE_EXISTING);
        
        if(tsFile != null && tsFile.exists()) {
          tsFile.delete();
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Unexpected exception transcoding video " + mediaProcessingItem.getCanonicalPath(), e);
    }
  }
  
  protected void convertM3u8File(MediaProcessingItem mediaProcessingItem, File output) {
    //TODO: add smb support
    List<M3u8Segment> segments = getM3u8Segments(new File(mediaProcessingItem.getCanonicalPath()));
    
    Double seconds = 0.0;
    Double duration = 0.0;
    for(Iterator<M3u8Segment> it = segments.iterator(); it.hasNext(); ) {
      M3u8Segment segment = (M3u8Segment)it.next();
      if((mediaProcessingItem.getStart() == null || seconds >= mediaProcessingItem.getStart()) && (mediaProcessingItem.getDuration() == null || duration < mediaProcessingItem.getDuration())) {
        duration += segment.getDuration();
      }
      else {
        it.remove();
      }
      seconds += segment.getDuration();
    }
      
    if(!segments.isEmpty()) {
      try {
        concatSegments(segments, output);
      }
      catch (IOException e) {
        log.error(e, e);
      }
    } 
    else {
      log.warn("Unable to create " + output + " from " + mediaProcessingItem);
    }
  }
  
  protected List<M3u8Segment> getM3u8Segments(File file) {
    List<M3u8Segment> segments = new ArrayList<>();
    if(file.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line = null;
        while((line = reader.readLine()) != null) {
          if(line.startsWith("#EXTINF:")) {
            Double duration = new Double(line.split(":")[1].split(",")[0]);
            String segmentFileName = reader.readLine();
            if(segmentFileName != null) {
              segments.add(new M3u8Segment(new File(file.getParentFile(), FileNameUtil.parseFileName(segmentFileName)), duration));
            }
          }
        }
      } catch (IOException e) {
        log.error(e, e);
      }
    }
    return segments;
  }
  
  protected void concatSegments(List<M3u8Segment> segments, File outputFile) throws IOException {
    try (FileOutputStream out = new FileOutputStream(outputFile)) {
      for(M3u8Segment segment : segments) {
        try (FileInputStream in = new FileInputStream(segment.getFile())) {
          byte[] buffer = new byte[4096];
          int l = 0;
          while ((l = in.read(buffer)) != -1) {
            out.write(buffer, 0, l);
          }
        }
      }
    }
  }
  
  protected boolean supportedVideo(MediaInfo mediaInfo) {
    if(mediaInfo != null && mediaInfo.getStreams() != null) {
      for(com.ninthridge.deeviar.medianalyzer.model.Stream stream : mediaInfo.getStreams()) {
        if("video".equals(stream.getCodecType()) && "h264".equals(stream.getCodecName())) {
          return true;
        }
      }
    }
    return false;
  }
  
  protected boolean supportedAudio(MediaInfo mediaInfo) {
    if(mediaInfo != null && mediaInfo.getStreams() != null) {
      for(com.ninthridge.deeviar.medianalyzer.model.Stream stream : mediaInfo.getStreams()) {
        if("audio".equals(stream.getCodecType()) && "aac".equals(stream.getCodecName()) && stream.getChannels() == 2) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void stop(Stream stream) {
    Process p = stream.getProcess();
    if(p != null) {
      
      log.info("Killing " + stream);
      stream.getProcess().destroy();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.error(e, e);
      }
      
      try {
        operatingSystemCmdExecutorFactory.getExecutor().killProcesses(stream.getId());
      } catch (IOException e) {
        log.error(e, e);
      }
    }
  }

  protected String format(long seconds) {
    long s = seconds % 60;
    long m = (seconds / 60) % 60;
    long h = (seconds / (60 * 60)) % 24;
    return String.format("%d:%02d:%02d", h,m,s);
  }
  
  class M3u8Segment {
    private File file;
    private Double duration;
    
    public M3u8Segment(File file, Double duration) {
      this.file = file;
      this.duration = duration;
    }
    public File getFile() {
      return file;
    }
    public Double getDuration() {
      return duration;
    }
    
  }
}
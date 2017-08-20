package com.ninthridge.deeviar.util;

import org.junit.Assert;
import org.junit.Test;

import com.ninthridge.deeviar.model.id.EpisodeId;
import com.ninthridge.deeviar.model.id.MovieId;
import com.ninthridge.deeviar.model.id.SeriesId;
import com.ninthridge.deeviar.model.id.VideoId;


public class FileNameUtilTest {

  @Test
  public void testIsVideoFile() {
    Assert.assertTrue(FileNameUtil.isVideoFile("file.mkv"));
    Assert.assertFalse(FileNameUtil.isVideoFile("file.jpg"));
    Assert.assertFalse(FileNameUtil.isVideoFile("file.srt"));
    Assert.assertFalse(FileNameUtil.isVideoFile("file.txt"));
  }
  
  @Test
  public void testIsImageFile() {
    Assert.assertFalse(FileNameUtil.isImageFile("file.mkv"));
    Assert.assertTrue(FileNameUtil.isImageFile("file.jpg"));
    Assert.assertFalse(FileNameUtil.isImageFile("file.srt"));
    Assert.assertFalse(FileNameUtil.isImageFile("file.txt"));
  }
  
  @Test
  public void testIsSubtitleTrackFile() {
    Assert.assertFalse(FileNameUtil.isSubtitleTrackFile("file.mkv"));
    Assert.assertFalse(FileNameUtil.isSubtitleTrackFile("file.jpg"));
    Assert.assertTrue(FileNameUtil.isSubtitleTrackFile("file.srt"));
    Assert.assertFalse(FileNameUtil.isSubtitleTrackFile("file.txt"));
  }
  
  @Test
  public void testParseExtension() {
    String uri = "/Homeland (2011)/Homeland.S06E11.720p.HDTV.H265-MRSK.mkv";
    Assert.assertEquals("mkv", FileNameUtil.parseExtension(uri));
  }

  @Test
  public void testTrimExtension() {
    String uri = "/Homeland (2011)/Homeland.S06E11.720p.HDTV.H265-MRSK.mkv";
    Assert.assertEquals("/Homeland (2011)/Homeland.S06E11.720p.HDTV.H265-MRSK", FileNameUtil.trimExtension(uri));
  }

  @Test
  public void testParseFileName() {
    String uri = "/Homeland (2011)/Homeland.S06E11.720p.HDTV.H265-MRSK.mkv";
    Assert.assertEquals("Homeland.S06E11.720p.HDTV.H265-MRSK.mkv", FileNameUtil.parseFileName(uri));
  }

  @Test
  public void testParseMovieFromUri() {
    String uri = "/Avatar (2009).mkv";
    MovieId movieId = FileNameUtil.parseMovieFromUri(uri);
    Assert.assertEquals("M19728746172009", movieId.getId());
    Assert.assertEquals("Avatar", movieId.getTitle());
    Assert.assertEquals(new Integer(2009), movieId.getYear());
  }

  @Test
  public void testParseEpisodeFromUri1() {
    String uri = "/Homeland (2011)/Homeland.S06E11.720p.HDTV.H265-MRSK.mkv";
    EpisodeId episodeId = FileNameUtil.parseEpisodeFromUri(uri);
    Assert.assertEquals("S4208467422011S0006E0011", episodeId.getId());
    Assert.assertEquals(new Integer(6), episodeId.getSeason());
    Assert.assertEquals(new Integer(11), episodeId.getEpisode());
    SeriesId seriesId = episodeId.getSeriesId();
    Assert.assertEquals("S4208467422011", seriesId.getId());
    Assert.assertEquals("Homeland", seriesId.getTitle());
    Assert.assertEquals(new Integer(2011), seriesId.getYear());
    Assert.assertTrue(seriesId.getId().endsWith("2011"));
  }

  public void testParseEpisodeFromUri2() {
    String uri = "/Homeland/Homeland.S06E11.720p.HDTV.H265-MRSK.mkv";
    EpisodeId episodeId = FileNameUtil.parseEpisodeFromUri(uri);
    Assert.assertEquals("S42084674S0006E0011", episodeId.getId());
    Assert.assertEquals(new Integer(6), episodeId.getSeason());
    Assert.assertEquals(new Integer(11), episodeId.getEpisode());
    SeriesId seriesId = episodeId.getSeriesId();
    Assert.assertEquals("S420846742", seriesId.getId());
    Assert.assertEquals("Homeland", seriesId.getTitle());
    Assert.assertNull(seriesId.getYear());
  }
  
  public void testParseEpisodeFromUri3() {
    String uri = "/Homeland (2011)/Homeland (2011) - S06E11.mkv";
    EpisodeId episodeId = FileNameUtil.parseEpisodeFromUri(uri);
    Assert.assertEquals("S4208467422011S0006E0011", episodeId.getId());
    Assert.assertEquals(new Integer(6), episodeId.getSeason());
    Assert.assertEquals(new Integer(11), episodeId.getEpisode());
    SeriesId seriesId = episodeId.getSeriesId();
    Assert.assertEquals("S4208467422011", seriesId.getId());
    Assert.assertEquals("Homeland", seriesId.getTitle());
    Assert.assertEquals(new Integer(2011), seriesId.getYear());
    Assert.assertTrue(seriesId.getId().endsWith("2011"));
  }
  
  @Test
  public void testParseVideoFromUri() {
    String uri = "/Blah.mkv";
    VideoId videoId = FileNameUtil.parseVideoFromUri(uri);
    Assert.assertEquals("V2073105", videoId.getId());
    Assert.assertEquals("Blah", videoId.getTitle());
  }

  @Test
  public void testParseYear() {
    String uri = "/Homeland (2011)/Homeland.S06E11.720p.HDTV.H265-MRSK.mkv";
    Assert.assertEquals(new Integer(2011), FileNameUtil.parseYear(uri));
  }
}

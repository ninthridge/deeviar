package com.ninthridge.deeviar.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.ProfilePermission;
import com.ninthridge.deeviar.repository.ProfileRepository;
import com.ninthridge.deeviar.util.FileNameUtil;
import com.ninthridge.deeviar.util.ImageUtil;
import com.ninthridge.deeviar.util.ImageUtil.VALIGN;

@Service("profileService")
public class ProfileService {

  protected final Log log = LogFactory.getLog(getClass());

  public static int PROFILE_IMAGE_WIDTH_HD = 600;
  public static int PROFILE_IMAGE_HEIGHT_HD = 600;

  @Autowired
  private Config config;

  @Autowired
  private ProfileRepository profileRepository;

  @Autowired
  private LineupService lineupService;

  public Set<Profile> findAll() {
    Set<Profile> profiles = profileRepository.getAll();
    if(profiles == null) {
      profiles = new HashSet<>();
    }
    return profiles;
  }

  public Profile find(String profileTitle) {
    for (Profile profile : findAll()) {
      if (profileTitle.equals(profile.getTitle())) {
        return profile;
      }
    }
    return null;
  }

  public Profile findByToken(String token) {
    for (Profile profile : findAll()) {
      if (token.equals(profile.getToken())) {
        return profile;
      }
    }
    return null;
  }

  public Set<Profile> findEnabled() {
    Set<Profile> validProfiles = new HashSet<>();
    for (Profile profile : findAll()) {
      if (profile.getEnabled() == null || profile.getEnabled()) {
        validProfiles.add(profile);
      }
    }
    return validProfiles;
  }

  public void saveProfile(Profile profile) throws IOException {
    Profile p = find(profile.getTitle());
    if (p != null) {
      if (profile.getLibraryImportLocations() != null) {
        p.setLibraryImportLocations(profile.getLibraryImportLocations());
      }
      if (profile.getPermissions() != null) {
        p.setPermissions(profile.getPermissions());
      }
      if (profile.getPin() != null) {
        String pin = profile.getPin();
        if (pin.length() > 0) {
          // we're hashing client side, but it should probably happen server side
          // pin = DigestUtils.sha256Hex(profile.getPin());
        }
        p.setPin(profile.getPin());
      }
      if (profile.getHdPosterUri() != null && !profile.getHdPosterUri().equals("")
          && !profile.getHdPosterUri().startsWith("/")) {
        BufferedImage image = ImageIO.read(new URL(profile.getHdPosterUri()));
        if (image != null) {
          saveProfileImage(p, image, FileNameUtil.parseExtension(profile.getHdPosterUri()));
        }
      }
      if(profile.getEnabled() == null) {
        profile.setEnabled(true);
      }
      profile = p;
    } else {
      setDefaults(profile);
    }
    profileRepository.save(profile.getTitle(), profile);
    lineupService.refreshProfileLineups();
  }

  public void deleteProfile(String profileTitle) {
    profileRepository.delete(profileTitle);
    lineupService.refreshProfileLineups();
  }

  public void saveProfileImage(Profile profile, BufferedImage image, String extension) throws IOException {
    setProfileImage(profile, image, extension);
    profileRepository.save(profile.getTitle(), profile);
  }

  protected void setProfileImage(Profile profile, BufferedImage image, String extension) throws IOException {
    if (profile.getHdPosterUri() != null) {
      File imageFileHd = profileImageFile(profile.getHdPosterUri());
      if (imageFileHd.exists()) {
        imageFileHd.delete();
      }
    }

    if (profile.getSdPosterUri() != null) {
      File imageFileSd = profileImageFile(profile.getSdPosterUri());
      if (imageFileSd.exists()) {
        imageFileSd.delete();
      }
    }

    if (image == null) {
      image = ImageUtil.createTextImage(Arrays.asList(profile.getTitle()), PROFILE_IMAGE_WIDTH_HD,
          PROFILE_IMAGE_HEIGHT_HD, Color.getHSBColor(new Float(Math.random()), 1f, .45f), Color.WHITE,
          new Font("Arial", Font.BOLD, 40), VALIGN.CENTER);
      extension = "png";
    }

    String fileName = profile.getTitle() + "." + extension.toLowerCase();
    String uri = "/images/profiles/" + fileName;
    File imageFileHD = profileImageFile(uri);
    ImageUtil.saveImage(image, imageFileHD);

    profile.setHdPosterUri(uri);
    profile.setSdPosterUri(uri);
  }

  protected File profileImageFile(String uri) {
    return new File(config.getImagesDir().getParentFile(), uri);
  }

  protected void setDefaults(Profile profile) {
    if (profile.getLibraryImportLocations() == null) {
      profile.setLibraryImportLocations(new HashMap<String, Set<String>>());
    }
    if (profile.getPermissions() == null) {
      profile.setPermissions(new HashSet<ProfilePermission>());
    }
    if (profile.getToken() == null) {
      profile.setToken(new BigInteger(130, new Random()).toString(32));
    }
    if (profile.getPin() == null) {
      profile.setPin("");
    }
    if (profile.getEnabled() == null) {
      profile.setEnabled(true);
    }

    File hdProfileImageFile = null;
    if (profile.getHdPosterUri() != null) {
      hdProfileImageFile = profileImageFile(profile.getHdPosterUri());
    }
    if (hdProfileImageFile == null || !hdProfileImageFile.exists()) {
      BufferedImage image = ImageUtil.createTextImage(Arrays.asList(profile.getTitle()), PROFILE_IMAGE_WIDTH_HD,
          PROFILE_IMAGE_HEIGHT_HD, Color.getHSBColor(new Float(Math.random()), 1f, .45f), Color.WHITE,
          new Font("Arial", Font.BOLD, 40), VALIGN.CENTER);
      try {
        setProfileImage(profile, image, "png");
      } catch (IOException e) {
        log.error(e, e);
      }
    }

    File sdProfileImageFile = null;
    if (profile.getSdPosterUri() != null) {
      sdProfileImageFile = profileImageFile(profile.getSdPosterUri());
    }
    if (sdProfileImageFile == null || !sdProfileImageFile.exists()) {
      profile.setSdPosterUri(profile.getHdPosterUri());
    }
  }
}
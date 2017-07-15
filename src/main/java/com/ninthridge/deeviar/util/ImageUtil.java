package com.ninthridge.deeviar.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageUtil {

  public static enum VALIGN {TOP, CENTER, BOTTOM}
  
  public static BufferedImage scaleDownAndCropImage(BufferedImage image, Integer width, Integer height) {
    int w = image.getWidth();
    int h = image.getHeight();
    if (w > width && h > height) {
      if (new Double(w) / new Double(width) > new Double(h) / new Double(height)) {
        image = convert(image.getScaledInstance(-1, height, Image.SCALE_DEFAULT), null);
      } else {
        image = convert(image.getScaledInstance(width, -1, Image.SCALE_DEFAULT), null);
      }
      w = image.getWidth(null);
      h = image.getHeight(null);
    }
    
    // crop
    if(w > width || h > height) {
      int x=0;
      if(w > width) {
        x = (w - width)/2;
        w = width;
      }
      
      int y=0;
      if(h > height) {
        y = (h - height)/2;
        h = height;
      }
      
      image = image.getSubimage(x, y, w, h);
    }

    return image;
  }

  public static BufferedImage scaleDownAndPadImage(BufferedImage image, Integer width, Integer height, Color bgColor) {
    int w = image.getWidth();
    int h = image.getHeight();
    if (w > width || h > height) {
      if (new Double(w) / new Double(width) > new Double(h) / new Double(height)) {
        image = convert(image.getScaledInstance(width, -1, Image.SCALE_DEFAULT), bgColor);
      } else {
        image = convert(image.getScaledInstance(-1, height, Image.SCALE_DEFAULT), bgColor);
      }
      w = image.getWidth(null);
      h = image.getHeight(null);
    }

    if(bgColor == null) {
      return image;
    }
    else {
      // pad
      BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = newImage.createGraphics();
      g.setColor(bgColor);
      g.fillRect(0, 0, width, height);
      
      int x = (width / 2) - (w / 2);
      int y = (height / 2) - (h / 2);
      g.drawImage(image, x, y, null);
      g.dispose();
      return newImage;
    }
  }

  public static BufferedImage convert(Image image, Color bgColor) {
    int width = image.getWidth(null);
    int height = image.getHeight(null);
    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = newImage.createGraphics();
    if(bgColor != null) {
      g.drawImage(image, 0, 0, bgColor, null);
    }
    else {
      g.drawImage(image, 0, 0, null);
    }
    g.dispose();
    return newImage;
  }

  public static void saveImage(BufferedImage image, File file) throws IOException {
    ImageIO.write(image, FileNameUtil.parseExtension(file.getName()), file);
  }
  
  public static BufferedImage createTextImage(List<String> lines, int width, int height, Color bgColor, Color fontColor, Font font, VALIGN valign) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    if(bgColor != null) {
      Graphics2D g = image.createGraphics();
      g.setColor(bgColor);
      g.fillRect(0,0,width,height);
      g.dispose();
    }
    
    return addText(image, lines, fontColor, font, valign);
  }
  
  public static BufferedImage addText(BufferedImage image, List<String> lines, Color fontColor, Font font, VALIGN valign) {
    int width = image.getWidth();
    int height = image.getHeight();
    
    Graphics2D g = image.createGraphics();
    g.setColor(fontColor);
    g.setFont(font);
    
    FontMetrics fm = g.getFontMetrics(font);
    
    double lineWidth = width*.9;
    
    // if a single word is larger than the allowed width, try again with a smaller font size
    for(String line : lines) {
      for(String word : line.split(" ")) {
        int wordWidth = fm.stringWidth(word);
        if(wordWidth > lineWidth) {
          int newFontSize = font.getSize()-1;
          if(newFontSize < 10) {
            return image;
          }
          else {
            return addText(image, lines, fontColor, new Font(font.getName(), font.getStyle(), newFontSize), valign);
          }
        }
      }
    }
    
    double border = (width-lineWidth)/2.0;
    
    List<String> splitLines = splitLines(lines, fm, lineWidth);
    
    int lineHeight = fm.getHeight();
    int ascent = fm.getAscent();
    
    for(String line : splitLines) {
      if(fm.stringWidth(line) > lineWidth) {
        //apparently there is a single word that is too big.  try again with a smaller font size
        int newFontSize = font.getSize()-1;
        if(newFontSize < 10) {
          return null;
        }
        return addText(image, lines, fontColor, new Font(font.getName(), newFontSize, font.getSize()-1), valign);
      }
    }
    
    for(int i=0; i<splitLines.size(); i++) {
      String line = splitLines.get(i);
      int stringWidth = fm.stringWidth(line);
      int x = (width - stringWidth) / 2;
      int y = 0;
      if(valign.equals(VALIGN.CENTER)) {
        y = ((height - (lineHeight * splitLines.size())) / 2) + (lineHeight * i) + ascent;
      }
      else if(valign.equals(VALIGN.BOTTOM)) {
        y = new Double(height - border).intValue() - ((splitLines.size()-(i+1)) * lineHeight);
      }
      else if(valign.equals(VALIGN.TOP)) {
        y = new Double(border).intValue() + ((i+1) * lineHeight);
      }
      g.drawString(line, x, y);
    }
    
    g.dispose();
    
    return image;
  }
  
  private static List<String> splitLines(List<String> lines, FontMetrics fm, double width) {
    List<String> splitLines = new ArrayList<>();
    for(String line : lines) {
      if(fm.stringWidth(line) > width) {
        splitLines.addAll(createLines(Arrays.asList(line.split(" ")), fm, width));
      }
      else {
        splitLines.add(line);
      }
    }
    return splitLines;
  }
  
  private static List<String> createLines(List<String> words, FontMetrics fm, double width) {
    String line = words.get(0);
    for(int i=1; i<words.size(); i++) {
      if(fm.stringWidth(line + " " + words.get(i)) > width) {
        List<String> lines = createLines(words.subList(i, words.size()), fm, width);
        lines.add(0, line);
        return lines;
      }
      else {
        line += " " + words.get(i);
      }
    }
    List<String> lines = new ArrayList<>();
    lines.add(line);
    return lines;
  }
}

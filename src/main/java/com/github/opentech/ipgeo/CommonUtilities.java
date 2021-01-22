package com.github.opentech.ipgeo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utilities
 * 
 * @author bhargava.kulkarni
 */
public class CommonUtilities {

  private static final Logger LOG = LoggerFactory.getLogger(CommonUtilities.class);

  private CommonUtilities() {
  }
  
  /**
   * Count number of lines in specified file.
   * 
   * @param filename
   * @return numberOfLines
   * @throws IOException
   */
  public static int countLinesInLocalFile(String filename) throws IOException {
    int numberOfLines = 0;
    String line;
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
      while ((line = bufferedReader.readLine()) != null) {
        numberOfLines++;
      }
    }
    return numberOfLines;
  }

  /**
   * Returns host name on which program is running.
   * 
   * @return hostname
   */
  public static String getHostname() {

    String hostname = "unknown";
    try {
      Process process = Runtime.getRuntime().exec("hostname");
      int exitStatus = process.waitFor();
      if (exitStatus == 0) {
        hostname = readInputStreamAsString(process.getInputStream()).trim();
      }
    } catch (Exception e) {
      LOG.error("Error while retrieving hostname {}", e.getMessage());
    }
    return hostname;
  }

  /**
   * Reads input stream & returns result
   * 
   * @param inputStream
   * @return output
   * @throws IOException
   */
  public static String readInputStreamAsString(InputStream inputStream) throws IOException {
    BufferedInputStream input = new BufferedInputStream(inputStream);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    int result = input.read();
    while (result != -1) {
      byte b = (byte) result;
      output.write(b);
      result = input.read();
    }
    return output.toString();
  }
}


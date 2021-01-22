package com.github.opentech.ipgeo;

/**
 * Lookup constants
 * 
 * @author bhargava.kulkarni
 */
public class LookupConstants {
  
  private LookupConstants() {
  }
  
  public static byte NEW_LINE = 0x0A;
  public static final String DATA_FILE_NAME = "data";
  public static final String INDEX_FILE_NAME = "index";
  public static final String METADATA_FILE_NAME = "metadata";
  public static String COLON = ":";
  public static String CREATED_BY = "created_by";
  public static String CREATED_AT = "created_at";
  public static String ALLOCATED_SIZE = "allocated_size";
  public static String STRIDE_LENGTH = "stride_length";
  public static String ROW_SIZE = "row_size";
  public static String LINES_PER_DATABLOCK = "lines_per_datablock";
  public static String TOTAL_DATABLOCK_LINES = "total_datablock_lines";
  public static String DATABLOCK_OFFSET_BITS = "datablock_offset_bits";
}

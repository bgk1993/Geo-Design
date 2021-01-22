package com.github.opentech.ipgeo;

/**
 * Parameters needed to initialize DataBlocks
 * 
 * @author bhargava.kulkarni
 */
public class DataBlockLookupInitArgs {

  /**
   * File name
   */
  private String filename;
  
  /**
   * Schema definition associated with file
   */
  private Schema schema;
  
  /**
   * Size of Data Block row expressed in bytes.
   */
  private int dataBlockRowSize;
  
  /**
   * Number of bits used for representing offset in each Data Block.
   */
  private int dataBlockOffsetBits;
  
  /**
   * Total number of lines in file
   */
  private int totalLines;
  
  /**
   * Number of lines in one data block
   */
  private int linesPerDataBlock;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public int getDataBlockRowSize() {
    return dataBlockRowSize;
  }

  public void setDataBlockRowSize(int dataBlockRowSize) {
    this.dataBlockRowSize = dataBlockRowSize;
  }

  public int getDataBlockOffsetBits() {
    return dataBlockOffsetBits;
  }

  public void setDataBlockOffsetBits(int dataBlockOffsetBits) {
    this.dataBlockOffsetBits = dataBlockOffsetBits;
  }

  public int getTotalLines() {
    return totalLines;
  }

  public void setTotalLines(int totalLines) {
    this.totalLines = totalLines;
  }

  public int getLinesPerDataBlock() {
    return linesPerDataBlock;
  }

  public void setLinesPerDataBlock(int linesPerDataBlock) {
    this.linesPerDataBlock = linesPerDataBlock;
  }
}

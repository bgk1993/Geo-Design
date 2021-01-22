package com.github.opentech.ipgeo;

/**
 * DataBlock initialization arguments
 * 
 * @author bhargava.kulkarni
 */
public class DataBlockInitArgs {
  
  /**
   * Block identifier.
   */
  private int blockNo;
  
  /**
   * Starting position within file expressed in bytes.
   */
  private long position;
  
  /**
   * Size of the file block expressed in bytes.
   */
  private int size;

  public DataBlockInitArgs(int blockNo, long position, int size) {
    this.blockNo = blockNo;
    this.position = position;
    this.size = size;
  }
  
  public int getBlockNo() {
    return blockNo;
  }

  public void setBlockNo(int blockNo) {
    this.blockNo = blockNo;
  }

  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public String toString() {
    return "DataBlockInitArgs [blockNo=" + blockNo + ", position=" + position + ", size=" + size
        + "]";
  }
}

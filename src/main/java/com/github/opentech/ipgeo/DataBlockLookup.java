package com.github.opentech.ipgeo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Efficient fixed length record lookup on large persistent Data.
 * 
 * @author bhargava.kulkarni
 *
 */
public class DataBlockLookup {

  /**
   * DataBlock lookup initialization variables.
   */
  private DataBlockLookupInitArgs dataBlockLookupInitArgs;

  /**
   * Data record processor
   */
  private DataRecordProcessor dataRecordProcessor;

  /**
   * Memory mapped DataBlocks.Since JAVA has limitation for maximum size of memory mapped file to
   * 2GB, single large file is mapped multiple times as data blocks.
   */
  private Map<Integer, DataBlock> memoryMappedDataBlocks;

  /**
   * Constructor for the class.
   * 
   * @param dataBlockLookupInitArgs Data block lookup arguments.
   */
  public DataBlockLookup(DataBlockLookupInitArgs dataBlockLookupInitArgs) {
    this.dataBlockLookupInitArgs = dataBlockLookupInitArgs;
    this.dataRecordProcessor = new DataRecordProcessor(this.dataBlockLookupInitArgs.getSchema());
    this.memoryMappedDataBlocks = new HashMap<Integer, DataBlock>();
    try {
      init();
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to initialise datablock lookup on "
          + this.dataBlockLookupInitArgs.getFilename() + " Reason: " + e.getMessage());
    }
  }

  /**
   * Initialize internal structures
   */
  public void init() throws IOException {
    int blockNo = 0;
    int blockSize = this.dataBlockLookupInitArgs.getLinesPerDataBlock()
        * this.dataBlockLookupInitArgs.getDataBlockRowSize(), size = 0;
    long totalSize = Long.valueOf(this.dataBlockLookupInitArgs.getTotalLines())
        * this.dataBlockLookupInitArgs.getDataBlockRowSize(), position = 0;

    for (long currentSize = 0; currentSize < totalSize; currentSize += blockSize) {
      position = currentSize;
      size = (int) Math.min((totalSize - currentSize), blockSize);
      if (size > 0) {
        DataBlockInitArgs dataBlockInitArgs = new DataBlockInitArgs(blockNo++, position, size);
        DataBlock dataBlock = new DataBlock(this.dataBlockLookupInitArgs, dataBlockInitArgs);
        memoryMappedDataBlocks.put(dataBlock.getBlockNo(), dataBlock);
      }
    }
  }

  /**
   * Select record from index.
   * index = BlockNo + Offset
   * Offset = line no within block
   * 
   * @param index search index
   * @return DataRecord selected record
   */
  public DataRecord selectRecord(int index) {
    int blockNo = (index >>> this.dataBlockLookupInitArgs.getDataBlockOffsetBits());
    int lineNo =
        (index & (0xFFFFFFFF >>> (32 - this.dataBlockLookupInitArgs.getDataBlockOffsetBits())));
    DataBlock dataBlock = this.memoryMappedDataBlocks.get(blockNo);
    if (dataBlock != null) {
      return this.dataRecordProcessor.readRecord(dataBlock, lineNo);
    } else {
      return null;
    }
  }

  /**
   * @return Size of data blocks
   */
  public long getSize() {
    return this.memoryMappedDataBlocks.entrySet().stream().map(e -> e.getValue().getSize())
        .collect(Collectors.summingLong(Integer::intValue));
  }
  
  /**
   * Un-initialize Data Block Lookup
   * 
   * @throws Exception
   */
  public void uninit() throws IOException {
    for (DataBlock datablock : memoryMappedDataBlocks.values()) {
      datablock.uninit();
    }
  }
}

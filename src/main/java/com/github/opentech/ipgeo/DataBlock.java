package com.github.opentech.ipgeo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * DataBock is a logical portion of the memory mapped with physical file portion located on the
 * disk.
 * 
 * @author bhargava.kulkarni
 */
public class DataBlock {

  /**
   * Name of the file.
   */
  private String filename;

  /**
   * Schema associated with data block.
   */
  private Schema schema;

  /**
   * DataBlock init parameters
   */
  private DataBlockInitArgs dataBlockInitArgs;

  /**
   * Reference to file used for creating memory block
   */
  private RandomAccessFile dataReader;

  /**
   * Memory mapped byte buffer.
   */
  private MappedByteBuffer mappedByteBuffer;

  /**
   * Variable that holds active record reads per thread execution context.
   */
  private ThreadLocal<DataRecord> dataRecord;

  /**
   * Constructor for the class.
   * 
   * @param dataBlockLookupInitArgs name of file
   * @param dataBlockInitArgs schema definition
   */
  public DataBlock(final DataBlockLookupInitArgs dataBlockLookupInitArgs,
      final DataBlockInitArgs dataBlockInitArgs) {
    this.filename = dataBlockLookupInitArgs.getFilename();
    this.schema = dataBlockLookupInitArgs.getSchema();
    this.dataBlockInitArgs = dataBlockInitArgs;
    this.dataRecord = new ThreadLocal<DataRecord>();
    try {
      init(dataBlockLookupInitArgs);
    } catch (Throwable e) {
      throw new IllegalStateException(
          "Failed to initialise datablock on file " + this.filename + " Reason: " + e.getMessage());
    }
  }

  /**
   * Initialize Data Block
   * 
   * @param dataBlockLookupInitArgs
   * @throws IOException
   */
  public void init(final DataBlockLookupInitArgs dataBlockLookupInitArgs) throws IOException {
    this.dataReader = new RandomAccessFile(this.filename, "r");
    this.mappedByteBuffer = dataReader.getChannel().map(FileChannel.MapMode.READ_ONLY,
        this.dataBlockInitArgs.getPosition(), this.dataBlockInitArgs.getSize());
    this.mappedByteBuffer.load();
    /**
     * Validate new Data Block.
     * Each record must be separated by newline character.
     */
    byte firstLineSeparator =
        this.mappedByteBuffer.get(dataBlockLookupInitArgs.getDataBlockRowSize() - 1);
    byte lastLineSeparator = this.mappedByteBuffer.get(this.dataBlockInitArgs.getSize() - 1);
    if (firstLineSeparator != LookupConstants.NEW_LINE
        || lastLineSeparator != LookupConstants.NEW_LINE) {
      throw new IllegalStateException("Invalid datablock: " + this.dataBlockInitArgs);
    }
  }

  /**
   * Un-Initialize Data Block
   * 
   * @throws IOException
   */
  @SuppressWarnings("restriction")
  public void uninit() throws IOException {
    /**
     * DirectByteBuffers are garbage collected by using a phantom reference and a
     * reference queue. Every once a while, the JVM checks the reference queue and
     * cleans the DirectByteBuffers. However, as this doesn't happen
     * immediately after discarding all references to a DirectByteBuffer, it's
     * possible to get OutOfMemoryError using DirectByteBuffers. This function
     * explicitly calls the Cleaner method of a DirectByteBuffer.
     */
    sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) this.mappedByteBuffer).cleaner();
    if (cleaner != null) {
      cleaner.clean();
    }
    this.dataReader.getChannel().close();
    this.dataReader.close();
  }

  /**
   * Get portion of memory bytes starting at offset until length bytes
   * 
   * @param offset
   * @param length
   * @return DataRecord record from given offset
   */
  public DataRecord get(int offset, int length) {
    DataRecord record = null;
    if (offset < this.dataBlockInitArgs.getSize()) {
      record = this.dataRecord.get();
      if (record == null) {
        this.dataRecord.set(new DataRecord(this.schema, length));
        record = this.dataRecord.get();
      }
      record.clear();
      this.mappedByteBuffer.position(offset);
      record.read(this.mappedByteBuffer, 0, length);
    }
    return record;
  }

  /**
   * Get Block Number
   * 
   * @return blockNo
   */
  public int getBlockNo() {
    return this.dataBlockInitArgs.getBlockNo();
  }
  
  /**
   * Get Block Size
   * 
   * @return block size
   */
  public int getSize() {
    return this.dataBlockInitArgs.getSize();
  }
}

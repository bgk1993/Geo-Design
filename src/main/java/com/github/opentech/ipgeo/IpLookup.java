package com.github.opentech.ipgeo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.maltalex.ineter.range.IPv4Range;
import com.github.maltalex.ineter.range.IPv4Subnet;

/**
 * IP lookup implementation.
 * 
 * @author bhargava.kulkarni
 */
public class IpLookup {

  /**
   * Logger for the class
   */
  private static final Logger LOG = LoggerFactory.getLogger(IpLookup.class);

  /**
   * Size of IPv4 address
   */
  private static int IPV4_SIZE = 32;

  /**
   * IP-GEO lookup DB base path when persisted.
   */
  private String basepath;

  /**
   * Create time of IP lookup structure.
   */
  private ZonedDateTime createTime;
  
  /**
   * Internal structure which holds IP Address indexing data.
   */
  public BitmapTrie lookupTree = new BitmapTrie();

  /**
   * Internal structure which holds data blocks.
   */
  private DataBlockLookup dataBlockLookup;

  /**
   * Buffer used for converting IP string to long.
   * Kept as an optimization to avoid frequent memory allocations in fast path.
   */
  private ThreadLocal<ByteBuffer> byteBuffer = new ThreadLocal<ByteBuffer>();

  /**
   * Flag to indicate IP lookup structure has initialized properly or not.
   */
  private boolean initialised = false;

  /**
   * Parameter to indicate maximum no of lines allowed in single lookup block.
   */
  private int linesPerDataBlock = 10000000;

  /**
   * Initializes IPv4 tree with default capacity of 1024 nodes. It should
   * be sufficient for small data.
   */
  public IpLookup() {
    init(1024);
    initialised = true;
  }

  /**
   * Initializes IPv4 tree with a given capacity.
   * 
   * @param allocatedSize initial capacity to allocate
   */
  public IpLookup(int allocatedSize) {
    init(allocatedSize);
    initialised = true;
  }

  /**
   * Initializes IPv4 tree with a given persisted path.
   * 
   * @param basepath
   * @param schema
   */
  public IpLookup(String basepath, Schema schema) {
    try {
      recover(basepath, schema);
      this.basepath = basepath;
      this.initialised = true;
    } catch (Exception e) {
      initialised = false;
      LOG.error("Failed to initialise IP lookup {}", e.getMessage());
    }
  }

  /**
   * Initialize IP lookup internal structures.
   * 
   * @param allocatedSize
   */
  private void init(int allocatedSize) {
    this.lookupTree.init(allocatedSize);
    this.createTime = ZonedDateTime.now();
  }

  /**
   * Un-initialize IP lookup internal structures.
   * 
   * @param clean Flag to indicate clear DB content.
   * 
   * @throws Exception
   */
  public void uninit(boolean clean) {
    try {
      this.dataBlockLookup.uninit();
      if (clean && this.basepath != null) {
        FileUtils.cleanDirectory(new File(this.basepath));
      }
    } catch (Exception e) {
      LOG.error("Failed to uninitialize IP lookup {}", e.getMessage());
    }
  }

  /**
   * Puts a key-value pair in a trie, using a string representation of IPv4 prefix.
   * 
   * @param ipSubnet IPv4 network as a string in form of "e.f.g.h/m", where e, f, g, h
   *        are IPv4 octets (in decimal) and "m" is a net mask in CIDR notation
   * @param value an arbitrary value that would be stored under a given key
   * @return true on successful add
   * @throws UnknownHostException
   */
  public boolean add(String ipSubnet, int value) throws UnknownHostException {
    int pos = ipSubnet.indexOf('/');
    String ipStr = ipSubnet.substring(0, pos);
    long ip = inet_aton(ipStr);

    String netmaskStr = ipSubnet.substring(pos + 1);
    int cidr = Integer.parseInt(netmaskStr);
    if (cidr > IPV4_SIZE) {
      return false;
    }
    long netmask = ((1L << (32 - cidr)) - 1L) ^ 0xffffffffL;
    return this.lookupTree.add(ip, netmask, value);
  }

  /**
   * Selects a value for a given IPv4 address, traversing trie and choosing
   * most specific value available for a given address.
   * 
   * @param key IPv4 address to look up, in string form (i.e. "e.f.g.h")
   * @return value at most specific IPv4 network in a tree for a given IPv4
   *         address
   */
  public DataRecord match(String ipAddress) {
    DataRecord dataRecord = null;
    try {
      int value = this.lookupTree.match(inet_aton(ipAddress));
      if (value != BitmapTrie.NO_VALUE) {
        dataRecord = this.dataBlockLookup.selectRecord(value);
      }
    } catch (Throwable e) {
      LOG.error("Error while matching for IP Address {}", ipAddress);
    }
    return dataRecord;
  }

  /**
   * Check IP lookup is initialized properly or not.
   * 
   * @return initialized - true if IP lookup structure is initialized properly.
   */
  public boolean isInitialised() {
    return this.initialised;
  }

  /**
   * Recover IP lookup internal structures from persistent storage.
   * 
   * @param basepath
   * @param schema
   * @throws Exception
   */
  private void recover(String basepath, Schema schema) throws Exception {

    Map<String, String> metadata = new HashMap<String, String>();
    BufferedReader metadataReader = new BufferedReader(
        new FileReader(basepath + File.separator + LookupConstants.METADATA_FILE_NAME));
    String line = null;
    while ((line = metadataReader.readLine()) != null) {
      String[] keyValue = line.split(LookupConstants.COLON, 2);
      metadata.put(keyValue[0].trim(), keyValue[1].trim());
    }
    metadataReader.close();
    
    this.createTime = ZonedDateTime.parse(metadata.get(LookupConstants.CREATED_AT));
    int allocatedSize = Integer.parseInt(metadata.get(LookupConstants.ALLOCATED_SIZE));
    this.lookupTree.recover(basepath + File.separator + LookupConstants.INDEX_FILE_NAME,
        allocatedSize);
    if (this.lookupTree.getSize() != allocatedSize) {
      throw new IllegalStateException("Unable to initialise IP Address Index");
    }

    DataBlockLookupInitArgs dataBlockLookupInitArgs = new DataBlockLookupInitArgs();
    dataBlockLookupInitArgs.setFilename(basepath + File.separator + LookupConstants.DATA_FILE_NAME);
    dataBlockLookupInitArgs.setSchema(schema);
    int rowSize = Integer.parseInt(metadata.get(LookupConstants.ROW_SIZE));
    int totalDatablockLines = Integer.parseInt(metadata.get(LookupConstants.TOTAL_DATABLOCK_LINES));
    
    dataBlockLookupInitArgs.setDataBlockOffsetBits(
        Integer.parseInt(metadata.get(LookupConstants.DATABLOCK_OFFSET_BITS)));
    dataBlockLookupInitArgs.setDataBlockRowSize(rowSize);
    dataBlockLookupInitArgs.setTotalLines(totalDatablockLines);
    dataBlockLookupInitArgs
        .setLinesPerDataBlock(Integer.parseInt(metadata.get(LookupConstants.LINES_PER_DATABLOCK)));
    this.dataBlockLookup = new DataBlockLookup(dataBlockLookupInitArgs);
    long totalSize = Long.valueOf(totalDatablockLines) * rowSize;
    if (this.dataBlockLookup.getSize() != totalSize) {
      throw new IllegalStateException("Unable to initialise DataBlock Lookup");
    }
  }

  /**
   * Persist IP lookup internal structures to disk. This case is designed keeping in mind
   * scenario where there is less frequent update to existing IP lookup structures but more memory
   * efficient & time efficient lookup operations are needed, especially when the size of lookup
   * data is really really huge to keep it entirely in memory.
   * 
   * @param source
   * @param target
   * @param schema
   * @throws Exception
   */
  public void persist(final String source, final String target, final Schema schema)
      throws Exception {

    int linesCount = CommonUtilities.countLinesInLocalFile(source);
    FileUtils.deleteQuietly(new File(target));
    Files.createDirectories(Paths.get(target));

    /**
     * Determine number of bits needed to represent Data Block & Offset Block
     * These values are dependent on the number of lines present in source file
     * Default value is 1M, which means for a source file with lines 4M,
     * total 4 Data Blocks are created & 2 bits are needed to represent 4 data blocks
     * Remaining (32-2)=30 bits are used to represent offset within Data block.
     */
    BufferedReader bufferedReader = new BufferedReader(new FileReader(source));
    RandomAccessFile dataOutputWriter =
        new RandomAccessFile(target + File.separator + LookupConstants.DATA_FILE_NAME, "rw");
    int maxNoOfBlocks = (linesCount / this.getLinesPerDataBlock())
        + ((linesCount % this.getLinesPerDataBlock()) == 0 ? 0 : 1);
    int offsetBits = Integer.numberOfLeadingZeros(Math.max(maxNoOfBlocks - 1, 1));

    String line = null;
    int blockNo = 0, lineNo = 0;
    int datalinesCount = 0;
    DataRecordProcessor dataRecordProcessor = new DataRecordProcessor(schema);

    /**
     * Process each record in source file, create indices for each IP range.
     * First column in source is assumed to have IP range / sub-net details.
     * Records for which validation fails or error occurs are skipped.
     */
    while ((line = bufferedReader.readLine()) != null) {
      String[] keyValuesPair = line.split("\t", 2);
      String[] values = keyValuesPair[1].split("\t");

      if (dataRecordProcessor.validate(values)) {

        IPv4Range ipv4Range = null;
        List<IPv4Subnet> ipv4subnets = null;
        try {
          ipv4Range = IPv4Range.parse(keyValuesPair[0]);
          ipv4subnets = ipv4Range.toSubnets();
        } catch (Exception e) {
          LOG.error("Unable to persist IP address {} details", e.getMessage());
          continue;
        }

        /**
         * Writing values to the persistent location & updating record index
         * in IP prefix Tree MUST be atomic.
         */
        if (dataRecordProcessor.writeRecord(dataOutputWriter, values)) {

          int index = (blockNo << offsetBits) | lineNo;
          int subnetsCount = 0;
          for (IPv4Subnet ipv4subnet : ipv4subnets) {
            if (this.add(ipv4subnet.toString(), index)) {
              subnetsCount++;
            }
          }
          if (subnetsCount != ipv4subnets.size()) {
            bufferedReader.close();
            dataOutputWriter.close();
            throw new IllegalStateException("Unable to add all subnets to the IP lookup structure");
          }
          lineNo++;
          if (lineNo % this.getLinesPerDataBlock() == 0) {
            lineNo = 0;
            blockNo++;
          }
          datalinesCount++;
        }
      } else {
        LOG.error("Invalid record found while persisting : {}", line);
      }
    }

    bufferedReader.close();
    dataOutputWriter.close();

    this.lookupTree.persist(target + File.separator + LookupConstants.INDEX_FILE_NAME);

    /**
     * Write metadata used for recovery of IP lookup data.
     */
    BufferedWriter metadataOutputWriter = new BufferedWriter(
        new FileWriter(target + File.separator + LookupConstants.METADATA_FILE_NAME));
    persistMetadata(metadataOutputWriter, LookupConstants.CREATED_BY, CommonUtilities.getHostname(),
        true);
    persistMetadata(metadataOutputWriter, LookupConstants.CREATED_AT,
        ZonedDateTime.now(ZoneOffset.UTC).toString(), true);
    persistMetadata(metadataOutputWriter, LookupConstants.ALLOCATED_SIZE,
        Integer.toString(this.lookupTree.getSize()), true);
    persistMetadata(metadataOutputWriter, LookupConstants.ROW_SIZE,
        Integer.toString(dataRecordProcessor.getRowSize()), true);
    persistMetadata(metadataOutputWriter, LookupConstants.LINES_PER_DATABLOCK,
        Integer.toString(this.getLinesPerDataBlock()), true);
    persistMetadata(metadataOutputWriter, LookupConstants.TOTAL_DATABLOCK_LINES,
        Integer.toString(datalinesCount), true);
    persistMetadata(metadataOutputWriter, LookupConstants.DATABLOCK_OFFSET_BITS,
        Integer.toString(offsetBits), false);
    metadataOutputWriter.close();
  }

  /**
   * Validate IP lookup data against given source
   * An exception is thrown if validation fails.
   * 
   * @param source
   * @param schema
   * @throws Exception
   */
  public void validate(final String source, final Schema schema) throws Exception {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(source));
    DataRecordProcessor dataRecordProcessor = new DataRecordProcessor(schema);
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      String[] keyValuesPair = line.split("\t", 2);
      String[] values = keyValuesPair[1].split("\t");

      if (dataRecordProcessor.validate(values)) {
        IPv4Range ipv4Range = null;
        List<IPv4Subnet> ipv4subnets = null;
        try {
          ipv4Range = IPv4Range.parse(keyValuesPair[0]);
          ipv4subnets = ipv4Range.toSubnets();
        } catch (Exception e) {
          LOG.error("Unable to validate IP address {} details", e.getMessage());
          continue;
        }

        for (IPv4Subnet ipv4subnet : ipv4subnets) {
          boolean valid = true;
          DataRecord firstRecord = this.match(ipv4subnet.getFirst().toString());
          for (int i = 0; i < values.length; i++) {
            if (!values[i].equals(firstRecord.read(i))) {
              valid = false;
            }
          }
          DataRecord lastRecord = this.match(ipv4subnet.getLast().toString());
          for (int i = 0; i < values.length; i++) {
            if (!values[i].equals(lastRecord.read(i))) {
              valid = false;
            }
          }
          if (!valid) {
            bufferedReader.close();
            throw new IllegalStateException(
                "IP lookup validation failed for address " + firstRecord + " " + lastRecord);
          }
        }
      } else {
        LOG.error("Invalid record found while validating : {}", line);
      }
    }
    bufferedReader.close();
  }

  /**
   * Write metadata to file.
   * 
   * @param metadataWriter
   * @param key
   * @param value
   * @param newline
   * @throws IOException
   */
  private static void persistMetadata(BufferedWriter metadataWriter, String key, String value,
      boolean newline) throws IOException {
    metadataWriter.write(key + LookupConstants.COLON + value);
    if (newline) {
      metadataWriter.newLine();
    }
  }

  /**
   * Helper method to convert String representation of IP address to long representation.
   * 
   * @param ipAddress
   * @return Long representation of ipAddress
   * @throws UnknownHostException
   */
  private long inet_aton(String ipAddress) throws UnknownHostException {
    if (byteBuffer.get() == null) {
      byteBuffer.set(ByteBuffer.allocate(8));
    }
    ByteBuffer buffer = byteBuffer.get();
    buffer.clear();
    buffer.putInt(0);
    buffer.put(InetAddress.getByName(ipAddress).getAddress());
    buffer.rewind();
    return buffer.getLong();
  }

  /**
   * Get Create Time of IpLookup Database.
   * 
   * @param basepath
   * @return ZonedDateTime
   */
  public static ZonedDateTime getCreateTime(final String basepath) {
    ZonedDateTime dateTime = null;
    try {
      Map<String, String> metadata = new HashMap<String, String>();
      BufferedReader metadataReader = new BufferedReader(
          new FileReader(basepath + File.separator + LookupConstants.METADATA_FILE_NAME));
      String line = null;
      while ((line = metadataReader.readLine()) != null) {
        String[] keyValue = line.split(LookupConstants.COLON, 2);
        metadata.put(keyValue[0].trim(), keyValue[1].trim());
      }
      metadataReader.close();
      String createTime = metadata.get(LookupConstants.CREATED_AT);

      if (createTime != null) {
        dateTime = ZonedDateTime.parse(createTime);
      }
    } catch (Exception e) {
      dateTime = null;
    }
    return dateTime;
  }

  /**
   * Set number of lines per Data Block.
   * 
   * @param linesPerDataBlock
   */
  public void setLinesPerBlock(int linesPerDataBlock) {
    this.linesPerDataBlock = linesPerDataBlock;
  }

  /**
   * Get number of liner per Data Block.
   * 
   * @return linesPerDataBlock
   */
  public int getLinesPerDataBlock() {
    return this.linesPerDataBlock;
  }
  
  /**
   * Create time at which IP lookup structure is created.
   * 
   * @return createTime
   */
  public ZonedDateTime getCreateTime() {
    return this.createTime;
  }
}

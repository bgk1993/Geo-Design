package com.github.opentech.ipgeo;

import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * An implementation of Tree Data Structure using Arrays.
 * This structure consumes less memory since only 12 Bytes are consumed per node
 * in the tree. Provides faster lookup to the data due to continuous allocation
 * of memory blocks. Ideally suited for cases where data needs to be loaded into
 * memory only once at the beginning & time sensitive lookup operations are
 * needed at high frequencies. This structure also supports data persistence,
 * which means data can be persisted in an Optimized fashion & later it can be
 * recovered. This is specifically useful when faster application bootstrap is
 * required since instead of building entire structure in sequential order from
 * beginning, only data recovery is performed.
 */
public class BitmapTrie {

  /**
   * These values have special meaning in tree structure.
   */
  public static final int NO_VALUE = -1;
  public static final int NULL_PTR = -1;
  public static final int ROOT_PTR = 0;
  public static final long MAX_IPV4_BIT = 0x80000000L;

  /**
   * Right descendant nodes
   */
  private int[] rightNodes;

  /**
   * Left descendant nodes
   */
  private int[] leftNodes;

  /**
   * Value nodes
   */
  private int[] valueNodes;

  /**
   * Current size of internal structures
   */
  private int size;
  
  /**
   * Maximum available size
   */
  private int maxSize;

  /**
   * Maximum allocated size
   */
  private int allocatedSize;


  /**
   * Initialize internal tree structure
   * @param allocatedSize
   */
  public void init(int allocatedSize) {

    this.size = 1;
    this.allocatedSize = allocatedSize;
    this.maxSize = (Integer.MAX_VALUE-32);
    this.rightNodes = new int[this.allocatedSize];
    this.leftNodes = new int[this.allocatedSize];
    this.valueNodes = new int[this.allocatedSize];

    this.leftNodes[0] = NULL_PTR;
    this.rightNodes[0] = NULL_PTR;
    this.valueNodes[0] = NO_VALUE;
  }

  /**
   * Adds a key-value pair into tree.
   * @param key IPv4 network prefix
   * @param mask IPv4 net mask in networked byte order format
   * @param value an arbitrary value to be stored against given key
   * @return true on successful add
   */
  public boolean add(long key, long mask, int value) {

    if (this.size >= this.maxSize) {
      return false;
    }
    
    long bit = MAX_IPV4_BIT;
    int current = ROOT_PTR;
    int next = ROOT_PTR;

    while ((bit & mask) != 0) {
      next = ((key & bit) != 0) ? this.rightNodes[current] : this.leftNodes[current];
      if (next == NULL_PTR)
        break;
      bit >>= 1;
      current = next;
    }

    if (next != NULL_PTR) {
      this.valueNodes[current] = value;
      return true;
    }

    while ((bit & mask) != 0) {
      if (this.size == this.allocatedSize)
        this.expandAllocatedSize();

      next = this.size;
      this.valueNodes[next] = NO_VALUE;
      this.rightNodes[next] = NULL_PTR;
      this.leftNodes[next] = NULL_PTR;

      if ((key & bit) != 0) {
        this.rightNodes[current] = next;
      } else {
        this.leftNodes[current] = next;
      }

      bit >>= 1;
      current = next;
      this.size++;
    }
    this.valueNodes[current] = value;
    return true;
  }

  /**
   * Matches a value for a given IPv4 address, traversing trie and choosing most
   * specific value available for a given address.
   * @param key IPv4 address to look up
   * @return value at most specific IPv4 network in a tree for a given IPv4
   *         address
   */
  public int match(long key) {
    long bit = MAX_IPV4_BIT;
    int value = NO_VALUE;
    int node = ROOT_PTR;

    while (node != NULL_PTR) {
      if (this.valueNodes[node] != NO_VALUE)
        value = this.valueNodes[node];
      node = ((key & bit) != 0) ? this.rightNodes[node] : this.leftNodes[node];
      bit >>= 1;
    }

    return value;
  }

  /**
   * Doubles allocated memory size of internal structures. Existing values are
   * copied to new memory location.
   */
  public void expandAllocatedSize() {

    int oldAllocatedSize = this.allocatedSize;
    this.allocatedSize = this.allocatedSize * 2;

    if (this.allocatedSize < 0) {
      this.allocatedSize = Integer.MAX_VALUE;
    }
    
    int[] newLeftNodes = new int[allocatedSize];
    System.arraycopy(this.leftNodes, 0, newLeftNodes, 0, oldAllocatedSize);
    this.leftNodes = newLeftNodes;

    int[] newRightNodes = new int[allocatedSize];
    System.arraycopy(this.rightNodes, 0, newRightNodes, 0, oldAllocatedSize);
    this.rightNodes = newRightNodes;

    int[] newValueNodes = new int[allocatedSize];
    System.arraycopy(this.valueNodes, 0, newValueNodes, 0, oldAllocatedSize);
    this.valueNodes = newValueNodes;
  }

  /**
   * Persist internal structures to a file.
   * Data is persisted in the order, [value-nodes] -> [left-nodes] ->
   * [right-nodes]
   * @param filename
   */
  public void persist(String filename) throws Exception {

    RandomAccessFile indexOutputWriter = new RandomAccessFile(filename, "rw");
    FileChannel fileChannel = indexOutputWriter.getChannel();
    MappedByteBuffer mappedByteBuffer =
        fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 3 * Integer.BYTES * this.size);
    IntBuffer intBuffer = mappedByteBuffer.asIntBuffer();
    intBuffer.put(this.valueNodes, 0, this.size);
    intBuffer.put(this.leftNodes, 0, this.size);
    intBuffer.put(this.rightNodes, 0, this.size);
    mappedByteBuffer.force();
    indexOutputWriter.close();
  }

  /**
   * Recover internal structures from file.
   * Data is recovered in the order, [value-nodes] -> [left-nodes] ->
   * [right-nodes]
   * @param filename
   * @param allocatedSize
   * @throws Exception
   */
  public void recover(String filename, int allocatedSize) throws Exception {
    
    init(allocatedSize);
    this.size = allocatedSize;
    RandomAccessFile indexInputReader = new RandomAccessFile(filename, "r");
    FileChannel fileChannel = indexInputReader.getChannel();
    MappedByteBuffer mappedByteBuffer =
        fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 3 * Integer.BYTES * this.size);
    IntBuffer intBuffer = mappedByteBuffer.asIntBuffer();
    intBuffer.get(this.valueNodes, 0, this.size);
    intBuffer.get(this.leftNodes, 0, this.size);
    intBuffer.get(this.rightNodes, 0, this.size);
    mappedByteBuffer.force();
    indexInputReader.close();
  }

  /**
   * @return size
   */
  public int getSize() {
    return this.size;
  }
  
  /**
   * @param size
   */
  void setSize(int size) {
    this.size = size;
  }
  
  /**
   * @return allocatedSize
   */
  public int getAllocatedSize() {
    return this.allocatedSize;
  }
  
  /**
   * @param allocatedSize
   */
  void setAllocatedSize(int allocatedSize) {
    this.allocatedSize = allocatedSize;
  }
  
  /**
   * Returns string representation of trie.
   */
  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    result.append("\n values: \n");
    for (int i = 0; i < this.size; i++) {
      result.append(this.valueNodes[i] + ",");
    }
    result.append("\n leftNodes: \n");
    for (int i = 0; i < this.size; i++) {
      result.append(this.leftNodes[i] + ",");
    }
    result.append("\n rightNodes: \n");
    for (int i = 0; i < this.size; i++) {
      result.append(this.rightNodes[i] + ",");
    }
    return result.toString();
  }
}

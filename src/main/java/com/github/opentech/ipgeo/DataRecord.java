package com.github.opentech.ipgeo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.github.opentech.ipgeo.Schema.Column;
import com.github.opentech.ipgeo.Schema.Column.Datatype;

/**
 * Fixed size data record.
 * 
 * @author bhargava.kulkarni
 */
public class DataRecord {

  /**
   * Schema definition
   */
  private Schema schema;
  
  /**
   * Record size.
   */
  private int size;

  /**
   * Internal buffer for managing row content.
   */
  private ByteBuffer byteBuffer;

  /**
   * Constructor for the class
   * 
   * @param schema
   * @param size
   */
  public DataRecord(Schema schema, int size) {
    this.schema = schema;
    this.size = size;
    this.byteBuffer = ByteBuffer.allocate(size);
  }

  /**
   * Clears contents of row
   */
  public void clear() {
    this.byteBuffer.clear();
  }

  /**
   * Write column to buffer
   * 
   * @param byteBuffer
   * @param value
   * @param column
   */
  public void writeColumn(String value, Column column) {
    switch (column.getDatatype()) {
      case SHORT:
        byteBuffer.putShort(Short.parseShort(value));
        break;
      case INT:
        byteBuffer.putInt(Integer.parseInt(value));
        break;
      default:
        break;
    }
  }

  /**
   * Append given byte of data at current buffer pointer
   * 
   * @param data
   */
  public void append(byte data) {
    this.byteBuffer.put(data);
  }

  /**
   * @return check whether record is valid or not
   */
  public boolean isValid() {
    return (this.byteBuffer.get(this.size-1) == LookupConstants.NEW_LINE);
  }
  
  /**
   * Write row to file
   * 
   * @param outputWriter
   * @return
   */
  public boolean write(RandomAccessFile outputWriter) {
    boolean writeStatus = true;
    try {
      outputWriter.write(this.byteBuffer.array());
    } catch (IOException e) {
      writeStatus = false;
    }
    return writeStatus;
  }

  /**
   * Read Short value from given index as column value
   * 
   * @param index
   * @return Short value of column
   */
  public short readShort(int index) {
    return byteBuffer.getShort(this.schema.getColumn(index).getOffset());
  }

  /**
   * Read Integer value from given index as column value
   * 
   * @param index
   * @return Integer value of column
   */
  public int readInt(int index) {
    return byteBuffer.getInt(this.schema.getColumn(index).getOffset());
  }

  /**
   * Read record from source buffer at specified offset & length
   * 
   * @param sourceBuffer
   * @param offset
   * @param length
   */
  public void read(final ByteBuffer sourceBuffer, int offset, int length) {
    sourceBuffer.get(this.byteBuffer.array(), offset, length);
  }
  
  /**
   * Read value as String from given index as column value
   * 
   * @param index
   * @return Column value as string
   */
  public String read(int index) {
    String value = null;
    Column column = this.schema.getColumn(index);
    switch (column.getDatatype()) {
      case SHORT:
        value = String.valueOf(byteBuffer.getShort(column.getOffset()));
        break;
      case INT:
        value = String.valueOf(byteBuffer.getInt(column.getOffset()));
        break;
      default:
        break;
    }
    return value;
  }
  
  /**
   * Returns string representation of data record.
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < this.schema.getTotalColumns(); i++) {
      Column column = this.schema.getColumn(i);
      result.append("Column Name=" + column.getName());
      result.append(", Column datatype=" + column.getDatatype());
      result.append(", Column offset=" + column.getOffset());
      result.append(", Column value="
          + ((column.getDatatype().equals(Datatype.INT)) ? this.readInt(i) : this.readShort(i)));
      result.append("\n");
    }
    return result.toString();
  }
}

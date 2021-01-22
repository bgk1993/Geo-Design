package com.github.opentech.ipgeo;

import java.io.RandomAccessFile;

import com.github.opentech.ipgeo.Schema.Column;

/**
 * Data records processor designed to process record reads & writes
 * 
 * @author bhargava.kulkarni
 */
public class DataRecordProcessor {

  /**
   * Schema definition used for processing records.
   */
  private Schema schema;

  /**
   * Variable that holds active record writes per each thread execution context.
   */
  private ThreadLocal<DataRecord> dataRecord;

  /**
   * Constructor for the class
   * 
   * @param schema
   */
  public DataRecordProcessor(Schema schema) {
    this.schema = schema;
    this.dataRecord = new ThreadLocal<DataRecord>();
  }

  /**
   * Validate values in accordance with schema definition
   * 
   * @param values
   * @return true if validation succeeds, false otherwise
   */
  public boolean validate(String[] values) {
    Column[] columns = this.schema.getColumns();
    boolean valid = true;
    try {
      valid = valid && (values != null) && (values.length == this.schema.getTotalColumns());
      if (!valid) {
        return valid;
      }
      for (int i = 0; i < columns.length; i++) {
        switch (columns[i].getDatatype()) {
          case SHORT:
            Short.parseShort(values[i]);
            break;
          case INT:
            Integer.parseInt(values[i]);
            break;
          default:
            valid = false;
            break;
        }
      }
    } catch (NumberFormatException e) {
      valid = false;
    }
    return valid;
  }

  /**
   * Write values to file as single line.
   * 
   * @param outputWriter
   * @param values
   * @return writeStatus  true if record is written to file successfully,
   *         false otherwise
   */
  public boolean writeRecord(RandomAccessFile outputWriter, String[] values) {
    Column[] columns = this.schema.getColumns();
    if (this.dataRecord.get() == null) {
      this.dataRecord.set(new DataRecord(this.schema, getRowSize()));
    }
    DataRecord record = this.dataRecord.get();
    record.clear();
    for (int i = 0; i < columns.length; i++) {
      record.writeColumn(values[i], columns[i]);
    }
    record.append(LookupConstants.NEW_LINE);
    return record.write(outputWriter);
  }

  /**
   * Read record from data block starting at offset
   * 
   * @param datablock
   * @param lineNo
   * @return byteBuffer  record
   */
  public DataRecord readRecord(final DataBlock datablock, int lineNo) {
    int rowSize = getRowSize();
    DataRecord record = datablock.get(lineNo * rowSize, rowSize);
    if (record != null && record.isValid()) {
      return record;
    }
    return null;
  }

  /**
   * @return row size as per schema definition.
   */
  public int getRowSize() {
    return this.schema.getSize() + 1;
  }
}

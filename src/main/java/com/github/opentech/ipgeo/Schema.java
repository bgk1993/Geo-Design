package com.github.opentech.ipgeo;

import java.util.List;

/**
 * Schema Definition.
 */
public class Schema {

  /**
   * Columns which constitute schema
   */
  private Column[] columns;
  
  /**
   * Column Definition
   */
  public static class Column {
   
    private String name;
    private Datatype datatype;
    private String description;
    private int offset;
    
    /**
     * Constructor for the class
     * 
     * @param name
     * @param datatype
     * @param description
     */
    public Column(String name, Datatype datatype, String description) {
      this.name = name;
      this.datatype = datatype;
      this.description = description;
      this.offset = -1;
    }
    
    /**
     * Data types Definition.
     */
    public static enum Datatype {
      
      SHORT(Short.SIZE/8),
      INT(Integer.SIZE/8);
      
      int size;
      
      Datatype(int size) {
        this.size = size;
      }
      
      public int getSize() {
        return this.size;
      }
    }
    
    public void setOffset(int offset) {
      this.offset = offset;
    }
    
    public String getName() {
      return name;
    }

    public Datatype getDatatype() {
      return datatype;
    }

    public String getDescription() {
      return description;
    }
    
    public int getOffset() {
      return this.offset;
    }
  }
  
  /**
   * Constructor for the class.
   * 
   * @param columns
   */
  public Schema(List<Column> columns) {
    this.columns = columns.stream().toArray(Column[]::new);
    int columnOffset = 0;
    for (Column column : this.columns) {
      column.setOffset(columnOffset);
      columnOffset += column.getDatatype().getSize();
    }
  }
  
  /**
   * Get Schema size
   * 
   * @return size
   */
  public int getSize() {
    int size = 0;
    for (int i=0; i<columns.length; i++) {
      size += columns[i].getDatatype().getSize();
    }
    return size;
  }
  
  /**
   * @return no of schema columns
   */
  public int getTotalColumns() {
    return columns.length;
  }
  
  /**
   * @return columns
   */
  public Column[] getColumns() {
    return columns;
  }
  
  /**
   * Return column from specified index
   * 
   * @param index
   * @return columns
   */
  public Column getColumn(int index) {
    return columns[index];
  }
}

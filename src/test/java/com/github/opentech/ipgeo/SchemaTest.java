package com.github.opentech.ipgeo;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.opentech.ipgeo.Schema;
import com.github.opentech.ipgeo.Schema.Column;
import com.github.opentech.ipgeo.Schema.Column.Datatype;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for class Schema
 */
public class SchemaTest {

  @Test
  public void testConstructor_positive() {
    List<Column> columns = Arrays.asList(new Column("country_code", Datatype.SHORT, "Country Code"),
        new Column("city_code", Datatype.INT, "City Code"));
    Schema schema = new Schema(columns);

    assertEquals(6, schema.getSize());
    assertEquals(2, schema.getTotalColumns());
    assertEquals(columns.size(), schema.getColumns().length);
    assertEquals(columns.get(0), schema.getColumn(0));
    assertEquals(columns.get(1), schema.getColumn(1));
  }
}

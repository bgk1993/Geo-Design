package com.github.opentech.ipgeo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.opentech.ipgeo.DataRecord;
import com.github.opentech.ipgeo.Schema;
import com.github.opentech.ipgeo.Schema.Column;
import com.github.opentech.ipgeo.Schema.Column.Datatype;

/**
 * Test cases for class DataRecord
 */
public class DataRecordTest {
  @Test
  public void testConstructor_positive() {
    List<Column> columns = Arrays.asList(new Column("country_code", Datatype.SHORT, "Country Code"),
        new Column("city_code", Datatype.INT, "City Code"));
    Schema schema = new Schema(columns);
    DataRecord record = new DataRecord(schema, schema.getSize());
    assertTrue(record != null);
  }

  @Test
  public void testWriteReadeColumn() {
    List<Column> columns = Arrays.asList(new Column("country_code", Datatype.SHORT, "Country Code"),
        new Column("city_code", Datatype.INT, "City Code"));
    Schema schema = new Schema(columns);
    DataRecord record = new DataRecord(schema, schema.getSize());
    record.writeColumn("1", new Column("country_code", Datatype.SHORT, ""));
    record.writeColumn("99999999", new Column("city_code", Datatype.INT, ""));
    assertEquals(1, record.readShort(0));
    assertEquals(99999999, record.readInt(1));
    assertEquals("1", record.read(0));
    assertEquals("99999999", record.read(1));
    assertTrue(record.toString() != null);
  }

  @Test
  public void testWrite() throws IOException {
    List<Column> columns = Arrays.asList(new Column("country_code", Datatype.SHORT, "Country Code"),
        new Column("city_code", Datatype.INT, "City Code"));
    Schema schema = new Schema(columns);
    DataRecord record = new DataRecord(schema, schema.getSize());
    RandomAccessFile fileMock = Mockito.mock(RandomAccessFile.class);
    Mockito.doNothing().when(fileMock).write(Mockito.any());
    assertTrue(record.write(fileMock));
  }
}

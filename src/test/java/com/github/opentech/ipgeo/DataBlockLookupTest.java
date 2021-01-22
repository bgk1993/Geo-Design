package com.github.opentech.ipgeo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.opentech.ipgeo.DataBlockLookup;
import com.github.opentech.ipgeo.DataBlockLookupInitArgs;
import com.github.opentech.ipgeo.DataRecord;
import com.github.opentech.ipgeo.Schema;
import com.github.opentech.ipgeo.Schema.Column;
import com.github.opentech.ipgeo.Schema.Column.Datatype;

/**
 * Unit test cases for class DataBlockLookup
 * 
 * @author bhargava.kulkarni
 *
 */
public class DataBlockLookupTest {

  private static DataBlockLookup dataBlockLookup;
  
  private static Schema schema;
  
  @BeforeClass
  public static void setUp() throws IOException {
    List<Column> columns = Arrays.asList(new Column("country_code", Datatype.SHORT, ""),
        new Column("city_code", Datatype.INT, ""), new Column("post_code_id", Datatype.INT, ""),
        new Column("region_code", Datatype.INT, ""), new Column("sic_code", Datatype.INT, ""),
        new Column("isp_name_code", Datatype.INT, ""),
        new Column("homebiz_type_code", Datatype.SHORT, ""),
        new Column("naics_code", Datatype.INT, ""), new Column("cbsa_code", Datatype.INT, ""),
        new Column("csa_code", Datatype.SHORT, ""), new Column("md_code", Datatype.INT, ""),
        new Column("mcc", Datatype.SHORT, ""), new Column("mnc", Datatype.SHORT, ""),
        new Column("conn_speed_code", Datatype.SHORT, ""),
        new Column("org_name_code", Datatype.INT, ""),
        new Column("ip_start_int", Datatype.INT, ""));
    schema = new Schema(columns);
    DataBlockLookupInitArgs dataBlockLookupInitArgs = new DataBlockLookupInitArgs();
    dataBlockLookupInitArgs.setFilename("src/test/resources/ip_geo/data");
    dataBlockLookupInitArgs.setSchema(schema);
    dataBlockLookupInitArgs.setDataBlockOffsetBits(30);
    dataBlockLookupInitArgs.setDataBlockRowSize(53);
    dataBlockLookupInitArgs.setTotalLines(5);
    dataBlockLookupInitArgs.setLinesPerDataBlock(2);
    dataBlockLookup = new DataBlockLookup(dataBlockLookupInitArgs);
  }
  
  @AfterClass
  public static void cleanup() throws IOException {
    dataBlockLookup.uninit();
  }
  
  @Test
  public void testSelectRecord() throws IOException {
    assertNotNull(dataBlockLookup.selectRecord(0));
    assertNotNull(dataBlockLookup.selectRecord(1));
    assertNotNull(dataBlockLookup.selectRecord(1073741824));
    assertNotNull(dataBlockLookup.selectRecord(1073741825));
    DataRecord record = dataBlockLookup.selectRecord(-2147483648);
    assertNotNull(record);
    assertEquals(840, record.readShort(0));
    assertEquals(22281, record.readInt(1));
    assertEquals(-1, record.readInt(2));
    assertEquals(9203, record.readInt(3));
    assertEquals(518210, record.readInt(4));
    assertEquals(-1, record.readInt(5));
    assertEquals(-1, record.readShort(6));
    assertEquals(518210, record.readInt(7));
    assertEquals(0, record.readInt(8));
    assertEquals(0, record.readShort(9));
    assertEquals(0, record.readInt(10));
    assertEquals(0, record.readShort(11));
    assertEquals(0, record.readShort(12));
    assertEquals(-1, record.readShort(13));
    assertEquals(-1, record.readInt(14));
    assertEquals(2117219584, record.readInt(15));
    assertNull(dataBlockLookup.selectRecord(-2147483647));
    assertNull(dataBlockLookup.selectRecord(-1073741824));
  }
  
  @Test (expected = IllegalStateException.class)
  public void testInitDatablock_invalid() throws IOException {
    DataBlockLookupInitArgs dataBlockLookupInitArgs = new DataBlockLookupInitArgs();
    dataBlockLookupInitArgs.setFilename("src/test/resources/ip_geo/data");
    dataBlockLookupInitArgs.setSchema(schema);
    dataBlockLookupInitArgs.setDataBlockOffsetBits(30);
    // Row size is supposed to be 53 & not 52
    dataBlockLookupInitArgs.setDataBlockRowSize(52);
    dataBlockLookupInitArgs.setTotalLines(5);
    dataBlockLookupInitArgs.setLinesPerDataBlock(2);
    DataBlockLookup dataBlockLookup1 = new DataBlockLookup(dataBlockLookupInitArgs);
  }
}

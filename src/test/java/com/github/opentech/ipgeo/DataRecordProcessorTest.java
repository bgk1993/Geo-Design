package com.github.opentech.ipgeo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.opentech.ipgeo.DataBlock;
import com.github.opentech.ipgeo.DataRecord;
import com.github.opentech.ipgeo.DataRecordProcessor;
import com.github.opentech.ipgeo.LookupConstants;
import com.github.opentech.ipgeo.Schema;
import com.github.opentech.ipgeo.Schema.Column;
import com.github.opentech.ipgeo.Schema.Column.Datatype;

/**
 * Unit test cases for class DataRecordProcessor
 * 
 * @author bhargava.kulkarni
 *
 */
public class DataRecordProcessorTest {

  private static DataRecordProcessor dataRecordProcessor;

  private static Schema schema;
  
  private static String outputPath = "src/test/resources/ip_geo/ip_geo_process.out";

  @BeforeClass
  public static void setUp() {
    List<Column> columns = Arrays.asList(new Column("country_code", Datatype.SHORT, "Country Code"),
        new Column("city_code", Datatype.INT, "City Code"));
    schema = new Schema(columns);
    dataRecordProcessor = new DataRecordProcessor(schema);
  }
  
  @Test
  public void testValidate_integer() {
    assertTrue(dataRecordProcessor.validate(new String[] {"100", Integer.MAX_VALUE + ""}));
  }

  @Test
  public void testValidate_short() {
    assertTrue(dataRecordProcessor.validate(new String[] {Short.MAX_VALUE + "", "200"}));
  }

  @Test
  public void testValidate_numberformatexception() {
    assertFalse(dataRecordProcessor.validate(new String[] {"a", "b"}));
  }

  @Test
  public void testWriteRecord() throws IOException {
    File outputFile = new File(outputPath);
    if (!outputFile.exists()) {
      outputFile.createNewFile();
    }
    RandomAccessFile outputWriter = new RandomAccessFile(outputPath, "rw");
    assertTrue(dataRecordProcessor.writeRecord(outputWriter, new String[] {"1234", "5678"}));
    assertTrue(dataRecordProcessor.writeRecord(outputWriter, new String[] {"777", "987"}));
    assertEquals(14, outputWriter.length());
    outputWriter.close();
  }

  @Test
  public void testReadRecord_positive() {
    DataBlock dataBlock = Mockito.mock(DataBlock.class);
    DataRecord record = new DataRecord(schema, 1);
    record.append(LookupConstants.NEW_LINE);
    Mockito.when(dataBlock.get(Mockito.anyInt(), Mockito.anyInt())).thenReturn(record);
    assertNotNull(dataRecordProcessor.readRecord(dataBlock, 100));
  }

  @Test
  public void testReadRecord_nullrecord() {
    DataBlock dataBlock = Mockito.mock(DataBlock.class);
    DataRecord record = new DataRecord(schema, schema.getSize() + 1);
    record.append(LookupConstants.NEW_LINE);
    Mockito.when(dataBlock.get(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
    assertNull(dataRecordProcessor.readRecord(dataBlock, 100));
  }

  @Test
  public void testGetRowSize() {
    assertEquals(7, dataRecordProcessor.getRowSize());
  }
}

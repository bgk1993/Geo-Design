package com.github.opentech.ipgeo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.opentech.ipgeo.IpLookup;
import com.github.opentech.ipgeo.Schema;
import com.github.opentech.ipgeo.Schema.Column;
import com.github.opentech.ipgeo.Schema.Column.Datatype;

/**
 * Unit test cases for class IpLookup
 * 
 * @author bhargava.kulkarni
 */
public class IpLookupTest {

  @Test
  public void testConstructor() {
    IpLookup ipLookup = new IpLookup();
    assertTrue(ipLookup.isInitialised());
    ipLookup = new IpLookup(100);
    assertTrue(ipLookup.isInitialised());
  }

  @Test
  public void testAdd_positive() throws Exception {
    IpLookup ipLookup = new IpLookup();
    assertTrue(ipLookup.add("0.0.0.0/24", 1000));
    assertTrue(ipLookup.add("255.255.255.255/28", 2000));
    assertTrue(ipLookup.add("127.0.0.0/31", 3000));
    assertTrue(ipLookup.add("68.122.148.34/32", 4000));
  }

  @Test(expected = UnknownHostException.class)
  public void testAdd_throwexception() throws Exception {
    IpLookup ipLookup = new IpLookup();
    assertFalse(ipLookup.add("128.0.0.0.0/33", 1000));
    ipLookup.add("257.0.0.0/24", 1000);
  }

  /**
   * This unit test case covers all cases
   * 1. Create Schema Definition
   * 2. Persist Indexed Data for given source Data
   * 3. Validate Indexed Data
   * 4. Verify Indexed Data Initialization
   * 5. Verify Matching
   * 
   * NOTE: Source Data expects first entry to be IPv4 address range / sub net &
   * remaining columns are values mapped to IP address.
   * 
   * If the first column is not IPv4 address, then that address will not be
   * indexed & hence not available for lookup as well.
   * 
   * @throws Exception
   */
  @Test
  public void testPersistRecoverMatch_positive() throws Exception {
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
    Schema schema = new Schema(columns);

    IpLookup ipLookup = new IpLookup();
    ipLookup.setLinesPerBlock(2000);
    ipLookup.persist("src/test/resources/ip_geo/ip_geo_2020_11_01_000_1.txt",
        "src/test/resources/ip_geo/output", schema);
    ipLookup = new IpLookup("src/test/resources/ip_geo/output", schema);
    ipLookup.validate("src/test/resources/ip_geo/ip_geo_2020_11_01_000_1.txt", schema);
    assertTrue(ipLookup.isInitialised());
    assertTrue(ipLookup.match("254.50.53.255") != null);
    assertTrue(ipLookup.match("216.254.241.10") != null);
    assertTrue(ipLookup.match("0.0.0.255") == null);

    ipLookup.uninit(true);
    FileUtils.deleteQuietly(new File("src/test/resources/ip_geo/output/"));
  }
}

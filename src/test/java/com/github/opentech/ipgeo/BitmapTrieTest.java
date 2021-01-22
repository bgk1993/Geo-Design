package com.github.opentech.ipgeo;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.opentech.ipgeo.BitmapTrie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Unit test cases for class BitmapTrie
 */
public class BitmapTrieTest {

  @BeforeClass
  public static void setup() throws IOException {
    Files.createDirectories(Paths.get("src/test/resources/ipgeo"));
  }

  @AfterClass
  public static void cleanup() {
    FileUtils.deleteQuietly(new File("src/test/resources/ipgeo"));
  }

  @Test
  public void testInit_positive() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(8);
    assertEquals(1, bitmapTrie.getSize());
  }

  @Test
  public void testAdd_single() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=1.32.232.0/21, net mask=255.255.248.0
    boolean status = bitmapTrie.add(0x120E800, 0xFFFFF800, 1);
    assertTrue(status);
    assertEquals(22, bitmapTrie.getSize());
  }

  @Test
  public void testAdd_multiple() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=1.32.232.0/21, net mask=255.255.248.0
    boolean status = bitmapTrie.add(0x120E800, 0xFFFFF800, 2);
    // IP=2.17.131.0/24, net mask=255.255.255.0
    status = status && bitmapTrie.add(0x2118300, 0xFFFFFF00, 3);
    assertTrue(status);
    assertEquals(40, bitmapTrie.getSize());
  }

  @Test
  public void testAdd_commonPrefix() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=2.17.131.0/24, net mask=255.255.255.0
    boolean status = bitmapTrie.add(0x2118300, 0xFFFFFF00, 4);
    // IP=2.17.131.0/16, net mask=255.255.0.0
    status = status && bitmapTrie.add(0x2110000, 0xFFFF0000, 5);
    assertTrue(status);
    assertEquals(25, bitmapTrie.getSize());
  }

  @Test
  public void testAdd_outOfSize() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    bitmapTrie.setSize(Integer.MAX_VALUE);
    // IP=1.32.232.0/21, net mask=255.255.248.0
    boolean status = bitmapTrie.add(0x120E800, 0xFFFFF800, 6);
    assertFalse(status);
    assertEquals(Integer.MAX_VALUE, bitmapTrie.getSize());
  }

  @Test
  public void testMatch_positive() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=1.32.232.0/21, net mask=255.255.248.0
    bitmapTrie.add(0x120E800, 0xFFFFF800, Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, bitmapTrie.match(0x120E800));
  }

  @Test
  public void testMatch_longestPrefix() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=2.17.131.0/16, net mask=255.255.0.0
    bitmapTrie.add(0x2110000, 0xFFFF0000, 98746831);
    // IP=2.17.131.0/24, net mask=255.255.255.0
    bitmapTrie.add(0x2118300, 0xFFFFFF00, 98746832);
    assertEquals(98746832, bitmapTrie.match(0x2118388));
  }

  @Test
  public void testMatch_nomatch() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=1.32.232.0/21, net mask=255.255.248.0
    bitmapTrie.add(0x120E800, 0xFFFFF800, 663);
    assertEquals(-1, bitmapTrie.match(0x120E700));
  }

  @Test
  public void testExpandAllocatedSize_positive() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(8);
    bitmapTrie.add(0x80800000, 0xFFFFFF00, 324);
    assertEquals(25, bitmapTrie.getSize());
    assertEquals(32, bitmapTrie.getAllocatedSize());
  }

  @Test
  public void testPersist_positive() throws Exception {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=2.17.131.0/24, net mask=255.255.255.0
    bitmapTrie.add(0x2118300, 0xFFFFFF00, 4);
    // IP=2.17.131.0/16, net mask=255.255.0.0
    bitmapTrie.add(0x2110000, 0xFFFF0000, 5);
    File file = new File("src/test/resources/ipgeo/indices1");
    bitmapTrie.persist("src/test/resources/ipgeo/indices1");
    assertTrue(file.exists());
  }

  @Test(expected = Exception.class)
  public void testPersist_throwsException() throws Exception {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=2.17.131.0/24, net mask=255.255.255.0
    bitmapTrie.add(0x2118300, 0xFFFFFF00, 4);
    // IP=2.17.131.0/16, net mask=255.255.0.0
    bitmapTrie.add(0x2110000, 0xFFFF0000, 5);
    bitmapTrie.persist("../../../resources/ipgeo/indices2");
  }

  @Test
  public void testRecover_positive() throws Exception {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=2.17.131.0/16, net mask=255.255.0.0
    bitmapTrie.add(0x2110000, 0xFFFF0000, 98746831);
    // IP=2.17.131.0/24, net mask=255.255.255.0
    bitmapTrie.add(0x2118300, 0xFFFFFF00, 98746832);
    bitmapTrie.persist("src/test/resources/ipgeo/indices3");
    BitmapTrie bitmapTrie1 = new BitmapTrie();
    bitmapTrie1.recover("src/test/resources/ipgeo/indices3", bitmapTrie.getSize());
    assertEquals(98746832, bitmapTrie.match(0x2118388));
  }

  @Test(expected = Exception.class)
  public void testRecover_throwsException() throws Exception {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    bitmapTrie.recover("../../../resources/ipgeo/indices4", bitmapTrie.getSize());
  }

  @Test
  public void testToString() {
    BitmapTrie bitmapTrie = new BitmapTrie();
    bitmapTrie.init(64);
    // IP=1.32.232.0/21, net mask=255.255.248.0
    bitmapTrie.add(0x120E800, 0xFFFFF800, Integer.MAX_VALUE);
    // Validate value nodes content
    assertTrue(bitmapTrie.toString()
        .contains("-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,2147483647,"));
  }
}

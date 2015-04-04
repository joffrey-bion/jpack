package org.hildan.compression;

import org.hildan.compression.huffman.adaptive.vitter.VitterTest;
import org.hildan.compression.huffman.semi_adaptive.StaticHuffmanTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ VitterTest.class, StaticHuffmanTest.class })
public class AllTests {

}

package com.jbion.compression;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.jbion.compression.huffman.adaptive.vitter.VitterTest;
import com.jbion.compression.huffman.semi_adaptive.StaticHuffmanTest;

@RunWith(Suite.class)
@SuiteClasses({ VitterTest.class, StaticHuffmanTest.class })
public class AllTests {

}

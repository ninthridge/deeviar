package com.ninthridge.deeviar.util;

import java.math.BigInteger;
import java.util.Random;

public class UniqueIdentifierGenerator {
  public static String generateUniqueIdentifier() {
    return new BigInteger(130, new Random()).toString(32);
  }
}

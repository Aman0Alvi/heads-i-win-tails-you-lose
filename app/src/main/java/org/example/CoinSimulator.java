package org.example;

import java.util.Random;

public class CoinSimulator {
  public static int flipNTimes(int n, double p, Random rng) {
    int heads = 0;
    for (int i = 0; i < n; i++) {
      if (rng.nextDouble() < p) heads++;
    }
    return heads;
  }
}

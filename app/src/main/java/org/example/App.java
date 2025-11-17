package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class App {

  public static void main(String[] args) throws IOException {
    int trialsPerSetting = 10; 
    int[] flipCounts = new int[] {10, 50, 100, 500, 1000};     
    double[] headProbabilities = new double[] {0.30, 0.50, 0.75};
    Long randomSeed = 42L;   
    String csvOutputPath = null; 

    Map<String, String> cliArgs = parseArgs(args);
    if (cliArgs.containsKey("m")) trialsPerSetting = Integer.parseInt(cliArgs.get("m"));
    if (cliArgs.containsKey("n")) flipCounts = parseIntList(cliArgs.get("n"));
    if (cliArgs.containsKey("p")) headProbabilities = parseDoubleList(cliArgs.get("p"));
    if (cliArgs.containsKey("seed")) {
      if (cliArgs.get("seed").equalsIgnoreCase("random")) randomSeed = null;
      else randomSeed = Long.parseLong(cliArgs.get("seed"));
    }
    if (cliArgs.containsKey("csv")) csvOutputPath = cliArgs.get("csv");

    Random random = (randomSeed == null) ? new Random() : new Random(randomSeed);

    List<TrialResult> results = new ArrayList<>();
    System.out.println("Number of Flips (n)\tTrial Number\tProbability of Heads (p)\tNumber of Heads");
    for (double headProbability : headProbabilities) {
      for (int flips : flipCounts) {

        for (int trialIndex = 1; trialIndex <= trialsPerSetting; trialIndex++) {
          int headCount = CoinSimulator.flipNTimes(flips, headProbability, random);
          TrialResult result = new TrialResult(flips, trialIndex, headProbability, headCount);
          results.add(result);
          System.out.printf("%d\t%d\t%.4f\t%d%n", flips, trialIndex, headProbability, headCount);
        }

        Stats stats = Stats.of(results.stream()
            .filter(r -> r.flips == flips && Double.compare(r.headProbability, headProbability) == 0)
            .mapToInt(r -> r.headCount)
            .toArray());

        double expectedHeads = flips*headProbability;
        System.out.printf("→ (n=%d, p=%.4f): mean=%.3f, std=%.3f, expected np=%.3f%n%n", flips, headProbability, stats.mean, stats.stdDev, expectedHeads);
      }
    }

    if (csvOutputPath != null) {
      writeCsv(csvOutputPath, results);
      System.out.println("Saved CSV to " + csvOutputPath);
    }
  }

  private static Map<String,String> parseArgs(String[] args) {
    Map<String,String> map = new HashMap<>();
    for (int i = 0; i < args.length - 1; i++) {
      if (args[i].startsWith("--")) map.put(args[i].substring(2), args[i+1]);
    }
    return map;
  }

  private static int[] parseIntList(String csv) {
    return Arrays.stream(csv.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
  }

  private static double[] parseDoubleList(String csv) {
    return Arrays.stream(csv.split(",")).map(String::trim).mapToDouble(Double::parseDouble).toArray();
  }

  private static void writeCsv(String outputPath, List<TrialResult> results) throws IOException {
    StringBuilder sb = new StringBuilder("n,m_trial,p,heads\n");
    for (TrialResult r : results) {
      sb.append(r.flips).append(",")
        .append(r.trialIndex).append(",")
        .append(r.headProbability).append(",")
        .append(r.headCount).append("\n");
    }
    Files.writeString(Path.of(outputPath), sb.toString());
  }

  private record TrialResult(int flips, int trialIndex, double headProbability, int headCount) {}

  private static class Stats {
    final double mean;
    final double stdDev;
    private Stats(double mean, double stdDev) { this.mean = mean; this.stdDev = stdDev; }

    static Stats of(int[] values) {
      if (values.length == 0) return new Stats(Double.NaN, Double.NaN);
      double sum = 0; for (int v : values) sum += v;
      double mean = sum/(double) values.length;
      double ss = 0; for (int v : values) ss += (v - mean)*(v - mean);
      double stdDev = Math.sqrt(ss/(values.length - 1)); 
      return new Stats(mean, stdDev);
    }
  }
}

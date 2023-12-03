package net.darmo_creations.mca_blocks_converter;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class Converter {
  public static final String VERSION = "1.0";

  private static final Pattern REGION_FILE_NAME_PATTERN = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
  private static final Pattern MAPPING_PATTERN = Pattern.compile("(\\w+:\\w+)\\s*->\\s*(\\w+:\\w+)");

  private final File regionDirParent;
  private final Stack<Coord> regionsCoords;
  private final Map<String, String> substitutionMap = new HashMap<>();
  private final int filesNumber;
  private final Set<ConverterThread> threads = new HashSet<>();
  private boolean started = false;
  private int progress = 0;
  private long startTime = 0;

  /**
   * Creates a browser for the given map.
   *
   * @param regionDirParent Path to the parent of the region directory.
   * @param configFile      Path to the mapping file.
   * @param threadsNb       Number of threads of the thread pool.
   */
  public Converter(File regionDirParent, File configFile, int threadsNb) throws IOException, ParseException {
    if (!regionDirParent.isDirectory()) throw new IOException("first argument must be a directory");
    if (!configFile.isFile()) throw new IOException("second argument must be a file");
    this.regionDirParent = regionDirParent;
    this.regionsCoords = this.initRegionsCoordinates();
    this.filesNumber = this.regionsCoords.size();
    this.initSubstitutionMap(configFile);
    for (int i = 0; i < threadsNb; i++) {
      this.threads.add(new ConverterThread(this));
    }
  }

  /**
   * Backup the region directory then returns the coordinates of regions to convert as a stack.
   */
  private Stack<Coord> initRegionsCoordinates() throws IOException {
    File backupDir = new File(this.regionDirParent, "region-backup");
    if (!backupDir.exists())
      //noinspection ResultOfMethodCallIgnored
      backupDir.mkdir();
    File regionDir = new File(this.regionDirParent, "region");
    Stack<Coord> points = new Stack<>();
    try (Stream<Path> files = Files.list(Paths.get(regionDir.toURI()))) {
      files.forEach(p -> {
        Matcher m = REGION_FILE_NAME_PATTERN.matcher(p.getFileName().toString());
        if (m.matches()) {
          File dest = new File(backupDir + File.separator + p.getFileName());
          try {
            Files.copy(p, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          int x = Integer.parseInt(m.group(1));
          int y = Integer.parseInt(m.group(2));
          points.push(new Coord(x, y));
        }
      });
    }
    return points;
  }

  private void initSubstitutionMap(File configFile) throws IOException, ParseException {
    try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
      int i = 1;
      String line;
      while ((line = br.readLine()) != null) {
        Matcher matcher = MAPPING_PATTERN.matcher(line);
        if (matcher.matches()) {
          String fromID = matcher.group(1);
          String toID = matcher.group(2);
          if (this.substitutionMap.containsKey(fromID))
            throw new ParseException("duplicate left ID '%s'".formatted(fromID), i);
          this.substitutionMap.put(fromID, toID);
        }
        i++;
      }
    }
  }

  public String mapID(String id) {
    return this.substitutionMap.getOrDefault(id, id);
  }

  /**
   * Returns the path of the directory containing the .mca files.
   */
  public File getRegionDirParent() {
    return this.regionDirParent;
  }

  public void start() {
    this.progress = 0;
    this.started = true;
    this.startTime = System.currentTimeMillis();
    this.threads.forEach(ConverterThread::start);
    System.out.printf("Converting directory '%s'%n", this.regionDirParent);
  }

  /**
   * Returns the coordinates of the next region to handle.
   */
  public synchronized Optional<Coord> getNextRegionCoordinate() {
    return this.regionsCoords.isEmpty() ? Optional.empty() : Optional.of(this.regionsCoords.pop());
  }

  public synchronized void updateProgress() {
    this.progress++;
    float percent = ((float) this.progress / this.filesNumber) * 100;
    System.out.format(Locale.ENGLISH, "\rProgression : %.2f%%", percent);
  }

  /**
   * Called whenever a thread finishes its task.
   * When the last thread calls this method the total time is printed.
   *
   * @param threadId ID of the thread that finished its task.
   */
  public synchronized void onThreadDone(long threadId) {
    if (this.started && !this.threads.isEmpty()) {
      this.threads.removeIf(thread -> thread.getId() == threadId);
    }
    if (this.threads.isEmpty()) {
      long rawTime = System.currentTimeMillis() - this.startTime;
      long hours = rawTime / (3600 * 1000);
      long minutes = (rawTime / (60 * 1000)) % 60;
      long seconds = (rawTime / 1000) % 60;
      long mseconds = rawTime % 1000;

      if (this.threads.isEmpty()) {
        System.out.println();
        if (hours != 0)
          System.out.printf("Finished in %d:%d:%d.%d.%n", hours, minutes, seconds, mseconds);
        else if (minutes != 0)
          System.out.printf("Finished in %d:%d.%d.%n", minutes, seconds, mseconds);
        else if (seconds != 0)
          System.out.printf("Finished in %d.%d s.%n", seconds, mseconds);
        else
          System.out.printf("Finished in %d ms.%n", mseconds);
      }
    }
  }
}

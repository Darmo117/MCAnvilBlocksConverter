package net.darmo_creations.mca_blocks_converter;

import java.io.*;
import java.text.*;

public final class Main {
  public static void main(String[] args) {
    if (args.length == 1 && args[0].equals("-h")) {
      printHelp();
    } else if (args.length == 2) {
      convertMap(new File(args[0]), new File(args[1]));
    } else {
      printUsage();
    }
  }

  private static void convertMap(File regionDirectory, File configFilePath) {
    try {
      String version = String.format("= Block Converter v%s =", Converter.VERSION);
      String frameLine = String.format("%" + version.length() + "s", "").replace(' ', '=');
      System.out.println(frameLine);
      System.out.println(version);
      System.out.println(frameLine);
      new Converter(regionDirectory, configFilePath, 4).start();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (ParseException e) {
      System.err.printf("Parse error @%d: %s%n", e.getErrorOffset(), e.getMessage());
    }
  }

  private static void printUsage() {
    System.err.println("Arguments : <path_to_region_files_dir> <path_to_config>");
    System.err.println("To show the help, use option -h");
  }

  private static void printHelp() {
    System.out.println("""
        ARGUMENTS

          {<path_to_region_files_dir> <path_to_config>|-h}

        OPTIONS

          -h
            Shows this help

        DESCRIPTION

          Conversion is done using the mappings from the provided file (2nd arg).
          Each mapping must be on a separate line and be in the format:
            ns1:id1,ns2:id2
          IDs cannot appear more than once on the left but may appear multiple times on the right.
        """);
  }

  private Main() {
  }
}

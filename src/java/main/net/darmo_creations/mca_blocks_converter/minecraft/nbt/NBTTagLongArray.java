package net.darmo_creations.mca_blocks_converter.minecraft.nbt;

import java.io.*;
import java.util.*;

public class NBTTagLongArray extends NBTBase {
  /** The array of saved longs */
  private long[] longArray;

  NBTTagLongArray() {
    this(new long[0]);
  }

  public NBTTagLongArray(long[] array) {
    this.longArray = array;
  }

  /**
   * Write the actual data contents of the tag, implemented in NBT extension classes
   */
  @Override
  void write(DataOutput output) throws IOException {
    output.writeInt(this.longArray.length);

    for (int i = 0; i < this.longArray.length; ++i) {
      output.writeLong(this.longArray[i]);
    }
  }

  @Override
  void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
    sizeTracker.read(192);
    int i = input.readInt();
    sizeTracker.read(32 * i);
    this.longArray = new long[i];

    for (int j = 0; j < i; ++j) {
      this.longArray[j] = input.readInt();
    }
  }

  /**
   * Gets the type byte for the tag.
   */
  @Override
  public byte getId() {
    return LONG_ARRAY;
  }

  @Override
  public String toString() {
    String s = "[";

    for (long i : this.longArray) {
      s = s + i + ",";
    }

    return s + "]";
  }

  /**
   * Creates a clone of the tag.
   */
  @Override
  public NBTBase copy() {
    long[] along = new long[this.longArray.length];
    System.arraycopy(this.longArray, 0, along, 0, this.longArray.length);
    return new NBTTagLongArray(along);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) ? Arrays.equals(this.longArray, ((NBTTagLongArray) o).longArray) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ Arrays.hashCode(this.longArray);
  }

  public long[] getLongArray() {
    return this.longArray;
  }
}
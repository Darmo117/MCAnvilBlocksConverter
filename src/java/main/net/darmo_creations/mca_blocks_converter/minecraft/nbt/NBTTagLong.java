package net.darmo_creations.mca_blocks_converter.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagLong extends NBTBase.NBTPrimitive {
  /** The long value for the tag. */
  private long data;
  
  NBTTagLong() {
    this(0);
  }
  
  public NBTTagLong(long data) {
    this.data = data;
  }
  
  /**
   * Write the actual data contents of the tag, implemented in NBT extension classes
   */
  @Override
  void write(DataOutput output) throws IOException {
    output.writeLong(this.data);
  }
  
  @Override
  void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
    sizeTracker.read(128);
    this.data = input.readLong();
  }
  
  /**
   * Gets the type byte for the tag.
   */
  @Override
  public byte getId() {
    return LONG;
  }
  
  @Override
  public String toString() {
    return this.data + "L";
  }
  
  /**
   * Creates a clone of the tag.
   */
  @Override
  public NBTBase copy() {
    return new NBTTagLong(this.data);
  }
  
  @Override
  public boolean equals(Object o) {
    if (super.equals(o)) {
      return this.data == ((NBTTagLong) o).data;
    }
    else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return super.hashCode() ^ (int) (this.data ^ this.data >>> 32);
  }
  
  @Override
  public long getLong() {
    return this.data;
  }
  
  @Override
  public int getInt() {
    return (int) (this.data & -1L);
  }
  
  @Override
  public short getShort() {
    return (short) ((int) (this.data & 65535L));
  }
  
  @Override
  public byte getByte() {
    return (byte) ((int) (this.data & 255L));
  }
  
  @Override
  public double getDouble() {
    return this.data;
  }
  
  @Override
  public float getFloat() {
    return (float) this.data;
  }
}
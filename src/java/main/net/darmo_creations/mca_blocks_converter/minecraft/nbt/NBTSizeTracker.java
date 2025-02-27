package net.darmo_creations.mca_blocks_converter.minecraft.nbt;

public class NBTSizeTracker {
  public static final NBTSizeTracker INFINITE = new NBTSizeTracker(0) {
    @Override
    public void read(long bits) {}
  };
  private final long max;
  private long read;
  
  public NBTSizeTracker(long max) {
    this.max = max;
  }
  
  /**
   * Tracks the reading of the given amount of bits(!)
   */
  public void read(long bits) {
    this.read += bits / 8;
    
    if (this.read > this.max) {
      throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.read + "bytes where max allowed: " + this.max);
    }
  }
  
  /*
   * UTF8 is not a simple encoding system, each character can be either 1, 2, or 3 bytes. Depending
   * on where it's numerical value falls. We have to count up each character individually to see the
   * true length of the data.
   * 
   * Basic concept is that it uses the MSB of each byte as a 'read more' signal. So it has to shift
   * each 7-bit segment.
   * 
   * This will accurately count the correct byte length to encode this string, plus the 2 bytes for
   * it's length prefix.
   */
  public static void readUTF(NBTSizeTracker tracker, String data) {
    tracker.read(16); // Header length
    if (data == null) return;
    
    int len = data.length();
    int utflen = 0;
    
    for (int i = 0; i < len; i++) {
      int c = data.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) utflen += 1;
      else if (c > 0x07FF) utflen += 3;
      else utflen += 2;
    }
    tracker.read(8 * utflen);
  }
}
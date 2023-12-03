package net.darmo_creations.mca_blocks_converter;

import net.darmo_creations.mca_blocks_converter.minecraft.*;
import net.darmo_creations.mca_blocks_converter.minecraft.nbt.*;

import java.io.*;
import java.util.*;

public class ConverterThread extends Thread {
  private static final int REGION_SIZE = 32;

  private final Converter converter;

  public ConverterThread(Converter converter) {
    this.converter = converter;
  }

  @Override
  public final void run() {
    File dir = this.converter.getRegionDirParent();
    Optional<Coord> optCoord;
    while (!this.isInterrupted() && (optCoord = this.converter.getNextRegionCoordinate()).isPresent()) {
      this.handleRegion(optCoord.get(), dir);
    }
    this.converter.onThreadDone(this.getId());
  }

  private void handleRegion(Coord regionCoordinate, File dir) {
    for (int x = 0; x < REGION_SIZE; x++) {
      for (int y = 0; y < REGION_SIZE; y++) {
        int chunkX = regionCoordinate.x() * REGION_SIZE + x;
        int chunkY = regionCoordinate.y() * REGION_SIZE + y;
        NBTTagCompound chunk = this.readChunk(regionCoordinate, dir, chunkX, chunkY);
        if (chunk == null) continue;
        this.updateChunk(chunk);
//        this.writeChunk(regionCoordinate, dir, chunkX, chunkY, chunk); // TEMP
      }
    }
    this.converter.updateProgress();
  }

  private NBTTagCompound readChunk(Coord regionCoordinate, File dir, int chunkX, int chunkY) {
    NBTTagCompound chunk;
    try (DataInputStream in = RegionFileCache.getChunkInputStream(dir, chunkX, chunkY)) {
      if (in == null) return null;
      chunk = CompressedStreamTools.read(in);
    } catch (IOException ignored) {
      System.err.printf("Error while reading chunk [%d, %d]@(%d, %d)%n",
          regionCoordinate.x(), regionCoordinate.y(), chunkX, chunkY);
      return null;
    }
    return chunk;
  }

  private void writeChunk(Coord regionCoordinate, File dir, int chunkX, int chunkY, NBTTagCompound chunk) {
    try (DataOutputStream out = RegionFileCache.getChunkOutputStream(dir, chunkX, chunkY)) {
      CompressedStreamTools.write(chunk, out);
    } catch (IOException e) {
      System.err.printf("Error while saving chunk [%d, %d]@(%d, %d)%n",
          regionCoordinate.x(), regionCoordinate.y(), chunkX, chunkY);
    }
  }

  private void updateChunk(NBTTagCompound chunk) {
    NBTTagList sections = chunk.getTagList("sections", NBTBase.COMPOUND);
    for (int i = 0; i < sections.tagCount(); i++) {
      NBTTagCompound section = (NBTTagCompound) sections.get(i);
      this.handleBlocksInSection(section);
    }
  }

  private void handleBlocksInSection(NBTTagCompound section) {
    NBTTagList blocks = section.getCompoundTag("block_states").getTagList("palette", NBTBase.COMPOUND);
    for (int i = 0; i < blocks.tagCount(); i++) {
      NBTTagCompound block = blocks.getCompoundTagAt(i);
      block.setString("Name", this.converter.mapID(block.getString("Name")));
    }
  }
}

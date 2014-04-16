
package com.sniperzciinema.portals.Util;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;


public class BlockCrawler {

	public static final int[][]	ADJ_LOC				= new int[][] { new int[] { -1, 0, 0 }, new int[] { 1, 0, 0 }, new int[] { 0, -1, 0 }, new int[] { 0, 1, 0 }, new int[] { 0, 0, -1 }, new int[] { 0, 0, 1 } };

	public static final int		DEFAULT_MAX_SIZE	= 1000;

	int							mMaxPortalSize;
	Block						mOrigBlock;
	ArrayList<String>			mProcessedBlocks;

	public BlockCrawler(int maxPortalSize)
	{
		this.mMaxPortalSize = maxPortalSize;
	}

	private void processAdjacent(Block block, Material type) {
		if ((block != null) && (block.getType() == type))
			if (!this.mProcessedBlocks.contains(new Coords(block.getLocation()).asStringIgnoreYawAndPitch()))
			{
				this.mProcessedBlocks.add(new Coords(block.getLocation()).asStringIgnoreYawAndPitch());
				for (int[] element : BlockCrawler.ADJ_LOC)
				{
					Location nextLoc = block.getLocation();
					nextLoc.setX(block.getX() + element[0]);
					nextLoc.setY(block.getY() + element[1]);
					nextLoc.setZ(block.getZ() + element[2]);
					if (this.mProcessedBlocks.size() < this.mMaxPortalSize)
						processAdjacent(nextLoc.getBlock(), block.getType());
				}
			}
	}

	public void start(Block origBlock, ArrayList<String> blockCoordsArr) {
		this.mOrigBlock = origBlock;
		this.mProcessedBlocks = blockCoordsArr;
		processAdjacent(this.mOrigBlock, this.mOrigBlock.getType());
	}

}

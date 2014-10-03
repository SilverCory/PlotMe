package com.worldcretornica.plotme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlotClearTask extends BukkitRunnable
{
	private final Queue<Block> blocks = new ConcurrentLinkedQueue<>();
	private final PlotMapInfo pmi;
	private final World w;
	private final Plot plot;
	private final Location bottom;
	private final Location top;
	private BukkitTask timerTask;

	public PlotClearTask(PlotMapInfo pmi, World w, Plot plot, Location bottom, Location top)
	{
		this.pmi = pmi;
		this.w = w;
		this.plot = plot;
		this.bottom = bottom;
		this.top = top;
	}

	public BukkitTask getTimerTask()
	{
		return timerTask;
	}

	@Override
	public void run()
	{
		int bottomX = bottom.getBlockX();
		int bottomZ = bottom.getBlockZ();
		int topX = top.getBlockX();
		int topZ = top.getBlockZ();

		for(int x = bottomX; x <= topX; x++)
		{
			for(int z = bottomZ; z <= topZ; z++)
			{
				for(int y = w.getMaxHeight(); y >= 0; --y)
				{
					Block block = new Location(w, x, y, z).getBlock();

					if(block.getY() == 0)
					{
						if(block.getTypeId() != pmi.BottomBlockId ||
							block.getData() != pmi.BottomBlockValue)
						{
							blocks.add(block);
						}
					}
					else if(block.getY() < pmi.RoadHeight)
					{
						if(block.getTypeId() != pmi.PlotFillingBlockId ||
							block.getData() != pmi.PlotFillingBlockValue)
						{
							blocks.add(block);
						}
					}
					else if(block.getY() == pmi.RoadHeight)
					{
						if(block.getTypeId() != pmi.PlotFloorBlockId ||
							block.getData() != pmi.PlotFloorBlockValue)
						{
							blocks.add(block);
						}
					}
					else
					{
						if(y == (pmi.RoadHeight + 1) &&
							(x == bottomX - 1 || x == topX + 1 || z == bottomZ - 1 || z == topZ + 1))
						{

						}
						else if(block.getType() != Material.AIR || block.getBiome() != Biome.PLAINS)
						{
							blocks.add(block);
						}
					}
				}
			}
		}

		final int asyncTaskId = getTaskId();
		timerTask = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				Block block;

				for(int i = 0; i < 1000 && (block = blocks.poll()) != null; ++i)
				{
					if (block.getBiome() != Biome.PLAINS)
					{
						block.setBiome(Biome.PLAINS);
					}

					BlockState state = block.getState();

					if(state instanceof InventoryHolder)
					{
						InventoryHolder holder = (InventoryHolder) state;
						holder.getInventory().clear();
					}

					if(state instanceof Jukebox)
					{
						Jukebox jukebox = (Jukebox) state;
						//Remove once they fix the NullPointerException
						try
						{
							jukebox.setPlaying(Material.AIR);
						}catch(Exception e){}
					}

					if(block.getY() == 0)
					{
						block.setTypeIdAndData(pmi.BottomBlockId, pmi.BottomBlockValue, false);
					}
					else if(block.getY() < pmi.RoadHeight)
					{
						block.setTypeIdAndData(pmi.PlotFillingBlockId, pmi.PlotFillingBlockValue, false);
					}
					else if(block.getY() == pmi.RoadHeight)
					{
						block.setTypeIdAndData(pmi.PlotFloorBlockId, pmi.PlotFloorBlockValue, false);
					}
					else if(block.getType() != Material.AIR)
					{
						block.setTypeIdAndData(0, (byte) 0, false);
					}
				}

				if(blocks.isEmpty() &&
					!Bukkit.getScheduler().isCurrentlyRunning(asyncTaskId) &&
					!Bukkit.getScheduler().isQueued(asyncTaskId))
				{
					cancel();

					PlotManager.adjustWall(bottom);
					PlotManager.RemoveLWC(w, plot);
				}
			}
		}.runTaskTimer(PlotMe.self, 0, 1);
	}
}

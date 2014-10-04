package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class PlotRunnableDeleteExpire implements Runnable {

	public void run()
	{
		if(PlotMe.worldcurrentlyprocessingexpired != null)
		{
			final World w = PlotMe.worldcurrentlyprocessingexpired;
			List<Plot> expiredplots = new ArrayList<Plot>();
			HashMap<String, Plot> plots = PlotManager.getPlots(w);
			String date = PlotMe.getDate();
			
			for(String id : plots.keySet())
			{
				Plot plot = plots.get(id);
				
				if(!plot.protect && !plot.finished && plot.expireddate != null &&
						PlotMe.getDate(plot.expireddate).compareTo(date.toString()) < 0 &&
						PlotManager.isAsyncRunning(w, plot))
				{
					PlotMe.counterdeletingexpired++;
					expiredplots.add(plot);
				}
				
				if(expiredplots.size() == PlotMe.nbperdeletionprocessingexpired)
				{
					break;
				}
			}
			
			if(expiredplots.size() == 0)
			{
				PlotMe.counterexpired = 0;
			}
			else
			{
				Collections.sort(expiredplots);
				
				for(int ictr = 0; ictr < PlotMe.nbperdeletionprocessingexpired && expiredplots.size() > 0; ictr++)
				{
					final Plot expiredplot = expiredplots.get(0);
					
					expiredplots.remove(0);

					PlotClearTask clearTask = PlotManager.clear(w, expiredplot);

					if(clearTask == null)
					{
						continue;
					}

					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							if (!PlotManager.isAsyncRunning(w, expiredplot))
							{
								cancel();

								String id = expiredplot.id;

								PlotManager.getPlots(w).remove(id);

								PlotManager.removeOwnerSign(w, id);
								PlotManager.removeSellSign(w, id);

								SqlManager.deletePlot(PlotManager.getIdX(id), PlotManager.getIdZ(id), w.getName().toLowerCase());

								PlotMe.cscurrentlyprocessingexpired.sendMessage("" + PlotMe.PREFIX + PlotMe.caption("MsgDeletedExpiredPlots") + " " + id);

								PlotMe.counterdeletingexpired--;
							}
						}
					}.runTaskTimer(PlotMe.self, 0, 1);

					PlotMe.counterexpired--;
				}
			}
			
			if(PlotMe.counterexpired == 0)
			{
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						if (PlotMe.counterdeletingexpired == null || PlotMe.counterdeletingexpired <= 0)
						{
							cancel();

							PlotMe.cscurrentlyprocessingexpired.sendMessage("" + PlotMe.PREFIX + PlotMe.caption("MsgDeleteSessionFinished"));
							PlotMe.worldcurrentlyprocessingexpired = null;
							PlotMe.cscurrentlyprocessingexpired = null;
							PlotMe.counterdeletingexpired = null;
						}
					}
				}.runTaskTimer(PlotMe.self, 0, 1);
			}
		}
	}
}

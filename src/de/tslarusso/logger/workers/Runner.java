package de.tslarusso.logger.workers;

public class Runner implements Runnable
{
	protected volatile Thread blinker;
	public void start()
	{
		blinker = new Thread( this );
		blinker.start();
	}

	public void stop()
	{
		Thread tmpBlinker = blinker;
		blinker = null;
		if ( tmpBlinker != null )
		{
			tmpBlinker.interrupt();
		}
	}

	public void run()
	{
	}

	public boolean isRunning()
	{
		return blinker != null;
	}
}

package de.tslarusso.logger.workers;

import de.tslarusso.logger.view.ResponseListener;

import java.net.Socket;

public class UpdateRunnable implements Runnable
{
	private String message;
	private Socket client;

	private final ResponseListener listener;

	UpdateRunnable( ResponseListener listener, String message, Socket client )
	{
		this.listener = listener;
		this.message = message;
		this.client = client;
	}

	public void run()
	{
		if ( message != null )
		{
			listener.newMessage( client, message );
		}
	}
}

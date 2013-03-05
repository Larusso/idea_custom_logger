package de.tslarusso.logger.workers;

import de.tslarusso.logger.view.ResponseListener;

import java.net.Socket;

public class DisconnectRunner implements Runnable
{
	private final ResponseListener listener;
	private final Socket client;

	DisconnectRunner( ResponseListener listener, Socket client )
	{
		this.listener = listener;
		this.client = client;
	}

	public void run()
	{
		listener.clientDisConnected( client );
	}
}

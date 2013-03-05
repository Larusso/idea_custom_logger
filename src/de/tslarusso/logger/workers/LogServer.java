package de.tslarusso.logger.workers;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import de.tslarusso.logger.view.ResponseListener;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LogServer extends Runner
{
	private static Logger LOG = Logger.getInstance( LogServer.class );

	private ServerSocket serverSocket;
	private int portNumber = 4444;
	private List<Runner> clients;
	private final ResponseListener responseListener;

	public LogServer( ResponseListener listener )
	{
		responseListener = listener;
		clients = new ArrayList<Runner>();
	}

	@Override
	public void run()
	{
		LOG.info( "start logging server" );
		try
		{
			serverSocket = new ServerSocket( portNumber );
			serverSocket.setSoTimeout( 1000 );
		}
		catch ( BindException bindError )
		{
			Notifications.Bus.notify( new Notification( "smeetLogger", "Server could not start", bindError.getMessage(), NotificationType.ERROR ) );
			LOG.error( "server could not start", bindError.getMessage() );
			return;
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return;
		}

		Thread thisThread = Thread.currentThread();
		while ( blinker == thisThread )
		{
			connect();
		}

		//cleanup
		Notifications.Bus.notify( new Notification( "smeetLogger", "Server stopped", "close all connections", NotificationType.INFORMATION ) );

		LOG.info( "Close server socket " + portNumber );
		for ( int i = 0; i < clients.size(); i++ )
		{
			Runner client = clients.get( i );
			client.stop();
		}

		try
		{
			serverSocket.close();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

	}

	private void connect()
	{
		Socket socket;
		try
		{
			System.out.println( "timeout " + serverSocket.getSoTimeout() );
			socket = serverSocket.accept();

			LOG.info( "client connected" );
			Notifications.Bus.notify( new Notification( "smeetLogger", "client connected", "new client connected", NotificationType.INFORMATION ) );
			Runner client = new LogReader( socket, responseListener );
			clients.add( client );
			client.start();

			//got to sleep for one second
			Thread.sleep( 1000 );
		}
		catch ( IOException e )
		{
			System.out.println( "socket i/o" );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}

package de.tslarusso.logger.workers;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.view.ResponseListener;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class LogServerWorker extends Runner
{
	private static Logger LOG = Logger.getInstance( LogServerWorker.class );
	private final ResponseListener responseListener;
	private final ServerSocket serverSocket;
	StringBuilder message;
	private Socket socket;

	private Runner client;
	private BufferedReader in;
	private final Project project;

	public LogServerWorker( ServerSocket serverSocket, ResponseListener responseListener, Project project )
	{
		this.serverSocket = serverSocket;
		this.responseListener = responseListener;
		this.project = project;
	}

	@Override
	public void run()
	{
		LOG.info( "start logging server" );
		Notifications.Bus.notify( new Notification( "smeetLogger", "Server started", "Server wait for connection", NotificationType.INFORMATION ), project );
		try
		{
			socket = serverSocket.accept();
		}
		catch ( SocketTimeoutException timeoutException )
		{
			LOG.info( "socket server timeout", timeoutException );
			Notifications.Bus.notify( new Notification( "smeetLogger", "Server timeout", "No connection could be established", NotificationType.WARNING ), project );
			closeConnection();

			return;
		}
		catch ( IOException e )
		{
			LOG.info( "server accept error" );
			closeConnection();
			return;
		}

		EventQueue.invokeLater( new ConnectRunnable( responseListener, socket ) );

		try
		{
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		//read log messages
		readLogs();

		//cleanup
		Notifications.Bus.notify( new Notification( "smeetLogger", "Server stopped", "close all connections", NotificationType.INFORMATION ), project );

		closeConnection();
	}

	private void closeConnection()
	{
		LOG.info( "Close server socket " + serverSocket.getLocalPort() );

		try
		{
			if ( socket != null )
			{
				socket.close();
			}
			serverSocket.close();
		}
		catch ( IOException e )
		{
			LOG.warn( "server socket could not be closed", e );
		}

		if ( socket != null )
		{
			EventQueue.invokeLater( new DisconnectRunnable( responseListener, socket ) );
		}

		EventQueue.invokeLater( new ShutDownRunnable( project ) );
	}

	private void readLogs()
	{
		Thread thisThread = Thread.currentThread();
		int lastUpdate = 0;
		List<String> messages = new ArrayList<String>();
		while ( blinker == thisThread )
		{
			String line;
			try
			{
				line = in.readLine();

				if ( line == null )
				{
					stop();
					continue;
				}

				line = line.trim();

				//check for start of new message
				if ( line.trim().startsWith( "!SOS" ) )
				{
					//ok we started a new line here
					//remove the !SOS command
					line = line.substring( 4, line.trim().length() );
					//check if we dealing with normal message or folded message

					if ( line.startsWith( "<showFoldMessage" ) )
					{
						//check if the message is in one line
						if ( line.endsWith( "</showFoldMessage>" ) )
						{
							//send it away
							EventQueue.invokeLater( new UpdateRunnable( responseListener, line, socket ) );
						}
						else
						{
							message = new StringBuilder( line + "\n" );
						}
					}
					else
					{
						//we can send the message right away
						EventQueue.invokeLater( new UpdateRunnable( responseListener, line, socket ) );
					}
				}
				else if ( message != null )
				{
					message.append( line + "\n" );

					if ( line.endsWith( "</showFoldMessage>" ) )
					{
						EventQueue.invokeLater( new UpdateRunnable( responseListener, message.toString(), socket ) );
					}

				}
			}
			catch ( IOException e )
			{
				LOG.warn( e );
			}
		}
	}
}

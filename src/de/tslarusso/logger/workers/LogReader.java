package de.tslarusso.logger.workers;

import de.tslarusso.logger.view.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;

public class LogReader extends Runner
{
	BufferedReader in;
	ResponseListener listener;
	Socket socket;
	StringBuilder message;

	LogReader( Socket socket, ResponseListener listener )
	{
		this.listener = listener;
		this.socket = socket;

		try
		{
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		EventQueue.invokeLater( new ConnectRunner( listener, socket ) );
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

				//check for start of new message
				if ( line.trim().startsWith( "!SOS" ) )
				{
					if ( message != null )
					{
						EventQueue.invokeLater( new UpdateRunner( listener, message.toString(), socket ) );
					}

					line = line.trim().substring( 4, line.trim().length() );
					message = new StringBuilder( line + "\n" );
				}
				else if ( message != null )
				{
					message.append( line.trim() + "\n" );
				}


				System.out.println( line );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}

		System.out.println( "close client" );
		try
		{
			socket.close();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		EventQueue.invokeLater( new DisconnectRunner( listener, socket ) );
	}
}

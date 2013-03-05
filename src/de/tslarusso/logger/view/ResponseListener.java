package de.tslarusso.logger.view;

import java.net.Socket;

public interface ResponseListener
{
	void newMessage( Socket client, String message );

	void clientConnected( Socket client );

	void clientDisConnected( Socket client );
}

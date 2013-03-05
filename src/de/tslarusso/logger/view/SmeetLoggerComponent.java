package de.tslarusso.logger.view;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.tslarusso.logger.model.LogMessage;
import de.tslarusso.logger.view.ui.SmeetLoggerWindow;
import de.tslarusso.logger.workers.LogServer;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

public class SmeetLoggerComponent implements ProjectComponent, ResponseListener, AnActionListener
{
	public static final String TOOLWINDOW_ID = "sMeet Logger";

	private final Project project;
	private final SmeetLoggerWindow smeetLoggerWindow;
	private ActionManager actionManager;
	private LogServer logServer;

	private final Map<Socket, Content> contentBySocket;
	private final Map<Socket, SmeetLoggerWindow> loggerWindowsBySocket;


	public SmeetLoggerComponent( Project project )
	{
		this.project = project;
		this.smeetLoggerWindow = new SmeetLoggerWindow( project );
		this.contentBySocket = new Hashtable<Socket, Content>();
		this.loggerWindowsBySocket = new Hashtable<Socket, SmeetLoggerWindow>();

		actionManager = ActionManager.getInstance();
		actionManager.addAnActionListener( this );
	}

	public static SmeetLoggerComponent getInstance( Project project )
	{
		return project.getComponent( SmeetLoggerComponent.class );
	}

	///////////////////////////////////////////////
	//  ProjectComponent implementation
	//////////////////////////////////////////////

	public void projectOpened()
	{

	}

	public void projectClosed()
	{
		if ( ToolWindowManager.getInstance( project ).getToolWindow( TOOLWINDOW_ID ) != null )
		{
			ToolWindowManager.getInstance( project ).unregisterToolWindow( TOOLWINDOW_ID );
		}
	}

	public void initComponent()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void disposeComponent()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@NotNull
	public String getComponentName()
	{
		return "SmeetLoggerComponent";
	}

	///////////////////////////////////////////////
	//  ResponseListener implementation
	//////////////////////////////////////////////

	public void newMessage( Socket client, String message )
	{
		LogMessage logMessage = convertMessage( message );
		final Content content = contentBySocket.get( client );
		final SmeetLoggerWindow ui = loggerWindowsBySocket.get( client );
		ui.addMessage( logMessage );
	}

	public void clientConnected( Socket client )
	{
		ToolWindow toolWindow = ToolWindowManager.getInstance( project ).getToolWindow( TOOLWINDOW_ID );
		if ( toolWindow == null )
		{
			toolWindow = ToolWindowManager.getInstance( project ).registerToolWindow( TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM );
			toolWindow.setAutoHide( false );
			toolWindow.setAvailable( true, null );
		}

		final ContentFactory contentFactory = toolWindow.getContentManager().getFactory();
		final SmeetLoggerWindow ui = new SmeetLoggerWindow( project );
		final Content loggerWindow = contentFactory.createContent( ui, "Logger", true );
		loggerWindow.setCloseable( false );

		toolWindow.getContentManager().addContent( loggerWindow );

		contentBySocket.put( client, loggerWindow );
		loggerWindowsBySocket.put( client, ui );
	}

	public void clientDisConnected( Socket client )
	{
		ToolWindow toolWindow = ToolWindowManager.getInstance( project ).getToolWindow( TOOLWINDOW_ID );
		Content loggerWindow = contentBySocket.get( client );
		//toolWindow.getContentManager().removeContent( loggerWindow, true );
	}

	///////////////////////////////////////////////
	//  AnActionListener implementation
	//////////////////////////////////////////////

	public void beforeActionPerformed( AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void afterActionPerformed( AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent )
	{
		String actionId = actionManager.getId( anAction );
		if ( actionId != null )
		{
			if ( actionId.equals( IdeActions.ACTION_DEFAULT_RUNNER ) || actionId.equals( IdeActions.ACTION_DEFAULT_DEBUGGER ) )
			{
				startSocketThread();
			}

			if ( actionId.equals( IdeActions.ACTION_STOP_PROGRAM ) )
			{
				stopSocketThread();
			}
		}
		else if ( anAction.toString().equals( "Close (null)" ) )
		{
			stopSocketThread();
		}
	}

	public void beforeEditorTyping( char c, DataContext dataContext )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	///////////////////////////////////////////////
	//  start/stop logging server
	//////////////////////////////////////////////

	private void startSocketThread()
	{
		System.out.println( "startSocketThread" );
		if ( logServer == null )
		{
			logServer = new LogServer( this );
		}
		if ( !logServer.isRunning() )
		{
			logServer.start();
		}
	}

	private void stopSocketThread()
	{
		System.out.println( "stopSocketThread" );
		if ( logServer != null )
		{
			logServer.stop();
		}
	}

	///////////////////////////////////////////////
	//  methods
	//////////////////////////////////////////////

	private LogMessage convertMessage( String message )
	{
		Document doc = null;
		try
		{
			doc = loadXMLFromString( message );
		}
		catch ( Exception e )
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		//<showMessage key="DEBUG">com.smeet.core.rpc.RPCManager Servicemethod getVersionInfo of service ProfileService returns result.</showMessage>

		/*
		<showFoldMessage key="ERROR">
		  <title>logTest multiline result</title>
		  <message>lalalalalalal</message>
		  <data>undefined</data>
		  <time>undefined</time>
		</showFoldMessage>
		*/

		Element messageElement = doc.getDocumentElement();

		String type = messageElement.getTagName();
		String level = messageElement.getAttribute( "key" );
		String mesg = null;
		if ( type.equals( "showFoldMessage" ) )
		{
			NodeList titles = messageElement.getElementsByTagName( "title" );
			NodeList messages = messageElement.getElementsByTagName( "message" );

			mesg = titles.item( 0 ).getFirstChild().getNodeValue();
			mesg += messages.item( 0 ).getFirstChild().getNodeValue();
		}
		else
		{
			mesg = messageElement.getFirstChild().getNodeValue();
		}


		return new LogMessage( type, mesg, level );
	}

	public static Document loadXMLFromString( String xml ) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource( new StringReader( xml ) );
		return builder.parse( is );
	}
}

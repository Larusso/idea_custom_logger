package de.tslarusso.logger;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.runners.RestartAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import de.tslarusso.logger.model.LogMessage;
import de.tslarusso.logger.view.ResponseListener;
import de.tslarusso.logger.view.ui.LogWindow;
import de.tslarusso.logger.workers.LogServerWorker;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

public class SmeetLoggerComponent implements ProjectComponent, ResponseListener, AnActionListener, ContentManagerListener
{
	private static Logger LOG = Logger.getInstance( SmeetLoggerComponent.class );

	public static final String TOOL_WINDOW_ID = "sMeetLogger";

	private final Project project;
	private ActionManager actionManager;
	private LogServerWorker logServer;

	private final Map<Socket, Content> contentBySocket;
	private final Map<Socket, LogWindow> loggerWindowsBySocket;


	public SmeetLoggerComponent( Project project )
	{
		this.project = project;
		this.contentBySocket = new Hashtable<Socket, Content>();
		this.loggerWindowsBySocket = new Hashtable<Socket, LogWindow>();
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
		if ( ToolWindowManager.getInstance( project ).getToolWindow( TOOL_WINDOW_ID ) != null )
		{
			ToolWindowManager.getInstance( project ).unregisterToolWindow( TOOL_WINDOW_ID );
		}
	}

	public void initComponent()
	{
		actionManager = ActionManager.getInstance();
		actionManager.addAnActionListener( this );
	}

	public void disposeComponent()
	{
		stopSocketThread();
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
		final LogWindow ui = loggerWindowsBySocket.get( client );
		ui.addMessage( logMessage );
	}

	public void clientConnected( Socket client )
	{
		SmeetLoggerSettings settings = ServiceManager.getService( project, SmeetLoggerSettings.class );
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance( project );
		ToolWindow toolWindow = toolWindowManager.getToolWindow( TOOL_WINDOW_ID );

		if ( toolWindow == null )
		{
			toolWindow = toolWindowManager.registerToolWindow( TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM );
			toolWindow.setIcon( IconLoader.getIcon( "/ide/notifications.png" ) );
			toolWindow.setSplitMode( true, null );
			toolWindow.getContentManager().addContentManagerListener( this );
		}

		final ContentFactory contentFactory = toolWindow.getContentManager().getFactory();
		final LogWindow ui = new LogWindow( project, settings );
		final Content loggerWindow = contentFactory.createContent( ui.getLogPanel(), "Logger", true );

		loggerWindow.setCloseable( false );

		toolWindow.getContentManager().addContent( loggerWindow );
		toolWindow.getContentManager().setSelectedContent( loggerWindow, true, true );

		contentBySocket.put( client, loggerWindow );
		loggerWindowsBySocket.put( client, ui );

		toolWindow.setAvailable( true, null );
	}

	public void clientDisConnected( Socket client )
	{
		Content loggerWindow = contentBySocket.get( client );
		loggerWindow.setCloseable( true );
		loggerWindow.setDisplayName( loggerWindow.getDisplayName() + "(closed)" );

		stopSocketThread();
	}

	///////////////////////////////////////////////
	//  AnActionListener implementation
	//////////////////////////////////////////////

	public void beforeActionPerformed( AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent )
	{
		//
	}

	public void afterActionPerformed( AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent )
	{
		SmeetLoggerSettings settings = ServiceManager.getService( project, SmeetLoggerSettings.class );
		if ( settings.isAutoStartConnection() )
		{
			String actionId = actionManager.getId( anAction );

			//dont start server if the related project is not the current project!
			if ( anActionEvent.getProject().equals( project ) )
			{
				if ( actionId != null )
				{
					if ( actionId.equals( IdeActions.ACTION_DEFAULT_RUNNER ) || actionId.equals( IdeActions.ACTION_DEFAULT_DEBUGGER ) )
					{
						//start socket only for flash configurations
						if ( RunManagerEx.getInstance( anActionEvent.getProject() ).getSelectedConfiguration().getConfiguration().getType().getId().equals( "FlashRunConfigurationType" ) )
						{
							startSocketThread();
						}
					}

					if ( actionId.equals( IdeActions.ACTION_STOP_PROGRAM ) )
					{
						stopSocketThread();
					}
				}
				else if ( anAction.getClass().equals( RestartAction.class ) )
				{
					//start socket only for flash configurations
					if ( RunManagerEx.getInstance( anActionEvent.getProject() ).getSelectedConfiguration().getConfiguration().getType().getId().equals( "FlashRunConfigurationType" ) )
					{
						startSocketThread();
					}
				}
				else if ( anAction.toString().equals( "Close (null)" ) )
				{
					stopSocketThread();
				}
			}
		}
	}

	public void beforeEditorTyping( char c, DataContext dataContext )
	{
		//
	}

	///////////////////////////////////////////////
	//  ContentManagerListener implementation
	//////////////////////////////////////////////

	public void contentAdded( ContentManagerEvent contentManagerEvent )
	{
		ToolWindowManager.getInstance( project ).getToolWindow( TOOL_WINDOW_ID ).show( null );
	}

	public void contentRemoved( ContentManagerEvent contentManagerEvent )
	{
		if ( contentManagerEvent.getContent().getManager().getContentCount() == 0 )
		{
			ToolWindow toolWindow = ToolWindowManager.getInstance( project ).getToolWindow( TOOL_WINDOW_ID );
			toolWindow.getContentManager().removeContentManagerListener( this );
			ToolWindowManager.getInstance( project ).unregisterToolWindow( TOOL_WINDOW_ID );
			stopSocketThread();
		}
	}

	public void contentRemoveQuery( ContentManagerEvent contentManagerEvent )
	{
		//
	}

	public void selectionChanged( ContentManagerEvent contentManagerEvent )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}


	///////////////////////////////////////////////
	//  start/stop logging server
	//////////////////////////////////////////////

	public void startSocketThread()
	{
		SmeetLoggerSettings settings = ServiceManager.getService( project, SmeetLoggerSettings.class );
		LOG.info( "start logging server socket" );
		if ( logServer == null )
		{
			try
			{
				LOG.info( String.format( "create server socket instance with connection port %d and timeout %d", settings.getConnectionPort(), settings.getConnectionTimeout() ) );
				ServerSocket socket = new ServerSocket( settings.getConnectionPort() );
				if ( settings.getConnectionTimeout() >= 0 )
				{
					socket.setSoTimeout( settings.getConnectionTimeout() );
				}

				logServer = new LogServerWorker( socket, this, project );
			}
			catch ( IOException e )
			{
				LOG.warn( e );
				Notifications.Bus.notify( new Notification( "smeetLogger", "Server start failed", "could not start the server" + e.getMessage(), NotificationType.WARNING ) );
			}
		}
		if ( logServer != null && !logServer.isRunning() )
		{
			logServer.start();
		}
	}

	public void stopSocketThread()
	{
		LOG.info( "stop socket server" );
		if ( logServer != null )
		{
			logServer.stop();
			logServer = null;
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

package de.tslarusso.logger.view;

import com.intellij.execution.RunManagerEx;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.components.ProjectComponent;
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

public class SmeetLoggerComponent implements ProjectComponent, ResponseListener, AnActionListener, ContentManagerListener
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
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance( project );
		ToolWindow toolWindow = toolWindowManager.getToolWindow( TOOLWINDOW_ID );
		if ( toolWindow == null )
		{
			toolWindow = toolWindowManager.registerToolWindow( TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM );
			toolWindow.setIcon( IconLoader.getIcon( "/ide/notifications.png" ) );
			toolWindow.setSplitMode( true, null );
			toolWindow.getContentManager().addContentManagerListener( this );
		}

		final ContentFactory contentFactory = toolWindow.getContentManager().getFactory();
		final SmeetLoggerWindow ui = new SmeetLoggerWindow( project );
		final Content loggerWindow = contentFactory.createContent( ui, "Logger", true );
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
		String actionId = actionManager.getId( anAction );
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
		else if ( anAction.toString().equals( "Close (null)" ) )
		{
			stopSocketThread();
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
		ToolWindowManager.getInstance( project ).getToolWindow( TOOLWINDOW_ID ).show( null );
	}

	public void contentRemoved( ContentManagerEvent contentManagerEvent )
	{
		if ( contentManagerEvent.getContent().getManager().getContentCount() == 0 )
		{
			ToolWindow toolWindow = ToolWindowManager.getInstance( project ).getToolWindow( TOOLWINDOW_ID );
			toolWindow.getContentManager().removeContentManagerListener( this );
			ToolWindowManager.getInstance( project ).unregisterToolWindow( TOOLWINDOW_ID );
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

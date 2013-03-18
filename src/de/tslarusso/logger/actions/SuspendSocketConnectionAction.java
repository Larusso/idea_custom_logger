package de.tslarusso.logger.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.SmeetLoggerComponent;

public class SuspendSocketConnectionAction extends AnAction
{
	public SuspendSocketConnectionAction()
	{
		super( "close connections", "close all socket connections", AllIcons.Actions.Suspend );
	}

	@Override
	public void update( AnActionEvent event )
	{
		//check if a socket connection is running
		Project project = event.getProject();

		Presentation presentation = event.getPresentation();
		presentation.setVisible( false );
		presentation.setEnabled( false );

		if ( project != null && project.hasComponent( SmeetLoggerComponent.class ) )
		{
			SmeetLoggerComponent loggerComponent = project.getComponent( SmeetLoggerComponent.class );
			event.getPresentation().setVisible( true );
			event.getPresentation().setEnabled( true );

			if ( loggerComponent.isLoggerRunning() )
			{
				presentation.setDescription( "close all socket connections" );
				presentation.setText( "close connections" );
				presentation.setIcon( AllIcons.Actions.Suspend );
			}
			else
			{
				presentation.setDescription( "start socket connections" );
				presentation.setText( "start logger" );
				presentation.setIcon( AllIcons.General.Run );
			}
		}

		super.update( event );
	}

	@Override
	public void actionPerformed( AnActionEvent event )
	{
		Project project = event.getProject();
		if ( project.hasComponent( SmeetLoggerComponent.class ) )
		{
			SmeetLoggerComponent smeetLoggerComponent = project.getComponent( SmeetLoggerComponent.class );
			if ( smeetLoggerComponent.isLoggerRunning() )
			{
				smeetLoggerComponent.stopSocketThread();
			}
			else
			{
				smeetLoggerComponent.startSocketThread();
			}
		}
	}
}

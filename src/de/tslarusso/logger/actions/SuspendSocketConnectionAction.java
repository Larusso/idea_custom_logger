package de.tslarusso.logger.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.SmeetLoggerComponent;

public class SuspendSocketConnectionAction extends AnAction
{
	public SuspendSocketConnectionAction()
	{
		super( "close connections", "close all socket connections", AllIcons.Actions.Suspend );
	}

	@Override
	public void actionPerformed( AnActionEvent event )
	{
		Project project = event.getProject();
		if ( project.hasComponent( SmeetLoggerComponent.class ) )
		{
			SmeetLoggerComponent smeetLoggerComponent = project.getComponent( SmeetLoggerComponent.class );
			smeetLoggerComponent.stopSocketThread();
		}
	}
}

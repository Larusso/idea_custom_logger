package de.tslarusso.logger;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.runners.RestartAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SmeetLoggerActionListener implements ApplicationComponent, AnActionListener
{
	@NotNull
	public String getComponentName()
	{
		return "smeetLogger.SmeetLoggerActionListener";
	}

	public void disposeComponent()
	{
		ActionManager actionManager = ActionManager.getInstance();
		actionManager.removeAnActionListener( this );
	}

	public void initComponent()
	{
		ActionManager actionManager = ActionManager.getInstance();
		actionManager.addAnActionListener( this );
	}

	///////////////////////////////////////////////
	//  AnActionListener implementation
	//////////////////////////////////////////////

	public void afterActionPerformed( AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent )
	{
	}

	public void beforeActionPerformed( AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent )
	{
		Project project = anActionEvent.getData( PlatformDataKeys.PROJECT );
		ActionManager actionManager = ActionManager.getInstance();
		SmeetLoggerSettings settings = ServiceManager.getService( project, SmeetLoggerSettings.class );
		SmeetLoggerComponent smeetLoggerComponent = project.getComponent( SmeetLoggerComponent.class );

		if ( settings.isAutoStartConnection() )
		{
			String actionId = actionManager.getId( anAction );

			//dont start server if the related project is not the current project!

			if ( actionId != null )
			{
				if ( actionId.equals( IdeActions.ACTION_DEFAULT_RUNNER ) || actionId.equals( IdeActions.ACTION_DEFAULT_DEBUGGER ) )
				{
					//start socket only for flash configurations
					if ( RunManagerEx.getInstance( anActionEvent.getProject() ).getSelectedConfiguration().getConfiguration().getType().getId().equals( "FlashRunConfigurationType" ) )
					{
						smeetLoggerComponent.startSocketThread();
					}
				}

				if ( actionId.equals( IdeActions.ACTION_STOP_PROGRAM ) )
				{
					smeetLoggerComponent.stopSocketThread();
				}
			}
			else if ( anAction.getClass().equals( RestartAction.class ) )
			{
				//start socket only for flash configurations
				if ( RunManagerEx.getInstance( anActionEvent.getProject() ).getSelectedConfiguration().getConfiguration().getType().getId().equals( "FlashRunConfigurationType" ) )
				{
					smeetLoggerComponent.startSocketThread();
				}
			}
			else if ( anAction.toString().equals( "Close (null)" ) )
			{
				smeetLoggerComponent.stopSocketThread();
			}
		}
	}

	public void beforeEditorTyping( char c, DataContext dataContext )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}
}

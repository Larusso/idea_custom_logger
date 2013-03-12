package de.tslarusso.logger.workers;

import com.intellij.openapi.project.Project;
import de.tslarusso.logger.SmeetLoggerComponent;

public class ShutDownRunner implements Runnable
{

	private final Project project;

	public ShutDownRunner( Project project )
	{
		this.project = project;
	}

	public void run()
	{
		if ( project.hasComponent( SmeetLoggerComponent.class ) )
		{
			SmeetLoggerComponent smeetLoggerComponent = project.getComponent( SmeetLoggerComponent.class );
			smeetLoggerComponent.stopSocketThread();
		}
	}
}

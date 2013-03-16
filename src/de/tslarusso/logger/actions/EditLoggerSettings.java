package de.tslarusso.logger.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.SmeetLoggerSettingsComponent;

public class EditLoggerSettings extends DumbAwareAction
{
	public EditLoggerSettings()
	{
		super( "Settings", "Edit Smeet Logger settings", AllIcons.General.Settings );
	}

	@Override
	public void actionPerformed( AnActionEvent e )
	{
		Project project = e.getProject();
		ShowSettingsUtil.getInstance().editConfigurable( project, new SmeetLoggerSettingsComponent( project ) );
	}
}

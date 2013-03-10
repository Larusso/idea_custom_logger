package de.tslarusso.logger;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.view.ui.SettingsFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SmeetLoggerSettingsComponent implements Configurable
{
	private SettingsFactory settingDialog;
	private final Project project;
	private final SmeetLoggerSettings settings;

	public SmeetLoggerSettingsComponent( Project project )
	{
		this.project = project;
		this.settings = SmeetLoggerSettings.getSafeInstance( project );
	}

	@Nls
	public String getDisplayName()
	{
		return "Smeet Logger";
	}

	@Nullable
	public String getHelpTopic()
	{
		return null;
	}

	@Nullable
	public JComponent createComponent()
	{
		settingDialog = new SettingsFactory();
		settingDialog.setData( settings );
		return settingDialog.getPanel();
	}

	public boolean isModified()
	{
		return settingDialog.isModified( settings );
	}

	public void apply() throws ConfigurationException
	{
		settingDialog.getData( settings );
	}

	public void reset()
	{
		settingDialog.setData( settings );
	}

	public void disposeUIResources()
	{
		settingDialog = null;
	}
}

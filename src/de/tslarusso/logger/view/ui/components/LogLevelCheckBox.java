package de.tslarusso.logger.view.ui.components;

import com.intellij.ui.components.JBCheckBox;
import de.tslarusso.logger.model.SmeetLogLevel;

public class LogLevelCheckBox extends JBCheckBox
{
	private SmeetLogLevel level;

	public SmeetLogLevel getLogLevel()
	{
		return level;
	}

	public LogLevelCheckBox( SmeetLogLevel level, String name )
	{
		super( name );
		level = level;
	}
}

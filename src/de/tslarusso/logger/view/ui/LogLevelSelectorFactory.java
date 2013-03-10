package de.tslarusso.logger.view.ui;

import com.intellij.ui.components.JBCheckBox;
import de.tslarusso.logger.model.SmeetLogLevel;
import de.tslarusso.logger.view.LogLevelSelector;
import de.tslarusso.logger.view.ui.components.LogLevelCheckBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogLevelSelectorFactory implements ActionListener, LogLevelSelector
{
	private JPanel logLevelPanal;

	private JBCheckBox debugButton;
	private JBCheckBox infoButton;
	private JBCheckBox warningButton;
	private JBCheckBox errorButton;
	private JBCheckBox fatalButton;

	//--------------------------------------------
	//  logLevel
	//
	//--------------------------------------------

	private int logLevel;

	public int getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel( int logLevel )
	{
		this.logLevel = logLevel;
		updateView();
	}

	LogLevelSelectorFactory()
	{

	}

	private void updateView()
	{
		Component[] components = logLevelPanal.getComponents();

		for ( int i = 0; i < components.length; i++ )
		{
			LogLevelCheckBox checkBox = ( LogLevelCheckBox ) components[ i ];
			checkBox.setSelected( ( ( logLevel & checkBox.getLogLevel().getLevel() ) > 0 ) );
		}
	}


	///////////////////////////////////////////////
	//  ActionListener implementation
	//////////////////////////////////////////////

	public void actionPerformed( ActionEvent actionEvent )
	{
		int level = 0;
		Component[] components = logLevelPanal.getComponents();
		for ( int i = 0; i < components.length; i++ )
		{
			LogLevelCheckBox checkBox = ( LogLevelCheckBox ) components[ i ];

			if ( checkBox.isSelected() )
			{
				level |= checkBox.getLogLevel().getLevel();
			}
		}

		logLevel = level;
	}

	private void createUIComponents()
	{
		debugButton = new LogLevelCheckBox( SmeetLogLevel.DEBUG, "debug" );
		infoButton = new LogLevelCheckBox( SmeetLogLevel.INFO, "info" );
		warningButton = new LogLevelCheckBox( SmeetLogLevel.WARN, "warning" );
		errorButton = new LogLevelCheckBox( SmeetLogLevel.ERROR, "error" );
		fatalButton = new LogLevelCheckBox( SmeetLogLevel.FATAL, "fatal" );
	}
}

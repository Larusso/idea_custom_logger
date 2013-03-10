package de.tslarusso.logger.view.ui;

import de.tslarusso.logger.SmeetLoggerSettings;
import de.tslarusso.logger.view.LogLevelSelector;

import javax.swing.*;
import java.text.DecimalFormat;

public class SettingsFactory implements LogLevelSelector
{
	private JPanel panel;
	private JFormattedTextField portInput;
	private JFormattedTextField timeoutInput;
	private LogLevelSelectorFactory logLevelSelector;
	private JCheckBox autoFoldMultilineMessagesCheckBox;
	private JCheckBox autoStartServerOnCheckBox;
	private LogFilterSelectorFactory logFilterSelector;

	public JPanel getPanel()
	{
		return panel;
	}

	private void createUIComponents()
	{
		DecimalFormat portFormat = new DecimalFormat( "0000" );
		portInput = new JFormattedTextField( portFormat );

		DecimalFormat timeoutFormat = new DecimalFormat( "0000000" );
		timeoutInput = new JFormattedTextField( timeoutFormat );
	}

	///////////////////////////////////////////////
	//  LogLevelSelector implementation
	//////////////////////////////////////////////

	public void setLogLevel( int logLevel )
	{
		logLevelSelector.setLogLevel( logLevel );
	}

	public int getLogLevel()
	{
		return logLevelSelector.getLogLevel();
	}

	public void setData( SmeetLoggerSettings data )
	{
		portInput.setText( Integer.toString( data.getConnectionPort() ) );
		timeoutInput.setText( Integer.toString( data.getConnectionTimeout() ) );
		autoFoldMultilineMessagesCheckBox.setSelected( data.isAutoFoldMultilineMessages() );
		autoStartServerOnCheckBox.setSelected( data.isAutoStartConnection() );
		logFilterSelector.setData( data );
	}

	public void getData( SmeetLoggerSettings data )
	{
		data.setConnectionPort( Integer.parseInt( portInput.getText() ) );
		data.setConnectionTimeout( Integer.parseInt( timeoutInput.getText() ) );
		data.setAutoFoldMultilineMessages( autoFoldMultilineMessagesCheckBox.isSelected() );
		data.setAutoStartConnection( autoStartServerOnCheckBox.isSelected() );
		logFilterSelector.getData( data );
	}

	public boolean isModified( SmeetLoggerSettings data )
	{
		if ( portInput.getText() != null ? !portInput.getText().equals( Integer.toString( data.getConnectionPort() ) ) : Integer.toString( data.getConnectionPort() ) != null )
		{
			return true;
		}
		if ( timeoutInput.getText() != null ? !timeoutInput.getText().equals( Integer.toString( data.getConnectionTimeout() ) ) : Integer.toString( data.getConnectionTimeout() ) != null )
		{
			return true;
		}
		if ( autoFoldMultilineMessagesCheckBox.isSelected() != data.isAutoFoldMultilineMessages() )
		{
			return true;
		}
		if ( autoStartServerOnCheckBox.isSelected() != data.isAutoStartConnection() )
		{
			return true;
		}

		if ( logFilterSelector.isModified( data ) )
		{
			return true;
		}

		return false;
	}
}

package de.tslarusso.logger.view.ui;

import com.intellij.ui.components.JBCheckBox;
import de.tslarusso.logger.model.LogFilter;

import javax.swing.*;

public class LogFilterSelectorFactory
{
	private JTextField filterField;
	private JPanel filterPanel;
	private JBCheckBox matchCaseCheckBox;
	private JBCheckBox regExCheckBox;

	public LogFilterSelectorFactory()
	{

	}

	public void setData( LogFilter data )
	{
		filterField.setText( data.getPattern() );
		matchCaseCheckBox.setSelected( data.isMatchingCase() );
		regExCheckBox.setSelected( data.isRegExp() );
	}

	public void getData( LogFilter data )
	{
		data.setPattern( filterField.getText() );
		data.setMatchingCase( matchCaseCheckBox.isSelected() );
		data.setRegExp( regExCheckBox.isSelected() );
	}

	public boolean isModified( LogFilter data )
	{
		if ( filterField.getText() != null ? !filterField.getText().equals( data.getPattern() ) : data.getPattern() != null )
		{
			return true;
		}
		if ( matchCaseCheckBox.isSelected() != data.isMatchingCase() )
		{
			return true;
		}
		if ( regExCheckBox.isSelected() != data.isRegExp() )
		{
			return true;
		}
		return false;
	}

	public JPanel getFilterPanel()
	{
		return filterPanel;
	}
}

package de.tslarusso.logger.view.ui;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.SmeetLoggerComponent;
import de.tslarusso.logger.SmeetLoggerSettings;
import de.tslarusso.logger.actions.ClearAllAction;
import de.tslarusso.logger.actions.PrintConsoleAction;
import de.tslarusso.logger.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LogWindow
{
	private final Project project;
	private final Document document;
	private final Editor editor;
	private final SmeetLoggerSettings settings;

	private JPanel logPanel;
	private JPanel editorContainer;
	private JToolBar mainToolBar;
	private LogFilterSelectorFactory logFilterSelector;
	private LogLevelSelectorFactory logLevelSelector;
	private JToolBar editorToolbar;
	private JButton applyButton;

	private List<LogMessage> messages;
	private List<LogMessage> filteredMessages;
	private Predicate<LogMessage> filterPredicate;
	private LogFilterImpl logFilter;

	private static class LogFilterImpl implements LogFilter
	{
		private String pattern;

		public String getPattern()
		{
			return pattern;  //To change body of implemented methods use File | Settings | File Templates.
		}

		public void setPattern( String pattern )
		{
			this.pattern = pattern;
		}

		private Boolean matchingCase;

		public Boolean isMatchingCase()
		{
			return matchingCase;  //To change body of implemented methods use File | Settings | File Templates.
		}

		public void setMatchingCase( Boolean matchingCase )
		{
			this.matchingCase = matchingCase;
		}

		private Boolean regExp;

		public Boolean isRegExp()
		{
			return regExp;
		}

		public void setRegExp( Boolean regExp )
		{
			this.regExp = regExp;
		}

		LogFilterImpl( SmeetLoggerSettings settings )
		{
			pattern = settings.getPattern();
			matchingCase = settings.isMatchingCase();
			regExp = settings.isRegExp();
		}
	}

	private int logLevel;

	public LogWindow( final Project project, final SmeetLoggerSettings settings )
	{
		this.project = project;
		this.settings = settings;

		this.logFilter = new LogFilterImpl( settings );
		this.logLevel = settings.getLogLevel();

		filterPredicate = new LogMessagePredicate( logLevel, logFilter, project );

		document = EditorFactory.getInstance().createDocument( "" );
		editor = EditorFactory.getInstance().createEditor( document, project );

		ActionGroup toolbarGroup = ( ActionGroup ) ActionManager.getInstance().getAction( "SmeetLogger.ToolBar" );
		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar( SmeetLoggerComponent.TOOLWINDOW_ID, toolbarGroup, false );

		mainToolBar.add( actionToolbar.getComponent() );
		mainToolBar.setPreferredSize( new Dimension( 30, -1 ) );
		mainToolBar.setMargin( new Insets( 0, 0, 0, 0 ) );
		mainToolBar.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		ActionToolbar editorActionToolbar = createToolbar( project, editor );
		JComponent component = editorActionToolbar.getComponent();

		editorToolbar = new JToolBar();
		editorToolbar.add( component );
		editorToolbar.setPreferredSize( new Dimension( 30, -1 ) );
		editorToolbar.setMargin( new Insets( 0, 0, 0, 0 ) );
		editorToolbar.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		editor.getSettings().setAdditionalPageAtBottom( false );
		editor.getSettings().setRightMarginShown( false );
		editor.getSettings().setLineMarkerAreaShown( false );
		editor.getSettings().setAnimatedScrolling( false );
		editor.getSettings().setRefrainFromScrolling( false );
		editor.getSettings().setAdditionalLinesCount( 0 );
		editor.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		editor.getCaretModel().moveToLogicalPosition( new LogicalPosition( document.getLineCount(), 1 ) );
		editorContainer.add( editor.getComponent(), BorderLayout.CENTER );

		editor.getComponent().add( editorToolbar, BorderLayout.WEST );
		editor.getComponent().setBorder( BorderFactory.createLineBorder( editor.getColorsScheme().getDefaultBackground() ) );
		logFilterSelector.setData( settings );
		logLevelSelector.setLogLevel( settings.getLogLevel() );

		applyButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent actionEvent )
			{
				if ( logFilterSelector.isModified( logFilter ) || logLevelSelector.getLogLevel() != logLevel )
				{
					logFilterSelector.getData( logFilter );
					logLevel = logLevelSelector.getLogLevel();

					updateFilteredList( logLevel, logFilter );
				}
			}
		} );
	}

	public void addMessage( final LogMessage message )
	{
		if ( messages == null )
		{
			messages = new ArrayList<LogMessage>();
		}

		messages.add( message );

		if ( messages.size() > 4000 )
		{
			messages.remove( 0 );
		}

		if ( filterPredicate.apply( message ) )
		{
			java.util.List<LogMessage> updateList = new ArrayList<LogMessage>( 1 );
			updateList.add( message );
			updateDocument( updateList );
		}
	}

	private void updateFilteredList( final int logLevel, final LogFilter filter )
	{
		if ( messages != null )
		{
			filterPredicate = new LogMessagePredicate( logLevel, filter, project );
			filteredMessages = Lists.newArrayList( Iterables.filter( messages, filterPredicate ) );

			updateDocument( filteredMessages, true );
		}
	}

	private void updateDocument( final List<LogMessage> messages )
	{
		updateDocument( messages, false );
	}

	private void updateDocument( final List<LogMessage> messages, final Boolean clearDocument )
	{
		CommandProcessor.getInstance().executeCommand( project, new Runnable()
		{
			public void run()
			{
				ApplicationManager.getApplication().runWriteAction( new Runnable()
				{
					public void run()
					{
						if ( clearDocument )
						{
							document.setText( "" );
							editor.getCaretModel().moveToLogicalPosition( new LogicalPosition( document.getLineCount(), 1 ) );
						}

						Boolean resetCaret = editor.getCaretModel().getLogicalPosition().line == Math.max( document.getLineCount() - 1, 0 );

						for ( int i = 0; i < messages.size(); i++ )
						{
							LogMessage message = messages.get( i );
							final int textLengthBefore = document.getTextLength();
							document.insertString( document.getTextLength(), message + "\n" );
							final TextAttributes textattributes = getAttributesForLogLevel( message.getLevel() );
							editor.getMarkupModel().addRangeHighlighter( textLengthBefore, document.getTextLength(), 0, textattributes, HighlighterTargetArea.EXACT_RANGE );

							if ( message.getType().equals( SmeetLogType.SHOW_FOLD_MESSAGE ) )
							{
								final String firstLine = message.toString().substring( 0, message.toString().indexOf( "\n" ) );
								editor.getFoldingModel().runBatchFoldingOperation( new Runnable()
								{
									public void run()
									{
										FoldRegion fr = editor.getFoldingModel().addFoldRegion( textLengthBefore, document.getTextLength() - 1, firstLine + " ->" );
										fr.setExpanded( !settings.isAutoFoldMultilineMessages() );
									}
								} );
							}
						}

						if ( resetCaret )
						{
							int offset = Math.max( document.getLineCount() - 1, 0 );
							editor.getCaretModel().moveToOffset( document.getLineEndOffset( offset ) );
							editor.getScrollingModel().scrollToCaret( ScrollType.CENTER_DOWN );
						}
					}
				} );
			}
		}, null, null, document );
	}

	private TextAttributes getAttributesForLogLevel( SmeetLogLevel level )
	{
		TextAttributes value = null;
		final EditorColorsManager editorColorsManager = EditorColorsManager.getInstance();
		final EditorColorsScheme globalScheme = editorColorsManager.getGlobalScheme();
		switch ( level )
		{
			case ALL:
			case DEBUG:
			case INFO:
				value = globalScheme.getAttributes( ConsoleViewContentType.NORMAL_OUTPUT_KEY );
				break;
			case ERROR:
				value = globalScheme.getAttributes( ConsoleViewContentType.ERROR_OUTPUT_KEY );
				break;
			case FATAL:
				value = globalScheme.getAttributes( ConsoleViewContentType.ERROR_OUTPUT_KEY );
				break;
			case WARN:
				value = globalScheme.getAttributes( ConsoleViewContentType.LOG_WARNING_OUTPUT_KEY );
				break;
		}

		return value;
	}

	private static ActionToolbar createToolbar( Project project, Editor editor )
	{
		DefaultActionGroup group = new DefaultActionGroup();
		group.add( new ToggleSoftWraps( editor ) );
		group.add( new ScrollToTheEndToolbarAction( editor ) );
		group.add( new PrintConsoleAction( editor ) );
		group.add( new ClearAllAction( editor ) );

		return ActionManager.getInstance().createActionToolbar( ActionPlaces.UNKNOWN, group, false );
	}

	public JPanel getLogPanel()
	{
		return logPanel;
	}

	private static class ToggleSoftWraps extends ToggleUseSoftWrapsToolbarAction
	{
		private final Editor myEditor;

		public ToggleSoftWraps( Editor editor )
		{
			super( SoftWrapAppliancePlaces.CONSOLE );
			myEditor = editor;
		}

		@Override
		protected Editor getEditor( AnActionEvent e )
		{
			return myEditor;
		}
	}

}

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
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import de.tslarusso.logger.actions.ClearAllAction;
import de.tslarusso.logger.actions.PrintConsoleAction;
import de.tslarusso.logger.model.LogMessage;
import de.tslarusso.logger.model.LogMessagePredicate;
import de.tslarusso.logger.model.SmeetLogLevel;
import de.tslarusso.logger.model.SmeetLogType;
import de.tslarusso.logger.SmeetLoggerComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;


public class SmeetLoggerWindow extends JPanel
{
	private final Project project;
	private final Document dc;
	private final Editor editor;
	JBTextField filterInput;

	private JComponent output;
	private StringBuilder stringBuilder;
	private List<LogMessage> messages;
	private List<LogMessage> filteredMessages;
	private Predicate<LogMessage> filterPredicate;
	private Map<JBCheckBox, SmeetLogLevel> logLevelCheckBoxMap;
	private boolean autoscrolls;

	@Override
	public void setAutoscrolls( boolean b )
	{
		autoscrolls = b;
	}

	@Override
	public boolean getAutoscrolls()
	{
		return autoscrolls;
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

	private static ActionToolbar createToolbar( Project project, Editor editor )
	{
		DefaultActionGroup group = new DefaultActionGroup();
		group.add( new ToggleSoftWraps( editor ) );
		group.add( new ScrollToTheEndToolbarAction( editor ) );
		group.add( new PrintConsoleAction( editor ) );
		group.add( new ClearAllAction( editor ) );

		return ActionManager.getInstance().createActionToolbar( ActionPlaces.UNKNOWN, group, false );
	}

	public SmeetLoggerWindow( final Project project )
	{
		this.project = project;
		this.stringBuilder = new StringBuilder();
		logLevelCheckBoxMap = new HashMap<JBCheckBox, SmeetLogLevel>( 5 );

		dc = EditorFactory.getInstance().createDocument( "" );
		editor = EditorFactory.getInstance().createEditor( dc, project );

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 4, 4, 4, 4 ) );

		ActionGroup toolbarGroup = ( ActionGroup ) ActionManager.getInstance().getAction( "SmeetLogger.ToolBar" );
		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar( SmeetLoggerComponent.TOOLWINDOW_ID, toolbarGroup, false );

		JToolBar toolBar = new JToolBar( SwingConstants.VERTICAL );
		toolBar.setFloatable( false );
		toolBar.add( actionToolbar.getComponent() );
		toolBar.setPreferredSize( new Dimension( 30, -1 ) );
		toolBar.setMargin( new Insets( 0, 0, 0, 0 ) );
		toolBar.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		ActionToolbar consoleActionToolbar = createToolbar( project, editor );
		JComponent test = consoleActionToolbar.getComponent();
		JPanel editorGroup = new JPanel( new BorderLayout() );

		JToolBar consoleToolBar = new JToolBar( SwingConstants.VERTICAL );
		consoleToolBar.setFloatable( false );
		consoleToolBar.add( test );
		consoleToolBar.setPreferredSize( new Dimension( 30, -1 ) );
		consoleToolBar.setMargin( new Insets( 0, 0, 0, 0 ) );
		consoleToolBar.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		editor.getSettings().setAdditionalPageAtBottom( false );
		editor.getSettings().setRightMarginShown( false );
		editor.getSettings().setLineMarkerAreaShown( false );
		editor.getSettings().setAnimatedScrolling( false );
		editor.getSettings().setRefrainFromScrolling( false );
		editor.getSettings().setAdditionalLinesCount( 0 );

		editor.getCaretModel().moveToLogicalPosition( new LogicalPosition( dc.getLineCount(), 1 ) );

		output = editor.getComponent();

		editorGroup.add( consoleToolBar, BorderLayout.WEST );
		editorGroup.add( output, BorderLayout.CENTER );
		editorGroup.setBorder( BorderFactory.createLineBorder( editor.getColorsScheme().getDefaultBackground() ) );

		JPanel southPanel = new JPanel( new BorderLayout() );

		JPanel levelSelectorPanel = new JPanel();
		levelSelectorPanel.setLayout( new FlowLayout() );

		JLabel levelInfo = new JLabel( "level:" );
		JBCheckBox debugButton = new JBCheckBox( "debug" );
		debugButton.setSelected( true );
		logLevelCheckBoxMap.put( debugButton, SmeetLogLevel.DEBUG );

		JBCheckBox infoButton = new JBCheckBox( "info" );
		infoButton.setSelected( true );
		logLevelCheckBoxMap.put( infoButton, SmeetLogLevel.INFO );

		JBCheckBox warningButton = new JBCheckBox( "warning" );
		warningButton.setSelected( true );
		logLevelCheckBoxMap.put( warningButton, SmeetLogLevel.WARN );

		JBCheckBox errorButton = new JBCheckBox( "error" );
		errorButton.setSelected( true );
		logLevelCheckBoxMap.put( errorButton, SmeetLogLevel.ERROR );

		JBCheckBox fatalButton = new JBCheckBox( "fatal" );
		fatalButton.setSelected( true );
		logLevelCheckBoxMap.put( fatalButton, SmeetLogLevel.FATAL );

		ActionListener filterActionListener = new ActionListener()
		{
			public void actionPerformed( ActionEvent actionEvent )
			{
				final String regex = filterInput.getText();
				int level = 0;

				Iterator keys = logLevelCheckBoxMap.keySet().iterator();
				while ( keys.hasNext() )
				{
					JBCheckBox checkBox = ( JBCheckBox ) keys.next();
					if ( checkBox.isSelected() )
					{
						level |= logLevelCheckBoxMap.get( checkBox ).getLevel();
					}
				}

				updateFilteredList( level, regex );
			}
		};

		debugButton.addActionListener( filterActionListener );
		infoButton.addActionListener( filterActionListener );
		warningButton.addActionListener( filterActionListener );
		errorButton.addActionListener( filterActionListener );
		fatalButton.addActionListener( filterActionListener );

		levelSelectorPanel.add( levelInfo );
		levelSelectorPanel.add( debugButton );
		levelSelectorPanel.add( infoButton );
		levelSelectorPanel.add( warningButton );
		levelSelectorPanel.add( errorButton );
		levelSelectorPanel.add( fatalButton );

		JPanel filterPanel = new JPanel( new BorderLayout( 3, 3 ) );
		filterPanel.setBorder( new EmptyBorder( 0, 12, 0, 3 ) );

		filterInput = new JBTextField( ".*?" );
		filterInput.getPreferredSize();
		filterInput.setPreferredSize( new Dimension( filterInput.getPreferredSize().width, 10 ) );

		JButton filterButton = new JButton( "apply" );

		filterButton.addActionListener( filterActionListener );

		filterButton.setPreferredSize( new Dimension( filterButton.getPreferredSize().width, 22 ) );

		filterPanel.add( new JLabel( "filter:" ), BorderLayout.WEST );
		filterPanel.add( filterInput, BorderLayout.CENTER );
		filterPanel.add( filterButton, BorderLayout.EAST );

		southPanel.add( levelSelectorPanel, BorderLayout.WEST );
		southPanel.add( filterPanel, BorderLayout.CENTER );

		add( toolBar, BorderLayout.WEST );
		add( editorGroup, BorderLayout.CENTER );
		add( southPanel, BorderLayout.SOUTH );

		setAutoscrolls( true );
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

		if ( filterPredicate == null || filterPredicate.apply( message ) )
		{
			List<LogMessage> updateList = new ArrayList<LogMessage>( 1 );
			updateList.add( message );
			updateDocument( updateList );
		}
	}

	private void updateFilteredList( int levelFlag, String regex )
	{
		if ( messages != null )
		{
			filterPredicate = new LogMessagePredicate( levelFlag, regex );
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
							dc.setText( "" );
							editor.getCaretModel().moveToLogicalPosition( new LogicalPosition( dc.getLineCount(), 1 ) );
						}

						Boolean resetCaret = editor.getCaretModel().getLogicalPosition().line == Math.max( dc.getLineCount() - 1, 0 );

						for ( int i = 0; i < messages.size(); i++ )
						{
							LogMessage message = messages.get( i );
							final int textLengthBefore = dc.getTextLength();
							dc.insertString( dc.getTextLength(), message + "\n" );
							final TextAttributes textattributes = getAttributesForLogLevel( message.getLevel() );
							editor.getMarkupModel().addRangeHighlighter( textLengthBefore, dc.getTextLength(), 0, textattributes, HighlighterTargetArea.EXACT_RANGE );

							if ( message.getType().equals( SmeetLogType.SHOW_FOLD_MESSAGE ) )
							{
								final String firstLine = message.toString().substring( 0, message.toString().indexOf( "\n" ) );
								editor.getFoldingModel().runBatchFoldingOperation( new Runnable()
								{
									public void run()
									{
										FoldRegion fr = editor.getFoldingModel().addFoldRegion( textLengthBefore, dc.getTextLength() - 1, firstLine + " ->" );
										fr.setExpanded( false );
									}
								} );
							}
						}

						if ( resetCaret )
						{
							editor.getCaretModel().moveToOffset( dc.getLineEndOffset( dc.getLineCount() - 1 ) );
							editor.getScrollingModel().scrollToCaret( ScrollType.CENTER_DOWN );
						}
					}
				} );
			}
		}, null, null, dc );
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
}

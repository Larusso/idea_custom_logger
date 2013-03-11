package de.tslarusso.logger.view.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.project.Project;
import de.tslarusso.logger.SmeetLoggerComponent;
import de.tslarusso.logger.actions.ClearAllAction;
import de.tslarusso.logger.actions.PrintConsoleAction;

import javax.swing.*;
import java.awt.*;

public class LogWindow
{
	private final Project project;
	private final Document document;
	private final Editor editor;

	private JPanel logPanel;
	private JPanel editorContainer;
	private JToolBar mainToolBar;
	private LogFilterSelectorFactory logFilterSelector;
	private LogLevelSelectorFactory logLevelSelector;
	private JToolBar editorToolbar;

	public LogWindow( final Project project )
	{
		this.project = project;
		document = EditorFactory.getInstance().createDocument( "" );
		editor = EditorFactory.getInstance().createEditor( document, project );

		ActionGroup toolbarGroup = ( ActionGroup ) ActionManager.getInstance().getAction( "SmeetLogger.ToolBar" );
		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar( SmeetLoggerComponent.TOOLWINDOW_ID, toolbarGroup, false );

		mainToolBar.add( actionToolbar.getComponent() );

		ActionToolbar editorActionToolbar = createToolbar( project, editor );
		JComponent component = editorActionToolbar.getComponent();

		editorToolbar.add( component );

		editor.getSettings().setAdditionalPageAtBottom( false );
		editor.getSettings().setRightMarginShown( false );
		editor.getSettings().setLineMarkerAreaShown( false );
		editor.getSettings().setAnimatedScrolling( false );
		editor.getSettings().setRefrainFromScrolling( false );
		editor.getSettings().setAdditionalLinesCount( 0 );

		editor.getCaretModel().moveToLogicalPosition( new LogicalPosition( document.getLineCount(), 1 ) );
		editorContainer.add( editor.getComponent(), BorderLayout.CENTER );
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

package de.tslarusso.logger.actions;

import com.intellij.execution.ExecutionBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;

public class ClearAllAction extends DumbAwareAction
{
	private final Editor editor;

	public ClearAllAction( Editor editor )
	{
		super( ExecutionBundle.message( "clear.all.from.console.action.name" ), "Clear the contents of the console", AllIcons.Actions.GC );
		this.editor = editor;
	}

	@Override
	public void actionPerformed( final AnActionEvent e )
	{
		final Document document = editor.getDocument();
		if ( editor != null )
		{
			CommandProcessor.getInstance().executeCommand( editor.getProject(), new Runnable()
			{
				public void run()
				{
					ApplicationManager.getApplication().runWriteAction( new Runnable()
					{
						public void run()
						{
							document.setText( "" );
						}
					} );
				}
			}, null, null, document );
		}
	}
}
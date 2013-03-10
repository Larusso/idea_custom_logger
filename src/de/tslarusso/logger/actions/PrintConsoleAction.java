package de.tslarusso.logger.actions;

import com.intellij.codeEditor.printing.PrintAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;

public class PrintConsoleAction extends PrintAction
{
	private final Editor editor;

	public PrintConsoleAction( Editor editor )
	{
		super();
		this.editor = editor;
	}

	@Override
	public void update( AnActionEvent anActionEvent )
	{
		super.update( anActionEvent );
		Presentation presentation = anActionEvent.getPresentation();
		presentation.setIcon( AllIcons.Graph.Print );
		presentation.setText( "print" );
		presentation.setDescription( "print" );
	}
}

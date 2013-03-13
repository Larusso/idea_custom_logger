package de.tslarusso.logger.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import org.jetbrains.annotations.Nullable;

public class ToggleSoftWraps extends ToggleUseSoftWrapsToolbarAction
{
	private final Editor editor;

	public ToggleSoftWraps( Editor editor )
	{
		super( SoftWrapAppliancePlaces.CONSOLE );
		this.editor = editor;
	}

	@Nullable
	@Override
	protected Editor getEditor( AnActionEvent anActionEvent )
	{
		return editor;
	}
}

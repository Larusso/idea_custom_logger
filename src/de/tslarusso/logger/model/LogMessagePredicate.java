package de.tslarusso.logger.model;

import com.google.common.base.Predicate;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LogMessagePredicate implements Predicate<LogMessage>
{
	private static Logger LOG = Logger.getInstance( LogMessagePredicate.class );

	private final int logLevelFlag;
	private Pattern pattern = null;
	private final Project project;

	public LogMessagePredicate( int logLevelFlag, LogFilter filter, Project project )
	{
		this.logLevelFlag = logLevelFlag;
		this.project = project;

		try
		{
			this.pattern = generatePattern( filter );
		}
		catch ( Exception e )
		{
			LOG.warn( String.format( "error compiling pattern %s", filter.getPattern() ), e );
			Notifications.Bus.notify( new Notification( "smeetLogger", "pattern error", e.getMessage(), NotificationType.ERROR ), project );
		}
	}

	public boolean apply( LogMessage logMessage )
	{
		if ( pattern != null )
		{
			Matcher m = pattern.matcher( logMessage.getMessage() );

			if ( m.matches() && ( logLevelFlag & logMessage.getLevel().getLevel() ) != 0 )
			{
				return true;
			}
		}
		return false;
	}

	private Pattern generatePattern( LogFilter filter ) throws PatternSyntaxException
	{
		Pattern pattern;
		int flags = Pattern.DOTALL;

		if ( !filter.isMatchingCase() )
		{
			flags |= Pattern.CASE_INSENSITIVE;
		}

		String patternString = filter.getPattern();

		if ( !filter.isRegExp() )
		{
			patternString = patternString.replaceAll( "\\?", "." );
			patternString = patternString.replaceAll( "\\*", ".*?" );
			patternString = StringUtil.escapePattern( patternString );
		}

		pattern = Pattern.compile( patternString, flags );

		return pattern;
	}
}

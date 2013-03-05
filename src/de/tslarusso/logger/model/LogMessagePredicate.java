package de.tslarusso.logger.model;

import com.google.common.base.Predicate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMessagePredicate implements Predicate<LogMessage>
{
	private final int logLevelFlag;
	private final Pattern pattern;

	public LogMessagePredicate( int logLevelFlag, String regex )
	{
		this.logLevelFlag = logLevelFlag;
		this.pattern = Pattern.compile( regex, Pattern.DOTALL );
	}

	public boolean apply( LogMessage logMessage )
	{
		Matcher m = pattern.matcher( logMessage.getMessage() );

		if ( m.matches() && ( logLevelFlag & logMessage.getLevel().getLevel() ) != 0 )
		{
			return true;
		}
		return false;
	}
}

package de.tslarusso.logger.model;

public class LogMessage
{
	private final SmeetLogLevel level;
	private final String message;
	private final SmeetLogType type;

	public LogMessage( String type, String message, String level )
	{
		this.type = SmeetLogType.getType( type );
		this.message = message;
		this.level = SmeetLogLevel.valueOf( level );
	}

	public SmeetLogLevel getLevel()
	{
		return level;
	}

	public String getMessage()
	{
		return message;
	}

	@Override
	public String toString()
	{
		return "[" + level + "] " + message;
	}

	public SmeetLogType getType()
	{
		return type;
	}
}

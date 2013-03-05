package de.tslarusso.logger.model;

public enum SmeetLogLevel
{
	ALL( 0x0001 ), DEBUG( 0x0002 ), INFO( 0x0004 ), WARN( 0x0008 ), ERROR( 0x0010 ), FATAL( 0x0020 );

	private int level;

	private SmeetLogLevel( int l )
	{
		level = l;
	}

	public static SmeetLogLevel getLevel( int l )
	{
		SmeetLogLevel result = SmeetLogLevel.ALL;
		for ( SmeetLogLevel level : values() )
		{
			if ( level.getLevel() == l )
			{
				result = level;
				break;
			}
		}
		return result;
	}

	public int getLevel()
	{
		return level;
	}
}

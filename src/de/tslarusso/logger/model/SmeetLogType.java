package de.tslarusso.logger.model;

public enum SmeetLogType
{
	SHOW_MESSAGE( "showMessage" ), SHOW_FOLD_MESSAGE( "showFoldMessage" );

	private String type;

	private SmeetLogType( String t )
	{
		type = t;
	}

	public static SmeetLogType getType( String t )
	{
		SmeetLogType result = SmeetLogType.SHOW_MESSAGE;
		for ( SmeetLogType level : values() )
		{
			if ( level.getType().equals( t ) )
			{
				result = level;
				break;
			}
		}
		return result;
	}

	public String getType()
	{
		return type;
	}
}

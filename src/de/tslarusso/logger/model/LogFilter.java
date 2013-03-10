package de.tslarusso.logger.model;

public interface LogFilter
{
	String getPattern();

	void setPattern( String pattern );

	Boolean isMatchingCase();

	void setMatchingCase( Boolean matchingCase );

	Boolean isRegExp();

	void setRegExp( Boolean regExp );
}

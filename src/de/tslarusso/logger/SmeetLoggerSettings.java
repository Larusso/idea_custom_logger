package de.tslarusso.logger;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import de.tslarusso.logger.model.LogFilter;
import de.tslarusso.logger.model.SmeetLogLevel;
import org.jetbrains.annotations.Nullable;

@State(
		      name = "SmeetLoggerConfiguration",
		      storages = {
				                 @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
				                 @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/smeetLogger.xml", scheme = StorageScheme.DIRECTORY_BASED)
		      }
)
public class SmeetLoggerSettings implements PersistentStateComponent<SmeetLoggerSettings.State>, LogFilter
{
	private State state = new State();

	public static SmeetLoggerSettings getSafeInstance( Project project )
	{
		SmeetLoggerSettings settings = ServiceManager.getService( project, SmeetLoggerSettings.class );
		return settings != null ? settings : new SmeetLoggerSettings();
	}

	@Nullable
	public State getState()
	{
		return state;
	}

	public void loadState( State state )
	{
		XmlSerializerUtil.copyBean( state, this.state );
	}

	///////////////////////////////////////////////
	//  state delegates
	//////////////////////////////////////////////

	//--------------------------------------------
	//  connectionPort
	//--------------------------------------------

	public int getConnectionPort()
	{
		return state.connectionPort;
	}

	public void setConnectionPort( int connectionPort )
	{
		state.connectionPort = connectionPort;
	}

	//--------------------------------------------
	//  connectionTimeout
	//--------------------------------------------

	public int getConnectionTimeout()
	{
		return state.connectionTimeout;
	}

	public void setConnectionTimeout( int connectionTimeout )
	{
		state.connectionTimeout = connectionTimeout;
	}

	//--------------------------------------------
	//  logFilter
	//--------------------------------------------

	public void setLogFilter( LogFilter logFilter )
	{
		state.logFilter = logFilter;
	}

	public LogFilter getLogFilter()
	{
		return state.logFilter;
	}

	//--------------------------------------------
	//  logLevel
	//--------------------------------------------

	public int getLogLevel()
	{
		return state.logLevel;
	}

	public void setLogLevel( int logLevel )
	{
		state.logLevel = logLevel;
	}

	//--------------------------------------------
	//  autoStartConnection
	//--------------------------------------------

	public boolean isAutoStartConnection()
	{
		return state.autoStartConnection;
	}

	public void setAutoStartConnection( boolean autoStartConnection )
	{
		state.autoStartConnection = autoStartConnection;
	}

	//--------------------------------------------
	//  autoFoldMultilineMessages
	//--------------------------------------------

	public boolean isAutoFoldMultilineMessages()
	{
		return state.autoFoldMultilineMessages;
	}

	public void setAutoFoldMultilineMessages( boolean autoFoldMultilineMessages )
	{
		state.autoFoldMultilineMessages = autoFoldMultilineMessages;
	}

	///////////////////////////////////////////////
	//  LogFilter delegates
	//////////////////////////////////////////////

	//--------------------------------------------
	//  pattern
	//--------------------------------------------

	public String getPattern()
	{
		return state.logFilter.pattern;
	}

	public void setPattern( String pattern )
	{
		state.logFilter.pattern = pattern;
	}

	//--------------------------------------------
	//  mathingCase
	//--------------------------------------------

	public Boolean isMatchingCase()
	{
		return state.logFilter.matchingCase;
	}

	public void setMatchingCase( Boolean matchingCase )
	{
		state.logFilter.matchingCase = matchingCase;
	}

	//--------------------------------------------
	//  regexp
	//--------------------------------------------

	public Boolean isRegExp()
	{
		return state.logFilter.regExp;
	}

	public void setRegExp( Boolean regExp )
	{
		state.logFilter.regExp = regExp;
	}

	///////////////////////////////////////////////
	//  State class
	//////////////////////////////////////////////

	public static class State
	{
		public int connectionPort = 4444;
		public int connectionTimeout = 16000;
		public boolean autoFoldMultilineMessages = true;
		public boolean autoStartConnection = true;
		public int logLevel = SmeetLogLevel.DEBUG.getLevel() | SmeetLogLevel.INFO.getLevel() | SmeetLogLevel.WARN.getLevel() | SmeetLogLevel.ERROR.getLevel() | SmeetLogLevel.FATAL.getLevel();
		public LogFilter logFilter = new LogFilter();
	}

	///////////////////////////////////////////////
	//  LogFilter class
	//////////////////////////////////////////////

	public static class LogFilter
	{
		public String pattern = "*";
		public boolean matchingCase = true;
		public boolean regExp = false;
	}
}

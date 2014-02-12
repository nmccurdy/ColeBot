import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.LevelRangeFilter;


public class LogAppender extends AppenderSkeleton {
	LogListener listener;
	
	private static LogAppender instance = null;
	
	public static LogAppender construct(LogListener listner) {
		instance = new LogAppender(listner);
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("%d{ABSOLUTE} %5p %c{1}:%L - %m%n");
		instance.setLayout(layout);
		instance.useInfoFilter();
		return instance;
	}
	
	public void useDebugFilter() {
		clearFilters();
		LevelRangeFilter filter = new LevelRangeFilter();
		filter.setLevelMin(Level.DEBUG);
		filter.setLevelMax(Level.INFO);
		addFilter(filter);
	}
	
	public void useInfoFilter() {
		clearFilters();
		LevelRangeFilter filter = new LevelRangeFilter();
		filter.setLevelMin(Level.INFO);
		filter.setLevelMax(Level.INFO);
		addFilter(filter);
	}
	
	public static LogAppender getInstance() throws Exception {
		if (instance != null) {
			return instance;
		} else {
			throw new Exception("You must call LogAppender.construct() first.");
		}
	}
	
	private LogAppender(LogListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		listener.log(this.layout.format(event));
	}

}

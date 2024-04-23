import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test {
	static final Logger logger = LogManager.getLogger();
	public static void main(String[] args) {
		logger.error("Integration test message");
	}
}

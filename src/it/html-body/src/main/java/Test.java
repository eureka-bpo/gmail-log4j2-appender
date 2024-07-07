import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test {
	static final Logger logger = LogManager.getLogger();
	public static void main(String[] args) {
		logger.error("Integration <b><font color=\"red\">test</font></b> message");
	}
}

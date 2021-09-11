package gregor0410.lazystronghold;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Lazystronghold implements ModInitializer {
    private static final String MOD_NAME = "LazyStronghold";
    public static Logger LOGGER = LogManager.getLogger();
    @Override
    public void onInitialize() {

    }
    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }
}

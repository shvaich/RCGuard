package me.shvaich.rcguard.asm;
import me.shvaich.rcguard.RCGuard;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@IFMLLoadingPlugin.Name(RCGuard.NAME + " ASM")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.TransformerExclusions(
    "me.shvaich.rcguard.asm"
)
public class LoadingPlugin implements IFMLLoadingPlugin {

    private static Boolean isDev;
    public static final Logger LOGGER = LogManager.getLogger(RCGuard.NAME + " - ASM");

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"me.shvaich.rcguard.asm.transformers.ClassTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        final Boolean isObfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
        if (isObfuscated != null) {
            isDev = !isObfuscated;
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static boolean isDevelopment() {
        if (isDev == null)
            throw new IllegalStateException("Accessed too early!");
        return isDev;
    }
}
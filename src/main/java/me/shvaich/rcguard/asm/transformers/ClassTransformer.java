package me.shvaich.rcguard.asm.transformers;

import me.shvaich.rcguard.asm.LoadingPlugin;
import me.shvaich.rcguard.asm.transformers.mc.MinecraftTransformer_ShovelGuard;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassTransformer implements IClassTransformer {

    private static final boolean OUTPUT_BYTECODE = Boolean.getBoolean("asm.debugBytecode");
    private final Map<String, List<MyTransformer>> transformerMap = new HashMap<>();

    public ClassTransformer() {
        registerTransformer(new MinecraftTransformer_ShovelGuard());
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !transformerMap.containsKey(transformedName)) return bytes;
        try {
            final ClassReader classReader = new ClassReader(bytes);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            for (final MyTransformer transformer : transformerMap.get(transformedName)) {
                final InjectionHandler handler = new InjectionHandler();
                transformer.transform(classNode, handler);
                final String niceTransformerName = stripClassName(transformer.getClass().getName());
                if (handler.isSuccessfulInjection()) {
                    debugLog("Successfully injected [" + niceTransformerName + "] to [" + transformedName + "]");
                }
                else {
                    if (handler.isFatalInjection()) {
                        LoadingPlugin.LOGGER.fatal("Fatal class transformation, transformer [{}] made [{}] more injections than expected in [{}]", niceTransformerName, -handler.getCount(), transformedName);
                        throw new InvalidTransformationException();
                    }
                    LoadingPlugin.LOGGER.error("Class transformation incomplete, transformer [{}] is missing [{}] injections in [{}]", niceTransformerName, handler.getCount(), transformedName);
                }
            }
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            final byte[] transformedBytes = classWriter.toByteArray();
            if (OUTPUT_BYTECODE) {
                saveOutputBytecode(transformedName, transformedBytes);
            }
            return transformedBytes;
        }
        catch (InvalidTransformationException e) { throw e; }
        catch (Throwable t) {
            LoadingPlugin.LOGGER.error("Failed to transform [{}]", transformedName, t);
            return bytes;
        }
    }

    private void registerTransformer(MyTransformer transformer) {
        for (final String className : transformer.getClassName()) {
            transformerMap.computeIfAbsent(className, t -> new ArrayList<>()).add(transformer);
        }
    }

    private static String stripClassName(String className) {
        final int lastDotIdx = className.lastIndexOf('.');
        return lastDotIdx == -1 ? className : className.substring(lastDotIdx + 1);
    }

    private static void debugLog(String msg) {
        if (LoadingPlugin.isDevelopment()) LoadingPlugin.LOGGER.info(msg);
        else LoadingPlugin.LOGGER.debug(msg);
    }

    private static void saveOutputBytecode(String transformedName, byte[] transformedBytes) {
        try {
            final File bytecodeDir = new File(Launch.minecraftHome, "rcguard_asm_bytecode");
            if (!bytecodeDir.exists() && !bytecodeDir.mkdirs()) {
                throw new IOException("Failed to create directory " + bytecodeDir);
            }
            final String niceTransformedName = stripClassName(transformedName) + ".class";
            final File bytecodeOutput = new File(bytecodeDir, niceTransformedName);

            try (final FileOutputStream os = new FileOutputStream(bytecodeOutput);) {
                os.write(transformedBytes);
            }
        }
        catch (Exception e) {
            LoadingPlugin.LOGGER.error("Failed to save bytecode output for [{}]", transformedName, e);
        }
    }

    private static final class InvalidTransformationException extends RuntimeException {
        public InvalidTransformationException() {}

        public InvalidTransformationException(String msg) { super(msg); }
    }
}
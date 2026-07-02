package me.shvaich.rcguard.asm.transformers;

import me.shvaich.rcguard.asm.LoadingPlugin;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public interface MyTransformer {

    String[] getClassName();

    void transform(ClassNode classNode, InjectionHandler handler);

    default String mapMethodName(ClassNode classNode, MethodNode methodNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, methodNode.name, methodNode.desc);
    }

    default String mapMethodInsnName(AbstractInsnNode insnNode) {
        final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

    default String mapClassName(String className) {
        return FMLDeobfuscatingRemapper.INSTANCE.map(className);
    }

    default String mapMethodDesc(MethodNode methodNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(methodNode.desc);
    }

    default String mapMethodInsnDesc(AbstractInsnNode insnNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(((MethodInsnNode) insnNode).desc);
    }

    default boolean checkMcMethod(ClassNode classNode, MethodNode methodNode, String name, String desc) {
        return mapMethodName(classNode, methodNode).equals(name) && mapMethodDesc(methodNode).equals(desc);
    }

    default boolean checkMcMethod(ClassNode classNode, MethodNode methodNode, String mcpName, String srgName, String desc) {
        return checkMcMethod(classNode, methodNode, LoadingPlugin.isDevelopment() ? mcpName : srgName, desc);
    }

    default boolean checkMcMethodInsn(AbstractInsnNode insnNode, int opcode, String owner, String name, String desc) {
        return insnNode.getOpcode() == opcode
                && insnNode instanceof MethodInsnNode
                && mapClassName(((MethodInsnNode) insnNode).owner).equals(owner)
                && mapMethodInsnName(insnNode).equals(name)
                && mapMethodInsnDesc(insnNode).equals(desc);
    }

    default boolean checkMcMethodInsn(AbstractInsnNode insnNode, int opcode, String owner, String mcpName, String srgName, String desc) {
        return checkMcMethodInsn(insnNode, opcode, owner, LoadingPlugin.isDevelopment() ? mcpName : srgName, desc);
    }

    default String getHookClass(String name) {
        return "me/shvaich/rcguard/asm/hooks/" + name;
    }
}

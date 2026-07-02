package me.shvaich.rcguard.asm.transformers.mc;

import me.shvaich.rcguard.asm.transformers.InjectionHandler;
import me.shvaich.rcguard.asm.transformers.MyTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MinecraftTransformer_ShovelGuard implements MyTransformer {

    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionHandler handler) {
        int injectionPoints = 1;
        handler.setInjectionPoints(injectionPoints);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "rightClickMouse", "func_147121_ag", "()V")) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (injectionPoints > 0 && checkMcMethodInsn(
                            insnNode,
                            Opcodes.INVOKEVIRTUAL,
                            "net/minecraft/client/multiplayer/PlayerControllerMP",
                            "isPlayerRightClickingOnEntity",
                            "func_178894_a",
                            "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/MovingObjectPosition;)Z"
                    )) {
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        list.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                getHookClass("MinecraftHook_ShovelGuard"),
                                "shouldCancelRightClick",
                                "(Lnet/minecraft/client/Minecraft;)Z",
                                false
                        ));
                        final LabelNode notCanceled = new LabelNode();
                        list.add(new JumpInsnNode(Opcodes.IFEQ, notCanceled));
                        list.add(new InsnNode(Opcodes.RETURN));
                        list.add(notCanceled);
                        methodNode.instructions.insertBefore(insnNode, list);
                        handler.addInjection();
                        injectionPoints--;
                    }
                }
            }
        }
    }
}
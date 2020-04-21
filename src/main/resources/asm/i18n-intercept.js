function initializeCoreMod() {
    var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var Opcodes = Java.type("org.objectweb.asm.Opcodes");

    return {"i18n-intercept": {
                "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.resources.I18n",
                "methodName": "format",
                "methodDesc": "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"
            }, "transformer": function(method) {

                ASM.log("DEBUG", "o/ just a friendly lOwOcalizatiωn coremod.");

                var methodCall = ASM.buildMethodCall("eutros/lowocalization/core/handler/LOwOcalizationHook",
                    "formatMessage",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    ASM.MethodType.STATIC
                );

                var target = ASM.findFirstInstruction(method, Opcodes.ARETURN);

                method.instructions.insertBefore(target, methodCall);

                return method;
            }
        }
    }
}
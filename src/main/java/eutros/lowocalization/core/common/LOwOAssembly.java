package eutros.lowocalization.core.common;

import eutros.lowocalization.api.ILOwOTransformation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// using the native scripting language provided by the JVM
public class LOwOAssembly implements Opcodes {

    public static final Logger LOGGER = LogManager.getLogger();

    private static final int LABEL = -1;
    private static final int CATCH = -2;

    private static int getOpcode(String code) throws AssemblyException {
        code = code.toUpperCase();
        if("LABEL".equals(code)) return LABEL;
        if("CATCH".equals(code)) return CATCH;
        try {
            return (int) Opcodes.class.getField(code).get(null);
        } catch(ReflectiveOperationException e) {
            throw new AssemblyException("No such opcode: " + code);
        }
    }

    public static abstract class AssemblyTransformation implements ILOwOTransformation {

        Pattern pattern;
        boolean global;

        private boolean valid = true;

        @Override
        public String transform(String source) {
            if (valid) {
                Random random = new Random(source.hashCode());
                try {
                    Matcher matcher = pattern.matcher(source);
                    if(global) {
                        if (matcher.find()) {
                            StringBuilder sb = new StringBuilder();
                            int end = 0;
                            do {
                                sb.append(source, end, matcher.start());
                                end = matcher.end();
                                sb.append(transformMatcher(matcher, random));
                            } while(matcher.find());
                            sb.append(source.substring(end));
                            return sb.toString();
                        }
                    } else {
                        if(matcher.matches()) return transformMatcher(matcher, random);
                    }
                } catch(Throwable e) {
                    LOGGER.error("Error running ASM transformer, invalidating.", e);
                    valid = false;
                }
            }
            return source;
        }

        protected abstract String transformMatcher(Matcher matcher, Random random);

    }

    private static final Method TRANSFORM_MATCHER;

    static {
        try {
            TRANSFORM_MATCHER = Method.getMethod(AssemblyTransformation.class
                    .getDeclaredMethod("transformMatcher", Matcher.class, Random.class));
        } catch(NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int nameIndex = 0;

    public static Optional<ILOwOTransformation> make(String source) {
        if (source.length() <= 2) return Optional.empty();
        char delim = source.charAt(0);
        int patternEnd = source.indexOf(delim, 1);
        if (patternEnd < 0 || patternEnd + 2 >= source.length()) return Optional.empty();

        Pattern pattern = Pattern.compile(source.substring(1, patternEnd));
        boolean global = source.charAt(patternEnd + 1) == 'g';
        if (global) patternEnd++;

        Type STRING = Type.getType(String.class);
        Type RANDOM = Type.getType(Random.class);

        int groupCount = pattern.matcher("").groupCount();
        Type[] types = new Type[groupCount + 2];
        for(int i = 0; i <= groupCount; i++) types[i] = STRING;
        types[groupCount + 1] = RANDOM;

        String internalName = "eutros/lowocalization/assembly/Transformer" + nameIndex++;
        Method staticMethod = new Method("doTransform", STRING, types);

        try {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            cw.visit(V1_8,
                    ACC_PUBLIC | ACC_SUPER,
                    internalName,
                    null,
                    Type.getInternalName(AssemblyTransformation.class),
                    null);
            MethodVisitor mv;
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(AssemblyTransformation.class),
                    "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            mv = cw.visitMethod(ACC_PROTECTED,
                    TRANSFORM_MATCHER.getName(),
                    TRANSFORM_MATCHER.getDescriptor(),
                    null,
                    null);
            mv.visitCode();
            for(int i = 0; i <= groupCount; i++) {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(i);
                mv.visitMethodInsn(INVOKEINTERFACE,
                        Type.getInternalName(MatchResult.class),
                        "group",
                        "(I)Ljava/lang/String;",
                        true);
            }
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC,
                    internalName,
                    staticMethod.getName(),
                    staticMethod.getDescriptor(),
                    false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC,
                    staticMethod.getName(),
                    staticMethod.getDescriptor(),
                    null,
                    null);
            mv.visitCode();
            tryCompile(source.substring(patternEnd + 1), mv);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            cw.visitEnd();

            byte[] bytes = cw.toByteArray();
            AssemblyTransformation loaded = tryLoad(bytes, internalName);
            loaded.pattern = pattern;
            loaded.global = global;
            return Optional.of(loaded);
        } catch(AssemblyException | RuntimeException e) {
            LOGGER.error("Couldn't compile assembly.", e);
        }

        return Optional.empty();
    }

    private static void tryCompile(String source, MethodVisitor mv) throws AssemblyException {
        Iterator<String> tokens = tokenize(source);

        HashMap<String, Label> labels = new HashMap<>();
        while(tokens.hasNext()) {
            String name = tokens.next();
            int opcode = getOpcode(name);
            if(INSNS.containsKey(opcode)) {
                try {
                    INSNS.get(opcode).accept(mv, opcode, tokens, labels);
                } catch(RuntimeException e) {
                    throw new AssemblyException("An unexpected error occurred", e);
                }
            } else {
                throw new AssemblyException(String.format("Invalid opcode: %s", name.toUpperCase()));
            }
        }
    }

    private static Pattern TOKEN_PATTERN = Pattern.compile("\"[^\"]*?\"|'[^']*?'|\\S+");

    private static Iterator<String> tokenize(String source) {
        Matcher matcher = TOKEN_PATTERN.matcher(source);
        return new Iterator<String>() {
            boolean hasNext = matcher.find();

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public String next() {
                if (!hasNext) throw new NoSuchElementException();
                String next = matcher.group();
                hasNext = matcher.find();
                return next;
            }
        };
    }

    private static class Loader extends ClassLoader {

        static Loader INSTANCE = new Loader(Thread.currentThread().getContextClassLoader());

        private Loader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(byte[] bytes, String name) {
            return defineClass(name, bytes, 0, bytes.length);
        }

    }

    @NotNull
    private static AssemblyTransformation tryLoad(byte[] bytes, String internalName) throws AssemblyException {
        try {
            Class<?> clazz = Loader.INSTANCE.define(bytes, internalName.replace('/', '.'));
            return (AssemblyTransformation) clazz.getConstructor().newInstance();
        } catch(Throwable t) {
            throw new AssemblyException("Something went wrong.", t);
        }
    }

    private static <T> Int2ObjectMap<T> mapAll(T value, int... keys) {
        Int2ObjectMap<T> set = new Int2ObjectOpenHashMap<>();
        for(int i : keys) set.put(i, value);
        return set;
    }

    @SafeVarargs
    private static <T> Int2ObjectMap<T> mergeMaps(Int2ObjectMap<T> first, Int2ObjectMap<T>... maps) {
        for(Int2ObjectMap<T> map : maps) first.putAll(map);
        return first;
    }

    private interface TokenConsumer {

        void accept(MethodVisitor mv, int opcode, Iterator<String> tokens, Map<String, Label> labels) throws AssemblyException;

    }

    private static Int2ObjectMap<TokenConsumer> INSNS =
            mergeMaps(
                    mapAll((mv, opcode, tokens, labels) -> mv.visitInsn(opcode),
                            NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
                            LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD,
                            FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE,
                            AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
                            SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
                            FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR,
                            LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I,
                            D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN,
                            DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, MONITOREXIT),
                    mapAll((mv, opcode, tokens, labels) -> mv.visitIntInsn(opcode, nextInt(tokens)),
                            BIPUSH, SIPUSH, NEWARRAY),
                    mapAll((mv, opcode, tokens, labels) -> mv.visitVarInsn(opcode, nextInt(tokens)),
                            ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, RET),
                    mapAll((mv, opcode, tokens, labels) -> mv.visitTypeInsn(opcode, nextString(tokens)),
                            NEW, ANEWARRAY, CHECKCAST, INSTANCEOF),
                    mapAll((mv, opcode, tokens, labels) ->
                                    mv.visitFieldInsn(opcode, nextString(tokens), nextString(tokens), nextString(tokens)),
                            GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD),
                    mapAll((mv, opcode, tokens, labels) ->
                                    mv.visitMethodInsn(opcode, nextString(tokens), nextString(tokens), nextString(tokens),
                            opcode == Opcodes.INVOKEINTERFACE),
                            INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE),
                    mapAll((mv, opcode, tokens, labels) -> mv.visitJumpInsn(opcode, nextLabel(tokens, labels)),
                            IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT,
                            IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL, IFNONNULL),
                    mapAll((mv, opcode, tokens, labels) -> mv.visitLabel(nextLabel(tokens, labels)),
                            LABEL),
                    mapAll((mv, opcode, tokens, labels) -> {
                                Object constant;
                                if(!tokens.hasNext()) throw new AssemblyException("Expected constant type, got <EOF>");
                                String token = tokens.next();
                                switch(token.toUpperCase()) {
                                    case "INT":
                                    case "INTEGER":
                                        constant = nextInt(tokens);
                                        break;
                                    case "LONG":
                                        constant = next(tokens, Long::parseLong, "long");
                                        break;
                                    case "FLOAT":
                                        constant = next(tokens, Float::parseFloat, "float");
                                        break;
                                    case "DOUBLE":
                                        constant = next(tokens, Double::parseDouble, "double");
                                        break;
                                    case "STRING":
                                        constant = nextString(tokens);
                                        break;
                                    case "CLASS":
                                    case "TYPE":
                                        constant = nextType(tokens, "OBJECT or ARRAY", Type.OBJECT, Type.ARRAY);
                                        break;
                                    default:
                                        throw new AssemblyException("Expected constant type, got " + token);
                                }
                                mv.visitLdcInsn(constant);
                            },
                            LDC),
                    mapAll((mv, opcode, tokens, labels) -> mv.visitIincInsn(nextInt(tokens), nextInt(tokens)),
                            IINC),
                    mapAll((mv, opcode, tokens, labels) -> {
                                int min = nextInt(tokens);
                                int max = nextInt(tokens);
                                Label dflt = nextLabel(tokens, labels);
                                int count = max - min; // this might be wrong but I'll never find out
                                Label[] tableLabels = new Label[count];
                                for(int i = 0; i < count; i++) tableLabels[i] = nextLabel(tokens, labels);
                                mv.visitTableSwitchInsn(min, max, dflt, tableLabels);
                            },
                            TABLESWITCH),
                    mapAll((mv, opcode, tokens, labels) -> {
                                int count = nextInt(tokens);
                                Label dflt = nextLabel(tokens, labels);
                                int[] tableKeys = new int[count];
                                for(int i = 0; i < count; i++) tableKeys[i] = nextInt(tokens);
                                Label[] tableLabels = new Label[count];
                                for(int i = 0; i < count; i++) tableLabels[i] = nextLabel(tokens, labels);
                                mv.visitLookupSwitchInsn(dflt, tableKeys, tableLabels);
                            },
                            LOOKUPSWITCH),
                    mapAll((mv, opcode, tokens, labels) ->
                                    mv.visitMultiANewArrayInsn(
                                            nextType(tokens, "ARRAY", Type.ARRAY).getDescriptor(),
                                            nextInt(tokens)
                                    ),
                            MULTIANEWARRAY),
                    mapAll((mv, opcode, tokens, labels) ->
                            mv.visitTryCatchBlock(
                                    nextLabel(tokens, labels),
                                    nextLabel(tokens, labels),
                                    nextLabel(tokens, labels),
                                    nextString(tokens)),
                            CATCH)
            );

    private static String nextToken(Iterator<String> tokens, String expected) throws AssemblyException {
        if(!tokens.hasNext()) throw new AssemblyException("Expected " + expected + ", got <EOF>");
        return tokens.next();
    }

    private static <T> T next(Iterator<String> tokens, Function<String, T> mapper, String expected) throws AssemblyException {
        String token = nextToken(tokens, expected);
        try {
            return mapper.apply(token);
        } catch(RuntimeException e) {
            throw new AssemblyException("Expected " + expected + ", got " + token, e);
        }
    }

    private static Type nextType(Iterator<String> tokens, String sortNames, int... sorts) throws AssemblyException {
        Type type = next(tokens, Type::getType, "type");
        for(int sort : sorts) if(type.getSort() == sort) return type;
        throw new AssemblyException("Expected " + sortNames + " type, got " + type);
    }

    private static Label nextLabel(Iterator<String> tokens, Map<String, Label> labels) throws AssemblyException {
        return labels.computeIfAbsent(nextToken(tokens, "label name"), s -> new Label());
    }

    private static int nextInt(Iterator<String> tokens) throws AssemblyException {
        return next(tokens, Integer::parseInt, "int");
    }

    private static String nextString(Iterator<String> tokens) throws AssemblyException {
        if(!tokens.hasNext()) throw new AssemblyException("Expected string, got <EOF>");
        String token = tokens.next();
        if(token.length() < 2 || token.charAt(0) != token.charAt(token.length() - 1) ||
                (token.charAt(0) != '"' && token.charAt(0) != '\''))
            throw new AssemblyException("Expected string, got " + token);
        return token.substring(1, token.length() - 1);
    }

    private static class AssemblyException extends Exception {

        public AssemblyException(String message) {
            super(message);
        }

        public AssemblyException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}

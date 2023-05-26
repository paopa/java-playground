package per.paooap.foreign.function;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.CLinker.VaList.Builder;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import jdk.incubator.foreign.SymbolLookup;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.CLinker.C_VA_LIST;

/**
 * Example calls to C library methods from Java/JDK17.
 * Run on Windows with:
 * java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.foreign Example17.java
 * Run on GNU/Linux with:
 * java -Djava.library.path=/lib/x86_64-linux-gnu --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.foreign Example17.java
 */
public class ForeignFunction
{
    // Foreign Function and Memory API were used to call C runtime library methods and to allocate memory outside Java heap.
    // By efficiently invoking foreign functions and by safely accessing foreign memory, we can avoid the overhead of JNI.

    private static final CLinker CLINKER = CLinker.getInstance();
    // LOADER is used for symbols from xyz.dll or libxyz.so
    // For every external library dependency add: System.loadLibrary("xyz");
    private static final SymbolLookup LOADER = SymbolLookup.loaderLookup();
    // SYSTEM is used for built-in C runtime library calls.
    private static final SymbolLookup SYSTEM = CLinker.systemLookup();

    static {
        System.out.println("os.name=" + System.getProperty("os.name"));
        System.out.println("java.library.path="
                + String.join(System.lineSeparator() + "\t", System.getProperty("java.library.path").split(File.pathSeparator)));
    }

    /**
     * Find native symbol, call System.loadLibrary("xyz") for each dependency
     */
    static MemoryAddress lookup(String name)
    {
        return Objects.requireNonNull(LOADER.lookup(name).or(() -> SYSTEM.lookup(name)).get(), () -> "Not found native method: " + name);
    }

    /**
     * Example calls to C runtime library
     */
    public static void main(String... args)
            throws Throwable
    {

        getpid();

        strlen("Hello World");

        printf();

        qsort(0, 9, 33, 45, 3, 4, 6, 5, 1, 8, 2, 7);

        vprintf("ONE=%d\n", 1234);
        vprintf("A=%d B=%d\n", 2, 4);
        vprintf("%d plus %d equals %d\n", 5, 7, 12);
    }

    // get a native method handle for 'getpid' function
    private static final MethodHandle GETPID$MH = CLINKER.downcallHandle(
            lookup(System.getProperty("os.name").startsWith("Windows") ? "_getpid" : "getpid"),
            MethodType.methodType(int.class),
            FunctionDescriptor.of(C_INT));

    private static void getpid()
            throws Throwable
    {
        int npid = (int) GETPID$MH.invokeExact();
        System.out.println("getpid() JAVA => " + ProcessHandle.current().pid() + " NATIVE => " + npid);
    }

    private static final MethodHandle STRLEN$MH = CLINKER.downcallHandle(lookup("strlen"),
            MethodType.methodType(long.class, MemoryAddress.class), FunctionDescriptor.of(C_LONG_LONG, C_POINTER));

    public static void strlen(String s)
            throws Throwable
    {
        System.out.println("strlen('" + s + "')");

        // size_t strlen(const char *str);
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            SegmentAllocator allocator = SegmentAllocator.arenaAllocator(scope);
            MemorySegment hello = CLinker.toCString(s, allocator);
            long len = (long) STRLEN$MH.invokeExact(hello.address()); // 5
            System.out.println(" => " + len);
        }
    }

    static class Qsort
    {
        static int qsortCompare(MemoryAddress addr1, MemoryAddress addr2)
        {
            int v1 = MemoryAccess.getIntAtOffset(MemorySegment.globalNativeSegment(), addr1.toRawLongValue());
            int v2 = MemoryAccess.getIntAtOffset(MemorySegment.globalNativeSegment(), addr2.toRawLongValue());
            return v1 - v2;
        }
    }

    private static final MethodHandle QSORT$MH = CLINKER.downcallHandle(lookup("qsort"),
            MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(C_POINTER, C_LONG_LONG, C_LONG_LONG, C_POINTER)
    );

    /**
     * THIS SHOWS DOWNCALL AND UPCALL - uses qsortCompare FROM C code!
     * void qsort(void *base, size_t nitems, size_t size, int (*compar)(const void *, const void*))
     *
     * @param toSort
     */
    public static int[] qsort(int... toSort)
            throws Throwable
    {
        System.out.println("qsort() " + Arrays.toString(toSort));

        MethodHandle comparHandle = MethodHandles.lookup()
                .findStatic(Qsort.class, "qsortCompare",
                        MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemoryAddress comparFunc = CLINKER.upcallStub(
                    comparHandle, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER), scope
            );

            SegmentAllocator allocator = SegmentAllocator.arenaAllocator(scope);
            // comparFunc = allocator.register(comparFunc);
            MemorySegment array = allocator.allocateArray(C_INT, toSort);
            QSORT$MH.invokeExact(array.address(), (long) toSort.length, 4L, comparFunc.address());
            int[] sorted = array.toIntArray();
            System.out.println(" => " + Arrays.toString(sorted));
            return sorted;
        }
    }

    private static final MethodHandle PRINTF$MH = CLINKER.downcallHandle(lookup("printf"),
            MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, int.class),
            FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_INT)
    );

    /**
     * This version hard-codes use of 3 int params as args to the string format
     */
    public static void printf()
            throws Throwable
    {
        System.out.println("printf()");
        int a = 10;
        int b = 7;
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            SegmentAllocator allocator = SegmentAllocator.arenaAllocator(scope);
            MemorySegment s = CLinker.toCString("%d times %d equals %d\n", allocator);
            int rc = (int) PRINTF$MH.invokeExact(s.address(), a, b, a * b);
            System.out.println(" => rc=" + rc);
        }
    }

    private static final MethodHandle vprintf = CLINKER.downcallHandle(lookup("vprintf"),
            MethodType.methodType(int.class, MemoryAddress.class, CLinker.VaList.class),
            FunctionDescriptor.of(C_INT, C_POINTER, C_VA_LIST));

    /**
     * vprintf takes a pointer to arg list rather than arg list as for printf
     */
    public static void vprintf(String format, int... args)
            throws Throwable
    {

        System.out.println("vprintf(\"" + format.replaceAll("[\r\n]{1,2}", "\\\\n") + "\") " + Arrays.toString(args));

        // Weird Builder callback mechanism to fill the varargs values
        Consumer<Builder> actions = builder -> {
            for (int v : args) {
                builder.vargFromInt(C_INT, v);
            }
        };

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            SegmentAllocator allocator = SegmentAllocator.arenaAllocator(scope);
            CLinker.VaList vlist = CLinker.VaList.make(actions, scope);
            MemorySegment s = CLinker.toCString(format, allocator);

            int rc = (int) vprintf.invokeExact(s.address(), vlist /* ????? .address() */);
            System.out.println(" => rc=" + rc);
        }
    }
}

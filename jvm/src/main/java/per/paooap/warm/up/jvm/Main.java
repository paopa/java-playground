package per.paooap.warm.up.jvm;

public class Main
{
    static {
        long start = System.nanoTime();
        Main.load();
        long end = System.nanoTime();
        System.out.println("Warm Up time : " + (end - start));
    }

    // Why did we need to warm up the JVM?
    // because the JVM loads the classes lazily, thus the first time we call the method m() it will take more time
    // than the subsequent calls. This is because the JVM will load the class and then execute the method.
    // The subsequent calls will be faster because the class is already loaded in the JVM.
    // This is called the warm up time. We need to warm up the JVM before we start measuring the performance of the code.
    // Also see, https://www.baeldung.com/java-jvm-warmup
    public static void main(String[] args)
    {
        long start = System.nanoTime();
        Main.load();
        long end = System.nanoTime();
        System.out.println("Total time taken : " + (end - start));
    }

    protected static void load()
    {
        for (int i = 0; i < 100000; i++) {
            Dummy dummy = new Dummy();
            dummy.m();
        }
    }
}
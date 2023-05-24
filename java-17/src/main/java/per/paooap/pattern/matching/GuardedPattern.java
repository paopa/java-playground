package per.paooap.pattern.matching;

public class GuardedPattern
{
    public static double process(Object o)
    {
        return switch (o) {
            case Integer i && i > 0 -> Math.sqrt(i);
            case Double d && d > 0 -> Math.sqrt(d);
            case String s && s.length() > 0 -> Math.sqrt(Integer.parseInt(s));
            default -> -1;
        };
    }

    public static void main(String[] args)
    {
        System.out.println(process(10));
        System.out.println(process(10.0));
        System.out.println(process("10"));
        System.out.println(process(-10));
        System.out.println(process(-10.0));
        System.out.println(process(""));
    }
}

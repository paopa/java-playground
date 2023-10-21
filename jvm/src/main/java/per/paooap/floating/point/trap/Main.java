package per.paooap.floating.point.trap;

public class Main
{
    public static void main(String[] args)
    {
        float abc[] = {0.1F, 0.2F, 0.3F};

        System.out.println("abc (f32)");
        System.out.printf("%10s: %x%n", "0.1 + 0.2", Float.floatToIntBits(abc[0] + abc[1]));
        System.out.printf("%10s: %x%n", "0.3", Float.floatToIntBits(abc[2]));
        System.out.println();

        double xyz[] = {0.1, 0.2, 0.3};

        System.out.println("xyz (f64)");
        System.out.printf("%12s %16s%n", "0.1 + 0.2:", Long.toHexString(Double.doubleToRawLongBits(xyz[0] + xyz[1])));
        System.out.printf("%12s %16s%n", "0.3:", Long.toHexString(Double.doubleToRawLongBits(xyz[2])));
        System.out.println();

        System.out.println((xyz[0] + xyz[1]) == xyz[2]);
    }
}

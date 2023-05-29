package per.paooap;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class Vector
{
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    public void newVectorComputation(float[] a, float[] b, float[] c)
    {
        for (var i = 0; i < a.length; i += SPECIES.length()) {
            var m = SPECIES.indexInRange(i, a.length);
            var va = FloatVector.fromArray(SPECIES, a, i, m);
            var vb = FloatVector.fromArray(SPECIES, b, i, m);
            var vc = va.mul(vb);
            vc.intoArray(c, i, m);
        }
    }

    public void commonVectorComputation(float[] a, float[] b, float[] c)
    {
        for (var i = 0; i < a.length; i++) {
            c[i] = a[i] * b[i];
        }
    }

    // This is the output of the program:
    // New vector computation: 210.406882 ms
    // Common vector computation: 16.382975 ms
    // The new vector computation is slower than the common vector computation.
    public static void main(String[] args)
    {
        var a = new float[1000000];
        var b = new float[1000000];
        var c = new float[1000000];

        for (var i = 0; i < a.length; i++) {
            a[i] = i;
            b[i] = i;
        }

        var vector = new Vector();
        var start = System.nanoTime();
        vector.newVectorComputation(a, b, c);
        var end = System.nanoTime();
        System.out.println("New vector computation: " + (end - start) / 1000000.0 + " ms");

        start = System.nanoTime();
        vector.commonVectorComputation(a, b, c);
        end = System.nanoTime();
        System.out.println("Common vector computation: " + (end - start) / 1000000.0 + " ms");
    }
}

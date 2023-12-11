package per.paooap.base64.error;

import java.util.Base64;

public class Main
{
    public static void main(String[] args)
    {
        String s = "~~~";
        String encoded = Base64.getUrlEncoder().encodeToString(s.getBytes());
        System.out.println(encoded);


        {
            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            System.out.println(new String(decoded));
        }

        {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            System.out.println(new String(decoded));
        }
    }
}
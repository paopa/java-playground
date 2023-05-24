package per.paooap.pattern.matching;

import per.paooap.sealed.Shape;

public class SwitchByType
{
    public String processShape(Shape shape)
    {
        // a switch works only with four primitive types: byte, short, char, int
        // and their wrapper classes Byte, Short, Character, Integer
        // and also with String and enum.

        // In java 13, the switch expression can be used -> to return a value

        // In java 17, the switch statement can be used to match the type of objects
        // and extract the values of their fields, without the need to cast them
        String result = switch (shape) {
            case Shape.Circle circle -> "Circle with radius " + circle.getRadius();
            case Shape.Rectangle rectangle -> "Rectangle with width " + rectangle.getWidth() + " and height " + rectangle.getHeight();
            default -> "Unknown shape";
        };

        return result;
    }

    public static void main(String[] args)
    {
        SwitchByType switcher = new SwitchByType();
        System.out.println(switcher.processShape(new Shape.Circle(10)));
        System.out.println(switcher.processShape(new Shape.Rectangle(10, 20)));
        System.out.println(switcher.processShape(new Shape.Square(10)));
    }
}

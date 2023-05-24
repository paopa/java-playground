package per.paooap.sealed;

public abstract sealed class Shape
        permits Shape.Circle, Shape.Rectangle, Shape.Square
{
    // sealed class is used to restrict the inheritance hierarchy
    // permits keyword is used to specify the classes that can extend the sealed class

    public static final class Circle
            extends Shape
    {
        private final double radius;

        public Circle(double radius)
        {
            this.radius = radius;
        }

        public double getRadius()
        {
            return radius;
        }
    }

    public static final class Rectangle
            extends Shape
    {
        private final double width;
        private final double height;

        public Rectangle(double width, double height)
        {
            this.width = width;
            this.height = height;
        }

        public double getWidth()
        {
            return width;
        }

        public double getHeight()
        {
            return height;
        }
    }

    public static final class Square
            extends Shape
    {
        private final double side;

        public Square(double side)
        {
            this.side = side;
        }

        public double getSide()
        {
            return side;
        }
    }
}

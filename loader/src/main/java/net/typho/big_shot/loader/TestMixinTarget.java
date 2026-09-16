package net.typho.big_shot.loader;

public class TestMixinTarget {
    public static void main() {
        float f = 3;

        if (Math.random() > 0) {
            int i = 10;
            System.out.println(i * f);
        }

        System.out.println("abc");
    }
}

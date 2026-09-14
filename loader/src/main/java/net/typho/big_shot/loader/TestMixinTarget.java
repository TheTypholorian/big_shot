package net.typho.big_shot.loader;

public class TestMixinTarget {
    public static void switchX(int i) {
        switch (i) {
            case 0 -> System.out.println("a");
        }
    }

    public static void switchY(int i) {
        System.out.println(switch (i) {
            case 0 -> i == 1 ? "a" : "b";
            default -> throw new AssertionError();
        });
    }

    public static void switch0(int i) {
        switch (i) {
            case 0 -> System.out.println("a");
            case 1 -> System.out.println("b");
            case 2 -> System.out.println("c");
        }
    }

    public static void main(int i) {
        switch (i) {
            case 0 -> System.out.println("a");
            case 1 -> System.out.println("b");
            case 2 -> System.out.println("c");
            case 25 -> System.out.println("z");
        }
    }

    public static void switch2(int i) {
        switch (i) {
            case 0 -> System.out.println("a");
            case 1 -> System.out.println("b");
            case 2 -> {
                System.out.println("c");
                System.out.println("123");
            }
            case 25 -> System.out.println("z");
        }
    }

    public static void switch3(int i) {
        System.out.println(switch (i) {
            case 0 -> "a";
            case 1 -> "b";
            case 2 -> "c";
            case 25 -> "z";
            default -> throw new AssertionError();
        });
    }

    public static enum TestEnum {
        A, B
    }

    public static void switch4(TestEnum e) {
        System.out.println(switch (e) {
            case TestEnum.B -> "b";
            case TestEnum.A -> "a";
            default -> throw new AssertionError();
        });
    }

    /*
    public static void switch6(ShaderType s) {
        System.out.println(switch (s) {
            case ShaderType.VERTEX -> "vert";
            case ShaderType.FRAGMENT -> "frag";
            default -> throw new AssertionError();
        });
    }

    public static void switch7(Direction d) {
        System.out.println(switch (d) {
            case DOWN -> "down";
            case UP -> "up";
            case NORTH -> "north";
            case SOUTH -> "south";
            case WEST -> "west";
            case EAST -> "east";
        });
    }
     */

    public static void switch5(String s) {
        System.out.println(switch (s) {
            case "a1" -> "a";
            case "b1" -> "b";
            case "c1" -> "c";
            default -> throw new AssertionError();
        });
    }
}

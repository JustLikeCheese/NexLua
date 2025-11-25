package com.mycompany;

@SuppressWarnings("unused")
public class TestClass {
    // Test Java Class
    public static int STATIC_FIELD = 1;
    public static final int FINAL_STATIC_FIELD = 2;

    public static int STATIC_METHOD() {
        return 3;
    }

    private static int a = 4;

    public static int getA() {
        return a;
    }

    public static void setA(int a) {
        TestClass.a = a;
    }

    public static int getb() {
        return 5;
    }

    public static int setb() {
        return 1;
    }

    class InnerClass {
    }

    // Test Java Object
    public int field = 10;
    public final int finalField = 20;
    private int x = 30;
    private int y = 40;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int gety() {
        return y;
    }

    public void sety() {
    }

    public TestClass() {
    }
}

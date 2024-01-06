package com.notbg;

import java.lang.reflect.Field;

public class Main {
    public static void main(String[] args) {
        try {
            // 获取类的 Class 对象
            Class<?> myClass = MyClass.class;

            // 使用 getField 获取公共字段，使用 getDeclaredField 获取所有字段（包括私有字段）
            Field field = myClass.getDeclaredField("myField");

            // 获取字段的类型
            Class<?> fieldType = field.getType();

            System.out.println("Field Type: " + fieldType.getName());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}

class MyClass {
    // 假设这是你要获取类型的字段
    public int myField;
}

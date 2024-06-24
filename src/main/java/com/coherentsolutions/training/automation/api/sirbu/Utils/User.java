package com.coherentsolutions.training.automation.api.sirbu.Utils;

import lombok.Data;

@Data
public class User {
    private String name;
    private String sex;
    private int age;
    private String zipCode;

    public User() {}

    public User(String name, String sex) {
        this.name = name;
        this.sex = sex;
    }

    public User(String name, String sex, int age, String zipCode) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.zipCode = zipCode;
    }
}
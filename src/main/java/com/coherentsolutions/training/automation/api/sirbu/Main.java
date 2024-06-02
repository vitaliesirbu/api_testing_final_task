package com.coherentsolutions.training.automation.api.sirbu;

import lombok.SneakyThrows;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        AuthProvider authProvider = AuthProvider.getInstance();

        String writeToken = authProvider.getWriteToken();
        String readToken = authProvider.getReadToken();

        System.out.println("Write Token: " + writeToken);
        System.out.println("Read Token: " + readToken);
    }
}

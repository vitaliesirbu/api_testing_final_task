package com.coherentsolutions.training.automation.api.sirbu;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        try {
            AuthProvider authProvider = AuthProvider.getInstance();
            String writeToken = authProvider.getWriteToken();
            String readToken = authProvider.getReadToken();

            System.out.println("Write Token: " + writeToken);
            System.out.println("Read Token: " + readToken);
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}

package com.coherentsolutions.training.automation.api.sirbu;

public class Main {
    public static void main(String[] args) {
        AuthProvider authProvider = AuthProvider.getInstance();
        try {
            String writeToken = authProvider.getWriteToken();
            String readToken = authProvider.getReadToken();

            System.out.println("Write Token: " + writeToken);
            System.out.println("Read Token: " + readToken);
        } catch (Exception e) {
            System.err.println("Error occurred while getting tokens: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

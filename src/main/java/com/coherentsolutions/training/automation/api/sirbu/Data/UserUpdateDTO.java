package com.coherentsolutions.training.automation.api.sirbu.Data;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private User userNewValues;
    private User userToChange;

    public UserUpdateDTO(User userToChange, User userNewValues) {
        this.userToChange = userToChange;
        this.userNewValues = userNewValues;
    }
}
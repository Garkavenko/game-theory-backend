package ru.gameTheory.server.routes.task1.dao;

import ru.gameTheory.server.routes.task1.models.User;

import java.util.LinkedList;
import java.util.List;

public class Users {
    public static List<User> users = new LinkedList<>();
    public static Long lastUserId = 1L;
}

package ru.gameTheory.server.routes.task2.dao;

import ru.gameTheory.server.routes.task2.models.Room;

import java.util.LinkedList;
import java.util.List;

public class RoomsDao {
    public static List<Room> rooms = new LinkedList<>();
    public static Long lastRoomId = 1L;
}

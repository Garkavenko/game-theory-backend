package ru.gameTheory.server.routes.task2.distributionMethods;

import ru.gameTheory.server.routes.task2.models.Room;
import ru.gameTheory.server.routes.task2.models.User;

import java.util.List;

public interface IMethod {
    public void calc(Room room, List<User> users);
    public List<Double> getDistributions(Room room, List<User> users);
}

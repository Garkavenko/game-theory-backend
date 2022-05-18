package ru.gameTheory.server.routes.task2.distributionMethods;

import ru.gameTheory.server.routes.task2.models.Room;
import ru.gameTheory.server.routes.task2.models.User;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BackMethod implements IMethod {
    public static String METHOD_ID = "4";


    @Override
    public void calc(Room room, List<User> users) {
        users.sort(Comparator.comparingInt(User::getOrder));
        for (User user : users) {
            Double sumOther = users.stream()
                    .map(user1 -> user1.getEvaluations().get(user1.getEvaluations().size() - 1))
                    .reduce(0.0, Double::sum);

            user.getDistributions().add(
                    user.getEvaluations().get(user.getEvaluations().size() - 1)
                    * (1 - (room.getResource() / sumOther))
            );
        }
    }

    @Override
    public List<Double> getDistributions(Room room, List<User> users) {
        users.sort(Comparator.comparingInt(User::getOrder));
        List<Double> distributions = new LinkedList<>();
        for (User user : users) {
            Double sumOther = users.stream()
                    .map(user1 -> user1.getEvaluations().get(user1.getEvaluations().size() - 1))
                    .reduce(0.0, Double::sum);

            distributions.add(
                    ( user.getEvaluations().get(user.getEvaluations().size() - 1)
                            * (1 - (room.getResource() / sumOther)))
            );
        }
        return distributions;
    }
}

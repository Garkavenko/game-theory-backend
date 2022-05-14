package ru.gameTheory.server.routes.task1.distributionMethods;

import ru.gameTheory.server.routes.task1.models.Room;
import ru.gameTheory.server.routes.task1.models.User;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ConvexMethod implements IMethod {
    public static String METHOD_ID = "1";


    @Override
    public void calc(Room room, List<User> users) {
        try {
            users.sort(Comparator.comparingInt(User::getOrder));
            for (User user : users) {
                Double sumOther = users.stream()
                        .filter(user1 -> user1.getOrder() >= user.getOrder())
                        .map(user1 -> user1.getEvaluations().get(user1.getEvaluations().size() - 1))
                        .reduce(0.0, (aDouble, aDouble2) -> aDouble + Math.pow(aDouble2, 2));
                Double alreadyDistributedSum = users.stream()
                        .filter(user1 -> user1.getOrder() < user.getOrder())
                        .map(user1 -> user1.getDistributions().get(user1.getDistributions().size() - 1))
                        .reduce(0.0, Double::sum);

                user.getDistributions().add(
                        (Math.pow(user.getEvaluations().get(user.getEvaluations().size() - 1), 2) / sumOther)
                                * (room.getResource() - alreadyDistributedSum)
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Double> getDistributions(Room room, List<User> users) {
        users.sort(Comparator.comparingInt(User::getOrder));
        List<Double> distributions = new LinkedList<>();
        for (User user : users) {
            Double sumOther = users.stream()
                    .filter(user1 -> user1.getOrder() >= user.getOrder())
                    .map(user1 -> user1.getEvaluations().get(user1.getEvaluations().size() - 1))
                    .reduce(0.0, (aDouble, aDouble2) -> aDouble + Math.pow(aDouble2, 2));
            Double alreadyDistributedSum = users.stream()
                    .filter(user1 -> user1.getOrder() < user.getOrder())
                    .map(user1 -> user1.getDistributions().get(user1.getDistributions().size() - 1))
                    .reduce(0.0, Double::sum);

            distributions.add(
                    (Math.pow(user.getEvaluations().get(user.getEvaluations().size() - 1), 2) / sumOther)
                            * (room.getResource() - alreadyDistributedSum)
            );
        }
        return distributions;
    }
}

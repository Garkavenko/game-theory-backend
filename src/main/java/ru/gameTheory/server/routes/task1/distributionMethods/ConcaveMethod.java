package ru.gameTheory.server.routes.task1.distributionMethods;

import ru.gameTheory.server.routes.task1.models.Room;
import ru.gameTheory.server.routes.task1.models.User;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ConcaveMethod implements IMethod{
    public static String METHOD_ID = "3";


    @Override
    public void calc(Room room, List<User> users) {
        users.sort(Comparator.comparingInt(User::getOrder));
        for (User user : users) {
            Double sumOther = users.stream()
                    .filter(user1 -> user1.getOrder() >= user.getOrder())
                    .map(user1 -> user1.getEvaluations().get(user1.getEvaluations().size() - 1))
                    .reduce(0.0, (aDouble, aDouble2) -> aDouble + Math.sqrt(aDouble2));
            Double alreadyDistributedSum = users.stream()
                    .filter(user1 -> user1.getOrder() < user.getOrder())
                    .map(user1 -> user1.getDistributions().get(user1.getDistributions().size() - 1))
                    .reduce(0.0, Double::sum);

            user.getDistributions().add(
                    (Math.sqrt(user.getEvaluations().get(user.getEvaluations().size() - 1)) / sumOther)
                            * (room.getResource() - alreadyDistributedSum)
            );
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
                    .reduce(0.0, (aDouble, aDouble2) -> aDouble + Math.sqrt(aDouble2));
            Double alreadyDistributedSum = users.stream()
                    .filter(user1 -> user1.getOrder() < user.getOrder())
                    .map(user1 -> user1.getDistributions().get(user1.getDistributions().size() - 1))
                    .reduce(0.0, Double::sum);

            distributions.add(
                    (Math.sqrt(user.getEvaluations().get(user.getEvaluations().size() - 1)) / sumOther)
                            * (room.getResource() - alreadyDistributedSum)
            );
        }
        return distributions;
    }
}

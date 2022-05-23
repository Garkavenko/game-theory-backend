package ru.gameTheory.server.routes.task1;

import ru.gameTheory.server.routes.task1.dao.RoomsDao;
import ru.gameTheory.server.routes.task1.dao.Users;
import ru.gameTheory.server.routes.task1.distributionMethods.*;
import ru.gameTheory.server.routes.task1.models.Room;
import ru.gameTheory.server.routes.task1.models.User;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Cron {

    public static List<Cron> crons = new LinkedList<>();

    public static Optional<Cron> getCronByRoomId(Long roomId) {
        return crons.stream().filter(cron -> Objects.equals(cron.roomId, roomId)).findFirst();
    }

    private Long roomId;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Runnable runnable;
    private ScheduledFuture scheduledFuture;

    public Long getRoomId() {
        return this.roomId;
    }

    public Cron(Long roomId) {
        this.roomId = roomId;
        this.runnable = () -> {
            final Optional<Room> roomOptional = RoomsDao.rooms
                    .stream()
                    .filter(room1 -> Objects.equals(room1.getId(), roomId))
                    .findFirst();
            if (roomOptional.isPresent()) {
                final Room room = roomOptional.get();

                if (room.getStarted()) {


                    final List<User> users = Users.users.stream()
                            .filter(user -> Objects.equals(user.getRoomId(), roomId))
                            .collect(Collectors.toList());

                    users.forEach(user -> {
                        if (user.getEvaluations().size() < room.getCurrentStepNumber()) {
                            if (user.getEvaluations().size() > 0) {
                                user.getEvaluations().add(user.getEvaluations().get(user.getEvaluations().size() - 1));
                            } else {
                                user.getEvaluations().add(user.getCost());
                            }
                        }
                    });

                    if (Objects.equals(room.getPriorityMethod(), ConvexMethod.METHOD_ID)) {
                        new ConvexMethod().calc(room, users);
                    } else if (Objects.equals(room.getPriorityMethod(), ConcaveMethod.METHOD_ID)) {
                        new ConcaveMethod().calc(room, users);
                    } else if (Objects.equals(room.getPriorityMethod(), LinearMethod.METHOD_ID)) {
                        new LinearMethod().calc(room, users);
                    } else {
                        throw new RuntimeException("Priority Method not supported");
                    }
                    users.forEach(u -> u.calcResult(room.getPenalty()));
                    room.calcResult(users);

                    room.setNextTickAt(new Date().getTime() + 120000);
                    room.setCurrentStepNumber(room.getCurrentStepNumber() + 1);
                    scheduledFuture = scheduler.schedule(this.runnable, 2, TimeUnit.MINUTES);
                } else {
                    clear();
                }
            } else {
               clear();
            }
        };
        crons.add(this);
        RoomsDao.rooms
                .stream()
                .filter(room1 -> Objects.equals(room1.getId(), roomId))
                .findFirst()
                .ifPresent(room -> {
                    room.setCurrentStepNumber(room.getCurrentStepNumber() + 1);
                    this.runnable.run();
                    // scheduledFuture = scheduler.schedule(this.runnable,60, TimeUnit.MINUTES);
                    // this.flush();
                });
    }

    public IMethod getMethod(String methodId) {
        if (methodId.equals(ConvexMethod.METHOD_ID)) {
            return new ConvexMethod();
        }
        if (methodId.equals(ConcaveMethod.METHOD_ID)) {
            return new ConcaveMethod();
        }
        if (methodId.equals(LinearMethod.METHOD_ID)) {
            return new LinearMethod();
        }
        if (methodId.equals(BackMethod.METHOD_ID)) {
            return new BackMethod();
        }
        return null;
    }

    public Cron(Long roomId, List<Task1.SetUsersBody.UserSetting> userSettings) {
        this.roomId = roomId;
        this.runnable = () -> {
            final Optional<Room> roomOptional = RoomsDao.rooms
                    .stream()
                    .filter(room1 -> Objects.equals(room1.getId(), roomId))
                    .findFirst();
            if (roomOptional.isPresent()) {
                final Room room = roomOptional.get();

                if (room.getStarted()) {
                    while (room.getCurrentStepNumber() < room.getStepsCount()) {
                        final List<User> users = Users.users.stream()
                                .filter(user -> Objects.equals(user.getRoomId(), roomId))
                                .sorted(Comparator.comparingInt(User::getOrder))
                                .collect(Collectors.toList());

                        if (room.getCenterResults().size() > 2) {
                            // TODO Predidushiy!!!
                            /*final boolean isNash = users.stream().allMatch((user1) -> {
                                final Double prevE1U = user1.getEvaluations().get(user1.getEvaluations().size() - 1);
                                final Double prevE2U = user1.getEvaluations().get(user1.getEvaluations().size() - 2);

                                final Double prev1U = user1.getResults().get(user1.getResults().size() - 1);
                                final Double prev2U = user1.getResults().get(user1.getResults().size() - 2);

                                final String user1Strategy = prevE1U > prevE2U ? "up" : (prevE1U < prevE2U ? "down" : "equal");

                                if (prev1U < prev2U) {
                                    final List<Double> distributions = getMethod(room.getPriorityMethod()).getDistributions(room, users.stream().map(user3 -> {
                                        final User newUser = new User();
                                        newUser.setOrder(user3.getOrder());
                                        newUser.setEvaluations(new LinkedList<>(user3.getEvaluations()));
                                        newUser.setDistributions(new LinkedList<>(user3.getDistributions()));
                                        if (user3.getId().equals(user1.getId())) {
                                            if (user1Strategy.equals("up")) {
                                                newUser.getEvaluations().add(Math.max(prevE1U - room.getStep(), room.getStep()));
                                            }
                                            if (user1Strategy.equals("down")) {
                                                newUser.getEvaluations().add(Math.min(prevE1U + room.getStep(), room.getResource()));
                                            }
                                            if (user1Strategy.equals("equal")) {
                                                newUser.getEvaluations().add(Math.min(prevE1U + room.getStep(), room.getResource()));
                                            }
                                        } else {
                                            newUser.getEvaluations().add(newUser.getEvaluations().get(newUser.getEvaluations().size() - 1));
                                        }
                                        return newUser;
                                    }).collect(Collectors.toList()));
                                    if (user1.getLastResult(distributions.get(user1.getOrder() - 1)) <= prev1U) {
                                        return true;
                                    }
                                }
                                return false;
                            });
                            if (isNash) {
                                room.setFinished(true);
                                clear();
                                return;
                            }*/

                            final boolean isNash = users.stream().allMatch((user1) -> {
                                final Double prevE1U = user1.getEvaluations().get(user1.getEvaluations().size() - 1);
                                final Double prevE2U = user1.getEvaluations().get(user1.getEvaluations().size() - 2);

                                final Double prev1U = user1.getResults().get(user1.getResults().size() - 1);
                                final Double prev2U = user1.getResults().get(user1.getResults().size() - 2);

                                final Integer min = 1;
                                final Integer max = (int) ((double) room.getResource());

                                boolean allMatched = true;
                                return IntStream.range(min, max + 1).allMatch(i -> {
                                //return IntStream.range((int) (prevE1U - room.getStep()), (int) (prevE1U + room.getStep())).allMatch(i -> {
                                    if (i == prevE1U) return true;
                                    if (Math.abs(prevE1U - i) % room.getStep() != 0) return true;
                                    /*if (users.stream().map(u -> u.getId() == user1.getId() ? (double) i : u.getEvaluations().get(u.getEvaluations().size() - 1)).reduce(0.0, Double::sum) < room.getResource()) {
                                        return true;
                                    }*/

                                    final List<Double> distributions = getMethod(room.getPriorityMethod()).getDistributions(room, users.stream().map(user3 -> {
                                        final User newUser = new User();
                                        newUser.setOrder(user3.getOrder());
                                        newUser.setEvaluations(new LinkedList<>(user3.getEvaluations()));
                                        newUser.setDistributions(new LinkedList<>(user3.getDistributions()));
                                        if (user3.getId().equals(user1.getId())) {
                                            newUser.getEvaluations().add((double) i);
                                        } else {
                                            newUser.getEvaluations().add(newUser.getEvaluations().get(newUser.getEvaluations().size() - 1));
                                        }
                                        return newUser;
                                    }).collect(Collectors.toList()));
                                    //room.getRoomType();
                                    if (user1.getLastResult(distributions.get(user1.getOrder() - 1), room.getPenalty()) > prev1U) {
                                        System.out.println("=================DEBUG");
                                        System.out.println("NO, " + user1.getOrder() + " can put " + i + " to win on step " + room.getCenterResults().size() + 1);
                                        System.out.println("=================DEBUG END");
                                    }

                                    return user1.getLastResult(distributions.get(user1.getOrder() - 1), room.getPenalty()) <= prev1U;
                                });
                            });
                            if (isNash) {
                                room.setFinished(true);
                                clear();
                                return;
                            }


                            /*final boolean isNash = users.stream().allMatch((user1) -> {
                                final Double prevE1U = user1.getEvaluations().get(user1.getEvaluations().size() - 1);
                                final Double prevE2U = user1.getEvaluations().get(user1.getEvaluations().size() - 2);

                                final Double prev1U = user1.getResults().get(user1.getResults().size() - 1);
                                final Double prev2U = user1.getResults().get(user1.getResults().size() - 2);
                                return Arrays.asList("up", "down").stream().allMatch(s -> {
                                    if (s.equals("up") && prevE1U.equals(room.getResource())) {
                                        return true;
                                    }
                                    final List<Double> distributions = getMethod(room.getPriorityMethod()).getDistributions(room, users.stream().map(user3 -> {
                                        final User newUser = new User();
                                        newUser.setOrder(user3.getOrder());
                                        newUser.setEvaluations(new LinkedList<>(user3.getEvaluations()));
                                        newUser.setDistributions(new LinkedList<>(user3.getDistributions()));
                                        if (user3.getId().equals(user1.getId())) {
                                            if (s.equals("up")) {
                                                newUser.getEvaluations().add(prevE1U + room.getStep());
                                            }
                                            if (s.equals("down")) {
                                                newUser.getEvaluations().add(Math.min(prevE1U - room.getStep(), room.getResource()));
                                            }
                                        } else {
                                            newUser.getEvaluations().add(newUser.getEvaluations().get(newUser.getEvaluations().size() - 1));
                                        }
                                        return newUser;
                                    }).collect(Collectors.toList()));
                                    if (user1.getLastResult(distributions.get(user1.getOrder() - 1)) <= prev1U) {
                                        return true;
                                    }
                                    return false;
                                });
                            });
                            if (isNash) {
                                room.setFinished(true);
                                clear();
                                return;
                            }*/
                        }

                        users.forEach(user -> {
                            if (user.getEvaluations().size() < room.getCurrentStepNumber()) {
                                if (user.getEvaluations().size() == 1) {
                                    userSettings.stream()
                                            .filter(userSetting -> userSetting.userId.equals(user.getId()))
                                            .findFirst()
                                            .ifPresent(userSetting -> {
                                                user.getEvaluations().add(userSetting.value);
                                            });
                                    if (user.getEvaluations().size() == 1) {
                                        user.getEvaluations().add(user.getEvaluations().get(0));
                                    }
                                } else if (user.getEvaluations().size() > 1) {
                                    final Double prev1 = user.getResults().get(user.getResults().size() - 1);
                                    final Double prev2 = user.getResults().get(user.getResults().size() - 2);

                                    final Double prevE1 = user.getEvaluations().get(user.getEvaluations().size() - 1);
                                    final Double prevE2 = user.getEvaluations().get(user.getEvaluations().size() - 2);

                                    final String strategy = prevE1 > prevE2 ? "up" : (prevE1 < prevE2 ? "down" : "equal");

                                    if (prev1 > prev2) {
                                        if (strategy.equals("up")) {
                                            user.getEvaluations().add(Math.min(prevE1 + room.getStep(), room.getResource()));
                                        }
                                        if (strategy.equals("down")) {
                                            user.getEvaluations().add(Math.max(prevE1 - room.getStep(), room.getStep()));
                                        }
                                        if (strategy.equals("equal")) {
                                            user.getEvaluations().add(prevE1);
                                        }
                                    }
                                    if (prev1 < prev2) {
                                        if (strategy.equals("up")) {
                                            user.getEvaluations().add(Math.max(prevE1 - room.getStep(), room.getStep()));
                                        }
                                        if (strategy.equals("down")) {
                                            user.getEvaluations().add(Math.min(prevE1 + room.getStep(), room.getResource()));
                                        }
                                        if (strategy.equals("equal")) {
                                            user.getEvaluations().add(Math.min(prevE1 + room.getStep(), room.getResource()));
                                        }
                                    }
                                    if (prev1.equals(prev2)) {
                                        user.getEvaluations().add(prevE1);
                                    }
                                } else {
                                    user.getEvaluations().add(user.getCost());
                                }
                            }
                        });


                        getMethod(room.getPriorityMethod()).calc(room, users);
                        users.forEach(u -> u.calcResult(room.getPenalty()));
                        room.calcResult(users);

                        room.setNextTickAt(new Date().getTime() + 30000);
                        room.setCurrentStepNumber(room.getCurrentStepNumber() + 1);
                    }
                    room.setFinished(true);
                } else {
                    clear();
                }
            } else {
                clear();
            }
        };
        crons.add(this);
        RoomsDao.rooms
                .stream()
                .filter(room1 -> Objects.equals(room1.getId(), roomId))
                .findFirst()
                .ifPresent(room -> {
                    room.setCurrentStepNumber(room.getCurrentStepNumber() + 1);
                    this.runnable.run();
                    // scheduledFuture = scheduler.schedule(this.runnable,60, TimeUnit.MINUTES);
                    // this.flush();
                });
    }

    public void clear() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        crons.remove(this);
    }

    public void flush() {
        this.scheduledFuture.cancel(false);
        if (this.scheduledFuture.isCancelled()) {
            this.runnable.run();
        }
    }

    private Cron(Runnable runnable) {}
}

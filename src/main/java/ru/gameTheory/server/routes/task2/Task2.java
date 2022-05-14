package ru.gameTheory.server.routes.task2;

import com.auth0.jwt.interfaces.Claim;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.gameTheory.server.routes.task2.controllers.ConnectUserController;
import ru.gameTheory.server.routes.task2.dao.RoomsDao;
import ru.gameTheory.server.routes.task2.dao.Users;
import ru.gameTheory.server.routes.task2.models.Room;
import ru.gameTheory.server.routes.task2.models.User;
import ru.gameTheory.server.utils.JWT;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.stream.Collectors;

public class Task2 {

    private static class CreateRoomBody {
        private String priorityMethod;
        private Double penalty;
        private Double step;
        private Double resource;
        private String roomType;
        private Integer usersNumber;
        private Integer stepsCount;
    }

    public static String connectUser(Request request, Response response) {
        final Map<String, Object> result = new HashMap<>();
        final ConnectUserController.ConnectUserBody connectUserBody = (new Gson()).fromJson(request.body(), ConnectUserController.ConnectUserBody.class);

        final Optional<Room> roomOptional = RoomsDao.rooms.stream()
            .filter(room -> Objects.equals(room.getId(), connectUserBody.roomId))
            .findFirst();
        if (roomOptional.isPresent()) {
            final Room room = roomOptional.get();
            if (room.getStarted()) {
                response.status(403);
            } else {
                final User newUser = ConnectUserController.connectUser(connectUserBody, room);
                final String userToken = JWT.createJWT(builder -> {
                    builder.withClaim("roomId", connectUserBody.roomId);
                    builder.withClaim("center", false);
                    builder.withClaim("userId", newUser.getId());
                });
                result.put("token", userToken);
            }
        } else {
            response.status(500);
        }
        response.type("application/json");
        return (new Gson()).toJson(result);
    }

    public static String getInfoForParticipant(Request request, Response response) {
        final Map<String, Claim> claimMap = JWT.getJWTClaims(request.queryParams("token"));

        final Map<String, Object> result = new HashMap<>();
        final Long roomId = claimMap.get("roomId").asLong();
        final Long userId = claimMap.get("userId").asLong();
        final Optional<Room> roomOptional = RoomsDao.rooms.stream().filter(room -> Objects.equals(room.getId(), roomId)).findFirst();
        final Optional<User> userOptional = Users.users.stream().filter(user -> Objects.equals(user.getId(), userId)).findFirst();

        if (roomOptional.isPresent() && userOptional.isPresent()) {
            final Room room = roomOptional.get();
            final User user = userOptional.get();
            result.put("roomId", roomId);
            result.put("results", user.getResults());
            result.put("order", user.getOrder());
            result.put("evaluations", user.getEvaluations());
            result.put("initValuePassed", user.isFirstEvaluationInit());
            result.put("cost", user.getCost());
            result.put("roomStarted", room.getStarted());
            result.put("nextTickAt", room.getNextTickAt());
            result.put("currentStep", room.getCurrentStepNumber());
            result.put("step", room.getStep());
            result.put("finished", room.getFinished());
            result.put("participantsCount", room.getParticipantsCount());
            response.status(200);
            response.type("application/json");
            return (new Gson()).toJson(result);
        } else {
            response.status(500);
            return "{}";
        }
    }

    public static String stopRoom(Request request, Response response) {
        final Map<String, Claim> claimMap = JWT.getJWTClaims(request.queryParams("token"));
        final Long roomId = claimMap.get("roomId").asLong();
        RoomsDao.rooms.stream().filter(room -> Objects.equals(room.getId(), roomId)).findFirst().ifPresent(room -> {
            room.setStarted(false);
            room.setFinished(true);
        });

        response.status(200);
        response.type("application/json");
        return "{}";
    }

    public static String getInfoForCenter(Request request, Response response) {
        final Map<String, Claim> claimMap = JWT.getJWTClaims(request.queryParams("token"));

        final Map<String, Object> result = new HashMap<>();
        final Long roomId = claimMap.get("roomId").asLong();
        final Boolean center = claimMap.get("center").asBoolean();
        final Optional<Room> roomOptional = RoomsDao.rooms.stream().filter(room -> Objects.equals(room.getId(), roomId)).findFirst();
        final List<User> users = Users.users.stream()
                .filter(user -> Objects.equals(user.getRoomId(), roomId))
                .sorted(Comparator.comparingLong(User::getId))
                .collect(Collectors.toList());

        if (roomOptional.isPresent() && center) {
            final Room room = roomOptional.get();
            result.put("roomId", roomId);
            result.put("roomStarted", room.getStarted());
            result.put("roomType", room.getRoomType());
            result.put("nextStepAfter", room.getNextTickAt() - new Date().getTime());
            result.put("nextTickAt", room.getNextTickAt());
            result.put("currentStep", room.getCurrentStepNumber());
            result.put("step", room.getStep());
            result.put("finished", room.getFinished());
            result.put("participantsCount", room.getParticipantsCount());
            result.put("results", room.getCenterResults());
            result.put("users", users);
            response.status(200);
            response.type("application/json");
            System.out.println(new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(result));
            return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(result);
        } else {
            response.status(500);
            return "{}";
        }
    }

    private static class SetUserDecisionBody {
        private String token;
        private String decision;
        private Double initValue;
    }

    public static String setUserDecision(Request request, Response response) {
        final Gson gson = new Gson();
        final SetUserDecisionBody body = gson.fromJson(request.body(), SetUserDecisionBody.class);
        response.type("application/json");

        final Map<String, Claim> claimMap = JWT.getJWTClaims(body.token);
        final Long userId = claimMap.get("userId").asLong();
        final Long roomId = claimMap.get("roomId").asLong();

        final Optional<User> userOptional = Users.users.stream()
                .filter(streamUser -> Objects.equals(streamUser.getId(), userId))
                .findFirst();

        final Optional<Room> roomOptional = RoomsDao.rooms.stream()
                .filter(room -> Objects.equals(room.getId(), roomId))
                .findFirst();

        if (userOptional.isPresent() && roomOptional.isPresent()) {
            final Room room = roomOptional.get();
            final User user = userOptional.get();

            if (user.getEvaluations().size() >= room.getCurrentStepNumber()) {
                response.status(500);
                return "{}";
            }

            if (!user.isFirstEvaluationInit()) {
                user.getEvaluations().add(body.initValue);
            } else {
                switch (body.decision) {
                    case "up": {
                        user.getEvaluations().add(user.getEvaluations().get(user.getEvaluations().size() - 1) + room.getStep());
                        break;
                    }
                    case "down": {
                        user.getEvaluations().add(user.getEvaluations().get(user.getEvaluations().size() - 1) - room.getStep());
                        break;
                    }
                    case "equal": {
                        user.getEvaluations().add(user.getEvaluations().get(user.getEvaluations().size() - 1));
                        break;
                    }
                    default: {
                        response.status(500);
                        return "{}";
                    }
                }
            }

            if (Users.users.stream().filter(user1 -> Objects.equals(user1.getRoomId(), roomId)).allMatch(user1 -> user1.getEvaluations().size() == room.getCurrentStepNumber())) {
                Cron.crons.stream()
                        .filter(cron -> Objects.equals(cron.getRoomId(), roomId))
                        .findFirst()
                        .ifPresent(Cron::flush);
            }
        }

        return "{}";
    }

    public static class SetUsersBody {
        public static class UserSetting {
            public Long userId;
            public Double value;
        }

        private List<UserSetting> settings = new LinkedList<>();
        private List<UserSetting> initValues = new LinkedList<>();
        private String token;
    }

    public static String setUsers(Request request, Response response) {
        final Gson gson = new Gson();
        final SetUsersBody setUsersBody = gson.fromJson(request.body(), SetUsersBody.class);

        final Map<String, Claim> claimMap = JWT.getJWTClaims(setUsersBody.token);
        final Long roomId = claimMap.get("roomId").asLong();
        final Boolean center = claimMap.get("center").asBoolean();

        if (!center) {
            response.status(403);
            response.type("text/plain");
            return "not ok";
        }

        setUsersBody.settings.forEach(userSetting -> Users.users.stream()
            .filter(streamUser -> Objects.equals(streamUser.getId(), userSetting.userId))
            .findFirst()
            .ifPresent(user -> {
                user.setCost(userSetting.value);
            }));

        RoomsDao.rooms.stream()
            .filter(room -> Objects.equals(room.getId(), roomId))
            .findFirst()
            .ifPresent(room -> {
                room.setStarted(true);
                if (room.getRoomType().equals("imitation")) {
                    new Cron(roomId, setUsersBody.initValues);
                } else {
                    new Cron(roomId);
                }
            });

        response.status(200);
        response.type("text/plain");
        return "ok";
    }

    private static class FinishRegistrationBody {
        private String token;
    }
    public static String finishRegistration(Request request, Response response) {
        response.type("text/plain");
        final FinishRegistrationBody body = (new Gson()).fromJson(request.body(), FinishRegistrationBody.class);
        final Long roomId = JWT.getJWTClaims(body.token).get("roomId").asLong();

        final Optional<Room> roomOptional = RoomsDao.rooms.stream()
                .filter(room -> Objects.equals(room.getId(), roomId))
                .findFirst();

        if (roomOptional.isPresent()) {
            roomOptional.get().setConnectAllowed(false);
            response.status(200);
            return "ok";
        }

        response.status(500);
        return "ok";
    }

    public static String roomParticipantsInfo(Request request, Response response) {
        final String token = request.queryParams("token");
        final Map<String, Claim> claimMap = JWT.getJWTClaims(token);
        final Long roomId = claimMap.get("roomId").asLong();

        final Map<String, Object> result = new HashMap<>();

        final Long count = Users.users.stream().filter(user -> Objects.equals(user.getRoomId(), roomId))
                .count();

        result.put("count", count);
        result.put("roomId", roomId);

        response.type("application/json");
        response.status(200);

        return new Gson().toJson(result);
    }

    public static String createRoom(Request request, Response response) {
        final Room newRoom = new Room();
        final CreateRoomBody body = (new Gson()).fromJson(request.body(), CreateRoomBody.class);
        newRoom.setId(RoomsDao.lastRoomId++);
        newRoom.setPenalty(body.penalty);
        newRoom.setResource(body.resource);
        newRoom.setPriorityMethod(body.priorityMethod);
        newRoom.setStep(body.step);
        newRoom.setStarted(false);
        newRoom.setFinished(false);
        newRoom.setStatus("created");
        newRoom.setNextTickAt(0L);
        newRoom.setNextTickAfter(0L);
        newRoom.setCurrentStepNumber(0);
        newRoom.setConnectAllowed(true);
        newRoom.setParticipantsCount(0);
        newRoom.setRoomType(body.roomType);
        newRoom.setStepsCount(body.stepsCount);
        newRoom.setUsersNumber(body.usersNumber);

        if (Objects.equals(body.roomType, "imitation")) {
            for(int i = 0; i < body.usersNumber; i++) {
                ConnectUserController.connectUser(new ConnectUserController.ConnectUserBody() {{
                    this.roomId = newRoom.getId();
                }}, newRoom);
            }
        }

        final String roomToken = JWT.createJWT(builder -> {
            builder.withClaim("roomId", newRoom.getId());
            builder.withClaim("center", true);
        });

        RoomsDao.rooms.add(newRoom);

        response.type("application/json");
        response.status(200);

        final Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", roomToken);
        return (new Gson()).toJson(responseMap);
    }
}

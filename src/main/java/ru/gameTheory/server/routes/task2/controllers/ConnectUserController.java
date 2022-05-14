package ru.gameTheory.server.routes.task2.controllers;

import ru.gameTheory.server.routes.task2.dao.Users;
import ru.gameTheory.server.routes.task2.models.Room;
import ru.gameTheory.server.routes.task2.models.User;
import ru.gameTheory.server.utils.JWT;

public class ConnectUserController {

    public static class ConnectUserBody {
        public Long roomId;
    }

    public static User connectUser(ConnectUserBody connectUserBody, Room room) {
        final User newUser = new User();
        newUser.setId(Users.lastUserId++);
        newUser.setRoomId(connectUserBody.roomId);
        newUser.setOrder(room.getParticipantsCount() + 1);
        Users.users.add(newUser);
        room.setParticipantsCount(room.getParticipantsCount() + 1);
        final String userToken = JWT.createJWT(builder -> {
            builder.withClaim("roomId", connectUserBody.roomId);
            builder.withClaim("center", false);
            builder.withClaim("userId", newUser.getId());
        });
        return newUser;
    }
}

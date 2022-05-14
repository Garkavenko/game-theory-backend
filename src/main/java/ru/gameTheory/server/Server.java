package ru.gameTheory.server;

import ru.gameTheory.server.routes.task1.Task1;
import ru.gameTheory.server.routes.task2.Task2;

import static spark.Spark.*;

public class Server {

    public static void start() {
        port(8080);
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request
                    .headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers",
                        accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request
                    .headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods",
                        accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        post("/createRoom", Task1::createRoom);
        post("/finishRegistration", Task1::finishRegistration);
        get("/roomParticipantsInfo", Task1::roomParticipantsInfo);
        post("/connectUser", Task1::connectUser);
        get("/getInfoForParticipant", Task1::getInfoForParticipant);
        get("/getInfoForCenter", Task1::getInfoForCenter);
        post("/setUsers", Task1::setUsers);
        post("/stopRoom", Task1::stopRoom);
        post("/setUserDecision", Task1::setUserDecision);

        post("/task2/createRoom", Task2::createRoom);
        post("/task2/finishRegistration", Task2::finishRegistration);
        get("/task2/roomParticipantsInfo", Task2::roomParticipantsInfo);
        post("/task2/connectUser", Task2::connectUser);
        get("/task2/getInfoForParticipant", Task2::getInfoForParticipant);
        get("/task2/getInfoForCenter", Task2::getInfoForCenter);
        post("/task2/setUsers", Task2::setUsers);
        post("/task2/stopRoom", Task2::stopRoom);
        post("/task2/setUserDecision", Task2::setUserDecision);

    }
}

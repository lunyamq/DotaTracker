package ru.sfedu.project.db;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.jar.JarEntry;

import static org.junit.jupiter.api.Assertions.*;

class SqlDatabaseClientTest {
    @Test
    void getMatchData() throws Exception {
        JSONObject match = SqlDatabaseClient.getMatchData("6227492909");
        assertNotNull(match, "Match is Null");

        int deleteCode = SqlDatabaseClient.deleteMatchData("6227492909");
        assertEquals(deleteCode, 1, "Delete match error");
    }

    @Test
    void getPlayerData() throws Exception {
        JSONObject player = SqlDatabaseClient.getPlayerData("321580662");
        assertNotNull(player, "Player is Null");

        int deleteCode = SqlDatabaseClient.deletePlayerData("321580662");
        assertEquals(deleteCode, 1, "Delete player error");
    }
}
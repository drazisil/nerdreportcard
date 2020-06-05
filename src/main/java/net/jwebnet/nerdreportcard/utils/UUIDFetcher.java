/*
 * Copyright (C) 2014
 *
 * evilmidget's UUID Fetcher.
 * See https://gist.github.com/evilmidget38/26d70114b834f71fb3b4
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.jwebnet.nerdreportcard.utils;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<Map<String, UUID>> {
    private static final int PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    public UUIDFetcher(List<String> names, boolean rateLimiting) {
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    public UUIDFetcher(List<String> names) {
        this(names, true);
    }

    public Map<String, UUID> call() throws Exception {
        Map<String, UUID> uuidMap = new HashMap<String, UUID>();
        int requests = (int) Math.floor(names.size() / PROFILES_PER_REQUEST) + 1;
        for (int i = 0; i < requests; i++) {
            HttpURLConnection connection = createConnection();
            String body = JSONArray.toJSONString(names.subList(i * PROFILES_PER_REQUEST, Math.min((i + 1) * PROFILES_PER_REQUEST, names.size())));
            writeBody(connection, body);
            JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            for (Object profile : array) {
                JSONObject jsonProfile = (JSONObject) profile;
                String id = (String) jsonProfile.get("id");
                String name = (String) jsonProfile.get("name");
                UUID uuid = UUIDFetcher.getUUID(id);
                uuidMap.put(name.toLowerCase(), uuid);
            }
            if (rateLimiting && i != requests - 1) {
                Thread.sleep(100L);
            }
        }
        return uuidMap;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" +id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static UUID getUUIDOf(String name) throws Exception {
        return new UUIDFetcher(Arrays.asList(name)).call().get(
                name.toLowerCase());
    }
}
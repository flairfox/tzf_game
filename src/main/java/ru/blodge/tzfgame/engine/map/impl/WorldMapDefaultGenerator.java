package ru.blodge.tzfgame.engine.map.impl;

import ru.blodge.tzfgame.constants.Direction;
import ru.blodge.tzfgame.engine.map.MapGenerator;
import ru.blodge.tzfgame.engine.model.Room;
import ru.blodge.tzfgame.engine.model.WorldMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class WorldMapDefaultGenerator implements MapGenerator {

    Random random = new Random();

    @Override
    public WorldMap generate(int roomsCount) {
        int width = roomsCount * 2 + 1;
        int height = roomsCount * 2 + 1;

        int[][] intMap = initIntMap(width, height);

        Room firstRoom = new Room(width / 2, height / 2);
        intMap[firstRoom.getX()][firstRoom.getY()] = 1;

        List<Room> rooms = new LinkedList<>();
        rooms.add(firstRoom);

        Queue<Room> roomsQueue = new LinkedList<>();
        Queue<Room> deadEnds = new LinkedList<>();
        roomsQueue.add(firstRoom);

        boolean isEnough = false;
        while (!isEnough) {
            Room currentRoom = roomsQueue.remove();

            boolean isDeadEnd = true;
            for (Direction direction : Direction.values()) {
                Room nextRoom = nextRoom(
                        intMap,
                        currentRoom,
                        direction);
                if (nextRoom != null) {
                    intMap[nextRoom.getX()][nextRoom.getY()] = 1;
                    linkRooms(currentRoom, nextRoom, direction);
                    rooms.add(nextRoom);
                    if (rooms.size() == roomsCount) {
                        isEnough = true;
                        break;
                    }

                    roomsQueue.add(nextRoom);
                    isDeadEnd = false;
                }
            }

            if (isDeadEnd)
                deadEnds.add(currentRoom);

            if (roomsQueue.isEmpty() && rooms.size() < roomsCount) {
                roomsQueue.add(deadEnds.remove());
            }
        }

        return normalizeToMap(rooms);
    }

    private void linkRooms(Room currentRoom, Room newRoom, Direction direction) {
        switch (direction) {
            case NORTH -> {
                currentRoom.setNorth(newRoom);
                newRoom.setSouth(currentRoom);
            }
            case EAST -> {
                currentRoom.setEast(newRoom);
                currentRoom.setWest(currentRoom);
            }
            case SOUTH -> {
                currentRoom.setSouth(newRoom);
                newRoom.setNorth(currentRoom);
            }
            case WEST -> {
                currentRoom.setWest(newRoom);
                newRoom.setEast(currentRoom);
            }
        }
    }

    private WorldMap normalizeToMap(List<Room> rooms) {
        int minX = Integer.MAX_VALUE;
        int maxX = 0;

        int minY = Integer.MAX_VALUE;
        int maxY = 0;

        for (Room room : rooms) {
            if (room.getX() < minX)
                minX = room.getX();

            if (room.getX() > maxX)
                maxX = room.getX();

            if (room.getY() < minY)
                minY = room.getY();

            if (room.getY() > maxY)
                maxY = room.getY();
        }

        int width = maxX - minX + 3;
        int height = maxY - minY + 3;

        for (Room room : rooms) {
            room.setX(room.getX() - minX + 1);
            room.setY(room.getY() - minY + 1);
        }

        WorldMap normalizedMap = new WorldMap(width, height);
        rooms.forEach(normalizedMap::addRoom);

        return normalizedMap;
    }

    private Room nextRoom(
            int[][] map,
            Room currentRoom,
            Direction direction) {

        int newX = currentRoom.getX() + direction.xShift();
        int newY = currentRoom.getY() + direction.yShift();

        if (map[newX][newY] == 0)
            if (countNeighbors(map, newX, newY) < 2)
                if (random.nextBoolean())
                    return new Room(newX, newY);

        return null;
    }

    private int countNeighbors(
            int[][] map,
            int roomX,
            int roomY) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            int x = roomX + direction.xShift();
            int y = roomY + direction.yShift();

            if (map[x][y] == 1)
                count++;
        }

        return count;
    }

    private int[][] initIntMap(int width, int height) {
        int[][] intMap = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                intMap[i][j] = 0;
            }
        }

        return intMap;
    }

}

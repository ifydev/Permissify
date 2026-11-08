package me.innectic.permissify.api.permission;

public record RankChange(Status status, String from, String to) {
    public enum Status {
        SUCCESS, NOT_FOUND, EMPTY, NOT_ON_TRACK, AMBIGUOUS, END, DATABASE_ERROR
    }
}

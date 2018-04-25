package models;

public final class User {
    private String name;
    private int token;

    public String getName() {
        return name;
    }

    public int getToken() {
        return token;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public User setToken(int token) {
        this.token = token;
        return this;
    }
}

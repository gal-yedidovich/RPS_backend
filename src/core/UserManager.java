package core;

import models.User;

import java.util.HashMap;

public enum UserManager {
	instance;

	private HashMap<Integer, User> cache = new HashMap<>();

	public void putUser(int token, User user) {
		cache.put(token, user);
	}

	public User get(int token) {
		return cache.get(token);
	}

	public boolean tokenExists(int token) {
		return cache.containsKey(token);
	}

	public void removeUser(int token) {
		cache.remove(token);
	}
}

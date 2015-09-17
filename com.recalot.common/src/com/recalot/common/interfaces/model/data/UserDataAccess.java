package com.recalot.common.interfaces.model.data;

import com.recalot.common.communication.Message;
import com.recalot.common.communication.User;
import com.recalot.common.exceptions.BaseException;

import java.util.Map;

/**
 * @author Matthaeus.schmedding
 */
public interface UserDataAccess {
    public User[] getUsers() throws BaseException;
    public int getUsersCount();
    public User getUser(String userId) throws BaseException;
    public User tryGetUser(String userId) throws BaseException;
    public User updateUser(String userId, Map<String, String> content) throws BaseException;
    public User createUser(Map<String, String> content) throws BaseException;
}

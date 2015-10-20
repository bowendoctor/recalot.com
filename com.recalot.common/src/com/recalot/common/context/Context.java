package com.recalot.common.context;

import com.recalot.common.communication.Service;

import java.util.Date;

/**
 * @author Matthäus Schmedding (info@recalot.com)
 */
public interface Context extends Service {
    public Object getContext(String userId);
    public Object getContext(String userId, String itemId);
    public Object getContext(String userId, String itemId, Date timestamp);
}


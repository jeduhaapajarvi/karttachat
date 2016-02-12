package com.tommilaurila.karttachat;

/**
 * Created by Sakari on 12.02.2016.
 */
//TODO merge this to networkPostTask, current implementation is hasty and could be improved
public interface NetworkTaskListener {
    void onTaskComplete(boolean taskSuccess);
}

package com.tommilaurila.karttachat;

/**
 * Created by tommi.laurila on 19.5.2015.
 */
public class Group {
    private int group_id;
    private int creator; // ryhmän luojan id
    private String groupName;
    private String groupPassword;
    private String creationTime;

    // konstruktori
    public Group() {}

    // get- ja set-metodit
    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPassword() {
        return groupPassword;
    }

    public void setGroupPassword(String groupPassword) {
        this.groupPassword = groupPassword;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    // ylikirjoitetaan toString-metodi siten, että se
    // palauttaa ryhmän nimen
    @Override
    public String toString() {
        return this.groupName;
    }
}

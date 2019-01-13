package com.example.manalighare.inclass08;

public class Task {

    String id,note,priority,time,status ;

    public Task(String id,String note, String priority, String time, String status) {
        this.id=id;
        this.note = note;
        this.priority = priority;
        this.time = time;
        this.status = status;
    }

    public Task() {
    }


    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", note='" + note + '\'' +
                ", priority='" + priority + '\'' +
                ", time='" + time + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

package com.planner.aeder.planner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class schedulesClasses {
    public static class CalendarDay {
        private int year;
        private int month;
        private int day;
        private List<Schedule> schedules = new ArrayList<>();

        public CalendarDay(int year, int month, int day, Schedule schedule) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.schedules.add(schedule);
        }

        public CalendarDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public void addSchedule(Schedule schedule) {
            this.schedules.add(schedule);
            Collections.sort(schedules, new SchedulesSorter());
        }

        public List<Schedule> getSchedules(){ return schedules; }
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public int getDay() { return day; }
    }

    public static class Schedule{
        public int hour;
        public int minute;
        public String title;
        public String text;
        public int total;

        public Schedule(int hour, int minute, String title, String text){
            this.hour = hour;
            this.minute = minute;
            this.title = title;
            this.text = text;
            this.total = hour * 60 + minute;
        }

        public Schedule(int hour, String title, String text){
            this.hour = hour;
            this.minute = 0;
            this.title = title;
            this.text = text;
            this.total = hour * 60 + minute;
        }

        public Schedule(int hour, int minute, String title){
            this.hour = hour;
            this.minute = minute;
            this.title = title;
            this.text = "";
            this.total = hour * 60 + minute;
        }

        public Schedule(int hour, String title){
            this.hour = hour;
            this.minute = 0;
            this.title = title;
            this.text = "";
            this.total = hour * 60 + minute;
        }

        public String getTitle() { return title; }
        public String getText() { return text; }
        public int getMinute() { return minute; }
        public int getHour() { return hour; }
    }

    static class SchedulesSorter implements Comparator<Schedule> {
        public int compare(Schedule a, Schedule b) {
            if ( a.total < b.total ) return -1;
            else if ( a.total == b.total ) return 0;
            else return 1;
        }
    }
}

package com.aragaer.jtt;
 
import com.aragaer.jtt.JTTHour;
 
interface IJTTService {
    JTTHour getHour();
    void startNotifying();
    void stopNotifying();
    void stopService();
}
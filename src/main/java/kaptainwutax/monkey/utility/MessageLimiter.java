package kaptainwutax.monkey.utility;

import com.google.gson.annotations.Expose;

public class MessageLimiter {

    @Expose public int everyone;
    @Expose public int here;
    @Expose public int role;
    @Expose public int user;

    public MessageLimiter(int everyone, int here, int role, int user) {
        this.everyone = everyone;
        this.here = here;
        this.role = role;
        this.user = user;
    }

    public MessageLimiter(int[] data) {
        this.everyone = data[0];
        this.here = data[1];
        this.role = data[2];
        this.user = data[3];
    }

    public boolean respectsLimits(MessageLimiter limiter) {
        if(limiter.everyone >= 0 && this.everyone > limiter.everyone)return false;
        else if(limiter.here >= 0 && this.here > limiter.here)return false;
        else if(limiter.role >= 0 && this.role > limiter.role)return false;
        else return limiter.user < 0 || this.user <= limiter.user;
    }

}

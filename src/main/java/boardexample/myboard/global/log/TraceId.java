package boardexample.myboard.global.log;

import boardexample.myboard.global.util.security.SecurityUtil;

import java.util.UUID;

public class TraceId {

    private String id;
    private int level;//깊이


    public TraceId() {
        this.id = createId();
        this.level = 0;
    }

    public TraceId(String id, int level) {
        this.id = id;
        this.level = level;
    }

    private String createId() {
        try {
            SecurityUtil.getLoginUsername();
        }catch (NullPointerException | ClassCastException e ){ //로그인 안하고 접근 & signUp등일 경우 anonymousUser가 반환되므로 캐스팅이 불가능
            return String.format("[Anonymous: %S]",UUID.randomUUID().toString().substring(0,8));
        }
        return SecurityUtil.getLoginUsername();
    }

    public TraceId createNextId(){
        return new TraceId(id, level + 1);
    }

    public TraceId createPreviousId(){
        return new TraceId(id, level - 1);
    }

    public boolean isFirstLevel(){
        return level == 0;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }
}

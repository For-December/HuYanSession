package cn.chahuyun.session.entity;

import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :多词条消息的消息信息
 * @Date 2022/8/17 19:20
 */
@Entity
@Table(name = "QuartzSession")
public class QuartzSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 是否包含动态消息参数
     */
    private boolean dynamic;
    /**
     * 是否是转发或语音消息
     */
    private boolean other;
    /**
     * 匹配定时任务的多消息
     */
    @JoinColumn(name = "quartzMessage_id")
    private int quartzMessageId;

    /**
     * 回复消息
     */
    @Column(length = 10240)
    private String reply;

    public QuartzSession() {
    }


    public QuartzSession(long bot, boolean dynamic, boolean other, String reply) {
        this.bot = bot;
        this.dynamic = dynamic;
        this.other = other;
        this.reply = reply;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public int getQuartzMessageId() {
        return quartzMessageId;
    }

    public void setQuartzMessageId(int quartzMessageID) {
        this.quartzMessageId = quartzMessageID;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }
}

package cn.chahuyun.sessionManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.SessionDataBase;
import cn.chahuyun.files.PluginData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.Random;

/**
 * SessionDialogue
 *
 * @author Zhangjiaxing
 * @description 关键词对话的消息触发
 * @date 2022/6/16 14:36
 */
public class SessionDialogue {

    public static final SessionDialogue INSTANCE = new SessionDialogue();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * @description 传递消息监视和指定的关键词对话
     * @author zhangjiaxing
     * @param messageEvent 消息事件
     * @param sessionDataBase 对话类
     * @date 2022/6/16 15:17
     */
    public void session(MessageEvent messageEvent,SessionDataBase sessionDataBase) {
        Contact subject = messageEvent.getSubject();
        switch (sessionDataBase.getType()) {
            case 2:
                //轮询回复
                l.info("轮询回复");
                //获取key
                String key = sessionDataBase.getKey();
                //获取下一次轮询下标
                int i = PluginData.INSTANCE.addPollNum(key);
                ArrayList<String> values = sessionDataBase.getValues();
                //判断轮询坐标
                i = i % values.size();
                //发送
                subject.sendMessage(MiraiCode.deserializeMiraiCode(values.get(i)));
                break;
            case 3:
                l.info("随机回复");
                //随机回复
                //获取随机回复下标
                Random random = new Random();
                ArrayList<String> valueList = sessionDataBase.getValues();
                int nextInt = random.nextInt(valueList.size());
                //发送
                subject.sendMessage(MiraiCode.deserializeMiraiCode(valueList.get(nextInt)));
                break;
            default:
                subject.sendMessage(MiraiCode.deserializeMiraiCode(sessionDataBase.getValue()));
                break;
        }


    }

}
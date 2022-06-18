package cn.chahuyun.Session;

import cn.chahuyun.GroupSession;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.data.SessionDataPaging;
import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import static cn.chahuyun.GroupSession.sessionData;

/**
 * 对话信息检测
 *
 * @author Zhangjiaxing
 * @description 检测对话是否合格
 * @date 2022/6/8 9:34
 */
public class SessionManage {

    public static final SessionManage INSTANCE = new SessionManage();

    private MiraiLogger l = GroupSession.INSTANCE.getLogger();

    /**
     * 学习正则
     */
    public String studyPattern = "(学习 [\\d\\w\\S\\u4e00-\\u9fa5]+ [\\d\\w\\S\\u4e00-\\u9fa5]+( ?(精准|模糊|头部|结尾))?)";
    /**
     * 查询正则
     */
    public String queryPattern = "(查询 ?([\\d\\w\\S\\u4e00-\\u9fa5])*)";
    /**
     * 删除正则
     */
    public String deletePattern = "(删除 ?([\\d\\w\\S\\u4e00-\\u9fa5])*)";


    /**
     * @description 判断该消息是不是规定字符
     * @author zhangjiaxing
     * @param messageChain 消息链
     * @date 2022/6/8 12:32
     * @return boolean
     */
    public boolean isString(MessageChain messageChain) {
        return false;
    }

    /**
     * @description 学习词汇的方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 20:56
     * @return boolean
     */
    public boolean studySession(MessageEvent event) {
        String messageString = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        //判断学习语法结构是否正确
        if (!Pattern.matches(studyPattern, messageString)) {
            subject.sendMessage("学习失败！学习结构应为：");
            subject.sendMessage("学习 (触发关键词) (回复内容) [精准|模糊|头部|结尾]");
            return false;
        }

        //分割学习数据
        String[] strings = messageString.split(" ");
        l.info(Arrays.toString(strings));
        //当为3时默认精准匹配
        if (strings.length == 3) {
            int type = 0;
            if (Pattern.matches("\\[mirai:", strings[1])) {
                type = 1;
            }
            //type = 0 为string类回复
            sessionData.put(strings[1],new SessionDataBase(strings[1],type,strings[2],null, DataEnum.ACCURATE));
            subject.sendMessage(new MessageChainBuilder().append("学习成功! ")
                    .append(MiraiCode.deserializeMiraiCode(strings[1]))
                    .append(" -> ")
                    .append(MiraiCode.deserializeMiraiCode(strings[2]))
                    .build());
            return true;
        }
        //查看结尾匹配类型
        switch (strings[3]) {
            case "精准":
                int jType = 0;
                if (Pattern.matches("\\[mirai:", strings[1])) {
                    jType = 1;
                }
                //type = 0 为string类回复
                sessionData.put(strings[1],new SessionDataBase(strings[1],jType,strings[2],null, DataEnum.ACCURATE));
                subject.sendMessage(new MessageChainBuilder().append("学习精准匹配成功! ")
                        .append(MiraiCode.deserializeMiraiCode(strings[1]))
                        .append(" -> ")
                        .append(MiraiCode.deserializeMiraiCode(strings[2]))
                        .build());
                break;
            case "模糊":
                int mType = 0;
                if (Pattern.matches("\\[mirai:", strings[1])) {
                    mType = 1;
                }
                sessionData.put(strings[1],new SessionDataBase(strings[1],mType,strings[2],null, DataEnum.VAGUE));
                subject.sendMessage(new MessageChainBuilder().append("学习模糊匹配成功! ")
                        .append(MiraiCode.deserializeMiraiCode(strings[1]))
                        .append(" -> ")
                        .append(MiraiCode.deserializeMiraiCode(strings[2]))
                        .build());
                break;
            case "头部":
                int tType = 0;
                if (Pattern.matches("\\[mirai:", strings[1])) {
                    tType = 1;
                }
                sessionData.put(strings[1],new SessionDataBase(strings[1],tType,strings[2],null, DataEnum.START));
                subject.sendMessage(new MessageChainBuilder().append("学习头部匹配成功! ")
                        .append(MiraiCode.deserializeMiraiCode(strings[1]))
                        .append(" -> ")
                        .append(MiraiCode.deserializeMiraiCode(strings[2]))
                        .build());
                break;
            case "结尾":
                int wType = 0;
                if (Pattern.matches("\\[mirai:", strings[1])) {
                    wType = 1;
                }
                sessionData.put(strings[1],new SessionDataBase(strings[1],wType,strings[2],null, DataEnum.END));
                subject.sendMessage(new MessageChainBuilder().append("学习结尾匹配成功! ")
                        .append(MiraiCode.deserializeMiraiCode(strings[1]))
                        .append(" -> ")
                        .append(MiraiCode.deserializeMiraiCode(strings[2]))
                        .build());
                break;
            default:break;
        }
        return false;
    }

    /**
     * @description 查询所有词汇的方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 20:57
     * @return boolean
     */
    public boolean querySession(MessageEvent event) {
        String messageString = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        //判断查询语法结构是否正确
        if (!Pattern.matches(queryPattern, messageString)) {
            subject.sendMessage("查询失败！查询结构应为：");
            subject.sendMessage("查询 [页数/关键词]");
            return false;
        }
        //当 消息仅为 查询 时 默认查询第一页消息
        if (messageString.length()<=3) {
            sessionMessageInInt(event);
            return true;
        }

        //关键词查询
        String[] strings = messageString.split(" ");
        Set<String> keySet = sessionData.keySet();
        if (strings.length >= 2 && keySet.contains(strings[1])) {
            subject.sendMessage(new MessageChainBuilder().append("查询成功！ ")
                    .append(MiraiCode.deserializeMiraiCode(strings[1]))
                    .append(" -> ")
                    .append(MiraiCode.deserializeMiraiCode(sessionData.get(strings[1]).getValue()))
                    .build());
            return true;
        } else {
            subject.sendMessage("查询失败！ 没有找到我能说的话！");
        }


        return false;
    }


    /**
     * @description 删除一个关键词
     * @author zhangjiaxing
     * @param event 消息触发事件
     * @date 2022/6/17 19:44
     * @return java.lang.Boolean
     */
    public Boolean deleteSession(MessageEvent event) {
        String messageString = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        //删除语法结构查询
        if (!Pattern.matches(deletePattern, messageString)) {
            subject.sendMessage("删除失败！删除结构为:");
            subject.sendMessage("删除 (触发类容)");
        }
        //分割
        String[] strings = messageString.split(" ");
        String keySet = sessionData.keySet().toString();
        //只需要关键词的key数组中有这个词就行，map不需要知道它在那
        if (strings.length == 2 && keySet.contains(strings[1])) {
            sessionData.remove(strings[1]);
            subject.sendMessage(new MessageChainBuilder().append("删除成功！")
                    .append(MiraiCode.deserializeMiraiCode(strings[1]))
                    .build());
            return true;
        }
        subject.sendMessage("删除失败，没有找到你要删除的东西~");
        return false;
    }

    /**
     * @description 根据传递的数量来进行分页显示
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 22:08
     * @return java.lang.Boolean
     */
    private Boolean sessionMessageInInt(MessageEvent event) {
        //进行分页
//        SessionDataPaging dataPaging = SessionDataPaging.queryPageInfo(pageNum, PAGE_SIZE, new ArrayList<>(sessionData.values()));
        //构造消息
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.add("-----------所有关键词-----------\n");
        //循环添加内容
        Collection<SessionDataBase> values = sessionData.values();
        for (SessionDataBase base : values) {
            messageChainBuilder.add(" ");
            messageChainBuilder.add(MiraiCode.deserializeMiraiCode(base.getKey()));
            messageChainBuilder.add("\t->\t");
            messageChainBuilder.add(MiraiCode.deserializeMiraiCode(base.getValue()));
            messageChainBuilder.add(" : ");
            messageChainBuilder.add(base.getDataEnum().getType()+"\n");
        }
        messageChainBuilder.add("-------------------------------");
        MessageChain messages = messageChainBuilder.build();
        //构造转发消息
        ForwardMessage build = new ForwardMessageBuilder(event.getSubject()).add(event.getBot(), messages).build();
        //发送消息
        event.getSubject().sendMessage(build);
        return true;

    }

}
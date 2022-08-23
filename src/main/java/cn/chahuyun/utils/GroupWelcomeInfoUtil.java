package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupWelcomeInfo;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.entity.WelcomeMessage;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * GroupWelcomeInfoUtil
 * 群欢迎消息工具
 *
 * @author Moyuyanli
 * @date 2022/8/22 9:27
 */
public class GroupWelcomeInfoUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    public static void addGroupWelcomeInfo(MessageEvent event) throws ExecutionException, InterruptedException {
        Contact subject = event.getSubject();
        User user = event.getSender();
        Bot bot = event.getBot();

        subject.sendMessage("请输入欢迎消息:");
        MessageEvent nextMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser)) {
            return;
        }
        MessageChain message = nextMessageEventFromUser.getMessage();

        String value = message.serializeToMiraiCode();
        int type = 0;

        //判断是否存在动态消息
        String dynamicPattern = "\\$\\w+\\(\\S+?\\)";
        Pattern compile = Pattern.compile(dynamicPattern);
        if (compile.matcher(value).find()) {
            type = 1;
        }
        //判断消息是否是转发消息或音频消息
        if (message.contains(ForwardMessage.Key) || message.contains(Audio.Key)) {
            type = 2;
            value = MessageChain.serializeToJsonString(message);
        }

        //随机标识
        int randomMark = (int) (Math.random() * 100);
        //是否随机发送
        boolean random = false;

        subject.sendMessage("请发送参数(一次发送，多参数中间隔开):");
        MessageEvent nextMessageEventFromUser1 = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser1)) {
            return;
        }
        String param = nextMessageEventFromUser.getMessage().serializeToMiraiCode();

        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), 0);

        //解析参数
        String[] split = param.split(" +");
        for (String s : split) {
            switch (s) {
                case "*":
                case "随机":
                    random = true;
                    break;
                case "0":
                case "全局":
                    scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
                    break;
                default:
                    String listPattern = "gr\\d+|群组\\d+";
                    if (Pattern.matches(listPattern, s)) {
                        int listId = Integer.parseInt(s.substring(2));
                        if (ListUtil.isContainsList(bot, listId)) {
                            subject.sendMessage("该群组不存在!");
                            return;
                        }
                        scope = new Scope(bot.getId(), "群组", false, true, subject.getId(), listId);
                    }
                    break;
            }
        }

        //是否新建还是添加
        List<GroupWelcomeInfo> welcomeInfoList = null;
        try {
            welcomeInfoList = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupWelcomeInfo> query = builder.createQuery(GroupWelcomeInfo.class);
                JpaRoot<GroupWelcomeInfo> from = query.from(GroupWelcomeInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));

                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            l.error("出错啦!", e);
        }

        GroupWelcomeInfo groupWelcomeInfo;

        if (welcomeInfoList == null || welcomeInfoList.isEmpty()) {
            groupWelcomeInfo = new GroupWelcomeInfo(bot.getId(), random, 0, randomMark, scope);
        } else {
            Scope finalScope1 = scope;
            Optional<GroupWelcomeInfo> optional = welcomeInfoList.stream().filter(it -> it.getScopeMark().equals(finalScope1.getId())).findFirst();
            groupWelcomeInfo = optional.isPresent() ? optional.get() : new GroupWelcomeInfo(bot.getId(), random, 0, randomMark, scope);
        }

        //查询重复
        List<WelcomeMessage> welcomeMessages = groupWelcomeInfo.getWelcomeMessages();
        WelcomeMessage welcomeMessage = new WelcomeMessage(bot.getId(), type, randomMark, value);
        if (welcomeMessages.contains(welcomeMessage)) {
            subject.sendMessage("这条欢迎消息已经存在");
            return;
        }
        welcomeMessages.add(welcomeMessage);
        groupWelcomeInfo.setWelcomeMessages(welcomeMessages);
        groupWelcomeInfo.setScope(scope);
        //保存或更新
        try {
            Scope finalScope = scope;
            HibernateUtil.factory.fromTransaction(session -> {
                //判断对应作用域是否存在
                if (!ScopeUtil.isScopeEmpty(finalScope)) {
                    //不存在则先添加作用域
                    session.persist(finalScope);
                }
                session.merge(groupWelcomeInfo);
                return 0;
            });
        } catch (Exception e) {
            l.error("出错啦！", e);
            subject.sendMessage("欢迎词保存失败!");
            return;
        }
        subject.sendMessage("欢迎词保存成功!");
    }

    /**
     * 查询群欢迎词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/23 9:00
     */
    public static void queryGroupWelcomeInfo(MessageEvent event) {
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        List<GroupWelcomeInfo> welcomeInfoList = null;
        try {
            welcomeInfoList = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupWelcomeInfo> query = builder.createQuery(GroupWelcomeInfo.class);
                JpaRoot<GroupWelcomeInfo> from = query.from(GroupWelcomeInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));
                List<GroupWelcomeInfo> list = session.createQuery(query).list();
                for (GroupWelcomeInfo info : list) {
                    if (info.getScope() == null) {
                        info.setScope(Objects.requireNonNull(ScopeUtil.getScope(info.getScopeMark())));
                    }
                }
                return list;
            });
        } catch (Exception e) {
            l.error("出错啦!", e);
        }

        if (welcomeInfoList == null || welcomeInfoList.isEmpty()) {
            subject.sendMessage("欢迎词为空");
            return;
        }

        ForwardMessageBuilder builder = new ForwardMessageBuilder(subject);
        builder.add(bot, new PlainText("以下本是bot所有群欢迎词↓"));
        for (GroupWelcomeInfo welcomeInfo : welcomeInfoList) {
            List<WelcomeMessage> welcomeMessages = welcomeInfo.getWelcomeMessages();
            Scope scope = welcomeInfo.getScope();
            MessageChainBuilder messages = new MessageChainBuilder();
            messages.add(new PlainText("欢迎词集合编号:" + welcomeInfo.getRandomMark()));
            messages.add(new PlainText("\n作用方式:" + scope.getScopeName()));
            if (scope.isGroupInfo()) {
                messages.add(new PlainText("\n群组编号:" + scope.getListId()));
            } else if (!scope.isGlobal()) {
                messages.add(new PlainText("\n群号:" + scope.getGroupNumber()));
            }
            messages.add(new PlainText("\n触发方式:" + (welcomeInfo.isRandom() ? "随机" : "轮询")));
            builder.add(bot, messages.build());
            ForwardMessageBuilder forwardMsgBuilder = new ForwardMessageBuilder(subject);
            for (WelcomeMessage welcomeMessage : welcomeMessages) {
                forwardMsgBuilder.add(bot, new PlainText("id:" + welcomeMessage.getId() + "\n==>").plus(MiraiCode.deserializeMiraiCode(welcomeMessage.getWelcomeMessage())));
            }
            builder.add(bot, forwardMsgBuilder.build());
        }
        subject.sendMessage(builder.build());
    }

    /**
     * 删除欢迎词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/23 9:37
     */
    public static void deleteGroupWelcomeInfo(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("[:：]")[1].split(" +");
        int key = Integer.parseInt(split[0]);
        int toKey = 0;
        if (split.length > 1) {
            toKey = Integer.parseInt(split[1]);
        }
        List<GroupWelcomeInfo> groupWelcomeInfos = null;
        try {
            groupWelcomeInfos = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupWelcomeInfo> query = builder.createQuery(GroupWelcomeInfo.class);
                JpaRoot<GroupWelcomeInfo> from = query.from(GroupWelcomeInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));
                query.where(builder.equal(from.get("randomMark"), key));
                List<GroupWelcomeInfo> list = session.createQuery(query).list();
                for (GroupWelcomeInfo info : list) {
                    if (info.getScope() == null) {
                        info.setScope(Objects.requireNonNull(ScopeUtil.getScope(info.getScopeMark())));
                    }
                }
                return list;
            });
        } catch (Exception e) {
            l.error("出错啦!", e);
        }

        if (groupWelcomeInfos == null) {
            subject.sendMessage("没有要删除的欢迎词!");
            return;
        }
        if (toKey != 0) {
            List<WelcomeMessage> welcomeMessages = groupWelcomeInfos.get(0).getWelcomeMessages();
            int finalToKey = toKey;
            Optional<WelcomeMessage> first = welcomeMessages.stream().filter(it -> it.getId() == finalToKey).findFirst();
            if (first.isPresent()) {
                WelcomeMessage welcomeMessage = first.get();
                welcomeMessages.remove(welcomeMessage);
            }
            GroupWelcomeInfo finalGroupWelcomeInfos = groupWelcomeInfos.get(0);
            try {
                HibernateUtil.factory.fromTransaction(session -> {
                    session.merge(finalGroupWelcomeInfos);
                    return 0;
                });
            } catch (Exception e) {
                subject.sendMessage("欢迎词删除失败!");
                l.error("欢迎词删除失败!", e);
                return;
            }
            subject.sendMessage("欢迎词删除成功!");
        } else {
            List<GroupWelcomeInfo> finalGroupWelcomeInfos = groupWelcomeInfos;
            try {
                HibernateUtil.factory.fromTransaction(session -> {
                    session.remove(finalGroupWelcomeInfos);
                    return 0;
                });
            } catch (Exception e) {
                subject.sendMessage("欢迎词集合删除失败!");
                l.error("欢迎词集合删除失败!", e);
                return;
            }
            subject.sendMessage("欢迎词集合删除成功!");
        }
    }


    /**
     * 轮询次数递增
     *
     * @param welcomeInfo 欢迎消息
     * @author Moyuyanli
     * @date 2022/8/22 16:32
     */
    public static void increase(GroupWelcomeInfo welcomeInfo) {
        welcomeInfo.setPollingNumber(welcomeInfo.getPollingNumber() + 1);
        HibernateUtil.factory.fromTransaction(session -> session.merge(welcomeInfo));
    }

}
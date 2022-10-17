package cn.chahuyun.event;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.BlackListData;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.manage.GroupManager;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * GroupEventListener
 * 群事件
 *
 * @author Moyuyanli
 * @date 2022/8/22 10:34
 */
public class GroupEventListener extends SimpleListenerHost {

    private static final MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 其他群事件
     */
    @EventHandler
    public void onMessage(@NotNull GroupEvent event) { // 可以抛出任何异常, 将在 handleException 处理
        MemberPermission botPermission = event.getGroup().getBotPermission();
        if (botPermission == MemberPermission.MEMBER) {
            return;
        }
        if (event instanceof MemberJoinEvent) {
            MemberJoinEvent memberJoinEvent = (MemberJoinEvent) event;
            if (GroupManager.detectBlackList(memberJoinEvent)) {
                return;
            }
            GroupManager.userJoinGroup(memberJoinEvent);
        }


    }

    /**
     * 申请加入事件
     */
    @EventHandler
    public void onMessageTwo(@NotNull MemberJoinRequestEvent event)  { // 可以抛出任何异常, 将在 handleException 处理
        long groupId = event.getGroupId();
        MemberPermission botPermission = Objects.requireNonNull(event.getGroup()).getBotPermission();
        if (botPermission == MemberPermission.MEMBER) {
            return;
        }
        if (!ConfigData.INSTANCE.getGroupList().contains(groupId)) {
            return;
        }
        if (GroupManager.detectBlackList(event)) {
            return;
        }
        GroupManager.userRequestGroup(event);
    }

    /**
     * 退群事件
     */
    @EventHandler
    public void onMessageThree(@NotNull MemberLeaveEvent event)  { // 可以抛出任何异常, 将在 handleException 处理
        long groupId = event.getGroupId();
        MemberPermission botPermission = event.getGroup().getBotPermission();
        if (botPermission == MemberPermission.MEMBER) {
            return;
        }
        if (!ConfigData.INSTANCE.getGroupList().contains(groupId)) {
            return;
        }
        if (BlackListData.INSTANCE.isAutoBlackList()) {
            GroupManager.autoAddBlackList(event);
        }
    }


}
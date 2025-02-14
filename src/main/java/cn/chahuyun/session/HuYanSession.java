package cn.chahuyun.session;

import cn.chahuyun.command.SessionCommand;
import cn.chahuyun.config.BlackListData;
import cn.chahuyun.config.SessionConfig;
import cn.chahuyun.session.controller.*;
import cn.chahuyun.session.event.GroupEventListener;
import cn.chahuyun.session.event.MessageEventListener;
import cn.chahuyun.session.utils.HibernateUtil;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.utils.MiraiLogger;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;


/**
 * @author Moyuyanli
 * @description 插件主类
 * @date 2022/6/16 21:35
 */
public final class HuYanSession extends JavaPlugin {

    /**
     * HuYanSession唯一实例
     */
    public static final HuYanSession INSTANCE = new HuYanSession();
    /**
     * 当前插件版本
     */
    public static final String VERSION = "2.4.0";
    /**
     * 日志
     */
    public static final MiraiLogger log = INSTANCE.getLogger();
    /**
     * 插件配置
     */
    public static final SessionConfig config = SessionConfig.INSTANCE;

    private HuYanSession() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanSession", VERSION)
                .name("HuYanSession")
                .info("壶言会话-服务于你的群聊!")
                .author("Moyuyanli")
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", false)
                .build());
    }


    @Override
    public void onEnable() {
        log.info("===================HuYanSession2===================");
        // 确实花里胡哨- -||
//        log.info("    //    / /      \\\\    / /                   //   ) )                                                 ");
//        log.info("   //___ / /        \\\\  / /  ___       __     ((         ___      ___      ___     ( )  ___       __    ");
//        log.info("  / ___   / //   / / \\\\/ / //   ) ) //   ) )    \\\\     //___) ) ((   ) ) ((   ) ) / / //   ) ) //   ) ) ");
//        log.info(" //    / / //   / /   / / //   / / //   / /       ) ) //         \\ \\      \\ \\    / / //   / / //   / /  ");
//        log.info("//    / / ((___( (   / / ((___( ( //   / / ((___ / / ((____   //   ) ) //   ) ) / / ((___/ / //   / /   ");
        log.info("HuYanSession2 当前版本: " + "v" + VERSION);
        //加载前置
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(this);
        //初始化插件数据库
        HibernateUtil.init(configuration);
        //设定事件监听的父域
        EventChannel<Event> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE);
        //加载配置文件
        reloadPluginConfig(SessionConfig.INSTANCE);
        reloadPluginConfig(BlackListData.INSTANCE);
        getLogger().info("插件配置已加载！");
        if (SessionConfig.INSTANCE.getOwner() == 0) {
            getLogger().warning("主人还没有设置，请设置主人!");
        } else {
            log.info("主人已设置:" + SessionConfig.INSTANCE.getOwner());
        }
        //加载指令
        CommandManager.INSTANCE.registerCommand(SessionCommand.INSTANCE, true);
        log.info("插件指令已加载！");
        //加载数据信息
        ListAction.init(true);
        SessionAction.init(true);
        PowerAction.init(true);
        GroupProhibitedAction.init(true);
        ManySessionAction.init(true);
        QuartzAction.init();

        //注册群事件
        channel.registerListenerHost(new MessageEventListener());
        log.info("群消息监听已注册！");
        channel.registerListenerHost(new GroupEventListener());
        log.info("群动作监听已注册！");


        log.info("壶言会话插件加载完成!");
        log.info("===================HuYanSession2===================");
    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!感谢您的使用!");
    }


}
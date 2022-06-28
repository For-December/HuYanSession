package cn.chahuyun.commandManager;

import cn.chahuyun.files.ConfigData;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * 基本指令
 *
 * @author Zhangjiaxing
 * @description 基础指令
 * @date 2022/6/8 9:40
 */
public class CommandManage extends JCompositeCommand {

    public CommandManage(JvmPlugin jvmPlugin) {
        super(jvmPlugin,
                "hy", "壶言管理命令");
    }

    /**
     * @description
     * @author zhangjiaxing
     * @param sender
     * @date 2022/6/15 17:03
     * @return void
     */
    @SubCommand("pu")
    @Description("噗~")
    public void pu(CommandSender sender) {
        sender.sendMessage("噗~");
    }

    @SubCommand("power")
    @Description("设置他人添加管理权限")
    public void powerToOther(CommandSender sender,String s ,long group ,long qq ,String power) {
        String user = "m" + group + "." + qq;
        MessageChain messages = ConfigData.INSTANCE.setAdminList(s, user, power);
        sender.sendMessage(messages);
    }

    @SubCommand("addgroup")
    @Description("添加检测群")
    public void setGroup(CommandSender sender,long group) {
        MessageChain messages = ConfigData.INSTANCE.setGroupList(true, group);
        sender.sendMessage(messages);
    }

    @SubCommand("delgroup")
    @Description("删除检测群")
    public void delGroup(CommandSender sender,long group) {
        MessageChain messages = ConfigData.INSTANCE.setGroupList(false, group);
        sender.sendMessage(messages);
    }

}
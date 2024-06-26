package top.ycmt.thetrackofshadow.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.mainCommand

// 插件命令
@CommandHeader(
    name = "thetrackofshadow",
    aliases = ["trackofshadow", "tos"],
    permission = "thetrackofshadow.access",
    permissionDefault = PermissionDefault.TRUE
)
object CommandHeader {
    // 加入游戏命令
    @CommandBody(
        permission = "thetrackofshadow.command.join",
        optional = true,
        permissionDefault = PermissionDefault.TRUE
    )
    val join = JoinCommand.command

    // 观看游戏命令
    @CommandBody(
        permission = "thetrackofshadow.command.watch",
        optional = true,
        permissionDefault = PermissionDefault.TRUE
    )
    val watch = WatchCommand.command

    // 离开游戏命令
    @CommandBody(
        permission = "thetrackofshadow.command.quit",
        optional = true,
        permissionDefault = PermissionDefault.TRUE
    )
    val quit = QuitCommand.command

    // 显示帮助命令
    @CommandBody(
        permission = "thetrackofshadow.command.help",
        optional = true,
        permissionDefault = PermissionDefault.TRUE
    )
    val help = HelpCommand.command

    // 不填写子命令时显示
    @CommandBody
    val main = mainCommand {
        execute<ProxyCommandSender> { sender, cmd, _ ->
            HelpCommand.sendHelpMsg(sender, cmd) // 发送帮助信息
        }
    }
}
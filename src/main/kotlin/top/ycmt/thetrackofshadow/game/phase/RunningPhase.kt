package top.ycmt.thetrackofshadow.game.phase

import org.bukkit.entity.Player
import taboolib.module.chat.impl.DefaultComponent
import top.ycmt.thetrackofshadow.constant.GameConst.GAME_MAX_TIME
import top.ycmt.thetrackofshadow.constant.LegacyTextConst.CN_LOGO_LEGACY_TEXT
import top.ycmt.thetrackofshadow.game.Game
import top.ycmt.thetrackofshadow.game.flow.*
import top.ycmt.thetrackofshadow.game.state.CancelState
import top.ycmt.thetrackofshadow.game.task.SpawnWorldTask
import top.ycmt.thetrackofshadow.pkg.chat.GradientColor.toGradientColor
import top.ycmt.thetrackofshadow.pkg.scoreboard.ScoreBoard
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// 游戏运行阶段
class RunningPhase(private val game: Game) : PhaseAbstract() {
    private var gameTick = 0L // 游戏tick

    // 游戏流程事件列表
    private val flowEvents: List<FlowInterface> = listOf(
        TeleportFlow(game), // 随机传送流程
        StartFlow(game), // 开始游戏流程
        RandomFlow(game), // 随机事件流程
        PVPFlow(game), // PVP开启流程
    )

    init {
        initGame()
    }

    // 初始化游戏
    private fun initGame() {
        // 初始化玩家积分
        game.scoreModule.initPlayersScore(game.playerModule.getAlivePlayers())
        // 初始化玩家统计信息
        game.statsModule.initPlayersStats(game.playerModule.getAlivePlayers())
        // 初始化子任务
        initTask()
        // 初始化玩家禁止状态
        game.cancelModule.addGlobalCancelState(
            CancelState.CANCEL_PVP, // 禁止PVP
            CancelState.CANCEL_OPEN_CHEST, // 禁止打开宝箱
        )
        game.playerModule.getAlivePlayers().forEach {
            // 初始化玩家
            game.playerModule.initPlayer(it)
            // 清除游戏玩家的计分板
            ScoreBoard.removeScore(it)
            // 发送玩法介绍
            sendGameIntroduce(it)
        }
    }

    // 初始化子任务
    private fun initTask() {
        game.subTaskModule.submitTask(period = 1 * 20L, task = SpawnWorldTask(game))
    }

    override fun onTick() {
        // 刷新记分板
        refreshBoard()

        // 胜利者是最后一名玩家 或 时间超出设置的时间 结算游戏
        if (game.playerModule.getOnlinePlayers().size <= 1 ||
            gameTick >= TimeUnit.MINUTES.toSeconds(GAME_MAX_TIME)
        ) {
            // 此阶段结束 进入结算阶段
            this.done()
            return
        }

        // 执行每一个事件 事件内部判断条件是否符合
        for (event in flowEvents) {
            // 剩余的时间
            val leftTime = event.finishTick - gameTick
            // 判断时间是否符合条件
            if (leftTime < 0L) {
                continue
            }
            event.exec(leftTime)
        }

        gameTick++
    }

    // 获取距离最近的事件
    private fun getRecentlyEventMsg(): String {
        var resultLeftTime = -1L
        var resultEventMsg = ""
        // 获取每一个事件的时间
        for (event in flowEvents) {
            // 剩余的时间
            val leftTime = event.finishTick - gameTick
            // 判断时间是否符合条件
            if (leftTime < 0L) {
                continue
            }
            // 初始赋值 或 剩余时间小于则替换
            if (resultLeftTime == -1L || resultEventMsg == "" || leftTime < resultLeftTime) {
                resultLeftTime = leftTime
                resultEventMsg = event.eventMsg
            }
        }

        // 获取到距离最近的事件则返回
        if (resultLeftTime != -1L && resultEventMsg != "") {
            return "§f$resultEventMsg - <#dcffcc,9adbb1>${timeToString(resultLeftTime)}</#>".toGradientColor()
        }
        // 默认信息
        return "§f距离结束时间 - <#dcffcc,9adbb1>${timeToString(TimeUnit.MINUTES.toSeconds(GAME_MAX_TIME) - gameTick)}</#>".toGradientColor()
    }

    // 获取距离结束的字符串时间
    private fun timeToString(time: Long): String {
        val str = StringBuilder()
        val minutes = time / 60
        val seconds = time % 60
        str.append(minutes).append(":")
        if (seconds <= 0) {
            str.append("00")
        } else {
            if (seconds <= 9) {
                str.append("0").append(seconds)
            } else {
                str.append(seconds)
            }
        }
        return str.toString()
    }

    // 刷新或创建计分板
    private fun refreshBoard() {
        // 设置日期格式
        val df = SimpleDateFormat("MM/dd/yy")

        for (p in game.playerModule.getOnlinePlayers()) {
            val board: ScoreBoard =
                if (ScoreBoard.hasScore(p)) ScoreBoard.getByPlayer(p)!! else ScoreBoard.createScore(
                    p,
                    CN_LOGO_LEGACY_TEXT
                )
            board.setSlot(15, "§7${df.format(Date())}  §8${game.setting.gameName}")
            board.setSlot(14, "")
            board.setSlot(13, getRecentlyEventMsg())
            board.setSlot(12, "")
            board.setSlot(
                11,
                "§f1. Null"
            )
            board.setSlot(
                10,
                "§f2. Null"
            )
            board.setSlot(
                9,
                "§f3. Null"
            )
            board.setSlot(8, "")
            board.setSlot(
                7,
                "§f剩余藏宝点 Null §f个"
            )
            board.setSlot(6, "")
            board.setSlot(5, "§f你的积分: <#dcffcc,9adbb1>Null</#>".toGradientColor())
            board.setSlot(4, "§f击杀玩家数: <#dcffcc,9adbb1>Null</#>".toGradientColor())
            board.setSlot(3, "§f找到的宝箱数: <#dcffcc,9adbb1>Null</#>".toGradientColor())
            board.setSlot(2, "")
            board.setSlot(1, "<#fff4ba,f4f687>mc.ycmt.top</#>".toGradientColor())
        }
        for (p in game.playerModule.getOnlineWatchers()) {
            val board: ScoreBoard =
                if (ScoreBoard.hasScore(p)) ScoreBoard.getByPlayer(p)!! else ScoreBoard.createScore(
                    p,
                    CN_LOGO_LEGACY_TEXT
                )
            board.setSlot(13, "§7${df.format(Date())}  §8${game.setting.gameName}")
            board.setSlot(12, "")
            board.setSlot(11, getRecentlyEventMsg())
            board.setSlot(10, "")
            board.setSlot(
                9,
                "§f1. Null"
            )
            board.setSlot(
                8,
                "§f2. Null"
            )
            board.setSlot(
                7,
                "§f3. Null"
            )
            board.setSlot(
                6,
                "§f4. Null"
            )
            board.setSlot(
                5,
                "§f5. Null"
            )
            board.setSlot(4, "")
            board.setSlot(
                3,
                "§f剩余藏宝点 Null §f个"
            )
            board.setSlot(2, "")
            board.setSlot(1, "<#fff4ba,f4f687>mc.ycmt.top</#>".toGradientColor())
        }
    }

    // 发送游戏介绍
    private fun sendGameIntroduce(player: Player) {
        player.sendMessage(
            "",
            DefaultComponent()
                .append("<#deffd2,bee8ff>■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■</#>".toGradientColor())
                .bold()
                .toLegacyText(),
            "",
            "                              $CN_LOGO_LEGACY_TEXT",
            "",
            DefaultComponent()
                .append("<#fff4ba,f4f687>搜寻各种物资，强化自己的战斗能力，最终决战击败其他玩家！</#>".toGradientColor())
                .bold()
                .toLegacyText(),
            DefaultComponent()
                .append("<#fff4ba,f4f687>争夺唯一的王位，死亡不是唯一的终点，存活到最后迎接胜利。</#>".toGradientColor())
                .bold()
                .toLegacyText(),
            "",
            DefaultComponent()
                .append("<#deffd2,bee8ff>■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■</#>".toGradientColor())
                .bold()
                .toLegacyText(),
            "",
        )
    }

}
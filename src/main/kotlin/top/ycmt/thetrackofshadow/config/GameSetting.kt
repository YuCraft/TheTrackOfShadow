package top.ycmt.thetrackofshadow.config

import taboolib.common.util.Vector

// 游戏设置数据
data class GameSetting(
    // 游戏名称
    val gameName: String,
    // 地图名称
    val mapName: String,
    // 开始游戏最少所需玩家数量
    val minPlayerCount: Int,
    // 最大玩家数量
    val maxPlayerCount: Int,
    // 大厅等待时间 单位秒
    val lobbyWaitTick: Long,
    // 游戏地图坐标
    val gameMapWorld: String,
    val gameMapVector1: Vector,
    val gameMapVector2: Vector,
)